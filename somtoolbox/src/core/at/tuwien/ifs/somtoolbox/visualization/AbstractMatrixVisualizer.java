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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.logging.Level;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import flanagan.interpolation.BiCubicSplineFast;

import at.tuwien.ifs.commons.util.MathUtils;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.visualization.contourplot.ContourPlot;

/**
 * @author Thomas Lidy
 * @author Rudolf Mayer
 * @version $Id: AbstractMatrixVisualizer.java 3883 2010-11-02 17:13:23Z frank $
 */
public abstract class AbstractMatrixVisualizer extends AbstractBackgroundImageVisualizer implements MatrixVisualizer {

    protected Palette palette = Palettes.getPaletteByName(getPreferredPaletteName());

    protected double minimumMatrixValue = -1;

    protected double maximumMatrixValue = -1;

    public double getMinimumMatrixValue() {
        return minimumMatrixValue;
    }

    public double getMaximumMatrixValue() {
        return maximumMatrixValue;
    }

    /**
     * overriding the method in the superclass as we have a different cache key, and to set the min & max matrix values
     * to -1
     */
    @Override
    public BufferedImage getVisualization(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        if (controlPanel != null) { // we don't always have this initialised, especially when we just create the
            // visualisation w/o the viewer
            controlPanel.updateZDim(gsom.getLayer().getZSize());
        }
        String cacheKey = getCacheKey(gsom, index, width, height);
        logImageCache(cacheKey);
        if (cache.get(cacheKey) == null) {
            minimumMatrixValue = Double.MAX_VALUE;
            maximumMatrixValue = Double.MIN_VALUE;
            cache.put(cacheKey, createVisualization(index, gsom, width, height));
        }
        return cache.get(cacheKey);
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height)
                + CACHE_KEY_SECTION_SEPARATOR
                + buildCacheKey("palette:" + palette.getShortName(), "reversed:" + palette.isReversed(), "interpolate:"
                        + interpolate, "contour:"
                        + contourMode
                        + (contourMode != ContourMode.None ? "/" + contourInterpolationMode + "/" + numberOfContours
                                : ""));
    }

    protected void setInterpolate(boolean interpolate) {
        this.interpolate = interpolate;
        if (controlPanel != null) { // we don't always have this initialised, especially when we just create the
            // visualisation w/o the viewer
            this.controlPanel.interpolateCheckbox.setSelected(interpolate);
        }
    }

    protected void drawContour(Graphics2D g, DoubleMatrix2D matrix, int width, int height, boolean fill)
            throws SOMToolboxException {
        ContourPlot plot = new ContourPlot(matrix.columns(), matrix.rows(), width, height);
        plot.setFill(fill);
        plot.setPalette(palette.getColors());
        plot.setNumberOfContours(numberOfContours);
        plot.setLogInterpolation(contourInterpolationMode == ContourInterpolationMode.Log);
        plot.setZedMatrix(matrix);
        plot.paint(g);
    }

    /**
     * Creates an image from a matrix of heights.
     * 
     * @param gsom The GrowingSOM to generate the image for
     * @param matrix The matrix with the calucalted heights.
     * @param width the desired width of the image, in pixels
     * @param height the desired height of the image, in pixels.
     * @param interpolate indicates whether the image should be interpolated if the widht or height exceeds the matrix
     *            dimensions.
     * @return the BufferedImage for those settings
     */
    protected BufferedImage createImage(GrowingSOM gsom, DoubleMatrix2D matrix, int width, int height,
            boolean interpolate) throws SOMToolboxException {
        /** drawing stuff * */
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        if (contourMode == ContourMode.Full) {
            drawContour(g, matrix, width, height, true);
            return res;
        }

        drawBackground(width, height, g);

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        if (interpolate) {
            BiCubicSplineFast bcs = computeSpline(gsom, matrix, width, height, unitWidth, unitHeight);

            int elevation = 0;
            int stepSize = Math.max(5000, height * width / 500);
            StdErrProgressWriter progress = new StdErrProgressWriter(height * width,
                    "Creating interpolated matrix image, pixel ", stepSize);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // adapted to mnemonic (sparse) SOMs
                    try {
                        if (gsom.getLayer().getUnit((x / unitWidth), (y / unitHeight)) != null) {
                            elevation = (int) Math.round(bcs.interpolate(y, x) * palette.maxColourIndex());
                            g.setPaint(palette.getColorConstrained(elevation));
                            g.fill(new Rectangle(x, y, 1, 1));
                            if (elevation < minimumMatrixValue) {
                                minimumMatrixValue = elevation;
                            }
                            if (elevation > maximumMatrixValue) {
                                maximumMatrixValue = elevation;
                            }
                        } else { // we show an empty (white) unit if this unit is not part of the mnemonic map
                            g.setPaint(Color.WHITE);
                            g.fill(new Rectangle(x, y, 1, 1));
                        }
                        progress.progress();
                    } catch (LayerAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            /** end bicubic spline stuff * */
        } else {
            double factorX = (double) matrix.columns() / (double) gsom.getLayer().getXSize();
            double factorY = (double) matrix.rows() / (double) gsom.getLayer().getYSize();
            g.setColor(null);
            int ci = 0;
            int xOff = 0;
            int yOff = 0;
            if (factorX != 1 && factorY != 1) {
                xOff = (int) Math.round(unitWidth / (factorX * 2));
                yOff = (int) Math.round(unitHeight / (factorY * 2));
            }

            for (int y = 0; y < matrix.rows(); y++) {
                for (int x = 0; x < matrix.columns(); x++) {
                    ci = (int) Math.round(matrix.get(y, x) * palette.maxColourIndex());
                    Color color = palette.getColor(ci);
                    g.setPaint(color);
                    log.log(Level.FINER, "{0}/{1} => matrix value: {2}, colorIndex: {3}, colour {4}", new Object[] { y,
                            x, matrix.get(y, x), ci, color });

                    g.fill(new Rectangle(xOff + x * (int) Math.round(unitWidth / factorX), yOff + y
                            * (int) Math.round(unitHeight / factorY), (int) Math.round(unitWidth / factorX),
                            (int) Math.round(unitHeight / factorY)));
                    if (ci < minimumMatrixValue) {
                        minimumMatrixValue = ci;
                    }
                    if (ci > maximumMatrixValue) {
                        maximumMatrixValue = ci;
                    }
                }
            }

            if (factorX != 1 && factorY != 1) { // border
                ci = (int) Math.round(matrix.get(0, 0) * palette.maxColourIndex()); // top-left
                g.fill(new Rectangle(0, 0, (int) Math.round(unitWidth / factorX * 2), (int) Math.round(unitHeight
                        / (factorY * 2))));
                ci = (int) Math.round(matrix.get(0, matrix.columns() - 1) * palette.maxColourIndex()); // top-right
                g.fill(new Rectangle(xOff + matrix.columns() * (int) Math.round(unitWidth / factorX), 0,
                        (int) Math.round(unitWidth / (factorX * 2)), (int) Math.round(unitHeight / (factorY * 2))));
                ci = (int) Math.round(matrix.get(matrix.rows() - 1, 0) * palette.maxColourIndex()); // bottom-left
                g.fill(new Rectangle(0, yOff + matrix.rows() * (int) Math.round(unitHeight / factorY),
                        (int) Math.round(unitWidth / (factorX * 2)), (int) Math.round(unitHeight / (factorY * 2))));
                ci = (int) Math.round(matrix.get(matrix.rows() - 1, matrix.columns() - 1) * palette.maxColourIndex()); // bottom-right
                g.fill(new Rectangle(xOff + matrix.columns() * (int) Math.round(unitWidth / factorX), yOff
                        + matrix.rows() * (int) Math.round(unitHeight / factorY), (int) Math.round(unitWidth
                        / (factorX * 2)), (int) Math.round(unitHeight / (factorY * 2))));
                for (int x = 0; x < matrix.columns(); x++) {
                    // top border
                    ci = (int) Math.round(matrix.get(0, x) * palette.maxColourIndex());
                    g.setPaint(palette.getColor(ci));
                    g.fill(new Rectangle(xOff + x * (int) Math.round(unitWidth / factorX), 0,
                            (int) Math.round(unitWidth / factorX), (int) Math.round(unitHeight / (factorY * 2))));
                    // bottom border
                    ci = (int) Math.round(matrix.get(matrix.rows() - 1, x) * palette.maxColourIndex());
                    g.setPaint(palette.getColor(ci));
                    g.fill(new Rectangle(xOff + x * (int) Math.round(unitWidth / factorX), (yOff + matrix.rows()
                            * (int) Math.round(unitHeight / factorY)), (int) Math.round(unitWidth / factorX),
                            (int) Math.round(unitHeight / (factorY * 2))));
                }
                for (int y = 0; y < matrix.rows(); y++) {
                    // left border
                    ci = (int) Math.round(matrix.get(y, 0) * palette.maxColourIndex());
                    g.setPaint(palette.getColor(ci));
                    g.fill(new Rectangle(0, yOff + y * (int) Math.round(unitHeight / factorY),
                            (int) Math.round(unitWidth / (factorX * 2)), (int) Math.round(unitHeight / factorY)));
                    // right border
                    ci = (int) Math.round(matrix.get(y, matrix.columns() - 1) * palette.maxColourIndex());
                    g.setPaint(palette.getColor(ci));
                    g.fill(new Rectangle((xOff + matrix.columns() * (int) Math.round(unitWidth / factorX)), yOff + y
                            * (int) Math.round(unitHeight / factorY), (int) Math.round(unitWidth / (factorX * 2)),
                            (int) Math.round(unitHeight / factorY)));
                }
            }
        }

        if (contourMode == ContourMode.Overlay) {
            drawContour(g, matrix, width, height, false);
        }

        return res;
    }

    protected BiCubicSplineFast computeSpline(GrowingSOM gsom, DoubleMatrix2D matrix, int width, int height,
            int unitWidth, int unitHeight) {
        DoubleMatrix2D matrixBorders = new DenseDoubleMatrix2D(matrix.rows() + 2, matrix.columns() + 2);
        matrixBorders.viewPart(1, 1, matrix.rows(), matrix.columns()).assign(matrix);
        matrixBorders.viewRow(0).assign(matrixBorders.viewRow(1));
        matrixBorders.viewRow(matrixBorders.rows() - 1).assign(matrixBorders.viewRow(matrixBorders.rows() - 2));
        matrixBorders.viewColumn(0).assign(matrixBorders.viewColumn(1));
        matrixBorders.viewColumn(matrixBorders.columns() - 1).assign(
                matrixBorders.viewColumn(matrixBorders.columns() - 2));

        /** start bicubic spline stuff * */
        // create support points
        double factorX = (double) (matrixBorders.columns() - 2) / (double) gsom.getLayer().getXSize();
        double factorY = (double) (matrixBorders.rows() - 2) / (double) gsom.getLayer().getYSize();
        double[] x1 = new double[matrixBorders.columns()];
        x1[0] = 0;
        for (int x = 0; x < matrixBorders.columns() - 2; x++) {
            x1[x + 1] = x * unitWidth / factorX + unitWidth / (2 * factorX);
        }
        x1[matrixBorders.columns() - 1] = width;
        double[] x2 = new double[matrixBorders.rows()];
        x2[0] = 0;
        for (int y = 0; y < matrixBorders.rows() - 2; y++) {
            x2[y + 1] = y * unitHeight / factorY + unitHeight / (2 * factorY);
        }
        x2[matrixBorders.rows() - 1] = height;

        BiCubicSplineFast bcs = new BiCubicSplineFast(x2, x1, matrixBorders.toArray());
        return bcs;
    }

    protected int constrainWithinPalette(int ci) {
        return MathUtils.constrainWithin(ci, 0, palette.maxColourIndex());
    }

    @Override
    public Color[] getPalette() {
        return palette.getColors();
    }

    @Override
    public void setPalette(Palette newPalette) {
        palette = newPalette;
    }

    @Override
    public void reversePalette() {
        palette.reverse();
    }

    @Override
    public Palette getCurrentPalette() {
        return palette;
    }

    /**
     * Default implementation using {@link Palettes#getDefaultPalette()}. Subclasses that want to use a different
     * palette should overwrite this method.
     */
    @Override
    public String getPreferredPaletteName() {
        return Palettes.getDefaultPalette().getName();
    }

    /** Deletes all cached elements that use the {@link Palette} with the given index. */
    public void invalidateCache(Palette palette) {
        for (String key : cache.keySet()) {
            if (key.contains("palette:" + palette.getShortName())) {
                cache.remove(key);
                log.info("Removed cache for: " + key);
            }
        }
    }

    /**
     * Computes the hit-histogram from the given {@link GrowingSOM}. Also sets the values of
     * {@link AbstractMatrixVisualizer#minimumMatrixValue} and {@link AbstractMatrixVisualizer#maximumMatrixValue}
     */
    protected DoubleMatrix2D computeHitHistogram(GrowingSOM gsom) throws LayerAccessException {
        final GrowingLayer layer = gsom.getLayer();
        DoubleMatrix2D matrix = new DenseDoubleMatrix2D(layer.getYSize(), layer.getXSize());
        // create matrix from number of hits per unit; also compute min & max of those values
        for (int x = 0; x < layer.getXSize(); x++) {
            for (int y = 0; y < layer.getYSize(); y++) {
                final int numberOfMappedInputs = layer.getUnit(x, y).getNumberOfMappedInputs();
                matrix.setQuick(y, x, numberOfMappedInputs);
                if (numberOfMappedInputs > maximumMatrixValue) {
                    maximumMatrixValue = numberOfMappedInputs;
                }
                if (numberOfMappedInputs < minimumMatrixValue) {
                    minimumMatrixValue = numberOfMappedInputs;
                }
            }
        }
        return matrix;
    }

}
