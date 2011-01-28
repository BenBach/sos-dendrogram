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

import java.util.Date;
import java.util.LinkedList;

/**
 * @author Jakob Frank
 * @version $Id: AdaptiveStdErrProgressWriter.java 3587 2010-05-21 10:35:33Z mayer $
 * @see StdErrProgressWriter
 */
public class AdaptiveStdErrProgressWriter extends StdErrProgressWriter {

    private LinkedList<Long> backtraceList = new LinkedList<Long>();

    private int backtraceSteps = 10;

    /**
     * @param totalSteps the number of Steps
     * @param message message to display
     */
    public AdaptiveStdErrProgressWriter(int totalSteps, String message) {
        super(totalSteps, message);
    }

    /**
     * @param totalSteps the number of Steps
     * @param message message to display
     * @param stepWidth How often the message should be updated. Use a bigger stepWidth to improve the performance. The
     *            first and last step are printed regardless of the value of the stepWith.
     */
    public AdaptiveStdErrProgressWriter(int totalSteps, String message, int stepWidth) {
        super(totalSteps, message, stepWidth);
        backtraceList.add(startDate);
        setBacktraceWindow(.1);
    }

    /**
     * @param totalSteps the number of Steps
     * @param message message to display
     * @param stepWidth How often the message should be updated. Use a bigger stepWidth to improve the performance. The
     *            first and last step are printed regardless of the value of the stepWith.
     * @param newLineWidth add a newline every
     */
    public AdaptiveStdErrProgressWriter(int totalSteps, String message, int stepWidth, int newLineWidth) {
        super(totalSteps, message, stepWidth, newLineWidth);
    }

    @Override
    public void progress(int currentStep) {
        if (finished) {
            return;
        }
        if (currentStep == 1 || currentStep == totalSteps || currentStep % stepWidth == 0) {
            if (backtraceList.size() == 0) {
                backtraceList.add(startDate);
            }
            this.currentStep = currentStep;
            long currentDate = System.currentTimeMillis();

            long backtraceDur = (currentDate - backtraceList.getFirst()) / backtraceList.size();
            backtraceList.addLast(currentDate);

            int stepsToGo = totalSteps - currentStep;
            long estimatedEndDate = currentDate + stepsToGo * backtraceDur;

            StringBuffer log = new StringBuffer(messageLength + 30);
            log.append("\r").append(message).append(currentStep).append(" of ").append(totalSteps).append(", ETA: ").append(
                    format.format(new Date(estimatedEndDate))).append(", ").append(
                    DateUtils.shortFormatDuration(estimatedEndDate - currentDate)).append(" remaining.").append(" ");
            System.err.print(log);

            if (newLineWidth != 0 && currentStep % newLineWidth == 0) {
                System.err.println();
            }
            if (currentStep == totalSteps) {
                System.err.println("\n\t --> Finished, took " + DateUtils.formatDuration(currentDate - startDate));
                finished = true;
            }
        }

        // Trunk the list
        while (backtraceList.size() > backtraceSteps) {
            backtraceList.removeFirst();
        }
    }

    public void setBacktraceWindow(int steps) {
        backtraceSteps = steps;
    }

    public void setBacktraceWindow(double window) {
        backtraceSteps = (int) (totalSteps / stepWidth * window);
        if (backtraceSteps < 10) {
            backtraceSteps = 10;
        }
    }
}
