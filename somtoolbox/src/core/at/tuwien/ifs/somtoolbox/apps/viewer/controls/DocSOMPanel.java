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

import javax.swing.ListSelectionModel;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.DocViewPanel;

/**
 * This class provides the link to the {@link DocViewPanel}
 * 
 * @author Christoph Becker
 * @see at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractSelectionPanel
 * @version $Id: DocSOMPanel.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class DocSOMPanel extends AbstractSelectionPanel {
    private static final long serialVersionUID = 1L;

    /**
     * creates a new DocSOMPanel with the provided state, containing a simple list, nothing more, and inits the
     * selection listener
     */
    public DocSOMPanel(CommonSOMViewerStateData state) {
        super(new BorderLayout(), state, "DocSOM Control", 1);
        playlists[0].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        initGui();
        setVisible(true);
    }

    private void initGui() {
        addSingleListScrollPanel(null);
    }
}
