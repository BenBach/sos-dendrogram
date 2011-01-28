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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomGenerator;

import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * Pretty much the classic K-Means clustering. Tried to keep it simple, though.
 * 
 * @author Robert Neumayer
 * @version $Id: KMeans.java 3921 2010-11-05 12:54:53Z mayer $
 */

public class KMeans {
    public enum InitType {
        RANDOM, RANDOM_INSTANCE, LINEAR, LINEAR_INSTANCE, EQUAL_NUMBERS
    }

    protected double[][] data;

    private int k, numberOfInstances, numberOfAttributes;

    private double[] minValues, maxValues, differences;

    // this one will be used for looking up cluster assignments
    // which in turn will help to terminate the training process
    // as soon as we don't experience any changes in cluster assignments
    private Hashtable<Integer, Integer> instancesInClusters;

    protected Cluster[] clusters;

    // TODO remove this after testing
    private static long RANDOM_SEED = 1234567;

    /**
     * Default constructor (as much defaulting as possible). Uses linear initialisation and Euclidean distance.
     * 
     * @param k number of clusters
     * @param data guess
     */
    public KMeans(int k, double[][] data) {
        this(k, data, InitType.RANDOM, new L2Metric());
    }

    /**
     * Instantiate a new KMeans object with:
     * 
     * @param k number of clusters
     * @param data the data to cluster
     * @param initialisation the initialisation method used (to be chosen from InitType)
     */
    public KMeans(int k, double[][] data, InitType initialisation) {
        this(k, data, initialisation, new L2Metric());
    }

    /**
     * Construct a new K-Means bugger.
     * 
     * @param k number of clusters
     * @param data the data set
     * @param initialisation initialisation type
     * @param distanceFunction an LnMetric of your choice
     */
    public KMeans(int k, double[][] data, InitType initialisation, DistanceMetric distanceFunction) {
        this.k = k;
        this.numberOfAttributes = data[0].length;
        this.numberOfInstances = data.length;
        this.instancesInClusters = new Hashtable<Integer, Integer>();
        this.clusters = new Cluster[k];
        this.data = data;

        // initialise a couple of things
        initMinAndMaxValues();

        switch (initialisation) {
            case LINEAR:
                initClustersLinearly(distanceFunction);
                break;
            case LINEAR_INSTANCE:
                initClustersLinearlyOnInstances(distanceFunction);
                break;
            case RANDOM:
                initClustersRandomly(distanceFunction);
                break;
            case RANDOM_INSTANCE:
                initClustersRandomlyOnInstances(distanceFunction);
                break;
            case EQUAL_NUMBERS:
                initClustersEqualNumbers(distanceFunction);
                break;
            default:
                break;
        }
        // printClusters();
        // this one is to do a first assignment of data points to clusters
        trainingStep();
    }

    /**
     * Train for a certain number of steps. Note that we won't stop until all training steps are finished.
     * 
     * @param numberOfSteps how many would you like?
     */
    public void train(int numberOfSteps) {
        for (int stepIndex = 0; stepIndex < numberOfSteps; stepIndex++) {
            System.out.println("step: " + stepIndex + " / " + trainingStep());
        }
    }

    /**
     * Train for as long as instances move between clusters. "Not moving" means that there hasn't been a change in the
     * last {@link #NUMBER_OF_UPDATE_RANGE} steps ({@value #NUMBER_OF_UPDATE_RANGE}).
     */
    public void train() {
        ArrayList<Boolean> lastUpdates = new ArrayList<Boolean>(NUMBER_OF_UPDATE_RANGE);
        boolean hasUpdatedInLastKRounds = false;
        do {
            boolean thisUpdate = trainingStep();
            hasUpdatedInLastKRounds = false;
            // check if there was any update in the last steps
            lastUpdates.add(0, thisUpdate);
            for (int i = 0; i < NUMBER_OF_UPDATE_RANGE && i < lastUpdates.size(); i++) {
                if (lastUpdates.get(i)) {
                    hasUpdatedInLastKRounds = true;
                    break;
                }
            }
        } while (hasUpdatedInLastKRounds);
        removeEmptyClusters();
    }

    // TODO implement a better stop criterion (e.g. less than 10 per cent of instances move between clusters per step)

    /**
     * Searches for clusters which have no instances assigned. These are then replaced FIXME FIXME private void
     * substituteEmptyClusters() { System.out.println("Removing empty clusters:"); double[] replacementCentroid = new
     * double[clusters[0].getCentroid().length]; for (int i = 0; i < clusters.length; i++) { if
     * (clusters[i].getNumberOfInstances() != 0) replacementCentroid = clusters[i].getCentroid().clone(); } for (int i =
     * 0; i < clusters.length; i++) { if (clusters[i].getNumberOfInstances() == 0)
     * clusters[i].setCentroid(replacementCentroid); } }
     */

    private void removeEmptyClusters() {
        Vector<Cluster> nonEmptyClusters = new Vector<Cluster>();
        for (Cluster cluster : clusters) {
            if (cluster.getNumberOfInstances() != 0) {
                nonEmptyClusters.add(cluster);
            }
        }
        Cluster[] remainingClusters = new Cluster[nonEmptyClusters.size()];
        for (int i = 0; i < nonEmptyClusters.size(); i++) {
            remainingClusters[i] = nonEmptyClusters.elementAt(i);
        }
        clusters = remainingClusters;
    }

    private int lastNumberOfUpdates = Integer.MAX_VALUE;

    private static final int NUMBER_OF_UPDATE_RANGE = 5; // we check that many steps for any updates

    /**
     * A classic training step in the K-Means world.
     * 
     * @return whether this step brought any changes or not. Note, this one also says no if there were as many changes
     *         as in the last step.
     */
    private boolean trainingStep() {
        boolean didUpdate = false;
        int numberOfUpdates = 0;
        for (int instanceIndex = 0; instanceIndex < numberOfInstances; instanceIndex++) {
            // find closest centroid
            int indexOfClosestCluster = 0;

            indexOfClosestCluster = getIndexOfClosestCluster(data[instanceIndex]);
            // if there's no assignment stored in our lookup table, we add all
            // instances to their according clusters (the one with the closest centroids that is)
            // so this happens in the first run only
            if (instancesInClusters.get(new Integer(instanceIndex)) == null) {
                clusters[indexOfClosestCluster].addIndex(instanceIndex);
                didUpdate = true;
                numberOfUpdates++;
            }
            // now we have them in the lookup table but the closest cluster changed from the last run
            // so we remove the instance from the cluster and assign it to a new one
            else if (!instancesInClusters.get(new Integer(instanceIndex)).equals(new Integer(indexOfClosestCluster))) {
                clusters[instancesInClusters.get(new Integer(instanceIndex))].removeInstanceIndex(instanceIndex);
                clusters[indexOfClosestCluster].addIndex(instanceIndex);
                didUpdate = true;
                numberOfUpdates++;
            }
            // we always update the lookup table
            instancesInClusters.put(instanceIndex, indexOfClosestCluster);
        }
        // calculate new centroids

        System.out.println("SSE: " + this.getSSE());
        System.out.println("performed: " + numberOfUpdates + " updates.");
        calculateNewCentroids();
        this.printCentroidsShort();
        if (numberOfUpdates == lastNumberOfUpdates) {
            return false;
        }
        lastNumberOfUpdates = numberOfUpdates;

        return didUpdate;
    }

    /**
     * Batch calculation of all cluster centroids.
     */
    private void calculateNewCentroids() {
        for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
            // go look for a new centroid if no instances are assigned to the cluster
            if (clusters[clusterIndex].getNumberOfInstances() == 0) {
                if (getSubstituteCentroid() != null) {
                    clusters[clusterIndex].setCentroid(getSubstituteCentroid());
                }
            }
            clusters[clusterIndex].calculateCentroid(data);
        }
    }

    /**
     * Get a new centroid for empty clusters. We therefore take the instance with the largest SSE to the cluster
     * centroid having the largest SSE. Get the idea? Read slowly.
     * 
     * @return a new centroid (rather: a clone thereof :))
     */
    private double[] getSubstituteCentroid() {
        double maxSSE = Double.NEGATIVE_INFINITY;
        int maxSSEIndex = -1;
        for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
            clusters[clusterIndex].calculateCentroid(data);
            double currentSSE = clusters[clusterIndex].SSE(data);
            if (currentSSE > maxSSE) {
                maxSSE = currentSSE;
                maxSSEIndex = clusterIndex;
            }
        }
        // System.out.println(maxSSEIndex);
        // System.out.println(clusters.length);
        // System.out.println(clusters[maxSSEIndex].getInstanceIndexWithMaxSSE(data));
        // FIXME is this the right way of handling this (if the max sse exists in a cluster that has no instances
        // assigned)
        if (clusters[maxSSEIndex].getInstanceIndexWithMaxSSE(data) == -1) {
            return null;
        }
        return data[clusters[maxSSEIndex].getInstanceIndexWithMaxSSE(data)].clone();
    }

    /**
     * Get the index of the closest cluster for the given instance index. Note that in case of equally distant clusters
     * we assign the first found cluster. At the end of the day this means that the clusters with lower indices will
     * have a tendency to be larger. It hopefully won't have too much impact, possibly a random assignment in case of
     * equal weights would make sense, however, this would require a couple of steps more in here.
     * 
     * @param instance the data vector to be assigned
     * @return index of the closest cluster centre
     */
    private int getIndexOfClosestCluster(double[] instance) {
        int indexOfClosestCluster = 0;
        double smallestDistance = Double.POSITIVE_INFINITY;
        double currentDistance = 0;
        for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
            currentDistance = clusters[clusterIndex].getDistanceToCentroid(instance);
            if (currentDistance < smallestDistance) {
                smallestDistance = currentDistance;
                indexOfClosestCluster = clusterIndex;
            }
        }
        return indexOfClosestCluster;
    }

    /**
     * Get a set of labels for the given clustering based on the occurrences of attributes within clusters, i.e. there's
     * a preference for labels occurring in many instance.
     * 
     * @return labels
     */
    public int[][] getOccurrenceLabels(int numberOfLabels) {
        int[][] labelIndices = new int[clusters.length][numberOfLabels];
        for (int i = 0; i < getClusters().length; i++) {
            Cluster c = getClusters()[i];
            int[] occ = c.getNumberOfAttributeOccurrences(data);
            int[] maxIndices = VectorTools.getIndicesOfMaxValues(occ, numberOfLabels);
            labelIndices[i] = maxIndices;
        }
        return labelIndices;
    }

    /**
     * Calculate random centroids for each cluster.
     */
    private void initClustersRandomly(DistanceMetric distanceFunction) {
        RandomGenerator rg = new JDKRandomGenerator();
        // FIXME: this is for testing purposes only
        rg.setSeed(RANDOM_SEED);
        // for each cluster
        for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
            // for each of its attributes
            double[] centroid = new double[numberOfAttributes];
            for (int attributeIndex = 0; attributeIndex < numberOfAttributes; attributeIndex++) {
                centroid[attributeIndex] = differences[attributeIndex] * rg.nextDouble() + minValues[attributeIndex];
            }
            clusters[clusterIndex] = new Cluster(centroid, distanceFunction);
        }
        System.out.println("initial centroids: ");
        // printCentroids();
    }

    /**
     * cluster centres are initialised by equally sized random chunks of the input data when there's 150 instances, we
     * assign 50 chosen randomly to each cluster and calculate its centre from these (the last cluster might be larger
     * if numInstances mod k < 0)
     */
    private void initClustersEqualNumbers(DistanceMetric distanceFunction) {
        HashSet<Integer> usedIndices = new HashSet<Integer>();
        int limit = numberOfInstances / k;
        // FIXME: Test clustering with new permutation generator!
        // int[] randPermIndices = RandomTools.permutation(new Random(RANDOM_SEED), this.numberOfInstances);
        JDKRandomGenerator rg = new JDKRandomGenerator();
        rg.setSeed(RANDOM_SEED);
        int[] randPermIndices = new RandomDataImpl(rg).nextPermutation(this.numberOfInstances, this.numberOfInstances);
        for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
            Cluster c = new Cluster(new double[data[0].length]);
            // System.out.println("cluster: " + clusterIndex);
            for (int randPermIndice : randPermIndices) {
                int currentIndex = randPermIndice;
                if ((c.getNumberOfInstances() < limit || clusterIndex == k - 1) && !usedIndices.contains(currentIndex)) {
                    c.addIndex(currentIndex);
                    usedIndices.add(currentIndex);
                    // System.out.print(" " + currentIndex);
                }
            }
            // System.out.println();
            c.calculateCentroid(data);
            // clusters[clusterIndex] = c;
            clusters[clusterIndex] = new Cluster(c.getCentroid(), distanceFunction);
            // System.out.println("setting cluster: " + clusterIndex + " / " + c.getNumberOfInstances());
        }
    }

    /** Take random points from the input data as centroids. */
    private void initClustersRandomlyOnInstances(DistanceMetric distanceFunction) {
        ArrayList<double[]> usedInstances = new ArrayList<double[]>();
        RandomGenerator rg = new JDKRandomGenerator();
        // FIXME: this is for testing purposes only
        rg.setSeed(RANDOM_SEED);
        // for each cluster
        for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
            // draw a random input
            double[] centroid = data[rg.nextInt(data.length - 1)].clone();
            while (usedInstances.contains(centroid)) {
                centroid = data[rg.nextInt(data.length - 1)].clone();
            }
            usedInstances.add(centroid);
            clusters[clusterIndex] = new Cluster(centroid, distanceFunction);
        }
    }

    /**
     * This one does linear initialisation. In the two dimensional space it will place the cluster centres on a diagonal
     * line of a square.
     */
    private void initClustersLinearly(DistanceMetric distanceFunction) {
        for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
            double[] centroid = new double[numberOfAttributes];
            for (int attributeIndex = 0; attributeIndex < numberOfAttributes; attributeIndex++) {
                centroid[attributeIndex] = (maxValues[attributeIndex] - minValues[attributeIndex])
                        / (clusters.length + 1) * (clusterIndex + 1) + minValues[attributeIndex];
            }
            clusters[clusterIndex] = new Cluster(centroid, distanceFunction);
        }
    }

    /**
     * like {@link #initClustersLinearly(DistanceMetric)}, but after computing the exact linear point, rather finds &
     * uses the closest instance from the data set as centroid.
     */
    private void initClustersLinearlyOnInstances(DistanceMetric distanceFunction) {
        ArrayList<double[]> usedInstances = new ArrayList<double[]>(); // to store which points are already taken
        for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
            double[] centroid = new double[numberOfAttributes];
            for (int attributeIndex = 0; attributeIndex < numberOfAttributes; attributeIndex++) {
                centroid[attributeIndex] = (maxValues[attributeIndex] - minValues[attributeIndex])
                        / (clusters.length + 1) * (clusterIndex + 1) + minValues[attributeIndex];
                // now find the closest real instance to this point
                double minDistance = Double.MAX_VALUE;
                double[] minData = null;
                try {
                    for (int i = 0; i < data.length; i++) {
                        if (distanceFunction.distance(centroid, data[i]) < minDistance
                                && !usedInstances.contains(data[i]) || i == data.length - 1) {
                            minData = data[i];
                            minDistance = distanceFunction.distance(centroid, data[i]);
                        }
                    }
                    usedInstances.add(minData);
                    centroid = minData.clone();
                } catch (MetricException e) {
                    e.printStackTrace();
                }
            }
            clusters[clusterIndex] = new Cluster(centroid, distanceFunction);
        }
    }

    /*
     * TODO implement initialisation by the most frequent points (i.e. where people stand around in SL) private void initClustersFrequency() {
     * clusters[0] = new Cluster(new double[] { 73, 183, 43 }); clusters[1] = new Cluster(new double[] { 144, 10, 33 }); clusters[2] = new Cluster(new
     * double[] { 118, 156, 62 }); clusters[3] = new Cluster(new double[] { 188, 64, 33 }); clusters[4] = new Cluster(new double[] { 188, 63, 33 });
     * clusters[5] = new Cluster(new double[] { 118, 156, 33 }); clusters[6] = new Cluster(new double[] { 141, 53, 42 }); clusters[7] = new
     * Cluster(new double[] { 117, 153, 62 }); }
     */

    /**
     * Initialise the cluster centres with the given centres.
     * 
     * @param centroids centroids for clusters.
     * @throws MoreCentresThanKException don't dare to set more or less centres than our k value.
     */
    public void setClusterCentroids(double[][] centroids) throws MoreCentresThanKException {
        if (centroids.length != k) {
            throw new MoreCentresThanKException("So, someone was trying to set " + centroids.length + " centres for "
                    + k + " clusters. Hint: possibly transpose the matrix and try again.");
        }
        for (int i = 0; i < clusters.length; i++) {
            clusters[i].setCentroid(centroids[i]);
        }
        // to do initial assignment of instances to the new centroids
        trainingStep();
    }

    /**
     * Utility method to get the min, max, and diff values of the data set. This is used for scaling the (random) values
     * in the initialisation functions.
     */
    private void initMinAndMaxValues() {
        minValues = new double[numberOfAttributes];
        maxValues = new double[numberOfAttributes];
        differences = new double[numberOfAttributes];
        // for each attribute
        for (int j = 0; j < numberOfAttributes; j++) {
            // in each instance (i.e. each single value now :-))
            minValues[j] = Double.MAX_VALUE;
            maxValues[j] = Double.MIN_VALUE;
            for (double[] element : data) {
                if (element[j] < minValues[j]) {
                    minValues[j] = element[j];
                }
                if (element[j] > maxValues[j]) {
                    maxValues[j] = element[j];
                }
            }
            differences[j] = maxValues[j] - minValues[j];
        }
    }

    /**
     * Get a double[][] of all cluster centroids.
     * 
     * @return all cluster centroids
     */
    public double[][] getClusterCentroids() {
        double[][] centroids = new double[k][numberOfAttributes];
        for (int indexClusters = 0; indexClusters < clusters.length; indexClusters++) {
            centroids[indexClusters] = clusters[indexClusters].getCentroid();
        }
        return centroids;
    }

    public double[][] getClusterVariances() {
        double[][] variances = new double[clusters.length][numberOfAttributes];
        for (int indexClusters = 0; indexClusters < clusters.length; indexClusters++) {
            double[][] instances = clusters[indexClusters].getInstances(data);
            // for all attributes in this cluster
            for (int i = 0; i < numberOfAttributes; i++) {
                double n = 0;
                double mean = 0;
                double m2 = 0;
                double delta = 0;
                for (double[] instance : instances) {
                    n++;
                    double value = instance[i];
                    delta = value - mean;
                    mean += delta / n;
                    m2 += delta * (value - mean);

                }
                variances[indexClusters][i] = m2 / n;
            }
        }
        for (double[] vars : variances) {
            System.out.println(ArrayUtils.toString(vars));
        }
        return variances;
    }

    /**
     * Get a double[][] of all cluster centroids. Normalised in the range of the original data.
     * 
     * @return all cluster centroids
     */
    public double[][] getMinMaxNormalisedClusterCentroids() {
        double[][] normalisedCentroids = new double[k][numberOfAttributes];
        for (int indexClusters = 0; indexClusters < k; indexClusters++) {
            double[] normalisedCentroid = clusters[indexClusters].getCentroid();
            for (int i = 0; i < normalisedCentroid.length; i++) {
                normalisedCentroid[i] = (normalisedCentroid[i] - minValues[i]) / (maxValues[i] - minValues[i]);
            }
            normalisedCentroids[indexClusters] = normalisedCentroid;
        }
        return normalisedCentroids;
    }

    /**
     * Get a double[][] of all cluster centroids. Normalised in the range of the centroids.
     * 
     * @return all cluster centroids
     */
    public double[][] getMinMaxNormalisedClusterCentroidsWithin() {
        double[] min = new double[data.clone()[0].length];
        double[] max = new double[data.clone()[0].length];
        // min[] = Double.MAX_VALUE;
        // double max[] = Double.MIN_VALUE;

        for (int i = 0; i < data[0].length; i++) {
            for (Cluster cluster : clusters) {
                if (i == 0) {
                    min[i] = Double.MAX_VALUE;
                    max[i] = Double.MIN_VALUE;
                }
                if (cluster.getCentroid()[i] > max[i]) {
                    max[i] = cluster.getCentroid()[i];
                }
                if (cluster.getCentroid()[i] < min[i]) {
                    min[i] = cluster.getCentroid()[i];
                }
            }
        }
        double[][] centroids = new double[k][numberOfAttributes];
        for (int indexClusters = 0; indexClusters < k; indexClusters++) {
            double[] centroid = clusters[indexClusters].getCentroid();
            for (int i = 0; i < centroid.length; i++) {
                centroid[i] = (centroid[i] - minValues[i]) / maxValues[i];
            }
            centroids[indexClusters] = centroid;
        }
        return centroids;
    }

    public double[] getMinValues() {
        return minValues;
    }

    public double[] getMaxValues() {
        return maxValues;
    }

    public double[] getDifferences() {
        return differences;
    }

    public Cluster[] getClusters() {
        return clusters;
    }

    /**
     * Get the sum of the squared error for all clusters.
     * 
     * @return SSE.
     */
    public double getSSE() {
        double sse = 0d;
        for (Cluster cluster : clusters) {
            sse += cluster.SSE(data);
        }
        return sse;
    }

    /**
     * Get the sum of the squared error for single clusters.
     * 
     * @return several SSEs.
     */
    public double[] getSSEs() {
        double[] sse = new double[k];
        for (int i = 0; i < clusters.length; i++) {
            sse[i] = clusters[i].SSE(data);
        }
        return sse;
    }

    public void printCentroids() {
        for (int i = 0; i < clusters.length; i++) {
            System.out.println(i + " / " + clusters[i].SSE(data) + " / " + clusters[i].getNumberOfInstances() + " / "
                    + ArrayUtils.toString(clusters[i].getCentroid()));
        }
    }

    public void printCentroidsShort() {
        for (int i = 0; i < clusters.length; i++) {
            System.out.println("\t" + i + " / " + clusters[i].SSE(data) + " / " + clusters[i].getNumberOfInstances());
        }
    }

    public void printClusterIndices() {
        for (int i = 0; i < clusters.length; i++) {
            System.out.println("Cluster: " + i);
            clusters[i].printClusterIndices(data);
        }
    }

    /**
     * @return Returns the data.
     */
    public double[][] getData() {
        return data;
    }
}
