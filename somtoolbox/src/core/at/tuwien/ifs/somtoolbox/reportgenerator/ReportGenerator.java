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

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.MnemonicSOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.reportgenerator.gui.ReportGenWindow;
import at.tuwien.ifs.somtoolbox.reportgenerator.output.OutputReport;
import at.tuwien.ifs.somtoolbox.reportgenerator.output.OutputReportHtml;
import at.tuwien.ifs.somtoolbox.reportgenerator.output.OutputReportLATEX;
import at.tuwien.ifs.somtoolbox.util.UiUtils;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @version $Id: ReportGenerator.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class ReportGenerator {

    public static final int HTML_REPORT = 1;

    public static final int LATEX_REPORT = 2;

    /**
     * the main method if the report generator is called as a stand alone application
     * 
     * @throws FileNotFoundException if a file could not be found.
     */
    public static void main(String[] args) throws FileNotFoundException, SOMLibFileFormatException {
        Parameter[] options = new Parameter[] { OptionFactory.getOptUnitDescriptionFile(true),
                OptionFactory.getOptWeightVectorFile(true), OptionFactory.getOptClassInformationFile(false),
                OptionFactory.getOptMapDescriptionFile(false), OptionFactory.getOptDataInformationFileFile(false),
                OptionFactory.getOptDataWinnerMappingFile(false), OptionFactory.getOptInputVectorFile(false),
                OptionFactory.getOptTemplateVectorFile(false), OptionFactory.getOptOutputFileName(false),
                OptionFactory.getOptProperties(false) };
        JSAPResult config = OptionFactory.parseResults(args, options);

        MnemonicSOMLibFormatInputReader inputReader = new MnemonicSOMLibFormatInputReader(
                config.getString("weightVectorFile"), config.getString("getOptUnitDescriptionFile"),
                config.getString("mapDescriptionFile"));
        GrowingSOM gsom = new GrowingSOM(inputReader);

        CommonSOMViewerStateData state = new CommonSOMViewerStateData();
        state.growingSOM = gsom;
        state.growingLayer = gsom.getLayer();
        state.somInputReader = inputReader;

        SharedSOMVisualisationData data = new SharedSOMVisualisationData();
        data.setFileName(SOMVisualisationData.CLASS_INFO, config.getString("classInformationFile"));
        data.setFileName(SOMVisualisationData.DATA_WINNER_MAPPING, config.getString("dataWinnerMappingFile"));
        data.setFileName(SOMVisualisationData.INPUT_VECTOR, config.getString("inputVectorFile"));
        data.setFileName(SOMVisualisationData.TEMPLATE_VECTOR, config.getString("templateVectorFile"));
        data.readAvailableData();
        state.inputDataObjects = data;
        UiUtils.setSOMToolboxLookAndFeel();
        new ReportGenerator(true, state, config.getString("output"), config.getString("properties"));
    }

    public ReportGenerator(boolean standalone, CommonSOMViewerStateData state) {
        this(standalone, state, null, null);
    }

    public ReportGenerator(boolean standalone, CommonSOMViewerStateData state, String outputFile, String propertiesFile) {
        new ReportGenWindow(standalone, this, state, outputFile, propertiesFile);
    }

    public void createReport(int type, String outputDirectory, DatasetInformation datasetInfo,
            TestRunResultCollection testruns) {

        OutputReport report = null;

        switch (type) {
            case HTML_REPORT:
                report = new OutputReportHtml(outputDirectory, datasetInfo.getEP());
                break;

            case LATEX_REPORT:
                report = new OutputReportLATEX(outputDirectory);
                break;

            default:
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").severe("unkown report type ... no report created.");
                return;
        }
        report.setDatasetInformation(datasetInfo);
        report.setTestrunInformation(testruns, type);
        report.createOutput();
    }
}
