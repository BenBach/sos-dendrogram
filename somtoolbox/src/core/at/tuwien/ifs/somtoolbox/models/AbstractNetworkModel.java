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
package at.tuwien.ifs.somtoolbox.models;

import java.util.Date;

import at.tuwien.ifs.somtoolbox.data.DataBaseSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.database.MySQLConnector;
import at.tuwien.ifs.somtoolbox.properties.FileProperties;
import at.tuwien.ifs.somtoolbox.util.DateUtils;

/**
 * This class provides basic support for implementing a NetworkModel.
 * 
 * @author Rudolf Mayer
 * @version $Id: AbstractNetworkModel.java 3830 2010-10-06 16:29:11Z mayer $
 */
public abstract class AbstractNetworkModel implements NetworkModel {

    // FIXME: this value should be bigger if we use kaski/gate labels
    public static final int DEFAULT_LABEL_COUNT = 5;

    protected Date trainingStart = new Date();

    /** whether or not the SOM is labelled */
    protected boolean labelled = false;

    protected static InputData getInputData(FileProperties fileProps) {
        InputData data;
        if (fileProps.isUsingDatabase()) {// Invoke database driven SOMLib input reader
            MySQLConnector dbConnector = new MySQLConnector(fileProps.getDatabaseServerAddress(),
                    fileProps.getDatabaseName(), fileProps.getDatabaseUser(), fileProps.getDatabasePassword(),
                    fileProps.getDatabaseTableNamePrefix());
            data = new DataBaseSOMLibSparseInputData(dbConnector, fileProps.sparseData(), fileProps.isNormalized(),
                    fileProps.numCacheBlocks(), fileProps.randomSeed());
        } else {// Invoke regular SOMLib input reader
            data = InputDataFactory.open(fileProps.vectorFileName(true), fileProps.templateFileName(true),
                    fileProps.sparseData(), fileProps.isNormalized(), fileProps.numCacheBlocks(),
                    fileProps.randomSeed());
        }
        return data;
    }

    protected String printTrainingTime() {
        return DateUtils.formatDuration(new Date().getTime() - trainingStart.getTime());
    }

    public void setSharedInputObjects(SharedSOMVisualisationData sharedInputObjects) {
        this.sharedInputObjects = sharedInputObjects;
    }

    public SharedSOMVisualisationData getSharedInputObjects() {
        return sharedInputObjects;
    }

    protected SharedSOMVisualisationData sharedInputObjects;

    public boolean isLabelled() {
        return labelled;
    }

    public void setLabelled(boolean labelled) {
        this.labelled = labelled;
    }

}
