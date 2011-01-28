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
package at.tuwien.ifs.somtoolbox.summarisation.parser;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import cern.colt.list.DoubleArrayList;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.summarisation.methods.CombinedMethod;
import at.tuwien.ifs.somtoolbox.summarisation.methods.KeywordMethod;
import at.tuwien.ifs.somtoolbox.summarisation.methods.LocationMethod;
import at.tuwien.ifs.somtoolbox.summarisation.methods.PartOfSpeech;
import at.tuwien.ifs.somtoolbox.summarisation.methods.TFxIDF;
import at.tuwien.ifs.somtoolbox.summarisation.methods.TitleMethod;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Julius Penaranda
 * @version $Id: Scorer.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class Scorer {
    public static final String ALL = "all";

    public static final String TFxIDF = "tfxidf";

    public static final String LOCATION = "location";

    public static final String TITLE_METHOD = "title-method";

    public static final String KEYWORD_BOTH = "keyword_both";

    public static final String KEYWORD_NOUN = "keyword_noun";

    public static final String KEYWORD_VERB = "keyword_verb";

    public static final String COMBINED = "combined";

    public static final String[] methods = { TFxIDF, KEYWORD_NOUN, KEYWORD_VERB, KEYWORD_BOTH, TITLE_METHOD, LOCATION,
            COMBINED };

    private TFxIDF tfxidf = null;

    private KeywordMethod key = null;

    private TitleMethod th = null;

    private LocationMethod lm = null;

    private CombinedMethod cm = null;

    private ArrayList<String>[] parsedDocuments = null;

    private ArrayList<String> filenames = null;

    private PartOfSpeech pos = null;

    private SentenceParser sParser = null;

    private Object[] itemNames = null;

    private String prefix = null;

    private InputData inputvectors = null;

    private SOMLibTemplateVector templatevectors = null;

    public Scorer(Object[] itemN, InputData input, SOMLibTemplateVector template) {
        this.pos = new PartOfSpeech();
        this.pos.readModel();
        this.itemNames = itemN;
        this.inputvectors = input;
        this.templatevectors = template;
    }

    public Scorer(String itemN, InputData input, SOMLibTemplateVector template) {
        this(new String[] { itemN }, input, template);
    }

    public void setFileNamePrefix(String fnprefix) {
        prefix = fnprefix;
    }

    public void parseDocuments() {
        sParser = new SentenceParser(itemNames);
        sParser.setFileNamePrefix(this.prefix);

        for (Object itemName : itemNames) {
            sParser.find_parse_Document((String) itemName);
        }

        setparsedDocuments(sParser.getParsedDocuments());
        setFileNames(sParser.getFileNames());
    }

    private void setparsedDocuments(ArrayList<String>[] pd) {
        this.parsedDocuments = pd;
    }

    public ArrayList<String>[] getParsedDocuments() {
        return this.parsedDocuments;
    }

    public ArrayList<String> getParsedDocument(int id) {
        return this.parsedDocuments[id];
    }

    public void setFileNames(ArrayList<String> fn) {
        filenames = fn;
    }

    public ArrayList<String> getFileNames() {
        return filenames;
    }

    public int getNumbOfSent(int i) {
        return this.parsedDocuments[i].size() - 1; // ignore title
    }

    public void setVectors(SOMLibSparseInputData input, SOMLibTemplateVector template) {
        this.inputvectors = input;
        this.templatevectors = template;
    }

    public DoubleArrayList computeScores(String algorithm, String filename, ArrayList<String> doc) {
        DoubleArrayList finalarray = null;

        // if TFxIDF
        if (algorithm.equals(TFxIDF)) {
            if (tfxidf == null) {
                tfxidf = new TFxIDF(this.pos);
                tfxidf.setVectors(this.inputvectors, this.templatevectors);
            }
            tfxidf.setDocument(filename, doc);
            finalarray = tfxidf.computeScores(ALL);
        }
        // if keyword
        else if (StringUtils.equalsAny(algorithm, KEYWORD_BOTH, KEYWORD_NOUN, KEYWORD_VERB)) {
            if (key == null) {
                key = new KeywordMethod(this.pos);
                key.setVectors(this.inputvectors, this.templatevectors);
            }
            key.setDocument(filename, doc);
            finalarray = key.computeScores(algorithm);
        }
        // if title-header
        else if (algorithm.equals(TITLE_METHOD)) {
            if (th == null) {
                th = new TitleMethod(this.pos);
                th.setVectors(this.inputvectors, this.templatevectors);
            }
            th.setDocument(filename, doc);
            finalarray = th.computeScores();
        }
        // if location
        else if (algorithm.equals(LOCATION)) {
            if (lm == null) {
                lm = new LocationMethod(this.pos);
                lm.setVectors(this.inputvectors, this.templatevectors);
            }
            lm.setDocument(filename, doc);
            finalarray = lm.computeScores();
        }
        // if combined
        else if (algorithm.equals(COMBINED)) {
            if (cm == null) {
                cm = new CombinedMethod(this.pos);
                cm.setVectors(this.inputvectors, this.templatevectors);
            }
            cm.setDocument(filename, doc);
            finalarray = cm.computeScores();
        }
        return finalarray;
    }

    /** * returns an array of scores of each sentence */
    public DoubleArrayList getScores(int docID, String algorithm) {
        if (ArrayUtils.contains(methods, algorithm)) {
            // FIXME: maybe implement some caching of these results ?
            return computeScores(algorithm, filenames.get(docID), parsedDocuments[docID]);
        } else {
            System.out.println("Scorer: getScores(): cannot identify type of algorithm");
            return null;
        }
    }

}
