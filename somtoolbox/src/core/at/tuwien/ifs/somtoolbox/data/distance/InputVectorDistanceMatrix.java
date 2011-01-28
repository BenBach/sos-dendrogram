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
import java.util.ArrayList;
import java.util.logging.Logger;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Base for classes providing a distance matrix of the input vectors, with generic methods and fields.<br/>
 * A distance matrix is of size <code>n*n</code>, where <code>n</code> is the number of input vectors. The matrix is
 * symmetric, i.e. the upper-right and lower-left halves contain the same values. The diagonal contains the distances of
 * one element to itself, and is thus always 0.
 * 
 * @author Rudolf Mayer
 * @version $Id: InputVectorDistanceMatrix.java 3711 2010-07-23 09:37:24Z mayer $
 */
public abstract class InputVectorDistanceMatrix {

    protected int numVectors;

    protected DistanceMetric metric;

    protected ArrayList<String> inputLabels;

    public DistanceMetric getMetric() {
        return metric;
    }

    public ArrayList<String> getInputLabels() {
        return inputLabels;
    }

    public InputVectorDistanceMatrix() {
        super();
    }

    /** Return the distance between input vectors x and y. */
    public abstract double getDistance(int x, int y);

    /**
     * Return the n nearest vectors of input x. Basic implementation of the method, sub-classes might provide an
     * optimised implementation.
     */
    public int[] getNNearest(int x, int num) {
        double[] distancesToInput = getDistances(x);
        int[] indices = new int[num];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = -1;
        }
        double[] distances = new double[num];

        for (int i = 0; i < numVectors; i++) {
            if (x == i) { // skip similarity to the vector itself..
                continue;
            }
            int element = 0;
            double distance = distancesToInput[i];
            boolean inserted = false;
            while (inserted == false && element < num) {
                if (indices[element] == -1 || distance < distances[element]) { // found place to insert unit
                    for (int m = num - 2; m >= element; m--) { // move units with greater distance to right
                        indices[m + 1] = indices[m];
                        distances[m + 1] = distances[m];
                    }
                    indices[element] = i;
                    distances[element] = distance;
                    inserted = true;
                }
                element++;
            }
        }
        return indices;
    }

    /**
     * Return the distances to all vectors from input x. This is a basic using {@link #getDistance(int, int)},
     * sub-classes might provide an optimised implementation.
     */
    public double[] getDistances(int x) {
        double[] d = new double[numVectors];
        for (int y = 0; y < d.length; y++) {
            d[y] = getDistance(x, y);
        }
        return d;
    }

    public int numVectors() {
        return numVectors;
    }

    /**
     * Gets all the distances in a single flat array avoiding duplicates from the pairwise distances, thus of the size
     * of <code>numVectors * (numVectors - * 1) / 2</code>.<br/>
     * This is a default implementation always constructing the array on the fly using the
     * {@link #getDistance(int, int)} method. Specific subclasses might provide better performing implementations, as
     * e.g. {@link LeightWeightMemoryInputVectorDistanceMatrix}.
     */
    public double[] getDistancesFlat() {
        double[] distances = new double[flatArraySize()];
        int index = 0;
        for (int x = 1; x < distances.length; x++) {
            for (int y = x + 1; y < distances.length; y++) {
                distances[index] = getDistance(x, y);
            }
        }
        return distances;
    }

    public DenseDoubleMatrix1D getDistancesFlatAsMatrix() {
        return new DenseDoubleMatrix1D(getDistancesFlat());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Distance matrix ").append(numVectors()).append("x").append(numVectors()).append("\n");
        if (numVectors() < 20) {
            for (int i = 0; i < numVectors(); i++) {
                sb.append(StringUtils.toStringWithPrecision(getDistances(i), 3)).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InputVectorDistanceMatrix)) {
            return false;
        } else {
            final InputVectorDistanceMatrix other = (InputVectorDistanceMatrix) obj;
            for (int i = 0; i < numVectors(); i++) {
                for (int j = 0; j < numVectors(); j++) {
                    if (getDistance(i, j) != other.getDistance(i, j)) {
                        System.out.println("not equal in " + i + "," + j + ": " + getDistance(i, j) + " <> "
                                + other.getDistance(i, j));
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Factory method that reads and creates an {@link InputVectorDistanceMatrix} from the given file. Depending on the
     * filename, returns either a {@link RandomAccessFileInputVectorDistanceMatrix} (if the filename ends with '.bin')
     * or a {@link LeightWeightMemoryInputVectorDistanceMatrix} (all other cases).<br>
     * TODO: maybe more intelligent checking for file type, possibly trying to read it as binary, and checking the first
     * bytes for a file type or so.
     */
    public static InputVectorDistanceMatrix initFromFile(String fileName) throws IOException, SOMToolboxException {
        if (fileName.endsWith(".bin") || !FileUtils.fileStartsWith(fileName, "$")) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Opening binary random access distance matrix file");
            return new RandomAccessFileInputVectorDistanceMatrix(fileName);
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Reading ASCII distance matrix into memory.");
            return new LeightWeightMemoryInputVectorDistanceMatrix(fileName);
        }
    }

    protected int flatArraySize() {
        return numVectors * (numVectors - 1) / 2;
    }

    public int rows() {
        return numVectors;
    }

    public int columns() {
        return numVectors;
    }

}