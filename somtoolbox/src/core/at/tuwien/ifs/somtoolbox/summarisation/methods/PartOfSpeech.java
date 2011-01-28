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

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Streams;

/**
 * @author Julius Penaranda
 * @version $Id: PartOfSpeech.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class PartOfSpeech {
    public static final String NOUN = "noun";

    public static final String ADJECTIVE = "adjective";

    public static final String VERB = "verb";

    private HmmDecoder decoder;

    private final String modelpath = "./src/core/rsc/partOfSpeech/medpost.model.gz";

    private TokenizerFactory TOKENIZER_FACTORY = new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");

    public PartOfSpeech() {
    }

    /**
     * reads Model
     */
    public void readModel() {
        try {
            File model = new File(modelpath);
            System.out.println("Reading model from file=" + model.toString());
            GZIPInputStream fileIn = new GZIPInputStream(new FileInputStream(model));
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
            Streams.closeInputStream(objIn);
            decoder = new HmmDecoder(hmm);

        } catch (Exception e) {
            System.out.println("PartofSpeech_Feature: readModel(): " + e.getMessage());
        }
    }

    /** returns type of tokens in the given line sentence, whether nouns, verbs... */
    public ArrayList<String> getTokens(String line, String type) {
        Tokenizer tokenizer;
        String[] tokens;
        String[] tags;
        ArrayList<String> resultarray = new ArrayList<String>();

        try {
            char[] cs = line.toCharArray();
            tokenizer = TOKENIZER_FACTORY.tokenizer(cs, 0, cs.length);
            tokens = tokenizer.tokenize();
            tags = decoder.firstBest(tokens);

            for (int i = 0; i < tokens.length; ++i) {
                // FIXME: refactor this!
                // # of nouns
                if (type == NOUN) {
                    if (tags[i].equals("NN") || tags[i].equals("NNP") || tags[i].equals("NNS")) {
                        // System.out.println("token: "+tokens[i]+" tag: "+tags[i]);
                        resultarray.add(tokens[i]);
                    }
                }

                // # of adjectives
                if (type == ADJECTIVE) {
                    if (tags[i].equals("JJ") || tags[i].equals("JJR") || tags[i].equals("JJT")) {
                        resultarray.add(tokens[i]);
                    }
                }

                // # of verbs
                if (type == VERB) {
                    if (tags[i].equals("VVB") || tags[i].equals("VVD") || tags[i].equals("VVG")
                            || tags[i].equals("VVI") || tags[i].equals("VVN") || tags[i].equals("VVNJ")
                            || tags[i].equals("VVGJ") || tags[i].equals("VVGN") || tags[i].equals("VVZ")) {
                        resultarray.add(tokens[i]);
                    }
                }

                // # of rel. pronouns
                if (tags[i].equals("PNR")) {
                    // do nothing
                }

                // # of prepositions
                if (tags[i].equals("II")) {
                    // do nothing
                }

                // # of adverbs
                if (tags[i].equals("RR") || tags[i].equals("RRR") || tags[i].equals("RRT")) {
                    // do nothing
                }

                // # of articles
                if (tokens[i].equals("the") || tokens[i].equals("The") || tokens[i].equals("THE")
                        || tokens[i].equals("a") || tokens[i].equals("A") || tokens[i].equals("an")
                        || tokens[i].equals("An") || tokens[i].equals("AN")) {
                    // do nothing
                }

                // # of pronouns
                if (tags[i].equals("PN") || tags[i].equals("PND") || tags[i].equals("PNG")) {
                    // do nothing
                }

                // # of modals
                if (tags[i].equals("VM") || tags[i].equals("VBB") || tags[i].equals("VBD") || tags[i].equals("VBG")
                        || tags[i].equals("VBI") || tags[i].equals("VBN") || tags[i].equals("VBZ")
                        || tags[i].equals("VDB") || tags[i].equals("VDD") || tags[i].equals("VDG")
                        || tags[i].equals("VDI") || tags[i].equals("VDN") || tags[i].equals("VDZ")
                        || tags[i].equals("VHB") || tags[i].equals("VHD") || tags[i].equals("VHG")
                        || tags[i].equals("VHI") || tags[i].equals("VHN") || tags[i].equals("VHZ")) {
                    // do nothing
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occured in LingPipe!");
        }
        return resultarray;
    }

}
