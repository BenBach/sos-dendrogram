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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizerInstance;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;

public class SOMPageParameters {
    public boolean allowMoveDown;

    public boolean allowMoveLeft;

    public boolean allowMoveRight;

    public boolean allowMoveUp;

    public int cellHeight;

    public int cellWidth;

    public boolean dataMode;

    public boolean expertMode;

    public String fodokId = null;

    public String fodokIds = null;

    public boolean hasXSelection = false;

    public boolean hasYSelection = false;

    public int mapHeight = -1;

    public int mapWidth = -1;

    int moveX;

    int moveY;

    public String paletteDefault = "RGB, 256 Gradient";// "Grayscale, 16 Gradient";

    public Palette palette = Palettes.getPaletteByName(paletteDefault);

    public String paletteParam = null;

    private int selectedX = -1;

    private int selectedY = -1;

    private boolean showGridDefault = false;

    public boolean showGrid = showGridDefault;

    private boolean showLabelsDefault = false;

    public boolean showLabels = showLabelsDefault;

    private boolean showNodesDefault = false;

    public boolean showNodes = showNodesDefault;

    public int smoothingFactorDefault = 10;

    public int smoothingFactor = smoothingFactorDefault;

    public String visDefault = "WeightedSDHNorm"; // "Weighted SDH";// "D-Matrix";// "Classic U-Matrix";//

    // "Smoothed Data Histograms";

    public String vis = visDefault;

    public String visualisationParam = null;

    public int zoom = 1;

    public String basicParams;

    public String linkParams;

    public String basicLink;

    String fullLink;

    private int beginSelectionX = -1;

    private int beginSelectionY = -1;

    private int endSelectionX = -1;

    private int endSelectionY = -1;

    public Point pointSelection;

    public Rectangle areaSelection;

    public Dimension move;

    public SOMPageParameters(HttpServletRequest request) {
        parseRequest(request);

        basicParams = "";
        if (selectedX != -1) {
            basicParams += "&x=" + selectedX;
        }
        if (selectedY != -1) {
            basicParams += "&y=" + selectedY;
        }
        if (expertMode) {
            basicParams += "&expertMode";
        }
        if (dataMode) {
            basicParams += "&dataMode";
        }
        if (StringUtils.isNotBlank(paletteParam)) {
            basicParams += "&palette=" + request.getParameter("palette");
        }
        if (StringUtils.isNotBlank(visualisationParam)) {
            basicParams += "&visualisation=" + request.getParameter("visualisation");
        }
        linkParams = basicParams + "&moveX=" + moveX + "&moveY=" + moveY + "&zoom=" + zoom;
        basicLink = "map?";
        if (StringUtils.isNotBlank(fodokIds)) {
            basicLink += "fodokIds=" + fodokIds;
        }
        if (basicLink.endsWith("?") && basicParams.startsWith("&")) {
            basicLink += basicParams.substring(1);
        } else {
            basicLink += basicParams;
        }

        fullLink = "map?";
        if (StringUtils.isNotBlank(fodokIds)) {
            fullLink += "fodokIds=" + fodokIds + "&";
        }
        if (StringUtils.isNotBlank(fodokId)) {
            fullLink += "fodokId=" + fodokId + "&";
        }
        fullLink += "moveX=" + moveX + "&moveY=" + moveY + "&zoom=" + zoom;
        if (StringUtils.isNotBlank(request.getParameter("palette"))) {
            fullLink += "&palette=" + request.getParameter("palette");
        }
        if (StringUtils.isNotBlank(request.getParameter("visualisation"))) {
            fullLink += "&visualisation=" + request.getParameter("visualisation");
        }

        if (request.getParameter("expertMode") != null) {
            fullLink += "&expertMode";
        }
        if (request.getParameter("dataMode") != null) {
            fullLink += "&dataMode";
        }
        System.out.println("Using palette: " + palette.getName());

    }

    public void parseRequest(HttpServletRequest request) {

        vis = visDefault;
        String paramVisualisation = request.getParameter("visualisation");
        if (StringUtils.isNotBlank(paramVisualisation)) {
            BackgroundImageVisualizerInstance visualisationTemp = Visualizations.getVisualizationByName(paramVisualisation);
            if (visualisationTemp != null) {
                vis = paramVisualisation;
            }
        }
        palette = Palettes.getPaletteByName(paletteDefault);
        String paramPalette = request.getParameter("palette");
        if (StringUtils.isNotBlank(paramPalette)) {
            Palette p = Palettes.getPaletteByName(paramPalette);
            if (p != null) {
                palette = p;
            }
        }

        String paramSmoothing = request.getParameter("smoothingFactor");
        if (StringUtils.isNotBlank(paramSmoothing)) {
            smoothingFactor = Integer.parseInt(paramSmoothing);
        } else {
            smoothingFactor = smoothingFactorDefault;
        }

        String paramShowGrid = request.getParameter("showGrid");
        if (StringUtils.isNotBlank(paramShowGrid)) {
            showGrid = Boolean.valueOf(paramShowGrid).booleanValue();
        } else {
            showGrid = showGridDefault;
        }
        String paramShowNodes = request.getParameter("showNodes");
        if (StringUtils.isNotBlank(paramShowNodes)) {
            showNodes = Boolean.valueOf(paramShowNodes).booleanValue();
        } else {
            showNodes = showNodesDefault;
        }
        String paramShowLables = request.getParameter("showLabels");
        if (StringUtils.isNotBlank(paramShowLables)) {
            showLabels = Boolean.valueOf(paramShowLables).booleanValue();
        } else {
            showLabels = showLabelsDefault;
        }

        if (StringUtils.isNumeric(request.getParameter("zoom"))) {
            zoom = Integer.parseInt(request.getParameter("zoom"));
        } else {
            zoom = 1;
        }

        String paramMapWidth = request.getParameter("mapWidth");
        if (StringUtils.isNumeric(paramMapWidth)) {
            mapWidth = Integer.parseInt(paramMapWidth);
        } else {
            mapWidth = -1;
        }

        String paramMapHeight = request.getParameter("mapHeight");
        if (StringUtils.isNumeric(paramMapHeight)) {
            mapHeight = Integer.parseInt(paramMapHeight);
        } else {
            mapHeight = -1;
        }

        try {
            moveX = Integer.parseInt(request.getParameter("moveX"));
        } catch (NumberFormatException e) {
            moveX = 0;
        }
        try {
            moveY = Integer.parseInt(request.getParameter("moveY"));
        } catch (NumberFormatException e) {
            moveY = 0;
        }
        move = new Dimension(moveX, moveY);
        System.out.println("MOVE: " + move);

        selectedX = -1;
        if (StringUtils.isNumeric(request.getParameter("x"))) {
            selectedX = Integer.parseInt(request.getParameter("x"));
        }
        selectedY = -1;
        if (StringUtils.isNumeric(request.getParameter("y"))) {
            selectedY = Integer.parseInt(request.getParameter("y"));
        }
        if (selectedX != -1 && selectedY != -1) {
            pointSelection = new Point(selectedX, selectedY);
        }

        if (StringUtils.isNumeric(request.getParameter("beginSelectionX"))) {
            beginSelectionX = Integer.parseInt(request.getParameter("beginSelectionX"));
        }
        if (StringUtils.isNumeric(request.getParameter("beginSelectionY"))) {
            beginSelectionY = Integer.parseInt(request.getParameter("beginSelectionY"));
        }
        if (StringUtils.isNumeric(request.getParameter("endSelectionX"))) {
            endSelectionX = Integer.parseInt(request.getParameter("endSelectionX"));
        }
        if (StringUtils.isNumeric(request.getParameter("endSelectionY"))) {
            endSelectionY = Integer.parseInt(request.getParameter("endSelectionY"));
        }
        if (beginSelectionX != -1 && beginSelectionY != -1) {
            areaSelection = new Rectangle(beginSelectionX, beginSelectionY, endSelectionX, endSelectionY);
        }

        if (request.getParameter("expertMode") != null) {
            expertMode = true;
        }
        if (request.getParameter("dataMode") != null) {
            dataMode = true;
        }

        if (StringUtils.isNotBlank(request.getParameter("palette"))) {
            paletteParam = request.getParameter("palette");
        }
        if (StringUtils.isNotBlank(request.getParameter("visualisation"))) {
            visualisationParam = request.getParameter("visualisation");
        }

        if (StringUtils.isNotBlank(request.getParameter("fodokId"))) {
            fodokId = request.getParameter("fodokId");
        }
        if (StringUtils.isNotBlank(request.getParameter("fodokIds"))) {
            fodokIds = request.getParameter("fodokIds");
        }

        allowMoveUp = zoom > 1 && moveY * -1 + 1 < zoom;
        allowMoveDown = zoom > 1 && moveY + 1 < zoom;
        allowMoveLeft = zoom > 1 && moveX * -1 + 1 < zoom;
        allowMoveRight = zoom > 1 && moveX + 1 < zoom;

    }

    public void setShowGridDefault(boolean showGridDefault) {
        this.showGridDefault = showGridDefault;
    }

    public int getSmoothingFactorDefault() {
        return smoothingFactorDefault;
    }

    public void setSmoothingFactorDefault(int smoothingFactorDefault) {
        this.smoothingFactorDefault = smoothingFactorDefault;
    }

    public void setPaletteDefault(String paletteDefault) {
        this.paletteDefault = paletteDefault;
    }

    public void setVisDefault(String visDefault) {
        this.visDefault = visDefault;
    }

    public void setShowLabelsDefault(boolean showLabelsDefault) {
        this.showLabelsDefault = showLabelsDefault;
    }
}