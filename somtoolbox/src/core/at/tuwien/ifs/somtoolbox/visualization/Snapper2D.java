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

import java.awt.geom.Point2D;
import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.clustering.DistanceFunctionType;
import at.tuwien.ifs.somtoolbox.clustering.functions.ComponentLine2DDistance;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;

public class Snapper2D extends Snapper {

    private Point2D[] grid;

    private double[] gridSums, gridDiffs;

    public Snapper2D(AbstractMetric distanceFunction, DistanceFunctionType lineDistanceFunction, int xSize, int ySize) {
        super(distanceFunction, lineDistanceFunction);
        createGrid(xSize, ySize);
    }

    public Point2D[] createGrid(int xSize, int ySize) {
        grid = new Point2D[xSize * ySize];
        int index = 0;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                Point2D.Double p = new Point2D.Double(x, y);
                grid[index++] = p;
            }
        }
        gridSums = new double[grid.length];
        gridDiffs = new double[grid.length];
        for (int i = 0; i < gridSums.length; i++) {
            gridSums[i] = grid[i].getX() + grid[i].getY();
            gridDiffs[i] = grid[i].getX() - grid[i].getY();
        }
        return grid;
    }

    /**
     * Snaps the next point on the line.
     * 
     * @param startPoint the point to start from
     * @param line the line to snap
     * @param currentPosition the current position on the line
     * @param direction forward (1) or backwards (-1)
     * @return a snapped line
     */
    private Point2D[] snapPoint(Point2D startPoint, Point2D[] line, int currentPosition, int direction) {
        Point2D[] result = new Point2D[line.length];
        if (currentPosition == -1 && direction == -1 || currentPosition == line.length && direction == 1) {
            for (int i = 0; i < result.length; i++) {
                result[i] = new Point2D.Double(0, 0);
            }
            return result;
        }

        int startPointCoordinatesSum = (int) (startPoint.getX() + startPoint.getY());
        int startPointCoordinatesDifference = (int) (startPoint.getX() - startPoint.getY());
        double minDistance = Double.MAX_VALUE;
        Point2D closestPoint = null;

        for (int i = 0; i < grid.length; i++) {
            // find units that are either in the same row (x equal), same column (y equal) or are in a diagonal (sum or
            // diff values equal)
            if (grid[i].getX() == startPoint.getX() || grid[i].getY() == startPoint.getY()
                    || gridSums[i] == startPointCoordinatesSum || gridDiffs[i] == startPointCoordinatesDifference) {
                double currentDistance = grid[i].distance(line[currentPosition]);
                if (currentDistance < minDistance) {
                    closestPoint = grid[i];
                    minDistance = currentDistance;
                }
            }
        }

        // compare this startpoint to the last one and check for identity and don't consider the closest but the point
        // itself for further processing
        // if so
        if (currentPosition > 1 && line[currentPosition].getX() == line[currentPosition - direction].getX()
                && line[currentPosition].getY() == line[currentPosition - direction].getY()) {
            result = snapPoint(startPoint, line, currentPosition + direction, direction);
            result[currentPosition] = startPoint;
        } else {
            result = snapPoint(closestPoint, line, currentPosition + direction, direction);
            result[currentPosition] = closestPoint;
        }
        return result;
    }

    public Point2D[][] snap(Point2D[][] lines) {
        Point2D[][] snappedLines = new Point2D[lines.length][lines[0].length];
        for (int i = 0; i < lines.length; i++) {
            snappedLines[i] = snap(lines[i]);
        }
        return snappedLines;
    }

    /**
     * Returns a snapped line of the given line. Snapping the metro lines means to find a line as similar as possible to
     * the given line, which has all bin centres in the unit centres, and line segments are connected in multiples of
     * 45° degree angles to each other.<br>
     * TODO: Consider disallowing 135° / 315° as too sharp turns.
     */
    public Point2D[] snap(Point2D[] line) {
        // Snapping process
        // 1. For each bin centre, find the 4 neighbouring Unit locations, thus resulting in bins * 4 points
        // 2. For each point: consider the point as fixed, and find a line that is correctly snapped (only 45°) angles
        // and as near as possible to the
        // original line
        // 3. From the resulting bins * 4 lines, chose the one closest to the original line
        ArrayList<Point2D[]> allSnappedLines = new ArrayList<Point2D[]>();

        for (int i = 0; i < line.length; i++) {
            Point2D[] neighbouringPoints = getNeighbouringUnits(line[i]);
            for (Point2D neighbouringPoint : neighbouringPoints) {
                // find the snapped points forward and backwards from the current bin point
                // this means we will have lines e.g. as follows (for 6 bins, and i == 3)
                // lineSegmentForward = (0/0 0/0 0/0 0/0 x5/y5 x6/y6)
                // lineSegmentBackward = (x1/y1 x2/y2, x3/y3 0/0 0/0 0/0)
                Point2D[] lineSegmentForward = snapPoint(neighbouringPoint, line, i + 1, +1);
                Point2D[] lineSegmentBackward = snapPoint(neighbouringPoint, line, i - 1, -1);

                // then merge them to one line, and set the fixed point
                Point2D[] mergedLine = new Point2D[lineSegmentForward.length];
                for (int k = 0; k < lineSegmentBackward.length; k++) {
                    mergedLine[k] = new Point2D.Double(lineSegmentForward[k].getX() + lineSegmentBackward[k].getX(),
                            lineSegmentForward[k].getY() + lineSegmentBackward[k].getY());
                }
                mergedLine[i] = neighbouringPoint;

                // now if that point is the same as the last one, don't add that one
                // for some strange reason this is required additionally to the condition in the recursion
                if (i > 1 && !(line[i].getX() == line[i - 1].getX() && line[i].getY() == line[i - 1].getY())) {
                    allSnappedLines.add(mergedLine);

                }
            }

        }
        // find the closest snapped line
        double minDist = Double.MAX_VALUE;
        Point2D[] minDistLine = null;
        for (int i = 0; i < allSnappedLines.size(); i++) {
            Point2D[] currentLine = allSnappedLines.get(i);
            double dist = new ComponentLine2DDistance(lineDistanceFunction).distance(line, currentLine);
            if (dist < minDist) {
                minDist = dist;
                minDistLine = currentLine;
            }
        }
        return minDistLine;
    }

    /**
     * Finds the four units around the given point.
     * 
     * @param p point to find neighbours for.
     * @return four neighbours.
     */
    public Point2D[] getNeighbouringUnits(Point2D p) {

        // FIXME what about setting all x values to x if x % 1 == 0?
        // FIXME what about not?
        Point2D leftUpper = new Point2D.Double((int) p.getX(), (int) p.getY());
        Point2D rightUpper = new Point2D.Double(leftUpper.getX() + 1, leftUpper.getY());
        Point2D leftLower = new Point2D.Double(leftUpper.getX(), leftUpper.getY() + 1);
        Point2D rightLower = new Point2D.Double(leftUpper.getX() + 1, leftUpper.getY() + 1);

        if (p.getX() % 1 == 0) {
            rightUpper.setLocation(leftUpper.getX(), leftUpper.getY());
            rightLower.setLocation(leftUpper.getX(), leftUpper.getY() + 1);
        }
        if (p.getY() % 1 == 0) {
            leftLower.setLocation(leftUpper.getX(), leftUpper.getY());
            rightLower.setLocation(leftUpper.getX() + 1, leftUpper.getY());
        }
        if (p.getX() % 1 == 0 && p.getY() % 1 == 0) {
            rightLower.setLocation(leftUpper.getX(), leftUpper.getY());
        }
        // the order of the points returned here is the same as in the matlab version
        return new Point2D[] { leftUpper, leftLower, rightUpper, rightLower };
    }
}
