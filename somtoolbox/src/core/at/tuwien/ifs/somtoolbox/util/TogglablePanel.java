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
package at.tuwien.ifs.somtoolbox.util;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A Panel that can toggle the visibility of its components.
 * 
 * @author Rudolf Mayer
 * @version $Id: TogglablePanel.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class TogglablePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final String TEXT_CLOSE = "<<<<";

    public static final String TEXT_OPEN = ">>>>";

    protected boolean expandedStatus = true;

    public TogglablePanel(LayoutManager layout) {
        super(layout);
    }

    protected void toggleState(JLabel trigger) {
        expandedStatus = !expandedStatus;
        if (expandedStatus) {
            trigger.setText(trigger.getText().replace(TEXT_OPEN, TEXT_CLOSE));
        } else {
            trigger.setText(trigger.getText().replace(TEXT_CLOSE, TEXT_OPEN));
        }
        for (Component c : getComponents()) {
            if (c != trigger) {
                c.setVisible(expandedStatus);
            }
        }
    }

}