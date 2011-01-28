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

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * A helper class around {@link GridBagConstraints}, providing convenience methods to set locations, etc..
 * 
 * @author Rudolf Mayer
 * @version $Id: GridBagConstraintsIFS.java 3862 2010-10-15 09:42:45Z frank $
 */
public class GridBagConstraintsIFS extends java.awt.GridBagConstraints {

    private static final long serialVersionUID = 1L;

    /** New constraints top-left, top-alignment left. */
    public GridBagConstraintsIFS() {
        gridx = 0;
        gridy = 0;
        anchor = NORTHWEST;
    }

    public GridBagConstraintsIFS(int anchor, int fill) {
        gridx = 0;
        gridy = 0;
        this.fill = fill;
        this.anchor = anchor;
    }

    /** reset to top-left position, top-alignment left */
    public void reset() {
        gridx = 0;
        gridy = 0;
        anchor = NORTHWEST;
    }

    public GridBagConstraintsIFS setFill(int fill) {
        this.fill = fill;
        return this;
    }

    public GridBagConstraintsIFS setAnchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public GridBagConstraintsIFS setPadding(int padx, int pady) {
        this.ipadx = padx;
        this.ipady = pady;
        return this;
    }

    public GridBagConstraintsIFS setPadding(int padx, int pady, Insets insets) {
        setPadding(padx, pady);
        return setInsets(insets);
    }

    public GridBagConstraintsIFS setInsets(int padding) {
        this.insets = new Insets(padding, padding, padding, padding);
        return this;
    }

    public GridBagConstraintsIFS setTopInset(int top) {
        this.insets.top = top;
        return this;
    }

    public GridBagConstraintsIFS setBottonInset(int bottom) {
        this.insets.bottom = bottom;
        return this;
    }

    public GridBagConstraintsIFS setLeftInset(int left) {
        this.insets.left = left;
        return this;
    }

    public GridBagConstraintsIFS setRightInset(int right) {
        this.insets.right = right;
        return this;
    }

    public GridBagConstraintsIFS setInsets(int x, int y) {
        this.insets = new Insets(y, x, y, x);
        return this;
    }

    public GridBagConstraintsIFS setInsets(Insets insets) {
        this.insets = insets;
        return this;
    }

    /** move into the next column */
    public GridBagConstraintsIFS nextCol() {
        gridx++;
        return this;
    }

    /** move into the next row */
    public GridBagConstraintsIFS nextRow() {
        gridx = 0;
        gridy++;
        return this;
    }

    /** moves to the given position */
    public GridBagConstraintsIFS moveTo(int x, int y) {
        gridx = x;
        gridy = y;
        return this;
    }

    /** set the grid width */
    public GridBagConstraintsIFS setGridWidth(int gridwidth) {
        this.gridwidth = gridwidth;
        return this;
    }

    /** set the grid height */
    public GridBagConstraintsIFS setGridHeight(int gridheight) {
        this.gridheight = gridheight;
        return this;
    }

    /** set the x and y weights */
    public GridBagConstraintsIFS setWeights(double weightx, double weighty) {
        this.weightx = weightx;
        this.weighty = weighty;
        return this;
    }

    public GridBagConstraintsIFS setWeightX(double weightX) {
        this.weightx = weightX;
        return this;
    }

    public GridBagConstraintsIFS setWeightY(double weightY) {
        this.weighty = weightY;
        return this;
    }

    /** resets the weights to 0 */
    public GridBagConstraintsIFS resetWeights() {
        this.weightx = 0;
        this.weighty = 0;
        return this;
    }

    /** Create a new instance with horizontal weight 1 */
    public GridBagConstraintsIFS fillWidth() {
        GridBagConstraintsIFS clone = this.clone();
        clone.weightx = 1.0;
        return clone;
    }

    /** Create a new instance with vertical weight 1 */
    public GridBagConstraintsIFS fillHeight() {
        GridBagConstraintsIFS clone = this.clone();
        clone.weighty = 1.0;
        return clone;
    }

    @Override
    public GridBagConstraintsIFS clone() {
        // TODO Auto-generated method stub
        return (GridBagConstraintsIFS) super.clone();
    }

}
