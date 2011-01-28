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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.util.ArrayUtils;

/**
 * This class implements a panel that allows the user to change various details of the map.
 * 
 * @author Rudolf Mayer
 * @version $Id: MapDetailPanel.java 3873 2010-10-28 09:29:58Z frank $
 */
public class MapDetailPanel extends AbstractViewerControl {

    private static final long serialVersionUID = 1L;

    protected JSpinner[] spinnerThreshold = new JSpinner[GeneralUnitPNode.NUMBER_OF_DETAIL_LEVELS];

    private JLabel labelCurrentZoom;

    public MapDetailPanel(String title, CommonSOMViewerStateData state) {
        super(title, state, new GridBagLayout());

        JPanel inputThresholdPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constrThreshold = new GridBagConstraints();
        constrThreshold.gridx = GridBagConstraints.RELATIVE;
        constrThreshold.gridy = 0;
        constrThreshold.fill = GridBagConstraints.BOTH;
        constrThreshold.gridwidth = 2;

        inputThresholdPanel.add(new JLabel("Current zoom: "), constrThreshold);
        labelCurrentZoom = new JLabel("");// GeneralUnitPNode.getCurrentZoomLevel());
        inputThresholdPanel.add(labelCurrentZoom, constrThreshold);

        constrThreshold.gridwidth = 1;
        constrThreshold.gridy += 1;

        inputThresholdPanel.add(new JLabel("No"), constrThreshold);
        inputThresholdPanel.add(new JLabel("Low"), constrThreshold);
        inputThresholdPanel.add(new JLabel("Medium"), constrThreshold);
        inputThresholdPanel.add(new JLabel("High"), constrThreshold);
        constrThreshold.gridy += 1;

        for (int i = 0; i < spinnerThreshold.length; i++) {
            spinnerThreshold[i] = new JSpinner(new SpinnerNumberModel(state.thresholdInputPercentage[i], 0, 100, 1));
            spinnerThreshold[i].setFont(smallerFont);
            spinnerThreshold[i].addChangeListener(new MapDetailChangeListener(i));
            inputThresholdPanel.add(spinnerThreshold[i], constrThreshold);
        }

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1.0;
        c.gridwidth = 4;

        getContentPane().add(inputThresholdPanel, c);
        c.gridy += 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 2;

    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    // @Override
    // public Dimension getPreferredSize() {
    // return new Dimension(state.controlElementsWidth, 75);
    // }

    public void updatePanel(double scale) {
        String detailLevel = GeneralUnitPNode.detailLevelNames[ArrayUtils.getBinIndex(scale, state.scaleLimits)];
        updatePanel(detailLevel);
    }

    public void updatePanel(String detailLevel) {
        labelCurrentZoom.setText(detailLevel);
    }

    protected class MapDetailChangeListener implements ChangeListener {
        private int detailLevel;

        public MapDetailChangeListener(int detailLevel) {
            super();
            this.detailLevel = detailLevel;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (detailLevel > 0 && detailLevel < spinnerThreshold.length) {
                state.thresholdInputPercentage[detailLevel] = ((Integer) spinnerThreshold[detailLevel].getValue()).intValue();
                state.mapPNode.reInitUnitPNodes(detailLevel);
            }
        }

    }

}
