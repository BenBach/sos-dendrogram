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

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;

import at.tuwien.ifs.commons.gui.controls.TitledCollapsiblePanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;

/**
 * This class implements basic functionality for a control panel in the SOMViewer application. All control elements
 * should extend this class.
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: AbstractViewerControl.java 3885 2010-11-02 17:19:10Z frank $
 */
public abstract class AbstractViewerControl extends TitledCollapsiblePanel {

    private static final long serialVersionUID = 2L;

    protected final CommonSOMViewerStateData state;

    protected static final Font smallFont = new Font("Tahoma_small", Font.PLAIN, 9);

    protected static final Font smallerFont = new Font("Tahoma_small", Font.PLAIN, 8);

    protected static final Insets SMALL_INSETS = new Insets(2, 5, 1, 5);

    /**
     * Creates a new instance with the given title.
     * 
     * @param title The title of the control element.
     * @param state The som viewer state object.
     */
    protected AbstractViewerControl(String title, CommonSOMViewerStateData state) {
        super(title);
        this.state = state;

        initialSetup();
    }

    /**
     * 
     */
    private void initialSetup() {
        this.setTitleBackground(Color.decode("#c3d4e8"));
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    }

    /**
     * Creates a new instance with the given title and layout.
     * 
     * @param title The title of the control element.
     * @param state The som viewer state object.
     * @param layout The layout to be used for this control element.
     */
    protected AbstractViewerControl(String title, CommonSOMViewerStateData state, LayoutManager layout) {
        this(title, state);
        getContentPane().setLayout(layout);
    }

    /**
     * Sets the visibility of this control.
     * 
     * @see java.awt.Component#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (!visible) {
            ((SOMViewer) state.parentFrame).uncheckComponentInMenu(this);
        }
        super.setVisible(visible);
    }

    /**
     * Expands the panel and requests focus.
     */
    public void setSelected(boolean b) {
        if (b) {
            setCollapsed(false);
            requestFocus();
        }
    }

    public void setIcon(boolean b) {
        setCollapsed(b);
    }

    /**
     * Determines if the control is fully functional (e.g. all required data is available)
     * 
     * @return <code>true</code> by default, subclasses should overwrite this.
     */
    public boolean isFullFunctional() {
        return true;
    }

}
