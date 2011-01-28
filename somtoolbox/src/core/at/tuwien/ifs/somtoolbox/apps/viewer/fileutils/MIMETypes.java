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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

/**
 * utility class to handle different filetypes <br>
 * shall contain actions for all needed filetypes
 * <p>
 * <i>Created on Dec 26, 2004</i>
 * </p>
 * 
 * @author Robert Neumayer
 */
public class MIMETypes {

    private String audioPlayer = "xmms"; // default player - can be overridden in somviewer.prop

    private String[] ALLOWED_FILE_ENDINGS = { "mp3", "wav", "ogg", "au" };

    private String PLAYLIST_TMP_FILENAME = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator")
            + "tmp.m3u";

    /**
     * sets the preferred audio player for files of type audio
     * 
     * @param audioPlayer - a string containing the path to the audio player
     */
    public void setAudioPlayer(String audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    /**
     * check if the selection consists of only one or more different filetypes
     * 
     * @param playList - selected playlist
     * @return - true if only one filetype, false if more
     */
    public boolean hasSingleFileType(Object[] playList) {
        if (playList.length == 0) {
            return true;
        }

        String type = getFileType(playList[0]);

        for (int i = 0; i < playList.length; i++) {
            if (!type.equals(getFileType(playList[i]))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if the play list can be exported, false otherwise.
     */
    public boolean isExportable(Object[] playList) {
        // if(getFileType(playList).equals("mp3") || getFileType(playList).equals("wav")) return true;

        for (int h = 0; h < playList.length; h++) {
            boolean found = false;
            // System.out.println("h: " + h + " " + playList[h]);
            for (int i = 0; i < ALLOWED_FILE_ENDINGS.length; i++) {
                if (!ALLOWED_FILE_ENDINGS[i].equalsIgnoreCase(getFileType(playList[h]))) {
                    // System.out.println(found);
                    found = false;
                } else {
                    // System.out.println(ALLOWED_FILE_ENDINGS[i] + " " + " " + playList[h] + " "
                    // + MIMETypes.getFileType(playList[h]) + found);
                    found = true;
                    break;
                }
            }
            if (found) {
                // System.out.println(playList[h] + " " + MIMETypes.getFileType(playList[h]) + found);
            } else {
                return false;
            }
        }
        return true;
    }

    /** get the filetype for a given selection (e.g. "mp3") */
    public String getFileType(Object[] playList) {
        if (hasSingleFileType(playList)) {
            return getFileType(playList[0]);
        } else {
            return "mixed";
        }
    }

    /**
     * do the appropriate syscalls for a single selection
     * 
     * @param o - the selected element
     */
    public void doSysCalls(Object o) {
        Object[] oar = new Object[1];
        oar[0] = o;
        doSysCalls(oar);
    }

    /**
     * does the syscalls for multiple selections
     * 
     * @param playList - selected playlist
     */
    public void doSysCalls(Object[] playList) {

        if (playList.length == 0) {
            return;
        }

        // String fileType;

        // check for Win or Linux
        // if (System.getProperty("os.name").equals("Linux"))
        // binary = "xmms";
        // else
        // binary = "winamp";

        // if (hasSingleFileType(playList)) {
        // fileType = getFileType(playList);
        // } else {
        // Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Playlist consists of multiple filetypes.");
        // in this case try to call app suitable for filetype of 1st playlist entry
        // fileType = getFileType(playList[0]);
        // return;
        // }
        // if only mp3s are selected write all songs to a playlist file and send it to xmms
        // if(fileType.equals("mp3")) {
        if (isExportable(playList)) {
            PlayList pl = new PlayList(PLAYLIST_TMP_FILENAME);
            for (Object element : playList) {
                pl.addSong(((String) element));
            }
            pl.writeToFile();

            call(audioPlayer, PLAYLIST_TMP_FILENAME);
            return;
        }

        // TODO just for testing: pdfs are sent to xmms as well
        /*
         * if(fileType.equals("pdf")) { PlayList pl = new PlayList(PLAYLIST_TMP_FILENAME); for(int i = 0; i < playList.length; i++){
         * pl.addSong(((String) playList[i])); } pl.writeToFile(); call("xmms", PLAYLIST_TMP_FILENAME); return; }
         */
        /*
         * if(fileType.equals("txt")) { // maybe concat and send to firefox? } if(fileType.equals("pdf")) { // directly send to acroread? }
         */
        // the last way out: firefox
        // urlencode and call firefox
        try {
            for (Object element : playList) {
                call("firefox", URLEncoder.encode((String) element, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
        }

    }

    private String getFileType(Object fullPathName) {
        String pathName = (String) fullPathName;
        return pathName.substring(pathName.lastIndexOf(".") + 1, pathName.length());
    }

    /**
     * sends a system call to the given app using the given parameters
     * 
     * @param app - application to be executed
     * @param params - parameters
     */
    public void call(String app, String params) {
        try {
            Runtime.getRuntime().exec(app + " " + params);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Doing syscall: " + app + " " + params);
        } catch (IOException e) {
            System.err.println("Call to  " + app + " somehow went wrong: " + e.getMessage());
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Error executing external application: " + app + " " + params);
        }
    }

}
