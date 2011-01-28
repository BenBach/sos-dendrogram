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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.player;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.URLStringParser;

import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;

/**
 * @author Jakob Frank
 * @version $Id: StandalonePlaySOMPlayer.java 3873 2010-10-28 09:29:58Z frank $
 */
public class StandalonePlaySOMPlayer extends JFrame implements PlayerListener {

    private static final long serialVersionUID = 1L;

    private PlayList playList;

    private File tmpSongDir;

    private PocketSOMConnectionHandler connector;

    public StandalonePlaySOMPlayer(URL url, int pin) {

        File tmpDir = new File(System.getProperty("java.io.tmpdir") + "/Player-" + System.currentTimeMillis() % 100000);
        tmpDir.deleteOnExit();
        tmpSongDir = new File(tmpDir, "audio");
        tmpSongDir.mkdirs();
        tmpSongDir.deleteOnExit();

        initialize();

        try {
            connector = new PocketSOMConnectionHandler(url, pin);
            // connector = new PocketSOMConnectionHandler("localhost", 1234);
            connector.start();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void initialize() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                connector.shutdown();
                System.exit(0);
            }

        });

        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.weightx = 2;
        gc.fill = GridBagConstraints.BOTH;

        this.setContentPane(main);

        playList = new PlayList();
        playList.addPlayerListener(this);
        JList liste = playList.createMatchingJList(true);
        JScrollPane scpPlayList = new JScrollPane(liste);

        main.add(new PlayerControl(playList), gc);
        gc.gridy += 1;

        gc.weighty = 1;
        main.add(scpPlayList, gc);
        gc.weighty = 0;
        gc.gridy += 1;

        JButton btnExtPlayer = new JButton("External");
        btnExtPlayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File m3u = File.createTempFile("external", ".m3u");
                    Iterator<File> iF = playList.getSongs().iterator();
                    PrintStream ps = new PrintStream(m3u);
                    ps.println("#EXTM3U");
                    while (iF.hasNext()) {
                        ps.println();
                        ps.println(iF.next().getAbsolutePath());
                    }
                    ps.close();

                    Runtime.getRuntime().exec(String.format("%s %s", "vlc", m3u.getAbsolutePath()));
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        });
        main.add(btnExtPlayer, gc);
        gc.gridy += 1;

        /* Core-Functionality done, now some extras */

        // Export Playlist
        // gc.gridx += 1;
        main.add(createExportPlaylistButton(liste), gc);

        pack();
    }

    /**
     * @param args Commandline args
     */
    public static void main(String[] args) {
        try {
            JSAP jsap = new JSAP();
            jsap.registerParameter(new UnflaggedOption("url", URLStringParser.getParser(), true, "URL of the psom-file"));
            jsap.registerParameter(new FlaggedOption("pin", JSAP.INTEGER_PARSER, "1234", false, 'p', "pin",
                    "The PIN to access the remote PlaySOM"));
            JSAPResult res = jsap.parse(args);
            if (res.success()) {
                new StandalonePlaySOMPlayer(res.getURL("url"), res.getInt("pin")).setVisible(true);
            } else {
                System.err.printf("USAGE: %s%n", jsap.getUsage());
                System.exit(1);
            }
        } catch (JSAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void playStarted(int mode, AudioVectorMetaData song) {
        // TODO Auto-generated method stub

    }

    @Override
    public void playStopped(int reason, AudioVectorMetaData song) {
        // TODO Auto-generated method stub

    }

    private JButton createExportPlaylistButton(final JList liste) {
        JButton b = new JButton("Export Playlist");

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(StandalonePlaySOMPlayer.this) != JFileChooser.APPROVE_OPTION) {
                    // Canceled
                    return;
                }
                File f = fc.getSelectedFile();
                if (!f.getName().endsWith(".m3u")) {
                    f = new File(f.getParentFile(), f.getName() + ".m3u");
                }

                try {
                    PrintStream ps = new PrintStream(f);

                    // Header
                    ps.println("#EXTM3U");

                    Iterator<File> iF = playList.getSongs().iterator();
                    Iterator<String> iD = playList.getDataItems().iterator();
                    while (iF.hasNext() && iD.hasNext()) {
                        File song = iF.next();
                        String title = iD.next();

                        ps.printf("%n#EXTINF:-1,%s%n%s%n", title, song);
                    }
                    ps.close();
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        return b;
    }

    private class PocketSOMConnectionHandler extends Thread {

        private static final String KEY_BASE_PATH = "collectionURL";

        private static final String KEY_CONNECTOR = "playSOM_Address";

        private final String basePath;

        private final int pin;

        private Socket ctrlSocket, dataSocket;

        private boolean playLocal = true;

        public PocketSOMConnectionHandler(URL url, int pin) throws UnknownHostException, IOException {

            this.pin = pin;

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // Read configuration
            Properties p = new Properties();
            p.load(con.getInputStream());

            String bp = p.getProperty(KEY_BASE_PATH);
            if (!bp.endsWith("/")) {
                bp += "/";
            }
            basePath = bp;

            String newAddress = p.getProperty(KEY_CONNECTOR);
            this.setName(getClass().getSimpleName() + " --> " + newAddress);

            String[] nas = newAddress.split(":");
            ctrlSocket = new Socket(nas[0], Integer.parseInt(nas[1]));
            dataSocket = new Socket(nas[0], Integer.parseInt(nas[1]));
        }

        @Override
        public void run() {

            try {
                PrintStream out = new PrintStream(ctrlSocket.getOutputStream(), true, "utf-8");
                BufferedReader in = new BufferedReader(new InputStreamReader(ctrlSocket.getInputStream()));

                PrintStream dataOut = new PrintStream(dataSocket.getOutputStream(), true, "utf-8");
                BufferedReader dataIn = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

                // Register
                if (!login(in, out)) {
                    return;
                }
                out.println("Register");

                if (!login(dataIn, dataOut)) {
                    return;
                }

                if (dataSocket.getInetAddress().isLoopbackAddress()) {
                    playLocal = false;
                }
                // Get the initial Playlist
                handlePlaylistChange(dataIn, dataOut);

                // Now listen...
                String line;
                while ((line = in.readLine()) != null) {
                    if (!line.startsWith("INFO: ")) {
                        continue;
                    }

                    if (line.startsWith("INFO: currentsong")) {
                        handleSongChange(line);
                    } else if (line.startsWith("INFO: playlist changed")) {
                        handlePlaylistChange(dataIn, dataOut);
                    } else {
                        System.out.printf("%s%n", line);
                    }
                }

                out.println("Unregister");
                out.println("Quit");
                dataOut.println("Quit");

                out.close();
                dataOut.close();
                in.close();
                dataIn.close();
                ctrlSocket.shutdownOutput();
                ctrlSocket.shutdownInput();
                dataSocket.shutdownOutput();
                dataSocket.shutdownInput();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void shutdown() {
            try {
                dataSocket.shutdownInput();
                dataSocket.shutdownOutput();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        private boolean login(BufferedReader in, PrintStream out) throws IOException {
            out.println("Login: Pin");
            out.println("PIN: " + pin);
            String response = in.readLine();
            if (!response.equals("Login: OK")) {
                System.err.printf("Login failed! (%s)%n");
                return false;
            }
            // out.println("Register");
            return true;
        }

        private void handlePlaylistChange(BufferedReader dataIn, PrintStream dataOut) {
            try {
                dataOut.println("Get: PlayList");
                playList.clearPlaylist();

                HashMap<String, File> dlList = new HashMap<String, File>();

                String line;
                while ((line = dataIn.readLine()) != null) {
                    if (line.startsWith("Song:")) {
                        String song = line.substring(line.indexOf(' ') + 1);
                        File songfile = new File(tmpSongDir, song);
                        songfile.createNewFile();
                        dlList.put(basePath + song, songfile);
                        playList.addSong(song, songfile);
                    } else if (line.equalsIgnoreCase("Playlist")) {
                        // Do nothing
                    } else if (line.equalsIgnoreCase("EndList")) {
                        // Ensure repainting.
                        StandalonePlaySOMPlayer.this.validate();
                        break;
                    } else {
                        System.err.printf("Illegal Command: \"%s\"%n", line);
                        break;
                    }
                }

                new MusicDownloadThread(dlList).start();

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.printf("x%n");
        }

        private void handleSongChange(String line) {
            String[] ls = line.split("\\s+", 3);

            if (ls.length > 2) {
                String song = ls[2];
                playList.stop();
                if (playLocal) {
                    playList.play(playList.getIndexOf(song));
                } else {
                    playList.setCurrentSong(playList.getIndexOf(song));
                }
            } else {
                playList.stop();
            }
            System.out.printf("x%n");
        }

        private class MusicDownloadThread extends Thread {
            private final HashMap<String, File> dlList;

            private final String basename;

            private static final String USER_AGENT = "PlaySOMPlayer/1.1 (standalone)";

            public MusicDownloadThread(HashMap<String, File> dlList) {
                basename = getClass().getSimpleName();
                setName(basename + " (idle)");
                this.dlList = dlList;
            }

            private void setProgress(int currentItem) {
                setName(basename + " (" + currentItem + "/" + dlList.size() + ")");
            }

            @Override
            public void run() {
                int i = 0;
                Set<Entry<String, File>> list = dlList.entrySet();
                for (Entry<String, File> entry : list) {
                    try {
                        setProgress(++i);
                        URL url = new URL(entry.getKey());
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestProperty("User-Agent", USER_AGENT);
                        con.connect();

                        InputStream is = con.getInputStream();
                        FileOutputStream os = new FileOutputStream(entry.getValue());

                        byte[] buffer = new byte[1024];
                        int len;
                        int all = 0;
                        while ((len = is.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                            all += len;
                        }
                        os.flush();

                        is.close();
                        os.close();
                        con.disconnect();
                        System.out.printf("Got %s (%.2fkB)%n", entry.getValue().getName(), all / 1024d);
                    } catch (MalformedURLException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        entry.getValue().delete();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        entry.getValue().delete();
                    }
                }
                setName(basename + " (got " + dlList.size() + ")");
            }
        }
    }

}
