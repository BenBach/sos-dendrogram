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

import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.properties.GHSOMProperties;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;

/**
 * This class stores information about a training run that produced a SOM of type Growing Grid (gg) In addition to all
 * features of the TestRunResultClass, it also prodived a tau2 (taken from the properties file)
 * 
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: GGSOMTestRunResult.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class GGSOMTestRunResult extends TestRunResult {

    /** instead of the SOMProperties, we now have GHSOMPropterties */
    private GHSOMProperties props = null;

    /**
     * creates a new instance of this type, by handing over a TestRunResult object. All information needed are then
     * taken from this object (all the paths to the files, etc ...
     * 
     * @param result an Object from which all the filepaths, the dataset information and the run id can be taken
     */
    public GGSOMTestRunResult(TestRunResult result) {

        super(result.getDatasetInfo(), result.getMapFilePath(), result.getPropertyFilePath(), result.getUnitFilePath(),
                result.getWeightFilePath(), result.getDWFilePath(), result.getRunId(), result.getType());

        try {
            this.props = new GHSOMProperties(result.getPropertyFilePath());
        } catch (PropertiesException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot load the properties file for run " + this.getRunId() + ". Will try without. Reason: " + e);
            this.props = null;
        }
    }

    /**
     * the explicit constructor for this object, that allows to define all required information separatly
     * 
     * @param datasetInfo containing all interesting information about the input vectors
     * @param mapFilePath absolute path to the map file (.map[.gz])
     * @param propertyFilePath absolute path to the property file (.prop)
     * @param unitFilePath absolute path to the unit file (.unit[.gz])
     * @param weightFilePath absolute path to the weight vector file (.wgt[.gz])
     * @param dwFilePath absolute path to the data winner mapping file (.dwm[.gz])
     * @param runId the id of this training run (start counting with 0)
     */
    public GGSOMTestRunResult(DatasetInformation datasetInfo, String mapFilePath, String propertyFilePath,
            String unitFilePath, String weightFilePath, String dwFilePath, int runId, int type) {

        super(datasetInfo, mapFilePath, propertyFilePath, unitFilePath, weightFilePath, dwFilePath, runId, type);
    }

    /**
     * returns the training parameter tau2 (that is the measure of the maximum data representation granularity that is
     * used in the training process)
     * 
     * @return the value of taus2
     */
    @Override
    public double getTau2() {
        if (this.props == null) {
            return -1;
        }
        return this.props.tau2();
    }
}
