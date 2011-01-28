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
import java.util.HashMap;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Reads a class information file in the ESOM Format (see http://databionic-esom.sourceforge.net/user.html#File_formats)
 * 
 * @author Rudolf Mayer
 * @version $Id: ESOMClassInformation.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class ESOMClassInformation extends SOMLibClassInformation {

    public ESOMClassInformation(String classInformationFileName) throws SOMToolboxException, IOException {
        super();
        this.classInformationFileName = classInformationFileName;
        readSOMLibClassInformationFile();
    }

    @Override
    protected void readSOMLibClassInformationFile() throws IOException {
        BufferedReader br = FileUtils.openFile("ESOM class File", classInformationFileName);
        String line = FileUtils.consumeHeaderComments(br);

        // first line: numVectors
        numData = Integer.parseInt(line.trim().substring(1).trim());

        // following lines: class names, and class colours
        HashMap<Integer, String> classes = new HashMap<Integer, String>();
        while ((line = br.readLine()) != null && line.trim().startsWith("%")) {
            line = line.trim().substring(1).trim();
            String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
            classes.put(Integer.parseInt(lineElements[0].trim()), lineElements[1].trim());
            // TODO: add reading the colours
        }

        // now process all the elements
        do {
            String[] lineElements = line.trim().split("\t");
            String classIndex = lineElements[1].trim();
            if (classes.size() > 0) {
                addItem(lineElements[0].trim(), classes.get(Integer.parseInt(classIndex)));
            } else {
                addItem(lineElements[0].trim(), classIndex);
            }
        } while ((line = br.readLine()) != null);

        processItems(false);
    }
}
