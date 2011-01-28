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
package at.tuwien.ifs.somtoolbox.reportgenerator.output;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.io.filefilter.PrefixFileFilter;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResultCollection;
import at.tuwien.ifs.somtoolbox.reportgenerator.TextualDescriptionProvider;
import at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers.InputQEContainer;
import at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers.UnitQEContainer;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterNode;

/**
 * this class is the base class for generating the part of the reports that describes a SOM. it creates output
 * containing information about basic properties of the learning process and the created SOM, as well as about the
 * distribution of the input data on this SOM. This class implements the description of a standard GrowingSOM, and is
 * subtyped for creating proper description of other SOMs.
 * 
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @version $Id: SOMDescriptionLATEX.java 3914 2010-11-04 14:28:18Z mayer $
 */
public class SOMDescriptionLATEX {

    /** the name of the directory where the image shall be saved - relative to the given baseDir path in the constructor */
    public static final String imgSubdir = "images";

    /** all strings that shall be in the output are sent to this object */
    protected ReportFileWriter writer;

    /** encapsulates all available information about the testrun - just ask */
    protected TestRunResult testrun;

    /** contains all available information about the input data */
    protected DatasetInformation dataset;

    /** the directory to which the created images shall be saved */
    protected String imgDir;

    /**
     * creates a new instance
     * 
     * @param writer object that handles how the created string is written to a file
     * @param dataset object storing information about input data
     * @param testrun object storing information about testrun results
     * @param baseDir path to the directory where created images shall be stored
     */
    public SOMDescriptionLATEX(ReportFileWriter writer, DatasetInformation dataset, TestRunResult testrun,
            String baseDir) {

        this.imgDir = baseDir;
        if (!this.imgDir.endsWith(System.getProperty("file.separator"))) {
            this.imgDir += System.getProperty("file.separator");
        }
        this.imgDir += SOMDescriptionLATEX.imgSubdir + System.getProperty("file.separator");
        File dir = new File(imgDir);
        if (dir.exists() && !dir.isDirectory()) {
            dir.delete();
        }
        if (!dir.exists()) {
            dir.mkdir();
        }
        this.writer = writer;
        this.dataset = dataset;
        this.testrun = testrun;
    }

    /**
     * initiates the creation of the output Creates the description of the SOM and training properties
     */
    public void printSOMDescription() {

        // SOM & training properties
        this.writer.appendLatexOutput("\\begin{itemize}\n");
        this.printSOMProperties();
        this.writer.appendLatexOutput("\\end{itemize}");

        // data distribution on SOM
        this.printDataDistribution();
    }

    /**
     * prints a list of properties describing the training process and the generated SOM this list contains besides
     * others: \\begin{itemize} \\item type and topology of SOM \\item dimensions of the som \\item different training
     * parameters \\item neighbourhood function \\item ... \\end{itemize}
     */
    protected void printSOMProperties() {

        this.printTopologyOfSOM();
        this.printSOMDimensions();
        this.printSigma();
        this.printTau();
        this.printLearningRate();
        this.printNeighbourhoodFunction();
        this.printMetricUsed();
        this.printNumberOfIterations();
        this.printRandomSeed();
        this.printTrainingTime();
        this.printTrainingDate();

    }

    /**
     * Prints the topology and type of the SOM (unit shape, type of SOM, ...) the value is dependent of the value
     * keyTopoology specified by the TestRunResult Object
     */
    protected void printTopologyOfSOM() {
        String top = (String) this.testrun.getMapProperty(TestRunResultCollection.keyTopology);
        if (top.equals("rect")) {
            this.writer.appendLatexOutput("\\item Type of SOM: GrowingSOM with rectangle units\n");
        } else if (top.equals("hex")) {
            this.writer.appendLatexOutput("\\item GrowingSOM with hexagonal units\n");
        } else if (top.equals("hfm")) {
            this.writer.appendLatexOutput("\\item Hierarchical Feature Map\n");
        } else if (top.equals("gcs")) {
            this.writer.appendLatexOutput("\\item Growing Cell Structures\n");
        } else {
            this.writer.appendLatexOutput("\\item Topology: " + top + "\n");
        }
    }

    /**
     * Prints the dimension of the SOM, that is the number of units in x and y direction (for Growing SOM this is
     * enough) (taken from the MySOMMapDescription, provided by the TestRunResult object)
     */
    protected void printSOMDimensions() {
        String xunits = (String) this.testrun.getMapProperty(TestRunResultCollection.keyXDim);
        String yunits = (String) this.testrun.getMapProperty(TestRunResultCollection.keyYDim);
        String dim = "unknown";
        if (xunits != null && yunits != null) {
            dim = xunits + " x " + yunits + " units";
        }
        this.writer.appendLatexOutput("\\item Dimensions of the SOM: " + dim + "\n");
    }

    /**
     * prints the value of the sigma, as one of the learning parameters (taken from the SOMProperty object)
     */
    protected void printSigma() {

        String sigma = "Property file not defined";
        if (this.testrun.getSigma() >= 0) {
            sigma = "" + this.testrun.getSigma();
        }
        this.writer.appendLatexOutput("\\item Neighbourhood radius (sigma): " + sigma + "\n");
    }

    /**
     * prints the value of the tau, as one of the learning parameters (taken from the SOMProperty object)
     */
    protected void printTau() {
        String tau = "Property file not defined";
        if (this.testrun.getTau() >= 0) {
            tau = "" + this.testrun.getTau();
        }
        this.writer.appendLatexOutput("\\item Desired data representation granularity (tau): " + tau + "\n");
    }

    /**
     * prints the metric used to calculate the distance between two vectors (taken from the MySOMMapDescription,
     * provided by the TestRunResult object)
     */
    protected void printMetricUsed() {
        String metric = (String) this.testrun.getMapProperty(TestRunResultCollection.keyMetric);
        if (metric == null || metric.length() <= 0) {
            metric = "not recorded";
        } else {
            if (metric.startsWith("at.tuwien.ifs.somtoolbox.layers.metrics.")) {
                metric = metric.substring(33);
            }
        }
        this.writer.appendLatexOutput("\\item Distance metric used: " + metric + "\n");
    }

    /**
     * prints the number of iterations used in the training process (taken from the MySOMMapDescription, provided by the
     * TestRunResult object)
     */
    protected void printNumberOfIterations() {
        String its = (String) this.testrun.getMapProperty(TestRunResultCollection.keyTotalIterations);
        if (its == null || its.length() <= 0) {
            its = "not recorded";
        }
        this.writer.appendLatexOutput("\\item Iterations made during training: " + its + "\n");

    }

    /**
     * if available, prints the time and date of the training (taken from the MySOMMapDescription, provided by the
     * TestRunResult object)
     */
    protected void printTrainingDate() {
        String trainingDate = (String) this.testrun.getMapProperty(TestRunResultCollection.keyStorageDate);
        if (trainingDate == null || trainingDate.length() <= 0) {
            trainingDate = "not recorded";
        }
        this.writer.appendLatexOutput("\\item Map has been trained on: " + trainingDate + "\n");
    }

    /**
     * if available, prints the time the training of the SOM needed (taken from the MySOMMapDescription, provided by the
     * TestRunResult object)
     */
    protected void printTrainingTime() {

        String trainingTime = (String) this.testrun.getMapProperty(TestRunResultCollection.keyTrainingTime);
        if (trainingTime == null || trainingTime.length() <= 0) {
            trainingTime = "not recorded";
        } else {
            trainingTime += " (seconds)";
        }
        this.writer.appendLatexOutput("\\item Time required for training: " + trainingTime + "\n");
    }

    /**
     * prints the random seed used for the initialization of the SOM (taken from the MySOMMapDescription, provided by
     * the TestRunResult object)
     */
    protected void printRandomSeed() {
        String randomSeed = (String) this.testrun.getMapProperty(TestRunResultCollection.keyRandomInit);
        if (randomSeed == null || randomSeed.length() <= 0) {
            randomSeed = "Could not detect initial random seed";
        }
        this.writer.appendLatexOutput("\\item Value for initializing random number generator: " + randomSeed + "\n");
    }

    /**
     * Prints the neighbourhood type and the initial neighbourhood range used for training (taken from the
     * MySOMMapDescription, provided by the TestRunResult object)
     */
    protected void printNeighbourhoodFunction() {

        String neighbourhoodType = (String) this.testrun.getMapProperty(TestRunResultCollection.keyNeighbourhoodType);
        String neighbourhoodInit = (String) this.testrun.getMapProperty(TestRunResultCollection.keyNeighbourhoodInit);

        if (neighbourhoodType != null && neighbourhoodType.length() > 0) {
            this.writer.appendLatexOutput("\\item Type Neighbourhood: " + neighbourhoodType + "\n");
        }
        if (neighbourhoodInit == null || neighbourhoodInit.length() <= 0) {
            neighbourhoodInit = "Could not detect initial neighbourhood range";
        }
        this.writer.appendLatexOutput("\\item Initial Neighobourhood range: " + neighbourhoodInit + "\n");
    }

    /**
     * prints the type of how the learning rate changes, and the initial learning rate (taken from the
     * MySOMMapDescription, provided by the TestRunResult object)
     */
    protected void printLearningRate() {

        String learntype = (String) this.testrun.getMapProperty(TestRunResultCollection.keyLearnrateType);
        String learnrate = (String) this.testrun.getMapProperty(TestRunResultCollection.keyLearnRateInit);

        if (learntype != null && learntype.length() > 0) {
            this.writer.appendLatexOutput("\\item Type of Learning: " + learntype + "\n");
        }
        if (learnrate == null || learnrate.length() <= 0) {
            learnrate = "Could not detect learning rate";
        }
        this.writer.appendLatexOutput("\\item Initial learning rate: " + learnrate + "\n");
    }

    public void printDataDistribution() {

        this.writer.appendLatexOutput("\\subsection{Distribution of the input data on the trained SOM:}\n");

        if (this.dataset.classInfoAvailable()) {
            // class information given
            this.testrun.createPieChartImages(this.imgDir);
        } else {
            // no class information given
            this.writer.appendLatexOutput("\\paragraph{There is no class information attached to this input}");
        }

        // represent the SOM as HTML table - maybe as two, if input were selected

        this.printMapLayout(this.dataset.classInfoAvailable());

        // say something about the class purity
        if (this.dataset.classInfoAvailable()) {
            this.writer.appendLatexOutput("\\subsection{Class mix on the units:}\n");
            this.writer.appendLatexOutput("In addition to the tables with the class pie-charts, the following list gives an overview "
                    + "about whether the classes are separated rather good or bad on the SOM. (The right column states how many units exist that contain "
                    + "the number of units given in the left column. Entries with 0 units matching are omitted)\\\\");
            int[] purity = this.testrun.getClassPurity1();
            this.writer.appendLatexOutput("\\begin{tabular}{cc}" + "# of classes&\\\n"
                    + "# of units with this # of classes\\\\\n");
            for (int i = 0; i < purity.length; i++) {
                if (purity[i] == 0) {
                    continue;
                }
                this.writer.appendLatexOutput(i + "&\n");
                this.writer.appendLatexOutput(purity[i] + "\\\\\n");
            }
            this.writer.appendLatexOutput("\\end{tabular}\\\\\n");

            // now we give an estimation how good the SOM seperates the classes
            this.writer.appendLatexOutput(this.testrun.getClassPurity2(purity));

            // try to explain where the classes are located, how strong they are splitted ...
            // Create Images...
            this.testrun.createClassDistributionImage(this.imgDir + System.getProperty("file.separator"),
                    "classDistribution.jpg", -1);
            this.testrun.createEntropyImage(this.imgDir + System.getProperty("file.separator"), "classEntropy.jpg", -1);
            for (int i = 0; i < this.dataset.getNumberOfClasses(); i++) {
                this.testrun.createEntropyImage(this.imgDir + System.getProperty("file.separator"), "classEntropy" + i
                        + ".jpg", i);
                this.testrun.createClassDistributionImage(this.imgDir + System.getProperty("file.separator"),
                        "classDistribution" + i + ".jpg", i);
            }

            this.writer.appendLatexOutput("\\subsection{Class locations and Entropy}");
            this.writer.appendLatexOutput("The following list tries to give some more information about where the different classes are located on the SOM. "
                    + "Therefore, the second column contains the weight values the mean vector of all input items within the class would have. The next column "
                    + "states the unit onto which this vector would be mapped. This unit is marked in the first image with the circle. The next column contains the "
                    + "coordinates of the \"mean unit\" of the class. This is the unit one obtains not by calculcating the mean value over the weight vectors of the "
                    + "input items, but over the coordinates of the units they are mapped to. This unit is denoted by a square in the first image. The last "
                    + "column before the images gives the standard derivaton to the \"mean unit\" (in units). This value "
                    + "is used to describe an area within the majority of input items belonging to this class are mapped (visualized by the shape of a rectangle in the first image)."
                    + "The first image in the next column contains a visualization of the described values, (Note that this visualisation has nothing to do with the trained SOM), whereas the second image shows the real class distribution on the SOM for comparison reasons."
                    + "The third image displays the Visualisation of the Class Entropy. Images 3 \\& 4 are also displayed seperatly for each class."
                    + " The Class with the largest Entropy Error is "
                    + this.dataset.getNameOfClass(this.testrun.getMaximumEEClassIndex())
                    + ", having an Entropy Error of "
                    + this.testrun.getClassEntropy(this.testrun.getMaximumEEClassIndex())
                    + "."
                    + " The Region with the largest Entropy  Error contains the classes ");
            for (int i = 0; i < this.testrun.getMaxEErrorRegionNames().length; i++) {
                this.writer.appendOutput(this.testrun.getMaxEErrorRegionNames()[i][0] + "  ("
                        + this.testrun.getMaxEErrorRegionNames()[i][1] + ") , ");
            }
            this.writer.appendOutput("having an Entropy Error of " + this.testrun.getRegionEntropyError(1) + ".  "
                    + String.format("%.3f", this.testrun.getPercOfZeroEntropyRegions())
                    + "\\% of the  regions have an Entropy error of 0.");

            this.writer.appendOutput("\n\\begin{longtable}{lcccccp{4cm}}\n"
                    + "class name&\n"
                    +
                    // "mean vector&\n" +
                    "mapped to unit&\n"
                    // + "\"mean unit\"&\n"
                    // + "radius&\n" +
                    + "Coordinates &\n" + "\\includegraphics[width=50pt]{" + SOMDescriptionLATEX.imgSubdir + "/run_"
                    + this.testrun.getRunId() + "_classDistribution.jpg}&\n" + "\\includegraphics[width=50pt]{"
                    + SOMDescriptionLATEX.imgSubdir + "/run_" + this.testrun.getRunId() + "_classEntropy.jpg}&\n"
                    + "Class Entropy&\n" + "Other classes\n" + " \\\\\n");
            for (int i = 0; i < this.dataset.getNumberOfClasses(); i++) {
                double[] mean = this.dataset.getClassMeanVector(i);
                // String meanS = "";
                // for(int c = 0; c < mean.length; c++) meanS += String.format("%.3f", mean[c])+"\\\\";
                int[] meanMapped = this.testrun.getMappedUnit(mean);
                int[] meanUnit = this.testrun.getClassMeanUnit(i);
                String coords = meanMapped[0] + "," + meanMapped[1];
                this.testrun.visualizeClassLayout(meanUnit, meanMapped, i, this.imgDir);
                this.writer.appendLatexOutput(this.dataset.getNameOfClass(i) + "&\n");
                // this.writer.appendLatexOutput(meanS+"&\n");
                this.writer.appendLatexOutput(coords + "&\n");
                // this.writer.appendLatexOutput(meanUnit[0] + "," + meanUnit[1] + "&\n");
                // this.writer.appendLatexOutput(meanUnit[2] + "," + meanUnit[3] + "&\n");
                this.writer.appendOutput("\\includegraphicstotab[width=50pt]{" + SOMDescriptionLATEX.imgSubdir
                        + "/run_" + this.testrun.getRunId() + "_classCenter_" + i + ".jpg}&\n"
                        + "\\includegraphicstotab[width=50pt]{" + SOMDescriptionLATEX.imgSubdir + "/run_"
                        + this.testrun.getRunId() + "_classDistribution" + i + ".jpg}&\n");
                String[][] classmix = this.testrun.getClassMix(i);
                this.writer.appendOutput("\\includegraphicstotab[width=50pt]{" + SOMDescriptionLATEX.imgSubdir
                        + "/run_" + this.testrun.getRunId() + "_classEntropy" + i + ".jpg}&\n");
                this.writer.appendOutput(String.format("%.3f", this.testrun.getClassEntropy(i)) + "&\n");
                for (int j = 0; j < classmix.length; j++) {
                    this.writer.appendOutput(classmix[j][0]);
                    this.writer.appendOutput(" : ");
                    this.writer.appendOutput(classmix[j][1] + "\n");
                    if (j < classmix.length - 1) {
                        this.writer.appendOutput("\\newline\n");
                        // this.writer.appendOutput("\\\\\n");
                        // this.writer.appendOutput("&&&&&&");

                    }
                }
                this.writer.appendOutput("\n\\\\\n");
                this.writer.appendOutput("\\hline\n");
            }

            this.writer.appendLatexOutput("\\end{longtable}\n");
        }

        // the quantization errors on the map
        this.writer.appendLatexOutput("\\section{Quality Measures of the SOM}\n"
                + "The next section gives an overview over the Quality Measures of the SOM to describe its Goodness."
                + "Both Quantization Error aswell as the topographic Error arecalculated and visualized.");
        this.writer.appendLatexOutput("\\subsection{Quantization errors of the SOM:}\n");
        this.printQuantizationErrorReport();

        // the topographics errors on the map
        this.writer.appendLatexOutput("\\subsection{Topographic errors of the SOM:}\n");
        this.printTopographicErrorReport();

        // cluster Information
        this.printClusterInfos();
    }

    /**
     * Creates output describing the layout of the created som this includes tables showing the distribution of the
     * input vectors of the som, as well as (if available) the distribution of the classes on the som. In addition, if
     * the user selected input items for getting their position on the trained SOM, this information is also created in
     * this function.
     * 
     * @param classInfoAvailable true if class information are available (and therefore a piechart should be inserted,
     *            false otherwise)
     */
    protected void printMapLayout(boolean classInfoAvailable) {

        // the information about the selected data items - listed
        InputDatum input;
        Unit unit;
        String key;
        Hashtable<String, Vector<InputDatum>> lookup = new Hashtable<String, Vector<InputDatum>>(); // there we store
        // for each unit the
        // input vectors
        // mapped to it
        Vector<InputDatum> mappedInputs;

        // --- the first table with number of input items and class distribution
        String clsinf = "";
        if (classInfoAvailable) {
            clsinf = " and class distribution";
        }
        this.writer.appendLatexOutput("\\paragraph{Distribution of the input vectors" + clsinf + ":}\n");
        this.writer.appendLatexOutput("Table 3 on page \\pageref{tab:pie} is the Visualisation of a Hit Histogram."
                + "It shows the amount of input units mapped to their corresponding units on the SOM.");
        if (classInfoAvailable) {
            this.writer.appendLatexOutput(" The Class Pie Chart Image gives an Overview on the amount of classes being mappewd to this unit."
                    + "White gaps signify empty spaces on the Map, therefore can be interpretetd as natural cluster boundaries.\\\\");
        }
        this.printDistributionDetailTable(null, classInfoAvailable);
        this.writer.appendOutput("A more sophisticated way to visualize the distributioun of classes is the SDH. "
                + TextualDescriptionProvider.getScientificDescription("sdh")
                + "Fgure 1,2 \\& 3 on page \\pageref{fig:sdh1} are the Visualisations SDH Images with increasing Values of \\textit{k}\n");

        int runID = this.testrun.getRunId();
        this.testrun.createSDHImages(this.imgDir + System.getProperty("file.separator"), "SDH_Visualization.jpg", 30,
                true, -1);
        this.testrun.createSDHImages(this.imgDir + System.getProperty("file.separator"), "SDH_Visualization.jpg", 30,
                true, -1);
        this.testrun.createSDHImages(this.imgDir + System.getProperty("file.separator"), "SDH_Visualization.jpg", 30,
                true, -1);
        this.writer.appendOutput("\\begin{center}\n" + "\\begin{figure}[htbp]\n");
        int dim = this.testrun.getSOMDimensions();
        for (int i = 1; i < dim; i = i + dim / 3) {
            this.writer.appendOutput("\\begin{minipage}[hbt]{4.5cm}\n" + "\\includegraphics[width=4.5cm]{"
                    + SOMDescriptionLATEX.imgSubdir + "/" + "SDH_pics_" + this.testrun.getRunId() + "/" + "s" + i + "_"
                    + this.testrun.getRunId() + "_SDH_Visualization.jpg}");

            this.writer.appendOutput("\\caption{SDH with k = " + i + "}" + "\\label{fig:sdh" + i + "}"
                    + "\\end{minipage}\n" + "\\hspace{2cm}\n");

        }
        this.writer.appendOutput("\\end{figure}\n" + "\\end{center}");
        // --- then we give some information about the selected input vectors
        if (this.dataset.getNumberOfSelectedInputs() > 0) {
            this.writer.appendLatexOutput("\\subsection{Location and quantization errors of selected input items:}\n");
            this.writer.appendOutput("Table 4 on page \\pageref{tab:loc} shows for all input items their corresponding Map Node and the Quantization Error \\textit{e}m$^q$ of that Node. Additionally the Distance  "
                    + "$\\xi_p^s$ from the Input units Vector \\textit{s} to the prototype vector \\textit{p} of the SOM unit."
                    + "is mapped, and how big the quantization error is. After that, this is visualized in another table.\\\\\n");
            // first a list of the selected data items with more information about them:
            this.writer.appendOutput("\\begin{longtable}{|c|c|l|l|l|}\\hline\n" + "Item No. \n&" + "Mapped Node \n&"
                    + "\\textit{e}$^q$ &\n" + "$\\xi_p^s$\n" + "\\\\");

            // we have to somehow map the data items to the units, s.t. we than can walk through the
            // units to retrieve the corresponding data items
            // while doing this, we print a list of all selected items with some additional information
            for (int i = 0; i < this.dataset.getNumberOfSelectedInputs(); i++) {
                this.writer.appendLatexOutput("\\hline");

                input = this.dataset.getInputDatum(this.dataset.getSelectedInputId(i));
                unit = this.testrun.getUnitMappedTo(input);
                key = unit.getXPos() + "_" + unit.getYPos();

                // create the latex output for the list
                this.writer.appendLatexOutput(input.getLabel()
                        + "&"
                        // where
                        + "[" + unit.getXPos() + ", " + unit.getYPos() + "] " + " & (qe="
                        + String.format("%.5f", this.testrun.getQEForUnit(unit.getXPos(), unit.getYPos())) + ""
                        + " and mqe="
                        + String.format("%.5f", this.testrun.getMQEForUnit(unit.getXPos(), unit.getYPos())) + ")&"
                        // qe
                        + String.format("%.5f", this.testrun.getInputMappedQE(input.getLabel()).getQE()) + "\n");
                this.writer.appendLatexOutput("\\\\");
                // add this input to the lookup table for creating the table
                if (lookup.containsKey(key)) {
                    // there's already one input vector mapped to this unit
                    mappedInputs = lookup.get(key);
                    lookup.remove(key);
                } else {
                    // new
                    mappedInputs = new Vector<InputDatum>();
                }
                mappedInputs.add(input);
                lookup.put(key, mappedInputs);
            }
            this.writer.appendLatexOutput("\\hline\\caption{Location and Quantization errors of selected input items}\n"
                    + "\\label{tab:loc}\n" + "\\end{longtable}\n");

            // print the table with the datasets inserted
            // NO!! PAPER IS TOO SMALL FOR IT :(
            // this.writer.appendLatexOutput("The following table visualized the information given in the list above. Values enclosed by \"\" denote "
            // +
            // "the label of a selected input item, the number in the top left corner of each cell gives the number of
            // input items totally mapped to
            // this unit. ");
            // this.printDistributionDetailTable(lookup, classInfoAvailable);
        }
    }

    /**
     * creates and outputs one out of two possible tables the first possible table: \\\\ the first table contains for
     * each unit only the number of input vectors mapped to it, and a pie chart image of the class distribution within
     * this unit. (lookup == null) If no class information is available, this table is not created\\\\ \\\\ The second
     * table additionally contains the ids of input vectors selected by the user within the unit they are mapped to.
     * Therefore lookup must be a hashtable that contains for each unit (key = "x_y") a list of InputDatum objects
     * specifying the input elements mapped to this unit. If for a key there's no list, then nothing is mapped to this
     * unit. if no class information is available, the table does not contain the number of input vectors mapped to each
     * unit
     * 
     * @param lookup a hashtable with the content specified above to map input vectors to units
     * @param classInformationAvailable true if class information (and therefore pie chart diagrams for the units are
     *            available, false otherwise
     */
    protected void printDistributionDetailTable(Hashtable<String, Vector<InputDatum>> lookup,
            boolean classInformationAvailable) {

        File pieDir = null;
        if (classInformationAvailable) {
            pieDir = new File(this.imgDir + "pieCharts_" + this.testrun.getRunId()
                    + System.getProperty("file.separator"));
        }

        this.writer.appendLatexOutput("\n\\begin{table}\n\\begin{center}\n\\begin{tabular}{");
        for (int x = 0; x < Integer.parseInt(((String) this.testrun.getMapProperty(TestRunResultCollection.keyXDim))); x++) {
            if (x == 0) {
                this.writer.appendOutput("|");
            }

            this.writer.appendOutput("|l");
            if (x == Integer.parseInt(((String) this.testrun.getMapProperty(TestRunResultCollection.keyXDim))) - 1) {
                this.writer.appendOutput("||");
            }
        }
        this.writer.appendLatexOutput("}\n");
        this.writer.appendOutput("\\hline\\hline");

        // as we want to create html output, we first have to iterate about the rows (tr elements)
        for (int y = 0; y < Integer.parseInt(((String) this.testrun.getMapProperty(TestRunResultCollection.keyYDim))); y++) {

            // and only within these elements about the columns (td elements)
            for (int x = 0; x < Integer.parseInt(((String) this.testrun.getMapProperty(TestRunResultCollection.keyXDim))); x++) {

                String img = "";
                String numbDiv = "";
                String mappedInputsDiv = "";
                String content1 = "";

                // the class pie chart and number of input items per unit (as these number is encoded within the pie
                // chart images names
                if (classInformationAvailable) {
                    String[] matches = pieDir.list(new PrefixFileFilter(x + "_" + y + "_")); // lookup whether
                    // there's an image
                    if (matches != null && matches.length > 0) {
                        // image exists => insert it
                        String number = matches[0].substring((x + "_" + y + "_").length());
                        number = number.substring(0, number.length() - 4);

                        img = "\n\\includegraphicstotab[width=20pt]{" + SOMDescriptionLATEX.imgSubdir + "/pieCharts_"
                                + this.testrun.getRunId() + "/" + matches[0] + "}\n";
                        numbDiv = "" + number;
                        content1 = numbDiv + img;
                    }
                } else {
                    int count = this.testrun.getNumberOfInputsOnUnit(x, y);
                    if (count > 0) {
                        numbDiv = "" + count;
                        content1 = numbDiv;
                    }
                }
                // the mapped input vectors
                if (lookup != null) {
                    String key = x + "_" + y;
                    Vector<InputDatum> mapped = lookup.get(key);
                    if (mapped != null) {
                        for (int i = 0; i < mapped.size(); i++) {
                            mappedInputsDiv += "\"" + mapped.get(i).getLabel() + "\"\\\\\n";
                        }
                    }
                }
                String content = content1 + mappedInputsDiv;

                // if there's an empty unit (if no unit => show noting)
                if (content.length() == 0 && this.testrun.hasUnitOn(x, y)) {
                    content = " ";
                }

                // write the table cell
                this.writer.appendOutput(content);
                if (x + 1 < Integer.parseInt(((String) this.testrun.getMapProperty(TestRunResultCollection.keyXDim)))) {
                    this.writer.appendOutput("&\n");
                }
            }// for x
            this.writer.appendLatexOutput("\\\\\\hline\n");
        }// for y
        this.writer.appendLatexOutput("\\hline\n\\end{tabular}\n\\caption{Visualisation of the Hit Histogram and the corresponding Pie-chart Class images}\n\\label{tab:pie}\n\\end{center}\n\\end{table}\n");
    }

    /**
     * prints a list with different quantization errors. This includes: \\begin{itemize} \\item Map mean quantization
     * error \\item Map mean mean quantization error \\item Min/Max unit quantization error \\item Min/Max unit mean
     * quantization error \\item Min/Max quantization error of an input vector (as it is mapped to SOM) \\item Min/Max
     * quantization error of an input vector (taken the best matching unit, as I think it could happen that during the
     * training an input vector is mapped to a unit that might be the best matching unit, but during the training
     * process is then pulled away from this input, s.t. in the end another unit would be better)
     */
    protected void printQuantizationErrorReport() {

        UnitQEContainer min;
        UnitQEContainer max;
        InputQEContainer mini;
        InputQEContainer maxi;
        String unitCoord;

        this.writer.appendOutput(TextualDescriptionProvider.getScientificDescription("QError") + "\\\\");
        // min/max quantization errors
        this.writer.appendLatexOutput("\\begin{itemize}\n");

        // map quantization error (mqe & mmqe)
        this.writer.appendLatexOutput("\\item Map quantization errors:\n\\begin{itemize}");
        this.writer.appendLatexOutput("\\item Map mean quantization error:"
                + String.format("%.5f", this.testrun.getMapMQE().getQE()) + "\n");
        this.writer.appendLatexOutput("\\item Map mean mean quantization error:"
                + String.format("%.5f", this.testrun.getMapMMQE().getQE()) + "\n");
        this.writer.appendLatexOutput("\\end{itemize}");

        // min unit/max unit quant. error
        this.writer.appendLatexOutput("\\item Unit quantization errors:\n\\begin{itemize}");
        min = this.testrun.getMinUnitQE();
        unitCoord = getUnitCoords(min);
        this.writer.appendLatexOutput("\\item Minimal unit quantization error: " + String.format("%.5f", min.getQE())
                + unitCoord + "\n");
        max = this.testrun.getMaxUnitQE();
        unitCoord = getUnitCoords(max);
        this.writer.appendLatexOutput("\\item Maximal unit quantization error: " + String.format("%.5f", max.getQE())
                + unitCoord + "\n");

        // min mqe/max mqe per unit
        min = this.testrun.getMinUnitMQE();
        unitCoord = getUnitCoords(min);
        this.writer.appendLatexOutput("\\item Minimal unit mean quantization error: "
                + String.format("%.5f", min.getQE()) + unitCoord + "\n");
        max = this.testrun.getMaxUnitMQE();
        unitCoord = getUnitCoords(max);
        this.writer.appendLatexOutput("\\item Maximal unit mean quantization error: "
                + String.format("%.5f", max.getQE()) + unitCoord + "\n");
        this.writer.appendLatexOutput("\\end{itemize}");

        // min qe / max qe per input vector (mapped)
        this.writer.appendLatexOutput("\\item Quantization errors of input vectors:\n\\begin{itemize}");
        mini = this.testrun.getMinInputMappedQE();
        this.writer.appendLatexOutput("\\item Minimal (mapped) quantization error of input vector: "
                + String.format("%.5f", mini.getQE()) + getInputCoords(mini) + "\n");
        maxi = this.testrun.getMaxInputMappedQE();
        this.writer.appendLatexOutput("\\item Maximal (mapped) quantization error of input vector: "
                + String.format("%.5f", maxi.getQE()) + getInputCoords(maxi) + "\n");

        // min qe/max qe per input vector (best matching)
        mini = this.testrun.getMinInputWinnerQE();
        this.writer.appendLatexOutput("\\item Minimal (best matching) quantization error of input vector: "
                + String.format("%.5f", mini.getQE()) + getInputCoords(mini) + "\n");
        maxi = this.testrun.getMaxInputWinnerQE();
        this.writer.appendLatexOutput("\\item Maximal (best matching) quantization error of input vector: "
                + String.format("%.5f", maxi.getQE()) + getInputCoords(maxi) + "\n");
        this.writer.appendLatexOutput("\\end{itemize}");

        this.writer.appendLatexOutput("\\end{itemize}\n");

        if (this.testrun.createQuantizationErrorImage(this.imgDir, "quantErr.jpg", 0)) {
            this.writer.appendLatexOutput("\\paragraph{visualization of the quantization error:}\n");
            this.writer.appendLatexOutput("The following image shows the quantization error on the SOM. Light (especially green) regions denote areas with low quantization "
                    + "error, whereas dark regions show units with high quantization errors.\\\\");
            this.writer.appendOutput("\\begin{figure}[htbp]\n" + "\\centering" + "\\includegraphics[width=4.5cm]{"
                    + SOMDescriptionLATEX.imgSubdir + "/run_" + this.testrun.getRunId() + "_quantErr.jpg}\n"
                    + "\\caption{Visualization of the Q-Error}" + "\\label{fig:QError}" + "\\end{figure}\n");
        }
    }

    /**
     * adds information about the topographic error on the map to the report beside a list containing the topographic
     * error of the map and the min/max top. error on the units, an image visualizing the distribution of the
     * topographic error on the map is output.
     */
    protected void printTopographicErrorReport() {

        UnitQEContainer min;
        UnitQEContainer max;
        String unitCoord;

        double maxv = this.testrun.getMaxUnitTE().getQE();
        if (maxv == 0) {
            this.writer.appendLatexOutput("There is no topographic error on the map!");
            return;
        }
        this.writer.appendOutput(TextualDescriptionProvider.getScientificDescription("TError") + "\\\\");

        this.writer.appendLatexOutput("\\begin{itemize}\n");

        // map topographic error
        this.writer.appendLatexOutput("\\item Topographic error of the som:\n\\begin{itemize}");

        this.writer.appendLatexOutput("\\item Topographic error of the map: "
                + String.format("%.5f", this.testrun.getMapTE()) + "\n");
        this.writer.appendLatexOutput("\\end{itemize}");

        // min unit/max unit top. error
        this.writer.appendLatexOutput("\\item Unit topographic errors:\n\\begin{itemize}");
        min = this.testrun.getMinUnitTE();
        unitCoord = getUnitCoords(min);
        this.writer.appendLatexOutput("\\item Number of units without topographic error: "
                + this.testrun.getNumberOfTElessUnits() + "");
        this.writer.appendLatexOutput("\\item Minimal topographic error > 0 of a unit : "
                + String.format("%.2f", min.getQE()) + unitCoord + "" + "\n");
        max = this.testrun.getMaxUnitTE();
        unitCoord = getUnitCoords(max);
        this.writer.appendLatexOutput("\\item Maximal topographic error of a unit: "
                + String.format("%.2f", max.getQE()) + unitCoord + "\n");
        this.writer.appendLatexOutput("\\end{itemize}\n");

        this.writer.appendLatexOutput("\\end{itemize}");

        if (this.testrun.createTopographicErrorImage(this.imgDir, "topErr.jpg")) {
            this.writer.appendLatexOutput("\\paragraph{visualization of the topographic error:}\n");
            this.writer.appendLatexOutput("This image shows the topographic error on the SOM. Dark areas mark regions of the SOM with high topographic error, "
                    + "lighter regions indicate less or no topographic error.\\\\\n");
            this.writer.appendOutput("\\begin{figure}[htbp]\n" + "\\centering" + "\\includegraphics[width=4.5cm]{"
                    + SOMDescriptionLATEX.imgSubdir + "/run_" + this.testrun.getRunId() + "_topErr.jpg}\n"
                    + "\\caption{Visualization of the T-Error}" + "\\label{fig:TError}" + "\\end{figure}\n");

        }
    }

    /**
     * formats a list of units for use in the quantization error list the created format is "on unit(s) at [x,y] - z
     * vectors mapped, [x2,y2] - z2 vectors mapped, ...
     * 
     * @param value the container from which the information about the number ob units can be picked
     * @return A string formatted as stated above
     */
    protected String getUnitCoords(UnitQEContainer value) {
        String unitCoord = "";
        for (int i = 0; i < value.getNumUnits(); i++) {
            if (i > 0) {
                unitCoord += ",\\\\ ";
            }
            unitCoord += "\\lbrack" + value.getUnitCoords(i) + "\\rbrack  - " + value.getNumberOfVectorsMapped(i)
                    + " vectors mapped";
        }
        if (value.getNumUnits() > 1) {
            unitCoord = "on units at : \\\\" + unitCoord;
        } else {
            unitCoord = "on unit at " + unitCoord;
        }
        return unitCoord;
    }

    /**
     * formats a list of input vectors for use in the quantization error list the created format is:
     * "on input vector(s) "id" on unit at[x,y], "id2" on unit at [x2,y2],...
     * 
     * @param value a container storing all information required to create the output
     * @return a string formatted as stated above
     */
    private String getInputCoords(InputQEContainer value) {
        String inputCoord = "";
        for (int i = 0; i < value.getNumInputs(); i++) {
            if (i > 0) {
                inputCoord += ", ";
            }
            inputCoord += "\"" + value.getInputLabel(i) + "\" on unit at \\lbrack" + value.getUnitCoords(i)
                    + "\\rbrack";
        }
        if (value.getNumInputs() > 1) {
            inputCoord = "on input vectors" + inputCoord;
        } else {
            inputCoord = "on input vector" + inputCoord;
        }
        return inputCoord;
    }

    /**
     * this function prints some information about the possible clusters that can be found on the SOM. It tries to find
     * some good, or stable clusters, using a simple heuristic. For a description of this, please see
     * TestRunResult.getStableClusters2. (it works at least quite well with the iris and the animal dataset - more test
     * tbd. as maximal number of clusters, the number of units on the SOM is choosen. Besides the tree of available
     * clusters, the ten "best" (according to this heuristic clusters are listed and visualized. Also the image of the
     * UMatrix of the SOM is created and attached to the report !!! I also wanted to do some tests with the k-nearest
     * neighbour clustering, but couldn't find any implementation of it. !!!
     */
    protected void printClusterInfos() {

        this.writer.appendLatexOutput("\\section{Clusters on the SOM:}\n");

        // an image of the UMatrix of the SOM
        if (this.testrun.createUDMatrixImage(this.imgDir, "umatrix.jpg", 30, 1)) {
            this.writer.appendLatexOutput("\\subsection{The U-Matrix visualization of the SOM:}\n");
            this.writer.appendLatexOutput(TextualDescriptionProvider.getScientificDescription("uMatrix")
                    + "Light, green areas on the map indicate small distances between units, whereas"
                    + "dark, red areas indicate bigger distances.\\\\\n");
            if (this.testrun.getGrowingSOM().getLayer().getXSize() * this.testrun.getGrowingSOM().getLayer().getYSize() < this.dataset.getNumberOfInputVectors()) {
                this.writer.appendOutput("Since the number of prototype Vectors is smaller than the number of Training Samples, the U-Matrix Visualization contains no artifacts.");
            } else {
                this.writer.appendOutput("Since the number of prototype Vectors is larger than the number of Training Samples, the U-Matrix Visualization contains artifacts around the positions, where data samples are mapped.");
            }

            this.writer.appendOutput("\\begin{figure}[htbp]\n" + "\\centering" + "\\includegraphics[width=4.5cm]{"
                    + SOMDescriptionLATEX.imgSubdir + "/run_" + this.testrun.getRunId() + "_umatrix.jpg}\n"
                    + "\\caption{The U-Matrix}" + "\\label{fig:UMatrix}" + "\\end{figure}\n");

        }

        ClusterNode node = this.testrun.getClusterNodes(1, 1); // the root node of the cluster Tree

        // print the cluster tree
        /*
         * this.writer.appendLatexOutput("\\paragraph{The cluster tree</p>"); this.writer.appendLatexOutput("The following is a representation of the
         * cluster tree. For each cluster (=node of the tree), its level (that " + "is the number of the step in which the node is splitted) is given,
         * as well as the number of steps for that the node isn't splitted into two subclusters. By " + "clicking onto a node, the subtree rooted in
         * this node can be expanded/collapsed. (at the beginning, the whole tree is collapsed)");
         * this.writer.appendLatexOutput("\\begin{itemize}\n");
         */
        int somDim = Integer.parseInt((String) this.testrun.getMapProperty(TestRunResultCollection.keyXDim))
                * Integer.parseInt((String) this.testrun.getMapProperty(TestRunResultCollection.keyYDim));

        /*
         * as we have no possibility to retrieve the depth of a node within the tree, given the node efficiently, and because we are netherless
         * traversing the tree once, we save for all node their depths. this is done in the nodeInfos array at the indices 2 ... n+1 (for node 1 ...
         * n). on position 0, the level of the node that is stable for the most number of steps, and on position 1, the according number of steps is
         * saved
         */
        int[] nodeInfos = new int[somDim + 2];
        nodeInfos[0] = 0;
        nodeInfos[1] = 0;
        // nodeInfos = this.printClusterNode(node, 1, somDim, 1, nodeInfos);
        // this.writer.appendLatexOutput("\\end{itemize}\n");
        // this.writer.appendLatexOutput("\\max stable is level " + nodeInfos[1] + " for "+nodeInfos[0] +
        // " new clusters");

        // print the top ten list and then the visualizations of it:

        // our second (and final) method
        Vector<double[]> stableList2 = this.testrun.getStableClusters2(node, 0.0, somDim, new Vector<double[]>());
        this.writer.appendLatexOutput("\n\\subsection{Top ten stable clusters:}\n");
        this.writer.appendLatexOutput("The following list contains the 10 most stable clusters (selected according to a simple heurstic). For each cluster,"
                + " the number of input items lying in this cluster is given. Also how the classes are distributed within the cluster. Also, for each cluster "
                + "a suggested name is displayed. A bigger image of the cluster can be retrieved by clicking onto the cluster image.");
        this.writer.appendLatexOutput("\\begin{itemize}");
        for (int i = 0; i < 10 && i < stableList2.size(); i++) {
            Vector<String> clusterNames = this.dataset.getClusterName(this.testrun.getClusterNodes(
                    (int) stableList2.get(i)[1], 1), (int) CommonSOMViewerStateData.getInstance().clusterByValue,
                    nodeInfos[(int) stableList2.get(i)[1]]);
            String label = "";
            for (int j = 0; j < clusterNames.size(); j++) {
                if (j > 0) {
                    label += "; ";
                }
                label += clusterNames.get(j);
            }
            this.writer.appendLatexOutput("\\item \\begin{itemize}\\item cluster "
                    + label
                    + "\\newline on  level "
                    + stableList2.get(i)[1]
                    + " with gain = "
                    + String.format("%.4f", stableList2.get(i)[0])
                    + "\\\\"
                    + this.testrun.getClusterNodes((int) stableList2.get(i)[1], 1).getNumberOfInputs()
                    + " inputs in cluster\\\\"
                    + classDistInCluster((int) stableList2.get(i)[1], this.testrun.getClusterNodes(
                            (int) stableList2.get(i)[1], 1).getNumberOfInputs())/*
                                                                                 * + "\\\\<a
                                                                                 * href=\"#clusterVis_"+i+"\">(see)</a></td>"
                                                                                 */);
            this.testrun.visualizeCluster((int) stableList2.get(i)[1], this.imgDir, nodeInfos, 50);
            this.testrun.visualizeCluster((int) stableList2.get(i)[1], this.imgDir, nodeInfos, 10);
            this.writer.appendOutput("\\includegraphics{" + SOMDescriptionLATEX.imgSubdir + "/run_"
                    + this.testrun.getRunId() + "_clusterLevel_" + (int) stableList2.get(i)[1] + "_10.jpg}\n");
            this.writer.appendLatexOutput("\\end{itemize}\n");
        }
        this.writer.appendLatexOutput("\\end{itemize}");

        /*
         * this.writer.appendLatexOutput("\\paragraph{visualization of top ten clusters:</p>"); for(int i = 0; i < 10; i++){ if( &&
         * this.testrun.visualizeCluster((int)stableList2.get(i)[1], this.imgDir, nodeInfos, 10)){ this.writer.appendLatexOutput("<p
         * class=\"header2\"><a name=\"clusterVis_"+i+"\">level "+ stableList2.get(i)[1] +"</a>\\\\" + "<img
         * src=\""+SOMDescriptionHTML.imgSubdir+"/run_"+this.testrun.getRunId()+"_clusterLevel_"+(int)stableList2.get(i)[1]+".jpg\" alt=\"\"/>" +
         * "</p>"); } }
         */

        /*
         * I also tried this .... //our first method - didn't work quie well, for more information, please see TestRunRest.getStableClusters1()
         * Vector<double[]> stableList1 = this.testrun.getStableClusters1(node, 1, somDim, new Vector<double[]>());
         * this.writer.appendLatexOutput("top ten stable clusters:"); this.writer.appendLatexOutput("\\begin{itemize}"); for(int i = 0; i <
         * stableList1.size(); i++){ this.writer.appendLatexOutput("\\item level "+ stableList1.get(i)[1] +" with " + stableList1.get(i)[0] + ""); }
         * this.writer.appendLatexOutput("\\end{itemize}"); //our third method - didn't work quie well, for more information, please see
         * TestRunRest.getStableClusters13() Vector<double[]> stableList3 = this.testrun.getStableClusters3(node, 0.0, somDim, new
         * Vector<double[]>()); this.writer.appendLatexOutput("top ten stable clusters:"); this.writer.appendLatexOutput("\\begin{itemize}"); for(int
         * i = 0; i < stableList3.size(); i++){ this.writer.appendLatexOutput("\\item level "+ stableList3.get(i)[1] +" with " + stableList3.get(i)[0]
         * + ""); } this.writer.appendLatexOutput("\\end{itemize}");
         */
    }

    /**
     * returns a formatted string that contains information about the classes present in the given cluster
     * 
     * @param level the level of the cluster of interest
     * @param numbInputs the number of input vectors mapped to this cluster
     * @return a formatted string containing some information about this cluster
     */
    private String classDistInCluster(int level, int numbInputs) {

        if (!this.dataset.classInfoAvailable()) {
            return "";
        }

        int numbClasses = 0;
        int[] classDist = this.testrun.getClassDistributionInCluster(level);
        for (int element : classDist) {
            if (element > 0) {
                numbClasses++;
            }
        }

        String out;
        if (numbClasses != 1) {
            out = numbClasses + " classes in cluster: ";
        } else {
            out = numbClasses + " class in cluster: ";
        }

        if (numbClasses > 0) {
            out += "\\begin{itemize}";
            for (int i = 0; i < classDist.length; i++) {
                if (classDist[i] == 0) {
                    continue;
                }

                out += "\\item "
                        + this.dataset.getNameOfClass(i)
                        + ": "
                        + classDist[i]
                        + " inputs ("
                        + String.format("%.2f", (double) classDist[i] / (double) numbInputs * 100)
                        + "%) = "
                        + String.format("%.2f", (double) classDist[i]
                                / (double) this.dataset.getNumberOfClassmembers(i) * 100) + "% of class members" + "";
            }
            out += "\\end{itemize}\n";
        } else {
            out += "\\\\\n";
        }

        return out;
    }

    // nodeInfos[0] = maxStableCount, nodeINfos[1] = maxStableCount-level nodeInfos[2 .. n+1]: depths of levels 1 ... n
    /**
     * outputs the subtree in one node of the cluster tree we add to the report. this node is collapsed by default. the
     * cluster tree is added as unorded list with sub-items to the report. This functions creates the subtree of this
     * cluster tree in one node, by calling itself recursivley. The subtree is collapsed by default.
     * 
     * @param node the node in the cluster tree, whose subtree shall be created
     * @param parentLevel the number of the level of the parent node - needed to calculate the number of new clusters
     *            this cluster remains unchanged
     * @param boundary the maximal level, where the recursion shall stop
     * @param depth the depth of this node in the tree, seen from the root
     * @param nodeInfos WARNING!!! this array contains two different information: at index 0, the maximal number of new
     *            clusters a cluster "survived" is saved. at index 1, the corresponding level is saved. from index i = 2
     *            to n+1 , the depth of the clusternode of level i is saved.
     * @return the array nodeInfos (see above - last parameter), containing the correct values for the subrtree rooted
     *         in "node".
     */
    /*
     * protected int[] printClusterNode(ClusterNode node, int parentLevel, int boundary, int depth, int[] nodeInfos){ //stop recursion if we are deep
     * enough if(node.getLevel() > boundary) return nodeInfos; CommonSOMViewerStateData state = CommonSOMViewerStateData.getInstance(); //we need this
     * for clustering ClusterNode lc = node.getChild1(); ClusterNode rc = node.getChild2(); int curStable = node.getLevel() - parentLevel - 1;
     * if(curStable > nodeInfos[0]){ nodeInfos[0] = curStable; nodeInfos[1] = node.getLevel(); } nodeInfos[node.getLevel()+1] = depth; //itself
     * Vector<String> labels = this.dataset.getClusterName(node, (int)state.clusterByValue, depth); this.writer.appendLatexOutput("\\item ");
     * this.writer.appendLatexOutput("<div onclick=\"swapClusterDisp("+node.getLevel()+");\">"); this.writer.appendLatexOutput("<b>level
     * "+node.getLevel()+"</b> "); if(labels.size() > 0){ this.writer.appendLatexOutput("("+labels.get(0)+")"); } this.writer.appendLatexOutput("
     * stable for: "+curStable); this.writer.appendLatexOutput("<div id=\"clusterNode_"+node.getLevel()+"\" style=\"display:none;\">"); //recursive
     * decent if(lc.getLevel()+1 <= boundary){ //first child this.writer.appendLatexOutput("\\begin{itemize}"); nodeInfos = this.printClusterNode(lc,
     * node.getLevel(), boundary, depth+1, nodeInfos); this.writer.appendLatexOutput("\\end{itemize}"); } if(rc.getLevel()+1 <= boundary){ //second
     * Child this.writer.appendLatexOutput("\\begin{itemize}"); nodeInfos = this.printClusterNode(rc, node.getLevel(), boundary, depth+1, nodeInfos);
     * this.writer.appendLatexOutput("\\end{itemize}"); } return nodeInfos; }
     */
}
