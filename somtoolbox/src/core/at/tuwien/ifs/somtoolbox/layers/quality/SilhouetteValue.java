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
import at.tuwien.ifs.somtoolbox.layers.metrics.L1Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * @author Robert Neumayer
 * @version $Id: SilhouetteValue.java 3883 2010-11-02 17:13:23Z frank $
 */
public class SilhouetteValue extends AbstractQualityMeasure {

    private double mapSilhouetteValue;

    private double[][] unitSilhouetteValues;

    Logger logger = Logger.getLogger("at.tuwien.ifs.somtoolbox");

    public SilhouetteValue(Layer layer, InputData data) {
        super(layer, data);
        logger.info("Start computing silhouette");
        mapQualityNames = new String[] { "silhouette" };
        mapQualityDescriptions = new String[] { "Silhouette Value" };
        unitQualityNames = new String[] { "silhouette" };
        unitQualityDescriptions = new String[] { "Silhouette Value" };
        int xSize = layer.getXSize();
        int ySize = layer.getYSize();

        this.unitSilhouetteValues = new double[xSize][ySize];

        StdErrProgressWriter progressWriter = new StdErrProgressWriter(layer.getXSize() * layer.getYSize(),
                "Calculating SilhouetteValue for unit ", 50);

        for (int y = 0; y < layer.getYSize(); y++) {
            for (int x = 0; x < layer.getXSize(); x++) {
                progressWriter.progress(y * layer.getYSize() + x);
                // logger.fine("handling unit: " + (x + 1) + " / " + (y + 1) + " of " + (layer.getXSize() *
                // layer.getYSize()));
                Unit u = null;
                try {
                    u = layer.getUnit(x, y);
                } catch (LayerAccessException e) {
                    // TODO: this does not happen
                }

                // added to deal with mnemonic (sparse) SOMs
                if (u != null) {
                    // find closest unit to that one
                    double minUnitDistance = Double.POSITIVE_INFINITY;
                    int xxx = 0;
                    int yyy = 0;
                    for (int yy = 0; yy < layer.getYSize(); yy++) {
                        for (int xx = 0; xx < layer.getXSize(); xx++) {
                            Unit closestUnit = null;
                            try {
                                closestUnit = layer.getUnit(xx, yy);
                            } catch (LayerAccessException e) {
                                // TODO: this does not happen
                            }
                            // do not compare it to itself
                            if (!(xx == x && yy == y) && closestUnit.getNumberOfMappedInputs() > 0) {
                                logger.fine("xx / yy || x / y " + xx + " / " + yy + " || " + x + " / " + y);
                                try {
                                    // assign value if smaller
                                    double distance = new L1Metric().distance(u.getWeightVector(),
                                            closestUnit.getWeightVector());
                                    if (distance < minUnitDistance) {
                                        minUnitDistance = distance;
                                        xxx = xx;
                                        yyy = yy;
                                    }
                                } catch (MetricException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // get b(i) out of other unit
                    Unit closestUnit = null;
                    try {
                        closestUnit = layer.getUnit(xxx, yyy);
                    } catch (LayerAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    logger.finer("\t found closest unit " + (xxx + 1) + " / " + (yyy + 1));
                    double[] tmpUnitSilhouetteValues = new double[u.getNumberOfMappedInputs()];
                    // for each mapped datum

                    for (int i = 0; i < u.getNumberOfMappedInputs(); i++) {
                        // logger.finer("Handling input: " + (i + 1) + " out of " + (u.getNumberOfMappedInputs()));
                        InputDatum thisDatum = this.data.getInputDatum(u.getMappedInputNames()[i]);
                        double distancesWithinCluster = 0;
                        // again for each mapped datum
                        for (int j = 0; j < u.getNumberOfMappedInputs(); j++) {
                            // compare to each other
                            // not to itself though
                            if (i == j) {
                                continue;
                            }

                            InputDatum thatDatum;
                            thatDatum = this.data.getInputDatum(u.getMappedInputNames()[j]);
                            try {
                                double withinDistance = new L1Metric().distance(thisDatum.getVector().toArray(),
                                        thatDatum.getVector().toArray());
                                distancesWithinCluster += withinDistance;
                                logger.finest("distances within: " + withinDistance);
                            } catch (MetricException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        // logger.finer("sum within: " + distancesWithinCluster);
                        double avgDistanceWithinCluster;
                        if (u.getNumberOfMappedInputs() == 1) {
                            avgDistanceWithinCluster = 0d;
                        } else {
                            avgDistanceWithinCluster = distancesWithinCluster / (u.getNumberOfMappedInputs() - 1);
                        }
                        logger.finer("\t found within cluster neighbour");

                        double minDist = Double.POSITIVE_INFINITY;
                        double distance = 0;
                        logger.finer("closestUnit.numberOfINputs: " + closestUnit.getNumberOfMappedInputs());
                        for (int p = 0; p < closestUnit.getNumberOfMappedInputs(); p++) {
                            InputDatum thatDatum = this.data.getInputDatum(closestUnit.getMappedInputNames()[p]);
                            try {
                                distance = new L1Metric().distance(thisDatum.getVector().toArray(), thatDatum);
                            } catch (MetricException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            logger.finest("distances to cluster neighbour datapoint: " + distance);
                            if (distance < minDist) {
                                minDist = distance;
                            }
                        }
                        logger.finer("\t found closest vector in cluster neighbour");
                        // there we go b(i)
                        double max = 0;
                        if (avgDistanceWithinCluster > minDist) {
                            max = avgDistanceWithinCluster;
                        } else {
                            max = minDist;
                        }
                        logger.finer("\t minDist - avgDistanceWithinCluster / max: " + minDist + " - "
                                + avgDistanceWithinCluster + " / " + max);
                        tmpUnitSilhouetteValues[i] = (minDist - avgDistanceWithinCluster) / max;
                    }
                    double unitSumSilhouette = 0;
                    logger.finer(String.valueOf(tmpUnitSilhouetteValues.length));
                    for (double tmpUnitSilhouetteValue : tmpUnitSilhouetteValues) {
                        unitSumSilhouette += tmpUnitSilhouetteValue;
                        logger.finer("\t" + tmpUnitSilhouetteValue);
                    }
                    double q = tmpUnitSilhouetteValues.length;

                    if (q != 0) {
                        this.unitSilhouetteValues[x][y] = unitSumSilhouette / q;
                    } else {
                        this.unitSilhouetteValues[x][y] = 0;
                    }
                }
            }
        }

        double sumSilhouette = 0;
        int NaNCount = 0;
        for (int y = 0; y < layer.getYSize(); y++) {
            for (int x = 0; x < layer.getXSize(); x++) {
                // logger.finer("x / y " + x + " / " + y + " silVal: " + this.unitSilhouetteValues[x][y]);
                if (Double.isNaN(this.unitSilhouetteValues[x][y])) {
                    NaNCount++;
                } else {
                    sumSilhouette += this.unitSilhouetteValues[x][y];
                }
            }
        }
        logger.finer("sumSilhouette: " + sumSilhouette);
        logger.finer("NaNCount: " + NaNCount);
        logger.finer("#units: " + new Double(layer.getXSize() * layer.getYSize()).doubleValue());
        logger.finer("#units - NaNCount: " + (new Double(layer.getXSize() * layer.getYSize()).doubleValue() - NaNCount));

        // get closest unit
        // get lowest distance in that cluster
        this.mapSilhouetteValue += sumSilhouette
                / (new Double(layer.getXSize() * layer.getYSize()).doubleValue() - NaNCount);
        logger.fine("unitSilhouetteValue: " + this.mapSilhouetteValue);
    }

    @Override
    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        if (name.equals("silhouettevalue")) {
            return this.mapSilhouetteValue;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure named " + name + " not found.");
        }
    }

    @Override
    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        return this.unitSilhouetteValues;
    }

}
