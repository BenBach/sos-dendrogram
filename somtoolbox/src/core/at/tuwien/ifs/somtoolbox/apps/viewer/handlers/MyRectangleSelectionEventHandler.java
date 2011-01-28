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
import java.util.logging.Logger;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;

/**
 * EventHandler for selecting by rectangle.
 * 
 * @author Robert Neumayer
 * @version $Id: MyRectangleSelectionEventHandler.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class MyRectangleSelectionEventHandler extends OrderedPSelectionEventHandler {

    private String selectionStatusString = "Selected Units:";

    public MyRectangleSelectionEventHandler(PNode marqueeParent, PNode selectableParents) {
        super(marqueeParent, selectableParents);
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));
        setMarqueePaint(Color.red);
        setMarqueePaintTransparency(0.3f);
        if (selectableParents instanceof MapPNode) {
            // we need to specifically add the sub-node of MapPNode that contains the GeneralUnitPNodes
            addSelectableParent(((MapPNode) selectableParents).getUnitsNode());
        }
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
        if (GeneralUnitPNode.class.isInstance(node)) {
            ((GeneralUnitPNode) node).setSelected(false);
            selectionStatusString = "Selected Units:";
        }
    }

    @Override
    protected boolean isMarqueeSelection(PInputEvent arg0) {
        return true;
    }

    @Override
    protected void drag(PInputEvent arg0) {
        super.drag(arg0);
    }

    // this would also be called when selecting additional areas with SHIFT key holded
    // thus, the StatusString would be reset. hence, it is reset only when undecorateSelectedNode (above) is called
    // protected void dragActivityFinalStep(PInputEvent arg0) {
    // System.out.println("drag final");
    // super.dragActivityFinalStep(arg0);
    // selectionStatusString = "Selected Units:";
    // }

    @Override
    public void mouseClicked(PInputEvent event) {
        PNode selectedNode = event.getPickedNode();
        while (selectedNode != null) {
            if (GeneralUnitPNode.class.isInstance(selectedNode)) {
                super.select(selectedNode);
                return;
            }
            selectedNode = selectedNode.getParent();
        }
    }
}
