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
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Thread to play music files.
 * 
 * @author Ewald Peiszer
 * @version $Id: PlaybackThread.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class PlaybackThread extends Thread {

    public static int decodedCount = 0;

    public static LinkedHashMap<File, File> decodedFiles = new LinkedHashMap<File, File>(50); // <File, File>

    /** Probability to decode a mp3 file to wav */
    public static final float DEFAULT_PROBABILITY_TO_DECODE = 0.5f;

    public static Random rand = new Random();

    // stats vars
    public static int songCount = 0;

    protected AudioInputStream[] audioInputStream = new AudioInputStream[2]; // We read audio data from here

    // Allocate a buffer for reading from the input stream and writing to the line.
    // Make it large enough to hold 4k audio frames. Note that the SourceDataLine also has its own internal buffer.
    protected byte[][] buffer = { new byte[1 * 1024 * Constants.DATALINE_FORMAT.getFrameSize()],
            new byte[1 * 1024 * Constants.DATALINE_FORMAT.getFrameSize()] }; // the buffer

    protected byte[] datalineBuffer = new byte[1 * 1024 * Constants.DATALINE_FORMAT.getFrameSize()]; // the data line

    // buffer

    private String decodedOutputDir;

    /** Flag if channel is empty: in this case, there will be silence on the respective channel */
    boolean[] empty = new boolean[2];

    protected File file1;

    protected File file2;

    /** Files to play */
    File[][] files = null;

    String id;

    protected SourceDataLine line = null;

    protected int monoFramesize = Constants.MONO_FORMAT.getFrameSize(); // sollte 2 sein

    /**
     * (if bRepeat == false): if the first channel's musicfile stops, it is set to true.<br>
     * If the second channel's musicfile stops, this thread is stopped.
     */
    boolean otherChannelAlreadyFinished = false;

    private Vector<PlaybackListener> playbackListeners = new Vector<PlaybackListener>();

    Point[] positions = new Point[2];

    float probalityToDecode = DEFAULT_PROBABILITY_TO_DECODE;

    protected boolean quitLoop = false;

    protected boolean ready = false;

    /**
     * Flag: if true, then music is played endlessly, alwas repeating. Songs are picked in random order.
     * <p>
     * If false, then on each channel there will be played each song in the list, from the first to the last, then exit.
     */
    boolean repeatShuffle = true;

    private boolean threadSuspended = false;

    /** Flag: if false, no global statistic variable will be updated by this Thread */
    boolean updateStats = true;

    /**
     * Flag: if <code>true</code>, then the respective channel is waiting for a {@link DecoderThread} to finish.
     */
    boolean[] waitForDecoder = { false, false };

    public PlaybackThread(String id, PlaybackThreadDataRecord record, SourceDataLine line, boolean repeat,
            float probalityToDecode, boolean updateStats, String decodedOutputDir) {
        this(id, record, line, repeat, probalityToDecode, decodedOutputDir);
        this.updateStats = updateStats;
    }

    public PlaybackThread(String id, PlaybackThreadDataRecord record, SourceDataLine line, boolean repeat,
            float probalityToDecode, String decodedOutputDir) {
        this(id, record, line, decodedOutputDir);
        this.repeatShuffle = repeat;
        this.probalityToDecode = probalityToDecode;
    }

    /**
     * Convenience constructur that takes a {@link PlaybackThreadDataRecord} and pulls all data from it to create a new
     * thread.<br>
     * Note the constructor call that looks quite crazy with all its necessary casts.
     */

    public PlaybackThread(String id, PlaybackThreadDataRecord record, SourceDataLine line, String decodedOutputDir) {
        setName(this.getClass().getSimpleName() + " (" + id + ")");
        this.id = id;
        this.decodedOutputDir = decodedOutputDir;
        if (record.position[0] != null) {
            this.positions[0] = record.position[0];
        }
        if (record.position[1] != null) {
            this.positions[1] = record.position[1];
        }

        this.line = line;

        // Initialize so that we are ready to start playing if "start()" is invoked

        Vector<File> tempVector = new Vector<File>();
        File file = null;

        try {
            // Create array of files
            files = new File[2][];

            // Try to create an array of files out of the strings and make sure all files exist
            for (int j = 0; j < record.listOfSongs.length; j++) {
                if (record.listOfSongs[j] != null) {
                    empty[j] = false;
                    for (int i = 0; i < record.listOfSongs[j].size(); i++) {
                        String currentSong = record.listOfSongs[j].get(i);
                        file = new File(currentSong);
                        if (file.exists()) {
                            tempVector.add(file);
                        } else {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").warning(
                                    id + ": " + "File not found '" + currentSong + "'. Ignoring");
                        }
                    }
                }
                // Make sure there is at least one file
                if (tempVector.size() >= 1) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").fine(id + ": " + "Channel " + j + ": ok.");
                    files[j] = tempVector.toArray(new File[0]);
                    tempVector.clear();
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").warning(
                            id + ": " + "Channel " + j + ": No song.");
                    muteChannel(j);
                    // throw new Exception();
                }
            }

            line.open(Constants.DATALINE_FORMAT);

            // everything is ok...
            ready = true;

        } catch (Exception ex) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").warning(
                    id + ": " + "Could not create NodeThread '" + id + "'. " + ex.getMessage());
            ex.printStackTrace();

        } finally { // Always relinquish the resources we use
            if (line != null) {
                line.close();
            }
            if (audioInputStream[0] != null) {
                try {
                    audioInputStream[0].close();
                } catch (IOException ex2) {
                }
            }
            if (audioInputStream[1] != null) {
                try {
                    audioInputStream[1].close();
                } catch (IOException ex1) {
                }
            }
        }
    }

    public boolean addPlaybackListener(PlaybackListener listener) {
        return playbackListeners.add(listener);
    }

    public void decodingFailed(int channel, boolean stats) {
        // Try again
        try {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").warning(
                    id + ": " + "Decoding failed. Trying next song...");
            AudioInputStream aintemp = getNextSong(channel, stats);
            if (aintemp != null) {
                audioInputStream[channel] = aintemp;
                unMuteChannel(channel);
                waitForDecoder[channel] = false;
            }
        } catch (Exception ex) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").warning(
                    id + ": " + "Getting next song failed... Trying next song");
            ex.printStackTrace();
            decodingFailed(channel, stats);
        }
    }

    public void decodingFinished(File file, int channel, boolean stats, DecoderThread dt) {
        try {
            unMuteChannel(channel);
            audioInputStream[channel] = prepareAudioInputStream(file, channel, stats); // this repaints grid
            PlaybackThread.decodedFiles.put(dt.getEncodedFile(), file);

            PlaybackThread.decodedCount++;
            for (int i = 0; i < playbackListeners.size(); i++) {
                PlaybackListener playbackListener = playbackListeners.get(i);
                playbackListener.updateStats(PlaybackThread.songCount);
            }

            waitForDecoder[channel] = false;
        } catch (Exception ex) {
            ex.printStackTrace();
            decodingFailed(channel, stats);
        }
    }

    public AudioInputStream getNextSong(int channel, boolean stats) throws IOException, UnsupportedAudioFileException {

        File tempFile = null;
        if (repeatShuffle) {
            // normal mode, get random song from list
            tempFile = files[channel][PlaybackThread.rand.nextInt(files[channel].length)];
        } else {
            // "playsound-mode"
            tempFile = files[channel][0];
            // One more file to come?
            if (files[channel].length > 1) {
                // yes, remove File with index 0
                Vector<File> tmp = new Vector<File>(Arrays.asList(files[channel]));
                tmp.remove(0);
                files[channel] = tmp.toArray(new File[0]);
            } else {
                // no, set Array to null as a signal to mute this channel
                files[channel] = null;
            }
        }

        // Is this already a wav?
        if (tempFile.getName().endsWith(".wav")) {
            // play it
            Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").fine(id + ": " + "playing wav...");
        } else {
            if (PlaybackThread.decodedFiles.containsKey(tempFile)) {
                // This file has already been decoded, so play the decoded version instead
                tempFile = PlaybackThread.decodedFiles.get(tempFile);
                Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").fine(
                        id + ": " + "found in HashMap, playing decoded version..");
            } else {
                // Is perhaps a decoded version already there?
                File tryDecodedFile = new File(DecoderThread.getDecodedFileName(tempFile, decodedOutputDir,
                        Constants.DECODED_SUFFIX));
                if (tryDecodedFile.exists()) {
                    // use already decoded version (was not in Hashmap)
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").fine(
                            id + ": " + "Accidently found: " + tryDecodedFile + ". I'll use it.");
                    PlaybackThread.decodedFiles.put(tempFile, tryDecodedFile);
                    tempFile = tryDecodedFile;
                } else {
                    // Should we decode this file or just play the mp3-version?
                    if (PlaybackThread.rand.nextFloat() < probalityToDecode) {
                        // Decode
                        Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").info(
                                id + ": " + "Decoding: " + tempFile);

                        // Change table model
                        if (positions[channel] != null) {
                            for (int i = 0; i < playbackListeners.size(); i++) {
                                PlaybackListener playbackListener = playbackListeners.get(i);
                                playbackListener.setDecodingAt(positions[channel].x, positions[channel].y,
                                        tempFile.getName());
                            }
                        }
                        waitForDecoder[channel] = true;

                        // While the file is being decoded, mute this channel
                        muteChannel(channel);

                        DecoderThread decoderThread = new DecoderThread(this, tempFile, channel, stats,
                                decodedOutputDir, Constants.DECODED_SUFFIX);
                        decoderThread.start();
                    } else {
                        // Play the mp3 file
                        Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").fine(id + ": " + "playing mp3...");
                    }
                }
            }
        }

        if (!empty[channel]) {
            return prepareAudioInputStream(tempFile, channel, stats);
        } else {
            return null;
        }
    }

    public boolean isReady() {
        return ready;
    }

    public void muteChannel(int channel) {
        Arrays.fill(buffer[channel], (byte) 0);
        if (!empty[channel]) {
            empty[channel] = true;
            if (updateStats && positions[channel] != null) {
                for (int i = 0; i < playbackListeners.size(); i++) {
                    PlaybackListener playbackListener = playbackListeners.get(i);
                    playbackListener.setMutedSpeaker(positions[channel].x, positions[channel].y, true);
                    playbackListener.updateStats(PlaybackThread.songCount);
                }
            }
        }
    }

    public void pausePlayback() {
        threadSuspended = true;
    }

    public AudioInputStream prepareAudioInputStream(File file, int channel, boolean stats) throws IOException,
            UnsupportedAudioFileException {
        AudioInputStream audioInputStream;

        Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").info(
                id + ": " + "Next song for channel " + channel + ": " + file);

        // update listeners
        if (positions[channel] != null) {
            for (int i = 0; i < playbackListeners.size(); i++) {
                PlaybackListener playbackListener = playbackListeners.get(i);
                playbackListener.setSongAt(positions[channel].x, positions[channel].y, file.getName());
            }
        }

        audioInputStream = AudioSystem.getAudioInputStream(file);

        audioInputStream = AudioSystem.getAudioInputStream(Constants.DATALINE_FORMAT, audioInputStream);

        if (stats && updateStats) {
            PlaybackThread.songCount++;
            for (int i = 0; i < playbackListeners.size(); i++) {
                PlaybackListener playbackListener = playbackListeners.get(i);
                playbackListener.updateStats(PlaybackThread.songCount);
            }
        }
        return audioInputStream;
    }

    public boolean removePlaybackListener(PlaybackListener listener) {
        return playbackListeners.remove(listener);
    }

    public void resumePlayback() {
        threadSuspended = false;

        synchronized (this) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").fine(id + ": " + "notifiy");
            this.notify();
        }
        line.start();
    }

    @Override
    public void run() {
        if (ready) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").info(id + ": " + " starts");

            int[] bytesRead = { 0, 0 };

            try {
                if (!empty[0]) {
                    audioInputStream[0] = getNextSong(0, false);
                } else {
                    // left channel empty
                    Arrays.fill(buffer[0], (byte) 0);
                    bytesRead[0] = buffer[0].length;
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").fine(
                            id + ": " + "Left channel empty, filling with 0");
                }
                if (!empty[1]) {
                    audioInputStream[1] = getNextSong(1, false);
                } else {
                    // right channel empty
                    Arrays.fill(buffer[1], (byte) 0);
                    bytesRead[1] = buffer[1].length;
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").fine(
                            id + ": " + "Right channel empty, filling with 0");
                }

                line.open(Constants.DATALINE_FORMAT);

                line.start();

                while (!quitLoop) {

                    // Thread suspended?
                    try {
                        if (threadSuspended) {
                            line.stop();
                            Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").fine(id + ": " + "waiting...");
                            synchronized (this) {
                                while (threadSuspended) {
                                    wait();
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                    }

                    // First, read some bytes from the input streams.
                    if (!empty[0] && audioInputStream[0] != null) {
                        bytesRead[0] = audioInputStream[0].read(buffer[0], 0, buffer[0].length);
                    }
                    if (!empty[1] && audioInputStream[1] != null) {
                        bytesRead[1] = audioInputStream[1].read(buffer[1], 0, buffer[1].length);
                    }

                    // For each channel
                    for (int channel = 0; channel < 2; channel++) {

                        // If any stream is at the end, we get the next song good?
                        // TODO: Only if we are not waiting for a DecoderThread
                        if (bytesRead[channel] == -1 && !waitForDecoder[channel]) {
                            if (repeatShuffle) {
                                audioInputStream[channel] = getNextSong(channel, true);
                            } else {
                                // "playsound-mode"
                                // Still another song to come for this channel?
                                if (files[channel] != null) {
                                    // get next song
                                    audioInputStream[channel] = getNextSong(channel, true);
                                } else {
                                    // other channel already finished?
                                    if (otherChannelAlreadyFinished) {
                                        // yes, so we break out of the loop and end
                                        // the thread
                                        quitLoop = true;
                                    } else {
                                        // no, in the meantime: silence, "empty"
                                        otherChannelAlreadyFinished = true;
                                        muteChannel(channel);
                                        bytesRead[channel] = buffer[channel].length;
                                    }
                                }
                            }
                        }
                    }

                    // Construct dataline-Buffer (from two stereo buffers; optimized!?)
                    int meanSample;
                    for (int j = 0; j < buffer[0].length - 4; j = j + 4) {
                        meanSample = ((buffer[0][j + 1] << 8 | buffer[0][j] & 0xFF) >> 1)
                                + ((buffer[0][j + 3] << 8 | buffer[0][j + 2] & 0xFF) >> 1);
                        datalineBuffer[j] = (byte) (meanSample & 0xFF);
                        datalineBuffer[j + 1] = (byte) (meanSample >> 8);

                        meanSample = ((buffer[1][j + 1] << 8 | buffer[1][j] & 0xFF) >> 1)
                                + ((buffer[1][j + 3] << 8 | buffer[1][j + 2] & 0xFF) >> 1);
                        datalineBuffer[j + 2] = (byte) (meanSample & 0xFF);
                        datalineBuffer[j + 3] = (byte) (meanSample >> 8);
                    }

                    line.write(datalineBuffer, 0, datalineBuffer.length);
                }

                line.drain();
                line.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally { // Always relinquish the resources we use
                if (line != null) {
                    line.close();
                }
                if (audioInputStream[0] != null) {
                    try {
                        audioInputStream[0].close();
                        audioInputStream[0] = null;
                    } catch (IOException ex2) {
                    }
                }
                if (audioInputStream[1] != null) {
                    try {
                        audioInputStream[1].close();
                        audioInputStream[1] = null;
                    } catch (IOException ex1) {
                    }
                }
            }

        }

    }

    public void stopPlayback() {
        quitLoop = true;
        if (threadSuspended) {
            resumePlayback();
        }
    }

    public void unMuteChannel(int channel) {
        if (empty[channel]) {
            empty[channel] = false;
            if (updateStats && positions[channel] != null) {
                for (int i = 0; i < playbackListeners.size(); i++) {
                    PlaybackListener playbackListener = playbackListeners.get(i);
                    playbackListener.setMutedSpeaker(positions[channel].x, positions[channel].y, true);
                    playbackListener.updateStats(PlaybackThread.songCount);
                }
            }
        }
    }

}