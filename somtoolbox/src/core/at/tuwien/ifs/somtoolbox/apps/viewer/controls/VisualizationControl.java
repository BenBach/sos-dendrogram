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
import java.awt.GridLayout;

import javax.swing.JLabel;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;
import at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer.VisualizationControlPanel;
import at.tuwien.ifs.somtoolbox.visualization.VisualizationUpdateListener;

/**
 * @author Rudolf Mayer
 * @version $Id: VisualizationControl.java 3873 2010-10-28 09:29:58Z frank $
 */
public class VisualizationControl extends AbstractViewerControl implements VisualizationUpdateListener {
    private static final long serialVersionUID = 1L;

    private SOMPane mapPane;

    private final String initialTitle;

    private final JLabel noVisLabel = new JLabel("No visualisation loaded");

    public VisualizationControl(String title, CommonSOMViewerStateData state, SOMPane mapPane) {
        super(title, state, new GridLayout(1, 1));
        initialTitle = title;
        this.mapPane = mapPane;
        // setContentBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        updateVisualisationControl();
        setVisible(true);
    }

    @Override
    public void updateVisualization() {
        mapPane.updateVisualization();
        mapPane.repaint();

        if (getPanel() != null) {
            getPanel().revalidate();
            getPanel().repaint();
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void updateVisualisationControl() {
        if (getContentPane().getComponentCount() > 0) {
            getContentPane().removeAll();
        }
        VisualizationControlPanel panel = getPanel();
        if (panel != null) {
            getContentPane().add(panel);
            mapPane.getCurrentVisualization().setVisualizationUpdateListener(this);
            panel.updateSwitchControls();
            setTitle(panel.getName());
        } else {
            getContentPane().add(noVisLabel);
            // setCollapsed(true);
            setTitle(initialTitle);
        }
        revalidate();
        // getContentPane().repaint();
        // updateVisualization();
    }

    private VisualizationControlPanel getPanel() {
        if (mapPane != null && mapPane.getCurrentVisualization() != null) {
            return mapPane.getCurrentVisualization().getControlPanel();
        } else {
            return null;
        }
    }

}
