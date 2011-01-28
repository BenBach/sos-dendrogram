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
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMInputReader;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.AbstractQualityMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.output.HTMLOutputter;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.output.labeling.AbstractLabeler;
import at.tuwien.ifs.somtoolbox.output.labeling.Labeler;
import at.tuwien.ifs.somtoolbox.properties.FileProperties;
import at.tuwien.ifs.somtoolbox.properties.GHSOMProperties;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;

/**
 * This class implements the Growing Hierarchical Self-Organizing Map. It is basically a wrapper for the
 * {@link at.tuwien.ifs.somtoolbox.models.GrowingSOM} and mainly handles command line execution and parameters. It
 * implements the {@link at.tuwien.ifs.somtoolbox.models.NetworkModel} interface wich is currently not used, but may be
 * used in the future. It is also not clear, if this class will be removed and replaced by the <code>GrowingSOM</code>,
 * becaus it already contains the hierarchical functionality, only the training procedure would have to be updated.
 * 
 * @author Michael Dittenbach
 * @version $Id: GHSOM.java 3993 2011-01-18 13:15:17Z mayer $
 */
public class GHSOM extends AbstractNetworkModel implements SOMToolboxApp {

    public static String DESCRIPTION = "The Growing Hierarchical SOM grows a hierarchy of maps, depending on the structure of the data set.";

    public static final Type APPLICATION_TYPE = Type.Training;

    // TODO: Long_Description
    public static String LONG_DESCRIPTION = DESCRIPTION;

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getSwitchHtmlOutput(false),
            OptionFactory.getOptLabeling(false), OptionFactory.getOptNumberLabels(false),
            OptionFactory.getOptNumberWinners(false), OptionFactory.getSwitchSkipDataWinnerMapping(),
            OptionFactory.getOptProperties(true) };

    private class ExpandedUnits {
        private Vector<Double> qualities;

        private Vector<Unit> units;

        public ExpandedUnits() {
            units = new Vector<Unit>();
            qualities = new Vector<Double>();
        }

        public void addAll(ExpandedUnits newUnits) {
            units.addAll(newUnits.getUnits());
            qualities.addAll(newUnits.getQualities());
        }

        public void addUnit(Unit unit, double qual) {
            units.addElement(unit);
            qualities.addElement(new Double(qual));
        }

        public int getNumElements() {
            return units.size();
        }

        public Collection<Double> getQualities() {
            return qualities;
        }

        public double getQuality(int i) {
            return qualities.elementAt(i).doubleValue();
        }

        public Unit getUnit(int i) {
            return units.elementAt(i);
        }

        public Collection<Unit> getUnits() {
            return units;
        }

        public void remove(int i) {
            units.remove(i);
            qualities.remove(i);
        }
    }

    /**
     * Method for stand-alone execution of map training.<br>
     * Options are:
     * <ul>
     * <li>-h toggles HTML output</li>
     * <li>-l name of class implementing the labeling algorithm</li>
     * <li>-n number of labels to generate</li>
     * <li>-w name of weight vector file in case of training an already trained map</li>
     * <li>-m name of map description file in case of training an already trained map</li>
     * <li>--noDWM switch to not write the data winner mapping file</li>
     * <li>properties name of properties file, mandatory</li>
     * </ul>
     * 
     * @param args the execution arguments as stated above.
     */
    public static void main(String[] args) {
        InputData data = null;
        FileProperties fileProps = null;

        GHSOM som = null;
        GHSOMProperties somProps = null;
        String networkModelName = "GHSOM";

        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting" + networkModelName);
        // Logger.getLogger("at.tuwien.ifs.somtoolbox").setLevel(Level.FINER);

        String propFileName = AbstractOptionFactory.getFilePath(config, "properties");
        String weightFileName = config.getString("weightVectorFile", null);
        // String mapDescFileName = config.getString("mapDescriptionFile", null);
        String labelerName = config.getString("labeling", null);
        int numLabels = config.getInt("numberLabels", DEFAULT_LABEL_COUNT);
        boolean skipDataWinnerMapping = config.getBoolean("skipDataWinnerMapping", false);
        Labeler labeler = null;
        // TODO: use parameter for max
        int numWinners = config.getInt("numberWinners", SOMLibDataWinnerMapping.MAX_DATA_WINNERS);

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

        if (weightFileName == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Training a new SOM.");
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Further training of an already trained SOM.");
        }

        try {
            fileProps = new FileProperties(propFileName);
            somProps = new GHSOMProperties(propFileName);
        } catch (PropertiesException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }

        data = getInputData(fileProps);

        som = new GHSOM(data.dim(), data.isNormalizedToUnitLength(), somProps, data);
        som.setSharedInputObjects(new SharedSOMVisualisationData(null, null, null, null,
                fileProps.vectorFileName(true), fileProps.templateFileName(true), null));
        som.getSharedInputObjects().setData(SOMVisualisationData.INPUT_VECTOR, data);
        som.train(data, somProps);

        if (labelerName != null) { // if labeling then label
            labeler.label(som, data, numLabels);
        }

        try {
            // TODO: make output format an argument
            SOMLibMapOutputter.write(som, fileProps.outputDirectory(), fileProps.namePrefix(false), true, somProps,
                    fileProps);
        } catch (IOException e) { // TODO: create new exception type
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not open or write to output file " + fileProps.namePrefix(false) + ": " + e.getMessage());
            System.exit(-1);
        }
        if (!skipDataWinnerMapping) {
            try {
                SOMLibMapOutputter.writeDataWinnerMappingFile(som, data, numWinners, fileProps.outputDirectory(),
                        fileProps.namePrefix(false), true);
            } catch (IOException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not open or write to output file " + fileProps.namePrefix(false) + ": " + e.getMessage());
                System.exit(-1);
            }
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Skipping writing data winner mapping file");
        }

        if (config.getBoolean("htmlOutput") == true) {
            try {
                // TODO: make output format an argument, zipped output
                new HTMLOutputter().write(som, fileProps.outputDirectory(), fileProps.namePrefix(false));

            } catch (IOException e) { // TODO: create new exception type
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not open or write to output file " + fileProps.namePrefix(false) + ": " + e.getMessage());
                System.exit(-1);
            }
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("finished" + networkModelName);
    }

    private GrowingLayer layer0 = null;

    private GrowingSOM topLayerMap = null;

    /**
     * Constructs a new <code>GHSOM</code> with <code>dim</code>-dimensional weight vectors. Argument <code>norm</code>
     * determines whether the randlomy initialized weight vectors should be normalized to unit length or not.
     * 
     * @param dim the dimensionality of the weight vectors.
     * @param norm specifies if the weight vectors are to be normalized to unit length.
     * @param props the network properties.
     */
    public GHSOM(int dim, boolean norm, GHSOMProperties props, InputData data) {
        layer0 = new GrowingLayer(1, 1, "at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric", dim, norm, props.pca(), 7,
                data);
        try {
            topLayerMap = new GrowingSOM(0, layer0.getUnit(0, 0), dim, norm, props, data);
        } catch (LayerAccessException e) { /* does not happen */
            assert false;
        }
    }

    /* (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel#setSharedInputObjects(at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData)
     */
    @Override
    public void setSharedInputObjects(SharedSOMVisualisationData sharedInputObjects) {
        super.setSharedInputObjects(sharedInputObjects);
        if (topLayerMap != null) {
            topLayerMap.setSharedInputObjects(sharedInputObjects);
        }
    }

    /**
     * Constructs an already trained <code>GHSOM</code> with a <code>SOMInputReader</code> provided by argument
     * <code>ir</code>.
     * 
     * @param ir an object implementing the <code>SOMinputReader</code> interface to load an already trained model.
     */
    public GHSOM(SOMInputReader ir) {
        topLayerMap = new GrowingSOM(ir);
    }

    // FIXME: this method should be moved to at.tuwien.ifs.somtoolbox.layers.GrowingLayer
    public ExpandedUnits getExpandedUnits(GrowingLayer layer, QualityMeasure qm, String qmName, double fraction,
            double totalQuality) {
        ExpandedUnits expUnits = new ExpandedUnits();
        double[][] quality = null;
        try {
            quality = qm.getUnitQualities(qmName);
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }
        try {
            for (int j = 0; j < layer.getYSize(); j++) {
                for (int i = 0; i < layer.getXSize(); i++) {
                    boolean willExpand = quality[i][j] > fraction * totalQuality
                            && layer.getUnit(i, j).getNumberOfMappedInputs() > 0;
                    if (Logger.getLogger("at.tuwien.ifs.somtoolbox").isLoggable(Level.INFO)) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                                "Expanding unit " + i + "/" + j + ": " + willExpand + "( unit quality: "
                                        + quality[i][j] + ", required quality: " + fraction + " * " + totalQuality
                                        + " =" + fraction * totalQuality + ")");
                    }
                    if (willExpand) {
                        expUnits.addUnit(layer.getUnit(i, j), quality[i][j]);
                    }
                }
            }
        } catch (LayerAccessException e) { /* does not happen */
            e.printStackTrace();
        }
        return expUnits;
    }

    /**
     * Returns the top-layer map
     * 
     * @return the top-layer map.
     */
    public GrowingSOM topLayerMap() {
        return topLayerMap;
    }

    /**
     * Trains the GHSOM with the input data and training parameters specified in the properties provided by argument
     * <code>props</code>.
     * 
     * @param data input data to train the map with.
     * @param props the training properties.
     */
    public void train(InputData data, GHSOMProperties props) {
        // String qualityMeasureName1 = "at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError.mqe";
        String growthQMName = props.growthQualityMeasureName();
        String expandQMName = props.expandQualityMeasureName();
        String[] growthQM = AbstractQualityMeasure.splitNameAndMethod(growthQMName);
        String[] expandQM = AbstractQualityMeasure.splitNameAndMethod(expandQMName);

        // set layer 0 unit to mean of data
        try {
            layer0.getUnit(0, 0).setWeightVector(data.getMeanVector().toArray());
            layer0.getUnit(0, 0).addMappedInput(data, false);
        } catch (SOMToolboxException e) { /* does not happen */
        }

        // calculate map error
        QualityMeasure qm0 = null;
        try {
            qm0 = AbstractQualityMeasure.instantiate(growthQM[0], layer0, data);// new QuantizationError(layer0, data);
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not instantiate quality measure.");
            System.exit(-1);
        }
        double totalMqe = Double.MAX_VALUE;
        try {
            totalMqe = qm0.getMapQuality(growthQM[1]);
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }

        QualityMeasure qm1 = topLayerMap.train(data, props, totalMqe, growthQMName);

        // check units for expansion
        ExpandedUnits unitQueue = getExpandedUnits(topLayerMap.getLayer(), qm1, expandQM[1], props.tau2(), totalMqe);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(unitQueue.getNumElements() + " units to expand.");

        int id = 1;
        while (unitQueue.getNumElements() > 0) {
            // pick next unit
            Unit currentUnit = unitQueue.getUnit(0);
            double currentQuality = unitQueue.getQuality(0);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(unitQueue.getNumElements() + " units to expand.");

            // create new map and assign to unit
            GrowingSOM newGSOM = new GrowingSOM(++id, currentUnit, data.dim(), data.isNormalizedToUnitLength(), props,
                    data);
            newGSOM.setSharedInputObjects(topLayerMap.getSharedInputObjects());
            currentUnit.setMappedSOM(newGSOM);

            // generate subset of data
            InputData newData = data.subset(currentUnit.getMappedInputNames());

            // train map
            QualityMeasure qm = currentUnit.getMappedSOM().train(newData, props, currentQuality, growthQMName);

            // calcuate quality
            // QualityMeasure qm = new QuantizationError(currentUnit.getMappedSOM().getLayer(), newData);
            ExpandedUnits newUnits = getExpandedUnits(newGSOM.getLayer(), qm, expandQM[1], props.tau2(), totalMqe);
            unitQueue.addAll(newUnits);
            unitQueue.remove(0);
        }
    }

}
