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

import java.awt.Color;
import java.awt.Window;

import javax.swing.event.ChangeEvent;

public class ClusterBoderColorChooser extends ColorChooser {
    private static final long serialVersionUID = 1L;

    private ClusteringControl clusteringControl;

    public ClusterBoderColorChooser(Window parent, Color color, ClusteringControl clusteringControl) {
        super(parent, color, "Select cluster border colour");
        this.clusteringControl = clusteringControl;
        setVisible(true);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        clusteringControl.updateClusterColourSelection(getColor());
    }

}
