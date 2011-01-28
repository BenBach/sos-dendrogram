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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import at.tuwien.ifs.somtoolbox.structures.ComponentLine3D;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.Point3d;

/**
 * Load the input format produced by the Second Life Analytics Suite.
 * 
 * @author Robert Neumayer
 * @version $Id: SecondLifeInputFileReader.java 3888 2010-11-02 17:42:53Z frank $
 */
public class SecondLifeInputFileReader {

    private String uid, region, pos_x, pos_y, pos_z, avatar_name, total_number, time, avatar_key, avatar_info;

    private int numberOfLines;

    private long[] uids;

    private String[] regions;

    private Point3d[] positions;

    // TODO index this differently
    private String[] avatarNames;

    private long[] totalNumbers;

    private Date[] times;

    private String[] avatarKeys;

    private Hashtable<String, Vector<Point3d>> avatarPositions;

    private void init(String secondLifeCoordinateFile) throws IOException {
        BufferedReader br = FileUtils.openFile("SecondLife Coordinate File", secondLifeCoordinateFile);

        br.readLine(); // skip header
        this.numberOfLines = 0;
        String line = null;

        while ((line = br.readLine()) != null) {
            this.numberOfLines++;
        }

        br.close();

        this.uids = new long[this.numberOfLines];
        this.regions = new String[this.numberOfLines];
        this.positions = new Point3d[this.numberOfLines];
        this.avatarNames = new String[this.numberOfLines];
        this.totalNumbers = new long[this.numberOfLines];
        this.times = new Date[this.numberOfLines];
        this.avatarKeys = new String[this.numberOfLines];

        avatarPositions = new Hashtable<String, Vector<Point3d>>();
    }

    public SecondLifeInputFileReader(String secondLifeCoordinateFile) throws IOException {

        this.init(secondLifeCoordinateFile);
        BufferedReader br = FileUtils.openFile("SecondLife Coordinate File", secondLifeCoordinateFile);

        String line = null;

        String headerLine = br.readLine();
        String[] elements = headerLine.split(";");
        uid = elements[0];
        region = elements[1];
        pos_x = elements[2];
        pos_y = elements[3];
        pos_z = elements[4];
        avatar_name = elements[5];
        total_number = elements[6];
        time = elements[7];
        avatar_key = elements[8];
        avatar_info = elements[9];

        String previousName = "init";
        for (int index = 0; (line = br.readLine()) != null; index++) {
            if (index == 0) {
                previousName = elements[5];
            }
            elements = line.split(";");
            uids[index] = Long.parseLong(elements[0]);
            regions[index] = elements[1];
            // System.out.println(index + " | " + uids[index] + new Point3d(Double.parseDouble(elements[2]),
            // Double.parseDouble(elements[3]),
            // Double.parseDouble(elements[4])));
            positions[index] = new Point3d(Double.parseDouble(elements[2]), Double.parseDouble(elements[3]),
                    Double.parseDouble(elements[4]));
            // System.out.println("added: " + positions[index]);
            avatarNames[index] = elements[5];
            if (previousName.equals(elements[5])) {
                if (avatarPositions.get(elements[5]) != null) {
                    Vector<Point3d> pos = avatarPositions.get(elements[5]);
                    pos.add(positions[index]);
                    avatarPositions.put(elements[5], pos);
                }
            } else {
                Vector<Point3d> v = new Vector<Point3d>();
                v.add(positions[index]);
                avatarPositions.put(elements[5], v);
            }
            previousName = elements[5];
            totalNumbers[index] = Long.parseLong(elements[6]);
            // TODO figure out something for the timestamps
            // times = new Date(elements[7]);
            avatarKeys[index] = elements[8];
            // avatarnfo = elements[9];

        }
        initMinAndMaxValues();
    }

    public String[] getAvatarKeys() {
        return avatarKeys;
    }

    public void setAvatarKeys(String[] avatarKeys) {
        this.avatarKeys = avatarKeys;
    }

    public String[] getAvatarNames() {
        return avatarNames;
    }

    public void setAvatarNames(String[] avatarNames) {
        this.avatarNames = avatarNames;
    }

    public Point3d[] getPositions() {
        return positions;
    }

    public static double[][][] point2DoubleArray(Point3d[][] pointArray) {
        double[][][] data = new double[pointArray.length][pointArray[0].length][];
        for (int i = 0; i < pointArray.length; i++) {
            data[i] = point2DoubleArray(pointArray[i]).clone();
        }
        return data;
    }

    public static double[][] point2DoubleArray(Point3d[] pointArray) {
        double[][] data = new double[pointArray.length][3];
        for (int i = 0; i < pointArray.length; i++) {
            data[i] = new double[] { pointArray[i].x, pointArray[i].y, pointArray[i].z };
        }
        return data;
    }

    public static Point3d[][] double2PointArray(double[][][] doubleArray) {
        Point3d[][] pointArray = new Point3d[doubleArray.length][doubleArray[0].length];
        for (int i = 0; i < doubleArray.length; i++) {
            for (int j = 0; j < pointArray[0].length; j++) {
                pointArray[i][j] = new Point3d(doubleArray[i][j][0], doubleArray[i][j][1], doubleArray[i][j][2]);
            }
        }
        return pointArray;
    }

    public static ArrayList<ComponentLine3D> double2ComponentLineArray(double[][][] doubleArray) {
        ArrayList<ComponentLine3D> res = new ArrayList<ComponentLine3D>();
        for (int i = 0; i < doubleArray.length; i++) {
            Point3d[] pointArray = new Point3d[doubleArray[i].length];
            for (int j = 0; j < pointArray.length; j++) {
                pointArray[j] = new Point3d(doubleArray[i][j][0], doubleArray[i][j][1], doubleArray[i][j][2]);
            }
            res.add(new ComponentLine3D(pointArray, i));
        }
        return res;
    }

    double[] minValues, maxValues;

    private void initMinAndMaxValues() {
        double[][] data = SecondLifeInputFileReader.point2DoubleArray(positions);
        int numberOfAttributes = data[0].length;
        minValues = new double[numberOfAttributes];
        maxValues = new double[numberOfAttributes];
        // for each attribute
        for (int j = 0; j < numberOfAttributes; j++) {
            // in each instance (i.e. each single value now :-))
            minValues[j] = Double.MAX_VALUE;
            maxValues[j] = Double.MIN_VALUE;
            for (double[] element : data) {
                if (element[j] < minValues[j]) {
                    minValues[j] = element[j];
                }
                if (element[j] > maxValues[j]) {
                    maxValues[j] = element[j];
                }
            }
        }
    }

    public void setPositions(Point3d[] positions) {
        this.positions = positions;
    }

    public String[] getRegions() {
        return regions;
    }

    public void setRegions(String[] regions) {
        this.regions = regions;
    }

    public long[] getTotalNumbers() {
        return totalNumbers;
    }

    public void setTotalNumbers(long[] totalNumbers) {
        this.totalNumbers = totalNumbers;
    }

    public long[] getUids() {
        return uids;
    }

    public void setUids(long[] uids) {
        this.uids = uids;
    }

    public String[] getDistinctAvatarNames() {
        Object[] distinctAvatarNamesAsObjects = avatarPositions.keySet().toArray();
        String[] distinctAvatarNames = new String[distinctAvatarNamesAsObjects.length];
        for (int i = 0; i < distinctAvatarNames.length; i++) {
            distinctAvatarNames[i] = distinctAvatarNamesAsObjects[i].toString();
        }
        return distinctAvatarNames;
    }

    public double[][] getAvatarPositions(String avatarName) {
        Vector<Point3d> names = avatarPositions.get(avatarName);
        Point3d oldPoint = null;
        int numberOfEqualPoints = 0;
        for (int i = 0; i < names.size(); i++) {

            // FIXME think abou this
            // if(i != 0 && oldPoint.equals(names.elementAt(i))){
            // numberOfEqualPoints++;
            // if(numberOfEqualPoints > 10){
            // names.remove(i);
            // i--;
            // }

            // }
            // oldPoint = names.elementAt(i);
        }
        double[][] positions = new double[names.size()][3];
        for (int i = 0; i < names.size(); i++) {
            // System.out.println("vector: " + i + " / " + names.elementAt(i));
            positions[i][0] = names.elementAt(i).x;
            positions[i][1] = names.elementAt(i).y;
            positions[i][2] = names.elementAt(i).z;
        }
        return positions;
    }

    public double[] getMaxValues() {
        return maxValues;
    }

    public double[] getMinValues() {
        return minValues;
    }
}
