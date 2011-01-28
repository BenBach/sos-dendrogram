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
package at.tuwien.ifs.somtoolbox.apps.analysis;

import java.util.Arrays;

import org.apache.commons.math.stat.StatUtils;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.util.ElementCounter;
import at.tuwien.ifs.somtoolbox.util.InverseComparator;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * @author Rudolf Mayer
 * @version $Id: FeatureDistributionAnalysis.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class FeatureDistributionAnalysis {

    private static final String separator = "---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";

    public static void main(String[] args) throws SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptInputVectorFile(true),
                OptionFactory.getOptClassInformationFile(false));
        String inputVectorFileName = config.getString("inputVectorFile");
        String classInfoFileName = config.getString("classInformationFile");

        final int paddingLength = 9;

        SOMLibSparseInputData input = new SOMLibSparseInputData(inputVectorFileName);
        SOMLibClassInformation classInfo = new SOMLibClassInformation(classInfoFileName);
        input.setClassInfo(classInfo);
        String[] classNames = classInfo.getClassNames();

        double[][] means = new double[classNames.length][];
        double[][] variances = new double[classNames.length][input.dim()];
        double[] classVariances = new double[input.dim()];
        double[] totalVariances = new double[input.dim()];
        double[] aggMeans = new double[input.dim()];
        int[] occurrences = new int[input.dim()];

        final L2Metric metric = new L2Metric();
        input.initDistanceMatrix(metric);

        final double[][] data = input.getData();

        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            double[][] calssData = input.getData(className);
            means[i] = VectorTools.meanVector(calssData);
            for (int j = 0; j < means[i].length; j++) {
                if (means[i][j] > 0) {
                    occurrences[j]++;
                }
            }
            for (int j = 0; j < variances[i].length; j++) {
                variances[i][j] = StatUtils.variance(VectorTools.slice(calssData, j));
            }
        }
        for (int i = 0; i < classVariances.length; i++) {
            // System.out.println(Arrays.toString(VectorTools.slice(means, i)));
            classVariances[i] = StatUtils.variance(VectorTools.slice(means, i));
            aggMeans[i] = StatUtils.mean(VectorTools.slice(means, i));
            totalVariances[i] = StatUtils.variance(VectorTools.slice(data, i));
        }

        // output
        System.out.println("\n");
        String[] classNameDifferences = StringUtils.getDifferences(classNames);
        for (int i = 0; i < classNameDifferences.length; i++) {
            String classNameDifference = classNameDifferences[i];
            System.out.print(StringUtils.pad(StringUtils.formatEndMaxLengthEllipsis(classNameDifference, paddingLength,
                    ".."), paddingLength));
            System.out.print(StringUtils.pad("Var", paddingLength) + (i + 1 < classNameDifferences.length ? " | " : ""));
        }
        System.out.print("|| " + StringUtils.pad("Mean", paddingLength) + StringUtils.pad("Var", paddingLength)
                + StringUtils.pad("Var.Mean", paddingLength) + StringUtils.pad("#Occ", paddingLength));
        System.out.println("\n" + separator);
        for (int i = 0; i < means[0].length; i++) {
            for (int j = 0; j < means.length; j++) {
                System.out.print(StringUtils.pad(means[j][i], paddingLength));
                System.out.print(StringUtils.pad(variances[j][i], paddingLength) + (j + 1 < means.length ? " | " : ""));
            }
            System.out.print("|| " + StringUtils.pad(aggMeans[i], paddingLength));
            System.out.print(StringUtils.pad(classVariances[i], paddingLength));
            System.out.print(StringUtils.pad(totalVariances[i], paddingLength));
            System.out.print(StringUtils.pad(occurrences[i], paddingLength));
            System.out.println();
        }

        System.out.println(separator + "\n");
        System.out.println("Nearest neighbours");
        System.out.print(StringUtils.pad("Weight-Vec", 15));

        int paddingLength2 = paddingLength - 2;
        for (String classNameDifference : classNameDifferences) {
            System.out.print(StringUtils.pad(StringUtils.formatEndMaxLengthEllipsis(classNameDifference,
                    paddingLength2, ".."), paddingLength2));
            System.out.print(" | ");
        }
        System.out.print(StringUtils.pad("Purity", paddingLength2));
        System.out.print(StringUtils.pad("MapSize", paddingLength2));
        System.out.println(StringUtils.pad("# Neighb", paddingLength2));

        String[] differences = StringUtils.getDifferences(input.getLabels());
        for (int i = 0; i < input.numVectors(); i++) {
            int[] perClass = new int[classNames.length];
            InputDatum inputDatum = input.getInputDatum(i);
            int classIndex = classInfo.getClassIndexForInput(inputDatum.getLabel());

            int classMemberCount = classInfo.getNumberOfClassMembers(classIndex) - 1;
            int number = classMemberCount * 1;
            final InputDatum[] nearestN = input.getNearestN(i, metric, number);
            for (InputDatum neighbour : nearestN) {
                perClass[classInfo.getClassIndexForInput(neighbour.getLabel())]++;
            }
            System.out.print(StringUtils.pad(differences[i], 15));
            for (int index : perClass) {
                System.out.print(StringUtils.pad(index, paddingLength2) + " | ");
            }
            System.out.print(StringUtils.pad(StringUtils.format(perClass[classIndex] * 100.0 / classMemberCount, 2)
                    + "%", paddingLength2));
            System.out.print(StringUtils.pad(classInfo.getNumberOfClassMembers(classIndex), paddingLength2));
            System.out.println(number);
        }

        System.out.println(separator + "\n");
        System.out.println("Total features: " + input.dim());
        ElementCounter<Integer> counter = new ElementCounter<Integer>();
        for (int d : occurrences) {
            counter.incCount(d);
        }

        Integer[] keys = counter.keySet().toArray(new Integer[counter.size()]);
        Arrays.sort(keys, new InverseComparator<Integer>());
        for (Integer key : keys) {
            System.out.println(key + " times: " + counter.getCount(key));
        }

        System.out.println(separator + "\n");
        System.out.println("Co-occurence of terms with other classes ");
        int paddingLength3 = paddingLength + 4;
        System.out.print(StringUtils.pad("Class/Count", paddingLength3) + " | ");
        for (int i = 0; i < classNames.length; i++) {
            System.out.print(StringUtils.pad(i, paddingLength2));
        }
        System.out.print(StringUtils.pad(" | Total", paddingLength2));
        System.out.print(StringUtils.pad(" | Dim", paddingLength2));
        System.out.println();
        System.out.println(StringUtils.repeatString((classNames.length + 2) * paddingLength2 + paddingLength3, "-"));

        for (int i = 0; i < classNameDifferences.length; i++) {
            String classNameDifference = classNameDifferences[i];
            System.out.print(StringUtils.pad(classNameDifference, paddingLength3) + " | ");

            // check terms this class uses
            ElementCounter<Integer> counter2 = new ElementCounter<Integer>();
            for (int j = 0; j < means[i].length; j++) {
                if (means[i][j] > 0) {
                    // count how often these terms are used in other classes
                    int otherClassTerms = 0;
                    for (int k = 0; k < classNameDifferences.length; k++) {
                        if (k != i && means[k][j] > 0) {
                            otherClassTerms++;
                        }
                    }
                    counter2.incCount(otherClassTerms);
                }
            }
            // System.out.println(counter2.keySet());
            for (int j = 0; j < classNameDifferences.length; j++) {
                System.out.print(StringUtils.pad(counter2.getCount(j), paddingLength2));
            }
            System.out.print(StringUtils.pad(" | " + counter2.totalCount(), paddingLength2));
            System.out.print(StringUtils.pad(" | " + input.dim(), paddingLength2));
            System.out.println();
        }
    }
}
