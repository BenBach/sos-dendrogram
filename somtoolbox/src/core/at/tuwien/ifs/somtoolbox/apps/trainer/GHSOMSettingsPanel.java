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
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author Jakob Frank
 * @version $Id: GHSOMSettingsPanel.java 3868 2010-10-21 15:52:31Z mayer $
 */
public class GHSOMSettingsPanel extends SOMModelSettingsPanel {

    private static final long serialVersionUID = 1L;

    private JLabel lblTau;

    private JSpinner spnTau;

    private JSpinner spnTau2;

    private JLabel lblTau2;

    /**
     * 
     */
    public GHSOMSettingsPanel() {
        initialize();
    }

    /**
     * 
     */
    private void initialize() {
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 3;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.insets = new Insets(2, 0, 2, 0);
        gridBagConstraints1.gridy = 0;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new Insets(2, 2, 2, 4);
        gridBagConstraints.gridy = 0;
        GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
        gridBagConstraints31.gridx = 1;
        gridBagConstraints31.weightx = 1.0;
        gridBagConstraints31.anchor = GridBagConstraints.WEST;
        gridBagConstraints31.insets = new Insets(2, 0, 2, 0);
        gridBagConstraints31.fill = GridBagConstraints.BOTH;
        gridBagConstraints31.gridy = 0;
        GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
        gridBagConstraints30.gridx = 0;
        gridBagConstraints30.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints30.anchor = GridBagConstraints.EAST;
        gridBagConstraints30.insets = new Insets(2, 2, 2, 4);
        gridBagConstraints30.gridy = 0;
        setLayout(new GridBagLayout());

        lblTau = new JLabel();
        lblTau.setText("tau");

        spnTau = new JSpinner();
        spnTau.setModel(new SpinnerNumberModel(1, 0, 1, .01));

        lblTau2 = new JLabel();
        lblTau2.setText("tau2");

        spnTau2 = new JSpinner();
        spnTau2.setModel(new SpinnerNumberModel(1, 0, 1, .01));

        this.add(lblTau, gridBagConstraints30);
        this.add(spnTau, gridBagConstraints31);
        this.add(lblTau2, gridBagConstraints);
        this.add(spnTau2, gridBagConstraints1);
    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.apps.trainer.SOMModelSettingsPanel#getProperties()
     */
    @Override
    public Properties getProperties() {
        Properties p = new Properties();
        p.setProperty("tau", spnTau.getValue().toString());
        p.setProperty("tau2", spnTau2.getValue().toString());
        return p;
    }

}
