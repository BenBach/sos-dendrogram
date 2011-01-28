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

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputDatum;

/**
 * The interface, distance metric classes have to implement.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: DistanceMetric.java 3743 2010-08-06 15:37:15Z mayer $
 */
public interface DistanceMetric extends Comparable<DistanceMetric> {

    public enum SparcseMetricModes {
        /** Mode where both vector elements have to be non-zero. */
        BOTH_NON_ZERO,
        /** Mode where only the first vector element has to be non-zero. */
        FIRST_NON_ZERO,
        /** Mode where only the second vector element has to be non-zero. */
        SECOND_NON_ZERO
    }

    /**
     * Calculates the distance between two vectors provided by argument <code>vector1</code> and <code>vector2</code>. A
     * <code>MetricException</code> is thrown, if the dimensionalities of the two vectors differ.
     * 
     * @param vector1 first vector.
     * @param vector2 second vector.
     * @return the distance between <code>vector1</code> and <code>vector2</code>.
     * @throws MetricException if the dimensionalities of the two vectors differ.
     */
    public double distance(double[] vector1, double[] vector2) throws MetricException;

    /**
     * Calculates the distance between two vectors provided by argument <code>vector1</code> and <code>vector2</code>. A
     * <code>MetricException</code> is thrown, if the dimensionalities of the two vectors differ.
     * 
     * @param vector1 first vector.
     * @param vector2 second vector.
     * @return the distance between <code>vector1</code> and <code>vector2</code>.
     * @throws MetricException if the dimensionalities of the two vectors differ.
     */
    public double distance(double[] vector1, DoubleMatrix1D vector2) throws MetricException;

    /**
     * Calculates the distance between two vectors provided by argument <code>vector</code> and <code>datum</code>. A
     * <code>MetricException</code> is thrown, if the dimensionalities of the two vectors differ.
     * 
     * @param vector first vector.
     * @param datum input datum.
     * @return the distance between <code>vector</code> and <code>datum</code>.
     * @throws MetricException if the dimensionalities of the two vectors differ.
     */
    public double distance(double[] vector, InputDatum datum) throws MetricException;

    /**
     * Calculates the distance between two vectors provided by argument <code>vector1</code> and <code>vector2</code>. A
     * <code>MetricException</code> is thrown, if the dimensionalities of the two vectors differ.
     * 
     * @param vector1 first vector.
     * @param vector2 second vector.
     * @return the distance between <code>vector1</code> and <code>vector2</code>.
     * @throws MetricException if the dimensionalities of the two vectors differ.
     */
    public double distance(DoubleMatrix1D vector1, double[] vector2) throws MetricException;

    /**
     * Calculates the distance between two vectors provided by argument <code>vector1</code> and <code>vector2</code>. A
     * <code>MetricException</code> is thrown, if the dimensionalities of the two vectors differ.
     * 
     * @param vector1 first vector.
     * @param vector2 second vector.
     * @return the distance between <code>vector1</code> and <code>vector2</code>.
     * @throws MetricException if the dimensionalities of the two vectors differ.
     */
    public double distance(DoubleMatrix1D vector1, DoubleMatrix1D vector2) throws MetricException;

    /**
     * Calculates the distance between two vectors provided by argument <code>vector</code> and <code>datum</code>. A
     * <code>MetricException</code> is thrown, if the dimensionalities of the two vectors differ.
     * 
     * @param vector first vector.
     * @param datum input datum.
     * @return the distance between <code>vector</code> and <code>datum</code>.
     * @throws MetricException if the dimensionalities of the two vectors differ.
     */
    public double distance(DoubleMatrix1D vector, InputDatum datum) throws MetricException;

    /**
     * Calculates the distance between two vectors provided by argument <code>datum</code> and <code>vector</code>. A
     * <code>MetricException</code> is thrown, if the dimensionalities of the two vectors differ.
     * 
     * @param datum input datum.
     * @param vector first vector.
     * @return the distance between <code>datum</code> and <code>vector</code>.
     * @throws MetricException if the dimensionalities of the two vectors differ.
     */
    public double distance(InputDatum datum, double[] vector) throws MetricException;

    /**
     * Calculates the distance between two vectors provided by argument <code>datum</code> and <code>vector</code>. A
     * <code>MetricException</code> is thrown, if the dimensionalities of the two vectors differ.
     * 
     * @param datum input datum.
     * @param vector first vector.
     * @return the distance between <code>datum</code> and <code>vector</code>.
     * @throws MetricException if the dimensionalities of the two vectors differ.
     */
    public double distance(InputDatum datum, DoubleMatrix1D vector) throws MetricException;

    /**
     * Calculates the distance between two vectors provided by argument <code>datum</code> and <code>datum2</code>. A
     * <code>MetricException</code> is thrown, if the dimensionalities of the two vectors differ.
     * 
     * @param datum first input datum.
     * @param datum2 second input datum.
     * @return the distance between <code>datum</code> and <code>vector</code>.
     * @throws MetricException if the dimensionalities of the two vectors differ.
     */
    public double distance(InputDatum datum, InputDatum datum2) throws MetricException;

    public double transformValue(double value);

    public double[] transformVector(double[] vector);

    /**
     * Sets additional parameters needed for the metric. The format of the parameter string is unspecified and depends
     * on the specific sub-class.
     */
    public void setMetricParams(String metricParamString) throws SOMToolboxException;

}
