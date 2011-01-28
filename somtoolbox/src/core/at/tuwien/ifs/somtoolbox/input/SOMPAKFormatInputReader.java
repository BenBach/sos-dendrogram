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

import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * Reads a SOM in SOMPAK file format. For details on this format, please see <a
 * href="http://www.cis.hut.fi/research/som_pak/som_doc.txt">http://www.cis.hut.fi/research/som_pak/som_doc.txt</a>.
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMPAKFormatInputReader.java 3883 2010-11-02 17:13:23Z frank $
 */
public class SOMPAKFormatInputReader extends AbstractSOMInputReader {

    private String[] componentNames;

    private String neighbourhoodFunction;

    private String somPakFilename;

    private String topology;

    public SOMPAKFormatInputReader(String somPakFilename) throws IOException {
        this.somPakFilename = somPakFilename;
        BufferedReader br = FileUtils.openFile("SOMPAK File", somPakFilename);
        String line = null;
        String headerLine = br.readLine();
        String[] elements = headerLine.split(" ");
        dim = Integer.parseInt(elements[0]);
        // FIXME: set topology in the gridTopology field!
        topology = elements[1];
        xSize = Integer.parseInt(elements[2]);
        ySize = Integer.parseInt(elements[3]);
        zSize = 1; // Currently only 2dim maps.
        neighbourhoodFunction = elements[4];
        String componentNamesLine = br.readLine();
        elements = componentNamesLine.split(" ");
        componentNames = new String[dim];
        System.arraycopy(elements, 1, componentNames, 0, componentNames.length);

        unitInfo = new UnitInformation[xSize][ySize][zSize];
        int index = 0;
        while ((line = br.readLine()) != null) {
            elements = line.split(" ");
            int x = index % xSize;
            int y = index / xSize;
            int z = 0;
            unitInfo[x][y][z] = new UnitInformation(dim);
            for (int i = 0; i < unitInfo[x][y][z].vector.length; i++) {
                unitInfo[x][y][z].vector[i] = Double.parseDouble(elements[i]);
            }
            index++;
        }
    }

    public String[] getComponentNames() {
        return componentNames;
    }

    @Override
    public String getFilePath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMapDescriptionFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getNeighbourhoodFunction() {
        return neighbourhoodFunction;
    }

    public String getTopology() {
        return topology;
    }

    @Override
    public String getUnitDescriptionFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getWeightVectorFileName() {
        return somPakFilename;
    }

    public static String getFormatName() {
        return "SOMPak";
    }
}
