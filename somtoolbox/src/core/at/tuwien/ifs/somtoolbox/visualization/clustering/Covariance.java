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

import org.apache.commons.lang.ArrayUtils;

public class Covariance {

    public static double[][] covariance(double[][] v1) {
        return covariance(v1, v1);
    }

    public static double[][] covariance(double[][] v1, double[][] v2) {
        int m = v1.length;
        int n1 = v1[0].length;
        int n2 = v2[0].length;
        double[][] X = new double[n1][n2];
        int degrees = m - 1;
        double c;
        double s1;
        double s2;
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                c = 0;
                s1 = 0;
                s2 = 0;
                for (int k = 0; k < m; k++) {
                    s1 += v1[k][i];
                    s2 += v2[k][j];
                }
                s1 = s1 / m;
                s2 = s2 / m;
                for (int k = 0; k < m; k++) {
                    c += (v1[k][i] - s1) * (v2[k][j] - s2);
                }
                X[i][j] = c / degrees;
            }
        }
        return X;
    }

    public static void main(String args[]) {

        double[] x = { 4.0000, 4.2000, 3.9000, 4.3000, 4.1000 };
        double[] y = { 2.0000, 2.1000, 2.0000, 2.1000, 2.2000 };

        double sum = 0;
        for (int i = 0; i < y.length; i++) {
            sum += x[i] * y[i];

        }
        System.out.println("Sum: " + sum);

        double[][] o = new double[2][];
        o[0] = x;
        o[1] = y;

        double[][] covar = covariance(o);
        for (double[] element : covar) {
            System.out.println(ArrayUtils.toString(element));
        }

    }

}
