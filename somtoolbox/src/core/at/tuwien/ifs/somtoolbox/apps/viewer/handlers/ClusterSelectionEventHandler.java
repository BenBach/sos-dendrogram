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
package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.logging.Logger;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterNode;

/**
 * EventHandler for selecting clusters by rectangle.
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: ClusterSelectionEventHandler.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class ClusterSelectionEventHandler extends OrderedPSelectionEventHandler {

    private String selectionStatusString = "Selected Units:";

    private MapPNode mapPNode;

    private ArrayList<GeneralUnitPNode> markedNodes = new ArrayList<GeneralUnitPNode>();

    public ClusterSelectionEventHandler(PNode marqueeParent, PNode selectableParents) {
        super(marqueeParent, selectableParents);
        if (selectableParents instanceof MapPNode) { // e.g. in the ComponentPlaneClusteringFrame, we do not have a
            // MapPNode, just a PNode
            this.mapPNode = (MapPNode) selectableParents;
            addSelectableParent(mapPNode.getUnitsNode()); // we need to specifically add the sub-node of MapPNode that
            // contains the GeneralUnitPNodes
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Not setting mapPNode in ClusterSelectionEventHandler");
        }
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));
        setMarqueePaint(Color.red);
        setMarqueePaintTransparency(0.3f);
    }

    @Override
    public void decorateSelectedNode(PNode node) {
        // do nothing now
        if (GeneralUnitPNode.class.isInstance(node)) {
            GeneralUnitPNode upn = (GeneralUnitPNode) node;
            upn.setSelected(true);
            selectionStatusString += " (" + upn.getUnit().getXPos() + "/" + upn.getUnit().getYPos() + ")";
            Logger.getLogger("at.tuwien.ifs.somtoolbox").finer(selectionStatusString);
        }
    }

    @Override
    public void undecorateSelectedNode(PNode node) {
        if (node instanceof GeneralUnitPNode) {
            ((GeneralUnitPNode) node).setSelected(false);
            selectionStatusString = "Selected Units:";
        }
    }

    @Override
    protected boolean isMarqueeSelection(PInputEvent e) {
        return true;
    }

    @Override
    protected void drag(PInputEvent e) {
        markCluster(e);
        markedNodes.clear();
        super.drag(e);
    }

    @Override
    public void mouseReleased(PInputEvent e) {
        super.mouseReleased(e);
        markedNodes.clear();
    }

    @Override
    public void mouseClicked(PInputEvent event) {
        markCluster(event);
        markedNodes.clear();
    }

    // Enable Multi-Select with CTRL
    @Override
    public boolean isOptionSelection(PInputEvent pie) {
        return pie.isControlDown();
    }

    private void markCluster(PInputEvent event) {
        PNode selectedNode = event.getPickedNode();
        while (selectedNode != null) {
            if (GeneralUnitPNode.class.isInstance(selectedNode) && !markedNodes.contains(selectedNode)) {
                GeneralUnitPNode gupNode = (GeneralUnitPNode) selectedNode;
                boolean doSelect = !isSelected(gupNode);

                if (mapPNode.getCurrentClusteringTree() != null) {
                    // System.out.println("marked unit: " + ((GeneralUnitPNode) selectedNode).getUnit());
                    // select all the units in this cluster
                    ClusterNode findNode = mapPNode.getCurrentClusteringTree().findClusterOf(gupNode,
                            mapPNode.getState().numClusters);
                    GeneralUnitPNode[] nodes = findNode.getNodes();
                    for (GeneralUnitPNode generalUnitPNode : nodes) {
                        // System.out.println("selecting cluster unit " + generalUnitPNode.getUnit());
                        if (doSelect) {
                            super.select(generalUnitPNode);
                        } else {
                            super.unselect(generalUnitPNode);
                            System.out.println("removing " + generalUnitPNode.getUnit());
                        }

                        markedNodes.add(generalUnitPNode);
                    }
                    // System.out.println("\n");
                } else {
                    // Without explicit clustering each unit is its own cluster...
                    if (doSelect) {
                        super.select(gupNode);
                    } else {
                        super.unselect(gupNode);
                    }
                    markedNodes.add(gupNode);
                    // unselectAll();
                }
                return;
            }
            selectedNode = selectedNode.getParent();
        }
    }
}
