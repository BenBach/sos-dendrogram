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
package at.tuwien.ifs.somtoolbox.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.input.SOMLibMapDescription;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.Layer.GridLayout;
import at.tuwien.ifs.somtoolbox.layers.Layer.GridTopology;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.quality.AbstractQualityMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.properties.FileProperties;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;
import at.tuwien.ifs.somtoolbox.util.comparables.UnitDistance;

/**
 * Gathers methods to write trained maps in the SOMLib file format
 * 
 * @author Michael Dittenbach
 * @version $Id: SOMLibMapOutputter.java 3981 2010-12-16 17:32:27Z mayer $
 */
public class SOMLibMapOutputter {

    /**
     * Writes a SOM (or SOM-like) structure to SOMLib format. Usually writes a Map Description File, Weight Vector File
     * and Unit Description File.
     * 
     * @param gsom The growing SOM to be written.
     * @param fDir Directory where to write the files to.
     * @param fName Filename without suffix. Usually the name of the training run.
     * @param gzipped Determines whether the written files should be gzipped or not. A &quot;.gz&quot; suffix is
     *            automatically appended.
     */
    public static void write(GrowingSOM gsom, String fDir, String fName, boolean gzipped, SOMProperties somProps,
            FileProperties fileProps) throws IOException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start saving SOMLib output files");

        writeWeightVectorFile(gsom, fDir, fName, gzipped);
        writeUnitDescriptionFile(gsom, fDir, fName, gzipped);
        writeMapDescriptionFile(gsom, fDir, fName, somProps, fileProps);

        if (gsom.getLayer().getVirtualLayer() != null) {
            // write adaptive coordinates file
            gsom.getLayer().getVirtualLayer().writeToFile(fDir, fName);
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished saving SOMLib output files");

    }

    public static void write(GHSOM ghsom, String fDir, String fName, boolean gzipped, SOMProperties somProps,
            FileProperties fileProps) throws IOException {
        write(ghsom.topLayerMap(), fDir, fName, gzipped, somProps, fileProps);
    }

    /**
     * Writes the Weight Vector File.
     * 
     * @param gsom The growing SOM to be written.
     * @param fDir Directory where to write the file to.
     * @param fName fName Filename without suffix. Usually the name of the training run.
     * @param gzipped Determines whether the written files should be gzipped or not. A &quot;.gz&quot; suffix is
     *            automatically appended.
     */
    public static void writeWeightVectorFile(GrowingSOM gsom, String fDir, String fName, boolean gzipped,
            String... extraHeaders) throws IOException {
        if (!fDir.endsWith(File.separator)) {
            fDir = fDir + File.separator;
        }
        int xDim = gsom.getLayer().getXSize();
        int yDim = gsom.getLayer().getYSize();
        int zDim = gsom.getLayer().getZSize();
        int vecDim = gsom.getLayer().getDim();
        String idString = gsom.getLayer().getIdString();
        BufferedWriter bw = writeWeightVectorFileHeader(fDir, fName, gzipped, xDim, yDim, zDim,
                gsom.getLayer().getGridLayout(), gsom.getLayer().getGridTopology(), vecDim, idString, extraHeaders);
        int numUnits = xDim * yDim * zDim;
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(numUnits, "Writing weight vector of unit ");
        // int unitsSoFar = 0;
        for (int k = 0; k < zDim; k++) {
            for (int j = 0; j < yDim; j++) {
                for (int i = 0; i < xDim; i++) {
                    progressWriter.progress(); // "Writing weight vector of unit " + i + "/" + j + "/" + k + ", ",
                    // ++unitsSoFar);
                    Unit u = null;
                    try {
                        u = gsom.getLayer().getUnit(i, j, k);
                    } catch (LayerAccessException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    // adapted to menmonic (sparse) SOMs
                    if (u != null) {
                        if (u.getMappedSOM() != null) {
                            writeWeightVectorFile(u.getMappedSOM(), fDir, fName, gzipped);
                        }
                        for (int ve = 0; ve < vecDim; ve++) {
                            // bw.write(form.format(u.weightVector()[ve])+" ");
                            bw.write(u.getWeightVector()[ve] + " ");
                        }
                    } else {
                        bw.write("null ");
                    }
                    bw.write("SOM_MAP_" + fName + "_(" + i + "/" + j + "/" + k + ")");
                    bw.newLine();
                }
            }
        }
        bw.close();
    }

    /**
     * Writes the Weight Vector File from given weight values, used e.g. when interpolating a SOM.
     */
    public static void writeWeightVectorFile(double[][][][] weights, GridLayout gridLayout, GridTopology gridTopology,
            String fDir, String fName, boolean gzipped) throws IOException {
        int xDim = weights.length;
        int yDim = weights[0].length;
        int zDim = weights[0][0].length;
        int vecDim = weights[0][0][0].length;
        String idString = "";
        BufferedWriter bw = writeWeightVectorFileHeader(fDir, fName, gzipped, xDim, yDim, zDim, gridLayout,
                gridTopology, vecDim, idString);
        int numUnits = xDim * yDim * zDim;
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(numUnits, "Writing weight vector of unit ");
        // int writtenSoFar = 0;
        for (int k = 0; k < zDim; k++) {
            for (int j = 0; j < yDim; j++) {
                for (int i = 0; i < xDim; i++) {
                    progressWriter.progress();// "Writing weight vector of unit " + i + "/" + j + "/" + k + ", ",
                    // ++writtenSoFar);
                    for (int ve = 0; ve < vecDim; ve++) {
                        bw.write(weights[i][j][k][ve] + " ");
                    }
                    bw.write("SOM_MAP_" + fName + "_(" + i + "/" + j + "/" + k + ")");
                    bw.newLine();
                }
            }
        }
        bw.close();
    }

    /** Writes the header part of the weight vector file. */
    protected static BufferedWriter writeWeightVectorFileHeader(String fDir, String fName, boolean gzipped, int xDim,
            int yDim, int zDim, GridLayout gridLayout, GridTopology gridTopology, int vecDim, String idString,
            String... extraHeaders) throws IOException, FileNotFoundException {
        BufferedWriter bw = null;
        String finalName = getWeightVectorFileName(fDir, fName, idString, gzipped);
        if (gzipped == true) {
            bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(finalName))));
        } else {
            bw = new BufferedWriter(new FileWriter(finalName));
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Saving SOMLib weight vector file" + finalName + " (" + new File(finalName).getAbsolutePath() + ")");

        bw.write("$TYPE som");
        bw.newLine();
        bw.write(SOMLibMapDescription.GRID_LAYOUT + " " + gridLayout.toString());
        bw.newLine();
        bw.write(SOMLibMapDescription.GRID_TOPOLOGY + " " + gridTopology.toString());
        bw.newLine();
        bw.write("$XDIM " + xDim);
        bw.newLine();
        bw.write("$YDIM " + yDim);
        bw.newLine();
        bw.write("$ZDIM " + zDim);
        bw.newLine();
        bw.write("$VEC_DIM " + vecDim);
        bw.newLine();

        for (String header : extraHeaders) {
            bw.write(header);
            bw.newLine();
        }

        return bw;
    }

    private static BufferedWriter openAndWriteUnitFileHeader(String fDir, String fName, String id, int xSize,
            int ySize, int zSize, GridLayout gridLayout, GridTopology gridTopology, boolean gzipped) throws IOException {
        BufferedWriter bw = null;
        String finalName = getUnitDescriptionFileName(fDir, fName, id, gzipped);

        if (gzipped == true) {
            bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(finalName))));
        } else {
            bw = new BufferedWriter(new FileWriter(finalName));
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Saving SOMLib unit description file " + finalName + " (" + new File(finalName).getAbsolutePath() + ")");

        bw.write("$TYPE som");
        bw.newLine();
        bw.write(SOMLibMapDescription.GRID_LAYOUT + " " + gridLayout.toString());
        bw.newLine();
        bw.write(SOMLibMapDescription.GRID_TOPOLOGY + " " + gridTopology.toString());
        bw.newLine();
        bw.write("$FILE_FORMAT_VERSION 1.2"); // added by frank, because of 3d-som
        bw.newLine(); // added by frank
        bw.write("$XDIM " + xSize);
        bw.newLine();
        bw.write("$YDIM " + ySize);
        bw.newLine();

        // Important: older versions of the SOMToolbox have pretty stupid reader implementations, and will fail if
        // there's a $ZDIM
        // thus, write ZDIM only if relevant, i.e. > 1; additionally, this also saves space in the output file
        if (zSize > 1) {
            bw.write("$ZDIM " + zSize);
            bw.newLine();
        }
        return bw;
    }

    /**
     * Writes the Unit Description File.
     * 
     * @param gsom The growing SOM to be written.
     * @param fDir Directory where to write the file to.
     * @param fName fName Filename without suffix. Usually the name of the training run.
     * @param gzipped Determines whether the written files should be gzipped or not. A &quot;.gz&quot; suffix is
     *            automatically appended.
     */
    public static void writeUnitDescriptionFile(GrowingSOM gsom, String fDir, String fName, boolean gzipped)
            throws IOException {

        BufferedWriter bw = openAndWriteUnitFileHeader(fDir, fName, gsom.getLayer().getIdString(),
                gsom.getLayer().getXSize(), gsom.getLayer().getYSize(), gsom.getLayer().getZSize(),
                gsom.getLayer().getGridLayout(), gsom.getLayer().getGridTopology(), gzipped);

        for (int k = 0; k < gsom.getLayer().getZSize(); k++) {
            for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
                for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                    Unit u = null;
                    try {
                        u = gsom.getLayer().getUnit(i, j, k);
                    } catch (LayerAccessException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    // adapted to mnemonic (sparse) SOMs
                    if (u != null) {
                        writeUnitInfoBegin(fName, bw, j, i, k, gsom.getLayer().getZSize());

                        if (u.getLayer().getQualityMeasure() != null) {
                            if (u.getLayer().getQualityMeasure().getClass().getName().equals(
                                    "at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError")) {
                                try {
                                    bw.write("$QUANTERROR_UNIT "
                                            + u.getLayer().getQualityMeasure().getUnitQualities("qe")[u.getXPos()][u.getYPos()]);
                                    bw.newLine();
                                    bw.write("$QUANTERROR_UNIT_AVG "
                                            + u.getLayer().getQualityMeasure().getUnitQualities("mqe")[u.getXPos()][u.getYPos()]);
                                    bw.newLine();
                                } catch (QualityMeasureNotFoundException e) {
                                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                            e.getMessage() + " Aborting. BTW: the must be a major flaw"
                                                    + "in the quality measure class that has been used.");
                                    System.exit(-1);
                                }
                            }
                        }

                        // we also want to write $NR_VEC_MAPPED 0 if there are no vectors.
                        bw.write("$NR_VEC_MAPPED " + u.getNumberOfMappedInputs());
                        bw.newLine();
                        if (u.getNumberOfMappedInputs() > 0) {
                            writeMappedVectors(bw, u.getMappedInputNames(), u.getMappedInputDistances());
                        }
                        if (u.getMappedSOM() != null) {
                            bw.write("$NR_SOMS_MAPPED 1");
                            bw.newLine();
                            bw.write("$URL_MAPPED_SOMS " + fName + u.getMappedSOM().getLayer().getIdString()); // TODO:
                            // removed
                            // +suffix
                            bw.newLine();
                            writeUnitDescriptionFile(u.getMappedSOM(), fDir, fName, gzipped);
                        }
                        if (u.getLabels() != null) {
                            bw.write("$NR_UNIT_LABELS " + u.getLabels().length);
                            bw.newLine();
                            bw.write("$UNIT_LABELS");
                            for (int l = 0; l < u.getLabels().length; l++) {
                                bw.write(" " + u.getLabels()[l].getName());
                            }
                            bw.newLine();
                            bw.write("$UNIT_LABELS_QE");
                            for (int l = 0; l < u.getLabels().length; l++) {
                                bw.write(" " + u.getLabels()[l].getQe());
                            }
                            bw.newLine();
                            bw.write("$UNIT_LABELS_WGT");
                            for (int l = 0; l < u.getLabels().length; l++) {
                                bw.write(" " + u.getLabels()[l].getValue());
                            }
                            bw.newLine();
                        }
                    }
                }
            }
        }
        bw.close();
    }

    private static void writeUnitInfoBegin(String fName, BufferedWriter bw, int j, int i, int k, int zSize)
            throws IOException {
        bw.write("$POS_X " + i);
        bw.newLine();
        bw.write("$POS_Y " + j);
        bw.newLine();
        // Important: older versions of the SOMToolbox have pretty stupid reader implementations, and will fail if
        // there's a $POS_Z
        // thus, write $POS_Z only if relevant, i.e. > 1; additionally, this also saves space in the output file
        if (zSize > 1) {
            bw.write("$POS_Z " + k);
            bw.newLine();
            bw.write("$UNIT_ID " + fName + "_(" + i + "/" + j + "/" + k + ")");
        } else {
            bw.write("$UNIT_ID " + fName + "_(" + i + "/" + j + ")");
        }
        bw.newLine();
    }

    private static void writeMappedVectors(BufferedWriter bw, String[] datalabels, double[] dataDistances)
            throws IOException {
        bw.write("$MAPPED_VECS");
        bw.newLine(); // added by lidy
        for (String datalabel : datalabels) {
            bw.write(datalabel); // changed by lidy in order to support spaces in filenames
            bw.newLine(); // added by lidy in order to support spaces in filenames
        }
        bw.write("$MAPPED_VECS_DIST");
        for (double dataDistance : dataDistances) {
            bw.write(" " + dataDistance);
        }
        bw.newLine();
    }

    /** Method to write a unit description file of an empty map. */
    public static void writeUnitDescriptionFile(int xdim, int ydim, GridLayout gridLayout, GridTopology gridTopology,
            String fDir, String fName, boolean gzipped) throws IOException {
        BufferedWriter bw = openAndWriteUnitFileHeader(fDir, fName, null, xdim, ydim, 1, gridLayout, gridTopology,
                gzipped);
        for (int j = 0; j < ydim; j++) {
            for (int i = 0; i < xdim; i++) {
                writeUnitInfoBegin(fName, bw, j, i, 1, 1);
                bw.write("$NR_VEC_MAPPED " + 0);
                bw.newLine();
            }
        }
        bw.close();
    }

    public static void writeUnitDescriptionFile(String[][][] labels, GridLayout gridLayout, GridTopology gridTopology,
            String fDir, String fName, boolean gzipped) throws IOException {
        BufferedWriter bw = openAndWriteUnitFileHeader(fDir, fName, null, labels.length, labels[0].length, 1,
                gridLayout, gridTopology, gzipped);
        for (int i = 0; i < labels.length; i++) {
            for (int j = 0; j < labels[i].length; j++) {
                writeUnitInfoBegin(fName, bw, j, i, 1, 1);
                bw.write("$NR_VEC_MAPPED " + labels[i][j].length);
                bw.newLine();
                writeMappedVectors(bw, labels[i][j], VectorTools.generateVectorWithValue(labels[i][j].length, 0));
            }
        }
        bw.close();
    }

    public static void writeMapDescriptionFile(GrowingSOM topLevelMap, String fDir, String fName,
            SOMProperties somProps, FileProperties fileProps) throws IOException {
        Stack<GrowingSOM> maps = new Stack<GrowingSOM>(); // store all maps to process
        maps.add(topLevelMap); // start from first map

        while (maps.size() > 0) { // process all remaining maps
            GrowingSOM gsom = maps.pop();
            maps.addAll(gsom.getLayer().getAllSubMaps());
            String idString = gsom.getLayer().getIdString();
            String finalName = getMapDescriptionFileName(fDir, fName, idString, false);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Saving SOMLib map description file " + finalName + " (" + new File(finalName).getAbsolutePath()
                            + ")");

            SOMLibMapDescription mapDescription = new SOMLibMapDescription();
            final InputData inputData = gsom.getSharedInputObjects().getInputData();
            try {
                // TODO in the future: check if GHSOM, Growing Grid, Mnemomic SOM, ...
                mapDescription.setProperty(SOMLibMapDescription.TYPE, "som");
                mapDescription.setProperty(SOMLibMapDescription.GRID_LAYOUT, gsom.getLayer().getGridLayout().toString());
                mapDescription.setProperty(SOMLibMapDescription.GRID_TOPOLOGY,
                        gsom.getLayer().getGridTopology().toString());
                mapDescription.setProperty(SOMLibMapDescription.X_DIM, gsom.getLayer().getXSize());
                mapDescription.setProperty(SOMLibMapDescription.Y_DIM, gsom.getLayer().getYSize());
                mapDescription.setProperty(SOMLibMapDescription.Z_DIM, gsom.getLayer().getZSize());
                mapDescription.setProperty(SOMLibMapDescription.VEC_DIM, gsom.getLayer().getDim());
                mapDescription.setProperty(SOMLibMapDescription.STORAGE_DATE, new SimpleDateFormat().format(new Date()));
                mapDescription.setProperty(SOMLibMapDescription.TRAINING_TIME, ""); // TODO
                mapDescription.setProperty(SOMLibMapDescription.LEARNRATE_TYPE, "exponential");// TODO: detailed formula
                mapDescription.setProperty(SOMLibMapDescription.LEARNRATE_INIT, somProps.learnrate());
                mapDescription.setProperty(SOMLibMapDescription.NEIGHBOURHOOD_TYPE, "exponential");// TODO: detailed
                // formula
                mapDescription.setProperty(SOMLibMapDescription.NEIGHBOURHOOD_INIT, somProps.sigma());
                mapDescription.setProperty(SOMLibMapDescription.RANDOM_INIT, somProps.randomSeed());
                mapDescription.setProperty(SOMLibMapDescription.TOTAL_ITERATIONS,
                        somProps.trainedIterations(inputData.numVectors()));
                mapDescription.setProperty(SOMLibMapDescription.TOTAL_TRAINING_VECTORS, inputData.numVectors());

                if (somProps.adaptiveCoordinatesTreshold() != null) {
                    mapDescription.setProperty(SOMLibMapDescription.ADAPTIVE_COORDINATES_THRESHOLD,
                            somProps.adaptiveCoordinatesTreshold());
                }

                mapDescription.setProperty(SOMLibMapDescription.VECTORS_NORMALISED,
                        inputData.isNormalizedToUnitLength());
                try {
                    // FIXME: more generic file format supporting different measures than mqe
                    String[] growthQM = AbstractQualityMeasure.splitNameAndMethod(somProps.growthQualityMeasureName());
                    final double mapQuality = gsom.getLayer().getQualityMeasure().getMapQuality(growthQM[1]);
                    mapDescription.setProperty(SOMLibMapDescription.QUANT_ERROR_MAP, mapQuality);
                    mapDescription.setProperty(SOMLibMapDescription.QUANT_ERROR_VECTOR, mapQuality
                            / inputData.numVectors());
                } catch (QualityMeasureNotFoundException e) {
                    mapDescription.setProperty(SOMLibMapDescription.QUANT_ERROR_MAP, "<error receiving value: "
                            + e.getMessage() + ">");
                    mapDescription.setProperty(SOMLibMapDescription.QUANT_ERROR_VECTOR, "<error receiving value: "
                            + e.getMessage() + ">");// TODO
                    e.printStackTrace();
                }
                mapDescription.setProperty(SOMLibMapDescription.URL_TRAINING_VECTOR, fileProps.vectorFileName(true));
                mapDescription.setProperty(SOMLibMapDescription.URL_TRAINING_VECTOR_DESCRIPTION, "");// TODO
                String contentType = inputData.getContentType();
                if (org.apache.commons.lang.StringUtils.isNotBlank(inputData.getContentSubType())) {
                    contentType += "-" + inputData.getContentSubType();
                }
                mapDescription.setProperty(SOMLibMapDescription.DATA_TYPE, contentType);
                if (inputData.getFeatureMatrixColumns() != -1 && inputData.getFeatureMatrixRows() != -1) {
                    mapDescription.setProperty(SOMLibMapDescription.DATA_DIM, inputData.getFeatureMatrixColumns() + "x"
                            + inputData.getFeatureMatrixRows());
                }
                mapDescription.setProperty(SOMLibMapDescription.URL_WEIGHT_VECTOR, getWeightVectorFileName(fDir, fName,
                        idString, false));
                mapDescription.setProperty(SOMLibMapDescription.URL_QUANT_ERROR_MAP, "");// TODO
                mapDescription.setProperty(SOMLibMapDescription.URL_MAPPED_INPUT_VECTOR, "");// TODO
                mapDescription.setProperty(SOMLibMapDescription.URL_MAPPED_INPUT_VECTOR_DESCRIPTION, "");// TODO
                mapDescription.setProperty(SOMLibMapDescription.URL_UNIT_DESCRIPTION, getUnitDescriptionFileName(fDir,
                        fName, idString, false));
                mapDescription.setProperty(SOMLibMapDescription.URL_TEMPLATE_VECTOR, fileProps.templateFileName(true));
                mapDescription.setProperty(SOMLibMapDescription.URL_DATA_WINNER_MAPPING, getDataWinnerMappingFileName(
                        fDir, fName, gsom.getLayer().getIdString(), false));
                mapDescription.setProperty(SOMLibMapDescription.URL_CLASS_INFO, ""); // TODO
                mapDescription.setProperty(SOMLibMapDescription.METRIC,
                        gsom.getLayer().getMetric().getClass().getName());
                mapDescription.setProperty(SOMLibMapDescription.LAYER_REVISION, gsom.getLayer().getRevision());
                mapDescription.setProperty(SOMLibMapDescription.DESCRIPTION, "");// TODO

                mapDescription.writeMapDescriptionFile(finalName);
                maps.addAll(gsom.getLayer().getAllSubMaps());
            } catch (SOMToolboxException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /**
     * Simple SOMLib Map description file writer, if you don't have a {@link GrowingSOM} object at hand, e.g. when
     * converting maps.
     */
    public static void writeMapDescriptionFile(int xSize, int ySize, int zSize, int dim, int numVectors, String fDir,
            String fName) throws IOException {
        String idString = "";
        String finalName = getMapDescriptionFileName(fDir, fName, idString, false);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Saving SOMLib map description file " + finalName + " (" + new File(finalName).getAbsolutePath() + ")");

        try {
            SOMLibMapDescription mapDescription = new SOMLibMapDescription();
            // TODO in the future: check if GHSOM, Growing Grid, Mnemomic SOM, ...
            mapDescription.setProperty(SOMLibMapDescription.TYPE, "som");
            mapDescription.setProperty(SOMLibMapDescription.GRID_LAYOUT, GridLayout.rectangular.toString());
            mapDescription.setProperty(SOMLibMapDescription.GRID_TOPOLOGY, GridTopology.planar.toString());
            mapDescription.setProperty(SOMLibMapDescription.X_DIM, xSize);
            mapDescription.setProperty(SOMLibMapDescription.Y_DIM, ySize);
            mapDescription.setProperty(SOMLibMapDescription.Z_DIM, zSize);
            mapDescription.setProperty(SOMLibMapDescription.VEC_DIM, dim);
            mapDescription.setProperty(SOMLibMapDescription.STORAGE_DATE, new SimpleDateFormat().format(new Date()));
            mapDescription.setProperty(SOMLibMapDescription.TOTAL_TRAINING_VECTORS, numVectors);
            mapDescription.setProperty(SOMLibMapDescription.URL_WEIGHT_VECTOR, getWeightVectorFileName(fDir, fName,
                    idString, false));
            mapDescription.setProperty(SOMLibMapDescription.URL_UNIT_DESCRIPTION, getUnitDescriptionFileName(fDir,
                    fName, idString, false));
            mapDescription.setProperty(SOMLibMapDescription.METRIC, new L2Metric().getClass().getName());
            mapDescription.writeMapDescriptionFile(finalName);
        } catch (SOMToolboxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public static void writeDataWinnerMappingFile(GHSOM ghsom, InputData data, int numWinners, String fDir,
            String fName, boolean gzipped) throws IOException {
        Stack<GrowingSOM> maps = new Stack<GrowingSOM>(); // store all maps to process
        maps.add(ghsom.topLayerMap()); // start from first map

        while (maps.size() > 0) { // process all remaining maps
            GrowingSOM map = maps.pop();
            int winners = Math.min(numWinners, map.getLayer().getXSize() * map.getLayer().getYSize());
            // use subset of data
            InputData newData = data.subset(map.getLayer().getSuperUnit().getMappedInputNames());
            writeDataWinnerMappingFile(map, newData, winners, fDir, fName, gzipped);
            maps.addAll(map.getLayer().getAllSubMaps());
        }
    }

    public static void writeDataWinnerMappingFile(GrowingSOM gsom, InputData data, int numWinners, String fDir,
            String fName, boolean gzipped) throws IOException {
        BufferedWriter bw = null;
        String finalName = getDataWinnerMappingFileName(fDir, fName, gsom.getLayer().getIdString(), gzipped);
        if (gzipped == true) {
            bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(finalName))));
        } else {
            bw = new BufferedWriter(new FileWriter(finalName));
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Saving SOMLib data winner mapping file" + finalName + " (" + new File(finalName).getAbsolutePath()
                        + ")");

        int numVectors = data.numVectors();

        int maxWinners = gsom.getLayer().getXSize() * gsom.getLayer().getYSize();
        if (numWinners > maxWinners) {
            numWinners = maxWinners;
        }

        final boolean is3DSOM = gsom.getLayer().getZSize() > 1;

        // FIXME: the header should be independent of whether we write an zSize >0 or not, we should just write an zDim
        // if we actually do have it
        // FIXME: add X/Y/ZDIM headers to the file

        // we don't want to always write the zDim, it just unnecessarily bloats the file size...
        if (is3DSOM) {
            bw.write("$FILE_FORMAT_VERSION 1.2"); // added by frank for 3d-SOM support.
        } else {
            bw.write("$FILE_FORMAT_VERSION 1.1"); // added by lidy, because of changed fileformat (see below)
        }
        bw.newLine(); // added by lidy
        bw.write("$NUM_WINNERS " + numWinners);
        bw.newLine();
        bw.write("$NUM_VECTORS " + numVectors);
        bw.newLine();

        // added by rudi, also write the metric used
        bw.write("$METRIC " + gsom.getLayer().getMetric().getClass().getName());
        bw.newLine();

        UnitDistance[] winners;
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors, "Getting winners for datum ", 10);

        // FIXME: number of threads should be a param
        int noThreads = Math.min(Runtime.getRuntime().availableProcessors(), 4);

        // do some "clever" decision whether to use multi-threading or not, i.e. when we have many units, or many
        // inputs, or both
        if (noThreads > 1 && gsom.getLayer().getUnitCount() * gsom.getLayer().getDim() > 100 * 100) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Working with " + noThreads + " parallel threads");
            ExecutorService e = Executors.newFixedThreadPool(noThreads);
            CountDownLatch latch = null;
            ArrayList<DataWinnerMappingThread> threads = new ArrayList<DataWinnerMappingThread>(noThreads);
            for (int i = 0; i < noThreads; i++) {
                threads.add(new DataWinnerMappingThread(gsom.getLayer(), numWinners));
            }
            for (int d = 0; d < numVectors;) {
                if (d + noThreads > numVectors) {
                    noThreads = numVectors - d;
                    System.out.println("d: " + d + ", capped threads to " + noThreads);
                }
                latch = new CountDownLatch(noThreads);
                for (int i = 0; i < noThreads; i++) {
                    threads.get(i).setData(latch, data.getInputDatum(d++));
                    e.execute(threads.get(i));
                }
                progressWriter.progress(d);
                try {
                    latch.await(); // wait for all processes to finish
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                for (int i = 0; i < noThreads; i++) {
                    bw.write(threads.get(i).output.toString());
                    bw.newLine();
                }
            }
            e.shutdown();
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Working single-threaded");
            for (int d = 0; d < numVectors; d++) {
                progressWriter.progress();
                bw.write(data.getInputDatum(d).getLabel());
                bw.newLine(); // added by lidy in order to support spaces in filenames
                winners = gsom.getLayer().getWinnersAndDistances(data.getInputDatum(d), numWinners);
                // adapted to mnemonic (sparse) SOMs
                for (int w = 0; w < numWinners; w++) { // was: gsom.getLayer().getUnitCount()
                    bw.write(winners[w].getUnit().printCoordinatesSpaceSeparated() + " "
                            + StringUtils.format(winners[w].getDistance(), 5) + " ");
                }
                bw.newLine();
            }
        }

        bw.close();
    }

    public static void writeDataWinnerMappingFile(SOMLibDataWinnerMapping dwm, String fDir, String fName, boolean gzip)
            throws IOException {
        PrintStream bw = null;
        String finalName = getDataWinnerMappingFileName(fDir, fName, "", gzip);
        if (gzip == true) {
            bw = new PrintStream(new GZIPOutputStream(new FileOutputStream(finalName)));
        } else {
            bw = new PrintStream(new FileOutputStream(finalName));
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Saving SOMLib data winner mapping file" + finalName + " (" + new File(finalName).getAbsolutePath()
                        + ")");

        boolean is3DSOM = dwm.is3D();
        // FIXME: the header should be independent of whether we write an zSize >0 or not, we should just write an zDim
        // if we actually do have it
        // FIXME: add X/Y/ZDIM headers to the file

        // we don't want to always write the zDim, it just unnecessarily bloats the file size...
        if (is3DSOM) {
            bw.println("$FILE_FORMAT_VERSION 1.2"); // added by frank for 3d-SOM support.
        } else {
            bw.println("$FILE_FORMAT_VERSION 1.1"); // added by lidy, because of changed fileformat (see below)
        }
        bw.println("$NUM_WINNERS " + dwm.getNumBMUs());
        bw.println("$NUM_VECTORS " + dwm.getNumVectors());

        // added by rudi, also write the metric used
        bw.println("$METRIC " + dwm.getMetric());

        for (String label : dwm.getLabels()) {
            try {
                bw.print(label);
                if (is3DSOM) {
                    bw.println();
                } else {
                    bw.print(" ");
                }
                int dIndex = dwm.getVectPos(label);
                int[] xs = dwm.getXPos(dIndex), ys = dwm.getYPos(dIndex), zs = dwm.getZPos(dIndex);
                double[] dists = dwm.getDists(dIndex);
                for (int i = 0; i < dists.length; i++) {
                    bw.printf(" %d", xs[i]);
                    bw.printf(" %d", ys[i]);
                    if (is3DSOM) {
                        bw.printf(" %d", zs[i]);
                    }
                    bw.printf(" %f", dists[i]);
                }
                bw.println();
            } catch (SOMToolboxException e) {
                throw new IOException(e);
            }
        }

        bw.close();
    }

    static class DataWinnerMappingThread implements Runnable {
        InputDatum datum;

        UnitDistance[] winners;

        GrowingLayer layer;

        int numWinners;

        StringBuilder output;

        CountDownLatch latch;

        DataWinnerMappingThread(GrowingLayer layer, int numWinners) {
            this.layer = layer;
            this.numWinners = numWinners;
        }

        void setData(CountDownLatch latch, InputDatum datum) {
            this.latch = latch;
            this.datum = datum;
            output = new StringBuilder(datum.getLabel()).append("\n");
        }

        @Override
        public void run() {
            winners = layer.getWinnersAndDistances(datum, numWinners);
            for (int w = 0; w < numWinners; w++) { // was: gsom.getLayer().getUnitCount()
                output.append(winners[w].getUnit().printCoordinatesSpaceSeparated()).append(" ").append(
                        StringUtils.format(winners[w].getDistance(), 5)).append(" ");
            }
            latch.countDown();
        }
    }

    protected static String getUnitDescriptionFileName(String fDir, String fName, String idString, boolean gzipped) {
        return FileUtils.getPathPrefix(fDir) + fName + (idString != null ? idString : "")
                + FileUtils.getSuffix("unit", gzipped);
    }

    protected static String getWeightVectorFileName(String fDir, String fName, String idString, boolean gzipped) {
        return FileUtils.getPathPrefix(fDir) + fName + idString + FileUtils.getSuffix("wgt", gzipped);
    }

    protected static String getDataWinnerMappingFileName(String fDir, String fName, String idString, boolean gzipped) {
        return FileUtils.getPathPrefix(fDir) + fName + idString + FileUtils.getSuffix("dwm", gzipped);
    }

    protected static String getMapDescriptionFileName(String fDir, String fName, String idString, boolean gzipped) {
        return FileUtils.getPathPrefix(fDir) + fName + idString + FileUtils.getSuffix("map", gzipped);
    }

}
