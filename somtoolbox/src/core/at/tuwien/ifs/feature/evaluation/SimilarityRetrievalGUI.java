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
package at.tuwien.ifs.feature.evaluation;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.VerticalLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.commons.gui.controls.swing.table.ButtonCellEditor;
import at.tuwien.ifs.commons.gui.controls.swing.table.ButtonCellRenderer;
import at.tuwien.ifs.commons.gui.util.MaximisedJFrame;
import at.tuwien.ifs.commons.models.ClassComboBoxModel;
import at.tuwien.ifs.commons.util.io.ExtensionFileFilterSwing;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.AudioPlayer;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.PlayerControl;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.metadata.MP3VectorMetaData;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.Metrics;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.NumberUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.UiUtils;
import at.tuwien.ifs.somtoolbox.util.comparables.InputDistance;

/**
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class SimilarityRetrievalGUI extends MaximisedJFrame implements SOMToolboxApp {
    private static final long serialVersionUID = 1L;

    public static final Parameter[] OPTIONS = new Parameter[] {};

    public static String DESCRIPTION = "GUI for similarity retrieval";

    public static String LONG_DESCRIPTION = "Provides a graphical interface for similarity retrieval. Allows to load multiple vector files";

    public static final Type APPLICATION_TYPE = Type.Utils;

    private static final String[] resultColumnNames = { "Rank", "Vector Label", "Distance" };

    private static final String[] databaseDetailsColumnNames = { "", "Filename", "Class" };

    private ArrayList<InputData> inputData = new ArrayList<InputData>();

    private SOMLibClassInformation classInfo = null;

    private ButtonGroup bgInputData = new ButtonGroup();

    private ButtonGroup bgDistanceDisplay = new ButtonGroup();

    private JButton btnStart;

    private JButton btnSaveResults;

    private JButton buttonLoadClassInfo;

    private JLabel labelNoInputData = new JLabel("No input data loaded!");

    private SpinnerNumberModel modelNumberNeighbours = new SpinnerNumberModel(0, 0, 0, 1);

    private JSpinner spinnerNumberNeighbours = new JSpinner(modelNumberNeighbours);

    private JPanel panelLoadedFeatureFiles = new JPanel(new VerticalLayout());

    private JPanel panelRetrieval;

    private ChartPanel chartPanel = new ChartPanel(null);

    private JComboBox comboQueryVector = new JComboBox();

    private JComboBox boxMetric = new JComboBox(new ClassComboBoxModel<DistanceMetric>(
            Metrics.getAvailableMetricClasses()));

    private JTable resultsTable = new JTable(new DefaultTableModel(new Object[][] {}, resultColumnNames));

    private JTable databaseDetailsTable;

    private JFileChooser fileChooser = new JFileChooser("/data/music/ISMIRgenre/");

    private JTextField txtFieldMusicPath = new JTextField(50);

    private AudioPlayer player = new AudioPlayer();

    public SimilarityRetrievalGUI() {
        super("Similarity Retrieval GUI");
        setLayout(new GridBagLayout());
        GridBagConstraintsIFS gcMain = new GridBagConstraintsIFS(GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // the panel with the feature files, allows to load new ones
        JPanel panelFeatureFiles = UiUtils.makeBorderedPanel(new VerticalLayout(), "Feature files");
        getContentPane().add(panelFeatureFiles, gcMain);

        panelLoadedFeatureFiles.add(labelNoInputData);

        panelFeatureFiles.add(panelLoadedFeatureFiles);

        JButton btnLoad = initButtonLoad();
        panelFeatureFiles.add(btnLoad);

        txtFieldMusicPath.setToolTipText("Path to the music");

        // TODO: remove
        txtFieldMusicPath.setText("/data/music/ISMIRgenre/mp3_44khz_128kbit_stereo_30sec");

        JButton btnBrowseMusicPath = UiUtils.createBrowseButton(txtFieldMusicPath, this, true);

        JPanel panelMusicPath = new JPanel();
        panelMusicPath.add(new JLabel("Music path"));
        panelMusicPath.add(txtFieldMusicPath);
        panelMusicPath.add(btnBrowseMusicPath);

        panelFeatureFiles.add(panelMusicPath);

        initButtonStart();

        initButtonSaveResults();

        JRadioButton rbDistanceAbsolute = UiUtils.makeRadioButton("absolute", bgDistanceDisplay, true);
        bgDistanceDisplay.add(rbDistanceAbsolute);
        JRadioButton rbDistanceRelative = UiUtils.makeRadioButton("relative", bgDistanceDisplay);
        bgDistanceDisplay.add(rbDistanceRelative);

        initPanelRetrieval();

        ((JSpinner.DefaultEditor) spinnerNumberNeighbours.getEditor()).getTextField().setColumns(6);

        panelRetrieval.setBorder(new TitledBorder("Options"));
        GridBagConstraintsIFS gc = new GridBagConstraintsIFS().setInsets(5, 2);
        panelRetrieval.add(new JLabel("# to retrieve"), gc);
        panelRetrieval.add(spinnerNumberNeighbours, gc.nextCol());
        panelRetrieval.add(new JLabel("Query vector"), gc.nextRow());
        panelRetrieval.add(comboQueryVector, gc.nextCol());

        panelRetrieval.add(new JLabel("Distances"), gc.nextRow());
        panelRetrieval.add(UiUtils.makeAndFillPanel(rbDistanceAbsolute, rbDistanceRelative), gc.nextCol());

        boxMetric.setSelectedItem(L2Metric.class.getSimpleName());
        panelRetrieval.add(new JLabel("Distance metric"), gc.nextRow());
        panelRetrieval.add(boxMetric, gc.nextCol());

        gc.nextRow().setGridWidth(2).setAnchor(GridBagConstraints.CENTER);
        panelRetrieval.add(UiUtils.makeAndFillPanel(btnStart, btnSaveResults), gc);
        panelRetrieval.setEnabled(false);

        getContentPane().add(panelRetrieval, gcMain.nextRow());

        resizeResultTableColumns();

        JScrollPane scrollPaneResults = new JScrollPane(resultsTable);
        scrollPaneResults.setBorder(new TitledBorder("Results"));
        getContentPane().add(scrollPaneResults, gcMain.nextRow());

        databaseDetailsTable = new JTable(new DefaultTableModel(new Object[][] {}, databaseDetailsColumnNames));
        databaseDetailsTable.setAutoCreateRowSorter(true);

        databaseDetailsTable.setDefaultEditor(JButton.class, new ButtonCellEditor());

        resizeDatabaseDetailsTableColumns();
        JScrollPane scrollPaneDatabaseDetails = new JScrollPane(databaseDetailsTable);

        // panel in the upper-right corner, holding the database table & buttons to load class assignment
        JPanel databaseDetailsPanel = UiUtils.makeBorderedPanel(new GridBagLayout(), "Database Details");
        GridBagConstraintsIFS gcDatabaseDetails = new GridBagConstraintsIFS(GridBagConstraints.CENTER,
                GridBagConstraints.BOTH);
        gcDatabaseDetails.setWeights(1, 1);
        databaseDetailsPanel.add(scrollPaneDatabaseDetails, gcDatabaseDetails);

        initButtonLoadClassInfo();
        databaseDetailsPanel.add(buttonLoadClassInfo, gc.nextRow());

        JPanel histogramPanel = UiUtils.makeBorderedPanel("Histogram of Distances");
        histogramPanel.add(chartPanel);

        JPanel detailsPanel = new JPanel(new VerticalLayout());
        gcMain.setGridHeight(3);
        gcMain.setWeights(1, 1);
        getContentPane().add(detailsPanel, gcMain.moveTo(1, 0));
        detailsPanel.add(databaseDetailsPanel);
        detailsPanel.add(histogramPanel);

    }

    private void initPanelRetrieval() {
        panelRetrieval = new JPanel(new GridBagLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void setEnabled(boolean enabled) {
                // also enable all subcomponents
                super.setEnabled(enabled);
                for (Component c : panelRetrieval.getComponents()) {
                    c.setEnabled(enabled);
                }
            }

        };
    }

    private void initButtonSaveResults() {
        btnSaveResults = new JButton("Save as ...");
        btnSaveResults.setEnabled(false); // can't save right away, need a first search
        btnSaveResults.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                    fileChooser.setSelectedFile(null);
                }

                int returnVal = fileChooser.showDialog(SimilarityRetrievalGUI.this, "Save results to file ...");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        PrintWriter w = FileUtils.openFileForWriting("Results file",
                                fileChooser.getSelectedFile().getAbsolutePath());
                        TableModel model = resultsTable.getModel();

                        // column headers
                        for (int col = 0; col < model.getColumnCount(); col++) {
                            w.print(model.getColumnName(col) + "\t");
                        }
                        w.println();

                        // write each data row
                        for (int row = 0; row < model.getRowCount(); row++) {
                            for (int col = 0; col < model.getColumnCount(); col++) {
                                w.print(model.getValueAt(row, col) + "\t");
                            }
                            w.println();
                        }
                        w.flush();
                        w.close();
                        JOptionPane.showMessageDialog(SimilarityRetrievalGUI.this, "Successfully wrote to file "
                                + fileChooser.getSelectedFile().getAbsolutePath(), "Results stored",
                                JOptionPane.INFORMATION_MESSAGE);

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private void initButtonStart() {
        btnStart = new JButton("Start");
        btnStart.setEnabled(false);
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Enumeration<AbstractButton> elements = bgInputData.getElements();
                while (elements.hasMoreElements()) {
                    InputDataRadioButton rb = (InputDataRadioButton) elements.nextElement();
                    if (rb.isSelected()) {
                        AbstractSOMLibSparseInputData inputData = rb.inputData;
                        try {
                            @SuppressWarnings("unchecked")
                            Class<? extends DistanceMetric> selectedClass = ((ClassComboBoxModel<DistanceMetric>) boxMetric.getModel()).getSelectedClass();
                            int inputDatumIndex = inputData.getInputDatumIndex((String) comboQueryVector.getSelectedItem());
                            DistanceMetric metric = AbstractMetric.instantiateNice(selectedClass.getName());
                            ArrayList<InputDistance> distances = inputData.getDistances(inputDatumIndex, metric);
                            Collections.sort(distances);

                            // prepare the data for the table
                            double maxDistance = distances.get(distances.size() - 1).getDistance();

                            int neighbours = modelNumberNeighbours.getNumber().intValue();
                            String actionCommand = bgDistanceDisplay.getSelection().getActionCommand();
                            boolean isAbsolute = !actionCommand.equals("relative");

                            Object[][] data = new Object[neighbours][3];
                            if (isAbsolute) {
                                for (int i = 0; i < data.length; i++) {
                                    data[i] = new Object[] { i + 1, distances.get(i).getInput().getLabel(),
                                            NumberUtils.setScale(4, distances.get(i).getDistance()) };
                                }
                            } else {
                                for (int i = 0; i < data.length; i++) {
                                    data[i] = new Object[] { i + 1, distances.get(i).getInput().getLabel(),
                                            StringUtils.formatAsPercent(distances.get(i).getDistance(), maxDistance, 3) };
                                }
                            }
                            resultsTable.setModel(new DefaultTableModel(data, resultColumnNames));
                            resizeResultTableColumns();

                            // prepare the data for the chart
                            double[] values = InputDistance.getDistanceValuesOnly(distances);
                            if (!isAbsolute) { // convert values to percent
                                for (int i = 0; i < values.length; i++) {
                                    values[i] = values[i] * 100.0 / maxDistance;
                                }
                            }

                            HistogramDataset ds = new HistogramDataset();
                            ds.addSeries("Distance", values, 100, 0, isAbsolute ? maxDistance : 100);
                            JFreeChart chart = ChartFactory.createHistogram(null, isAbsolute ? "absolute distance"
                                    : "distance in percent", "# of objects", ds, PlotOrientation.VERTICAL, false, true,
                                    true);
                            chartPanel.setChart(chart);

                        } catch (SOMToolboxException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                }
                // enable the saveAs button
                btnSaveResults.setEnabled(true);
            }

        });
    }

    public JButton initButtonLoad() {
        JButton buttonLoad = new JButton("Load");
        buttonLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new ExtensionFileFilterSwing(new String[] { "arff", "vec", "rp", "rh", "ssd",
                        "tfxidf" }));
                if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                    fileChooser.setSelectedFile(null);
                }

                // TODO: remove
                fileChooser.setSelectedFile(new File("/data/music/ISMIRgenre/vec/mp3_vec_conv_from_wav/ISMIRgenre.rp"));

                int returnVal = fileChooser.showDialog(SimilarityRetrievalGUI.this, "Open input data");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // FIXME: remove dependency on AbstractSOMLibSparseInputData
                    AbstractSOMLibSparseInputData data = (AbstractSOMLibSparseInputData) InputDataFactory.open(fileChooser.getSelectedFile().getAbsolutePath());

                    String[] newLabels = data.getLabels();
                    Arrays.sort(newLabels);

                    if (inputData.size() > 0) {
                        // check if the input data files match; if not, don't add this new one
                        String[] existingLabels = inputData.get(0).getLabels();
                        Arrays.sort(existingLabels);
                        if (!ArrayUtils.isEquals(existingLabels, newLabels)) {
                            JOptionPane.showMessageDialog(SimilarityRetrievalGUI.this,
                                    "New data loaded doesn't have the same labels as the existing ones. Aborting",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            System.out.println(Arrays.toString(existingLabels));
                            System.out.println(Arrays.toString(newLabels));
                            return;
                        }
                    }

                    inputData.add(data);
                    final InputDataRadioButton rb = new InputDataRadioButton(data);
                    bgInputData.add(rb);
                    rb.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String actionCommand = rb.getActionCommand();
                            System.out.println(actionCommand);
                        }
                    });
                    if (inputData.size() == 1) { // first time loaded
                        rb.setSelected(true);
                        // enable the retrieval panel
                        panelRetrieval.setEnabled(true);
                        panelLoadedFeatureFiles.remove(labelNoInputData);

                        // populate the combo box for the retrieval
                        comboQueryVector.setModel(new DefaultComboBoxModel(data.getLabels()));
                        modelNumberNeighbours.setMaximum(data.numVectors() - 1);
                        modelNumberNeighbours.setValue(Math.min(5, data.numVectors()));
                        spinnerNumberNeighbours.setToolTipText("Maximum value:" + (data.numVectors() - 1));
                        btnStart.setEnabled(true);

                        // fill the library tab
                        Object[][] libraryData = new Object[data.numVectors()][];
                        ImageIcon icon = UiUtils.getIcon(PlayerControl.ICON_PREFIX, "play" + PlayerControl.ICON_SUFFIX);
                        for (int i = 0; i < libraryData.length; i++) {
                            final JButton button = new JButton(icon);
                            final int index = i;
                            button.setActionCommand(String.valueOf(i));
                            button.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    String label = inputData.get(0).getLabel(index);
                                    try {
                                        player.stop();
                                        player.play(new MP3VectorMetaData(new File(txtFieldMusicPath.getText()
                                                + File.separator + label), label));
                                    } catch (FileNotFoundException e1) {
                                        JOptionPane.showMessageDialog(SimilarityRetrievalGUI.this,
                                                "Error playing audio file: " + e1.getMessage(), "Playback Error",
                                                JOptionPane.ERROR_MESSAGE);
                                        e1.printStackTrace();
                                    }
                                }
                            });
                            libraryData[i] = new Object[] { button, data.getLabel(i), "unknown" };
                        }
                        databaseDetailsTable.setModel(new DefaultTableModel(libraryData, databaseDetailsColumnNames));
                        databaseDetailsTable.getColumn(databaseDetailsColumnNames[0]).setCellEditor(
                                new ButtonCellEditor());

                        databaseDetailsTable.getColumn("").setCellRenderer(new ButtonCellRenderer());

                        resizeDatabaseDetailsTableColumns();

                        buttonLoadClassInfo.setEnabled(true);
                    }
                    panelLoadedFeatureFiles.add(rb);
                    pack();
                }
            }
        });
        return buttonLoad;
    }

    private JButton initButtonLoadClassInfo() {
        buttonLoadClassInfo = new JButton("Load class info file");
        buttonLoadClassInfo.setEnabled(false);
        buttonLoadClassInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new ExtensionFileFilterSwing(new String[] { "cls", "txt" }));
                if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                    fileChooser.setSelectedFile(null);
                }

                // TODO: remove
                fileChooser.setSelectedFile(new File("/data/music/ISMIRgenre/filelist_ISMIRgenre_mp3_wclasses.txt"));

                int returnVal = fileChooser.showDialog(SimilarityRetrievalGUI.this, "Open class information file");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        SOMLibClassInformation clsInfo = new SOMLibClassInformation(
                                fileChooser.getSelectedFile().getAbsolutePath());
                        // Sanity check if all the inputs are in the class info file
                        String[] labels = inputData.get(0).getLabels();
                        ArrayList<String> missingLabels = new ArrayList<String>();
                        for (String label : labels) {
                            if (!clsInfo.hasClassAssignmentForName(label)) {
                                missingLabels.add(label);
                            }
                        }
                        int answer = JOptionPane.YES_OPTION;
                        if (missingLabels.size() > 0) {
                            System.out.println(missingLabels);
                            String missing = StringUtils.toString(missingLabels.toArray(), 5);
                            answer = JOptionPane.showConfirmDialog(SimilarityRetrievalGUI.this,
                                    "Class information file '" + fileChooser.getSelectedFile().getAbsolutePath()
                                            + "' is missing the class assignment for " + missingLabels.size()
                                            + " vectors\n    " + missing + "\nContinue loading?",
                                    "Missing class assignment", JOptionPane.YES_NO_OPTION);
                        }
                        if (missingLabels.size() == 0 || answer == JOptionPane.YES_OPTION) {
                            classInfo = clsInfo;
                            TableModel model = databaseDetailsTable.getModel();
                            for (int i = 0; i < labels.length; i++) {
                                model.setValueAt(clsInfo.getClassName(labels[i]), i, 2);
                            }
                            resizeDatabaseDetailsTableColumns();
                        }
                    } catch (SOMToolboxException e1) {
                        JOptionPane.showMessageDialog(SimilarityRetrievalGUI.this,
                                "Error loading class information file: " + e1.getMessage() + ". Aborting", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        e1.printStackTrace();
                    }
                }
            }
        });
        return buttonLoadClassInfo;
    }

    private void resizeResultTableColumns() {
        UiUtils.packColumns(resultsTable, 2);
    }

    private void resizeDatabaseDetailsTableColumns() {
        UiUtils.packColumns(databaseDetailsTable, 2);
    }

    public static void main(String[] args) {
        SimilarityRetrievalGUI gui = new SimilarityRetrievalGUI();
        gui.pack();
        gui.setVisible(true);
    }

    private class InputDataRadioButton extends JRadioButton {
        private static final long serialVersionUID = 1L;

        private AbstractSOMLibSparseInputData inputData;

        InputDataRadioButton(AbstractSOMLibSparseInputData data) {
            super(data.getDataSource());
            this.inputData = data;
        }
    }

}
