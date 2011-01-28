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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;

/**
 * Class to create Clustering trees with the Ward's Linkage algorithm. This class is not compatible with mnemonic SOMs
 * (and probably also not compatible with hierarchical SOMs)
 * 
 * @author Angela Roiger
 * @version $Id: WardsLinkageTreeBuilderAll.java 3938 2010-11-17 15:15:25Z mayer $
 */
public class WardsLinkageTreeBuilderAll extends AbstractWardsLinkageTreeBuilder {

    // lazyUpdate is faster and should return the same results
    private boolean lazyUpdate;

    /**
     * Only use this constructor if you suspect WardsLinkageTreeBuilderAll(true) does not return a correct clustering
     */
    public WardsLinkageTreeBuilderAll() {
        this(false);
    }

    /**
     * Only use false if you suspect WardsLinkageTreeBuilderAll(true) does not return a correct clustering. The new
     * Update function only recalculates the distance values before merging two clusters. This way a lot less
     * calculations have to be made, reducing the complexity from n^3 to n^2.
     * 
     * @param lazyUpdate true to use faster update function
     */
    public WardsLinkageTreeBuilderAll(boolean lazyUpdate) {
        super();
        this.lazyUpdate = lazyUpdate;
    }

    /**
     * Calculation of the Clustering. This code is only compatible with rectangular, non hierarchical SOMs!
     * 
     * @param units the GeneralUnitPNode Array containing all the units of the SOM
     * @return the ClusteringTree (i.e. the top node of the tree)
     */
    @Override
    public ClusteringTree createTree(GeneralUnitPNode[][] units) throws ClusteringAbortedException {
        /*
         * Computational complexity (n= number of units): n^2 if the Tree builder was constructed using lazyUpdate = true; else n^3
         */

        /*
         * To make this code compatible with mnemonic soms: set this.level to the number of units used.
         */

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start Clustering ");
        this.level = units.length * units[0].length;

        // initialize monitor
        resetMonitor(2 * level);

        TreeSet<NodeDistance> dists = calculateInitialDistances(units);

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Building Tree ");

        NodeDistance toMerge;
        ClusterNode newNode = null;

        HashMap<ClusterNode, ClusterNode> parents = null; // only used for lazyUpdate

        if (lazyUpdate) {
            parents = new HashMap<ClusterNode, ClusterNode>();
        }

        while (dists.size() > 0) {
            toMerge = dists.first();
            dists.remove(toMerge);

            if (lazyUpdate) {
                // check if this is an old entry that need to be updated
                boolean dirty = false;
                for (;;) {
                    ClusterNode p = parents.get(toMerge.n1);
                    if (p == null) {
                        break;
                    }
                    toMerge.n1 = p;
                    dirty = true;
                }
                for (;;) {
                    ClusterNode p = parents.get(toMerge.n2);
                    if (p == null) {
                        break;
                    }
                    toMerge.n2 = p;
                    dirty = true;
                }
                if (toMerge.n1 == toMerge.n2) {
                    // deprecated entry
                    continue;
                }
                if (dirty) {
                    // recalculate distance, reinsert and continue the while loop
                    toMerge.dist = calcESSincrease(toMerge.n1, toMerge.n2);
                    dists.add(toMerge);
                    continue;
                }
            } // if (lazyUpdate)

            level--;
            incrementMonitor();
            allowAborting();
            newNode = new ClusterNode(toMerge.n1, toMerge.n2, level, toMerge.dist);

            if (lazyUpdate) {
                parents.put(toMerge.n1, newNode);
                parents.put(toMerge.n2, newNode);
            } else {
                HashMap<List<Object>, NodeDistance> duplicateEliminator = new HashMap<List<Object>, NodeDistance>();
                List<Object> pair;

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
                        throw new AssertionError("this should have been removed by dists.remove(toMerge)");
                        // i.remove();
                    }

                    // & keep only the shortest distance for each connection
                    if (x.n1 == newNode || x.n2 == newNode) {

                        if (x.n1 == newNode) { // make pair where new node is first
                            pair = Arrays.asList(new Object[] { x.n1, x.n2 });
                        } else {
                            pair = Arrays.asList(new Object[] { x.n2, x.n1 });
                        }
                        // keep only shorter distance
                        if (!duplicateEliminator.containsKey(pair)) {

                            x.dist = calcESSincrease(x.n1, x.n2);

                            duplicateEliminator.put(pair, x);
                        } else {

                        }
                        i.remove();

                    }
                }

                dists.addAll(duplicateEliminator.values());
            } // if (lazyUpdate) else
        }
        finishMonitor();
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished Clustering - Wards Linkage (all)");

        return new ClusteringTree(newNode, units.length);
    }

    /**
     * Calculates the initial distances from each To each other unit where there is not yet a distance calculated. This
     * results in n^2/2 distance calculations and the same amount of created objects. Can take long in case of large
     * maps. This code is only compatible with rectangular, non hierarchical SOMs!
     * 
     * @param units A GeneralUnitPNode[][] containing the Units of the som
     * @return a TreeSet of NodeDistances containing the distances between the units starting with the smallest.
     */
    private TreeSet<NodeDistance> calculateInitialDistances(GeneralUnitPNode[][] units)
            throws ClusteringAbortedException {

        /*
         * To make this code compatible with mnemonic soms: Take care only distances between existing units are calculated only calculate (store) the
         * distance between 2 clusters once
         */

        int xdim = units.length;
        int ydim = units[0].length;

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
                    for (int k = i; k < xdim; k++) {
                        int start = 0; // start at the beginning of the row...
                        if (k == i) { // unless it's my own row ...
                            start = j + 1; // then start with the next item.
                        }
                        for (int l = start; l < ydim; l++) {
                            dists.add(new NodeDistance(tmp[i][j], tmp[k][l], calcESSincrease(tmp[i][j], tmp[k][l])));
                        }
                    }

                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Cannot create clustering: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        }
        return dists;
    }

    @Override
    public String getClusteringAlgName() {
        return "Ward's Linkage (all)";
    }

}
