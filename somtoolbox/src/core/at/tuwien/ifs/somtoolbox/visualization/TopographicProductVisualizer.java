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
import at.tuwien.ifs.somtoolbox.layers.quality.TopographicProduct;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Visualization of some aspects of the Topographic Product Quality Measure, computation in {@link TopographicProduct}<br>
 * Notes: Only the measure relating to the Units will be drawn
 * 
 * @author Gerd Platzgummer
 * @version $Id: TopographicProductVisualizer.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class TopographicProductVisualizer extends AbstractBackgroundImageVisualizer implements QualityMeasureVisualizer {

    private TopographicProduct tp = null;

    public TopographicProductVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Topographic Product" };
        VISUALIZATION_SHORT_NAMES = new String[] { "TopographicProduct" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Topographic Product of a given k- Result mapped on the corresponding Map Units of the trained SOM.\n"
                + "H.U. Bauer and K.R. Pawelzik. Quantifying the neighborhood preservation of self- organizing feature maps\n"
                + "In: IEEE Transactions on Neural Networks 3, pages 570-579, 1992" };

        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR /* , SOMVisualisationData.QM_CACHE_FILE */};
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        // InputData data = null;
        // String cachefile = fileNames[1]; // cachefile auslesen, kann auch null sein wenn nicht angegeben vom user
        // fileNames[1] = null; // l�schen, damit das Popupfenster wieder kommt
        // setFileName(1, null); // nouamoll
        // if (cachefile == null) // kein Cachefile angegeben, daher neu berechnen
        // {
        // if (tp == null) {
        // if (fileNames[0] != null) {
        // InputData data = new SOMLibSparseInputData(fileNames[0], true, true, 1, 7);
        String cachefile = null;
        if (gsom.getSharedInputObjects().getData(neededInputObjects[0]) == null) {
            throw new SOMToolboxException("You need to specify " + neededInputObjects[0]);
        }

        InputData data = gsom.getSharedInputObjects().getInputData();

        tp = new TopographicProduct(gsom.getLayer(), data);
        return createTPImage(gsom, width, height, cachefile);
    }

    private BufferedImage createTPImage(GrowingSOM gsom, int width, int height, String cachefile) {
        double[][] unitquals = null;
        if (cachefile == null) {
            try {
                String k = (String) JOptionPane.showInputDialog(null, "K=:\n", "Customized Dialog",
                        JOptionPane.PLAIN_MESSAGE, null, null, null);
                unitquals = tp.getUnitQualities("TP_Unit|" + k);
            } catch (QualityMeasureNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            unitquals = readFromCacheFile(gsom, cachefile);
        }

        return createImage(gsom, width, height, unitquals);
    }

    /** Generates a visualisation with a fixed k */
    public BufferedImage createTPImage(GrowingSOM gsom, int width, int height, int k) {
        double[][] unitquals = null;
        tp = new TopographicProduct(gsom.getLayer(), gsom.getSharedInputObjects().getInputData());
        try {
            unitquals = tp.getUnitQualities("TP_Unit|" + k);
        } catch (QualityMeasureNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return createImage(gsom, width, height, unitquals);
    }

    private BufferedImage createImage(GrowingSOM gsom, int width, int height, double[][] unitquals) {
        double maxTP = Double.MIN_VALUE;
        double minTP = Double.MAX_VALUE;

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
                if (unitquals[u.getXPos()][u.getYPos()] > maxTP) {
                    maxTP = unitquals[u.getXPos()][u.getYPos()];
                }
                if (unitquals[u.getXPos()][u.getYPos()] < minTP) {
                    minTP = unitquals[u.getXPos()][u.getYPos()];
                }
                // }
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
                // if (u.getNumberOfMappedInputs()>0) {
                // ci = (int) (((unitquals[u.getXPos()][u.getYPos()]-minTP)/(maxTP-minTP))*255) ;
                // g.setPaint (new Color(0, 255-ci, 255-ci));
                ci = 1.0 - ((unitquals[u.getXPos()][u.getYPos()] - minTP) / (maxTP - minTP) * 0.6 + 0.2);
                g.setPaint(new Color(Color.HSBtoRGB((float) 0.87, (float) 0.5, (float) ci)));// H-value==color==blue

                // } else {
                // g.setPaint(Color.WHITE);
                // }
                g.setColor(null);
                g.fill(new Rectangle(x * unitWidth, y * unitHeight, unitWidth, unitHeight));
            }
        }
        return res;
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

    // }

}
