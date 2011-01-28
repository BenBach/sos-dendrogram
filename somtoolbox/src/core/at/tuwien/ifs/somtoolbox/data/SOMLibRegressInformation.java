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
package at.tuwien.ifs.somtoolbox.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class provides information about the real output value for the {@link InputData} input vectors.<br>
 * <p>
 * The file format consists of a <code>header</code> and the content as follows:
 * </p>
 * <b>$TYPE</b> string, mandatory. Fixed to <i>output_information.</i> <br>
 * <b>$XDIM</b> integer, mandatory: number of units in x-direction.<br>
 * <b>$YDIM</b> integer, mandatory: dimensionality of the regression file, equals the number of input vectors (
 * {@link InputData#numVectors()}). <br>
 * <b>labelName_n&nbsp;outputValue</b> <br>
 * <p>
 * Alternatively, the file format can be more simple, and not contain any file header. Then, there is only a list of
 * lines with two tabulator-separated <code>Strings</code> in the form of <code>labelName&nbsp;regressionValue</code>.<br>
 * </p>
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMLibRegressInformation.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class SOMLibRegressInformation {
    private static final Logger logger = Logger.getLogger("at.tuwien.ifs.somtoolbox");

    /** The file name to read from. */
    private String regressionInformationFileName = null;

    private LinkedHashMap<String, Double> dataHash = new LinkedHashMap<String, Double>();

    private double maxPrediction;

    private double minPrediction;

    private double meanPrediction;

    /**
     * Creates a new class information object by trying to read the given file in both the versions with a file header (
     * {@link #readSOMLibRegressionInformationFile()}) and the tab separated file (
     * {@link SOMLibClassInformation#readTabSepClassInformationFile()}).
     */
    public SOMLibRegressInformation(String regressionInformationFileName) throws SOMToolboxException {
        this.regressionInformationFileName = regressionInformationFileName;
        try {
            readSOMLibRegressionInformationFile();
        } catch (ClassInfoHeaderNotFoundException e) {
            try {
                logger.info(e.getMessage());
                logger.info("Trying to read tab/space separated regression info file...");
                readTabSepRegressionInformationFile();
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new SOMLibFileFormatException("Problems reading regression information file "
                        + regressionInformationFileName + ": ' " + e.getMessage() + "'. Aborting.");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Regression information file correctly loaded.");
    }

    /** Reads a regression information file containing a header and class indices. */
    private void readSOMLibRegressionInformationFile() throws IOException, SOMToolboxException {
        int columns = 0;
        BufferedReader br = FileUtils.openFile("regression information file", regressionInformationFileName);

        Map<String, String> headers = FileUtils.readSOMLibFileHeaders(br, "regression information");
        String line = headers.get("FIRST_CONTENT_LINE");
        int index = Integer.parseInt(headers.get("LINE_NUMBER"));// line counter
        if (index < 2) {
            throw new ClassInfoHeaderNotFoundException(
                    "Regression information file: no header line starting with $ found");
        }

        columns = Integer.parseInt(headers.get("$XDIM"));
        if (columns < 2) {
            throw new SOMLibFileFormatException(
                    "Regression information file format corrupt. At least 2 columns (name, predictedValue) required. Aborting.");
        }
        int numData = Integer.parseInt(headers.get("$YDIM"));

        // READ REST OF THE FILE
        if (numData == 0) {
            throw new SOMLibFileFormatException(
                    "Regression information file format corrupt. Missing $YDIM value. Aborting.");
        }

        index = 0;

        while (line != null) {
            // TODO if line is no comment line ($)
            index++;
            String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
            if (lineElements.length != columns) {
                throw new SOMLibFileFormatException("Regression information file format corrupt in element number "
                        + index + ", incorrect number of columns: XDIM:   " + columns + ", columns: "
                        + lineElements.length + ". Aborting.");
            } else {
                try {
                    dataHash.put(lineElements[0], Double.parseDouble(lineElements[1]));
                } catch (NumberFormatException e) {
                    throw new SOMLibFileFormatException("Output number format corrupt in element number " + index
                            + ": '" + lineElements[1] + "'. Aborting.");
                }
            }

            line = br.readLine();
        }

        if (index != numData) {
            throw new SOMLibFileFormatException(
                    "Output information file format corrupt. Incorrect number of data items. Aborting.\n"
                            + Integer.toString(index) + " " + Integer.toString(numData));
        }
        br.close();
        computeStats();
    }

    private void readTabSepRegressionInformationFile() throws SOMToolboxException, IOException {
        String line = null;
        int index = 0; // line counter
        BufferedReader br = FileUtils.openFile("Class information file", regressionInformationFileName);

        while ((line = br.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }
            index++;
            String[] lineElements = line.split("\t");

            if (lineElements.length != 2) {
                br.close();
                throw new SOMLibFileFormatException("Number of elements per line must be exactly 2! Error in line "
                        + index);
            }
            try {
                dataHash.put(lineElements[0], Double.parseDouble(lineElements[1]));
            } catch (NumberFormatException e) {
                throw new SOMLibFileFormatException("Output number format corrupt in element number " + index + ": '"
                        + lineElements[1] + "'. Aborting.");
            }
        }
        br.close();
        computeStats();
    }

    public void computeStats() {
        maxPrediction = Double.MIN_VALUE;
        minPrediction = Double.MAX_VALUE;
        for (Double value : dataHash.values()) {
            meanPrediction += value;
            if (value < minPrediction) {
                minPrediction = value;
            }
            if (value > maxPrediction) {
                maxPrediction = value;
            }
        }
        meanPrediction = meanPrediction / dataHash.size();
    }

    public double getPrediction(String vectorname) {
        if (!dataHash.containsKey(vectorname)) {
            logger.warning("Could not find prediction for input '" + vectorname + "'.");
            return 0;
        }
        return dataHash.get(vectorname);
    }

    /** @return Returns the maximum prediction value */
    public double getMaxPrediction() {
        return maxPrediction;
    }

    /** @return Returns the mean prediction value */
    public double getMeanPrediction() {
        return meanPrediction;
    }

    /** @return Returns the minimum prediction value */
    public double getMinPrediction() {
        return minPrediction;
    }

}
