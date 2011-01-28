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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback.ActivityGridModel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback.Commons;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback.ControlFrame;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback.FindMeLoopThread;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback.LayoutTable;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback.LineListModel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback.PlaybackThread;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback.TPlaybackThreadDataRecord;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * @author Ewald Peiszer
 * @version $Id: MultichannelPlaybackPanel.java 3877 2010-11-02 15:43:17Z frank $
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MultichannelPlaybackPanel extends AbstractSelectionPanel implements ActionListener, ListSelectionListener {
    private static final long serialVersionUID = 1L;

    // JB-Stuff
    BorderLayout borderLayout1 = new BorderLayout();

    JTabbedPane jTabbedPane1 = new JTabbedPane();

    JPanel jPanel1 = new JPanel();

    // JPanel jPanel2 = new JPanel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();

    GridBagLayout gridBagLayout2 = new GridBagLayout();

    JList li_lines = new JList();

    JScrollPane jScrollPane1 = new JScrollPane();

    JToggleButton btn_findme_loop = new JToggleButton();

    JButton btn_findme = new JButton();

    JLabel jLabel3 = new JLabel();

    JPanel pnl_layout_table_gen = new JPanel();

    JTextField txt_table_x = new JTextField();

    JLabel jLabel4 = new JLabel();

    JTextField txt_table_y = new JTextField();

    JLabel jLabel5 = new JLabel();

    JButton btn_gen = new JButton();

    JScrollPane jScrollPane2 = new JScrollPane();

    JButton btn_autoassign = new JButton();

    JButton btn_start = new JButton();

    JLabel jLabel6 = new JLabel();

    JScrollPane jScrollPane3 = new JScrollPane();

    JTextPane tp_help = new JTextPane();

    JButton btn_load = new JButton();

    JButton btn_save = new JButton();

    // dummyInit stuff (used if Java version insufficient
    JTextPane tpHint = new JTextPane();

    // my stuff
    LayoutTable tb_layout = null;

    DefaultTableModel tbmod_layout = null;

    /**
     * Stores the assignments for each cell of the layout table.
     * <p>
     * The vector contains <code>GeneralUnitPNode</code>s
     */
    Vector[][] aavGPNs = null;

    boolean bAssignmentChanged = false;

    protected SOMPane somPane;

    protected MapPNode map;

    protected GrowingSOM gsom;

    protected FindMeLoopThread fmlt;

    /**
     * If true, changes of the selection in the SOMPane will be ignored.
     * <p>
     * Used if selection is programmatically removed (because anotehr cell in layout table is being selected; then the
     * assigment should not be changed.
     */
    private boolean bIgnoreNextSelectionChange = false;

    /** Is created and filled when parsing layout table */
    protected TPlaybackThreadDataRecord[] aPtdata;

    protected LinkedHashMap<String, PlaybackThread> lhmThreads = new LinkedHashMap<String, PlaybackThread>();

    protected ActivityGridModel actmod;

    // public MultichannelMp3Panel() {
    //
    // }

    public MultichannelPlaybackPanel(CommonSOMViewerStateData state, SOMPane somPane) {
        super(new GridBagLayout(), state, Commons.APP_NAME);
        this.somPane = somPane;
        this.map = somPane.getMap();
        this.gsom = map.getGsom();
        try {
            String vers = System.getProperty("java.version");
            // Test, ob Java 1.5 oder 1.6 verwendet wird
            // (ich bin zu faul, um jetzt ein allgemiene Abfrage > 1.4 zu schreiben)
            if (!vers.startsWith("1.5") && !vers.startsWith("1.6")) {
                Commons.log.info("You are using Java version " + vers + "...");
                Commons.log.warning("... not sufficient. You need at least 1.5 (5.0)");
                JOptionPane.showMessageDialog(null, Commons.APP_NAME + " needs at least Java Version 1.5 (5.0)",
                        "Wrong Java version", JOptionPane.ERROR_MESSAGE);
                dummyInit();
            } else {
                jbInit();
                myInit();
            }
            setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dummyInit() throws Exception {
        tpHint.setContentType("text/html");
        tpHint.setText("<p style='color:#aa0000; font-family:Arial'><b>"
                + Commons.APP_NAME
                + "</b> needs at least Java Version 1.5 (5.0). "
                + "<br><br>If you would like to use this panel please start SOMViewer again using a more recent JVM.</p>");
        tpHint.setEditable(false);
        this.getContentPane().setLayout(borderLayout1);
        this.getContentPane().add(tpHint, BorderLayout.CENTER);
    }

    private void jbInit() throws Exception {
        btn_start.setEnabled(false);
        btn_start.setMaximumSize(new Dimension(123, 27));
        btn_start.setMinimumSize(new Dimension(123, 27));
        btn_start.setPreferredSize(new Dimension(123, 60));
        btn_start.setToolTipText("Open Control Dialog and start playback");
        this.getContentPane().setLayout(borderLayout1);
        jPanel1.setLayout(gridBagLayout1);
        // jPanel2.setLayout(gridBagLayout2);
        btn_findme_loop.setMaximumSize(new Dimension(2147483647, 45));
        btn_findme_loop.setMinimumSize(new Dimension(110, 27));
        btn_findme_loop.setPreferredSize(new Dimension(85, 27));
        btn_findme_loop.setToolTipText("Plays a short soundclip to each of your output lines; one after another, "
                + "until you press the button a second time.");
        btn_findme_loop.setActionCommand("findmeloop");
        btn_findme_loop.setMargin(new Insets(2, 2, 2, 2));
        btn_findme_loop.setMnemonic('L');
        btn_findme_loop.setText("Find me (loop)");
        btn_findme.setText("Find me");
        btn_findme.setMaximumSize(new Dimension(2147483647, 45));
        btn_findme.setMinimumSize(new Dimension(110, 27));
        btn_findme.setPreferredSize(new Dimension(85, 27));
        btn_findme.setToolTipText("Plays a short soundclip to the selected output line.");
        btn_findme.setActionCommand("findme");
        btn_findme.setMargin(new Insets(2, 2, 2, 2));
        btn_findme.setMnemonic('F');
        jLabel3.setLabelFor(jScrollPane2);
        jLabel3.setText("Layout table:");
        pnl_layout_table_gen.setMinimumSize(new Dimension(220, 30));
        pnl_layout_table_gen.setPreferredSize(new Dimension(220, 30));
        txt_table_x.setMinimumSize(new Dimension(22, 24));
        txt_table_x.setPreferredSize(new Dimension(22, 24));
        txt_table_x.setToolTipText("Number of columns for layout table");
        txt_table_x.setText("55");
        jLabel4.setLabelFor(txt_table_x);
        jLabel4.setText("columns, ");
        txt_table_y.setMinimumSize(new Dimension(22, 24));
        txt_table_y.setPreferredSize(new Dimension(22, 24));
        txt_table_y.setToolTipText("Number of rows for layout table");
        txt_table_y.setText("5");
        jLabel5.setLabelFor(txt_table_y);
        jLabel5.setText("rows");
        btn_gen.setMaximumSize(new Dimension(50, 24));
        btn_gen.setMinimumSize(new Dimension(50, 24));
        btn_gen.setPreferredSize(new Dimension(50, 24));
        btn_gen.setToolTipText("<html>Generate new layout table<br><b>Note: the current assignment "
                + "will be lost!</b></html>");
        btn_gen.setMargin(new Insets(2, 2, 2, 2));
        btn_gen.setMnemonic('G');
        btn_gen.setText("Gen!");
        jScrollPane1.setBorder(null);
        jScrollPane1.setDebugGraphicsOptions(0);
        jScrollPane1.setMinimumSize(new Dimension(200, 100));
        jScrollPane1.setPreferredSize(new Dimension(220, 200));
        li_lines.setToolTipText("These are the sound output lines that are currently available on " + "your system.");
        li_lines.setMinimumSize(new Dimension(0, 0));
        jScrollPane2.setBorder(null);
        jScrollPane2.setMinimumSize(new Dimension(200, 100));
        jScrollPane2.setToolTipText("<html>One ore more units of the SOM should be assigned to each cell "
                + "of this layout table<p>Use Ctrl-Left-mouse-button to deselect a selected "
                + "table cell.</p></html>");
        btn_autoassign.setEnabled(false);
        btn_autoassign.setToolTipText("<html>Tries to find a optimal assignment regarding the current SOM.<p>\u000B"
                + "Note: the current assignment will be lost!</p></html>");
        btn_autoassign.setActionCommand("autoassign");
        btn_autoassign.setMnemonic('C');
        btn_autoassign.setText("Automatic assignment");
        btn_start.setActionCommand("start");
        btn_start.setMnemonic('P');
        btn_start.setText("Start Playback!");
        jLabel6.setLabelFor(li_lines);
        jLabel6.setText("Available output lines:");
        jLabel6.setDisplayedMnemonic('O');
        jPanel1.setBorder(null);
        jPanel1.setMinimumSize(new Dimension(220, 398));
        jPanel1.setToolTipText("Control layout of speakers and assignment to SOM units");
        jTabbedPane1.setBorder(null);
        jTabbedPane1.setMinimumSize(new Dimension(220, 427));
        tp_help.setEditable(false);
        tp_help.setText("<html><h1>todo</h1></html>");
        tp_help.setContentType("text/html");
        // jPanel2.setToolTipText("Set preferences");
        jScrollPane3.setToolTipText("Instructions how to use this control panel / About");
        btn_load.setMaximumSize(new Dimension(50, 24));
        btn_load.setMinimumSize(new Dimension(50, 24));
        btn_load.setPreferredSize(new Dimension(50, 24));
        btn_load.setMargin(new Insets(2, 2, 2, 2));
        btn_load.setText("Load");
        btn_save.setMaximumSize(new Dimension(50, 24));
        btn_save.setMinimumSize(new Dimension(50, 24));
        btn_save.setPreferredSize(new Dimension(50, 24));
        btn_save.setMargin(new Insets(2, 2, 2, 2));
        btn_save.setText("Save");
        /*
         * jPanel2.add(jTextField1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0 ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0,
         * 0, 0), 56, 0)); jPanel2.add(jLabel1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0 ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
         * new Insets(0, 0, 0, 1), 0, 0)); jPanel2.add(jLabel2, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0 ,GridBagConstraints.WEST,
         * GridBagConstraints.NONE, new Insets(30, 0, 0, 0), 0, 0)); jPanel2.add(jButton1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
         * ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), -27, 0)); jPanel2.add(jTextField2, new GridBagConstraints(0,
         * 3, 1, 1, 0.0, 0.0 ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, -2, 0, 2), 57, 0));
         */
        this.getContentPane().add(jTabbedPane1, BorderLayout.CENTER);
        jTabbedPane1.add(jPanel1, "Assignment");
        jScrollPane1.getViewport().add(li_lines, null);
        pnl_layout_table_gen.add(txt_table_x, null);
        pnl_layout_table_gen.add(jLabel4, null);
        pnl_layout_table_gen.add(txt_table_y, null);
        pnl_layout_table_gen.add(jLabel5, null);
        pnl_layout_table_gen.add(btn_gen, null);
        jScrollPane3.getViewport().add(tp_help);
        // jTabbedPane1.add(jPanel2, "Prefs");
        jTabbedPane1.add(jScrollPane3, "?");
        jPanel1.add(jLabel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(25, 0, 0, 0), 0, 0));
        jPanel1.add(jLabel6, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(jScrollPane2, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.3, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(btn_findme_loop, new GridBagConstraints(0, 2, 1, 1, 0.5, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(btn_findme, new GridBagConstraints(1, 2, 2, 1, 0.5, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(pnl_layout_table_gen, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(btn_autoassign, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(btn_start, new GridBagConstraints(0, 7, 3, 4, 0.0, 0.2, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(25, 0, 0, 0), 0, 0));
        jPanel1.add(jScrollPane1, new GridBagConstraints(0, 1, 3, 1, 1.0, 0.5, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0)); /*
                                                                          * jTabbedPane1.setMnemonicAt(0, 'a'); jTabbedPane1.setMnemonicAt(1, 'p'); jTabbedPane1.setMnemonicAt(2, '?');
                                                                          * jTabbedPane1.setToolTipTextAt(0, jPanel1.getToolTipText()); jTabbedPane1.setToolTipTextAt(1,
                                                                          * jPanel2.getToolTipText()); jTabbedPane1.setToolTipTextAt(2, jScrollPane3.getToolTipText());
                                                                          */
        jPanel1.add(btn_load, new GridBagConstraints(1, 3, 1, 1, 0.5, 0.0, GridBagConstraints.SOUTHEAST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(btn_save, new GridBagConstraints(2, 3, 1, 1, 0.5, 0.0, GridBagConstraints.SOUTHEAST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        jTabbedPane1.setMnemonicAt(0, 'a');
        jTabbedPane1.setMnemonicAt(1, '?');
        jTabbedPane1.setToolTipTextAt(0, jPanel1.getToolTipText());
        jTabbedPane1.setToolTipTextAt(1, jScrollPane3.getToolTipText());

    }

    /**
     * "handwritten" Swing related init-stuff, in order not to interfere with JBuilder's automatically generated code
     */
    private void myInit() {
        li_lines.setModel(new LineListModel());
        btn_findme.addActionListener(this);
        btn_findme_loop.addActionListener(this);
        btn_gen.addActionListener(this);
        btn_autoassign.addActionListener(this);
        btn_start.addActionListener(this);
        btn_save.addActionListener(this);
        btn_load.addActionListener(this);
        // btn_save.setEnabled(false);
        btn_load.setToolTipText("Loads assignment from file. The dimensions of the current SOM and the SOM that was loaded when you saved the assignment must match.");
        btn_save.setToolTipText("Saves current assignment (SOM units -> layout table cell) as a file.");
        // pre fill in textfields for layout table
        int iGuess = (int) Math.ceil(Math.sqrt(li_lines.getModel().getSize() * 2));
        txt_table_x.setText("" + iGuess);
        txt_table_y.setText("" + iGuess);
        try {
            tp_help.setPage(ClassLoader.getSystemResource(Commons.INSTRUCTIONS_HTMLFILENAME));
        } catch (IOException ex) {
            Commons.log.warning("Cannot display helppage.");
            // ex.printStackTrace();
            tp_help.setText("<html><h2>Warning:</h2><p>Cannot display helppage.</p></html>");
        }
    }

    /** Let the user confirm that the current assignment will be lost if he/she continues */
    boolean confirmNewAssignment() {
        return !bAssignmentChanged
                || JOptionPane.showConfirmDialog(this, "The current assignment will be lost.\n\nContinue?",
                        Commons.APP_NAME, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION;

    }

    /**
     * Let the user confirm that the current assignment AND SPEAKER LAYOUT will be lost if he/she continues
     */
    boolean confirmNewAssignmentAndSpeakerLayout() {
        return !bAssignmentChanged
                || JOptionPane.showConfirmDialog(this,
                        "Both the current speaker layout and assignment will be lost.\n\nContinue?", Commons.APP_NAME,
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION;

    }

    /**
     * Adds the data directory (or "fileprefix") to each music file in the given array.
     * <p>
     * Both <code>asInputs</code> and <code>sDatadir</code> may be null.
     * 
     * @return array of string of the same size as the input array or a zero-sized array if <code>asInputs</code> is
     *         null
     */
    protected String[] createFileArray(String[] asInputs, String sDatadir) {
        if (sDatadir == null) {
            // no change
            return asInputs;
        }
        if (asInputs == null) {
            // return empty array
            return new String[0];
        }
        String[] as = new String[asInputs.length];
        for (int i = 0; i < asInputs.length; i++) {
            as[i] = Commons.makeSureThatLastCharIsACorrectFileSeparator(sDatadir) + asInputs[i];
        }
        return as;
    }

    /**
     * <p>
     * Parses the data in layout table and constructs the array used to create the <code>PlaybackThread</code>s
     * <p>
     * Checks if the data is valid.
     * <p>
     * If not valid, asks the user for confirmation.
     * 
     * @return true if data is valid OR user has confirmed in case of invalidity
     */
    boolean isValidOrConfirmed() {
        // Create new, empty data record array
        aPtdata = new TPlaybackThreadDataRecord[LineListModel.getMixerCount()];

        TableModel mod = tb_layout.getModel();

        // New table model
        actmod = new ActivityGridModel(mod);

        String field;
        char ch;
        int iChannel;
        int iLine = -1;
        GeneralUnitPNode pnode;
        Unit u;
        String[] asChannel = { "Left", "Right" };
        StringBuffer sbWarnings = new StringBuffer();
        int iWarningsCount = 0;

        // Go through all cells and fill in data into array
        for (int c = 0; c < mod.getColumnCount(); c++) {
            for (int r = 0; r < mod.getRowCount(); r++) {
                field = (String) mod.getValueAt(r, c);
                if (field != null && !field.equals("")) {
                    // We assume the contents is well-formed.
                    // if not, an exception is thrown
                    try {
                        ch = field.substring(field.length() - 1).toUpperCase().charAt(0);
                        // ch contains 'L' or 'R'
                        if (ch == 'L') {
                            iChannel = 0;
                        } else {
                            iChannel = 1;
                        }
                        iLine = Integer.parseInt(Commons.cutEndOfString(field, 1));
                        // Create object if null
                        if (aPtdata[iLine] == null) {
                            aPtdata[iLine] = new TPlaybackThreadDataRecord();
                        }

                        // Enter position of this channel of this line
                        if (!aPtdata[iLine].setPos(iChannel, c, r)) {
                            // position has already be entered, multiple usage of the same speaker
                            //
                            iWarningsCount++;
                            sbWarnings.append("(column " + c + " / row " + r + "): Multiple usage of speaker " + field
                                    + "\n");
                        }

                        // Build table model
                        actmod.setSpeakerAt(c, r, LineListModel.getOurMixerIdString(iLine) + ", " + asChannel[iChannel]);

                        // Enter music file list
                        if (aavGPNs[c][r] != null && aavGPNs[c][r].size() > 0) {
                            Iterator iter = aavGPNs[c][r].iterator();
                            while (iter.hasNext()) {
                                pnode = (GeneralUnitPNode) iter.next();
                                u = pnode.getUnit();
                                aPtdata[iLine].addSongs(
                                        iChannel,
                                        createFileArray(u.getMappedInputNames(),
                                                CommonSOMViewerStateData.fileNamePrefix));
                            }
                        } else {
                            // no unit assigned
                            iWarningsCount++;
                            sbWarnings.append("(column " + c + " / row " + r + "): No units assigned to speaker "
                                    + field + "\n");
                        }
                    } catch (NumberFormatException nfex) {
                        iWarningsCount++;
                        sbWarnings.append("(column "
                                + c
                                + " / row "
                                + r
                                + "): "
                                + field
                                + " is not a well formed speaker identification string.\n\tPlease refer to the manual (third tab at this control panel).\n");
                    } catch (ArrayIndexOutOfBoundsException nfex) {
                        iWarningsCount++;
                        sbWarnings.append("(column " + c + " / row " + r + "): Output line " + iLine
                                + " does not exist.\n\tRange of allowed indices: 0 to "
                                + (LineListModel.getMixerCount() - 1) + "\n");
                    }
                } else {
                    // cell empty
                    iWarningsCount++;
                    sbWarnings.append("(column " + c + " / row " + r + "): empty, no speaker\n");
                }

            }

        }

        // loop over array to find null values
        for (int i = 0; i < aPtdata.length; i++) {
            if (aPtdata[i] == null) {
                // no speaker of this line used
                iWarningsCount++;
                sbWarnings.append("(" + LineListModel.getOurMixerIdString(i) + "): not used\n");
            } else {
                if (aPtdata[i].avMusic[0] == null) {
                    // left speaker of this line not used
                    iWarningsCount++;
                    sbWarnings.append("(" + LineListModel.getOurMixerIdString(i) + "): left channel not used\n");
                }
                if (aPtdata[i].avMusic[1] == null) {
                    // right speaker of this line not used
                    iWarningsCount++;
                    sbWarnings.append("(" + LineListModel.getOurMixerIdString(i) + "): right channel not used\n");
                }
            }
        }

        final int GRENZE = 700;
        String sWarningsMax;
        if (sbWarnings.length() > GRENZE) {
            sWarningsMax = sbWarnings.substring(0, GRENZE) + "\n[...]\n";
        } else {
            sWarningsMax = sbWarnings.toString();
        }

        // returns true if there were no warnings or if the user confirms the warnings
        return iWarningsCount == 0
                || JOptionPane.showConfirmDialog(this, "" + iWarningsCount + " warning(s) occured:\n\n" + sWarningsMax
                        + "\nAre you sure you want to start playback anyway?\n\n", Commons.APP_NAME,
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    public void actionPerformed(ActionEvent parm1) {
        if (parm1.getSource() == btn_findme) {
            // Find me (once)
            // Nur wenn etwas in der Liste markiert ist
            int[] aInt = li_lines.getSelectedIndices();
            if (aInt.length > 0) {
                Commons.playSound("airhorn_l.mp3", "airhorn_r.mp3", aInt[0]);
            }
        }
        if (parm1.getSource() == btn_findme_loop) {
            if (btn_findme_loop.isSelected()) {
                int[] aInt = li_lines.getSelectedIndices();
                if (aInt.length > 0) {
                    fmlt = new FindMeLoopThread(aInt[0]); // Start at selected index
                } else {
                    fmlt = new FindMeLoopThread(0); // start at 0
                }
                // start thread
                fmlt.start();
            } else {
                // stop thread
                fmlt.stopIt();
            }
        }
        if (parm1.getSource() == btn_gen) {
            // Generate new layout table
            if (tb_layout == null || confirmNewAssignmentAndSpeakerLayout()) {
                try {
                    int rows = Integer.parseInt(txt_table_y.getText());
                    int cols = Integer.parseInt(txt_table_x.getText());

                    aavGPNs = new Vector[cols][rows];
                    for (Vector[] element : aavGPNs) {
                        for (int j = 0; j < element.length; j++) {
                            element[j] = new Vector();
                        }
                    }

                    // table init
                    tb_layout = new LayoutTable(this, aavGPNs);

                    tbmod_layout = new DefaultTableModel(rows, cols);
                    tb_layout.setModel(tbmod_layout);
                    jScrollPane2.getViewport().add(tb_layout);
                    btn_autoassign.setEnabled(true);
                    bAssignmentChanged = false;
                    // clear marking on mapPane
                    unmarkUnits();
                    btn_start.setEnabled(false);
                    // btn_save.setEnabled(false);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "The inputs for the number of rows and columns resp are not valid.\n\nThe layout table will not be created.",
                            Commons.APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (parm1.getSource() == btn_autoassign) {
            // Start autoassignment
            if (tb_layout != null && confirmNewAssignment()) {
                // Table exists and (has not been changed OR user confirms)
                int iSourceX = tb_layout.getColumnCount();
                int iSourceY = tb_layout.getRowCount();
                int iTargetX = gsom.getLayer().getXSize();
                int iTargetY = gsom.getLayer().getYSize();
                float fRatioX = (float) iTargetX / (float) iSourceX;
                float fRatioY = (float) iTargetY / (float) iSourceY;
                int iCellX = 0, iCellY = 0, iOldCellX = 0, iOldCellY = 0;
                float fValueX = 0, fValueY = 0;
                for (int x = 0; x < iSourceX; x++, fValueY = 0, iOldCellY = 0) {
                    fValueX += fRatioX;
                    iCellX = Math.round(fValueX);
                    for (int y = 0; y < iSourceY; y++) {
                        aavGPNs[x][y].clear();
                        fValueY += fRatioY;
                        iCellY = Math.round(fValueY);
                        for (int x1 = iOldCellX; x1 < iCellX; x1++) {
                            for (int y1 = iOldCellY; y1 < iCellY; y1++) {
                                aavGPNs[x][y].add(map.getUnit(x1, y1));
                            }
                        }
                        iOldCellY = iCellY;
                    }
                    iOldCellX = iCellX;
                }
                tb_layout.repaint();
                bAssignmentChanged = true;
                tb_layout.changeSelection(0, 0, false, false);
                setToolTipsForLayoutTable();
                btn_start.setEnabled(true);
                btn_save.setEnabled(true);
            }
        }
        if (parm1.getSource() == btn_start) {
            // Start playback
            // isValidOrConfirmed() does all the parsing
            if (tb_layout != null && isValidOrConfirmed()) {
                // create Threads
                // lhmThreads = new LinkedHashMap();
                // HashMap is assumed to be empty here (we save time by not creating it
                // new every time
                PlaybackThread pt;
                for (int i = 0; i < aPtdata.length; i++) {
                    if (aPtdata[i] != null) {
                        try {
                            Mixer.Info mi = LineListModel.getOurMixerInfoAt(i);
                            SourceDataLine sdl = null;
                            sdl = (SourceDataLine) AudioSystem.getMixer(mi).getLine(Commons.datalineformat_info);
                            pt = new PlaybackThread(LineListModel.getOurMixerIdString(i), aPtdata[i], sdl);
                            lhmThreads.put("" + i, pt);
                            Commons.log.fine("Created PlaybackThread for line #" + i);
                        } catch (LineUnavailableException ex1) {
                            Commons.log.warning("Could not create PlaybackThread for line #" + i);
                            ex1.printStackTrace();
                        }
                    } else {
                        // line not used/assigned/etc
                        Commons.log.info("Did not create PlaybackThread for line #" + i);
                    }
                }

                Commons.cf = new ControlFrame(lhmThreads, actmod, btn_start);

                // MultichannelMp3_Shared.cf.setModal(true);
                Commons.cf.setVisible(true);
            }
        }
        if (parm1.getSource() == btn_save) {
            // Save assignment
            if (tb_layout != null) {
                File f = null;
                try {
                    String sKey;
                    String sValue;
                    Properties props = new Properties();
                    int rows, cols;
                    Unit u;
                    StringBuffer sb;

                    rows = tb_layout.getModel().getRowCount();
                    cols = tb_layout.getModel().getColumnCount();
                    // Dimensions of layout table: x,y
                    props.setProperty(Commons.KEY_DIM_LAYOUT_TABLE, "" + cols + Commons.SEP_IN_VALUE + rows);

                    // Dimension of SOM: x,y
                    props.setProperty(Commons.KEY_DIM_SOM, "" + gsom.getLayer().getXSize() + Commons.SEP_IN_VALUE
                            + gsom.getLayer().getYSize());

                    // Filename of SOM: filename (just for info)
                    props.setProperty(
                            Commons.KEY_FILE_SOM,
                            "(not implemented yet, is a nice-to-have, because then the user could get a hint for which SOM this assignment was intended.)");

                    // Assignments and speaker layout
                    for (int c = 0; c < aavGPNs.length; c++) {
                        for (int r = 0; r < aavGPNs[c].length; r++) {
                            sb = new StringBuffer();
                            sKey = "" + Commons.SEP_IN_KEY + c + Commons.SEP_IN_KEY + r;
                            if (aavGPNs[c][r] != null && aavGPNs[c][r].size() > 0) {
                                Iterator iter = aavGPNs[c][r].iterator();
                                while (iter.hasNext()) {
                                    u = ((GeneralUnitPNode) iter.next()).getUnit();
                                    sb.append(u.getXPos());
                                    sb.append("/");
                                    sb.append(u.getYPos());
                                    sb.append(Commons.SEP_IN_VALUE);
                                }
                            }
                            // todo: cut 1 char from end? - seems not necessary
                            props.setProperty(Commons.KEY_ASSIGNMENT + sKey, sb.toString());
                            // speaker layout
                            sValue = (String) tb_layout.getModel().getValueAt(r, c);
                            if (sValue != null) {
                                props.setProperty(Commons.KEY_SPEAKER + sKey, sValue);
                            }
                        }
                    }
                    f = Commons.getChosenFile(true, this, Commons.PROPFILE_SUFFIX, state.getFileChooser());
                    if (f != null) {
                        FileOutputStream fos = new FileOutputStream(f);
                        props.store(fos, Commons.PROPFILEHEADER);
                        fos.close();
                        Commons.log.info("Assignment file " + f + " successfully saved.");
                    }

                } catch (Exception ex2) {
                    Commons.log.warning("Could not save assignment file.");
                    ex2.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Could not save assignment file:\n" + f.getAbsolutePath()
                            + "\n(" + ex2.getMessage() + ")", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (parm1.getSource() == btn_load) {
            // Load assignment
            if (confirmNewAssignmentAndSpeakerLayout()) {
                File f = null;
                try {
                    f = Commons.getChosenFile(false, this, Commons.PROPFILE_SUFFIX, state.getFileChooser());
                    if (f != null) {
                        String sKey;
                        String sValue;
                        Properties props = new Properties();
                        int rows, cols, x, y;
                        StringTokenizer st, st2;
                        String sUnit_coo;

                        FileInputStream fis = null;
                        fis = new FileInputStream(f);
                        props = new Properties();
                        props.load(fis);
                        fis.close();

                        // Dimension of SOM: x,y
                        sValue = props.getProperty(Commons.KEY_DIM_SOM);
                        st = new StringTokenizer(sValue, "" + Commons.SEP_IN_VALUE);
                        cols = Integer.parseInt(st.nextToken());
                        rows = Integer.parseInt(st.nextToken());

                        // Check if dimensions of current SOM are the same
                        if (cols != gsom.getLayer().getXSize() || rows != gsom.getLayer().getYSize()) {
                            JOptionPane.showMessageDialog(this, "Wrong SOM dimensions.\nExpected: " + cols + "/" + rows
                                    + "\nCurrent: " + gsom.getLayer().getXSize() + "/" + gsom.getLayer().getYSize(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            throw new Exception("The dimensions of the current SOM do not match!");
                        }

                        // Dimensions of layout table: x,y
                        sValue = props.getProperty(Commons.KEY_DIM_LAYOUT_TABLE);
                        st = new StringTokenizer(sValue, "" + Commons.SEP_IN_VALUE);
                        cols = Integer.parseInt(st.nextToken());
                        rows = Integer.parseInt(st.nextToken());

                        // Create new layout table
                        aavGPNs = new Vector[cols][rows];
                        for (Vector[] element : aavGPNs) {
                            for (int j = 0; j < element.length; j++) {
                                element[j] = new Vector();
                            }
                        }
                        // table init
                        tb_layout = new LayoutTable(this, aavGPNs);
                        tbmod_layout = new DefaultTableModel(rows, cols);
                        tb_layout.setModel(tbmod_layout);
                        jScrollPane2.getViewport().add(tb_layout);

                        // Assignments ans speaker layout
                        for (int c = 0; c < cols; c++) {
                            for (int r = 0; r < rows; r++) {
                                sKey = Commons.KEY_ASSIGNMENT + Commons.SEP_IN_KEY + c + Commons.SEP_IN_KEY + r;
                                sValue = props.getProperty(sKey);
                                st = new StringTokenizer(sValue, "" + Commons.SEP_IN_VALUE);
                                while (st.hasMoreTokens()) {
                                    sUnit_coo = st.nextToken(); // something like "12/1"
                                    st2 = new StringTokenizer(sUnit_coo, "/");
                                    x = Integer.parseInt(st2.nextToken());
                                    y = Integer.parseInt(st2.nextToken());

                                    aavGPNs[c][r].add(somPane.getMap().getUnit(x, y));
                                }
                                // speaker layout
                                sKey = Commons.KEY_SPEAKER + Commons.SEP_IN_KEY + c + Commons.SEP_IN_KEY + r;
                                sValue = props.getProperty(sKey);
                                if (sValue != null) {
                                    tb_layout.getModel().setValueAt(sValue, r, c);
                                }

                            }
                        }
                        Commons.log.info("Assignment file " + f + " successfully loaded.");
                        bAssignmentChanged = true;
                        setToolTipsForLayoutTable();

                        // Buttons states
                        btn_autoassign.setEnabled(true);
                        btn_start.setEnabled(true);
                    }
                } catch (Exception ex2) {
                    Commons.log.warning("Could not load assignment file.");
                    ex2.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Could not load assignment file:\n" + f.getAbsolutePath()
                            + "\n(" + ex2.getMessage() + ")", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }

    /** called whenever the selection in the somPane has changed */
    @Override
    public void unitSelectionChanged(Object[] selection, boolean newSelection) {
        if (bIgnoreNextSelectionChange) {
            // Ignore this change
            // System.out.println("bIgnoreNextSelectionChange was true");
            return;
        }
        if (tb_layout != null && this.isVisible()) {
            bAssignmentChanged = true;
            btn_start.setEnabled(true);
            btn_save.setEnabled(true);
            unmarkUnits();

            // for single selection mode
            // int rowIndex = tb_layout.getSelectedRow();
            // int colIndex = tb_layout.getSelectedColumn();

            // Get the min and max ranges of selected cells
            int rowIndexStart = tb_layout.getSelectedRow();
            int rowIndexEnd = tb_layout.getSelectionModel().getMaxSelectionIndex();
            int colIndexStart = tb_layout.getSelectedColumn();
            int colIndexEnd = tb_layout.getColumnModel().getSelectionModel().getMaxSelectionIndex();

            // Check each cell in the range
            for (int r = rowIndexStart; r <= rowIndexEnd; r++) {
                for (int c = colIndexStart; c <= colIndexEnd; c++) {
                    if (tb_layout.isCellSelected(r, c)) {
                        // cell is selected
                        aavGPNs[c][r].clear();
                        aavGPNs[c][r].addAll(Arrays.asList(selection));
                    }
                }
            }
        }
        // actually it would be enough if this is called only if the mouse button is released
        // but how do I know that?
        // Anyway it seems that things still go fast enough
        setToolTipsForLayoutTable();
    }

    /**
     * Iterates through cells of layout table and constructs an array with strings for those cells, that have been
     * assigned.
     * <p>
     * Unassigned cells' strings remain null
     * <p>
     * Finally the array is given to the layout table.
     */
    public void setToolTipsForLayoutTable() {
        // only if layout table exists
        if (tb_layout != null) {
            Vector v;
            Iterator iter;
            StringBuffer sb = new StringBuffer();
            String[][] aasToolTips = new String[aavGPNs.length][aavGPNs[0].length];
            String[] names;
            GeneralUnitPNode pnode;
            Unit u;
            int iTTLines, iTTCols, iMore = 0;
            // if no nodes are assigned at a certain cell, the place in the array can remain null
            for (int i = 0; i < aavGPNs.length; i++) {
                for (int j = 0; j < aavGPNs[i].length; j++) {
                    v = aavGPNs[i][j];
                    if (v != null && v.size() > 0) {
                        sb.replace(0, sb.length(), "<html><p>");
                        iTTLines = 0;
                        iTTCols = 0;
                        iter = v.iterator();
                        while (iter.hasNext()) {
                            pnode = (GeneralUnitPNode) iter.next();
                            u = pnode.getUnit();
                            names = u.getMappedInputNames();
                            if (names != null) {
                                for (String element : names) {
                                    if (iTTLines < Commons.MAX_TOOLTIP_LINES) {
                                        sb.append(element);
                                        sb.append(", ");
                                        iTTCols += element.length() + 2;
                                        if (iTTCols > Commons.MAX_TOOLTIP_COLUMNS) {
                                            // new line
                                            iTTCols = 0;
                                            sb.append("<br>");
                                            iTTLines++;
                                        }
                                    } else {
                                        iMore++;
                                    }
                                }
                            }
                        }
                        if (iTTLines >= Commons.MAX_TOOLTIP_LINES) {
                            sb.append("<br>..." + iMore + " more");
                        }
                        sb.append("</p></html>");
                        aasToolTips[i][j] = sb.toString();
                    }
                }
            }
            tb_layout.setToolTips(aasToolTips);
        }
    }

    // @Override
    // public Dimension getPreferredSize() {
    // return new Dimension(state.controlElementsWidth, 660);
    // }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(state.controlElementsWidth, 330); // debug epei_intern (war: 330)
    }

    /**
     * Is called if the selection of <code>tb_layout</code> changes. Since it is not possible to determine the table
     * from the event's source, <code>tb_layout</code> is hardcoded.
     * <p>
     * The method marks all units on the <code>mapPane</code> who are assigned to at least one of the selected cells of
     * <code>tb_layout</code>
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        JTable table = tb_layout;
        if (e.getValueIsAdjusting()) {
            // The mouse button has not yet been released
        }
        // For single selection mode
        // int rowIndex = tb_layout.getSelectedRow();
        // int colIndex = tb_layout.getSelectedColumn();

        // Get the min and max ranges of selected cells
        int rowIndexStart = table.getSelectedRow();
        int rowIndexEnd = table.getSelectionModel().getMaxSelectionIndex();
        int colIndexStart = table.getSelectedColumn();
        int colIndexEnd = table.getColumnModel().getSelectionModel().getMaxSelectionIndex();

        bIgnoreNextSelectionChange = true; // unitSelectionChanged will be called
        // somPane.getCanvas().removeSelection(); // but it will be ignored
        bIgnoreNextSelectionChange = false;

        unmarkUnits();
        // Check each cell in the range
        for (int r = rowIndexStart; r <= rowIndexEnd; r++) {
            for (int c = colIndexStart; c <= colIndexEnd; c++) {
                if (table.isCellSelected(r, c)) {
                    if (r != -1 && c != -1 && aavGPNs[c][r] != null) {
                        Iterator iter = aavGPNs[c][r].iterator();
                        while (iter.hasNext()) {
                            GeneralUnitPNode item = (GeneralUnitPNode) iter.next();
                            markUnit(item.getUnit().getXPos(), item.getUnit().getYPos());
                        }
                    }

                }
            }
        }

    }

    /** We print relevant command line parameters if this panel is shown */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // is to become visible
            Commons.log.fine("Using directory for decoded files: " + Commons.sDecodedOutputDir);
            Commons.log.fine("Decoding probability: " + Commons.p_decode);
        }
    }

}