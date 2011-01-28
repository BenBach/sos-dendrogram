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
package at.tuwien.ifs.somtoolbox.apps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.PaletteDisplayer;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.visualization.ColorGradient;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;

/**
 * This class allows you to quickly view, edit, create and export palettes. It can be used embedded into the SOMViewer
 * or started Standalone.
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: PaletteEditor.java 3877 2010-11-02 15:43:17Z frank $
 */
public class PaletteEditor extends JFrame {
    private static final long serialVersionUID = 1L;

    private JPanel northPanel = null;

    private JPanel centerPanel = null;

    private JPanel mainPanel = null;

    private JPanel southPanel = null;

    private JButton btnClose = null;

    private JButton btnSaveJava = null;

    private JButton btnSaveMatLab = null;

    private JButton btnSaveXML = null;

    private JButton btnNew = null;

    private JPanel selectPanel = null;

    private PaletteDisplayer paletteDrawingPanel = null;

    private JCheckBox chkInterpol = null;

    private JSpinner spnTargetColorCount = null;

    private JLabel lblColors = null;

    private JScrollPane scpColors = null;

    private JPanel colorPanel = null;

    private JCheckBox chkUseGradientPoints = null;

    private SOMViewer somViewer;

    private CommonSOMViewerStateData state;

    private Palette displayedPalette = new Palette();

    private final int mode;

    private static final int STANDALONE = 0, SOMVIEWER = 1;

    private JButton btnApply = null;

    private JPanel pnlSeperator = null;

    private JPanel pnlAddRemoveColors = null;

    private JToggleButton btnAddColor = null;

    private JToggleButton btnDelColor = null;

    private JLabel lblWhatToDo = null;

    private JPanel pnlSettings = null;

    private JLabel jLabel = null;

    private JTextField txtShortname = null;

    private JLabel jLabel1 = null;

    private JTextField txtLongname = null;

    private JLabel jLabel2 = null;

    private JScrollPane scpDescription = null;

    private JTextArea txtDescription = null;

    private JComboBox cmbPalettes = null;

    private JTextField xmlFile = null;

    private JPanel pnlColors = null;

    private JLabel jLabel3 = null;

    private JTextField txtGroup = null;

    private JLabel jLabel4 = null;

    private JLabel jLabel5 = null;

    private JCheckBox chkVisible = null;

    /**
     * Creates a new {@link PaletteEditor} in the Standalone-mode.
     */
    public PaletteEditor() {
        mode = STANDALONE;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initialize();

        setPalette(new Palette());
    }

    /**
     * Creates a new {@link PaletteEditor} in the Embedded-mode, used together with the {@link SOMViewer}.
     * 
     * @param somViewer the {@link SOMViewer} to wich the changes sould be applied.
     * @param state the {@link CommonSOMViewerStateData} holding various data, including the palettes to edit.
     */
    public PaletteEditor(SOMViewer somViewer, CommonSOMViewerStateData state) {
        mode = SOMVIEWER;
        this.somViewer = somViewer;
        this.state = state;

        initialize();

        setPalette(somViewer.getCurrentlySelectedPalette());
        cmbPalettes.setSelectedItem(somViewer.getCurrentlySelectedPalette());
    }

    private JPanel getStandalonePaletteSelector() {
        final JPanel pnl = new JPanel();
        pnl.setLayout(new GridBagLayout());

        xmlFile = new JTextField();
        xmlFile.setColumns(30);

        JButton dlg = new JButton("...");
        dlg.addActionListener(new ActionListener() {
            private JFileChooser jfc = new JFileChooser() {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".xml");
                }
            };

            @Override
            public void actionPerformed(ActionEvent e) {
                if (jfc.showOpenDialog(pnl) == JFileChooser.APPROVE_OPTION) {
                    xmlFile.setText(jfc.getSelectedFile().getAbsolutePath());
                }
            }
        });

        JButton load = new JButton("load");
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = new File(xmlFile.getText());
                if (f.exists()) {
                    try {
                        setPalette(Palette.loadPaletteFromXML(f));
                    } catch (SOMToolboxException e1) {
                        JOptionPane.showMessageDialog(PaletteEditor.this, f + " is not a valid XMLPalette!");
                        e1.printStackTrace();
                    }
                }
            }
        });

        GridBagConstraints cfile = new GridBagConstraints();
        cfile.fill = GridBagConstraints.VERTICAL;
        cfile.weightx = 1.0;

        GridBagConstraints cload = new GridBagConstraints();
        cload.insets = new Insets(0, 5, 0, 0);

        pnl.add(xmlFile, cfile);
        pnl.add(dlg);
        pnl.add(load, cload);

        return pnl;
    }

    private JPanel getEmbeddedPaletteSelector() {
        final JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout());

        cmbPalettes = new JComboBox(Palettes.getAvailablePalettes());
        if (somViewer != null) {
            cmbPalettes.setSelectedItem(somViewer.getCurrentlySelectedPalette());
        } else {
            cmbPalettes.setSelectedItem(Palettes.getDefaultPalette());
        }

        cmbPalettes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPalette(Palettes.getPaletteByName(cmbPalettes.getSelectedItem().toString()));

                /* Old version */
                // String t = cmbPalettes.getSelectedItem().toString();
                // for (Palette p : state.palettes) {
                // if (p.toString().equals(t)) {
                // setPalette(p);
                // }
                // }
            }
        });

        pnl.add(cmbPalettes, BorderLayout.CENTER);
        return pnl;
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setTitle("PaletteEditor");
        this.setContentPane(getMainPanel());
        this.pack();
    }

    /**
     * This method initializes northPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getNorthPanel() {
        if (northPanel == null) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new Insets(2, 5, 2, 2);
            northPanel = new JPanel();
            northPanel.setLayout(new GridBagLayout());
            northPanel.add(getBtnNew(), new GridBagConstraints());
            northPanel.add(getSelectPanel(), gridBagConstraints);
        }
        return northPanel;
    }

    /**
     * This method initializes centerPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getCenterPanel() {
        if (centerPanel == null) {
            GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
            gridBagConstraints26.gridx = 0;
            gridBagConstraints26.fill = GridBagConstraints.BOTH;
            gridBagConstraints26.weightx = 1.0;
            gridBagConstraints26.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints26.gridy = 3;
            GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
            gridBagConstraints18.gridx = 1;
            gridBagConstraints18.gridwidth = 0;
            gridBagConstraints18.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints18.gridy = 0;
            GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
            gridBagConstraints17.gridx = 1;
            gridBagConstraints17.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints17.gridwidth = 0;
            gridBagConstraints17.gridy = 2;
            lblWhatToDo = new JLabel();
            lblWhatToDo.setText(" ");
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 3;
            gridBagConstraints11.anchor = GridBagConstraints.EAST;
            gridBagConstraints11.gridy = 5;
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.gridx = 2;
            gridBagConstraints9.anchor = GridBagConstraints.WEST;
            gridBagConstraints9.gridy = 5;
            lblColors = new JLabel();
            lblColors.setText("Colors");
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.gridx = 1;
            gridBagConstraints8.gridy = 5;
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 0;
            gridBagConstraints7.gridy = 5;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.fill = GridBagConstraints.BOTH;
            gridBagConstraints1.weighty = 1.0;
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.gridy = 1;
            gridBagConstraints1.insets = new Insets(10, 5, 10, 5);
            centerPanel = new JPanel();
            centerPanel.setLayout(new GridBagLayout());
            centerPanel.add(getPaletteDrawingPanel(), gridBagConstraints1);
            centerPanel.add(getChkInterpol(), gridBagConstraints7);
            centerPanel.add(getSpnTargetColorCount(), gridBagConstraints8);
            centerPanel.add(lblColors, gridBagConstraints9);
            centerPanel.add(getChkUseGradientPoints(), gridBagConstraints11);
            centerPanel.add(lblWhatToDo, gridBagConstraints17);
            centerPanel.add(getPnlSettings(), gridBagConstraints18);
            centerPanel.add(getPnlColors(), gridBagConstraints26);
        }
        return centerPanel;
    }

    /**
     * This method initializes mainPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(getNorthPanel(), BorderLayout.NORTH);
            mainPanel.add(getCenterPanel(), BorderLayout.CENTER);
            mainPanel.add(getSouthPanel(), BorderLayout.SOUTH);
        }
        return mainPanel;
    }

    /**
     * This method initializes southPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getSouthPanel() {
        if (southPanel == null) {
            GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            gridBagConstraints13.gridx = 1;
            gridBagConstraints13.weightx = 1.0;
            gridBagConstraints13.gridy = 1;
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 2;
            gridBagConstraints12.gridy = 1;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 1;
            gridBagConstraints6.gridy = 0;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridy = 1;
            gridBagConstraints5.gridx = 5;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridy = 1;
            gridBagConstraints4.gridx = 4;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridy = 1;
            gridBagConstraints3.gridx = 3;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridy = 1;
            southPanel = new JPanel();
            southPanel.setLayout(new GridBagLayout());
            southPanel.add(getBtnClose(), gridBagConstraints2);
            southPanel.add(getBtnSaveJava(), gridBagConstraints3);
            southPanel.add(getBtnSaveMatLab(), gridBagConstraints4);
            southPanel.add(getBtnSaveXML(), gridBagConstraints5);
            southPanel.add(getBtnApply(), gridBagConstraints12);
            southPanel.add(getPnlSeperator(), gridBagConstraints13);
        }
        return southPanel;
    }

    /**
     * This method initializes btnClose
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton();
            btnClose.setText("Close");
            btnClose.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    System.out.println("Palette: " + getPaletteDrawingPanel().getSize().width + "x"
                            + getPaletteDrawingPanel().getSize().height);
                    System.out.println("Colors: " + getColorPanel().getSize().width + "x"
                            + getColorPanel().getSize().height);

                    PaletteEditor.this.setVisible(false);
                    PaletteEditor.this.dispose();
                    if (mode == STANDALONE) {
                        System.exit(0);
                    }

                }
            });
        }
        return btnClose;
    }

    /**
     * This method initializes btnSaveJava
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnSaveJava() {
        if (btnSaveJava == null) {
            btnSaveJava = new JButton();
            btnSaveJava.setVisible(false);
            btnSaveJava.setText("Export Javacode");
            btnSaveJava.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser jfc = null;
                    if (state != null) {
                        jfc = state.getFileChooser();
                    }
                    if (jfc == null) {
                        jfc = new JFileChooser();
                    }
                    if (jfc.showSaveDialog(PaletteEditor.this) != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    Palette p = getPalette();
                    p.savePaletteAsJavaCode(jfc.getSelectedFile());
                    setPalette(p);
                }
            });
        }
        return btnSaveJava;
    }

    /**
     * This method initializes btnSaveMatLab
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnSaveMatLab() {
        if (btnSaveMatLab == null) {
            btnSaveMatLab = new JButton();
            btnSaveMatLab.setText("Export MatLab");
            btnSaveMatLab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser jfc = null;
                    if (state != null) {
                        jfc = state.getFileChooser();
                    }
                    if (jfc == null) {
                        jfc = new JFileChooser();
                    }
                    if (jfc.showSaveDialog(PaletteEditor.this) != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    Palette p = getPalette();
                    p.savePaletteToMatlab(jfc.getSelectedFile());
                    setPalette(p);
                }
            });
        }
        return btnSaveMatLab;
    }

    /**
     * This method initializes btnSaveXML
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnSaveXML() {
        if (btnSaveXML == null) {
            btnSaveXML = new JButton();
            btnSaveXML.setText("Export XML");
            btnSaveXML.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser jfc = null;
                    if (state != null) {
                        jfc = state.getFileChooser();
                    }
                    if (jfc == null) {
                        jfc = new JFileChooser();
                    }
                    if (jfc.showSaveDialog(PaletteEditor.this) != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    Palette p = getPalette();
                    p.savePaletteToXML(jfc.getSelectedFile());
                    setPalette(p);
                }
            });
        }
        return btnSaveXML;
    }

    /**
     * This method initializes btnNew
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnNew() {
        if (btnNew == null) {
            btnNew = new JButton();
            btnNew.setText("New");
            btnNew.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Palette p = new Palette();
                    if (mode == SOMVIEWER) {
                        Palettes.addPalette(p);
                        if (state != null) {
                            cmbPalettes.setModel(new JComboBox(Palettes.getAvailablePalettes()).getModel());
                            cmbPalettes.setSelectedItem(p);
                        }
                    } else {
                        xmlFile.setText("");
                    }
                    setPalette(p);
                }
            });
        }
        return btnNew;
    }

    /**
     * This method initializes selectPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getSelectPanel() {
        if (selectPanel == null) {
            selectPanel = new JPanel();
            selectPanel.setLayout(new BorderLayout());
            switch (mode) {
                case STANDALONE:
                    selectPanel.add(getStandalonePaletteSelector(), BorderLayout.CENTER);
                    break;
                case SOMVIEWER:
                    selectPanel.add(getEmbeddedPaletteSelector(), BorderLayout.CENTER);
                    break;
                default:
            }
        }
        return selectPanel;
    }

    /**
     * This method initializes paletteDrawingPanel
     * 
     * @return at.tuwien.ifs.somtoolbox.apps.viewer.controls.PaletteDrawingPanel
     */
    private PaletteDisplayer getPaletteDrawingPanel() {
        if (paletteDrawingPanel == null) {
            paletteDrawingPanel = new PaletteDisplayer();
        }
        return paletteDrawingPanel;
    }

    /**
     * This method initializes chkInterpol
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkInterpol() {
        // Not used at the moment
        if (chkInterpol == null) {
            chkInterpol = new JCheckBox();
            chkInterpol.setText("interpolate to");
            chkInterpol.setVisible(false);
        }
        return chkInterpol;
    }

    /**
     * This method initializes spnTargetColorCount
     * 
     * @return javax.swing.JSpinner
     */
    private JSpinner getSpnTargetColorCount() {
        if (spnTargetColorCount == null) {
            spnTargetColorCount = new JSpinner();
            // We just support up to 2^16 colors.
            spnTargetColorCount.setModel(new SpinnerNumberModel(0, 0, 65536, 1));
            spnTargetColorCount.getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    Palette p = getPalette();
                    p.setNumberOfGradientColours(((Integer) spnTargetColorCount.getModel().getValue()).intValue());
                    setPalette(p);
                }
            });
        }
        return spnTargetColorCount;
    }

    /**
     * This method initializes scpColors
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getScpColors() {
        if (scpColors == null) {
            scpColors = new JScrollPane();
            scpColors.setMinimumSize(new Dimension(420, 75));
            scpColors.setViewportView(getColorPanel());
        }
        return scpColors;
    }

    /**
     * This method initializes colorPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getColorPanel() {
        if (colorPanel == null) {
            colorPanel = new JPanel();
            colorPanel.setMinimumSize(new Dimension(420, 75));
            colorPanel.setPreferredSize(new Dimension(420, 75));
            colorPanel.setLayout(new GridBagLayout());
        }
        return colorPanel;
    }

    /**
     * This method initializes chkUseGradientPoints
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkUseGradientPoints() {
        if (chkUseGradientPoints == null) {
            chkUseGradientPoints = new JCheckBox();
            chkUseGradientPoints.setText("use gradient points");
            chkUseGradientPoints.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (chkUseGradientPoints.isSelected()) { // Convert from nonGradient to Gradient
                        Palette g = getPalette();
                        int colors = g.getColors().length;
                        double[] points = new double[colors];
                        double step = 1d / (colors - 1);
                        for (int i = 0; i < points.length; i++) {
                            points[i] = i * step;
                        }
                        try {
                            ColorGradient cg = new ColorGradient(points, g.getColors());
                            setPalette(new Palette(g.getName(), g.getShortName(), g.getDescription(), cg, 256));
                        } catch (SOMToolboxException e1) {
                            // FIXME: MUST NOT HAPPEN!
                            e1.printStackTrace();
                        }
                    } else { // Convert from Gradient to nonGradient
                        Palette g = getPalette();
                        ColorGradient cg = g.getGradient();
                        if (cg == null) {
                            return;
                        }
                        Color[] cs = new Color[cg.getNumberOfPoints()];
                        for (int i = 0; i < cs.length; i++) {
                            cs[i] = cg.getGradientColor(i);
                        }
                        setPalette(new Palette(g.getName(), g.getShortName(), g.getDescription(), cs));
                    }
                }

            });
        }
        return chkUseGradientPoints;
    }

    /**
     * This method initializes btnApply
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnApply() {
        if (btnApply == null) {
            btnApply = new JButton();
            btnApply.setText("Apply");
            if (mode == STANDALONE) {
                btnApply.setVisible(false);
            }
            btnApply.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (mode == SOMVIEWER) {
                        if (cmbPalettes != null && state != null) {
                            if (somViewer != null) {
                                somViewer.updatePaletteAfterEditing();
                                somViewer.rebuildPaletteMenu();
                            }
                        }
                    }
                }
            });
        }
        return btnApply;
    }

    /**
     * This method initializes pnlSeperator
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlSeperator() {
        if (pnlSeperator == null) {
            pnlSeperator = new JPanel();
            pnlSeperator.setLayout(new GridBagLayout());
        }
        return pnlSeperator;
    }

    /**
     * This method initializes pnlAddRemoveColors
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlAddRemoveColors() {
        if (pnlAddRemoveColors == null) {
            GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
            gridBagConstraints16.insets = new Insets(2, 2, 2, 2);
            gridBagConstraints16.fill = GridBagConstraints.HORIZONTAL;
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            gridBagConstraints15.gridx = 0;
            gridBagConstraints15.insets = new Insets(2, 2, 2, 2);
            gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints15.gridy = 1;
            pnlAddRemoveColors = new JPanel();
            pnlAddRemoveColors.setLayout(new GridBagLayout());
            pnlAddRemoveColors.add(getBtnAddColor(), gridBagConstraints16);
            pnlAddRemoveColors.add(getBtnDelColor(), gridBagConstraints15);
        }
        return pnlAddRemoveColors;
    }

    /**
     * This method initializes btnAddColor
     * 
     * @return javax.swing.JButton
     */
    private JToggleButton getBtnAddColor() {
        if (btnAddColor == null) {
            btnAddColor = new JToggleButton();
            btnAddColor.setText("add color");
            btnAddColor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (btnAddColor.isSelected()) {
                        btnDelColor.setSelected(false);
                        lblWhatToDo.setText("Select the Color AFTER which you want to insert a new Color");
                    } else {
                        lblWhatToDo.setText(" ");
                    }
                }
            });
        }
        return btnAddColor;
    }

    /**
     * This method initializes btnDelColor
     * 
     * @return javax.swing.JButton
     */
    private JToggleButton getBtnDelColor() {
        if (btnDelColor == null) {
            btnDelColor = new JToggleButton();
            btnDelColor.setText("remove color");
            btnDelColor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (btnDelColor.isSelected()) {
                        btnAddColor.setSelected(false);
                        lblWhatToDo.setText("Select the Color you want to delete");
                    } else {
                        lblWhatToDo.setText(" ");
                    }
                }
            });
        }
        return btnDelColor;
    }

    /**
     * This method initializes pnlSettings
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlSettings() {
        if (pnlSettings == null) {
            GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
            gridBagConstraints29.gridx = 1;
            gridBagConstraints29.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints29.gridwidth = 2;
            gridBagConstraints29.insets = new Insets(0, 0, 0, 0);
            gridBagConstraints29.gridy = 3;
            GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
            gridBagConstraints28.gridx = 0;
            gridBagConstraints28.anchor = GridBagConstraints.EAST;
            gridBagConstraints28.gridy = 3;
            jLabel5 = new JLabel();
            jLabel5.setText("Visibility:");
            GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
            gridBagConstraints27.gridx = 2;
            gridBagConstraints27.gridy = 2;
            jLabel4 = new JLabel();
            jLabel4.setText("(optional)");
            GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
            gridBagConstraints14.fill = GridBagConstraints.BOTH;
            gridBagConstraints14.gridy = 2;
            gridBagConstraints14.weightx = 1.0;
            gridBagConstraints14.insets = new Insets(2, 2, 2, 2);
            gridBagConstraints14.gridx = 1;
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.gridx = 0;
            gridBagConstraints10.anchor = GridBagConstraints.EAST;
            gridBagConstraints10.gridy = 2;
            jLabel3 = new JLabel();
            jLabel3.setText("Group:");
            GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
            gridBagConstraints25.fill = GridBagConstraints.BOTH;
            gridBagConstraints25.gridy = 4;
            gridBagConstraints25.weightx = 1.0;
            gridBagConstraints25.weighty = 1.0;
            gridBagConstraints25.insets = new Insets(2, 2, 2, 2);
            gridBagConstraints25.gridx = 1;
            gridBagConstraints25.gridwidth = 2;
            GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
            gridBagConstraints23.fill = GridBagConstraints.BOTH;
            gridBagConstraints23.gridy = 2;
            gridBagConstraints23.weightx = 1.0;
            gridBagConstraints23.weighty = 1.0;
            gridBagConstraints23.gridx = 1;
            GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
            gridBagConstraints24.anchor = GridBagConstraints.EAST;
            GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
            gridBagConstraints22.gridx = 0;
            gridBagConstraints22.anchor = GridBagConstraints.NORTHEAST;
            gridBagConstraints22.insets = new Insets(2, 0, 0, 0);
            gridBagConstraints22.gridy = 4;
            jLabel2 = new JLabel();
            jLabel2.setText("Description:");
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.fill = GridBagConstraints.BOTH;
            gridBagConstraints21.gridy = 1;
            gridBagConstraints21.weightx = 1.0;
            gridBagConstraints21.insets = new Insets(2, 2, 2, 2);
            gridBagConstraints21.gridx = 1;
            gridBagConstraints21.gridwidth = 2;
            GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
            gridBagConstraints20.gridx = 0;
            gridBagConstraints20.anchor = GridBagConstraints.EAST;
            gridBagConstraints20.gridy = 1;
            jLabel1 = new JLabel();
            jLabel1.setText("Longname:");
            GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
            gridBagConstraints19.fill = GridBagConstraints.BOTH;
            gridBagConstraints19.gridy = 0;
            gridBagConstraints19.weightx = 1.0;
            gridBagConstraints19.insets = new Insets(2, 2, 2, 2);
            gridBagConstraints19.gridx = 1;
            gridBagConstraints19.gridwidth = 2;
            jLabel = new JLabel();
            jLabel.setText("Shortname:");
            pnlSettings = new JPanel();
            pnlSettings.setLayout(new GridBagLayout());
            pnlSettings.add(jLabel, gridBagConstraints24);
            pnlSettings.add(getTxtShortname(), gridBagConstraints19);
            pnlSettings.add(jLabel1, gridBagConstraints20);
            pnlSettings.add(getTxtLongname(), gridBagConstraints21);
            pnlSettings.add(jLabel2, gridBagConstraints22);
            pnlSettings.add(getScpDescription(), gridBagConstraints25);
            pnlSettings.add(jLabel3, gridBagConstraints10);
            pnlSettings.add(getTxtGroup(), gridBagConstraints14);
            pnlSettings.add(jLabel4, gridBagConstraints27);
            pnlSettings.add(jLabel5, gridBagConstraints28);
            pnlSettings.add(getChkVisible(), gridBagConstraints29);
        }
        return pnlSettings;
    }

    /**
     * This method initializes txtShortname
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtShortname() {
        if (txtShortname == null) {
            txtShortname = new JTextField();
            txtShortname.setText("testast");
        }
        return txtShortname;
    }

    /**
     * This method initializes txtLongname
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtLongname() {
        if (txtLongname == null) {
            txtLongname = new JTextField();
        }
        return txtLongname;
    }

    /**
     * This method initializes scpDescription
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getScpDescription() {
        if (scpDescription == null) {
            scpDescription = new JScrollPane();
            scpDescription.setViewportView(getTxtDescription());
        }
        return scpDescription;
    }

    /**
     * This method initializes txtDescription
     * 
     * @return javax.swing.JTextArea
     */
    private JTextArea getTxtDescription() {
        if (txtDescription == null) {
            txtDescription = new JTextArea();
            txtDescription.setRows(3);
            txtDescription.setLineWrap(true);
            txtDescription.setWrapStyleWord(true);
        }
        return txtDescription;
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlColors() {
        if (pnlColors == null) {
            pnlColors = new JPanel();
            pnlColors.setLayout(new BorderLayout());
            pnlColors.add(getScpColors(), BorderLayout.CENTER);
            pnlColors.add(getPnlAddRemoveColors(), BorderLayout.EAST);
        }
        return pnlColors;
    }

    /**
     * This method initializes txtGroup
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtGroup() {
        if (txtGroup == null) {
            txtGroup = new JTextField();
        }
        return txtGroup;
    }

    /**
     * This method initializes chkVisible
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkVisible() {
        if (chkVisible == null) {
            chkVisible = new JCheckBox();
            chkVisible.setText("show palette in the palette menu");
            chkVisible.setSelected(true);
        }
        return chkVisible;
    }

    /**
     * Starts the {@link PaletteEditor} in Standalone-Mode.
     * 
     * @param args <c>args[0]</c> can be a xml-Palette-file {@link Palette#savePaletteToXML(File)} which will be loaded
     *            at startup.
     */
    public static void main(String[] args) {
        PaletteEditor ade = new PaletteEditor();
        if (args.length != 0) {
            System.out.println(args[0]);
            File f = new File(args[0]);
            if (f.exists()) {
                try {
                    Palette p = Palette.loadPaletteFromXML(f);
                    ade.xmlFile.setText(f.getAbsolutePath());
                    ade.setPalette(p);
                } catch (SOMToolboxException e) {
                    e.printStackTrace();
                }
            }
        }
        ade.setVisible(true);
    }

    private JButton createColorButton(final int i, Color c) {
        final JButton button = new JButton();
        button.setText("");
        button.setBackground(c);
        button.setBorder(BorderFactory.createMatteBorder(7, 25, 8, 25, c));
        button.setMinimumSize(new Dimension(60, 30));
        button.setPreferredSize(new Dimension(60, 30));

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnAddColor.isSelected()) {
                    Palette p = getPalette();
                    p.insertColor(i, Color.WHITE);
                    setPalette(p);
                } else if (btnDelColor.isSelected()) {
                    Palette p = getPalette();
                    p.deleteColor(i);
                    setPalette(p);
                } else {
                    final JColorChooser jcc = new JColorChooser();
                    jcc.setColor(button.getBackground());
                    JDialog jd = JColorChooser.createDialog(PaletteEditor.this, "Choose a color", true, jcc,
                            new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    button.setBackground(jcc.getColor());
                                    button.setBorder(BorderFactory.createMatteBorder(7, 25, 8, 25, jcc.getColor()));
                                    Palette p = PaletteEditor.this.getPalette();
                                    ColorGradient cg = p.getGradient();
                                    if (cg != null) {
                                        cg.setGradientPoint(i, -1, jcc.getColor());
                                        p.setGradient(cg);
                                    } else {
                                        p.setColor(i, jcc.getColor());
                                    }
                                    PaletteEditor.this.setPalette(p);
                                }
                            }, null);
                    jd.setVisible(true);
                }
                btnAddColor.setSelected(false);
                btnDelColor.setSelected(false);
                lblWhatToDo.setText(" ");
                // System.out.println(button.getSize());
            }
        });
        return button;
    }

    private JSpinner createGradientSpinner(final int i, double value, double lBound, double uBound) {
        final JSpinner spinner = new JSpinner();
        spinner.setMinimumSize(new Dimension(60, 20));
        spinner.setPreferredSize(new Dimension(60, 20));
        // spinner.setModel(new SpinnerNumberModel(value, 0,1,0.001));
        spinner.setModel(new SpinnerNumberModel(value, lBound, uBound, 0.001));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.000"));
        spinner.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // System.out.println(spinner.getSize());
                Palette p = PaletteEditor.this.getPalette();
                ColorGradient cg = p.getGradient();
                cg.setGradientPoint(i, ((Double) spinner.getModel().getValue()).doubleValue(), null);
                p.setGradient(cg);
                PaletteEditor.this.setPalette(p);
            }
        });
        return spinner;
    }

    private Palette getPalette() {
        displayedPalette.setShortName(txtShortname.getText().replaceAll("\\s+", ""));
        displayedPalette.setName(txtLongname.getText());
        displayedPalette.setPaletteGroup(getTxtGroup().getText().replaceAll("\\s+", ""));
        displayedPalette.setHidden(!chkVisible.isSelected());
        displayedPalette.setDescription(txtDescription.getText().replaceAll("[\\r\\n]", " ").replaceAll("\\s+", " "));
        return displayedPalette;
    }

    /**
     * Set the palette to edit.
     * 
     * @param p The Palette.
     */
    private void setPalette(Palette p) {
        if (p == null) {
            this.setTitle("PaletteEditor");
            displayedPalette = null;

            txtShortname.setText("");
            txtLongname.setText("");
            txtGroup.setText("");
            chkVisible.setSelected(true);
            txtDescription.setText("");
        } else {
            this.setTitle("Editing " + p.getName());
            displayedPalette = p;

            txtShortname.setText(p.getShortName());
            txtLongname.setText(p.getName());
            txtGroup.setText(p.getPaletteGroup());
            chkVisible.setSelected(!p.isHidden());
            txtDescription.setText(p.getDescription());
        }

        // Build the colors-panel
        colorPanel.removeAll();
        GridBagConstraints buttonConstr = new GridBagConstraints();
        buttonConstr.gridx = 0;
        buttonConstr.gridy = 0;
        // buttonConstr.fill = GridBagConstraints.BOTH;
        buttonConstr.insets = new Insets(5, 7, 5, 7);
        GridBagConstraints spinnerConstr = new GridBagConstraints();
        spinnerConstr.gridx = 0;
        spinnerConstr.gridy = 1;
        spinnerConstr.insets = new Insets(0, 7, 5, 7);
        spinnerConstr.fill = GridBagConstraints.HORIZONTAL;

        if (p != null) {
            if (p.getGradient() != null) { // gradient palette
                chkUseGradientPoints.setSelected(true);

                spnTargetColorCount.getModel().setValue(new Integer(p.getNumberOfGradientColours()));

                ColorGradient cg = p.getGradient();
                for (int i = 0; i < cg.getNumberOfPoints(); i++) {

                    buttonConstr.gridx = i;
                    spinnerConstr.gridx = i;
                    colorPanel.add(createColorButton(i, cg.getGradientColor(i)), buttonConstr);
                    if (i > 0 && i < cg.getNumberOfPoints() - 1) {

                        colorPanel.add(
                                createGradientSpinner(i, cg.getGradientPoint(i), cg.getGradientPoint(i - 1),
                                        cg.getGradientPoint(i + 1)), spinnerConstr);
                    } else {
                        JLabel l = new JLabel("" + StringUtils.format(cg.getGradientPoint(i), 3, true));
                        l.setHorizontalTextPosition(SwingConstants.CENTER);
                        l.setHorizontalAlignment(SwingConstants.CENTER);
                        colorPanel.add(l, spinnerConstr);
                    }
                }
            } else { // non-gradient palette
                chkUseGradientPoints.setSelected(false);

                Color[] cls = p.getColors();
                for (int i = 0; i < cls.length; i++) {
                    buttonConstr.gridx = i;
                    spinnerConstr.gridx = i;
                    colorPanel.add(createColorButton(i, cls[i]), buttonConstr);
                }
            }
        } else {
            // TODO: Deactivate some controls?
        }
        spnTargetColorCount.setVisible(chkUseGradientPoints.isSelected());
        lblColors.setVisible(chkUseGradientPoints.isSelected());

        paletteDrawingPanel.setPalette(p);

        colorPanel.revalidate();
        colorPanel.repaint();

    }

} // @jve:decl-index=0:visual-constraint="16,15"
