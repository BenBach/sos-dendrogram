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

import java.io.IOException;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.layers.GrowingCellLayer;
import at.tuwien.ifs.somtoolbox.output.GrowingCellStructuresMapOutputter;
import at.tuwien.ifs.somtoolbox.properties.FileProperties;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;

/**
 * This class implements the Growing Cell Structures. It is basically a wrapper for the
 * {@link at.tuwien.ifs.somtoolbox.layers.GrowingCellLayer} and mainly handles command line execution and parameters. It
 * implements the {@link at.tuwien.ifs.somtoolbox.models.NetworkModel} interface which is currently not used, but may be
 * used in the future.
 * 
 * @author Johannes Inführ
 * @author Andreas Zweng
 * @version $Id: GrowingCellStructures.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class GrowingCellStructures extends AbstractNetworkModel {
    protected GrowingCellLayer layer = null;

    // parameters for celllayer, bypassing somproperties (noofiterations==epoch count)

    private static float epsilonB = 0.06f;// adaption of winning unit

    private static float epsilonN = 0.002f;// adaption of topological neighbors of winning unit

    private static float alpha = 0.05f; // reduction of signal frequencies

    private static int lamda = 100; // seen inputs before a unit is added and some are deleted

    private static float eta = 0.09f; // cutoffvalue for normalized probability density

    /**
     * Constructs a new <code>GrowingCellStructures</code> with <code>dim</code>-dimensional weight vectors. Argument
     * <code>norm</code> determines whether the randomly initialized weight vectors should be normalized to unit length
     * or not.
     * 
     * @param dim the dimensionality of the weight vectors.
     * @param normalize specifies if the weight vectors are to be normalized to unit length.
     * @param props the network properties.
     */
    public GrowingCellStructures(int dim, boolean normalize, SOMProperties props, InputData data) {
        layer = new GrowingCellLayer(dim, normalize, props.randomSeed(), data);

    }

    /**
     * Method for stand-alone execution of map training. Options are:<br/>
     * <ul>
     * <li>--noDWM switch to not write the data winner mapping file</li>
     * <li>properties name of properties file, mandatory</li>
     * </ul>
     * 
     * @param args the execution arguments as stated above.
     */
    public static void main(String[] args) {
        InputData data = null;
        FileProperties fileProps = null;

        GrowingCellStructures som = null;
        SOMProperties somProps = null;
        String networkModelName = "GrowingCellStructures";

        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_GROWING_CELL_STRUCTURES);

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting" + networkModelName);

        String propFileName = config.getString("properties");
        // no resuming
        boolean skipDataWinnerMapping = config.getBoolean("skipDataWinnerMapping", false);

        // no labeling

        int numWinners = config.getInt("numberWinners", SOMLibDataWinnerMapping.MAX_DATA_WINNERS);

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Training a new SOM.");

        try {
            fileProps = new FileProperties(propFileName);
            somProps = new SOMProperties(propFileName);
        } catch (PropertiesException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }

        data = getInputData(fileProps);

        som = new GrowingCellStructures(data.dim(), data.isNormalizedToUnitLength(), somProps, data);

        // setting input data so it is accessible by map output
        som.setSharedInputObjects(new SharedSOMVisualisationData(null, null, null, null,
                fileProps.vectorFileName(true), fileProps.templateFileName(true), null));
        som.getSharedInputObjects().setData(SOMVisualisationData.INPUT_VECTOR, data);

        som.train(data, somProps);

        try {
            GrowingCellStructuresMapOutputter.write(som, fileProps.outputDirectory(), fileProps.namePrefix(false),
                    true, somProps, fileProps);
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not open or write to output file " + fileProps.namePrefix(false) + ": " + e.getMessage());
            System.exit(-1);
        }
        if (!skipDataWinnerMapping) {
            numWinners = Math.min(numWinners, som.getLayer().getXSize() * som.getLayer().getYSize());
            try {
                GrowingCellStructuresMapOutputter.writeDataWinnerMappingFile(som, data, numWinners,
                        fileProps.outputDirectory(), fileProps.namePrefix(false), true);
            } catch (IOException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not open or write to output file " + fileProps.namePrefix(false) + ": " + e.getMessage());
                System.exit(-1);
            }
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Skipping writing data winner mapping file");
        }

        // no html output

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("finished" + networkModelName);
    }

    /**
     * Trains a GrowingCellStructures
     * 
     * @param data inputdata used for training
     * @param props properties for training
     */
    private void train(InputData data, SOMProperties props) {
        layer.train(data, epsilonB, epsilonN, alpha, lamda, eta, props);
    }

    public GrowingCellLayer getLayer() {
        return layer;
    }
}
