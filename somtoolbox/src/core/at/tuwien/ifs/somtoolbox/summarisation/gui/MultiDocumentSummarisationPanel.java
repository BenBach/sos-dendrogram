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
package at.tuwien.ifs.somtoolbox.summarisation.gui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cern.colt.list.DoubleArrayList;

import at.tuwien.ifs.somtoolbox.summarisation.output.DocumentDisplayer;
import at.tuwien.ifs.somtoolbox.summarisation.output.ResultHandler;
import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;

/**
 * @author Julius Penaranda
 * @version $Id: MultiDocumentSummarisationPanel.java 3883 2010-11-02 17:13:23Z frank $
 */
public class MultiDocumentSummarisationPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    public static final String SINGLE_DOCUMENT = "Single Document";

    public static final String SIM_METHOD = "Sim-Method";

    public static final String MULTIDOC_SUM = "Multidoc-Sum";

    public static final String MULTI_SDS = "Multi SDS";

    private NavigationPanel navP = null;

    private String mdstype = null;

    private Scorer scorer = null;

    private ResultHandler resulth = null;

    private DocumentDisplayer documentdispl = null;

    private int compressionValue = 50;

    private double similarityValue = 50;

    private String algorithm = null;

    private boolean clickedOnce = false;

    private boolean sumClicked = false;

    private JPanel panelSim = new JPanel(new GridBagLayout());

    private JPanel panelScore = new JPanel(new GridLayout(2, 2));

    public MultiDocumentSummarisationPanel(NavigationPanel navPanel, Scorer sc, ResultHandler rh,
            DocumentDisplayer docdis) {
        setLayout(new GridBagLayout());
        this.navP = navPanel;
        this.scorer = sc;
        this.resulth = rh;
        this.documentdispl = docdis;

        setBorder(BorderFactory.createEtchedBorder());
        JLabel MDSLabel = new JLabel("   MDS Methods");
        MDSLabel.setForeground(Color.blue);

        JRadioButton method_1 = new JRadioButton(MULTI_SDS);
        method_1.addActionListener(this);

        JRadioButton method_2 = new JRadioButton(MULTIDOC_SUM);
        method_2.addActionListener(this);

        JRadioButton method_3 = new JRadioButton(SIM_METHOD);
        method_3.addActionListener(this);

        JRadioButton sds_method = new JRadioButton(SINGLE_DOCUMENT);
        sds_method.addActionListener(this);

        ButtonGroup group = new ButtonGroup();
        group.add(method_1);
        group.add(method_2);
        group.add(method_3);
        group.add(sds_method);

        JSpinner similaritySpinner = new JSpinner(new SpinnerNumberModel(similarityValue, 0, 100, 1));
        similaritySpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner source = (JSpinner) e.getSource();
                similarityValue = (Integer) source.getValue() / 100d;
                resulth.find_similarities(similarityValue);
                documentdispl.clearResults();
                documentdispl.showAllResults(resulth.getMultiResultDocs(), resulth.getMultiResultScores(),
                        resulth.getMultiResultFilenames(), navP.isScores(), navP.getTypeHighlight(), navP.isFileNames());
            }
        });

        JLabel lengthLabel = new JLabel("Length");
        JSpinner lengthSpinner = new JSpinner(new SpinnerNumberModel(compressionValue, 0, 100, 1));
        lengthSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner source = (JSpinner) e.getSource();
                setCompressionValue((Integer) source.getValue());
                if (clickedOnce) {
                    if (getMDSType() == MULTI_SDS) {
                        method_1();
                    } else if (getMDSType() == MULTIDOC_SUM) {
                        method_2();
                    } else if (getMDSType() == SIM_METHOD) {
                        method_3();
                    }
                    if (!sumClicked) {
                        showAll();
                    }
                }
            }
        });
        JLabel methodLabel = new JLabel("Method");
        final JComboBox methodCombo = new JComboBox(Scorer.methods);
        methodCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAlgorithmType((String) methodCombo.getSelectedItem());
            }
        });

        JButton sumButton = new JButton("Summarize");
        sumButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickedOnce = true;
                sumClicked = true;
                navP.setSumClicked(true);

                if (getMDSType() == MULTI_SDS) {
                    method_1();
                } else if (getMDSType() == MULTIDOC_SUM) {
                    method_2();
                } else if (getMDSType() == SIM_METHOD) {
                    method_3();
                } else if (getMDSType() == SINGLE_DOCUMENT) {
                    sds_method();
                }
            }
        });

        JButton showButton = new JButton("Show All");
        showButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAll();
            }
        });

        JLabel simLabel = new JLabel("Similarity");

        panelScore.add(lengthLabel);
        panelScore.add(lengthSpinner);
        panelScore.add(methodLabel);
        panelScore.add(methodCombo);

        GridBagConstraintsIFS gcSim = new GridBagConstraintsIFS().fillWidth();
        panelSim.add(simLabel, gcSim);
        panelSim.add(similaritySpinner, gcSim.nextCol());

        GridBagConstraintsIFS gc = new GridBagConstraintsIFS().setInsets(new Insets(5, 10, 5, 10));

        add(MDSLabel, gc);
        add(method_1, gc.nextRow());
        add(method_3, gc.nextCol());
        add(method_2, gc.nextRow());
        add(sds_method, gc.nextCol());

        add(panelSim, gc.nextRow().setGridWidth(2));
        add(panelScore, gc);
        panelSim.setVisible(false);
        panelSim.setPreferredSize(panelScore.getPreferredSize());

        add(sumButton, gc.nextRow().setGridWidth(1));
        add(showButton, gc.nextCol());

        method_1.setSelected(true);
        setMDSType(MULTI_SDS);
        setCompressionValue(((Integer) lengthSpinner.getValue()).intValue());
        setAlgorithmType(Scorer.TFxIDF);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setMDSType(e.getActionCommand());
        navP.sdsActive = e.getActionCommand() == SINGLE_DOCUMENT;
        toggleElements(e.getActionCommand() != SIM_METHOD);
    }

    private void toggleElements(boolean showFirstComponents) {
        panelScore.setVisible(showFirstComponents);
        panelSim.setVisible(!showFirstComponents);
    }

    private void showAll() {
        documentdispl.clearResults();
        sumClicked = false;
        this.navP.setSumClicked(false);

        if (getMDSType() == MULTI_SDS) {
            for (int i = 0; i < this.navP.itemNames.length; i++) {
                documentdispl.showOriginal(i, resulth.getResultDoc(i), resulth.getDocumentScores(i), navP.isScores(),
                        navP.getTypeHighlight(), navP.isFileNames());
            }
        }
        if (getMDSType() == MULTIDOC_SUM || getMDSType() == SIM_METHOD) {
            documentdispl.showOriginal(resulth.getMultiResultDocs(), resulth.getDocumentScores(), navP.isScores(),
                    navP.getTypeHighlight(), navP.isFileNames());
        }
        if (getMDSType() == SINGLE_DOCUMENT) {
            int selectedDoc = this.navP.getSelectedDocument();
            documentdispl.clearResults();
            documentdispl.showOriginal(selectedDoc, resulth.getResultDoc(selectedDoc),
                    resulth.getDocumentScores(selectedDoc), navP.isScores(), navP.getTypeHighlight(), false);
        }
    }

    private void method_1() {
        documentdispl.clearResults();
        for (int i = 0; i < this.navP.getFileNames().size(); i++) {
            DoubleArrayList scores = scorer.getScores(i, getAlgorithmType());
            if (scores == null) {
                System.out.println("an error occured while computing scores");
            }
            resulth.storeScore(i, scorer.getScores(i, getAlgorithmType()));
            resulth.createResult(i, getCompressionValue());
            documentdispl.showResult(i, resulth.getResultDoc(i), resulth.getResultScores(i), navP.isScores(),
                    navP.getTypeHighlight(), navP.isFileNames());
        }
    }

    private void method_2() {
        documentdispl.clearResults();
        DoubleArrayList allscores = new DoubleArrayList();
        for (int i = 0; i < this.navP.getFileNames().size(); i++) {
            resulth.storeScore(i, scorer.getScores(i, getAlgorithmType()));
            allscores.addAllOf(scorer.getScores(i, getAlgorithmType()));
        }
        resulth.createAllResults(allscores, getCompressionValue());
        documentdispl.showAllResults(resulth.getMultiResultDocs(), resulth.getMultiResultScores(),
                resulth.getMultiResultFilenames(), navP.isScores(), navP.getTypeHighlight(), navP.isFileNames());
    }

    private void method_3() {
        documentdispl.clearResults();
        for (int i = 0; i < this.navP.getFileNames().size(); i++) {
            resulth.storeScore(i, scorer.getScores(i, getAlgorithmType()));
        }
        resulth.find_similarities(similarityValue);
        documentdispl.showAllResults(resulth.getMultiResultDocs(), resulth.getMultiResultScores(),
                resulth.getMultiResultFilenames(), navP.isScores(), navP.getTypeHighlight(), navP.isFileNames());
    }

    private void sds_method() {
        int compression = getCompressionValue();
        int selectedDoc = navP.getSelectedDocument();

        DoubleArrayList scoreArray = scorer.getScores(selectedDoc, getAlgorithmType());
        resulth.storeScore(selectedDoc, scoreArray);
        resulth.createResult(selectedDoc, compression);
        documentdispl.clearResults();
        documentdispl.showResult(selectedDoc, resulth.getResultDoc(selectedDoc), resulth.getResultScores(selectedDoc),
                navP.isScores(), navP.getTypeHighlight(), false);
    }

    void setMDSType(String type) {
        this.mdstype = type;
    }

    String getMDSType() {
        return this.mdstype;
    }

    void setCompressionValue(int val) {
        this.compressionValue = val;
    }

    int getCompressionValue() {
        return this.compressionValue;
    }

    void setAlgorithmType(String algo) {
        this.algorithm = algo;
    }

    String getAlgorithmType() {
        return this.algorithm;
    }
}
