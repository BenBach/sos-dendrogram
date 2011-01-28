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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.math.Functions;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.ImageUtils;
import at.tuwien.ifs.somtoolbox.util.VisualisationUtils;

/**
 * A hit-histogram visualiser, with three different modes of the hit count:
 * <ul>
 * <li>Colour-coding the values</li>
 * <li>Size and colour-encoding of the values</li>
 * <li>Textually displaying the numeric values</li>
 * </ul>
 * 
 * @author Rudolf Mayer
 * @version $Id: HitHistogramVisualizer.java 3642 2010-07-12 09:30:53Z mayer $
 */
public class HitHistogramVisualizer extends AbstractMatrixVisualizer implements BackgroundImageVisualizer {
    public HitHistogramVisualizer() {
        NUM_VISUALIZATIONS = 3;
        VISUALIZATION_NAMES = new String[] { "Hit Histogram - Colour Coding", "Hit Histogram - Size & Colour Coding",
                "Hit Histogram - Textual Values" };
        VISUALIZATION_SHORT_NAMES = new String[] { "HitHistogramColour", "HitHistogramSize", "HitHistogramNumeric" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "A simple Hit Histogram with colour coding",
                "Hit Histogram with size and colour coding", "Hit Histogram with textual display of the numeric values" };
        setInterpolate(false);
    }

    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        checkVariantIndex(variantIndex, getClass());

        DoubleMatrix2D matrix = computeHitHistogram(gsom);

        if (variantIndex == 0) { // simple colour coding - just user the super-class method
            matrix.assign(Functions.div(maximumMatrixValue));
            return super.createImage(gsom, matrix, width, height, interpolate);

        } else if (variantIndex == 1) { // size coding
            matrix.assign(Functions.div(maximumMatrixValue));
            BufferedImage res = ImageUtils.createEmptyImage(width, height);
            Graphics2D g = (Graphics2D) res.getGraphics();

            double unitWidth = (double) width / gsom.getLayer().getXSize();
            double unitHeight = (double) height / gsom.getLayer().getYSize();

            int ci = 0;
            double totalArea = unitWidth * unitHeight;
            double aspectRatio = unitWidth / unitHeight;
            for (int y = 0; y < matrix.rows(); y++) {
                for (int x = 0; x < matrix.columns(); x++) {
                    // determine relative size of the area & sides
                    double relativeSize = matrix.get(y, x);
                    double relativeArea = totalArea * relativeSize;
                    int relativeWidth = (int) Math.round(Math.sqrt(relativeArea * aspectRatio));
                    int relativeHeight = (int) Math.round(Math.sqrt(relativeArea * aspectRatio) * 1 / aspectRatio);

                    ci = (int) Math.round(relativeSize * palette.maxColourIndex());
                    g.setPaint(palette.getColor(ci));

                    g.fill(new Rectangle((int) (x * unitWidth + (unitWidth - relativeWidth) / 2),// 
                            (int) (y * unitHeight + (unitHeight - relativeHeight) / 2), //
                            relativeWidth, relativeHeight));
                }
            }
            return res;

        } else if (variantIndex == 2) { // textual display of numeric values
            BufferedImage res = ImageUtils.createEmptyImage(width, height);
            Graphics2D g = (Graphics2D) res.getGraphics();
            g.setColor(Color.BLACK);
            double unitWidth = (double) width / gsom.getLayer().getXSize();
            double unitHeight = (double) height / gsom.getLayer().getYSize();

            g.setFont(new Font("sansserif", Font.BOLD, 32));
            FontMetrics metrics = g.getFontMetrics();

            for (int y = 0; y < matrix.rows(); y++) {
                for (int x = 0; x < matrix.columns(); x++) {
                    Point unitCentre = VisualisationUtils.getUnitCentreLocation(x, y, unitWidth, unitHeight);
                    int count = (int) matrix.get(y, x);
                    if (count > 0) {
                        String str = String.valueOf(count);
                        int stringWidth = metrics.stringWidth(str);
                        g.drawString(str, unitCentre.x - stringWidth / 2, unitCentre.y
                                + (metrics.getAscent() - metrics.getDescent()) / 2);
                    }
                }
            }
            return res;
        } else {
            throw getVariantException(variantIndex, getClass());
        }
    }

    @Override
    public int getPreferredScaleFactor() {
        return 1;
    }

    @Override
    public String getPreferredPaletteName() {
        return "Redscale32";
    }

}
