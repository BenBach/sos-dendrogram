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
package at.tuwien.ifs.somtoolbox.visualization;

/**
 * Represents a specific instance of a background image visualizer, i.e. the visualizer class and the used variant.
 * 
 * @author Rudolf Mayer
 * @version $Id: BackgroundImageVisualizerInstance.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class BackgroundImageVisualizerInstance {
    private BackgroundImageVisualizer vis;

    private int variant;

    private String displayName;

    public BackgroundImageVisualizer getVis() {
        return vis;
    }

    public int getVariant() {
        return variant;
    }

    public BackgroundImageVisualizerInstance(BackgroundImageVisualizer vis, int variant) {
        super();
        this.vis = vis;
        this.variant = variant;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return vis.getVisualizationName(variant);
    }

    public String getShortName() {
        return vis.getVisualizationShortName(variant);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "Visualisation: '" + vis.getVisualizationName(variant) + "', displayed as '" + displayName + ",";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BackgroundImageVisualizerInstance)) {
            return false;
        }
        return ((BackgroundImageVisualizerInstance) obj).vis.equals(vis)
                && ((BackgroundImageVisualizerInstance) obj).variant == variant;
    }
}
