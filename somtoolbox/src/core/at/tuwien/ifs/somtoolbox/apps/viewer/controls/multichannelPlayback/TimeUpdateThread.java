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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback;

/**
 * updates the time displayed in the control frame, interval: 1 second
 * 
 * @author Ewald Peiszer
 * @version $Id: TimeUpdateThread.java 3358 2010-02-11 14:35:07Z mayer $
 */

public class TimeUpdateThread extends Thread {

    private boolean bQuitLoop = false;

    private boolean bPaused = false;

    private long lPauseStarted;

    /** Duration of all pauses accumulated in millisecs */
    private long lPausesDuration = 0;

    public TimeUpdateThread() {
        this.setName(getClass().getSimpleName());
        this.setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run() {
        while (!bQuitLoop) {
            if (Commons.cf != null && !bPaused) {
                Commons.cf.updateTime(lPausesDuration);
            }
            try {
                sleep(1000); // wait one second
            } catch (InterruptedException ex) {
            }
        }
    }

    public void stop_it() {
        bQuitLoop = true;
    }

    public void start_pause() {
        bPaused = true;
        lPauseStarted = System.currentTimeMillis();
    }

    public void end_pause() {
        bPaused = false;
        lPausesDuration += System.currentTimeMillis() - lPauseStarted;
    }

}