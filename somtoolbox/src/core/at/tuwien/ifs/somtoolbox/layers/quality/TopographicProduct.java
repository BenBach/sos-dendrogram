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

import java.util.Arrays;
import java.util.Random;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;

/**
 * Implementation of Topographic Product Quality Measure.<br>
 * More Infos: H.U. Bauer and K.R. Pawelzik. Quantifying the neighborhood preservation of self- organizing feature maps.
 * In IEEE Transactions on Neural Networks 3, pages 570-579, 1992.
 * 
 * @author Gerd Platzgummer
 * @version $Id: TopographicProduct.java 3883 2010-11-02 17:13:23Z frank $
 */
public class TopographicProduct extends AbstractQualityMeasure {
    DistanceMetric metric = null;

    double _K = 0;

    DistanceTag[][][] protoDist = null;

    DistanceTag[][][] mapDist = null;

    double[][] _Q1Q2temp = null; // Formel 16 im Bauer-Paper; Tempor�rer TP Wert f�r 1 bestimmtes k w�hrend Iteration

    double[][] tpUnitValues = null;

    public TopographicProduct(Layer layer, InputData data) {
        super(layer, data);

        metric = layer.getMetric();

        int xSize = layer.getXSize();
        int ySize = layer.getYSize();

        mapDist = new DistanceTag[xSize][ySize][xSize * ySize];
        protoDist = new DistanceTag[xSize][ySize][xSize * ySize];
        tpUnitValues = new double[xSize][ySize];
    }

    private void resetResults() {
        for (int x = 0; x < layer.getXSize(); x++) {
            for (int y = 0; y < layer.getYSize(); y++) {
                tpUnitValues[x][y] = 1.0;
            }
        }
    }

    private void rankOutputSpace() {
        try {
            int xSize = layer.getXSize();
            int ySize = layer.getYSize();

            DistanceTagComparator comparator = new DistanceTagComparator();

            System.out.println("xSize " + xSize + " ySize " + ySize);

            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    System.out.println("output x " + x + " y " + y);
                    for (int x2 = 0; x2 < xSize; x2++) {
                        for (int y2 = 0; y2 < ySize; y2++) {
                            // System.out.println("x " + x + " y " + y + " x2 " + x2 + " y2 " + y2);
                            double distance = Double.MAX_VALUE;
                            if (x != x2 || y != y2) {
                                distance = Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
                            }
                            mapDist[x][y][x2 * ySize + y2] = new DistanceTag(x2, y2, distance);
                        }
                    }
                    // System.out.println("vor sort");
                    Arrays.sort(mapDist[x][y], comparator);
                    // QuickSort.sort(mapDist[x][y], comparator);

                    // rank it 2006 (rankt den Output space)
                    double curdist = 1;
                    int currank = 1;
                    for (int z = 0; z < xSize * ySize; z++) {
                        if (mapDist[x][y][z].getDistance() != curdist) {
                            curdist = mapDist[x][y][z].getDistance();
                            currank = z + 1;
                        }

                        mapDist[x][y][z].setRank(currank);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void rankInputSpace() {
        try {
            int xSize = layer.getXSize();
            int ySize = layer.getYSize();

            DistanceTagComparator comparator = new DistanceTagComparator();

            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    System.out.println("input x " + x + " y " + y);
                    for (int x2 = 0; x2 < xSize; x2++) {
                        for (int y2 = 0; y2 < ySize; y2++) {
                            double distance = Double.MAX_VALUE;
                            if (x != x2 || y != y2) {
                                distance = metric.distance(((GrowingLayer) layer).getUnit(x, y).getWeightVector(),
                                        ((GrowingLayer) layer).getUnit(x2, y2).getWeightVector());
                            }
                            protoDist[x][y][x2 * ySize + y2] = new DistanceTag(x2, y2, distance);
                        }
                    }
                    // System.out.println("vor input sort");
                    Arrays.sort(protoDist[x][y], comparator);
                    // QuickSort.sort(protoDist[x][y], comparator);

                    // rank it 2006 (rankt den Input space)
                    double curdist = -1;
                    int currank = 0;
                    for (int z = 0; z < xSize * ySize; z++) {
                        if (protoDist[x][y][z].getDistance() != curdist) {
                            curdist = protoDist[x][y][z].getDistance();
                            currank = z + 1;
                        }

                        protoDist[x][y][z].setRank(currank);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("ho " + ex.getMessage());
        }
    }

    private void calculateTP() {
        for (int x = 0; x < layer.getXSize(); x++) {
            for (int y = 0; y < layer.getYSize(); y++) {
                for (int k = 0; k < _K; k++) {
                    tpUnitValues[x][y] = tpUnitValues[x][y] * calcQ1(x, y, k) * calcQ2(x, y, k);
                }
                tpUnitValues[x][y] = Math.pow(tpUnitValues[x][y], 1 / (2 * _K));
            }
        }
    }

    private double calculateTPMap() {
        int kmax = layer.getXSize() * layer.getYSize() - 1;
        double tpmap = 0.0;
        for (int k = 1; k <= kmax; k++) {
            System.out.println("MAP: k " + k);

            resetResults();

            _K = k;
            calculateTP();

            DebugDoubles("calculateTPMap3");

            for (int x = 0; x < layer.getXSize(); x++) {
                for (int y = 0; y < layer.getYSize(); y++) {
                    tpmap += Math.log(tpUnitValues[x][y]);
                }
            }
            System.out.println(tpmap);
        }
        tpmap = tpmap * 1 / (kmax * (kmax + 1.0));
        return tpmap;
    }

    private void DebugDoubles(String loc) {

        // System.out.println("DebugDoubles in " + loc);
        // System.out.println("_K=" + _K);
        // for (int x = 11; x < layer.getXSize(); x++) {
        // for (int y = 3; y < layer.getYSize(); y++) {
        // System.out.println("X=" + x + " Y=" + y + " _TPU=" + tpUnitValues[x][y]);
        // }
        // }

    }

    private int GetRandomIndex(DistanceTag[] dtags, int chosen) {
        double rank = dtags[chosen].getRank();
        int minindex = chosen;
        for (int i = minindex; i >= 0; i--) {
            if (dtags[i].getRank() != rank) {
                minindex = i + 1;
                break;
            }
        }

        int maxindex = chosen;
        for (int i = maxindex; i < dtags.length; i++) {
            if (dtags[i].getRank() != rank) {
                maxindex = i - 1;
                break;
            }
        }

        Random rand = new Random();
        return rand.nextInt(maxindex - minindex + 1) + minindex;
    }

    private double calcQ1(int x, int y, int k) {
        try {
            double[] curProto = layer.getUnit(x, y).getWeightVector(); // Protoyp der aktuellen Unit
            // System.out.println("q1 a");

            int random = GetRandomIndex(mapDist[x][y], k);

            DistanceTag kNextDistanceTag = mapDist[x][y][random];
            Unit kNextUnit = layer.getUnit(kNextDistanceTag.getX(), kNextDistanceTag.getY());// Koordinaten zu k
            // n�chster Unit ermitteln
            double[] kNextProto = kNextUnit.getWeightVector();
            double q1Dividend = metric.distance(curProto, kNextProto);

            double q1Divisor = protoDist[x][y][k].getDistance();

            return q1Dividend / q1Divisor;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return 0.0;
        }
    }

    private double calcQ2(int x, int y, int k) {
        try {
            int random = GetRandomIndex(mapDist[x][y], k);

            double q2Dividend = mapDist[x][y][random].getDistance();

            DistanceTag kNextDistanceTag = protoDist[x][y][k];
            double q2Divisor = Math.sqrt((kNextDistanceTag.getX() - x) * (kNextDistanceTag.getX() - x)
                    + (kNextDistanceTag.getY() - y) * (kNextDistanceTag.getY() - y));

            return q2Dividend / q2Divisor;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return 0.0;
        }
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure#getMapQuality(java.lang.String)
     */
    @Override
    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        if (name.equals("TP_Map")) {
            resetResults();

            rankInputSpace();
            rankOutputSpace();

            return calculateTPMap();
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure#getUnitQualities(java.lang.String)
     */
    @Override
    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        if (name.startsWith("TP_Unit|")) {
            int k = Integer.valueOf(name.substring(8)).intValue();
            // Berechnung wird hier gestartet; Caching: wenn gleiches k schon ein Ergebnis vorhanden, dann dieses
            // zur�ckgeben, sonst Neuberechnung
            if (_K != k) {
                resetResults();
                rankInputSpace();
                rankOutputSpace();
                _K = k;
                calculateTP();
            }
            return tpUnitValues;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

    /** *********************************************************************************************************** */

    /** *********************************************************************************************************** */

    public class DistanceTag {
        int x;

        int y;

        double distance = 0.0;

        double rank = 0.0;

        public DistanceTag(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public DistanceTag(int x, int y, double distance) {
            this(x, y);
            this.distance = distance;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double d) {
            distance = d;
        }

        public double getRank() {
            return rank;
        }

        public void setRank(double r) {
            rank = r;
        }
    }

    public class DistanceTagComparator implements java.util.Comparator<DistanceTag> {

        @Override
        public int compare(DistanceTag t1, DistanceTag t2) {
            double diff = t1.getDistance() - t2.getDistance();
            if (diff > 0.0) {
                return 1;
            } else if (diff < 0.0) {
                return -1;
            } else {
                return 0;
            }
        }
    }

}
