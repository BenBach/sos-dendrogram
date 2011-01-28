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
package at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * @author Thomas Kern
 * @author Magdalena Widl
 * @author Rudolf Mayer
 * @version $Id: Graph.java 3883 2010-11-02 17:13:23Z frank $
 */
public abstract class Graph {
    public static L2Metric metric = new L2Metric();

    // / Adjacency List
    protected TreeMap<Node, LinkedList<Edge>> adjList;

    // A list of the edges in the graph
    protected ArrayList<Edge> edges;

    protected GrowingSOM gsom;

    protected List<Edge> mst;

    protected double maximumEdgeWeight;

    protected double minimumEdgeWeight;

    public Graph(GrowingSOM gsom) {
        this.gsom = gsom;
        edges = new ArrayList<Edge>();
        adjList = new TreeMap<Node, LinkedList<Edge>>();
        minimumEdgeWeight = Double.MAX_VALUE;
        maximumEdgeWeight = -Double.MAX_VALUE;
    }

    protected abstract List<Edge> calculateEdge();

    protected void connectTwoNodes(Unit unit, HashMap<Unit, Unit> hm, Unit neighbour) {

        String lu = unit.toString();
        String lv = neighbour.toString();
        Node u = getNode(lu);
        Node v = getNode(lv);

        try {
            if (!(hm.containsKey(unit) && hm.get(unit) == neighbour || hm.containsKey(neighbour)
                    && hm.get(neighbour) == unit)) {
                insert(u, v, metric.distance(unit.getWeightVector(), neighbour.getWeightVector()));
                hm.put(unit, neighbour);
            }
        } catch (MetricException e) {
            // does not happen
            e.printStackTrace();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
        }
    }

    protected abstract void createNodes(Unit[] units);

    public abstract void drawLine(Graphics2D g, int unitWidth, int unitHeight, Edge e, boolean weighting);

    public List<Edge> getMinimumSpanningTree() {
        if (mst == null) {
            this.mst = this.calculateEdge();
        }

        return mst;
    }

    protected abstract ArrayList<Unit> getNeighbours(int horIndex, int verIndex, Unit[][] units);

    protected Node getNode(String label) {
        for (Node n : adjList.keySet()) {
            if (n.getLabel().equals(label)) {
                return n;
            }
        }
        return null;
    }

    protected void insert(Node u, Node v, double w) {

        Edge e = new Edge(u, v, w);
        adjList.get(u).add(e);
        adjList.get(v).add(new Edge(v, u, w));

        edges.add(e);
    }

    protected List<Edge> kruskalMST() {
        // Create an empty list of edges to hold the tree edges (for Kruskal)
        ArrayList<Edge> treeEdges = new ArrayList<Edge>();

        // Create a set for every node
        LinkedList<TreeSet<Node>> kruskalSets = new LinkedList<TreeSet<Node>>();
        for (Node n : adjList.keySet()) {
            TreeSet<Node> s = new TreeSet<Node>();
            s.add(n);
            kruskalSets.add(s);
        }

        // Sort edges by weight (Fancy code...)
        Collections.sort(edges, new Comparator<Edge>() {
            @Override
            public int compare(Edge e1, Edge e2) {
                return e1.getWeight() > e2.getWeight() ? 1 : -1;
            }
        });

        /* Do Kruskal */
        for (Edge e : edges) {
            Node u = e.getStart();
            Node v = e.getEnd();
            TreeSet<Node> uset = null;
            TreeSet<Node> vset = null;

            for (TreeSet<Node> s : kruskalSets) {
                if (s.contains(u)) {
                    uset = s;
                }
                if (s.contains(v)) {
                    vset = s;
                }
            }

            assert uset != null;
            if (!uset.equals(vset)) {
                uset.addAll(vset);
                kruskalSets.remove(vset);
                treeEdges.add(e);
            }
        }

        // compute extrema
        for (Edge e : treeEdges) {
            if (maximumEdgeWeight < e.getWeight()) {
                maximumEdgeWeight = e.getWeight();
            }
            if (minimumEdgeWeight > e.getWeight()) {
                minimumEdgeWeight = e.getWeight();
            }
        }

        return treeEdges;
    }

    protected int[] computeLineThickness(Edge e, int unitWidth, int unitHeight, boolean weighting) {
        // draw the line & circle approx. 1/10 of the unitWidth
        int lineWidth;
        int lineHeight;
        // apply weighting
        int lineWidthFraction = 5;
        if (weighting) {
            double relativeThickness = 1 - e.getWeight() / getMaximumEdgeWeight();
            lineWidth = Math.max((int) Math.round(unitWidth * relativeThickness / lineWidthFraction), 1);
            lineHeight = Math.max((int) Math.round(unitHeight * relativeThickness / lineWidthFraction), 1);
            // System.out.println("\t" + e + ", weight: " + e.getWeight() + ", e.getWeight() - getMinimumEdgeWeight(): "
            // + (e.getWeight() - getMinimumEdgeWeight()) + " => relativeThickness: " + relativeThickness + " => " +
            // lineWidth);
        } else {
            lineWidth = Math.round(unitWidth / lineWidthFraction);
            lineHeight = Math.round(unitHeight / lineWidthFraction);
        }
        return new int[] { lineWidth, lineHeight };
    }

    /**
     * @return Returns the maximum edge weight ({@link #maximumEdgeWeight})
     */
    public double getMaximumEdgeWeight() {
        return maximumEdgeWeight;
    }

    /**
     * @return Returns the minimum edge weight ({@link #minimumEdgeWeight})
     */
    public double getMinimumEdgeWeight() {
        return minimumEdgeWeight;
    }
}