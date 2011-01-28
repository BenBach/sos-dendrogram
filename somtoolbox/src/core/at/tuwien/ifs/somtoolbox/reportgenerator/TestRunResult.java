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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.commons.lang.StringUtils;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;

import at.tuwien.ifs.commons.util.MathUtils;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.PieChartPNode;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.layers.quality.EntropyMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.IntrinsicDistance;
import at.tuwien.ifs.somtoolbox.layers.quality.InversionMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.MetricMultiScaling;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError;
import at.tuwien.ifs.somtoolbox.layers.quality.SOMDistortion;
import at.tuwien.ifs.somtoolbox.layers.quality.SammonMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.SilhouetteValue;
import at.tuwien.ifs.somtoolbox.layers.quality.SpearmanCoefficient;
import at.tuwien.ifs.somtoolbox.layers.quality.TopographicError;
import at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.labeling.LabelSOM;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;
import at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers.InputQEContainer;
import at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers.MapQEContainer;
import at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers.QMContainer;
import at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers.UnitQEContainer;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.VisualisationUtils;
import at.tuwien.ifs.somtoolbox.util.comparables.UnitDistance;
import at.tuwien.ifs.somtoolbox.visualization.EntropyVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.FlowBorderlineVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.IntrinsicDistanceVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.MappingDistortionVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.MetroMapVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import at.tuwien.ifs.somtoolbox.visualization.QuantizationErrorVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.SilhouetteVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.SmoothedCountHistograms;
import at.tuwien.ifs.somtoolbox.visualization.SmoothedDataHistograms;
import at.tuwien.ifs.somtoolbox.visualization.ThematicClassMapVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.TopographicErrorVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.TopographicProductVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.TrustwothinessVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.UMatrix;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterNode;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusteringAbortedException;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusteringTree;
import at.tuwien.ifs.somtoolbox.visualization.clustering.WardsLinkageTreeBuilderAll;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @version $Id: TestRunResult.java 3800 2010-09-13 14:03:43Z frank $
 */
public class TestRunResult {

    public QMContainer QMContainer = null;

    public DatasetInformation datasetInfo = null;

    private MySOMLibMapDescription mapDescription = null;

    private SOMProperties props = null;

    private String mapFilePath = null;

    private String unitFilePath = null;

    private String weightFilePath = null;

    private String propertyFilePath = null;

    private String dwFilePath = null;

    private HashMap<String, String> texts = new HashMap<String, String>();

    protected SharedSOMVisualisationData visData = null;

    private SOMLibFormatInputReader inputReader = null;

    private int runId = -1;

    private CommonSOMViewerStateData state = null;

    private ClusteringTree clusterTree = null;

    // private String classImage = "";
    private GrowingSOM som = null;

    private ThematicClassMapVisualizer thematic_visualizer = (ThematicClassMapVisualizer) Visualizations.getVisualizationByName(
            "Thematic Class Map").getVis();

    public EntropyVisualizer entropy_visualizer = (EntropyVisualizer) Visualizations.getVisualizationByName(
            "Entropy Visualiser").getVis();

    private SmoothedDataHistograms sdh_visualizer = (SmoothedDataHistograms) Visualizations.getVisualizationByName(
            "Smoothed Data Histograms").getVis();

    private TrustwothinessVisualizer trustworthyness_visualizer = (TrustwothinessVisualizer) Visualizations.getVisualizationByName(
            "Trustworthiness").getVis();

    private TopographicProductVisualizer topographicproduct_visualizer = (TopographicProductVisualizer) Visualizations.getVisualizationByName(
            "Topographic Product").getVis();

    private int type;

    private int[][][] classDistribution = null;

    private MetroMapVisualizer metroVis = (MetroMapVisualizer) Visualizations.getVisualizationByName("Metro Map").getVis();

    /** Constructor for an already trained SOM & State, e.g. when using the report generator from the SOM Viewer */
    public TestRunResult(DatasetInformation datasetInfo, String mapFilePath, String propertyFilePath,
            String unitFilePath, String weightFilePath, String dwFilePath, int runId, int type,
            CommonSOMViewerStateData state) {

        this.state = state;
        this.type = type;
        this.runId = runId;
        if (!state.growingSOM.isLabelled()) {
            new LabelSOM().label(state.growingSOM, state.inputDataObjects.getInputData(),
                    AbstractNetworkModel.DEFAULT_LABEL_COUNT);
        }

        // load the map description and the property file
        try {
            if (StringUtils.isNotEmpty(mapFilePath)) {
                this.mapDescription = new MySOMLibMapDescription(mapFilePath);
            }
            if (StringUtils.isNotEmpty(propertyFilePath)) {
                this.props = new SOMProperties(propertyFilePath);
            }
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot read map description file of run " + this.runId + ". Reason: " + e);
            this.mapDescription = null;
        } catch (PropertiesException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot load the properties file for run " + this.runId + ". Will try without. Reason: " + e);
            this.props = null;
        }

        this.mapFilePath = mapFilePath;
        this.unitFilePath = unitFilePath;
        this.weightFilePath = weightFilePath;
        this.propertyFilePath = propertyFilePath;
        this.dwFilePath = dwFilePath;
        this.datasetInfo = datasetInfo;
        this.som = state.growingSOM;
        this.visData = state.inputDataObjects;
    }

    /** Constructor without Trained SOM, e.g. when running stand-alone */
    public TestRunResult(DatasetInformation datasetInfo, String mapFilePath, String propertyFilePath,
            String unitFilePath, String weightFilePath, String dwFilePath, int runId, int type) {
        this.type = type;
        this.runId = runId;

        // load the map description and the property file
        try {
            if (StringUtils.isNotEmpty(mapFilePath)) {
                this.mapDescription = new MySOMLibMapDescription(mapFilePath);
            }
            if (StringUtils.isNotEmpty(propertyFilePath)) {
                this.props = new SOMProperties(propertyFilePath);
            }
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot read map description file of run " + this.runId + ". Reason: " + e);
            this.mapDescription = null;
        } catch (PropertiesException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot load the properties file for run " + this.runId + ". Will try without. Reason: " + e);
            this.props = null;
        }

        this.mapFilePath = mapFilePath;
        this.unitFilePath = unitFilePath;
        this.weightFilePath = weightFilePath;
        this.propertyFilePath = propertyFilePath;
        this.dwFilePath = dwFilePath;
        this.datasetInfo = datasetInfo;
        this.state = this.getStateObject();

    }

    public DatasetInformation getDatasetInfo() {
        return this.datasetInfo;
    }

    public String getMapFilePath() {
        return this.mapFilePath;
    }

    public String getUnitFilePath() {
        return this.unitFilePath;
    }

    public String getDWFilePath() {
        return this.dwFilePath;
    }

    public String getWeightFilePath() {
        return this.weightFilePath;
    }

    public int getRunId() {
        return this.runId;
    }

    public int getNumberOfInputsOnUnit(int x, int y) {
        if (this.classDistribution == null) {
            this.prepareClassDistribution();
        }
        return MathUtils.getSumOf(this.classDistribution[x][y]);
    }

    public boolean hasMapDescription() {
        return this.mapDescription != null;
    }

    public boolean hasPropertyFilenpath() {
        return new File(this.propertyFilePath).exists();
    }

    public String getPropertyFilePath() {
        return this.propertyFilePath;
    }

    public Object getMapProperty(String property) {
        if (this.mapDescription != null) {
            return this.mapDescription.getProperty(property);
        } else {
            return null;
        }
    }

    public boolean hasUnitOn(int x, int y) {
        return this.getGrowingSOM().getLayer().isValidUnitLocation(x, y);
    }

    public double getSigma() {
        if (this.props == null) {
            return -1;
        }
        return this.props.sigma();
    }

    public double getTau() {
        if (this.props == null) {
            return -1;
        }
        return this.props.tau();
    }

    public double getTau2() {
        return -1;
    }

    public int getNumberOfMaps() {
        return 1;
    }

    /**
     * returns the mqe of the trained SOM returns the mqe of the trained SOM, as specified by the QuantizationError
     * Object - see there for more information
     * 
     * @return the mqe of the trained map
     */
    public MapQEContainer getMapMQE() {

        QuantizationError qe = this.somQE();

        MapQEContainer qeContainer = null;
        try {
            qeContainer = new MapQEContainer(qe.getMapQuality("mqe"));
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot retrieve map mqe for run " + this.runId + ". Will work without. Reason: " + e);
            return new MapQEContainer(0);
        }
        return qeContainer;
    }

    /**
     * returns the mmqe of the trained SOM returns the mmqe of the trained SOM, as specified by the QuantizationError
     * Object - see there for more information
     * 
     * @return the mmqe of the trained map
     */
    public MapQEContainer getMapMMQE() {
        QuantizationError qe = this.somQE();

        MapQEContainer qeContainer = null;
        try {
            qeContainer = new MapQEContainer(qe.getMapQuality("mmqe"));
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot retrieve map mmqe for run " + this.runId + ". Will work without. Reason: " + e);
            return new MapQEContainer(0);
        }
        return qeContainer;
    }

    /**
     * returns the smallest quantization error of all units where at least one input vector is mapped on
     * 
     * @return object containing the smallest QE and some information about the unit
     */
    public UnitQEContainer getMinUnitQE() {
        GrowingLayer layer = this.getGrowingSOM().getLayer();
        QuantizationError qe = this.somQE();
        double[][] errors;
        double minError;
        Vector<Integer> argminx = new Vector<Integer>();
        Vector<Integer> argminy = new Vector<Integer>();

        try {
            errors = qe.getUnitQualities("qe");
            minError = errors[0][0];
            argminx.add(new Integer(0));
            argminy.add(new Integer(0));
            for (int i = 0; i < errors.length; i++) {
                for (int j = 0; j < errors[i].length; j++) {
                    // look whether the qe is smaller
                    if (errors[i][j] < minError && layer.hasMappedInput(i, j)) {
                        // new minimum
                        minError = errors[i][j];
                        argminx.removeAllElements();
                        argminy.removeAllElements();
                        argminx.add(new Integer(i));
                        argminy.add(new Integer(j));
                    } else if (errors[i][j] == minError && layer.hasMappedInput(i, j)) {
                        // additional minimum
                        argminx.add(new Integer(i));
                        argminy.add(new Integer(j));
                    }
                }
            }

            Unit[] units = new Unit[argminx.size()];
            for (int i = 0; i < argminx.size(); i++) {
                units[i] = layer.getUnit(argminx.get(i).intValue(), argminy.get(i).intValue());
            }
            return new UnitQEContainer(units, minError);

        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot get unit qualities for run " + this.runId + ". Reason: " + e);
            return null;
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot get unit in for run " + this.runId + ". Reason: " + e);
            return null;
        }
    }

    /**
     * returns the topographic error of the map (using quality measure "TE_Map")
     * 
     * @return the topographic error of the map or -1 (in case of any problems)
     */
    public double getMapTE() {
        TopographicError toperr = new TopographicError(this.getGrowingSOM().getLayer(), this.datasetInfo.getInputData());
        try {
            return toperr.getMapQuality("TE_Map");
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot calculate qtopographic error of SOM from run " + this.runId + ". Reason: " + e);
            return -1;
        }
    }

    /**
     * returns the maximal topographic error of any unit on the map (together with the unit)
     * 
     * @return an object storing information about both: the biggest topographic error of the map as well as of the unit
     *         containing this error, null in case of any error
     */
    public UnitQEContainer getMaxUnitTE() {
        GrowingLayer layer = this.getGrowingSOM().getLayer();
        TopographicError toperr = new TopographicError(layer, this.datasetInfo.getInputData());
        double[][] errors;
        double maxError;
        Vector<Integer> argmaxx = new Vector<Integer>();
        Vector<Integer> argmaxy = new Vector<Integer>();

        try {
            errors = toperr.getUnitQualities("TE_Unit");
            maxError = errors[0][0];
            argmaxx.add(new Integer(0));
            argmaxy.add(new Integer(0));
            for (int i = 0; i < errors.length; i++) {
                for (int j = 0; j < errors[i].length; j++) {
                    // look whether the te is smaller
                    if (errors[i][j] > maxError && layer.hasMappedInput(i, j)) {
                        // new maximium
                        maxError = errors[i][j];
                        argmaxx.removeAllElements();
                        argmaxy.removeAllElements();
                        argmaxx.add(new Integer(i));
                        argmaxy.add(new Integer(j));
                    } else if (errors[i][j] == maxError && layer.hasMappedInput(i, j)) {
                        // additional minimum
                        argmaxx.add(new Integer(i));
                        argmaxy.add(new Integer(j));
                    }
                }
            }

            Unit[] units = new Unit[argmaxx.size()];
            for (int i = 0; i < argmaxx.size(); i++) {
                units[i] = layer.getUnit(argmaxx.get(i).intValue(), argmaxy.get(i).intValue());
            }
            return new UnitQEContainer(units, maxError);

        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot calculate max topographic error of units on SOM from run " + this.runId + ". Reason: " + e);
            return null;
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot calculate max topographic error of units on SOM from run " + this.runId + ". Reason: " + e);
            return null;
        }
    }

    /**
     * returns the silhouette value of the map (using quality measure "silhouette")
     * 
     * @return the silhouette value of the map or -1 (in case of any problems)
     */
    public double getMapSilouette() {
        SilhouetteValue sil = new SilhouetteValue(this.getGrowingSOM().getLayer(), this.datasetInfo.getInputData());
        try {
            return sil.getMapQuality("silhouettevalue");
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot calculate silhouette value of SOM from run " + this.runId + ". Reason: " + e);
            return -1;
        }
    }

    /**
     * returns the number of units on the map having no topographic error
     * 
     * @return the number of units without topographic error, -1 in case of any error
     */
    public int getNumberOfTElessUnits() {
        Layer layer = this.getGrowingSOM().getLayer();
        TopographicError toperr = new TopographicError(layer, this.datasetInfo.getInputData());
        double[][] errors;
        int count = 0;
        try {
            errors = toperr.getUnitQualities("TE_Unit");
            for (double[] error : errors) {
                for (double element : error) {
                    if (element == 0) {
                        count++;
                    }
                }
            }
            return count;
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot calculate min topographic error of units on SOM from run " + this.runId + ". Reason: " + e);
            return -1;
        }
    }

    /**
     * returns the number of units on the map having no Silouette Value
     * 
     * @return the number of units without silouette value error, -1 in case of any error
     */
    public int getNumberOfSilouettelessUnits() {
        Layer layer = this.getGrowingSOM().getLayer();
        SilhouetteValue toperr = new SilhouetteValue(layer, this.datasetInfo.getInputData());
        double[][] errors;
        int count = 0;
        try {
            errors = toperr.getUnitQualities("silhouette");
            for (double[] error : errors) {
                for (double element : error) {
                    if (Double.isNaN(element)) {
                        count++;
                    }
                }
            }
            return count;
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot calculate min silhouette values of units on SOM from run " + this.runId + ". Reason: " + e);
            return -1;
        }
    }

    /**
     * returns the minimal topographic error (> 0!!) of any unit on the map (together with the unit)
     * 
     * @return an object storing information about both: the smallest topographic error (> 0!!) of the map as well as of
     *         the unit containing this error, null in case of an error
     */
    public UnitQEContainer getMinUnitTE() {
        GrowingLayer layer = this.getGrowingSOM().getLayer();
        TopographicError toperr = new TopographicError(layer, this.datasetInfo.getInputData());
        double[][] errors;
        double minError;
        Vector<Integer> argminx = new Vector<Integer>();
        Vector<Integer> argminy = new Vector<Integer>();

        try {
            errors = toperr.getUnitQualities("TE_Unit");
            minError = -1;
            argminx.add(new Integer(0));
            argminy.add(new Integer(0));
            for (int i = 0; i < errors.length; i++) {
                for (int j = 0; j < errors[i].length; j++) {
                    if (errors[i][j] == 0) {
                        continue;
                    }

                    // look whether the qe is smaller
                    if ((minError < 0 || errors[i][j] < minError) && layer.hasMappedInput(i, j)) {
                        // new minimum
                        minError = errors[i][j];
                        argminx.removeAllElements();
                        argminy.removeAllElements();
                        argminx.add(new Integer(i));
                        argminy.add(new Integer(j));
                    } else if (errors[i][j] == minError && layer.hasMappedInput(i, j)) {
                        // additional minimum
                        argminx.add(new Integer(i));
                        argminy.add(new Integer(j));
                    }
                }
            }

            Unit[] units = new Unit[argminx.size()];
            for (int i = 0; i < argminx.size(); i++) {
                units[i] = layer.getUnit(argminx.get(i).intValue(), argminy.get(i).intValue());
            }
            return new UnitQEContainer(units, minError);

        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot calculate min topographic error of units on SOM from run " + this.runId + ". Reason: " + e);
            return null;
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot calculate min topographic error of units on SOM from run " + this.runId + ". Reason: " + e);
            return null;
        }
    }

    /**
     * returns the minimal silhouette Value of any unit on the map (together with the unit)
     * 
     * @return an object storing information about both: the smallest silhouette error (> 0!!) of the map as well as of
     *         the unit containing this error, null in case of an error
     */
    public UnitQEContainer getMinUnitSilouette() {
        GrowingLayer layer = this.getGrowingSOM().getLayer();
        SilhouetteValue sil = new SilhouetteValue(layer, this.datasetInfo.getInputData());
        double[][] errors;
        double minError;
        Vector<Integer> argminx = new Vector<Integer>();
        Vector<Integer> argminy = new Vector<Integer>();

        try {
            errors = sil.getUnitQualities("silhouette");
            minError = Double.MAX_VALUE;
            argminx.add(new Integer(0));
            argminy.add(new Integer(0));
            for (int i = 0; i < errors.length; i++) {
                for (int j = 0; j < errors[i].length; j++) {
                    if (errors[i][j] == 0) {
                        continue;
                    }

                    // look whether the qe is smaller
                    if (errors[i][j] < minError && layer.hasMappedInput(i, j)) {
                        // new minimum
                        minError = errors[i][j];
                        argminx.removeAllElements();
                        argminy.removeAllElements();
                        argminx.add(new Integer(i));
                        argminy.add(new Integer(j));
                    } else if (errors[i][j] == minError && layer.hasMappedInput(i, j)) {
                        // additional minimum
                        argminx.add(new Integer(i));
                        argminy.add(new Integer(j));
                    }
                }
            }

            Unit[] units = new Unit[argminx.size()];
            for (int i = 0; i < argminx.size(); i++) {
                units[i] = layer.getUnit(argminx.get(i).intValue(), argminy.get(i).intValue());
            }
            return new UnitQEContainer(units, minError);

        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot calculate min silhouette value of units on SOM from run " + this.runId + ". Reason: " + e);
            return null;
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot calculate min silhouette value of units on SOM from run " + this.runId + ". Reason: " + e);
            return null;
        }
    }

    /**
     * Returns the quantization error of the unit at the specified coordinates That is, for all inputs mapped to this
     * unit, the distance between the weigh vector of the input item and the unit is summed and returned
     * 
     * @param x the x-coordinate of the unit on the map
     * @param y the y-coordinate of the unit on the map
     * @return the qe of the unit at the specified coordinates, -1 in case of an error
     */
    public double getQEForUnit(int x, int y) {
        try {
            return this.somQE().getUnitQualities("qe")[x][y];
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot retrieve quantization error for unit " + x + "," + y + " in run " + this.runId
                            + ". Reason: " + e);
            return -1;
        }
    }

    /**
     * returns the biggest quantization error of all units where at least one input vector is mapped on
     * 
     * @return an object containing both, the value of the qe as well as information about the unit this qe is located
     *         on, null in case of any error
     */
    public UnitQEContainer getMaxUnitQE() {
        GrowingLayer layer = this.getGrowingSOM().getLayer();
        QuantizationError qe = this.somQE();
        double[][] errors;
        double maxError;
        Vector<Integer> argmaxx = new Vector<Integer>();
        Vector<Integer> argmaxy = new Vector<Integer>();

        try {
            errors = qe.getUnitQualities("qe");
            maxError = errors[0][0];
            argmaxx.add(new Integer(0));
            argmaxy.add(new Integer(0));
            for (int i = 0; i < errors.length; i++) {
                for (int j = 0; j < errors[i].length; j++) {
                    // look whether the qe is smaller
                    if (errors[i][j] > maxError && layer.hasMappedInput(i, j)) {
                        // new minimum
                        maxError = errors[i][j];
                        argmaxx.removeAllElements();
                        argmaxy.removeAllElements();
                        argmaxx.add(new Integer(i));
                        argmaxy.add(new Integer(j));
                    } else if (errors[i][j] == maxError && layer.hasMappedInput(i, j)) {
                        // additional minimum
                        argmaxx.add(new Integer(i));
                        argmaxy.add(new Integer(j));
                    }
                }
            }

            Unit[] units = new Unit[argmaxx.size()];
            for (int i = 0; i < argmaxx.size(); i++) {
                units[i] = layer.getUnit(argmaxx.get(i).intValue(), argmaxy.get(i).intValue());
            }
            return new UnitQEContainer(units, maxError);

        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot get unit qualities for run " + this.runId + ". Reason: " + e);
            return null;
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot get unit in for run " + this.runId + ". Reason: " + e);
            return null;
        }
    }

    /**
     * returns the smallest mean quantization error of all units where at least one input vector is mapped on. (that is:
     * sum of distances / number of vectors)
     * 
     * @return an object containing the value of the smallest mqe of all units on the map as well as information about
     *         the unit the error is located on. null in case of any problem.
     */
    public UnitQEContainer getMinUnitMQE() {
        GrowingLayer layer = this.getGrowingSOM().getLayer();
        QuantizationError qe = this.somQE();
        double[][] errors;
        double minError;
        Vector<Integer> argminx = new Vector<Integer>();
        Vector<Integer> argminy = new Vector<Integer>();

        try {
            errors = qe.getUnitQualities("mqe");
            minError = errors[0][0];
            argminx.add(new Integer(0));
            argminy.add(new Integer(0));
            for (int i = 0; i < errors.length; i++) {
                for (int j = 0; j < errors[i].length; j++) {
                    // look whether the qe is smaller
                    if (errors[i][j] < minError && layer.hasMappedInput(i, j)) {
                        // new minimum
                        minError = errors[i][j];
                        argminx.removeAllElements();
                        argminy.removeAllElements();
                        argminx.add(new Integer(i));
                        argminy.add(new Integer(j));
                    } else if (errors[i][j] == minError && layer.hasMappedInput(i, j)) {
                        // additional minimum
                        argminx.add(new Integer(i));
                        argminy.add(new Integer(j));
                    }
                }
            }

            Unit[] units = new Unit[argminx.size()];
            for (int i = 0; i < argminx.size(); i++) {
                units[i] = layer.getUnit(argminx.get(i).intValue(), argminy.get(i).intValue());
            }
            return new UnitQEContainer(units, minError);

        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot get unit qualities for run " + this.runId + ". Reason: " + e);
            return null;
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot get unit in for run " + this.runId + ". Reason: " + e);
            return null;
        }
    }

    /**
     * returs the mean quantization error for the unit with the specified coordinates the mqe of a unit = sum or qe
     * errors / number of input items mapped to this unit. This value is returned
     * 
     * @param x the x-coordinate of the unit on the map
     * @param y the y-coordinate of the unit on the map
     * @return the mqe of the specified unit or -1 in case of any error.
     */
    public double getMQEForUnit(int x, int y) {
        try {
            return this.somQE().getUnitQualities("mqe")[x][y];
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot retrieve mean quantization error for unit " + x + "," + y + " in run " + this.runId
                            + ". Reason: " + e);
            return -1;
        }
    }

    /**
     * returns the biggest mean quantization error of all units where at least one input vector is mapped on. (that is:
     * sum of distances / number of vectors)
     * 
     * @return an object containing the value of the biggest mqe as well as information about the unit this error is
     *         located on, null in case of any problems
     */
    public UnitQEContainer getMaxUnitMQE() {
        GrowingLayer layer = this.getGrowingSOM().getLayer();
        QuantizationError qe = this.somQE();
        double[][] errors;
        double maxError;
        Vector<Integer> argmaxx = new Vector<Integer>();
        Vector<Integer> argmaxy = new Vector<Integer>();

        try {
            errors = qe.getUnitQualities("mqe");
            maxError = errors[0][0];
            argmaxx.add(new Integer(0));
            argmaxy.add(new Integer(0));
            for (int i = 0; i < errors.length; i++) {
                for (int j = 0; j < errors[i].length; j++) {
                    // look whether the qe is smaller
                    if (errors[i][j] > maxError && layer.hasMappedInput(i, j)) {
                        // new minimum
                        maxError = errors[i][j];
                        argmaxx.removeAllElements();
                        argmaxy.removeAllElements();
                        argmaxx.add(new Integer(i));
                        argmaxy.add(new Integer(j));
                    } else if (errors[i][j] == maxError && layer.hasMappedInput(i, j)) {
                        // additional minimum
                        argmaxx.add(new Integer(i));
                        argmaxy.add(new Integer(j));
                    }
                }
            }

            Unit[] units = new Unit[argmaxx.size()];
            for (int i = 0; i < argmaxx.size(); i++) {
                units[i] = layer.getUnit(argmaxx.get(i).intValue(), argmaxy.get(i).intValue());
            }
            return new UnitQEContainer(units, maxError);

        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot get unit qualities for run " + this.runId + ". Reason: " + e);
            return null;
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot get unit in for run " + this.runId + ". Reason: " + e);
            return null;
        }
    }

    /**
     * returns the unit the specified input item is mapped to
     * 
     * @param input the input item, for which the unit it is mapped to shall be returned
     * @return the unit onto which the given input item is mapped
     */
    public Unit getUnitMappedTo(InputDatum input) {
        GrowingSOM som = this.getGrowingSOM();
        GrowingLayer layer = som.getLayer();
        Unit mappedTo = layer.getUnitForDatum(input.getLabel());
        return mappedTo;
    }

    /**
     * calculates the qe for the specified input vector Calculates the quantization error of the input vector specified
     * by its name, that is the distance of the given input vector to the unit it is mapped to.
     * 
     * @param name the name specifying the input vector
     * @return an object containing the value of the qe of the specified input
     */
    public InputQEContainer getInputMappedQE(String name) {

        double distance = 0;
        GrowingSOM som = this.getGrowingSOM();
        GrowingLayer layer = som.getLayer();
        Unit mappedTo = layer.getUnitForDatum(name);
        if (mappedTo == null) {
            return null;
        }

        distance = mappedTo.getMappedInputDistance(mappedTo.getInputIndex(name));
        InputQEContainer qeContainer = new InputQEContainer();
        qeContainer.addMapUnit(mappedTo);
        qeContainer.setQE(distance);
        qeContainer.addInput(this.datasetInfo.getInputDatum(name));

        return qeContainer;
    }

    /**
     * mappes the vector to the current SOM and returns x and y coordinates of the winning unit. searches the winning
     * unit on the SOM for the given weight vector (that ist the SOM whose weight vector is closest to the given vecotr
     * and returns the coordinates of this unit.
     * 
     * @param vector the vector of the item that shall be mapped to the unit
     * @return and array containing x and y coordinates of the winning unit
     */
    public int[] getMappedUnit(double[] vector) {
        GrowingLayer layer = this.getGrowingSOM().getLayer();

        InputDatum classMean = new InputDatum("class_mean_vector", new DenseDoubleMatrix1D(vector));
        Unit winner = layer.getWinner(classMean);

        return new int[] { winner.getXPos(), winner.getYPos() };
    }

    /**
     * returns the mean x and y coordinates (and the standard derivation) of a class the following is done: for all
     * input elements of the given class, the sum of the x coordinates of the unit the item is mapped onto is build, and
     * then divided by the number of input items in this class. (the same for y) That is, the mean unit in terms of the
     * units the item are really mapped onto is calculated. (meanx = sum(x)/n, where n is the number of input items in
     * the class, and the sum is over all input items, and x is the x-coordinate of the unit the item is mapped onto -
     * equivalent for y). And the standard derivation of this value is also calculated
     * 
     * @param classId the id of the class for which the mean unit shall be calculated
     * @return the mean x and y coordinates (at index 0=x, 1=y) as well as their standard derivation (2=x, 3=y)
     */
    public int[] getClassMeanUnit(int classId) {
        GrowingLayer layer = this.getGrowingSOM().getLayer();
        double[] mean = new double[4];
        for (int i = 0; i < mean.length; i++) {
            mean[i] = 0;
        }

        // get the labels of the input vectors belonging to this class
        String[] ins = this.datasetInfo.getInputLabelsofClass(classId);
        int n = ins.length;

        // mean
        for (String in : ins) {
            Unit temp = layer.getUnitForDatum(in);
            if (temp.getXPos() > 20) {
                System.out.println("yepp- " + in);
                System.exit(0);
            }
            mean[0] += (double) temp.getXPos() / (double) n;
            mean[1] += (double) temp.getYPos() / (double) n;
        }

        // variance/radius
        for (String in : ins) {
            Unit temp = layer.getUnitForDatum(in);
            mean[2] += (temp.getXPos() - mean[0]) * (temp.getXPos() - mean[0]) / (n - 1);
            mean[3] += (temp.getYPos() - mean[1]) * (temp.getYPos() - mean[1]) / (n - 1);
        }
        mean[2] = Math.sqrt(mean[2]);
        mean[3] = Math.sqrt(mean[3]);
        return new int[] { (int) Math.round(mean[0]), (int) Math.round(mean[1]), (int) Math.round(mean[2]),
                (int) Math.round(mean[3]) };
    }

    /**
     * creates an image that visualizes the "mean unit" of a class, its standard derivation and the unit onto which the
     * mean weight vector of the class would be mapped Creates an image, that visualizes the units of the SOM. The
     * position of the mean unit (defined by the indexes 0,1 of the meanUnit parameter) is marked by a square, the
     * position of the unit onto which the mean weight vector would be mapped by a circle. The radius (standard
     * derivation of the mean unit) is denoted by a rectangle, going meanUnit[2] units to the left and right of the mean
     * unit, and meanUnit[3] items to the top and bottom the image is named run_RUNID_classCenter_CLASSID.jpg";
     * 
     * @param meanUnit [0]: the x coordinate of the mean unit, [1]: the y coordinate, [2]: the derivation in
     *            x-direction, [3]: the derivation in y direction
     * @param mappedMeanVector [0]: the x coordinate of the unit onto which the mean weight vector of the class would be
     *            mapped, [1]: the y coordinate
     * @param classId the id of the class for which the image shall be made
     * @param outputDir the directory into which the image shall be saved. (neither checked nor created)
     */
    public boolean visualizeClassLayout(int[] meanUnit, int[] mappedMeanVector, int classId, String outputDir) {
        String visName = "class center visualization of class " + classId;
        int maxImageSize = 500; // the max. number of pixels the larger side of the image may have
        int xunits = getSomXSize(); // number of units in x direction
        int yunits = getSomYSize(); // number of units in y direction
        int maxUnits = Math.max(xunits, yunits);

        int preferredUnitSize = 10;
        int unitSize = preferredUnitSize;
        if (maxUnits * preferredUnitSize > maxImageSize) {
            // the we have to use a smaller unit size
            unitSize = maxImageSize / maxUnits;
            if (unitSize < 1) {
                unitSize = 1; // well a unit needs some space
            }
        }

        int width = unitSize * xunits + 10; // the width of the complete image
        int height = unitSize * yunits + 10; // the height of the complete image
        int x = 0; // x position of a unit in the image
        int y = 0; // y position of a unit in the image

        BufferedImage buffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = buffImage.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(Color.black);
        Rectangle rect = new Rectangle(5, 5, width - 10, height - 10); // draw the border of the SOM
        graphics.draw(rect); // ...
        rect.setBounds(0, 0, unitSize, unitSize); // prepare the Rectangle representing a unit

        graphics.setColor(Color.CYAN); // we use a not such intensive color

        // draw the units of the map - looks nicer than only the coloured cluster
        graphics.setColor(Color.black);
        for (int i = 0; i < xunits; i++) {
            for (int j = 0; j < yunits; j++) {
                x = 5 + i * unitSize;
                y = 5 + j * unitSize;

                rect.setLocation(x, y);
                graphics.draw(rect);
            }
        }

        // now draw:
        int[] rgb = this.datasetInfo.getClassColorRGB(classId);
        graphics.setColor(new Color(rgb[0], rgb[1], rgb[2]));

        // the unit where the mean vector is mapped tp
        x = 6 + mappedMeanVector[0] * unitSize;
        y = 6 + mappedMeanVector[1] * unitSize;
        graphics.fillOval(x, y, unitSize - 2, unitSize - 2);
        graphics.setColor(Color.BLACK);
        graphics.drawOval(x, y, unitSize - 2, unitSize - 2);

        graphics.setColor(new Color(rgb[0], rgb[1], rgb[2]));

        // the unit that is the mean unit
        x = 5 + meanUnit[0] * unitSize;
        y = 5 + meanUnit[1] * unitSize;
        graphics.fillRect(x, y, unitSize, unitSize);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(x, y, unitSize, unitSize);

        graphics.setStroke(new BasicStroke(2));
        graphics.setColor(new Color(rgb[0], rgb[1], rgb[2]));

        // a rectangle showing the radius of the class

        int xcorr = 0;
        int ycorr = 0;
        int w = 0;
        int h = 0;

        x = meanUnit[0] - meanUnit[2];
        y = meanUnit[1] - meanUnit[3];
        if (x < 0) {
            xcorr = x;
            x = 0;
        }
        if (y < 0) {
            ycorr = y;
            y = 0;
        }
        x = 5 + x * unitSize;
        y = 5 + y * unitSize;

        w = (1 + 2 * meanUnit[2] + xcorr) * unitSize;
        h = (1 + 2 * meanUnit[3] + ycorr) * unitSize;

        graphics.drawRect(x, y, w, h);

        String filename = "run_" + this.runId + "_classCenter_" + classId + ".jpg";
        return writeImagesAsJPG(outputDir, filename, buffImage, visName);
    }

    /**
     * calculates the qe for the specified input vector Calculates the quantization error of the input vector specified
     * by its name, that is the distance of the given input vector to the winner unit on the som. (it is not clear yet,
     * whether this unit must be the same as the unit this element is mapped onto, or whether during training the map
     * can change in a way that the mapped unit is not the winner at the end of the training. There would be a chance if
     * an input vector at the beginning of the training is lying between two units, but during the rest of the training,
     * the unit chosen moves away from the input vector, while the other one comes closer ...)
     * 
     * @param name the name specifying the input vector
     */
    public InputQEContainer getInputWinnerQE(String name) {

        GrowingSOM som = this.getGrowingSOM();
        GrowingLayer layer = som.getLayer();

        UnitDistance[] winnersAndDistances = layer.getWinnersAndDistances(this.datasetInfo.getInputDatum(name), 1);

        InputQEContainer inputQE = new InputQEContainer();
        inputQE.addMapUnit(winnersAndDistances[0].getUnit());
        inputQE.addInput(this.datasetInfo.getInputDatum(name));
        inputQE.setQE(winnersAndDistances[0].getDistance());
        return inputQE;
    }

    /**
     * returns the min qe of any input item compared with the unit it is mapped to returns the value of the smallest
     * distance between the weight vector of an input item and the unit this item is mapped to. It also returns the
     * input item(s) and unit(s) between those error arises
     * 
     * @return object containing the information specified above
     */
    public InputQEContainer getMinInputMappedQE() {

        int numbData = this.datasetInfo.getNumberOfInputVectors();
        InputQEContainer curMinContainer = this.getInputMappedQE(this.datasetInfo.getInputDatum(0).getLabel());
        InputQEContainer tempContainer = null;
        double curMin = curMinContainer.getQE();

        for (int i = 0; i < numbData; i++) {
            tempContainer = this.getInputMappedQE(this.datasetInfo.getInputDatum(i).getLabel());
            if (tempContainer.getQE() < curMin) {

                // new minimum found
                curMin = tempContainer.getQE();
                curMinContainer.clearInputs();
                curMinContainer.clearMapUnits();
                curMinContainer.setQE(tempContainer.getQE());
                curMinContainer.addInput(tempContainer.getInput(0));
                curMinContainer.addMapUnit(tempContainer.getMapUnit(0));

            } else if (tempContainer.getQE() == curMin) {

                // add to existing minima
                curMinContainer.addInput(tempContainer.getInput(0));
                curMinContainer.addMapUnit(tempContainer.getMapUnit(0));
            }
        }

        return curMinContainer;
    }

    /**
     * returns the max qe of any input item compared with the unit it is mapped to returns the value of the greatest
     * distance between the weight vector of an input item and the unit this item is mapped to. It also returns the
     * input item(s) and unit(s) between those error arises
     * 
     * @return object containing the information specified above
     */
    public InputQEContainer getMaxInputMappedQE() {

        int numbData = this.datasetInfo.getNumberOfInputVectors();
        InputQEContainer curMaxContainer = this.getInputMappedQE(this.datasetInfo.getInputDatum(0).getLabel());
        InputQEContainer tempContainer = null;
        double curMax = curMaxContainer.getQE();

        for (int i = 0; i < numbData; i++) {
            tempContainer = this.getInputMappedQE(this.datasetInfo.getInputDatum(i).getLabel());
            if (tempContainer.getQE() > curMax) {
                // new maxima found
                curMax = tempContainer.getQE();
                curMaxContainer.clearInputs();
                curMaxContainer.clearMapUnits();
                curMaxContainer.setQE(tempContainer.getQE());
                curMaxContainer.addInput(tempContainer.getInput(0));
                curMaxContainer.addMapUnit(tempContainer.getMapUnit(0));

            } else if (tempContainer.getQE() == curMax) {
                // add to existing maxima
                curMaxContainer.addInput(tempContainer.getInput(0));
                curMaxContainer.addMapUnit(tempContainer.getMapUnit(0));
            }
        }
        return curMaxContainer;
    }

    /**
     * returns the min qe of any input item compared with all units on the map returns the value of the smallest
     * distance between the weight vector of an input item and alle the weight vectors of units on the map. It also
     * returns the input item(s) and unit(s) between those error arises.
     * 
     * @return object containing the information specified above
     */
    public InputQEContainer getMinInputWinnerQE() {
        int numbData = this.datasetInfo.getNumberOfInputVectors();
        InputQEContainer curMinContainer = this.getInputWinnerQE(this.datasetInfo.getInputDatum(0).getLabel());
        InputQEContainer tempContainer = null;
        double curMin = curMinContainer.getQE();

        for (int i = 0; i < numbData; i++) {
            tempContainer = this.getInputWinnerQE(this.datasetInfo.getInputDatum(i).getLabel());
            if (tempContainer.getQE() < curMin) {
                // new minimum found
                curMin = tempContainer.getQE();
                curMinContainer.clearInputs();
                curMinContainer.clearMapUnits();
                curMinContainer.setQE(tempContainer.getQE());
                curMinContainer.addInput(tempContainer.getInput(0));
                curMinContainer.addMapUnit(tempContainer.getMapUnit(0));

            } else if (tempContainer.getQE() == curMin) {
                // add to existing minima
                curMinContainer.addInput(tempContainer.getInput(0));
                curMinContainer.addMapUnit(tempContainer.getMapUnit(0));
            }
        }

        return curMinContainer;
    }

    /**
     * returns the max qe of any input item compared with all units on the map returns the value of the greatest
     * distance between the weight vector of an input item and alle the weight vectors of units on the map. It also
     * returns the input item(s) and unit(s) between those error arises.
     * 
     * @return object containing the information specified above
     */
    public InputQEContainer getMaxInputWinnerQE() {
        int numbData = this.datasetInfo.getNumberOfInputVectors();
        InputQEContainer curMaxContainer = this.getInputWinnerQE(this.datasetInfo.getInputDatum(0).getLabel());
        InputQEContainer tempContainer = null;
        double curMax = curMaxContainer.getQE();

        for (int i = 0; i < numbData; i++) {
            tempContainer = this.getInputWinnerQE(this.datasetInfo.getInputDatum(i).getLabel());
            if (tempContainer.getQE() > curMax) {
                // new maxima found
                curMax = tempContainer.getQE();
                curMaxContainer.clearInputs();
                curMaxContainer.clearMapUnits();
                curMaxContainer.setQE(tempContainer.getQE());
                curMaxContainer.addInput(tempContainer.getInput(0));
                curMaxContainer.addMapUnit(tempContainer.getMapUnit(0));

            } else if (tempContainer.getQE() == curMax) {
                // add to existing maxima
                curMaxContainer.addInput(tempContainer.getInput(0));
                curMaxContainer.addMapUnit(tempContainer.getMapUnit(0));
            }
        }
        return curMaxContainer;
    }

    /**
     * returns the cluster node representing the cluster at the specified level
     * 
     * @param level the level for which the cluster node shall be returned
     * @param numbClusters should be 1
     * @return the cluster node representing the specfied value
     */
    public ClusterNode getClusterNodes(int level, int numbClusters) {
        ClusterNode node = null;
        this.getClusterTree().getClusteringInto(numbClusters);
        node = this.getClusterTree().findNode(level);
        return node;
    }

    /**
     * returns the QuantizationError Object for the SOM specified by this TestRunResult Creates the some according to
     * the data stored by this object, and returns the Quantization Error Object provided by the Layer (which can be
     * retrieved from the GrowingSOM)
     * 
     * @return the QuantizationError object for this SOM
     */
    private QuantizationError somQE() {
        GrowingSOM som = this.getGrowingSOM();
        Layer somLayer = som.getLayer();
        return new QuantizationError(somLayer, this.datasetInfo.getInputData());
    }

    /** returns the SOM learned within the run this object represents */
    public GrowingSOM getGrowingSOM() {
        if (this.som == null) {
            this.som = new GrowingSOM(this.getInputReader());
            this.visData = this.getVisData();
            this.som.setSharedInputObjects(this.visData);

            // label the SOM, if it's not labelled yet
            if (!som.isLabelled()) {
                new LabelSOM().label(this.som, this.datasetInfo.getInputData(), this.datasetInfo.getVectorDim());
            }

            SOMLibFormatInputReader somlib = this.getInputReader();
            Palette[] palettes = Palettes.getAvailablePalettes();
            Palette defaultPalette = Palettes.getDefaultPalette();
            int defaultPaletteIndex = Palettes.getPaletteIndex(defaultPalette);
            Visualizations.initVisualizations(visData, somlib, defaultPaletteIndex, defaultPalette, palettes);
        }
        return som;
    }

    /**
     * returns for i = 0 ... numberOfClasses onto how many units on the map have exactly items of i different classes
     * are mapped on
     * 
     * @return array of length numberOfClasses + 1, where the index denotes the number of classes, and the value the
     *         number of corresponding units
     */
    public int[] getClassPurity1() {
        if (this.classDistribution == null) {
            this.prepareClassDistribution();
        }
        int[] purity = new int[this.datasetInfo.getNumberOfClasses() + 1];
        for (int i = 0; i < purity.length; i++) {
            purity[i] = 0;
        }
        int count = 0;
        for (int[][] element : this.classDistribution) {
            for (int[] element2 : element) {
                count = 0;
                for (int element3 : element2) {
                    if (element3 > 0) {
                        count++;
                    }
                }
                purity[count]++;
            }
        }
        return purity;
    }

    /**
     * returns an textual interpretation of how good the SOM separates the different classes this is done quite simple
     * by grouping the error to:
     * <ul>
     * <li>less than 5%</li>
     * <li>less than 10%</li>
     * <li>less than 20%</li>
     * <li>less than 50%</li>
     * <li>less than 70%</li>
     * <li>less than 85%</li>
     * <li>less or equal than 100%</li>
     * 
     * @param purity an array storing at each index i how many units exist that have items of i different classes mapped
     *            onto it
     * @return a string giving an estimation of how good the SOM separates the classes
     */
    public String getClassPurity2(int[] purity) {

        String concl = "";
        double corr = (double) (MathUtils.getSumOf(purity) - purity[0] - purity[1])
                / (double) (MathUtils.getSumOf(purity) - purity[0]);
        if (corr < 0.05) {
            concl = "The SOM separates the classes very fine. (" + String.format("%.2f", corr * 100)
                    + "% of non-empty units contain more than one class)";
        } else if (corr < 0.1) {
            concl = "The SOM separates the classes quite good. (" + String.format("%.2f", corr * 100)
                    + "% of non-empty units contain more than one class)";
        } else if (corr < 0.2) {
            concl = "The SOM separates the classes good. (" + String.format("%.2f", corr * 100)
                    + "% of non-empty units contain more than one class)";
        } else if (corr < 0.5) {
            concl = "The classes are separated average by the SOM. (" + String.format("%.2f", corr * 100)
                    + "% of non-empty units contain more than one class)";
        } else if (corr < 0.7) {
            concl = "The SOM separates the classes poor. (" + String.format("%.2f", corr * 100)
                    + "% of non-empty units contain more than one class)";
        } else if (corr < 0.85) {
            concl = "The SOM separates the classes very poor. (" + String.format("%.2f", corr * 100)
                    + "% of non-empty units contain more than one class)";
        } else {
            concl = "The SOM separates the classes very poor, there's hardly any unit containing only one class. ("
                    + String.format("%.2f", corr * 100) + "% of non-empty units contain more than one class)";
        }
        double average = (double) this.getGrowingSOM().getLayer().getXSize()
                * (double) this.getGrowingSOM().getLayer().getYSize() / this.datasetInfo.getNumberOfClasses();
        concl += " This is also supported by the fact, that for " + this.getGrowingSOM().getLayer().getXSize() + " x "
                + this.getGrowingSOM().getLayer().getYSize() + " units " + "of the Map, "
                + String.format("%.2f", average) + " units are reserved in average for one Class,"
                + "with each Class having an average span of " + String.format("%.2f", this.getMeanClassSpread())
                + " units";
        return concl;
    }

    /**
     * helper function to initialize the Shared data object for visualizations
     * 
     * @return a SharedSOMVisualiationData object storing the paths to the important files
     */
    public SharedSOMVisualisationData getVisData() {
        if (this.visData == null) {
            visData = new SharedSOMVisualisationData();
            visData.setFileName(SOMVisualisationData.DATA_WINNER_MAPPING, this.dwFilePath);
            visData.setFileName(SOMVisualisationData.CLASS_INFO, this.datasetInfo.getClassInformationFilename());
            visData.setFileName(SOMVisualisationData.INPUT_VECTOR, this.datasetInfo.getInputDataFilename());
            visData.setFileName(SOMVisualisationData.TEMPLATE_VECTOR, this.datasetInfo.getTemplateFilename());
            visData.setFileName(SOMVisualisationData.LINKAGE_MAP, "");
            visData.readAvailableData();
        }
        return this.visData;
    }

    /**
     * UPDATE creates a jpg showing the class distribtion on the trained SOM The jpg is saved to the output dir with the
     * following name: run_RUNID_FILENAME
     * 
     * @param outputDir string defining the output directory where the image shall be saved (neither checked nor
     *            created)
     * @param filename the filename that shall be used for saving the image (run_RUNID_ is prefixed)
     * @param ClassID Specifies the Class to be shown, when ClasID is -1, teh whoel class dictribution image is painted
     * @return true if everything worked fine, false in case of any error (false does not give any information about
     *         whether the image exists or not)
     */
    public boolean createClassDistributionImage(String outputDir, String filename, int ClassID) {
        if (!this.datasetInfo.classInfoAvailable()) {
            return false;
        }

        int width = getSomXSize() * 10;
        int height = getSomYSize() * 10;
        // this.thematic_visualizer.setInputObjects(this.getVisData());

        String visName = "class distribution visualization";
        try {
            BufferedImage buffImage;
            if (ClassID > -1) {
                buffImage = thematic_visualizer.createVisualization(0, this.getGrowingSOM(), width, height, ClassID);
            } else {
                buffImage = thematic_visualizer.createVisualization(0, this.getGrowingSOM(), width, height);
            }
            VisualisationUtils.drawBorder(buffImage);
            return writeImagesAsJPG(outputDir, filename, buffImage, visName);
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        }
    }

    /** Creates a D-Matrix Image */
    public boolean createDMatrixImage(String outputDir, String filename, int unitScale) {
        UMatrix visualizer = new UMatrix();

        String visName = "dmatrix visualization";
        try {
            BufferedImage buffImage = visualizer.createVisualization(1, this.getGrowingSOM(),
                    getSomXSize() * unitScale, getSomYSize() * unitScale);
            return writeImagesAsJPG(outputDir, filename, buffImage, visName);
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        }
    }

    private boolean writeImagesAsJPG(String outputDir, String filename, BufferedImage buffImage, String visName) {
        outputDir = FileUtils.prepareOutputDir(outputDir);
        try {
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(1);// 1 specifies minimum compression and maximum quality
            writer.setOutput(new FileImageOutputStream(new File(outputDir + "run_" + this.runId + "_" + filename)));
            writer.write(buffImage);
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        }
        return true;
    }

    private int getSomYSize() {
        return this.getGrowingSOM().getLayer().getYSize();
    }

    private int getSomXSize() {
        return this.getGrowingSOM().getLayer().getXSize();
    }

    /**
     * creates an image visualizing the quantization error of this SOM the image is saved under the given path and name,
     * although a prefix run_runId_ is added to the image name
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created)
     * @param filename the name under which the image (together with the prefix run_[runId]_ ) is saved
     * @param variant 0 = normal, 1 =mean Quant
     * @return true if everything worked fine, false if there was any problem. False does neither assure that the image
     *         exists, nor that is does not
     */
    public boolean createQuantizationErrorImage(String outputDir, String filename, int variant) {
        QuantizationErrorVisualizer visualizer = new QuantizationErrorVisualizer();
        visualizer.setPalette(Palettes.getPaletteByName("Cartography Color, 128 Gradient (SOMToolbox 0.4.x)"));
        int unitScale = 30;
        int width = getSomXSize() * unitScale;
        int height = getSomYSize() * unitScale;
        String visName = "quantization error visualization";
        BufferedImage buffImage = visualizer.createVisualization(variant, this.getGrowingSOM(), width, height);
        VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), width, height);
        return writeImagesAsJPG(outputDir, filename, buffImage, visName);
    }

    /** Creates a Silhouette image */
    public boolean createSilouetteImage(String outputDir, String filename, int variant) {
        SilhouetteVisualizer visualizer = (SilhouetteVisualizer) Visualizations.getVisualizationByName(
                "Silhouette Value").getVis();
        visualizer.setPalette(Palettes.getPaletteByName("Cartography Color, 128 Gradient (SOMToolbox 0.4.x)"));
        int unitScale = 30;
        int width = getSomXSize() * unitScale;
        int height = getSomYSize() * unitScale;
        String visName = "Silhouette visualization";
        BufferedImage buffImage = visualizer.createVisualization(variant, this.getGrowingSOM(), width, height);
        VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), width, height);
        return writeImagesAsJPG(outputDir, filename, buffImage, visName);
    }

    /**
     * creates an image visualizing the topographic error of this SOM the image is saved under the given path and name,
     * although a prefix run_runId_ is added to the image name
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created)
     * @param filename the name under which the image (together with the prefix run_[runId]_ ) is saved
     * @return true if everything worked fine, false if there was any problem. False does neither assure that the image
     *         exists, nor that is does not
     */
    public boolean createTopographicErrorImage(String outputDir, String filename) {
        TopographicErrorVisualizer visualizer = (TopographicErrorVisualizer) Visualizations.getVisualizationByName(
                "Topographic Error neighbourhood - 4 units").getVis();
        // visualizer.setInputObjects(this.getVisData());
        int unitScale = 30;
        int width = getSomXSize() * unitScale;
        int height = getSomYSize() * unitScale;
        String visName = "topographic error visualization";
        try {
            BufferedImage buffImage = visualizer.createVisualization(0, this.getGrowingSOM(), width, height);
            VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), width, height);
            writeImagesAsJPG(outputDir, filename, buffImage, visName);
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        }
        return true;
    }

    /** Creates a Intrinsic Distance Image */
    public boolean createIntrinsicDistanceImage(String outputDir, String filename) {
        IntrinsicDistanceVisualizer visualizer = (IntrinsicDistanceVisualizer) Visualizations.getVisualizationByName(
                "Intrinsic Distance").getVis();
        int unitScale = 30;
        int width = getSomXSize() * unitScale;
        int height = getSomYSize() * unitScale;
        String visName = "Intrinisic Distance visualization";
        try {
            BufferedImage buffImage = visualizer.createVisualization(2, this.getGrowingSOM(), width, height);
            VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), width, height);
            return writeImagesAsJPG(outputDir, filename, buffImage, visName);
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        }
    }

    /**
     * create the U-D-Matrix visualization of the SOM the image is saved under the given path (which is neither checked
     * nor created) under the given name, only the prefix run_runId_ is added to the filename.
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created
     * @param filename the name under which the image (together with the prefix run_[runId]_ ) is saved
     * @param unitScale defines how many pixels shall be used to represent one unit (so one unit has size
     *            unitScalexunitScale pixels
     * @param index which visualisation to create; 0 = U Matrix, 1 = D Matrix;
     * @return true if everything worked fine, false if there was any problem. False does neither assure that the image
     *         exists, nor that is does not
     */
    public boolean createUDMatrixImage(String outputDir, String filename, int unitScale, int index) {
        int width = getSomXSize() * unitScale;
        int height = getSomYSize() * unitScale;
        UMatrix visualizer = new UMatrix();
        visualizer.setPalette(Palettes.getPaletteByName("RGB, 256 Gradient"));
        this.createPaletteImage(outputDir, "palette_17.jpg", "RGB, 256 Gradient");

        String visName = "umatrix visualization";
        try {
            BufferedImage buffImage = visualizer.createVisualization(index, this.getGrowingSOM(), width, height);
            VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), width, height);
            return writeImagesAsJPG(outputDir, filename, buffImage, visName);
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        }
    }

    /**
     * create the Flow & BorderLine visualization of the SOM the image is saved under the given path (which is neither
     * checked nor created) under the given name, only the prefix run_runId_ is added to the filename.
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created
     * @param filename the name under which the image (together with the prefix run_[runId]_ ) is saved
     * @param unitScale defines how many pixels shall be used to represent one unit (so one unit has size
     *            unitScalexunitScale pixels
     * @return true if everything worked fine, false if there was any problem. False does neither assure that the image
     *         exists, nor that is does not
     */
    public boolean createFlowBorderLineImage(String outputDir, String filename, int unitScale) {
        FlowBorderlineVisualizer visualizer = new FlowBorderlineVisualizer();
        String visName = "FlowBorderlineVisualizer visualization";
        int width = getSomXSize() * unitScale;
        int height = getSomYSize() * unitScale;
        try {
            BufferedImage buffImage = visualizer.createVisualization(2, this.getGrowingSOM(), width, height);
            VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), width, height);
            return writeImagesAsJPG(outputDir, filename, buffImage, visName);
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        }
    }

    /**
     * create the Distortion visualization of the SOM the image is saved under the given path (which is neither checked
     * nor created) under the given name, only the prefix run_runId_ is added to the filename.
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created
     * @param filename the name under which the image (together with the prefix run_[runId]_ ) is saved
     * @param unitScale defines how many pixels shall be used to represent one unit (so one unit has size
     *            unitScalexunitScale pixels
     * @param index the index
     * @return true if everything worked fine, false if there was any problem. False does neither assure that the image
     *         exists, nor that is does not
     */
    public boolean createDistortionImage(String outputDir, String filename, int unitScale, int index) {
        int width = getSomXSize() * unitScale;
        int height = getSomYSize() * unitScale;
        MappingDistortionVisualizer visualizer = new MappingDistortionVisualizer();
        String visName = "Distortion visualization";
        try {
            BufferedImage buffImage = visualizer.createVisualization(index, this.getGrowingSOM(), width, height);
            VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), width, height);
            return writeImagesAsJPG(outputDir, filename, buffImage, visName);
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        }
    }

    /**
     * create a SDH visualization of the SOM the image is saved under the given path (which is neither checked nor
     * created) under the given name, only the prefix run_runId_ is added to the filename.
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created
     * @param filename the name under which the image (together with the prefix run_[runId]_ ) is saved
     * @param unitScale defines how many pixels shall be used to represent one unit (so one unit has size
     *            unitScalexunitScale pixels
     * @param Latex used for latex output
     */
    public void createSDHImages(String outputDir, String filename, int unitScale, boolean Latex, int step) {

        int X = this.getGrowingSOM().getLayer().getXSize();
        int Y = this.getGrowingSOM().getLayer().getYSize();
        this.sdh_visualizer = new SmoothedDataHistograms();
        sdh_visualizer.setPalette(Palettes.getPaletteByName("Cartography Color, 256 Gradient, less water"));

        outputDir = FileUtils.prepareOutputDir(outputDir);
        outputDir += "SDH_pics_" + this.runId + File.separator;
        FileUtils.clearOutputDir(outputDir);

        int dim = this.getGrowingSOM().getLayer().getXSize() * this.getGrowingSOM().getLayer().getYSize();
        String visName = "SDH Visualization";

        if (Latex) {
            step = dim / 3;// create only 3 pictures in latex output
        }
        try {
            for (int smoothing = 1; smoothing < dim; smoothing += step) {
                String name = "s" + smoothing + "_" + filename;
                BufferedImage buffImage = sdh_visualizer.getVisualization(0, smoothing, this.getGrowingSOM(), X
                        * unitScale, Y * unitScale);
                VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), X * unitScale, Y * unitScale);
                writeImagesAsJPG(outputDir, name, buffImage, visName);
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").info(
                        "Created SDH image " + smoothing + " / " + (dim - 1));
            }
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
        }
    }

    /**
     * create TrustWortyNess pictures of the SOM the image is saved under the given path (which is neither checked nor
     * created) under the given name, only the prefix run_runId_ is added to the filename.
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created
     * @param filename the name under which the image (together with the prefix run_[runId]_ ) is saved
     * @param unitScale defines how many pixels shall be used to represent one unit (so one unit has size
     *            unitScalexunitScale pixels
     * @param Latex used for latex output
     */
    public void createTrustworthyNessImages(String outputDir, String filename, int unitScale, boolean Latex, int step) {
        int height = getSomYSize() * unitScale;
        int width = getSomXSize() * unitScale;
        this.trustworthyness_visualizer = new TrustwothinessVisualizer();

        outputDir = FileUtils.prepareOutputDir(outputDir);
        outputDir += "TrustworthyNessImages_pics_" + this.runId + File.separator;
        FileUtils.clearOutputDir(outputDir);
        int dim = this.getGrowingSOM().getLayer().getXSize() * this.getGrowingSOM().getLayer().getYSize();
        // get visualization
        String visName = "Trustworthy Visualization";
        if (!Latex) {
            for (int i = 1; i < dim; i = i + step) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").info(
                        "Created Trustworthyness image " + i + " / "
                                + (getGrowingSOM().getLayer().getXSize() * getGrowingSOM().getLayer().getYSize() - 1));
                BufferedImage buffImage = trustworthyness_visualizer.createTWImage(getGrowingSOM(), width, height, i);
                VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), width, height);
                String name = "tw_" + i + filename;
                writeImagesAsJPG(outputDir, name, buffImage, visName);
            }
        } else { // create only 3 pictures in latex output
            for (int i = 1; i < dim; i = i + dim / 3) {
                BufferedImage buffImage = trustworthyness_visualizer.createTWImage(this.getGrowingSOM(), width, height,
                        i);
                VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), width, height);
                String name = "tw" + i + filename;
                writeImagesAsJPG(outputDir, name, buffImage, visName);
            }
        }
    }

    /**
     * create a TopographicProduct visualization of the SOM the image is saved under the given path (which is neither
     * checked nor created) under the given name, only the prefix run_runId_ is added to the filename.
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created
     * @param filename the name under which the image (together with the prefix run_[runId]_ ) is saved
     * @param unitScale defines how many pixels shall be used to represent one unit (so one unit has size
     *            unitScalexunitScale pixels
     * @param Latex used for latex output
     */
    public void createTopographicProductImages(String outputDir, String filename, int unitScale, boolean Latex, int step) {

        int X = this.getGrowingSOM().getLayer().getXSize();
        int Y = this.getGrowingSOM().getLayer().getYSize();
        this.topographicproduct_visualizer = new TopographicProductVisualizer();

        outputDir = FileUtils.prepareOutputDir(outputDir);
        outputDir += "TopographicProduct_pics_" + this.runId + File.separator;
        FileUtils.clearOutputDir(outputDir);
        int dim = this.getGrowingSOM().getLayer().getXSize() * this.getGrowingSOM().getLayer().getYSize();
        // get visualization
        String visName = "TopographicProduct Visualization";
        if (!Latex) {
            for (int i = 1; i < dim; i = i + step) {
                String name = "tp_" + i + filename;
                BufferedImage buffImage = topographicproduct_visualizer.createTPImage(this.getGrowingSOM(),
                        getSomXSize() * unitScale, getSomYSize() * unitScale, i);
                VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), X * unitScale, Y * unitScale);
                writeImagesAsJPG(outputDir, name, buffImage, visName);
            }
        } else { // create only 3 pictures in latex output
            for (int i = 1; i < dim; i = i + dim / 3) {
                String name = "tw" + i + filename;
                BufferedImage buffImage = topographicproduct_visualizer.createTPImage(this.getGrowingSOM(),
                        getSomXSize() * unitScale, getSomYSize() * unitScale, i);
                VisualisationUtils.drawUnitGrid(buffImage, getGrowingSOM(), X * unitScale, Y * unitScale);
                writeImagesAsJPG(outputDir, name, buffImage, visName);
            }
        }
    }

    /**
     * creates a visualization of the distribution of the input data on the SOM the image is saved under the given path
     * (which is neither checked nor created) under the given name, only the prefix run_runId_ is added to the filename.
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created
     * @param filename the name under which the image (together with the prefix run_[runId]_ ) is saved
     */
    public boolean createInputDistributionImage(String outputDir, String filename) {
        SmoothedCountHistograms visualizer = new SmoothedCountHistograms();
        visualizer.setPalette(Palettes.getPaletteByName("Cartography Color, 256 Gradient, less water"));

        // we first have to find out the data distribution we want to visualize
        Unit[][] us = this.getGrowingSOM().getLayer().get2DUnits();
        double[][] hist = new double[us.length][us[0].length];
        for (int i = 0; i < us.length; i++) {
            for (int j = 0; j < us[i].length; j++) {
                hist[i][j] = us[i][j].getNumberOfMappedInputs();
            }
        }
        visualizer.setHistogram(hist);
        String visName = "visualization of the distribution of input values";
        try {
            BufferedImage buffImage = visualizer.createVisualization(0, this.getGrowingSOM(), getSomXSize() * 10,
                    getSomYSize() * 10);
            return writeImagesAsJPG(outputDir, filename, buffImage, visName);
        } catch (SOMLibFileFormatException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        }
    }

    /**
     * Creates a SingleMetroMapComponentImage with the given component index. Note that the dimensions of the Pictures
     * are fixed.
     */
    public boolean createSingleMetroMapComponentImage(String outputDir, String filename, int component) {
        String visName = "Single Component MetroMap image of input values";
        BufferedImage buffImage = metroVis.createMetromapImage(0, getGrowingSOM(),
                this.getGrowingSOM().getLayer().getXSize() * 10, this.getGrowingSOM().getLayer().getYSize() * 10,
                component);
        return writeImagesAsJPG(outputDir, "component_" + filename, buffImage, visName);
    }

    /**
     * Creates an Entropy Image, and saves it at the given Location.
     * 
     * @param ClassID if ClassID is set to -1, the Picture generated by default is that of all classes of the SOM. If
     *            ClassID is > -1, the Class with the specified ClassID-index is displayed
     */
    public boolean createEntropyImage(String outputDir, String filename, int ClassID) {
        if (!datasetInfo.classInfoAvailable()) {
            return false;
        }

        this.entropy_visualizer.setPalette(Palettes.getPaletteByName("Redscale, 32 Gradient"));

        String visName = "visualization of the entropy distribution";
        try {
            BufferedImage buffImage = entropy_visualizer.createVisualization(ClassID, this.getGrowingSOM(),
                    getSomXSize() * 10, getSomYSize() * 10);
            VisualisationUtils.drawBorder(buffImage);
            return writeImagesAsJPG(outputDir, filename, buffImage, visName);
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Problems creating the " + visName + " for run " + this.runId + ". Reason: " + e);
            return false;
        }
    }

    /**
     * Creates a Palette Image on the color palette on the specified Index<br/>
     * FIXME: move this way from this class
     */
    public void createPaletteImage(String outputDir, String filename, String palName) {
        Palette pal = Palettes.getPaletteByName(palName);
        BufferedImage img = new BufferedImage(265, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        int scaleHeight = 0;
        int scaleWidth = 0;
        Rectangle2D.Double square = new Rectangle2D.Double(0, 0, 265, 40);
        g2d.setColor(Color.white);
        g2d.fill(square);
        g2d.draw(square);
        Font font = new Font("Monospaced", Font.PLAIN, 9);
        g2d.setColor(Color.BLACK);
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics(font);
        scaleHeight = metrics.getHeight() + 2;

        scaleWidth = metrics.stringWidth("100");
        int Width = 215;
        int Height = 34;
        // System.out.println("Font: " + scaleWidth + "x" + scaleHeight);
        int werte = Width / (scaleWidth * 3);
        float step = 100f / werte;
        for (int i = 0; i <= werte; i++) {
            String text = Math.round(i * step) + "";
            int x = scaleWidth / 2 - metrics.stringWidth(text) / 2 + Math.round((Width - scaleWidth) * i * step / 100);
            int y = Height - metrics.getDescent();
            g2d.drawString(text, x, y);
        }

        int xStart = scaleWidth / 2;
        int xEnd = Width - scaleWidth / 2;
        int paletteHeight = Height - scaleHeight; // - insets.top - insets.bottom - scaleHeight;
        Color[] colors = pal.getColors();
        float step1 = (float) (xEnd - xStart) / (float) colors.length;

        for (int i = 0; i < colors.length; i++) {
            g2d.setColor(colors[i]);
            g2d.fillRect(xStart + Math.round(step1 * i), 0, Math.round(step1 + 1), paletteHeight);
        }
        try {
            ImageIO.write(img, "jpg", new File(outputDir + filename));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * calculates for each unit on the SOM, how many members of each class are mapped to it the resulting value is
     * stored in a 3-dim. array, where the first dimension denots the x position of the unit, the second the y position,
     * and the third the id of the class. There the number of input items mapped to this unit belongig to this class is
     * saved. this array is stored in this.classDistribution
     */
    private void prepareClassDistribution() {
        // x, y, class
        if (this.datasetInfo.getNumberOfClasses() > 0) {
            this.classDistribution = new int[getSomXSize()][getSomYSize()][this.datasetInfo.getNumberOfClasses()];
        } else {
            this.classDistribution = new int[getSomXSize()][getSomYSize()][1];
        }

        // initialize the array with 0:
        for (int x = 0; x < this.classDistribution.length; x++) {
            for (int y = 0; y < this.classDistribution[x].length; y++) {
                for (int c = 0; c < this.classDistribution[x][y].length; c++) {
                    this.classDistribution[x][y][c] = 0;
                }
            }
        }

        // now start counting (I couldn't find these information anywhere :(
        GrowingSOM som = this.getGrowingSOM();
        Layer layer = som.getLayer();

        for (int i = 0; i < this.datasetInfo.getNumberOfInputVectors(); i++) {

            InputDatum input = this.datasetInfo.getInputDatum(i);
            Unit unit = layer.getUnitForDatum(input.getLabel());
            if (this.datasetInfo.getNumberOfClasses() > 0) {
                this.classDistribution[unit.getXPos()][unit.getYPos()][this.datasetInfo.getClassIndexOfInput(input.getLabel())]++;
            } else {
                this.classDistribution[unit.getXPos()][unit.getYPos()][0]++;
            }
        }
    }

    /**
     * creates for each unit having at least one input item mapped to it an image of the class distribution the images
     * are stored in an subdirectory of the specified directory (which is neither checked nor created) named
     * "pieCharts_runId". All content in this directory is deleted before the images are created.
     * 
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created
     */
    public void createPieChartImages(String outputDir) {
        // calculate which classes are in which units
        this.prepareClassDistribution();

        outputDir = FileUtils.prepareOutputDir(outputDir);
        outputDir += "pieCharts_" + this.runId + File.separator;
        FileUtils.clearOutputDir(outputDir);

        int unitWidth = 60;
        int unitHeight = 60;
        final SOMLibClassInformation classInfo = som.getSharedInputObjects().getClassInfo();
        final Color[] classColors = classInfo.getClassColors();

        // ok - now that we know the class distribution within each unit, we paint the diagrams
        for (int x = 0; x < this.classDistribution.length; x++) {
            for (int y = 0; y < this.classDistribution[x].length; y++) {
                if (MathUtils.getSumOf(this.classDistribution[x][y]) > 0) {
                    String visName = "pie charts";
                    BufferedImage buffImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = buffImage.createGraphics();
                    graphics.setBackground(Color.WHITE);
                    graphics.fillRect(0, 0, buffImage.getWidth(), buffImage.getHeight());

                    try {
                        if (som.getLayer().getUnit(x, y) != null
                                && som.getLayer().getUnit(x, y).getNumberOfMappedInputs() > 0) {
                            int[] values = classInfo.computeClassDistribution(som.getLayer().getUnit(x, y).getMappedInputNames());
                            PieChartPNode.drawPlot(graphics, values, classColors, x * unitWidth, y * unitHeight,
                                    unitWidth, unitHeight);
                        }
                    } catch (LayerAccessException e) {
                        // should not happen
                        e.printStackTrace();
                    }

                    String filename = x + "_" + y + "_" + MathUtils.getSumOf(this.classDistribution[x][y]) + ".jpg";
                    writeImagesAsJPG(outputDir, filename, buffImage, visName);
                }
            }
        }
    }

    /**
     * creates and returns the clustering tree for this SOM the cluster method used is WardsLinkageTreeBuilder. If an
     * error occurs, null is probably returned.
     * 
     * @return the clustering tree for this som, or null in case of an error
     */
    public ClusteringTree getClusterTree() {
        if (this.clusterTree == null) {
            MapPNode mapPNode = new MapPNode(null, getGrowingSOM(), getGrowingSOM().getLayer(), state);
            WardsLinkageTreeBuilderAll clustering = new WardsLinkageTreeBuilderAll();
            GeneralUnitPNode[][] unitNodes = new GeneralUnitPNode[getSomXSize()][getSomYSize()];
            for (int x = 0; x < unitNodes.length; x++) {
                for (int y = 0; y < unitNodes[x].length; y++) {
                    unitNodes[x][y] = mapPNode.getUnit(x, y);
                }
            }
            try {
                this.clusterTree = clustering.createTree(unitNodes);
            } catch (ClusteringAbortedException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                        "Problems trying to create clusters for run " + this.runId + ". Reason: " + e);
                return null;
            }
        }
        return this.clusterTree;
    }

    /**
     * tries to sort all possible clusters by their "stability" that is, tries to give a measure about how good a
     * cluster is. Thereby, as simple qualification method, simply the improvement of the distance between all units
     * within a cluster and the mean vector of a cluster, divided by the number of units within a cluster compared to
     * the same value in the parent cluster is chosen. This works surprisingly well. (that is: biggest difference of
     * parentvalue - my value)
     * 
     * @param node the node in the cluster tree, whose subtree shall be scanned through
     * @param parentValue the value of the quality measure in the parent node
     * @param boundary a boundary for the level of clusters, where the calculation shall be stopped, that is a boundary
     *            for the depth the subtree of node shall be scanned
     * @param stableClusterList a sorted list of clusters, into which the clusters of this subtree shall be inserted-
     * @return an ordered list containing all the clusters specified in the last argument as well as all clusters in the
     *         subtree rooted in node "node" having level smaller than boundary
     */
    public Vector<double[]> getStableClusters2(ClusterNode node, double parentValue, int boundary,
            Vector<double[]> stableClusterList) {

        if (node.getLevel() > boundary) {
            return stableClusterList;
        }

        ClusterNode lc = node.getChild1();
        ClusterNode rc = node.getChild2();

        // berechne die Summe der Entfernungen zwischen units in cluster und mean Vector
        GeneralUnitPNode[] units = node.getNodes();

        double dist = 0;
        try {
            for (GeneralUnitPNode unit : units) {
                dist += this.getGrowingSOM().getLayer().getMetric().distance(unit.getUnit().getWeightVector(),
                        node.getMeanVector());
            }
        } catch (MetricException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Error calcuating distance in run " + this.runId + " ... Reason: " + e);
            dist = -1;
        }
        // dist = dist / (double)units.length;
        double gain = parentValue - dist;
        boolean added = false;
        for (int i = 0; i < stableClusterList.size(); i++) {
            if (gain > stableClusterList.get(i)[0]) {
                stableClusterList.add(i, new double[] { gain, node.getLevel() });
                added = true;
                break;
            }
        }
        if (!added) {
            stableClusterList.add(new double[] { gain, node.getLevel() });
        }

        // descend
        if (lc.getLevel() + 1 <= boundary) {
            // first child
            stableClusterList = this.getStableClusters2(lc, dist, boundary, stableClusterList);
        }

        if (rc.getLevel() + 1 <= boundary) {
            // second Child
            stableClusterList = this.getStableClusters2(rc, dist, boundary, stableClusterList);
        }

        return stableClusterList;
    }

    /**
     * returns for the given cluster, how many input elements of the different classses are mapped to units of this
     * cluster returns an array, that contains for each class, how many input vectors belonging to this class are mapped
     * to units of the cluster
     * 
     * @param clusterLevel the level of the cluster
     * @return the number of inputs for each cluss within the cluster
     */
    public int[] getClassDistributionInCluster(int clusterLevel) {

        int[] distribution = new int[this.datasetInfo.getNumberOfClasses()];
        int[] temp;
        Unit unit;
        if (this.classDistribution == null) {
            this.prepareClassDistribution();
        }

        ClusterNode node = this.getClusterTree().findNode(clusterLevel);
        GeneralUnitPNode[] units = node.getNodes();

        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = 0;
        }
        if (units == null) {
            return distribution;
        }
        for (GeneralUnitPNode unit2 : units) {
            unit = unit2.getUnit();
            temp = this.classDistribution[unit.getXPos()][unit.getYPos()];
            for (int j = 0; j < temp.length; j++) {
                distribution[j] += temp[j];
            }
        }
        return distribution;
    }

    /**
     * creates a visualization of the given cluster the visualization is an image of the SOM, where the units are
     * displayed, an the units belonging to the cluster are coloured. Further, the name of the cluster is written in the
     * cluster (as least tried to)<br/>
     * <br/>
     * WARNING!! nodeDepths should contain the depths of the cluster nodes. But for level i, the according depth is not
     * in nodeDepths[i-1] or [i], but at [i+1]!!!!
     * 
     * @param clusterLevel the level of the cluster to be visualized (uniquly identifies the cluster within the tree)
     * @param outputDir the path to the output dir where the image shall be saved (must exists, is neither checked nor
     *            created
     * @param nodeDepths the array containing the depths of the cluster nodes in the cluster tree. Read warning above!!!
     * @param preferredUnitSize the length a unit-side has in the ideal case (that is if there aren't too many units),
     *            that is, a unit gets dimenstions preferredUnitSizexpreferredUnitSize
     * @return true if no error was encountered during execution, false otherwise (if false is returned, nothing can be
     *         stated about whether the image exists or not)
     */
    public boolean visualizeCluster(int clusterLevel, String outputDir, int[] nodeDepths, int preferredUnitSize) {
        String visName = "cluster visualization of level " + clusterLevel;

        // retrieve the nodes belonging to the cluster
        ClusterNode node = this.getClusterTree().findNode(clusterLevel);
        GeneralUnitPNode[] units = node.getNodes();
        if (units.length == 0) {
            return false; // no unit in this cluster => it's no interesting cluster at all
        }

        int maxImageSize = 500; // the max. number of pixels the larger side of the image may have

        int xunits = getSomXSize(); // number of units in x direction
        int yunits = getSomYSize(); // number of units in y direction
        int maxUnits = Math.max(xunits, yunits);

        int unitSize = preferredUnitSize;
        if (maxUnits * preferredUnitSize > maxImageSize) {
            // the we have to use a smaller unit size
            unitSize = maxImageSize / maxUnits;
            if (unitSize == 0) {
                unitSize = 1; // well a unit needs some space
            }
        }

        int width = unitSize * xunits + 10; // the width of the complete image
        int height = unitSize * yunits + 10; // the height of the complete image
        int x = 0; // x position of a unit in the image
        int y = 0; // y position of a unit in the image
        double scale = -1;

        BufferedImage buffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = buffImage.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(Color.black);
        Rectangle rect = new Rectangle(5, 5, width - 10, height - 10); // draw the border of the SOM
        graphics.draw(rect); // ...
        rect.setBounds(0, 0, unitSize, unitSize); // prepare the Rectangle representing a unit

        graphics.setColor(Color.CYAN); // we use a not such intensive color

        if (units.length > 0) {
            scale = unitSize / units[0].getWidth();
        }

        // now we paint the units
        for (GeneralUnitPNode unit : units) {

            x = 5 + (int) (unit.getX() / unit.getWidth()) * unitSize;
            y = 5 + (int) (unit.getY() / unit.getHeight()) * unitSize;

            rect.setLocation(x, y);
            graphics.fill(rect);
        }

        // draw the units of the map - looks nicer than only the coloured cluster
        graphics.setColor(Color.black);
        for (int i = 0; i < xunits; i++) {
            for (int j = 0; j < yunits; j++) {
                x = 5 + i * unitSize;
                y = 5 + j * unitSize;

                rect.setLocation(x, y);
                graphics.draw(rect);
            }
        }

        // and finally we try to label the cluster
        Vector<String> clusterNames = this.datasetInfo.getClusterName(node,
                (int) CommonSOMViewerStateData.getInstance().clusterByValue, nodeDepths[node.getLevel() + 1]);

        graphics.setColor(Color.RED);
        Point2D.Double center = node.getCentroid();
        int twidth = 0;
        for (int j = 0; j < clusterNames.size(); j++) {
            // if(j > 0) label += "\n ";
            // label += clusterNames.get(j);
            if (clusterNames.get(j).length() > twidth) {
                twidth = clusterNames.get(j).length();
            }
        }
        x = 5 + (int) (center.getX() * scale);
        y = 5 + (int) (center.getY() * scale);
        twidth = twidth * 10;
        int theight = clusterNames.size() * 20;

        // correct x and y position of text
        if (x + twidth > unitSize * xunits + 10) {
            x = Math.max(0, unitSize * xunits + 10 - twidth);
        }
        if (y + theight > unitSize * yunits + 10) {
            y = Math.max(0, unitSize * yunits + 10 - theight);
        }

        // we only label the image if we have enough space for the text in a feasible size
        if (twidth < unitSize * xunits) {
            for (int i = 0; i < clusterNames.size(); i++) {
                graphics.drawString(clusterNames.get(i), x, y + i * 20);
            }
        }

        String filename = "clusterLevel_" + clusterLevel + "_" + preferredUnitSize + ".jpg";
        return writeImagesAsJPG(outputDir, filename, buffImage, visName);
    }

    /**
     * returns an instance of input reader the following files are handed to the reader: this.weightFilePath,
     * this.unitFilePath, this.mapFilePath the reader created is saved in this.inputReader
     * 
     * @return an instance of input reader.
     */
    protected SOMLibFormatInputReader getInputReader() {
        if (this.inputReader == null) {
            try {
                this.inputReader = new SOMLibFormatInputReader(this.weightFilePath, this.unitFilePath, this.mapFilePath);
            } catch (FileNotFoundException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").severe(
                        "Cannot find file for creating input reader: " + e);
            } catch (SOMLibFileFormatException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").severe("Cannot create input reader: " + e);
            }
        }
        return this.inputReader;
    }

    /**
     * returns an instance of CommonSOMViewerStateData, that stores basic properties for visualization of SOMs and is
     * needed for creating most of the images
     * 
     * @return and instance of this object, initialized with only default values.
     */
    private CommonSOMViewerStateData getStateObject() {
        if (this.state == null) {
            this.state = new CommonSOMViewerStateData();
            this.state.inputDataObjects = this.getVisData();
            this.state.clusterWithLabels = 2;
            this.state.labelsWithValues = false;
        }
        return this.state;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    /* UNUSED TRIES */
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* the following two functions are two different tries to determine good clusters */
    /** Quality measure for clusters that didn't work */
    public Vector<double[]> getStableClusters1(ClusterNode node, int count, int boundary,
            Vector<double[]> stableClusterList) {

        if (node.getLevel() > boundary) {
            return stableClusterList;
        }

        ClusterNode lc = node.getChild1();
        ClusterNode rc = node.getChild2();

        // calculate cluster stability
        double curStable = (double) (node.getLevel() - count - 1) / (double) node.getLevel();
        boolean added = false;
        for (int i = 0; i < stableClusterList.size(); i++) {
            if (curStable > stableClusterList.get(i)[0]) {
                stableClusterList.add(i, new double[] { curStable, node.getLevel() });
                added = true;
                break;
            }
        }
        if (!added) {
            stableClusterList.add(new double[] { curStable, node.getLevel() });
        }
        // descend
        if (lc.getLevel() + 1 <= boundary) {
            // first child
            stableClusterList = this.getStableClusters1(lc, node.getLevel(), boundary, stableClusterList);
        }

        if (rc.getLevel() + 1 <= boundary) {
            // second Child
            stableClusterList = this.getStableClusters1(rc, node.getLevel(), boundary, stableClusterList);
        }
        return stableClusterList;
    }

    /** quality measure for clusters that didn't work */
    public Vector<double[]> getStableClusters3(ClusterNode node, double parentValue, int boundary,
            Vector<double[]> stableClusterList) {

        if (node.getLevel() > boundary) {
            return stableClusterList;
        }

        ClusterNode lc = node.getChild1();
        ClusterNode rc = node.getChild2();

        // berechne die Summe der Entfernungen zwischen units in cluster und mean Vector
        GeneralUnitPNode[] units = node.getNodes();

        double dist = 0;
        try {
            for (GeneralUnitPNode unit : units) {
                dist += this.getGrowingSOM().getLayer().getMetric().distance(unit.getUnit().getWeightVector(),
                        node.getMeanVector());
            }
        } catch (MetricException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Error calcuating distance in run " + this.runId + " ... Reason: " + e);
            dist = -1;
        }
        if (units.length > 0) {
            dist = dist / units.length;
        }

        double gain = Math.abs(parentValue - dist);
        boolean added = false;
        for (int i = 0; i < stableClusterList.size(); i++) {
            if (gain > stableClusterList.get(i)[0]) {
                stableClusterList.add(i, new double[] { gain, node.getLevel() });
                added = true;
                break;
            }
        }
        if (!added) {
            stableClusterList.add(new double[] { gain, node.getLevel() });
        }

        // descend
        if (lc.getLevel() + 1 <= boundary) {
            // first child
            stableClusterList = this.getStableClusters2(lc, dist, boundary, stableClusterList);
        }

        if (rc.getLevel() + 1 <= boundary) {
            // second Child
            stableClusterList = this.getStableClusters2(rc, dist, boundary, stableClusterList);
        }

        return stableClusterList;
    }

    /** Returns an Array containing the Range in SOMUnits from all Classes, or null if no Class info is available */
    public int[] getClassRangesInUnits() {
        int[] values = new int[this.datasetInfo.getNumberOfClasses()];
        boolean[] added = new boolean[this.datasetInfo.getNumberOfClasses()]; /*
                                                                               * thjis is used to check wheter a class individual has already been
                                                                               * added to the array.Since we only want to know how many units have
                                                                               * inidividuals of a class
                                                                               */
        Arrays.fill(added, false);
        if (this.datasetInfo.classInfoAvailable()) {
            int sizeX = this.getGrowingSOM().getLayer().getXSize();
            int sizeY = this.getGrowingSOM().getLayer().getYSize();
            try {
                for (int i = 0; i < sizeX; i++) {
                    for (int j = 0; j < sizeY; j++) {
                        Unit unit = this.getGrowingSOM().getLayer().getUnit(i, j);
                        if (unit != null && unit.getNumberOfMappedInputs() > 0) {
                            for (int c = 0; c < unit.getNumberOfMappedInputs(); c++) {
                                if (added[this.datasetInfo.getClassIndexOfInput(unit.getMappedInputNames()[c])] == false) {
                                    values[this.datasetInfo.getClassIndexOfInput(unit.getMappedInputNames()[c])] += 1;
                                    added[this.datasetInfo.getClassIndexOfInput(unit.getMappedInputNames()[c])] = true;
                                }
                            }
                        }
                        Arrays.fill(added, false); /* set alle boolean values to false again, and go to the next unit */
                    }
                }
            } catch (LayerAccessException e) {
                e.printStackTrace();
            }
            return values;
        } else {
            return null;
        }
    }

    /**
     * Returns the mean Class spread of all Classes in Units;
     * 
     * @return double the mean class spread, unit measured
     */
    public double getMeanClassSpread() {
        int[] values = this.getClassRangesInUnits();
        int number = 0;
        if (values != null) {
            for (int value : values) {
                number += value;
            }
            return (double) number / values.length;
        } else {
            return number;
        }
    }

    /**
     * returns Entropy (Max/Min) error Region
     * 
     * @param mode (1 = max, 2 = min)
     */
    public double getRegionEntropyError(int mode) {
        double val = -1.0;
        switch (mode) {
            case 1:
                val = this.entropy_visualizer.getMaximumEntropy();
                break;
            case 2:
                val = this.entropy_visualizer.getMinimumEntropy();
                break;
        }
        return val;
    }

    /** Returns the maximum Entropy Error Region's classnames */
    public String[][] getMaxEErrorRegionNames() {
        return this.entropy_visualizer.getMaximumEntropyRegionNames();
    }

    /** Returns the percentage of regions with 0 entropy */
    public double getPercOfZeroEntropyRegions() {
        return this.entropy_visualizer.getPercOfZeroEntropyRegions();
    }

    /** Returns the entropy for class with index */
    public double getClassEntropy(int index) {
        return this.entropy_visualizer.ClassEntropy(index);
    }

    /** Returns the class with the maximum EntropyError */
    public int getMaximumEEClassIndex() {
        double max = 0.0;
        int counter = 0;
        for (int i = 0; i < this.datasetInfo.getNumberOfClasses(); i++) {
            if (this.getClassEntropy(i) > max) {
                max = this.getClassEntropy(i);
                counter = i;
            }
        }
        return counter;
    }

    public HashMap<String, String> getTexts() {
        return texts;
    }

    /**
     * returns every other class+hits contained in the regions with classmembers from the given index, or null if index
     * exceeds the possible number of classes
     */
    public String[][] getClassMix(int index) {
        return this.entropy_visualizer.ClassEntropyNames(index);
    }

    /** gets the Type of the Testrunresult (HTML, LATEX) */
    public int getType() {
        return this.type;

    }

    /** Returns the SOMs Dimensions */
    public int getSOMDimensions() {
        return this.getGrowingSOM().getLayer().getXSize() * this.getGrowingSOM().getLayer().getYSize();

    }

    /** returns an array containing the counting for each class for the given unit */
    public int[] getClassesForUnit(int x, int y) {
        if (this.classDistribution == null) {
            this.prepareClassDistribution();
        }
        return this.classDistribution[x][y];
    }

    /** Returns the Class Distribution array for each Unit of the Map */
    public int[][][] getClassDistribution() {
        if (this.classDistribution == null) {
            this.prepareClassDistribution();
        }
        return this.classDistribution;
    }

    public ArrayList<int[]> getAllUnitsContainingClass(int index) {
        ArrayList<int[]> UnitCoords = new ArrayList<int[]>();
        if (this.classDistribution == null) {
            this.prepareClassDistribution();
        }
        for (int x = 0; x < this.classDistribution.length; x++) {
            for (int y = 0; y < this.classDistribution[x].length; y++) {
                if (classDistribution[x][y][index] > 0) {
                    int[] temp = new int[4];
                    temp[0] = x;
                    temp[1] = y;
                    temp[2] = classDistribution[x][y][index];
                    UnitCoords.add(temp);
                }
            }
        }
        return UnitCoords;
    }

    public int getNumberofClasses() {
        return this.datasetInfo.getNumberOfClasses();
    }

    /**
     * Fills the QMContainer Object with all quality Measures that were specified in the report
     */
    public void fillQMContainer() throws QualityMeasureNotFoundException {
        this.QMContainer = new QMContainer(this);

        QuantizationError qerr = new QuantizationError(this.getGrowingSOM().getLayer(), this.datasetInfo.getInputData());
        TopographicError toperr = new TopographicError(this.getGrowingSOM().getLayer(), this.datasetInfo.getInputData());
        IntrinsicDistance idist = new IntrinsicDistance(this.getGrowingSOM().getLayer(),
                this.datasetInfo.getInputData());
        SilhouetteValue sil = new SilhouetteValue(this.getGrowingSOM().getLayer(), this.datasetInfo.getInputData());
        SOMDistortion dist = new SOMDistortion(this.getGrowingSOM().getLayer(), this.datasetInfo.getInputData());
        SpearmanCoefficient spear = new SpearmanCoefficient(this.getGrowingSOM().getLayer(),
                this.datasetInfo.getInputData());
        SammonMeasure sammon = new SammonMeasure(this.getGrowingSOM().getLayer(), this.datasetInfo.getInputData());
        MetricMultiScaling mscal = new MetricMultiScaling(this.getGrowingSOM().getLayer(),
                this.datasetInfo.getInputData());
        InversionMeasure inv = new InversionMeasure(this.getGrowingSOM().getLayer(), this.datasetInfo.getInputData());
        EntropyMeasure entropy = new EntropyMeasure(this.getGrowingSOM().getLayer(), this.datasetInfo.getInputData());

        double[][] qerrUnits = qerr.getUnitQualities("qe");
        double[][] mqerrUnits = qerr.getUnitQualities("mqe");
        double[][] idistUnits = idist.getUnitQualities("ID_Unit");
        double[][] teUnits = toperr.getUnitQualities("TE_Unit");
        double[][] silUnits = sil.getUnitQualities("silhouette");
        double[][] distUnits = dist.getUnitQualities("unitTotal");
        double[][] entropyUnits = entropy.getUnitQualities("entropy");

        ArrayList<String> wanted_QM = this.datasetInfo.getEP().getSelectedQualitMeasure();
        if (wanted_QM.contains("Quantization Error")) {
            this.QMContainer.putUnitQualities("Quantization Error", qerrUnits);
        }
        if (wanted_QM.contains("Mean Quantization Error")) {
            this.QMContainer.putUnitQualities("Mean Quantization Error", mqerrUnits);
        }
        if (wanted_QM.contains("Intrinsic Distance")) {
            this.QMContainer.putUnitQualities("Intrinsic Distance", idistUnits);
        }
        if (wanted_QM.contains("Topographic Error")) {
            this.QMContainer.putUnitQualities("Topographic Error", teUnits);
        }
        if (wanted_QM.contains("Silhouette Value")) {
            this.QMContainer.putUnitQualities("Silhouette Value", silUnits);
        }
        if (wanted_QM.contains("Distortion Values")) {
            this.QMContainer.putUnitQualities("Distortion Values", distUnits);
        }
        if (wanted_QM.contains("Entropy Error")) {
            this.QMContainer.putUnitQualities("Entropy Error", entropyUnits);
        }

        if (wanted_QM.contains("Topographic Error")) {
            this.QMContainer.putMapQualities("Topographic Error", toperr.getMapQuality("TE_Map"));
        }
        if (wanted_QM.contains("Spearman Coefficient")) {
            this.QMContainer.putMapQualities("Spearman Coefficient", spear.getMapQuality("spearmanCoefficient"));
        }
        if (wanted_QM.contains("Silhouette Value")) {
            this.QMContainer.putMapQualities("Silhouette Value", sil.getMapQuality("silhouettevalue"));
        }
        if (wanted_QM.contains("Sammon Measure")) {
            this.QMContainer.putMapQualities("Sammon Measure", sammon.getMapQuality("sammon"));
        }
        if (wanted_QM.contains("Mean Quantization Error")) {
            this.QMContainer.putMapQualities("Mean Quantization Error", qerr.getMapQuality("mmqe"));
        }
        if (wanted_QM.contains("Quantization Error")) {
            this.QMContainer.putMapQualities("Quantization Error", qerr.getMapQuality("mqe"));
        }
        if (wanted_QM.contains("Metric Multiscaling")) {
            this.QMContainer.putMapQualities("Metric Multiscaling", mscal.getMapQuality("metricmultiscaling"));
        }
        if (wanted_QM.contains("Inversion Measure")) {
            this.QMContainer.putMapQualities("Inversion Measure", inv.getMapQuality("inversion"));
        }
        if (wanted_QM.contains("Intrinsic Distance")) {
            this.QMContainer.putMapQualities("Intrinsic Distance", idist.getMapQuality("ID_Map"));
        }
        if (wanted_QM.contains("Intrinsic Distance")) {
            this.QMContainer.putMapQualities("Entropy Error", entropy.getMapQuality("entropy"));
        }
        for (int i = 0; i < this.QMContainer.UnitQualityMeasureNames.size(); i++) {
            this.QMContainer.classifyUnits(this.QMContainer.UnitQualityMeasureNames.get(i));
        }
    }

    public QMContainer getQMContainer() {
        try {
            if (this.QMContainer == null) {
                this.fillQMContainer();
            }
            return this.QMContainer;
        } catch (QualityMeasureNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Cannot get unit qualities for run " + this.runId + ". Reason: " + e);
            return null;
        }
    }

}
