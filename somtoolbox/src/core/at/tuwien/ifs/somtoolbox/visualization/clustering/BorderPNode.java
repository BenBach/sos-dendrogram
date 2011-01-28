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

import java.awt.BasicStroke;
import java.util.ListIterator;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * Stores all the border lines for one cluster.
 * 
 * @author Angela Roiger
 * @version $Id: BorderPNode.java 3888 2010-11-02 17:42:53Z frank $
 */
public class BorderPNode extends PNode {
    private static final long serialVersionUID = 1L;

    /**
     * Changes the stroke of the border to the specified BasicStroke
     * 
     * @param bs the new stroke for the border
     */
    @SuppressWarnings("rawtypes")
    public void changeBorderStroke(BasicStroke bs) {
        for (ListIterator li = this.getChildrenIterator(); li.hasNext();) {
            ((PPath) li.next()).setStroke(bs);
        }
    }

}
