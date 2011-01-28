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

import java.util.ArrayList;
import java.util.Hashtable;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.comparables.UnitDistance;

/**
 * Implementation of Intrinsic Distance Quality
 * 
 * @author Gerd Platzgummer
 * @version $Id: IntrinsicDistance.java 3883 2010-11-02 17:13:23Z frank $
 */
public class IntrinsicDistance extends AbstractQualityMeasure {

    double[] _SampleSummand2 = null;

    double[][] UnitSummand1;

    double[][] UnitSummand2;

    double[][] Unit_ID;

    double MapSummand1;

    double Map_ID = 0.0;

    public IntrinsicDistance(Layer layer, InputData data) {
        super(layer, data);

        int xSize1 = layer.getXSize();
        int ySize1 = layer.getYSize();
        UnitSummand1 = new double[xSize1][ySize1];
        UnitSummand2 = new double[xSize1][ySize1];
        Unit_ID = new double[xSize1][ySize1];

        for (int x = 0; x < xSize1; x++) {
            for (int y = 0; y < ySize1; y++) {
                UnitSummand1[x][y] = 0.0; // Summand 1: enthaelt Distanz eines jeden Samples zu seiner BMU, gemappt auf
                // die Unit
                UnitSummand2[x][y] = 0.0; // Summand 2: Distanz von BMU (eines Samples) zu 2nd ueber Dijkstra, gemappt
                // auf die Unit
            }

        }
        MapSummand1 = 0;
        /** *********Summand 1: Aequivalent zum (einfachen) Quantization Error */
        int nonEmpty = 0;
        for (int y = 0; y < layer.getYSize(); y++) {

            for (int x = 0; x < layer.getXSize(); x++) {
                double quantErr = 0;
                Unit u = null;

                try {
                    u = layer.getUnit(x, y);
                } catch (LayerAccessException e) {
                    // TODO: this does not happen
                }
                double[] dists = u.getMappedInputDistances();
                for (int i = 0; i < u.getNumberOfMappedInputs(); i++) {
                    quantErr += dists[i];
                }
                UnitSummand1[x][y] = quantErr;
                if (u.getNumberOfMappedInputs() > 0) {
                    nonEmpty++;
                }
            }
            // MapSummand1 += UnitSummand1[x][y];

        }

        /** *********Summand 2: Dist BMU 2ndBMU ueber den shortest path */

        Unit[] V = ((GrowingLayer) layer).getAllUnits();

        int xSize = layer.getXSize();
        int ySize = layer.getYSize();
        int unitcount = xSize * ySize;
        int samplecount = data.numVectors();

        Hashtable<Unit, UnitInfo> units = new Hashtable<Unit, UnitInfo>();

        DistanceMetric metric = layer.getMetric();

        try {
            for (int i = 0; i < unitcount; i++) // 4 Nachbarunits bestimmen, Distanzen dazu
            {
                units.put(V[i], new UnitInfo(V[i]));

                QuadUnitDistance quad = new QuadUnitDistance();
                for (int p = 0; p < 4; p++) {
                    Unit neigh4 = null;
                    if (p == 0 && V[i].getXPos() > 0) {
                        neigh4 = layer.getUnit(V[i].getXPos() - 1, V[i].getYPos());
                    } else if (p == 1 && V[i].getYPos() > 0) {
                        neigh4 = layer.getUnit(V[i].getXPos(), V[i].getYPos() - 1);
                    } else if (p == 2 && V[i].getXPos() < xSize - 1) {
                        neigh4 = layer.getUnit(V[i].getXPos() + 1, V[i].getYPos());
                    } else if (p == 3 && V[i].getYPos() < ySize - 1) {
                        neigh4 = layer.getUnit(V[i].getXPos(), V[i].getYPos() + 1);
                    }

                    if (neigh4 != null) {
                        UnitDistance ud = new UnitDistance(neigh4, metric.distance(V[i].getWeightVector(),
                                neigh4.getWeightVector()));
                        quad.putUnitDistance(p, ud);
                    }
                }
                units.get(V[i]).setQuad(quad); // die 2-4 nachbarn und die jeweilige distanz
            }
        } catch (LayerAccessException ex) {
        } catch (MetricException mex) {
        }

        _SampleSummand2 = new double[samplecount];
        for (int s = 0; s < samplecount; s++) {
            Unit[] winners = ((GrowingLayer) layer).getWinners(data.getInputDatum(s), 2);

            Unit bmu = winners[0];
            Unit sbmu = winners[1];

            for (int u = 0; u < unitcount; u++) {
                UnitInfo ui = units.get(V[u]);
                ui.setPredecessor(null);
                ui.setDistance(Double.MAX_VALUE); // alle distances auf unendlich initialisieren
            }

            units.get(bmu).setDistance(0.0);

            // Shortest path zwischen der bmu und sbmu
            Dijkstra(V, unitcount, bmu, sbmu, units);

            _SampleSummand2[s] = units.get(sbmu).getDistance();
            UnitSummand2[bmu.getXPos()][bmu.getYPos()] += _SampleSummand2[s];
        }
        /* hier noch den _SampleSummand2[s] auf die jeweilige Unit mappen */
        // UnitSummand2[bmu.getXPos()][bmu.getYPos()]++;
        for (int x = 0; x < xSize1; x++) {
            for (int y = 0; y < ySize1; y++) {
                Unit_ID[x][y] = UnitSummand1[x][y] + UnitSummand2[x][y];
                Map_ID += Unit_ID[x][y];
            }

        }
        Map_ID = Map_ID / samplecount; // oder nonEmpty: Units mit assoziierten Samples

    }

    private void Dijkstra(Unit[] V, int unitcount, Unit bmu, Unit sbmu, Hashtable<Unit, UnitInfo> units) {
        ArrayList<Unit> s = new ArrayList<Unit>();
        s.add(bmu);

        // relax bmu
        QuadUnitDistance q = units.get(bmu).getQuad();
        for (int i = 0; i < 4; i++) {
            UnitDistance ud = q.getUnitDistance(i);
            if (ud != null) {
                Relax(units.get(bmu), units.get(ud.getUnit()), ud.getDistance());
            }
        }

        while (true) {
            // naechste Unit laut distances, die nicht schon in S ist, heraussuchen
            double mindistance = Double.MAX_VALUE;
            Unit minunit = null;
            for (int i = 0; i < unitcount; i++) {
                if (!s.contains(V[i]) && units.get(V[i]).getDistance() < mindistance) {
                    mindistance = units.get(V[i]).getDistance();
                    minunit = V[i];
                }
            }

            s.add(minunit);

            // relax minunit
            QuadUnitDistance q1 = units.get(minunit).getQuad();
            for (int i = 0; i < 4; i++) {
                UnitDistance ud1 = q1.getUnitDistance(i);
                if (ud1 != null) {
                    Relax(units.get(minunit), units.get(ud1.getUnit()), ud1.getDistance());
                }
            }

            // abbruch?
            if (minunit == sbmu) {
                break;
            }
        }

    }

    private void Relax(UnitInfo u, UnitInfo v, double distance) {
        if (v.getDistance() > u.getDistance() + distance) {
            v.setDistance(u.getDistance() + distance);
            v.setPredecessor(u.getUnit());
        }
    }

    public class QuadUnitDistance {
        UnitDistance[] unitDistances = new UnitDistance[4]; // {null, null, null, null};

        public QuadUnitDistance() {
        }

        public UnitDistance getUnitDistance(int index) {
            return unitDistances[index];
        }

        public void putUnitDistance(int index, UnitDistance ud) {
            unitDistances[index] = ud;
        }
    }

    public class UnitInfo {
        Unit unit;

        double distance = 0.0;

        Unit predecessor = null;

        QuadUnitDistance quadUnitDistance = null;

        public UnitInfo(Unit u) {
            unit = u;
        }

        public Unit getUnit() {
            return unit;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double d) {
            distance = d;
        }

        public Unit getPredecessor() {
            return predecessor;
        }

        public void setPredecessor(Unit pre) {
            predecessor = pre;
        }

        public QuadUnitDistance getQuad() {
            return quadUnitDistance;
        }

        public void setQuad(QuadUnitDistance quad) {
            quadUnitDistance = quad;
        }
    }

    /** *********************************************************************************************************** */
    /*
     * Ausgabe: ID_Sample-(nicht ID_Unit!)- Werte, Durchschnitt
     */
    @Override
    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        if (name.equals("ID_Map")) {
            return Map_ID;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

    /*
     * Ausgabe: ID_Sample- Werte, Mapping auf die entsprechende Unit
     */

    @Override
    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        if (name.equals("ID_Unit")) {
            return Unit_ID;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

}
