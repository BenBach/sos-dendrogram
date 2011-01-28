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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.jdom.JDOMException;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.visualization.Palette;

/**
 * Converts palettes given as RGB values to the XML format used in the SOMToolbox.
 * 
 * @author Rudolf Mayer
 * @version $Id: RGBPaletteConverter.java 3671 2010-07-15 09:05:01Z frank $
 */
public class RGBPaletteConverter implements SOMToolboxApp {
    public static String DESCRIPTION = "Converts palettes given as RGB values to the XML format used in the SOMToolbox.";

    public static String LONG_DESCRIPTION = DESCRIPTION;

    public static Parameter[] OPTIONS = {
            OptionFactory.getOptInputFileName(true),
            new FlaggedOption("name", JSAP.STRING_PARSER, null, false, JSAP.NO_SHORTFLAG, "name",
                    "Name of the palette."),
            new FlaggedOption("shortName", JSAP.STRING_PARSER, null, false, JSAP.NO_SHORTFLAG, "shortName",
                    "Short name of the palette."),
            new FlaggedOption("description", JSAP.STRING_PARSER, null, false, JSAP.NO_SHORTFLAG, "description",
                    "Description of the palette."), OptionFactory.getOptOutputFileName(true) };

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static void main(String[] args) throws JDOMException, IOException {
        System.out.println(Arrays.toString(args));
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);
        String fileName = config.getString("inputFile");
        String outputfile = config.getString("output");
        String name = config.getString("name", "");
        String shortName = config.getString("shortName", name);
        String description = config.getString("description", name);

        BufferedReader reader = FileUtils.openFile("RGB Palette", fileName);
        ArrayList<Color> colours = new ArrayList<Color>();
        String line;
        int lineIndex = 0;
        while ((line = reader.readLine()) != null) {
            lineIndex++;
            if (line.trim().length() > 0) {
                // FIXME use ColorStringParser instead
                String[] values = line.trim().split(StringUtils.REGEX_SPACE_OR_TAB);
                if (values.length == 3) {
                    try {
                        colours.add(new Color(Float.parseFloat(values[0]), Float.parseFloat(values[1]),
                                Float.parseFloat(values[2])));
                    } catch (NumberFormatException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                                "Line " + lineIndex + " contained an illegal value: '" + line + "'. Ignoring line.");
                    }
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                            "Line " + lineIndex + " contained an invalid number of RGB arguments: expected 3, found "
                                    + values.length + " (" + line + "). Ignoring line.");
                }
            }
        }
        new Palette(name, shortName, description, colours.toArray(new Color[colours.size()])).savePaletteToXML(new File(
                outputfile));
    }
}
