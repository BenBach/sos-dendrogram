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
package at.tuwien.ifs.somtoolbox.apps;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFrame;

import org.math.plot.Plot3DPanel;
import org.math.plot.plots.ScatterPlot;
import org.math.plot.render.AbstractDrawer;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;

/**
 * Plots a unit file, can be used especially to plot a 3D-SOM.
 * 
 * @author Jakob Frank
 * @version $Id: UnitFileViewer.java 3665 2010-07-15 08:58:53Z frank $
 */
public class UnitFileViewer extends JFrame implements SOMToolboxApp {
    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptUnitDescriptionFile(true),
            OptionFactory.getOptShowLabels(), OptionFactory.getSwitchVerboose() };

    public static final String DESCRIPTION = "Plots a unit file, can be used especially to plot a 3D-SOM";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Viewer;

    private static final long serialVersionUID = 1L;

    private static final int CLS = 5;

    private Plot3DPanel plotPanel;

    private Color dotColor, labelColor;

    public Color getDotColor() {
        return dotColor;
    }

    public void setDotColor(Color dotColor) {
        this.dotColor = dotColor;
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(Color labelColor) {
        this.labelColor = labelColor;
    }

    public UnitFileViewer(double[][][] data) {
        this(null, data);
    }

    public UnitFileViewer(String title, double[][][] data) {
        this(data, null);
    }

    public UnitFileViewer(double[][][] data, String[][] labels) {
        this("Unit File Viewer", data, null);
    }

    public UnitFileViewer(String title, double[][][] data, String[][] labels) {

        dotColor = Color.red;
        labelColor = Color.black;

        initialize();
        setTitle(title);

        // plot(data, labels);
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());

        plotPanel = new Plot3DPanel();

        getContentPane().add(plotPanel);

        pack();
    }

    public void plot(double[][][] data) {
        plot(data, null, 0, 0, 0);
    }

    public void plot(double[][][] data, String[][] labels, double xScale, double yScale, double zScale) {
        for (int i = 0; i < data.length; i++) {
            if (data[i].length > 0) {
                ScatterPlot plot = new ScatterPlot("Plot_" + (i + 1), dotColor, AbstractDrawer.ROUND_DOT,
                        AbstractDrawer.DEFAULT_DOT_RADIUS + i, data[i]);
                plotPanel.addPlot(plot);

                if (xScale != 0 && yScale != 0 && zScale != 0) {
                    plotPanel.setFixedBounds(0, 0d, xScale);
                    plotPanel.setFixedBounds(1, 0d, yScale);
                    plotPanel.setFixedBounds(2, 0d, zScale);
                }

                if (labels != null) {
                    for (int j = 0; j < data[i].length; j++) {
                        plotPanel.addLabel(labels[i][j], labelColor, data[i][j]);
                    }
                }
            }
        }
    }

    public void gridPlot(double[][][] data, String[][] labels, double xScale, double yScale, double zScale) {
        // ScatterPlot plot = new ScatterPlot("adf", Color.BLUE, null, null);
        for (int i = 0; i < data.length; i++) {
            if (data[i].length > 0) {

                // TODO remove 'Title'
                plotPanel.addLinePlot("Lines_" + (i + 1), data[i]);
                // FIXME +i for massive dots
                plotPanel.addPlot(new ScatterPlot("Plot_" + (i + 1), dotColor, AbstractDrawer.ROUND_DOT,
                        AbstractDrawer.DEFAULT_DOT_RADIUS, data[i]));

                // TODO
                if (xScale != 0 && yScale != 0 && zScale != 0) {
                    plotPanel.setFixedBounds(0, 0d, xScale);
                    plotPanel.setFixedBounds(1, 0d, yScale);
                    plotPanel.setFixedBounds(2, 0d, zScale);
                }
                // plotPanel.setFixedBounds(0, 0d, 10d);
                // plotPanel.setFixedBounds(1, 0d, 10d);
                // plotPanel.setFixedBounds(2, 0d, 10d);
                if (labels != null) {
                    for (int j = 0; j < data[i].length; j++) {
                        plotPanel.addLabel(labels[i][j], labelColor, data[i][j]);
                    }
                }
            }
        }
    }

    /** Method for stand-alone execution. */
    public static void main(String[] args) {

        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String udf = config.getString("unitDescriptionFile");
        int showLabels = config.getInt("showLabels");
        boolean verbose = config.getBoolean("verbose");
        try {

            SOMLibFormatInputReader slfir = new SOMLibFormatInputReader(null, udf, null);

            int xSize, ySize, zSize;
            xSize = slfir.getXSize();
            ySize = slfir.getYSize();
            zSize = slfir.getZSize();

            int maxUnits = xSize * ySize * zSize;

            // Search the max:
            int max = 0;
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    for (int z = 0; z < zSize; z++) {
                        try {
                            if (max < slfir.getNrVecMapped(x, y, z)) {
                                max = slfir.getNrVecMapped(x, y, z);
                            }
                        } catch (IndexOutOfBoundsException e) {

                        }
                    }
                }
            }
            final int classes = Math.min(max, CLS);
            int stepW = Math.max(max / classes, 1);

            double[][][] _data = new double[classes][maxUnits][3];
            int[] _index = new int[classes];
            String[][] _labels = new String[classes][maxUnits];

            // Loading the Points
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    for (int z = 0; z < zSize; z++) {
                        if (slfir.getNrVecMapped(x, y, z) > 0) {
                            int s = Math.max(slfir.getNrVecMapped(x, y, z) / stepW - 1, 0);
                            _data[s][_index[s]][0] = x + 0.5;
                            _data[s][_index[s]][1] = y + 0.5;
                            _data[s][_index[s]][2] = z + 0.5;

                            String[] ls = slfir.getMappedVecs(x, y, z);
                            int maxLabels = Math.min(ls.length, 1);
                            String l = null;
                            for (int i = 0; i < maxLabels; i++) {
                                if (l == null) {
                                    l = ls[i];
                                } else {
                                    l += ", " + ls[i];
                                }
                            }
                            _labels[s][_index[s]] = l;

                            _index[s]++;
                        }
                    }
                }
            }

            // Cleaning
            double[][][] data = new double[classes][][];
            String[][] labels = new String[classes][];

            for (int i = 0; i < data.length; i++) {
                data[i] = new double[_index[i]][3];
                labels[i] = new String[_index[i]];
                for (int j = 0; j < data[i].length; j++) {
                    for (int j2 = 0; j2 < data[i][j].length; j2++) {
                        data[i][j][j2] = _data[i][j][j2];
                    }
                    labels[i][j] = _labels[i][j];

                }
            }

            if (verbose) {
                checkData(data, labels);
            }

            UnitFileViewer v;
            if (showLabels > 0) {
                v = new UnitFileViewer(new File(slfir.getUnitDescriptionFileName()).getName(), data, labels);
                v.plot(data, labels, 0, 0, 0);
            } else {
                v = new UnitFileViewer(new File(slfir.getUnitDescriptionFileName()).getName(), data);
                v.plot(data, null, 0, 0, 0);
            }
            v.setSize(800, 800);
            v.setVisible(true);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SOMLibFileFormatException e) {
            e.printStackTrace();
        }

    }

    private static void checkData(double[][][] data, String[][] labels) {
        System.out.println();
        System.out.println();
        for (int i = 0; i < data.length; i++) {
            System.out.println("DataSet_" + i);
            for (int j = 0; j < data[i].length; j++) {
                System.out.print("  ");
                for (int k = 0; k < data[i][j].length; k++) {
                    System.out.print(data[i][j][k] + " ");
                }
                if (labels != null) {
                    System.out.print(labels[i][j]);
                }
                System.out.println();
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

}
