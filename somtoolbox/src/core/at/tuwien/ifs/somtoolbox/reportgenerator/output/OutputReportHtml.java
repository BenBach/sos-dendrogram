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

import java.io.IOException;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.EditableReportProperties;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResultCollection;
import at.tuwien.ifs.somtoolbox.reportgenerator.TextualDescriptionProvider;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.HTMLUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class generates the Report as HTML output. It holds a reference to an instance of ReportFileWriter, to which all
 * Strings that shall appear in the output are send. This class does only handle how this strings schall look like, and
 * not with any technical detail about how these strings are actually written to a file or elsewhere.
 * 
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @author Rudolf Mayer
 * @version $Id: OutputReportHtml.java 3883 2010-11-02 17:13:23Z frank $
 */
public class OutputReportHtml implements OutputReport {

    /** handles the actual writing of the output. All Strings that shall appear in the output are handed over to it */
    private ReportFileWriter writer;

    private DatasetInformation datasetInformation;

    private TestRunResultCollection testruns;

    private String outputDirPath = "";

    private String imagePath;

    private int type;

    private EditableReportProperties EP = null;

    /**
     * Creates a new Object to finally print the report in HTML
     * 
     * @param outputDir the path to the directory where the files shall be saved to
     */
    public OutputReportHtml(String outputDir, EditableReportProperties EP) {
        outputDir = FileUtils.prepareOutputDir(outputDir);
        this.writer = new ReportFileWriter(outputDir + "index.html", 1);
        this.EP = EP;
        this.outputDirPath = outputDir;
        imagePath = outputDirPath + "images" + System.getProperty("file.separator");
    }

    /**
     * gives an Object to the report writer that stores all information about the used dataset. Should be specified
     * before the createOutput function is called, otherwise no information about the dataset can be reported
     * 
     * @param infoObj object containing all available information about the used dataset
     */
    @Override
    public void setDatasetInformation(DatasetInformation infoObj) {
        this.datasetInformation = infoObj;
    }

    /**
     * gives an Object to the report writer that stores all information about the performed that shall be documented
     * within the report. Should be specified before the createOutput function is called, otherwise no information about
     * the testruns can be reported
     * 
     * @param testruns object containting all available information about the testruns performed
     */
    @Override
    public void setTestrunInformation(TestRunResultCollection testruns, int type) {
        this.testruns = testruns;
        this.type = type;
    }

    /**
     * creates the report about as HTML file. All necessary setXXX functions should be called before this function is
     * called and the output starts.
     */
    @Override
    public void createOutput() {
        this.printReportStart();
        if (this.datasetInformation != null) {
            this.printDatasetReport();
        }
        if (this.testruns != null && this.testruns.getNumberOfRuns() > 0) {
            this.printTestrunsReport();
        }
        this.printReportEnd();
    }

    /**
     * prints the start of the report. includes the page start, title, and references to JS and CSS files. The contents
     * are read from rsc/reportGenerator/pageStart.html
     */
    private void printReportStart() {
        try {
            writer.appendOutput(FileUtils.readFileContents("./src/core/rsc/reportGenerator/pageStart.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * prints the foot of the report this includes any fixed data to be displayed at the end, as well as all markup
     * required to finish and close the report
     */
    private void printReportEnd() {
        this.writer.appendOutput("\n\t</body>\n</html>");
        this.writer.finish();
        this.createScriptAndStyleFiles();
        Logger.getLogger("at.tuwien.ifs.somtoolbox.reportgenerator").info("Finished Report");
    }

    /**
     * outputs the section concerning the dataset Information included in this section are:
     * <ul>
     * <li>number of inputs</li>
     * <li>dimension of inputs</li>
     * <li>information about the data distribution within the different dimensions</li>
     * <li>...</li>
     * </ul>
     * Also, if class information are provided, these are printed. This includes: the number of classes, the number of
     * inputs within each class, ...
     */
    private void printDatasetReport() {
        /* Some inizialisations.. we will need them later */
        // Get the Array with the most important Dims, ordered decreasingly by importance
        // FIXME: don't do PCA with higher dimensions!!
        double[][] dim_array = this.datasetInformation.getPCAdeterminedDims();

        /*
         * Here we calculate how many Dimensions are still "left over" so to say, that is, which ones have not been visualized due to their small
         * importance. Anyway we want to see how many dimensions share what remaining Percantage of the total variance
         */
        double perc = this.datasetInformation.calculateAccumulatedVariance();

        writeHeader("1.0 The Dataset");
        writer.appendOutput("<p>This section describes the dataset that was used for training the SOM, and therefore shall be represented by the SOM.<br/>"
                + "After some general information, a closer look is taken onto each dimension of the input vectors. Input Vectors are chosen according to the "
                + "Results of a <u onclick=\"javascript:showVisualisationDescriptions('"
                + TextualDescriptionProvider.getScientificDescription("Principal Component Analysis")
                + "')\"><font color =\"blue\">PCA</font></u>. Since Variance can be used "
                + "as a measure of Quality in the Data, the corresponding Percentage of the total Variance is"
                + "displayed, to underline the Goodness of the Dimension.");
        if (dim_array.length != datasetInformation.getVectorDim()) { // if the vector had more dimensions than selected
            // by the user
            writer.appendOutput(" The remaining " + (datasetInformation.getVectorDim() - dim_array.length) + " "
                    + "dimensions that are not displayed share an accumulated Variance of "
                    + String.format("%.2f", 100.0 - perc * 100) + " %.");
        }

        if (datasetInformation.classInfoAvailable()) {
            writer.appendOutput("<br/>\nAt the end of this section, some information about the existing classes and how the input items are distributed on them are given.");
        }
        this.writer.appendOutput("</p>");

        // Information about the input data vectors:
        this.writer.appendOutput("<span class=\"header\">Dataset:</span><br/>");
        this.writer.appendOutput("Number of input vectors: " + datasetInformation.getNumberOfInputVectors() + "<br/>\n");
        this.writer.appendOutput("Dimensionality of input vectors: " + datasetInformation.getVectorDim() + "<br/>\n");
        this.writer.appendOutput("Dataset has been normalized: "
                + StringUtils.formatBooleanValue(0, this.datasetInformation.isNormalized()) + "<br/>\n");
        this.writer.appendOutput("Number of Classes: " + this.classNumberString() + "<br/>\n");
        this.writer.appendOutput("</div>");

        this.writer.appendOutput("<div class=\"infoBlock\">");
        this.writer.appendOutput("<span class=\"header\">Attribute details:</span>");

        writer.appendOutput("<table>\n");
        writer.appendOutput(HTMLUtils.printTableHeader("label", "min value", "max value", "mean value", "variance",
                "# of zeros", "discrete (integer) values", "binary values", "% of total variance",
                "Metro Map Visualization"));

        writer.appendOutput("<tbody>\n");
        for (int i = (int) dim_array[0][0], counter = 0; counter < dim_array.length; counter++, i = counter < dim_array.length
                ? (int) dim_array[counter][0] : (int) dim_array[0][0]) {

            testruns.getRun(0).createSingleMetroMapComponentImage(imagePath, i + "_SingleMetroMapComponent.jpg", i);

            writer.appendOutput(HTMLUtils.printTableRow(new String[] { null, "middleText" },//
                    datasetInformation.getAttributeLabel(i), datasetInformation.getNumericalDataProps(
                            DatasetInformation.MIN_VALUE, i), //
                    datasetInformation.getNumericalDataProps(DatasetInformation.MAX_VALUE, i), //
                    datasetInformation.getNumericalDataProps(DatasetInformation.MEAN_VALUE, i),//
                    datasetInformation.getNumericalDataProps(DatasetInformation.VAR_VALUE, i), //
                    datasetInformation.getNumericalDataProps(DatasetInformation.ZERO_VALUE, i)
                            + " ("
                            + String.format("%.2f", datasetInformation.getNumericalDataProps(
                                    DatasetInformation.ZERO_VALUE, i)
                                    / datasetInformation.getNumberOfInputVectors() * 100) + "%)",//
                    datasetInformation.getBoolDataProps(DatasetInformation.DISCRETE, i),//
                    datasetInformation.getBoolDataProps(DatasetInformation.ONLY01, i),//
                    String.format("%.2f", dim_array[counter][1] * 100) + "%",//
                    "<img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_" + testruns.getRun(0).getRunId()
                            + "_component_" + i + "_SingleMetroMapComponent.jpg\"/>",//
                    "&nbsp;"));
        }
        writer.appendOutput("</tbody>\n");
        writer.appendOutput("</table>\n");
        writer.appendOutput("</div>");

        // more detailed class information
        if (this.datasetInformation.getNumberOfClasses() > 0) {
            writeSubHeader("1.0 The Dataset");

            writer.appendOutput("<table>\n");
            writer.appendOutput(HTMLUtils.printTableHeader("Class label", "Number of Classmembers",
                    "% of input belong to", "Class colour used"));
            for (int i = 0; i < this.datasetInformation.getNumberOfClasses(); i++) {
                writer.appendOutput(HTMLUtils.printTableRow(
                        new String[] { null, "middleText" },// 
                        new String[] { null, null, null,
                                "background:" + StringUtils.getRGBString(this.datasetInformation.getClassColorRGB(i)) },//
                        datasetInformation.getNameOfClass(i), //
                        String.format("%.2f", datasetInformation.getClassInfo().getPercentageOfClassMembers(i)) + "%",//
                        "&nbsp;"));
            }
            this.writer.appendOutput("</table>");
            this.writer.appendOutput("</div>");
        }
    }

    private void writeSubHeader(String header) {
        this.writer.writeTableofContentsSubEntry(header);
        this.writer.appendOutput("<span class=\"header\"><a name =\"" + header + "\" ><font color=\"black\">" + header
                + "</a></font>:</span>\n");
        this.writer.appendOutput("<div class=\"infoBlock\">\n");
    }

    private void writeHeader(String header) {
        this.writer.writeTableofContentsEntry(header);
        this.writer.appendOutput("<h2><a name =\"" + header + "\"><font color=\"black\">" + header
                + "</a></h2></font>\n");
        this.writer.appendOutput("<div class=\"infoBlock\">");
    }

    /**
     * first outputs the section containing the results from the different testruns specified, then a short comparison
     * of the testruns (SOM configuration and basic results) is printed. for the output of the sections for the
     * different training runs, this.printReportOnTestrun(run) is used
     */
    private void printTestrunsReport() {

        // header
        if (this.testruns.getNumberOfRuns() > 1) {
            this.writer.appendOutput("<h2><a name =\"2.0 Results of the SOM trainings\" ><font color=\"black\">2.0 Results of the SOM trainings:</a></h2></font>\n");
            this.writer.appendOutput("<p>In this section, details about the different specified trainings and their results are "
                    + "presented. For each training,");
        } else {
            this.writer.appendOutput("<h2><a name =\"2.0 Results of the SOM trainings\"><font color=\"black\">2.0 Results of the SOM trainings:</a></h2></font>\n");
            this.writer.appendOutput("<p>In this section, details about the training and the resuling SOM are "
                    + "presented. Therefore,");

        }
        this.writer.writeTableofContentsEntry("2.0 Results of the SOM trainings");
        this.writer.appendOutput(" first some basic properties describing the training parameters and the resulting "
                + "SOM are given. Then, the distribution of the input data on the trained SOM is described. This includes the quantization and "
                + "topographic errors existing on the map.");
        if (this.datasetInformation.classInfoAvailable()) {
            this.writer.appendOutput("<br/>Together with the distribution of the input data items, also information about how and where the classes"
                    + " are distributed on the map are given.");
        }
        this.writer.appendOutput("<br/>At the end, a selection of 10 clusters (and therefore possibilities to group the unit on the SOM) is described.</p>");

        // first print details about the different runs
        for (int r = 0; r < this.testruns.getNumberOfRuns(); r++) {
            this.printReportOnTestrun(r, this.testruns.getNumberOfRuns() > 1);
        }

        // then print the information retrieved by comparing the runs
        if (this.testruns.getNumberOfRuns() > 1) {
            this.printComparingReport();
        }// otherwise comparing runs would not make any sense
    }

    /**
     * prints the results of/information about one som training and its results. What infromation is actual written to
     * the report depends on the type of som that has been trained, as all kind of SOMs may need different information
     * to be given. In general, first, a list of parameters for the training process and some basic properties of the
     * som are outputted. Then, if a class file has been selected, a table visualizing the class distribution is given.
     * If input vectors has been selected for retrieving information about where they are located, another table storing
     * these information is printed. After some quantization errors, information about the clusters found are written.
     * 
     * @param r the index of the testrun (start counting by 0)
     * @param moreRuns true if the results of more than one training has been spcified, false otherwise
     */
    private void printReportOnTestrun(int r, boolean moreRuns) {

        if (moreRuns) {
            this.writer.appendOutput("<h2>" + (r + 1) + ". trained SOM:</h2>");
        }
        this.writer.appendOutput("<h3> SOM & training properties:</h3>\n");

        // to distinguish the different SOMs, we need its idetinfying String
        String somtype = (String) this.testruns.getProperty(TestRunResultCollection.keyTopology, r);
        SOMDescriptionHTML somdescr = null;

        if (somtype != null && somtype.equals("gg")) {
            // growing grid: seems to be produced by GHSOM if no child map is created
            somdescr = new SOMGGDescriptionHTML(this.writer, this.datasetInformation, this.testruns.getRun(r),
                    this.outputDirPath, this.datasetInformation.getEP());
        } else if (somtype != null && somtype.equals("ghsom")) {
            // a growing hierachical som: has more than one level of soms
            somdescr = new SOMGHSOMDescriptionHTML(this.writer, this.datasetInformation, this.testruns.getRun(r),
                    this.outputDirPath, this.datasetInformation.getEP());
        } else {
            // mainly GrowingSOM, but we use it also as generic outputter for types with only small differences to
            // GrowingSOM
            somdescr = new SOMDescriptionHTML(this.writer, this.datasetInformation, this.testruns.getRun(r),
                    this.outputDirPath, this.datasetInformation.getEP());
        }

        // now that we have the correct handler for our SOM: handle ;)
        somdescr.printSOMDescription();

    }

    /**
     * creates the sections that compares the results of the different specified trainings this comparison constists of
     * a table containing some basic properties of the SOMs, the visualizations of the class distribution, the
     * distribution of the input items and the U-Matrices of the SOM.
     */
    private void printComparingReport() {

        this.writer.appendOutput("<h2>Comparison of the SOMs</h2>");

        this.writer.appendOutput("<p>This section tries to compare the SOMs retrieved within the different trainings specified. Therefore, besides a list of the "
                + "basic properties that can be used to describe the training and the SOM, different visualizations are presented.</p>");

        // first: a table comparing the basic properties of the SOMs
        this.writer.appendOutput("<h3>Basic properties of trained SOMs</h3>");
        this.writer.appendOutput("<div class=\"infoBlock\">");
        if (this.testruns.getNumberOfRuns() < 2) {
            this.writer.appendOutput("<p class=\"header2\">Only one run specified, please see the property list for this run</p>");
        } else {
            // at least two SOMs to compare
            this.writer.appendOutput("<table>\n<thead>\n<tr>\n</tr>\n" + "<tr>\n" + "<th>som type</th>\n"
                    + "<th>dimensions</th>\n" + "<th>sigma</th>\n" + "<th>tau</th>" + "<th>tau2</th>"
                    + "<th>init learn rate</th>" + "<th>init ngbh. range</th>" + "<th>iterations</th>"
                    + "<th>mqe of map</th>" + "</tr>\n" + "</thead>\n" + "<tbody>\n");

            for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
                TestRunResult temp = this.testruns.getRun(i);

                this.writer.appendOutput("<tr>\n");
                this.writer.appendOutput("\t<td>" + temp.getMapProperty(TestRunResultCollection.keyTopology)
                        + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + this.getSOMDimensions(temp) + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + StringUtils.formatDouble(temp.getSigma())
                        + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + StringUtils.formatDouble(temp.getTau())
                        + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + StringUtils.formatDouble(temp.getTau2())
                        + "</td>\n");
                this.writer.appendOutput("<td class=\"middleText\">"
                        + StringUtils.formatString((String) temp.getMapProperty(TestRunResultCollection.keyLearnRateInit))
                        + "</td>\n");
                this.writer.appendOutput("<td class=\"middleText\">"
                        + StringUtils.formatString((String) temp.getMapProperty(TestRunResultCollection.keyNeighbourhoodInit))
                        + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">"
                        + temp.getMapProperty(TestRunResultCollection.keyTotalIterations) + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">"
                        + StringUtils.formatDouble(temp.getMapMQE().getQE()) + "</td>\n");
                this.writer.appendOutput("</tr>\n");
            }
            this.writer.appendOutput("</tbody>\n" + "</table>");

        }
        this.writer.appendOutput("</div>");

        // the next thing are the images showing the class distribution
        if (this.datasetInformation.classInfoAvailable()) {
            this.writer.appendOutput("<h3>Class distribution on the trained SOMs</h3>");
            this.writer.appendOutput("<div class=\"infoBlock\">");
            this.writer.appendOutput("The following images show the distribution of the classes on the trained SOM. Thereby, SOMs having "
                    + "different dimensions are displayed in different size. Each unit on the SOM got 10x10 pixel within the image.");

            this.writer.appendOutput("<table><thead></thead><tbody><tr>");
            for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
                TestRunResult temp = this.testruns.getRun(i);
                temp.createClassDistributionImage(imagePath, "classDistribution.jpg", -1);
                this.writer.appendOutput("<td><img src=\"images/run_" + temp.getRunId()
                        + "_classDistribution.jpg\" alt=\"class distribution of som " + i + "\"/></td>");
            }
            this.writer.appendOutput("</tr></tr>");
            for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
                this.writer.appendOutput("<td>run " + (i + 1) + "</td>");
            }
            this.writer.appendOutput("</tr></tbody></table>");
            this.writer.appendOutput("</div>");
        }

        // then an image with the comparison of the input data distribution
        this.writer.appendOutput("<h3>Distribution of the input vectors on the trained SOMs</h3>");
        this.writer.appendOutput("<div class=\"infoBlock\">");
        this.writer.appendOutput("The following images show the distribution of the input vectors on the trained SOM. Green (and other light colors) indicate areas "
                + "where only a small number of input items is mapped to, whereas dark colors mark the areas with more items. "
                + "Again, different sizes of the maps are preserved by the images.");
        this.writer.appendOutput("<table><thead></thead><tbody><tr>");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            TestRunResult temp = this.testruns.getRun(i);
            temp.createInputDistributionImage(imagePath, "inputDistribution.jpg");
            this.writer.appendOutput("<td><img src=\"images/run_" + temp.getRunId()
                    + "_inputDistribution.jpg\" alt=\"class distribution of som " + i + "\"/></td>");
        }
        this.writer.appendOutput("</tr></tr>");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            this.writer.appendOutput("<td>run " + (i + 1) + "</td>");
        }
        this.writer.appendOutput("</tr></tbody></table>");
        this.writer.appendOutput("</div>");

        // the last thing is an image of the umatrix for comparison
        this.writer.appendOutput("<h3>U-Matrices of the trained SOM</h3>");
        this.writer.appendOutput("<div class=\"infoBlock\">");
        this.writer.appendOutput("Although an visual description of the U-Matrix of the SOMs is contained in the detail description, to give a "
                + "better possibility to compare them, a smaller version of the images is put here again. <br/>"
                + "Therefore, the following images show the distance between neighboured units on the SOM. Again, light areas (especially green) represent"
                + "a small distance between the units, whereas dark colors (especially red) show bigger distances. Like before, different sizes of the "
                + "SOMs are preserved within their representation");
        this.writer.appendOutput("<table><thead></thead><tbody><tr>");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            TestRunResult temp = this.testruns.getRun(i);
            temp.createUDMatrixImage(imagePath, "umatrix_small.jpg", 10, 0);
            this.writer.appendOutput("<td><img src=\"images/run_" + temp.getRunId()
                    + "_umatrix_small.jpg\" alt=\"class distribution of som " + i + "\"/></td>");
        }
        this.writer.appendOutput("</tr></tr>");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            this.writer.appendOutput("<td>run " + (i + 1) + "</td>");
        }
        this.writer.appendOutput("</tr></tbody></table>");
        this.writer.appendOutput("</div>");
    }

    /**
     * returns the dimension of the SOM nicley formatted. returned as string x-size x y- size
     * 
     * @param testrun the testrun for which the dimensions are needed
     * @return unknown or "x-size x y-size" (in units)
     */
    private String getSOMDimensions(TestRunResult testrun) {
        String xunits = (String) testrun.getMapProperty(TestRunResultCollection.keyXDim);
        String yunits = (String) testrun.getMapProperty(TestRunResultCollection.keyYDim);
        String dim = "unknown";
        if (xunits != null && yunits != null) {
            dim = xunits + " x " + yunits;
        }
        return dim;
    }

    /**
     * returns the information (or missing information) about classes nicely formatted
     * 
     * @return the number of classes or information that this information is missing
     */
    protected String classNumberString() {
        if (this.datasetInformation.getNumberOfClasses() < 0) {
            return "There are no class information attached to this input";
        } else {
            return "" + this.datasetInformation.getNumberOfClasses();
        }
    }

    /** Creates the "reporter.js" and "reporter.css" files, from the templates in rsc/reportGenerator/ */
    private void createScriptAndStyleFiles() {
        // some content in the first part of the JS depends on the exact report configuration
        String content = "var sdhArray = new Array(" + this.testruns.getNumberOfRuns() + ");\n";
        content += "var tpArray = new Array(" + this.testruns.getNumberOfRuns() + ");\n";
        content += "var twArray = new Array(" + this.testruns.getNumberOfRuns() + ");\n";

        try {
            // read the static content
            content += FileUtils.readFileContents("./src/core/rsc/reportGenerator/reporter.js");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            FileUtils.writeToFile(content, this.outputDirPath + "reporter.js");
            FileUtils.copyFile("./src/core/rsc/reportGenerator/reporter.css", this.outputDirPath + "reporter.css");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SOMToolboxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
