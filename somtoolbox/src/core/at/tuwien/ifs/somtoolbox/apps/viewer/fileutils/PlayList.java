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
package at.tuwien.ifs.somtoolbox.apps.viewer.fileutils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Add songs to a .m3u playlist and write the playlist file to disk
 * 
 * @author Robert Neumayer
 * @version $Id: PlayList.java 3586 2010-05-21 10:34:19Z mayer $
 */
public class PlayList {

    private Vector<String> songs;

    private File file;

    public PlayList(String fileName) {
        super();
        this.songs = new Vector<String>();
        this.file = new File(fileName);
    }

    public PlayList(File file) {
        super();
        this.songs = new Vector<String>();
        this.file = file;
    }

    /**
     * adds a song to the playlist
     * 
     * @param song - song to add
     * @return - true if okay, false otherwise
     */
    public boolean addSong(String song) {
        return this.songs.add(song);
    }

    /**
     * set the contents of an object array as playlist
     * 
     * @param sar - object array to be set as playlist (and casted to strings)
     */
    public void setSongs(Object[] sar) {
        for (Object element : sar) {
            this.songs.add((String) element);
            // System.out.println("adding song: " + sar[i]);
        }
    }

    /**
     * write this playlist to the file specified in the constructor
     */
    public void writeToFile() {
        // was used to restore the whitespaces in filenames
        // from now on only URL-encoded names are supported
        // this.restoreUnderscores();
        // System.out.println(this.songs.size());
        try {
            FileWriter fw = new FileWriter(this.file);
            for (int i = 0; i < this.songs.size(); i++) {
                fw.write(songs.elementAt(i) + "\n");
            }
            fw.close();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Exported file: " + this.file.getAbsolutePath() + " to disk");
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Could not initialize FileWriter for file: " + this.file);
            e.printStackTrace();
        }
    }
}
