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

import java.util.Hashtable;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;

public class UnitKMeans extends KMeans {

    private static Hashtable<double[], GeneralUnitPNode> unitLookupTable;

    public UnitKMeans(int k, GeneralUnitPNode[][] units) {
        super(k, convert(units));
    }

    public UnitKMeans(int k, GeneralUnitPNode[][] units, InitType initialisation) {
        super(k, convert(units), initialisation);
    }

    /**
     * Convert a GeneralUnitPNode[][] to a simple doule[][].
     * 
     * @param units units to convert to.
     * @return plain double[][] data matrix.
     */
    public static double[][] convert(GeneralUnitPNode[][] units) {
        double[][] data = new double[units.length * units[0].length][];
        unitLookupTable = new Hashtable<double[], GeneralUnitPNode>();
        int i = 0;
        for (int y = 0; y < units[0].length; y++) {
            for (GeneralUnitPNode[] unit : units) {
                data[i] = unit[y].getUnit().getWeightVector();
                unitLookupTable.put(data[i], unit[y]);
                i++;
            }
        }
        return data;
    }

    /**
     * Returns the ClusterNodes for the given level. Thanks a million Angela for this prime example of programming art
     * :-)
     */
    public ClusterNode[] getClusterNodes(int level) {
        ClusterNode newNode = null;
        ClusterNode[] clusterNodes = new ClusterNode[clusters.length];
        for (int clusterIndex = 0; clusterIndex < clusters.length; clusterIndex++) {
            level--;
            double[][] instances = clusters[clusterIndex].getInstances(data);
            for (int instanceIndex = 0; instanceIndex < instances.length; instanceIndex++) {
                // create new node

                if (instanceIndex == 0) {
                    newNode = new ClusterNode(unitLookupTable.get(instances[instanceIndex]), level);
                } else {
                    newNode = new ClusterNode(newNode,
                            new ClusterNode(unitLookupTable.get(instances[instanceIndex]), 1), level);
                }
            }
            clusterNodes[clusterIndex] = newNode;
        }
        return clusterNodes;
    }
}
