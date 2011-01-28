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

import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * The frame holding the {@link SOMPane}.
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMFrame.java 3873 2010-10-28 09:29:58Z frank $
 */
public class SOMFrame extends JInternalFrame implements InternalFrameListener {
    private static final long serialVersionUID = 1L;

    private CommonSOMViewerStateData state;

    public SOMFrame(CommonSOMViewerStateData state) {
        super("SOM", true, true, true, true);
        setName("SOM Frame");
        this.state = state;
        addInternalFrameListener(this);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        ((SOMViewer) state.parentFrame).uncheckComponentInMenu(this);
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
    }

}
