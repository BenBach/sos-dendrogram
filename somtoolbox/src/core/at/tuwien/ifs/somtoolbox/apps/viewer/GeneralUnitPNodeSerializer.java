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

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * replacement class for serializing and de-serializing a {@link GeneralUnitPNode}
 * 
 * @author Angela Roiger
 * @version $Id: GeneralUnitPNodeSerializer.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class GeneralUnitPNodeSerializer implements Serializable {
    private static final long serialVersionUID = 1L;

    // position of the GeneralUnitPNode in MapPNode.units
    private int x;

    private int y;

    // for deserialization
    public GeneralUnitPNodeSerializer() {
    }

    public GeneralUnitPNodeSerializer(GeneralUnitPNode unit) {
        this.x = unit.getUnit().getXPos();
        this.y = unit.getUnit().getYPos();

        // small check to make sure we can restore it
        if (CommonSOMViewerStateData.getInstance().mapPNode.getUnit(x, y) != unit) {
            throw new AssertionError("GeneralUnitPNode is not where it's supposed to be.");
        }
    }

    // Called by deserialization. Find and return the "real" GeneralUnitPNode.
    private Object readResolve() throws ObjectStreamException {
        return CommonSOMViewerStateData.getInstance().mapPNode.getUnit(x, y);
    }
}
