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
package at.tuwien.ifs.somtoolbox.apps.trainer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 * @author Jakob Frank
 * @version $Id: MnemonicSOMSettingsPanel.java 3877 2010-11-02 15:43:17Z frank $
 */
public class MnemonicSOMSettingsPanel extends SOMModelSettingsPanel {
    private static final long serialVersionUID = 1L;

    private JTextField txtUnitFile = null;

    private JButton btnUnitFileOpen = null;

    private JLabel jLabel = null;

    public MnemonicSOMSettingsPanel() {

        initialize();
    }

    /**
     * 
     */
    private void initialize() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new Insets(2, 2, 2, 4);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridy = 0;
        GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
        gridBagConstraints10.insets = new Insets(0, 0, 1, 0);
        gridBagConstraints10.gridx = 2;
        gridBagConstraints10.fill = GridBagConstraints.BOTH;
        GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
        gridBagConstraints6.fill = GridBagConstraints.BOTH;
        gridBagConstraints6.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints6.gridx = 1;
        gridBagConstraints6.weightx = 1.0;
        this.setLayout(new GridBagLayout());
        this.add(getTxtUnitFile(), gridBagConstraints6);
        this.add(getBtnUnitFileOpen(), gridBagConstraints10);
        this.add(getJLabel(), gridBagConstraints);
    }

    /**
     * This method initializes txtUnitFile
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtUnitFile() {
        if (txtUnitFile == null) {
            txtUnitFile = new JTextField();
            txtUnitFile.setColumns(10);
        }
        return txtUnitFile;
    }

    /**
     * This method initializes btnUnitFileOpen
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnUnitFileOpen() {
        if (btnUnitFileOpen == null) {
            btnUnitFileOpen = new JButton();
            btnUnitFileOpen.setText("...");
            btnUnitFileOpen.setMargin(new Insets(0, 0, 0, 0));
            btnUnitFileOpen.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    execFileChooser(txtUnitFile, new FileFilter() {

                        @Override
                        public String getDescription() {
                            return "SOMLib Unit Files";
                        }

                        @Override
                        public boolean accept(File f) {
                            if (f.isDirectory() || f.getName().endsWith(".unit") || f.getName().endsWith(".unit.gz")) {
                                return true;
                            }
                            return false;
                        }
                    }, false, false);
                }
            });
        }
        return btnUnitFileOpen;
    }

    @Override
    public Properties getProperties() {
        return new Properties();
    }

    @Override
    public String[] getAdditionalParams() {
        return new String[] { "-u", getTxtUnitFile().getText() };
    }

    /**
     * This method initializes jLabel
     * 
     * @return javax.swing.JLabel
     */
    private JLabel getJLabel() {
        if (jLabel == null) {
            jLabel = new JLabel();
            jLabel.setText("Unit File");
        }
        return jLabel;
    }

}
