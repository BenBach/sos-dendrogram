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
package at.tuwien.ifs.somtoolbox.apps.helper;

import java.io.File;
import java.io.IOException;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;

/**
 * This class "cleans" a class info file, i.e. it removes from the class info file instances that are not present in the
 * input vector. This can happen e.g. with text data, where not all input documents are used in the final map due to
 * sparsity reasons.
 * 
 * @author Rudolf Mayer
 * @version $Id: ClassInfoFileCleaner.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class ClassInfoFileCleaner {
    public static void main(String[] args) throws IOException, SOMToolboxException {
        // register and parse all options for the
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptInputVectorFile(true),
                OptionFactory.getOptClassInformationFile(true), OptionFactory.getOptOutputFileName(true),
                OptionFactory.getOptOutputDirectory(false));

        String vectorFileName = config.getString("inputVectorFile");
        String classInfoFile = config.getString("classInformationFile");
        String outputDir = config.getString("outputDirectory", ".");
        String outputFileName = config.getString("output");

        SOMLibSparseInputData inputData = new SOMLibSparseInputData(vectorFileName);
        SOMLibClassInformation classInfo = new SOMLibClassInformation(classInfoFile);
        classInfo.removeNotPresentElements(inputData);
        InputDataWriter.writeAsSOMLib(classInfo, outputDir + File.separator + outputFileName);
    }
}
