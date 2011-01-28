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
package at.tuwien.ifs.commons.gui.controls.swing.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * @author Jakob Frank
 * @version $Id: ColorCellRenderer.java 3876 2010-11-02 15:10:17Z frank $
 */
public class ColorCellRenderer extends JLabel implements TableCellRenderer {
    private static final long serialVersionUID = 1L;

    Border unselectedBorder = null;

    Border selectedBorder = null;

    boolean isBordered = true;

    public ColorCellRenderer(boolean isBordered) {
        this.isBordered = isBordered;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Color newColor = (Color) color;
        setBackground(newColor);
        if (isBordered) {
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                }
                setBorder(unselectedBorder);
            }
        }
        return this;
    }
}