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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;

import at.tuwien.ifs.somtoolbox.layers.Layer.GridLayout;
import at.tuwien.ifs.somtoolbox.layers.Layer.GridTopology;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Provides the functionality to read a saved network model in SOMLib format.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: SOMLibFormatInputReader.java 3889 2010-11-03 12:45:46Z frank $
 */
public class SOMLibFormatInputReader extends AbstractSOMInputReader {

    /** The suffix of the map description files */
    public static final String mapFileNameSuffix = ".map";

    /** The suffix of the weight vector files */
    public static final String weightFileNameSuffix = ".wgt";

    /** The suffix of the unit description files */
    public static final String unitFileNameSuffix = ".unit";

    private String mapDescriptionFileName = null;

    private String unitDescriptionFileName = null;

    private String weightVectorFileName = null;

    /**
     * Sole constructor taking the name of the weight vector file, the unit description file and the map description
     * file as arguments.
     * 
     * @param weightVectorFileName the name of the weight vector file.
     * @param unitDescriptionFileName the name of the unit description file.
     * @param mapDescriptionFileName the name of the map description file.
     * @throws FileNotFoundException if one of the files with the given name is not found.
     * @throws SOMLibFileFormatException if the format of one of the file is corrupt.
     */
    public SOMLibFormatInputReader(String weightVectorFileName, String unitDescriptionFileName,
            String mapDescriptionFileName) throws FileNotFoundException, SOMLibFileFormatException {
        // test if we can read all files, to prevent waiting a few minutes on reading the weight-vector file, if then
        // the unit file can't be read
        if (weightVectorFileName != null) {
            FileUtils.openFile("Weight vector file", weightVectorFileName);
        }
        if (unitDescriptionFileName != null) {
            FileUtils.openFile("Unit description file", unitDescriptionFileName);
        }
        if (org.apache.commons.lang.StringUtils.isNotEmpty(mapDescriptionFileName)) {
            FileUtils.openFile("Map description file", mapDescriptionFileName);
        }

        // now do the actual reading
        if (weightVectorFileName != null) {
            readWeightVectorFile(weightVectorFileName);
        }
        if (unitDescriptionFileName != null) {
            readUnitDescriptionFile(unitDescriptionFileName);
        }
        if (org.apache.commons.lang.StringUtils.isNotEmpty(mapDescriptionFileName)) {
            readMapDescriptionFile(mapDescriptionFileName);
        }
        this.unitDescriptionFileName = unitDescriptionFileName;
        this.weightVectorFileName = weightVectorFileName;
        this.mapDescriptionFileName = mapDescriptionFileName;
    }

    /** @see at.tuwien.ifs.somtoolbox.input.SOMInputReader#getFilePath() */
    @Override
    public String getFilePath() {
        String res = "";
        if (weightVectorFileName != null) {
            res = weightVectorFileName.substring(0,
                    weightVectorFileName.lastIndexOf(System.getProperty("file.separator")) + 1);
        } else if (unitDescriptionFileName != null) {
            res = unitDescriptionFileName.substring(0,
                    unitDescriptionFileName.lastIndexOf(System.getProperty("file.separator")) + 1);
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "You should not have come here. Something went awfully wrong. Aborting.");
        }
        return res;
    }

    @Override
    public String getMapDescriptionFileName() {
        return mapDescriptionFileName;
    }

    @Override
    public String getUnitDescriptionFileName() {
        return unitDescriptionFileName;
    }

    @Override
    public String getWeightVectorFileName() {
        return weightVectorFileName;
    }

    /**
     * Reads a SOMLib map description file with the given name.
     * 
     * @param fileName the name of the file to open.
     */
    protected void readMapDescriptionFile(String fileName) throws FileNotFoundException, SOMLibFileFormatException {
        BufferedReader br = FileUtils.openFile("Map description file", fileName);
        String line = null;
        boolean foundZSize = false;
        try {
            while ((line = br.readLine()) != null) {

                if (line.startsWith("$TYPE")) {
                    // ignore
                } else if (line.startsWith(SOMLibMapDescription.GRID_LAYOUT)) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    gridLayout = GridLayout.valueOf(lineElements[1].trim());
                } else if (line.startsWith(SOMLibMapDescription.GRID_TOPOLOGY)) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    gridTopology = GridTopology.valueOf(lineElements[1].trim());
                } else if (line.startsWith("$XDIM")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        int x = Integer.parseInt(lineElements[1]);
                        if (xSize > 0 && x != xSize) {
                            throw new SOMLibFileFormatException(
                                    "Map descripton file corrupt. xSize of map is different than given in the unit description or weight vector file.");
                        } else {
                            xSize = x;
                        }
                    } else {
                        throw new SOMLibFileFormatException(
                                "Map description file format corrupt: no x-dimension specified in line '" + line + "'.");
                    }
                } else if (line.startsWith("$YDIM")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        int y = Integer.parseInt(lineElements[1]);
                        if (ySize > 0 && y != ySize) {
                            throw new SOMLibFileFormatException(
                                    "Map description file corrupt. ySize of map is different than given in the unit description or weight vector file.");
                        } else {
                            ySize = y;
                        }
                    } else {
                        throw new SOMLibFileFormatException(
                                "Map description file format corrupt: no y-dimension specified in line '" + line + "'.");
                    }
                } else if (line.startsWith("$ZDIM")) {
                    foundZSize = true;
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        int z = Integer.parseInt(lineElements[1]);
                        if (zSize > 0 && z != zSize) {
                            throw new SOMLibFileFormatException(
                                    "Map description file corrupt. zSize of map is different than given in the unit description or weight vector file.");
                        } else {
                            zSize = z;
                        }
                    } else {
                        throw new SOMLibFileFormatException(
                                "Map description file format corrupt: no z-dimension specified in line '" + line + "'.");
                    }
                } else if (line.startsWith("$VEC_DIM") || line.startsWith("$VECDIM")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        dim = Integer.parseInt(lineElements[1]);
                    } else {
                        throw new SOMLibFileFormatException(
                                "Map description file format corrupt: no vector dimension specified in line '" + line
                                        + "'.");
                    }
                } else if (line.startsWith("$METRIC")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        metricName = lineElements[1];
                    } else {
                        throw new SOMLibFileFormatException(
                                "Map description file format corrupt: no metric specified in line '" + line + "'.");
                    }
                } // else if ... for other entries
            }

            // If no $ZDIM was found default to 1
            if (!foundZSize && zSize <= 0) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("No $ZDIM found. Setting zSize to default 1");
                zSize = 1;
            }
        } catch (IOException e) {
            throw new SOMLibFileFormatException("Could not read from map description file.");
        }
    }

    /**
     * Reads a SOMLib unit description file with the given name.
     * 
     * @param fileName the name of the file to open.
     */
    protected void readUnitDescriptionFile(String fileName) throws FileNotFoundException, SOMLibFileFormatException {
        BufferedReader br = FileUtils.openFile("Unit description file", fileName);
        double fileFormatVersion = 1.0;
        String line = null;
        int ySizeRead = -1;

        /* read header */
        boolean foundZSize = false;
        try {
            while (!(line = br.readLine()).startsWith("$POS")) {
                // line = br.readLine();
                if (line.startsWith("$TYPE")) {
                    // ignore
                } else if (line.startsWith(SOMLibMapDescription.GRID_LAYOUT)) {
                    // FIXME: check if there's no clash with previously read weight file
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    gridLayout = GridLayout.valueOf(lineElements[1].trim());
                } else if (line.startsWith(SOMLibMapDescription.GRID_TOPOLOGY)) {
                    // FIXME: check if there's no clash with previously read weight file
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    gridTopology = GridTopology.valueOf(lineElements[1].trim());
                } else if (line.startsWith("$FILE_FORMAT_VERSION")) {
                    try {
                        fileFormatVersion = Double.parseDouble(line.trim().split(StringUtils.REGEX_SPACE_OR_TAB, 2)[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new SOMLibFileFormatException(
                                "Unit description file corrupt. Unknown FILE_FORMAT_VERSION");
                    }
                } else if (line.startsWith("$XDIM")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        int x = Integer.parseInt(lineElements[1]);
                        if (xSize > 0 && x != xSize) {
                            throw new SOMLibFileFormatException(
                                    "Unit description file corrupt. xSize of map is different than given in the weight vector file.");
                        } else {
                            xSize = x;
                        }
                    } else {
                        throw new SOMLibFileFormatException("Unit description file format corrupt.");
                    }
                } else if (line.startsWith("$YDIM")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        ySizeRead = Integer.parseInt(lineElements[1]);
                        if (ySize > 0 && ySizeRead != ySize) {
                            throw new SOMLibFileFormatException(
                                    "Unit description file format corrupt. ySize of map is different than given in the weight vector file.");
                        } else {
                            ySize = ySizeRead;
                        }
                    } else {
                        throw new SOMLibFileFormatException("Unit description file format corrupt.");
                    }
                } else if (line.startsWith("$ZDIM")) {
                    foundZSize = true;
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        int zSizeRead = Integer.parseInt(lineElements[1]);
                        if (zSize > 0 && zSizeRead != zSize) {
                            throw new SOMLibFileFormatException(
                                    "Unit description file format corrupt. zSize of map is different than given in the weight vector file.");
                        } else {
                            zSize = zSizeRead;
                        }
                    } else {
                        throw new SOMLibFileFormatException("Unit description file format corrupt.");
                    }

                }
            } // end header
        } catch (NullPointerException e) {
            throw new SOMLibFileFormatException("Unit description file format corrupt: Incomplete header!");
        } catch (IOException e) {
            throw new SOMLibFileFormatException("Could not read from unit description file.");
        }

        if (!foundZSize && zSize <= 0) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("No $ZDIM found. Setting zSize to default 1");
            zSize = 1;
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Unit description file version " + fileFormatVersion);

        if (unitInfo == null) {
            unitInfo = new UnitInformation[xSize][ySize][zSize];
        }
        try {
            StdErrProgressWriter progressWriter = new StdErrProgressWriter(xSize * ySize * zSize,
                    "Reading info for unit ", 10);
            // line = br.readLine();
            while (line != null) {
                if (line.startsWith("$POS_X")) { // new unit
                    int x = 0;
                    int y = 0;
                    int z = 0;
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        x = Integer.parseInt(lineElements[1]);
                    } else {
                        throw new SOMLibFileFormatException("Unit description file format corrupt.");
                    }
                    line = br.readLine();
                    if (line.startsWith("$POS_Y")) {
                        lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                        if (lineElements.length > 1) {
                            y = Integer.parseInt(lineElements[1]);
                        } else {
                            throw new SOMLibFileFormatException(
                                    "Unit description file format corrupt, no $POS_Y specified in '" + line + "'.");
                        }
                    } else {
                        throw new SOMLibFileFormatException(
                                "Unit description file format corrupt. $POS_Y must follow immediately after $POS_X.");
                    }
                    line = br.readLine();
                    if (line.startsWith("$POS_Z")) {
                        lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                        if (lineElements.length > 1) {
                            z = Integer.parseInt(lineElements[1]);
                        } else {
                            throw new SOMLibFileFormatException("Unit description file format corrupt.");
                        }
                    }
                    if (unitInfo[x][y][z] == null) { // adapted for menmonic (sparse) SOMs
                        unitInfo[x][y][z] = new UnitInformation();
                    }
                    unitInfo[x][y][z].posX = x;
                    unitInfo[x][y][z].posY = y;
                    unitInfo[x][y][z].posZ = z;

                    progressWriter.progress();
                    // read info for unit
                    while (line != null && !line.startsWith("$POS_X")) {
                        if (line.startsWith("$UNIT_ID")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            unitInfo[x][y][z].unitId = lineElements[1];
                        } else if (line.startsWith("$QUANTERROR_UNIT")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].quantErrorUnit = Double.parseDouble(lineElements[1]);
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$QUANTERROR_UNIT_AVG")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].quantErrorUnitAvg = Double.parseDouble(lineElements[1]);
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$NR_VEC_MAPPED")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].nrVecMapped = Integer.parseInt(lineElements[1]);
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$MAPPED_VECS_DIST")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].mappedVecsDist = new double[unitInfo[x][y][z].nrVecMapped];
                                for (int i = 0; i < unitInfo[x][y][z].nrVecMapped; i++) {
                                    unitInfo[x][y][z].mappedVecsDist[i] = Double.parseDouble(lineElements[i + 1]);
                                }
                            } else if (unitInfo[x][y][z].nrVecMapped == 0) {
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$MAPPED_VECS")) {
                            if (fileFormatVersion >= 1.1) {
                                // new file format with vector names line by line
                                if (unitInfo[x][y][z].nrVecMapped > 0) {
                                    unitInfo[x][y][z].mappedVecs = new String[unitInfo[x][y][z].nrVecMapped];
                                    for (int i = 0; i < unitInfo[x][y][z].nrVecMapped; i++) {
                                        unitInfo[x][y][z].mappedVecs[i] = br.readLine();
                                    }
                                    CollectionUtils.addAll(allVectorNames, unitInfo[x][y][z].mappedVecs);
                                }
                            } else {
                                // original file format with spaces separated vector names one 1 line
                                lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                                if (lineElements.length > 1) {
                                    unitInfo[x][y][z].mappedVecs = new String[unitInfo[x][y][z].nrVecMapped];
                                    for (int i = 0; i < unitInfo[x][y][z].nrVecMapped; i++) {
                                        unitInfo[x][y][z].mappedVecs[i] = lineElements[i + 1];
                                    }
                                    CollectionUtils.addAll(allVectorNames, unitInfo[x][y][z].mappedVecs);
                                } else if (unitInfo[x][y][z].nrVecMapped == 0) {
                                    // do nothing
                                } else {
                                    throw new SOMLibFileFormatException("Unit description file format corrupt.");
                                }
                            }
                        } else if (line.startsWith("$NR_UNIT_LABELS")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].nrUnitLabels = Integer.parseInt(lineElements[1]);
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$UNIT_LABELS_QE")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].unitLabelsQe = new double[unitInfo[x][y][z].nrUnitLabels];
                                for (int i = 0; i < unitInfo[x][y][z].nrUnitLabels; i++) {
                                    unitInfo[x][y][z].unitLabelsQe[i] = Double.parseDouble(lineElements[i + 1]);
                                }
                            } else if (unitInfo[x][y][z].nrUnitLabels == 0) {
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$UNIT_LABELS_WGT")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].unitLabelsWgt = new double[unitInfo[x][y][z].nrUnitLabels];
                                for (int i = 0; i < unitInfo[x][y][z].nrUnitLabels; i++) {
                                    unitInfo[x][y][z].unitLabelsWgt[i] = Double.parseDouble(lineElements[i + 1]);
                                }
                            } else if (unitInfo[x][y][z].nrUnitLabels == 0) {
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$UNIT_LABELS")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].unitLabels = new String[unitInfo[x][y][z].nrUnitLabels];
                                labelled = true;
                                for (int i = 0; i < unitInfo[x][y][z].nrUnitLabels; i++) {
                                    unitInfo[x][y][z].unitLabels[i] = lineElements[i + 1];
                                }
                            } else if (unitInfo[x][y][z].nrUnitLabels == 0) {
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }

                            // Changes Nataliya for semantic labels
                        } else if (line.startsWith("$KASKI_GATE ")) { // Kaski Gate labels
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            unitInfo[x][y][z].nrKaskiGate = lineElements.length;
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].kaskiGateLabels = new String[lineElements.length];
                                for (int i = 0; i < lineElements.length - 1; i++) {
                                    unitInfo[x][y][z].kaskiGateLabels[i] = lineElements[i + 1];
                                }
                            } else if (unitInfo[x][y][z].nrKaskiGate == 0) { // FIXME: needed?
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$KASKI_WEIGHTS")) { // Kaski Weights
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            unitInfo[x][y][z].nrkaskiweights = lineElements.length - 1;
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].kaskiWeightLabels = new double[unitInfo[x][y][z].nrkaskiweights];
                                for (int i = 0; i < unitInfo[x][y][z].nrkaskiweights; i++) {
                                    unitInfo[x][y][z].kaskiWeightLabels[i] = Double.parseDouble(lineElements[i + 1]);
                                }
                            } else if (unitInfo[x][y][z].nrKaskiGate == 0) { // FIXME: needed?
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$GATE_WEIGHTS")) { // Gate weights
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            unitInfo[x][y][z].nrgateweights = lineElements.length;
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].gateWeightLabels = new double[lineElements.length];
                                for (int i = 0; i < lineElements.length - 1; i++) {
                                    if (lineElements[i + 1].equals("") || lineElements[i + 1].equals(";")) {
                                        unitInfo[x][y][z].gateWeightLabels[i] = 0;
                                    } else {
                                        unitInfo[x][y][z].gateWeightLabels[i] = Double.parseDouble(lineElements[i + 1]);
                                    }
                                }
                            } else if (unitInfo[x][y][z].nrKaskiGate == 0) { // FIXME: needed?
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$BEST_CONTEXT")) { // Best Context
                            lineElements = line.split(";");
                            unitInfo[x][y][z].nrbestcontext = lineElements.length;
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].bestContextLabels = new String[lineElements.length];
                                for (int i = 0; i < lineElements.length - 1; i++) {
                                    unitInfo[x][y][z].bestContextLabels[i] = lineElements[i + 1];
                                }
                            } else if (unitInfo[x][y][z].nrKaskiGate == 0) { // FIXME: needed?
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$KASKI_GATE_CONTEXT")) { // Kaski Gate Context
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            unitInfo[x][y][z].nrContextGate = lineElements.length;
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].contextGateLabels = new String[lineElements.length];
                                for (int i = 0; i < lineElements.length - 1; i++) {
                                    unitInfo[x][y][z].contextGateLabels[i] = lineElements[i + 1];
                                }
                            } else if (unitInfo[x][y][z].nrContextGate == 0) { // FIXME: needed?
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$KASKI")) { // kaski labels
                            lineElements = line.trim().split(StringUtils.REGEX_SPACE_OR_TAB);
                            unitInfo[x][y][z].nrKaski = lineElements.length - 1;
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].kaskiLabels = new String[unitInfo[x][y][z].nrKaski];
                                for (int i = 0; i < unitInfo[x][y][z].nrKaski; i++) {
                                    unitInfo[x][y][z].kaskiLabels[i] = lineElements[i + 1];
                                }
                            } else if (unitInfo[x][y][z].nrKaski == 0) { // FIXME: needed?
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");

                            }
                            // Changes Nataliya END

                        } else if (line.startsWith("$NR_SOMS_MAPPED")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].nrSomsMapped = Integer.parseInt(lineElements[1]);
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        } else if (line.startsWith("$URL_MAPPED_SOMS")) {
                            lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                            if (lineElements.length > 1) {
                                unitInfo[x][y][z].urlMappedSoms = new String[unitInfo[x][y][z].nrSomsMapped];
                                for (int i = 0; i < unitInfo[x][y][z].nrSomsMapped; i++) {
                                    unitInfo[x][y][z].urlMappedSoms[i] = lineElements[i + 1];
                                }
                            } else if (unitInfo[x][y][z].nrSomsMapped == 0) {
                                // do nothing
                            } else {
                                throw new SOMLibFileFormatException("Unit description file format corrupt.");
                            }
                        }
                        line = br.readLine();
                    }

                    // sanity check for size stuff
                    // TODO: also add checks for other (non-compulsory) data than $MAPPED_VECS and $MAPPED_VECS_DIST
                    int nrVecMapped = unitInfo[x][y][z].nrVecMapped;
                    String[] mappedVecs = unitInfo[x][y][z].mappedVecs;
                    double[] mappedVecsDist = unitInfo[x][y][z].mappedVecsDist;
                    String unitID = x + "/" + y + "/" + z;
                    int mappedVecsLength = mappedVecs == null ? -1 : mappedVecs.length;
                    int mappedVecsDistLength = mappedVecsDist == null ? -1 : mappedVecsDist.length;
                    if (nrVecMapped > 0 && mappedVecsLength != nrVecMapped) {
                        throw new SOMLibFileFormatException("Number of items in $MAPPED_VECS (" + mappedVecsLength
                                + ") doesn't equal $NR_VEC_MAPPED (" + nrVecMapped + ") in " + unitID);
                    }
                    if (nrVecMapped > 0 && mappedVecsDistLength != nrVecMapped) {
                        throw new SOMLibFileFormatException("Number of items in $MAPPED_VECS_DIST ("
                                + mappedVecsDistLength + ") doesn't equal $NR_VEC_MAPPED (" + nrVecMapped + ") in "
                                + unitID);
                    }

                }
            }
            br.close();
        } catch (IOException e) {
            throw new SOMLibFileFormatException("Could not read from unit description file.");
        } catch (NumberFormatException e) {
            throw new SOMLibFileFormatException("Number format corrupt in unit description file.");
        }
    }

    /**
     * Reads a SOMLib weight vector file with the given name.
     * 
     * @param fileName the name of the file to open.
     */
    private void readWeightVectorFile(String fileName) throws FileNotFoundException, SOMLibFileFormatException {
        BufferedReader br = FileUtils.openFile("Weight vector file", fileName);
        String line = null;
        boolean foundZSize = false;
        try {
            while ((line = br.readLine()) != null) {
                // Skip blank lines
                if (line.trim().equals("")) {
                    continue;
                }
                // Skip comments
                if (line.trim().startsWith("#")) {
                    continue;
                }
                // End when the first non-header-line appears
                if (!line.startsWith("$")) {
                    break;
                }

                if (line.startsWith("$TYPE")) {
                    // ignore
                } else if (line.startsWith(SOMLibMapDescription.GRID_LAYOUT)) {
                    // FIXME: check if there's no clash with previously read weight or unit files
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    gridLayout = GridLayout.valueOf(lineElements[1].trim());
                } else if (line.startsWith(SOMLibMapDescription.GRID_TOPOLOGY)) {
                    // FIXME: check if there's no clash with previously read weight or unit files
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    gridTopology = GridTopology.valueOf(lineElements[1].trim());
                } else if (line.startsWith("$XDIM")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        int x = Integer.parseInt(lineElements[1]);
                        if (xSize > 0 && x != xSize) {
                            throw new SOMLibFileFormatException(
                                    "Weight vector file corrupt. xSize of map is different than given in the unit description file.");
                        } else {
                            xSize = x;
                        }
                    } else {
                        throw new SOMLibFileFormatException("Weight vector file format corrupt.");
                    }
                } else if (line.startsWith("$YDIM")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        int y = Integer.parseInt(lineElements[1]);
                        if (ySize > 0 && y != ySize) {
                            throw new SOMLibFileFormatException(
                                    "Weight vector file corrupt. ySize of map is different than given in the unit description file.");
                        } else {
                            ySize = y;
                        }
                    } else {
                        throw new SOMLibFileFormatException("Weight vector file format corrupt.");
                    }
                } else if (line.startsWith("$ZDIM")) {
                    foundZSize = true;
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        int z = Integer.parseInt(lineElements[1]);
                        if (zSize > 0 && z != zSize) {
                            throw new SOMLibFileFormatException(
                                    "Weight vector file corrupt. zSize of map is different than given in the unit description file.");
                        } else {
                            zSize = z;
                        }
                    } else {
                        throw new SOMLibFileFormatException("Weight vector file format corrupt.");
                    }
                } else if (line.startsWith("$VEC_DIM") || line.startsWith("$VECDIM")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        dim = Integer.parseInt(lineElements[1]);
                    } else {
                        throw new SOMLibFileFormatException("Weight vector file format corrupt.");
                    }
                }
            }
        } catch (IOException e) {
            throw new SOMLibFileFormatException("Could not read from weight vector file.");
        }

        if (!foundZSize && zSize <= 0) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("No $ZDIM found. Setting zSize to default 1");
            zSize = 1;
        }

        boolean first = false;
        if (unitInfo == null) {
            unitInfo = new UnitInformation[xSize][ySize][zSize];
            first = true;
        }
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (first == true) {
                        unitInfo[i][j][k] = new UnitInformation();
                    }
                    unitInfo[i][j][k].vector = new double[dim];
                }
            }
        }
        try {
            int numUnits = xSize * ySize * zSize;
            StdErrProgressWriter progressWriter = new StdErrProgressWriter(numUnits, "Reading weight vector of unit ",
                    10);
            for (int k = 0; k < zSize; k++) {
                for (int j = 0; j < ySize; j++) {
                    for (int i = 0; i < xSize; i++) {
                        if (line == null) {
                            throw new SOMLibFileFormatException("Error reading weightv vector file");
                        }
                        processUnitElement(line, k, j, i); // System.out.println();
                        line = br.readLine();
                        progressWriter.progress();// "Reading weight vector of unit " + i + "/" + j + "/" + k + ", ",
                        // ((j * xSize) + i + 1));
                    }
                }
            }
            // if ((xSize!=props.xSize()) || (ySize!=props.ySize())) {
            // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Map size given in properties file differs from actual
            // map size. Ignoring values in
            // properties file.");
            // }
            br.close();
        } catch (IOException e) {
            throw new SOMLibFileFormatException("Could not read from weight vector file.");
        } catch (NumberFormatException e) {
            throw new SOMLibFileFormatException("Weight vector number format corrupt.");
        }
    }

    protected void processUnitElement(String line, int k, int j, int i) {
        String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
        for (int ve = 0; ve < dim; ve++) {
            unitInfo[i][j][k].vector[ve] = Double.parseDouble(lineElements[ve]);
        }
        // String label = lineElements[dim]; // dummy
    }

    public static String getFormatName() {
        return "SOMLib";
    }

}
