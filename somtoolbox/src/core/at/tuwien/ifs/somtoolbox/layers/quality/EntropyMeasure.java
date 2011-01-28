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
package at.tuwien.ifs.somtoolbox.layers.quality;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * Implementation of SOM Entropy Measure. (Hulle 2000)
 * 
 * @author Christoph Hohenwarter
 * @version $Id: EntropyMeasure.java 3883 2010-11-02 17:13:23Z frank $
 */
public class EntropyMeasure extends AbstractQualityMeasure {

    private double entropie = 0;

    private double[][] unitsE;

    public EntropyMeasure(Layer layer, InputData data) {
        super(layer, data);

        mapQualityNames = new String[] { "entropy" };
        mapQualityDescriptions = new String[] { "Entropy Measure" };

        int xs = layer.getXSize();
        int ys = layer.getYSize();
        double totalHits = 0.0;
        unitsE = new double[xs][ys];

        ArrayList<Unit> units = new ArrayList<Unit>();

        double summe = 0;

        // Construction of an array A of all neurons
        for (int xi = 0; xi < xs; xi++) {
            for (int yi = 0; yi < ys; yi++) {
                try {
                    units.add(layer.getUnit(xi, yi));
                    totalHits = totalHits + layer.getUnit(xi, yi).getNumberOfMappedInputs();
                } catch (LayerAccessException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        for (int i = 0; i < units.size(); i++) {
            double pi = units.get(i).getNumberOfMappedInputs() / totalHits;
            // To avoid NaN-calculations of log10
            if (pi > 0) {
                unitsE[units.get(i).getXPos()][units.get(i).getYPos()] = pi * Math.log10(pi) * -1;
                summe = summe + pi * Math.log10(pi);
            }
        }

        summe = summe * -1;

        // Endresult
        entropie = summe;

    }

    @Override
    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        return entropie;
    }

    @Override
    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        return this.unitsE;
    }

}
