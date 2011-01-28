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
package at.tuwien.ifs.somtoolbox.reportgenerator.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @version $Id: ReportFileWriter.java 3888 2010-11-02 17:42:53Z frank $
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ReportFileWriter {

    private File outputFile = null;

    // FIXME: refactor this table-of-contents, uses a tree-structure to represent menu entries...
    private ArrayList TableofContents = new ArrayList();

    private ArrayList lastEntry = null;

    private ArrayList lastSubentry = null;

    private StringBuffer writer = null;

    // FIXME: use contants/enums for the type
    private int type; // 1== HTML, 2 == LATEX

    public ReportFileWriter(String filename, int Type) {
        this.type = Type;
        outputFile = new File(filename);
        try {
            outputFile.createNewFile();
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").info(
                    "Writing Report to " + outputFile.getAbsolutePath());
            // writer = new String();
            writer = new StringBuffer();
        } catch (IOException ioe) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "Unfortunately, there was an error while trying to open file '" + filename + "'. Reason: " + ioe);

        }
    }

    /**
     * write the given output to the file
     * 
     * @param string the string that shall be written to the file.
     */
    public void appendOutput(String string) {
        writer.append(string);
    }

    /** write the given content to the file */
    public void appendOutput(StringBuilder sb) {
        writer.append(sb);
    }

    /**
     * writes the string to the file, but first replaces some characters to their fitting latex representation
     * 
     * @param string the string that shall be written to the file
     */
    public void appendLatexOutput(String string) {

        // string = string.replaceAll("_", "\\\\_");
        string = string.replaceAll("#", "\\\\#");
        string = string.replaceAll("%", "\\\\%");
        // writer.concat(string);

    }

    /** closes the file and therefore finishes the output */
    public void finish() {
        try {
            this.writer.trimToSize();
            String Final = this.writeTableOfContents() + this.writer.toString();
            BufferedWriter buff = new BufferedWriter(new FileWriter(outputFile));
            buff.write(Final);
            buff.close();
        } catch (IOException ioe) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").warning(
                    "unfortunately, there was an error while I was trying to finish the file '" + outputFile
                            + "'. Reason: " + ioe);
        }
    }

    /** Writes a table of content entry to the register */
    public void writeTableofContentsEntry(String content) {
        ArrayList new_regPoint = new ArrayList();
        new_regPoint.add(content);
        this.TableofContents.add(new_regPoint);
        this.lastEntry = new_regPoint;
    }

    /** Writes a table of content entry to the subregister */
    public void writeTableofContentsSubEntry(String content) {
        ArrayList new_subregPoint = new ArrayList();
        new_subregPoint.add(content);
        this.lastEntry.add(new_subregPoint);
        this.lastSubentry = new_subregPoint;
    }

    /** Writes a table of content entry to the sub-subregister */
    public void writeTableofContentsSubSubEntry(String content) {
        ArrayList new_subsubregPoint = new ArrayList();
        new_subsubregPoint.add(content);
        this.lastSubentry.add(new_subsubregPoint);
    }

    public String writeTableOfContents() {
        String out = "";
        if (this.type == 1) {// HTML
            Object o;
            ArrayList a, a1;
            out += "<h1>Contents:</h1>";
            Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").info("Writing OutputFile");
            out += "<ul>";
            for (int i = 0; i < this.TableofContents.size(); i++) {
                ArrayList l1 = (ArrayList) TableofContents.get(i);
                for (int j = 0; j < l1.size(); j++) {
                    if (l1.get(j).getClass().getName().equals("java.util.ArrayList")) { // 1st Sub Level
                        out += "<ul><ul>";
                        ArrayList l2 = (ArrayList) l1.get(j);
                        for (int k = 0; k < l2.size(); k++) {
                            if (l2.get(k).getClass().getName().equals("java.util.ArrayList")) { // 2nd Sub Level
                                ArrayList l3 = (ArrayList) l2.get(k);
                                out += "<ul><ul><ul>";
                                for (int l = 0; l < l3.size(); l++) {
                                    out += "<a href =\"#" + (String) l3.get(l) + "\">" + (String) l3.get(l) + "</a>"; // 3rd
                                    // Sub
                                    // Level
                                }
                                out += "</ul></ul></ul>";
                            } else {
                                out += "<a href =\"#" + (String) l2.get(k) + "\">" + (String) l2.get(k) + "</a>";
                            }
                        }

                        out += "</ul></ul>";
                    } else {
                        out += "<a href =\"#" + (String) l1.get(j) + "\">" + (String) l1.get(j) + "</a>";
                    }
                }
            }
            out += "</ul>";
        } else {// LATEX

        }

        return out;
    }

}
