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
package at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers;

import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: UnitQEContainer.java 3883 2010-11-02 17:13:23Z frank $
 */
public class UnitQEContainer implements QEContainer {

    private double qe;

    private Unit[] unit;

    public UnitQEContainer() {

    }

    public UnitQEContainer(Unit[] unit, double qe) {
        this.setQE(qe);
        this.setUnit(unit);
    }

    public void setUnit(Unit[] unit) {
        this.unit = unit;
    }

    @Override
    public double getQE() {
        return qe;
    }

    @Override
    public void setQE(double qe) {
        this.qe = qe;
    }

    public int getNumUnits() {
        return this.unit.length;
    }

    public String getUnitCoords(int index) {
        return this.unit[index].getXPos() + "," + this.unit[index].getYPos();
    }

    public int getNumberOfVectorsMapped(int index) {
        return this.unit[index].getNumberOfMappedInputs();
    }
}
