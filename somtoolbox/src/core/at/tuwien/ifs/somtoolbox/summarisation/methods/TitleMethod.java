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
package at.tuwien.ifs.somtoolbox.summarisation.methods;

import java.util.ArrayList;
import java.util.StringTokenizer;

import cern.colt.list.DoubleArrayList;

import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;

/**
 * @author Julius Penaranda
 * @version $Id: TitleMethod.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class TitleMethod extends TFxIDF {
    private ArrayList<String> doc;

    private DoubleArrayList tharray;

    private DoubleArrayList finalarray;

    public TitleMethod(PartOfSpeech p) {
        super(p);
    }

    @Override
    public void setDocument(String filename, ArrayList<String> doc) {
        super.setDocument(filename, doc);
        this.doc = doc;
    }

    // # of phrases in title is divided by the total phrases in title.
    // this value is then multiplied by the constant 0.1, and adds to sentence weights
    public DoubleArrayList computeScores() {

        StringTokenizer strtok = new StringTokenizer(this.doc.get(0));
        DoubleArrayList sentscores = super.computeScores(Scorer.ALL);
        tharray = new DoubleArrayList();
        finalarray = new DoubleArrayList();
        String sent;
        int totalphrase = strtok.countTokens();
        double weight;
        double numphrase;

        // for each sentence in document, ignore title
        for (int i = 1; i < this.doc.size(); i++) {
            strtok = new StringTokenizer(this.doc.get(0));
            numphrase = 0;
            sent = this.doc.get(i).toLowerCase();

            while (strtok.hasMoreElements()) {
                int index = 0;
                String word = strtok.nextToken().toLowerCase();

                while (sent.indexOf(word, index) != -1) {
                    numphrase = numphrase + 1.0;
                    // System.out.println("word found: "+word+" in sentence nr: "+i);
                    index = sent.indexOf(word, index) + word.length();
                }
            }
            if (numphrase != 0.0) {
                weight = numphrase / new Integer(totalphrase).doubleValue() * 0.1;
            } else {
                weight = 0.0;
            }
            // System.out.println("weight for sentence "+i+": "+weight);
            // System.out.println("total for sentence "+i+": "+ (weight+ sentscores.get(i)));
            this.tharray.add(weight);
            this.finalarray.add(weight + sentscores.get(i - 1));
        }

        return this.finalarray;
    }

    /** returns Title-Scores */
    public DoubleArrayList getTitleScores() {
        return this.tharray;
    }

}
