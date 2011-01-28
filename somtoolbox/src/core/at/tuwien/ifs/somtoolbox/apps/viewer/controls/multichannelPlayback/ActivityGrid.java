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

import java.awt.Component;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import net.sf.jeppers.grid.DefaultStyleModel;
import net.sf.jeppers.grid.GridCellRenderer;
import net.sf.jeppers.grid.JGrid;

/**
 * Description: For use in <code>ControlFrame</code>. Same layout (=number of rows, columns) as LayoutTable. Shows
 * activity for each cell: current song, whether speaker is muted etc. <br>
 * I had to use JGrid instead of JTable because JTable kinda s*cks (I didn't manage to set the width of the columns as I
 * wanted them, etc)
 * 
 * @author Ewald Peiszer
 * @version $Id: ActivityGrid.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class ActivityGrid extends JGrid {
    private static final long serialVersionUID = 1L;

    /** Fixed row height */
    public final int ROW_HEIGHT = 105;

    /** Fixed column width */
    public final int COLUMN_WIDTH = 238;

    /**
     * We need the GridModel here already because
     * <ul>
     * <li>we need the numbers of rows and cols
     * <li>and we handle over the boolean array aabMuted to the costum CellRenderer
     * <p>
     * At this time, we assume that all setSpeakerAt() calls have been made and that the cells that still do not have an
     * speaker assigned can me coloured as "muted" in the grid
     */
    public ActivityGrid(ActivityGridModel mod) {
        super(mod.getRowCount(), mod.getColumnCount());
        this.setGridModel(mod);
        this.setAllColumnWidth(COLUMN_WIDTH);
        this.setAllRowHeight(ROW_HEIGHT);
        // center cell text
        DefaultStyleModel styleModel = (DefaultStyleModel) this.getStyleModel();
        styleModel.getDefaultCellStyle().setHorizontalAlignment(SwingConstants.LEFT);
        styleModel.getDefaultCellStyle().setVerticalAlignment(SwingConstants.CENTER);
        // use own CellRenderer
        styleModel.setRenderer(new Object().getClass(), new MyGridCellRenderer(mod.aabMuted));
        // not assigned cells are marked as muted
        for (int x = 0; x < mod.aasFirstLine.length; x++) {
            for (int y = 0; y < mod.aasFirstLine[x].length; y++) {
                if (mod.aasFirstLine[x][y] == null) {
                    mod.aabMuted[x][y] = true;
                }
            }
        }
    }

    /** this grid is read-only */
    public boolean isCellEditable(int rowIndex, int vColIndex) {
        return false;
    }

    /**
     * Gives a reference of this grid to the gridmodel, which must be a <code>ActivityGridModel</code>
     */
    public void setGridModel(ActivityGridModel mod) {
        super.setGridModel(mod);
        mod.setGrid(this);
    }

    public void setAllColumnWidth(int width) {
        for (int i = 0; i < this.getColumnCount(); i++) {
            this.setColumnWidth(i, width);
        }
    }

    public void setAllRowHeight(int height) {
        for (int i = 0; i < this.getRowCount(); i++) {
            this.setRowHeight(i, height);
        }
    }

    /**
     * only the anchor cell should be selected, nothing else (because otherwise it doesn't look pretty anymore
     */
    @Override
    public boolean isSelected(int row, int col) {
        Point ptAnchor = new Point(getSelectionModel().getAnchorColumn(), getSelectionModel().getAnchorRow());
        // Point ptAnchor = new Point(getSelectionModel().getLeadColumn(), getSelectionModel().getLeadRow()); // lead
        // cell is last selected cell
        Point ptCurrent = new Point(col, row);
        if (ptAnchor.equals(ptCurrent)) {
            return super.isSelected(row, col);
        } else {
            return false;
        }
    }

    @Override
    public Component prepareRenderer(GridCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        // Tooltip
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            /*
             * if (aavGPNs[vColIndex][rowIndex].size() > 0) { if (aasToopTips != null) { jc.setToolTipText(aasToopTips[vColIndex][rowIndex]); } else {
             * //jc.setToolTipText(aavGPNs[vColIndex][rowIndex].get(0).toString()); } } else { jc.setToolTipText("(No units assigned yet to this
             * cell.)"); }
             */
            System.out.print("ist jc!");
            jc.setToolTipText("test !");
        }
        return c;
    }

}