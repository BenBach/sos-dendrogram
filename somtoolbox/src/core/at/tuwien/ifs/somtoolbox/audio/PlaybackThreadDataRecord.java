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
package at.tuwien.ifs.somtoolbox.audio;

import java.awt.Point;
import java.util.Arrays;
import java.util.Vector;

/**
 * Record type for data for one PlaybackThread.<br>
 * Contains:
 * <ul>
 * <li>position of the two speakers
 * <li>Lists of music files
 * </ul>
 * 
 * @author Ewald Peiszer
 * @version $Id: PlaybackThreadDataRecord.java 3583 2010-05-21 10:07:41Z mayer $
 */

public class PlaybackThreadDataRecord {
    /**
     * Positions of speakers. Array is initialized at the time the object is created.<br>
     * However, both entries ([0] and [1]) are <code>null</code> until
     * {@link PlaybackThreadDataRecord#setPosition(int, int, int)} is called.
     */
    public Point[] position = new Point[2];

    /**
     * Lists of songs to play. Array is initialized at the time the object is created.<br>
     * However, both entries ([0] and [1]) are <code>null</code> until
     * {@link PlaybackThreadDataRecord#addSongs(int, String[])} is called.
     */
    @SuppressWarnings("unchecked")
    public Vector<String>[] listOfSongs = new Vector[2];

    /**
     * Sets the position of one speaker
     * 
     * @return false if the position has already be set earlier, true otherwise
     */
    public boolean setPosition(int channel, int x, int y) {
        if (position[channel] == null) {
            position[channel] = new Point(x, y);
            return true;
        } else {
            position[channel].setLocation(x, y);
            return false;
        }
    }

    public boolean addSongs(int channel, String[] songNames) {
        if (listOfSongs[channel] == null) {
            listOfSongs[channel] = new Vector<String>();
            listOfSongs[channel].addAll(Arrays.asList(songNames));
            return true;
        } else {
            listOfSongs[channel].addAll(Arrays.asList(songNames));
            return false;
        }
    }

}