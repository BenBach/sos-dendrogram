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

import org.apache.commons.math.random.RandomDataImpl;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;

import at.tuwien.ifs.somtoolbox.apps.DataSetViewer;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;

public class DataGenerator {
    static private RandomDataImpl rand = new RandomDataImpl();

    public static void main(String[] args) throws IOException, SOMLibFileFormatException {
        double[][] dataDef1 = { { 2.0, 2.0 }, { 0.5, 0.5 } };
        double[][] dataDef2 = { { 0.0, 0.0 }, { 1, 1 } };
        double[][] dataDef3 = { { -3, 3 }, { 1.5, 1.5 } };
        double[][][] dataDef = { dataDef1, dataDef2, dataDef3 };
        int[] numberOfPoints = { 150, 100, 75 };
        int dimension = 2;

        int totalNumber = 0;
        for (int numberOfPoint : numberOfPoints) {
            totalNumber += numberOfPoint;
        }

        double[][][] data = new double[dataDef.length][][];
        String[] labels = new String[dataDef.length];
        String[][] dataNames = new String[dataDef.length][];

        InputDatum[] d = new InputDatum[totalNumber];

        int index = 0;
        for (int i = 0; i < dataDef.length; i++) {
            labels[i] = "Cluster_" + i;
            data[i] = new double[numberOfPoints[i]][];
            dataNames[i] = new String[numberOfPoints[i]];
            for (int j = 0; j < numberOfPoints[i]; j++) {
                dataNames[i][j] = labels[i] + "_" + (j + 1);
                d[index] = generatePoint(dataNames[i][j], dataDef[i][0], dataDef[i][1]);
                data[i][j] = d[index].getVector().toArray();
                index++;
            }
        }
        String basicFileName = args[0];
        SOMLibClassInformation classInfo = new SOMLibClassInformation(labels, dataNames);
        SOMLibSparseInputData inputData = new SOMLibSparseInputData(d, classInfo);
        SOMLibTemplateVector templateVector = new SOMLibTemplateVector(totalNumber, dimension);
        InputDataWriter.writeAsSOMLib(inputData, templateVector, classInfo, basicFileName);

        DataSetViewer viewer = new DataSetViewer(null, "Viewer", labels, data, null);
        viewer.setVisible(true);
        index++;
    }

    public static InputDatum[] generatePoints(String name, int num, double[] mean, double[] sigma) {
        InputDatum[] res = new InputDatum[num];
        for (int i = 0; i < num; i++) {
            res[i] = generatePoint(name + (i + 1), mean, sigma);
        }
        return res;
    }

    private static InputDatum generatePoint(String name, double[] mean, double[] sigma) {
        double[] values = new double[mean.length];
        for (int j = 0; j < mean.length; j++) {
            values[j] = rand.nextGaussian(mean[j], sigma[j]);
        }
        return new InputDatum(name, new DenseDoubleMatrix1D(values));
    }

}
