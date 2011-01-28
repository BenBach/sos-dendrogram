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
package at.tuwien.ifs.somtoolbox.apps.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.output.labeling.AbstractLabeler;
import at.tuwien.ifs.somtoolbox.output.labeling.Labeler;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * Maps inputs to an already trained SOM.
 * 
 * @author Angela Roiger
 * @author Rudolf Mayer
 * @version $Id: DataMapper.java 3987 2011-01-10 15:23:49Z mayer $
 */
public class DataMapper implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptWeightVectorFile(true),
            OptionFactory.getOptMapDescriptionFile(false), OptionFactory.getOptInputVectorFile(true),
            OptionFactory.getOptUnitDescriptionFile(false), OptionFactory.getOptClassInformationFile(false),
            OptionFactory.getOptClasslist(false), OptionFactory.getOptLabeling(false),
            OptionFactory.getOptNumberLabels(false), OptionFactory.getOptNumberWinners(false),
            OptionFactory.getSwitchSkipDataWinnerMapping(), OptionFactory.getOptOutputFileName(false) };

    public static final String DESCRIPTION = "Maps inputs to an already trained SOM.";

    public static final String LONG_DESCRIPTION = DESCRIPTION.concat(" If a unit-file is given, the data items are added to the loaded map, without a unti file the mapping starts with an empty map.");

    public static final Type APPLICATION_TYPE = Type.Utils;

    public static void main(String[] args) throws FileNotFoundException, IOException, SOMToolboxException {
        new DataMapper(args);
    }

    public DataMapper(String[] args) throws FileNotFoundException, IOException, SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String mapDescFileName = AbstractOptionFactory.getFilePath(config, "mapDescriptionFile");
        String weightVectorFileName = AbstractOptionFactory.getFilePath(config, "weightVectorFile");
        String unitDescriptionFileName = AbstractOptionFactory.getFilePath(config, "unitDescriptionFile");
        String classInformationFileName = AbstractOptionFactory.getFilePath(config, "classInformationFile");
        String outputPrefix = AbstractOptionFactory.getFilePath(config, "output");

        String skipClassesString = config.getString("classList");
        boolean skipDataWinnerMapping = config.getBoolean("skipDataWinnerMapping", false);
        int numDataWinners = config.getInt("numberWinners");
        String labelerName = config.getString("labeling", null);
        int numLabels = config.getInt("numberLabels", AbstractNetworkModel.DEFAULT_LABEL_COUNT);

        ArrayList<String> mappingExceptions = new ArrayList<String>();
        if (StringUtils.isNotBlank(skipClassesString)) {
            String[] tmp = skipClassesString.split(",");
            for (String element : tmp) {
                mappingExceptions.add(element);
            }
        }

        GrowingSOM som = null;
        /* restore SOM */
        try {
            som = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFileName, unitDescriptionFileName,
                    mapDescFileName));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            e.printStackTrace();
            System.exit(-1);
        }

        SOMLibClassInformation classInfo = null;
        if (classInformationFileName != null) {
            try {
                classInfo = new SOMLibClassInformation(classInformationFileName);
            } catch (SOMToolboxException e1) {
                e1.printStackTrace();
            }
        }

        InputData data = new SOMLibSparseInputData(AbstractOptionFactory.getFilePath(config, "inputVectorFile"));
        // map the data
        mapCompleteDataAfterTraining(som, data, classInfo, mappingExceptions, labelerName, numLabels);
        // compute quality measure
        // TODO FIXME : pass the quality measure as parameter!
        String qualityMeasureName = "at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError.mqe";
        som.getLayer().setQualityMeasure(qualityMeasureName);

        if (outputPrefix == null) {
            outputPrefix = FileUtils.extractSOMLibInputPrefix(FileUtils.stripPathPrefix(weightVectorFileName))
                    + ".remapped";
        }
        try {
            SOMLibMapOutputter.writeUnitDescriptionFile(som, "", outputPrefix, true);
        } catch (IOException e) { // TODO: create new exception type
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not open or write to output file " + outputPrefix + ": " + e.getMessage());
            System.exit(-1);
        }
        if (!skipDataWinnerMapping) {
            int numWinners = Math.min(numDataWinners, som.getLayer().getXSize() * som.getLayer().getYSize());
            try {
                SOMLibMapOutputter.writeDataWinnerMappingFile(som, data, numWinners, "", outputPrefix, true);
            } catch (IOException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not open or write to output file " + outputPrefix + ": " + e.getMessage());
                System.exit(-1);
            }
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Skipping writing data winner mapping file");
        }
        // just copy along the class information file, so we have a copy with the same name-prefix, eases SOMViewer
        // starting..
        if (classInformationFileName != null) {
            String classInfoDestination = outputPrefix + ".cls"
                    + (classInformationFileName.endsWith(".gz") ? ".gz" : "");
            FileUtils.copyFile(classInformationFileName, classInfoDestination);
        }
    }

    /**
     * @see GrowingLayer#mapCompleteDataAfterTraining
     */
    // FIXME: this is just a copy of GrowingLayer#mapCompleteDataAfterTraining, would be good to have some code
    // re-used..
    // FIXME: this would also profit from multi-threading...
    private void mapCompleteDataAfterTraining(GrowingSOM som, InputData data, SOMLibClassInformation classInfo,
            ArrayList<String> mappingExceptions, String labelerName, int numLabels) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start mapping data.");
        InputDatum datum = null;
        Unit winner = null;
        int numVectors = data.numVectors();

        int skippedInstances = 0;
        for (int i = 0; i < data.numVectors(); i++) {
            try {
                InputDatum currentInput = data.getInputDatum(i);
                String inpLabel = currentInput.getLabel();
                if (classInfo != null && mappingExceptions.contains(classInfo.getClassName(inpLabel))) {
                    skippedInstances++;
                }
            } catch (SOMLibFileFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (mappingExceptions.size() > 0) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Skipping classes: " + mappingExceptions + ", containing a total of " + skippedInstances
                            + " inputs.");
        }

        StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors - skippedInstances, "Mapping datum ",
                50);
        L2Metric metric = new L2Metric();
        for (int i = 0; i < numVectors; i++) {
            datum = data.getInputDatum(i);

            String inpLabel = datum.getLabel();
            try {
                if (classInfo != null && mappingExceptions.contains(classInfo.getClassName(inpLabel))) {
                    continue; // Skips this mapping step
                } else {
                    winner = som.getLayer().getWinner(datum, metric);
                    winner.addMappedInput(datum, false); // TODO: think about recursion
                    progressWriter.progress();
                }
            } catch (SOMLibFileFormatException e) {
                // TODO Auto-generated catch block
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("This should never happen");
                e.printStackTrace();
            }
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished mapping data.");
        som.getLayer().calculateQuantizationErrorForUnits();
        som.getLayer().clearLabels();

        Labeler labeler = null;

        if (labelerName != null) { // if labeling then label
            try {
                labeler = AbstractLabeler.instantiate(labelerName);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Instantiated labeler " + labelerName);
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not instantiate labeler \"" + labelerName + "\".");
                System.exit(-1);
            }
        }

        if (labelerName != null) { // if labeling then label
            labeler.label(som, data, numLabels);
        }
    }

}
