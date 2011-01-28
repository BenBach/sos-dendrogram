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
package at.tuwien.ifs.somtoolbox.apps.viewer;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.ClusterSelectionEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.LineSelectionEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyInputDragSequenceEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyLabelDragSequenceEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyRectangleSelectionEventHandler;

/**
 * adds selection handler set and get methods to the piccolo pcanvas class
 * 
 * @author Robert Neumayer
 * @version $Id: MyPCanvas.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class MyPCanvas extends PCanvas {

    private static final long serialVersionUID = 1L;

    private PDragSequenceEventHandler selectionEventHandler;

    /**
     * Get the selection event handler associated with this canvas.
     */
    public PDragSequenceEventHandler getSelectionEventHandler() {
        return this.selectionEventHandler;
    }

    public MyRectangleSelectionEventHandler getMyRectangleSelectionEventHandler() {
        return (MyRectangleSelectionEventHandler) this.selectionEventHandler;
    }

    public LineSelectionEventHandler getLineSelectionEventHandler() {
        return (LineSelectionEventHandler) this.selectionEventHandler;
    }

    /**
     * Set the selection event handler associated with this canvas.
     * 
     * @param handler the new selection event handler
     */
    public void setSelectionEventHandler(MyRectangleSelectionEventHandler handler) {
        if (this.selectionEventHandler instanceof MyRectangleSelectionEventHandler) {
            return;
        }
        clearOldHandlers();
        this.selectionEventHandler = handler;
        if (this.selectionEventHandler != null) {
            addInputEventListener(this.selectionEventHandler);
        }
    }

    private void clearOldHandlers() {
        if (this.selectionEventHandler != null) {
            if (this.selectionEventHandler instanceof LineSelectionEventHandler) {
                ((LineSelectionEventHandler) this.selectionEventHandler).deleteOldLine();
            } else if (this.selectionEventHandler instanceof ClusterSelectionEventHandler) {
                PSelectionEventHandler p = (PSelectionEventHandler) this.selectionEventHandler;
                p.unselectAll();
            } else if (this.selectionEventHandler instanceof PSelectionEventHandler) {
                PSelectionEventHandler p = (PSelectionEventHandler) this.selectionEventHandler;
                p.unselectAll();
            }
            removeInputEventListener(this.selectionEventHandler);
        }
    }

    /**
     * Set the corridor selection event handler associated with this canvas.
     * 
     * @param handler the new selection event handler
     */
    public void setSelectionEventHandler(LineSelectionEventHandler handler) {
        if (this.selectionEventHandler instanceof LineSelectionEventHandler) {
            return;
        }
        clearOldHandlers();
        this.selectionEventHandler = handler;
        if (this.selectionEventHandler != null) {
            addInputEventListener(this.selectionEventHandler);
        }
    }

    /**
     * Set the cluster selection event handler associated with this canvas.
     * 
     * @param handler the new selection event handler
     */
    public void setSelectionEventHandler(ClusterSelectionEventHandler handler) {
        if (this.selectionEventHandler instanceof ClusterSelectionEventHandler) {
            return;
        }
        clearOldHandlers();
        this.selectionEventHandler = handler;
        if (this.selectionEventHandler != null) {
            addInputEventListener(this.selectionEventHandler);
        }
    }

    /**
     * Removes the current selectionEventHandler associated with this canvas and adds event handler for moving inputs.
     * 
     * @param handler the event handler for input moving
     */
    public void setSelectionEventHandler(MyInputDragSequenceEventHandler handler) {
        if (this.selectionEventHandler instanceof MyInputDragSequenceEventHandler) {
            return;
        }
        if (this.selectionEventHandler != null) {
            PDragSequenceEventHandler p = this.selectionEventHandler;
            if (p instanceof LineSelectionEventHandler) {
                LineSelectionEventHandler lse = (LineSelectionEventHandler) p;
                lse.deleteOldLine();
            }
            if (p instanceof MyRectangleSelectionEventHandler) {
                MyRectangleSelectionEventHandler rse = (MyRectangleSelectionEventHandler) p;
                rse.unselectAll();
            }
            removeInputEventListener(this.selectionEventHandler);
            this.selectionEventHandler = handler;
            if (this.selectionEventHandler != null) {
                addInputEventListener(this.selectionEventHandler);
            }
        }
    }

    // Angela: TODO Maybe merge the three setSelectionEventHandler in 1 function

    /**
     * Removes the current selectionEventHandler associated with this canvas and adds event handler for moving Labels.
     * 
     * @param handler the event handler for label moving
     */
    public void setSelectionEventHandler(MyLabelDragSequenceEventHandler handler) {
        if (this.selectionEventHandler instanceof MyLabelDragSequenceEventHandler) {
            return;
        }
        if (this.selectionEventHandler != null) {
            PDragSequenceEventHandler p = this.selectionEventHandler;
            if (p instanceof LineSelectionEventHandler) {
                LineSelectionEventHandler lse = (LineSelectionEventHandler) p;
                lse.deleteOldLine();
            }
            if (p instanceof MyRectangleSelectionEventHandler) {
                MyRectangleSelectionEventHandler rse = (MyRectangleSelectionEventHandler) p;
                rse.unselectAll();
            }
            removeInputEventListener(this.selectionEventHandler);
            this.selectionEventHandler = handler;
            if (this.selectionEventHandler != null) {
                addInputEventListener(this.selectionEventHandler);
            }
        }
    }

    // edit by epei
    public void removeSelection() {
        PDragSequenceEventHandler handler = this.getSelectionEventHandler();
        if (handler instanceof MyRectangleSelectionEventHandler) {
            ((MyRectangleSelectionEventHandler) handler).unselectAll();
        }
        if (handler instanceof LineSelectionEventHandler) {
            ((LineSelectionEventHandler) handler).deleteOldLine();
        }
    }

}
