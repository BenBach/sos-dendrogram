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
 * Implements the L-Infinity metric,defined for two vectors x and y as <i>max( |xi-yi| ), i = 1,...,|x|</i>.
 * 
 * @author Rudolf Mayer
 * @version $Id: LInfinityMetric.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class LInfinityMetric extends AbstractMetric implements DistanceMetric {

    /** @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[]) */
    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        checkDimensions(vector1, vector2);
        double maxDist = 0;
        for (int i = 0; i < vector1.length; i++) {
            maxDist = Math.max(Math.abs(vector1[i] - vector2[i]), maxDist);
        }
        return maxDist;
    }

    @Override
    public String toString() {
        return "L-infinity-metric";
    }

}
