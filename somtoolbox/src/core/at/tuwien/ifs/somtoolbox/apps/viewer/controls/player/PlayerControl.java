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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.UiUtils;

public class PlayerControl extends JPanel implements PlayerListener {
    private static final long serialVersionUID = 1L;

    private JButton play, next, prev;

    private JLabel status;

    private static final String ACT_PLAY = "PLAY";

    private static final String ACT_STOP = "STOP";

    private static final String ACT_NEXT = "FASTFORWARD";

    private static final String ACT_PREV = "REWIND";

    public static final String ICON_PREFIX = "rsc/icons/control_";

    public static final String ICON_SUFFIX = "_blue.png";

    private ImageIcon playIcon = null, stopIcon = null, nextIcon = null, prevIcon = null;

    private PlayList playlist;

    public PlayerControl(PlayList playlist) {
        this.playlist = playlist;
        initialize();
    }

    private void adaptPlayButton(String targetAction) {
        Icon i = null;
        String caption = "";

        if (ACT_PLAY.equals(targetAction)) {
            i = playIcon;
            caption = ACT_PLAY;
        } else {
            i = stopIcon;
            caption = ACT_STOP;
        }

        play.setActionCommand(targetAction);
        play.setIcon(i);
        if (i == null) {
            play.setText(caption);
        } else {
            play.setText("");
        }
    }

    private void initialize() {
        playIcon = UiUtils.getIcon(ICON_PREFIX, ACT_PLAY.toLowerCase() + ICON_SUFFIX);
        stopIcon = UiUtils.getIcon(ICON_PREFIX, ACT_STOP.toLowerCase() + ICON_SUFFIX);
        nextIcon = UiUtils.getIcon(ICON_PREFIX, ACT_NEXT.toLowerCase() + ICON_SUFFIX);
        prevIcon = UiUtils.getIcon(ICON_PREFIX, ACT_PREV.toLowerCase() + ICON_SUFFIX);

        setLayout(new GridBagLayout());
        GridBagConstraintsIFS gc = new GridBagConstraintsIFS(GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL).setInsets(3, 1);

        prev = new JButton();
        prev.setActionCommand(ACT_PREV);
        if (prevIcon == null) {
            prev.setText(ACT_PREV);
        } else {
            prev.setBorder(null);
            prev.setIcon(prevIcon);
        }
        prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playlist.prev();
            }
        });
        add(prev, gc);

        play = new JButton();
        play.setBorder(null);
        play.setActionCommand(ACT_PLAY);
        adaptPlayButton(ACT_PLAY);
        play.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals(ACT_PLAY)) {
                    playlist.play();
                    // play.setActionCommand(ACT_STOP);
                    // adaptPlayButton(ACT_STOP);
                } else {
                    playlist.stop();
                    // play.setActionCommand(ACT_PLAY);
                    // adaptPlayButton(ACT_PLAY);
                }
            }
        });
        add(play, gc.nextCol());

        next = new JButton();
        add(next, gc.nextCol());
        next.setActionCommand(ACT_NEXT);
        next.setIcon(nextIcon);
        if (nextIcon == null) {
            next.setText(ACT_NEXT);
        } else {
            next.setBorder(null);
        }

        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playlist.next();
            }
        });

        add(status = new JLabel("Stopped"), gc.nextCol().setAnchor(GridBagConstraints.NORTHWEST).fillWidth());
        // status.q

        playlist.addPlayerListener(this);
    }

    @Override
    public void playStarted(int mode, AudioVectorMetaData song) {
        status.setText(song.getDisplayLabel());
        adaptPlayButton(ACT_STOP);
    }

    @Override
    public void playStopped(int reason, AudioVectorMetaData song) {
        switch (reason) {
            case PlayerListener.STOP_REASON_ENDED:
                status.setText("Finished");
                break;
            case PlayerListener.STOP_REASON_STOPPED:
                status.setText("Stopped");
                break;
        }
        adaptPlayButton(ACT_PLAY);
    }

}
