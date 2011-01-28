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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.math.Functions;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * @author frank
 * @version $Id: SearchResultHistogramVisualizer.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class SearchResultHistogramVisualizer extends AbstractMatrixVisualizer {

    private final int[][] searchResultHistogram;

    /**
     * 
     */
    public SearchResultHistogramVisualizer(int[][] searchResult) {
        searchResultHistogram = searchResult;
    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer#createVisualization(int,
     * at.tuwien.ifs.somtoolbox.models.GrowingSOM, int, int)
     */
    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        final GrowingLayer layer = gsom.getLayer();
        DoubleMatrix2D matrix = new DenseDoubleMatrix2D(layer.getYSize(), layer.getXSize());
        for (int x = 0; x < layer.getXSize(); x++) {
            for (int y = 0; y < layer.getYSize(); y++) {
                final int numberOfMappedInputs = searchResultHistogram[x][y];
                matrix.setQuick(y, x, numberOfMappedInputs);
                if (numberOfMappedInputs > maximumMatrixValue) {
                    maximumMatrixValue = numberOfMappedInputs;
                }
                if (numberOfMappedInputs < minimumMatrixValue) {
                    minimumMatrixValue = numberOfMappedInputs;
                }
            }
        }
        matrix.assign(Functions.div(maximumMatrixValue));
        return super.createImage(gsom, matrix, width, height, interpolate);
    }

    @Override
    public BufferedImage getVisualization(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        /* No Caching of search results */
        return createVisualization(index, gsom, width, height);
    }

}
