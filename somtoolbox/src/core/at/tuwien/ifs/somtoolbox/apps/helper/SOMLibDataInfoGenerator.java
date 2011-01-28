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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Handles the normalization of vector files in SOMLib format. This class can be run in standalone mode taking two
 * arguments, i.e. input and output file. If the input file is gzip-compressed, the output will also be written
 * gzip-compressed. The .gz suffix has to be specified manually in order not to alter filenames to something other than
 * intended by the user.
 * <p>
 * <i>Created on Mar 16, 2004</i>
 * </p>
 * 
 * @author Michael Dittenbach
 * @version $Id: SOMLibDataInfoGenerator.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class SOMLibDataInfoGenerator {

    /**
     * Static method for standalone invocation.
     * 
     * @param args Usage: method-type input-filename output-filename
     */
    public static void main(String[] args) {
        // -b base directory, mand.
        // -r common part to be removed, opt.
        // input vector file
        // data info file

        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_DATA_INFO_GENERATOR);

        String baseDir = config.getString("baseDir");
        String removeDir = config.getString("removeDir");
        String inputVectorFileName = config.getString("inputVectorFile");
        String dataInfoFileName = config.getString("dataInfoFile");

        try {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting data info file generation");
            generateDataInfo(inputVectorFileName, dataInfoFileName, baseDir, removeDir);
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("finished data info file generation");
    }

    /**
     * Static method taking input and output filename as argument. File handling is done in this method and throws a
     * FileNotFoundException if the input file can not be found and an IOException if some other file handling error
     * occurs. If the input file is gzip-compressed, the output will also be written gzip-compressed. The .gz suffix has
     * to be specified manually in order not to alter filenames to something other than intended by the user.
     * 
     * @param inFileName Name of input file.
     * @param outFileName Name of output file.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *             other reason cannot be opened for reading.
     */
    public static void generateDataInfo(String inFileName, String outFileName, String baseDir, String removeDir)
            throws FileNotFoundException, IOException {
        BufferedReader inFile = null;
        boolean gzipped = false;
        try {
            inFile = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inFileName))));
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    inFileName + " is gzip compressed. Trying compressed read. Creating compressed output.");
            gzipped = true;
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Input vector file " + inFileName + " not found. Aborting.");
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    inFileName + " is not gzip compressed. Trying uncompressed read. Creating uncompressed output.");
            try {
                inFile = new BufferedReader(new FileReader(inFileName));
                gzipped = false;
            } catch (FileNotFoundException e2) {
                throw new FileNotFoundException("Input vector file " + inFileName + " not found. Aborting.");
            }
        }
        BufferedWriter outFile = null;
        try {
            if (gzipped == false) {
                outFile = new BufferedWriter(new FileWriter(outFileName));
            } else {
                outFile = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(
                        outFileName))));
            }
        } catch (IOException e) {
            throw new IOException("Can not open data info file " + outFileName + ". Aborting.");
        }
        generateDataInfo(inFile, outFile, baseDir, removeDir);
    }

    /**
     * Static method taking a Buffered Reader and BufferedWriter as argument. The method exits the program if the file
     * format is corrupt. TODO: This should be weakened in future by throwing a SOMLibVectorFileFormatException.
     * 
     * @param inReader BufferedReader reading the file continaing unnormalized vectors.
     * @param outWriter BuffererWriter writing the normalized vector file.
     */
    public static void generateDataInfo(BufferedReader inReader, BufferedWriter outWriter, String baseDir,
            String removeDir) {
        String line = null;
        // int vectorDim = 0;
        int numVectors = 0;
        // String fileSeparator = System.getProperty("file.separator");

        char sep1 = getSeparatorFromString(baseDir);
        if (!baseDir.endsWith(String.valueOf(sep1))) {
            baseDir = baseDir + String.valueOf(sep1);
        }
        sep1 = getSeparatorFromString(removeDir);
        if (!removeDir.endsWith(String.valueOf(sep1))) {
            removeDir = removeDir + String.valueOf(sep1);
        }

        try {
            int index = 1;
            outWriter.write("$TYPE data_info");
            outWriter.newLine();
            outWriter.write("$BASE_DIR " + URLEncoder.encode(baseDir, "UTF-8"));
            outWriter.newLine();
            while ((line = inReader.readLine()) != null) {
                if (line.startsWith("$")) { // write fields XDIM and YDIM through without change
                    if (line.startsWith("$TYPE")) {
                        // ignore
                    } else if (line.startsWith("$XDIM")) {
                        outWriter.write(line);
                        outWriter.newLine();
                        String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                        if (lineElements.length > 1) {
                            numVectors = Integer.parseInt(lineElements[1]);
                        } else {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                    "Input vector file format corrupt. Aborting.");
                            System.exit(-1);
                        }
                    } else if (line.startsWith("$YDIM")) {
                        outWriter.write(line);
                        outWriter.newLine();
                        String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                        if (lineElements.length > 1) {
                            numVectors *= Integer.parseInt(lineElements[1]);
                        } else {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                    "Input vector file format corrupt. Aborting.");
                            System.exit(-1);
                        }
                    } else if (line.startsWith("$VEC_DIM") || line.startsWith("$VECDIM")) {
                        String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                        if (lineElements.length > 1) {
                            // vectorDim = Integer.parseInt(lineElements[1]);
                        } else {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                    "Input vector file format corrupt. Aborting.");
                            System.exit(-1);
                        }
                    }
                } else {
                    String label = line.substring(line.lastIndexOf(' ') + 1);
                    label = URLDecoder.decode(label, "UTF-8");
                    char sep = getSeparatorFromString(label);
                    String displayName = label.substring(label.lastIndexOf(sep) + 1);

                    String fileName = null;
                    if (label.indexOf(removeDir) != -1) {
                        fileName = label.substring(removeDir.length());
                    }

                    // System.out.println("removeDir: "+removeDir);

                    // System.out.println(label + " " + displayName + " " + fileName);
                    // System.out.println(displayName + " " + fileName);

                    outWriter.write(URLEncoder.encode(label, "UTF-8") + " " + URLEncoder.encode(displayName, "UTF-8")
                            + " " + URLEncoder.encode(fileName, "UTF-8"));
                    outWriter.newLine();
                    index++;
                }
            }
            if (index - 1 != numVectors) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Input vector file format corrupt. Incorrect number of vectors - header: " + numVectors
                                + ", read " + index + " . Aborting.");
                System.exit(-1);
            }
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Problem writing data info file. Aborting.");
            System.exit(-1);
        }
        try {
            inReader.close();
            outWriter.close();
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Problem closing vector files. Aborting.");
            System.exit(-1);
        }
    }

    /**
     * @param path the path to calculate the separator of.
     * @return the separator string, either '/' or '\\'
     */
    private static char getSeparatorFromString(String path) {
        char sep = '/';
        if (path.indexOf("/") != -1) {
            sep = '/';
        } else if (path.indexOf("\\") != -1) {
            sep = '\\';
        }
        return sep;
    }

}
