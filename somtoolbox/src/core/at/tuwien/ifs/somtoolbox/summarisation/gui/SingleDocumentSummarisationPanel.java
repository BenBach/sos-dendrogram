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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
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
 * @author Rudolf Mayer
 * @version $Id: SingleDocumentSummarisationPanel.java 3883 2010-11-02 17:13:23Z frank $
 */
public class SingleDocumentSummarisationPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private ButtonGroup group = null;

    private String algorithm = null;

    private int compressionValue = 0;

    private NavigationPanel navP = null;

    private Scorer scorer = null;

    private ResultHandler resulth = null;

    private DocumentDisplayer documentdispl = null;

    public SingleDocumentSummarisationPanel(NavigationPanel navPanel, Scorer sc, ResultHandler rh,
            DocumentDisplayer docdis) {
        super(new GridBagLayout());
        this.navP = navPanel;
        this.scorer = sc;
        this.resulth = rh;
        this.documentdispl = docdis;

        this.compressionValue = 50;
        this.algorithm = Scorer.TFxIDF;

        setBorder(BorderFactory.createEtchedBorder());

        JLabel methodsLabel = new JLabel("Methods");
        methodsLabel.setForeground(Color.blue);

        JRadioButton fullTextButton = new JRadioButton("Full text");
        fullTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAll();
            }
        });
        fullTextButton.setSelected(true);

        JRadioButton tfxidfButton = new JRadioButton(Scorer.TFxIDF);
        tfxidfButton.addActionListener(this);

        JRadioButton locButton = new JRadioButton(Scorer.LOCATION);
        locButton.addActionListener(this);

        JRadioButton titleButton = new JRadioButton(Scorer.TITLE_METHOD);
        titleButton.addActionListener(this);

        JRadioButton keyNounButton = new JRadioButton(Scorer.KEYWORD_NOUN);
        keyNounButton.addActionListener(this);

        JRadioButton keyVerbButton = new JRadioButton(Scorer.KEYWORD_VERB);
        keyVerbButton.addActionListener(this);

        JRadioButton keyBothButton = new JRadioButton(Scorer.KEYWORD_BOTH);
        keyBothButton.addActionListener(this);

        JRadioButton combinedButton = new JRadioButton(Scorer.COMBINED);
        combinedButton.addActionListener(this);

        JSpinner lengthSpinner = new JSpinner();
        lengthSpinner.setModel(new SpinnerNumberModel(compressionValue, 0, 100, 1));
        lengthSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                compressionValue = (Integer) ((JSpinner) e.getSource()).getValue();
                summarize();
            }
        });

        // Group RadioButtons
        group = new ButtonGroup();
        group.add(fullTextButton);
        group.add(tfxidfButton);
        group.add(locButton);
        group.add(titleButton);
        group.add(keyNounButton);
        group.add(keyVerbButton);
        group.add(keyBothButton);
        group.add(combinedButton);

        GridBagConstraintsIFS gc = new GridBagConstraintsIFS().setInsets(new Insets(5, 10, 5, 10));

        add(methodsLabel, gc);
        add(fullTextButton, gc.nextRow());
        add(tfxidfButton, gc.nextRow());
        add(keyNounButton, gc.nextCol());
        add(locButton, gc.nextRow());
        add(keyVerbButton, gc.nextCol());
        add(combinedButton, gc.nextRow());
        add(keyBothButton, gc.nextCol());

        add(new JLabel("Length"), gc.nextRow().setAnchor(GridBagConstraints.NORTHEAST));
        add(lengthSpinner, gc.nextCol().setAnchor(GridBagConstraints.NORTHWEST));

        showAll();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.algorithm = e.getActionCommand();
        summarize();
    }

    private void showAll() {
        this.navP.setSumClicked(false);
        int selectedDoc = this.navP.getSelectedDocument();
        documentdispl.clearResults();
        documentdispl.showOriginal(selectedDoc, resulth.getResultDoc(selectedDoc),
                resulth.getDocumentScores(selectedDoc), navP.isScores(), navP.getTypeHighlight(), false);
    }

    private void summarize() {
        this.navP.setSumClicked(true);
        int selectedDoc = this.navP.getSelectedDocument();

        DoubleArrayList scoreArray = scorer.getScores(selectedDoc, algorithm);
        resulth.storeScore(selectedDoc, scoreArray);
        resulth.createResult(selectedDoc, this.compressionValue);
        documentdispl.clearResults();
        documentdispl.showResult(selectedDoc, resulth.getResultDoc(selectedDoc), resulth.getResultScores(selectedDoc),
                navP.isScores(), navP.getTypeHighlight(), false);
    }

}
