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
package at.tuwien.ifs.somtoolbox.apps.initEval;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.layers.quality.TopographicError;

/**
 * @author Stefan Bischof
 * @author Leo Sklenitzka
 * @version $Id: EvaluationMain.java 3893 2010-11-03 13:57:47Z mayer $
 */
public class EvaluationMain {
    private static String DATASETNAME = "iris";

    private static String FILENAME = "evaluation." + DATASETNAME + ".csv";

    private static BufferedWriter out;

    private static String separator = ";";

    private static String propFilename = "data/" + DATASETNAME + "/" + DATASETNAME + ".prop";

    private static String[] args = new String[] { propFilename };

    private static int sleeptime = 5000;

    /**
     * How often should each initialization be run?
     */
    private static int numRuns = 5;

    private static void readParams(String[] args) {
        if (args.length != 1) {
            System.out.println("USAGE: somtoolbox EvaluationMain testsetname");
            System.exit(1);
        }

        DATASETNAME = args[0];
        FILENAME = "evaluation." + DATASETNAME + ".csv";
        propFilename = "data/" + DATASETNAME + "/" + DATASETNAME + ".prop";
        args = new String[] { propFilename };
    }

    public static void main(String[] args) throws IOException, InterruptedException, SOMLibFileFormatException {

        readParams(args);

        System.out.println("Testing: " + DATASETNAME.toUpperCase());
        Thread.sleep(sleeptime);

        out = new BufferedWriter(new FileWriter(FILENAME));

        out.write("Initialization Type");
        out.write(separator);
        out.write("Initialization Duration");
        out.write(separator);
        out.write("Training Duration");
        out.write(separator);
        out.write("mqe");
        out.write(separator);
        out.write("mmqe");
        out.write(separator);
        out.write("TE");
        out.write(separator);
        out.write("TE8");
        out.write("\n");

        for (int i = 0; i < numRuns; i++) {

            System.gc();
            Thread.sleep(sleeptime);

            evaluateRandomSampling();

            System.gc();
            Thread.sleep(sleeptime);

            evaluateRandom();

            System.gc();
            Thread.sleep(sleeptime);

            evaluateExistingSOM();

            System.gc();
            Thread.sleep(sleeptime);

            evaluatePCA();

            // evaluateManual();

        }

        out.close();

    }

    public static String getDatasetName() {
        return DATASETNAME;
    }

    private static void evaluateExistingSOM() throws IOException, InterruptedException {
        Properties props = loadProperties();
        int xsize = Integer.parseInt(props.getProperty("xSize"));
        int ysize = Integer.parseInt(props.getProperty("ySize"));

        // first train a SOM with a quarter of the size of the resulting SOM
        changeInitMethodAndSizes("Random", xsize / 2, ysize / 2);
        at.tuwien.ifs.somtoolbox.models.GrowingSOM.main(args);

        System.gc();
        Thread.sleep(sleeptime);

        // now train the "real" SOM initialized with the values of the previous SOM
        changeInitMethodAndSizes("SOM", xsize, ysize);

        at.tuwien.ifs.somtoolbox.models.GrowingSOM.main(args);
        evaluate("SOM");
    }

    private static void evaluatePCA() throws IOException, InterruptedException {
        evaluateGeneric("PCA");
    }

    private static void evaluateRandomSampling() throws IOException, InterruptedException {
        evaluateGeneric("RandomSampling");
    }

    private static void evaluateRandom() throws IOException, InterruptedException {
        evaluateGeneric("Random");
    }

    private static void evaluateGeneric(String name) throws FileNotFoundException, IOException, InterruptedException {
        changeInitMethod(name);

        at.tuwien.ifs.somtoolbox.models.GrowingSOM.main(args);
        evaluate(name);
    }

    private static void evaluate(String methodName) throws IOException {
        out.write(methodName);
        out.write(separator);
        out.write("\"" + Measure.getInitalizationDuration() + "\"");
        out.write(separator);
        out.write("\"" + Measure.getTrainDuration() + "\"");
        out.write(separator);

        QualityMeasure qm = Measure.getQualityMeasure();

        try {
            for (String key : qm.getMapQualityNames()) {
                out.write("\"" + qm.getMapQuality(key) + "\"");
                out.write(separator);
            }
        } catch (QualityMeasureNotFoundException e) {
            e.printStackTrace();
        }

        qm = new TopographicError(Measure.getLayer(), Measure.getInputData());

        try {
            for (String key : new String[] { "TE_Map", "TE8_Map" }) {
                out.write("\"" + qm.getMapQuality(key) + "\"");
                out.write(separator);
            }
        } catch (QualityMeasureNotFoundException e) {
            e.printStackTrace();
        }

        out.write("\n");
        Measure.reset();
    }

    private static Properties loadProperties() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileReader(propFilename));

        return props;
    }

    private static void changeInitMethod(String method) throws FileNotFoundException, IOException, InterruptedException {
        Properties props = loadProperties();
        props.setProperty("initMethod", method);
        props.store(new FileOutputStream(propFilename), "");
        Thread.sleep(2000);
    }

    private static void changeInitMethodAndSizes(String method, int xSize, int ySize) throws FileNotFoundException,
            IOException, InterruptedException {
        Properties props = loadProperties();
        props.setProperty("initMethod", method);
        props.setProperty("xSize", Integer.toString(xSize));
        props.setProperty("ySize", Integer.toString(ySize));
        props.store(new FileOutputStream(propFilename), "");
        Thread.sleep(2000);
    }
}
