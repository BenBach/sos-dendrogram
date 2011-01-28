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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Option;
import com.martiansoftware.jsap.stringparsers.FileStringParser;

/**
 * @author Jakob Frank
 * @version $Id: FileEditor.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class FileEditor extends OptionEditor {

    private static final long serialVersionUID = 1L;

    private static File globalPreviousCurrentDir = new File(System.getProperty("user.home"));

    private final JTextField txtInput;

    /**
     * @param option The option to edit.
     */
    public FileEditor(Option option) {
        super(option);
        if (!(option.getStringParser() instanceof FileStringParser)) {
            throw new IllegalArgumentException(
                    "FileEditor accept only options with 'FileStringParser'; the option passed uses '"
                            + option.getStringParser().getClass().getSimpleName() + "'.");
        }
        txtInput = new JTextField();
        initialize();
    }

    private void initialize() {
        txtInput.setColumns(20);
        txtInput.getDocument().addDocumentListener(new DocumentListener() {

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

        final JFileChooser fileChooser = new JFileChooser();

        // set the file chooser accept option according to the parser
        final FileStringParser parser = (FileStringParser) option.getStringParser();
        if (parser.mustBeDirectory()) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else if (parser.mustBeFile()) {
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        } else {
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
        if (parser.getFileFilter() != null) {
            fileChooser.setFileFilter(parser.getFileFilter());
        }

        GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
        gridBagConstraints7.fill = GridBagConstraints.BOTH;
        GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
        gridBagConstraints6.fill = GridBagConstraints.BOTH;
        gridBagConstraints6.weightx = 1.0;
        add(txtInput, gridBagConstraints6);
        final JButton btnFileChooser = new JButton();
        btnFileChooser.setMargin(new Insets(0, 0, 0, 0));
        btnFileChooser.setText("...");
        btnFileChooser.setToolTipText("Browse for the file/directory");
        btnFileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (txtInput.getText().trim().length() < 1) {
                    fileChooser.setCurrentDirectory(globalPreviousCurrentDir);
                } else {
                    File current = new File(txtInput.getText());
                    if (!current.isDirectory()) {
                        current = current.getParentFile();
                    }
                    fileChooser.setCurrentDirectory(current);
                }

                int result;
                if (parser.mustExist()) {
                    result = fileChooser.showOpenDialog(FileEditor.this);
                } else {
                    result = fileChooser.showSaveDialog(FileEditor.this);
                }
                if (result == JFileChooser.APPROVE_OPTION) {
                    final File selectedFile = fileChooser.getSelectedFile();
                    globalPreviousCurrentDir = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
                    txtInput.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        add(btnFileChooser, gridBagConstraints7);
        txtInput.addPropertyChangeListener("enabled", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                btnFileChooser.setEnabled(evt.getNewValue().equals(Boolean.TRUE));
            }
        });

    }

    @Override
    public void setEnabled(boolean enabled) {
        txtInput.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setToolTipText(java.lang.String)
     */
    @Override
    public void setToolTipText(String text) {
        txtInput.setToolTipText(text);
    }

    @Override
    boolean checkValidity() {
        if (option.required() && txtInput.getText().trim().length() == 0) {
            return false;
        }
        if (txtInput.getText().trim().length() > 0 && !new File(txtInput.getText()).exists()) {
            return false;
        }

        return true;
    }

    @Override
    String getArgument() {
        return txtInput.getText();
    }

    /* (non-Javadoc)
     * @see at.tuwien.ifs.commons.gui.jsap.OptionEditor#setInitialValue(com.martiansoftware.jsap.JSAPResult)
     */
    @Override
    public void setInitialValue(JSAPResult result) {
        if (result.userSpecified(option.getID())) {
            userInput = true;
            txtInput.setText(result.getFile(option.getID()).getPath());
        }
    }
}
