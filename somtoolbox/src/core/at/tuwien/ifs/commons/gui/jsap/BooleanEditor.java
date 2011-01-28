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

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Option;

/**
 * @author frank
 * @version $Id: BooleanEditor.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class BooleanEditor extends OptionEditor {

    private static final long serialVersionUID = 1L;

    private final JCheckBox box;

    /**
     * @param option The Option this editor is for.
     */
    public BooleanEditor(Option option) {
        super(option);

        box = new JCheckBox();
        box.setText(option.getUsageName());
        box.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                userInput = true;
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(box, gbc);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setToolTipText(java.lang.String)
     */
    @Override
    public void setToolTipText(String text) {
        box.setToolTipText(text);
    }

    @Override
    boolean checkValidity() {
        return true;
    }

    @Override
    String getArgument() {
        return Boolean.toString(box.isSelected());
    }

    /* (non-Javadoc)
     * @see at.tuwien.ifs.commons.gui.jsap.OptionEditor#setInitialValue(com.martiansoftware.jsap.JSAPResult)
     */
    @Override
    public void setInitialValue(JSAPResult result) {
        if (result.userSpecified(option.getID())) {
            userInput = true;
            box.setSelected(result.getBoolean(option.getID()));
        }
    }

}
