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

/**
 * An interface defining a listener for progress messages. Currently {@link StdErrProgressWriter} is the only class
 * implementing this interface.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: ProgressListener.java 3583 2010-05-21 10:07:41Z mayer $
 */
public interface ProgressListener {

    /** Progress by the given steps, and change the message */
    public void progress(String message, int currentStep);

    /** Progress by the given steps. */
    public void progress(int currentStep);

    /** Progress by one step. */
    public void progress();

    /** Progress by one step, and change the message */
    public void progress(String message);

    public void insertRow(int rows, String message);

    public void insertColumn(int columns, String message);

    public int getCurrentStep();
}
