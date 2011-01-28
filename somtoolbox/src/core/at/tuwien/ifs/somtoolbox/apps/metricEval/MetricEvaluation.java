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
package at.tuwien.ifs.somtoolbox.apps.metricEval;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L1Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.LInfinityMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.LnAlphaMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.LnMetric;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * @author Rudolf Mayer
 * @version $Id: MetricEvaluation.java 3832 2010-10-06 21:26:23Z mayer $
 */
public class MetricEvaluation {

    public static void main(String[] args) throws IOException, SOMToolboxException {
        String root = args[0] + File.separator;
        String collection = args[1];
        String[] featureMethods;
        String outputDir = "results";
        if (args.length > 2) {
            outputDir = args[2];
        }
        if (!outputDir.endsWith(File.separator)) {
            outputDir += File.separator;
        }

        new File(outputDir).mkdirs();

        if (args.length > 3) {
            featureMethods = new String[] { args[3] };
        } else {
            featureMethods = new String[] { "rh", "ssd", "rp" };
        }

        // Test parameters
        DistanceMetric[] metrics = new DistanceMetric[] { new L1Metric(), new LnMetric(1.5), new L2Metric(),
                new LnMetric(2.5), new LInfinityMetric() };
        int[] precisions = new int[] { 100, 50, 30, 20, 10, 5, 3, 1 };

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM, HH:mm:ss");

        for (String featureMethod : featureMethods) { // DIFFERENT FEATURES

            Date startDate = new Date();
            PrintWriter writer = new PrintWriter(new FileWriter(outputDir + collection + "-" + featureMethod
                    + "_results_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(startDate) + ".csv"));
            writer.println("Collection: " + collection);
            writer.println("Feature: " + featureMethod);
            writer.println("Started: " + startDate);
            String baseFileName = root + File.separator + collection + File.separator;
            baseFileName = root + File.separator + collection + File.separator;
            SOMLibClassInformation classInfo = new SOMLibClassInformation(baseFileName + collection + ".cls");
            String vectorBaseFileName = baseFileName + "vec" + File.separator + collection + ".";
            // DistanceMetric[] metrics = new DistanceMetric[];

            writer.print("\n\nFeatures,Metric,Alpha,");
            for (int k = 0; k < precisions.length; k++) {
                writer.print("Precision at " + precisions[k]);
                if (k + 1 < precisions.length) {
                    writer.print(",");
                }
            }
            writer.println("");

            // FIXME: remove dependency on AbstractSOMLibSparseInputData
            AbstractSOMLibSparseInputData data = (AbstractSOMLibSparseInputData) InputDataFactory.open(vectorBaseFileName
                    + featureMethod + ".norm.gz");
            System.out.println("\r**** " + simpleDateFormat.format(new Date()) + ": feature "
                    + featureMethod.toUpperCase() + " [" + data.dim() + " dims]"
                    + "                                      ");

            for (DistanceMetric metric : metrics) { // METRIC
                System.out.println("\r\t" + simpleDateFormat.format(new Date()) + ": " + metric
                        + "                                      ");
                // for (double alpha = 0.05; alpha < 1.05; alpha += 0.05) { // ALPHA Values
                for (int alphaValue = 1; alphaValue <= 20; alphaValue++) { // ALPHA Values
                    double alpha = alphaValue * 0.05;
                    System.out.println("\r\t\t" + simpleDateFormat.format(new Date()) + ": alpha: " + alpha
                            + "                                      ");
                    data.transformValues(new LnAlphaMetric(alpha, 1));
                    data.initDistanceMatrix(metric);
                    StdErrProgressWriter progress = new StdErrProgressWriter(data.numVectors(),
                            "\t\tCalculating precision for vector ");

                    double[] averagePrecisions = new double[precisions.length];
                    for (int i = 0; i < averagePrecisions.length; i++) {
                        averagePrecisions[i] = 0;
                    }

                    for (int vectorIndex = 0; vectorIndex < data.numVectors(); vectorIndex++) {
                        progress.progress(vectorIndex);
                        InputDatum input = data.getInputDatum(vectorIndex);
                        int classIndex = classInfo.getClassIndex(input.getLabel());
                        // classInfo.getNumberOfClassMembers(classIndex);
                        InputDatum[] matches = data.getNearestN(vectorIndex, metric, precisions[0]);

                        for (int precisionIndex = 0; precisionIndex < precisions.length; precisionIndex++) { // calucalte
                            // precision
                            int sameClassCount = 0;
                            for (int currentMatch = 0; currentMatch < precisions[precisionIndex]; currentMatch++) {
                                if (classInfo.getClassIndex(matches[currentMatch].getLabel()) == classIndex) {
                                    sameClassCount++;
                                }
                            }
                            double precision = (double) sameClassCount / (double) precisions[precisionIndex];
                            averagePrecisions[precisionIndex] += precision;
                        }
                    }
                    writer.print(featureMethod + "," + metric + "," + alpha + ",");
                    for (int i = 0; i < averagePrecisions.length; i++) { // caluclate average precision for each feature
                        averagePrecisions[i] = averagePrecisions[i] / data.numVectors();
                        writer.print(averagePrecisions[i]);
                        if (i + 1 < averagePrecisions.length) {
                            writer.print(",");
                        }
                    }
                    writer.println("");
                    writer.flush();
                }
                writer.println("\n");
            }
            writer.println("\n\n");
            Date endDate = new Date();
            double duration = endDate.getTime() - startDate.getTime() / 1000;
            String endMessage = "Finished: " + endDate + " (" + duration / (60 * 60) + " minutes)";
            System.out.println(endMessage);
            writer.println(endMessage);
            writer.close();
        }
    }

}
