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

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * This Visualizer provides two variants of the U-Matrix.
 * <ol>
 * <li>Implementation of the classic U-Matrix as described in <i><b>Ultsch, A., and Siemon, H.P.</b> Kohonen's Self
 * Organizing Feature Maps for Exploratory Data Analysis. In Proc. Intern. Neural Networks, 1990, pp. 305-308, Kluwer
 * Academic Press, Paris, France.</i>.</li>
 * <li>Same as 1., but D-Matrix Values only.</li>
 * </ol>
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: UMatrix.java 3852 2010-10-12 13:32:25Z mayer $
 */
public class UMatrix extends AbstractMatrixVisualizer implements BackgroundImageVisualizer {

    public static final String[] UMATRIX_SHORT_NAMES = new String[] { "UMatrix", "DMatrix" };

    public UMatrix() {
        NUM_VISUALIZATIONS = 2;
        VISUALIZATION_NAMES = new String[] { "U-Matrix", "D-Matrix", };
        VISUALIZATION_SHORT_NAMES = UMATRIX_SHORT_NAMES;
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Implementation of the classic U-Matrix as described in \"Ultsch, A., and Siemon, H.P.\n"
                        + "Kohonen's Self Organizing Feature Maps for Exploratory Data Analysis.\n"
                        + "In Proc.Intern. Neural Networks, 1990, pp. 305-308, Kluwer Academic Press, Paris, France.\"",//
                "D-Matrix Values only", };
        setInterpolate(false); // by default, the U-Matrix is not interpolated
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        checkVariantIndex(index, getClass());
        if (index == 0) {
            return createOriginalUMatrix(gsom, width, height);
        } else {
            return createOriginalDMatrix(gsom, width, height);
        }
    }

    /**
     * Creates an image of the D-Matrix visualisation.
     * 
     * @param gsom the GrowingSOM to generate the visualisation for
     * @param width the desired width of the image, in pixels
     * @param height the desired height of the image, in pixels.
     * @return an image for this visualisation.
     */
    private BufferedImage createOriginalDMatrix(GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        DoubleMatrix2D dmatrix = createUMatrix(gsom).viewStrides(2, 2);
        // System.out.println(new cern.colt.matrix.doublealgo.Formatter("%1.4f").toString(dmatrix));
        VectorTools.normalise(dmatrix);
        return createImage(gsom, dmatrix, width, height, interpolate);
    }

    /**
     * Creates an image of the U-Matrix visualisation.
     * 
     * @param gsom the GrowingSOM to generate the visualisation for
     * @param width the desired width of the image, in pixels
     * @param height the desired height of the image, in pixels.
     * @return an image for this visualisation.
     */
    private BufferedImage createOriginalUMatrix(GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        DoubleMatrix2D umatrix = createUMatrix(gsom);
        // System.out.println(new cern.colt.matrix.doublealgo.Formatter("%1.4f").toString(umatrix));
        VectorTools.normalise(umatrix);
        return createImage(gsom, umatrix, width, height, interpolate);
    }

    /**
     * Creates the height matrix.
     * 
     * @param gsom the GrowingSOM to generate the visualisation for
     * @return a matrix containing heights for each coordinate.
     */
    public DoubleMatrix2D createUMatrix(GrowingSOM gsom) {
        GrowingLayer layer = gsom.getLayer();
        DistanceMetric metric = layer.getMetric();
        int umatW = layer.getXSize() * 2 - 1;
        int umatH = layer.getYSize() * 2 - 1;
        DoubleMatrix2D umatrix = DoubleFactory2D.dense.make(umatH, umatW, -1);

        // calc horizontal, vertical and diagonal distances between units
        for (int row = 0; row < umatH; row++) {
            for (int col = 0; col < umatW; col++) {
                try {
                    if (col % 2 != 0 && row % 2 == 0) { // horizontal
                        umatrix.set(row, col, metric.distance(layer.getUnit((col - 1) / 2, row / 2).getWeightVector(),
                                layer.getUnit((col - 1) / 2 + 1, row / 2).getWeightVector()));
                    } else if (col % 2 == 0 && row % 2 != 0) { // vertical
                        umatrix.set(row, col, metric.distance(layer.getUnit(col / 2, (row - 1) / 2).getWeightVector(),
                                layer.getUnit(col / 2, (row - 1) / 2 + 1).getWeightVector()));
                    } else if (col % 2 != 0 && row % 2 != 0) { // diagonal
                        double d1 = metric.distance(layer.getUnit((col - 1) / 2, (row - 1) / 2).getWeightVector(),
                                layer.getUnit((col - 1) / 2 + 1, (row - 1) / 2 + 1).getWeightVector());
                        double d2 = metric.distance(layer.getUnit((col - 1) / 2, (row - 1) / 2 + 1).getWeightVector(),
                                layer.getUnit((col - 1) / 2 + 1, (row - 1) / 2).getWeightVector());
                        umatrix.set(row, col, (d1 + d2) / (2 * Math.sqrt(2)));
                    }
                } catch (MetricException me) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(me.getMessage());
                    System.exit(-1);
                } catch (LayerAccessException lae) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(lae.getMessage());
                    System.exit(-1);
                }
            }
        }

        // interpolate based on surrounding distances between units (median)
        for (int row = 0; row < umatH; row += 2) {
            for (int col = 0; col < umatW; col += 2) {
                if (row == 0 && col == 0) { // upper left unit
                    umatrix.set(row, col, VectorTools.median(umatrix.get(row + 1, col), umatrix.get(row, col + 1)));
                } else if (col == umatW - 1 && row == 0) { // upper right unit
                    umatrix.set(row, col, VectorTools.median(umatrix.get(row, col - 1), umatrix.get(row + 1, col)));
                } else if (col == 0 && row == umatH - 1) { // lower left unit
                    umatrix.set(row, col, VectorTools.median(umatrix.get(row, col + 1), umatrix.get(row - 1, col)));
                } else if (col == umatW - 1 && row == umatH - 1) { // lower right unit
                    umatrix.set(row, col, VectorTools.median(umatrix.get(row, col - 1), umatrix.get(row - 1, col)));
                } else if (col == 0) { // left border
                    umatrix.set(row, col, VectorTools.median(umatrix.get(row - 1, col), umatrix.get(row + 1, col),
                            umatrix.get(row, col + 1)));
                } else if (col == umatW - 1) { // right border
                    umatrix.set(row, col, VectorTools.median(umatrix.get(row - 1, col), umatrix.get(row + 1, col),
                            umatrix.get(row, col - 1)));
                } else if (row == 0) { // top border
                    umatrix.set(row, col, VectorTools.median(umatrix.get(row, col - 1), umatrix.get(row, col + 1),
                            umatrix.get(row + 1, col)));
                } else if (row == umatH - 1) { // bottom border
                    umatrix.set(row, col, VectorTools.median(umatrix.get(row, col - 1), umatrix.get(row, col + 1),
                            umatrix.get(row - 1, col)));
                } else { // middle unit
                    umatrix.set(row, col, VectorTools.median(umatrix.get(row, col - 1), umatrix.get(row, col + 1),
                            umatrix.get(row - 1, col), umatrix.get(row + 1, col)));
                }
            }
        }
        return umatrix;
    }

    @Override
    public String getPreferredPaletteName() {
        return "Matlab SOMToolbox UMatrix";
    }

}
