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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.commons.models.ClassComboBoxModel;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxMain;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;
import at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel;
import at.tuwien.ifs.somtoolbox.models.GrowingCellStructures;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.models.NetworkModel;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.SubClassFinder;
import at.tuwien.ifs.somtoolbox.util.UiUtils;

/**
 * The SOMTrainer is a graphical Interface to train a new SOM. It allows setting various parameters, input and output
 * data and has different SOM-Modes available.
 * 
 * @author Jakob Frank
 * @version $Id: SOMTrainer.java 3877 2010-11-02 15:43:17Z frank $
 * @see AbstractNetworkModel
 */
public class SOMTrainer extends JFrame implements SOMToolboxApp {

    private static final long serialVersionUID = 1L;

    public static final Parameter[] OPTIONS = new Parameter[] {};

    public static final String DESCRIPTION = "Graphical Interface to train a SOM";

    public static final String LONG_DESCRIPTION = "The " + SOMTrainer.class.getSimpleName()
            + " provides a graphical Interface to create a SOM based on different SOM Models.";

    public static final Type APPLICATION_TYPE = Type.Training;

    private JPanel main = null;

    private JPanel pnlMapSettings = null;

    private JLabel lblTitle = null;

    private JTextField txtTitle = null;

    private JLabel lblModel = null;

    private JComboBox cmbModel = null;

    private JPanel pnlInputData = null;

    private JLabel lblVecFile = null;

    private JPanel pnlVec = null;

    private JTextField txtInputVecotrFile = null;

    private JButton btnVecFileOpen = null;

    private JLabel lblTvFile = null;

    private JPanel pnlTv = null;

    private JTextField txtTemplateVecotrFile = null;

    private JButton btnTVFileOpen = null;

    private JLabel lblOutputDir = null;

    private JPanel pnlOutDir = null;

    private JTextField txtOutDir = null;

    private JButton btnOutDirFileSaver = null;

    private JPanel pnlModelSettings = null;

    private JPanel pnlMisc = null;

    private JLabel lblThreads = null;

    private JLabel lblLogFile = null;

    private JSpinner spnThreads = null;

    private JPanel pnlLog = null;

    private JTextField txtLogFile = null;

    private JButton btnLogFileSaver = null;

    private SpinnerNumberModel spnThreadsModel;

    private JProgressBar pgbTraining = null;

    private JPanel pnlGo = null;

    private JButton btnTrain = null;

    private JButton btnSave = null;

    private JCheckBox chkSparse = null;

    private JCheckBox chkNormalized = null;

    private JPanel pnlSettings = null;

    private JLabel lblXSize = null;

    private JSpinner spnXSize = null;

    private JLabel lblYSize = null;

    private JSpinner spnYSize = null;

    private JSpinner spnLearnrate = null;

    private JSpinner spnSigma = null;

    private JSpinner spnIterations = null;

    private JLabel lblIteration = null;

    private JRadioButton rdoIterations = null;

    private JRadioButton rdoCycles = null;

    private JLabel lblMetric = null;

    private JLabel lblQuality = null;

    private JComboBox cmbMetric = null;

    private JComboBox cmbQualityMeasure = null;

    private JLabel jLabel = null;

    private JSpinner spnRandomSeed = null;

    private JCheckBox chkCreateDWM = null;

    private JCheckBox chkSigma = null;

    private JCheckBox chkLernrate = null;

    private SOMModelSettingsPanel pnlModelSpecificSettings = null;

    private ClassComboBoxModel<DistanceMetric> cmbMetricModel;

    private ClassComboBoxModel<QualityMeasure> cmbQualityMeasureModel;

    private ClassComboBoxModel<AbstractNetworkModel> cmbModelModel; // @jve:decl-index=0:visual-constraint="635,54"

    private JCheckBox chkLabelSOM = null;

    private JLabel lblLabels = null;

    private JSpinner spnLabels = null;

    private JSpinner spnWinnerCount = null;

    /**
     * This method initializes
     */
    public SOMTrainer() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("SOM Trainer");
        this.setResizable(false);
        this.setContentPane(getMain());
        this.getRootPane().setDefaultButton(getBtnTrain());
        this.pack();
    }

    /**
     * This method initializes main
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMain() {
        if (main == null) {
            GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
            gridBagConstraints71.gridx = 0;
            gridBagConstraints71.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints71.weightx = 1.0;
            gridBagConstraints71.anchor = GridBagConstraints.EAST;
            gridBagConstraints71.insets = new Insets(2, 2, 2, 2);
            gridBagConstraints71.gridy = 5;
            GridBagConstraints gridBagConstraints63 = new GridBagConstraints();
            gridBagConstraints63.gridx = 0;
            gridBagConstraints63.fill = GridBagConstraints.BOTH;
            gridBagConstraints63.weightx = 1.0;
            gridBagConstraints63.insets = new Insets(4, 2, 4, 2);
            gridBagConstraints63.gridy = 4;
            GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
            gridBagConstraints51.gridx = 0;
            gridBagConstraints51.fill = GridBagConstraints.BOTH;
            gridBagConstraints51.weightx = 1.0;
            gridBagConstraints51.gridy = 3;
            GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
            gridBagConstraints41.gridx = 0;
            gridBagConstraints41.fill = GridBagConstraints.BOTH;
            gridBagConstraints41.weightx = 1.0;
            gridBagConstraints41.gridy = 2;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 0;
            gridBagConstraints11.fill = GridBagConstraints.BOTH;
            gridBagConstraints11.weightx = 1.0;
            gridBagConstraints11.gridy = 0;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridy = 1;
            main = new JPanel();
            main.setLayout(new GridBagLayout());
            main.setName("main");
            main.add(getPnlMapSettings(), gridBagConstraints);
            main.add(getPnlInputData(), gridBagConstraints11);
            main.add(getPnlModelSettings(), gridBagConstraints41);
            main.add(getPnlMisc(), gridBagConstraints51);
            main.add(getPgbTraining(), gridBagConstraints63);
            main.add(getPnlGo(), gridBagConstraints71);
        }
        return main;
    }

    /**
     * This method initializes pnlMapSettings
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlMapSettings() {
        if (pnlMapSettings == null) {
            GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
            gridBagConstraints22.gridx = 0;
            gridBagConstraints22.gridwidth = 2;
            gridBagConstraints22.weightx = 1.0;
            gridBagConstraints22.fill = GridBagConstraints.BOTH;
            gridBagConstraints22.insets = new Insets(2, 0, 2, 0);
            gridBagConstraints22.gridy = 3;
            GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            gridBagConstraints13.gridx = 1;
            gridBagConstraints13.fill = GridBagConstraints.BOTH;
            gridBagConstraints13.weightx = 1.0;
            gridBagConstraints13.insets = new Insets(1, 0, 1, 0);
            gridBagConstraints13.gridy = 1;
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 0;
            gridBagConstraints12.anchor = GridBagConstraints.WEST;
            gridBagConstraints12.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints12.gridy = 1;
            lblOutputDir = new JLabel();
            lblOutputDir.setText("Output Dir *");
            lblOutputDir.setToolTipText("This field is required");
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.anchor = GridBagConstraints.WEST;
            gridBagConstraints4.insets = new Insets(2, 2, 2, 4);
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.fill = GridBagConstraints.BOTH;
            gridBagConstraints3.gridy = 2;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.insets = new Insets(1, 0, 1, 0);
            gridBagConstraints3.gridx = 1;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.anchor = GridBagConstraints.WEST;
            gridBagConstraints2.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints2.gridy = 2;
            lblModel = new JLabel();
            lblModel.setText("SOM Model");
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = GridBagConstraints.BOTH;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.insets = new Insets(1, 0, 1, 0);
            gridBagConstraints1.gridx = 1;
            lblTitle = new JLabel();
            lblTitle.setText("Title");
            pnlMapSettings = new JPanel();
            pnlMapSettings.setLayout(new GridBagLayout());
            pnlMapSettings.setBorder(BorderFactory.createTitledBorder(null, "Map Settings",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                    new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
            pnlMapSettings.add(lblTitle, gridBagConstraints4);
            pnlMapSettings.add(getTxtTitle(), gridBagConstraints1);
            pnlMapSettings.add(lblModel, gridBagConstraints2);
            pnlMapSettings.add(getCmbModel(), gridBagConstraints3);
            pnlMapSettings.add(lblOutputDir, gridBagConstraints12);
            pnlMapSettings.add(getPnlOutDir(), gridBagConstraints13);
            pnlMapSettings.add(getPnlSettings(), gridBagConstraints22);
            pnlMapSettings.addPropertyChangeListener("enabled", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    setEnabledToChildren(pnlMapSettings, evt.getNewValue().equals(Boolean.TRUE), true);
                }
            });

        }
        return pnlMapSettings;
    }

    /**
     * This method initializes txtTitle
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtTitle() {
        if (txtTitle == null) {
            txtTitle = new JTextField();
            txtTitle.setColumns(10);
        }
        return txtTitle;
    }

    /**
     * This method initializes cmbModel
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getCmbModel() {
        if (cmbModel == null) {
            cmbModel = new JComboBox();
            cmbModel.setModel(getCmbModelModel());

            cmbModel.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.out.println("Update \"Model Specific Settings\" Panel");

                    pnlModelSpecificSettings = SOMModelSettingsPanel.createModelSpecificConfigPanel(getCmbModelModel().getSelectedClass());

                    if (pnlModelSpecificSettings == null) {
                        getPnlModelSettings().removeAll();
                        getPnlModelSettings().setVisible(false);
                    } else {
                        GridBagConstraints c = new GridBagConstraints();
                        c.fill = GridBagConstraints.BOTH;
                        c.weightx = 1;
                        c.weighty = 1;
                        getPnlModelSettings().removeAll();
                        getPnlModelSettings().add(pnlModelSpecificSettings, c);
                        getPnlModelSettings().setVisible(true);
                    }
                    SOMTrainer.this.validate();
                    SOMTrainer.this.pack();
                    System.out.printf("Done%n");
                }
            });

            // TODO: hard coded initial selection
            cmbModel.setSelectedItem(GrowingSOM.class.getSimpleName());

        }
        return cmbModel;
    }

    /**
     * @return the Model of {@link NetworkModel}s
     */
    private ClassComboBoxModel<AbstractNetworkModel> getCmbModelModel() {
        if (cmbModelModel == null) {
            ArrayList<Class<? extends AbstractNetworkModel>> models = SubClassFinder.findSubclassesOf(AbstractNetworkModel.class);
            ArrayList<Class<? extends AbstractNetworkModel>> toRemove = new ArrayList<Class<? extends AbstractNetworkModel>>();
            for (Class<? extends AbstractNetworkModel> class1 : models) {
                if (class1.isInterface() || Modifier.isAbstract(class1.getModifiers())) {
                    toRemove.add(class1);
                }
            }
            // TODO: hard coded exclusion
            toRemove.add(GrowingCellStructures.class);
            models.removeAll(toRemove);
            cmbModelModel = new ClassComboBoxModel<AbstractNetworkModel>(models);
        }
        return cmbModelModel;
    }

    /**
     * This method initializes pnlInputData
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlInputData() {
        if (pnlInputData == null) {
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 1;
            gridBagConstraints21.anchor = GridBagConstraints.WEST;
            gridBagConstraints21.gridy = 1;
            GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
            gridBagConstraints20.gridx = 0;
            gridBagConstraints20.gridy = 1;
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.anchor = GridBagConstraints.WEST;
            gridBagConstraints9.insets = new Insets(2, 2, 2, 4);
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.gridx = 1;
            gridBagConstraints8.fill = GridBagConstraints.BOTH;
            gridBagConstraints8.weightx = 1.0;
            gridBagConstraints8.gridy = 2;
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 0;
            gridBagConstraints7.anchor = GridBagConstraints.WEST;
            gridBagConstraints7.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints7.gridy = 2;
            lblTvFile = new JLabel();
            lblTvFile.setText("Template Vector File");
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 1;
            gridBagConstraints5.fill = GridBagConstraints.BOTH;
            gridBagConstraints5.weightx = 1.0;
            gridBagConstraints5.gridy = 0;
            lblVecFile = new JLabel();
            lblVecFile.setText("Input Vector File *");
            lblVecFile.setToolTipText("This field is required");
            pnlInputData = new JPanel();
            pnlInputData.setLayout(new GridBagLayout());
            pnlInputData.setBorder(BorderFactory.createTitledBorder(null, "Input Data",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                    new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
            pnlInputData.add(lblVecFile, gridBagConstraints9);
            pnlInputData.add(getPnlVec(), gridBagConstraints5);
            pnlInputData.add(lblTvFile, gridBagConstraints7);
            pnlInputData.add(getPnlTv(), gridBagConstraints8);
            pnlInputData.add(getChkSparse(), gridBagConstraints20);
            pnlInputData.add(getChkNormalized(), gridBagConstraints21);
            pnlInputData.addPropertyChangeListener("enabled", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    setEnabledToChildren(pnlInputData, evt.getNewValue().equals(Boolean.TRUE), true);
                }
            });
        }
        return pnlInputData;
    }

    /**
     * This method initializes pnlVec
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlVec() {
        if (pnlVec == null) {
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.insets = new Insets(0, 0, 1, 0);
            gridBagConstraints10.fill = GridBagConstraints.BOTH;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.fill = GridBagConstraints.BOTH;
            gridBagConstraints6.insets = new Insets(0, 0, 0, 0);
            gridBagConstraints6.weightx = 1.0;
            pnlVec = new JPanel();
            pnlVec.setLayout(new GridBagLayout());
            pnlVec.add(getTxtInputVecotrFile(), gridBagConstraints6);
            pnlVec.add(getBtnVecFileOpen(), gridBagConstraints10);
        }
        return pnlVec;
    }

    /**
     * This method initializes txtInputVecotrFile
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtInputVecotrFile() {
        if (txtInputVecotrFile == null) {
            txtInputVecotrFile = new JTextField();
            txtInputVecotrFile.setColumns(10);
        }
        return txtInputVecotrFile;
    }

    /**
     * This method initializes btnVecFileOpen
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnVecFileOpen() {
        if (btnVecFileOpen == null) {
            btnVecFileOpen = new JButton();
            btnVecFileOpen.setText("...");
            btnVecFileOpen.setMargin(new Insets(0, 0, 0, 0));
            btnVecFileOpen.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    File f = execFileChooser(txtInputVecotrFile, new FileFilter() {

                        @Override
                        public String getDescription() {
                            return "SOMLib Vector Files";
                        }

                        @Override
                        public boolean accept(File f) {
                            if (f.isDirectory() || f.getName().endsWith(".vec") || f.getName().endsWith(".vec.gz")) {
                                return true;
                            }
                            return false;
                        }
                    }, false, false);
                    if (f != null) {
                        if (f.getName().contains(".norm.")) {
                            chkNormalized.setSelected(true);
                        }
                    }
                }
            });
        }
        return btnVecFileOpen;
    }

    /**
     * This method initializes pnlTv
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlTv() {
        if (pnlTv == null) {
            GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
            gridBagConstraints30.insets = new Insets(0, 0, 1, 0);
            GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
            gridBagConstraints61.fill = GridBagConstraints.BOTH;
            gridBagConstraints61.weightx = 1.0;
            pnlTv = new JPanel();
            pnlTv.setLayout(new GridBagLayout());
            pnlTv.add(getTxtTemplateVecotrFile(), gridBagConstraints61);
            pnlTv.add(getBtnTVFileOpen(), gridBagConstraints30);
        }
        return pnlTv;
    }

    /**
     * This method initializes txtTemplateVecotrFile
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtTemplateVecotrFile() {
        if (txtTemplateVecotrFile == null) {
            txtTemplateVecotrFile = new JTextField();
            txtTemplateVecotrFile.setColumns(10);
        }
        return txtTemplateVecotrFile;
    }

    /**
     * This method initializes btnTVFileOpen
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnTVFileOpen() {
        if (btnTVFileOpen == null) {
            btnTVFileOpen = new JButton();
            btnTVFileOpen.setText("...");
            btnTVFileOpen.setMargin(new Insets(0, 0, 0, 0));

            btnTVFileOpen.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    execFileChooser(txtTemplateVecotrFile, new FileFilter() {

                        @Override
                        public String getDescription() {
                            return "TemplateVector Files";
                        }

                        @Override
                        public boolean accept(File f) {
                            if (f.isDirectory() || f.getName().endsWith(".tv") || f.getName().endsWith(".tv.gz")) {
                                return true;
                            }
                            return false;
                        }
                    }, false, false);

                }
            });
        }
        return btnTVFileOpen;
    }

    /**
     * This method initializes pnlOutDir
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlOutDir() {
        if (pnlOutDir == null) {
            GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
            gridBagConstraints31.insets = new Insets(0, 0, 1, 0);
            GridBagConstraints gridBagConstraints611 = new GridBagConstraints();
            gridBagConstraints611.fill = GridBagConstraints.BOTH;
            gridBagConstraints611.weightx = 1.0;
            pnlOutDir = new JPanel();
            pnlOutDir.setLayout(new GridBagLayout());
            pnlOutDir.add(getTxtOutDir(), gridBagConstraints611);
            pnlOutDir.add(getBtnOutDirFileSaver(), gridBagConstraints31);
        }
        return pnlOutDir;
    }

    /**
     * This method initializes txtOutDir
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtOutDir() {
        if (txtOutDir == null) {
            txtOutDir = new JTextField();
            txtOutDir.setColumns(10);
            txtOutDir.setToolTipText("This field is required");
        }
        return txtOutDir;
    }

    /**
     * This method initializes btnOutDirFileSaver
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnOutDirFileSaver() {
        if (btnOutDirFileSaver == null) {
            btnOutDirFileSaver = new JButton();
            btnOutDirFileSaver.setMargin(new Insets(0, 0, 0, 0));
            btnOutDirFileSaver.setText("...");
            btnOutDirFileSaver.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    execFileChooser(txtOutDir, null, true, true);
                }
            });
        }
        return btnOutDirFileSaver;
    }

    /**
     * This method initializes pnlModelSettings
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlModelSettings() {
        if (pnlModelSettings == null) {
            pnlModelSettings = new JPanel();
            pnlModelSettings.setLayout(new GridBagLayout());
            pnlModelSettings.setBorder(BorderFactory.createTitledBorder(null, "Model Specific Settings",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                    new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
            pnlModelSettings.setVisible(false);
            pnlModelSettings.addPropertyChangeListener("enabled", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    setEnabledToChildren(pnlModelSettings, evt.getNewValue().equals(Boolean.TRUE), true);
                }
            });

        }
        return pnlModelSettings;
    }

    /**
     * This method initializes pnlMisc
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlMisc() {
        if (pnlMisc == null) {
            GridBagConstraints gridBagConstraints48 = new GridBagConstraints();
            gridBagConstraints48.gridx = 3;
            gridBagConstraints48.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints48.weightx = 1.0;
            gridBagConstraints48.gridy = 2;
            GridBagConstraints gridBagConstraints44 = new GridBagConstraints();
            gridBagConstraints44.gridx = 0;
            gridBagConstraints44.gridwidth = GridBagConstraints.RELATIVE;
            gridBagConstraints44.anchor = GridBagConstraints.EAST;
            gridBagConstraints44.gridy = 2;
            GridBagConstraints gridBagConstraints43 = new GridBagConstraints();
            gridBagConstraints43.gridx = 3;
            gridBagConstraints43.insets = new Insets(2, 0, 2, 0);
            gridBagConstraints43.weightx = 0.8;
            gridBagConstraints43.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints43.gridy = 0;
            GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
            gridBagConstraints42.gridx = 2;
            gridBagConstraints42.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints42.gridy = 0;
            jLabel = new JLabel();
            jLabel.setText("Random seed");
            GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
            gridBagConstraints17.gridx = 1;
            gridBagConstraints17.fill = GridBagConstraints.BOTH;
            gridBagConstraints17.weightx = 1.0;
            gridBagConstraints17.gridwidth = 0;
            gridBagConstraints17.gridy = 1;
            GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
            gridBagConstraints16.fill = GridBagConstraints.BOTH;
            gridBagConstraints16.gridy = 0;
            gridBagConstraints16.weightx = 1.2;
            gridBagConstraints16.insets = new Insets(2, 0, 2, 0);
            gridBagConstraints16.gridx = 1;
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            gridBagConstraints15.gridx = 0;
            gridBagConstraints15.anchor = GridBagConstraints.WEST;
            gridBagConstraints15.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints15.gridy = 1;
            lblLogFile = new JLabel();
            lblLogFile.setText("Logfile");
            GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
            gridBagConstraints14.gridx = 0;
            gridBagConstraints14.anchor = GridBagConstraints.WEST;
            gridBagConstraints14.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints14.gridy = 0;
            lblThreads = new JLabel();
            lblThreads.setText("Training Threads");
            pnlMisc = new JPanel();
            pnlMisc.setLayout(new GridBagLayout());
            pnlMisc.setBorder(BorderFactory.createTitledBorder(null, "Misc Settings",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                    new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
            pnlMisc.add(lblThreads, gridBagConstraints14);
            pnlMisc.add(lblLogFile, gridBagConstraints15);
            pnlMisc.add(getSpnThreads(), gridBagConstraints16);
            pnlMisc.add(getPnlLog(), gridBagConstraints17);
            pnlMisc.add(jLabel, gridBagConstraints42);
            pnlMisc.add(getSpnRandomSeed(), gridBagConstraints43);
            pnlMisc.add(getChkCreateDWM(), gridBagConstraints44);
            pnlMisc.add(getSpnWinnerCount(), gridBagConstraints48);
            pnlMisc.addPropertyChangeListener("enabled", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    setEnabledToChildren(pnlMisc, evt.getNewValue().equals(Boolean.TRUE), true);
                }
            });

        }
        return pnlMisc;
    }

    /**
     * This method initializes spnThreads
     * 
     * @return javax.swing.JSpinner
     */
    private JSpinner getSpnThreads() {
        if (spnThreads == null) {
            spnThreads = new JSpinner();
            Runtime r = Runtime.getRuntime();
            spnThreadsModel = new SpinnerNumberModel(r.availableProcessors(), 1, r.availableProcessors() * 2, 1);
            spnThreads.setModel(spnThreadsModel);
        }
        return spnThreads;
    }

    /**
     * This method initializes pnlLog
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlLog() {
        if (pnlLog == null) {
            GridBagConstraints gridBagConstraints101 = new GridBagConstraints();
            gridBagConstraints101.insets = new Insets(0, 0, 1, 0);
            GridBagConstraints gridBagConstraints62 = new GridBagConstraints();
            gridBagConstraints62.fill = GridBagConstraints.BOTH;
            gridBagConstraints62.insets = new Insets(1, 0, 1, 0);
            gridBagConstraints62.weightx = 1.0;
            pnlLog = new JPanel();
            pnlLog.setLayout(new GridBagLayout());
            pnlLog.add(getTxtLogFile(), gridBagConstraints62);
            pnlLog.add(getBtnLogFileSaver(), gridBagConstraints101);
        }
        return pnlLog;
    }

    /**
     * This method initializes txtLogFile
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtLogFile() {
        if (txtLogFile == null) {
            txtLogFile = new JTextField();
            txtLogFile.setColumns(10);
        }
        return txtLogFile;
    }

    /**
     * This method initializes btnLogFileSaver
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnLogFileSaver() {
        if (btnLogFileSaver == null) {
            btnLogFileSaver = new JButton();
            btnLogFileSaver.setMargin(new Insets(0, 0, 0, 0));
            btnLogFileSaver.setText("...");
            btnLogFileSaver.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    execFileChooser(txtInputVecotrFile, new FileFilter() {

                        @Override
                        public String getDescription() {
                            return "Logfile";
                        }

                        @Override
                        public boolean accept(File f) {
                            if (f.isDirectory() || f.getName().endsWith(".log")) {
                                return true;
                            }
                            return false;
                        }
                    }, true, false);
                }
            });
        }
        return btnLogFileSaver;
    }

    /**
     * This method initializes pgbTraining
     * 
     * @return javax.swing.JProgressBar
     */
    private JProgressBar getPgbTraining() {
        if (pgbTraining == null) {
            pgbTraining = new JProgressBar();
            pgbTraining.setStringPainted(true);
            pgbTraining.setString("");
        }
        return pgbTraining;
    }

    /**
     * This method initializes pnlGo
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlGo() {
        if (pnlGo == null) {
            GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
            gridBagConstraints19.gridx = 1;
            gridBagConstraints19.insets = new Insets(0, 2, 0, 0);
            gridBagConstraints19.anchor = GridBagConstraints.EAST;
            GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
            gridBagConstraints18.gridx = 0;
            gridBagConstraints18.anchor = GridBagConstraints.EAST;
            gridBagConstraints18.gridy = 0;
            pnlGo = new JPanel();
            pnlGo.setLayout(new GridBagLayout());
            pnlGo.add(getBtnTrain(), gridBagConstraints19);
            pnlGo.add(getBtnSave(), gridBagConstraints18);
        }
        return pnlGo;
    }

    /**
     * This method initializes btnTrain
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnTrain() {
        if (btnTrain == null) {
            btnTrain = new JButton();
            btnTrain.setText("Start Training");
            btnTrain.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    startTraining();
                }
            });
        }
        return btnTrain;
    }

    /**
     * This method initializes btnSave
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnSave() {
        if (btnSave == null) {
            btnSave = new JButton();
            btnSave.setText("Save Settings...");
            btnSave.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        File outDir = new File(getTxtOutDir().getText());
                        String basename = getTxtTitle().getText().length() > 0 ? getTxtTitle().getText() : "som";
                        File pFile = new File(outDir, basename + ".prop");
                        JFileChooser fc = new JFileChooser(outDir);
                        fc.setSelectedFile(pFile);
                        if (fc.showSaveDialog(SOMTrainer.this) == JFileChooser.APPROVE_OPTION) {
                            FileWriter fw = new FileWriter(fc.getSelectedFile());
                            String comment = fc.getSelectedFile().getName() + " prop file";
                            createSOMProps().store(fw, StringUtils.wrap(comment, 80, "#   "));
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
        return btnSave;
    }

    /**
     * This method initializes chkSparse
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkSparse() {
        if (chkSparse == null) {
            chkSparse = new JCheckBox();
            chkSparse.setText("sparse data");
        }
        return chkSparse;
    }

    /**
     * This method initializes chkNormalized
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkNormalized() {
        if (chkNormalized == null) {
            chkNormalized = new JCheckBox();
            chkNormalized.setText("normalized");
        }
        return chkNormalized;
    }

    /**
     * This method initializes pnlSettings
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPnlSettings() {
        if (pnlSettings == null) {
            GridBagConstraints gridBagConstraints47 = new GridBagConstraints();
            gridBagConstraints47.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints47.gridy = 6;
            gridBagConstraints47.weightx = 1.0;
            gridBagConstraints47.insets = new Insets(2, 0, 2, 0);
            gridBagConstraints47.gridx = 1;
            GridBagConstraints gridBagConstraints46 = new GridBagConstraints();
            gridBagConstraints46.gridx = 2;
            gridBagConstraints46.anchor = GridBagConstraints.WEST;
            gridBagConstraints46.insets = new Insets(0, 2, 0, 0);
            gridBagConstraints46.gridy = 6;
            lblLabels = new JLabel();
            lblLabels.setText("Labels");
            GridBagConstraints gridBagConstraints45 = new GridBagConstraints();
            gridBagConstraints45.gridx = 0;
            gridBagConstraints45.gridy = 6;
            GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
            gridBagConstraints28.gridx = 0;
            gridBagConstraints28.anchor = GridBagConstraints.EAST;
            gridBagConstraints28.gridy = 1;
            GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
            gridBagConstraints23.gridx = 2;
            gridBagConstraints23.anchor = GridBagConstraints.EAST;
            gridBagConstraints23.gridy = 1;
            GridBagConstraints gridBagConstraints40 = new GridBagConstraints();
            gridBagConstraints40.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints40.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints40.anchor = GridBagConstraints.EAST;
            GridBagConstraints gridBagConstraints39 = new GridBagConstraints();
            gridBagConstraints39.fill = GridBagConstraints.BOTH;
            gridBagConstraints39.gridy = 5;
            gridBagConstraints39.weightx = 1.0;
            gridBagConstraints39.gridwidth = 3;
            gridBagConstraints39.insets = new Insets(1, 0, 1, 0);
            gridBagConstraints39.gridx = 1;
            GridBagConstraints gridBagConstraints38 = new GridBagConstraints();
            gridBagConstraints38.fill = GridBagConstraints.BOTH;
            gridBagConstraints38.gridy = 4;
            gridBagConstraints38.weightx = 1.0;
            gridBagConstraints38.gridwidth = 3;
            gridBagConstraints38.insets = new Insets(1, 0, 1, 0);
            gridBagConstraints38.gridx = 1;
            GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
            gridBagConstraints37.gridx = 0;
            gridBagConstraints37.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints37.anchor = GridBagConstraints.EAST;
            gridBagConstraints37.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints37.gridy = 5;
            lblQuality = new JLabel();
            lblQuality.setText("Quality Measure");
            GridBagConstraints gridBagConstraints36 = new GridBagConstraints();
            gridBagConstraints36.gridx = 0;
            gridBagConstraints36.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints36.anchor = GridBagConstraints.EAST;
            gridBagConstraints36.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints36.gridy = 4;
            lblMetric = new JLabel();
            lblMetric.setText("Metric");
            GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
            gridBagConstraints35.gridx = 3;
            gridBagConstraints35.anchor = GridBagConstraints.WEST;
            gridBagConstraints35.weightx = 0.25;
            gridBagConstraints35.gridy = 3;
            GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
            gridBagConstraints34.gridx = 2;
            gridBagConstraints34.anchor = GridBagConstraints.WEST;
            gridBagConstraints34.weightx = 0.25;
            gridBagConstraints34.gridy = 3;
            GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
            gridBagConstraints33.gridx = 0;
            gridBagConstraints33.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints33.anchor = GridBagConstraints.EAST;
            gridBagConstraints33.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints33.gridy = 3;
            lblIteration = new JLabel();
            lblIteration.setText("Train");
            GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
            gridBagConstraints32.gridx = 1;
            gridBagConstraints32.weightx = 0.5;
            gridBagConstraints32.anchor = GridBagConstraints.WEST;
            gridBagConstraints32.insets = new Insets(2, 0, 2, 0);
            gridBagConstraints32.gridy = 3;
            GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
            gridBagConstraints29.gridx = 3;
            gridBagConstraints29.anchor = GridBagConstraints.WEST;
            gridBagConstraints29.weightx = 0.5;
            gridBagConstraints29.insets = new Insets(2, 0, 2, 0);
            gridBagConstraints29.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints29.gridy = 1;
            GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
            gridBagConstraints27.gridx = 1;
            gridBagConstraints27.fill = GridBagConstraints.BOTH;
            gridBagConstraints27.weightx = 0.5;
            gridBagConstraints27.anchor = GridBagConstraints.WEST;
            gridBagConstraints27.insets = new Insets(2, 0, 2, 0);
            gridBagConstraints27.gridy = 1;
            GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
            gridBagConstraints26.gridx = 3;
            gridBagConstraints26.weightx = 0.5;
            gridBagConstraints26.anchor = GridBagConstraints.WEST;
            gridBagConstraints26.insets = new Insets(2, 0, 2, 0);
            gridBagConstraints26.gridy = 0;
            GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
            gridBagConstraints25.gridx = 2;
            gridBagConstraints25.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints25.anchor = GridBagConstraints.EAST;
            gridBagConstraints25.insets = new Insets(2, 2, 2, 4);
            gridBagConstraints25.gridy = 0;
            lblYSize = new JLabel();
            lblYSize.setText("YSize");
            GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
            gridBagConstraints24.gridx = 1;
            gridBagConstraints24.weightx = 0.5;
            gridBagConstraints24.anchor = GridBagConstraints.WEST;
            gridBagConstraints24.insets = new Insets(2, 0, 2, 0);
            gridBagConstraints24.gridy = 0;
            lblXSize = new JLabel();
            lblXSize.setText("XSize");
            pnlSettings = new JPanel();
            pnlSettings.setLayout(new GridBagLayout());
            pnlSettings.add(lblXSize, gridBagConstraints40);
            pnlSettings.add(getSpnXSize(), gridBagConstraints24);
            pnlSettings.add(lblYSize, gridBagConstraints25);
            pnlSettings.add(getSpnYSize(), gridBagConstraints26);
            pnlSettings.add(getSpnLearnrate(), gridBagConstraints27);
            pnlSettings.add(getSpnSigma(), gridBagConstraints29);
            pnlSettings.add(getSpnIterations(), gridBagConstraints32);
            pnlSettings.add(lblIteration, gridBagConstraints33);
            pnlSettings.add(getRdoIterations(), gridBagConstraints34);
            pnlSettings.add(getRdoCycles(), gridBagConstraints35);
            pnlSettings.add(lblMetric, gridBagConstraints36);
            pnlSettings.add(getCmbMetric(), gridBagConstraints38);
            pnlSettings.add(lblQuality, gridBagConstraints37);
            pnlSettings.add(getCmbQualityMeasure(), gridBagConstraints39);
            pnlSettings.add(getChkSigma(), gridBagConstraints23);
            pnlSettings.add(getChkLernrate(), gridBagConstraints28);
            pnlSettings.add(getChkLabelSOM(), gridBagConstraints45);
            pnlSettings.add(lblLabels, gridBagConstraints46);
            pnlSettings.add(getSpnLabels(), gridBagConstraints47);
            ButtonGroup it = new ButtonGroup();
            it.add(getRdoCycles());
            it.add(getRdoIterations());

            lblQuality.setVisible(false);
            getCmbQualityMeasure().setVisible(false);
        }
        return pnlSettings;
    }

    /**
     * This method initializes spnXSize
     * 
     * @return javax.swing.JSpinner
     */
    private JSpinner getSpnXSize() {
        if (spnXSize == null) {
            spnXSize = new JSpinner();
            spnXSize.setModel(new SpinnerNumberModel(20, 1, Integer.MAX_VALUE, 1));
        }
        return spnXSize;
    }

    /**
     * This method initializes spnYSize
     * 
     * @return javax.swing.JSpinner
     */
    private JSpinner getSpnYSize() {
        if (spnYSize == null) {
            spnYSize = new JSpinner();
            spnYSize.setModel(new SpinnerNumberModel(14, 1, Integer.MAX_VALUE, 1));
        }
        return spnYSize;
    }

    /**
     * This method initializes spnLearnrate
     * 
     * @return javax.swing.JSpinner
     */
    private JSpinner getSpnLearnrate() {
        if (spnLearnrate == null) {
            spnLearnrate = new JSpinner() {
                private static final long serialVersionUID = 1L;

                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled && getChkLernrate().isSelected());
                }
            };
            spnLearnrate.setModel(new SpinnerNumberModel(0.7, 0, 1, 0.001));
            spnLearnrate.setEnabled(getChkLernrate().isSelected());
        }
        return spnLearnrate;
    }

    /**
     * This method initializes spnSigma
     * 
     * @return javax.swing.JSpinner
     */
    private JSpinner getSpnSigma() {
        if (spnSigma == null) {
            spnSigma = new JSpinner() {
                private static final long serialVersionUID = 1L;

                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled && getChkSigma().isSelected());
                }
            };
            spnSigma.setModel(new SpinnerNumberModel(1.0, 1.0, 16.0, .1));
            spnSigma.setEnabled(getChkSigma().isSelected());
        }
        return spnSigma;
    }

    /**
     * This method initializes spnIterations
     * 
     * @return javax.swing.JSpinner
     */
    private JSpinner getSpnIterations() {
        if (spnIterations == null) {
            spnIterations = new JSpinner();
            spnIterations.setModel(new SpinnerNumberModel(1500, 1, Integer.MAX_VALUE, 1));
        }
        return spnIterations;
    }

    /**
     * This method initializes rdoIterations
     * 
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getRdoIterations() {
        if (rdoIterations == null) {
            rdoIterations = new JRadioButton();
            rdoIterations.setText("Iterations");
            rdoIterations.setSelected(true);
        }
        return rdoIterations;
    }

    /**
     * This method initializes rdoCycles
     * 
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getRdoCycles() {
        if (rdoCycles == null) {
            rdoCycles = new JRadioButton();
            rdoCycles.setText("Cycles");
        }
        return rdoCycles;
    }

    /**
     * This method initializes cmbMetric
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getCmbMetric() {
        if (cmbMetric == null) {
            cmbMetric = new JComboBox();
            cmbMetric.setModel(getCmbMetricModel());

        }
        return cmbMetric;
    }

    /**
     * @return the MetricModel
     */
    private ClassComboBoxModel<DistanceMetric> getCmbMetricModel() {
        if (cmbMetricModel == null) {
            ArrayList<Class<? extends DistanceMetric>> distances = SubClassFinder.findSubclassesOf(DistanceMetric.class);
            ArrayList<Class<? extends DistanceMetric>> toRemove = new ArrayList<Class<? extends DistanceMetric>>();
            for (Class<? extends DistanceMetric> class1 : distances) {
                if (class1.isInterface() || Modifier.isAbstract(class1.getModifiers())) {
                    toRemove.add(class1);
                }
            }
            distances.removeAll(toRemove);
            cmbMetricModel = new ClassComboBoxModel<DistanceMetric>(distances);
        }
        return cmbMetricModel;
    }

    /**
     * This method initializes cmbQualityMeasure
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getCmbQualityMeasure() {
        if (cmbQualityMeasure == null) {
            cmbQualityMeasure = new JComboBox();
            cmbQualityMeasure.setModel(getCmbQualityMeasureModel());

        }
        return cmbQualityMeasure;
    }

    /**
     * @return the {@link QualityMeasure} Model
     */
    private ClassComboBoxModel<QualityMeasure> getCmbQualityMeasureModel() {
        if (cmbQualityMeasureModel == null) {
            ArrayList<Class<? extends QualityMeasure>> qualities = SubClassFinder.findSubclassesOf(QualityMeasure.class);
            ArrayList<Class<? extends QualityMeasure>> toRemove = new ArrayList<Class<? extends QualityMeasure>>();
            for (Class<? extends QualityMeasure> class1 : qualities) {
                if (class1.isInterface() || Modifier.isAbstract(class1.getModifiers())) {
                    toRemove.add(class1);
                }
            }
            qualities.removeAll(toRemove);
            cmbQualityMeasureModel = new ClassComboBoxModel<QualityMeasure>(qualities);
        }
        return cmbQualityMeasureModel;
    }

    private File execFileChooser(JTextField target, FileFilter filter, boolean isToSave, boolean directorySelect) {
        File cwd = new File(target.getText());
        JFileChooser c = new JFileChooser(cwd);
        if (filter != null) {
            c.addChoosableFileFilter(filter);
            c.setFileFilter(filter);
        }
        if (directorySelect) {
            c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }

        int returnVal;
        if (isToSave) {
            returnVal = c.showSaveDialog(this);
        } else {
            returnVal = c.showOpenDialog(this);
        }

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = c.getSelectedFile();
            target.setText(file.getAbsolutePath());
            return file;
        }
        return null;
    }

    private Properties createSOMProps() {
        Properties p = null;
        if (pnlModelSpecificSettings != null) {
            p = pnlModelSpecificSettings.getProperties();
        } else {
            p = new Properties();
        }

        p.setProperty("outputDirectory", getTxtOutDir().getText());
        p.setProperty("workingDirectory", getTxtOutDir().getText());
        p.setProperty("namePrefix", getTxtTitle().getText());
        p.setProperty("vectorFileName", getTxtInputVecotrFile().getText());
        p.setProperty("sparseData", getChkSparse().isSelected() ? "yes" : "no");
        p.setProperty("isNormalized", getChkNormalized().isSelected() ? "yes" : "no");
        if (getTxtTemplateVecotrFile().getText().length() > 0) {
            p.setProperty("templateFileName", getTxtTemplateVecotrFile().getText());
        }

        p.setProperty("xSize", getSpnXSize().getValue().toString());
        p.setProperty("ySize", getSpnYSize().getValue().toString());
        if (getChkLernrate().isSelected()) {
            p.setProperty("learnrate", getSpnLearnrate().getValue().toString());
        }
        if (getChkSigma().isSelected()) {
            p.setProperty("sigma", getSpnSigma().getValue().toString());
        }

        p.setProperty("randomSeed", getSpnRandomSeed().getValue().toString());

        ClassComboBoxModel<DistanceMetric> mm = getCmbMetricModel();
        if (mm.getSelectedClass() != null) {
            p.setProperty("metricName", mm.getSelectedClass().getName());
        }

        ClassComboBoxModel<QualityMeasure> qm = getCmbQualityMeasureModel();
        if (qm.getSelectedClass() != null) {
            p.setProperty("growthQualityMeasureName", qm.getSelectedClass().getName());
        }

        if (rdoIterations.isSelected()) {
            p.setProperty("numIterations", getSpnIterations().getValue().toString());
        } else {
            p.setProperty("numCycles", getSpnIterations().getValue().toString());
        }

        return p;
    }

    private String[] createCmdLine(File propFile) {
        ArrayList<String> l = new ArrayList<String>();

        l.add(getCmbModelModel().getSelectedClass().getSimpleName());

        if (pnlModelSpecificSettings != null) {
            for (String string : pnlModelSpecificSettings.getAdditionalParams()) {
                l.add(string);
            }
        }

        if (getChkLabelSOM().isSelected()) {
            l.add("-l");
            l.add("LabelSOM");
            l.add("-n");
            l.add(getSpnLabels().getValue().toString());
        }

        if (getChkCreateDWM().isSelected()) {
            l.add("--numberWinners");
            l.add(getSpnWinnerCount().getValue().toString());
        } else {
            l.add("--skipDWM");
        }

        l.add("--cpus");
        l.add(getSpnThreads().getValue().toString());

        l.add(propFile.getAbsolutePath());

        return l.toArray(new String[l.size()]);
    }

    private void setGuiLocked(boolean locked) {
        getPnlInputData().setEnabled(!locked);
        getPnlMapSettings().setEnabled(!locked);
        getPnlModelSettings().setEnabled(!locked);
        getPnlMisc().setEnabled(!locked);
    }

    private void startTraining() {
        // FIXME: provide some more generic way for checking this
        if (getTxtOutDir().getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "You need to specify the output directory!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (getTxtTitle().getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "You need to specify the title!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setGuiLocked(true);
        try {
            File outDir = new File(getTxtOutDir().getText());
            String basename = getTxtTitle().getText();
            if (!outDir.exists()) {
                outDir.mkdirs();
            }

            // Save Props
            Properties prop = createSOMProps();
            File pFile = new File(outDir, basename + ".prop");

            String[] cmdLine = createCmdLine(pFile);

            final String header = basename + " prop file\n# somtoolbox " + Arrays.deepToString(cmdLine);
            prop.store(new FileWriter(pFile), StringUtils.wrap(header, 80, "#   "));

            System.out.printf("%s%n", Arrays.deepToString(cmdLine));
            prop.store(System.out, null);

            SOMToolboxMain.main(cmdLine);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setGuiLocked(false);
    }

    /**
     * This method initializes spnRandomSeed
     * 
     * @return javax.swing.JSpinner
     */
    private JSpinner getSpnRandomSeed() {
        if (spnRandomSeed == null) {
            spnRandomSeed = new JSpinner();
            spnRandomSeed.setModel(new SpinnerNumberModel(7, 1, Integer.MAX_VALUE, 1));
        }
        return spnRandomSeed;
    }

    /**
     * This method initializes chkCreateDWM
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkCreateDWM() {
        if (chkCreateDWM == null) {
            chkCreateDWM = new JCheckBox();
            chkCreateDWM.setText("create DataWinnerMapping file");
            chkCreateDWM.setSelected(true);
            chkCreateDWM.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    getSpnWinnerCount().setEnabled(chkCreateDWM.isSelected());
                }
            });
        }
        return chkCreateDWM;
    }

    /**
     * This method initializes chkSigma
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkSigma() {
        if (chkSigma == null) {
            chkSigma = new JCheckBox();
            chkSigma.setText("sigma");
            chkSigma.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    getSpnSigma().setEnabled(chkSigma.isSelected());
                }
            });
            chkSigma.setSelected(false);
        }
        return chkSigma;
    }

    /**
     * This method initializes chkLernrate
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkLernrate() {
        if (chkLernrate == null) {
            chkLernrate = new JCheckBox();
            chkLernrate.setText("lernrate");
            chkLernrate.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    getSpnLearnrate().setEnabled(chkLernrate.isSelected());
                }
            });
            chkLernrate.setSelected(true);
        }
        return chkLernrate;
    }

    private static void setEnabledToChildren(JPanel panel, boolean enabled, boolean recursive) {
        Component[] children = panel.getComponents();
        for (Component child : children) {
            child.setEnabled(enabled);
            if (recursive && child instanceof JPanel) {
                setEnabledToChildren((JPanel) child, enabled, recursive);

            }
        }
    }

    /**
     * This method initializes chkLabelSOM
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkLabelSOM() {
        if (chkLabelSOM == null) {
            chkLabelSOM = new JCheckBox();
            chkLabelSOM.setText("label SOM");
            chkLabelSOM.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getSpnLabels().setEnabled(chkLabelSOM.isSelected());
                }
            });
        }
        return chkLabelSOM;
    }

    /**
     * This method initializes spnLabels
     * 
     * @return javax.swing.JTextField
     */
    private JSpinner getSpnLabels() {
        if (spnLabels == null) {
            spnLabels = new JSpinner() {
                private static final long serialVersionUID = 1L;

                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled && getChkLabelSOM().isSelected());
                    if (lblLabels != null) {
                        lblLabels.setEnabled(enabled && getChkLabelSOM().isSelected());
                    }
                }

            };
            spnLabels.setEnabled(getChkLabelSOM().isSelected());
        }
        return spnLabels;
    }

    /**
     * This method initializes spnWinnerCount
     * 
     * @return javax.swing.JSpinner
     */
    private JSpinner getSpnWinnerCount() {
        if (spnWinnerCount == null) {
            spnWinnerCount = new JSpinner() {
                private static final long serialVersionUID = 1L;

                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled && getChkCreateDWM().isSelected());
                }
            };
            spnWinnerCount.getModel().setValue(300);
        }
        return spnWinnerCount;
    }

    public static void main(String[] args) {
        UiUtils.setSOMToolboxLookAndFeel();
        new SOMTrainer().setVisible(true);
    }
} // @jve:decl-index=0:visual-constraint="10,10"
