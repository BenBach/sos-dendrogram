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
package at.tuwien.ifs.somtoolbox.layers;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import cern.colt.matrix.DoubleFactory3D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix3D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.jet.math.Functions;

import at.tuwien.ifs.commons.util.MathUtils;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.input.InputCorrections;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.InputCorrection;
import at.tuwien.ifs.somtoolbox.layers.Unit.FeatureWeightMode;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractWeightedMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2MetricSparse;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.layers.quality.AbstractQualityMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;
import at.tuwien.ifs.somtoolbox.structures.ComponentLine2D;
import at.tuwien.ifs.somtoolbox.util.Cuboid;
import at.tuwien.ifs.somtoolbox.util.PCA;
import at.tuwien.ifs.somtoolbox.util.ProgressListener;
import at.tuwien.ifs.somtoolbox.util.ProgressListenerFactory;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;
import at.tuwien.ifs.somtoolbox.util.comparables.ComponentRegionCount;
import at.tuwien.ifs.somtoolbox.util.comparables.InputDistance;
import at.tuwien.ifs.somtoolbox.util.comparables.InputNameDistance;
import at.tuwien.ifs.somtoolbox.util.comparables.UnitDistance;

/**
 * Implementation of a growing Self-Organizing Map layer that can also be static in size. Layer growth is based on the
 * quantization errors of the units and the distance to their respective neighboring units.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: GrowingLayer.java 3978 2010-12-16 14:36:44Z mayer $
 */
//
//
// FIXME: refactor this, and make several sub-classes of GrowingLayer, namely:
// - 3D layer
// - Batch-SOM layer
// - specific training layers (missing values, leaving classes out, ...)
//
public class GrowingLayer implements Layer {
    private static final double CUTOFF_SIGMA = Double.MIN_VALUE * 150; // 0.0000000000000000000000000000000000000000000000000001;

    public static final int[] ROTATIONS = { 90, 180, 270 };

    protected int dim = 0;

    private int identifier = 1;

    private int interruptEvery = 0;

    private int level = 0;

    protected DistanceMetric metric = null;

    protected AbstractWeightedMetric metricWeighted = null;

    private String metricName = null;

    private boolean normalized = false;

    private QualityMeasure qualityMeasure = null;

    private Random rand = null;

    private Random randSkipProbability = null;

    private final String revision = "$Revision: 3978 $";

    private Unit superUnit = null;

    private TrainingInterruptionListener til = null;

    protected Unit[][][] units = null;

    protected int xSize = 0;

    protected int ySize = 0;

    protected int zSize = 0;

    double[] minFeatureValues = null;

    double[] maxFeatureValues = null;

    protected GridLayout gridLayout = GridLayout.rectangular;

    protected GridTopology gridTopology = GridTopology.planar;

    private Hashtable<Integer, Point2D[][]> binAssignmentCache = new Hashtable<Integer, Point2D[][]>();

    private Hashtable<Integer, ArrayList<ComponentRegionCount>> regionAssignmentCache = new Hashtable<Integer, ArrayList<ComponentRegionCount>>();

    private static int THREAD_COUNT = 1;

    // private static final int NO_CPUS = Runtime.getRuntime().availableProcessors();

    private CountDownLatch doneSignal;

    private ExecutorService e;

    private Cuboid[] ranges = null;

    private final int threadsUsed;

    public enum Rotation {
        ROTATE_90 {
            @Override
            public String toString() {
                return "90";
            }
        },
        ROTATE_180 {
            @Override
            public String toString() {
                return "180";
            }
        },
        ROTATE_270 {
            @Override
            public String toString() {
                return "270";
            }
        };

        public static Rotation getByDegree(int degree) {
            return valueOf("ROTATE_" + degree);
        }
    }

    public enum Flip {
        HORIZONTAL, VERTICAL, DEPTH
    }

    /**
     * Stores the number of units that are not empty, i.e. that have at least one input mapped. This field should not be
     * used directly, but only via {@link #getNumberOfNotEmptyUnits()}, as the calculation of the value is only
     * performed by that method and stored for future access.
     */
    private int notEmptyUnits = -1;

    private InputData data;

    int skippedNonSelected = 0;

    int trainedNonSelected = 0;

    private String commonVectorLabelPrefix;

    public DoubleMatrix2D unitDistanceMatrix;

    private AdaptiveCoordinatesVirtualLayer virtualLayer = null;

    /**
     * Convenience constructor for top layer map of GHSOM or a single map. The identifier of the map is set to 1 and the
     * superordinate unit is set to <code>null</code>.
     * 
     * @param xSize the number of columns.
     * @param ySize the number of rows.
     * @param metricName the name of the distance metric to use.
     * @param dim the dimensionality of the weight vectors.
     * @param normalized the type of normalization that is applied to the weight vectors of newly created units. This is
     *            usually <code>Normalization.NONE</code> or <code>Normalization.UNIT_LEN</code>.
     * @param seed the random seed for creation of the units' weight vectors.
     * @see <a href="../data/normalisation/package.html">Normalisation</a>
     */
    public GrowingLayer(int xSize, int ySize, String metricName, int dim, boolean normalized, boolean usePCA,
            long seed, InputData data) {
        this(xSize, ySize, 1, metricName, dim, normalized, usePCA, seed, data);
    }

    /**
     * Convenience constructor for top layer map of GHSOM or a single map. The identifier of the map is set to 1 and the
     * superordinate unit is set to <code>null</code>.
     * 
     * @param xSize the number of columns.
     * @param ySize the number of rows.
     * @param zSize the depth
     * @param metricName the name of the distance metric to use.
     * @param dim the dimensionality of the weight vectors.
     * @param normalized the type of normalization that is applied to the weight vectors of newly created units. This is
     *            usually <code>Normalization.NONE</code> or <code>Normalization.UNIT_LEN</code>.
     * @param seed the random seed for creation of the units' weight vectors.
     * @see <a href="../data/normalisation/package.html">Normalisation</a>
     */
    public GrowingLayer(int xSize, int ySize, int zSize, String metricName, int dim, boolean normalized,
            boolean usePCA, long seed, InputData data) {
        this(1, null, xSize, ySize, zSize, metricName, dim, normalized, usePCA, seed, data);
    }

    /**
     * Constructor for a new, untrained layer.
     * 
     * @param id the unique id of the layer in a hierarchy.
     * @param su the pointer to the corresponding unit in the upper layer map.
     * @param xSize the number of units in horizontal direction.
     * @param ySize the number of units in vertical direction.
     * @param metricName the name of the distance metric to use.
     * @param dim the dimensionality of the weight vectors.
     * @param normalized the type of normalization that is applied to the weight vectors of newly created units. This is
     *            usually <code>Normalization.NONE</code> or <code>Normalization.UNIT_LEN</code>.
     * @param seed the random seed for creation of the units' weight vectors.
     * @see <a href="../data/normalisation/package.html">Normalisation</a>
     */
    public GrowingLayer(int id, Unit su, int xSize, int ySize, String metricName, int dim, boolean normalized,
            boolean usePCA, long seed, InputData data) {
        this(id, su, xSize, ySize, 1, metricName, dim, normalized, usePCA, seed, data);
    }

    /**
     * Constructor for a new, untrained layer.
     * 
     * @param id the unique id of the layer in a hierarchy.
     * @param su the pointer to the corresponding unit in the upper layer map.
     * @param xSize the number of units in horizontal direction.
     * @param ySize the number of units in vertical direction.
     * @param zSize the number of units in depth
     * @param metricName the name of the distance metric to use.
     * @param dim the dimensionality of the weight vectors.
     * @param normalized the type of normalization that is applied to the weight vectors of newly created units. This is
     *            usually <code>Normalization.NONE</code> or <code>Normalization.UNIT_LEN</code>.
     * @param seed the random seed for creation of the units' weight vectors.
     * @see <a href="../data/normalisation/package.html">Normalisation</a>
     */
    public GrowingLayer(int id, Unit su, int xSize, int ySize, int zSize, String metricName, int dim,
            boolean normalized, boolean usePCA, long seed, InputData data) {
        rand = new Random(seed);
        randSkipProbability = new Random();
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.metricName = metricName;
        this.data = data;
        this.normalized = normalized;
        this.dim = dim;
        try {
            metric = AbstractMetric.instantiate(metricName);
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not instantiate metric \"" + metricName + "\" for layer.");
            System.exit(-1);
        }
        this.threadsUsed = THREAD_COUNT;
        this.doneSignal = new CountDownLatch(threadsUsed);

        initCPURanges();

        units = new Unit[xSize][ySize][zSize];
        //
        // perform optional PCA?
        //

        if (usePCA) {
            double[][] dataArray = data.getData();
            double[][] projectedDataArray = new double[data.numVectors()][2];

            // perform PCA
            System.out.println("");
            System.out.println("  *** Calculating PCA...");
            PCA pca = new PCA(dataArray);

            //
            // find the 2 main axis from the PCA
            //
            double firstAxisVar = Double.MIN_VALUE;
            double secondAxisVar = Double.MIN_VALUE;
            int firstAxisIndex = -1;
            int secondAxisIndex = -1;

            for (int curAxis = 0; curAxis < dim; curAxis++) {
                if (pca.info[curAxis] > firstAxisVar) {
                    secondAxisVar = firstAxisVar;
                    secondAxisIndex = firstAxisIndex;

                    firstAxisVar = pca.info[curAxis];
                    firstAxisIndex = curAxis;
                } else if (pca.info[curAxis] > secondAxisVar) {
                    secondAxisVar = pca.info[curAxis];
                    secondAxisIndex = curAxis;
                }
            }

            System.out.println("");
            System.out.println("  *** firstAxisIndex: " + firstAxisIndex + " secondAxisIndex: " + secondAxisIndex);

            //
            // project the data points
            //
            for (int i = 0; i < data.numVectors(); i++) {
                float xProj = 0.f;
                for (int j = 0; j < dim; j++) {
                    xProj += dataArray[i][j] * pca.U[firstAxisIndex][j];
                }

                projectedDataArray[i][0] = xProj;

                float yProj = 0.f;
                for (int j = 0; j < dim; j++) {
                    yProj += dataArray[i][j] * pca.U[secondAxisIndex][j];
                }

                projectedDataArray[i][1] = yProj;
            }

            // find minX,minY,maxX,maxY
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;
            for (int i = 0; i < data.numVectors(); i++) {
                if (projectedDataArray[i][0] < minX) {
                    minX = projectedDataArray[i][0];
                }
                if (projectedDataArray[i][1] < minY) {
                    minY = projectedDataArray[i][1];
                }

                if (projectedDataArray[i][0] > maxX) {
                    maxX = projectedDataArray[i][0];
                }
                if (projectedDataArray[i][1] > maxY) {
                    maxY = projectedDataArray[i][1];
                }
            }

            double diffX = maxX - minX;
            double diffY = maxY - minY;
            double cellSizeX = diffX / xSize;
            double cellSizeY = diffY / ySize;

            System.out.println("");
            System.out.println("  *** diffX: " + diffX + " diffY: " + diffY);

            // ...
            for (int k = 0; k < zSize; k++) {
                for (int j = 0; j < ySize; j++) {
                    for (int i = 0; i < xSize; i++) {
                        // find the closes point in the data point cloud
                        int closestPointIndex = -1;
                        double closesPointDist = Double.MAX_VALUE;

                        for (int curPoint = 0; curPoint < data.numVectors(); curPoint++) {
                            double[] curCellCoords = new double[2];
                            curCellCoords[0] = i * cellSizeX + cellSizeX / 2;
                            curCellCoords[1] = j * cellSizeY + cellSizeY / 2;

                            double curPointDist = Math.sqrt(Math.pow(
                                    projectedDataArray[curPoint][0] - curCellCoords[0], 2)
                                    + Math.pow(projectedDataArray[curPoint][1] - curCellCoords[1], 2));

                            if (curPointDist < closesPointDist) {
                                closesPointDist = curPointDist;
                                closestPointIndex = curPoint;
                            }
                        }

                        double[] closesPointVec = new double[dim];
                        for (int l = 0; l < dim; l++) {
                            closesPointVec[l] = dataArray[closestPointIndex][l];
                        }

                        units[i][j][k] = new Unit(this, i, j, closesPointVec);
                    }
                }
            }
        } else {
            for (int k = 0; k < zSize; k++) {
                for (int j = 0; j < ySize; j++) {
                    for (int i = 0; i < xSize; i++) {
                        units[i][j][k] = new Unit(this, i, j, k, dim, rand, normalized);
                    }
                }
            }
        }
        superUnit = su;
        if (su != null) {
            level = su.getMapLevel() + 1;
        } else {
            level = 0;
        }
        identifier = id;
    }

    /**
     * Constructor for an already trained layer as specified by 2-dimensional array of d-dimensional weight vectors as
     * argument <code>vectors</code>.
     * 
     * @param xSize the number of columns.
     * @param ySize the number of rows.
     * @param metricName the name of the distance metric to use.
     * @param dim the dimensionality of the weight vectors.
     * @param vectors the three dimensional array of <code>d</code> dimensional weight vectors.
     * @param seed the random seed for creation of the units' weight vectors.
     * @throws SOMToolboxException if arguments <code>x</code>, <code>y</code> and <code>d</code> do not correspond to
     *             the dimensions of argument <code>vectors</code>.
     */
    public GrowingLayer(int id, Unit su, int xSize, int ySize, String metricName, int dim, double[][][] vectors,
            long seed) throws SOMToolboxException {
        this(id, su, xSize, ySize, 1, metricName, dim, addDimension(xSize, ySize, vectors), seed);
    }

    protected static double[][][][] addDimension(int x, int y, double[][][] vec) {
        double[][][][] vector = new double[x][y][1][];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                vector[i][j][0] = vec[i][j];
            }
        }
        return vector;
    }

    /**
     * Constructor for an already trained layer as specified by 2-dimensional array of d-dimensional weight vectors as
     * argument <code>vectors</code>.
     * 
     * @param xSize the number of columns.
     * @param ySize the number of rows.
     * @param zSize the depth
     * @param metricName the name of the distance metric to use.
     * @param dim the dimensionality of the weight vectors.
     * @param vectors the two dimensional array of <code>d</code> dimensional weight vectors.
     * @param seed the random seed for creation of the units' weight vectors.
     * @throws SOMToolboxException if arguments <code>x</code>, <code>y</code> and <code>d</code> do not correspond to
     *             the dimensions of argument <code>vectors</code>.
     */
    public GrowingLayer(int id, Unit su, int xSize, int ySize, int zSize, String metricName, int dim,
            double[][][][] vectors, long seed) throws SOMToolboxException {
        // adapted to mnemonic (sparse) SOMs
        if (vectors.length != xSize || vectors[0].length != ySize || vectors[0][0].length != zSize
                || getDimension(xSize, ySize, zSize, vectors) > 0 && getDimension(xSize, ySize, zSize, vectors) != dim) {
            throw new SOMToolboxException(
                    "Dimensions provided by arguments mismatch dimensions of weight vector array: size "
                            + vectors.length + "x" + vectors[0].length + "x" + vectors[0][0].length + " vs. " + xSize
                            + "x" + ySize + "x" + zSize + ", dimension: " + getDimension(xSize, ySize, zSize, vectors)
                            + " vs " + dim + ".");
        }
        rand = new Random(seed);
        randSkipProbability = new Random();
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.metricName = metricName;
        this.dim = dim;
        this.threadsUsed = THREAD_COUNT;
        this.doneSignal = new CountDownLatch(threadsUsed);

        initCPURanges();

        units = new Unit[xSize][ySize][zSize];
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    // changed to adapt to mnemonic (sparse) SOM
                    if (vectors[i][j][k] != null) {
                        units[i][j][k] = new Unit(this, i, j, k, vectors[i][j][k]);
                    }
                }
            }
        }
        try {
            metric = AbstractMetric.instantiate(metricName);
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not instantiate metric \"" + metricName + "\" for layer.");
            System.exit(-1);
        }
        superUnit = su;
        if (su != null) {
            level = su.getMapLevel() + 1;
        } else {
            level = 1;
        }
        identifier = id;

        // compute quantisation error
        calculateQuantizationErrorForUnits();
    }

    /**
     * Checks whether this and the given {@link GrowingLayer} are equal in their weight (model) vectors. Other
     * information, such as mapping of vectors, labels, is not considered. This method can e.g. be utilised to check if
     * two layers have been trained equally.
     */
    public boolean equalWeights(GrowingLayer otherLayer) {
        if (otherLayer.getUnitCount() != getUnitCount()) {
            return false;
        }
        for (int i = 0; i < getUnitCount(); i++) {
            try {
                if (getUnitForIndex(i).getWeightVector() != otherLayer.getUnitForIndex(i).getWeightVector()) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                            "Layers differ in " + getUnitForIndex(i) + "\n\t"
                                    + Arrays.toString(getUnitForIndex(i).getWeightVector()) + "\n\t"
                                    + Arrays.toString(otherLayer.getUnitForIndex(i).getWeightVector()));
                    return false;
                }
            } catch (LayerAccessException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true; // all units have the same weight vector
    }

    /**
     * Needed for mnemonic (sparse) SOMs to find out the vector dimension, by finding the first non-null vector in the
     * array
     * 
     * @return the vector dimension, or 0 if all vectors are null
     */
    private int getDimension(int x, int y, int z, double[][][][] vectors) {
        for (int dep = 0; dep < z; dep++) {
            for (int col = 0; col < x; col++) {
                for (int row = 0; row < y; row++) {
                    if (vectors[col][row][dep] != null && vectors[col][row][dep].length > 0) {
                        return vectors[col][row][dep].length;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Calculates the quantization error for all units of the layer.
     */
    public void calculateQuantizationErrorForUnits() {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Starting quantization error calculation.");
        int currentUnitNum = 0;
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(xSize * ySize * zSize,
                "Calculating qe for unit ");
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (zSize > 1) {
                        progressWriter.progress("Calculating qe for unit " + i + "/" + j + "/" + k + ", ",
                                (currentUnitNum + 1));
                    } else {
                        progressWriter.progress("Calculating qe for unit " + i + "/" + j + ", ", (currentUnitNum + 1));
                    }
                    units[i][j][k].calculateQuantizationError();
                    currentUnitNum++;
                }
            }
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished quantization error calculation.");
    }

    /**
     * Removes all labels from the units.
     */
    public void clearLabels() {
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    units[i][j][k].clearLabels();
                    if (units[i][j][k].getMappedSOM() != null) {
                        units[i][j][k].getMappedSOM().getLayer().clearLabels();
                    }
                }
            }
        }
    }

    /**
     * Removes all mapped input data from the units.
     */
    public void clearMappedInput() {
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    units[i][j][k].clearMappedInput();
                    if (units[i][j][k].getMappedSOM() != null) {
                        units[i][j][k].getMappedSOM().getLayer().clearMappedInput();
                    }
                }
            }
        }
    }

    /**
     * Returns all units of the layer in an array.
     * 
     * @return all units of the layer in an array.
     */
    @Override
    public Unit[] getAllUnits() {
        Unit[] res = new Unit[xSize * ySize];
        int cnt = 0;
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    res[cnt] = units[i][j][k];
                    cnt++;
                }
            }
        }
        return res;
    }

    public ArrayList<GrowingSOM> getAllSubMaps() {
        ArrayList<GrowingSOM> maps = new ArrayList<GrowingSOM>();
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (units[i][j][k] != null && units[i][j][k].getMappedSOM() != null) {
                        maps.add(units[i][j][k].getMappedSOM());
                    }
                }
            }
        }
        return maps;
    }

    /**
     * Returns all units with depth 0 of the layer
     * 
     * @return returns all units with depth 0 of the layer
     */
    public Unit[][] get2DUnits() {
        if (zSize > 1) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Accessing 2D Units but depth > 1!");
        }
        Unit[][] res = new Unit[xSize][ySize];
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                res[i][j] = units[i][j][0];
            }
        }
        return res;
    }

    /**
     * Returns all units of the layer in a 3D array.
     * 
     * @return all units of the layer in a 3D array.
     */
    public Unit[][][] getUnits() {
        return units;
    }

    public boolean hasMappedInput(int x, int y) {
        try {
            Unit u = getUnit(x, y);
            return u != null && u.getNumberOfMappedInputs() > 0;
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Cannot acces unit at coordinates " + x + "/" + y);
            return false;
        }
    }

    /**
     * Returns all mapped inputs from all the units of the layer in an array.
     * 
     * @param sort indicates whether the labels should be sorted (alphabetically)
     * @return all units of the layer in an array.
     */
    public String[] getAllMappedDataNames(boolean sort) {
        ArrayList<String> dataNames = getAllMappedDataNamesAsList();
        String[] result = dataNames.toArray(new String[dataNames.size()]);
        if (sort) {
            Arrays.sort(result);
        }
        return result;
    }

    public ArrayList<String> getAllMappedDataNamesAsList() {
        Unit[] units = getAllUnits();
        ArrayList<String> dataNames = new ArrayList<String>();
        for (Unit unit : units) {
            if (unit != null && unit.getMappedInputNames() != null) {
                dataNames.addAll(Arrays.asList(unit.getMappedInputNames()));
            }
        }
        return dataNames;
    }

    public String[] getAllMappedDataNames() {
        return getAllMappedDataNames(false);
    }

    public double[][][] getComponentPlane3D(int component) {
        if (component >= 0 && component < dim) {
            double[][][] res = new double[xSize][ySize][zSize];
            for (int k = 0; k < zSize; k++) {
                for (int j = 0; j < ySize; j++) {
                    for (int i = 0; i < xSize; i++) {
                        res[i][j][k] = units[i][j][k].getWeightVector()[component];
                    }
                }
            }
            return res;
        } else {
            return null;
        }

    }

    @Override
    public double[][] getComponentPlane(int component) {
        return getComponentPlane(component, 0);
    }

    @Override
    public double[][] getComponentPlane(int component, int z) {
        if (component >= 0 && component < dim) {
            double[][] res = new double[xSize][ySize];
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    res[i][j] = units[i][j][z].getWeightVector()[component];
                }
            }
            return res;
        } else {
            return null;
        }
    }

    public double[][] getExtremes() throws LayerAccessException {
        if (minFeatureValues == null || maxFeatureValues == null) {
            minFeatureValues = new double[dim];
            maxFeatureValues = new double[dim];
            Arrays.fill(minFeatureValues, Double.MAX_VALUE);
            Arrays.fill(maxFeatureValues, -Double.MAX_VALUE);
            double[] wvec;

            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    wvec = getUnit(x, y).getWeightVector();
                    for (int i = 0; i < dim; i++) {
                        if (wvec[i] < minFeatureValues[i]) {
                            minFeatureValues[i] = wvec[i];
                        }
                        if (wvec[i] > maxFeatureValues[i]) {
                            maxFeatureValues[i] = wvec[i];
                        }
                    }
                }
            }
        }
        return new double[][] { minFeatureValues, maxFeatureValues };
    }

    public int[][] getBinAssignment(int component, int bins) {
        if (component >= 0 && component < dim) {
            double minValue = Double.MAX_VALUE;
            double maxValue = Double.MIN_VALUE;
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    for (int k = 0; k < zSize; k++) {
                        if (units[i][j][k] != null) { // check needed for mnemonic SOM
                            double d = units[i][j][k].getWeightVector()[component];
                            if (d < minValue) {
                                minValue = d;
                            }
                            if (d > maxValue) {
                                maxValue = d;
                            }
                        }
                    }
                }
            }
            double distance = maxValue - minValue;
            double binStep = distance / bins;
            double[] binBorders = new double[bins];
            for (int i = 0; i < binBorders.length - 1; i++) {
                binBorders[i] = minValue + (i + 1) * binStep;
            }
            // set max value specifically to avoid it being potentially lower when computed as in the loop above
            binBorders[binBorders.length - 1] = maxValue;
            // double percentage = 1 / (double) bins / 5;
            // double MIN_VALUE = minValue + (binStep * percentage / 100d);

            int[][] binAssignment = new int[xSize][ySize];
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    for (int k = 0; k < zSize; k++) {
                        if (units[i][j][k] != null) { // check needed for mnemonic SOM
                            double d = units[i][j][k].getWeightVector()[component];
                            // if (MIN_VALUE != 0 && d > MIN_VALUE) { // skip 0 components for text data
                            for (int b = 0; b < bins; b++) {
                                if (d <= binBorders[b]) {
                                    binAssignment[i][j] = b;
                                    break;
                                }
                            }
                            // } else {
                            // // System.out.println("setting -1 value for 0 comp: " + units[i][j][k].printCoordinates()
                            // + ", d: " + d);
                            // binAssignement[i][j] = -1;
                            // }
                        } else {
                            binAssignment[i][j] = -1;
                        }
                    }
                }
            }
            return binAssignment;
        } else {
            return null;
        }
    }

    public Point2D[] getBinCentres(int[][] binAssignment, int bins) {
        Point2D.Double[] res = new Point2D.Double[bins];
        ArrayList<java.awt.geom.Point2D.Double> p = new ArrayList<java.awt.geom.Point2D.Double>();
        int[] xs = new int[bins];
        int[] ys = new int[bins];
        int[] counts = new int[bins];
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                if (binAssignment[x][y] != -1) { // do not consider not assigned units
                    xs[binAssignment[x][y]] += x;
                    ys[binAssignment[x][y]] += y;
                    counts[binAssignment[x][y]] += 1;
                }
            }
        }
        for (int i = 0; i < ys.length; i++) {
            if (counts[i] != 0) {
                res[i] = new Point2D.Double((double) xs[i] / (double) counts[i], (double) ys[i] / (double) counts[i]);
                p.add(new Point2D.Double((double) xs[i] / (double) counts[i], (double) ys[i] / (double) counts[i]));
            } else { // if the bin is empty
                if (i > 0) { // we use the same point as for the previous bin
                    res[i] = res[i - 1];
                }
            }
            if (i == 1 && res[0] == null) { // we need to check the special case of the first bin being empty
                res[0] = res[i];
            }
        }
        // debug info
        // System.out.println("centre sizes: " + Arrays.toString(counts));
        return res;
    }

    public Point2D[][] getBinCentres(int bins) {
        Point2D[][] cachedResult = binAssignmentCache.get(new Integer(bins));
        if (cachedResult != null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Getting bin assignment for " + bins + " bins from cache.");
            return cachedResult;
        } else {
            StdErrProgressWriter progress = new StdErrProgressWriter(dim, "Calculating bin assignment for component ",
                    Math.min(dim / 10, 10));
            Point2D[][] result = new Point2D[dim][bins];
            for (int i = 0; i < dim; i++) {
                int[][] binAssignement = getBinAssignment(i, bins);
                result[i] = getBinCentres(binAssignement, bins);
                progress.progress();
            }
            binAssignmentCache.put(new Integer(bins), result);
            return result;
        }
    }

    public ArrayList<ComponentLine2D> getBinCentresAsList(int bins) {
        ArrayList<ComponentLine2D> res = new ArrayList<ComponentLine2D>();
        Point2D[][] binCentres = getBinCentres(bins);
        for (int i = 0; i < binCentres.length; i++) {
            res.add(new ComponentLine2D(binCentres[i], i));
        }
        return res;
    }

    public double getBinDeviation(int[][] binAssignment, int bins) {
        Point2D.Double[] res = new Point2D.Double[bins];
        ArrayList<java.awt.geom.Point2D.Double> p = new ArrayList<java.awt.geom.Point2D.Double>();
        int[] xs = new int[bins];
        int[] ys = new int[bins];
        int[] counts = new int[bins];
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                if (binAssignment[x][y] != -1) { // do not consider not assigned units

                    xs[binAssignment[x][y]] += x;
                    ys[binAssignment[x][y]] += y;
                    counts[binAssignment[x][y]] += 1;
                }
            }
        }
        double sumDeviation = 0;
        for (int i = 0; i < ys.length; i++) {
            if (counts[i] != 0) {
                res[i] = new Point2D.Double((double) xs[i] / (double) counts[i], (double) ys[i] / (double) counts[i]);
                p.add(new Point2D.Double((double) xs[i] / (double) counts[i], (double) ys[i] / (double) counts[i]));
            } else { // if the bin is empty
                if (i > 0) { // we use the same point as for the previous bin
                    res[i] = res[i - 1];
                }
            }
            if (i == 1 && res[0] == null) { // we need to check the special case of the first bin being empty
                res[0] = res[i];
            }

            double sumXdif = 0;
            double sumYdif = 0;

            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    if (binAssignment[x][y] != -1) { // do not consider not assigned units

                        sumXdif = Math.abs(x - res[i].x);
                        sumYdif = Math.abs(y - res[i].y);

                    }
                }
            }
            if (counts[i] != 0) {
                sumDeviation += sumXdif / counts[i] + sumYdif / counts[i];
            }
        }

        // debug info
        // System.out.println("centre sizes: " + Arrays.toString(counts));
        return sumDeviation / bins;
    }

    public double[] getDeviation(int bins) {

        double[] result = new double[dim];
        for (int i = 0; i < dim; i++) {
            int[][] binAssignement = getBinAssignment(i, bins);
            result[i] = getBinDeviation(binAssignement, bins);

        }

        return result;

    }

    // AREA STUFF

    private int[][] getRegionAssignement(int[][] binAssignement, int numberOfBins) throws LayerAccessException {
        int numberOfRegions = 0;
        int[][] regionAssignment = new int[binAssignement.length][binAssignement[0].length];
        for (int x = 0; x < regionAssignment.length; x++) {
            for (int y = 0; y < regionAssignment[x].length; y++) {
                if (regionAssignment[x][y] == 0) {// if not yet assigned
                    numberOfRegions = numberOfRegions + 1; // increase counter # of regions
                    regionAssignment[x][y] = numberOfRegions;// % assign this unit to the new region
                    processRegionRecursive(binAssignement, regionAssignment, x, y);
                }
            }

        }
        return regionAssignment;
    }

    private void processRegionRecursive(int[][] binAssignment, int[][] regionAssignment, int x, int y)
            throws LayerAccessException {
        int thisBin = binAssignment[x][y];
        for (Unit neighbour : getNeighbouringUnits(x, y, 0)) { // check all neighbours
            int neighX = neighbour.getXPos();
            int neighY = neighbour.getYPos();
            if (regionAssignment[neighX][neighY] == 0) {// if they are not yet assigned
                if (binAssignment[neighX][neighY] == thisBin) {
                    regionAssignment[neighX][neighY] = regionAssignment[x][y]; // then put it in the same region
                    processRegionRecursive(binAssignment, regionAssignment, neighX, neighY);
                }
            }
        }
    }

    private int getNumberOfRegions(int[][] regionAssignement) {
        int numberOfRegions = -1;
        for (int[] element : regionAssignement) {
            for (int element2 : element) {
                if (element2 > numberOfRegions) {
                    numberOfRegions = element2;
                }
            }
        }
        return numberOfRegions;
    }

    public ArrayList<ComponentRegionCount> getNumberOfRegions(int bins) throws LayerAccessException {
        ArrayList<ComponentRegionCount> cachedResult = regionAssignmentCache.get(new Integer(bins));
        if (cachedResult != null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Getting region assignment for " + bins + " bins from cache.");
            return cachedResult;
        } else {
            StdErrProgressWriter progress = new StdErrProgressWriter(dim,
                    "Calculating number of regions for component ", Math.min(dim / 10, 10));
            ArrayList<ComponentRegionCount> result = new ArrayList<ComponentRegionCount>();
            for (int i = 0; i < dim; i++) {
                int[][] binAssignement = getBinAssignment(i, bins);
                result.add(new ComponentRegionCount(getNumberOfRegions(getRegionAssignement(binAssignement, bins)),
                        new Integer(i)));
                progress.progress();
            }
            regionAssignmentCache.put(new Integer(bins), result);
            return result;
        }
    }

    /**
     * Returns the dimensionality of the weight vectors.
     * 
     * @return the dimensionality of the weight vectors.
     */
    public int getDim() {
        return dim;
    }

    /**
     * Returns the unit having the highest quantization error.
     * 
     * @return the unit having the highest quantization error.
     */
    private Unit getErrorUnit(QualityMeasure qm, String methodName) {
        Unit errorUnit = null;
        double maxQe = 0;
        try {
            for (int k = 0; k < zSize; k++) {
                for (int j = 0; j < ySize; j++) {
                    for (int i = 0; i < xSize; i++) {
                        double qe = qm.getUnitQualities(methodName)[i][j];
                        // double qe = units[i][j].getQuantizationError();
                        if (qe > maxQe) {
                            maxQe = qe;
                            errorUnit = units[i][j][k];
                        }
                    }
                }
            }
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }
        return errorUnit;
    }

    /**
     * Returns a <code>Vector</code> of units that are subject to be expanded or already have a subordinate map
     * assigned. The criterion for expansion is determined by tau2 and mqe0.
     * 
     * @param tau2 The fraction of mqe0 that determines wheter a unit is expanded or not.
     * @param mqe0 The mqe0 of the top layer map.
     * @return <code>Vector</code> of units. Returns empty vector if no units are expanded.
     */
    // public Vector getExpandedUnits(double tau2, double mqe0) {
    // Unit[] res = null;
    // Vector expUnits = new Vector();
    // for (int i=0; i<getAllUnits().length; i++) {
    // //if (getAllUnits()[i].getNumberOfMappedInputs()>0) {
    // // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Checking unit for expansion: qe="+(
    // StringUtils.format2FractionDigits.format(getAllUnits()[i].getQuantizationError())+
    // //
    // " > tau2*mqe0:"+StringUtils.format2FractionDigits.format(tau2)+"*"+StringUtils.format2FractionDigits.format(mqe0)+"="+(
    // StringUtils.format2FractionDigits.format((tau2*mqe0)));
    // //}
    // if ((getAllUnits()[i].getQuantizationError() > (tau2*mqe0)) && (getAllUnits()[i].getNumberOfMappedInputs()>0)) {
    // expUnits.addElement(getAllUnits()[i]);
    // }
    // }
    // /*if (expUnits.size()>0) {
    // res = new Unit[expUnits.size()];
    // for (int i=0; i<expUnits.size(); i++) {
    // res[i] = (Unit)expUnits.elementAt(i);
    // }
    // }*/
    // return expUnits;
    // }
    /**
     * Returns the ID of the layer.
     * 
     * @return the ID of the layer.
     */
    public int getIdentifier() {
        return identifier;
    }

    @Override
    public String getIdString() {
        if (getLevel() <= 1) {
            return "";
        } else {
            String id = "_" + getLevel() + "_" + getIdentifier() + "_";
            if (getSuperUnit() == null) {
                id += "0_0";
            } else {
                id += getSuperUnit().getXPos() + "_" + getSuperUnit().getYPos();
            }
            return id;
        }
    }

    @Override
    public int getLevel() {
        return level;
    }

    public double getMapDistance(int x1, int y1, int x2, int y2) {
        return getMapDistance(x1, y1, 0, x2, y2, 0);
    }

    @Override
    public double getMapDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
    }

    @Override
    public double getMapDistance(Unit u1, Unit u2) {
        return getMapDistance(u1.getXPos(), u1.getYPos(), u1.getZPos(), u2.getXPos(), u2.getYPos(), u2.getZPos());
    }

    public double getMapDistanceSq(int x1, int y1, int z1, int x2, int y2, int z2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
    }

    public double getMapDistanceSq(Unit u1, Unit u2) {
        return getMapDistanceSq(u1.getXPos(), u1.getYPos(), u1.getZPos(), u2.getXPos(), u2.getYPos(), u2.getZPos());
    }

    /**
     * Calculates and returns the mean quantization error of the map based on the mean of the quantization errors of the
     * single units.
     * 
     * @return the mean quantization error of the map, or 0 if no data is mapped.
     */
    /*
     * public double getMeanMeanQuantizationError() { double mqe = 0; int nonemptyUnits = 0; for (int j=0;j<ySize;j++) { for (int i=0; i<xSize; i++) {
     * mqe += units[i][j].getMeanQuantizationError(); if (units[i][j].getMeanQuantizationError() > 0) { nonemptyUnits++; } } } if (nonemptyUnits>0) {
     * return mqe/nonemptyUnits; } else { return 0; } }
     */

    /**
     * Calculates and returns the mean quantization error of the map based on the quantization errors of the single
     * units.
     * 
     * @return the mean quantization error of the map, or 0 if no data is mapped.
     */
    /*
     * public double getMeanQuantizationError() { double mqe = 0; int nonemptyUnits = 0; for (int j=0;j<ySize;j++) { for (int i=0; i<xSize; i++) { mqe
     * += units[i][j].getQuantizationError(); if (units[i][j].getQuantizationError() > 0) { nonemptyUnits++; } } } if (nonemptyUnits>0) { return
     * mqe/nonemptyUnits; } else { return 0; } }
     */

    @Override
    public DistanceMetric getMetric() {
        return metric;
    }

    /**
     * Returns the neighboring unit of a unit specified by argument <code>u</code> with the most distant weight vector.
     * 
     * @param u the unit for which the most dissimilar neighbor should be determined.
     * @return the neighboring unit with the most distant weight vector.
     */
    protected Unit getMostDissimilarNeighbor(Unit u) {
        Unit neighbor = null;
        double largestDistance = 0;
        double distance = 0;
        try {
            for (Unit neighbouringUnit : getNeighbouringUnits(u)) {
                try {
                    distance = metric.distance(u.getWeightVector(), neighbouringUnit.getWeightVector());
                } catch (MetricException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                }
                if (distance > largestDistance) {
                    neighbor = neighbouringUnit;
                    largestDistance = distance;
                }
            }
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
        }

        if (neighbor != null) {
            return neighbor;
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "A unit on a one-unit SOM has no neighbors. Something went terribly wrong ;-) Aborting.");
            System.exit(-1);
            return null;
        }
    }

    @Override
    public QualityMeasure getQualityMeasure() {
        return qualityMeasure;
    }

    @Override
    public String getRevision() {
        return revision;
    }

    /**
     * Returns the superordinate unit, or <code>null</code> if none exists.
     * 
     * @return the superordinate unit, or <code>null</code> if none exists.
     */
    public Unit getSuperUnit() {
        return superUnit;
    }

    @Override
    public Unit getUnit(int x, int y) throws LayerAccessException {
        return getUnit(x, y, 0);
    }

    @Override
    public Unit getUnit(int x, int y, int z) throws LayerAccessException { // TODO: check for invalid x/y
        try {
            return units[x][y][z];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new LayerAccessException("Position " + x + "/" + y + "/" + z + " is invalid. Map size is " + xSize
                    + "x" + ySize + "x" + zSize);
        }
    }

    /**
     * Returns the index of given unit coordinates. The index is equivalent to the index of the unit in question in the
     * array returned by {@link #getAllUnits()}.
     */
    public int getUnitIndex(int x, int y) {
        return y * xSize + x;
    }

    public Unit getUnitForIndex(int index) throws LayerAccessException {
        int y = index / xSize;
        int x = index % xSize;
        return getUnit(x, y);
    }

    @Override
    public Unit getUnitForDatum(String name) {
        for (int k = 0; k < zSize; k++) {
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    if (units[i][j][k].isMapped(name)) {
                        return units[i][j][k];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the winner unit for a given input datum specified by argument <code>input</code>.
     * 
     * @param input the input datum for which the winner unit will be searched.
     * @param metric the metric to be used.
     * @return the winner unit.
     */
    public Unit getWinner(InputDatum input, DistanceMetric metric) {
        Unit winner = null;
        double smallestDistance = Double.MAX_VALUE;
        double[] inputVector = input.getVector().toArray();
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {

                    double distance = 0;
                    try {
                        distance = metric.distance(units[i][j][k].getWeightVector(), inputVector);
                        // For adaptive coordinate calculation: store distance from the unit to the winner
                        if (virtualLayer != null) {
                            virtualLayer.setDistanceToWinner(i, j, distance);
                        }
                    } catch (MetricException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    if (distance < smallestDistance) {
                        smallestDistance = distance;
                        winner = units[i][j][k];
                    }
                }
            }
        }
        return winner;
    }

    /**
     * Returns the winner for a given unit, using a weighted distance metric. Used for semi-supervised/active learning &
     * correcting input locations.
     */
    public Unit getWinner(InputDatum input, AbstractWeightedMetric metric) {
        Unit winner = null;
        double smallestDistance = Double.MAX_VALUE;
        double[] inputVector = input.getVector().toArray();
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    try {
                        double distance = metric.distance(units[i][j][k].getWeightVector(), inputVector,
                                units[i][j][k].getFeatureWeights());
                        if (distance < smallestDistance) {
                            smallestDistance = distance;
                            winner = units[i][j][k];
                        }
                    } catch (MetricException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                }
            }
        }
        return winner;
    }

    /**
     * Returns the winner unit for a given input datum specified by argument <code>input</code>.
     * 
     * @param input the input datum for which the winner unit will be searched.
     * @return the winner unit.
     */
    public Unit getWinner(InputDatum input) {
        return getWinner(input, metric);
    }

    /** Return the winning unit for the given query. */
    public Unit getWinnerFromQuery(String query, SOMLibTemplateVector templateVector) {
        double[] values = getInputVector(query, templateVector);

        InputDatum datum = new InputDatum("Query: " + query, new SparseDoubleMatrix1D(values));

        // FIXME: make this more generic to use other Sparse Metrics
        Unit winner = getWinner(datum, new L2MetricSparse());
        return winner;
    }

    public Unit[] getWinnersFromQuery(String query, int num, SOMLibTemplateVector templateVector) {
        double[] values = getInputVector(query, templateVector);

        InputDatum datum = new InputDatum("Query: " + query, new SparseDoubleMatrix1D(values));

        // FIXME: make this more generic to use other Sparse Metrics
        Unit[] winners = getWinners(datum, num, new L2MetricSparse());
        return winners;
    }

    public String[] getWinningInputDataFromQuery(String query, int num, SOMLibTemplateVector templateVector) {
        String[] data = new String[num];
        Unit[] winners = getWinnersFromQuery(query, getUnitCount(), templateVector);
        int counter = 0;
        for (Unit winner : winners) {
            String[] mappedInputNames = winner.getMappedInputNames();
            if (mappedInputNames != null) {
                for (String mappedInputName : mappedInputNames) {
                    data[counter] = mappedInputName;
                    counter++;
                    if (counter >= data.length) {
                        return data;
                    }
                }
            }
        }
        return data;
    }

    private double[] getInputVector(String query, SOMLibTemplateVector templateVector) {
        String[] terms = query.split(" ");
        Hashtable<String, Integer> termMap = new Hashtable<String, Integer>();
        for (String term : terms) {
            if (termMap.get(term) != null) {
                Integer v = termMap.get(term);
                termMap.put(term, new Integer(v.intValue() + 1));
            } else {
                termMap.put(term, new Integer(1));
            }
        }
        double[] values = templateVector.getTFxIDFVectorFromTerms(termMap);
        return values;
    }

    /**
     * Returns a number of best-matching units sorted by distance (ascending) for a given input datum. If the number of
     * best-matching units is greater than the total number of units on the map, all units of the map are returned
     * (appropriately ranked).
     * 
     * @param input the input datum for which the best-matching units will be searched.
     * @param num the number of best-matching units.
     * @return an array of Unit containing best-matching units sorted ascending by distance from the input datum.
     */
    public Unit[] getWinners(InputDatum input, int num, DistanceMetric metric) {
        if (num > xSize * ySize * zSize) {
            num = xSize * ySize * zSize;
        }
        Unit[] res = new Unit[num];
        double[] dists = new double[num];
        for (int i = 0; i < num; i++) {
            res[i] = null;
            dists[i] = Double.MAX_VALUE;
        }

        DoubleMatrix1D vec = input.getVector();
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    double distance = 0;
                    try {
                        distance = metric.distance(units[i][j][k].getWeightVector(), vec);
                    } catch (MetricException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    int element = 0;
                    boolean inserted = false;
                    while (inserted == false && element < num) {
                        if (distance < dists[element]) { // found place to insert unit
                            for (int m = num - 2; m >= element; m--) { // move units with greater distance to right
                                res[m + 1] = res[m];
                                dists[m + 1] = dists[m];
                            }
                            res[element] = units[i][j][k];
                            dists[element] = distance;
                            inserted = true;
                        }
                        element++;
                    }

                }
            }
        }
        return res;
    }

    public Unit[] getWinners(InputDatum input, int num) {
        return getWinners(input, num, metric);
    }

    /**
     * Returns a number of best-matching units and distances sorted by distance (ascending) for a given input datum. If
     * the number of best-matching units is greater than the total number of units on the map, all units of the map are
     * returned (appropriately ranked).
     * 
     * @param input the input datum for which the winner unit will be searched.
     * @param num the number of best-matching units.
     * @return the <code>Vector</code> containing an array of Unit (elementAt(0)) and array of double (elementAt(1))
     *         containing best-matching units sorted ascending by distance from the input datum.
     */
    public UnitDistance[] getWinnersAndDistances(InputDatum input, int num) {
        if (num > xSize * ySize * zSize) {
            num = xSize * ySize * zSize;
        }
        UnitDistance[] res = new UnitDistance[num];
        DoubleMatrix1D vec = input.getVector();
        // FIXME: this algorithm should be optimisable, especially the inserting part!
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    double distance = 0;
                    try {
                        distance = metric.distance(units[i][j][k].getWeightVector(), vec);
                    } catch (MetricException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    int element = 0;
                    boolean inserted = false;
                    while (inserted == false && element < num) {
                        if (res[element] == null || distance < res[element].getDistance()) { // found place to insert
                            // unit
                            for (int m = num - 2; m >= element; m--) { // move units with greater distance to right
                                res[m + 1] = res[m];
                            }
                            res[element] = new UnitDistance(units[i][j][k], distance);
                            inserted = true;
                        }
                        element++;
                    }

                }
            }
        }
        return res;
    }

    @Override
    public int getXSize() {
        return xSize;
    }

    @Override
    public int getYSize() {
        return ySize;
    }

    @Override
    public int getZSize() {
        return zSize;
    }

    public int getUnitCount() {
        return getXSize() * getYSize() * getZSize();
    }

    public boolean hasNeighbours(int x, int y) throws LayerAccessException {
        if (x > 0 && getUnit(x - 1, y, 0) != null) {
            return true;
        } else if (x + 1 < units.length && getUnit(x + 1, y, 0) != null) {
            return true;
        } else if (y > 0 && getUnit(x, y - 1, 0) != null) {
            return true;
        } else if (y + 1 < units[x].length && getUnit(x, y + 1, 0) != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasNeighbours(int x, int y, int z) throws LayerAccessException {
        if (x > 0 && getUnit(x - 1, y, z) != null) {
            return true;
        } else if (x + 1 < units.length && getUnit(x + 1, y, z) != null) {
            return true;
        } else if (y > 0 && getUnit(x, y - 1, z) != null) {
            return true;
        } else if (y + 1 < units[x].length && getUnit(x, y + 1, z) != null) {
            return true;
        } else if (z > 0 && getUnit(x, y, z - 1) != null) {
            return true;
        } else if (z + 1 < units[x].length && getUnit(x, y, z + 1) != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isEdgeColumn(int x) throws LayerAccessException {
        return x == 0 || x + 1 == xSize;
    }

    public boolean isEdgeRow(int y) throws LayerAccessException {
        return y == 0 || y + 1 == ySize;
    }

    /**
     * NOT USED AT THE MOMENT
     * 
     * @param data
     */
    /*
     * public void initLayerBySamples(InputData data) { for (int j=0; j<ySize; j++) { for (int i=0; i<xSize; i++) { InputDatum datum =
     * data.getRandomInputDatum(1,5); // FIXME: 1,5!!! units[i][j].initWeightVectorBySample(datum); } } }
     */

    /**
     * Inserts a row or column of units between units specified by argument <code>a</code> and <code>b</code>.
     * 
     * @param a a unit on the layer.
     * @param b a unit on the layer.
     */
    private void insertRowColumn(Unit a, Unit b, ProgressListener listener) {
        Unit[][][] newUnits = null;
        if (a.getXPos() != b.getXPos()) { // insert column
            int insertPos = Math.max(a.getXPos(), b.getXPos());
            String message = "Inserted column " + insertPos + ". New size is " + (xSize + 1) + "x" + ySize + "x"
                    + zSize;
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(message);
            listener.insertColumn(xSize + 1, message);
            newUnits = new Unit[xSize + 1][ySize][zSize];
            for (int i = 0; i < xSize + 1; i++) {
                for (int j = 0; j < ySize; j++) {
                    for (int k = 0; k < zSize; k++) {
                        if (i < insertPos) {
                            newUnits[i][j][k] = units[i][j][k];
                        } else if (i == insertPos) {
                            try {
                                newUnits[i][j][k] = new Unit(this, i, j, k, AbstractMetric.meanVector(
                                        units[i - 1][j][k].getWeightVector(), units[i][j][k].getWeightVector()));
                            } catch (MetricException e) {
                                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                                System.exit(-1);
                            }
                        } else if (i > insertPos) {
                            newUnits[i][j][k] = units[i - 1][j][k];
                            newUnits[i][j][k].updatePosition(i, j, k);
                        }
                    }
                }
            }
            xSize++;
        } else if (a.getYPos() != b.getYPos()) { // insert row
            int insertPos = Math.max(a.getYPos(), b.getYPos());
            String message = "Inserted row " + insertPos + ". New size is " + xSize + "x" + ySize + "x" + zSize;
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(message);

            listener.insertRow(ySize + 1, message);

            newUnits = new Unit[xSize][ySize + 1][zSize];
            for (int k = 0; k < zSize; k++) {
                for (int j = 0; j < ySize + 1; j++) {
                    for (int i = 0; i < xSize; i++) {
                        if (j < insertPos) {
                            newUnits[i][j][k] = units[i][j][k];
                        } else if (j == insertPos) {
                            try {
                                newUnits[i][j][k] = new Unit(this, i, j, k, AbstractMetric.meanVector(
                                        units[i][j - 1][k].getWeightVector(), units[i][j][k].getWeightVector()));
                            } catch (MetricException e) {
                                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                                System.exit(-1);
                            }
                        } else if (j > insertPos) {
                            newUnits[i][j][k] = units[i][j - 1][k];
                            newUnits[i][j][k].updatePosition(i, j, k);
                        }
                    }
                }
            }
            ySize++;
        } else if (a.getZPos() != b.getZPos()) { // insert depth-level
            int insertPos = Math.max(a.getZPos(), b.getZPos());
            String message = "Inserted depth-level " + insertPos + ". New size is " + xSize + "x" + ySize + "x"
                    + (zSize + 1);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(message);

            listener.insertRow(zSize + 1, message);

            newUnits = new Unit[xSize][ySize][zSize + 1];
            for (int k = 0; k < zSize + 1; k++) {
                for (int j = 0; j < ySize; j++) {
                    for (int i = 0; i < xSize; i++) {
                        if (k < insertPos) {
                            newUnits[i][j][k] = units[i][j][k];
                        } else if (k == insertPos) {
                            try {
                                newUnits[i][j][k] = new Unit(this, i, j, k, AbstractMetric.meanVector(
                                        units[i][j][k - 1].getWeightVector(), units[i][j][k].getWeightVector()));
                            } catch (MetricException e) {
                                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                                System.exit(-1);
                            }
                        } else if (k > insertPos) {
                            newUnits[i][j][k] = units[i][j][k - 1];
                            newUnits[i][j][k].updatePosition(i, j, k);
                        }
                    }
                }
            }
            zSize++;
        }
        units = newUnits;
    }

    /**
     * Maps data onto layer without recalculating the quantization error after every single input datum.<br>
     * FIXME: add multi-threading
     * 
     * @param data input data to be mapped onto layer.
     */
    private void mapCompleteDataAfterTraining(InputData data) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start mapping data.");
        InputDatum datum = null;
        Unit winner = null;
        int numVectors = data.numVectors();
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors, "Mapping datum ", 50);
        for (int i = 0; i < numVectors; i++) {
            datum = data.getInputDatum(i);
            winner = getWinner(datum);
            winner.addMappedInput(datum, false);
            progressWriter.progress();
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished mapping data.");
    }

    /**
     * Maps input data onto layer. Quantization errors of units are updated automatically. This is slow for a large
     * number of input data.
     * 
     * @param data input data to be mapped onto layer.
     */
    public void mapData(InputData data) {
        int numVectors = data.numVectors();
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors, "Mapping datum ", 50);
        for (int i = 0; i < numVectors; i++) {
            mapDatum(data.getInputDatum(i));
            progressWriter.progress();
        }
    }

    /**
     * Maps a single input datum onto the layer and returns the winning unit.
     * 
     * @param datum input datum to be mapped onto the layer.
     * @return the winner unit.
     */
    public Unit mapDatum(InputDatum datum) {
        Unit winner = getWinner(datum);
        winner.addMappedInput(datum, true);
        if (winner.getMappedSOM() != null) {
            winner.getMappedSOM().getLayer().mapDatum(datum);
        }
        return winner;
    }

    /**
     * Assigns an object that implements the {@link TrainingInterruptionListener} interface to perform actions in
     * certain intervals during training determined by argument <code>i</code>.
     * 
     * @param t the listening object.
     * @param i the number of training cycles after which the training process is interrupted.
     */
    public void setTrainingInterruptionListener(TrainingInterruptionListener t, int i) {
        til = t;
        if (t == null) {
            interruptEvery = 0;
        } else {
            interruptEvery = i;
        }
    }

    /**
     * Trains the layer with the input data. If the value of argument <code>tau</code> is 1, a fix-sized layer is
     * trained, otherwise the layer grows until a certain quality criterion determined by <code>tau</code> and the mean
     * quantization error of the data (which is automatically calculated) is reached. The process ends with all training
     * data being mapped onto the growing SOM. The units' quantization errors are calculated.
     * 
     * @param data input data to train the layer with.
     * @param iniLearnrate initial value of learnrate.
     * @param iniSigma initial sigma determining the neighborhood of the winner.
     * @param numIterations the number of iterations until the mapping quality check.
     * @param tau the fraction of the map's quantization error that determines the quality of the mapping.
     */

    public QualityMeasure train(InputData data, double iniLearnrate, double iniSigma, int numIterations, double tau,
            String qualityMeasureName, SOMProperties trainingProps) {
        return train(data, iniLearnrate, iniSigma, numIterations, 0, tau, qualityMeasureName, trainingProps);
    }

    /**
     * Trains the layer with the input data. If the value of argument <code>tau</code> is 1, a fix-sized layer is
     * trained, otherwise the layer grows until a certain quality criterion determined by <code>tau</code> and the mean
     * quantization error of the data (which is automatically calculated) is reached. The process ends with all training
     * data being mapped onto the growing SOM. The units' quantization errors are calculated.
     * 
     * @param data input data to train the layer with.
     * @param iniLearnrate initial value of learnrate.
     * @param iniSigma initial sigma determining the neighborhood of the winner.
     * @param numIterations the number of iterations until the mapping quality check.
     * @param startIteration start with iteration x -- important for Lernrate / Sigma calculation
     * @param tau the fraction of the map's quantization error that determines the quality of the mapping.
     */

    public QualityMeasure train(InputData data, double iniLearnrate, double iniSigma, int numIterations,
            int startIteration, double tau, String qualityMeasureName, SOMProperties trainingProps) {
        // TODO: Quanitzation error default ... add method to allow for free definition
        return train(data, iniLearnrate, iniSigma, numIterations, 0, tau, Double.MAX_VALUE, qualityMeasureName,
                trainingProps);
    }

    /**
     * Trains the layer with the input data. If the value of argument <code>tau</code> is 1, a fix-sized layer is
     * trained, otherwise the layer grows until a certain quality criterion determined by <code>tau</code> and the mean
     * quantization error specified by argument <code>mqe0</code> is reached. The process ends with all training data
     * being mapped onto the growing SOM. The units' quantization errors are calculated.
     * 
     * @param data input data to train the layer with.
     * @param initialLearnrate initial value of learnrate.
     * @param initialSigma initial sigma determining the neighbourhood of the winner.
     * @param numIterations the number of iterations until the mapping quality check.
     * @param tau the fraction of the map's quantisation error that determines the quality of the mapping.
     * @param targetQualityValue mean quantisation error determining the desired granularity of data representation.
     *            Used for layers in GHSOMs.
     */

    public QualityMeasure train(InputData data, double initialLearnrate, double initialSigma, int numIterations,
            double tau, double targetQualityValue, String qualityMeasureName, SOMProperties trainingProps) {
        return train(data, initialLearnrate, initialSigma, numIterations, 0, tau, targetQualityValue,
                qualityMeasureName, trainingProps);
    }

    /**
     * Trains the layer with the input data. If the value of argument <code>tau</code> is 1, a fix-sized layer is
     * trained, otherwise the layer grows until a certain quality criterion determined by <code>tau</code> and the mean
     * quantization error specified by argument <code>mqe0</code> is reached. The process ends with all training data
     * being mapped onto the growing SOM. The units' quantization errors are calculated.
     * 
     * @param data input data to train the layer with.
     * @param numIterations the number of iterations until the mapping quality check.
     * @param startIteration start with iteration x -- important for Learnrate / Sigma calculation
     * @param tau the fraction of the map's quantisation error that determines the quality of the mapping.
     * @param targetQualityValue mean quantisation error determining the desired granularity of data representation.
     *            Used for layers in GHSOMs.
     * @param initialLearnrate initial value of learnrate.
     * @param initialSigma initial sigma determining the neighbourhood of the winner.
     */

    public QualityMeasure train(InputData data, double initialLearnrate, double initialSigma, int numIterations,
            int startIteration, double tau, double targetQualityValue, String qualityMeasureName,
            SOMProperties trainingProps) {
        // double initialLearnrate = iniLearnrate;
        // double initialSigma = iniSigma;
        double expParam = numIterations / 8; // TODO: hidden parameter
        double expParam2 = numIterations / 5; // TODO: hidden parameter
        double favourSelected = 3.0; // FIXME: hidden Parameter

        if (qualityMeasureName == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "No quality measure provided for training. Using mean quantization error of map.");
            qualityMeasureName = "at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError.mqe";
        }
        String[] qmNameMethod = AbstractQualityMeasure.splitNameAndMethod(qualityMeasureName);

        // if no GHSOM use but tau<1 //TODO:change back!!!!!!!!!!!
        if (targetQualityValue == Double.MAX_VALUE) {// && (tau<1)) { // to save time when using large data sets
            GrowingLayer layer0 = new GrowingLayer(1, 1, 1, "at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric", dim,
                    false, trainingProps.pca(), 7, data);

            // set layer 0 unit to mean of data
            try {
                layer0.getUnit(0, 0, 0).setWeightVector(data.getMeanVector().toArray());
                layer0.getUnit(0, 0, 0).addMappedInput(data, false);
            } catch (SOMToolboxException e) { /* does not happen */
            }

            // calculate map error
            QualityMeasure qm0 = null;
            try {
                qm0 = AbstractQualityMeasure.instantiate(qmNameMethod[0], layer0, data);// new QuantizationError(layer0,
                // data);
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not instantiate quality measure.");
                System.exit(-1);
            }

            // calc qm0
            try {
                targetQualityValue = qm0.getMapQuality(qmNameMethod[1]);
            } catch (QualityMeasureNotFoundException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
                System.exit(-1);
            }
        }

        // init executor thread, if needed
        if (threadsUsed > 1) {
            e = Executors.newFixedThreadPool(threadsUsed);
        }

        boolean reachedQuality = false;
        QualityMeasure qm = null;
        int selectedInstances = 0;
        SOMLibClassInformation classInfo = null;
        double minProbability = 1;

        // if special training, do some init
        if (specialClassMode(trainingProps) || trainingProps.datumToUnitMappings().size() > 0) {
            /* Angela: special treatment of some clases during training */

            if (trainingProps.getClassInfoFileName() != null
                    && (trainingProps.getSelectedClassMode() == SOMProperties.MODE_EXCEPT || trainingProps.getSelectedClassMode() == SOMProperties.MODE_FAVOUR)) {
                try {
                    classInfo = new SOMLibClassInformation(trainingProps.getClassInfoFileName());
                    for (int i = 0; i < data.numVectors(); i++) {
                        try {
                            InputDatum currentInput = data.getInputDatum(i);
                            String inpLabel = currentInput.getLabel();
                            if (trainingProps.getSelectedClasses().contains(classInfo.getClassName(inpLabel))) {
                                selectedInstances++;
                            }
                        } catch (SOMLibFileFormatException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } catch (SOMToolboxException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            minProbability = 1 - 1d / favourSelected;

            double cycles = (double) numIterations / (double) data.numVectors();
            if (trainingProps.getSelectedClassMode() == SOMProperties.MODE_EXCEPT) {
                numIterations = (int) (cycles * (data.numVectors() - selectedInstances));
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Skipping classes: " + trainingProps.getSelectedClasses() + ", total " + selectedInstances
                                + " inputs.");
            } else if (trainingProps.getSelectedClassMode() == SOMProperties.MODE_FAVOUR) {
                numIterations = (int) ((data.numVectors() + selectedInstances * (favourSelected - 1)) * cycles);
                numIterations = (int) (data.numVectors() * cycles * favourSelected);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Favouring classes: " + trainingProps.getSelectedClasses() + ", total " + selectedInstances
                                + " inputs, favour factor: " + favourSelected + " --> min probability: "
                                + minProbability);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Total inputs: " + data.numVectors() + " --> iterations: ~"
                                + (int) (numIterations / favourSelected));
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "No classes selected, normaly training with " + data.numVectors() + " inputs.");
            }

            skippedNonSelected = 0;
            trainedNonSelected = 0;

        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Going to train " + numIterations + " iterations.");
        ProgressListener progressWriter = ProgressListenerFactory.getInstance().createProgressListener(numIterations,
                "Iteration ", Math.max(1, Math.min(1000, numIterations / 200)), numIterations / 20);
        while (reachedQuality == false) {
            if (specialClassMode(trainingProps) || trainingProps.datumToUnitMappings().size() > 0) {
                trainSpecial(data, numIterations, 0, trainingProps, initialLearnrate, initialSigma, expParam,
                        expParam2, classInfo, minProbability, progressWriter);
            } else {
                trainNormal(data, numIterations, 0, trainingProps, initialLearnrate, initialSigma, expParam, expParam2,
                        progressWriter);
            }

            if (tau == 1) { // fixed size, just map the data and finish
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Training done.");
                clearMappedInput();
                mapCompleteDataAfterTraining(data);

                // calc QualityMeasure
                try {
                    qm = AbstractQualityMeasure.instantiate(qmNameMethod[0], this, data);
                    // printInfo(targetQualityValue, qmNameMethod, qm);
                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not instantiate quality measure.");
                    System.exit(-1);
                }
                reachedQuality = true;

            } else { // possibly growing, check the map's mqe against mqe0*tau
                clearMappedInput();
                mapCompleteDataAfterTraining(data);

                // calc QualityMeasure
                try {
                    qm = AbstractQualityMeasure.instantiate(qmNameMethod[0], this, data);
                    // printInfo(targetQualityValue, qmNameMethod, qm);
                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not instantiate quality measure.");
                    System.exit(-1);
                }

                double currentQuality = 0;
                try {
                    currentQuality = qm.getMapQuality(qmNameMethod[1]);
                } catch (QualityMeasureNotFoundException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
                    System.exit(-1);
                }

                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Reached layer quality? "
                                + (currentQuality <= targetQualityValue * tau)//
                                + " (current quality: " + currentQuality + ", required quality: " + targetQualityValue
                                + " * " + tau + " = " + targetQualityValue * tau + ")");
                if (currentQuality <= targetQualityValue * tau) {
                    reachedQuality = true;
                } else { // grow it
                    // determine unit with highest qe/mqe
                    Unit e = getErrorUnit(qm, qm.getUnitQualityNames()[0]); // TODO: attention! alternative criteria
                    // possible
                    // determine unit with largest distance
                    Unit d = getMostDissimilarNeighbor(e);
                    // insert row/column
                    insertRowColumn(e, d, progressWriter);
                }
            }
        }

        // stop executor thread, if needed
        if (threadsUsed > 1) {
            e.shutdown();
        }

        this.qualityMeasure = qm;
        return qm;
    }

    private void trainSpecial(InputData data, int numIterations, int startIteration, SOMProperties trainingProps,
            double initialLearnrate, double initialSigma, double expParam, double expParam2,
            SOMLibClassInformation classInfo, double minProbability, ProgressListener progressWriter) {
        double currentLearnrate = initialLearnrate;
        double currentSigma = initialSigma;

        for (int i = startIteration; i < numIterations; i++) {
            if (til != null) {
                if (i % interruptEvery == 0) {
                    til.interruptionOccurred(i, numIterations);
                }
            }

            // No check for batch som - special training is always witch incremental algorithm

            // get new input
            InputDatum currentInput = data.getRandomInputDatum(i, numIterations);

            String inpLabel = currentInput.getLabel();

            try {
                boolean contains = trainingProps.getSelectedClasses() != null
                        && trainingProps.getSelectedClasses().contains(classInfo.getClassName(inpLabel));
                if (contains && trainingProps.getSelectedClassMode() == SOMProperties.MODE_EXCEPT) {
                    // Skips this training step
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                            "Found class " + classInfo.getClassName(inpLabel) + " at input #" + inpLabel);
                    i--;
                    continue;
                } else {
                    if (trainingProps.getSelectedClassMode() == SOMProperties.MODE_FAVOUR) {
                        // training with favouring selected classes
                        double prob = randSkipProbability.nextDouble();
                        if (!contains) {
                            if (prob < minProbability) {
                                // skipping
                                progressWriter.progress();
                                skippedNonSelected++;
                                continue;
                            } else {
                                trainedNonSelected++;
                            }
                        }
                    }
                    // do normal training
                    //
                    // test if we have a fixed datum to unit mapping for currentInput
                    Unit winner = null;
                    for (int j = 0; j < trainingProps.datumToUnitMappings().size(); j++) {
                        SOMProperties.DatumToUnitMapping mapping = trainingProps.datumToUnitMappings().get(j);

                        if (currentInput.getLabel().equals(mapping.label)) {
                            try {
                                System.out.println("\n   *** Using a datum with fixed mapping, label: " + mapping.label
                                        + "unit x, y: " + mapping.unitX + ", " + mapping.unitY);

                                winner = getUnit(mapping.unitX, mapping.unitY);
                            } catch (Exception e) {
                                System.out.println("\n\n   !!!! *******  GrowingLayer.train(): Exception: " + e);
                            }
                        }
                    }

                    if (winner == null) {
                        // get winner
                        winner = getWinner(currentInput);
                    }

                    // update weight vectors
                    updateUnits(winner, currentInput, currentLearnrate, currentSigma);

                    // adjust learnrate and neighborhood
                    currentLearnrate = initialLearnrate * Math.exp(-1.0 * i / expParam); // exponential
                    currentSigma = initialSigma * Math.exp(-1.0 * i / expParam2); // exponential
                    if (currentLearnrate < 0.0001) {
                        currentLearnrate = 0.0001;
                    } // TODO: hidden parameter
                    if (currentSigma < 0.01) {
                        currentSigma = 0.01;
                    } // TODO: hidden parameter

                    /* debug */
                    /*
                     * if (i % 700 == 0) { clearMappedInput(); mapCompleteDataAfterTraining(data); calculateQuantizationErrorAfterTraining();
                     * System.out.println("--------------------"); System.out.println("Iteration: "+i+" MQE: "+getMeanQuantizationError()+" MMQE:
                     * "+getMeanMeanQuantizationError()); System.out.println(" Learnrate: "+currentLearnrate+" Sigma: "+currentSigma); for (int
                     * j=0;j<ySize; j++) { for (int jj=0;jj<xSize; jj++) { System.out.print(units[jj][j].getNumberOfMappedInputs()+" "); }
                     * System.out.println(); } System.out.println(); }
                     */
                    progressWriter.progress();
                }
            } catch (SOMLibFileFormatException e) {
                // TODO Auto-generated catch block
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("This should never happen");
                e.printStackTrace();
            }

        }
    }

    private void trainNormal(InputData data, int numIterations, int startIteration, SOMProperties trainingProps,
            double initialLearnrate, double initialSigma, double expParam, double expParam2,
            ProgressListener progressWriter) {

        if (trainingProps.adaptiveCoordinatesTreshold() != null) {
            virtualLayer = new AdaptiveCoordinatesVirtualLayer(xSize, ySize,
                    trainingProps.adaptiveCoordinatesTreshold());
            Logger.getLogger("at.tuwien.ifs.somtoolbox.GrowingLayer").info(
                    "Adaptive Coordinates Threshold specified: "
                            + Arrays.toString(trainingProps.adaptiveCoordinatesTreshold()));
        }

        double currentLearnrate = initialLearnrate;
        double currentSigma = initialSigma;

        for (int i = startIteration; i < numIterations; i++) {
            if (til != null) {
                if (i % interruptEvery == 0) {
                    til.interruptionOccurred(i, numIterations);
                }
            }

            if (trainingProps.batchSom()) {
                try {
                    progressWriter.progress();
                    for (int j = 0; j < data.numVectors(); j++) {
                        InputDatum currentInput = data.getInputDatum(j);
                        Unit winner = getWinner(currentInput);
                        winner.addBatchSomNeighbour(currentInput);
                        for (Unit element : getNeighbouringUnits(winner, trainingProps.neighbourWidth())) {
                            element.addBatchSomNeighbour(currentInput);
                        }
                    }
                    for (int k = 0; k < zSize; k++) {
                        for (int j = 0; j < ySize; j++) {
                            for (int l = 0; l < xSize; l++) {
                                units[l][j][k].getWeightVectorFromBatchSomNeighbourhood();
                                units[l][j][k].clearBatchSomList();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // get new input
                InputDatum currentInput = data.getRandomInputDatum(i, numIterations);

                // get winner & update weight vectors
                final Unit winner = getWinner(currentInput);
                updateUnits(winner, currentInput, currentLearnrate, currentSigma);
                if (virtualLayer != null) {
                    virtualLayer.updateUnitsVirtualSpacePos(units, metric, winner, currentInput, i);
                }

                // adjust learnrate and neighborhood
                currentLearnrate = initialLearnrate * Math.exp(-1.0 * i / expParam); // exponential
                currentSigma = initialSigma * Math.exp(-1.0 * i / expParam2); // exponential
                if (currentLearnrate < 0.0001) {
                    currentLearnrate = 0.0001;
                } // TODO: hidden parameter
                if (currentSigma < 0.01) {
                    currentSigma = 0.01;
                } // TODO: hidden parameter

                progressWriter.progress();
            }
        }
    }

    private boolean specialClassMode(SOMProperties trainingProps) {
        return trainingProps.getClassInfoFileName() != null
                && (trainingProps.getSelectedClassMode() == SOMProperties.MODE_EXCEPT || trainingProps.getSelectedClassMode() == SOMProperties.MODE_FAVOUR);
    }

    private void initCPURanges() {
        if (threadsUsed <= 1 || ranges != null || xSize * ySize * zSize < 2) {
            return;
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox.GrowingLayer").info(
                "Splitting map into (at most) " + threadsUsed + " slices.");
        ArrayList<Cuboid> cubs = new ArrayList<Cuboid>();
        cubs.add(new Cuboid(0, xSize, 0, ySize, 0, zSize));
        while (cubs.size() < threadsUsed) {
            // Get biggest cuboid
            int bigS = -1;
            int bigI = -1;
            for (int i = 0; i < cubs.size(); i++) {
                Cuboid c = cubs.get(i);
                int curS = (c.getEndX() - c.getStartX()) * (c.getEndY() - c.getStartY())
                        * (c.getEndZ() - c.getStartZ());
                if (curS > bigS) {
                    bigS = curS;
                    bigI = i;
                }
            }

            // The biggest cuboid will be split into two smaller ones.
            Cuboid biggest = cubs.remove(bigI);
            int xSpan = biggest.getEndX() - biggest.getStartX();
            int ySpan = biggest.getEndY() - biggest.getStartY();
            int zSpan = biggest.getEndZ() - biggest.getStartZ();

            // If the biggest cuboid contains only one unit there is no more splitting possible --> exit
            if (Math.max(xSpan, Math.max(ySpan, zSpan)) < 2) {
                cubs.add(biggest);
                Logger.getLogger("at.tuwien.ifs.somtoolbox.GrowingLayer").info("No more splitting possible.");
                break;
            }

            // We split along the longest side.
            Cuboid new1, new2;
            if (xSpan >= ySpan && xSpan >= zSpan) {
                // We split along x
                int split = biggest.getStartX() + xSpan / 2;
                new1 = new Cuboid(biggest.getStartX(), split, biggest.getStartY(), biggest.getEndY(),
                        biggest.getStartZ(), biggest.getEndZ());
                new2 = new Cuboid(split, biggest.getEndX(), biggest.getStartY(), biggest.getEndY(),
                        biggest.getStartZ(), biggest.getEndZ());
            } else if (ySpan >= xSpan && ySpan >= zSpan) {
                // We split along y
                int split = biggest.getStartY() + ySpan / 2;
                new1 = new Cuboid(biggest.getStartX(), biggest.getEndX(), biggest.getStartY(), split,
                        biggest.getStartZ(), biggest.getEndZ());
                new2 = new Cuboid(biggest.getStartX(), biggest.getEndX(), split, biggest.getEndY(),
                        biggest.getStartZ(), biggest.getEndZ());
            } else if (zSpan >= xSpan && zSpan >= ySpan) {
                // We split along z
                int split = biggest.getStartZ() + zSpan / 2;
                new1 = new Cuboid(biggest.getStartX(), biggest.getEndX(), biggest.getStartY(), biggest.getEndY(),
                        biggest.getStartZ(), split);
                new2 = new Cuboid(biggest.getStartX(), biggest.getEndX(), biggest.getStartY(), biggest.getEndY(),
                        split, biggest.getEndZ());
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.GrowingLayer").severe(
                        "Serious error while splitting into Cuboides for Multi-CPU");
                System.exit(1);
                // Must not happen, just to satisfy compiler...
                new1 = new2 = null;
            }
            System.out.printf("Splitting %s into %s and %s%n", biggest, new1, new2);
            cubs.add(new1);
            cubs.add(new2);
        }

        // The splits are done.
        ranges = cubs.toArray(new Cuboid[0]);

        // for the splits we need to make sure that the end value of the lower and the start value of the higher range
        // are equal, i.e. regard it as the range length rather than the endpoint
        System.out.println("Splits:");
        for (int i = 0; i < ranges.length; i++) {
            System.out.println("\t" + i + ": " + ranges[i]);
        }
    }

    private void printInfo(double targetQualityValue, String[] qmNameMethod, QualityMeasure qm)
            throws QualityMeasureNotFoundException {
        System.out.println(qmNameMethod[0] + " " + qmNameMethod[1]);
        System.out.println("targetQuality (qm0) " + targetQualityValue);

        for (int i = 0; i < qm.getMapQualityNames().length; i++) {
            System.out.println(qm.getMapQualityNames()[i] + " " + qm.getMapQuality(qm.getMapQualityNames()[i]));
        }
        System.out.println();
        for (int q = 0; q < qm.getUnitQualityNames().length; q++) {
            System.out.println(qm.getUnitQualityNames()[q]);
            double[][] matr = qm.getUnitQualities(qm.getUnitQualityNames()[q]);
            for (int j = 0; j < matr[0].length; j++) {
                for (double[] element : matr) {
                    System.out.print(StringUtils.format(element[j], 2) + "\t");
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    private void updateUnits(Unit winner, InputDatum input, double learnrate, double sigma) {
        updateUnitsNormal(winner, input, learnrate, sigma);

        // updateUnitsNoBorder(winner, input, learnrate, sigma);
    }

    /**
     * Updates the weight vectors of the all map units with respect to the input datum and the according winner unit. NO
     * BORDER
     * 
     * @param winner the winner unit.
     * @param input the input datum.
     * @param learnrate the learnrate.
     * @param sigma the width of the Gaussian determining the neighborhood radius.
     */
    private void updateUnitsNoBorder(Unit winner, InputDatum input, double learnrate, double sigma) {
        double unitDist = 0;
        double opt1 = 2 * sigma * sigma;
        double[] unitVector = null;
        double[] inputVector = input.getVector().toArray();

        // calc hci sum for corner unit as winner (0/0) (min)
        DoubleMatrix3D minHcis = DoubleFactory3D.dense.make(zSize, ySize, xSize);
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    unitDist = getMapDistance(0, 0, 0, i, j, k);
                    minHcis.setQuick(k, j, i, Math.exp(-1 * unitDist * unitDist / opt1));
                }
            }
        }
        double minHciSum = minHcis.aggregate(Functions.plus, Functions.identity);

        // calc hcis for all units with regard to winner
        DoubleMatrix3D hcis = DoubleFactory3D.dense.make(zSize, ySize, xSize);
        int wxp = winner.getXPos();
        int wyp = winner.getYPos();
        int wzp = winner.getZPos();
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    unitDist = getMapDistance(wxp, wyp, wzp, i, j, k);
                    hcis.setQuick(k, j, i, Math.exp(-1 * unitDist * unitDist / opt1));
                }
            }
        }
        double hciSum = hcis.aggregate(Functions.plus, Functions.identity);

        // do adaptation of vectors with the according normalized hcis and learnrate
        hcis.assign(Functions.mult(minHciSum / hciSum * learnrate));

        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    for (int ve = 0; ve < dim; ve++) {
                        unitVector = units[i][j][k].getWeightVector();
                        unitVector[ve] = unitVector[ve] + hcis.getQuick(k, j, i) * (inputVector[ve] - unitVector[ve]);
                    }
                }
            }
        }
    }

    /**
     * Updates the weight vectors of the all map units with respect to the input datum and the according winner unit.
     * 
     * @param winner the winner unit.
     * @param input the input datum.
     * @param learnrate the learnrate.
     * @param sigma the width of the Gaussian determining the neighborhood radius.
     */
    protected void updateUnitsNormal(Unit winner, InputDatum input, double learnrate, double sigma) {
        double opt1 = 2 * sigma * sigma;
        double[] inputVector = input.getVector().toArray();

        // UnitUpdateFunction uuf = new UnitUpdateFunction(hci);

        if (threadsUsed == 1) {
            updateUnitsInArea(winner, learnrate, opt1, inputVector, 0, xSize, 0, ySize, 0, zSize);
        } else {
            doneSignal = new CountDownLatch(threadsUsed);
            for (Cuboid range : ranges) {
                e.execute(new UpdaterThread(winner, learnrate, opt1, inputVector, range));
            }
            try {
                doneSignal.await(); // wait for all processes to finish
            } catch (InterruptedException ie) {
            }
        }
    }

    private void updateUnitsInArea(Unit winner, double learnrate, double opt1, double[] inputVector, Cuboid range) {
        updateUnitsInArea(winner, learnrate, opt1, inputVector, range.getStartX(), range.getEndX(), range.getStartY(),
                range.getEndY(), range.getStartZ(), range.getEndZ());
    }

    private void updateUnitsInArea(Unit winner, double learnrate, double opt1, double[] inputVector, int startX,
            int endX, int startY, int endY, int startZ, int endZ) {
        double[] unitVector = null;
        double hci;
        for (int z = startZ; z < endZ; z++) {
            for (int y = startY; y < endY; y++) {
                for (int x = startX; x < endX; x++) {
                    // Euclidean metric on output layer
                    // hci = learnrate * Math.exp((-1*Math.pow((unitDist/opt1),2)));
                    // use squared distance directly
                    hci = learnrate * Math.exp(-1 * getMapDistanceSq(winner, units[x][y][z]) / opt1);

                    // if (hci <= CUTOFF_SIGMA) { // don't update if we are outside the update range
                    // continue;
                    // }

                    unitVector = units[x][y][z].getWeightVector();

                    // inputVector = input.vector();

                    /** ** debug *** */
                    /*
                     * if ((i==winner.getXPos()) && (j==winner.getYPos())) { System.out.println(input.getLabel()+"\t"+hci+"\t"+learnrate+"\t"+sigma);
                     * }
                     */
                    /** ** end *** */
                    // System.out.println(i+"\t"+j+"\t"+hci);
                    // uuf.hci(hci);
                    // unitVector.assign(inputVector,uuf);
                    for (int ve = 0; ve < dim; ve++) {
                        if (!Double.isNaN(unitVector[ve])) { // skip updating of missing values
                            unitVector[ve] += hci * (inputVector[ve] - unitVector[ve]);
                        }
                    }
                }
            }
        }

    }

    /** Calculates and returns the number of units that are not empty, i.e. that have at least one input mapped. */
    public int getNumberOfNotEmptyUnits() throws LayerAccessException {
        if (notEmptyUnits == -1) {
            notEmptyUnits = 0;
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    Unit unit = getUnit(i, j, 0);
                    if (unit != null && unit.getNumberOfMappedInputs() != 0) {
                        notEmptyUnits += 1;
                    }
                }
            }
        }
        return notEmptyUnits;
    }

    public void setQualityMeasure(String qualityMeasureName) {
        String[] growthQM = AbstractQualityMeasure.splitNameAndMethod(qualityMeasureName);
        try {
            qualityMeasure = AbstractQualityMeasure.instantiate(growthQM[0], this, null);
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not instantiate quality measure.");
            System.exit(-1);
        }
    }

    public InputData getData() {
        return data;
    }

    public String[] getNNearestInputs(String datumlabel, int n, InputData data) throws LayerAccessException,
            MetricException {
        InputDatum datum = data.getInputDatum(datumlabel);
        Unit u = getUnitForDatum(datumlabel);

        ArrayList<InputNameDistance> items = new ArrayList<InputNameDistance>();
        for (int j = 0; j < u.getNumberOfMappedInputs(); j++) {
            String label = u.getMappedInputName(j);
            items.add(new InputNameDistance(metric.distance(data.getInputDatum(label), datum), label));
        }
        Collections.sort(items);
        // if we did find enough hits on the first unit we are done
        if (items.size() > n) {
            return getFirstNLabels(n, items);
        }

        // otherwise we add hits from the neighbouring units
        ArrayList<InputNameDistance> otherItems = new ArrayList<InputNameDistance>();
        for (Unit neighbourUnit : getNeighbouringUnits(u)) {
            for (int j = 0; j < neighbourUnit.getNumberOfMappedInputs(); j++) {
                String label = neighbourUnit.getMappedInputName(j);
                otherItems.add(new InputNameDistance(metric.distance(data.getInputDatum(label), datum), label));
            }
        }
        Collections.sort(otherItems);
        // we select the hits from the other units after the selected unit.
        items.addAll(otherItems.subList(0, n - items.size()));
        return getFirstNLabels(n, items);
    }

    public String[] getNNearestInputs(Unit u, int n) throws LayerAccessException {
        ArrayList<InputNameDistance> items = new ArrayList<InputNameDistance>();
        for (int j = 0; j < u.getNumberOfMappedInputs(); j++) {
            items.add(new InputNameDistance(u.getMappedInputDistance(j), u.getMappedInputName(j)));
        }
        Collections.sort(items);
        // if we did find enough hits on the first unit we are done
        if (items.size() > n) {
            return getFirstNLabels(n, items);
        }

        // otherwise we add hits from the neighbouring units
        ArrayList<InputNameDistance> otherItems = new ArrayList<InputNameDistance>();
        for (Unit neighbourUnit : getNeighbouringUnits(u)) {
            for (int j = 0; j < neighbourUnit.getNumberOfMappedInputs(); j++) {
                otherItems.add(new InputNameDistance(neighbourUnit.getMappedInputDistance(j),
                        neighbourUnit.getMappedInputName(j)));
            }
        }
        Collections.sort(otherItems);
        // we rank the hits from the other units after the selected unit.
        items.addAll(otherItems.subList(0, n - items.size()));
        return getFirstNLabels(n, items);
    }

    private String[] getFirstNLabels(int n, ArrayList<InputNameDistance> items) {
        String[] result = new String[Math.min(n, items.size())];
        Iterator<InputNameDistance> iterator = items.iterator();
        for (int i = 0; i < result.length; i++) {
            Object obj = iterator.next();
            if (obj instanceof InputNameDistance) {
                result[i] = ((InputNameDistance) obj).getLabel();
            } else if (obj instanceof InputDistance) {
                result[i] = ((InputDistance) obj).getInput().getLabel();

            } else {
                result[i] = obj.toString();
            }
        }
        return result;
    }

    /**
     * Get direct neighbours of the given unit. Direct neighbours are neighbours in the same column or row of the SOM,
     * thus this method returns at most six neighbours (two for each of the x, y and z dimensions).
     */
    protected ArrayList<Unit> getNeighbouringUnits(Unit u) throws LayerAccessException {
        return getNeighbouringUnits(u.getXPos(), u.getYPos(), u.getZPos());
    }

    public ArrayList<Unit> getNeighbouringUnits(int x, int y) throws LayerAccessException {
        return getNeighbouringUnits(x, y, 0);
    }

    private ArrayList<Unit> getNeighbouringUnits(int x, int y, int z) throws LayerAccessException {
        ArrayList<Unit> neighbourUnits = new ArrayList<Unit>();

        if (x > 0) {
            neighbourUnits.add(getUnit(x - 1, y, z));
        }
        if (x + 1 < getXSize()) {
            neighbourUnits.add(getUnit(x + 1, y, z));
        }
        if (y > 0) {
            neighbourUnits.add(getUnit(x, y - 1, z));
        }
        if (y + 1 < getYSize()) {
            neighbourUnits.add(getUnit(x, y + 1, z));
        }
        if (z > 0) {
            neighbourUnits.add(getUnit(x, y, z - 1));
        }
        if (z + 1 < getZSize()) {
            neighbourUnits.add(getUnit(x, y, z + 1));
        }
        return neighbourUnits;
    }

    /** Convenience method for {@link #getNeighbouringUnits(int, int, int, double)} */
    protected ArrayList<Unit> getNeighbouringUnits(Unit u, double radius) throws LayerAccessException {
        return getNeighbouringUnits(u.getXPos(), u.getYPos(), u.getZPos(), radius);
    }

    /** Convenience method for {@link #getNeighbouringUnits(int, int, int, double)} */
    public ArrayList<Unit> getNeighbouringUnits(int x, int y, double radius) throws LayerAccessException {
        return getNeighbouringUnits(x, y, 0, radius);
    }

    /**
     * Gets neighbours within a certain radius; uses {@link #getMapDistance(int, int, int, int, int, int)} for map
     * distance computation
     */
    public ArrayList<Unit> getNeighbouringUnits(int x, int y, int z, double radius) throws LayerAccessException {
        ArrayList<Unit> neighbourUnits = new ArrayList<Unit>();

        int rad = (int) Math.ceil(radius);
        int upperLimitX = Math.min(x + rad, getXSize() - 1);
        int lowerLimitX = Math.max(x - rad, 0);
        int upperLimitY = Math.min(y + rad, getYSize() - 1);
        int lowerLimitY = Math.max(y - rad, 0);
        int upperLimitZ = Math.min(z + rad, getZSize() - 1);
        int lowerLimitZ = Math.max(z - rad, lowerLimitZ = 0);

        for (int x2 = lowerLimitX; x2 <= upperLimitX; x2++) {
            for (int y2 = lowerLimitY; y2 <= upperLimitY; y2++) {
                for (int z2 = lowerLimitZ; z2 <= upperLimitZ; z2++) {
                    if (x2 != x || y2 != y || z2 != z) {
                        if (getMapDistance(x, y, z, x2, y2, z2) <= radius) {
                            neighbourUnits.add(getUnit(x2, y2, z2));
                        }
                    }
                }
            }
        }
        return neighbourUnits;
    }

    /** Computes a distance matrix between all {@link Unit}s in the {@link Layer} */
    public DoubleMatrix2D getUnitDistanceMatrix() {
        if (unitDistanceMatrix == null) {
            DistanceMetric metric = getMetric();
            int unitCount = getUnitCount();
            unitDistanceMatrix = new DenseDoubleMatrix2D(unitCount, unitCount);
            double distance = -1;

            int comparisons = unitCount * unitCount / 2 - unitCount; // symmetric distance matrix
            int stepSize = Math.max(1000, comparisons / 500);
            StdErrProgressWriter progress = new StdErrProgressWriter(comparisons, "Calculating unit distance matrix ",
                    stepSize);

            try {
                for (int index1 = 0; index1 < unitCount; index1++) {
                    for (int index2 = index1 + 1; index2 < unitCount; index2++) {
                        distance = metric.distance(getUnitForIndex(index1).getWeightVector(),
                                getUnitForIndex(index2).getWeightVector());
                        unitDistanceMatrix.setQuick(index1, index2, distance);
                        unitDistanceMatrix.setQuick(index2, index1, distance);
                        progress.progress();
                    }
                }

            } catch (MetricException e) {
                e.printStackTrace();
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            } catch (LayerAccessException e) {
                e.printStackTrace();
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            }
        }
        return unitDistanceMatrix;
    }

    public boolean isValidUnitLocation(Point pos) {
        return isValidUnitLocation(pos.x, pos.y);
    }

    public boolean isValidUnitLocation(int x, int y) {
        if (x >= 0 && x < units.length && y >= 0 && y < units[x].length) {
            return true;
        }
        return false;
    }

    @Override
    public int getNumberOfMappedInputs() {
        int count = 0;
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    count += units[i][j][k].getNumberOfMappedInputs();
                }
            }
        }
        return count;
    }

    /**
     * Implementation of general weighting as in Nünrberger/Detyniecki, 'Weighted Self-Organizing Maps: Incorporating
     * User Feedback'
     */
    public InputCorrections computeUnitFeatureWeights(InputCorrections corrections, InputData data,
            FeatureWeightMode mode) throws SOMToolboxException {
        if (data == null) {
            throw new SOMToolboxException("Input data needs to be loaded!");
        }
        try {
            metricWeighted = AbstractWeightedMetric.instantiate(metricName + "Weighted");
        } catch (Exception e) {
            throw new SOMToolboxException("Could not instantiate metric \"" + metricName + "\" for layer.");
        }

        double learnRate = 0.7;
        int neighbourhood = MathUtils.min((xSize + 1) / 2, (ySize + 1) / 2, 1);
        int maxIterations = 5000;

        // init weights
        final double[] w1 = VectorTools.generateOneVector(dim);
        Unit[] allUnits = getAllUnits();
        for (Unit unit : allUnits) {
            if (mode == FeatureWeightMode.GLOBAL) {
                unit.setFeatureWeights(w1);
            } else {
                unit.setFeatureWeights(VectorTools.generateOneVector(dim));
            }
        }
        boolean allMoved = false;
        int iteration = 0;

        // start training feature weights
        while (!allMoved && iteration < maxIterations) {
            for (InputCorrection correction : corrections.getInputCorrections()) {
                iteration++;
                final InputDatum inputDatum = data.getInputDatum(correction.getLabel());
                final Unit winner = getWinner(inputDatum, metricWeighted);

                if (winner != correction.getTargetUnit()) { // check if the computed mapping is as desired
                    System.out.println("Computed mapping not correct for " + correction.getLabel() + ", winner is "
                            + winner.printCoordinates() + ", should be "
                            + correction.getTargetUnit().printCoordinates());
                    // adapt weights for all units
                    for (InputCorrection corr : corrections.getInputCorrections()) {

                        final double[] inputVector = data.getInputDatum(corr.getLabel()).getVector().toArray();

                        // compute error vectors
                        double[] e_si = VectorTools.subtract(inputVector, corr.getSourceUnit().getWeightVector());
                        double vectorLength_si = VectorTools.vectorLength(e_si);
                        e_si = VectorTools.divide(e_si, vectorLength_si);

                        double[] e_ti = VectorTools.subtract(inputVector, corr.getTargetUnit().getWeightVector());
                        double vectorLength_ti = VectorTools.vectorLength(e_ti);
                        e_ti = VectorTools.divide(e_ti, vectorLength_ti);

                        if (mode == FeatureWeightMode.GLOBAL) {
                            double[] sub = VectorTools.subtract(e_si, e_ti);
                            double[] w_i = VectorTools.add(w1, VectorTools.multiply(sub, learnRate));
                            // adapt feature vector only of first unit, and copy them, thus they will be the same for
                            // all other units
                            allUnits[0].copyFeatureWeights(VectorTools.multiply(allUnits[0].getFeatureWeights(), w_i));

                        } else if (mode == FeatureWeightMode.LOCAL) {
                            // adapt source unit weights
                            double[] w_si = VectorTools.add(w1, VectorTools.multiply(e_si, learnRate));
                            corr.getSourceUnit().setFeatureWeights(
                                    VectorTools.multiply(corr.getSourceUnit().getFeatureWeights(), w_si));
                            // adapt target unit weights
                            double[] w_ti = VectorTools.subtract(w1, VectorTools.multiply(e_ti, learnRate));
                            corr.getTargetUnit().setFeatureWeights(
                                    VectorTools.multiply(corr.getTargetUnit().getFeatureWeights(), w_ti));

                        } else if (mode == FeatureWeightMode.GENERAL) {
                            // we can use the same code for local & general, as they only
                            for (Unit unit : allUnits) {
                                double mapDistanceToSource = getMapDistance(unit, corr.getSourceUnit());
                                double mapDistanceToTarget = getMapDistance(unit, corr.getTargetUnit());

                                // only adapt if we are in the neighbourhood of source or target unit
                                if (mapDistanceToSource < neighbourhood || mapDistanceToTarget < neighbourhood) {
                                    double g_sn = neighbourhoodFeatureWeight(mapDistanceToSource, neighbourhood);
                                    double[] g_sn_e_si = VectorTools.multiply(e_si, g_sn); // g_sn * e_sn

                                    double g_tn = neighbourhoodFeatureWeight(mapDistanceToTarget, neighbourhood);
                                    double[] g_tn_e_st = VectorTools.multiply(e_ti, g_tn); // g_tn * e_tn

                                    double[] sub = VectorTools.subtract(g_sn_e_si, g_tn_e_st); // g_sn * e_sn - g_tn *
                                    // e_tn
                                    double[] multiply = VectorTools.multiply(sub, learnRate); // learn * (g_sn*e_sn -
                                    // g_tn*e_tn)
                                    double[] w_ni = VectorTools.add(w1, multiply); // w_1 + learn * (g_sn*e_sn -
                                    // g_tn*e_tn)
                                    unit.setFeatureWeights(VectorTools.multiply(unit.getFeatureWeights(), w_ni)); // w_n
                                    // *
                                    // w_ni
                                }
                            }
                        }
                    }
                    // normalise weights to length == dim
                    if (mode == FeatureWeightMode.GLOBAL) {
                        // for the global weights we again copy the values
                        allUnits[0].copyFeatureWeights(VectorTools.normaliseByLength(allUnits[0].getFeatureWeights(),
                                dim));
                    } else {
                        for (Unit unit : allUnits) {
                            unit.setFeatureWeights(VectorTools.normaliseByLength(unit.getFeatureWeights(), dim));
                        }
                    }
                }
            }
        }

        System.out.println("Iterations: " + iteration);
        // for (Unit unit : allUnits) {
        // System.out.println(Arrays.toString(unit.getFeatureWeights()));
        // }

        // after the feature weights are calculated, identify the difference in mapping to before
        InputCorrections calculatedCorrections = new InputCorrections();
        for (int i = 0; i < data.numVectors(); i++) {
            InputDatum input = data.getInputDatum(i);
            Unit winner = getWinner(input);
            Unit winnerWeighted = getWinner(input, metricWeighted);
            if (winner != winnerWeighted) {
                calculatedCorrections.addComputedInputCorrection(winner, winnerWeighted, input.getLabel(), corrections);
            }
            if (corrections.get(input.getLabel()) != null) {
                InputCorrection corr = corrections.get(input.getLabel());
                System.out.println("details for " + input.getLabel());
                System.out.println("distance to original: " + metricWeighted.distance(input, corr.getSourceUnit()));
                System.out.println("distance to target: " + metricWeighted.distance(input, corr.getTargetUnit()));
                System.out.println("factor: " + metricWeighted.distance(input, corr.getTargetUnit())
                        / metricWeighted.distance(input, corr.getSourceUnit()));
            }
        }
        System.out.println();
        System.out.println(corrections.getInputCorrections());
        System.out.println(calculatedCorrections.getInputCorrections());
        return calculatedCorrections;
    }

    /**
     * Computes the maximum value the neighbourhood radius can take, that is in the diagonal from one corner to the
     * other. <br/>
     * Computed as Mth.sqrt(xSize^2 + ySize^2), and rounded up to the next higher decimal place.
     */
    public double maxNeighbourhoodRadius() {
        int xDist = getXSize() - 1;
        int yDist = getYSize() - 1;
        return org.apache.commons.math.util.MathUtils.round(Math.sqrt(xDist * xDist + yDist * yDist) + 0.05, 1);
    }

    public double neighbourhoodFeatureWeight(double dist, int maxDist) {
        if (dist < maxDist) {
            return 1 - dist / maxDist;
        } else {
            return 0;
        }
    }

    class UpdaterThread implements Runnable {
        private Unit winner;

        private double learnrate;

        private double opt;

        private double[] inputVector;

        private Cuboid range;

        @Override
        public void run() {
            updateUnitsInArea(winner, learnrate, opt, inputVector, range);
            doneSignal.countDown();
        }

        public UpdaterThread(Unit winner, double learnrate, double opt, double[] inputVector, Cuboid range) {
            this.winner = winner;
            this.learnrate = learnrate;
            this.opt = opt;
            this.inputVector = inputVector;
            this.range = range;
        }
    }

    public static int getNO_CPUS() {
        return THREAD_COUNT;
    }

    public static void setNO_CPUS(int no_cpus) {
        THREAD_COUNT = no_cpus;
    }

    /**
     * Flip the layer.
     */
    public void flip(Flip flip) {
        Unit[][][] out = new Unit[getXSize()][getYSize()][getZSize()];
        switch (flip) {
            case HORIZONTAL:
                for (int x = 0; x < getXSize(); x++) {
                    for (int y = 0; y < getYSize(); y++) {
                        // System.out.println("Moving " + x + "/" + y + " (" + getXSize() + "x" + getYSize() + ") => " +
                        // x + "/" + (getYSize() - y -
                        // 1));
                        out[x][y] = units[x][getYSize() - y - 1];
                    }
                }
                break;
            case VERTICAL:
                for (int x = 0; x < getXSize(); x++) {
                    out[x] = units[getXSize() - x - 1];
                    // System.out.println(ArrayUtils.toString(out));
                }
                break;
            case DEPTH:
                for (int x = 0; x < getXSize(); x++) {
                    for (int y = 0; y < getYSize(); y++) {
                        out[x][y] = units[x][y];
                        ArrayUtils.reverse(out[x][y]);
                    }
                }
                break;
        }

        units = out;

        // set new x & y size
        xSize = units.length;
        ySize = units[0].length;
        // System.out.println(printSize());

        // rewrite unit indices after rotating
        for (int x = 0; x < getXSize(); x++) {
            for (int y = 0; y < getYSize(); y++) {
                for (int z = 0; z < getZSize(); z++) {
                    units[x][y][z].setPositions(x, y, z);
                }
            }
        }
    }

    public void rotate(int rotation) throws SOMToolboxException {
        rotate(Rotation.getByDegree(rotation));
    }

    /** Clockwise rotate the layer by the given degrees. */
    public void rotate(Rotation rotation) {
        Unit[][][] out;
        // System.out.println(printSize());
        switch (rotation) {
            case ROTATE_270:
                out = new Unit[getYSize()][getXSize()][getZSize()];
                for (int x = 0; x < getXSize(); x++) {
                    for (int y = 0; y < getYSize(); y++) {
                        // System.out.println("Moving " + x + "/" + y + " (" + getXSize() + "x" + getYSize() + ") => " +
                        // (getXSize() - x - 1) + "/" +
                        // y);
                        out[y][x] = units[getXSize() - x - 1][y];
                    }
                }
                break;
            case ROTATE_180:
                out = new Unit[getXSize()][getYSize()][getZSize()];
                for (int x = 0; x < getXSize(); x++) {
                    for (int y = 0; y < getYSize(); y++) {
                        out[x][y] = units[getXSize() - x - 1][getYSize() - y - 1];
                    }
                }
                break;
            case ROTATE_90:
                out = new Unit[getYSize()][getXSize()][getZSize()];
                for (int x = 0; x < getXSize(); x++) {
                    for (int y = 0; y < getYSize(); y++) {
                        // System.out.println("Moving " + x + "/" + y + " (" + getXSize() + "x" + getYSize() + ") => " +
                        // x + "/" + (getYSize() - y -
                        // 1));
                        out[y][x] = units[x][getYSize() - y - 1];
                    }
                }
                break;
            default:
                return;
        }

        units = out;

        // set new x & y size
        xSize = units.length;
        ySize = units[0].length;
        // System.out.println(printSize());

        // rewrite unit indices after rotating
        for (int x = 0; x < getXSize(); x++) {
            for (int y = 0; y < getYSize(); y++) {
                for (int z = 0; z < getZSize(); z++) {
                    units[x][y][z].setPositions(x, y, z);
                }
            }
        }
    }

    public static void checkRotation(int rotation) throws SOMToolboxException {
        if (!ArrayUtils.contains(ROTATIONS, rotation)) {
            throw new SOMToolboxException("Invalid rotation value '" + rotation + "', allowed values are: "
                    + Arrays.toString(ROTATIONS));
        }
    }

    public String printUnitIndices() {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < getXSize(); x++) {
            for (int y = 0; y < getYSize(); y++) {
                try {
                    sb.append(getUnit(x, y) != null ? getUnit(x, y).printCoordinates() : "  -").append("\t");
                } catch (LayerAccessException e) {
                    // does not happen
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String printSize() {
        return xSize + " x " + ySize + (zSize > 1 ? " x " + zSize : "");
    }

    public void setCommonVectorLabelPrefix(String commonVectorLabelPrefix) {
        this.commonVectorLabelPrefix = commonVectorLabelPrefix;
    }

    public String getCommonVectorLabelPrefix() {
        if (commonVectorLabelPrefix == null) {
            ArrayList<String> allLabels = new ArrayList<String>();
            final Unit[] units = getAllUnits();
            for (Unit unit : units) {
                CollectionUtils.addAll(allLabels, unit.getMappedInputNames());
            }
            commonVectorLabelPrefix = allLabels.get(0);
            for (int i = 0; i < allLabels.size(); i++) {
                commonVectorLabelPrefix = StringUtils.getCommonPrefix(commonVectorLabelPrefix, allLabels.get(i));
            }
        }
        return commonVectorLabelPrefix;
    }

    @Override
    public GridLayout getGridLayout() {
        return gridLayout;
    }

    @Override
    public GridTopology getGridTopology() {
        return gridTopology;
    }

    public void setGridLayout(GridLayout gridLayout) {
        this.gridLayout = gridLayout;
    }

    public void setGridTopology(GridTopology gridTopology) {
        this.gridTopology = gridTopology;
    }

    /**
     * Clone might not be fully functional!
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {

        GrowingLayer newLayer = new GrowingLayer(this.identifier, this.superUnit, this.xSize, this.ySize, this.zSize,
                this.metricName, this.dim, this.normalized, false, 0, this.data);
        newLayer.units = this.units.clone();
        newLayer.setGridLayout(this.getGridLayout());
        newLayer.setGridTopology(this.getGridTopology());

        return newLayer;
    }

    public AdaptiveCoordinatesVirtualLayer getVirtualLayer() {
        return virtualLayer;
    }
}
