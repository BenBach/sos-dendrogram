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

import edu.umd.cs.piccolo.nodes.PText;

/**
 * The color chooser panel used to edit colors of labels.
 * 
 * @author Angela Roiger
 * @version $Id: LabelEditColorChooser.java 3873 2010-10-28 09:29:58Z frank $
 */
public class LabelEditColorChooser extends ColorChooser {
    private static final long serialVersionUID = 1L;

    PText text = null;

    /**
     * Creates and shows a new color chooser window.
     * 
     * @param t the PText of which the color will be changed.
     */
    public LabelEditColorChooser(Window parent, PText t) {
        super(parent, (Color) t.getTextPaint(), "Changing color for label '" + t.getText() + "'");
        this.text = t;
        setVisible(true);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        text.setTextPaint(cc.getColor());
    }

}
