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
 * Implements a fast version of the L2 or Euclidean metric, by not taking the square root. Thus, this implementation
 * should be used only when the ranking of distances is important, and the total distance value does not matter.
 * 
 * @author Rudolf Mayer
 * @version $Id: L2MetricFast.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class L2MetricFast extends L2Metric {

    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        double dist = 0;
        for (int i = 0; i < vector1.length; i++) {
            dist += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
        }
        return dist;
    }

    @Override
    public String toString() {
        return "L2-Fast";
    }

}
