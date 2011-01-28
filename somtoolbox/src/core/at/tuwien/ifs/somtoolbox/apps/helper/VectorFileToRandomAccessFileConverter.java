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
import java.io.RandomAccessFile;

import org.apache.commons.lang.StringUtils;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataFileFormatConverter;
import at.tuwien.ifs.somtoolbox.data.RandomAccessFileSOMLibInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;

/**
 * Converts an input file to a binary/random access input file. his class customises the handling of data read from the
 * file by storing it in an Random Access File.
 * <p>
 * This is a specific, memory saving implementation, that could otherwise be handled with
 * {@link InputDataFileFormatConverter}
 * </p>
 * .
 * 
 * @author Rudolf Mayer
 * @version $Id: VectorFileToRandomAccessFileConverter.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class VectorFileToRandomAccessFileConverter extends SOMLibSparseInputData {

    private RandomAccessFile file;

    boolean headerWritten = false;

    public VectorFileToRandomAccessFileConverter(String inputVectorFile) throws IOException {
        String outputFile = StringUtils.chomp(inputVectorFile, ".gz") + ".bin";
        if (new File(outputFile).exists()) {
            new File(outputFile).delete();
        }
        file = new RandomAccessFile(outputFile, "rw");
        // write all the vectors
        readVectorFile(inputVectorFile, false);
        // write the vector labels in the end of the file
        RandomAccessFileSOMLibInputData.writeVectorLabels(file, dataNames);
        file.close();
    }

    /**
     * Stores the information read in the random access file.
     */
    @Override
    protected void processLine(int documentIndex, String[] lineElements) throws Exception {
        if (!headerWritten) {
            headerWritten = RandomAccessFileSOMLibInputData.writeHeader(file, numVectors(), dim());
        }
        String label = lineElements[dim].trim();
        dataNames[documentIndex] = label;
        for (int termIndex = 0; termIndex < dim; termIndex++) {
            file.writeDouble(Double.parseDouble(lineElements[termIndex]));
        }
    }

    @Override
    protected void initMatrix(boolean sparse) {
        // do nothing, we don't need to store anything
    }

    /**
     * Starts the conversion
     * 
     * @param args Needed program arguments:
     *            <ul>
     *            <li>-v inputVectorFile, mandatory</li>
     *            </ul>
     * @throws IOException If the input vector file can't be read, or the output binary file can't be written.
     */
    public static void main(String[] args) throws IOException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptInputVectorFile(true));
        String inputVectorFile = config.getString("inputVectorFile");

        new VectorFileToRandomAccessFileConverter(inputVectorFile);
    }

}
