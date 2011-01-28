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
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * Implementation of SOM Distortion Measure Quality.
 * 
 * @author Michael Dittenbach
 * @version $Id: SOMDistortion.java 3883 2010-11-02 17:13:23Z frank $
 */
public class SOMDistortion extends AbstractQualityMeasure {
    private double distortion;

    // private double totalDistortion;

    private double[][] hits;

    private double[][] unitAverage;

    private double[][] unitTotal;

    public SOMDistortion(Layer layer, InputData data) {
        super(layer, data);
        mapQualityNames = new String[] { "distortion" };
        mapQualityDescriptions = new String[] { "SOM Distortion" };
        unitQualityNames = new String[] { "unitAverage", "unitTotal" };
        unitQualityDescriptions = new String[] { "Average Unit Distortion", "Total Unit Distortion" };
        int xSize = layer.getXSize();
        int ySize = layer.getYSize();

        unitTotal = new double[xSize][ySize];
        unitAverage = new double[xSize][ySize];
        hits = new double[xSize][ySize];
        for (int j = 0; j < ySize; j++) {
            for (int i = 0; i < xSize; i++) {
                unitTotal[i][j] = 0;
                unitAverage[i][j] = 0;
                hits[i][j] = 0;
            }
        }
        distortion = 0;

        double[][] dist2 = new double[xSize][ySize];

        try {
            for (int d = 0; d < data.numVectors(); d++) {
                InputDatum datum = data.getInputDatum(d);
                double minDist2 = Double.MAX_VALUE;
                int bmuX = -1;
                int bmuY = -1;

                // calculate squared distances and remember BMU
                for (int j = 0; j < ySize; j++) {
                    for (int i = 0; i < xSize; i++) {
                        dist2[i][j] = squaredDistance(datum, layer.getUnit(i, j).getWeightVector());
                        if (dist2[i][j] < minDist2) {
                            minDist2 = dist2[i][j];
                            bmuX = i;
                            bmuY = j;
                        }

                    }
                }

                // calculate total distortion of units
                Unit bmu = layer.getUnit(bmuX, bmuY);
                for (int j = 0; j < ySize; j++) {
                    for (int i = 0; i < xSize; i++) {
                        unitTotal[i][j] += dist2[i][j]
                                * neighborhoodFunction(layer.getMapDistance(bmu, layer.getUnit(i, j)));
                    }
                }

                // increase hit variable for BMU
                hits[bmuX][bmuY]++;
            }

            // calculate average distortion per unit, sum up total distortions
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    distortion += unitTotal[i][j];
                    if (hits[i][j] > 0) {
                        unitAverage[i][j] = unitTotal[i][j] / hits[i][j];
                    }
                }
            }

            // totalDistortion = distortion;

            // average distortion measure
            distortion = distortion / data.numVectors();
        } catch (MetricException me) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(me.getMessage());
            System.exit(-1);
        } catch (LayerAccessException lae) {
            // TODO: this does not happen
        }
        dist2 = null;
    }

    @Override
    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        if (name.equals("distortion")) {
            return distortion;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

    @Override
    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        if (name.equals("unitTotal")) {
            return unitTotal;
        } else if (name.equals("unitAverage")) {
            return unitAverage;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

    private double neighborhoodFunction(double dist) {
        // e^-(d^2/2*sigma^2)
        // return Math.exp((-1*dist*dist)/(2*0.01*0.01));
        return Math.exp(-1 * dist * dist / 0.002);
    }

    private double squaredDistance(InputDatum datum, double[] vector2) throws MetricException {
        double[] vector1 = datum.getVector().toArray();

        if (vector1.length != vector2.length) {
            throw new MetricException(
                    "Oops ... tried to calculate distance between two vectors with different dimensionalities.");
        }

        double dist = 0;
        int dim = vector1.length;
        for (int ve = 0; ve < dim; ve++) {
            dist += (vector1[ve] - vector2[ve]) * (vector1[ve] - vector2[ve]);
        }
        return dist;
    }

}
