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
package at.tuwien.ifs.somtoolbox.models;

import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;

/**
 * A GHSOMLayer represents all GrowingLayers of a Level.
 * 
 * @author Simon Tragatschnig
 */
public class GHSOMHierarchyRoot extends GHSOMLevelLayer {

    // ausgehend von root-knoten gibt es fuer jede unit im root-layer - untergeordnete layer, welche als ein level
    // dargestellt werden
    public GHSOMHierarchyRoot(GrowingLayer root) {
        super(root);
    }

    /** returns the levelLayer of <code>level</code> */
    public GHSOMLevelLayer getLevel(int level) {
        GHSOMLevelLayer layer = this;
        for (int i = 0; i < level; i++) {
            layer = layer.getChildren();
        }
        return layer;
    }

}
