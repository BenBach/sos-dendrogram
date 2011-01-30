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
package at.tuwien.ifs.somtoolbox.visualization.clustering;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.layers.Label;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import org.apache.commons.lang.ArrayUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.*;

/**
 * Class containing one node in the cluster tree.
 * 
 * @author Angela Roiger
 * @version $Id: ClusterNode.java 3938 2010-11-17 15:15:25Z mayer $
 */
public class ClusterNode implements Serializable {


    public static final Color INTIAL_BORDER_COLOUR = Color.BLACK;

    private Color borderColor = INTIAL_BORDER_COLOUR;
    private Color selectedBorderColor = Color.RED;

    // increment serialVersionUID if class changes so it is incompatible with previous versions (-> [de]serialization )
    private static final long serialVersionUID = 2L;

    private GeneralUnitPNode[] unitNodes;

    private BorderPNode border = null;

    private ColoredClusterPNode colorNode = new ColoredClusterPNode(this);

    private PNode labelNode = null;

    private int level;

    private ClusterNode child1 = null;

    private ClusterNode child2 = null;

    private boolean labelContainsValues = false;

    private double factorValue = Double.NaN;

    private ClusterLabel[] labels = null;

    private double mergeCost;

    int numberOfInputs;

    private double centroidX = 0;

    private double centroidY = 0;

    // to store centroid of the weight vectors
    private double[] mean = null;

    // bounding rectangle:
    private double x;

    private double y;

    private double width;

    private double height;

    private boolean isSelected;

    /**
     * Returns the mean vector of the cluster's weight vectors. Calculates it if it is not set yet.
     * 
     * @return the mean vector of the cluster's weight vectors
     */
    public double[] getMeanVector() {

        int weightVectorLength = getUnitNodes()[0].getUnit().getWeightVector().length;
        if (mean == null) {
            mean = new double[weightVectorLength];

            for (int j = 0; j < weightVectorLength; j++) {
                double sum = 0;
                for (GeneralUnitPNode unitNode : getUnitNodes()) {

                    sum = sum + unitNode.getUnit().getWeightVector()[j];
                }

                mean[j] = sum / getUnitNodes().length;
            }
        }
        return mean;
    }

    /**
     * @return the Centroid of the cluster on the map
     */
    public Point2D.Double getCentroid() {
        Point2D.Double centroid;
        if (centroidX == 0.0d && centroidY == 0.0d) {
            double x = 0;
            double y = 0;
            int i;
            for (i = 0; i < getUnitNodes().length; i++) {
                x = x + getUnitNodes()[i].getX();
                y = y + getUnitNodes()[i].getY();
            }
            x = x / i + getUnitNodes()[0].getWidth() / 2;
            y = y / i + getUnitNodes()[0].getHeight() / 2;
            centroid = new Point2D.Double(x, y);
            centroidX = x;
            centroidY = y;
        } else {
            centroid = new Point2D.Double(centroidX, centroidY);
        }
        return centroid;
    }

    public void setLabelNode(PNode n) {
        this.labelNode = n;
    }

    public PNode getLabelNode() {
        return this.labelNode;
    }

    /**
     * Connects two ClusterNodes to one cluster
     * 
     * @param level The level of the new cluster.
     */
    public ClusterNode(ClusterNode n1, ClusterNode n2, int level) {
        this.level = level;
        this.setUnitNodes(new GeneralUnitPNode[n1.getNodes().length + n2.getNodes().length]);

        for (int i = 0; i < n1.getNodes().length; i++) {
            this.getUnitNodes()[i] = n1.getNodes()[i];
        }
        for (int i = 0; i < n2.getNodes().length; i++) {
            this.getUnitNodes()[i + n1.getNodes().length] = n2.getNodes()[i];
        }
        this.child1 = n1;
        this.child2 = n2;
        this.border = makeBorder();

        // Adding border as child of the clusternode... makes problems with colored clusters
        // addChild(border);

        // Bounds - for label placement
        // bounds = rectangle around cluster nodes

        if (n1.getX() < n2.getX()) {
            this.setX(n1.getX());
        } else {
            this.setX(n2.getX());
        }
        if (n1.getY() < n2.getY()) {
            this.setY(n1.getY());
        } else {
            this.setY(n2.getY());
        }

        this.setWidth(Math.max(n1.getX() + n1.getWidth(), n2.getX() + n2.getWidth()) - Math.min(n1.getX(), n2.getX()));
        this.setHeight((Math.max(n1.getY() + n1.getHeight(), n2.getY() + n2.getHeight()) - Math.min(n1.getY(),
                n2.getY())));
        this.colorNode.setBounds(this.getX(), this.getY(), this.getWidth(), this.getHeight());

        this.numberOfInputs = n1.getNumberOfInputs() + n2.getNumberOfInputs();
    }

    public ClusterNode(ClusterNode n1, ClusterNode n2, int level, double mergeCost) {
        this(n1, n2, level);
        this.mergeCost = mergeCost;
    }

    /**
     * Sets this.label and this.numberOfInputs for this cluster. Used only during initialisation when there is only one
     * node per cluster.
     * 
     * @param node the GeneralUnitPNode inside this "Cluster"
     */
    private void writeLabelInfos(GeneralUnitPNode node) {
        Unit u = node.getUnit();
        this.numberOfInputs = u.getNumberOfMappedInputs();
    }

    /**
     * Creates an initial ClusterNode with only one Node inside.
     * 
     * @param leaf the GeneralUnitPNode to be put in the cluster
     * @param level a number >= the number of total units.
     */
    public ClusterNode(GeneralUnitPNode leaf, int level) {
        setUnitNodes(new GeneralUnitPNode[1]);
        getUnitNodes()[0] = leaf;
        border = makeBorder();

        this.level = level;

        this.setX(leaf.getX());
        this.setY(leaf.getY());
        this.setHeight(leaf.getHeight());
        this.setWidth(leaf.getWidth());
        this.colorNode.setBounds(this.getX(), this.getY(), this.getWidth(), this.getHeight());

        writeLabelInfos(leaf);
    }

    /**
     * Calculates and sets new Labels with the given parameters. After calling this function you have to set factorValue
     * and labelContainsValues.
     * 
     * @param factorValue A double value between 0 and 1 to determine how much the mean value should influence the
     *            label.
     * @param factorQe A double value between 0 and 1 to determine how much the qe value should influence the label.
     * @param factorNumber Currently unused. Should determine the influence of the number of input vectors of the
     *            clusters.
     * @param withValue set to true if you want a label with values
     */
    private void calcLabel(double factorValue, double factorQe, double factorNumber, boolean withValue) {
        // parameter?? anz inputs(nicht verwendet)

        TreeMap<String, Label> allLabels = new TreeMap<String, Label>();
        double maxVal = 0;
        double maxQe = 0;
        int count = 0; // number of units containing inputs

        Label[] tmpLabels;// = new Label[5];
        for (GeneralUnitPNode unitNode : getUnitNodes()) {
            int num = unitNode.getUnit().getNumberOfMappedInputs();
            if (num > 0) {
                count++;
            }
            tmpLabels = unitNode.getLabels(Unit.LABELSOM);// change if other types of Labels should also be used
            if (tmpLabels != null) {
                Label tmpLabel;
                for (Label tmpLabel2 : tmpLabels) {
                    tmpLabel = new Label(tmpLabel2.getName(), tmpLabel2.getValue(), tmpLabel2.getQe());

                    if (tmpLabel.getValue() > maxVal) {
                        maxVal = tmpLabel.getValue();
                    }
                    if (tmpLabel.getQe() > maxQe) {
                        maxQe = tmpLabel.getQe();
                    }

                    if (allLabels.containsKey(tmpLabel.getName())) {
                        Label duplicate = allLabels.remove(tmpLabel.getName());
                        tmpLabel = new Label(tmpLabel.getName(), (tmpLabel.getValue() + duplicate.getValue()),
                                (tmpLabel.getQe() + duplicate.getQe()) / 2);
                    }
                    allLabels.put(tmpLabel.getName(), tmpLabel);
                }
            }
        }

        // 
        // beruecksichigen? anz inputs oder anz units mit inputs

        TreeSet<ClusterLabel> tmp = new TreeSet<ClusterLabel>();

        for (Label l : allLabels.values()) {
            if (!withValue) {
                tmp.add(new ClusterLabel(l.getName(), l.getValue() / count, l.getQe() / count, 0 - (l.getValue()
                        / (count * maxVal) * factorValue + (1 - l.getQe() / maxQe) * factorQe)));
            } else {
                tmp.add(new ClusterLabel(l.getName() + "\n" + StringUtils.format(l.getValue() / count, 3), l.getValue()
                        / count, l.getQe() / count, 0 - (l.getValue() * factorValue / (count * maxVal) + (1 - l.getQe()
                        / maxQe)
                        * factorQe)));
            }
        }

        this.labels = tmp.toArray(new ClusterLabel[tmp.size()]);
    }

    /**
     * returns current label or null in case label is not set
     */
    public ClusterLabel[] getLabels() {
        return labels;
    }

    /**
     * calculates and sets the current labels in case label is not set yet or the values have changed and returns it
     */
    public ClusterLabel[] getLabels(double fValue, boolean withValue) {
        if (labels == null) {
            calcLabel(fValue, 1 - fValue, 1, withValue);
        }
        if (this.factorValue != fValue) {
            calcLabel(fValue, 1 - fValue, 1, withValue);
        }
        if (withValue != this.labelContainsValues) {
            calcLabel(fValue, 1 - fValue, 1, withValue);
        }

        return labels;
    }

    /**
     * Returns the number of input vectors inside this cluster
     * 
     * @return the number of input vectors
     */
    public int getNumberOfInputs() {
        return numberOfInputs;
    }

    public double getFactorValue() {
        return this.factorValue;
    }

    /**
     * Does the Label of this Cluster contain a value
     */
    public boolean getWithValue() {
        return this.labelContainsValues;
    }

    /**
     * Set the factor for the value in calculation of the labels - beween 0 and 1. The factor for the qe is
     * automatically calculated as 1-d
     * 
     * @param d the factor for the value
     */
    public void setFactorValue(double d) {
        this.factorValue = d;
    }

    /**
     * Should the label contain a value in the text.
     */
    public void setWithValue(boolean b) {
        this.labelContainsValues = b;
    }

    /**
     * returns the first child cluster
     */
    public ClusterNode getChild1() {
        return child1;
    }

    /**
     * returns the second child cluster
     */
    public ClusterNode getChild2() {
        return child2;
    }

    /**
     * in which level of the clustering tree is this node (1 = top node)
     */
    public int getLevel() {
        return this.level;
    }

    /**
     * Returns all the {@link GeneralUnitPNode}s contained in this cluster
     */
    public GeneralUnitPNode[] getNodes() {
        return this.getUnitNodes();
    }

    public boolean containsNode(GeneralUnitPNode node) {
        return ArrayUtils.contains(this.getUnitNodes(), node);
    }

    public boolean containsAllNodes(Collection<GeneralUnitPNode> nodes) {
        if (nodes.size() != getUnitNodes().length) {
            return false;
        }
        for (GeneralUnitPNode node : nodes) {
            if (!ArrayUtils.contains(this.getUnitNodes(), node)) {
                return false;
            }
        }
        return true;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * Returns a border for this cluster
     * 
     * @return PNode containing the borders as children
     */
    private BorderPNode makeBorder() {
        ArrayList<Rectangle2D> lines = new ArrayList<Rectangle2D>();
        for (GeneralUnitPNode u : getUnitNodes()) {
            double left = u.getX();
            double right = u.getX() + u.getWidth();
            double top = u.getY();
            double bottom = u.getY() + u.getHeight();
            xorBorderLine(lines, left, top, right, top); // top line
            xorBorderLine(lines, left, bottom, right, bottom); // bottom line
            xorBorderLine(lines, left, top, left, bottom); // left line
            xorBorderLine(lines, right, top, right, bottom); // right line
        }

        BorderPNode border = new BorderPNode();
        for (Rectangle2D rect : lines) {
            PPath line = PPath.createLine((float) rect.getMinX(), (float) rect.getMinY(), (float) rect.getMaxX(),
                    (float) rect.getMaxY());
            line.setStrokePaint(isSelected ? selectedBorderColor : borderColor);
            border.addChild(line);
        }
        return border;
    }

    public PNode getBorder(Color borderColor) {
        this.borderColor = borderColor;
        this.border = makeBorder();
        return border;
    }

    /**
     * @return the border elements of this cluster in a PNode
     */
    public PNode getBorder() {
        if (this.border == null) {
            this.border = makeBorder();
        }
        return this.border;
    }

    // add the line to "lines" if it does not exist yet, otherwise remove it (i.e. do the XOR)
    private static void xorBorderLine(ArrayList<Rectangle2D> lines, double x1, double y1, double x2, double y2) {
        Rectangle2D rect = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
        if (lines.contains(rect)) {
            lines.remove(rect);
        } else {
            lines.add(rect);
        }
    }

    /**
     * Sets the color of the cluster.
     * 
     * @param newPaint The new color of the cluster
     */
    public void setPaint(Paint newPaint) {
        colorNode.setPaint(newPaint);
    }

    public ColoredClusterPNode getColoredCluster() {
        return this.colorNode;
    }

    /**
     * get/set Height/Width/X/Y: used for the bounding rectangle of the cluster.
     */
    public double getHeight() {
        return height;
    }

    /**
     * get/set Height/Width/X/Y: used for the bounding rectangle of the cluster.
     */

    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * get/set Height/Width/X/Y: used for the bounding rectangle of the cluster.
     */
    public double getWidth() {
        return width;
    }

    /**
     * get/set Height/Width/X/Y: used for the bounding rectangle of the cluster.
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * get/set Height/Width/X/Y: used for the bounding rectangle of the cluster.
     */
    public double getX() {
        return x;
    }

    /**
     * get/set Height/Width/X/Y: used for the bounding rectangle of the cluster.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * get/set Height/Width/X/Y: used for the bounding rectangle of the cluster.
     */
    public double getY() {
        return y;
    }

    /**
     * get/set Height/Width/X/Y: used for the bounding rectangle of the cluster.
     */
    public void setY(double y) {
        this.y = y;
    }

    /** Changes the colour of the cluster borders */
    public void setBorderColor(Color c) {
        this.borderColor = c;
    }

    /** @return Returns the mergeCost. */
    public double getMergeCost() {
        return mergeCost;
    }

    /**
     * @param unitNodes The unitNodes to set.
     */
    public void setUnitNodes(GeneralUnitPNode[] unitNodes) {
        this.unitNodes = unitNodes;
    }

    /**
     * @return Returns the unitNodes.
     */
    public GeneralUnitPNode[] getUnitNodes() {
        return unitNodes;
    }

}
