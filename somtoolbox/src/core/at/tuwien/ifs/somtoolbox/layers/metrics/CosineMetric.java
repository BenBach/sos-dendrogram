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
 * Implements the cosine metric, defined for two vectors d1 and d2 as <i>d1xd2 / (|d1|*|d2|)</i>.
 * 
 * @author Rudolf Mayer
 * @version $Id: CosineMetric.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class CosineMetric extends AbstractMetric {

    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        double v1Xv2 = 0;
        double normV1 = 0;
        double normV2 = 0;
        for (int i = 0; i < vector2.length; i++) {
            v1Xv2 += vector1[i] * vector2[i];
            normV1 += vector1[i] * vector1[i];
            normV2 += vector2[i] * vector2[i];
        }
        normV1 = Math.sqrt(normV1);
        normV2 = Math.sqrt(normV2);
        return v1Xv2 / (normV1 * normV2);
    }

    @Override
    public double distance(DoubleMatrix1D vector1, DoubleMatrix1D vector2) throws MetricException {
        double v1Xv2 = vector1.zDotProduct(vector2);
        double normV1 = Math.sqrt(vector1.aggregate(Functions.plus, Functions.square));
        double normV2 = Math.sqrt(vector2.aggregate(Functions.plus, Functions.square));
        return v1Xv2 / (normV1 * normV2);
    }

    @Override
    public String toString() {
        return "Cosine";
    }

    /** Main method to test the metric. */
    public static void main(String[] args) throws MetricException {
        double[] d1 = new double[] { 2, 1, 1, 1, 0 };
        double[] d2 = new double[] { 0, 0, 0, 1, 0 };
        CosineMetric cosineMetric = new CosineMetric();
        // expected result: 0.378
        System.out.println(cosineMetric.distance(d1, d2));

        performanceTest(cosineMetric, 1500000);
    }

}
