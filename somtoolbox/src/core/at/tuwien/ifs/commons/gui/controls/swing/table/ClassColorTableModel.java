/*
 * Copyright 2004-2010 Institute of Software Technology and Interactive Systems, Vienna University of Technology
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

/**
 * @author Jakob Frank
 * @version $Id: ClassColorTableModel.java 3943 2010-11-22 14:22:07Z frank $
 */
public class ClassColorTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int NAME_COLUMN_INDEX = 0;

    public static final int COLOR_COLUMN_INDEX = 1;

    public static final int SELECT_COLUMN_INDEX = 2;

    private static String[] columnTitles = { "Name", "Color", "Show" };

    final private Color[] classColors;

    final private String[] classNames;

    final private boolean[] classSelected;

    private boolean selectionAllowed;

    public ClassColorTableModel() {
        this(new String[0], new Color[0]);
    }

    public ClassColorTableModel(String[] names, Color[] colors) {
        if (names.length != colors.length) {
            throw new IllegalArgumentException("names.length and colors.length differ!");
        }
        classNames = names;
        classColors = colors;
        classSelected = new boolean[names.length];
        for (int i = 0; i < classSelected.length; i++) {
            classSelected[i] = true;
        }

        selectionAllowed = true;
    }

    public void setAllSelected(boolean selected) {
        for (int i = 0; i < classSelected.length; i++) {
            classSelected[i] = selected;
        }
        fireTableDataChanged();
    }

    public void setSelected(int rowIndex, boolean selected) {
        classSelected[rowIndex] = selected;
        fireTableDataChanged();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return classNames.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return (selectionAllowed ? SELECT_COLUMN_INDEX : COLOR_COLUMN_INDEX) + 1;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int columnIndex) {
        return columnTitles[columnIndex];
    }

    public void setColumnName(int columnIndex, String name) {
        columnTitles[columnIndex] = name;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLOR_COLUMN_INDEX:
                return Color.class;
            case SELECT_COLUMN_INDEX:
                return Boolean.class;
            default:
                return String.class;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != NAME_COLUMN_INDEX;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case NAME_COLUMN_INDEX:
                return classNames[rowIndex];
            case COLOR_COLUMN_INDEX:
                return classColors[rowIndex];
            case SELECT_COLUMN_INDEX:
                return classSelected[rowIndex];
            default:
                return null;
        }

    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case COLOR_COLUMN_INDEX:
                classColors[rowIndex] = (Color) aValue;
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
            case SELECT_COLUMN_INDEX:
                classSelected[rowIndex] = (Boolean) aValue;
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        String[] names = new String[] { "Rot", "Gelb", "Grün", "Blau", "Rosa" };
        Color[] colors = new Color[] { Color.red, Color.yellow, Color.green, Color.blue, Color.magenta };

        final ClassColorTableModel model = new ClassColorTableModel(names, colors);

        final JTable table = createColorLegendTable(model);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(240, 120));

        JScrollPane scr = new JScrollPane(table);

        p.add(scr, BorderLayout.CENTER);

        final JTextField txtFilter = new JTextField();
        final TableRowSorter<ClassColorTableModel> sorter = new TableRowSorter<ClassColorTableModel>(model);
        table.setRowSorter(sorter);

        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            private void actionPerformed(DocumentEvent e) {
                RowFilter<ClassColorTableModel, Integer> rf = null;
                try {
                    final Matcher matcher = Pattern.compile(txtFilter.getText(), Pattern.CASE_INSENSITIVE).matcher("");
                    rf = new RowFilter<ClassColorTableModel, Integer>() {
                        @Override
                        public boolean include(
                                javax.swing.RowFilter.Entry<? extends ClassColorTableModel, ? extends Integer> entry) {
                            matcher.reset(entry.getModel().getClassName(entry.getIdentifier()));
                            return matcher.find();
                        }
                    };
                } catch (java.util.regex.PatternSyntaxException ex) {
                    return;
                }
                sorter.setRowFilter(rf);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actionPerformed(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                actionPerformed(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        p.add(txtFilter, BorderLayout.NORTH);

        final JToggleButton tb = new JToggleButton("Selectable");
        tb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                model.selectionAllowed = tb.isSelected();
                model.fireTableStructureChanged();
            }
        });
        p.add(tb, BorderLayout.SOUTH);

        f.setContentPane(p);

        f.pack();
        f.setVisible(true);
    }

    /**
     * Creates a ColorLegendTable to be used with the required default listeners added.
     * 
     * @param theModel the {@link ClassColorTableModel}
     * @return a JTable with nice default settings
     */
    public static JTable createColorLegendTable(ClassColorTableModel theModel) {
        final JTable table = new JTable();

        table.setModel(theModel);
        table.setDefaultRenderer(Color.class, new ColorCellRenderer(true));
        table.setDefaultEditor(Color.class, new ColorCellEditor());

        /*
         * Handle clicks on the 3rd column header (booleans) 
         */
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    TableColumnModel cm = table.getColumnModel();

                    int tColIndex = cm.getColumnIndexAtX(e.getX());
                    if (tColIndex < 0) {
                        return;
                    }
                    if (table.getModel() instanceof ClassColorTableModel) {
                        ClassColorTableModel cctm = (ClassColorTableModel) table.getModel();
                        int colIndex = table.convertColumnIndexToModel(tColIndex);
                        if (colIndex == ClassColorTableModel.SELECT_COLUMN_INDEX) {
                            cctm.setAllSelected(!cctm.isAllSelected());
                        }
                    }

                }
            }
        });

        /*
         * Use a custom HeaderRenderer (Checkbox in Header) 
         */
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            private JCheckBox box = null;

            private JLabel lbl = null;

            private ClassColorTableModel model = null;

            @Override
            public Component getTableCellRendererComponent(final JTable tables, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                if (model == null || !tables.getModel().equals(model)) {
                    lbl = null;
                    box = null;
                    if (tables.getModel() instanceof ClassColorTableModel) {
                        model = (ClassColorTableModel) tables.getModel();
                    } else {
                        model = null;
                    }
                }

                if (model == null
                        || table.convertColumnIndexToModel(column) != ClassColorTableModel.SELECT_COLUMN_INDEX) {
                    if (lbl == null) {
                        lbl = new JLabel(value.toString());

                        lbl.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                        lbl.setHorizontalTextPosition(SwingConstants.LEADING);

                        lbl.setFont(tables.getTableHeader().getFont());
                        lbl.setForeground(tables.getTableHeader().getForeground());
                        lbl.setBackground(tables.getTableHeader().getBackground());

                        lbl.setEnabled(tables.getTableHeader().isEnabled());
                    }
                    lbl.setText(value.toString());
                    return lbl;
                } else {
                    if (box == null) {
                        box = new JCheckBox(value.toString());

                        box.setSelected(model.isAllSelected());

                        box.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                        box.setBorderPainted(true);
                        box.setHorizontalAlignment(SwingConstants.CENTER);
                        box.setHorizontalTextPosition(SwingConstants.LEADING);

                        box.setFont(tables.getTableHeader().getFont());
                        box.setForeground(tables.getTableHeader().getForeground());
                        box.setBackground(tables.getTableHeader().getBackground());

                        box.setEnabled(tables.getTableHeader().isEnabled());

                        model.addTableModelListener(new TableModelListener() {

                            @Override
                            public void tableChanged(TableModelEvent e) {
                                box.setSelected(model.isAllSelected());
                                table.getTableHeader().revalidate();
                                table.getTableHeader().repaint();
                            }
                        });
                    }
                    return box;
                }
            }
        });

        /*
         * Use a context menu to allow multi-selection
         */
        table.addMouseListener(new MouseAdapter() {

            private JPopupMenu pop = null;

            private ClassColorTableModel model = null;

            private void setSelectedClassesVisible(boolean visible) {
                if (model != null) {
                    for (int i : table.getSelectedRows()) {
                        model.setSelected(table.convertRowIndexToModel(i), visible);
                    }
                }
            }

            private void triggerPopup(MouseEvent e) {
                if (model == null || !table.getModel().equals(model)) {
                    if (table.getModel() instanceof ClassColorTableModel) {
                        model = (ClassColorTableModel) table.getModel();
                    } else {
                        model = null;
                    }
                }

                if (model != null && model.isSelectionAllowed() && e.isPopupTrigger()
                        && table.getSelectedRowCount() > 0) {
                    if (pop == null) {
                        pop = new JPopupMenu();
                        JMenuItem selAll = new JMenuItem("Show selected classes");
                        selAll.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                setSelectedClassesVisible(true);
                            }
                        });
                        JMenuItem hidAll = new JMenuItem("Hide selected classes");
                        hidAll.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                setSelectedClassesVisible(false);
                            }
                        });
                        pop.add(selAll);
                        pop.add(hidAll);
                    }
                    pop.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                triggerPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                triggerPopup(e);
            }

        });

        /*
         * Fix column widths
         */
        fixColumnWidth(table, SELECT_COLUMN_INDEX, true, true);
        fixColumnWidth(table, COLOR_COLUMN_INDEX, true, false);
        fixColumnWidth(table, NAME_COLUMN_INDEX, true, false);

        return table;
    }

    private static void fixColumnWidth(JTable table, int columnIndex, boolean setMin, boolean setMax) {

        final TableColumn column = table.getColumnModel().getColumn(columnIndex);
        TableCellRenderer hRenderer = column.getHeaderRenderer();
        if (hRenderer == null) {
            hRenderer = table.getTableHeader().getDefaultRenderer();
        }
        final Component hRendererComponent = hRenderer.getTableCellRendererComponent(table, columnTitles[columnIndex],
                false, false, -1, columnIndex);

        int contentWidth = 0;
        for (int i = 0; i < table.getRowCount(); i++) {
            TableCellRenderer cRenderer = column.getCellRenderer();
            if (cRenderer == null) {
                cRenderer = table.getDefaultRenderer(table.getModel().getColumnClass(columnIndex));
            }
            final Component cRendererConponent = cRenderer.getTableCellRendererComponent(table,
                    table.getModel().getValueAt(i, columnIndex), false, false, i, columnIndex);
            contentWidth = Math.max(contentWidth, cRendererConponent.getPreferredSize().width);
        }

        final int headerWidth = Math.max(hRendererComponent.getPreferredSize().width, contentWidth);

        column.setPreferredWidth((int) Math.ceil(headerWidth * 1.2f));
        if (setMin) {
            column.setMinWidth((int) Math.ceil(headerWidth * 1.2f));
        }
        if (setMax) {
            column.setMaxWidth((int) Math.ceil(headerWidth * 1.2f));
        }

    }

    public boolean isAllSelected() {
        boolean res = true;
        for (boolean element : classSelected) {
            res &= element;
            if (!res) {
                break;
            }
        }
        return res;
    }

    public String[] getSelectedClasses() {
        ArrayList<String> l = new ArrayList<String>();
        for (int i = 0; i < classNames.length; i++) {
            if (classSelected[i]) {
                l.add(classNames[i]);
            }
        }
        return l.toArray(new String[l.size()]);
    }

    public int[] getSelectedClassIndices() {
        ArrayList<Integer> l = new ArrayList<Integer>();
        for (int i = 0; i < classNames.length; i++) {
            if (classSelected[i]) {
                l.add(i);
            }
        }
        int[] r = new int[l.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = l.get(i).intValue();
        }
        return r;
    }

    public Color[] getColors() {
        // return classColors;
        Color[] c = new Color[classColors.length];
        for (int i = 0; i < c.length; i++) {
            if (classSelected[i]) {
                c[i] = classColors[i];
            } else {
                c[i] = new Color(0, 0, 0, 0);
            }
        }
        return c;
    }

    public Color getColor(int row) {
        return classColors[row];
    }

    public String[] getClassNames() {
        return classNames;
    }

    public String getClassName(int row) {
        return classNames[row];
    }

    /**
     * @return the longest ClassName
     */
    public String getLongestName() {
        String s = "";
        int l = 0;
        for (String className : classNames) {
            if (className.length() > l) {
                s = className;
                l = s.length();
            }
        }
        return s;
    }

    public boolean isSelectionAllowed() {
        return selectionAllowed;
    }

    public void setSelectionAllowed(boolean selectionAllowed) {
        this.selectionAllowed = selectionAllowed;
        fireTableStructureChanged();
    }

    public static BufferedImage asBufferedImage(ClassColorTableModel theModel) {
        return asBufferedImage(theModel, false);
    }

    /**
     * @param theModel The model to paint
     * @param showAllRows paint all rows or just selected
     * @return a buffered image.
     */
    public static BufferedImage asBufferedImage(ClassColorTableModel theModel, boolean showAllRows) {
        final ClassColorTableModel m = new ClassColorTableModel(theModel.getClassNames(), theModel.getColors());
        m.setSelected(theModel.getSelectedClassIndices());
        m.setSelectionAllowed(false);

        JTable table = new JTable(m);
        table.setDefaultRenderer(Color.class, new ColorCellRenderer(false));

        if (!showAllRows) {
            TableRowSorter<ClassColorTableModel> sorter = new TableRowSorter<ClassColorTableModel>(m);
            table.setRowSorter(sorter);
            sorter.setRowFilter(new RowFilter<ClassColorTableModel, Integer>() {

                @Override
                public boolean include(
                        javax.swing.RowFilter.Entry<? extends ClassColorTableModel, ? extends Integer> entry) {
                    return m.classSelected[entry.getIdentifier().intValue()];
                }
            });
        }
        fixColumnWidth(table, NAME_COLUMN_INDEX, true, true);
        fixColumnWidth(table, COLOR_COLUMN_INDEX, true, true);

        Dimension size = table.getPreferredSize();
        table.setSize(size);
        // FIXME: For some reason this does not work...
        BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        table.paint(g);
        return bi;
    }

    public void setSelected(int[] selectedClassIndices) {
        setAllSelected(false);
        for (int i = 0; i < selectedClassIndices.length; i++) {
            classSelected[selectedClassIndices[i]] = true;
        }
        fireTableDataChanged();
    }

}
