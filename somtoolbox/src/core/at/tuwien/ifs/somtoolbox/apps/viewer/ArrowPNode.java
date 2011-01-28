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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Line2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.InputCorrections;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.CreationType;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.InputCorrection;
import at.tuwien.ifs.somtoolbox.visualization.comparison.Shift;

/**
 * Class for arrows on the SOM Visualization (that indicate shifts in a SOM)
 * 
 * @author Doris Baum
 * @version $Id: ArrowPNode.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class ArrowPNode extends PNode {
    private static final long serialVersionUID = 1L;

    // coordinates of the arrow
    private double x1 = 0;

    private double y1 = 0;

    private double x2 = 0;

    private double y2 = 0;

    // line object for arrow
    private Line2D.Double arrow = null;

    private boolean typeVisibility = true;

    private boolean selectionVisibility = true;

    // do we want to show the arrowheads at the beginning and end of the line?
    private boolean showStartHead = false;

    private boolean showEndHead = true;

    private PNode tooltipNode;

    // how long and broad should the arrowheads be?
    private double arrowHeadLength = 40;

    private double arrowHeadWidth = 20;

    // polygon object for drawing the arrowheads
    private Polygon startHead = null;

    private Polygon endHead = null;

    // arrow color
    private Color color = Color.orange;

    private static final Color OUTLIERCOLOR = Color.red;

    private static final Color STABLECOLOR = Color.green;

    private static final Color ADJACENTCOLOR = Color.cyan;

    private static final Color CLUSTERCOLOR = Color.blue;

    // stroke for the line
    private BasicStroke stroke = new BasicStroke(2.0f);

    // type of arrow: outlier or stable data vector? determines color
    private int type = -1;

    private CreationType creationType;

    private float arrowHeadScale = 1f;

    public static final int OUTLIER = Shift.OUTLIER;

    public static final int STABLE = Shift.STABLE;

    public static final int ADJACENT = Shift.ADJACENT;

    public static final int CLUSTER = Shift.CLUSTER;

    public static final int MAXARROWWIDTH = 20;

    public ArrowPNode(Point start, Point end, double offsetX, double offsetY) {
        this(start.x + offsetX, start.y + offsetY, end.x + offsetX, end.y + offsetY);
    }

    /**
     * Constructor that takes arrow coordinates
     */
    public ArrowPNode(double x1, double y1, double x2, double y2) {
        super();
        this.setPaint(color);

        arrow = new Line2D.Double();
        startHead = new Polygon();
        endHead = new Polygon();

        this.setArrow(x1, y1, x2, y2);
    }

    public ArrowPNode(InputCorrections.CreationType creationType, String[][] attributes, String tooltip, Color color,
            double width, double height, Point pointBegin, Point pointEnd) {
        this(pointBegin, pointEnd, width / 2, height / 2);
        this.creationType = creationType;
        for (String[] attribute : attributes) {
            addAttribute(attribute[0], attribute[1]);
        }
        tooltipNode = new PNode();
        tooltipNode.addAttribute("tooltip", tooltip);
        addChild(tooltipNode);
        tooltipNode.moveToFront();
        setColor(color);
    }

    /**
     * Show the arrowhead at the (x1,y1)-end of the line?
     * 
     * @param state arrowhead "on" or "off"?
     */
    public void showStartArrowHead(boolean state) {
        this.showStartHead = state;
    }

    public void setArrowHeadScale(float scale) {
        this.arrowHeadScale = scale;
    }

    /**
     * Show the arrowhead at the (x2,y2)-end of the line?
     * 
     * @param state arrowhead "on" or "off"?
     */
    public void showEndArrowHead(boolean state) {
        this.showEndHead = state;
    }

    /**
     * Calculate the coordinates for the 3 points defining each arrowhead (the arrowhead is a triangle) and generate
     * polygon objects with these points. The polygon objects are then held in attributes startHead and endHead. The
     * lenght of the arrow line is adjusted so that it doesn't overlap with the arrowheds
     */
    private void calculateArrow() {

        // Black Magic happens here... (it all makes sense if you draw
        // little sketches of arrowheads. I promise.)
        double width = Math.abs(x2 - x1);
        double height = Math.abs(y2 - y1);
        double alpha = Math.atan(width / height);
        double deltax1 = arrowHeadScale * arrowHeadLength * Math.sin(alpha);
        double deltay1 = arrowHeadScale * arrowHeadLength * Math.cos(alpha);
        double deltax2 = arrowHeadScale * arrowHeadWidth / 2 * Math.cos(alpha);
        double deltay2 = arrowHeadScale * arrowHeadWidth / 2 * Math.sin(alpha);

        double signA = 1;
        double signB = 1;

        if (x1 < x2) {
            signA = 1;
        } else {
            signA = -1;
        }

        if (y1 < y2) {
            signB = 1;
        } else {
            signB = -1;
        }

        startHead = new Polygon();
        startHead.addPoint((int) x1, (int) y1);
        startHead.addPoint((int) (x1 + deltax1 * signA + deltax2 * signB), (int) (y1 + deltay1 * signB - deltay2
                * signA));
        startHead.addPoint((int) (x1 + deltax1 * signA - deltax2 * signB), (int) (y1 + deltay1 * signB + deltay2
                * signA));

        endHead = new Polygon();
        endHead.addPoint((int) x2, (int) y2);
        endHead.addPoint((int) (x2 - deltax1 * signA + deltax2 * signB), (int) (y2 - deltay1 * signB - deltay2 * signA));
        endHead.addPoint((int) (x2 - deltax1 * signA - deltax2 * signB), (int) (y2 - deltay1 * signB + deltay2 * signA));

        double x1adj = x1;
        double y1adj = y1;
        double x2adj = x2;
        double y2adj = y2;

        if (showStartHead) {
            x1adj = x1 + deltax1 * signA;
            y1adj = y1 + deltay1 * signB;
        }
        if (showEndHead) {
            x2adj = x2 - deltax1 * signA;
            y2adj = y2 - deltay1 * signB;
        }

        arrow.setLine(x1adj, y1adj, x2adj, y2adj);
        if (tooltipNode != null) {
            tooltipNode.setBounds(endHead.getBounds2D());
        }
    }

    /**
     * (Re)sets the coordinates of the arrow to (x1, y1) (x2, y2); produces a line and appropriate arrowheads and resets
     * the Node's bounding box to hold the new arrow
     */
    public void setArrow(double x1, double y1, double x2, double y2) {
        // save coordinates
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        // calculate bounding box:
        // width and height
        double width = Math.abs(x1 - x2);
        double height = Math.abs(y1 - y2);

        // if this is a horizontal or vertical line, make sure the width or hight isn't 0
        // so that the bounding box doesn't disappear -- this would lead to the line not being drawn
        if (width == 0) {
            width = 1;
        }
        if (height == 0) {
            height = 1;
        }

        double boundX = 0;
        double boundY = 0;
        // and bounding box coordinates
        if (x1 < x2) {
            boundX = x1;
        } else {
            boundX = x2;
        }

        if (y1 < y2) {
            boundY = y1;
        } else {
            boundY = y2;
        }

        this.setBounds(boundX, boundY, width, height);
    }

    /**
     * Set the bounds of this PNode object.
     * 
     * @see edu.umd.cs.piccolo.PNode#setBounds(double, double, double, double)
     */
    @Override
    public boolean setBounds(double x, double y, double w, double h) {
        return super.setBounds(x, y, w, h);
    }

    /**
     * Paints the arrow line with given color and stroke, adding arrowheads as applicable
     * 
     * @see edu.umd.cs.piccolo.PNode#paint(edu.umd.cs.piccolo.util.PPaintContext)
     */
    @Override
    protected void paint(PPaintContext paintContext) {

        calculateArrow();

        Graphics2D g2d = paintContext.getGraphics();

        g2d.setPaint(color);
        g2d.setStroke(stroke);
        g2d.draw(arrow);
        if (showStartHead) {
            g2d.fill(startHead);
        }
        if (showEndHead) {
            g2d.fill(endHead);
        }
    }

    /**
     * Is the visibility of this ArrowPNode currently set to true?
     */
    @Override
    public boolean getVisible() {
        return this.typeVisibility && this.selectionVisibility;
    }

    public int getType() {
        return type;
    }

    public CreationType getCreationType() {
        return creationType;
    }

    public void setType(int type) {
        this.type = type;
        if (type == OUTLIER) {
            color = OUTLIERCOLOR;
        } else if (type == STABLE) {
            color = STABLECOLOR;
        } else if (type == CLUSTER) {
            color = CLUSTERCOLOR;
        } else if (type == ADJACENT) {
            color = ADJACENTCOLOR;
        } else {
            this.type = -1;
        }
    }

    public void setLineWidth(double width) {
        arrowHeadLength = Math.sqrt(width) * 30;
        arrowHeadWidth = Math.sqrt(width) * 15;
        float w = (float) width;
        stroke = new BasicStroke(w);
    }

    public void setProportionalWidth(double propWidth) {
        double width = propWidth * MAXARROWWIDTH;
        this.setLineWidth(width);
    }

    public void setSelectionVisibility(boolean selectionVisibility) {
        this.selectionVisibility = selectionVisibility;
    }

    public void setTypeVisibility(boolean typeVisibility) {
        this.typeVisibility = typeVisibility;
    }

    @Override
    public void setVisible(boolean visibility) {

    }

    public void setColor(Color color) {
        this.color = color;
    }

    public static ArrowPNode createInputCorrectionArrow(InputCorrection c, InputCorrections.CreationType creationType,
            GeneralUnitPNode sourceUnitNode, GeneralUnitPNode targetUnitNode) {
        String tooltipBegin;
        Color colour;
        if (creationType == InputCorrections.CreationType.MANUAL) {
            tooltipBegin = "Manual correction";
            colour = Color.RED;
        } else {
            tooltipBegin = "Computed correction";
            colour = Color.GREEN;
        }
        String[][] attributes = new String[][] { { SOMVisualisationData.INPUT_CORRECTIONS, c.getLabel() } };
        return new ArrowPNode(creationType, attributes, tooltipBegin + " '" + c.getLabel() + "' "
                + c.getSourceUnit().printCoordinates() + " -> " + c.getTargetUnit().printCoordinates(), colour,
                sourceUnitNode.getWidth(), sourceUnitNode.getHeight(), sourceUnitNode.getPostion(),
                targetUnitNode.getPostion());
    }

}
