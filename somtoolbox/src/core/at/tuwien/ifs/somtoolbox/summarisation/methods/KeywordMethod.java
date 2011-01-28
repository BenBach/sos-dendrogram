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

import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;

/**
 * @author Julius Penaranda
 * @version $Id: KeywordMethod.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class KeywordMethod extends TFxIDF {

    private DoubleArrayList keyarray = null;

    public KeywordMethod(PartOfSpeech p) {
        super(p);
    }

    @Override
    public void setDocument(String filename, ArrayList<String> doc) {
        super.setDocument(filename, doc);
    }

    @Override
    public DoubleArrayList computeScores(String type) {
        keyarray = new DoubleArrayList();

        if (type == Scorer.KEYWORD_BOTH) {
            DoubleArrayList noun = new DoubleArrayList();
            DoubleArrayList verb = new DoubleArrayList();
            noun = super.computeScores(PartOfSpeech.NOUN);
            verb = super.computeScores(PartOfSpeech.VERB);
            // System.out.println("noun array: "+ noun.toString());
            // System.out.println("verb array: "+ verb.toString());
            double result = 0.0;

            for (int i = 0; i < noun.size(); i++) {
                result = noun.get(i) + verb.get(i);
                keyarray.add(result);
            }
            // System.out.println("both array: "+ keyarray.toString());
        } else if (type == Scorer.KEYWORD_NOUN) {
            keyarray = super.computeScores(PartOfSpeech.NOUN);
        } else if (type == Scorer.KEYWORD_VERB) {
            keyarray = super.computeScores(PartOfSpeech.VERB);
        }

        return keyarray;
    }

}
