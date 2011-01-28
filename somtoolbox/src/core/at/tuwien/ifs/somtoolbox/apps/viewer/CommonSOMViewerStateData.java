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

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JFileChooser;

import at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractSelectionPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractViewerControl;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MapDetailPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.MIMETypes;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.properties.SOMViewerProperties;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterNode;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusteringTree;

/**
 * This class stores common viewer state data, and is shared among the different panels of the SOMViewer application.
 * 
 * @author Rudolf Mayer
 * @version $Id: CommonSOMViewerStateData.java 3765 2010-08-20 13:40:21Z mayer $
 */
public class CommonSOMViewerStateData {
    private static CommonSOMViewerStateData instance = null; // Angela - the single state instance of the application

    // static pre- and suffix for relative filenames
    public static String fileNamePrefix = "";

    public static String fileNameSuffix = "";

    // SOM Comparision
    public String secondSOMName = "";

    // for exporting the correct variant
    public int currentVariant;

    public JFileChooser fileChooser;

    public static MIMETypes MimeTypes = new MIMETypes();

    public ArrayList<Component> registeredViewerControls = new ArrayList<Component>();

    public Hashtable<String, Component> registeredComponentWindows = new Hashtable<String, Component>();

    public SharedSOMVisualisationData inputDataObjects;

    public SOMInputReader somInputReader;

    public boolean colorClusters = false; // do we want the clusters colored or just with a border?

    public boolean labelsWithValues = false;

    public int clusterWithLabels = 0;

    public double clusterByValue = 1;

    public float clusterBorderWidthMagnificationFactor = ClusteringTree.INITIAL_BORDER_WIDTH_MAGNIFICATION_FACTOR;

    public Color clusterBorderColour = ClusterNode.INTIAL_BORDER_COLOUR;

    public boolean hideUnitDetails = false;

    public Frame parentFrame;

    public GrowingLayer growingLayer;

    public MapPNode mapPNode;

    public GrowingSOM growingSOM;

    public int controlElementsWidth = 0; // to be set by application

    // map-detail specific information
    public int[] thresholdInputPercentage = { 100, 100, 100, 100 };

    public double[] scaleLimits = { 0, 0.4, 0.7, 1.5 };

    public boolean labelVisibilityMode = true;

    public boolean hitsVisibilityMode = true;

    public boolean dataVisibilityMode = true;

    public boolean exactUnitPlacement = false;

    public boolean exactUnitPlacementEnabled = true;

    public boolean shiftOverlappingInputs = false;

    public boolean displayInputLinkage = false;

    private String classPiechartMode = SOMViewer.TOGGLE_PIE_CHARTS_SHOW;

    /**
     * The maximum yOffset for labels in any of the units. Used to have uniform sizes of the pie-charts in
     * {@link GeneralUnitPNode}.
     */
    public double[] maxLabelYOffset = new double[GeneralUnitPNode.NUMBER_OF_DETAIL_LEVELS];

    public static SOMViewerProperties somViewerProperties = new SOMViewerProperties();

    public AbstractSelectionPanel selectionPanel;

    public int numClusters = 1;

    // end map-detail specific information

    public CommonSOMViewerStateData(CommonSOMViewerStateData other) {
        this(other.parentFrame);
        this.controlElementsWidth = other.controlElementsWidth;
        this.dataVisibilityMode = other.dataVisibilityMode;
        this.fileChooser = other.fileChooser;
        this.labelVisibilityMode = other.labelVisibilityMode;
        // copy the map-detail control panel. FIXME: maybe we should copy all windows to the other state?
        this.registeredComponentWindows.put("Map Detail Control",
                other.registeredComponentWindows.get("Map Detail Control"));
        this.somInputReader = other.somInputReader;
        this.growingSOM = other.growingSOM;
        this.growingLayer = other.growingLayer;
    }

    public CommonSOMViewerStateData(SOMViewer viewer, int width) {
        this(viewer);
        controlElementsWidth = width;
        instance = this; // Angela - simple singleton pattern
    }

    // Angela - allows the object deserializer to find the state of the application
    public static CommonSOMViewerStateData getInstance() {
        if (instance == null) {
            instance = new CommonSOMViewerStateData();
        }
        return instance;
    }

    public CommonSOMViewerStateData(Frame viewer) {
        this();
        this.parentFrame = viewer;
    }

    public CommonSOMViewerStateData() {
        inputDataObjects = new SharedSOMVisualisationData();
        instance = this;
    }

    public void registerComponentWindow(Component component, String name) {
        if (component instanceof AbstractViewerControl) {
            registeredViewerControls.add(component);
        }
        registeredComponentWindows.put(name, component);
    }

    public MapDetailPanel getMapDetailPanel() {
        return (MapDetailPanel) registeredComponentWindows.get("Map Detail Control");
    }

    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    public SOMViewerProperties getSOMViewerProperties() {
        return somViewerProperties;
    }

    public SOMViewer getSOMViewer() {
        if (parentFrame instanceof SOMViewer) {
            return (SOMViewer) parentFrame;
        }
        return null;
    }

    public String getClassPiechartMode() {
        return classPiechartMode;
    }

    public void setClassPiechartMode(String classPiechartMode) {
        this.classPiechartMode = classPiechartMode;
    }

}
