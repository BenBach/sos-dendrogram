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
package at.tuwien.ifs.somtoolbox.summarisation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.ui.tabbedui.VerticalLayout;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.summarisation.gui.NavigationPanel;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: SummariserGUI.java 3883 2010-11-02 17:13:23Z frank $
 */
public class SummariserGUI extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    public JScrollPane scrollP = new JScrollPane();

    public SummariserGUI(JFrame parent, CommonSOMViewerStateData state, Object[] itemName) {
        super(parent);
        setSize(new Dimension(850, 690));
        setTitle("Summarizer");

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollP, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new VerticalLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(this);

        leftPanel.add(new NavigationPanel(this, state, itemName));
        leftPanel.add(closeButton);
        getContentPane().add(leftPanel, BorderLayout.WEST);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
    }

}
