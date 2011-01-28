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

/**
 * A pair of units. Note: for the equality of two pairs, the ordering of their units is NOT relevant.
 * 
 * @author Stefan Ruemmele
 * @author Christian Kapeller
 * @author Frank Pourvoyeur
 * @author Rudolf Mayer
 * @version $Id: UnitPair.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class UnitPair {
    private Unit first;

    private Unit second;

    public UnitPair(Unit first, Unit second) {
        this.first = first;
        this.second = second;
    }

    /** Returns true, if both pairs contain the same units regardless of their ordering. */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UnitPair)) {
            return false;
        }

        UnitPair pair = (UnitPair) obj;
        if (first.equals(pair.first) && second.equals(pair.second)) {
            return true;
        } else if (first.equals(pair.second) && second.equals(pair.first)) {
            return true;
        }
        return false;
    }

    public Unit getFirst() {
        return first;
    }

    public Unit getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return first.getXPos() + "/" + first.getYPos() + " <-> " + second.getXPos() + "/" + second.getYPos();
    }
}