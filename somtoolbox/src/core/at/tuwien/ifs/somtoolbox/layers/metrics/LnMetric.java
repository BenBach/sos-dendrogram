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
import cern.colt.matrix.DoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * Generic Ln metric. L1 and L2 metrics are still implemented in seperate classes for performance reasons.
 * 
 * @author Rudolf Mayer
 * @version $Id: LnMetric.java 3883 2010-11-02 17:13:23Z frank $
 */
public class LnMetric extends AbstractMetric implements DistanceMetric {

    private final LnMetricMatrix LN_METRIC_MATRIX = new LnMetricMatrix();

    private double n;

    private double root;

    public LnMetric(double power) {
        this.n = power;
        root = 1 / n;
    }

    /** @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[]) */
    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        checkDimensions(vector1, vector2);
        double dist = 0;
        for (int i = 0; i < vector1.length; i++) {
            dist += Math.pow(Math.abs(vector1[i] - vector2[i]), n);
        }
        return Math.pow(dist, 1 / n);
    }

    @Override
    public String toString() {
        return "L-" + n;
    }

    @Override
    public double distance(DoubleMatrix1D vector1, DoubleMatrix1D vector2) throws MetricException {

        return Math.pow(vector1.aggregate(vector2, Functions.plus, LN_METRIC_MATRIX), root);
    }

    class LnMetricMatrix implements DoubleDoubleFunction {
        @Override
        public double apply(double arg0, double arg1) {
            // return Math.pow(arg0-arg1, n);
            return Math.pow(Math.abs(arg0 - arg1), n);
        }

    }
}
