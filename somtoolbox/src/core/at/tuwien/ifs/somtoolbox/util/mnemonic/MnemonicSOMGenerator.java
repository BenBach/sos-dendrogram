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
package at.tuwien.ifs.somtoolbox.util.mnemonic;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.util.UiUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: MnemonicSOMGenerator.java 3883 2010-11-02 17:13:23Z frank $
 */
public class MnemonicSOMGenerator extends JFrame implements ChangeListener, SOMToolboxApp {
    private static final long serialVersionUID = 1L;

    private static final short DEFAULT_ROWS = 8;

    private static final short DEFAULT_COLS = (short) 15;

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static final String DESCRIPTION = "MnemonicSOMGenerator allows to create an arbitrary shaped SOM (unit file)";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptBackgroundImage(true),
            new FlaggedOption("totalNodes", JSAP.INTEGER_PARSER, null, false, 'n', "nodes"),
            new FlaggedOption("rows", JSAP.SHORT_PARSER, null, false, 'r', "rows"),
            new FlaggedOption("cols", JSAP.SHORT_PARSER, null, false, 'c', "columns") };

    private static final String APP_ICON = "resources/icons/somviewer_logo-24.png";

    private static final Logger logger = Logger.getLogger(MnemonicSOMGenerator.class.getName());

    private BorderLayout mainBorderLayout = new BorderLayout();

    private BorderLayout controlsPanelBorderLayout = new BorderLayout();

    private GridBagLayout labelpanelGridBagLayout = new GridBagLayout();

    private JButton buttonExit;

    private JButton buttonLoad;

    private JButton buttonSave;

    private JFileChooser fileChooser = new JFileChooser();

    private JLabel labelActiveNodesTitle;

    private JLabel labelActiveNodesValue;

    private JLabel labelStatus;

    private JPanel controlsPanel = new JPanel();

    private JPanel buttonPanel = new JPanel();

    private JPanel labelPanel = new JPanel();

    private JSpinner nodeSlider = new JSpinner();

    private MapPanel mapPanel;

    private JButton buttonSaveImage = new JButton();

    private int enabledNodes = 0;

    public MnemonicSOMGenerator(short cols, short rows) {
        this(null, cols, rows);
    }

    public MnemonicSOMGenerator(String image, short cols, short rows) {
        boolean[][] toDraw = new boolean[cols][rows];
        for (int i = 0; i < toDraw.length; i++) {
            for (int j = 0; j < toDraw[0].length; j++) {
                toDraw[i][j] = true;
            }
        }
        mapPanel = new MapPanel(toDraw, image);

        initFrame();
    }

    private void initFrame() {
        UiUtils.setSOMToolboxLookAndFeel();
        setIconImage(Toolkit.getDefaultToolkit().getImage(SOMViewer.class.getResource(APP_ICON)));

        setResizable(false);
        jbInit();
        registerListeners();
        updateNodeCount();
        pack();
    }

    public MnemonicSOMGenerator(String image, int totalNodes) {
        if (image != null) {
            mapPanel = new MapPanel(totalNodes, image);
        }

        initFrame();
    }

    public static void main(String[] args) {
        JSAP jsap = OptionFactory.registerOptions(OPTIONS);
        JSAPResult result = OptionFactory.parseResults(args, jsap);
        if (!result.success()) {
            return;
        }

        File imageFile = result.getFile("backgroundImage");

        if (result.userSpecified("rows") != result.userSpecified("cols")) {
            // Error
            OptionFactory.printUsage(jsap, MnemonicSOMGenerator.class.getSimpleName(), result,
                    "You must either specify both or none of \"rows\" and \"columns\".");
        } else if (result.userSpecified("rows") && result.userSpecified("cols")) {
            if (result.userSpecified("totalNodes")) {
                logger.warning("Specified \"rows\", \"columns\" and \"totalNodes\". Ignoring \"totalNodes\".");
            }
            // XY
            new MnemonicSOMGenerator(imageFile.getAbsolutePath(), result.getShort("cols"), result.getShort("rows")).setVisible(true);
        } else if (result.userSpecified("totalNodes")) {
            // NODES
            new MnemonicSOMGenerator(imageFile.getAbsolutePath(), result.getInt("totalNodes")).setVisible(true);
        } else {
            new MnemonicSOMGenerator(imageFile.getAbsolutePath(), DEFAULT_COLS, DEFAULT_ROWS).setVisible(true);
        }
        // new MnemonicSOMGenerator(DEFAULT_COLS, DEFAULT_ROWS).setVisible(true);
    }

    private void jbInit() {
        this.getContentPane().setLayout(mainBorderLayout);
        this.setTitle("Mnemonic Map Creator");

        controlsPanel.setLayout(controlsPanelBorderLayout);
        labelPanel.setLayout(labelpanelGridBagLayout);

        buttonSaveImage = new JButton("saveImage");
        buttonSaveImage.setMnemonic('i');
        buttonSaveImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveImage(e);
            }
        });

        labelActiveNodesTitle = new JLabel("active Nodes: ");
        labelActiveNodesValue = new JLabel("...");

        labelStatus = new JLabel("<<status>>");
        labelStatus.setAlignmentX((float) 0.5);
        labelStatus.setHorizontalAlignment(SwingConstants.CENTER);

        buttonExit = new JButton("exit");
        buttonExit.setMnemonic('E');

        buttonLoad = new JButton("load Map");
        buttonLoad.setMnemonic('L');

        buttonSave = new JButton("save Map");
        buttonSave.setMnemonic('S');

        nodeSlider.setModel(new SpinnerNumberModel(mapPanel.getNodeCount(), 1, 10000, 1));
        nodeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newNodeCount = ((Integer) nodeSlider.getValue()).intValue();
                mapPanel.createNodes(newNodeCount);
                mapPanel.repaint();
            }
        });

        this.getContentPane().add(mapPanel, BorderLayout.CENTER);

        buttonPanel.add(buttonSave, null);
        buttonPanel.add(buttonSaveImage, null);
        buttonPanel.add(buttonLoad, null);
        buttonPanel.add(buttonExit, null);
        buttonPanel.add(nodeSlider);

        controlsPanel.add(buttonPanel, BorderLayout.SOUTH);

        labelPanel.add(labelActiveNodesTitle, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        labelPanel.add(labelActiveNodesValue, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        labelPanel.add(labelStatus, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        controlsPanel.add(labelPanel, BorderLayout.CENTER);
        this.getContentPane().add(controlsPanel, BorderLayout.SOUTH);
    }

    private void registerListeners() {
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveMap();
            }
        });
        buttonLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSOM();
            }
        });
        buttonExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitApplication();
            }
        });
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
        mapPanel.addChangeListener(this);
    }

    public void exitApplication() {
        System.exit(0);
    }

    /**
     * 
     *
     */
    public void saveMap() {
        int vectorDimension = 10;
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                labelStatus.setText("saving map....");

                // write the unit description file
                BufferedWriter fileOutUnit = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile().getPath()
                        + SOMLibFormatInputReader.unitFileNameSuffix));
                fileOutUnit.write("$TYPE rect\n");
                fileOutUnit.write("$XDIM " + mapPanel.getToDraw().length + "\n");
                fileOutUnit.write("$YDIM " + mapPanel.getToDraw()[0].length + "\n");

                for (int col = 0; col < mapPanel.getToDraw().length; col++) {
                    for (int row = 0; row < mapPanel.getToDraw()[0].length; row++) {
                        if (mapPanel.getToDraw()[col][row]) {
                            fileOutUnit.write("$POS_X " + col + "\n");
                            fileOutUnit.write("$POS_Y " + row + "\n");
                            fileOutUnit.write("$UNIT_ID " + fileChooser.getSelectedFile().getPath() + "_(" + col + "/"
                                    + row + ")\n");
                        }
                    }
                }
                fileOutUnit.flush();
                fileOutUnit.close();

                // write the map description file
                BufferedWriter fileOutMap = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile().getPath()
                        + ".map"));
                fileOutMap.write("$TYPE som\n");
                fileOutMap.write("$XDIM " + mapPanel.getToDraw().length + "\n");
                fileOutMap.write("$YDIM " + mapPanel.getToDraw()[0].length + "\n");
                fileOutMap.write("$VEC_DIM " + vectorDimension + "\n");

                fileOutMap.write("$METRIC at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric\n");
                fileOutMap.write("$LAYER_REVISION $Revision: 3883 $");

                fileOutMap.flush();
                fileOutMap.close();

                labelStatus.setText("Map saved successfully to '" + fileChooser.getSelectedFile().getPath() + "'!");

            } catch (FileNotFoundException e) {
                System.out.println(e);
                e.getStackTrace();
            } catch (IOException e) {
                System.out.println(e);
                e.getStackTrace();
            }
        }
    }

    public void loadSOM() {
        // if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        // labelStatus.setText("loading map....");
        // TODO: implement
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        updateNodeCount();
    }

    private void updateNodeCount() {
        System.out.println("updating node count");
        enabledNodes = 0;
        for (int i = 0; i < mapPanel.getToDraw().length; i++) {
            for (int j = 0; j < mapPanel.getToDraw()[0].length; j++) {
                if (mapPanel.getToDraw()[i][j]) {
                    enabledNodes++;
                }
            }
        }
        labelActiveNodesValue.setText(String.valueOf(enabledNodes));
    }

    public void saveImage(ActionEvent e) {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            labelStatus.setText("saved image to: " + mapPanel.saveScreenToImage(fileChooser.getSelectedFile()));
        }
    }
}