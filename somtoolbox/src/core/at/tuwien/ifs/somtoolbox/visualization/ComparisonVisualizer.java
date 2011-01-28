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
package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.visualization.clustering.LabelCoordinates;
import at.tuwien.ifs.somtoolbox.visualization.comparison.SOMComparison;

/**
 * @author Doris Baum
 * @version $Id: ComparisonVisualizer.java 3883 2010-11-02 17:13:23Z frank $
 */

public class ComparisonVisualizer extends AbstractMatrixVisualizer implements BackgroundImageVisualizer { // ,
    // ListSelectionListener
    // {

    private DefaultListModel soms = new DefaultListModel();

    private double[][] meanDistances = null;

    private double[][] varDistances = null;

    private boolean storeValid = false;

    private int oldindex = -1;

    private double threshold = 0;

    private int clusterNo = 5;

    private TreeMap<String, double[][]> clusterDistances = null;

    private final int MEAN = 0;

    private final int VAR = 1;

    private final int CLUSTER = 2;

    private final int CLUSTERVAR = 3;

    private final double MAX_DISTANCE_THRESHOLD = 100;

    private final int MAX_CLUSTER_NO = 100;

    public ComparisonVisualizer() {

        NUM_VISUALIZATIONS = 4;
        VISUALIZATION_NAMES = new String[] { "Comparison - Mean", "Comparison - Variance", "Comparison - Cluster mean",
                "Comparison - Cluster variance" };
        VISUALIZATION_SHORT_NAMES = new String[] { "ComparisonMean", "ComparisonVar", "ComparisonClusterMean",
                "ComparisonClustVar" };
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Comparison between two or more SOMs - calculates the mean euclidean distance in the compared SOMs\n"
                        + "between data vectors which lie on the same unit in the displayed SOM.",
                "Comparison between two or more SOMs - calculates the variance of the euclidean distances in the compared SOMs\n"
                        + " between data vectors which lie on the same unit in the displayed SOM.",
                "Comparison between two or more SOMs - calculates the mean cluster distance in the compared SOMs\n"
                        + " between data vectors which lie on the same unit in the displayed SOM.",
                "Comparison between two or more SOMs - calculates the cluster distance variance in the compared SOMs\n"
                        + " between data vectors which lie on the same unit in the displayed SOM." };

        // don't initialise the control panel if we have no graphics environment (e.g. in server applications)
        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new ComparisonControlPanel(this);
        }
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();
        drawBackground(width, height, g);

        // if we have no data, just make white map
        if (soms == null || soms.size() == 0) {
            g.setPaint(Color.white);
            g.fill(new Rectangle(0, 0, width, height));
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "No SOMs loaded, thus not displaying any visualisation; add some SOMs and hit recalculate to generate matrices");
            // else do full blown visualisation
        } else {

            Object[] dummy = soms.toArray();
            String[] prefixes = new String[dummy.length];
            for (int i = 0; i < dummy.length; i++) {
                prefixes[i] = (String) dummy[i];
            }

            // TODO ueberpruefung ob korrekte SOM description files? wann und wo und wie moeglichst schnell, ohne die
            // SOMs komplett zu laden?

            // TODO frage: warum wird initguielements nicht zuende ausgefuehrt, wenn createvis unterbrochen wird

            if ((oldindex == MEAN || oldindex == VAR) && (index == CLUSTER || index == CLUSTERVAR)
                    || (oldindex == CLUSTER || oldindex == CLUSTERVAR) && (index == MEAN || index == VAR)) {
                storeValid = false;
            }
            oldindex = index;

            // have a sort of cache for the mean distances; if only the palette was changed, don't recalculate
            // everything
            if (!storeValid) {
                this.calculateMeanVarDistance(gsom, prefixes, index, true);
                storeValid = true;
            }

            int xSize = gsom.getLayer().getXSize();
            int ySize = gsom.getLayer().getYSize();

            int unitWidth = width / xSize;
            int unitHeight = height / ySize;

            int ci = 0;
            for (int y = 0; y < ySize; y++) {
                for (int x = 0; x < xSize; x++) {
                    if (index == MEAN || index == CLUSTER) {
                        ci = (int) Math.round((1 - meanDistances[x][y]) * palette.maxColourIndex());
                    } else if (index == VAR || index == CLUSTERVAR) {
                        ci = (int) Math.round((1 - varDistances[x][y]) * palette.maxColourIndex());
                    }
                    g.setPaint(palette.getColor(ci));
                    g.fill(new Rectangle(x * unitWidth, y * unitHeight, unitWidth, unitHeight));
                }
            }
        }

        return res;
    }

    public void calculateMeanVarDistance(GrowingSOM gsom, String[] prefixes, int index, boolean normalized)
            throws SOMToolboxException {
        GrowingLayer layer = gsom.getLayer();
        int xSize = layer.getXSize();
        int ySize = layer.getYSize();

        String[] labelList = gsom.getLayer().getAllMappedDataNames(true);

        // create and initialise variable for the mean distances
        double[][] meandist = new double[xSize][ySize];
        for (int a = 0; a < xSize; a++) {
            Arrays.fill(meandist[a], 0.0);
        }
        // create and initialise variable for the mean distances
        double[][] vardist = new double[xSize][ySize];
        for (int a = 0; a < xSize; a++) {
            Arrays.fill(vardist[a], 0.0);
        }

        // init hashmap for cluster distance arrays
        clusterDistances = new TreeMap<String, double[][]>();

        // go through all the SOMs that should be compared with the main one
        for (String prefixe : prefixes) {

            // load second SOM to compare with
            GrowingSOM secondSOM = SOMComparison.loadGSOM(prefixe);

            // if the vector labels aren't the same in both SOMs, then throw exeption
            if (!Arrays.equals(labelList, secondSOM.getLayer().getAllMappedDataNames(true))) {
                soms.removeElement(prefixe);
                SOMComparison.printInputDifferenceErrorMesage(labelList, secondSOM.getLayer().getAllMappedDataNames(
                        true));
                throw new SOMToolboxException(
                        "The input vector sets of the SOMs aren't equal - can't do comparison! See the logs for input vector differences.");
            }

            // calculate the distances between the vectors in the second SOM
            double[][] dist2 = null;
            if (index == MEAN || index == VAR) {
                dist2 = SOMComparison.calculcateIntraSOMDistanceMatrix(SOMComparison.getLabelCoordinates(secondSOM));
            } else if (index == CLUSTER || index == CLUSTERVAR) {
                int[][] assignment2 = SOMComparison.calculateClusterAssignment(secondSOM, clusterNo);
                LabelCoordinates[] coords2 = SOMComparison.getLabelCoordinates(secondSOM);

                double[][] distances = SOMComparison.calculateClusterDistances(assignment2, clusterNo);
                clusterDistances.put(prefixe, distances);

                dist2 = SOMComparison.calculcateIntraSOMClusterDistanceMatrix(coords2, assignment2, clusterNo,
                        distances);
            } else {
                throw new SOMToolboxException("Invalid visualisation index: " + index);
            }

            // go through all units in the first SOM's layer...
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    // ... for each unit, get the labels of the vectors mapped to them
                    String[] unitnames = layer.getUnit(x, y).getMappedInputNames();

                    // ... and calculate the mean pairwise distance in SOM2 for those vectors
                    double currentDistance = 0;
                    double currentVar = 0;
                    int distanceCounts = 0;

                    if (unitnames != null) {
                        // compare each vector with all following vectors on the unit...
                        for (int n = 0; n < unitnames.length; n++) {
                            int indexa = Arrays.binarySearch(labelList, unitnames[n]);
                            for (int m = n + 1; m < unitnames.length; m++) {
                                int indexb = Arrays.binarySearch(labelList, unitnames[m]);
                                if (dist2[indexa][indexb] > threshold) {
                                    currentDistance += dist2[indexa][indexb];
                                    currentVar += dist2[indexa][indexb] * dist2[indexa][indexb];
                                }
                                distanceCounts++;
                            }
                        }
                    }
                    if (distanceCounts != 0) {
                        currentDistance = currentDistance / distanceCounts;
                        currentVar = currentVar / distanceCounts;
                    }
                    meandist[x][y] += currentDistance;
                    vardist[x][y] += currentVar;
                }
            }

        }

        // divide cumulated distance by number of SOMs to compare, to really get mean distance
        // also find maximum of the mean distances
        double maxDist = 0;
        double maxVar = 0;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                meandist[x][y] = meandist[x][y] / prefixes.length;
                // calculate variance: Var(x) = E(X^2) - (E(X))^2
                vardist[x][y] = vardist[x][y] / prefixes.length - meandist[x][y] * meandist[x][y];
                if (meandist[x][y] > maxDist) {
                    maxDist = meandist[x][y];
                }
                if (vardist[x][y] > maxVar) {
                    maxVar = vardist[x][y];
                }
            }
        }

        // if normalization is required, normalize to maximum distance
        if (normalized == true) {
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    meandist[x][y] = meandist[x][y] / maxDist;
                    vardist[x][y] = vardist[x][y] / maxVar;
                }
            }
        }

        if (clusterDistances.size() != 0) {
            ((ComparisonControlPanel) controlPanel).btShowDistances.setEnabled(true);
        }

        meanDistances = meandist;
        varDistances = vardist;
    }

    public void addSOM(String fileName) {
        String prefix = FileUtils.extractSOMLibInputPrefix(fileName);
        if (!soms.contains(prefix)) {
            soms.addElement(prefix);
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Not adding already existing SOM '" + prefix + "' (extracted from '" + fileName + "')");
        }
    }

    private class ComparisonControlPanel extends VisualizationControlPanel implements ActionListener {
        private static final long serialVersionUID = 1L;

        private JButton btAddSOMs = null;

        private JButton btRemove = null;

        private JButton btReCalculate = null;

        protected JButton btShowDistances = null;

        private JSpinner spThreshold = null;

        private JLabel lThreshold = null;

        private JList somlist = null;

        private JSpinner spClusterNo = null;

        private JLabel lClusterNo = null;

        private final String ADD_SOMS = "addSOMs";

        private final String REMOVE = "remove";

        private final String RECALCULATE = "reCalculate";

        private final String SHOW_DIST = "showDistances";

        final static String addToolTip = "Add SOMs to the comparison; it is sufficient to add one description file per SOM, "
                + "the other two description files with the same prefix will automatically be found";

        final static String showDistancesToolTip = "Shows cluster distance matrices for the SOMs in the list in an extra window.";

        final static String thresholdToolTip = "Distance threshold: if the distance between two vectors is less than or equal the distance threshold,  "
                + "don't take it into account for this visualization.";

        final static String clusterNoToolTip = "Number of clusters  to generate (using Ward's linkage);  "
                + "only relevant for Cluster mean and Cluster variance visualisations.";

        final static String removeToolTip = "Remove SOMs from the list.";

        final static String recalculateToolTip = "Recalculate the visualisation; hit this button to see the changes from adding or remove SOMs ;-)";

        private ComparisonControlPanel(ComparisonVisualizer comp) {
            super("SOM Comparison Control");
            this.initGUIElements(comp);
            setVisible(true);
        }

        private void initGUIElements(ComparisonVisualizer comp) {
            setName("Comparison Control");

            JPanel comparisonPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.fill = GridBagConstraints.HORIZONTAL;

            int gridy = 2;

            spThreshold = new JSpinner(new SpinnerNumberModel(comp.threshold, 0, comp.MAX_DISTANCE_THRESHOLD, 0.5));
            spThreshold.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSpinner src = (JSpinner) e.getSource();
                    threshold = ((Double) src.getValue()).doubleValue();
                }
            });
            lThreshold = new JLabel("Dist. threshold:");
            spThreshold.setToolTipText(thresholdToolTip);
            lThreshold.setToolTipText(thresholdToolTip);

            constr.gridx = 0;
            constr.gridy = gridy;
            comparisonPanel.add(lThreshold, constr);
            constr.gridx = 1;
            constr.gridy = gridy;
            constr.anchor = GridBagConstraints.NORTHEAST;
            comparisonPanel.add(spThreshold, constr);

            gridy++;

            spClusterNo = new JSpinner(new SpinnerNumberModel(comp.clusterNo, 0, comp.MAX_CLUSTER_NO, 1));
            spClusterNo.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSpinner src = (JSpinner) e.getSource();
                    clusterNo = ((Integer) src.getValue()).intValue();
                }
            });
            lClusterNo = new JLabel("# clusters:");
            spClusterNo.setToolTipText(clusterNoToolTip);
            lClusterNo.setToolTipText(clusterNoToolTip);

            constr.gridx = 0;
            constr.gridy = gridy;
            comparisonPanel.add(lClusterNo, constr);
            constr.gridx = 1;
            constr.gridy = gridy;
            constr.anchor = GridBagConstraints.NORTHEAST;
            comparisonPanel.add(spClusterNo, constr);

            gridy++;
            constr.fill = GridBagConstraints.BOTH;

            btAddSOMs = new JButton("Add SOMs");
            btAddSOMs.setFont(smallerFont);
            btAddSOMs.setActionCommand(ADD_SOMS);
            btAddSOMs.addActionListener(this);
            btAddSOMs.setToolTipText(addToolTip);
            constr.gridx = 0;
            constr.gridy = gridy;
            comparisonPanel.add(btAddSOMs, constr);

            btRemove = new JButton("Remove");
            btRemove.setFont(smallerFont);
            btRemove.setActionCommand(REMOVE);
            btRemove.addActionListener(this);
            btRemove.setToolTipText(removeToolTip);
            constr.gridx = 1;
            constr.gridy = gridy;
            comparisonPanel.add(btRemove, constr);

            gridy++;

            btReCalculate = new JButton("Recalculate");
            btReCalculate.setFont(smallerFont);
            btReCalculate.setActionCommand(RECALCULATE);
            btReCalculate.addActionListener(this);
            btReCalculate.setToolTipText(recalculateToolTip);
            constr.gridx = 0;
            constr.gridy = gridy;
            comparisonPanel.add(btReCalculate, constr);

            btShowDistances = new JButton("Cluster distances");
            btShowDistances.setFont(smallerFont);
            btShowDistances.setActionCommand(SHOW_DIST);
            btShowDistances.addActionListener(this);
            btShowDistances.setToolTipText(showDistancesToolTip);
            btShowDistances.setEnabled(false);
            constr.gridx = 1;
            constr.gridy = gridy;
            comparisonPanel.add(btShowDistances, constr);

            soms = new DefaultListModel();
            somlist = new JList(soms);
            somlist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            // somlist.addListSelectionListener(comp);
            somlist.setVisibleRowCount(10);
            JScrollPane listScroller = new JScrollPane(somlist);
            listScroller.setPreferredSize(new Dimension(50, 50));

            constr.fill = GridBagConstraints.BOTH;
            gridy++;
            constr.gridx = 0;
            constr.gridy = gridy;
            constr.gridwidth = 2;
            constr.weightx = 0.5;
            constr.weighty = 1.0;
            comparisonPanel.add(listScroller, constr);

            this.add(comparisonPanel, c);

        }

        protected void addSOMs() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setCurrentDirectory(map.getState().getFileChooser().getCurrentDirectory());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.addChoosableFileFilter(new FileUtils.SOMDescriptionFileFilter());

            if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                fileChooser = new JFileChooser(fileChooser.getSelectedFile().getPath());
            }
            fileChooser.setName("Choose SOMs to compare");
            int returnVal = fileChooser.showDialog(this.getParent(), "Choose SOMs");

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                for (File selectedFile : selectedFiles) {
                    addSOM(selectedFile.getPath() + selectedFile.getName());
                }
            }
        }

        private void recalculate() {
            storeValid = false;
            for (String visualizationName : VISUALIZATION_SHORT_NAMES) {
                invalidateCache(visualizationName);
            }
            if (visualizationUpdateListener != null) {
                visualizationUpdateListener.updateVisualization();
            }
        }

        private void remove() {
            int[] deletedIndices = somlist.getSelectedIndices();

            if (deletedIndices != null) {
                for (int i = deletedIndices.length - 1; i >= 0; i--) {
                    soms.removeElementAt(deletedIndices[i]);
                }
            }

            if (soms == null || soms.size() == 0) {
                btShowDistances.setEnabled(false);
            }
        }

        private void showDist() {
            if (clusterDistances == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No cluster distance matrices present; add some SOMs and hit recalculate to generate matrices");
            } else {
                final String nl = System.getProperty("line.separator");

                String output = "";
                for (String key : clusterDistances.keySet()) {
                    double[][] value = clusterDistances.get(key);
                    output += "SOM: " + key + nl;
                    output += "Distances: " + nl;
                    for (int i = 0; i < value.length; i++) {
                        for (int j = i + 1; j < value[0].length; j++) {
                            output += "Cluster " + i + " to cluster " + j + ": " + value[i][j] + nl;
                        }
                    }
                    output += nl;
                }

                this.makeDistancesDialog(output);
            }
        }

        private void makeDistancesDialog(String output) {
            final JDialog dialog = new JDialog(map.getState().parentFrame, "Cluster distances");

            JTextArea textArea = new JTextArea(output);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });
            JPanel closePanel = new JPanel();
            closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.LINE_AXIS));
            closePanel.add(Box.createHorizontalGlue());
            closePanel.add(closeButton);
            closePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));

            JPanel contentPane = new JPanel(new BorderLayout());
            contentPane.add(scrollPane, BorderLayout.CENTER);
            contentPane.add(closePanel, BorderLayout.PAGE_END);
            contentPane.setOpaque(true);
            dialog.setContentPane(contentPane);

            dialog.setSize(new Dimension(350, 400));
            dialog.setLocationRelativeTo(map.getState().parentFrame);
            dialog.setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand() == ADD_SOMS) {
                this.addSOMs();
            } else if (e.getActionCommand() == REMOVE) {
                this.remove();
            } else if (e.getActionCommand() == RECALCULATE) {
                this.recalculate();
            } else if (e.getActionCommand() == SHOW_DIST) {
                this.showDist();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(map.getState().controlElementsWidth, 400);
        }

        @Override
        public Dimension getMinimumSize() {
            return this.getPreferredSize();
        }
    }
}
