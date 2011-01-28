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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import edu.umd.cs.piccolo.PLayer;

import at.tuwien.ifs.commons.gui.util.MaximisedJFrame;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.SOMToolboxMetaConstants;
import at.tuwien.ifs.somtoolbox.apps.DataSetViewer;
import at.tuwien.ifs.somtoolbox.apps.PaletteEditor;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractSelectionPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractViewerControl;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.ClassLegendPane;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.ClusteringControl;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.ComparisonPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.DocSOMPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.GHSOMNavigationPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MapDetailPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MapOverviewPane;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MultichannelPlaybackPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.PalettePanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.PlaySOMPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.PlaygroundPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.QuerySOMPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.ShiftsControlPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.VisualizationControl;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.PlaySOMPlayer;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver.PocketSOMConnector;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.PocketSOMFormatUtils;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.LoggingHandler;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.InputCorrections;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.CreationType;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.InputCorrection;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer.Flip;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer.Rotation;
import at.tuwien.ifs.somtoolbox.layers.Unit.FeatureWeightMode;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;
import at.tuwien.ifs.somtoolbox.properties.SOMViewerProperties;
import at.tuwien.ifs.somtoolbox.reportgenerator.ReportGenerator;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.JMultiLineRadioButtonMenuItem;
import at.tuwien.ifs.somtoolbox.util.SwingWorker;
import at.tuwien.ifs.somtoolbox.util.UiUtils;
import at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.AbstractMatrixVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizerInstance;
import at.tuwien.ifs.somtoolbox.visualization.ComparisonVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import at.tuwien.ifs.somtoolbox.visualization.QualityMeasureVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.SmoothedDataHistograms;
import at.tuwien.ifs.somtoolbox.visualization.ThematicClassMapVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;
import at.tuwien.ifs.somtoolbox.visualization.SmoothedDataHistograms.SDHControlPanel;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusteringAbortedException;
import at.tuwien.ifs.somtoolbox.visualization.clustering.CompleteLinkageTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.KMeansTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.SingleLinkageTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.TreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.WardsLinkageTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.WardsLinkageTreeBuilderAll;

/**
 * The class providing the main window of the SOMViewer application. Initialises all the control element windows (see
 * {@link at.tuwien.ifs.somtoolbox.apps.viewer.controls} package), toolbars, and the {@link SOMFrame} holding the map
 * representation ({@link MapPNode} ).
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @author Thomas Lidy
 * @version $Id: SOMViewer.java 3919 2010-11-05 11:58:02Z mayer $
 */
public class SOMViewer extends MaximisedJFrame implements ActionListener, Observer, SOMToolboxApp {

    public static final String DESCRIPTION = "An interactive viewer for exploring SOMs, using different visualisations";

    public static final Type APPLICATION_TYPE = Type.Viewer;

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Parameter[] OPTIONS = new Parameter[] {
            // mandatory
            OptionFactory.getOptUnitDescriptionFile(true),
            OptionFactory.getOptWeightVectorFile(true),
            // additional input files
            OptionFactory.getOptDataWinnerMappingFile(false), OptionFactory.getOptMapDescriptionFile(false),
            OptionFactory.getOptTemplateVectorFile(false), OptionFactory.getOptInputVectorFile(false),
            OptionFactory.getOptClassInformationFile(false), OptionFactory.getOptRegressionInformationFile(false),
            OptionFactory.getOptDataInformationFileFile(false),
            OptionFactory.getOptHighlightedDataNamesFile(false),
            OptionFactory.getOptLinkageFile(false),
            OptionFactory.getOptInputCorrections(false),
            OptionFactory.getOptClassColoursFile(false),
            //
            OptionFactory.getOptFileNamePrefix(false), OptionFactory.getOptFileNameSuffix(false),
            OptionFactory.getOptApplicationDirectory(false),
            OptionFactory.getOptViewerWorkingDir(false),
            // initial vis
            OptionFactory.getOptInitialVisualisation(false), OptionFactory.getOptInitialVisParams(false),
            OptionFactory.getOptInitialPalette(false),
            // second SOM
            OptionFactory.getOptSetSecondSOM(false),
            // Others
            OptionFactory.getSwitchDocumentMode(), OptionFactory.getSwitchNoPlayer(),
            OptionFactory.getOptDecodeProbability(false), OptionFactory.getOptDecodedOutputDir(false) };

    private static final long serialVersionUID = 1L;

    public static final String PREFS_FILE = "somviewer.prop";

    // messages
    private static final String CENTER_AND_FIT_MAP = "Center and fit map to screen";

    private static final String SELECT_LINE = "Select Line Selection Handler";

    private static final String SELECT_RECTANGLE = "Select Rectangle or Unit Selection Handler";

    private static final String SELECT_CLUSTER = "Select Cluster Selection Handler";

    private static final String RESET_DESKTOP_LAYOUT = "Reset desktop windows layout";

    private static final String SOMVIEWER_3D = "Start 3D SOM Viewer";

    private static final String MOVE_INPUT = "Move Input"; // rudi for semi-supervised

    private static final String MOVE_LABEL = "Move Labels"; // Angela

    private static final String CREATE_LABEL = "Create new Label"; // Angela

    static final String TOGGLE_PIE_CHARTS_SHOW = "Pie-charts";

    static final String TOGGLE_PIE_CHARTS_SHOW_COUNTS = "Pie-charts with counts";

    static final String TOGGLE_PIE_CHARTS_SHOW_PERCENT = "Pie-charts with percent";

    static final String TOGGLE_PIE_CHARTS_NONE = "No pie-charts";

    private static final String[] TOGGLE_PIE_CHARTS_MODES = { TOGGLE_PIE_CHARTS_SHOW, TOGGLE_PIE_CHARTS_SHOW_COUNTS,
            TOGGLE_PIE_CHARTS_SHOW_PERCENT, TOGGLE_PIE_CHARTS_NONE };

    // icons are not static, so they don't get initialised when the class is loaded by SOMToolboxMain
    // this would lead to an error on headless environments, e.g. in a 'screen'
    private final ImageIcon TOGGLE_PIE_CHARTS_ICONS[] = { UiUtils.getIcon("piechart.png"),
            UiUtils.getIcon("piechart_label.png"), UiUtils.getIcon("piechart_label_percent.png"),
            UiUtils.getIcon("piechart_off.png") };

    private static final String TOGGLE_LABELS = "Show labels";

    private static final String TOGGLE_HITS = "Show hits";

    private static final String TOGGLE_DATA = "Show data";

    private static final String TOGGLE_EXACT_PLACEMENT = "Exact placement of input vectors";

    private static final String TOGGLE_RELOCATE = "Relocate overlapping input vectors";

    private static final String TOGGLE_LINKAGE = "Display input linkages";

    private static final String MSG_EXACTPLACEMENT_DISABLED = "Data winner mapping file needs to be loaded for this feature!";

    // resources
    public static final String RESOURCE_PATH_ICONS = "rsc/icons/";

    // settings
    // wether to create a fullscreen gui or not
    // private static final boolean fullScreen = true;

    private String unitDescriptionFileName = null;

    private String weightVectorFileName = null;

    // private String highlightedDataNamesFileName = null;

    private String mapDescriptionFileName = null;

    private String classInformationFileName = null;

    private String regressionInformationFileName = null;

    private String dataInformationFileName = null;

    private String inputVectorFileName = null;

    private String templateVectorFileName;

    private String dataWinnerMappingFileName = null;

    private String linkageMapFileName = null;

    private JFrame docViewerFrame = null;

    private boolean documentMode = false;

    private String viewerWorkingDirectoryName = ".";

    private String applicationDirectory = ".";

    private SOMViewerProperties prefs;

    private LoggingHandler loggingHandler = null;

    private BackgroundImageVisualizer initialVisualisation;

    private int initialVisualisationVariant;

    private String classColoursFile = null;

    // menu
    private JMenuBar menuBar = null;

    private JMenu visualizationMenu = null;

    private JMenu paletteMenu = null;

    private ButtonGroup visualizationMenuItemGroup = null;

    private ButtonGroup paletteMenuItemGroup = null;

    private ButtonModel oldSelectedVisualizationMenuItem = null;

    private JMultiLineRadioButtonMenuItem thematicClassRadioButton = null;

    private JCheckBoxMenuItem reversePaletteMenuItem = null;

    private ButtonGroup clusterMethodGroup;

    private int clusteringLevel = 1;

    private ButtonModel previousSelectedClusteringMethod; // previously selected item

    private JMenu windowMenu;

    // ToolBar
    private JToolBar toolBar = null;

    private JPopupMenu menuPie = new JPopupMenu();

    private JButton buttonPie = new JButton();

    private AbstractButton shiftOverlappingToggleButton;

    private AbstractButton exactPlacementToggleButton;

    private AbstractButton linkageToggleButton;

    // StatusBar
    private StatusBar statusBar = null;

    // panes and other stuff
    // private JDesktopPane desktop = new JDesktopPane();

    private ClassLegendPane classLegendPane = null;

    private VisualizationControl visControlPanel = null;

    private ClusteringControl clusteringControl = null;

    private SOMPane mapPane = null;

    private PalettePanel palettePanel = null;

    private ControlCollector collector = null;

    private QuerySOMPanel queryPane = null;

    private CommonSOMViewerStateData state = new CommonSOMViewerStateData(this, 220);

    private SOMFrame somFrame = new SOMFrame(state);

    // SOM Comparison stuff:
    private JCheckBoxMenuItem showShiftsMenuItem = null;

    private ShiftsControlPanel shiftsControlPanel = null;

    private JMenu switchMapSubmenu = null;

    private JMultiLineRadioButtonMenuItem useMainMap = null;

    private JMultiLineRadioButtonMenuItem useSecondMap = null;

    private boolean noInternalPlayer;

    private Vector<VisualizationChangeListener> visChangeListeners = new Vector<VisualizationChangeListener>();

    private JMenuItem paletteEditorMenuItem = null;

    public static final String NO_JAVA3D_ERROR_MESSAGE = "Problem intialising Java3D!"
            + "\nPlease make sure you downloaded it from http://java3d.dev.java.net/ and installed it to the 'lib' directory of your Java Runtime (i.e. $JAVA_HOME/jre/lib)."
            + "\nAborting.";

    /**
     * Starts a new SOM Viewer frame.
     * 
     * @param config Needed program arguments:
     *            <ul>
     *            <li>-u unitDescriptionFileName, mandatory</li>
     *            <li>-w weightVectorFileName, mandatory</li>
     *            <li>-l drawLines, switch</li>
     *            <li>-m mapDescriptionFileName, optional</li>
     *            <li>-c classInformationFileName, optional</li>
     *            <li>-r regressionInformationFileNameInformationFileName, optional</li>
     *            <li>-d dataNamesFilename, optional</li>
     *            <li>-i dataInfoFileName, optional</li>
     *            <li>-v inputVectorFile, optional</li>
     *            <li>-t templateVectorFile, optional</li>
     *            <li>--dw dataWinnerMappingFile, optional</li>
     *            <li>-t templateVectorFile, optional</li>
     *            <li>-p fileNamePrefix, optional</li>
     *            <li>-s fileNameSuffix, optional</li>
     *            <li>--dir viewerWorkingDirectory, optional</li>
     *            <li>-o documentMode, switch, default = false</li>
     *            <li>imageName</li>
     *            </ul>
     * @throws HeadlessException When started in an environment that does not support a keyboard, display, or mouse.
     */
    public SOMViewer(JSAPResult config) throws HeadlessException {
        super();

        // Look and Feel is set her, as the call to Visualizations.getAvailableVisualizations() triggers the
        // initialisation of the control panels, then in the initial system look and feel (see #48, 224)
        UiUtils.setSOMToolboxLookAndFeel();
        setDefaultLookAndFeelDecorated(true);

        // just trigger to do initialisation work as finding visualisations and palettes in the beginning
        // with a current bug happening sometimes on loading the visualisation classes, this also prevents spending time
        // on loading the input data,
        // and then the application to hang
        Visualizations.getAvailableVisualizations();
        Palettes.getAvailablePalettes();

        /* set handler for all logging events, which decides where to output it */
        try {
            loggingHandler = new LoggingHandler();
            loggingHandler.setLevel(Level.FINEST);
            loggingHandler.setParentComponent(this);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").setLevel(Level.FINEST);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").addHandler(loggingHandler);
        } catch (SecurityException e1) {
            e1.printStackTrace();
        }

        // Jakob Frank: init state moved here - avoids NullPointerException
        state = new CommonSOMViewerStateData(this, 220);

        // highlightedDataNamesFileName = config.getString("highlightedDataNamesFile");
        unitDescriptionFileName = AbstractOptionFactory.getFilePath(config, "unitDescriptionFile");
        weightVectorFileName = AbstractOptionFactory.getFilePath(config, "weightVectorFile");
        mapDescriptionFileName = AbstractOptionFactory.getFilePath(config, "mapDescriptionFile");
        classInformationFileName = AbstractOptionFactory.getFilePath(config, "classInformationFile");
        regressionInformationFileName = AbstractOptionFactory.getFilePath(config, "regressionInformationFile");
        dataInformationFileName = AbstractOptionFactory.getFilePath(config, "dataInformationFile");
        inputVectorFileName = AbstractOptionFactory.getFilePath(config, "inputVectorFile");
        templateVectorFileName = AbstractOptionFactory.getFilePath(config, "templateVectorFile");
        dataWinnerMappingFileName = AbstractOptionFactory.getFilePath(config, "dataWinnerMappingFile");
        linkageMapFileName = AbstractOptionFactory.getFilePath(config, "linkageMapFile");
        classColoursFile = AbstractOptionFactory.getFilePath(config, "classColours");
        noInternalPlayer = config.getBoolean("noplayer");

        final String s = AbstractOptionFactory.getFilePath(config, "secondSOMPrefix");
        if (StringUtils.isNotBlank(s)) {
            final File file = new File(s).getAbsoluteFile();
            if (file.getParentFile() != null && file.getParentFile().exists()) {
                state.secondSOMName = file.getAbsolutePath();
                // add SOM to comparison visualisation
                ((ComparisonVisualizer) Visualizations.getVisualizationByName("ComparisonMean").getVis()).addSOM(s);
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Couldn't find path for second some, given: '" + s + ", resolves to: '"
                                + file.getAbsolutePath() + "'.");
            }
        }

        String inputCorrectionsFileName = config.getString("inputCorrections");

        if (config.getString("applicationDirectory", System.getenv("SOMTOOLBOX_BASEDIR")) != null) {
            applicationDirectory = config.getString("applicationDirectory", System.getenv("SOMTOOLBOX_BASEDIR"));
        }
        if (AbstractOptionFactory.getFilePath(config, "viewerWorkingDirectory") != null) {
            viewerWorkingDirectoryName = AbstractOptionFactory.getFilePath(config, "viewerWorkingDirectory");
        } else { // default the directory to the unit file location as the most likely path
            viewerWorkingDirectoryName = FileUtils.getPathFrom(unitDescriptionFileName);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Defaulting viewer working directory to " + viewerWorkingDirectoryName);
        }

        if (config.getString("fileNamePrefix") != null) {
            CommonSOMViewerStateData.fileNamePrefix = config.getString("fileNamePrefix");
        }
        if (config.getString("fileNameSuffix") != null) {
            CommonSOMViewerStateData.fileNameSuffix = config.getString("fileNameSuffix");
        }

        documentMode = config.getBoolean("documentMode", false);

        /* load preferences from file */
        String prefs_file = applicationDirectory + System.getProperty("file.separator") + PREFS_FILE;
        try {
            // first try properties file for specific operating system
            String osname = System.getProperty("os.name").split(" ", 2)[0];
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Reading preferences from " + prefs_file + "." + osname);
            prefs = new SOMViewerProperties(prefs_file + "." + osname);
        } catch (PropertiesException e) {
            try {
                // then try to read generic properties file
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("FAILED. Reading preferences from " + prefs_file);
                prefs = new SOMViewerProperties(prefs_file);
            } catch (PropertiesException pe) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(pe.getMessage());
                // TODO show message
                prefs = new SOMViewerProperties(); // create empty prefs to avoid NullPointerExceptions
            }
        }
        // Load the users personal prefs-file (if it exists)
        File userPrefs = SOMToolboxMetaConstants.USER_SOMVIEWER_PREFS;
        if (userPrefs.exists() && userPrefs.canRead()) {
            try {
                prefs.load(new FileReader(userPrefs));
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Could not load users preferences file: " + userPrefs.getPath());
            }
        }

        CommonSOMViewerStateData.somViewerProperties = prefs;

        if (prefs.getAudioPlayer() != null) {
            CommonSOMViewerStateData.MimeTypes.setAudioPlayer(prefs.getAudioPlayer());
        }

        /** ** input objects *** */
        state.inputDataObjects = new SharedSOMVisualisationData(classInformationFileName,
                regressionInformationFileName, dataInformationFileName, dataWinnerMappingFileName, inputVectorFileName,
                templateVectorFileName, linkageMapFileName);
        // reading input objects, if we have filenames set
        state.inputDataObjects.readAvailableData();

        if (StringUtils.isNotBlank(inputCorrectionsFileName)) {
            state.inputDataObjects.setFileName(SOMVisualisationData.INPUT_CORRECTIONS, inputCorrectionsFileName);
        }

        createAndShowGUI();

        // if passed as parameter - set initial palette
        String initialPalette = config.getString("initialPalette");
        if (initialPalette != null) {
            Palette palette = Palettes.getPaletteByName(initialPalette);
            if (palette != null) {
                // activate palette in menu
                for (int i = 0; i < paletteMenu.getMenuComponentCount(); i++) {
                    if (paletteMenu.getMenuComponent(i) instanceof JRadioButtonMenuItem) {
                        JRadioButtonMenuItem rb = (JRadioButtonMenuItem) paletteMenu.getMenuComponent(i);
                        if (at.tuwien.ifs.somtoolbox.util.StringUtils.equalsAny(rb.getText(), palette.getName(),
                                palette.getShortName())) {
                            rb.setSelected(true);
                            break;
                        }
                    }
                }

            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Unknown initial palette '" + initialPalette + "'.");
            }
        }

        // if passed as parameter - set initial visualisation
        String initialVis = config.getString("initialVisualisation");
        if (initialVis != null) {
            BackgroundImageVisualizerInstance vis = Visualizations.getVisualizationByName(initialVis);
            if (vis != null) {
                initialVisualisation = vis.getVis();
                initialVisualisationVariant = vis.getVariant();
                try {
                    if (initialVisualisation instanceof AbstractMatrixVisualizer) {
                        ((AbstractMatrixVisualizer) initialVisualisation).reversePalette();
                    }

                    // if passed as parameter - set initial visualisation params
                    String initialVisParams = config.getString("initialVisParams");
                    if (initialVisParams != null) {
                        // set params for SDH
                        if (initialVisualisation instanceof SmoothedDataHistograms) {
                            try {
                                int smoothingFactor = Integer.parseInt(initialVisParams);
                                SmoothedDataHistograms sdh = (SmoothedDataHistograms) initialVisualisation;
                                sdh.setSmoothingFactor(smoothingFactor);
                                SDHControlPanel controlPanel = (SDHControlPanel) sdh.getControlPanel();
                                controlPanel.spinnerSmoothingFactor.setValue(new Integer(smoothingFactor));
                            } catch (NumberFormatException e) {
                                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                                        "Visualisation Param 'Smoothing factor' is not a number: '" + initialVisParams
                                                + "'.");
                            }
                        } else if (initialVisualisation instanceof ThematicClassMapVisualizer) {
                            ThematicClassMapVisualizer them = (ThematicClassMapVisualizer) initialVisualisation;
                            boolean voronoi = false;
                            boolean chessBoard = false;
                            double minVisibleClass = 0;
                            String[] params = initialVisParams.split(",");
                            for (String param : params) {
                                String[] property = param.split("=");
                                if (property == null || property.length != 2) {
                                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                                            "Visualisation property '" + property + "' is not correct in : '"
                                                    + initialVisParams + "'.");
                                } else {
                                    if (property[0].equalsIgnoreCase("voronoi")) {
                                        voronoi = Boolean.valueOf(property[1]).booleanValue();
                                    } else if (property[0].equalsIgnoreCase("chessboard")) {
                                        chessBoard = Boolean.valueOf(property[1]).booleanValue();
                                    } else if (property[0].equalsIgnoreCase("minVisibleClass")) {
                                        minVisibleClass = Double.valueOf(property[1]).doubleValue();
                                    }

                                }
                            }
                            System.out.println("params - chessBoard:" + chessBoard + ", voronoi:" + voronoi
                                    + ", minVisibleClass:" + minVisibleClass);
                            them.setInitialParams(chessBoard, voronoi, minVisibleClass);
                        } else if (initialVisualisation instanceof ComparisonVisualizer) {
                            ComparisonVisualizer compVis = (ComparisonVisualizer) initialVisualisation;
                            String[] soms = initialVisParams.split(",");
                            for (String string : soms) {
                                compVis.addSOM(string);
                            }
                        }
                    }

                    mapPane.setInitialVisualization(initialVisualisation, initialVisualisationVariant);

                    // select correct visualisation from menu
                    for (int i = 0; i < visualizationMenu.getMenuComponentCount(); i++) {
                        Component comp = visualizationMenu.getMenuComponent(i);
                        if (comp instanceof JRadioButtonMenuItem) {
                            JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) comp;
                            if (at.tuwien.ifs.somtoolbox.util.StringUtils.equalsAny(radioButton.getText(),
                                    vis.getShortName(), vis.getName())) {
                                radioButton.setSelected(true);
                            }
                        }
                    }

                    updatePalettePanel();
                    visControlPanel.updateVisualisationControl();
                    resetControlElements(true);

                } catch (SOMToolboxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Unknown initial visualisation '" + initialVis + "'.");
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Valid options are: " + Arrays.toString(Visualizations.getAvailableVisualizationNames()));
            }
        }

        displayFrame(state.selectionPanel);

        // if passed as parameter - set second SOM
        if (StringUtils.isNotBlank(getMap().getState().secondSOMName)) {
            updateSOMComparison(true);
            mapPane.setShiftArrowsVisibility(true);
            mapPane.centerAndFitMapToScreen(0);
        }
    }

    private void createAndShowGUI() {
        /** ** files given on command line are read in here, map is created *** */
        mapPane = new SOMPane(this, weightVectorFileName, unitDescriptionFileName, mapDescriptionFileName, state);

        // setTitle("PlaySOM");
        setTitle("SOM Viewer - " + weightVectorFileName + " (version: " + SOMToolboxMetaConstants.getVersion() + ")");
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                    ClassLoader.getSystemResource(RESOURCE_PATH_ICONS + "somviewer_logo-24.png")));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Could not find application logo image file. Continuing.");
        }

        initWindowClosing();

        state.fileChooser = new JFileChooser(new File(viewerWorkingDirectoryName + "/."));

        /** ** MENU *** */
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // begin file menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem dataFilesMenuItem = new JMenuItem("Data Files");
        dataFilesMenuItem.setMnemonic(KeyEvent.VK_F);
        dataFilesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SharedSOMVisualisationDataDialog(SOMViewer.this, state).setVisible(true);
            }
        });
        fileMenu.add(dataFilesMenuItem);

        // add the plot data menu entry
        JMenuItem plotDataMenuItem;
        if (state.inputDataObjects == null || state.inputDataObjects.getInputData() == null) {
            plotDataMenuItem = new JMenuItem("Plot data (data needs to be loaded!)");
            plotDataMenuItem.setEnabled(false);
        } else if (state.inputDataObjects.getInputData().dim() > 3) {
            plotDataMenuItem = new JMenuItem("Plot data (only for dim <= 3)");
            plotDataMenuItem.setEnabled(false);
        } else {
            plotDataMenuItem = new JMenuItem("Plot data");
        }
        plotDataMenuItem.setMnemonic(KeyEvent.VK_P);
        plotDataMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataSetViewer viewer;
                SOMLibClassInformation classInfo = state.inputDataObjects.getClassInfo();
                InputData inputData = state.inputDataObjects.getInputData();
                if (inputData == null) {
                    SOMVisualisationData inputObject = state.inputDataObjects.getObject(SOMVisualisationData.INPUT_VECTOR);
                    try {
                        inputObject.loadFromFile(state.fileChooser, SOMViewer.this);
                        inputData = state.inputDataObjects.getInputData();
                    } catch (SOMToolboxException e1) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Input data file needed!");
                    }
                    if (inputData == null) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Input data file needed!");
                        return;
                    }
                }
                if (classInfo == null) {
                    viewer = new DataSetViewer(SOMViewer.this, inputData.getData());
                } else {
                    inputData.setClassInfo(classInfo);
                    String[] classNames = classInfo.classNames();
                    double[][][] data = new double[classNames.length][][];
                    for (int i = 0; i < classNames.length; i++) {
                        try {
                            data[i] = inputData.getData(classNames[i]);
                        } catch (SOMToolboxException ex) {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                    "Error retrieving class info: " + ex.getMessage());
                        }
                    }
                    viewer = new DataSetViewer(SOMViewer.this, classNames, classLegendPane.getColors(), data);
                }
                viewer.setVisible(true);
            }

        });
        fileMenu.add(plotDataMenuItem);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);
        // end file menu

        menuBar.add(createMapMenu());

        getContentPane().setLayout(new BorderLayout());

        /** ** TOOLBar *** */
        toolBar = new JToolBar("SOMViewer Toolbar");

        toolBar.add(makeToolbarButton("center3d-24.png", CENTER_AND_FIT_MAP, "Center Map"));
        toolBar.add(makeToolbarButton("reset-desktop.png", RESET_DESKTOP_LAYOUT, "Reset Layout"));
        toolBar.addSeparator();

        // Selection handler buttons
        ArrayList<AbstractButton> selectionHandlerButtons = new ArrayList<AbstractButton>();
        selectionHandlerButtons.add(makeToolbarToggleButton("rectangle.png", SELECT_RECTANGLE, "Rectangle Selection",
                true));
        selectionHandlerButtons.add(makeToolbarToggleButton("line.png", SELECT_LINE, "Line Selection", false));
        selectionHandlerButtons.add(makeToolbarToggleButton("clusterSelection.png", SELECT_CLUSTER,
                "Cluster Selection", false));
        // rudi: moving inputs for semi-supervised learning
        selectionHandlerButtons.add(makeToolbarToggleButton("moveinput.png", MOVE_INPUT, "Move Inputs", false));
        // Angela: move Labels
        selectionHandlerButtons.add(makeToolbarToggleButton("movelabel.png", MOVE_LABEL, "Move Labels", false));

        ButtonGroup selectionHandlerButtonGroup = new ButtonGroup();
        for (AbstractButton button : selectionHandlerButtons) {
            toolBar.add(button);
            selectionHandlerButtonGroup.add(button);
        }
        toolBar.addSeparator();
        // end selection handler buttons

        // toggles for map details (pie-charts, labels, data info
        boolean classInfoLoaded = getMap().getInputObjects().getClassInfo() != null;

        buttonPie.setIcon(TOGGLE_PIE_CHARTS_ICONS[0]);
        buttonPie.setToolTipText("Toggle pie-chart display");
        buttonPie.setEnabled(classInfoLoaded);
        buttonPie.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                menuPie.show(buttonPie, 0, buttonPie.getHeight());
            }
        });

        for (int i = 0; i < TOGGLE_PIE_CHARTS_MODES.length; i++) {
            menuPie.add(makeButtonMenutEntry(TOGGLE_PIE_CHARTS_MODES[i], TOGGLE_PIE_CHARTS_ICONS[i]));
        }

        JMenuBar menuBarPieCharts = new JMenuBar();
        menuBarPieCharts.setBorderPainted(false);
        menuBarPieCharts.add(menuPie);
        toolBar.add(buttonPie);

        toolBar.add(makeToolbarToggleButton("labels.png", TOGGLE_LABELS, TOGGLE_LABELS, state.labelVisibilityMode));
        toolBar.add(makeToolbarToggleButton("hits.png", TOGGLE_HITS, TOGGLE_HITS, state.hitsVisibilityMode));
        toolBar.add(makeToolbarToggleButton("data.png", TOGGLE_DATA, TOGGLE_DATA, state.dataVisibilityMode));

        boolean linkageInfoLoaded = getMap().getInputObjects().getLinkageMap() != null;
        linkageToggleButton = makeToolbarToggleButton("linkage.png", TOGGLE_LINKAGE, "Linkage",
                state.displayInputLinkage);
        linkageToggleButton.setEnabled(linkageInfoLoaded);
        toolBar.add(linkageToggleButton);

        toolBar.addSeparator();

        // toggles specific for input location (exact placement)
        exactPlacementToggleButton = makeToolbarToggleButton("exactPlacement.png", TOGGLE_EXACT_PLACEMENT, "Exact",
                state.exactUnitPlacement);
        toolBar.add(exactPlacementToggleButton);

        shiftOverlappingToggleButton = makeToolbarToggleButton("shiftOverlapping.png", TOGGLE_RELOCATE, "Relocate",
                state.shiftOverlappingInputs);
        toolBar.add(shiftOverlappingToggleButton);

        if (!state.exactUnitPlacementEnabled) {
            exactPlacementToggleButton.setEnabled(false);
            exactPlacementToggleButton.setToolTipText(TOGGLE_EXACT_PLACEMENT + ": " + MSG_EXACTPLACEMENT_DISABLED);
            shiftOverlappingToggleButton.setEnabled(false);
            shiftOverlappingToggleButton.setToolTipText(TOGGLE_RELOCATE + ": " + MSG_EXACTPLACEMENT_DISABLED);
        }

        toolBar.addSeparator();
        // end toggles for map details

        toolBar.add(makeToolbarButton("createlabel.png", CREATE_LABEL, "New label"));
        toolBar.addSeparator();

        AbstractButton btn3dViewer = makeToolbarButton("somviewer3D_logo-24.png", SOMVIEWER_3D, "3D");
        // Check if 3D is available
        try {
            Class.forName("at.tuwien.ifs.somtoolbox.apps.viewer3d.SOMViewer3D");
        } catch (ClassNotFoundException e1) {
            btn3dViewer.setEnabled(false);
        }
        toolBar.add(btn3dViewer);
        toolBar.addSeparator();

        getContentPane().add(toolBar, BorderLayout.PAGE_START);

        /** ** StatusBar *** */
        statusBar = new StatusBar();
        getContentPane().add(statusBar, BorderLayout.PAGE_END);
        if (loggingHandler != null) {
            loggingHandler.setStatusBar(statusBar);
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Initializing GUI ...");

        /** ** Layout of the left control pane *** */
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;

        collector = new ControlCollector("Test", state);
        // desktop.add(collector, c);

        /** ** Map Overview *** */
        MapOverviewPane mapOverviewPane = new MapOverviewPane("Map overview", state);
        mapOverviewPane.connect(mapPane.getCanvas(), new PLayer[] { mapPane.getCanvas().getLayer() });
        collector.addControl(mapOverviewPane, true);

        /** ** PlaySOMPanel *** */
        AbstractSelectionPanel selectionPanel = makeSelectionPanel();
        mapPane.connectSelectionHandlerTo(selectionPanel);
        c.weighty = 0.5; // IMPORTANT note: these values remain active for ComparisonPanel
        c.weightx = 0.1; // and are reset afterwards
        // desktop.add(selectionPanel, c);
        collector.addControl(selectionPanel, true);
        state.selectionPanel = selectionPanel;

        AbstractSelectionPanel diverPanel = null;
        if (state.growingLayer.getAllSubMaps().size() > 0) {
            // added by Philip Langer
            /** ** HierarchicalDiverPanel *** */
            diverPanel = new GHSOMNavigationPanel(state, mapPane);
            mapPane.connectSelectionHandlerTo(diverPanel);
            diverPanel.setVisible(true);
            collector.addControl(diverPanel, true);
        }
        // /added by Philip Langer

        /** ** Comparison *** */
        ComparisonPanel compPanel = new ComparisonPanel(state);
        mapPane.connectSelectionHandlerTo(compPanel);
        collector.addControl(compPanel);
        compPanel.setVisible(false);
        c.weightx = 0.0;
        c.weighty = 0.0;

        MultichannelPlaybackPanel multichannelPlaybackPanel = new MultichannelPlaybackPanel(state, mapPane);
        mapPane.connectSelectionHandlerTo(compPanel);
        // desktop.add(mapPane, c);
        // mapPane.setVisible(false);
        c.weightx = 0.0;
        c.weighty = 0.0;

        /** ** Class legend panel *** */
        classLegendPane = new ClassLegendPane(mapPane, "Class legend", state);
        collector.addControl(classLegendPane, true);

        /** ** Query legend panel *** */
        queryPane = new QuerySOMPanel("Query", state);
        collector.addControl(queryPane);
        if (!documentMode) {
            queryPane.setVisible(false);
        }

        /** ** Map detail panel *** */
        MapDetailPanel mapDetailPanel = new MapDetailPanel("Map details", state);
        mapDetailPanel.setVisible(false);
        collector.addControl(mapDetailPanel);
        // show the initial zoom factor
        mapDetailPanel.updatePanel(mapPane.getCanvas().getCamera().getViewScale());

        /** ** Shifts Control Panel *** */
        shiftsControlPanel = new ShiftsControlPanel(mapPane, state, "Shifts Control Panel");
        collector.addControl(shiftsControlPanel);

        /** ** Palette panel *** */
        palettePanel = new PalettePanel("Palette", state);
        collector.addControl(palettePanel, true);

        /** ** Visualization Control panel (e.g. SDH) *** */
        visControlPanel = new VisualizationControl("Visualisation control", state, mapPane); // just a placeholder for
        // the actual control panel
        collector.addControl(visControlPanel, true);
        // desktop.add(visControlPanel, c);

        /** ** Clustering Control panel (Angela) *** */
        clusteringControl = new ClusteringControl("Clustering Control", state, mapPane);
        clusteringControl.setVisible(false);
        collector.addControl(clusteringControl);

        /* PocketSOM Conncetor (Jakob) */
        PocketSOMConnector psCon = new PocketSOMConnector("PocketSOM Connector", state);
        psCon.setVisible(false);
        collector.addControl(psCon);

        /* */
        PlaygroundPanel pgp = new PlaygroundPanel("Playground", state);
        pgp.setVisible(false);
        collector.addControl(pgp);

        somFrame.getContentPane().add(mapPane);
        somFrame.toBack();
        // desktop.add(somFrame);

        JSplitPane central = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, collector, mapPane);
        central.setOneTouchExpandable(true);
        getContentPane().add(central, BorderLayout.CENTER);

        // getContentPane().add(desktop, BorderLayout.CENTER);

        // collector.pack();
        collector.setVisible(true);

        createVisualizationMenu();
        menuBar.add(visualizationMenu);

        /** ** palette menu *** */
        menuBar.add(createPaletteMenu());

        // Angela: add the cluster menu
        createClusterMenu();

        // add the export menu
        createExportMenu();

        // add the input correction menu
        JMenu inputCorrectionMenu = new JMenu("Input correction");

        JMenuItem loadInputCorrectionsFileMenuItem = new JMenuItem("Load corrections file");
        loadInputCorrectionsFileMenuItem.setMnemonic(KeyEvent.VK_L);
        loadInputCorrectionsFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File inputFile = ExportUtils.getFilePath(SOMViewer.this, state.fileChooser,
                        "Load input corrections file", new FileNameExtensionFilter("Input corrections file (*.cor)",
                                "corr"));
                if (inputFile != null) {
                    try {
                        state.inputDataObjects.getInputCorrections().readFromFile(inputFile.getAbsolutePath(),
                                state.growingLayer, state.inputDataObjects.getInputData());
                        getMap().createInputCorrectionArrows();
                        getMap().updateDetailsAfterMoving();
                        JOptionPane.showMessageDialog(SOMViewer.this, "Loading input corrections from file '"
                                + inputFile.getAbsolutePath() + "' finished!");
                    } catch (SOMToolboxException ex) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ex.getMessage());
                    }
                }
            }
        });
        inputCorrectionMenu.add(loadInputCorrectionsFileMenuItem);

        JMenuItem saveInputCorrectionsFileMenuItem = new JMenuItem("Save corrections file");
        saveInputCorrectionsFileMenuItem.setMnemonic(KeyEvent.VK_S);
        saveInputCorrectionsFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File outputFile = ExportUtils.getFilePath(SOMViewer.this, state.fileChooser,
                        "Save input corrections file", new FileNameExtensionFilter("Input corrections file (*.corr)",
                                "corr"));
                if (outputFile != null) {
                    try {
                        String outputFileName = outputFile.getAbsolutePath();
                        if (!outputFileName.endsWith(".corr")) {
                            outputFileName += ".corr";
                            outputFile = new File(outputFileName);
                        }
                        state.inputDataObjects.getInputCorrections().writeToFile(outputFile);
                        JOptionPane.showMessageDialog(SOMViewer.this, "Saving input corrections to file '"
                                + outputFile.getAbsolutePath() + "' finished!");
                    } catch (SOMToolboxException ex) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ex.getMessage());
                    }
                }
            }
        });
        inputCorrectionMenu.add(saveInputCorrectionsFileMenuItem);

        JMenuItem saveUnitFileMenuItem = new JMenuItem("Save moved unit file");
        saveUnitFileMenuItem.setMnemonic(KeyEvent.VK_U);
        saveUnitFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File outputFile = ExportUtils.getFilePath(SOMViewer.this, state.fileChooser, "Save unit file",
                        new FileNameExtensionFilter("Unit description files (*.unit)", "unit"));
                if (outputFile != null) {
                    try {
                        SOMLibMapOutputter.writeUnitDescriptionFile(state.growingSOM,
                                outputFile.getParentFile().getAbsolutePath(), outputFile.getName(), true);
                        JOptionPane.showMessageDialog(SOMViewer.this, "Saving to unit file '"
                                + outputFile.getAbsolutePath() + "' finished!");
                    } catch (IOException ex) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                "Error writing unit file: " + ex.getMessage());
                    }
                }
            }
        });
        inputCorrectionMenu.add(saveUnitFileMenuItem);

        JMenuItem clearInputCorrectionsMenuItem = new JMenuItem("Clear corrections");
        clearInputCorrectionsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (InputCorrection correction : state.inputDataObjects.getInputCorrections().getInputCorrections()) {
                    correction.getSourceUnit().addMappedInput(correction.getLabel(), correction.getOriginalDistance(),
                            true);
                    correction.getTargetUnit().removeMappedInput(correction.getLabel());
                }
                getMap().updateDetailsAfterMoving();
                state.inputDataObjects.getInputCorrections().getInputCorrections().clear();
                getMap().clearInputCorrections();
            }
        });
        inputCorrectionMenu.add(clearInputCorrectionsMenuItem);

        final JCheckBoxMenuItem showInputCorrectionsMenuItem = new JCheckBoxMenuItem("Show corrections");
        showInputCorrectionsMenuItem.setSelected(true);
        showInputCorrectionsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getMap().setInputCorrectionsVisible(showInputCorrectionsMenuItem.isSelected());
            }
        });
        inputCorrectionMenu.add(showInputCorrectionsMenuItem);

        JMenu calcFeatureWeightsMenu = new JMenu("Calculate feature weights");

        JMenuItem calcFeatureWeightsGlobalMenuItem = new JMenuItem("Global");
        calcFeatureWeightsGlobalMenuItem.addActionListener(new CalculateFeatureWeightsActionListener(
                FeatureWeightMode.GLOBAL));
        calcFeatureWeightsMenu.add(calcFeatureWeightsGlobalMenuItem);

        JMenuItem calcFeatureWeightsLocalMenuItem = new JMenuItem("Local");
        calcFeatureWeightsLocalMenuItem.addActionListener(new CalculateFeatureWeightsActionListener(
                FeatureWeightMode.LOCAL));
        calcFeatureWeightsMenu.add(calcFeatureWeightsLocalMenuItem);

        JMenuItem calcFeatureWeightsGeneralMenuItem = new JMenuItem("General");
        calcFeatureWeightsGeneralMenuItem.addActionListener(new CalculateFeatureWeightsActionListener(
                FeatureWeightMode.GENERAL));
        calcFeatureWeightsMenu.add(calcFeatureWeightsGeneralMenuItem);

        inputCorrectionMenu.add(calcFeatureWeightsMenu);

        menuBar.add(inputCorrectionMenu);

        // end mapCorrectionMenu

        // add the labelling menu
        JMenu labelMenu = new JMenu("Labelling");
        JMenuItem runLabelMenuItem = new JMenuItem("Rerun labelling");
        runLabelMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LabellingDialog(state).setVisible(true);
            }
        });
        labelMenu.add(runLabelMenuItem);
        menuBar.add(labelMenu);

        // window menu
        state.registerComponentWindow(toolBar, "Toolbar");
        state.registerComponentWindow(somFrame, "SOM Map");
        // controls, ordered in their appearance
        state.registerComponentWindow(mapOverviewPane, "Map Overview");
        state.registerComponentWindow(selectionPanel, "Play Selection");
        if (diverPanel != null) {
            // added by Philip Langer
            state.registerComponentWindow(diverPanel, "Hierarchical Diver Control");
            // /added by Philip Langer
        }
        state.registerComponentWindow(classLegendPane, "Class Legend");
        state.registerComponentWindow(shiftsControlPanel, "Shifts Control");
        state.registerComponentWindow(palettePanel, "Palette");
        state.registerComponentWindow(compPanel, "Label Comparison");
        state.registerComponentWindow(queryPane, "Map Query");
        state.registerComponentWindow(visControlPanel, "Visualization Control");
        state.registerComponentWindow(clusteringControl, "Clustering Control");
        state.registerComponentWindow(mapDetailPanel, "Map Detail Control");
        state.registerComponentWindow(psCon, "PocketSOM Connector");
        state.registerComponentWindow(pgp, "Playground");
        state.registerComponentWindow(multichannelPlaybackPanel, "Multichannel playback panel");

        // register observers to the class information
        getMap().getInputObjects().getObject(SOMVisualisationData.CLASS_INFO).addObserver(this);

        pack();
        somFrame.pack();
        somFrame.setLocation(state.controlElementsWidth, 0);
        somFrame.setVisible(true);

        toolBar.add(Box.createHorizontalGlue());
        AbstractButton btnDonate = makeToolbarButton("donate.png", "Make a donation", "Donate!");
        btnDonate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(new URI("http://www.ifs.tuwien.ac.at/dm/somtoolbox/"));
                } catch (Exception ex) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Could not launch external browser.");
                }
            }
        });
        toolBar.add(btnDonate);

        createWindowMenu(mapOverviewPane);
    }

    private int rotatedQuadrants = 0;

    private boolean flippedX = false, flippedY = false;

    private void doAnimation() {
        // Set to 0 to deactivate animation.
        int animationDuration = 1500;
        int delta = 1000;
        Random rand = new Random();
        for (Unit u : state.growingLayer.getAllUnits()) {
            GeneralUnitPNode unit = state.mapPNode.getUnit(u);
            AffineTransform transform = AffineTransform.getQuadrantRotateInstance(-rotatedQuadrants, unit.getX()
                    + unit.getWidth() / 2, unit.getY() + unit.getHeight() / 2);
            if (flippedX) {
                if (rotatedQuadrants % 2 == 0) {
                    transform.scale(-1, 1);
                    transform.translate(-(2 * unit.getX() + unit.getHeight()), 0);
                } else {
                    transform.scale(1, -1);
                    transform.translate(0, -(2 * unit.getY() + unit.getWidth()));
                }
            }
            if (flippedY) {
                if (rotatedQuadrants % 2 == 0) {
                    transform.scale(1, -1);
                    transform.translate(0, -(2 * unit.getY() + unit.getWidth()));
                } else {
                    transform.scale(-1, 1);
                    transform.translate(-(2 * unit.getX() + unit.getHeight()), 0);
                }
            }
            unit.animateToTransform(transform, animationDuration
                    + (animationDuration > 0 ? rand.nextInt(delta) - delta / 2 : 0));
        }
        AffineTransform transform = AffineTransform.getQuadrantRotateInstance(rotatedQuadrants,
                state.mapPNode.getWidth() / 2, state.mapPNode.getHeight() / 2);
        if (flippedX) {
            transform.scale(-1, 1);
            transform.translate(-state.mapPNode.getWidth(), 0);
        }
        if (flippedY) {
            transform.scale(1, -1);
            transform.translate(0, -state.mapPNode.getHeight());
        }
        mapPane.node.animateToTransform(transform, animationDuration);
        mapPane.lineSelection.animateToTransform(transform, animationDuration);
    }

    /**
     * @return The Map Menu
     */
    private JMenu createMapMenu() {
        JMenu mapMenu = new JMenu("Map");
        mapMenu.setMnemonic('M');

        JMenuItem rotCW = makeButtonMenutEntry("Rotate Clockwise", "map_rotate+90.png", 'R', KeyStroke.getKeyStroke(
                KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));
        rotCW.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotatedQuadrants = (rotatedQuadrants + 1) % 4;
                doAnimation();
            }

        });

        JMenuItem rotCCW = makeButtonMenutEntry("Rotate CounterClockwise", "map_rotate-90.png", 'C',
                KeyStroke.getKeyStroke("alt pressed L"));
        rotCCW.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotatedQuadrants = (rotatedQuadrants + 3) % 4;
                doAnimation();
            }
        });

        JMenuItem flipH = makeButtonMenutEntry("Flip around horizontal axis", "map_flip_h.png", 'H',
                KeyStroke.getKeyStroke("alt pressed H"));
        flipH.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rotatedQuadrants % 2 == 0) {
                    flippedY = !flippedY;
                } else {
                    flippedX = !flippedX;
                }
                doAnimation();
            }
        });

        JMenuItem flipV = makeButtonMenutEntry("Flip around vertical axis", "map_flip_v.png", 'V',
                KeyStroke.getKeyStroke("alt pressed V"));
        flipV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rotatedQuadrants % 2 == 0) {
                    flippedX = !flippedX;
                } else {
                    flippedY = !flippedY;
                }
                doAnimation();
            }

        });

        JMenuItem save = makeButtonMenutEntry("Save Map", "map_save.png");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JCheckBox chkGZIP = new JCheckBox("GZip compress");
                    chkGZIP.setSelected(true);
                    JPanel accessory = new JPanel();
                    accessory.add(chkGZIP);
                    state.fileChooser.setAccessory(accessory);
                    final int result = state.fileChooser.showSaveDialog(SOMViewer.this);
                    state.fileChooser.setAccessory(null);
                    if (result != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    File target = state.fileChooser.getSelectedFile();
                    String dir = target.getParent();
                    String output = target.getName();

                    GrowingSOM newSOM = (GrowingSOM) state.growingSOM.clone();
                    if (flippedX) {
                        newSOM.getLayer().flip(Flip.VERTICAL);
                    }
                    if (flippedY) {
                        newSOM.getLayer().flip(Flip.HORIZONTAL);
                    }
                    switch (rotatedQuadrants) {
                        case 1:
                            newSOM.getLayer().rotate(Rotation.ROTATE_90);
                            break;
                        case 2:
                            newSOM.getLayer().rotate(Rotation.ROTATE_180);
                            break;
                        case 3:
                            newSOM.getLayer().rotate(Rotation.ROTATE_270);
                            break;
                        default:
                            // nop;
                    }

                    SOMLibMapOutputter.writeUnitDescriptionFile(newSOM, dir, output, chkGZIP.isSelected());
                    SOMLibMapOutputter.writeWeightVectorFile(newSOM, dir, output, chkGZIP.isSelected());
                    if (state.growingSOM.getSharedInputObjects().getObject(SOMVisualisationData.DATA_WINNER_MAPPING) != null) {
                        SOMLibDataWinnerMapping dwm = state.growingSOM.getSharedInputObjects().getDataWinnerMapping().clone();
                        if (flippedX) {
                            dwm.flipV(state.growingLayer.getXSize());
                        }
                        if (flippedY) {
                            dwm.flipH(state.growingLayer.getYSize());
                        }
                        dwm.rotate(rotatedQuadrants, state.growingLayer.getXSize(), state.growingLayer.getYSize());
                        SOMLibMapOutputter.writeDataWinnerMappingFile(dwm, dir, output, chkGZIP.isSelected());
                    }
                } catch (CloneNotSupportedException e1) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not copy the map");
                    e1.printStackTrace();
                } catch (IOException e2) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not save the changed map");
                    e2.printStackTrace();
                }
            }
        });

        mapMenu.add(rotCW);
        mapMenu.add(rotCCW);
        mapMenu.addSeparator();
        mapMenu.add(flipH);
        mapMenu.add(flipV);
        mapMenu.addSeparator();
        mapMenu.add(save);

        return mapMenu;
    }

    private JMenuItem makeButtonMenutEntry(final String text, final String imageName, final char mnemonic,
            final KeyStroke shortCut) {
        return makeButtonMenutEntry(text, imageName, ((int) mnemonic), shortCut);
    }

    private JMenuItem makeButtonMenutEntry(final String text, final String imageName, final int mnemonic) {
        return makeButtonMenutEntry(text, imageName, mnemonic, null);
    }

    private JMenuItem makeButtonMenutEntry(final String text, final String imageName) {
        return makeButtonMenutEntry(text, imageName, -1);
    }

    private JMenuItem makeButtonMenutEntry(final String text, final String imageName, final int mnemonic,
            final KeyStroke shortCut) {
        JMenuItem menuItem = makeButtonMenutEntry(text, UiUtils.getIcon(imageName));
        if (mnemonic > 0) {
            menuItem.setMnemonic(mnemonic);
        }
        if (shortCut != null) {
            menuItem.setAccelerator(shortCut);
        }

        return menuItem;
    }

    private JMenuItem makeButtonMenutEntry(final String text, final ImageIcon icon) {
        JMenuItem jMenuItem = new JMenuItem(text, icon);
        jMenuItem.addActionListener(this);
        return jMenuItem;
    }

    private void displayFrame(AbstractSelectionPanel selectionPanel) {
        resetControlElements(false);
        mapPane.centerAndFitMapToScreen(0);
        if (documentMode) {
            initDocViewer(selectionPanel);
        }
        if (classColoursFile != null && state.inputDataObjects.getClassInfo() != null) {
            state.inputDataObjects.getClassInfo().loadClassColours(new File(classColoursFile));
            classLegendPane.updateClassColours();
        }
        setVisible(true);
    }

    private JMenu createPaletteMenu() {
        if (paletteMenu == null) {
            paletteMenu = new JMenu("Palette");
            paletteMenu.setMnemonic(KeyEvent.VK_P);
            rebuildPaletteMenu();
        }
        return paletteMenu;
    }

    public void rebuildPaletteMenu() {
        final int MIN_SUBMENU_SIZE = 3;
        paletteMenu.removeAll();
        paletteMenuItemGroup = new ButtonGroup();

        // List available palettes
        Hashtable<String, ArrayList<Palette>> plist = new Hashtable<String, ArrayList<Palette>>();
        Palette[] availablePalettes = Palettes.getAvailablePalettes();
        for (Palette palette : availablePalettes) {
            if (palette.isHidden()) {
                continue;
            }
            ArrayList<Palette> sub = plist.get(palette.getPaletteGroup());
            if (sub == null) {
                sub = new ArrayList<Palette>();
            }
            sub.add(palette);
            plist.put(palette.getPaletteGroup(), sub);
        }

        TreeSet<String> groups = new TreeSet<String>();
        groups.addAll(plist.keySet());
        for (String group : groups) {
            if (group.length() == 0) {
                // The default group is handled separately.
                continue;
            }
            ArrayList<Palette> pl = plist.get(group);
            JMenu currentM;
            if (pl.size() < 1) {
                // Ignore empty groups
                continue;
            } else if (pl.size() < MIN_SUBMENU_SIZE) {
                // Flat
                currentM = paletteMenu;
            } else {
                // SubMenu
                currentM = new JMenu(group);
                paletteMenu.add(currentM);
            }
            for (Palette palette : pl) {
                JRadioButtonMenuItem paletteMenuItem = new JRadioButtonMenuItem(palette.getName());
                paletteMenuItem.setMnemonic(palette.getName().charAt(0));
                paletteMenuItem.setActionCommand(palette.getName());
                paletteMenuItemGroup.add(paletteMenuItem);
                paletteMenuItem.addActionListener(new PaletteCheckboxMenuItemListener(palette));
                paletteMenuItem.setToolTipText(palette.getDescription());

                if (palette == Palettes.getDefaultPalette()) {
                    paletteMenuItem.setSelected(true);
                }
                currentM.add(paletteMenuItem);
            }
        }

        // The default group
        if (paletteMenu.getComponentCount() > 0) {
            paletteMenu.addSeparator();
        }
        if (plist.get("") != null) {
            for (Palette palette : plist.get("")) {
                JRadioButtonMenuItem paletteMenuItem = new JRadioButtonMenuItem(palette.getName());
                paletteMenuItem.setMnemonic(palette.getName().charAt(0));
                paletteMenuItemGroup.add(paletteMenuItem);
                paletteMenuItem.addActionListener(new PaletteCheckboxMenuItemListener(palette));
                paletteMenuItem.setToolTipText(palette.getDescription());

                if (palette == Palettes.getDefaultPalette()) {
                    paletteMenuItem.setSelected(true);
                }
                paletteMenu.add(paletteMenuItem);
            }
        }

        // Other Stuff
        paletteMenu.addSeparator();
        if (reversePaletteMenuItem == null) {
            reversePaletteMenuItem = new JCheckBoxMenuItem("Reverse");
            reversePaletteMenuItem.setMnemonic(KeyEvent.VK_R);
            reversePaletteMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        boolean success = getMap().reversePalette();
                        if (success) {
                            SOMViewer.this.visualizationChangeSuccess();
                        } else {
                            SOMViewer.this.visualizationChangeFailure();
                        }
                        updatePalettePanel();
                        mapPane.repaint();
                    } catch (SOMToolboxException ex) {
                        JOptionPane.showMessageDialog(SOMViewer.this, ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
        paletteMenu.add(reversePaletteMenuItem);
        paletteMenu.addSeparator();

        JMenuItem importer = new JMenuItem("Import Palette File");
        importer.setMnemonic(KeyEvent.VK_I);
        importer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = state.getFileChooser();
                jfc.setFileFilter(new FileFilter() {

                    @Override
                    public String getDescription() {
                        return "XML Palette Files";
                    }

                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        if (f.getName().endsWith(".xml")) {
                            return true;
                        }
                        return false;
                    }
                });
                if (jfc.showOpenDialog(SOMViewer.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        Palettes.addPalette(Palette.loadPaletteFromXML(jfc.getSelectedFile()));
                        rebuildPaletteMenu();
                    } catch (SOMToolboxException e1) {
                        JOptionPane.showMessageDialog(SOMViewer.this, e1.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        paletteMenu.add(importer);
        paletteMenu.addSeparator();

        if (paletteEditorMenuItem == null) {
            paletteEditorMenuItem = new JMenuItem("Palette Editor");
            paletteEditorMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new PaletteEditor(SOMViewer.this, state).setVisible(true);
                }
            });
        }
        paletteMenu.add(paletteEditorMenuItem);
    }

    private void resetControlElements(boolean maximizeElements) {
        for (AbstractViewerControl avc : collector.getDefaultControls()) {
            if (avc.isFullFunctional()) {
                avc.setVisible(true);
            }
        }

        for (int i = 0; i < state.registeredViewerControls.size(); i++) {
            AbstractViewerControl comp = (AbstractViewerControl) state.registeredViewerControls.get(i);
            if (comp.isVisible()) {
                if (maximizeElements) {
                    comp.setCollapsed(false);
                }
            }
        }
    }

    private void resetDesktopLayout() {
        resetControlElements(true);
    }

    private AbstractSelectionPanel makeSelectionPanel() {
        if (documentMode) {
            return new DocSOMPanel(state);
        } else {
            if (noInternalPlayer) {
                return new PlaySOMPanel(state);
            } else {
                return new PlaySOMPlayer(state);
            }
        }
    }

    private void createWindowMenu(MapOverviewPane mapOverviewPane) {
        windowMenu = new JMenu("Window");
        windowMenu.setMnemonic(KeyEvent.VK_W);

        ArrayList<String> componentNames = new ArrayList<String>(state.registeredComponentWindows.keySet());
        Collections.sort(componentNames);
        for (String name : componentNames) {
            Component component = state.registeredComponentWindows.get(name);
            MyJCheckBoxMenuItem checkBoxMenuItem = new MyJCheckBoxMenuItem(name, component);
            windowMenu.add(checkBoxMenuItem);
        }

        menuBar.add(windowMenu);
    }

    public void uncheckComponentInMenu(Component comp) {
        if (windowMenu == null) {
            return;
        }
        Component[] components = windowMenu.getMenuComponents();
        for (Component component2 : components) {
            if (component2 instanceof MyJCheckBoxMenuItem) {
                MyJCheckBoxMenuItem item = (MyJCheckBoxMenuItem) component2;
                if (item.getComponent() == comp) {
                    item.setSelected(false);
                }
            }
        }
    }

    private void createVisualizationMenu() {
        visualizationMenu = new JMenu("Visualization");
        visualizationMenu.setMnemonic(KeyEvent.VK_V);
        visualizationMenuItemGroup = new ButtonGroup();
        JRadioButtonMenuItem nonVis = new JRadioButtonMenuItem("none");
        nonVis.setToolTipText("no cluster visualization");
        nonVis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapPane.setNoVisualization();
                palettePanel.setPalette(null);
                visControlPanel.updateVisualisationControl();
            }
        });
        visualizationMenuItemGroup.add(nonVis);
        visualizationMenu.add(nonVis);
        BackgroundImageVisualizer[] visualisations = Visualizations.getAvailableVisualizations();
        JMenu currentVisMenu = new JMenu("Visualisations");
        visualizationMenu.add(currentVisMenu);
        boolean firstQualityMeasure = false;
        boolean firstCompareVis = false;
        for (int v = 0; v < visualisations.length; v++) {
            if (visualisations[v] instanceof ComparisonVisualizer && !firstCompareVis) {
                currentVisMenu.remove(currentVisMenu.getItemCount() - 1);
                currentVisMenu = new JMenu("SOM Comparison");
                visualizationMenu.add(currentVisMenu);
                firstCompareVis = true;
            } else if (visualisations[v] instanceof QualityMeasureVisualizer && !firstQualityMeasure) {
                currentVisMenu.remove(currentVisMenu.getItemCount() - 1);
                currentVisMenu = new JMenu("Quality Measures");
                visualizationMenu.add(currentVisMenu);
                firstQualityMeasure = true;
            }
            for (int w = 0; w < visualisations[v].getNumberOfVisualizations(); w++) {
                JMultiLineRadioButtonMenuItem mi = new JMultiLineRadioButtonMenuItem(
                        visualisations[v].getVisualizationName(w));
                mi.addActionListener(new VisualizationActionListener(v, w));
                mi.setToolTipText(visualisations[v].getVisualizationDescription(w));
                visualizationMenuItemGroup.add(mi);
                currentVisMenu.add(mi);
                if (visualisations[v] instanceof ThematicClassMapVisualizer) {
                    thematicClassRadioButton = mi;
                    if (!((ThematicClassMapVisualizer) visualisations[v]).hasClassInfo()) {
                        mi.setEnabled(false);
                    }
                }
            }
            currentVisMenu.add(new JSeparator());
        }
        currentVisMenu.remove(currentVisMenu.getItemCount() - 1);
        nonVis.setSelected(true);
        oldSelectedVisualizationMenuItem = nonVis.getModel();

        JMenuItem clearVisCacheMenuItem = new JMenuItem("Clear Visualisation Cache");
        clearVisCacheMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // clear the visualisation cache
                AbstractBackgroundImageVisualizer.clearVisualisationCache();
            }
        });
        visualizationMenu.add(clearVisCacheMenuItem);

        visualizationMenu.add(new JSeparator());

        final JCheckBoxMenuItem showBackgroundImageMenuItem = new JCheckBoxMenuItem("Show background image");
        showBackgroundImageMenuItem.setMnemonic(KeyEvent.VK_B);
        showBackgroundImageMenuItem.setEnabled(false);
        showBackgroundImageMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getMap().setBackgroundImageVisibility(showBackgroundImageMenuItem.isSelected());
                mapPane.getCurrentVisualization().getControlPanel().updateSwitchControls();
            }
        });
        visualizationMenu.add(showBackgroundImageMenuItem);

        JMenuItem loadBackgroundImageMenuItem = new JMenuItem("Load background image");
        loadBackgroundImageMenuItem.setMnemonic(KeyEvent.VK_L);
        loadBackgroundImageMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = state.fileChooser;
                if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                    state.fileChooser = new JFileChooser(fileChooser.getSelectedFile().getPath());
                }
                fileChooser.setName("Open Background Image");
                int returnVal = fileChooser.showDialog(SOMViewer.this, "Open Image");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        BufferedImage image = ImageIO.read(new File(fileChooser.getSelectedFile().getAbsolutePath()));
                        getMap().setBackgroundImage(image);
                        showBackgroundImageMenuItem.setEnabled(true);
                        showBackgroundImageMenuItem.setSelected(true);
                        if (mapPane.getCurrentVisualization() != null
                                && mapPane.getCurrentVisualization().getControlPanel() != null) {
                            mapPane.getCurrentVisualization().getControlPanel().updateSwitchControls();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        visualizationMenu.add(loadBackgroundImageMenuItem);

        showShiftsMenuItem = new JCheckBoxMenuItem("Show data shifts");
        showShiftsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapPane.setShiftArrowsVisibility(showShiftsMenuItem.isSelected());
                shiftsControlPanel.setEnabled(showShiftsMenuItem.isSelected());
            }
        });
        if (getMap().getState().secondSOMName.equals("")) {
            showShiftsMenuItem.setEnabled(false);
            shiftsControlPanel.setVisible(false);
        } else {
            showShiftsMenuItem.setSelected(true);
            shiftsControlPanel.setVisible(true);
        }
        visualizationMenu.add(showShiftsMenuItem);

        // switch map submenu
        switchMapSubmenu = new JMenu("Switch map");
        switchMapSubmenu.setToolTipText("Switch visualisations between main and second map.");

        useMainMap = new JMultiLineRadioButtonMenuItem("Use main map");
        useMainMap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // add magic here.
                // mapPane.setShiftsVisibility(showShiftsMenuItem.isSelected());
                // shiftsControlPanel.setEnabled(showShiftsMenuItem.isSelected());
                // mapPane.updateShifts();
            }
        });
        useSecondMap = new JMultiLineRadioButtonMenuItem("Use second map");
        useSecondMap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // add magic here.
                // mapPane.setShiftsVisibility(showShiftsMenuItem.isSelected());
                // shiftsControlPanel.setEnabled(showShiftsMenuItem.isSelected());
                // mapPane.updateShifts();
            }
        });

        ButtonGroup switchMapMenuGroup = new ButtonGroup();
        switchMapMenuGroup.add(useMainMap);
        switchMapMenuGroup.add(useSecondMap);

        switchMapSubmenu.add(useMainMap);
        switchMapSubmenu.add(useSecondMap);

        if (getMap().getState().secondSOMName.equals("")) {
            switchMapSubmenu.setEnabled(false);
        } else {
            switchMapSubmenu.setSelected(true);
        }

        visualizationMenu.add(switchMapSubmenu);

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("SOMViewer ready.");
    }

    /**
     * creates a menu entry for exporting the current visualization
     */
    private void createExportMenu() {
        JMenu exportMenu = new JMenu("Export");
        exportMenu.setMnemonic(KeyEvent.VK_X);

        JMenuItem exportMapPane = new JMenuItem("Export current MapPane ...");
        exportMapPane.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExportUtils.saveMapPaneAsImage(SOMViewer.this, SOMViewer.this.state.getFileChooser(), mapPane,
                        "Save MapPane as PNG");
            }
        });

        JMenuItem exportVisualization = new JMenuItem("Export current Visualization ...");
        exportVisualization.setMnemonic(KeyEvent.VK_X);
        exportVisualization.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File filePath = ExportUtils.getFilePath(SOMViewer.this, SOMViewer.this.state.getFileChooser(),
                        "Save Visualization as PNG");
                if (filePath != null) {
                    try {
                        ExportUtils.saveVisualizationAsImage(SOMViewer.this.state, -1, filePath.getAbsolutePath());
                        JOptionPane.showMessageDialog(SOMViewer.this, "Export to file finished!");
                    } catch (SOMToolboxException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(getParent(), ex.getMessage(), "Error saving",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JMenuItem exportToPocketSOMFormat = new JMenuItem("Export to PocketSOMFormat ...");
        exportToPocketSOMFormat.setMnemonic(KeyEvent.VK_P);
        exportToPocketSOMFormat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File outputFile = ExportUtils.getFilePath(SOMViewer.this, state.fileChooser, "Save in PocketSOMFormat");
                if (outputFile != null) {
                    PocketSOMFormatUtils.convertMapFormat(state.growingLayer, outputFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(SOMViewer.this, "Export to PocketSOMFormat finished!");
                }
            }
        });

        JMenuItem exportRhythmPatterns = new JMenuItem("Export Rhythm Patterns Images ...");
        exportRhythmPatterns.setMnemonic(KeyEvent.VK_R);
        exportRhythmPatterns.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File outputFile = ExportUtils.getFilePath(SOMViewer.this, state.fileChooser,
                        "Export Rhythm Patterns Images");
                if (outputFile != null) {
                    try {
                        ExportUtils.saveRhythmPatternsOfWeightVectors(outputFile.getAbsolutePath(), state.growingLayer);
                        JOptionPane.showMessageDialog(SOMViewer.this, "Export of Rhythm Patterns Images finished!");
                    } catch (SOMToolboxException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(getParent(), ex.getMessage(),
                                "Error exporting Rhythm Patterns Images!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JMenuItem exportVisualizationAdvanced = new JMenuItem("Export as HTML ...");
        exportVisualizationAdvanced.setMnemonic(KeyEvent.VK_X);
        exportVisualizationAdvanced.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ExportDialog(SOMViewer.this, state).setVisible(true);
            }
        });

        JMenuItem exportTuxRacer = new JMenuItem("Export TuxRacer Map...");
        exportTuxRacer.setMnemonic(KeyEvent.VK_T);
        exportTuxRacer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TuxRacerExportDialog(SOMViewer.this, state).setVisible(true);
            }
        });

        // ADDED: SEBASTIAN SKRITEK -
        JMenuItem exportReport = new JMenuItem("Create Report ... ");
        exportReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ReportGenerator(false, state);
            }
        });

        exportMenu.add(exportMapPane);
        exportMenu.add(exportVisualization);
        exportMenu.add(exportVisualizationAdvanced);
        exportMenu.add(exportRhythmPatterns);
        exportMenu.add(exportToPocketSOMFormat);
        exportMenu.add(exportTuxRacer);
        exportMenu.add(exportReport);
        menuBar.add(exportMenu);
    }

    private class CalculateFeatureWeightsActionListener implements ActionListener {
        private FeatureWeightMode mode;

        public CalculateFeatureWeightsActionListener(FeatureWeightMode mode) {
            this.mode = mode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                InputCorrections calculatedCorrections = getMap().getGsom().getLayer().computeUnitFeatureWeights(
                        state.inputDataObjects.getInputCorrections(), state.inputDataObjects.getInputData(), mode);
                // remove potentially existing nodes
                getMap().clearInputCorrections(CreationType.COMPUTED);
                for (InputCorrection correction : calculatedCorrections.getInputCorrections()) {
                    ArrowPNode arrow = ArrowPNode.createInputCorrectionArrow(correction,
                            InputCorrections.CreationType.COMPUTED, getMap().getUnit(correction.getSourceUnit()),
                            getMap().getUnit(correction.getTargetUnit()));
                    getMap().getInputCorrectionsPNode().addChild(arrow);
                    arrow.moveToBack();
                }
            } catch (SOMToolboxException ex) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ex.getMessage());
            }
        }
    }

    private class ClusteringMenuItemActionListener implements ActionListener {
        private TreeBuilder builder;

        public ClusteringMenuItemActionListener(TreeBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SwingWorker worker = new TreeBuildWorker();
            worker.start();
        }

        // builds the tree in a separate thread and shows progress bar
        private class TreeBuildWorker extends SwingWorker {
            private ProgressMonitor monitor;

            public TreeBuildWorker() {
                monitor = new ProgressMonitor(SOMViewer.this, "Building cluster tree...", "", 0, 100); // Maximum will
                // be set later;
            }

            @Override
            public Object construct() {
                if (builder != null) {
                    builder.setMonitor(monitor);
                }
                try {
                    getMap().buildTree(builder);
                    showPalettePanel();
                    redrawClustering();

                    // also show clustering control element
                    clusteringControl.updateControlDisplay();
                    clusteringControl.setVisible(true);

                } catch (ClusteringAbortedException ex) {
                    // reset to old menu item
                    SOMViewer.this.clusterMethodGroup.setSelected(previousSelectedClusteringMethod, true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "Error during Clustering: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                    // reset to old menu item
                    SOMViewer.this.clusterMethodGroup.setSelected(previousSelectedClusteringMethod, true);
                }
                previousSelectedClusteringMethod = SOMViewer.this.clusterMethodGroup.getSelection();
                monitor.close();
                return null;
            }
        }
    }

    // Angela
    private void redrawClustering() {
        // BasicStroke bs = new BasicStroke(12.0f);
        getMap().showClusters(this.clusteringLevel, false);
    }

    private MapPNode getMap() {
        return mapPane.getMap();
    }

    // Angela: creates a menu entry for showing clusters
    private void createClusterMenu() {
        JMenu clusterMenu = new JMenu("Clustering");
        JMenuItem menuItemCPCLustering = new JMenuItem("Component Plane Clustering");
        menuItemCPCLustering.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ComponentPlaneClusteringFrame componentPlaneFrame = new ComponentPlaneClusteringFrame(
                            SOMViewer.this, getMap().getGsom(), state.inputDataObjects.getTemplateVector());
                    componentPlaneFrame.setLocation(state.controlElementsWidth, 0);
                    componentPlaneFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    componentPlaneFrame.pack();
                    componentPlaneFrame.setVisible(true);
                } catch (SOMToolboxException ex) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "Error during Component Plane Clustering: " + ex.getMessage());
                }
            }
        });
        clusterMenu.add(menuItemCPCLustering);

        JMenu menuMapClustering = new JMenu("Map Clustering");
        clusterMenu.add(menuMapClustering);
        clusterMethodGroup = new ButtonGroup();

        JRadioButtonMenuItem menuItem = makeClusteringMenuItem("None", null, menuMapClustering);
        clusterMethodGroup.add(menuItem);
        menuItem.getModel().setSelected(true);
        previousSelectedClusteringMethod = menuItem.getModel();

        clusterMethodGroup.add(makeClusteringMenuItem("Single linkage", new SingleLinkageTreeBuilder(),
                menuMapClustering));
        clusterMethodGroup.add(makeClusteringMenuItem("Complete linkage", new CompleteLinkageTreeBuilder(),
                menuMapClustering));
        clusterMethodGroup.add(makeClusteringMenuItem("Ward's linkage (fast, inexact)", new WardsLinkageTreeBuilder(),
                menuMapClustering));
        clusterMethodGroup.add(makeClusteringMenuItem("Ward's linkage (exact)", new WardsLinkageTreeBuilderAll(),
                menuMapClustering));
        clusterMethodGroup.add(makeClusteringMenuItem("Ward's linkage (exact, experimental)",
                new WardsLinkageTreeBuilderAll(true), menuMapClustering));
        clusterMethodGroup.add(makeClusteringMenuItem("K-means", new KMeansTreeBuilder(), menuMapClustering));

        menuBar.add(clusterMenu);
    }

    private JRadioButtonMenuItem makeClusteringMenuItem(String name, TreeBuilder builder, JMenu menuMapClustering) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(name);
        menuItem.addActionListener(new ClusteringMenuItemActionListener(builder));
        menuMapClustering.add(menuItem);
        return menuItem;
    }

    private AbstractButton makeToolbarButton(String imageName, String toolTipText, String altText) {
        return UiUtils.setToolbarButtonDetails(new JButton(), this, imageName, toolTipText, altText, false);
    }

    private AbstractButton makeToolbarToggleButton(String imageName, String toolTipText, String altText,
            boolean isSelected) {
        return UiUtils.setToolbarButtonDetails(new JToggleButton(), this, imageName, toolTipText, altText, isSelected);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals(CENTER_AND_FIT_MAP)) {
            mapPane.centerAndFitMapToScreen();
        } else if (cmd.equals(RESET_DESKTOP_LAYOUT)) {
            resetDesktopLayout();
        } else if (cmd.equals(SELECT_LINE)) {
            mapPane.setLine();
        } else if (cmd.equals(SELECT_RECTANGLE)) {
            mapPane.setRectangle();
        } else if (cmd.equals(SELECT_CLUSTER)) {
            mapPane.setCluster();
        } else if (cmd.equals(MOVE_INPUT)) {
            // rudi for moving inputs
            mapPane.setInput();
        } else if (cmd.equals(MOVE_LABEL)) {
            // Angela
            mapPane.setLabel();
        } else if (cmd.equals(CREATE_LABEL)) {
            // Angela
            getMap().createLabel();
        } else if (cmd.equals(TOGGLE_DATA)) {
            state.dataVisibilityMode = ((AbstractButton) e.getSource()).isSelected();
            getMap().reInitUnitDetails();
        } else if (cmd.equals(TOGGLE_LABELS)) {
            state.labelVisibilityMode = ((AbstractButton) e.getSource()).isSelected();
            getMap().reInitUnitDetails();
        } else if (cmd.equals(TOGGLE_HITS)) {
            state.hitsVisibilityMode = ((AbstractButton) e.getSource()).isSelected();
            getMap().reInitUnitDetails();
        } else if (at.tuwien.ifs.somtoolbox.util.StringUtils.equalsAny(cmd, TOGGLE_PIE_CHARTS_MODES)) {
            state.setClassPiechartMode(cmd);
            classLegendPane.setEnabled(cmd != TOGGLE_PIE_CHARTS_NONE);
            mapPane.updateClassSelection(null);
            buttonPie.setIcon(TOGGLE_PIE_CHARTS_ICONS[ArrayUtils.indexOf(TOGGLE_PIE_CHARTS_MODES, cmd)]);
            // buttonPie.setSelected(false);
        } else if (cmd.equals(TOGGLE_EXACT_PLACEMENT)) {
            state.exactUnitPlacement = ((AbstractButton) e.getSource()).isSelected();
            getMap().reInitUnitDetails();
        } else if (cmd.equals(TOGGLE_RELOCATE)) {
            state.shiftOverlappingInputs = ((AbstractButton) e.getSource()).isSelected();
            getMap().updatePointLocations();
        } else if (cmd.equals(TOGGLE_LINKAGE)) {
            state.displayInputLinkage = ((AbstractButton) e.getSource()).isSelected();
            state.mapPNode.setLinkageVisibilityMode(state.displayInputLinkage);
        } else if (cmd.equals(SOMVIEWER_3D)) {
            Constructor<JInternalFrame> constr = null;
            Object viewerState3D = null;
            try {
                // first do class-loading, to see if we have the optional components available
                // load & instantiate state object of 3D
                Class<?> classViewerState3D = Class.forName("at.tuwien.ifs.somtoolbox.apps.viewer3d.CommonSOMViewerStateData3D");
                Constructor<?> constrViewerState3D = classViewerState3D.getConstructor(CommonSOMViewerStateData.class);
                viewerState3D = constrViewerState3D.newInstance(state);
                // load and instantiate 3D Viewer itself
                @SuppressWarnings("unchecked")
                Class<JInternalFrame> c = (Class<JInternalFrame>) Class.forName("at.tuwien.ifs.somtoolbox.apps.viewer3d.SOMViewer3D");
                constr = c.getConstructor(classViewerState3D);
            } catch (Exception e1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "SOMViever3D not available. To enable add the required classes to your classpath");
                return;
            }
            try {
                // now actually instantiate it
                JInternalFrame somviewer3D = constr.newInstance(new Object[] { viewerState3D });
                // desktop.add(somviewer3D);
                somviewer3D.pack();
                somviewer3D.setLocation(state.controlElementsWidth, 0);
                somviewer3D.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            } catch (Exception e1) {
                if (e1 instanceof SOMToolboxException) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e1.getMessage());
                } else {
                    if (e1 instanceof InvocationTargetException
                            && ((InvocationTargetException) e1).getTargetException() instanceof SOMToolboxException) {
                        // happens when the SOMToolboxException is thrown in SOMViewer3D#init
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                ((InvocationTargetException) e1).getTargetException().getMessage());
                    } else {
                        e1.printStackTrace();
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                "Error loading 3D Viewer: " + e1.getMessage());
                    }
                }
            }

        }
    }

    public static void main(String[] args) {
        // register and parse all options
        JSAPResult config = AbstractOptionFactory.parseResults(args, OPTIONS);
        new SOMViewer(config);
    }

    public void addVisualizationChangeListener(VisualizationChangeListener l) {
        visChangeListeners.add(l);
    }

    public void removeVisualizationChangeListener(VisualizationChangeListener l) {
        visChangeListeners.remove(l);
    }

    private void visualizationChangeFailure() {
        visualizationMenuItemGroup.setSelected(oldSelectedVisualizationMenuItem, true);
    }

    private void visualizationChangeSuccess() {
        oldSelectedVisualizationMenuItem = visualizationMenuItemGroup.getSelection();
        for (VisualizationChangeListener listener : visChangeListeners) {
            listener.visualisationChanged();
        }
    }

    private class VisualizationActionListener implements ActionListener {
        private int visualization;

        private int variant;

        public VisualizationActionListener(int vi, int va) {
            super();
            visualization = vi;
            variant = va;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final SwingWorker worker = new SwingWorker() {
                @Override
                public Object construct() {
                    try {
                        state.currentVariant = variant;
                        boolean success = mapPane.setVisualization(visualization, variant);
                        state.currentVariant = variant;
                        if (success) {
                            visControlPanel.updateVisualisationControl();
                            SOMViewer.this.visualizationChangeSuccess();
                            updatePalettePanel();
                            mapPane.repaint();
                        } else {
                            SOMViewer.this.visualizationChangeFailure();
                        }
                    } catch (SOMToolboxException e) {
                        JOptionPane.showMessageDialog(SOMViewer.this, e.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
            };
            worker.start(); // required for SwingWorker 3
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void update(Observable o, Object arg) {
        menuPie.setEnabled(false);
        if (arg instanceof SOMLibClassInformation && arg != null) { // we loaded a new class info
            getMap().updateClassInfo((SOMLibClassInformation) arg);
            classLegendPane.initClassTable();
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridwidth = GridBagConstraints.REMAINDER;
            menuPie.setEnabled(true);
            ThematicClassMapVisualizer tcmVis = getMap().getThematicClassMapVisualizer();
            if (tcmVis != null) {
                thematicClassRadioButton.setEnabled(true);
            }

            if (mapPane.getSecondMap() != null) {
                mapPane.getSecondMap().updateClassInfo((SOMLibClassInformation) arg);
                tcmVis = mapPane.getSecondMap().getThematicClassMapVisualizer();
                if (tcmVis != null) {
                    thematicClassRadioButton.setEnabled(true);
                }
            }
        } else {
            state.setClassPiechartMode(TOGGLE_PIE_CHARTS_NONE);
            getMap().updateClassInfo((SOMLibClassInformation) arg);
            if (mapPane.getSecondMap() != null) {
                mapPane.getSecondMap().updateClassInfo((SOMLibClassInformation) arg);
            }
            mapPane.updateClassSelection(null);
            classLegendPane.initNoClassInfo();
            thematicClassRadioButton.setEnabled(false);
        }
    }

    public void updateSOMComparison(boolean haveData) {

        boolean error = false;

        try {
            mapPane.updateSOMComparison();
        } catch (SOMToolboxException e1) {
            error = true;
        }

        if (haveData && !error) {
            shiftsControlPanel.initGUIElements();
            showShiftsMenuItem.setEnabled(true);
            switchMapSubmenu.setEnabled(true);
        } else {
            shiftsControlPanel.initNoShiftsInfo();
            showShiftsMenuItem.setEnabled(false);
            switchMapSubmenu.setEnabled(false);
        }
    }

    public void updatePaletteAfterEditing() {
        try {
            boolean success = getMap().reloadPaletteAfterEditing(getCurrentlySelectedPalette());
            if (success) {
                SOMViewer.this.visualizationChangeSuccess();
            } else {
                SOMViewer.this.visualizationChangeFailure();
            }
            showPalettePanel();
        } catch (SOMToolboxException e) {
            JOptionPane.showMessageDialog(SOMViewer.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Palette getCurrentlySelectedPalette() {
        return Palettes.getPaletteByName(paletteMenuItemGroup.getSelection().getActionCommand());
    }

    private class MyJCheckBoxMenuItem extends JCheckBoxMenuItem implements ActionListener {
        private static final long serialVersionUID = 1L;

        private Component component;

        public MyJCheckBoxMenuItem(String name, Component component) {
            super(name);
            this.component = component;
            setSelected(component.isVisible());
            setMnemonic(name.charAt(0));
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            component.setVisible(isSelected());
        }

        @Override
        public Component getComponent() {
            return component;
        }
    }

    private class PaletteCheckboxMenuItemListener implements ActionListener {
        private Palette palette;

        public PaletteCheckboxMenuItemListener(Palette palette) {
            this.palette = palette;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                boolean success = getMap().changePalette(palette);
                if (success) {
                    SOMViewer.this.visualizationChangeSuccess();
                } else {
                    SOMViewer.this.visualizationChangeFailure();
                }
                // Angela: moved some code to showPalettePanel (clustering needs panel too)
                showPalettePanel();

            } catch (SOMToolboxException ex) {
                JOptionPane.showMessageDialog(SOMViewer.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void initDocViewer(AbstractSelectionPanel sp) {
        /** ** DocViewPanel *** */
        DocViewPanel docviewer = new DocViewPanel();
        docviewer.setDocumentPath(CommonSOMViewerStateData.fileNamePrefix);
        docviewer.setDocumentSuffix(CommonSOMViewerStateData.fileNameSuffix);
        sp.setItemListener(docviewer);

        docViewerFrame = new JFrame("DocViewer") {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(450, 750);
            }
        };
        // This does not work in Java 1.4
        // FIXME: find another solution in 1.4
        // docViewerFrame.setAlwaysOnTop(true);

        docViewerFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        docViewerFrame.getContentPane().add(docviewer, BorderLayout.CENTER);
        docViewerFrame.pack();
        docViewerFrame.setVisible(true);
    }

    /**
     * handles the window closing to dispose of a docviewer frame, if present, and not to do EXIT on close, but dispose.<br>
     * If running standalone, the JVM will exit automatically after disposing the last frame, but if called from another
     * application, this will only dispose this window, not exit the JVM.
     */
    private void initWindowClosing() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // DISPOSE_ON_CLOSE); //EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            /**
             * maybe there are some other windows we want to close too?
             */
            @Override
            public void windowClosing(WindowEvent e) {
                if (!documentMode) {
                    System.exit(0);
                }

                System.out.println("closing");
                if (docViewerFrame != null) {
                    docViewerFrame.setVisible(false);
                    docViewerFrame.dispose();
                }
                setVisible(false);
                dispose();
            }
        });
    }

    public Color[] getClassLegendColors() {
        return classLegendPane.getColors();
    }

    // Angela: shows or hides the palette panel
    private void showPalettePanel() {
        BackgroundImageVisualizer currentVisualization = mapPane.getCurrentVisualization();
        if (currentVisualization != null || state.colorClusters) {
            if (currentVisualization instanceof AbstractMatrixVisualizer) {
                AbstractMatrixVisualizer vis = (AbstractMatrixVisualizer) currentVisualization;
                palettePanel.setPalette(vis.getCurrentPalette().getColors(), vis.getMinimumMatrixValue(),
                        vis.getMaximumMatrixValue());
            } else {
                palettePanel.setPalette(getCurrentlySelectedPalette().getColors());
            }
        } else {
            palettePanel.setPalette(null);
        }
        mapPane.repaint();
    }

    private void updatePalettePanel() {
        BackgroundImageVisualizer curVis = mapPane.getCurrentVisualization();
        if (curVis != null) {
            if (curVis instanceof AbstractMatrixVisualizer) {
                AbstractMatrixVisualizer matrixVis = (AbstractMatrixVisualizer) curVis;
                if (matrixVis.getPalette() != null) {
                    reversePaletteMenuItem.setSelected(matrixVis.getCurrentPalette().isReversed());
                    palettePanel.setPalette(matrixVis.getPalette(), matrixVis.getMinimumMatrixValue(),
                            matrixVis.getMaximumMatrixValue());
                }
            }
        } else {
            palettePanel.setPalette(null);
        }
    }

    public String getUnitDescriptionFileName() {
        return unitDescriptionFileName;
    }

    public String getWeightVectorFileName() {
        return weightVectorFileName;
    }

    public String getMapDescriptionFileName() {
        return mapDescriptionFileName;
    }

    public CommonSOMViewerStateData getSOMViewerState() {
        return state;
    }

}
