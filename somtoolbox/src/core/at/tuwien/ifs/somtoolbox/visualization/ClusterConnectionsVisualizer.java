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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math.util.MathUtils;

import cern.colt.matrix.DoubleMatrix2D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;

/**
 * Implementation of the Cluster Connections Visualisation as described in <i><b>D. Merkl and A. Rauber.</b> Proceedings
 * of the Workshop on Self-Organizing Maps (WSOM97), Helsinki, Finland, June 4-6 1997.
 * 
 * @author Robert Thurnher
 * @author Michael Groh
 * @author Rudolf Mayer
 * @version $Id: ClusterConnectionsVisualizer.java 3874 2010-11-02 14:14:38Z mayer $
 */
public class ClusterConnectionsVisualizer extends AbstractBackgroundImageVisualizer implements
        BackgroundImageVisualizer {

    private double t1 = 0.8;

    private double t2 = 1.1;

    private double t3 = 1.6;

    public ClusterConnectionsVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Cluster Connections" };
        VISUALIZATION_SHORT_NAMES = new String[] { "ClusterConn" };
        VISUALIZATION_DESCRIPTIONS = new String[] { " Implementation of the Cluster Connections Visualisation as described in \"D. Merkl and A. Rauber.\"\n"
                + "Proceedings of the Workshop on Self-Organizing Maps (WSOM97),\n"
                + "Helsinki, Finland, June 4-6 1997." };
        controlPanel = new ClusterConnectionsControlPanel();
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int currentVariant, int width, int height) {
        return appendToCacheKey(gsom, currentVariant, width, height, "t1:" + t1, "t2:" + t2, "t3:" + t3);
    }

    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        GrowingLayer layer = gsom.getLayer();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, width, height);

        // need to calculate some params relative to the unit width/height, which is relative to the desired output size
        // a unitWidth/Height of 10, as used previously, works by default only fine in the SOMViewer
        int somWidth = layer.getXSize();
        int somHeight = layer.getYSize();
        double unitWidth = width / somWidth;
        double unitHeight = height / somHeight;

        DoubleMatrix2D unitDistanceMatrix = gsom.getLayer().getUnitDistanceMatrix();

        for (int col = 0; col < somWidth; col++) {
            for (int row = 0; row < somHeight; row++) {

                if (col < somWidth - 1) {
                    // draw horizontal connection
                    double distanceRight = unitDistanceMatrix.get(layer.getUnitIndex(col, row), layer.getUnitIndex(
                            col + 1, row));
                    g.setPaint(getColor(distanceRight));
                    int xPos = (int) (col * unitHeight + unitHeight * 0.7);
                    int yPos = (int) (row * unitWidth + unitWidth * 0.4);
                    g.fillRect(xPos, yPos, (int) (unitWidth * 0.6), (int) (unitHeight * 0.2));
                }

                if (row < somHeight - 1) {
                    // draw vertical connection
                    double distanceLower = unitDistanceMatrix.get(layer.getUnitIndex(col, row), layer.getUnitIndex(col,
                            row + 1));
                    g.setPaint(getColor(distanceLower));

                    int xPos = (int) (col * unitHeight + unitHeight * 0.4);
                    int yPos = (int) (row * unitWidth + unitWidth * 0.7);
                    g.fillRect(xPos, yPos, (int) (unitWidth * 0.2), (int) (unitHeight * 0.6));
                }
            }
        }

        return image;
    }

    /** Gets the colour representing a certain distance value. */
    private Color getColor(double distance) {
        if (distance <= t1) {
            return Color.BLACK;
        } else if (distance > t1 && distance <= t2) {
            return Color.GRAY;
        } else if (distance > t2 && distance <= t3) {
            return Color.LIGHT_GRAY;
        } else if (distance > t3) {
            return Color.WHITE;
        } else {
            throw new IllegalStateException("This can't happen.");
        }
    }

    public class ClusterConnectionsControlPanel extends VisualizationControlPanel implements ChangeListener {
        private static final long serialVersionUID = 1L;

        private JCheckBox instantUpdateCheckBox = new JCheckBox("Instant update", true);;

        private JLabel t1label;

        private JLabel t2label;

        private JLabel t3label;

        private JSlider t1slider;

        private JSlider t2slider;

        private JSlider t3slider;

        public ClusterConnectionsControlPanel() {
            super("Cluster Connections Control Panel");
            instantUpdateCheckBox.setToolTipText("Indicate whether the visualisation should be updated right away, or only at the end of the slider dragging");

            t1slider = new JSlider(0, 200, (int) (t1 * 100));
            t1slider.addChangeListener(this);
            t2slider = new JSlider(0, 200, (int) (t2 * 100));
            t2slider.addChangeListener(this);
            t3slider = new JSlider(0, 200, (int) (t3 * 100));
            t3slider.addChangeListener(this);

            t1label = new JLabel("t1: " + t1);
            t2label = new JLabel("t2: " + t2);
            t3label = new JLabel("t3: " + t3);

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraintsIFS gc = new GridBagConstraintsIFS(GridBagConstraints.NORTHWEST,
                    GridBagConstraints.HORIZONTAL);
            panel.add(instantUpdateCheckBox, gc.nextRow());
            panel.add(t1label, gc.nextRow());
            panel.add(t1slider, gc.nextRow());
            panel.add(t2label, gc.nextRow());
            panel.add(t2slider, gc.nextRow());
            panel.add(t3label, gc.nextRow());
            panel.add(t3slider, gc.nextRow());
            add(panel, c);
        }

        /*** Handles state changes of sliders control. */
        @Override
        public void stateChanged(ChangeEvent event) {
            JSlider source = (JSlider) event.getSource();

            if (source.equals(t1slider)) {
                if (t2slider.getValue() < t1slider.getValue()) {
                    t2slider.setValue(source.getValue());
                }
                if (instantUpdateCheckBox.isSelected() || !source.getValueIsAdjusting()) {
                    t1 = t1slider.getValue() / 100d;
                    t1label.setText("t1: " + t1);
                }
            } else if (source.equals(t2slider)) {
                if (t3slider.getValue() < t2slider.getValue()) {
                    t3slider.setValue(t2slider.getValue());
                }
                if (t1slider.getValue() > t2slider.getValue()) {
                    t1slider.setValue(t2slider.getValue());
                }
                if (instantUpdateCheckBox.isSelected() || !source.getValueIsAdjusting()) {
                    t2 = t2slider.getValue() / 100d;
                    t2label.setText("t2: " + t2);
                }
            } else if (source.equals(t3slider)) {
                if (t2slider.getValue() > t3slider.getValue()) {
                    t2slider.setValue(t3slider.getValue());
                }
                if (instantUpdateCheckBox.isSelected() || !source.getValueIsAdjusting()) {
                    t3 = t3slider.getValue() / 100d;
                    t3label.setText("t3: " + t3);
                }
            }
            if (instantUpdateCheckBox.isSelected() || !source.getValueIsAdjusting()) {
                visualizationUpdateListener.updateVisualization();
            }
        }
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        HashMap<String, BufferedImage> res = new HashMap<String, BufferedImage>();
        // modify t1 0.7 - 0.9
        // modify t2 1.0 - 1.2
        // modify t3 1.5 - 1.7
        double oldT1 = t1; // save original values
        double oldT2 = t2;
        double oldT3 = t3;

        double stepWidth = 0.05;
        for (t1 = 0.7; MathUtils.round(t1, 2) <= 0.9; t1 += stepWidth) {
            for (t2 = Math.max(t1, 1.0); MathUtils.round(t2, 2) <= 1.2; t2 += stepWidth) {
                for (t3 = Math.max(t2, 1.5); MathUtils.round(t3, 2) <= 1.7; t3 += stepWidth) {
                    String key = String.format("_%.2f_%.2f_%.2f", t1, t2, t3);
                    res.put(key, getVisualization(index, gsom, width, height));
                }
            }
        }

        t1 = oldT1; // reset to original values
        t2 = oldT2;
        t3 = oldT3;

        return res;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            int maxFlavours) throws SOMToolboxException {
        // TODO Auto-generated method stub
        return super.getVisualizationFlavours(index, gsom, width, height, maxFlavours);
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException {
        // FIXME: Implement this
        return super.getVisualizationFlavours(index, gsom, width, height, flavourParameters);
    }

}
