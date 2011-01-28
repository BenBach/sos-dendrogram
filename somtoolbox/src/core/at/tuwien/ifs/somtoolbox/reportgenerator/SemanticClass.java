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
import java.util.Collection;
import java.util.HashMap;

/**
 * @author Martin Waitzbauer (0226025)
 * @version $Id: SemanticClass.java 3888 2010-11-02 17:42:53Z frank $
 */
@SuppressWarnings("rawtypes")
public class SemanticClass {

    public int index;

    public String Report = null;

    public int[] CenterPoint;

    public int SharedClasses = 0;

    public ArrayList<SemanticNode> SemanticNodes = new ArrayList<SemanticNode>();

    private ArrayList<int[]> UnitswithClassList = new ArrayList<int[]>();

    public ArrayList<int[]> ClassPartitions = new ArrayList<int[]>();

    public ArrayList ConnectedClasses = new ArrayList();

    public int[] regionmix;

    public double[] concentrationmix;

    public int MemberCount = 0;

    private ArrayList<SemanticNode> EdgeNodes = null;

    // private double Spread =0.0;
    private double Compactness = 0.0;

    public SemanticClass(int index, ArrayList<int[]> UnitswithClassList) {
        this.index = index;
        this.UnitswithClassList = UnitswithClassList;
    }

    public ArrayList QMIntersectionClassArrayListMAX = new ArrayList();

    public ArrayList QMIntersectionClassArrayListMIN = new ArrayList();

    public void setMeanPoint(int[] mp) {
        this.CenterPoint = mp;
    }

    /*
     * public void setClassPartitions(ArrayList ClassPartitions){ this.ClassPartitions = ClassPartitions; this.Spread =
     * UnitswithClassList.size()/ClassPartitions.size(); }
     */

    /**
     * Lists all different partitions of the class (a Partition is consicered a connected Area of units with no more
     * than 1 as distance between each unit)
     */
    public void calculateClassPartitions() {
        HashMap<String, int[]> result = new HashMap<String, int[]>();
        for (int i = 0; i < UnitswithClassList.size(); i++) {
            ArrayList<Integer> trail = getTrail(i, new ArrayList<Integer>()); // Gets the trail, since there are
                                                                              // manyequal
            // trails, only keep unique trails
            int[] tempTrail = new int[trail.size()];
            for (int j = 0; j < trail.size(); j++) {
                tempTrail[j] = trail.get(j);
            }
            Arrays.sort(tempTrail);
            String key = "";
            for (int element : tempTrail) {
                key += element;
            }
            if (!result.containsKey(key)) {
                result.put(key, tempTrail);
            }
        }
        Collection<int[]> col = result.values();
        this.ClassPartitions = new ArrayList<int[]>(col);
        // this.Spread = (double)UnitswithClassList.size()/(double)ClassPartitions.size();
    }

    /** Finds a Trail for unit i through the class (all units with distance 1 will be added to one trail) */
    private ArrayList<Integer> getTrail(int i, ArrayList<Integer> trail) {
        int[] N1 = UnitswithClassList.get(i);
        if (!trail.contains(i)) {
            trail.add(i);
        }
        for (int h = 0; h < UnitswithClassList.size(); h++) {
            int[] N2 = UnitswithClassList.get(h);
            if (!N1.equals(N2)) {
                int[] temp = SemanticInterpreterGrid.getDistance(N1, N2);
                if (Math.abs(temp[0]) <= 1 && Math.abs(temp[1]) <= 1 && !trail.contains(h)) {
                    trail = getTrail(h, trail);
                }
            }
        }
        return trail;
    }

    /**
     * Calculates How much of the class units lie in a radius around the middle point ( this radius is the mean over all
     * distances from the middle point to other class points)
     */
    public void calculateCompactness() {
        double distance = 0.0;
        for (int i = 0; i < this.UnitswithClassList.size(); i++) {
            int[] temp = SemanticInterpreterGrid.getDistance(CenterPoint, UnitswithClassList.get(i));
            distance += Math.abs((double) temp[0]) + Math.abs((double) temp[1]);
        }
        distance = Math.round(distance / UnitswithClassList.size() / 2);
        double UnitsWithinRange = 0.0;
        for (int i = 0; i < this.UnitswithClassList.size(); i++) {
            int[] temp = SemanticInterpreterGrid.getDistance(CenterPoint, UnitswithClassList.get(i));
            if (Math.abs((double) temp[0]) <= distance && Math.abs((double) temp[1]) <= distance) {
                UnitsWithinRange++;
            }
        }
        this.Compactness = UnitsWithinRange / this.UnitswithClassList.size();
    }

    /** calculates the point with the most distance to the class centre */
    public int[] getFurthestMember() {
        int max = Integer.MIN_VALUE;
        int[] result = new int[2];
        for (int i = 0; i < this.UnitswithClassList.size(); i++) {
            int temp[] = UnitswithClassList.get(i);
            int point[] = SemanticInterpreterGrid.getDistance(this.CenterPoint, temp);
            int distance = Math.abs(point[0]) + Math.abs(point[1]);
            if (max < distance) {
                max = distance;
                result = temp;
            }
        }
        return result;
    }

    public void setReport(String rep) {
        this.Report = rep;
    }

    /**
     * Returns an ArrayList containing directions of the Units given in the ArrayList, measured to the Center of the
     * Class
     */
    public ArrayList<String> getIntersectionDirections(ArrayList l) {
        ArrayList<String> result = new ArrayList<String>();
        String w = "";
        for (int i = 0; i < l.size(); i++) {
            ArrayList m = (ArrayList) l.get(i);
            for (int r = 0; r < m.size(); r++) {
                int e = (Integer) m.get(r);
                int[] d = SemanticInterpreterGrid.getDistance(this.CenterPoint, this.UnitswithClassList.get(e));
                if (d[0] > 0) {
                    if (!result.contains("east")) {
                        w = "east of";
                    }
                }
                if (d[0] < 0) {
                    if (!result.contains("west")) {
                        w = "west of";
                    }
                }
                if (d[1] > 0) {
                    if (!result.contains("south")) {
                        w = "south of";
                    }
                }
                if (d[1] < 0) {
                    if (!result.contains("north")) {
                        w = "north of";
                    }
                }
                if (d[1] == 0) {
                    if (!result.contains("in")) {
                        w = "in";
                    }
                }
                if (d[0] == 0) {
                    if (!result.contains("in")) {
                        w = "in";
                    }
                }
            }
        }
        result.add(w);
        return result;
    }

    public void addRegionMix(int[] regions) {
        this.regionmix = regions;
    }

    public void addConcentrationMix(double[] conc) {
        this.concentrationmix = conc;
    }

    public void addNode(SemanticNode s) {
        this.SemanticNodes.add(s);
    }

    public void setSharedClasses(int classes) {
        this.SharedClasses = classes;
    }

    /*
     * public Collection getSharedClasses(){ return this.SharedClasses; }
     */
    public double getCompactness() {
        return this.Compactness;
    }

    /**
     * Return true, if the Class matches the wanted concentration in the region index
     * 
     * @param EP
     * @return
     */
    /*
     * public boolean matchesConcentrationRequirements(EditableReportProperties EP){ boolean out = false; if(EP != null){ int concMIN =
     * EP.getMINConcentration(); int concMAX = EP.getMAXConcentration(); if(concMIN!= -1 && concMAX!= -1){ if(this.concentrationmix[index] >= concMIN
     * && this.concentrationmix[index] <= concMAX) out = true; } else{ if(concMIN!= -1){ // User made MIN Requirements for this Region
     * if(this.concentrationmix[index] >= concMIN) // Matches? out=true; } if(concMAX!= -1){ // User made MAX Requirements for this Region
     * if(this.concentrationmix[index] <= concMAX) // Matches? out=true; } } } return out; }
     */
    /** Return true, if the Class matches the wanted compactness in the region index */
    public boolean matchesCompactnessRequirements(EditableReportProperties EP) {
        boolean out = false;
        int compMIN = EP.getMINCompactness();
        int compMAX = EP.getMAXCompactness();
        if (compMIN != -1 && compMAX != -1) {
            if (this.Compactness * 100 >= compMIN && this.Compactness * 100 <= compMAX) {
                out = true;
            }
        } else {
            if (compMIN != -1) { // User made MIN Requirements for this Region
                if (this.Compactness * 100 >= compMIN) {
                    out = true;
                }
            }
            if (compMAX != -1) { // User made MAX Requirements for this Region
                if (this.Compactness * 100 <= compMAX) {
                    out = true;
                }
            }
        }
        return out;
    }

    /** Sets the Semantic Nodes that were found to be on the edges of the class */
    public void setClassEdges(ArrayList<SemanticNode> edges) {
        if (this.EdgeNodes == null) {
            EdgeNodes = new ArrayList<SemanticNode>();
        }
        this.EdgeNodes = edges;
    }

    public ArrayList<SemanticNode> getEdgeNodes() {
        return this.EdgeNodes;
    }

    /**
     * Sets the setQMIntersectionClassArrayList, containing the Name of the QM on uneven places, and the
     * IntersectionNodes-ArrayList on even Places for Maximum Units of the QM
     */
    public void setQMIntersectionClassArrayListMAX(ArrayList QMIntersectionClassArrayList) {
        this.QMIntersectionClassArrayListMAX = QMIntersectionClassArrayList;
    }

    /**
     * Sets the setQMIntersectionClassArrayList, containing the Name of the QM on uneven places, and the
     * IntersectionNodes-ArrayList on even Places for Minimum Units of the QM
     */
    public void setQMIntersectionClassArrayListMIN(ArrayList QMIntersectionClassArrayList) {
        this.QMIntersectionClassArrayListMIN = QMIntersectionClassArrayList;
    }

}
