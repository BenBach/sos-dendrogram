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

import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;

/**
 * Interface for map layers.<br>
 * TODO: Currently, this is rather limited to rectangular layer structures.
 * 
 * @author Michael Dittenbach
 * @version $Id: Layer.java 3583 2010-05-21 10:07:41Z mayer $
 */
public interface Layer {

    public enum GridLayout {
        triangular, rectangular("rect"), hexagonal("hex");
        private String somPakName;

        private GridLayout() {
            this.somPakName = this.toString();
        }

        private GridLayout(String somPakName) {
            this.somPakName = somPakName;
        }

        public String getSomPakName() {
            return somPakName;
        }
    }

    public enum GridTopology {
        planar, cylindrical, toroid, spherical
    }

    /**
     * Returns a component plane of the component specified by the argument <code>component</code>. Returns
     * <code>null</code>, if the argument <code>component</code> is out of range.
     * 
     * @param component the index of the component.
     * @return a double matrix containing the values of the weight vectors' respective components, or <code>null</code>
     *         if argument <code>component</code> is invalid.
     */
    public double[][] getComponentPlane(int component);

    /**
     * Returns a component plane of the component specified by the argument <code>component</code>. Returns
     * <code>null</code>, if the argument <code>component</code> is out of range.
     * 
     * @param component the index of the component.
     * @param z TODO
     * @return a double matrix containing the values of the weight vectors' respective components, or <code>null</code>
     *         if argument <code>component</code> is invalid.
     */
    public double[][] getComponentPlane(int component, int z);

    /**
     * Returns the identification string of the map layer.
     * 
     * @return the identification string of the map layer.
     */
    public String getIdString();

    /**
     * Returns the level of the map layer in a hierarchical model.
     * 
     * @return the level of the map layer in a hierarchical model.
     */
    public int getLevel();

    /**
     * Returns the distance between two units on the map grid.
     * 
     * @param x1 the horizontal position of the first unit.
     * @param y1 the vertical position of the first unit.
     * @param z1 the height position of the first unit.
     * @param x2 the horizontal position of the second unit.
     * @param y2 the vertical position of the second unit.
     * @param z2 the height position of the second unit.
     * @return the distance between two units.
     */
    public double getMapDistance(int x1, int y1, int z1, int x2, int y2, int z2);

    /**
     * Returns the distance between two units on the map grid.
     * 
     * @param u1 the first unit.
     * @param u2 the second unit.
     * @return the distance between two units.
     */
    public double getMapDistance(Unit u1, Unit u2);

    /**
     * Returns the metric used for distance calculation.
     * 
     * @return the metric used for distance calculation.
     */
    public DistanceMetric getMetric();

    /**
     * Returns the quality information.
     * 
     * @return the quality information, or <code>null</code> if not existent.
     */
    public QualityMeasure getQualityMeasure();

    /**
     * Returns the revision string of this layer. This string should be written to output files to be able to trace back
     * the implementation revision based on the CVS revision a map was trained with.
     * 
     * @return the revision string of this layer.
     */
    public String getRevision();

    /**
     * Returns the <code>Unit</code> at the position specified by the <code>x</code> and <code>y</code> arguments. A
     * <code>LayerAccessException</code> is thrown, if the coordinates are invalid.
     * 
     * @param x the horizontal position on the map layer.
     * @param y the vertical position on the map layer.
     * @return the unit at the specified position.
     * @throws LayerAccessException if the coordinates are out of range.
     */
    public Unit getUnit(int x, int y) throws LayerAccessException;

    /**
     * Returns the <code>Unit</code> at the position specified by the <code>x</code> and <code>y</code> arguments. A
     * <code>LayerAccessException</code> is thrown, if the coordinates are invalid.
     * 
     * @param x the horizontal position on the map layer.
     * @param y the vertical position on the map layer.
     * @param z TODO
     * @return the unit at the specified position.
     * @throws LayerAccessException if the coordinates are out of range.
     */
    public Unit getUnit(int x, int y, int z) throws LayerAccessException;

    /**
     * Returns the unit onto which the datum specified by its name by argument <code>name</code> is mapped.
     * 
     * @param name the name of the input datum to be searched for.
     * @return the unit onto which the datum is mapped, or <code>null</code> if the datum is not found on the map.
     */
    public Unit getUnitForDatum(String name);

    /**
     * Returns the width of the map layer.
     * 
     * @return the width of the map layer.
     */
    public int getXSize();

    /**
     * Returns the height of the map layer.
     * 
     * @return the height of the map layer.
     */
    public int getYSize();

    /**
     * Returns the depth of the map layer.
     * 
     * @return the depth of the map layer.
     */
    public int getZSize();

    /** Returns the total number of Inputs mapped on all units in the map layer. */
    public int getNumberOfMappedInputs();

    /** Returns an array of all units in the map layer; the specific order of the units is unspecified. */
    public Unit[] getAllUnits();

    public GridTopology getGridTopology();

    public GridLayout getGridLayout();
}
