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
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This class reads input vector data from files in the SOMPak format, as provided by the MATLAB SOMToolbox. See
 * http://www.cis.hut.fi/projects/somtoolbox/package/docs2/som_read_data.html for format details.
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMPAKInputData.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class SOMPAKInputData extends SOMLibSparseInputData {
    public static final String INDICATOR_COMPONENTS = "#n";

    public SOMPAKInputData(String vectorFileName) {
        super(vectorFileName);
    }

    @Override
    protected void readVectorFile(String vectorFileName, boolean sparse) {
        BufferedReader br = openFile(vectorFileName);
        try {
            // read the first header line, which gives the dimensionality
            String dimString = br.readLine();
            dim = Integer.parseInt(dimString);

            String[] componentNames = null;

            // the second line might give us the component names, e.g. in the format of
            // #n SepalL SepalW PetalL PetalW
            String line = br.readLine();
            if (line.startsWith(INDICATOR_COMPONENTS)) {
                line = line.substring(INDICATOR_COMPONENTS.length()).trim();
                componentNames = line.split(" ");
                line = br.readLine();
            }

            // we don't have any information on the number of vectors available, thus we need to first read them into a
            // list
            ArrayList<String> lines = new ArrayList<String>();
            while (line != null) {
                if (line.trim().length() > 0) {
                    lines.add(line.trim());
                }
                line = br.readLine();
            }
            // now we can compute the number of vectors, and initialise our data structures accordingly.
            numVectors = lines.size();
            // now that we know numVectors, we can also create the template vector
            templateVector = new SOMLibTemplateVector(numVectors, componentNames);

            classInfo = new SOMLibClassInformation();

            initDataStructures(sparse);

            // finally, process all vectors
            int index = 0;
            for (String s : lines) {
                String[] lineElements = s.split(" ");
                // TODO: add a sanity check for lineElements.length == dim (or dim+1 if we have classes)
                for (int ve = 0; ve < dim; ve++) {
                    setMatrixValue(index, ve, parseDouble(lineElements[ve]));
                }
                addInstance(index, String.valueOf(index + 1));
                // if we have a class info
                if (lineElements.length > dim) {
                    classInfo.addItem(String.valueOf(index + 1), lineElements[dim].trim());
                }
                index++;
            }
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ERROR_MESSAGE_FILE_FORMAT_CORRUPT);
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        classInfo.processItems(false);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("SOMPak vector file  seems to be correct. Riding on ...");
    }

    public static String getFileNameSuffix() {
        return ".sompak";
    }

    public static String getFormatName() {
        return "SOMPak";
    }

}
