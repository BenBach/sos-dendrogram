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
package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import at.tuwien.ifs.somtoolbox.util.ProgressListener;

/**
 * The status bar displayed in the lower-right part of the SOMViewer application.
 * 
 * @author Thomas Lidy
 * @version $Id: StatusBar.java 3873 2010-10-28 09:29:58Z frank $
 */
public class StatusBar extends JPanel implements ProgressListener {
    private static final long serialVersionUID = 1L;

    private JLabel statusText = null;

    private JProgressBar progressBar = null;

    public StatusBar() {
        super(new BorderLayout());

        setBorder(BorderFactory.createLoweredBevelBorder());

        statusText = new JLabel("ready.", SwingConstants.LEFT);
        // statusText.setAlignmentX(JLabel.LEFT);
        statusText.setHorizontalAlignment(SwingConstants.LEFT);
        // statusText.setBorder( BorderFactory.createEtchedBorder());
        add(statusText, BorderLayout.WEST);

        // add progress bar
        progressBar = new JProgressBar();
        add(progressBar, BorderLayout.EAST);

        // TODO add methods for ProgessBar
    }

    public void setText(String text) {
        statusText.setText(text);
    }

    @Override
    public void insertColumn(int columns, String message) {
        // currently not used in this implementation
    }

    @Override
    public void insertRow(int rows, String message) {
        // currently not used in this implementation
    }

    @Override
    public void progress(String message, int currentStep) {
        // currently not used in this implementation
    }

    @Override
    public void progress(int currentStep) {
        progressBar.setValue(currentStep);
    }

    public void initProgressBar(int totalSteps) {
        progressBar.setMinimum(0);
        progressBar.setMaximum(totalSteps);
    }

    @Override
    public void progress() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    @Override
    public void progress(String message) {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    @Override
    public int getCurrentStep() {
        return progressBar.getValue();
    }

}
