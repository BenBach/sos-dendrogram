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
package at.tuwien.ifs.somtoolbox.data;

import java.io.File;

import org.math.io.files.ASCIIFile;
import org.math.io.parser.ArrayString;

import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Reads data from a simple matrix file. Rows are separated by newlines, and columns by spaces or tabs.
 * 
 * @author Rudolf Mayer
 * @version $Id: SimpleMatrixInputData.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class SimpleMatrixInputData extends AbstractSOMLibSparseInputData {
    private double[][] matrix;

    public SimpleMatrixInputData(String fileName) {
        ArrayString.defaultColumnDelimiter = StringUtils.REGEX_SPACE_OR_TAB;
        matrix = ASCIIFile.readDoubleArray(new File(fileName));
        numVectors = matrix.length;
        dim = matrix[0].length;
        dataNames = new String[numVectors];
        for (int i = 0; i < matrix.length; i++) {
            dataNames[i] = String.valueOf(i);
        }
        templateVector = new SOMLibTemplateVector(numVectors(), dim());
    }

    @Override
    public InputDatum getInputDatum(int d) {
        return new InputDatum(dataNames[d], matrix[d]);
    }

    @Override
    public double[] getInputVector(int d) {
        return matrix[d];
    }

    @Override
    public double getValue(int x, int y) {
        return matrix[x][y];
    }

    @Override
    public double mqe0(DistanceMetric metric) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public InputData subset(String[] names) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static String getFormatName() {
        return "simpleMatrix";
    }

    public static String getFileNameSuffix() {
        return ".matrix";
    }
}
