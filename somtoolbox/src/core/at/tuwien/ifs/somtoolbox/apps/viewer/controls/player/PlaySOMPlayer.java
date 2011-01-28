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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXBusyLabel;

import cern.colt.matrix.DoubleMatrix1D;
import edu.umd.cs.piccolo.nodes.PPath;

import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.RhythmPattern;
import at.tuwien.ifs.somtoolbox.apps.viewer.RhythmPatternsVisWindow;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractSelectionPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractViewerControl;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.PlaySOMPanel;
import at.tuwien.ifs.somtoolbox.data.DataDimensionException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.metadata.AbstractVectorMetaData;
import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.summarisation.SummariserGUI;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;

/**
 * @author Jakob Frank
 * @version $Id: PlaySOMPlayer.java 3752 2010-08-10 15:37:18Z frank $
 */
public class PlaySOMPlayer extends AbstractSelectionPanel implements PlayerListener {
    /**
     * @author Jakbo Frank
     * @version $Id: PlaySOMPlayer.java 3752 2010-08-10 15:37:18Z frank $
     */
    private final class SearchThread extends Thread {
        /**
         * 
         */
        private final String q;

        private boolean isCancelled;

        /**
         * @param q The search string
         */
        private SearchThread(String q) {
            this.q = q;
            this.isCancelled = false;
        }

        public void cancel() {
            isCancelled = true;
        }

        @Override
        public void run() {
            btnSearch.setText("Cancel");
            txtSearch.setEditable(false);
            lblFindBusy.setBusy(true);
            lblFindBusy.setVisible(true);
            lblFindLabel.setText("Searching...");

            setName("SearchThread (\"" + q + "\")");
            state.mapPNode.clearHistogramOverlayVisualization();
            if (searchList == null) {
                // init searchList
                searchList = new LinkedList<AbstractVectorMetaData>();
                String[] dNames = state.growingLayer.getAllMappedDataNames();
                for (String name : dNames) {
                    searchList.add(AbstractVectorMetaData.createMetaData(name));
                    if (isCancelled) {
                        searchList = null;
                        return;
                    }
                }
            }
            // System.err.println("Searching for \"" + q + "\"");
            clearList();
            hitHist = new int[state.growingLayer.getXSize()][state.growingLayer.getYSize()];
            for (int x = 0; x < hitHist.length; x++) {
                for (int y = 0; y < hitHist[x].length; y++) {
                    hitHist[x][y] = 0;
                    if (isCancelled) {
                        return;
                    }
                }
            }
            int c = 0;
            for (AbstractVectorMetaData vec : searchList) {
                // try {
                // Thread.sleep(25);
                // } catch (InterruptedException e) {
                // }
                if (vec.matches(q)) {
                    Unit u = state.growingLayer.getUnitForDatum(vec.getID());
                    hitHist[u.getXPos()][u.getYPos()] = 1;
                    addToList(vec.getID(), u);
                    playList.addSong(vec.getID());
                    c++;
                    if (isCancelled) {
                        break;
                    }
                }
            }

            btnSearch.setText("Find");
            txtSearch.setEditable(true);
            chkShowHH.setEnabled(true);
            lblFindBusy.setBusy(false);
            lblFindBusy.setVisible(false);
            lblFindLabel.setText(c + " items found.");
        }
    }

    private static final long serialVersionUID = 1L;

    private static final Color CIRCLE_COLOR = Color.BLUE;

    private static final int CIRCLE_STROKE_WIDTH = 15;

    private static final float CIRCLE_TRANSPARENCY = .4f;

    public static final String TITLE = "PlaySOMPlayer";

    /**
     * 0 == none, 1 == highlight, 2 == circle;
     */
    public static final int DEFAULT_PLAYING_MARKER = 2;

    private int playing_marker = DEFAULT_PLAYING_MARKER;

    private PlayList playList;

    private PPath circle;

    // private Hashtable<File, double[]> filePos;
    private Hashtable<File, GeneralUnitPNode> nodePos;

    private JTextField txtSearch;

    private JButton btnSearch;

    private Vector<GeneralUnitPNode> foundUnits = null;

    private GeneralUnitPNode playingNode;

    private JLabel lblFindLabel;

    private static Logger log = Logger.getLogger("at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.PlaySOMPlayer");

    // private JList lstPlayList;

    public PlaySOMPlayer(CommonSOMViewerStateData state) {
        super(new BorderLayout(), state, TITLE);

        // filePos = new Hashtable<File, double[]>();
        nodePos = new Hashtable<File, GeneralUnitPNode>();

        circle = createCircle();
        state.mapPNode.addChild(circle);
        // circle.repaint();

        // init gui
        initialize();
        // this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private PPath createCircle() {
        MapPNode mp = state.mapPNode;
        PPath c = PPath.createEllipse(0, 0, mp.getUnitWidth(), mp.getUnitHeight());
        c.setPickable(false);
        c.setStrokePaint(CIRCLE_COLOR);
        c.setStroke(new BasicStroke(CIRCLE_STROKE_WIDTH));
        c.setTransparency(CIRCLE_TRANSPARENCY);
        c.setVisible(false);

        return c;

    }

    @Override
    synchronized public void clearList() {
        super.clearList();
        playList.clearPlaylist();
    }

    synchronized public List<String> getPlayList() {
        return playList.getDataItems();
    }

    synchronized public void startPlaying() {
        playList.play();
    }

    synchronized public void startPlaying(String dataItem) {
        playList.play(playList.getIndexOf(dataItem));
    }

    synchronized public void stopPlaying() {
        playList.stop();
    }

    synchronized public void skipPlayer(int count) {
        playList.skip(count);
    }

    private void initialize() {
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

        /*
         * Would be nice to display the current Rhythm-Patterns Matrix while playing. Wouldn't it?
         */
        // Rhythm pattern
        final JPanel visPanel = createVisualisationPanel(liste);
        playList.addPlayerListener(new PlayerListener() {
            @Override
            public void playStarted(int mode, AudioVectorMetaData song) {
                visPanel.repaint();
            }

            @Override
            public void playStopped(int reason, AudioVectorMetaData song) {
                visPanel.repaint();
            }
        });

        final TitledCollapsiblePanel vP = new TitledCollapsiblePanel("Rhythm Pattern", new BorderLayout());
        vP.setCollapsed(true);
        vP.setContentBorder(BorderFactory.createEmptyBorder());
        vP.add(visPanel, BorderLayout.CENTER);
        main.add(vP, gc);
        gc.gridy += 1;

        PlayerControl pc = new PlayerControl(playList);
        TitledCollapsiblePanel pP = new TitledCollapsiblePanel("Player", new BorderLayout());
        pP.add(pc, BorderLayout.CENTER);
        main.add(pP, gc);
        gc.gridy += 1;

        gc.weighty = 1;
        main.add(scpPlayList, gc);
        gc.weighty = 0;
        gc.gridy += 1;

        /* Core-Functionality done, now some extras */

        // Search
        gc.insets.top = 2;
        main.add(createSearchPanel(), gc);

        // // RhythmPattern
        // gc.gridy += 1;
        // gc.gridwidth = 1;
        // gc.insets.top = 0;
        // main.add(createRhythmPatternButton(liste), gc);
        //
        // // Export Playlist
        // gc.gridx += 1;
        // main.add(createExportPlaylistButton(liste), gc);

        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.gridy++;
        main.add(createExtraFuncPanel(liste), gc);

        // this.pack();
        this.setVisible(true);
    }

    private JPanel createExtraFuncPanel(JList liste) {
        TitledCollapsiblePanel extra = new TitledCollapsiblePanel("Extras", new GridBagLayout());
        GridBagConstraintsIFS gc = new GridBagConstraintsIFS(GridBagConstraints.CENTER, 0);

        extra.setCollapsed(true);

        // RhythmPattern
        extra.add(createRhythmPatternButton(liste), gc);

        // Export Playlist
        extra.add(createExportPlaylistButton(liste), gc.nextCol());

        // Summarise button - new Code from Julius Penaranda
        JButton buttonSummarise = new JButton("Summarise");
        buttonSummarise.setMargin(SMALL_INSETS);
        buttonSummarise.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // require input vector file
                if (state.inputDataObjects.getInputData() == null) { // not loaded
                    SOMVisualisationData inputObject = state.inputDataObjects.getObject(SOMVisualisationData.INPUT_VECTOR);
                    try { // try to load it
                        inputObject.loadFromFile(state.fileChooser, state.parentFrame);
                    } catch (SOMToolboxException exception) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(exception.getMessage());
                    }
                    if (state.inputDataObjects.getInputData() == null) { // not loaded
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                "Input vector file needed for summarisation!");
                        return;
                    }
                }

                Object[] items;

                if (PlaySOMPlayer.this.playlists[0].isSelectionEmpty()) {
                    items = new Object[PlaySOMPlayer.this.playlists[0].getModel().getSize()];
                    for (int i = 0; i < PlaySOMPlayer.this.playlists[0].getModel().getSize(); i++) {
                        items[i] = PlaySOMPlayer.this.playlists[0].getModel().getElementAt(i);
                    }
                } else {
                    items = PlaySOMPlayer.this.playlists[0].getSelectedValues();
                }
                JFrame parent = (SOMViewer) PlaySOMPlayer.this.getParent().getParent().getParent().getParent().getParent().getParent();
                new SummariserGUI(parent, state, items);

            }
        });
        extra.add(buttonSummarise, gc.nextRow());
        // END: Summarise

        // BEGIN: Andreas Senfter: add evalPanel Button
        JButton btEvalSOM = new JButton("Acoustic Evaluation");
        btEvalSOM.setMargin(SMALL_INSETS);
        btEvalSOM.addActionListener(new ActionListener() {
            private AbstractViewerControl evalPanel = null;

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends AbstractViewerControl> c = (Class<? extends AbstractViewerControl>) Class.forName("at.tuwien.ifs.somtoolbox.apps.viewer.controls.EvalSOMPanel");

                    if (evalPanel == null) {
                        Constructor<? extends AbstractViewerControl> constr = c.getConstructor(
                                CommonSOMViewerStateData.class, PlaySOMPanel.class);
                        // evalPanel = new EvalSOMPanel(state, this);
                        evalPanel = constr.newInstance(new Object[] { state, this });
                        // PlaySOMPlayer.super.getDesktopPane().add(evalPanel);
                    }
                    if (evalPanel != null) {
                        evalPanel.setVisible(true);
                        evalPanel.setSelected(true);
                    }
                } catch (InvocationTargetException e2) {
                    e2.getCause().printStackTrace();
                } catch (Exception e2) {
                }
            }

        });
        extra.add(btEvalSOM, gc.nextCol());
        // END: Andreas Senfter

        return extra;
    }

    private JButton createExportPlaylistButton(final JList liste) {
        JButton b = new JButton("Export Playlist");

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = state.fileChooser;
                if (fc.showSaveDialog(PlaySOMPlayer.this) != JFileChooser.APPROVE_OPTION) {
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
        b.setMargin(SMALL_INSETS);
        return b;
    }

    private JButton createRhythmPatternButton(final JList liste) {
        JButton b = new JButton();
        b.setText("Show RP");
        b.setToolTipText("Show the Rhythm Pattern of the current song.");

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndices[] = liste.getSelectedIndices();

                if (selectedIndices.length == 0) {
                    String label;
                    Unit unit;
                    Collection<GeneralUnitPNode> c = nodePos.values();
                    List<Unit> tmp = new ArrayList<Unit>();
                    for (GeneralUnitPNode node : c) {
                        unit = node.getUnit();
                        if (!tmp.contains(unit)) {
                            tmp.add(unit);
                            label = "Weight vector of map unit (" + unit.getXPos() + "/" + unit.getYPos() + ")";
                            showRhythmPattern(unit.getWeightVector(), label);
                        }
                    }
                } else {
                    ArrayList<String> vecNames;
                    vecNames = new ArrayList<String>();

                    for (int selectedIndice : selectedIndices) {
                        try {
                            // vecNames[i] = (String) this.playlists[0].getModel().getElementAt(selectedIndices[i]);
                            vecNames.add(playList.getPlayListItem(selectedIndice).getID());
                            // System.out.println(selectedIndices[i]+ ": " + vecNames[i]);
                        } catch (NullPointerException npe) {
                            // This happens sometimes with an empty playlist. No need to worry!
                        }
                    }
                    showRhythmPattern(vecNames.toArray(new String[] {}));
                }
            }
        });

        b.setMargin(SMALL_INSETS);
        return b;
    }

    private JPanel createVisualisationPanel(JList list) {
        JPanel rpVis = new JPanel();
        // JPanel visualisation = new JPanel();
        rpVis.setLayout(new GridBagLayout());
        rpVis.setBackground(Color.black);
        rpVis.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        JPanel draw = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.black);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                try {
                    String di = playList.getPlayListItem(playList.getCurrentSongIndex()).getID();
                    if (di.equals("")) {
                        return;
                    }
                    ;
                    InputData iv = state.inputDataObjects.getInputData();

                    InputDatum id = iv.getInputDatum(di);
                    RhythmPattern rp = new RhythmPattern(id.getVector().toArray(), iv.getFeatureMatrixColumns(),
                            iv.getFeatureMatrixRows());
                    rp.bx = 3;
                    rp.by = 3;
                    rp.paint(g);
                } catch (DataDimensionException e) {
                    log.info(e.getMessage());
                    // e.printStackTrace();
                } catch (Exception e) {
                    // No song playing right now. TODO: Handle this issue better.
                }
                // super.paintComponent(g);
            }

        };
        // draw.setMinimumSize(new Dimension(60 * 3, 24 * 3));
        // draw.setPreferredSize(new Dimension(60 * 3, 24 * 3));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        rpVis.add(draw, gc);

        return rpVis;
    }

    /**
     * show RhythmPattern(s) of selected vector(s)
     */
    public void showRhythmPattern(String[] vecNames) {
        if (vecNames == null) {
            return;
        }
        if (vecNames.length == 0) {
            return;
        }

        String dispname; // name for pattern to display in window
        // String content_subtype;
        DoubleMatrix1D vec = null;
        InputDatum iv = null;
        InputData inputVectors = state.inputDataObjects.getInputData();

        if (inputVectors == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Input vectors not found! Please load input vector file first!");
            return;
        }

        // by omitting this, RP vis is also possible for non-RP data
        // if (!inputVectors.getContentType().equals("audio")) {
        // Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Content type of input vectors is not 'audio'!");
        // return;
        // }
        //
        // content_subtype = inputVectors.getContentSubType();
        // if (!content_subtype.startsWith("rp")) {
        // Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Content subtype of input vectors is not 'rp'!");
        // return;
        // }

        int xdim = inputVectors.getFeatureMatrixColumns();
        int ydim = inputVectors.getFeatureMatrixRows();

        if (xdim == -1 || ydim == -1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Could not get dimensions of Rhythm Patterns!" + " - Defaulting to 24x60 Rhythm Pattern.");
            xdim = 60;
            ydim = 24;
        }

        if (vecNames.length == 1) {// show single Rhythm Pattern
            if (vecNames[0] == null) {
                return;
            }

            iv = inputVectors.getInputDatum(vecNames[0]);
            if (iv == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not get input vector data for " + vecNames[0]);
                return;
            }
            vec = iv.getVector();

            dispname = vecNames[0];
        } else { // compute average of multiple vectors
            vec = inputVectors.getMeanVector(vecNames);
            dispname = Integer.toString(vecNames.length) + " selected vectors";
        }

        if (vec == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not get (mean) input vector data for selected vectors!");
            return;
        }

        RhythmPatternsVisWindow rpwin;
        try {
            rpwin = new RhythmPatternsVisWindow(state.parentFrame, vec, xdim, ydim, dispname);
            rpwin.pack();
            rpwin.setLocation(300, 100);
            rpwin.setVisible(true);
        } catch (DataDimensionException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
        }
    }

    /**
     * show RhythmPattern(s) of weight vector(s) of currently selected unit(s)
     */
    public void showRhythmPattern(double[] vector, String dispname) {
        // TODO solve this: xdim and ydim again have to be guessed without inputvector file or $DATA_TYPE

        InputData inputVectors = state.inputDataObjects.getInputData();

        int xdim = -1;
        int ydim = -1;

        if (inputVectors == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Input vectors not found! Please load input vector file first!");
        } else {
            xdim = inputVectors.getFeatureMatrixColumns();
            ydim = inputVectors.getFeatureMatrixRows();
        }

        if (xdim == -1 || ydim == -1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Could not get dimensions of Rhythm Patterns!" + " - Defaulting to 24x60 Rhythm Pattern.");
            xdim = 60;
            ydim = 24;
        }

        RhythmPatternsVisWindow rpwin;
        try {
            rpwin = new RhythmPatternsVisWindow(state.parentFrame, vector, xdim, ydim, dispname);
            rpwin.pack();
            rpwin.setLocation(300, 100);
            rpwin.setVisible(true);
        } catch (DataDimensionException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
        }
    }

    private JPanel createSearchPanel() {
        TitledCollapsiblePanel search = new TitledCollapsiblePanel("Search/Find");
        search.setLayout(new GridBagLayout());

        GridBagConstraints l = new GridBagConstraints();
        GridBagConstraints t = new GridBagConstraints();
        GridBagConstraints b = new GridBagConstraints();

        l.gridx = 0;
        l.gridy = 0;
        l.weightx = 0;
        l.anchor = GridBagConstraints.EAST;
        l.insets.right = 2;

        t.fill = GridBagConstraints.BOTH;
        t.gridx = 1;
        t.gridy = 0;
        t.weightx = 1;

        b.gridx = 2;
        b.gridy = 0;
        b.gridwidth = GridBagConstraints.REMAINDER;
        b.weightx = 0;

        txtSearch = new JTextField();
        txtSearch.setToolTipText("Search for items, using (java) regular expressions");
        final String SEARCH_BUTTON_TEXT = "Find";
        btnSearch = new JButton(SEARCH_BUTTON_TEXT);
        btnSearch.setMargin(SMALL_INSETS);
        lblFindBusy = new JXBusyLabel(new Dimension(12, 12));
        lblFindBusy.setVisible(false);
        lblFindLabel = new JLabel("");
        // TODO: Use BusyButton
        chkShowHH = new JCheckBox("Show ResultHistogram");
        chkShowHH.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chkShowHH.isSelected()) {
                    state.mapPNode.showHistogramOverlayVisualization(hitHist, 0);
                } else {
                    state.mapPNode.clearHistogramOverlayVisualization();
                }

            }
        });
        chkShowHH.setEnabled(false);

        JLabel find = new JLabel("Find:");
        find.setLabelFor(txtSearch);

        search.add(find, l);
        search.add(txtSearch, t);
        search.add(btnSearch, b);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
        GridBagConstraints x = new GridBagConstraints();
        x.gridx = 0;
        x.gridwidth = 3;
        x.gridx = GridBagConstraints.REMAINDER;
        p.add(lblFindBusy);
        p.add(lblFindLabel);
        search.add(p, x);
        search.add(chkShowHH, x);

        ActionListener searchAC = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchThread != null && searchThread.isAlive()) {
                    searchThread.cancel();
                } else {
                    searchThread = new SearchThread(txtSearch.getText().trim());
                    searchThread.start();
                }

            }
        };

        txtSearch.addActionListener(searchAC);
        btnSearch.addActionListener(searchAC);

        return search;
    }

    private List<AbstractVectorMetaData> searchList = null;

    private JXBusyLabel lblFindBusy;

    int[][] hitHist = new int[state.growingLayer.getXSize()][state.growingLayer.getYSize()];

    private JCheckBox chkShowHH;

    private SearchThread searchThread = null;

    @SuppressWarnings("unused")
    private void oldSearchFor(String query) {

        if (foundUnits != null) {
            for (GeneralUnitPNode u : foundUnits) {
                u.removeQueryHit();
            }
        }
        foundUnits = null;

        if (query == "") {
            // empty search, disable
            playing_marker = DEFAULT_PLAYING_MARKER;
            return;
        } else {
            playing_marker = 2;
        }

        Pattern searchPattern;
        try {
            searchPattern = Pattern.compile(query);
        } catch (PatternSyntaxException pse) {
            Logger.getLogger("PlaySOMPlayer").warning("Could not compile search string: \"" + query + "\"");
            return;
        }

        Unit[] units = state.growingLayer.getAllUnits();
        foundUnits = new Vector<GeneralUnitPNode>();
        for (Unit unit : units) {
            String[] data = unit.getMappedInputNames();
            if (data == null) {
                continue;
            }
            boolean firstHitOnThisUnit = true;
            for (String element : data) {
                Matcher m = searchPattern.matcher(element);
                if (m.matches()) {
                    System.out.println("Found " + element);
                    if (firstHitOnThisUnit) {
                        GeneralUnitPNode gup = state.mapPNode.getUnit(unit);
                        foundUnits.add(gup);
                        gup.setQueryHit();
                        // firstHitOnThisUnit = false;
                    }
                    addToList(element, unit);
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractSelectionPanel#addToList(java.lang.String, at.tuwien.ifs.somtoolbox.layers.Unit)
     */
    @Override
    public void addToList(String elementName, Unit u) {
        addToList(elementName, CommonSOMViewerStateData.fileNamePrefix, u);
    }

    @Override
    public void addToList(String elementName, String fileNamePrefix, Unit u) {
        super.addToList(elementName, fileNamePrefix, u);
        File candidate = new File(fileNamePrefix, elementName);
        if (candidate.exists()) {
            playList.addSong(elementName, candidate);
            nodePos.put(candidate, state.mapPNode.getUnit(u));

        }
    }

    @Override
    public void unitSelectionChanged(Object[] selection, boolean newSelection) {
        // super.unitSelectionChanged(selection, newSelection);
        // System.out.println("SELECT: " + selection.length + " (" + newSelection + ")");

        // newUnit++;
        playList.clearPlaylist();

        // unitsInPlaylist.clear();
        new Thread() {
            @Override
            public void run() {
                setName("PlayerStopper");
                playList.stop();
            }
        }.start();

        Vector<Unit> list2 = new Vector<Unit>();
        for (Object element : selection) {
            if (element instanceof GeneralUnitPNode) {
                GeneralUnitPNode node = (GeneralUnitPNode) element;
                list2.add(node.getUnit());
                if (!unitsInPlaylist.contains(node.getUnit())) {
                    unitsInPlaylist.add(node.getUnit());
                }
                String[] items = node.getMappedDataNames();
                if (items != null) {
                    for (String item : items) {
                        // for (int j = 0; j < 1; j++) {
                        File song = new File(CommonSOMViewerStateData.fileNamePrefix + item
                                + CommonSOMViewerStateData.fileNameSuffix);
                        playList.addSong(item, song);
                        // filePos.put(song, new double[] {node.getX(), node.getY()});
                        nodePos.put(song, node);
                    }
                }
            }
        }
        unitsInPlaylist.retainAll(list2);
    }

    @Override
    public void playStarted(int mode, AudioVectorMetaData song) {
        playingNode = nodePos.get(song.getAudioFile());
        // System.out.println("Moving circle to " + node.getX() + "/" + node.getY());
        if (playingNode == null) {
            return;
        }
        switch (playing_marker) {
            case 0:
                break;
            case 1:
                playingNode.setSelected(true);
                break;
            case 2:
                double x = playingNode.getX();// - (CIRCLE_STROKE_WIDTH / 2);
                double y = playingNode.getY();// - (CIRCLE_STROKE_WIDTH / 2);
                // if (circle.getVisible()) {
                circle.setVisible(true);
                circle.animateToPositionScaleRotation(x, y, 1, 0, 750);
                // } else {
                // circle.setX(x);
                // circle.setY(y);
                // }
                state.mapPNode.repaint();
                break;
        }
    }

    @Override
    public void playStopped(int reason, AudioVectorMetaData song) {
        // System.out.println("Hiding circle");
        if (playingNode != null) {
            if (playing_marker == 1) {
                playingNode.setSelected(false);
            }
        }

        circle.setVisible(false);
        state.mapPNode.repaint();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(state.controlElementsWidth, 350);
    }

    public String getCurrentSongID() {
        try {
            return playList.getPlayListItem(playList.getCurrentSongIndex()).getID();
        } catch (Exception e) {
            return "";
        }
    }

    public AudioVectorMetaData getCurrentSong() {
        try {
            return playList.getPlayListItem(playList.getCurrentSongIndex());
        } catch (Exception e) {
            return null;
        }
    }

    public double[] getCurrentPos() {
        try {
            double x, y;

            x = playingNode.getX() / state.mapPNode.getWidth() + playingNode.getWidth();
            y = playingNode.getY() / state.mapPNode.getHeight() + playingNode.getHeight();

            return new double[] { x, y };
        } catch (Exception e) {
            return new double[] { 0.0, 0.0 };
        }
    }

    public void addPlayerListener(PlayerListener l) {
        playList.addPlayerListener(l);
    }

    public void removePlayerListener(PlayerListener l) {
        playList.removePlayerListener(l);
    }

    public void addPlayListListener(PlayListListener pll) {
        playList.addPlayListListener(pll);
    }

    public void removePlayListListener(PlayListListener pll) {
        playList.removePlayListListener(pll);
    }

    public boolean isPlaying() {
        return playList.isPlaying();
    }
}
