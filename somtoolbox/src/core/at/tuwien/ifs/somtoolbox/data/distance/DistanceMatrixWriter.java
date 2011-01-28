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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibMapDescription;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Writes the input matrix for a given data set to an ASCII or binary format. A distance matrix is of size
 * <code>n*n</code>, where <code>n</code> is the number of elements. The matrix is symmetric, i.e. the upper-right and
 * lower-left halves contain the same values. The diagonal contains the distances of one element to itself, and is thus
 * always 0. <br/>
 * Supported formats are:
 * <ul>
 * <li>Plain ASCII, containing all values in the matrix</li>
 * <li>SOMlib ASCII, containing just the upper-right half of the symmetric matrix, skipping the diagonal (can be read by
 * {@link AbstractMemoryInputVectorDistanceMatrix})</li>
 * <li>Orange (http://www.ailab.si/orange/) ASCII, containing the diagonal and the lower-left half</li>
 * <li>Binary, storing the same information as SOMLib ASCII, but in a binary format for random access (can be read by
 * {@link RandomAccessFileInputVectorDistanceMatrix})</li>
 * </ul>
 * 
 * @author Rudolf Mayer
 * @version $Id: DistanceMatrixWriter.java 3869 2010-10-21 15:56:09Z mayer $
 */
public class DistanceMatrixWriter implements SOMToolboxApp {

    private static final FlaggedOption OPT_CLASS_INFORMATION_FILE = OptionFactory.getOptClassInformationFile(false);

    /** Format for the Orange data mining tool (http://www.ailab.si/orange/) */
    private static final String ORANGE = "Orange";

    /** Binary format (for {@link RandomAccessFile} */
    private static final String BINARY = "Binary";

    /** SOMLib format (half-matrix) */
    private static final String SOM_LIB = "SOMLib";

    private static final String PLAIN = "plain";

    public static String[] OUTPUT_FORMATS = { SOM_LIB, PLAIN, BINARY, ORANGE };

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static final String DESCRIPTION = "Writes a distance matrix for the given data";

    public static final String LONG_DESCRIPTION = "Writes a distance matrix for the given data, in ASCII or binary format";;

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputVectorFile(true),
            OPT_CLASS_INFORMATION_FILE, OptionFactory.getOptMetric(false), OptionFactory.getOptMetricParams(false),
            OptionFactory.getOptOutputFileName(true),
            OptionFactory.getOptOutputFormat(false, SOM_LIB, DistanceMatrixWriter.OUTPUT_FORMATS) };

    public static void main(String[] args) throws SOMToolboxException, IOException {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String inputVectorFileName = OptionFactory.getFilePath(config, "inputVectorFile");
        String classInfoFile = OptionFactory.getFilePath(config, "classInformationFile");

        String outputFileName = OptionFactory.getFilePath(config, "output");

        String metricName = config.getString("metric");
        String metricParams = config.getString("metricParams");
        DistanceMetric metric = AbstractMetric.instantiateNice(metricName);
        metric.setMetricParams(metricParams);

        String outputFormat = config.getString("outputFormat");

        InputData data = new SOMLibSparseInputData(inputVectorFileName, null, classInfoFile);

        if (outputFormat.equals(SOM_LIB)) {
            writeSOMLibFileInputVectorDistanceMatrix(data, outputFileName, metric);
        } else if (outputFormat.equals(ORANGE)) {
            if (classInfoFile == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "No class information present - writing Orange Distance Matrix files with input labels");
            }
            writeOrangeFileInputVectorDistanceMatrix(data, outputFileName, metric);
        } else if (outputFormat.equals(PLAIN)) {// full format, no headers
            writePlainFileInputVectorDistanceMatrix(data, outputFileName, metric);
        } else { // binary
            writeRandomAccessFileInputVectorDistanceMatrix(data, outputFileName, metric);
        }
    }

    /** Write input distance matrix to ASCII file, computing distances on the fly. */
    public static void writeSOMLibFileInputVectorDistanceMatrix(InputData data, String fileName, DistanceMetric metric)
            throws IOException, MetricException {
        writeSOMLibFileInputVectorDistanceMatrix(data, fileName, metric, true);
    }

    /** Write input distance matrix to ASCII file, computing distances on the fly. */
    public static void writeSOMLibFileInputVectorDistanceMatrix(InputData data, String fileName, DistanceMetric metric,
            boolean gzip) throws IOException, MetricException {
        int numVec = data.numVectors();
        PrintWriter out = printSOMLibHeader(numVec, fileName, metric, gzip);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Storing input distance matrix with metric " + metric + " to ASCII file " + fileName);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVec, "Calculating distances for vector ", 1);
        for (int i = 0; i < numVec; i++) {
            for (int j = i + 1; j < numVec; j++) {
                out.print(metric.distance(data.getInputDatum(i), data.getInputDatum(j)));
                if (j + 1 < numVec) {
                    out.print(" ");
                }
            }
            if (i + 1 < numVec) { // only print newline if lines are not empty
                out.println();
            }
            progress.progress();
        }
        out.flush();
        out.close();
    }

    /** Write input distance matrix to an ASCII file in plain format, computing distances on the fly. */
    public static void writePlainFileInputVectorDistanceMatrix(InputData data, String fileName, DistanceMetric metric)
            throws IOException, MetricException {
        int numVec = data.numVectors();
        PrintWriter out = FileUtils.openFileForWriting(AbstractMemoryInputVectorDistanceMatrix.FILE_TYPE, fileName);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Storing input distance matrix with metric " + metric + " to plain distance file " + fileName);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVec, "Calculating distances for vector ", 1);
        for (int i = 0; i < numVec; i++) {
            InputDatum datum = data.getInputDatum(i);
            for (int j = 0; j < numVec; j++) {
                if (i == j) {
                    out.print("0");
                } else {
                    out.print(StringUtils.format(metric.distance(datum, data.getInputDatum(j)), 8));
                }
                if (j + 1 < numVec) {
                    out.print(" ");
                }
            }
            out.println();
            progress.progress();
        }
        out.flush();
        out.close();
    }

    /**
     * Write input distance matrix to an ASCII file for the Orange data mining toolkit ((http://www.ailab.si/orange/),
     * computing distances on the fly.
     */
    public static void writeOrangeFileInputVectorDistanceMatrix(InputData data, String fileName, DistanceMetric metric)
            throws IOException, MetricException {
        int numVec = data.numVectors();
        PrintWriter out = FileUtils.openFileForWriting(AbstractMemoryInputVectorDistanceMatrix.FILE_TYPE, fileName);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Storing input distance matrix with metric " + metric + " to Orange distance file " + fileName);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVec, "Calculating distances for vector ", 1);
        out.println(data.numVectors() + " labeled");
        for (int i = 0; i < numVec; i++) {
            InputDatum datum = data.getInputDatum(i);
            if (data.classInformation() != null) {
                out.print(data.classInformation().getClassName(i));
            } else {
                out.print(data.getLabel(i));
            }
            out.print("\t");
            for (int j = 0; j < i; j++) {
                out.print(StringUtils.format(metric.distance(datum, data.getInputDatum(j)), 8));
                if (j + 1 < numVec) {
                    out.print("\t");
                }
            }
            out.print("0.000000\n");
            progress.progress();
        }
        out.flush();
        out.close();
    }

    /** Write pre-calculated input distance matrix to an ASCII file in SOMLib format. */
    public static void writeSOMLibFileInputVectorDistanceMatrix(double[][] distances, String fileName,
            DistanceMetric metric, boolean gzip) throws IOException, MetricException {
        int numVec = distances[0].length;
        PrintWriter out = printSOMLibHeader(distances.length, fileName, metric, gzip);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Storing pre-calculated input distance matrix with metric " + metric + " to ASCII file " + fileName);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVec, "Calculating distances for vector ", 1);
        for (int i = 0; i < numVec; i++) {
            for (int j = i + 1; j < distances[i].length; j++) {
                out.print(distances[i][j]);
                if (j + 1 < distances[i].length) {
                    out.print(" ");
                }
            }
            if (i + 1 < distances[i].length) { // only print newline if lines are not empty
                out.println();
            }
            progress.progress();
        }
        out.flush();
        out.close();
    }

    private static PrintWriter printSOMLibHeader(int numVectors, String fileName, DistanceMetric metric, boolean gzip)
            throws IOException {
        final PrintWriter out = FileUtils.openFileForWriting(AbstractMemoryInputVectorDistanceMatrix.FILE_TYPE,
                fileName, gzip);
        out.println("$NUM_VECTORS " + numVectors);
        out.println(SOMLibMapDescription.METRIC + " " + metric.getClass().getCanonicalName());
        return out;
    }

    /** Write input distance matrix to a binary file, computing distances on the fly. */
    public static void writeRandomAccessFileInputVectorDistanceMatrix(InputData data, String fileName,
            DistanceMetric metric) throws IOException, MetricException {
        int numVec = data.numVectors();
        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        file.writeInt(numVec);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Storing input distance matrix with metric " + metric + " to BINARY file " + fileName);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVec, "Calculating distances for vector ", 1);
        for (int i = 0; i < numVec; i++) {
            for (int j = i + 1; j < numVec; j++) {
                file.writeDouble(metric.distance(data.getInputDatum(i), data.getInputDatum(j)));
            }
            progress.progress();
        }
        file.writeChars(metric.getClass().getCanonicalName().trim());
        file.close();
    }

    /** Write pre-calculated input distance matrix to a binary file. */
    public static void writeRandomAccessFileInputVectorDistanceMatrix(double[][] distances, String fileName,
            DistanceMetric metric) throws IOException, MetricException {
        int numVec = distances[0].length;
        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Storing pre-calculated input distance matrix with metric " + metric + " to file " + fileName);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVec, "Calculating distances for vector ", 1);
        file.writeInt(numVec);
        for (int i = 0; i < numVec; i++) {
            for (int j = i + 1; j < distances[j].length; j++) {
                file.writeDouble(distances[i][j]);
            }
            progress.progress();
        }
        file.writeChars(metric.getClass().getCanonicalName().trim());
        file.close();
    }
}
