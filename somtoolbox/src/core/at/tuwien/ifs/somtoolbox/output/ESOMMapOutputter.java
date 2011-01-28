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
package at.tuwien.ifs.somtoolbox.output;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Saves a map in the ESOM format (see http://databionic-esom.sourceforge.net/user.html#File_formats)
 * 
 * @author Rudolf Mayer
 * @version $Id: ESOMMapOutputter.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class ESOMMapOutputter {
    public static void write(GrowingSOM gsom, InputData data, String dir, String fileName, boolean gzipped)
            throws IOException, SOMToolboxException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start saving ESOM output files");

        dir = FileUtils.prepareOutputDir(dir);

        // write the ESOM weights (.wts) file, analog to the SOMLib Weight Vector File
        PrintWriter writer = FileUtils.openFileForWriting("ESOM weights file", fileName + ".wts", gzipped);
        writer.println("#ESOM weights file, saved with the Java SOMToolbox (http://www.ifs.tuwien.ac.at/dm/somtoolbox/)");
        writer.println("% " + gsom.getLayer().getYSize() + " " + gsom.getLayer().getXSize());// first row, then column
        // number
        writer.println("% " + gsom.getLayer().getDim());
        for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
            for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                writer.println(StringUtils.interleave(gsom.getLayer().getUnit(x, y).getWeightVector(), "\t"));
            }
        }
        writer.flush();
        writer.close();

        if (data != null) {
            // write the ESOM bestmatches (.bm) file, similar to a SOMLibDataWinnerMapping, but with only one winner
            writer = FileUtils.openFileForWriting("ESOM bestmatches file", fileName + ".bm", gzipped);
            writer.println("#ESOM bestmatches file, saved with the Java SOMToolbox (http://www.ifs.tuwien.ac.at/dm/somtoolbox/)");
            writer.println("% " + gsom.getLayer().getYSize() + " " + gsom.getLayer().getXSize());// first row, then
            // column number
            writer.println("% " + data.numVectors());
            for (int i = 0; i < data.numVectors(); i++) {
                Unit winner = gsom.getLayer().getWinner(data.getInputDatum(i));
                // index starts with 1, make sure this is in synch with AbstractSOMLibSparseInputData#writeAsESOM
                writer.println(i + 1 + "\t" + winner.getYPos() + "\t" + winner.getXPos()); // first row, then column
                // index
            }
            writer.flush();
            writer.close();
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "No input data provided - not writing ESOM bestmatches file.");
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished saving ESOM output files");

    }
}
