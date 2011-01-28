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

import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * Implements the Mahalanobis distance metric. This metric requires the covariance matrix of the input data to be
 * pre-calculated and set via the {@link #init(double[][])} method prior to calculating distances.
 * 
 * @author Rudolf Mayer
 * @version $Id: MahalanobisMetric.java 3583 2010-05-21 10:07:41Z mayer $
 */

public class MahalanobisMetric extends AbstractMetric {

    double[][] covarianceMatrix;

    public void init(double[][] covarianceMatrix) {
        this.covarianceMatrix = covarianceMatrix;
    }

    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        if (covarianceMatrix == null) {
            throw new MetricException(
                    "Mahalanobis metric not initialised with covariance matrix, run init(double[][] covarianceMatrix) first!");
        }
        double squareSum = 0.0;
        final double[] diff = VectorTools.subtract(vector1, vector2);
        VectorTools.multiply(VectorTools.multiply(diff, covarianceMatrix), diff);
        return squareSum;
    }
}
