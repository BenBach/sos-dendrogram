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
package at.tuwien.ifs.somtoolbox.visualization.clustering;

import at.tuwien.ifs.somtoolbox.layers.Label;

/**
 * Extends the Label class with an additional value for sorting to determine the order of the labels. The natural order
 * of this class is by ascending 'sortingValue'. If two ClusterLabels have equal sortingValue, they are compared by
 * their names.
 * 
 * @author Angela Roiger
 * @version $Id: ClusterLabel.java 3883 2010-11-02 17:13:23Z frank $
 */
public class ClusterLabel extends Label implements Comparable<ClusterLabel> {
    private static final long serialVersionUID = 1L;

    private double sortingValue = 0.0d;

    public ClusterLabel(Label l, double sortingValue) {
        super(l.getName(), l.getValue(), l.getQe());
        this.sortingValue = sortingValue;
    }

    public ClusterLabel(String name) {
        super(name);
    }

    public ClusterLabel(String name, double value) {
        super(name, value);
    }

    public ClusterLabel(String name, double value, double qe) {
        super(name, value, qe);
    }

    /**
     * Constructs a ClusterLabel object with the given arguments.
     * 
     * @param name the name of the label.
     * @param value the label value.
     * @param qe the quantization error of the label.
     * @param sortingValue the value determining the order of labels
     */
    public ClusterLabel(String name, double value, double qe, double sortingValue) {
        super(name, value, qe);
        this.sortingValue = sortingValue;
    }

    @Override
    public int compareTo(ClusterLabel c) {
        int comp = Double.compare(this.sortingValue, c.getSortingValue());
        if (comp != 0) {
            return comp;
        } else {
            // if values are equal compare the name
            return this.getName().compareTo(c.getName());
        }
    }

    public double getSortingValue() {
        return this.sortingValue;
    }

    public void setSortingValue(double d) {
        this.sortingValue = d;
    }
}
