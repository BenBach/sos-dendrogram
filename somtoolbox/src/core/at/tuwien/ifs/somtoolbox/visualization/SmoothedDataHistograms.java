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

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.math.Functions;
import flanagan.interpolation.BiCubicSplineFast;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.VectorTools;
import at.tuwien.ifs.somtoolbox.util.comparables.UnitDistance;

/**
 * This visualizer provides an implementation of the <i>Smoothed Data Histograms</i> in three variants.
 * <ol>
 * <li>Implementation of the Smoothed Data Histograms as described in <i><b>E. Pampalk, A. Rauber, and D. Merkl.</b>
 * Proceedings of the International Conference on Artificial Neural Networks (ICANN'02), pp 871-876, LNCS 2415, Madrid,
 * Spain, August 27-30, 2002, Springer Verlag.</i></li>
 * <li>An extension of the Smoothed Data Histograms. Not the rank is taken into account for histogram calculation, but
 * distances "between input vectors and weight vectors.</li>
 * <li>As 2., but additionally values are normalized per datum.</li>
 * </ol>
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: SmoothedDataHistograms.java 3888 2010-11-02 17:42:53Z frank $
 */
public class SmoothedDataHistograms extends AbstractMatrixVisualizer implements BackgroundImageVisualizer,
        ChangeListener {

    /**
     * The minimum value for the smoothing factor (1), resulting in only the best winning units to get a hit counted.
     */
    protected static final int MIN_SMOOTHING_VALUE = 1;

    /**
     * The maximum value for the smoothing factor (300).
     */
    protected static int MAX_SMOOTHING_VALUE = 300;

    /**
     * The default value for the smoothing factor (15).
     */
    protected static int DEFAULT_SMOOTHING_VALUE = 15;

    /**
     * The currently used smoothing factor. The smoothing factor decides how many n-best matching units get a hit
     * counted.
     */
    protected int s = DEFAULT_SMOOTHING_VALUE;

    /**
     * A cache for the different smoothing factors.
     */
    protected Hashtable<Integer, Histogram>[] smoothingCache = null;

    protected SOMLibDataWinnerMapping dataWinnerMapping = null;

    @SuppressWarnings("unchecked")
    public SmoothedDataHistograms() {
        NUM_VISUALIZATIONS = 3;
        VISUALIZATION_NAMES = new String[] { "Smoothed Data Histograms", "Weighted SDH", "Weighted SDH (norm.)" };
        VISUALIZATION_SHORT_NAMES = new String[] { "SDH", "WeightedSDH", "WeightedSDHNorm" };
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Implementation of Smoothed Data Histograms as described in \"E. Pampalk, A. Rauber, and D. Merkl.\n"
                        + "Proceedings of the International Conference on Artificial Neural Networks (ICANN'02),\n"
                        + "pp 871-876, LNCS 2415, Madrid, Spain, August 27-30, 2002, Springer Verlag.",

                "Extension of Smoothed Data Histograms. Not rank is taken into account for histogram calculation, but distances\n"
                        + "between input vectors and weight vectors.",

                "Extension of Smoothed Data Histograms. Not rank is taken into account for histogram calculation, but distances\n"
                        + "between input vectors and weight vectors. Values are normalized per datum." };
        neededInputObjects = new String[] { SOMVisualisationData.DATA_WINNER_MAPPING, SOMVisualisationData.INPUT_VECTOR };

        reversePalette();
        smoothingCache = new Hashtable[NUM_VISUALIZATIONS];

        // don't initialise the control panel if we have no graphics environment (e.g. in server applications)
        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new SDHControlPanel();
        }
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR + "smoothing:" + s;
    }

    /** Visualisation for a specific smoothing factor */
    public BufferedImage getVisualization(int index, int smoothingFactor, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        int oldSmoothingFactor = s;
        s = smoothingFactor;
        controlPanel.updateZDim(gsom.getLayer().getZSize());
        String cacheKey = getCacheKey(gsom, index, width, height);
        logImageCache(cacheKey);
        if (cache.get(cacheKey) == null) {
            cache.put(cacheKey, createVisualization(index, gsom, width, height));
        }
        s = oldSmoothingFactor;
        return cache.get(cacheKey);
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        return createVisualization(index, gsom, width, height, 1, 1, false, true);
    }

    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height, int blockWidth,
            int blockHeight, boolean forceSmoothingCacheInitialisation, boolean shallDrawBackground)
            throws SOMToolboxException {
        checkNeededObjectsAvailable(gsom);
        ceckInitSmoothingCache(gsom, forceSmoothingCacheInitialisation);

        // FIXME: this part is not working anymore, but has to be generally in AbstractBackgroundImageVisualizer
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        if (shallDrawBackground) {
            drawBackground(width, height, g);
        }

        return super.createImage(gsom, smoothingCache[index].get(s).mh, width, height, interpolate);
    }

    @Override
    protected BiCubicSplineFast computeSpline(GrowingSOM gsom, DoubleMatrix2D matrix, int width, int height,
            int unitWidth, int unitHeight) {
        int xSize = gsom.getLayer().getXSize();
        int ySize = gsom.getLayer().getYSize();
        double[] x1 = new double[xSize + 2];
        x1[0] = 0;
        for (int x = 0; x < xSize; x++) {
            x1[x + 1] = x * unitWidth + unitWidth / 2;
        }
        x1[xSize + 1] = width;
        double[] x2 = new double[ySize + 2];
        x2[0] = 0;
        for (int y = 0; y < ySize; y++) {
            x2[y + 1] = y * unitHeight + unitHeight / 2;
        }
        x2[ySize + 1] = height;

        DenseDoubleMatrix2D sdhWithBorders = new DenseDoubleMatrix2D(ySize + 2, xSize + 2);

        // top-left corner
        sdhWithBorders.setQuick(0, 0, matrix.getQuick(0, 0) - (matrix.getQuick(1, 1) - matrix.getQuick(0, 0)) / 2);
        // top-right corner
        sdhWithBorders.setQuick(0, xSize + 1,
                matrix.getQuick(0, xSize - 1) - (matrix.getQuick(1, xSize - 2) - matrix.getQuick(0, xSize - 1)) / 2);
        // bottom-left corner
        sdhWithBorders.setQuick(ySize + 1, 0,
                matrix.getQuick(ySize - 1, 0) - (matrix.getQuick(ySize - 2, 1) - matrix.getQuick(ySize - 1, 0)) / 2);
        // FIXME ? was sdhWithBorders.setQuick(xSize, ySize) for former method 'createVisualization3'.
        // bottom-right corner
        sdhWithBorders.setQuick(
                ySize + 1,
                xSize + 1,
                matrix.getQuick(ySize - 1, xSize - 1)
                        - (matrix.getQuick(ySize - 2, xSize - 2) - matrix.getQuick(ySize - 1, xSize - 1)) / 2);

        for (int x = 1; x < xSize + 1; x++) {
            // top row
            sdhWithBorders.setQuick(0, x,
                    matrix.getQuick(0, x - 1) - (matrix.getQuick(1, x - 1) - matrix.getQuick(0, x - 1)) / 2);
            // bottom row
            sdhWithBorders.setQuick(
                    ySize + 1,
                    x,
                    matrix.getQuick(ySize - 1, x - 1)
                            - (matrix.getQuick(ySize - 2, x - 1) - matrix.getQuick(ySize - 1, x - 1)) / 2);
        }
        for (int y = 1; y < ySize + 1; y++) {
            // left column
            sdhWithBorders.setQuick(y, 0,
                    matrix.getQuick(y - 1, 0) - (matrix.getQuick(y - 1, 1) - matrix.getQuick(y - 1, 0)) / 2);
            // right column
            sdhWithBorders.setQuick(
                    y,
                    xSize + 1,
                    matrix.getQuick(y - 1, xSize - 1)
                            - (matrix.getQuick(y - 1, xSize - 2) - matrix.getQuick(y - 1, xSize - 1)) / 2);
        }
        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                sdhWithBorders.setQuick(y + 1, x + 1, matrix.getQuick(y, x));
            }
        }
        BiCubicSplineFast bcs = new BiCubicSplineFast(x2, x1, sdhWithBorders.toArray());
        return bcs;
    }

    @Override
    protected void checkNeededObjectsAvailable(GrowingSOM gsom) throws SOMToolboxException {
        if (gsom.getSharedInputObjects().getData(neededInputObjects[0]) == null
                && gsom.getSharedInputObjects().getData(neededInputObjects[1]) == null) {
            throw new SOMToolboxException("You need to specify at least one out of " + neededInputObjects[0] + " or "
                    + neededInputObjects[1]);
        }
    }

    protected void ceckInitSmoothingCache(GrowingSOM gsom, boolean forceSmoothingCacheInitialisation)
            throws SOMToolboxException {
        if (forceSmoothingCacheInitialisation || smoothingCache[0] == null || smoothingCache[1] == null
                || smoothingCache[2] == null) {
            initSmoothingCache(gsom);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().getClass() == JSpinner.class) {
            JSpinner src = (JSpinner) e.getSource();
            s = ((Integer) src.getValue()).intValue();
        }
        if (visualizationUpdateListener != null) {
            visualizationUpdateListener.updateVisualization();
        }
    }

    /**
     * Return the currently used smoothing factor.
     * 
     * @return the smoothing factor
     */
    public int getSmoothingFactor() {
        return s;
    }

    protected void computeDefaultAndMaxSmoothingValues(int xSize, int ySize) {
        if (xSize * ySize < MAX_SMOOTHING_VALUE) {
            MAX_SMOOTHING_VALUE = xSize * ySize;
            DEFAULT_SMOOTHING_VALUE = (MAX_SMOOTHING_VALUE - MIN_SMOOTHING_VALUE) / 2;
            s = DEFAULT_SMOOTHING_VALUE;

            if (controlPanel != null) { // check needed for headless environments
                ((SDHControlPanel) controlPanel).spinnerSmoothingFactor.setModel(new SpinnerNumberModel(s,
                        MIN_SMOOTHING_VALUE, MAX_SMOOTHING_VALUE, 1));
            }
        }
    }

    protected void initSmoothingCache(GrowingSOM gsom) throws SOMToolboxException {
        computeDefaultAndMaxSmoothingValues(gsom.getLayer().getXSize(), gsom.getLayer().getYSize());

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Initialization of SDH cache (s=" + MIN_SMOOTHING_VALUE + ",...," + MAX_SMOOTHING_VALUE + ") started.");

        for (int i = 0; i < smoothingCache.length; i++) {
            smoothingCache[i] = new Hashtable<Integer, Histogram>();
        }

        // get max number of winners for each datum

        int numVectors = 0;
        Unit[][] winners = null;
        int[][] xPos = null;
        int[][] yPos = null;
        int[][] zPos = null;
        double[][] dists = null;

        if (gsom.getSharedInputObjects().getDataWinnerMapping() != null) { // we have mapping or file
            dataWinnerMapping = gsom.getSharedInputObjects().getDataWinnerMapping();
            numVectors = dataWinnerMapping.getNumVectors();
            winners = new Unit[numVectors][];
            xPos = new int[numVectors][];
            yPos = new int[numVectors][];
            zPos = new int[numVectors][];
            dists = new double[numVectors][];

            for (int d = 0; d < numVectors; d++) {
                xPos[d] = dataWinnerMapping.getXPos(d);
                yPos[d] = dataWinnerMapping.getYPos(d);
                zPos[d] = dataWinnerMapping.getZPos(d);
                dists[d] = dataWinnerMapping.getDists(d);
            }
            MAX_SMOOTHING_VALUE = dataWinnerMapping.getNumBMUs();
            DEFAULT_SMOOTHING_VALUE = (MAX_SMOOTHING_VALUE - MIN_SMOOTHING_VALUE) / 2;
            s = DEFAULT_SMOOTHING_VALUE;

            if (controlPanel != null) { // check needed for headless environments
                ((SDHControlPanel) controlPanel).spinnerSmoothingFactor.setModel(new SpinnerNumberModel(s,
                        MIN_SMOOTHING_VALUE, MAX_SMOOTHING_VALUE, 1));
            }
        } else if (gsom.getSharedInputObjects().getInputData() != null) { // we have an input vector file
            InputData data = gsom.getSharedInputObjects().getInputData();
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            numVectors = data.numVectors();
            winners = new Unit[numVectors][];
            xPos = new int[numVectors][];
            yPos = new int[numVectors][];
            zPos = new int[numVectors][];
            dists = new double[numVectors][];
            StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors, "Getting winners for datum ", 10);

            for (int d = 0; d < numVectors; d++) {

                UnitDistance[] winnDist = gsom.getLayer().getWinnersAndDistances(data.getInputDatum(d),
                        MAX_SMOOTHING_VALUE);
                winners[d] = new Unit[winnDist.length];
                dists[d] = new double[winnDist.length];
                for (int i = 0; i < winnDist.length; i++) {
                    winners[d][i] = winnDist[i].getUnit();
                    dists[d][i] = winnDist[i].getDistance();
                }

                xPos[d] = new int[MAX_SMOOTHING_VALUE];
                yPos[d] = new int[MAX_SMOOTHING_VALUE];
                zPos[d] = new int[MAX_SMOOTHING_VALUE];
                for (int w = 0; w < MAX_SMOOTHING_VALUE; w++) {
                    xPos[d][w] = winners[d][w].getXPos();
                    yPos[d][w] = winners[d][w].getYPos();
                    zPos[d][w] = winners[d][w].getZPos();
                }
                progressWriter.progress(d + 1);
                // TODO: store the generated data winner mapping for sub-sequent use.
            }
        } else { // throw an exception that will later be handled
            throw new SOMToolboxException("You need to specify at least one out of " + neededInputObjects[0] + " or "
                    + neededInputObjects[1]);
        }

        // create and cache histograms for each smoothing factor
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(MAX_SMOOTHING_VALUE, "Smoothing factor: ");
        for (int svar = MIN_SMOOTHING_VALUE; svar <= MAX_SMOOTHING_VALUE; svar++) {
            DenseDoubleMatrix2D sdhs[] = new DenseDoubleMatrix2D[NUM_VISUALIZATIONS];
            for (int k = 0; k < NUM_VISUALIZATIONS; k++) {
                sdhs[k] = new DenseDoubleMatrix2D(gsom.getLayer().getYSize(), gsom.getLayer().getXSize());
            }

            int cs = 0;
            for (int i = 0; i < svar; i++) {
                cs += svar - i;
            }

            for (int d = 0; d < numVectors; d++) {
                // normalization needed for sdh variant 3
                double max = 0;
                double min = Double.MAX_VALUE;
                for (int w = 0; w < svar; w++) {
                    if (dists[d][w] > max) {
                        max = dists[d][w];
                    }
                    if (dists[d][w] < min) {
                        min = dists[d][w];
                    }
                }

                // create sdh matrix entries
                for (int w = 0; w < svar; w++) {
                    final int row = xPos[d][w];
                    final int column = yPos[d][w];
                    sdhs[0].setQuick(column, row, sdhs[0].getQuick(column, row) + ((double) svar - (double) w) / cs);
                    sdhs[1].setQuick(column, row, sdhs[1].getQuick(column, row) + 1.0d / dists[d][w]);
                    sdhs[2].setQuick(column, row, sdhs[2].getQuick(column, row) + 1.0d - (dists[d][w] - min)
                            / (max - min));
                }
            }

            // determine max and min value of matrices
            double[] maxValues = new double[NUM_VISUALIZATIONS];
            double[] minValues = new double[NUM_VISUALIZATIONS];
            for (int i = 0; i < maxValues.length; i++) {
                maxValues[i] = 0;
                minValues[i] = Double.MAX_VALUE;
            }
            for (int k = 0; k < NUM_VISUALIZATIONS; k++) {
                minValues[k] = sdhs[k].aggregate(Functions.min, Functions.identity);
                maxValues[k] = sdhs[k].aggregate(Functions.max, Functions.identity);
            }

            // normalize sdh matrix
            for (int k = 0; k < NUM_VISUALIZATIONS; k++) {
                VectorTools.normalise(sdhs[k]);
            }

            // set the generated smoothing caches.
            for (int k = 0; k < NUM_VISUALIZATIONS; k++) {
                smoothingCache[k].put(new Integer(svar), new Histogram(sdhs[k]));
            }

            progressWriter.progress();
        }
        winners = null;
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Initialization of SDH cache finished.");
    }

    protected class Histogram {
        public DenseDoubleMatrix2D mh;

        public Histogram(DenseDoubleMatrix2D h) {
            mh = h;
        }
    }

    /**
     * A control panel extending the generic {@link AbstractBackgroundImageVisualizer.VisualizationControlPanel}, adding
     * additionally a {@link JSpinner} for controlling the smoothing factor.
     * 
     * @author Rudolf Mayer
     */
    public class SDHControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        /**
         * The {@link JSpinner} controlling the smoothing factor.
         */
        public JSpinner spinnerSmoothingFactor = null;

        private SDHControlPanel() {
            super("SDH Control");
            spinnerSmoothingFactor = new JSpinner(new SpinnerNumberModel(getSmoothingFactor(), MIN_SMOOTHING_VALUE,
                    MAX_SMOOTHING_VALUE, 1));
            spinnerSmoothingFactor.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    s = (Integer) spinnerSmoothingFactor.getValue();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            JPanel smoothingFactorPanel = new JPanel();
            smoothingFactorPanel.add(new JLabel("Smoothing factor: "));
            smoothingFactorPanel.add(spinnerSmoothingFactor);
            add(smoothingFactorPanel, c);
        }
    }

    /**
     * Overrides {@link AbstractBackgroundImageVisualizer#needsAdditionalFiles()}, as we need only one of the two
     * possible input files to create this visualisation. If the data winner mapping is present, it will be used
     * directly, otherwise it can be created from the input vectors.
     */
    @Override
    public String[] needsAdditionalFiles() {
        String[] dataFiles = super.needsAdditionalFiles();
        if (dataFiles.length < 2) { // we need only one of the files
            return null;
        } else {
            return dataFiles;
        }
    }

    /**
     * Sets the smoothing factor.
     * 
     * @param smoothingFactor the new smoothing factor
     */
    public void setSmoothingFactor(int smoothingFactor) {
        s = smoothingFactor;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String getHTMLVisualisationControl(Map params) {
        StringBuffer b = new StringBuffer();
        b.append("Smoothing factor:\n");
        b.append("<select name=\"smoothingFactor\">\n");
        for (int i = MIN_SMOOTHING_VALUE; i < MAX_SMOOTHING_VALUE; i++) {
            b.append("<option value=\"" + i + "\"");
            if (params.get("smoothingFactor") != null && params.get("smoothingFactor").equals(String.valueOf(i))) {
                b.append(" selected");
            }
            b.append(">" + i + "</option>\n");
        }
        b.append("</select>\n");
        return b.toString();
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width,
            int height) throws SOMToolboxException {
        return getVisualizationFlavours(variantIndex, gsom, width, height, MAX_SMOOTHING_VALUE);
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width,
            int height, int maxFlavours) throws SOMToolboxException {
        HashMap<String, BufferedImage> result = new HashMap<String, BufferedImage>();

        // we need to set the default values, otherwise in the first iteration, we won't be using smoothing factor 1,
        // but the computed"optimal" value
        initSmoothingCache(gsom);

        int currentSF = getSmoothingFactor();
        int count = 0;
        for (int i = 1; i <= MAX_SMOOTHING_VALUE;) {
            count++;
            if (variantIndex == 2 && i == 1) {
                continue; // smoothing factor 1 doesn't work with normalised version, just skip the image
            }
            String key = String.format("_smooth%d", i);

            BufferedImage val = getVisualization(variantIndex, i, gsom, width, height);
            result.put(key, val);
            if (count >= maxFlavours) {
                break;
            }
            if (i < 20) {
                i += 1;
            }
            if (20 <= i && i < 50) {
                i += 2;
            } else if (50 <= i) {
                i += 5;
            }
        }
        setSmoothingFactor(currentSF);
        return result;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width,
            int height, Map<String, String> flavourParameters) throws SOMToolboxException {
        // FIXME: Implement Method
        return getVisualizationFlavours(variantIndex, gsom, width, height);
    }

}
