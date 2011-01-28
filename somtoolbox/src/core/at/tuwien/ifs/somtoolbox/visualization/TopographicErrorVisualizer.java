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
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.layers.quality.TopographicError;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Visualization of some aspects of the Topographic Error Quality Measure, computation in {@link TopographicError}<br>
 * Notes: Only the measures relating to the Units will be drawn (Unit_QE, Unit_MQE)
 * 
 * @author Gerd Platzgummer
 * @version $Id: TopographicErrorVisualizer.java 3898 2010-11-03 16:46:07Z mayer $
 */
public class TopographicErrorVisualizer extends AbstractBackgroundImageVisualizer implements QualityMeasureVisualizer {

    private TopographicError topographicError = null;

    public TopographicErrorVisualizer() {
        NUM_VISUALIZATIONS = 2;
        VISUALIZATION_NAMES = new String[] { "Topographic Error neighbourhood - 4 units",
                "Topographic Error neighborhood - 8 units" };
        VISUALIZATION_SHORT_NAMES = new String[] { "TopographicError4Units", "TopographicError8units" };
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Topographic Error, 4 nearest neighbors on the map defined to be adjacent",
                "Topographic Error, 8 nearest neighbors on the map defined to be adjacent" };
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR };
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        // InputData data = null;
        // String cachefile = fileNames[1]; //cachefile auslesen, kann auch null sein wenn nicht angegeben vom user
        // gattuso
        // fileNames[1] = null; //l�schen, damit das Popupfenster wieder kommt gattuso
        // setFileName(1, null); //nouamoll, gattuso
        // if (cachefile == null) //kein Cachefile angegeben, daher neu berechnen gattuso
        // {

        String cachefile = null;
        if (topographicError == null) {
            if (gsom.getSharedInputObjects().getData(neededInputObjects[0]) == null) {
                throw new SOMToolboxException("You need to specify " + neededInputObjects[0]);
            }
            topographicError = new TopographicError(gsom.getLayer(), gsom.getSharedInputObjects().getInputData());
        }

        switch (index) {
            case 0: {
                return createQEImage(gsom, width, height, cachefile);// gattuso
            }
            case 1: {
                return createMQEImage(gsom, width, height, cachefile); // gattuso
            }
            default: {
                return null;
            }
        }
    }

    private BufferedImage createQEImage(GrowingSOM gsom, int width, int height, String cachefile) { // gattuso
        double maxTE = Double.MIN_VALUE;
        double minTE = Double.MAX_VALUE;

        // gattusostart
        double[][] unitquals = null;
        if (cachefile == null) {
            try {
                unitquals = topographicError.getUnitQualities("TE_Unit");
            } catch (QualityMeasureNotFoundException e) {
            }
        } else {
            unitquals = new double[gsom.getLayer().getXSize()][gsom.getLayer().getYSize()];

            // aus Cachefile auslesen

            try {
                BufferedReader br = new BufferedReader(new FileReader(cachefile));
                String line = null;
                int y = 0;
                while ((line = br.readLine()) != null) {
                    if (line.trim() != "") {
                        StringTokenizer st = new StringTokenizer(line);
                        int x = 0;
                        while (st.hasMoreTokens()) {
                            unitquals[x][y] = Double.parseDouble(st.nextToken());
                            x++;
                        }
                        y++;
                    }
                }
                br.close();
            } catch (Exception ex) {
            }

        }
        // gattusoend

        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                try {
                    Unit u = gsom.getLayer().getUnit(i, j);
                    if (u.getNumberOfMappedInputs() > 0) {
                        if (unitquals[u.getXPos()][u.getYPos()] > maxTE) { // gattuso
                            maxTE = unitquals[u.getXPos()][u.getYPos()]; // gattuso
                        }
                        if (unitquals[u.getXPos()][u.getYPos()] < minTE) { // gattuso
                            minTE = unitquals[u.getXPos()][u.getYPos()]; // gattuso
                        }
                    }
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                    // FIXME: don't use System.exit()!
                    System.exit(-1);
                } // getting the maxTE- and the minTE- Value -kind of Normalization relating to the colour palette
            }
        }

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        double ci = 0;
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
                    // ci =
                    // (int)Math.round(((unitquals[u.getXPos()][u.getYPos()]-minTE)/(maxTE-minTE))*(double)(paletteSize-1));
                    // //gattuso
                    // g.setPaint(palette[ci]); //mapping of the Value to the colour of the visualization
                    ci = 1.0 - ((unitquals[u.getXPos()][u.getYPos()] - minTE) / (maxTE - minTE) * 0.6 + 0.2);
                    if (unitquals[u.getXPos()][u.getYPos()] == 0.0) {
                        g.setPaint(Color.WHITE);
                    } else {
                        g.setPaint(new Color(Color.HSBtoRGB((float) 0.0, (float) 0.5, (float) ci))); // H-value==color==red
                    }

                } else {
                    g.setPaint(Color.WHITE);
                    // g.setPaint(new Color(Color.HSBtoRGB((float)0.0, (float)0.5, (float)1.0))); //H-value==color==red
                }
                g.setColor(null);
                g.fill(new Rectangle(x * unitWidth, y * unitHeight, unitWidth, unitHeight));
            }
        }
        return res;
    }

    private BufferedImage createMQEImage(GrowingSOM gsom, int width, int height, String cachefile) { // gattuso
        double maxTE8 = Double.MIN_VALUE;
        double minTE8 = Double.MAX_VALUE;

        // gattusostart
        double[][] unitquals = null;
        if (cachefile == null) {
            try {
                unitquals = topographicError.getUnitQualities("TE8_Unit");
            } catch (QualityMeasureNotFoundException e) {

            }
        } else {
            unitquals = new double[gsom.getLayer().getXSize()][gsom.getLayer().getYSize()];

            // aus Cachefile auslesen

            try {
                BufferedReader br = new BufferedReader(new FileReader(cachefile));
                String line = null;
                int y = 0;
                while ((line = br.readLine()) != null) {
                    if (line.trim() != "") {
                        StringTokenizer st = new StringTokenizer(line);
                        int x = 0;
                        while (st.hasMoreTokens()) {
                            unitquals[x][y] = Double.parseDouble(st.nextToken());
                            x++;
                        }
                        y++;
                    }
                }
                br.close();
            } catch (Exception ex) {
            }

        }
        // gattusoend

        // gattuso: da kommt ein try-catch weg
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                Unit u = null;
                try {
                    u = gsom.getLayer().getUnit(i, j);
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                    System.exit(-1);
                } // getting the maxTE- and the minTE- Value -kind of Normalization relating to the colour palette
                if (u.getNumberOfMappedInputs() > 0) {
                    if (unitquals[u.getXPos()][u.getYPos()] > maxTE8) { // gattuso
                        maxTE8 = unitquals[u.getXPos()][u.getYPos()]; // gattuso
                    }
                    if (unitquals[u.getXPos()][u.getYPos()] < minTE8) { // gattuso
                        minTE8 = unitquals[u.getXPos()][u.getYPos()]; // gattuso
                    }
                }
            }
        }

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        double ci = 0;// gattuso: da kommt ein try-catch weg
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
                    // ci =
                    // (int)Math.round(((unitquals[u.getXPos()][u.getYPos()]-minTE8)/(maxTE8-minTE8))*(double)(paletteSize-1));
                    // //gattuso
                    // g.setPaint(palette[ci]); //mapping of the Value to the colour of the visualization
                    ci = 1.0 - ((unitquals[u.getXPos()][u.getYPos()] - minTE8) / (maxTE8 - minTE8) * 0.6 + 0.2);
                    if (unitquals[u.getXPos()][u.getYPos()] == 0.0) {
                        g.setPaint(Color.WHITE);
                    } else {
                        g.setPaint(new Color(Color.HSBtoRGB((float) 0.0, (float) 0.5, (float) ci))); // H-value==color==red
                    }
                } else {
                    g.setPaint(Color.WHITE); // no Result
                    // g.setPaint(new Color(Color.HSBtoRGB((float)0.0, (float)0.25, (float)1.0))); //H-value==color==red
                }
                g.setColor(null);
                g.fill(new Rectangle(x * unitWidth, y * unitHeight, unitWidth, unitHeight));
            }
        }
        return res;
    }

}
