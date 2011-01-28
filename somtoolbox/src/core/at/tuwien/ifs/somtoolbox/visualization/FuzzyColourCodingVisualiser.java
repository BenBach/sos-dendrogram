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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.math.Functions;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.ImageUtils;
import at.tuwien.ifs.somtoolbox.util.UiUtils;
import at.tuwien.ifs.somtoolbox.util.VisualisationUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: FuzzyColourCodingVisualiser.java 3869 2010-10-21 15:56:09Z mayer $
 */
public class FuzzyColourCodingVisualiser extends AbstractBackgroundImageVisualizer {
    protected double T = 1;

    protected int r = 1;

    protected boolean showUnitNodes = true;

    protected boolean showConnectingLines = true;

    protected boolean showColourCoding = true;

    public FuzzyColourCodingVisualiser() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Fuzzy Colouring" };
        VISUALIZATION_SHORT_NAMES = VISUALIZATION_NAMES;
        VISUALIZATION_DESCRIPTIONS = new String[] { "Inplementation of Fuzzy Colouring as described in \""
                + "Johan Himberg. A SOM based cluster visualization and its application for false coloring.\n"
                + " In Proceedings of the IEEE-INNS-ENNS International Joint Conference on Neural Networks (IJCNN 2000), vol. 3, pp. 587-592,\n"
                + " Como, Italy, 2000. " };

        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new FuzzyColouringControlPanel();
        }
        preferredScaleFactor = 1;
    }

    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {

        GrowingLayer layer = gsom.getLayer();

        BufferedImage res = ImageUtils.createEmptyImage(width, height);
        Graphics2D g = (Graphics2D) res.getGraphics();

        double unitWidth = width / (double) layer.getXSize();
        double unitHeight = height / (double) layer.getYSize();

        // set up array of unit coordinates
        Point2D.Double[][] locations = new Point2D.Double[layer.getXSize()][layer.getYSize()];
        for (int i = 0; i < layer.getXSize(); i++) {
            for (int j = 0; j < layer.getYSize(); j++) {
                locations[i][j] = new Double(i, j);
            }
        }

        // construct a dissimilarity matrix of the model vectors
        DoubleMatrix2D unitDistanceMatrix = layer.getUnitDistanceMatrix();

        // transform to a similarity matrix - Equation (1) in Himberg 2000.
        DoubleMatrix2D similarityMatrix = unitDistanceMatrix.copy();
        similarityMatrix.assign(new DoubleFunction() {
            @Override
            public double apply(double argument) {
                return Math.exp(-(argument * argument / T));
            }
        });

        // normalise each row so that it sums up to 1
        for (int i = 0; i < similarityMatrix.rows(); i++) {
            DoubleMatrix1D row = similarityMatrix.viewRow(i);
            final double sum = row.aggregate(Functions.plus, Functions.identity);
            row.assign(new DoubleFunction() {
                @Override
                public double apply(double argument) {
                    return argument / sum;
                }
            });
        }

        // contraction process
        // FIXME: check this with the Matlab implementation, it seems that is a bit different to the paper
        // http://www.cis.hut.fi/somtoolbox/package/docs2/som_fuzzycolor.html)
        for (int k = 0; k < r; k++) {
            Double[][] newLocations = new Double[layer.getXSize()][layer.getYSize()];
            for (int x = 0; x < layer.getXSize(); x++) {
                for (int y = 0; y < layer.getYSize(); y++) {
                    Double loc = locations[x][y];
                    Double newLoc = new Double(loc.x, loc.y);
                    int unitIndex = layer.getUnitIndex(x, y);
                    for (int x1 = 0; x1 < layer.getXSize(); x1++) {
                        for (int y1 = 0; y1 < layer.getYSize(); y1++) {
                            if (x != x1 && y != y1) {
                                int otherUnitIndex = layer.getUnitIndex(x1, y1);
                                double similarity = similarityMatrix.getQuick(unitIndex, otherUnitIndex);
                                // move towards that location
                                double diffX = locations[x1][y1].x - loc.x;
                                double diffY = locations[x1][y1].y - loc.y;
                                newLoc.setLocation(newLoc.x + diffX * similarity, newLoc.y + diffY * similarity);
                            }
                        }
                    }
                    newLocations[x][y] = newLoc;
                }
            }
            locations = newLocations;
        }

        // obtain RGB slice according to the (contracted) unit positions, and draw visualisation
        Color[][] colours = new Color[locations.length][locations[0].length];

        if (showColourCoding) {
            double colourZoomX = 255.0 / layer.getXSize();
            double colourZoomY = 255.0 / layer.getYSize();
            for (int i = 0; i < layer.getXSize(); i++) {
                for (int j = 0; j < layer.getYSize(); j++) {
                    Double loc = locations[i][j];

                    // colour the SOM unit
                    colours[i][j] = new Color(
                    // red is 255 on the top, and 0 on the bottom
                            (int) Math.round(colourZoomY * (layer.getYSize() - loc.y)),
                            // green is 255 on the left, and 0 on the right
                            (int) Math.round(colourZoomX * (layer.getXSize() - loc.x)),
                            // blue is 0 on the top, and 255 on the bottom
                            (int) Math.round(colourZoomY * loc.y));

                    g.setColor(colours[i][j]);
                    g.fillRect((int) (i * unitWidth), (int) (j * unitHeight), (int) unitWidth, (int) unitHeight);
                }
            }
        }

        if (showUnitNodes) {
            int markerHeight = (int) (unitHeight / 5);
            int markerWidth = (int) (unitWidth / 5);
            for (int i = 0; i < layer.getXSize(); i++) {
                for (int j = 0; j < layer.getYSize(); j++) {
                    Double loc = locations[i][j];
                    // draw the nodes
                    g.setColor(Color.black);
                    Point markerPos = getMarkerPos(unitWidth, unitHeight, markerWidth, markerHeight, loc);
                    VisualisationUtils.drawMarker(g, markerWidth, markerHeight, markerPos);
                }
            }
        }

        if (showConnectingLines) {
            g.setColor(Color.black);
            int lineWidth = (int) Math.round(unitWidth / 20);
            int lineHeight = (int) Math.round(unitHeight / 20);
            // draw the connections between nodes; can do this only after colouring, as it needs to be on top
            for (int i = 0; i < layer.getXSize(); i++) {
                for (int j = 0; j < layer.getYSize(); j++) {

                    // draw the nodes connections to the right
                    Point start = getLinePos(unitWidth, unitHeight, locations[i][j]);
                    if (i + 1 < layer.getXSize()) {
                        Point end = getLinePos(unitWidth, unitHeight, locations[i + 1][j]);
                        VisualisationUtils.drawThickLine(g, start.x, start.y, end.x, end.y, lineWidth, lineHeight);
                    }
                    // draw the nodes connections to the right
                    if (j + 1 < layer.getYSize()) {
                        Point end = getLinePos(unitWidth, unitHeight, locations[i][j + 1]);
                        VisualisationUtils.drawThickLine(g, start.x, start.y, end.x, end.y, lineWidth, lineHeight);
                    }
                }
            }
        }

        return res;
    }

    private Point getMarkerPos(double unitWidth, double unitHeight, int markerWidth, int markerHeight, Double loc) {
        return new Point((int) Math.round(loc.x * unitWidth + (unitWidth - markerWidth) / 2), (int) Math.round(loc.y
                * unitHeight + (unitHeight - markerHeight) / 2));
    }

    private Point getLinePos(double unitWidth, double unitHeight, Double loc) {
        return new Point((int) Math.round(loc.x * unitWidth + unitWidth / 2), (int) Math.round(loc.y * unitHeight
                + unitHeight / 2));
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int currentVariant, int width, int height) {
        return appendToCacheKey(gsom, currentVariant, width, height, "T:" + T, "r:" + r, "colorCoding:"
                + showColourCoding, "showUnits:" + showUnitNodes, "showConnections:" + showConnectingLines);
    }

    protected class FuzzyColouringControlPanel extends VisualizationControlPanel {

        private static final long serialVersionUID = 1L;

        public FuzzyColouringControlPanel() {
            super("Fuzzy (False) Colouring");

            final JSpinner tSpinner = new JSpinner(new SpinnerNumberModel(T, 0.01, 2, 0.01));
            tSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    T = ((SpinnerNumberModel) tSpinner.getModel()).getNumber().doubleValue();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            final JSpinner rSpinner = new JSpinner(new SpinnerNumberModel(r, 1, 30, 1));
            rSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    r = ((SpinnerNumberModel) rSpinner.getModel()).getNumber().intValue();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            final JCheckBox boxShowConnectingLines = new JCheckBox("Connect nodes", showConnectingLines);
            boxShowConnectingLines.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showConnectingLines = ((JCheckBox) e.getSource()).isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            JCheckBox boxShowNodes = new JCheckBox("Plot units", showUnitNodes);
            boxShowNodes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showUnitNodes = ((JCheckBox) e.getSource()).isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            JCheckBox boxShowColours = new JCheckBox("Colour coding", showColourCoding);
            boxShowColours.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showColourCoding = ((JCheckBox) e.getSource()).isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            JPanel colouringPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.anchor = GridBagConstraints.NORTHEAST;
            constr.insets = new Insets(2, 2, 2, 2);
            constr.gridy = 0;

            colouringPanel.add(UiUtils.makeLabelWithTooltip("T", "Value of contraction parameter T"), constr);
            colouringPanel.add(tSpinner, constr);
            constr.gridy += 1;

            colouringPanel.add(UiUtils.makeLabelWithTooltip("r", "Number of contraction rounds"), constr);
            colouringPanel.add(rSpinner, constr);
            constr.gridy += 1;

            colouringPanel.add(boxShowNodes, constr);
            colouringPanel.add(boxShowConnectingLines, constr);
            constr.gridy += 1;

            colouringPanel.add(boxShowColours, constr);

            add(colouringPanel, c);

        }
    }

}
