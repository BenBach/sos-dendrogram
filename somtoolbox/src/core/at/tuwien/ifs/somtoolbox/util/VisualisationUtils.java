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
package at.tuwien.ifs.somtoolbox.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * This class gathers utility methods for SOM visualisations, such as drawing a unit grid or borders around already
 * created visualisations.
 * 
 * @author Rudolf Mayer
 * @version $Id: VisualisationUtils.java 3696 2010-07-16 13:51:26Z mayer $
 */
public class VisualisationUtils {

    /** draws a black border around the image */
    public static void drawBorder(BufferedImage buffImage) {
        Graphics2D g2D = buffImage.createGraphics();
        int width = buffImage.getWidth();
        int height = buffImage.getHeight();
        g2D.setColor(Color.BLACK);
        g2D.drawLine(0, height - 1, width - 1, height - 1);
        g2D.drawLine(width - 1, 0, width - 1, height - 1);
        g2D.drawLine(0, 0, width - 1, 0);
        g2D.drawLine(0, 0, 0, height - 1);
    }

    /** Draws a thick line of the given width and height, between the given coordinates */
    public static void drawThickLine(Graphics2D g, int x1, int y1, int x2, int y2, int lineWidth, int lineHeight) {
        if (Math.max(lineWidth, lineHeight) == 1) { // draw a normal line
            g.drawLine(x1, y1, x2, y2);
        } else { // draw think line, as a polygon
            int dX = x2 - x1;
            int dY = y2 - y1;
            // line length
            double lineLength = Math.sqrt(dX * dX + dY * dY);

            double scaleX = lineWidth / (2 * lineLength);
            double scaleY = lineHeight / (2 * lineLength);

            // The x,y increments from an endpoint needed to create a rectangle
            double ddx = -scaleX * dY;
            double ddy = scaleY * dX;
            ddx += ddx > 0 ? 0.5 : -0.5;
            ddy += ddy > 0 ? 0.5 : -0.5;
            int dx = (int) ddx;
            int dy = (int) ddy;

            // Now we can compute the corner points
            int xPoints[] = new int[4];
            int yPoints[] = new int[4];

            xPoints[0] = x1 + dx;
            yPoints[0] = y1 + dy;
            xPoints[1] = x1 - dx;
            yPoints[1] = y1 - dy;
            xPoints[2] = x2 - dx;
            yPoints[2] = y2 - dy;
            xPoints[3] = x2 + dx;
            yPoints[3] = y2 + dy;

            g.fillPolygon(xPoints, yPoints, 4);
        }

    }

    /**
     * Draws a think line between the two given points. The line width and height are computed as 1/10 of the given
     * unitWidth/Height values
     */
    public static void drawThickLine(Graphics2D g, Point p1, Point p2, int unitWidth, int unitHeight) {
        // draw the line & circle approx. 1/10 of the unitWidth
        int lineWidth = Math.round(unitWidth / 10);
        int lineHeight = Math.round(unitHeight / 10);
        VisualisationUtils.drawThickLine(g, p1.x, p1.y, p2.x, p2.y, lineWidth, lineHeight);
    }

    /** Draws a thick line from the centre of u1 to u2, using {@link #drawThickLine(Graphics2D, Point, Point, int, int)} */
    public static void drawThickLine(Graphics2D g, Unit u1, Unit u2, int unitWidth, int unitHeight) {
        VisualisationUtils.drawThickLine(g, VisualisationUtils.getUnitCentreLocation(u1, unitWidth, unitHeight),
                VisualisationUtils.getUnitCentreLocation(u2, unitWidth, unitHeight), unitWidth, unitHeight);
    }

    /**
     * Draws a marker in the centre of the given unit. The unitWidth & unitHeight are needed to compute the pixel
     * location, markerWidth and markerHeight indicate the size of the circle/oval to draw.
     */
    public static void drawUnitCentreMarker(Graphics2D g, Unit unit, int unitWidth, int unitHeight, int markerWidth,
            int markerHeight) {
        Point unitCentre = VisualisationUtils.getUnitCentreLocation(unit, unitWidth, unitHeight, markerWidth,
                markerHeight);
        drawMarker(g, markerWidth, markerHeight, unitCentre);
    }

    /** Draws a circle-marker on the given position */
    public static void drawMarker(Graphics2D g, int markerWidth, int markerHeight, Point location) {
        int x = location.x;
        int y = location.y;
        g.fillOval(x, y, markerWidth - 1, markerHeight - 1);
        // FIXME: for some reason, fillOval doesn't work inside the SOMViewer, maybe an issue with piccollo... for that
        // reason, we also use drawOval
        g.drawOval(x, y, markerWidth - 1, markerHeight - 1);
    }

    /** Draws a black grid of units on the {@link BufferedImage} */
    public static void drawUnitGrid(BufferedImage bufferedImage, GrowingSOM gsom, int width, int height) {
        drawUnitGrid((Graphics2D) bufferedImage.getGraphics(), gsom, width, height);
    }

    /** Draws a black grid of units on the {@link Graphics2D} object */
    public static void drawUnitGrid(Graphics2D g, GrowingSOM gsom, int width, int height) {
        VisualisationUtils.drawUnitGrid(g, gsom, width, height, Color.BLACK);
    }

    /**
     * Draws a grid of units on the {@link Graphics2D} object in the given colour. The width of the grid lines depends
     * on the image resolution, and is 1/20 of the unit width.
     */
    public static void drawUnitGrid(Graphics2D g, GrowingSOM gsom, int width, int height, Color colour) {
        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        int lineWidth = Math.max(1, Math.max(unitWidth / 20, unitHeight / 20));

        g.setColor(colour);
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke(lineWidth));

        for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
            for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
                try {
                    if (gsom.getLayer().getUnit(x, y) != null) { // check needed for mnemonic SOMs
                        // to avoid having thinner lines at the edges, we need to do some modifications
                        // 1. start with an offset on the left and upper edge units to have the line to be the same
                        // width
                        int offsetX = x == 0 ? lineWidth / 2 : 0;
                        int offsetY = y == 0 ? lineWidth / 2 : 0;
                        // 2. don't draw the whole width in the edge units, as:
                        // - in the left & upper edge we start with the offset
                        // - in the right & lower edge units, we want the line to be same width, so we need to start it
                        // earlier
                        int gridWidth = gsom.getLayer().isEdgeColumn(x) ? unitWidth - lineWidth / 2 : unitWidth;
                        int gridHeight = gsom.getLayer().isEdgeRow(y) ? unitHeight - lineWidth / 2 : unitHeight;
                        g.drawRect(x * unitWidth + offsetX, y * unitHeight + offsetY, gridWidth, gridHeight);
                    }
                } catch (LayerAccessException e) {
                    // should never happen
                    e.printStackTrace();
                }
            }
        }
        g.setStroke(stroke);
    }

    public static Point getUnitCentreLocation(int xPos, int yPos, double unitWidth, double unitHeight) {
        return new Point((int) ((xPos + 0.5) * unitWidth), (int) ((yPos + 0.5) * unitHeight));
    }

    public static Point getUnitCentreLocation(Unit unit, double unitWidth, double unitHeight) {
        return new Point((int) ((unit.getXPos() + 0.5) * unitWidth), (int) ((unit.getYPos() + 0.5) * unitHeight));
    }

    public static Point getUnitCentreLocation(Unit unit, double unitWidth, double unitHeight, int offsetX, int offsetY) {
        return new Point((int) ((unit.getXPos() + 0.5) * unitWidth) - offsetX / 2,
                (int) ((unit.getYPos() + 0.5) * unitHeight) - offsetY / 2);
    }

}
