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

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

public class HttpErrorHandler {

    private static final String SERVER_FOOTER = "SOMViewer - PocketSOMConnector";

    private static final String HTML_000 = "<html><head><title>%1$d - %2$s</title><body><h1>%1$d - %2$s</h1><p>%3$s</p><p>%4$s</p><hr><address>%5$s</addess></body><html>";

    public static void sendError(HttpExchange t, int errCode) throws IOException {
        sendError(t, errCode, "");
    }

    public static void sendError(HttpExchange t, int errCode, String message) throws IOException {
        t.getRequestBody();

        String title, descr;
        switch (errCode) {
            case 404:
                title = "Not found";
                descr = String.format("Requested document %s does not exist.", t.getRequestURI().getPath());
                break;
            case 500:
                title = "Internal Error";
                descr = "Internal server error occured. Sorry!";
                break;
            case 501:
                title = "Not implemented";
                descr = String.format("Method %s not implemented.", t.getRequestMethod());
                break;
            default:
                title = "Crestfallen";
                descr = "I could not satisfy your request. Sorry!";
                break;
        }
        String response = String.format(HTML_000, errCode, title, descr, message, SERVER_FOOTER);

        t.sendResponseHeaders(errCode, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}