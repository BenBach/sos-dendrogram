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

/**
 * Represents a cuboid with integer coordinates.
 * 
 * @author Rudolf Mayer
 * @version $Id: Cuboid.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class Cuboid {

    int startX;

    int endX;

    int startY;

    int endY;

    int startZ;

    int endZ;

    public Cuboid(int startX, int endX, int startY, int endY, int startZ, int endZ) {
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
        this.startZ = startZ;
        this.endZ = endZ;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public int getEndZ() {
        return endZ;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getStartZ() {
        return startZ;
    }

    @Override
    public String toString() {
        return startX + "/" + startY + "/" + startZ + " - " + endX + "/" + endY + "/" + endZ;
    }

}
