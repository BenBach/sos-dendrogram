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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.ObjectStreamException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.logging.Logger;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

import at.tuwien.ifs.somtoolbox.apps.viewer.PieChartPNode.PieChartLabelMode;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MapOverviewPane;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibDataInformation;
import at.tuwien.ifs.somtoolbox.layers.Label;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * The graphical representation of one SOM Unit, including labels, data items and class pie charts. This class makes use
 * of the <a href="http://www.cs.umd.edu/hcil/jazz/" target="_blank">Piccolo framework</a> and is the top {@link PNode}
 * for all unit-level visualisations. This PNode has one of the four child nodes, depending on the current zoom level of
 * the {@link SOMViewer} application - {@link GeneralUnitPNode#DETAIL_LEVEL_NO},
 * {@link GeneralUnitPNode#DETAIL_LEVEL_LOW}, {@link GeneralUnitPNode#DETAIL_LEVEL_MEDIUM} and
 * {@link GeneralUnitPNode#DETAIL_LEVEL_HIGH}. Each of the four different PNodes represents four different levels of
 * information details displayed. Each those detail nodes has potentially other child nodes:
 * <ul>
 * <li>The pie-charts representing class information - {@link PieChartPNode}</li>
 * <li>A {@link PNode} for holding the Labels, which are {@link PText} objects</li>
 * <li>A {@link PNode} for holding the data item names, which are {@link PText} objects</li>
 * <li>A {@link PNode} for holding the quality measure value, which is a {@link PText} object</li>
 * </ul>
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: GeneralUnitPNode.java 3969 2010-12-15 13:17:22Z mayer $
 */
public class GeneralUnitPNode extends PNode {
    private static final long serialVersionUID = 1L;

    public static final int DETAIL_LEVEL_NO = 0;

    public static final int DETAIL_LEVEL_LOW = 1;

    public static final int DETAIL_LEVEL_MEDIUM = 2;

    public static final int DETAIL_LEVEL_HIGH = 3;

    public static final int NUMBER_OF_DETAIL_LEVELS = 4;

    /**
     * Names for the different zoom/scale levels, corresponding to {@link #DETAIL_LEVEL_NO}, {@link #DETAIL_LEVEL_LOW},
     * ..
     */
    public static final String[] detailLevelNames = { "none", "low", "medium", "high" };

    public static final int DATA_DISPLAY_VARIANT_INPUTOBJECT = 0;

    public static final int DATA_DISPLAY_VARIANT_INPUTOBJECTSHIFTED = 1;

    public static final int DATA_DISPLAY_VARIANT_TEXT = 2;

    public static final int DATA_DISPLAY_VARIANTS = 3;

    private static int[] NUMBER_OF_LABELS = { 1, 2, 3, -1 };

    private static int[] NUMBER_OF_COLUMNS = { 1, 1, 1, 3 };

    private static int[] FONT_SIZE_LABELS = { 25, 20, 12, 6 };

    private static int[] MAX_LABEL_LENGTH = { 9, 9, 20, 10 };

    private static int[] FONT_SIZE_DATA = { 40, 40, 36, 4 };

    private int currentDetailLevel = DETAIL_LEVEL_NO;

    private Unit u = null;

    private double X = 0;

    private double Y = 0;

    private double width = 0;

    private double height = 0;

    private boolean drawBorder = true;

    private Rectangle2D border = null;

    private final Color borderColor = Color.gray;

    private final BasicStroke borderStroke = new BasicStroke(1.0f);

    private int[] selectedClassIndices = null;

    private boolean showOnlySelectedClasses = false;

    private boolean selected = false;

    private PPath selectionMarker = null;

    private ArrayList<ArrowPNode> arrowsFromThisUnit = new ArrayList<ArrowPNode>();

    private double oldScale = 1;

    private double currentScale = 1;

    /** Nodes for details at different levels */
    private PNode[] detailNodes = new PNode[NUMBER_OF_DETAIL_LEVELS];

    /**
     * Data input nodes for different levels, length of {@link #NUMBER_OF_DETAIL_LEVELS}, contains a {@link PText} at
     * {@link #DATA_DISPLAY_VARIANT_TEXT}, and {@link InputPNode}s at {@link #DATA_DISPLAY_VARIANT_INPUTOBJECT} and
     * {@link #DATA_DISPLAY_VARIANT_INPUTOBJECTSHIFTED}.
     */
    private PNode[][][] dataDetail = new PNode[NUMBER_OF_DETAIL_LEVELS][DATA_DISPLAY_VARIANTS][];

    private PNode[] dataCountDetail = new PNode[NUMBER_OF_DETAIL_LEVELS];

    /** Label nodes for different levels */
    private PNode[] labelDetailNodes = new PNode[NUMBER_OF_DETAIL_LEVELS];

    /** PieChart nodes for different levels */
    private PieChartPNode[] pieChartDetailNodes = new PieChartPNode[NUMBER_OF_DETAIL_LEVELS];

    private SOMLibClassInformation classInfo = null;

    private SOMLibDataInformation dataInfo = null;

    private boolean classInfoSelectionChanged = false;

    private PPath queryResultMarker;

    private Point[][] locations;

    private CommonSOMViewerStateData state;

    /**
     * Constructor for mnemonic (sparse) SOMs. Initialises an empty cell with no unit attached.
     */
    public GeneralUnitPNode(int x, int y, double width, double height) {
        setPickable(false);
        setChildrenPickable(false);
        X = x * width;
        Y = y * height;
        initPNodeProperties(width, height);
        addChild(detailNodes[DETAIL_LEVEL_NO]);
    }

    public GeneralUnitPNode(Unit u, CommonSOMViewerStateData state, SOMLibClassInformation classInfo,
            SOMLibDataInformation dataInfo, double width, double height) {
        this(u, state, classInfo, dataInfo, null, width, height);
    }

    public GeneralUnitPNode(Unit u, CommonSOMViewerStateData state, SOMLibClassInformation classInfo,
            SOMLibDataInformation dataInfo, Point[][] locations, double width, double height) {
        this.u = u;
        X = u.getXPos() * width;
        Y = u.getYPos() * height;
        initPNodeProperties(width, height);

        this.classInfo = classInfo;
        this.dataInfo = dataInfo;
        this.locations = locations;

        this.state = state;

        if (classInfo != null) { // class information present, generate pie chart
            initClassPieCharts(u, classInfo, width, height);
        }
        for (int i = 0; i < detailNodes.length; i++) {
            if (detailNodes[i] == null) {
                detailNodes[i] = new PNode();
            }
        }

        if (u.getNumberOfMappedInputs() > 0) {
            initDetails();
        }
        addChild(detailNodes[DETAIL_LEVEL_MEDIUM]);
    }

    public GeneralUnitPNode(Unit u, GeneralUnitPNode clone) {
        this(u, clone.state, clone.classInfo, clone.dataInfo, clone.locations, clone.width, clone.height);
    }

    private void addDataChildren() {
        PNode[] nodes = dataDetail[currentDetailLevel][getDataInputVariant()];
        if (state.dataVisibilityMode) {
            if (nodes != null) {
                for (PNode node2 : nodes) {
                    PNode node = new PNode();
                    node.addChild(node2);
                    addChild(node);
                }
            }
        }
    }

    public void reInitUnitDetails() {
        if (u.getNumberOfMappedInputs() > 0) {
            removeDetailNodes();
            for (PNode detailNode : detailNodes) {
                detailNode.removeAllChildren();
                removeAllChildren();
            }
            initDetails();
            addChild(detailNodes[currentDetailLevel]);
            addDataChildren();
        }
    }

    public void reInitUnitDetails(int detailLevel) {
        if (u.getNumberOfMappedInputs() > 0) {
            removeDetailNodes();
            // re-init changed one
            if (detailLevel >= DETAIL_LEVEL_NO && detailLevel <= DETAIL_LEVEL_HIGH) {
                detailNodes[detailLevel].removeAllChildren();
                removeAllChildren();
                for (int i = 0; i < DATA_DISPLAY_VARIANTS; i++) {
                    dataDetail[detailLevel][i] = null;
                }
            }
            initDetails(detailLevel);
            addChild(detailNodes[currentDetailLevel]);
            addDataChildren();
        }
    }

    /** remove currently added detail levels. */
    private void removeDetailNodes() {
        for (PNode detailNode : detailNodes) {
            if (detailNode.isDescendentOf(this)) {
                removeChild(detailNode);
            }
        }
    }

    /**
     * Initializes common properties for unit PNodes and empty PNodes
     */
    private void initPNodeProperties(double width, double height) {
        border = new Rectangle2D.Double();
        this.width = width;
        this.height = height;
        border.setRect(X, Y, width, height);
        this.setBounds(X, Y, width, height);
        selectionMarker = PPath.createRectangle((float) X, (float) Y, (float) width, (float) height);
        selectionMarker.setPaint(Color.decode("#ff7505"));
        selectionMarker.setTransparency(0.5f);

        queryResultMarker = PPath.createRectangle((float) X, (float) Y, (float) width, (float) height);
        queryResultMarker.setPaint(Color.ORANGE);
        queryResultMarker.setTransparency(0.5f);
    }

    public void initClassPieCharts(Unit u, SOMLibClassInformation classInfo, double width, double height) {
        this.classInfo = classInfo;
        if (classInfo != null) { // class information present, generate pie chart
            int[] values = classInfo.computeClassDistribution(u.getMappedInputNames());
            // TODO: debug output (can be used for cluster purity calculator in the future)
            // DoubleMatrix1D dummy = new DenseDoubleMatrix1D(values);
            // System.out.println("Cluster purity for unit ("+u.getXPos()+"/"+u.getYPos()+"):");
            // for (int cc=0; cc<classInfo.numClasses(); cc++) {
            // System.out.println(" "+classInfo.classNames()[cc]+" "+(values[cc]/u.getNumberOfMappedInputs()));
            // }
            // end debug

            // FIXME: maybe reuse the PieChartPNode for each detail level?
            pieChartDetailNodes[DETAIL_LEVEL_NO] = new PieChartPNode(0, 0, width, height, values,
                    u.getNumberOfMappedInputs());
            pieChartDetailNodes[DETAIL_LEVEL_LOW] = new PieChartPNode(0, 0, width, height, values,
                    u.getNumberOfMappedInputs());
            pieChartDetailNodes[DETAIL_LEVEL_MEDIUM] = new PieChartPNode(0, 0, width, height, values,
                    u.getNumberOfMappedInputs());
        }
    }

    /**
     * Initializes text labels for this node. The labels are displayed in a virtual table like structure.
     * 
     * @param numLabels Number of labels to be shown. If '-1' the limit is set to the number of labels in unit
     *            description file.
     * @param numCol Number of table columns. Setting to '1' means row table.
     * @param fontSize Font size to use for drawing labels
     * @param length Maximum label length, longer labels will be trunkated
     */
    private PNode initLabels(int numLabels, final int numCol, final int fontSize, final int length) {
        // -1 means we want to show all labels. additionally check if we have enough labels...
        if (numLabels == -1 || u.getLabels().length < numLabels) {
            numLabels = u.getLabels().length;
        }

        PNode labelsNode = new PNode();
        if (u.getLabels() != null) {
            Font labelsFont = new Font("Sans", Font.PLAIN, fontSize);

            double xOffset[] = new double[numCol];
            for (int i = 0; i < numCol; i++) {
                xOffset[i] = 2 + i * width / numCol;
            }
            double yOffset = 2;
            int columnIndex = 0;

            for (int i = 0; i < numLabels; i++) {
                PText label = new PText(u.getLabels()[i].getName());
                label.setPickable(false);
                // TODO: find a better way to set the colour. maybe the current palette can give the preferred label
                // colour?
                if (state.exactUnitPlacement && getMapPNode().getCurrentVisualization() == null) { // white labels for
                    // sky vis
                    label.setTextPaint(Color.WHITE);
                } else {
                    label.setTextPaint(Color.BLACK);
                }
                label.setFont(labelsFont);
                label.setOffset(xOffset[columnIndex], yOffset); // (i * labelsFont.getSize()) + 2
                label.addAttribute("tooltip", u.getLabels()[i].getName() + "\nqe: "
                        + StringUtils.format(u.getLabels()[i].getQe(), 3) + "\nmean: "
                        + StringUtils.format(u.getLabels()[i].getValue(), 3));
                label.addAttribute("type", "label");
                labelsNode.addChild(label);

                if (label.getBoundsReference().getWidth() > width / numCol) {
                    label.setText(label.getText().substring(0, Math.min(length, label.getText().length())) + "...");
                }

                if ((i + 1) % numCol == 0) { // Next row
                    yOffset += labelsFont.getSize() + 2;
                    columnIndex = 0;
                } else { // Next column
                    columnIndex++;
                }
            }
        }
        return labelsNode;
    }

    public MapPNode getMapPNode() {
        // parent is the PNode holding all GeneralUnitPNodes, the parent of which in turn is the MapPNode
        return (MapPNode) getParent().getParent();
    }

    /**
     * Initializes textual details
     * 
     * @param threshold Displays description of mapped inputs below certain threshold (count), otherwise a count is
     *            displayed.
     * @param fontSize Text size
     * @param yInOffset Positional offset
     */
    private PNode[] initData(int threshold, int fontSize, double yInOffset, int detailLevel, int variant) {
        final String commonVectorLabelPrefix = state.growingLayer.getCommonVectorLabelPrefix();
        int nodesToDisplay = u.getNumberOfMappedInputs() * threshold / 100;

        if (variant == DATA_DISPLAY_VARIANT_INPUTOBJECT || variant == DATA_DISPLAY_VARIANT_INPUTOBJECTSHIFTED) { // Display
            // stars
            // or
            // simply
            // count
            if (locations != null) {
                PNode[] dataNodes = new PNode[nodesToDisplay];
                for (int index = 0; index < nodesToDisplay; index++) {
                    PNode star = new StarPNode();
                    String inputName = u.getMappedInputName(index);
                    String inputDistance = StringUtils.format(u.getMappedInputDistance(index), 3);
                    String inputText = "";

                    try {
                        if (dataInfo != null) {
                            inputText = URLDecoder.decode(dataInfo.getDataDisplayName(inputName), "UTF-8");
                        } else {
                            inputText = URLDecoder.decode(inputName, "UTF-8");
                        }
                    } catch (Exception e) {
                        inputText = inputName;
                    }
                    star.addAttribute("id", inputName.replaceFirst(commonVectorLabelPrefix, ""));
                    star.addAttribute("type", "data");
                    star.setPickable(true);
                    if (dataInfo != null) {
                        try {
                            star.addAttribute("tooltip", inputText + "\ndistance: " + inputDistance + "\n"
                                    + URLDecoder.decode(dataInfo.getDataLocation(inputName), "UTF-8"));
                        } catch (Exception e) {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                                    "URLDecoder had problems reading the name of the datum '" + inputName + "'.");
                            star.addAttribute("tooltip", inputText + "\ndistance: " + inputDistance);
                        }
                        star.addAttribute("location", dataInfo.getBaseDir() + dataInfo.getDataLocation(inputName));
                    } else {
                        star.addAttribute("tooltip", inputText + "\ndistance: " + inputDistance);
                        star.addAttribute("location", inputName);
                    }
                    star.setOffset(this.X + locations[variant][index].getX(), this.Y + locations[variant][index].getY());
                    dataNodes[index] = star;
                }
                return dataNodes;
            } else {
                return new PNode[0];
            }
        } else {
            PNode[] dataNodes = new PNode[nodesToDisplay];
            if (detailLevel == DETAIL_LEVEL_HIGH) {
                Font font = new Font("Sans", Font.PLAIN, fontSize);
                final int numDataCol = 3;
                double xDataOffsets[] = new double[numDataCol];
                for (int i = 0; i < numDataCol; i++) {
                    xDataOffsets[i] = i * width / numDataCol + 2;
                }
                double yOffset = yInOffset + dataCountDetail[detailLevel].getBoundsReference().getHeight() + 2;
                int numData = Math.min(dataNodes.length, u.getNumberOfMappedInputs()); // limitation by number of labels
                // in unit description file
                int x = 0;
                for (int index = 0; index < numData; index++) {
                    PText pText = null;
                    final String inputName = u.getMappedInputNames()[index];
                    String inputText = null;
                    try {
                        if (dataInfo != null) {
                            inputText = URLDecoder.decode(dataInfo.getDataDisplayName(inputName), "UTF-8");
                        } else {
                            inputText = URLDecoder.decode(inputName, "UTF-8");
                        }
                    } catch (Exception e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                                "URLDecoder had problems reading the name of the datum '" + inputName + "'.");
                        inputText = inputName;
                    }
                    pText = new PText(inputText.replaceFirst(commonVectorLabelPrefix, ""));

                    pText.addAttribute("id", inputName);
                    pText.addAttribute("type", "data");
                    pText.setFont(font);
                    pText.setTextPaint(Color.BLUE);
                    pText.setOffset(this.X + xDataOffsets[x], this.Y + yOffset);
                    if (dataInfo != null) {
                        try {
                            pText.addAttribute("tooltip", inputText + "\ndistance: "
                                    + StringUtils.format(u.getMappedInputDistances()[index], 3) + "\n"
                                    + URLDecoder.decode(dataInfo.getDataLocation(inputName), "UTF-8"));
                        } catch (Exception e) {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                                    "URLDecoder had problems reading the name of the datum '" + inputName + "'.");
                            pText.addAttribute("tooltip", pText.getText() + "\ndistance: "
                                    + StringUtils.format(u.getMappedInputDistances()[index], 3));
                        }
                        pText.addAttribute("location", dataInfo.getBaseDir() + dataInfo.getDataLocation(inputName));
                    } else {
                        pText.addAttribute("tooltip", inputText + "\ndistance: "
                                + StringUtils.format(u.getMappedInputDistances()[index], 3));
                        pText.addAttribute("location", inputName);
                    }
                    dataNodes[index] = pText;
                    if (pText.getBoundsReference().getWidth() > width / numDataCol) {
                        pText.setText(pText.getText().substring(0, Math.min(16, pText.getText().length())) + "...");
                    }
                    x++;
                    if ((index + 1) % numDataCol == 0) { // next line
                        yOffset += font.getSize() + 2; // TODO: check qualityHighYOffset!!!!
                        x = 0;
                    }
                }
                return dataNodes;
            }
        }
        return new PNode[0];
    }

    /**
     * Initializes the details node. A pie chart is generated if class info is available (except
     * {@link #DETAIL_LEVEL_HIGH}, quality measure info is added for {@link #DETAIL_LEVEL_HIGH}.
     */
    private void initDetails() {
        for (int level = 0; level < NUMBER_OF_DETAIL_LEVELS; level++) {
            initDetails(level);
        }
    }

    private void initDetails(int level) {
        detailNodes[level].setOffset(this.X, this.Y);
        double xOffset = 0;
        double yOffset = 0;

        // labels
        if (u.getLabels() != null && state.labelVisibilityMode) {
            if (labelDetailNodes[level] == null) {
                labelDetailNodes[level] = initLabels(NUMBER_OF_LABELS[level], NUMBER_OF_COLUMNS[level],
                        FONT_SIZE_LABELS[level], MAX_LABEL_LENGTH[level]);
            }
            detailNodes[level].addChild(labelDetailNodes[level]);
            yOffset = labelDetailNodes[level].getFullBoundsReference().getHeight();
            if (yOffset > state.maxLabelYOffset[level]) {
                state.maxLabelYOffset[level] = yOffset;
            }
        }

        // quality, for high level only
        if (level == DETAIL_LEVEL_HIGH) {
            QualityMeasure qm = null;
            if ((qm = u.getLayer().getQualityMeasure()) != null) {
                PNode qualityHigh = new PNode();
                Font qualityHighFont = new Font("Sans", Font.PLAIN, 4);

                try {
                    for (int i = 0; i < qm.getUnitQualityNames().length; i++) {
                        PText qmText = new PText(qm.getUnitQualityNames()[i]
                                + ": "
                                + StringUtils.format(
                                        qm.getUnitQualities(qm.getUnitQualityNames()[i])[u.getXPos()][u.getYPos()], 3));
                        qmText.setPickable(false);
                        qmText.setFont(qualityHighFont);
                        qmText.setOffset(xOffset, 0);
                        xOffset += qmText.getWidth() + 4;
                    }
                } catch (QualityMeasureNotFoundException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            e.getMessage() + " Aborting. BTW: the must be a major flaw"
                                    + "in the quality measure class that has been used.");
                    System.exit(-1);
                }

                double qualityHighYOffset = height - (qualityHigh.getFullBoundsReference().getHeight() + 2);
                qualityHigh.setOffset(2, qualityHighYOffset);
                detailNodes[level].addChild(qualityHigh);
            }
        }

        if (dataCountDetail[level] == null) {
            Font font = new Font("Sans", Font.PLAIN, FONT_SIZE_DATA[level]);
            String inputCount = String.valueOf(u.getNumberOfMappedInputs());
            PText numDataText = new PText();
            if (level == DETAIL_LEVEL_NO || level == DETAIL_LEVEL_LOW || level == DETAIL_LEVEL_MEDIUM) {
                numDataText.setText(inputCount);
                // FIXME: use a calculated xOffset rather than a fixed one, e.g. as below
                // double xOffsetNumData = (width - defaultToolkit.getFontMetrics(font).stringWidth(inputCount)) / 2;
                numDataText.setOffset(2, (height - yOffset) / 2 - numDataText.getFont().getSize() / 2 + yOffset);
                dataCountDetail[level] = numDataText;
            } else if (level == DETAIL_LEVEL_HIGH) {
                numDataText.setText("Number of data items: " + inputCount);
                numDataText.setOffset(2, yOffset);
                dataCountDetail[level] = numDataText;
            }
            numDataText.setPickable(false);
            numDataText.setFont(font);
        }
        dataCountDetail[level].setVisible(state.hitsVisibilityMode);
        detailNodes[level].addChild(dataCountDetail[level]);

        // data
        for (int i = 0; i < DATA_DISPLAY_VARIANTS; i++) {
            if (dataDetail[level][i] == null) {
                dataDetail[level][i] = initData(state.thresholdInputPercentage[level], FONT_SIZE_DATA[level], yOffset,
                        level, i);
            }
        }

        // class info, not for high details
        if (level != DETAIL_LEVEL_HIGH) {
            if (classInfo != null) { // class information present, generate pie chart
                double pWidth = width - xOffset;
                double pHeight = height
                        - (level == DETAIL_LEVEL_NO ? height / 5
                                : dataCountDetail[level].getFullBoundsReference().getHeight());
                pieChartDetailNodes[level].setBounds(xOffset, 0, pWidth, pHeight);
                if (state.getClassPiechartMode() != SOMViewer.TOGGLE_PIE_CHARTS_NONE) {
                    detailNodes[level].addChild(pieChartDetailNodes[level]);
                }
            }
        }
    }

    /** Updates the units displayed info by removing & re-creating them. */
    public void updateDetailsAfterMoving() {
        removeDetailNodes();
        for (int detailLevel = 0; detailLevel < detailNodes.length; detailLevel++) {
            detailNodes[detailLevel].removeAllChildren();
            removeAllChildren();
            for (int i = 0; i < DATA_DISPLAY_VARIANTS; i++) {
                dataDetail[detailLevel][i] = null;
                dataCountDetail[detailLevel] = null;
                pieChartDetailNodes[detailLevel] = null;
            }
            if (u.getNumberOfMappedInputs() > 0) {
                initDetails(detailLevel);
            }
        }
        addChild(detailNodes[currentDetailLevel]);
        addDataChildren();
        repaint();
    }

    /** @see edu.umd.cs.piccolo.PNode#paint(edu.umd.cs.piccolo.util.PPaintContext) */
    @Override
    protected void paint(PPaintContext paintContext) {
        Graphics2D g2d = paintContext.getGraphics();

        if (state.exactUnitPlacement && getMapPNode().getCurrentVisualization() == null) { // black background for sky
            // vis
            border.setRect(X, Y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.fill(border);
        }

        if (drawBorder) {
            border.setRect(X, Y, width, height);
            g2d.setStroke(borderStroke);
            g2d.setPaint(Color.CYAN);
            g2d.setColor(borderColor);
            g2d.draw(border);
        }

        PCamera pCam = paintContext.getCamera();
        if (!((PCanvas) pCam.getComponent()).getClass().equals(MapOverviewPane.MapOverviewCanvas.class)) { // only for
            // main
            // display
            currentScale = paintContext.getScale();
            if (currentScale != oldScale) {
                // System.out.println("SCALE: "+currentScale);
                // double os = t10.getScale();
                // t10.setScale(1/currentScale);
                // if (t10.getGlobalFullBounds().width>this.width) {
                // t10.setScale(os);
                // }

                if (currentScale < state.scaleLimits[1]) { // no information
                    if (currentDetailLevel != DETAIL_LEVEL_NO) {
                        currentDetailLevel = DETAIL_LEVEL_NO;
                        detailChanged();
                    }
                } else if (currentScale >= state.scaleLimits[1] && currentScale < state.scaleLimits[2]) { // little
                    // information
                    if (currentDetailLevel != DETAIL_LEVEL_LOW) {
                        currentDetailLevel = DETAIL_LEVEL_LOW;
                        detailChanged();
                    }
                } else if (currentScale >= state.scaleLimits[2] && currentScale < state.scaleLimits[3]) { // more labels
                    if (currentDetailLevel != DETAIL_LEVEL_MEDIUM) {
                        currentDetailLevel = DETAIL_LEVEL_MEDIUM;
                        detailChanged();
                    }
                } else if (currentScale >= state.scaleLimits[3]) { // detailed information
                    if (currentDetailLevel != DETAIL_LEVEL_HIGH) {
                        currentDetailLevel = DETAIL_LEVEL_HIGH;
                        detailChanged();
                    }
                }

                oldScale = currentScale;
            }
        }
    }

    /** @see edu.umd.cs.piccolo.PNode#setBounds(double, double, double, double) */
    @Override
    public boolean setBounds(double x, double y, double width, double height) {
        if (super.setBounds(x, y, width, height)) {
            border.setFrame(x, y, width, height);
            return true;
        }
        return false;
    }

    /**
     * Updates the class pie chart visibility.
     */
    public void updateClassPieCharts() {
        if (u.getNumberOfMappedInputs() > 0 && classInfoSelectionChanged) {
            boolean pieChartVisible = false;
            if (state.getClassPiechartMode() == SOMViewer.TOGGLE_PIE_CHARTS_NONE) { // hiding class info is selected
                pieChartVisible = false;
            } else if (selectedClassIndices == null) { // no classes selected, show pie chart in any case
                pieChartVisible = true;
            } else { // at least one class is selected
                int[] classValues = pieChartDetailNodes[DETAIL_LEVEL_LOW].getValues();
                // check whether this unit contains any mapped data items belonging to a selected class
                // determines whether we show a pie chart at all
                int sc = 0;
                while (sc < selectedClassIndices.length && pieChartVisible == false) {
                    if (classValues[selectedClassIndices[sc]] > 0) {
                        pieChartVisible = true;
                    }
                    sc++;
                }
            }
            for (int i = 0; i < detailNodes.length; i++) {
                if (detailNodes[i] != null && pieChartDetailNodes[i] != null) {
                    if (state.getClassPiechartMode() == SOMViewer.TOGGLE_PIE_CHARTS_SHOW_COUNTS) {
                        pieChartDetailNodes[i].setShowLegend(PieChartLabelMode.Count);
                    } else if (state.getClassPiechartMode() == SOMViewer.TOGGLE_PIE_CHARTS_SHOW_PERCENT) {
                        pieChartDetailNodes[i].setShowLegend(PieChartLabelMode.Percent);
                    } else {
                        pieChartDetailNodes[i].setShowLegend(PieChartLabelMode.None);
                    }
                    if (selectedClassIndices != null) {
                        Color[] cs = pieChartDetailNodes[i].getLegendColors();
                        for (int j = 0; j < cs.length; j++) {
                            boolean colorVisible = !getShowOnlySelectedClasses();
                            if (!colorVisible) {
                                for (int selectedClassIndice : selectedClassIndices) {
                                    if (j == selectedClassIndice) {
                                        colorVisible = true;
                                        break;
                                    }
                                }
                            }
                            if (colorVisible) {
                                cs[j] = new Color(cs[j].getRed(), cs[j].getGreen(), cs[j].getBlue());
                            } else {
                                cs[j] = new Color(cs[j].getRed(), cs[j].getGreen(), cs[j].getBlue(), 0);
                            }

                        }
                        pieChartDetailNodes[i].setColors(cs);
                    }
                    boolean ancestorOf = detailNodes[i].isAncestorOf(pieChartDetailNodes[i]);
                    if (state.getClassPiechartMode() != SOMViewer.TOGGLE_PIE_CHARTS_NONE && pieChartVisible) {
                        if (!ancestorOf) {
                            detailNodes[i].addChild(pieChartDetailNodes[i]);
                        }
                    } else {
                        if (ancestorOf) {
                            detailNodes[i].removeChild(pieChartDetailNodes[i]);
                        }
                    }
                }
            }
        }
        classInfoSelectionChanged = false;
    }

    public int getDataInputVariant() {
        if (state.exactUnitPlacement) {
            if (state.shiftOverlappingInputs) {
                return DATA_DISPLAY_VARIANT_INPUTOBJECTSHIFTED;
            } else {
                return DATA_DISPLAY_VARIANT_INPUTOBJECT;
            }
        } else {
            return DATA_DISPLAY_VARIANT_TEXT;
        }
    }

    /**
     * Updates child nodes to display upon change in detail level
     */
    private void detailChanged() {
        removeAllChildren();
        setSelected(selected);
        addChild(detailNodes[currentDetailLevel]);
        addDataChildren();
    }

    public boolean hasPieCharts() {
        return pieChartDetailNodes[DETAIL_LEVEL_LOW] != null;
    }

    /**
     * This implementation does not check for the pie charts ({@link #pieChartDetailNodes} to be initialised and should
     * therefore be only used if it is for sure != null.
     */
    public Color getClassLegendColorFast(int index) {
        return pieChartDetailNodes[DETAIL_LEVEL_LOW].getLegendColor(index);
    }

    public void setClassColor(int index, Color color) {
        for (PieChartPNode pieChartDetailNode : pieChartDetailNodes) {
            if (pieChartDetailNode != null) {
                pieChartDetailNode.setColor(index, color);
            }
        }
        repaint();
    }

    public void setClassColors(Color[] colors) {
        for (PieChartPNode pieChartDetailNode : pieChartDetailNodes) {
            if (pieChartDetailNode != null) {
                pieChartDetailNode.setColors(colors);
            }
        }
        repaint();
    }

    public void updateClassSelection(int[] indices) {
        selectedClassIndices = indices;
        classInfoSelectionChanged = true;
        updateClassPieCharts();
    }

    public String[] getMappedDataNames() {
        return u.getMappedInputNames();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean sel) {
        selected = sel;
        if (selected) {
            if (!selectionMarker.isDescendentOf(this)) {
                addChild(selectionMarker);
            }
        } else {
            if (selectionMarker.isDescendentOf(this)) {
                removeChild(selectionMarker);
            }
        }
    }

    public Label[] getLabels(String type) {
        return u.getLabels(type);
    }

    public void setQueryHit() {
        addChild(queryResultMarker);
    }

    public void removeQueryHit() {
        removeChild(queryResultMarker);
    }

    /**
     * Returns the associtated SOM unit for this node
     */
    public Unit getUnit() {
        return u;
    }

    public PieChartPNode getClassPieChart(int width, int height) {
        if (classInfo != null) { // class information present, generate pie chart
            int[] values = classInfo.computeClassDistribution(u.getMappedInputNames());
            PieChartPNode pieChartPNode = new PieChartPNode(0, 0, width, height, values, u.getNumberOfMappedInputs());
            Color[] colors = pieChartDetailNodes[DETAIL_LEVEL_NO].getLegendColors();
            for (int i = 0; i < colors.length; i++) {
                pieChartPNode.setColor(i, colors[i]);
            }
            return pieChartPNode;
        } else {
            return null;
        }
    }

    // Angela: used by the Serializer -- serialize another object instead
    private Object writeReplace() throws ObjectStreamException {
        return new GeneralUnitPNodeSerializer(this);
    }

    public ArrayList<ArrowPNode> getArrows() {
        return arrowsFromThisUnit;
    }

    public void setArrows(ArrayList<ArrowPNode> arrows) {
        this.arrowsFromThisUnit = arrows;
    }

    public void addArrow(ArrowPNode arrow) {
        this.arrowsFromThisUnit.add(arrow);
    }

    public void resetArrows() {
        this.arrowsFromThisUnit = new ArrayList<ArrowPNode>();
    }

    public Point[] getLocations() {
        if (state.shiftOverlappingInputs) {
            return locations[DATA_DISPLAY_VARIANT_INPUTOBJECTSHIFTED];
        } else {
            return locations[DATA_DISPLAY_VARIANT_INPUTOBJECT];
        }
    }

    public Point getPostion() {
        return new Point((int) X, (int) Y);
    }

    public boolean getShowOnlySelectedClasses() {
        return showOnlySelectedClasses;
    }

    public void setShowOnlySelectedClasses(boolean showOnlySelectedClasses) {
        this.showOnlySelectedClasses = showOnlySelectedClasses;
        classInfoSelectionChanged = true;
        updateClassPieCharts();
    }

    @Override
    public String toString() {
        return u.toString();
    }

}