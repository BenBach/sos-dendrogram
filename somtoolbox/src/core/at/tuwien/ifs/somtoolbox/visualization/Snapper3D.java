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
package at.tuwien.ifs.somtoolbox.visualization;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.clustering.DistanceFunctionType;
import at.tuwien.ifs.somtoolbox.clustering.functions.ComponentLine3DDistance;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.util.Point3d;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

public class Snapper3D extends Snapper {

    private Point3d[] grid;

    private static double gridSize = 0d;

    /*
     * public static double[][] convertPointArrayToDoubleArrayArray(Point2D[] centres){ double[][] doubleCentres = new double[centres.length][2]; for
     * (int i = 0; i < centres.length; i++) { doubleCentres[i][1] = centres[i].getX(); doubleCentres[i][1] = centres[i].getY(); } return
     * doubleCentres; }
     */

    // public Snapper2D(AbstractMetric distanceFunction, LineDistanceFunction lineDistanceFunction, int xSize, int
    // ySize) {
    public Snapper3D(AbstractMetric distanceFunction, DistanceFunctionType lineDistanceFunction) {
        this.distanceFunction = distanceFunction;
        this.lineDistanceFunction = lineDistanceFunction;
    }

    public Snapper3D(double gridSize, AbstractMetric distanceFunction, DistanceFunctionType lineDistanceFunction) {
        this.distanceFunction = distanceFunction;
        this.lineDistanceFunction = lineDistanceFunction;
    }

    public static double[] normalise(double[] vec) {
        double maxValue = 0d;
        double minValue = 0d;
        // we don't normalise vectors of length 1 (I'm sick of having 2.0 at every position)
        if (vec.length == 1) {
            return vec;
        }
        for (int i = 0; i < vec.length; i++) {
            double currentValue = vec[i];
            if (i == 0) {
                maxValue = currentValue;
                minValue = currentValue;
            }
            if (currentValue > maxValue) {
                maxValue = currentValue;
            }
            if (currentValue < minValue) {
                minValue = currentValue;
            }
        }

        for (int i = 0; i < vec.length; i++) {
            if (maxValue == 0) {
                vec[i] = 0;
            } else {
                vec[i] = (vec[i] + minValue) / maxValue;
            }
        }
        return vec;
    }

    public Point3d[] doSnapping(double[][] centres) throws SOMToolboxException {
        Point3d[] line = new Point3d[centres.length];
        for (int i = 0; i < centres.length; i++) {
            line[i] = new Point3d(centres[i][0], centres[i][1], centres[i][2]);
        }
        return doSnapping(line);
    }

    public Point3d[] doSnapping(Point3d[] line) throws SOMToolboxException {
        // int numberOfBins = binCentres[0].length;
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Starting 3d snapping process");
        Point3d[] snappedBinCentres = new Point3d[line.length];
        // for (int i_components = 0; i_components < centres.length; i_components++) {
        // FIXME: kind of static
        // Point3d[] x = centres[i_components];
        // snappedBinCentres[i_components] = snap(centres[i_components], grid.length, 3);
        // }

        // FIXME
        // Point3d[] ar = new Point3d[centres.length];
        // for (int i = 0; i < centres.length; i++) {
        // ar[i] = new Point3d(centres[i][0], centres[i][1], centres[i][2]);
        // }
        System.out.println("start snapping for : " + ArrayUtils.toString(line));
        snappedBinCentres = snap(line);
        // Point3d[][] snappedCentres = snappedBinCentres;
        // Hashtable<Point3d, int[]> outgoingLineTable = new Hashtable<Point3d, int[]>(); // find counts for parallel
        // lines
        // for (int l = 0; l < snappedCentres.length; l++) { // for each component
        // Point3d[] snappedLine = snappedCentres[l];
        // }
        return snappedBinCentres;
    }

    /**
     * returns the direction between two nodes based on the following scheme: 0 7 left up up right up 1 \ | / 6 left - *
     * - right 6 / | \ 5 left down down right down 3 4
     * 
     * @param current current node
     * @param next next node to go to
     * @return dir from current to next
     */
    private int getDirection(Point3d current, Point3d next) {
        // this handles the case when this annoying function is called with identical parameters
        int dir = -1;
        // up
        if (current.x == next.x && current.y > next.y) {
            dir = 0;
        }
        // right up
        if (current.x < next.x && current.y > next.y) {
            dir = 1;
        }
        // right
        if (current.x < next.x && current.y == next.y) {
            dir = 2;
        }
        // right down
        if (current.x < next.x && current.y < next.y) {
            dir = 3;
        }
        // down
        if (current.x == next.x && current.y < next.y) {
            dir = 4;
        }
        // left down
        if (current.x > next.x && current.y < next.y) {
            dir = 5;
        }
        // left
        if (current.x > next.x && current.y == next.y) {
            dir = 6;
        }
        // left up
        if (current.x > next.x && current.y > next.y) {
            dir = 7;
        }
        return dir;
    }

    // TODO make private again
    public Point3d[] createGrid(int xSize, int ySize, int zSize) {
        // createGrid(xSize, ySize, zSize, "", "", "");
        // int xSize = gsom.getLayer().getXSize();
        // int ySize = gsom.getLayer().getYSize();
        // ]

        if (gridSize != 0) {
            // xSize = (int) gridSize;
            // ySize = (int) gridSize;
            // zSize = (int) gridSize;
        }
        // FIXME: static ...
        // int xSize = new Double(gridSize).intValue();
        // int ySize = new Double(gridSize).intValue();
        // int zSize = new Double(gridSize).intValue();

        // if (grid == null) {
        // init the data structures for neighbourhood lookup

        grid = new Point3d[xSize * ySize * zSize];
        int index = 0;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                // allSOMCoordinates[x * ySize + y] = new Point2D.Double(x, y);
                for (int z = 0; z < zSize; z++) {
                    // System.out.println(x + " / " + y + " / " + z + " | " + index);// + (x + y + z) + "| " + (x*y*z) +
                    // " | index: " + (x * xSize
                    // + y * ySize + z));
                    Point3d p = new Point3d(x, y, z);
                    // System.out.println(p);
                    grid[index++] = p;

                }
            }
        }
        /*
         * gridSumsXY = new double[grid.length]; gridDiffsXY = new double[grid.length]; gridSumsYZ = new double[grid.length]; gridDiffsYZ = new
         * double[grid.length]; gridSumsXZ = new double[grid.length]; gridDiffsXZ = new double[grid.length]; for (int i = 0; i < gridSumsXY.length;
         * i++) { gridSumsXY[i] = grid[i].x + grid[i].y; // + grid[i].y; gridSumsYZ[i] = grid[i].y + grid[i].z; // + grid[i].y; gridSumsXZ[i] =
         * grid[i].x + grid[i].z; // + grid[i].y; gridDiffsXY[i] = grid[i].x - grid[i].y; // - grid[i].y; gridDiffsYZ[i] = grid[i].y - grid[i].z; // -
         * grid[i].y; gridDiffsXZ[i] = grid[i].x - grid[i].z; // + grid[i].y; }
         */
        // }
        // System.out.println(ArrayUtils.toString(grid));
        // System.out.println(ArrayUtils.toString(gridSums));
        // System.out.println(ArrayUtils.toString(gridDiffs));
        // System.out.println(grid.length);
        // System.out.println(grid[0][0].length);
        // System.out.println(ArrayUtils.toString(grid));
        return grid;
    }

    public double[][] getGrid() {
        return convert(grid);
    }

    public double[][] convert(Point3d[] array) {
        double[][] data = new double[array.length][3];
        for (int i = 0; i < array.length; i++) {
            data[i] = new double[] { array[i].x, array[i].y, array[i].z };
        }
        return data;
    }

    /**
     * Returns a snapped line of the given line. Snapping the metro lines means to find a line as similar as possible to
     * the given line, which has all bin centres in the unit centres, and line segments are connected in multiples of
     * 45° degree angles to each other.<br>
     * TODO: Consider disallowing 135° / 315° as too sharp turns. private Point2D[] snap(Point2D[] line, int xSize, int
     * ySize) { // Snapping process // 1. For each bin centre, find the 4 neighbouring Unit locations, thus resulting in
     * bins * 4 points // 2. For each point: consider the point as fixed, and find a line that is correctly snapped
     * (only 45°) angles and as near as possible to the // original line // 3. From the resulting bins * 4 lines, chose
     * the one closest to the original line ArrayList<Point2D[]> allSnappedLines = new ArrayList<Point2D[]>(); for (int
     * i = 0; i < line.length; i++) { Point2D[] neighbouringPoints = getNeighbouringUnits(line[i]); for (int j = 0; j <
     * neighbouringPoints.length; j++) { // find the snapped points forward and backwards from the current bin point //
     * this means we will have lines e.g. as follows (for 6 bins, and i == 3) // lineSegmentForward = (0/0 0/0 0/0 0/0
     * x5/y5 x6/y6) // lineSegmentBackward = (x1/y1 x2/y2, x3/y3 0/0 0/0 0/0) Point2D[] lineSegmentForward =
     * snapPoint(neighbouringPoints[j], line, i + 1, +1, xSize, ySize, centres[0].length); Point2D[] lineSegmentBackward
     * = snapPoint(neighbouringPoints[j], line, i - 1, -1, xSize, ySize, centres[0].length); // then merge them to one
     * line, and set the fixed point Point2D[] mergedLine = new Point2D[lineSegmentForward.length]; for (int k = 0; k <
     * lineSegmentBackward.length; k++) { mergedLine[k] = new Point2D.Double(lineSegmentForward[k].getX() +
     * lineSegmentBackward[k].getX(), lineSegmentForward[k].getY() + lineSegmentBackward[k].getY()); } mergedLine[i] =
     * neighbouringPoints[j]; // now if that point is the same as the last one, don't add that one // for some strange
     * reason this is required additionally to the condition in the recursion if ((i > 1 && (!(line[i].getX() == line[i
     * - 1].getX() && line[i].getY() == line[i - 1].getY())))) { allSnappedLines.add(mergedLine); } } } // find the
     * closest snapped line double minDist = Double.MAX_VALUE; Point2D[] minDistLine = null; for (int i = 0; i <
     * allSnappedLines.size(); i++) { Point2D[] currentLine = (Point2D[]) allSnappedLines.get(i); double dist =
     * LineDistance.lineDistance(line, currentLine, lineDistanceFunction); if (dist < minDist) { minDist = dist;
     * minDistLine = currentLine; } } return minDistLine; }
     */
    /**
     * Returns a snapped line of the given line. Snapping the metro lines means to find a line as similar as possible to
     * the given line, which has all bin centres in the unit centres, and line segments are connected in multiples of
     * 45° degree angles to each other.<br>
     * TODO: Consider disallowing 135° / 315° as too sharp turns.
     */
    private Point3d[] snap(Point3d[] line) {
        // Snapping process
        // 1. For each bin centre, find the 4 neighbouring Unit locations, thus resulting in bins * 4 points
        // 2. For each point: consider the point as fixed, and find a line that is correctly snapped (only 45°) angles
        // and as near as possible to the
        // original line
        // 3. From the resulting bins * 4 lines, chose the one closest to the original line
        ArrayList<Point3d[]> allSnappedLines = new ArrayList<Point3d[]>();

        StdErrProgressWriter progress = new StdErrProgressWriter(line.length * 8, "calc snap ", line.length * 8 / 100);
        for (int i = 0; i < line.length; i++) {
            System.out.println("\tsnapping point: " + line[i]);
            Point3d[] neighbouringPoints = getNeighbouringUnits(line[i]);
            for (Point3d neighbouringPoint : neighbouringPoints) {
                // find the snapped points forward and backwards from the current bin point
                // this means we will have lines e.g. as follows (for 6 bins, and i == 3)
                // lineSegmentForward = (0/0 0/0 0/0 0/0 x5/y5 x6/y6)
                // lineSegmentBackward = (x1/y1 x2/y2, x3/y3 0/0 0/0 0/0)
                Point3d[] lineSegmentForward = snapPoint(neighbouringPoint, line, i + 1, +1, line.length);
                Point3d[] lineSegmentBackward = snapPoint(neighbouringPoint, line, i - 1, -1, line.length);

                // then merge them to one line, and set the fixed point
                Point3d[] mergedLine = new Point3d[lineSegmentForward.length];
                for (int k = 0; k < lineSegmentBackward.length; k++) {
                    // TODO does this have to be normalised?
                    mergedLine[k] = new Point3d((lineSegmentForward[k].x + lineSegmentBackward[k].x),
                            (lineSegmentForward[k].y + lineSegmentBackward[k].y),
                            (lineSegmentForward[k].z + lineSegmentBackward[k].z));
                }
                mergedLine[i] = neighbouringPoint;
                // System.out.println("candidate: " + i + " " + ArrayUtils.toString(mergedLine[i]));

                // now if that point is the same as the last one, don't add that one
                // for some strange reason this is required additionally to the condition in the recursion
                // FIXME is this necessary?
                // FIXME was this necessary?
                // if ((i > 1
                // && (!(line[i].x == line[i - 1].x
                // && line[i].y == line[i - 1].y
                // && line[i].z == line[i - 1].z)))) {
                allSnappedLines.add(mergedLine);
                // System.out.println("chose that one: " + ArrayUtils.toString(mergedLine));
                // }
                // progress.progress();
            }

        }
        // find the closest snapped line
        double minDist = Double.MAX_VALUE;
        Point3d[] minDistLine = null;
        // System.out.println("found " + allSnappedLines.size() + " solutions.");
        if (allSnappedLines.size() == 0) {
            return line.clone();
        }
        for (int i = 0; i < allSnappedLines.size(); i++) {
            Point3d[] currentLine = allSnappedLines.get(i);
            double dist = new ComponentLine3DDistance(lineDistanceFunction).distance(line, currentLine);
            // System.out.println("candidate: " + i + " " + ArrayUtils.toString(currentLine));
            if (dist < minDist) {
                minDist = dist;
                minDistLine = currentLine;
            }
        }
        // return allSnappedLines.get(8);
        return minDistLine;
    }

    /**
     * Snaps the next point on the line.
     * 
     * @param startPoint the point to start from
     * @param line the line to snap
     * @param currentPosition the current position on the line
     * @param direction forward (1) or backwards (-1)
     * @param bins number of bins
     * @return a snapped line
     */
    private Point3d[] snapPoint(Point3d startPoint, Point3d[] line, int currentPosition, int direction, int bins) {
        Point3d[] result = new Point3d[bins];
        if (currentPosition == -1 && direction == -1 || currentPosition == bins && direction == 1) {
            for (int i = 0; i < result.length; i++) {
                result[i] = new Point3d(0d, 0d, 0d);
            }
            return result;
        }

        // System.out.println(startPoint);

        // int startPointCoordinatesSumXY = (int) (startPoint.x + startPoint.y); // + startPoint.z);
        // int startPointCoordinatesSumYZ = (int) (startPoint.y + startPoint.z); // + startPoint.z);
        // int startPointCoordinatesSumXZ = (int) (startPoint.x + startPoint.z); // + startPoint.z);

        // int startPointCoordinatesDifferenceXY = (int) (startPoint.x - startPoint.y); // - startPoint.z);
        // int startPointCoordinatesDifferenceYZ = (int) (startPoint.y - startPoint.z); // - startPoint.z);
        // int startPointCoordinatesDifferenceXZ = (int) (startPoint.x - startPoint.z); // - startPoint.z);
        double minDistance = Double.MAX_VALUE;
        Point3d closestPoint = null;

        // TODO
        // System.out.println(ArrayUtils.toString(line));
        // System.out.println("currentposition: " + currentPosition);
        // System.out.println("direction: " + direction);
        // System.out.println("bins: " + bins);
        for (Point3d element : grid) {
            // find units that are either in the same row (x equal), same column (y equal) or are in a diagonal (sum or
            // diff values equal)
            // if (grid[i].x == startPoint.x || grid[i].y == startPoint.y || grid[i].z == startPoint.z
            // || gridSums[i] == startPointCoordinatesSum || gridDiffs[i] == startPointCoordinatesDifference) {
            /*
             * if ( (grid[i].x == startPoint.x || gridSumsXY[i] == startPointCoordinatesSumXY || gridSumsXY[i] == startPointCoordinatesSumXY) // &&
             * grid[i].x == startPoint.z)// && grid[i].x == startPoint.y) && (grid[i].y == startPoint.y || gridSumsYZ[i] == startPointCoordinatesSumYZ
             * || gridDiffsYZ[i] == startPointCoordinatesDifferenceYZ) // && grid[i].y == startPoint.z) // && grid[i].x == grid[i].x) // && (grid[i].z
             * == startPoint.z // || gridSumsXZ[i] == startPointCoordinatesSumXZ // || gridDiffsXZ[i] == startPointCoordinatesDifferenceXZ)) ) {
             */

            if ((element.x == startPoint.x || element.y == startPoint.y || Math.abs(element.x - startPoint.x) == Math.abs(element.y
                    - startPoint.y))
                    && (element.x == startPoint.x || element.z == startPoint.z || Math.abs(element.x - startPoint.x) == Math.abs(element.z
                            - startPoint.z))
                    && (element.z == startPoint.z || element.y == startPoint.y || Math.abs(element.z - startPoint.z) == Math.abs(element.y
                            - startPoint.y))) {

                double currentDistance = element.distance(line[currentPosition]);
                if (currentDistance < minDistance) {
                    closestPoint = (Point3d) element.clone();
                    minDistance = currentDistance;
                }
            }
        }

        // compare this startpoint to the last one and check for identity and don't consider the closest but the point
        // itself for further processing
        // if so
        if (currentPosition > 1 && line[currentPosition].x == line[currentPosition - direction].x
                && line[currentPosition].y == line[currentPosition - direction].y
                && line[currentPosition].z == line[currentPosition - direction].z) {
            result = snapPoint(startPoint, line, currentPosition + direction, direction, bins);
            result[currentPosition] = (Point3d) startPoint.clone();
        } else {
            result = snapPoint(closestPoint, line, currentPosition + direction, direction, bins);
            result[currentPosition] = (Point3d) closestPoint.clone();
        }
        return result;
    }

    public Point3d[] getNeighbouringUnits(Point3d p) {
        boolean debug = false;

        if (debug) {
            System.out.println(p);
        }

        Point3d leftUpperLow = new Point3d(new Double(p.x).intValue(), new Double(p.y).intValue(),
                new Double(p.z).intValue());

        Point3d leftUpperHigh = new Point3d(leftUpperLow.x, leftUpperLow.y, leftUpperLow.z + 1);
        Point3d leftLowerLow = new Point3d(leftUpperLow.x + 1, leftUpperLow.y, leftUpperLow.z);
        Point3d leftLowerHigh = new Point3d(leftUpperLow.x + 1, leftUpperLow.y, leftUpperLow.z + 1);

        Point3d rightUpperLow = new Point3d(leftUpperLow.x, leftUpperLow.y + 1, leftUpperLow.z);
        Point3d rightUpperHigh = new Point3d(leftUpperLow.x, leftUpperLow.y + 1, leftUpperLow.z + 1);
        Point3d rightLowerLow = new Point3d(leftUpperLow.x + 1, leftUpperLow.y + 1, leftUpperLow.z);
        Point3d rightLowerHigh = new Point3d(leftUpperLow.x + 1, leftUpperLow.y + 1, leftUpperLow.z + 1);

        // now limit the options if any of the three input
        // coordinates is an integer already
        if (p.x % 1 == 0) {
            leftUpperLow.x = p.x;
            leftUpperHigh.x = p.x;
            leftLowerLow.x = p.x;
            leftLowerHigh.x = p.x;
            rightUpperLow.x = p.x;
            rightUpperHigh.x = p.x;
            rightLowerLow.x = p.x;
            rightLowerHigh.x = p.x;
        }
        if (p.y % 1 == 0) {
            leftUpperLow.y = p.y;
            leftUpperHigh.y = p.y;
            leftLowerLow.y = p.y;
            leftLowerHigh.y = p.y;
            rightUpperLow.y = p.y;
            rightUpperHigh.y = p.y;
            rightLowerLow.y = p.y;
            rightLowerHigh.y = p.y;
        }
        if (p.z % 1 == 0) {
            leftUpperLow.z = p.z;
            leftUpperHigh.z = p.z;
            leftLowerLow.z = p.z;
            leftLowerHigh.z = p.z;
            rightUpperLow.z = p.z;
            rightUpperHigh.z = p.z;
            rightLowerLow.z = p.z;
            rightLowerHigh.z = p.z;
        }

        // TODO remove this shit
        if (debug) {
            System.out.println("leftUpperLow: " + leftUpperLow);
            System.out.println("leftUpperHigh: " + leftUpperHigh);

            System.out.println("leftLowerLow: " + leftLowerLow);
            System.out.println("leftLowerHigh: " + leftLowerHigh);

            System.out.println();

            System.out.println("rightUpperLow: " + rightUpperLow);
            System.out.println("rightUpperHigh: " + rightUpperHigh);

            System.out.println("rightLowerLow: " + rightLowerLow);
            System.out.println("rightLowerHigh: " + rightLowerHigh);
        }

        return new Point3d[] { leftUpperLow, leftUpperHigh, leftLowerLow, leftLowerHigh, rightUpperLow, rightUpperHigh,
                rightLowerLow, rightLowerHigh };

    }
}
