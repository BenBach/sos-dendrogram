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
package at.tuwien.ifs.somtoolbox.structures;

import java.awt.geom.Point2D;

/**
 * 2D version of a component line
 * 
 * @author Robert Neumayer
 * @version $Id: ComponentLine2D.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class ComponentLine2D extends ElementWithIndex {

    private Point2D[] points;

    public ComponentLine2D(Point2D[] points) {
        super(-1);
        this.points = points;
    }

    public ComponentLine2D(Point2D[] points, Integer index) {
        super(index);
        this.points = points;
    }

    public Point2D get(int index) {
        return points[index];
    }

    public Point2D[] getPoints() {
        return points;
    }

    public int getLength() {
        return points.length;
    }

}