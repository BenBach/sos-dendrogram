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
package at.tuwien.ifs.somtoolbox.clustering.functions;

import java.awt.geom.Point2D;

import at.tuwien.ifs.somtoolbox.clustering.Cluster;
import at.tuwien.ifs.somtoolbox.clustering.DistanceFunctionType;
import at.tuwien.ifs.somtoolbox.structures.ComponentLine2D;

public class ComponentLine2DDistance implements ClusterElementFunctions<ComponentLine2D> {
    private DistanceFunctionType distanceType;

    public ComponentLine2DDistance(DistanceFunctionType distanceType) {
        this.distanceType = distanceType;
    }

    public static ClusterElementFunctions<ComponentLine2D> getEuclidean() {
        return new ComponentLine2DDistance(DistanceFunctionType.Euclidean);
    }

    @Override
    /* Computes the distance between two lines, using the given distance function. */
    public double distance(ComponentLine2D line1, ComponentLine2D line2) {
        return distance(line1.getPoints(), line2.getPoints());
    }

    public double distance(Point2D[] points1, Point2D[] points2) {
        switch (distanceType) {
            case Euclidean:
                return euclideanLineDistance(points1, points2);
            case MinEuclidean:
                return minimumEuclideanLineDistance(points1, points2);
            case Edit:
                return editLineDistance(points1, points2);
            case Area:
                return areaLineDistance(points1, points2);
            case EuclideanDiffNrOfStops:
                return euclideanLineDistanceDifferentNumberOfStops(points1, points2);
            default:
                return -1; // TODO: maybe throw an exception?
        }
    }

    /** Computes the distance between two lines in terms of absolute distances between single segments. */
    public static double euclideanLineDistance(Point2D[] line1, Point2D[] line2) {
        double distance1 = 0;
        double distance2 = 0;
        for (int i = 0; i < line2.length; i++) {
            distance1 += line1[i].distance(line2[i]);
            distance2 += line1[i].distance(line2[line2.length - i - 1]);
        }
        return Math.min(distance1, distance2);
    }

    private static Point2D getNextValidPoint(Point2D[] line, int index) {
        for (int i = index; i < line.length; i++) {
            if (i == line.length - 1 || line[i].equals(line[i + 1])) {
                return line[i];
            }
        }
        return null;
    }

    private static Point2D getPreviousValidPoint(Point2D[] line, int index) {
        for (int i = index; i >= 0; i--) {
            if (i == 0 || line[i].equals(line[i - 1])) {
                return line[i];
            }
        }
        return null;
    }

    public static double euclideanLineDistanceDifferentNumberOfStops(Point2D[] line1, Point2D[] line2) {
        double distance1 = 0;
        double distance2 = 0;
        for (int i = 0; i < line2.length; i++) {
            final int endIndex = line2.length - i - 1;
            Point2D previousValidPoint1 = getPreviousValidPoint(line2, i);
            Point2D nextValidPoint1 = getNextValidPoint(line2, i);
            System.out.println("nvp " + nextValidPoint1);
            distance1 += Math.min(line1[i].distance(previousValidPoint1), line1[i].distance(nextValidPoint1));
            Point2D previousValidPoint = getPreviousValidPoint(line1, endIndex);
            Point2D nextValidPoint = getNextValidPoint(line1, endIndex);
            distance2 += Math.min(line2[endIndex].distance(previousValidPoint),
                    line2[endIndex].distance(nextValidPoint));

        }
        return Math.min(distance1, distance2);
    }

    /** Computes the distance between two lines in terms of distances between single segments. */
    public static double minimumEuclideanLineDistance(Point2D[] line1, Point2D[] line2) {
        double distance1 = 0;
        double distance2 = 0;
        for (int i = 0; i < line2.length; i++) {
            double minDistance1 = Double.MAX_VALUE;
            double minDistance2 = Double.MAX_VALUE;
            for (Point2D element : line2) {
                double tempDistance1 = line1[i].distance(line2[i]);
                if (tempDistance1 < minDistance1) {
                    minDistance1 = tempDistance1;
                }
                double tempDistance2 = line1[i].distance(line2[line2.length - i - 1]);
                if (tempDistance2 < minDistance2) {
                    minDistance2 = tempDistance2;
                }
            }
            distance1 += minDistance1;
            distance2 += minDistance2;
        }
        return Math.min(distance1, distance2);
    }

    /**
     * Computes the distance between two lines in terms of edit operations necessary to move one line onto the other.
     * This basically computes distances for beginning and end points of single lines and penalises non parallel lines .
     */
    public static double editLineDistance(Point2D[] line1, Point2D[] line2) {
        double dist = 0;
        double parallelFactor = .5;
        for (int i = 1; i < line2.length - 1; i++) {
            double dn = line1[i].distance(line2[i]) + line1[i + 1].distance(line2[i + 1]);
            double dr = line1[i].distance(line2[i + 1]) + line1[i + 1].distance(line2[i]);

            if (linesParallel(line1[i], line1[i + 1], line2[i], line2[i + 1])) {
                dn = dn * parallelFactor;
                dr = dr * parallelFactor;
            }
            dist += dn + dr;
        }
        return dist;
    }

    /** Computes if two lines are parallel to each other, by comparing their slopes. */
    public static boolean linesParallel(Point2D line1Begin, Point2D line1End, Point2D line2Begin, Point2D line2End) {
        return curveSlope(line1Begin, line1End) == curveSlope(line2Begin, line2End)
                || curveSlope(line1Begin, line1End) == curveSlope(line2End, line2Begin);
    }

    /** Computes the slope of a line/curve between the given points. */
    public static double curveSlope(Point2D start, Point2D end) {
        return (end.getX() - start.getX()) / (end.getY() - start.getY());
    }

    /** Computes the distance between two lines by computing the area stretching between them. */
    public static double areaLineDistance(Point2D[] line1, Point2D[] line2) {
        // FIXME this can't be it
        double dist = 0;
        return dist;
    }

    @Override
    public ComponentLine2D meanObject(Cluster<? extends ComponentLine2D> elements) {
        if (elements.size() == 1) {
            return elements.get(0);
        }
        Point2D[] mean = new Point2D[elements.get(0).getLength()];
        for (int i = 0; i < mean.length; i++) {
            double x = 0;
            double y = 0;
            for (int j = 0; j < elements.size(); j++) {
                ComponentLine2D array = elements.get(j);
                x += array.get(i).getX();
                y += array.get(i).getY();
            }
            x = x / elements.size();
            y = y / elements.size();
            mean[i] = new Point2D.Double(x, y);
        }
        return new ComponentLine2D(mean);
    }

    public int getIndexOfLineClosestToMean(Cluster<? extends ComponentLine2D> elements) {
        double minDist = Double.POSITIVE_INFINITY;
        int minIndex = 0;
        ComponentLine2D meanObject = meanObject(elements);
        for (int k = 0; k < elements.size(); k++) {
            double distance = distance(meanObject, elements.get(k));
            if (distance <= minDist) {
                minDist = distance;
                minIndex = k;
            }
        }
        return minIndex;
    }

    @Override
    public String toString(Cluster<? extends ComponentLine2D> elements) {
        StringBuilder sb = new StringBuilder();
        for (Point2D p : meanObject(elements).getPoints()) {
            if (sb.length() > 0) {
                sb.append(" => ");
            }
            sb.append(DF.format(p.getX())).append(" / ").append(DF.format(p.getY()));
        }
        return getClass().getSimpleName() + ", lines: " + elements.size() + ", mean line: " + sb;
    }
}
