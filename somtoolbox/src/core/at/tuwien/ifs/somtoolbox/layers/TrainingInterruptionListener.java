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
package at.tuwien.ifs.somtoolbox.layers;

/**
 * This interface should be implemented by classes that want to train a map with regular interruptions to perform
 * special operations during training.
 * 
 * @author Michael Dittenbach
 * @version $Id: TrainingInterruptionListener.java 3583 2010-05-21 10:07:41Z mayer $
 */
public interface TrainingInterruptionListener {

    /**
     * Is called when an interruption of the training process occurs.
     * 
     * @param currentIteration the iteration when the interruption occurred.
     * @param numIterations the target iteration number where the training will stop.
     */
    public void interruptionOccurred(int currentIteration, int numIterations);

}
