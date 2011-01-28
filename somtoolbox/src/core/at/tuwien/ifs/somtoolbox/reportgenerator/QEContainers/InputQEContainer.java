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

import java.util.Vector;

import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: InputQEContainer.java 3883 2010-11-02 17:13:23Z frank $
 */
public class InputQEContainer implements QEContainer {

    private double qe;

    private Vector<InputDatum> inputs;

    private Vector<Unit> units;

    public InputQEContainer() {
    }

    /** add a new input datum to the container */
    public void addInput(InputDatum input) {
        if (this.inputs == null) {
            this.inputs = new Vector<InputDatum>();
        }
        this.inputs.add(input);
    }

    /** set the complete list of input data stored in this container (existing items are overridden) */
    public void setInputs(Vector<InputDatum> inputs) {
        this.inputs = inputs;
    }

    /**
     * removes all inputs stored in this container
     */
    public void clearInputs() {
        this.inputs.removeAllElements();
    }

    /**
     * return the input datum at the specified index
     * 
     * @param i the index from which the index datum shall be returned. Not checked whether there exits one ... so watch
     *            out
     * @return the input datum at the specified index
     */
    public InputDatum getInput(int i) {
        return this.inputs.get(i);
    }

    /**
     * adds a unit to this container
     * 
     * @param unit the unit to add
     */
    public void addMapUnit(Unit unit) {
        if (this.units == null) {
            this.units = new Vector<Unit>();
        }
        this.units.add(unit);
    }

    /**
     * sets the complete list of units stored in this container. Old list is overridden
     * 
     * @param units the list of units to store
     */
    public void setMapUnits(Vector<Unit> units) {
        this.units = units;
    }

    /**
     * removes all units from the list
     */
    public void clearMapUnits() {
        this.units.removeAllElements();
    }

    /**
     * returns the unit at the specified index
     * 
     * @param i the index of the unit to return (is not checked whether this index exists - watch out)
     * @return the unit at the specified index
     */
    public Unit getMapUnit(int i) {
        return this.units.get(i);
    }

    /**
     * return the quantization error, this container stores
     * 
     * @return the qe stored
     */
    @Override
    public double getQE() {
        return this.qe;
    }

    /**
     * sets the qe of this container to the given value
     * 
     * @param qe the qe to be storec
     */
    @Override
    public void setQE(double qe) {
        this.qe = qe;
    }

    /**
     * the number of input items having the qe stored by this object
     * 
     * @return the number of input items having this qe
     */
    public int getNumInputs() {
        return this.inputs.size();
    }

    /**
     * returns a string of format x-coord x y-coord of the unit at the specified index
     * 
     * @param index the index of the unit in the list of units stored in this object
     * @return a string formatted as described above
     */
    public String getUnitCoords(int index) {
        return this.units.get(index).getXPos() + "," + this.units.get(index).getYPos();
    }

    /**
     * returns the label for the input at the given index
     * 
     * @param index the index of the label in the list of labels hold by this object
     * @return the label of the input at the specified index
     */
    public String getInputLabel(int index) {
        return this.inputs.get(index).getLabel();
    }
}
