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
package at.tuwien.ifs.somtoolbox.layers.initialisation;

import java.util.Random;

import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * Random SOM Initializer
 * 
 * @author Stefan Bischof
 * @author Leo Sklenitzka
 */
public class RandomInitializer implements LayerInitializer {

    private Layer layer;

    private int xSize;

    private int ySize;

    private int zSize;

    private int dim;

    private Random rand;

    private boolean normalized;

    public RandomInitializer(Layer layer, int xSize, int ySize, int zSize, int dim, Random rand, boolean normalized) {
        this.layer = layer;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.dim = dim;
        this.rand = rand;
        this.normalized = normalized;
    }

    /**
     * Initialize the SOM Layer randomly
     * 
     * @return initialized SOM
     */
    @Override
    public Unit[][][] initialize() {
        Unit[][][] units = new Unit[xSize][ySize][zSize];

        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    units[i][j][k] = new Unit(layer, i, j, k, dim, rand, normalized);
                }
            }
        }

        return units;
    }

}
