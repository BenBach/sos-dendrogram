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
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.SOMPAKInputData;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Saves a map in the SOMPAK format (see http://www.cis.hut.fi/projects/somtoolbox/package/docs2/som_read_cod.html)
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMPAKMapOutputter.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class SOMPAKMapOutputter {
    public static void write(GrowingSOM gsom, TemplateVector tv, String dir, String fileName, boolean gzipped)
            throws IOException, SOMToolboxException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start saving SOMPAK output files");
        fileName = StringUtils.appendExtension(fileName, ".cod");
        dir = FileUtils.prepareOutputDir(dir);

        PrintWriter writer = FileUtils.openFileForWriting("SOMPAK file", fileName, gzipped);
        // first line contains a lot of info: dimensions, shape, size, neighbourhood type
        // e.g. 4 rect 18 12 gaussian
        writer.println(gsom.getLayer().getDim() + " " + gsom.getLayer().getGridLayout().getSomPakName() + " "
                + gsom.getLayer().getXSize() + " " + gsom.getLayer().getYSize() + " " + "gaussian");
        if (tv == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Template vector not loaded - creating a generic one.");
            tv = new SOMLibTemplateVector(gsom.getLayer().getNumberOfMappedInputs(), gsom.getLayer().getDim());
        }
        writer.println(SOMPAKInputData.INDICATOR_COMPONENTS + " " + StringUtils.toString(tv.getLabels(), "", "", " "));
        for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
            for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                writer.println(StringUtils.interleave(gsom.getLayer().getUnit(x, y).getWeightVector(), " "));
            }
        }
        writer.flush();
        writer.close();

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished saving SOMPAK output files");

    }

}
