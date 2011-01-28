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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.nodes.PLine;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;

/**
 * Implements the line selection model, everything below a drawn line is selected.
 * 
 * @author Robert Neumayer
 * @version $Id: LineSelectionEventHandler.java 3888 2010-11-02 17:42:53Z frank $
 */
public class LineSelectionEventHandler extends OrderedPSelectionEventHandler {

    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION";

    // width of units * blur factor = selection --> blur factor 1.0 means that every unit below the selection and +/-
    // unit with is selected
    public static final float BLUR_FACTOR = 0.2f;

    private String selectionStatusString = "";

    private Vector<PNode> lineParts = null; // stores parts of lines drawn for later removal

    private List<PNode> selectableParents = null; // List of nodes whose children can be selected

    private PNode marqueeParent = null; // Node that marquee is added to as a child

    // private Point2D presspt = null;

    // private Point2D canvasPressPt = null;

    private HashMap<Object, Boolean> allItems = null; // Used within drag handler temporarily

    // private ArrayList unselectList = null; // Used within drag handler temporarily

    // private HashMap marqueeMap = null;

    private PNode pressNode = null; // Node pressed on (or null if none)

    // private boolean firsttime = true;

    /**
     * Creates a selection event handler.
     * 
     * @param marqueeParent The node to which the event handler dynamically adds a marquee (temporarily) to represent
     *            the area being selected.
     * @param selectableParent The node whose children will be selected by this event handler.
     */
    public LineSelectionEventHandler(PNode marqueeParent, PNode selectableParent) {
        super(marqueeParent, selectableParent);
        this.marqueeParent = marqueeParent;
        this.selectableParents = new ArrayList<PNode>();
        this.selectableParents.add(selectableParent);
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));
        init();
    }

    @Override
    public void decorateSelectedNode(PNode node) {
        // do nothing now
        // super.decorateSelectedNode(arg0);
        if (GeneralUnitPNode.class.isInstance(node)) {
            ((GeneralUnitPNode) node).setSelected(true);
        }
    }

    @Override
    public void undecorateSelectedNode(PNode node) {
        // super.undecorateSelectedNode(node);
        if (GeneralUnitPNode.class.isInstance(node)) {
            ((GeneralUnitPNode) node).setSelected(false);
        }
    }

    @Override
    public void init() {
        super.init();
        this.lineParts = new Vector<PNode>();
        allItems = new HashMap<Object, Boolean>();
        this.setIsDragging(false);
    }

    /**
     * delete the old selection / line
     */
    public void deleteOldLine() {
        // System.out.println("delete old line " + lineParts.size());
        if (!lineParts.isEmpty()) {
            for (PNode linePart : lineParts) {
                marqueeParent.removeChild(linePart);
            }
            lineParts.clear();
        }
        unselectAll();
        marqueeParent.repaint();
    }

    // //////////////////////////////////////////////////////
    // The overridden methods from PDragSequenceEventHandler
    // //////////////////////////////////////////////////////

    static Point2D startPoint;

    static Point2D lastPoint;

    static Point2D nextPoint;

    static Point2D endPoint;

    PLine currentLine = null;

    @Override
    protected void startDrag(PInputEvent e) {
        super.startDrag(e);
        startPoint = marqueeParent.globalToLocal(e.getPosition());
        this.initializeSelection(e);
        this.deleteOldLine();
        this.selectionStatusString = "Selected Units:";
        lastPoint = startPoint;
        // currentLine = new PLine();
        // currentLine.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // currentLine.setStrokePaint(Color.red);
        // // currentLine.setTransparency(0.2f);
        // marqueeParent.addChild(currentLine);
        // lineParts.add(currentLine);
    }

    @Override
    protected void drag(PInputEvent e) {
        nextPoint = marqueeParent.globalToLocal(e.getPosition());

        // float blur = 0.4f;
        PPath pp = PPath.createLine((float) lastPoint.getX(), (float) lastPoint.getY(), (float) nextPoint.getX(),
                (float) nextPoint.getY());
        pp.setStroke(new BasicStroke(14));
        pp.setStrokePaint(Color.red);
        // pp.setTransparency(0.2f);
        marqueeParent.addChild(pp);
        lineParts.add(pp);
        // PLine pl = new PLine();
        // pl.addPoint(0, lastPoint.getX(), lastPoint.getY());
        // pl.addPoint(1, nextPoint.getX(), nextPoint.getY());
        // pl.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // pl.setStrokePaint(Color.red);
        // marqueeParent.addChild(pl);
        // lineParts.add(pl);

        if (currentLine != null) {
            currentLine.addPoint(currentLine.getPointCount(), nextPoint.getX(), nextPoint.getY());
        }
        updateWhatever(e);
        lastPoint = nextPoint;
        PNotificationCenter.defaultCenter().postNotification(SELECTION_CHANGED_NOTIFICATION, this);
    }

    // //////////////////////////
    // Additional methods, partly restricted
    // //////////////////////////

    @Override
    protected boolean isMarqueeSelection(PInputEvent pie) {
        return true;
    }

    @Override
    protected void initializeMarquee(PInputEvent e) {
    }

    @Override
    protected void startMarqueeSelection(PInputEvent e) {
    }

    /** adds the node specified by the event e to the current selection (if it is not selected already) */
    protected void updateWhatever(PInputEvent e) {
        for (PNode parent : selectableParents) {
            for (Object o : parent.getAllNodes()) {
                if (o instanceof GeneralUnitPNode) {
                    GeneralUnitPNode upn = (GeneralUnitPNode) o;
                    if (this.onSelection(upn, nextPoint)) {
                        if (!this.alreadySelected(o)) {
                            super.select(upn);
                            this.selectionStatusString += " (" + upn.getUnit().getXPos() + "/"
                                    + upn.getUnit().getYPos() + ")";
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").finer(selectionStatusString);
                        }
                    }
                }
                allItems.put(o, Boolean.TRUE);
            }
        }
    }

    /**
     * check if the given Point lies on the given UnitPNode
     * 
     * @return - true if coordinates match, false if not
     */
    protected boolean onSelection(GeneralUnitPNode upn, Point2D selectedPoint) {
        double nodex1 = upn.getX();
        double nodex2 = upn.getX() + upn.getWidth();
        double nodey1 = upn.getY();
        double nodey2 = upn.getY() + upn.getHeight();
        /*
         * debug System.out.println("_______________"); System.out.println(nodex1); System.out.println(nodex2); System.out.println(nodey1);
         * System.out.println(nodey2); System.out.println("_____"); System.out.println(selectedPoint.getX() + " " + selectedPoint.getY());
         */

        if (selectedPoint.getX() >= nodex1 - upn.getWidth() * BLUR_FACTOR
                && selectedPoint.getX() <= nodex2 + upn.getWidth() * BLUR_FACTOR) {
            if (selectedPoint.getY() >= nodey1 - upn.getWidth() * BLUR_FACTOR
                    && selectedPoint.getY() <= nodey2 + upn.getHeight() * BLUR_FACTOR) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void endMarqueeSelection(PInputEvent e) {
    }

    @Override
    protected void endStandardSelection(PInputEvent e) {
        pressNode = null;
    }
}