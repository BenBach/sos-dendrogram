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
package at.tuwien.ifs.somtoolbox.layers;

import java.util.LinkedList;
import java.util.List;

import at.tuwien.ifs.somtoolbox.util.growingCellStructures.GrowingCellTetraheder;

/**
 * Extension of Unit, needed to save additional data of growing cell structures
 * 
 * @author Johannes Inführ
 * @author Andreas Zweng
 * @version $Id: GrowingCellUnit.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class GrowingCellUnit extends Unit {
    /** The Tetraheders this Unit is connected to */
    private List<GrowingCellTetraheder> connectedTetraheders;

    /** The signal counter of this unit */
    private double signalCounter;

    /** The estimate of space covered by this unit */
    private double voronoiEstimate;

    // displayparameters
    private int diameter = 10;

    /** Position of Unit in Displayspace X */
    private double posX;

    /** Position of Unit in Displayspace Y */
    private double posY;

    public double getSignalCounter() {
        return signalCounter;
    }

    public void setSignalCounter(double d) {
        this.signalCounter = d;
    }

    /**
     * Std Constructor, initializes the unit wich weights
     * 
     * @param layer The layer on which this unit resides
     * @param weights The weight vector of this unit
     */
    public GrowingCellUnit(GrowingCellLayer layer, double[] weights) {
        super(layer, -1, -1, weights);

        connectedTetraheders = new LinkedList<GrowingCellTetraheder>();
        signalCounter = 0;
        voronoiEstimate = 0;
    }

    public double getVoronoiEstimate() {
        return voronoiEstimate;
    }

    public void setVoronoiEstimate(double voronoiEstimate) {
        this.voronoiEstimate = voronoiEstimate;
    }

    /** Connects this unit to Tetraheder ct */
    public void connect(GrowingCellTetraheder ct) {
        connectedTetraheders.add(ct);
    }

    /**
     * @return Tetraheders this unit is connected to
     */
    public List<GrowingCellTetraheder> getConnectedTetraheders() {
        return connectedTetraheders;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public String toString() {
        // return "cu: no of tets: "+connectedTetraheders.size()+" sigCount: "+signalCounter+" ("+posX+","+posY+")";
        return "unit pos: (" + posX + "," + posY + ")";
    }

    /** * Disconnects the Unit from tetraheder t */
    public void disconnect(GrowingCellTetraheder t) {
        connectedTetraheders.remove(t);
    }

    /** Puts the Unit at Position (x,y) in Display-Space */
    public void putAtPosition(double x, double y) {
        posX = x;
        posY = y;
    }

    /**
     * @return X-Coordinate of Unit in Display-Space
     */
    public double getX() {
        return posX;
    }

    /**
     * @return Y-Coordinate of Unit in Display-Space
     */
    public double getY() {
        return posY;
    }

    @Override
    public int getXPos() {
        return (int) getX();
    }

    @Override
    public int getYPos() {
        return (int) getY();
    }

    /**
     * @return Diameter of Unit (for physics simulation)
     */
    public int getDiameter() {
        return diameter;
    }

    /**
     * @param deltax Movement along x-axis
     * @param deltay Movement along y-axis
     */
    public void applyMovement(double deltax, double deltay) {
        posX += deltax;
        posY += deltay;
    }

    @Override
    public GrowingCellUnit clone() {
        GrowingCellUnit u = new GrowingCellUnit((GrowingCellLayer) getLayer(), getWeightVector());
        u.posX = posX;
        u.posY = posY;

        return u;
    }

}
