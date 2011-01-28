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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Option;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;

/**
 * @author frank
 * @version $Id: EnumerationEditor.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class EnumerationEditor extends OptionEditor {

    private static final long serialVersionUID = 1L;

    private final JComboBox combo;

    public EnumerationEditor(Option option) {
        super(option);

        EnumeratedStringParser esp = (EnumeratedStringParser) option.getStringParser();

        String[] values;
        String[] v = esp.getValidOptionValues();
        if (option.required()) {
            values = v;
        } else { // add an empty option at the beginning, to allow selecting no value
            values = new String[v.length + 1];
            values[0] = "";
            System.arraycopy(v, 0, values, 1, v.length);
        }
        combo = new JComboBox(values);
        if (option.getDefault() != null && option.getDefault().length > 0) {
            combo.setSelectedItem(option.getDefault()[0]);
        }
        combo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                userInput = true;
            }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(combo, gbc);
    }

    @Override
    boolean checkValidity() {
        if (option.required() && combo.getSelectedItem().equals("")) {
            return false;
        } else {
            return true;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setToolTipText(java.lang.String)
     */
    @Override
    public void setToolTipText(String text) {
        combo.setToolTipText(text);
    }

    @Override
    String getArgument() {
        return combo.getSelectedItem().toString();
    }

    /* (non-Javadoc)
     * @see at.tuwien.ifs.commons.gui.jsap.OptionEditor#setInitialValue(com.martiansoftware.jsap.JSAPResult)
     */
    @Override
    public void setInitialValue(JSAPResult result) {
        if (result.userSpecified(option.getID())) {
            userInput = true;
            combo.setSelectedItem(result.getString(option.getID()));
        }
    }

}
