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
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Visualization of some aspects of Quantization Error <br>
 * Notes: Only the measures relating to the Units will be drawn (Unit_QE, Unit_MQE)<br>
 * FIXME:
 * <ul>
 * <li>does not need input vector, data is stored already in unit file</li>
 * <li>computations of both methods is very similar, should be combined</li>
 * </ul>
 * 
 * @author Michael Dittenbach
 * @version $Id: QuantizationErrorVisualizer.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class QuantizationErrorVisualizer extends AbstractMatrixVisualizer implements QualityMeasureVisualizer {

    private QuantizationError qe = null;

    public QuantizationErrorVisualizer() {
        NUM_VISUALIZATIONS = 2;
        VISUALIZATION_NAMES = new String[] { "Quantization error", "Mean quantization error" };
        VISUALIZATION_SHORT_NAMES = new String[] { "QuantizationErr", "MeanQuantizationErr" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Quantization Error per Unit", "Mean Quantization Error per Unit" };
        // TODO: input data might be needed in case unit files do not store quantisation error
        // --> need to change QuantisationError class too
        neededInputObjects = null;
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height) {
        if (qe == null) {
            qe = new QuantizationError(gsom.getLayer(), null);
        }

        switch (index) {
            case 0: {
                return createQEImage(gsom, width, height);
            }
            case 1: {
                return createMQEImage(gsom, width, height);
            }
            default: {
                return null;
            }
        }
    }

    private BufferedImage createQEImage(GrowingSOM gsom, int width, int height) {
        double maxQE = Double.MIN_VALUE;
        double minQE = Double.MAX_VALUE;
        try {
            for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
                for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                    Unit u = null;
                    try {
                        u = gsom.getLayer().getUnit(i, j);
                    } catch (LayerAccessException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    if (u.getNumberOfMappedInputs() > 0) {
                        if (qe.getUnitQualities("qe")[u.getXPos()][u.getYPos()] > maxQE) {
                            maxQE = qe.getUnitQualities("qe")[u.getXPos()][u.getYPos()];
                        }
                        if (qe.getUnitQualities("qe")[u.getXPos()][u.getYPos()] < minQE) {
                            minQE = qe.getUnitQualities("qe")[u.getXPos()][u.getYPos()];
                        }
                    }
                }
            }
        } catch (QualityMeasureNotFoundException e) {
            // this does not happen
        }
        minimumMatrixValue = minQE;
        maximumMatrixValue = maxQE;

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        // Random rand = new Random(33330);
        int ci = 0;
        try {
            for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
                for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                    Unit u = null;
                    try {
                        u = gsom.getLayer().getUnit(x, y);
                    } catch (LayerAccessException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    if (u.getNumberOfMappedInputs() > 0) {
                        ci = (int) Math.round((qe.getUnitQualities("qe")[u.getXPos()][u.getYPos()] - minQE)
                                / (maxQE - minQE) * palette.maxColourIndex());
                        g.setPaint(palette.getColor(ci));
                    } else {
                        g.setPaint(Color.WHITE);
                    }
                    g.setColor(null);
                    g.fill(new Rectangle(x * unitWidth, y * unitHeight, unitWidth, unitHeight));
                }
            }
        } catch (QualityMeasureNotFoundException e) {
            // this does not happen
        }
        return res;
    }

    private BufferedImage createMQEImage(GrowingSOM gsom, int width, int height) {
        double maxMQE = Double.MIN_VALUE;
        double minMQE = Double.MAX_VALUE;
        try {
            for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
                for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                    Unit u = null;
                    try {
                        u = gsom.getLayer().getUnit(i, j);
                    } catch (LayerAccessException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    if (u.getNumberOfMappedInputs() > 0) {
                        if (qe.getUnitQualities("mqe")[u.getXPos()][u.getYPos()] > maxMQE) {
                            maxMQE = qe.getUnitQualities("mqe")[u.getXPos()][u.getYPos()];
                        }
                        if (qe.getUnitQualities("mqe")[u.getXPos()][u.getYPos()] < minMQE) {
                            minMQE = qe.getUnitQualities("mqe")[u.getXPos()][u.getYPos()];
                        }
                    }
                }
            }
        } catch (QualityMeasureNotFoundException e) {
            // this does not happen
        }
        minimumMatrixValue = minMQE;
        maximumMatrixValue = maxMQE;

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        // Random rand = new Random(33330);
        int ci = 0;
        try {
            for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
                for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                    Unit u = null;
                    try {
                        u = gsom.getLayer().getUnit(x, y);
                    } catch (LayerAccessException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    if (u.getNumberOfMappedInputs() > 0) {
                        ci = (int) Math.round((qe.getUnitQualities("mqe")[u.getXPos()][u.getYPos()] - minMQE)
                                / (maxMQE - minMQE) * palette.maxColourIndex());
                        g.setPaint(palette.getColor(ci));
                    } else {
                        g.setPaint(Color.WHITE);
                    }
                    g.setColor(null);
                    g.fill(new Rectangle(x * unitWidth, y * unitHeight, unitWidth, unitHeight));
                }
            }
        } catch (QualityMeasureNotFoundException e) {
            // this does not happen
        }
        return res;
    }

}
