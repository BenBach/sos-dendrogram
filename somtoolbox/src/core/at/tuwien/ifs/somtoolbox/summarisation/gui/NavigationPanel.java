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
import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextField;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.summarisation.SummariserGUI;
import at.tuwien.ifs.somtoolbox.summarisation.output.DocumentDisplayer;
import at.tuwien.ifs.somtoolbox.summarisation.output.ResultHandler;
import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;
import at.tuwien.ifs.somtoolbox.util.CollectionUtils;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.visualization.ColorGradientFactory;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: NavigationPanel.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class NavigationPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    Object[] itemNames = null;

    private Scorer scorer = null;

    private ResultHandler resulth = null;

    private DocumentDisplayer documentdispl = null;

    private OptionPanel optionPanel = null;

    private SearchPanel searchPanel = null;

    int selectedDocument = 0;

    private SingleDocumentSummarisationPanel sdsPanel = null;

    private MultiDocumentSummarisationPanel mdsPanel = null;

    private DocumentListPanel doclistPanel = null;

    boolean sdsActive = true;

    private SummariserGUI summarizer = null;

    private boolean sumClicked = false;

    private PalettePanel palettePanel = null;

    private CommonSOMViewerStateData state;

    public NavigationPanel(SummariserGUI sum, CommonSOMViewerStateData state, Object[] itemNames) {
        super(new GridBagLayout());
        this.summarizer = sum;
        this.itemNames = itemNames;
        this.state = state;

        InputData inputvectors = state.growingSOM.getSharedInputObjects().getInputData();
        SOMLibTemplateVector templatevectors = state.inputDataObjects.getTemplateVector();

        scorer = new Scorer(itemNames, inputvectors, templatevectors);
        scorer.setFileNamePrefix(CommonSOMViewerStateData.fileNamePrefix);
        scorer.parseDocuments();
        resulth = new ResultHandler(itemNames, scorer.getParsedDocuments());
        documentdispl = new DocumentDisplayer(state, sum.scrollP, scorer.getParsedDocuments(), itemNames);

        palettePanel = new PalettePanel(this);
        doclistPanel = new DocumentListPanel(this);
        searchPanel = new SearchPanel(this);
        optionPanel = new OptionPanel(this);

        setPalette(ColorGradientFactory.GrayscaleGradient().toPalette(9));

        GridBagConstraintsIFS gc = new GridBagConstraintsIFS(GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL);
        if (itemNames.length > 1) {
            mdsPanel = new MultiDocumentSummarisationPanel(this, scorer, resulth, documentdispl);
            add(mdsPanel, gc);
            add(doclistPanel, gc.nextRow());
            sdsActive = false;
            setTitle("Multi-document summarisation: " + itemNames.length + " items selected");
        } else {
            sdsPanel = new SingleDocumentSummarisationPanel(this, scorer, resulth, documentdispl);
            add(sdsPanel, gc);
            if (itemNames.length > 0) {
                setTitle("Single document summarisation: " + itemNames[0]);
            }
        }
        add(optionPanel, gc.nextRow());
        add(palettePanel, gc.nextRow());
        add(searchPanel, gc.nextRow());
        repaint();
    }

    public void setPalette(Color[] palette) {
        documentdispl.setPalette(palette);
    }

    public int getSelectedDocument() {
        return selectedDocument;
    }

    ArrayList<String> getFileNames() {
        return scorer.getFileNames();
    }

    public int getIndexofFileName(String fn) {
        return CollectionUtils.indexOf(getFileNames(), fn);
    }

    String[] getData() {
        ArrayList<String> filenames = getFileNames();
        String[] stringnames = // new String[filenames.size()];
        filenames.toArray(new String[filenames.size()]);
        for (int i = 0; i < stringnames.length; i++) {
            try {
                if (stringnames[i] != null) {
                    stringnames[i] = URLDecoder.decode(stringnames[i], "UTF-8");
                    // stringnames[i] = stringnames[i].replaceAll("%2F", "/");
                    // System.out.println("name " + i + ": " + stringnames[i]);
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (int i = 0; i < filenames.size(); i++) {
            // System.out.println("filename: " + stringnames[i]);
            // System.out.println("sentences: " + scorer.getNumbOfSent(i));
            stringnames[i] = stringnames[i] + " (" + scorer.getNumbOfSent(i) + ")";
        }
        return stringnames;
    }

    void setTitle(String title) {
        this.summarizer.setTitle(title);
    }

    void setSumClicked(boolean click) {
        this.sumClicked = click;
    }

    boolean getSumClicked() {
        return this.sumClicked;
    }

    public int getNumbOfSent(int i) {
        return scorer.getNumbOfSent(i);
    }

    public int getTypeHighlight() {
        int highl = -1;
        if (optionPanel.highlightCB.isSelected()) {
            highl = 1;
        } else if (optionPanel.wordCB.isSelected()) {
            highl = 2;
        }
        return highl;
    }

    public boolean isScores() {
        return optionPanel.scoreCB.isSelected();
    }

    public boolean isFileNames() {
        return optionPanel.filenameCB.isSelected();
    }

    /**
     * updates the results once one of the checkboxes is clicked (scores, highlights..)
     */
    void updateResults() {
        documentdispl.clearResults();
        if (sdsActive) {
            int selectedDoc = getSelectedDocument();
            documentdispl.clearResults();
            if (getSumClicked()) {
                documentdispl.showResult(selectedDoc, resulth.getResultDoc(selectedDoc),
                        resulth.getResultScores(selectedDoc), isScores(), getTypeHighlight(), false);
            } else {
                documentdispl.showOriginal(selectedDoc, resulth.getResultDoc(selectedDoc),
                        resulth.getDocumentScores(selectedDoc), isScores(), getTypeHighlight(), false);
            }
        } else {
            // if sumButton was clicked, update in summary
            if (getSumClicked()) {
                if (mdsPanel.getMDSType() == MultiDocumentSummarisationPanel.MULTI_SDS) {
                    for (int i = 0; i < getFileNames().size(); i++) {
                        documentdispl.showResult(i, resulth.getResultDoc(i), resulth.getResultScores(i), isScores(),
                                getTypeHighlight(), isFileNames());
                    }
                } else if (mdsPanel.getMDSType() == MultiDocumentSummarisationPanel.MULTIDOC_SUM
                        || mdsPanel.getMDSType() == MultiDocumentSummarisationPanel.SIM_METHOD) {
                    documentdispl.showAllResults(resulth.getMultiResultDocs(), resulth.getMultiResultScores(),
                            resulth.getMultiResultFilenames(), isScores(), getTypeHighlight(), isFileNames());
                }
            }
            // else update in original text
            else {
                if (mdsPanel.getMDSType() == MultiDocumentSummarisationPanel.MULTI_SDS) {
                    for (int i = 0; i < getFileNames().size(); i++) {
                        // resulth.showOriginal(i, score, highlight, filename_bool, false);
                        documentdispl.showOriginal(i, resulth.getResultDoc(i), resulth.getDocumentScores(i),
                                isScores(), getTypeHighlight(), isFileNames());
                    }
                } else if (mdsPanel.getMDSType() == MultiDocumentSummarisationPanel.MULTIDOC_SUM
                        || mdsPanel.getMDSType() == MultiDocumentSummarisationPanel.SIM_METHOD) {
                    // resulth.showOriginal(-1, score,highlight, filename_bool, true);
                    documentdispl.showOriginal(resulth.getMultiResultDocs(), resulth.getDocumentScores(), isScores(),
                            getTypeHighlight(), isFileNames());
                }
            }
        }
    }

    public void searchActionPerformed(ActionEvent e) {
        JTextField searchField = (JTextField) e.getSource();
        documentdispl.clearResults();
        ArrayList<String> doku = null;
        if (sdsActive) {
            if (getSumClicked()) {
                doku = resulth.getResultDoc(getSelectedDocument());
            }
            documentdispl.searchString(getSelectedDocument(), doku, searchField.getText());
        } else {
            for (int i = 0; i < getFileNames().size(); i++) {
                if (getSumClicked()) {
                    doku = resulth.getResultDoc(i);
                }
                documentdispl.searchString(i, doku, searchField.getText());
            }
        }
        documentdispl.setCaretPosition(0);
    }

    public CommonSOMViewerStateData getState() {
        return state;
    }

}
