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

/**
 * Used to store a distance between two clusters. The order of the Objects is by ascending distances. If two distances
 * are equal, the Object with the lower hash code comes first.
 * 
 * @author Angela Roiger
 * @version $Id: NodeDistance.java 3883 2010-11-02 17:13:23Z frank $
 */
class NodeDistance implements Comparable<NodeDistance> {

    ClusterNode n1;

    ClusterNode n2;

    // double -> float .. to save space
    double dist;

    public NodeDistance(ClusterNode no1, ClusterNode no2, double d) {
        this.n1 = no1;
        this.n2 = no2;
        this.dist = (float) d;
    }

    /**
     * Compare this Node distance to another Object. Two distances are equal if all their components (nodes & distance
     * value) are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof NodeDistance) {
            NodeDistance tmp = (NodeDistance) o;
            return this.n1 == tmp.n1 && this.n2 == tmp.n2 && this.dist == tmp.dist;
        }
        return false;
    }

    /**
     * Must be equal if the objects are equal according to nodeDistance.equal. Should not be equal otherwise.
     */
    @Override
    public int hashCode() {
        return this.n1.hashCode() + 2 * this.n2.hashCode();
    }

    @Override
    public int compareTo(NodeDistance o) throws ClassCastException {
        if (this.equals(o)) {
            return 0;
        }
        if (this.dist == o.dist) {
            return new Integer(this.hashCode()).compareTo(o.hashCode());
        } else {
            return Double.compare(this.dist, o.dist);
        }
    }
}
