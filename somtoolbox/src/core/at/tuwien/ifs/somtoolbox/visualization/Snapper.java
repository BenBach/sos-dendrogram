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
package at.tuwien.ifs.somtoolbox.visualization;

import at.tuwien.ifs.somtoolbox.clustering.DistanceFunctionType;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;

public class Snapper {

    protected AbstractMetric distanceFunction;

    protected DistanceFunctionType lineDistanceFunction;

    public Snapper() {
        distanceFunction = new L2Metric();
        lineDistanceFunction = DistanceFunctionType.Euclidean;
    }

    public Snapper(AbstractMetric distanceFunction, DistanceFunctionType lineDistanceFunction) {
        this.distanceFunction = distanceFunction;
        this.lineDistanceFunction = lineDistanceFunction;
    }
}
