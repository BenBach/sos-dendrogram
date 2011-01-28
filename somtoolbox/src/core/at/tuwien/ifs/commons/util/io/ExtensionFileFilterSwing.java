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
package at.tuwien.ifs.commons.util.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.StringUtils;

/**
 * A generic swing {@link FileFilter} that accepts directories, and files with any of the supplied correct extensions,
 * or gzipped files with the correct extension prefix. If no description is passed to the constructor, a generic one
 * will be generated.
 * 
 * @author Rudolf Mayer
 * @version $Id: ExtensionFileFilterSwing.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class ExtensionFileFilterSwing extends FileFilter {

    private String[] extensions;

    private String description;

    public ExtensionFileFilterSwing(String... extensions) {
        this.extensions = extensions;

        description = "";
        for (String extension : extensions) {
            if (StringUtils.isNotBlank(description)) {
                description += "; ";
            }
            description += "*." + extension;
            if (!extension.endsWith(".gz")) {
                description += ", *." + extension + ".gz";
            }
        }
    }

    public ExtensionFileFilterSwing(String description, String... extensions) {
        this.extensions = extensions;
        this.description = description;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        for (String extension : extensions) {
            if (f.getName().endsWith("." + extension) || f.getName().endsWith("." + extension + ".gz")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
