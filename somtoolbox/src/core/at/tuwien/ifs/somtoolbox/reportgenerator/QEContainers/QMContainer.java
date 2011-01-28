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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.reportgenerator.SemanticClass;
import at.tuwien.ifs.somtoolbox.reportgenerator.SemanticInterpreterGrid;
import at.tuwien.ifs.somtoolbox.reportgenerator.SemanticNode;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;

/**
 * @author Martin Waitzbauer (0226025)
 * @version $Id: QMContainer.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class QMContainer {

    HashMap<String, double[][]> UnitQualities;

    HashMap<String, Double> UnitMAXQualities;

    HashMap<String, Double> UnitMINQualities;

    HashMap<String, Double> MapQualities;

    HashMap<String, int[][]> UnitQualitiesClassified;

    public ArrayList<String> UnitQualityMeasureNames;

    SemanticInterpreterGrid sGrid;

    private ArrayList<String> damagedcriteriaQM = new ArrayList<String>();;

    private HashMap<Integer, String> QualityIdentifier = new HashMap<Integer, String>();

    TestRunResult run;

    public static int NUMBER_OF_CLASSES = 5;

    public QMContainer(TestRunResult run) {
        UnitQualities = new HashMap<String, double[][]>();
        MapQualities = new HashMap<String, Double>();
        UnitQualitiesClassified = new HashMap<String, int[][]>();
        UnitMAXQualities = new HashMap<String, Double>();
        UnitMINQualities = new HashMap<String, Double>();
        UnitQualityMeasureNames = new ArrayList<String>();
        this.run = run;
        writeQualityIdentifier();
    }

    public void putUnitQualities(String Name, double[][] units) {
        this.UnitQualities.put(Name, units);
        if (!this.UnitQualityMeasureNames.contains(Name)) {
            this.UnitQualityMeasureNames.add(Name);
        }
    }

    public double[][] getUnitQualities(String Name) {
        return UnitQualities.get(Name);
    }

    public void putMapQualities(String Name, double measure) {
        this.MapQualities.put(Name, measure);
    }

    /**
     * returns the maximal Value of the quality measure with "Name"as identifier
     * 
     * @return an Unit Array storing the Unit(s) with the biggest Value
     * @param Area null if whole grid is admitted, != null ifi want a Part only
     */
    public Unit[] getMaxUnit(String Name, Unit[] Area) {
        double[][] errors;
        double maxError;
        Vector<Integer> argmaxx = new Vector<Integer>();
        Vector<Integer> argmaxy = new Vector<Integer>();
        Unit[] units = null;
        errors = this.UnitQualities.get(Name);
        maxError = Double.MIN_VALUE;
        argmaxx.add(new Integer(0));
        argmaxy.add(new Integer(0));

        if (errors != null) {
            try {
                if (Area != null) {
                    for (Unit u : Area) {
                        if (errors[u.getXPos()][u.getYPos()] > maxError
                                && run.getGrowingSOM().getLayer().hasMappedInput(u.getXPos(), u.getYPos())) {
                            // new maximum
                            maxError = errors[u.getXPos()][u.getYPos()];
                            argmaxx.removeAllElements();
                            argmaxy.removeAllElements();
                            argmaxx.add(new Integer(u.getXPos()));
                            argmaxy.add(new Integer(u.getYPos()));
                        } else if (errors[u.getXPos()][u.getYPos()] == maxError
                                && run.getGrowingSOM().getLayer().hasMappedInput(u.getXPos(), u.getYPos())) {
                            // additional minimum
                            argmaxx.add(new Integer(u.getXPos()));
                            argmaxy.add(new Integer(u.getYPos()));
                        }
                    }
                } else {
                    for (int i = 0; i < errors.length; i++) {
                        for (int j = 0; j < errors[i].length; j++) {
                            // look whether the te is smaller
                            if (errors[i][j] > maxError && run.getGrowingSOM().getLayer().hasMappedInput(i, j)) {
                                // new maximum
                                maxError = errors[i][j];
                                argmaxx.removeAllElements();
                                argmaxy.removeAllElements();
                                argmaxx.add(new Integer(i));
                                argmaxy.add(new Integer(j));
                            } else if (errors[i][j] == maxError && run.getGrowingSOM().getLayer().hasMappedInput(i, j)) {
                                // additional minimum
                                argmaxx.add(new Integer(i));
                                argmaxy.add(new Integer(j));
                            }
                        }
                    }
                    this.UnitMAXQualities.put(Name, maxError);
                }
                units = new Unit[argmaxx.size()];
                for (int i = 0; i < argmaxx.size(); i++) {
                    units[i] = this.run.getGrowingSOM().getLayer().getUnit(argmaxx.get(i).intValue(),
                            argmaxy.get(i).intValue());
                }
                return units;
            } catch (LayerAccessException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                        "Cannot calculate max value of units on SOM from run " + this.run.getRunId() + ". Reason: " + e);
                return null;
            }
        } else {
            return null;
        }

    }

    /**
     * returns the minimal Value of the quality measure with "Name"as identifier
     * 
     * @return an Unit Array storing the Unit(s) with the smallest Value
     * @param Area != null if i want area selection, null if whole grid
     */
    public Unit[] getMinUnit(String Name, Unit[] Area) {

        double[][] errors;
        double minError;
        Vector<Integer> argmaxx = new Vector<Integer>();
        Vector<Integer> argmaxy = new Vector<Integer>();
        Unit[] units = null;
        errors = this.UnitQualities.get(Name);
        minError = Double.MAX_VALUE;
        argmaxx.add(new Integer(0));
        argmaxy.add(new Integer(0));

        if (errors != null) {
            try {
                if (Area != null) {
                    for (Unit u : Area) {
                        if (errors[u.getXPos()][u.getYPos()] != 0 && errors[u.getXPos()][u.getYPos()] < minError
                                && run.getGrowingSOM().getLayer().hasMappedInput(u.getXPos(), u.getYPos())) {
                            // new minimium
                            minError = errors[u.getXPos()][u.getYPos()];
                            argmaxx.removeAllElements();
                            argmaxy.removeAllElements();
                            argmaxx.add(new Integer(u.getXPos()));
                            argmaxy.add(new Integer(u.getYPos()));
                        } else if (errors[u.getXPos()][u.getYPos()] == minError
                                && run.getGrowingSOM().getLayer().hasMappedInput(u.getXPos(), u.getYPos())) {
                            // additional minimum
                            argmaxx.add(new Integer(u.getXPos()));
                            argmaxy.add(new Integer(u.getYPos()));
                        }
                    }
                } else {
                    for (int i = 0; i < errors.length; i++) {
                        for (int j = 0; j < errors[i].length; j++) {
                            // look whether the te is smaller
                            if (errors[i][j] != 0 && errors[i][j] < minError
                                    && run.getGrowingSOM().getLayer().hasMappedInput(i, j)) {
                                // new maximium
                                minError = errors[i][j];
                                argmaxx.removeAllElements();
                                argmaxy.removeAllElements();
                                argmaxx.add(new Integer(i));
                                argmaxy.add(new Integer(j));
                            } else if (errors[i][j] == minError && run.getGrowingSOM().getLayer().hasMappedInput(i, j)) {
                                // additional minimum
                                argmaxx.add(new Integer(i));
                                argmaxy.add(new Integer(j));
                            }
                        }
                    }
                    this.UnitMINQualities.put(Name, minError);
                }

                units = new Unit[argmaxx.size()];
                for (int i = 0; i < argmaxx.size(); i++) {
                    units[i] = this.run.getGrowingSOM().getLayer().getUnit(argmaxx.get(i).intValue(),
                            argmaxy.get(i).intValue());
                }
                return units;

            } catch (LayerAccessException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                        "Cannot calculate min value of units on SOM from run " + this.run.getRunId() + ". Reason: " + e);
                return null;
            }
        }
        return null;

    }

    /** Puts all the Values of the QM identified with "Name" in "Classes" 1-5 (strong - very weak) */
    public void classifyUnits(String Name) {
        // int z = 77;
        // if (Name.equals("Silhouette Value")) {
        // z = 7;
        // }
        // Unit[] minU = this.getMinUnit(Name, null);
        // Unit[] maxU = this.getMaxUnit(Name, null);
        double[][] currQual = this.UnitQualities.get(Name);
        int[][] currclassQual = new int[currQual.length][currQual[0].length];
        double min = -1, max = -1;

        if (this.UnitMINQualities.containsKey(Name)) {
            min = this.UnitMINQualities.get(Name);
        }

        if (this.UnitMAXQualities.containsKey(Name)) {
            max = this.UnitMAXQualities.get(Name);
        }
        // double span = max - min;
        for (int i = 0; i < currQual.length; i++) {
            for (int j = 0; j < currQual[i].length; j++) {
                int k = 0;
                /*
                 * while(!((min+kstep) < currQual[i][j] && (min+(k+1)step > currQual[i][j])) ){ // Look in wich class my Unit is k++; }
                 */
                // double v = currQual[i][j];
                if (run.getGrowingSOM().getLayer().hasMappedInput(i, j)) {
                    k = (int) Math.round((currQual[i][j] - min) / (max - min) * 4) + 1;
                }

                currclassQual[i][j] = k;
            }
        }
        this.UnitQualitiesClassified.put(Name, currclassQual);
    }

    /** Returns the int [] of classified units for identifier Name */
    public int[][] getClassifiedUnits(String Name) {
        return this.UnitQualitiesClassified.get(Name);
    }

    /** Computes the number of classified units for the given class and identifier */
    public int getNumberOfClassifiedUnits(String Name, int clss) {
        int[][] values = this.UnitQualitiesClassified.get(Name);
        int counter = 0;
        for (int[] value : values) {
            for (int element : value) {
                if (element == clss) {
                    counter++;
                }
            }
        }
        return counter++;
    }

    /**
     * NOT USEDCURRENTLY returns the average distance from all units, classified with "clss" from QM "Name"
     * 
     * @param Name
     * @return
     */
    /*
     * public double getAverageDistance(String Name, int clss){ ArrayList temp = new ArrayList(); double out = 0.0 ; int [][] values = (int[][])
     * this.UnitQualitiesClassified.get(Name); for(int i = 0; i < values.length;i++){ for(int j = 0; j < values[i].length;j++){ if(values[i][j] ==
     * clss){ int [] tempcoords = new int[2]; tempcoords[0]=i; tempcoords[1]=j; temp.add(tempcoords); } } } return out; }
     */

    /** Returns the map qualities of the given quality measure */
    public Double getMapQualities(String Name) {
        return this.MapQualities.get(Name);
    }

    /**
     * creates an image visualizing the Quality Measure "Name" of this SOM the image is saved under the given path and
     * name, although a prefix run_runId_ is added to the image name
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created)
     * @param filename the name under which the image (together with the prefix run_[runId]_ ) is saved
     * @return true if everything worked fine, false if there was any problem. False does neither assure that the image
     *         exists, nor that is does not
     */
    public boolean createQualityMeasureImage(String outputDir, String filename, String Name) {
        boolean out = false;
        if (Name.equals("Quantization Error")) {
            out = this.run.createQuantizationErrorImage(outputDir, filename, 0);
        }
        if (Name.equals("Mean Quantization Error")) {
            out = this.run.createQuantizationErrorImage(outputDir, filename, 1);
        }
        if (Name.equals("Silhouette Value")) {
            out = this.run.createSilouetteImage(outputDir, filename, 0);
        }
        if (Name.equals("Topographic Error")) {
            out = this.run.createTopographicErrorImage(outputDir, filename);
        }
        if (Name.equals("Intrinisic Distance")) {
            out = this.run.createIntrinsicDistanceImage(outputDir, filename);
        }
        return out;
    }

    public double getUnitMAXQualities(String Name) {
        return this.UnitMAXQualities.get(Name);
    }

    public double getUnitMINQualities(String Name) {
        return this.UnitMINQualities.get(Name);
    }

    /**
     * Returns the string representation of the given int value<br/>
     * FIXME: refactor this!
     */
    public String getClassIdentifier(int c) {
        String out = "";
        switch (c) {
            case 5:
                out = "very high";
                break;
            case 4:
                out = "high";
                break;
            case 3:
                out = "intermediate";
                break;
            case 2:
                out = "low";
                break;
            case 1:
                out = "very low";
                break;
            default:
                out = "not such class known!";
        }
        return out;

    }

    /**
     * Returns the maximum of the selected classified Region
     * 
     * @param region null if whole Grid
     * @param Name QM Identifier
     * @return the maximum of the selected classified Region
     */
    public int getMaximumClassifiedRegionValue(Unit[] region, String Name) {
        int[][] classArray = this.UnitQualitiesClassified.get(Name);
        int max = Integer.MIN_VALUE;
        if (region != null) {
            for (Unit u : region) {
                if (classArray[u.getXPos()][u.getYPos()] > max & classArray[u.getXPos()][u.getYPos()] != 0) {
                    // 0 if unit has no Inputs
                    max = classArray[u.getXPos()][u.getYPos()];
                }
            }
        } else { // whole Grid
            for (int[] element : classArray) {
                for (int element2 : element) {
                    if (element2 > max) {
                        max = element2;
                    }
                }
            }
        }

        return max;
    }

    /**
     * Returns the minimum of the selected classified Region
     * 
     * @param region null if whole Grid
     * @param Name QM Identifier
     * @return the minimum of the selected classified Region
     */
    public int getMinimumClassifiedRegionValue(Unit[] region, String Name) {
        int[][] classArray = this.UnitQualitiesClassified.get(Name);
        int min = Integer.MAX_VALUE;
        if (region != null) {
            for (Unit u : region) {
                if (classArray[u.getXPos()][u.getYPos()] < min && classArray[u.getXPos()][u.getYPos()] != 0) {
                    // == 0 if unit has no Inputs
                    min = classArray[u.getXPos()][u.getYPos()];
                }
            }
        } else { // whole Grid
            for (int[] element : classArray) {
                for (int element2 : element) {
                    if (element2 < min) {
                        min = element2;
                    }
                }
            }
        }

        return min;

    }

    /**
     * Compares the 2 QM with each other,i.e looks through all units (only in the Region if region != NULL) and looks
     * for similar values at the same Units (TRY: Enlarge the search radius to a certain neighbourhood radius (1))
     * 
     * @param type 1 = Max-Max, 2 = Min-Min, 3 = Max-Min, 4 = Min-Max Compares according to Min /Max Values return a
     *            Array [0] gives the percentage of similarity, [1] gives the absolute difference in strength between
     *            max/min 1/2, [2] the region of intersection(1-9)
     */
    public double[] compareQualities(String Name1, String Name2, Unit[] Region, int type) {
        int[][] classArray1 = this.UnitQualitiesClassified.get(Name1);
        int[][] classArray2 = this.UnitQualitiesClassified.get(Name2);
        double[] out = new double[2];
        int minmax1 = 0, minmax2 = 0;
        int counter1 = 0, counter2 = 0;
        if (type == 1) { // Max-Max Report
            minmax1 = this.getMaximumClassifiedRegionValue(null, Name1);
            minmax2 = this.getMaximumClassifiedRegionValue(null, Name2);
        }
        if (type == 2) { // Min-Min Report
            minmax1 = this.getMinimumClassifiedRegionValue(null, Name1);
            minmax2 = this.getMinimumClassifiedRegionValue(null, Name2);
        }
        if (type == 3) { // Max-Min Report
            minmax1 = this.getMaximumClassifiedRegionValue(null, Name1);
            minmax2 = this.getMinimumClassifiedRegionValue(null, Name2);
        }
        if (type == 4) { // Min-Max Report
            minmax1 = this.getMinimumClassifiedRegionValue(null, Name1);
            minmax2 = this.getMaximumClassifiedRegionValue(null, Name2);
        }
        for (Unit u : Region) {
            // we have a wanted value
            if (classArray1[u.getXPos()][u.getYPos()] == minmax1
                    || classArray1[u.getXPos()][u.getYPos()] == minmax1 - 1
                    || classArray1[u.getXPos()][u.getYPos()] == minmax1 + 1) {
                counter1++;
                if (classArray2[u.getXPos()][u.getYPos()] == minmax2
                        || classArray2[u.getXPos()][u.getYPos()] == minmax2 - 1
                        || classArray2[u.getXPos()][u.getYPos()] == minmax2 + 1) {
                    counter2++;
                }
            }
        }
        out[0] = counter1 > 0 ? (double) counter2 / (double) counter1 : 0.0;
        out[1] = minmax1 - minmax2;
        return out;
    }

    /**
     * Returns an array of Units, witch have Intersection of QM Name1 & Name 2 with the given type of operation
     */
    public ArrayList<Unit> getComparedQMRegionOccurances(String Name1, String Name2, Unit[] Region, int type) {
        int[][] classArray1 = this.UnitQualitiesClassified.get(Name1);
        int[][] classArray2 = this.UnitQualitiesClassified.get(Name2);
        ArrayList<Unit> out = new ArrayList<Unit>();
        int minmax1 = 0, minmax2 = 0;
        int counter1 = 0;
        if (type == 1) { // Max-Max Report
            minmax1 = this.getMaximumClassifiedRegionValue(null, Name1);
            minmax2 = this.getMaximumClassifiedRegionValue(null, Name2);
        }
        if (type == 2) { // Min-Min Report
            minmax1 = this.getMinimumClassifiedRegionValue(null, Name1);
            minmax2 = this.getMinimumClassifiedRegionValue(null, Name2);
        }
        if (type == 3) { // Max-Min Report
            minmax1 = this.getMaximumClassifiedRegionValue(null, Name1);
            minmax2 = this.getMinimumClassifiedRegionValue(null, Name2);
        }
        if (type == 4) { // Min-Max Report
            minmax1 = this.getMinimumClassifiedRegionValue(null, Name1);
            minmax2 = this.getMaximumClassifiedRegionValue(null, Name2);
        }
        for (Unit u : Region) {
            // we have a wanted value
            if (classArray1[u.getXPos()][u.getYPos()] == minmax1
                    || classArray1[u.getXPos()][u.getYPos()] == minmax1 - 1
                    || classArray1[u.getXPos()][u.getYPos()] == minmax1 + 1) {
                counter1++;
                if (classArray2[u.getXPos()][u.getYPos()] == minmax2
                        || classArray2[u.getXPos()][u.getYPos()] == minmax2 - 1
                        || classArray2[u.getXPos()][u.getYPos()] == minmax2 + 1) {
                    out.add(u);
                }
            }
        }
        return out;
    }

    /**
     * Sets the SemanticGrid for calculations PLEASE NOTE: only the "Master- Grid can be set here" -> wich is the Grid
     * over the full Map, inluding all ClassReports
     */
    public void setsGrid(SemanticInterpreterGrid sGrid) {
        this.sGrid = sGrid;
    }

    /**
     * Returns an array with the original identification configuration , of how the qualifier QM should behave on
     * MAXIMUM / MINIMUM Value
     * 
     * @param qualifier the QM
     * @param type 1= max, 2 = Min
     * @return an Array of arraylists, built up as following: out[0] = high Density out[1] = low Density out[2] = mixed
     *         classes out[3] = one class out[4] = Units on Map edges out[5] = Units not on Map edges out[6] = Having
     *         neighboring empty units out[7] = having no neighboring empt unist out[8] = Units on Cluster edges out[9]
     *         = Units in Clusters out[10] = Big Distance of mapped Vectors to Prototype vector. out[11] = Small
     *         Distance of mapped Vectors to Prototype vector. out[12] = Big average Distance of mapped Vectors compared
     *         to each other. out[13] = Small average Distance of mapped Vectors compared to each other. out[14] = Input
     *         Vectors equally distributed over the Map out[15] = Input Vectors not equally distributed over the Map
     *         out[16] = Intra Cluster Distances == 0, Inter Cluster Distances != 0 out[17] = Intra Cluster Distances !=
     *         0, Inter Cluster Distances == 0
     */
    public QMConfigurationProfile getOriginalConfiguration(String qualifier, int type) {
        QMConfigurationProfile out_array = new QMConfigurationProfile(18);
        if (type == 1) { // return how the QM should be ideally in the original configuration when looked at MAXIMUM
            // Values
            if (qualifier.equals("Topographic Error")) {
                out_array.createNewElement(0);
                out_array.insert(0, 1.0);
                out_array.createNewElement(2);
                out_array.insert(2, 1.0);
                out_array.createNewElement(4);
                out_array.insert(4, 1.0);
                out_array.createNewElement(6);
                out_array.insert(6, 1.0);
                out_array.createNewElement(8);
                out_array.insert(8, 1.0);
            }
            if (qualifier.equals("Quantization Error")) {
                out_array.createNewElement(10);
                out_array.insert(10, 1.0);
            }
            if (qualifier.equals("Silhouette Value")) {
                out_array.createNewElement(16);
                out_array.insert(16, 1.0);
            }
            if (qualifier.equals("Mean Quantization Error")) {
                out_array.createNewElement(1);
                out_array.insert(1, 1.0);
                out_array.createNewElement(2);
                out_array.insert(2, 1.0);
                out_array.createNewElement(4);
                out_array.insert(4, 1.0);
                out_array.createNewElement(12);
                out_array.insert(12, 1.0);
            }
            if (qualifier.equals("Intrinsic Distance")) {
                // Mixture of Quantization Error & Topographic Error
                out_array.createNewElement(10);
                out_array.insert(10, 1.0);
                out_array.createNewElement(0);
                out_array.insert(0, 1.0);
                out_array.createNewElement(2);
                out_array.insert(2, 1.0);
                out_array.createNewElement(4);
                out_array.insert(4, 1.0);
                out_array.createNewElement(6);
                out_array.insert(6, 1.0);
                out_array.createNewElement(8);
                out_array.insert(8, 1.0);
            }
            if (qualifier.equals("Entropy Error")) {
                out_array.createNewElement(14);
                out_array.insert(14, 1.0);
            }
        }
        if (type == 2) { // return how the QM should be ideally in the original configuration when looked at MINIMUM
            // Values
            if (qualifier.equals("Topographic Error")) {
                out_array.createNewElement(1);
                out_array.insert(1, 1.0);
                out_array.createNewElement(3);
                out_array.insert(3, 1.0);
                out_array.createNewElement(5);
                out_array.insert(5, 1.0);
                out_array.createNewElement(7);
                out_array.insert(7, 1.0);
                out_array.createNewElement(9);
                out_array.insert(9, 1.0);
            }
            if (qualifier.equals("Quantization Error")) {
                out_array.createNewElement(11);
                out_array.insert(11, 1.0);
            }
            if (qualifier.equals("Mean Quantization Error")) {
                out_array.createNewElement(0);
                out_array.insert(0, 1.0);
                out_array.createNewElement(3);
                out_array.insert(3, 1.0);
                out_array.createNewElement(5);
                out_array.insert(5, 1.0);
                out_array.createNewElement(11);
                out_array.insert(11, 1.0);
            }
            if (qualifier.equals("Intrinsic Distance")) {
                // Mixture of Quantization Error & Topographic Error
                out_array.createNewElement(11);
                out_array.insert(11, 1.0);
                out_array.createNewElement(1);
                out_array.insert(1, 1.0);
                out_array.createNewElement(3);
                out_array.insert(3, 1.0);
                out_array.createNewElement(5);
                out_array.insert(5, 1.0);
                out_array.createNewElement(7);
                out_array.insert(7, 1.0);
                out_array.createNewElement(9);
                out_array.insert(9, 1.0);
            }
            if (qualifier.equals("Entropy Error")) {
                out_array.createNewElement(15);
                out_array.insert(15, 1.0);
            }
            if (qualifier.equals("Silhouette Value")) {
                out_array.createNewElement(17);
                out_array.insert(17, 1.0);
            }
        }

        if (out_array.isEmpty()) {
            return null;
        } else {
            return out_array;
        }
    }

    /**
     * Returns an array with the actual identification configuration , of how the qualifier QM behaves
     * 
     * @param qualifier the QM
     * @param units the units witch are tested
     * @param type 1 = MAX, 2 = MIN
     * @return an Array of arraylists, built up as following: out[0] = high Density out[1] = low Density out[2] = mixed
     *         classes out[3] = one class out[4] = Units on Map edges out[5] = Units not on Map edges out[6] = Having
     *         neighboring empty units out[7] = having no neighboring empt unist out[8] = Units on Cluster edges out[9]
     *         = Units in Clusters out[10] = Big Distance of mapped Vectors to Prototype vector. out[11] = Small
     *         Distance of mapped Vectors to Prototype vector. out[12] = Big average Distance of mapped Vectors compared
     *         to each other. out[13] = Small average Distance of mapped Vectors compared to each other. out[14] = Input
     *         Vectors equally distributed over the Map out[15] = Input Vectors not equally distributed over the Map
     *         out[16] = Intra Cluster Distances == 0, Inter Cluster Distances != 0 out[17] = Intra Cluster Distances !=
     *         0, Inter Cluster Distances == 0
     */
    public QMConfigurationProfile getActualConfiguration(String qualifier, Unit[] units, int type) {
        QMConfigurationProfile out_array = new QMConfigurationProfile(18);
        if (type == 1) { // WHat should be taken into account when looked at MAX properties
            if (qualifier.equals("Mean Quantization Error")) {
                out_array.createNewElement(1);
                out_array.createNewElement(2);
                out_array.createNewElement(4);
                out_array.createNewElement(12);
                out_array.insert(12, 1.0); // no need for further calculations

                for (Unit unit : units) { // Test all units
                    Boolean hasMixedClasses = this.hasMixedClasses(unit);
                    if (this.hasLowDensity(unit) == false) {
                        out_array.insert(1, unit);
                    }

                    if (this.isOnMapEdge(unit) == false) {
                        out_array.insert(4, unit);
                    }
                    if (hasMixedClasses == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(2))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(2));
                        }
                    } else {
                        if (hasMixedClasses == false) {
                            out_array.insert(2, unit);
                        }
                    }

                }
                String pLD_MQE = units.length - out_array.lengthOfElement(1) + " / " + units.length;// 1.0
                // -(double)out_array[1].size()/(double)units.length;
                out_array.insert(1, 0, pLD_MQE);
                String pLMC_MQE = units.length - out_array.lengthOfElement(2) + " / " + units.length;// 1.0 -
                // (double)out_array[2].size()/(double)units.length;
                out_array.insert(2, 0, pLMC_MQE);
                String pME_MQE = units.length - out_array.lengthOfElement(4) + " / " + units.length;// 1.0 -
                // (double)out_array[4].size()/(double)units.length;
                out_array.insert(4, 0, pME_MQE);
            }

            if (qualifier.equals("Topographic Error")) {
                out_array.createNewElement(0);
                out_array.createNewElement(2);
                out_array.createNewElement(4);
                out_array.createNewElement(6);
                out_array.createNewElement(8);

                for (Unit unit : units) { // Test all units
                    boolean HighDens = this.hasHighDensity(unit);
                    Boolean hasMixedClasses = this.hasMixedClasses(unit);
                    boolean isMapedge = this.isOnMapEdge(unit);
                    ArrayList<SemanticClass> isClusterEdge = this.isOnClusterEdge(unit);
                    Boolean neighborsempty = this.hasNeighboringEmptyUnits(unit);

                    if (HighDens == false) {
                        out_array.insert(0, unit);
                    }

                    if (hasMixedClasses == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(2))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(2));
                        }
                    } else {
                        if (hasMixedClasses == false) {
                            out_array.insert(2, unit);
                        }
                    }
                    if (isMapedge == false) {
                        out_array.insert(4, unit);
                    }

                    if (isClusterEdge == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(8))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(8));
                        }
                    } else {
                        if (isClusterEdge.size() == 0) {
                            out_array.insert(8, unit);
                        }
                    }

                    if (neighborsempty == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(6))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(6));
                        }
                    } else {
                        if (neighborsempty == false) {
                            out_array.insert(6, unit);
                        }
                    }

                }

                String pHD_TE = units.length - out_array.lengthOfElement(0) + " / " + units.length;// 1.0
                // -(double)out_array[1].size()/(double)units.length;
                out_array.insert(0, 0, pHD_TE);
                String pLMC_TE = units.length - out_array.lengthOfElement(2) + " / " + units.length;// 1.0 -
                // (double)out_array[2].size()/(double)units.length;
                out_array.insert(2, 0, pLMC_TE);
                String pME_TE = units.length - out_array.lengthOfElement(4) + " / " + units.length;// 1.0 -
                // (double)out_array[4].size()/(double)units.length;
                out_array.insert(4, 0, pME_TE);
                String pNE_TE = units.length - out_array.lengthOfElement(6) + " / " + units.length;
                out_array.insert(6, 0, pNE_TE);
                String pCE_TE = units.length - out_array.lengthOfElement(8) + " / " + units.length;
                out_array.insert(8, 0, pCE_TE);

            }
            if (qualifier.equals("Intrinsic Distance")) {
                // Pnly evaluate the TE Part
                out_array.createNewElement(0);
                out_array.createNewElement(2);
                out_array.createNewElement(4);
                out_array.createNewElement(6);
                out_array.createNewElement(8);

                for (Unit unit : units) { // Test all units
                    Boolean hasMixedClasses = this.hasMixedClasses(unit);
                    ArrayList<SemanticClass> isClusterEdge = this.isOnClusterEdge(unit);
                    Boolean neighborsempty = this.hasNeighboringEmptyUnits(unit);
                    if (this.hasHighDensity(unit) == false) {
                        out_array.insert(0, unit);
                    }

                    if (hasMixedClasses == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(2))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(2));
                        }
                    } else {
                        if (hasMixedClasses == false) {
                            out_array.insert(2, unit);
                        }
                    }
                    if (this.isOnMapEdge(unit) == false) {
                        out_array.insert(4, unit);
                    }

                    if (isClusterEdge == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(8))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(8));
                        }
                    } else {
                        if (isClusterEdge.size() == 0) {
                            out_array.insert(8, unit);
                        }
                    }

                    if (neighborsempty == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(6))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(6));
                        }
                    } else {
                        if (neighborsempty == false) {
                            out_array.insert(6, unit);
                        }
                    }
                }

                String pHD_TE = units.length - out_array.lengthOfElement(0) + " / " + units.length;// 1.0
                // -(double)out_array[1].size()/(double)units.length;
                out_array.insert(0, 0, pHD_TE);
                String pLMC_TE = units.length - out_array.lengthOfElement(2) + " / " + units.length;// 1.0 -
                // (double)out_array[2].size()/(double)units.length;
                out_array.insert(2, 0, pLMC_TE);
                String pME_TE = units.length - out_array.lengthOfElement(4) + " / " + units.length;// 1.0 -
                // (double)out_array[4].size()/(double)units.length;
                out_array.insert(4, 0, pME_TE);
                String pNE_TE = units.length - out_array.lengthOfElement(6) + " / " + units.length;
                out_array.insert(6, 0, pNE_TE);
                String pCE_TE = units.length - out_array.lengthOfElement(8) + " / " + units.length;
                out_array.insert(8, 0, pCE_TE);
            }
        }
        if (type == 2) { // WHat should be taken into account when looked at MIN properties
            if (qualifier.equals("Mean Quantization Error")) {
                out_array.createNewElement(0);
                out_array.createNewElement(3);
                out_array.createNewElement(5);
                out_array.createNewElement(13);
                out_array.insert(13, 1.0); // no need for further calculations

                for (Unit unit : units) { // Test all units
                    Boolean hasSingleClasses = this.hasSingleClass(unit);
                    boolean isMapedge = this.isOnMapEdge(unit);
                    if (this.hasHighDensity(unit) == false) {
                        out_array.insert(0, unit);
                    }

                    if (hasSingleClasses == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(2))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(2));
                        }
                    } else {
                        if (hasSingleClasses == false) {
                            out_array.insert(3, unit);
                        }
                    }
                    if (isMapedge == true) {
                        out_array.insert(5, unit);
                    }

                }
                String pHD_MQE = units.length - out_array.lengthOfElement(0) + " / " + units.length;// 1.0
                // -(double)out_array[1].size()/(double)units.length;
                out_array.insert(0, 0, pHD_MQE);
                String pLSC_MQE = units.length - out_array.lengthOfElement(3) + " / " + units.length;// 1.0 -
                // (double)out_array[2].size()/(double)units.length;
                out_array.insert(3, 0, pLSC_MQE);
                String pnME_MQE = units.length - out_array.lengthOfElement(5) + " / " + units.length;// 1.0 -
                // (double)out_array[4].size()/(double)units.length;
                out_array.insert(5, 0, pnME_MQE);
            }
            if (qualifier.equals("Topographic Error")) {
                out_array.createNewElement(1);
                out_array.createNewElement(3);
                out_array.createNewElement(5);
                out_array.createNewElement(7);
                out_array.createNewElement(9);

                for (int i = 0; i < units.length; i++) { // Test all units
                    Boolean isSingleClasses = this.hasSingleClass(units[i]);
                    ArrayList<SemanticClass> isNotClusterEdge = this.isOnClusterEdge(units[i]);
                    Boolean Noneighborsempty = this.hasNeighboringEmptyUnits(units[i]);
                    if (Noneighborsempty != null) {
                        Noneighborsempty = !Noneighborsempty;
                    }

                    if (this.hasLowDensity(units[i]) == false) {
                        out_array.insert(1, units[i]);
                    }

                    if (isSingleClasses == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(3))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(3));
                        }
                    } else {
                        if (isSingleClasses == false) {
                            out_array.insert(3, units[i]);
                        }
                    }
                    if (!this.isOnMapEdge(units[i]) == false) {
                        out_array.insert(5, units[i]);
                    }

                    if (isNotClusterEdge == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(9))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(9));
                        }
                    } else {
                        if (isNotClusterEdge.size() != 0) {
                            out_array.insert(9, units[i]);
                        }
                    }

                    if (Noneighborsempty == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(7))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(7));
                        }
                    } else {
                        if (Noneighborsempty == false) {
                            out_array.insert(7, units[i]);
                        }
                    }
                }

                String pHD_TE = units.length - out_array.lengthOfElement(1) + " / " + units.length;// 1.0
                // -(double)out_array[1].size()/(double)units.length;
                out_array.insert(1, 0, pHD_TE);
                String pLMC_TE = units.length - out_array.lengthOfElement(3) + " / " + units.length;// 1.0 -
                // (double)out_array[2].size()/(double)units.length;
                out_array.insert(3, 0, pLMC_TE);
                String pME_TE = units.length - out_array.lengthOfElement(5) + " / " + units.length;// 1.0 -
                // (double)out_array[4].size()/(double)units.length;
                out_array.insert(5, 0, pME_TE);
                String pNE_TE = units.length - out_array.lengthOfElement(7) + " / " + units.length;
                out_array.insert(7, 0, pNE_TE);
                String pCE_TE = units.length - out_array.lengthOfElement(9) + " / " + units.length;
                out_array.insert(9, 0, pCE_TE);

            }

            if (qualifier.equals("Intrinsic Distance")) {
                // Only evaluate the Topographic Error part
                out_array.createNewElement(1);
                out_array.createNewElement(3);
                out_array.createNewElement(5);
                out_array.createNewElement(7);
                out_array.createNewElement(9);

                for (int i = 0; i < units.length; i++) { // Test all units
                    Boolean isSingleClasses = this.hasSingleClass(units[i]);
                    ArrayList<SemanticClass> isNotClusterEdge = this.isOnClusterEdge(units[i]);
                    Boolean Noneighborsempty = this.hasNeighboringEmptyUnits(units[i]);
                    if (Noneighborsempty != null) {
                        Noneighborsempty = !Noneighborsempty;
                    }

                    if (this.hasLowDensity(units[i]) == false) {
                        out_array.insert(1, units[i]);
                    }

                    if (isSingleClasses == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(3))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(3));
                        }
                    } else {
                        if (isSingleClasses == false) {
                            out_array.insert(3, units[i]);
                        }
                    }
                    if (!this.isOnMapEdge(units[i]) == false) {
                        out_array.insert(5, units[i]);
                    }

                    if (isNotClusterEdge == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(9))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(9));
                        }
                    } else {
                        if (isNotClusterEdge.size() != 0) {
                            out_array.insert(9, units[i]);
                        }
                    }

                    if (Noneighborsempty == null) {
                        if (!this.damagedcriteriaQM.contains(this.getQualityIdentifier(7))) {
                            this.damagedcriteriaQM.add(this.getQualityIdentifier(7));
                        }
                    } else {
                        if (Noneighborsempty == false) {
                            out_array.insert(7, units[i]);
                        }
                    }
                }

                String pHD_TE = units.length - out_array.lengthOfElement(1) + " / " + units.length;// 1.0
                // -(double)out_array[1].size()/(double)units.length;
                out_array.insert(1, 0, pHD_TE);
                String pLMC_TE = units.length - out_array.lengthOfElement(3) + " / " + units.length;// 1.0 -
                // (double)out_array[2].size()/(double)units.length;
                out_array.insert(3, 0, pLMC_TE);
                String pME_TE = units.length - out_array.lengthOfElement(5) + " / " + units.length;// 1.0 -
                // (double)out_array[4].size()/(double)units.length;
                out_array.insert(5, 0, pME_TE);
                String pNE_TE = units.length - out_array.lengthOfElement(7) + " / " + units.length;
                out_array.insert(7, 0, pNE_TE);
                String pCE_TE = units.length - out_array.lengthOfElement(9) + " / " + units.length;
                out_array.insert(9, 0, pCE_TE);
            }
        }

        if (out_array.isEmpty()) {
            return null;
        } else {
            return out_array;
        }
    }

    /**
     * Tests whether the Unit U is having a High Mapped Input Density.<br/>
     * This is made upon an assumption, we say a unit is highly dense when the average value of the first 15% of units
     * with highest values is equal or above that value that
     */
    public boolean hasHighDensity(Unit u) {
        Unit[] units = this.run.getGrowingSOM().getLayer().getAllUnits();
        double InputCount = 0.0;

        for (Unit unit : units) {
            if (unit.getNumberOfMappedInputs() > 0) {
                InputCount++;
            }
        }
        double f15p = InputCount / units.length * 15.0;
        int[] ValuesOff15P = new int[(int) f15p];
        int f15pCounter = 0;
        ArrayList<Integer> alreadyUsed = new ArrayList<Integer>();

        while (f15pCounter < f15p) {
            int MaxMapped = Integer.MIN_VALUE;

            for (Unit unit : units) {
                int inputs = unit.getNumberOfMappedInputs();
                if (inputs > MaxMapped && !alreadyUsed.contains(inputs)) {
                    MaxMapped = inputs;
                }
            }

            // *Fetch all values with current MaxMapped Max*/
            for (Unit unit : units) {
                int inputs = unit.getNumberOfMappedInputs();
                if (inputs == MaxMapped) {
                    if (f15pCounter < f15p) {
                        ValuesOff15P[f15pCounter] = MaxMapped;
                        f15pCounter++;
                        if (!alreadyUsed.contains(MaxMapped)) {
                            alreadyUsed.add(MaxMapped);
                        }
                    }
                }
            }
        }

        // Build average, then Compare it to actual unit
        int f15pAverage = 0;
        for (int element : ValuesOff15P) {
            f15pAverage += element;
        }
        f15pAverage = (int) Math.round((double) (f15pAverage / ValuesOff15P.length));

        if (u.getNumberOfMappedInputs() >= f15pAverage) {
            return true;
        } else {
            return false;
        }

    }

    /** Returns true if the Unit has a low density */
    public boolean hasLowDensity(Unit u) {
        return !this.hasHighDensity(u);
    }

    /**
     * Returns whether there are mixed classes on the Unit. If no class file was used during report Creation, die
     * measure is left out
     */
    public Boolean hasMixedClasses(Unit u) {
        int[] coords = new int[2];
        Boolean out = null;
        coords[0] = u.getXPos();
        coords[1] = u.getYPos();
        SemanticNode n = this.getNode(coords);
        if (n != null && n.empty != null) {
            if (n.Classes != null) {
                if (n.Classes.size() > 1) {
                    out = true;
                } else {
                    out = false;
                }
            } else {
                out = false;
            }
        }
        return out;
    }

    public Boolean hasSingleClass(Unit u) {
        Boolean b = hasMixedClasses(u);
        if (b != null) {
            return !b;
        } else {
            return b;
        }
    }

    /**
     * tests whether a unit is on the edge of the map<br>
     * FIXME: this should go to {@link GrowingLayer}
     */
    public boolean isOnMapEdge(Unit u) {
        // int x = u.getXPos();
        // int y = u.getYPos();
        for (int x_new = -1; x_new <= 1; x_new++) {
            for (int y_new = -1; y_new <= 1; y_new++) {
                int[] c = new int[2];
                c[0] = u.getXPos() + x_new;
                c[1] = u.getYPos() + y_new;
                SemanticNode s = this.getNode(c);
                if (s == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * returns a ArrayList with all classes for witch unit u resembles a Cluster Edge Unit for those classes. this need
     * a clss file to b presents, returns null otherwise
     */
    public ArrayList<SemanticClass> isOnClusterEdge(Unit u) {
        ArrayList<SemanticClass> out = null;
        if (this.sGrid.SemanticClassesIndex != null) {
            Iterator<SemanticClass> clsses = sGrid.SemanticClassesIndex.values().iterator();
            out = new ArrayList<SemanticClass>();
            while (clsses.hasNext()) {
                SemanticClass s = clsses.next();
                ArrayList<SemanticNode> edgeNodes = s.getEdgeNodes();
                for (int i = 0; i < edgeNodes.size(); i++) {
                    SemanticNode n = edgeNodes.get(i);
                    if (n.realCoordinates[0] == u.getXPos() && n.realCoordinates[1] == u.getYPos()) {
                        out.add(s);
                        break;
                    }
                }
            }
        }
        return out;
    }

    /**
     * * Returns true if 8-fold neighbourhood shows any Units with no Inputs. Need a class file to b presents, returns
     * null otherwise<br/>
     * FIXME: this should be move to {@link GrowingLayer}
     */
    public Boolean hasNeighboringEmptyUnits(Unit u) {
        Boolean out = null;
        boolean wasempty = false;
        for (int x_new = -1; x_new <= 1; x_new++) {
            for (int y_new = -1; y_new <= 1; y_new++) {
                int[] c = new int[2];
                c[0] = u.getXPos() + x_new;
                c[1] = u.getYPos() + y_new;
                if (c[0] >= 0 && c[0] <= this.sGrid.XLength - 1 && c[1] >= 0 && c[1] <= this.sGrid.YLength - 1
                        && c[0] != u.getXPos() && c[1] != u.getYPos()) {
                    SemanticNode s = this.getNode(c);
                    if (s != null) {
                        out = s.containsNoClasses(); // true if no classes , null if no class file was given
                        if (out != null) {
                            if (out) {
                                wasempty = true; // save the variable, in case the next unit gives false for #out#
                            }
                        }
                    }
                }
            }
        }
        if (wasempty) {
            out = true;
        }

        // out will be either false or, if no class file was present out will stay null
        return out;
    }

    /** returns the Semantic Node for given coordinates. a node is null if no class file was attached */
    private SemanticNode getNode(int[] c) {
        SemanticNode node = null;
        int[] coords;
        if (this.sGrid.labels != null) {
            for (SemanticNode[] label : this.sGrid.labels) {
                for (SemanticNode element : label) {
                    coords = element.realCoordinates;
                    if (coords[0] == c[0] && coords[1] == c[1]) {
                        node = element;
                    }
                }
            }
        }
        return node;
    }

    /**
     * Returns a String Representation of the Meaning of the Position of the ArrayList Array
     * 
     * @param quality_list_index gives a Description to the Index of the quality List below quality_list[0] = high
     *            Density quality_list[1] = low Density quality_list[2] = mixed classes quality_list[3] = one class
     *            quality_list[4] = Units on Map edges quality_list[5] = Units not on Map edges quality_list[6] = Having
     *            neighboring empty units quality_list[7] = having no neighboring empt unist quality_list[8] = Units on
     *            Cluster edges quality_list[9] = Units in Clusters quality_list[10] = Big Distance of mapped Vectors to
     *            Prototype vector. quality_list[11] = Small Distance of mapped Vectors to Prototype vector.
     *            quality_list[12] = Big average Distance of mapped Vectors compared to each other. quality_list[13] =
     *            Small average Distance of mapped Vectors compared to each other. quality_list[14] = Input Vectors
     *            equally distributed over the Map quality_list[15] = Input Vectors not equally distributed over the Map
     *            quality_list[16] = Intra Cluster Distances == 0, Inter Cluster Distances != 0 quality_list[17] = Intra
     *            Cluster Distances != 0, Inter Cluster Distances == 0
     */
    public String getQualityIdentifier(int quality_list_index) {
        String out = this.QualityIdentifier.get(quality_list_index);
        if (out == null) {
            out = "Error Occurded, check getQualityIdentifier(int quality_list_index)...";
        }
        return out;

    }

    public ArrayList<String> getClassFileDependantQualities() {
        return this.damagedcriteriaQM;
    }

    public void clearDamagedCriteriaList() {
        this.damagedcriteriaQM.clear();
    }

    public void writeQualityIdentifier() {
        this.QualityIdentifier.put(0, "High Density");
        this.QualityIdentifier.put(1, "Low Density");
        this.QualityIdentifier.put(2, "Mixed Classes");
        this.QualityIdentifier.put(3, "Single Class");
        this.QualityIdentifier.put(4, "units on Map Edges");
        this.QualityIdentifier.put(5, "Units not on Map Edges");
        this.QualityIdentifier.put(6, "Units having neighboring empty Units");
        this.QualityIdentifier.put(7, "Units having no neighboring empty Units");
        this.QualityIdentifier.put(8, "Units on Class Cluster Edges");
        this.QualityIdentifier.put(9, "Units inside Clusters");
        this.QualityIdentifier.put(10, "Big Distance of mapped Vectors to Prototype vector");
        this.QualityIdentifier.put(11, "Small Distance of mapped Vectors to Prototype vector");
        this.QualityIdentifier.put(12, "Big average Distance of mapped Vectors compared to each other");
        this.QualityIdentifier.put(13, "Small average Distance of mapped Vectors compared to each other");
        this.QualityIdentifier.put(14, "Input Vectors equally distributed over the Map");
        this.QualityIdentifier.put(15, "Input Vectors not equally distributed over the Map");
        this.QualityIdentifier.put(16, "Intra Cluster Distances equal to 0, Inter Cluster Distances not 0");
        this.QualityIdentifier.put(17, "Intra Cluster Distances not 0, Inter Cluster Distances equal to 0");
    }

}
