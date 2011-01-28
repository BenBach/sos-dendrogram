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
package at.tuwien.ifs.somtoolbox.data.normalisation;

import java.io.IOException;

import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Standard score nomalisation, normalises the attributes to have zero mean and the standard deviation as max values,
 * i.e. z = (x - x_mean) / standardDevition.<br>
 * FIXME: the computation gives slightly different results than trying in Excel...
 * 
 * @author Rudolf Mayer
 * @version $Id: StandardScoreNormaliser.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class StandardScoreNormaliser extends AbstractNormaliser {
    double[] sums;

    double[] means;

    double[] standardDeviation;

    @Override
    public void preReading() {
        sums = new double[dim];
    }

    @Override
    public void postReading() throws IOException {
        means = new double[dim];
        standardDeviation = new double[dim];
        for (int i = 0; i < sums.length; i++) {
            means[i] = sums[i] / numVectors;
        }
        for (int i = 0; i < data.rows(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                double v = data.getQuick(i, j);
                standardDeviation[j] += (v - means[j]) * (v - means[j]);
            }
        }
        for (int i = 0; i < standardDeviation.length; i++) {
            standardDeviation[i] = Math.sqrt(standardDeviation[i] / numVectors);
        }
        for (int i = 0; i < data.rows(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                double v = data.getQuick(i, j);
                v = (v - means[j]) / standardDeviation[j];
                if (v == 0) {
                    writer.write("0 ");
                } else {
                    writer.write(StringUtils.format(v, 15) + " ");
                }
            }
            writer.write(dataNames[i]);
            writer.newLine();
        }
    }

    @Override
    protected void processLine(int index, String[] lineElements) throws Exception {
        for (int ve = 0; ve < dim; ve++) {
            double value = Double.parseDouble(lineElements[ve]);
            setMatrixValue(index, ve, value);
            sums[ve] += value;
        }
        addInstance(index, lineElements[dim]);
    }
}
