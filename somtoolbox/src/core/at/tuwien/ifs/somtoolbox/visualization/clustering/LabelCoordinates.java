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

import java.awt.Point;

import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * @author Doris Baum
 * @version $Id: LabelCoordinates.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class LabelCoordinates extends Point {
    private static final long serialVersionUID = 1L;

    public String label;

    public LabelCoordinates() {
        super();
        label = "";
    }

    public LabelCoordinates(int x, int y, String label) {
        super(x, y);
        this.label = label;
    }

    /** Calculate euclidean distance between this point and the other point */
    public double distance(LabelCoordinates other) throws MetricException {
        // simple distance between two points
        return Math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y));
    }
}
