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
package at.tuwien.ifs.somtoolbox.layers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibMapDescription;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: AdaptiveCoordinatesVirtualLayer.java 3869 2010-10-21 15:56:09Z mayer $
 */
public class AdaptiveCoordinatesVirtualLayer {

    public static final String FILE_EXTENSION = "adaptiveCoord";

    private HashMap<Double, AdaptiveCoordinatesVirtualUnit[][]> virtualUnits = new HashMap<Double, AdaptiveCoordinatesVirtualUnit[][]>();

    private HashMap<Double, Integer> startedVirtualAdaptionIn = new HashMap<Double, Integer>();

    private double[] thresholds;

    private int xSize;

    private int ySize;

    public AdaptiveCoordinatesVirtualLayer(int ySize, int xSize, double... acThreshold) {
        this.thresholds = acThreshold;
        this.xSize = xSize;
        this.ySize = ySize;
        for (double element : acThreshold) {
            AdaptiveCoordinatesVirtualUnit[][] virtualUnitsTemp = new AdaptiveCoordinatesVirtualUnit[xSize][ySize];
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    virtualUnitsTemp[i][j] = new AdaptiveCoordinatesVirtualUnit(i, j);
                }
            }
            virtualUnits.put(element, virtualUnitsTemp);
        }
    }

    public AdaptiveCoordinatesVirtualLayer(String fileName) throws IOException, SOMToolboxException {
        String fileType = "Adaptive Coordinates file";
        BufferedReader br = FileUtils.openFile(fileType, fileName);
        Map<String, String> headers = FileUtils.readSOMLibFileHeaders(br, fileType);

        String line = headers.get("FIRST_CONTENT_LINE");
        int index = Integer.parseInt(headers.get("LINE_NUMBER"));// line counter
        if (index < 2) {
            throw new SOMToolboxException(fileType + ": no header line starting with $ found");
        }

        xSize = Integer.parseInt(headers.get("$XDIM"));
        ySize = Integer.parseInt(headers.get("$XDIM"));
        int nrThresholds = Integer.parseInt(headers.get("$ZDIM"));
        thresholds = StringUtils.parseDoubles(headers.get(SOMLibMapDescription.ADAPTIVE_COORDINATES_THRESHOLD));

        if (nrThresholds != thresholds.length) {
            throw new SOMLibFileFormatException("$ZDIM of " + nrThresholds + " and actually found " + thresholds.length
                    + " thresholds do not match! Error in line " + index);
        }

        for (double threshold : thresholds) {
            virtualUnits.put(threshold, new AdaptiveCoordinatesVirtualUnit[xSize][ySize]);
        }

        while (line != null) {
            String[] parts = line.split(" ");
            if (parts.length < 4) { // need at least 4 values (x & y, and adaptive coordinates x & y)
                throw new SOMLibFileFormatException("Number of elements per line must be at least 4! Error in line "
                        + index);
            }
            if (parts.length % 2 != 0) { // uneven numbers must not happen
                throw new SOMLibFileFormatException("Number of elements per line must be multiple of 2! Error in line "
                        + index);
            }
            int xPos = Integer.parseInt(parts[0]);
            int yPos = Integer.parseInt(parts[1]);
            for (int i = 2; i < parts.length; i += 2) {
                double acXPos = Double.parseDouble(parts[i]);
                double acYPos = Double.parseDouble(parts[i + 1]);
                double threshold = thresholds[(i - 2) / 2];
                virtualUnits.get(threshold)[xPos][yPos] = new AdaptiveCoordinatesVirtualUnit(acXPos, acYPos);
            }

            line = br.readLine();
        }

        // check whether we have found all units in the file
        for (double threshold : thresholds) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (virtualUnits.get(threshold)[i][j] == null) {
                        throw new SOMLibFileFormatException("The " + fileType
                                + "didn't contain any information about unit " + i + "/" + j + " for threshold "
                                + threshold + ".");
                    }
                }
            }
        }
    }

    /**
     * Updates the virtual space position of all map units with respect to the input datum and the according winner
     * unit. See Adaptive Coordinates approach.
     * 
     * @param winner the winner unit
     * @param input the input datum
     * @param curIteration the current iteration
     */
    public void updateUnitsVirtualSpacePos(Unit[][][] units, DistanceMetric metric, Unit winner, InputDatum input,
            int curIteration) {

        for (double threshold : thresholds) {
            double[] inputVector = input.getVector().toArray();

            if (curIteration > threshold * xSize * ySize) {
                if (startedVirtualAdaptionIn.get(threshold) == null) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                            "Iteration " + curIteration + ": started training virtual layer for threshold " + threshold);
                    startedVirtualAdaptionIn.put(threshold, curIteration);
                }
                AdaptiveCoordinatesVirtualUnit virtualWinner = virtualUnits.get(threshold)[winner.getXPos()][winner.getYPos()];

                // No adaption during the first few training cycles
                int k = 0;
                for (int j = 0; j < ySize; j++) {
                    for (int i = 0; i < xSize; i++) {
                        try {
                            double unitDistanceToInputAfterAdaption = metric.distance(units[i][j][k].getWeightVector(),
                                    inputVector);
                            virtualUnits.get(threshold)[i][j].updateAdaptiveCoordinates(
                                    unitDistanceToInputAfterAdaption, virtualWinner.getAXPos(),
                                    virtualWinner.getAYPos());
                        } catch (MetricException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    public AdaptiveCoordinatesVirtualUnit getVirtualUnit(double threshold, int x, int y) {
        return virtualUnits.get(threshold)[x][y];
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public void writeToFile(String fDir, String fName) throws IOException {
        boolean gzipped = true;
        BufferedWriter bw = null;
        String finalName = FileUtils.getPathPrefix(fDir) + fName + FileUtils.getSuffix(FILE_EXTENSION, gzipped);

        if (gzipped == true) {
            bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(finalName))));
        } else {
            bw = new BufferedWriter(new FileWriter(finalName));
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Saving SOMLib adaptive coordinates file " + finalName + " (" + new File(finalName).getAbsolutePath()
                        + ")");

        bw.write("$TYPE ADAPTIVE_COORDINATES");
        bw.newLine();
        bw.write("$FILE_FORMAT_VERSION 1.0");
        bw.newLine();
        bw.write("$XDIM " + getXSize());
        bw.newLine();
        bw.write("$YDIM " + getYSize());
        bw.newLine();
        bw.write("$ZDIM " + thresholds.length); // the number of thresholds
        bw.newLine();
        bw.write(SOMLibMapDescription.ADAPTIVE_COORDINATES_THRESHOLD + " " + StringUtils.toString(thresholds, "", ""));
        bw.newLine();

        for (int j = 0; j < getYSize(); j++) {
            for (int i = 0; i < getXSize(); i++) {
                bw.write(i + " " + j);
                for (double threshold : thresholds) {
                    bw.write(" " + virtualUnits.get(threshold)[i][j].getAXPos() + " "
                            + virtualUnits.get(threshold)[i][j].getAYPos());
                }
                bw.write("\n");
            }
        }
        bw.flush();
        bw.close();
    }

    protected void printAdaptiveCoordinates() {
        for (double threshold : thresholds) {
            System.out.println("\n=================== Threshold " + threshold);
            for (int j = 0; j < getYSize(); j++) {
                for (int i = 0; i < getXSize(); i++) {
                    AdaptiveCoordinatesVirtualUnit acu = virtualUnits.get(threshold)[i][j];
                    System.out.print(StringUtils.format(acu.getAXPos(), 5, true) + "/"
                            + StringUtils.format(acu.getAYPos(), 5, true) + "\t");
                }
                System.out.println();
            }
        }
    }

    public void setDistanceToWinner(int x, int y, double distanceToWinners) {
        for (double threshold : thresholds) {
            getVirtualUnit(threshold, x, y).setDistanceToWinner(distanceToWinners);
        }
    }

    /** @return Returns the adaptive coordinates thresholds used for this set of virtual layers. */
    public double[] getThresholds() {
        return thresholds;
    }
}
