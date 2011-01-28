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
package at.tuwien.ifs.feature.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.comparables.InputDistance;

/**
 * Provides a simple similarity-retrieval on vector files, as stand-alone application.
 * 
 * @author Rudolf Mayer
 * @version $Id: SimilarityRetrieval.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class SimilarityRetrieval implements SOMToolboxApp {
    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputVectorFile(true),
            OptionFactory.getOptInputLabel(true), OptionFactory.getOptNumberNeighbours(false),
            OptionFactory.getOptMetric(false) };

    public static final String DESCRIPTION = "Performs similarity retrieval in a given database (vector file)";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Utils;

    public static void main(String[] args) throws SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);
        File inputVectorFile = config.getFile("inputVectorFile");
        String inputLabel = config.getString("inputLabel");
        String metricName = config.getString("metric");
        DistanceMetric metric = AbstractMetric.instantiateNice(metricName);

        int neighbours = config.getInt("numberNeighbours", 10);

        try {
            SOMLibSparseInputData data = new SOMLibSparseInputData(inputVectorFile.getAbsolutePath());
            int inputDatumIndex = data.getInputDatumIndex(inputLabel);
            if (inputDatumIndex == -1) {
                throw new IllegalArgumentException("Input with label '" + inputLabel + "' not found in vector file '"
                        + inputVectorFile + "'; possible labels are: " + StringUtils.toString(data.getLabels(), 15));
            }

            ArrayList<InputDistance> distances = data.getDistances(inputDatumIndex, metric);
            Collections.sort(distances);

            // if the input vector file contained fewer vectors than requested neighbours
            if (neighbours > distances.size()) {
                System.out.println("Requested " + neighbours + " similar vectors, but vector file contains only "
                        + distances.size() + " entries -> limiting result set size.");
                neighbours = distances.size();
            }

            String[] labels = new String[neighbours];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = distances.get(i).getInput().getLabel();
            }
            int longestStringLength = StringUtils.getLongestStringLength(labels);
            System.out.println("\n\nNearest neighbours to '" + inputLabel + "', using distance metric: " + metric);
            System.out.println("Rank\tInputName" + StringUtils.repeatString(longestStringLength - 9, " ")
                    + "\tDistance");
            for (int i = 0; i < neighbours; i++) {
                InputDistance inputDistance = distances.get(i);
                String label = inputDistance.getInput().getLabel();
                int spacing = longestStringLength - label.length();
                System.out.println(i + 1 + "\t" + label + StringUtils.repeatString(spacing, " ") + "\t"
                        + inputDistance.getDistance());
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage() + ". Aborting.");
            System.exit(-1);
        }
    }
}
