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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;
import at.tuwien.ifs.somtoolbox.util.ElementCounter;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * Merges two or more SOMLib Input files, i.e. vector and template files. Template vectors can be off different
 * dimensionality, and may contain different features, but some features may also be overlapping. Different merge
 * strategies are available: union of all features sets, intersection of features sets, and strategies in between,
 * retaining a feature if it appears in at least x sets. This class is a bit similar to {@link VectorFileMerger}, but
 * more generic in the number of inputs it can take, and in the merging strategies, though it doesn't support the
 * weighting strategies implemented in {@link VectorFileMerger}.
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMLibInputMerger.java 3956 2010-11-29 15:33:45Z frank $
 */
// FIXME: merge this with VectorFileMerger at some point
public class SOMLibInputMerger implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptMergeMode(),
            OptionFactory.getOptInputDirectory(false), OptionFactory.getOptOutputFileName(true),
            OptionFactory.getOptSOMLibInputs(false) };

    public static final String DESCRIPTION = "Merges two or more SOMLib Input files, i.e. vector and template files";

    public static final String LONG_DESCRIPTION = DESCRIPTION
            + ". Template vectors can be off different dimensionality, and may contain different features, but some features may also be overlapping."
            + " Different merge strategies are available: union of all features sets, intersection of features sets, and strategies in between, retaining a feature if it appears in at least x sets";

    public static final Type APPLICATION_TYPE = Type.Helper;

    enum mergeMode {
        Union, Intersection, MinOccurence, All
    }

    public static void main(String[] args) throws IOException, SOMLibFileFormatException {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String[] inputTvs = FileUtils.findAllSOMLibFiles(config, "inputs", "inputDir", ".tv", ".vec");
        String[] inputVecs = inputTvs;
        mergeVectors(inputTvs, inputVecs, config.getFile("output").getAbsolutePath(), config.getString("mode"), null);
    }

    public static void mergeVectors(final String[] inputTvs, final String[] inputVecs, String outFile,
            String modeString, int[] size) throws IOException, SOMLibFileFormatException {
        int percentage = -1;
        mergeMode mode = mergeMode.All;
        if (mode != null) {
            if (modeString.equalsIgnoreCase(mergeMode.Intersection.toString())) {
                mode = mergeMode.Intersection;
            } else if (modeString.equalsIgnoreCase(mergeMode.Union.toString())) {
                mode = mergeMode.Union;
            } else if (modeString.equalsIgnoreCase(mergeMode.All.toString())) {
            } else {
                try {
                    percentage = Integer.parseInt(modeString);
                    mode = mergeMode.MinOccurence;
                } catch (NumberFormatException e) {
                    System.out.println("Illegal mode '" + modeString + "'. Aborting");
                    throw new IllegalArgumentException("Illegal mode '" + modeString + "'.");
                }
            }
        }
        if (mode == mergeMode.All) {
            merge(inputTvs, inputVecs, outFile + mergeMode.Union, percentage, mergeMode.Union, size);
            for (int i = 2; i < inputVecs.length; i++) {
                merge(inputTvs, inputVecs, outFile + mergeMode.MinOccurence + i, i, mergeMode.MinOccurence, size);
            }
            merge(inputTvs, inputVecs, outFile + mergeMode.Intersection, percentage, mergeMode.Intersection, size);
        } else {
            merge(inputTvs, inputVecs, outFile, percentage, mode, size);
        }
    }

    private static void merge(final String[] inputTvs, final String[] inputVecs, String outFile, int percentage,
            mergeMode mode, int[] size) throws IOException, SOMLibFileFormatException {
        System.out.println("\n\nStarting feature merging, mode: " + mode
                + (mode == mergeMode.MinOccurence ? ", min occurrence: " + percentage : "") + "\n\n");

        if (outFile.contains(File.separator)) {
            // create output dir
            new File(outFile).getParentFile().mkdirs();
        }

        TemplateVector[] tvs = new TemplateVector[inputTvs.length];

        int totalFeatureCount = 0;
        int totalVectorCount = 0;

        // process template vectors
        Collection<String> mergedFeatures = new LinkedHashSet<String>();
        LinkedHashSet<String> allFeatures = new LinkedHashSet<String>();
        ElementCounter<String> counter = new ElementCounter<String>();

        for (int i = 0; i < inputTvs.length; i++) {
            System.out.println("processing: " + inputTvs[i]);
            if (!FileUtils.extractSOMLibDataPrefix(inputTvs[i]).equals(inputTvs[i])) {
                inputTvs[i] = FileUtils.extractSOMLibDataPrefix(inputTvs[i]);
                System.out.println("\t=> extracted prefix ");
            }
            System.out.println("processing: " + inputTvs[i] + ".tv");
            tvs[i] = new SOMLibTemplateVector(inputTvs[i] + ".tv");
            totalVectorCount += tvs[i].numVectors();
            final ArrayList<String> labels = tvs[i].getLabelsAsList();
            if (mode == mergeMode.Intersection) {
                if (i == 0) {// in the first iteration of an intersection, we have to add all features
                    mergedFeatures.addAll(labels);
                }
                // System.out.println("\n\n\nintersection mode, retaining");
                mergedFeatures.retainAll(labels);
            } else if (mode == mergeMode.Union) {
                // System.out.println("\n\n\nunion mode, adding");
                mergedFeatures.addAll(labels);
            } else {
                for (String label : labels) {
                    counter.incCount(label);
                }
            }
            // System.out.println("\n\nSize of mergedFeatures in iteration " + i + ": " + mergedFeatures.size());
            allFeatures.addAll(labels);
            totalFeatureCount += labels.size();
        }

        if (mode == mergeMode.MinOccurence) {
            mergedFeatures.addAll(counter.keyList(percentage));
        }
        mergedFeatures = new ArrayList<String>(mergedFeatures);
        Collections.sort((ArrayList<String>) mergedFeatures);

        System.out.println("\n\n");
        System.out.println("===================================================");
        System.out.println("feature stats:");
        System.out.println("Total features in TemplateVectors: " + totalFeatureCount);
        System.out.println("Unique features: " + allFeatures.size());
        System.out.println("Merged features: " + mergedFeatures.size());
        System.out.println("Number of vectors: " + totalVectorCount);
        System.out.println();
        System.out.println("===================================================");

        // If we merge files where the TVs don't have the same number of vectors as the input vectors,
        // e.g. if those were generated from transforming a SOM's weight-vectors to an input vector file we need to
        // re-calculate the number of vectors
        totalVectorCount = 0;
        for (String inputVec : inputVecs) {
            final Map<String, String> headers = FileUtils.readSOMLibFileHeaders(
                    FileUtils.openFile("Input Vector File", inputVec + InputData.inputFileNameSuffix), "input vector");
            totalVectorCount += Integer.parseInt(headers.get("$XDIM"));
        }

        TemplateVector mergedTV = new SOMLibTemplateVector(0, mergedFeatures.toArray(new String[mergedFeatures.size()]));
        for (TemplateVector templateVector : tvs) {
            for (int k = 0; k < templateVector.dim(); k++) {
                if (mergedTV.containsLabel(templateVector.getLabel(k))) {
                    final int index = mergedTV.getIndexOfFeature(templateVector.getLabel(k));
                    mergedTV.getElement(index).mergeStatiscticsWithOtherElement(templateVector.getElement(k));
                    mergedTV.incNumVectors(templateVector.numVectors());
                }
            }
        }
        // with the correct term statistics, we can write the template vector file
        InputDataWriter.writeAsSOMLib(mergedTV, outFile + ".tv");

        // start writing the vector, first open a class-info file to add vectors too later
        SOMLibClassInformation classInfo = new SOMLibClassInformation();

        PrintWriter writer = FileUtils.openFileForWriting("Input vector file", outFile + ".vec", true);
        InputDataWriter.writeHeaderToFile(writer, totalVectorCount, mergedFeatures.size());

        for (int i = 0; i < inputVecs.length; i++) {
            System.out.println("processing: " + inputVecs[i] + InputData.inputFileNameSuffix);
            final InputData inputData = new SOMLibSparseInputData(inputVecs[i] + InputData.inputFileNameSuffix);
            inputData.setTemplateVector(tvs[i]);
            final TemplateVector templateVector = tvs[i];
            final String classname = inputVecs[i];
            for (int j = 0; j < inputData.numVectors(); j++) {
                final InputDatum inputDatum = inputData.getInputDatum(j);
                final DoubleMatrix1D originalVector = inputDatum.getVector();
                SparseDoubleMatrix1D newVector = new SparseDoubleMatrix1D(mergedTV.dim());
                boolean[] presentTerms = VectorTools.createBooleanArray(mergedTV.dim(), false);
                for (int k = 0; k < templateVector.dim(); k++) {
                    if (mergedTV.containsLabel(templateVector.getLabel(k))) {
                        final int index = mergedTV.getIndexOfFeature(templateVector.getLabel(k));
                        newVector.setQuick(index, originalVector.getQuick(k));
                        presentTerms[index] = true;
                    }
                }
                for (int k = 0; k < presentTerms.length; k++) {
                    if (!presentTerms[k]) {
                        newVector.setQuick(k, Double.NaN);
                    }
                }
                // newVector = VectorTools.normaliseByLength(newVector.toArray());
                InputDataWriter.writeInputDatumToFile(writer, inputDatum.getLabel(), newVector);
                writer.flush();
                classInfo.addItem(inputDatum.getLabel(), classname);
            }
        }
        writer.close();

        // now we can write the class-info file too (vectors got still added in the loop)
        classInfo.processItems(true);
        InputDataWriter.writeAsSOMLib(classInfo, outFile + ".cls");

        // finally, write a properties file
        if (size == null) {
            size = VectorTools.computeDefaultSize(totalVectorCount, 7);
        }
        try {
            new SOMProperties(size[0], size[1], SOMLibSparseInputData.DEFAULT_RANDOM_SEED, 20, 0,
                    SOMProperties.defaultLearnRate, -1d, 1d, "MissingValueMetricWrapper", false).writeToFile(outFile,
                    ".", true);
        } catch (PropertiesException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
