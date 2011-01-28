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
package at.tuwien.ifs.somtoolbox.apps.config;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.layers.quality.TopographicError;

/**
 * This class provides methods to get commonly used JSAP options. All new options your application may need should also
 * be registered here.
 * 
 * @author Rudolf Mayer
 * @version $Id: OptionFactory.java 3846 2010-10-11 14:49:19Z frank $
 */
public class OptionFactory extends AbstractOptionFactory {

    public static final Parameter[] OPTIONS_GROWING_CELL_STRUCTURES = { getSwitchSkipDataWinnerMapping(),
            getOptNumberWinners(false), getOptProperties(true) };

    public static final Parameter[] OPTIONS_DATA_INFO_GENERATOR = { getOptBaseDirName(true),
            getOptRemoveDirName(false), getOptInputVectorFile(true), getOptDataInformationFileFile(true) };

    public static final Parameter[] OPTIONS_INPUT_SIMILARITY_COMPUTER = { getOptInputVectorDistanceMatrix(false),
            getOptInputVectorFile(true), getOptMetric(false), getOptMetricParams(false), getOptNumberNeighbours(true),
            getOptOutputFileName(true), getOptOutputFormat(false, "ASCII", new String[] { "ASCII", "SAT-DB" }) };

    public static final Parameter[] OPTIONS_LAGUS_KEYWORD_LABELER = { getOptInputVectorFile(true),
            getOptTemplateVectorFile(true), getOptWeightVectorFile(true), getOptUnitDescriptionFile(true),
            getOptNumberLabels(false, "5"), getSwitchIsDenseData(), getOptMapDescriptionFile(false),
            getOptInputDirectory(true) };

    public static final Parameter[] OPTIONS_UNIT_DESCRIPTION_REWRITER = { getOptUnitDescriptionFile(true),
            getOptNameMappingFile(true), getOptOutputVector() };

    public static final Parameter[] OPTIONS_VECTORFILE_CHOPPER = { getOptInputFileName(), getOptKeepInputsFile(true),
            getOptOutputFileName(true) };

    public static final Parameter[] OPTIONS_VECTORFILE_PREFIX_ADDER = { getOptInputFileName(), getOptProperties(true),
            getOptOutputVector() };

    public static final Parameter[] OPTIONS_VECTORFILE_REWRITER = { getOptInputFileName(), getOptNameMappingFile(true),
            getOptOutputVector() };

    public static final Parameter[] OPTIONS_MAP_INTERPOLATOR = { getOptWeightVectorFile(true), getOptOutputVector() };

    public static FlaggedOption getOptAnnotationFile(boolean required) {
        return new FlaggedOption("annotationFile", getInputFileParser(), null, required, 'a', JSAP.NO_LONGFLAG,
                "Annotation File.");
    }

    public static Parameter getSwitchSkipInstanceNames() {
        return new Switch("skipInstanceNames", JSAP.NO_SHORTFLAG, "skipInstanceNames",
                "Skipping writing of instance names in ARFF file");
    }

    public static Parameter getSwitchSkipInputsWithoutClass() {
        return new Switch("skipInputsWithoutClass", JSAP.NO_SHORTFLAG, "skipInputsWithoutClass",
                "Skipping writing instances without assigned class to ARFF file");
    }

    public static Parameter getSwitchTabSeparatedClassFile() {
        return new Switch("tabSeparatedClassFile", JSAP.NO_SHORTFLAG, "tabSeparated",
                "Write the class-file tab separated.");
    }

    public static Switch getSwitchWriteTVFile() {
        return new Switch("writeTV", JSAP.NO_SHORTFLAG, "tv",
                "Create and write an apropriate TemplateVector file: <outfile>.tv");
    }

    public static FlaggedOption getOptNormalizeWeights() {
        return new FlaggedOption("weights", JSAP.FLOAT_PARSER, null, false, 'q', "weights",
                "Apply different weights when normalising the vectors. No normalisation if skipped, missing values default to 1").setList(
                true).setListSeparator(':');
    }

    public static Switch getSwitchNoPlayer() {
        return new Switch("noplayer", JSAP.NO_SHORTFLAG, "noplayer",
                "Don't use the internal player (PlaySOMPlayer), create the classic PlaySOMPanel");
    }

    public static FlaggedOption getOptUseMultiCPU(boolean required) {
        return new FlaggedOption("cpus", JSAP.INTEGER_PARSER, "1", required, JSAP.NO_SHORTFLAG, "cpus",
                "Numbers of CPUs to use.");
    }

    public static FlaggedOption getOptApplicationDirectory(boolean required) {
        return new FlaggedOption("applicationDirectory", getInputDirectoryParser(), null, required, JSAP.NO_SHORTFLAG,
                "appdir", "Directory containing the SOMViewer application start file and the "
                        + at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer.PREFS_FILE + " file.");
    }

    public static FlaggedOption getOptBaseDirName(boolean required) {
        return new FlaggedOption("baseDir", getInputDirectoryParser(), null, required, 'b', "baseDir",
                "Base directory where the branch of files resides.");
    }

    public static FlaggedOption getOptBaseName(boolean required) {
        return new FlaggedOption("basename", JSAP.STRING_PARSER, null, required, 'o', "output");
    }

    public static FlaggedOption getOptClassColoursFile(boolean required) {
        return new FlaggedOption(
                "classColours",
                getInputFileParser("txt"),
                null,
                required,
                JSAP.NO_SHORTFLAG,
                "colours",
                "File listing the RGB values of the colours to be used for the class-based visualisations (Pie-charts, Thematic class map).");
    }

    public static FlaggedOption getOptClassInformationFile(boolean required) {
        return new FlaggedOption("classInformationFile", getInputFileParser("cls", "clsinf", "clsinfo", "classinfo",
                "txt"), null, required, 'c', JSAP.NO_LONGFLAG,
                "Class information file containing the class for each data item.");
    }

    public static UnflaggedOption getOptInputI(int i) {
        return new UnflaggedOption(String.format("input%d", new Integer(i)), getInputFileParser(), "", true, false,
                String.format("The %d. input file", new Integer(i)));
    }

    public static UnflaggedOption getOptInput() {
        return new UnflaggedOption("input", getInputFileParser(), null, true, true, "The input files");
    }

    /* Angela */
    public static FlaggedOption getOptClasslist(boolean required) {
        return new FlaggedOption("classList", getInputFileParser(), null, required, JSAP.NO_SHORTFLAG, "classlist",
                "A List of class names seperated by comma");
    }

    public static FlaggedOption getOptDatabaseServerAddress(boolean required) {
        return new FlaggedOption("databaseServerAddress", JSAP.STRING_PARSER, "localhost", required, JSAP.NO_SHORTFLAG,
                "server", "Servername (or IP) of the database server. Defaults to 'localhost'.");
    }

    public static FlaggedOption getOptDatabaseName(boolean required) {
        return new FlaggedOption("databaseName", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG, "dbName",
                "Name of the database.");
    }

    public static FlaggedOption getOptDatabasePassword(boolean required) {
        return new FlaggedOption("databasePassword", JSAP.STRING_PARSER, "", required, JSAP.NO_SHORTFLAG, "password",
                "Password for the database acccess. Defaults to an empty password.");
    }

    public static FlaggedOption getOptDatabaseTableNamePrefix(boolean required) {
        return new FlaggedOption("databaseTableNamePrefix", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG,
                "tablePrefix", "Prefix for the tables in the database.");
    }

    public static FlaggedOption getOptDatabaseUser(boolean required) {
        return new FlaggedOption("databaseUser", JSAP.STRING_PARSER, "root", required, JSAP.NO_SHORTFLAG, "user",
                "Username for the database acccess. Defaults to 'root'.");
    }

    public static FlaggedOption getOptDataInformationFileFile(boolean required) {
        return new FlaggedOption("dataInformationFile", getInputFileParser(), null, required, 'i', JSAP.NO_LONGFLAG,
                "Data information file containing information such as location of each data item.");
    }

    public static FlaggedOption getOptDataWinnerMappingFile(boolean required) {
        return new FlaggedOption("dataWinnerMappingFile", getInputFileParser("dwm"), null, required, JSAP.NO_SHORTFLAG,
                "dw", "Unit description file describing the winners mapped onto a unit for a SOM/GHSOM.");
    }

    public static UnflaggedOption getOptSOMLibInputs(boolean required) {
        return new UnflaggedOption("inputs", JSAP.STRING_PARSER, null, required, true,
                "Prefix for the input files to merge (i.e. file name w/o .tv/.vec extension");
    }

    public static UnflaggedOption getOptSOMLibMaps(boolean required) {
        return new UnflaggedOption("maps", JSAP.STRING_PARSER, null, required, true,
                "Prefix for the SOM maps to merge (i.e. file name w/o .unit/.wgt extension");
    }

    public static FlaggedOption getOptSetSecondSOM(boolean required) {
        return new FlaggedOption("secondSOMPrefix", JSAP.STRING_PARSER, null, required, '2', "secondSOM",
                "Prefix for the set of files representing second SOM");
    }

    public static FlaggedOption getOptDecodedOutputDir(boolean required) {
        return new FlaggedOption("decodedOutputDir", getOutputDirectoryParser(), null, required, JSAP.NO_SHORTFLAG,
                "decodeDir",
                "(When using multi-channel audio playback) Decoded mp3 files will be saved to and loaded from this directory.");
    }

    public static FlaggedOption getOptDimension(boolean required) {
        return new FlaggedOption("dimension", JSAP.INTEGER_PARSER, null, required, JSAP.NO_SHORTFLAG, "dim",
                "The dimension of the input data.");
    }

    public static FlaggedOption getOptDrawUnitGrid(boolean required) {
        return new FlaggedOption("unitGrid", JSAP.BOOLEAN_PARSER, "true", required, 'g', "unitGrid",
                "Whether to draw the grid of units.");
    }

    public static FlaggedOption getOptFileNamePrefix(boolean required) {
        return new FlaggedOption("fileNamePrefix", JSAP.STRING_PARSER, null, required, 'p', JSAP.NO_LONGFLAG,
                "Prefix for the relative path to the files. This option is only to be used if no absolute paths are available.");
    }

    public static FlaggedOption getOptFileNameSuffix(boolean required) {
        return new FlaggedOption(
                "fileNameSuffix",
                JSAP.STRING_PARSER,
                null,
                required,
                's',
                JSAP.NO_LONGFLAG,
                "Suffix to the filenames (aka file endings, e.g. \".mp3\"). This option is only to be used if no absolute paths are available.");
    }

    public static FlaggedOption getOptHeight(boolean required) {
        return new FlaggedOption("height", JSAP.INTEGER_PARSER, null, required, JSAP.NO_SHORTFLAG, "height",
                "The height of a unit.");
    }

    public static FlaggedOption getOptHighlightedDataNamesFile(boolean required) {
        return new FlaggedOption("dataNamesFile", getInputFileParser(), null, required, 'd', JSAP.NO_LONGFLAG,
                "File containing the names of data items to be highlighted.");
    }

    public static UnflaggedOption getOptHtmlFileName() {
        return new UnflaggedOption("htmlFile", getOuputFileParser(), null, true, false, "Name of HTML file to write.");
    }

    public static FlaggedOption getOptIgnoreFile(boolean required) {
        return new FlaggedOption("IgnoreFile", getInputFileParser(), null, required, 'g', JSAP.NO_LONGFLAG,
                "Ignore File.");
    }

    public static UnflaggedOption getOptBackgroundImage(boolean required) {
        return new UnflaggedOption("backgroundImage", getInputFileParser("png", "jpg", "jpeg"), null, required, false,
                "The Background Image (png, jpg)");
    }

    public static UnflaggedOption getOptImageFileName() {
        return new UnflaggedOption("imageFile", getOuputFileParser(), null, true, false,
                "Name of image file to write. No Suffix needed. Writes as PNG and EPS.");
    }

    public static FlaggedOption getOptImageFileType(boolean required) {
        return new FlaggedOption("filetype", JSAP.STRING_PARSER, "png", required, JSAP.NO_SHORTFLAG, "type");
    }

    public static FlaggedOption getOptInitialVisParams(boolean required) {
        return new FlaggedOption("initialVisParams", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG,
                "visParams",
                "Parameters for the initial visualisation. Currently only implemented for SmoothedDataHistograms visualisations.");
    }

    public static FlaggedOption getOptInitialVisualisation(boolean required) {
        return new FlaggedOption("initialVisualisation", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG, "vis",
                "The name of the initial visualisation.");
    }

    public static FlaggedOption getOptInitialPalette(boolean required) {
        return new FlaggedOption("initialPalette", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG, "palette",
                "The name of the initial palette to be used.");
    }

    public static FlaggedOption getOptInputFileName(boolean required) {
        return new FlaggedOption("inputFile", getInputFileParser(), null, required, 'f', JSAP.NO_LONGFLAG,
                "Name of input file to be read.");
    }

    public static UnflaggedOption getOptInputFileName() {
        return new UnflaggedOption("input", getInputFileParser(), true, "Name of input vector file to be read.");
    }

    public static FlaggedOption getOptInputLabel(boolean required) {
        return new FlaggedOption("inputLabel", JSAP.STRING_PARSER, null, required, 'l', JSAP.NO_LONGFLAG,
                "Name/label of the input vector");
    }

    public static FlaggedOption getOptInputVectorFile(boolean required) {
        return new FlaggedOption("inputVectorFile", getInputFileParser(), null, required, 'v', JSAP.NO_LONGFLAG,
                "Input file containing the input vectors to be mapped.");
    }

    public static FlaggedOption getOptInputFormat(boolean required, String[] validOptionValues) {
        return new FlaggedOption("inputFormat", makeEnumeratedStringParser(validOptionValues), null, required,
                JSAP.NO_SHORTFLAG, "inputFormat", "Format of the input file, valid values are: "
                        + Arrays.toString(validOptionValues)
                        + (!required ? "\nIf not specified, the format will be determined from the file extension."
                                : ""));
    }

    public static FlaggedOption getOptInputVectorDistanceMatrix(boolean required) {
        return new FlaggedOption("inputVectorDistanceMatrix", getInputFileParser(), null, required, 'x', "matrix",
                "Input file containing the distance matrix of the input vectors.");
    }

    public static FlaggedOption getOptInterleave(boolean required, Integer defaultValue) {
        return new FlaggedOption("interleave", JSAP.INTEGER_PARSER, String.valueOf(defaultValue), required,
                JSAP.NO_SHORTFLAG, "interleave", "Interleave between the indices.");
    }

    public static FlaggedOption getOptK(boolean required) {
        return new FlaggedOption("k", JSAP.STRING_PARSER, null, false, 'k', JSAP.NO_LONGFLAG);
    }

    public static FlaggedOption getOptKaskiNumber(boolean required) {
        return new FlaggedOption("KaskiNumber", JSAP.STRING_PARSER, null, required, 'k', JSAP.NO_LONGFLAG,
                "Number of Kaski keywords.");
    }

    public static FlaggedOption getOptKeepInputsFile(boolean required) {
        return new FlaggedOption("keepFile", getInputFileParser(), null, required, 'k', JSAP.NO_LONGFLAG,
                "The file that defines which inputs to keep.");
    }

    public static FlaggedOption getOptLabelFileName(boolean required) {
        return new FlaggedOption("labelFileName", getInputFileParser(), null, required, 'l', JSAP.NO_LONGFLAG,
                "File containing the XML labels.");
    }

    public static FlaggedOption getOptLabeling(boolean required) {
        return new FlaggedOption("labeling", JSAP.STRING_PARSER, null, required, 'l', JSAP.NO_LONGFLAG,
                "Labeling algorithm to use.");
    }

    public static FlaggedOption getOptLinkageFile(boolean required) {
        return new FlaggedOption("linkageMapFile", getInputFileParser(), null, required, JSAP.NO_SHORTFLAG, "linkage",
                "File containing data item linkage mapping.");
    }

    public static FlaggedOption getOptMapDescriptionFile(boolean required) {
        return new FlaggedOption("mapDescriptionFile", getInputFileParser("map"), null, required, 'm',
                JSAP.NO_LONGFLAG, "Map description file describing a mapped SOM/GHSOM.");
    }

    public static FlaggedOption getOptInputCorrections(boolean required) {
        return new FlaggedOption("inputCorrections", getInputFileParser(), null, required, JSAP.NO_SHORTFLAG,
                "corrections", "Name of the file containing input corrections.");
    }

    public static FlaggedOption getOptMergeMode() {
        return new FlaggedOption("mode", JSAP.STRING_PARSER, "All", false, JSAP.NO_SHORTFLAG, "mode",
                "The merging mode to use, i.e. Union, Intersection, or a number of vectors the term occurrs.\n"
                        + " If no mode is provided, all possible combinations will be generated.");
    }

    public static FlaggedOption getOptMetric(boolean required) {
        return new FlaggedOption(
                "metric",
                JSAP.STRING_PARSER,
                "at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric",
                required,
                JSAP.NO_SHORTFLAG,
                "metric",
                "Name of the metric to be used for distance calculation in input space. at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric is default.");
    }

    public static FlaggedOption getOptMetricParams(boolean required) {
        return new FlaggedOption("metricParams", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG, "metricParams",
                "Parameters for the metric.");
    }

    public static FlaggedOption getOptNameMappingFile(boolean required) {
        return new FlaggedOption("nameMappingFile", getInputFileParser(), null, required, JSAP.NO_SHORTFLAG, "nameMap",
                "Name mapping file <original name> -> <new name>");
    }

    public static FlaggedOption getOptNormMethod(boolean required) {
        return new FlaggedOption(
                "method",
                makeEnumeratedStringParser("UNIT_LEN", "MIN_MAX", "STANDARD_SCORE"),
                "UNIT_LEN",
                required,
                'm',
                JSAP.NO_LONGFLAG,
                "Normalization method.\n"
                        + "UNIT_LEN normalises the vectors in the input file to unit length.\n"
                        + "MIN_MAX normalises each attributes between 0 and 1.\n"
                        + "STANDARD_SCORE normalises each attribute to a mean of 0, and a max value of the standard deviation.\n");
    }

    public static FlaggedOption getOptNumberLabels(boolean required) {
        return new FlaggedOption("numberLabels", JSAP.INTEGER_PARSER, null, required, 'n', JSAP.NO_LONGFLAG,
                "Number of labels per unit. Useless if no labeling algorithm is specified.");
    }

    public static FlaggedOption getOptNumberLabels(boolean required, String defaultValue) {
        FlaggedOption o = new FlaggedOption("numberLabels", JSAP.INTEGER_PARSER, defaultValue, required, 'n',
                JSAP.NO_LONGFLAG);
        String help = "Number of labels per unit. ";
        if (defaultValue != null) {
            help += "Default value is " + defaultValue + ". ";
        }
        o.setHelp(help);
        return o;
    }

    public static FlaggedOption getOptNumberVariants(boolean required, Integer defaultValue) {
        return new FlaggedOption("variants", JSAP.INTEGER_PARSER, String.valueOf(defaultValue), required,
                JSAP.NO_SHORTFLAG, "variants", "Number of variants to generate.");
    }

    public static FlaggedOption getOptNumberWinners(boolean required) {
        return new FlaggedOption("numberWinners", JSAP.INTEGER_PARSER,
                String.valueOf(SOMLibDataWinnerMapping.MAX_DATA_WINNERS), required, JSAP.NO_SHORTFLAG, "numberWinners",
                "Number of winners to write. Default is " + SOMLibDataWinnerMapping.MAX_DATA_WINNERS + ".");
    }

    public static FlaggedOption getOptNumberNeighbours(boolean required) {
        return new FlaggedOption("numberNeighbours", JSAP.INTEGER_PARSER, null, required, 'n', "numberNeighbours",
                "Number of neighbours to find.");
    }

    public static FlaggedOption getOptOriginalText(boolean required) {
        return new FlaggedOption("OriginalText", JSAP.STRING_PARSER, null, required, 'o', JSAP.NO_LONGFLAG,
                "Original text collection.");
    }

    public static FlaggedOption getOptInputDirectory(boolean required) {
        return new FlaggedOption("inputDir", getInputDirectoryParser(), null, required, JSAP.NO_SHORTFLAG, "inputDir",
                "Path to the input directory.");
    }

    public static FlaggedOption getOptRotation(boolean required) {
        return new FlaggedOption("rotation", JSAP.INTEGER_PARSER, null, required, 'r', "rotation",
                "Rotation of the new map, values are: 90, 180, 270.");
    }

    public static FlaggedOption getOptFlip(boolean required) {
        return new FlaggedOption("flip", JSAP.CHARACTER_PARSER, null, required, 'f', "flip",
                "Flip the map, values are h[orizontal] or v[ertical].");
    }

    public static FlaggedOption getOptOutputDirectory(boolean required) {
        return new FlaggedOption("outputDirectory", getOutputDirectoryParser(), null, required, JSAP.NO_SHORTFLAG,
                "outputDir", "Name of the output directory.");
    }

    public static UnflaggedOption getOptOutputFileName(boolean required) {
        return new UnflaggedOption("output", getOuputFileParser(), null, required, false, "Name of the output file.");
    }

    public static FlaggedOption getOptOutputFormat(boolean required, String[] validOptionValues) {
        return getOptOutputFormat(required, null, validOptionValues);
    }

    public static FlaggedOption getOptOutputFormat(boolean required, String defaultValue, String[] validOptionValues) {
        String msg = StringUtils.isNotBlank(defaultValue) ? "" : !required
                ? "\nIf not specified, the format will be determined from the file extension." : "";
        return new FlaggedOption("outputFormat", makeEnumeratedStringParser(validOptionValues), defaultValue, required,
                JSAP.NO_SHORTFLAG, "outputFormat", "Format of the output file, valid values are: "
                        + Arrays.toString(validOptionValues) + msg);
    }

    public static UnflaggedOption getOptOutputVector() {
        return new UnflaggedOption("output", getOuputFileParser(), null, true, false,
                "Name of new vector file to be created.");
    }

    public static FlaggedOption getOptDecodeProbability(boolean required) {
        return new FlaggedOption("probabilityDecode", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG,
                "pDdecode",
                "(When using multi-channel audio playback) Probability to decode a mp3 file to wav before playing.");
    }

    public static UnflaggedOption getOptProperties(boolean required) {
        return new UnflaggedOption("properties", getInputFileParser(), null, required, false, "Name of property file.");
    }

    /** Quality Measure Class (e.g. q_te for {@link TopographicError} */
    public static FlaggedOption getOptQualityMeasureClass(boolean required) {
        return new FlaggedOption("qualityMeasureClass", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG,
                "qualityClass", "Quality measure class.");
    }

    /** Quality Measure variant */
    public static FlaggedOption getOptQualityMeasureVariant(boolean required) {
        return new FlaggedOption("qualityMeasureVariant", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG,
                "qualityVariant", "Quality measure variant.");
    }

    public static FlaggedOption getOptRegressionInformationFile(boolean required) {
        return new FlaggedOption("regressionInformationFile", getInputFileParser(), null, required, 'r',
                JSAP.NO_LONGFLAG, "Regression information file containing the predicted values for each data item.");
    }

    public static FlaggedOption getOptRemoveDirName(boolean required) {
        return new FlaggedOption("removeDir", JSAP.STRING_PARSER, null, required, 'r', "removeDir",
                "Part of name of datum to be removed, if any.");
    }

    public static FlaggedOption getOptReplacement(boolean required) {
        return new FlaggedOption("replacement", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG, "replacement",
                "The string to replace with.");
    }

    public static FlaggedOption getOptTemplateVectorFile(boolean required) {
        return new FlaggedOption("templateVectorFile", getInputFileParser("tv"), null, required, 't', "tv",
                "Template vector file containing vector element labels.");
    }

    public static FlaggedOption getOptReplace(boolean required) {
        return new FlaggedOption("replace", JSAP.STRING_PARSER, null, required, JSAP.NO_SHORTFLAG, "replace",
                "The string to be replace.");
    }

    public static FlaggedOption getOptUnitDescriptionFile(boolean required) {
        return new FlaggedOption("unitDescriptionFile", getInputFileParser("unit"), null, required, 'u',
                JSAP.NO_LONGFLAG, "Unit description file describing a mapped SOM/GHSOM.");
    }

    public static FlaggedOption getOptUnstemmedDirectory(boolean required) {
        return new FlaggedOption("unstemmedDirectory", getInputDirectoryParser(), null, required, JSAP.NO_SHORTFLAG,
                "unstemmedDir", "Path to the directory with the unstemmed files.");
    }

    public static FlaggedOption getOptViewerWorkingDir(boolean required) {
        return new FlaggedOption("viewerWorkingDirectory", getInputDirectoryParser(), null, required,
                JSAP.NO_SHORTFLAG, "datadir",
                "Directory containing the input data items needed for the visualisations.");
    }

    public static UnflaggedOption getOptVisualisations(boolean required) {
        return new UnflaggedOption("vis", JSAP.STRING_PARSER, null, required, true, "The visualisation(s) to create.");
    }

    public static FlaggedOption getOptWeightVectorFile(boolean required) {
        return new FlaggedOption("weightVectorFile", getInputFileParser("wgt"), null, required, 'w', JSAP.NO_LONGFLAG,
                "Weight vector file describing a SOM/GHSOM.");
    }

    public static FlaggedOption getOptWeightVectorFileInit(boolean required) {
        return new FlaggedOption("weightVectorFile", getInputFileParser("wgt"), null, required, 'w', JSAP.NO_LONGFLAG,
                "Weight vector file used for initialization of the map.");
    }

    public static FlaggedOption getOptWidth(boolean required) {
        return new FlaggedOption("width", JSAP.INTEGER_PARSER, "10", required, JSAP.NO_SHORTFLAG, "width",
                "The width of a unit.");
    }

    public static FlaggedOption getOptXMLStructures(boolean required) {
        return new FlaggedOption("XMLStructures", getInputFileParser("xml"), null, required, 'x', JSAP.NO_LONGFLAG,
                "XML Structures.");
    }

    public static FlaggedOption getOptGZip(boolean required, boolean defaultValue) {
        return new FlaggedOption("gzip", JSAP.BOOLEAN_PARSER, String.valueOf(defaultValue), required, 'z', "gzip",
                "Whether or not to gzip the output.");
    }

    public static FlaggedOption getOptShowLabels() {
        return new FlaggedOption("showLabels", JSAP.INTEGER_PARSER, "0", false, JSAP.NO_SHORTFLAG, "showLabels",
                "How many labels per unit should be displayed (default = " + "0" + ")");
    }

    public static FlaggedOption getOptSomPAKFile(boolean required) {
        return new FlaggedOption("somPAKFile", getInputFileParser(), null, required, 'p', "somPAKFile",
                "Name of the SOMPAK input file.");
    }

    public static FlaggedOption getOptStartIndex(boolean required, Integer defaultValue) {
        return new FlaggedOption("startIndex", JSAP.INTEGER_PARSER, String.valueOf(defaultValue), required,
                JSAP.NO_SHORTFLAG, "startIndex", "The start index.");
    }

    public static FlaggedOption getOptStemmedDirectory(boolean required) {
        return new FlaggedOption("stemmedDirectory", getInputDirectoryParser(), null, required, JSAP.NO_SHORTFLAG,
                "stemmedDir", "Path to the directory with the stemmed files.");
    }

    public static Switch getSwitchDocumentMode() {
        return new Switch(
                "documentMode",
                'o',
                JSAP.NO_LONGFLAG,
                "Activates the Document mode, which hides the PlaySOM toolbar for exporting playlists and instead activates a document preview. For this to work properly, you have to specify the document path prefix denoting the base path for the files, e.g. 'file:///c:/somemap/files/', and the suffix, like .html.");
    }

    public static Switch getSwitchDrawLines() {
        return new Switch("drawLines", 'l', "drawLines", "Draw trajectory lines on map.");
    }

    public static Switch getSwitchHtmlOutput(boolean required) {
        return new Switch("htmlOutput", 'h', null, "Generate HTML output.");
    }

    public static Switch getSwitchIgnoreLabelsWithZero() {
        return new Switch("ignoreLabelsWithZero", JSAP.NO_SHORTFLAG, "ignoreZero",
                "Ignore labels with zero mean value and que.");
    }

    public static Switch getSwitchIsDenseData() {
        return new Switch("denseData", JSAP.NO_SHORTFLAG, "dense", "Set if input data vectors are densely populated.");
    }

    public static Switch getSwitchIsNormalized() {
        return new Switch("normalization", JSAP.NO_SHORTFLAG, "normalized",
                "Set, if vectors are normalized to unit length. At the moment this option is not crucial.");
    }

    public static Parameter getSwitchPreserveFeatureOrder() {
        return new Switch("preserveFeatureOrder", JSAP.NO_SHORTFLAG, "preserveFeatureOrder",
                "Wether or not preserve the order of features.");
    }

    public static Parameter getSwitchPreserveVectorOrder() {
        return new Switch("preserveVectorOrder", JSAP.NO_SHORTFLAG, "preserveVectorOrder",
                "Wether or not preserve the order of vectors.");
    }

    public static Switch getSwitchSkipDataWinnerMapping() {
        return new Switch("skipDataWinnerMapping", JSAP.NO_SHORTFLAG, "skipDWM",
                "Skip writing the data winner mapping file");
    }

    public static Switch getSwitchVerboose() {
        return new Switch("verbose", JSAP.NO_SHORTFLAG, "verbose", "Be more verbose...");
    }

    public static FlaggedOption getOptCompressionRate() {
        return new FlaggedOption("compression", JSAP.INTEGER_PARSER, null, true, 'c', "compression",
                "compression rate in percent");
    }

    public static FlaggedOption getOptDocument() {
        return new FlaggedOption("document", getInputFileParser(), null, true, 'd', "document", "document to summarize");
    }

    public static FlaggedOption getOptMethod() {
        return new FlaggedOption("method", JSAP.STRING_PARSER, null, true, 'm', "method",
                "summarization method: tfxidf, location, title, combined");
    }

    public static void main(String[] args) {
        testDuplicateOptions(new OptionFactory());
    }

}