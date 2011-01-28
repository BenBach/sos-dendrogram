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
import java.awt.image.BufferedImage;

import flanagan.interpolation.BiCubicSplineFast;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * visualizes counts (e.g. number of documents per unit) in an Islands of Music like manner, i.e. like SDH with s=1
 * 
 * @author Thomas Lidy
 * @version $Id: SmoothedCountHistograms.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class SmoothedCountHistograms extends AbstractMatrixVisualizer {

    double[][] sdh = null; // histogram (basis for visualization)

    int xSize = 0;

    int ySize = 0;

    double maxValue = 0;

    double minValue = Double.MAX_VALUE;

    public SmoothedCountHistograms() {
        NUM_VISUALIZATIONS = 2;
    }

    public void setHistogram(double[][] hist) {
        this.sdh = hist; // ((Histogram)smoothingCache[index].get(new Integer(s))).mh;
        this.normalize();
    }

    private void normalize() {
        if (sdh == null) {
            return;
        }

        xSize = sdh.length;
        ySize = sdh[0].length;

        /** determine max and min value of matrices * */

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                /* logarithmise */
                if (sdh[x][y] > 0) {
                    sdh[x][y] = Math.log(sdh[x][y]);
                } else if (sdh[x][y] < 0) {
                    sdh[x][y] = -Math.log(-sdh[x][y]);
                }

                if (sdh[x][y] > maxValue) {
                    maxValue = sdh[x][y];
                }
                if (sdh[x][y] < minValue) {
                    minValue = sdh[x][y];
                }
            }
        }

        /** normalize histogram values * */

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                sdh[x][y] = (sdh[x][y] - minValue) / (maxValue - minValue);
            }
        }
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        if (sdh == null || sdh.length == 0) {
            throw new SOMToolboxException("No histogram data provided for SmoothedCountHistograms visualization!");
            // must call setHistogram first!
        }

        int unitWidth = width / xSize;
        int unitHeight = height / ySize;

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int ci = 0;
        double paletteLim = palette.maxColourIndex();

        if (index == 0) /* flat histogram color image */
        {
            BufferedImage smallimg = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    ci = (int) (sdh[x][y] * paletteLim);
                    smallimg.setRGB(x, y, palette.getColor(ci).getRGB());
                }
            }
            g.drawImage(smallimg, 0, 0, width, height, Color.BLACK, null);
            // smallimg.getScaledInstance(width, height, BufferedImage.SCALE_DEFAULT);
        } else /* index == 1 ... sdh-like image */
        {

            /** start bicubic spline stuff * */

            double[] x1 = new double[xSize + 2];
            x1[0] = 0;
            for (int x = 0; x < xSize; x++) {
                x1[x + 1] = x * unitWidth + unitWidth / 2;
            }
            x1[xSize + 1] = width;
            double[] x2 = new double[ySize + 2];
            x2[0] = 0;
            for (int y = 0; y < ySize; y++) {
                x2[y + 1] = y * unitHeight + unitHeight / 2;
            }
            x2[ySize + 1] = height;

            double[][] sdhWithBorders = new double[xSize + 2][ySize + 2];

            sdhWithBorders[0][0] = sdh[0][0] - (sdh[1][1] - sdh[0][0]) / 2; // top-left corner
            sdhWithBorders[xSize + 1][0] = sdh[xSize - 1][0] - (sdh[xSize - 2][1] - sdh[xSize - 1][0]) / 2; // top-right
            // corner
            sdhWithBorders[0][ySize + 1] = sdh[0][ySize - 1] - (sdh[1][ySize - 2] - sdh[0][ySize - 1]) / 2; // bottom-left
            // corner
            sdhWithBorders[xSize + 1][ySize + 1] = sdh[xSize - 1][ySize - 1]
                    - (sdh[xSize - 2][ySize - 2] - sdh[xSize - 1][ySize - 1]) / 2; // bottom-right
            // corner
            for (int x = 1; x < xSize + 1; x++) {
                sdhWithBorders[x][0] = sdh[x - 1][0] - (sdh[x - 1][1] - sdh[x - 1][0]) / 2; // top row
                sdhWithBorders[x][ySize + 1] = sdh[x - 1][ySize - 1] - (sdh[x - 1][ySize - 2] - sdh[x - 1][ySize - 1])
                        / 2; // bottom row
            }
            for (int y = 1; y < ySize + 1; y++) {
                sdhWithBorders[0][y] = sdh[0][y - 1] - (sdh[1][y - 1] - sdh[0][y - 1]) / 2; // left column
                sdhWithBorders[xSize + 1][y] = sdh[xSize - 1][y - 1] - (sdh[xSize - 2][y - 1] - sdh[xSize - 1][y - 1])
                        / 2; // right column
            }
            for (int y = 0; y < ySize; y++) {
                for (int x = 0; x < xSize; x++) {
                    sdhWithBorders[x + 1][y + 1] = sdh[x][y];
                }
            }
            BiCubicSplineFast bcs = new BiCubicSplineFast(x1, x2, sdhWithBorders);
            // bcs.calcDeriv();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    ci = (int) Math.round(bcs.interpolate(x + 0.5, y + 0.5) * paletteLim);
                    // limit ci value between 0 & max value
                    ci = Math.min(palette.getNumberOfColours() - 1, Math.max(0, ci));

                    // check for mnemonic SOM: if a unit is empty and has a low value, or the unit has no neighbours
                    // if ((ci < 3 && gsom.getLayer().getUnit(x/unitWidth, y/unitHeight) == null) ||
                    // !gsom.getLayer().hasNeighbours(x/unitWidth,
                    // y/unitHeight)) {
                    // g.setPaint(Color.WHITE); // we draw this position white
                    // } else {
                    // g.setPaint(palette.getColor(ci));
                    // }
                    // g.fill(new Rectangle(x,y,1,1));

                    res.setRGB(x, y, palette.getColor(ci).getRGB());
                }
            }
            /** end bicubic spline stuff * */
        }

        return res;
    }

}
