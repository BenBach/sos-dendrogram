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
import java.util.Arrays;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;

/**
 * This class converts between various file formats for {@link InputData}. Currently supported formats are listed in
 * {@link InputDataFactory#INPUT_FILE_FORMAT_TYPES} and {@link InputDataWriter#OUTPUT_FILE_FORMAT_TYPES} respective.
 * 
 * @author Rudolf Mayer
 * @version $Id: InputDataFileFormatConverter.java 3833 2010-10-06 21:35:47Z mayer $
 */
public class InputDataFileFormatConverter implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] {
            OptionFactory.getOptInputFormat(false, InputDataFactory.INPUT_FILE_FORMAT_TYPES),
            OptionFactory.getOptInputFileName(), OptionFactory.getOptTemplateVectorFile(false),
            OptionFactory.getOptClassInformationFile(false),
            OptionFactory.getOptOutputFormat(false, InputDataWriter.OUTPUT_FILE_FORMAT_TYPES),
            OptionFactory.getOptGZip(false, true), OptionFactory.getOptOutputFileName(true),
            OptionFactory.getSwitchSkipInstanceNames(), OptionFactory.getSwitchSkipInputsWithoutClass(),
            OptionFactory.getSwitchTabSeparatedClassFile() };

    public static String DESCRIPTION = "Converts between various file formats for input data.";

    public static String LONG_DESCRIPTION = "Converts between various file formats for input data. Currently supported formats are "
            + Arrays.toString(InputDataFactory.INPUT_FILE_FORMAT_TYPES)
            + " and "
            + Arrays.toString(InputDataWriter.OUTPUT_FILE_FORMAT_TYPES) + ", respective";

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static void main(String[] args) throws IOException, SOMToolboxException {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String inputFileName = OptionFactory.getFilePath(config, "input");
        String templateVectorFile = OptionFactory.getFilePath(config, "templateVectorFile");
        String classInformationFile = OptionFactory.getFilePath(config, "classInformationFile");
        String fName = OptionFactory.getFilePath(config, "output");
        boolean skipInstanceNames = config.getBoolean("skipInstanceNames");
        boolean skipInputsWithoutClass = config.getBoolean("skipInputsWithoutClass");
        boolean tabSeparatedClassFile = config.getBoolean("tabSeparatedClassFile");

        String inputFormat = config.getString("inputFormat");
        if (inputFormat == null) {
            inputFormat = InputDataFactory.detectInputFormatFromExtension(inputFileName, "input");
        }
        String outputFormat = config.getString("outputFormat");
        if (outputFormat == null) {
            outputFormat = InputDataFactory.detectInputFormatFromExtension(fName, "output");
        }

        InputData data = null;

        data = InputDataFactory.open(inputFormat, inputFileName);
        if (templateVectorFile != null) {
            data.setTemplateVector(new SOMLibTemplateVector(templateVectorFile));
        }
        if (classInformationFile != null) {
            if (inputFormat.equals(ESOMInputData.getFormatName())) {
                data.setClassInfo(new ESOMClassInformation(classInformationFile));
            } else {
                data.setClassInfo(new SOMLibClassInformation(classInformationFile));
            }
        }

        InputDataWriter.write(fName, data, outputFormat, tabSeparatedClassFile, skipInstanceNames,
                skipInputsWithoutClass);

    }

}
