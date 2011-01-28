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

import cern.colt.function.DoubleDoubleFunction;

/**
 * Implements the L2 or Euclidean metric. Though this class could us at.tuwien.ifs.somtoolbox.layers.metrics.LNMetric,
 * for performance issues this less complex computation should be used.
 * 
 * @author Michael Dittenbach
 * @version $Id: L2Metric.java 3883 2010-11-02 17:13:23Z frank $
 */
public class L2Metric extends AbstractMetric implements DistanceMetric {

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[])
     */
    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        checkDimensions(vector1, vector2);
        double dist = 0;
        for (int i = 0; i < vector1.length; i++) {
            dist += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
        }
        return Math.sqrt(dist);
    }

    /**
     * @return the norm of the vector.
     */
    public double norm(double[] vector) {
        double dist = 0;
        int dim = vector.length;
        for (int ve = 0; ve < dim; ve++) {
            dist += vector[ve] * vector[ve];
        }
        return Math.sqrt(dist);
    }

    // this implementation is seriously slower than distance(double[], double[]), and has therefore been commented out
    //
    // public double distance(DoubleMatrix1D vector1, DoubleMatrix1D vector2) throws MetricException {
    // return Math.sqrt(vector1.assign(vector2, Functions.minus).aggregate(Functions.plus, Functions.pow(2)));
    // }

    @Override
    public String toString() {
        return "L2";
    }

    class L2MetricMatrix implements DoubleDoubleFunction {
        @Override
        public double apply(double arg0, double arg1) {
            return (arg0 - arg1) * (arg0 - arg1);
        }

        public double apply(double arg0) {
            return 0;
        }
    }

    public static void main(String[] args) {
        performanceTest(new L2Metric(), 1500000);
    }

}
