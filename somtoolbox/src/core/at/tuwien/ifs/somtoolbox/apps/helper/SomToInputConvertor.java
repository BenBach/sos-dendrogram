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

import java.io.File;
import java.io.IOException;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * Converts a SOM map to an input vector file.
 * 
 * @author Rudolf Mayer
 * @version $Id: SomToInputConvertor.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class SomToInputConvertor {
    public static void main(String[] args) throws SOMLibFileFormatException, IOException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptWeightVectorFile(true),
                OptionFactory.getOptOutputFileName(true));
        final String weightVectorFile = config.getString("weightVectorFile");
        final String outFile = config.getString("output");
        SOMLibFormatInputReader ir = new SOMLibFormatInputReader(weightVectorFile, null, null);
        GrowingLayer layer = new GrowingLayer(ir.getXSize(), ir.getYSize(), ir.getZSize(), ir.getMetricName(),
                ir.getDim(), false, false, 7, null);
        final Unit[] allUnits = layer.getAllUnits();
        InputDatum[] inputData = new InputDatum[allUnits.length];
        String name;
        if (weightVectorFile.contains(File.separator)) {
            name = weightVectorFile.substring(weightVectorFile.lastIndexOf(File.separator) + 1);
        } else {
            name = weightVectorFile;
        }
        name = name.replace(".wgt", "");
        for (int i = 0; i < allUnits.length; i++) {

            inputData[i] = new InputDatum(name + "_" + allUnits[i].printCoordinates(), allUnits[i].getWeightVector());
        }
        SOMLibSparseInputData data = new SOMLibSparseInputData(inputData, null);
        InputDataWriter.writeAsSOMLib(data, outFile + ".vec");
    }
}
