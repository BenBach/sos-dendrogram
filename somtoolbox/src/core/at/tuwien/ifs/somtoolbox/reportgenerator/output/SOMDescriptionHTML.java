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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.EditableReportProperties;
import at.tuwien.ifs.somtoolbox.reportgenerator.SemanticInterpreterGrid;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResultCollection;
import at.tuwien.ifs.somtoolbox.reportgenerator.TextualDescriptionProvider;
import at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers.InputQEContainer;
import at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers.QMContainer;
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
 * @version $Id: SOMDescriptionHTML.java 3914 2010-11-04 14:28:18Z mayer $
 */
public class SOMDescriptionHTML {

    /** the name of the directory where the image shall be saved - relative to the given baseDir path in the constructor */
    public static final String imgSubdir = "images";

    /** ArrayList for quality measures */
    @SuppressWarnings("rawtypes")
    ArrayList qualifiers = new ArrayList();

    /** all strings that shall be in the output are sent to this object */
    protected ReportFileWriter writer;

    /** encapsulates all available information about the testrun - just ask */
    protected TestRunResult testrun;

    /** contains all available information about the input data */
    protected DatasetInformation dataset;

    /** Quality Measure Container */

    private QMContainer qm = null;

    /** the directory to which the created images shall be saved */
    private String imgDir;

    /** The SemanticGrid used for Semantic Interpretation of Quality Measures & Classes */
    private SemanticInterpreterGrid sGrid;

    private EditableReportProperties EP;

    /**
     * creates a new instance
     * 
     * @param writer object that handles how the created string is written to a file
     * @param dataset object storing information about input data
     * @param testrun object storing information about testrun results
     */
    public SOMDescriptionHTML(ReportFileWriter writer, DatasetInformation dataset, TestRunResult testrun,
            String baseDir, EditableReportProperties EP) {
        this.EP = EP;
        this.imgDir = baseDir;
        if (!StringUtils.isBlank(imgDir) && !imgDir.endsWith(File.separator)) {
            this.imgDir += File.separator;
        }
        this.imgDir += SOMDescriptionHTML.imgSubdir + File.separator;
        File dir = new File(imgDir);
        if (dir.exists() && !dir.isDirectory()) {
            dir.delete();
        }
        if (!dir.exists()) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").info(
                    "Creating image output directory '" + dir.getAbsolutePath() + "', success: " + dir.mkdirs());
            dir.mkdir();
        }
        this.writer = writer;
        this.dataset = dataset;
        this.testrun = testrun;
        this.qualifiers = this.EP.getSelectedQualitMeasure();
        this.qm = this.testrun.getQMContainer();
        // UPDATE MAZ
        sGrid = new SemanticInterpreterGrid(this.testrun.getGrowingSOM().getLayer().getAllUnits(), this.testrun, true,
                0, this.testrun.getType(), this.imgDir);
        if (this.dataset.classInfoAvailable()) {
            sGrid.initializeGridwithClasses(true);
        }
        // END
    }

    /**
     * initiates the creation of the output Creates the description of the SOM and training properties
     */
    public void printSOMDescription() {

        // SOM & training properties
        // UPDATE MAZ
        if (this.testrun.hasMapDescription()) {
            this.writer.appendOutput("<ul>");
            this.printSOMProperties();
            this.writer.appendOutput("</ul>");
        }
        // END
        else {
            this.writer.appendOutput("Here should be displayed some Information about the SOM Properties, but no Description File was used during Creation of this report.");
        }

        // data distribution on SOM
        this.printDataDistribution();
        if (this.EP.includeRegionReport()) {
            this.printRegionalOverviewReport();
            // make a semantic report
        }
    }

    /**
     * prints a list of properties describing the training process and the generated SOM this list contains besides
     * others:
     * <ul>
     * <li>type and topology of SOM</li>
     * <li>dimensions of the som</li>
     * <li>different training parameters</li>
     * <li>neighbourhood function</li>
     * <li>...</li>
     * </ul>
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
            this.writer.appendOutput("<li>Type of SOM: GrowingSOM with rectangle units</li>\n");
        } else if (top.equals("hex")) {
            this.writer.appendOutput("<li>GrowingSOM with hexagonal units</li>\n");
        } else if (top.equals("hfm")) {
            this.writer.appendOutput("<li>Hierarchical Feature Map</li>\n");
        } else if (top.equals("gcs")) {
            this.writer.appendOutput("<li>Growing Cell Structures</li>\n");
        } else {
            this.writer.appendOutput("<li>Topology: " + top + "</li>\n");
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
        this.writer.appendOutput("<li>Dimensions of the SOM: " + dim + "</li>\n");
    }

    /**
     * prints the value of the sigma, as one of the learning parameters (taken from the SOMProperty object)
     */
    protected void printSigma() {

        String sigma = "Property file not defined";
        if (this.testrun.getSigma() >= 0) {
            sigma = "" + this.testrun.getSigma();
        }
        this.writer.appendOutput("<li>Neighbourhood radius (sigma): " + sigma + "</li>\n");
    }

    /**
     * prints the value of the tau, as one of the learning parameters (taken from the SOMProperty object)
     */
    protected void printTau() {
        String tau = "Property file not defined";
        if (this.testrun.getTau() >= 0) {
            tau = "" + this.testrun.getTau();
        }
        this.writer.appendOutput("<li>Desired data representation granularity (tau): " + tau + "</li>\n");
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
        this.writer.appendOutput("<li>Distance metric used: " + metric + "</li>\n");
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
        this.writer.appendOutput("<li>Iterations made during traingin: " + its + "</li>\n");

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
        this.writer.appendOutput("<li>Map has been trained on: " + trainingDate + "</li>\n");
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
        this.writer.appendOutput("<li>Time required for training: " + trainingTime + "</li>\n");
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
        this.writer.appendOutput("<li>Value for initializing random number generator: " + randomSeed + "</li>\n");
    }

    /**
     * Prints the neighbourhood type and the initial neighbourhood range used for training (taken from the
     * MySOMMapDescription, provided by the TestRunResult object)
     */
    protected void printNeighbourhoodFunction() {

        String neighbourhoodType = (String) this.testrun.getMapProperty(TestRunResultCollection.keyNeighbourhoodType);
        String neighbourhoodInit = (String) this.testrun.getMapProperty(TestRunResultCollection.keyNeighbourhoodInit);

        if (neighbourhoodType != null && neighbourhoodType.length() > 0) {
            this.writer.appendOutput("<li>Type Neighbourhood: " + neighbourhoodType + "</li>\n");
        }
        if (neighbourhoodInit == null || neighbourhoodInit.length() <= 0) {
            neighbourhoodInit = "Could not detect initial neighbourhood range";
        }
        this.writer.appendOutput("<li>Initial Neighobourhood range: " + neighbourhoodInit + "</li>\n");
    }

    /**
     * prints the type of how the learning rate changes, and the initial learning rate (taken from the
     * MySOMMapDescription, provided by the TestRunResult object)
     */
    protected void printLearningRate() {

        String learntype = (String) this.testrun.getMapProperty(TestRunResultCollection.keyLearnrateType);
        String learnrate = (String) this.testrun.getMapProperty(TestRunResultCollection.keyLearnRateInit);

        if (learntype != null && learntype.length() > 0) {
            this.writer.appendOutput("<li>Type of Learning: " + learntype + "</li>\n");
        }
        if (learnrate == null || learnrate.length() <= 0) {
            learnrate = "Could not detect learning rate";
        }
        this.writer.appendOutput("<li>Initial learning rate: " + learnrate + "</li>\n");
    }

    public void printDataDistribution() {

        this.writer.appendOutput("<h3><a name =\"2.1 Distribution of the input data on the trained SOM\"><font color=\"black\">2.1 Distribution of the input data on the trained SOM:</a></h3></font>\n");
        this.writer.writeTableofContentsSubEntry("2.1 Distribution of the input data on the trained SOM");
        this.writer.appendOutput("<div class=\"infoBlock\">");

        if (this.dataset.classInfoAvailable()) {
            // class information given
            this.testrun.createPieChartImages(this.imgDir);
        } else {
            // no class information given
            this.writer.appendOutput("<p class=\"header\">There is no class information attached to this input</p>");
            this.writer.appendOutput("</div>");
            this.writer.appendOutput("<div class=\"infoBlock\">");
        }

        // represent the SOM as HTML table - maybe as two, if input were selected

        this.printMapLayout(this.dataset.classInfoAvailable());
        this.writer.appendOutput("</div>");

        // say something about the class purity
        if (this.dataset.classInfoAvailable()) {
            this.writer.appendOutput("<div class=\"infoBlock\">");
            this.writer.appendOutput("<p class=\"header\"><a name =\"2.3 Class mix on the units\"><font color=\"black\">2.3 Class mix on the units:</a></font></p>");
            this.writer.writeTableofContentsSubEntry("2.3 Class mix on the units");
            this.writer.appendOutput("In addition to the tables with the class pie-charts, the following list gives an overview "
                    + "about whether the classes are separated rather good or bad on the SOM. (The right column states how many units exist that contain "
                    + "the number of units given in the left column. Entries with 0 units matching are omitted)");
            int[] purity = this.testrun.getClassPurity1();
            this.writer.appendOutput("<table>\n<thead>\n<tr>\n</tr>\n" + "<tr>\n" + "<th># of classes</th>\n"
                    + "<th># of units with this # of classes</th>\n" + "</tr>\n" + "</thead>\n" + "<tbody>\n");
            for (int i = 0; i < purity.length; i++) {
                if (purity[i] == 0) {
                    continue;
                }
                this.writer.appendOutput("<tr>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + i + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + purity[i] + "</td>\n");
                this.writer.appendOutput("</tr>\n");
            }
            this.writer.appendOutput("</tbody>" + "</table>");

            // now we give an estimation how good the SOM seperates the classes
            this.writer.appendOutput(this.testrun.getClassPurity2(purity));

            // try to explain where the classes are located, how strong they are splitted ...
            // create Images..
            this.testrun.createClassDistributionImage(this.imgDir, "classDistribution.jpg", -1);
            this.testrun.createEntropyImage(this.imgDir, "classEntropy.jpg", -1);
            for (int i = 0; i < this.dataset.getNumberOfClasses(); i++) {
                this.testrun.createEntropyImage(this.imgDir, "classEntropy" + i + ".jpg", i);
                this.testrun.createClassDistributionImage(this.imgDir, "classDistribution" + i + ".jpg", i);
            }
            this.writer.appendOutput("<p class=\"header\"><a name =\"2.3.1 Class locations\"><font color=\"black\">2.3.1 Class locations</a></font></p>");
            this.writer.writeTableofContentsSubSubEntry("2.3.1 Class locations");
            this.writer.appendOutput("<br><p>The following list tries to give some more information about where the different classes are located on the SOM. "
                    + "Therefore, the second column contains the weight values the mean vector of all input items within the class would have. The next column "
                    + "states the unit onto which this vector would be mapped. This unit is marked in the first image with the circle. The next column contains the "
                    + "coordinates of the \"mean unit\" of the class. This is the unit one obtains not by calculcating the mean value over the weight vectors of the "
                    + "input items, but over the coordinates of the units they are mapped to. This unit is denoted by a square in the first image. The last "
                    + "column before the images gives the standard derivaton to the \"mean unit\" (in units). This value "
                    + "is used to describe an area within the majority of input items belonging to this class are mapped (visualized by the shape of a rectangle in the first image).<br/>"
                    + "The first image in the next column contains a visualization of the described values, (Note that this visualisation has nothing to do with the trained SOM), whereas the second image shows the real class distribution on the SOM for comparison reasons."
                    + "The third image displays the Visualisation of the Class Entropy. Images 3 & 4 are also displayed seperatly for each class.</p>"
                    + " The Class with the largest Entropy Error is "
                    + this.dataset.getNameOfClass(this.testrun.getMaximumEEClassIndex())
                    + ", having an Entropy Error of "
                    + Math.round(this.testrun.getClassEntropy(this.testrun.getMaximumEEClassIndex()))
                    + "."
                    + " The Region with the largest Entropy  Error contains the classes ");
            for (int i = 0; i < this.testrun.getMaxEErrorRegionNames().length; i++) {
                this.writer.appendOutput(this.testrun.getMaxEErrorRegionNames()[i][0] + "  ("
                        + this.testrun.getMaxEErrorRegionNames()[i][1] + ") , ");
            }
            this.writer.appendOutput("having an Entropy Error of " + this.testrun.getRegionEntropyError(1) + ".  "
                    + String.format("%.3f", this.testrun.getPercOfZeroEntropyRegions())
                    + "% of the  regions have an Entropy error of 0.");

            /* Embed Images.. */
            this.writer.appendOutput("<table border=\"1\"><tbody>" + "<tr>" + "<th>class name</th>"
                    + "<th>mean vector</th>" + "<th>mapped to unit</th>" + "<th>\"mean unit\"</th>" + "<th>radius</th>"
                    + "<th>Values by Coordinates</th>" + "<th><img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_"
                    + this.testrun.getRunId() + "_classDistribution.jpg\" alt=\"class distribution of som "
                    + this.testrun.getRunId() + "\"/></th>" + "<th>Class Entropy </th><th>Other classes in Area</th>"
                    + "<th><img src =\"" + SOMDescriptionHTML.imgSubdir + "/run_" + this.testrun.getRunId()
                    + "_classEntropy.jpg\" alt=\"class entropy of som " + this.testrun.getRunId() + "\"/></th>"
                    + "</tr>");
            for (int i = 0; i < this.dataset.getNumberOfClasses(); i++) {
                double[] mean = this.dataset.getClassMeanVector(i);
                String meanS = "";
                for (double element : mean) {
                    meanS += String.format("%.3f", element) + "<br/>";
                }
                int[] meanMapped = this.testrun.getMappedUnit(mean);
                int[] meanUnit = this.testrun.getClassMeanUnit(i);
                String coords = meanMapped[0] + "," + meanMapped[1];
                this.testrun.visualizeClassLayout(meanUnit, meanMapped, i, this.imgDir);
                this.writer.appendOutput("<tr><td>" + this.dataset.getNameOfClass(i) + "</td>");
                this.writer.appendOutput("<td class=\"middleText\">" + meanS + "</td>");
                this.writer.appendOutput("<td class=\"middleText\">" + coords + "</td>");
                this.writer.appendOutput("<td class=\"middleText\">" + meanUnit[0] + "," + meanUnit[1] + "</td>");
                this.writer.appendOutput("<td class=\"middleText\">" + meanUnit[2] + "," + meanUnit[3] + "</td>");
                this.writer.appendOutput("<td class=\"middleText\"><img src=\"" + SOMDescriptionHTML.imgSubdir
                        + "/run_" + this.testrun.getRunId() + "_classCenter_" + i
                        + ".jpg\" alt=\"class center of class " + this.dataset.getNameOfClass(i) + "\"/></td>"
                        + "<td><img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_" + this.testrun.getRunId()
                        + "_classDistribution" + i + ".jpg\" alt=\"class distribution of som "
                        + this.testrun.getRunId() + "\"/></td>" + "<td>" + Math.round(this.testrun.getClassEntropy(i))
                        + "</td>" + "<td><center>");
                String[][] classmix = this.testrun.getClassMix(i);
                for (String[] element : classmix) {
                    this.writer.appendOutput(element[0]);
                    this.writer.appendOutput("&nbsp;:&nbsp;");
                    this.writer.appendOutput(element[1]);
                    this.writer.appendOutput("<br>");
                }
                this.writer.appendOutput("</center></td>" + "<td><img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_"
                        + this.testrun.getRunId() + "_classEntropy" + i + ".jpg\" alt=\"class " + i
                        + " entropy of som " + this.testrun.getRunId() + "\"/></td>");
                this.writer.appendOutput("</tr>");
            }

            this.writer.appendOutput("</tbody></table>");

            this.writer.appendOutput("</div>");
        }

        // the Quality Measure Section
        if (this.EP.getSelectedQualitMeasure().size() > 0) {
            this.printQualityMeasureReport();
        }
        if (this.EP.includeDistPic() || this.EP.includeFlowBorderLinePic() || this.EP.includeSDHVisualization()
                || this.EP.includeTPVisualization() || this.EP.includeTWVisualization()) {
            this.includeAdditionalVisualizations();
        }
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

        this.writer.appendOutput("<p class=\"header\">Distribution of the input vectors" + clsinf + ":</p>");
        this.writer.appendOutput("The following table shows a Hit Histogram of the Map's Units.");
        if (classInfoAvailable) {
            this.writer.appendOutput(" It also shows, how the classes are distributed on the SOM, and where which of "
                    + "the classes are located.<br>");
        }
        this.printDistributionDetailTable(null, classInfoAvailable);
        this.writer.appendOutput("<center><br><i> Tab. 1:Visualisation of the Hit Histogram and the corresponding Pie-chart Class images</i></center>");

        /* SDH Section display */
        int maxs = this.testrun.getGrowingSOM().getLayer().getXSize()
                * this.testrun.getGrowingSOM().getLayer().getYSize();
        if (this.EP.includeSDHVisualization()) {
            String sdh_desc = TextualDescriptionProvider.getScientificDescription("Smoothed Data Histograms");
            this.writer.appendOutput("<br>A more sophisticated way to visualize the distributioun of classes is the <u onclick=\"javascript:showVisualisationDescriptions('"
                    + sdh_desc
                    + "')\"><font color =\"blue\">Smoothed Data Histogramm (SDH)</font></u>. SDHs use a spread Parameter that fixes the amount of Bins being used to  make this Visualization. Differnet Visualizations with different <i>s</i> values can be viewed by clicking on the Buttons.<br><br> "
                    + "<center>");
            int stepSDH = this.EP.getSDHStep() != -1 ? this.EP.getSDHStep() : 2;
            showSDHSwitchBox(maxs, stepSDH);
        }

        // --- then we give some information about the selected input vectors
        if (this.dataset.getNumberOfSelectedInputs() > 0) {
            this.writer.appendOutput("<p class=\"header\"><a name =\"2.2 Location and Quality mesures of selected input items\"><font color=\"black\">2.2 Location and Quality mesures of selected input items:</a></font></p>");
            this.writer.writeTableofContentsSubEntry("2.2 Location and Quality mesures of selected input items");
            this.writer.appendOutput("<p>The following list contains for each selected input item the information onto which unit this item "
                    + "is mapped, and how big theValueof the quality Measure is. After that, this is visualized in another table.</p>");
            // first a list of the selected data items with more information about them:
            this.writer.appendOutput("<ul>\n");

            // we have to somehow map the data items to the units, s.t. we than can walk through the
            // units to retrieve the corresponding data items
            // while doing this, we print a list of all selected items with some additional information
            /*
             * qualifiers.add("Quantization Error"); qualifiers.add("Mean Quantization Error"); qualifiers.add("Intrinsic Distance");
             * qualifiers.add("Topographic Error"); qualifiers.add("Silhouette Value"); qualifiers.add("Distortion Values");
             * qualifiers.add("Entropy Error"); qualifiers.add("Spearman Coefficient"); qualifiers.add("Sammon Measure");
             * qualifiers.add("Metric Multiscaling"); qualifiers.add("Inversion Measure");
             */

            this.writer.appendOutput("<table border=1>");
            this.writer.appendOutput("<tr><th>Input Node</th><th>Coordinates</th><th>Distance to Prototype</th>");
            for (int i = 0; i < this.qualifiers.size(); i++) {
                if (this.qm.getUnitQualities((String) qualifiers.get(i)) != null) {
                    this.writer.appendOutput("<th>" + (String) qualifiers.get(i) + "</th>");
                }
            }
            this.writer.appendOutput("</tr>");

            for (int i = 0; i < this.dataset.getNumberOfSelectedInputs(); i++) {
                this.writer.appendOutput("<tr>");
                input = this.dataset.getInputDatum(this.dataset.getSelectedInputId(i));
                unit = this.testrun.getUnitMappedTo(input);
                key = unit.getXPos() + "_" + unit.getYPos();

                // create the html output for the list
                this.writer.appendOutput("<td>" + input.getLabel() + "</td>"
                        +
                        // where
                        "<td>[" + unit.getXPos() + ", " + unit.getYPos() + "] </td>" + " <td>"
                        + String.format("%.5f", this.testrun.getInputMappedQE(input.getLabel()).getQE()) + "</td>\n");
                // all selected quality measures
                for (int j = 0; j < this.qualifiers.size(); j++) {
                    if (this.qm.getUnitQualities((String) qualifiers.get(j)) != null) {
                        double[][] unitqual = this.qm.getUnitQualities((String) qualifiers.get(j));
                        this.writer.appendOutput("<td>"
                                + String.format("%2f", unitqual[unit.getXPos()][unit.getYPos()]) + "</td>");
                    }
                }

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
                this.writer.appendOutput("</tr>");
            }
            this.writer.appendOutput("</table><br>");

            // print the table with the datasets inserted
            this.writer.appendOutput("<p>The following table visualized the information given in the list above. Values enclosed by \"\" denote "
                    + "the label of a selected input item, the number in the top left corner of each cell gives the number of input items totally mapped to this unit. "
                    + "More information about the quantization error of a unit or the distance of an input to the unit it's mapped to can be retrieved by "
                    + "moving the mouse over an input item (requires JavaScript to be enabled)</p>");
            this.printDistributionDetailTable(lookup, classInfoAvailable);
            this.writer.appendOutput("<br><i> Tab. 2:Visualisation of the Hit Histogram, the corresponding Pie-chart Class images, and detailed information about the input Data</i></center>");
        }
    }

    /**
     * creates and outputs one out of two possible tables the first possible table: <br/>
     * the first table contains for each unit only the number of input vectors mapped to it, and a pie chart image of
     * the class distribution within this unit. (lookup == null) If no class information is available, this table is not
     * created<br/>
     * <br/>
     * The second table additionally contains the ids of input vectors selected by the user within the unit they are
     * mapped to. Therefore lookup must be a hashtable that contains for each unit (key = "x_y") a list of InputDatum
     * objects specifying the input elements mapped to this unit. If for a key there's no list, then nothing is mapped
     * to this unit. if no class information is available, the table does not contain the number of input vectors mapped
     * to each unit
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

        this.writer.appendOutput("<center><table border=\"1\">\n" + "<tbody>\n");

        // as we want to create html output, we first have to iterate about the rows (tr elements)
        for (int y = 0; y < this.testrun.getGrowingSOM().getLayer().getYSize(); y++) {

            this.writer.appendOutput("<tr>\n");

            // and only within these elements about the columns (td elements)
            for (int x = 0; x < this.testrun.getGrowingSOM().getLayer().getXSize(); x++) {
                String img = "";
                String numbDiv = "";
                String mappedInputsDiv = "";
                String content1 = "";

                // FIXME: showing number of input items per unit shouldn't be dependent on pie-chart images...
                // EDIT: it is not, the number of mapped items will be displayed also without a cls file being present
                // the class pie chart and number of input items per unit (as these number is encoded within the pie
                // chart images names
                if (classInformationAvailable) {
                    String prefix = "run_" + this.testrun.getRunId() + "_" + x + "_" + y + "_";
                    String[] matches = pieDir.list(new PrefixFileFilter(prefix)); // lookup whether there's an image
                    if (matches != null && matches.length > 0) {
                        // image exists => insert it
                        String number = matches[0].substring(prefix.length());
                        number = number.substring(0, number.length() - 4);

                        img = "<img " + /* imgStyle+ */" src=\"" + SOMDescriptionHTML.imgSubdir + "/pieCharts_"
                                + this.testrun.getRunId() + "/" + matches[0] + "\" onmouseover=\"showClassInfo(this,"
                                + number + ",event);\" "
                                + "onmouseout=\"hideClassInfoDiv();\" onmousemove=\"positionClassInfoDiv(event);\"/>";
                        numbDiv = "<div style=\"position:absolute;"/* +numberStyle */+ "\">" + number + "</div>";
                        content1 = "<div class=\"upperDiv\">" + numbDiv + img + "</div>";
                    }
                } else {
                    int count = this.testrun.getNumberOfInputsOnUnit(x, y);
                    if (count > 0) {
                        numbDiv = "<div>" + count + "</div>";
                        content1 = "<div class=\"upperDiv\">" + numbDiv + "</div>";
                    }
                }
                // the mapped input vectors
                if (lookup != null) {
                    String key = x + "_" + y;
                    Vector<InputDatum> mapped = lookup.get(key);
                    if (mapped != null) {
                        mappedInputsDiv = "<div class=\"mappedInputsDiv\">";

                        for (int i = 0; i < mapped.size(); i++) {
                            Unit unit = this.testrun.getUnitMappedTo(mapped.get(i));
                            mappedInputsDiv += "<div class=\"mappedInputDiv\""
                                    + "onmouseover=\"showInputInfo(this,'"
                                    + String.format("%.5f", this.testrun.getQEForUnit(unit.getXPos(), unit.getYPos()))
                                    + "','"
                                    + String.format("%.5f", this.testrun.getMQEForUnit(unit.getXPos(), unit.getYPos()))
                                    + "','"
                                    + String.format("%.5f",
                                            this.testrun.getInputMappedQE(mapped.get(i).getLabel()).getQE()) + "','"
                                    + "[" + unit.getXPos() + ", " + unit.getYPos() + "]',event);\" "
                                    + "onmouseout=\"hideInputInfoDiv();\" "
                                    + "onmousemove=\"positionInputInfoDiv(event);\"" + "/>\""
                                    + mapped.get(i).getLabel() + "\"</div>";
                        }
                        mappedInputsDiv += "</div>";
                    }
                }
                String content = content1 + mappedInputsDiv;

                // if there's an empty unit (if no unit => show noting)
                if (content.length() == 0 && this.testrun.hasUnitOn(x, y)) {
                    content = "&nbsp;";
                }

                // write the table cell
                this.writer.appendOutput("<td class=\"classTableTD\">" + content + "</td>\n");
            }// for x
            this.writer.appendOutput("</tr>\n");
        }// for y
        this.writer.appendOutput("</tbody>\n" + "</table></center>\n");

        /*
         * if(lookup != null){ this.writer.appendOutput("(The number in the top left corner of each cell (the only one above the pie chart) denots the
         * total number of input values mapped to the according unit.)"); }
         */
    }

    /**
     * Adds a silouette Report
     */
    /*
     * protected void printSilouetteReport() { try{ this.testrun.fillQMContainer(); }catch(QualityMeasureNotFoundException e){ } UnitQEContainer min;
     * UnitQEContainer max; String unitCoord; //double maxv = this.testrun.getMaxUnitSilouette().getQE(); if (maxv == 0) {
     * this.writer.appendOutput("There is no Silouette Property available on the map!"); return; } this.writer.appendOutput("<ul>\n"); // map
     * topographic error String silouette_desc = this.testrun.getScientificDescription("silouette");
     * this.writer.appendOutput("<li><span class=\"header2\">Soluette Values of the som:</span><ul>");
     * this.writer.appendOutput("<li>Silhoutte Value of the map:" + String.format("%.5f", this.testrun.getMapSilouette()) + "</li>\n");
     * this.writer.appendOutput("</ul></li>"); // min unit/max unit top. error
     * this.writer.appendOutput("<li><span class=\"header2\">Silhouette Unit Values:</span><ul>"); min = this.testrun.getMinUnitSilouette(); unitCoord
     * = getUnitCoords(min); this.writer.appendOutput("<li>Number of units without silouette values: " + this.testrun.getNumberOfSilouettelessUnits()
     * + "</li>"); this.writer.appendOutput("<li>Minimal Silhouette Value of a unit : " + String.format("%.2f", min.getQE()) + " (" + unitCoord + ")"
     * + "</li>\n"); // max = this.testrun.getMaxUnitSilouette(); int max = 7; unitCoord = getUnitCoords(max);
     * this.writer.appendOutput("<li>Maximal Silhouette Value of a unit: " + String.format("%.2f", max.getQE()) + " (" + unitCoord + ")" + "</li>\n");
     * this.writer.appendOutput("</ul></li>\n"); this.writer.appendOutput("</ul>"); if (this.testrun.createSilouetteImage(this.imgDir,
     * "Silouette.jpg")) { this.writer.appendOutput("<p class=\"header\">visualization of the Silouette</p>");
     * //this.writer.appendOutput("<p>This image shows the topographic error on the SOM. Dark areas mark regions of the SOM with high topographic error, "
     * // + "lighter regions indicate less or no topographic error.</p>"); this.writer.appendOutput("<img src=\"" + SOMDescriptionHTML.imgSubdir +
     * "/run_" + this.testrun.getRunId() + "_Silouette.jpg\" alt=\"visualizatin of the silouette\"/>"); } }
     */

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
            this.writer.appendOutput("There is no topographic error on the map!");
            return;
        }

        this.writer.appendOutput("<ul>\n");

        // map topographic error
        String tErr_desc = TextualDescriptionProvider.getScientificDescription("Topographic Error");
        this.writer.appendOutput("Next we will look at the <u onclick=\"javascript:showVisualisationDescriptions('"
                + tErr_desc + "')\"><font color =\"blue\">Topographic Error</font></u> of the SOM.<br><br>");
        this.writer.appendOutput("<li><span class=\"header2\">Topographic error of the som:</span><ul>");

        this.writer.appendOutput("<li>Topographic error of the map:" + String.format("%.5f", this.testrun.getMapTE())
                + "</li>\n");
        this.writer.appendOutput("</ul></li>");

        // min unit/max unit top. error
        this.writer.appendOutput("<li><span class=\"header2\">Unit topographic errors:</span><ul>");
        min = this.testrun.getMinUnitTE();
        unitCoord = getUnitCoords(min);
        this.writer.appendOutput("<li>Number of units without topographic error: "
                + this.testrun.getNumberOfTElessUnits() + "</li>");
        this.writer.appendOutput("<li>Minimal topographic error >0 of a unit : " + String.format("%.2f", min.getQE())
                + " (" + unitCoord + ")" + "</li>\n");
        max = this.testrun.getMaxUnitTE();
        unitCoord = getUnitCoords(max);
        this.writer.appendOutput("<li>Maximal topographic error of a unit: " + String.format("%.2f", max.getQE())
                + " (" + unitCoord + ")" + "</li>\n");
        this.writer.appendOutput("</ul></li>\n");

        this.writer.appendOutput("</ul>");

        if (this.testrun.createTopographicErrorImage(this.imgDir, "topErr.jpg")) {
            this.writer.appendOutput("<p class=\"header\">visualization of the topographic error:</p>");
            this.writer.appendOutput("<p>This image shows the topographic error on the SOM. Dark areas mark regions of the SOM with high topographic error, "
                    + "lighter regions indicate less or no topographic error.</p>");
            this.writer.appendOutput("<img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_" + this.testrun.getRunId()
                    + "_topErr.jpg\" alt=\"visualizatin of top. error\"/>");
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
                unitCoord += ", ";
            }
            unitCoord += "[" + value.getUnitCoords(i) + "] - " + value.getNumberOfVectorsMapped(i) + " vectors mapped";
        }
        if (value.getNumUnits() > 1) {
            unitCoord = "on units at " + unitCoord;
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
            inputCoord += "\"" + value.getInputLabel(i) + "\" on unit at [" + value.getUnitCoords(i) + "]";
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

        this.writer.appendOutput("<h3><a name =\"4.0 Clusters on the SOM\" ><font color=\"black\">4.0 Clusters on the SOM:</a></font></h3>");
        this.writer.appendOutput("<div class=\"infoBlock\">");
        this.writer.writeTableofContentsEntry("4.0 Clusters on the SOM");

        // an image of the UMatrix of the SOM
        if (this.testrun.createUDMatrixImage(this.imgDir, "umatrix.jpg", 30, 0)
                && this.testrun.createUDMatrixImage(this.imgDir, "dmatrix.jpg", 30, 1)) {
            String uMatrix_desc = TextualDescriptionProvider.getScientificDescription("U-Matrix");
            this.writer.appendOutput("<p class=\"header\"><u onclick=\"javascript:showVisualisationDescriptions('"
                    + uMatrix_desc + "')\"><font color =\"blue\">The U-Matrix visualization</font></u></p>");
            this.writer.appendOutput("This image visualizes the the distances between neighboured units of the SOM, and therefore shows quite well how"
                    + " similar input items are, and where they are more different. Light, green areas on the map indicate small distances between units, whereas"
                    + "dark, red areas indicate bigger distances.<br>");
            /* Say if the U-matrix visualization is correct */
            if (this.testrun.getGrowingSOM().getLayer().getXSize() * this.testrun.getGrowingSOM().getLayer().getYSize() < this.dataset.getNumberOfInputVectors()) {
                this.writer.appendOutput("Since the number of prototype Vectors is smaller than the number of Training Samples, the U-Matrix Visualization contains no artifacts.");
            } else {
                this.writer.appendOutput("Since the number of prototype Vectors is larger than the number of Training Samples, the U-Matrix Visualization contains artifacts around the positions, where data samples are mapped.");
            }

            this.writer.appendOutput("<br><br><center><img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_"
                    + this.testrun.getRunId() + "_umatrix.jpg\" alt=\"umatrix visualization\"/>");
            this.writer.appendOutput("<br>");
            this.writer.appendOutput("<img src=\"" + SOMDescriptionHTML.imgSubdir
                    + "/palette_17.jpg\" alt=\"umatrix palette visualization\"/></center>");
            String dMatrix_desc = TextualDescriptionProvider.getScientificDescription("D-Matrix");
            this.writer.appendOutput("<br>" + "<u onclick=\"javascript:showVisualisationDescriptions('" + dMatrix_desc
                    + "')\"><font color =\"blue\">The D-Matrix visualization</font></u>");
            this.writer.appendOutput("<br><br><center><img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_"
                    + this.testrun.getRunId() + "_dmatrix.jpg\" alt=\"dmatrix visualization\"/>");
            this.writer.appendOutput("<br>");
            this.writer.appendOutput("<img src=\"" + SOMDescriptionHTML.imgSubdir
                    + "/palette_17.jpg\" alt=\"umatrix palette visualization\"/></center>");
            // guiaguia;
        }

        ClusterNode node = this.testrun.getClusterNodes(1, 1); // the root node of the cluster Tree

        // print the cluster tree
        this.writer.appendOutput("<p class=\"header\"><a name =\"4.1 The cluster tree\" ><font color=\"black\">4.1 The cluster tree:</a></font></p>");
        this.writer.appendOutput("The following is a representation of the cluster tree. For each cluster (=node of the tree), its level (that "
                + "is the number of the step in which the node is splitted) is given, as well as the number of steps for that the node isn't splitted into two subclusters. By "
                + "clicking onto a node, the subtree rooted in this node can be expanded/collapsed. (at the beginning, the whole tree is collapsed)");
        this.writer.appendOutput("<ul>\n");
        this.writer.writeTableofContentsSubEntry("4.1 The cluster tree");
        int somDim = this.testrun.getGrowingSOM().getLayer().getXSize()
                * this.testrun.getGrowingSOM().getLayer().getYSize();

        /*
         * as we have no possibility to retrieve the depth of a node within the tree, given the node efficiently, and because we are netherless
         * traversing the tree once, we save for all node their depths. this is done in the nodeInfos array at the indices 2 ... n+1 (for node 1 ...
         * n). on position 0, the level of the node that is stable for the most number of steps, and on position 1, the according number of steps is
         * saved
         */
        int[] nodeInfos = new int[somDim + 2];
        nodeInfos[0] = 0;
        nodeInfos[1] = 0;
        nodeInfos = this.printClusterNode(node, 1, somDim, 1, nodeInfos);
        this.writer.appendOutput("</ul>\n");
        this.writer.appendOutput("max stable is level " + nodeInfos[1] + " for " + nodeInfos[0] + " new clusters");

        // print the top ten list and then the visualizations of it:

        // our second (and final) method
        Vector<double[]> stableList2 = this.testrun.getStableClusters2(node, 0.0, somDim, new Vector<double[]>());
        this.writer.appendOutput("<p class=\"header\">Top ten stable clusters:</p>");
        this.writer.appendOutput("The following list contains the 10 most stable clusters (selected according to a simple heurstic). For each cluster,"
                + " the number of input items lying in this cluster is given. Also how the classes are distributed within the cluster. Also, for each cluster "
                + "a suggested name is displayed. A bigger image of the cluster can be retrieved by clicking onto the cluster image.");
        this.writer.appendOutput("<table><tbody>");
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
            this.writer.appendOutput("<tr><td><ul><li>cluster " + label + "<br/> on  level " + stableList2.get(i)[1]
                    + " with gain = " + String.format("%.4f", stableList2.get(i)[0]) + "<br/>"
                    + this.testrun.getClusterNodes((int) stableList2.get(i)[1], 1).getNumberOfInputs()
                    + " inputs in cluster<br/>");

            // + showSemanticClusterInterpretation((int) stableList2.get(i)[1]));

            // classDistInCluster((int) stableList2.get(i)[1],this.testrun.getClusterNodes((int) stableList2.get(i)[1],
            // 1).getNumberOfInputs()));
            // UPDATE MAZ
            if (this.dataset.classInfoAvailable()) {
                this.writer.appendOutput(createClusterReport((int) stableList2.get(i)[1]));
            }
            // END
            this.writer.appendOutput("<td>");
            this.testrun.visualizeCluster((int) stableList2.get(i)[1], this.imgDir, nodeInfos, 50);
            this.testrun.visualizeCluster((int) stableList2.get(i)[1], this.imgDir, nodeInfos, 10);
            this.writer.appendOutput("<a href=\"" + SOMDescriptionHTML.imgSubdir + "/run_" + this.testrun.getRunId()
                    + "_clusterLevel_" + (int) stableList2.get(i)[1] + "_50.jpg" + "\"><img src=\""
                    + SOMDescriptionHTML.imgSubdir + "/run_" + this.testrun.getRunId() + "_clusterLevel_"
                    + (int) stableList2.get(i)[1] + "_10.jpg\" alt=\"\"/></a>");
            this.writer.appendOutput("</li></ul></td></tr>");
        }
        this.writer.appendOutput("</tbody></table>");

        /*
         * this.writer.appendOutput("<p class=\"header\">visualization of top ten clusters:</p>"); for(int i = 0; i < 10; i++){ if( &&
         * this.testrun.visualizeCluster((int)stableList2.get(i)[1], this.imgDir, nodeInfos, 10)){ this.writer.appendOutput("<p class=\"header2\"><a
         * name=\"clusterVis_"+i+"\">level "+ stableList2.get(i)[1] +"</a><br/>" + "<img
         * src=\""+SOMDescriptionHTML.imgSubdir+"/run_"+this.testrun.getRunId()+"_clusterLevel_"+(int)stableList2.get(i)[1]+".jpg\" alt=\"\"/>" +
         * "</p>"); } }
         */
        this.writer.appendOutput("</div>");

        /*
         * I also tried this .... //our first method - didn't work quie well, for more information, please see TestRunRest.getStableClusters1()
         * Vector<double[]> stableList1 = this.testrun.getStableClusters1(node, 1, somDim, new Vector<double[]>()); this.writer.appendOutput("top ten
         * stable clusters:"); this.writer.appendOutput("<ul>"); for(int i = 0; i < stableList1.size(); i++){ this.writer.appendOutput("<li>level "+
         * stableList1.get(i)[1] +" with " + stableList1.get(i)[0] + "</li>"); } this.writer.appendOutput("</ul>"); //our third method - didn't work
         * quie well, for more information, please see TestRunRest.getStableClusters13() Vector<double[]> stableList3 =
         * this.testrun.getStableClusters3(node, 0.0, somDim, new Vector<double[]>()); this.writer.appendOutput("top ten stable clusters:");
         * this.writer.appendOutput("<ul>"); for(int i = 0; i < stableList3.size(); i++){ this.writer.appendOutput("<li>level "+ stableList3.get(i)[1]
         * +" with " + stableList3.get(i)[0] + "</li>"); } this.writer.appendOutput("</ul>");
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
            // out = "<div onlclick=\"showClassesInCluster(classes);\">";
            out = "<a href=\"javascript:showClassesInCluster(" + level + "," + this.testrun.getRunId() + ")\">"
                    + numbClasses + " classes in cluster:</a> ";

        } else {
            // out = "<div onlclick=\"showClassesInCluster("+level+");\">";
            out = "<a href=\"javascript:showClassesInCluster(" + level + "," + this.testrun.getRunId() + ")\">"
                    + numbClasses + " class in cluster:</a> ";

        }

        if (numbClasses > 0) {
            out += "<ul id =\"classes_" + level + "_" + this.testrun.getRunId() + "\" style=\"display:none\">";
            for (int i = 0; i < classDist.length; i++) {
                if (classDist[i] == 0) {
                    continue;
                }

                out += "<li>"
                        + this.dataset.getNameOfClass(i)
                        + ": "
                        + classDist[i]
                        + " inputs ("
                        + String.format("%.2f", (double) classDist[i] / (double) numbInputs * 100)
                        + "%) = "
                        + String.format("%.2f", (double) classDist[i]
                                / (double) this.dataset.getNumberOfClassmembers(i) * 100) + "% of class members"
                        + "</li>";
            }
            out += "</ul>";
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
    protected int[] printClusterNode(ClusterNode node, int parentLevel, int boundary, int depth, int[] nodeInfos) {

        // stop recursion if we are deep enough
        if (node.getLevel() > boundary) {
            return nodeInfos;
        }

        CommonSOMViewerStateData state = CommonSOMViewerStateData.getInstance(); // we need this for clustering
        ClusterNode lc = node.getChild1();
        ClusterNode rc = node.getChild2();
        int curStable = node.getLevel() - parentLevel - 1;
        if (curStable > nodeInfos[0]) {
            nodeInfos[0] = curStable;
            nodeInfos[1] = node.getLevel();
        }
        nodeInfos[node.getLevel() + 1] = depth;

        // itself
        Vector<String> labels = this.dataset.getClusterName(node, (int) state.clusterByValue, depth);

        this.writer.appendOutput("<li>");
        this.writer.appendOutput("<div onclick=\"swapClusterDisp(" + node.getLevel() + ");\">");
        this.writer.appendOutput("<b>level " + node.getLevel() + "</b> ");
        if (labels.size() > 0) {
            this.writer.appendOutput("(" + labels.get(0) + ")");
        }
        this.writer.appendOutput(" stable for: " + curStable);
        this.writer.appendOutput("</div>");
        this.writer.appendOutput("<div id=\"clusterNode_" + node.getLevel() + "\" style=\"display:none;\">");

        // recursive decent
        if (lc.getLevel() + 1 <= boundary) {
            // first child
            this.writer.appendOutput("<ul>");
            nodeInfos = this.printClusterNode(lc, node.getLevel(), boundary, depth + 1, nodeInfos);
            this.writer.appendOutput("</ul>");
        }

        if (rc.getLevel() + 1 <= boundary) {
            // second Child
            this.writer.appendOutput("<ul>");
            nodeInfos = this.printClusterNode(rc, node.getLevel(), boundary, depth + 1, nodeInfos);
            this.writer.appendOutput("</ul>");
        }
        this.writer.appendOutput("</div");
        this.writer.appendOutput("</li>");
        return nodeInfos;
    }

    /**
     * Generate teh sdh switch box for all possible values of s: create pics, and bind them to the page
     */
    public void showSDHSwitchBox(int maxs, int step) {
        int runID = this.testrun.getRunId();
        int maxAnzahl = -1;
        for (int i = 1; i < maxs; i += step) {
            maxAnzahl = i;
        }
        this.testrun.createSDHImages(this.imgDir, "SDH_Visualization.jpg", 30, false, step);
        this.writer.appendOutput("<br>");
        this.writer.appendOutput("<script language\"JavaScript\"  type =\"text/javascript\">" + /* Initialize the SDH picture pointer array */
        "initSDH();" + "setMaxAnzahlSDH(" + maxAnzahl + ");\n" + "</script>");
        for (int i = 1; i < maxs; i += step) {
            if (i == 1) {
                this.writer.appendOutput("<div id=\"sdh_" + runID + "_" + i + "\" style=\"display:block\">");
            } else {
                this.writer.appendOutput("<div id=\"sdh_" + runID + "_" + i + "\" style=\"display:none\">");
            }
            this.writer.appendOutput("<img src=\"" + SOMDescriptionHTML.imgSubdir + "/" + "SDH_pics_"
                    + this.testrun.getRunId() + "/run_" + this.testrun.getRunId() + "_s" + i
                    + "_SDH_Visualization.jpg\" alt=\"SDH Visualisation of som with k = " + i + "\">"
                    + "<br>Visualization 1:SDH Image with <i>s</i> = " + i + "</div>");
        }
        this.writer.appendOutput("<input type=\"button\" name=\"<<\" value=\"<<\" onclick=\"javascript:prevSDH("
                + this.testrun.getRunId() + "," + step + ")\">"
                + "&nbsp;&nbsp;&nbsp;<input type=\"button\" name=\">>\" value=\">>\" onclick=\"javascript:nextSDH("
                + this.testrun.getRunId() + "," + step + ")\">" + "&nbsp;&nbsp;&nbsp</center>");
    }

    /**
     * Generate teh TrustworthyNessImages switch box for all possible values of s: create pics, and bind them to the
     * page
     */
    public void showTrustworthyNessSwitchBox(int maxs, int step) {
        int runID = this.testrun.getRunId();
        int maxAnzahl = -1;
        for (int i = 1; i < maxs; i += step) {
            maxAnzahl = i;
        }
        this.testrun.createTrustworthyNessImages(this.imgDir, "TrustworthyNessImages_Visualization.jpg", 30, false,
                step);
        this.writer.appendOutput("<br>");
        this.writer.appendOutput("<script language\"JavaScript\"  type =\"text/javascript\">" + /*
                                                                                                 * Initialize the ToporaphicProduct picture pointer
                                                                                                 * array
                                                                                                 */
        "initTW();" + "setMaxAnzahlTW(" + maxAnzahl + ");\n" + "</script>");
        for (int i = 1; i < maxs; i += step) {
            if (i == 1) {
                this.writer.appendOutput("<div id=\"tw_" + runID + "_" + i + "\" style=\"display:block\">");
            } else {
                this.writer.appendOutput("<div id=\"tw_" + runID + "_" + i + "\" style=\"display:none\">");
            }
            this.writer.appendOutput("<img src=\""
                    + SOMDescriptionHTML.imgSubdir
                    + "/"
                    + "TrustworthyNessImages_pics_"
                    + this.testrun.getRunId()
                    + "/run_"
                    + this.testrun.getRunId()
                    + "_tw_"
                    + i
                    + "TrustworthyNessImages_Visualization.jpg\" alt=\"TrustworthyNessImages Visualisation of som with k = "
                    + i + "\">" + "<br>Visualization 1:TrustworthyNessImages Image with <i>k</i> = " + i + "</div>");
        }
        this.writer.appendOutput("<input type=\"button\" name=\"<<\" value=\"<<\" onclick=\"javascript:prevTW("
                + this.testrun.getRunId() + "," + step + ")\">"
                + "&nbsp;&nbsp;&nbsp;<input type=\"button\" name=\">>\" value=\">>\" onclick=\"javascript:nextTW("
                + this.testrun.getRunId() + "," + step + ")\">" + "&nbsp;&nbsp;&nbsp</center><br>");
    }

    /**
     * Generate teh TopographicProduct switch box for all possible values of s: create pics, and bind them to the page
     */
    public void showTopographicProductSwitchBox(int maxs, int step) {
        int runID = this.testrun.getRunId();
        int maxAnzahl = -1;
        for (int i = 1; i < maxs; i += step) {
            maxAnzahl = i;
        }
        this.testrun.createTopographicProductImages(this.imgDir, "TopographicProduct_Visualization.jpg", 30, false,
                step);
        this.writer.appendOutput("<br>");
        this.writer.appendOutput("<script language\"JavaScript\"  type =\"text/javascript\">" + /*
                                                                                                 * Initialize the ToporaphicProduct picture pointer
                                                                                                 * array
                                                                                                 */
        "initTP();\n" + "setMaxAnzahlTP(" + maxAnzahl + ");\n" + "</script>");
        for (int i = 1; i < maxs; i += step) {
            if (i == 1) {
                this.writer.appendOutput("<div id=\"tp_" + runID + "_" + i + "\" style=\"display:block\">");
            } else {
                this.writer.appendOutput("<div id=\"tp_" + runID + "_" + i + "\" style=\"display:none\">");
            }
            this.writer.appendOutput("<img src=\"" + SOMDescriptionHTML.imgSubdir + "/" + "TopographicProduct_pics_"
                    + this.testrun.getRunId() + "/run_" + this.testrun.getRunId() + "_tp_" + i
                    + "TopographicProduct_Visualization.jpg\" alt=\"TopographicProduct Visualisation of som with k = "
                    + i + "\">" + "<br>Visualization 1:TopographicProduct Image with <i>k</i> = " + i + "</div>");
        }
        this.writer.appendOutput("<input type=\"button\" name=\"<<\" value=\"<<\" onclick=\"javascript:prevTP("
                + this.testrun.getRunId() + "," + step + ")\">"
                + "&nbsp;&nbsp;&nbsp;<input type=\"button\" name=\">>\" value=\">>\" onclick=\"javascript:nextTP("
                + this.testrun.getRunId() + "," + step + ")\">" + "&nbsp;&nbsp;&nbsp</center><br>");
    }

    /**
     * Creates a semantic Report on teh SOM
     */
    public void printRegionalOverviewReport() {
        Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").info("Making Regional Overview Report");
        this.writer.appendOutput("<p class=\"header\"><a name =\"5.0 The Regions of the SOM\" ><font color=\"black\">5.0 The Regions of the SOM</a></font></p><br><br>");
        this.writer.writeTableofContentsEntry("5.0 The Regions of the SOM");
        this.writer.appendOutput("The Grid Body of the SOM is segmented in 9 Regions,according to their geographical Location. There are 4 Sections, one for every Quadrant, 4 Regions that act as intersecting Regions between the Quadrants, and one centered Region.");
        this.writer.appendOutput("The following Info Table is meant for a specific deeper look into classes in the distincitve Areas of the SOM.<br>");
        if (EP.getMINCompactness() != -1 || EP.getMAXCompactness() != -1) {
            this.writer.appendOutput(" According to the specified User input Preferences, only those classes are listed, that match the folowing conditions: <br>");
        }
        if (EP.getMINCompactness() != -1) {
            this.writer.appendOutput("<li>Min. Compactness:" + EP.getMINCompactness() + " %.</li>");
        }
        if (EP.getMAXCompactness() != -1) {
            this.writer.appendOutput("<li>Max. Compactness:" + EP.getMAXCompactness() + " %.</li>");
        }
        for (int i = 0; i < SemanticInterpreterGrid.NUMBER_OF_REGIONS; i++) {
            this.writer.appendOutput("<b>" + SemanticInterpreterGrid.getRegion(i + 1) + "</b><br><br>");

            if (this.EP.includeSemanticReport()) {
                this.writer.appendOutput(sGrid.createQMComparisonReportonGrid(null, i + 1) + "<br>");
            }
            if (EP.includeRegionReport()) {
                if (this.dataset.classInfoAvailable()) {
                    this.writer.appendOutput(sGrid.createClassReportOnGrid(i + 1, 0, null) + "<br><br>");
                } else {
                    this.writer.appendOutput(" A Regional Class Report could not be generated, since a (*.cls) File was missing during creation of this report.");
                }
            }
        }

    }

    /**
     * Creates a semantic Report on the Given Clusterlevel
     * 
     * @param clusterlevel the clusterlevel
     * @return the report
     */
    public String createClusterReport(int clusterlevel) {
        String out = "";
        Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").info("Making Clusterlevel Report");
        ClusterNode node = this.testrun.getClusterTree().findNode(clusterlevel);
        GeneralUnitPNode[] Punits = node.getNodes();
        Unit[] units = new Unit[Punits.length];
        for (int i = 0; i < units.length; i++) {
            units[i] = Punits[i].getUnit();
        }
        units = SemanticInterpreterGrid.sortNodes(units);
        SemanticInterpreterGrid g = new SemanticInterpreterGrid(units, this.testrun, false, 0, 1, this.imgDir);
        g.setFatherGrid(sGrid);
        g.initializeGridwithClasses(false);
        if (this.EP.includeClusterReport()) {
            if (EP.getMINCompactness() != -1 || EP.getMAXCompactness() != -1) {
                out += " According to the specified User input Preferences, only those classes are listed, that match the folowing conditions: <br>";
            }
            if (EP.getMINCompactness() != -1) {
                out += "<li>Min. Compactness:" + EP.getMINCompactness() + " %.</li>";
            }
            if (EP.getMAXCompactness() != -1) {
                out += "<li>Max. Compactness:" + EP.getMAXCompactness() + " %.</li>";
            }
            out += g.createClassReportOnGrid(0, clusterlevel, null);
            if (this.EP.includeSemanticReport()) {
                out += g.createQMComparisonReportonGrid(null, 0);
            }
        }

        return out;
    }

    /**
     * Prints a report opn all selected qualitymeasures
     */
    public void printQualityMeasureReport() {
        this.writer.appendOutput("<div class=\"infoBlock\">");
        this.writer.appendOutput("<p class=\"header\"><h3><a name =\"3.0 Quality Measures of the SOM\" ><font color=\"black\">3.0 Quality Measures of the SOM:</a></font></h3></p>"
                + "<br>The next section gives an overview over the Quality Measures of the SOM to describe its Goodess.");
        this.writer.writeTableofContentsEntry("3.0 Quality Measures of the SOM");
        this.writer.appendOutput("Quality Measure Values were classified in 5 classes:" + this.qm.getClassIdentifier(5)
                + ", " + this.qm.getClassIdentifier(4) + ", " + this.qm.getClassIdentifier(3) + ", "
                + this.qm.getClassIdentifier(2) + " and " + this.qm.getClassIdentifier(1) + " Values.");
        for (int i = 0; i < qualifiers.size(); i++) {
            this.writer.appendOutput("<p class=\"header\"><h3><a name =\"3." + (i + 1) + " "
                    + (String) qualifiers.get(i) + "\"><font color=\"black\">3." + (i + 1) + " "
                    + (String) qualifiers.get(i) + ":</a></h3></font></p>");
            this.writer.writeTableofContentsSubEntry("3." + (i + 1) + " " + (String) qualifiers.get(i));
            this.writer.appendOutput(this.sGrid.printQualityMeasureReport((String) qualifiers.get(i)));

            if (((String) qualifiers.get(i)).equals("Quantization Error")) {
                InputQEContainer mini;
                InputQEContainer maxi;
                this.writer.appendOutput("Quantization errors of input vectors:");
                mini = this.testrun.getMinInputMappedQE();
                this.writer.appendOutput("Minimal (mapped) quantization error of input vector: "
                        + String.format("%.5f", mini.getQE()) + getInputCoords(mini) + "\n");
                maxi = this.testrun.getMaxInputMappedQE();
                this.writer.appendOutput("\\item Maximal (mapped) quantization error of input vector: "
                        + String.format("%.5f", maxi.getQE()) + getInputCoords(maxi) + "\n");
            }
            this.writer.appendOutput("</div>");
        }
        this.writer.appendOutput("<br>");
    }

    public void includeAdditionalVisualizations() {
        this.writer.appendOutput("<p class=\"header\"><h3><a name =\"3." + (qualifiers.size() + 1)
                + " Additional Visualizations " + "\"><font color=\"black\">3." + (qualifiers.size() + 1)
                + " Additional Visualizations :</a></h3></font></p>");
        this.writer.writeTableofContentsSubEntry("3." + (qualifiers.size() + 1) + " Additional Visualizations");
        this.writer.appendOutput("<br>");
        this.writer.appendOutput("The following Visualizations are being created by combining one or more Quality Measure.<br>");
        this.writer.appendOutput("<br>");

        // *Topographic Product Flow& BorderLine & Distortion Values & Trustowrthyness Switch Boxes*//
        int maxs = this.testrun.getGrowingSOM().getLayer().getXSize()
                * this.testrun.getGrowingSOM().getLayer().getYSize();

        if (this.EP.includeTPVisualization()) {
            String topProduct = TextualDescriptionProvider.getScientificDescription("Topographic Product");
            this.writer.appendOutput("<u onclick=\"javascript:showVisualisationDescriptions('" + topProduct
                    + "')\"><font color =\"blue\">Topographic Product</font></u>");
            this.writer.appendOutput("<center>");
            int stepTP = this.EP.getTPStep() != -1 ? this.EP.getTPStep() : 2;
            showTopographicProductSwitchBox(maxs, stepTP);
        }

        if (this.EP.includeTWVisualization()) {
            String Trustworthy = TextualDescriptionProvider.getScientificDescription("Trustworthyness");
            this.writer.appendOutput("<u onclick=\"javascript:showVisualisationDescriptions('" + Trustworthy
                    + "')\"><font color =\"blue\">Trustworthyness</font></u>");
            this.writer.appendOutput("<center>");
            int stepTW = this.EP.getTWStep() != -1 ? this.EP.getTWStep() : 2;
            showTrustworthyNessSwitchBox(maxs, stepTW);
        }

        if (this.EP.includeFlowBorderLinePic()) {
            this.testrun.createFlowBorderLineImage(this.imgDir, "flowborderline.jpg", 30);
            String flowBorderLine = TextualDescriptionProvider.getScientificDescription("FlowBorderline");
            this.writer.appendOutput("<u onclick=\"javascript:showVisualisationDescriptions('" + flowBorderLine
                    + "')\"><font color =\"blue\">Flow & Borderline</font></u>");
            this.writer.appendOutput("<br><center><img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_"
                    + this.testrun.getRunId()
                    + "_flowborderline.jpg\" alt=\"Flow & BorderLine visualization\"/></center>");
            this.writer.appendOutput("<u onclick=\"javascript:showVisualisationDescriptions('" + flowBorderLine
                    + "')\"><font color =\"blue\">Flow & Borderline</font></u><br>");
        }

        if (this.EP.includeDistPic()) {
            this.testrun.createDistortionImage(this.imgDir, "distortionsqrt.jpg", 30, 0);
            this.testrun.createDistortionImage(this.imgDir, "distortion2nd3rd.jpg", 30, 1);
            String distortion = TextualDescriptionProvider.getScientificDescription("Distortion Values");

            this.writer.appendOutput("2 different Modi exist for this Visualization.");
            this.writer.appendOutput("<br><center><img src=\""
                    + SOMDescriptionHTML.imgSubdir
                    + "/run_"
                    + this.testrun.getRunId()
                    + "_distortionsqrt.jpg\" alt=\"Flow & BorderLine visualization sqrt\"/><br>Distortion is shown by lines between winners that are farther away than sqrt(2)</center><br>");
            this.writer.appendOutput("<br><center><img src=\""
                    + SOMDescriptionHTML.imgSubdir
                    + "/run_"
                    + this.testrun.getRunId()
                    + "_distortion2nd3rd.jpg\" alt=\"Flow & BorderLine visualization 2nd3rd\"/><br>If third-best winner is farther away from the winner than the second-best, a line is drawn.</center><br>");
        }
    }

}
