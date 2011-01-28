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

import at.tuwien.ifs.somtoolbox.clustering.Cluster;
import at.tuwien.ifs.somtoolbox.clustering.DistanceFunctionType;
import at.tuwien.ifs.somtoolbox.structures.ComponentLine3D;
import at.tuwien.ifs.somtoolbox.util.Point3d;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

public class ComponentLine3DDistance implements ClusterElementFunctions<ComponentLine3D> {
    private DistanceFunctionType distanceType;

    public ComponentLine3DDistance(DistanceFunctionType distanceType) {
        this.distanceType = distanceType;
    }

    public static ClusterElementFunctions<ComponentLine3D> getEuclidean() {
        return new ComponentLine3DDistance(DistanceFunctionType.Euclidean);
    }

    @Override
    /* Computes the distance between two lines, using the given distance function. */
    public double distance(ComponentLine3D element1, ComponentLine3D element2) {
        return distance(element1.getPoints(), element2.getPoints());
    }

    public double distance(Point3d[] points1, Point3d[] points2) {
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
    public static double euclideanLineDistance(Point3d[] line1, Point3d[] line2) {
        double distance1 = 0;
        double distance2 = 0;
        for (int i = 0; i < line2.length; i++) {
            distance1 += line1[i].distance(line2[i]);
            distance2 += line1[i].distance(line2[line2.length - i - 1]);
            // distance2 = 10000;
        }
        return 1;
        // return distance1;
        // return Math.min(distance1, distance2);
    }

    private static Point3d getNextValidPoint(Point3d[] line, int index) {
        for (int i = index; i < line.length - 1; i++) {
            if (line[i].equals(line[i + 1]) || index == line.length - 1) {
                return line[i];
            }
        }
        return null;
    }

    private static Point3d getPreviousValidPoint(Point3d[] line, int index) {
        for (int i = index; i >= 0; i--) {
            if (index == 0 || line[i].equals(line[i - 1])) {
                return line[i];
            }
        }
        return null;
    }

    public static double euclideanLineDistanceDifferentNumberOfStops(Point3d[] line1, Point3d[] line2) {
        double distance1 = 0;
        double distance2 = 0;
        for (int i = 0; i < line2.length; i++) {
            final int endIndex = line2.length - i - 1;
            distance1 += Math.min(line1[i].distance(getPreviousValidPoint(line2, i)),
                    line1[i].distance(getNextValidPoint(line2, i)));
            distance2 += Math.min(line2[endIndex].distance(getPreviousValidPoint(line1, endIndex)),
                    line2[endIndex].distance(getNextValidPoint(line1, endIndex)));

        }
        return Math.min(distance1, distance2);
    }

    /** Computes the distance between two lines in terms of distances between single segments. */
    public static double minimumEuclideanLineDistance(Point3d[] line1, Point3d[] line2) {
        double distance1 = 0;
        double distance2 = 0;
        for (int i = 0; i < line2.length; i++) {
            double minDistance1 = Double.MAX_VALUE;
            double minDistance2 = Double.MAX_VALUE;
            for (Point3d element : line2) {
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
    public static double editLineDistance(Point3d[] line1, Point3d[] line2) {
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

    /**
     * Computes if two lines are parallel to each other, by checking the cross product of their direction vectors.
     **/
    public static boolean linesParallel(Point3d line1Begin, Point3d line1End, Point3d line2Begin, Point3d line2End) {
        // get direction vectors
        // get cross product
        // return true if cross product == 0 0 0
        Point3d direction1 = new Point3d(line1Begin.x - line1End.x, line1Begin.y - line1End.y, line1Begin.z
                - line1End.z);
        Point3d direction2 = new Point3d(line2Begin.x - line2End.x, line2Begin.y - line2End.y, line2Begin.z
                - line2End.z);
        Point3d cp = VectorTools.crossProduct(direction1, direction2);
        if (cp.x == 0 && cp.y == 0 && cp.z == 0) {
            return true;
        } else {
            return false;
        }
    }

    /** Computes the distance between two lines by computing the area stretching between them. */
    public static double areaLineDistance(Point3d[] line1, Point3d[] line2) {
        // FIXME this can't be it
        double dist = 0;
        return dist;
    }

    @Override
    public ComponentLine3D meanObject(Cluster<? extends ComponentLine3D> elements) {
        if (elements.size() == 1) {
            return elements.get(0);
        }
        Point3d[] mean = new Point3d[elements.get(0).getLength()];
        for (int i = 0; i < mean.length; i++) {
            double x = 0;
            double y = 0;
            double z = 0;
            for (int j = 0; j < elements.size(); j++) {
                Point3d[] array = elements.get(j).getPoints();
                x += array[i].x;
                y += array[i].y;
                z += array[i].z;
            }
            x = x / elements.size();
            y = y / elements.size();
            z = z / elements.size();
            mean[i] = new Point3d(x, y, z);
        }
        return new ComponentLine3D(mean);
    }

    public int getIndexOfLineClosestToMean(Cluster<? extends ComponentLine3D> elements) {
        double minDist = Double.POSITIVE_INFINITY;
        int minIndex = 0;
        ComponentLine3D meanObject = meanObject(elements);
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
    public String toString(Cluster<? extends ComponentLine3D> elements) {
        StringBuilder sb = new StringBuilder();
        for (Point3d p : meanObject(elements).getPoints()) {
            if (sb.length() > 0) {
                sb.append(" => ");
            }
            sb.append(DF.format(p.x)).append(" / ").append(DF.format(p.y)).append(" / ").append(DF.format(p.z));
        }
        return getClass().getSimpleName() + ", lines: " + elements.size() + ", mean line: " + sb;
    }
}
