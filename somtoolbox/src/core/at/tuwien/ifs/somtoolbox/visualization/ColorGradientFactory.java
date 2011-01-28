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
package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * Provides a collection of methods to get a set of predefined {@link ColorGradient} instances.
 * 
 * @author Michael Dittenbach
 * @author Angela Roiger
 * @version $Id: ColorGradientFactory.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class ColorGradientFactory {

    /**
     * Color gradient for Sky visualization
     */
    public static ColorGradient galaxyGradientYellow() {
        double[] gradPoints = { 0.0, 0.6, 0.8, 0.89, 0.92, 0.96, 1.0 };
        Color[] gradColors = { new Color(0, 0, 0), new Color(32, 32, 32), new Color(96, 96, 96),
                new Color(153, 153, 153), new Color(204, 204, 204), new Color(255, 244, 164), new Color(255, 255, 209) };
        ColorGradient gradient = null;
        try {
            gradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return gradient;
    }

    /**
     * Color gradient for Sky visualization
     */
    public static ColorGradient galaxyGradientGrey() {
        double[] gradPoints = new double[] { 0.0, 0.5, 0.75, 0.85, 0.9, 0.956, 1.0 };
        Color[] gradColors = new Color[] { new Color(0, 0, 0), new Color(66, 66, 66), new Color(96, 96, 96),
                new Color(100, 100, 100), new Color(110, 110, 110), new Color(120, 120, 120), new Color(130, 130, 130) };
        ColorGradient gradient = null;
        try {
            gradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return gradient;
    }

    public static ColorGradient CartographyColorGradient() {
        double[] gradPoints = { 0.0, 0.375, 0.5, 0.53125, 0.5625, 0.6875, 0.875, 1.0 };
        Color[] gradColors = { new Color(0, 0, 128), new Color(0, 0, 255), new Color(0, 128, 255),
                new Color(240, 240, 64), new Color(32, 160, 0), new Color(224, 224, 0), new Color(128, 128, 128),
                new Color(255, 255, 255) };
        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

    public static ColorGradient CartographyColorGradientLessWater() {
        double[] gradPoints = { 0.0, 0.15, 0.35, 0.47, 0.57, 0.7, 0.9, 1.0 };
        Color[] gradColors = { new Color(0, 0, 128), new Color(0, 0, 255), new Color(0, 128, 255),
                new Color(240, 240, 64), new Color(32, 160, 0), new Color(224, 224, 0), new Color(128, 128, 128),
                new Color(255, 255, 255) };
        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

    public static ColorGradient GrayscaleGradient() {
        double[] gradPoints = { 0.0, 1.0 };
        Color[] gradColors = { new Color(50, 50, 50), new Color(255, 255, 255) };
        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

    /** Test palette "Mountains 1a" */
    public static ColorGradient CartographyMountain1aGradient() {

        double[] gradPoints = { 0.0, 0.31, 0.68, 0.92, 1.0 };
        Color[] gradColors = { new Color(0, 100, 0), new Color(200, 200, 100), new Color(150, 0, 0),
                new Color(150, 150, 150), new Color(255, 255, 255) };
        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

    /** Test palette "Mountains 1b" */
    public static ColorGradient CartographyMountain1bGradient() {

        double[] gradPoints = { 0.0, 0.165, 0.32, 0.5, 0.66, 0.84, 1.0 };
        Color[] gradColors = { new Color(0, 100, 70), new Color(25, 128, 50), new Color(224, 210, 122),
                new Color(162, 71, 3), new Color(160, 0, 0), new Color(120, 120, 120), new Color(255, 255, 255) };
        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

    /** Test palette "Mountains 2a" */
    public static ColorGradient CartographyMountain2aGradient() {

        double[] gradPoints = { 0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0 };
        Color[] gradColors = { new Color(220, 250, 178), new Color(82, 184, 59), new Color(101, 148, 52),
                new Color(240, 157, 1), new Color(139, 11, 0), new Color(104, 40, 15), new Color(139, 95, 70),
                new Color(189, 189, 189), new Color(254, 252, 255) };
        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

    /** Test palette "Mountains 2b" */
    public static ColorGradient CartographyMountain2bGradient() {
        double[] gradPoints = { 0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0 };
        Color[] gradColors = { new Color(175, 240, 232), new Color(220, 250, 178), new Color(82, 184, 59),
                new Color(101, 148, 52), new Color(240, 157, 1), new Color(139, 11, 0), new Color(104, 40, 15),
                new Color(139, 95, 70), new Color(189, 189, 189) };
        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

    /** Test palette "Mountains 2c" */
    public static ColorGradient CartographyMountain2cGradient() {
        double[] gradPoints = { 0.0, 0.125, 0.25, 0.375, 0.547, 0.72, 0.83, 0.94, 1.0 };
        Color[] gradColors = { new Color(175, 240, 232), new Color(220, 250, 178), new Color(82, 184, 59),
                new Color(101, 148, 52), new Color(240, 157, 1), new Color(139, 11, 0), new Color(104, 40, 15),
                new Color(139, 95, 70), new Color(200, 200, 205) };
        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

    /** Test palette "Mountains 2d" */
    public static ColorGradient CartographyMountain2dGradient() {
        double[] gradPoints = { 0.0, 0.125, 0.25, 0.42, 0.59, 0.72, 0.84, 0.92, 1.0 };
        Color[] gradColors = { new Color(220, 250, 178), new Color(82, 184, 59), new Color(101, 148, 52),
                new Color(240, 157, 1), new Color(139, 11, 0), new Color(104, 40, 15), new Color(139, 95, 70),
                new Color(189, 189, 189), new Color(254, 252, 255) };
        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

    /** Test palette "RGB" */
    public static ColorGradient RGBGradient() {
        double[] gradPoints = { 0.0, 0.25, 0.5, 0.75, 1.0 };
        Color[] gradColors = { new Color(255, 0, 0), new Color(255, 255, 0), new Color(0, 255, 0),
                new Color(0, 255, 255), new Color(0, 0, 255) };
        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

    /** Test palette "RGB Light" */
    public static ColorGradient RGBGradientLight() {
        double[] gradPoints = { 0.0, 0.25, 0.5, 0.75, 1.0 };
        Color[] gradColors = { new Color(255, 90, 90), new Color(255, 255, 90), new Color(90, 255, 90),
                new Color(90, 255, 255), new Color(90, 90, 255) };

        ColorGradient cartographyGradient = null;
        try {
            cartographyGradient = new ColorGradient(gradPoints, gradColors);
        } catch (SOMToolboxException e) {
            // TODO: this does not happen
            e.printStackTrace();
        }
        return cartographyGradient;
    }

}
