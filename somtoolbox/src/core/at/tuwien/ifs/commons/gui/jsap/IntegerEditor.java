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

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Option;

/**
 * @author Jakob Frank
 * @version $Id: IntegerEditor.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class IntegerEditor extends OptionEditor {

    private static final long serialVersionUID = 1L;

    private final JSpinner number;

    private String defaultString;

    public IntegerEditor(Option option) {
        super(option);

        final SpinnerNumberModel model = new SpinnerNumberModel();
        model.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                userInput = true;
            }
        });
        number = new JSpinner(model);
        if (option.getDefault() != null) {
            defaultString = getFlatDefaultList();
            try {
                model.setValue(Integer.parseInt(defaultString));
            } catch (NumberFormatException e) {
                defaultString = "";
            }
        } else {
            defaultString = "";
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(number, gbc);

    }

    @Override
    boolean checkValidity() {
        try {
            Integer.parseInt(number.getValue().toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    String getArgument() {
        return number.getValue().toString();
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setToolTipText(java.lang.String)
     */
    @Override
    public void setToolTipText(String text) {
        number.setToolTipText(text);
    }

    /* (non-Javadoc)
     * @see at.tuwien.ifs.commons.gui.jsap.OptionEditor#setInitialValue(com.martiansoftware.jsap.JSAPResult)
     */
    @Override
    public void setInitialValue(JSAPResult result) {
        if (result.userSpecified(option.getID())) {
            userInput = true;
            number.setValue(result.getInt(option.getID()));
        }
    }

}
