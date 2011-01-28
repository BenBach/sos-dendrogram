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
package at.tuwien.ifs.somtoolbox.apps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.clustering.Cluster;
import at.tuwien.ifs.somtoolbox.clustering.DistanceFunctionType;
import at.tuwien.ifs.somtoolbox.clustering.WardClustering;
import at.tuwien.ifs.somtoolbox.clustering.functions.ComponentLine3DDistance;
import at.tuwien.ifs.somtoolbox.input.SecondLifeInputFileReader;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.structures.ComponentLine3D;
import at.tuwien.ifs.somtoolbox.util.Point3d;
import at.tuwien.ifs.somtoolbox.util.VectorTools;
import at.tuwien.ifs.somtoolbox.visualization.Snapper3D;
import at.tuwien.ifs.somtoolbox.visualization.clustering.KMeans;

/**
 * Main class to {@link SecondLifeInputFileReader}
 * 
 * @author Robert Neumayer
 * @version $Id: SecondLifeFileReaderMain.java 3929 2010-11-09 12:08:24Z mayer $
 */
public class SecondLifeFileReaderMain {

    public static void plot(double[][][] snappedCentroids, double xSize, double ySize, double zSize) {
        // FIXME
        double[][][] plots = new double[1][][];

        UnitFileViewer ufv;

        plots[0] = snappedCentroids[0];
        // plots[1] = snappedCentroids[1];

        ufv = new UnitFileViewer("metro experiments centroids", plots, null);
        ufv.gridPlot(snappedCentroids, null, xSize, ySize, zSize);
        ufv.setVisible(true);

    }

    public static void plot(double[][][] positions, double[][][] centroids, double[][][] snappedCentroids,
            double xSize, double ySize, double zSize) {
        // FIXME
        double[][][] plots = new double[6][][];

        /*
         * plots[0] = snapper.convert(x[0]); System.out.println(ArrayUtils.toString(plots[0])); ufv = new UnitFileViewer("metro experiments", plots,
         * null); ufv.gridPlot(plots, null); ufv.setVisible(true);
         */
        UnitFileViewer ufv;
        /*
         * ufv = new UnitFileViewer("metro experiments all data", plots, null); // plots[0] = snapper.convert(positions); plots[0] = positions[0];
         * ufv.plot(plots, null, xSize, ySize, zSize); ufv.setVisible(true);
         */

        plots[0] = centroids[0];
        plots[1] = centroids[1];
        plots[2] = snappedCentroids[0];
        plots[3] = snappedCentroids[1];
        plots[4] = positions[0];
        plots[5] = positions[1];

        // ufv.gridPlot(plots, null, xSize, ySize, zSize);
        // ufv.setVisible(true);

        ufv = new UnitFileViewer("metro experiments centroids", plots, null);
        // System.out.println(ArrayUtils.toString(plots[0]));

        // ufv = new UnitFileViewer("metro experiments snapped centroids", plots, null);
        ufv.gridPlot(plots, null, xSize, ySize, zSize);
        ufv.setVisible(true);

    }

    // int numberOfSteps = 2;
    /*
     * double[][] data = new double[5][2]; double[] data0 = {22d, 21d}; data[0] = data0; double[] data1 = {19d, 20d}; data[1] = data1; double[] data2
     * = {18d, 22d}; data[2] = data2; double[] data3 = {1d, 3d}; data[3] = data3; double[] data4 = {3d, 2d}; data[4] = data4;
     */

    public static double[][][] getSnappedCentroids(double[][][] centroids, double xSize, double ySize, double zSize,
            DistanceFunctionType lineDistanceFunction) {

        double factor = 2d;
        xSize /= factor;
        ySize /= factor;
        zSize /= factor;

        centroids = VectorTools.divide(centroids, factor);

        Snapper3D snapper = new Snapper3D(new L2Metric(), lineDistanceFunction);
        snapper.createGrid((int) xSize, (int) ySize, (int) zSize);

        double[][][] snappedCentroids = new double[centroids.length][][];
        for (int i = 0; i < centroids.length; i++) {
            try {
                System.out.println("snapping: " + ArrayUtils.toString(centroids[i]));
                Point3d[] pointArray = snapper.doSnapping(centroids[i]);
                System.out.println("snapped to: " + ArrayUtils.toString(pointArray));
                snappedCentroids[i] = SecondLifeInputFileReader.point2DoubleArray(pointArray);
            } catch (SOMToolboxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return VectorTools.multiply(snappedCentroids, factor);
        // return snappedCentroids;

    }

    public static double[][][] getCentroidsPerAvatar(int k, double[][][] positions) {
        double[][][] centroidsForAvatars = new double[positions.length][][];
        for (int i = 0; i < positions.length; i++) {
            System.out.println("Clustering Avatar " + (i + 1) + " of " + positions.length);
            KMeans kmeans = new KMeans(k, positions[i]);
            kmeans.train();
            centroidsForAvatars[i] = kmeans.getClusterCentroids();
        }
        return centroidsForAvatars;
    }

    public static Point3d[][] aggregateLines(double[][][] centroids, int aggregationTargetNumberOfComponents,
            DistanceFunctionType lineDistanceFunction) {
        List<? extends Cluster<ComponentLine3D>> clusters;
        ArrayList<ComponentLine3D> data = SecondLifeInputFileReader.double2ComponentLineArray(centroids);
        ComponentLine3DDistance dist = new ComponentLine3DDistance(lineDistanceFunction);
        WardClustering<ComponentLine3D> wardClustering = new WardClustering<ComponentLine3D>(dist,
                aggregationTargetNumberOfComponents);
        clusters = wardClustering.doCluster(data);
        Point3d[][] centroidLines = new Point3d[clusters.size()][];

        for (int i = 0; i < clusters.size(); i++) {
            centroidLines[i] = dist.meanObject(clusters.get(i)).getPoints();
            System.out.println("centroidlines(" + i + ")" + ArrayUtils.toString(centroidLines[i]));
        }
        return centroidLines;
    }

    public static void writeOut(String fileName, double[][][] data) {
        for (int h = 0; h < data.length; h++) {
            for (int i = 0; i < data[h].length; i++) {
                String s = "";
                for (int j = 0; j < data[h][i].length; j++) {
                    s += data[h][i][j];
                    // if(i != (data[h].length - 1)) // && j != (data[h][i].length - 1))
                    if (!(i * j == (data[h].length - 1) * (data[h][i].length - 1))) {
                        s += " ";
                    }
                }
                writeOut(fileName, s);
            }
            // s += "\n";
            writeOut(fileName, "\n");

        }
    }

    public static void writeOut(String fileName, String s) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(fileName), true));
            out.write(s);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args Command Line args
     */
    public static void main(String[] args) {
        SecondLifeInputFileReader slifr = null;
        try {
            // slifr = new SecondLifeInputFileReader("/home/neumayer/generali/Generali/SL_dump-head600_sorted.csv");
            slifr = new SecondLifeInputFileReader("/home/neumayer/generali/Generali/SL_dump_sorted.csv");
            // slifr = new SecondLifeInputFileReader("/home/neumayer/SL_dump_sorted.csv");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // System.out.println(ArrayUtils.toString(slifr.getPositions()));
        String[] avatarNames = slifr.getDistinctAvatarNames();
        System.out.println("found avatar names: " + ArrayUtils.toString(avatarNames));
        double[][][] positions = new double[avatarNames.length][][];

        for (int i = 0; i < avatarNames.length; i++) {
            positions[i] = slifr.getAvatarPositions(avatarNames[i]);
            // if(i < 5){
            // System.out.println(ArrayUtils.toString(avatarNames[i]));
            // System.out.println("\t" + ArrayUtils.toString(positions[i]));
            // }
        }
        // Point3d[] positions = slifr.getPositions();
        System.out.println("Fetched coordinates for " + positions.length + " avatars for region "
                + slifr.getRegions()[0]);
        int k = 12;

        double[][][] centroids = SecondLifeFileReaderMain.getCentroidsPerAvatar(k, positions);

        // double yoke = 0;
        System.out.println("Maxvalues: " + ArrayUtils.toString(slifr.getMaxValues()));
        double xSize = slifr.getMaxValues()[0], ySize = slifr.getMaxValues()[1], zSize = slifr.getMaxValues()[2];

        DistanceFunctionType lineDistanceFunction = DistanceFunctionType.Euclidean;

        double[][][] snappedCentroids = SecondLifeFileReaderMain.getSnappedCentroids(centroids, xSize, ySize, zSize,
                lineDistanceFunction);

        // double[][][] centroids = kmeans.getMinMaxNormalisedClusterCentroidsWithin();

        // SecondLifeFileReaderMain.plot(positions, centroids, snappedCentroids, xSize, ySize, zSize);

        // SecondLifeFileReaderMain.plot(positions, centroids, snappedCentroids, xSize, ySize, zSize);
        // SecondLifeFileReaderMain.plot(positions, centroids, snappedCentroids, xSize, ySize, zSize);
        // SecondLifeFileReaderMain.plot(positions, centroids, snappedCentroids, xSize, ySize, zSize);

        double[][][] aggregatedCentroids = SecondLifeInputFileReader.point2DoubleArray(SecondLifeFileReaderMain.aggregateLines(
                centroids, 12, lineDistanceFunction));

        // for (int i = 0; i < aggregatedCentroids.length; i++) {
        // for (int j = 0; j < aggregatedCentroids[0].length; j++) {
        // System.out.println(ArrayUtils.toString(aggregatedCentroids[i][j]));
        // }
        // }

        double[][][] aggregatedSnappedCentroids = SecondLifeFileReaderMain.getSnappedCentroids(aggregatedCentroids,
                xSize, ySize, zSize, lineDistanceFunction);

        SecondLifeFileReaderMain.plot(positions, xSize, ySize, zSize);
        SecondLifeFileReaderMain.plot(centroids, xSize, ySize, zSize);
        SecondLifeFileReaderMain.plot(snappedCentroids, xSize, ySize, zSize);
        SecondLifeFileReaderMain.plot(aggregatedSnappedCentroids, xSize, ySize, zSize);

        writeOut("generali.snapped-" + k + "-means-per-avatar.out.txt", snappedCentroids);
        writeOut("generali.all-" + k + "-means-per-avatar.out.txt", positions);
        writeOut("generali.centroids-" + k + "-means-per-avatar.out.txt", centroids);
        writeOut("generali.aggregated-" + k + "-means-per-avatar.out.txt", aggregatedCentroids);
        writeOut("generali.aggregatedsnapped-" + k + "-means-per-avatar.out.txt", aggregatedSnappedCentroids);

    }
}
