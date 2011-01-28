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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import at.tuwien.ifs.commons.gui.controls.swing.table.ClassColorTableModel;
import at.tuwien.ifs.commons.gui.controls.swing.table.ColorCellEditor;
import at.tuwien.ifs.commons.gui.controls.swing.table.ColorCellRenderer;

/**
 * Displays a name-colour legend, i.e. the names of different items and the colours attached to them, as a table. In a
 * special mode, it is possible to set a limit and offset to display only parts of the given data arrays (see
 * {@link #setData(String[], Color[], int, int)}) - this can be useful e.g. if the data shall be split up in several
 * tables that are arranged horizontally
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: ColourLegendTable.java 3935 2010-11-12 12:57:45Z frank $
 * @deprecated use {@link ClassColorTableModel} and an ordinary {@link JTable} instead.
 */
@Deprecated
public class ColourLegendTable extends JTable {
    private static final long serialVersionUID = 2L;

    private String[] names = null;

    private Color[] colors = null;

    private String[] columnNames;

    private int length;

    public ColourLegendTable() { // empty constructor meant for initial init w/o names/colours
    }

    public ColourLegendTable(String[] columnNames, ListSelectionListener listener) {
        this(null, null, columnNames, listener);
    }

    public ColourLegendTable(String[] names, Color[] colors, String[] columnNames, ListSelectionListener listener) {
        super();
        setDefaultRenderer(Color.class, new ColorCellRenderer(true));
        setDefaultEditor(Color.class, new ColorCellEditor());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (listener != null) {
            ListSelectionModel listSelectionModel = getSelectionModel();
            listSelectionModel.addListSelectionListener(listener);
        }
        this.columnNames = columnNames;
        if (names != null && colors != null) {
            setData(names, colors, 0, names.length);
        }
    }

    public void setData(String[] names, Color[] colors, int offset, int limit) {
        this.names = names;
        this.colors = colors;
        this.length = limit - offset;
        Object[][] data = new Object[limit][2];
        for (int c = offset; c < offset + limit; c++) {
            data[c - offset][0] = names[c];
            data[c - offset][1] = colors[c];
        }
        setModel(new ClassColorTableModel(names, colors));
        if (names.length > 0) {
            initColumnSizes();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.JTable#setModel(javax.swing.table.TableModel)
     */
    @Override
    public void setModel(TableModel dataModel) {
        if (dataModel instanceof ClassColorTableModel) {
            super.setModel(dataModel);
        } else {
            throw new IllegalArgumentException("Must set a ClassColorTableModel");
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.JTable#getModel()
     */
    @Override
    public ClassColorTableModel getModel() {
        return (ClassColorTableModel) super.getModel();
    }

    private void initColumnSizes() {
        ClassColorTableModel model = getModel();
        TableCellRenderer headerRenderer = getTableHeader().getDefaultRenderer();
        TableColumn column = getColumnModel().getColumn(0);
        int headerWidth = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0,
                0).getPreferredSize().width;
        int cellWidth = getDefaultRenderer(model.getColumnClass(0)).getTableCellRendererComponent(this,
                model.getLongestName(), false, false, 0, 0).getPreferredSize().width;
        column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        getColumnModel().getColumn(1).setPreferredWidth(25);
    }

    public Color[] getColors() {
        return colors;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i = 0; i < getComponents().length; i++) {
            // getComponents()[i].setEnabled(enabled);
        }
    }

    public BufferedImage asBufferedImage() {
        // this is a simple approach to export the class legend, by exporting it exactly as it is drawn
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        paint(g);
        return image;
    }

}
