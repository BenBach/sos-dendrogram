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

import java.math.BigDecimal;

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
 * @version $Id: SOMSilhouetteValue.java 3883 2010-11-02 17:13:23Z frank $
 */
public class SOMSilhouetteValue extends AbstractQualityMeasure {

    private double mapSilhouetteValue;

    private double[][] unitSilhouetteValues;

    private double min, max;

    public SOMSilhouetteValue(Layer layer, InputData data) {
        super(layer, data);
        boolean debug = false;
        System.out.println("Start computing SOM silhouette");
        mapQualityNames = new String[] { "silhouette" };
        mapQualityDescriptions = new String[] { "Silhouette Value" };
        unitQualityNames = new String[] { "silhouette" };
        unitQualityDescriptions = new String[] { "Silhouette Value" };
        int xSize = layer.getXSize();
        int ySize = layer.getYSize();

        this.max = 0;
        this.min = 0;

        this.unitSilhouetteValues = new double[xSize][ySize];

        StdErrProgressWriter progressWriter = new StdErrProgressWriter(layer.getXSize() * layer.getYSize(),
                "Calculating SOMSilhouetteValue for unit ", 50);

        for (int y = 0; y < layer.getYSize(); y++) {
            for (int x = 0; x < layer.getXSize(); x++) {
                progressWriter.progress(y * layer.getYSize() + x);
                // System.out.println("handling unit: " + (x + 1) + " / " + (y + 1) + " of " + (layer.getXSize() *
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
                                if (debug) {
                                    System.out.println("xx / yy || x / y " + xx + " / " + yy + " || " + x + " / " + y);
                                }
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
                        e.printStackTrace();
                    }

                    if (debug) {
                        System.out.println("\t found closest unit " + (xxx + 1) + " / " + (yyy + 1));
                    }
                    double[] tmpUnitSilhouetteValues = new double[u.getNumberOfMappedInputs()];
                    // for each mapped datum

                    for (int i = 0; i < u.getNumberOfMappedInputs(); i++) {
                        if (debug) {
                            System.out.println("Handling input: " + (i + 1) + " out of " + u.getNumberOfMappedInputs());
                        }
                        InputDatum thisDatum = this.data.getInputDatum(u.getMappedInputNames()[i]);
                        double avgDistanceWithinCluster = 0d;
                        if (debug) {
                            System.out.println("\t found within cluster neighbour");
                        }

                        try {
                            avgDistanceWithinCluster = new L1Metric().distance(u.getWeightVector(), thisDatum);
                        } catch (MetricException e1) {
                            e1.printStackTrace();
                        }
                        double minDist = Double.POSITIVE_INFINITY;
                        if (debug) {
                            System.out.println("closestUnit.numberOfINputs: " + closestUnit.getNumberOfMappedInputs());
                        }

                        try {
                            minDist = new L1Metric().distance(thisDatum.getVector().toArray(),
                                    closestUnit.getWeightVector());
                        } catch (MetricException e) {
                            e.printStackTrace();
                        }

                        if (debug) {
                            System.out.println("\t found closest vector in cluster neighbour");
                        }
                        double max = 0;
                        if (avgDistanceWithinCluster > minDist) {
                            max = avgDistanceWithinCluster;
                        } else {
                            max = minDist;
                        }
                        if (debug) {
                            System.out.println("\t minDist - avgDistanceWithinCluster / max: " + minDist + " - "
                                    + avgDistanceWithinCluster + " / " + max);
                        }
                        tmpUnitSilhouetteValues[i] = (minDist - avgDistanceWithinCluster) / max;
                    }
                    double unitSumSilhouette = 0;
                    if (debug) {
                        System.out.println(tmpUnitSilhouetteValues.length);
                    }
                    for (double tmpUnitSilhouetteValue : tmpUnitSilhouetteValues) {
                        unitSumSilhouette += tmpUnitSilhouetteValue;
                        if (debug) {
                            System.out.println("\t" + tmpUnitSilhouetteValue);
                        }
                    }
                    this.unitSilhouetteValues[x][y] = unitSumSilhouette
                            / new Double(tmpUnitSilhouetteValues.length).doubleValue();
                }
            }
        }

        double sumSilhouette = 0;
        int NaNCount = 0;
        for (int y = 0; y < layer.getYSize(); y++) {
            for (int x = 0; x < layer.getXSize(); x++) {
                BigDecimal bd = new BigDecimal(0);
                if (Double.isNaN(this.unitSilhouetteValues[x][y])) {
                    NaNCount++;
                } else {
                    bd = new BigDecimal(this.unitSilhouetteValues[x][y]);
                    bd = bd.setScale(50, BigDecimal.ROUND_UP);
                    sumSilhouette += this.unitSilhouetteValues[x][y];

                }
                // System.out.println("x / y " + x + " / " + y + " silVal: " + bd.doubleValue());
            }
        }
        if (debug) {
            System.out.println("sumSilhouette: " + sumSilhouette);
        }
        if (debug) {
            System.out.println("NaNCount: " + NaNCount);
        }
        if (debug) {
            System.out.println("#units: " + new Double(layer.getXSize() * layer.getYSize()).doubleValue());
        }
        if (debug) {
            System.out.println("#units - NaNCount: "
                    + (new Double(layer.getXSize() * layer.getYSize()).doubleValue() - NaNCount));
        }
        // get closest unit
        // get lowest distance in that cluster
        this.mapSilhouetteValue = sumSilhouette
                / (new Double(layer.getXSize() * layer.getYSize()).doubleValue() - NaNCount);
        System.out.println("mapSilhouetteValue: " + this.mapSilhouetteValue);
    }

    @Override
    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        if (name.equals("somSilhouettevalue")) {
            return this.mapSilhouetteValue;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure named " + name + " not found.");
        }
    }

    @Override
    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        return this.unitSilhouetteValues;
    }

    public void findMaxMin() {
        this.max = Double.NEGATIVE_INFINITY;
        this.min = Double.POSITIVE_INFINITY;
        for (int y = 0; y < layer.getYSize(); y++) {
            for (int x = 0; x < layer.getXSize(); x++) {
                if (this.unitSilhouetteValues[x][y] > this.max) {
                    this.max = this.unitSilhouetteValues[x][y];
                }
                if (this.unitSilhouetteValues[x][y] < this.min) {
                    this.min = this.unitSilhouetteValues[x][y];
                }
            }
        }
    }

    public double getMax() {
        if (this.max == 0) {
            this.findMaxMin();
        }
        return this.max;
    }

    public double getMin() {
        if (this.min == 0) {
            this.findMaxMin();
        }
        return this.min;
    }
}
