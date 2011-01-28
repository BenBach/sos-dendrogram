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

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Represents two nodes / one output line
 * 
 * @author Ewald Peiszer
 * @version $Id: PlaybackThread.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class PlaybackThread extends Thread {
    String id;

    Point[] aPos = new Point[2];

    /** Files to play */
    File[][] aFiles = null;

    /** Flag if channel is empty: in this case, there will be silence on the respective channel */
    boolean[] aEmpty = new boolean[2];

    /**
     * Flag: if <code>true</code>, then the respective channel is waiting for a <code>DecoderThread</code> to finish.
     */
    boolean[] aWaitForDecoder = { false, false };

    /**
     * Flag: if true, then music is played endlessly, alwas repeating. Songs are picked in random order.
     * <p>
     * If false, then on each channel there will be played each song in the list, from the first to the last, then exit.
     */
    boolean bRepeat_Shuffle = true;

    /** Used as a pointer if <code>bRepeat_Shuffle</code> is false */

    /** Flag: if false, no global statistic variable will be updated by this Thread */
    boolean bUpdStats = true;

    /** Probability to decode a mp3 file to wav */
    float p_decode = Commons.p_decode;

    /**
     * (if bRepeat == false): if the first channel's musicfile stops, it is set to true.
     * <p>
     * If the second channel's musicfile stops, this thread is stopped.
     */
    boolean bOtherChannelAlreadyFinished = false;

    private boolean threadSuspended = false;

    protected File file1, file2;

    protected SourceDataLine line = null;

    protected AudioInputStream[] ain = new AudioInputStream[2]; // We read audio data from here

    protected boolean ready = false;

    protected boolean bQuitLoop = false;

    // Allocate a buffer for reading from the input stream and writing
    // to the line. Make it large enough to hold 4k audio frames.
    // Note that the SourceDataLine also has its own internal buffer.
    // int framesize = datalineformat.getFrameSize( ); // sollte 4 sein
    protected int datalineframesize = Commons.datalineformat.getFrameSize(); // sollte 4 sein

    protected int monoframesize = Commons.monoformat.getFrameSize(); // sollte 2 sein

    protected byte[][] buffer = { new byte[1 * 1024 * datalineframesize], new byte[1 * 1024 * datalineframesize] }; // the

    // buffer

    /*
     * protected byte[] buffer1 = new byte[1 * 1024 * monoframesize]; // the buffer protected byte[] buffer2 = new byte[1 * 1024 * monoframesize]; //
     * the buffer
     */

    protected byte[] datalinebuffer = new byte[1 * 1024 * datalineframesize]; // the data line buffer

    // int numbytes1 = 0, numbytes2 = 0; // how many bytes

    /*
     * public PlaybackThread(String id, String[][] asFiles, SourceDataLine line, boolean bUpdStats) { this(id, asFiles, line, true,
     * MultichannelMp3_Shared.p_decode, bUpdStats); }
     */
    public PlaybackThread(String id, TPlaybackThreadDataRecord record, SourceDataLine line, boolean bRepeat,
            float p_decode, boolean bUpdStats) {
        this(id, record, line, bRepeat, p_decode);
        this.bUpdStats = bUpdStats;
    }

    public PlaybackThread(String id, TPlaybackThreadDataRecord record, SourceDataLine line, boolean bRepeat,
            float p_decode) {
        this(id, record, line);
        this.bRepeat_Shuffle = bRepeat;
        this.p_decode = p_decode;
    }

    /**
     * Convenience constructur that takes a <code>TPlaybackThreadDataRecord</code> and pulls all data from it to create
     * a new thread
     * <p>
     * Note the constructor call that looks quite crazy with all its necessary casts.
     */
    /*
     * public PlaybackThread(String id, TPlaybackThreadDataRecord record, SourceDataLine line) { // We assume that if a channel is not used,
     * avMusic[i] is null // otherwise also the pPos[i] must be _not null_. String[][] aas; if (record.avMusic[0] != null && record.avMusic[1] !=
     * null) { // both are used aas = new String[][] { (String[]) record.avMusic[0].toArray(new String[0]), (String[]) record.avMusic[1].toArray(new
     * String[0])}; this(id, record.pPos[0].x, record.pPos[0].y, record.pPos[1].x, record.pPos[1].y, new String[][] { (String[])
     * record.avMusic[0].toArray(new String[0]), (String[]) record.avMusic[1].toArray(new String[0])} , line); } else if (record.avMusic[0] == null &&
     * record.avMusic[1] != null) { // only right is used aas = new String[][] { {}, (String[]) record.avMusic[1].toArray(new String[0])}; } else if
     * (record.avMusic[0] != null && record.avMusic[1] == null) { // only left is used aas = new String[][] { (String[]) record.avMusic[0].toArray(new
     * String[0]), {} }; } this(id, record.pPos[0].x, record.pPos[0].y, record.pPos[1].x, record.pPos[1].y, new String[][] { (String[])
     * record.avMusic[0].toArray(new String[0]), (String[]) record.avMusic[1].toArray(new String[0])} , line); }
     */

    public PlaybackThread(String id, TPlaybackThreadDataRecord record, SourceDataLine line) {
        this.setName(getClass().getSimpleName() + " (" + id + ")");
        this.id = id;
        if (record.pPos[0] != null) {
            this.aPos[0] = record.pPos[0];
        }
        if (record.pPos[1] != null) {
            this.aPos[1] = record.pPos[1];
        }

        this.line = line;

        // Initialize so that we are ready to start playing if "start()" is invoked

        Vector<File> vTmp = new Vector<File>();
        File f = null;

        try {
            // Create array of files
            aFiles = new File[2][];
            String sAktFile;

            // Try to create an array of files out of the strings and make sure all files
            // exist
            for (int j = 0; j < record.avMusic.length; j++) {
                if (record.avMusic[j] != null) {
                    aEmpty[j] = false;
                    for (int i = 0; i < record.avMusic[j].size(); i++) {
                        sAktFile = (String) record.avMusic[j].get(i);
                        f = new File(sAktFile);
                        if (f.exists()) {
                            vTmp.add(f);
                        } else {
                            log_warning("File not found '" + sAktFile + "'. Ignoring");
                        }
                    }
                }
                // Make sure there is at least one file
                if (vTmp.size() >= 1) {
                    log_fine("Channel " + j + ": ok.");
                    aFiles[j] = vTmp.toArray(new File[0]);
                    vTmp.clear();
                } else {
                    log_warning("Channel " + j + ": No song.");
                    muteChannel(j);
                    // throw new Exception();
                }
            }

            /*
             * this.file1 = new File(sfile1); this.file2 = new File(sfile2);
             */
            /*
             * DataLine.Info info = new DataLine.Info(SourceDataLine.class, MultichannelMp3_Shared.datalineformat); // Open the line through which
             * we'll play the streaming audio. line = (SourceDataLine) AudioSystem.getLine(info);
             */
            line.open(Commons.datalineformat);

            // everything is ok...
            ready = true;

        } catch (Exception ex) {
            log_warning("Could not create NodeThread '" + id + "'. " + ex.getMessage());
            ex.printStackTrace();

        } finally { // Always relinquish the resources we use
            if (line != null) {
                line.close();
            }
            if (ain[0] != null) {
                try {
                    ain[0].close();
                } catch (IOException ex2) {
                }
            }
            if (ain[1] != null) {
                try {
                    ain[1].close();
                } catch (IOException ex1) {
                }
            }
        }
    }

    /*
     * public PlaybackThread(String id, int x1, int y1, int x2, int y2, String[][] asFiles, SourceDataLine line) { this.id = id; this.x1 = x1; this.x2 =
     * x2; this.y1 = y1; this.y2 = y2; this.line = line; // Initialize so that we are ready to start playing if "start()" is invoked Vector vTmp = new
     * Vector(); File f = null; try { // Create array of files aFiles = new File[2][]; // Try to create an array of files out of the strings and make
     * sure all files // exist for (int j = 0; j < asFiles.length; j++) { if (asFiles[j] != null) { aEmpty[j] = false; for (int i = 0; i <
     * asFiles[j].length; i++) { f = new File(asFiles[j][i]); if (f.exists()) { vTmp.add(f); } else { log_warning("File not found '" + asFiles[j][i] +
     * "'. Ignoring"); } } // Make sure there is at least one file if (vTmp.size() >= 1) { log_fine("Channel " + j + " ok."); aFiles[j] = (File[])
     * vTmp.toArray(new File[0]); vTmp.clear(); } else { log_warning("No song."); muteChannel(j); //throw new Exception(); } } else { aEmpty[j] =
     * true; } } line.open(MultichannelMp3_Shared.datalineformat); // everything is ok... ready = true; } catch (Exception ex) { log_warning("Could
     * not create NodeThread '" + id + "'. " + ex.getMessage()); ex.printStackTrace(); } finally { // Always relinquish the resources we use if (line !=
     * null) line.close(); if (ain[0] != null) { try { ain[0].close(); } catch (IOException ex2) { } } if (ain[1] != null) { try { ain[1].close(); }
     * catch (IOException ex1) { } } } }
     */

    public void muteChannel(int channel) {
        Arrays.fill(buffer[channel], (byte) 0);
        // bytesread1 = buffer[channel].length;
        if (!aEmpty[channel]) {
            aEmpty[channel] = true;
            if (bUpdStats) {
                // MultichannelMp3_Shared.iCurrentlyMuted++;
                if (Commons.cf != null && aPos[channel] != null) {
                    Commons.cf.mod.setMutedSpeaker(aPos[channel].x, aPos[channel].y, true);
                    Commons.cf.updateStats();
                }
            }
        }
    }

    public void unmuteChannel(int channel) {
        if (aEmpty[channel]) {
            aEmpty[channel] = false;
            if (bUpdStats) {
                // MultichannelMp3_Shared.iCurrentlyMuted--;
                if (Commons.cf != null && aPos[channel] != null) {
                    Commons.cf.mod.setMutedSpeaker(aPos[channel].x, aPos[channel].y, false);
                }
                Commons.cf.updateStats();
            }
        }
    }

    public AudioInputStream getNextSong(int channel, boolean stats) throws IOException, UnsupportedAudioFileException {

        File tempfile = null;
        if (bRepeat_Shuffle) {
            // normal mode, get random song from list
            tempfile = aFiles[channel][Commons.rand.nextInt(aFiles[channel].length)];
        } else {
            // "playsound-mode"
            tempfile = aFiles[channel][0];
            // One more file to come?
            if (aFiles[channel].length > 1) {
                // yes, remove File with index 0
                Vector<File> tmp = new Vector<File>(Arrays.asList(aFiles[channel]));
                tmp.remove(0);
                aFiles[channel] = tmp.toArray(new File[0]);
            } else {
                // no, set Array to null as a signal to mute this channel
                aFiles[channel] = null;
            }
        }

        // Is this already a wav?
        if (tempfile.getName().endsWith(".wav")) {
            // play it
            log_fine("playing wav...");
        } else {
            if (Commons.lhmDecoded.containsKey(tempfile)) {
                // This file has already been decoded, so play the decoded version instead
                tempfile = Commons.lhmDecoded.get(tempfile);
                log_fine("found in HashMap, playing decoded version..");
            } else {
                // Is perhaps a decoded version already there?
                File fTryDecoded = new File(DecoderThread.getDecodedFileName(tempfile));
                if (fTryDecoded.exists()) {
                    // use already decoded version (was not in Hashmap)
                    log_fine("Accidently found: " + fTryDecoded + ". I'll use it.");
                    Commons.lhmDecoded.put(tempfile, fTryDecoded);
                    tempfile = fTryDecoded;
                } else {
                    // Should we decode this file or just play the mp3-version?
                    if (Commons.rand.nextFloat() < p_decode) {
                        // Decode
                        log_info("Decoding: " + tempfile);

                        // Change table model
                        if (aPos[channel] != null) {
                            Commons.cf.mod.setDecodingAt(aPos[channel].x, aPos[channel].y, tempfile.getName());
                        }

                        aWaitForDecoder[channel] = true;

                        // While the file is being decoded, mute this channel
                        muteChannel(channel);

                        DecoderThread dt = new DecoderThread(this, tempfile, channel, stats);
                        dt.start();
                    } else {
                        // Play the mp3 file
                        log_fine("playing mp3...");
                    }
                }
            }
        }

        if (!aEmpty[channel]) {
            return prepareAin(tempfile, channel, stats);
        } else {
            return null;
        }
    }

    public AudioInputStream prepareAin(File file, int channel, boolean stats) throws IOException,
            UnsupportedAudioFileException {
        AudioInputStream ain;

        log_info("Next song for channel " + channel + ": " + file);

        // Change table model
        if (aPos[channel] != null) {
            Commons.cf.mod.setSongAt(aPos[channel].x, aPos[channel].y, file.getName());
        }

        ain = AudioSystem.getAudioInputStream(file);

        ain = AudioSystem.getAudioInputStream(Commons.datalineformat, ain);
        // ain = AudioSystem.getAudioInputStream(MultichannelMp3_Shared.monoformat, ain);
        // ain = AudioSystem.getAudioInputStream(MultichannelMp3_Shared.datalineformat, ain);
        if (stats && bUpdStats) {
            Commons.iSongscount++;
            if (Commons.cf != null) {
                Commons.cf.updateStats();
            }
        }
        return ain;
    }

    public void decodingFinished(File file, int channel, boolean stats, DecoderThread dt) {
        try {
            unmuteChannel(channel);
            ain[channel] = prepareAin(file, channel, stats); // this repaints grid
            Commons.lhmDecoded.put(dt.getEncodedFile(), file);

            Commons.iDecodedcount++;
            if (Commons.cf != null) {
                Commons.cf.updateStats();
            }
            aWaitForDecoder[channel] = false;
        } catch (Exception ex) {
            ex.printStackTrace();
            decodingFailed(channel, stats);
        }
    }

    public void decodingFailed(int channel, boolean stats) {
        // Try again
        try {
            log_warning("Decoding failed. Trying next song...");
            AudioInputStream aintemp = getNextSong(channel, stats);
            if (aintemp != null) {
                ain[channel] = aintemp;
                unmuteChannel(channel);
                aWaitForDecoder[channel] = false;
            }
        } catch (Exception ex) {
            log_warning("Getting next song failed... Trying next song");
            ex.printStackTrace();
            decodingFailed(channel, stats);
        }
    }

    @Override
    public void run() {
        if (ready) {
            log_info(" starts");

            // int numbytes1 = 0, numbytes2 = 0; // TODO: remaining bytes?!
            int[] bytesread = { 0, 0 };

            try {
                if (!aEmpty[0]) {
                    ain[0] = getNextSong(0, false);
                } else {
                    // left channel empty
                    Arrays.fill(buffer[0], (byte) 0);
                    bytesread[0] = buffer[0].length;
                    log_fine("Left channel empty, filling with 0");
                }
                if (!aEmpty[1]) {
                    ain[1] = getNextSong(1, false);
                } else {
                    // right channel empty
                    Arrays.fill(buffer[1], (byte) 0);
                    bytesread[1] = buffer[1].length;
                    log_fine("Right channel empty, filling with 0");
                }

                line.open(Commons.datalineformat);

                line.start();

                while (!bQuitLoop) {

                    // Thread suspended?
                    try {
                        if (threadSuspended) {
                            line.stop();
                            log_fine("waiting...");
                            synchronized (this) {
                                while (threadSuspended) {
                                    wait();
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                    }

                    // First, read some bytes from the input streams.
                    if (!aEmpty[0] && ain[0] != null) {
                        bytesread[0] = ain[0].read(buffer[0], 0, buffer[0].length);
                    }
                    if (!aEmpty[1] && ain[1] != null) {
                        bytesread[1] = ain[1].read(buffer[1], 0, buffer[1].length);
                    }

                    // For each channel
                    for (int iChannel = 0; iChannel < 2; iChannel++) {

                        // If any stream is at the end, we get the next song
                        // good? TODO
                        // Only if we are not waiting for a DecoderThread
                        if (bytesread[iChannel] == -1 && !aWaitForDecoder[iChannel]) {
                            if (bRepeat_Shuffle) {
                                ain[iChannel] = getNextSong(iChannel, true);
                            } else {
                                // "playsound-mode"
                                // Still another song to come for this channel?
                                if (aFiles[iChannel] != null) {
                                    // get next song
                                    ain[iChannel] = getNextSong(iChannel, true);
                                } else {
                                    // other channel already finished?
                                    if (bOtherChannelAlreadyFinished) {
                                        // yes, so we break out of the loop and end
                                        // the thread
                                        bQuitLoop = true;
                                    } else {
                                        // no, in the meantime: silence, "empty"
                                        bOtherChannelAlreadyFinished = true;
                                        muteChannel(iChannel);
                                        bytesread[iChannel] = buffer[iChannel].length;
                                    }
                                }
                            }
                        }
                    }

                    // Construct dataline-Buffer (from two stereo buffers; naive, bytewise)
                    /*
                     * for (int j = 0; j < buffer1.length - 4; j = j + 4) { datalinebuffer[j] = (byte) (buffer1[j] / 2 + buffer1[j+2] / 2);
                     * datalinebuffer[j+1] = (byte) (buffer1[j+1] / 2 + buffer1[j+3] / 2); datalinebuffer[j+2] = (byte) (buffer2[j] / 2 + buffer2[j+2] /
                     * 2); datalinebuffer[j+3] = (byte) (buffer2[j+1] / 2 + buffer2[j+3] / 2); }
                     */

                    // Construct dataline-Buffer (from two stereo buffers; better, samplewise)
                    /*
                     * int iSample1, iSample2, iMeanSample; boolean bBigEndian = true; // temp for (int j = 0; j < buffer1.length - 4; j = j + 4) {
                     * iSample1 = TConversionTool.bytesToInt16(buffer1, j, bBigEndian); iSample2 = TConversionTool.bytesToInt16(buffer1, j+2,
                     * bBigEndian); iMeanSample = iSample1 / 2 + iSample2 / 2; TConversionTool.intToBytes16(iMeanSample, datalinebuffer, j,
                     * bBigEndian); iSample1 = TConversionTool.bytesToInt16(buffer2, j, bBigEndian); iSample2 = TConversionTool.bytesToInt16(buffer2,
                     * j+2, bBigEndian); iMeanSample = iSample1 / 2 + iSample2 / 2; TConversionTool.intToBytes16(iMeanSample, datalinebuffer, j+2,
                     * bBigEndian); }
                     */

                    // Construct dataline-Buffer (from two stereo buffers; optimized!?)
                    int iMeanSample;
                    for (int j = 0; j < buffer[0].length - 4; j = j + 4) {
                        iMeanSample = ((buffer[0][j + 1] << 8 | buffer[0][j] & 0xFF) >> 1)
                                + ((buffer[0][j + 3] << 8 | buffer[0][j + 2] & 0xFF) >> 1);
                        datalinebuffer[j] = (byte) (iMeanSample & 0xFF);
                        datalinebuffer[j + 1] = (byte) (iMeanSample >> 8);

                        iMeanSample = ((buffer[1][j + 1] << 8 | buffer[1][j] & 0xFF) >> 1)
                                + ((buffer[1][j + 3] << 8 | buffer[1][j + 2] & 0xFF) >> 1);
                        datalinebuffer[j + 2] = (byte) (iMeanSample & 0xFF);
                        datalinebuffer[j + 3] = (byte) (iMeanSample >> 8);
                    }

                    // Construct dataline-Buffer (from two mono buffers)
                    /*
                     * for (int j = 0; j < buffer1.length - 4; j = j + 2) { datalinebuffer[j * 2] = buffer1[j]; datalinebuffer[(j * 2) + 1] =
                     * buffer1[j+1]; datalinebuffer[(j * 2) + 2] = buffer2[j]; datalinebuffer[(j * 2) + 3] = buffer2[j+1]; }
                     */

                    // log_info("writing");
                    line.write(datalinebuffer, 0, datalinebuffer.length);

                }

                // the following commands are commented out because they
                // are never reached...

                line.drain();
                line.close();
            } catch (Exception e) {
                e.printStackTrace();
                // System.exit(1);
            } finally { // Always relinquish the resources we use
                if (line != null) {
                    line.close();
                }
                if (ain[0] != null) {
                    try {
                        ain[0].close();
                        ain[0] = null;
                    } catch (IOException ex2) {
                    }
                }
                if (ain[1] != null) {
                    try {
                        ain[1].close();
                        ain[1] = null;
                    } catch (IOException ex1) {
                    }
                }
            }

        }

    }

    public void stop_playback() {
        bQuitLoop = true;
        if (threadSuspended) {
            resume_playback();
        }
    }

    public void pause_playback() {
        threadSuspended = true;
        // line.stop();
    }

    public void resume_playback() {
        threadSuspended = false;

        synchronized (this) {
            log_fine("notifiy");
            this.notify();
        }
        line.start();
    }

    protected void log_fine(String msg) {
        Commons.log.fine(id + ": " + msg);
    }

    protected void log_info(String msg) {
        Commons.log.info(id + ": " + msg);
    }

    protected void log_warning(String msg) {
        Commons.log.warning(id + ": " + msg);
    }

}