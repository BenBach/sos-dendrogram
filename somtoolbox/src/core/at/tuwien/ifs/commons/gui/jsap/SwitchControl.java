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
package at.tuwien.ifs.commons.gui.jsap;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

/**
 * @author Jakob Frank
 * @version $Id: SwitchControl.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class SwitchControl extends ParameterControl {

    private final Switch sw;

    private final JCheckBox control;

    private JLabel label;

    /**
     * @param param The Switch this Editor is for.
     */
    public SwitchControl(Switch param) {
        super(param);
        sw = param;
        control = new JCheckBox(sw.getUsageName());
        control.setToolTipText(formatToolTip(sw.getHelp()));
    }

    /**
     * @param param The Switch this Editor is for.
     * @param result initial value;
     */
    public SwitchControl(Switch param, JSAPResult result) {
        this(param);
        if (result.userSpecified(param.getID())) {
            control.setSelected(result.getBoolean(param.getID()));
        }
    }

    @Override
    public JComponent getEditor() {
        return control;
    }

    @Override
    public String[] getCommandLine() {
        if (control.isSelected()) {
            return new String[] { createFlagString(sw.getShortFlag(), sw.getLongFlag()) };
        }
        return new String[] {};
    }

    @Override
    public JLabel getLabel() {
        if (label == null) {
            label = new JLabel("");
            label.setToolTipText(formatToolTip(sw.getHelp()));
        }
        return label;
    }

    @Override
    boolean isValid() {
        return true;
    }

    /* (non-Javadoc)
     * @see at.tuwien.ifs.commons.gui.jsap.ParameterControl#isRequired()
     */
    @Override
    public boolean isRequired() {
        return false;
    }

}
