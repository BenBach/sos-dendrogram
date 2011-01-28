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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * This class contains static fields, methods and some additional logic that is used by multichannel playback related
 * classes
 * 
 * @author Ewald Peiszer
 * @version $Id: Commons.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class Commons {

    public static String sUnitDescFileName = null;

    public static String sJukeboxdir = null;

    public static String sDecodedOutputDir = "." + File.separator;

    public static float p_decode = 0.5f;

    public static Logger log = Logger.getLogger("multichannel");

    public static Random rand = new Random();

    public static LinkedHashMap<File, File> lhmDecoded = new LinkedHashMap<File, File>(50); // <File, File>

    // stats vars
    public static int iSongscount = 0;

    public static int iDecodedcount = 0;

    /** Time when playback was started */
    public static long lStarttime;

    /** Format to display playback time */
    public static SimpleDateFormat sdfHHmmss = new SimpleDateFormat("HH:mm:ss");
    static {
        sdfHHmmss.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static ControlFrame cf;

    /** Resource directory: {@value #RES_DIR} */
    final static String RES_DIR = "rsc/multichannel/";

    final static FileFilter ASSIGNMENT_FILEFILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            String filename = file.getName();
            return filename.endsWith(PROPFILE_SUFFIX);
        }

        @Override
        public String getDescription() {
            return "*" + PROPFILE_SUFFIX;
        }
    };

    public static final boolean bBigEndian = false;

    public static final AudioFormat datalineformat = new AudioFormat(44100, 16, 2, true, bBigEndian);

    public static final AudioFormat monoformat = new AudioFormat(44100, 16, 1, true, bBigEndian);

    public static final DataLine.Info datalineformat_info = new DataLine.Info(SourceDataLine.class,
            Commons.datalineformat);

    public static final Color LIGHTRED = new Color(255, 220, 220);

    public static final Color HIGHLIGHT = new Color(255, 227, 127);

    public static final Color MUTED_SPEAKER = Color.lightGray;

    public static final String[] A_FINDME_FILES = { "00.wav", "01.wav", "02.wav", "03.wav", "04.wav", "05.wav",
            "06.wav", "07.wav", "08.wav", "09.wav", "10.wav" };

    public static final String FINDME_LEFT = "left.wav";

    public static final String FINDME_RIGHT = "right.wav";

    public static final String FINDME_SILENCE = "silence.wav";

    public static final String FINDME_GENERIC = "findme_generic.mp3";

    public static final String FINDME_INTRO = "this_is_channel.wav";// epei2

    public static final long SLEEPFOR = 4800; // epei2

    public static final String DECODED_SUFFIX = " Decoded.wav";

    public static final String APP_NAME = "Distributed Music";

    public static final String LOGFILENAME = "multichannel.log";

    public static final String INSTRUCTIONS_HTMLFILENAME = RES_DIR + "multichannel-helppage.html";

    public static final int MAX_TOOLTIP_COLUMNS = 50;

    public static final int MAX_TOOLTIP_LINES = 9;

    // for assignment save files
    /** Separater for use in keys for property files */
    public static final char SEP_IN_KEY = '-';

    /** Separater for use in values for property files */
    public static final char SEP_IN_VALUE = ',';

    public static final String KEY_DIM_LAYOUT_TABLE = "dimension_layout_table";

    public static final String KEY_DIM_SOM = "dimension_SOM";

    public static final String KEY_FILE_SOM = "filename_SOM";

    public static final String KEY_ASSIGNMENT = "assignment";

    public static final String KEY_SPEAKER = "speaker";

    public static final String PROPFILEHEADER = "ASSIGNMENT FILE for " + APP_NAME + " (part of PlaySOM)"
            + "-------------------------------------------------------------"
            + "-- Do not edit this file \"by hand\" unless you know what you are doing.";

    public static final String PROPFILE_SUFFIX = ".assignment";

    // Set Logfile
    static {
        try {
            Handler h = new FileHandler(LOGFILENAME);
            h.setFormatter(new SimpleFormatter());
            log.addHandler(h);
            log.setLevel(Level.FINEST);
        } catch (Exception ex) {
            log.warning("Cannot enable logfile: " + LOGFILENAME);
        }
    }

    /** Plays two sound files on the required output line */
    public static void playSound(String file1, String file2, int iLine) {
        playSound(new String[][] { { file1 }, { file2 } }, iLine);
    }

    /** Overloading: <code>playSound</code> with an array of files */
    public static void playSound(String[][] aFiles, int iLine) {
        try {
            SourceDataLine sdl = (SourceDataLine) LineListModel.getOurMixerAt(iLine).getLine(
                    Commons.datalineformat_info);
            String[][] aNames = new String[2][];
            for (int j = 0; j < 2; j++) {
                aNames[j] = new String[aFiles[j].length];
                for (int i = 0; i < aFiles[j].length; i++) {
                    aNames[j][i] = Commons.RES_DIR + aFiles[j][i];
                }
            }
            TPlaybackThreadDataRecord record = new TPlaybackThreadDataRecord();
            record.addSongs(0, aNames[0]);
            record.addSongs(1, aNames[1]);

            PlaybackThread nt = new PlaybackThread("findme", record, sdl, false, 0.0f, false);
            if (nt.ready) {
                nt.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes sure that the given String ends with the OS-correct File.separator ('/' on Unix, '\\' on Windows)
     */
    public static String makeSureThatLastCharIsACorrectFileSeparator(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            // Cut last char and OS-append correct separator
            return path.substring(0, path.length() - 1) + File.separator;
        } else {
            return path + File.separator;
        }
    }

    /**
     * Returns the string until (excluding) the first dot (.)
     * 
     * @return filename without suffices
     */
    public static String stripSuffix(String sMitSuffix) {
        int pos = sMitSuffix.indexOf(".");
        return sMitSuffix.substring(0, pos);
    }

    /**
     * Places the given component in the middle of the screen.
     * <p>
     * Actually intended for <code>JFrame</code> and <code>
     * JDialog</code>, but <code>java.awt.Component</code> is superclass
     * of both.
     */
    public static void centerWindow(Component fenster) {
        // Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = fenster.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        fenster.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    /** Removes the last "number" characters of the string */
    public static String cutEndOfString(String in, int number) {
        return in.substring(0, in.length() - number);
    }

    /**
     * Gives the user the possibility to select a filename using the Filechooser-Dialog
     * <p>
     * suffix should start with the .
     * 
     * @return the selected file or null if the user canceled the dialog
     */
    public static File getChosenFile(boolean bSave, Component parent, final String suffix, JFileChooser jfc)
            throws Exception {
        File f;

        jfc.rescanCurrentDirectory();

        jfc.setFileFilter(Commons.ASSIGNMENT_FILEFILTER);

        int ret;
        if (bSave) {
            ret = jfc.showSaveDialog(parent);
        } else {
            ret = jfc.showOpenDialog(parent);
        }
        if (ret == JFileChooser.APPROVE_OPTION) {
            // Es wurde auf Speichern/Laden geklickt
            f = jfc.getSelectedFile();
            // Fehlen die Suffixe?
            if (!f.getName().endsWith(suffix)) {
                // Suffixe anhängen
                f = new File(f.getCanonicalFile() + suffix);
            }
            if (bSave) {
                // Datei speichern
                if (!f.exists()
                        || JOptionPane.showConfirmDialog(parent, f.getAbsolutePath() + "\nOverwrite?",
                                "Please confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

                    return f;
                } else {
                    // Datei existiert und Bestätigungsdialog war nicht YES
                    return null;
                }
            } else {
                // Datei laden
                if (f.exists()) {
                    return f;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

}