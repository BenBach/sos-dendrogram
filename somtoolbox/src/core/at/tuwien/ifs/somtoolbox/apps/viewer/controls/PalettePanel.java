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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.JLabel;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.visualization.Palette;

/**
 * Implements a graphical elements displaying the currently used {@link Palette}.
 * 
 * @author Rudolf Mayer
 * @version $Id: PalettePanel.java 3873 2010-10-28 09:29:58Z frank $
 */
public class PalettePanel extends AbstractViewerControl implements ComponentListener {
    private static final long serialVersionUID = 1L;

    static final int BORDER = 10;

    private final JLabel noPalette = new JLabel("No palette loaded!");

    private PaletteDisplayer drawPalettePanel = null;

    public PalettePanel(String title, CommonSOMViewerStateData state) {
        super(title, state, new BorderLayout());
        setPalette(null);
        addComponentListener(this);
    }

    public void setPalette(Color[] palette) {
        setPalette(palette, -1, -1);
    }

    public void setPalette(Color[] palette, double minValue, double maxValue) {
        getContentPane().removeAll();
        if (palette != null) {
            if (drawPalettePanel == null) {
                drawPalettePanel = new PaletteDisplayer(new Palette("", "", "", palette));
                drawPalettePanel.setToolTipText("Click to switch scale between absolute and relative values");
                drawPalettePanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        drawPalettePanel.setShowPercent(!drawPalettePanel.getShowPercent());
                    }
                });
            } else {
                drawPalettePanel.setPalette(new Palette("", "", "", palette));
            }
            drawPalettePanel.setRange(minValue, maxValue);
            getContentPane().add(drawPalettePanel, BorderLayout.CENTER);
        } else {
            getContentPane().add(noPalette, BorderLayout.CENTER);
            setCollapsed(true);
        }
        revalidate();
        repaint();
    }

    public Color[] getPalette() {
        return drawPalettePanel.getPalette().getColors();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").fine("component resized called");
        if (drawPalettePanel != null) {
            // drawPalettePanel.adjustPaletteSize();
            repaint();
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

}
