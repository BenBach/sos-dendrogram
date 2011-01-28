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

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;

/**
 * A wrapper class around other distance metrics, modifying the distance computation in such a way that only vector
 * attributes that are not missing (indicated by {@link InputData#MISSING_VALUE} are considered.<br/>
 * When instantiating using the empty constructor {@link #MissingValueMetricWrapper()} the default metric
 * {@link #DEFAULT_METRIC} is used.
 * 
 * @author Rudolf Mayer
 * @version $Id: MissingValueMetricWrapper.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class MissingValueMetricWrapper extends AbstractMetric {
    private static final L2Metric DEFAULT_METRIC = new L2Metric();

    private DistanceMetric metric;

    public MissingValueMetricWrapper() {
        this(DEFAULT_METRIC);
    }

    public MissingValueMetricWrapper(DistanceMetric metric) {
        this.metric = metric;
    }

    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        // prepare the input vectors, i.e. remove elements with missing values
        ArrayList<Double> vec1 = new ArrayList<Double>();
        ArrayList<Double> vec2 = new ArrayList<Double>();
        for (int i = 0; i < vector1.length; i++) {
            if (vector1[i] == vector1[i] && vector2[i] == vector2[i]) { // see Double.isNan() for this check
                vec1.add(vector1[i]);
                vec2.add(vector2[i]);
            }
        }
        if (vector1.length != vec1.size()) {
            vector1 = ArrayUtils.toPrimitive(vec1.toArray(new Double[vec1.size()]));
            vector2 = ArrayUtils.toPrimitive(vec2.toArray(new Double[vec2.size()]));
        }
        return metric.distance(vector1, vector2);
    }

    @Override
    public void setMetricParams(String metricParamString) throws SOMToolboxException {
        super.setMetricParams(metricParamString);
        String[] params = metricParamString.split(";");
        for (String string : params) {
            String[] parts = string.split("=");
            if (parts[0].equals("class")) {
                metric = instantiateNice(parts[1]);
            }
        }
    }

    public void setMetric(DistanceMetric metric) {
        this.metric = metric;
    }

}
