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
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.structures.DoubleVector2D;

/**
 * Implements functions needed for clustering of double arrays.
 * 
 * @author Rudolf Mayer
 * @version $Id: DoubleVector2DDistance.java 3927 2010-11-09 12:04:54Z mayer $
 */
public class DoubleVector2DDistance implements ClusterElementFunctions<DoubleVector2D> {
    protected DistanceMetric metric;

    public DoubleVector2DDistance(DistanceMetric metric) {
        this.metric = metric;
    }

    @Override
    /* Computes the distance between two lines, using the given distance function. */
    public double distance(DoubleVector2D element1, DoubleVector2D element2) {
        return distance(element1.getPoints(), element2.getPoints());
    }

    public double distance(double[] vector1, double[] vector2) {
        try {
            return metric.distance(vector1, vector2);
        } catch (MetricException e) {
            return 0; // doesn't happen
        }
    }

    @Override
    public DoubleVector2D meanObject(Cluster<? extends DoubleVector2D> elements) {
        if (elements.size() == 1) {
            return elements.get(0);
        }
        double[] meanVector = new double[elements.get(0).getLength()];
        for (int i = 0; i < meanVector.length; i++) {
            double sum = 0;
            for (int j = 0; j < elements.size(); j++) {
                sum += elements.get(j).get(i);
            }
            meanVector[i] = sum / elements.size();
        }
        return new DoubleVector2D(meanVector);
    }

    public int getIndexOfLineClosestToMean(Cluster<? extends DoubleVector2D> elements) {
        double minDist = Double.POSITIVE_INFINITY;
        int minIndex = 0;
        DoubleVector2D meanObject = meanObject(elements);
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
    public String toString(Cluster<? extends DoubleVector2D> elements) {
        StringBuilder sb = new StringBuilder();
        for (double p : meanObject(elements).getPoints()) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(DF.format(p));
        }
        return getClass().getSimpleName() + " # vectors: " + elements.size() + ", mean vector: " + sb;
    }
}
