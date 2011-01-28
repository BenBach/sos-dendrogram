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
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;

import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * An abstract sequence handler that implements common tasks as drag start an drag activity step methods. Subclasses in
 * most cases will have to implement the {@link #endDrag(PInputEvent)} to do specific handling at the end of the
 * dragging.
 * 
 * @author Rudolf Mayer
 * @version $Id: AbstractDragSequenceEventHandler.java 3589 2010-05-21 10:42:01Z mayer $
 */
public abstract class AbstractDragSequenceEventHandler extends PDragSequenceEventHandler {

    protected Point2D startPos;

    protected double clickX;

    protected double clickY;

    /**
     * Define what values for the {@link PNode#getAttribute(Object)} are acceptable to select a {@link PNode} for
     * dragging. Subclasses have to set values to this variable in their constructor, otherwise all PNodes will be
     * dragable.
     */
    protected String[] allowedNodeTypes;

    public AbstractDragSequenceEventHandler() {
        super();
    }

    @Override
    protected void dragActivityStep(PInputEvent e) {
        Point2D currPos = e.getPosition();
        e.getPickedNode().getParent().setOffset(currPos.getX() - clickX, currPos.getY() - clickY);
    }

    @Override
    protected void startDrag(PInputEvent e) {
        // if you remove this "if" then you can move all PNodes !
        if (allowedNodeTypes == null || StringUtils.equalsAny(e.getPickedNode().getAttribute("type"), allowedNodeTypes)) {
            super.startDrag(e);
            startPos = e.getPosition();
            clickX = startPos.getX() - e.getPickedNode().getParent().getXOffset();
            clickY = startPos.getY() - e.getPickedNode().getParent().getYOffset();

            // debug info:
            if (e.getPickedNode() instanceof PText) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").finest(
                        "Moving '" + ((PText) e.getPickedNode()).getText() + "'");
            }
        }
    }

}