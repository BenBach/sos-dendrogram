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

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.comparables.InputNameDistance;

/**
 * This class implements an {@link AbstractMemoryInputVectorDistanceMatrix} using a two-dimensional double array (i.e.
 * double[][]).<br/>
 * This implies a higher memory consumption as compared to {@link LeightWeightMemoryInputVectorDistanceMatrix}, but also
 * faster access times for {@link #getDistance(int, int)} and {@link #getDistances(int)}, while
 * {@link #getDistancesFlat()} needs to be generated on the fly.
 * 
 * @author Rudolf Mayer
 * @version $Id: FullMemoryInputVectorDistanceMatrix.java 3704 2010-07-20 10:42:42Z mayer $
 */
public class FullMemoryInputVectorDistanceMatrix extends AbstractMemoryInputVectorDistanceMatrix {
    protected double[][] distanceMatrix;

    public FullMemoryInputVectorDistanceMatrix(InputData data, DistanceMetric metric) throws MetricException {
        super(data, metric);
    }

    public FullMemoryInputVectorDistanceMatrix(String fileName) throws IOException, SOMToolboxException {
        super(fileName);
    }

    @Override
    protected void setValue(int x, int y, double value) {
        distanceMatrix[x][y] = value;
        distanceMatrix[y][x] = value;
    }

    @Override
    protected void initStorage() {
        distanceMatrix = new double[numVectors][numVectors];
    }

    @Override
    public double getDistance(int x, int y) {
        return distanceMatrix[x][y];
    }

    @Override
    public double[] getDistances(int x) {
        return distanceMatrix[x];
    }

    public ArrayList<InputNameDistance> getNamedDistances(int x) {
        ArrayList<InputNameDistance> res = new ArrayList<InputNameDistance>(numVectors - 1);
        for (int i = 0; i < numVectors; i++) {
            res.add(new InputNameDistance(getDistance(x, i), inputLabels.get(i)));
        }
        return res;
    }

}
