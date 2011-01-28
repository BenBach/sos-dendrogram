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

import static org.apache.commons.lang.StringUtils.isBlank;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.util.PaintList;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringIntegerComparator;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class provides information about class labels for the {@link InputData} input vectors.<br>
 * <p>
 * The file format consists of a <code>header</code> and the content as follows:
 * </p>
 * <b>$TYPE</b> string, mandatory. Fixed to <i>class_information.</i> <br>
 * <b>$NUM_CLASSES</b> integer, mandatory: gives the number of classes. <br>
 * <b>$CLASS_NAMES</b> mandatory: a space-separated list of class names; the count has to be the same as in
 * $NUM_CLASSES. <br>
 * <b>$XDIM</b> integer, mandatory: number of units in x-direction. Fixed to <i>2</i>. <br>
 * <b>$YDIM</b> integer, mandatory: dimensionality class information vector, equals the number of input vectors (
 * {@link InputData#numVectors()}). <br>
 * <b>labelName_n&nbsp;classIndex_n</b> the $YDIM number of mappings from the input vector label name to the class label
 * index [0...($NUM_CLASSES-1)]. <br>
 * <p>
 * See also an example file from the <a href="../../../../../examples/iris.cls">Iris data set</a>.
 * </p>
 * <p>
 * Alternatively, the file format can be more simple, and not contain any file header. Then, there is only a list of
 * lines with two tabulator-seperated <code>Strings</code> in the form of <code>labelName&nbsp;className</code>.<br>
 * The number of classes, and the indices of those classes, are computer automatically.
 * </p>
 * <p>
 * Finally, the simplest form of the file is to have lines with just the class label; then, this class is assigned to
 * the input datum with the index of the line number.<br>
 * The number of classes, and the indices of those classes, are computer automatically.
 * </p>
 * 
 * @author Michael Dittenbach
 * @author Thomas Lidy
 * @author Rudolf Mayer
 * @version $Id: SOMLibClassInformation.java 3888 2010-11-02 17:42:53Z frank $
 */
@SuppressWarnings("rawtypes")
public class SOMLibClassInformation {

    private static final Logger logger = Logger.getLogger("at.tuwien.ifs.somtoolbox");

    /**
     * The file name to read from.
     */
    protected String classInformationFileName = null;

    /**
     * The number of classes. Either read from the file header, or computed from the distinct number of class names in
     * the tab-seperated file.
     */
    private int numClasses = 0;

    /**
     * The names of the classes. Either read from the file header, or computed from the distinct class names in the
     * tab-seperated file.
     */
    private String[] classNames = null;

    /** Returns the names of the classes. */
    public String[] getClassNames() {
        return classNames;
    }

    /** Returns an array of data names for each class. */
    public String[][] getDataNamesPerClass() {
        String[][] all = new String[classNames.length][];
        for (int i = 0; i < classNames.length; i++) {
            all[i] = getDataNamesInClass(classNames[i]);
        }
        return all;
    }

    public String[] getDataNames() {
        return dataNames;
    }

    /**
     * The number of inputs in each class.
     */
    private int[] classMemberCount = null;

    /**
     * The number of input vectors. Either read from the file header, or computed from the number of data lines in the
     * tab-seperated file.
     */
    protected int numData = 0;

    // FIXME: not used?
    private String[] dataNames = null;

    /**
     * A mapping input index => class index, for fast lookup.
     */
    private int[] dataClasses = null;

    /**
     * Mapping class name => class index, for fast lookup.
     */
    private HashMap<String, Comparable> dataHash = null;

    private ArrayList<String> classNamesTemp;

    private ArrayList<String> dataNamesTemp;

    private PaintList paintList;

    /**
     * Constructor intended to be used e.g. when generating data, or when reading a file with the
     * {@link SOMPAKInputData}
     */
    public SOMLibClassInformation() {
        dataHash = new HashMap<String, Comparable>();
        classNamesTemp = new ArrayList<String>();
        dataNamesTemp = new ArrayList<String>();
    }

    public SOMLibClassInformation(Map<String, String> classAssignment) {
        this();
        for (Map.Entry<String, String> entry : classAssignment.entrySet()) {
            addItem(entry.getKey(), entry.getValue());
        }
        processItems(false);
    }

    /** Constructor intended to be used when generating data. */
    public SOMLibClassInformation(String[] classNames, String[][] dataName) {
        this();
        this.classNames = classNames;
        numClasses = classNames.length;

        numData = 0;
        classMemberCount = new int[numClasses];
        for (int i = 0; i < dataName.length; i++) {
            numData += dataName[i].length;
            classMemberCount[i] = dataName[i].length;
        }

        dataNames = new String[numData];
        dataClasses = new int[numData];
        dataHash = new HashMap<String, Comparable>();

        int index = 0;
        for (int i = 0; i < dataName.length; i++) {
            for (int j = 0; j < dataName[i].length; j++) {
                dataNames[index] = dataName[i][j];
                dataHash.put(dataName[i][j], new Integer(i));
                dataClasses[index] = i;
                index++;
            }
            // System.arraycopy(dataName[i], 0, dataNames, index, dataName[i].length);
            // index += dataName[i].length;
        }
        initPaintList();
    }

    /**
     * Creates a new class information object by trying to read the given file in both the versions with a file header (
     * {@link #readSOMLibClassInformationFile()}) and the tab-separated file ({@link #readTabSepClassInformationFile()}
     * ).
     * 
     * @param classInformationFileName The file to read from
     * @throws SOMToolboxException if there is any error in the file format
     */
    public SOMLibClassInformation(String classInformationFileName) throws SOMToolboxException {
        this.classInformationFileName = classInformationFileName;

        // TODO: br.close() in case of any error!!!
        try {
            readSOMLibClassInformationFile();
        } catch (ClassInfoHeaderNotFoundException nfe) {
            logger.info("Reading SOMLib Class infromation file format failed: " + nfe.getMessage());
            logger.info("Trying to read tab/space separated class info file...");
            try {
                readTabSepClassInformationFile();
            } catch (SOMToolboxException e) {
                try {
                    logger.info("Reading tab/space separated class file failed: " + e.getMessage());
                    logger.info("Trying to read simple format...");
                    readSimple();
                } catch (IOException ioEx) {
                    throwClassInfoReadingError(classInformationFileName, ioEx);
                }
            } catch (IOException e) {
                throwClassInfoReadingError(classInformationFileName, e);
            }
        } catch (IOException e) {
            throwClassInfoReadingError(classInformationFileName, e);
        }
        if (paintList == null) {
            initPaintList();
        }

        logger.info("Class information file correctly loaded.");
    }

    private void throwClassInfoReadingError(String classInformationFileName, IOException e)
            throws SOMLibFileFormatException {
        throw new SOMLibFileFormatException("Problems reading class information file " + classInformationFileName
                + ": ' " + e.getMessage() + "'. Aborting.");
    }

    /**
     * Reads a class information file containing no header, and tab-separated String entries for the input vector and
     * class labels.
     * 
     * @throws SOMToolboxException if there is any error in the file format
     */
    private void readTabSepClassInformationFile() throws SOMToolboxException, IOException {
        String line = null;
        String name, classname;
        int index = 0; // line counter
        classNamesTemp = new ArrayList<String>();
        dataNamesTemp = new ArrayList<String>();
        BufferedReader br = FileUtils.openFile("Class information file", classInformationFileName);

        dataHash = new HashMap<String, Comparable>();

        while ((line = br.readLine()) != null) {
            index++;
            if (line.trim().length() == 0) {
                continue; // ignore empty lines
            }
            String[] lineElements = line.split("[\t]+"); // StringUtils.REGEX_SPACE_OR_TAB);

            if (lineElements.length != 2) {
                br.close();
                throw new SOMLibFileFormatException("Number of elements per line must be exactly 2! Error in line "
                        + index);
            }

            name = lineElements[0];
            classname = lineElements[1];
            addItem(name, classname);
        }

        br.close();

        processItems(true);
    }

    private void readSimple() throws SOMToolboxException, IOException {
        boolean lastEmpty = false;
        String line = null;
        int index = 0; // line counter
        BufferedReader br = FileUtils.openFile("Class information file", classInformationFileName);

        while ((line = br.readLine()) != null) {
            index++;
            if (isBlank(line)) {
                lastEmpty = true;
            } else {
                if (lastEmpty) {
                    br.close();
                    throw new SOMLibFileFormatException("Empty line # " + index);
                }
                addItem(String.valueOf(index - 1), line.trim());
            }
        }
        br.close();
        processItems(false);
    }

    public void processItems(boolean sort) {
        numData = dataNamesTemp.size();
        numClasses = classNamesTemp.size();
        classNames = classNamesTemp.toArray(new String[numClasses]);
        if (sort) {
            Arrays.sort(classNames, new StringIntegerComparator(classNames));
        }
        classNamesTemp = new ArrayList<String>(Arrays.asList(classNames));

        dataNames = dataNamesTemp.toArray(new String[numData]);
        dataClasses = new int[numData];

        classMemberCount = new int[numClasses];
        for (int i = 0; i < dataNamesTemp.size(); i++) {
            String label = dataNamesTemp.get(i);
            Object classname = dataHash.get(label);
            int classid = classNamesTemp.indexOf(classname);
            if (classid < 0) {
                System.out.printf("(%d) Did not find classindex for \"%s\", which is the class of \"%s\"%n", i,
                        classname, label);
                continue;
            }
            dataNames[i] = label;
            dataClasses[i] = classid;
            dataHash.put(label, new Integer(classid));
            classMemberCount[classid]++;
        }
        initPaintList();
    }

    public void addItem(String label, String classname) {
        // Rudi: not sure what the below code attempted to do, but it breaks things in some cases, as too little steps
        // are done
        // also, it is kind-of duplicate to if (dataHash.containsKey(label))
        // thus, commented it out..

        // if (!dataHash.containsKey(label)) {
        // dataHash.put(label, classname);
        // } else {
        // return;
        // }

        if (!classNamesTemp.contains(classname)) {
            classNamesTemp.add(classname);
        }
        if (dataHash.containsKey(label)) {
            if (!dataHash.get(label).equals(classname)) {
                logger.warning("Ignoring duplicate label " + label + ", existing class is: '" + dataHash.get(label)
                        + "', new class would have been: '" + classname + "'.");
            }
        } else {
            dataHash.put(label, classname);
            dataNamesTemp.add(label);
        }
    }

    /** Reads a class information file containing a header and class indices. */
    protected void readSOMLibClassInformationFile() throws IOException, SOMToolboxException {
        String line = null;
        int index = 0; // line counter
        int columns = 0;
        numData = 0;
        BufferedReader br = FileUtils.openFile("Class information file", classInformationFileName);

        // PROCESS HEADER as long as lines start with $
        while ((line = br.readLine()) != null) {

            // we ignore comment lines
            if (line.startsWith("#")) {
                continue;
            }

            if (!line.startsWith("$")) {
                break;
            }

            index++;

            if (line.startsWith("$TYPE")) {
                // ignore
            } else if (line.startsWith("$NUM_CLASSES")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    numClasses = Integer.parseInt(lineElements[1]);
                } else {
                    throw new SOMLibFileFormatException(
                            "Class information file format corrupt in $NUM_CLASSES line. Aborting.");
                }
            } else if (line.startsWith("$CLASS_NAMES")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    classNames = new String[numClasses];
                    if (lineElements.length > numClasses + 1) {
                        throw new SOMLibFileFormatException(
                                "Class information file format corrupt in $CLASS_NAMES line; expecting to find "
                                        + numClasses + " classes, but found " + (lineElements.length - 1)
                                        + ". Aborting.");
                    }
                    for (int c = 0; c < numClasses; c++) {
                        classNames[c] = lineElements[c + 1];
                    }
                } else {
                    throw new SOMLibFileFormatException(
                            "Class information file format corrupt in $CLASS_NAMES line. Aborting.");
                }
            } else if (line.startsWith("$XDIM")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    columns = Integer.parseInt(lineElements[1]);
                    if (columns < 2) {
                        throw new SOMLibFileFormatException(
                                "Class information file format corrupt. At least 2 columns (name, classId) required. Aborting.");
                    }
                } else {
                    throw new SOMLibFileFormatException();
                }
            } else if (line.startsWith("$YDIM")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    numData = Integer.parseInt(lineElements[1]);
                } else {
                    throw new SOMLibFileFormatException(
                            "Class information file format corrupt in $YDIM line. Aborting.");
                }
            } else if (line.startsWith("$CLASS_COLOURS ")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    paintList = new PaintList();
                    int colourIndex = 0;
                    for (int i = 1; i < lineElements.length; i++) {
                        String[] rgb = lineElements[i].split(",");
                        if (rgb.length == 3) {
                            try {
                                Color colour = new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]),
                                        Integer.parseInt(rgb[2]));
                                paintList.setPaint(colourIndex, colour);
                                colourIndex++;
                            } catch (NumberFormatException e) {
                                throw new SOMLibFileFormatException(
                                        "Class information file format corrupt in $CLASS_COLOURS - colour '"
                                                + lineElements[i] + "' is not correct:" + e.getMessage() + "Aborting.");
                            }
                        } else {
                            throw new SOMLibFileFormatException(
                                    "Class information file format corrupt in $CLASS_COLOURS - colour '"
                                            + lineElements[i] + "' is not correct. Aborting.");
                        }
                    }
                    if (colourIndex + 1 < numClasses()) { // if we have too few classes
                        logger.info("Class info file contained too few class colours (" + colourIndex
                                + ", # of classes: " + numClasses() + "), filling up with default classes.");

                        DrawingSupplier supplier = new DefaultDrawingSupplier();
                        for (int i = colourIndex + 1; i < numClasses(); i++) {
                            paintList.setPaint(i, supplier.getNextPaint());
                        }
                    }
                } else {
                    throw new SOMLibFileFormatException(
                            "Class information file format corrupt in $CLASS_COLOURS line. Aborting.");
                }
            }
        }

        if (index == 0) {
            throw new ClassInfoHeaderNotFoundException("Class information file: no header line starting with $ found");
        }

        classMemberCount = new int[numClasses];

        // READ REST OF THE FILE
        if (numData == 0) {
            throw new SOMLibFileFormatException("Class information file format corrupt. Missing $YDIM value. Aborting.");
        }

        dataNames = new String[numData];
        dataClasses = new int[numData];
        dataHash = new HashMap<String, Comparable>(numData);

        index = 0;

        while (line != null) {
            // TODO if line is no comment line ($)
            index++;
            String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
            if (lineElements.length != columns) {
                throw new SOMLibFileFormatException("Class information file format corrupt in element number " + index
                        + ", incorrect number of columns: XDIM:   " + columns + ", columns: " + lineElements.length
                        + ". Aborting.");
            } else {
                try {
                    dataNames[index - 1] = lineElements[0];
                    dataClasses[index - 1] = Integer.parseInt(lineElements[1]);
                    dataHash.put(lineElements[0], dataClasses[index - 1]);
                    classMemberCount[Integer.parseInt(lineElements[1])]++;
                } catch (NumberFormatException e) { // does not happen at the moment
                    throw new SOMLibFileFormatException("Class id number format corrupt in element number " + index
                            + ": '" + lineElements[1] + "'. Aborting.");
                }
            }

            line = br.readLine();
        }

        if (index != numData) {
            throw new SOMLibFileFormatException(
                    "Class information file format corrupt. Incorrect number of data items. Aborting.\n"
                            + Integer.toString(index) + " " + Integer.toString(numData));
        }

        br.close();
    }

    /**
     * Gets the number of classes, as read from $NUM_CLASSES, or computed.
     * 
     * @return the number of classes.
     */
    public int numClasses() {
        return numClasses;
    }

    /**
     * Returns all the distinct class names.
     * 
     * @return the class names.
     */
    public String[] classNames() {
        return classNames;
    }

    /**
     * Gets the index number for a given class label.
     * 
     * @param className the class label.
     * @return the index of that label.
     */
    public int getClassIndex(String className) {
        Object classid = dataHash.get(className);
        if (classid == null) {
            return -1;
        } else {
            return ((Integer) classid).intValue();
        }
    }

    /**
     * Gets the class label name for a given input vector index.
     * 
     * @param index index of the input vector.
     * @return the name of the class.
     */
    public String getClassName(int index) {
        return classNames[dataClasses[index]];
    }

    /**
     * Gets the class name for a vector name.
     * 
     * @param vectorName the name of the input vector.
     * @return the name of the class.
     * @throws SOMLibFileFormatException If there is no class information available for the given vector name/label
     */
    public String getClassName(String vectorName) throws SOMLibFileFormatException {
        Object object = dataHash.get(vectorName);
        if (object instanceof Integer) {
            return classNames[((Integer) object).intValue()];
        } else {
            throw new SOMLibFileFormatException(
                    "Class information file incomplete. Error pairing input vectors with class names for vector label '"
                            + vectorName + "'.");
        }
    }

    public boolean hasClassAssignmentForName(String vectorName) {
        return dataHash.containsKey(vectorName);
    }

    public int getClassIndexForInput(String vectorName) throws SOMLibFileFormatException {
        Object object = dataHash.get(vectorName);
        if (object instanceof Integer) {
            return ((Integer) object).intValue();
        } else {
            throw new SOMLibFileFormatException(
                    "Class information file corrupt. Error pairing input vectors with class names for vector: "
                            + vectorName);
        }
    }

    /**
     * Gets the number of input vectors in the given class.
     * 
     * @param classIndex the index of the class.
     * @return the total number of inputs in that class.
     */
    public int getNumberOfClassMembers(int classIndex) {
        return classMemberCount[classIndex];
    }

    public double getPercentageOfClassMembers(int classIndex) {
        return classMemberCount[classIndex] / (double) numData;
    }

    public String[] getDataNamesInClass(String className) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < numData; i++) {
            if (getClassName(i).equals(className)) {
                result.add(dataNames[i]);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /** computes the percentages of class membership for the given label names */
    public int[] computeClassDistribution(String[] labelNames) {
        int[] values = new int[numClasses()];
        for (int v = 0; v < values.length; v++) {
            values[v] = 0;
        }
        if (labelNames != null) {
            for (String labelName : labelNames) {
                int ci = getClassIndex(labelName);
                if (ci < 0) {
                    System.err.println("ERROR: Class index could not be retrieved for item " + labelName);
                } else {
                    values[ci] += 1;
                }
            }
        }
        return values;
    }

    /** Initialise a standard paint list */
    private void initPaintList() {
        paintList = new PaintList();
        DrawingSupplier supplier = new DefaultDrawingSupplier();
        for (int i = 0; i < numClasses(); i++) {
            paintList.setPaint(i, supplier.getNextPaint());
        }
    }

    /** Get the class colours as {@link PaintList}. */
    public PaintList getPaintList() {
        return paintList;
    }

    /** Get all class colours. */
    public Color[] getClassColors() {
        Color[] res = new Color[paintList.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = (Color) paintList.getPaint(i);
        }
        return res;
    }

    /** Get the colour for the given class index. */
    public Color getClassColor(int index) {
        return (Color) paintList.getPaint(index);
    }

    /** Get the colour for the given class index. */
    public void setClassColor(int index, Color color) {
        paintList.setPaint(index, color);
    }

    /** Load colours from an external (non-classinfo) file. */
    public boolean loadClassColours(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String strcols[];
            int r, g, b;
            int i = 0;
            boolean done = false;

            while ((line = br.readLine()) != null & !done) {
                if (i >= numClasses()) {
                    logger.warning("Color file contains more colors than needed in current class visualization. Skipping colors.");
                    done = true;
                } else {
                    strcols = line.split(" ");
                    if (strcols.length != 3) {
                        logger.severe("Color file: Error in line '" + line
                                + "' - did not find 3 int color parts (RGB)!");
                        br.close();
                        return false;
                    }
                    r = Integer.parseInt(strcols[0]);
                    g = Integer.parseInt(strcols[1]);
                    b = Integer.parseInt(strcols[2]);

                    // set new color
                    paintList.setPaint(i, new Color(r, g, b));

                    i++;
                }
            }

            br.close();

            if (i < numClasses()) {
                logger.warning("Color file contained less colors than needed in current class visualization. Keeping some old colors.");
            }
            logger.info("Successfully loaded " + i + " class colours from file '" + file.getAbsolutePath() + "'.");
        } catch (Exception ex) {
            logger.severe("Could not read color file" + file.toString() + "! " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public void removeNotPresentElements(SOMLibSparseInputData inputData) {
        int[] classMemberCountTemp = new int[numClasses];
        LinkedHashMap<String, Comparable> newData = new LinkedHashMap<String, Comparable>();
        StdErrProgressWriter progress = new StdErrProgressWriter(dataNames.length, "Checking class info for item ", 10);
        for (int i = 0; i < dataNames.length; i++) {
            if (inputData.getInputDatum(dataNames[i]) != null) {
                // keep the data
                newData.put(dataNames[i], dataClasses[i]);
                classMemberCountTemp[dataClasses[i]]++;
            }
            progress.progress();
        }
        System.out.println("Original class info size: " + dataNames.length);
        System.out.println("Reduced class info size: " + newData.size());
        System.out.println("Input data size: " + inputData.numVectors());
        System.out.println("The new class distribution:");
        for (int i = 0; i < classMemberCountTemp.length; i++) {
            System.out.println(" " + i + "\t" + classNames[i] + "\t: " + classMemberCountTemp[i]);
        }
        // FIXME: we should also check if there are some classes that are not present any more!
        classMemberCount = classMemberCountTemp;
        dataNames = newData.keySet().toArray(new String[newData.size()]);
        dataClasses = new int[newData.size()];
        for (int i = 0; i < dataClasses.length; i++) {
            dataClasses[i] = (Integer) newData.get(i);
        }
        numData = newData.size();
        dataHash = newData;

        for (String element : inputData.dataNames) {
            if (dataHash.get(element) == null) {
                System.out.println("Could not find class for input '" + element + "'.");
            }
        }
    }

    /** Method for stand-alone execution to convert a file to the SOMLibClassInformation format. */
    public static void main(String[] args) throws SOMToolboxException, IOException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptClassInformationFile(true),
                OptionFactory.getOptOutputFileName(true));
        InputDataWriter.writeAsSOMLib(new SOMLibClassInformation(config.getString("classInformationFile")),
                config.getString("output"));
    }
}
