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

/**
 * Interface for visualisation of matrix data of/on a SOM. If you want to provide new matrix visualisers, you have to
 * implement this interface and register your visualisation in {@link Visualizations#getAvailableVisualizations()}. If
 * your visualisation requires user input to e.g. control parameters, extend
 * {@link at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer.VisualizationControlPanel} to add
 * your specific control panel inputs.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @author Thomas Lidy
 * @version $Id: MatrixVisualizer.java 3583 2010-05-21 10:07:41Z mayer $
 */
public interface MatrixVisualizer {

    /**
     * Gets the currently used palette.
     * 
     * @return the currently used palette
     */
    public Color[] getPalette();

    /**
     * Sets a new palette.
     * 
     * @param palette the new palette
     */
    public void setPalette(Palette palette);

    /**
     * Reverts the currently used palette.
     */
    public void reversePalette();

    /** Returns the {@link Palette} this visualisation currently is set to. */
    public Palette getCurrentPalette();

    /** Returns the name of the {@link Palette} this visualisation prefers as initial palette. */
    public String getPreferredPaletteName();

}
