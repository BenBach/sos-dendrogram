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
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.math.plot.Plot2DPanel;
import org.math.plot.Plot3DPanel;
import org.math.plot.PlotPanel;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.helper.DataSetGenerator;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.util.CentredDialog;

/**
 * Implements a {@link JDialog} for displaying a 2 or 3 dimensional data set. It is basically a wrapper around
 * {@link Plot2DPanel} and {@link Plot3DPanel}, respectively.<br>
 * The data given has to follow the same format as expected by the <code>Plot2DPanel</code> and <code>Plot3DPanel</code>
 * , i.e. an double[][], where the first index is the data item, and the second index are the X, Y and possible Z
 * coordinates, i.e. an array of double[#items][2] or double[#items][3].
 * 
 * @author Rudolf Mayer
 * @version $Id: DataSetViewer.java 3832 2010-10-06 21:26:23Z mayer $
 */
public class DataSetViewer extends CentredDialog implements SOMToolboxApp {

    private static final long serialVersionUID = 1L;

    public static Type APPLICATION_TYPE = Type.Utils;

    public static String DESCRIPTION = "";

    public static String LONG_DESCRIPTION = DESCRIPTION;

    public static Parameter[] OPTIONS = { OptionFactory.getOptInputVectorFile(false) };

    /** Runs the demo stand-alone application */
    public static void main(String[] args) {

        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);
        String inputVector = OptionFactory.getFilePath(config, "inputVectorFile");

        if (inputVector != null) {
            InputData data = InputDataFactory.open(inputVector);
            if (data.dim() > 3) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Can only visualise data sets of at most three dimensions - provided data set has a dimensionality of "
                                + data.dim() + ". Aborting.");
                return;
            }
            DataSetViewer viewer1 = new DataSetViewer(null, inputVector, data.getData());
            viewer1.setLocation(viewer1.getLocation().x - 300, viewer1.getLocation().y);

        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Showing a demo with a generated data set");
            int n = 10;
            int dim = 3;
            double[][] datas1 = new double[n][dim];
            double[][] datas2 = new double[n][dim];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < dim; j++) {
                    datas1[i][j] = Math.random();
                    datas2[i][j] = Math.random();
                }
            }

            DataSetGenerator generator = new DataSetGenerator();
            double[][][] data = generator.getData();

            JFrame frame = new JFrame("Dataset Viewer Demo Frame");
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
            DataSetViewer viewer1 = new DataSetViewer(frame, "2D - Data set", DataSetGenerator.CLASS_NAMES, data);
            viewer1.setLocation(viewer1.getLocation().x - 300, viewer1.getLocation().y);
            DataSetViewer viewer2 = new DataSetViewer(frame, "3D - Data set", new String[] { "data1", "data2" },
                    new double[][][] { datas1, datas2 });
            viewer2.setLocation(viewer2.getLocation().x + 300, viewer2.getLocation().y);
        }
    }

    private String[] classNames;

    private Color[] colors;

    private double[][][] dataSets;

    private PlotPanel plotPanel;

    private String[][] dataNames;

    private JFrame owner;

    public DataSetViewer(JFrame owner, String title, double[][] data) {
        this(owner, title, new String[] { "Data" }, new double[][][] { data });
    }

    public DataSetViewer(JFrame owner, String title, String[] classNames, Color[] colors, double[][][] dataSets) {
        super(owner, title, false);
        if (owner == null) {
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }
        this.classNames = classNames;
        this.dataSets = dataSets;
        this.colors = colors;
        this.owner = owner;
        initPlot();
    }

    public DataSetViewer(JFrame owner, String title, String[] classNames, double[][][] dataSets, String[][] dataNames) {
        super(owner, title, false);
        if (owner == null) {
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }
        this.classNames = classNames;
        this.dataSets = dataSets;
        this.dataNames = dataNames;
        this.owner = owner;
        initPlot();
    }

    public DataSetViewer(JFrame owner, String title, String[] classNames, double[][][] dataSets) {
        this(owner, title, classNames, (Color[]) null, dataSets);
    }

    public DataSetViewer(SOMViewer viewer, double[][] data) {
        this(viewer, "Data Set Viewer", data);
    }

    public DataSetViewer(SOMViewer viewer, String[] classNames, Color[] colors, double[][][] data) {
        this(viewer, "Data Set Viewer", classNames, colors, data);
    }

    private void initPlot() {
        if (dataSets[0][0].length == 2) {
            plotPanel = new Plot2DPanel();
            // Data plots addition
            for (int i = 0; i < classNames.length; i++) {
                if (colors != null) {
                    ((Plot2DPanel) plotPanel).addScatterPlot(classNames[i], colors[i], dataSets[i]);
                } else {
                    ((Plot2DPanel) plotPanel).addScatterPlot(classNames[i], dataSets[i]);
                }
            }
        } else if (dataSets[0][0].length == 3) {
            plotPanel = new Plot3DPanel();
            // Data plots addition
            for (int i = 0; i < classNames.length; i++) {
                if (colors != null) {
                    ((Plot3DPanel) plotPanel).addScatterPlot(classNames[i], colors[i], dataSets[i]);
                } else {
                    ((Plot3DPanel) plotPanel).addScatterPlot(classNames[i], dataSets[i]);
                }
            }
        }

        for (int i = 0; i < dataSets.length; i++) {
            if (dataNames != null) {
                for (int j = 0; j < dataSets[i].length; j++) {
                    plotPanel.addLabel(dataNames[i][j], Color.LIGHT_GRAY, dataSets[i][j]);

                }
            }
        }

        if (classNames.length > 1) {
            plotPanel.addLegend("SOUTH");
        }

        getContentPane().add(plotPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        if (owner == null) {
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            device.setFullScreenWindow(this);
        } else {
            pack();
        }
    }
}
