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
package at.tuwien.ifs.somtoolbox.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.layers.Layer.GridLayout;
import at.tuwien.ifs.somtoolbox.layers.Layer.GridTopology;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;

/**
 * Properties for SOM training.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: SOMProperties.java 3899 2010-11-03 16:47:00Z mayer $
 */
public class SOMProperties extends Properties {
    public static final String WORKING_DIRECTORY = "workingDirectory";

    private static final String DEFAULT_METRIC_NAME = L2Metric.class.getCanonicalName();

    private static final String METRIC_PACKAGE = "at.tuwien.ifs.somtoolbox.layers.metrics.";

    public static final double defaultLearnRate = 0.7;

    public static final String propertiesFileNameSuffix = ".prop";

    public class DatumToUnitMapping {
        public String label;

        public int unitX, unitY;
    }

    private static final long serialVersionUID = 1L;

    // private int expansionCheckCycles = 0;

    // private int expansionCheckIterations = 0;

    private boolean batchSom = false;

    private int neighbourWidth = 3;

    private double learnrate = 0;

    private String metricName = null;

    private String growthQualityMeasureName = null;

    private int numCycles = 0;

    private int numIterations = 0;

    private int dumpEvery = -1;

    /**
     * Default = -1 --> do not dump.
     * 
     * @return iteration % dumpEvery == 0 --> dump
     */
    public int getDumpEvery() {
        return dumpEvery;
    }

    private long randomSeed = -1;

    private double sigma = -1;

    private double tau = 1;

    private int xSize = 0;

    private int ySize = 0;

    private int zSize = 0;

    private GridTopology gridTopology = GridTopology.planar;

    private GridLayout gridLayout = GridLayout.rectangular;

    private boolean usePCA = false;

    private Vector<DatumToUnitMapping> datumToUnitMappings = new Vector<DatumToUnitMapping>();

    /* Angela: training exceptions */
    private ArrayList<String> selectedClasses = null;

    private String classInfoFileName = null;

    private int selectedClassMode;

    private int minimumFeatureDensity = -1;

    private double[] adaptiveCoordinatesThreshold;

    public static final int MODE_NORMAL = 0;

    public static final int MODE_EXCEPT = 1;

    public static final int MODE_FAVOUR = 2;

    /* Jakob: dumpEvery */
    public SOMProperties(int xSize, int ySize, int zSize, long seed, int trainingCycles, int trainingIterations,
            int dumpEvery, double lernrate, double sigma, double tau, String metric, boolean usePCA)
            throws PropertiesException {
        this(xSize, ySize, zSize, seed, trainingCycles, trainingIterations, lernrate, sigma, tau, metric, usePCA);
        this.dumpEvery = dumpEvery;
    }

    /* Jakob: 3D (zSize) */
    public SOMProperties(int xSize, int ySize, int zSize, long seed, int trainingCycles, int trainingIterations,
            double lernrate, double sigma, double tau, String metric, boolean usePCA) throws PropertiesException {
        this(xSize, ySize, seed, trainingCycles, trainingIterations, lernrate, sigma, tau, metric, usePCA);
        this.zSize = zSize;
    }

    public SOMProperties(int xSize, int ySize, int numIterations, double lernrate) throws PropertiesException {
        this.xSize = xSize;
        this.ySize = ySize;
        this.numIterations = numIterations;
        this.learnrate = lernrate;
    }

    public SOMProperties(int xSize, int ySize, long seed, int numCycles, int numIterations, double learnrate,
            double sigma, double tau, String metricName, boolean usePCA) throws PropertiesException {
        this(xSize, ySize, numIterations, learnrate);
        this.zSize = 1;
        this.tau = tau;
        this.metricName = metricName;
        this.numCycles = numCycles;
        this.growthQualityMeasureName = "QuantizationError.mqe";
        this.sigma = sigma;
        this.randomSeed = -1;
        this.usePCA = usePCA;
        validatePropertyValues();
    }

    /**
     * Loads and encapsulated properties for the SOM training process.
     * 
     * @param fname name of the properties file.
     * @throws PropertiesException thrown if properties file could not be opened or the values of the properties are
     *             illegal.
     */
    public SOMProperties(String fname) throws PropertiesException {
        try {
            load(new FileInputStream(fname));
        } catch (Exception e) {
            throw new PropertiesException("Could not open properties file " + fname);
        }
        parse();
    }

    public SOMProperties(Properties properties) throws PropertiesException {
        putAll(properties);
        parse();
    }

    private void parse() throws PropertiesException {
        try {
            usePCA = StringUtils.equals(getProperty("usePCA"), "true");
            if (usePCA) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Using PCA initialisation.");
            }

            // TODO: this has to be moved to some external file most likely
            if (getProperty("datumToUnitMappings") != null) {
                java.util.StringTokenizer st = new java.util.StringTokenizer(getProperty("datumToUnitMappings"));

                while (st.hasMoreTokens()) {
                    DatumToUnitMapping mapping = new DatumToUnitMapping();

                    mapping.label = st.nextToken();
                    mapping.unitX = Integer.parseInt(st.nextToken());
                    mapping.unitY = Integer.parseInt(st.nextToken());

                    System.out.println("\n   *** Adding datum to unit mapping, datum label: " + mapping.label
                            + "unit x, y: " + mapping.unitX + ", " + mapping.unitY);

                    datumToUnitMappings.add(mapping);
                }
            }

            // ...
            neighbourWidth = Integer.parseInt(getProperty("neighbour_width", "3"));
            batchSom = Boolean.parseBoolean(getProperty("batch_som", "false"));
            if (batchSom) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Training in Batch-SOM mode");
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Training incrementaly");
            }

            this.xSize = Integer.parseInt(getProperty("xSize", "0"));
            this.ySize = Integer.parseInt(getProperty("ySize", "0"));
            this.zSize = Integer.parseInt(getProperty("zSize", "1"));
            this.learnrate = Double.parseDouble(getProperty("learnrate", "0"));
            this.sigma = Double.parseDouble(getProperty("sigma", "-1"));
            this.metricName = getProperty("metricName");
            this.growthQualityMeasureName = getProperty("growthQualityMeasureName");
            this.randomSeed = Long.parseLong(getProperty("randomSeed", "-1"));
            this.numIterations = Integer.parseInt(getProperty("numIterations", "0"));
            this.numCycles = Integer.parseInt(getProperty("numCycles", "0"));
            this.dumpEvery = Integer.parseInt(getProperty("dumpEvery", "-1"));
            this.tau = Double.parseDouble(getProperty("tau", "1"));

            // parameter for adaptive coordinate visualization
            if (getProperty("adaptiveCoordinatesThreshold") != null) {
                adaptiveCoordinatesThreshold = at.tuwien.ifs.somtoolbox.util.StringUtils.parseDoublesAndRanges(getProperty("adaptiveCoordinatesThreshold"));
            }

            /* Angela: for training selection (excepting or favouring) */
            String selectedClassesString = getProperty("selectedClasses", null);
            if (selectedClassesString != null) {
                selectedClasses = new ArrayList<String>();
                String[] selectedClassesTmp = selectedClassesString.split(",");
                for (String element : selectedClassesTmp) {
                    selectedClasses.add(element.trim());
                }
            }
            classInfoFileName = getProperty("classInfoFileName", null);
            if (classInfoFileName != null && getProperty(WORKING_DIRECTORY, null) != null) {
                String tmpStr = getProperty(WORKING_DIRECTORY, null).concat(File.separator).concat(classInfoFileName);
                classInfoFileName = tmpStr;
            }
            selectedClassMode = Integer.parseInt(getProperty("classselectionmode", String.valueOf(MODE_NORMAL)));
            minimumFeatureDensity = Integer.parseInt(getProperty("minimumFeatureDensity", String.valueOf(-1)));

            validatePropertyValues();

            // FIXME: find a generic method to parse enums w/o repeating the code...
            try {
                gridLayout = GridLayout.valueOf(getProperty("gridLayout", GridLayout.rectangular.toString()));
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Illegal value '" + getProperty("gridLayout") + "' for property '" + "gridLayout"
                                + "', valid options are: " + Arrays.toString(GridLayout.values()));
                System.exit(-1);
            }
            try {
                gridTopology = GridTopology.valueOf(getProperty("gridTopology", GridTopology.planar.toString()));
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Illegal value '" + getProperty("gridTopology") + "' for property '" + "gridTopology"
                                + "', valid options are: " + Arrays.toString(GridTopology.values()));
                System.exit(-1);
            }
        } catch (NumberFormatException e) {
            throw new PropertiesException("Illegal numeric value '" + e + "' in properties file.");
        }
    }

    private void validatePropertyValues() throws PropertiesException {
        if (xSize <= 0 || ySize <= 0) {
            throw new PropertiesException("Either xSize or ySize is less than or equal zero.");
        }
        if (zSize > 1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("zSize > 1 --> Training 3D-SOM");
        }
        if (learnrate <= 0) {
            throw new PropertiesException("Learnrate is less than or equal zero or missing.");
        }

        if (sigma <= 0) {
            // FIXME: find a good initial value for sigma (neighbourhood radius)
            // old equation from michael, produces too small radii !!
            // sigma = Math.sqrt((-1 * Math.pow(0.375 * Math.max(xSize, ySize), 2)) / (2 * Math.log(0.3)));

            // new equation, inspired by e.g.
            // http://xmipp.cnb.csic.es/NewXmipp/Web_Site/public_html/NewXmipp/Applications/Src/SOM/Help/som.html and
            // http://www.spatialanalysisonline.com/output/html/SOMunsupervisedclassificationofhyper-spectralimagedata.html
            // initial radius = span whole map! we take a slightly smaller radius here
            // sigma = Math.max(xSize, ySize) * 0.9;

            // newest method, inspired by ESOM
            // http://databionic-esom.sourceforge.net/user.html#Training_Parameters: should be on the order of half the
            // smaller length of the grid.
            // special handling of 1-dimensional maps => we take half of the longer axis
            if (xSize == 1 || ySize == 1) {
                sigma = Math.min(xSize, ySize) / 2d;
            } else {
                sigma = Math.max(xSize, ySize) / 2d;
            }
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Sigma is missing or negative. Defaulting to "
                            + at.tuwien.ifs.somtoolbox.util.StringUtils.format(sigma, 2) + " for a map of size "
                            + xSize + "x" + ySize);
        }
        if (tau == 1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("tau = 1 implies fix-sized layer");
        } else if (tau <= 0 || tau > 1) {
            throw new PropertiesException("Tau less than or equal zero or greater than 1.");
        }
        if (StringUtils.isBlank(metricName)) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("No metricName given. Defaulting to L2Metric.");
            metricName = DEFAULT_METRIC_NAME;
        } else if (!metricName.startsWith(METRIC_PACKAGE)) {
            metricName = METRIC_PACKAGE + metricName;
        }
        if (StringUtils.isBlank(growthQualityMeasureName)) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "No growthQualityMeasureName given. Defaulting to QuantizationError.mqe.");
            growthQualityMeasureName = "at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError.mqe";
        } else {
            growthQualityMeasureName = "at.tuwien.ifs.somtoolbox.layers.quality." + growthQualityMeasureName;
        }
        if (randomSeed == -1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("No randomSeed given. Defaulting to 7.");
            randomSeed = 7;
        }

        if (numIterations <= 0 && numCycles <= 0) {
            throw new PropertiesException("numIterations and numCycles are less than or equal zero or missing.\n"
                    + "Provide either numIterations or numCycles (multiple of # training data).");
        } else if (numIterations > 0 && numCycles > 0) {
            throw new PropertiesException("numIterations and numCycles are mutually exclusive.\n"
                    + "Specify just one of them.");
        } else if (numIterations > 0 && numCycles <= 0) {
            numCycles = 0; // just to be sure
            if (tau == 1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "numIterations defines the fixed number of training iterations.");
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "numIterations defines the number of iterations after which an expansion check is performed.");
            }
        } else if (numIterations <= 0 && numCycles > 0) {
            numIterations = 0; // just to be sure
            if (tau == 1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "numCycles defines the fixed number of training cycles (multiples of the number of data).");
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "numCycles defines the number of cycles (multiples of the number of data) after which an expansion check is performed.");
            }
        }
    }

    /**
     * Returns an ArrayList of Strings containing the class names which should be excluded from training.
     * 
     * @return classes to be excluded from the training
     */
    public ArrayList<String> getSelectedClasses() {
        return this.selectedClasses;
    }

    public int getSelectedClassMode() {
        return selectedClassMode;
    }

    public String getClassInfoFileName() {
        return this.classInfoFileName;
    }

    /**
     * Returns the batch_som status.
     * 
     * @return the batch_som status.
     */
    public boolean batchSom() {
        return batchSom;
    }

    /**
     * Returns the neighbour_width.
     * 
     * @return the neighbour_width.
     */
    public int neighbourWidth() {
        return neighbourWidth;
    }

    /**
     * Returns the learnrate.
     * 
     * @return the learnrate.
     */
    public double learnrate() {
        return learnrate;
    }

    /**
     * Returns the name of the used metric.
     * 
     * @return the name of the used metric.
     */
    public String metricName() {
        return metricName;
    }

    /**
     * Returns the name of the used quality measure.
     * 
     * @return the name of the used quality measure.
     */
    public String growthQualityMeasureName() {
        return growthQualityMeasureName;
    }

    /**
     * Returns the number of training cycles.
     * 
     * @return the number of training cycles.
     */
    public int numCycles() {
        return numCycles;
    }

    /**
     * Returns the number of training iterations.
     * 
     * @return the number of training iterations.
     */
    public int numIterations() {
        return numIterations;
    }

    /**
     * Return the number of iterations really trained, either using {@link #numIterations} or {@link #numCycles},
     * whichever value is set.
     */
    public int trainedIterations(int numVectors) {
        if (numIterations() > 0) {
            return numIterations();
        } else {
            return numCycles() * numVectors;
        }
    }

    /**
     * Returns the random seed.
     * 
     * @return the random seed.
     */
    public long randomSeed() {
        return randomSeed;
    }

    /**
     * Returns sigma determining the neighbourhood radius.
     * 
     * @return sigma determining the neighbourhood radius.
     */
    public double sigma() {
        return sigma;
    }

    /**
     * Returns tau determining the desired data representation granularity.
     * 
     * @return tau determining the desired data representation granularity.
     */
    public double tau() {
        return tau;
    }

    /**
     * Returns the number of units in horizontal direction.
     * 
     * @return the number of units in horizontal direction.
     */
    public int xSize() {
        return xSize;
    }

    /**
     * Returns the number of units in vertical direction.
     * 
     * @return the number of units in vertical direction.
     */
    public int ySize() {
        return ySize;
    }

    /**
     * Returns the number of units in z-direction. Default is 1
     * 
     * @return the number of units in z-direction. Default is 1
     */
    public int zSize() {
        return zSize;
    }

    public int getMinimumFeatureDensity() {
        return minimumFeatureDensity;
    }

    public boolean pca() {
        return usePCA;
    }

    public GridTopology getGridTopology() {
        return gridTopology;
    }

    public GridLayout getGridLayout() {
        return gridLayout;
    }

    public Vector<DatumToUnitMapping> datumToUnitMappings() {
        return datumToUnitMappings;
    }

    /** Writes the properties to a file. */
    public void writeToFile(String dataName, String outputDir, boolean normalised) throws IOException {
        String[] split = dataName.split(File.separator);
        String name = split[split.length - 1];
        String fileName = dataName + propertiesFileNameSuffix;

        // we are not using the Properties.store() method, as we want to guarantee a certain order in the file
        PrintWriter writer = at.tuwien.ifs.somtoolbox.util.FileUtils.openFileForWriting("SOM Properties File",
                fileName, false);

        writer.println("outputDirectory=" + (outputDir != null ? outputDir : name));
        writer.println(WORKING_DIRECTORY + "=.");
        writer.println("namePrefix=" + name);
        writer.println();
        writer.println("vectorFileName=" + name + InputData.inputFileNameSuffix);
        writer.println("templateFileName=" + name + TemplateVector.templateFileNameSuffix);
        writer.println();
        writer.println("isNormalized=" + normalised);
        writer.println("randomSeed=" + randomSeed());
        writer.println();
        if (StringUtils.isNotBlank(metricName) && !metricName.equals(DEFAULT_METRIC_NAME)) {
            writer.println("metricName=" + metricName);
            writer.println();
        }
        writer.println("xSize=" + xSize);
        writer.println("ySize=" + ySize);
        if (zSize > 1) {
            writer.println("zSize=" + zSize);
        }
        writer.println();
        writer.println("learnrate=" + learnrate);
        if (numCycles > 0) {
            writer.println("numCycles=" + numCycles);
        } else {
            writer.println("numIterations=" + numIterations);
        }
        writer.flush();
        writer.close();
    }

    public double[] adaptiveCoordinatesTreshold() {
        return adaptiveCoordinatesThreshold;
    }
}
