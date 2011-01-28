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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibMapDescription;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.AbstractQualityMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.models.GrowingCellStructures;
import at.tuwien.ifs.somtoolbox.properties.FileProperties;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.comparables.UnitDistance;

/**
 * FIXME: most of these methods can probably be unified with the methods in {@link SOMLibMapOutputter}.
 * 
 * @author Johannes Inführ
 * @author Andreas Zweng
 * @version $Id: GrowingCellStructuresMapOutputter.java 3981 2010-12-16 17:32:27Z mayer $
 */
public class GrowingCellStructuresMapOutputter extends SOMLibMapOutputter {

    public static void write(GrowingCellStructures csom, String fDir, String fName, boolean gzipped,
            SOMProperties somProps, FileProperties fileProps) throws IOException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start saving SOMLib output files");
        writeWeightVectorFile(csom, fDir, fName, gzipped);
        writeUnitDescriptionFile(csom, fDir, fName, gzipped);
        writeMapDescriptionFile(csom, fDir, fName, somProps, fileProps);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished saving SOMLib output files");
    }

    private static void writeWeightVectorFile(GrowingCellStructures csom, String fDir, String fName, boolean gzipped)
            throws IOException {
        if (!fDir.endsWith(File.separator)) {
            fDir = fDir + File.separator;
        }
        int xDim = csom.getLayer().getXSize();
        int yDim = csom.getLayer().getYSize();
        int zDim = csom.getLayer().getZSize();
        int vecDim = csom.getLayer().getDim();
        String idString = csom.getLayer().getIdString();
        BufferedWriter bw = writeWeightVectorFileHeader(fDir, fName, gzipped, xDim, yDim, zDim,
                csom.getLayer().getGridLayout(), csom.getLayer().getGridTopology(), vecDim, idString);
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
                        u = csom.getLayer().getUnit(i, j, k);
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

    private static void writeUnitDescriptionFile(GrowingCellStructures csom, String fDir, String fName, boolean gzipped)
            throws IOException {
        if (!fDir.endsWith(File.separator)) {
            fDir = fDir + File.separator;
        }
        BufferedWriter bw = null;
        String finalName = getUnitDescriptionFileName(fDir, fName, csom.getLayer().getIdString(), gzipped);

        if (gzipped == true) {
            bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(finalName))));
        } else {
            bw = new BufferedWriter(new FileWriter(finalName));
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Saving SOMLib unit description file " + finalName + " (" + new File(finalName).getAbsolutePath() + ")");

        bw.write("$TYPE rect");
        bw.newLine();
        bw.write("$FILE_FORMAT_VERSION 1.2"); // added by frank, because of 3d-som
        bw.newLine(); // added by frank
        bw.write("$XDIM " + csom.getLayer().getXSize());
        bw.newLine();
        bw.write("$YDIM " + csom.getLayer().getYSize());
        bw.newLine();
        bw.write("$ZDIM " + csom.getLayer().getZSize());
        bw.newLine();

        for (int k = 0; k < csom.getLayer().getZSize(); k++) {
            for (int j = 0; j < csom.getLayer().getYSize(); j++) {
                for (int i = 0; i < csom.getLayer().getXSize(); i++) {
                    Unit u = null;
                    try {
                        u = csom.getLayer().getUnit(i, j, k);
                    } catch (LayerAccessException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    // adapted to mnemonic (sparse) SOMs
                    if (u != null) {
                        bw.write("$POS_X " + i);
                        bw.newLine();
                        bw.write("$POS_Y " + j);
                        bw.newLine();
                        bw.write("$POS_Z " + k);
                        bw.newLine();
                        bw.write("$UNIT_ID " + fName + "_(" + i + "/" + j + "/" + k + ")");
                        bw.newLine();

                        if (u.getLayer().getQualityMeasure() != null) {
                            if (u.getLayer().getQualityMeasure().getClass().getName().equals(
                                    "at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError")) {
                                try {
                                    bw.write("$QUANTERROR_UNIT "
                                            + u.getLayer().getQualityMeasure().getUnitQualities("qe")[i][j]);
                                    bw.newLine();
                                    bw.write("$QUANTERROR_UNIT_AVG "
                                            + u.getLayer().getQualityMeasure().getUnitQualities("mqe")[i][j]);
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
                            bw.write("$MAPPED_VECS");
                            bw.newLine(); // added by lidy
                            String datalabels[] = u.getMappedInputNames();
                            for (int l = 0; l < u.getNumberOfMappedInputs(); l++) {
                                bw.write(datalabels[l]); // changed by lidy in order to support spaces in filenames
                                bw.newLine(); // added by lidy in order to support spaces in filenames
                            }
                            bw.write("$MAPPED_VECS_DIST");
                            for (int d = 0; d < u.getNumberOfMappedInputs(); d++) {
                                bw.write(" " + u.getMappedInputDistances()[d]);
                            }
                            bw.newLine();
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

    private static void writeMapDescriptionFile(GrowingCellStructures csom, String fDir, String fName,
            SOMProperties somProps, FileProperties fileProps) throws IOException {
        Stack<GrowingCellStructures> maps = new Stack<GrowingCellStructures>(); // store all maps to process
        maps.add(csom); // start from first map

        while (maps.size() > 0) { // process all remaining maps
            GrowingCellStructures csommap = maps.pop();
            maps.addAll(csommap.getLayer().getAllSubMaps());
            String idString = csommap.getLayer().getIdString();
            String finalName = getMapDescriptionFileName(fDir, fName, idString, false);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Saving SOMLib map description file " + finalName + " (" + new File(finalName).getAbsolutePath()
                            + ")");

            SOMLibMapDescription mapDescription = new SOMLibMapDescription();
            final InputData inputData = csommap.getSharedInputObjects().getInputData();
            try {
                mapDescription.setProperty(SOMLibMapDescription.TYPE, "gcs");
                mapDescription.setProperty(SOMLibMapDescription.GRID_LAYOUT,
                        csommap.getLayer().getGridLayout().toString());
                mapDescription.setProperty(SOMLibMapDescription.GRID_TOPOLOGY,
                        csommap.getLayer().getGridTopology().toString());
                mapDescription.setProperty(SOMLibMapDescription.X_DIM, csommap.getLayer().getXSize());
                mapDescription.setProperty(SOMLibMapDescription.Y_DIM, csommap.getLayer().getYSize());
                mapDescription.setProperty(SOMLibMapDescription.Z_DIM, csommap.getLayer().getZSize());
                mapDescription.setProperty(SOMLibMapDescription.VEC_DIM, csommap.getLayer().getDim());
                mapDescription.setProperty(SOMLibMapDescription.STORAGE_DATE, new SimpleDateFormat().format(new Date()));
                mapDescription.setProperty(SOMLibMapDescription.TRAINING_TIME, ""); // TODO
                mapDescription.setProperty(SOMLibMapDescription.LEARNRATE_TYPE, "exponential");// TODO: detailed formula
                mapDescription.setProperty(SOMLibMapDescription.LEARNRATE_INIT, somProps.learnrate());
                mapDescription.setProperty(SOMLibMapDescription.NEIGHBOURHOOD_TYPE, "exponential");// TODO: detailed
                // formula
                mapDescription.setProperty(SOMLibMapDescription.NEIGHBOURHOOD_INIT, somProps.sigma());
                mapDescription.setProperty(SOMLibMapDescription.RANDOM_INIT, somProps.randomSeed());
                mapDescription.setProperty(SOMLibMapDescription.TOTAL_ITERATIONS, somProps.numIterations());
                mapDescription.setProperty(SOMLibMapDescription.TOTAL_TRAINING_VECTORS, inputData.numVectors());
                mapDescription.setProperty(SOMLibMapDescription.VECTORS_NORMALISED,
                        inputData.isNormalizedToUnitLength());
                try {
                    // FIXME: more generic file format supporting different measures than mqe
                    String[] growthQM = AbstractQualityMeasure.splitNameAndMethod(somProps.growthQualityMeasureName());
                    final double mapQuality = csommap.getLayer().getQualityMeasure().getMapQuality(growthQM[1]);
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
                mapDescription.setProperty(SOMLibMapDescription.URL_WEIGHT_VECTOR, getWeightVectorFileName(fDir, fName,
                        idString, false));
                mapDescription.setProperty(SOMLibMapDescription.URL_QUANT_ERROR_MAP, "");// TODO
                mapDescription.setProperty(SOMLibMapDescription.URL_MAPPED_INPUT_VECTOR, "");// TODO
                mapDescription.setProperty(SOMLibMapDescription.URL_MAPPED_INPUT_VECTOR_DESCRIPTION, "");// TODO
                mapDescription.setProperty(SOMLibMapDescription.URL_UNIT_DESCRIPTION, getUnitDescriptionFileName(fDir,
                        fName, idString, false));
                mapDescription.setProperty(SOMLibMapDescription.URL_TEMPLATE_VECTOR, fileProps.templateFileName(true));
                mapDescription.setProperty(SOMLibMapDescription.URL_DATA_WINNER_MAPPING, getDataWinnerMappingFileName(
                        fDir, fName, csommap.getLayer().getIdString(), false));
                mapDescription.setProperty(SOMLibMapDescription.URL_CLASS_INFO, ""); // TODO
                mapDescription.setProperty(SOMLibMapDescription.METRIC,
                        csommap.getLayer().getMetric().getClass().getName());
                mapDescription.setProperty(SOMLibMapDescription.LAYER_REVISION, csommap.getLayer().getRevision());
                mapDescription.setProperty(SOMLibMapDescription.DESCRIPTION, "");// TODO

                mapDescription.writeMapDescriptionFile(finalName);
                maps.addAll(csommap.getLayer().getAllSubMaps());
            } catch (SOMToolboxException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

    }

    public static void writeDataWinnerMappingFile(GrowingCellStructures csom, InputData data, int numWinners,
            String fDir, String fName, boolean gzipped) throws IOException {
        BufferedWriter bw = null;
        String finalName = getDataWinnerMappingFileName(fDir, fName, csom.getLayer().getIdString(), gzipped);
        if (gzipped == true) {
            bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(finalName))));
        } else {
            bw = new BufferedWriter(new FileWriter(finalName));
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Saving SOMLib data winner mapping file" + finalName + " (" + new File(finalName).getAbsolutePath()
                        + ")");

        int numVectors = data.numVectors();

        int maxWinners = csom.getLayer().getXSize() * csom.getLayer().getYSize();
        if (numWinners > maxWinners) {
            numWinners = maxWinners;
        }

        // bw.write("$FILE_FORMAT_VERSION 1.1"); // added by lidy, because of
        // changed fileformat (see below)
        bw.write("$FILE_FORMAT_VERSION 1.2"); // added by frank for 3d-som
        // support.
        bw.newLine(); // added by lidy
        bw.write("$NUM_WINNERS " + numWinners);
        bw.newLine();
        bw.write("$NUM_VECTORS " + numVectors);
        bw.newLine();
        UnitDistance[] winners;
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors, "Getting winners for datum ", 10);
        for (int d = 0; d < numVectors; d++) {
            progressWriter.progress(d + 1);
            bw.write(data.getInputDatum(d).getLabel());
            bw.newLine(); // added by lidy in order to support spaces in
            // filenames
            winners = csom.getLayer().getWinnersAndDistances(data.getInputDatum(d), numWinners);
            // adapted to mnemonic (sparse) SOMs
            for (int w = 0; w < numWinners; w++) { // was: gsom.getLayer().getUnitCount()
                bw.write(" " + winners[w].getUnit().getXPos() + " " + winners[w].getUnit().getYPos() + " "
                        + winners[w].getUnit().getZPos() + " " + StringUtils.format(winners[w].getDistance(), 5));
            }
            bw.newLine();
        }

        bw.close();

    }
}
