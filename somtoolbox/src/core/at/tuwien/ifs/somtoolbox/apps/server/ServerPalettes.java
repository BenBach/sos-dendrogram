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
package at.tuwien.ifs.somtoolbox.apps.server;

import at.tuwien.ifs.somtoolbox.visualization.Palette;

/**
 * @author Rudolf Mayer
 * @version $Id: ServerPalettes.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class ServerPalettes extends at.tuwien.ifs.somtoolbox.visualization.Palettes {

    public static String getPaletteControl(Palette selected) {
        Palette[] pals = getAvailablePalettes();
        StringBuffer b = new StringBuffer(pals.length * 70);
        if (pals.length > 1 && pals.length <= 3) { // make radio buttons
            for (Palette pal : pals) {
                b.append("<input type=\"radio\" name=\"palette\" onchange=\"this.form.submit()\" value=\""
                        + pal.getName() + "\"");
                if (pal == selected) {
                    b.append(" selected ");
                }
                b.append(">" + pal.getName() + "\n");
            }
        } else { // make a select drop down
            b.append("<select name=\"palette\" onchange=\"this.form.submit()\">\n");
            for (Palette pal : pals) {
                b.append("<option value=\"" + pal.getName() + "\"");
                if (pal == selected) {
                    b.append(" selected ");
                }
                b.append(">" + pal.getName() + "</option>\n");
            }
            b.append("</select>\n");
        }
        return b.toString();
    }

    public static String getPaletteControl(String selected) {
        return getPaletteControl(getPaletteByName(selected));
    }

}
