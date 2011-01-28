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

/**
 * This class can be used to compare two input names by their distance. Used for ordering distances in various places.
 * 
 * @author Rudolf Mayer
 * @version $Id: InputNameDistance.java 3883 2010-11-02 17:13:23Z frank $
 */
public class InputNameDistance implements Comparable<InputNameDistance> {
    private double distance;

    private String label;

    public InputNameDistance(double distance, String input) {
        this.distance = distance;
        this.label = input;
    }

    @Override
    public int compareTo(InputNameDistance otherInput) {
        if (distance != otherInput.distance) {
            return Double.compare(distance, otherInput.distance);
        } else {
            return label.compareTo(otherInput.label);
        }
    }

    public String getLabel() {
        return label;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InputNameDistance)) {
            return false;
        }
        return ((InputNameDistance) obj).distance == distance && ((InputNameDistance) obj).label.equals(label);
    }

    @Override
    public String toString() {
        return new StringBuilder(label).append(" (").append(distance).append(")").toString();
    }
}
