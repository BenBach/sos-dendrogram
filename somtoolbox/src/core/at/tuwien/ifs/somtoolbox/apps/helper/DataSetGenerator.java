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
package at.tuwien.ifs.somtoolbox.apps.helper;

import java.awt.geom.Point2D;
import java.util.Vector;

import org.apache.commons.math.random.RandomDataImpl;

/**
 * Utility class to generate a two-dimensional data-set distributed in various different classes.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: DataSetGenerator.java 3888 2010-11-02 17:42:53Z frank $
 */
public class DataSetGenerator {

    private static final String CLASS_1 = "Class 1";

    private static final String CLASS_2 = "Class 2";

    private static final String CLASS_3 = "Class 3";

    private static final String CLASS_4 = "Class 4";

    private static final String CLASS_5 = "Class 5";

    private static final String CLASS_6 = "Class 6";

    public static final String[] CLASS_NAMES = { CLASS_1, CLASS_2, CLASS_3, CLASS_4, CLASS_5, CLASS_6 };

    public class DataPoint {
        private String name = null;

        private Point2D point = null;

        public DataPoint(String c, double x, double y) {
            name = c;
            point = new Point2D.Double(x, y);
        }

        public double getX() {
            return point.getX();
        }

        public double getY() {
            return point.getY();
        }

        public void setLocation(double x, double y) {
            point.setLocation(x, y);
        }

        public String getName() {
            return name;
        }
    }

    static private RandomDataImpl rand = new RandomDataImpl();

    Vector<DataPoint> allPoints = new Vector<DataPoint>();

    Vector<DataPoint>[] classPoints;

    @SuppressWarnings("unchecked")
    public DataSetGenerator() {
        rand = new RandomDataImpl();
        classPoints = new Vector[CLASS_NAMES.length];
        for (int i = 0; i < classPoints.length; i++) {
            classPoints[i] = new Vector<DataPoint>();
        }

        int i = 0;

        classPoints[i].addAll(generatePoints(CLASS_1 + "_1", 50, 5, 3, 1, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_1 + "_2", 50, 11, 3, 1, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_1 + "_3", 50, 5, 6, 1, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_1 + "_4", 50, 11, 6, 1, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_1 + "_5", 50, 5, 9, 1, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_1 + "_6", 50, 11, 9, 1, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_1 + "_7", 50, 5, 12, 1, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_1 + "_8", 50, 11, 12, 1, 0.5));

        i++;
        classPoints[i].addAll(generatePoints(CLASS_2 + "_1", 50, 19, 7.5, 0.5, 2.75));
        classPoints[i].addAll(generatePoints(CLASS_2 + "_2", 50, 23, 7.5, 0.5, 2.75));
        classPoints[i].addAll(generatePoints(CLASS_2 + "_3", 50, 27, 7.5, 0.5, 2.75));

        i++;
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_1", 50, 4, 18, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_2", 50, 5.5, 18, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_3", 50, 7, 18, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_4", 50, 8.5, 18, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_5", 50, 10, 18, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_6", 50, 11.5, 18, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_7", 50, 4, 19.5, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_8", 50, 5.5, 19.5, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_9", 50, 7, 19.5, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_10", 50, 8.5, 19.5, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_11", 50, 10, 19.5, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_12", 50, 11.5, 19.5, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_13", 50, 4, 21, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_14", 50, 5.5, 21, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_15", 50, 7, 21, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_16", 50, 8.5, 21, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_17", 50, 10, 21, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_1_18", 50, 11.5, 21, 0.25, 0.25));
        classPoints[i].addAll(generatePoints(CLASS_3 + "_2", 50, 8, 24.5, 2.5, 1.25));

        i++;
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_1", 50, 19, 18, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_2", 50, 20, 18, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_3", 50, 21, 18, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_4", 50, 22, 18, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_5", 50, 23, 18, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_6", 50, 24, 18, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_7", 50, 25, 18, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_8", 50, 26, 18, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_9", 50, 27, 18, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_10", 50, 19, 19, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_11", 50, 20, 19, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_12", 50, 21, 19, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_13", 50, 22, 19, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_14", 50, 23, 19, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_15", 50, 24, 19, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_16", 50, 25, 19, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_17", 50, 26, 19, 0.125, 0.125));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_1_18", 50, 27, 19, 0.125, 0.125));

        classPoints[i].addAll(generatePoints(CLASS_4 + "_2", 50, 20, 22.5, 1, 0.75));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_3", 50, 26, 22.5, 1, 0.75));
        classPoints[i].addAll(generatePoints(CLASS_4 + "_4", 50, 23, 26.5, 2.5, 0.75));

        i++;
        classPoints[i].addAll(generatePoints(CLASS_5 + "_1", 50, 8, 33, 2.5, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_5 + "_2", 50, 8, 36, 2.5, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_5 + "_3", 50, 8, 39, 2.5, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_5 + "_4", 50, 8, 42, 2.5, 0.5));

        i++;
        classPoints[i].addAll(generatePoints(CLASS_6 + "_1", 50, 20, 33.5, 1, 0.75));
        classPoints[i].addAll(generatePoints(CLASS_6 + "_2", 50, 26, 33.5, 1, 0.75));

        classPoints[i].addAll(generatePoints(CLASS_6 + "_3_1", 50, 19, 37.5, 0.25, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_6 + "_3_2", 50, 20.5, 37.5, 0.25, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_6 + "_3_3", 50, 22, 37.5, 0.25, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_6 + "_3_4", 50, 23.5, 37.5, 0.25, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_6 + "_3_5", 50, 25, 37.5, 0.25, 0.5));
        classPoints[i].addAll(generatePoints(CLASS_6 + "_3_6", 50, 26.5, 37.5, 0.25, 0.5));

        classPoints[i].addAll(generatePoints(CLASS_6 + "_4", 50, 20, 41.5, 1, 0.75));
        classPoints[i].addAll(generatePoints(CLASS_6 + "_5", 50, 26, 41.5, 1, 0.75));

        for (Vector<DataPoint> classPoint : classPoints) {
            allPoints.addAll(classPoints[i]);
        }

        // wenn alle generiert +minval damit alle positiv sind
        makeNonNegative(allPoints);
    }

    public void printDataSet() {
        System.out.println(getDataSetAsString());
    }

    public String getDataSetAsString() {
        StringBuffer sb = new StringBuffer();
        sb.append("$TYPE vec\n");
        sb.append("$XDIM " + allPoints.size() + "\n");
        sb.append("$YDIM 1\n");
        sb.append("$VEC_DIM 2\n");

        // somlib format
        for (int i = 0; i < allPoints.size(); i++) {
            DataPoint d = allPoints.elementAt(i);
            sb.append(d.getX() + " " + d.getY() + " " + d.getName() + "\n");
        }

        sb.append("\n");

        // for matlab
        sb.append("X = [");
        for (int i = 0; i < allPoints.size(); i++) {
            DataPoint d = allPoints.elementAt(i);
            sb.append(d.getX() + " ");
        }
        sb.append("]\n");
        sb.append("Y = [");
        for (int i = 0; i < allPoints.size(); i++) {
            DataPoint d = allPoints.elementAt(i);
            sb.append(d.getY() + " ");
        }
        sb.append("]\n");
        return sb.toString();
    }

    /**
     * @param points vector of dataPoints
     */
    private void makeNonNegative(Vector<DataPoint> points) {
        double minXValue = Double.MAX_VALUE;
        double minYValue = Double.MAX_VALUE;
        for (DataPoint d : points) {
            if (d.getX() < minXValue) {
                minXValue = d.getX();
            }
            if (d.getY() < minYValue) {
                minYValue = d.getY();
            }
        }

        double xoff = 0, yoff = 0;
        if (minXValue < 0) {
            xoff = Math.abs(minXValue);
        }
        if (minYValue < 0) {
            yoff = Math.abs(minYValue);
        }
        for (int i = 0; i < points.size(); i++) {
            DataPoint d = points.elementAt(i);
            d.setLocation(d.getX() + xoff, d.getY() + yoff);
        }
    }

    public static void main(String[] args) {
        new DataSetGenerator();
    }

    private Vector<DataPoint> generatePoints(String name, int num, double x, double y, double sigmaX, double sigmaY) {
        Vector<DataPoint> res = new Vector<DataPoint>(num);
        for (int i = 0; i < num; i++) {
            res.addElement(new DataPoint(name + "-" + (i + 1), rand.nextGaussian(x, sigmaX), rand.nextGaussian(y,
                    sigmaY)));
        }
        return res;
    }

    public DataPoint[] getPoints() {
        return allPoints.toArray(new DataPoint[allPoints.size()]);
    }

    public double[][] getPointsAsDoubles() {
        double[][] result = new double[allPoints.size()][2];
        for (int i = 0; i < allPoints.size(); i++) {
            result[i][0] = allPoints.elementAt(i).getX();
            result[i][1] = allPoints.elementAt(i).getY();
        }
        return result;
    }

    public double[][] get3DPointsAsDoubles() {
        double[][] result = new double[allPoints.size()][3];
        for (int i = 0; i < allPoints.size(); i++) {
            result[i][0] = allPoints.elementAt(i).getX();
            result[i][1] = allPoints.elementAt(i).getY();
            result[i][2] = allPoints.elementAt(i).getY() * allPoints.elementAt(i).getX() / 2;
        }
        return result;
    }

    public float[][] getPointsAsFloats() {
        return doublesToFloats(getPointsAsDoubles());
    }

    public float[][] get3DPointsAsFloats() {
        return doublesToFloats(get3DPointsAsDoubles());
    }

    private float[][] doublesToFloats(double[][] doubles) {
        float[][] floats = new float[doubles.length][doubles[0].length];
        for (int i = 0; i < doubles.length; i++) {
            for (int j = 0; j < doubles[i].length; j++) {
                floats[i][j] = (float) doubles[i][j];
            }
        }
        return floats;
    }

    public double[][][] getData() {
        double[][][] data = new double[CLASS_NAMES.length][][];
        for (int i = 0; i < data.length; i++) {
            data[i] = new double[classPoints[i].size()][2];
            for (int j = 0; j < classPoints[i].size(); j++) {
                data[i][j][0] = classPoints[i].elementAt(j).getX();
                data[i][j][1] = classPoints[i].elementAt(j).getY();
            }
        }
        return data;
    }

    public double[][][] getData3D() {
        double[][][] data = new double[CLASS_NAMES.length][][];
        for (int i = 0; i < data.length; i++) {
            data[i] = new double[classPoints[i].size()][2];
            for (int j = 0; j < classPoints[i].size(); j++) {
                data[i][j][0] = classPoints[i].elementAt(j).getX();
                data[i][j][1] = classPoints[i].elementAt(j).getY();
                data[i][i][2] = classPoints[i].elementAt(i).getY() * allPoints.elementAt(i).getX() / 2;
            }
        }
        return data;
    }

}
