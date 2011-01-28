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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.util.CentredDialog;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.VisualisationUtils;

/**
 * Provides a dialog for setting various options when exporting visualisations.
 * 
 * @author Rudolf Mayer
 * @version $Id: ExportDialog.java 3873 2010-10-28 09:29:58Z frank $
 */
public class ExportDialog extends CentredDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    private static final int defaultUnitWidth = 25;

    private JCheckBox exportBackgroundCheckBox = new JCheckBox("Export background image");

    private JButton exportButton = new JButton("Export");

    private JCheckBox exportImagemapCheckBox = new JCheckBox("Export image map");

    private JCheckBox exportVisCheckBox = new JCheckBox("Export visualisation");

    private JCheckBox exportGridCheckBox = new JCheckBox("Draw grid");

    private JCheckBox exportNoteSymbolsCheckBox = new JCheckBox("Draw note symbols");

    private JCheckBox exportHybridImageCheckBox = new JCheckBox("Export hybrid visualisation");

    private JCheckBox regenerateImagesCheckBox = new JCheckBox("Generate visualization image(s)");

    // TODO: enable this only when data-type is audio-rp
    private JCheckBox generateRhythmPatternsCheckBox = new JCheckBox("Generate Rhythm Pattern plots");

    private JCheckBox copyDataCheckBox = new JCheckBox("Copy data items");

    private JTextField fileNameTextField = new JTextField(30);

    private JTextField cleanDataNamesTextField = new JTextField(30);

    private JTextField streamingURLTextField = new JTextField(30);

    private JTextField templatesTextField = new JTextField(30);

    private JTextField titleTextField = new JTextField(30);

    private GrowingLayer layer;

    private SpinnerNumberModel spinnerNumberModel;

    private CommonSOMViewerStateData state;

    private JSpinner widthSpinner;

    private JLabel panelComputedDimension;

    private InputData inputVector;

    private boolean isAudioSOM;

    private String dataDir;

    public ExportDialog(JFrame owner, CommonSOMViewerStateData state) {
        super(owner, "Export", true);
        this.state = state;
        layer = state.growingLayer;
        inputVector = state.inputDataObjects.getInputData();
        isAudioSOM = inputVector != null && inputVector.getContentType().equalsIgnoreCase("audio");
        dataDir = "data";

        // isAudioSOM = true; // temporary fix, should make check for audio type work

        // fileNameTextField.setText(state.viewerWorkingDirectoryName);
        // cleanDataNamesTextField.setText(state.viewerWorkingDirectoryName);
        // streamingURLTextField.setText(state.viewerWorkingDirectoryName);
        titleTextField.setText("My Map");

        // // temporarily for mozart
        // fileNameTextField.setText("/home/mayer/workspace/mozart/mozart");
        // cleanDataNamesTextField.setText("/mnt/nemesis/work/mozart/filelist_edit.txt");
        // streamingURLTextField.setText("http://www.ifs.tuwien.ac.at/mir/mozart/");
        //
        // // temporarily for ISMIR
        // fileNameTextField.setText("/home/mayer/workspace/mir/playsom/demo/ballroom_dance");
        // cleanDataNamesTextField.setText("");
        // streamingURLTextField.setText("http://www.ifs.tuwien.ac.at/mir/playsom/demo/");
        // templatesTextField.setText("/home/mayer/workspace/mir/playsom/demo/style/");
        // titleTextField.setText("PlaySOM-Demo: Ballroom dance music");
        //
        // // temporarily for FoDok
        // fileNameTextField.setText("/home/mayer/work/fodok/webDemo/fodokWebDemo");
        // cleanDataNamesTextField.setText("");
        // streamingURLTextField.setText("http://www.ifs.tuwien.ac.at/~mayer/fodok/demo/");
        // templatesTextField.setText("/home/mayer/work/fodok/webDemo/style");
        // titleTextField.setText("FoDok Austria - Web Demo");
        //
        // // temporarily for ISMIR Poster
        // fileNameTextField.setText("/home/mayer/work/papers/ISMIR2006/poster/screeners");
        //        
        // // temporarily for crime data
        // titleTextField.setText("Crime Data - Simple");
        // fileNameTextField.setText("/home/mayer/work/crime-data/web/simple/som");
        templatesTextField.setText("");

        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);

        getContentPane().add(new JLabel("Output Path + Filename"), c);
        getContentPane().add(fileNameTextField, c);
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File filePath = ExportUtils.getFilePath(ExportDialog.this, ExportDialog.this.state.getFileChooser(),
                        "Export file name base");
                if (filePath != null) {
                    ExportDialog.this.fileNameTextField.setText(filePath.getAbsolutePath());
                }
            }
        });
        getContentPane().add(browseButton, c);

        c.gridy = c.gridy + 1;
        getContentPane().add(new JLabel("HTML templates"), c);
        getContentPane().add(templatesTextField, c);
        JButton browseButtonHTMLTemplates = new JButton("Browse");
        browseButtonHTMLTemplates.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File filePath = ExportUtils.getFilePath(ExportDialog.this, ExportDialog.this.state.getFileChooser(),
                        "HTML templates");
                if (filePath != null) {
                    ExportDialog.this.templatesTextField.setText(filePath.getAbsolutePath());
                }
            }
        });
        getContentPane().add(browseButtonHTMLTemplates, c);

        c.gridy = c.gridy + 1;
        getContentPane().add(new JLabel("Clean Data Names"), c);
        getContentPane().add(cleanDataNamesTextField, c);
        JButton browseButtonDataNames = new JButton("Browse");
        browseButtonDataNames.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File filePath = ExportUtils.getFilePath(ExportDialog.this, ExportDialog.this.state.getFileChooser(),
                        "Data item name mapping");
                if (filePath != null) {
                    ExportDialog.this.cleanDataNamesTextField.setText(filePath.getAbsolutePath());
                }
            }
        });
        getContentPane().add(browseButtonDataNames, c);

        // if (isAudioSOM) {
        c.gridy = c.gridy + 1;
        getContentPane().add(new JLabel("Data Dir / Streaming URL"), c);
        getContentPane().add(streamingURLTextField, c);
        JButton browseButtonStreamingURL = new JButton("Browse");
        browseButtonStreamingURL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File filePath = ExportUtils.getFilePath(ExportDialog.this, ExportDialog.this.state.getFileChooser(),
                        "Streaming URL");
                if (filePath != null) {
                    ExportDialog.this.streamingURLTextField.setText(filePath.getAbsolutePath());
                }
            }
        });
        getContentPane().add(browseButtonStreamingURL, c);
        // }

        c.gridy = c.gridy + 1;
        getContentPane().add(new JLabel("Unit-width: "), c);
        // spinnerNumberModel = new SpinnerNumberModel(layer.getXSize() * defaultUnitWidth, layer.getXSize(),
        // layer.getXSize() * 200, layer.getXSize());
        spinnerNumberModel = new SpinnerNumberModel(defaultUnitWidth, 1, 200, 1);
        widthSpinner = new JSpinner(spinnerNumberModel);
        widthSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // panelComputedHeight.setText(String.valueOf(((Integer) spinnerNumberModel.getValue()).intValue()
                // / defaultUnitWidth * layer.getYSize()));
                int intValue = ((Integer) spinnerNumberModel.getValue()).intValue();
                panelComputedDimension.setText(intValue * layer.getXSize() + "x" + intValue * layer.getYSize());
            }
        });
        JPanel panelWidth = new JPanel();
        panelWidth.add(widthSpinner, c);
        panelWidth.add(new JLabel(" == "));
        panelComputedDimension = new JLabel(defaultUnitWidth * layer.getXSize() + "x" + defaultUnitWidth
                * layer.getYSize());
        panelWidth.add(panelComputedDimension, c);
        panelWidth.add(new JLabel("pixel"));
        getContentPane().add(panelWidth, c);

        c.gridy = c.gridy + 1;
        getContentPane().add(new JLabel("Image map header"), c);
        getContentPane().add(titleTextField, c);

        c.gridwidth = 3;
        c.gridy = c.gridy + 1;
        JPanel exportImagesPanel = new JPanel(new GridLayout(1, 3));
        if (state.mapPNode.getCurrentVisualization() == null) {
            exportVisCheckBox.setEnabled(false);
        } else {
            exportVisCheckBox.setSelected(true);
        }
        exportImagesPanel.add(exportVisCheckBox, c);

        if (state.mapPNode.getBackgroundImage() == null) {
            exportBackgroundCheckBox.setEnabled(false);
        } else {
            exportBackgroundCheckBox.setSelected(true);
        }
        exportImagesPanel.add(exportBackgroundCheckBox, c);

        if (state.mapPNode.getBackgroundImage() != null && state.mapPNode.getCurrentVisualization() != null) {
            exportHybridImageCheckBox.setSelected(true);
        } else {
            exportHybridImageCheckBox.setEnabled(false);
        }
        exportImagesPanel.add(exportHybridImageCheckBox, c);
        getContentPane().add(exportImagesPanel, c);

        c.gridy = c.gridy + 1;
        JPanel imageLayerPanel = new JPanel(new GridLayout(1, 3));
        if (state.mapPNode.getBackgroundImage() != null || state.mapPNode.getCurrentVisualization() != null) {
            exportGridCheckBox.setSelected(true);
        } else {
            // exportGridCheckBox.setEnabled(false);
        }
        imageLayerPanel.add(exportGridCheckBox, c);

        getContentPane().add(imageLayerPanel, c);

        c.gridy = c.gridy + 1;
        JPanel imageMapPanel = new JPanel(new GridLayout(1, 3));
        exportImagemapCheckBox.setSelected(true);
        imageMapPanel.add(exportImagemapCheckBox, c);
        getContentPane().add(imageMapPanel, c);

        JPanel regeneratePanel = new JPanel(new GridLayout(1, 3));
        c.gridy = c.gridy + 1;
        regenerateImagesCheckBox.setSelected(true);
        regeneratePanel.add(regenerateImagesCheckBox, c);
        getContentPane().add(regeneratePanel, c);

        if (isAudioSOM) {
            JPanel audioPanel = new JPanel(new GridLayout(1, 3));
            audioPanel.setBorder(BorderFactory.createTitledBorder("Audio"));
            c.gridy = c.gridy + 1;
            // c.fill = GridBagConstraints.HORIZONTAL;

            if (state.mapPNode.getBackgroundImage() != null || state.mapPNode.getCurrentVisualization() != null) {
                exportNoteSymbolsCheckBox.setSelected(true);
            } else {
                exportNoteSymbolsCheckBox.setEnabled(false);
            }
            audioPanel.add(exportNoteSymbolsCheckBox, c);

            generateRhythmPatternsCheckBox.setSelected(true);
            audioPanel.add(generateRhythmPatternsCheckBox, c);

            getContentPane().add(audioPanel, c);
        } else { // change to text SOM!
            JPanel dataPanel = new JPanel(new GridLayout(1, 3));
            dataPanel.setBorder(BorderFactory.createTitledBorder("Data"));
            c.gridy = c.gridy + 1;
            // c.fill = GridBagConstraints.HORIZONTAL;

            dataPanel.add(copyDataCheckBox, c);
            copyDataCheckBox.setSelected(true);

            getContentPane().add(dataPanel, c);
        }

        c.gridy = c.gridy + 1;
        c.anchor = GridBagConstraints.EAST;

        exportButton.addActionListener(this);
        getContentPane().add(exportButton, c);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int unitWidth = ((Integer) spinnerNumberModel.getValue()).intValue();
        int width = unitWidth * layer.getXSize();
        int height = unitWidth * layer.getYSize();
        String baseName = fileNameTextField.getText();
        String imageDir = baseName + "_map/";
        new File(imageDir).mkdir();
        String absoluteDir = new File(FileUtils.getPathFrom(baseName)).getAbsolutePath();

        boolean doCopyData = copyDataCheckBox.isSelected();

        dataDir = streamingURLTextField.getText();
        // data is not copied if data dir is a URL
        // TODO: give a warning or make copyDataCheckBox disabled if URL is entered in streamingURLTextField
        if (FileUtils.isURL(dataDir)) {
            doCopyData = false;
        }

        try {
            ArrayList<String[]> linkInfo = new ArrayList<String[]>();

            if (exportBackgroundCheckBox.isSelected()) {
                linkInfo.add(new String[] { "Image", "Mozart-Silhouette" });

                if (regenerateImagesCheckBox.isSelected()) { // generate image only when check box is selected
                    BufferedImage image = ExportUtils.scaleBackgroundImage(state, width);
                    if (exportGridCheckBox.isSelected()) {
                        VisualisationUtils.drawUnitGrid((Graphics2D) image.getGraphics(), state.growingSOM, width,
                                height, Color.GRAY);
                    }
                    if (exportNoteSymbolsCheckBox.isSelected()) {
                        ExportUtils.drawLinkInfo(state.growingLayer, state.mapPNode, unitWidth,
                                (Graphics2D) image.getGraphics(), CommonSOMViewerStateData.fileNamePrefix);
                    }
                    FileUtils.saveImageToFile(imageDir + linkInfo.get(linkInfo.size() - 1)[0] + ".png", image);
                }
                if (state.inputDataObjects.getClassInfo() != null) {
                    linkInfo.add(new String[] { "Image-ClassInfo", "[+categories]" });
                    if (regenerateImagesCheckBox.isSelected()) { // generate image only when check box is selected
                        BufferedImage background = ExportUtils.scaleBackgroundImage(state, width);
                        if (exportGridCheckBox.isSelected()) {
                            VisualisationUtils.drawUnitGrid((Graphics2D) background.getGraphics(), state.growingSOM,
                                    width, height, Color.GRAY);
                        }
                        ExportUtils.drawClassInfo(state.growingLayer, state.mapPNode, unitWidth,
                                (Graphics2D) background.getGraphics());
                        FileUtils.saveImageToFile(imageDir + linkInfo.get(linkInfo.size() - 1)[0] + ".png", background);

                        linkInfo.add(new String[] { "Image-ClassInfo-only", "[categories]" });
                        // BufferedImage classesOnly = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        if (exportGridCheckBox.isSelected()) {
                            VisualisationUtils.drawUnitGrid((Graphics2D) background.getGraphics(), state.growingSOM,
                                    width, height, Color.GRAY);
                        }
                        ExportUtils.drawClassInfo(state.growingLayer, state.mapPNode, unitWidth,
                                (Graphics2D) background.getGraphics());
                        FileUtils.saveImageToFile(imageDir + linkInfo.get(linkInfo.size() - 1)[0] + ".png", background);
                    }
                }
                linkInfo.add(new String[] { "SPACER", "&nbsp;&nbsp;" });
            }

            if (exportVisCheckBox.isSelected()) {
                linkInfo.add(new String[] { "Visualisation", "Topic Islands" });
                if (regenerateImagesCheckBox.isSelected()) { // generate image only when check box is selected
                    BufferedImage image = ExportUtils.getVisualization(state, unitWidth);
                    if (exportGridCheckBox.isSelected()) {
                        VisualisationUtils.drawUnitGrid((Graphics2D) image.getGraphics(), state.growingSOM, width,
                                height, Color.GRAY);
                    }
                    if (exportNoteSymbolsCheckBox.isSelected()) {
                        ExportUtils.drawLinkInfo(state.growingLayer, state.mapPNode, unitWidth,
                                (Graphics2D) image.getGraphics(), CommonSOMViewerStateData.fileNamePrefix);
                    }
                    FileUtils.saveImageToFile(imageDir + linkInfo.get(linkInfo.size() - 1)[0] + ".png", image);
                }
                if (state.inputDataObjects.getClassInfo() != null) {
                    linkInfo.add(new String[] { "Visualisation-ClassInfo", "[+categories]" });
                    if (regenerateImagesCheckBox.isSelected()) { // generate image only when check box is selected
                        BufferedImage image = ExportUtils.getVisualization(state, unitWidth);
                        if (exportGridCheckBox.isSelected()) {
                            VisualisationUtils.drawUnitGrid((Graphics2D) image.getGraphics(), state.growingSOM, width,
                                    height, Color.GRAY);
                        }
                        ExportUtils.drawClassInfo(state.growingLayer, state.mapPNode, unitWidth,
                                (Graphics2D) image.getGraphics());
                        FileUtils.saveImageToFile(imageDir + linkInfo.get(linkInfo.size() - 1)[0] + ".png", image);
                    }
                }
                linkInfo.add(new String[] { "SPACER", "&nbsp;&nbsp;" });
            } else {
                linkInfo.add(new String[] { "Map", "Plain" });
                if (regenerateImagesCheckBox.isSelected()) { // generate image only when check box is selected
                    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    if (exportGridCheckBox.isSelected()) {
                        VisualisationUtils.drawUnitGrid((Graphics2D) image.getGraphics(), state.growingSOM, width,
                                height, Color.GRAY);
                    }
                    if (exportNoteSymbolsCheckBox.isSelected()) {
                        ExportUtils.drawLinkInfo(state.growingLayer, state.mapPNode, unitWidth,
                                (Graphics2D) image.getGraphics(), CommonSOMViewerStateData.fileNamePrefix);
                    }
                    FileUtils.saveImageToFile(imageDir + linkInfo.get(linkInfo.size() - 1)[0] + ".png", image);
                }
                if (state.inputDataObjects.getClassInfo() != null) {
                    linkInfo.add(new String[] { "Map-ClassInfo", "[+categories]" });
                    if (regenerateImagesCheckBox.isSelected()) { // generate image only when check box is selected
                        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        if (exportGridCheckBox.isSelected()) {
                            VisualisationUtils.drawUnitGrid((Graphics2D) image.getGraphics(), state.growingSOM, width,
                                    height, Color.GRAY);
                        }
                        ExportUtils.drawClassInfo(state.growingLayer, state.mapPNode, unitWidth,
                                (Graphics2D) image.getGraphics());
                        FileUtils.saveImageToFile(imageDir + linkInfo.get(linkInfo.size() - 1)[0] + ".png", image);
                    }
                }
                linkInfo.add(new String[] { "SPACER", "&nbsp;&nbsp;" });
            }

            if (exportHybridImageCheckBox.isSelected()) {
                linkInfo.add(new String[] { "Hybrid", "Silhouette & Islands" });
                if (regenerateImagesCheckBox.isSelected()) { // generate image only when check box is selected
                    BufferedImage image = ExportUtils.scaleBackgroundImage(state, width);

                    // set opacity factor for alpha composition
                    float alpha = 55 / 100f;
                    Graphics2D g = (Graphics2D) image.getGraphics();
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g.drawImage(ExportUtils.getVisualization(state, unitWidth), 0, 0, null);

                    if (exportGridCheckBox.isSelected()) {
                        VisualisationUtils.drawUnitGrid((Graphics2D) image.getGraphics(), state.growingSOM, width,
                                height, Color.GRAY);
                    }
                    ExportUtils.drawLinkInfo(state.growingLayer, state.mapPNode, unitWidth,
                            (Graphics2D) image.getGraphics(), CommonSOMViewerStateData.fileNamePrefix);
                    FileUtils.saveImageToFile(imageDir + linkInfo.get(linkInfo.size() - 1)[0] + ".png", image);
                }
            }
            if (doCopyData) {
                new File(absoluteDir + File.separator + dataDir).mkdirs();
                try {
                    ExportUtils.copyData(CommonSOMViewerStateData.fileNamePrefix, absoluteDir + File.separator
                            + dataDir, "../" + new File(baseName).getName() + "_details/styleUnitDetails.css",
                            state.growingLayer.getAllMappedDataNames());
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if (exportImagemapCheckBox.isSelected()) {
                String[][] visualisations = new String[linkInfo.size()][2];
                for (int i = 0; i < visualisations.length; i++) {
                    visualisations[i] = linkInfo.get(i);
                }

                Properties cleanDataNamesMapping = null;
                if (cleanDataNamesTextField.getText() != null && !cleanDataNamesTextField.getText().trim().equals("")) {
                    try {
                        cleanDataNamesMapping = new Properties();
                        cleanDataNamesMapping.load(new FileInputStream(cleanDataNamesTextField.getText().trim()));
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                String htmlTemplatesDir = null;
                if (templatesTextField.getText() != null && !templatesTextField.getText().trim().equals("")) {
                    htmlTemplatesDir = templatesTextField.getText().trim();
                }

                String imageMapTitle = null;
                if (titleTextField.getText() != null && !titleTextField.getText().trim().equals("")) {
                    imageMapTitle = titleTextField.getText().trim();
                }

                SOMLibClassInformation classInfo = state.inputDataObjects.getClassInfo();
                Color[] classLegendColors = ((SOMViewer) getParent()).getClassLegendColors();

                new ExportUtils().saveImageMap(state.growingLayer, unitWidth, baseName, new File(baseName).getName(),
                        visualisations, isAudioSOM, classInfo, classLegendColors, cleanDataNamesMapping,
                        CommonSOMViewerStateData.fileNamePrefix, dataDir, htmlTemplatesDir, imageMapTitle,
                        generateRhythmPatternsCheckBox.isSelected(), true);
            }
            JOptionPane.showMessageDialog(this, "Export finished!");
        } catch (SOMToolboxException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(getParent(), ex.getMessage(), "Error during export",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
