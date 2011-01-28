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

import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections.keyvalue.MultiKey;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.math.Functions;
import cern.jet.stat.quantile.DoubleQuantileFinder;
import cern.jet.stat.quantile.QuantileFinderFactory;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.distance.InputVectorDistanceMatrix;
import at.tuwien.ifs.somtoolbox.data.distance.LeightWeightMemoryInputVectorDistanceMatrix;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.ArrayUtils;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;
import at.tuwien.ifs.somtoolbox.util.inputVerifier.DoubleNumberInputVerifier;

/**
 * This visualizer implements:
 * <ul>
 * <li>Ultsch, A. Maps for the Visualization of high-dimensional Data Spaces. In Proceedings Workshop on Self-Organizing
 * Maps (WSOM 2003), Kyushu, Japan</li>
 * <li>Ultsch, A. U*-Matrix: a Tool to visualize Clusters in high dimensional Data. Technical Report No. 36, Dept. of
 * Mathematics and Computer Science, University of Marburg, Germany, 2003</li>
 * </ul>
 * 
 * @author Rudolf Mayer
 * @version $Id: PMatrix.java 3874 2010-11-02 14:14:38Z mayer $
 */
public class PMatrix extends UMatrix {
    // a cache for the computed p-matrix, which is expensive, and used also for the U*-Matrix
    private static final HashMap<MultiKey, DoubleMatrix2D> pMatrixCache = new HashMap<MultiKey, DoubleMatrix2D>();

    /** pareto-optimal size, as dervied in the paper "Maps for the Visualization of high-dimensional Data Space" */
    public static final double PARETO_SIZE = 0.2013;

    private InputVectorDistanceMatrix distanceMatrix;

    /** The radius for the density calculation */
    private double radius = Double.NaN;

    DoubleMatrix1D percentiles = null;

    public PMatrix() {
        VISUALIZATION_NAMES = new String[] { "P-Matrix", "U*-Matrix" };
        VISUALIZATION_SHORT_NAMES = new String[] { "PMatrix", "UStarMatrix" };
        NUM_VISUALIZATIONS = VISUALIZATION_NAMES.length;
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Implementation of the P-Matrix, as described in Ultsch, A.\n"
                        + "Maps for the Visualization of high-dimensional Data Spaces.\n"
                        + "In Proceedings Workshop on Self-Organizing Maps (WSOM 2003), Kyushu, Japan",
                "Implementation of the U*-Matrix (U- and P-Matrix combined), as described in Ultsch. A.\n"
                        + "U*-Matrix: a Tool to visualize Clusters in high dimensional Data\n"
                        + "Technical Report No. 36, Dept. of Mathematics and Computer Science, University of Marburg, Germany, 2003" };
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR_DISTANCE_MATRIX,
                SOMVisualisationData.INPUT_VECTOR };
        // don't initialise the control panel if we have no graphics environment (e.g. in server applications)
        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new PMatrixControlPanel();
        }
    }

    @Override
    public String[] needsAdditionalFiles() {
        String[] neededDataFiles = super.needsAdditionalFiles();
        // we only need the input vector file, the distance matrix is a bonus
        if (org.apache.commons.lang.ArrayUtils.contains(neededDataFiles, SOMVisualisationData.INPUT_VECTOR)) {
            return neededDataFiles;
        } else {
            return null;
        }
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR + "radius:" + radius;
    }

    public class PMatrixControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        /** The {@link JSpinner} to set the radius via the percentile */
        private JSpinner spinnerPercentile = new JSpinner(new SpinnerNumberModel(20, 1, 50, 1));

        /** The {@link JTextField} to directly set a radius */
        private JTextField textFieldRadius = new JTextField();

        private PMatrixControlPanel() {
            super("P/U*Matrix Control");
            spinnerPercentile.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    radius = percentiles.getQuick((Integer) spinnerPercentile.getValue());
                    updateRadiusTextField();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }

            });
            textFieldRadius.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    radius = Double.parseDouble(textFieldRadius.getText());
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            textFieldRadius.setInputVerifier(new DoubleNumberInputVerifier());
            JButton buttonRecalc = new JButton("Compute optimal radius");
            buttonRecalc.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setOptimalRadius();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            JPanel pmatrixPanel = new JPanel(new GridBagLayout());
            GridBagConstraintsIFS gc = new GridBagConstraintsIFS(GridBagConstraints.NORTHWEST,
                    GridBagConstraints.HORIZONTAL);
            gc.insets = new Insets(0, 2, 2, 1);

            pmatrixPanel.add(new JLabel("Density percentile"), gc);
            pmatrixPanel.add(spinnerPercentile, gc.nextCol().setWeightX(1.0));
            pmatrixPanel.add(new JLabel("P-Radius"), gc.nextRow().setWeightX(0));
            pmatrixPanel.add(textFieldRadius, gc.nextCol().setWeightX(1.0));
            pmatrixPanel.add(buttonRecalc, gc.nextRow().setGridWidth(2).setAnchor(GridBagConstraints.CENTER));
            add(pmatrixPanel, c);
        }

    }

    private void setOptimalRadius() {
        int percentile = calculateParetoRadiusPercentile(distanceMatrix, percentiles);
        radius = percentiles.get(percentile);
        ((PMatrixControlPanel) controlPanel).spinnerPercentile.setValue(percentile);
        updateRadiusTextField();
    }

    private void updateRadiusTextField() {
        ((PMatrixControlPanel) controlPanel).textFieldRadius.setText(StringUtils.format(radius, 5, true));
        ((PMatrixControlPanel) controlPanel).textFieldRadius.setToolTipText("Exact radius: " + radius);
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        checkVariantIndex(index, getClass());

        if (distanceMatrix == null) {
            if (gsom.getSharedInputObjects().getInputVectorDistanceMatrix() != null) {
                distanceMatrix = gsom.getSharedInputObjects().getInputVectorDistanceMatrix();
            } else {
                distanceMatrix = gsom.getSharedInputObjects().getInputVectorDistanceMatrix();
                distanceMatrix = new LeightWeightMemoryInputVectorDistanceMatrix(
                        gsom.getSharedInputObjects().getInputData(), gsom.getLayer().getMetric());
            }

            // calculate the percentiles in the input data
            percentiles = createPercentiles(distanceMatrix);
            // guess the paretoRadius from the percentiles and the input data
            setOptimalRadius();
        }

        DoubleMatrix2D matrix;
        if (index == 0) {
            matrix = createPMatrix(gsom);
        } else {
            matrix = createUStarMatrix(gsom);
        }
        VectorTools.normalise(matrix);
        return createImage(gsom, matrix, width, height, interpolate);
    }

    public DoubleMatrix2D createPMatrix(GrowingSOM gsom) throws MetricException, LayerAccessException {
        // we store a cache of the PMatrix, as it is computationally expensive, and is be used for both P-Matrix and
        // U*-Matrix
        if (!pMatrixCache.containsKey(new MultiKey(gsom, radius))) {

            GrowingLayer layer = gsom.getLayer();
            InputData data = gsom.getSharedInputObjects().getInputData();
            DistanceMetric metric = layer.getMetric();

            int pmatW = layer.getXSize();
            int pmatH = layer.getYSize();
            DoubleMatrix2D pmatrix = new DenseDoubleMatrix2D(pmatH, pmatW);

            // now check // for each unit in the SOM how many of the input data items lie within the Pareto radius
            // around the unit's weight vector
            int index = 0;
            for (int x = 0; x < layer.getXSize(); x++) {
                for (int y = 0; y < layer.getYSize(); y++) {
                    int withinRadius = 0;
                    for (int i = 0; i < data.numVectors(); i++) { // check whether the distance to each input is within
                        // the pareto radius
                        if (metric.distance(layer.getUnit(x, y).getWeightVector(), data.getInputVector(i)) < radius) {
                            withinRadius++;
                        }
                        pmatrix.set(y, x, withinRadius);
                    }
                    index++;
                }
            }
            pMatrixCache.put(new MultiKey(gsom, radius), pmatrix);
        }

        return pMatrixCache.get(new MultiKey(gsom, radius));
    }

    public static int coordinates2index(int row, int col, int columns) {
        return row * columns + col;
    }

    private DoubleMatrix1D createPercentiles(InputVectorDistanceMatrix distances) {
        // FIXME: document this
        final double[] distancesFlat = distances.getDistancesFlat();
        DoubleMatrix1D reducedValues = new DenseDoubleMatrix1D(distancesFlat);

        // calculate distance percentiles
        DoubleQuantileFinder finder = QuantileFinderFactory.newDoubleQuantileFinder(true, reducedValues.size(), 0.0,
                0.0, 100, null);
        finder.addAllOf(new DoubleArrayList(distancesFlat));
        double[] p = ArrayUtils.getLinearPercentageArray();
        DoubleArrayList percentages = new DoubleArrayList(p);
        return new DenseDoubleMatrix1D(finder.quantileElements(percentages).elements());
    }

    private DoubleMatrix1D getAllDensities(InputVectorDistanceMatrix distances, double radius) {
        // FIXME: use DoubleMatrix1D right away, w/o having to go via a list
        DoubleArrayList list = new DoubleArrayList();
        for (int row = 0; row < distances.rows(); row++) {
            int counter = 0;
            // get only the right part of the matrix (its symmetric)
            for (int col = row; col < distances.columns(); col++) {
                double distance = distances.getDistance(row, col);
                if (distance < radius) {
                    counter++;
                }
            }
            list.add(counter);
        }
        return DoubleFactory1D.dense.make(list);
    }

    private int calculateParetoRadiusPercentile(InputVectorDistanceMatrix distances, DoubleMatrix1D percentiles) {

        // the paper describes the 18th percentile as a good start value for gaussian distributions
        int percentile = 18;
        double radius;

        // variables needed for the search
        int last_percentile = percentile;
        double diff = 0.0;
        double last_diff = 1.0;
        double median_size;
        boolean stop = false;
        double upper_size = 1.0;
        double lower_size = 0.0;

        // upper and lower search boundaries for the percentiles
        double upper_percentile = 50;
        double lower_percentile = 2;

        Logger log = Logger.getLogger("at.tuwien.ifs.somtoolbox");

        while (!stop) {
            // get current radius from the percentile
            radius = percentiles.getQuick(percentile);

            // compute densities with this radius
            DoubleMatrix1D densities = getAllDensities(distances, radius);

            // median percentage of points in spheres
            if (densities.size() != 0) {
                double median = VectorTools.median(densities.toArray());
                double mean = densities.zSum() / densities.size();
                log.info("Mean: " + mean + " median: " + median);
                median_size = Math.max(median, mean) / distances.columns();
            } else {
                median_size = 0;
            }
            log.fine("spheres for " + percentile + "%-tile contain on average " + Math.round(median_size * 100)
                    + "% of the data");

            // compute difference of median size to the defined optimum
            diff = median_size - PARETO_SIZE;

            // stop if last step was 1, or the defined upper/lower stopping criterion is reached
            stop = Math.abs(percentile - last_percentile) == 1 || percentile == upper_percentile
                    || percentile == lower_percentile;

            if (!stop) { // iterate
                last_percentile = percentile;
                last_diff = diff;

                // adjust percentile towards optimum with linear interpolation
                if (diff > 0) {
                    upper_percentile = percentile;
                    upper_size = median_size;
                } else {
                    lower_percentile = percentile;
                    lower_size = median_size;
                }

                // compute the estimated position of pareto size in the current search interval
                double pest = (PARETO_SIZE - lower_size) / (upper_size - lower_size)
                        * (upper_percentile - lower_percentile) + lower_percentile;

                // step towards the estimated position
                double step = pest - percentile;

                // always go at least 1 resp. -1
                if (step > 0) {
                    step = Math.max(step, 1);
                } else {
                    step = Math.min(step, -1);
                }
                percentile = percentile + (int) Math.round(step);
            } else {
                // if it is better, revert to the last percentile before we stopped
                if (Math.abs(diff) > Math.abs(last_diff)) {
                    percentile = last_percentile;
                }
            }
        }

        log.info("P-Matrix: " + percentile + "%tile chosen.");
        return percentile;
    }

    public DoubleMatrix2D createUStarMatrix(GrowingSOM gsom) throws MetricException, LayerAccessException {
        DoubleMatrix2D umatrix = createUMatrix(gsom);
        DoubleMatrix2D pmatrix = createPMatrix(gsom);

        double meanP = pmatrix.zSum() / pmatrix.size();
        double maxP = pmatrix.aggregate(Functions.max, Functions.identity);
        double diff = meanP - maxP;

        DoubleMatrix2D ustarmatrix = DoubleFactory2D.dense.make(gsom.getLayer().getYSize(), gsom.getLayer().getXSize(),
                -1);

        for (int x = 0; x < ustarmatrix.rows(); x++) {
            for (int y = 0; y < ustarmatrix.columns(); y++) {
                double uheight = umatrix.getQuick(x * 2, y * 2);
                double pheight = pmatrix.getQuick(x, y);
                double scaleFactor = (pheight - meanP) / diff + 1;
                ustarmatrix.setQuick(x, y, uheight * scaleFactor);
            }
        }

        return ustarmatrix;
    }

}
