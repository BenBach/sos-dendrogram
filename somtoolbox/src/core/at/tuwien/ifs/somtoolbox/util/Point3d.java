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
package at.tuwien.ifs.somtoolbox.util;

import java.awt.Point;

/**
 * A representation of a 3-dimensional point, similar to {@link Point}, but using double (or float ({@link Float})
 * precision.
 * 
 * @author Rudolf Mayer
 * @version $Id: Point3d.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class Point3d {

    public double x;

    public double y;

    public double z;

    public Point3d() {
        this(0, 0, 0);
    }

    public Point3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    /** Returns the Euclidean distance between this point and the other. */
    public double distance(Point3d other) {
        double distX = this.x - other.x;
        double distY = this.y - other.y;
        double distZ = this.z - other.z;
        return Math.sqrt(distX * distX + distY * distY + distZ * distZ);
    }

    @Override
    public Object clone() {
        return new Point3d(x, y, z);
    }

}
