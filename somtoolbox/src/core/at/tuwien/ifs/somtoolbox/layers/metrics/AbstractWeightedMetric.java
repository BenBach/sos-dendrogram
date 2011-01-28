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

import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Rudolf Mayer
 * @version $Id: AbstractWeightedMetric.java 3583 2010-05-21 10:07:41Z mayer $
 */
public abstract class AbstractWeightedMetric extends AbstractMetric {
    protected double[] featureWeights;

    public static AbstractWeightedMetric instantiate(String mName) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        return (AbstractWeightedMetric) Class.forName(mName).newInstance();
    }

    public abstract double distance(double[] vector1, double[] vector2, double[] weights) throws MetricException;

    public double distance(double[] vector, Unit unit) throws MetricException {
        return distance(vector, unit.getWeightVector(), unit.getFeatureWeights());
    }

    public double distance(InputDatum inputDatum, Unit unit) throws MetricException {
        return distance(inputDatum.getVector().toArray(), unit.getWeightVector(), unit.getFeatureWeights());
    }

    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        throw new MetricException("Distance measurement without feature weights not supported!");
    }
}
