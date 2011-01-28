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
package at.tuwien.ifs.somtoolbox.output.labeling;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Implements the <code>Keyword selection</code> labelling method, as described in <i><b>Lagus, K. and Kaski,
 * S.</b>:Keyword selection method for characterizing text document maps. Proceedings of ICANN99, 9th International
 * Conference on Artificial Neural Networks, volume 1, pages 371-376, IEEE, London. </i><br/>
 * This implementation is based on Lucene.<br/>
 * FIXME: still incomplete, based on old/deprecated Lucene API
 * 
 * @author Rudolf Mayer
 * @version $Id: LagusKeywordLabeler.java 3883 2010-11-02 17:13:23Z frank $
 */
public class LagusKeywordLabeler extends AbstractLabeler {
    String path;

    public static void main(String[] args) {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_LAGUS_KEYWORD_LABELER);

        int numLabels = config.getInt("numberLabels", 5);
        String inputVectorFilename = config.getString("inputVectorFile");
        boolean denseData = config.getBoolean("denseData", false);
        String templateVectorFilename = config.getString("templateVectorFile", null);
        String unitDescriptionFilename = config.getString("unitDescriptionFile", null);
        String weightVectorFilename = config.getString("weightVectorFile");
        String mapDescriptionFilename = config.getString("mapDescriptionFile", null);

        GrowingSOM gsom = null;
        try {
            gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFilename, unitDescriptionFilename,
                    mapDescriptionFilename));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            e.printStackTrace();
            System.exit(-1);
        }

        InputData data = InputDataFactory.open(inputVectorFilename, templateVectorFilename, !denseData, true, 1,
                7);
        Labeler labeler = new LagusKeywordLabeler(config.getString("inputDir"));
        labeler.label(gsom, data, numLabels);
    }

    public LagusKeywordLabeler(String path) {
        super();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        this.path = path;
    }

    @Override
    public void label(GHSOM ghsom, InputData data, int num) {
        label(ghsom.topLayerMap(), data, num);
    }

    @Override
    public void label(GrowingSOM gsom, InputData data, int num) {
        label(gsom, data, num, false);
    }

    @Override
    public void label(GrowingSOM gsom, InputData data, int num, boolean ignoreLabelsWithZero) {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_24);
        Unit[] units = gsom.getLayer().getAllUnits();

        try {
            IndexWriter fullIndex = new IndexWriter(FSDirectory.open(new File("index")), analyzer, true,
                    MaxFieldLength.UNLIMITED);
            IndexWriter[] unitIndices = new IndexWriter[units.length];

            for (int i = 0; i < units.length; i++) { // do labeling for each unit
                if (units[i].getNumberOfMappedInputs() != 0) {
                    InputDatum[] unitData = data.getInputDatum(units[i].getMappedInputNames());
                    String[] vectorNames = new String[unitData.length];
                    for (int j = 0; j < unitData.length; j++) {
                        vectorNames[j] = unitData[j].getLabel();
                    }
                    // Store the index in memory:
                    Directory directory = new RAMDirectory();
                    try {
                        unitIndices[i] = new IndexWriter(directory, analyzer, true, MaxFieldLength.UNLIMITED);
                        unitIndices[i].setMaxFieldLength(25000);
                        // FileIndexer indexer = new FileIndexer(unitIndices[i],
                        // (String[]) new ArrayList(FileIndexer.KNOWN_FILE_TYPES.keySet()).toArray(new
                        // String[FileIndexer.KNOWN_FILE_TYPES.size()]));
                        // indexer.indexDocs(path, vectorNames);
                        fullIndex.addIndexesNoOptimize(new Directory[] { unitIndices[i].getDirectory() });
                        IndexSearcher isearcher = new IndexSearcher(directory, true);

                        TermEnum terms = isearcher.getIndexReader().terms();
                        do {
                            System.out.println("Term: " + terms.term());
                        } while (terms.next());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
