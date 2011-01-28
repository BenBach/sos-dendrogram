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
package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * A Cluster used in KMeans clustering. Has a centroid and a number of indices of a data set assigned to it.
 * 
 * @see KMeans
 * @author Robert Neumayer
 * @version $Id: Cluster.java 3583 2010-05-21 10:07:41Z mayer $
 */

public class Cluster {

    private static final int MAX_DIM_DEBUG = 500;

    private static final int MAX_INDICES_DEBUG = 150;

    private Vector<Integer> indices;

    private double[] centroid;

    private DistanceMetric distanceFunction;

    public Cluster() {
        indices = new Vector<Integer>();
        // defaulting to Euclidean distance.
        distanceFunction = new L2Metric();
    }

    public Cluster(double[] centroid) {
        this();
        this.centroid = centroid;
    }

    public Cluster(double[] centroid, DistanceMetric distanceFunction) {
        this(centroid);
        this.distanceFunction = distanceFunction;
    }

    public Cluster(DistanceMetric distanceFunction) {
        this();
        this.distanceFunction = distanceFunction;
    }

    /**
     * Calculate the centroid of this cluster. This is done by summing up all individual values divided by the number of
     * instances assigned to it.
     * 
     * @param data the data set.
     */
    public void calculateCentroid(double[][] data) {
        for (int instanceIndex = 0; instanceIndex < indices.size(); instanceIndex++) {
            for (int attributeIndex = 0; attributeIndex < data[indices.elementAt(instanceIndex)].length; attributeIndex++) {
                if (instanceIndex == 0) {
                    centroid[attributeIndex] = 0;
                }
                centroid[attributeIndex] += data[indices.elementAt(instanceIndex)][attributeIndex] / indices.size();
            }
        }
    }

    /** Removes the instance according to the given index. */
    public void removeInstanceIndex(int instanceIndex) {
        indices.remove(new Integer(instanceIndex));
    }

    /**
     * Add the index of a data point to this cluster.
     * 
     * @param index to add.
     */
    public void addIndex(int index) {
        indices.add(new Integer(index));
    }

    /**
     * Set the centroid of this cluster.
     * 
     * @param centroid to set.
     */
    public void setCentroid(double[] centroid) {
        this.centroid = centroid;
    }

    // FIXME medidate over this fuck or something
    public double[] getCentroid() {
        return centroid.clone();
    }

    public Vector<Integer> getIndices() {
        return this.indices;
    }

    public int getNumberOfInstances() {
        return indices.size();
    }

    /**
     * Tough one to guess.
     */
    public void printClusterIndices(double[][] data) {
        if (centroid.length > 500) {
            System.out.println("< Surpressing centroid debug output due to high dimensionality (" + centroid.length
                    + ") >");
        } else {
            System.out.println("\tCentroid: " + ArrayUtils.toString(centroid));
        }
        System.out.println("\tSSE: " + SSE(data));
        for (int i = 0; i < indices.size() && i < MAX_INDICES_DEBUG; i++) {
            System.out.println("\tindex " + indices.elementAt(i) + " / "
                    + getDistanceToCentroid(data[indices.elementAt(i)]));
        }
        if (indices.size() > MAX_INDICES_DEBUG) {
            System.out.println("Surpressing output of " + (indices.size() - MAX_INDICES_DEBUG)
                    + " additional elements.");
        }
    }

    /**
     * Tough one to guess.
     */
    public void printClusterIndices() {
        if (centroid.length > MAX_DIM_DEBUG) {
            System.out.println("< Surpressing centroid debug output due to high dimensionality (" + centroid.length
                    + ") >");
        } else {
            System.out.println("\tCentroid: " + ArrayUtils.toString(centroid));
        }
        for (int i = 0; i < indices.size() && i < MAX_INDICES_DEBUG; i++) {
            System.out.println("\tindex " + indices.elementAt(i));
        }
        if (indices.size() > MAX_INDICES_DEBUG) {
            System.out.println("Surpressing output of " + (indices.size() - MAX_INDICES_DEBUG)
                    + " additional elements.");
        }
    }

    /**
     * Returns all the instances belonging to this cluster according to the given data set.
     * 
     * @param data instances.
     * @return plain matrix of all assigned instances.
     */
    public double[][] getInstances(double[][] data) {
        double[][] instances = new double[indices.size()][data[0].length];
        for (int i = 0; i < indices.size(); i++) {
            instances[i] = data[indices.elementAt(i)];
        }
        return instances;
    }

    /**
     * Calculate the sum of the squared error (SSE) for this cluster. This is the distances of the cluster's centroid to
     * all units assigned.
     * 
     * @param data matrix to compute the SSE for.
     * @return the SSE value for this cluster.
     */
    public double SSE(double[][] data) {
        double sse = 0d;
        for (int i = 0; i < indices.size(); i++) {
            try {
                sse += distanceFunction.distance(data[indices.elementAt(i)], centroid);
            } catch (MetricException e) {
                e.printStackTrace();
            }
        }
        return sse;
    }

    /** SSE again, this time the average one (i.e. divided by the number of instances within this cluster) */
    public double averageSSE(double[][] data) {
        return SSE(data) / this.getNumberOfInstances();
    }

    /**
     * Get the distance of a given instance to this cluster's centroid.
     * 
     * @param instance some instance.
     * @return the distance according to the used distance function.
     */
    public double getDistanceToCentroid(double[] instance) {
        try {
            return distanceFunction.distance(centroid, instance);
        } catch (MetricException e) {
            e.printStackTrace();
        }
        return 0d;
    }

    /**
     * Get the numbers of occurrences of each attribute in this cluster.
     * 
     * @return array for each attribute and the number of how many instances it occurs in
     */
    public int[] getNumberOfAttributeOccurrences(double[][] data) {
        int[] counts = new int[data[0].length];
        for (int i = 0; i < indices.size(); i++) {
            double[] row = data[indices.elementAt(i)];
            for (int j = 0; j < row.length; j++) {
                if (i == 0) {
                    counts[j] = 0;
                }
                counts[j] += row[j] > 0d ? 1 : 0;
            }
        }
        for (int i = 0; i < counts.length; i++) {
            counts[i] = counts[i] == 0 ? -1 : counts[i];
        }
        return counts;
    }

    /** Get the instance with the maximum SSE of all instances assigned to this cluster. */
    public int getInstanceIndexWithMaxSSE(double[][] data) {
        int index = -1;
        double maxSSE = Double.NEGATIVE_INFINITY;
        double currentSSE = 0;

        for (int i = 0; i < indices.size(); i++) {
            try {
                currentSSE = distanceFunction.distance(data[indices.elementAt(i)], centroid);

                if (currentSSE > maxSSE) {
                    maxSSE = currentSSE;
                    index = indices.elementAt(i);
                }
            } catch (MetricException e) {
                e.printStackTrace();
            }
        }
        return index;
    }
}