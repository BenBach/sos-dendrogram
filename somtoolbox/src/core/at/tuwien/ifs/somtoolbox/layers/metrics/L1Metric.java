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

import cern.colt.matrix.DoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * Implements the L1 or city block metric. Though this class could us at.tuwien.ifs.somtoolbox.layers.metrics.LNMetric,
 * for performance issues this less complex computation should be used.
 * 
 * @author Michael Dittenbach
 * @version $Id: L1Metric.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class L1Metric extends AbstractMetric implements DistanceMetric {

    /** @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[]) */
    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        checkDimensions(vector1, vector2);
        double dist = 0;
        for (int i = 0; i < vector1.length; i++) {
            dist += Math.abs(vector1[i] - vector2[i]);
        }
        return dist;
    }

    @Override
    public double distance(DoubleMatrix1D vector1, DoubleMatrix1D vector2) throws MetricException {
        return vector1.aggregate(vector2, Functions.plus, Functions.chain(Functions.abs, Functions.minus));
    }

    @Override
    public String toString() {
        return "L1";
    }
}
