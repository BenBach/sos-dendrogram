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

import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PNode;

/**
 * Methods for positioning the label inside a cluster.
 * 
 * @author Angela Roiger
 * @version $Id: LabelPositioning.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class LabelPositioning {

    /**
     * Place the label in the center of the surrounding rectangle of the cluster.
     */
    public static void center(ClusterNode cluster, PNode label) {
        label.setWidth(label.getChild(0).getWidth());
        label.setHeight(label.getChild(0).getHeight());
        label.centerFullBoundsOnPoint(cluster.getX() + cluster.getWidth() / 2, cluster.getY() + cluster.getHeight() / 2);

    }

    /**
     * Place the label in the centroid of the cluster.
     */
    public static void centroid(ClusterNode cluster, PNode label) {
        Point2D.Double centroid = cluster.getCentroid();
        label.setOffset(centroid.x, centroid.y);
    }

}
