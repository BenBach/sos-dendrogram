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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.EditLabelEventListener;

/**
 * Class for storing the clustering.
 * 
 * @author Angela Roiger
 * @version $Id: ClusteringTree.java 3938 2010-11-17 15:15:25Z mayer $
 */
public class ClusteringTree extends PNode implements Serializable {
    public static final float INITIAL_BORDER_WIDTH_MAGNIFICATION_FACTOR = 1.2f;

    private static final long serialVersionUID = 3918891774261635426l;

    private ClusterNode topNode;

    /** contains all currently shown clustering layers */
    private SortedMap<Integer, ClusterElementsStorage> allClusteringElements = new TreeMap<Integer, ClusterElementsStorage>();

    private static final EditLabelEventListener labelListener = new EditLabelEventListener();

    private int startFontSize;

    private int width;

    public static final Font defaultFont = new Font("Sans", Font.PLAIN, 40);

    /**
     * Initializes the tree with the given top Node.
     * 
     * @param top the top Cluster
     * @param width the width of the map in Units. The width is needed for initializing the Font size.
     */
    ClusteringTree(ClusterNode top, int width) {
        this.width = width;
        topNode = top;
        startFontSize = 6 * width; // 10-> 6
    }

    public ClusterNode findNode(int lvl) {
        return findNode(topNode, lvl);
    }

    /**
     * @return null if node could not be found
     */
    public ClusterNode findNode(ClusterNode start, int lvl) {
        if (start == null) {
            return null;
        }
        if (start.getLevel() == lvl) {
            return start;
        }
        ClusterNode node = findNode(start.getChild1(), lvl);
        if (node != null) {
            return node;
        }
        return findNode(start.getChild2(), lvl);
    }

    /**
     * Returns the borders and the Labels of the clustering into l clusters and all other currently painted cluster's
     * borders and labels
     * 
     * @param l the number of clusters
     * @return a PNode containing all borders and Labels as children
     */
    public SortedMap<Integer, ClusterElementsStorage> getClusteringInto(int l) {
        return getClusteringInto(l, false);
    }

    /**
     * Create a new allClusteringElements containing only the sticky layers from the previous version.
     */
    private void clearClusteringElements() {
        SortedMap<Integer, ClusterElementsStorage> m = new TreeMap<Integer, ClusterElementsStorage>();

        for (Integer i : allClusteringElements.keySet()) {
            ClusterElementsStorage n = allClusteringElements.get(i);
            if (n.sticky) {
                m.put(i, n);
            }
        }
        allClusteringElements = m;
    }

    /**
     * Recreates the ClusterElementsStorageNode for layer l. Replaces allClusteringElements with a new HashMap
     * containing all sticky layers from the previous version and the newly created layer l.
     * 
     * @param l the number of clusters
     * @param sticky should this clustering stay visible
     * @return the updated allClusteringElements
     */
    public SortedMap<Integer, ClusterElementsStorage> getClusteringInto(int l, boolean sticky) {

        CommonSOMViewerStateData state = CommonSOMViewerStateData.getInstance();

        clearClusteringElements();

        ArrayList<ClusterNode> clusterStorage = new ArrayList<ClusterNode>();
        ClusterElementsStorage store = new ClusterElementsStorage();
        ArrayList<PNode> allLabels = new ArrayList<PNode>();

        // to draw all clusters until level l we must find all children of clusters above "level"
        getAllChildrenUntil(l - 1, topNode, clusterStorage);

        if (state.clusterWithLabels > 0) {
            // loop through all ClusterNodes
            for (ClusterNode oneCluster : clusterStorage) {
                ClusterLabel[] tmpLabels = oneCluster.getLabels(state.clusterByValue, state.labelsWithValues);
                if (tmpLabels != null && tmpLabels.length > 0) {
                    PText labelText = null;
                    PNode bigLabel = oneCluster.getLabelNode();

                    if (bigLabel == null) {
                        int numLabelShown = Math.min(state.clusterWithLabels, tmpLabels.length);
                        bigLabel = new PNode();

                        // first(big) Label
                        labelText = new PText(tmpLabels[0].getName());
                        labelText.addAttribute("type", "clusterLabel");
                        bigLabel.addChild(labelText);

                        for (int i = 1; i < numLabelShown; i++) {
                            // other(smaller) labels:
                            labelText = new PText(tmpLabels[i].getName());
                            labelText.addAttribute("type", "smallClusterLabel");
                            bigLabel.addChild(labelText);
                        }

                        LabelPositioning.center(oneCluster, bigLabel);

                        bigLabel.addInputEventListener(labelListener);

                    } else {
                        // edit old Labels

                        // only change labeltext if something changed... to keep manually edited labels
                        int numLabelsShown = Math.min(state.clusterWithLabels, tmpLabels.length);

                        int i;
                        if (oneCluster.getWithValue() != state.labelsWithValues
                                || oneCluster.getFactorValue() != state.clusterByValue) {
                            for (i = 0; i < bigLabel.getChildrenCount(); i++) {
                                if (i < bigLabel.getChildrenCount()) {
                                    labelText = (PText) bigLabel.getChild(i);
                                    labelText.setText(tmpLabels[i].getName());
                                }
                            }
                            oneCluster.setWithValue(state.labelsWithValues);
                            oneCluster.setFactorValue(state.clusterByValue);
                        }

                        for (i = bigLabel.getChildrenCount(); i < numLabelsShown; i++) {
                            labelText = new PText(tmpLabels[i].getName());
                            labelText.addAttribute("type", "smallClusterLabel");
                            bigLabel.addChild(labelText);
                        }

                        // remove unneeded Labels
                        for (int j = numLabelsShown; j < bigLabel.getChildrenCount(); j++) {
                            bigLabel.removeChild(j);
                        }
                    }
                    allLabels.add(bigLabel);

                    oneCluster.setLabelNode(bigLabel);
                }
            }
        } else {
            allLabels = null;
        }

        // move all borders and colors to Arraylists
        store.clusterBorders = new ArrayList<PNode>();
        ClusterElementsStorage colorStore = new ClusterElementsStorage();
        colorStore.clusterColors = new ArrayList<ColoredClusterPNode>();
        for (ClusterNode element : clusterStorage) {
            store.clusterBorders.add(element.getBorder(state.clusterBorderColour));
            colorStore.clusterColors.add(element.getColoredCluster());
        }

        store.clusterLabels = allLabels;

        store.sticky = sticky;
        allClusteringElements.put(new Integer(l), store);
        resizeLabelsAndBorders(state.clusterBorderWidthMagnificationFactor);

        // store the coloring
        // highest integer value to make sure it will always be the last element when painting
        allClusteringElements.put(new Integer(Integer.MAX_VALUE), colorStore);
        return allClusteringElements;
    }

    /**
     * give some advanced debug output
     */
    public void dumpAllClusteringElements() {
        System.out.println("clustering elements dump:");
        for (Integer i : allClusteringElements.keySet()) {
            System.out.print(i + ": ");
            for (PNode pNode : allClusteringElements.get(i).clusterLabels) {
                PText label = (PText) pNode.getChild(0);
                System.out.print(label.getText() + " ");
            }
            System.out.println();
        }
    }

    // Function for recursion in resizeLabelsAndBorders()
    private void resizeLabelsAndBorders(TreeSet<Integer> ts, int fontSize, float borderWidth) {
        Integer c = ts.first();
        ts.remove(ts.first());
        // remove until the last element
        if (!ts.isEmpty()) {
            resizeLabelsAndBorders(ts, fontSize * 2 / 3, borderWidth * 2 / 3);
        }

        // now change FontSize
        ClusterElementsStorage n = allClusteringElements.get(c);
        n.changeFont(defaultFont.deriveFont(new Float(fontSize).floatValue()));

        n.changeBorderStroke(new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
    }

    /**
     * Resizes all labels so that labels of a lower layer are 1/3 smaller than the one above. The maximum font
     * size/border width is set by the constructor.
     */
    private void resizeLabelsAndBorders(float borderWidthFactor) {
        TreeSet<Integer> ts = new TreeSet<Integer>(allClusteringElements.keySet());
        int fontSize = startFontSize;
        resizeLabelsAndBorders(ts, fontSize, width * borderWidthFactor);
    }

    public SortedMap<Integer, ClusterElementsStorage> getAllClusteringElements() {
        return allClusteringElements;
    }

    /**
     * Searches the clusters that are less or equal lvl and stores their children (ClusterNodes) which are > lvl. Used
     * to get a clustering into lvl+1 clusters.
     * 
     * @param level the level.
     * @param start the top ClusterNode
     * @param store a ArrayList to store the results
     */
    private void getAllChildrenUntil(int level, ClusterNode start, ArrayList<ClusterNode> store) {
        if (start.getLevel() > level) {
            // I'm a level lvl child -- add myself and return
            store.add(start);

        } else {
            // not a level lvl child yet -- dig deeper
            getAllChildrenUntil(level, start.getChild1(), store);
            getAllChildrenUntil(level, start.getChild2(), store);
        }
    }

    /** Find the {@link ClusterNode} that contains the given {@link GeneralUnitPNode} at the given level. */
    public ClusterNode findClusterOf(GeneralUnitPNode unitPNode, int level) {
        // printTree(topNode, 0);
        ArrayList<ClusterNode> store = new ArrayList<ClusterNode>();
        getAllChildrenUntil(level - 1, topNode, store);
        for (ClusterNode clusterNode : store) {
            if (clusterNode.containsNode(unitPNode)) {
                return clusterNode;
            }
        }
        return null;
    }

    public void printTree(ClusterNode start, int x) {
        for (int i = 0; i < x; i++) {
            System.out.print("  ");
        }
        if (start != null) {
            System.out.println((x > 0 ? "--> " : "") + start.getLevel());
            printTree(start.getChild1(), x + 1);
            printTree(start.getChild2(), x + 1);
        } else {
            System.out.println("--> empty");
        }
    }

    /**
     * Adds the EditLabelEventLister to all Labels. Used after deserialization.
     */
    public void addEditLabelEventListenerToAll() {
        for (ClusterElementsStorage clusterElementsStorage : allClusteringElements.values()) {
            ArrayList<PNode> labelsArray = clusterElementsStorage.clusterLabels;

            // 16.1.07 added if
            if (labelsArray != null) {
                for (PNode labels : labelsArray) {
                    labels.addInputEventListener(labelListener);
                }
            }
        }
    }

    /**
     * Changes the colors of the tree according to the currently chosen palette. In each step the palette is split in 2
     * halves and each child cluster gets one half. The color of a cluster shown on the screen is the color
     * "in the middle" of its palette. This means in the worst case there are 2^n colors needed to paint n clusters, but
     * this also means that close clusters will have more similar colors than outliers.
     */
    public void recolorTree() {
        recolorTree(getPalette(), topNode);
    }

    /**
     * Function for recursion in recolorTree()
     * 
     * @param col Color[] contaning the Palette
     */
    private void recolorTree(Color[] col, ClusterNode n) {
        CommonSOMViewerStateData state = CommonSOMViewerStateData.getInstance();

        if (n == null) {
            return;
        }
        if (state.colorClusters) {
            n.setPaint(col[col.length / 2]);
        } else {
            n.setPaint(null);
        }

        if (col.length > 1 && state.colorClusters) {
            Color[] tmp1 = new Color[col.length / 2];
            Color[] tmp2 = new Color[col.length - col.length / 2];
            System.arraycopy(col, 0, tmp1, 0, tmp1.length);
            System.arraycopy(col, tmp1.length, tmp2, 0, tmp2.length);

            if (n.getChild1() == null) {
                return;
            }

            Point2D.Double c1 = n.getChild1().getCentroid();
            Point2D.Double c2 = n.getChild2().getCentroid();

            if (c1.x < c2.x || c1.x == c2.x && c1.y < c2.y) {
                recolorTree(tmp1, n.getChild1());
                recolorTree(tmp2, n.getChild2());
            } else {
                recolorTree(tmp2, n.getChild1());
                recolorTree(tmp1, n.getChild2());
            }
        } else {
            recolorTree(col, n.getChild1());
            recolorTree(col, n.getChild2());
        }
    }

    /**
     * Gest the current palette from the state.
     * 
     * @return the curren palette.
     */
    public Color[] getPalette() {
        CommonSOMViewerStateData state = CommonSOMViewerStateData.getInstance();
        return state.getSOMViewer().getCurrentlySelectedPalette().getColors();
    }

    // Doris
    public int[][] getClusterAssignment(int level, int xSize, int ySize) {

        int[][] assignment = new int[xSize][ySize];
        for (int a = 0; a < xSize; a++) {
            Arrays.fill(assignment[a], -1);
        }

        ArrayList<ClusterNode> list = new ArrayList<ClusterNode>();
        getAllChildrenUntil(level - 1, topNode, list);

        int clusterNo = 0;
        for (ClusterNode curcluster : list) {
            GeneralUnitPNode[] unitNodes = curcluster.getNodes();
            for (GeneralUnitPNode unitNode : unitNodes) {
                int x = unitNode.getUnit().getXPos();
                int y = unitNode.getUnit().getYPos();

                assignment[x][y] = clusterNo;
            }
            clusterNo++;
        }
        return assignment;
    }

    public ArrayList<ClusterNode> getNodesAtLevel(int level) {
        ArrayList<ClusterNode> list = new ArrayList<ClusterNode>();
        getAllChildrenUntil(level - 1, topNode, list);
        return list;
    }
}
