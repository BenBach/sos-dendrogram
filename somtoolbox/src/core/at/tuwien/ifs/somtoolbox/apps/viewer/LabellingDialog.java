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
package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.output.labeling.LabelSOM;
import at.tuwien.ifs.somtoolbox.util.CentredDialog;

/**
 * Implements a graphical control element to run or re-run labelling of the map.
 * 
 * @author Rudolf Mayer
 * @version $Id: LabellingDialog.java 3873 2010-10-28 09:29:58Z frank $
 */
public class LabellingDialog extends CentredDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    private JCheckBox methodCheckBox;

    private JSpinner numLabelsSpinner;

    private JButton runLabellingButton;

    private SpinnerNumberModel spinnerNumberModel;

    CommonSOMViewerStateData state;

    public LabellingDialog(CommonSOMViewerStateData state) throws HeadlessException {
        super(state.parentFrame, "SOM Labelling", true);
        this.state = state;
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.ipadx = 5;
        c.ipadx = 2;
        c.insets = new Insets(5, 5, 5, 5);

        c.gridy = 0;

        Font headerFont = new Font("", Font.BOLD, 12);

        JLabel typeLabel = new JLabel("Labelling method");
        typeLabel.setFont(headerFont);
        c.gridwidth = 2;
        getContentPane().add(typeLabel, c);
        c.gridwidth = 1;

        JLabel numLabelsLabel = new JLabel("# of Labels");
        numLabelsLabel.setFont(headerFont);
        getContentPane().add(numLabelsLabel, c);

        c.gridy = c.gridy + 1;

        methodCheckBox = new JCheckBox();
        getContentPane().add(methodCheckBox, c);

        JLabel methodLabel = new JLabel("LabelSOM");
        getContentPane().add(methodLabel, c);

        spinnerNumberModel = new SpinnerNumberModel(5, 0, 100, 1);
        numLabelsSpinner = new JSpinner(spinnerNumberModel);
        getContentPane().add(numLabelsSpinner, c);

        c.gridy = c.gridy + 1;
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 3;
        runLabellingButton = new JButton("label");
        runLabellingButton.addActionListener(this);
        getContentPane().add(runLabellingButton, c);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LabelSOM labeler = new LabelSOM();
        SOMVisualisationData inputVectorObject = state.inputDataObjects.getObject(SOMVisualisationData.INPUT_VECTOR);
        SOMVisualisationData templateVectorObject = state.inputDataObjects.getObject(SOMVisualisationData.TEMPLATE_VECTOR);
        try {
            boolean hasInput = inputVectorObject.hasData()
                    || inputVectorObject.loadFromFile(state.fileChooser, state.parentFrame);
            boolean hasTemplate = false;
            if (hasInput) {
                hasTemplate = templateVectorObject.hasData()
                        || templateVectorObject.loadFromFile(state.fileChooser, state.parentFrame);
            }
            if (hasInput && hasTemplate) {
                InputData inputData = (InputData) inputVectorObject.getData();
                inputData.setTemplateVector((TemplateVector) templateVectorObject.getData());
                labeler.label(state.growingSOM, inputData, spinnerNumberModel.getNumber().intValue());
                state.mapPNode.reInitLabels();
            } else {
                JOptionPane.showMessageDialog(state.parentFrame,
                        "Both Input Vector and Template Vector file are needed for labelling!", "Error!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SOMToolboxException ex) {
            JOptionPane.showMessageDialog(state.parentFrame, ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        }

    }

}
