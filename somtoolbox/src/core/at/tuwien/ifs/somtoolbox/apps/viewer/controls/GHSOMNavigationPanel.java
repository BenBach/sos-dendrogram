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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMFrame;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GHSOMHierarchyRoot;
import at.tuwien.ifs.somtoolbox.models.GHSOMLevelLayer;

/**
 * A panel providing drill down and roll up features for a hierarchical growing som.
 * 
 * @author Philip Langer
 *         <p[dot]langer[at]gmail[dot]com>
 */
public class GHSOMNavigationPanel extends AbstractSelectionPanel implements ActionListener {

    /**
     * The zoom in action command.
     */
    private static final String AC_DRILL_DOWN = "drill-down";

    /**
     * The zoom out action command.
     */
    private static final String AC_ROLL_UP = "roll-up";

    /**
     * The label for the zoom in button.
     */
    private static final String LBL_DRILL_DOWN_BUTTON = "Drill down";

    /**
     * The label for the zoom out button.
     */
    private static final String LBL_ROLL_UP_BUTTON = "Roll up";

    /**
     * The serial id.
     */
    private static final long serialVersionUID = 6360870188975691823L;

    /**
     * The drill down {@link JButton}.
     */
    private JButton btDrillDown;

    /**
     * The roll up {@link JButton}.
     */
    private JButton btRollUp;

    /**
     * the current viewed level
     */
    private int currentLevel = -1;

    /**
     * The currently selected {@link Unit}.
     */
    private Unit currentUnit = null;

    /**
     * The logger for this type.
     */
    private Logger logger = Logger.getLogger("at.tuwien.ifs.somtoolbox");

    /**
     * a vector containing all opened sub-frames
     */
    private Vector<SOMFrame> openedFrames = new Vector<SOMFrame>();

    private GHSOMHierarchyRoot rootLayer = new GHSOMHierarchyRoot(state.growingSOM.getLayer());

    /**
     * Reference to the som pane.
     */
    private SOMPane somPane;

    /**
     * Constructor.
     * 
     * @param state state.
     * @param mapPane som map.
     */
    public GHSOMNavigationPanel(CommonSOMViewerStateData state, SOMPane mapPane) {
        super(new GridBagLayout(), state, "GHSOM Navigation Panel");
        this.somPane = mapPane;
        this.initGUIElements();

        // set current level to rootLevel
        currentLevel = 0;

        setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand() == AC_DRILL_DOWN) {
            drillDown(currentUnit);
        } else if (ae.getActionCommand() == AC_ROLL_UP) {
            rollUp();
        }
    }

    private void addLayerFrame(GrowingLayer layer) {
        SOMFrame newSomFrame = new SOMFrame(state);
        openedFrames.add(newSomFrame);

        newSomFrame.setVisible(true);
        newSomFrame.setSize(400, 400);
        if (layer.getSuperUnit() != null) {
            newSomFrame.setTitle("SubLayer of Unit " + layer.getSuperUnit().getXPos() + "/"
                    + layer.getSuperUnit().getYPos());
        } else {
            newSomFrame.setTitle("no parent detected");
        }

        SOMPane newSomPane = new SOMPane(somPane.getMap().getParentFrame(), state.growingSOM, state.growingLayer, state);
        newSomPane.setSize(newSomFrame.getWidth() - 15, newSomFrame.getHeight() - 15);

        newSomFrame.add(newSomPane);
        newSomFrame.setResizable(true);
        somPane.getCanvas().add(newSomFrame);
        newSomPane.validate();
        newSomPane.centerAndFitMapToScreen(0);
    }

    /**
     * Drills down the underlying map of the specified {@link Unit}.
     * 
     * @param unit the node to zoom into.
     */
    private void drillDown(Unit unit) {
        // check if null
        if (unit == null) {
            throw new IllegalArgumentException("Specified unit node is null");
        }

        // check if drill down able
        if (!isDrillDownable(unit)) {
            throw new IllegalArgumentException("Specified unit node is drillable");
        }

        addLayerFrame(unit.getMappedSOM().getLayer());
        // TODO doesn't work
        // switchMainMap((GrowingLayer)unit.getMappedSOM().getLayer());

        logger.info("drill down to " + unit.toString());
        somPane.getBorder();

        somPane.addNotify();
        // TODO select underlying map and repaint with it
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(state.controlElementsWidth, 200);
    }

    // @Override
    // public Dimension getPreferredSize() {
    // return new Dimension(state.controlElementsWidth, 300);
    // }

    /**
     * Initializes the GUI elements.
     */
    private void initGUIElements() {

        JPanel ghsomPanel = new JPanel(new GridBagLayout());

        JPanel navigPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        // drill down button
        btDrillDown = new JButton(LBL_DRILL_DOWN_BUTTON);
        btDrillDown.setActionCommand(AC_DRILL_DOWN);
        btDrillDown.addActionListener(this);
        btDrillDown.setEnabled(false);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;
        navigPanel.add(btDrillDown, c);

        // drill down button
        btRollUp = new JButton(LBL_ROLL_UP_BUTTON);
        btRollUp.setActionCommand(AC_ROLL_UP);
        btRollUp.addActionListener(this);
        btRollUp.setEnabled(false);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;
        navigPanel.add(btRollUp, c);

        ghsomPanel.add(navigPanel, c);

        JPanel levelPanel = new JPanel(new GridLayout(4, 1));
        // create a toggle-button for each layer
        ButtonGroup group = new ButtonGroup();
        JToggleButton button;
        for (int i = 0; i <= GHSOMLevelLayer.getDepth(); i++) {
            button = new JToggleButton(); // TODO overwrite jtoggelbutton and
            // add information about current
            // level
            button.setName("" + i); // always must contain the Level-Number!
            button.setText("level " + i);

            // if level = 0, select the root level element
            if (i == 0) {
                button.setSelected(true);
            }

            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    // TODO Auto-generated method stub
                    JToggleButton button = (JToggleButton) event.getSource();
                    if (!button.getName().equals("" + currentLevel)) {
                        currentLevel = Integer.parseInt(button.getName());
                        System.out.println(button.getName() + "selected: " + button.isSelected());

                        // close former windows
                        for (SOMFrame openFrame : openedFrames) {
                            openFrame.dispose();
                        }

                        // if level = 0, set the root map to visible again
                        if (currentLevel == 0) {
                            somPane.getMap().setVisible(true);
                        } else {
                            // if level != 0 set root map to invisible
                            somPane.getMap().setVisible(false);

                            // create level frames
                            ButtonModel buttonModel = button.getModel();
                            boolean armed = buttonModel.isArmed();
                            boolean pressed = buttonModel.isPressed();
                            boolean selected = buttonModel.isSelected();
                            System.out.println("armed / pressed / selected " + armed + " / " + pressed + " / "
                                    + selected);

                            // load level of SOM
                            GHSOMLevelLayer layer = rootLayer.getLevel(Integer.parseInt(button.getName()));
                            // for each growingLayer of the level create a
                            // window
                            CommonSOMViewerStateData state = somPane.getMap().getState();

                            for (GrowingLayer glayer : layer.getLevelLayer()) {
                                state.growingLayer = glayer;

                                addLayerFrame(glayer);

                                System.out.println("somframe created");
                            }

                            // arrange frames
                            for (int i = 0; i < openedFrames.size(); i++) {
                                openedFrames.get(i).setLocation(15 * i, 15 * i);
                            }

                        }
                    }
                    somPane.validate();
                }
            });
            group.add(button);
            levelPanel.add(button);
        }

        // add scrollpane
        // TODO doesn't work propperly
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(levelPanel);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.CENTER;
        ghsomPanel.add(scrollPane, c);

        getContentPane().add(ghsomPanel, c);
    }

    /**
     * Returns <code>true</code> if the specified <code>unit</code> has an underlying map and is therefore
     * drill-down-able.
     * 
     * @param unit {@link Unit} to check.
     * @return <code>true</code> if drill-down-able, <code>false</code> otherwhise.
     */
    private boolean isDrillDownable(Unit unit) {
        return unit.getMappedSOM() != null;
    }

    /**
     * Returns <code>true</code> if the current map is a child of a {@link Unit}.
     * 
     * @return <code>true</code> if roll up is possible, <code>false</code> otherwise.
     */
    private boolean isRollUpable(GrowingLayer growingLayer) {
        return growingLayer.getSuperUnit() != null;
    }

    /**
     * Rolls up to the parent of the current layer.
     */
    private void rollUp() {
        // TODO select parent map of current map and repaint
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    // private void switchMainMap(GrowingLayer layer) {
    // // FIXME doesn't work ...
    // SOMPane newSomPane = new SOMPane(somPane.getMap().getParentFrame(), state.growingSOM, state.growingLayer, state);
    // somPane = newSomPane;
    // newSomPane.validate();
    // newSomPane.centerAndFitMapToScreen(0);
    // }

    @Override
    public void unitSelectionChanged(Object[] selection, boolean newSelection) {
        super.unitSelectionChanged(selection, newSelection);

        // as we just can zoom into one unit we just track the
        // first of the selected nodes.
        logger.fine(String.valueOf(selection.length));
        if (selection.length == 1) {
            currentUnit = ((GeneralUnitPNode) selection[0]).getUnit();

            // check if unit is drillable.
            if (isDrillDownable(currentUnit)) {
                btDrillDown.setEnabled(true);
            } else {
                btDrillDown.setEnabled(false);
            }

        } else {
            btDrillDown.setEnabled(false);
        }
        if (selection.length > 0 && selection[0] instanceof GeneralUnitPNode) {
            GrowingLayer layer = (GrowingLayer) ((GeneralUnitPNode) selection[0]).getUnit().getLayer();
            // update roll up button
            btRollUp.setEnabled(isRollUpable(layer));
        }
    }

}
