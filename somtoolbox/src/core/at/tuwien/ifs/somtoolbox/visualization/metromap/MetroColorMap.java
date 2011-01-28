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
package at.tuwien.ifs.somtoolbox.visualization.metromap;

import java.awt.Color;

/**
 * Color map from the london underground. See the official <a
 * href="http://www.tfl.gov.uk/tfl/corporate/media/designstandards/default.asp?standard=tube">design standards
 * document</a> for more details.
 * 
 * @author Rudolf Mayer
 * @version $Id: MetroColorMap.java 3583 2010-05-21 10:07:41Z mayer $ *
 */
public class MetroColorMap {

    public static final Color centralLine = new Color(220, 36, 31); // Central (red)

    public static final Color northern = Color.BLACK;// Northern (black)

    public static final Color victoria = new Color(0, 160, 226);// Victoria (light blue)

    public static final Color circleLine = new Color(255, 206, 0); // Circle (yellow)

    public static final Color districtLine = new Color(0, 114, 41); // District (green)

    public static final Color waterlooAndCity = new Color(118, 208, 189); // Waterloo & City (turquoise)

    public static final Color hammersmithAndCity = new Color(215, 153, 175);// Hammersmith & City (pink)

    public static final Color jubilee = new Color(134, 143, 152);// Jubilee (grey)

    public static final Color bakerloo = new Color(137, 78, 36);// Bakerloo (brown)

    public static final Color picadilly = new Color(0, 25, 168);// Piccadilly (blue)

    public static final Color metropolitan = new Color(117, 16, 86);// Metropolitan (magenta)

    public static final Color eastLondon = new Color(236, 158, 0);// East london (orange)

    public static Color[] colours = { centralLine, northern, victoria, circleLine, districtLine, waterlooAndCity,
            hammersmithAndCity, jubilee, bakerloo, picadilly, metropolitan, eastLondon };

    public Color getColor(int index) {
        return colours[index % colours.length];
    }

    public Color[] getColors(int count) {
        Color[] res = new Color[count];
        for (int i = 0; i < res.length; i++) {
            res[i] = getColor(i);
        }
        return res;
    }

    public int getColourCount() {
        return colours.length;
    }

}