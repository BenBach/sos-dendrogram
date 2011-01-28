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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math.util.MathUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.distance.InputVectorDistanceMatrix;
import at.tuwien.ifs.somtoolbox.data.distance.LeightWeightMemoryInputVectorDistanceMatrix;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.UnitPair;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.DateUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.VisualisationUtils;

/**
 * This visualisation provides two visualization plugin-ins for neighbourhood graphs. The first one uses knn-based
 * distances, the second one radius-based distances.<br>
 * Described in:<br>
 * <i><b>Georg Poelzlbauer, Andreas Rauber, and Michael Dittenbach</b>. <a
 * href="http://www.ifs.tuwien.ac.at/~poelzlbauer/publications/Poe05ISNN.pdf"> Advanced visualization techniques for
 * self-organizing maps with graph-based methods.</a> In Jun Wang, Xiaofeng Liao, Zhang Yi, editors, Proceedings of the
 * Second International Symposium on Neural Networks (ISNN'05), pages 75-80, Chongqing, China, May 30 - June 1 2005.
 * Springer-Verlag. </i>
 * 
 * @author Stefan Ruemmele
 * @author Christian Kapeller
 * @author Frank Pourvoyeur
 * @author Rudolf Mayer
 * @version $Id: NeighbourhoodGraph.java 3883 2010-11-02 17:13:23Z frank $
 */
public class NeighbourhoodGraph extends AbstractBackgroundImageVisualizer {
    /* range of possible k-values */
    private static final int MIN_K = 1;

    private static final int MAX_K = 30;

    /* range of possible radius-values */
    private static final double MIN_RADIUS = 0.1;

    private static final double MAX_RADIUS = 10.0;

    /** number of neighbours for knn-based distances */
    private int k;

    /** radius for radius-based distances */
    private double radius;

    /** control panel for this plug-in */
    private NeighbourhoodControlPanel neighbourhoodPanel;

    private int currentVisualization;

    private InputData inputData;

    private InputVectorDistanceMatrix distanceMatrix;

    private DistanceMetric metric;

    /** caches the results of knn-based connections */
    @SuppressWarnings("unchecked")
    private ArrayList<UnitPair>[] knnLinesCache = new ArrayList[MAX_K - MIN_K + 1];

    /** caches the results of radius-based connections */
    private Hashtable<Double, ArrayList<UnitPair>> radiusLinesCache = new Hashtable<Double, ArrayList<UnitPair>>();

    int numVectors;

    /**
     * Constructor.
     */
    public NeighbourhoodGraph() {
        NUM_VISUALIZATIONS = 2;

        VISUALIZATION_NAMES = new String[] { "Neighbourhood Graph: k-nn", "Neighbourhood Graph: Radius" };
        VISUALIZATION_SHORT_NAMES = new String[] { "NeighbourhoodKnn", "NeighbourhoodRadius" };

        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Implementation of Neighbourhood graph using knn-based distances, as described in \""
                        + "G. Poelzlbauer, A. Rauber, M. Dittenbach. \n Advanced "
                        + "visualization of Self-Organizing Maps with vector fields.\n"
                        + "In Jun Wang, Xiaofeng Liao, Zhang Yi, editors, Proceedings of the Second International Symposium on Neural Networks (ISNN'05), pages 75-80, Chongqing, China, May 30 - June 1 2005. Springer-Verlag.\"",
                "Neighbourhood graph using radius-based distances" };

        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR_DISTANCE_MATRIX,
                SOMVisualisationData.INPUT_VECTOR };

        k = MIN_K;
        radius = MIN_RADIUS;

        neighbourhoodPanel = new NeighbourhoodControlPanel();
        controlPanel = neighbourhoodPanel;
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {

        if (gsom.getSharedInputObjects().getData(neededInputObjects[0]) == null
                && gsom.getSharedInputObjects().getData(neededInputObjects[1]) == null) {
            throw new SOMToolboxException("You need to specify at least one out of " + neededInputObjects[0] + " or "
                    + neededInputObjects[1]);
        }

        if (inputData == null) {
            inputData = gsom.getSharedInputObjects().getInputData();
            metric = gsom.getLayer().getMetric();
            numVectors = inputData.numVectors();
            distanceMatrix = gsom.getSharedInputObjects().getInputVectorDistanceMatrix();
            if (distanceMatrix == null) {
                distanceMatrix = new LeightWeightMemoryInputVectorDistanceMatrix(inputData, metric);
            }
        }

        BufferedImage res;
        Graphics2D g;
        ArrayList<UnitPair> lines;

        if (index < 0 || index > 1) {
            return null;
        }

        currentVisualization = index;

        if (index == 0) {
            neighbourhoodPanel.label.setText("KNN Control" + ": ");
            neighbourhoodPanel.spinner.setModel(new SpinnerNumberModel(k, MIN_K, MAX_K, MIN_K));
            lines = createKNNBased(gsom, width, height);
        } else {
            neighbourhoodPanel.label.setText("Radius Control" + ": ");
            neighbourhoodPanel.spinner.setModel(new SpinnerNumberModel(radius, MIN_RADIUS, MAX_RADIUS, MIN_RADIUS));
            lines = createRadiusBased(gsom, width, height);
        }
        neighbourhoodPanel.panel.revalidate();

        // create empty image
        res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) res.getGraphics();

        g.setColor(Color.WHITE);
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, width, height);

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(0.3f));

        // connect the units of a pair with a line
        for (UnitPair pair : lines) {
            VisualisationUtils.drawThickLine(g, pair.getFirst(), pair.getSecond(), unitWidth, unitHeight);
        }

        return res;
    }

    /**
     * Returns a list of unit-pairs, for which at least one of the two units is one of the k-nearest neighbours of the
     * other one.
     */
    private ArrayList<UnitPair> createKNNBased(GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        if (knnLinesCache[k - 1] == null) {
            long start = System.currentTimeMillis();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Starting calculation of knn = " + k);

            ArrayList<UnitPair> lines = new ArrayList<UnitPair>();

            if (inputData == null) {
                throw new SOMToolboxException("You need to specify the " + neededInputObjects[0]);
            }

            StdErrProgressWriter progress = new StdErrProgressWriter(numVectors,
                    "Calculating nearest neighbours for vector ", numVectors / 10);

            // iterate through all vectors
            for (int i = 0; i < numVectors; i++) {
                // the unit of the current vector
                Unit firstUnit = gsom.getLayer().getWinner(inputData.getInputDatum(i));

                int[] nearest = distanceMatrix.getNNearest(i, k);
                for (int element : nearest) {
                    if (element < 0) {
                        continue;
                    }
                    // create a pair out of the two units
                    Unit secondUnit = gsom.getLayer().getWinner(inputData.getInputDatum(element));
                    if (!firstUnit.equals(secondUnit)) {
                        UnitPair pair = new UnitPair(firstUnit, secondUnit);
                        // the list contains only one entry for each pair
                        if (!lines.contains(pair)) {
                            lines.add(pair);
                        }
                    }
                }
                progress.progress(i + 1);
            }
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Finished calculation of knn = " + k + ", "
                            + DateUtils.formatDurationOneUnit(System.currentTimeMillis() - start));
            knnLinesCache[k - 1] = lines;
        }
        return knnLinesCache[k - 1];
    }

    /**
     * Returns a list of unit-pairs, for which the distance of at least one of them to the other one is smaller than the
     * fixed radius.
     */
    private ArrayList<UnitPair> createRadiusBased(GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        if (radiusLinesCache.get(new Double(radius)) == null) {
            if (inputData == null) {
                throw new SOMToolboxException("You need to specify the " + neededInputObjects[0]);
            }

            long start = System.currentTimeMillis();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Starting calculation of radius = " + radius);
            ArrayList<UnitPair> lines = new ArrayList<UnitPair>();

            StdErrProgressWriter progress = new StdErrProgressWriter(numVectors,
                    "Calculating nearest neighbours for vector ", numVectors / 10);
            // iterate through all vectors
            for (int i = 0; i < numVectors; i++) {
                progress.progress(i);
                // the unit of the current vector
                Unit firstUnit = gsom.getLayer().getWinner(inputData.getInputDatum(i));

                // find the units with a distance smaller than radius
                for (int j = 0; j < numVectors; j++) {
                    if (distanceMatrix.getDistance(i, j) < radius) {
                        // create a pair out of the two units
                        Unit secondUnit = gsom.getLayer().getWinner(inputData.getInputDatum(j));
                        if (!firstUnit.equals(secondUnit)) {
                            UnitPair pair = new UnitPair(firstUnit, secondUnit);
                            // the list contains only one entry for each pair
                            if (!lines.contains(pair)) {
                                lines.add(pair);
                            }
                        }
                    }
                }
            }
            radiusLinesCache.put(new Double(radius), lines);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Finished calculation of radius = " + radius + ", "
                            + DateUtils.formatDurationOneUnit(System.currentTimeMillis() - start));
        }
        return radiusLinesCache.get(new Double(radius));
    }

    /** The control panel for the two plug-ins, containing a JSpinner. */
    private class NeighbourhoodControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        private JPanel panel;

        private JLabel label;

        private JSpinner spinner;

        private NeighbourhoodControlPanel() {
            super("Neighbourhood Control");
            spinner = new JSpinner(new SpinnerNumberModel(0, 0, 1, 1));
            spinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent ev) {
                    if (currentVisualization == 0) {
                        // Workaround for spinner.setMaxValue()
                        int newK = ((Number) neighbourhoodPanel.spinner.getValue()).intValue();
                        if (newK == k) {
                            return;
                        }
                        int maxK = distanceMatrix.columns();
                        if (newK > maxK) {
                            neighbourhoodPanel.spinner.getModel().setValue(maxK);
                            return;
                        }
                        k = ((Number) neighbourhoodPanel.spinner.getValue()).intValue();
                    } else {
                        radius = ((Number) neighbourhoodPanel.spinner.getValue()).doubleValue();
                    }

                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            label = new JLabel("Control: ");
            panel = new JPanel();

            panel.add(label);
            panel.add(spinner);
            add(panel, c);
        }
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        if (index == 0) {
            return super.appendToCacheKey(gsom, index, width, height, "key:" + k);
        } else {
            return super.appendToCacheKey(gsom, index, width, height, "radius:" + radius);
        }
    }

    public HashMap<String, BufferedImage> getVisualizationFlavours_K(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        // K
        HashMap<String, BufferedImage> res = new HashMap<String, BufferedImage>();
        int currentK = k;
        for (int i = MIN_K; i <= MAX_K; i++) {
            k = i;
            res.put("_k" + i, getVisualization(index, gsom, width, height));
        }
        k = currentK;
        return res;
    }

    public HashMap<String, BufferedImage> getVisualizationFlavours_R(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        // R
        HashMap<String, BufferedImage> res = new HashMap<String, BufferedImage>();
        double currentR = radius;
        for (double i = MIN_RADIUS; MathUtils.round(i, 1) <= MAX_RADIUS;) {
            radius = i;
            res.put(String.format("_radius%.2f", i), getVisualization(index, gsom, width, height));

            if (MathUtils.round(i, 1) < 5.0) {
                i += 0.1;
            } else {
                i += 1;
            }

        }
        radius = currentR;
        return res;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        if (index == 0) {
            return getVisualizationFlavours_K(index, gsom, width, height);
        } else {
            return getVisualizationFlavours_R(index, gsom, width, height);
        }
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            int maxFlavours) throws SOMToolboxException {
        Logger.getLogger(this.getClass().getName()).warning("Not implemented, creating all flavours");
        return getVisualizationFlavours(index, gsom, width, height);
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException {
        Logger.getLogger(this.getClass().getName()).warning("Not implemented, creating all flavours");
        return getVisualizationFlavours(index, gsom, width, height);
    }

    @Override
    public String[] needsAdditionalFiles() {
        String[] dataFiles = super.needsAdditionalFiles();
        if (dataFiles.length < 2) { // we need only one of the files
            return null;
        } else {
            return dataFiles;
        }
    }

}