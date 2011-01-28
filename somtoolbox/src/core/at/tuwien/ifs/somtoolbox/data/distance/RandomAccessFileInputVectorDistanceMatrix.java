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
import java.io.RandomAccessFile;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;

/**
 * A distance matrix based on a binary {@link RandomAccessFile}. This implementation does not read the matrix into the
 * memory, and is thus suited especially for big datasets.
 * <p>
 * The file is built as follows:
 * <ul>
 * <li>One integer value, giving the number of vectors</li>
 * <li>A series of double values representing the upper-right half of the symmetric distance matrix, not containing the
 * values in the diagonal itself (as they are all 0).<br>
 * Thus, there are (n-1)! double values, and the matrix file contains the following (x, y) tuples:
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
 * </li>
 * <li>The name of the metric used, as String (until the end of the file).</li>
 * </ul>
 * </p>
 * 
 * @author Rudolf Mayer
 * @version $Id: RandomAccessFileInputVectorDistanceMatrix.java 3706 2010-07-20 11:07:54Z mayer $
 */
public class RandomAccessFileInputVectorDistanceMatrix extends InputVectorDistanceMatrix {
    public static final int BYTES_HEADER = Integer.SIZE / 8;

    private static final int BYTES_CHAR = Character.SIZE / 8;

    private static final int BYTES_DOUBLE = Double.SIZE / 8;

    private RandomAccessFile file;

    public RandomAccessFileInputVectorDistanceMatrix(String fileName) throws IOException, SOMToolboxException {
        file = new RandomAccessFile(fileName, "rw");
        numVectors = file.readInt();
    }

    @Override
    public DistanceMetric getMetric() {
        if (metric == null) {
            try {
                long offset = (getOffset(numVectors - 1, numVectors - 1, numVectors) + 1) * Double.SIZE / 8
                        + BYTES_HEADER;
                file.seek(offset);
                String metricName = "";
                for (long i = offset; i < file.length(); i += BYTES_CHAR) {
                    final char readChar = file.readChar();
                    metricName += readChar;
                }
                metricName = metricName.trim();
                metric = AbstractMetric.instantiateNice(metricName);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SOMToolboxException e) {
                e.printStackTrace();
            }
        }
        return metric;
    }

    @Override
    public double getDistance(int x, int y) {
        if (x == y) {
            return 0;
        } else {
            try {
                file.seek(getOffset(x, y, numVectors) * BYTES_DOUBLE + BYTES_HEADER);
                return file.readDouble();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    /** Find the offset of a specific value in the linear order */
    protected static long getOffset(long x, long y, long numVectors) {
        if (y > x) {
            return getOffset(y, x, numVectors);
        }
        // we need to use long, cause otherwise for larger files, we get an overflow and thus negative numbers!
        long factor = (long) ((y + 1) / 2d * y);
        long pos = y * numVectors - factor - 1 + x - y;
        return pos;
    }
}
