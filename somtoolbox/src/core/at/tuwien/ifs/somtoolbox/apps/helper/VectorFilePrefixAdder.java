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

import java.io.IOException;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;

/**
 * @author Rudolf Mayer
 * @version $Id: VectorFilePrefixAdder.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class VectorFilePrefixAdder {

    public static void main(String[] args) throws IOException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_VECTORFILE_PREFIX_ADDER);
        String prefix = config.getString("prefix");

        String fileName = config.getString("inputFile");
        String outputFileName = config.getString("output");
        SOMLibSparseInputData data = new SOMLibSparseInputData(fileName);
        int numVectors = data.numVectors();
        for (int i = 0; i < numVectors; i++) {
            data.setLabel(i, prefix + data.getLabel(i));
        }
        InputDataWriter.writeAsSOMLib(data, outputFileName);
    }

}
