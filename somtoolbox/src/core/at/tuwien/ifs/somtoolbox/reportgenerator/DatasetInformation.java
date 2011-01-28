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
package at.tuwien.ifs.somtoolbox.reportgenerator;

import java.awt.Color;
import java.io.File;
import java.util.Vector;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.util.PCA;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterLabel;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterNode;

/**
 * FIXME: most probably all the methods in this class should be part of {@link InputData} and
 * {@link SOMLibClassInformation}, respectively ! <br>
 * this class collects all available information about the values in the input dataset, like from the input file, the
 * template vector file, ... and maybe computes some properties of its own. It's job is to give one centralized placed
 * where the actual report generators (the output object) can ask for the data.
 * 
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: DatasetInformation.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class DatasetInformation {

    public static final int MIN_VALUE = 1;

    public static final int MAX_VALUE = 2;

    public static final int MEAN_VALUE = 3;

    public static final int VAR_VALUE = 4;

    public static final int ZERO_VALUE = 5;

    public static final int ONLY01 = 6;

    public static final int DISCRETE = 7;

    private Vector<Integer> selectedIndices;

    private InputData inputData;

    private String inputDataFilename;

    private String tvFilename;

    private TemplateVector inputTemplate;

    private SOMLibClassInformation classInfo;

    private String[] classNames = null;

    private String classInformationFilename = null;

    private EditableReportProperties EP;

    /* variables containing information about the data distribution */

    /** we check whether there are values != 0 or 1 */
    private boolean only01[] = null;

    /** only an estimation - we call values discrete if they are integer values */
    private boolean discrete[] = null;

    /** holds for each dimension the minimal value */
    private double[] min = null;

    /** holds for each dimension the maximal value */
    private double[] max = null;

    /** holds for each dimension the mean value */
    private double[] mean = null;

    /** holds for each dimension the variance */
    private double[] var = null;

    /** holds for each dimension the number of 0 - values. Using this we estimate the missing values */
    private int zeroValues[] = null;

    boolean denseData = true;

    // Input Data herrichten
    /**
     * creates a new object storing information about a given dataset
     * 
     * @param selectedIndices Vector of indices of the input items selected for more information
     * @param inputDataFilename the path to the file containing the input data
     * @param tvFilename the path to the file containin the template vector
     * @param classInformationFile the path to the file containing the class information
     * @param EP the customized Report Features of the Semantic Report
     */
    public DatasetInformation(Vector<Integer> selectedIndices, String inputDataFilename, String tvFilename,
            String classInformationFile, EditableReportProperties EP) {
        this(selectedIndices, inputDataFilename, tvFilename, classInformationFile, EP, new CommonSOMViewerStateData());
    }

    public DatasetInformation(Vector<Integer> selectedIndices, String inputDataFilename, String tvFilename,
            String classInformationFile, EditableReportProperties EP, CommonSOMViewerStateData state) {

        // check what files we already have in the state, and try to load the missing ones
        if (state.inputDataObjects.getInputData() == null) { // need to load the input data
            state.inputDataObjects.setFileName(SOMVisualisationData.INPUT_VECTOR, inputDataFilename);
            state.inputDataObjects.readAvailableData();
        }
        this.inputData = state.inputDataObjects.getInputData();

        if (state.inputDataObjects.getClassInfo() == null) {
            if (new File(classInformationFile).exists()) {
                this.classInformationFilename = classInformationFile;
                state.inputDataObjects.setFileName(SOMVisualisationData.CLASS_INFO, classInformationFile);
                state.inputDataObjects.readAvailableData();
                if (state.inputDataObjects.getClassInfo() == null) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                            "Could not read class information file from '" + classInformationFile
                                    + "' - generating report without class information.");
                }
                this.classInfo = state.inputDataObjects.getClassInfo();
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                        "No class information file specified - generating report without class information.");
            }
        } else {
            classInfo = state.inputDataObjects.getClassInfo();
        }
        this.EP = EP;
        this.tvFilename = tvFilename;
        this.selectedIndices = selectedIndices;
        this.inputTemplate = this.inputData.templateVector();
        this.inputDataFilename = inputDataFilename;
    }

    /**
     * returns whether class information are attached to the input vectors does not check whether it is a valid file,
     * only whether a String with length > 0 has been specified as path
     * 
     * @return true if a class information file (.cls) has been specified, false otherwise
     */
    public boolean classInfoAvailable() {
        return classInfo != null;
    }

    public SOMLibClassInformation getClassInfo() {
        return classInfo;
    }

    /**
     * returns the number of input vectors used for training the SOM, that is the number of different vectors present in
     * the input file for the SOM training.
     * 
     * @return the number of input vectors that appear in the input file
     */
    public int getNumberOfInputVectors() {
        return inputData.numVectors();
    }

    /**
     * returns the mean vector of all input items belonging to the given class
     * 
     * @param classId the id of the class for which the mean vector shall be calculated
     * @return the mean vector of the class
     */
    public double[] getClassMeanVector(int classId) {

        double[] mean = new double[this.getVectorDim()];
        int n = this.classInfo.getNumberOfClassMembers(classId);
        for (int i = 0; i < mean.length; i++) {
            mean[i] = 0;
        }
        String[] ins = this.classInfo.getDataNamesInClass(this.getNameOfClass(classId));

        for (String in : ins) {
            double[] temp = this.inputData.getInputDatum(in).getVector().toArray();
            for (int c = 0; c < temp.length; c++) {
                mean[c] += temp[c] / n;
            }
        }
        return mean;
    }

    /**
     * returns the dimension of the input vectors, that is the same as the number of attributes used to describe the
     * objects.
     * 
     * @return the dimension of the input vectors
     */
    public int getVectorDim() {
        return inputData.dim();
    }

    /**
     * returns whether the values in the given dimension are all only 0 or 1
     * 
     * @param index the dimension (starting with 0) for which this property is requested
     * @return true if all input vectors contain only 0 or 1 in this dimension, false otherwise
     */
    public boolean is01(int index) {
        if (this.only01 == null) {
            this.checkDatatypes();
        }
        if (index > this.only01.length - 1) {
            return false;
        }

        return this.only01[index];
    }

    /**
     * returns whether our heuristic estimates this dimension to contain discrete values This is the case, if all values
     * in this dimension are exact integer values.
     * 
     * @param index the dimension (starting with 0) for which the estimation is requested
     * @return true if all input vectors have only plain integers as values in this dimension, false otherwise
     */
    public boolean isDiscrete(int index) {
        if (this.discrete == null) {
            this.checkDatatypes();
        }
        if (index > this.discrete.length - 1) {
            return false;
        }

        return this.discrete[index];
    }

    /**
     * returns the number of input vectors that have 0 as value in the given dimension
     * 
     * @param index the dimension (starting with 0) for which the number is requested
     * @return the number of input vectors having the value 0 in the given dimension
     */
    public int getNumberOfZeroValues(int index) {
        if (this.zeroValues == null) {
            this.checkDatatypes();
        }
        if (index > this.zeroValues.length - 1) {
            return -1;
        }

        return this.zeroValues[index];
    }

    /**
     * returns whether the input set has been normalized (in fact, this functions returns the result of
     * InputData.isNormalizedToUnitLength())
     * 
     * @return true if data iset is normalized, false if not
     */
    public boolean isNormalized() {
        return this.inputData.isNormalizedToUnitLength();
    }

    /**
     * FIXME: split this into simple single getter methods... !<br>
     * returns the requested value describing the distribution of the input values. The types of information available
     * are described by the constant members of this class (this function returns numerical properties):
     * <ul>
     * <li>min value of all input vectors(MIN_VALUE)</li>
     * <li>max value of all input vectors(MAX_VALUE)</li>
     * <li>mean value of all input vecotrs(MEAN_VALUE)</li>
     * <li>variance of the values in the input vectors(VAR_VALUE)</li>
     * <li>the number of input vectors having 0 as value(ZERO_VALUE) (is in fact int, not double)</li>
     * </ul>
     * all information are returned for the given dimension (argument attribute).
     * 
     * @param type specifies the type of information to be returned: allowed are some constants defined by this class
     *            (see above)
     * @param attribute the index of the attribute for which the value shall be returned (starting with 0)
     * @return the requested value. if the requested type is not available, -1 is returned
     */
    public double getNumericalDataProps(int type, int attribute) {

        switch (type) {

            case MIN_VALUE:
                if (this.min == null) {
                    this.checkDatatypes();
                }
                return this.min[attribute];

            case MAX_VALUE:
                if (this.max == null) {
                    this.checkDatatypes();
                }
                return this.max[attribute];

            case MEAN_VALUE:
                if (this.mean == null) {
                    this.checkDatatypes();
                }
                return this.mean[attribute];

            case VAR_VALUE:
                if (this.var == null) {
                    this.checkDatatypes();
                }
                return this.var[attribute];

            case ZERO_VALUE:
                if (this.zeroValues == null) {
                    this.checkDatatypes();
                }
                return this.zeroValues[attribute];

            default:
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                        "Requested unknown dataset distribution value: " + type
                                + " (in DatasetInformation.getNumericalDataProps())");
                return -1;
        }
    }

    /**
     * FIXME: split this into simple single getter methods... !<br>
     * returns the requested value describing the distribution of the input values. The types of information available
     * are described by the constant members of this class (this function returns boolean properties):
     * <ul>
     * <li>whether all values are plain integers(DISCRETE)</li>
     * <li>whether all values are either 0 or 1 (ONLY01)</li>
     * </ul>
     * all information are returned for the given dimension (argument attribute).
     * 
     * @param type specifies the type of information to be returned: allowed are some constants defined by this class
     *            (see above)
     * @param attribute the index of the attribute for which the value shall be returned (starting with 0)
     * @return the requested value. if the requested type is not available, -1 is returned
     */
    public boolean getBoolDataProps(int type, int attribute) {

        switch (type) {

            case DISCRETE:
                if (this.discrete == null) {
                    this.checkDatatypes();
                }
                return this.discrete[attribute];

            case ONLY01:
                if (this.only01 == null) {
                    this.checkDatatypes();
                }
                return this.only01[attribute];

            default:
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                        "Requested unknown dataset distribution value: " + type
                                + " (in DatasetInformation.getBoolDataProps())");
                return false;
        }

    }

    /**
     * returns the label (that is the name defined for an attribute in the template vector file) for the specified
     * attribute. If no template file is given, only the index of the attribute is returned.
     * 
     * @param dim the index within the vector of the attribute whose label shall be returned
     * @return the label specified in the template vector file or (if not present) the index of the attribute
     */
    public String getAttributeLabel(int dim) {
        if (this.inputTemplate != null) {
            return inputTemplate.getLabel(dim);
        } else {
            return "" + dim;
        }
    }

    /**
     * returns the number of classes. If there are no class information are attached to data, -1 is returned.
     * 
     * @return the number of classes or -1
     */
    public int getNumberOfClasses() {
        if (this.classInfo == null) {
            return -1;
        } else {
            return this.classInfo.numClasses();
        }
    }

    /**
     * returns the name of the class specified by the index
     * 
     * @param c the index of the class (starting with 0)
     * @return the name of the class specified by the index, the empty String in case of any error finding the name
     */
    public String getNameOfClass(int c) {
        if (this.classInfo == null) {
            return "";
        }
        if (this.classNames == null) {
            this.classNames = this.classInfo.classNames();
        }
        if (this.classNames.length <= c) {
            return "";
        }
        return this.classNames[c];
    }

    /**
     * returns a list of labels of all input items belonging to the given class
     * 
     * @param classId the id of the class for which the input items are requested
     * @return a list containing the lables of the input items belonging to this class
     */
    public String[] getInputLabelsofClass(int classId) {
        return this.classInfo.getDataNamesInClass(this.getNameOfClass(classId));
    }

    /**
     * returns an array of length three containing the r,g,b values of the colour used to colour the specified class
     * 
     * @param c the index of the class for which the colour is requested
     * @return an array containing the r, g and b definitions of a color
     */
    public int[] getClassColorRGB(int c) {
        int[] rgb = new int[3];

        Color[] colors = this.classInfo.getClassColors();
        if (colors.length > c) {
            rgb[0] = colors[c].getRed();
            rgb[1] = colors[c].getGreen();
            rgb[2] = colors[c].getBlue();
        } else {
            rgb[0] = 255;
            rgb[1] = 255;
            rgb[2] = 255;
        }

        return rgb;
    }

    /**
     * returns the number of input elements belonging to the given class if no class information is attached to this
     * input, -1 is returned
     * 
     * @param c the index of the class (starting with 0)
     * @return the number of elements belonging to this class, or -1
     */
    public int getNumberOfClassmembers(int c) {
        if (this.classInfo == null) {
            return -1;
        } else {
            return this.classInfo.getNumberOfClassMembers(c);
        }
    }

    /** returns the index of the class the input vector specified by its index belongs to */
    public int getClassIndexOfInput(String inputLabel) {
        return this.classInfo.getClassIndex(inputLabel);
    }

    /**
     * returns the path of the file containin the class information
     * 
     * @return path to the file containting the class information
     */
    public String getClassInformationFilename() {
        return this.classInformationFilename;
    }

    /**
     * runs over all dimensions of the input vectors and tries to fetch some information about their data ranges and
     * other properties information gathered are:
     * <ul>
     * <li>min and max value within each dimension (this.min, this.max)</li>
     * <li>does a dimension contain only 0/1 values (this.only01)</li>
     * <li>does a dimension contain only plain integer values (this.discrete)</li>
     * <li>how many 0 (=missing?) values are in each dimension (this.zeroValues)</li>
     * </ul>
     * the results are stored in the appropriate arrays
     */
    private void checkDatatypes() {

        int d = this.getVectorDim(); // dimension of input vectors
        int n = this.getNumberOfInputVectors(); // the numbers of vectors
        double temp;

        this.only01 = new boolean[d];
        this.discrete = new boolean[d];
        this.min = new double[d];
        this.max = new double[d];
        this.mean = new double[d];
        this.var = new double[d];
        this.zeroValues = new int[d];

        // check all dimensions
        for (int i = 0; i < d; i++) {
            // initialization of the arrays for this dimension
            this.only01[i] = true;
            this.discrete[i] = true;
            this.min[i] = this.inputData.getInputDatum(0).getVector().get(i);
            this.max[i] = this.inputData.getInputDatum(0).getVector().get(i);
            this.mean[i] = 0;
            this.mean[i] = 0;
            this.zeroValues[i] = 0;

            // and now for each input vector in this dimension
            for (int j = 0; j < n; j++) {

                temp = this.inputData.getInputDatum(j).getVector().get(i); // retrieve the current value

                // discrete or 01?
                if ((int) temp != temp && this.discrete[i]) {
                    // not even discrete value
                    this.only01[i] = false;
                    this.discrete[i] = false;
                } else if ((double) (int) temp % 2 != temp && this.only01[i]) {
                    // no 01 value
                    this.only01[i] = false;
                }

                // min/max values:
                if (temp < this.min[i]) {
                    this.min[i] = temp;
                }
                if (temp > this.max[i]) {
                    this.max[i] = temp;
                }

                // mean value:
                this.mean[i] += temp / n;

                // zero Values
                if (temp == 0) {
                    this.zeroValues[i]++;
                }
            }

            // now that we have the mean value for this dimension, we can calculate the variance
            for (int j = 0; j < n; j++) {
                temp = this.inputData.getInputDatum(j).getVector().get(i); // retrieve the current value
                this.var[i] += (temp - this.mean[i]) * (temp - this.mean[i]) / (n - 1);
            }
        }
    }

    /**
     * returns the InputData object storing information about the input data used for training the som. Needed by
     * objects of type TestRunResult for some analysis
     * 
     * @return the input data used to train the SOM
     */
    public InputData getInputData() {
        return this.inputData;
    }

    /** returns the InputDatum labelled with the specified name */
    public InputDatum getInputDatum(String name) {
        return this.inputData.getInputDatum(name);
    }

    /** returns the InputDatum at the specified index */
    public InputDatum getInputDatum(int d) {
        return this.inputData.getInputDatum(d);
    }

    /**
     * returns the number of inputs the user has selected to get information about their position on the SOM
     * 
     * @return the number of inputs selected by the user.
     */
    public int getNumberOfSelectedInputs() {
        return this.selectedIndices.size();
    }

    /**
     * returns the id of the inputVector at position index in the list of selected inputs each input vector is
     * identified by an id, which is its index in the complete input. The vectors selected by the user (to display their
     * position on the SOM) are also stored in a list. To retrieve the "real" id of the vector at position index in this
     * list, this function should be used
     * 
     * @param index the index of the vector in the list of selected inputs
     * @return the id of the corresponding input, that is the index in the complete input list, -1 if error
     */
    public int getSelectedInputId(int index) {
        if (index >= this.selectedIndices.size()) {
            return -1;
        }
        return this.selectedIndices.get(index).intValue();
    }

    /**
     * returns the complete filename of the file containing the input data complete filename means including the path.
     * The string is not verified to point to a valid input file (or any file at all).
     * 
     * @return the complete filename (including absolute path) of the input filename
     */
    public String getInputDataFilename() {
        return this.inputDataFilename;
    }

    /**
     * returns the complete filename of the file containing the template data complete filename means including the
     * path. The string is not verified to point to a valid template file (or any file at all).
     * 
     * @return the complete filename (including absolute path) of the template filename
     */
    public String getTemplateFilename() {
        return this.tvFilename;
    }

    /**
     * Tries to name a cluster by the input data mapped to units lying within the cluster For naming the cluster, some
     * very simple heuristics are used: First, if there are any labels of the clusters, which correpsond to 0/1
     * attributes, and their values are all 0 (or 1) in the cluster, the name of this attribute is included to the name
     * of the cluster. (attributes of 0/1 type are supposed to encode any "has this property" yes/no information,
     * thereby the value 1 is interpreted as "cluster has this property", whereas 0 is interpreted as "has not") If
     * there are any labels that don't correspond not 0/1 attributes, it is checked whether both subclusters have the
     * same value for this label. If yes, the name of this label is included to the name of the cluster If none of the
     * properties above is valid, the first nodeDepth-1 labels of the cluster suggested by the clustering algorithm is
     * used. (at least for the animal map this works quite well)
     * 
     * @param node the node representing the cluster tha shall be named
     * @param clusterByValue indicates whether the labels for the cluster shall be created by value (is handed unchanged
     *            to ClusterNode.getLabels(clusterByValue, boolen)
     * @param nodeDepth the depth of the node in the tree, whereby the root (i.e. the cluster containing the whole map)
     *            node has depth 1
     * @return the list of labels found for this cluster
     */
    public Vector<String> getClusterName(ClusterNode node, int clusterByValue, int nodeDepth) {

        Vector<String> texts = new Vector<String>();

        ClusterLabel[] labels = node.getLabels(clusterByValue, false);
        if (labels == null || labels.length == 0) {
            texts.add("no label found");
            return texts;
        }

        String curLabel;
        int[] inds = new int[labels.length];
        /*
         * Following idea: - if the data is only 0/1, we take all those where we have 0/1 as value, and call it has label and has not label -
         * otherwise we take all labels, that have the same value in both child nodes - we take at least one label for the cluster
         */

        boolean[] labeled = new boolean[labels.length];
        String[] createdLabels = new String[labels.length];
        for (int i = 0; i < labeled.length; i++) {
            labeled[i] = false;
        }

        // first check whether we have 0/1 values (indications for "has/has not" a given property
        for (int i = 0; i < labels.length; i++) {
            curLabel = labels[i].getName();
            inds[i] = -1;
            for (int j = 0; j < this.getVectorDim(); j++) {
                if (this.inputTemplate.getLabel(j).equals(curLabel)) {
                    inds[i] = j;
                    break;
                }
            }

            if (inds[i] >= 0 && this.is01(inds[i])) {
                if (labels[i].getValue() == 0) {
                    // yes - strike
                    curLabel = "has/is no/not " + curLabel;
                    labeled[i] = true;
                    createdLabels[i] = curLabel;
                } else if (labels[i].getValue() == 1) {
                    // ok - strike too
                    curLabel = "has/is " + curLabel;
                    labeled[i] = true;
                    createdLabels[i] = curLabel;
                }
            }
        }

        // then check for all labels left, whether the are the same as in both child nodes:
        ClusterNode child1 = node.getChild1();
        ClusterNode child2 = node.getChild2();

        ClusterLabel[] labels1 = child1.getLabels(clusterByValue, false);
        ClusterLabel[] labels2 = child2.getLabels(clusterByValue, false);

        for (int i = 0; i < labeled.length; i++) {
            if (labeled[i]) {
                continue; // already done
            }
            if (inds[i] > 0 && is01(inds[i])) {
                continue; // already done
            }
            if (labels1.length < i + 1 || labels2.length < i + 1) {
                break;
            }

            if (labels[i].getValue() == labels1[i].getValue() && labels[i].getValue() == labels2[i].getValue()) {
                curLabel = labels[i].getName() + " = " + String.format("%.6f", labels[i].getValue());
                labeled[i] = true;
                createdLabels[i] = curLabel;
            }
        }

        for (int i = 0; i < createdLabels.length; i++) {
            if (labeled[i]) {
                texts.add(createdLabels[i]);
            }
        }

        if (texts.size() == 0) {
            /*
             * we need some labels. the question is: which and how many. The idea is to stick to the labeling algorithm provided by the toolbox, and
             * to simlpy pick the first k ones. We make k dependent from the depth of the node, by the idea that ideally each new cluster introduced a
             * new dimension.
             */
            for (int i = 0; i < nodeDepth - 1 && i < labels.length; i++) {
                texts.add(labels[i].getName() + " = " + String.format("%.6f", labels[i].getValue()));
            }

        }
        return texts;
    }

    /**
     * This method calculates the most important Dimensions of the Dataset according to the results of a PCA, and rows
     * the resulting dim-index in a new array on first index. On index 2, the corresponding % of the TotalVariance is
     * calculated (as a quality measure)
     * 
     * @return new array with most important dims ranked decreasingly.
     */

    public double[][] getPCAdeterminedDims() {
        double[][] result_array;
        if (this.EP == null) {// If selected Number >this.getVectorDim() or if no specified value was entered, take all
            // PCAComponents
            result_array = new double[this.getVectorDim()][2];
        } else {
            if (this.EP.getMetroMapComponents() > this.getVectorDim() || this.EP.getMetroMapComponents() < 0) {
                result_array = new double[this.getVectorDim()][2];
            } else {
                result_array = new double[this.EP.getMetroMapComponents()][2];
            }
        }
        double[][] data = inputData.getData();
        PCA pca = new PCA(data);
        double BestAxisVar = Double.MAX_VALUE;
        double CurrBestAxisVar = Double.MIN_VALUE;

        int CurrBestAxisIndex = -1;
        int counter = 0;
        double temp = 0.0;

        while (counter != result_array.length) { /* Loop it result array length times and get the best Eigenvalue. */
            CurrBestAxisVar = Double.MIN_VALUE;
            for (int curAxis = 0; curAxis < this.getVectorDim(); curAxis++) {
                if (pca.info[curAxis] > CurrBestAxisVar && pca.info[curAxis] < BestAxisVar) {
                    CurrBestAxisVar = pca.info[curAxis];
                    CurrBestAxisIndex = curAxis;
                }
            }
            temp = pca.info[CurrBestAxisIndex] / this.getVectorDim();
            BestAxisVar = CurrBestAxisVar;
            result_array[counter][0] = CurrBestAxisIndex; /* save the best Dim index on index 0 */
            result_array[counter][1] = temp; /* and its corresponding Variance Value on index 1 */
            counter++;
        }
        return result_array;
    }

    /**
     * this method is just a small helper method, used to display the Dimensions in the top-part of the output document
     * It accumulates the Variances and calculates this Percentage from the total Variance
     */
    public double calculateAccumulatedVariance() {
        double perc = 0.0;
        double[][] array = this.getPCAdeterminedDims();
        for (double[] element : array) {
            perc += element[1];
        }
        return perc;
    }

    /** Returns the names of the 3 files, used for training */
    public String[] getTrainingDataInfo() {
        String[] list = new String[3];
        list[0] = new String(applyNameFix(this.inputDataFilename));
        list[1] = new String(applyNameFix(this.tvFilename));
        if (this.classInformationFilename == null) {
            list[2] = "no class information file";
        } else {
            list[2] = new String(applyNameFix(this.classInformationFilename));
        }
        ;

        return list;
    }

    /** small helper method for getTrainingDataInfo */
    private static String applyNameFix(String target) {
        int c1 = target.lastIndexOf(System.getProperty("file.separator"), target.length());
        return target.substring(c1 + 1, target.length());
    }

    /** Returns the Editable Report Properties for the Semantic Report */
    public EditableReportProperties getEP() {
        return this.EP;
    }

}
