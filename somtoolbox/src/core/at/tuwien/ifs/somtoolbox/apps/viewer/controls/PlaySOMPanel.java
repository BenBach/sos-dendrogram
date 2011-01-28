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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import cern.colt.matrix.DoubleMatrix1D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.RhythmPatternsVisWindow;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.PlayList;
import at.tuwien.ifs.somtoolbox.data.DataDimensionException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.summarisation.SummariserGUI;

/**
 * This class represents the panel for the PlaySOM,<br/>
 * it provides controls for exporting and editing of selected playlists.
 * 
 * @author Michael Dittenbach
 * @version $Id: PlaySOMPanel.java 3873 2010-10-28 09:29:58Z frank $
 */
public class PlaySOMPanel extends AbstractSelectionPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private JButton btPlaySelected = null;

    private JButton btPlayAll = null;

    private JButton btDelSelected = null;

    private JButton btExportPlaylist = null;

    private JButton btRhythmPatternVis = null;

    private JButton btEvalSOM = null;

    private JButton buttonSummarise = null;

    private JRadioButton rbFlat;

    private JRadioButton rbSmoothed;

    private JButton btClearHist = null;

    private JTextField txSearchField = null;

    private JLabel lblSearch = null;

    private JCheckBox chkCountHist = null;

    private final String PLAY_SELECTED = "playSelected";

    public final String PLAY_ALL = "playAll";

    private final String DELETE_SELECTED = "deleteSelected";

    private final String EXPORT_PLAYLIST = "exportPlaylist";

    private final String SEARCH = "search";

    private final String SHOW_RHYTHM_PATTERN = "showRhythmPattern";

    private final String SHOW_COUNT_HIST = "showCountHistogram";

    private final String CLEAR_COUNT_HIST = "clearCountHistogram";

    private final String SUMMARISE = "summarise";

    private final String EVAL_SOM = "evaluate";

    private JFrame parent = null;

    private AbstractViewerControl evalPanel = null;

    /**
     * 
     */
    public PlaySOMPanel(CommonSOMViewerStateData state) {
        super(new GridBagLayout(), state, "PlaySOM Control");
        this.initGUIElements();
        setVisible(true);
    }

    /**
     * creates the GUI elements of the Panel (a few JButtons and the JList)
     */
    protected void initGUIElements() {
        setPreferredSize(new Dimension(state.controlElementsWidth, 400));
        int bt_row = 0;
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        // Play buttons
        btPlaySelected = new JButton("Play selected");
        btPlaySelected.setActionCommand(PLAY_SELECTED);
        btPlaySelected.addActionListener(this);
        c.gridx = 0;
        c.gridy = bt_row;
        c.anchor = GridBagConstraints.NORTH;
        getContentPane().add(btPlaySelected, c);
        btPlayAll = new JButton("Play all");
        btPlayAll.setActionCommand(PLAY_ALL);
        btPlayAll.addActionListener(this);
        c.gridx = 1;
        c.gridy = bt_row;
        c.anchor = GridBagConstraints.NORTH;
        getContentPane().add(btPlayAll, c);
        bt_row++;

        // Remove/Export buttons
        btDelSelected = new JButton("Remove selected");
        btDelSelected.setActionCommand(DELETE_SELECTED);
        btDelSelected.addActionListener(this);
        c.gridx = 0;
        c.gridy = bt_row;
        c.anchor = GridBagConstraints.NORTH;
        getContentPane().add(btDelSelected, c);
        btExportPlaylist = new JButton("Export Playlist");
        btExportPlaylist.setActionCommand(EXPORT_PLAYLIST);
        btExportPlaylist.setEnabled(false);
        btExportPlaylist.addActionListener(this);
        c.gridx = 1;
        c.gridy = bt_row;
        c.anchor = GridBagConstraints.NORTH;
        getContentPane().add(btExportPlaylist, c);
        bt_row++;

        // BEGIN: Andreas Senfter: add evalPanel Button
        btEvalSOM = new JButton("Acoustic Evaluation");
        btEvalSOM.setActionCommand(EVAL_SOM);
        btEvalSOM.addActionListener(this);
        c.gridx = 0;
        c.gridy = bt_row;
        c.anchor = GridBagConstraints.NORTH;
        getContentPane().add(btEvalSOM, c);
        // END: Andreas Senfter

        // Summarise button - new Code from Julius Penaranda
        buttonSummarise = new JButton("Summarise");
        buttonSummarise.setActionCommand(SUMMARISE);
        buttonSummarise.addActionListener(this);
        c.gridx = 1;
        c.anchor = GridBagConstraints.NORTH;
        getContentPane().add(buttonSummarise, c);
        bt_row++;

        // Playlist
        c.gridx = 0;
        c.gridy = bt_row;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.weighty = 1.0;
        addSingleListScrollPanel(c);
        playlists[0].setVisibleRowCount(14);
        // //this is important: it will determine the size of the whole PlaySOMPanel
        // //playlists[0].setPreferredSize(playlists[0].getSize());
        // playlists[0].setPreferredSize(new Dimension(200,400));
        // playlists[0].setMinimumSize(new Dimension(150,200));
        c.weightx = 0.5;
        c.weighty = 0.0;
        bt_row++;

        // button for Rhythm Patterns Visualization
        btRhythmPatternVis = new JButton("Show Rhythm Pattern");
        btRhythmPatternVis.setActionCommand(SHOW_RHYTHM_PATTERN);
        btRhythmPatternVis.setEnabled(false);
        btRhythmPatternVis.addActionListener(this);
        c.gridx = 0;
        c.gridy = bt_row;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.SOUTH;
        getContentPane().add(btRhythmPatternVis, c);
        bt_row++;

        // File name search
        lblSearch = new JLabel("File name search: ");
        c.gridx = 0;
        c.gridy = bt_row;
        c.gridwidth = 1;
        getContentPane().add(lblSearch, c);

        txSearchField = new JTextField();
        c.gridx = 1;
        c.gridy = bt_row;
        getContentPane().add(txSearchField, c);
        txSearchField.setActionCommand(SEARCH);
        txSearchField.addActionListener(this);
        bt_row++;

        // check box for Hit Histogram Visualization
        chkCountHist = new JCheckBox("show Hit Histogram");
        chkCountHist.setActionCommand(SHOW_COUNT_HIST);
        chkCountHist.addActionListener(this);
        c.gridx = 0;
        c.gridy = bt_row;
        c.anchor = GridBagConstraints.WEST;
        getContentPane().add(chkCountHist, c);

        // radio buttons for type of Hit Histogram Visualization
        rbFlat = new JRadioButton("flat", true);
        rbSmoothed = new JRadioButton("smoothed");
        ButtonGroup rbgroup = new ButtonGroup();
        rbgroup.add(rbFlat);
        rbgroup.add(rbSmoothed);
        rbFlat.setEnabled(chkCountHist.isSelected());
        rbSmoothed.setEnabled(chkCountHist.isSelected());
        JPanel rbPanel = new JPanel();
        rbPanel.add(rbFlat);
        rbPanel.add(rbSmoothed);
        c.gridx = 1;
        c.gridy = bt_row;
        c.anchor = GridBagConstraints.EAST;
        getContentPane().add(rbPanel, c);
        bt_row++;

        // button for removing the Count Histogram Visualization
        btClearHist = new JButton("clear Hit Histogram");
        btClearHist.setActionCommand(CLEAR_COUNT_HIST);
        btClearHist.setEnabled(true);
        btClearHist.addActionListener(this);
        c.gridx = 0;
        c.gridy = bt_row;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.SOUTH;
        getContentPane().add(btClearHist, c);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == PLAY_SELECTED) {
            int selectedIndices[] = this.playlists[0].getSelectedIndices();
            if (selectedIndices.length > 0) {
                CommonSOMViewerStateData.MimeTypes.doSysCalls(this.getSelectedSongs());
            }
        } else if (e.getActionCommand() == PLAY_ALL) {
            CommonSOMViewerStateData.MimeTypes.doSysCalls(absPathVectors[0].toArray());
        } else if (e.getActionCommand() == DELETE_SELECTED) {
            int[] sel = playlists[0].getSelectedIndices();
            for (int i = sel.length - 1; i >= 0; i--) {
                playlistmodels[0].removeElementAt(sel[i]);
                absPathVectors[0].remove(sel[i]);
            }
        }
        // BEGIN: Andreas Senfter: open EvalSOMPanel
        else if (e.getActionCommand() == EVAL_SOM) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends AbstractViewerControl> c = (Class<? extends AbstractViewerControl>) Class.forName("at.tuwien.ifs.somtoolbox.apps.viewer.controls.EvalSOMPanel");

                if (evalPanel == null) {
                    Constructor<? extends AbstractViewerControl> constr = c.getConstructor(
                            CommonSOMViewerStateData.class, PlaySOMPanel.class);
                    // evalPanel = new EvalSOMPanel(state, this);
                    evalPanel = constr.newInstance(new Object[] { state, this });
                    // super.getDesktopPane().add(evalPanel);
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
        // END: Andreas Senfter

        else if (e.getActionCommand() == EXPORT_PLAYLIST) {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Playlists (*.m3u)", "m3u"));
            fc.showSaveDialog(this);
            File selFile = fc.getSelectedFile();
            PlayList pl = new PlayList(selFile);
            pl.setSongs(this.absPathVectors[0].toArray());
            if (selFile != null) {
                pl.writeToFile();
            }
        } else if (e.getActionCommand() == SEARCH) {
            String query = txSearchField.getText();
            searchFilenames(query, chkCountHist.isSelected());
            txSearchField.setText("");
        } else if (e.getActionCommand() == SHOW_COUNT_HIST) {
            rbFlat.setEnabled(chkCountHist.isSelected());
            rbSmoothed.setEnabled(chkCountHist.isSelected());
        } else if (e.getActionCommand() == CLEAR_COUNT_HIST) {
            state.mapPNode.clearHistogramOverlayVisualization();
        } else if (e.getActionCommand() == SHOW_RHYTHM_PATTERN) {
            String[] vecNames;
            int selectedIndices[] = this.playlists[0].getSelectedIndices();

            if (selectedIndices.length == 0) {
                // show averaged Rhythm Pattern of _all_ files
                // int size = this.playlists[0].getModel().getSize();
                // if (size == 0) return;
                //                
                // vecNames = new String[size];
                //                
                // for (int i = 0; i < size; i++)
                // {
                // vecNames[i] = (String) this.playlists[0].getModel().getElementAt(i);
                // }
                // showRhythmPattern(vecNames);

                // show RhythmPattern(s) of weight vector(s) of currently selected unit(s)
                String label;
                Unit unit;
                int nu = selections[0].length;
                for (int u = 0; u < nu; u++) {
                    unit = selections[0][u].getUnit();
                    label = "Weight vector of map unit (" + unit.getXPos() + "/" + unit.getYPos() + ")";
                    showRhythmPattern(unit.getWeightVector(), label);
                }
            } else {
                vecNames = new String[selectedIndices.length];

                for (int i = 0; i < selectedIndices.length; i++) {
                    vecNames[i] = (String) this.playlists[0].getModel().getElementAt(selectedIndices[i]);
                    // System.out.println(selectedIndices[i]+ ": " + vecNames[i]);
                }
                showRhythmPattern(vecNames);
            }
        }
        // new code from Julius Penaranda -----------------------------------------------------
        else if (e.getActionCommand() == SUMMARISE) {
            // require input vector file
            if (state.inputDataObjects.getInputData() == null) { // not loaded
                SOMVisualisationData inputObject = state.inputDataObjects.getObject(SOMVisualisationData.INPUT_VECTOR);
                try { // try to load it
                    inputObject.loadFromFile(state.fileChooser, state.parentFrame);
                } catch (SOMToolboxException exception) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(exception.getMessage());
                }
                if (state.inputDataObjects.getInputData() == null) { // not loaded
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Input vector file needed for summarisation!");
                    return;
                }
            }

            Object[] items;
            if (this.playlists[0].isSelectionEmpty()) {
                items = new Object[this.playlists[0].getModel().getSize()];
                for (int i = 0; i < this.playlists[0].getModel().getSize(); i++) {
                    items[i] = this.playlists[0].getModel().getElementAt(i);
                }
            } else {
                items = this.playlists[0].getSelectedValues();
            }
            parent = (SOMViewer) this.getParent().getParent().getParent().getParent().getParent().getParent();
            new SummariserGUI(parent, state, items);
        }
    }

    /**
     * called whenever the selection has changed, the new selection is added to the JList and to the list of the file's
     * absolute pathnames
     */
    @Override
    public void unitSelectionChanged(Object[] selection, boolean newSelection) {
        super.unitSelectionChanged(selection, newSelection);
        if (selection.length > 0) {
            // activate the export button if the selection contains only mp3s
            if (playlistmodels[0].toArray().length > 0) {
                if (CommonSOMViewerStateData.MimeTypes.hasSingleFileType(this.absPathVectors[0].toArray())
                        && CommonSOMViewerStateData.MimeTypes.isExportable(this.absPathVectors[0].toArray())) {
                    this.btExportPlaylist.setEnabled(true);
                    this.btRhythmPatternVis.setEnabled(true);
                }
            } else {
                this.btExportPlaylist.setEnabled(false);
                this.btRhythmPatternVis.setEnabled(false);
            }
            if (playlistmodels[0].toArray() == null) {
                this.btExportPlaylist.setEnabled(false);
            }
        }
    }

    private void enableButtons(boolean bool) {
        this.btExportPlaylist.setEnabled(bool);
        this.btRhythmPatternVis.setEnabled(bool);
    }

    /**
     * parses a String for next integer, skipping spaces
     * 
     * @param string to parse
     * @param startpos starting position within string for search
     * @return Point: x contains start position of integer, y end position (null if parsing fails)
     */
    private Point getPosOfNextInt(String string, int startpos) {
        int l = string.length();
        Point pos = new Point(); // x will store start position, y will store end position
        pos.y = startpos;

        while (pos.y < l && Character.isSpaceChar(string.charAt(pos.y))) {
            pos.y++;
        }
        if (pos.y == l) {
            return null; // (spaces till the end)
        }
        pos.x = pos.y;
        while (pos.y < l && Character.isDigit(string.charAt(pos.y))) {
            pos.y++;
        }
        if (pos.y == pos.x) {
            return null; // (no digits found)
        }

        return pos;
    }

    /**
     * compare "ordering index" of a filename if it is smaller or greater than (a) certain value(s) ordering index is
     * assumed to be a number between two points before the filename extension e.g. mypieceofmusic.0345.mp3 -> ordering
     * index is 345
     * 
     * @param string filename string containing an ordering index (e.g. mypieceofmusic.0345.mp3)
     * @param ind_greater integer, ordering index must be greater in comparison (if set to -1, ind_greater will be
     *            ignored)
     * @param ind_smaller integer, ordering index must be smaller in comparison (if set to -1, ind_smaller will be
     *            ignored)
     * @return true if comparison result is true, false if comparison result is false or ordering index could not be
     *         parsed
     */
    private boolean compareOrderIndex(String string, int ind_greater, int ind_smaller) {
        int p2 = string.lastIndexOf("."); // search . from extension
        if (p2 == -1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Index of . could not be found in filename '" + string + "'");
            return false;
        } else if (p2 == 0) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Second last . could not be found in filename '" + string + "'");
            return false;
        }

        int p1 = string.lastIndexOf(".", p2 - 1); // search 2nd last point
        if (p1 == -1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Second last . could not be found in filename '" + string + "'");
            return false;
        }
        p1 = p1 + 1;

        System.out.println("file: " + string + " p1/p2:" + p1 + "/" + p2);

        int file_ind;

        try {
            file_ind = Integer.parseInt(string.substring(p1, p2));
            System.out.println("file_ind: " + file_ind + "ind_smaller: " + ind_smaller + " ind_greater: " + ind_greater);
        } catch (NumberFormatException nfe) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Order index could not be found in filename '" + string + "'! Search failed!");
            return false;
        }

        if (ind_smaller > -1) {
            if (!(file_ind < ind_smaller)) {
                return false;
            }
        }

        if (ind_greater > -1) {
            if (!(file_ind > ind_greater)) {
                return false;
            }
        }

        return true;
    }

    /**
     * traverses all units of the map and searches for the query string as substring of the document filenames updates
     * the playlist with the filenames found and marks the according units on the map
     * 
     * @param query the string for querying the filenames
     * @param createHistogram if set to true, the results is shown in an Islands of Music / SDH like manner if set to
     *            false, the units containing the items found are marked with a single color
     */
    private void searchFilenames(String query, boolean createHistogram) {
        // TODO: Also search in the depth (3rd dimension)?
        int xSize = state.growingLayer.getXSize();
        int ySize = state.growingLayer.getYSize();
        Unit[][] units = state.growingLayer.get2DUnits();
        int[][] hist = null; // storing unit counts as histogram
        String[] mappedNames;
        int nfound = 0;
        int ind_greater = -1;
        int ind_smaller = -1;

        String searchmsg = "";

        /* the usage of > and/or < allows search for ordered indices included in filenames */
        if (query.indexOf(">") != -1 || query.indexOf("<") != -1) {
            searchmsg = searchmsg + "and ordering index ";
            int p;
            Point pos; // start and end position of >|< number expression
            p = query.indexOf(">");
            if (p > -1) {
                pos = getPosOfNextInt(query, p + 1);
                if (pos == null) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Invalid usage of > in search expression!");
                    return;
                }
                ind_greater = Integer.parseInt(query.substring(pos.x, pos.y));
                query = new StringBuffer(query).delete(p, pos.y).toString();
                System.out.println("query: " + query);
                searchmsg = searchmsg + "> " + ind_greater;
            }

            p = query.indexOf("<");
            if (p > -1) {
                pos = getPosOfNextInt(query, p + 1);
                if (pos == null) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Invalid usage of < in search expression!");
                    return;
                }
                ind_smaller = Integer.parseInt(query.substring(pos.x, pos.y));
                query = new StringBuffer(query).delete(p, pos.y).toString();
                System.out.println("query: " + query);
                searchmsg = searchmsg + "< " + ind_smaller;
            }
        }

        query = query.trim(); // really quit all leading and trailing spaces?

        searchmsg = "Searching filenames for '" + query + "' " + searchmsg;
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(searchmsg + " ...");

        clearList();

        if (createHistogram) {
            hist = new int[xSize][ySize];
        }

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                // System.out.println("Unit " + x + "_" + y);//DEBUG
                if (createHistogram) {
                    hist[x][y] = 0;
                }

                if (units[x][y] != null) {// check for MnemonicSOM
                    mappedNames = units[x][y].getMappedInputNames();
                    if (mappedNames != null) {
                        for (String mappedName : mappedNames) {
                            if (mappedName.indexOf(query) != -1) {
                                boolean add = true;
                                if (ind_greater > -1 || ind_smaller > -1) {
                                    add = compareOrderIndex(mappedName, ind_greater, ind_smaller);
                                }

                                if (add) {
                                    // add to playlist
                                    addToList(mappedName, units[x][y]);
                                    nfound++;
                                    if (createHistogram) {
                                        hist[x][y] += 1;
                                    } else {
                                        markUnit(x, y);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        enableButtons((nfound > 0)); // enable buttons if items were found
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(searchmsg + ": " + nfound + " found.");

        if (createHistogram) {
            state.mapPNode.showHistogramOverlayVisualization(hist, rbSmoothed.isSelected() ? 1 : 0); // 2nd param is
            // visualization
            // index
        }
    }

    /**
     * show RhythmPattern(s) of weight vector(s) of currently selected unit(s)
     */
    public void showRhythmPattern(double[] vector, String dispname) {
        // TODO solve this: xdim and ydim again have to be guessed without inputvector file or $DATA_TYPE

        InputData inputVectors;
        inputVectors = state.inputDataObjects.getInputData();

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

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(state.controlElementsWidth, 250);
    }

}
