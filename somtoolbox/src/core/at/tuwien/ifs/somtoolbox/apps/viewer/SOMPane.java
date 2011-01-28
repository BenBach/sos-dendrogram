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

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import javax.swing.JFrame;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyInputDragSequenceEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyLabelDragSequenceEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyWheelZoomEventHandler;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;
import at.tuwien.ifs.somtoolbox.visualization.comparison.QuiverPNode;
import at.tuwien.ifs.somtoolbox.visualization.comparison.SOMComparison;

/**
 * A specific subclass of {@link GenericPNodeScrollPane} that holds a {@link MapPNode} and handles additionaly label and
 * input movement events.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: SOMPane.java 3939 2010-11-17 16:06:14Z frank $
 */
public class SOMPane extends GenericPNodeScrollPane {
    private static final long serialVersionUID = 1L;

    private MyInputDragSequenceEventHandler inputDragHandler;

    // to select Labels for moving them
    private MyLabelDragSequenceEventHandler labelDragHandler;

    private MapPNode map = null;

    private MapPNode map2 = null;

    // SOM Comparision
    private SOMComparison somComparision = null;

    private boolean shiftArrowsVisibility = false;

    private QuiverPNode quiver = null;

    private final int SECOND_MAP_OFFSET = 200;

    private double secMapXOffset = 0;

    private double secMapYOffset = 0;

    // change this for the default location of the second SOM. "right" makes sense for wide-screens, "bottom" might be
    // better otherwise
    private String secMapPosition = "right";

    /**
     * Default constructor.
     */
    public SOMPane(JFrame parent, String weightVectorFileName, String unitDescriptionFileName,
            String mapDescriptionFileName, CommonSOMViewerStateData state) {
        super();
        this.state = state;

        try {
            map = new MapPNode(parent, weightVectorFileName, unitDescriptionFileName, mapDescriptionFileName, state);
            node = map;
        } catch (FileNotFoundException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Cannot read input file(s): " + e.getMessage() + " - stopping.");
            System.exit(1); // FIXME: don't use System.exit
        } catch (SOMLibFileFormatException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Cannot read input file(s): " + e.getMessage() + " - stopping.");
            System.exit(1); // FIXME: don't use System.exit
        }
        init();
    }

    /**
     * constructor for an already loaded growing som. can be used to create a sompane from a subhierarchy of a grwoing
     * som
     */
    public SOMPane(JFrame parent, GrowingSOM gsom, GrowingLayer layer, CommonSOMViewerStateData state) {
        super();
        this.state = state;
        map = new MapPNode(parent, gsom, layer, state);
        node = map;
        init();
    }

    @Override
    protected void init() {
        super.init();

        // add a new listener that will act on mouse wheel events, and will update the MapDetailPanel
        // we have to register the listener here rather than in MapPNode, as not always, all events will be fired in
        // MapPNode
        canvas.addInputEventListener(new PInputEventListener() {
            @Override
            public void processEvent(PInputEvent aEvent, int type) {
                // update zoom display on mouse wheel, but only for main display (MyPCanvas)
                if (aEvent.isMouseWheelEvent() && aEvent.getSourceSwingEvent().getSource() instanceof MyPCanvas) {
                    state.getMapDetailPanel().updatePanel(aEvent.getTopCamera().getViewScale());
                }
            }
        });

        // connect all keyboard events to the canvas' root pane
        canvas.getRoot().getDefaultInputManager().setKeyboardFocus(new PBasicInputEventHandler() {
            @Override
            public void keyPressed(PInputEvent event) {
                super.keyPressed(event);
                if (event.getSourceSwingEvent() instanceof KeyEvent) {
                    KeyEvent keyEvent = (KeyEvent) event.getSourceSwingEvent();
                    int key = keyEvent.getKeyCode();

                    // panning keys with the arrow keys & the corner keys on the num-pad
                    if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_KP_DOWN || key == KeyEvent.VK_END
                            || key == KeyEvent.VK_PAGE_DOWN) {
                        canvas.getCamera().translateView(0, -10);
                    }
                    if (key == KeyEvent.VK_UP || key == KeyEvent.VK_KP_UP || key == KeyEvent.VK_HOME
                            || key == KeyEvent.VK_PAGE_UP) {
                        canvas.getCamera().translateView(0, 10);
                    }
                    if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT || key == KeyEvent.VK_HOME
                            || key == KeyEvent.VK_END) {
                        canvas.getCamera().translateView(10, 0);
                    }
                    if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT || key == KeyEvent.VK_PAGE_UP
                            || key == KeyEvent.VK_PAGE_DOWN) {
                        canvas.getCamera().translateView(-10, 0);
                    }
                    // zooming with +/- keys
                    if (key == KeyEvent.VK_PLUS || key == KeyEvent.VK_ADD) {
                        MyWheelZoomEventHandler.processZoomEvent(event, -1, canvas.getCamera());
                    } else if (key == KeyEvent.VK_MINUS || key == KeyEvent.VK_SUBTRACT) {
                        MyWheelZoomEventHandler.processZoomEvent(event, 1, canvas.getCamera());
                    }
                } else {
                    // this should never happen...
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "SOMPane.PBasicInputEventHandler: Got an unknown event type: "
                                    + event.getSourceSwingEvent());
                }
            }
        });

        // make new arrow container and make it listen to the selection changes
        // on the first map
        quiver = new QuiverPNode(this);
        this.connectSelectionHandlerTo(quiver);

        // redraw interface if necessary
        validate();

    }

    /**
     * initialize both selection handlers and set rectangle selection as default. also initializes handler for selecting
     * and moving a cluster label.
     */
    @Override
    public void initSelectionHandlers() {
        super.initSelectionHandlers();
        this.inputDragHandler = new MyInputDragSequenceEventHandler(
                map.getGsom().getSharedInputObjects().getInputCorrections());
        this.labelDragHandler = new MyLabelDragSequenceEventHandler();
    }

    /** set the input selection handler. */
    public void setInput() {
        ((MyPCanvas) canvas).setSelectionEventHandler(this.inputDragHandler);
        PNotificationCenter.defaultCenter().addListener(this, "selectionChanged",
                PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION, this.inputDragHandler);
    }

    // Angela
    /**
     * Change the selection handler from selecting units to moving labels. Moving labels is of course no selection but
     * it looks nasty if units get selected while dragging the label around. Therefore the current selection handler
     * gets disabled.
     */
    public void setLabel() {
        ((MyPCanvas) canvas).setSelectionEventHandler(this.labelDragHandler);
        PNotificationCenter.defaultCenter().addListener(this, "selectionChanged",
                PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION, this.labelDragHandler);
    }

    /** @deprecated use {@link Visualizations} instead */
    @Deprecated
    public BackgroundImageVisualizer[] getVisualizations() {
        return map.getVisualizations();
    }

    public BackgroundImageVisualizer getCurrentVisualization() {
        return map.getCurrentVisualization();
    }

    public void setNoVisualization() {
        map.setNoVisualization();
    }

    public boolean setInitialVisualization(BackgroundImageVisualizer vis, int variant) throws SOMToolboxException {
        return map.setInitialVisualizationOnStartup(vis, variant);
    }

    public boolean setVisualization(BackgroundImageVisualizer vis, int variant) throws SOMToolboxException {
        return map.setVisualization(vis, variant);
    }

    public boolean setVisualization(int vis, int variant) throws SOMToolboxException {
        return map.setVisualization(vis, variant);
    }

    public Color[] getClassLegendColors() {
        return map.getClassLegendColors();
    }

    public String[] getClassLegendNames() {
        return map.getClassLegendNames();
    }

    public void updateVisualization() {
        map.updateVisualization();
    }

    public void updateClassSelection(int[] indices) {
        updateClassSelection(indices, map);
        if (map2 != null) {
            updateClassSelection(indices, map2);
        }
    }

    public void setShowOnlySelectedClasses(boolean selectedClassesOnly) {
        map.setShowOnlySelectedClasses(selectedClassesOnly);
    }

    private void updateClassSelection(int[] indices, MapPNode mapPNode) {
        mapPNode.updateClassSelection(indices);
        if (mapPNode.getThematicClassMapVisualizer() != null) {
            mapPNode.getThematicClassMapVisualizer().invalidateCache();
        }
    }

    public void setClassColor(int index, Color color) {
        map.setClassColor(index, color);
        if (map2 != null) {
            map2.setClassColor(index, color);
        }
    }

    @Override
    public void centerAndFitMapToScreen(int animationDuration) {
        // if the second map is diplayed
        if (shiftArrowsVisibility && map2 != null) {
            double x = 0, y = 0, x2, y2, width, height;
            // take the leftmost point as x
            if (secMapXOffset < 0) {
                x = secMapXOffset;
            }
            // take the highest point as y
            if (secMapYOffset < 0) {
                y = secMapYOffset;
            }
            // take the rightmost point as x2
            if (map.getWidth() > secMapXOffset + map2.getWidth()) {
                x2 = map.getWidth();
            } else {
                x2 = secMapXOffset + map2.getWidth();
            }
            // take the lowest point as y2
            if (map.getHeight() > secMapYOffset + map2.getHeight()) {
                y2 = map.getHeight();
            } else {
                y2 = secMapYOffset + map2.getHeight();
            }
            // calculate width and height
            width = Math.abs(x - x2);
            height = Math.abs(y - y2);
            // use coordinates just calculated
            canvas.getCamera().animateViewToCenterBounds(new Rectangle2D.Double(x, y, width, height), true,
                    animationDuration);
        } else {
            super.centerAndFitMapToScreen(animationDuration);
        }
    }

    public MapPNode getMap() {
        return map;
    }

    /**
     * Is called when the comparison object for comparison between two SOMs must be changed. Either loads new second SOM
     * and computes new arrows accordingly, or throws away all currently held arrows (if the comparison object is
     * unloaded).
     */
    public void updateSOMComparison() throws SOMToolboxException {
        // remove old compareSOMs object
        somComparision = new SOMComparison();
        // remove all old arrows
        quiver.dropArrows();

        if (canvas.getLayer().isAncestorOf(quiver)) {
            canvas.getLayer().removeChild(quiver);
        }

        // remove second map
        if (map2 != null && canvas.getLayer().isAncestorOf(map2)) {
            canvas.getLayer().removeChild(map2);
        }
        map2 = null;

        // get the filename for the second SOM
        String compareFileName = state.secondSOMName;

        // if the filename's not empty ==> a new set of files was loaded
        if (!compareFileName.equals("")) {
            // get prefix of filename (to load the other description files as well)
            String prefix = FileUtils.extractSOMLibInputPrefix(compareFileName);

            // contruct new MapPNode for second SOM
            try {
                // classInfo = (SOMLibClassInformation) inputObjects.getData(SOMVisualisationData.CLASS_INFO);
                String classInfoFile = state.inputDataObjects.getObject(SOMVisualisationData.CLASS_INFO).getFileName();
                CommonSOMViewerStateData state2 = new CommonSOMViewerStateData(state);
                state2.inputDataObjects = new SharedSOMVisualisationData(classInfoFile, null, "", "", "", "", "");

                // reading input objects, if we have filenames set
                state2.inputDataObjects.readAvailableData();

                map2 = new MapPNode(map.getParentFrame(), prefix + SOMLibFormatInputReader.weightFileNameSuffix, prefix
                        + SOMLibFormatInputReader.unitFileNameSuffix, prefix
                        + SOMLibFormatInputReader.mapFileNameSuffix, state2);
                this.setSecSOMPosition();

                this.updateSecMap();
                this.updateQuiver();

            } catch (FileNotFoundException e1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Cannot read input file(s): " + e1.getMessage() + " - stopping.");
            } catch (SOMLibFileFormatException e1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Cannot read input file(s): " + e1.getMessage() + " - stopping.");
            }

            try {
                // load the SOMs into the compareSOMs object
                somComparision.loadGSOMs(map.getGsom(), prefix);

                // compute new arrows
                quiver.computeArrows();

            } catch (SOMToolboxException e1) {
                // if there was an error loading the new SOMs
                // throw away all newly created objects
                map2 = null;
                somComparision = null;
                state.secondSOMName = "";
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e1.getMessage());
                throw e1;
            } catch (Exception e2) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e2.getMessage());
                e2.printStackTrace();
            }
        }
    }

    public void useSecSOMOffset() {
        map2.setOffset(secMapXOffset, secMapYOffset);
    }

    /**
     * Sets the position of the second SOM according to attribute secMapPosition
     */
    public void setSecSOMPosition() {
        if (secMapPosition.equals("bottom")) {
            secMapXOffset = (map.getWidth() - map2.getWidth()) / 2;
            secMapYOffset = map.getHeight() + SECOND_MAP_OFFSET;
        } else if (secMapPosition.equals("top")) {
            secMapXOffset = (map.getWidth() - map2.getWidth()) / 2;
            secMapYOffset = (map2.getHeight() + SECOND_MAP_OFFSET) * -1;
        } else if (secMapPosition.equals("left")) {
            secMapXOffset = (map2.getWidth() + SECOND_MAP_OFFSET) * -1;
            secMapYOffset = (map.getHeight() - map2.getHeight()) / 2;
        } else if (secMapPosition.equals("right")) {
            secMapXOffset = map.getWidth() + SECOND_MAP_OFFSET;
            secMapYOffset = (map.getHeight() - map2.getHeight()) / 2;
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Invalid Position for Second Map: " + secMapPosition);
        }

        this.useSecSOMOffset();
    }

    /** Sets the position of the second SOM according to position and saves the position in attribute secMapPosition */
    public void setSecSOMPosition(String position) {
        this.secMapPosition = position;
        this.setSecSOMPosition();
    }

    public CommonSOMViewerStateData getState() {
        return state;
    }

    public double getSecMapXOffset() {
        return secMapXOffset;
    }

    public void setSecMapXOffset(double secMapXOffset) {
        this.secMapXOffset = secMapXOffset;
        this.useSecSOMOffset();
    }

    public double getSecMapYOffset() {
        return secMapYOffset;
    }

    public void setSecMapYOffset(double secMapYOffset) {
        this.secMapYOffset = secMapYOffset;
        this.useSecSOMOffset();
    }

    public SOMComparison getSOMComparision() {
        return somComparision;
    }

    public MapPNode getSecondMap() {
        return map2;
    }

    public QuiverPNode getQuiver() {
        return quiver;
    }

    public boolean isShiftArrowsVisibility() {
        return shiftArrowsVisibility;
    }

    public void setShiftArrowsVisibility(boolean shiftArrowsVisibility) {
        if (shiftArrowsVisibility != this.shiftArrowsVisibility) {
            this.shiftArrowsVisibility = shiftArrowsVisibility;
            this.updateSecMap();
            this.updateQuiver();
            quiver.updateClusterBorders();
        }
    }

    private void updateSecMap() {
        if (map2 != null) {
            if (shiftArrowsVisibility == true) {
                if (!canvas.getLayer().isAncestorOf(map2)) {
                    canvas.getLayer().addChild(map2);
                }
            } else {
                if (canvas.getLayer().isAncestorOf(map2)) {
                    canvas.getLayer().removeChild(map2);
                }
            }
        }
    }

    private void updateQuiver() {
        if (quiver != null) {
            if (shiftArrowsVisibility == true) {
                if (!canvas.getLayer().isAncestorOf(quiver)) {
                    canvas.getLayer().addChild(quiver);
                    // quiver.moveToBack();
                    if (getMap().currentVisualizationImage != null) {
                        quiver.moveInFrontOf(getMap().currentVisualizationImage);
                    }
                }
            } else {
                if (canvas.getLayer().isAncestorOf(quiver)) {
                    canvas.getLayer().removeChild(quiver);
                }
            }
        }
    }
}
