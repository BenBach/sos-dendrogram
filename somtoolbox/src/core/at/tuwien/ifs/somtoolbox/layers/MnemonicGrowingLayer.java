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
package at.tuwien.ifs.somtoolbox.layers;

import java.util.ArrayList;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.comparables.UnitDistance;

/**
 * A mnemonic layer is a rectangular layer that might not have all grid positions occupied by units.
 * 
 * @version $Id: MnemonicGrowingLayer.java 3590 2010-05-21 10:43:45Z mayer $
 * @author Rudolf Mayer
 */
public class MnemonicGrowingLayer extends GrowingLayer {

    Integer[][][][][][] distanceMatrix_;

    private int unitCount;

    public void countDistances(int distanceFromStart, Unit startUnit, Unit currentUnit) {
        int xpos = currentUnit.getXPos();
        int ypos = currentUnit.getYPos();
        int zpos = currentUnit.getZPos();

        // the unit has not been reached yet or on a longer path
        if (distanceMatrix_[startUnit.getXPos()][startUnit.getYPos()][startUnit.getZPos()][xpos][ypos][zpos] == null
                || distanceMatrix_[startUnit.getXPos()][startUnit.getYPos()][startUnit.getZPos()][xpos][ypos][zpos].intValue() > distanceFromStart) {
            distanceMatrix_[startUnit.getXPos()][startUnit.getYPos()][startUnit.getZPos()][xpos][ypos][zpos] = new Integer(
                    distanceFromStart);

            // now we check all neighbours.
            if (xpos > 0 && units[xpos - 1][ypos][zpos] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos - 1][ypos][zpos]);
            }
            if (xpos + 1 < units.length && units[xpos + 1][ypos][zpos] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos + 1][ypos][zpos]);
            }
            if (ypos > 0 && units[xpos][ypos - 1][zpos] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos][ypos - 1][zpos]);
            }
            if (ypos + 1 < units[0].length && units[xpos][ypos + 1][zpos] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos][ypos + 1][zpos]);
            }
            if (zpos > 0 && units[xpos][ypos][zpos - 1] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos][ypos][zpos - 1]);
            }
            if (zpos + 1 < units[0][0].length && units[xpos][ypos][zpos + 1] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos][ypos][zpos + 1]);
            }
        }
    }

    public void initDistances() {
        distanceMatrix_ = new Integer[units.length][units[0].length][units[0][0].length][units.length][units[0].length][units[0][0].length];

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Calculating unit distances");
        int totalUnitNum = units.length * units[0].length * units[0][0].length;
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(totalUnitNum, "Restoring state of unit ", 10);
        int currentUnitNum = 0;

        for (int col = 0; col < units.length; col++) {
            for (int row = 0; row < units[0].length; row++) {
                for (int slice = 0; slice < units[0][0].length; slice++) {
                    currentUnitNum++;
                    if (units[col][row][slice] != null) {
                        progressWriter.progress("Calculating distance of unit " + col + "/" + row + "/" + slice + ", ",
                                (currentUnitNum + 1));
                        countDistances(0, units[col][row][slice], units[col][row][slice]);
                    } else {
                        progressWriter.progress("Skipping empty unit " + col + "/" + row + ", ", (currentUnitNum + 1));
                    }
                }
            }
        }
    }

    public MnemonicGrowingLayer(int id, Unit su, int x, int y, String metricName, int d, double[][][] vectors, long seed)
            throws SOMToolboxException {
        this(id, su, x, y, 0, metricName, d, GrowingLayer.addDimension(x, y, vectors), seed);
    }

    public MnemonicGrowingLayer(int id, Unit su, int x, int y, int z, String metricName, int d, double[][][][] vectors,
            long seed) throws SOMToolboxException {
        super(id, su, x, y, z, metricName, d, vectors, seed);
        initDistances();

        unitCount = 0;
        for (int j = 0; j < ySize; j++) {
            for (int i = 0; i < xSize; i++) {
                if (units[i][j][0] != null) {
                    unitCount++;
                }
            }
        }
    }

    @Override
    public Unit getWinner(InputDatum input) {
        Unit winner = null;
        double smallestDistance = Double.MAX_VALUE;
        // double[] inputVector = input.getVector().toArray();
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (units[i][j][k] != null) {
                        double distance = 0;
                        try {
                            distance = metric.distance(units[i][j][k].getWeightVector(), input);
                        } catch (MetricException e) {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                            System.exit(-1);
                        }
                        if (distance < smallestDistance) {
                            smallestDistance = distance;
                            winner = units[i][j][k];
                        }
                    }
                }
            }
        }
        return winner;
    }

    @Override
    public void clearMappedInput() {
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (units[i][j][k] != null) {
                        units[i][j][k].clearMappedInput();
                        if (units[i][j][k].getMappedSOM() != null) {
                            units[i][j][k].getMappedSOM().getLayer().clearMappedInput();
                        }
                    }
                }
            }
        }
    }

    @Override
    public double getMapDistance(int x1, int y1, int x2, int y2) {
        return getMapDistance(x1, y1, 0, x2, y2, 0);
    }

    @Override
    public double getMapDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return distanceMatrix_[x1][y1][z1][x2][y2][z2].doubleValue();
    }

    @Override
    protected void updateUnitsNormal(Unit winner, InputDatum input, double learnrate, double sigma) {
        double unitDist, hci = 0;
        double opt1 = 2 * sigma * sigma;
        double[] unitVector = null;
        double[] inputVector = input.getVector().toArray();

        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (units[i][j] != null) {
                        // Sparse city block distance
                        unitDist = getMapDistance(winner, units[i][j][k]);

                        // hci = learnrate * Math.exp((-1*Math.pow((unitDist/opt1),2)));
                        hci = learnrate * Math.exp(-1 * unitDist * unitDist / opt1);
                        unitVector = units[i][j][k].getWeightVector();
                        for (int ve = 0; ve < dim; ve++) {
                            unitVector[ve] = unitVector[ve] + hci * (inputVector[ve] - unitVector[ve]);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Unit[] getAllUnits() {
        ArrayList<Unit> tempUnits = new ArrayList<Unit>(xSize * ySize * zSize / 2);
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (units[i][j][k] != null) {
                        tempUnits.add(units[i][j][k]);
                    }
                }
            }
        }
        return tempUnits.toArray(new Unit[tempUnits.size()]);
    }

    @Override
    public UnitDistance[] getWinnersAndDistances(InputDatum input, int num) {
        int maxNum = 0;
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (units[i][j][k] != null) {
                        maxNum++;
                    }
                }
            }
        }

        if (num > maxNum) {
            num = maxNum;
        }
        UnitDistance[] res = new UnitDistance[num];
        DoubleMatrix1D vec = input.getVector();
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (units[i][j][k] != null) {
                        double distance = 0;
                        try {
                            distance = metric.distance(units[i][j][k].getWeightVector(), vec);
                        } catch (MetricException e) {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                            System.exit(-1);
                        }
                        int element = 0;
                        boolean inserted = false;
                        while (inserted == false && element < num) {
                            if (res[element] == null || distance < res[element].getDistance()) { // found place to
                                // insert unit
                                for (int m = num - 2; m >= element; m--) { // move units with greater distance to
                                    // right
                                    res[m + 1] = res[m];
                                }
                                res[element] = new UnitDistance(units[i][j][k], distance);
                                inserted = true;
                            }
                            element++;
                        }
                    }

                }
            }
        }
        return res;
    }

    @Override
    public int getUnitCount() {
        return unitCount;
    }

}