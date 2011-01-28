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
 * This class can be used to compare two regions of a metro map visualisation.
 * 
 * @author Rudolf Mayer
 * @version $Id: ComponentRegionCount.java 3883 2010-11-02 17:13:23Z frank $
 */
public class ComponentRegionCount implements Comparable<ComponentRegionCount> {
    private int numberOfRegion;

    private Integer index;

    public ComponentRegionCount(int distance, Integer index) {
        this.numberOfRegion = distance;
        this.index = index;
    }

    @Override
    public int compareTo(ComponentRegionCount other) {
        if (numberOfRegion != other.numberOfRegion) {
            return Double.compare(numberOfRegion, other.numberOfRegion);
        } else {
            return index.compareTo(other.index);
        }
    }

    public Integer getIndex() {
        return index;
    }

    public int getNumberOfRegion() {
        return numberOfRegion;
    }

    public double getFactor(int numberOfBins) {
        return (double) numberOfRegion / (double) numberOfBins;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComponentRegionCount)) {
            return false;
        }
        return ((ComponentRegionCount) obj).numberOfRegion == numberOfRegion
                && ((ComponentRegionCount) obj).index.equals(index);
    }

}
