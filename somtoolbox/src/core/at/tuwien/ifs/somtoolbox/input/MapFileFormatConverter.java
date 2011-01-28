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
package at.tuwien.ifs.somtoolbox.input;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.InputDataWriter;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.ESOMMapOutputter;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.output.SOMPAKMapOutputter;

/**
 * This class converts between various file formats for trained SOMs. Currently supported formats are listed in
 * {@link #FILE_FORMAT_TYPES}.
 * 
 * @author Rudolf Mayer
 * @version $Id: MapFileFormatConverter.java 3830 2010-10-06 16:29:11Z mayer $
 */
public class MapFileFormatConverter implements SOMToolboxApp {
    /** Supported File Format Types, currently SOMLib, SOMPak and ESOM */
    public static final String[] FILE_FORMAT_TYPES = { SOMLibFormatInputReader.getFormatName(),
            SOMPAKFormatInputReader.getFormatName(), ESOMFormatInputReader.getFormatName() };

    public static final Parameter[] OPTIONS = new Parameter[] {
            OptionFactory.getOptInputFormat(true, MapFileFormatConverter.FILE_FORMAT_TYPES),
            OptionFactory.getOptInputFileName(), OptionFactory.getOptInputVectorFile(false),
            OptionFactory.getOptTemplateVectorFile(false), OptionFactory.getOptUnitDescriptionFile(false),
            OptionFactory.getOptOutputDirectory(false),
            OptionFactory.getOptOutputFormat(true, MapFileFormatConverter.FILE_FORMAT_TYPES),
            OptionFactory.getOptGZip(false, true), OptionFactory.getOptOutputFileName(true) };

    public static final String DESCRIPTION = "Converts between various file formats for trained SOMs.";

    public static final String LONG_DESCRIPTION = DESCRIPTION + "Currently supported formats are "
            + Arrays.toString(FILE_FORMAT_TYPES);

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static void main(String[] args) throws IOException, SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);
        String inputFormat = config.getString("inputFormat", null);
        String outputFormat = config.getString("outputFormat", null);
        boolean gzipped = config.getBoolean("gzip");

        String inputFileName = config.getString("input", null);
        String unitDescriptionFile = config.getString("unitDescriptionFile", null);

        String templateVectorFile = config.getString("templateVectorFile", null);
        String inputVectorFile = config.getString("inputVectorFile", null);

        InputData data = null;
        if (inputVectorFile != null) {
            data = InputDataFactory.open(inputVectorFile);
        }

        SOMInputReader reader = null;
        SOMLibTemplateVector tv = null;
        if (inputFormat.equals(SOMLibFormatInputReader.getFormatName())) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Reading SOMLib Input Format.");
            reader = new SOMLibFormatInputReader(inputFileName, unitDescriptionFile, null);
            if (templateVectorFile != null) {
                tv = new SOMLibTemplateVector(templateVectorFile);
            }
        } else if (inputFormat.equals(SOMPAKFormatInputReader.getFormatName())) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Reading SOMPak Input Format.");
            reader = new SOMPAKFormatInputReader(inputFileName);
            tv = new SOMLibTemplateVector(reader.getXSize() * reader.getYSize(), reader.getDim());
            tv.setComponentNames(((SOMPAKFormatInputReader) reader).getComponentNames());
        } else if (inputFormat.equals(ESOMFormatInputReader.getFormatName())) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Reading ESOM Input Format.");
            reader = new ESOMFormatInputReader(inputFileName, unitDescriptionFile);
        }

        GrowingSOM gsom = new GrowingSOM(reader);
        String fDir = config.getString("outputDirectory", ".");
        String fName = config.getString("output", null);
        if (!new File(fDir).exists()) {
            new File(fDir).mkdirs();
        }
        if (outputFormat.equals(SOMLibFormatInputReader.getFormatName())) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing SOMLib Output Format.");
            SOMLibMapOutputter.writeUnitDescriptionFile(gsom, fDir, fName, gzipped);
            SOMLibMapOutputter.writeWeightVectorFile(gsom, fDir, fName, gzipped);
            if (tv != null) {
                InputDataWriter.writeAsSOMLib(tv, fDir + File.separator + fName + ".tv");
            }
        } else if (outputFormat.equals(SOMPAKFormatInputReader.getFormatName())) {
            SOMPAKMapOutputter.write(gsom, tv, fDir, fName, gzipped);
        } else if (outputFormat.equals(ESOMFormatInputReader.getFormatName())) {
            ESOMMapOutputter.write(gsom, data, fDir, fName, gzipped);
        }
    }
}
