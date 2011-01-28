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
package at.tuwien.ifs.somtoolbox.clustering;

import java.util.ArrayList;
import java.util.HashMap;

import prefuse.data.Tree;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * @author Rudolf Mayer
 * @version $Id: HierarchicalClusteringAlgorithm.java 3932 2010-11-09 16:56:38Z mayer $
 * @param <E> Class in the cluster
 */
public interface HierarchicalClusteringAlgorithm<E> extends ClusteringAlgorithm<E> {

    public Tree getPrefuseTree();

    /** Returns the clusters at all levels */
    public HashMap<Integer, ArrayList<HierarchicalCluster<E>>> getClustersAtLevel() throws SOMToolboxException;

    /** Returns the cluster at a certain level, where the level equals the number of clusters */
    public ArrayList<HierarchicalCluster<E>> getClustersAtLevel(int num) throws SOMToolboxException;

}
