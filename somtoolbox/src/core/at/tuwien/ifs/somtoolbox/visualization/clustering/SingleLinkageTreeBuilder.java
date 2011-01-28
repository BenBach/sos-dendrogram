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

import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * Single Linkage Clustering Algorithm. This is an adapted version only calculating the distances to the direct
 * neighbours. This class is not compatible with mnemonic SOMs (and probably also not compatible with hierarchical SOMs)
 * 
 * @author Angela Roiger
 * @version $Id: SingleLinkageTreeBuilder.java 3938 2010-11-17 15:15:25Z mayer $
 */
public class SingleLinkageTreeBuilder extends TreeBuilder {

    /**
     * Calculation of the Clustering. This code is only compatible with rectangular, non hierarchical SOMs!
     * 
     * @param units the GeneralUnitPNode Array containing all the units of the SOM
     * @return the ClusteringTree (i.e. the top node of the tree)
     */
    @Override
    public ClusteringTree createTree(GeneralUnitPNode[][] units) throws ClusteringAbortedException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start Clustering ");
        this.level = units.length * units[0].length;

        // initialize monitor
        resetMonitor(2 * level);

        TreeSet<NodeDistance> dists = calculateNearestDistances(units);

        NodeDistance toMerge;
        ClusterNode newNode = null;
        while (dists.size() > 0) {
            toMerge = dists.first();
            // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Shortest: "+tmpDist.dist);

            dists.remove(toMerge);
            level--;
            incrementMonitor();
            allowAborting();
            newNode = new ClusterNode(toMerge.n1, toMerge.n2, level, toMerge.dist);

            // HashMap duplicateEliminator = new HashMap();

            // remove not needed connections and change distances from n1,n2 to newNode
            for (Iterator<NodeDistance> i = dists.iterator(); i.hasNext();) {
                NodeDistance x = i.next();
                if (x.n1 == toMerge.n1 || x.n1 == toMerge.n2) {
                    x.n1 = newNode;
                }
                if (x.n2 == toMerge.n1 || x.n2 == toMerge.n2) {
                    x.n2 = newNode;
                }
                if (x.n1 == x.n2) {
                    i.remove();
                }

                //                
                // // & keep only the shortest distance for each connection
                // if ((x.n1 == newNode) || (x.n2 == newNode)) {
                //
                // if (x.n1 == newNode) { // make pair where new node is first
                // pair = Arrays.asList(new Object[] { x.n1, x.n2 });
                // } else {
                // pair = Arrays.asList(new Object[] { x.n2, x.n1 });
                // }
                // // keep only shorter distance
                // if (!duplicateEliminator.containsKey(pair)) {
                //
                // duplicateEliminator.put(pair, x);
                // } else {
                //
                // }
                // i.remove();
                //
                // }

            }
            // dists.addAll(duplicateEliminator.values());
        }
        finishMonitor();
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished Clustering - Single Linkage");

        return new ClusteringTree(newNode, units.length);
    }

    /**
     * Calculates the distances from each unit to its neighbours to the right, bottom and bottom-right.
     * 
     * @param units A GeneralUnitPNode[][] containing the Units of the som
     * @return a TreeSet of NodeDistances containing the distances between the units starting with the smallest.
     */
    private TreeSet<NodeDistance> calculateNearestDistances(GeneralUnitPNode[][] units)
            throws ClusteringAbortedException {
        int xdim = units.length;
        int ydim = units[0].length;

        // Angela: Pfusch :)
        L2Metric l1 = new L2Metric();

        ClusterNode[][] tmp = new ClusterNode[xdim][ydim];
        TreeSet<NodeDistance> dists = new TreeSet<NodeDistance>();

        // create all basic Nodes
        for (int i = 0; i < xdim; i++) {
            for (int j = 0; j < ydim; j++) {
                tmp[i][j] = new ClusterNode(units[i][j], level);
            }
        }

        // calculate initial distances:
        for (int i = 0; i < xdim; i++) {
            for (int j = 0; j < ydim; j++) {
                incrementMonitor();
                allowAborting();
                try {
                    if (i < xdim - 1) {
                        dists.add(new NodeDistance(tmp[i][j], tmp[i + 1][j], l1.distance(
                                units[i][j].getUnit().getWeightVector(), units[i + 1][j].getUnit().getWeightVector())));
                        // dists.add(new NodeDistance(tmp[i][j],tmp[i+1][j], l1.distance(
                        // Normalization.normalizeVectorToUnitLength(units[i][j].getWeightVector()),
                        // Normalization.normalizeVectorToUnitLength(units[i+1][j].getWeightVector()))) );
                        // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Distance: "+ i +","+ j +","+ (i+1) +","+ j
                        // +":"+l1.distance(units[i][j].getWeightVector(),units[i+1][j].getWeightVector()));
                    }
                    if (j < ydim - 1) {
                        dists.add(new NodeDistance(tmp[i][j], tmp[i][j + 1], l1.distance(
                                units[i][j].getUnit().getWeightVector(), units[i][j + 1].getUnit().getWeightVector())));
                        // dists.add(new
                        // NodeDistance(tmp[i][j],tmp[i][j+1],l1.distance(Normalization.normalizeVectorToUnitLength(units[i][j].getWeightVector()),Normalization.normalizeVectorToUnitLength(units[i][j+1].getWeightVector())))
                        // );
                        // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Distance: "+ i +","+ j +","+ i +","+ (j+1)
                        // +":"+l1.distance(units[i][j].getWeightVector(),units[i][j+1].getWeightVector()));
                        if (i < xdim - 1) {
                            dists.add(new NodeDistance(tmp[i][j], tmp[i + 1][j + 1], l1.distance(
                                    units[i][j].getUnit().getWeightVector(),
                                    units[i + 1][j + 1].getUnit().getWeightVector())));
                        }
                    }
                    if (j > 1 && i < xdim - 1) {
                        dists.add(new NodeDistance(tmp[i][j], tmp[i + 1][j - 1], l1.distance(
                                units[i][j].getUnit().getWeightVector(),
                                units[i + 1][j - 1].getUnit().getWeightVector())));
                    }
                } catch (MetricException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Cannot create clustering: " + e.getMessage());
                }
            }
        }

        // // calculate initial distances v2:
        // for (int i = 0; i < xdim; i++) {
        // for (int j = 0; j < ydim; j++) {
        // incrementMonitor();
        // allowAborting();
        // try {
        // for (int k = i; k < xdim; k++) {
        // int start = 0; // start at the beginning of the row...
        // if (k == i) { // unless it's my own row ...
        // start = j + 1; // then start with the next item.
        // }
        // for (int l = start; l < ydim; l++) {
        // dists.add(new NodeDistance(tmp[i][j], tmp[k][l], l1.distance(
        // units[i][j].getUnit().getWeightVector(), units[k][l].getUnit().getWeightVector())));
        // }
        // }
        //
        // } catch (Exception e) {
        // Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Cannot create clustering: " + e.getMessage());
        // e.printStackTrace();
        // }
        // }
        //
        // }

        return dists;
    }

    @Override
    public String getClusteringAlgName() {
        return "SingleLinkage";
    }

}
