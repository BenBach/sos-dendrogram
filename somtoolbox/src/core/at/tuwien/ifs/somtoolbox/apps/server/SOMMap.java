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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import cern.colt.Arrays;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.visualization.AbstractMatrixVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizerInstance;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import at.tuwien.ifs.somtoolbox.visualization.SmoothedDataHistograms;
import at.tuwien.ifs.somtoolbox.visualization.ThematicClassMapVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;

/**
 * @author Rudolf Mayer
 * @version $Id: SOMMap.java 3888 2010-11-02 17:42:53Z frank $
 */
public class SOMMap {

    public String documentDetailLink = "http://wall4.soft.uni-linz.ac.at/fodokat/details";

    public String documentDetailParamName = "fodok_id";

    public String mapLink = "http://wall4.soft.uni-linz.ac.at/fodokat/map";

    public String mapParamName = "fodok_ids";

    private static final Color COLOR_SEARCHRESULT = Color.WHITE;

    private static final String BASICMAP_IMAGES_DIR = "mapImages/base";

    private static final String TEMP_IMAGES_DIR = "mapImages/temp";

    public static final String SEPARATOR = java.io.File.separator;

    private static final int MINIMUM_DIAMETER = 8;

    public static final int DEFAULT_CELL_SIZE = 16;

    public String applicationPath;

    private String imagePathBase;

    private String imagePathTemp;

    // private String imagePathLabels;

    public static final ServerSOM som = new ServerSOM();

    private int cellWidth;

    private int cellHeight;

    private int mapWidth;

    private int mapHeight;

    private Hashtable<String, Object[]> basicMapCache = new Hashtable<String, Object[]>();

    private Hashtable<String, HTMLMapInformation> interactionMapCache = new Hashtable<String, HTMLMapInformation>();

    private Hashtable<String, HTMLMapInformation> sectionMapCache = new Hashtable<String, HTMLMapInformation>();

    private float dotSize = 0.50f;

    private String fullLink;

    static SOMMap singleton;

    static synchronized SOMMap getInstance() throws ServletException {
        if (singleton == null) {
            singleton = new SOMMap();
        }
        System.out.println("get instance");
        return singleton;
    }

    Logger logger = Logger.getLogger("");

    public SOMMap() throws ServletException {
        System.out.println("SOMMap: Service started");
        URL u = this.getClass().getClassLoader().getResource("");
        applicationPath = new File(u.getFile()).getParentFile().getParent();
        imagePathBase = applicationPath + SEPARATOR + BASICMAP_IMAGES_DIR + SEPARATOR;
        imagePathTemp = applicationPath + SEPARATOR + TEMP_IMAGES_DIR + SEPARATOR;
        // imagePathLabels = applicationPath + SEPARATOR + LABEL_IMAGES_DIR + SEPARATOR;
        System.out.println("Application path: " + applicationPath + "; image path basics: " + imagePathBase
                + "; image path temp: " + imagePathTemp);
        som.setDefaultPalette(Palettes.getDefaultPalette());
        som.load(applicationPath);
    }

    public HTMLMapInformation createMapSection(HttpServletRequest request, String vis, Palette palette,
            int additionalVisParam, boolean showNodes, boolean showGrid, String fodokID, int paramMapWidth,
            int paramMapHeight) throws LayerAccessException {
        initMap(paramMapWidth, paramMapHeight, palette);
        String markedMapKey = getMapSelectionKey(vis, palette, fodokID);
        int zoom = 1; // TODO: make constant / dynamic
        if (sectionMapCache.get(markedMapKey) == null) {
            String imageMap;
            String[] nNearest = null;
            System.out.println("Creating map section: " + markedMapKey);
            BufferedImage mapImage = getBasicMap(vis, palette, additionalVisParam, zoom, showNodes, showGrid, true);

            Graphics2D gra = mapImage.createGraphics();
            gra.setColor(Color.WHITE);

            int offsetX = 0;
            int offsetY = 0;
            int diameter = getDiameter(zoom);
            int factorWidth = cellWidth * 1;
            int cellOffsetX = (factorWidth - diameter) / 2;
            int factorHeight = cellHeight * 1;
            int cellOffsetY = (factorHeight - diameter) / 2;

            Unit targetUnit = som.growingSOM.getLayer().getUnitForDatum(fodokID);
            System.out.println("zoom: " + zoom);

            // if we show a single unit, we get the smaller map
            if (targetUnit != null) {
                int x = targetUnit.getXPos();
                int y = targetUnit.getYPos();
                System.out.println("target unit: " + x + "/" + y);
                drawMarker(gra, COLOR_SEARCHRESULT, zoom, offsetX, offsetY, diameter, cellOffsetX, cellOffsetY, x, y);
                int mapRadius = 3;
                int startX = Math.max(x - mapRadius, 0);
                int endX = Math.min(x + mapRadius, som.growingSOM.getLayer().getXSize());
                int startY = Math.max(y - mapRadius, 0);
                int endY = Math.min(y + mapRadius, som.growingSOM.getLayer().getYSize());

                System.out.println("locations: " + new Rectangle(startX, startY, endX, endY));

                int subImageX = (startX - offsetX) * factorWidth;
                int subImageY = (startY - offsetY) * factorHeight;
                int subImageWidth = (endX - startX) * factorWidth;
                int subImageHeight = (endY - startY) * factorHeight;

                System.out.println("image: " + new Rectangle(subImageX, subImageY, subImageWidth, subImageHeight));
                mapImage = mapImage.getSubimage(subImageX, subImageY, subImageWidth, subImageHeight);
                imageMap = createImageMap(zoom, startX, startY, endX, endY, fodokID, fullLink);
                if (som.inputDataObjects.getInputData() != null) {
                    try {
                        nNearest = som.growingLayer.getNNearestInputs(fodokID, 5, som.inputDataObjects.getInputData());
                    } catch (MetricException e) {
                        nNearest = som.growingLayer.getNNearestInputs(targetUnit, 5);
                        e.printStackTrace();
                    }
                } else {
                    nNearest = som.growingLayer.getNNearestInputs(targetUnit, 5);
                }
            } else {
                if (fodokID != null) {
                    System.out.println("** Did not find unit for " + fodokID);
                }
                imageMap = createImageMap(zoom, offsetX, offsetY, fullLink);
            }

            try {
                String mapFileName = markedMapKey + ".png";
                File mapImageFile = new File(imagePathTemp + SEPARATOR + mapFileName);
                boolean success = ImageIO.write(mapImage, "png", mapImageFile);
                if (!success) {
                    System.out.println("*** Could not write temp image " + mapImageFile.getAbsolutePath());
                }

                sectionMapCache.put(markedMapKey, new HTMLMapInformation(TEMP_IMAGES_DIR + "/" + mapFileName, imageMap,
                        nNearest));
                System.out.println("Put to cache:" + markedMapKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Getting from cache: " + markedMapKey);
        return sectionMapCache.get(markedMapKey);
    }

    private int getDiameter(int zoom) {
        int diameter;
        if (cellWidth < cellHeight) {
            diameter = Math.round(cellWidth * dotSize * zoom) - 2;
        } else {
            diameter = Math.round(cellHeight * dotSize * zoom) - 2;
        }
        if (diameter < MINIMUM_DIAMETER) {
            diameter = MINIMUM_DIAMETER;
        }
        return diameter;
    }

    public HTMLMapInformation createFullMap(HttpServletRequest request, String vis, Palette palette,
            int additionalVisParam, int paramMapWidth, int paramMapHeight, int zoom, int moveX, int moveY,
            Point selection, boolean showGrid, boolean showNodes, boolean showLabels, String fodokIDs, String fullLink,
            Rectangle areaSelection) throws LayerAccessException {
        initMap(paramMapWidth, paramMapHeight, palette);
        ((ThematicClassMapVisualizer) Visualizations.getVisualizationByName("ClassMap").getVis()).setZoom(cellWidth
                * zoom);

        String interactionMapKey = getInteractionMapKey(fodokIDs,
                getMapKey(vis, palette, moveX, moveY, zoom, showLabels, showGrid), selection);

        if (interactionMapCache.get(interactionMapKey) == null) {
            System.out.println("Creating map for ids: " + interactionMapKey);
            BufferedImage mapImage = getBasicMap(vis, palette, additionalVisParam, zoom, showNodes, showGrid,
                    showLabels);

            Graphics2D gra = mapImage.createGraphics();

            gra.setColor(Color.WHITE);

            int offsetX = 0;
            int offsetY = 0;
            int diameter = getDiameter(zoom);

            int factorWidth = cellWidth * zoom;
            int cellOffsetX = (factorWidth - diameter) / 2;
            int factorHeight = cellHeight * zoom;
            int cellOffsetY = (factorHeight - diameter) / 2;

            // we have an area selected
            if (areaSelection != null) {
                int beginX = areaSelection.x / (cellWidth * zoom) + offsetX;
                int beginY = areaSelection.y / (cellHeight * zoom) + offsetY;
                int endX = (areaSelection.x + areaSelection.width) / (cellWidth * zoom) + offsetX;
                int endY = (areaSelection.y + areaSelection.height) / (cellHeight * zoom) + offsetY;
                System.out.println("selection: " + areaSelection);
                System.out.println("map: " + new Rectangle(beginX, beginY, endX, endY));
            }

            // calculate offset of visible map begin, according to zoom level and movement in X / Y directions
            int visibleX = som.somdata.getXSize() / zoom;
            int visibleY = som.somdata.getYSize() / zoom;

            System.out.println("visible: " + visibleX + "x" + visibleY);

            int middleX = visibleX * zoom / 2;
            int middleY = visibleY * zoom / 2;
            System.out.println("middle: " + middleX + "x" + middleY);

            double windowX = (double) som.somdata.getXSize() / (double) zoom / 2;
            double windowY = (double) som.somdata.getYSize() / (double) zoom / 2;
            System.out.println("window: " + windowX + "x" + windowY);
            System.out.println("move: " + moveX + "," + moveY);
            if (zoom > 1) {
                offsetX = (int) Math.round(middleX + (moveX - 1) * windowX);
                offsetY = (int) Math.round(middleY + (moveY - 1) * windowY);
                System.out.println("offset calc: " + (middleX + (moveX - 1) * windowX) + "x"
                        + (middleY + (moveY - 1) * windowY));
                System.out.println("offset calc: " + offsetX + "x" + offsetY);
            }
            if (zoom == 2) {
                offsetX = getOffsetZoom2(moveX, som.somdata.getXSize());
                offsetY = getOffsetZoom2(moveY, som.somdata.getYSize());
            } else if (zoom == 3) {
                offsetX = getOffsetZoom3(moveX, som.somdata.getXSize());
                offsetY = getOffsetZoom3(moveY, som.somdata.getYSize());
            }
            System.out.println("offset manual: " + offsetX + "x" + offsetY);

            int extraOffSetX = 0;
            int extraOffSetY = 0;

            if (zoom > 1) {
                int realWidth = mapWidth + 1;
                int realHeight = mapHeight + 1;
                // int subImageX = offsetX * cellWidth;
                // int subImageY = offsetY * cellHeight;
                // min-check to prevent going over right or lower border
                // int subImageX = Math.min(offsetX * cellWidth * zoom, (mapImage.getWidth() - (realWidth)));
                // int subImageY = Math.min(offsetY * cellHeight * zoom, (mapImage.getHeight() - (realHeight)));
                int subImageX = offsetX * cellWidth * zoom;
                int subImageY = offsetY * cellHeight * zoom;
                int maxOffsetX = mapImage.getWidth() - realWidth;
                int maxOffsetY = mapImage.getHeight() - realHeight;

                if (subImageX > maxOffsetX) {
                    extraOffSetX = subImageX - mapImage.getWidth() - realWidth;
                    subImageX = maxOffsetX;
                }
                if (subImageY > maxOffsetY) {
                    extraOffSetY = subImageY - mapImage.getHeight() - realHeight;
                    subImageY = maxOffsetY;
                }
                // int subImageWidth = visibleX * cellWidth * zoom;
                // int subImageHeight = visibleY * cellHeight;
                System.out.println("coordinates: " + new Rectangle(subImageX, subImageY, realWidth, realHeight));

                mapImage = mapImage.getSubimage(subImageX, subImageY, realWidth, realHeight);

                // BufferedImage section = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_INT_RGB);
                // Image scaledInstance = mapImage.getScaledInstance(mapWidth, mapHeight, Image.SCALE_SMOOTH);
                // section.createGraphics().drawImage(scaledInstance, 0, 0, section.getWidth(), section.getHeight(),
                // null);
                // mapImage = section;
                gra = mapImage.createGraphics();
            }
            String imageMap = createImageMap(zoom, offsetX, offsetY, offsetX + visibleX, offsetY + visibleY, null,
                    fullLink);

            ArrayList<Unit> markedUnits = new ArrayList<Unit>();
            if (fodokIDs != null) {
                String[] fodokIds = fodokIDs.split(",");
                System.out.println("processing ids:" + Arrays.toString(fodokIds));
                for (String fodokId : fodokIds) {
                    Unit targetUnit = som.growingSOM.getLayer().getUnitForDatum(fodokId);
                    if (targetUnit != null) {
                        if (!markedUnits.contains(targetUnit)) {
                            markedUnits.add(targetUnit);
                        }
                    } else {
                        System.out.println("***** Did not find target unit for " + fodokId);
                    }
                }
                for (int i = 0; i < markedUnits.size(); i++) {
                    Unit u = markedUnits.get(i);
                    if (zoom > 1
                            && (u.getXPos() < offsetX || u.getXPos() > offsetX + visibleX || u.getYPos() < offsetY || u.getYPos() > offsetY
                                    + visibleY)) {
                        System.out.println("Unit " + u.getXPos() + "/" + u.getYPos() + " out of bounds.");
                    } else {
                        System.out.println("marking unit " + u.getXPos() + "/" + u.getYPos());
                        // System.out.println("x: " + x + ", y: " + y);
                        // System.out.println("offsetX: " + offsetX + ", offsetY: " + offsetY);
                        // System.out.println("factorWidth: " + (cellWidth * zoom) + ", factorHeight: " + (cellHeight *
                        // zoom));
                        drawMarker(gra, COLOR_SEARCHRESULT, zoom, offsetX, offsetY, diameter, cellOffsetX, cellOffsetY,
                                u.getXPos(), u.getYPos());
                        // System.out.println("filling: " + (xPos + 1) + "," + (yPos + 1) + "," + diameter + "," +
                        // diameter);
                    }
                }
            }
            System.out.println("selected: " + selection);
            if (selection != null) {
                try {
                    Unit selectedUnit = som.growingSOM.getLayer().getUnit(selection.x, selection.y);
                    if (markedUnits.contains(selectedUnit)) {
                        drawMarker(gra, Color.BLUE, zoom, offsetX, offsetY, diameter, cellOffsetX, cellOffsetY,
                                selectedUnit.getXPos(), selectedUnit.getYPos());
                    } else {
                        drawMarker(gra, Color.DARK_GRAY, zoom, offsetX, offsetY, diameter, cellOffsetX, cellOffsetY,
                                selectedUnit.getXPos(), selectedUnit.getYPos());
                    }
                } catch (LayerAccessException e) {
                    System.out.println("*** Tried to access non-existing unit " + selection);
                }
            }

            try {
                File mapImageFile = new File(imagePathTemp + interactionMapKey.hashCode() + ".png");
                boolean success = ImageIO.write(mapImage, "png", mapImageFile);
                if (!success) {
                    System.out.println("*** Could not write temp file " + mapImageFile.getAbsolutePath());
                }
                interactionMapCache.put(interactionMapKey,
                        new HTMLMapInformation(TEMP_IMAGES_DIR + "/" + mapImageFile.getName(), imageMap));
                System.out.println("put to cache:" + interactionMapKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Getting from cache: " + interactionMapKey);
        return interactionMapCache.get(interactionMapKey);
    }

    private int getOffsetZoom3(int move, float visible) {
        int offset = 0;
        if (move == -2) {
            offset = 0;
        } else if (move == -1) {
            offset = Math.round(visible / 6.0f);
        } else if (move == 0) {
            offset = Math.round(visible / 3.0f);
        } else if (move == 1) {
            offset = Math.round(visible / 2.0f);
        } else if (move == 2) {
            offset = (int) (visible - visible / 3);
        }
        return offset;
    }

    private int getOffsetZoom2(int move, float visible) {
        if (move == -1) {
            return 0;
        } else if (move == 0) {
            return Math.round(visible / 4.0f);
        } else if (move == 1) {
            return Math.round(visible / 2.0f);
        }
        return 0;
    }

    public HTMLMapInformation createFullMap(HttpServletRequest request, String vis, Palette palette,
            int additionalVisParam, boolean showNodes, boolean showGrid, boolean showLables, String fodokIDs,
            int paramMapWidth, int paramMapHeight, String fullLink) throws LayerAccessException {
        return createFullMap(request, vis, palette, additionalVisParam, paramMapWidth, paramMapHeight, 1, 0, 0, null,
                showGrid, showNodes, showLables, fodokIDs, fullLink, null);
    }

    private void initMap(int paramMapWidth, int paramMapHeight, Palette palette) {
        som.setDefaultPalette(palette);
        if (paramMapWidth != -1) {
            cellWidth = cellHeight = paramMapWidth / som.growingLayer.getXSize();
        } else {
            cellWidth = DEFAULT_CELL_SIZE;
        }

        if (paramMapHeight != -1) {
            cellWidth = cellHeight = paramMapHeight / som.growingLayer.getYSize();
        } else {
            cellHeight = DEFAULT_CELL_SIZE;
        }

        mapWidth = cellWidth * som.growingLayer.getXSize();
        mapHeight = cellHeight * som.growingLayer.getYSize();
    }

    public static boolean isNotEmpty(String s) {
        return s != null && s.trim().length() > 0;
    }

    public static boolean isNotEmptyNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private BufferedImage getBasicMap(String vis, Palette palette, int additionalVisParam, int zoom, boolean showNodes,
            boolean showGrid, boolean showLabels) {
        String basicMapKey = getBasicMapKey(vis, palette, zoom, showGrid, showLabels);
        String basicMapFileName = basicMapKey + "_overview.png";

        // create the basic visualisation,
        if (basicMapCache.get(basicMapKey) == null) {
            System.out.println("Creating basic map: " + basicMapKey);
            BufferedImage baseMapImage;

            BackgroundImageVisualizer visualizer;

            BackgroundImageVisualizerInstance visualisation = Visualizations.getVisualizationByName(vis);
            if (visualisation == null) {
                System.out.println("*** SOMMap: No visualization selected");
                return null;
            }

            visualizer = visualisation.getVis();
            int var = visualisation.getVariant();

            som.setDefaultPalette(palette);

            try {
                if (visualizer instanceof AbstractMatrixVisualizer) {
                    ((AbstractMatrixVisualizer) visualizer).setPalette(som.getDefaultPalette());
                    // FIXME: why reverse?
                    ((AbstractMatrixVisualizer) visualizer).reversePalette();
                }

                // set visualisation dependend parameter
                if (visualizer instanceof SmoothedDataHistograms) {
                    ((SmoothedDataHistograms) visualizer).setSmoothingFactor(additionalVisParam);
                }

                BufferedImage visImage = visualizer.getVisualization(var, som.growingSOM, mapWidth * zoom, mapHeight
                        * zoom);
                baseMapImage = new BufferedImage(mapWidth * zoom + 1, mapHeight * zoom + 1, BufferedImage.TYPE_INT_RGB);
                baseMapImage.createGraphics().drawImage(visImage, 0, 0, visImage.getWidth(), visImage.getHeight(), null);

                Graphics gra = baseMapImage.getGraphics();

                // draw the grid
                gra.setColor(Color.WHITE);
                if (showGrid == true) {
                    for (int x = 0; x < som.somdata.getXSize(); x++) {
                        gra.drawLine((x * cellWidth * zoom), 0, (x * cellWidth * zoom), mapHeight * zoom);
                    }
                    for (int y = 0; y < som.somdata.getYSize(); y++) {
                        gra.drawLine(0, (y * cellHeight * zoom), mapWidth * zoom, (y * cellHeight * zoom));
                    }
                }

                System.out.println("showLabels getbasci: " + showLabels);
                System.out.println("Labels path: " + ServerSOM.labelsPath);
                // add the labels
                if (showLabels) {
                    File file = new File(ServerSOM.labelsPath + "label-zoom" + zoom + ".png");
                    try {
                        BufferedImage labels = ImageIO.read(file);
                        gra.drawImage(labels, 0, 0, baseMapImage.getWidth(), baseMapImage.getHeight(), 0, 0,
                                labels.getWidth(), labels.getHeight(), null);
                    } catch (IOException e) {
                        System.out.println("Could not read labels image: " + e.getMessage() + ", file: "
                                + file.getAbsolutePath());
                    }
                }

                File mapImageFile = new File(imagePathBase + SEPARATOR + basicMapFileName);
                boolean success = ImageIO.write(baseMapImage, "png", mapImageFile);
                if (!success) {
                    System.out.println("*** Could not write base image " + mapImageFile.getAbsolutePath());
                }
                basicMapCache.put(basicMapKey, new Object[] { basicMapFileName, baseMapImage });
                System.out.println("Put to cache:" + basicMapKey);
            } catch (IOException e) {
                System.out.println("SOMMap: Couldn`t create SOM Overview.");
                e.printStackTrace();
                return null;
            } catch (SOMToolboxException e) {
                System.out.println("SOMMap: Couldn`t create SOM Overview.");
                e.printStackTrace();
                return null;
            }
        }

        System.out.println("Getting from cache: " + basicMapKey);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(imagePathBase + SEPARATOR + basicMapFileName));
        } catch (IOException e) {
            System.out.println("*** Tried to read: " + imagePathBase + SEPARATOR + basicMapFileName);
            e.printStackTrace();
        }
        return bufferedImage;
    }

    private void drawMarker(Graphics2D gra, Color fillColor, int zoom, int offsetX, int offsetY, int diameter,
            int cellOffsetX, int cellOffsetY, int unitX, int unitY) {
        int xPos = (unitX - offsetX) * cellWidth * zoom + cellOffsetX;
        int yPos = (unitY - offsetY) * cellHeight * zoom + cellOffsetY;
        gra.setColor(Color.black);
        gra.fillOval(xPos, yPos, diameter + 2, diameter + 2);
        gra.setColor(fillColor);
        gra.fillOval(xPos + 1, yPos + 1, diameter, diameter);
        System.out.println("Filling marker: " + xPos + "/" + yPos + ", diameter: " + diameter);
    }

    private String getBasicMapKey(String vis, Palette palette, int zoom, boolean showGrid, boolean showLabels) {
        vis = Visualizations.getVisualizationShortName(vis);
        return mapWidth * zoom + "x" + mapHeight * zoom + "_" + vis + "_" + palette.getShortName() + "_zoom-" + zoom
                + "_grid-" + showGrid + "_labels-" + showLabels;
    }

    private String getMapSelectionKey(String vis, Palette palette, String fodokID) {
        vis = Visualizations.getVisualizationShortName(vis);
        return mapWidth + "x" + mapHeight + "_" + vis + "_" + palette.getShortName() + "_fodokId-" + fodokID;
    }

    private String getInteractionMapKey(String fodokID, String mapKey, Point selection) {
        String key = mapKey;
        if (fodokID != null) {
            key += "_" + fodokID;
        }
        if (selection != null) {
            key += "_x" + selection.x;
            key += "_y" + selection.y;
        }
        return key;
    }

    private String getMapKey(String vis, Palette palette, int moveX, int moveY, int zoom, boolean showLabels,
            boolean showGrid) {
        vis = Visualizations.getVisualizationShortName(vis);
        return mapWidth + "x" + mapHeight + "_" + vis + "_" + palette.getShortName() + "_zoom-" + zoom + "_movex-"
                + moveX + "_movey-" + moveY + "_grid-" + showGrid + "_labels-" + showLabels;
    }

    public String createImageMap(int zoom, int offsetX, int offsetY, String fullLink) throws LayerAccessException {
        return createImageMap(zoom, offsetX, offsetY, som.somdata.getXSize(), som.somdata.getYSize(), null, fullLink);
    }

    public String createImageMap(int zoom, int offsetX, int offsetY, int endX, int endY, String fodokId, String fullLink)
            throws LayerAccessException {
        return createImageMap(zoom, offsetX, offsetY, 0, 0, endX, endY, fodokId, fullLink);
    }

    public String createImageMap(int zoom, int offsetX, int offsetY, int extraOffsetX, int extraOffsetY, int endX,
            int endY, String fodokId, String fullLink) throws LayerAccessException {
        System.out.println("creating image map. zoom: " + zoom + ", offsetX: " + offsetX + ", offsetY: " + offsetY
                + ", endX: " + endX + ", endY: " + endY);
        StringBuffer imageMap = new StringBuffer("<map name=\"som-map\">\n");
        for (int x = offsetX; x < endX; x++) {
            for (int y = offsetY; y < endY; y++) {
                StringBuffer thisUnit = new StringBuffer("<area shape=\"rect\" ");
                int coordX = (x - offsetX) * cellWidth * zoom + extraOffsetX;
                int coordY = (y - offsetY) * cellHeight * zoom + extraOffsetY;
                thisUnit.append("coords=\"" + coordX + "," + coordY + "," + (coordX + cellWidth * zoom) + ","
                        + (coordY + cellHeight * zoom) + "\" ");
                Unit u = som.growingSOM.getLayer().getUnit(x, y);
                StringBuffer label = new StringBuffer(x + "/" + y + ":").append(u.getUnitLabels());
                thisUnit.append("title=\"" + label.toString() + "\" class=\"tooltip2\"");
                // if (isNotEmpty(fodokId)) {
                // thisUnit.append("href=\"http://wall4.soft.uni-linz.ac.at/fodokat/map?fodok_id=" + fodokId + "\" ");
                // } else {
                thisUnit.append("href=\"" + fullLink + "&x=" + x + "&y=" + y + "\" ");
                // }
                thisUnit.append("/>\n");
                imageMap.append(thisUnit.toString());
            }
        }
        imageMap.append("</map>\n");
        return imageMap.toString();
    }

    public String getVisualisationSelect(String vis, String selected) {
        StringBuffer b = new StringBuffer(2 * 150);
        b.append("<select style=\"font-size: xx-small;\" name=\"visualisation\" onchange=\"this.form.submit()\">\n");
        addVis(vis, vis, selected, b);
        addVis(vis, "Thematic Class Map", selected, b);
        b.append("</select>\n");
        return b.toString();
    }

    private void addVis(String defaultVis, String vis, String selected, StringBuffer b) {
        BackgroundImageVisualizerInstance v = Visualizations.getVisualizationByName(vis);
        BackgroundImageVisualizer backgroundImageVisualizer = v.getVis();
        int index = v.getVariant();
        String visualizationName = backgroundImageVisualizer.getVisualizationName(index);
        String[] additionalFiles = backgroundImageVisualizer.needsAdditionalFiles();
        if (additionalFiles == null || additionalFiles.length == 0) { // only show vis that are functional
            b.append("<option style=\"font-size: x-small;\" value=\"" + visualizationName + "\"");
            if (visualizationName.equals(selected) || StringUtils.isBlank(selected) && vis.equals(defaultVis)) {
                b.append(" selected ");
            }
            b.append(">" + visualizationName + "</option>\n");
        }

    }

    public String getClassLegend() {
        SOMLibClassInformation classInfo = som.inputDataObjects.getClassInfo();
        StringBuffer b = new StringBuffer(2 * 150);
        if (classInfo != null) {
            b.append("<table>\n");
            Color[] colors = classInfo.getClassColors();
            for (int i = 0; i < colors.length; i++) {
                b.append("<tr>\n");
                // b.append("<td stlye=\"background-color: rgb(" + colors[i].getRed() + "," + colors[i].getGreen() + ","
                // + colors[i].getBlue()
                // + ")\" >&nbsp;&nbsp;&nbsp;");
                b.append("<td>");
                b.append("<span style=\"background-color: rgb(" + colors[i].getRed() + "," + colors[i].getGreen() + ","
                        + colors[i].getBlue() + ")\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;");
                b.append("</td>");
                b.append("<td>" + at.tuwien.ifs.somtoolbox.util.StringUtils.beautifyForHTML(classInfo.classNames()[i])
                        + "</td>");
                b.append("</tr>\n");
            }
            b.append("</table>\n");
        }
        return b.toString();
    }
}
