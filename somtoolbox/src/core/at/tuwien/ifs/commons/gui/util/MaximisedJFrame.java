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
package at.tuwien.ifs.commons.gui.util;

import java.awt.Dimension;

import javax.swing.JFrame;

import at.tuwien.ifs.somtoolbox.util.UiUtils;

/**
 * A {@link JFrame} that will be maximised in size
 * 
 * @author Rudolf Mayer
 * @version $Id: MaximisedJFrame.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class MaximisedJFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    protected Dimension screenSize;

    public MaximisedJFrame(String title) {
        super(title);
    }

    public MaximisedJFrame() {
        super();
    }

    @Override
    public Dimension getPreferredSize() {
        if (screenSize == null) {
            screenSize = UiUtils.getMaxUsableScreenSize();
        }
        return screenSize;
    }

}
