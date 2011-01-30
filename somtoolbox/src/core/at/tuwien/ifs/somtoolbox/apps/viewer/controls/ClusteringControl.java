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

import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.LabelXmlUtils;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.layers.quality.EntropyAndPurityCalculator;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.UiUtils;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.clustering.*;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PObjectOutputStream;
import org.apache.commons.collections15.PredicateUtils;
import org.apache.commons.collections15.Transformer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The control panel for the clustering functionality.
 * 
 * @author Angela Roiger
 * @author Rudolf Mayer
 * @version $Id: ClusteringControl.java 3970 2010-12-15 13:17:59Z mayer $
 */
public class ClusteringControl extends AbstractViewerControl {
    private static final long serialVersionUID = 1L;

    private static final FileNameExtensionFilter clusteringFilter = new FileNameExtensionFilter(
            "Clustering and Labels (*.clustering)", "clustering");

    private static final FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("Labels as xml (*.xml)", "xml");

    private JSpinner spinnerNoCluster;

    private JSpinner labelSpinner;

    private GridBagConstraintsIFS c = new GridBagConstraintsIFS(GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH).setWeights(
            1, 1);

    private int numClusters = 1;

    private SOMPane mapPane;

    private JCheckBox colorCluster;

    private JCheckBox showValues;

    private JSlider valueQe;

    private JCheckBox sticky = new JCheckBox("fix", false);;

    private JPanel kmeansInitialisationPanel = new JPanel();

    private boolean st = false;

    private int numLabels = 0;

    private int maxCluster;

    private JButton buttonColour;

    private JButton qualityMeasureButton;

    private JLabel entropyLabel;

    private JLabel purityLabel;

    private JPanel dendogramPanel;

    public ClusteringControl(String title, CommonSOMViewerStateData state, SOMPane mappane) {
        super(title, state, new GridBagLayout());
        this.mapPane = mappane;
        init();
        updateControlDisplay();
    }



    public void init() {

        maxCluster = state.growingLayer.getXSize() * state.growingLayer.getYSize();

        spinnerNoCluster = new JSpinner(new SpinnerNumberModel(1, 1, maxCluster, 1));
        spinnerNoCluster.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                numClusters = (Integer) ((JSpinner) e.getSource()).getValue();
                SortedMap<Integer, ClusterElementsStorage> m = mapPane.getMap().getCurrentClusteringTree().getAllClusteringElements();
                if (m.containsKey(numClusters)) {
                    st = m.get(numClusters).sticky;
                } else {
                    st = false;
                }
                sticky.setSelected(st);
                redrawClustering();
            }
        });

        JPanel clusterPanel = UiUtils.makeBorderedPanel(new FlowLayout(FlowLayout.LEFT, 10, 0), "Clusters");

        sticky.setToolTipText("Marks this number of clusters as sticky for a certain leve; the next set of clusters will have a smaller boundary");
        sticky.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                st = sticky.isSelected();
                SortedMap<Integer, ClusterElementsStorage> m = mapPane.getMap().getCurrentClusteringTree().getAllClusteringElements();
                if (m.containsKey(numClusters)) {
                    ClusterElementsStorage c = m.get(numClusters);
                    c.sticky = st;
                    // System.out.println("test");
                    // ((ClusterElementsStorageNode)m.get(numClusters)).sticky = st;
                    redrawClustering();
                }
            }
        });

        colorCluster = new JCheckBox("colour", state.colorClusters);
        colorCluster.setToolTipText("Fill the clusters in colours");
        colorCluster.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.colorClusters = colorCluster.isSelected();
                // TODO: Palette anzeigen (?)
                redrawClustering();
            }
        });

        UiUtils.fillPanel(clusterPanel, new JLabel("#"), spinnerNoCluster, sticky, colorCluster);
        getContentPane().add(clusterPanel, c.nextRow());

        dendogramPanel = UiUtils.makeBorderedPanel(new GridLayout(0, 1), "Dendogram");
        getContentPane().add(dendogramPanel, c.nextRow());

        JPanel numLabelPanel = UiUtils.makeBorderedPanel(new GridBagLayout(), "Labels");
        GridBagConstraintsIFS gcLabels = new GridBagConstraintsIFS();

        labelSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        labelSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                numLabels = (Integer) ((JSpinner) e.getSource()).getValue();
                state.clusterWithLabels = numLabels;
                redrawClustering();
            }
        });

        this.showValues = new JCheckBox("values", state.labelsWithValues);
        this.showValues.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.labelsWithValues = showValues.isSelected();
                redrawClustering();
            }
        });

        int start = new Double((1 - state.clusterByValue) * 100).intValue();
        valueQe = new JSlider(0, 100, start);
        valueQe.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int byValue = ((JSlider) e.getSource()).getValue();
                state.clusterByValue = 1 - byValue / 100;
                redrawClustering();
            }
        });

        numLabelPanel.add(new JLabel("# Labels"), gcLabels);
        numLabelPanel.add(labelSpinner, gcLabels.nextCol());
        numLabelPanel.add(showValues, gcLabels.nextCol());
        numLabelPanel.add(valueQe, gcLabels.nextRow().setGridWidth(3).setFill(GridBagConstraints.HORIZONTAL));
        getContentPane().add(numLabelPanel, c.nextRow());

        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(0, new JLabel("by Value"));
        labelTable.put(100, new JLabel("by Qe"));
        valueQe.setToolTipText("Method how to select representative labels - by QE, or the attribute values");
        valueQe.setLabelTable(labelTable);
        valueQe.setPaintLabels(true);

        final JComboBox initialisationBox = new JComboBox(KMeans.InitType.values());
        initialisationBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = mapPane.getMap().getClusteringTreeBuilder();
                if (o instanceof KMeansTreeBuilder) {
                    ((KMeansTreeBuilder) o).reInit((KMeans.InitType) initialisationBox.getSelectedItem());
                    // FIXME: is this call needed?
                    mapPane.getMap().getCurrentClusteringTree().getAllClusteringElements();
                    redrawClustering();
                }
            }
        });

        getContentPane().add(
                UiUtils.fillPanel(kmeansInitialisationPanel, new JLabel("k-Means initialisation"), initialisationBox),
                c.nextRow());

        JPanel borderPanel = new TitledCollapsiblePanel("Border", new GridLayout(1, 4), true);

        JSpinner borderSpinner = new JSpinner(new SpinnerNumberModel(
                ClusteringTree.INITIAL_BORDER_WIDTH_MAGNIFICATION_FACTOR, 0.1, 5, 0.1));
        borderSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                state.clusterBorderWidthMagnificationFactor = ((Double) ((JSpinner) e.getSource()).getValue()).floatValue();
                redrawClustering();
            }
        });

        borderPanel.add(new JLabel("width"));
        borderPanel.add(borderSpinner);

        borderPanel.add(new JLabel("colour"));
        buttonColour = new JButton("");
        buttonColour.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ClusterBoderColorChooser(state.parentFrame, state.clusterBorderColour, ClusteringControl.this);
            }
        });
        buttonColour.setBackground(state.clusterBorderColour);
        borderPanel.add(buttonColour);

        getContentPane().add(borderPanel, c.nextRow());

        JPanel evaluationPanel = new TitledCollapsiblePanel("Cluster Evaluation", new GridLayout(3, 2), true);

        evaluationPanel.add(new JLabel("quality measure"));
        qualityMeasureButton = new JButton();
        qualityMeasureButton.setText("ent/pur calc");
        qualityMeasureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("doing entropy here");
                ClusteringTree clusteringTree = state.mapPNode.getClusteringTree();
                if (clusteringTree == null) {
                    // we have not clustered yet
                    return;
                }
                // System.out.println(clusteringTree.getChildrenCount());

                // FIXME put this in a method and call it on numcluster.change()
                SOMLibClassInformation classInfo = state.inputDataObjects.getClassInfo();
                ArrayList<ClusterNode> clusters = clusteringTree.getNodesAtLevel(numClusters);
                System.out.println(clusters.size());
                // EntropyMeasure.computeEntropy(clusters, classInfo);
                EntropyAndPurityCalculator eapc = new EntropyAndPurityCalculator(clusters, classInfo);
                // FIXME round first
                entropyLabel.setText(String.valueOf(eapc.getEntropy()));
                purityLabel.setText(String.valueOf(eapc.getPurity()));

            }
        });
        evaluationPanel.add(qualityMeasureButton);
        GridBagConstraintsIFS gcEval = new GridBagConstraintsIFS();

        evaluationPanel.add(new JLabel("entropy"), gcEval);
        evaluationPanel.add(new JLabel("purity"), gcEval.nextCol());

        entropyLabel = new JLabel("n/a");
        purityLabel = new JLabel("n/a");

        evaluationPanel.add(entropyLabel, gcEval.nextRow());
        evaluationPanel.add(purityLabel, gcEval.nextCol());

        getContentPane().add(evaluationPanel, c.nextRow());

        JPanel panelButtons = new JPanel(new GridLayout(1, 4));

        JButton saveButton = new JButton("Save");
        saveButton.setFont(smallFont);
        saveButton.setMargin(SMALL_INSETS);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser;
                if (state.fileChooser.getSelectedFile() != null) {
                    fileChooser = new JFileChooser(state.fileChooser.getSelectedFile().getPath());
                } else {
                    fileChooser = new JFileChooser();
                }
                fileChooser.addChoosableFileFilter(clusteringFilter);
                fileChooser.addChoosableFileFilter(xmlFilter);

                File filePath = ExportUtils.getFilePath(ClusteringControl.this, fileChooser,
                        "Save Clustering and Labels");

                if (filePath != null) {
                    if (xmlFilter.accept(filePath)) {
                        LabelXmlUtils.saveLabelsToFile(state.mapPNode, filePath);
                    } else {
                        try {
                            FileOutputStream fos = new FileOutputStream(filePath);
                            GZIPOutputStream gzipOs = new GZIPOutputStream(fos);
                            PObjectOutputStream oos = new PObjectOutputStream(gzipOs);

                            oos.writeObjectTree(mapPane.getMap().getCurrentClusteringTree());
                            oos.writeObjectTree(mapPane.getMap().getManualLabels());
                            oos.writeInt(state.clusterWithLabels);
                            oos.writeBoolean(state.labelsWithValues);
                            oos.writeDouble(state.clusterByValue);
                            oos.writeObject(spinnerNoCluster.getValue());

                            oos.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    // keep the selected path for future references
                    state.fileChooser.setSelectedFile(filePath.getParentFile());
                }
            }
        });
        panelButtons.add(saveButton);

        JButton loadButton = new JButton("Load");
        loadButton.setFont(smallFont);
        loadButton.setMargin(SMALL_INSETS);
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = getFileChooser();
                fileChooser.setName("Open Clustering");
                fileChooser.addChoosableFileFilter(xmlFilter);
                fileChooser.addChoosableFileFilter(clusteringFilter);

                int returnVal = fileChooser.showDialog(ClusteringControl.this, "Open");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File filePath = fileChooser.getSelectedFile();
                    if (xmlFilter.accept(filePath)) {
                        PNode restoredLabels = null;
                        try {
                            restoredLabels = LabelXmlUtils.restoreLabelsFromFile(fileChooser.getSelectedFile());
                            PNode manual = state.mapPNode.getManualLabels();

                            ArrayList<PNode> tmp = new ArrayList<PNode>();
                            for (ListIterator<?> iter = restoredLabels.getChildrenIterator(); iter.hasNext();) {
                                PNode element = (PNode) iter.next();
                                tmp.add(element);
                            }
                            manual.addChildren(tmp);
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Successfully loaded cluster labels.");
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                                    "Error loading cluster labels: " + e1.getMessage());
                        }

                    } else {

                        try {
                            FileInputStream fis = new FileInputStream(filePath);
                            GZIPInputStream gzipIs = new GZIPInputStream(fis);
                            ObjectInputStream ois = new ObjectInputStream(gzipIs);
                            ClusteringTree tree = (ClusteringTree) ois.readObject();

                            PNode manual = (PNode) ois.readObject();
                            PNode all = mapPane.getMap().getManualLabels();
                            ArrayList<PNode> tmp = new ArrayList<PNode>();
                            for (ListIterator<?> iter = manual.getChildrenIterator(); iter.hasNext();) {
                                PNode element = (PNode) iter.next();
                                tmp.add(element);
                            }
                            all.addChildren(tmp);

                            state.clusterWithLabels = ois.readInt();
                            labelSpinner.setValue(state.clusterWithLabels);
                            state.labelsWithValues = ois.readBoolean();
                            showValues.setSelected(state.labelsWithValues);
                            state.clusterByValue = ois.readDouble();
                            valueQe.setValue(new Double((1 - state.clusterByValue) * 100).intValue());

                            mapPane.getMap().buildTree(tree);
                            spinnerNoCluster.setValue(ois.readObject());

                            ois.close();
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Successfully loaded clustering.");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                                    "Error loading clustering: " + ex.getMessage());
                        }

                    }
                    // keep the selected path for future references
                    state.fileChooser.setSelectedFile(filePath.getParentFile());
                }
            }

        });
        panelButtons.add(loadButton);

        JButton exportImages = new JButton("Export");
        exportImages.setFont(smallFont);
        exportImages.setMargin(SMALL_INSETS);
        exportImages.setToolTipText("Export labels as images (not yet working)");
        exportImages.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (CommonSOMViewerStateData.fileNamePrefix != null
                        && !CommonSOMViewerStateData.fileNamePrefix.equals("")) {
                    fileChooser.setCurrentDirectory(new File(CommonSOMViewerStateData.fileNamePrefix));
                } else {
                    fileChooser.setCurrentDirectory(state.getFileChooser().getCurrentDirectory());
                }
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                    fileChooser.setSelectedFile(null);
                }
                fileChooser.setName("Choose path");
                int returnVal = fileChooser.showDialog(ClusteringControl.this, "Choose path");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // save images
                    String path = fileChooser.getSelectedFile().getAbsolutePath();
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing label images to " + path);
                }
            }
        });
        panelButtons.add(exportImages);

        JButton deleteManual = new JButton("Delete");
        deleteManual.setFont(smallFont);
        deleteManual.setMargin(SMALL_INSETS);
        deleteManual.setToolTipText("Delete manually added labels.");
        deleteManual.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.mapPNode.getManualLabels().removeAllChildren();
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Manual Labels deleted.");
            }
        });
        panelButtons.add(deleteManual);
        c.anchor = GridBagConstraints.NORTH;
        this.getContentPane().add(panelButtons, c.nextRow());

        this.setVisible(true);
    }

    // update allClusteringElements of the current clustering tree and show them
    private void redrawClustering() {
        // BasicStroke bs = new BasicStroke(12.0f);
        this.mapPane.getState().numClusters = numClusters;
        this.mapPane.getMap().showClusters(this.numClusters, sticky.isSelected());
    }

    private JFileChooser getFileChooser() {
        JFileChooser fileChooser;
        if (state.fileChooser.getSelectedFile() != null) {
            fileChooser = new JFileChooser(state.fileChooser.getSelectedFile().getPath());
        } else {
            fileChooser = new JFileChooser();
        }
        return fileChooser;
    }

    public void updateClusterColourSelection(Color colour) {
        buttonColour.setBackground(colour);
        state.clusterBorderColour = colour;
        redrawClustering();
    }

    /** Makes sure all controls are displayed as needed. Currently deals with the {@link #kmeansInitialisationPanel} */
    public void updateControlDisplay() {
        kmeansInitialisationPanel.setVisible(mapPane.getMap().getCurrentClusteringTree() != null
                && mapPane.getMap().getClusteringTreeBuilder() instanceof KMeansTreeBuilder);

        ClusteringTree clusteringTree = mapPane.getMap().getCurrentClusteringTree();

        if(clusteringTree == null) return;

        Tree<ClusterNode, Integer> clusterTree = clusteringTree.getJUNGTree();
        TreeLayout<ClusterNode, Integer> clusterLayout = new TreeLayout<ClusterNode, Integer>(clusterTree, 30, 100);

        Comparator<ClusterNode> mergeCostComparator = new Comparator<ClusterNode>() {
            @Override
            public int compare(ClusterNode o1, ClusterNode o2) {
                return Double.compare(o1.getMergeCost(), o2.getMergeCost());
            }
        };

        final double maxMergeCost = Collections.max(clusterTree.getVertices(), mergeCostComparator).getMergeCost();
        final double minMergeCost = Collections.min(clusterTree.getVertices(), mergeCostComparator).getMergeCost();

        final Palette palette = mapPane.getState().getSOMViewer().getCurrentlySelectedPalette();

        final VisualizationViewer<ClusterNode, Integer> vv = new VisualizationViewer<ClusterNode, Integer>(clusterLayout);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<ClusterNode, Integer>());
        vv.getRenderContext().setEdgeStrokeTransformer(new Transformer<Integer, Stroke>() {
            @Override
            public Stroke transform(Integer integer) {
                return new BasicStroke(1.0f);
            }
        });
        vv.getRenderContext().setEdgeArrowPredicate(
                PredicateUtils.<Context<Graph<ClusterNode, Integer>, Integer>>falsePredicate());
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<ClusterNode, String>() {
            @Override
            public String transform(ClusterNode clusterNode) {
                Point2D.Double centroid = clusterNode.getCentroid();
                return String.format("%d @ (%f, %f)", clusterNode.getNodes().length, centroid.getX(), centroid.getY());
            }
        });
        vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<ClusterNode, Paint>() {
            @Override
            public Paint transform(ClusterNode clusterNode) {
                double pos = clusterNode.getMergeCost() - minMergeCost;
                pos /= maxMergeCost - minMergeCost;
                pos *= palette.getNumberOfColours();

                return palette.getColor((int)pos);
            }
        });

        GraphZoomScrollPane vv2 = new GraphZoomScrollPane(vv);

        vv2.setPreferredSize(new Dimension(dendogramPanel.getParent().getWidth(), 200));
        vv2.setVisible(true);

        DefaultModalGraphMouse<ClusterNode, Integer> graphMouse = new DefaultModalGraphMouse<ClusterNode, Integer>();
        vv.setGraphMouse(graphMouse);
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

        vv.addGraphMouseListener(new GraphMouseListener<ClusterNode>() {
            @Override
            public void graphClicked(ClusterNode clusterNode, MouseEvent me) {
                numClusters = clusterNode.getLevel();
                redrawClustering();
            }

            @Override
            public void graphPressed(ClusterNode clusterNode, MouseEvent me) {
            }

            @Override
            public void graphReleased(ClusterNode clusterNode, MouseEvent me) {
            }
        });

        dendogramPanel.removeAll();
        dendogramPanel.add(vv2);

        getContentPane().validate();
    }

}
