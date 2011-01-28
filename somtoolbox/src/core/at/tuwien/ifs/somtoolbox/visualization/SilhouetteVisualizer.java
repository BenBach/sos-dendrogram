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

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.PseudoSilhouetteValue;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.layers.quality.SOMSilhouetteValue;
import at.tuwien.ifs.somtoolbox.layers.quality.SilhouetteValue;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * @author Robert Neumayer
 * @version $Id: SilhouetteVisualizer.java 3590 2010-05-21 10:43:45Z mayer $
 */

public class SilhouetteVisualizer extends AbstractMatrixVisualizer implements QualityMeasureVisualizer {

    private SilhouetteValue silhouetteValue = null;

    private PseudoSilhouetteValue pseudoSilhouetteValue = null;

    private SOMSilhouetteValue somSilhouetteValue = null;

    public SilhouetteVisualizer() {
        NUM_VISUALIZATIONS = 3;
        VISUALIZATION_NAMES = new String[] { "Silhouette Value", "Pseudo Silhouette Value", "SOM Silhouette" };
        VISUALIZATION_SHORT_NAMES = new String[] { "SilhouetteVal", "PseudoSilhouetteVal", "SOMSilhouette" };
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "The Silhouette validation technique (Rousseeuw, 1987), be aware of the performance issues.",
                "Simplification of the Silhouette technique (compares distances of units within unit to weight vector of other units, as opposed to all mapped vectors of that unit)",
                "Further simplification of the Silhouette algorithm. Distances within units are represented by the distance of each mapped datum and the weight vector of the unit it self or the next closest unit." };

        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR, SOMVisualisationData.INPUT_VECTOR,
                SOMVisualisationData.INPUT_VECTOR };
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height) {

        switch (index) {
            case 0: {
                if (this.silhouetteValue == null) {
                    InputData data = gsom.getSharedInputObjects().getInputData();
                    if (data != null) {
                        this.silhouetteValue = new SilhouetteValue(gsom.getLayer(), data);
                    } else {
                        return null; // TODO / FIXME: throw an exception?
                    }
                }
                return createSVImage(gsom, width, height);
            }
            case 1: {
                if (this.pseudoSilhouetteValue == null) {
                    InputData data = gsom.getSharedInputObjects().getInputData();
                    if (data != null) {
                        this.pseudoSilhouetteValue = new PseudoSilhouetteValue(gsom.getLayer(), data);
                    } else {
                        return null; // TODO / FIXME: throw an exception?
                    }
                }
                return createPSVImage(gsom, width, height);
            }
            case 2: {
                if (this.somSilhouetteValue == null) {
                    InputData data = gsom.getSharedInputObjects().getInputData();
                    if (data != null) {
                        this.somSilhouetteValue = new SOMSilhouetteValue(gsom.getLayer(), data);
                    } else {
                        return null; // TODO / FIXME: throw an exception?
                    }
                }
                return createSSVImage(gsom, width, height);
            }
            default: {
                return null;
            }
        }
    }

    private BufferedImage createSVImage(GrowingSOM gsom, int width, int height) {
        // double maxSV = 1;
        // double minSV = -1;

        double maxSV = Double.MIN_VALUE;
        double minSV = Double.MAX_VALUE;
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
                        if (this.silhouetteValue.getUnitQualities("silhouetteValue")[u.getXPos()][u.getYPos()] > maxSV) {
                            maxSV = this.silhouetteValue.getUnitQualities("silhouetteValue")[u.getXPos()][u.getYPos()];
                        }
                        if (this.silhouetteValue.getUnitQualities("silhouetteValue")[u.getXPos()][u.getYPos()] < minSV) {
                            minSV = this.silhouetteValue.getUnitQualities("silhouetteValue")[u.getXPos()][u.getYPos()];
                        }
                    }
                }
            }
        } catch (QualityMeasureNotFoundException e) {
            // this does not happen
        }

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

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
                        ci = (int) Math.round((this.silhouetteValue.getUnitQualities("silhouetteValue")[u.getXPos()][u.getYPos()] - minSV)
                                / (maxSV - minSV) * palette.maxColourIndex());
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

    private BufferedImage createPSVImage(GrowingSOM gsom, int width, int height) {
        double maxSV = 1;
        double minSV = -1;
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
                        if (this.pseudoSilhouetteValue.getUnitQualities("pseudoSilhouetteValue")[u.getXPos()][u.getYPos()] > maxSV) {
                            maxSV = this.pseudoSilhouetteValue.getUnitQualities("pseudoSilhouetteValue")[u.getXPos()][u.getYPos()];
                        }
                        if (this.pseudoSilhouetteValue.getUnitQualities("pseudoSilhouetteValue")[u.getXPos()][u.getYPos()] < minSV) {
                            minSV = this.pseudoSilhouetteValue.getUnitQualities("pseudoSilhouetteValue")[u.getXPos()][u.getYPos()];
                        }
                    }
                }
            }
        } catch (QualityMeasureNotFoundException e) {
            // this does not happen
        }

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

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
                        ci = (int) Math.round((this.pseudoSilhouetteValue.getUnitQualities("pseudoSilhouetteValue")[u.getXPos()][u.getYPos()] - minSV)
                                / (maxSV - minSV) * palette.maxColourIndex());
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

    private BufferedImage createSSVImage(GrowingSOM gsom, int width, int height) {
        double maxSV = this.somSilhouetteValue.getMax();
        double minSV = this.somSilhouetteValue.getMin();
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
                        if (this.somSilhouetteValue.getUnitQualities("somSilhouetteValue")[u.getXPos()][u.getYPos()] > maxSV) {
                            maxSV = this.somSilhouetteValue.getUnitQualities("somSilhouetteValue")[u.getXPos()][u.getYPos()];
                        }
                        if (this.somSilhouetteValue.getUnitQualities("somSilhouetteValue")[u.getXPos()][u.getYPos()] < minSV) {
                            minSV = this.somSilhouetteValue.getUnitQualities("somSilhouetteValue")[u.getXPos()][u.getYPos()];
                        }
                    }
                }
            }
        } catch (QualityMeasureNotFoundException e) {
            // this does not happen
        }

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

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
                        ci = (int) Math.round((this.somSilhouetteValue.getUnitQualities("somSilhouetteValue")[u.getXPos()][u.getYPos()] - minSV)
                                / (maxSV - minSV) * palette.maxColourIndex());
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