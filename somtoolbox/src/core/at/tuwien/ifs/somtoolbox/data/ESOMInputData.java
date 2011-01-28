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
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Reads a input data file in the ESOM format. For details on the file-format specification, see
 * http://databionic-esom.sourceforge.net/user.html#File_formats.
 * 
 * @author Rudolf Mayer
 * @version $Id: ESOMInputData.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class ESOMInputData extends SOMLibSparseInputData {

    public ESOMInputData(String vectorFileName) {
        super(vectorFileName);
    }

    @Override
    protected void readVectorFile(String vectorFileName, boolean sparse) {
        try {
            BufferedReader br = FileUtils.openFile("ESOM input data file", vectorFileName);
            // ignore comment lines
            String line = FileUtils.consumeHeaderComments(br);

            // first line: numVectors
            numVectors = Integer.parseInt(line.trim().substring(1).trim());
            // second line: dimensionality. Also includes the index/label field, thus we store the value -1
            dim = Integer.parseInt(br.readLine().trim().substring(1).trim()) - 1;

            initDataStructures(false);

            // third line - column types
            // TODO: process it
            line = br.readLine();

            // fourth line - component names => construct a template vector
            line = br.readLine();
            String[] componentNames = line.split(StringUtils.REGEX_SPACE_OR_TAB);
            templateVector = new SOMLibTemplateVector(numVectors, (String[]) ArrayUtils.remove(componentNames, 0));

            // all the other lines are data
            int index = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                // TODO: add a sanity check for lineElements.length == dim (or dim+1 if we have classes)
                for (int ve = 0; ve < dim; ve++) {
                    setMatrixValue(index, ve, parseDouble(lineElements[ve + 1]));
                }
                addInstance(index, lineElements[0]);
                index++;
            }

        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ERROR_MESSAGE_FILE_FORMAT_CORRUPT);
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("ESOM vector file seems to be correct. Riding on ...");
    }

    public static String getFileNameSuffix() {
        return ".esom";
    }

    public static String getFormatName() {
        return "ESOM";
    }

}
