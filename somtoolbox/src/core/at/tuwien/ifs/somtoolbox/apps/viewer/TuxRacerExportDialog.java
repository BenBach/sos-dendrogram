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
package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.util.CentredDialog;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;

/**
 * @author Jakob Frank
 * @version $Id: TuxRacerExportDialog.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class TuxRacerExportDialog extends CentredDialog {

    private static final String CONVERT_BINARY = "convert";

    private static final String CONVERT_CMDLINE = "%s %s %s";

    private static final String ELEVATION_PALETTE = "TuxElevation";

    private static final String TERRAIN_PALETTE = "TuxTerrain";

    private static final String FILENAME_TCL = "course.tcl";

    private static final String FILENAME_ELEVATION = "elevation.rgb";

    private static final String FILENAME_TREES = "trees.rgb";

    private static final String FILENAME_TERRAIN = "terrain.rgb";

    private static final String FILENAME_SONGMAPPING = "music.tcl";

    private static final Color NOTE_MARKER_COLOR = new Color(0, 232, 255);

    // private static final Color TERRAIN_WATER = new Color(0, 0, 0);

    // private static final Color TERRAIN_GRAS = new Color(127, 127, 127);

    // private static final Color TERRAIN_SAND = new Color(255, 255, 255);

    private static final long serialVersionUID = 1L;

    private static final String MAP_AUTHOR = "The PlaySOM Team";// <http://olymp.ifs.tuwien.ac.at/somtoolbox>";

    private final CommonSOMViewerStateData csState;

    public TuxRacerExportDialog(JFrame owner, CommonSOMViewerStateData state) {
        super(owner, "TuxRacer Export", false);
        this.csState = state;

        initialize();
    }

    private void initialize() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        getContentPane().add(main);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;
        gbc.weightx = 1d;
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = 0;
        gbcLabel.weightx = 0;
        gbcLabel.fill = GridBagConstraints.BOTH;

        main.add(new JLabel("Save to:"), gbcLabel);
        JPanel dirPanel = new JPanel();
        dirPanel.setLayout(new BorderLayout());
        final JTextField txtDir = new JTextField();
        dirPanel.add(txtDir, BorderLayout.CENTER);
        JButton btnSelDir = new JButton("...");
        btnSelDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                txtDir.setText("");
            }
        });
        dirPanel.add(btnSelDir, BorderLayout.EAST);
        main.add(dirPanel, gbc);

        gbc.gridy++;
        gbcLabel.gridy++;

        main.add(new JLabel("Mapname:"), gbcLabel);
        final JTextField txtName = new JTextField();
        main.add(txtName, gbc);

        gbc.gridy++;
        gbcLabel.gridy++;

        gbc.gridwidth = 1;
        main.add(new JLabel("Unit Size (px):"), gbcLabel);
        final SpinnerNumberModel unitSize = new SpinnerNumberModel();
        final JLabel lblMS = new JLabel();
        unitSize.setMinimum(1);
        unitSize.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int x = csState.growingLayer.getXSize();
                int y = csState.growingLayer.getYSize();
                int w = unitSize.getNumber().intValue();
                lblMS.setText(String.format("Mapsize: %d x %d px", w * x, w * y));
            }
        });
        unitSize.setValue(25);
        main.add(new JSpinner(unitSize), gbc);
        main.add(lblMS, gbc);
        gbc.gridwidth = 2;

        gbc.gridy++;
        gbcLabel.gridy++;

        gbcLabel.gridwidth = 3;
        final JCheckBox chkCopyMusic = new JCheckBox("copy audio files");
        main.add(chkCopyMusic, gbcLabel);
        gbcLabel.gridwidth = 1;

        gbc.gridy++;
        gbcLabel.gridy++;

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        });
        main.add(btnCancel, gbcLabel);

        JButton btnExport = new JButton("Export");
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    exportTuxMap(new File(txtDir.getText()), txtName.getText(), unitSize.getNumber().intValue(),
                            chkCopyMusic.isSelected());
                    closeDialog();
                } catch (SOMToolboxException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        main.add(btnExport, gbc);

        pack();
    }

    private void exportTuxMap(File toDir, String mapName, int unitSize, boolean copyAudioFiles)
            throws SOMToolboxException, IOException {
        File mapDir = new File(toDir, mapName.toLowerCase().replaceAll(" ", ""));
        int xSize = unitSize * csState.growingLayer.getXSize();
        int ySize = unitSize * csState.growingLayer.getYSize();

        // Prepare map...
        boolean wasExactPlacement = csState.exactUnitPlacement;

        // Create the mapDir
        mapDir.mkdirs();

        saveElevationFile(mapDir, unitSize);
        saveTerrainFile(mapDir, unitSize);
        saveSongPositions(mapDir, unitSize);
        saveTCL(mapDir, mapName, xSize, ySize);
        doPostProcessing(mapDir, mapName, xSize, ySize, copyAudioFiles);

        // Rest map...
        csState.mapPNode.changePalette(csState.getSOMViewer().getCurrentlySelectedPalette());
        csState.exactUnitPlacement = wasExactPlacement;
        csState.mapPNode.reInitUnitDetails();
    }

    private void doPostProcessing(File mapDir, String mapName, int size, int size2, boolean copyAudioFiles) {
        // Convert *.bmp to *.sgi and rename to *.rgb
        convertImage(mapDir, FILENAME_TREES);
        convertImage(mapDir, FILENAME_ELEVATION);
        convertImage(mapDir, FILENAME_TERRAIN);

        File musicDir = new File(mapDir, "music");
        musicDir.mkdir();
        if (copyAudioFiles) {
            // FIXME Copy seems not to work...
            String[] fileList = csState.inputDataObjects.getInputData().getLabels();
            for (String dataItem : fileList) {
                String src = CommonSOMViewerStateData.fileNamePrefix + dataItem
                        + CommonSOMViewerStateData.fileNameSuffix;
                String dst = musicDir.getAbsolutePath() + File.separator + dataItem;

                // Create Dir-Structure
                new File(dst).getParentFile().mkdirs();
                FileUtils.copyFileSafe(dst, src);
            }
        }
    }

    private boolean convertImage(File mapDir, String fileNameBase) {
        if (runAndWaitExternalCommand(String.format(CONVERT_CMDLINE, CONVERT_BINARY, fileNameBase + ".bmp",
                fileNameBase + ".sgi"), mapDir, 5000)) {
            return new File(mapDir, fileNameBase + ".sgi").renameTo(new File(mapDir, fileNameBase));
        }
        return false;
    }

    private boolean runAndWaitExternalCommand(String command, File workingDir, final long timeout) {
        try {
            // Execute the command
            Process p = Runtime.getRuntime().exec(command, null, workingDir);

            // Wait for the Command to finish
            p.waitFor();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    private void saveTerrainFile(File mapDir, int unitSize) throws SOMToolboxException, IOException {
        saveVisualisaton(new File(mapDir, FILENAME_TERRAIN + ".bmp"), unitSize, TERRAIN_PALETTE);
    }

    private void saveTCL(File mapDir, String mapName, int xSize, int ySize) throws FileNotFoundException {
        // TODO: Check if this is correct so!
        PrintStream ps = new PrintStream(new File(mapDir, FILENAME_TCL));

        ps.println("tux_elev_scale 30.0");
        ps.printf("tux_course_dim %d %d %d %d%n", xSize, ySize, xSize, ySize);
        ps.printf("tux_course_name \"%s\"%n", mapName);
        ps.printf("tux_course_author \"%s\"%n", MAP_AUTHOR);
        ps.printf("tux_elev %s%n", FILENAME_ELEVATION);
        ps.printf("tux_terrain %s%n", FILENAME_TERRAIN);
        ps.printf("tux_start_pt %d %d%n", xSize / 2, ySize / 2);

        ps.printf("%ntux_course_init%n");

        ps.close();
    }

    private void saveSongPositions(File mapDir, int unitSize) throws IOException {
        Unit[] us = csState.growingLayer.getAllUnits();

        int xSize = unitSize * csState.growingLayer.getXSize();
        int ySize = unitSize * csState.growingLayer.getYSize();

        // Create the mapping file
        PrintStream ps = new PrintStream(new File(mapDir, FILENAME_SONGMAPPING));

        // Create and init the note-placement img
        BufferedImage bi = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = bi.createGraphics();
        gr.setColor(Color.BLACK);
        gr.fillRect(0, 0, xSize, ySize);
        gr.dispose();

        for (Unit unit : us) {
            Point[] pos = csState.mapPNode.getStarCoords(unit, unitSize);
            String[] items = unit.getMappedInputNames();
            for (int i = 0; i < pos.length; i++) {
                int x = unitSize * unit.getXPos() + pos[i].x;
                int y = unitSize * unit.getYPos() + pos[i].y;
                ps.printf("%s %d %d %n", items[i], x, y);
                bi.setRGB(x, y, NOTE_MARKER_COLOR.getRGB());
            }
        }

        ps.close();
        ImageIO.write(bi, "bmp", new File(mapDir, FILENAME_TREES + ".bmp"));

    }

    private void saveElevationFile(File mapDir, int unitSize) throws IOException, SOMToolboxException {
        saveVisualisaton(new File(mapDir, FILENAME_ELEVATION + ".bmp"), unitSize, ELEVATION_PALETTE);
    }

    private void saveVisualisaton(File filename, int unitSize, String visName) throws SOMToolboxException, IOException {
        // Prepare
        csState.mapPNode.changePalette(Palettes.getPaletteByName(visName));
        csState.mapPNode.reInitUnitDetails();

        // Save
        BufferedImage elev = ExportUtils.getVisualization(csState, unitSize);
        ImageIO.write(elev, "bmp", filename);
    }

    /**
     * 
     */
    private void closeDialog() {
        TuxRacerExportDialog.this.setVisible(false);
        TuxRacerExportDialog.this.dispose();
    }
}
