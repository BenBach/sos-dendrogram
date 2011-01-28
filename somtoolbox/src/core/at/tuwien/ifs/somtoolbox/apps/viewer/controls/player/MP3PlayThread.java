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

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;

/**
 * @author Jakob Frank
 */
public class MP3PlayThread extends AudioPlayThread {

    private Player player;

    /**
     * @param toPlay the {@link AudioVectorMetaData} to play
     * @param caller who to inform.
     */
    public MP3PlayThread(AudioVectorMetaData toPlay, PlayerListener caller) {
        super(toPlay, caller);
    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.AudioPlayThread#run()
     */
    @Override
    public boolean doPlaying() {
        try {
            player = new Player(new FileInputStream(song.getAudioFile()));
            if (player == null) {
                throw new Exception("Could not start player");
            }
            player.play();
        } catch (JavaLayerException e) {
            /*
             * This error happens with some mp3s. Since we can't go further, we played to the end.
             */
            if (e.getException() instanceof ArrayIndexOutOfBoundsException) {
                System.err.println("MP3-Player: ERROR in " + song.getID());
                return true;
            }
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
        }
        return player.isComplete();

    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.AudioPlayThread#stopPlaying()
     */
    @Override
    public void stopPlaying() {
        if (player != null) {
            player.close();
        }
    }

}
