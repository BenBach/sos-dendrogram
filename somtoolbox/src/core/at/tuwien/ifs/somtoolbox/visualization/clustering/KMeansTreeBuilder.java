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

import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.visualization.clustering.KMeans.InitType;

/**
 * Builds a cluster tree using K-Means.
 * 
 * @author Robert Neumayer
 * @version $Id: KMeansTreeBuilder.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class KMeansTreeBuilder extends NonHierarchicalTreeBuilder {

    private InitType initType;

    public KMeansTreeBuilder() {
        this.initType = InitType.RANDOM;
    }

    @Override
    public ClusteringTree createTree(GeneralUnitPNode[][] units) throws ClusteringAbortedException {
        int k = 1;
        ClusteringTree tree = createTree(units, k);
        super.cache.put(k, tree);
        return tree;
    }

    @Override
    public ClusteringTree createTree(GeneralUnitPNode[][] units, int k) throws ClusteringAbortedException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Started k-means clustering, k=" + k);

        this.level = units.length * units[0].length;

        // get data for clustering
        // call kmeans
        // create clusternodes out of clusters and a clustertree out of clusternodes

        UnitKMeans ukm = new UnitKMeans(k, units, initType);
        ukm.train();
        ukm.printClusterIndices();
        // ukm.getClusterNode();

        ClusterNode newNode = null;
        ClusterNode[] clusterNodes = ukm.getClusterNodes(level);
        newNode = clusterNodes[0];
        resetMonitor(k);
        for (int i = 1; i < k; i++) {
            incrementMonitor();
            newNode = new ClusterNode(newNode, clusterNodes[i], i);
        }
        finishMonitor();
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished Clustering - KMeans");
        return new ClusteringTree(newNode, units.length);

    }

    public void reInit(InitType type) {
        cache.clear();
        this.initType = type;
    }

    @Override
    public String getClusteringAlgName() {
        return "k-Means";
    }
}