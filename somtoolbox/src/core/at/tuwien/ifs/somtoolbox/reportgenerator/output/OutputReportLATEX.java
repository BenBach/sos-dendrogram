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

import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResultCollection;
import at.tuwien.ifs.somtoolbox.reportgenerator.TextualDescriptionProvider;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class generates the Report as LATEX output. It holds a reference to an instance of ReportFileWriter, to which
 * all Strings that shall appear in the output are send. This class does only handle how this strings schall look like,
 * and not with any technical detail about how these strings are actually written to a file or elsewhere.
 * 
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @version $Id: OutputReportLATEX.java 3883 2010-11-02 17:13:23Z frank $
 */
public class OutputReportLATEX implements OutputReport {

    /** handles the actual writing of the output. All Strings that shall appear in the output are handed over to it */
    private ReportFileWriter writer;

    private DatasetInformation datasetInformation;

    private TestRunResultCollection testruns;

    private String outputDirPath = "";

    private int type;

    /**
     * Creates a new Object to finally print the report in LATEX
     * 
     * @param outputDir the path to the directory where the files shall be saved to
     */
    public OutputReportLATEX(String outputDir) {

        if (!outputDir.endsWith(System.getProperty("file.separator"))) {
            outputDir += System.getProperty("file.separator");
        }
        this.writer = new ReportFileWriter(outputDir + "report.tex", 2);
        this.outputDirPath = outputDir;
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
        System.out.println("HIER");
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
     * prints the head of the report this includes the title and all tags or markup required at the beginning of the
     * report
     */
    private void printReportStart() {

        this.writer.appendOutput("%neuer Befehl: \\includegraphicstotab[..]{..}\n"
                + "\\newlength{\\myx} % Variable zum Speichern der Bildbreite\n"
                + "\\newlength{\\myy} % Variable zum Speichern der Bildhöhe\n"
                + "\\newcommand\\includegraphicstotab[2][\\relax]{%\n" + "% Abspeichern der Bildabmessungen\n"
                + "\\settowidth{\\myx}{\\includegraphics[{#1}]{#2}}%\n"
                + "\\settoheight{\\myy}{\\includegraphics[{#1}]{#2}}%\n" + "% das eigentliche Einfügen\n"
                + "\\parbox[c][1.1\\myy][c]{\\myx}{%\n" + "\\includegraphics[{#1}]{#2}}%\n" + "}% Ende neuer Befehl\n"
                + "\\documentclass[a4paper,10pt]{article}\n" + "\\usepackage{longtable}\n" + "\\usepackage{color}\n"
                + "\\usepackage{colortbl}\n" + "%Definiere die Tabellenfarben\n");
        /* Definiere die Tabellenfarben */
        for (int i = 0; i < this.datasetInformation.getNumberOfClasses(); i++) {
            String CurrClassColor = StringUtils.getLatexRGBString(this.datasetInformation.getClassColorRGB(i));
            this.writer.appendOutput("\\definecolor{" + CurrClassColor + "}{rgb}{");
            this.writer.appendOutput(StringUtils.getLatexRGBString(this.datasetInformation.getClassColorRGB(i)) + "}");
        }
        this.writer.appendOutput("%ENDE FARBDEFINITION");

        this.writer.appendOutput("\\usepackage{amsmath}\n" + "\\usepackage{graphics}\n" + "\\usepackage{rotating}\n"
                + "\\voffset -3.9cm\n" + "\\hoffset -2.6cm\n" + "\\oddsidemargin 1.5cm\n" + "\\evensidemargin 0cm\n"
                + "\\topmargin 2.5cm\n" + "\\headsep 0pt\n" + "\\headheight 0pt\n" + "\\textheight 24.2cm\n"
                + "\\textwidth 18.5cm\n" + "\\title{Automatically generated Report for the SOM}\n" + "\\author{}\n"
                + "\\begin{document}\n" + "\\maketitle\n" + "\\begin{abstract}\n"
                + "\\begin{center}This is an automatically generated report about the training of a SOM.\n"
                + "This  Report is based on Data Records in the Files:\\\\\n");

        String[] filenames = new String[] { "Input Vector File:", "Template Vector File:", "Class information File:" };
        for (int i = 0; i < filenames.length; i++) {
            this.writer.appendOutput("\\textbf{" + filenames[i] + "} "
                    + this.datasetInformation.getTrainingDataInfo()[i] + "\\\\\n");
        }
        this.writer.appendOutput("\\end{center}\n" + "\\end{abstract}\n");

        /* Inhaltsverzeichnis erstellen */
        this.writer.appendOutput("\\tableofcontents");
    }

    /**
     * prints the foot of the report this includes any fixed data to be displayed at the end, as well as all markup
     * required to finish and close the report
     */
    private void printReportEnd() {
        this.writer.appendLatexOutput("\\end{document}");
        this.writer.finish();
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
        // header
        double[][] dim_array = this.datasetInformation.getPCAdeterminedDims(); /*
                                                                                * Get the Array with the most important Dims, ordered decreasingly by
                                                                                * importance
                                                                                */

        /*
         * Here we calculate how many Dimensions are still "left over" so to say, that is, which ones have not been visualized due to their small
         * importance. Anyway we want to see how many dimensions share what remaining Percentage of the total variance
         */
        double perc = this.datasetInformation.calculateAccumulatedVariance();
        String pca_desc = TextualDescriptionProvider.getScientificDescription("pca");
        this.writer.appendLatexOutput("\\section{The Dataset}\n");
        this.writer.appendLatexOutput("This section describes the dataset that was used for training the SOM, and therefore shall be represented by the SOM.\n"
                + "After some general information, a closer look is taken onto each dimension of the input vectors. Input Vectors are chosen according to the results"
                + "of a " + pca_desc);
        if (dim_array.length == 5) { /* Dim_array == 5 , if the Dim vector had more than 5 dimensions */
            this.writer.appendOutput(" The remaining " + (this.datasetInformation.getVectorDim() - dim_array.length)
                    + " " + "dimensions that are not displayed share an accumulated Variance of "
                    + String.format("%.2f", 100.0 - perc * 100) + "\\%.");
        }
        if (this.datasetInformation.classInfoAvailable()) {
            this.writer.appendLatexOutput("\\\\At the end of this section, some information about the existing classes and how the input items are distributed on them "
                    + "are given.");
        }

        // Information about the input data vectors:
        this.writer.appendLatexOutput("\\paragraph{Dataset:\\\\}\n");
        this.writer.appendLatexOutput("Number of input vectors: " + datasetInformation.getNumberOfInputVectors()
                + "\\\\\n");
        this.writer.appendLatexOutput("Dimensionality of input vectors: " + datasetInformation.getVectorDim()
                + "\\\\\n");
        this.writer.appendLatexOutput("Dataset has been normalized: "
                + StringUtils.formatBooleanValue(0, this.datasetInformation.isNormalized()) + "\\\\\n");
        this.writer.appendLatexOutput("Number of Classes: " + this.classNumberString() + "\\\\\n");

        this.writer.appendLatexOutput("\\paragraph{Attribute details\\\\}\n"
                + "Table 1 on page \\pageref{tab:attdetails} gives an Overview over the Attributes of the Vectors of the trained SOM. The table contains Statistical Base "
                + "numbers and the Variance. The Vectors are  shown in decreasing Importance, according to the Results of the PCA.");

        this.writer.appendLatexOutput("\\begin{sidewaystable}\n\\begin{tabular}{lccccccccc}\n"
                + "\\textit{label} &\n"
                + "\\textit{Min} &\n"
                + "\\textit{Max} &\n"
                + "\\textit{Mean} &\n"
                + "\\textit{Variance} &\n"
                + "\\textit{# of zeros} &\n"
                + "\\textit{Discr. values} &\n"
                + "\\textit{only $0/1$ values} &\n \\textit{% of total Variance} &\n \\textit{Metro Map Component}\\\\\n");

        for (int i = (int) dim_array[0][0], counter = 0; counter < dim_array.length; counter++, i = counter < dim_array.length
                ? (int) dim_array[counter][0] : (int) dim_array[0][0]) {

            this.writer.appendLatexOutput(this.datasetInformation.getAttributeLabel(i) + "&\n");
            this.writer.appendLatexOutput(String.format("%.3f", this.datasetInformation.getNumericalDataProps(
                    DatasetInformation.MIN_VALUE, i))
                    + "&\n");
            this.writer.appendLatexOutput(String.format("%.3f", this.datasetInformation.getNumericalDataProps(
                    DatasetInformation.MAX_VALUE, i))
                    + "&\n");
            this.writer.appendLatexOutput(String.format("%.5f", this.datasetInformation.getNumericalDataProps(
                    DatasetInformation.MEAN_VALUE, i))
                    + "&\n");
            this.writer.appendLatexOutput(String.format("%.5f", this.datasetInformation.getNumericalDataProps(
                    DatasetInformation.VAR_VALUE, i))
                    + "&\n");
            this.writer.appendLatexOutput((int) this.datasetInformation.getNumericalDataProps(
                    DatasetInformation.ZERO_VALUE, i)
                    + " ("
                    + String.format("%.2f", this.datasetInformation.getNumericalDataProps(
                            DatasetInformation.ZERO_VALUE, i)
                            / this.datasetInformation.getNumberOfInputVectors() * 100) + "%)" + "&\n");
            this.writer.appendLatexOutput(StringUtils.formatBooleanValue(0, datasetInformation.getBoolDataProps(
                    DatasetInformation.DISCRETE, i))
                    + "&\n");
            this.writer.appendLatexOutput(StringUtils.formatBooleanValue(0, datasetInformation.getBoolDataProps(
                    DatasetInformation.ONLY01, i))
                    + "&\n" + String.format("%.2f", dim_array[counter][1] * 100) + " % &\n");
            testruns.getRun(0).createSingleMetroMapComponentImage(
                    this.outputDirPath + SOMDescriptionHTML.imgSubdir + File.separator,
                    i + "_SingleMetroMapComponent.jpg", i);
            this.writer.appendLatexOutput("\\includegraphics[width=50pt]{" + SOMDescriptionHTML.imgSubdir
                    + "/component_" + i + "_SingleMetroMapComponent.jpg}\\\\");
        }
        this.writer.appendLatexOutput("\\end{tabular}\n\\caption{Attribute Details}\n\\label{tab:attdetails}\n\\end{sidewaystable}");

        // more detailed class information
        if (this.datasetInformation.getNumberOfClasses() > 0) {
            this.writer.appendLatexOutput("\\subsection{Class details:}\n"
                    + "Table 2 on page \\pageref{tab:classdetails} gives a closer numerical look to the distribution of all classes on the Map.It lists how many input items belong to "
                    + "the specific class, aswell as the total absolute percentage of how many input items belong to this class. The uttemrost right"
                    + "colum displays the Color used for this Class in some other Visualisations.");
            this.writer.appendLatexOutput("\\begin{longtable}{lccl}\n" + "Class label&\n" + "Number of Classmembers&\n"
                    + "% of input belong to&\n" + "Class colour used\\\\\n");
            for (int i = 0; i < this.datasetInformation.getNumberOfClasses(); i++) {

                this.writer.appendLatexOutput(this.datasetInformation.getNameOfClass(i) + "&\n");
                this.writer.appendLatexOutput(this.datasetInformation.getNumberOfClassmembers(i) + "&\n");
                this.writer.appendLatexOutput(String.format("%.2f",
                        (double) this.datasetInformation.getNumberOfClassmembers(i)
                                / (double) this.datasetInformation.getNumberOfInputVectors() * 100)
                        + "%&\n");
                this.writer.appendLatexOutput("\\cellcolor{"
                        + StringUtils.getLatexRGBString(this.datasetInformation.getClassColorRGB(i)) + "}\\\\\n");
            }
            this.writer.appendLatexOutput("\\caption{Class Details}\n" + "\\label{tab:classdetails}"
                    + "\\end{longtable}\n");
        }
    }

    /**
     * first outputs the section containing the results from the different teststruns specified, then a short comparison
     * of the testruns (SOM configuration and basic results) is printed. for the output of the sections for the
     * different training runs, this.printReportOnTestrun(run) is used
     */
    private void printTestrunsReport() {

        // header
        if (this.testruns.getNumberOfRuns() > 1) {
            this.writer.appendLatexOutput("\\section{Results of the SOM trainings}\n");
            this.writer.appendLatexOutput("In this section, details about the different specified trainings and their results are "
                    + "presented. For each training,");
        } else {
            this.writer.appendLatexOutput("\\section{Results of the SOM training}\n");
            this.writer.appendLatexOutput("In this section, details about the training and the resuling SOM are "
                    + "presented. Therefore,");

        }
        this.writer.appendLatexOutput(" first some basic properties describing the training parameters and the resulting "
                + "SOM are given. Then, the distribution of the input data on the trained SOM is described. This includes the quantization and "
                + "topographic errors existing on the map.");
        if (this.datasetInformation.classInfoAvailable()) {
            this.writer.appendLatexOutput("\\\\Together with the distribution of the input data items, also information about how and where the classes"
                    + " are distributed on the map are given.");
        }
        this.writer.appendLatexOutput("\\\\At the end, a selection of 10 clusters (and therefore possibilities to group the unit on the SOM) is described.");

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
            this.writer.appendLatexOutput("\\subsection{" + (r + 1) + ". trained SOM:}\n");
        }
        this.writer.appendLatexOutput("\\subsection{SOM \\& training properties:}\n");

        // to distinguish the different SOMs, we need its idetinfying String
        String somtype = (String) this.testruns.getProperty(TestRunResultCollection.keyTopology, r);
        SOMDescriptionLATEX somdescr = null;

        if (somtype.equals("gg")) {
            // growing grid: seems to be produced by GHSOM if no child map is created
            somdescr = new SOMGGDescriptionLATEX(this.writer, this.datasetInformation, this.testruns.getRun(r),
                    this.outputDirPath);
        } else if (somtype.equals("ghsom")) {
            // a growing hierachical som: has more than one level of soms
            somdescr = new SOMGHSOMDescriptionLATEX(this.writer, this.datasetInformation, this.testruns.getRun(r),
                    this.outputDirPath);
        } else {
            // mainly GrowingSOM, but we use it also as generic outputter for types with only small differences to
            // GrowingSOM
            somdescr = new SOMDescriptionLATEX(this.writer, this.datasetInformation, this.testruns.getRun(r),
                    this.outputDirPath);
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
        writer.appendLatexOutput("\\section{Comparison of the SOMs}\n");

        writer.appendLatexOutput("This section tries to compare the SOMs retrieved within the different trainings specified. Therefore, besides a list of the "
                + "basic properties that can be used to describe the training and the SOM, different visualizations are presented.");

        // first: a table comparing the basic properties of the SOMs
        writer.appendLatexOutput("\\subsubsection{Basic properties of trained SOMs}");
        if (testruns.getNumberOfRuns() < 2) {
            writer.appendLatexOutput("\\paragraph{Only one run specified, please see the property list for this run}\n");
        } else {
            // at least two SOMs to compare
            writer.appendLatexOutput("\\begin{tabular}{lcccccccc}\n" + "som type&\n" + "dimensions&\n" + "sigma&\n"
                    + "tau&\n" + "tau2&\n" + "init learn rate&\n" + "init ngbh. range&\n" + "iterations&\n"
                    + "mqe of map\\\\\n");

            for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
                TestRunResult temp = this.testruns.getRun(i);

                writer.appendLatexOutput(temp.getMapProperty(TestRunResultCollection.keyTopology) + "&\n");
                writer.appendLatexOutput(this.getSOMDimensions(temp) + "&\n");
                writer.appendLatexOutput(StringUtils.formatDouble(temp.getSigma()) + "&\n");
                writer.appendLatexOutput(StringUtils.formatDouble(temp.getTau()) + "&\n");
                writer.appendLatexOutput(StringUtils.formatDouble(temp.getTau2()) + "&\n");
                writer.appendLatexOutput(StringUtils.formatString((String) temp.getMapProperty(TestRunResultCollection.keyLearnRateInit))
                        + "&\n");
                writer.appendLatexOutput(StringUtils.formatString((String) temp.getMapProperty(TestRunResultCollection.keyNeighbourhoodInit))
                        + "&\n");
                writer.appendLatexOutput(temp.getMapProperty(TestRunResultCollection.keyTotalIterations) + "&\n");
                writer.appendLatexOutput(StringUtils.formatDouble(temp.getMapMQE().getQE()) + "\\\\\n");
            }
            writer.appendLatexOutput("\\end{tabular}");

        }

        // the next thing are the images showing the class distribution
        if (this.datasetInformation.classInfoAvailable()) {
            this.writer.appendLatexOutput("\\subsubsection{Class distribution on the trained SOMs}\n");
            this.writer.appendLatexOutput("The following images show the distribution of the classes on the trained SOM. Thereby, SOMs having "
                    + "different dimensions are displayed in different size. Each unit on the SOM got 10x10 pixel within the image.\\\\\n");

            this.writer.appendLatexOutput("\n\\begin{tabular}{");
            for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
                this.writer.appendOutput("l");
            }
            this.writer.appendOutput("}");
            for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
                TestRunResult temp = this.testruns.getRun(i);
                temp.createClassDistributionImage(this.outputDirPath + "images" + System.getProperty("file.separator"),
                        "classDistribution.jpg", -1);
                this.writer.appendOutput("\\includegraphics{images/run_" + temp.getRunId() + "_classDistribution.jpg}");
                if (i + 1 < this.testruns.getNumberOfRuns()) {
                    this.writer.appendOutput("\n&\n");
                }
            }
            this.writer.appendLatexOutput("\\\\\n");
            for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
                this.writer.appendLatexOutput("run " + (i + 1));
                if (i + 1 < this.testruns.getNumberOfRuns()) {
                    this.writer.appendOutput("\n&\n");
                }
            }
            this.writer.appendLatexOutput("\\end{tabular}");
        }

        // then an image with the comparison of the input data distribution
        this.writer.appendLatexOutput("\\subsubsection{Distribution of the input vectors on the trained SOMs}\n");
        this.writer.appendLatexOutput("The following images show the distribution of the input vectors on the trained SOM. Green (and other light colors) indicate areas "
                + "where only a small number of input items is mapped to, whereas dark colors mark the areas with more items. "
                + "Again, different sizes of the maps are preserved by the images.\\\\\n");
        this.writer.appendLatexOutput("\\begin{tabular}{");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            this.writer.appendOutput("l");
        }
        this.writer.appendOutput("}");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            TestRunResult temp = this.testruns.getRun(i);
            temp.createInputDistributionImage(this.outputDirPath + "images" + System.getProperty("file.separator"),
                    "inputDistribution.jpg");
            this.writer.appendOutput("\\includegraphics{images/run_" + temp.getRunId() + "_inputDistribution.jpg}");
            if (i + 1 < this.testruns.getNumberOfRuns()) {
                this.writer.appendOutput("\n&\n");
            }
        }
        this.writer.appendLatexOutput("\\\\\n");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            this.writer.appendLatexOutput("run " + (i + 1));
            if (i + 1 < this.testruns.getNumberOfRuns()) {
                this.writer.appendOutput("\n&\n");
            }
        }
        this.writer.appendLatexOutput("\\end{tabular}");

        // the last thing is an image of the umatrix for comparison
        this.writer.appendLatexOutput("\\subsubsection{U-Matrices of the trained SOM}");
        this.writer.appendLatexOutput("Although an visual description of the U-Matrix of the SOMs is contained in the detail description, to give a "
                + "better possibility to compare them, a smaller version of the images is put here again. \\\\\n"
                + "Therefore, the following images show the distance between neighboured units on the SOM. Again, light areas (especially green) represent"
                + "a small distance between the units, whereas dark colors (especially red) show bigger distances. Like before, different sizes of the "
                + "SOMs are preserved within their representation\\\\\n");
        this.writer.appendLatexOutput("\\begin{tabular}{");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            this.writer.appendOutput("l");
        }
        this.writer.appendOutput("}");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            TestRunResult temp = this.testruns.getRun(i);
            temp.createUDMatrixImage(this.outputDirPath + "images" + System.getProperty("file.separator"),
                    "umatrix_small.jpg", 10, 0);
            this.writer.appendOutput("\\includegraphics{images/run_" + temp.getRunId() + "_umatrix_small.jpg}");
            if (i + 1 < this.testruns.getNumberOfRuns()) {
                this.writer.appendOutput("\n&\n");
            }
        }
        this.writer.appendLatexOutput("\\\\\n");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            this.writer.appendLatexOutput("run " + (i + 1));
            if (i + 1 < this.testruns.getNumberOfRuns()) {
                this.writer.appendOutput("\n&\n");
            }
        }
        this.writer.appendLatexOutput("\\end{tabular}");
        ;
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
    private String classNumberString() {
        if (this.datasetInformation.getNumberOfClasses() < 0) {
            return "There are no class information attached to this input";
        } else {
            return "" + this.datasetInformation.getNumberOfClasses();
        }
    }

}
