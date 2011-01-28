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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.sun.net.httpserver.HttpServer;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.nodes.PLine;

import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.commons.gui.controls.swing.table.ButtonCellEditor;
import at.tuwien.ifs.commons.gui.controls.swing.table.ButtonCellRenderer;
import at.tuwien.ifs.commons.gui.controls.swing.table.ColorCellRenderer;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.VisualizationChangeListener;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractViewerControl;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.PlaySOMPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.PlayListListener;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.PlaySOMPlayer;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.PlayerListener;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver.httphandler.MapInformationProvider;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver.httphandler.PocketSOMConfigProvider;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver.httphandler.SongProvider;
import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Jakob Frank
 * @version $Id: PocketSOMConnector.java 3900 2010-11-04 10:06:02Z frank $
 */
public class PocketSOMConnector extends AbstractViewerControl {
    private static final long serialVersionUID = 1L;

    private static final String START = "Listen...";

    private static final String STOP = "Stop";

    /**
     * Timeout for GC (in sec.): {@value}
     */
    private static final int LIFETIME = 15 * 60;

    private ConnectorEndpoint con = null;

    private JButton btnServerCtrl = null;

    private JTextField txtPIN = null;

    private JTextField txtPort = null;

    private JScrollPane scpUsers = null;

    private JTable tblUsers = null;

    private UserTableModel utm = null;

    private HttpServer httpServer;

    private JTextField txtHttpPort;

    private JButton btnHitHist;

    private int[][] pathHitHist;

    private JPanel pnlMerge;

    private JCheckBox chkMergeDebugPrint;

    public PocketSOMConnector(String title, CommonSOMViewerStateData state) {
        super(title, state);
        if (state != null) {
            pathHitHist = new int[state.growingLayer.getXSize()][state.growingLayer.getYSize()];
            for (int i = 0; i < pathHitHist.length; i++) {
                for (int j = 0; j < pathHitHist[i].length; j++) {
                    pathHitHist[i][j] = 0;
                }
            }
            initialize();
        }
    }

    private void initialize() {
        GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
        gridBagConstraints31.fill = GridBagConstraints.BOTH;
        gridBagConstraints31.gridy = 4;
        gridBagConstraints31.weightx = 1.0;
        gridBagConstraints31.weighty = 1.0;
        gridBagConstraints31.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints31.insets = new Insets(6, 0, 0, 0);
        gridBagConstraints31.gridx = 0;
        GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
        gridBagConstraints21.fill = GridBagConstraints.BOTH;
        gridBagConstraints21.gridy = 0;
        gridBagConstraints21.weightx = 1.0;
        gridBagConstraints21.gridx = 1;
        GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        gridBagConstraints11.fill = GridBagConstraints.BOTH;
        gridBagConstraints11.gridy = 1;
        gridBagConstraints11.weightx = 1.0;
        gridBagConstraints11.gridx = 1;
        GridBagConstraints gridBagConstraints99 = new GridBagConstraints();
        gridBagConstraints99.fill = GridBagConstraints.BOTH;
        gridBagConstraints99.gridy = 2;
        gridBagConstraints99.weightx = 1.0;
        gridBagConstraints99.gridx = 1;
        GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
        gridBagConstraints41.fill = GridBagConstraints.BOTH;
        gridBagConstraints41.gridy = 5;
        gridBagConstraints41.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints41.weightx = 1.0;
        gridBagConstraints41.insets = new Insets(6, 0, 0, 0);
        gridBagConstraints41.gridx = 0;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.insets = new Insets(0, 0, 0, 2);
        gridBagConstraints3.anchor = GridBagConstraints.EAST;
        gridBagConstraints3.gridy = 0;
        JLabel lblPort = new JLabel();
        lblPort.setText("Control Port:");
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 2;
        gridBagConstraints2.gridheight = 1;
        gridBagConstraints2.gridy = 0;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 2);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.gridy = 1;
        GridBagConstraints gridBagConstraints98 = new GridBagConstraints();
        gridBagConstraints98.gridx = 0;
        gridBagConstraints98.insets = new Insets(0, 0, 0, 2);
        gridBagConstraints98.anchor = GridBagConstraints.EAST;
        gridBagConstraints98.gridy = 2;
        GridBagConstraints gridBagConstraints02 = new GridBagConstraints();
        gridBagConstraints02.gridx = 2;
        gridBagConstraints02.gridy = 1;
        GridBagConstraints gridBagConstraints03 = new GridBagConstraints();
        gridBagConstraints03.gridx = 2;
        gridBagConstraints03.gridy = 2;
        GridBagConstraints gridBagConstraints3x = new GridBagConstraints();
        gridBagConstraints3x.gridx = 0;
        gridBagConstraints3x.gridy = 3;
        gridBagConstraints3x.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints3x.fill = GridBagConstraints.NONE;
        gridBagConstraints3x.anchor = GridBagConstraints.EAST;

        JLabel lblPIN = new JLabel();
        lblPIN.setText("Master PIN:");
        JLabel lblHttpPort = new JLabel();
        lblHttpPort.setText("Http Port:");
        JPanel mainP = new JPanel();
        mainP.setLayout(new GridBagLayout());

        mainP.add(lblPort, gridBagConstraints3);
        mainP.add(getTxtPort(), gridBagConstraints21);
        mainP.add(lblHttpPort, gridBagConstraints);
        mainP.add(getTxtHttpPort(), gridBagConstraints11);
        mainP.add(lblPIN, gridBagConstraints98);
        mainP.add(getTxtPIN(), gridBagConstraints99);
        mainP.add(getBtnServerCtrl(), gridBagConstraints3x);

        mainP.add(getScpUsers(), gridBagConstraints31);

        mainP.add(getPnlMerge(), gridBagConstraints41);
        this.setContentPane(mainP);
    }

    protected boolean stopServer() {
        if (httpServer != null) {
            httpServer.stop(2);
            httpServer = null;
        }

        if (con == null) {
            return false;
        }
        con.shutdown();
        con = null;
        txtPort.setEnabled(true);
        txtPIN.setEnabled(true);
        txtHttpPort.setEnabled(true);
        return true;
    }

    protected boolean startServer() {
        try {

            // Endpoint
            txtPort.setEnabled(false);
            txtPIN.setEnabled(false);
            int port = Integer.parseInt(txtPort.getText());
            if (con != null) {
                return false;
            }
            con = new ConnectorEndpoint(port, txtPIN.getText());
            con.start();

            // Config
            txtHttpPort.setEnabled(false);
            if (httpServer == null) {
                String musicContext = "/music";
                String configContext = "/eps";
                int httpP = 8000;
                try {
                    httpP = Integer.parseInt(txtHttpPort.getText());
                } catch (NumberFormatException e) {

                }
                PocketSOMConfigProvider ps = new PocketSOMConfigProvider(state, port, musicContext);
                httpServer = HttpServer.create(new InetSocketAddress(httpP), 0);
                httpServer.setExecutor(null);
                httpServer.createContext(configContext, ps);
                httpServer.createContext("/ePocketSOM", ps);
                httpServer.createContext(musicContext, new SongProvider(state, musicContext));
                httpServer.createContext("/", new MapInformationProvider(state, musicContext, configContext));
                httpServer.start();
            }

            return true;
        } catch (Exception e) {
            txtPort.setEnabled(true);
            txtPIN.setEnabled(true);
            txtHttpPort.setEnabled(true);
            return false;
        }
    }

    protected PocketSOMConnector(String title, CommonSOMViewerStateData state, LayoutManager layout) {
        this(title, state);
    }

    /**
     * This method initializes btnServerCtrl
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnServerCtrl() {
        if (btnServerCtrl == null) {
            btnServerCtrl = new JButton();
            btnServerCtrl.setText(START);
            btnServerCtrl.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getActionCommand().equals(START)) {
                        if (startServer()) {
                            btnServerCtrl.setText(STOP);
                            btnServerCtrl.setActionCommand(STOP);
                        }
                    } else {
                        if (stopServer()) {
                            btnServerCtrl.setText(START);
                            btnServerCtrl.setActionCommand(START);
                        }
                    }

                }
            });

        }
        return btnServerCtrl;
    }

    private JButton getBtnHitHist() {
        if (btnHitHist == null) {
            btnHitHist = new JButton("Show HitHist");
            btnHitHist.setToolTipText("Show the path hit histogram");
            btnHitHist.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    state.mapPNode.showHistogramOverlayVisualization(pathHitHist, 0); // 2nd param is visualization
                    // index
                }
            });
        }
        return btnHitHist;
    }

    /**
     * This method initializes txtPIN
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtPIN() {
        if (txtPIN == null) {
            txtPIN = new JTextField();
            // txtPIN.setColumns(4);
            txtPIN.setText("1234");
        }
        return txtPIN;
    }

    /**
     * This method initializes txtPort
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTxtPort() {
        if (txtPort == null) {
            txtPort = new JTextField();
            // txtPort.setColumns(6);
            txtPort.setText("9619");
        }
        return txtPort;
    }

    private JTextField getTxtHttpPort() {
        if (txtHttpPort == null) {
            txtHttpPort = new JTextField();
            // txtHttpPort.setColumns(6);
            txtHttpPort.setText("8000");
        }
        return txtHttpPort;
    }

    /**
     * This method initializes scpUsers
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getScpUsers() {
        if (scpUsers == null) {
            scpUsers = new JScrollPane(getTblUsers());
            scpUsers.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    getTblUsers().clearSelection();
                }
            });
            scpUsers.setPreferredSize(new Dimension(state.controlElementsWidth / 2, Math.max(
                    getTblUsers().getRowCount(), 4) * getTblUsers().getRowHeight()));
        }
        return scpUsers;
    }

    /**
     * This method initializes tblUsers
     * 
     * @return javax.swing.JTable
     */
    private JTable getTblUsers() {
        if (tblUsers == null) {
            tblUsers = new JTable();
            tblUsers.setDefaultRenderer(Color.class, new ColorCellRenderer(true));
            tblUsers.setDefaultRenderer(JButton.class, new ButtonCellRenderer());
            tblUsers.setDefaultEditor(JButton.class, new ButtonCellEditor());
            utm = new UserTableModel();
            tblUsers.setModel(utm);
            tblUsers.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    utm.setSelected(tblUsers.getSelectedRows());
                }
            });
            tblUsers.setFillsViewportHeight(true);
            // tblUsers.setPreferredSize(new Dimension(state.controlElementsWidth));
        }
        return tblUsers;
    }

    private JPanel getPnlMerge() {
        if (pnlMerge == null) {
            pnlMerge = new TitledCollapsiblePanel("Advanced", true);
            Container c = ((TitledCollapsiblePanel) pnlMerge).getContentPane();
            c.setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridy = 0;
            gc.gridwidth = GridBagConstraints.RELATIVE;
            c.add(new JLabel("Merge selected Paths:"), gc);
            gc.gridwidth = GridBagConstraints.REMAINDER;
            gc.anchor = GridBagConstraints.EAST;
            c.add(getChkMergeDebugPrint(), gc);
            gc.gridy++;

            gc.gridwidth = 2;
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.BOTH;

            c.add(getBtnMergeUnitBased(), gc);
            c.add(getBtnMergeLineBased(), gc);
            gc.gridy++;
            c.add(getBtnMergePathInputSpace(), gc);
            c.add(getBtnMergePathMapSpace(), gc);
            gc.gridy++;
            c.add(getBtnMergeConcat(), gc);
            c.add(getBtnReversePath(), gc);
            gc.gridy++;
            c.add(getBtnHighlightPath(), gc);
            c.add(getBtnHitHist(), gc);
            gc.gridy++;
            c.add(getBtnClearMerges(), gc);
            c.add(getBtnUsePath(), gc);

            getTblUsers().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    // boolean status = getTblUsers().getSelectedRowCount() > 1;
                    // pnlMerge.setEnabled(status);
                    // for (Component c : pnlMerge.getComponents()) {
                    // c.setEnabled(status);
                    // }
                }
            });

            // Initially disabled
            // pnlMerge.setEnabled(false);
            // for (Component c : pnlMerge.getComponents()) {
            // c.setEnabled(false);
            // }

        }
        return pnlMerge;
    }

    private PathMerger pathMerger = null;

    private JButton btnMergeUnitBased;

    private JButton btnMergeLineBased;

    private JButton btnHighlightPath;

    private JButton btnMergePathMapSpace;

    private JButton btnMergePathInputSpace;

    private JButton btnReversePath;

    private JButton btnClearMerges;

    private JButton btnMergeConcat;

    private JButton btnUsePath;

    private JButton getBtnMergeUnitBased() {
        if (btnMergeUnitBased == null) {
            btnMergeUnitBased = new JButton("unitBasedMerge");
            btnMergeUnitBased.setMargin(SMALL_INSETS);
            btnMergeUnitBased.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (pathMerger == null) {
                        pathMerger = new PathMerger(state.mapPNode);
                    }
                    pathMerger.setDebug(getChkMergeDebugPrint().isSelected());
                    int[] rows = tblUsers.getSelectedRows();
                    PNode[] nodes = new PNode[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        nodes[i] = utm.getRow(rows[i]).node;
                    }
                    pathMerger.unitBasedMerge(nodes);
                }
            });
        }
        return btnMergeUnitBased;
    }

    private JButton getBtnMergeLineBased() {
        if (btnMergeLineBased == null) {
            btnMergeLineBased = new JButton("lineBasedMerge");
            btnMergeLineBased.setMargin(SMALL_INSETS);
            btnMergeLineBased.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (pathMerger == null) {
                        pathMerger = new PathMerger(state.mapPNode);
                    }
                    pathMerger.setDebug(getChkMergeDebugPrint().isSelected());
                    int[] rows = tblUsers.getSelectedRows();
                    PNode[] nodes = new PNode[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        nodes[i] = utm.getRow(rows[i]).node;
                    }
                    pathMerger.lineBasedMerge(nodes);
                }
            });
        }
        return btnMergeLineBased;
    }

    private JButton getBtnHighlightPath() {
        if (btnHighlightPath == null) {
            btnHighlightPath = new JButton("Highlight");
            btnHighlightPath.setMargin(SMALL_INSETS);
            btnHighlightPath.addActionListener(new ActionListener() {
                boolean currentStatus = false;

                @Override
                public void actionPerformed(ActionEvent e) {
                    currentStatus = !currentStatus;
                    PathMerger pm = new PathMerger(state.mapPNode, false);
                    PNode node = utm.getRow(tblUsers.getSelectedRow()).node;
                    pm.highlightIntersectingUnits(node, currentStatus);
                }
            });
        }
        return btnHighlightPath;
    }

    private JButton getBtnMergePathMapSpace() {
        if (btnMergePathMapSpace == null) {
            btnMergePathMapSpace = new JButton("New Map Interm");
            btnMergePathMapSpace.setMargin(SMALL_INSETS);
            btnMergePathMapSpace.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (pathMerger == null) {
                        pathMerger = new PathMerger(state.mapPNode);
                    }
                    pathMerger.setDebug(getChkMergeDebugPrint().isSelected());
                    int[] rows = tblUsers.getSelectedRows();
                    PNode[] nodes = new PNode[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        nodes[i] = utm.getRow(rows[i]).node;
                    }
                    pathMerger.newIntermediateMapMerge(nodes);
                }
            });
        }
        return btnMergePathMapSpace;
    }

    private JButton getBtnMergePathInputSpace() {
        if (btnMergePathInputSpace == null) {
            btnMergePathInputSpace = new JButton("New IS Interm");
            btnMergePathInputSpace.setMargin(SMALL_INSETS);
            btnMergePathInputSpace.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (pathMerger == null) {
                        pathMerger = new PathMerger(state.mapPNode);
                    }
                    pathMerger.setDebug(getChkMergeDebugPrint().isSelected());
                    int[] rows = tblUsers.getSelectedRows();
                    PNode[] nodes = new PNode[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        nodes[i] = utm.getRow(rows[i]).node;
                    }
                    pathMerger.newIntermediateInputSpaceMerge(nodes);
                }
            });
        }
        return btnMergePathInputSpace;
    }

    private JButton getBtnReversePath() {
        if (btnReversePath == null) {
            btnReversePath = new JButton("Reverse Path");
            btnReversePath.setMargin(SMALL_INSETS);
            btnReversePath.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int[] rows = tblUsers.getSelectedRows();
                    for (int row : rows) {
                        utm.getRow(row).reversePaths();
                    }
                }
            });
        }
        return btnReversePath;
    }

    private JButton getBtnClearMerges() {
        if (btnClearMerges == null) {
            btnClearMerges = new JButton("Clear");
            btnClearMerges.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (pathMerger == null) {
                        pathMerger = new PathMerger(state.mapPNode);
                    }
                    pathMerger.setDebug(getChkMergeDebugPrint().isSelected());
                    int[] rows = tblUsers.getSelectedRows();
                    PNode[] nodes = new PNode[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        nodes[i] = utm.getRow(rows[i]).node;
                    }
                    pathMerger.deleteAllDrawnStuff();

                    state.mapPNode.clearHistogramOverlayVisualization();
                    for (int i = 0; i < pathHitHist.length; i++) {
                        for (int j = 0; j < pathHitHist[i].length; j++) {
                            pathHitHist[i][j] = 0;
                        }
                    }
                }
            });
        }
        return btnClearMerges;
    }

    private JButton getBtnMergeConcat() {
        if (btnMergeConcat == null) {
            btnMergeConcat = new JButton("Concat");
            btnMergeConcat.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (pathMerger == null) {
                        pathMerger = new PathMerger(state.mapPNode);
                    }
                    pathMerger.setDebug(getChkMergeDebugPrint().isSelected());
                    int[] rows = tblUsers.getSelectedRows();
                    PNode[] nodes = new PNode[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        nodes[i] = utm.getRow(rows[i]).node;
                    }

                    pathMerger.concatPaths(nodes);
                }
            });
        }
        return btnMergeConcat;
    }

    private JButton getBtnUsePath() {
        if (btnUsePath == null) {
            btnUsePath = new JButton("Use Path");
            btnUsePath.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (pathMerger == null) {
                        return;
                    }

                    PLine border = pathMerger.getBorderLine();
                    if (border == null) {
                        return;
                    }

                    PNode p = utm.addPath("Border", Color.CYAN);
                    p.addChild(border);

                }
            });
        }
        return btnUsePath;
    }

    private JCheckBox getChkMergeDebugPrint() {
        if (chkMergeDebugPrint == null) {
            chkMergeDebugPrint = new JCheckBox("Debug");
        }
        return chkMergeDebugPrint;
    }

    /**
     * @deprecated should not be started directly
     * @param args Commandline args
     */
    @Deprecated
    public static void main(String[] args) {
        try {
            PocketSOMConnector psc = new PocketSOMConnector("TEST", null);

            String eps = "/eps/";
            String data = "/music/";

            HttpServer s = HttpServer.create(new InetSocketAddress(8000), 0);
            s.setExecutor(null);
            s.createContext(eps, new PocketSOMConfigProvider(psc.state, s.getAddress().getPort() + 5, data));
            s.createContext("/", new MapInformationProvider(psc.state, data, eps));
            s.start();

            JOptionPane.showMessageDialog(null, "Stop");
            s.stop(5);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The ConnectorEndpoint, receiving Paths and PlayLists.
     * 
     * @author Jakob Frank
     */
    private class ConnectorEndpoint extends Thread {

        boolean running;

        ServerSocket server;

        // Vector because Vector is synchronized!
        private Vector<Socket> notificationReceivers;

        private PlayerListener _PlayerListener;

        private PlayListListener _PlayListListener;

        private VisualizationChangeListener _VisualizationChangeListener;

        private final String pin;

        private ConnectorEndpoint(int port, String pin) throws Exception {
            running = false;
            this.pin = pin;
            server = new ServerSocket(port);

            this.setName(this.getClass().getSimpleName() + " (Port " + port + ", PIN: " + pin + ")");

            _PlayerListener = new PlayerListener() {
                @Override
                public void playStarted(int mode, AudioVectorMetaData song) {
                    informListeners("currentsong " + song.getID());
                }

                @Override
                public void playStopped(int reason, AudioVectorMetaData song) {
                    informListeners("currentsong");
                }
            };
            _PlayListListener = new PlayListListener() {
                @Override
                public void playListContentChanged() {
                    informListeners("playlist changed");
                }
            };
            _VisualizationChangeListener = new VisualizationChangeListener() {
                @Override
                public void visualisationChanged() {
                    informListeners("visualisation changed");
                }
            };
            notificationReceivers = new Vector<Socket>();
        }

        @Override
        public void run() {
            running = true;
            register4PlayerNotifications();
            state.getSOMViewer().addVisualizationChangeListener(_VisualizationChangeListener);
            try {
                server.setSoTimeout(500);
                while (running) {
                    try {
                        Socket sock = server.accept();
                        Worker w = new Worker(sock);
                        w.start();
                        // new Thread(w).start();
                    } catch (SocketTimeoutException e) {
                        // nop;
                    }
                }
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            state.getSOMViewer().removeVisualizationChangeListener(_VisualizationChangeListener);
            unregister4PlayerNotifications();
            utm.clearAllPaths();
        }

        public void shutdown() {
            running = false;
        }

        private void register4PlayerNotifications() {
            if (state.selectionPanel instanceof PlaySOMPlayer) {
                PlaySOMPlayer p = (PlaySOMPlayer) state.selectionPanel;
                p.addPlayerListener(_PlayerListener);
                p.addPlayListListener(_PlayListListener);
            }
        }

        private void unregister4PlayerNotifications() {
            if (state.selectionPanel instanceof PlaySOMPlayer) {
                PlaySOMPlayer p = (PlaySOMPlayer) state.selectionPanel;
                p.removePlayerListener(_PlayerListener);
                p.removePlayListListener(_PlayListListener);
            }
        }

        // int counter = 0;
        private void informListeners(String message) {
            String msg = String.format("INFO: %s%n", message);
            List<Socket> deadSockets = new LinkedList<Socket>();
            synchronized (notificationReceivers) {
                for (Socket s : notificationReceivers) {
                    if (s.isClosed() || !s.isConnected() || s.isOutputShutdown()) {
                        System.out.printf("Socket is dead: %s:%d%n", s.getInetAddress().getHostAddress(), s.getPort());
                        deadSockets.add(s);
                        continue;
                    }
                    try {
                        // System.out.printf("Message to %s:%d - %s", s.getInetAddress().getHostAddress(), s.getPort(),
                        // msg);
                        s.getOutputStream().write(msg.getBytes());
                        s.getOutputStream().flush();
                    } catch (Exception e) {
                        System.out.printf("Socket has problems: %s:%d%n", s.getInetAddress().getHostAddress(),
                                s.getPort());
                        // e.printStackTrace();
                        deadSockets.add(s);
                    }
                }
                notificationReceivers.removeAll(deadSockets);
            }
        }

        private class Worker extends Thread {// implements Runnable {
            private Socket socket;

            public final String CLIENT;

            public final String MY_NAME = "WorkerTread";

            private PlaySOMPlayer player = null;

            private boolean authenticated = false;

            public Worker(Socket socket) {
                CLIENT = String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort());

                this.setName(this.toString());

                this.socket = socket;
                authenticated = false;
                if (state.selectionPanel instanceof PlaySOMPlayer) {
                    player = (PlaySOMPlayer) state.selectionPanel;
                }
            }

            @Override
            public String toString() {
                return String.format("%s (%s)", MY_NAME, CLIENT);
            }

            @Override
            public void run() {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                    for (String line = br.readLine(); line != null; line = br.readLine()) {
                        Date d = new Date();
                        System.out.printf("%tT.%tL %s %c %s%n", d, d, CLIENT, (authenticated ? '+' : '-'), line);

                        if (line.startsWith("Sending:")) {
                            String contentType = line.split(" ", 2)[1];
                            if (contentType.equals("Line")) {
                                receivePath(br);
                            } else if (contentType.equals("Playlist")) {
                                receivePlaylist(br);
                            }
                        } else if (line.startsWith("Login:")) {
                            clientLogin(line, br, ps);
                        } else if (line.startsWith("Get:")) {
                            sendStatusInfo(line, br, ps);
                        } else if (line.startsWith("Player:")) {
                            if (authenticated) {
                                playerControl(line);
                            }
                        } else if (line.matches("([Uu]n)?[Rr]egister.*")) {
                            manageRegistrations(line, socket, ps);
                        } else if (line.matches("[Qq](uit)?")) {
                            break;
                        } else if (line.startsWith("Ping")) {
                            if (authenticated) {
                                String message = "Ping";
                                String[] lt = line.split("[ \t]+", 2);
                                if (lt.length > 1) {
                                    message = lt[1];
                                }
                                informListeners(message);
                            }
                        } else if (line.startsWith("Debug")) {
                            // Nop, sysout already done.
                            /*
                             * OLD } else if (false == true) { // This is old and therefore inactive! System.err.println("Illegal Communication!");
                             * System.err.println("Communication dump:"); System.err.println("  " + line); while ((line = br.readLine()) != null) {
                             * System.err.println("  " + line); } System.err.println("End Dump");
                             */
                        } else { // This is new
                            System.out.println("   Illegal Communication!");
                            // System.err.println(" " + line);
                        }
                    }
                    socket.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }

            }

            /**
             * Handle client authentication.
             */
            private void clientLogin(String line, BufferedReader in, PrintStream out) {
                String lType = line.split("[ \t]+")[1];
                final String L_OK = "Login: OK";
                final String L_ER = "Login: FAILED";

                if (lType.equalsIgnoreCase("PIN")) {
                    try {
                        String pLine = in.readLine();
                        String receivedPin = pLine.split("[ \t]+", 2)[1];
                        if (receivedPin.equals(pin)) {
                            authenticated = true;
                            out.println(L_OK);
                        } else {
                            out.printf("%s %s%n", L_ER, "Wrong PIN");
                        }
                    } catch (Exception e) {
                        out.printf("%s %s%n", L_ER, "Communication Error");
                    }
                } else if (lType.equalsIgnoreCase("USER")) {
                    try {
                        // String uLine = in.readLine();
                        // String pLine = in.readLine();
                        out.printf("%s Sorry, function is not implemented%n", L_ER);
                    } catch (Exception e) {
                        out.printf("%s %s%n", L_ER, "Communication Error");
                    }
                }
            }

            private void manageRegistrations(String line, Socket socket, PrintStream out) {
                if (line.equalsIgnoreCase("register")) {
                    String current = "";
                    if (player != null && player.isPlaying()) {
                        current = player.getCurrentSongID();
                        out.printf("INFO: currentsong %s%n", current);
                    } else {
                        out.printf("INFO: currentsong%n");
                    }
                    notificationReceivers.add(socket);
                } else if (line.equalsIgnoreCase("unregister")) {
                    notificationReceivers.remove(socket);
                }

            }

            private void playerControl(String line) {
                // For authenticated users only!
                if (!authenticated) {
                    return;
                }

                String lt[] = line.split("[ \t]+", 3);
                if (lt.length < 2) {
                    return;
                }

                if (lt[1].equalsIgnoreCase("play")) {
                    if (lt.length > 2) {
                        player.startPlaying(lt[2]);
                    } else {
                        player.startPlaying();
                    }
                } else if (lt[1].equalsIgnoreCase("stop")) {
                    player.stopPlaying();
                } else if (lt[1].equalsIgnoreCase("next")) {
                    player.skipPlayer(1);
                } else if (lt[1].equalsIgnoreCase("prev")) {
                    player.skipPlayer(-1);
                }
            }

            private void sendStatusInfo(String line, BufferedReader in, PrintStream out) {
                String ls[] = line.split("[ \t]+", 2);
                if (ls.length < 2) {
                    System.err.printf("Unknown request: \"%s\"%n", line);
                    return;
                }
                if ("CurrentSong".equalsIgnoreCase(ls[1])) {
                    String current = "";
                    if (player != null && player.isPlaying()) {
                        current = player.getCurrentSongID();
                    }
                    out.printf("currentsong: %s%n", current);
                } else if ("CurrentPos".equalsIgnoreCase(ls[1])) {
                    double[] xy = new double[] { -1, -1 };
                    if (player != null && player.isPlaying()) {
                        xy = player.getCurrentPos();
                    }
                    out.printf("currentpos: %f/%f%n", xy[0], xy[1]);
                } else if ("VisTimeStamp".equalsIgnoreCase(ls[1])) {
                    System.err.println("VisTimeStamp not jet implemented!");
                } else if ("PlayList".equalsIgnoreCase(ls[1])) {
                    out.println("PlayList");
                    if (player != null) {
                        for (String song : player.getPlayList()) {
                            out.printf("Song: %s%n", song);
                        }
                    }
                    out.println("EndList");
                } else {
                    System.err.printf("Unknown request: \"%s\"%n", line);
                }
            }

            private void receivePath(BufferedReader br) {
                PNode receivedLinePath = null;
                Color lineColor = Color.green;
                int lineWidth = 14;
                String username = socket.getInetAddress().getHostAddress();

                int xSize = state.growingLayer.getXSize();
                float width = state.mapPNode.getUnitWidth() * xSize;
                int ySize = state.growingLayer.getYSize();
                float height = state.mapPNode.getUnitHeight() * ySize;

                try {
                    String line;
                    PLine currentLine = null;
                    while ((line = br.readLine()) != null) {
                        // System.out.println("-->" + line);
                        if (line.startsWith("LinePoint:")) {
                            // if (receivedLinePath == null)
                            if (currentLine == null) {
                                continue;
                            }
                            String[] lineSplit = line.split("[ /]", 3);
                            float currentX = Float.parseFloat(lineSplit[1]);
                            float currentY = Float.parseFloat(lineSplit[2]);
                            currentLine.addPoint(currentLine.getPointCount(), currentX * width, currentY * height);
                            currentLine.repaint();
                            try {
                                pathHitHist[(int) (currentX * xSize)][(int) (currentY * ySize)]++;
                            } catch (Exception e) {
                                System.out.println("X");
                            }
                            // }
                        } else if (line.startsWith("LineStart")) {
                            String[] lineSplit = line.split(" ");

                            for (int i = 1; i < lineSplit.length; i++) {
                                String[] argument = lineSplit[i].split("=", 2);
                                String key = argument[0];
                                String val = argument[1];
                                if (key.equals("color")) {
                                    try {
                                        lineColor = Color.decode(val);
                                    } catch (Exception e) {
                                    }
                                } else if (key.equals("width")) {
                                    try {
                                        lineWidth = Integer.parseInt(val);
                                    } catch (NumberFormatException e) {
                                    }
                                } else if (key.equals("username") || key.equals("user")) {
                                    username = val;
                                }
                            }
                            receivedLinePath = utm.addPath(username, lineColor);
                            currentLine = new PLine();
                            currentLine.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND,
                                    BasicStroke.JOIN_ROUND));
                            currentLine.setStrokePaint(lineColor);
                            receivedLinePath.addChild(currentLine);
                        } else if (line.startsWith("LineEnd")) {
                            break; // Back to the start...
                        } else if (line.startsWith("ClearLines")) {
                            utm.clearPaths(username);
                        } else {
                            System.err.println("Invalid Communication:");
                            System.err.println("  " + line);
                            System.err.println("Reset.");
                            break;
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void receivePlaylist(BufferedReader br) {
                String receivedFileNamePrefix = "";
                try {
                    // boolean pinChecked = false;
                    String line;
                    line = br.readLine();
                    if (line.startsWith("PIN: ")) {
                        String sentPin = line.split(" ", 2)[1];
                        if (sentPin.equals(pin)) {
                            // pinChecked = true;
                            authenticated = true;
                            socket.getOutputStream().write("OK\n".getBytes());
                        } else {
                            socket.getOutputStream().write("Wrong PIN\n".getBytes());
                        }
                        line = br.readLine();
                    }
                    // if (pinChecked) {
                    if (authenticated) {
                        ArrayList<String> songs = new ArrayList<String>();
                        for (; line != null; line = br.readLine()) {
                            // System.out.println(line);
                            if (line.startsWith("BaseURL:")) {
                                receivedFileNamePrefix = line.split(" ", 2)[1];
                            } else if (line.startsWith("Song:")) {
                                String song = line.split(" ", 2)[1];
                                try {
                                    song = URLDecoder.decode(song, "utf8");
                                } catch (Exception e) {

                                }
                                songs.add(song);
                            } else if (line.startsWith("StartPath:")) {
                                // nop
                            } else if (line.startsWith("Length:")) {
                                // nop
                            } else if (line.startsWith("EndPath")) {
                                break;
                            } else {
                                System.err.println("Invalid Communication:");
                                System.err.println("  \"" + line + "\"");
                                System.err.println("Reset.");
                                break;
                            }
                        }
                        // This is done in the calling run-method!
                        // socket.close();

                        // Check and create the playlist
                        if (state.selectionPanel instanceof PlaySOMPanel) {
                            PlaySOMPanel play = (PlaySOMPanel) state.selectionPanel;

                            play.clearList();
                            Unit[] map = state.growingLayer.getAllUnits();
                            for (String song : songs) {
                                Unit u = mapContains(song, map);
                                if (u != null) {
                                    play.addToList(song, u);
                                } else {
                                    play.addToList(song, receivedFileNamePrefix, u);
                                }
                            }

                            play.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, play.PLAY_ALL));
                        } else if (state.selectionPanel instanceof PlaySOMPlayer) {
                            PlaySOMPlayer play = (PlaySOMPlayer) state.selectionPanel;

                            synchronized (play) {
                                play.clearList();
                                Unit[] map = state.growingLayer.getAllUnits();
                                for (String song : songs) {
                                    Unit u = mapContains(song, map);
                                    if (u != null) {
                                        play.addToList(song, u);
                                    } else {
                                        play.addToList(song, receivedFileNamePrefix, u);
                                    }
                                }
                                // This is now done in playerControl(...)
                                // play.startPlaying();
                            }
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private Unit mapContains(String song, Unit[] map) {
                for (Unit u : map) {
                    if (u != null) {
                        String[] songs = u.getMappedInputNames();
                        if (songs != null) {
                            for (String song2 : songs) {
                                if (song.equals(song2)) {
                                    return u;
                                }
                            }
                        }
                    }
                }
                return null;
            }
        }

    }

    /**
     * The TableModel for Users connected to the SOMViewer
     * 
     * @author Jakob Frank
     */
    class UserTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        private final String[] columnNames = { "User", "Color", "Paths", "" };

        private PNode remotePaths;

        private GarbageCollector gc;

        private List<UserTableRow> users;

        public UserTableModel() {
            remotePaths = new PNode();
            state.mapPNode.addChild(remotePaths);
            users = new ArrayList<UserTableRow>();

            gc = new GarbageCollector();
            gc.start();
        }

        public UserTableRow getRow(int i) {
            return users.get(i);
        }

        public void setSelected(int[] selectedRows) {
            for (UserTableRow r : users) {
                r.setHighlighted(false);
            }

            for (int i : selectedRows) {
                users.get(i).setHighlighted(true);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            gc.shutdown();
            super.finalize();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return users.size();
        }

        protected PNode addPath(String username, Color color) {
            UserTableRow u = null;
            synchronized (users) {
                // for (int i = 0; i < users.size(); i++) {
                // UserTableRow utr = (UserTableRow) users.get(i);
                // if (utr.username.equalsIgnoreCase(username)) {
                // u = utr;
                // break;
                // }
                // }
                if (u == null) {
                    u = new UserTableRow(username, color);
                    users.add(u);
                    fireTableDataChanged();
                }
                if (u.color.getRGB() != color.getRGB()) {
                    u.changeColorTo(color);
                }
                PNode res = u.createPath();
                fireTableDataChanged();
                return res;
            }
        }

        protected PNode addPath(String username) {
            UserTableRow u = null;
            synchronized (users) {
                for (int i = 0; i < users.size(); i++) {
                    UserTableRow utr = users.get(i);
                    if (utr.username.equalsIgnoreCase(username)) {
                        u = utr;
                        break;
                    }
                }
                if (u == null) {
                    return null;
                }
                PNode res = u.createPath();
                fireTableDataChanged();
                return res;
            }
        }

        public void clearPaths(String username) {
            boolean dataChanged = false;
            synchronized (users) {
                for (int i = 0; i < users.size(); i++) {
                    UserTableRow utr = users.get(i);
                    if (utr.username.equalsIgnoreCase(username)) {
                        utr.clearPaths();
                        dataChanged = true;
                    }
                }
            }

            if (dataChanged) {
                fireTableDataChanged();
            }

        }

        public void clearPaths(int index) {
            synchronized (users) {
                users.get(index).clearPaths();
            }
            fireTableDataChanged();
        }

        public void clearAllPaths() {
            for (int i = 0; i < users.size(); i++) {
                UserTableRow utr = users.get(i);
                utr.clearPaths();
            }
            fireTableDataChanged();
        }

        public void removeUser(String username) {
            UserTableRow u = null;
            synchronized (users) {
                for (int i = 0; i < users.size(); i++) {
                    UserTableRow utr = users.get(i);
                    if (utr.username.equalsIgnoreCase(username)) {
                        u = utr;
                        break;
                    }
                }

                if (u != null) {
                    u.clearPaths();
                    users.remove(u);
                    fireTableDataChanged();
                }
            }

        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 3;
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            final UserTableRow utr = users.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return utr.username;
                case 1:
                    return utr.color;
                case 2:
                    return new Integer(utr.paths);
                case 3:
                    JButton btn = new JButton("Delete");
                    btn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            UserTableModel.this.clearPaths(rowIndex);
                        }
                    });
                    return btn;
                default:
                    // nop
            }
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                default:
                    return String.class;
                case 1:
                    return Color.class;
                case 2:
                    return Integer.class;
                case 3:
                    return JButton.class;
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        class UserTableRow {
            private static final float HIGHLIGHT_FACTOR = 1.6f;

            private String username;

            private Color color;

            private Date timestamp;

            private int paths;

            private PNode node;

            private boolean highlighted = false;

            public UserTableRow(String username, Color color) {
                this.username = username;
                this.color = color;
                paths = 0;
                node = new PNode();

                remotePaths.addChild(node);
                timestamp = new Date();
            }

            public void reversePaths() {
                PNode newNode = new PNode();
                for (int i = 0; i < node.getChildrenCount(); i++) {
                    PLine l = (PLine) node.getChild(i);
                    PLine nl = new PLine();
                    for (int j = 0; j < l.getPointCount(); j++) {
                        nl.addPoint(0, l.getPoint(j, new Point2D.Double()).getX(),
                                l.getPoint(j, new Point2D.Double()).getY());
                    }
                    nl.setStroke(l.getStroke());
                    newNode.addChild(nl);
                }
                node = newNode;
            }

            public void changeColorTo(Color newColor) {
                this.color = newColor;
                ListIterator<?> i = node.getChildrenIterator();
                while (i.hasNext()) {
                    PNode n = (PNode) i.next();
                    if (n instanceof PPath) {
                        PPath p = (PPath) n;
                        p.setStrokePaint(newColor);
                    }
                }
            }

            public void setHighlighted(boolean highlight) {
                if (highlighted == highlight) {
                    return;
                }
                highlighted = highlight;
                float factor = 1 / HIGHLIGHT_FACTOR;
                if (highlight) {
                    factor = HIGHLIGHT_FACTOR;
                }

                ListIterator<?> i = node.getChildrenIterator();
                while (i.hasNext()) {
                    PNode n = (PNode) i.next();
                    if (n instanceof PPath) {
                        PPath p = (PPath) n;
                        float width = ((BasicStroke) p.getStroke()).getLineWidth();
                        // System.out.printf("%f, %f --> %f%n", width, factor, width * factor);
                        p.setStroke(new BasicStroke(width * factor));
                    }
                }
            }

            public UserTableRow(String username) {
                this(username, Color.CYAN);
            }

            public PNode createPath() {
                timestamp = new Date();
                paths++;
                return node;
            }

            public void clearPaths() {
                timestamp = new Date();
                node.removeAllChildren();
                node.repaint();
                paths = 0;
            }

        }

        private class GarbageCollector extends Thread {
            private boolean running;

            private int interval;

            public GarbageCollector() {
                this.setName("PathGarbageCollector");
                running = true;
                interval = 1000;
            }

            @Override
            public void run() {
                while (running) {
                    Date now = new Date();
                    List<UserTableRow> toRemove = new ArrayList<UserTableRow>();
                    for (int i = 0; i < users.size(); i++) {
                        UserTableRow utr = users.get(i);
                        if (utr.timestamp.getTime() + LIFETIME * 1000 < now.getTime()) {
                            toRemove.add(utr);
                        } else if (utr.paths <= 0) {
                            toRemove.add(utr);
                        }

                    }
                    if (toRemove.size() > 0) {
                        for (int i = 0; i < toRemove.size(); i++) {
                            toRemove.get(i).clearPaths();
                        }
                        synchronized (users) {
                            users.removeAll(toRemove);
                        }
                        fireTableDataChanged();
                    }
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                    }
                }
            }

            public void shutdown() {
                running = false;
            }
        }

    }
}
