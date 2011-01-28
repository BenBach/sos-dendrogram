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
 * A singleton factory creating {@link ProgressListener ProgressListeners}. If an instance of a ProgressListener is set,
 * this instance is returned instead of creating a new one. This allows calling applications to set their own
 * ProgressListener, while preserving backwards compatibility.
 * 
 * @author Christoph Becker
 * @version $Id: ProgressListenerFactory.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class ProgressListenerFactory {

    private static ProgressListenerFactory me = null;

    private ProgressListener listener = null;

    /**
     * @param numIterations maxIterations
     * @return the singleton progress listener
     */
    public ProgressListener createProgressListener(int numIterations, String iteration) {
        return createProgressListener(numIterations, iteration, 1);
    }

    public ProgressListener createProgressListener(int numIterations, String iteration, int stepWidth) {
        if (listener != null) {
            return listener;
        }
        return new StdErrProgressWriter(numIterations, iteration, stepWidth);
    }

    public ProgressListener createProgressListener(int numIterations, String iteration, int stepWidth, int newLineWidth) {
        if (listener != null) {
            return listener;
        }
        return new StdErrProgressWriter(numIterations, iteration, stepWidth, newLineWidth);
    }

    public static ProgressListenerFactory getInstance() {
        if (me == null) {
            me = new ProgressListenerFactory();
        }
        return me;
    }

    public ProgressListener getListener() {
        return listener;
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

}
