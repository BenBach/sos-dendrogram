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

import java.util.ArrayList;

import cern.colt.list.DoubleArrayList;

import at.tuwien.ifs.somtoolbox.util.ArrayUtils;

/**
 * @author Julius Penaranda
 * @version $Id: MultiDocumentHandler.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class MultiDocumentHandler {
    private static final String WORD_DELIMITER = "  \n.,!?";

    private ArrayList<String>[] parsedDocuments = null;

    private DoubleArrayList[] allscores = null;

    private ArrayList<String> resultdoc = null;

    private ArrayList<String> resultfilenames = null;

    private DoubleArrayList resultscores = null;

    private double degree = 0;

    private Object[] itemNames = null;

    MultiDocumentHandler(Object[] items, ArrayList<String>[] parsedDoc) {
        this.itemNames = items;
        this.parsedDocuments = parsedDoc;
    }

    public void createAllResults(double threshold) {
        this.resultdoc = new ArrayList<String>();
        this.resultfilenames = new ArrayList<String>();
        this.resultscores = new DoubleArrayList();

        System.out.println("multidocumenthandler: threshold: " + threshold);

        // for each document
        for (int j = 0; j < this.parsedDocuments.length; j++) {
            // for each score of sentence
            for (int h = 0; h < this.allscores[j].size(); h++) {
                if (this.allscores[j].get(h) >= threshold) {
                    resultdoc.add(this.parsedDocuments[j].get(h + 1));
                    resultfilenames.add((String) this.itemNames[j]);
                    resultscores.add(this.allscores[j].get(h));
                }
            }
        }
        removeRedundancy(resultdoc, resultscores, resultfilenames);
    }

    public void removeRedundancy(ArrayList<String> resultd, DoubleArrayList resultsc, ArrayList<String> resultfile) {
        ArrayList<String> doc = new ArrayList<String>();
        DoubleArrayList score = new DoubleArrayList();
        ArrayList<String> files = new ArrayList<String>();

        // for each sentence
        for (int i = 0; i < resultd.size(); i++) {
            String sent = resultdoc.get(i);
            double simMax = 0; // max similarity value
            int sentMax = 0; // sentence with max similarity

            for (int j = 0; j < resultd.size(); j++) {
                if (i != j) {
                    String sent2 = resultdoc.get(j);
                    double sim = computeSimilarity(sent, sent2);
                    if (sim >= 0.5 && simMax < sim) {
                        simMax = sim;
                        sentMax = j;
                    }
                }
            }

            if (simMax == 0) { // if there's no similarity
                doc.add(resultd.get(i));
                score.add(resultsc.get(i));
                files.add(resultfile.get(i));
            } else { // else
                if (sentMax > i) { // if similar sentence hasn't been examined yet
                    if (resultsc.get(i) >= resultsc.get(sentMax)) { // if score of 1st sent is greater than 2nd
                        doc.add(resultd.get(i)); // add 1st sent to result
                        score.add(resultsc.get(i));
                        files.add(resultfile.get(i));
                    } else { // otherwise add 2nd sent to result
                        doc.add(resultd.get(sentMax));
                        score.add(resultsc.get(sentMax));
                        files.add(resultfile.get(sentMax));
                    }
                }
            }
        }
        this.resultdoc = doc;
        this.resultscores = score;
        this.resultfilenames = files;
    }

    public ArrayList<String> getResultDocs() {
        return resultdoc;
    }

    public DoubleArrayList getResultScores() {
        return resultscores;
    }

    public ArrayList<String> getResultFileNames() {
        return resultfilenames;
    }

    /**
     * identifies sentence similarity across documents; numdoc sets the minimum number of documents in which similarity
     * of sentence occurs; degree sets similarity degree;
     */
    void find_similarities(double degr) {
        this.degree = degr;
        this.resultdoc = new ArrayList<String>();
        this.resultscores = new DoubleArrayList();
        this.resultfilenames = new ArrayList<String>();

        for (int i = 0; i < this.parsedDocuments.length; i++) {

            for (int h = 0; h < this.parsedDocuments.length; h++) {

                if (i == 0 && i != h) {
                    // System.out.println("i: "+i+", h: "+h);
                    if (this.parsedDocuments[i].size() < this.parsedDocuments[h].size()) {
                        compareDocuments(h, i);
                    } else {
                        compareDocuments(i, h);
                    }
                }
                if (i != h && i != 0 && i < h) {
                    // System.out.println("i: "+i+", h: "+h);
                    if (this.parsedDocuments[i].size() < this.parsedDocuments[h].size()) {
                        compareDocuments(h, i);
                    } else {
                        compareDocuments(i, h);
                    }
                }
            }
        }
        removeRedundancy(resultdoc, resultscores, resultfilenames);
    }

    void compareDocuments(int d1, int d2) {
        // String[] tok2= null;
        ArrayList<String> doc = this.parsedDocuments[d1];
        ArrayList<String> doc2 = this.parsedDocuments[d2];

        // for each sentence in doc, ignore title
        for (int a = 1; a < doc.size(); a++) {
            String sent = doc.get(a).toLowerCase();

            double simMax = 0;
            int sentMax = 0;
            String finalsent = "";
            String finalsent2 = "";

            // for each sentence in doc2, ignore title
            for (int b = 1; b < doc2.size(); b++) {
                String sent2 = doc2.get(b).toLowerCase();

                double sim = computeSimilarity(sent, sent2);

                if (sim >= this.degree) {
                    if (simMax <= sim) {
                        simMax = sim;
                        sentMax = b;
                        finalsent = sent;
                        finalsent2 = sent2;
                    }
                }
            }

            if (simMax != 0.0) {
                System.out.println(this.itemNames[d1] + ": ");
                System.out.println(finalsent + ": " + this.allscores[d1].get(a - 1));
                System.out.println(this.itemNames[d2] + ": ");
                System.out.println(finalsent2 + ": " + this.allscores[d2].get(sentMax - 1));
                System.out.println("simMax: " + simMax);
                System.out.println("");
                System.out.println("-------------------------------------");
                if (this.allscores[d1].get(a - 1) >= this.allscores[d2].get(sentMax - 1)) {
                    this.resultdoc.add(this.parsedDocuments[d1].get(a));
                    this.resultscores.add(this.allscores[d1].get(a - 1));
                    this.resultfilenames.add(this.itemNames[d1] + ", " + this.itemNames[d2]);
                } else {
                    this.resultdoc.add(this.parsedDocuments[d2].get(sentMax));
                    this.resultscores.add(this.allscores[d2].get(sentMax - 1));
                    this.resultfilenames.add(this.itemNames[d1] + ", " + this.itemNames[d2]);
                }

            }
        } // for each sentence in doc
    }

    double computeSimilarity(String sent, String sent2) {
        String[] tok = sent.split(WORD_DELIMITER);
        String[] tok2 = sent2.split(WORD_DELIMITER);
        ArrayList<String> overlaparray = new ArrayList<String>();
        int overlap = 0;

        // for each sentence
        for (String element : tok) {
            String word = element;

            // check if word has been checked already
            if (!overlaparray.contains(word)) {
                int num1 = ArrayUtils.countOccurrences(word, tok);
                int num2 = ArrayUtils.countOccurrences(word, tok2);
                if (num1 != 0 && num2 != 0) {
                    if (num1 <= num2) {
                        overlap = overlap + num1;
                    } else {
                        overlap = overlap + num2;
                    }
                    overlaparray.add(word);
                }
            }
        }
        // System.out.println("overlapped words: "+overlaparray.toString());
        double overl = new Integer(overlap).doubleValue();
        double words_1 = new Integer(tok.length).doubleValue();
        double words_2 = new Integer(tok2.length).doubleValue();

        double sim = 2 * overl / (words_1 + words_2);
        // double sim2 = overl / ((words_1+words_2)-overl);

        return sim;
    }

    void storeScores(DoubleArrayList[] scores) {
        this.allscores = scores;
    }

}
