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
package at.tuwien.ifs.somtoolbox.doc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang.StringEscapeUtils;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp.Type;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.util.SubClassFinder;

/**
 * @author Rudolf Mayer
 * @version $Id: RunnablesReferenceCreator.java 3869 2010-10-21 15:56:09Z mayer $
 */
public class RunnablesReferenceCreator {
    public static void main(String[] args) {
        ArrayList<Class<? extends SOMToolboxApp>> runnables = SubClassFinder.findSubclassesOf(SOMToolboxApp.class, true);
        Collections.sort(runnables, SOMToolboxApp.TYPE_GROUPED_COMPARATOR);

        StringBuilder sbIndex = new StringBuilder(runnables.size() * 50);
        StringBuilder sbDetails = new StringBuilder(runnables.size() * 200);

        sbIndex.append("\n<table border=\"0\">\n");

        Type lastType = null;

        for (Class<? extends SOMToolboxApp> c : runnables) {
            try {
                // Ignore abstract classes and interfaces
                if (Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) {
                    continue;
                }

                Type type = Type.getType(c);
                if (type != lastType) {
                    sbIndex.append("  <tr> <td colspan=\"2\"> <h5> " + type + " Applications </h5> </td> </tr>\n");
                    sbDetails.append("<h2> " + type + " Applications </h2>\n");
                    lastType = type;
                }
                String descr = "N/A";
                try {
                    descr = (String) c.getDeclaredField("DESCRIPTION").get(null);
                } catch (Exception e) {
                }
                String longDescr = "descr";
                try {
                    longDescr = (String) c.getDeclaredField("LONG_DESCRIPTION").get(null);
                } catch (Exception e) {
                }

                sbIndex.append("  <tr>\n");
                sbIndex.append("    <td> <a href=\"#").append(c.getSimpleName()).append("\">").append(c.getSimpleName()).append(
                        "</a> </td>\n");
                sbIndex.append("    <td> ").append(descr).append(" </td>\n");
                sbIndex.append("  </tr>\n");

                sbDetails.append("<h3 id=\"").append(c.getSimpleName()).append("\">").append(c.getSimpleName()).append(
                        "</h3>\n");
                sbDetails.append("<p>").append(longDescr).append("</p>\n");

                try {
                    Parameter[] options = (Parameter[]) c.getField("OPTIONS").get(null);
                    JSAP jsap = AbstractOptionFactory.registerOptions(options);
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(os);
                    AbstractOptionFactory.printHelp(jsap, c.getName(), ps);
                    sbDetails.append("<pre>").append(StringEscapeUtils.escapeHtml(os.toString())).append("</pre>");
                } catch (Exception e1) { // we didn't find the options => let the class be invoked ...
                }

            } catch (SecurityException e) {
                // Should not happen - no Security
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        sbIndex.append("</table>\n\n");
        System.out.println(sbIndex);
        System.out.println(sbDetails);
    }

    public static boolean isMainApp(Class<? extends SOMToolboxApp> c) {
        return c.getPackage().getName().endsWith(".apps") || c.getPackage().getName().endsWith(".viewer")
                || c.getPackage().getName().endsWith(".models");
    }
}
