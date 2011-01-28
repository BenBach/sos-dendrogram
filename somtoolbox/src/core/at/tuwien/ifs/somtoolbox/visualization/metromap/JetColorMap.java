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
 * Colour map similar to the Jet colour map in MATLAB.
 * 
 * @author Rudolf Mayer
 * @version $Id: JetColorMap.java 3358 2010-02-11 14:35:07Z mayer $ *
 */
public class JetColorMap {
    public Color getColor(float x) {
        float a; // alpha
        if (x < 0.f) {
            return new Color(0.f, 0.f, 0.f);
        } else if (x < 0.125f) {
            a = x / 0.125f;
            return new Color(0.f, 0.f, 0.5f + 0.5f * a);
        } else if (x < 0.375f) {
            a = (x - 0.125f) / 0.25f;
            return new Color(0.f, a, 1.f);
        } else if (x < 0.625f) {
            a = (x - 0.375f) / 0.25f;
            return new Color(a, 1.f, 1.f - a);
        } else if (x < 0.875f) {
            a = (x - 0.625f) / 0.25f;
            return new Color(1.f, 1.f - a, 0.f);
        } else if (x <= 1.0f) {
            a = (x - 0.875f) / 0.125f;
            return new Color(1.f - 0.5f * a, 0.f, 0.f);
        } else {
            return new Color(1.f, 1.f, 1.f);
        }
    }

    public Color[] getColors(int count) {
        Color[] res = new Color[count];
        for (int i = 0; i < res.length; i++) {
            res[i] = getColor(i / (float) count);
        }
        return res;
    }
}
