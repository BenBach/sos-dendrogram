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
package at.tuwien.ifs.commons.gui.jsap;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Option;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;

/**
 * @author frank
 * @version $Id: GenericGUI.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class GenericGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private final ArrayList<ParameterControl> argList;

    /**
     * Generate a {@link GenericGUI} based on the given {@link SOMToolboxApp}
     * 
     * @param app the {@link SOMToolboxApp}
     */
    public GenericGUI(final Class<? extends SOMToolboxApp> app) {
        this(app, new String[] {});
    }

    /**
     * Generate a {@link GenericGUI} based on the given {@link SOMToolboxApp}
     * 
     * @param app the {@link SOMToolboxApp}
     * @param args the command line arguments
     */
    public GenericGUI(Class<? extends SOMToolboxApp> app, String[] args) {
        argList = new ArrayList<ParameterControl>();
        initialize(app, args);
    }

    /**
     * Build and initialize the GUI
     * 
     * @param args initial settings for the params.
     */
    private void initialize(final Class<? extends SOMToolboxApp> app, String[] args) {
        setTitle(app.getSimpleName() + " - Config");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scroll, BorderLayout.CENTER);

        Parameter[] params;
        try {
            Field f = app.getField("OPTIONS");
            if (f.getType().equals(Parameter[].class)) {
                params = (Parameter[]) f.get(null);
            } else {
                params = new Parameter[0];
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            params = new Parameter[0];
        }

        JSAP jsap = new JSAP();

        for (Parameter parameter : params) {
            try {
                jsap.registerParameter(parameter);
            } catch (JSAPException e1) {
            }
        }
        JSAPResult result = jsap.parse(args);

        if (params.length > 0) {
            Arrays.sort(params, new Comparator<Parameter>() {
                @Override
                public int compare(Parameter p1, Parameter p2) {
                    boolean r1 = false, r2 = false;

                    if (p1 instanceof Option) {
                        r1 = ((Option) p1).required();
                    }
                    if (p2 instanceof Option) {
                        r2 = ((Option) p2).required();
                    }

                    if (r1 == r2) {
                        return 0;
                    } else {
                        if (r1) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                }
            });

            TitledCollapsiblePanel requiredParamsPanel = new TitledCollapsiblePanel("Required Parameter",
                    new GridBagLayout());
            TitledCollapsiblePanel optionalParamsPanel = new TitledCollapsiblePanel("Optional Parameter",
                    new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.anchor = GridBagConstraints.NORTH;
            contentPanel.add(requiredParamsPanel, c);
            c.gridy++;
            contentPanel.add(optionalParamsPanel, c);
            c.gridy++;
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            contentPanel.add(new JPanel(), c);

            TitledCollapsiblePanel current = requiredParamsPanel;
            for (Parameter parameter : params) {
                if (parameter instanceof Option) {
                    if (!((Option) parameter).required()) {
                        current = optionalParamsPanel;
                    }

                }
                addParameter(current, parameter, result);
            }
        } else {
            contentPanel.add(new JLabel("No command line arguments available"));
            this.addComponentListener(new ComponentAdapter() {

                @Override
                public void componentShown(ComponentEvent e) {
                    doProceed(app);
                }

            });
        }

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc0 = new GridBagConstraints();
        gbc0.gridx = 0;
        gbc0.anchor = GridBagConstraints.EAST;
        gbc0.gridy = 0;
        gbc0.insets = new Insets(2, 2, 2, 2);
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 1;
        gbc1.anchor = GridBagConstraints.EAST;
        gbc1.gridy = 0;
        gbc1.insets = new Insets(2, 2, 2, 2);
        JButton btnProceed = new JButton("Proceed");
        getRootPane().setDefaultButton(btnProceed);
        btnProceed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doProceed(app);
            }
        });

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GenericGUI.this.dispose();
            }
        });

        southPanel.add(btnClose, gbc0);
        southPanel.add(btnProceed, gbc1);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        pack();
    }

    private void addParameter(TitledCollapsiblePanel contentPanel, Parameter parameter, JSAPResult result) {
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0;
        l.gridy = GridBagConstraints.RELATIVE;
        l.anchor = GridBagConstraints.EAST;
        l.insets = new Insets(2, 2, 2, 2);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.insets = new Insets(2, 2, 2, 2);

        try {
            ParameterControl conf = ParameterControl.createParameterControl(parameter, result);
            argList.add(conf);

            contentPanel.add(conf.getLabel(), l);
            contentPanel.add(conf.getEditor(), c);
        } catch (SOMToolboxException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param app the app to proceed
     */
    private void doProceed(final Class<? extends SOMToolboxApp> app) {
        if (validateInput()) {
            ArrayList<String> argL = new ArrayList<String>();
            for (ParameterControl ed : argList) {
                argL.addAll(Arrays.asList(ed.getCommandLine()));
            }

            System.out.print("Starting " + app.getName());
            final String[] args = argL.toArray(new String[argL.size()]);
            for (String a : args) {
                System.out.print(" " + a);
            }
            System.out.println();

            new Thread() {
                @Override
                public void run() {
                    try {
                        setVisible(false);
                        Method main = app.getDeclaredMethod("main", String[].class);
                        main.invoke(null, new Object[] { args });

                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            System.err.printf("Some args are missing%n");
        }

    }

    private boolean validateInput() {
        boolean valid = true;
        for (ParameterControl editor : argList) {
            valid &= editor.validate();
        }
        return valid;
    }

    /**
     * @param args [empty]
     */
    public static void main(String[] args) {
        new GenericGUI(SOMViewer.class).setVisible(true);
    }

}
