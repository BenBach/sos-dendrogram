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
 * Min-max normalisation, normalises the attributes between 0 and 1.
 * 
 * @author Rudolf Mayer
 * @version $Id: MinMaxNormaliser.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class MinMaxNormaliser extends AbstractNormaliser {

    double[] minValues;

    double[] maxValues;

    @Override
    public void preReading() {
        // in min-max data structure
        minValues = new double[dim];
        maxValues = new double[dim];
        for (int i = 0; i < minValues.length; i++) {
            minValues[i] = Double.MAX_VALUE;
            maxValues[i] = Double.MIN_VALUE;
        }
    }

    @Override
    public void postReading() throws IOException {
        // normalise and write the data
        for (int i = 0; i < data.rows(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                double v = data.getQuick(i, j);
                v = (v - minValues[j]) / (maxValues[j] - minValues[j]);
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
            if (value < minValues[ve]) {
                minValues[ve] = value;
            }
            if (value > maxValues[ve]) {
                maxValues[ve] = value;
            }
        }
        addInstance(index, lineElements[dim]);
    }
}
