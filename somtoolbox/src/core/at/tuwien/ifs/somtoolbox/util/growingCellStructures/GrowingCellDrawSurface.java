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
package at.tuwien.ifs.somtoolbox.util.growingCellStructures;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.NumberFormat;

import javax.swing.JPanel;

import at.tuwien.ifs.somtoolbox.layers.GrowingCellUnit;
import at.tuwien.ifs.somtoolbox.models.GrowingCellStructures;

/**
 * Ermoeglicht das Zeichnen einer {@link GrowingCellStructures} waehrend des Trainings. Gibt die Units an ihren
 * Anzeigepositionen, ihre Verbindungen, die gemappten Inputvektoren und den Signal-Count aus.
 * 
 * @author Johannes Inführ
 * @author Andreas Zweng
 * @version $Id: GrowingCellDrawSurface.java 3585 2010-05-21 10:33:21Z mayer $
 */
public class GrowingCellDrawSurface extends JPanel {
    private static final long serialVersionUID = 1L;

    private double posScale = 3.0;// scales positions by this factor for bigger representation

    private Object[] data;

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.black);

        if (data == null) {
            return;
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        for (Object o : data) {
            GrowingCellTetraheder t = (GrowingCellTetraheder) o;

            GrowingCellUnit[] units = t.getCellUnits();
            for (GrowingCellUnit u : units) {// draw every unit
                g2.fillOval((int) (u.getX() * posScale), (int) (u.getY() * posScale), u.getDiameter(), u.getDiameter());

                int xbase = (int) (u.getX() * posScale) + u.getDiameter();
                int ybase = (int) (u.getY() * posScale);
                int stepsize = 11;

                // draw mapped data
                String[] mi = u.getMappedInputNames();
                if (mi != null) {
                    for (String s : mi) {
                        if (s != null) {
                            g2.drawString(s, xbase, ybase);
                        }
                        ybase += stepsize;
                    }
                }

                // draw signal counter
                g2.drawString(nf.format(u.getSignalCounter()), xbase, ybase);
            }

            // draw connections of the tetraheder
            int dofs = units[0].getDiameter() / 2;

            g2.drawLine((int) (units[0].getX() * posScale) + dofs, (int) (units[0].getY() * posScale) + dofs,
                    (int) (units[1].getX() * posScale) + dofs, (int) (units[1].getY() * posScale) + dofs);
            g2.drawLine((int) (units[0].getX() * posScale) + dofs, (int) (units[0].getY() * posScale) + dofs,
                    (int) (units[2].getX() * posScale) + dofs, (int) (units[2].getY() * posScale) + dofs);
            g2.drawLine((int) (units[2].getX() * posScale) + dofs, (int) (units[2].getY() * posScale) + dofs,
                    (int) (units[1].getX() * posScale) + dofs, (int) (units[1].getY() * posScale) + dofs);
        }
    }

    /**
     * Draws the Cells in the Array of CellTetraeders
     * 
     * @param objects Array of CellTetraeders
     */
    public void drawTheCells(Object[] objects) {
        this.data = objects;

        invalidate();
        validate();

        getParent().repaint();
    }

}
