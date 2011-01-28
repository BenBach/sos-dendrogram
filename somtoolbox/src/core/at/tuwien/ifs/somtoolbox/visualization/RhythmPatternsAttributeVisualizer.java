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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.lang.ArrayUtils;

import flanagan.interpolation.BiCubicSplineFast;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: RhythmPatternsAttributeVisualizer.java 3874 2010-11-02 14:14:38Z mayer $
 */
public class RhythmPatternsAttributeVisualizer extends AbstractMatrixVisualizer implements BackgroundImageVisualizer,
        ActionListener {

    private static final String LOW_FREQ_DOM = "low freq dom";

    private static final String NON_AGGRESSIVE = "non-aggressive";

    private static final String BASS = "bass";

    private static final String MAX_FLUX = "max flux";

    private static final String[] attributeTypes = { MAX_FLUX, BASS, LOW_FREQ_DOM, NON_AGGRESSIVE };

    private double[][] maxfluxValues = null;

    private double[][] bassValues = null;

    private double[][] nonaggValues = null;

    private double[][] lfdValues = null;

    private String selectedAttributeType = MAX_FLUX;

    public RhythmPatternsAttributeVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Rhythm Patterns Attributes" };
        VISUALIZATION_SHORT_NAMES = new String[] { "RPAttributes" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "max flux, bass, non-aggressive, low frequencies dominant" };

        // input vector file is only needed for determining dimensions of RP matrix,
        // not for actual vis calcualtion
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR };

        reversePalette();

        // don't initialise the control panel if we have no graphics environment (e.g. in server applications)
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                controlPanel = new RPAControlPanel(this);
            } catch (Throwable e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Caught runtime exception/error during graphics init: " + e.getMessage()
                                + "\n Headless environment? " + GraphicsEnvironment.isHeadless());
            }
        }

    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return getCacheKey(gsom, index, width, height, selectedAttributeType);
    }

    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height, String attributeType) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR + "attribute:"
                + attributeType;
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {

        // FIXME: this cache of the matrices doesn't take into account any changes in the width & height!
        if (maxfluxValues == null && bassValues == null && nonaggValues == null && lfdValues == null) {
            initVisualizationMatrices(gsom, width, height);
        }

        if (cache.get(getCacheKey(gsom, index, width, height)) == null) {
            // create all four images at once
            cache.put(getCacheKey(gsom, 0, width, height, MAX_FLUX), createImage(maxfluxValues, width, height,
                    interpolate));
            cache.put(getCacheKey(gsom, 0, width, height, BASS), createImage(bassValues, width, height, interpolate));
            cache.put(getCacheKey(gsom, 0, width, height, NON_AGGRESSIVE), createImage(nonaggValues, width, height,
                    interpolate));
            cache.put(getCacheKey(gsom, 0, width, height, LOW_FREQ_DOM), createImage(lfdValues, width, height,
                    interpolate));
        }

        return cache.get(getCacheKey(gsom, index, width, height));
    }

    private void initVisualizationMatrices(GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        int nBark = -1;
        int nModFreq = -1;

        InputData inputVectors = inputObjects.getInputData();

        if (inputVectors == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Input vector file not available - Cannot determine dimensions of Rhythm Pattern! - Guessing default values.");
        } else {
            // those 2 methods return -1 if $DATA_DIM header line is not present in input vector file
            nBark = inputVectors.getFeatureMatrixRows();
            nModFreq = inputVectors.getFeatureMatrixColumns();
        }

        if (nBark == -1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Number of Bark bands for Rhythm Patterns attribute visualization could not be determined. Assuming 24.");
            nBark = 24;
        }
        if (nModFreq == -1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Number of Modulation frequencies for Rhythm Patterns attribute visualization could not be determined. Assuming 60.");
            nModFreq = 60;
        }

        if (gsom.getLayer().getDim() != nBark * nModFreq) {
            // try to determine the number of bark bands by assuming 60 modulation frequencies
            // rule of thumb: has to be a integer number, and 10 <= bands <=25
            if (gsom.getLayer().getDim() % nModFreq == 0 && gsom.getLayer().getDim() / nModFreq <= 25
                    && gsom.getLayer().getDim() / nModFreq >= 10) {
                int nBarkOld = nBark;
                nBark = gsom.getLayer().getDim() / nModFreq;
                System.out.println("Vector dimension (" + gsom.getLayer().getDim() + ") "
                        + "did not match RP dimension (" + nBarkOld + "x" + nModFreq
                        + "); assuming 60 modulation frequencies, calculated " + nBark + " bark bands, continuing.");
            } else {
                throw new SOMToolboxException("Initialization of Rhythm Patterns Attributes Visualization failed, "
                        + "because vector dimension (" + gsom.getLayer().getDim() + ") "
                        + "does not match RP dimension (" + nBark + "x" + nModFreq + ").");
            }
        }

        int mapXSize = gsom.getLayer().getXSize();
        int mapYsize = gsom.getLayer().getYSize();

        maxfluxValues = new double[mapXSize][mapYsize];
        bassValues = new double[mapXSize][mapYsize];
        nonaggValues = new double[mapXSize][mapYsize];
        lfdValues = new double[mapXSize][mapYsize];

        /*
         * max flux: highest value in the rhythm pattern
         */
        for (int y = 0; y < mapYsize; y++) {
            for (int x = 0; x < mapXSize; x++) {
                double max = 0;
                double[] vec = null;
                vec = gsom.getLayer().getUnit(x, y).getWeightVector();
                for (int i = 0; i < gsom.getLayer().getDim(); i++) {
                    if (vec[i] > max) {
                        max = vec[i];
                    }
                }
                maxfluxValues[x][y] = max;
            }
        }

        /*
         * bass: sum of the values in the two lowest frequency bands (Bark 1-2) with a modulation frequency higher than 1Hz.
         */
        for (int y = 0; y < mapYsize; y++) {
            for (int x = 0; x < mapXSize; x++) {
                bassValues[x][y] = 0;
                double[] vec = null;
                vec = gsom.getLayer().getUnit(x, y).getWeightVector();
                for (int bark = 0; bark <= 1; bark++) {
                    for (int i = 6; i <= nModFreq; i++) {
                        bassValues[x][y] += vec[bark * nModFreq + i];
                    }
                }
            }
        }

        /*
         * non_aggressiveness: the ratio of the sum of values modulation frequencies below 0.5Hz and Bark greater or equal 3, compared to the sum of
         * all.
         */

        /*
         * // normalize weight vectors by attribute double[][][] normWeights = new double[gsom.xSize()][gsom.ySize()][gsom.dim()]; for (int d=0;
         * d<gsom.dim(); d++) { // for each dimension // get max and min values of dimension double maxdimval = 0; double mindimval =
         * Double.MAX_VALUE; Unit[] units = gsom.getAllUnits(); for (int u=0; u<units.length; u++) { if (units[u].weightVector()[d] > maxdimval) {
         * maxdimval = units[u].weightVector()[d]; } if (units[u].weightVector()[d] < mindimval) { mindimval = units[u].weightVector()[d]; } } for
         * (int yy=0; yy<gsom.ySize(); yy++) { for (int xx=0; xx<gsom.xSize(); xx++) { normWeights[xx][yy][d] =
         * (gsom.getUnit(xx,yy).weightVector()[d]-mindimval)/(maxdimval-mindimval); } } }
         */

        // normalize weight vector elements to interval [0-1]
        double[][][] normWeights = new double[mapXSize][mapYsize][gsom.getLayer().getDim()];
        for (int y = 0; y < mapYsize; y++) {
            for (int x = 0; x < mapXSize; x++) {
                double maxval = 0;
                double minval = Double.MAX_VALUE;
                double[] vec = null;
                vec = gsom.getLayer().getUnit(x, y).getWeightVector();
                for (double element : vec) {
                    if (element > maxval) {
                        maxval = element;
                    }
                    if (element < minval) {
                        minval = element;
                    }
                }
                for (int i = 0; i < vec.length; i++) {
                    normWeights[x][y][i] = (vec[i] - minval) / (maxval - minval);
                }
            }
        }

        for (int y = 0; y < mapYsize; y++) {
            for (int x = 0; x < mapXSize; x++) {
                nonaggValues[x][y] = 0;
                for (int bark = 2; bark < nBark; bark++) {
                    for (int i = 0; i <= 2; i++) {
                        nonaggValues[x][y] += normWeights[x][y][bark * nModFreq + i];
                    }
                }
            }
        }
        normWeights = null;

        /*
         * low frequencies dominant: ratio between the sum of the values in the highest 5 and lowest 5 frequency bands
         */

        for (int y = 0; y < mapYsize; y++) {
            for (int x = 0; x < mapXSize; x++) {
                lfdValues[x][y] = 0;
                double[] vec = null;
                vec = gsom.getLayer().getUnit(x, y).getWeightVector();
                // low value
                double lowval = 0;
                for (int bark = 0; bark <= 4; bark++) {
                    for (int i = 0; i < nModFreq; i++) {
                        lowval = vec[bark * nModFreq + i];
                    }
                }
                // high value
                double highval = 0;
                for (int bark = nBark - 5; bark < nBark; bark++) {
                    for (int i = 0; i < nModFreq; i++) {
                        highval += vec[bark * nModFreq + i];
                    }
                }
                lfdValues[x][y] = lowval / highval;
            }
        }

        /*
         * for (int c=paletteSize-1; c>=0; c--) { int r = 0+(int)(c((double)255/((double)paletteSize))); int g =
         * 0+(int)(c((double)255/((double)paletteSize))); int b = 0+(int)(c((double)255/((double)paletteSize))); palette[c] = new Color(r,g,b); }
         */

        /* try segmentation begin */
        /*
         * int rgbval[] = { 150,150,150 }; WritableRaster wraster = maxfluxImage.getRaster(); for (int y=0; y < maxfluxImage.getHeight(); y++) { for
         * (int x=0; x < maxfluxImage.getWidth(); x++) { for(int band = 0; band < 3; band++) { if ((wraster.getSample(x, y, band) > rgbval[band]))
         * wraster.setSample(x, y, band, 0); else wraster.setSample(x, y, band, 255); } } }
         */
        /* try segmentation end */

    }

    private BufferedImage createImage(double[][] values, int width, int height, boolean interpolate) {
        // FIXME: this should be unified with createImage in AbstractMatrixVisualizer
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        drawBackground(width, height, g);

        int xSize = values.length;
        int ySize = values[0].length;
        int unitWidth = width / xSize;
        int unitHeight = height / ySize;

        // normalization of values
        double max = 0;
        double min = Double.MAX_VALUE;
        for (int j = 0; j < ySize; j++) {
            for (int i = 0; i < xSize; i++) {
                if (values[i][j] > max) {
                    max = values[i][j];
                }
                if (values[i][j] < min) {
                    min = values[i][j];
                }
            }
        }

        int ci = 0;

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

        double[][] valuesWithBorders = new double[xSize + 2][ySize + 2];
        valuesWithBorders[0][0] = Math.max(values[0][0] - (values[1][1] - values[0][0]) / 2, 0d); // top-left corner
        valuesWithBorders[xSize + 1][0] = Math.max(values[xSize - 1][0] - (values[xSize - 2][1] - values[xSize - 1][0])
                / 2, 0d); // top-right
        // corner
        valuesWithBorders[0][ySize + 1] = Math.max(values[0][ySize - 1] - (values[1][ySize - 2] - values[0][ySize - 1])
                / 2, 0d); // bottom-left
        // corner
        valuesWithBorders[xSize + 1][ySize + 1] = Math.max(values[xSize - 1][ySize - 1]
                - (values[xSize - 2][ySize - 2] - values[xSize - 1][ySize - 1]) / 2, 0d); // bottom-right corner
        for (int x = 1; x < xSize + 1; x++) {
            valuesWithBorders[x][0] = Math.max(values[x - 1][0] - (values[x - 1][1] - values[x - 1][0]) / 2, 0d); // top
            // row
            valuesWithBorders[x][ySize + 1] = Math.max(values[x - 1][ySize - 1]
                    - (values[x - 1][ySize - 2] - values[x - 1][ySize - 1]) / 2, 0d); // bottom
            // row
        }
        for (int y = 1; y < ySize + 1; y++) {
            valuesWithBorders[0][y] = Math.max(values[0][y - 1] - (values[1][y - 1] - values[0][y - 1]) / 2, 0d); // left
            // column
            valuesWithBorders[xSize + 1][y] = Math.max(values[xSize - 1][y - 1]
                    - (values[xSize - 2][y - 1] - values[xSize - 1][y - 1]) / 2, 0d); // right
            // column
        }
        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                valuesWithBorders[x + 1][y + 1] = values[x][y];
            }
        }

        if (interpolate) {
            BiCubicSplineFast bcs = new BiCubicSplineFast(x1, x2, valuesWithBorders);
            // bcs.calcDeriv();

            int stepSize = Math.max(5000, height * width / 500);
            StdErrProgressWriter progress = new StdErrProgressWriter(height * width,
                    "Creating interpolated matrix image, pixel ", stepSize);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    ci = (int) Math.round((bcs.interpolate(x + 0.5, y + 0.5) - min) / (max - min)
                            * palette.maxColourIndex());
                    g.setPaint(palette.getColorConstrained(ci));
                    g.fill(new Rectangle(x, y, 1, 1));
                    progress.progress();
                }
            }
        } else {
            for (int y = 0; y < ySize; y++) {
                for (int x = 0; x < xSize; x++) {
                    ci = (int) Math.round((valuesWithBorders[x][y] - min) / (max - min) * palette.maxColourIndex());
                    g.setPaint(palette.getColorConstrained(ci));
                    g.fill(new Rectangle(x * unitWidth, y * unitWidth, unitWidth, unitHeight));
                }
            }

        }
        return res;
    }

    public String getSelectedAttributeType() {
        return selectedAttributeType;
    }

    public void selectNextAttributeType() {
        int nextIndex = ArrayUtils.indexOf(attributeTypes, selectedAttributeType) + 1;
        if (nextIndex == getNumberOfAttributeTypes()) {
            nextIndex = 0;
        }
        selectedAttributeType = attributeTypes[nextIndex];
    }

    public int getNumberOfAttributeTypes() {
        return 4;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        selectedAttributeType = ((JRadioButton) e.getSource()).getActionCommand();
        if (visualizationUpdateListener != null) {
            visualizationUpdateListener.updateVisualization();
        }
    }

    private class RPAControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        private JRadioButton rbMaxFlux = null;

        private JRadioButton rbBass = null;

        private JRadioButton rbNonAggressive = null;

        private JRadioButton rbLowFreqDom = null;

        private ButtonGroup bg = null;

        private RPAControlPanel(RhythmPatternsAttributeVisualizer rpa) {
            super("Rhythm Patterns Attribute Control");

            // FIXME: add tooltips to buttons that explain the attributes visualised
            rbMaxFlux = new JRadioButton(MAX_FLUX, true);
            rbMaxFlux.addActionListener(rpa);
            rbMaxFlux.setActionCommand(MAX_FLUX);

            rbBass = new JRadioButton(BASS, false);
            rbBass.addActionListener(rpa);
            rbBass.setActionCommand(BASS);

            rbNonAggressive = new JRadioButton(NON_AGGRESSIVE, false);
            rbNonAggressive.addActionListener(rpa);
            rbNonAggressive.setActionCommand(NON_AGGRESSIVE);

            rbLowFreqDom = new JRadioButton(LOW_FREQ_DOM, false);
            rbLowFreqDom.addActionListener(rpa);
            rbLowFreqDom.setActionCommand(LOW_FREQ_DOM);

            bg = new ButtonGroup();
            bg.add(rbMaxFlux);
            bg.add(rbBass);
            bg.add(rbNonAggressive);
            bg.add(rbLowFreqDom);

            // JPanel rpaPanel = new JPanel(new GridLayout(2, 2));
            JPanel rpaPanel = new JPanel(new GridBagLayout());

            GridBagConstraints c2 = new GridBagConstraints();
            c2.gridx = GridBagConstraints.RELATIVE;
            c2.gridy = 0;
            c2.fill = GridBagConstraints.HORIZONTAL;

            rpaPanel.add(rbMaxFlux, c2);
            rpaPanel.add(rbNonAggressive, c2);
            c2.gridy = c2.gridy + 1;
            rpaPanel.add(rbBass, c2);
            rpaPanel.add(rbLowFreqDom, c2);
            add(rpaPanel, c);
        }

    }

    /** Saves all flavours of RP attribute types (#attributeTypes). */
    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width,
            int height) throws SOMToolboxException {
        HashMap<String, BufferedImage> hashMap = new HashMap<String, BufferedImage>(attributeTypes.length, 1); // optimal
        // size
        for (String attributeType : attributeTypes) {
            hashMap.put("_" + getSelectedAttributeType(), getVisualization(variantIndex, gsom, width, height));
            selectNextAttributeType();
        }
        return hashMap;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width,
            int height, int maxFlavours) throws SOMToolboxException {
        // we can't really select any subset of these very distinctive flavours.
        if (maxFlavours != attributeTypes.length) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "RhythmPatternsAttributeVisualizer will always generate the " + attributeTypes.length
                            + " distinctive flavours, ignoring specified max value of " + maxFlavours + ".");
        }
        return getVisualizationFlavours(variantIndex, gsom, width, height);
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width,
            int height, Map<String, String> flavourParameters) throws SOMToolboxException {
        // FIXME: implement this
        return super.getVisualizationFlavours(variantIndex, gsom, width, height, flavourParameters);
    }
}
