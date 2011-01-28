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
package at.tuwien.ifs.somtoolbox.reportgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers.QMConfigurationProfile;
import at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers.QMContainer;
import at.tuwien.ifs.somtoolbox.reportgenerator.output.SOMDescriptionHTML;

/**
 * This class gives a semantic interpretation of the Coordinates of the Units of a SOM.It calculates
 * North/South/east/West and Middle Regions of the given Array of UnitNodes. This is used for the semantic Report of the
 * ReportGenerator.
 * 
 * @author Martin Waitzbauer (0226025)
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SemanticInterpreterGrid {

    public int XLength;

    public int YLength;

    private int Region;

    private String imgDir;

    private TestRunResult TestRun;

    public SemanticNode[][] labels;

    public Unit[] units;

    private int[] Gravity = { 4, 5, 6, 7, 8 };

    public SemanticInterpreterGrid fatherGrid = null;

    private int type;

    private int GridSize = 0;

    public static int NUMBER_OF_REGIONS = 9;

    public HashMap<Integer, SemanticClass>[] SemanticClasses = new HashMap[SemanticInterpreterGrid.NUMBER_OF_REGIONS];

    public HashMap<Integer, SemanticClass> SemanticClassesIndex = null;

    public int totalEmptyNodes = 0;

    private EditableReportProperties EP = null;

    private QMContainer qm = null;

    public SemanticInterpreterGrid(Unit[] units, TestRunResult testrun, boolean master, int Region, int type,
            String imgDir) {
        int maxX = 0;
        int maxY = 0;
        int minY = 1000;
        int minX = 1000;
        this.imgDir = imgDir;
        this.units = units;
        this.Region = Region;
        this.type = type;
        this.EP = testrun.datasetInfo.getEP();
        for (int i = 0; i < this.SemanticClasses.length; i++) {
            SemanticClasses[i] = new HashMap<Integer, SemanticClass>();
        }
        if (testrun.datasetInfo.classInfoAvailable()) {
            this.SemanticClassesIndex = new HashMap<Integer, SemanticClass>();
        }
        // Get s the X, Y Boundaries for the given Array of units
        for (Unit u : units) {
            maxX = Math.max(maxX, u.getXPos());
            maxY = Math.max(maxY, u.getYPos());
            minX = Math.min(minX, u.getXPos());
            minY = Math.min(minY, u.getYPos());
        }
        XLength = maxX - minX + 1;
        YLength = maxY - minY + 1;
        TestRun = testrun;
        this.labels = new SemanticNode[XLength][YLength];
        int pointer = 0;
        for (int i = 0; i < YLength && pointer < units.length; i++) {
            for (int j = 0; j < XLength && pointer < units.length; j++) {
                labels[j][i] = new SemanticNode();
                labels[j][i].setRealCoordinates(units[pointer].getXPos(), units[pointer].getYPos());
                pointer++;
                GridSize++;
            }
        }
        int[] xPartition;
        int[] yPartition;
        if (XLength <= 2) { // If the side is too short, compute simpler approach
            xPartition = computeMiddlePartition(XLength);
        } else {
            xPartition = getPartition(XLength);
        }
        if (YLength <= 2) { // If the side is too short, compute simpler approach
            yPartition = computeMiddlePartition(YLength);
        } else {
            yPartition = getPartition(YLength);
        }

        int[] xAxis = computeAxisPartition(XLength);
        int[] yAxis = computeAxisPartition(YLength);
        if (master && XLength >= 10 && YLength >= 10) { /* Only makes sense if the Grid is sufficient large */
            this.qm = this.TestRun.getQMContainer();
            /* Create all small Grids (1-9) */
            SemanticInterpreterGrid NW = new SemanticInterpreterGrid(cutArray(0, xPartition[0] - 1, 0,
                    yPartition[0] - 1), testrun, false, 1, this.type, this.imgDir);
            SemanticInterpreterGrid NMiddle = new SemanticInterpreterGrid(cutArray(xPartition[0], xPartition[1], 0,
                    yPartition[0] - 1), testrun, false, 2, this.type, this.imgDir);
            SemanticInterpreterGrid NE = new SemanticInterpreterGrid(cutArray(xPartition[1] + 1, XLength - 1, 0,
                    yPartition[0] - 1), testrun, false, 3, this.type, this.imgDir);
            SemanticInterpreterGrid WMiddle = new SemanticInterpreterGrid(cutArray(0, xPartition[0] - 1, yPartition[0],
                    yPartition[1]), testrun, false, 4, this.type, this.imgDir);
            SemanticInterpreterGrid MiddleMiddle = new SemanticInterpreterGrid(cutArray(xPartition[0], xPartition[1],
                    yPartition[0], yPartition[1]), testrun, false, 5, this.type, this.imgDir);
            SemanticInterpreterGrid EMiddle = new SemanticInterpreterGrid(cutArray(xPartition[1] + 1, XLength - 1,
                    yPartition[0], yPartition[1]), testrun, false, 6, this.type, this.imgDir);
            SemanticInterpreterGrid SW = new SemanticInterpreterGrid(cutArray(0, xPartition[0] - 1, yPartition[1] + 1,
                    YLength - 1), testrun, false, 7, this.type, this.imgDir);
            SemanticInterpreterGrid SMiddle = new SemanticInterpreterGrid(cutArray(xPartition[0], xPartition[1],
                    yPartition[1] + 1, YLength - 1), testrun, false, 8, this.type, this.imgDir);
            SemanticInterpreterGrid SE = new SemanticInterpreterGrid(cutArray(xPartition[1] + 1, XLength - 1,
                    yPartition[1] + 1, YLength - 1), testrun, false, 9, this.type, this.imgDir);
            // SemanticInterpreterGrid[] SemanticInterpreterGrid = new SemanticInterpreterGrid[9];
            int pointX = 0;
            int pointY = 0;
            // NW
            for (int i = 0; i < yPartition[0]; i++) {
                for (int j = 0; j < xPartition[0]; j++) {
                    this.labels[j][i] = NW.getSemanticNodeArray()[j][i];
                    if (j == xPartition[0] - 1 && i == yPartition[0] - 1) {
                        this.labels[j][i].setSpecialLocation("Close to the Middle");
                    } else {
                        if (j == xPartition[0] - 1) {
                            this.labels[j][i].setSpecialLocation("Close to the Region north of the Map Center");
                        }
                        if (i == yPartition[0] - 1) {
                            this.labels[j][i].setSpecialLocation("Close to the Region west of the Map Center");
                        }
                    }
                }
            }
            // NMIDDLE
            pointX = xPartition[0];
            for (int i = 0; i < yPartition[0]; i++) {
                for (int j = 0, k = pointX; j < xPartition[1] - xPartition[0] + 1; j++, k++) {
                    this.labels[k][i] = NMiddle.getSemanticNodeArray()[j][i];
                    if (j == xPartition[0]) {
                        this.labels[k][i].setSpecialLocation("Close to the north-west Quadrant");
                    }
                    if (j == xPartition[1]) {
                        this.labels[k][i].setSpecialLocation("Close to the north-east Quadrant");
                    }
                    if (i == yPartition[0] - 1) {
                        this.labels[k][i].setSpecialLocation("Close to the Map Center");
                    }
                }
            }
            // NE
            pointX = xPartition[1] + 1;
            for (int i = 0; i < yPartition[0]; i++) {
                for (int j = 0, k = pointX; j < xPartition[0]; j++, k++) {
                    this.labels[k][i] = NE.getSemanticNodeArray()[j][i];
                }
            }
            // WMIDDLE
            pointY = yPartition[0];
            for (int i = 0; i < xPartition[0]; i++) {
                for (int j = 0, k = pointY; j < yPartition[1] - yPartition[0] + 1; k++, j++) {
                    this.labels[i][k] = WMiddle.getSemanticNodeArray()[i][j];
                }
            }
            // MIDDLE MIDDLE
            pointX = xPartition[0];
            for (int i = 0, u = pointY; i < yPartition[1] - yPartition[0] + 1; i++, u++) {
                for (int j = 0, k = pointX; j < xPartition[1] - xPartition[0] + 1; j++, k++) {
                    this.labels[k][u] = MiddleMiddle.getSemanticNodeArray()[i][j];
                }
            }
            // EMiddle
            pointX = xPartition[1] + 1;
            for (int i = 0, u = pointX; i < xPartition[0]; i++, u++) {
                for (int j = 0, k = pointY; j < yPartition[1] - yPartition[0] + 1; k++, j++) {
                    this.labels[u][k] = EMiddle.getSemanticNodeArray()[i][j];
                }
            }
            // SW
            pointY = yPartition[1] + 1;
            for (int i = 0; i < yPartition[0]; i++) {
                for (int j = 0, u = pointY; j < xPartition[0]; u++, j++) {
                    this.labels[i][u] = SW.getSemanticNodeArray()[i][j];
                }
            }
            // SMIDDLE
            pointX = xPartition[0];
            for (int i = 0, u = pointY; i < yPartition[0]; u++, i++) {
                for (int j = 0, k = pointX; j < xPartition[1] - xPartition[0] + 1; j++, k++) {
                    this.labels[k][u] = SMiddle.getSemanticNodeArray()[j][i];
                }
            }
            // SE
            pointX = xPartition[1] + 1;
            for (int i = 0, u = pointY; i < yPartition[0]; u++, i++) {
                for (int j = 0, k = pointX; j < xPartition[0]; j++, k++) {
                    this.labels[k][u] = SE.getSemanticNodeArray()[j][i];
                }
            }
        } else {
            // Project X Axis
            int NS = 0;
            for (int i = 0; i < YLength; i++) {
                for (int j = 0; j < xAxis.length; j++) {
                    if (j < xPartition[0] && labels[j][i] != null) {
                        labels[j][i].setWestEastDegree(xAxis[j]);
                        labels[j][i].setWELocationCell("West");
                        labels[j][i].setRegion(this.Region);
                        labels[j][i].setDescription();
                    }
                    if (j >= xPartition[0] && j <= xPartition[1] && labels[j][i] != null) {
                        if (i + 1 <= YLength / 2) {
                            NS = 1;
                        }
                        if (i + 1 > YLength / 2) {
                            NS = 2;
                        }
                        labels[j][i].setWestEastDegree(xAxis[j]);
                        labels[j][i].setOrientation(NS);
                        labels[j][i].setRegion(this.Region);
                        labels[j][i].setDescription();
                    }
                    if (j > xPartition[1] && labels[j][i] != null) {
                        labels[j][i].setWestEastDegree(xAxis[j]);
                        labels[j][i].setWELocationCell("East");
                        labels[j][i].setRegion(this.Region);
                        labels[j][i].setDescription();
                    }
                }
            }
            // Project Y Axis
            int WE = 0;
            for (int i = 0; i < XLength; i++) {
                for (int j = 0; j < yAxis.length; j++) {
                    if (j < yPartition[0] && labels[i][j] != null) {
                        labels[i][j].setNorthSouthDegree(yAxis[j]);
                        labels[i][j].setNSLocationCell("North");
                        labels[i][j].setRegion(this.Region);
                        labels[i][j].setDescription();
                    }
                    if (j >= yPartition[0] && j <= yPartition[1] && labels[i][j] != null) {
                        if (i + 1 <= XLength / 2) {
                            WE = 3;
                        }
                        if (i + 1 > XLength / 2) {
                            WE = 4;
                        }
                        labels[i][j].setNorthSouthDegree(yAxis[j]);
                        labels[i][j].setRegion(this.Region);
                        labels[i][j].setOrientation(WE);
                        labels[i][j].setDescription();
                    }
                    if (j > yPartition[1] && labels[i][j] != null) {
                        labels[i][j].setNorthSouthDegree(yAxis[j]);
                        labels[i][j].setNSLocationCell("South");
                        labels[i][j].setRegion(this.Region);
                        labels[i][j].setDescription();
                    }
                }
            }
        }
    }

    /** Estimates the Areas for the Middle part of the given Axis */
    private int[] getPartition(int axislength) {
        int[] Borders = new int[2];
        int counter;
        if (axislength % 2 == 0) { /* Even Number of Units */
            counter = 2;
            Borders[0] = axislength / 2 - 1;
            Borders[1] = axislength / 2;
            while (counter < axislength / 4) { /* Grow middle part to max 1/4 of the axis length */
                Borders[1] = Borders[1] + 1;
                Borders[0] = Borders[0] - 1;
                counter += 2;
            }
        } else { /* Uneven Number of units */
            counter = 1;
            Borders[0] = axislength / 2;
            Borders[1] = axislength / 2;
            while (counter < axislength / 4) { /* Grow middle part to max 1/4 of the axis length */
                Borders[1] = Borders[1] + 1;
                Borders[0] = Borders[0] - 1;
                counter += 2;
            }
        }
        return Borders;
    }

    /** Computes the Middle Partition (& let it grow) for the given axis */
    private int[] computeMiddlePartition(int axislength) {
        int[] Partition = getPartition(axislength);
        int l = Partition[0] - 1;
        int r = Partition[1];
        int middle = (r - l) / 2;
        if (middle == 0) {
            middle = 1;
        }
        int[] middleArray = new int[r - l];
        for (int i = 0; i < middleArray.length; i++) {
            if (i < middle) {
                middleArray[i] = 1; // N-W
            }
            if (i == middle) {
                middleArray[i] = 2; // MITTE
            }
            if (i > middle) {
                middleArray[i] = 3; // O-S
            }
        }
        return middleArray;
    }

    /**
     * Computes the areas for the left(type==1) or right (type==2) side of the Middle part for the given Axis and
     * divides the parts equally among the granularity measures defines by the [] Gravity
     */
    private int[] computeSidePartition(int axislength, int type) {
        int[] Partition = getPartition(axislength);
        int[] gradient = new int[Partition[0]];
        // Arrays.fill(gradient, -1);
        int Interval = Partition[0] / Gravity.length;
        int missing = Partition[0] - Interval * Gravity.length;
        int pointer = -1;
        if (type == 1) {
            pointer = this.Gravity.length;
        }
        if (type == 2) {
            pointer = 1;
        }

        if (Interval >= 1) {
            for (int i = 0; i < gradient.length; i += Interval) {
                for (int t = i, count = 0; count < Interval; t++, count++) {
                    if (pointer > 0 && pointer < Gravity.length + 1) {
                        gradient[t] = this.Gravity[pointer - 1];
                    }
                }
                if (type == 1) {
                    pointer--;
                }
                if (type == 2) {
                    pointer++;
                }
            }
            if (type == 1) {
                int[] copy = Arrays.copyOfRange(gradient, 0, gradient.length);
                for (int z = missing, c = 0, d = 0; z > 0; z--, c += 2, d++) {
                    gradient[c + 1] = gradient[c];
                    for (int x = c + 2, y = d + 1; x < gradient.length; x++, y++) {
                        gradient[x] = copy[y];
                    }
                }
            }
            if (type == 2) {
                int[] tempArray = computeSidePartition(axislength, 1);
                for (int i = 0, j = tempArray.length; i < gradient.length; i++, j--) {
                    gradient[i] = tempArray[j - 1];
                }
            }
        } else {
            if (type == 1) {
                for (int i = 0, count = Gravity[Gravity.length - 1]; i < gradient.length; i++, count--) {
                    gradient[i] = count;
                }
            }
            if (type == 2) {
                int[] tempArray = computeSidePartition(axislength, 1);
                for (int i = 0, j = tempArray.length; i < gradient.length; i++, j--) {
                    gradient[i] = tempArray[j - 1];
                }
            }
        }
        return gradient;
    }

    /** Combines the used partitions for one axis (for easier handling) */
    private int[] computeAxisPartition(int axislength) {
        ArrayList<int[]> list = new ArrayList<int[]>();
        list.add(computeSidePartition(axislength, 1));
        list.add(computeMiddlePartition(axislength));
        list.add(computeSidePartition(axislength, 2));
        int[] result = new int[axislength];
        int pointer = 0;
        for (int i = 0; i < list.size(); i++) {
            int[] tempArray = list.get(i);
            for (int element : tempArray) {
                result[pointer] = element;
                pointer++;
            }
        }
        return result;
    }

    /** returns the semantic node array */
    public SemanticNode[][] getSemanticNodeArray() {
        return this.labels;
    }

    /**
     * Creates all the Info needed for Report
     * 
     * @param regularGrid true if the Grid is regular squared
     */
    public void initializeGridwithClasses(boolean regularGrid) {
        // ArrayList<int[]> UnitswithClassList;
        int[] currUnitClasses;
        this.setSemanticGrid();
        // double[][][] classDistribution = this.TestRun.getClassDistribution();
        // int[] classmix = new int[NUMBER_OF_REGIONS];
        for (int i = 0; i < this.YLength; i++) {
            for (int j = 0; j < XLength; j++) {
                int pointer = i * this.XLength + j;
                if (pointer < units.length) {
                    Unit u = this.units[pointer];
                    currUnitClasses = this.TestRun.getClassesForUnit(u.getXPos(), u.getYPos());
                    for (int h = 0; h < currUnitClasses.length; h++) { // create a report for the current class
                        if (currUnitClasses[h] > 0) {
                            if (regularGrid) {
                                createClassReport(h);
                            } else {
                                addClasstoGrid(h);
                            }
                        }
                    }
                    if (u.getNumberOfMappedInputs() == 0) { // No Classes on Unit
                        this.labels[j][i].setcontainsNoClasses(true);
                        this.totalEmptyNodes++;
                    } else {
                        this.labels[j][i].setcontainsNoClasses(false);
                    }
                }
            }
        }
    }

    /**
     * Creates a 3 fold Report
     * 
     * @param indexReport = Number of the GridNumber to be analyzed
     * @param clusterlevelReport number of the Clusterlevel to be analyzed
     * @param QMReportIntersection [0] = Name of Quality Measure, [1] = 1 = MAX, 2 = MIN, [3..length] intersectio nunits
     *            of the QM to be analyzed
     */
    public String createClassReportOnGrid(int indexReport, int clusterlevelReport, ArrayList QMReportIntersection) {
        // count classes on Units
        // Now say something about the classes
        String out = "";
        if (!this.TestRun.datasetInfo.classInfoAvailable()) {
            return out;
        }
        Iterator<SemanticClass> ii;
        HashMap<Integer, SemanticClass> classeshmap = null;
        if (indexReport != 0) {
            classeshmap = this.SemanticClasses[indexReport - 1];
        }
        if (clusterlevelReport != 0) {
            classeshmap = this.getClusterLevelClasses();
        }
        if (QMReportIntersection != null) {
            classeshmap = new HashMap<Integer, SemanticClass>();
            for (int i = 2; i < QMReportIntersection.size(); i++) {
                SemanticNode s = (SemanticNode) QMReportIntersection.get(i);
                if (s.Classes != null) {
                    for (int j = 0; j < s.Classes.size(); j++) {
                        SemanticClass c = s.Classes.get(j);
                        boolean ok = true;
                        if (this.EP.getMAXCompactness() != -1 || this.EP.getMINCompactness() != -1) {
                            if (!c.matchesCompactnessRequirements(this.EP)) {
                                ok = false;
                            }
                        }
                        if (!classeshmap.containsKey(c.index) && ok) {
                            classeshmap.put(c.index, c);
                        }

                    }
                }
            }
        }
        // which class can be found most often
        Iterator<SemanticClass> io = classeshmap.values().iterator();
        int[] ClassCounter = new int[this.TestRun.datasetInfo.getNumberOfClasses()];
        while (io.hasNext()) {
            SemanticClass s = io.next();
            ClassCounter[s.index]++;
        }
        int maxClass = Integer.MIN_VALUE;
        int maxClassIndex = -1;
        for (int i = 0; i < ClassCounter.length; i++) {
            if (ClassCounter[i] > maxClass) {
                maxClass = ClassCounter[i];
                maxClassIndex = i;
            }
        }

        ii = classeshmap.values().iterator();
        ArrayList<SemanticNode> EmptyNodes = new ArrayList<SemanticNode>();
        int overallCompactness = 0;
        String Region = new String();
        ArrayList<Integer> ClassesCompleteinRegion = new ArrayList<Integer>();
        ArrayList<Integer> ClassesOutsideofRegion = new ArrayList<Integer>();
        Region = getRegion(indexReport);
        int FullyConnected = 0;
        ArrayList<ArrayList<SemanticNode>> EmptyNodesTrail = null;
        if (indexReport != 0 || clusterlevelReport != 0) {
            getClassesfromFatherGrid();
            for (int i = 0; i < YLength; i++) {
                for (int j = 0; j < XLength; j++) {
                    if (labels[j][i] != null) {
                        Boolean conatinsNoClasses = labels[j][i].containsNoClasses();
                        if (conatinsNoClasses != null) {
                            if (conatinsNoClasses) { // Unit has no Classes on it
                                if (indexReport != 0) {
                                    if (labels[j][i].Region == indexReport) {
                                        EmptyNodes.add(this.labels[j][i]);
                                        totalEmptyNodes++;
                                    } else {
                                        totalEmptyNodes++;
                                    }
                                }
                                if (clusterlevelReport != 0) {
                                    EmptyNodes.add(this.labels[j][i]);
                                    totalEmptyNodes = fatherGrid.totalEmptyNodes;
                                }
                            }
                        }
                    }
                }
            }
            // Look if the empty nodes in this Region are connected
            EmptyNodesTrail = this.getEmptyNodeTrail(EmptyNodes);
        }

        int[] regionMix = new int[SemanticInterpreterGrid.NUMBER_OF_REGIONS];
        Arrays.fill(regionMix, 0);
        int RegionsharedClasses = 0;
        int TotalsharedClasses = 0;
        if (clusterlevelReport != 0) { // we have a Cluster
            TotalsharedClasses = this.getNumberofSharedClasses(true);
        }
        if (indexReport != 0) { // we have a regular Region from a Grid
            TotalsharedClasses = this.getNumberofSharedClasses(false);
        }

        /** MAIN CLASS LOOP **/
        // That is where all the calculations are done / Class
        while (ii.hasNext()) {
            boolean seperated = false;
            SemanticClass sem = ii.next();

            // Special Class Calculation when QMIntersectionreport is not NULL

            if (QMReportIntersection != null) {
                ArrayList currClassIntersectionsMAX = new ArrayList();
                ArrayList currClassIntersectionsMIN = new ArrayList();
                for (int i = 0; i < this.qm.UnitQualityMeasureNames.size(); i++) { // for all qualityMeasures
                    ArrayList<SemanticNode> tempIntersectMAX = new ArrayList<SemanticNode>();
                    ArrayList<SemanticNode> tempIntersectMIN = new ArrayList<SemanticNode>();
                    if (!QMReportIntersection.get(0).equals(this.qm.UnitQualityMeasureNames.get(i))) { // that are not
                        // the current
                        // qualifier
                        ArrayList<int[]> classUnits = this.TestRun.getAllUnitsContainingClass(sem.index);
                        for (int j = 0; j < classUnits.size(); j++) { // for all units
                            int[] currUnit = classUnits.get(j);
                            int[][] qmUnits = this.qm.getClassifiedUnits(this.qm.UnitQualityMeasureNames.get(i));
                            if ((Integer) QMReportIntersection.get(1) == 1) { // MAX-MAX REPORT
                                if (qmUnits[currUnit[0]][currUnit[1]] == 5 || qmUnits[currUnit[0]][currUnit[1]] == 4) {// we
                                    // have
                                    // a
                                    // maximum
                                    // of
                                    // another
                                    // qm there aswell
                                    tempIntersectMAX.add(this.getNode(currUnit));
                                }
                            }
                            if ((Integer) QMReportIntersection.get(1) == 2) { // MIN-MIN REPORT
                                if (qmUnits[currUnit[0]][currUnit[1]] == 2 || qmUnits[currUnit[0]][currUnit[1]] == 1) {// we
                                    // have
                                    // a
                                    // minimum
                                    // of
                                    // another
                                    // qm there aswell
                                    tempIntersectMIN.add(this.getNode(currUnit));
                                }
                            }
                            if ((Integer) QMReportIntersection.get(1) == 3) { // MAX-MIN REPORT
                                if (qmUnits[currUnit[0]][currUnit[1]] == 5 || qmUnits[currUnit[0]][currUnit[1]] == 4) { // we
                                    // have
                                    // a
                                    // minimum
                                    // of
                                    // another
                                    // qm there aswell
                                    tempIntersectMAX.add(this.getNode(currUnit));
                                }
                                if (qmUnits[currUnit[0]][currUnit[1]] == 2 || qmUnits[currUnit[0]][currUnit[1]] == 1) {// we
                                    // have
                                    // a
                                    // minimum
                                    // of
                                    // another
                                    // qm there aswell
                                    tempIntersectMIN.add(this.getNode(currUnit));
                                }
                            }
                            if ((Integer) QMReportIntersection.get(1) == 4) { // MIN-MAX REPORT
                                if (qmUnits[currUnit[0]][currUnit[1]] == 2 || qmUnits[currUnit[0]][currUnit[1]] == 1) {// we
                                    // have
                                    // a
                                    // minimum
                                    // of
                                    // another
                                    // qm there aswell
                                    tempIntersectMAX.add(this.getNode(currUnit));
                                }
                                if (qmUnits[currUnit[0]][currUnit[1]] == 5 || qmUnits[currUnit[0]][currUnit[1]] == 4) { // we
                                    // have
                                    // a
                                    // minimum
                                    // of
                                    // another
                                    // qm there aswell
                                    tempIntersectMIN.add(this.getNode(currUnit));
                                }
                            }
                        }
                        if (tempIntersectMAX.size() > 0) { // we had intersectional nodes with other QM
                            currClassIntersectionsMAX.add(this.qm.UnitQualityMeasureNames.get(i)); // QM Name on uneven
                            // Places;
                            currClassIntersectionsMAX.add(tempIntersectMAX); // Intersectional node array on even places
                        }
                        if (tempIntersectMIN.size() > 0) { // we had intersectional nodes with other QM
                            currClassIntersectionsMIN.add(this.qm.UnitQualityMeasureNames.get(i)); // QM Name on uneven
                            // Places;
                            currClassIntersectionsMIN.add(tempIntersectMIN); // Intersectional node array on even places
                        }
                    }
                }
                /*
                 * if(!QMIntersectionClasshMap.containsKey(sem.index) && currClassIntersections.size() >0){ QMIntersectionClasshMap.put(sem.index,
                 * currClassIntersections); }
                 */
                if (currClassIntersectionsMIN.size() > 0) {
                    sem.setQMIntersectionClassArrayListMIN(currClassIntersectionsMIN);
                }
                if (currClassIntersectionsMAX.size() > 0) {
                    sem.setQMIntersectionClassArrayListMAX(currClassIntersectionsMAX);
                }
            }
            /**/
            /** Say something about how many classes are in / outside the Region **/

            if (indexReport != 0) { // Report on Partial Grid
                if (sem.regionmix[indexReport - 1] > 0) { // Class from this Region
                    RegionsharedClasses += sem.SharedClasses; // how many classes share their points with how many other
                    // classes
                    if (sem.getCompactness() > 0.6) {// how many classes are compact
                        overallCompactness++;
                    }
                    if (sem.ClassPartitions.size() == 1) {
                        FullyConnected++;
                    }

                    for (int z = 0; z < sem.regionmix.length; z++) { // say how much of the classes are in the region
                        if (sem.regionmix[z] > 0 && z != indexReport - 1) { // Found classentry in another region
                            regionMix[z]++;
                            seperated = true;
                        }
                    }
                    if (seperated) {
                        ClassesOutsideofRegion.add(sem.index);
                    } else {
                        ClassesCompleteinRegion.add(sem.index);
                    }
                }
            } else { // Reporton Whole Grid or on intersectional Nodes
                RegionsharedClasses += sem.SharedClasses; // how many classes share their points with how many other
                // classes
                if (sem.getCompactness() > 0.6) { // how many classes are compact
                    overallCompactness++;
                }
                if (sem.ClassPartitions.size() == 1) {
                    FullyConnected++;
                }
                if (clusterlevelReport != 0) {
                    if (this.TestRun.datasetInfo.getNumberOfClassmembers(sem.index) != sem.MemberCount) {
                        seperated = true;
                    }
                }
                if (QMReportIntersection != null) {
                    int QMClassCount = 0;
                    for (int k = 2; k < QMReportIntersection.size(); k++) {
                        SemanticNode s = (SemanticNode) QMReportIntersection.get(k);
                        int[] classes = this.TestRun.getClassesForUnit(s.realCoordinates[0], s.realCoordinates[1]);
                        QMClassCount += classes[sem.index];
                    }
                    if (this.TestRun.datasetInfo.getNumberOfClassmembers(sem.index) != QMClassCount) {
                        seperated = true;
                    }
                }
                if (seperated) {
                    ClassesOutsideofRegion.add(sem.index);
                } else {
                    ClassesCompleteinRegion.add(sem.index);
                }
            }
        }

        if (this.type == 1) { // HTML REPORT
            if (classeshmap.size() > 0) {
                if (indexReport != 0) {
                    out += "<p class=\"header\">Class Report on Region:</p>";
                }
                if (clusterlevelReport != 0) {
                    out += "<p class=\"header\">Class Report on Cluster:</p>";
                }
                if (QMReportIntersection != null) {
                    out += "<p class=\"header\">Special Class Report on Intersectional Units:</p>";
                }
                if (indexReport != 0) {
                    out += "The Region " + Region + " maintains ";
                }
                if (clusterlevelReport != 0) {
                    out += "The Clusterregion maintains ";
                }
                if (QMReportIntersection != null) {
                    out += "The intersectional Units of these Quality Measures contain ";
                }
                out += ClassesOutsideofRegion.size()
                        + ClassesCompleteinRegion.size()
                        + " ("
                        + Math.round(((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size())
                                / this.TestRun.getNumberofClasses() * 100) + " %) of the Classes of the SOM.";
                if (ClassesCompleteinRegion.size() > 0 || ClassesOutsideofRegion.size() > 0) {
                    if (indexReport != 0 && ClassesCompleteinRegion.size() > 0) {
                        out += "<a href=\"javascript:showSemanticClassesinRegion('in',"
                                + indexReport
                                + ","
                                + this.TestRun.getRunId()
                                + ");\">"
                                + ClassesCompleteinRegion.size()
                                + "("
                                + Math.round(ClassesCompleteinRegion.size()
                                        / ((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size())
                                        * 100) + ")%</a> ";
                        out += "<div id =\"classes_in_" + indexReport + "_" + this.TestRun.getRunId()
                                + "\" style=\"display:none\">";
                        // out+="<b>(</b>"+inside+"<b>)</b>";
                        out += this.showClassInfos(ClassesCompleteinRegion, null);
                        out += "</div> are only found in this Sector. ";
                    }
                    if (clusterlevelReport != 0 && ClassesCompleteinRegion.size() > 0) {
                        out += "<a href=\"javascript:showSemanticClassesinRegion('in',"
                                + (clusterlevelReport + 10)
                                + ","
                                + this.TestRun.getRunId()
                                + ");\">"
                                + ClassesCompleteinRegion.size()
                                + "("
                                + Math.round(ClassesCompleteinRegion.size()
                                        / ((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size())
                                        * 100) + ")%</a> ";
                        out += "<div id =\"classes_in_" + (10 + clusterlevelReport) + "_" + this.TestRun.getRunId()
                                + "\" style=\"display:none\">";
                        // out+="<b>(</b>"+inside+"<b>)</b>";
                        out += this.showClassInfos(ClassesCompleteinRegion, null);
                        out += "</div> are only found in this Sector. ";
                    }
                    if (QMReportIntersection != null && ClassesCompleteinRegion.size() > 0) {
                        out += "<a href=\"javascript:showSemanticClassesinRegion('in','"
                                + QMReportIntersection
                                + "',"
                                + this.TestRun.getRunId()
                                + ");\">"
                                + ClassesCompleteinRegion.size()
                                + "("
                                + Math.round(ClassesCompleteinRegion.size()
                                        / ((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size())
                                        * 100) + ")%</a> ";
                        out += "<div id =\"classes_in_" + QMReportIntersection + "_" + this.TestRun.getRunId()
                                + "\" style=\"display:none\">";
                        // out+="<b>(</b>"+inside+"<b>)</b>";
                        out += this.showClassInfos(ClassesCompleteinRegion, (Integer) QMReportIntersection.get(1));
                        out += "</div> are only found on those Units. ";
                    }
                    if (indexReport != 0 && ClassesOutsideofRegion.size() > 0) {
                        out += "<a href=\"javascript:showSemanticClassesinRegion('out',"
                                + indexReport
                                + ","
                                + this.TestRun.getRunId()
                                + ");\">"
                                + ClassesOutsideofRegion.size()
                                + "("
                                + Math.round(ClassesOutsideofRegion.size()
                                        / ((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size())
                                        * 100) + ")%</a> ";
                        out += "<div id =\"classes_out_" + indexReport + "_" + this.TestRun.getRunId()
                                + "\" style=\"display:none\">";
                        // out+="<b>(</b>"+outside+"<b>)</b>";
                        out += this.showClassInfos(ClassesOutsideofRegion, null);
                        out += "</div>";
                    }
                    if (clusterlevelReport != 0 && ClassesOutsideofRegion.size() > 0) {
                        out += "<a href=\"javascript:showSemanticClassesinRegion('out',"
                                + (clusterlevelReport + 10)
                                + ","
                                + this.TestRun.getRunId()
                                + ");\">"
                                + ClassesOutsideofRegion.size()
                                + "("
                                + Math.round(ClassesOutsideofRegion.size()
                                        / ((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size())
                                        * 100) + ")%</a> ";
                        out += "<div id =\"classes_out_" + (10 + clusterlevelReport) + "_" + this.TestRun.getRunId()
                                + "\" style=\"display:none\">";
                        // out+="<b>(</b>"+outside+"<b>)</b>";
                        out += this.showClassInfos(ClassesOutsideofRegion, null);
                        out += "</div>";
                    }
                    if (QMReportIntersection != null && ClassesOutsideofRegion.size() > 0) {
                        out += "<a href=\"javascript:showSemanticClassesinRegion('out','"
                                + QMReportIntersection
                                + "',"
                                + this.TestRun.getRunId()
                                + ");\">"
                                + ClassesOutsideofRegion.size()
                                + "("
                                + Math.round(ClassesOutsideofRegion.size()
                                        / ((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size())
                                        * 100) + ")%</a> ";
                        out += "<div id =\"classes_out_" + QMReportIntersection + "_" + this.TestRun.getRunId()
                                + "\" style=\"display:none\">";
                        // out+="<b>(</b>"+outside+"<b>)</b>";
                        out += this.showClassInfos(ClassesOutsideofRegion, (Integer) QMReportIntersection.get(1));
                        out += "</div>";
                    }
                    if (ClassesOutsideofRegion.size() > 0) {
                        out += "are spread among ";
                        if (indexReport != 0) {
                            String regions = "";
                            for (int i = 0; i < regionMix.length; i++) {
                                if (regionMix[i] > 0) {
                                    regions += getRegion(i + 1) + ",";
                                }
                            }
                            if (ClassesOutsideofRegion.size() > 0) {
                                regions = regions.substring(0, regions.length() - 1) + ".";
                                out += regions;
                            }
                        } else {
                            if (clusterlevelReport != 0) {
                                out += "neighboring Regions";
                            }
                            if (QMReportIntersection != null) {
                                out += "different Units";
                            }
                        }
                    }
                }
                out += " The Class with the most Occurance is" + this.TestRun.datasetInfo.getNameOfClass(maxClassIndex)
                        + ". ";
            } else {
                out += "There were no classes found on the Units.";
            }

        }
        if (this.type == 2) { // LATEX REPORT
            if (ClassesCompleteinRegion.size() > 0 || ClassesOutsideofRegion.size() > 0) {
                out += ClassesOutsideofRegion.size()
                        + ClassesCompleteinRegion.size()
                        + " ("
                        + Math.round(((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size())
                                / this.TestRun.getNumberofClasses() * 100)
                        + " %) of the Classes of the SOM."
                        + +ClassesCompleteinRegion.size()
                        + "("
                        + Math.round(ClassesCompleteinRegion.size()
                                / ((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size())
                                * 100)
                        + ")% are only found in this Sector,"
                        + ClassesOutsideofRegion
                        + "("
                        + Math.round(ClassesOutsideofRegion.size()
                                / ((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size())
                                * 100) + ")% are" + " spread among ";
                if (indexReport != 0) {
                    String regions = "";
                    for (int i = 0; i < regionMix.length; i++) {
                        if (regionMix[i] > 0) {
                            regions += getRegion(i + 1) + ",";
                        }
                    }
                    regions = regions.substring(0, regions.length() - 1) + ".";
                    out += regions;
                } else {
                    if (clusterlevelReport != 0) {
                        out += "neighboring Regions";
                    }
                    if (QMReportIntersection != null) {
                        out += "Units, not contained in the Intersection";
                    }
                }
            }
        }

        if (QMReportIntersection == null) {
            out += Math.round((double) EmptyNodes.size() / (double) totalEmptyNodes * 100)
                    + " % of all empty Unit Nodes can be found in this Region. ";
            if (EmptyNodes.size() > 0) {
                out += "The empty Nodes of this  Region are found in " + EmptyNodesTrail.size()
                        + " connected 'Trails'.";
            }
        }
        if (ClassesCompleteinRegion.size() > 0 || ClassesOutsideofRegion.size() > 0) {
            out += overallCompactness
                    + " ("
                    + Math.round(overallCompactness
                            / ((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size()) * 100)
                    + "%) of the classes found are very centered, meaning that at least 60% of all Classmembers are in very short distance to their common Center of Gravity. This Distance"
                    + "is calculated on the mean Distance of the Central Unit Node to all other Nodes containing Class Members.";
            out += FullyConnected
                    + " ("
                    + Math.round(FullyConnected
                            / ((double) ClassesOutsideofRegion.size() + (double) ClassesCompleteinRegion.size()) * 100)
                    + "%) of the classes are not seperated over the Map. (Every Unit with Classmembers has a maximal distance of one to another unit with classmembers).";
            if (QMReportIntersection == null) {
                out += "Concerning the Separation of the Classes in the Region, one can  say that ";
                double InOutRatio = (double) ClassesCompleteinRegion.size() / (double) ClassesOutsideofRegion.size();
                if (InOutRatio < 0.6) { // Only 40% of the regions classes are seperated within
                    out += " it is not easy to make a clear statement on the Seperation in this Region only due to the Fact that Classes maintained, are also mapped outside of this Region. The following Statement therefore doesnt count for this Region alone, but also counts for some of the regions listed above.";
                    out += "Each class shares its units with an average "
                            + String.format("%.2f",
                                    (double) RegionsharedClasses
                                            / (double) (ClassesOutsideofRegion.size() + ClassesCompleteinRegion.size()))
                            + " other classes. In Comparison to that the Overall mean of Classes on the SOM that share their units with other Classes is "
                            + String.format("%.2f",
                                    (double) TotalsharedClasses / (double) this.TestRun.getNumberofClasses()) + ". ";
                    double percentage = (double) RegionsharedClasses
                            / (double) (ClassesOutsideofRegion.size() + ClassesCompleteinRegion.size())
                            / ((double) TotalsharedClasses / (double) this.TestRun.getNumberofClasses());
                    if (percentage <= 0.4 && percentage > 0.0) {
                        out += "So there is an distinct proof that these Classes are seperated quite good.";
                    }
                    if (percentage > 0.4 && percentage <= 0.6) {
                        out += "So there is a slight Indication that these classes are seperated good.";
                    }
                    if (percentage > 0.6 && percentage <= 0.8) {
                        out += "So there is a slight Indication that some of these Classes might be sepereated better than the average case.";
                    }
                    if (percentage > 0.8 && percentage <= 1.1) {
                        out += "So there is a strong Indication that these Classes are seperated averagely.";
                    }
                    if (percentage > 1.1 && percentage <= 1.5) {
                        out += "So there is strong Indication that these Classes are seperated worse, than in the mean Average case.";
                    }
                    if (percentage > 1.5) {
                        out += "So there is a clear proof that these Classes are really bad seperated , and may count among the worst seperated Classes of the SOM.";
                    }
                } else { // wehave a good amount of classes completly fenced in this Region
                    out += " more Classes in this Region lie completly inside,  than there are Classes seperated over fencing Regions.  One can therefore think  of  the following statement as being possibly accurate for the Classes in whole Region.  Each class shares its units with an average "
                            + String.format("%.2f",
                                    (double) RegionsharedClasses
                                            / (double) (ClassesOutsideofRegion.size() + ClassesCompleteinRegion.size()))
                            + " other classes. In Comparison to that the Overall mean of Classes on the SOM that share their units with other Classes is "
                            + String.format("%.2f",
                                    (double) TotalsharedClasses / (double) this.TestRun.getNumberofClasses()) + ". ";
                    double percentage = (double) RegionsharedClasses
                            / (double) (ClassesOutsideofRegion.size() + ClassesCompleteinRegion.size())
                            / ((double) TotalsharedClasses / (double) this.TestRun.getNumberofClasses());
                    if (percentage <= 0.4 && percentage > 0.0) {
                        out += "So there is a distinct proof that the Classes in the Region are seperated quite good.";
                    }
                    if (percentage > 0.4 && percentage <= 0.6) {
                        out += "So there is a slight Indication that the classes in this Region are seperated good.";
                    }
                    if (percentage > 0.6 && percentage <= 0.8) {
                        out += "So there is a slight Indication that some of the Classes in this Region might be sepereated better than the average case.";
                    }
                    if (percentage > 0.8 && percentage <= 1.1) {
                        out += "So there is a strong Indication that the Classes in this Region are seperated averagely.";
                    }
                    if (percentage > 1.1 && percentage <= 1.5) {
                        out += "So there is a strong Indication that the Classes in this Region are seperated worse, than in the mean Average case.";
                    }
                    if (percentage > 1.5) {
                        out += "So there is a clear proof that these Classes in this Region are really bad seperated , and may count among the worst seperated Classes of the SOM.";
                    }
                }
            } else { // Special QM Intersection report
                /*
                 * Iterator ClassNames = QMIntersectionClasshMap.keySet().iterator(); Iterator Values = QMIntersectionClasshMap.values().iterator();
                 * out+="Following Classes can also be found in the Regions, where the listed Quality Measure has a "; String MinMax ="";
                 * if((Integer)QMReportIntersection.get(1)==1) MinMax+= "Maximum"; else MinMax+= "Minimum"; out+=MinMax+" ."; out+="<br>";
                 * out+="<ul>"; while(ClassNames.hasNext()){ String clssName = this.TestRun.datasetInfo.getNameOfClass((Integer)ClassNames.next());
                 * int zz =6; if(clssName.equals("Beverly")) zz=77; ArrayList Datalist = (ArrayList)Values.next(); out+="<li>";
                 * out+="<u>"+clssName+"</u>"; for(int i =0; i < Datalist.size()-1;i+=2){ out+=" -> "; out+=Datalist.get(i); ArrayList nodes =
                 * (ArrayList)Datalist.get(i+1); ArrayList [] desArray = new ArrayList[9]; for(int k =0; k < desArray.length;k++){ desArray[k]=new
                 * ArrayList(); } for(int j =0; j < nodes.size();j++){ SemanticNode s = (SemanticNode)nodes.get(j); if(s!=null)
                 * desArray[s.Region-1].add(s); } out+="<br>"; out+=" found in:"+this.simplifyRegionDescription(desArray); out+="<br>"; }
                 * out+="</li>"; } out+="</ul>";
                 */
            }

        }

        return out;
    }

    public static int[] getDistance(int[] PointA, int[] PointB) {
        int[] result = new int[2];
        result[0] = (PointA[0] - PointB[0]) * -1;
        result[1] = (PointA[1] - PointB[1]) * -1;
        return result;

    }

    /**
     * Creates a Class Report on Class with index j, saves it in the SemantiClass currClass and puts it on the
     * SemanticNode
     */
    public void createClassReport(int j) {
        if (!this.SemanticClassesIndex.containsKey(j)) { // Only calculate if we didnt see this class so far
            ArrayList<int[]> UnitswithClassList = this.TestRun.getAllUnitsContainingClass(j);
            SemanticClass currClass = new SemanticClass(j, UnitswithClassList);
            int[] middle = this.TestRun.getClassMeanUnit(j);
            currClass.setMeanPoint(middle);
            // nOw that we know the ClassMiddle, try to give information if the class is connected, if it is centered
            // around the middle point, or so
            // How much of the class is connected by a Distance of 1
            currClass.calculateClassPartitions();
            // Is the Class centred around the Centre of Gravity?
            currClass.calculateCompactness();

            // How many other classes share the same Units and where are the most units of the class located.
            int[] regionmix = new int[NUMBER_OF_REGIONS];
            double[] concentrationnmix = new double[NUMBER_OF_REGIONS];
            int[][] classmix = new int[UnitswithClassList.size()][this.TestRun.getNumberofClasses()];
            for (int i = 0; i < UnitswithClassList.size(); i++) {
                for (int q = 0; q < this.TestRun.getNumberofClasses(); q++) {
                    classmix[i][q] = 0;
                }
            }
            for (int i = 0; i < UnitswithClassList.size(); i++) {
                int[] temp = UnitswithClassList.get(i);
                SemanticNode n = this.getNode(temp);
                if (n != null) {
                    currClass.MemberCount += temp[2];
                    regionmix[n.Region - 1]++;
                    concentrationnmix[n.Region - 1] += temp[2];
                    int[] otherClasses = this.TestRun.getClassesForUnit(temp[0], temp[1]);
                    for (int u = 0; u < otherClasses.length; u++) {
                        if (otherClasses[u] > 0.0) {
                            classmix[i][u]++;
                        }
                    }
                }
            }
            // witch units are on the Class Edges
            ArrayList<SemanticNode> EdgeNodes = new ArrayList<SemanticNode>();
            ArrayList<SemanticNode> SemanticNodeswithClassList = new ArrayList<SemanticNode>();
            for (int i = 0; i < UnitswithClassList.size(); i++) {
                int[] coords = UnitswithClassList.get(i);
                SemanticNode s = this.getNode(coords);
                SemanticNodeswithClassList.add(s);
            }
            for (int x = 0; x < this.XLength; x++) {
                boolean SNEdge = false;
                boolean NSEdge = false;
                for (int y = 0; y < this.YLength; y++) { // Grasp Nodes in N-S Direction
                    int[] coords = new int[2];
                    coords[0] = x;
                    coords[1] = y;
                    SemanticNode n = this.getNode(coords);
                    if (SemanticNodeswithClassList.contains(n)) {
                        if (NSEdge) {
                            EdgeNodes.add(n);
                            NSEdge = false;
                        }
                    } else {
                        NSEdge = true;
                    }
                }
                for (int y = this.YLength - 1; y >= 0; y--) { // Grasp Nodes in S-N Direction
                    int[] coords = new int[2];
                    coords[0] = x;
                    coords[1] = y;
                    SemanticNode n = this.getNode(coords);
                    if (SemanticNodeswithClassList.contains(n)) {
                        if (SNEdge) {
                            if (!EdgeNodes.contains(n)) {
                                EdgeNodes.add(n);
                            }
                            SNEdge = false;
                        }
                    } else {
                        SNEdge = true;
                    }
                }

            }
            currClass.setClassEdges(EdgeNodes);

            for (int y = 0; y < this.YLength; y++) {
                boolean WEEdge = false;
                boolean EWEdge = false;
                for (int x = 0; x < this.XLength; x++) { // Grasp Nodes in W-E Direction
                    int[] coords = new int[2];
                    coords[0] = x;
                    coords[1] = y;
                    SemanticNode n = this.getNode(coords);
                    if (SemanticNodeswithClassList.contains(n)) {
                        if (WEEdge) {
                            if (!EdgeNodes.contains(n)) {
                                EdgeNodes.add(n);
                            }
                            WEEdge = false;
                        }
                    } else {
                        WEEdge = true;
                    }
                }
                for (int x = this.XLength - 1; x >= 0; x--) { // Grasp Nodes in E-W Direction
                    int[] coords = new int[2];
                    coords[0] = x;
                    coords[1] = y;
                    SemanticNode n = this.getNode(coords);
                    if (SemanticNodeswithClassList.contains(n)) {
                        if (EWEdge) {
                            if (!EdgeNodes.contains(n)) {
                                EdgeNodes.add(n);
                            }
                            EWEdge = false;
                        }
                    } else {
                        EWEdge = true;
                    }
                }
            }
            for (int i = 0; i < concentrationnmix.length; i++) {
                concentrationnmix[i] = concentrationnmix[i] / currClass.MemberCount * 100;
            }

            currClass.addRegionMix(regionmix); // In how many of the Regions is the Class present
            currClass.addConcentrationMix(concentrationnmix); // How many Members of the Class are seperated over
            // Regions
            String report = "";
            if (this.type == 1) {
                report = "<h2>" + this.TestRun.datasetInfo.getNameOfClass(j) + "</h2></br><br>" + "The Class "
                        + this.TestRun.datasetInfo.getNameOfClass(j) + " is maintained by " + UnitswithClassList.size()
                        + "units. It has its class middle " + this.getNode(middle).Description + ".";
            }
            report += "Around its Class Center " + String.format("%.2f", currClass.getCompactness() * 100)
                    + " % of the Class Units are located.";

            if (currClass.getCompactness() >= 0 && currClass.getCompactness() <= 0.2) {
                report += " So the Class doesnt take a square-centered shape, and almost every class member is dislocated from its class center. ";
            }
            if (currClass.getCompactness() > 0.2 && currClass.getCompactness() <= 0.4) {
                report += " So one can say that alot of classmembers lie away from its class center. ";
            }
            if (currClass.getCompactness() > 0.4 && currClass.getCompactness() <= 0.6) {
                report += " So one can say that the class has a relatively good density of class members around its center. ";
            }
            if (currClass.getCompactness() > 0.6 && currClass.getCompactness() <= 0.8) {
                report += "So one can say that a good amount of class members arelocated very closely to its center, and that the Class has a very good Compactness. ";
            }
            if (currClass.getCompactness() > 0.8) {
                report += " So one can say that the class is very centered around its Class Middle. ";
            }

            // is the class separated into several parts?
            if (currClass.ClassPartitions.size() > 1) {
                report += "It is divided in " + currClass.ClassPartitions.size()
                        + " partitions. The Percentage of Members is given in the brackets. ";
                int max = Integer.MIN_VALUE;
                int[] trail = null;
                SemanticNode m = null;
                HashMap<Integer, int[]> location = new HashMap<Integer, int[]>();
                for (int z = 0; z < currClass.ClassPartitions.size(); z++) {
                    int temp[] = currClass.ClassPartitions.get(z);
                    SemanticNode n;
                    n = this.getNode(UnitswithClassList.get(temp[0]));
                    // int = n.Region;
                    if (!location.containsKey(n.Region)) {
                        location.put(n.Region, temp);
                    } else {
                        int nodes[] = location.get(n.Region);
                        nodes = concatArrays(temp, nodes);
                        location.put(n.Region, nodes);
                    }
                }
                Iterator<int[]> node_values = location.values().iterator();
                Iterator<Integer> node_key = location.keySet().iterator();
                String classpieces = "";
                int counter = 0;
                while (node_values.hasNext()) {
                    int reg = node_key.next();
                    int[] nodes = node_values.next();
                    counter++;
                    classpieces += counter + ": " + nodes.length + " unit(s) in " + getRegion(reg) + " ("
                            + String.format("%.2f", currClass.concentrationmix[reg - 1]) + "%) ,";
                    if (nodes.length > max) {
                        max = nodes.length;
                        trail = nodes;
                        m = getNode(UnitswithClassList.get(nodes[0]));
                    }
                }
                classpieces = classpieces.substring(0, classpieces.length() - 1);
                report += classpieces;
                int amount = 0;
                for (int c = 0; c < trail.length; c++) {
                    int[] array = UnitswithClassList.get(c);
                    amount += array[2];
                }
                report += ". Whereof the biggest connected Cluster is maintained in the " + getRegion(m.Region)
                        + " containing "
                        + String.format("%.2f", ((double) trail.length / (double) UnitswithClassList.size() * 100))
                        + "% of all of the Class units and "
                        + String.format("%.2f", (double) amount / (double) currClass.MemberCount * 100)
                        + " % of its members. ";
            } else {
                report += "It is fully connected, and contains no seperated blocks. The Distribution of Classmembers among Regions is given by Percentage:<br>";
                for (int f = 0; f < currClass.concentrationmix.length; f++) {
                    if (currClass.concentrationmix[f] > 0) {
                        report += getRegion(f + 1) + " (" + currClass.concentrationmix[f] + "%)";
                    }
                }
            }
            // Now say something where the most units are located
            /*
             * int max = Integer.MIN_VALUE; int Region = 0; boolean seperated = false; int amount =0; for(int e=0; e< regionmix.length;e++){
             * if(regionmix[e] > 0 && regionmix[e]>max){ max = regionmix[e]; Region = e+1; seperated = true; amount++; } } if(seperated)
             * report+="The class is seperated among "
             * +(amount+1)+" Regions of the SOM. Most units ("+(double)max/(double)UnitswithClassList.size()100+"%) are  located "+ "in the ";
             * SemanticNode t = getNode(currClass.CenterPoint); if(Region == t.Region) report+="same Region as the Class Middle."; else
             * report+=SemanticNode.getRegion(Region);
             */

            // where is the furthest ClasssMember?
            SemanticNode t = this.getNode(currClass.getFurthestMember());
            report += "The furthest Class member of " + this.TestRun.datasetInfo.getNameOfClass(j)
                    + " is located in the " + SemanticInterpreterGrid.getRegion(t.Region);
            // Say how Purely the class is
            HashMap<Integer, ArrayList<Integer>> ClassList = new HashMap<Integer, ArrayList<Integer>>();
            int counter = 0;
            boolean pure = false;
            for (int i = 0; i < UnitswithClassList.size(); i++) {
                for (int p = 0; p < this.TestRun.getNumberofClasses(); p++) {
                    if (classmix[i][p] > 0) {
                        if (p != j) {
                            pure = false;
                            if (!ClassList.containsKey(p)) {
                                ArrayList<Integer> l = new ArrayList<Integer>();
                                l.add(i);
                                ClassList.put(p, l);

                            }
                            for (int z = i + 1; z < UnitswithClassList.size(); z++) {// Look for the same classon other
                                // units
                                for (int k = 0; k < classmix[z].length; k++) {
                                    if (p == k && classmix[i][p] > 0 && classmix[z][k] > 0) {
                                        if (!ClassList.containsKey(p)) {
                                            ArrayList<Integer> l = new ArrayList<Integer>();
                                            l.add(z);
                                            ClassList.put(p, l);
                                        } else {
                                            ArrayList<Integer> l = ClassList.get(p);
                                            if (!l.contains(z)) {
                                                l.add(z);
                                            }
                                            ClassList.put(p, l);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (ClassList.size() == 0) {
                                pure = true;
                                counter++;
                            }
                        }
                    }
                }
            }
            // String [] sArray = null;
            int globalSharedMax = -1;
            int globalUnitMax = -1;
            ArrayList<Integer> maximumUnitValues = new ArrayList<Integer>();
            if (ClassList.size() > 0) {
                report += "The Class shares its units with " + ClassList.size() + " other Classes.";
                ArrayList compare = new ArrayList();
                /* Find the maximum of classes that share the same points */
                Iterator<Integer> ii = ClassList.keySet().iterator();
                ArrayList<Integer> check = new ArrayList<Integer>();
                for (int i = 0; i < ClassList.keySet().size(); i++) {
                    int classindex = ii.next();
                    check.add(classindex);
                    ArrayList<Integer> classes = ClassList.get(classindex);
                    globalUnitMax = Math.max(globalUnitMax, classes.size());
                    Iterator<Integer> ia = ClassList.keySet().iterator();
                    while (ia.hasNext()) {
                        int classindex1 = ia.next();
                        if (!check.contains(classindex1)) {
                            ArrayList<Integer> classes1 = ClassList.get(classindex1);
                            compare = this.compareArrayLists(classes, classes1);
                            globalSharedMax = Math.max(globalSharedMax, compare.size());
                        }
                    }
                }
                // Save the maximum values
                ArrayList maximumSharedValues = new ArrayList();
                maximumUnitValues = new ArrayList<Integer>();
                Iterator<Integer> bb = ClassList.keySet().iterator();
                ArrayList<Integer> check1 = new ArrayList<Integer>();
                int biggestInteresctionAmount = 0;
                boolean intersect = false;
                for (int i = 0; i < ClassList.keySet().size(); i++) {
                    int classindex = bb.next();
                    check1.add(classindex);
                    ArrayList<Integer> classes = ClassList.get(classindex);
                    if (classes.size() == globalUnitMax) {
                        maximumUnitValues.add(classindex);
                    }
                    Iterator<Integer> ia = ClassList.keySet().iterator();
                    while (ia.hasNext()) {
                        int classindex1 = ia.next();
                        if (!check1.contains(classindex1)) {
                            ArrayList<Integer> classes1 = ClassList.get(classindex1);
                            compare = this.compareArrayLists(classes, classes1);
                            if (compare.size() == globalSharedMax) {
                                // FIXME: replace arraylist with some structure/class
                                ArrayList[] temp = new ArrayList[3];
                                for (int q = 0; q < temp.length; q++) {
                                    temp[q] = new ArrayList();
                                }
                                temp[0].add(classindex);
                                temp[1].add(classindex1);
                                if (compare.size() > 0) {// we have shared nodes among classes
                                    temp[2].add(compare);
                                }
                                biggestInteresctionAmount = compare.size();
                                if (compare.size() > 0) {
                                    intersect = true;
                                }
                                maximumSharedValues.add(temp);
                            }
                        }
                    }
                }
                if (ClassList.size() > 1) {// we have more than one shared class, make more calculations...
                    ArrayList Container = new ArrayList();
                    ArrayList[] sharedClasses = new ArrayList[2]; // Contains the classes on [0] and their shared units
                    // on [1];
                    sharedClasses[0] = new ArrayList();
                    sharedClasses[1] = new ArrayList();
                    int c = 0;

                    for (int i = 0; i < maximumSharedValues.size(); i++) {
                        ArrayList[] temp = (ArrayList[]) maximumSharedValues.get(i);
                        if (!sharedClasses[0].contains(temp[0].get(0))) {
                            sharedClasses[0].add(temp[0].get(0));
                            if (maximumSharedValues.size() > 1) { // if we have enough class entries to possible build a
                                // trail
                                // sharedClasses[0] =
                                // getclassTrail(maximumSharedValues,(Integer)temp[1].get(0),sharedClasses[0]);
                                sharedClasses = getclassTrail(maximumSharedValues);
                            } else {
                                sharedClasses[0].add(temp[1].get(0)); // we have just 2 classes that share the same
                                // units
                            }
                            sharedClasses[1] = ((ArrayList[]) maximumSharedValues.get(0))[2];
                            c += sharedClasses[0].size();
                            Container.add(sharedClasses);
                        }
                    }
                    // create a report for all other found classes (used for more comfort in the output)
                    if (maximumUnitValues.size() > 1) {
                        report += "Among these, there are the Classes ";
                    } else {
                        report += "Among these, there is the Class ";
                    }
                    for (int i = 0; i < maximumUnitValues.size(); i++) {
                        report += this.TestRun.datasetInfo.getNameOfClass(maximumUnitValues.get(i)) + ", ";
                    }
                    report += "that can be found to the largest Extend (" + globalUnitMax + " units) on "
                            + this.TestRun.datasetInfo.getNameOfClass(j) + "s units.";
                    String classes = "";
                    if (intersect) {
                        for (int i = 0; i < Container.size(); i++) {
                            int Count = 0;
                            ArrayList[] l = (ArrayList[]) Container.get(i);
                            for (int o = 0; o < l[0].size(); o++) {
                                for (int k = 0; k < maximumUnitValues.size(); k++) {
                                    if (l[0].get(o) == maximumUnitValues.get(k)) {
                                        Count++;
                                    }
                                }
                                if (o == l[0].size() - 1) {// G
                                    // ArrayList h = ClassList.get(l[0].get(o));
                                    classes += this.TestRun.datasetInfo.getNameOfClass((Integer) l[0].get(o))
                                            + " intersecting " + this.TestRun.datasetInfo.getNameOfClass(j) + " ";
                                    ArrayList interS = currClass.getIntersectionDirections(sharedClasses[1]);
                                    for (int n = 0; n < interS.size(); n++) {
                                        classes += interS.get(n) + ", ";
                                    }
                                    classes += "of its Class Center in a total amount of " + biggestInteresctionAmount
                                            + " units";
                                } else {
                                    ArrayList h = ClassList.get(l[0].get(o));
                                    classes += this.TestRun.datasetInfo.getNameOfClass((Integer) l[0].get(o)) + ", ";
                                }
                            }
                            if (Count > 0) {
                                if (Count == 1) {
                                    report += "This Class is also Part of the biggest Interesecting Area.";
                                } else {
                                    report += Math.round((double) Count / (double) maximumUnitValues.size() * 100)
                                            + " % of these " + maximumUnitValues.size()
                                            + " Classes are also Part of a the biggest Interesecting Area.";
                                }
                            }

                        }
                        report += "Classes sharing this intersection Area are: " + classes;
                    } else {
                        // report+=" Out of the "+ClassList.size()+" other classes, there are"+c+" other Classes that could be found in the "+Container.size()+" biggest Intersection Area(s) of "+this.TestRun.datasetInfo.getNameOfClass(j)+",having a maximum amount of shared intersecting Units of "+globalSharedMax+".";
                        report += " But they cannot be found having any Intersection area with each other.";
                    }
                    // HIER BACKUP EINF�GEN
                    if (pure) {
                        report += "There are also " + counter + " units of " + UnitswithClassList.size()
                                + ",that are perfectly seperated, and dont contain any other Classes on them.";
                    }

                } else {
                    report += " The Class shares its units with one other Class: ";
                    Iterator<Integer> iu = ClassList.keySet().iterator();
                    while (iu.hasNext()) {
                        int classindex = iu.next();
                        report += this.TestRun.datasetInfo.getNameOfClass(classindex);
                    }
                    report += ". Both Classes share a total amount of " + globalUnitMax + " with each other";
                }
            } else {
                report += " The Class doesnt have any other Classes on its units, it is therefore perfectly seperated.";
            }
            currClass.setSharedClasses(ClassList.size());
            currClass.setReport(report);
            for (int i = 0; i < UnitswithClassList.size(); i++) { // Save the class on the semantic nodes
                int[] temp = new int[2];
                temp[0] = UnitswithClassList.get(i)[0];
                temp[1] = UnitswithClassList.get(i)[1];
                SemanticNode sNode = this.getNode(temp);
                sNode.addClass(currClass);
                currClass.addNode(sNode);
            }
            putClassinContainer(currClass);
            for (int i = 0; i < maximumUnitValues.size(); i++) {
                if (!this.SemanticClassesIndex.containsKey(maximumUnitValues.get(i))) {
                    this.createClassReport(maximumUnitValues.get(i));
                }
            }
        } else {
            // SemanticClass currClass = (SemanticClass)this.SemanticClasses.get(j); //NO Need to do calculations twice
            // sNode.addClass(currClass);
        }
    }

    /** helper method, cuts a double array from this.units defined by X and Y */
    public Unit[] cutArray(int x1, int x2, int y1, int y2) {
        Unit[] result = new Unit[(x2 - x1 + 1) * (y2 - y1 + 1)];
        int index = 0;
        for (int i = y1, l = 0; i < y2 + 1; i++, l++) {
            for (int j = x1, k = 0; j < x2 + 1; j++, k++) {
                int pointer = i * this.XLength + j;
                result[index] = this.units[pointer];
                index++;
            }
        }
        return result;
    }

    /** returns the Semantic Node for given coordinates */
    private SemanticNode getNode(int[] c) {
        SemanticNode node = null;
        int[] coords;
        for (int i = 0; i < this.YLength; i++) {
            for (int j = 0; j < this.XLength; j++) {
                if (this.labels[j][i] != null) {
                    coords = this.labels[j][i].realCoordinates;
                    if (coords[0] == c[0] && coords[1] == c[1]) {
                        node = this.labels[j][i];
                    }
                }
            }
        }
        if (node != null) {
            if (node.Region == 0) {
                node = this.fatherGrid.getNode(c);
            }
        }
        return node;
    }

    /**
     * Compares two array lists and return how many elements are equal<br/>
     * FIXME: move to a generic utility class
     */
    private ArrayList<Integer> compareArrayLists(ArrayList<Integer> a1, ArrayList<Integer> a2) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < a1.size(); i++) {
            for (int j = 0; j < a2.size(); j++) {
                if (a1.get(i).equals(a2.get(j))) {
                    result.add(a1.get(i));
                }
            }
        }
        return result;
    }

    /** Goes through the given list and tries to see which classes are connected */
    private ArrayList[] getclassTrail(ArrayList l) {
        ArrayList[] out = new ArrayList[2];
        out[0] = new ArrayList();
        out[1] = new ArrayList();
        for (int i = 0; i < l.size(); i++) {
            ArrayList[] temp = (ArrayList[]) l.get(i);
            ArrayList class1 = temp[0];
            ArrayList class2 = temp[1];
            ArrayList units = temp[2];
            if (!out[0].contains(class1.get(0))) {
                out[0].add(class1.get(0));
            }
            if (!out[0].contains(class2.get(0))) {
                out[0].add(class2.get(0));
            }
            for (int j = 0; j < units.size(); j++) {
                if (!out[1].contains(units.get(j))) {
                    out[1].add(units.get(j));
                }
            }
        }
        return out;
    }

    /*
     * private ArrayList getclassTrail(ArrayList <ArrayList[]> l,int lookfor,ArrayList Trail){ for(int i =0; i < l.size();i++){ ArrayList [] temp =
     * l.get(i); if(lookfor==(Integer)temp[0].get(0)){ if(!Trail.contains((Integer)temp[0].get(0))) Trail.add((Integer)temp[0].get(0));
     * if(!Trail.contains((Integer)temp[1].get(0))) Trail.add((Integer)temp[1].get(0)); Trail = getclassTrail(l,(Integer)temp[1].get(0),Trail); } }
     * return Trail; }
     */
    /**
     * Used if the Grid wasn't made from a square Grid, so we use the Class Descriptions from a squared one in
     * SOMDEscriptionHTLM
     */
    public void setFatherGrid(SemanticInterpreterGrid grid) {
        this.fatherGrid = grid;
    }

    /** Returns a list containing all connect/unconnected parts of the empty units */
    public ArrayList<ArrayList<SemanticNode>> getEmptyNodeTrail(ArrayList<SemanticNode> EmptyNodes) {
        boolean[] used = new boolean[EmptyNodes.size()];
        Arrays.fill(used, false);
        ArrayList<SemanticNode> temptrail = new ArrayList<SemanticNode>();
        ArrayList<ArrayList<SemanticNode>> lookup = new ArrayList<ArrayList<SemanticNode>>();
        for (int i = 0; i < EmptyNodes.size(); i++) {
            SemanticNode s1 = EmptyNodes.get(i);
            if (used[i] == false) {
                temptrail = new ArrayList<SemanticNode>();
                temptrail.add(s1);
                lookup.add(temptrail);
            }
            for (int j = 0; j < EmptyNodes.size(); j++) {
                SemanticNode s2 = EmptyNodes.get(j);
                int[] distance = SemanticInterpreterGrid.getDistance(s1.realCoordinates, s2.realCoordinates);
                if (Math.abs(distance[0]) <= 1 && Math.abs(distance[1]) <= 1) {
                    for (int u = 0; u < lookup.size(); u++) {
                        ArrayList<SemanticNode> temp = lookup.get(u);
                        if (temp.contains(s1) && !temp.contains(s2)) {
                            temp.add(s2);
                            used[j] = true;
                        }
                    }
                }
            }
        }
        return lookup;

        /*
         * if(index < EmptyNodes.size()){ boolean found = false; boolean process = true; SemanticNode s1 = EmptyNodes.get(index);
         * if(!temptrail.contains(s1)) temptrail.add(s1); for(int j =0; j < EmptyNodes.size();j++){ SemanticNode s2 = EmptyNodes.get(j); int []
         * distance = this.getDistance(s1.realCoordinates, s2.realCoordinates); if(Math.abs(distance[0]) <=1 && Math.abs(distance[1]) <=1 &&
         * !s1.equals(s2)){ for(int u = 0; u < lookup.size();u++){ ArrayList temp = (ArrayList)lookup.get(u); if(temp.contains(s1) &&
         * !temp.contains(s2)){ temp.add(s2); used[j]=true; process = false; } } if(!temptrail.contains(s1) && used[index]==false && process){
         * temptrail.add(s1); used[index]=true; found=true; } if(!temptrail.contains(s2) && used[j]==false && process){ temptrail.add(s2);
         * used[j]=true; found=true; } if(found) lookup = getEmptyNodeTrail(j,EmptyNodes,temptrail,lookup,used); } } if(found==false &&
         * !lookup.contains(temptrail) && temptrail.size() >0 && process){ lookup.add(temptrail); for(int i =0; i < used.length;i++){
         * if(used[i]==false) lookup = getEmptyNodeTrail(i,EmptyNodes,new ArrayList(),lookup,used); } } } return lookup;
         */

    }

    /**
     * Sort the Unit array according to the Coordinates<br>
     * FIXME: refactor this with some generic java sorting algorithm, implementing a special {@link Comparator} first if
     * needed
     */
    public static Unit[] sortNodes(Unit[] units) {
        boolean[] flagsY = new boolean[units.length];
        Arrays.fill(flagsY, false);
        int pointer = 0;
        Unit[] out = new Unit[units.length];
        while (pointer < units.length) {
            int counter = 0;
            int miny = Integer.MAX_VALUE;
            ArrayList<Unit> minYvalues = new ArrayList<Unit>();
            for (int i = 0; i < units.length; i++) { // Search for minimum
                if (units[i].getYPos() < miny && flagsY[i] != true) {
                    miny = units[i].getYPos();
                }
            }

            for (int i = 0; i < units.length; i++) { // add minimal X values to array
                if (units[i].getYPos() == miny) {
                    minYvalues.add(units[i]);
                    flagsY[i] = true;
                }
            }
            boolean[] flagsX = new boolean[units.length];
            Arrays.fill(flagsX, false);
            for (int t = 0; t < minYvalues.size(); t++) { // in this array look for minimal Y values
                int minx = Integer.MAX_VALUE;
                for (int i = 0; i < minYvalues.size(); i++) {
                    Unit u = minYvalues.get(i);
                    if (u.getXPos() < minx && flagsX[i] != true) {
                        minx = u.getXPos();
                        counter = i;
                    }
                }
                flagsX[counter] = true; // set minimal pos to true
                for (int i = 0; i < minYvalues.size(); i++) {
                    Unit u = minYvalues.get(i);
                    if (u.getXPos() == minx) {
                        out[pointer] = u;
                        pointer++;
                    }
                }
            }
        }
        return out;
    }

    /**
     * adds a class from ClassCointainer to this SemanticClass Hashmap, used if the Grid is non Regular
     * 
     * @param j classindex
     */
    public void addClasstoGrid(int j) {
        ArrayList<int[]> UnitswithClassList = this.TestRun.getAllUnitsContainingClass(j);

        SemanticClass sem = fatherGrid.getClassfromGrid(j);
        sem.MemberCount = 0; // Reset the amount of Members, taken from another Grid
        for (int i = 0; i < UnitswithClassList.size(); i++) {
            int[] temp = UnitswithClassList.get(i);
            SemanticNode n = this.getNode(temp);
            if (n != null) {
                sem.MemberCount += temp[2];
            }
        }
        this.putClassinContainer(sem);
    }

    /**
     * Concatenates two arrays. <br>
     * FIXME: use {@link ArrayUtils#addAll(int[], int[])} instead
     */
    public int[] concatArrays(int[] a, int[] b) {
        int size = a.length + b.length;
        int[] result = new int[size];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i];
        }
        for (int i = 0, j = a.length; i < b.length; i++, j++) {
            result[j] = b[i];
        }
        return result;
    }

    /** returns a detailed information on the classes according to the parameters entered during creation */
    /*
     * public String getEPwantedClassProperties(){ String out=""; EditableReportProperties EP = this.TestRun.datasetInfo.getEP(); boolean
     * wanted_distortion = false; boolean wanted_compactness = false; if(EP.getWantedClassCompactness()<0|| EP.getWantedClassDistortion() <0){
     * out+="According to the Parameters entered during ReportCreation, a more detailed List about Class "; if(EP.getWantedClassDistortion() <0 &&
     * EP.getWantedClassCompactness() <0){ out+="Compactness & Distortion"; wanted_distortion=true; wanted_compactness = true; } else{
     * if(EP.getWantedClassDistortion()<0){ out+="Distortion"; wanted_distortion=true; } if(EP.getWantedClassCompactness()<0){ out+="Compactness";
     * wanted_compactness=true; } } out+=" is given. The following Table gives a detailed View on Classes in the Region with ";
     * if(wanted_compactness){ out+=EP.getWantedClassCompactness()+" % Compactness"; if(wanted_distortion)
     * out+=" and "+EP.getWantedClassDistortion()+" % Distortion."; else out+="."; } if(wanted_distortion){
     * out+=EP.getWantedClassDistortion()+" % Distortion"; } if(this.type==1){ // HTML REPORT } else{ //LATEX Report } } return out; }
     */
    /**
     * Returns the String Name for the region with index i<br/>
     * FIXME: refactor this with a hashmap or similar
     */
    public static String getRegion(int i) {
        switch (i) {
            case 1:
                return "North-West Quadrant ";

            case 2:
                return "Area between the North-West and North-East Quadrant ";

            case 3:
                return "North-East Quadrant ";

            case 4:
                return "Area between the north-west and  South-West Quadrant";

            case 5:
                return "Central Middle of the Map Center";

            case 6:
                return "Area between the north-east and  South-East Quadrant";

            case 7:
                return "South-West Quadrant ";

            case 8:
                return "Area between the South-west and  South-East Quadrant";

            case 9:
                return "South-East Quadrant";

            default:
                return "Unknown Region ";
        }
    }

    /** Puts the class in the right HashMap, and checks for wanted EditableProperties */
    public void putClassinContainer(SemanticClass cl) {
        this.SemanticClassesIndex.put(cl.index, cl);
        if (EP.getMINCompactness() != -1 || EP.getMAXCompactness() != -1) { // Check ob die Klasse die
            // EPvorraussetzungen erf�llt
            for (int i = 0; i < cl.regionmix.length; i++) {
                // Check for MIN Concentration
                boolean ok = true;
                if (EP.getMINCompactness() != -1 || EP.getMAXCompactness() != -1) {
                    if (!cl.matchesCompactnessRequirements(this.EP)) {
                        ok = false;
                    }
                }
                if (cl.regionmix[i] > 0 && ok) {
                    this.SemanticClasses[i].put(cl.index, cl);
                }
            }
        } else {
            for (int i = 0; i < cl.regionmix.length; i++) {
                if (cl.regionmix[i] > 0) {
                    this.SemanticClasses[i].put(cl.index, cl);
                }
            }
        }
    }

    public SemanticClass getClassfromGrid(int index) {
        /*
         * while(search){ for(int i =0; i < this.SemanticClasses.length;i++){ cl = (SemanticClass)SemanticClasses[i].get(index); if(cl!=null){ search
         * = false; break; } } }
         */
        SemanticClass cl = this.SemanticClassesIndex.get(index);
        return cl;

    }

    /** Gets the specific Classes for the Cluster Area, Used if this.fatherGrid != null & Index = 0 */
    public HashMap<Integer, SemanticClass> getClusterLevelClasses() {
        HashMap<Integer, SemanticClass> out = new HashMap<Integer, SemanticClass>();
        int x1 = -1;
        int y1 = -1;
        for (SemanticNode[] n : this.labels) {
            for (int k = 0; k < n.length && n[k] != null; k++) {
                x1 = n[k].realCoordinates[0];
                y1 = n[k].realCoordinates[1];
                for (SemanticNode[] m : this.fatherGrid.labels) {
                    for (SemanticNode element : m) {
                        int x2 = element.realCoordinates[0];
                        int y2 = element.realCoordinates[1];
                        if (x1 == x2 && y1 == y2 && element.Classes != null) {
                            for (int u = 0; u < element.Classes.size(); u++) {
                                SemanticClass c = element.Classes.get(u);
                                boolean ok = true;
                                if (this.EP.getMAXCompactness() != -1 || this.EP.getMINCompactness() != -1) {
                                    if (!c.matchesCompactnessRequirements(this.EP)) {
                                        ok = false;
                                    }
                                }
                                if (!out.containsKey(c.index) && ok) {
                                    out.put(c.index, c);
                                }
                            }
                        }
                    }
                }
            }
        }
        return out;
    }

    /** Returns the Number of all shared Classes within this Grid */
    public int getNumberofSharedClasses(boolean clusterarea) {
        int out = 0;
        ArrayList<Integer> counter = new ArrayList<Integer>();
        if (clusterarea) {
            for (HashMap<Integer, SemanticClass> semanticClasse : this.fatherGrid.SemanticClasses) {
                Iterator<SemanticClass> ii = semanticClasse.values().iterator();
                while (ii.hasNext()) {
                    SemanticClass c = ii.next();
                    if (!counter.contains(c.index)) {
                        out += c.SharedClasses;
                        counter.add(c.index);
                    }
                }
            }
        } else {
            for (HashMap<Integer, SemanticClass> semanticClasse : this.SemanticClasses) {
                Iterator<SemanticClass> ii = semanticClasse.values().iterator();
                while (ii.hasNext()) {
                    SemanticClass c = ii.next();
                    if (!counter.contains(c.index)) {
                        out += c.SharedClasses;
                        counter.add(c.index);
                    }
                }
            }
        }

        return out;
    }

    /**
     * Prints a report on all selected Quality Measures, and adds a picture Frame if a visualization is available
     * qualifiers is a String array, containing all Names from the quality measures
     */
    public String printQualityMeasureReport(String qualifier) {
        String out = "";
        String unitCoord = "";

        // Object k;
        out += "<ul>\n";
        /* MAP SECTION */
        String curr_desc = TextualDescriptionProvider.getScientificDescription(qualifier);
        out += "Information on <u onclick=\"javascript:showVisualisationDescriptions('" + curr_desc
                + "')\"><font color =\"blue\">" + qualifier + "</font></u> of  the SOM.<br>";
        if (this.qm.getMapQualities(qualifier) != null) {
            out += "The Map Quality Measure is the average from the sum of all Units with mapped input";
            out += "<br><li><span class=\"header2\">Map Quality Measure:"
                    + String.format("%.2f", this.qm.getMapQualities(qualifier)) + "</span>";
            out += "</ul></li>";
        }
        /* UNIT SECTION */
        // min unit/max unit quality measure
        Unit[] min = this.qm.getMinUnit(qualifier, null);
        Unit[] max = this.qm.getMaxUnit(qualifier, null);
        if (min != null && max != null) {
            out += " Underneath MIN / MAX Values of " + qualifier
                    + "are listed. The Units Coordinates are given in Brackets";
            out += "<br>";
            out += "<li><span class=\"header2\">Unit Quality Measures:</span><ul>";
            for (int j = 0; j < min.length; j++) {
                unitCoord += "[" + min[j].getXPos() + "," + min[j].getYPos() + "]";
                if (j > 0 && j < min.length - 1) {
                    unitCoord += " , ";
                }
            }
            out += "<li>Minimal unit quality measure: " + String.format("%.2f", this.qm.getUnitMINQualities(qualifier))
                    + " (" + unitCoord + ")" + "</li>\n";
            unitCoord = "";
            for (int j = 0; j < max.length; j++) {
                unitCoord += "[" + max[j].getXPos() + "," + max[j].getYPos() + "]";
                if (j > 0 && j < max.length - 1) {
                    unitCoord += " , ";
                }
            }
            out += "<li>Maximal unit quality measure: " + String.format("%.2f", this.qm.getUnitMAXQualities(qualifier))
                    + " (" + unitCoord + ")" + "</li>\n";
            out += "</ul>";
            out += "The Unit Distribution gives the Distribution of Quality Measure Values on the Map. Percentage Values are calculated without Units with no Input";
            out += "<br>";
            out += "<li><span class=\"header2\">Unit Distribution Description:</span><ul>";
            /*
             * out += "<li>"+this.qm.getClassIdentifier(5)+": "+String.format("%.2f",this.qm.getClassifiedUnitsPerc(qualifier, 5))+"%"; out +=
             * printQMRegionDistribution(qualifier,5); out += "</li>\n"; out +=
             * "<li>"+this.qm.getClassIdentifier(4)+": "+String.format("%.2f",this.qm.getClassifiedUnitsPerc(qualifier, 4))+"%"; out +=
             * printQMRegionDistribution(qualifier,4); out += "</li>\n"; out +=
             * "<li>"+this.qm.getClassIdentifier(3)+": "+String.format("%.2f",this.qm.getClassifiedUnitsPerc(qualifier, 3))+"%"; out +=
             * printQMRegionDistribution(qualifier,3); out += "</li>\n"; out +=
             * "<li>"+this.qm.getClassIdentifier(2)+": "+String.format("%.2f",this.qm.getClassifiedUnitsPerc(qualifier, 2))+"%"; out +=
             * printQMRegionDistribution(qualifier,2); out += "</li>\n"; out +=
             * "<li>"+this.qm.getClassIdentifier(1)+": "+String.format("%.2f",this.qm.getClassifiedUnitsPerc(qualifier, 1))+"%"; out +=
             * printQMRegionDistribution(qualifier,1); out += "</li>\n"; out += "</ul>";
             */
            /* Try to give a reporton the distribution of values */
            double maxx = Double.MIN_VALUE;
            int mostCommonValue = 0;
            for (int i = 1; i <= 5; i++) {
                double qPerc = (double) this.qm.getNumberOfClassifiedUnits(qualifier, i)
                        / (double) this.TestRun.getSOMDimensions();
                if (qPerc > maxx) {
                    maxx = qPerc;
                    mostCommonValue = i;
                }
            }

            /*
             * What of the values are outliers.. Since we have 5 classes of strongness, we can assume that if there is a percentage of occurence of
             * 0.0% the direct class above, or below is an outlier
             */
            ArrayList<Integer> outliers = new ArrayList<Integer>();
            for (int i = 1; i <= 5; i++) {
                double qPerc = (double) this.qm.getNumberOfClassifiedUnits(qualifier, i)
                        / (double) this.TestRun.getSOMDimensions();
                if (qPerc == 0.0) {
                    if (i == 4) {
                        outliers.add(i + 1);
                    }
                    if (i == 2) {
                        outliers.add(i - 1);
                    }
                }

            }
            String high = "";
            String low = "";
            String highRegionDesc = "";
            String lowRegionDesc = "";
            ArrayList[] HighRegArray = new ArrayList[9];
            ArrayList[] LowRegArray = new ArrayList[9];
            for (int i = 0; i < HighRegArray.length; i++) {
                HighRegArray[i] = new ArrayList();
                LowRegArray[i] = new ArrayList();
            }
            int HighDN = this.qm.getNumberOfClassifiedUnits(qualifier, 5);
            int LowDN = this.qm.getNumberOfClassifiedUnits(qualifier, 1);
            double HighDP = (double) this.qm.getNumberOfClassifiedUnits(qualifier, 5)
                    / (double) this.TestRun.getSOMDimensions() * 100;
            double LowDP = (double) this.qm.getNumberOfClassifiedUnits(qualifier, 1)
                    / (double) this.TestRun.getSOMDimensions() * 100;
            /* Accumulate High / Low Values+(given outliers) to Strings */
            /* HIGH VALUES */

            if (!outliers.contains(5)) {
                high = " and " + this.qm.getClassIdentifier(4);
                HighDN += this.qm.getNumberOfClassifiedUnits(qualifier, 4);
                HighDP += (double) this.qm.getNumberOfClassifiedUnits(qualifier, 4)
                        / (double) this.TestRun.getSOMDimensions() * 100;
                ArrayList<SemanticNode>[] veryStrong = getRegionOccurance(qualifier, 5);
                ArrayList<SemanticNode>[] Strong = getRegionOccurance(qualifier, 4);
                for (int i = 0; i < NUMBER_OF_REGIONS; i++) {
                    if (veryStrong[i].size() > 0 || Strong[i].size() > 0) {
                        HighRegArray[i] = mergeArrayList(veryStrong[i], Strong[i]);
                    }
                }
                highRegionDesc = simplifyRegionDescription(HighRegArray);
            } else {
                highRegionDesc = simplifyRegionDescription(getRegionOccurance(qualifier, 5));
            }
            /* LOW VALUES */
            if (!outliers.contains(1)) {
                low = " and " + this.qm.getClassIdentifier(2);
                LowDN += this.qm.getNumberOfClassifiedUnits(qualifier, 2);
                LowDP += (double) this.qm.getNumberOfClassifiedUnits(qualifier, 2)
                        / (double) this.TestRun.getSOMDimensions() * 100;
                ArrayList<SemanticNode>[] veryWeak = getRegionOccurance(qualifier, 1);
                ArrayList<SemanticNode>[] Weak = getRegionOccurance(qualifier, 2);
                for (int i = 0; i < NUMBER_OF_REGIONS; i++) {
                    if (veryWeak[i].size() > 0 || Weak[i].size() > 0) {
                        LowRegArray[i] = mergeArrayList(veryWeak[i], Weak[i]);
                    }
                }
                lowRegionDesc = simplifyRegionDescription(LowRegArray);
            } else {
                lowRegionDesc = simplifyRegionDescription(getRegionOccurance(qualifier, 1));
            }

            out += HighDN + " (" + (int) Math.round(HighDP) + " %) of all the Units have "
                    + this.qm.getClassIdentifier(5) + high + " Values.";
            out += "They can be found in: " + highRegionDesc + " ";
            if (outliers.contains(5) == true) {
                out += "We can consider these values as being outliers, since there are no units on the map with "
                        + this.qm.getClassIdentifier(4) + " Values.";
            }
            out += "Most Units contain ";
            // If mostf common value was already among v. strong, strong
            if (this.qm.getClassIdentifier(5).equals(this.qm.getClassIdentifier(mostCommonValue))
                    || this.qm.getClassIdentifier(4).equals(this.qm.getClassIdentifier(mostCommonValue))) {
                out += "also " + this.qm.getClassIdentifier(mostCommonValue) + " Values.";
            } else {
                out += this.qm.getClassIdentifier(mostCommonValue) + " Values , located "
                        + simplifyRegionDescription(getRegionOccurance(qualifier, mostCommonValue)) + " ";
            }
            // If mostf common value was already among v. weak, weak
            if (this.qm.getClassIdentifier(1).equals(this.qm.getClassIdentifier(mostCommonValue))
                    || this.qm.getClassIdentifier(2).equals(this.qm.getClassIdentifier(mostCommonValue))) {
                out += this.qm.getClassIdentifier(1) + low + " Values can be found on " + LowDN + " ("
                        + (int) Math.round(LowDP) + " %) of all Units.";
            } else {
                out += this.qm.getClassIdentifier(1) + low + " Values can be found on " + LowDN + "("
                        + (int) Math.round(LowDP) + " %) of all Units.";
                out += "They can be found in: " + lowRegionDesc + ". ";
            }
            if (outliers.contains(1) == true) {
                out += "We can consider these values as being outliers, since there are no units on the map with "
                        + this.qm.getClassIdentifier(2) + " Values.";
            }

            /* MAXIMUM CIRCUMSTANCES original_configuration != null, when teh configuration for qualifier is already defined */
            QMConfigurationProfile original_configuration = this.qm.getOriginalConfiguration(qualifier, type);
            if (original_configuration != null) {
                out += this.testQM(null, qualifier, HighRegArray, 1);
            }

        }
        if (this.qm.createQualityMeasureImage(imgDir, qualifier + ".jpg", qualifier)) {
            out += "<p class=\"header\">Visualization of the " + qualifier + " image:</p>";

            out += "<img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_" + this.TestRun.getRunId() + "_" + qualifier
                    + ".jpg\" alt=\"visualization of quality measure" + qualifier + "\"/>";
            // add the palette
            if (qualifier.equals("Quantization Error") || qualifier.equals("Mean Quantization Error")
                    || qualifier.equals("Silhouette Value")) {
                int index = 0; // 1st palette
                out += "<br>";
                this.TestRun.createPaletteImage(imgDir, "palette_" + index + ".jpg",
                        "Cartography Color, 128 Gradient (SOMToolbox 0.4.x)");
                out += "<img src=\"" + SOMDescriptionHTML.imgSubdir + "/" + "palette_" + index + ".jpg"
                        + "\" alt=\"visualization of the palette " + index + "\"/>";
            }

        }

        out += "</ul>";
        if (this.qm.UnitQualityMeasureNames.contains(qualifier)) { // && this.TestRun.datasetInfo.classInfoAvailable())
            // {
            if (EP.getMINCompactness() != -1 || EP.getMAXCompactness() != -1) {
                out += " According to the specified User input Preferences, only those classes are listed, that match the folowing conditions: <br>";
            }
            if (EP.getMINCompactness() != -1) {
                out += "<li>Min. Compactness:" + EP.getMINCompactness() + " %.</li>";
            }
            if (EP.getMAXCompactness() != -1) {
                out += "<li>Max. Compactness:" + EP.getMAXCompactness() + " %.</li>";
            }
            out += this.createQMComparisonReportonGrid(qualifier, 0); // Create a QM Comparison reporton qualifier and
            // the whole map
        }
        return out;
    }

    /** Return an ArrayList with the SemanticNodes of ocurrances for the given quality measure and class */
    public ArrayList<SemanticNode>[] getRegionOccurance(String Name, int clss) {
        ArrayList<SemanticNode>[] regions = new ArrayList[9];
        for (int i = 0; i < regions.length; i++) {
            regions[i] = new ArrayList<SemanticNode>();
        }
        int[][] values = this.qm.getClassifiedUnits(Name);
        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values[x].length; y++) {
                if (values[x][y] == clss) { // classified as clss
                    int[] coords = new int[2];
                    coords[0] = x;
                    coords[1] = y;
                    SemanticNode node = this.getNode(coords);
                    regions[node.Region - 1].add(node);
                }
            }
        }
        return regions;
    }

    /**
     * Returns a simplified Region description (i.e region 3,6,9 lie all on the right side of the grid <br>
     * FIXME: refactor this
     */
    public String simplifyRegionDescription(ArrayList[] regions) {

        String out = "";
        boolean[] Used = new boolean[regions.length];
        Arrays.fill(Used, false);
        boolean west = false;
        boolean south = false;
        boolean north = false;
        boolean east = false;

        ArrayList<String> temp = new ArrayList<String>();
        ArrayList<Integer> globalMAX = this.getMaxRegionValue(regions, null);
        ArrayList<Integer> intersectGlobalMax = new ArrayList<Integer>();
        // try to cover all cases
        // Vertical
        if (regions[0].size() > 0 && regions[1].size() > 0 && regions[2].size() > 0) {
            Integer[] wantedRegions = { 0, 1, 2 };
            north = true;
            ArrayList<Integer> max = this.getMaxRegionValue(regions, wantedRegions);
            ArrayList<Integer> intersect = new ArrayList<Integer>();
            ArrayList<Integer> globMax = new ArrayList<Integer>();
            for (int i = 0; i < max.size(); i++) {
                for (int j = 0; j < globalMAX.size(); j++) {
                    if (max.get(i) == globalMAX.get(j)) {
                        globMax.add(max.get(i));
                        intersect.add(max.get(i));
                        intersectGlobalMax.add(max.get(i));
                    }
                }
            }
            for (int i = 0; i < intersect.size(); i++) {
                Integer index_to_remove = intersect.get(i);
                max.remove(index_to_remove);
            }

            String s = "In the whole North ";
            if (globMax.size() > 0) {
                s += "with the highest concentration (global Maximum) ";
                for (int z = 0; z < globMax.size(); z++) {
                    if (z < globMax.size() - 1) {
                        s += getRegion(globMax.get(z) + 1) + "(" + regions[globMax.get(z)].size() + ") and ";
                    } else {
                        s += getRegion(globMax.get(z) + 1) + "(" + regions[globMax.get(z)].size() + ")";
                    }
                }
                s += ".";
            }
            if (max.size() > 0) {
                if (s.charAt(s.length() - 1) == '.') {
                    s = s.substring(0, s.length() - 1);
                    s += " and a weaker concentration ";
                    for (int v = 0; v < max.size(); v++) {
                        if (!globMax.contains(max.get(v))) {
                            if (v < max.size() - 1) {
                                s += getRegion(max.get(v) + 1) + "(" + regions[max.get(v)].size() + ") and ";
                            } else {
                                s += getRegion(max.get(v) + 1) + "(" + regions[max.get(v)].size() + ")";
                            }
                        }
                    }
                    s += ".";
                } else {
                    s += " with the highest regional concentration (local Maximum) in ";
                    for (int k = 0; k < max.size(); k++) {
                        if (k < max.size() - 1) {
                            s += getRegion(max.get(k) + 1) + "(" + regions[max.get(k)].size() + ") and ";
                        } else {
                            s += getRegion(max.get(k) + 1) + "(" + regions[max.get(k)].size() + ")";
                        }
                    }
                    s += ".";
                }
            }
            Used[0] = true;
            Used[1] = true;
            Used[2] = true;
            temp.add(s);
        }
        if (regions[0].size() > 0 && regions[3].size() > 0 && regions[6].size() > 0) {
            Integer[] wantedRegions = { 0, 3, 6 };
            west = true;
            ArrayList<Integer> max = this.getMaxRegionValue(regions, wantedRegions);
            ArrayList<Integer> intersect = new ArrayList<Integer>();
            ArrayList<Integer> globMax = new ArrayList<Integer>();
            for (int i = 0; i < max.size(); i++) {
                for (int j = 0; j < globalMAX.size(); j++) {
                    if (max.get(i) == globalMAX.get(j)) {
                        globMax.add(max.get(i));
                        intersect.add(max.get(i));
                        intersectGlobalMax.add(max.get(i));
                    }
                }
            }
            for (int i = 0; i < intersect.size(); i++) {
                Integer index_to_remove = intersect.get(i);
                max.remove(index_to_remove);
            }

            String s = "In the whole West ";
            if (globMax.size() > 0) {
                s += "with the highest concentration (global Maximum) ";
                for (int z = 0; z < globMax.size(); z++) {
                    if (z < globMax.size() - 1) {
                        s += getRegion(globMax.get(z) + 1) + "(" + regions[globMax.get(z)].size() + ") and ";
                    } else {
                        s += getRegion(globMax.get(z) + 1) + "(" + regions[globMax.get(z)].size() + ")";
                    }
                }
                s += ".";
            }
            if (max.size() > 0) {
                if (s.charAt(s.length() - 1) == '.') {
                    s = s.substring(0, s.length() - 1);
                    s += " and a weaker concentration ";
                    for (int v = 0; v < max.size(); v++) {
                        if (!globMax.contains(max.get(v))) {
                            if (v < max.size() - 1) {
                                s += getRegion(max.get(v) + 1) + "(" + regions[max.get(v)].size() + ") and ";
                            } else {
                                s += getRegion(max.get(v) + 1) + "(" + regions[max.get(v)].size() + ")";
                            }
                        }
                    }
                    s += ".";
                } else {
                    s += " with the highest regional concentration (local Maximum) in ";
                    for (int k = 0; k < max.size(); k++) {
                        if (k < max.size() - 1) {
                            s += getRegion(max.get(k) + 1) + "(" + regions[max.get(k)].size() + ") and ";
                        } else {
                            s += getRegion(max.get(k) + 1) + "(" + regions[max.get(k)].size() + ")";
                        }
                    }
                    s += ".";
                }
            }
            Used[0] = true;
            Used[3] = true;
            Used[6] = true;
            temp.add(s);
        }
        if (regions[6].size() > 0 && regions[7].size() > 0 && regions[8].size() > 0) {
            Integer[] wantedRegions = { 6, 7, 8 };
            south = true;
            ArrayList max = this.getMaxRegionValue(regions, wantedRegions);
            ArrayList intersect = new ArrayList();
            ArrayList globMax = new ArrayList();
            for (int i = 0; i < max.size(); i++) {
                for (int j = 0; j < globalMAX.size(); j++) {
                    if ((Integer) max.get(i) == globalMAX.get(j)) {
                        globMax.add(max.get(i));
                        intersect.add(max.get(i));
                        intersectGlobalMax.add((Integer) max.get(i));
                    }
                }
            }
            for (int i = 0; i < intersect.size(); i++) {
                Integer index_to_remove = (Integer) intersect.get(i);
                max.remove(index_to_remove);
            }

            String s = "In the whole South ";
            if (globMax.size() > 0) {
                s += "with the highest concentration (global Maximum) ";
                for (int z = 0; z < globMax.size(); z++) {
                    if (z < globMax.size() - 1) {
                        s += getRegion((Integer) globMax.get(z) + 1) + "(" + regions[(Integer) globMax.get(z)].size()
                                + ") and ";
                    } else {
                        s += getRegion((Integer) globMax.get(z) + 1) + "(" + regions[(Integer) globMax.get(z)].size()
                                + ")";
                    }
                }
                s += ".";
            }
            if (max.size() > 0) {
                if (s.charAt(s.length() - 1) == '.') {
                    s = s.substring(0, s.length() - 1);
                    s += " and a weaker concentration ";
                    for (int v = 0; v < max.size(); v++) {
                        if (!globMax.contains(max.get(v))) {
                            if (v < max.size() - 1) {
                                s += getRegion((Integer) max.get(v) + 1) + "(" + regions[(Integer) max.get(v)].size()
                                        + ") and ";
                            } else {
                                s += getRegion((Integer) max.get(v) + 1) + "(" + regions[(Integer) max.get(v)].size()
                                        + ")";
                            }
                        }
                    }
                    s += ".";
                } else {
                    s += " with the highest regional concentration (local Maximum) in ";
                    for (int k = 0; k < max.size(); k++) {
                        if (k < max.size() - 1) {
                            s += getRegion((Integer) max.get(k) + 1) + "(" + regions[(Integer) max.get(k)].size()
                                    + ") and ";
                        } else {
                            s += getRegion((Integer) max.get(k) + 1) + "(" + regions[(Integer) max.get(k)].size() + ")";
                        }
                    }
                    s += ".";
                }
            }
            Used[6] = true;
            Used[7] = true;
            Used[8] = true;
            temp.add(s);
        }
        if (regions[2].size() > 0 && regions[5].size() > 0 && regions[8].size() > 0) {
            Integer[] wantedRegions = { 0, 5, 8 };
            east = true;
            ArrayList intersect = new ArrayList();
            ArrayList max = this.getMaxRegionValue(regions, wantedRegions);
            ArrayList globMax = new ArrayList();
            for (int i = 0; i < max.size(); i++) {
                for (int j = 0; j < globalMAX.size(); j++) {
                    if ((Integer) max.get(i) == globalMAX.get(j)) {
                        globMax.add(max.get(i));
                        intersect.add(max.get(i));
                        intersectGlobalMax.add((Integer) max.get(i));
                    }
                }
            }
            for (int i = 0; i < intersect.size(); i++) {
                Integer index_to_remove = (Integer) intersect.get(i);
                max.remove(index_to_remove);
            }

            String s = "In the whole East ";
            if (globMax.size() > 0) {
                s += "with the highest concentration (global Maximum) ";
                for (int z = 0; z < globMax.size(); z++) {
                    if (z < globMax.size() - 1) {
                        s += getRegion((Integer) globMax.get(z) + 1) + "(" + regions[(Integer) globMax.get(z)].size()
                                + ") and ";
                    } else {
                        s += getRegion((Integer) globMax.get(z) + 1) + "(" + regions[(Integer) globMax.get(z)].size()
                                + ")";
                    }
                }
                s += ".";
            }
            if (max.size() > 0) {
                if (s.charAt(s.length() - 1) == '.') {
                    s = s.substring(0, s.length() - 1);
                    s += " and a weaker concentration ";
                    for (int v = 0; v < max.size(); v++) {
                        if (!globMax.contains(max.get(v))) {
                            if (v < max.size() - 1) {
                                s += getRegion((Integer) max.get(v) + 1) + "(" + regions[(Integer) max.get(v)].size()
                                        + ") and ";
                            } else {
                                s += getRegion((Integer) max.get(v) + 1) + "(" + regions[(Integer) max.get(v)].size()
                                        + ")";
                            }
                        }
                    }
                    s += ".";
                } else {
                    s += " with the highest regional concentration (local Maximum) in ";
                    for (int k = 0; k < max.size(); k++) {
                        if (k < max.size() - 1) {
                            s += getRegion((Integer) max.get(k) + 1) + "(" + regions[(Integer) max.get(k)].size()
                                    + ") and ";
                        } else {
                            s += getRegion((Integer) max.get(k) + 1) + "(" + regions[(Integer) max.get(k)].size() + ")";
                        }
                    }
                    s += ".";
                }
            }
            Used[2] = true;
            Used[5] = true;
            Used[8] = true;
            temp.add(s);
        }
        /* Everywhere on th map */
        if (west == true && south == true && north == true && east == true) {
            temp = new ArrayList();
            String s = "";
            if (regions[4].size() > 0) {
                s = "On the whole Map, with a global maximum ";
                Used[4] = true;
            } else {
                s = "on the whole Map, except the Middle of the Map, with a global maximum ";
                Used[4] = true;
            }
            for (int i = 0; i < globalMAX.size(); i++) {
                if (i < globalMAX.size() - 1) {
                    s += getRegion(globalMAX.get(i) + 1) + "(" + regions[i].size() + ") and ";
                } else {
                    s += getRegion(globalMAX.get(i) + 1) + "(" + regions[i].size() + ")";
                }
            }
            s += ".";
            temp.add(s);
        } else { // just (maybe) N / W / S / E + single regions
            boolean noDescriptionYet = true;
            for (int i = 0; i < Used.length; i++) {
                if (Used[i] == true && regions[i].size() > 0) {
                    noDescriptionYet = false; // we already have a description
                }
            }
            // delte all Items that were already taken into report als being global max
            for (int i = 0; i < intersectGlobalMax.size(); i++) {
                Integer index_to_remove = intersectGlobalMax.get(i);
                globalMAX.remove(index_to_remove);
            }

            String s = "";
            if (noDescriptionYet == false && globalMAX.size() > 0) {// we already have a description OF S/N/E/W
                s += " Additionally theres a remarkable occurance in ";
            }
            if (noDescriptionYet == true && globalMAX.size() > 0) {
                s += " in ";
            }

            for (int i = 0; i < Used.length; i++) {
                if (Used[i] == false && regions[i].size() > 0) {
                    if (globalMAX.contains(i)) {
                        s += getRegion(i + 1) + " (" + regions[i].size() + ",Global Maximum),";
                    } else {
                        s += getRegion(i + 1) + "(" + regions[i].size() + "),";
                    }
                }
            }
            if (s.length() > 0) {
                s = s.substring(0, s.length() - 1);
                temp.add(s);
            }
        }

        if (temp.size() >= 1) {
            for (int i = 0; i < temp.size(); i++) {
                out += temp.get(i);
            }
            if (out.charAt(out.length() - 1) == '.') {
                out = out.substring(0, out.length() - 1);
            }
            return out + " of the Map.";
        } else {
            return " Nowhere on the Map."; // No Units with this Class found on the Map;
        }
    }

    /**
     * Returns the MAX Value of the Region (Region values need to be summed and cast into the array at the specific
     * Region index)
     * 
     * @param regions [9] double/int
     * @param wantedRegions specifies a selectrion of Region, NULL if whole map regions wanted
     */
    public ArrayList<Integer> getMaxRegionValue(ArrayList[] regions, Integer[] wantedRegions) {
        double max = Double.MIN_VALUE;
        ArrayList<Integer> out = new ArrayList<Integer>();
        ArrayList<Integer> out1 = new ArrayList<Integer>();

        if (wantedRegions == null) { // All regions
            for (int i = 0; i < regions.length; i++) {
                if (regions[i].size() >= max) {
                    max = regions[i].size();
                    out.add(i);
                }
            }
        } else { // Region selection
            for (int i = 0; i < regions.length; i++) {
                for (Integer wantedRegion : wantedRegions) {
                    if (i == wantedRegion.intValue() && regions[i].size() >= max) {
                        max = regions[i].size();
                        out.add(i);
                    }
                }
            }
        }

        for (int i = 0; i < out.size(); i++) {
            int index = out.get(i);
            if (max == regions[index].size()) {
                out1.add(out.get(i));
            }
        }
        return out1;
    }

    /**
     * Creates a Comparison report on all Quality Measures(qualifier=null), or on a specific Quality Measure(=Name), on
     * the given Region, or on the whole grid if the Region is 0
     */
    public String createQMComparisonReportonGrid(String qualifier, int Region) {
        String out = "";
        Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").info(
                "Making Semantic Quality Measure Interpretation for " + qualifier + " in region " + Region);
        Unit[] units = null;

        if (Region != 0) {
            ArrayList temp = new ArrayList();
            for (SemanticNode[] label : this.labels) {
                for (SemanticNode element : label) {
                    if (element.Region == Region) {
                        temp.add(element);
                    }
                }
            }
            units = new Unit[temp.size()];
            for (int i = 0; i < temp.size(); i++) {
                SemanticNode s = (SemanticNode) temp.get(i);
                int coords[] = s.realCoordinates;
                try {
                    units[i] = this.TestRun.getGrowingSOM().getLayer().getUnit(coords[0], coords[1]);
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                            "Cannot acces Units for Quality Measure Report for run " + this.TestRun.getRunId()
                                    + ". Reason: " + e);
                }
            }
        } else {
            // units = this.units;
            units = this.getNonEmptyUnits();
        }

        /* Find Maximum and all QM Having their maximum,minimum, falling together, or showing reverse proportional behavior */
        ArrayList<ArrayList<String>> maxmaxValues = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> minminValues = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> maxminValues = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> minmaxValues = new ArrayList<ArrayList<String>>();

        if (qualifier == null) { // Reporton all Quality Measures
            for (int i = 0; i < this.qm.UnitQualityMeasureNames.size(); i++) {
                String qualifier1 = this.qm.UnitQualityMeasureNames.get(i); // 1st Quality Measure(0..n)
                ArrayList<String> qualifiermaxmaxValues = new ArrayList<String>();
                qualifiermaxmaxValues.add(qualifier1);
                maxmaxValues.add(qualifiermaxmaxValues);
                ArrayList<String> qualifierminminValues = new ArrayList<String>();
                qualifierminminValues.add(qualifier1);
                minminValues.add(qualifierminminValues);
                ArrayList<String> qualifiermaxminValues = new ArrayList<String>();
                qualifiermaxminValues.add(qualifier1);
                maxminValues.add(qualifiermaxminValues);
                ArrayList<String> qualifierminmaxValues = new ArrayList<String>();
                qualifierminmaxValues.add(qualifier1);
                minmaxValues.add(qualifierminmaxValues);

                ArrayList tempStorageMAXMAX = new ArrayList();
                ArrayList tempStorageMINMIN = new ArrayList();
                ArrayList tempStorageMAXMIN = new ArrayList();
                ArrayList tempStorageMINMAX = new ArrayList();
                double[] tempComparisonMAXMAX = new double[2];
                double[] tempComparisonMINMIN = new double[2];
                double[] tempComparisonMAXMIN = new double[2];
                double[] tempComparisonMINMAX = new double[2];
                double maxmax = Double.MIN_VALUE;
                double minmin = Double.MIN_VALUE;
                double maxmin = Double.MIN_VALUE;
                double minmax = Double.MIN_VALUE;
                for (int j = 0; j < this.qm.UnitQualityMeasureNames.size(); j++) { // 2nd Quality Measure (i+1...n)
                    String qualifier2 = this.qm.UnitQualityMeasureNames.get(j);
                    if (!qualifier1.equals(qualifier2)) {
                        tempComparisonMAXMAX = qm.compareQualities(qualifier1, qualifier2, units, 1);
                        tempComparisonMINMIN = qm.compareQualities(qualifier1, qualifier2, units, 2);
                        tempComparisonMAXMIN = qm.compareQualities(qualifier1, qualifier2, units, 3);
                        tempComparisonMINMAX = qm.compareQualities(qualifier1, qualifier2, units, 4);
                        // MAX - MAX
                        if (tempComparisonMAXMAX[0] >= maxmax) {
                            maxmax = tempComparisonMAXMAX[0];
                            tempStorageMAXMAX.add(tempComparisonMAXMAX);
                            tempStorageMAXMAX.add(j);
                        }
                        // MIN- MIN
                        if (tempComparisonMINMIN[0] >= minmin) {
                            minmin = tempComparisonMINMIN[0];
                            tempStorageMINMIN.add(tempComparisonMINMIN);
                            tempStorageMINMIN.add(j);
                        }
                        // MAX- MIN
                        if (tempComparisonMAXMIN[0] >= maxmin) {
                            maxmin = tempComparisonMAXMIN[0];
                            tempStorageMAXMIN.add(tempComparisonMAXMIN);
                            tempStorageMAXMIN.add(j);
                        }
                        // MIN- MAX
                        if (tempComparisonMINMAX[0] >= minmax) {
                            minmax = tempComparisonMINMAX[0];
                            tempStorageMINMAX.add(tempComparisonMINMAX);
                            tempStorageMINMAX.add(j);
                        }
                    }

                }

                // get Quality Measures having common maximum Values
                ArrayList q = new ArrayList();
                double[] a = new double[2];
                if (tempStorageMAXMAX.size() > 0) {
                    for (int v = 0; v < tempStorageMAXMAX.size() - 1; v = v + 2) {
                        a = (double[]) tempStorageMAXMAX.get(v);
                        q = maxmaxValues.get(i);
                        if (a[0] == maxmax) {
                            int index = (Integer) tempStorageMAXMAX.get(v + 1);
                            q.add(this.qm.UnitQualityMeasureNames.get(index));
                        }
                    }
                    q.add(a[0]);
                }

                // get Quality Measures having common minimum Values
                if (tempStorageMINMIN.size() > 0) {
                    for (int v = 0; v < tempStorageMINMIN.size() - 1; v = v + 2) {
                        a = (double[]) tempStorageMINMIN.get(v);
                        q = minminValues.get(i);
                        if (a[0] == minmin) {
                            int index = (Integer) tempStorageMINMIN.get(v + 1);
                            q.add(this.qm.UnitQualityMeasureNames.get(index));
                        }
                    }
                    q.add(a[0]);
                }

                // get Quality Measures having max - min reverse proportional behavior
                if (tempStorageMAXMIN.size() > 0) {
                    for (int v = 0; v < tempStorageMAXMIN.size() - 1; v = v + 2) {
                        a = (double[]) tempStorageMAXMIN.get(v);
                        q = maxminValues.get(i);
                        if (a[0] == maxmin) {
                            int index = (Integer) tempStorageMAXMIN.get(v + 1);
                            q.add(this.qm.UnitQualityMeasureNames.get(index));
                        }
                    }
                    q.add(a[0]);
                }

                // get Quality Measures having min - max reverse proportional behavior
                if (tempStorageMINMAX.size() > 0) {
                    for (int v = 0; v < tempStorageMINMAX.size() - 1; v = v + 2) {
                        a = (double[]) tempStorageMINMAX.get(v);
                        q = minmaxValues.get(i);
                        if (a[0] == minmax) {
                            int index = (Integer) tempStorageMINMAX.get(v + 1);
                            q.add(this.qm.UnitQualityMeasureNames.get(index));
                        }
                    }
                    q.add(a[0]);
                }
            }
        } else { // Report on qualifier QM
            String qualifier1 = "";
            for (int i = 0; i < this.qm.UnitQualityMeasureNames.size(); i++) {
                if (this.qm.UnitQualityMeasureNames.get(i).equals(qualifier)) {// found wanted QM
                    qualifier1 = this.qm.UnitQualityMeasureNames.get(i);
                }
            }
            ArrayList qualifiermaxmaxValues = new ArrayList();
            qualifiermaxmaxValues.add(qualifier1);
            maxmaxValues.add(qualifiermaxmaxValues);
            ArrayList qualifierminminValues = new ArrayList();
            qualifierminminValues.add(qualifier1);
            minminValues.add(qualifierminminValues);
            ArrayList qualifiermaxminValues = new ArrayList();
            qualifiermaxminValues.add(qualifier1);
            maxminValues.add(qualifiermaxminValues);
            ArrayList qualifierminmaxValues = new ArrayList();
            qualifierminmaxValues.add(qualifier1);
            minmaxValues.add(qualifierminmaxValues);

            ArrayList tempStorageMAXMAX = new ArrayList();
            ArrayList tempStorageMINMIN = new ArrayList();
            ArrayList tempStorageMAXMIN = new ArrayList();
            ArrayList tempStorageMINMAX = new ArrayList();
            double[] tempComparisonMAXMAX = new double[2];
            double[] tempComparisonMINMIN = new double[2];
            double[] tempComparisonMAXMIN = new double[2];
            double[] tempComparisonMINMAX = new double[2];
            double maxmax = Double.MIN_VALUE;
            double minmin = Double.MIN_VALUE;
            double maxmin = Double.MIN_VALUE;
            double minmax = Double.MIN_VALUE;
            for (int j = 0; j < this.qm.UnitQualityMeasureNames.size(); j++) { // loop all other QM
                String qualifier2 = this.qm.UnitQualityMeasureNames.get(j);
                if (!qualifier1.equals(qualifier2)) {
                    tempComparisonMAXMAX = qm.compareQualities(qualifier1, qualifier2, units, 1);
                    tempComparisonMINMIN = qm.compareQualities(qualifier1, qualifier2, units, 2);
                    tempComparisonMAXMIN = qm.compareQualities(qualifier1, qualifier2, units, 3);
                    tempComparisonMINMAX = qm.compareQualities(qualifier1, qualifier2, units, 4);
                    // MAX - MAX
                    if (tempComparisonMAXMAX[0] >= maxmax) {
                        maxmax = tempComparisonMAXMAX[0];
                        tempStorageMAXMAX.add(tempComparisonMAXMAX);
                        tempStorageMAXMAX.add(j);
                    }
                    // MIN- MIN
                    if (tempComparisonMINMIN[0] >= minmin) {
                        minmin = tempComparisonMINMIN[0];
                        tempStorageMINMIN.add(tempComparisonMINMIN);
                        tempStorageMINMIN.add(j);
                    }
                    // MAX- MIN
                    if (tempComparisonMAXMIN[0] >= maxmin) {
                        maxmin = tempComparisonMAXMIN[0];
                        tempStorageMAXMIN.add(tempComparisonMAXMIN);
                        tempStorageMAXMIN.add(j);
                    }
                    // MIN- MAX
                    if (tempComparisonMINMAX[0] >= minmax) {
                        minmax = tempComparisonMINMAX[0];
                        tempStorageMINMAX.add(tempComparisonMINMAX);
                        tempStorageMINMAX.add(j);
                    }
                }

            }

            // get Quality Measures having common maximum Values
            ArrayList q = new ArrayList();
            double[] a = new double[2];
            if (tempStorageMAXMAX.size() > 0) {
                for (int v = 0; v < tempStorageMAXMAX.size() - 1; v = v + 2) {
                    a = (double[]) tempStorageMAXMAX.get(v);
                    q = maxmaxValues.get(0);
                    if (a[0] == maxmax) {
                        int index = (Integer) tempStorageMAXMAX.get(v + 1);
                        q.add(this.qm.UnitQualityMeasureNames.get(index));
                    }
                }
                q.add(a[0]);
            }

            // get Quality Measures having common minimum Values
            if (tempStorageMINMIN.size() > 0) {
                for (int v = 0; v < tempStorageMINMIN.size() - 1; v = v + 2) {
                    a = (double[]) tempStorageMINMIN.get(v);
                    q = minminValues.get(0);
                    if (a[0] == minmin) {
                        int index = (Integer) tempStorageMINMIN.get(v + 1);
                        q.add(this.qm.UnitQualityMeasureNames.get(index));
                    }
                }
                q.add(a[0]);
            }

            // get Quality Measures having max - min reverse proportional behavior
            if (tempStorageMAXMIN.size() > 0) {
                for (int v = 0; v < tempStorageMAXMIN.size() - 1; v = v + 2) {
                    a = (double[]) tempStorageMAXMIN.get(v);
                    q = maxminValues.get(0);
                    if (a[0] == maxmin) {
                        int index = (Integer) tempStorageMAXMIN.get(v + 1);
                        q.add(this.qm.UnitQualityMeasureNames.get(index));
                    }
                }
                q.add(a[0]);
            }

            // get Quality Measures having min - max reverse proportional behavior
            if (tempStorageMINMAX.size() > 0) {
                for (int v = 0; v < tempStorageMINMAX.size() - 1; v = v + 2) {
                    a = (double[]) tempStorageMINMAX.get(v);
                    q = minmaxValues.get(0);
                    if (a[0] == minmax) {
                        int index = (Integer) tempStorageMINMAX.get(v + 1);
                        q.add(this.qm.UnitQualityMeasureNames.get(index));
                    }
                }
                q.add(a[0]);
            }
        }

        // ReadOutthe Values
        if (this.type == 1) {// html Report
            out += "<br>";
            out += "<p class=\"header\">Semantic Quality Measure Interpretation</p><br>";
            out += "<span class=\"header2\"><u>Maximum / Maximum Relationship:</u></span><br><br>";
            out += "<ul>";
            for (int i = 0; i < maxmaxValues.size(); i++) {
                ArrayList list = maxmaxValues.get(i);
                if (list.size() > 1) {
                    out += (String) list.get(0) + " shares its maximum Values with " + (list.size() - 2)
                            + " other Quality Measures to a percentage of "
                            + Math.round((Double) list.get(list.size() - 1) * 100);
                    out += "<br>";
                    ArrayList semnodearray = new ArrayList();
                    semnodearray.add(list.get(0));
                    semnodearray.add(1);
                    for (int j = 1; j < list.size() - 1; j++) {
                        out += "<li>";
                        out += "<b>" + (String) list.get(j) + ":</b><br>";
                        // Say where we have the intersections
                        ArrayList[] intersectionList = this.getQMIntersectionList((String) list.get(0),
                                (String) list.get(j), units, 1);
                        out += "Intersecting Areas can be found " + this.simplifyRegionDescription(intersectionList);
                        out += "<br>";
                        out += testQM(qualifier, (String) list.get(j), intersectionList, 1);
                        out += "<br>";
                        for (ArrayList element : intersectionList) {
                            for (int b = 0; b < element.size(); b++) {
                                semnodearray.add(element.get(b));
                            }
                        }
                        out += "</li>";
                    }
                    out += this.createClassReportOnGrid(0, 0, semnodearray);
                    out += "<br>";
                }
            }
            out += "</ul>";
            out += "<br>";
            out += "<span class=\"header2\"><u>Minimum / Minimum Relationship:</u></span><br><br>";
            out += "<ul>";
            for (int i = 0; i < minminValues.size(); i++) {
                ArrayList list = minminValues.get(i);
                if (list.size() > 1) {
                    out += (String) list.get(0) + " shares its minimum Values with " + (list.size() - 2)
                            + " other Quality Measures to a percentage of "
                            + Math.round((Double) list.get(list.size() - 1) * 100);
                    out += "<br>";
                    ArrayList semnodearray = new ArrayList();
                    semnodearray.add(list.get(0));
                    semnodearray.add(2);
                    for (int j = 1; j < list.size() - 1; j++) {
                        out += "<li>";
                        out += "<b>" + (String) list.get(j) + ":</b><br>";
                        // Say where we have the intersections
                        ArrayList[] intersectionList = this.getQMIntersectionList((String) list.get(0),
                                (String) list.get(j), units, 2);
                        out += "Intersecting Areas can be found " + this.simplifyRegionDescription(intersectionList);
                        out += "<br>";
                        out += testQM(qualifier, (String) list.get(j), intersectionList, 2);
                        out += "<br>";
                        for (ArrayList element : intersectionList) {
                            for (int b = 0; b < element.size(); b++) {
                                semnodearray.add(element.get(b));
                            }
                        }
                        out += "</li>";
                    }
                    out += this.createClassReportOnGrid(0, 0, semnodearray);
                    out += "<br>";
                }
            }
            out += "</ul>";
            out += "<br>";
            out += "<span class=\"header2\"><u>Reverse Maximum / Minimum Relationship:</u></span><br><br>";
            out += "<ul>";
            for (int i = 0; i < maxminValues.size(); i++) {
                ArrayList list = maxminValues.get(i);
                if (list.size() > 1) {
                    out += (String) list.get(0) + " shows a reverse proportional behavior (MAX - MIN) with "
                            + (list.size() - 2) + " other Quality Measures to a percentage of "
                            + Math.round((Double) list.get(list.size() - 1) * 100);
                    out += "<br>";
                    ArrayList semnodearray = new ArrayList();
                    semnodearray.add(list.get(0));
                    semnodearray.add(3);
                    for (int j = 1; j < list.size() - 1; j++) {
                        out += "<li>";
                        out += "<b>" + (String) list.get(j) + ":</b><br>";
                        // Say where we have the intersections
                        ArrayList[] intersectionList = this.getQMIntersectionList((String) list.get(0),
                                (String) list.get(j), units, 3);
                        out += "Intersecting Areas can be found " + this.simplifyRegionDescription(intersectionList);
                        out += "<br>";
                        out += testQM(qualifier, (String) list.get(j), intersectionList, 3);
                        out += "<br>";
                        for (ArrayList element : intersectionList) {
                            for (int b = 0; b < element.size(); b++) {
                                semnodearray.add(element.get(b));
                            }
                        }
                        out += "</li>";
                    }
                    out += this.createClassReportOnGrid(0, 0, semnodearray);
                    out += "<br>";
                }
            }
            out += "</ul>";
            out += "<br>";
            out += "<span class=\"header2\"><u>Reveres Minimum / Maximum Relationship:</u></span><br><br>";
            out += "<ul>";
            for (int i = 0; i < minmaxValues.size(); i++) {
                ArrayList list = minmaxValues.get(i);
                if (list.size() > 1) {
                    out += (String) list.get(0) + " shows a reverse proportional behavior (MIN - MAX) with "
                            + (list.size() - 2) + " other Quality Measures to a percentage of "
                            + Math.round((Double) list.get(list.size() - 1) * 100);
                    out += "<br>";
                    ArrayList semnodearray = new ArrayList();
                    semnodearray.add(list.get(0));
                    semnodearray.add(4);
                    for (int j = 1; j < list.size() - 1; j++) {
                        out += "<li>";
                        out += "<b>" + (String) list.get(j) + ":</b><br>";
                        // Say where we have the intersections
                        ArrayList[] intersectionList = this.getQMIntersectionList((String) list.get(0),
                                (String) list.get(j), units, 4);
                        out += "Intersecting Areas can be found " + this.simplifyRegionDescription(intersectionList);
                        out += "<br>";
                        out += testQM(qualifier, (String) list.get(j), intersectionList, 4);
                        out += "<br>";
                        for (ArrayList element : intersectionList) {
                            for (int b = 0; b < element.size(); b++) {
                                semnodearray.add(element.get(b));
                            }
                        }
                        out += "</li>";
                    }
                    out += this.createClassReportOnGrid(0, 0, semnodearray);
                    out += "<br>";
                }
            }
            out += "</ul>";
            out += "<br>";
        } else { // LatexReport

        }

        return out;
    }

    /**
     * Creates a table that shows thew class infos if QMIntersectionClasshMap != Null, we also include a column showing
     * 
     * @param classes the class indices
     * @param type 1,2,3,4 reports
     * @return a html-formatted table that shows the class infos
     */
    public String showClassInfos(ArrayList<Integer> classes, Integer type) {
        String out = "";
        if (this.type == 1) {// HTML
            out += "<table border=\"1\"><tbody>" + "<tr>" + "<th>class name</th>" + "<th>mean vector</th>"
                    + "<th>mapped to unit</th>" + "<th>\"mean unit\"</th>" + "<th>radius</th>"
                    + "<th>Values by Coordinates</th>" + "<th><img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_"
                    + this.TestRun.getRunId() + "_classDistribution.jpg\" alt=\"class distribution of som "
                    + this.TestRun.getRunId() + "\"/></th>" + "<th>Class Entropy </th><th>Other classes in Area</th>"
                    + "<th><img src =\"" + SOMDescriptionHTML.imgSubdir + "/run_" + this.TestRun.getRunId()
                    + "_classEntropy.jpg\" alt=\"class entropy of som " + this.TestRun.getRunId() + "\"/></th>";
            if (type != null) {
                if (type == 1) { // MAX-MAX
                    out += "<th> On Maximum Units for Quality Measure:</th>";
                }
                if (type == 2) { // MIN MIN
                    out += "<th> On Minimum Units for Quality Measure:</th>";
                }
                if (type == 3) { // MAX MIN
                    out += "<th> On Maximum Units for Quality Measure:</th>";
                    out += "<th> On Minimum Units for Quality Measure:</th>";
                }
                if (type == 4) { // Min MAX
                    out += "<th> On Minimum Units for Quality Measure:</th>";
                    out += "<th> On Maximum Units for Quality Measure:</th>";
                }
            }

            out += "</tr>";
            for (int i = 0; i < classes.size(); i++) {
                int cIndex = classes.get(i);
                SemanticClass c = this.getClassfromGrid(cIndex);
                double[] mean = this.TestRun.datasetInfo.getClassMeanVector(c.index);
                String meanS = "";
                for (double element : mean) {
                    meanS += String.format("%.3f", element) + "<br/>";
                }
                int[] meanMapped = this.TestRun.getMappedUnit(mean);
                int[] meanUnit = this.TestRun.getClassMeanUnit(c.index);
                String coords = meanMapped[0] + "," + meanMapped[1];
                this.TestRun.visualizeClassLayout(meanUnit, meanMapped, c.index, this.imgDir);
                out += "<tr><td><u onclick=\"javascript:showSemanticDescription('" + c.Report + "')\">"
                        + this.TestRun.datasetInfo.getNameOfClass(c.index) + "</u></td>";
                out += "<td class=\"middleText\">" + meanS + "</td>";
                out += "<td class=\"middleText\">" + coords + "</td>";
                out += "<td class=\"middleText\">" + meanUnit[0] + "," + meanUnit[1] + "</td>";
                out += "<td class=\"middleText\">" + meanUnit[2] + "," + meanUnit[3] + "</td>";
                out += "<td class=\"middleText\"><img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_"
                        + this.TestRun.getRunId() + "_classCenter_" + c.index + ".jpg\" alt=\"class center of class "
                        + this.TestRun.datasetInfo.getNameOfClass(c.index) + "\"/></td>" + "<td><img src=\""
                        + SOMDescriptionHTML.imgSubdir + "/run_" + this.TestRun.getRunId() + "_classDistribution"
                        + c.index + ".jpg\" alt=\"class distribution of som " + this.TestRun.getRunId() + "\"/></td>"
                        + "<td>" + String.format("%2f", this.TestRun.getClassEntropy(c.index)) + "</td>"
                        + "<td><center>";
                // BREAKPOINT
                String[][] classmix = this.TestRun.getClassMix(c.index);
                for (String[] element : classmix) {
                    out += element[0];
                    out += "&nbsp;:&nbsp;";
                    out += element[1];
                    out += "<br>";
                }
                out += "</center></td>" + "<td><img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_"
                        + this.TestRun.getRunId() + "_classEntropy" + c.index + ".jpg\" alt=\"class " + c.index
                        + " entropy of som " + this.TestRun.getRunId() + "\"/></td>";
                if (type != null) {
                    if (type == 1) { // MAX-MAX
                        out += "<td><center>";
                        for (int h = 0; h < c.QMIntersectionClassArrayListMAX.size(); h += 2) {
                            out += c.QMIntersectionClassArrayListMAX.get(h);
                            out += "<br>";
                        }
                        out += "</center></td>";
                    }
                    if (type == 2) { // MIN MIN
                        out += "<td><center>";
                        for (int h = 0; h < c.QMIntersectionClassArrayListMIN.size(); h += 2) {
                            out += c.QMIntersectionClassArrayListMIN.get(h);
                            out += "<br>";
                        }
                        out += "</center></td>";
                    }
                    if (type == 3) { // MAX MIN
                        out += "<td><center>";
                        for (int h = 0; h < c.QMIntersectionClassArrayListMAX.size(); h += 2) {
                            out += c.QMIntersectionClassArrayListMAX.get(h);
                            out += "<br>";
                        }
                        out += "</td>";
                        out += "<td>";
                        for (int h = 0; h < c.QMIntersectionClassArrayListMIN.size(); h += 2) {
                            out += c.QMIntersectionClassArrayListMIN.get(h);
                            out += "<br>";
                        }
                        out += "</center></td>";
                    }
                    if (type == 4) { // Min MAX
                        out += "<td><center>";
                        for (int h = 0; h < c.QMIntersectionClassArrayListMIN.size(); h += 2) {
                            out += c.QMIntersectionClassArrayListMIN.get(h);
                            out += "<br>";
                        }
                        out += "</td>";
                        out += "<td>";
                        for (int h = 0; h < c.QMIntersectionClassArrayListMAX.size(); h += 2) {
                            out += c.QMIntersectionClassArrayListMAX.get(h);
                            out += "<br>";
                        }
                        out += "</center></td>";
                    }
                }
                out += "</tr>";
            }
            out += "</tbody></table>";
        } else { // LATEX

        }

        return out;
    }

    public ArrayList mergeArrayList(ArrayList l1, ArrayList l2) {
        ArrayList out = new ArrayList();
        for (int i = 0; i < l1.size(); i++) {
            Object o = l1.get(i);
            out.add(o);
        }
        for (int i = 0; i < l2.size(); i++) {
            Object o = l2.get(i);
            out.add(o);
        }
        return out;
    }

    /**
     * Returns a 9 field arrayList containing intersection units of QM Name1 & Name2, on units, that were chosen with
     * the comparing operation type
     * 
     * @param Name1 QualityMeasure1
     * @param Name2 QualityMeasure2
     * @param units Array of units.
     * @param type the type (see {@link QMContainer#getComparedQMRegionOccurances(String, String, Unit[], int)}
     * @return a 9 field arrayList containing intersection units of QM Name1 & Name2
     */
    public ArrayList[] getQMIntersectionList(String Name1, String Name2, Unit[] units, int type) {

        ArrayList unitArray = this.qm.getComparedQMRegionOccurances(Name1, Name2, units, type);
        ArrayList[] out = new ArrayList[NUMBER_OF_REGIONS];
        for (int i = 0; i < out.length; i++) {
            out[i] = new ArrayList();
        }
        for (int e = 0; e < unitArray.size(); e++) {
            Unit u = (Unit) unitArray.get(e);
            int[] coords = new int[2];
            coords[0] = u.getXPos();
            coords[1] = u.getYPos();
            SemanticNode s = this.getNode(coords);
            out[s.Region - 1].add(s);
        }
        return out;
    }

    /**
     * Puts this semanticGrid on the QuMContainer, uised or calculations ONLY the masterGrid should be set here, witch
     * is the one covering the whole Map
     */
    public void setSemanticGrid() {
        if (this.qm == null) {
            this.qm = this.TestRun.getQMContainer();
        }
        if (this.fatherGrid != null) {
            this.qm.setsGrid(this.fatherGrid);
        } else {
            this.qm.setsGrid(this);
        }

    }

    /**
     * Tests the SemanticNodes found in the ArrayList-array about QM specific properties
     * 
     * @param QM_compare the QM Identifier that is QM_to is compared to if QM_compare == null, we dont make a
     *            comparison, but just a test of the quality Measure
     * @param type 1 = MAX, 2 = MIN
     * @return The test results.
     */
    public String testQM(String QM_compare, String QM_to, ArrayList[] semanticNodes, int type) {
        String out = "";
        ArrayList temp = new ArrayList();
        // Transform Arraylist [] into readable Unit [] form//
        for (ArrayList list : semanticNodes) {
            for (int j = 0; j < list.size(); j++) {
                SemanticNode s = (SemanticNode) list.get(j);
                temp.add(s);
            }
        }
        Unit[] units = new Unit[temp.size()];
        for (int i = 0; i < units.length; i++) {
            SemanticNode s = (SemanticNode) temp.get(i);
            try {
                units[i] = this.TestRun.getGrowingSOM().getLayer().getUnit(s.realCoordinates[0], s.realCoordinates[1]);
            } catch (LayerAccessException e) {
                e.printStackTrace();
            }
        }
        /* Begin actual Calculation */

        int type1 = type;
        if (type == 3) {
            type1 = 2;
        }
        if (type == 4) {
            type1 = 1;
        }
        QMConfigurationProfile original_configuration = this.qm.getOriginalConfiguration(QM_to, type1);
        this.setSemanticGrid();
        if (original_configuration != null) {
            QMConfigurationProfile act_configuration = this.qm.getActualConfiguration(QM_to, units, type1);
            /* Compare the 2 ArrayLists */
            String MinMax = "";
            out += "According to results examined in Papers " + QM_to + " shows a ";
            if (type1 == 1) {
                MinMax = "Maximum";
            } else {
                MinMax = "Mininmum";
            }
            out += MinMax + " behavior on its Units under following circumstances:";
            out += "<ul>";

            for (int i = 0; i < original_configuration.SIZE; i++) {
                if (!original_configuration.isNullatPos(i)) {
                    out += "<li>" + this.qm.getQualityIdentifier(i) + "</li>";
                }
            }
            out += "</ul>";
            out += "<br>";
            String act_conf = "";
            /* Start comparing, only if the QM need special Calulation, tro veryfy its MAX / Minimum Values */
            if (act_configuration != null) {
                ArrayList unViolated = new ArrayList();
                for (int i = 0; i < act_configuration.SIZE; i++) {
                    if (!act_configuration.isNullatPos(i)) {
                        if (act_configuration.lengthOfElement(i) > 1) { // We have at least one Unit that fails the
                            // conditions on this position
                            ArrayList[] region_desc = new ArrayList[NUMBER_OF_REGIONS];
                            for (int k = 0; k < region_desc.length; k++) {
                                region_desc[k] = new ArrayList();
                            }
                            String perc = (String) act_configuration.getElement(i, 0);
                            if (act_conf.length() == 0) { // Build Table Body
                                act_conf = "<table border=\"1\">";
                                act_conf += "<tr>";
                                act_conf += "<th> Condition fullfilled </th>";
                                act_conf += "<th> on # of Units </th>";
                                act_conf += "<th> Violation in Region </th>";
                                act_conf += "</tr>";
                            }
                            if (this.qm.getClassFileDependantQualities().contains(this.qm.getQualityIdentifier(i))) {
                                act_conf += "<font color =\"red\">";
                            } else {
                                act_conf += "<font color =\"black\">";
                            }
                            act_conf += "<tr>";
                            act_conf += "<td>" + this.qm.getQualityIdentifier(i) + "</td>";
                            act_conf += "<td>" + perc + " </td>";
                            for (int j = 1; j < act_configuration.lengthOfElement(i); j++) {
                                Unit u = (Unit) act_configuration.getElement(i, j);
                                int[] coords = new int[2];
                                coords[0] = u.getXPos();
                                coords[1] = u.getYPos();
                                SemanticNode s = this.getNode(coords);
                                region_desc[s.Region - 1].add(s);
                            }
                            if (this.qm.getClassFileDependantQualities().contains(this.qm.getQualityIdentifier(i))) {
                                act_conf += "<td> Data wrong, due tu missing input! <td>";
                            } else {
                                act_conf += "<td>" + this.simplifyRegionDescription(region_desc) + "</td>";
                            }
                            act_conf += "</tr>";
                        } else {
                            unViolated.add(this.qm.getQualityIdentifier(i));
                        }
                    }
                }
                if (act_conf.length() > 0) { // we had Units that did not match all conditions
                    act_conf += "</table>";
                    out += "However, the Quality Measure "
                            + QM_to
                            + " did not match all of those conditions. The following table gives more detailed information:";
                    out += "<br>";
                    out += act_conf;
                    out += "<br>";
                    out += "So there is only ";
                    out += "<br>";

                    for (int h = 0; h < unViolated.size(); h++) {
                        if (!this.qm.getClassFileDependantQualities().contains(unViolated.get(h))) {
                            out += "<li>" + unViolated.get(h) + "</li>";
                        }
                    }
                    out += "<br>";
                    out += " not violated.";
                    out += "<br>";
                    if (this.qm.getClassFileDependantQualities().size() > 0) {
                        out += "For the correct Calculation of following Criteria a *.cls File would be needed, but none was give for the creation of this report. Therefore the information could not be interpreted.<br>";

                        for (int o = 0; o < this.qm.getClassFileDependantQualities().size(); o++) {
                            out += "<li>" + this.qm.getClassFileDependantQualities().get(o) + "</li>";
                        }
                    }

                } else {
                    out += "All of the Units were tested for these conditions to match, and no Violation could be found at all.";
                    if (this.qm.getClassFileDependantQualities().size() > 0) {
                        out += ". However for the correct Calculation of following Criteria a *.cls File would be needed, but none was give for the creation of this report <br>";

                        for (int o = 0; o < this.qm.getClassFileDependantQualities().size(); o++) {
                            out += "<li>" + this.qm.getClassFileDependantQualities().get(o) + "</li>";
                        }
                    }
                }
                this.qm.clearDamagedCriteriaList();
            }
            /* FURTHER Comparison between the possible MAX / MIN Values and intersections */
            if (QM_compare != null) {
                ArrayList possible_IntersectionCriteria = new ArrayList();
                QMConfigurationProfile orig_conf_compare = null;
                QMConfigurationProfile orig_conf_to = null;
                String clue_orig_ompare = "";
                String clue_orig_to = "";
                if (type == 1) {
                    orig_conf_compare = this.qm.getOriginalConfiguration(QM_compare, 1);
                    orig_conf_to = this.qm.getOriginalConfiguration(QM_to, 1);
                    clue_orig_ompare = "Maximum";
                    clue_orig_to = "Maximum";
                }
                if (type == 2) {
                    orig_conf_compare = this.qm.getOriginalConfiguration(QM_compare, 2);
                    orig_conf_to = this.qm.getOriginalConfiguration(QM_to, 2);
                    clue_orig_ompare = "Minimum";
                    clue_orig_to = "Minimum";
                }
                if (type == 3) {
                    orig_conf_compare = this.qm.getOriginalConfiguration(QM_compare, 1);
                    orig_conf_to = this.qm.getOriginalConfiguration(QM_to, 2);
                    clue_orig_ompare = "Maximum";
                    clue_orig_to = "Minimum";
                }
                if (type == 4) {
                    orig_conf_compare = this.qm.getOriginalConfiguration(QM_compare, 2);
                    orig_conf_to = this.qm.getOriginalConfiguration(QM_to, 1);
                    clue_orig_ompare = "Minimum";
                    clue_orig_to = "Maximum";
                }

                if (orig_conf_compare != null && orig_conf_to != null) {
                    for (int a = 0; a < orig_conf_compare.SIZE; a++) {
                        if (!orig_conf_compare.isNullatPos(a) && !orig_conf_to.isNullatPos(a)) {
                            possible_IntersectionCriteria.add(a);
                        }
                    }

                    if (possible_IntersectionCriteria.size() > 0) {
                        out += "There is a direct hint that both Quality Measures share a common Condition:";
                        out += "<ul>";
                        for (int b = 0; b < possible_IntersectionCriteria.size(); b++) {
                            out += this.qm.getQualityIdentifier((Integer) possible_IntersectionCriteria.get(b))
                                    + " leads for " + QM_compare + " to a " + clue_orig_ompare + " and for " + QM_to
                                    + " to a " + clue_orig_to + "<br>";
                        }
                        out += "</ul>";
                    }
                }
            }

        } else {
            out = "Unfortunatly there has not yet been added any Information, why this QM shows MAXIMUM / MINIMUM behavior.";
        }

        return out;
    }

    /** Puts the Fathers Grid Classes in this grid */
    public void getClassesfromFatherGrid() {
        if (this.fatherGrid != null) {
            for (int i = 0; i < this.labels.length; i++) {
                for (int j = 0; j < this.labels[i].length; j++) {
                    if (labels[i][j] != null) {
                        int[] coords = labels[i][j].realCoordinates;
                        SemanticNode fNode = this.fatherGrid.getNode(coords);
                        this.labels[i][j].Classes = fNode.Classes;
                    }
                }
            }
        }
    }

    /**
     * Returns an array containing nonempty units<br>
     * FIXME: refactor this, make a method in {@link GrowingLayer}, based on
     * {@link GrowingLayer#getNumberOfNotEmptyUnits()}
     */
    public Unit[] getNonEmptyUnits() {
        ArrayList<Unit> l = new ArrayList();
        for (Unit unit : this.units) {
            if (unit.getNumberOfMappedInputs() > 0) {
                l.add(unit);
            }
        }
        Unit[] u = new Unit[l.size()];
        for (int i = 0; i < u.length; i++) {
            u[i] = l.get(i);
        }
        return u;
    }
}
