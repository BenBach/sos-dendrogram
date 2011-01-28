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
package at.tuwien.ifs.somtoolbox.apps.viewer.fileutils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;

/**
 * A generic file filter specialised on displaying correct descriptions and file extensions for all
 * {@link SOMVisualisationData} types.
 * 
 * @author Rudolf Mayer
 * @version $Id: MySOMVisualisationDataFileFilter.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class MySOMVisualisationDataFileFilter extends FileFilter {
    private SOMVisualisationData data;

    public MySOMVisualisationDataFileFilter(SOMVisualisationData data) {
        this.data = data;
    }

    @Override
    public boolean accept(File pathname) {
        for (int i = 0; i < data.getExtensions().length; i++) {
            if (pathname.isDirectory() || pathname.getName().endsWith(data.getExtensions()[i])
                    || pathname.getName().endsWith(data.getExtensions()[i] + ".gz")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        String types = "";
        for (int i = 0; data.getExtensions() != null && i < data.getExtensions().length; i++) {
            if (types.length() > 0) {
                types += ", ";
            }
            if (data.getExtensions()[i] != null && data.getExtensions()[i].length() > 0) {
                types += "*." + data.getExtensions()[i] + ", " + "*." + data.getExtensions()[i] + ".gz";
            } else {
                types += "*";
            }
        }
        return data.getType() + " files (" + types + ")";
    }
}
