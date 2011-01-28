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

/** This class bundles utility methods related to HTML content generation. */
public class HTMLUtils {

    public static StringBuilder printTableHeader(Object... headerNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <thead>");
        sb.append("    <tr>");
        for (Object header : headerNames) {
            sb.append(" <th>" + header + "</th> ");
        }
        sb.append("    </tr>\n");
        sb.append("</thead>");
        return sb;
    }

    public static StringBuilder printTableRow(Object... contents) {
        return printTableRow(null, contents);
    }

    public static StringBuilder printTableRow(String[] classes, Object... contents) {
        return printTableRow(null, null, contents);
    }

    public static StringBuilder printTableRow(String[] classes, String[] styles, Object... contents) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <tr>\n");
        for (int i = 0; i < contents.length; i++) {
            Object object = contents[i];
            sb.append("    <td" + (classes != null ? "class=\"" + classes[Math.min(i, classes.length)] + "\"" : "")
                    + (styles != null ? "style=\"" + styles[Math.min(i, styles.length)] + "\"" : "") + ">" + object
                    + "</td>\n");
        }
        sb.append("</tr>\n");
        return sb;
    }
}
