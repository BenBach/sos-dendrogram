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
package at.tuwien.ifs.somtoolbox.input;

import at.tuwien.ifs.somtoolbox.layers.Layer.GridLayout;
import at.tuwien.ifs.somtoolbox.layers.Layer.GridTopology;

/**
 * Interface for classes providing the functionality of reading network models from file.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: SOMInputReader.java 3583 2010-05-21 10:07:41Z mayer $
 */
public interface SOMInputReader {

    public static final String UNIT_FILE = "Unit File";

    public static final String WEIGHT_VECTOR = "Weight Vector File";

    public static final String MAP_FILE = "Map File";

    /**
     * Returns the dimensionality of the weight vectors.
     * 
     * @return the dimensionality of the weight vectors.
     */
    public int getDim();

    /**
     * Returns the name of the path where the files are located.
     * 
     * @return the name of the path where the files are located.
     */
    public String getFilePath();

    /**
     * Returns the name of the map description file, or null if not loaded.
     * 
     * @return the name of the map description file, or null if not loaded.
     */
    public String getMapDescriptionFileName();

    /**
     * Returns an array of strings containing the names of vectors mapped onto a certain unit specified by coordinates
     * <code>x</code> and <code>y</code>. If the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code>
     * will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return an array of strings containing the names of vectors mapped onto a certain unit specified by coordinates
     *         <code>x</code> and <code>y</code>.
     */
    public String[] getMappedVecs(int x, int y);

    /**
     * Returns an array of strings containing the names of vectors mapped onto a certain unit specified by coordinates
     * <code>x</code> and <code>y</code>. If the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code>
     * will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return an array of strings containing the names of vectors mapped onto a certain unit specified by coordinates
     *         <code>x</code> and <code>y</code>.
     */
    public String[] getMappedVecs(int x, int y, int z);

    /**
     * Returns an array of double values containing the distances between mapped vectors and the unit specified by
     * coordinates <code>x</code> and <code>y</code>. If the coordinates are invalid, an
     * <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return an array of double values containing the distances between mapped vectors and the unit specified by
     *         coordinates <code>x</code> and <code>y</code>.
     */
    public double[] getMappedVecsDist(int x, int y);

    /**
     * Returns an array of double values containing the distances between mapped vectors and the unit specified by
     * coordinates <code>x</code> and <code>y</code>. If the coordinates are invalid, an
     * <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return an array of double values containing the distances between mapped vectors and the unit specified by
     *         coordinates <code>x</code> and <code>y</code>.
     */
    public double[] getMappedVecsDist(int x, int y, int z);

    /**
     * Returns the name of the metric used for distance calculation.
     * 
     * @return the name of the metric used for distance calculation.
     */
    public String getMetricName();

    /**
     * Returns the number of subordinate maps of a certain unit specified by coordinates <code>x</code> and
     * <code>y</code>. If the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown.
     * The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return the number of subordinate maps of a certain unit specified by coordinates <code>x</code> and
     *         <code>y</code>.
     */
    public int getNrSomsMapped(int x, int y);

    /**
     * Returns the number of subordinate maps of a certain unit specified by coordinates <code>x</code> and
     * <code>y</code>. If the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown.
     * The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return the number of subordinate maps of a certain unit specified by coordinates <code>x</code> and
     *         <code>y</code>.
     */
    public int getNrSomsMapped(int x, int y, int z);

    /**
     * Returns the number of labels of a certain unit specified by coordinates <code>x</code> and <code>y</code>. If the
     * coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is
     * not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return the number of labels of a certain unit specified by coordinates <code>x</code> and <code>y</code>.
     */
    public int getNrUnitLabels(int x, int y);

    /**
     * Returns the number of labels of a certain unit specified by coordinates <code>x</code> and <code>y</code>. If the
     * coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is
     * not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return the number of labels of a certain unit specified by coordinates <code>x</code> and <code>y</code>.
     */
    public int getNrUnitLabels(int x, int y, int z);

    public int getNrKaskiGateLabels(int x, int y);

    public int getNrKaskiGateLabels(int x, int y, int z);

    public int getNrKaskiLabels(int x, int y);

    public int getNrKaskiLabels(int x, int y, int z);

    public int getNrContextGateLabels(int x, int y);

    public int getNrContextGateLabels(int x, int y, int z);

    public int getNrKaskiWeights(int x, int y);

    public int getNrKaskiWeights(int x, int y, int z);

    public int getNrGateWeights(int x, int y);

    public int getNrGateWeights(int x, int y, int z);

    public int getNrBestContext(int x, int y);

    public int getNrBestContext(int x, int y, int z);

    /**
     * Returns the number of vectors mapped onto a certain unit specified by coordinates <code>x</code> and
     * <code>y</code>. If the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown.
     * The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return the number of vectors mapped onto a certain unit specified by coordinates <code>x</code> and
     *         <code>y</code>.
     */
    public int getNrVecMapped(int x, int y);

    /**
     * Returns the number of vectors mapped onto a certain unit specified by coordinates <code>x</code> and
     * <code>y</code>. If the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown.
     * The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return the number of vectors mapped onto a certain unit specified by coordinates <code>x</code> and
     *         <code>y</code>.
     */
    public int getNrVecMapped(int x, int y, int z);

    /**
     * Returns the quantization error of a certain unit specified by coordinates <code>x</code> and <code>y</code>. If
     * the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function
     * is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return the quantization error of a certain unit specified by coordinates <code>x</code> and <code>y</code>.
     */
    public double getQuantErrorUnit(int x, int y);

    /**
     * Returns the quantization error of a certain unit specified by coordinates <code>x</code> and <code>y</code>. If
     * the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function
     * is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return the quantization error of a certain unit specified by coordinates <code>x</code> and <code>y</code>.
     */
    public double getQuantErrorUnit(int x, int y, int z);

    /**
     * Returns the mean quantization error of a certain unit specified by coordinates <code>x</code> and <code>y</code>.
     * If the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling
     * function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return the mean quantization error of a certain unit specified by coordinates <code>x</code> and <code>y</code>.
     */
    public double getQuantErrorUnitAvg(int x, int y);

    /**
     * Returns the mean quantization error of a certain unit specified by coordinates <code>x</code> and <code>y</code>.
     * If the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling
     * function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return the mean quantization error of a certain unit specified by coordinates <code>x</code> and <code>y</code>.
     */
    public double getQuantErrorUnitAvg(int x, int y, int z);

    /**
     * Returns the name of the unit description file, or <code>null</code> if not loaded.
     * 
     * @return the name of the unit description file, or <code>null</code> if not loaded.
     */
    public String getUnitDescriptionFileName();

    /**
     * Returns an array of strings containing the names of labels of a certain unit specified by coordinates
     * <code>x</code> and <code>y</code>. If the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code>
     * will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return an array of strings containing the names of labels of a certain unit specified by coordinates
     *         <code>x</code> and <code>y</code>.
     */
    public String[] getUnitLabels(int x, int y);

    /**
     * Returns an array of strings containing the names of labels of a certain unit specified by coordinates
     * <code>x</code> and <code>y</code>. If the coordinates are invalid, an <code>ArrayIndexOutOfBoundsException</code>
     * will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return an array of strings containing the names of labels of a certain unit specified by coordinates
     *         <code>x</code> and <code>y</code>.
     */
    public String[] getUnitLabels(int x, int y, int z);

    public String[] getKaskiGateUnitLabels(int x, int y);

    public String[] getKaskiGateUnitLabels(int x, int y, int z);

    public String[] getContextGateUnitLabels(int x, int y);

    public String[] getContextGateUnitLabels(int x, int y, int z);

    public String[] getKaskiUnitLabels(int x, int y);

    public String[] getKaskiUnitLabels(int x, int y, int z);

    public String[] getBestContextUnitLabels(int x, int y);

    public String[] getBestContextUnitLabels(int x, int y, int z);

    /**
     * Returns an array of double values containing the quantization error of the labels of a unit specified by
     * coordinates <code>x</code> and <code>y</code>. If the coordinates are invalid, an
     * <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return an array of double values containing the quantization error of the labels of a unit specified by
     *         coordinates <code>x</code> and <code>y</code>.
     */
    public double[] getUnitLabelsQe(int x, int y);

    /**
     * Returns an array of double values containing the quantization error of the labels of a unit specified by
     * coordinates <code>x</code> and <code>y</code>. If the coordinates are invalid, an
     * <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return an array of double values containing the quantization error of the labels of a unit specified by
     *         coordinates <code>x</code> and <code>y</code>.
     */
    public double[] getUnitLabelsQe(int x, int y, int z);

    /**
     * Returns an array of double values containing the mean values of the labels of a unit specified by coordinates
     * <code>x</code> and <code>y</code> . If the coordinates are invalid, an
     * <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return an array of double values containing the mean values of the labels of a unit specified by coordinates
     *         <code>x</code> and <code>y</code> .
     */
    public double[] getUnitLabelsWgt(int x, int y);

    /**
     * Returns an array of double values containing the mean values of the labels of a unit specified by coordinates
     * <code>x</code> and <code>y</code> . If the coordinates are invalid, an
     * <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return an array of double values containing the mean values of the labels of a unit specified by coordinates
     *         <code>x</code> and <code>y</code> .
     */
    public double[] getUnitLabelsWgt(int x, int y, int z);

    public double[] getKaskiUnitLabelsWgt(int x, int y);

    public double[] getKaskiUnitLabelsWgt(int x, int y, int z);

    public double[] getKaskiGateUnitLabelsWgt(int x, int y);

    public double[] getKaskiGateUnitLabelsWgt(int x, int y, int z);

    /**
     * Returns an array of strings containing the file names of subordinate maps of a certain unit specified by
     * coordinates <code>x</code> and <code>y</code>. If the coordinates are invalid, an
     * <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @return an array of strings containing the file names of subordinate maps of a certain unit specified by
     *         coordinates <code>x</code> and <code>y</code>.
     */
    public String[] getUrlMappedSoms(int x, int y);

    /**
     * Returns an array of strings containing the file names of subordinate maps of a certain unit specified by
     * coordinates <code>x</code> and <code>y</code>. If the coordinates are invalid, an
     * <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to catch it.
     * 
     * @param x horizontal position of the unit on the map.
     * @param y vertical position of the unit on the map.
     * @param z TODO
     * @return an array of strings containing the file names of subordinate maps of a certain unit specified by
     *         coordinates <code>x</code> and <code>y</code>.
     */
    public String[] getUrlMappedSoms(int x, int y, int z);

    /**
     * Returns a 4-dimensional double array of the weight vectors. The first dimension represents units of a map in
     * horizontal direction, the second dimension represents the units of a map in vertical direction and the third
     * dimension represents units of a map in depth.
     * 
     * @return a 4-dimensional double array of the weight vectors.
     */
    public double[][][][] getVectors();

    /**
     * Returns the name of the weight vector file, or <code>null</code> if not loaded.
     * 
     * @return the name of the weight vector file, or <code>null</code> if not loaded.
     */
    public String getWeightVectorFileName();

    /**
     * Returns the number of units in horizontal direction.
     * 
     * @return the number of units in horizontal direction.
     */
    public int getXSize();

    /**
     * Returns the number of units in vertical direction.
     * 
     * @return the number of units in vertical direction.
     */
    public int getYSize();

    /**
     * Returns the number of units in depth.
     * 
     * @return the number of units in depth.
     */
    public int getZSize();

    public GridTopology getGridTopology();

    public GridLayout getGridLayout();

    /**
     * Returns the longest substring that is common for all input data items. Use this to improve the visual display of
     * vector labels.
     */
    public String getCommonVectorLabelPrefix();

    /** Returns whether the SOM is labelled. */
    public boolean isLabelled();

}
