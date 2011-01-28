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
package at.tuwien.ifs.somtoolbox.data.normalisation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: AbstractNormaliser.java 3587 2010-05-21 10:35:33Z mayer $
 */
abstract class AbstractNormaliser extends SOMLibSparseInputData {
    protected BufferedWriter writer;

    public void normalise(String inputFileName, String outputFileName) throws IOException {
        String line = null;
        BufferedReader br = openFile(inputFileName);
        writer = new BufferedWriter(new FileWriter(outputFileName));
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#")) { // ignore comment lines
                Logger.getLogger("at.tuwien.ifs.somtoolbox").finest("Read comment '" + line + "'.");
            } else if (line.startsWith("$")) { // just write the headers to the output file
                writer.write(line);
                writer.newLine();
                if (line.startsWith("$VEC_DIM") || line.startsWith("$VECDIM")) {
                    dim = Integer.parseInt(line.split(StringUtils.REGEX_SPACE_OR_TAB)[1]);
                }
            } else if (line.length() > 0) { // we reached a content line, stop reading
                break;
            }
        }
        writer.write("$NORMALISATION " + getClass().getName());
        writer.newLine();
        preReading();
        super.init(sparse, false, SOMLibSparseInputData.DEFAULT_RANDOM_SEED);
        super.readVectorFile(inputFileName, sparse);
        postReading();
        writer.flush();
        writer.close();
        br.close();
    }

    public abstract void postReading() throws IOException;

    public abstract void preReading();
}
