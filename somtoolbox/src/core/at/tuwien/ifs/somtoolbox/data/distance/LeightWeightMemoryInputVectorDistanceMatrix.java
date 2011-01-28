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

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class implements an {@link AbstractMemoryInputVectorDistanceMatrix} using a one-dimensional double array (i.e.
 * double[]).<br/>
 * Similar to {@link RandomAccessFileInputVectorDistanceMatrix}, the storage is sparse, not containing the diagonal and
 * the lower half, i.e. as follows:
 * 
 * <pre>
 * [(2,1) (3,1) (4,1) (5,1) (6,1)]
 * [      (3,2) (4,2) (5,1) (6,2)]
 * [            (4,3) (5,3) (6,3)]
 * [                  (5,4) (6,4)]
 * [                        (6,5)]
 * [                             ]
 * </pre>
 * 
 * Thus, there are (n-1)! double values. If this is still too much memory consumption, consider using
 * {@link RandomAccessFileInputVectorDistanceMatrix} instead.<br/>
 * Compared to {@link FullMemoryInputVectorDistanceMatrix}, the memory consumption is lower, and
 * {@link #getDistancesFlat()} is very fast, while {@link #getDistances(int)} needs to be constructed on the fly.
 * 
 * @author Rudolf Mayer
 * @version $Id: LeightWeightMemoryInputVectorDistanceMatrix.java 3704 2010-07-20 10:42:42Z mayer $
 */
public class LeightWeightMemoryInputVectorDistanceMatrix extends AbstractMemoryInputVectorDistanceMatrix {

    double[] distances;

    public LeightWeightMemoryInputVectorDistanceMatrix(InputData data, DistanceMetric metric) throws MetricException {
        super(data, metric);
    }

    public LeightWeightMemoryInputVectorDistanceMatrix(String fileName) throws IOException, SOMToolboxException {
        super(fileName);
    }

    @Override
    protected void setValue(int x, int y, double value) {
        distances[getMatrixIndex(x, y)] = value;
    }

    @Override
    protected void initStorage() {
        distances = new double[flatArraySize()];
    }

    /** Finds the index in vector for the matrix position (x,y); see {@link #distances} */
    @Override
    public double getDistance(int x, int y) {
        if (x == y) {
            return 0;
        } else {
            final int matrixIndex = getMatrixIndex(x, y);
            if (matrixIndex > distances.length || matrixIndex < 0) {
                System.out.println(x + "x" + y + "=>" + matrixIndex);
            }
            return distances[matrixIndex];
        }
    }

    protected int getMatrixIndex(int x, int y) {
        if (x > y) {
            return getMatrixIndex(y, x);
        } else if (x < y) {
            return x * numVectors + y - (x + 1) * (x + 2) / 2;
        }
        return -1;
    }

    /**
     * This implementation is not of good performance, as the matrix row is always constructed on the fly. If you need
     * to use this method often, consider using a {@link FullMemoryInputVectorDistanceMatrix} instead
     */
    @Override
    public double[] getDistances(int x) {
        double[] row = new double[numVectors];
        for (int y = 0; y < row.length; y++) {
            row[y] = getDistance(x, y);
        }
        return row;
    }

    /**
     * This implementation is of high performance, as it returns only the already internally constructed array.
     * 
     * @see InputVectorDistanceMatrix#getDistancesFlat()
     */
    @Override
    public double[] getDistancesFlat() {
        return distances;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Distance matrix ").append(numVectors()).append("x").append(numVectors()).append("\n");
        if (numVectors() < 20) {
            for (int x = 0; x < numVectors(); x++) {
                sb.append("[");
                for (int y = 0; y < numVectors(); y++) {
                    sb.append(StringUtils.format(getDistance(x, y), 3, true));
                    if (y + 1 < numVectors()) {
                        sb.append(" ");
                    }
                }
                sb.append("]\n");
            }
        }
        return sb.toString();
    }

}
