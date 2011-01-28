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
import java.awt.Font;
import java.awt.FontMetrics;

/**
 * This class represents all the information needed to draw a specific label - its psition, font, rotation, etc.
 * 
 * @author Rudolf Mayer
 * @version $Id: LabelDescription.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class LabelDescription {
    /** The default font of all labels. Instances will use this font, and only modify the size. */
    public static Font DEFAULT_FONT = new Font("Sans", Font.PLAIN, 40);

    /** The colour to draw the label with. */
    private Color color;

    /**
     * The font to draw the label with. This will be initially a copy of {@link #DEFAULT_FONT}, with only the size
     * modified to {@link #fontSize}
     */
    private Font font;

    /** The font size to draw the label with. Used to instantiate {@link #font}. */
    private float fontSize;

    /** The rotation of the label. */
    private double rotation;

    /** The label text. */
    private String text;

    /** The horizontal position of the label. */
    private int x;

    /** The vertical position of the label. */
    private int y;

    private boolean visible;

    public LabelDescription(String text, float fontSize, int x, int y, double rotation, boolean visible) {
        super();
        this.x = x;
        this.y = y;
        this.fontSize = fontSize;
        this.text = text;
        this.font = DEFAULT_FONT.deriveFont(fontSize);
        this.rotation = rotation;
        this.visible = visible;
    }

    /**
     * Compares two {@link LabelDescription} with each other. They are considered identical if the values of
     * {@link #getText()}, {@link #getX()}, {@link #getY()}, {@link #getFontSize()}, {@link #getColor()} and
     * {@link #getRotation()} are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LabelDescription)) {
            return false;
        }
        LabelDescription other = (LabelDescription) obj;
        if (getText().equals(other.getText()) && getX() == other.getX() && getY() == other.getY()
                && getFontSize() == other.getFontSize() && getColor() == other.getColor()
                && getRotation() == other.getRotation()) {
            return true;
        } else {
            return false;
        }
    }

    /** Gets the current colour of this label. */
    public Color getColor() {
        return color;
    }

    /** Gets the current font of this label. */
    public Font getFont() {
        return font;
    }

    /** Gets a scaled version of the current font. The font-size of label is scaled by the given factor. */
    public Font getFont(double scale) {
        return font.deriveFont((float) getFontSize(scale));
    }

    /** Gets the current font size of this label. */
    public float getFontSize() {
        return fontSize;
    }

    /** Gets scaled value of the current font size of this label. */
    public double getFontSize(double scale) {
        return font.getSize2D() * scale;
    }

    /** Gets the current rotation of this label. */
    public double getRotation() {
        return rotation;
    }

    /** Gets the current text of this label. */
    public String getText() {
        return text;
    }

    /**
     * @return the x position of the label
     */
    public int getX() {
        return x;
    }

    /**
     * Calculates a scaled x position of the label, by the given scale factor
     * 
     * @param scale the scale factor
     * @return the scaled x position of the label
     */
    public int getX(double scale) {
        return (int) (x * scale);
    }

    /**
     * @return the y position of the label
     */
    public int getY() {
        return y;
    }

    /**
     * Calculates a scaled y position of the label, by the given scale factor
     * 
     * @param scale the scale factor
     * @return the scaled y positon of the label
     */
    public int getY(double scale) {
        return (int) (y * scale);
    }

    /**
     * Calculates the offset y position, considering font ascents.
     * 
     * @param fontMetrics metric to calculate the ascents.
     * @return the y position increased by the ascent.
     */
    public int getYBaseline(FontMetrics fontMetrics) {
        return y + fontMetrics.getAscent();
    }

    /**
     * Calculates a scaled offset y position, considering font ascents.
     * 
     * @param fontMetrics metric to calculate the ascents.
     * @param scale the scale factor
     * @param lineNumber the line number of the string, used when wanting to print multi-line labels
     * @return the scaled y position increased by the ascent.
     */
    public int getYBaseline(FontMetrics fontMetrics, double scale, int lineNumber) {
        return (int) (y * scale) + fontMetrics.getAscent() + fontMetrics.getHeight() * lineNumber;
    }

    /** Sets a new colour for the label. */
    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isVisible() {
        return visible;
    }

}
