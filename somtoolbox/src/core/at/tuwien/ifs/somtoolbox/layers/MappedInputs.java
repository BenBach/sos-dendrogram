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
package at.tuwien.ifs.somtoolbox.layers;

import java.util.Vector;

/**
 * Wrapper class for input data mapped onto units. This class holds the names and distances of input data, both sorted
 * by ascending distance.
 * 
 * @author Michael Dittenbach
 * @version $Id: MappedInputs.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class MappedInputs {
    private Vector<Double> inputDistances;

    private Vector<String> inputNames;

    private int number = 0;

    /**
     * Default Constructor.
     */
    public MappedInputs() {
        inputNames = new Vector<String>();
        inputDistances = new Vector<Double>();
    }

    /**
     * Adds an input datum defined by argument <code>name</code> and <code>distance</code> to the list. It is inserted
     * at a position to retain ordering by ascending distance.
     * 
     * @param name the identification string of the input datum.
     * @param dist the distance between a unit's weight vector and the inptu datum. This value is precalcuated and
     *            independent from this class.
     */
    public void addMappedInput(String name, Double dist) {
        if (number == 0) { // first input
            inputNames.addElement(name);
            inputDistances.addElement(dist);
            number++;
        } else { // try inbetween
            int i = 0;
            boolean inserted = false;
            double distDoubleValue = dist.doubleValue();
            while (i < number && inserted == false) {
                if (distDoubleValue <= inputDistances.elementAt(i).doubleValue()) {
                    inputNames.insertElementAt(name, i);
                    inputDistances.insertElementAt(dist, i);
                    number++;
                    inserted = true;
                } else {
                    i++;
                }
            }
            if (inserted == false && i == number) { // input with highest distance
                inputNames.addElement(name);
                inputDistances.addElement(dist);
                number++;
            }
        }
    }

    /**
     * Clears all input data. This method removes all elements from the lists and sets the <code>number</code> variable
     * to 0 accordingly.
     */
    public void clearMappedInputs() {
        inputNames.removeAllElements();
        inputDistances.removeAllElements();
        number = 0;
    }

    /**
     * Returns an array of distances between this unit's weight vector and the vectors of the mapped input data. The
     * array is sorted from smallest to largest distance.
     * 
     * @return an array of distances between this unit's weight vector and the vectors of the mapped input data.
     */
    public double[] getMappedInputDistances() {
        if (number == 0) {
            return null;
        } else {
            double[] res = new double[number];
            for (int i = 0; i < number; i++) {
                res[i] = inputDistances.elementAt(i).doubleValue();
            }
            return res;
        }
    }

    /**
     * Returns an array of strings containing the identifiers of the mapped input data. The array is sorted from the
     * input with the smallest distance to the one having the largest in analogy to {@link #getMappedInputDistances()}.
     * 
     * @return an array of strings containing the identifiers of the mapped input data.
     */
    public String[] getMappedInputNames() {
        if (number == 0) {
            return null;
        } else {
            String[] res = new String[number];
            for (int i = 0; i < number; i++) {
                res[i] = inputNames.elementAt(i);
            }
            return res;
        }
    }

    /**
     * Returns the number of mapped input data.
     * 
     * @return the number of mapped input data.
     */
    public int getNumberOfMappedInputs() {
        return number;
    }

    /**
     * Checks if an input datum with identifier <code>name</code> is contained.
     * 
     * @param name the name of the input datum to be checked.
     * @return <code>true</code> if an input datum with identifier specified by argument <code>name</code> is mapped
     *         onto this unit, <code>false</code> otherwise.
     */
    public boolean isMapped(String name) {
        return inputNames.contains(name);
    }

}
