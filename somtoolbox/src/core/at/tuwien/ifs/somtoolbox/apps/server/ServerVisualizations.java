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
package at.tuwien.ifs.somtoolbox.apps.server;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizerInstance;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;

/**
 * @author Rudolf Mayer
 * @version $Id: ServerVisualizations.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class ServerVisualizations extends Visualizations {

    public static String getVisualisationsControl(BackgroundImageVisualizerInstance selected) {
        return getVisualisationsControl(selected.getVis(), selected.getVariant());
    }

    public static String getVisualisationsControl(ArrayList<BackgroundImageVisualizerInstance> availableVis,
            BackgroundImageVisualizerInstance selected) {
        System.out.println("ready visualisations:");
        System.out.println(ArrayUtils.toString(availableVis));
        System.out.println("selected vis: " + selected);
        StringBuffer b = new StringBuffer(availableVis.size() * 150);
        if (availableVis.size() > 1 && availableVis.size() <= 3) { // make radio buttons
            for (int i = 0; i < availableVis.size(); i++) {
                BackgroundImageVisualizerInstance vis = availableVis.get(i);
                b.append("<input type=\"radio\" name=\"visualisation\" onchange=\"this.form.submit()\" value=\""
                        + vis.getVis().getVisualizationShortName(vis.getVariant()) + "\"");
                System.out.println("vis: " + vis + "[" + vis.hashCode() + "], selected: " + selected + "["
                        + selected.hashCode() + "]");
                if (vis.equals(selected)) {
                    b.append(" checked=\"checked\" ");
                }
                b.append(">" + vis.getDisplayName() + "\n");
            }
        } else {// make a select drop down
            b.append("<select name=\"visualisation\" onchange=\"this.form.submit()\">\n");
            for (int i = 0; i < availableVis.size(); i++) {
                BackgroundImageVisualizerInstance vis = availableVis.get(i);
                b.append("<option value=\"" + vis.getVis().getVisualizationShortName(vis.getVariant()) + "\"");
                if (vis.equals(selected)) {
                    b.append(" selected ");
                }
                b.append(">" + vis.getDisplayName() + "</option>\n");
            }
            b.append("</select>\n");
        }
        return b.toString();
    }

    public static String getVisualisationsControl(BackgroundImageVisualizer selected, int selectedVariant) {
        BackgroundImageVisualizer[] vis = getReadyVisualizations();
        System.out.println("ready visualisations:");
        System.out.println(ArrayUtils.toString(vis));
        StringBuffer b = new StringBuffer(vis.length * 150);
        if (vis.length <= 3) { // make radio buttons
            for (BackgroundImageVisualizer vi : vis) {
                for (int j = 0; j < vi.getNumberOfVisualizations(); j++) {
                    b.append("<input type=\"radio\" name=\"visualisation\" onchange=\"this.form.submit()\" value=\""
                            + vi.getVisualizationName(j) + "\"");
                    if (vi == selected && selectedVariant == j) {
                        b.append(" checked=\"checked\" ");
                    }
                    b.append(">" + vi.getVisualizationName(j) + "\n");
                }
            }
        } else {// make a select drop down
            b.append("<select name=\"visualisation\" onchange=\"this.form.submit()\">\n");
            for (BackgroundImageVisualizer vi : vis) {
                for (int j = 0; j < vi.getNumberOfVisualizations(); j++) {
                    b.append("<option value=\"" + vi.getVisualizationName(j) + "\"");
                    if (vi == selected && selectedVariant == j) {
                        b.append(" selected ");
                    }
                    b.append(">" + vi.getVisualizationName(j) + "</option>\n");
                }
            }
            b.append("</select>\n");
        }
        return b.toString();
    }

    public static String getVisualisationsControl(String selected) {
        return getVisualisationsControl(getVisualizationByName(selected).getVis(),
                getVisualizationByName(selected).getVariant());
    }
}
