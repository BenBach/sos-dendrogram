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
package at.tuwien.ifs.somtoolbox.apps.helper;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang.StringUtils;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.distance.InputVectorDistanceMatrix;
import at.tuwien.ifs.somtoolbox.data.distance.LeightWeightMemoryInputVectorDistanceMatrix;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * Writes the nearest/most similar vectors for a given data set.
 * 
 * @author Rudolf Mayer
 * @version $Id: VectorSimilarityWriter.java 3704 2010-07-20 10:42:42Z mayer $
 */
public class VectorSimilarityWriter {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IOException, SOMToolboxException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_INPUT_SIMILARITY_COMPUTER);
        String inputVectorDistanceMatrix = config.getString("inputVectorDistanceMatrix");
        String inputVectorFileName = config.getString("inputVectorFile");
        int numNeighbours = config.getInt("numberNeighbours");
        String outputFormat = config.getString("outputFormat");

        InputVectorDistanceMatrix matrix = null;
        InputData data = new SOMLibSparseInputData(inputVectorFileName);

        if (StringUtils.isNotBlank(inputVectorDistanceMatrix)) {
            matrix = InputVectorDistanceMatrix.initFromFile(inputVectorDistanceMatrix);
        } else {
            String metricName = config.getString("metric");
            DistanceMetric metric = AbstractMetric.instantiate(metricName);
            matrix = new LeightWeightMemoryInputVectorDistanceMatrix(data, metric);
        }

        String outputFileName = config.getString("output");
        PrintWriter w = FileUtils.openFileForWriting("Similarity File", outputFileName);

        if (outputFormat.equals("SAT-DB")) {
            // find feature type
            String type = "";
            if (inputVectorFileName.endsWith(".rh") || inputVectorFileName.endsWith(".rp")
                    || inputVectorFileName.endsWith(".ssd")) {
                type = "_" + inputVectorFileName.substring(inputVectorFileName.lastIndexOf(".") + 1);
            }
            w.println("INSERT INTO `sat_track_similarity_ifs" + type
                    + "` (`TRACKID`, `SIMILARITYCOUNT`, `SIMILARITYIDS`) VALUES ");
        }

        int numVectors = matrix.numVectors();
        // numVectors = 10; // for testing
        StdErrProgressWriter progress = new StdErrProgressWriter(numVectors, "Writing similarities for vector ", 1);
        for (int i = 0; i < numVectors; i++) {
            int[] nearest = matrix.getNNearest(i, numNeighbours);
            if (outputFormat.equals("SAT-DB")) {
                w.print("  (" + i + " , NULL, '");
                for (int j = 0; j < nearest.length; j++) {
                    String label = data.getLabel(nearest[j]);
                    w.print(label.replace(".mp3", "")); // strip ending
                    if (j + 1 < nearest.length) {
                        w.print(",");
                    } else {
                        w.print("')");
                    }
                }
                if (i + 1 < numVectors) {
                    w.print(",");
                }
            } else {
                w.print(data.getLabel(i) + ",");
                for (int j = 0; j < nearest.length; j++) {
                    w.print(data.getLabel(nearest[j]));
                    if (j + 1 < nearest.length) {
                        w.print(",");
                    }
                }
            }
            w.println();
            w.flush();
            progress.progress();
        }
        if (outputFormat.equals("SAT-DB")) {
            w.print(";");
        }
        w.flush();
        w.close();
    }
}
