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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.VerticalLayout;

import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractViewerControl;

public class ControlCollector extends JPanel {// JInternalFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel content;

    private JMenu controlMenu = null;

    private ArrayList<AbstractViewerControl> defaultControls = new ArrayList<AbstractViewerControl>();

    public ControlCollector(String title, final CommonSOMViewerStateData state) {
        // super(title);
        content = new JPanel();
        content.setLayout(new VerticalLayout(1));
        JScrollPane scp = new JScrollPane(content);
        scp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        super.setLayout(new BorderLayout());
        super.add(scp, BorderLayout.CENTER);
    }

    /**
     * @param panel the panel to add
     * @deprecated use {@link #addControl(AbstractViewerControl, boolean)} instead.
     */
    @Deprecated
    public void addControl(TitledCollapsiblePanel panel) {
        content.add(panel);
        revalidate();
    }

    /**
     * @deprecated use {@link #addControl(AbstractViewerControl, boolean)} directly.
     */
    @Deprecated
    public void add(AbstractViewerControl avc) {
        addControl(avc, false);
    }

    @Deprecated
    public void add(TitledCollapsiblePanel comp) {
        addControl(comp);
    }

    @Deprecated
    public void add(TitledCollapsiblePanel comp, Object contraint) {
        addControl(comp);
    }

    /**
     * @deprecated use {@link #addControl(AbstractViewerControl, boolean)} directly.
     */
    @Deprecated
    public void add(AbstractViewerControl avc, Object constraints) {
        addControl(avc, false);
    }

    public void addControl(final AbstractViewerControl avc) {
        addControl(avc, false);
    }

    public void addControl(final AbstractViewerControl avc, boolean isDefaultControl) {
        addControl(avc, -1, isDefaultControl);
    }

    public void addControl(final AbstractViewerControl avc, int index) {
        addControl(avc, index, false);
    }

    public void addControl(final AbstractViewerControl avc, int index, boolean isDefaultControl) {
        content.add(avc, index);
        if (controlMenu != null) {
            controlMenu.add(createMenuEntry(avc), index);
        }
        if (isDefaultControl) {
            defaultControls.add(avc);
        }
        revalidate();
    }

    /**
     * Create and set up a {@link TitledCollapsiblePanel}. Set default colors/borders for Control-Elements.
     * 
     * @return a ready set up and decorated {@link TitledCollapsiblePanel}.
     */
    @SuppressWarnings("unused")
    private TitledCollapsiblePanel createCollapsibleControlPanel() {
        TitledCollapsiblePanel panel = new TitledCollapsiblePanel();

        panel.setTitleBackground(Color.decode("#c3d4e8"));
        // panel.setTitleBackground(Color.RED);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        return panel;
    }

    public JMenu getControlMenu(String title) {
        if (controlMenu == null) {
            controlMenu = new JMenu(title);

            for (Component c : content.getComponents()) {
                if (c instanceof AbstractViewerControl) {
                    final AbstractViewerControl avc = (AbstractViewerControl) c;
                    controlMenu.add(createMenuEntry(avc));
                }
            }
        }
        return controlMenu;
    }

    /**
     */
    private JCheckBoxMenuItem createMenuEntry(final AbstractViewerControl avc) {
        final JCheckBoxMenuItem chk = new JCheckBoxMenuItem(avc.getTitle(), avc.isVisible());
        chk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                avc.setVisible(chk.isSelected());
            }
        });
        avc.addPropertyChangeListener("visible", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!evt.getNewValue().equals(chk.isSelected())) {
                    chk.setSelected(evt.getNewValue().equals(Boolean.TRUE));
                }
            }
        });
        return chk;
    }

    public ArrayList<AbstractViewerControl> getDefaultControls() {
        return defaultControls;
    }

}
