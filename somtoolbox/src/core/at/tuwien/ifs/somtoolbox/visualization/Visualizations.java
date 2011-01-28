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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMInputReader;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.SubClassFinder;

/**
 * @author Rudolf Mayer
 * @version $Id: Visualizations.java 3738 2010-08-05 17:25:03Z mayer $
 */
public class Visualizations {

    private static final String[] EXCLUDE_CLASSES = new String[] { "SmoothedCountHistograms",
            "SearchResultHistogramVisualizer" };

    public static BackgroundImageVisualizer[] singleton;

    public static int maxVariants = 0;

    private static Integer initFrom = null;

    public static BackgroundImageVisualizer[] getAvailableVisualizations() {
        if (singleton == null) {
            ArrayList<BackgroundImageVisualizer> visClasses = new ArrayList<BackgroundImageVisualizer>();
            ArrayList<String> visClassNames = new ArrayList<String>();

            ArrayList<Class<? extends BackgroundImageVisualizer>> viss = SubClassFinder.findSubclassesOf(BackgroundImageVisualizer.class);
            for (Class<? extends BackgroundImageVisualizer> vis : viss) {
                // Ignore abstract classes and interfaces
                if (Modifier.isAbstract(vis.getModifiers()) || Modifier.isInterface(vis.getModifiers())) {
                    continue;
                }

                // Ignore exclude classes
                if (StringUtils.equalsAny(vis.getSimpleName(), EXCLUDE_CLASSES)) {
                    continue;
                }

                // Ignore 3D vis (without directly referencing them)
                boolean is3DVis = false;
                final Class<?>[] interfaces = vis.getInterfaces();
                for (Class<?> i : interfaces) {
                    if (i.getSimpleName().equals("TerrainHeightGenerator")) {
                        is3DVis = true;
                        break;
                    }
                }
                if (is3DVis) {
                    continue;
                }

                try {
                    visClasses.add(vis.newInstance());
                    visClassNames.add(vis.getSimpleName());
                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").severe(
                            "Error loading visualisation class : " + vis.getName());
                    e.printStackTrace();
                }
            }
            if (visClasses.size() == 0) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").severe(
                        "Did not find any matching visualisation classes. Aborting.");
                System.exit(-1);
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info(
                        "Found " + visClasses.size() + " visualisation classes.");
            }

            singleton = visClasses.toArray(new BackgroundImageVisualizer[visClasses.size()]);
            Arrays.sort(singleton);

            Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info(
                    "Registered total of " + singleton.length + " visualisations "
                            + StringUtils.toString(visClassNames) + ".");
        }
        return singleton;
    }

    public static BackgroundImageVisualizer[] getReadyVisualizations() {
        ArrayList<BackgroundImageVisualizer> vis = new ArrayList<BackgroundImageVisualizer>();
        for (BackgroundImageVisualizer v : getAvailableVisualizations()) {
            if (ArrayUtils.isEmpty(v.needsAdditionalFiles())) { // only take vis that have all files
                vis.add(v);
                // System.out.println("adding " + v);
            } else {
                // System.out.println("skipping " + v);
            }
        }
        return vis.toArray(new BackgroundImageVisualizer[vis.size()]);
    }

    public static String[] getReadyVisualizationNames() {
        BackgroundImageVisualizer[] readyVisualizations = getReadyVisualizations();
        ArrayList<String> names = new ArrayList<String>(readyVisualizations.length * 2);
        for (BackgroundImageVisualizer readyVisualization : readyVisualizations) {
            BackgroundImageVisualizer vis = readyVisualization;
            for (int j = 0; j < vis.getNumberOfVisualizations(); j++) {
                names.add(readyVisualization.getVisualizationShortName(j));
            }
        }
        return names.toArray(new String[names.size()]);
    }

    public static void initVisualizations(SharedSOMVisualisationData inputObjects, SOMInputReader reader,
            int defaultPaletteIndex, Palette defaultPalette, Palette[] palettes) {
        initVisualizations(inputObjects, reader, null);
    }

    /**
     * Initialises all registered visualisation - sets the {@link SharedSOMVisualisationData} input objects, the
     * {@link MapPNode} map, and default palettes.
     */
    public static void initVisualizations(SharedSOMVisualisationData inputObjects, SOMInputReader reader, MapPNode map) {
        // only initialise if we did not do it before, or if we have new data (indicated by a new hashcode of
        // SharedSOMVisualisationData
        if (inputObjects != null && initFrom != null && inputObjects.dataHashCode() == initFrom) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info(
                    "Not initialising visualisations again, provided data is still the same.");
            return;
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info("Initialising visualisations...");
        initFrom = inputObjects.dataHashCode();
        maxVariants = 0;
        BackgroundImageVisualizer[] visualizations = getAvailableVisualizations();
        for (BackgroundImageVisualizer visualization : visualizations) {
            visualization.setSOMData(reader);
            visualization.setInputObjects(inputObjects);
            visualization.setMap(map);
            maxVariants = Math.max(maxVariants, visualization.getNumberOfVisualizations());
        }
    }

    /**
     * Tries to locate a visualisation by the given name.
     * 
     * @param name the name of the visualisation.
     * @return the visualisation matching the given name, or <code>null</code> otherwise.
     */
    public static BackgroundImageVisualizerInstance getVisualizationByName(String name) {
        BackgroundImageVisualizer[] availableVisualizations = getAvailableVisualizations();
        for (BackgroundImageVisualizer vis : availableVisualizations) {
            for (int j = 0; j < vis.getNumberOfVisualizations(); j++) {
                if (vis.getVisualizationName(j).equals(name) || vis.getVisualizationShortName(j).equals(name)) {
                    return new BackgroundImageVisualizerInstance(vis, j);
                }
            }
        }
        return null;
    }

    public static String getVisualizationShortName(String longName) {
        BackgroundImageVisualizerInstance vis = getVisualizationByName(longName);
        return vis.getVis().getVisualizationShortName(vis.getVariant());
    }

    public static String[] getAvailableVisualizationNames() {
        BackgroundImageVisualizer[] availableVisualizations = getAvailableVisualizations();
        ArrayList<String> names = new ArrayList<String>(availableVisualizations.length * 2);
        for (BackgroundImageVisualizer vis : availableVisualizations) {
            for (int j = 0; j < vis.getNumberOfVisualizations(); j++) {
                names.add(vis.getVisualizationName(j) + " (or " + vis.getVisualizationShortName(j) + " )");
            }
        }
        return names.toArray(new String[names.size()]);
    }

}
