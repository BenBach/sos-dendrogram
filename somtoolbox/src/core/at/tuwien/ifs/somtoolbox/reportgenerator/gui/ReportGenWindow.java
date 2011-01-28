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
package at.tuwien.ifs.somtoolbox.reportgenerator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.lang.ArrayUtils;
import org.jfree.ui.tabbedui.VerticalLayout;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMInputReader;
import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.EditableReportProperties;
import at.tuwien.ifs.somtoolbox.reportgenerator.ReportGenerator;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResultCollection;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.UiUtils;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @author Rudolf Mayer
 * @version $Id: ReportGenWindow.java 3585 2010-05-21 10:33:21Z mayer $
 */

public class ReportGenWindow {
    private static final String PROPERTIES_FILE = "Properties File";

    private static final String OUTPUT_FILE = "Output File";

    public static final String[] unitAndMmapMeasures = new String[] { "Quantization Error", "Mean Quantization Error",
            "Intrinsic Distance", "Topographic Error", "Entropy Error", "Silhouette Value", "Distortion Values" };

    public static final String[] mapMeasures = new String[] { "Spearman Coefficient", "Sammon Measure",
            "Metric Multiscaling", "Inversion Measure" };

    private ReportGenerator reportGenerator = null;

    private Vector<InputVectorSelectorPane> inputElements = null;

    private Vector<RunResultSpecifierPane> testrunResults = null;

    private HashMap<String, String> paths = new HashMap<String, String>();

    private CommonSOMViewerStateData state = null;

    /* GUI THINGS */
    private JTextField textInputVectorsLocation = null;

    private JTextField Map_location = null;

    private JTextField textTemplateVectorsLocation = null;

    private JTextField textOutputLocation = null;

    private JTextField textClassFileLocation = null;

    private JTextField textPropertiesLocation = null;

    private JCheckBox checkBoxClusterReport;

    private JCheckBox checkBoxRegionalReport;

    private JCheckBox checkBoxScientificDescription;

    private JCheckBox checkBoxSemanticInterpretation;

    private JCheckBox checkBoxDistortion;

    private JCheckBox checkBoxFlowBorderline;

    private JCheckBox checkBoxMetroMap;

    private JCheckBox checkBoxTrustworthiness;

    private JCheckBox checkBoxTopographicProduct;

    private JCheckBox checkBoxSDH;

    private JSpinner jSpinnerSDH;

    private JSpinner jSpinnerTopographicProduct;

    private JSpinner jSpinnerTrustworthiness;

    private JSpinner jSpinnerMetroMap;

    private JSpinner jSpinnerClassCompactnessMAX;

    private JSpinner jSpinnerClassCompactnessMIN;

    private JCheckBox[] mapMeasureBoxes;

    private JCheckBox[] unitAndMapMeasuresBoxes;

    private JCheckBox[] allMeasuresBoxes;

    private JScrollPane inputElementSelectionScroller = null;

    private JScrollPane testrunScroller = null;

    private JPanel inputElementSelectionScrollerContent = null;

    private JPanel testrunScrollerContent = null;

    private JFileChooser fileChooser = null;

    private JFileChooser dirChooser;

    private EditableReportProperties EP = null;

    public ReportGenWindow(boolean standalone, ReportGenerator reportGen, CommonSOMViewerStateData state,
            String outputPath, String propertiesFile) {
        // fill in the input boxes from the state
        for (SOMVisualisationData data : state.inputDataObjects.getObjects()) {
            paths.put(data.getType(), data.getFileName());
        }
        paths.put(SOMInputReader.UNIT_FILE, state.somInputReader.getUnitDescriptionFileName());
        paths.put(SOMInputReader.WEIGHT_VECTOR, state.somInputReader.getWeightVectorFileName());
        paths.put(SOMInputReader.MAP_FILE, state.somInputReader.getMapDescriptionFileName());
        paths.put(OUTPUT_FILE, outputPath);
        paths.put(PROPERTIES_FILE, propertiesFile);

        this.state = state;
        this.init(standalone, reportGen);
    }

    private void init(boolean standalone, ReportGenerator reportGen) {
        this.reportGenerator = reportGen;
        this.inputElements = new Vector<InputVectorSelectorPane>();
        this.testrunResults = new Vector<RunResultSpecifierPane>();

        JFrame jFrame = new JFrame("SOMToolbox Report Generator");
        jFrame.setSize(new Dimension(700, 600));
        jFrame.setContentPane(getJContentPane());
        UiUtils.centerWindow(jFrame);
        jFrame.pack();
        jFrame.setVisible(true);

        JLabel l;
        for (int i = 0; i < 100; i++) {
            l = new JLabel();
            l.setText("test" + i);
            this.inputElementSelectionScroller.add(l);
        }

        if (standalone) {
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

    public String getMapPath() {
        return paths.get(SOMInputReader.MAP_FILE);
    }

    public String getUnitPath() {
        return paths.get(SOMInputReader.UNIT_FILE);
    }

    public String getWeightPath() {
        return paths.get(SOMInputReader.WEIGHT_VECTOR);
    }

    public String getDataWinnerMappingPath() {
        return paths.get(SOMVisualisationData.DATA_WINNER_MAPPING);
    }

    public String getInputVectorPath() {
        return paths.get(SOMVisualisationData.INPUT_VECTOR);
    }

    public String getClassInfoPath() {
        return paths.get(SOMVisualisationData.CLASS_INFO);
    }

    public String getTemplatePath() {
        return paths.get(SOMVisualisationData.TEMPLATE_VECTOR);
    }

    public String getPropertiesPath() {
        return paths.get(PROPERTIES_FILE);
    }

    public String getOutputPath() {
        return paths.get(OUTPUT_FILE);
    }

    /**
     * loads the specified Input Vector file into some data structure (hopefully one that already exists) and prints a
     * list of the vectors in the selection list for marking inputs that shall be observed
     * 
     * @return true if loading was successful, false otherwise
     */
    public boolean loadInputVectors() {
        String inputVectorFilename = textInputVectorsLocation.getText();
        String templateVectorFilename = textTemplateVectorsLocation.getText();

        boolean denseData = true;

        // check whether the files really exist
        if (!new File(inputVectorFilename).exists()) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning("File does not exist: " + inputVectorFilename);
            return false;
        }
        if (!new File(templateVectorFilename).exists()) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "File does not exist: " + templateVectorFilename);
            return false;
        }

        // das inputfile laden
        InputData data = new SOMLibSparseInputData(inputVectorFilename, templateVectorFilename, !denseData, true, 1, 7);
        InputDatum curVector = null;
        InputVectorSelectorPane newPane = null;

        // und die Auswahlliste erstellen
        this.clearInputElementSelectionPane();
        for (int i = 0; i < data.numVectors(); i++) {
            curVector = data.getInputDatum(i);
            newPane = new InputVectorSelectorPane(i, "" + curVector.getLabel(), curVector);
            this.inputElementSelectionScrollerContent.add(newPane);
            this.inputElements.add(newPane);
            // System.out.println(curVector.getLabel()+ ": "+curVector.toString());
        }
        this.inputElementSelectionScrollerContent.updateUI();
        return true;
    }

    public void generateReport(int type) {
        // the user requested to create a report
        // Eine Liste mit den ids der ausgewählten Input-Vectoren erstellen
        Vector<Integer> idList = new Vector<Integer>();
        for (int i = 0; i < this.inputElements.size(); i++) {
            if (this.inputElements.get(i).isSelected()) {
                idList.add(new Integer(this.inputElements.get(i).getId()));
            }
        }
        // Reading customized Propeties
        this.EP = this.readEditableProperties();

        // check if we have the input data loaded already, and then use the alternative constructor of
        // DatasetInformation
        DatasetInformation dataInfo = null;
        if (state.inputDataObjects.getInputData() != null) {
            dataInfo = new DatasetInformation(idList, textInputVectorsLocation.getText(),
                    textTemplateVectorsLocation.getText(), textClassFileLocation.getText(), EP, state);
        } else {
            dataInfo = new DatasetInformation(idList, textInputVectorsLocation.getText(),
                    textTemplateVectorsLocation.getText(), textClassFileLocation.getText(), EP);
        }
        this.reportGenerator.createReport(type, textOutputLocation.getText(), dataInfo, getTestrunResultCollection(
                dataInfo, type));
    }

    /**
     * removes the specified testrun the testrun identified with this id (that is the one at position id (starting with
     * 1st position == 0)) is removed from the list of testruns as well as from the panel.
     * 
     * @param id the index of the testrun to remove
     */
    public void removeTestrun(int id) {
        this.testrunScrollerContent.remove(this.testrunResults.get(id));
        this.testrunResults.remove(id);
        this.testrunScrollerContent.updateUI();
        for (int i = id; i < this.testrunResults.size(); i++) {
            this.testrunResults.get(i).updateId(i);
        }
    }

    /** Makes a report on the default Map. */
    private TestRunResultCollection getTestrunResultCollection(DatasetInformation dataInfo, int type) {
        TestRunResultCollection collection = new TestRunResultCollection();
        TestRunResult default_testrun;
        if (this.state == null) {
            // Update: The first Element in the TestRunResult collection is the default testrun itself.
            default_testrun = new TestRunResult(dataInfo, Map_location.getText(), textPropertiesLocation.getText(),
                    getUnitPath(), getWeightPath(), getDataWinnerMappingPath(), 0, type);
        } else {
            // initialize with already trained SOM
            default_testrun = new TestRunResult(dataInfo, Map_location.getText(), textPropertiesLocation.getText(),
                    getUnitPath(), getWeightPath(), getDataWinnerMappingPath(), 0, type, state);
        }
        collection.addTestrunResult(default_testrun);
        for (int i = 0; i < this.testrunResults.size(); i++) {

            collection.addTestrunResult(new TestRunResult(dataInfo, this.testrunResults.get(i).getMapFilePath(),
                    this.testrunResults.get(i).getPropertyFilePath(), this.testrunResults.get(i).getUnitFilePath(),
                    this.testrunResults.get(i).getWeightFilePath(), this.testrunResults.get(i).getDwFilePath(), i + 1,
                    type));
        }
        collection.setObjectsToCorrectType();
        return collection;
    }

    /** inserts a new panel for specifying information about a testrun to the testrunScrollerContent pane */
    private void addTestrunPane() {
        RunResultSpecifierPane newRun = new RunResultSpecifierPane(this.testrunResults.size(), this);
        this.testrunScrollerContent.add(newRun);
        this.testrunResults.add(newRun);
        this.testrunScrollerContent.updateUI();
    }

    /**
     * removes all the panes for marking input elements as interesting
     */
    private void clearInputElementSelectionPane() {
        this.inputElementSelectionScrollerContent.removeAll();
        this.inputElements.removeAllElements();
    }

    /** Create the main panel */
    private JPanel getJContentPane() {
        JPanel panelContent = new JPanel();
        panelContent.setLayout(new GridBagLayout());
        panelContent.setName("Report Generator");

        GridBagConstraintsIFS gc = new GridBagConstraintsIFS().fillWidth().setInsets(new Insets(5, 5, 0, 5));

        textInputVectorsLocation = new JTextField(getInputVectorPath(), 50);
        JButton browseInputFileButton = new JButton("Browse");
        browseInputFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // show the dialog to select the input file
                if (getFileChooser(textInputVectorsLocation.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    textInputVectorsLocation.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });

        panelContent.add(new JLabel("Input vector file (vec):"), gc);
        panelContent.add(textInputVectorsLocation, gc.nextCol());
        panelContent.add(browseInputFileButton, gc.nextCol());

        textTemplateVectorsLocation = new JTextField(getTemplatePath(), 50);
        JButton browseTemplateFileButton = new JButton("Browse");
        browseTemplateFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // show the dialog to select the template file
                if (getFileChooser(textTemplateVectorsLocation.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    textTemplateVectorsLocation.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });

        panelContent.add(new JLabel("Template vector file (.tv):"), gc.nextRow());
        panelContent.add(textTemplateVectorsLocation, gc.nextCol());
        panelContent.add(browseTemplateFileButton, gc.nextCol());

        textClassFileLocation = new JTextField(getClassInfoPath(), 50);
        JButton browseClassFileButton = new JButton("Browse");
        browseClassFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // show the dialog to select the file with the class information
                if (getFileChooser(textClassFileLocation.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    textClassFileLocation.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        panelContent.add(new JLabel("Class information file (.cls)"), gc.nextRow());
        panelContent.add(textClassFileLocation, gc.nextCol());
        panelContent.add(browseClassFileButton, gc.nextCol());

        Map_location = new JTextField(getMapPath(), 50);
        JButton buttonBrowseMapFile = new JButton("Browse");
        buttonBrowseMapFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // show the dialog to select the Map file
                if (getDirectoryChooser(Map_location.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    Map_location.setText(dirChooser.getSelectedFile().getPath());
                }
            }
        });
        panelContent.add(new JLabel("Map File (*.map.[gz]):"), gc.nextRow());
        panelContent.add(Map_location, gc.nextCol());
        panelContent.add(buttonBrowseMapFile, gc.nextCol());

        JButton browsePropertiesButton = new JButton("Browse");
        browsePropertiesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // show the dialog to select the property file
                if (getFileChooser(textPropertiesLocation.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    textPropertiesLocation.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });

        textPropertiesLocation = new JTextField(getPropertiesPath(), 50);
        panelContent.add(new JLabel("Property File(*.prop):"), gc.nextRow());
        panelContent.add(textPropertiesLocation, gc.nextCol());
        panelContent.add(browsePropertiesButton, gc.nextCol());

        JButton buttonLoadInputVector = new JButton("Load");
        buttonLoadInputVector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadInputVectors();
            }
        });

        panelContent.add(buttonLoadInputVector, gc.nextRow().setGridWidth(3).clone().setAnchor(GridBagConstraints.EAST));

        textOutputLocation = new JTextField(getOutputPath(), 50);
        JButton browseOutputFileButton = new JButton("Browse");
        browseOutputFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // show the dialog to select the output file
                if (getDirectoryChooser(textOutputLocation.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    textOutputLocation.setText(dirChooser.getSelectedFile().getPath());
                }
            }
        });
        panelContent.add(new JLabel("Output file:"), gc.nextRow());
        panelContent.add(textOutputLocation, gc.nextCol());
        panelContent.add(browseOutputFileButton, gc.nextCol());

        GridBagConstraintsIFS gc2 = new GridBagConstraintsIFS(GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL).setGridWidth(2);
        JPanel panelInputVectorSelection = new JPanel(new GridBagLayout());
        panelInputVectorSelection.setBorder(BorderFactory.createTitledBorder("Input vectors"));

        JButton buttonUnselectAllInputs = new JButton("unselect all");
        buttonUnselectAllInputs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < inputElements.size(); i++) {
                    inputElements.get(i).unselect();
                }
            }
        });

        JButton buttonSelectAllInputs = new JButton("select all");
        buttonSelectAllInputs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < inputElements.size(); i++) {
                    inputElements.get(i).select();
                }
            }
        });

        inputElementSelectionScrollerContent = new JPanel();
        inputElementSelectionScrollerContent.setLayout(new BoxLayout(inputElementSelectionScrollerContent,
                BoxLayout.Y_AXIS));
        inputElementSelectionScroller = new JScrollPane(inputElementSelectionScrollerContent);
        inputElementSelectionScroller.setPreferredSize(new Dimension(120, 100));

        panelInputVectorSelection.add(inputElementSelectionScroller, gc2);
        panelInputVectorSelection.add(buttonSelectAllInputs, gc2.nextRow().setGridWidth(1));
        panelInputVectorSelection.add(buttonUnselectAllInputs, gc2.nextCol());

        JPanel panelTestruns = new JPanel(new VerticalLayout());
        panelTestruns.setBorder(BorderFactory.createTitledBorder("Additional Testruns"));
        testrunScrollerContent = new JPanel();
        testrunScrollerContent.setLayout(new BoxLayout(testrunScrollerContent, BoxLayout.Y_AXIS));
        testrunScroller = new JScrollPane(testrunScrollerContent);
        testrunScroller.setPreferredSize(new Dimension(450, 100));

        JButton addTestrunButton = new JButton("add new testrun");
        addTestrunButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTestrunPane();
            }
        });

        panelTestruns.add(testrunScroller);
        panelTestruns.add(addTestrunButton);

        JPanel panelInputAndTestruns = new JPanel(new GridBagLayout());
        gc2 = new GridBagConstraintsIFS(GridBagConstraints.NORTH, GridBagConstraints.BOTH).setInsets(5, 5);
        panelInputAndTestruns.add(panelInputVectorSelection, gc2);
        panelInputAndTestruns.add(panelTestruns, gc2.nextCol());

        panelContent.add(panelInputAndTestruns, gc.nextRow().setGridWidth(3).setAnchor(GridBagConstraints.NORTH));
        panelContent.add(getEditableReportAtributesPane(), gc.nextRow().setGridWidth(3));

        // export buttons
        JButton generateHTMLReportButton = new JButton("Generate HTML-Report");
        generateHTMLReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateReport(ReportGenerator.HTML_REPORT);
            }
        });
        JButton genLatexReportButton = new JButton("Generate LaTEx-Report");
        genLatexReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateReport(ReportGenerator.LATEX_REPORT);
            }
        });

        JPanel panelGenerateButtons = new JPanel();
        panelGenerateButtons.add(generateHTMLReportButton);
        panelGenerateButtons.add(genLatexReportButton);
        panelContent.add(panelGenerateButtons, gc.nextRow());

        return panelContent;
    }

    /** Return the panel holding the controls to change the report options */
    private JPanel getEditableReportAtributesPane() {
        // Visualizations & Quality Measures panel
        JPanel panelQualityMeasures = new JPanel(new GridBagLayout());
        panelQualityMeasures.setBorder(BorderFactory.createTitledBorder("Visualizations & Quality Measures"));
        GridBagConstraintsIFS gc = new GridBagConstraintsIFS().setInsets(10, 5);

        mapMeasureBoxes = createCheckBoxes(mapMeasures);
        unitAndMapMeasuresBoxes = createCheckBoxes(unitAndMmapMeasures);
        allMeasuresBoxes = (JCheckBox[]) ArrayUtils.addAll(mapMeasureBoxes.clone(), unitAndMapMeasuresBoxes);

        checkBoxSemanticInterpretation = new JCheckBox("Semantic Interpretation");
        checkBoxScientificDescription = new JCheckBox("Method Description");

        JButton buttonSelectAllQM = new JButton("Select All");
        buttonSelectAllQM.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectQualityBoxes(true);
            }
        });
        JButton buttonSelectNoneQM = new JButton("Select None");
        buttonSelectNoneQM.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectQualityBoxes(false);
            }
        });

        panelQualityMeasures.add(new JLabel("Unit & Map Quality Measures"), gc);
        panelQualityMeasures.add(new JLabel("Map Quality Measures"), gc.nextCol());

        for (int i = 0; i < unitAndMapMeasuresBoxes.length; i++) {
            panelQualityMeasures.add(unitAndMapMeasuresBoxes[i], gc.nextRow());
            if (i < mapMeasureBoxes.length) {
                panelQualityMeasures.add(mapMeasureBoxes[i], gc.nextCol());
            }
        }
        panelQualityMeasures.add(buttonSelectAllQM, gc.nextRow());
        panelQualityMeasures.add(buttonSelectNoneQM, gc.nextCol());
        panelQualityMeasures.add(checkBoxSemanticInterpretation, gc.nextRow());
        panelQualityMeasures.add(checkBoxScientificDescription, gc.nextCol());

        // Additional Reports panel
        JPanel panelClassReports = new JPanel(new GridBagLayout());
        panelClassReports.setBorder(BorderFactory.createTitledBorder("Additional Reports"));
        gc = new GridBagConstraintsIFS().setInsets(10, 5);

        checkBoxClusterReport = new JCheckBox("Cluster Report");
        checkBoxRegionalReport = new JCheckBox("Regional Report");
        jSpinnerClassCompactnessMIN = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        jSpinnerClassCompactnessMAX = new JSpinner(new SpinnerNumberModel(100, 0, 100, 1));

        panelClassReports.add(checkBoxClusterReport, gc);

        panelClassReports.add(checkBoxRegionalReport, gc.nextRow());

        panelClassReports.add(new JLabel("Class compactness"), gc.nextRow());
        panelClassReports.add(jSpinnerClassCompactnessMIN, gc.nextCol());
        panelClassReports.add(jSpinnerClassCompactnessMAX, gc.nextCol());

        // Visualizations panel
        JPanel panelVisualizations = new JPanel(new GridBagLayout());
        panelVisualizations.setBorder(BorderFactory.createTitledBorder("Visualizations"));
        gc = new GridBagConstraintsIFS().setInsets(10, 5);

        checkBoxDistortion = new JCheckBox("Distortion", true);
        checkBoxFlowBorderline = new JCheckBox("Flow & BorderLine", true);
        checkBoxMetroMap = new JCheckBox("Metro Map", true);
        checkBoxSDH = new JCheckBox("SDH", true);
        checkBoxTrustworthiness = new JCheckBox("Trustworthiness", true);
        checkBoxTopographicProduct = new JCheckBox("Topographic Product", true);

        jSpinnerTopographicProduct = new JSpinner(new SpinnerNumberModel(1, 1, state.growingLayer.getUnitCount(), 1));
        jSpinnerTrustworthiness = new JSpinner(new SpinnerNumberModel(1, 1, state.growingLayer.getUnitCount(), 1));
        jSpinnerSDH = new JSpinner(new SpinnerNumberModel(1, 1, state.growingLayer.getUnitCount(), 1));
        jSpinnerMetroMap = new JSpinner(new SpinnerNumberModel(1, 1, state.growingLayer.getUnitCount(), 1));

        panelVisualizations.add(checkBoxMetroMap, gc);
        panelVisualizations.add(new JLabel("# components"), gc.nextCol());
        panelVisualizations.add(jSpinnerMetroMap, gc.nextCol());

        panelVisualizations.add(checkBoxSDH, gc.nextRow());
        panelVisualizations.add(new JLabel("Step"), gc.nextCol());
        panelVisualizations.add(jSpinnerSDH, gc.nextCol());

        panelVisualizations.add(checkBoxFlowBorderline, gc.nextRow());

        panelVisualizations.add(checkBoxDistortion, gc.nextRow());

        panelVisualizations.add(checkBoxTrustworthiness, gc.nextRow());
        panelVisualizations.add(new JLabel("Step"), gc.nextCol());
        panelVisualizations.add(jSpinnerTrustworthiness, gc.nextCol());

        panelVisualizations.add(checkBoxTopographicProduct, gc.nextRow());
        panelVisualizations.add(new JLabel("Step"), gc.nextCol());
        panelVisualizations.add(jSpinnerTopographicProduct, gc.nextCol());

        JPanel reportAttributePanel = new JPanel(new GridBagLayout());
        gc = new GridBagConstraintsIFS(GridBagConstraints.WEST, GridBagConstraints.BOTH);
        reportAttributePanel.add(panelQualityMeasures, gc.setGridHeight(2));
        reportAttributePanel.add(panelClassReports, gc.nextCol().setGridHeight(1));
        reportAttributePanel.add(panelVisualizations, gc.moveTo(1, 1));

        return reportAttributePanel;
    }

    private JCheckBox[] createCheckBoxes(String[] m) {
        JCheckBox[] unitAndMapMeasureBoxes = new JCheckBox[m.length];
        for (int i = 0; i < unitAndMapMeasureBoxes.length; i++) {
            unitAndMapMeasureBoxes[i] = new JCheckBox(m[i], true);
        }
        return unitAndMapMeasureBoxes;
    }

    private void selectQualityBoxes(final boolean selected) {
        for (JCheckBox box : allMeasuresBoxes) {
            box.setSelected(selected);
        }
    }

    private JFileChooser getFileChooser(String path) {
        fileChooser = new JFileChooser(path);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        return fileChooser;
    }

    private JFileChooser getDirectoryChooser(String path) {
        dirChooser = new JFileChooser(path);
        dirChooser.setMultiSelectionEnabled(false);
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return dirChooser;
    }

    /** Reads the options */
    private EditableReportProperties readEditableProperties() {
        EditableReportProperties properties = new EditableReportProperties();

        /* PANEL Quality Measures */
        properties.setSelectedQualitMeasure(this.getSelectedQualityMeasures());
        properties.setIncludeSemanticReport(this.checkBoxSemanticInterpretation.isSelected());

        /* PANEL Visualizations */
        properties.setMetroMapComponents(((SpinnerNumberModel) jSpinnerMetroMap.getModel()).getNumber().intValue());
        properties.setIncludeDistortion(checkBoxDistortion.isSelected());
        properties.setIncludeFlowBorderLine(checkBoxFlowBorderline.isSelected());
        properties.setTrustWorthinessStep(((SpinnerNumberModel) jSpinnerTrustworthiness.getModel()).getNumber().intValue());
        properties.setTopographicProductStep(((SpinnerNumberModel) jSpinnerTopographicProduct.getModel()).getNumber().intValue());
        properties.setSdhStep(((SpinnerNumberModel) jSpinnerSDH.getModel()).getNumber().intValue());
        properties.setIncludeTrustWorthiness(checkBoxTrustworthiness.isSelected());
        properties.setIncludeTopographicProduct(checkBoxTopographicProduct.isSelected());
        properties.setIncludeSDH(checkBoxSDH.isSelected());

        /**/
        /* CLASS REPORTS PANEL */
        properties.setIncludeClusterReport(checkBoxClusterReport.isSelected());
        properties.setIncludeRegionReport(checkBoxRegionalReport.isSelected());
        properties.setMinCompactness(((SpinnerNumberModel) jSpinnerClassCompactnessMIN.getModel()).getNumber().intValue());
        properties.setMaxCompactness(((SpinnerNumberModel) jSpinnerClassCompactnessMAX.getModel()).getNumber().intValue());
        return properties;
    }

    /** Returns a list containing the selected quality measures */
    public ArrayList<String> getSelectedQualityMeasures() {
        ArrayList<String> result = new ArrayList<String>();
        for (JCheckBox box : allMeasuresBoxes) {
            if (box.isSelected()) {
                result.add(box.getText());
            }
        }
        return result;
    }

}
