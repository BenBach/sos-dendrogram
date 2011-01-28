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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;

/**
 * @author Ewald Peiszer
 * @version $Id: ControlFrame.java 3873 2010-10-28 09:29:58Z frank $
 */
public class ControlFrame extends JFrame implements MouseListener {
    private static final long serialVersionUID = 1L;

    boolean paused = false;

    /** Contains PlaybackThreads; the key is the index of the line as a String */
    LinkedHashMap<String, PlaybackThread> lhmThreads;

    TimeUpdateThread tuThread = new TimeUpdateThread();

    /** Reference to the Start button on the Panel. Used to en- and disable it */
    JButton btn_start_on_panel;

    ImageIcon ii_pause;

    ImageIcon ii_play;

    ImageIcon ii_stop;

    ActivityGridModel mod;

    JLabel lb_songs1 = new JLabel();

    JLabel lb_songs2 = new JLabel();

    JButton btn_pause = new JButton();

    JButton btn_stop = new JButton();

    JLabel lb_Playtime1 = new JLabel();

    JLabel lb_Playtime2 = new JLabel();

    GridBagLayout gridBagLayout1 = new GridBagLayout();

    JScrollPane jScrollPane1 = new JScrollPane();

    ActivityGrid gr_activity;

    /** Constructor does not yet start the threads */
    public ControlFrame(LinkedHashMap<String, PlaybackThread> lhm, ActivityGridModel mod, JButton btn_start_on_panel) {
        this.lhmThreads = lhm;
        this.mod = mod;
        this.btn_start_on_panel = btn_start_on_panel;
        ii_pause = new ImageIcon(ClassLoader.getSystemResource(SOMViewer.RESOURCE_PATH_ICONS + "pause.png"));
        ii_play = new ImageIcon(ClassLoader.getSystemResource(SOMViewer.RESOURCE_PATH_ICONS + "play.png"));
        ii_stop = new ImageIcon(ClassLoader.getSystemResource(SOMViewer.RESOURCE_PATH_ICONS + "stop.png"));

        try {
            gr_activity = new ActivityGrid(mod);
            jbInit();

            // my init
            tuThread.start();
            this.pack();

            // gr_activity.addMouseListener(this);
            // could be used to pause specific threads

            // calculate preferred size
            // size of other components (buttons, labels; not grid)
            int iAdditionalX = 0;
            int iAdditionalY = 110;
            // size of dialog insets (since we cannot set the size of the contentpane
            // directly in 1.4 (only 1.5 and greater))
            Insets insDlg = this.getInsets();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            // Size is the minimum of
            // the size the grid + other components + insets
            // AND
            // the screensize (minus something to make it look a bit prettier)
            int iResultX = Math.min(gr_activity.getSize().width + iAdditionalX + insDlg.left + insDlg.right,
                    screenSize.width);
            int iResultY = Math.min(gr_activity.getSize().height + iAdditionalY + insDlg.top + insDlg.bottom,
                    screenSize.height - 50);
            // System.out.println(iResultX + " " + iResultY);
            // this.getContentPane().setPreferredSize(new Dimension(iResultX, iResultY)); (if javac 1.5)
            this.setSize(new Dimension(iResultX, iResultY));
            // this.pack();
            Commons.centerWindow(this);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        getContentPane().setLayout(gridBagLayout1);
        btn_pause.setMaximumSize(new Dimension(140, 87));
        btn_pause.setMinimumSize(new Dimension(140, 87));
        btn_pause.setPreferredSize(new Dimension(140, 87));
        btn_pause.setToolTipText("Pauses / resumes playback");
        btn_pause.setText("Pause");
        btn_pause.addActionListener(new ControlFrame_btn_pause_actionAdapter(this));
        btn_pause.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn_pause.setHorizontalTextPosition(SwingConstants.CENTER);
        btn_pause.setIcon(ii_pause);
        btn_stop.setMaximumSize(new Dimension(140, 87));
        btn_stop.setMinimumSize(new Dimension(140, 87));
        btn_stop.setPreferredSize(new Dimension(140, 87));
        btn_stop.setToolTipText("Stops playback and exit");
        btn_stop.setIcon(null);
        btn_stop.setText("Stop and exit");
        btn_stop.addActionListener(new ControlFrame_btn_stop_actionAdapter(this));
        btn_stop.setIcon(ii_stop);
        btn_stop.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn_stop.setHorizontalTextPosition(SwingConstants.CENTER);
        lb_songs2.setText("0");
        // this.getContentPane().setMinimumSize(new Dimension(330, 200));
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setTitle(Commons.APP_NAME + " - Control Panel");
        lb_Playtime1.setText("Total playback time:");
        lb_Playtime2.setText("0");
        jScrollPane1.setBorder(null);
        jScrollPane1.setMinimumSize(new Dimension(100, 100));
        jScrollPane1.setPreferredSize(new Dimension(454, 404));
        this.getContentPane().add(
                btn_pause,
                new GridBagConstraints(0, 1, 1, 2, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                        new Insets(0, 10, 10, 10), 0, 0));
        this.getContentPane().add(
                btn_stop,
                new GridBagConstraints(1, 1, 1, 2, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                        new Insets(0, 10, 10, 10), 0, 0));
        this.getContentPane().add(
                lb_Playtime1,
                new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 10), 0, 0));
        this.getContentPane().add(
                lb_songs1,
                new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.getContentPane().add(
                lb_Playtime2,
                new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.getContentPane().add(
                lb_songs2,
                new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.getContentPane().add(
                jScrollPane1,
                new GridBagConstraints(0, 0, 4, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 10, 0), 0, 0));
        jScrollPane1.getViewport().add(gr_activity);

        lb_songs1.setText("# songs played:");
    }

    public void btn_pause_actionPerformed(ActionEvent actionEvent) {
        if (paused) {
            // resume playback
            Iterator<Entry<String, PlaybackThread>> iter = lhmThreads.entrySet().iterator();
            while (iter.hasNext()) {
                iter.next().getValue().resume_playback();
            }
            tuThread.end_pause();
            btn_pause.setText("Pause");
            btn_pause.setIcon(ii_pause);
        } else {
            // pause
            Iterator<Entry<String, PlaybackThread>> iter = lhmThreads.entrySet().iterator();
            while (iter.hasNext()) {
                iter.next().getValue().pause_playback();
            }
            tuThread.start_pause();
            btn_pause.setText("Resume");
            btn_pause.setIcon(ii_play);
        }
        paused = !paused;
        this.repaint();
    }

    public void btn_stop_actionPerformed(ActionEvent actionEvent) {
        // exit
        this.setVisible(false);
    }

    public void updateStats() {
        lb_songs2.setText("" + Commons.iSongscount);
        lb_songs2.repaint();
    }

    public void updateTime(long lPausesDuration) {
        // get difference to current time
        long currentTime = System.currentTimeMillis();
        lb_Playtime2.setText(""
                + Commons.sdfHHmmss.format(new Date(currentTime - Commons.lStarttime - lPausesDuration)));
        lb_Playtime2.repaint();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(530, 260);
    }

    @Override
    public void setVisible(boolean parm1) {
        if (parm1) {
            // on show

            // Disable Start-button on panel
            btn_start_on_panel.setEnabled(false);

            // Starting time
            Commons.lStarttime = System.currentTimeMillis();
            ;

            // Start threads
            Iterator<Entry<String, PlaybackThread>> iter = lhmThreads.entrySet().iterator();
            while (iter.hasNext()) {
                iter.next().getValue().start();

            }
        } else {
            // on close
            // stop timer thread
            tuThread.stop_it();
            // Stop threads
            Iterator<Entry<String, PlaybackThread>> iter = lhmThreads.entrySet().iterator();
            while (iter.hasNext()) {
                iter.next().getValue().stop_playback();
            }
            lhmThreads.clear();

            // Ensable Start-button on panel
            btn_start_on_panel.setEnabled(true);
        }
        super.setVisible(parm1);
    }

    // NOTE: Currently, the mouse.... methods are not used and never called
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) { // Double-click
            // Determine cell at mouse cursor
            // Pause respective thread
            // OR
            // Pause all other threads
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        /** TODO Implement this java.awt.event.MouseListener method */
        throw new java.lang.UnsupportedOperationException("Method mousePressed() not yet implemented.");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        /** TODO Implement this java.awt.event.MouseListener method */
        throw new java.lang.UnsupportedOperationException("Method mouseReleased() not yet implemented.");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        /** TODO Implement this java.awt.event.MouseListener method */
        throw new java.lang.UnsupportedOperationException("Method mouseEntered() not yet implemented.");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        /** TODO Implement this java.awt.event.MouseListener method */
        throw new java.lang.UnsupportedOperationException("Method mouseExited() not yet implemented.");
    }
}

class ControlFrame_btn_stop_actionAdapter implements ActionListener {
    private ControlFrame adaptee;

    ControlFrame_btn_stop_actionAdapter(ControlFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        adaptee.btn_stop_actionPerformed(actionEvent);
    }
}

class ControlFrame_btn_pause_actionAdapter implements ActionListener {
    private ControlFrame adaptee;

    ControlFrame_btn_pause_actionAdapter(ControlFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        adaptee.btn_pause_actionPerformed(actionEvent);
    }
}
