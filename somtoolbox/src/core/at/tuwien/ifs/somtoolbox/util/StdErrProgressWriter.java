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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A progress listener that writes the progress messages to the standard error stream ({@link System#err}).
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: StdErrProgressWriter.java 3883 2010-11-02 17:13:23Z frank $
 */
public class StdErrProgressWriter implements ProgressListener {

    protected String message = "";

    protected int currentStep = 0;

    protected int totalSteps = 0;

    protected int stepWidth = 1;

    protected long startDate = 0;

    private long elapsed = 0;

    private long estimatedLength = 0;

    private long estimatedEndDate = 0;

    protected DateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

    protected short messageLength = 0;

    protected short screenWidth = Short.MAX_VALUE;

    protected int newLineWidth;

    protected boolean finished = false;

    /**
     * Initialises a progress writer with a {@link StdErrProgressWriter#stepWidth} of 1.
     */
    public StdErrProgressWriter(int totalSteps, String message) {
        this(totalSteps, message, 1);
    }

    /**
     * Initialises a new progress writer.
     * 
     * @param totalSteps The total number of steps
     * @param message The basic message, "x of y, ETA hh:mm:ss zzz" will be added automatically to this.
     * @param stepWidth How often the message should be updated. Use a bigger stepWidth to improve the performance. The
     *            first and last step are printed regardless of the value of the stepWith.
     */
    public StdErrProgressWriter(int totalSteps, String message, int stepWidth) {
        this.message = message;
        this.messageLength = (short) message.length();
        this.screenWidth = getScreenWidth();
        this.totalSteps = totalSteps;
        this.stepWidth = stepWidth;
        if (stepWidth == 0) {
            this.stepWidth = 1;
        }
        startDate = new Date().getTime();
    }

    private short getScreenWidth() {
        String lines = System.getenv("LINES");
        if (lines != null) {
            try {
                short w = Short.parseShort(lines);
                return w;
            } catch (Exception e) {

            }
        }
        return Short.MAX_VALUE;
    }

    public StdErrProgressWriter(int totalSteps, String message, int stepWidth, int newLineWidth) {
        this(totalSteps, message, stepWidth);
        this.newLineWidth = Math.max(totalSteps / 20, 1000);
        newLineWidth = newLineWidth / stepWidth * stepWidth;
    }

    @Override
    public void progress() {
        progress(++currentStep);
    }

    @Override
    public void progress(int currentStep) {
        if (finished) {
            return;
        }
        if (currentStep == 1 || currentStep == totalSteps || currentStep % stepWidth == 0) {
            this.currentStep = currentStep;
            long currentDate = new Date().getTime();
            elapsed = currentDate - startDate;
            double percentDone = (double) currentStep / (double) totalSteps * 100.0d;
            estimatedLength = (long) (elapsed / percentDone) * 100;
            estimatedEndDate = startDate + estimatedLength;

            StringBuilder log = new StringBuilder(messageLength + 30);
            log.append("\r").append(message).append(currentStep).append(" of ").append(totalSteps).append(", ETA: ").append(
                    format.format(new Date(estimatedEndDate))).append(", ").append(
                    DateUtils.shortFormatDuration(estimatedEndDate - currentDate)).append(" left").append(" ");
            System.err.print(log);

            if (newLineWidth != 0 && currentStep % newLineWidth == 0) {
                System.err.println();
            }
            if (currentStep == totalSteps) {
                System.err.println("\n\t --> Finished, took " + DateUtils.formatDuration(elapsed));
                finished = true;
            }
        }
    }

    @Override
    public void progress(String message) {
        progress(message, currentStep + 1);
    }

    @Override
    public void progress(String message, int currentStep) {
        this.message = message.trim();
        this.messageLength = (short) message.length();
        progress(currentStep);
    }

    @Override
    public void insertRow(int rows, String message) {
        // currently not used in this implementation
    }

    @Override
    public void insertColumn(int columns, String message) {
        // currently not used in this implementation
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int getCurrentStep() {
        return currentStep;
    }

    public int getSteps() {
        return totalSteps;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

}
