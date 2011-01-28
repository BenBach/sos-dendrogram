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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * (Based on AudioDecoder.java from jsresources.org)
 * 
 * @author Ewald Peiszer
 * @version $Id: DecoderThread.java 3888 2010-11-02 17:42:53Z frank $
 */
public class DecoderThread extends Thread {
    /**
     * Static data structure not to decode a file twice at the same time
     * <p>
     * That means that all files that are currently being decoded have been put into the structure together with the
     * DecoderThread as the value.
     */
    static protected LinkedHashMap<File, DecoderThread> lhmCurrentlyDecoding = new LinkedHashMap<File, DecoderThread>(); // <File,

    // DecoderThread>

    private File encodedFile;

    private File pcmFile;

    private int channel;

    private boolean stats;

    private PlaybackThread nt;

    public DecoderThread(PlaybackThread nt, File encF, int channel, boolean stats) throws FileNotFoundException {
        this.nt = nt;
        this.encodedFile = encF;
        this.channel = channel;
        this.stats = stats;
        if (!encodedFile.exists()) {
            throw new FileNotFoundException();
        }
        this.pcmFile = new File(getDecodedFileName(encodedFile));
        this.setPriority(Thread.MIN_PRIORITY);
    }

    public static String getDecodedFileName(File encF) {
        return Commons.sDecodedOutputDir + Commons.stripSuffix(encF.getName()) + Commons.DECODED_SUFFIX;
    }

    @Override
    public void run() {
        // is being currently processed?
        if (lhmCurrentlyDecoding.containsKey(encodedFile)) {
            Commons.log.finer("Already in process, waiting: " + encodedFile);
            // only wait for other thread to finish
            while (lhmCurrentlyDecoding.containsKey(encodedFile)) {
                try {
                    sleep(10000);
                } catch (InterruptedException ex1) {
                }
            }
            // finished, give msg to nodethread
            nt.decodingFinished(pcmFile, channel, stats, this);
        } else {

            // do it the normal way
            AudioInputStream ais = null;
            try {
                // Put into hashmap
                lhmCurrentlyDecoding.put(encodedFile, this);

                ais = AudioSystem.getAudioInputStream(encodedFile);
                ais = AudioSystem.getAudioInputStream(Commons.datalineformat, ais);

                AudioFormat.Encoding targetEncoding = AudioFormat.Encoding.PCM_SIGNED;
                AudioInputStream pcmAIS = AudioSystem.getAudioInputStream(targetEncoding, ais);
                AudioFileFormat.Type fileType = AudioFileFormat.Type.AU;

                int nWrittenBytes = AudioSystem.write(pcmAIS, fileType, pcmFile);
                Thread.sleep(300);
                // finished, give msg to nodethread
                nt.decodingFinished(pcmFile, channel, stats, this);
            } catch (ThreadDeath td) {
                System.out.println("Da haben wir aber noch mal Glück gehabt");
                throw td;
            } catch (Exception ex) {
                ex.printStackTrace();
                nt.decodingFailed(channel, stats);
                Commons.log.warning(ex.getMessage());
            } finally {
                lhmCurrentlyDecoding.remove(encodedFile);
            }
        }
    }

    public File getPcmFile() {
        return pcmFile;
    }

    public File getEncodedFile() {
        return encodedFile;
    }

}
