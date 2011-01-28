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

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMInputReader;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer.VisualizationControlPanel;

/**
 * Interface defining a visualisation of a SOM. If you want to provide new visualisers, you have to implement this
 * interface and register your visualisation in {@link Visualizations#getAvailableVisualizations()}. If your
 * visualisation requires user input to e.g. control parameters, extend
 * {@link at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer.VisualizationControlPanel} to add
 * your specific control panel inputs.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: BackgroundImageVisualizer.java 3888 2010-11-02 17:42:53Z frank $
 */
public interface BackgroundImageVisualizer extends Comparable<BackgroundImageVisualizer> {

    public static final int DEFAULT_BACKGROUND_VISUALIZATION_SCALE = 13;

    /**
     * Returns a visualisation image.
     * 
     * @param variantIndex the index of the variant to use
     * @param gsom the GrowingSOM to take build the visualisation for
     * @param width the desired width of the image, in pixels
     * @param height the desired height of the image, in pixels
     * @return an image for this visualisation
     * @throws SOMToolboxException If there was an error creating the visualisation
     */
    public BufferedImage getVisualization(int variantIndex, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException;

    /**
     * @return Returns the number of visualizations
     */
    public int getNumberOfVisualizations();

    /**
     * Gets the names of all visualisation variants provided by this visualiser.
     * 
     * @return a String array containing all names
     */
    public String[] getVisualizationNames();

    /**
     * Gets the name of a specified visualisation variant.
     * 
     * @param variantIndex the index of the variant to use
     * @return the name of the visualisation variant
     */
    public String getVisualizationName(int variantIndex);

    /**
     * Gets the short names of all visualisation variants provided by this visualiser.
     * 
     * @return a String array containing all short names
     */
    public String[] getVisualizationShortNames();

    /**
     * Gets the short name of a specified visualisation variant.
     * 
     * @param variantIndex the index of the variant to use
     * @return the short name of the visualisation variant
     */
    public String getVisualizationShortName(int variantIndex);

    /**
     * Gets the descriptions of all visualisation variants provided by this visualiser.
     * 
     * @return a String array containing all descriptions
     */
    public String[] getVisualizationDescriptions();

    /**
     * Gets the descriptions of a specified visualisation variant.
     * 
     * @param variantIndex the index of the variant to use
     * @return the description of the visualisation variant
     */
    public String getVisualizationDescription(int variantIndex);

    /**
     * Gets the visualisation control panel.
     * 
     * @return the control panel of this visualizer
     */
    public VisualizationControlPanel getControlPanel();

    /**
     * Checks whether this visualisation still needs some input files to generate an image.
     * 
     * @return an array containing the names of the input objects needed
     */
    public String[] needsAdditionalFiles();

    /**
     * Sets a new listener for visualisation update events.
     * 
     * @param listener the new listener to be registered
     */
    public void setVisualizationUpdateListener(VisualizationUpdateListener listener);

    /**
     * Sets the input objects needed to create visualisations.
     * 
     * @see at.tuwien.ifs.somtoolbox.data.SOMVisualisationData
     * @param inputObjects the new input objects
     */
    public void setInputObjects(SharedSOMVisualisationData inputObjects);

    /**
     * Sets the input data needed to create visualisations.
     * 
     * @param reader the som input reader
     */
    public void setSOMData(SOMInputReader reader);

    /**
     * Sets the map this visualiser operates on.
     * 
     * @param map the map
     */
    public void setMap(MapPNode map);

    /**
     * Returns HTML control elements (inputs) to be used by the webserver version of the SOM. Only the inputs are
     * required, the surrounding form will be provided.
     * 
     * @param params the parameters as passed by the web request - used to select the values in the inputs.
     * @return HTML code containing the inputs
     */
    @SuppressWarnings("rawtypes")
    public String getHTMLVisualisationControl(Map params);

    /**
     * Return the preferred scale factor for interpolation for this visualisation. This is useful for visualisations
     * that do not want to be interpolated, or at least less interpolated, e.g. when they draw lines rather than having
     * a matrix-height profile.
     */
    public int getPreferredScaleFactor();

    /**
     * Returns all visualisation flavours of the given variant, e.g. applying possible parameters to the visualisation,
     * etc.<br/>
     * The keys in the map shall be specific suffixes that describe how the flavours were constructed, e.g. they might
     * contain parameter names and values.
     */
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException;

    /**
     * Returns all visualisation flavours of the given variant just as
     * {@link #getVisualizationFlavours(int, GrowingSOM, int, int)}, but limiting the number of flavours to the given
     * maximum number.
     */
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            int maxFlavours) throws SOMToolboxException;

    /**
     * Returns all visualisation flavours of the given variant just as
     * {@link #getVisualizationFlavours(int, GrowingSOM, int, int)}, but limiting the number of flavours by the given
     * parameters.
     */
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException;

}
