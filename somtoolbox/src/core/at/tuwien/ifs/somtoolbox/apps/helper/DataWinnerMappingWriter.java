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

import java.io.IOException;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;

/**
 * This class writes a data winner mapping file from a trained map.
 * 
 * @author Rudolf Mayer
 * @version $Id: DataWinnerMappingWriter.java 3691 2010-07-15 09:23:21Z frank $
 */
public class DataWinnerMappingWriter implements SOMToolboxApp {
    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputVectorFile(true),
            OptionFactory.getOptWeightVectorFile(true), OptionFactory.getOptUnitDescriptionFile(true),
            OptionFactory.getOptMapDescriptionFile(false), OptionFactory.getOptNumberWinners(true),
            OptionFactory.getOptOutputFileName(true), OptionFactory.getOptOutputDirectory(false) };

    public static final String DESCRIPTION = "Writes a data winner mapping file from a trained map";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static void main(String[] args) throws SOMLibFileFormatException, IOException {
        // register and parse all options for the Data Winner Mapping writer
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String vectorFileName = config.getString("inputVectorFile");
        String weightVectorFile = config.getString("weightVectorFile");
        String unitDescriptionFile = config.getString("unitDescriptionFile");
        String mapDescriptionFile = config.getString("mapDescriptionFile");
        String outputDir = config.getString("outputDirectory", ".");
        String outputFileName = config.getString("output");
        int numWinners = config.getInt("numberWinners", 50);

        SOMLibSparseInputData inputData = new SOMLibSparseInputData(vectorFileName);
        GrowingSOM gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFile, unitDescriptionFile,
                mapDescriptionFile));

        if (numWinners > gsom.getLayer().getXSize() * gsom.getLayer().getYSize()) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "More winners specified than units available on the map. Using number of map units (" + numWinners
                            + ").");
            numWinners = gsom.getLayer().getXSize() * gsom.getLayer().getYSize();
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Going to write DataWinnerMapping file with " + numWinners + " winners.");
        }

        SOMLibMapOutputter.writeDataWinnerMappingFile(gsom, inputData, numWinners, outputDir, outputFileName, true);
    }

}
