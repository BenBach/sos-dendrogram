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
package at.tuwien.ifs.somtoolbox.data;

import java.util.HashMap;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * Class representing a specific input datum.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: InputDatum.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class InputDatum {

    private String label;

    private DoubleMatrix1D vector;

    private int dim;

    private HashMap<Object, Object> properties;

    private int nonZeros;

    /**
     * Constructs a new InputDatum.
     * 
     * @param label The label of the input datum. Basically this should be a sort of unique id.
     * @param vector The vector holding the values.
     */
    public InputDatum(String label, DoubleMatrix1D vector) {
        this.label = label;
        dim = vector.size();
        this.vector = new DenseDoubleMatrix1D(dim);
        this.vector.assign(vector);
        this.nonZeros = -1;
    }

    /**
     * Constructs a new InputDatum.
     * 
     * @param label The label of the input datum. Basically this should be a sort of unique id.
     * @param vector The vector holding the values, this time as a double[].
     */
    public InputDatum(String label, double[] vector) {
        // DoubleMatrix1D = new DoubleMatri
        this.label = label;
        dim = vector.length;
        this.vector = new DenseDoubleMatrix1D(dim);
        this.vector.assign(vector);
        this.nonZeros = -1;
    }

    public InputDatum(String label, DoubleMatrix1D vector, int nonZeros) {
        this.label = label;
        this.dim = vector.size();
        this.vector = new DenseDoubleMatrix1D(dim);
        this.vector.assign(vector);
        this.nonZeros = nonZeros;
    }

    /**
     * Returns the label of the InputDatum.
     * 
     * @return the label of the InputDatum.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the vector of the InputDatum.
     * 
     * @return the vector of the InputDatum.
     */
    public DoubleMatrix1D getVector() {
        return vector;
    }

    /**
     * Returns the dimensionality of the vector.
     * 
     * @return the dimensionality of the vector.
     */
    public int getDim() {
        return dim;
    }

    /**
     * Returns the value of a property specified by the key or <code>null</code> if the key does not exist.
     * 
     * @param key The property key.
     * @return the value of this property or null
     */
    public Object getProperty(Object key) {
        if (properties == null || key == null) {
            return null;
        } else {
            return properties.get(key);
        }
    }

    /**
     * Adds an arbitrary key/value property to the InputDatum. Useful for GUIs that need to attach various information
     * to a datum. If value is <code>null</code>, the property is removed.
     * 
     * @param key The property key.
     * @param value The property value.
     */
    public void addProperty(Object key, Object value) {
        if (value == null && properties == null) {
            return;
        }

        if (properties == null) {
            properties = new HashMap<Object, Object>();
        }

        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }

        if (properties.size() == 0) {
            properties = null;
        }
    }

    /**
     * Returns a String representation of this {@link InputDatum} as <code><i>labelName</i>[<i>vector</i>]</code>.
     */
    @Override
    public String toString() {
        return getLabel() + " [" + getVector() + "]";
    }

    /**
     * Compares two {@link InputDatum} by both comparing the labels and vectors.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof InputDatum && ((InputDatum) obj).getLabel().equals(getLabel())
                && ((InputDatum) obj).getVector().equals(getVector());
    }

    public int getFeatureDensity() {
        if (nonZeros == -1) { // if the value was not yet calculated --> calculate it
            calculateFeatureDensity();
        }
        return nonZeros;
    }

    void calculateFeatureDensity() {
        nonZeros = 0;
        for (int i = 0; i < vector.size(); i++) {
            if (vector.getQuick(i) != 0) {
                nonZeros++;
            }
        }
    }

}
