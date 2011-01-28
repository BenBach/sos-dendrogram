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
 * Plays different audio files ("one", "two" etc) on each output line one after another.
 * 
 * @author Ewald Peiszer
 * @version $Id: FindMeLoopThread.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class FindMeLoopThread extends Thread {
    protected int iIndex = 0;

    boolean bStop = false;

    public FindMeLoopThread(int iStart) {
        this.setName(getClass().getSimpleName() + "-" + iStart);
        this.setPriority(Thread.MIN_PRIORITY);
        iIndex = iStart;
    }

    @Override
    public void run() {
        while (!bStop) {
            // Define array this way because Java kinda sucks sometimes
            // Assume that no spoken number for the current line index
            // exists, take generic sound file
            String[][] aFilesToPlay = { // epei2
            { Commons.FINDME_INTRO, Commons.FINDME_GENERIC, Commons.FINDME_LEFT },
                    { Commons.FINDME_INTRO, Commons.FINDME_GENERIC, Commons.FINDME_SILENCE, Commons.FINDME_RIGHT } };
            if (iIndex < Commons.A_FINDME_FILES.length) {
                // if the spoken soundfile exists, replace the array entries
                aFilesToPlay[0][1] = Commons.A_FINDME_FILES[iIndex]; // epei2
                aFilesToPlay[1][1] = Commons.A_FINDME_FILES[iIndex];
            }
            Commons.playSound(aFilesToPlay, iIndex);
            // increment index
            iIndex++;
            if (iIndex == LineListModel.aMixer.length) {
                iIndex = 0;
            }
            // Sleep, until playing should be finished
            // (it would be more elegent if I wait until it _is_ finished.)
            try {
                Thread.sleep(Commons.SLEEPFOR);
            } catch (InterruptedException ex) {
            }
        }
    }

    /** Sets a flag that will stop the thread at the next possible time */
    public void stopIt() {
        bStop = true;
    }

}