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
package at.tuwien.ifs.somtoolbox.util.growingCellStructures;

import at.tuwien.ifs.somtoolbox.layers.GrowingCellUnit;

/**
 * A Tetraheder of 3 Cellstructures (for efficient adding and removing of units)
 * 
 * @author Johannes Inführ
 * @author Andreas Zweng
 * @version $Id: GrowingCellTetraheder.java 3585 2010-05-21 10:33:21Z mayer $
 */
public class GrowingCellTetraheder {
    /** The Units that belong to this Tetraheder */
    private GrowingCellUnit[] cellUnits;

    /**
     * Std Constructor, creates Tetraheder with Units c1,c2 and c3 (and connects them to this tetraheder)
     * 
     * @param c1 Unit1
     * @param c2 Unit2
     * @param c3 Unit3
     */
    public GrowingCellTetraheder(GrowingCellUnit c1, GrowingCellUnit c2, GrowingCellUnit c3) {
        cellUnits = new GrowingCellUnit[3];

        cellUnits[0] = c1;
        cellUnits[1] = c2;
        cellUnits[2] = c3;

        c1.connect(this);
        c2.connect(this);
        c3.connect(this);
    }

    public GrowingCellUnit[] getCellUnits() {
        return cellUnits;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    /**
     * @param unit the Unit
     * @return true if this Tetraeder connects unit
     */
    public boolean contains(GrowingCellUnit unit) {
        for (GrowingCellUnit u : cellUnits) {
            if (u.equals(unit)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param u1 Unit1
     * @param u2 Unit2
     * @return Unit !=u1 and !=u2
     */
    public GrowingCellUnit getRemainingUnit(GrowingCellUnit u1, GrowingCellUnit u2) {
        for (GrowingCellUnit u : cellUnits) {
            if (!u.equals(u1) && !u.equals(u2)) {
                return u;
            }
        }

        return null;
    }
}
