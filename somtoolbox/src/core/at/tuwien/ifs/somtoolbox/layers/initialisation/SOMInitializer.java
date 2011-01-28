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
package at.tuwien.ifs.somtoolbox.layers.initialisation;

import java.io.FileNotFoundException;

import at.tuwien.ifs.somtoolbox.apps.initEval.EvaluationMain;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Stefan Bischof
 * @author Leo Sklenitzka
 * @version $Id: SOMInitializer.java 3893 2010-11-03 13:57:47Z mayer $
 */
public class SOMInitializer implements LayerInitializer {

    private Layer layer;

    private int xSize;

    private int ySize;

    private int zSize;

    private double[][][][] data;

    private int initzsize;

    private int initysize;

    private int initxsize;

    public SOMInitializer(Layer layer, int xSize, int ySize, int zSize) {
        this.layer = layer;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    /**
   * 
   */
    @Override
    public Unit[][][] initialize() {

        Unit[][][] units = new Unit[xSize][ySize][zSize];
        try {
            read();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SOMLibFileFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int k = 0; k < zSize; k++) {
            double[] a = asdf(k, initzsize, zSize);
            int z1 = (int) a[0];
            int z2 = (int) a[1];
            double w1_z = a[2];
            double w2_z = a[3];

            // suche zwei z-koordinaten-kandidaten z1, z2
            // sowie zwei gewichte (summe = 1)

            for (int j = 0; j < ySize; j++) {
                a = asdf(j, initysize, ySize);
                int y1 = (int) a[0];
                int y2 = (int) a[1];
                double w1_y = a[2];
                double w2_y = a[3];

                // suche zwei y-koordinaten-kandidaten y1, y2
                // sowie zwei gewichte (summe = 1)

                for (int i = 0; i < xSize; i++) {
                    a = asdf(i, initxsize, xSize);
                    int x1 = (int) a[0];
                    int x2 = (int) a[1];
                    double w1_x = a[2];
                    double w2_x = a[3];

                    // suche zwei x-koordinaten-kandidaten x1, x2
                    // sowie zwei gewichte (summe = 1)

                    // => 8 koordinaten aka. Units
                    double[][] vectors = null;
                    vectors = new double[][] {
                            // initSOM.getUnit(x1, y1, z1).getFeatureWeights(),
                            // initSOM.getUnit(x1, y1, z2).getFeatureWeights(),
                            // initSOM.getUnit(x1, y2, z1).getFeatureWeights(),
                            // initSOM.getUnit(x1, y2, z2).getFeatureWeights(),
                            // initSOM.getUnit(x2, y1, z1).getFeatureWeights(),
                            // initSOM.getUnit(x2, y1, z2).getFeatureWeights(),
                            // initSOM.getUnit(x2, y2, z1).getFeatureWeights(),
                            // initSOM.getUnit(x2, y2, z2).getFeatureWeights()
                            data[x1][y1][z1], data[x1][y1][z2], data[x1][y2][z1], data[x1][y2][z2], data[x2][y1][z1],
                            data[x2][y1][z2], data[x2][y2][z1], data[x2][y2][z2]

                    };
                    // => 8 gewichte
                    double[] weights = new double[] { w1_z * w1_y * w1_x, w2_z * w1_y * w1_x, w1_z * w2_y * w1_x,
                            w2_z * w2_y * w1_x, w1_z * w1_y * w2_x, w2_z * w1_y * w2_x, w1_z * w2_y * w2_x,
                            w2_z * w2_y * w2_x };

                    // vectorMean aufrufen und aktuelle Unit damit initialisieren
                    double[] newfeaturevector = vectorMean(vectors, weights);

                    units[i][j][k] = new Unit(layer, i, j, k, newfeaturevector);
                }
            }
        }

        return units;
    }

    /** Read weights from an already trained SOM */
    private void read() throws FileNotFoundException, SOMLibFileFormatException {
        String datasetname = EvaluationMain.getDatasetName();
        String path = "data/" + datasetname + "/output-" + datasetname + "/" + datasetname;
        SOMLibFormatInputReader r = new SOMLibFormatInputReader(path + ".wgt.gz", path + ".unit.gz", path + ".map");
        data = r.getVectors();
        initzsize = r.getZSize();
        initysize = r.getYSize();
        initxsize = r.getXSize();
    }

    /**
     * Calculates a weighted mean of vectors. Prone to numerical error: floating point arithmetics ...
     * 
     * @param vectors an array of double vectors
     * @param weights weights for a weighted mean calculation
     * @return a mean vector
     */
    protected static double[] vectorMean(double[][] vectors, double[] weights) throws IllegalArgumentException {
        int numVectors = vectors.length;
        int dimVectors = vectors[0].length;
        // if(v1.length != v2.length) {
        // throw new IllegalArgumentException("Unequal vector dimensionality");
        // }
        if (weights.length < numVectors) {
            throw new IllegalArgumentException("To few mean weights");
        }

        double[] means = new double[dimVectors];

        for (int i = 0; i < means.length; i++) {
            double mean = 0.0;
            for (int j = 0; j < numVectors; j++) {
                mean += vectors[j][i] * weights[j];
            }
            means[i] = mean;
        }

        return means;
    }

    private static double[] asdf(double k, double zinitsize, double zsize) {
        if (zsize <= 1) {
            return new double[] { 0.0, 0.0, 0.5, 0.5 };
        }
        double k0 = k * (zinitsize - 1) / (zsize - 1);
        double z1 = java.lang.Math.floor(k0);
        double z2 = java.lang.Math.ceil(k0);
        double weight1_z = k0 - z1;
        double weight2_z = z2 - k0;

        // System.out.println("k:" + k +
        // ", k0:"+k0+" ("+z1+","+z2+"), ("+weight1_z+","+weight2_z+")");

        return new double[] { z1, z2, weight1_z, weight2_z };
    }

    /** Test method for testing the other methods */
    public static void main(String[] args) {
        // double[][] vectors = new double[][] {
        // {1.0,4.0},
        // {2.0,5.0},
        // {3.0,6.0},
        // {4.0,7.0}
        // };
        // double[] weights = new double[] {0.25,0.25,0.25,0.25};
        // System.out.println(java.util.Arrays.toString(vectorMean(vectors, weights
        // )));

        // double zsize = 10.0;
        // double zinitsize = 5.0;
        // for (int k = 0; k < zsize; k++) {
        // double k0 = (k * (zinitsize - 1)) / (zsize - 1);
        // double a[] = asdf(k, zinitsize, zsize);
        // double z1 = a[0];
        // double z2 = a[1];
        // double weight1_z = a[2];
        // double weight2_z = a[3];
        //
        // }
    }
}
