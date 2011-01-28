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

import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.EditableReportProperties;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;

/**
 * Objects of this type handle the HTML report-writing for Growing Grids. In additon to the information outputtet by the
 * SOMDescription, also information about tau2 is given
 * 
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @version $Id: SOMGGDescriptionHTML.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class SOMGGDescriptionHTML extends SOMDescriptionHTML {

    public SOMGGDescriptionHTML(ReportFileWriter writer, DatasetInformation dataset, TestRunResult testrun,
            String imgDir, EditableReportProperties EP) {
        super(writer, dataset, testrun, imgDir, EP);
    }

    /**
     * initiates the creation of the output Creates the description of the SOM and training properties
     */
    @Override
    protected void printSOMProperties() {

        this.printTopologyOfSOM();
        this.printSOMDimensions();
        this.printSigma();
        this.printTau();
        this.printTau2();
        this.printLearningRate();
        this.printNeighbourhoodFunction();
        this.printMetricUsed();
        this.printNumberOfIterations();
        this.printRandomSeed();
        this.printTrainingTime();
        this.printTrainingDate();

    }

    protected void printTau2() {
        String tau2 = "Property file not defined";
        if (this.testrun.getTau2() >= 0) {
            tau2 = "" + this.testrun.getTau2();
        }
        this.writer.appendOutput("<li>Maximum data representation granularity (tau2): " + tau2 + "</li>\n");
    }

    @Override
    protected void printTopologyOfSOM() {
        this.writer.appendOutput("<li>Type of SOM: Growing Grid</li>\n");
    }
}
