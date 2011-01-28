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

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;

/**
 * @author Julius Penaranda
 * @version $Id: CombinedMethod.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class CombinedMethod extends TFxIDF {
    LocationMethod lm = null;

    TitleMethod tm = null;

    KeywordMethod km = null;

    DoubleArrayList locarray = null;

    DoubleArrayList titlearray = null;

    DoubleArrayList tfxidfarray = null;

    DoubleArrayList kmarray = null;

    DoubleArrayList finalarray = null;

    public CombinedMethod(PartOfSpeech p) {
        super(p);
        lm = new LocationMethod(p);
        tm = new TitleMethod(p);
        km = new KeywordMethod(p);

    }

    @Override
    public void setVectors(InputData input, SOMLibTemplateVector template) {
        super.setVectors(input, template);
        lm.setVectors(input, template);
        tm.setVectors(input, template);
        km.setVectors(input, template);
    }

    @Override
    public void setDocument(String filename, ArrayList<String> doc) {
        super.setDocument(filename, doc);
        lm.setDocument(filename, doc);
        tm.setDocument(filename, doc);
        km.setDocument(filename, doc);
    }

    /**
     * adds scores of alle implemented methods
     */
    public DoubleArrayList computeScores() {
        tfxidfarray = super.computeScores(Scorer.ALL);
        lm.computeScores();
        locarray = lm.getLocationScores();
        tm.computeScores();
        titlearray = tm.getTitleScores();
        kmarray = km.computeScores(Scorer.KEYWORD_BOTH);

        finalarray = new DoubleArrayList();
        for (int i = 0; i < this.tfxidfarray.size(); i++) {
            finalarray.add(tfxidfarray.get(i) + locarray.get(i) + titlearray.get(i) + kmarray.get(i));
        }

        return finalarray;
    }

}
