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

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * Base container for input data mapped onto units. This class holds the names and distances of input data, both sorted
 * by ascending distance.
 * 
 * @author Michael Dittenbach
 * @author Khalid Latif
 * @version $Id: InputContainer.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class InputContainer {

    private Vector<Double> inputDistances;

    private Vector<String> inputNames;

    /**
     * Default Constructor.
     */
    public InputContainer() {
        inputNames = new Vector<String>();
        inputDistances = new Vector<Double>();
    }

    /**
     * Adds an input datum defined by argument <code>name</code> and <code>distance</code> to the list. It is inserted
     * at a position to retain ordering by ascending distance. If two inputs have the same distance, ordering is
     * lexicographically.
     * 
     * @param name the identification string of the input datum.
     * @param dist the distance between a unit's weight vector and the inptu datum. This value is precalcuated and
     *            independent from this class.
     */
    protected void addMappedInput(String name, Double dist) {
        double dist0 = dist.doubleValue();

        for (int i = 0; i < inputNames.size(); i++) {
            if (dist0 <= inputDistances.get(i).doubleValue() && name.compareTo(inputNames.get(i)) < 0) {
                inputNames.add(i, name);
                inputDistances.add(i, dist);
                // Thats all folks!
                return;
            }
        }
        // The control moves here only if the IF inside FOR loop fails
        // for all elements meaning the the new input has highest distance or it is first input
        inputNames.add(name);
        inputDistances.add(dist);
    }

    /**
     * Returns the number of mapped input data.
     * 
     * @return the number of mapped input data.
     */
    public int getNumberOfMappedInputs() {
        return inputNames.size();
    }

    /**
     * Clears all input data. This method removes all elements from the lists and sets the <code>number</code> variable
     * to 0 accordingly.
     */
    public void clearMappedInputs() {
        inputNames.removeAllElements();
        inputDistances.removeAllElements();
    }

    /**
     * Returns an array of distances between this unit's weight vector and the vectors of the mapped input data. The
     * array is sorted from smallest to largest distance.
     * 
     * @return an array of distances between this unit's weight vector and the vectors of the mapped input data.
     */
    public double[] getMappedInputDistances() {
        if (inputDistances.isEmpty()) {
            return null;
        }

        double[] res = new double[inputDistances.size()];
        for (int i = 0; i < inputDistances.size(); i++) {
            res[i] = inputDistances.get(i).doubleValue();
        }
        return res;
    }

    /**
     * Returns the distance between this unit's weight vector and the vector of the mapped input data at the specified
     * <code>index</code>.
     * 
     * @see java.util.List#get(int)
     */
    public double getMappedInputDistance(int index) {
        return inputDistances.get(index);
    }

    public double getMappedInputDistance(String label) throws SOMToolboxException {
        int index = inputNames.indexOf(label);
        if (index == -1) {
            throw new SOMToolboxException("No input '" + label + "' mapped on " + this);
        } else {
            return inputDistances.get(index);
        }
    }

    /**
     * Returns an array of strings containing the identifiers of the mapped input data. The array is sorted from the
     * input with the smallest distance to the one having the largest in analogy to {@link #getMappedInputDistances()}.
     * 
     * @return an array of strings containing the identifiers of the mapped input data.
     */
    public String[] getMappedInputNames() {
        if (inputNames.isEmpty()) {
            return null;
        }
        return inputNames.toArray(new String[inputNames.size()]);
    }

    public Vector<String> getMappedInputNamesAsList() {
        return inputNames;
    }

    /**
     * Returns the name identifier of the mapped input data at the specified <code>index</code>.
     * 
     * @see java.util.List#get(int)
     */
    public String getMappedInputName(int index) {
        return inputNames.get(index);
    }

    /**
     * Finds the index for a given input name.
     */
    public int getInputIndex(String name) {
        for (int i = 0; i < inputNames.size(); i++) {
            if (inputNames.get(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if an input datum with identifier <code>name</code> is mapped onto this container.
     * 
     * @param name the name of the input datum to be checked.
     * @return <code>true</code> if an input datum with identifier specified by argument <code>name</code> is mapped
     *         onto this unit, <code>false</code> otherwise.
     */
    public boolean isMapped(String name) {
        return inputNames.contains(name);
    }

    public void removeMappedInput(String label) {
        int index = getInputIndex(label);
        inputNames.remove(index);
        inputDistances.remove(index);
    }

}
