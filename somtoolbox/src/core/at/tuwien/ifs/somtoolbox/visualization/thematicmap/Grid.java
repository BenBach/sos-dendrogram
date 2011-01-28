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
package at.tuwien.ifs.somtoolbox.visualization.thematicmap;

import edu.cornell.cs.voronoi.Pnt;

/**
 * @author Taha Abdel Aziz
 * @version $Id: Grid.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class Grid extends Pnt {
    public Pnt topLeft;

    public Pnt bottomRight;

    public Pnt center;

    public boolean occupied;

    public static final double SIZE = 1.0;

    public SOMClass clss;

    /** Creates a new instance of Grid */
    public Grid(Pnt _topLeft, Pnt _bottomRight) {
        super(_topLeft.coord(0) / 2 + _bottomRight.coord(0) / 2, _topLeft.coord(1) / 2 + _bottomRight.coord(1) / 2);
        this.topLeft = _topLeft;
        this.bottomRight = _bottomRight;
    }

    /** Creates a new instance of Grid */
    public Grid(Pnt _topLeft) {
        super(_topLeft.coord(0) / 2 + SIZE / 2, _topLeft.coord(1) / 2 + SIZE / 2);
        this.topLeft = _topLeft;
        this.bottomRight = new Pnt(_topLeft.coord(0) / 2 + SIZE, _topLeft.coord(1) / 2 + SIZE);
    }

}
