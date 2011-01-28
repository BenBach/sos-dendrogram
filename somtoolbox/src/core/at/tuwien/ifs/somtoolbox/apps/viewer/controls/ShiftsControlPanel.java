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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;

/**
 * @author Doris Baum
 * @version $Id: ShiftsControlPanel.java 3873 2010-10-28 09:29:58Z frank $
 */
public class ShiftsControlPanel extends AbstractViewerControl implements ActionListener, ItemListener {
    private static final long serialVersionUID = 1L;

    static final String sourceToolTip = "<html><body>Neighbourhood radius for the source neighbourhood;<br>"
            + "if cumulative is disabled, all shifts in the neighbourhood of a stable shift will be marked as adjacent shifts;<br>"
            + "if cumulative is enabled, additionally the vectors in the neighbourhood of a unit are counted as \"on\" this unit.</html></body>";

    static final String targetToolTip = "<html><body>Neighbourhood radius for the target neighbourhood;<br>"
            + "if cumulative is disabled, all shifts in the neighbourhood of a stable shift will be marked as adjacent shifts;<br>"
            + "if cumulative is enabled, additionally the vectors in the neighbourhood of a unit are counted as \"on\" this unit.</html></body>";

    static final String stableCountToolTip = "Minimum number of vectors that moved from a neighbourhood/unit to a neighbourhood/unit that count as a stable shift.";

    static final String outlierCountToolTip = "Minimum number of vectors that moved from a neighbourhood/unit to a neighbourhood/unit that count as a outlier shift.";

    static final String minAbsoluteToolTip = "Minimum absolute count that a vector needs to have to be displayed.";

    static final String positionToolTip = "Position of the second (target) SOM relative to the "
            + "first (source) SOM.";

    static final String positionManualToolTip = "Set position of the second (target) SOM "
            + "manually by specifying an offset.";

    static final String cumulativeToolTip = "<html><body>Switches between cumulative mode and un-cumulative mode;<br>"
            + "in cumulative mode, vectors in the neighbourhood of a unit are counted as \"on\" this unit;<br>"
            + "in un-cumulative mode, the neighbourshoods are only used for the identification of adjacent shifts.</html></body>";

    static final String absoluteToolTip = "<html><body>Switches the definition of the count thresholds;<br>"
            + "absolute: the absolute number of vectors that moved from a neighbourhood/unit to a neighbourhood/unit is used;<br>"
            + "percent: the percentage of vectors that moved from a neighbourhood/unit to a neighbourhood/unit is used.</html></body>";

    static final String showOnlyToolTip = "Show only stable/adjacent/outlier shifts as arrows.";

    private SOMPane mapPane = null;

    private GridBagConstraints bag = new GridBagConstraints();

    private JLabel noShiftInfoLoaded = null;

    private JSpinner sourceSpinner = null;

    private JSpinner targetSpinner = null;

    private JSpinner outlierCountSpinner = null;

    private JSpinner stableCountSpinner = null;

    private JSpinner minAbsoluteCountSpinner = null;

    private JLabel sourceLabel = null;

    private JLabel targetLabel = null;

    private JLabel countLabel = null;

    private JLabel outlierCLabel = null;

    private JLabel stableCLabel = null;

    private JLabel minAbsoluteCLabel = null;

    private JRadioButton countPercentRB = null;

    private JRadioButton countAbsoluteRB = null;

    private JCheckBox cumulativeCB = null;

    private JSeparator sep1 = null;

    private JSeparator sep2 = null;

    private JSeparator sep3 = null;

    private JLabel positionLabel = null;

    private JRadioButton posBottomRB = null;

    private JRadioButton posTopRB = null;

    private JRadioButton posLeftRB = null;

    private JRadioButton posRightRB = null;

    private JLabel positionLabelX = null;

    private JLabel positionLabelY = null;

    private JSpinner positionXSpinner = null;

    private JSpinner positionYSpinner = null;

    private JCheckBox outlierCB = null;

    private JCheckBox stableCB = null;

    private JCheckBox adjacentCB = null;

    private JCheckBox clusterCB = null;

    private JCheckBox multiMatchCB = null;

    private JLabel clusterLabel = null;

    private JSpinner clusterSpinner = null;

    public ShiftsControlPanel(SOMPane mapPane, CommonSOMViewerStateData state, String title) {
        super(title, state, new GridBagLayout());
        this.mapPane = mapPane;

        initNoShiftsInfo();
    }

    private void removeAllGUIElements() {
        if (sourceSpinner != null) {
            this.remove(sourceSpinner);
        }
        if (targetSpinner != null) {
            this.remove(targetSpinner);
        }
        if (outlierCountSpinner != null) {
            this.remove(outlierCountSpinner);
        }
        if (stableCountSpinner != null) {
            this.remove(stableCountSpinner);
        }
        if (sourceLabel != null) {
            this.remove(sourceLabel);
        }
        if (targetLabel != null) {
            this.remove(targetLabel);
        }
        if (countLabel != null) {
            this.remove(countLabel);
        }
        if (countPercentRB != null) {
            this.remove(countPercentRB);
        }
        if (countAbsoluteRB != null) {
            this.remove(countAbsoluteRB);
        }
        if (outlierCLabel != null) {
            this.remove(outlierCLabel);
        }
        if (stableCLabel != null) {
            this.remove(stableCLabel);
        }
        if (minAbsoluteCLabel != null) {
            this.remove(minAbsoluteCLabel);
        }
        if (minAbsoluteCountSpinner != null) {
            this.remove(minAbsoluteCountSpinner);
        }
        if (positionLabel != null) {
            this.remove(positionLabel);
        }
        if (posBottomRB != null) {
            this.remove(posBottomRB);
        }
        if (posTopRB != null) {
            this.remove(posTopRB);
        }
        if (posLeftRB != null) {
            this.remove(posLeftRB);
        }
        if (posRightRB != null) {
            this.remove(posRightRB);
        }
        if (positionLabelX != null) {
            this.remove(positionLabelX);
        }
        if (positionLabelY != null) {
            this.remove(positionLabelY);
        }
        if (positionXSpinner != null) {
            this.remove(positionXSpinner);
        }
        if (positionYSpinner != null) {
            this.remove(positionYSpinner);
        }
        if (sep1 != null) {
            this.remove(sep1);
        }
        if (outlierCB != null) {
            this.remove(outlierCB);
        }
        if (stableCB != null) {
            this.remove(stableCB);
        }
        if (adjacentCB != null) {
            this.remove(adjacentCB);
        }
        if (sep2 != null) {
            this.remove(sep2);
        }
        if (clusterCB != null) {
            this.remove(clusterCB);
        }
        if (cumulativeCB != null) {
            this.remove(cumulativeCB);
        }
        if (clusterLabel != null) {
            this.remove(clusterLabel);
        }
        if (clusterSpinner != null) {
            this.remove(clusterSpinner);
        }
        if (sep3 != null) {
            this.remove(sep3);
        }
        if (noShiftInfoLoaded != null) {
            remove(noShiftInfoLoaded);
        } else {
            noShiftInfoLoaded = new JLabel("There's no information on shifts available!");
        }
    }

    public void setThresholdBlock(boolean state) {
        sourceLabel.setEnabled(state);
        sourceSpinner.setEnabled(state);
        targetLabel.setEnabled(state);
        targetSpinner.setEnabled(state);
        countLabel.setEnabled(state);
        stableCLabel.setEnabled(state);
        outlierCLabel.setEnabled(state);
        outlierCountSpinner.setEnabled(state);
        stableCountSpinner.setEnabled(state);
        cumulativeCB.setEnabled(state);
        countPercentRB.setEnabled(state);
        countAbsoluteRB.setEnabled(state);

        if (state) {
            minAbsoluteCountSpinner.setEnabled(!mapPane.getSOMComparision().isAbsolute());
            minAbsoluteCLabel.setEnabled(!mapPane.getSOMComparision().isAbsolute());
        } else {
            minAbsoluteCountSpinner.setEnabled(state);
            minAbsoluteCLabel.setEnabled(state);
        }
    }

    public void initGUIElements() {
        this.removeAllGUIElements();

        bag.fill = GridBagConstraints.HORIZONTAL;

        sourceLabel = new JLabel("Source threshold: ");
        sourceLabel.setToolTipText(sourceToolTip);

        sourceSpinner = new JSpinner(new SpinnerNumberModel(mapPane.getSOMComparision().getSourceThreshold(), 0,
                mapPane.getSOMComparision().getMaxDistance(), 0.5));
        sourceSpinner.setToolTipText(sourceToolTip);
        sourceSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner src = (JSpinner) e.getSource();
                double sourceThreshold = ((Double) src.getValue()).doubleValue();
                mapPane.getSOMComparision().setSourceThreshold(sourceThreshold);
                mapPane.getQuiver().computeArrows();
            }
        });

        targetLabel = new JLabel("Target threshold: ");
        targetLabel.setToolTipText(targetToolTip);

        targetSpinner = new JSpinner(new SpinnerNumberModel(mapPane.getSOMComparision().gettargetThreshold(), 0,
                mapPane.getSOMComparision().getMaxDistance(), 0.5));
        targetSpinner.setToolTipText(targetToolTip);
        targetSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner src = (JSpinner) e.getSource();
                double targetThreshold = ((Double) src.getValue()).doubleValue();
                mapPane.getSOMComparision().settargetThreshold(targetThreshold);
                mapPane.getQuiver().computeArrows();
            }
        });

        countLabel = new JLabel("Count thresholds:");

        countAbsoluteRB = new JRadioButton("absolute", mapPane.getSOMComparision().isAbsolute());
        countAbsoluteRB.setToolTipText(absoluteToolTip);
        countPercentRB = new JRadioButton("percent", !mapPane.getSOMComparision().isAbsolute());
        countPercentRB.setToolTipText(absoluteToolTip);

        countAbsoluteRB.setActionCommand("absolute");
        countPercentRB.setActionCommand("percent");

        ButtonGroup abspergroup = new ButtonGroup();
        abspergroup.add(countAbsoluteRB);
        abspergroup.add(countPercentRB);

        countAbsoluteRB.addActionListener(this);
        countPercentRB.addActionListener(this);

        outlierCLabel = new JLabel("outlier: ");
        outlierCLabel.setToolTipText(outlierCountToolTip);

        outlierCountSpinner = new JSpinner(
                new SpinnerNumberModel(mapPane.getSOMComparision().getOutlierCountThreshold(), 1,
                        mapPane.getSOMComparision().getMaxCount(), 1));
        outlierCountSpinner.addChangeListener(new outlierAbsoluteChangeListener());
        outlierCountSpinner.setToolTipText(outlierCountToolTip);

        stableCLabel = new JLabel("stable: ");
        stableCLabel.setToolTipText(stableCountToolTip);

        stableCountSpinner = new JSpinner(new SpinnerNumberModel(mapPane.getSOMComparision().getStableCountThreshold(),
                1, mapPane.getSOMComparision().getMaxCount(), 1));
        stableCountSpinner.addChangeListener(new stableAbsoluteChangeListener());
        stableCountSpinner.setToolTipText(outlierCountToolTip);

        minAbsoluteCLabel = new JLabel("min. absolute: ");
        minAbsoluteCLabel.setToolTipText(minAbsoluteToolTip);
        minAbsoluteCLabel.setEnabled(!mapPane.getSOMComparision().isAbsolute());

        minAbsoluteCountSpinner = new JSpinner(new SpinnerNumberModel(
                mapPane.getSOMComparision().getMinAbsoluteCount(), 1, mapPane.getSOMComparision().getMaxCount(), 1));
        minAbsoluteCountSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner src = (JSpinner) e.getSource();
                int minAbsoluteCount = ((Integer) src.getValue()).intValue();
                mapPane.getSOMComparision().setMinAbsoluteCount(minAbsoluteCount);
                mapPane.getQuiver().computeArrows();
            }
        });
        minAbsoluteCountSpinner.setToolTipText(minAbsoluteToolTip);
        minAbsoluteCountSpinner.setEnabled(!mapPane.getSOMComparision().isAbsolute());

        cumulativeCB = new JCheckBox("cumulative", mapPane.getQuiver().isCumulative());
        cumulativeCB.setToolTipText(cumulativeToolTip);
        cumulativeCB.addItemListener(this);

        int gridy = 0;

        bag.fill = GridBagConstraints.HORIZONTAL;
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(sourceLabel, bag);

        bag.gridx = 1;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.EAST;
        getContentPane().add(sourceSpinner, bag);

        gridy++;

        bag.fill = GridBagConstraints.HORIZONTAL;
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(targetLabel, bag);

        bag.gridx = 1;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.EAST;
        getContentPane().add(targetSpinner, bag);

        gridy++;

        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(cumulativeCB, bag);

        gridy++;

        bag.fill = GridBagConstraints.HORIZONTAL;
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 2;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(countLabel, bag);

        gridy++;

        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(countAbsoluteRB, bag);

        bag.gridx = 1;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(countPercentRB, bag);

        gridy++;

        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(stableCLabel, bag);

        bag.gridx = 1;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.EAST;
        getContentPane().add(stableCountSpinner, bag);

        gridy++;

        bag.fill = GridBagConstraints.HORIZONTAL;
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(outlierCLabel, bag);

        bag.gridx = 1;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.EAST;
        getContentPane().add(outlierCountSpinner, bag);

        gridy++;

        bag.fill = GridBagConstraints.HORIZONTAL;
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(minAbsoluteCLabel, bag);

        bag.gridx = 1;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.EAST;
        getContentPane().add(minAbsoluteCountSpinner, bag);

        gridy++;

        bag.fill = GridBagConstraints.HORIZONTAL;

        sep1 = new JSeparator();
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 2;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(sep1, bag);

        positionLabel = new JLabel("Second Map Position:");
        positionLabel.setToolTipText(positionToolTip);
        posBottomRB = new JRadioButton("bottom", true);
        posBottomRB.setToolTipText(positionToolTip);
        posTopRB = new JRadioButton("top");
        posTopRB.setToolTipText(positionToolTip);
        posLeftRB = new JRadioButton("left");
        posLeftRB.setToolTipText(positionToolTip);
        posRightRB = new JRadioButton("right");
        posRightRB.setToolTipText(positionToolTip);

        posBottomRB.setActionCommand("bottom");
        posTopRB.setActionCommand("top");
        posLeftRB.setActionCommand("left");
        posRightRB.setActionCommand("right");

        ButtonGroup rbgroup = new ButtonGroup();
        rbgroup.add(posBottomRB);
        rbgroup.add(posTopRB);
        rbgroup.add(posLeftRB);
        rbgroup.add(posRightRB);
        posRightRB.setSelected(true);

        posBottomRB.addActionListener(this);
        posTopRB.addActionListener(this);
        posLeftRB.addActionListener(this);
        posRightRB.addActionListener(this);

        positionLabelX = new JLabel("X Offset: ");
        positionLabelX.setToolTipText(positionManualToolTip);

        positionXSpinner = new JSpinner(new SpinnerNumberModel(mapPane.getSecMapXOffset(), -10000, 10000, 5));
        positionXSpinner.setToolTipText(positionManualToolTip);
        positionXSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner src = (JSpinner) e.getSource();
                double xOffset = ((Double) src.getValue()).doubleValue();
                mapPane.setSecMapXOffset(xOffset);
                mapPane.getQuiver().computeArrows();
            }
        });

        positionLabelY = new JLabel("Y Offset: ");
        positionLabelY.setToolTipText(positionManualToolTip);

        positionYSpinner = new JSpinner(new SpinnerNumberModel(mapPane.getSecMapYOffset(), -10000, 10000, 5));
        positionYSpinner.setToolTipText(positionManualToolTip);
        positionYSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner src = (JSpinner) e.getSource();
                double yOffset = ((Double) src.getValue()).doubleValue();
                mapPane.setSecMapYOffset(yOffset);
                mapPane.getQuiver().computeArrows();
            }
        });

        gridy++;

        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 2;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(positionLabel, bag);

        gridy++;

        JPanel panelMapPosition = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelMapPosition.add(posBottomRB);
        panelMapPosition.add(posTopRB);
        panelMapPosition.add(posLeftRB);
        panelMapPosition.add(posRightRB);

        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 2;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(panelMapPosition, bag);

        gridy++;

        bag.fill = GridBagConstraints.HORIZONTAL;
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(positionLabelX, bag);

        // bag.fill = GridBagConstraints.NONE;
        bag.gridx = 1;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.EAST;
        getContentPane().add(positionXSpinner, bag);

        gridy++;

        bag.fill = GridBagConstraints.HORIZONTAL;
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(positionLabelY, bag);

        bag.gridx = 1;
        bag.gridy = gridy;
        bag.gridwidth = 1;
        bag.anchor = GridBagConstraints.EAST;
        getContentPane().add(positionYSpinner, bag);

        gridy++;

        sep2 = new JSeparator();
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 2;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(sep2, bag);

        stableCB = new JCheckBox("stable", mapPane.getQuiver().stableArrowsOn());
        stableCB.addItemListener(this);
        stableCB.setToolTipText(showOnlyToolTip);

        outlierCB = new JCheckBox("outlier", mapPane.getQuiver().outlierArrowsOn());
        outlierCB.addItemListener(this);
        outlierCB.setToolTipText(showOnlyToolTip);

        adjacentCB = new JCheckBox("adjacent", mapPane.getQuiver().adjacentArrowsOn());
        adjacentCB.addItemListener(this);
        adjacentCB.setToolTipText(showOnlyToolTip);

        gridy++;

        JPanel panelShifts = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelShifts.add(new JLabel("Shifts:"));
        panelShifts.add(stableCB);
        panelShifts.add(outlierCB);
        panelShifts.add(adjacentCB);

        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 2;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(panelShifts, bag);

        gridy++;

        sep3 = new JSeparator();
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 2;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(sep3, bag);

        clusterCB = new JCheckBox("Cluster Shifts", mapPane.getQuiver().clusterArrowsOn());
        clusterCB.setToolTipText("Toggle Cluster Shifts Visualisation - show equivalences between clusters in the two SOMs.");
        clusterCB.addItemListener(this);

        clusterLabel = new JLabel("# Clusters");
        clusterLabel.setToolTipText("Select the number of clusters");

        clusterSpinner = new JSpinner(new SpinnerNumberModel(mapPane.getSOMComparision().getClusterNo(), 1,
                mapPane.getSOMComparision().MAXCLUSTERNO, 1));
        clusterSpinner.setToolTipText("Select the number of clusters");
        clusterSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner src = (JSpinner) e.getSource();
                int clusterNo = ((Integer) src.getValue()).intValue();
                mapPane.getSOMComparision().setClusterNo(clusterNo);
                mapPane.getQuiver().computeArrows();
            }
        });

        multiMatchCB = new JCheckBox("Multi-Match", mapPane.getQuiver().clusterArrowsOn());
        multiMatchCB.setToolTipText("Whether a cluster should be only matched once, or can be best-match for more");
        multiMatchCB.addItemListener(this);

        JPanel panelClusterShifts = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelClusterShifts.add(clusterCB);
        panelClusterShifts.add(clusterLabel);
        panelClusterShifts.add(clusterSpinner);
        JPanel panelClusterShifts2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelClusterShifts2.add(multiMatchCB);

        gridy++;
        bag.gridx = 0;
        bag.gridy = gridy;
        bag.gridwidth = 2;
        bag.anchor = GridBagConstraints.WEST;
        getContentPane().add(panelClusterShifts, bag);
        bag.gridy += 1;
        getContentPane().add(panelClusterShifts2, bag);

        repaint();
    }

    public void initNoShiftsInfo() {
        // remove panel elements
        this.removeAllGUIElements();

        getContentPane().add(noShiftInfoLoaded);
        repaint();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void switchCountSpinners(boolean absolute) {

        if (absolute) {
            ChangeListener[] listeners = outlierCountSpinner.getChangeListeners();
            for (ChangeListener listener : listeners) {
                outlierCountSpinner.removeChangeListener(listener);
            }
            outlierCountSpinner.addChangeListener(new outlierAbsoluteChangeListener());
            outlierCountSpinner.setModel(new SpinnerNumberModel(mapPane.getSOMComparision().getOutlierCountThreshold(),
                    1, mapPane.getSOMComparision().getMaxCount(), 1));

            listeners = stableCountSpinner.getChangeListeners();
            for (ChangeListener listener : listeners) {
                stableCountSpinner.removeChangeListener(listener);
            }
            stableCountSpinner.addChangeListener(new stableAbsoluteChangeListener());
            stableCountSpinner.setModel(new SpinnerNumberModel(mapPane.getSOMComparision().getStableCountThreshold(),
                    1, mapPane.getSOMComparision().getMaxCount(), 1));

            minAbsoluteCountSpinner.setEnabled(false);
            minAbsoluteCLabel.setEnabled(false);

        } else {
            ChangeListener[] listeners = outlierCountSpinner.getChangeListeners();
            for (ChangeListener listener : listeners) {
                outlierCountSpinner.removeChangeListener(listener);
            }
            outlierCountSpinner.addChangeListener(new outlierPercentChangeListener());
            outlierCountSpinner.setModel(new SpinnerNumberModel(
                    mapPane.getSOMComparision().getOutlierPercentThreshold(), 0, 100, 1));

            listeners = stableCountSpinner.getChangeListeners();
            for (ChangeListener listener : listeners) {
                stableCountSpinner.removeChangeListener(listener);
            }
            stableCountSpinner.addChangeListener(new stablePercentChangeListener());
            stableCountSpinner.setModel(new SpinnerNumberModel(mapPane.getSOMComparision().getStablePercentThreshold(),
                    0, 100, 1));
            minAbsoluteCountSpinner.setEnabled(true);
            minAbsoluteCLabel.setEnabled(true);
        }

        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("bottom") || e.getActionCommand().equals("top")
                || e.getActionCommand().equals("left") || e.getActionCommand().equals("right")) {
            mapPane.setSecSOMPosition(e.getActionCommand());
            mapPane.getQuiver().computeArrows();
        }
        if (e.getActionCommand().equals("percent") || e.getActionCommand().equals("absolute")) {

            boolean absolute = true;
            if (e.getActionCommand().equals("percent")) {
                absolute = false;
            }

            mapPane.getSOMComparision().setAbsolute(absolute);
            this.switchCountSpinners(absolute);
            mapPane.getQuiver().computeArrows();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        boolean selected = false;

        if (e.getStateChange() == ItemEvent.SELECTED) {
            selected = true;
        }

        Object source = e.getItemSelectable();
        if (source == outlierCB) {
            mapPane.getQuiver().enableOutlierArrows(selected);
        } else if (source == stableCB) {
            mapPane.getQuiver().enableStableArrows(selected);
        } else if (source == adjacentCB) {
            mapPane.getQuiver().enableAdjacentArrows(selected);
        } else if (source == clusterCB) {
            this.setThresholdBlock(!selected);
            mapPane.getQuiver().enableClusterArrows(selected);
        } else if (source == multiMatchCB) {
            mapPane.getQuiver().setMultiMatch(selected);
        } else if (source == cumulativeCB) {
            mapPane.getQuiver().setCumulative(selected);
        }
    }

    private class stableAbsoluteChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner src = (JSpinner) e.getSource();
            int countThreshold = ((Integer) src.getValue()).intValue();
            mapPane.getSOMComparision().setStableCountThreshold(countThreshold);
            mapPane.getQuiver().computeArrows();
        }
    }

    private class stablePercentChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner src = (JSpinner) e.getSource();
            double countThreshold = ((Double) src.getValue()).doubleValue();
            mapPane.getSOMComparision().setStablePercentThreshold(countThreshold);
            mapPane.getQuiver().computeArrows();
        }
    }

    private class outlierAbsoluteChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner src = (JSpinner) e.getSource();
            int countThreshold = ((Integer) src.getValue()).intValue();
            mapPane.getSOMComparision().setOutlierCountThreshold(countThreshold);
            mapPane.getQuiver().computeArrows();
        }
    }

    private class outlierPercentChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner src = (JSpinner) e.getSource();
            double countThreshold = ((Double) src.getValue()).doubleValue();
            mapPane.getSOMComparision().setOutlierPercentThreshold(countThreshold);
            mapPane.getQuiver().computeArrows();
        }
    }
}
