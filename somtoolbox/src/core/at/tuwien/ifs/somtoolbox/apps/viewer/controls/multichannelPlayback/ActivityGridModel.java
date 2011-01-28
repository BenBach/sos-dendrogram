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

import javax.swing.table.TableModel;

import net.sf.jeppers.grid.DefaultGridModel;
import net.sf.jeppers.grid.JGrid;

/**
 * @author Ewald Peiszer
 * @version $Id: ActivityGridModel.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class ActivityGridModel extends DefaultGridModel {
    private static final long serialVersionUID = 1L;

    private StringBuffer sb = new StringBuffer();

    private JGrid grid;

    /** Contains names of mixer */
    protected String[][] aasFirstLine;

    /** Contains song names, is set via method call */
    protected String[][] aasSecondLine;

    /** True for the respective cell if this speaker is currently muted (eg waiting for decoding) */
    public boolean[][] aabMuted;

    /** Creates the model with the same dimensions as the given model */
    public ActivityGridModel(TableModel mod) {
        super(mod.getRowCount(), mod.getColumnCount());
        aasFirstLine = new String[mod.getColumnCount()][mod.getRowCount()];
        aasSecondLine = new String[mod.getColumnCount()][mod.getRowCount()];
        aabMuted = new boolean[mod.getColumnCount()][mod.getRowCount()];
        // inital value is false for all booleans!
        // System.out.println("Test: " + aabMuted[2][2]);
    }

    @Override
    public Object getValueAt(int r, int c) {
        if (aasFirstLine[c][r] != null) {
            sb.replace(0, sb.length(), "<html><p>(");
            sb.append(aasFirstLine[c][r]);
            sb.append(")<br><br>");
            if (aasSecondLine[c][r] != null) {
                sb.append(aasSecondLine[c][r]);
            } else {
                sb.append("(NA)");
            }
            sb.append("</p></html>");
            return sb.toString();
        } else {
            // not used
            return "(not used)";
        }

    }

    /*
     * public Object getValueAt(int r, int c) { if (aasFirstLine[c][r] != null) { sb.replace(0, sb.length(), "<html><p>Speaker:<br>");
     * sb.append(aasFirstLine[c][r]); sb.append("<br><br>"); if (aasSecondLine[c][r] != null) { sb.append(aasSecondLine[c][r]); } else
     * sb.append("(NA)"); sb.append("</p></html>"); return sb.toString(); } else { // not used return "(not used)"; } }
     */

    public void setSpeakerAt(int x, int y, String str) {
        aasFirstLine[x][y] = str;
    }

    public void setSongAt(int x, int y, String str) {
        aasSecondLine[x][y] = "Currently playing:<br><b>" + str + "</b>";
        // if we have the reference to the table, we repaint it
        if (grid != null) {
            grid.repaint();
        }
    }

    public void setDecodingAt(int x, int y, String str) {
        aasSecondLine[x][y] = "Currently decoding:<br>" + str;
        // if we have the reference to the table, we repaint it
        if (grid != null) {
            grid.repaint();
        }
    }

    public void setMutedSpeaker(int x, int y, boolean muted) {
        aabMuted[x][y] = muted;
    }

    /** Stores a reference of the table this table model is displayed in */
    public void setGrid(JGrid grid) {
        this.grid = grid;
    }

    @Override
    public boolean isCellEditable(int parm1, int parm2) {
        return false;
    }
    /*
     * public int getRowCount() { // (must have at least one column) return aasFirstLine[0].length; } public int getColumnCount() { return
     * aasFirstLine.length; }
     */
}