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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class gathers methods to write certain {@link InputData}, {@link TemplateVector} and
 * {@link SOMLibClassInformation} in a certain number of file formats, such as SOMLib, WEKA ARFF, SOMPak and ESOM.
 * 
 * @author Rudolf Mayer
 * @version $Id: InputDataWriter.java 3848 2010-10-12 12:29:26Z mayer $
 */
public class InputDataWriter {

    /** Supported Output File Format Types */
    public static final String[] OUTPUT_FILE_FORMAT_TYPES = { SOMLibSparseInputData.getFormatName(),
            ARFFFormatInputData.getFormatName(), RandomAccessFileSOMLibInputData.getFormatName(),
            ESOMInputData.getFormatName(), SOMPAKInputData.getFormatName(), "Orange", "CSV" };

    /**
     * Writes the data to <a href="http://databionic-esom.sourceforge.net/user.html#File_formats">ESOM lrn/cls
     * format</a>.
     */
    public static void writeAsESOM(InputData data, String fileName) throws IOException, SOMLibFileFormatException {
        String fileNameLrn = StringUtils.appendExtension(fileName, ".lrn");

        // write the header, see http://databionic-esom.sourceforge.net/user.html#File_formats
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing input data as ESOM file to '" + fileNameLrn + "'.");
        PrintWriter writer = FileUtils.openFileForWriting("ESOM lrn", fileNameLrn, false);
        if (org.apache.commons.lang.StringUtils.isNotBlank(data.getDataSource())) {
            writer.println("# Converted from " + data.getDataSource() + ".");
        }
        writer.println("% " + data.numVectors());
        writer.println("% " + (data.dim() + 1));
        writer.println("% 9" + StringUtils.repeatString(data.dim(), "\t 1"));
        writer.println("% Key\t" + StringUtils.interleave(data.templateVector().getLabels(), "\t"));
        for (int i = 0; i < data.numVectors(); i++) {
            writer.print(String.valueOf(i + 1)); // index in the lrn file will start with 1, make sure this is in synch
            // with ESOMMapOutputter
            for (int j = 0; j < data.dim(); j++) {
                writer.print("\t" + data.getValue(i, j));
            }
            writer.println();
        }
        writer.close();

        // write the names file
        String fileNameNames = StringUtils.appendOrReplaceExtension(fileName, ".lrn", ".names");
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing names as ESOM file to '" + fileNameNames + "'.");
        writer = FileUtils.openFileForWriting("ESOM names", fileNameNames, false);
        if (org.apache.commons.lang.StringUtils.isNotBlank(data.getDataSource())) {
            writer.println("# Converted from " + data.getDataSource() + ".");
        }
        writer.println("% " + data.numVectors());
        for (int i = 0; i < data.numVectors(); i++) {
            // index in the names file starts at 1, make sure this is in synch with lrn file and ESOMMapOutputter
            writer.println(String.valueOf(i + 1) + "\t" + data.getLabel(i));
        }
        writer.close();

        if (data.classInformation() != null) {
            // guess a good filename
            String fileNameCls = StringUtils.appendOrReplaceExtension(fileName, ".lrn", ".cls");
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Writing class info as ESOM file to '" + fileNameCls + "'.");
            writeAsESOM(data.classInformation(), fileNameCls);
        }

    }

    /**
     * Writes the class information as <a href="http://databionic-esom.sourceforge.net/user.html#File_formats">ESOM
     * cls</a> file.
     */
    public static void writeAsESOM(SOMLibClassInformation classInfo, String fileName) throws IOException,
            SOMLibFileFormatException {
        PrintWriter writer = FileUtils.openFileForWriting("ESOM class info", fileName);
        writer.println("% " + classInfo.numData);
        // write class index => class name mapping in header
        for (int i = 0; i < classInfo.numClasses(); i++) {
            writer.println("% " + i + " " + classInfo.getClassName(i));
        }
        for (String element : classInfo.getDataNames()) {
            writer.println(element + "\t" + classInfo.getClassIndexForInput(element));
        }
        writer.flush();
        writer.close();
    }

    public static void writeAsSOMLib(InputData data, String fileName) throws IOException {
        PrintWriter writer = FileUtils.openFileForWriting("Input vector file", fileName, true);
        InputDataWriter.writeHeaderToFile(writer, data.numVectors(), data.dim());
        for (int i = 0; i < data.numVectors(); i++) {
            InputDatum inputDatum = data.getInputDatum(i);
            InputDataWriter.writeInputDatumToFile(writer, inputDatum);
        }

        writer.flush();
        writer.close();
    }

    /** Writes the class information to a file. */
    public static void writeAsSOMLib(InputData data, TemplateVector templateVector,
            SOMLibClassInformation classInformation, boolean tabSeparatedClassFile, String basicFileName)
            throws IOException, SOMLibFileFormatException {
        writeAsSOMLib(data, basicFileName + ".vec");
        if (templateVector != null) {
            InputDataWriter.writeAsSOMLib(templateVector, basicFileName + ".tv");
        }
        if (classInformation != null) {
            if (tabSeparatedClassFile) {
                InputDataWriter.writeToFileTabSeparated(classInformation, basicFileName + ".cls");
            } else {
                InputDataWriter.writeAsSOMLib(classInformation, basicFileName + ".cls");
            }
        }
    }

    /** Writes the class information to a file. */
    public static void writeAsSOMLib(InputData data, TemplateVector templateVector,
            SOMLibClassInformation classInformation, String basicFileName) throws IOException,
            SOMLibFileFormatException {
        writeAsSOMLib(data, templateVector, classInformation, false, basicFileName);
    }

    /** Writes the class information to a file in SOMLib format. */
    public static void writeAsSOMLib(SOMLibClassInformation classInfo, String fileName) throws IOException,
            SOMLibFileFormatException {
        PrintWriter writer = FileUtils.openFileForWriting("SOMLib class info", fileName);
        writer.println("$TYPE class_information");
        writer.println("$NUM_CLASSES " + classInfo.numClasses());
        writer.write("$CLASS_NAMES ");
        for (int i = 0; i < classInfo.numClasses(); i++) {
            writer.write(classInfo.getClassName(i));
            if (i + 1 < classInfo.numClasses()) {
                writer.write(" ");
            }
        }
        writer.println();
        writer.println("$XDIM 2");
        writer.println("$YDIM " + classInfo.numData);
        for (String element : classInfo.getDataNames()) {
            writer.println(element + " " + classInfo.getClassName(element));
        }

        writer.flush();
        writer.close();
    }

    /** Writes the class information to a file in SOMLib format. */
    public static void writeAsSOMLib(HashMap<String, String> classInfo, HashSet<String> classNames, String fileName)
            throws IOException, SOMLibFileFormatException {
        ArrayList<String> classNamesList = new ArrayList<String>(classNames);
        Collections.sort(classNamesList);

        PrintWriter writer = FileUtils.openFileForWriting("SOMLib class info", fileName);
        writer.println("$TYPE class_information");

        writer.println("$NUM_CLASSES " + classNames.size());
        writer.println("$CLASS_NAMES " + StringUtils.toString(classNamesList, "", "", " "));
        writer.println("$XDIM 2");
        writer.println("$YDIM " + classInfo.size());
        for (String key : classInfo.keySet()) {
            writer.println(key + " " + classNamesList.indexOf(classInfo.get(key)));
        }

        writer.flush();
        writer.close();
    }

    public static void writeAsSOMLib(TemplateVector tv, String fileName) throws IOException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start writing new  template vector to '" + fileName + "'.");
        PrintWriter writer = FileUtils.openFileForWriting("Template Vector", fileName, fileName.endsWith(".gz"));
        writeTempplateHeaderToFile(writer, fileName, tv.numVectors(), tv.dim(), tv.numinfo());
        for (int i = 0; i < tv.dim(); i++) {
            writeElementToFile(writer, i, tv.getElement(i));
        }
        writer.flush();
        writer.close();
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished.");
    }

    public static void writeElementToFile(PrintWriter writer, int i, TemplateVectorElement e) {
        StringBuffer b = new StringBuffer();
        int numinfo = e.getTemplateVector().numinfo();
        if (numinfo > 2) {
            b.append(" ").append(e.getDocumentFrequency());
        }
        if (numinfo > 3) {
            b.append(" ").append(e.getCollectionTermFrequency());
        }
        if (numinfo > 4) {
            b.append(" ").append(e.getMinimumTermFrequency());
        }
        if (numinfo > 5) {
            b.append(" ").append(e.getMaximumTermFrequency());
        }
        if (numinfo > 6) {
            b.append(" ").append(e.getMeanTermFrequency());
        }
        if (e.getComment() != null) {
            b.append(" ").append(e.getComment());
        }
        writer.println(i + " " + e.getLabel() + b.toString());
    }

    /**
     * Writes input data in the SOMPAK format (see
     * http://www.cis.hut.fi/projects/somtoolbox/package/docs2/som_read_data.html)
     */
    public static void writeAsSOMPAK(InputData data, String fileName) throws IOException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing input data as SOMPAK file to '" + fileName + "'.");
        PrintWriter writer = FileUtils.openFileForWriting("SOMPAK data", fileName, false);
        // number of dimensions first, simply the number
        writer.println(data.dim());
        // now all component names
        TemplateVector tv = data.templateVector();
        if (tv == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Template vector not loaded - creating a generic one.");
            tv = new SOMLibTemplateVector(data.numVectors(), data.dim());
        }
        writer.println(SOMPAKInputData.INDICATOR_COMPONENTS + " " + StringUtils.toString(tv.getLabels(), "", "", " "));
        // now all data, appended by the class name
        for (int i = 0; i < data.numVectors(); i++) {
            for (int j = 0; j < data.dim(); j++) {
                writer.print(data.getValue(i, j));
                if (j + 1 < data.dim()) {
                    writer.print(" ");
                }
            }
            if (data.classInformation() != null) {
                writer.print(" " + data.classInformation().getClassName(i));
            }
            writer.println();
        }
        writer.close();
    }

    /** Writes the data to <a href="http://www.cs.waikato.ac.nz/~ml/weka/arff.html">Weka ARFF format</a>. */
    public static void writeAsWekaARFF(InputData data, String fileName, boolean writeInstanceNames,
            boolean skipInputsWithoutClass) throws IOException, SOMToolboxException {
        if (data.classInformation() == null) {
            throw new SOMToolboxException("Class Information File needed for WEKA ARFF writing");
        }
        fileName = StringUtils.ensureExtension(fileName, ".arff");
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing input data as ARFF file to '" + fileName + "'.");
        PrintWriter writer = FileUtils.openFileForWriting("Weka ARFF", fileName, false);

        TemplateVector tv = data.templateVector();
        if (tv == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Template vector not loaded - creating a generic one.");
            tv = new SOMLibTemplateVector(data.numVectors(), data.dim());
        }

        String relation = fileName.substring(0, fileName.length() - 4);
        writer.println("@RELATION " + relation + "\n");
        for (int i = 0; i < tv.dim(); i++) {
            writer.println("@ATTRIBUTE " + tv.getLabel(i) + " NUMERIC");
        }

        if (writeInstanceNames) {
            writer.println("@ATTRIBUTE instanceName STRING");
        }

        writer.println(getWekaClassHeader(data.classInformation().classNames()));

        writer.println("@DATA");
        int skipCounter = 0;
        StdErrProgressWriter progress = new StdErrProgressWriter(data.numVectors(), "Writing vector ",
                data.numVectors() / 10);
        for (int i = 0; i < data.numVectors(); i++) {
            InputDatum inputDatum = data.getInputDatum(i);
            if (skipInputsWithoutClass && !data.classInformation().hasClassAssignmentForName(inputDatum.getLabel())) {
                skipCounter++;
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Skipping datum '" + inputDatum.getLabel() + "', as it has no class assigned; skipped "
                                + skipCounter + " so far.");
                continue;
            }
            DoubleMatrix1D vector = inputDatum.getVector();
            for (int j = 0; j < data.dim(); j++) {
                writer.print(vector.get(j) + ",");
            }

            if (writeInstanceNames) {
                writer.print("'" + StringUtils.escapeForWeka(inputDatum.getLabel()) + "',");
            }

            writer.println("'" + data.classInformation().getClassName(inputDatum.getLabel()) + "'");
            progress.progress();
        }
        writer.flush();
        writer.close();
    }

    public static String getWekaClassHeader(String[] classNames) {
        String classNamesString = "";
        for (String className : classNames) {
            if (classNamesString.length() > 0) {
                classNamesString += ",";
            }
            classNamesString += "'" + StringUtils.escapeClassNameForWeka(className) + "'";
        }
        String x = "@ATTRIBUTE class {" + classNamesString + "}";
        return x;
    }

    /**
     * Writes input data in the tab-separated format used by the Orange data mining toolkit (see
     * http://www.ailab.si/orange/)
     */
    public static void writeAsOrange(InputData data, String fileName) throws IOException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing input data as Orange file to '" + fileName + "'.");
        PrintWriter writer = FileUtils.openFileForWriting("Orange data", fileName, false);

        TemplateVector tv = data.templateVector();
        if (tv == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Template vector not loaded - creating a generic one.");
            tv = new SOMLibTemplateVector(data.numVectors(), data.dim());
        }
        SOMLibClassInformation classInformation = data.classInformation();
        boolean haveClassInfo = classInformation != null;

        /* - first the tab-separated names of the features
        - then the types of the features
        - and then the indicator whether a feature is the class assignment
        e.g. for IRIS:
             sepallength sepalwidth  petallength petalwidth  class 
             continuous  continuous  continuous  continuous  discrete
                                                             class
        */

        // row 1: tab-separated label names
        writer.print(StringUtils.toString(tv.getLabels(), "", "", "\t"));
        if (haveClassInfo) { // and optionally the class
            writer.print("\tclass");
        }
        writer.println();

        // row 2: data types
        for (int i = 0; i < tv.dim(); i++) {
            writer.print("continuous"); // all are continuous
            if (i + 1 < tv.dim()) {
                writer.print("\t");
            }
        }
        if (haveClassInfo) {
            writer.print("\tdiscrete"); // just the class is discrete
        }
        writer.println();

        // row 3: indicating options to the attributes
        writer.print(StringUtils.repeatString(tv.dim() - 1, "\t"));
        if (haveClassInfo) {
            writer.print("\tclass"); // the class attribute
        }
        writer.println();

        // now the data, tab separated, and optionally with the class assignment

        // now all data, appended by the class name
        for (int i = 0; i < data.numVectors(); i++) {
            for (int j = 0; j < data.dim(); j++) {
                writer.print(data.getValue(i, j));
                if (j + 1 < data.dim()) {
                    writer.print("\t");
                }
            }
            if (haveClassInfo) {
                writer.print("\t" + classInformation.getClassName(i));
            }
            writer.println();
        }
        writer.close();
    }

    public static void writeAsCSV(InputData data, String fileName) throws IOException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing input data as CVS to '" + fileName + "'.");
        PrintWriter writer = FileUtils.openFileForWriting("CVS", fileName, false);

        TemplateVector tv = data.templateVector();
        if (tv == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Template vector not loaded - creating a generic one.");
            tv = new SOMLibTemplateVector(data.numVectors(), data.dim());
        }
        SOMLibClassInformation classInformation = data.classInformation();
        boolean haveClassInfo = classInformation != null;

        String separator = "\t";

        // header: tab-separated label names
        writer.print(StringUtils.toString(tv.getLabels(), "", "", separator, "\""));
        if (haveClassInfo) { // and optionally the class
            writer.print(separator + "class");
        }
        writer.println();

        // data: tab separated, optionally with the class assignment
        for (int i = 0; i < data.numVectors(); i++) {
            for (int j = 0; j < data.dim(); j++) {
                writer.print(data.getValue(i, j));
                if (j + 1 < data.dim()) {
                    writer.print(separator);
                }
            }
            if (haveClassInfo) {
                writer.print(separator + classInformation.getClassName(i));
            }
            writer.println();
        }
        writer.close();
    }

    public static void writeHeaderToFile(PrintWriter writer, int numVectors, int dim) throws IOException {
        writer.println("$TYPE vec");
        writer.println("$XDIM " + numVectors);
        writer.println("$YDIM 1");
        writer.println("$VEC_DIM " + dim);
    }

    public static void writeInputDatumToFile(PrintWriter writer, InputDatum inputDatum) throws IOException {
        InputDataWriter.writeInputDatumToFile(writer, inputDatum.getLabel(), inputDatum.getVector());
    }

    public static void writeInputDatumToFile(PrintWriter writer, String label, DoubleMatrix1D vector)
            throws IOException {
        for (int i = 0; i < vector.size(); i++) {
            if (!Double.isNaN(vector.get(i))) {
                writer.write(vector.get(i) + " ");
            } else {
                writer.write("? ");
            }
        }
        writer.println(label);
    }

    public static void writeTempplateHeaderToFile(PrintWriter writer, String fileName, final int numVectors,
            final int dim, final int numInfo) throws IOException {
        writer.println("$TYPE template");
        writer.println("$XDIM " + numInfo);
        writer.println("$YDIM " + numVectors);
        writer.println("$VEC_DIM " + dim);
    }

    /** Writes the class information to a tab-separated file. */
    public static void writeToFileTabSeparated(SOMLibClassInformation classInfo, String fileName) throws IOException,
            SOMLibFileFormatException {
        PrintWriter writer = FileUtils.openFileForWriting("Tab-separated class info", fileName);
        for (String element : classInfo.getDataNames()) {
            writer.println(element + "\t" + classInfo.getClassName(element));
        }
        writer.flush();
        writer.close();
    }

    public static void write(String fName, InputData data, String outputFormat, boolean tabSeparatedClassFile,
            boolean skipInstanceNames, boolean skipInputsWithoutClass) throws IOException, SOMToolboxException {
        Logger logger = Logger.getLogger("at.tuwien.ifs.somtoolbox");
        if (outputFormat.equals(AbstractSOMLibSparseInputData.getFormatName())) {
            logger.info("Writing SOMLib Data Format.");
            InputDataWriter.writeAsSOMLib(data, data.templateVector(), data.classInformation(), tabSeparatedClassFile,
                    fName + ".vec");
        } else if (outputFormat.equals(RandomAccessFileSOMLibInputData.getFormatName())) {
            logger.info("Writing Random Access Binary Data Format.");
            RandomAccessFileSOMLibInputData.write(data, fName);
        } else if (outputFormat.equals(ARFFFormatInputData.getFormatName())) {
            logger.info("Writing ARFF Data Format, skipping instance names: " + skipInstanceNames);
            InputDataWriter.writeAsWekaARFF(data, fName, !skipInstanceNames, skipInputsWithoutClass);
        } else if (outputFormat.equals(ESOMInputData.getFormatName())) {
            logger.info("Writing ESOM Data Format");
            InputDataWriter.writeAsESOM(data, fName);
        } else if (outputFormat.equals(SOMPAKInputData.getFormatName())) {
            logger.info("Writing SOMPAK Data Format");
            InputDataWriter.writeAsSOMPAK(data, fName);
        } else if (outputFormat.equals("Orange")) {
            logger.info("Writing Orange Data Format");
            InputDataWriter.writeAsOrange(data, fName);
        } else if (outputFormat.equals("CSV")) {
            logger.info("Writing CSV Format");
            InputDataWriter.writeAsCSV(data, fName);
        } else {
            // check for logical programming mistakes, basically
            throw new SOMToolboxException("Didn't write format of type '" + outputFormat
                    + "', most likely a programming error.");
        }
    }
}
