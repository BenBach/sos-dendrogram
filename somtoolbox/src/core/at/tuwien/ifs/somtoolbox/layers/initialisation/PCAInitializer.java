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

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.util.PCA;

/**
 * SOM Principal Component Initializer
 * 
 * @author Stefan Bischof
 * @author Leo Sklenitzka
 */
public class PCAInitializer implements LayerInitializer {
    private Layer layer;

    private int xSize;

    private int ySize;

    private int zSize;

    private InputData data;

    private int dim;

    public PCAInitializer(Layer layer, int size, int size2, int size3, InputData data, int dim) {
        this.layer = layer;
        xSize = size;
        ySize = size2;
        zSize = size3;
        this.data = data;
        this.dim = dim;
    }

    /**
     * Initialize the SOM Layer using Principal Component Analysis
     * 
     * @return initialized SOM
     */
    @Override
    public Unit[][][] initialize() {
        Unit[][][] units = new Unit[xSize][ySize][zSize];

        double[][] dataArray = data.getData();
        double[][] projectedDataArray = new double[data.numVectors()][2];

        // perform PCA
        System.out.println("");
        System.out.println("  *** Calculating PCA...");
        PCA pca = new PCA(dataArray);

        //
        // find the 2 main axis from the PCA
        //
        double firstAxisVar = Double.MIN_VALUE;
        double secondAxisVar = Double.MIN_VALUE;
        int firstAxisIndex = -1;
        int secondAxisIndex = -1;

        for (int curAxis = 0; curAxis < dim; curAxis++) {
            if (pca.info[curAxis] > firstAxisVar) {
                secondAxisVar = firstAxisVar;
                secondAxisIndex = firstAxisIndex;

                firstAxisVar = pca.info[curAxis];
                firstAxisIndex = curAxis;
            } else if (pca.info[curAxis] > secondAxisVar) {
                secondAxisVar = pca.info[curAxis];
                secondAxisIndex = curAxis;
            }
        }

        System.out.println("");
        System.out.println("  *** firstAxisIndex: " + firstAxisIndex + " secondAxisIndex: " + secondAxisIndex);

        //
        // project the data points
        //
        for (int i = 0; i < data.numVectors(); i++) {
            float xProj = 0.f;

            for (int j = 0; j < dim; j++) {
                xProj += dataArray[i][j] * pca.U[firstAxisIndex][j];
            }

            projectedDataArray[i][0] = xProj;

            float yProj = 0.f;

            for (int j = 0; j < dim; j++) {
                yProj += dataArray[i][j] * pca.U[secondAxisIndex][j];
            }

            projectedDataArray[i][1] = yProj;
        }

        // find minX,minY,maxX,maxY
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (int i = 0; i < data.numVectors(); i++) {
            if (projectedDataArray[i][0] < minX) {
                minX = projectedDataArray[i][0];
            }
            if (projectedDataArray[i][1] < minY) {
                minY = projectedDataArray[i][1];
            }

            if (projectedDataArray[i][0] > maxX) {
                maxX = projectedDataArray[i][0];
            }
            if (projectedDataArray[i][1] > maxY) {
                maxY = projectedDataArray[i][1];
            }
        }

        double diffX = maxX - minX;
        double diffY = maxY - minY;
        double cellSizeX = diffX / xSize;
        double cellSizeY = diffY / ySize;

        System.out.println("");
        System.out.println("  *** diffX: " + diffX + " diffY: " + diffY);

        // ...
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    // find the closes point in the data point cloud
                    int closestPointIndex = -1;
                    double closesPointDist = Double.MAX_VALUE;

                    for (int curPoint = 0; curPoint < data.numVectors(); curPoint++) {
                        double[] curCellCoords = new double[2];

                        curCellCoords[0] = i * cellSizeX + cellSizeX / 2;
                        curCellCoords[1] = j * cellSizeY + cellSizeY / 2;

                        double curPointDist = Math.sqrt(Math.pow(projectedDataArray[curPoint][0] - curCellCoords[0], 2)
                                + Math.pow(projectedDataArray[curPoint][1] - curCellCoords[1], 2));

                        if (curPointDist < closesPointDist) {
                            closesPointDist = curPointDist;
                            closestPointIndex = curPoint;
                        }
                    }

                    double[] closesPointVec = new double[dim];

                    for (int l = 0; l < dim; l++) {
                        closesPointVec[l] = dataArray[closestPointIndex][l];
                    }

                    units[i][j][k] = new Unit(layer, i, j, closesPointVec);
                }
            }
        }

        return units;
    }

}
