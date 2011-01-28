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
package at.tuwien.ifs.somtoolbox.layers.metrics;

import java.awt.Point;

import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * A metric for mnemonic SOMs. The metric is basically a Manhattan/L1 Metric, but takes into consideration that not all
 * paths between two units might be possible (as the grid of the Mnemonic SOM might be sparse). For performance reasons,
 * a distance matrix is pre-calculated.
 * 
 * @version $Id: MnemonicSOMMetric.java 3586 2010-05-21 10:34:19Z mayer $
 * @author Rudolf Mayer
 */
public class MnemonicSOMMetric extends L2Metric {
    int[][][][] distanceMatrix;

    Integer[][][][] distanceMatrix_;

    /** @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[]) */
    @Override
    public double distance(InputDatum datum1, InputDatum datum2) throws MetricException {
        Point point1 = (Point) datum1.getProperty("coordinates");
        Point point2 = (Point) datum2.getProperty("coordinates");
        if (point1 == null || point2 == null) {
            throw new IllegalArgumentException("Input data do not contain 'coordinates' property");
        }
        return distanceMatrix[point1.x][point1.y][point2.x][point2.y];
    }

    public void countDistances(int distanceFromStart, Unit startUnit, Unit currentUnit, Unit[][] units) {
        int xpos = currentUnit.getXPos();
        int ypos = currentUnit.getYPos();
        if (distanceMatrix_[startUnit.getXPos()][startUnit.getYPos()][xpos][ypos] == null
                || distanceMatrix_[startUnit.getXPos()][startUnit.getYPos()][xpos][ypos].intValue() > distanceFromStart) {
            // the unit has not been reached yet or on a longer path
            distanceMatrix_[startUnit.getXPos()][startUnit.getYPos()][xpos][ypos] = new Integer(distanceFromStart);

            // now we check all neighbours.
            if (xpos > 0 && units[xpos - 1][ypos] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos - 1][ypos], units);
            }

            if (xpos + 1 < units.length && units[xpos + 1][ypos] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos + 1][ypos], units);
            }

            if (ypos > 0 && units[xpos][ypos - 1] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos][ypos - 1], units);
            }

            if (ypos + 1 < units[0].length && units[xpos][ypos + 1] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos][ypos + 1], units);
            }
        }
    }

    public MnemonicSOMMetric(Unit[][] units) {
        // init the non-empty units to have a max-value distance.
        distanceMatrix_ = new Integer[units.length][units[0].length][units.length][units[0].length];

        for (Unit[] unit : units) {
            for (int row = 0; row < units[0].length; row++) {
                if (unit[row] != null) {
                    countDistances(0, unit[row], unit[row], units);
                }
            }
        }
    }

}
