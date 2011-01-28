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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;

public class PlayList implements PlayerListener {

    private static final long serialVersionUID = 1L;

    private List<AudioVectorMetaData> plContent;

    private PlayListModel lm = null;

    private int currentSong;

    private boolean isPlaying = false;

    private AudioPlayer player;

    private Vector<PlayerListener> listeners;

    private Vector<PlayListListener> plListeners;

    private NotificationPostponer pllNP;

    private boolean showFilenames = false;

    public PlayList(AudioPlayer player) {
        listeners = new Vector<PlayerListener>();
        plListeners = new Vector<PlayListListener>();
        plContent = new ArrayList<AudioVectorMetaData>();
        player.addMP3PlayerListener(this);
        this.player = player;
    }

    public PlayList() {
        this(new AudioPlayer());
    }

    public boolean addAllSongs(List<String> songs) {
        List<File> songfiles = new ArrayList<File>();
        for (String song : songs) {
            songfiles.add(new File(CommonSOMViewerStateData.fileNamePrefix + song
                    + CommonSOMViewerStateData.fileNameSuffix));
        }
        return addAllSongs(songs, songfiles);
    }

    public boolean addAllSongs(List<String> songs, List<File> songfiles) {
        // System.out.println("addAllSongs");
        boolean e = true;
        for (File file : songfiles) {
            try {
                e = plContent.add(AudioVectorMetaData.createMetaData(file)) & e;
            } catch (FileNotFoundException e1) {
                e = false;
            }
        }
        if (lm != null) {
            lm.fireContentsChanged(this, 0, plContent.size());
        }
        informPlayListListeners();
        return e;
    }

    public boolean addSong(String song, File songfile) {
        // System.out.println("addSong");
        boolean e;
        try {
            e = plContent.add(AudioVectorMetaData.createMetaData(song, songfile));
        } catch (FileNotFoundException e1) {
            e = false;
        }
        if (lm != null) {
            lm.fireContentsChanged(this, 0, plContent.size());
        }
        informPlayListListeners();
        return e;
    }

    public boolean addSong(String song) {
        String path = CommonSOMViewerStateData.fileNamePrefix + song + CommonSOMViewerStateData.fileNameSuffix;
        return addSong(song, new File(path));
    }

    public void addSong(String song, File songfile, int position) {
        try {
            plContent.add(position, AudioVectorMetaData.createMetaData(songfile));
            if (lm != null) {
                lm.fireContentsChanged(this, 0, plContent.size());
            }
            informPlayListListeners();
        } catch (FileNotFoundException e) {
        }
    }

    public void addSong(String song, int position) {
        String path = CommonSOMViewerStateData.fileNamePrefix + song + CommonSOMViewerStateData.fileNameSuffix;
        addSong(song, new File(path), position);
    }

    public List<AudioVectorMetaData> getPlayListItems() {
        return plContent;
    }

    public List<File> getSongs() {
        List<File> fList = new ArrayList<File>();
        for (AudioVectorMetaData md : plContent) {
            fList.add(md.getAudioFile());
        }
        return fList;
    }

    public AudioVectorMetaData getPlayListItem(int index) {
        if (plContent.size() == 0) {
            return null;
        }
        return plContent.get(index);
    }

    public List<String> getDataItems() {
        List<String> sList = new ArrayList<String>();
        for (AudioVectorMetaData md : plContent) {
            sList.add(md.getID());
        }
        return sList;
    }

    public int getIndexOf(final String id) {
        for (int i = 0; i < plContent.size(); i++) {
            if (id.equals(plContent.get(i).getID())) {
                return i;
            }
        }
        return 0;
    }

    public AudioVectorMetaData removeSong(int index) {
        AudioVectorMetaData f = plContent.remove(index);
        if (lm != null) {
            lm.fireContentsChanged(this, 0, plContent.size());
        }
        informPlayListListeners();
        return f;

    }

    public List<AudioVectorMetaData> remove(int from, int to) {
        List<AudioVectorMetaData> sl = plContent.subList(from, to);
        plContent.removeAll(sl);
        if (lm != null) {
            lm.fireContentsChanged(this, 0, plContent.size());
        }
        informPlayListListeners();
        return sl;
    }

    public void clearPlaylist() {
        stop();
        currentSong = 0;
        plContent.clear();
        if (lm != null) {
            lm.fireContentsChanged(this, 0, plContent.size());
        }
        informPlayListListeners();
    }

    public void next() {
        skip(1);
    }

    public void prev() {
        skip(-1);
    }

    @SuppressWarnings("unused")
    private boolean moreToPlay() {
        return currentSong > plContent.size() - 2;
    }

    /**
     * @param count How many songs to skip
     * @return <c>true</c> if there was a song to skip to, <c>false</c> otherwise.
     */
    public boolean skip(int count) {
        boolean result = false;
        currentSong += count;
        if (currentSong < 0 || currentSong > plContent.size() - 1) {
            currentSong = Math.min(Math.max(currentSong, 0), plContent.size() - 1);
            isPlaying = false;
            player.stop();
        } else {
            if (isPlaying) {
                player.play(plContent.get(currentSong));
                result = true;
            } else {
                player.stop();
            }
        }
        if (lm != null) {
            lm.fireContentsChanged(this, 0, plContent.size());
        }

        return result;
    }

    @Override
    public void playStarted(int mode, AudioVectorMetaData song) {
        // System.out.println("Playing " + title);
        for (PlayerListener pl : listeners) {
            pl.playStarted(mode, song);
        }
    }

    @Override
    public void playStopped(int reason, AudioVectorMetaData song) {
        // System.out.println("Finished " + title);
        boolean sendNotification = false;
        if (reason == PlayerListener.STOP_REASON_ENDED) {
            // If song ended and there is more in the PL, we do not send notifications
            sendNotification = !skip(1);
        } else {
            sendNotification = !isPlaying;
        }
        // System.out.printf("R: %d, T: %s, isPlaying:%b, sentNotify:%b%n", reason, title, isPlaying, sendNotification);
        if (sendNotification) {
            for (PlayerListener pl : listeners) {
                pl.playStopped(reason, song);
            }
        }

    }

    public void play(int selectedIndex) {
        currentSong = Math.min(Math.max(selectedIndex, 0), plContent.size() - 1);
        if (lm != null) {
            lm.fireContentsChanged(this, 0, plContent.size());
        }
        play();
    }

    public void play() {
        if (currentSong < 0 || currentSong >= plContent.size()) {
            return;
        }
        isPlaying = true;
        player.play(plContent.get(currentSong));
    }

    public void stop() {
        isPlaying = false;
        player.stop();
    }

    public void addPlayerListener(PlayerListener l) {
        listeners.add(l);
    }

    public void removePlayerListener(PlayerListener l) {
        listeners.remove(l);
    }

    public void addPlayListListener(PlayListListener pll) {
        plListeners.add(pll);
    }

    public void removePlayListListener(PlayListListener pll) {
        plListeners.remove(pll);
    }

    private void informPlayListListeners() {
        if (pllNP == null) {
            pllNP = new NotificationPostponer();
        }

        if (pllNP.isAlive()) {
            pllNP.postpone();
        } else {
            pllNP = new NotificationPostponer();
            pllNP.start();
        }
    }

    public int getCurrentSongIndex() {
        return currentSong;
    }

    public JList createMatchingJList(boolean addDefaultListeners) {
        if (lm == null) {
            lm = new PlayListModel();
        }
        final JList list = new JList();
        list.setModel(lm);
        list.setCellRenderer(new PlayListCellRenderer());

        if (addDefaultListeners) {
            list.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == '\n') {
                        PlayList.this.play(list.getSelectedIndex());
                    }
                }
            });
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        PlayList.this.play(list.getSelectedIndex());
                    }
                }
            });
        }

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(7);

        return list;
    }

    private final class NotificationPostponer extends Thread {
        private boolean doPostpone = true;

        private static final int WAIT = 500; // 0.5s

        @Override
        public void run() {
            setName(this.getClass().getSimpleName());
            while (doPostpone) {
                doPostpone = false;
                try {
                    Thread.sleep(WAIT);
                } catch (InterruptedException e) {
                }
            }
            // System.out.println("--> Notify");
            for (PlayListListener pll : plListeners) {
                pll.playListContentChanged();
            }
        }

        public void postpone() {
            doPostpone = true;
        }
    }

    private class PlayListModel extends AbstractListModel {
        private static final long serialVersionUID = 1L;

        @Override
        public Object getElementAt(int index) {
            return plContent.get(index);
        }

        @Override
        public int getSize() {
            return plContent.size();
        }

        @Override
        protected void fireContentsChanged(Object source, int index0, int index1) {
            super.fireContentsChanged(source, index0, index1);
        }

        @Override
        protected void fireIntervalAdded(Object source, int index0, int index1) {
            super.fireIntervalAdded(source, index0, index1);
        }

        @Override
        protected void fireIntervalRemoved(Object source, int index0, int index1) {
            super.fireIntervalRemoved(source, index0, index1);
        }
    }

    private class PlayListCellRenderer implements ListCellRenderer {

        private JLabel l;

        public PlayListCellRenderer() {
            l = new JLabel();
            l.setOpaque(true);
            new JLabel();
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            AudioVectorMetaData f = (AudioVectorMetaData) value;
            // System.err.println(index + " is " +
            // value.getClass().getCanonicalName());

            // isSelected
            if (isSelected) {
                l.setBackground(list.getSelectionBackground());
                l.setForeground(list.getSelectionForeground());
            } else {
                l.setBackground(list.getBackground());
                l.setForeground(list.getForeground());
            }

            // isPlaying
            if (PlayList.this.getPlayListItem(PlayList.this.currentSong).getID().equals(f.getID())) {
                l.setFont(l.getFont().deriveFont(Font.BOLD));
            } else {
                l.setFont(l.getFont().deriveFont(Font.PLAIN));
            }

            if (showFilenames) {
                l.setText(f.getAudioFile().getName());
            } else {
                l.setText(f.getDisplayLabel());
            }
            return l;
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setCurrentSong(int index) {
        currentSong = Math.min(Math.max(index, 0), plContent.size() - 1);
        if (lm != null) {
            lm.fireContentsChanged(this, 0, plContent.size());
        }
    }

}
