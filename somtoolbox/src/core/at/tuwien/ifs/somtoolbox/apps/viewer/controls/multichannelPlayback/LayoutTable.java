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
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MultichannelPlaybackPanel;

/**
 * Used in the {@link MultichannelPlaybackPanel}
 * 
 * @author Ewald Peiszer
 * @version $Id: LayoutTable.java 3877 2010-11-02 15:43:17Z frank $
 */
@SuppressWarnings("rawtypes")
public class LayoutTable extends JTable {
    private static final long serialVersionUID = 1L;

    private Vector[][] aavGPNs = null;

    private String[][] aasToopTips = null;

    public LayoutTable(ListSelectionListener lsl, Vector[][] aavGPNs) {
        this.aavGPNs = aavGPNs;

        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(true);
        this.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.setTableHeader(null);
        this.getSelectionModel().addListSelectionListener(lsl);
        this.getColumnModel().getSelectionModel().addListSelectionListener(lsl);

    }

    public void setToolTips(String[][] aas) {
        this.aasToopTips = aas;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);

        if (isCellSelected(rowIndex, vColIndex)) {
            // selected
            c.setBackground(getSelectionBackground());
        } else {
            if (aavGPNs[vColIndex][rowIndex].size() > 0) {
                // colour
                c.setBackground(Commons.HIGHLIGHT);
            } else {
                // no colour
                c.setBackground(getBackground());
            }
        }

        // Tooltip
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            if (aavGPNs[vColIndex][rowIndex].size() > 0) {
                if (aasToopTips != null) {
                    jc.setToolTipText(aasToopTips[vColIndex][rowIndex]);
                } else {
                    // jc.setToolTipText(aavGPNs[vColIndex][rowIndex].get(0).toString());
                }
            } else {
                jc.setToolTipText("(No units assigned yet to this cell.)");
            }
        }
        return c;
    }

}