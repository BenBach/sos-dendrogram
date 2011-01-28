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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import at.tuwien.ifs.somtoolbox.visualization.Palette;

/**
 * Component to display a Palette. <br>
 * Nice-to-have features:
 * <ul>
 * <li>scaling (just showing some part of the palette)</li>
 * </ul>
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: PaletteDisplayer.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class PaletteDisplayer extends JComponent {
    private static final long serialVersionUID = 1L;

    private static final int MIN_PALETTE_WIDTH = 35;

    private Palette palette;

    private boolean showScale;

    private boolean autoOrientation;

    private int orientation;

    private Font font;

    private boolean showPercent;

    public boolean getShowPercent() {
        return showPercent;
    }

    public void setShowPercent(boolean showPercent) {
        this.showPercent = showPercent;
        revalidate();
        repaint();
    }

    private double minValue = 0, maxValue = 100;

    /**
     * Create a new PaletteDisplayer.
     */
    public PaletteDisplayer() {
        super();
        palette = null;
        showScale = true;
        showPercent = true;
        autoOrientation = true;
        font = new Font("Monospaced", Font.PLAIN, 9);

        orientation = SwingConstants.HORIZONTAL;
    }

    /**
     * Create a new PaletteDisplayer, displaying the given Palette.
     * 
     * @param palette the Palette to display.
     */
    public PaletteDisplayer(Palette palette) {
        this();
        this.palette = palette;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isAutoOrientation()) {
            if (getWidth() < getHeight()) {
                setOrientation(SwingConstants.VERTICAL);
            }
            if (getWidth() > getHeight()) {
                setOrientation(SwingConstants.HORIZONTAL);
            }
        }
        if (isOpaque()) { // paint background
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (palette != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            Insets insets = getInsets();
            int scaleHeight = 0;
            int scaleWidth = 0;
            double max = 100d;
            double min = 0d;
            if (!showPercent) {
                max = maxValue;
                min = minValue;
            }
            if (orientation == SwingConstants.HORIZONTAL) {
                // Write the scale
                if (isShowScale()) {
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(font);
                    FontMetrics metrics = g2d.getFontMetrics(font);
                    scaleHeight = metrics.getHeight() + 2;
                    scaleWidth = metrics.stringWidth(Math.round(max) + (showPercent ? " %" : " "));
                    // System.out.println("Font: " + scaleWidth + "x" + scaleHeight);
                    int werte = (getWidth() - insets.left - insets.right) / (scaleWidth * 3);
                    double step = (max - min) / werte;

                    for (int i = 0; i <= werte; i++) {
                        String text = Math.round(min + i * step) + (showPercent ? "%" : "");
                        int x = insets.left
                                + scaleWidth
                                / 2
                                - metrics.stringWidth(text)
                                / 2
                                + (int) Math.round((getWidth() - insets.left - insets.right - scaleWidth) * i * step
                                        / (max - min));
                        int y = getHeight() - insets.bottom - metrics.getDescent();
                        g2d.drawString(text, x, y);
                    }
                }

                int xStart = insets.left + scaleWidth / 2;
                int xEnd = getWidth() - insets.right - scaleWidth / 2;
                int paletteHeight = getHeight() - insets.top - insets.bottom - scaleHeight;
                Color[] colors = getPalette().getColors();
                float step = (float) (xEnd - xStart) / (float) colors.length;

                for (int i = 0; i < colors.length; i++) {
                    g2d.setColor(colors[i]);
                    g2d.fillRect(xStart + Math.round(step * i), insets.top, Math.round(step + 1), paletteHeight);
                }
            } else { // VERTICAL
                // Write the scale
                if (isShowScale()) {
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(font);
                    FontMetrics metrics = g2d.getFontMetrics(font);
                    scaleHeight = metrics.getHeight();
                    scaleWidth = metrics.stringWidth(Math.round(max) + (showPercent ? " %" : " ")) + 2;

                    int werte = (getHeight() - insets.top - insets.bottom) / (scaleHeight * 4);
                    double step = (max - min) / werte;

                    for (int i = 0; i <= werte; i++) {
                        String text = Math.round(min + i * step) + (showPercent ? "% " : " ");
                        int x = insets.left + scaleWidth - metrics.stringWidth(text);
                        int y = getHeight()
                                - insets.bottom
                                - (int) Math.round((getHeight() - insets.top - insets.bottom - scaleHeight) * i * step
                                        / (max - min));
                        g2d.drawString(text, x, y);
                    }
                }

                int yStart = insets.top + scaleHeight / 2;
                int yEnd = getHeight() - insets.top - scaleHeight / 2;
                int paletteWidth = getWidth() - insets.left - insets.right - scaleWidth;
                Color[] colors = getPalette().getColors();
                float step = (float) (yEnd - yStart) / (float) colors.length;

                for (int i = 0; i < colors.length; i++) {
                    g2d.setColor(colors[i]);
                    g2d.fillRect(insets.left + scaleWidth, yEnd - Math.round(step * (i + 1)), paletteWidth,
                            Math.round(step + 1));
                }
            }
            g2d.dispose();
        }
    }

    /**
     * Returns wheter the Components orientation is automatically adjusted. Default is <c>true</c>
     * 
     * @return Returns the autoOrientation.
     */
    public boolean isAutoOrientation() {
        return autoOrientation;
    }

    /**
     * Sets wheter the components orientation should be automatically adjusted.
     * 
     * @param autoOrientation The autoOrientation to set.
     */
    public void setAutoOrientation(boolean autoOrientation) {
        this.autoOrientation = autoOrientation;
    }

    /**
     * Returns the font used for the scale.
     * 
     * @return Returns the font.
     */
    @Override
    public Font getFont() {
        return font;
    }

    /**
     * Sets the Font used for the scale.
     * 
     * @param font The font to set.
     */
    @Override
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Returns the components orientation. Default is {@link SwingConstants#HORIZONTAL}
     * 
     * @return Returns the orientation.
     * @see #getOrientation()
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Set the Orientation. This can either be {@link SwingConstants#HORIZONTAL} or {@link SwingConstants#VERTICAL}
     * 
     * @param orientation The orientation to set.
     * @see SwingConstants
     */
    public void setOrientation(int orientation) {
        if (orientation == SwingConstants.HORIZONTAL || orientation == SwingConstants.VERTICAL) {
            this.orientation = orientation;
        }
    }

    /**
     * Get the palette that is displayed.
     * 
     * @return Returns the palette.
     */
    public Palette getPalette() {
        return palette;
    }

    /**
     * Set the palette to display.
     * 
     * @param palette The palette to set.
     */
    public void setPalette(Palette palette) {
        this.palette = palette;
        revalidate();
        repaint();
    }

    /**
     * Determines whether a scale is shown or not. Default is <c>true<c>.
     * 
     * @return Returns the showScale.
     */
    public boolean isShowScale() {
        return showScale;
    }

    /**
     * Set to <c>true</c> if a scale should be shown.
     * 
     * @param showScale The showScale to set.
     */
    public void setShowScale(boolean showScale) {
        this.showScale = showScale;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension s = super.getMinimumSize();
        if (s == null) {
            s = new Dimension(0, 0);
        }
        int h = 0, w = 0;
        Insets insets = getInsets();
        h = insets.top + insets.bottom + MIN_PALETTE_WIDTH;
        w = insets.left + insets.right + MIN_PALETTE_WIDTH;

        if (h > s.height) {
            s.height = h;
        }
        if (w > s.width) {
            s.width = w;
        }
        return new Dimension(w > s.width ? w : s.width, h > s.height ? h : s.height);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension s = super.getPreferredSize();
        if (s == null) {
            s = new Dimension(0, 0);
        }
        int h = 0, w = 0;
        Insets insets = getInsets();
        h = insets.top + insets.bottom + MIN_PALETTE_WIDTH;
        w = insets.left + insets.right + MIN_PALETTE_WIDTH;

        if (h > s.height) {
            s.height = h;
        }
        if (w > s.width) {
            s.width = w;
        }
        return new Dimension(w > s.width ? w : s.width, h > s.height ? h : s.height);
    }

    public void setRange(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        revalidate();
        repaint();
    }

}
