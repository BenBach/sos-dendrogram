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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * Reads a map in the ESOM Format (see http://databionic-esom.sourceforge.net/user.html#File_formats)
 * 
 * @author Rudolf Mayer
 * @version $Id: ESOMFormatInputReader.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class ESOMFormatInputReader extends AbstractSOMInputReader {

    private String weightsFile;

    private String bmFile;

    public ESOMFormatInputReader(String weightsFile, String bmFile) throws NumberFormatException, IOException,
            SOMToolboxException {
        // FIXME: set topology in the gridTopology field!

        this.weightsFile = weightsFile;
        this.bmFile = bmFile;

        // read the weights file
        BufferedReader br = FileUtils.openFile("ESOM map file", weightsFile);

        String line = FileUtils.consumeHeaderComments(br);

        // first line: size
        line = line.trim().substring(1).trim();
        ySize = Integer.parseInt(line.split(" ")[0]); // first rows
        xSize = Integer.parseInt(line.split(" ")[1]); // then columns
        zSize = 1;
        // second line: dimensionality
        line = br.readLine();
        dim = Integer.parseInt(line.trim().substring(1).trim());

        // there might be a third header line, which is undocumented, and contains simply a "1" for each dimension...
        line = br.readLine();
        if (line != null && line.trim().startsWith("%")) {
            // we just consume this line..
            line = br.readLine();
        }

        // subsequent lines: weights
        unitInfo = new UnitInformation[xSize][ySize][zSize];
        int index = 0;
        while (line != null) {
            String[] elements = line.split(at.tuwien.ifs.somtoolbox.util.StringUtils.REGEX_SPACE_OR_TAB);
            int x = index % xSize;
            int y = index / xSize;
            int z = 0;
            unitInfo[x][y][z] = new UnitInformation(dim);
            for (int i = 0; i < unitInfo[x][y][z].vector.length; i++) {
                unitInfo[x][y][z].vector[i] = Double.parseDouble(elements[i]);
            }
            index++;
            line = br.readLine();
        }

        // if existing, read the bestmatch file to construct the unit information
        if (StringUtils.isNotBlank(bmFile)) {
            br = FileUtils.openFile("SOMPAK File", bmFile);

            line = FileUtils.consumeHeaderComments(br);

            // first line: map size
            line = line.trim().substring(1).trim();
            int ySizeBM = Integer.parseInt(line.split(" ")[0]); // first rows
            int xSizeBM = Integer.parseInt(line.split(" ")[1]); // then columns
            // check for mismatch
            if (xSizeBM != xSize) {
                throw new SOMToolboxException("Header in weights (" + weightsFile + ") and bestmatches (" + bmFile
                        + ") differ in xSize: " + xSize + " <-> " + xSizeBM);
            }
            if (ySizeBM != ySize) {
                throw new SOMToolboxException("Header in weights (" + weightsFile + ") and bestmatches (" + bmFile
                        + ") differ in ySize: " + ySize + " <-> " + ySizeBM);
            }

            // second line: number of lines
            line = br.readLine(); // just consume it, we don't really need it (except if we add some sanity check)

            while ((line = br.readLine()) != null) {
                String[] lineElements = line.trim().split("\t");
                String label = lineElements[0].trim();
                int yPos = Integer.parseInt(lineElements[1].trim());
                int xPos = Integer.parseInt(lineElements[2].trim());
                ArrayUtils.add(unitInfo[xPos][yPos][0].mappedVecs, label);
            }
        }
    }

    @Override
    public String getFilePath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMapDescriptionFileName() {
        return null;
    }

    @Override
    public String getUnitDescriptionFileName() {
        return bmFile;
    }

    @Override
    public String getWeightVectorFileName() {
        return weightsFile;
    }

    public static String getFormatName() {
        return "ESOM";
    }

}
