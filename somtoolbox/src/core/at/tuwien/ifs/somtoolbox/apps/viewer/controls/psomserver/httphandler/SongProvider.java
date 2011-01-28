/*
 * Copyright 2004-2010 Information & Software Engineering Group (188/1)
 *                     Institute of Software Technology and Interactive Systems
 *                     Vienna University of Technology, Austria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.ifs.tuwien.ac.at/dm/somtoolbox/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver.httphandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;

/**
 * The SongProvider sends requested Songs via http.
 * 
 * @author Jakob Frank
 */
public class SongProvider implements HttpHandler {
    private static final String LOG_SEP = " - ";

    private Logger log;

    private final String pathOffset;

    public SongProvider(CommonSOMViewerStateData state, String context) {
        this.pathOffset = context;
        log = Logger.getLogger(this.getClass().getName());
    }

    private void sendSong(HttpExchange t, File song) throws IOException {
        log.info("Delivering song: " + song.getAbsolutePath() + " (" + song.length() + " Byte)");
        FileInputStream fis = new FileInputStream(song);

        int size = fis.available();
        byte[] bSong = new byte[size];
        fis.read(bSong);

        log.info(200 + LOG_SEP + t.getRequestURI().toString());
        t.getResponseHeaders().add("Accept-Ranges", "bytes");
        t.getResponseHeaders().add("Content-Type", "audio/mpeg; name=\"" + song.getName() + "\"");
        t.sendResponseHeaders(200, size);

        if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            OutputStream os = t.getResponseBody();
            os.write(bSong);
            os.close();
        }
        t.close();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String path = t.getRequestURI().getPath();
        path = path.replaceFirst(pathOffset, "");
        File song = new File(CommonSOMViewerStateData.fileNamePrefix, path);
        if (song.exists()) {
            sendSong(t, song);
        } else {
            log.info(song.getAbsolutePath() + " not found");
            HttpErrorHandler.sendError(t, 404);
        }

    }

}