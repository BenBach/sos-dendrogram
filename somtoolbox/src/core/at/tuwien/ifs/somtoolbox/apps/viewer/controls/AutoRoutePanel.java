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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.nodes.PLine;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Jakob Frank
 * @version $Id: AutoRoutePanel.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class AutoRoutePanel extends AbstractViewerControl {

    private static final long serialVersionUID = 1L;

    private float bigD;

    private float smallD;

    private PNode route;

    private PNode dots;

    private PNode lines;

    private SpinnerNumberModel spinnerModel;

    private JRadioButton rdoFlat;

    private JRadioButton rdoSnap;

    private JRadioButton rdoHigh;

    private JPanel mainP = null;

    private JCheckBox chkTSP;

    private JCheckBox chkDebug;

    private boolean debug = false;

    private boolean applyTSP = false;

    private Random rand;

    private LinkedList<Unit> touchedUnits;

    private static Logger log = Logger.getLogger("at.tuwien.ifs.somtoolbox.AutoRoute");

    private SpinnerNumberModel lineModel;

    public AutoRoutePanel(String title, CommonSOMViewerStateData state) {
        super(title, state);
        bigD = state.mapPNode.getUnitWidth() / 3;
        smallD = state.mapPNode.getUnitHeight() / 6;

        setContentPane(getAutoRoutePanel());
    }

    public JPanel getAutoRoutePanel() {
        if (mainP != null) {
            return mainP;
        }
        mainP = new JPanel();
        mainP.setLayout(new GridBagLayout());
        setContentPane(mainP);

        route = new PNode();
        dots = new PNode();
        lines = new PNode();
        route.addChild(lines);
        route.addChild(dots);

        state.mapPNode.addChild(route);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        JLabel lblSteps = new JLabel("Steps: ");
        spinnerModel = new SpinnerNumberModel(0, 0, 100, 1);
        spinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                doAutoRouting();
            }
        });
        JSpinner spnSteps = new JSpinner(spinnerModel);
        spnSteps.setEditor(new JSpinner.NumberEditor(spnSteps, "#"));
        lblSteps.setLabelFor(spnSteps);
        mainP.add(lblSteps, gbc);
        mainP.add(spnSteps, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;

        ButtonGroup calc = new ButtonGroup();
        rdoFlat = new JRadioButton("flat");
        rdoFlat.setSelected(true);
        rdoFlat.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                doAutoRouting();
            }
        });
        calc.add(rdoFlat);
        mainP.add(rdoFlat, gbc);
        gbc.gridy++;

        rdoSnap = new JRadioButton("snapped");
        rdoSnap.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                doAutoRouting();
            }
        });
        calc.add(rdoSnap);
        mainP.add(rdoSnap, gbc);
        gbc.gridy++;

        rdoHigh = new JRadioButton("highdimensional");
        rdoHigh.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                doAutoRouting();
            }
        });
        calc.add(rdoHigh);
        mainP.add(rdoHigh, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;

        chkTSP = new JCheckBox("TSP");
        chkTSP.setSelected(false);
        chkTSP.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                applyTSP = chkTSP.isSelected();
                // FIXME: if it works, do autorouting here!
                // doAutoRouting();
            }
        });
        mainP.add(chkTSP, gbc);

        chkDebug = new JCheckBox("debug");
        chkDebug.setSelected(debug);
        chkDebug.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                debug = chkDebug.isSelected();
                doAutoRouting();
            }
        });
        mainP.add(chkDebug, gbc);
        gbc.gridy++;

        JButton btnRoute = new JButton("Route");
        btnRoute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rand = new Random();
                doAutoRouting();
            }
        });
        mainP.add(btnRoute, gbc);

        JButton btnPlay = new JButton("Play");
        btnPlay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAndPlay();
            }
        });
        mainP.add(btnPlay, gbc);

        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dots.removeAllChildren();
                lines.removeAllChildren();
            }
        });
        mainP.add(btnClear, gbc);
        gbc.gridy++;
        gbc.gridwidth = 3;

        lineModel = new SpinnerNumberModel(15, 1, 100, 1);
        lineModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                doAutoRouting();
            }
        });
        JSpinner spnLine = new JSpinner(lineModel);
        mainP.add(spnLine, gbc);

        return mainP;
    }

    private void createAndPlay() {
        state.selectionPanel.clearList();
        // Create the Playlist
        for (Unit u : touchedUnits) {
            System.out.printf("%s%n", u.toString());
            if (u.getNumberOfMappedInputs() > 0) {
                state.selectionPanel.addToList(u.getMappedInputName(0), u);
            }
        }
    }

    /**
     * 
     */
    private void doAutoRouting() {
        dots.removeAllChildren();
        lines.removeAllChildren();

        touchedUnits = new LinkedList<Unit>();

        // Collect units to route
        List<GeneralUnitPNode> units = new ArrayList<GeneralUnitPNode>();
        // for (int i = 0; i < state.growingLayer.getXSize(); i++) {
        // for (int j = 0; j < state.growingLayer.getYSize(); j++) {
        // GeneralUnitPNode u = state.mapPNode.getUnit(i, j);
        // if (u.isSelected()) {
        // units.add(u);
        // }
        // }
        // }
        for (Unit node : state.selectionPanel.unitsInPlaylist) {
            units.add(state.mapPNode.getUnit(node));
        }

        if (applyTSP) {

            log.info("Solving TSP");
            List<GeneralUnitPNode> tspList = new Vector<GeneralUnitPNode>();
            SimpleTSPSolver s = new SimpleTSPSolver(units, rdoHigh.isSelected(), rand);
            s.solve();
            int[] t = s.getTour();
            int n = s.start;
            for (@SuppressWarnings("unused")
            int element : t) {
                tspList.add(units.get(n));
                n = t[n];
            }
            units = tspList;
            log.info(String.format("TSP solved, total length is %f%n", s.tour.length()));
        }

        if (rdoFlat.isSelected()) {
            doFlatRouting(units);
        } else if (rdoSnap.isSelected()) {
            doSnappedRouting(units);
        } else if (rdoHigh.isSelected()) {
            doHighDimRouting(units);
        }
    }

    private void doSnappedRouting(List<GeneralUnitPNode> units) {
        GeneralUnitPNode last = null, current = null;
        float lastX, lastY, currentX = 0, currentY = 0;
        HashMap<GeneralUnitPNode, PPath> dotCache = new HashMap<GeneralUnitPNode, PPath>();

        PLine line = new PLine();
        BasicStroke bs = new BasicStroke(lineModel.getNumber().floatValue(), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND);
        line.setStroke(bs);
        lines.addChild(line);

        Iterator<GeneralUnitPNode> it = units.iterator();
        while (it.hasNext()) {
            last = current;
            lastX = currentX;
            lastY = currentY;

            current = it.next();
            currentX = (float) (current.getX() + current.getWidth() / 2);
            currentY = (float) (current.getY() + current.getHeight() / 2);

            // Dot on the unit
            PPath d = PPath.createEllipse(currentX - bigD / 2, currentY - bigD / 2, bigD, bigD);
            dotCache.put(current, d);
            dots.addChild(d);

            // Draw line and intermedate steps
            if (last != null) {
                int steps = spinnerModel.getNumber().intValue();

                float dx = (currentX - lastX) / (steps + 1);
                float dy = (currentY - lastY) / (steps + 1);

                for (int i = 1; i <= steps; i++) {
                    float ipx = (float) (last.getX() + last.getWidth() / 2 + i * dx);
                    float ipy = (float) (last.getY() + last.getHeight() / 2 + i * dy);
                    float oipx = ipx, oipy = ipy;

                    // Snapping!
                    int x = (int) (ipx / state.mapPNode.getUnitWidth());
                    int y = (int) (ipy / state.mapPNode.getUnitHeight());
                    GeneralUnitPNode u = state.mapPNode.getUnit(x, y);
                    ipx = (float) (u.getX() + u.getWidth() / 2);
                    ipy = (float) (u.getY() + u.getHeight() / 2);

                    line.addPoint(line.getPointCount(), ipx, ipy);

                    // Small dot
                    PPath el = dotCache.get(u);
                    @SuppressWarnings("unused")
                    boolean newDot = false;
                    if (el == null) {
                        el = PPath.createEllipse(ipx - smallD / 2, ipy - smallD / 2, smallD, smallD);
                        newDot = true;
                        dotCache.put(u, el);
                        dots.addChild(el);
                        touchedUnits.add(u.getUnit());
                    }
                    if (debug) {
                        PPath sh = PPath.createLine(ipx, ipy, oipx, oipy);
                        sh.setStrokePaint(Color.red);
                        lines.addChild(sh);

                        PPath od = PPath.createEllipse(oipx - smallD / 4, oipy - smallD / 4, smallD / 2, smallD / 2);
                        od.setStrokePaint(Color.red);
                        el.addChild(od);
                        PText c = new PText(String.format("%d (%d/%d)", i, x, y));
                        c.setX(od.getX() + od.getWidth());
                        c.setY(od.getY());
                        od.addChild(c);
                    }
                    lastX = ipx;
                    lastY = ipy;
                }

            }
            line.addPoint(line.getPointCount(), currentX, currentY);
            touchedUnits.add(current.getUnit());
        }
    }

    private void doHighDimRouting(List<GeneralUnitPNode> units) {
        // TODO: Assumes Euclid distance. Consider metric used for training.
        GeneralUnitPNode last = null, current = null;
        double[] lastW = null, currentW = null;
        float currentX = 0, currentY = 0;

        PLine line = new PLine();
        BasicStroke bs = new BasicStroke(lineModel.getNumber().floatValue(), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND);
        line.setStroke(bs);
        lines.addChild(line);

        HashMap<GeneralUnitPNode, PPath> dotCache = new HashMap<GeneralUnitPNode, PPath>();

        int steps = spinnerModel.getNumber().intValue();

        Iterator<GeneralUnitPNode> it = units.iterator();
        int uc = 0;
        while (it.hasNext()) {
            last = current;
            lastW = currentW;

            current = it.next();
            uc++;
            // Where are we?
            currentW = current.getUnit().getWeightVector();
            currentX = (float) (current.getX() + current.getWidth() / 2);
            currentY = (float) (current.getY() + current.getHeight() / 2);

            // Dot on the unit
            PPath d = PPath.createEllipse(currentX - bigD / 2, currentY - bigD / 2, bigD, bigD);
            if (debug) {
                PText t = new PText(String.format("%d", uc));
                t.setX(currentX - t.getWidth() / 2);
                t.setY(currentY - t.getHeight() / 2);
                d.addChild(t);
            }
            dotCache.put(current, d);
            dots.addChild(d);

            if (last != null) {
                double[] dw = new double[currentW.length];
                for (int i = 0; i < dw.length; i++) {
                    dw[i] = (currentW[i] - lastW[i]) / (steps + 1);
                }

                double[] bW = lastW;
                for (int i = 1; i <= steps; i++) {
                    bW = wgtAdd(bW, dw);

                    Unit u = state.growingLayer.getWinner(new InputDatum("tmp", new DenseDoubleMatrix1D(bW)));
                    GeneralUnitPNode gu = state.mapPNode.getUnit(u);

                    if (dotCache.get(gu) != null) {
                        continue;
                    }

                    float ipx = (float) (gu.getX() + gu.getWidth() / 2);
                    float ipy = (float) (gu.getY() + gu.getHeight() / 2);

                    PPath dot = PPath.createEllipse(ipx - smallD / 2, ipy - smallD / 2, smallD, smallD);
                    dotCache.put(gu, dot);
                    dots.addChild(dot);
                    touchedUnits.add(u);
                    line.addPoint(line.getPointCount(), ipx, ipy);
                }

            }
            touchedUnits.add(current.getUnit());
            line.addPoint(line.getPointCount(), currentX, currentY);
        }

    }

    private double[] wgtAdd(double[] a, double[] b) {
        double[] c = new double[a.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = a[i] + b[i];
        }
        return c;
    }

    private void doFlatRouting(List<GeneralUnitPNode> units) {
        GeneralUnitPNode last = null, current = null;
        float lastX, lastY, currentX = 0, currentY = 0;

        PLine line = new PLine();
        BasicStroke bs = new BasicStroke(lineModel.getNumber().floatValue(), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND);
        line.setStroke(bs);
        lines.addChild(line);

        Iterator<GeneralUnitPNode> it = units.iterator();
        int uc = 0;
        while (it.hasNext()) {
            last = current;
            lastX = currentX;
            lastY = currentY;

            current = it.next();
            uc++;
            currentX = (float) (current.getX() + current.getWidth() / 2);
            currentY = (float) (current.getY() + current.getHeight() / 2);

            // Dot on the unit
            PPath d = PPath.createEllipse(currentX - bigD / 2, currentY - bigD / 2, bigD, bigD);
            if (debug) {
                PText t = new PText(String.format("%d", uc));
                t.setX(currentX - t.getWidth() / 2);
                t.setY(currentY - t.getHeight() / 2);
                d.addChild(t);
            }
            dots.addChild(d);

            // Draw line and intermedate steps
            if (last != null) {
                int steps = spinnerModel.getNumber().intValue();

                float dx = (currentX - lastX) / (steps + 1);
                float dy = (currentY - lastY) / (steps + 1);

                for (int i = 1; i <= steps; i++) {
                    float ipx = lastX + dx;
                    float ipy = lastY + dy;

                    // Small dot
                    dots.addChild(PPath.createEllipse(ipx - smallD / 2, ipy - smallD / 2, smallD, smallD));
                    // Line
                    line.addPoint(line.getPointCount(), ipx, ipy);

                    lastX = ipx;
                    lastY = ipy;
                }

            }
            line.addPoint(line.getPointCount(), currentX, currentY);

        }
    }

    public class SimpleTSPSolver {

        private Tour tour;

        private int start;

        @SuppressWarnings("unused")
        private int end;

        public SimpleTSPSolver(List<GeneralUnitPNode> units) {
            this(units, false, new Random());
        }

        public SimpleTSPSolver(List<GeneralUnitPNode> units, boolean routeHighDim, Random rand) {
            Graph g = new Graph(units.size());
            // TODO Hard-coded Metric! Uses L2 only
            for (int i = 0; i < units.size(); i++) {
                for (int j = 0; j < units.size(); j++) {
                    double[] v1, v2;
                    if (routeHighDim) {
                        v1 = units.get(i).getUnit().getWeightVector();
                        v2 = units.get(j).getUnit().getWeightVector();
                    } else {
                        v1 = new double[] { units.get(i).getX(), units.get(i).getY() };
                        v2 = new double[] { units.get(j).getX(), units.get(j).getY() };
                    }
                    double dist = 0;
                    for (int k = 0; k < v2.length; k++) {
                        dist += Math.pow(v1[k] - v2[k], 2);
                    }
                    dist = Math.sqrt(dist);
                    g.connect(i, j, dist);
                }
            }

            tour = new Tour(g);
            tour.random(rand);
        }

        public void solve() {
            // TODO: Solve better than local?
            tour.localoptimize();

            double maxDist = 0;
            int toi = -1, fromi = -1;
            for (int i = 0; i < tour.to.length; i++) {
                if (maxDist < tour.g.distance(i, tour.to[i])) {
                    maxDist = tour.g.distance(i, tour.to[i]);
                    toi = i;
                    fromi = tour.to[i];
                }
            }

            tour.to[toi] = -1;
            end = toi;
            tour.from[fromi] = -1;
            start = fromi;
        }

        public int[] getTour() {
            return tour.to;
        }

        /**
         * contains a Matrix of distances for a graph.
         */
        class Graph {
            protected int n; // N

            double dist[][]; // M

            final static double INFTY = Double.MAX_VALUE;

            public Graph(int n) {
                this.n = n;
                dist = new double[n][n];
                int i, j;
                // initially disconnect all points
                for (i = 0; i < n; i++) {
                    for (j = 0; j < n; j++) {
                        if (i != j) {
                            connect(i, j, INFTY);
                        } else {
                            connect(i, j, 0);
                        }
                    }
                }
            }

            void connect(int i, int j, double x) {
                dist[i][j] = x;
            }

            final double distance(int i, int j) {
                return dist[i][j];
            }

            final int size() {
                return n;
            }
        }

        /**
         * A path in a graph. From[i] is the index of the point leading to i. To[i] the index of the point after i. The
         * path can optimize itself in a graph.
         */
        class Tour {
            Graph g;

            int n;

            double l;

            public int from[], to[];

            public Tour(Graph g) {
                n = g.size();
                this.g = g;
                from = new int[n];
                to = new int[n];
            }

            // return a clone path
            @Override
            public Object clone() {
                Tour p = new Tour(g);
                p.l = l;
                int i;
                for (i = 0; i < n; i++) {
                    p.from[i] = from[i];
                    p.to[i] = to[i];
                }
                return p;
            }

            /** Create a random route. random path. */
            public void random(Random r) {
                int i, j, i0, j0, k;
                for (i = 0; i < n; i++) {
                    to[i] = -1;
                }

                for (i0 = i = 0; i < n - 1; i++) {
                    j = (int) (r.nextLong() % (n - i));
                    to[i0] = 0;
                    for (j0 = k = 0; k < j; k++) {
                        j0++;
                        while (to[j0] != -1) {
                            j0++;
                        }
                    }
                    while (to[j0] != -1) {
                        j0++;
                    }
                    to[i0] = j0;
                    from[j0] = i0;
                    i0 = j0;
                }
                to[i0] = 0;
                from[0] = i0;
                getlength();
            }

            /**
             * The length of this route.
             * 
             * @return The length
             */
            public double length() {
                return l;
            }

            /**
             * try to find another path with shorter length using removals of points j and inserting i,j,i+1
             * 
             * @return <code>true</code> if an improvement has been found, <code>false</code> otherwise.
             */
            public boolean improve() {
                int i, j, h;
                double d1, d2;
                double H[] = new double[n];
                for (i = 0; i < n; i++) {
                    H[i] = -g.distance(from[i], i) - g.distance(i, to[i]) + g.distance(from[i], to[i]);
                }
                for (i = 0; i < n; i++) {
                    d1 = -g.distance(i, to[i]);
                    j = to[to[i]];
                    while (j != i) {
                        d2 = H[j] + g.distance(i, j) + g.distance(j, to[i]) + d1;
                        if (d2 < -1e-10) {
                            h = from[j];
                            to[h] = to[j];
                            from[to[j]] = h;
                            h = to[i];
                            to[i] = j;
                            to[j] = h;
                            from[h] = j;
                            from[j] = i;
                            getlength();
                            return true;
                        }
                        j = to[j];
                    }
                }
                return false;
            }

            /**
             * improve the path locally, using replacements of i,i+1 and j,j+1 with i,j and i+1,j+1
             * 
             * @return <code>true</code> if an improvement has been found, <code>false</code> otherwise.
             */
            public boolean improvecross() {
                int i, j, h, h1, hj;
                double d1, d2, d;
                for (i = 0; i < n; i++) {
                    d1 = -g.distance(i, to[i]);
                    j = to[to[i]];
                    d2 = 0;
                    d = 0;
                    while (to[j] != i) {
                        d += g.distance(j, from[j]) - g.distance(from[j], j);
                        d2 = d1 + g.distance(i, j) + d + g.distance(to[i], to[j]) - g.distance(j, to[j]);
                        if (d2 < -1e-10) {
                            h = to[i];
                            h1 = to[j];
                            to[i] = j;
                            to[h] = h1;
                            from[h1] = h;
                            hj = i;
                            while (j != h) {
                                h1 = from[j];
                                to[j] = h1;
                                from[j] = hj;
                                hj = j;
                                j = h1;
                            }
                            from[j] = hj;
                            getlength();
                            return true;
                        }
                        j = to[j];
                    }
                }
                return false;
            }

            /**
             * compute the length of the path
             */
            void getlength() {
                l = 0;
                int i;
                for (i = 0; i < n; i++) {
                    l += g.distance(i, to[i]);
                }
            }

            /**
             * find a local optimum starting from this path
             */
            void localoptimize() {
                do {
                    while (improve()) {
                        // ...
                    }
                } while (improvecross());
            }
        }

    }

}
