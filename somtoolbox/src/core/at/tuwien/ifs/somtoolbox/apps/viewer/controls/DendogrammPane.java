/*
 * Copyright 2004-2010 Institute of Software Technology and Interactive Systems, Vienna University of Technology
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

import java.awt.GridLayout;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;

/**
 * @author Ben
 * @version $Id: $
 */
public class DendogrammPane extends AbstractViewerControl {
    /**
     * @param title
     * @param state
     */
    protected DendogrammPane(String title, CommonSOMViewerStateData state) {
        super(title, state, new GridLayout(1, 1));
        // TODO Auto-generated constructor stub
    }

    private static final long serialVersionUID = 1L;

}
