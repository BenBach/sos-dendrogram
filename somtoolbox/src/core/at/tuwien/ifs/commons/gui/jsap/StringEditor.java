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

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Option;

/**
 * @author Jakob Frank
 * @version $Id: StringEditor.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class StringEditor extends OptionEditor {

    private static final long serialVersionUID = 1L;

    private JTextField text;

    private final String defaultString;

    public StringEditor(Option option) {
        super(option);
        text = new JTextField();
        text.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                userInput = true;
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                userInput = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                userInput = true;
            }
        });
        if (option.getDefault() != null) {
            defaultString = getFlatDefaultList();
            text.setText(defaultString);
        } else {
            defaultString = "";
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(text, gbc);
    }

    @Override
    boolean checkValidity() {
        if (option.required() && text.getText().trim().length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    String getArgument() {
        return text.getText();
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setToolTipText(java.lang.String)
     */
    @Override
    public void setToolTipText(String text) {
        this.text.setToolTipText(text);
    }

    /* (non-Javadoc)
     * @see at.tuwien.ifs.commons.gui.jsap.OptionEditor#setInitialValue(com.martiansoftware.jsap.JSAPResult)
     */
    @Override
    public void setInitialValue(JSAPResult result) {
        if (result.userSpecified(option.getID())) {
            userInput = true;
            text.setText(result.getString(option.getID()));
        }
    }
}
