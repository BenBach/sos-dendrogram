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
package at.tuwien.ifs.commons.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

import at.tuwien.ifs.commons.gui.SOMToolboxAppChooser.MainRadioButton.MainRadioButtonModel;
import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.commons.gui.jsap.GenericGUI;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp.Type;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.SubClassFinder;
import at.tuwien.ifs.somtoolbox.util.UiUtils;

/**
 * @author Jakob Frank
 * @author Rudolf Mayer
 * @version $Id: SOMToolboxAppChooser.java 3876 2010-11-02 15:10:17Z frank $
 */
public class SOMToolboxAppChooser extends JFrame {

    private static final long serialVersionUID = 1L;

    private final String[] args;

    private JButton btnCancel = null;

    private JButton btnLaunch = null;

    private JLabel lblSelect = null;

    private JTextArea txtDescription = null;

    private ButtonGroup bgMainApps = new ButtonGroup();

    private ArrayList<Class<? extends SOMToolboxApp>> runnables;

    private ArrayList<TitledCollapsiblePanel> applicationTypePanels = new ArrayList<TitledCollapsiblePanel>();

    /**
     * @param runnables {@link ArrayList} of runnables to show.
     */
    public SOMToolboxAppChooser(ArrayList<Class<? extends SOMToolboxApp>> runnables) {
        this(runnables, new String[] {});
    }

    /**
     * @param runnables {@link ArrayList} of runnables to show.
     * @param args command line arguments to pass along.
     */
    public SOMToolboxAppChooser(ArrayList<Class<? extends SOMToolboxApp>> runnables, String[] args) {
        Collections.sort(runnables, SOMToolboxApp.TYPE_GROUPED_COMPARATOR);
        this.runnables = runnables;
        this.args = args;

        initialize();
    }

    private void initialize() {
        this.setTitle("ApplicationLauncher");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new GridBagLayout());

        GridBagConstraintsIFS gc = new GridBagConstraintsIFS(GridBagConstraints.CENTER, GridBagConstraints.BOTH);
        gc.setInsets(new Insets(10, 10, 0, 10));

        lblSelect = new JLabel("Select Application to start:");
        lblSelect.setFont(UiUtils.scaleFont(lblSelect, 1.4));
        add(lblSelect, gc);

        add(getPanelMainClass(), gc.nextRow());
        add(getTxtDescription(), gc.nextRow());

        JPanel panelButtons = new JPanel();
        panelButtons.add(getBtnCancel());
        panelButtons.add(getBtnLaunch());
        add(panelButtons, gc.nextRow());

        this.getRootPane().setDefaultButton(getBtnLaunch());

        // to determine a good size, we set the biggest panel as visible
        TitledCollapsiblePanel firstPanel = applicationTypePanels.get(0);

        // 1. find the largest panel
        TitledCollapsiblePanel largestPanel = firstPanel;
        for (TitledCollapsiblePanel panel : applicationTypePanels) {
            if (panel.getContentPane().getComponentCount() > largestPanel.getContentPane().getComponentCount()) {
                largestPanel = panel;
            }
        }

        // 2. set the largest as visible
        firstPanel.setAnimated(false);
        largestPanel.setAnimated(false);
        largestPanel.setCollapsed(false);

        // 3. run pack
        this.pack();

        // 4. set the first panel as visible
        firstPanel.setCollapsed(false);
        largestPanel.setCollapsed(true);
        firstPanel.setAnimated(true);
        largestPanel.setAnimated(true);
    }

    public static class MainRadioButton extends JRadioButton {
        private static final long serialVersionUID = 2L;

        public MainRadioButton(Class<? extends SOMToolboxApp> app) {
            super(app.getSimpleName());
            setModel(new MainRadioButtonModel(app));
        }

        public static class MainRadioButtonModel extends JToggleButton.ToggleButtonModel {
            private static final long serialVersionUID = 1L;

            final private Class<? extends SOMToolboxApp> app;

            public MainRadioButtonModel(Class<? extends SOMToolboxApp> app) {
                this.app = app;
            }

            public Class<? extends SOMToolboxApp> getApp() {
                return app;
            }
        }
    }

    private JPanel getPanelMainClass() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraintsIFS gcMain = new GridBagConstraintsIFS(GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH);

        TitledCollapsiblePanel panel = null;
        GridBagConstraintsIFS gc = new GridBagConstraintsIFS(GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH);
        gc.setWeights(1, 1);

        Type lastType = null;

        int cols = 2; // how many radio buttons will appear next to each other
        int counter = 0;
        for (int i = 0; i < runnables.size(); i++) {
            Class<? extends SOMToolboxApp> c = runnables.get(i);
            Type type = Type.getType(c);

            if (type != lastType) { // start a new panel
                panel = new TitledCollapsiblePanel(type.toString(), new GridBagLayout(), true);
                panel.addPropertyChangeListener("collapsed", new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getNewValue().equals(Boolean.FALSE)) { // only react when a panel is de-collapsed
                            // otherwise, we react also on the subsequent event of closing the previous open panel
                            for (TitledCollapsiblePanel otherPanel : applicationTypePanels) {
                                // close all other, not collapsed panels
                                if (otherPanel != evt.getSource() && !otherPanel.isCollapsed()) {
                                    otherPanel.setCollapsed(true);
                                }
                            }
                        }
                    }
                });

                mainPanel.add(panel, gcMain.nextRow());
                applicationTypePanels.add(panel);
                lastType = type;
                counter = 0;
            }

            if (counter % cols == 0) {
                counter = 0;
                gc.nextRow();
            }
            MainRadioButton rb = new MainRadioButton(c);
            panel.add(rb, gc.nextCol());
            bgMainApps.add(rb);
            rb.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    MainRadioButtonModel sModel = (MainRadioButtonModel) ((MainRadioButton) e.getSource()).getModel();
                    String desk = "";
                    try {
                        Field f = sModel.app.getDeclaredField("LONG_DESCRIPTION");
                        desk = (String) f.get(null);
                    } catch (Exception err) {
                        try {
                            Field f = sModel.app.getDeclaredField("DESCRIPTION");
                            desk = (String) f.get(null);
                        } catch (Exception err2) {
                        }
                    }
                    getTxtDescription().setText(desk);
                    getTxtDescription().setVisible(true);
                    getBtnLaunch().setEnabled(true);
                }
            });
            counter++;

        }
        return mainPanel;
    }

    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setText("Exit");
            btnCancel.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    SOMToolboxAppChooser.this.setVisible(false);
                    SOMToolboxAppChooser.this.dispose();
                    System.exit(0);
                }
            });
        }
        return btnCancel;
    }

    private JButton getBtnLaunch() {
        if (btnLaunch == null) {
            btnLaunch = new JButton();
            btnLaunch.setText("Launch");
            btnLaunch.setEnabled(false);
            btnLaunch.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    MainRadioButtonModel selection = (MainRadioButtonModel) bgMainApps.getSelection();
                    if (selection == null) {
                        return;
                    }

                    Class<? extends SOMToolboxApp> c = selection.getApp();
                    if (c == null) {
                        return;
                    }

                    new GenericGUI(c, args).setVisible(true);
                    SOMToolboxAppChooser.this.setVisible(false);
                    SOMToolboxAppChooser.this.dispose();
                }
            });
        }
        return btnLaunch;
    }

    private JTextArea getTxtDescription() {
        if (txtDescription == null) {
            txtDescription = new JTextArea();
            txtDescription.setWrapStyleWord(true);
            txtDescription.setLineWrap(true);
            txtDescription.setEditable(false);
            txtDescription.setBackground(this.getBackground());
            txtDescription.setBorder(BorderFactory.createTitledBorder("Description"));
            txtDescription.setRows(5);
        }
        return txtDescription;
    }

    public static void main(String[] args) {
        new SOMToolboxAppChooser(SubClassFinder.findSubclassesOf(SOMToolboxApp.class, true)).setVisible(true);
    }
}
