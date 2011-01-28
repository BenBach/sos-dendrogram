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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Rudolf Mayer
 * @version $Id: RunResultSpecifierPane.java 3583 2010-05-21 10:07:41Z mayer $
 */

public class RunResultSpecifierPane extends JPanel {

    public static final long serialVersionUID = 1l;

    private int id = -1;

    private ReportGenWindow controller;

    private JLabel headerLabel = null;

    private JTextField weightText = null;

    private JTextField mapText = null;

    private JTextField unitText = null;

    private JTextField propertyText = null;

    private JTextField dwText = null;

    private JFileChooser fileChooser;

    public RunResultSpecifierPane(int id, ReportGenWindow controller) {
        this.id = id;
        this.controller = controller;

        // initialize the UI component
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        this.add(getHeaderPane());
        this.add(getMapPane());
        this.add(getPropertyPane());
        this.add(getWeightPane());
        this.add(getUnitPane());
        this.add(getDWPane());
    }

    public String getMapFilePath() {
        return this.mapText.getText();
    }

    public String getUnitFilePath() {
        return this.unitText.getText();
    }

    public String getWeightFilePath() {
        return this.weightText.getText();
    }

    public String getPropertyFilePath() {
        return this.propertyText.getText();
    }

    public String getDwFilePath() {
        return this.dwText.getText();
    }

    /**
     * updates the id of the object to the given value. this may be necessary if another RunResultSpecifier has been
     * removed from a list.
     * 
     * @param newId the new id of this object
     */
    public void updateId(int newId) {
        this.id = newId;
        this.headerLabel.setText("Results of run " + (this.id + 1) + ":");
    }

    /* PRIVATE UI PART */

    /** creates and returns the header Panel (the one with the header text and the remove Button */
    private JPanel getHeaderPane() {
        JPanel headerPane = new JPanel(new FlowLayout(FlowLayout.LEFT));

        headerLabel = new JLabel("Results of run " + (this.id + 1) + ":");
        headerPane.add(headerLabel);

        JButton removeButton = new JButton("remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.removeTestrun(id);
            }
        });
        headerPane.add(removeButton);
        return headerPane;
    }

    /** creates and returns the Panel that contains the Values for specifying the location of the .wgt file */
    private JPanel getWeightPane() {
        JPanel weightPane = new JPanel(new FlowLayout(FlowLayout.LEFT));

        weightPane.add(new JLabel("weight vector file (*.wgt[.gz]):"));

        weightText = new JTextField(controller.getWeightPath(), 10);
        weightPane.add(weightText);

        JButton weightBrowseButton = new JButton("Browse");
        weightBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getFileChooser(weightText.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    weightText.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        weightPane.add(weightBrowseButton);
        return weightPane;
    }

    /** creates and returns the Panel that contains the Values for specifying the location of the .map file */
    private JPanel getMapPane() {
        JPanel mapPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mapPane.add(new JLabel("map description file (*.map[.gz]):"));

        mapText = new JTextField(controller.getMapPath(), 10);
        mapPane.add(mapText);

        JButton mapBrowseButton = new JButton("Browse");
        mapBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getFileChooser(mapText.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    mapText.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        mapPane.add(mapBrowseButton);
        return mapPane;
    }

    /** creates and returns the Panel that contains the Values for specifying the location of the .unit file */
    private JPanel getUnitPane() {
        JPanel unitPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        unitPane.setPreferredSize(new Dimension(380, 25));

        unitPane.add(new JLabel("unit vector file (*.unit[.gz]):"));

        unitText = new JTextField(controller.getUnitPath(), 10);
        unitPane.add(unitText);

        JButton unitBrowseButton = new JButton("Browse");
        unitBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getFileChooser(unitText.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    unitText.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        unitPane.add(unitBrowseButton);
        return unitPane;
    }

    /** creates and returns the Panel that contains the Values for specifying the location of the .props file */
    private JPanel getPropertyPane() {
        JPanel propertyPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        propertyPane.setPreferredSize(new Dimension(380, 25));

        propertyPane.add(new JLabel("property file (*.prop):"));

        propertyText = new JTextField(controller.getPropertiesPath(), 10);
        propertyPane.add(propertyText);

        JButton propertyBrowseButton = new JButton("Browse");
        propertyBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getFileChooser(propertyText.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    propertyText.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        propertyPane.add(propertyBrowseButton);
        return propertyPane;
    }

    /** creates and returns the Panel that contains the Values for specifying the location of the .dw file */
    private JPanel getDWPane() {
        JPanel dwPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dwPane.setPreferredSize(new Dimension(380, 25));
        dwPane.add(new JLabel("data winner file (*.dwm[.gz]):"));

        dwText = new JTextField(controller.getDataWinnerMappingPath(), 10);
        dwPane.add(dwText);

        JButton dwBrowseButton = new JButton("Browse");
        dwBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getFileChooser(dwText.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    dwText.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        dwPane.add(dwBrowseButton);
        return dwPane;
    }

    private JFileChooser getFileChooser(String path) {
        fileChooser = new JFileChooser(path);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        return fileChooser;
    }

}
