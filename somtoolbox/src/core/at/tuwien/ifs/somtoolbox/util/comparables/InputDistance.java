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
package at.tuwien.ifs.somtoolbox.util.comparables;

import java.util.Collection;
import java.util.Iterator;

import at.tuwien.ifs.somtoolbox.data.InputDatum;

/**
 * This class can be used to compare two InputDatum objects by their distance. Used for ordering distances in various
 * places.
 * 
 * @author Rudolf Mayer
 * @version $Id: InputDistance.java 3883 2010-11-02 17:13:23Z frank $
 */
public class InputDistance implements Comparable<InputDistance> {
    private double distance;

    private InputDatum input;

    public InputDistance(double distance, InputDatum input) {
        this.distance = distance;
        this.input = input;
    }

    @Override
    public int compareTo(InputDistance otherInput) {
        if (distance != otherInput.distance) {
            return Double.compare(distance, otherInput.distance);
        } else {
            return input.getLabel().compareTo(otherInput.getInput().getLabel());
        }
    }

    public InputDatum getInput() {
        return input;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InputDistance)) {
            return false;
        }
        return ((InputDistance) obj).distance == distance && ((InputDistance) obj).input.equals(input);
    }

    public static double[] getDistanceValuesOnly(Collection<InputDistance> distances) {
        double[] values = new double[distances.size()];
        int index = 0;
        for (Iterator<InputDistance> iterator = distances.iterator(); iterator.hasNext(); index++) {
            InputDistance inputDistance = iterator.next();
            values[index] = inputDistance.getDistance();

        }
        return values;
    }
}
