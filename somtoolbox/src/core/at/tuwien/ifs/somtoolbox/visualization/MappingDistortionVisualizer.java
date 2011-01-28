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
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * @author Michael Dittenbach
 * @version $Id: MappingDistortionVisualizer.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class MappingDistortionVisualizer extends AbstractBackgroundImageVisualizer implements QualityMeasureVisualizer {

    public MappingDistortionVisualizer() {
        NUM_VISUALIZATIONS = 2;
        VISUALIZATION_NAMES = new String[] { "Distortion (sqrt(2))", "Distortion (2nd-best>3rd-best)" };
        VISUALIZATION_SHORT_NAMES = new String[] { "DistortionSqrt", "Distortion2nd3rd" };
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Distortion is shown by lines between winners that are farther away than sqrt(2).",
                "If third-best winner is farther away from the winner than the second-best, a line is drawn." };
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR };

    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        switch (index) {
            case 0: {
                return createDistortionImage(gsom, width, height);
            }
            case 1: {
                return createDistortionImage2(gsom, width, height);
            }
            default: {
                return null;
            }
        }
    }

    private BufferedImage createDistortionImage(GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        InputData data = gsom.getSharedInputObjects().getInputData();
        if (data == null) {
            throw new SOMToolboxException("You need to specify the " + neededInputObjects[0]);
        }

        // InputData data = new SOMLibSparseInputData(fileNames[0], true, true,1,7); // TODO: exception handling in
        // future?
        // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();
        g.setColor(Color.WHITE);
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, width, height);

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(0.3f));
        for (int d = 0; d < data.numVectors(); d++) {
            Unit[] winners = gsom.getLayer().getWinners(data.getInputDatum(d), 2);
            if (mapDistance(winners[0], winners[1]) > Math.sqrt(2)) {
                g.draw(new Line2D.Double(winners[0].getXPos() * unitWidth + unitWidth / 2, winners[0].getYPos()
                        * unitHeight + unitHeight / 2, winners[1].getXPos() * unitWidth + unitWidth / 2,
                        winners[1].getYPos() * unitHeight + unitHeight / 2));
            }
        }

        return res;
    }

    private BufferedImage createDistortionImage2(GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        InputData data = gsom.getSharedInputObjects().getInputData();
        if (data == null) {
            throw new SOMToolboxException("You need to specify the " + neededInputObjects[0]);
        }
        // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();
        g.setColor(Color.WHITE);
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, width, height);

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(0.3f));
        for (int d = 0; d < data.numVectors(); d++) {
            Unit[] winners = gsom.getLayer().getWinners(data.getInputDatum(d), 3);
            if (mapDistance(winners[0], winners[1]) > mapDistance(winners[0], winners[2])) {
                g.draw(new Line2D.Double(winners[0].getXPos() * unitWidth + unitWidth / 2, winners[0].getYPos()
                        * unitHeight + unitHeight / 2, winners[1].getXPos() * unitWidth + unitWidth / 2,
                        winners[1].getYPos() * unitHeight + unitHeight / 2));
            }
        }

        return res;
    }

    private double mapDistance(Unit unit1, Unit unit2) {
        return Math.sqrt((unit1.getXPos() - unit2.getXPos()) * (unit1.getXPos() - unit2.getXPos())
                + (unit1.getYPos() - unit2.getYPos()) * (unit1.getYPos() - unit2.getYPos()));
    }

}
