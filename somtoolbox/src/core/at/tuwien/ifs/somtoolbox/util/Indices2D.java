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
 * @author Rudolf Mayer
 * @version $Id: Indices2D.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class Indices2D {
    public int startX;

    public int startY;

    public int endX;

    public int endY;

    public Indices2D(int startX, int startY) {
        this(startX, startY, 0, 0);
    }

    public Indices2D(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public void setEnd(int endX, int endY) {
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public String toString() {
        return startX + "/" + startY + " - " + endX + "/" + endY;
    }

}
