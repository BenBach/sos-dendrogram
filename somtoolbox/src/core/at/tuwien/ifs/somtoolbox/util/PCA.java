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
package at.tuwien.ifs.somtoolbox.util;

import org.math.array.DoubleArray;
import org.math.array.StatisticSample;

/**
 * Principal Component Analysis
 * 
 * @version $Id: PCA.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class PCA {

    double[][] X; // initial datas : lines = events and columns = variables

    double[] meanX, stdevX;

    double[][] Z; // X centered reduced

    double[][] cov; // Z covariance matrix

    public double[][] U; // projection matrix

    public double[] info; // information matrix

    public PCA(double[][] X) {
        this.X = X;

        stdevX = StatisticSample.stddeviation(X);
        meanX = StatisticSample.mean(X);

        Z = centerReduce(X);

        cov = StatisticSample.covariance(Z);

        Jama.EigenvalueDecomposition e = org.math.array.LinearAlgebra.eigen(cov);
        U = DoubleArray.transpose(e.getV().getArray());
        info = e.getRealEigenvalues(); // covariance matrix is symetric, so only real eigenvalues...
    }

    // normalization of x relatively to X mean and standard deviation
    public double[][] centerReduce(double[][] x) {
        double[][] y = new double[x.length][x[0].length];
        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < y[i].length; j++) {
                y[i][j] = (x[i][j] - meanX[j]) / stdevX[j];
            }
        }
        return y;
    }

    // de-normalization of y relatively to X mean and standard deviation
    public double[] invCenterReduce(double[] y) {
        return invCenterReduce(new double[][] { y })[0];
    }

    // de-normalization of y relatively to X mean and standard deviation
    public double[][] invCenterReduce(double[][] y) {
        double[][] x = new double[y.length][y[0].length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                x[i][j] = y[i][j] * stdevX[j] + meanX[j];
            }
        }
        return x;
    }

}