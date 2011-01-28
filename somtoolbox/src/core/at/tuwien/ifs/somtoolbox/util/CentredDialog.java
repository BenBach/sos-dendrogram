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
package at.tuwien.ifs.somtoolbox.util;

import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.Window;
import java.util.logging.Logger;

import javax.swing.JDialog;

/**
 * This class represents a JDialog that will always be centred on top of the owning JFrame.
 * 
 * @author Rudolf Mayer
 * @version $Id: CentredDialog.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class CentredDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public CentredDialog(Window owner, String title, boolean modal) {
        super(owner, title);
        setModal(modal);
    }

    private void centre() {
        try {
            // Get the owning window size
            Dimension ownerSize = getOwner().getSize();

            // Calculate the frame location
            int x = (ownerSize.width - getWidth()) / 2 + getOwner().getLocationOnScreen().x;
            int y = (ownerSize.height - getHeight()) / 2 + getOwner().getLocationOnScreen().y;

            // Set the new frame location
            setLocation(x, y);
        } catch (IllegalComponentStateException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Coudl not centre dialog: " + e.getMessage());
        }
    }

    @Override
    public void setSize(Dimension size) {
        setSize(size.width, size.height);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        centre();
    }

    @Override
    public void pack() {
        super.pack();
        centre();
    }

}
