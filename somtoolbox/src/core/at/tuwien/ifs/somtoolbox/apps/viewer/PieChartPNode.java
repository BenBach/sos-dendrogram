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
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.PaintList;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MapOverviewPane;

/**
 * Wrapper for displaying a pie chart within the Piccolo zooming interface framework.
 * 
 * @author Michael Dittenbach
 * @version $Id: PieChartPNode.java 3939 2010-11-17 16:06:14Z frank $
 */
public class PieChartPNode extends PNode {
    public enum PieChartLabelMode {
        None, Count, Percent
    }

    private static final long serialVersionUID = 1L;

    private int[] values = null;

    private int itemCount;

    private PiePlot plot;

    private Rectangle2D border = new Rectangle2D.Double();

    private double X = 0;

    private double Y = 0;

    private double width = 0;

    private double height = 0;

    private final CountLabeler countLabeler = new CountLabeler();

    private final PercentageLabeler percentageLabeler = new PercentageLabeler();

    /**
     * Creates a PieChartPNode at the given coordinates with the given values.
     * 
     * @param x X coordinate of the chart.
     * @param y Y coordinate of the chart.
     * @param values Array of <code>double</code> containing the values.
     */
    public PieChartPNode(double x, double y, double w, double h, int[] values, int itemCount) {
        super();

        X = x;
        Y = y;
        width = w;
        height = h;
        this.values = values;
        this.itemCount = itemCount;
        plot = createPiechartPlot(values);
        setShowLegend(PieChartLabelMode.None);
        this.setBounds(X, Y, width, height);
        // TODO: check id values == null
    }

    /** Creates a piechart from the given count values */
    public static PiePlot createPiechartPlot(int[] values) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (int i = 0; i < values.length; i++) {
            dataset.setValue(String.valueOf(i), values[i]);
        }

        PiePlot piePlot = new PiePlot(dataset);
        piePlot.setShadowPaint(null);
        piePlot.setBackgroundPaint(null);
        piePlot.setOutlinePaint(null);
        piePlot.setIgnoreZeroValues(true); // makes sure we have no labels for sections with zero values..
        piePlot.setIgnoreNullValues(true);
        piePlot.setSectionOutlinesVisible(false); // hides the border around each pie slice
        piePlot.setLabelGenerator(null);
        return piePlot;
    }

    /**
     * Draw pie-charts on the given {@link Graphics2D} object, with the provided values and colours, and unit width.
     * Used when exporting pie charts as stand-alone image, or for superimposing them on an exported visualisation.
     */
    public static void drawPlot(Graphics2D g2d, int[] values, Color[] colors, double x, double y, double width,
            double height) {
        PiePlot piePlot = createPiechartPlot(values);
        piePlot.setLabelGap(0);
        piePlot.setInteriorGap(0);
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] != null) {
                piePlot.setSectionPaint(i, colors[i]);
            }
        }
        piePlot.draw(g2d, new Rectangle2D.Double(x, y, width, height), null, null, null);
    }

    @Override
    public boolean setBounds(double x, double y, double width, double height) {
        if (super.setBounds(x, y, width, height)) {
            border.setFrame(x, y, width, height);
            this.X = x;
            this.Y = y;
            this.width = width;
            this.height = height;
            return true;
        }
        return false;
    }

    @Override
    protected void paint(PPaintContext paintContext) {
        PCamera pCam = paintContext.getCamera();
        // paint only for the main display, not the MapOverviewPane
        if (!((PCanvas) pCam.getComponent()).getClass().equals(MapOverviewPane.MapOverviewCanvas.class)) {
            Graphics2D g2d = paintContext.getGraphics();
            border.setRect(X, Y, width, height);
            plot.draw(g2d, new Rectangle2D.Double(X, Y, width, height), null, null, null);
        }
    }

    /**
     * Returns the colors used in the diagram. Can be used for a legend displayed in a different Swing element.
     * 
     * @return Array of <code>Color</code>.
     */
    public Color[] getLegendColors() {
        Color[] res = new Color[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = (Color) plot.getSectionPaint(i);
        }
        return res;
    }

    public PaintList getPaintList() {
        PaintList p = new PaintList();
        for (int i = 0; i < values.length; i++) {
            p.setPaint(i, plot.getSectionPaint(i));
        }
        return p;
    }

    /** Returns the colour used in the diagram at the specified index. */
    public Color getLegendColor(int index) {
        return (Color) plot.getSectionPaint(index);
    }

    public void setColor(int index, Color color) {
        plot.setSectionPaint(index, color);
    }

    public void setColors(Color[] colors) {
        for (int i = 0; i < colors.length; i++) {
            plot.setSectionPaint(i, colors[i]);
        }
    }

    public int[] getValues() {
        return values;
    }

    public void setShowLegend(PieChartLabelMode mode) {
        switch (mode) {
            case Count:
                plot.setLabelGenerator(countLabeler);
                break;
            case Percent:
                plot.setLabelGenerator(percentageLabeler);
                break;
            case None:
                plot.setLabelGenerator(null);
                break;
        }
    }

    @SuppressWarnings("rawtypes")
    private class CountLabeler implements PieSectionLabelGenerator {
        @Override
        public String generateSectionLabel(PieDataset dataset, Comparable key) {
            return String.valueOf(dataset.getValue(key).intValue());
        }

        @Override
        public AttributedString generateAttributedSectionLabel(PieDataset dataset, Comparable key) {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    private class PercentageLabeler implements PieSectionLabelGenerator {
        @Override
        public String generateSectionLabel(PieDataset dataset, Comparable key) {
            return String.valueOf(Math.round(100 * dataset.getValue(key).doubleValue() / itemCount) + "%");
        }

        @Override
        public AttributedString generateAttributedSectionLabel(PieDataset dataset, Comparable key) {
            return null;
        }
    }

}
