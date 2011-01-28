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

import javax.swing.JOptionPane;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.layers.quality.Trustworthiness_NeighborhoodPreservation;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Visualization of some aspects of Quality Measure 6: Trustwothiness: computation in layers/quality/Q6Q7_TWNP.java<br>
 * Notes: Only the measure relating to the Units will be drawn
 * 
 * @author Gerd Platzgummer
 * @version $Id: TrustwothinessVisualizer.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class TrustwothinessVisualizer extends AbstractBackgroundImageVisualizer implements QualityMeasureVisualizer {

    private Trustworthiness_NeighborhoodPreservation tw = null;

    public TrustwothinessVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Trustworthiness" };
        VISUALIZATION_SHORT_NAMES = new String[] { "Trustworthiness" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Trustworthiness: J. Venna and S. Kaski, Neighborhood Preservation in nonlinear projection methods.\n"
                + "In: An experimental study, Artificial Neural Networks- ICANN 2001 (Berlin), p. 485-491, 2001 " };
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR /* , SOMVisualisationData.QM_CACHE_FILE */};
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        // InputData data = null;
        // String cachefile = fileNames[1]; // cachefile auslesen, kann auch null sein wenn nicht angegeben vom user
        // fileNames[1] = null; // löschen, damit das Popupfenster wieder kommt
        // setFileName(1, null); // nouamoll
        // if (cachefile == null) // kein Cachefile angegeben, daher neu berechnen
        // {
        // if (tw == null) {
        // if (fileNames[0] != null) {
        // InputData data = new SOMLibSparseInputData(fileNames[0], true, true, 1, 7);
        String cachefile = null;
        if (gsom.getSharedInputObjects().getData(neededInputObjects[0]) == null) {
            throw new SOMToolboxException("You need to specify " + neededInputObjects[0]);
        }

        InputData data = gsom.getSharedInputObjects().getInputData();
        tw = new Trustworthiness_NeighborhoodPreservation(gsom.getLayer(), data);
        return createTWImage(gsom, width, height, cachefile);
    }

    private BufferedImage createTWImage(GrowingSOM gsom, int width, int height, String cachefile) {
        double[][] unitquals = null;
        if (cachefile == null) {
            try {
                String k = (String) JOptionPane.showInputDialog(null, "K=:\n", "Customized Dialog",
                        JOptionPane.PLAIN_MESSAGE, null, null, null);
                unitquals = tw.getUnitQualities("TW_Unit|" + k);
            } catch (QualityMeasureNotFoundException e) {

            }
        } else {
            unitquals = readFromCacheFile(gsom, cachefile);
        }
        return createTWImage(gsom, width, height, unitquals);
    }

    /** /** Generates a visualisation with a fixed k */
    public BufferedImage createTWImage(GrowingSOM gsom, int width, int height, int k) {
        double[][] unitquals = null;
        InputData data = gsom.getSharedInputObjects().getInputData();
        tw = new Trustworthiness_NeighborhoodPreservation(gsom.getLayer(), data);

        try {
            unitquals = tw.getUnitQualities("TW_Unit|" + k);
        } catch (QualityMeasureNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return createTWImage(gsom, width, height, unitquals);
    }

    private double[][] readFromCacheFile(GrowingSOM gsom, String cachefile) {
        double[][] unitquals;
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
        return unitquals;
    }

    private BufferedImage createTWImage(GrowingSOM gsom, int width, int height, double[][] unitquals) {
        double maxTW = Double.MIN_VALUE;
        double minTW = Double.MAX_VALUE;
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                Unit u = null;
                try {
                    u = gsom.getLayer().getUnit(i, j);
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                    System.exit(-1);
                }
                // if (u.getNumberOfMappedInputs()>0) {
                if (unitquals[u.getXPos()][u.getYPos()] > maxTW) {
                    maxTW = unitquals[u.getXPos()][u.getYPos()];
                }
                if (unitquals[u.getXPos()][u.getYPos()] < minTW) {
                    minTW = unitquals[u.getXPos()][u.getYPos()];
                }
                // }
            }
        }

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        int ci = 0;
        for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
            for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                Unit u = null;
                try {
                    u = gsom.getLayer().getUnit(x, y);
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                    System.exit(-1);
                }
                // if (u.getNumberOfMappedInputs()>0) {
                // ci =
                // (int)Math.round(((qe.getUnitQualities("QE_UnitQE")[u.getXPos()][u.getYPos()]-minQE)/(maxQE-minQE))*(double)(paletteSize-1));
                // g.setPaint(palette[ci]);
                ci = (int) ((unitquals[u.getXPos()][u.getYPos()] - minTW) / (maxTW - minTW) * 255);
                g.setPaint(new Color(255 - ci, 255, 170));
                // } else {
                // g.setPaint(Color.WHITE);
                // }
                g.setColor(null);
                g.fill(new Rectangle(x * unitWidth, y * unitHeight, unitWidth, unitHeight));
            }
        }
        return res;
    }

    // }

}
