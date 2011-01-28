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
package at.tuwien.ifs.somtoolbox.layers.quality;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * Implementation of SOM Inversion Measure for multidimenional Inputdata. by Zrehen and Blayo, 1992
 * 
 * @author Christoph Hohenwarter
 * @version $Id: InversionMeasure.java 3883 2010-11-02 17:13:23Z frank $
 */
public class InversionMeasure extends AbstractQualityMeasure {

    private double inversion = 0.0;

    public InversionMeasure(Layer layer, InputData data) {
        super(layer, data);

        mapQualityNames = new String[] { "inversion" };
        mapQualityDescriptions = new String[] { "Inversion Measure" };

        DistanceMetric metric = layer.getMetric();
        ArrayList<Unit> neurons = new ArrayList<Unit>();

        int xs = layer.getXSize();
        int ys = layer.getYSize();
        int N = xs * ys;
        int Ck = 0;
        int Card = 0;
        int sum = 0;

        // Construction of an array A of all neurons
        for (int xi = 0; xi < xs; xi++) {
            for (int yi = 0; yi < ys; yi++) {
                try {
                    neurons.add(layer.getUnit(xi, yi));
                } catch (LayerAccessException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        // Construction of AxA out of the array A of the neurons
        // (n1,n2) element of AxA with n1!=n2
        for (int n1 = 0; n1 < neurons.size(); n1++) {
            for (int n2 = 0; n2 < neurons.size(); n2++) {
                if (n1 != n2) {
                    Unit neuro1 = neurons.get(n1);
                    Unit neuro2 = neurons.get(n2);

                    // Number of pairs of AxA without n1==n2
                    Ck++;

                    Card = 0;
                    for (int n3 = 0; n3 < neurons.size(); n3++) {

                        // If Wk is Wi or Wj then there is no calculation
                        if (n3 != n1 && n3 != n2) {
                            Unit neuro3 = neurons.get(n3);
                            double t1 = 0.0;
                            double t2 = 0.0;
                            try { // Formula by Zrehen and Blayo
                                t1 = metric.distance(neuro3.getWeightVector(), multScalVec(0.5, addVec(
                                        neuro1.getWeightVector(), neuro2.getWeightVector())));
                                t2 = 0.5 * metric.distance(neuro1.getWeightVector(), neuro2.getWeightVector());
                            } catch (MetricException e) {
                                e.printStackTrace();
                            }
                            // If the diskproperty D is fullfilled
                            if (t1 <= t2) {
                                Card++;
                            }
                        }
                    }
                    // Sum of the neurons with weightvector fullfilling
                    // the diskproperty D
                    sum = sum + Card;
                }
            }
        }
        // Result of the sum of neurons fulling the diskproperty skaled by the
        // number of pairs
        inversion = 1.0 / (Ck * (N - 2.0)) * sum;
    }

    // Basic function for addition of two vectors
    private double[] addVec(double[] v1, double[] v2) {

        double[] v3 = new double[v1.length];

        for (int i = 0; i < v1.length; i++) {
            v3[i] = v1[i] + v2[i];
        }

        return v3;
    }

    // Multiplikation of a vector with a scalar
    private double[] multScalVec(double x, double[] vec) {
        double[] v = new double[vec.length];

        for (int i = 0; i < vec.length; i++) {
            v[i] = x * vec[i];
        }

        return v;
    }

    @Override
    public double getMapQuality(String name) throws QualityMeasureNotFoundException {

        return inversion;
    }

    @Override
    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {

        throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
    }

}
