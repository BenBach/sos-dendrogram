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
package at.tuwien.ifs.somtoolbox.apps.viewer;

import edu.umd.cs.piccolo.PNode;

/**
 * Representation of an input object on the map
 * 
 * @author Khalid Latif
 * @version $Id: InputPNode.java 3358 2010-02-11 14:35:07Z mayer $
 */
public abstract class InputPNode extends PNode {

    private static final long serialVersionUID = 1l;

    /** Quarter of the height of the node */
    public static final int HEIGHT_4 = 3;

    /** Half of the height of the node */
    public static final int HEIGHT_2 = HEIGHT_4 * 2;

    /** Height of the node */
    public static final int HEIGHT = HEIGHT_2 * 2;

    /** Quarter of the width of the node */
    public static final int WIDTH_4 = 3;

    /** Half of the width of the node */
    public static final int WIDTH_2 = WIDTH_4 * 2;

    /** Width of the node */
    public static final int WIDTH = WIDTH_2 * 2;

    /** Minimum distance (square) to be maintained between the nodes */
    public static final int MIN_DISTANCE_SQ = WIDTH_4 * HEIGHT_4;

    /**
     * Default constructor
     */
    public InputPNode() {
        setWidth(WIDTH);
        setHeight(HEIGHT);
    }

    /**
     * Initializes this node with the given x, y position
     * 
     * @see PNode#setBounds(double, double, double, double)
     */
    public InputPNode(double x, double y) {
        super.setBounds(x, y, WIDTH, HEIGHT);
    }

}
