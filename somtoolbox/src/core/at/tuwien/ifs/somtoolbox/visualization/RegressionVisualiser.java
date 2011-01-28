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

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.SOMLibRegressInformation;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * @author Rudolf Mayer
 * @version $Id: RegressionVisualiser.java 3763 2010-08-20 13:27:02Z mayer $
 */
public class RegressionVisualiser extends AbstractMatrixVisualizer implements BackgroundImageVisualizer {

    public RegressionVisualiser() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Regression Visualiser" };
        VISUALIZATION_SHORT_NAMES = new String[] { "Regression" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "" };
        neededInputObjects = new String[] { SOMVisualisationData.REGRESS_INFORMATION };
        setInterpolate(false);
    }

    @Override
    public String getPreferredPaletteName() {
        return "Redscale32";
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
        checkVariantIndex(index, getClass());

        SOMLibRegressInformation regressInfo = gsom.getSharedInputObjects().getSOMLibRegressInformation();

        int xSize = gsom.getLayer().getXSize();
        int ySize = gsom.getLayer().getYSize();

        double unitWidth = (double) width / xSize;
        double unitHeight = (double) height / ySize;
        unitWidth = (int) unitWidth;
        unitHeight = (int) unitHeight;

        DoubleMatrix2D matrix = DoubleFactory2D.dense.make(ySize, xSize);
        double maxValue = Double.MIN_VALUE;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                Unit u = gsom.getLayer().getUnit(x, y);
                // System.out.println(u);
                double unitPrediction = 0.0;
                if (u.getMappedInputNames() != null) {
                    for (String string : u.getMappedInputNames()) {
                        double prediction = regressInfo.getPrediction(string);
                        // System.out.println(string + ": " + prediction);
                        unitPrediction += prediction;
                    }
                    unitPrediction = unitPrediction / u.getMappedInputNames().length;
                    // System.out.println("unit pred: " + unitPrediction);
                    if (unitPrediction > maxValue) {
                        maxValue = unitPrediction;
                    }
                    // System.out.println();
                } else {
                    // FIXME: not sure how to treat empty units. should they actually have a value of 0 (might screw
                    // colour palette ranges !) ?
                    unitPrediction = regressInfo.getMinPrediction();
                }
                matrix.set(y, x, unitPrediction);
            }
        }
        // System.out.println(matrix);
        VectorTools.normalise(matrix);
        // System.out.println(matrix);
        return super.createImage(gsom, matrix, width, height, interpolate);
    }

}
