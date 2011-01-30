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
package at.tuwien.ifs.somtoolbox.apps.viewer;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.PaletteEditor;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibDataInformation;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.InputCorrections;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.CreationType;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.InputCorrection;
import at.tuwien.ifs.somtoolbox.input.MnemonicSOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.LabelPNodeGenerator;
import at.tuwien.ifs.somtoolbox.util.ProgressListener;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.visualization.*;
import at.tuwien.ifs.somtoolbox.visualization.clustering.*;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Logger;

/**
 * The graphical representation of a map in the {@link SOMViewer} application. This class makes use of the <a
 * href="http://www.cs.umd.edu/hcil/jazz/" target="_blank">Piccolo framework</a> and is the top {@link PNode} for all
 * map-level visualisations. The class holds several other PNodes as children:
 * <ul>
 * <li>An array of {@link GeneralUnitPNode} - {@link MapPNode#units}</li>
 * <li>The currently selected visualisation image - {@link MapPNode#currentVisualizationImage}</li>
 * <li>The currently selected background image - {@link MapPNode#backgroundImage}</li>
 * </ul>
 * 
 * @author Michael Dittenbach
 * @version $Id: MapPNode.java 3939 2010-11-17 16:06:14Z frank $
 */
public class MapPNode extends PNode {
    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_UNIT_HEIGHT = 130;

    public static final int DEFAULT_UNIT_WIDTH = 130;

    public static final Font DEFAULT_TOOLTIP_FONT = new Font("Sans", Font.PLAIN, 10);

    public static final Font DEFAULT_TOOLTIP_FONT_UNITINFO = new Font("Courier", Font.PLAIN, 7);

    private GrowingSOM gsom = null;

    private SOMLibClassInformation classInfo = null;

    private SOMLibDataInformation dataInfo = null;

    private GeneralUnitPNode[][] units = null;

    private int UNIT_WIDTH = DEFAULT_UNIT_WIDTH; // 130

    private int UNIT_HEIGHT = DEFAULT_UNIT_HEIGHT; // 130

    /** @deprecated use {@link Visualizations} instead */
    @Deprecated
    private BackgroundImageVisualizer[] visualizations = null;

    private BackgroundImageVisualizer currentVisualization = null;

    private int currentVisualizationVariant = 0;

    // private PImage[][] visualizationImageCache = null;
    private PImage backgroundImage = null;

    protected PImage currentVisualizationImage = null;

    // private SmoothedCountHistograms overlayVis;
    private AbstractMatrixVisualizer overlayVis;

    private int overlayVis_index = 0;

    private PImage overlayVisualizationImage = null;

    // Angela
    private SortedMap<Integer, ClusterElementsStorage> currentClusteringElements = new TreeMap<Integer, ClusterElementsStorage>();

    private ClusteringTree currentClusteringTree = null;

    private PNode manualLabels = new PNode();

    private PNode unitsNode = new PNode();

    private PNode inputCorrectionsPNode = new PNode();

    private SharedSOMVisualisationData inputObjects;

    private JFrame parentFrame;

    private CommonSOMViewerStateData state;

    private BufferedImage originalBackgroundImage;

    private boolean backgroundImageVisible;

    private PPath inputLinkagePath;

    private TreeBuilder clusteringTreeBuilder;

    private PNode clusterLines;

    /**
     * Default constructors - reading of input files not yet done.
     */
    public MapPNode(JFrame parentFrame, String weightVectorFileName, String unitDescriptionFileName,
            String mapDescriptionFileName, CommonSOMViewerStateData state) throws FileNotFoundException,
            SOMLibFileFormatException {
        this(parentFrame, new MnemonicSOMLibFormatInputReader(weightVectorFileName, unitDescriptionFileName,
                mapDescriptionFileName), state, true);
    }

    /**
     * Constructor if input files have already been read.
     */
    public MapPNode(JFrame parentFrame, SOMLibFormatInputReader inputReader, CommonSOMViewerStateData state,
            boolean inizializeVis) {
        super();
        // create GrowingSOM
        gsom = new GrowingSOM(inputReader);
        GrowingLayer growingLayer = gsom.getLayer();
        init(parentFrame, inputReader, state, growingLayer, inizializeVis);
    }

    /**
     * Constructor for a already loaded GrowingLayer - can be used for visualize sublayers of a ghsom!
     * 
     * @param parentFrame The frame containing the {@link MapPNode}
     * @param gsom The SOM containing the growing layer
     * @param growingLayer The layer for which the {@link MapPNode} is generated
     * @param state The state of the SOMViewer
     */
    public MapPNode(JFrame parentFrame, GrowingSOM gsom, GrowingLayer growingLayer, CommonSOMViewerStateData state) {
        super();
        this.gsom = gsom;
        init(parentFrame, null, state, growingLayer, true);
    }

    private void init(JFrame parentFrame, SOMLibFormatInputReader inputReader, CommonSOMViewerStateData state,
            GrowingLayer growingLayer, boolean inizializeVis) {
        this.state = state;
        state.somInputReader = inputReader;

        this.parentFrame = parentFrame;

        state.mapPNode = this;

        state.growingLayer = growingLayer;
        state.growingSOM = gsom;
        inputObjects = state.inputDataObjects;
        gsom.setSharedInputObjects(inputObjects);

        classInfo = inputObjects.getClassInfo();
        dataInfo = inputObjects.getDataInfo();

        // rudi: read input mapping shifts. note: has to be done before the units are constructed & displayed
        SOMVisualisationData inputCorrectionContainer = state.inputDataObjects.getObject(SOMVisualisationData.INPUT_CORRECTIONS);
        String fileName = inputCorrectionContainer.getFileName();
        if (fileName != null && !fileName.trim().equals("")) {
            try {
                InputCorrections inputCorrections = new InputCorrections(fileName, growingLayer,
                        state.inputDataObjects.getInputData());
                inputCorrectionContainer.setData(inputCorrections);
            } catch (SOMToolboxException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            }
        } else if (inputCorrectionContainer.getData() == null) { // create an empty object
            inputCorrectionContainer.setData(new InputCorrections());
        }

        // create unit nodes
        units = new GeneralUnitPNode[growingLayer.getXSize()][growingLayer.getYSize()];
        unitsNode = new PNode();
        unitsNode.addAttribute("type", "unitsNode");
        addChild(unitsNode);
        try {
            if (inputObjects.getDataWinnerMapping() == null) {
                state.exactUnitPlacementEnabled = false;
            }
            ProgressListener progress = new StdErrProgressWriter(growingLayer.getUnitCount(), "Initialising unit ", 50);
            for (int j = 0; j < growingLayer.getYSize(); j++) {
                for (int i = 0; i < growingLayer.getXSize(); i++) {
                    if (growingLayer.getUnit(i, j) != null) { // check needed for mnemonic SOMs (might not have all
                        // units != null)
                        Unit unit = growingLayer.getUnit(i, j);
                        Point[][] locations = null;
                        if (inputObjects.getDataWinnerMapping() != null) {
                            locations = initInputLocations(unit);
                        }
                        units[i][j] = new GeneralUnitPNode(unit, state, classInfo, dataInfo, locations, UNIT_WIDTH,
                                UNIT_HEIGHT);
                        unitsNode.addChild(units[i][j]);
                    }
                    progress.progress();
                }
            }
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            System.exit(-1);
        }

        // if we have a class info on startup, we show classes
        if (classInfo != null) {
            state.setClassPiechartMode(SOMViewer.TOGGLE_PIE_CHARTS_SHOW);
            setClassColors(classInfo.getClassColors());
        }

        // create tooltip object and add event listener
        // final ToolTipPNode tooltipNode = new ToolTipPNode();
        // this.addChild(tooltipNode);
        // this.addInputEventListener(new MyMapInputEventHandler(tooltipNode, this));

        // initialize available visualizations
        if (inizializeVis) {
            Visualizations.initVisualizations(inputObjects, inputReader, this);
        }
        visualizations = Visualizations.getAvailableVisualizations();

        // Angela: add the empty nodes for manually created labels
        this.addChild(manualLabels);
        manualLabels.moveToFront();

        // add input correction arrows
        addChild(inputCorrectionsPNode);
        inputCorrectionsPNode.moveToFront();

        // rudi: display linkages between data items
        if (state.inputDataObjects.getLinkageMap() != null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Drawing constellations.");
            GeneralPath path = new GeneralPath();
            Map<String, String> linkageMap = state.inputDataObjects.getLinkageMap();
            for (String beginName : linkageMap.keySet()) {
                String endName = linkageMap.get(beginName);
                Point beginPoint = getPointLocation(beginName);
                Point endPoint = getPointLocation(endName);
                if (beginPoint != null && endPoint != null) {
                    path.append(new Line2D.Double(beginPoint, endPoint), false);
                }
            }
            inputLinkagePath = new PPath(path);
            inputLinkagePath.setStrokePaint(new Color(232, 232, 57));
            inputLinkagePath.setStroke(new BasicStroke(1.5f));
            inputLinkagePath.setPickable(false);
            if (state.displayInputLinkage) {
                addChild(inputLinkagePath);
                inputLinkagePath.moveToBack();
            }
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Drawing constellations done.");
        }

        // display input mapping shifts. note: has to be done after the units are constructed
        createInputCorrectionArrows();
    }

    public void createInputCorrectionArrows() {
        for (InputCorrection correction : inputObjects.getInputCorrections().getInputCorrections()) {
            ArrowPNode arrow = ArrowPNode.createInputCorrectionArrow(correction, InputCorrections.CreationType.MANUAL,
                    getUnit(correction.getSourceUnit()), getUnit(correction.getTargetUnit()));
            inputCorrectionsPNode.addChild(arrow);
            arrow.moveToBack();
        }
    }

    public void updateDetailsAfterMoving() {
        for (InputCorrection correction : inputObjects.getInputCorrections().getInputCorrections()) {
            getUnit(correction.getSourceUnit()).updateDetailsAfterMoving();
            getUnit(correction.getTargetUnit()).updateDetailsAfterMoving();
        }
    }

    /** Calculates the absolute position of an input on the {@link MapPNode}. */
    private Point getPointLocation(String name) {
        Unit unitBegin = gsom.getLayer().getUnitForDatum(name);
        if (unitBegin != null) {
            GeneralUnitPNode generalUnitPNode = units[unitBegin.getXPos()][unitBegin.getYPos()];
            Point offset = generalUnitPNode.getPostion();

            int inputIndexBegin = unitBegin.getInputIndex(name);
            Point point = generalUnitPNode.getLocations()[inputIndexBegin];

            return new Point(point.x + offset.x + InputPNode.WIDTH_2, point.y + offset.y + InputPNode.HEIGHT_2);
        } else {
            System.out.println("did not find datum " + name);
            return null;
        }
    }

    public Point[] getStarCoords(Unit unit, int unitSize) {
        int currentUW = UNIT_WIDTH;
        int currentUH = UNIT_HEIGHT;
        UNIT_WIDTH = unitSize;
        UNIT_HEIGHT = unitSize;

        Point[] coords = initInputLocations(unit)[1];

        UNIT_WIDTH = currentUW;
        UNIT_HEIGHT = currentUH;
        return coords;
    }

    /**
     * Computes the locations of each input vector within a specific unit
     */
    private Point[][] initInputLocations(Unit unit) {

        int size = unit.getNumberOfMappedInputs();
        // Store XY displacements in a temporary array
        double[][] locdxy = new double[size][2];
        double[][] locdxyShifted = new double[size][2];

        // Process all mapped inputs in the unit
        for (int index = 0; index < size; index++) {
            // Find vector position for this mapped input
            String label = unit.getMappedInputName(index);

            int vindex;
            try {
                vindex = inputObjects.getDataWinnerMapping().getVectPos(label);
            } catch (SOMToolboxException e1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        e1.getMessage() + " Could not draw exact point on unit!");
                // e1.printStackTrace();
                continue;
            }

            // Get winners information
            int[] xpos = inputObjects.getDataWinnerMapping().getXPos(vindex);
            int[] ypos = inputObjects.getDataWinnerMapping().getYPos(vindex);
            double[] dist = inputObjects.getDataWinnerMapping().getDists(vindex);

            // Computes displacement of the input vector against the next best winning unit.
            for (int uindex = 1; uindex <= 3; uindex++) {
                double vx = xpos[uindex] - xpos[0];
                double vy = ypos[uindex] - ypos[0];

                // Find x-axis and y-axis pull force of the winner. The pull force of 2nd unit is higher than the 3rd
                // and so on
                double force = dist[0] / (dist[uindex] * uindex);

                // Now calculate displacement. The key is: farther the unit, lesser must be the displacement.
                // Additionally the displacement is zero if units are not pulling in different directions.
                // Consider unit U1 being positioned at [2, 4] and unit U2's position being [3, 4].
                // Now clearly both units lie on the same position on y-axis and thus there is no need to compute
                // displacement for y-axis
                locdxy[index][0] += vx == 0 ? 0 : UNIT_WIDTH / 2 * force / vx;
                locdxy[index][1] += vy == 0 ? 0 : UNIT_WIDTH / 2 * force / vy;
            }

            locdxyShifted[index][0] = locdxy[index][0];
            locdxyShifted[index][1] = locdxy[index][1];

            // Compute force of replusion if the current input is ovlapping with any of the previous inputs
            // store locations into separate field to allow easy switching
            for (int rindex = 0; rindex < index; rindex++) {
                double distance = Point2D.distanceSq(locdxy[rindex][0], locdxy[rindex][1], locdxy[index][0],
                        locdxy[index][1]);
                if (distance < InputPNode.MIN_DISTANCE_SQ) {
                    // Its like other input pushing the this input towards boundary if its trying to overlap
                    locdxyShifted[index][0] *= 1.03;
                    locdxyShifted[index][1] *= 1.03;
                    // And on the other sie this input is forcing that input to leave a free space for him by moving
                    // towards center.
                    // double factor = distance/InputPNode.MIN_DISTANCE_SQ;
                    double factor = 0.8;
                    locdxyShifted[rindex][0] *= factor;
                    locdxyShifted[rindex][1] *= factor;
                }
            }
        }
        // Now calculate the actual XY position for display
        Point[][] locations = new Point[2][size];
        for (int i = 0; i < size; i++) {
            locations[0][i] = new Point((int) (UNIT_WIDTH / 2 + locdxy[i][0]), (int) (UNIT_WIDTH / 2 + locdxy[i][1]));
            locations[1][i] = new Point((int) (UNIT_WIDTH / 2 + locdxyShifted[i][0]),
                    (int) (UNIT_WIDTH / 2 + locdxyShifted[i][1]));
        }
        return locations;
    }

    /**
     * @deprecated use {@link Visualizations#singleton} instead
     */
    @Deprecated
    public BackgroundImageVisualizer[] getVisualizations() {
        return visualizations;
    }

    /** @deprecated use {@link Visualizations} instead */
    @Deprecated
    public ThematicClassMapVisualizer getThematicClassMapVisualizer() {
        for (BackgroundImageVisualizer visualization : visualizations) {
            if (visualization.getClass().equals(ThematicClassMapVisualizer.class)) {
                return (ThematicClassMapVisualizer) visualization;
            }
        }
        return null;
    }

    public BackgroundImageVisualizer getCurrentVisualization() {
        return currentVisualization;
    }

    public void setNoVisualization() {
        currentVisualization = null;
        currentVisualizationVariant = 0;
        if (currentVisualizationImage != null && currentVisualizationImage.isDescendentOf(this)) {
            this.removeChild(currentVisualizationImage);
            currentVisualizationImage = null;
            // System.out.println("currentVisualizationImage removed "+currentVisualizationImage);
        }
    }

    public void updateVisualization() {
        if (currentVisualization == null) {
            return;
        }
        try {
            PImage newVisualization = new PImage(getVisualisation());
            if (currentVisualizationImage != null && currentVisualizationImage.isDescendentOf(this)) {
                this.removeChild(currentVisualizationImage);
                currentVisualizationImage = null;
            }
            currentVisualizationImage = newVisualization;
            initCurrentVisualisation();
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
        }
    }

    private void initCurrentVisualisation() {
        currentVisualizationImage.setScale(currentVisualization.getPreferredScaleFactor());
        addChild(currentVisualizationImage);
        currentVisualizationImage.setPickable(false);
        currentVisualizationImage.moveToBack();
        if (backgroundImage != null) { // move the visualisation on top of the background image
            moveInFrontOf(backgroundImage);
            backgroundImage.moveToBack();
        } else {
        }
    }

    /**
     * Creates a histogram based visualization, which is *temporarily* placed over the current visualization
     * 
     * @param hist int[][] with histogram data, e.g. unit counts
     * @param vis_index visualization variant index (0 = flat, 1 = smoothed)
     */
    public void showHistogramOverlayVisualization(int[][] hist, int vis_index) {
        if (overlayVisualizationImage != null) {
            clearHistogramOverlayVisualization();
        }

        overlayVis_index = vis_index;
        // overlayVis = new SmoothedCountHistograms();
        overlayVis = new SearchResultHistogramVisualizer(hist);
        // overlayVis.setHistogram(hist);
        overlayVis.setPalette(state.getSOMViewer().getCurrentlySelectedPalette());
        try {
            overlayVisualizationImage = new PImage(overlayVis.getVisualization(overlayVis_index, gsom,
                    getScaledBackgroundImageWidth(overlayVis), getScaledBackgroundImageHeight(overlayVis)));
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            return;
        }
        overlayVisualizationImage.setScale(BackgroundImageVisualizer.DEFAULT_BACKGROUND_VISUALIZATION_SCALE);
        overlayVisualizationImage.setPickable(false);
        addChild(overlayVisualizationImage);
    }

    /**
     * Removes temporary histogram based visualization
     */
    public void clearHistogramOverlayVisualization() {
        if (overlayVisualizationImage != null && overlayVisualizationImage.isDescendentOf(this)) {
            removeChild(overlayVisualizationImage);
            overlayVisualizationImage = null;
            overlayVis = null;
            overlayVis_index = 0;
        }
    }

    public boolean reversePalette() throws SOMToolboxException {
        if (overlayVisualizationImage != null && overlayVisualizationImage.isDescendentOf(this)) {
            // -> overlayVis is currently active
            overlayVis.reversePalette();
            this.removeChild(overlayVisualizationImage);

            overlayVisualizationImage = new PImage(overlayVis.getVisualization(overlayVis_index, gsom,
                    getScaledBackgroundImageWidth(overlayVis), getScaledBackgroundImageHeight(overlayVis)));
            overlayVisualizationImage.setScale(BackgroundImageVisualizer.DEFAULT_BACKGROUND_VISUALIZATION_SCALE);
            overlayVisualizationImage.setPickable(false);
            addChild(overlayVisualizationImage);
        }

        if (currentVisualizationImage != null && currentVisualizationImage.isDescendentOf(this)) {
            if (currentVisualization instanceof AbstractMatrixVisualizer) {
                ((AbstractMatrixVisualizer) currentVisualization).reversePalette();
            }
            BufferedImage img = getVisualisation();
            this.removeChild(currentVisualizationImage);
            currentVisualizationImage = new PImage(img);
            initCurrentVisualisation();
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Reloads the given palette after it has been edited with the {@link PaletteEditor}, i.e. basically invalidates the
     * cache for all visualisations and then calls {@link MapPNode#changePalette(Palette)}
     */
    public boolean reloadPaletteAfterEditing(Palette palette) throws SOMToolboxException {
        // FIXME: move this method to the Visualizations class !
        for (BackgroundImageVisualizer visualization : visualizations) {
            if (visualization instanceof AbstractMatrixVisualizer) {
                ((AbstractMatrixVisualizer) visualization).invalidateCache(palette);
            }
        }
        return changePalette(palette);
    }

    public boolean changePalette(Palette palette) throws SOMToolboxException {
        boolean doneSomething = false;

        if (overlayVisualizationImage != null && overlayVisualizationImage.isDescendentOf(this)) {
            // -> overlayVis is currently active
            overlayVis.setPalette(palette);
            this.removeChild(overlayVisualizationImage);

            overlayVisualizationImage = new PImage(overlayVis.getVisualization(overlayVis_index, gsom,
                    getScaledBackgroundImageWidth(overlayVis), getScaledBackgroundImageHeight(overlayVis)));
            overlayVisualizationImage.setScale(BackgroundImageVisualizer.DEFAULT_BACKGROUND_VISUALIZATION_SCALE);
            overlayVisualizationImage.setPickable(false);
            addChild(overlayVisualizationImage);
        }

        if (currentVisualizationImage != null && currentVisualizationImage.isDescendentOf(this)) {
            if (currentVisualization instanceof AbstractMatrixVisualizer) {
                ((AbstractMatrixVisualizer) currentVisualization).setPalette(palette);
            }
            BufferedImage img = getVisualisation();
            this.removeChild(currentVisualizationImage);
            currentVisualizationImage = new PImage(img);
            initCurrentVisualisation();
            repaint();
            doneSomething = true;
        }
        // Angela: Palettes for Clustering
        if (currentClusteringTree != null) {
            // no need to set palette, it is read from state when recoloring
            // currentClusteringTree.setPalette(palette);
            currentClusteringTree.recolorTree();
            doneSomething = true;
        }
        return doneSomething;
    }

    // Angela
    /**
     * Display the specified number of clusters
     */
    public void showClusters(int count) {
        showClusters(count, false);
    }

    // Angela
    /**
     * Display the specified number of Clusters.
     * 
     * @param sticky should this level ov clustering stay visible when other levels of clustering are displayed.
     */
    public void showClusters(int count, boolean sticky) {
        if (clusteringTreeBuilder instanceof NonHierarchicalTreeBuilder) {
            // if we have a non-hierarchical tree, we need to replace the current clustering tree
            try {
                currentClusteringTree = ((NonHierarchicalTreeBuilder) clusteringTreeBuilder).getTree(units, count);
            } catch (ClusteringAbortedException e) {
                e.printStackTrace();
            }
            // currentClusteringTree.setState(state);
            currentClusteringTree.recolorTree();
        }
        if (currentClusteringTree == null) {
            setClusteringElements(null);
        } else {
            setClusteringElements(currentClusteringTree.getClusteringInto(count, sticky));
            currentClusteringTree.recolorTree();
        }
    }

    private float scaleLineWidth(int depth, int max, int min) {
        float MAX_LINE_WIDTH = 50.0f;
        float MIN_LINE_WIDTH = 1.0f;

        float lineWidth = depth - min;
        lineWidth /= max - min;
        lineWidth *= MAX_LINE_WIDTH - MIN_LINE_WIDTH;
        lineWidth += MIN_LINE_WIDTH;

        return Math.abs(lineWidth);
    }

    // Angela
    /**
     * Creates new {@link TreeBuilder}. if the builder is null, the current clustering is removed.
     */
    public void buildTree(TreeBuilder builder) throws ClusteringAbortedException {
        if (clusterLines != null)
            this.removeChild(clusterLines);

        if (builder == null) {
            currentClusteringTree = null;
        } else {
            currentClusteringTree = builder.createTree(units);
            HashMap<PNode, Integer> distanceInfo =  currentClusteringTree.getDendrogramDistanceInfo();

            int maxDepth = Integer.MIN_VALUE;
            int minDepth = Integer.MAX_VALUE;

            Integer[] depths = new Integer[0];
            depths = distanceInfo.values().toArray(depths);

            for (int i=0; i<depths.length; i++) {
                int depth1 = depths[i];

                for (int j=i+1; j<depths.length; j++) {
                    int depth2 = depths[j];
                    int depthDiff = Math.abs(depth1-depth2);

                    if (depthDiff > maxDepth) maxDepth = depthDiff;
                    if (depthDiff<minDepth) minDepth = depthDiff;
                }
            }

            clusterLines = new PNode();

            double OFFSET = 25;

            for (int col = 0; col < units.length; col++) {
                for (int row = 0; row < units[col].length; row++) {
                    if (row < units[col].length - 1) {
                        GeneralUnitPNode unit1 = units[col][row];
                        GeneralUnitPNode unit2 = units[col][row + 1];

                        float x1 = (float) (unit1.getX() + unit1.getWidth() / 2);
                        float y1 = (float) (unit1.getY() + unit1.getHeight() - OFFSET);
                        float x2 = (float) (unit2.getX() + unit2.getWidth() / 2);
                        float y2 = (float) (unit2.getY() + OFFSET);

                        PPath line = PPath.createLine(x1, y1, x2, y2);

                        int depth = currentClusteringTree.compareClusterDistanceOfPNodes(unit1, unit2);
                        float lineWidth = scaleLineWidth(depth, maxDepth, minDepth);

                        line.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
                        clusterLines.addChild(line);
                    }

                    if (col < units.length - 1) {
                        GeneralUnitPNode unit1 = units[col][row];
                        GeneralUnitPNode unit2 = units[col + 1][row];

                        float x1 = (float) (unit1.getX() + unit1.getWidth() + OFFSET);
                        float y1 = (float) (unit1.getY() + unit1.getHeight() / 2);
                        float x2 = (float) (unit2.getX() - OFFSET);
                        float y2 = (float) (unit2.getY() + unit2.getHeight() / 2);

                        PPath line = PPath.createLine(x1, y1, x2, y2);

                        int depth = currentClusteringTree.compareClusterDistanceOfPNodes(unit1, unit2);
                        float lineWidth = scaleLineWidth(depth, maxDepth, minDepth);

                        line.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
                        clusterLines.addChild(line);
                    }
                }
            }

            clusterLines.moveToFront();

            addChild(clusterLines);

            // currentClusteringTree.setState(state);
            currentClusteringTree.recolorTree();
        }
        this.clusteringTreeBuilder = builder;
    }

    /**
     * Rebuilds a clustering from the given tree. Used for deserialization.
     */
    public void buildTree(ClusteringTree tree) {
        currentClusteringTree = tree;
        setClusteringElements(tree.getAllClusteringElements());
        tree.addEditLabelEventListenerToAll();
    }

    public ClusteringTree getCurrentClusteringTree() {
        return this.currentClusteringTree;
    }

    // Angela
    /**
     * Replaces currentClusteringElements with elements and makes sure the correct stuff is displayed. Also accepts null
     * -> just removes.
     */
    public void setClusteringElements(SortedMap<Integer, ClusterElementsStorage> elements) {

        // Remove all oldborders and labels
        if (currentClusteringElements != null) {
            HashSet<PNode> tmp = new HashSet<PNode>(); // use set to avoid duplicates (labels appearing on multiple
            // layers)
            for (ClusterElementsStorage n : currentClusteringElements.values()) {
                if (n.clusterBorders != null) {
                    tmp.addAll(n.clusterBorders);
                }
                if (n.clusterLabels != null) {
                    tmp.addAll(n.clusterLabels);
                }
                if (n.clusterColors != null) {
                    tmp.addAll(n.clusterColors);
                }
            }
            this.removeChildren(tmp);
        }

        currentClusteringElements = elements;

        // show new elements
        if (elements != null) {
            for (ClusterElementsStorage n : currentClusteringElements.values()) {
                if (n.clusterBorders != null) {
                    // System.out.println("Added border node " + n.border.hashCode() + " (" + n.hashCode() + ")");
                    for (PNode borderLine : n.clusterBorders) {
                        addChild(borderLine);
                        borderLine.moveToBack();
                        if (currentVisualizationImage != null) {
                            borderLine.moveInFrontOf(currentVisualizationImage);
                        }
                    }
                }
                if (n.clusterLabels != null) {
                    // System.out.println("Added label node " + n.labels.hashCode() + " (" + n.hashCode() + ")");
                    for (PNode label : n.clusterLabels) {
                        addChild(label);
                        label.moveToFront();
                    }
                }

                if (n.clusterColors != null) {
                    for (PNode colorCluster : n.clusterColors) {
                        addChild(colorCluster);
                        colorCluster.moveToBack();
                    }
                }
            }
        }
        // System.out.println("Applied new clustering " + elements.hashCode());
    }

    // Angela:
    /**
     * Adds a manual label to the map. Pops up a Dialog asking for the text. The created label has the size 10x number
     * of units in width.
     */
    public void createLabel() {
        // maybe move this code somewhere else... not 100% appropriate in this class.
        String txt = JOptionPane.showInputDialog("Enter label text:");
        PNode bigLabel;
        // TODO: Fontsize: not nice to have it hard coded here... maybe something
        // like a maxFontSize for the cluster labels and make it depending on that value
        bigLabel = LabelPNodeGenerator.newLabel(txt, this.units.length * 10);
        manualLabels.addChild(bigLabel);
        // bigLabel.moveToFront();
    }

    public PNode getManualLabels() {
        return manualLabels;
    }

    public ArrayList<PNode> getAllClusterLabels() {
        ArrayList<PNode> labels = new ArrayList<PNode>();
        for (ClusterElementsStorage cluster : currentClusteringElements.values()) {
            if (cluster.clusterLabels != null) {
                labels.addAll(cluster.clusterLabels);
            }
        }
        return labels;
    }

    // rudi for storing levels of labels separately
    @SuppressWarnings("unchecked")
    public ArrayList<PNode>[] getClusterLabelsByLevel() {
        ArrayList<ArrayList<PNode>> labels = new ArrayList<ArrayList<PNode>>();
        for (ClusterElementsStorage cluster : currentClusteringElements.values()) {
            if (cluster.clusterLabels != null) {
                labels.add(cluster.clusterLabels);
            }
        }
        return labels.toArray(new ArrayList[labels.size()]);
    }

    // Doris
    public ClusteringTree getClusteringTree() {
        return currentClusteringTree;
    }

    private int getScaledBackgroundImageWidth() {
        return getScaledBackgroundImageWidth(currentVisualization);
    }

    private int getScaledBackgroundImageWidth(BackgroundImageVisualizer visualization) {
        return gsom.getLayer().getXSize() * UNIT_WIDTH / visualization.getPreferredScaleFactor();
    }

    private int getScaledBackgroundImageHeight() {
        return getScaledBackgroundImageHeight(currentVisualization);
    }

    private int getScaledBackgroundImageHeight(BackgroundImageVisualizer visualization) {
        return gsom.getLayer().getYSize() * UNIT_HEIGHT / visualization.getPreferredScaleFactor();
    }

    private int getBackgroundImageWidth() {
        return gsom.getLayer().getXSize() * UNIT_WIDTH;
    }

    private int getBackgroundImageHeight() {
        return gsom.getLayer().getYSize() * UNIT_HEIGHT;
    }

    public boolean setVisualization(int vis, int variant) throws SOMToolboxException {
        return setVisualization(visualizations[vis], variant);
    }

    /**
     * Method to be used for setting the initial visualisation on startup - does not actually create the image, just
     * sets the {@link #currentVisualization} and {@link #currentVisualizationVariant}.
     */
    public boolean setInitialVisualizationOnStartup(BackgroundImageVisualizer vis, int variant)
            throws SOMToolboxException {
        currentVisualization = vis;
        currentVisualizationVariant = variant;

        String[] neededInputs = currentVisualization.needsAdditionalFiles();
        if (neededInputs != null && neededInputs.length > 0) {
            for (String neededInput : neededInputs) {
                SOMVisualisationData inputObject = inputObjects.getObject(neededInput);
                // System.out.println("in need of: " + inputObject.getFileName() + ", "+ inputObject.getType() + ", " +
                // inputObject.getName());
                inputObject.loadFromFile(state.getFileChooser(), parentFrame);
                if (currentVisualization.needsAdditionalFiles() == null
                        || currentVisualization.needsAdditionalFiles().length == 0) {
                    // if after reading this file we do not need any additional ones
                    break; // we stop asking for more files
                }
            }
        }
        // no further files needed
        try {
            if (getVisualisation() != null) {
                return true;
            } else {
                currentVisualization = null;
                currentVisualizationVariant = 0;
                return false;
            }
        } catch (SOMToolboxException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            return false;
        }
    }

    /**
     * Gets the visualisation variant from the current {@link #currentVisualization} obtaining the values from
     * {@link #currentVisualizationVariant}, {@link #getScaledBackgroundImageWidth()} and
     * {@link #getScaledBackgroundImageHeight()}.
     */
    private BufferedImage getVisualisation() throws SOMToolboxException {
        return currentVisualization.getVisualization(currentVisualizationVariant, gsom,
                getScaledBackgroundImageWidth(), getScaledBackgroundImageHeight());
    }

    public boolean setVisualization(BackgroundImageVisualizer vis, int variant) throws SOMToolboxException {
        boolean res = false;

        BackgroundImageVisualizer oldVis = currentVisualization;
        int oldVariant = currentVisualizationVariant;

        currentVisualization = vis;
        currentVisualizationVariant = variant;

        String[] neededInputs = currentVisualization.needsAdditionalFiles();
        if (neededInputs != null && neededInputs.length > 0) {
            for (String neededInput : neededInputs) {
                SOMVisualisationData inputObject = inputObjects.getObject(neededInput);
                // System.out.println("in need of: " + inputObject.getFileName() + ", "+ inputObject.getType() + ", " +
                // inputObject.getName());
                inputObject.loadFromFile(state.getFileChooser(), parentFrame);
                if (currentVisualization.needsAdditionalFiles() == null
                        || currentVisualization.needsAdditionalFiles().length == 0) {
                    // if after reading this file we do not need any additional ones
                    break; // we stop asking for more files
                }
            }
        }
        // no further files needed
        try {
            BufferedImage img = getVisualisation();
            if (img != null) {
                PImage newVisualization = new PImage(img);
                if (currentVisualizationImage != null && currentVisualizationImage.isDescendentOf(this)) {
                    this.removeChild(currentVisualizationImage);
                    currentVisualizationImage = null;
                }
                currentVisualizationImage = newVisualization;
                res = true;
            } else {
                currentVisualization = oldVis;
                currentVisualizationVariant = oldVariant;
                res = false;
                return res;
            }
            initCurrentVisualisation();
            return res;
        } catch (SOMToolboxException e) {
            // JOptionPane.showMessageDialog(parentFrame, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            // Logging Handler will show MessageDialog when Message is severe:
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            return false;
        }
    }

    /** Return the GeneralUnitPNode at the unit index x/y. */
    public GeneralUnitPNode getUnit(int x, int y) {
        return units[x][y];
    }

    /** Return the GeneralUnitPNode at the unit index x/y. */
    public GeneralUnitPNode getUnit(Unit u) {
        return units[u.getXPos()][u.getYPos()];
    }

    /** Return the unit width in pixles. */
    public int getUnitWidth() {
        return UNIT_WIDTH;
    }

    /** Return the unit height in pixels. */
    public int getUnitHeight() {
        return UNIT_HEIGHT;
    }

    /** Return the map width in pixels, i.e. the unit width times the xSize of the map. */
    @Override
    public double getWidth() {
        return UNIT_WIDTH * gsom.getLayer().getXSize();
    }

    /** Return the map height in pixels, i.e. the height width times the ySize of the map. */
    @Override
    public double getHeight() {
        return UNIT_HEIGHT * gsom.getLayer().getYSize();
    }

    @Override
    public boolean setBounds(double x, double y, double width, double height) {
        if (super.setBounds(x, y, width, height)) {
            // setFrame(x, y, width, height);
            return true;
        }
        return false;
    }

    public void setLinkageVisibilityMode(boolean visible) {
        if (inputLinkagePath != null) {
            if (visible) {
                if (!inputLinkagePath.isDescendentOf(this)) {
                    addChild(inputLinkagePath);
                }
            } else {
                if (inputLinkagePath.isDescendentOf(this)) {
                    removeChild(inputLinkagePath);
                }
            }
        }
    }

    public void reInitUnitDetails() {
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                if (units[i][j] != null) { // check needed for mnemonic SOMs
                    units[i][j].reInitUnitDetails();
                }
            }
        }
    }

    public Color[] getClassLegendColors() {
        if (classInfo != null) {
            return classInfo.getClassColors();
        } else {
            return null;
        }
    }

    public void setClassColor(int index, Color color) {
        if (classInfo != null) {
            for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
                for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                    if (units[i][j] != null && units[i][j].hasPieCharts()) { // check needed for mnemonic SOMs
                        units[i][j].setClassColor(index, color);
                    }
                }
            }
        }
    }

    public void setClassColors(Color[] colors) {
        if (classInfo != null) {
            for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
                for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                    if (units[i][j] != null && units[i][j].hasPieCharts()) { // check needed for mnemonic SOMs
                        units[i][j].setClassColors(colors);
                    }
                }
            }
        }
    }

    public String[] getClassLegendNames() {
        if (classInfo != null) {
            return classInfo.classNames();
        } else {
            return null;
        }
    }

    public void setShowOnlySelectedClasses(boolean selectedClassesOnly) {
        // TODO Auto-generated method stub
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                if (units[i][j] != null) { // check needed for mnemonic SOMs
                    units[i][j].setShowOnlySelectedClasses(selectedClassesOnly);
                }
            }
        }
        this.repaint();
    }

    public void updateClassSelection(int[] indices) {
        // System.out.println(indices);
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                if (units[i][j] != null) { // check needed for mnemonic SOMs
                    units[i][j].updateClassSelection(indices);
                }
            }
        }
        this.repaint();
    }

    public void updatePointLocations() {
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                if (units[i][j] != null) { // check needed for mnemonic SOMs
                    units[i][j].reInitUnitDetails();
                }
            }
        }
    }

    public SharedSOMVisualisationData getInputObjects() {
        return inputObjects;
    }

    public void updateClassInfo(SOMLibClassInformation classInfo) {
        this.classInfo = classInfo;
        try {
            for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
                for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                    if (gsom.getLayer().getUnit(i, j) != null) { // check needed for mnemonic SOMs
                        units[i][j].initClassPieCharts(units[i][j].getUnit(), classInfo, UNIT_WIDTH, UNIT_HEIGHT);
                        // now let the new class pie charts be displayed (though the chosen solution seems like a
                        // hack..)
                        units[i][j].reInitUnitDetails();
                        units[i][j].updateClassSelection(null);
                    }
                }
            }
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            System.exit(-1);
        }
    }

    // public void setClassVisibility(boolean classVisibility) {
    // this.showClassInfo = classVisibility;
    // for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
    // for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
    // if (units[i][j] != null) { // check needed for mnemonic SOMs
    // units[i][j].setClassVisibility(classVisibility);
    // }
    // }
    // }
    // }

    public void updateClassVisibility() {
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                if (units[i][j] != null) { // check needed for mnemonic SOMs
                    units[i][j].reInitUnitDetails();
                }
            }
        }
    }

    public GrowingSOM getGsom() {
        return gsom;
    }

    public void setBackgroundImage(BufferedImage background) {
        if (backgroundImage != null) { // remove a possibly already existing background iamge
            removeChild(backgroundImage);
        }
        originalBackgroundImage = background;
        backgroundImage = new PImage(background.getScaledInstance(getBackgroundImageWidth(),
                getBackgroundImageHeight(), Image.SCALE_SMOOTH));
        backgroundImage.setPickable(false);
        setBackgroundImageVisibility(true);
    }

    public void setBackgroundImageVisibility(boolean visible) {
        backgroundImageVisible = visible;
        if (visible) {
            addChild(backgroundImage);
            backgroundImage.moveToBack();
        } else {
            removeChild(backgroundImage);
        }
    }

    public void reInitLabels() {
        // FIXME Khalid: GeneralUnitPNode constructor requires locations

        /*
         * for (int j=0;j<gsom.getLayer().getYSize();j++) { for (int i=0;i<gsom.getLayer().getXSize();i++) { //units[i][j].reInitLabels(); try { if
         * (gsom.getLayer().getUnit(i,j) != null) { // check needed for mnemonic SOMs (might not have all units != null) units[i][j] = new
         * GeneralUnitPNode(gsom.getLayer().getUnit(i,j), classInfo, dataInfo, UNIT_WIDTH, UNIT_HEIGHT); units[i][j].repaint(); } } catch
         * (LayerAccessException e) { // TODO Auto-generated catch block e.printStackTrace(); } } } repaint();
         */
    }

    public BufferedImage getBackgroundImage() {
        return originalBackgroundImage;
    }

    public boolean isBackgroundImageVisible() {
        return backgroundImageVisible;
    }

    public CommonSOMViewerStateData getState() {
        return state;
    }

    public JFrame getParentFrame() {
        return parentFrame;
    }

    /**
     * Resets the list of arrows originating from a unit for each unit on the map.
     */
    public void resetArrows() {
        for (GeneralUnitPNode[] unit : units) {
            for (int j = 0; j < units[0].length; j++) {
                unit[j].resetArrows();
            }
        }
    }

    public void reInitUnitPNodes(int detailLevel) {
        for (GeneralUnitPNode[] unit : units) {
            for (int j = 0; j < units[0].length; j++) {
                unit[j].reInitUnitDetails(detailLevel);
            }
        }
    }

    public PNode getInputCorrectionsPNode() {
        return inputCorrectionsPNode;
    }

    public void setInputCorrectionsVisible(boolean visible) {
        inputCorrectionsPNode.setVisible(visible);
    }

    public void clearInputCorrections() {
        inputCorrectionsPNode.removeAllChildren();
    }

    public void clearInputCorrections(CreationType type) {
        ArrayList<PNode> toRemove = new ArrayList<PNode>();
        for (Iterator<?> iterator = inputCorrectionsPNode.getChildrenIterator(); iterator.hasNext();) {
            ArrowPNode node = (ArrowPNode) iterator.next();
            if (node.getCreationType() == type) {
                toRemove.add(node);
            }
        }
        inputCorrectionsPNode.removeChildren(toRemove);
    }

    /**
     * @return Returns the builder.
     */
    public TreeBuilder getClusteringTreeBuilder() {
        return clusteringTreeBuilder;
    }

    public GeneralUnitPNode getGeneralUnitPNodeAtPos(Point2D p) {
        if (unitsNode.getFullBounds().contains(p)) {

            int xPos = (int) Math.floor(p.getX() / getUnitWidth());
            int yPos = (int) Math.floor(p.getY() / getUnitHeight());

            return getUnit(xPos, yPos);
        } else {
            return null;
        }
    }

    public GeneralUnitPNode getGeneralUnitPNodeAtPos(double x, double y) {
        return getGeneralUnitPNodeAtPos(new Point2D.Double(x, y));
    }

    public GeneralUnitPNode getGeneralUnitPNodeAtPos(float x, float y) {
        return getGeneralUnitPNodeAtPos(new Point2D.Float(x, y));
    }

    public PNode getUnitsNode() {
        return unitsNode;
    }

}
