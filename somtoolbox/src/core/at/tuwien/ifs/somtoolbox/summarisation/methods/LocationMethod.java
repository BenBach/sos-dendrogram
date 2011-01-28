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
 * @version $Id: LocationMethod.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class LocationMethod extends TFxIDF {
    private ArrayList<String> doc;

    private DoubleArrayList lmarray;

    private DoubleArrayList finalarray;

    public LocationMethod(PartOfSpeech p) {
        super(p);
    }

    @Override
    public void setDocument(String filename, ArrayList<String> doc) {
        this.doc = doc;
        super.setDocument(filename, doc);
    }

    /**
     * computes LocationMethod scores of all sentences
     */
    public DoubleArrayList computeScores() {
        DoubleArrayList sentscores = super.computeScores(Scorer.ALL);
        lmarray = new DoubleArrayList();
        finalarray = new DoubleArrayList();

        // for each sentence, ignore title
        for (int i = 1; i < this.doc.size(); i++) {
            if (i == 1) {
                lmarray.add(2.0);
                finalarray.add(sentscores.get(i - 1) + 2.0);
            } else if (i == this.doc.size() - 1) {
                lmarray.add(1.0);
                finalarray.add(sentscores.get(i - 1) + 1.0);
            } else {
                lmarray.add(0.0);
                finalarray.add(sentscores.get(i - 1));
            }
        }
        return finalarray;
    }

    public DoubleArrayList getLocationScores() {
        return this.lmarray;
    }

}
