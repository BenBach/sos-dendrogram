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
 * Reads input data from a <a href="http://www.cs.waikato.ac.nz/~ml/weka/arff.html">WEKA ARFF File Format</a>.<br>
 * 
 * @author Rudolf Mayer
 * @version $Id: ARFFFormatInputData.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class ARFFFormatInputData extends SOMLibSparseInputData {
    public static final String RELATION = "@RELATION";

    public static final String ATTRIBUTE = "@ATTRIBUTE";

    public static final String DATA = "@DATA";

    public static final String INTEGER_TYPE = "integer";

    public static final String NUMERIC_TYPE = "NUMERIC";

    public static final String STRING_TYPE = "string";

    private static final char NULLCHAR = '\u0000';

    public ARFFFormatInputData(String arffFileName) {
        this(arffFileName, DEFAULT_SPARSE, DEFAULT_NORMALISED, DEFAULT_NUM_CACHE_BLOCKS, DEFAULT_RANDOM_SEED);
    }

    public ARFFFormatInputData(String arffFileName, boolean sparse, boolean norm, int numCacheBlocks, long seed) {
        super(arffFileName, sparse, norm, numCacheBlocks, seed);
    }

    @Override
    protected void readVectorFile(String arffFileName, boolean sparse) {
        classInfo = new SOMLibClassInformation();

        BufferedReader br = openFile(arffFileName);
        try {
            String line = br.readLine();

            // skip lines till first attribute
            ArrayList<String> components = new ArrayList<String>();
            while (line != null && !line.toUpperCase().startsWith(ATTRIBUTE)) {
                line = br.readLine();
            }

            int labelIndex = -1;
            int classIndex = -1;

            // read all attributes
            while (line != null && line.startsWith(ATTRIBUTE.toUpperCase())) {
                String lineData = line.substring(ATTRIBUTE.length()).trim();
                int split = lineData.lastIndexOf(' ');
                String name = lineData.substring(0, split).trim();
                if (name.startsWith("\"")) {
                    name = name.substring(1);
                }
                if (name.endsWith("\"")) {
                    name = name.substring(0, (name.length() - 1));
                }
                String type = lineData.substring(split).trim();

                // numerical attribute => treat as vector element
                if (INTEGER_TYPE.equalsIgnoreCase(type) || NUMERIC_TYPE.equalsIgnoreCase(type)) {
                    components.add(name);
                } else if (type.equalsIgnoreCase(STRING_TYPE)) {
                    // string attribute => treat as instance/label name
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                            "Assuming String-type attribute '" + name + "' as instance name.");
                    labelIndex = components.size();
                } else if (type.contains("{") && type.contains("}")) {
                    // categorical feature => treat as class index
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                            "Assuming categorical attribute '" + name + "' as class index.");
                    classIndex = components.size();
                }

                line = br.readLine();
            }

            int expectedLineComponents = components.size() + (labelIndex > 0 ? 1 : 0) + (classIndex > 0 ? 1 : 0);

            // skip lines up to data marker
            while (line != null && !line.toUpperCase().startsWith(DATA)) {
                line = br.readLine();
            }
            // skip lines up to data
            while (line != null && (line.toUpperCase().startsWith(DATA) || line.trim().length() == 0)) {
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
            dim = components.size();
            // now that we know numVectors, we can also create the template vector
            templateVector = new SOMLibTemplateVector(numVectors, components.toArray(new String[components.size()]), 2);

            classInfo = new SOMLibClassInformation();

            initDataStructures(sparse);

            int index = 0;
            // read data lines
            for (String lineData : lines) {
                ArrayList<String> values = new ArrayList<String>();

                // parse line
                if (lineData.length() != 0) {
                    char quotCharacter = NULLCHAR;
                    StringBuilder buffer = new StringBuilder("");

                    for (int c = 0; c < lineData.length(); c++) {
                        char ch = lineData.charAt(c);

                        if (quotCharacter != NULLCHAR) {
                            if (ch == quotCharacter) {
                                quotCharacter = NULLCHAR;
                            } else {
                                buffer.append(ch);
                            }
                        } else if (ch == '"' || ch == '\'') {
                            quotCharacter = ch;
                        } else if (ch == ',') {
                            String featureValue = buffer.toString();
                            values.add(featureValue);
                            buffer = new StringBuilder("");
                        } else {
                            buffer.append(ch);
                        }
                    }

                    // check buffer
                    if (buffer.length() != 0 && values.size() < expectedLineComponents) {
                        String featureValue = buffer.toString();
                        values.add(featureValue);
                    }
                }

                // process the line values
                String label;
                if (labelIndex > 0) {
                    label = values.get(labelIndex);
                    values.remove(labelIndex);
                    if (classIndex > labelIndex) {
                        classIndex--;
                    }
                } else {
                    label = String.valueOf(index + 1);
                }
                String className = null;
                if (classIndex > 0) {
                    className = values.get(classIndex);
                    values.remove(classIndex);
                }
                for (int ve = 0; ve < dim; ve++) {
                    setMatrixValue(index, ve, parseDouble(values.get(ve)));
                }
                addInstance(index, label);
                if (className != null) {
                    // if we have a class info
                    classInfo.addItem(label, className);
                }
                index++;
            }

        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ERROR_MESSAGE_FILE_FORMAT_CORRUPT);
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        classInfo.processItems(false);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("ARFF vector file  seems to be correct. Riding on ...");

    }

    protected String getClassAttributeName() {
        return "class";
    }

    public static String getFormatName() {
        return "ARFF";
    }

    public static String getFileNameSuffix() {
        return ".arff";
    }

}
