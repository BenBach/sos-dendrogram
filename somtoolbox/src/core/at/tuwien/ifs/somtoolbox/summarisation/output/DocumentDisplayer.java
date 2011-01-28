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
package at.tuwien.ifs.somtoolbox.summarisation.output;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Julius Penaranda
 * @version $Id: DocumentDisplayer.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class DocumentDisplayer {
    private StyledDocument doc = null;

    private JTextPane textp = null;

    String newline = "\n";

    ArrayList<String>[] parsedDocuments = null;

    private Object[] itemNames = null;

    private CommonSOMViewerStateData state = null;

    private SOMLibSparseInputData inputvectors = null;

    private SOMLibTemplateVector templatevectors = null;

    private IntArrayList intarray = new IntArrayList();

    private DoubleArrayList doubarray = new DoubleArrayList();

    public DocumentDisplayer(CommonSOMViewerStateData st, JScrollPane pane, ArrayList<String>[] parsedDoc,
            Object[] items) {
        this.state = st;
        this.inputvectors = (SOMLibSparseInputData) state.growingSOM.getSharedInputObjects().getData(
                SOMVisualisationData.INPUT_VECTOR);
        this.templatevectors = (SOMLibTemplateVector) state.inputDataObjects.getData(SOMVisualisationData.TEMPLATE_VECTOR);

        this.parsedDocuments = parsedDoc;
        this.textp = new JTextPane();
        textp.setCaretPosition(0);
        this.itemNames = items;
        this.textp.setSize(new Dimension(600, 600));
        pane.setViewportView(textp);
        this.doc = textp.getStyledDocument();
        System.out.println("*** IN Document displayer");
    }

    /**
     * displays results
     */
    public void showResult(int docID, ArrayList<String> resultdoc, DoubleArrayList resultscores, boolean scores,
            int highl, boolean filename) {
        try {
            // display title
            if (filename) {
                doc.insertString(doc.getLength(), this.parsedDocuments[docID].get(0) + " ", doc.getStyle("bold"));
                doc.insertString(doc.getLength(), (String) this.itemNames[docID] + " " + newline, doc.getStyle("bold2"));
            } else {
                doc.insertString(doc.getLength(), this.parsedDocuments[docID].get(0) + newline, doc.getStyle("bold"));
            }

            for (int i = 0; i < resultdoc.size(); i++) {
                String sentence = resultdoc.get(i);

                if (highl == -1) {
                    doc.insertString(doc.getLength(), sentence + newline, doc.getStyle("regular"));
                } else if (highl == 1) {
                    highlight_sent(getBorderSentence(resultscores), sentence, resultscores.get(i));
                } else if (highl == 2) {
                    double border = getBorderWord(docID);
                    highlight_word(border, sentence);
                }

                if (scores) {
                    doc.insertString(doc.getLength(), "scores: ", doc.getStyle("regular"));
                    doc.insertString(doc.getLength(), StringUtils.format(resultscores.get(i), 3) + newline,
                            doc.getStyle("underline"));

                }
            }
            doc.insertString(doc.getLength(), newline, doc.getStyle("regular"));
            // doc.insertString(doc.getLength(), newline, doc.getStyle("regular"));

        } catch (BadLocationException ble) {
            System.err.println("Error in ResultHandler: displayResults():" + ble.getMessage());
        }
        setCaretPosition(0);
    }

    public void showAllResults(ArrayList<String> result, DoubleArrayList resultscores,
            ArrayList<String> resultItemnames, boolean scores, int highl, boolean filename) {
        try {
            if (result.size() == 0) {
                doc.insertString(doc.getLength(), "No results!" + newline, doc.getStyle("regular"));
            } else {

                for (int i = 0; i < result.size(); i++) {
                    String sentence = result.get(i);

                    if (highl == -1) {
                        doc.insertString(doc.getLength(), sentence + newline, doc.getStyle("regular"));
                    } else if (highl == 1) {
                        highlight_sent(getBorderSentence(resultscores), sentence, resultscores.get(i));
                    } else if (highl == 2) {
                        double border = getBorderWord();
                        highlight_word(border, sentence);
                    }

                    if (scores) {
                        doc.insertString(doc.getLength(), "scores: ", doc.getStyle("regular"));
                        doc.insertString(doc.getLength(), StringUtils.format(resultscores.get(i), 3),
                                doc.getStyle("underline"));
                        if (!filename) {
                            doc.insertString(doc.getLength(), newline, doc.getStyle("regular"));
                        }
                    }
                    if (filename) {
                        doc.insertString(doc.getLength(), " filename: ", doc.getStyle("regular"));
                        doc.insertString(doc.getLength(), resultItemnames.get(i) + newline, doc.getStyle("underline"));
                    }
                }
            }
            doc.insertString(doc.getLength(), newline, doc.getStyle("regular"));
            // doc.insertString(doc.getLength(), newline, doc.getStyle("regular"));
        } catch (BadLocationException bl) {
            System.err.println("Error in ResultHandler: displayResults():" + bl.getMessage());
        }
        setCaretPosition(0);
    }

    /**
     * shows full text and highlights selected sentences used for summarization
     */
    public void showOriginal(ArrayList<String> result, DoubleArrayList[] allscores, boolean scores, int highl,
            boolean filename) {
        for (int i = 0; i < this.itemNames.length; i++) {
            showOriginal(i, result, allscores[i], scores, highl, filename);
        }
    }

    /** shows original text and highlights the sentences equal to the sentences in 'result' */
    public void showOriginal(int docID, ArrayList<String> result, DoubleArrayList allscores, boolean scores, int highl,
            boolean filename) {
        try {
            // display title
            if (filename) {
                doc.insertString(doc.getLength(), this.parsedDocuments[docID].get(0) + " ", doc.getStyle("bold"));
                doc.insertString(doc.getLength(), (String) this.itemNames[docID] + " " + newline, doc.getStyle("bold2"));
            } else {
                doc.insertString(doc.getLength(), this.parsedDocuments[docID].get(0) + newline, doc.getStyle("bold"));
            }

            ArrayList<String> fulltext = this.parsedDocuments[docID];
            String displaysentence;

            // for each sentence, ignore title
            for (int i = 1; i < fulltext.size(); i++) {
                displaysentence = fulltext.get(i);
                if (result != null) {
                    boolean found = false;
                    for (int j = 0; j < result.size(); j++) {
                        String sent = result.get(j);
                        if (sent.equals(displaysentence)) {
                            if (highl == -1) {
                                doc.insertString(doc.getLength(), displaysentence + newline, doc.getStyle("regular"));
                            } else if (highl == 1) {
                                highlight_sent(getBorderSentence(allscores), displaysentence, allscores.get(i - 1));
                            } else if (highl == 2) {
                                double border = getBorderWord(docID);
                                highlight_word(border, displaysentence);
                            }

                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        doc.insertString(doc.getLength(), displaysentence + newline, doc.getStyle("regular"));
                    }

                } else {
                    doc.insertString(doc.getLength(), displaysentence + newline, doc.getStyle("regular"));
                }
                if (scores) {
                    doc.insertString(doc.getLength(), "scores: ", doc.getStyle("regular"));
                    doc.insertString(doc.getLength(), StringUtils.format(allscores.get(i - 1), 3) + newline,
                            doc.getStyle("underline"));
                }
            }
            doc.insertString(doc.getLength(), newline, doc.getStyle("regular"));

        } catch (Exception e) {
            System.err.println("Error in ResultHandler: showAll(): " + e.getMessage());
        }
        setCaretPosition(0);
    }

    public void searchString(int docID, ArrayList<String> doku, String str) {
        try {
            ArrayList<String> text = null;
            if (doku == null) {
                text = this.parsedDocuments[docID];
            } else {
                text = doku;
            }
            String searchString = str.toLowerCase();

            // display title
            doc.insertString(doc.getLength(), this.parsedDocuments[docID].get(0) + newline, doc.getStyle("bold"));

            for (int i = 0; i < text.size(); i++) {
                int j = i;
                if (doku == null) { // if search in original text, ignore title
                    j = j + 1;
                    if (j == text.size()) {
                        break;
                    }
                }
                String sentence = text.get(j).toLowerCase();
                if (sentence.indexOf(searchString) != -1 && !str.equals("")) {
                    int searchindex = 0;
                    int beginning = 0;
                    while (sentence.indexOf(searchString, beginning) != -1) {
                        searchindex = sentence.indexOf(searchString, beginning);
                        doc.insertString(doc.getLength(), text.get(j).substring(beginning, searchindex),
                                doc.getStyle("regular"));
                        doc.insertString(doc.getLength(), text.get(j).substring(searchindex,
                                searchindex + searchString.length()), doc.getStyle("highlight"));
                        beginning = searchindex + searchString.length();
                    }
                    doc.insertString(doc.getLength(), text.get(j).substring(beginning) + newline,
                            doc.getStyle("regular"));
                } else {
                    doc.insertString(doc.getLength(), text.get(j) + newline, doc.getStyle("regular"));
                }
                // doc.insertString(doc.getLength(),truncate_score(this.scorearrays[h].get(i))+newline,
                // doc.getStyle("underline"));
            }
            doc.insertString(doc.getLength(), newline, doc.getStyle("regular"));

        } catch (Exception e) {
            System.err.println("Error in ResultHandler: searchString(): " + e.getMessage());
        }
    }

    private double getBorderSentence(DoubleArrayList doublearray) {
        DoubleArrayList array = new DoubleArrayList();
        array = doublearray.copy();
        array.quickSort();
        array.reverse();
        double maxValue = array.get(0);
        return maxValue / 5;
    }

    private double getBorderWord(int ind) {
        this.intarray = new IntArrayList();
        this.doubarray = new DoubleArrayList();
        double maxScoreWord = 0.0;

        DoubleMatrix1D doublevec = inputvectors.getInputDatum((String) this.itemNames[ind]).getVector();
        doublevec.getNonZeros(intarray, doubarray);
        ArrayList<String> doku = this.parsedDocuments[ind];

        for (int i = 1; i < doku.size(); i++) {
            // for each word in template vector
            for (int b = 0; b < intarray.size(); b++) {
                String word = templatevectors.getLabel(intarray.get(b));

                if (doku.get(i).indexOf(word) != -1) {
                    if (maxScoreWord < doubarray.get(b)) {
                        maxScoreWord = doubarray.get(b);
                    }
                }
            }
        }
        return maxScoreWord / 5;
    }

    private double getBorderWord() {
        this.intarray = new IntArrayList();
        this.doubarray = new DoubleArrayList();
        double maxScoreWord = 0.0;

        for (int i = 1; i < this.itemNames.length; i++) {
            IntArrayList tempintarray = new IntArrayList();
            DoubleArrayList tempdoubarray = new DoubleArrayList();
            DoubleMatrix1D doublevec = inputvectors.getInputDatum((String) this.itemNames[i]).getVector();
            doublevec.getNonZeros(tempintarray, tempdoubarray);
            this.intarray.addAllOf(tempintarray);
            this.doubarray.addAllOf(tempdoubarray);
            ArrayList<String> doku = this.parsedDocuments[i];

            for (int j = 1; j < doku.size(); j++) {
                // for each word in template vector
                for (int b = 0; b < intarray.size(); b++) {
                    String word = templatevectors.getLabel(intarray.get(b));

                    if (doku.get(j).indexOf(word) != -1) {
                        if (maxScoreWord < doubarray.get(b)) {
                            maxScoreWord = doubarray.get(b);
                        }
                    }
                }
            }
        }
        return maxScoreWord / 5;
    }

    private void highlight_word(double border, String sent) {
        try {
            int sent_index = 0;
            StringTokenizer st = new StringTokenizer(sent, "\u0020\t.;,?!\"");

            while (st.hasMoreElements()) {
                String token = st.nextToken();
                double score = 0.0;
                double tempborder = 0.0;

                for (int b = 0; b < intarray.size(); b++) {
                    String word = templatevectors.getLabel(intarray.get(b));
                    String token_word = token.toLowerCase();
                    if (token_word.indexOf(word, 0) != -1) {
                        if (score < doubarray.get(b)) {
                            score = doubarray.get(b);
                        }
                    }
                }
                if (sent_index <= sent.length()) {
                    if (score <= border) {
                        int begin = sent.indexOf(token, sent_index);
                        doc.insertString(doc.getLength(), sent.substring(sent_index, begin + token.length() + 1),
                                doc.getStyle("highlight_1"));
                        sent_index = begin + token.length() + 1;
                    } else {
                        tempborder = border * 2;
                        if (score <= tempborder) {
                            int begin = sent.indexOf(token, sent_index);
                            doc.insertString(doc.getLength(), sent.substring(sent_index, begin + token.length() + 1),
                                    doc.getStyle("highlight_2"));
                            sent_index = begin + token.length() + 1;
                        } else {
                            tempborder = border * 3;
                            if (score <= tempborder) {
                                int begin = sent.indexOf(token, sent_index);
                                doc.insertString(doc.getLength(),
                                        sent.substring(sent_index, begin + token.length() + 1),
                                        doc.getStyle("highlight_3"));
                                sent_index = begin + token.length() + 1;
                            } else {
                                tempborder = border * 4;
                                if (score <= tempborder) {
                                    int begin = sent.indexOf(token, sent_index);
                                    doc.insertString(doc.getLength(), sent.substring(sent_index, begin + token.length()
                                            + 1), doc.getStyle("highlight_4"));
                                    sent_index = begin + token.length() + 1;
                                } else {
                                    int begin = sent.indexOf(token, sent_index);
                                    doc.insertString(doc.getLength(), sent.substring(sent_index, begin + token.length()
                                            + 1), doc.getStyle("highlight_5"));
                                    sent_index = begin + token.length() + 1;
                                }
                            }
                        }
                    }
                }
            }
            doc.insertString(doc.getLength(), newline, doc.getStyle("regular"));
        } catch (Exception f) {
            System.err.println("Error in ResultHandler: highlight(int,String,double): " + f.getMessage());
        }
    }

    /** highlights sentence with a colour according to its score */
    private void highlight_sent(double border, String sent, double score) {
        try {

            double tempborder = 0.0;
            if (score <= border) {
                doc.insertString(doc.getLength(), sent + newline, doc.getStyle("highlight_1"));
            } else {
                tempborder = border * 2;
                if (score <= tempborder) {
                    doc.insertString(doc.getLength(), sent + newline, doc.getStyle("highlight_2"));
                } else {
                    tempborder = border * 3;
                    if (score <= tempborder) {
                        doc.insertString(doc.getLength(), sent + newline, doc.getStyle("highlight_3"));
                    } else {
                        tempborder = border * 4;
                        if (score <= tempborder) {
                            doc.insertString(doc.getLength(), sent + newline, doc.getStyle("highlight_4"));
                        } else {
                            // tempborder= border*5;
                            // if (score <= tempborder) {
                            doc.insertString(doc.getLength(), sent + newline, doc.getStyle("highlight_5"));
                            // }
                        }
                    }
                }
            }
        } catch (Exception f) {
            System.err.println("Error in ResultHandler: highlight(int,String,double): " + f.getMessage());
        }
    }

    public void setCaretPosition(int i) {
        this.textp.setCaretPosition(i);
    }

    public void clearResults() {
        try {
            this.doc.remove(0, doc.getLength());
        } catch (Exception f) {
            System.err.println("SingleDocument: clearResults(): " + f.getMessage());
        }
    }

    public void setPalette(Color[] palet) {
        addStylesToDocument(doc, palet);
    }

    private void removehighlightStyles() {
        doc.removeStyle("highlight_1");
        doc.removeStyle("highlight_2");
        doc.removeStyle("highlight_3");
        doc.removeStyle("highlight_4");
        doc.removeStyle("highlight_5");
        doc.removeStyle("regular");
        doc.removeStyle("bold");
        doc.removeStyle("bold2");
        doc.removeStyle("small");
        doc.removeStyle("large");

    }

    protected void addStylesToDocument(StyledDocument doc, Color[] col) {
        removehighlightStyles();
        // Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontSize(regular, 14);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);
        StyleConstants.setFontSize(s, 14);

        s = doc.addStyle("underline", regular);
        StyleConstants.setUnderline(s, true);

        Color color = col[4];
        s = doc.addStyle("highlight", regular);
        StyleConstants.setBackground(s, color);

        color = col[6];
        s = doc.addStyle("highlight_1", regular);
        StyleConstants.setBackground(s, color);

        color = col[5];
        s = doc.addStyle("highlight_2", regular);
        StyleConstants.setBackground(s, color);

        color = col[4];
        s = doc.addStyle("highlight_3", regular);
        StyleConstants.setBackground(s, color);

        color = col[3];
        s = doc.addStyle("highlight_4", regular);
        StyleConstants.setBackground(s, color);

        color = col[2];
        s = doc.addStyle("highlight_5", regular);
        StyleConstants.setBackground(s, color);

        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, Color.RED);
        StyleConstants.setFontSize(s, 20);

        s = doc.addStyle("bold2", regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, Color.BLACK);
        StyleConstants.setFontSize(s, 20);

        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 14);

        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 22);
    }

}
