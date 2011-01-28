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

import org.apache.commons.lang.StringUtils;

import prefuse.data.Node;
import prefuse.data.Tree;

public class HierarchicalCluster<E> extends Cluster<E> {
    public static final String COLUMN_NAME_LEVEL = "level";

    public static final String COLUMN_NAME_CONTENT = "content";

    public static final String COLUMN_NAME_CONTENT_LONG = "contentLong";

    private double mergeCost;

    private HierarchicalCluster<E> leftNode;

    private HierarchicalCluster<E> rightNode;

    int size = -1; // cache of computation

    public HierarchicalCluster(E datum, String label) {
        super(datum, label);
    }

    public HierarchicalCluster(HierarchicalCluster<E> cluster1, HierarchicalCluster<E> cluster2) {
        super();
        data.addAll(cluster1.getData());
        data.addAll(cluster2.getData());
        this.label = cluster1.getLabel() + " + " + cluster2.getLabel();
        this.leftNode = cluster1;
        this.rightNode = cluster2;
    }

    public Node buildPrefuseTree(Tree tree, Node root) {
        Node n = tree.addChild(root);
        if (isLeaf()) {
            n.setString(COLUMN_NAME_LEVEL, toString());
            n.setString(COLUMN_NAME_CONTENT, shortContentToString());
            n.setString(COLUMN_NAME_CONTENT_LONG, contentToString());
        } else {
            n.setString(COLUMN_NAME_LEVEL, leftNode.toString() + " + " + rightNode.toString());
            n.setString(COLUMN_NAME_CONTENT, shortContentToString());
            n.setString(COLUMN_NAME_CONTENT_LONG, contentToString());
            leftNode.buildPrefuseTree(tree, n);
            rightNode.buildPrefuseTree(tree, n);
        }
        return n;
    }

    /** Cuts off the string when the content information is too long, but adds the number of surpressed cluster names. */
    private String shortContentToString() {
        final String res = contentToString();
        final int max = 75;
        if (res.length() < max) {
            return res;
        } else {
            String res2 = res.substring(0, max - 5);
            int endPoint = res2.lastIndexOf(CONTENT_SEPARATOR_CHAR);
            res2 = res2.substring(0, endPoint);
            final String res3 = res.substring(endPoint);
            final int more = StringUtils.countMatches(res3, CONTENT_SEPARATOR_CHAR);
            return res2 + " ... (" + more + " more)";
        }
    }

    public HierarchicalCluster<E> getLeftNode() {
        return leftNode;
    }

    public double getMergeCost() {
        return mergeCost;
    }

    public double getMergeCostIncrease() {
        if (isLeaf()) {
            return getMergeCost();
        } else {
            return getMergeCost() - (leftNode.getMergeCost() + rightNode.getMergeCost());
        }
    }

    public HierarchicalCluster<E> getRightNode() {
        return rightNode;
    }

    public boolean isLeaf() {
        return leftNode == null && rightNode == null;
    }

    public void setMergeCost(double mergeCost) {
        this.mergeCost = mergeCost;
    }

    public int depth() {
        if (isLeaf()) { // leaf
            return 1;
        } else {
            // return left.size() + right.size();
            if (size == -1) {
                size = leftNode.size() + leftNode.size();
            }
            return size;
        }
    }

    @Override
    public String toString() {
        return "Cluster level #" + depth();
    }

    public boolean contains(E element) {
        return getLeftNode() == element || getRightNode() == element || getLeftNode().contains(element)
                || getRightNode().contains(element);
    }

}
