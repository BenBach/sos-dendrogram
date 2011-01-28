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
package at.tuwien.ifs.somtoolbox.properties;

import java.io.FileInputStream;
import java.util.Properties;

import at.tuwien.ifs.somtoolbox.visualization.Palettes;

/**
 * Properties for SOMViewer application.
 * 
 * @author Thomas Lidy
 * @version $Id: SOMViewerProperties.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class SOMViewerProperties extends Properties {
    private static final long serialVersionUID = 1L;

    // private String audioPlayer = null;

    // private String palettesDir = Palettes.DEFAULT_PALETTES_DIR;

    private String htmlMapTemplatesDir = System.getProperty("user.dir") + "/rsc/html/map";

    /**
     * Loads the properties (or preferences) for the SOMViewer application and GUI.
     * 
     * @param fname name of the properties file.
     * @throws PropertiesException thrown if properties file could not be opened or the values of the properties are
     *             illegal.
     */
    public SOMViewerProperties(String fname) throws PropertiesException {
        try {
            load(new FileInputStream(fname));
        } catch (Exception e) {
            throw new PropertiesException("Could not open SOMViewer properties file " + fname);
        }
        try {

            if (getAudioPlayer() == null) {
                throw new PropertiesException("audioPlayer not set in " + fname);
            }

        } catch (NumberFormatException e) {
            throw new PropertiesException("Illegal numeric value in SOMViewer properties file.");
        }
    }

    /**
     * Creates an empty SOMViewerProperties object.
     */
    public SOMViewerProperties() {
    }

    /**
     * Returns the path name to call the preferred audio player to play audio files from PlaySOM Panel.
     * 
     * @return path name to audio player.
     */
    public String getAudioPlayer() {
        return getProperty("audioPlayer");
    }

    public String getPalettesDir() {
        return getProperty("palettes.dir", Palettes.DEFAULT_PALETTES_DIR);
    }

    public String getHtmlMapTemplatesDir() {
        return getProperty("html.template.dir", htmlMapTemplatesDir);
    }

}
