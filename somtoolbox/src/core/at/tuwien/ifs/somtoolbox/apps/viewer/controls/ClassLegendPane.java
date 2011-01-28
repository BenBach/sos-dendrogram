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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import at.tuwien.ifs.commons.gui.controls.swing.table.ClassColorTableModel;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;

/**
 * Displays the class legend, i.e. the names of the different classes and the colours attached to them. Class colours
 * can be changed; the assignment can also be save and loaded to/from a file.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @author Thomas Lidy
 * @version $Id: ClassLegendPane.java 3939 2010-11-17 16:06:14Z frank $
 */
public class ClassLegendPane extends AbstractViewerControl {

    private static final long serialVersionUID = 1L;

    // private static final String[] columnNames = new String[] { "Class Name", "Color" };

    // private ColourLegendTable table = null;

    private JTable table = null;

    private ClassColorTableModel classColorTM = null;

    private JLabel noClassesLoaded = null;

    private SOMPane mapPane = null;

    private JFileChooser fc;

    private JTextField txtFilter = null;

    public ClassLegendPane(SOMPane mapPane, String title, CommonSOMViewerStateData state) {
        super(title, state, new GridBagLayout());
        this.mapPane = mapPane;

        if (mapPane.getClassLegendNames() != null) {
            // this.colors = state.inputDataObjects.getClassInfo().getClassColors();
            initClassTable();
            setVisible(true);
        } else {
            initNoClassInfo();
            setVisible(false);
        }
    }

    public void initNoClassInfo() {
        if (table != null) {
            remove(table);
        }
        if (noClassesLoaded != null) {
            remove(noClassesLoaded);
        } else {
            noClassesLoaded = new JLabel("No class information file was loaded!");
        }
        add(noClassesLoaded);
        repaint();
    }

    public void initClassTable() {
        GridBagConstraintsIFS b = new GridBagConstraintsIFS(GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH).setWeights(
                1, 1);

        if (table != null) {
            remove(table);
        }
        if (noClassesLoaded != null) {
            remove(noClassesLoaded);
        }

        classColorTM = new ClassColorTableModel(mapPane.getClassLegendNames(), mapPane.getClassLegendColors());
        table = ClassColorTableModel.createColorLegendTable(classColorTM);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(state.controlElementsWidth, 120));

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());
        JLabel lblFilter = new JLabel("Filter:");
        JButton btnClearFilter = new JButton("x");
        btnClearFilter.setMargin(new Insets(0, 0, 0, 0));

        txtFilter = new JTextField();
        final TableRowSorter<ClassColorTableModel> sorter = new TableRowSorter<ClassColorTableModel>(classColorTM);
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
        lblFilter.setLabelFor(txtFilter);
        btnClearFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtFilter.setText("");
            }
        });
        final GridBagConstraintsIFS c = new GridBagConstraintsIFS(GridBagConstraints.BASELINE_LEADING,
                GridBagConstraints.BOTH);
        filterPanel.add(lblFilter, c);
        filterPanel.add(txtFilter, c.nextCol().setWeightX(1.0));
        filterPanel.add(btnClearFilter, c.nextCol().resetWeights());

        if (classColorTM.getRowCount() > 9) {
            add(filterPanel, b.nextRow().setGridWidth(3));
        }

        // Jakob: to propagate color-changes
        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() == 1) {
                    TableModel tm = table.getModel();
                    for (int i = 0; i < tm.getRowCount(); i++) {
                        if (i == TableModelEvent.HEADER_ROW) {
                            continue;
                        }
                        Color c = (Color) tm.getValueAt(i, 1);
                        state.inputDataObjects.getClassInfo().setClassColor(i, c);
                        // state.mapPNode.setClassColor(i, c);
                        state.mapPNode.setClassColors(((ClassColorTableModel) tm).getColors());
                    }
                } else if (e.getColumn() == 2 || e.getColumn() == -1) {
                    mapPane.updateClassSelection(classColorTM.getSelectedClassIndices());
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        add(sp, b.nextRow().setGridWidth(3));

        final JCheckBox chkShowOnlySelectedPieSegments = new JCheckBox("Show only selected classes");
        chkShowOnlySelectedPieSegments.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mapPane.setShowOnlySelectedClasses(chkShowOnlySelectedPieSegments.isSelected());
            }
        });
        add(chkShowOnlySelectedPieSegments, b.nextRow().setAnchor(GridBagConstraints.EAST));

        // init load/save buttons
        JButton btLoadColors = new JButton("Load Colours");
        btLoadColors.setMargin(new Insets(2, 2, 1, 2));
        btLoadColors.setFont(smallFont);
        btLoadColors.addActionListener(new ActionListener() {
            // listener for loading
            @Override
            public void actionPerformed(ActionEvent e) {
                initFileChooser();
                fc.showOpenDialog(ClassLegendPane.this);
                File file = fc.getSelectedFile();
                if (file == null) {
                    return;
                }

                if (state.inputDataObjects.getClassInfo().loadClassColours(file)) {
                    // then update visualization
                    updateClassColours();
                    mapPane.repaint();
                }
            }
        });
        add(btLoadColors, b.nextRow().setGridWidth(1).resetWeights());

        JButton btSaveColors = new JButton("Save Colours");
        btSaveColors.setFont(smallFont);
        btSaveColors.setMargin(new Insets(2, 2, 1, 2));
        btSaveColors.addActionListener(new ActionListener() {
            // listener for saving
            @Override
            public void actionPerformed(ActionEvent e) {
                if (classColorTM.getColors() != null) {
                    initFileChooser();
                    fc.showSaveDialog(ClassLegendPane.this);
                    File file = fc.getSelectedFile();
                    if (file == null) {
                        return;
                    }

                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Saving colors to file: " + file.toString());

                    try {
                        FileWriter fw = new FileWriter(file);
                        final Color[] colors = classColorTM.getColors();
                        for (Color color : colors) {
                            fw.write(color.getRed() + " " + color.getGreen() + " " + color.getBlue() + "\n");
                        }
                        fw.close();
                    } catch (IOException ex) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                                "Could not write colors to file! " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        });
        add(btSaveColors, b.nextCol());

        JButton btExportlegend = new JButton("Export Legend");
        btExportlegend.setFont(smallFont);
        btExportlegend.setMargin(new Insets(2, 2, 1, 2));
        btExportlegend.addActionListener(new ActionListener() {
            // listener for exporting the class legend to an image file
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = ExportUtils.getFilePath(ClassLegendPane.this, state.getFileChooser(),
                        "Export class legend to");
                if (file != null) {
                    try {
                        FileUtils.saveImageToFile(file.getAbsolutePath(),
                                ClassColorTableModel.asBufferedImage(classColorTM, true));
                    } catch (SOMToolboxException e1) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                                "Could not write class legend to file '" + file.getAbsolutePath() + "': "
                                        + e1.getMessage());
                    }
                }
            }
        });
        add(btExportlegend, b.nextCol());

    }

    private void initFileChooser() {
        if (fc == null) {
            fc = new JFileChooser(state.fileChooser.getCurrentDirectory());
        }
    }

    public void updateClassColours() {
        for (int i = 0; i < table.getRowCount(); i++) {
            table.setValueAt(state.inputDataObjects.getClassInfo().getClassColors()[i], i, 1);
        }
    }

    private class ClassSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            // System.out.println("value changed");
            Vector<Integer> selIndices = new Vector<Integer>();
            if (e.getValueIsAdjusting() == false) {
                if (lsm.isSelectionEmpty()) {
                    // no selection
                    mapPane.updateClassSelection(null);
                } else {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    for (int i = minIndex; i <= maxIndex; i++) {
                        if (lsm.isSelectedIndex(i)) {
                            selIndices.addElement(new Integer(i));
                        }
                    }
                    int[] selectedIndices = new int[selIndices.size()];
                    for (int i = 0; i < selectedIndices.length; i++) {
                        selectedIndices[i] = selIndices.elementAt(i).intValue();
                    }
                    mapPane.updateClassSelection(selectedIndices);
                }
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        table.setEnabled(enabled);
    }

    public Color[] getColors() {
        return classColorTM.getColors();
    }

    /* (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractViewerControl#isFullFunctional()
     */
    @Override
    public boolean isFullFunctional() {
        return mapPane.getClassLegendNames() != null;
    }

}
