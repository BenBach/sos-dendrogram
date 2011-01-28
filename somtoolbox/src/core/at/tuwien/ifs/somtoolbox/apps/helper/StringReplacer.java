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
package at.tuwien.ifs.somtoolbox.apps.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * Replaces strings by a replacement in the given file.
 * 
 * @author Rudolf Mayer
 * @version $Id: StringReplacer.java 3675 2010-07-15 09:07:26Z frank $
 */
public class StringReplacer implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptReplace(true),
            OptionFactory.getOptReplacement(false), OptionFactory.getOptInputFileName(false),
            OptionFactory.getOptInputDirectory(false) };

    public static final String DESCRIPTION = "Replaces strings by a replacement in the given file";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Helper;

    public static void main(String[] args) throws IOException {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);
        String toReplace = config.getString("replace");
        String replacement = config.getString("replacement", "");

        String fileName = config.getString("inputFile");
        String directoryName = config.getString("inputDirectory");

        if (fileName != null) {
            replaceInFile(toReplace, replacement, fileName);
        } else if (directoryName != null) {
            String[] files = new File(directoryName).list();
            for (String file : files) {
                toReplace = replaceInFile(toReplace, replacement, file);
            }
        } else {
            System.out.println("You should specificy either the input file or directory!");
        }
    }

    private static String replaceInFile(String toReplace, String replacement, final String fileName)
            throws FileNotFoundException, IOException {
        System.out.println("Replacing '" + toReplace + "' with '" + replacement + "' in file '" + fileName + "'.");
        BufferedReader br = FileUtils.openFile("", fileName);
        BufferedWriter out;
        File file = new File(fileName + ".tmp");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        if (fileName.endsWith(".gz")) {
            out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(fileOutputStream)));
        } else {
            out = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
        }
        String line = null;
        if (toReplace.equals("+")) { // escape regex characters
            toReplace = "\\" + toReplace;
        }
        while ((line = br.readLine()) != null) {
            out.write(line.replaceAll(toReplace, replacement) + "\n");
        }
        br.close();
        out.flush();
        out.close();
        file.renameTo(new File(fileName));
        return toReplace;
    }
}
