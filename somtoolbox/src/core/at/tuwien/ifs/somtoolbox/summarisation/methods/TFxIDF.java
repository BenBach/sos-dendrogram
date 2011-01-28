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

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;

/**
 * @author Julius Penaranda
 * @version $Id: TFxIDF.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class TFxIDF {
    private InputData inputVector = null;

    private SOMLibTemplateVector templateVector = null;

    private InputDatum inputd = null;

    private DoubleMatrix1D doublevec = null;

    private ArrayList<String> doc = null;

    private IntArrayList intarray = new IntArrayList();

    private DoubleArrayList doubarray = new DoubleArrayList();

    private DoubleArrayList tfxidfarray = null;

    private PartOfSpeech pos = null;

    public TFxIDF() {
        // do nothing
    }

    public TFxIDF(PartOfSpeech p) {
        this.pos = p;
    }

    public void setVectors(InputData input, SOMLibTemplateVector template) {
        this.inputVector = input;
        this.templateVector = template;
    }

    /** Sets document */
    public void setDocument(String filename, ArrayList<String> doc) {
        this.inputd = this.inputVector.getInputDatum(filename);
        this.doc = doc;
    }

    /**
     * computes scores of each sentence
     */
    public DoubleArrayList computeScores(String type) {
        this.intarray = new IntArrayList();
        this.doubarray = new DoubleArrayList();

        this.doublevec = this.inputd.getVector();
        this.doublevec.getNonZeros(intarray, doubarray);
        tfxidfarray = new DoubleArrayList(); // stores score values
        String sent;

        // for each sentence, ignore title
        for (int a = 1; a < this.doc.size(); a++) {
            sent = doc.get(a).toLowerCase(); // converts to lower case
            double sentvalue = 0.0;
            int numWords = 0;

            // store scores of all words
            if (type == Scorer.ALL) {
                // for each word in templatevector
                for (int b = 0; b < intarray.size(); b++) {
                    String word = templateVector.getLabel(intarray.get(b));
                    // add score value to 'sentvalue'
                    while (sent.indexOf(word) != -1) {
                        sentvalue = sentvalue + doubarray.get(b);
                        sent = sent.substring(sent.indexOf(word) + word.length());
                        numWords++;
                    }
                }
            }
            // store scores of other words, i.e. noun, adjective,..
            else {
                ArrayList<String> resulttokens = this.pos.getTokens(sent, type);

                for (int b = 0; b < intarray.size(); b++) {
                    String word = templateVector.getLabel(intarray.get(b));

                    for (int i = 0; i < resulttokens.size(); i++) {
                        if (resulttokens.get(i).indexOf(word) != -1) {
                            // System.out.println("word gefunden: "+word+"("+doubarray.get(b)+") value: "+sentvalue);
                            sentvalue = sentvalue + doubarray.get(b);
                            numWords++;
                        }
                    }
                }
            }

            // normalize and add score of sentence to tfxidf-Array
            if (sentvalue != 0) {
                sentvalue = sentvalue / new Integer(numWords).doubleValue();
            }
            tfxidfarray.add(sentvalue);
        }

        return tfxidfarray;
    }

}
