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
import java.util.ArrayList;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * This Visualizer provides the Gap visualiser, as described in <i><b>Samuel Kaski, Janne Nikkilä, and Teuvo
 * Kohonen.</b> Methods for exploratory cluster analysis. In Proceedings of the International Conference on Advances in
 * Infrastructure for Electronic Business, Science, and Education on the Internet, L'Aquila, July 31–August 6. Scuola
 * Superiore G. Reiss Romoli, 2000, L'Aquila, July 31-August 6. ISBN 88-85280-52-8</i>
 * 
 * @author Rudolf Mayer
 * @version $Id: GapVisualiser.java 3869 2010-10-21 15:56:09Z mayer $
 */
public class GapVisualiser extends UMatrix {

    public double dataPercentage = 0.8d;

    public double neighbourHood = 0.1d;

    public GapVisualiser() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Gap-Matrix" };
        VISUALIZATION_SHORT_NAMES = new String[] { "Gap" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Implementation of the Gap visualisation as described in \"Samuel Kaski, Janne Nikkilä, and Teuvo Kohonen\n"
                + "Methods for exploratory cluster analysis. In Proceedings of SSGRR 2000.\"" };
        setInterpolate(false); // by default, the Gap-Matrix is not interpolated
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR };
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        checkVariantIndex(index, getClass());
        return createGapMatrix(gsom, width, height);
    }

    private BufferedImage createGapMatrix(GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        AbstractSOMLibSparseInputData inputData = (AbstractSOMLibSparseInputData) gsom.getSharedInputObjects().getInputData();
        GrowingLayer layer = gsom.getLayer();
        DistanceMetric metric = layer.getMetric();
        int umatW = layer.getXSize() * 2 - 1;
        int umatH = layer.getYSize() * 2 - 1;
        try {
            double[][][] g = new double[layer.getXSize()][layer.getYSize()][];
            for (int row = 0; row < layer.getYSize(); row++) {
                for (int col = 0; col < layer.getXSize(); col++) {

                    final double[] m_i = layer.getUnit(col, row).getWeightVector();

                    // find all the name of the inputs mapped to the neighbouring units
                    ArrayList<String> mappedItems = new ArrayList<String>();
                    // consider a radius of 1.5, which means an 8-unit neighbourhood
                    final ArrayList<Unit> neighbouringUnits = layer.getNeighbouringUnits(col, row, 1.5);
                    for (Unit unit : neighbouringUnits) {
                        mappedItems.addAll(unit.getMappedInputNamesAsList());
                    }

                    // retrieve the inputs to the names, and sort them by their distance to the model vector
                    InputDatum[] nearestN = inputData.getByNameDistanceSorted(m_i, mappedItems, metric);

                    // take 80% of those
                    int cut = (int) Math.round(nearestN.length * dataPercentage);
                    double[][] vectors = new double[cut][];
                    for (int i = 0; i < vectors.length; i++) {
                        vectors[i] = nearestN[i].getVector().toArray();
                    }

                    double[] c_i;
                    if (vectors.length > 0) {
                        c_i = VectorTools.meanVector(vectors); // Equation (5)
                    } else {
                        c_i = m_i;
                    }
                    g[col][row] = VectorTools.subtract(m_i, c_i); // Equation (4)
                }
            }

            // System.out.println(ArrayUtils.toString(g));
            // System.out.println(StringUtils.toString(g));

            DoubleMatrix2D matrix = DoubleFactory2D.dense.make(umatH, umatW, -1);

            boolean first = true;

            // calc horizontal, vertical and diagonal distances between units
            if (first) {
                for (int row = 0; row < layer.getYSize(); row++) {
                    for (int col = 0; col < layer.getXSize(); col++) {
                        try {
                            double[] g_i = g[col][row];
                            if (col + 1 < layer.getXSize()) { // rightwards
                                matrix.set(row * 2, col * 2 + 1, metric.distance(g_i, g[col + 1][row]));
                                if (row + 1 < layer.getYSize()) { // right-downwards
                                    matrix.set(row * 2 + 1, col * 2 + 1, metric.distance(g_i, g[col + 1][row + 1]));
                                }
                            }
                            if (row + 1 < layer.getYSize()) { // downwards
                                matrix.set(row * 2 + 1, col * 2, metric.distance(g_i, g[col][row + 1]));
                            }
                        } catch (MetricException me) {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(me.getMessage());
                            System.exit(-1);
                        }
                    }
                }
            } else {
                for (int row = 0; row < umatH; row++) {
                    for (int col = 0; col < umatW; col++) {
                        try {
                            int x = (col - 1) / 2;
                            if (col % 2 != 0 && row % 2 == 0) { // horizontal
                                matrix.set(row, col, metric.distance(g[x][row / 2], g[x + 1][row / 2]));
                            } else {
                                int y = (row - 1) / 2;
                                if (col % 2 == 0 && row % 2 != 0) { // vertical
                                    matrix.set(row, col, metric.distance(g[col / 2][y], g[col / 2][y + 1]));
                                } else if (col % 2 != 0 && row % 2 != 0) { // diagonal
                                    double d1 = metric.distance(g[x][y], g[x + 1][y + 1]);
                                    double d2 = metric.distance(g[x][y + 1], g[x + 1][y]);
                                    matrix.set(row, col, (d1 + d2) / (2 * Math.sqrt(2)));
                                }
                            }
                        } catch (MetricException me) {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(me.getMessage());
                            System.exit(-1);
                        }
                    }
                }
            }

            // System.out.println(matrix);

            // interpolate based on surrounding distances between units (median)
            for (int row = 0; row < umatH; row += 2) {
                for (int col = 0; col < umatW; col += 2) {
                    if (row == 0 && col == 0) { // upper left unit
                        matrix.set(row, col, VectorTools.median(matrix.get(row + 1, col), matrix.get(row, col + 1)));
                    } else if (col == umatW - 1 && row == 0) { // upper right unit
                        matrix.set(row, col, VectorTools.median(matrix.get(row, col - 1), matrix.get(row + 1, col)));
                    } else if (col == 0 && row == umatH - 1) { // lower left unit
                        matrix.set(row, col, VectorTools.median(matrix.get(row, col + 1), matrix.get(row - 1, col)));
                    } else if (col == umatW - 1 && row == umatH - 1) { // lower right unit
                        matrix.set(row, col, VectorTools.median(matrix.get(row, col - 1), matrix.get(row - 1, col)));
                    } else if (col == 0) { // left border
                        matrix.set(row, col, VectorTools.median(matrix.get(row - 1, col), matrix.get(row + 1, col),
                                matrix.get(row, col + 1)));
                    } else if (col == umatW - 1) { // right border
                        matrix.set(row, col, VectorTools.median(matrix.get(row - 1, col), matrix.get(row + 1, col),
                                matrix.get(row, col - 1)));
                    } else if (row == 0) { // top border
                        matrix.set(row, col, VectorTools.median(matrix.get(row, col - 1), matrix.get(row, col + 1),
                                matrix.get(row + 1, col)));
                    } else if (row == umatH - 1) { // bottom border
                        matrix.set(row, col, VectorTools.median(matrix.get(row, col - 1), matrix.get(row, col + 1),
                                matrix.get(row - 1, col)));
                    } else { // middle unit
                        matrix.set(row, col, VectorTools.median(matrix.get(row, col - 1), matrix.get(row, col + 1),
                                matrix.get(row - 1, col), matrix.get(row + 1, col)));
                    }
                }
            }

            VectorTools.normalise(matrix);
            return createImage(gsom, matrix, width, height, interpolate);
        } catch (LayerAccessException e) {
            e.printStackTrace();
            return null;
        }

    }
}
