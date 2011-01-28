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
package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.Component;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JOptionPane;

import at.tuwien.ifs.somtoolbox.apps.viewer.StatusBar;

/**
 * Handles events from Logger and decides where to output it.
 * 
 * @author Thomas Lidy
 * @version $Id: LoggingHandler.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class LoggingHandler extends Handler {

    private StatusBar statusBar = null;

    private Component parentComp = null; // parent window to show messageBox in

    public void setStatusBar(StatusBar statusbar) {
        this.statusBar = statusbar;
    }

    public void deattachStatusBar() {
        this.statusBar = null;
    }

    public void setParentComponent(Component parentcomp) {
        this.parentComp = parentcomp;
    }

    public void deattachParentComponent() {
        this.parentComp = null;
    }

    @Override
    public void publish(LogRecord record) {
        Level level = record.getLevel();
        if (statusBar != null) {
            // System.out.println("level: " + record.getLevel().toString());
            if (level == Level.SEVERE) {
                statusBar.setText("SEVERE: " + record.getMessage());
            } else if (level == Level.WARNING) {
                statusBar.setText("WARNING: " + record.getMessage());
            } else {
                statusBar.setText(record.getMessage());
            }
        }
        if (parentComp != null && level == Level.SEVERE) {
            JOptionPane.showMessageDialog(parentComp, record.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        deattachStatusBar();
    }

    public void progress(int currentStep) {
        statusBar.progress(currentStep);
    }

    public void initProgressBar(int totalSteps) {
        statusBar.initProgressBar(totalSteps);
    }

}
