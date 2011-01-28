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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math.util.MathUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.ImageUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class provides two visualizations:
 * <ul>
 * <li>Flow visualization</li>
 * <li>Borderline visualizations.</li>
 * </ul>
 * both described in:<br>
 * <i><b>Georg Poelzlbauer, Michael Dittenbach, Andreas Rauber</b>. Advanced visualization of Self-Organizing Maps with
 * vector fields. <a href="http://www.sciencedirect.com/science?_ob=GatewayURL&_method=citationSearch&_urlVersion=4&_origin=SDVIALERTHTML&_version=1&_uoikey=B6T08-4K7166Y-4&md5=f8260f66027afdb1d445893049cad9d6"
 * >Neural Networks, 19(6-7):911-922</a>, July-August 2006.</i>
 * 
 * @author Dominik Schnitzer
 * @author Peter Widhalm
 * @version $Id: FlowBorderlineVisualizer.java 3883 2010-11-02 17:13:23Z frank $
 */
public class FlowBorderlineVisualizer extends AbstractBackgroundImageVisualizer {

    public static final String[] FLOWBORDER_SHORT_NAMES = new String[] { "Flow", "Border", "FlowBorder" };

    // Visualization Parameters
    private double sigma = 1.5;

    private double stretchConst = 0.7;

    private GrowingSOM gsom;

    private double[][] ax; // x-komponente des flow

    private double[][] ay; // y-komponente des flow

    private double maxa; // maximale flow-Komponente (fuer Normalisierung bei Darstellung)

    public FlowBorderlineVisualizer() {
        NUM_VISUALIZATIONS = 3;
        VISUALIZATION_NAMES = new String[] { "Flow", "Borderline", "Flow & Borderline" };
        VISUALIZATION_SHORT_NAMES = FLOWBORDER_SHORT_NAMES;
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Inplementation of Flow Visualization as described in \""
                        + "G. Poelzlbauer, M. Dittenbach, A. Rauber. \n Advanced "
                        + "visualization of Self-Organizing Maps with vector fields.\n"
                        + "Neural Networks, 19(6-7):911-922, July-August 2006.\"", "Borderline Visualization Variant",
                "Combined Flow/Borderline Visualization Variant" };

        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new FlowBorderlineControlPanel();
        }
        // Scale for the FlowBorderlineVisualizer needs to be smaller, as the visualisation is made of lines, which
        // cannot be scaled too much.
        preferredScaleFactor = 2;
    }

    protected class FlowBorderlineControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        protected JPanel sigmaPanel;

        public JSpinner sigmaSpinner;

        public FlowBorderlineControlPanel() {
            super("Flow/Borderline Control");

            sigmaSpinner = new JSpinner(new SpinnerNumberModel(sigma, 0.5, 30.0, 0.1));
            sigmaSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    sigma = ((Double) ((JSpinner) e.getSource()).getValue()).doubleValue();
                    if (visualizationUpdateListener != null) {
                        clearFlows();
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });

            sigmaPanel = new JPanel();
            sigmaPanel.add(new JLabel("Sigma: "));
            sigmaPanel.add(sigmaSpinner);
            add(sigmaPanel, c);
        }
    }

    private void clearFlows() {
        // FIXME: consider having a cache for ax, ay, maxa, instead of always computing it
        ax = null;
        ay = null;
        maxa = 0;
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        checkVariantIndex(index, getClass());

        // max-value for sigma = max diagonal
        double maxSigma = gsom.getLayer().maxNeighbourhoodRadius();
        ((FlowBorderlineControlPanel) controlPanel).sigmaSpinner.setModel(new SpinnerNumberModel(sigma, 0.5, maxSigma,
                0.1));
        ((FlowBorderlineControlPanel) controlPanel).sigmaSpinner.setToolTipText("Value for the neighbourhood radius. Ranges from 0.5 to "
                + maxSigma + ", the maximum value for the diagonal distance.");

        this.gsom = gsom;

        if (ax == null || ay == null) { // only calculate flow when really needed
            try {
                calculateFlows();
            } catch (Exception ex) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ex.getMessage());
                return null;
            }
        }

        BufferedImage res = ImageUtils.createEmptyImage(width, height);
        Graphics2D g = (Graphics2D) res.getGraphics();
        g.setColor(Color.blue);

        double unitWidth = width / gsom.getLayer().getXSize();
        double stretchBorder = unitWidth / maxa * stretchConst;

        // draw thicker lines, but save existing stroke first
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke((int) Math.round(unitWidth / 7)));

        for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
            for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
                draw(index, g, x, y, unitWidth, stretchBorder);
            }
        }
        g.setStroke(oldStroke); // reset to original stroke

        return res;
    }

    /**
     * Formel 1 distance in feature space
     */
    private double df(int x1, int y1, int x2, int y2) throws LayerAccessException, MetricException {
        // Formel 1
        return gsom.getLayer().getMetric().distance(gsom.getLayer().getUnit(x1, y1).getWeightVector(),
                gsom.getLayer().getUnit(x2, y2).getWeightVector());
    }

    /**
     * Formel 2 distance in output space
     */
    private double dout(int x1, int y1, int x2, int y2) {
        // Formel 2
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * Formel 3
     */
    private double kernel(double dout) {
        // Formel 3
        // return Math.exp(dout*dout/(2.0*sigma));
        // FIXME: why not the function above, which is in the paper ?
        return Math.exp(-1.0 * dout * dout / (2 * sigma * sigma));
    }

    /**
     * Formel 8, 9 10, 11, 12, 13, 14, 15, 16
     */
    private void calculateFlows() throws LayerAccessException, MetricException {

        int xSize = gsom.getLayer().getXSize();
        int ySize = gsom.getLayer().getYSize();
        ax = new double[xSize][ySize];
        ay = new double[xSize][ySize];
        maxa = 0.0;

        StdErrProgressWriter progress = new StdErrProgressWriter(xSize * ySize, "Calculating flows for unit ", xSize
                * ySize / 10);
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {

                double roxplus = 0.0;
                double roxminus = 0.0;
                double royplus = 0.0;
                double royminus = 0.0;

                double omegaxplus = 0.0;
                double omegaxminus = 0.0;
                double omegayplus = 0.0;
                double omegayminus = 0.0;

                for (int x2 = 0; x2 < xSize; x2++) {
                    for (int y2 = 0; y2 < ySize; y2++) {

                        if (x != x2 || y != y2) {

                            // Formel 8
                            int doutx = x2 - x;
                            int douty = y2 - y;

                            // Formel 9
                            double alpha = Math.atan2(douty, doutx);

                            // Formel 10
                            double h = kernel(dout(x, y, x2, y2));
                            double omegax = Math.cos(alpha) * h;
                            double omegay = Math.sin(alpha) * h;

                            // Formel 11, 12, 13, 14, 15
                            double df = df(x, y, x2, y2);
                            if (omegax > 0.0) {
                                roxplus += omegax * df;
                                omegaxplus += omegax;
                            } else {
                                roxminus += -omegax * df;
                                omegaxminus += -omegax;
                            }
                            if (omegay > 0.0) {
                                royplus += omegay * df;
                                omegayplus += omegay;
                            } else {
                                royminus += -omegay * df;
                                omegayminus += -omegay;
                            }

                        }
                    }
                }

                // Formel 16
                ax[x][y] = (-roxminus * omegaxplus + roxplus * omegaxminus) / (roxplus + roxminus);
                ay[x][y] = (-royminus * omegayplus + royplus * omegayminus) / (royplus + royminus);

                if (ax[x][y] > maxa) {
                    maxa = ax[x][y];
                }
                if (ay[x][y] > maxa) {
                    maxa = ay[x][y];
                }
                progress.progress();
            }
        }
    }

    private void draw(int mode, Graphics2D g, int x, int y, double unitWidth, double stretchBorder) {

        if (mode == 1 || mode == 2) {

            int bx1 = (int) (x * unitWidth + unitWidth / 2.0 - ay[x][y] * stretchBorder);
            int by1 = (int) (y * unitWidth + unitWidth / 2.0 + ax[x][y] * stretchBorder);
            int bx2 = (int) (x * unitWidth + unitWidth / 2.0 + ay[x][y] * stretchBorder);
            int by2 = (int) (y * unitWidth + unitWidth / 2.0 - ax[x][y] * stretchBorder);

            float w = (float) ((float) Math.sqrt(Math.pow(bx1 - bx2, 2) + Math.pow(by1 - by2, 2)) * .2 / 3);

            g.setStroke(new BasicStroke(w));
            g.setColor(Color.red);

            g.drawLine(bx1, by1, bx2, by2);
        }

        if (mode == 0 || mode == 2) {

            double lengthx = ax[x][y] * stretchBorder;
            double lengthy = ay[x][y] * stretchBorder;

            int bx1 = (int) (x * unitWidth + unitWidth / 2.0);
            int by1 = (int) (y * unitWidth + unitWidth / 2.0);
            int bx2 = (int) (x * unitWidth + unitWidth / 2.0 - lengthx);
            int by2 = (int) (y * unitWidth + unitWidth / 2.0 - lengthy);

            int deltaX = bx2 - bx1;
            int deltaY = by2 - by1;
            double frac = 0.2;

            int headTipX = bx2 + (int) (deltaX * 0.2);
            int headTipY = by2 + (int) (deltaY * 0.2);

            int headEndX1 = bx1 + (int) ((1 - frac) * deltaX + frac * deltaY);
            int headEndY1 = by1 + (int) ((1 - frac) * deltaY - frac * deltaX);
            int headEndX2 = bx1 + (int) ((1 - frac) * deltaX - frac * deltaY);
            int headEndY2 = by1 + (int) ((1 - frac) * deltaY + frac * deltaX);

            float w = (float) (Math.sqrt(Math.pow(headEndX1 - headEndX2, 2) + Math.pow(headEndY1 - headEndY2, 2)) / 3f);

            g.setStroke(new BasicStroke(w));
            g.setColor(Color.blue);

            g.drawLine(bx1, by1, bx2, by2);
            g.fillPolygon(new int[] { headTipX, headEndX1, headEndX2 }, new int[] { headTipY, headEndY1, headEndY2 }, 3);
        }
    }

    /**
     * Scale for the {@link FlowBorderlineVisualizer} needs to be smaller, as the visualisation is made of lines, which
     * cannot be scaled too much.
     */
    @Override
    public int getPreferredScaleFactor() {
        return 2;
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return appendToCacheKey(gsom, index, width, height, CACHE_KEY_SECTION_SEPARATOR + "sigma:" + sigma);
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        return getVisualizationFlavours(index, gsom, width, height, -1);
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            int maxFlavours) throws SOMToolboxException {
        // create images from sigma = 0.1 to 3.0 in steps of 0.1, then in steps o 0.2 till 10, then in steps of 0.5
        // or until maxFlavours is reached
        HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
        double maxNeighbourhoodRadius = gsom.getLayer().maxNeighbourhoodRadius();
        int numberOfDigits = at.tuwien.ifs.commons.util.MathUtils.numberOfDigits(Math.round(maxNeighbourhoodRadius));

        double currentSigma = sigma; // save old value
        sigma = 0.5;
        while (MathUtils.round(sigma, 1) <= maxNeighbourhoodRadius
                && (maxFlavours == -1 || images.size() <= maxFlavours)) {
            clearFlows();
            images.put("_sigma_" + StringUtils.format(sigma, 1, true, numberOfDigits), getVisualization(index, gsom,
                    width, height));
            if (MathUtils.round(sigma, 1) < 1.0) {
                sigma += 0.1;
            } else if (MathUtils.round(sigma, 1) < 30) {
                sigma += 0.5;
            } else {
                sigma += 1;
            }
        }
        sigma = currentSigma;
        return images;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException {
        // FIXME: implement this
        return getVisualizationFlavours(index, gsom, width, height);
    }
}
