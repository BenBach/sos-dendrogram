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

import java.util.Vector;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * Implementation of Topographic Function Quality Measure<br>
 * More Infos: T. Villmann, R. Der, M. Herrmann, and T.M. Martinez. Topology preservation in self- organizing feature
 * maps: Exact definition and measurement.In IEEE Transactions on Neural Networks 8, pages 256-266, 1997.
 * 
 * @author Gerd Platzgummer
 * @version $Id: TopographicFunction.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class TopographicFunction {

    Layer layer = null;

    InputData data = null;

    public TopographicFunction(Layer layer, InputData data) {
        this.layer = layer;
        this.data = data;
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure#getUnitQualities(java.lang.String)
     */
    public double[] getFunctionValues(int K) {

        int xsize = layer.getXSize();
        int ysize = layer.getYSize();

        // TEST
        // xsize = 2;
        // ysize = 2;
        // TEST ENDE

        // N = Total number of units
        int N = xsize * ysize;

        // initialize adjacency matrix
        int[][] adj = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                adj[i][j] = Integer.MAX_VALUE;
            }
        }

        // build adjacency matrix
        for (int s = 0; s < data.numVectors(); s++) {
            Unit[] winners = ((GrowingLayer) layer).getWinners(data.getInputDatum(s), 2);

            Unit bmu = winners[0];
            Unit sbmu = winners[1];

            int xposn = xsize * bmu.getYPos() + bmu.getXPos();
            int yposn = xsize * sbmu.getYPos() + sbmu.getXPos();
            if (xposn > yposn) {// triangular adjacency matrix
                adj[xposn][yposn] = 1;
            } else {
                adj[yposn][xposn] = 1;
            }
        }

        for (int zeile = 0; zeile < N; zeile++) {// jeden Start-Knoten durchgehen
            for (int spalte = zeile + 1; spalte < N; spalte++) {// jeden Ziel-Knoten durchgehen
                if (adj[spalte][zeile] != 1) {
                    // erreichbare Knoten ermitteln
                    Vector<Integer> erreichbaren = new Vector<Integer>();
                    for (int i = 0; i < N; i++) {
                        if (i != zeile) {// adj[i][zeile] fuer i==zeile ist immer Integer.MAX_VALUE, daher unwichtig

                            int d = Integer.MAX_VALUE;
                            if (i < zeile) {// Dreiecksmatrix
                                d = adj[zeile][i];
                            } else {
                                d = adj[i][zeile];
                            }
                            if (d < Integer.MAX_VALUE) {
                                erreichbaren.add(new Integer(i));
                            }
                        }
                    }

                    while (!erreichbaren.isEmpty()) {
                        // naehesten erreichbaren Knoten ermitteln
                        int min = Integer.MAX_VALUE;
                        int minpos = -1;
                        int minvpos = -1;
                        for (int i = 0; i < erreichbaren.size(); i++) {
                            int minposcand = erreichbaren.get(i).intValue();
                            int mincand = -1;
                            if (minposcand < zeile) { // Dreiecksmatrix
                                mincand = adj[zeile][minposcand];
                            } else {
                                mincand = adj[minposcand][zeile];
                            }

                            if (mincand < min) {
                                min = mincand;
                                minpos = minposcand;
                                minvpos = i;
                            }
                        }

                        if (minpos != -1) {// muss immer true sein

                            erreichbaren.remove(minvpos); // naehesten aus dem Vector der Erreichbaren entfernen
                            if (minpos == spalte) {// wenn der Naechste schon der Zielknoten ist
                                break; // dann hoeren wir auf, es gibt keinen naeheren Weg
                            } else {// relax

                                // wir suchen alle Knoten, die von minpos aus erreichbar sind
                                for (int i = 0; i < N; i++) {
                                    if (i != minpos && i != zeile) { // adj[i][minpos] fuer i==minpos ist immer
                                        // Integer.MAX_VALUE, daher
                                        // unwichtig

                                        int d = Integer.MAX_VALUE;
                                        if (i < minpos) {// Dreiecksmatrix
                                            d = adj[minpos][i];
                                        } else {
                                            d = adj[i][minpos];
                                        }

                                        if (d < Integer.MAX_VALUE) { // also erreichbar

                                            if (i < zeile) {// Dreiecksmatrix

                                                if (d + min < adj[zeile][i]) {
                                                    adj[zeile][i] = d + min;
                                                }
                                            } else {
                                                if (d + min < adj[i][zeile]) {
                                                    adj[i][zeile] = d + min;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /* DEBUG ONLY */
        for (int z = 0; z < N; z++) {
            for (int s = 0; s < N; s++) {
                if (adj[s][z] == Integer.MAX_VALUE) {
                    System.out.print("X");
                } else {
                    System.out.print(adj[s][z]);
                }
                System.out.print(" ");
            }
            System.out.print("\n");
        }
        /**/
        double[] f = new double[K * 2 + 1]; // -K, 0, +K
        for (int i = 0; i < K * 2 + 1; i++) {
            int k = i - K; // start at -K
            f[i] = 0.0; // initialize to 0

            if (k < 0) {// case of -K

                for (int u = 0; u < N; u++) {// go through all units

                    int xpos = u % xsize; // determine coordinates of current unit
                    int ypos = u / xsize;

                    for (int n = 0; n < 4; n++) {// 4 neighbours

                        int v = -1;
                        if (n == 0 && xpos - 1 >= 0) {// 1st neighbour = left neighbour
                            v = ypos * xsize + xpos - 1; // number of left neighbour unit (if exists)
                        } else if (n == 1 && ypos - 1 >= 0) { // 2nd neighbour = top neighbour
                            v = (ypos - 1) * xsize + xpos; // number of top neighbour unit (if exists)
                        } else if (n == 2 && xpos < xsize - 1) {// right neighbour
                            v = ypos * xsize + xpos + 1; // number of right neighbour (if exists)
                        } else if (n == 3 && ypos < ysize - 1) {// bottom neighbour
                            v = (ypos + 1) * xsize + xpos; // number of bottom neighbour (if exists)
                        }
                        if (v > -1) {// if neighbour exists

                            int d = -1;
                            if (v > u) {// Dreiecksmatrix
                                d = adj[v][u];
                            } else {
                                d = adj[u][v];
                            }
                            if (d > k * -1) {
                                f[i] += 1.0;
                            }
                        }
                    }
                }
            } else if (k > 0) {
                for (int z = 0; z < N; z++) {
                    for (int s = z + 1; s < N; s++) {
                        if (adj[s][z] == 1) {
                            int xpos1 = z % xsize;
                            int ypos1 = z / xsize;
                            int xpos2 = s % xsize;
                            int ypos2 = s / xsize;
                            int maxnorm = -1;
                            if (Math.abs(xpos1 - xpos2) > Math.abs(ypos1 - ypos2)) {
                                maxnorm = Math.abs(xpos1 - xpos2);
                            } else {
                                maxnorm = Math.abs(ypos1 - ypos2);
                            }

                            if (maxnorm > k) {
                                f[i] += 1.0;
                            }
                        }
                    }
                }
            }
            f[i] = f[i] / N;
        }
        f[K] = f[K - 1] + f[K + 1];

        return f;
    }
}
