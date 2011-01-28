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
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang.NotImplementedException;

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
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Layer.GridTopology;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.ToroidLayer;
import at.tuwien.ifs.somtoolbox.layers.TrainingInterruptionListener;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;
import at.tuwien.ifs.somtoolbox.output.HTMLOutputter;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.output.labeling.AbstractLabeler;
import at.tuwien.ifs.somtoolbox.output.labeling.Labeler;
import at.tuwien.ifs.somtoolbox.properties.FileProperties;
import at.tuwien.ifs.somtoolbox.properties.GHSOMProperties;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * This class implements the Growing Self-Organizing Map. It is basically a wrapper for the
 * {@link at.tuwien.ifs.somtoolbox.layers.GrowingLayer} and mainly handles command line execution and parameters. It
 * implements the {@link at.tuwien.ifs.somtoolbox.models.NetworkModel} interface wich is currently not used, but may be
 * used in the future.
 * 
 * @author Michael Dittenbach
 * @version $Id: GrowingSOM.java 3883 2010-11-02 17:13:23Z frank $
 */
public class GrowingSOM extends AbstractNetworkModel implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getSwitchHtmlOutput(false),
            OptionFactory.getOptLabeling(false), OptionFactory.getOptNumberLabels(false),
            OptionFactory.getOptWeightVectorFileInit(false), OptionFactory.getOptMapDescriptionFile(false),
            OptionFactory.getSwitchSkipDataWinnerMapping(), OptionFactory.getOptNumberWinners(false),
            OptionFactory.getOptProperties(true), OptionFactory.getOptUseMultiCPU(false) };

    public static final Type APPLICATION_TYPE = Type.Training;

    public static String DESCRIPTION = "Provides a Growing Grid and a static SOM (with the growth parameters disabled)";

    // TODO: Long_Description
    public static String LONG_DESCRIPTION = "Provides a Growing Grid (Bernd Fritzke, 1995), i.e. a Self-Organising Map that can dynamically grow by inserting whole rows or cells. When setting the growth-controlling parameters to disable growth, it can also be used to train a standard SOM.";

    /**
     * Method for stand-alone execution of map training. Options are:<br/>
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

        GrowingSOM som = null;
        SOMProperties somProps = null;
        String networkModelName = "GrowingSOM";

        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting" + networkModelName);

        int cpus = config.getInt("cpus", 1);
        int systemCPUs = Runtime.getRuntime().availableProcessors();
        // We do not use more CPUs than available!
        if (cpus > systemCPUs) {
            String msg = "Number of CPUs required exceeds number of CPUs available.";
            if (cpus > 2 * systemCPUs) {
                msg += "Limiting to twice the number of available processors: " + 2 * systemCPUs;
                cpus = 2 * systemCPUs;
            }
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(msg);
        }
        GrowingLayer.setNO_CPUS(cpus);

        String propFileName = AbstractOptionFactory.getFilePath(config, "properties");
        String weightFileName = AbstractOptionFactory.getFilePath(config, "weightVectorFile");
        String mapDescFileName = AbstractOptionFactory.getFilePath(config, "mapDescriptionFile");
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
            somProps = new SOMProperties(propFileName);
        } catch (PropertiesException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }

        data = getInputData(fileProps);

        if (weightFileName == null) {
            som = new GrowingSOM(data.isNormalizedToUnitLength(), somProps, data);
        } else {
            try {
                som = new GrowingSOM(new SOMLibFormatInputReader(weightFileName, null, mapDescFileName));
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
                System.exit(-1);
            }
        }

        if (somProps.getDumpEvery() > 0) {
            IntermediateSOMDumper dumper = som.new IntermediateSOMDumper(fileProps);
            som.layer.setTrainingInterruptionListener(dumper, somProps.getDumpEvery());
        }

        // setting input data so it is accessible by map output
        som.setSharedInputObjects(new SharedSOMVisualisationData(null, null, null, null,
                fileProps.vectorFileName(true), fileProps.templateFileName(true), null));
        som.getSharedInputObjects().setData(SOMVisualisationData.INPUT_VECTOR, data);

        som.train(data, somProps);

        if (labelerName != null) { // if labeling then label
            labeler.label(som, data, numLabels);
        }

        try {
            SOMLibMapOutputter.write(som, fileProps.outputDirectory(), fileProps.namePrefix(false), true, somProps,
                    fileProps);
        } catch (IOException e) { // TODO: create new exception type
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not open or write to output file " + fileProps.namePrefix(false) + ": " + e.getMessage());
            System.exit(-1);
        }
        if (!skipDataWinnerMapping) {
            numWinners = Math.min(numWinners, som.getLayer().getXSize() * som.getLayer().getYSize());
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
                new HTMLOutputter().write(som, fileProps.outputDirectory(), fileProps.namePrefix(false));
            } catch (IOException e) { // TODO: create new exception type
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not open or write to output file " + fileProps.namePrefix(false) + ": " + e.getMessage());
                System.exit(-1);
            }
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "finished" + networkModelName + "(" + som.getLayer().getGridLayout() + ", "
                        + som.getLayer().getGridTopology() + ")");
    }

    protected GrowingLayer layer = null;

    public GrowingSOM(Properties properties) throws PropertiesException {
        FileProperties fileProps = new FileProperties(properties);
        SOMProperties somProps = new SOMProperties(properties);
        InputData data = getInputData(fileProps);

        // setting input data so it is accessible by map output
        setSharedInputObjects(new SharedSOMVisualisationData(null, null, null, null, fileProps.vectorFileName(true),
                fileProps.templateFileName(true), null));
        getSharedInputObjects().setData(SOMVisualisationData.INPUT_VECTOR, data);

        initLayer(data.isNormalizedToUnitLength(), somProps, data);
        train(data, somProps);
        if (somProps.getDumpEvery() > 0) {
            IntermediateSOMDumper dumper = new IntermediateSOMDumper(fileProps);
            layer.setTrainingInterruptionListener(dumper, somProps.getDumpEvery());
        }
    }

    /**
     * Constructs a new <code>GrowingSOM</code> with <code>dim</code>-dimensional weight vectors. Argument
     * <code>norm</code> determines whether the randomly initialised weight vectors should be normalised to unit length
     * or not.
     * 
     * @param norm specifies if the weight vectors are to be normalised to unit length.
     * @param props the network properties.
     */
    public GrowingSOM(boolean norm, SOMProperties props, InputData data) {
        initLayer(norm, props, data);
    }

    private void initLayer(boolean norm, SOMProperties props, InputData data) {
        if (props.getGridTopology() == GridTopology.planar) {
            layer = new GrowingLayer(props.xSize(), props.ySize(), props.zSize(), props.metricName(), data.dim(), norm,
                    props.pca(), props.randomSeed(), data);
        } else if (props.getGridTopology() == GridTopology.toroid) {
            layer = new ToroidLayer(props.xSize(), props.ySize(), props.zSize(), props.metricName(), data.dim(), norm,
                    props.pca(), props.randomSeed(), data);
        } else {
            throw new NotImplementedException("Supported for grid topology " + props.getGridTopology()
                    + " not yet implemented.");
        }
    }

    /**
     * Constructs and trains a new <code>GrowingSOM</code>. All the non-specified parameters will be automatically set
     * to <i>"default"</i> values.
     */
    public GrowingSOM(int xSize, int ySize, int numIterations, InputData data) throws PropertiesException {
        SOMProperties props = new SOMProperties(xSize, ySize, numIterations, SOMProperties.defaultLearnRate);
        initLayer(false, props, data);
        train(data, props);
    }

    /** Constructs and trains a new <code>GrowingSOM</code>. */
    public GrowingSOM(int xSize, int ySize, int zSize, String metricName, int numIterations, boolean normalised,
            boolean usePCAInit, int randomSeed, InputData data) throws PropertiesException {
        SOMProperties props = new SOMProperties(xSize, ySize, zSize, randomSeed, 0, numIterations,
                SOMProperties.defaultLearnRate, -1, -1, null, usePCAInit);
        initLayer(false, props, data);
        train(data, props);
    }

    /**
     * Constructs a new <code>GrowingSOM</code> with <code>dim</code>-dimensional weight vectors. Argument
     * <code>norm</code> determines whether the randlomy initialized weight vectors should be normalized to unit length
     * or not. In hierarchical network models consisting of multiple maps such as the {@link GHSOM}, a unique identifier
     * is assigned by argument <code>id</code> and the superordinate unit is provided by argument <code>su</code>.
     * 
     * @param id a unique identifier used in hierarchies of maps (e.g. the <code>GHSOM</code>).
     * @param su the superordinate unit of the map.
     * @param dim the dimensionality of the weight vectors.
     * @param norm specifies if the weight vectors are to be normalized to unit length.
     * @param props the network properties.
     */
    public GrowingSOM(int id, Unit su, int dim, boolean norm, SOMProperties props, InputData data) {
        layer = new GrowingLayer(id, su, props.xSize(), props.ySize(), props.zSize(), props.metricName(), dim, norm,
                props.pca(), props.randomSeed(), data);
    }

    /**
     * Private constructor used recursively in hierarchical network models consisting of multiple maps. A unique
     * identifier is assigned by argument <code>id</code> and the superordinate unit is provided by argument
     * <code>su</code>.
     * 
     * @param id a unique identifier used in hierarchies of maps (e.g. the <code>GHSOM</code>).
     * @param su the superordinate unit of the map.
     * @param ir an object implementing the <code>SOMinputReader</code> interface to load an already trained model.
     */
    protected GrowingSOM(int id, Unit su, SOMInputReader ir) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Starting layer restoration.");

        // FIXME: the initialisation of the layer should actually be done in the layer class itself
        try {
            // TODO: think about rand seed (7), use map description file when provided
            layer = new GrowingLayer(id, su, ir.getXSize(), ir.getYSize(), ir.getZSize(), ir.getMetricName(),
                    ir.getDim(), ir.getVectors(), 7);
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            System.exit(-1);
        }
        labelled = ir.isLabelled();

        restoreLayer(id, ir, layer);
    }

    protected GrowingSOM(int id, Unit su, SOMInputReader ir, GrowingLayer layer) {
        this.layer = layer;
        labelled = ir.isLabelled();
        restoreLayer(id, ir, layer);
    }

    private void restoreLayer(int id, SOMInputReader ir, GrowingLayer layer) {
        layer.setGridLayout(ir.getGridLayout());
        layer.setGridTopology(ir.getGridTopology());

        int numUnits = layer.getXSize() * layer.getYSize();
        int currentUnitNum = 0;

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Restoring state of " + numUnits + " units: ");

        StdErrProgressWriter progressWriter = new StdErrProgressWriter(numUnits, "Restoring state of unit ", 10);
        try {
            for (int j = 0; j < layer.getYSize(); j++) {
                for (int i = 0; i < layer.getXSize(); i++) {
                    // adapted to mnemonic (sparse) SOMs
                    if (layer.getUnit(i, j, 0) == null) { // if this unit is empty, i.e. not part of the mnemonic map
                        // --> we skip it
                        progressWriter.progress("Skipping empty unit " + i + "/" + j + ", ", (currentUnitNum + 1));
                    } else { // otherwise we read this unit
                        progressWriter.progress("Restoring state of unit " + i + "/" + j + ", ", (currentUnitNum + 1));
                        layer.getUnit(i, j, 0).restoreMappings(ir.getNrVecMapped(i, j), ir.getMappedVecs(i, j),
                                ir.getMappedVecsDist(i, j));
                        layer.getUnit(i, j, 0).restoreLabels(ir.getNrUnitLabels(i, j), ir.getUnitLabels(i, j),
                                ir.getUnitLabelsQe(i, j), ir.getUnitLabelsWgt(i, j));
                        layer.getUnit(i, j, 0).restoreKaskiLabels(ir.getNrKaskiLabels(i, j),
                                ir.getKaskiUnitLabels(i, j), ir.getKaskiUnitLabelsWgt(i, j));
                        layer.getUnit(i, j, 0).restoreKaskiGateLabels(ir.getNrKaskiGateLabels(i, j),
                                ir.getKaskiGateUnitLabels(i, j, 0));
                        if (ir.getNrSomsMapped(i, j) > 0) { // if expanded then create new growingsom
                            String subWeightFileName = null;
                            if (ir.getWeightVectorFileName() != null) {
                                subWeightFileName = ir.getFilePath() + ir.getUrlMappedSoms(i, j)[0]
                                        + SOMLibFormatInputReader.weightFileNameSuffix;
                            }
                            String subUnitFileName = null;
                            if (ir.getUnitDescriptionFileName() != null) {
                                subUnitFileName = ir.getFilePath() + ir.getUrlMappedSoms(i, j)[0]
                                        + SOMLibFormatInputReader.unitFileNameSuffix;
                            }
                            String subMapFileName = null;
                            if (ir.getMapDescriptionFileName() != null) {
                                subMapFileName = ir.getFilePath() + ir.getUrlMappedSoms(i, j)[0]
                                        + SOMLibFormatInputReader.mapFileNameSuffix;
                            }
                            try {
                                layer.getUnit(i, j, 0).setMappedSOM(
                                        new GrowingSOM(++id, layer.getUnit(i, j, 0), new SOMLibFormatInputReader(
                                                subWeightFileName, subUnitFileName, subMapFileName)));
                            } catch (Exception e) {
                                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
                                System.exit(-1);
                            }
                        }
                    }
                    currentUnitNum++;
                }
            }
            // TODO FIXME : pass the quality measure as parameter!
            String qualityMeasureName = "at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError.mqe";
            layer.setQualityMeasure(qualityMeasureName);
            layer.setCommonVectorLabelPrefix(ir.getCommonVectorLabelPrefix());
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            System.exit(-1);
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished layer restoration.");
        // layer.calculateQuantizationErrorAfterTraining(); is done by the unit.
    }

    /**
     * Constructs an already trained <code>GrowingSOM</code> with a <code>SOMInputReader</code> provided by argument
     * <code>ir</code>.
     * 
     * @param ir an object implementing the <code>SOMinputReader</code> interface to load an already trained model.
     */
    public GrowingSOM(SOMInputReader ir) {
        this(1, null, ir);
    }

    /** only used for subclassing */
    protected GrowingSOM() {
    }

    /**
     * Returns the actual map layer.
     * 
     * @return the actual map layer
     */
    public GrowingLayer getLayer() {
        return layer;
    }

    /**
     * Trains the map with the input data and training parameters specified in the properties provided by argument
     * <code>props</code>. If the value of property <code>tau</code> is 1, a fix-sized layer is trained, otherwise the
     * layer grows until a certain quality criterion determined by <code>tau</code> and the mean quantization error
     * specified by argument <code>mqe0</code> of the data (which is automatically calculated) is reached. This method
     * is usually used for GHSOM training.
     * 
     * @param data input data to train the map with.
     * @param props the training properties.
     * @param targetQualityValue the desired granularity of data representation. Used for maps in GHSOMs.
     */
    public QualityMeasure train(InputData data, GHSOMProperties props, double targetQualityValue,
            String qualityMeasureName) {
        // call training function depending on the properties (iterations || cycles)
        int iterationsToTrain = 0;
        if (props.numIterations() > 0) {
            iterationsToTrain = props.numIterations();
        } else {
            iterationsToTrain = props.numCycles() * data.numVectors();
        }

        return layer.train(data, props.learnrate(), props.sigma(), iterationsToTrain, 0, props.tau(),
                targetQualityValue, qualityMeasureName, props);
    }

    /**
     * Trains the map with the input data and training parameters specified in the properties provided by argument
     * <code>props</code>. If the value of property <code>tau</code> is 1, a fix-sized layer is trained, otherwise the
     * layer grows until a certain quality criterion determined by <code>tau</code> and the mean quantization error of
     * the data (which is automatically calculated) is reached.
     * 
     * @param data input data to train the map with.
     * @param props the training properties
     */
    public void train(InputData data, SOMProperties props) {
        // call training function depending on the properties (iterations || cycles)
        int iterationsToTrain = 0;
        if (props.numIterations() > 0) {
            iterationsToTrain = props.numIterations();
        } else {
            iterationsToTrain = props.numCycles() * data.numVectors();
        }

        layer.train(data, props.learnrate(), props.sigma(), iterationsToTrain, 0, props.tau(),
                props.growthQualityMeasureName(), props);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GrowingSOM) {
            // compare the layers for equality
            return getLayer().equalWeights(((GrowingSOM) o).getLayer());
        }
        // false in all other cases...
        return false;
    }

    private class IntermediateSOMDumper implements TrainingInterruptionListener {

        private final FileProperties fileProperties;

        public IntermediateSOMDumper(FileProperties fileProperties) {
            this.fileProperties = fileProperties;
        }

        @Override
        public void interruptionOccurred(int currentIteration, int numIterations) {
            // FIXME: maybe skip writing the SOM at 0 iterations (0 mod x == 0 ...)
            String filename = fileProperties.namePrefix(false) + "_" + currentIteration;
            try {
                SOMLibMapOutputter.writeWeightVectorFile(GrowingSOM.this, fileProperties.outputDirectory(), filename,
                        true, "$CURRENT_ITERATION=" + currentIteration, "$NUM_ITERATIONS=" + numIterations);
            } catch (IOException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not open or write to output file " + filename + ": " + e.getMessage());
            }

        }

    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        GrowingSOM newGSOM = new GrowingSOM();
        newGSOM.labelled = this.labelled;
        newGSOM.layer = (GrowingLayer) this.layer.clone();
        newGSOM.sharedInputObjects = this.sharedInputObjects;
        newGSOM.trainingStart = this.trainingStart;

        return newGSOM;
    }

}
