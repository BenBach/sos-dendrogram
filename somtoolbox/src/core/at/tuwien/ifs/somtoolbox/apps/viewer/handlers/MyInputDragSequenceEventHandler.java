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

import java.awt.geom.Point2D;
import java.util.logging.Logger;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.ArrowPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.InputCorrections;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.InputCorrection;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Used to move an input vector to a different unit.
 * 
 * @author Rudolf Mayer
 * @version $Id: MyInputDragSequenceEventHandler.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class MyInputDragSequenceEventHandler extends AbstractDragSequenceEventHandler {

    private InputCorrections shifts;

    public MyInputDragSequenceEventHandler(InputCorrections shifts) {
        this.shifts = shifts;
        allowedNodeTypes = new String[] { "data" };
    }

    @Override
    protected void endDrag(PInputEvent e) {
        super.endDrag(e);

        final PText pText = (PText) e.getPickedNode();
        final PNode pNode = pText.getParent();
        final String label = pText.getText();
        final GeneralUnitPNode sourceUnitNode = (GeneralUnitPNode) pNode.getParent();
        final MapPNode mapPNode = sourceUnitNode.getMapPNode();

        // find new unit
        final Point2D endPos = e.getPosition();
        final int newX = (int) (endPos.getX() / sourceUnitNode.getWidth());
        final int newY = (int) (endPos.getY() / sourceUnitNode.getHeight());
        final GeneralUnitPNode targetUnitNode = mapPNode.getUnit(newX, newY);

        // remove input from old unit, both graphically (GeneralUnitPNode) and logically (Unit)
        sourceUnitNode.removeChild(pNode);
        sourceUnitNode.getUnit().removeMappedInput(label);
        sourceUnitNode.updateDetailsAfterMoving();

        // and add it to the new unit
        targetUnitNode.addChild(pNode);
        targetUnitNode.getUnit().addMappedInput(label, 0.0, true); // FIXME: if input vector available, we should
        // calculate the new distance
        targetUnitNode.updateDetailsAfterMoving();

        InputCorrection correction;
        try {
            correction = shifts.addManualInputCorrection(sourceUnitNode.getUnit(), targetUnitNode.getUnit(), label);
            System.out.println(shifts);

            // remove potentially existing arrow
            if (!correction.getSourceUnit().equals(sourceUnitNode.getUnit())) {
                for (int i = 0; i < mapPNode.getInputCorrectionsPNode().getChildrenCount(); i++) {
                    PNode node = mapPNode.getInputCorrectionsPNode().getChild(i);
                    if (StringUtils.equals(node.getAttribute(SOMVisualisationData.INPUT_CORRECTIONS), label)) {
                        mapPNode.getInputCorrectionsPNode().removeChild(node);
                        break;
                    }
                }
            }

            // draw an arrow for the mapping shift
            ArrowPNode arrow = ArrowPNode.createInputCorrectionArrow(correction, InputCorrections.CreationType.MANUAL,
                    sourceUnitNode, targetUnitNode);
            mapPNode.getInputCorrectionsPNode().addChild(arrow);
            arrow.moveToBack();
        } catch (SOMToolboxException ex) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error moving input: " + ex.getMessage());
        }

    }

}
