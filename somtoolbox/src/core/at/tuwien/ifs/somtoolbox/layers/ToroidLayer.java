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

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;

/**
 * Implementation of a toroid Self-Organizing Map layer, i.e. a "doughnut" shaped layer, whose left &amp; right and
 * upper &amp; lower edges are interconnected. This class mainly adjusts distance functions.
 * 
 * @author Rudolf Mayer
 * @version $Id: ToroidLayer.java 3583 2010-05-21 10:07:41Z mayer $
 */

public class ToroidLayer extends GrowingLayer {

    /** @see GrowingLayer#GrowingLayer(int, int, String, int, boolean, boolean, long, InputData) */
    public ToroidLayer(int xSize, int ySize, String metricName, int dim, boolean normalize, boolean usePCA, long seed,
            InputData data) {
        super(xSize, ySize, metricName, dim, normalize, usePCA, seed, data);
        initToroid();
    }

    /** @see GrowingLayer#GrowingLayer(int, int, int, String, int, boolean, boolean, long, InputData) */
    public ToroidLayer(int xSize, int ySize, int zSize, String metricName, int dim, boolean normalize, boolean usePCA,
            long seed, InputData data) {
        super(xSize, ySize, zSize, metricName, dim, normalize, usePCA, seed, data);
        initToroid();
    }

    /** @see GrowingLayer#GrowingLayer(int, Unit, int, int, String, int, boolean, boolean, long, InputData) */
    public ToroidLayer(int id, Unit su, int xSize, int ySize, String metricName, int dim, boolean normalize,
            boolean usePCA, long seed, InputData data) {
        super(id, su, xSize, ySize, metricName, dim, normalize, usePCA, seed, data);
        initToroid();
    }

    /** @see GrowingLayer#GrowingLayer(int, Unit, int, int, int, String, int, boolean, boolean, long, InputData) */
    public ToroidLayer(int id, Unit su, int xSize, int ySize, int zSize, String metricName, int dim, boolean normalize,
            boolean usePCA, long seed, InputData data) {
        super(id, su, xSize, ySize, metricName, dim, normalize, usePCA, seed, data);
        initToroid();
    }

    /** @see GrowingLayer#GrowingLayer(int, Unit, int, int, String, int, double[][][], long) */
    public ToroidLayer(int id, Unit su, int xSize, int ySize, String metricName, int dim, double[][][] vectors,
            long seed) throws SOMToolboxException {
        super(id, su, xSize, ySize, metricName, dim, vectors, seed);
        initToroid();
    }

    /** @see GrowingLayer#GrowingLayer(int, Unit, int, int, int, String, int, double[][][][], long) */
    public ToroidLayer(int id, Unit su, int xSize, int ySize, int zSize, String metricName, int dim,
            double[][][][] vectors, long seed) throws SOMToolboxException {
        super(id, su, xSize, ySize, zSize, metricName, dim, vectors, seed);
        initToroid();
    }

    private void initToroid() {
        gridTopology = GridTopology.toroid;
    }

    /** Toroid distance on the map */
    @Override
    public double getMapDistance(int x1, int y1, int x2, int y2) {
        int distX = Math.min(Math.abs(x1 - x2), xSize - Math.abs(x1 - x2));
        int distY = Math.min(Math.abs(y1 - y2), ySize - Math.abs(y1 - y2));
        return Math.sqrt(distX * distX + distY * distY);
    }

    @Override
    public double getMapDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        int distX = Math.min(Math.abs(x1 - x2), xSize - Math.abs(x1 - x2));
        int distY = Math.min(Math.abs(y1 - y2), ySize - Math.abs(y1 - y2));
        int distZ = Math.min(Math.abs(z1 - z2), zSize - Math.abs(z1 - z2));
        return Math.sqrt(distX * distX + distY * distY + distZ * distZ);
    }

    @Override
    public double getMapDistanceSq(int x1, int y1, int z1, int x2, int y2, int z2) {
        int distX = Math.min(Math.abs(x1 - x2), xSize - Math.abs(x1 - x2));
        int distY = Math.min(Math.abs(y1 - y2), ySize - Math.abs(y1 - y2));
        int distZ = Math.min(Math.abs(z1 - z2), zSize - Math.abs(z1 - z2));
        return distX * distX + distY * distY + distZ * distZ;
    }

    /** On a toroid map each unit has a neighbour */
    @Override
    public boolean hasNeighbours(int x, int y) throws LayerAccessException {
        return true;
    }

    @Override
    protected ArrayList<Unit> getNeighbouringUnits(Unit u) throws LayerAccessException {
        int x = u.getXPos();
        int y = u.getYPos();
        int z = u.getZPos();
        ArrayList<Unit> neighbourUnits = new ArrayList<Unit>();

        if (x > 0) {
            neighbourUnits.add(getUnit(x - 1, y, z));
        } else {
            neighbourUnits.add(getUnit(xSize - 1, y, z));
        }
        if (x + 1 < getXSize()) {
            neighbourUnits.add(getUnit(x + 1, y, z));
        } else {
            neighbourUnits.add(getUnit(0, y, z));
        }

        if (y > 0) {
            neighbourUnits.add(getUnit(x, y - 1, z));
        } else {
            neighbourUnits.add(getUnit(x, ySize - 1, z));
        }
        if (y + 1 < getYSize()) {
            neighbourUnits.add(getUnit(x, y + 1, z));
        } else {
            neighbourUnits.add(getUnit(x, 0, z));
        }

        if (z > 0) {
            neighbourUnits.add(getUnit(x, y, z - 1));
        } else {
            neighbourUnits.add(getUnit(x, y, zSize - 1));
        }
        if (z + 1 < getYSize()) {
            neighbourUnits.add(getUnit(x, y, z + 1));
        } else {
            neighbourUnits.add(getUnit(x, y, 0));
        }

        return neighbourUnits;
    }

    @Override
    protected ArrayList<Unit> getNeighbouringUnits(Unit u, double radius) throws LayerAccessException {
        // FIXME: implement this :-)
        throw new IllegalArgumentException("Not implemented");
    }

}