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
package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;
import at.tuwien.ifs.somtoolbox.visualization.ComponentPlanesVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;

/**
 * This class implements ordered display and clustering of SOM Component Planes. The components planes are transformed
 * to vectors, and are subsequently either displayed in their order, or clustered on a new SOM.
 * 
 * @author Arnaud Moreau
 * @author Peter Vorlaufer
 * @author Rudolf Mayer
 * @version $Id: ComponentPlaneClusteringFrame.java 3984 2010-12-21 16:30:25Z frank $
 */
public class ComponentPlaneClusteringFrame extends JFrame implements ActionListener, ChangeListener {
    private static final String CLUSTER = "Clustering";

    private static final String DISPLAY = "Display ordered";

    private static final long serialVersionUID = 1L;

    private AbstractSOMLibSparseInputData input;

    private SOMProperties props;

    private String[] labels;

    private GrowingSOM orginalSom;

    private GenericPNodeScrollPane pane;

    private JSpinner spinnerXSize;

    private JSpinner spinnerYSize;

    private SpinnerNumberModel spinnerNumberModelXSize;

    private SpinnerNumberModel spinnerNumberModelYSize;

    /** A cache for already trained SOMs. */
    private Hashtable<String, ComponentPlaneClustering> clusteredMapCache = new Hashtable<String, ComponentPlaneClustering>();

    private PNode unclusteredComponentPNodeWithNames;

    private PNode unclusteredComponentPNodeWithOutNames;

    final int uHeight = MapPNode.DEFAULT_UNIT_HEIGHT;

    final int uWidth = MapPNode.DEFAULT_UNIT_WIDTH;

    private int dim;

    private SOMLibTemplateVector tv;

    private SOMViewer somViewer;

    private CommonSOMViewerStateData state;

    private ButtonGroup buttons;

    private int padding = 12;

    private JCheckBox checkboxShowComponentNames;

    public ComponentPlaneClusteringFrame(SOMViewer somViewer, GrowingSOM orginalSom, SOMLibTemplateVector tv)
            throws SOMToolboxException {
        super("Component Plane Clustering");
        this.orginalSom = orginalSom;
        this.somViewer = somViewer;
        this.tv = tv;
        GrowingLayer layer = orginalSom.getLayer();
        dim = tv.dim();
        // create covariance matrix from CPs
        DoubleMatrix2D cov = this.getCov(layer);
        labels = new String[dim];
        InputDatum[] newData = new InputDatum[dim];
        // extract feature names and save new training vectors
        for (int i = 0; i < dim; i++) {
            labels[i] = tv.getLabel(i);
            newData[i] = new InputDatum(labels[i], cov.viewColumn(i), cov.viewColumn(i).cardinality());
        }

        // compute new x=y SOM Size
        int newSOMAxisSize = (int) Math.ceil(Math.sqrt(dim)) + 1;

        // create new Input Data for the SOM
        input = AbstractSOMLibSparseInputData.create(newData, null);
        tv = new SOMLibTemplateVector(input.numVectors(), input.dim());

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JRadioButton radioButtonDisplay = new JRadioButton(DISPLAY);
        radioButtonDisplay.setActionCommand(DISPLAY);
        radioButtonDisplay.addActionListener(this);
        topPanel.add(radioButtonDisplay);

        JRadioButton radioButtonCluster = new JRadioButton(CLUSTER);
        radioButtonCluster.setActionCommand(CLUSTER);
        radioButtonCluster.addActionListener(this);
        topPanel.add(radioButtonCluster);

        topPanel.add(new JLabel("xSize"));
        spinnerNumberModelXSize = new SpinnerNumberModel(newSOMAxisSize, 1, 50, 1);
        spinnerXSize = new JSpinner(spinnerNumberModelXSize);
        spinnerXSize.setEnabled(false);
        spinnerXSize.addChangeListener(this);
        topPanel.add(spinnerXSize);

        topPanel.add(new JLabel("ySize"));
        spinnerNumberModelYSize = new SpinnerNumberModel(newSOMAxisSize, 1, 50, 1);
        spinnerYSize = new JSpinner(spinnerNumberModelYSize);
        spinnerYSize.setEnabled(false);
        spinnerYSize.addChangeListener(this);
        topPanel.add(spinnerYSize);

        buttons = new ButtonGroup();
        buttons.add(radioButtonDisplay);
        buttons.add(radioButtonCluster);
        radioButtonDisplay.setSelected(true);

        JButton buttonSave = new JButton("Save");
        buttonSave.setToolTipText("Save the component plane pane to an image file");
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExportUtils.saveMapPaneAsImage(getParent(), ComponentPlaneClusteringFrame.this.state.getFileChooser(),
                        pane, "Save MapPane as PNG");
            }
        });
        topPanel.add(buttonSave);

        checkboxShowComponentNames = new JCheckBox("Show component names", true);
        checkboxShowComponentNames.addActionListener(this);
        topPanel.add(checkboxShowComponentNames);

        contentPane.add(topPanel, BorderLayout.NORTH);

        state = new CommonSOMViewerStateData(somViewer.getSOMViewerState());
        state.inputDataObjects = new SharedSOMVisualisationData();
        state.inputDataObjects.setData(SOMVisualisationData.TEMPLATE_VECTOR, tv);

        unclusteredComponentPNodeWithOutNames = createUnclusteredPane(somViewer, tv, layer, false);
        unclusteredComponentPNodeWithNames = createUnclusteredPane(somViewer, tv, layer, true);
        pane = new GenericPNodeScrollPane(state, unclusteredComponentPNodeWithNames);
        // Set initial pane size...
        pane.setPreferredSize(unclusteredComponentPNodeWithNames.getFullBounds().getBounds().getSize());

        contentPane.add(pane, BorderLayout.CENTER);
    }

    private ComponentPlaneClustering createClusteredPane(SOMViewer parent, SOMLibTemplateVector tv, GrowingLayer layer)
            throws SOMToolboxException {

        int xSize = spinnerNumberModelXSize.getNumber().intValue();
        int ySize = spinnerNumberModelYSize.getNumber().intValue();

        // check if SOM size can hold all CPs
        if (xSize * ySize < layer.getDim()) {
            throw new SOMToolboxException("Size of map (" + xSize + "x" + ySize
                    + ") can't be smaller than number of dimensions (" + layer.getDim() + ") !");
        }

        // specify Properties of new SOM
        try {
            int iterations = Math.max(1000, input.numVectors() * 100);
            props = new SOMProperties(xSize, ySize, 7, 0, iterations, 0.7, 0, 1, "", true);
        } catch (PropertiesException pe) {
            pe.printStackTrace();
        }

        // create Layer and train it
        GrowingSOM cpsom = new GrowingSOM(false, props, input);
        cpsom.train(input, props);

        // check if there are multiple items on one Unit
        reStructureMap(cpsom);

        CommonSOMViewerStateData state = new CommonSOMViewerStateData(parent.getSOMViewerState());
        state.inputDataObjects = new SharedSOMVisualisationData();
        state.inputDataObjects.setData(SOMVisualisationData.TEMPLATE_VECTOR, tv);
        return new ComponentPlaneClustering(cpsom, makeComponentPNode(createComponentPlanesVisualizer(state), cpsom));
    }

    public PNode makeComponentPNode(ComponentPlanesVisualizer visualizer, GrowingSOM cpsom) throws SOMToolboxException {
        final GrowingLayer layer = cpsom.getLayer();
        PNode componentImages = createPNode(layer.getXSize(), layer.getYSize());

        // make a map grid
        for (int x = 0; x < layer.getXSize(); x++) {
            for (int y = 0; y < layer.getYSize(); y++) {
                PPath rect = PPath.createRectangle((float) x * (uWidth + padding), (float) y * (uHeight + padding),
                        (uWidth + padding), (uHeight + padding));
                componentImages.addChild(rect);
            }
        }

        // draw all component images
        for (int i = 0; i < labels.length; i++) {
            Unit u = layer.getUnitForDatum(labels[i]);
            createComponentImage(visualizer, componentImages, i, u.getXPos(), u.getYPos(), false);
        }
        return componentImages;
    }

    private PNode createUnclusteredPane(SOMViewer parent, SOMLibTemplateVector tv, GrowingLayer layer,
            boolean showComponentNames) throws SOMToolboxException {
        int neededXSize = (int) Math.ceil(Math.sqrt(dim));
        int neededYSize = (int) Math.floor(Math.sqrt(dim));
        PNode componentImages = createPNode(neededXSize, neededYSize);

        // draw all component images
        for (int i = 0; i < labels.length; i++) {
            int xPos = i % neededXSize;
            int yPos = i / neededXSize;
            createComponentImage(createComponentPlanesVisualizer(state), componentImages, i, xPos, yPos,
                    showComponentNames);
        }
        return componentImages;
    }

    private ComponentPlanesVisualizer createComponentPlanesVisualizer(CommonSOMViewerStateData state) {
        ComponentPlanesVisualizer vis = new ComponentPlanesVisualizer();
        vis.setInputObjects(state.inputDataObjects);
        vis.setPalette(Palettes.getPaletteByName("RGB256"));
        return vis;
    }

    private void createComponentImage(ComponentPlanesVisualizer visualizer, PNode componentImages, int componentIndex,
            int xPos, int yPos, boolean showComponentNames) throws SOMToolboxException {
        BufferedImage bimg = visualizer.createVisualization(0, componentIndex, orginalSom,
                orginalSom.getLayer().getXSize() * 10, orginalSom.getLayer().getYSize() * 10);
        int textHeight = 15;
        if (showComponentNames) { // also display component names?
            PText componentName = new PText(labels[componentIndex]);
            double width2 = componentName.getWidth();
            componentName.setHeight(textHeight);
            componentImages.addChild(componentName);
            componentName.moveToFront();
            componentName.translate((uWidth - width2) / 2 + (uWidth + padding) * xPos + padding / 2,
                    (uHeight + padding + textHeight) * yPos + padding / 2 - 0.2 * textHeight);
        }
        PImage img = new PImage(bimg);
        img.addAttribute("tooltip", "Component #" + componentIndex + ", '" + labels[componentIndex] + "'");
        img.setWidth(uWidth);
        img.setHeight(uHeight);
        componentImages.addChild(img);
        img.moveToFront();
        img.translate((uWidth + padding) * xPos + padding / 2, (uHeight + padding) * yPos
                + (showComponentNames ? textHeight * (yPos + 1) : 0) + padding / 2);
    }

    private PNode createPNode(int xSize, int ySize) {
        PNode componentImages = new PNode();
        componentImages.setWidth(xSize * (uWidth + padding) + 2);
        componentImages.setHeight(ySize * (uHeight + padding) + 2);
        return componentImages;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        update();

        // set the minimum spinner value to prevent a map size smaller than the number of dimensions
        spinnerNumberModelXSize.setMinimum((int) Math.ceil(dim / spinnerNumberModelYSize.getNumber().doubleValue()));
        spinnerNumberModelYSize.setMinimum((int) Math.ceil(dim / spinnerNumberModelXSize.getNumber().doubleValue()));
    }

    private void update() {
        if (buttons.getSelection().getActionCommand() == DISPLAY) {
            if (checkboxShowComponentNames.isSelected()) {
                pane.setPNode(unclusteredComponentPNodeWithNames);
            } else {
                pane.setPNode(unclusteredComponentPNodeWithOutNames);
            }
        } else {
            String key = spinnerNumberModelXSize.getNumber() + "x" + spinnerNumberModelYSize.getNumber();
            if (clusteredMapCache.get(key) == null) {
                try {
                    clusteredMapCache.put(key, createClusteredPane(somViewer, tv, orginalSom.getLayer()));
                    pane.setPNode(clusteredMapCache.get(key).vis);
                } catch (SOMToolboxException e1) {
                    e1.printStackTrace();
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "Error creating component plane clustering: " + e1.getMessage());
                }
            } else {
                pane.setPNode(clusteredMapCache.get(key).vis);
            }
        }
        boolean enableSpinner = buttons.getSelection().getActionCommand() != DISPLAY;
        spinnerXSize.setEnabled(enableSpinner);
        spinnerYSize.setEnabled(enableSpinner);
    }

    private DoubleMatrix2D getCov(GrowingLayer layer) {
        // serialise CPs in new matrix (rows = number of units, columns = dimension)
        DenseDoubleMatrix2D matrix = new DenseDoubleMatrix2D(layer.getXSize() * layer.getYSize(), layer.getDim());
        for (int i = 0; i < layer.getDim(); i++) {
            double[][] cp = layer.getComponentPlane(i);
            for (int n = 0; n < layer.getXSize(); n++) {
                for (int m = 0; m < layer.getYSize(); m++) {
                    matrix.setQuick(n * layer.getYSize() + m, i, cp[n][m]);
                }
            }
        }
        // compute covariance matrix
        DoubleMatrix2D covariance = Statistic.covariance(matrix);

        // normalise covariance matrix, to have diagonals of 1
        DoubleMatrix2D diagonal = new DenseDoubleMatrix2D(covariance.columns(), 1);
        for (int i = 0; i < covariance.columns(); i++) {
            diagonal.setQuick(i, 0, covariance.getQuick(i, i));
        }
        Algebra algebra = new Algebra();
        DoubleMatrix2D mult = algebra.mult(diagonal, algebra.transpose(diagonal));
        mult.assign(Functions.sqrt);

        covariance.assign(mult, Functions.div);

        return covariance;
    }

    private void reStructureMap(GrowingSOM cpsom) {
        int doubleUnits = 0;
        int iter = 0;
        InputData d = cpsom.getLayer().getData();
        do {
            Unit[] units = cpsom.getLayer().getAllUnits();
            doubleUnits = 0;
            for (Unit unit : units) {
                String[] lab = unit.getMappedInputNames();
                if (lab != null) {
                    if (lab.length > 1) {
                        doubleUnits++;
                        for (int j = 1; j < lab.length; j++) {
                            InputDatum da = d.getInputDatum(lab[j]);
                            Unit[] winners = cpsom.getLayer().getWinners(da, 2 + iter);
                            winners[1 + iter].addMappedInput(da, true);
                            unit.removeMappedInput(lab[j]);
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                                    "Moving " + lab[j] + " from " + unit + " to " + winners[1 + iter]);
                        }
                    }
                }
            }
            iter++;
        } while (doubleUnits > 0);
    }

    private class ComponentPlaneClustering {
        private GrowingSOM som;

        private PNode vis;

        public ComponentPlaneClustering(GrowingSOM som, PNode vis) {
            this.som = som;
            this.vis = vis;
        }
    }
}
