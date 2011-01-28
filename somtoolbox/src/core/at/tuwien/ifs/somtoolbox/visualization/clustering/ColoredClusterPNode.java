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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;

/**
 * Class used to paint the clusters on the map. Each ColoredClusterPNode is associated with one ClusterNode.
 * 
 * @author Angela Roiger
 * @version $Id: ColoredClusterPNode.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class ColoredClusterPNode extends PNode {
    private static final long serialVersionUID = 1L;

    private ClusterNode correspondingCluster;

    public ColoredClusterPNode(ClusterNode cluster) {
        super();
        correspondingCluster = cluster;
    }

    /**
     * Fills the all Units ({@link GeneralUnitPNode}) inside the Cluster with Color. Replaces the paint method from
     * {@link PNode}.
     */
    @Override
    protected void paint(PPaintContext paintContext) {

        GeneralUnitPNode[] unitNodes = correspondingCluster.getNodes();

        if (this.getPaint() != null) {
            Graphics2D g2d = paintContext.getGraphics();
            g2d.setPaint(this.getPaint());
            for (GeneralUnitPNode u : unitNodes) {
                g2d.fill(new Rectangle2D.Double(u.getX(), u.getY(), u.getWidth(), u.getHeight()));
            }
        }
    }
}
