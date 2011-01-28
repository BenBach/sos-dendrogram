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

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer.Flip;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;

/**
 * Rotates a map by the given degrees, and writes a new unit- and weight-vector file.
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: MapRotator.java 3757 2010-08-17 14:15:41Z frank $
 */
public class MapRotator implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptUnitDescriptionFile(true),
            OptionFactory.getOptWeightVectorFile(true), OptionFactory.getOptDataWinnerMappingFile(false),
            OptionFactory.getOptOutputFileName(true), OptionFactory.getOptRotation(false),
            OptionFactory.getOptFlip(false) };

    public static final String DESCRIPTION = "Rotates a map by the given degrees, and writes a new unit- and weight-vector file";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static void main(String[] args) {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);
        String unitDescriptionFile = config.getString("unitDescriptionFile");
        String weightVectorFile = config.getString("weightVectorFile");
        String dwmFile = config.getString("dataWinnerMappingFile");
        String output = config.getString("output");
        final String dir = ".";

        try {
            GrowingSOM gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFile, unitDescriptionFile, null));
            SOMLibDataWinnerMapping dwm = null;
            if (dwmFile != null) {
                dwm = new SOMLibDataWinnerMapping(dwmFile);
            }
            final int xSize = gsom.getLayer().getXSize();
            final int ySize = gsom.getLayer().getYSize();

            // Flip map?
            if (config.userSpecified("flip")) {
                char flip = config.getChar("flip");
                switch (flip) {
                    case 'h':
                        gsom.getLayer().flip(Flip.HORIZONTAL);
                        if (dwm != null) {
                            dwm.flipH(ySize);
                        }
                        break;
                    case 'v':
                        gsom.getLayer().flip(Flip.VERTICAL);
                        if (dwm != null) {
                            dwm.flipV(xSize);
                        }
                        break;
                    default:
                        System.err.printf("Invalid flip operation: %s%n", config.getString("filp"));
                        System.exit(1);
                        break;
                }
                System.out.printf("Flip: %s%n", flip);
            }

            if (config.userSpecified("rotation")) {
                int rotation = config.getInt("rotation");
                try {
                    GrowingLayer.checkRotation(rotation);
                    System.out.printf("Rotate: %d%n", rotation);
                } catch (SOMToolboxException e) {
                    System.err.printf("Invalid rotation operatrion: %d%n", rotation);
                    System.exit(1);
                }
                gsom.getLayer().rotate(rotation);
                if (dwm != null) {
                    dwm.rotate(rotation / 90, xSize, ySize);
                }

            }

            SOMLibMapOutputter.writeUnitDescriptionFile(gsom, dir, output, true);
            SOMLibMapOutputter.writeWeightVectorFile(gsom, dir, output, true);
            if (dwm != null) {
                SOMLibMapOutputter.writeDataWinnerMappingFile(dwm, dir, output, true);
            }
        } catch (Exception e) {
            System.err.printf("%s%n", e.getMessage());
            System.exit(2);
        }
    }
}
