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

import cern.colt.matrix.DoubleMatrix1D;

import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * A reader for the Marsyas 0.2 ARFF format, which has the label name as a comment in front of the vector file name.
 * 
 * @author Rudolf Mayer
 * @version $Id: MarsyasARFFInputData.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class MarsyasARFFInputData extends ARFFFormatInputData {

    public MarsyasARFFInputData(String arffFileName) {
        super(arffFileName);
    }

    @Override
    protected void readVectorFile(String arffFileName, boolean sparse) {
        super.readVectorFile(arffFileName, sparse);
        // after reading the marsyas file, we parse the instance names
        SOMLibClassInformation newClassInfo = new SOMLibClassInformation();
        try {
            final BufferedReader reader = FileUtils.openFile("ARFF Input Vector File", arffFileName);
            String line;
            do {
                line = reader.readLine();
            } while (line != null && !line.trim().equals("@data"));
            // now we expect blocks of two lines, first a comment with the instance name, then the actual data
            int index = 0;
            while ((line = reader.readLine()) != null) {

                String instanceName = line.replaceFirst("%", "").trim(); // replace the first (and only first!) %

                // write new class
                newClassInfo.addItem(instanceName, classInfo.getClassName(dataNames[index]));

                dataNames[index] = instanceName;

                // do some checking if the data equals the data vector read before..
                String dataString = reader.readLine();
                final String[] splits = dataString.split(",");
                if (splits.length - 1 != dim()) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "Mismatch in WEKA ARFF reader and manual reader, for line '" + instanceName
                                    + "', expected dimensionality " + dim() + " found " + (splits.length - 1) + ".");
                    System.exit(-1);
                }
                final DoubleMatrix1D vector = data.viewRow(index);
                for (int i = 0; i < splits.length - 1; i++) {
                    splits[i] = splits[i].trim();
                    if (Double.parseDouble(splits[i]) != vector.get(i)) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                "Mismatch in WEKA ARFF reader and manual reader, for line '" + instanceName
                                        + "', element " + i + ", expected " + vector.get(i) + ", found " + splits[i]
                                        + ".");
                        System.exit(-1);
                    }
                }
                vector.toArray();

                index++;
            }

            // check if we read all the instances
            if (index != dataNames.length) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Read " + index + " new instance names, expected " + dataNames.length);
            }

            newClassInfo.processItems(false);
            this.classInfo = newClassInfo;

        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ERROR_MESSAGE_FILE_FORMAT_CORRUPT);
            e.printStackTrace();
        }
    }

    public static String getFormatName() {
        return "Marsyas0.2ARFF";
    }

    public static void main(String[] args) {
        new MarsyasARFFInputData("/tmp/collection_600_allmp3s_manual_complete_absPaths.arff");
    }

    @Override
    protected String getClassAttributeName() {
        return "output"; // well, this is the name that Marsyas uses..
    }
}
