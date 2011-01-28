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
package at.tuwien.ifs.somtoolbox.layers.metrics;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.SubClassFinder;

/**
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class Metrics {
    private static DistanceMetric[] singleton;

    private static Class<DistanceMetric>[] singletonClasses;

    @SuppressWarnings("unchecked")
    public static DistanceMetric[] getAvailableMetrics() {
        if (singleton == null) {
            // FIXME: this code is a simplified version of Visualizations#getAvailableVisualizations
            // FIXME: make a generic method that takes excluded class names and Strings

            ArrayList<DistanceMetric> metricClasses = new ArrayList<DistanceMetric>();
            ArrayList<String> metricClassNames = new ArrayList<String>();

            ArrayList<Class<? extends DistanceMetric>> metrics = SubClassFinder.findSubclassesOf(DistanceMetric.class);
            for (Class<? extends DistanceMetric> metric : metrics) {
                // Ignore abstract classes and interfaces
                if (Modifier.isAbstract(metric.getModifiers()) || Modifier.isInterface(metric.getModifiers())) {
                    continue;
                }

                try {
                    Constructor<? extends DistanceMetric> constructor = metric.getConstructor((Class<?>[]) null);
                    metricClasses.add(metric.newInstance());
                    metricClassNames.add(metric.getSimpleName());
                } catch (NoSuchMethodException e) {
                    // skipping classes that don't have a default constructor
                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.metrics").severe(
                            "Error loading metric class : " + metric.getName());
                    e.printStackTrace();
                }
            }
            if (metricClasses.size() == 0) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.metrics").severe(
                        "Did not find any matching metric classes. Aborting.");
                System.exit(-1);
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.metrics").info(
                        "Found " + metricClasses.size() + " metric classes.");
            }

            singleton = metricClasses.toArray(new DistanceMetric[metricClasses.size()]);
            Arrays.sort(singleton);

            singletonClasses = new Class[singleton.length];
            for (int i = 0; i < singleton.length; i++) {
                singletonClasses[i] = (Class<DistanceMetric>) singleton[i].getClass();
            }

            Logger.getLogger("at.tuwien.ifs.somtoolbox.metrics").info(
                    "Registered total of " + singleton.length + " metrics " + StringUtils.toString(metricClassNames)
                            + ".");
        }
        return singleton;
    }

    public static Class<DistanceMetric>[] getAvailableMetricClasses() {
        if (singletonClasses == null) {
            getAvailableMetrics();
        }
        return singletonClasses;
    }

}
