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
package at.tuwien.ifs.somtoolbox.reportgenerator.gui;

import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import at.tuwien.ifs.somtoolbox.data.InputDatum;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: InputVectorSelectorPane.java 3585 2010-05-21 10:33:21Z mayer $
 */

public class InputVectorSelectorPane extends JPanel {

    static final long serialVersionUID = 1701;

    private int id = -1;

    private String name = "";

    private JCheckBox jCheckBox = null;

    private JLabel jLabel = null;

    public InputVectorSelectorPane(int id, String name, InputDatum inputVector) {

        super();

        this.name = name;
        this.id = id;
        // this.inputVector = inputVector;

        if (this.name.length() == 0) {
            this.name = "" + this.id;
        }

        // initialize the UI component
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        jLabel = new JLabel();
        jLabel.setText(this.name + " (" + getInputLabelDetails(inputVector) + ")");
        this.setLayout(flowLayout);
        this.add(getJCheckBox());
        this.add(jLabel);

    }

    private String getInputLabelDetails(InputDatum inputVector) {
        String values = "";
        for (int i = 0; i < inputVector.getDim(); i++) {
            if (i > 0) {
                values += "; ";
            }
            values += String.format("%.3f", inputVector.getVector().get(i));
        }
        return values;
    }

    /**
     * selects this Entry
     */
    public void select() {
        this.getJCheckBox().setSelected(true);
        this.updateUI();
    }

    /**
     * unselects this Entry
     */
    public void unselect() {
        this.getJCheckBox().setSelected(false);
        this.updateUI();
    }

    /**
     * This method initializes jCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBox() {
        if (jCheckBox == null) {
            jCheckBox = new JCheckBox();
        }
        return jCheckBox;
    }

    /**
     * returns whether this input vector is selected or not
     * 
     * @return true if selected, false if not
     */
    public boolean isSelected() {
        return this.jCheckBox.isSelected();
    }

    /**
     * returns the id of this vector (that is its index in all lists and the input file
     * 
     * @return the index of the vector
     */
    public int getId() {
        return this.id;
    }
}
