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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.math.Functions;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.AdaptiveCoordinatesVirtualLayer;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.UiUtils;

/**
 * Implementation of the adaptive coordinates method.
 * 
 * @author Timo Kropp (0627880)
 * @author Goran Jovanov (0727760)
 * @author Rudolf Mayer
 * @version $Id: AdaptiveCoordinatesVisualizer.java 3883 2010-11-02 17:13:23Z frank $
 */
public class AdaptiveCoordinatesVisualizer extends AbstractMatrixVisualizer implements BackgroundImageVisualizer {

    private int dotSize = 7;

    private int fontSize = 12;

    private boolean showUnitNames = false;

    private boolean showInputNames = false;

    private boolean showDots = true;

    private boolean showHitHisto = true;

    private double selectedThreshold;

    public AdaptiveCoordinatesVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Adaptive Coordinates" };
        VISUALIZATION_SHORT_NAMES = new String[] { "AdaptCoord" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Adaptive Coordinates" };
        setInterpolate(false);
        preferredScaleFactor = 2;

        neededInputObjects = new String[] { SOMVisualisationData.ADAPTIVE_COORDINATES,
                SOMVisualisationData.INPUT_VECTOR };

        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new AdaptiveCoordinatesControlPanel();
        }
    }

    @Override
    protected void checkNeededObjectsAvailable(GrowingSOM gsom) throws SOMToolboxException {
        super.checkNeededObjectsAvailable(gsom);
        // TODO: in the future, we should be able to create the .adaptiveCoord file with training newly..
        // if (gsom.getSharedInputObjects().getData(neededInputObjects[0]) == null
        // && gsom.getSharedInputObjects().getData(neededInputObjects[1]) == null) {
        // throw new SOMToolboxException("You need to specify at least one out of " + neededInputObjects[0] + " or "
        // + neededInputObjects[1]);
        // }
    }

    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        checkNeededObjectsAvailable(gsom);

        final GrowingLayer layer = gsom.getLayer();
        AdaptiveCoordinatesVirtualLayer virtualLayer = gsom.getSharedInputObjects().getAdaptiveCoordinates();
        if (virtualLayer == null) {
            // TODO: compute on the fly!
        }

        if (controlPanel != null) {
            ((AdaptiveCoordinatesControlPanel) controlPanel).setThresholds(virtualLayer.getThresholds());
        } else {
            selectedThreshold = virtualLayer.getThresholds()[0];
        }

        BufferedImage bufferedImage = null;
        DoubleMatrix2D matrix = null;

        if (showHitHisto) {
            matrix = computeHitHistogram(gsom);
            matrix.assign(Functions.div(maximumMatrixValue));// normalisation
        } else {
            matrix = new DenseDoubleMatrix2D(layer.getYSize(), layer.getXSize());
        }
        bufferedImage = super.createImage(gsom, matrix, width, height, interpolate);
        drawPoints(bufferedImage, gsom.getLayer(), virtualLayer, width, height);
        return bufferedImage;
    }

    private void drawPoints(BufferedImage bufferedImage, GrowingLayer layer,
            AdaptiveCoordinatesVirtualLayer virtualLayer, int width, int height) throws LayerAccessException {
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
        g.setColor(Color.black);
        g.setStroke(new BasicStroke(0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] { 5f }, 5f));
        g.setFont(new Font("Arial", Font.PLAIN, fontSize));

        double unitWidth = width / layer.getXSize();
        double unitHeight = height / layer.getYSize();

        for (int x = 0; x < layer.getXSize(); x++) {
            for (int y = 0; y < layer.getYSize(); y++) {
                int posX = (int) (unitWidth * virtualLayer.getVirtualUnit(selectedThreshold, x, y).getAXPos()
                        + unitWidth / 2 + 0.5);
                int posY = (int) (unitHeight * virtualLayer.getVirtualUnit(selectedThreshold, x, y).getAYPos()
                        + unitHeight / 2 + 0.5);
                if (showDots) {
                    g.fillOval(posX - (int) (dotSize / 2 + 0.5), posY - (int) (dotSize / 2 + 0.5), dotSize, dotSize);
                }
                if (showUnitNames) {
                    g.drawString(layer.getUnit(x, y).toString(), posX + dotSize / 2, posY);
                }
                if (showInputNames && layer.getUnit(x, y).getMappedInputNames() != null) {
                    String labels = "";
                    for (int i = 0; i < layer.getUnit(x, y).getMappedInputNames().length; i++) {
                        labels = labels + layer.getUnit(x, y).getMappedInputNames()[i] + " ";
                    }
                    g.drawString(labels, posX + dotSize / 2, posY + fontSize - 2);
                }
            }
        }

    }

    protected class AdaptiveCoordinatesControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        private JSpinner spinnerThresholds;

        private JLabel labelThresholds = UiUtils.makeLabelWithTooltip("Thresholds",
                "Select the thresholds for the Adaptive Coordinates");

        public AdaptiveCoordinatesControlPanel() {
            super("Adaptive Coordinate Control");

            SpinnerListModel listModel = new SpinnerListModel();
            spinnerThresholds = new JSpinner(listModel);
            ((JSpinner.DefaultEditor) spinnerThresholds.getEditor()).getTextField().setEditable(false);
            spinnerThresholds.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    selectedThreshold = (Double) spinnerThresholds.getValue();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            JSpinner dotRadiusSpinner = new JSpinner(new SpinnerNumberModel(dotSize, 1, 100.0, 1));
            dotRadiusSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    dotSize = (int) ((Double) ((JSpinner) e.getSource()).getValue()).doubleValue();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            final SpinnerNumberModel fontsizeSpinnerModel = new SpinnerNumberModel(fontSize, 1, 30, 1);
            JSpinner fontsizeSpinner = new JSpinner(fontsizeSpinnerModel);
            fontsizeSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    fontSize = fontsizeSpinnerModel.getNumber().intValue();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            // Show Unit Names checkbox
            JCheckBox boxShowUnitNames = new JCheckBox("Unit labels", showUnitNames);
            boxShowUnitNames.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showUnitNames = ((JCheckBox) e.getSource()).isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            // Show Unit Labels
            JCheckBox boxShowInputNames = new JCheckBox("Input labels", showInputNames);
            boxShowInputNames.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showInputNames = ((JCheckBox) e.getSource()).isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            // Show Dots
            JCheckBox boxShowDots = new JCheckBox("Adaptive Coords", showDots);
            boxShowDots.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showDots = ((JCheckBox) e.getSource()).isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            // Show Hit Histogram
            JCheckBox boxShowHitHisto = new JCheckBox("Hit Histogram");
            boxShowHitHisto.setSelected(showHitHisto);
            boxShowHitHisto.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showHitHisto = ((JCheckBox) e.getSource()).isSelected();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            JPanel acPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.anchor = GridBagConstraints.NORTHWEST;
            constr.insets = new Insets(2, 2, 2, 2);
            constr.gridy = 0;

            acPanel.add(labelThresholds, constr);
            acPanel.add(spinnerThresholds, constr);

            constr.gridy += 1;
            JPanel dotSizePanel = new JPanel();
            dotSizePanel.add(UiUtils.makeLabelWithTooltip("Dot size: ",
                    "The radius of the dots indicating the adaptive coordinates"));
            dotSizePanel.add(dotRadiusSpinner);
            acPanel.add(dotSizePanel, constr);

            JPanel fontSizePanel = new JPanel();
            fontSizePanel.add(new JLabel("Font size: "));
            fontSizePanel.add(fontsizeSpinner);
            acPanel.add(fontSizePanel, constr);

            constr.gridy += 1;
            acPanel.add(boxShowUnitNames, constr);
            acPanel.add(boxShowInputNames, constr);

            constr.gridy += 1;
            acPanel.add(boxShowDots, constr);
            acPanel.add(boxShowHitHisto, constr);

            add(acPanel, c);
        }

        public void setThresholds(double... thresholds) {
            SpinnerListModel model = (SpinnerListModel) spinnerThresholds.getModel();

            // find out whether we need to update the model
            boolean modelEqual = true;

            if (model == null || model.getList().size() == 0) { // empty model => replace
                modelEqual = false;
            } else if (model.getList().size() != thresholds.length) { // different size => replace
                modelEqual = false;
            } else { // same size => check contents
                for (int i = 0; i < model.getList().size(); i++) {
                    if (!model.getList().get(i).equals(thresholds[i])) {
                        modelEqual = false;
                        break;
                    }
                }
            }
            if (!modelEqual) {
                // for some reason, Arrays.asList() didn't return a List that the SpinnerListModel can handle
                List<Double> list = new ArrayList<Double>(thresholds.length);
                for (double threshold : thresholds) {
                    list.add(threshold);
                }
                model.setList(list);

                spinnerThresholds.setToolTipText("Select the Adaptive Coordinates threshold ("
                        + StringUtils.toString(thresholds, "", "") + ")");

                spinnerThresholds.setVisible(model.getList().size() > 0);
                labelThresholds.setVisible(spinnerThresholds.isVisible());
                selectedThreshold = (Double) model.getValue();
            }
        }

    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR
                + buildCacheKey("threshold:" + selectedThreshold, //
                        "unitLabels:" + showUnitNames, "inputLabels:" + showInputNames, "fontSize:" + fontSize,//
                        "showDots:" + showDots, "dotSize:" + dotSize, //
                        "hitHisto:" + showHitHisto);
    }

    /**
     * Overrides {@link AbstractBackgroundImageVisualizer#needsAdditionalFiles()}, as we need only one of the two
     * possible input files to create this visualisation. If the adaptive coordinates file is present, it will be used
     * directly, otherwise it can be created from the input vectors.
     */
    @Override
    public String[] needsAdditionalFiles() {
        String[] dataFiles = super.needsAdditionalFiles();
        // TODO: in the future, we should be able to create the .adaptiveCoord file with training newly..
        // if (dataFiles.length < 2) { // we need only one of the files
        // return null;
        // } else {
        // return dataFiles;
        // }
        return dataFiles;
    }
}
