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
package at.tuwien.ifs.somtoolbox.visualization.comparison;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.CollectionUtils;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterEquivalence;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusteringAbortedException;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusteringTree;
import at.tuwien.ifs.somtoolbox.visualization.clustering.LabelCoordinates;
import at.tuwien.ifs.somtoolbox.visualization.clustering.WardsLinkageTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.WardsLinkageTreeBuilderAll;

/**
 * @author Doris Baum
 * @version $Id: SOMComparison.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class SOMComparison {

    private GrowingSOM gsom1 = null;

    private GrowingSOM gsom2 = null;

    private LabelCoordinates[] coords1 = null;

    private LabelCoordinates[] coords2 = null;

    private String[] labelList1 = null;

    private String[] labelList2 = null;

    private double[][] dist1 = null;

    private double[][] dist2 = null;

    private double maxDistance = 0;

    private int maxCount = 0;

    private double sourceThreshold = 0;

    private double targetThreshold = 0;

    private int outlierCountThreshold = 0;

    private int stableCountThreshold = 0;

    private double outlierPercentThreshold = 75;

    private double stablePercentThreshold = 100;

    private int minAbsoluteCount = 1;

    private boolean absolute = true;

    private boolean multiMatch = false;

    private int clusterNo = 5;

    public final int MAXCLUSTERNO = 200;

    public static LabelCoordinates[] getLabelCoordinates(GrowingSOM gsom) {
        GrowingLayer layer = gsom.getLayer();
        String[] vectorLabels = layer.getAllMappedDataNames(true);
        int xSize = layer.getXSize();
        int ySize = layer.getYSize();

        LabelCoordinates[] coords = new LabelCoordinates[vectorLabels.length];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = new LabelCoordinates();
        }

        try {
            // go through all units in the layer...
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    // ... for each unit, get the labels of the
                    // vectors mapped to them
                    String[] unitnames = layer.getUnit(i, j).getMappedInputNames();
                    if (unitnames != null) {
                        for (String element : unitnames) {
                            // for each label, look up its position in the ArrayList...
                            int index = Arrays.binarySearch(vectorLabels, element);
                            // and save its units coordinates
                            coords[index].x = i;
                            coords[index].y = j;
                            coords[index].label = element;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(e.getMessage());
        }

        return coords;
    }

    // private LabelCoordinates[] alternateGetLabelCoordinates(GrowingSOM gsom) {
    //
    // GrowingLayer layer = gsom.getLayer();
    //
    // String[] vectorLabels = gsom.getLayer().getAllMappedDataNames(true);
    //
    // LabelCoordinates[] coords = new LabelCoordinates[vectorLabels.length];
    // for (int i = 0; i < coords.length; i++) {
    // coords[i] = new LabelCoordinates();
    // }
    //
    // for (int i = 0; i < vectorLabels.length; i++) {
    // String label = (String) vectorLabels[i];
    // Unit unit = layer.getUnitForDatum(label);
    // coords[i].x = unit.getXPos();
    // coords[i].y = unit.getYPos();
    // coords[i].label = label;
    // }
    //
    // return coords;
    // }

    public static GrowingSOM loadGSOM(String setname) {
        String unitDescriptionFileName = setname + SOMLibFormatInputReader.unitFileNameSuffix;
        String weightVectorFileName = setname + SOMLibFormatInputReader.weightFileNameSuffix;
        String mapDescriptionFileName = setname + SOMLibFormatInputReader.mapFileNameSuffix;

        try {
            return new GrowingSOM(new SOMLibFormatInputReader(weightVectorFileName, unitDescriptionFileName,
                    mapDescriptionFileName));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            return null;
        }
    }

    /** Calculate the distance matrix for all mapped vectors from the information where the inputs are mapped */
    public static double[][] calculcateIntraSOMDistanceMatrix(LabelCoordinates[] coords) throws MetricException {
        double[][] distanceMatrix = new double[coords.length][coords.length];
        for (int j = 0; j < coords.length; j++) { // label 1
            for (int i = j; i < coords.length; i++) { // label 2
                double distance = coords[j].distance(coords[i]);
                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance;
            }
        }
        return distanceMatrix;
    }

    public static double[][] calculateClusterDistances(int[][] clusterAssig, int clusterNo) {
        int xSize = clusterAssig.length;
        int ySize = clusterAssig[0].length;

        double maxdist = Math.ceil(Math.sqrt(xSize * xSize + ySize * ySize));

        double[][] mindist = new double[clusterNo][clusterNo];
        for (double[] element : mindist) {
            Arrays.fill(element, maxdist);
        }

        double distance = -1;
        int cluster1 = -1;
        int cluster2 = -1;

        for (int x1 = 0; x1 < xSize; x1++) {
            for (int y1 = 0; y1 < ySize; y1++) {
                for (int x2 = x1; x2 < xSize; x2++) {
                    for (int y2 = y1; y2 < ySize; y2++) {
                        distance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
                        cluster1 = clusterAssig[x1][y1];
                        cluster2 = clusterAssig[x2][y2];
                        if (distance < mindist[cluster1][cluster2]) {
                            mindist[cluster1][cluster2] = distance;
                            mindist[cluster2][cluster1] = distance;
                        }
                    }
                }
            }
        }

        return mindist;
    }

    /** Assigns each unit in the grid a number for the cluster it belongs to */
    public static int[][] calculateClusterAssignment(GrowingSOM gsom, int level) throws LayerAccessException,
            ClusteringAbortedException {

        // generate GUPNodes for the SOM
        GeneralUnitPNode[][] units = new GeneralUnitPNode[gsom.getLayer().getXSize()][gsom.getLayer().getYSize()];
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                if (gsom.getLayer().getUnit(i, j) != null) { // check needed for mnemonic SOMs (might not have all units
                    // != null)
                    units[i][j] = new GeneralUnitPNode(gsom.getLayer().getUnit(i, j),
                            CommonSOMViewerStateData.getInstance(), null, null, 0, 0);
                }
            }
        }

        // TODO move these lines to Comparison? Even better would be to use tree attribute in MapPNode
        // create cluster tree builder.
        WardsLinkageTreeBuilder wards = new WardsLinkageTreeBuilder();
        ClusteringTree tree = wards.createTree(units);

        return tree.getClusterAssignment(level, gsom.getLayer().getXSize(), gsom.getLayer().getYSize());
    }

    public int[] clusterEquivalent(int[][] assignment1, int[][] assignment2, LabelCoordinates[] coords1,
            LabelCoordinates[] coords2, int numberOfClusters, double[] percentages) {

        final boolean debug = true;

        if (debug) {
            System.out.println("first");
            for (int y = 0; y < assignment1[0].length; y++) {
                for (int[] element : assignment1) {
                    System.out.print(element[y] + " ");
                }
                System.out.println();
            }

            System.out.println("second");
            for (int y = 0; y < assignment2[0].length; y++) {
                for (int[] element : assignment2) {
                    System.out.print(element[y] + " ");
                }
                System.out.println();
            }
        }

        // have counters for equivalence assignments
        int[][] equiv = new int[numberOfClusters][numberOfClusters];
        for (int a = 0; a < numberOfClusters; a++) {
            Arrays.fill(equiv[a], 0);
        }

        // have counter for number of data vectors in cluster
        int[] clusterSize = new int[numberOfClusters];
        Arrays.fill(clusterSize, 0);

        // count how often a data vector from cluster a in SOM 1 is
        // assigned to cluster b in SOM 2
        int cluster1 = -1;
        int cluster2 = -1;
        for (int i = 0; i < coords1.length; i++) {
            cluster1 = assignment1[coords1[i].x][coords1[i].y];
            cluster2 = assignment2[coords2[i].x][coords2[i].y];
            equiv[cluster1][cluster2]++;
            clusterSize[cluster1]++;
        }

        // make an equivalence table which holds in each element an assignment and the
        // percentage of vectors from cluster a in SOM 1 that moved to cluster b in SOM 2
        int rowcount = 0;
        ClusterEquivalence[] equivTable = new ClusterEquivalence[numberOfClusters * numberOfClusters];
        for (int i = 0; i < numberOfClusters; i++) {
            for (int j = 0; j < numberOfClusters; j++) {
                equivTable[rowcount] = new ClusterEquivalence();
                if (clusterSize[i] > 0) {
                    equivTable[rowcount].percentage = (double) equiv[i][j] / (double) clusterSize[i];
                } else {
                    equivTable[rowcount].percentage = 0.0;
                }
                equivTable[rowcount].cluster1 = i;
                equivTable[rowcount].cluster2 = j;
                rowcount++;
            }
        }

        // sort the table according to percentages
        Arrays.sort(equivTable);

        if (debug) {
            System.out.println();
            System.out.println("1st cluster; 2nd cluster; confidence;");
            NumberFormat nf = NumberFormat.getPercentInstance();
            for (int i = equivTable.length - 1; i >= 0; i--) {
                System.out.println(equivTable[i].cluster1 + "; " + equivTable[i].cluster2 + "; "
                        + nf.format(equivTable[i].percentage) + ";");
            }
        }

        // have markers whether clusters were already assigned
        // don't assign a cluster twice!
        boolean[] used1 = new boolean[numberOfClusters];
        Arrays.fill(used1, false);
        boolean[] used2 = new boolean[numberOfClusters];
        Arrays.fill(used2, false);
        // count how many assignments have been made so far
        int equivCount = 0;
        int[] equivAssignment = new int[numberOfClusters];
        Arrays.fill(equivAssignment, -1);

        // go through the table and find the best assignments
        // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Final cluster equivalence");
        breakpoint: for (int i = equivTable.length - 1; i >= 0; i--) {
            if (!used1[equivTable[i].cluster1] && !used2[equivTable[i].cluster2]) {
                equivAssignment[equivTable[i].cluster1] = equivTable[i].cluster2;
                percentages[equivTable[i].cluster1] = equivTable[i].percentage;
                used1[equivTable[i].cluster1] = true;
                if (!multiMatch) {
                    used2[equivTable[i].cluster2] = true;
                }
                equivCount++;
                // Logger.getLogger("at.tuwien.ifs.somtoolbox").info(equivTable[i].cluster1 + " -> " +
                // equivTable[i].cluster2 + " : " +
                // equivTable[i].percentage);
            }
            if (equivCount >= numberOfClusters) {
                break breakpoint;
            }
        }

        // for all unassigned clusters
        // may be unnecessary
        for (int i = 0; i < numberOfClusters - equivCount; i++) {
            int count1 = 0;
            int count2 = 0;
            while (used1[count1] && count1 < used1.length) {
                count1++;
            }
            while (used2[count2] && count2 < used2.length) {
                count2++;
            }
            equivAssignment[count1] = count2;
            used1[count1] = true;
            used2[count2] = true;
        }

        if (debug) {
            System.out.println();
            System.out.println("1st cluster; 2nd cluster; confidence;");
            NumberFormat nf = NumberFormat.getPercentInstance();
            for (int i = 0; i < equivAssignment.length; i++) {
                System.out.println(i + "; " + equivAssignment[i] + "; " + nf.format(percentages[i]) + ";");
            }
        }

        return equivAssignment;
    }

    /**
     * Calculate the cluster distance matrix for all mapped vectors from the information where the inputs are mapped
     */
    public static double[][] calculcateIntraSOMClusterDistanceMatrix(LabelCoordinates[] coords,
            int[][] secSOMClusterAssignment, int clusterNo, double[][] distances) {

        int size = coords.length;
        double[][] distanceMatrix = new double[size][size];

        for (int j = 0; j < size; j++) { // label 1
            for (int i = j; i < size; i++) { // label 2
                int cluster1 = secSOMClusterAssignment[coords[j].x][coords[j].y];
                int cluster2 = secSOMClusterAssignment[coords[i].x][coords[i].y];
                double distance = distances[cluster1][cluster2];
                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance;
            }
        }

        return distanceMatrix;
    }

    private int highestCount(int[] neighbourCount) {
        int highestCount = 0;
        for (int element : neighbourCount) {
            if (element > highestCount) {
                highestCount = element;
            }
        }
        return highestCount;
    }

    public ArrayList<Shift> calculateShifts(boolean cumulative) throws SOMToolboxException {
        // if one of the necessary arrays is not set, throw exception
        if (coords1 == null || coords2 == null || dist1 == null || dist2 == null) {
            throw new SOMToolboxException(
                    "You need to call CompareSOMs.calculateMatrices before you can calculate any shifts!");
        }

        int size = coords1.length;

        // have a counter how many neighbours a vector has in the first SOM
        int[] oldNeighbourCount = new int[size];
        Arrays.fill(oldNeighbourCount, 1);

        // have a counter how many old neighbours of a vector are in its new neighbourhood
        // init to 1 because a vector is neighbour to itself
        int[] neighbourCount = new int[size];
        Arrays.fill(neighbourCount, 1);

        // count how many neighbours stay the same
        for (int j = 0; j < size; j++) { // label 1
            for (int i = j + 1; i < size; i++) { // label 2
                // if the vectors are on the same unit in the source SOM or if we're counting cumulative
                // and the vectors are within a radius of source threshold of each other
                if (dist1[j][i] == 0 || cumulative && dist1[j][i] <= sourceThreshold) {
                    // count old neighbours
                    oldNeighbourCount[i]++;
                    oldNeighbourCount[j]++;

                    // if the vectors are on the same unit in the target SOM or if we're counting cumulative
                    // and the vectors are within a radius of target threshold of each other
                    if (dist2[j][i] == 0 || cumulative && dist2[j][i] <= targetThreshold) {
                        // count neighbours that stayed the same
                        neighbourCount[i]++;
                        neighbourCount[j]++;
                    }
                }
            }
        }

        // find overall highest count
        int highestCount = this.highestCount(neighbourCount);

        // have marker if the vector does a stable shift
        boolean[] stableMarker = new boolean[size];
        Arrays.fill(stableMarker, false);

        // list for all shifts
        ArrayList<Shift> allshifts = new ArrayList<Shift>();

        // generate new shift objects for stable shifts and fill them with the data
        for (int i = 0; i < size; i++) {
            if (absolute && neighbourCount[i] >= stableCountThreshold || !absolute
                    && (double) neighbourCount[i] / oldNeighbourCount[i] >= stablePercentThreshold / 100
                    && neighbourCount[i] >= minAbsoluteCount) {
                Shift shift = new Shift();
                shift.setCoords(coords1[i].x, coords1[i].y, coords2[i].x, coords2[i].y);
                shift.setLabel(labelList1[i]);
                shift.setCount(neighbourCount[i]);
                shift.setPercent((double) neighbourCount[i] / oldNeighbourCount[i]);
                shift.setProportion((double) neighbourCount[i] / highestCount);
                shift.setType(Shift.STABLE);
                allshifts.add(shift);
                stableMarker[i] = true;
            }
        }

        // have marker if the vector is "adjacent" (within target threshold) to stable shift
        boolean[] adjacentMarker = new boolean[size];
        Arrays.fill(adjacentMarker, false);

        // find vectors "adjacent" to stable vectors
        for (int j = 0; j < size; j++) { // label 1
            for (int i = j + 1; i < size; i++) { // label 2
                if (dist1[j][i] <= sourceThreshold) {
                    if (dist2[j][i] <= targetThreshold) {
                        int adjacentIndex = -1;
                        // pick them only if they are not stable themselves
                        if (stableMarker[j] && !stableMarker[i]) {
                            adjacentIndex = i;
                        }
                        if (stableMarker[i] && !stableMarker[j]) {
                            adjacentIndex = j;
                        }
                        if (adjacentIndex != -1) {
                            adjacentMarker[adjacentIndex] = true;
                        }
                    }
                }
            }
        }

        // generate new shift objects for adjacent shifts and fill them with the data
        for (int i = 0; i < size; i++) {
            if (adjacentMarker[i]) {
                if (absolute || !absolute && neighbourCount[i] >= minAbsoluteCount) {
                    Shift shift = new Shift();
                    shift.setCoords(coords1[i].x, coords1[i].y, coords2[i].x, coords2[i].y);
                    shift.setLabel(labelList1[i]);
                    shift.setCount(neighbourCount[i]);
                    shift.setPercent((double) neighbourCount[i] / oldNeighbourCount[i]);
                    shift.setProportion((double) neighbourCount[i] / highestCount);
                    shift.setType(Shift.ADJACENT);
                    allshifts.add(shift);
                }
            }
        }

        // generate new shift objects for outlier shifts and fill them with the data
        for (int i = 0; i < size; i++) {
            // must be higher than outlier threshold but not a stable or adjacent shift already
            if ((absolute && neighbourCount[i] >= outlierCountThreshold || !absolute
                    && (double) neighbourCount[i] / oldNeighbourCount[i] >= outlierPercentThreshold / 100
                    && neighbourCount[i] >= minAbsoluteCount)
                    && !stableMarker[i] && !adjacentMarker[i]) {
                Shift shift = new Shift();
                shift.setCoords(coords1[i].x, coords1[i].y, coords2[i].x, coords2[i].y);
                shift.setLabel(labelList1[i]);
                shift.setCount(neighbourCount[i]);
                shift.setPercent((double) neighbourCount[i] / oldNeighbourCount[i]);
                shift.setProportion((double) neighbourCount[i] / highestCount);
                shift.setType(Shift.OUTLIER);
                allshifts.add(shift);
            }
        }

        return purgeShifts(allshifts);
    }

    // make a new list and throw out those shifts pointing from and to the same units
    private ArrayList<Shift> purgeShifts(ArrayList<Shift> allshifts) {
        // sort the shifts according to unit position
        Collections.sort(allshifts);

        // make a new list and throw out those shifts pointing from and to the same units
        ArrayList<Shift> resultShifts = new ArrayList<Shift>();
        int oldX1 = -1, oldY1 = -1, oldX2 = -1, oldY2 = -1;
        int oldType = -1;
        Iterator<Shift> iter = allshifts.iterator();
        Shift curShift = null;
        Shift lastNewShift = null;
        while (iter.hasNext()) {
            curShift = iter.next();

            if (!(curShift.getX1() == oldX1 && curShift.getY1() == oldY1 && curShift.getX2() == oldX2 && curShift.getY2() == oldY2)) {
                // This is a new Shift
                resultShifts.add(curShift);
                lastNewShift = curShift;
            } else {
                // This Shift is already in the result list.
                if (curShift.getType() != oldType) {
                    System.out.println("Types don't match: !" + curShift.getType() + " <-> " + oldType);
                } else {
                    // Sum up the props...
                    if (lastNewShift != null) { // Should not be necessary, but who knows...
                        // lastNewShift.setCount(lastNewShift.getCount() + curShift.getCount());
                        // lastNewShift.setLabel(lastNewShift.getLabel() + ", " + curShift.getLabel());
                        // // Should be good...
                        // lastNewShift.setPercent(lastNewShift.getPercent() + curShift.getPercent());
                    }
                }
            }

            oldX1 = curShift.getX1();
            oldY1 = curShift.getY1();
            oldX2 = curShift.getX2();
            oldY2 = curShift.getY2();
            oldType = curShift.getType();
        }

        // TODO: Some stats, do more!
        long allS = 0, stableS = 0, outS = 0, adjS = 0;
        double stableP = 0, outP = 0, adjP = 0;
        for (Shift shift : resultShifts) {
            allS += shift.getCount();
            switch (shift.getType()) {
                case Shift.STABLE:
                    stableS += shift.getCount();
                    stableP += shift.getPercent() * shift.getCount();
                    break;
                case Shift.ADJACENT:
                    adjS += shift.getCount();
                    adjP += shift.getPercent() * shift.getCount();
                    break;
                case Shift.OUTLIER:
                    outS += shift.getCount();
                    outP += shift.getPercent() * shift.getCount();
                    break;
            }
        }
        // outP /=

        String stat = String.format("(%d); Stable:   %d (%5.2f); " + "Adjacent: %d (%5.2f); " + "Outlier:  %d (%5.2f)",
                allS, stableS, 100 * (double) stableS / allS, adjS, 100 * (double) adjS / allS, outS, 100
                        * (double) outS / allS);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Stats: " + stat);
        System.out.println("\n\nStats:\n" + stat.replaceAll("; ", "\n") + "\n\n");

        return resultShifts;
    }

    public ArrayList<Shift> calculateClusterShifts(MapPNode map1, MapPNode map2) throws ClusteringAbortedException {
        // build clustering trees for both maps
        // Angela: WardsLinkageTreeBuilderAll() ist jetzt das "richtige" wards (aber halt langsam)
        // 2x "if" dazugegeben, damit nicht jedesmal der baum neu berechnet wird, wenn ein anderes level von
        // clustern angezeigt wird...
        if (map1.getCurrentClusteringTree() == null) {
            map1.buildTree(new WardsLinkageTreeBuilderAll());
        }

        ClusteringTree tree1 = map1.getClusteringTree();
        int[][] assignment1 = tree1.getClusterAssignment(clusterNo, gsom1.getLayer().getXSize(),
                gsom1.getLayer().getYSize());

        if (map2.getCurrentClusteringTree() == null) {
            map2.buildTree(new WardsLinkageTreeBuilderAll());
        }

        ClusteringTree tree2 = map2.getClusteringTree();
        int[][] assignment2 = tree2.getClusterAssignment(clusterNo, gsom2.getLayer().getXSize(),
                gsom2.getLayer().getYSize());

        // find the leftmost highest units to represent each cluster for both SOMs
        LabelCoordinates[] topunits1 = new LabelCoordinates[clusterNo];
        LabelCoordinates[] topunits2 = new LabelCoordinates[clusterNo];
        boolean used[] = new boolean[clusterNo];
        Arrays.fill(used, false);

        for (int y = 0; y < gsom1.getLayer().getYSize(); y++) {
            for (int x = 0; x < gsom1.getLayer().getXSize(); x++) {
                if (!used[assignment1[x][y]]) {
                    topunits1[assignment1[x][y]] = getClusterMeanPoint(assignment1, y, x, gsom1);
                    used[assignment1[x][y]] = true;
                }
            }
        }

        Arrays.fill(used, false);

        for (int y = 0; y < gsom2.getLayer().getYSize(); y++) {
            for (int x = 0; x < gsom2.getLayer().getXSize(); x++) {
                if (!used[assignment2[x][y]]) {
                    topunits2[assignment2[x][y]] = getClusterMeanPoint(assignment2, y, x, gsom2);
                    used[assignment2[x][y]] = true;
                }
            }
        }

        // find equivalences between the clusters
        double[] percentages = new double[clusterNo];
        int[] equiv = clusterEquivalent(assignment1, assignment2, coords1, coords2, clusterNo, percentages);

        // list of shifts
        ArrayList<Shift> allShifts = new ArrayList<Shift>();

        int size = coords1.length;

        // count the vectors that move from the same unit to the same unit
        int[] neighbourCount = new int[size];
        Arrays.fill(neighbourCount, 1);
        for (int j = 0; j < size; j++) { // label 1
            for (int i = j + 1; i < size; i++) { // label 2
                if (dist1[j][i] == 0) {
                    if (dist2[j][i] == 0) {
                        neighbourCount[i]++;
                        neighbourCount[j]++;
                    }
                }
            }
        }

        // find the highes number of moved vectors
        int highestCount = this.highestCount(neighbourCount);

        // stable and outlier shifts (that stay in or move out of the cluster)
        for (int i = 0; i < size; i++) {
            if (equiv[assignment1[coords1[i].x][coords1[i].y]] == assignment2[coords2[i].x][coords2[i].y]) {

                Shift shift = new Shift();
                shift.setCoords(coords1[i].x, coords1[i].y, coords2[i].x, coords2[i].y);
                shift.setLabel(labelList1[i]);
                shift.setCount(neighbourCount[i]);
                shift.setProportion((double) neighbourCount[i] / highestCount);
                shift.setType(Shift.STABLE);
                allShifts.add(shift);
            } else {
                Shift shift = new Shift();
                shift.setCoords(coords1[i].x, coords1[i].y, coords2[i].x, coords2[i].y);
                shift.setLabel(labelList1[i]);
                shift.setCount(neighbourCount[i]);
                shift.setProportion((double) neighbourCount[i] / highestCount);
                shift.setType(Shift.OUTLIER);
                allShifts.add(shift);
            }
        }

        ArrayList<Shift> resultShifts = this.purgeShifts(allShifts);

        // cluster shifts
        for (int i = 0; i < equiv.length; i++) {
            Shift shift = new Shift();
            shift.setCoords(topunits1[i].x, topunits1[i].y, topunits2[equiv[i]].x, topunits2[equiv[i]].y);
            shift.setLabel(topunits1[i].label + " -> " + topunits2[equiv[i]].label);
            // shift.setCount(0);
            if (percentages[i] < 0.01) {
                percentages[i] = 0.01;
            }
            shift.setProportion(percentages[i]);
            // shift.setBreadth();
            shift.setType(Shift.CLUSTER);
            resultShifts.add(shift);
        }

        return resultShifts;
    }

    /** Try to find a mean-point for a cluster */
    private LabelCoordinates getClusterMeanPoint(int[][] assignment, int y, int x, GrowingSOM gsom) {
        // System.out.println(ArrayUtils.toString(assignment));
        int classNumber = assignment[x][y];
        double count = 0;
        int xPos = 0;
        int yPos = 0;
        for (int i = 0; i < assignment.length; i++) {
            for (int j = 0; j < assignment[i].length; j++) {
                if (classNumber == assignment[i][j]) {
                    count++;
                    xPos += i;
                    yPos += j;
                    // System.out.println("adding point to " + classNumber + ": " + i + ", " + j);
                }
            }
        }
        // System.out.println("found points: " + count + ", acc: " + xPos + ", " + yPos);

        xPos = (int) Math.floor(xPos / count + 0.49);
        yPos = (int) Math.floor(yPos / count + 0.49);
        try {
            // if the mean is outside the cluster, or on an empty unit in a small cluster (<3 units)
            if (assignment[xPos][yPos] != classNumber
                    || gsom.getLayer().getUnit(xPos, yPos).getNumberOfMappedInputs() == 0 && count < 3) {
                // find the unit with most data mapped on
                Unit max = null;
                for (int i = 0; i < assignment.length; i++) {
                    for (int j = 0; j < assignment[i].length; j++) {
                        if (classNumber == assignment[i][j]) {
                            final Unit unit = gsom.getLayer().getUnit(i, j);
                            if (max == null || unit.getNumberOfMappedInputs() > max.getNumberOfMappedInputs()) {
                                max = unit;
                            }
                        }
                    }
                }
                xPos = max.getXPos();
                yPos = max.getYPos();
            }
        } catch (LayerAccessException e) {
            // does not happen
            e.printStackTrace();
        }
        // System.out.println("finally: " + xPos + ", " + yPos);
        return new LabelCoordinates(xPos, yPos, "cluster " + assignment[x][y]);
    }

    public void loadGSOMsFromPrefix(String prefix1, String prefix2) throws SOMToolboxException {
        loadGSOMs(loadGSOM(prefix1), prefix2);
    }

    public void loadGSOMs(GrowingSOM gsom, String prefix) throws SOMToolboxException {
        gsom1 = gsom;
        gsom2 = loadGSOM(prefix);

        labelList1 = gsom1.getLayer().getAllMappedDataNames(true);
        labelList2 = gsom2.getLayer().getAllMappedDataNames(true);

        if (!Arrays.equals(labelList1, labelList2)) {
            printInputDifferenceErrorMesage(labelList1, labelList2);
            throw new SOMToolboxException(
                    "The input vector sets of the SOMs aren't equal - can't do comparison! See the logs for input vector differences.");
        }

        calculateMatrices();
    }

    public void calculateMatrices() {
        try {
            maxCount = gsom1.getLayer().getAllMappedDataNames().length;

            double maxDistance1 = Math.sqrt(gsom1.getLayer().getXSize() * gsom1.getLayer().getXSize()
                    + gsom1.getLayer().getYSize() * gsom1.getLayer().getYSize());
            double maxDistance2 = Math.sqrt(gsom2.getLayer().getXSize() * gsom2.getLayer().getXSize()
                    + gsom2.getLayer().getYSize() * gsom2.getLayer().getYSize());

            if (maxDistance2 > maxDistance1) {
                maxDistance = maxDistance2;
            } else {
                maxDistance = maxDistance1;
            }

            stableCountThreshold = 5;
            if (stableCountThreshold > maxCount) {
                stableCountThreshold = maxCount;
            }
            outlierCountThreshold = 1;

            maxDistance = Math.ceil(maxDistance);

            // get the coordinates of the unit each input vector (and its label have been mapped to
            coords1 = getLabelCoordinates(gsom1);
            coords2 = getLabelCoordinates(gsom2);

            // calculate the distance matrices for each SOM
            dist1 = calculcateIntraSOMDistanceMatrix(coords1);
            dist2 = calculcateIntraSOMDistanceMatrix(coords2);
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
        }
    }

    /**
     * Compare two SOMs according to where input vectors come to lie on the map
     */
    public int getMaxCount() {
        return maxCount;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public double gettargetThreshold() {
        return targetThreshold;
    }

    public void settargetThreshold(double targetThreshold) {
        this.targetThreshold = targetThreshold;
    }

    public int getOutlierCountThreshold() {
        return outlierCountThreshold;
    }

    public int getStableCountThreshold() {
        return stableCountThreshold;
    }

    public double getTargetThreshold() {
        return targetThreshold;
    }

    public void setOutlierCountThreshold(int outlierCountThreshold) {
        this.outlierCountThreshold = outlierCountThreshold;
    }

    public void setStableCountThreshold(int stableCountThreshold) {
        this.stableCountThreshold = stableCountThreshold;
    }

    public void setTargetThreshold(double targetThreshold) {
        this.targetThreshold = targetThreshold;
    }

    public int getClusterNo() {
        return clusterNo;
    }

    public void setClusterNo(int clusterNo) {
        this.clusterNo = clusterNo;
    }

    public double getSourceThreshold() {
        return sourceThreshold;
    }

    public void setSourceThreshold(double sourceThreshold) {
        this.sourceThreshold = sourceThreshold;
    }

    public double getOutlierPercentThreshold() {
        return outlierPercentThreshold;
    }

    public void setOutlierPercentThreshold(double outlierPercentThreshold) {
        this.outlierPercentThreshold = outlierPercentThreshold;
    }

    public double getStablePercentThreshold() {
        return stablePercentThreshold;
    }

    public void setStablePercentThreshold(double stablePercentThreshold) {
        this.stablePercentThreshold = stablePercentThreshold;
    }

    public boolean isAbsolute() {
        return absolute;
    }

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    public int getMinAbsoluteCount() {
        return minAbsoluteCount;
    }

    public void setMinAbsoluteCount(int minAbsoluteCount) {
        this.minAbsoluteCount = minAbsoluteCount;
    }

    public static void printInputDifferenceErrorMesage(String[] labelList, String[] labelList2) {
        ArrayList<String>[] uniqueElements = CollectionUtils.getUniqueElements(labelList, labelList2);
        System.out.println("\n==============================================================");
        System.out.println("Inputs only in first SOM (" + uniqueElements[0].size() + ")");
        for (String s2 : uniqueElements[0]) {
            System.out.println("\t" + s2);
        }
        System.out.println("\n==============================================================");
        System.out.println("Inputs only in second SOM (" + uniqueElements[1].size() + ")");
        for (String s2 : uniqueElements[1]) {
            System.out.println("\t" + s2);
        }
    }

    public void setMultiMatch(boolean multiMatch) {
        this.multiMatch = multiMatch;
    }

    public boolean isMultiMatch() {
        return multiMatch;
    }
}
