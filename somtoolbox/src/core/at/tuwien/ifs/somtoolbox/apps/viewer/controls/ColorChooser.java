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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.util.CentredDialog;

/**
 * A generic color chooser dialog.
 * 
 * @author Angela Roiger
 * @author Rudolf Mayer
 * @version $Id: ColorChooser.java 3873 2010-10-28 09:29:58Z frank $
 */
public abstract class ColorChooser extends CentredDialog implements ChangeListener {

    private static final long serialVersionUID = 1L;

    protected JColorChooser cc = null;

    public ColorChooser(Window parent, Color color, String title) {
        super(parent, title, true);
        setLayout(new BorderLayout());
        cc = new JColorChooser(color);
        cc.getSelectionModel().addChangeListener(this);
        setTitle(title);
        getContentPane().add(cc, BorderLayout.CENTER);
        final JButton button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ColorChooser.this.setVisible(false);
                ColorChooser.this.dispose();
            }
        });
        getContentPane().add(button, BorderLayout.SOUTH);
        setSize(500, 300);
    }

    public Color getColor() {
        return cc.getColor();
    }

}