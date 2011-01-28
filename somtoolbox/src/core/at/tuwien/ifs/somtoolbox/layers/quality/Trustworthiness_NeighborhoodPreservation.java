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

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;

/**
 * Implematation of Trustworthiness and Neighborhood preservation Quality Measures
 * 
 * @author Gerd Platzgummer
 * @version $Id: Trustworthiness_NeighborhoodPreservation.java 3883 2010-11-02 17:13:23Z frank $
 */
public class Trustworthiness_NeighborhoodPreservation extends AbstractQualityMeasure {

    DistanceMetric metric = null;

    double _K = 0.0;

    DistanceTag[][] inputDistances = null;

    DistanceTag[][] outputDistances = null;

    double[][] twUnitValues = null;

    double twMapValue = 0.0;

    double twK = -1;

    double[][] npUnitValues = null;

    double npMapValue = 0.0;

    double npK = -1;

    public Trustworthiness_NeighborhoodPreservation(Layer layer, InputData data) {
        super(layer, data);
        metric = layer.getMetric();

        int xSize = layer.getXSize();
        int ySize = layer.getYSize();
        twUnitValues = new double[xSize][ySize];
        npUnitValues = new double[xSize][ySize];

    }

    private void calculateTW() {
        int samplecount = data.numVectors();
        int xSize = layer.getXSize();
        int ySize = layer.getYSize();

        double[] samplevalues = new double[samplecount]; // errror value per Sample
        for (int j = 0; j < samplecount; j++) {
            samplevalues[j] = 0.0;
            for (int n = 0; n < samplecount && outputDistances[j][n].getMinRank() <= _K; n++) {
                int tag = outputDistances[j][n].getTag();
                for (int m = 0; m < samplecount; m++) {
                    if (inputDistances[j][m].getTag() == tag) {
                        if (inputDistances[j][m].getMinRank() > _K) {
                            samplevalues[j] += inputDistances[j][m].getRank() - _K;
                        }
                    }
                }
            }
        }

        // compute Map-value
        twMapValue = 0.0;
        for (int i = 0; i < samplecount; i++) {
            twMapValue += samplevalues[i];
        }
        twMapValue = 1 - 2 / (samplecount * _K * (2 * samplecount - 3 * _K - 1)) * twMapValue;

        // reset Unit- values
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                twUnitValues[x][y] = 0.0;
            }
        }

        // compute Unit-values
        for (int i = 0; i < samplecount; i++) {
            Unit bmu = ((GrowingLayer) layer).getWinners(data.getInputDatum(i), 1)[0];
            twUnitValues[bmu.getXPos()][bmu.getYPos()] += samplevalues[i];
        }

        twK = _K;
        // cleanup
        inputDistances = null;
        outputDistances = null;
    }

    private void calculateNP() {
        int samplecount = data.numVectors();
        int xSize = layer.getXSize();
        int ySize = layer.getYSize();

        double[] samplevalues = new double[samplecount]; // errror value per Sample
        for (int j = 0; j < samplecount; j++) {
            samplevalues[j] = 0.0;
            for (int n = 0; n < samplecount && inputDistances[j][n].getMinRank() <= _K; n++) {
                int tag = inputDistances[j][n].getTag();
                for (int m = 0; m < samplecount; m++) {
                    if (outputDistances[j][m].getTag() == tag) {
                        if (outputDistances[j][m].getMinRank() > _K) {
                            samplevalues[j] += outputDistances[j][m].getRank() - _K;
                        }
                    }
                }
            }
        }

        // compute Map-values
        npMapValue = 0.0;
        for (int i = 0; i < samplecount; i++) {
            npMapValue += samplevalues[i];
        }
        npMapValue = 1 - 2 / (samplecount * _K * (2 * samplecount - 3 * _K - 1)) * npMapValue;

        // reset Unit-values
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                npUnitValues[x][y] = 0.0;
            }
        }

        // compute Unit-values
        for (int i = 0; i < samplecount; i++) {
            Unit bmu = ((GrowingLayer) layer).getWinners(data.getInputDatum(i), 1)[0];
            npUnitValues[bmu.getXPos()][bmu.getYPos()] += samplevalues[i];
        }

        npK = _K;
        // cleanup
        inputDistances = null;
        outputDistances = null;
    }

    private void rankingInputSpace() { // This computes the ranking (order) in the Input Space for each value
        int samplecount = data.numVectors();

        inputDistances = new DistanceTag[samplecount][samplecount];
        try {
            DistanceTagComparator comparator = new DistanceTagComparator();
            for (int s = 0; s < samplecount; s++) {
                double[] svec = data.getInputDatum(s).getVector().toArray();

                for (int t = 0; t < samplecount; t++) {
                    if (s == t) {
                        inputDistances[s][t] = new DistanceTag(t, Double.MAX_VALUE);
                    } else {
                        inputDistances[s][t] = new DistanceTag(t, metric.distance(svec,
                                data.getInputDatum(t).getVector().toArray()));
                    }
                }
                Arrays.sort(inputDistances[s], comparator);
                // QuickSort.sort(inputDistances[s], comparator);

                Rank(inputDistances, s, samplecount);

                /*
                 * System.out.print(s + ": "); for (int t = 0; t < samplecount; t++) System.out.print(_InputDistances[s][t].getDistance() + " ");
                 * System.out.println();
                 */
            }
        } catch (Exception ex) {
            System.out.println("EXCEPTION: " + ex.getMessage());
        }

    }

    private void rankingOutputSpace() { // This computes the ranking (order) in the Output Space for each value
        int samplecount = data.numVectors();

        outputDistances = new DistanceTag[samplecount][samplecount];
        try {
            DistanceTagComparator comparator = new DistanceTagComparator();
            for (int s = 0; s < samplecount; s++) {
                Unit s_bmu = ((GrowingLayer) layer).getWinners(data.getInputDatum(s), 1)[0];
                int s_x = s_bmu.getXPos();
                int s_y = s_bmu.getYPos();

                for (int t = 0; t < samplecount; t++) {
                    if (s == t) {
                        outputDistances[s][t] = new DistanceTag(t, Double.MAX_VALUE);
                    } else {
                        Unit t_bmu = ((GrowingLayer) layer).getWinners(data.getInputDatum(t), 1)[0];
                        double distance = Math.sqrt((s_x - t_bmu.getXPos()) * (s_x - t_bmu.getXPos())
                                + (s_y - t_bmu.getYPos()) * (s_y - t_bmu.getYPos()));
                        outputDistances[s][t] = new DistanceTag(t, distance);
                    }
                }

                Arrays.sort(outputDistances[s], comparator);
                // QuickSort.sort(outputDistances[s], comparator);

                Rank(outputDistances, s, samplecount);

                // System.out.print(s + ": ");
                // for (int t = 0; t < samplecount; t++)
                // System.out.print(outputDistances[s][t].getRank() + " ");
                // System.out.println();

            }
        } catch (Exception ex) {
            System.out.println("EXCEPTION: " + ex.getMessage());
        }

    }

    private void Rank(DistanceTag[][] distances, int s, int samplecount) {
        int sittingbull = 0;
        double sbdistance = distances[s][0].getDistance();
        // distances[s][0].setMinRank(1);
        int countequal = 0;
        int ranksum = 0;
        int currank = 0;
        int minrank = 1;
        for (int roadrunner = 1; roadrunner < samplecount; roadrunner++) {
            currank++;
            countequal++;
            ranksum += currank;
            distances[s][roadrunner].setRank(currank);
            if (distances[s][roadrunner].getDistance() != sbdistance) {
                while (sittingbull < roadrunner) {
                    distances[s][sittingbull].setRank((double) ranksum / (double) countequal);
                    distances[s][sittingbull].setMinRank(minrank);
                    sittingbull++;
                }
                minrank = currank + 1;
                ranksum = 0;
                countequal = 0;
                sbdistance = distances[s][roadrunner].getDistance();
            }
            if (roadrunner == samplecount - 1) // letzter Wert ist immer das aktuelle Sample s
            {
                distances[s][roadrunner].setRank(samplecount);
                distances[s][roadrunner].setMinRank(samplecount);
            }
        }
    }

    /** *********************************************************************************************************** */
    /*
     * Ausgabe: TRUSTWORTHINESS
     */
    @Override
    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        if (name.startsWith("TW_Map|")) {
            int k = Integer.valueOf(name.substring(7)).intValue();
            // Berechnung wird hier gestartet; Caching: wenn gleiches k schon ein Ergebnis vorhanden, dann dieses
            // zur�ckgeben, sonst Neuberechnung
            if (twK != k) {
                _K = k;
                rankingInputSpace();
                rankingOutputSpace();
                calculateTW();
            }
            return twMapValue;
        }

        else if (name.startsWith("NP_Map|")) {
            int k = Integer.valueOf(name.substring(7)).intValue();
            // Berechnung wird hier gestartet; Caching: wenn gleiches k schon ein Ergebnis vorhanden, dann dieses
            // zur�ckgeben, sonst Neuberechnung
            if (npK != k) {
                _K = k;
                rankingInputSpace();
                rankingOutputSpace();
                calculateNP();
            }
            return npMapValue;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure#getUnitQuality(java.lang.String)
     */
    @Override
    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        if (name.startsWith("TW_Unit|")) {
            int k = Integer.valueOf(name.substring(8)).intValue();
            // Berechnung wird hier gestartet; Caching: wenn gleiches k schon ein Ergebnis vorhanden, dann dieses
            // zur�ckgeben, sonst Neuberechnung
            if (twK != k) {
                _K = k;
                rankingInputSpace();
                rankingOutputSpace();
                calculateTW();
            }
            return twUnitValues;
        }

        else if (name.startsWith("NP_Unit|")) {
            int k = Integer.valueOf(name.substring(8)).intValue();
            // Berechnung wird hier gestartet; Caching: wenn gleiches k schon ein Ergebnis vorhanden, dann dieses
            // zur�ckgeben, sonst Neuberechnung
            if (npK != k) {
                _K = k;
                rankingInputSpace();
                rankingOutputSpace();
                calculateNP();
            }
            return npUnitValues;
        }

        else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

    /** *********************************************************************************************************** */

    /** *********************************************************************************************************** */

    public class DistanceTag {
        int _Tag;

        double _Distance = 0.0;

        double _Rank = 0.0;

        int _MinRank = 0;

        public DistanceTag(int tag) {
            _Tag = tag;
        }

        public DistanceTag(int tag, double distance) {
            _Tag = tag;
            _Distance = distance;
        }

        public int getTag() {
            return _Tag;
        }

        public double getDistance() {
            return _Distance;
        }

        public void setDistance(double d) {
            _Distance = d;
        }

        public double getRank() {
            return _Rank;
        }

        public void setRank(double r) {
            _Rank = r;
        }

        public int getMinRank() {
            return _MinRank;
        }

        public void setMinRank(int m) {
            _MinRank = m;
        }
    }

    public class DistanceTagComparator implements java.util.Comparator<DistanceTag> {
        public DistanceTagComparator() {
        }

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
