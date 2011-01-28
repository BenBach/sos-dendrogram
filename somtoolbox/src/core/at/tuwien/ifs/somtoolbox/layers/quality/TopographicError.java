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
package at.tuwien.ifs.somtoolbox.layers.quality;

import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.util.ProgressListener;
import at.tuwien.ifs.somtoolbox.util.ProgressListenerFactory;

/**
 * Implementation of Topographic Error Quality Measure.<br>
 * TODO: can maybe be optimised using data winner mapping file ({@link SOMLibDataWinnerMapping}).
 * 
 * @author Gerd Platzgummer
 * @version $Id: TopographicError.java 3883 2010-11-02 17:13:23Z frank $
 */
public class TopographicError extends AbstractQualityMeasure {

    double averageError = 0.0;

    double average8Error = 0.0;

    double[][] unitError;

    double[][] unit8Error;

    public TopographicError(Layer layer, InputData data) {
        super(layer, data);

        int xSize = layer.getXSize();
        int ySize = layer.getYSize();

        unitError = new double[xSize][ySize];
        unit8Error = new double[xSize][ySize];

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                unitError[x][y] = 0.0;
                unit8Error[x][y] = 0.0;
            }

        }
        int numVectors = data.numVectors();
        double sum = 0.0;
        double sum8 = 0.0;
        double[] sampleError = new double[numVectors];
        double[] sample8Error = new double[numVectors];

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Initialising topographic error");
        ProgressListener progress = ProgressListenerFactory.getInstance().createProgressListener(numVectors,
                "Processing vector ", 10);

        for (int d = 0; d < numVectors; d++) {

            Unit[] winners = ((GrowingLayer) layer).getWinners(data.getInputDatum(d), 2);

            Unit bmu = winners[0];
            Unit sbmu = winners[1];

            // 4er-Nachbarschaft
            if (Math.abs(bmu.getXPos() - sbmu.getXPos()) == 1 && bmu.getYPos() == sbmu.getYPos()
                    || bmu.getXPos() == sbmu.getXPos() && Math.abs(bmu.getYPos() - sbmu.getYPos()) == 1) {
                sampleError[d] = 0.0;
            } else {
                sampleError[d] = 1.0;
                sum++;
                unitError[bmu.getXPos()][bmu.getYPos()]++;
            }
            // 8er- Nachbarschaft
            if (Math.abs(bmu.getXPos() - sbmu.getXPos()) == 1 && Math.abs(bmu.getYPos() - sbmu.getYPos()) == 0
                    || Math.abs(bmu.getXPos() - sbmu.getXPos()) == 0 && Math.abs(bmu.getYPos() - sbmu.getYPos()) == 1
                    || Math.abs(bmu.getXPos() - sbmu.getXPos()) == 1 && Math.abs(bmu.getYPos() - sbmu.getYPos()) == 1) {
                sample8Error[d] = 0.0;
            } else {
                sample8Error[d] = 1.0;
                sum8++;
                unit8Error[bmu.getXPos()][bmu.getYPos()]++;
            }
            progress.progress();
        }
        averageError = sum / numVectors;
        average8Error = sum8 / numVectors;
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure#getMapQuality(java.lang.String)
     */
    @Override
    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        if (name.equals("TE_Map")) {
            return averageError;
        } else if (name.equals("TE8_Map")) {
            return average8Error;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure#getUnitQualities(java.lang.String)
     */
    @Override
    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        if (name.equals("TE_Unit")) {
            return unitError;
        } else if (name.equals("TE8_Unit")) {
            return unit8Error;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

}
