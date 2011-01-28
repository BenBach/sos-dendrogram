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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * This is the implementation of an Activity Histogram.<br>
 * FIXME: there are a lot of common parts with {@link ComponentPlanesVisualizer} ==> make a superclass for both of them.
 * 
 * @author Roman Gerger
 * @author Florian Mistelbauer
 * @author Rudolf Mayer
 * @version $Id: ActivityHistogram.java 3849 2010-10-12 12:56:45Z frank $
 */
class ActivityHistogram extends AbstractItemVisualizer {

    private String dataPoint;

    private InputData inputData;

    public ActivityHistogram() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Activity Histogram" };
        VISUALIZATION_SHORT_NAMES = new String[] { "ActivityHistogram" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Displays a colour-coding of the activity of a certain input vector over the whole map, i.e. in principle the vector's distances to all weight vectors.\nImplemented by Roman Gerger and Florian Mistelbauer" };
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR };
        setInterpolate(false);
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR + "input:" + dataPoint;
    }

    /**
     * Draws the activity histogram. Given one input point it calculates the Euclidian distance to each weight vector.
     * 
     * @throws SOMToolboxException If the {@link InputData} is not provided
     */
    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        if (inputData == null) {
            inputData = gsom.getSharedInputObjects().getInputData();
            if (inputData == null) {
                throw new SOMToolboxException("You need to specify the " + neededInputObjects[0]);
            } else {
                dataPoint = inputData.getInputDatum(0).getLabel();
            }
        }

        if (!(controlPanel instanceof ActivityHistrogramControlPanel)) {
            // create control panel once we have the input data vector, and if it is a generic panel
            controlPanel = new ActivityHistrogramControlPanel(this, inputData);
        }

        // read the vector file to fetch the necessary data
        InputDatum item = inputData.getInputDatum(dataPoint);
        L2Metric metric = new L2Metric();

        // calculate the distance matrix
        // FIXME: maybe move the computation to GrowingLayer
        DoubleMatrix2D plane = new DenseDoubleMatrix2D(gsom.getLayer().getYSize(), gsom.getLayer().getXSize());
        for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
            for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                plane.set(y, x, metric.distance(gsom.getLayer().getUnit(x, y).getWeightVector(), item));
            }
        }

        // normalise distances, create image from matrix
        VectorTools.divByMax(plane);
        return createImage(gsom, plane, width, height, interpolate);
    }

    /** Implements the control UI for this visualisation. */
    private class ActivityHistrogramControlPanel extends AbstractSelectedItemVisualizerControlPanel implements
            ListSelectionListener {
        private static final long serialVersionUID = 1L;

        private ActivityHistrogramControlPanel(ActivityHistogram hist, InputData inputData) {
            super("Activity Histogram Control");
            JPanel histPanel = new JPanel(new GridBagLayout());

            GridBagConstraints constr = new GridBagConstraints();
            constr.gridwidth = GridBagConstraints.REMAINDER;
            constr.fill = GridBagConstraints.BOTH;
            constr.weightx = 1.0;
            constr.weighty = 1.0;

            initialiseList(inputData.getLabels());
            JScrollPane listScroller = new JScrollPane(list);
            listScroller.setPreferredSize(new Dimension(map.getState().controlElementsWidth, 150));
            listScroller.setMaximumSize(new Dimension(map.getState().controlElementsWidth, 150));
            histPanel.add(listScroller, constr);

            text.setToolTipText("Enter a (part) of an input vector label, and start the search with the <enter> key");
            text.setText(dataPoint);
            histPanel.add(text, constr);

            add(histPanel, c);
            setVisible(true);
        }

        @Override
        public void componentResized(ComponentEvent e) {
            // setBorder(BorderFactory.createLineBorder(Color.green));
            super.componentResized(e);
            // updateListSize(list, getHeight(), inputData.numVectors());
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            dataPoint = (String) list.getSelectedValue();
            if (visualizationUpdateListener != null) {
                visualizationUpdateListener.updateVisualization();
            }
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(map.getState().controlElementsWidth, 300);
        }

    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        return getVisualizationFlavours(index, gsom, width, height,
                gsom.getSharedInputObjects().getInputData().numVectors());
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            int maxFlavours) throws SOMToolboxException {
        String currentDataPoint = dataPoint;
        InputData currentInputData = inputData;

        HashMap<String, BufferedImage> result = new HashMap<String, BufferedImage>();

        String[] labels = gsom.getSharedInputObjects().getInputData().getLabels();

        inputData = gsom.getSharedInputObjects().getInputData();
        for (int i = 0; i < labels.length && i < maxFlavours; i++) {
            dataPoint = inputData.getInputDatum(i).getLabel();
            result.put("_" + labels[i], getVisualization(index, gsom, width, height));
        }
        dataPoint = currentDataPoint;
        inputData = currentInputData;
        return result;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException {
        // FIXME: Implement this
        return super.getVisualizationFlavours(index, gsom, width, height, flavourParameters);
    }

}