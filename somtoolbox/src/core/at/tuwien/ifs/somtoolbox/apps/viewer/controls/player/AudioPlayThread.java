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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.player;

import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;

/**
 * @author Jakob Frank
 */
public abstract class AudioPlayThread extends Thread {

    private final PlayerListener listener;

    protected final AudioVectorMetaData song;

    /**
     * @param song the Song that will be played.
     * @param someoneToInform someone to inform when the playing has ended.
     */
    public AudioPlayThread(AudioVectorMetaData song, PlayerListener someoneToInform) {
        listener = someoneToInform;
        this.song = song;
        this.setName(this.getClass().getSimpleName() + " (" + song.getID() + ")");
        // System.out.printf("Playing \"%s\" (%s)%n", title, audioFile.getAbsolutePath());
    }

    @Override
    public final void run() {
        this.setPriority(NORM_PRIORITY + (MAX_PRIORITY - NORM_PRIORITY) / 2);
        System.out.printf("Playing \"%s\" (%s)%n", song.getDisplayLabel(), song.getAudioFile().getAbsolutePath());
        boolean playedToEnd = doPlaying();
        if (playedToEnd) {
            listener.playStopped(PlayerListener.STOP_REASON_ENDED, song);
        } else {
            listener.playStopped(PlayerListener.STOP_REASON_STOPPED, song);
        }
    }

    /**
     * Play the audio file. This method must block until replay is finished.
     * 
     * @return <c>true</c> iff the file was played to the end, <c>false</c> otherwise.
     */
    public abstract boolean doPlaying();

    /**
     * Interrupt/Stop the playing. The method {@link #doPlaying()} should return <c>false</c> in this case.
     */
    public abstract void stopPlaying();

}
