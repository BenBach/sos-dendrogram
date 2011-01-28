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

/**
 * Implements the L2 or Euclidean metric, considering only those values for the distance calculation that have non-zero
 * values for the first, second or both vectors, depending on the initialisation mode.
 * 
 * @author Rudolf Mayer
 * @version $Id: L2MetricSparse.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class L2MetricSparse extends AbstractMetric {
    private SparcseMetricModes mode;

    /** Initialises the metric with {@link DistanceMetric.SparcseMetricModes#FIRST_NON_ZERO}. */
    public L2MetricSparse() {
        this(SparcseMetricModes.FIRST_NON_ZERO);
    }

    public L2MetricSparse(SparcseMetricModes mode) {
        this.mode = mode;
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[])
     */
    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        checkDimensions(vector1, vector2);
        double dist = 0;
        boolean hadMatch = false;

        // The following code could be written nicer & shorter if we would make the if check inside the loop, as the
        // loop is the same.
        // However doing only one conditional check is better in performance
        if (mode == SparcseMetricModes.BOTH_NON_ZERO) {
            for (int ve = 0; ve < vector1.length; ve++) {
                if (vector1[ve] > 0 && vector2[ve] > 0) {
                    dist += (vector1[ve] - vector2[ve]) * (vector1[ve] - vector2[ve]);
                    hadMatch = true;
                }
            }
        } else if (mode == SparcseMetricModes.FIRST_NON_ZERO) {
            for (int ve = 0; ve < vector1.length; ve++) {
                if (vector1[ve] > 0) {
                    dist += (vector1[ve] - vector2[ve]) * (vector1[ve] - vector2[ve]);
                    hadMatch = true;
                }
            }
        } else {
            for (int ve = 0; ve < vector1.length; ve++) {
                if (vector2[ve] > 0) {
                    dist += (vector1[ve] - vector2[ve]) * (vector1[ve] - vector2[ve]);
                    hadMatch = true;
                }
            }
        }
        if (hadMatch) {
            return Math.sqrt(dist);
        } else {
            System.out.println("returning max value");
            return Double.MAX_VALUE;
        }
    }

    @Override
    public String toString() {
        return "L2-metric sparse";
    }

}
