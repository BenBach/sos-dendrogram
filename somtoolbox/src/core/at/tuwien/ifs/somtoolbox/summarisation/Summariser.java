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
package at.tuwien.ifs.somtoolbox.summarisation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import cern.colt.list.DoubleArrayList;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.summarisation.output.ResultHandler;
import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: Summariser.java 3832 2010-10-06 21:26:23Z mayer $
 */
public class Summariser {
    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputDirectory(true),
            OptionFactory.getOptDocument(), OptionFactory.getOptOutputDirectory(true),
            OptionFactory.getOptCompressionRate(), OptionFactory.getOptMethod(),
            OptionFactory.getOptTemplateVectorFile(true), OptionFactory.getOptInputVectorFile(true) };

    public static void main(String[] args) {
        buildResult(args);
    }

    private static void buildResult(String[] args) {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String document = config.getString("document");
        String input = config.getString("inputDir");
        String outputDirectory = config.getString("outputDirectory");
        int compression = config.getInt("compression");
        String method = config.getString("method");
        String inputvector = config.getString("input");
        String templatevector = config.getString("template");

        File output = new File(outputDirectory);
        if (!output.exists()) {
            output.mkdir();
            System.out.println("result folder created");
        }

        try {
            InputData inputvec = InputDataFactory.open(inputvector);
            SOMLibTemplateVector templatevec = new SOMLibTemplateVector(templatevector);
            Scorer scorer = new Scorer(document, inputvec, templatevec);
            scorer.setFileNamePrefix(input);
            scorer.parseDocuments();

            Object[] item = new Object[1];
            item[0] = document;
            ResultHandler resulth = new ResultHandler(item, scorer.getParsedDocuments());
            DoubleArrayList scoreArray = scorer.getScores(0, method);
            resulth.storeScore(0, scoreArray);
            resulth.createResult(0, compression);
            ArrayList<String> resultdoc = resulth.getResultDoc(0);

            File resultfile = new File(outputDirectory + File.separator + document);
            if (!resultfile.exists()) {
                resultfile.createNewFile();
            }
            BufferedWriter bufwriter = new BufferedWriter(new FileWriter(resultfile.getAbsolutePath()));
            bufwriter.write(scorer.getParsedDocument(0).get(0) + "\n");
            for (int i = 0; i < resultdoc.size(); i++) {
                bufwriter.write(resultdoc.get(i));
            }
            bufwriter.close();
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage() + " ");
            e.printStackTrace();
        }

    }

}
