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
package at.tuwien.ifs.somtoolbox.data.distance;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.input.SOMLibMapDescription;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * This implementation stores the distances in memory. It is constructed either by calculating distances from the given
 * input data on the fly, or by reading them from an ASCII file. If memory is an issue (consider using
 * {@link RandomAccessFileInputVectorDistanceMatrix} instead).
 * 
 * @author Rudolf Mayer
 * @version $Id: AbstractMemoryInputVectorDistanceMatrix.java 3706 2010-07-20 11:07:54Z mayer $
 */
public abstract class AbstractMemoryInputVectorDistanceMatrix extends InputVectorDistanceMatrix {
    public static final String FILE_TYPE = "Distance Matrix File";

    /**
     * Constructs the distance matrix by computing the distances on the fly. Not suited for large data sets, where the
     * computation time might take long (consider reading it from a file using
     * {@link #AbstractMemoryInputVectorDistanceMatrix(String)})
     */
    public AbstractMemoryInputVectorDistanceMatrix(InputData data, DistanceMetric metric) throws MetricException {
        numVectors = data.numVectors();
        inputLabels = new ArrayList<String>(numVectors);
        initStorage();
        StdErrProgressWriter progress = new StdErrProgressWriter(numVectors, "Calculating distance matrix: ",
                numVectors / 10);
        for (int i = 0; i < numVectors; i++) {
            InputDatum inputDatum = data.getInputDatum(i);
            for (int j = i + 1; j < numVectors; j++) {
                setValue(i, j, metric.distance(inputDatum, data.getInputDatum(j)));
            }
            inputLabels.add(inputDatum.getLabel());
            progress.progress(i);
        }
    }

    protected abstract void initStorage();

    protected abstract void setValue(int x, int y, double value);

    /** Reads the distance matrix from an ASCII file, and stores it in memory. */
    public AbstractMemoryInputVectorDistanceMatrix(String fileName) throws IOException, SOMToolboxException {
        BufferedReader br = FileUtils.openFile(FILE_TYPE, fileName);
        Map<String, String> headers = FileUtils.readSOMLibFileHeaders(br, "input vector distance metric");
        numVectors = Integer.parseInt(headers.get("$NUM_VECTORS"));
        metric = AbstractMetric.instantiateNice(headers.get(SOMLibMapDescription.METRIC));

        initStorage();
        String line = headers.get("FIRST_CONTENT_LINE");
        int lineNumber = 0;
        while (line != null) {
            line = line.trim();
            String[] distances = line.split(" ");
            if (distances.length != numVectors - (lineNumber + 1)) {
                throw new SOMToolboxException("Distance Matrix File corrupt in data line " + lineNumber + ", contains "
                        + distances.length + " instead of " + (numVectors - (lineNumber + 1)) + " expected elements!");
            }
            for (int i = 0; i < distances.length; i++) {
                if (distances[i].trim().length() == 0) {
                    System.out.println("empty element in " + lineNumber + ", " + i);
                }
                setValue(lineNumber, i + lineNumber + 1, Double.parseDouble(distances[i]));
            }
            line = br.readLine();
            lineNumber++;
        }
    }

}
