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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * A factory class that knows how to build a {@link InputData} object from a given file name
 * 
 * @author Rudolf Mayer
 * @version $Id: InputDataFactory.java 3868 2010-10-21 15:52:31Z mayer $
 */
public class InputDataFactory {

    private static final HashMap<String, Class<? extends AbstractSOMLibSparseInputData>> inputClasses = new HashMap<String, Class<? extends AbstractSOMLibSparseInputData>>();

    /** Supported Input File Format Types */
    public static final String[] INPUT_FILE_FORMAT_TYPES;

    private static final HashMap<String, String> knownExtensions = new HashMap<String, String>();

    static {
        knownExtensions.put(SOMLibSparseInputData.getFileNameSuffix(), SOMLibSparseInputData.getFormatName());
        inputClasses.put(SOMLibSparseInputData.getFormatName(), SOMLibSparseInputData.class);

        knownExtensions.put(ARFFFormatInputData.getFileNameSuffix(), ARFFFormatInputData.getFormatName());
        inputClasses.put(ARFFFormatInputData.getFormatName(), ARFFFormatInputData.class);

        knownExtensions.put(RandomAccessFileSOMLibInputData.getFileNameSuffix(),
                RandomAccessFileSOMLibInputData.getFormatName());
        inputClasses.put(RandomAccessFileSOMLibInputData.getFormatName(), RandomAccessFileSOMLibInputData.class);

        knownExtensions.put(SimpleMatrixInputData.getFileNameSuffix(), SimpleMatrixInputData.getFormatName());
        inputClasses.put(SimpleMatrixInputData.getFormatName(), SimpleMatrixInputData.class);

        knownExtensions.put(SOMPAKInputData.getFileNameSuffix(), SOMPAKInputData.getFormatName());
        inputClasses.put(SOMPAKInputData.getFormatName(), SOMPAKInputData.class);

        knownExtensions.put(ESOMInputData.getFileNameSuffix(), ESOMInputData.getFormatName());
        inputClasses.put(ESOMInputData.getFormatName(), ESOMInputData.class);

        inputClasses.put(MarsyasARFFInputData.getFormatName(), MarsyasARFFInputData.class);

        INPUT_FILE_FORMAT_TYPES = inputClasses.keySet().toArray(new String[inputClasses.size()]);
    }

    // FIXME: support more file formats, such as ESOM and SOMPAK
    public static InputData open(String vectorFileName, String templateFileName, boolean sparse, boolean norm,
            int numCacheBlocks, long seed) {

        // try to detect from extension
        try {
            String inputFormat = detectInputFormatFromExtension(vectorFileName, "input");
            if (inputFormat != null) {
                if (!inputFormat.equals(AbstractSOMLibSparseInputData.getFormatName())) {
                    return open(inputFormat, vectorFileName);
                } else {
                    return new SOMLibSparseInputData(vectorFileName, templateFileName, sparse, norm, numCacheBlocks,
                            seed);
                }
            }
        } catch (SOMToolboxException e1) {
            e1.printStackTrace();
        }

        // check if we have a SOMLib Format
        try {
            Map<String, String> headers = FileUtils.readSOMLibFileHeaders(FileUtils.openFile("Input vector file",
                    vectorFileName), "Input vector file");
            System.out.println(headers);
            if (headers.size() > 2) {
                // headers found
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Found " + (headers.size() - 2) + " SOMlib headers, assuming SOMLib format.");
                return new SOMLibSparseInputData(vectorFileName, templateFileName, sparse, norm, numCacheBlocks, seed);
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "No SOMlib headers found, trying simple matrix format.");
                return new SimpleMatrixInputData(vectorFileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new SimpleMatrixInputData(vectorFileName);
        }
    }

    public static InputData open(String inputVectorFileName) {
        return open(inputVectorFileName, null, true, true, 1, 7);
    }

    public static InputData open(String formatName, String inputFileName) throws SOMToolboxException {
        try {
            Class<? extends AbstractSOMLibSparseInputData> c = inputClasses.get(formatName);
            if (c == null) {
                throw new SOMToolboxException("Unknown Format: '" + formatName + "', possible formats are: "
                        + inputClasses.keySet());
            }
            Constructor<? extends AbstractSOMLibSparseInputData> constr = c.getConstructor(String.class);
            return constr.newInstance(inputFileName);
        } catch (SOMToolboxException e) {
            throw e; // just throw it on
        } catch (Exception e) {
            throw new SOMToolboxException("Could not instanciate reader for '" + formatName + "': " + e.getMessage());
        }
    }

    public static String detectInputFormatFromExtension(String inputFileName, String type) throws SOMToolboxException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "No " + type + " format specified, detecting from file extension...");
        if (inputFileName.endsWith(".gz")) {
            inputFileName = inputFileName.substring(0, inputFileName.length() - 3);
        }
        for (String extension : knownExtensions.keySet()) {
            if (inputFileName.endsWith(extension)) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "... found '" + extension + "' extension, assuming '" + knownExtensions.get(extension));
                return knownExtensions.get(extension);
            }
        }
        throw new SOMToolboxException("Unknown " + type + " format for file '" + inputFileName
                + "', please specify the " + type + " format via the option.");
    }

}
