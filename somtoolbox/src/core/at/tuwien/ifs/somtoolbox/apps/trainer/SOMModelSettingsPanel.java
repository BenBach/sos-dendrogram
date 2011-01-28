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
package at.tuwien.ifs.somtoolbox.apps.trainer;

import java.io.File;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.models.MnemonicSOM;

/**
 * @author Jakob Frank
 * @version $Id: SOMModelSettingsPanel.java 3868 2010-10-21 15:52:31Z mayer $
 */
public abstract class SOMModelSettingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public Properties getProperties() {
        return new Properties();
    }

    public String[] getAdditionalParams() {
        return new String[] {};
    }

    protected static File execFileChooser(JTextField target, FileFilter filter, boolean isToSave,
            boolean directorySelect) {
        File cwd = new File(target.getText());
        JFileChooser c = new JFileChooser(cwd);
        if (filter != null) {
            c.addChoosableFileFilter(filter);
            c.setFileFilter(filter);
        }
        if (directorySelect) {
            c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }

        int returnVal;
        if (isToSave) {
            returnVal = c.showSaveDialog(target);
        } else {
            returnVal = c.showOpenDialog(target);
        }

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = c.getSelectedFile();
            target.setText(file.getAbsolutePath());
            return file;
        }
        return null;
    }

    static SOMModelSettingsPanel createModelSpecificConfigPanel(Class<? extends AbstractNetworkModel> cls) {
        if (cls.equals(GrowingSOM.class)) {
            return new GrowingSOMSettingsPanel();
        } else if (cls.equals(GHSOM.class)) {
            return new GHSOMSettingsPanel();
        } else if (cls.equals(MnemonicSOM.class)) {
            return new MnemonicSOMSettingsPanel();
        } else {
            return null;
        }

    }
}
