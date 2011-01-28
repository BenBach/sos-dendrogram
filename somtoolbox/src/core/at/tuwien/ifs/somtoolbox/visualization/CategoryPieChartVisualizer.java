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
import java.awt.image.BufferedImage;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.PieChartPNode;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.ImageUtils;

/**
 * Visualises category information, such as class assignement, via pie charts. An alternative mode encodes the hits on
 * the unit in the pie size.
 * 
 * @author Rudolf Mayer
 * @version $Id: CategoryPieChartVisualizer.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class CategoryPieChartVisualizer extends AbstractBackgroundImageVisualizer implements BackgroundImageVisualizer {
    public CategoryPieChartVisualizer() {
        NUM_VISUALIZATIONS = 2;
        VISUALIZATION_NAMES = new String[] { "Category Pie Charts", "Category Pie Charts - Size Coding" };
        VISUALIZATION_SHORT_NAMES = new String[] { "PieCharts", "PieChartsSize" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Pie charts", "Pie charts, hit-histogram size encoded" };
    }

    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        checkVariantIndex(variantIndex, getClass());

        GrowingLayer layer = gsom.getLayer();
        double unitWidth = (double) width / gsom.getLayer().getXSize();
        double unitHeight = (double) height / gsom.getLayer().getYSize();

        BufferedImage res = ImageUtils.createEmptyImage(width, height);
        Graphics2D g = (Graphics2D) res.getGraphics();

        final SOMLibClassInformation classInfo = inputObjects.getClassInfo();
        final Color[] classColors = classInfo.getClassColors();

        double maximumMatrixValue = 0;
        for (int x = 0; x < layer.getXSize(); x++) {
            for (int y = 0; y < layer.getYSize(); y++) {
                final int numberOfMappedInputs = layer.getUnit(x, y).getNumberOfMappedInputs();
                if (numberOfMappedInputs > maximumMatrixValue) {
                    maximumMatrixValue = numberOfMappedInputs;
                }
            }
        }

        double totalArea = unitWidth * unitHeight;
        double aspectRatio = unitWidth / unitHeight;

        for (int y = 0; y < layer.getYSize(); y++) {
            for (int x = 0; x < layer.getXSize(); x++) {
                if (layer.getUnit(x, y) != null && layer.getUnit(x, y).getNumberOfMappedInputs() > 0) {
                    String[] mappedInputNames = layer.getUnit(x, y).getMappedInputNames();
                    int[] values = classInfo.computeClassDistribution(mappedInputNames);
                    if (variantIndex == 0) {
                        PieChartPNode.drawPlot(g, values, classColors, x * unitWidth, y * unitHeight, unitWidth,
                                unitHeight);
                    } else {
                        double relativeSize = mappedInputNames.length / maximumMatrixValue;
                        double relativeArea = totalArea * relativeSize;
                        int relativeWidth = (int) Math.round(Math.sqrt(relativeArea * aspectRatio));
                        int relativeHeight = (int) Math.round(Math.sqrt(relativeArea * aspectRatio) * 1 / aspectRatio);

                        PieChartPNode.drawPlot(g, values, classColors, x * unitWidth + (unitWidth - relativeWidth) / 2,
                                y * unitHeight + (unitHeight - relativeHeight) / 2, relativeWidth, relativeHeight);
                    }
                }
            }
        }
        return res;
    }

    @Override
    public int getPreferredScaleFactor() {
        return 1;
    }

}
