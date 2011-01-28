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
package at.tuwien.ifs.somtoolbox.output;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.models.GHSOM;

/**
 * @author Michael Dittenbach
 * @version $Id: AttendeeMapper.java 3686 2010-07-15 09:16:12Z frank $
 */
public class AttendeeMapper implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptHighlightedDataNamesFile(true),
            OptionFactory.getOptLabeling(false), OptionFactory.getOptUnitDescriptionFile(true),
            OptionFactory.getOptHtmlFileName() };

    public static final String DESCRIPTION = "Writes an HTML output of the map, with highlighting certain data items";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Utils;

    /**
     * Method for stand-alone execution of the attendee mapper. Options are:<br>
     * <ul>
     * <li>-d data names file, mandatory</li>
     * <li>-u unit description file, mandatory</li>
     * <li>htmlName name of output HTML file, mandatory</li>
     * </ul>
     * 
     * @param args the execution arguments as stated above.
     */
    public static void main(String[] args) {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String dataNamesFileName = config.getString("dataNamesFile");
        String unitDescriptionFileName = config.getString("unitDescriptionFile");
        String htmlFileName = config.getString("htmlFile");

        GHSOM ghsom = null;
        try {
            ghsom = new GHSOM(new SOMLibFormatInputReader(null, unitDescriptionFileName, null));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }

        String[] dataNames = readDataNames(dataNamesFileName);

        String fDir = htmlFileName.substring(0, htmlFileName.lastIndexOf(System.getProperty("file.separator")) + 1);
        String fName = htmlFileName.substring(htmlFileName.lastIndexOf(System.getProperty("file.separator")) + 1);
        if (fName.endsWith(".html")) {
            fName = fName.substring(0, (fName.length() - 5));
        }

        try {
            new HTMLOutputter().write(ghsom, fDir, fName, dataNames);
        } catch (IOException e) { // TODO: create new exception type
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not open or write to output file " + htmlFileName + ": " + e.getMessage());
            System.exit(-1);
        }
    }

    private static String[] readDataNames(String fName) {
        ArrayList<String> tmpList = new ArrayList<String>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fName));
            String line = null;
            while ((line = br.readLine()) != null) {
                StringTokenizer strtok = new StringTokenizer(line, " \t", false);
                while (strtok.hasMoreTokens()) {
                    tmpList.add(strtok.nextToken());
                }
            }
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not open or read from file " + fName + " containing the data names. Aborting.");
            System.exit(-1);
        }
        return tmpList.toArray(new String[tmpList.size()]);
    }

}