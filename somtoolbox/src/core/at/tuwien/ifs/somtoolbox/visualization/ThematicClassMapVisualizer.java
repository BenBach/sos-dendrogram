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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.util.PaintList;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import flanagan.interpolation.BiCubicSplineFast;

import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.ProgressListener;
import at.tuwien.ifs.somtoolbox.util.ProgressListenerFactory;
import at.tuwien.ifs.somtoolbox.util.VectorTools;
import at.tuwien.ifs.somtoolbox.visualization.thematicmap.RegionManager;

/**
 * This visualisation colours the map according to the distribution of classes assigned to the data items. Helper
 * methods are to be found in the package {@link at.tuwien.ifs.somtoolbox.visualization.thematicmap}.
 * 
 * @author Taha Abdel Aziz
 * @author Florian Guggenberger
 * @author Ewald Peiszer
 * @author Andrei Grecu
 * @author Rudolf Mayer
 * @version $Id: ThematicClassMapVisualizer.java 3883 2010-11-02 17:13:23Z frank $
 */
public class ThematicClassMapVisualizer extends AbstractBackgroundImageVisualizer implements BackgroundImageVisualizer {
    public static final String CLASSMAP_SHORT_NAME = "ClassMap";

    private static final int METHOD_ALPHA = 1;

    private static final int METHOD_BLACK = 3;

    private static final int METHOD_WHITE = 2;

    private static final int METHOD_NO_COMBINATION = 0;

    private static final int METHOD_HSV_MODEL = 5;

    private int zoom = MapPNode.DEFAULT_UNIT_WIDTH / preferredScaleFactor;

    double min_visible_class = 0;

    double gamma = 1;

    double contrast = 1;

    double gain = 1;

    double method = 0;

    boolean voronoi = true;

    boolean chessBoard = false;

    protected Hashtable<String, RegionManager> regionCache = new Hashtable<String, RegionManager>();

    private double minimumMatrixValue = 255;

    private double maximumMatrixValue = -1;

    private double HSVRANGE = 255;

    public ThematicClassMapVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Thematic Class Map" };
        VISUALIZATION_SHORT_NAMES = new String[] { CLASSMAP_SHORT_NAME };
        VISUALIZATION_DESCRIPTIONS = new String[] { "This visualisation colours the map thematically by the class membership of the units. This visualisation is only available if a class information file is loaded.\n"
                + "Implementation as described in \"R. Mayer, A. A. Taha, and A. Rauber. Visualising Class Distribution on Self-Organising Maps.\n"
                + "Proceedings of the International Conference on Artificial Neural Networks (ICANN'07),\n"
                + "pp 359-368, LNCS 4669, Porto, Portugal, September 9-13, 2007, Springer Verlag.\"" };
        neededInputObjects = new String[] { SOMVisualisationData.CLASS_INFO };

        // don't initialise the control panel if we have no graphics environment (e.g. in server applications)
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                controlPanel = new ClassVisControlPanel(this);
            } catch (Throwable e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Caught runtime exception/error during graphics init: " + e.getMessage()
                                + "\n Headless environment? " + GraphicsEnvironment.isHeadless());
            }
        }
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int currentVariant, int width, int height) {
        return super.getCacheKey(gsom, currentVariant, width, height) + CACHE_KEY_SECTION_SEPARATOR
                + buildCacheKey("voronoi:" + voronoi, "chess:" + chessBoard, "minClass:" + min_visible_class)
                + CACHE_KEY_SECTION_SEPARATOR
                + buildCacheKey("weight:" + method, "gamma:" + gamma, "contrast:" + contrast, "gain:" + gain);
    }

    public void setInitialParams(boolean chessBoard, boolean voronoi, double minVisibleClass) {
        ClassVisControlPanel classVisControlPanel = (ClassVisControlPanel) controlPanel;
        classVisControlPanel.minSizeSpinner.setValue(new Double(minVisibleClass * 100));
        classVisControlPanel.chessBoardCheckbox.setSelected(chessBoard);
        classVisControlPanel.voronoiCheckbox.setSelected(voronoi);
        update(minVisibleClass, 0, 0, 0, METHOD_NO_COMBINATION, voronoi, chessBoard);
    }

    private int clamp(int a, int min, int max) {
        if (a < min) {
            return min;
        }
        if (a > max) {
            return max;
        }
        return a;
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        return createVisualization(index, gsom, width, height, -1);
    }

    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height, int ClassID)
            throws SOMToolboxException {
        checkNeededObjectsAvailable(gsom);

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = res.createGraphics();

        drawBackground(width, height, g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);

        // DistanceMetric metric = gsom.getLayer().getMetric();
        int xSize = gsom.getLayer().getXSize();
        int ySize = gsom.getLayer().getYSize();

        // key for the cache, need to consider width and height as region sizes depend on this
        // this is specifically needed for the server-side application
        String key = min_visible_class + "_" + width + "_" + height;

        if (regionCache.get(key) == null) {
            try {
                PaintList paintList = inputObjects.getClassInfo().getPaintList();
                // System.out.println("MapPNode.DEFAULT_UNIT_WIDTH: " + MapPNode.DEFAULT_UNIT_WIDTH);
                // System.out.println("preferredScaleFactor: " + preferredScaleFactor);
                // System.out.println("zoom: " + zoom);
                RegionManager regionManager = new RegionManager(inputObjects.getClassInfo(), paintList, width, height,
                        min_visible_class, zoom);
                ProgressListener progressWriter = ProgressListenerFactory.getInstance().createProgressListener(
                        gsom.getLayer().getNumberOfNotEmptyUnits(), "Creating region ", 10);

                // int h = 0;
                for (int j = 0; j < ySize; j++) {
                    for (int i = 0; i < xSize; i++) {
                        Unit unit = gsom.getLayer().getUnit(i, j);
                        if (unit != null && unit.getNumberOfMappedInputs() != 0) {
                            // SOMRegion r = regionManager.addNewRegion(unit);
                            regionManager.addNewRegion(unit);
                            progressWriter.progress();
                        }
                    }
                }
                regionManager.build();
                regionCache.put(key, regionManager);
            } catch (LayerAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
        RegionManager regionManager = regionCache.get(key);
        regionManager.resetResolvingState();
        if (ClassID != -1) { // special mode for showing only one class if
            regionManager.fillSingleRegion(g, chessBoard, ClassID);
        } else {
            regionManager.fillRegions(g, chessBoard);
        }
        if (voronoi) {
            regionManager.drawRegions(g);
        }

        // If method != 0 (ie no combination)
        if (method != METHOD_NO_COMBINATION) {
            applyColourWeighting(gsom, width, height, res, g);
        }

        return res;
    }

    private void applyColourWeighting(GrowingSOM gsom, int width, int height, BufferedImage res, Graphics2D g)
            throws SOMToolboxException {
        // hack epei
        UMatrix um = new UMatrix();

        // we need the grayscale 256 palette for some of the modifcations
        Palette grayscale256 = Palettes.getPaletteByName("Grayscale256");
        if (grayscale256 == null) {
            throw new SOMToolboxException("Palette 'Grayscale256' is needed for the U-Matrix modifications!");
        }
        um.setPalette(grayscale256);

        BufferedImage umbi; // = um.createVisualization(0, gsom, width, height);

        DoubleMatrix2D umm = um.createUMatrix(gsom);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").finer("MW(UM) = " + umm.zSum() / (umm.rows() * umm.columns()));
        VectorTools.normalise(umm);

        double mw = umm.zSum() / (umm.rows() * umm.columns());
        Logger.getLogger("at.tuwien.ifs.somtoolbox").finer("norm-MW(UM) = " + mw);

        double temp_um;

        for (int r = 0; r < umm.rows(); r++) {
            for (int c = 0; c < umm.columns(); c++) {
                temp_um = ((Math.pow(umm.get(r, c) - mw + 0.5, gamma) - 0.5) * contrast + 0.5) * gain;
                if (temp_um < 0) {
                    temp_um = 0;
                }
                if (temp_um > 1) {
                    temp_um = 1;
                }
                umm.set(r, c, temp_um);
            }
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").finer(
                "f-norm-MW(UM) = " + umm.zSum() / (umm.rows() * umm.columns()));

        umbi = um.createImage(gsom, umm, width, height, interpolate);

        double umatrix[][] = getUmValues(gsom, umm, width, height);
        double rangedistance = maximumMatrixValue - minimumMatrixValue;

        if (method == METHOD_ALPHA) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g.drawImage(umbi, null, 0, 0);
        } else {
            Color c_CC, c_UM = null, c_DEST;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    c_CC = new Color(res.getRGB(x, y));
                    c_UM = new Color(umbi.getRGB(x, y));

                    if (method == METHOD_WHITE) {
                        // mit weiss mischen
                        c_DEST = new Color(c_CC.getRed() * (255 - c_UM.getRed()) / 255 + c_UM.getRed(), c_CC.getGreen()
                                * (255 - c_UM.getGreen()) / 255 + c_UM.getGreen(), c_CC.getBlue()
                                * (255 - c_UM.getBlue()) / 255 + c_UM.getBlue());
                    } else if (method == METHOD_BLACK) {
                        // mit schwarz mischen
                        c_DEST = new Color(c_CC.getRed() * (255 - c_UM.getRed()) / 255, c_CC.getGreen()
                                * (255 - c_UM.getGreen()) / 255, c_CC.getBlue() * (255 - c_UM.getBlue()) / 255);
                    } else if (method == METHOD_HSV_MODEL) {
                        float[] HSV = new float[3];
                        HSV = Color.RGBtoHSB(c_CC.getRed(), c_CC.getGreen(), c_CC.getBlue(), HSV);
                        /** Range mapping * */
                        double percent4range = 100 * (umatrix[x][y] - minimumMatrixValue) / rangedistance;
                        double Saturation = HSVRANGE * percent4range / 100;

                        /** set saturation value * */
                        HSV[1] = (float) Saturation / 255;
                        c_DEST = new Color(Color.HSBtoRGB(HSV[0], HSV[1], HSV[2]));
                    } else {
                        // Multiplication
                        c_DEST = new Color(clamp(Math.round(c_CC.getRed() * (1 + (float) c_UM.getRed() / (float) 255)
                                / 2), 0, 255), clamp(Math.round(c_CC.getGreen()
                                * (1 + (float) c_UM.getGreen() / (float) 255) / 2), 0, 255), clamp(
                                Math.round(c_CC.getBlue() * (1 + (float) c_UM.getBlue() / (float) 255) / 2), 0, 255));

                    }
                    res.setRGB(x, y, c_DEST.getRGB());
                }
            }

            // regionManager.drawDelaunayTrangulation(g);
        }
    }

    public void update(double val, double gamma, double contrast, double gain, int method, boolean voronoi,
            boolean chessBoard) {
        this.min_visible_class = val;
        this.gain = gain;
        this.gamma = gamma;
        this.contrast = contrast;
        this.method = method;
        this.voronoi = voronoi;
        this.chessBoard = chessBoard;
        if (visualizationUpdateListener != null) {
            visualizationUpdateListener.updateVisualization();
        }
    }

    class ClassVisControlPanel extends VisualizationControlPanel implements ChangeListener, ActionListener {
        private static final String TOOL_TIP = "<html>" //
                + "<h3>Colour Weighting using the U-Matrix</h3>" //
                + "<p><b>Computation:</b></p>" //
                + "<ol>"
                + "<li>Simple Alpha Blending: draw UM onto TCM using"
                + "<pre>g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));"
                + "g.drawImage(UMatrix_BufferedImage, null, 0, 0);</pre>"
                + "<li>Alpha Blending to white: DEST = TMC * (255 - UM) / 255 + UM"
                + "<li>Alpha Blending to black: DEST = TMC * (255 - UM) / 255"
                + "<li>Multiplication: DEST = TMC * (1 + UM / 255) / 2"
                + "</ol>"
                + "<p>Gamma, contrast and gain are used to re-calculate the UMatrix using</p>"
                + "<pre>UM = ((pow(UM - E(UM) + 0.5, gamma) - 0.5) * contrast + 0.5) * gain</pre>"
                + "<p><br>Notes: <br>" + "TMC ... Thematic Class Map<br>"
                + "UM ... UMatrix<br>"
                + "E(x) ...expectation</p>" + //
                "</html>";

        private static final long serialVersionUID = 1L;

        private ThematicClassMapVisualizer classvisualizer;

        private javax.swing.JLabel minVisClassLabel;

        private javax.swing.JLabel gammaLabel;

        private javax.swing.JLabel contrastLabel;

        private javax.swing.JLabel gainLabel;

        private javax.swing.JCheckBox chessBoardCheckbox;

        private javax.swing.JCheckBox voronoiCheckbox;

        private javax.swing.JComboBox combinationUM;

        private JSpinner minSizeSpinner;

        private JSpinner gammaSpinner;

        private JSpinner contrastSpinner;

        private JSpinner gainSpinner;

        /** Creates new form ClassVisControlPanel */
        public ClassVisControlPanel(at.tuwien.ifs.somtoolbox.visualization.ThematicClassMapVisualizer classvisualizer) {
            super("Class Map Control");
            this.classvisualizer = classvisualizer;
            initComponents();
        }

        public final String[] uMatrixWeightingOptions = new String[] { "No U-Matrix weighting",
                "Simple Alpha Blending", "Alpha Blending to white", "Alpha Blending to black", "Multiplication",
                "HSV Colouring" };

        /**
         * This method is called from within the constructor to initialize the form.
         */
        private void initComponents() {
            JPanel classVisPanel = new JPanel();
            GridBagConstraintsIFS constr = new GridBagConstraintsIFS(GridBagConstraintsIFS.NORTHWEST,
                    GridBagConstraintsIFS.HORIZONTAL);
            constr.setInsets(2, 1);

            classVisPanel.setLayout(new GridBagLayout());

            voronoiCheckbox = new JCheckBox("Vorono lines");
            voronoiCheckbox.setSelected(true);
            voronoiCheckbox.setMnemonic(KeyEvent.VK_V);
            voronoiCheckbox.addActionListener(this);
            classVisPanel.add(voronoiCheckbox, constr);

            chessBoardCheckbox = new JCheckBox("Chessboard");
            chessBoardCheckbox.setMnemonic(KeyEvent.VK_C);
            chessBoardCheckbox.addActionListener(this);
            classVisPanel.add(chessBoardCheckbox, constr.nextCol());

            minVisClassLabel = new javax.swing.JLabel("Min. visible %");
            minVisClassLabel.setToolTipText("Only classes which contribute at least the given percentage to a map node are considered");
            classVisPanel.add(minVisClassLabel, constr.nextRow());

            minSizeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 5.0));
            minSizeSpinner.addChangeListener(this);
            classVisPanel.add(minSizeSpinner, constr.nextCol());

            TitledCollapsiblePanel colourWeightingPanel = new TitledCollapsiblePanel("Colour Weighting",
                    new GridBagLayout(), true);
            constr.resetWeights().reset();

            combinationUM = new javax.swing.JComboBox(uMatrixWeightingOptions);
            combinationUM.setFont(smallerFont);
            combinationUM.addActionListener(this);
            colourWeightingPanel.add(combinationUM, constr.setGridWidth(2).setWeightX(1.0));

            // help icon with tool tip
            JLabel helpLabel = new JLabel(new ImageIcon(ClassLoader.getSystemResource("rsc/icons/help.png")));
            helpLabel.setFont(smallerFont);
            helpLabel.setToolTipText(TOOL_TIP);
            // Tooltip should stay visible since it's such a long one...
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
            colourWeightingPanel.add(helpLabel, constr.nextCol().nextCol().setGridWidth(1).resetWeights());

            gammaLabel = new javax.swing.JLabel("Gamma: ");
            colourWeightingPanel.add(gammaLabel, constr.nextRow());
            gammaSpinner = new JSpinner(new SpinnerNumberModel(1, 0.1, 9.0, 0.05));
            gammaSpinner.setFont(smallerFont);
            gammaSpinner.addChangeListener(this);
            colourWeightingPanel.add(gammaSpinner, constr.nextCol().setWeightX(1.0));

            contrastLabel = new javax.swing.JLabel("Contrast: ");
            colourWeightingPanel.add(contrastLabel, constr.resetWeights().nextRow());
            contrastSpinner = new JSpinner(new SpinnerNumberModel(1, 0.1, 10, 0.05));
            contrastSpinner.setFont(smallerFont);
            contrastSpinner.addChangeListener(this);
            colourWeightingPanel.add(contrastSpinner, constr.setWeightX(1.0).nextCol());

            gainLabel = new javax.swing.JLabel("Gain: ");
            colourWeightingPanel.add(gainLabel, constr.resetWeights().nextRow());
            gainSpinner = new JSpinner(new SpinnerNumberModel(1, 0.1, 10, 0.05));
            gainSpinner.setFont(smallerFont);
            gainSpinner.addChangeListener(this);
            colourWeightingPanel.add(gainSpinner, constr.setWeightX(1.0).nextCol());

            add(classVisPanel, c);
            c.gridy += 1;
            add(colourWeightingPanel, c);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JComponent s = (JComponent) e.getSource();

            if (s == voronoiCheckbox || s == chessBoardCheckbox) {
                JCheckBox checkBox = (JCheckBox) s;
                ChangeListener changeListener = checkBox.getChangeListeners()[0];
                checkBox.removeChangeListener(changeListener);
                regenerateVisualisation();
                checkBox.addChangeListener(changeListener);
            } else if (s == minSizeSpinner // only if we have umatrix weighting --> need to update
                    || (s == gammaSpinner || s == contrastSpinner || s == gainSpinner)
                    && combinationUM.getSelectedIndex() > 0) {
                JSpinner spinner = (JSpinner) s;
                ChangeListener changeListener = spinner.getChangeListeners()[0];
                spinner.removeChangeListener(changeListener);
                regenerateVisualisation();
                spinner.addChangeListener(changeListener);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            regenerateVisualisation();
        }

        private void regenerateVisualisation() {
            double val = ((Double) minSizeSpinner.getValue()).doubleValue() / 100;
            double gamma = ((Double) gammaSpinner.getValue()).doubleValue();
            double gain = ((Double) gainSpinner.getValue()).doubleValue();
            double contrast = ((Double) contrastSpinner.getValue()).doubleValue();
            int method = combinationUM.getSelectedIndex();
            classvisualizer.update(val, gamma, contrast, gain, method, voronoiCheckbox.isSelected(),
                    chessBoardCheckbox.isSelected());
        }
    }

    public boolean hasClassInfo() {
        return inputObjects.getClassInfo() != null;
    }

    /**
     * Adapted from {@link AbstractMatrixVisualizer#createImage(GrowingSOM, DoubleMatrix2D, int, int, boolean)}.<br>
     * FIXME: check if the two methods can be merged. *
     */
    private double[][] getUmValues(GrowingSOM gsom, DoubleMatrix2D matrix, int width, int height) {

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        DoubleMatrix2D matrixBorders = new DenseDoubleMatrix2D(matrix.rows() + 2, matrix.columns() + 2);
        matrixBorders.viewPart(1, 1, matrix.rows(), matrix.columns()).assign(matrix);
        matrixBorders.viewRow(0).assign(matrixBorders.viewRow(1));
        matrixBorders.viewRow(matrixBorders.rows() - 1).assign(matrixBorders.viewRow(matrixBorders.rows() - 2));
        matrixBorders.viewColumn(0).assign(matrixBorders.viewColumn(1));
        matrixBorders.viewColumn(matrixBorders.columns() - 1).assign(
                matrixBorders.viewColumn(matrixBorders.columns() - 2));

        /** start bicubic spline stuff * */
        // create support points
        double factorX = (double) (matrixBorders.columns() - 2) / (double) gsom.getLayer().getXSize();
        double factorY = (double) (matrixBorders.rows() - 2) / (double) gsom.getLayer().getYSize();
        double[] x1 = new double[matrixBorders.columns()];
        x1[0] = 0;
        for (int x = 0; x < matrixBorders.columns() - 2; x++) {
            x1[x + 1] = x * unitWidth / factorX + unitWidth / (2 * factorX);
        }
        x1[matrixBorders.columns() - 1] = width;
        double[] x2 = new double[matrixBorders.rows()];
        x2[0] = 0;
        for (int y = 0; y < matrixBorders.rows() - 2; y++) {
            x2[y + 1] = y * unitHeight / factorY + unitHeight / (2 * factorY);
        }
        x2[matrixBorders.rows() - 1] = height;

        BiCubicSplineFast bcs = new BiCubicSplineFast(x2, x1, matrixBorders.toArray());
        // bcs.calcDeriv();

        int ci = 0;
        double umatrix[][] = new double[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // adapted to mnemonic (sparse) SOMs
                try {
                    if (gsom.getLayer().getUnit((x / unitWidth), (y / unitHeight)) != null) {
                        ci = (int) Math.round(bcs.interpolate(y, x) * (HSVRANGE - 1));
                        if (ci < 0) {
                            ci = 0;
                        } else if (ci >= HSVRANGE) {
                            ci = (int) HSVRANGE;
                        }
                    }
                } catch (LayerAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (ci < minimumMatrixValue) {
                    minimumMatrixValue = ci;
                }
                if (ci > maximumMatrixValue) {
                    maximumMatrixValue = ci;
                }
                umatrix[x][y] = ci;
            }

        }
        /** end bicubic spline stuff * */
        return umatrix;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    /** Deletes all cached elements from this visualisation. */
    public void invalidateCache() {
        for (String visualizationName : VISUALIZATION_SHORT_NAMES) {
            invalidateCache(visualizationName);
        }
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width,
            int height) throws SOMToolboxException {
        HashMap<String, BufferedImage> res = new HashMap<String, BufferedImage>();
        // if (needsAdditionalFiles() != null) return res;

        boolean v = voronoi;
        boolean ch = chessBoard;
        double vc = min_visible_class;

        for (int i = 5; i <= 100;) {
            String k = String.format("_class%dp", i);

            // T T
            update(i / 100d, 0, 0, 0, METHOD_NO_COMBINATION, true, true);
            res.put(k + "-voronoi-chessBoard", getVisualization(variantIndex, gsom, width, height));
            // T F
            update(i / 100d, 0, 0, 0, METHOD_NO_COMBINATION, true, false);
            res.put(k + "-voronoi", getVisualization(variantIndex, gsom, width, height));
            // F T
            update(i / 100d, 0, 0, 0, METHOD_NO_COMBINATION, false, true);
            res.put(k + "-chessBoard", getVisualization(variantIndex, gsom, width, height));
            // F F
            update(i / 100d, 0, 0, 0, METHOD_NO_COMBINATION, false, false);
            res.put(k, getVisualization(variantIndex, gsom, width, height));

            if (i < 60) {
                i += 5;
            } else {
                i += 10;
            }
        }

        update(vc, 0, 0, 0, METHOD_NO_COMBINATION, v, ch);

        return res;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width,
            int height, int maxFlavours) throws SOMToolboxException {
        Logger.getLogger(this.getClass().getName()).warning("Not implemented, creating all flavours");
        return getVisualizationFlavours(variantIndex, gsom, width, height);
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width,
            int height, Map<String, String> flavourParameters) throws SOMToolboxException {
        Logger.getLogger(this.getClass().getName()).warning("Not implemented, creating all flavours");
        return getVisualizationFlavours(variantIndex, gsom, width, height);
    }

}
