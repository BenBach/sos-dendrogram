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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 * This is a helper class for the {@link JMultiLineToolTip} doing the actualy rendering of the multi-line tooltip. <br>
 * <br>
 * The original code was found at code found at <a href="http://www.codeguru.com/java/articles/122.shtml"
 * target="_blank">Java CodeGuru</a> and is written by Zafir Anjum.
 * 
 * @author Michael Dittenbach
 * @version $Id: MultiLineToolTipUI.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class MultiLineToolTipUI extends BasicToolTipUI {
    static MultiLineToolTipUI sharedInstance = new MultiLineToolTipUI();

    Font smallFont;

    static JToolTip tip;

    protected CellRendererPane rendererPane;

    private static JTextArea textArea;

    public MultiLineToolTipUI() {
        super();
    }

    public static ComponentUI createUI(JComponent c) {
        return sharedInstance;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        tip = (JToolTip) c;
        rendererPane = new CellRendererPane();
        c.add(rendererPane);
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);

        c.remove(rendererPane);
        rendererPane = null;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Dimension size = c.getSize();
        textArea.setBackground(c.getBackground());
        rendererPane.paintComponent(g, textArea, c, 1, 1, size.width - 1, size.height - 1, true);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        String tipText = ((JToolTip) c).getTipText();
        if (tipText == null) {
            return new Dimension(0, 0);
        }
        textArea = new JTextArea(tipText);
        rendererPane.removeAll();
        rendererPane.add(textArea);
        textArea.setWrapStyleWord(true);
        int width = ((JMultiLineToolTip) c).getFixedWidth();
        int columns = ((JMultiLineToolTip) c).getColumns();

        if (columns > 0) {
            textArea.setColumns(columns);
            textArea.setSize(0, 0);
            textArea.setLineWrap(true);
            textArea.setSize(textArea.getPreferredSize());
        } else if (width > 0) {
            textArea.setLineWrap(true);
            Dimension d = textArea.getPreferredSize();
            d.width = width;
            d.height++;
            textArea.setSize(d);
        } else {
            textArea.setLineWrap(false);
        }

        Dimension dim = textArea.getPreferredSize();

        dim.height += 1;
        dim.width += 1;
        return dim;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }
}
