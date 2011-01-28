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
package at.tuwien.ifs.somtoolbox.apps.initEval;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;

/**
 * Static class managing time measurement of training and initialization phase
 * 
 * @author Stefan Bischof
 * @author Leo Sklenitzka
 */
public class Measure {
    /** training start time in ms */
    private static long startTrain;

    /** initialization start time in ms */
    private static long startInitialization;

    /** duration of training in ms */
    private static long trainDuration;

    /** duration of initialization in ms */
    private static long initializationDuration;

    private static Layer layer;

    private static QualityMeasure qualitymeasure;

    private static InputData inputData;

    /**
     * Starts time measurement for training
     */
    public static void startTrain() {
        startTrain = getCurrentTime();
    }

    /**
     * Stops time measurement for training
     */
    public static void endTrain() {
        trainDuration = max(getDurationSince(startTrain), trainDuration);
    }

    /**
     * Starts time measurement for initialization
     */
    public static void startInitialization() {
        startInitialization = getCurrentTime();
    }

    /**
     * Stops time measurement for initialization
     */
    public static void endInitialization() {
        initializationDuration = max(getDurationSince(startInitialization), initializationDuration);
    }

    /**
     * @return Time needed for training
     */
    protected static long getTrainDuration() {
        return trainDuration;
    }

    /**
     * @return Time needed for initialization
     */
    protected static long getInitalizationDuration() {
        return initializationDuration;
    }

    protected static void reset() {
        startTrain = 0;
        startInitialization = 0;
        trainDuration = 0;
        initializationDuration = 0;
    }

    /**
     * Delegate method for getting the current time
     * 
     * @return the current time in ms
     */
    private static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public static void setLayer(Layer growingLayer) {
        layer = growingLayer;
    }

    protected static Layer getLayer() {
        return layer;
    }

    /**
     * @return difference between given time <code>start</code> and current time
     */
    private static long getDurationSince(long start) {
        return getCurrentTime() - start;
    }

    private static long max(long x, long y) {
        return x > y ? x : y;
    }

    public static void setQualityMeasure(QualityMeasure qm) {
        qualitymeasure = qm;

    }

    protected static QualityMeasure getQualityMeasure() {
        return qualitymeasure;
    }

    public static void setInputData(InputData data) {
        inputData = data;
    }

    public static InputData getInputData() {
        return inputData;
    }
}
