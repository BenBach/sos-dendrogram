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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.VisualisationUtils;
import at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree.Edge;
import at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree.Graph;
import at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree.InputdataGraph;
import at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree.SomGraph;
import at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree.SomGraph.NeighbourhoodMode;

/**
 * @author Thomas Kern
 * @author Magdalena Widl
 * @author Rudolf Mayer
 * @version $Id: MinimumSpanningTreeVisualizer.java 3862 2010-10-15 09:42:45Z frank $
 */
public class MinimumSpanningTreeVisualizer extends AbstractBackgroundImageVisualizer implements
        BackgroundImageVisualizer {

    //
    // Visualisation parameters
    //    
    private boolean weightLines = false;

    private boolean skipInterpolationUnits = false;

    private NeighbourhoodMode neighbourhoodMode = NeighbourhoodMode.All;

    private int disconnectUnfavoured = 0;

    // END Visualisation parameters

    public MinimumSpanningTreeVisualizer() {
        NUM_VISUALIZATIONS = 3;
        VISUALIZATION_NAMES = new String[] { "Minimum Spanning Tree SOM", "Minimum Spanning Tree Input Data",
                "Minimum Spanning Tree Both" };
        VISUALIZATION_SHORT_NAMES = new String[] { "MSTsom", "MSTdata", "MSTboth" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Implementation of a minimum spanning tree on a SOM",
                "Implementation of a minimum spanning tree on the input data",
                "Implementation of a minimum spanning tree on the SOM and input data, ideal for comparing them" };
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR };

        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new MinimumSpanningTreeControlPanel();
        }
        preferredScaleFactor = getPreferredScaleFactor();
    }

    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        checkVariantIndex(variantIndex, getClass());

        // set maximum value for disconnect spinner, based on the size of the SOM to visualise
        if (controlPanel != null) {
            ((MinimumSpanningTreeControlPanel) controlPanel).disconnectUnfavouredModel.setMaximum(gsom.getLayer().getUnitCount());
        }

        if (drawInputTree(variantIndex)) {
            if (gsom.getSharedInputObjects().getInputData() == null) {
                throw new SOMToolboxException("Input data is needed for this Minimum Spanning Tree!");
            }
        }
        BufferedImage image = new BufferedImage(width, height, Transparency.TRANSLUCENT);

        if (drawSOMTree(variantIndex)) { // draw the SOM tree
            drawMinimumSpanningTree(image, new SomGraph(gsom, skipInterpolationUnits, neighbourhoodMode),
                    disconnectUnfavoured, gsom.getLayer(), Color.BLACK);
        }
        if (drawInputTree(variantIndex)) { // draw the input data tree
            drawMinimumSpanningTree(image, new InputdataGraph(gsom), disconnectUnfavoured, gsom.getLayer(), Color.BLUE);
        }
        return image;
    }

    private boolean drawInputTree(int index) {
        return index == 1 || index == 2;
    }

    private boolean drawSOMTree(int index) {
        return index == 0 || index == 2;
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        if (drawSOMTree(index)) {// draw the SOM tree
            return appendToCacheKey(gsom, index, width, height, "weighting:" + weightLines, "disconnect:"
                    + disconnectUnfavoured, "skipInterpolation:" + skipInterpolationUnits, "neighbourhood:"
                    + neighbourhoodMode.toString());
        } else {
            return appendToCacheKey(gsom, index, width, height, "weighting:" + weightLines, "disconnect:"
                    + disconnectUnfavoured);
        }
    }

    private void drawMinimumSpanningTree(BufferedImage res, Graph graph, int disconnectUnfavoured, GrowingLayer layer,
            Color color) {
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = res.getWidth() / layer.getXSize();
        int unitHeight = res.getHeight() / layer.getYSize();

        // draw the line & circle approx. 1/20 of the unitWidth
        int lineWidth = Math.round(unitWidth / 5);
        int lineHeight = Math.round(unitHeight / 5);

        // draw the edges
        g.setPaint(color);

        List<Edge> mst = graph.getMinimumSpanningTree();
        for (int i = 0; i < mst.size() && i + disconnectUnfavoured < mst.size(); i++) {
            Edge e = mst.get(i);
            graph.drawLine(g, unitWidth, unitHeight, e, weightLines);
        }

        // draw the nodes
        g.setPaint(Color.RED);
        for (Edge e : mst) {
            VisualisationUtils.drawUnitCentreMarker(g, e.getStart().getUnit(), unitWidth, unitHeight, lineWidth,
                    lineHeight); // starting vertex
            VisualisationUtils.drawUnitCentreMarker(g, e.getEnd().getUnit(), unitWidth, unitHeight, lineWidth,
                    lineHeight); // end vertex
        }
    }

    private class MinimumSpanningTreeControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        private JCheckBox weightLinesCheckbox = new JCheckBox("Weight lines", weightLines);

        private JCheckBox skipInterpolationUnitsCheckbox = new JCheckBox("Skip interpol. Units", skipInterpolationUnits);

        private JComboBox neighbourhoodModeBox = new JComboBox(NeighbourhoodMode.values());

        private SpinnerNumberModel disconnectUnfavouredModel = new SpinnerNumberModel(disconnectUnfavoured, 0, 100, 1);

        private JSpinner disconnectUnfavouredSpinner = new JSpinner(disconnectUnfavouredModel);

        public MinimumSpanningTreeControlPanel() {
            super("MinimumSpanningTree Control");
            c.insets = new Insets(1, 4, 1, 4);

            weightLinesCheckbox.setToolTipText("Weight the lines of the MST by the relative distance between the nodes.");
            weightLinesCheckbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    weightLines = weightLinesCheckbox.isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            add(weightLinesCheckbox, c);

            skipInterpolationUnitsCheckbox.setToolTipText("Skip nodes that are just interpolation nodes, i.e. nodes that have no input samples mapped");
            skipInterpolationUnitsCheckbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    skipInterpolationUnits = skipInterpolationUnitsCheckbox.isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            c.gridy++;
            add(skipInterpolationUnitsCheckbox, c);

            neighbourhoodModeBox.setSelectedItem(neighbourhoodMode);
            neighbourhoodModeBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    neighbourhoodMode = (NeighbourhoodMode) neighbourhoodModeBox.getSelectedItem();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            c.gridy++;
            add(neighbourhoodModeBox, c);

            disconnectUnfavouredSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    disconnectUnfavoured = disconnectUnfavouredModel.getNumber().intValue();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            JPanel disconnectLastXPanel = new JPanel();
            JLabel label = new JLabel("Disconnect edges");
            label.setToolTipText("Do not connect the X least favourable edges");
            disconnectLastXPanel.add(label);
            disconnectLastXPanel.add(disconnectUnfavouredSpinner);
            c.gridy++;
            add(disconnectLastXPanel, c);
        }
    }

    @Override
    public int getPreferredScaleFactor() {
        // the visualisation is mostly lines => less zooming
        return 1;
    }

}
