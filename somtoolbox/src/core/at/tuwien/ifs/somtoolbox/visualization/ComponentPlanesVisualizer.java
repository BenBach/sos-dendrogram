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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * This visualiser provides a visualisation of component planes, i.e. of the template vector elements.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: ComponentPlanesVisualizer.java 3871 2010-10-27 08:43:07Z frank $
 */
public class ComponentPlanesVisualizer extends AbstractItemVisualizer {

    private SOMLibTemplateVector templateVector = null;

    public ComponentPlanesVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Component Planes" };
        VISUALIZATION_SHORT_NAMES = new String[] { "ComponentPlanes" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Visualization of component planes." };
        neededInputObjects = new String[] { SOMVisualisationData.TEMPLATE_VECTOR };
        setInterpolate(false);
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR + "component:"
                + currentElement;
    }

    public BufferedImage createVisualization(int index, int plane, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        currentElement = plane;
        return createVisualization(index, gsom, width, height);
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        if (templateVector == null) {
            templateVector = gsom.getSharedInputObjects().getTemplateVector();
            if (templateVector == null) {
                throw new SOMToolboxException("You need to specify the " + neededInputObjects[0]);
            }
        }

        if (!(controlPanel instanceof ComponentPlaneControlPanel)) {
            // create control panel once we have the template vector, and if it is a generic panel
            controlPanel = new ComponentPlaneControlPanel(this, templateVector);
        }

        if (index == 0) {
            return createComponentPlaneImage(gsom, width, height);
        } else {
            return null;
        }
    }

    private BufferedImage createComponentPlaneImage(GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        DoubleMatrix2D plane = new DenseDoubleMatrix2D(gsom.getLayer().getComponentPlane(currentElement, 0));
        plane = plane.viewDice();
        VectorTools.normalise(plane);
        return createImage(gsom, plane, width, height, interpolate);
    }

    /**
     * A control panel extending the generic {@link AbstractBackgroundImageVisualizer.VisualizationControlPanel}, adding
     * additionally a {@link JList} and a {@link JTextField} for selecting a component from the {@link TemplateVector}.
     * 
     * @author Rudolf Mayer
     */
    private class ComponentPlaneControlPanel extends AbstractSelectedItemVisualizerControlPanel implements
            ActionListener, ListSelectionListener {
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a new component-plane control panel
         * 
         * @param vis The ComponentPlanesVisualizer listening to updates from the list box.
         * @param templateVector The {@link TemplateVector} containing the components.
         */
        private ComponentPlaneControlPanel(ComponentPlanesVisualizer vis, SOMLibTemplateVector templateVector) {
            super("Comp. Planes Control");

            JPanel compPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();

            initialiseList(templateVector.getLabels());
            JScrollPane listScroller = new JScrollPane(list);
            listScroller.setPreferredSize(new Dimension(CommonSOMViewerStateData.getInstance().controlElementsWidth,
                    150));
            listScroller.setMaximumSize(new Dimension(CommonSOMViewerStateData.getInstance().controlElementsWidth, 150));

            constr.gridwidth = GridBagConstraints.REMAINDER;
            constr.fill = GridBagConstraints.BOTH;
            constr.weightx = 1.0;
            constr.weighty = 1.0;
            compPanel.add(listScroller, constr);

            text.setToolTipText("Enter a (part) of a component plane name, and start the search with the <enter> key");
            text.setText(templateVector.getLabel(currentElement));
            compPanel.add(text, constr);

            add(compPanel, c);
        }
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        return getVisualizationFlavours(index, gsom, width, height, gsom.getSharedInputObjects().getInputData().dim());
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            int maxFlavours) throws SOMToolboxException {
        int currentPlane = currentElement; // save the currently selected element & template vector
        SOMLibTemplateVector currentTemplateVector = templateVector;
        templateVector = gsom.getSharedInputObjects().getTemplateVector();

        HashMap<String, BufferedImage> result = new HashMap<String, BufferedImage>();
        for (int i = 0; i < templateVector.dim() && i < maxFlavours; i++) {
            currentElement = i;
            result.put("_" + templateVector.getLabel(i), getVisualization(index, gsom, width, height));
        }
        currentElement = currentPlane;// set the selected element & template vector back to the original
        templateVector = currentTemplateVector;
        return result;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException {
        // FIXME: Implement this
        return super.getVisualizationFlavours(index, gsom, width, height, flavourParameters);
    }

    @Override
    public String getPreferredPaletteName() {
        return "RGB256";
    }

}
