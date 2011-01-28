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

import java.util.Vector;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * @author Ewald Peiszer
 * @version $Id: LineListModel.java 3888 2010-11-02 17:42:53Z frank $
 */
public class LineListModel implements ListModel {
    static Mixer.Info[] aMixer;

    // LinkedHashMap lhm;

    public LineListModel() {
        // lhm = _lhm;

        // Determine which lines are to be used
        aMixer = AudioSystem.getMixerInfo();
        Vector<Info> vTemp = new Vector<Info>();
        Mixer.Info mi;
        String mi_name = null;
        for (int i = 0; i < aMixer.length; i++) {
            try {
                // Try to get a line
                mi = aMixer[i];
                SourceDataLine sdl = (SourceDataLine) AudioSystem.getMixer(mi).getLine(Commons.datalineformat_info);
                // if that was possible, we can use this line
                // unless its name contains "Primary" or "Java"
                mi_name = mi.getName();
                if (mi_name.indexOf("Java") != -1) {
                    throw new Exception("Contains 'Java'");
                }
                if (mi_name.indexOf("Primary") != -1) {
                    throw new Exception("Contains 'Primary'");
                }
                if (mi_name.indexOf("Primärer") != -1) {
                    throw new Exception("Contains 'Primaerer'");
                }
                vTemp.add(aMixer[i]);
            } catch (Exception ex) {
                // do not add this mixer
                Commons.log.fine("Skipping mixer #" + i + " (" + mi_name + "):" + ex.getMessage());
            }
        }

        // hack epei2. Einkommentieren, um k outputlines zu simulieren, auch wenn
        // man nur eine Soundkarte hat.
        /*
         * for (int i = 0; i < 12; i++) { vTemp.add(vTemp.get(0)); }
         */

        aMixer = new Mixer.Info[vTemp.size()];
        aMixer = vTemp.toArray(aMixer);
    }

    /**
     * addListDataListener
     * 
     * @param listDataListener ListDataListener TODO Diese javax.swing.ListModel-Methode implementieren
     */
    @Override
    public void addListDataListener(ListDataListener listDataListener) {
    }

    /**
     * getElementAt
     * 
     * @param _int int
     * @return Object
     */
    @Override
    public Object getElementAt(int _int) {
        if (_int >= 0 && _int < aMixer.length) {
            // Dieser Mixer bereits vergeben?
            // if (lhm.containsKey(""+_int))
            // return ">>>" + "#" + _int + ": " + aMixer[_int].toString() + "<<<"; // Markierung
            // else
            return getOurMixerIdString(_int);
        } else {
            return null;
        }
    }

    /**
     * getSize
     * 
     * @return int
     */
    @Override
    public int getSize() {
        return aMixer.length;
    }

    public static int getMixerCount() {
        return aMixer.length;
    }

    /**
     * removeListDataListener
     * 
     * @param listDataListener ListDataListener TODO Diese javax.swing.ListModel-Methode implementieren
     */
    @Override
    public void removeListDataListener(ListDataListener listDataListener) {
    }

    public static Mixer getOurMixerAt(int pos) {
        return AudioSystem.getMixer(aMixer[pos]);
    }

    public static Mixer.Info getOurMixerInfoAt(int pos) {
        return aMixer[pos];
    }

    public static String getOurMixerIdString(int pos) {
        return "#" + pos + ": " + getOurMixerInfoAt(pos).getName();
    }
}
