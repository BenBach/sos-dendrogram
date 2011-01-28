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

/**
 * This is an abstract class for non-hierarchical tree builders, which need to build a new clustering tree for each
 * level. This class provides caching of already computed cluster levels.
 * 
 * @author Rudolf Mayer
 * @version $Id: NonHierarchicalTreeBuilder.java 3583 2010-05-21 10:07:41Z mayer $
 */
public abstract class NonHierarchicalTreeBuilder extends TreeBuilder {
    protected Hashtable<Integer, ClusteringTree> cache = new Hashtable<Integer, ClusteringTree>();

    public abstract ClusteringTree createTree(GeneralUnitPNode[][] units, int k) throws ClusteringAbortedException;

    public ClusteringTree getTree(GeneralUnitPNode[][] units, int k) throws ClusteringAbortedException {
        if (cache.get(k) == null) {
            cache.put(k, createTree(units, k));
        }
        return cache.get(k);
    }

}