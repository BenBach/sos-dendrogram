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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;

/**
 * This class provides utility methods for User-Interfaces.
 * 
 * @author Rudolf Mayer
 * @version $Id: UiUtils.java 3863 2010-10-20 16:25:06Z mayer $
 */
public class UiUtils {

    /**
     * Places the given component in the middle of the screen.<br>
     * Actually intended for {@link JFrame} and {@link JDialog}, but {@link java.awt.Component} is superclass of both.
     */
    public static void centerWindow(Component window) {
        // Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = window.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        window.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    public static void setSOMToolboxLookAndFeel() {
        try {
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticLookAndFeel");
        } catch (Exception e) {
            try {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Could not find JGoodies Look & Feel - defaulting to cross-platform Look & Feel.");
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e2) {
            }
        }
    }

    public static void recursiveAddBorderToPanel(JComponent c) {
        if (c instanceof JPanel) {
            JPanel panel = (JPanel) c;

            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE),
                    panel.getName()));

            for (Component ch : panel.getComponents()) {
                if (ch instanceof JComponent) {
                    recursiveAddBorderToPanel((JComponent) ch);
                }
            }
        }
    }

    public static ImageIcon getIcon(String imageName) {
        return getIcon(SOMViewer.RESOURCE_PATH_ICONS, imageName);
    }

    public static ImageIcon getIcon(String path, String imageName) {
        ImageIcon icon = new ImageIcon();
        if (imageName != null && imageName.length() > 0) {
            URL imageURL = ClassLoader.getSystemResource(path + imageName);
            if (imageURL != null) {
                icon = new ImageIcon(imageURL);
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Image resource for button '" + imageName + "' not found in path '"
                                + SOMViewer.RESOURCE_PATH_ICONS + "'.");
            }
        }
        return icon;
    }

    public static JLabel makeLabelWithTooltip(String labelText, String tooltipText) {
        JLabel label = new JLabel(labelText);
        label.setToolTipText(tooltipText);
        return label;
    }

    public static JPanel makeAndFillPanel(Component... components) {
        JPanel panel = new JPanel();
        return fillPanel(panel, components);
    }

    public static JPanel fillPanel(JPanel panel, Component... components) {
        for (Component component : components) {
            panel.add(component);
        }
        return panel;
    }

    public static AbstractButton setToolbarButtonDetails(AbstractButton button, ActionListener listener,
            String imageName, String toolTipText, String altText, boolean isSelected) {
        URL imageURL = ClassLoader.getSystemResource(SOMViewer.RESOURCE_PATH_ICONS + imageName);
        button.setActionCommand(toolTipText);
        button.setToolTipText(toolTipText);
        button.addActionListener(listener);
        button.setSelected(isSelected);

        if (imageURL != null) {
            button.setIcon(new ImageIcon(imageURL, altText));
        } else {
            button.setText(altText);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Image resource for button not found. This should not happen, continuing anyway.");
        }

        return button;
    }

    public static JRadioButton makeRadioButton(String text, ButtonGroup buttonGroup) {
        return makeRadioButton(text, buttonGroup, false);
    }

    public static JRadioButton makeRadioButton(String text, ButtonGroup buttonGroup, boolean selected) {
        return makeRadioButton(text, text, buttonGroup, selected);
    }

    public static JRadioButton makeRadioButton(String text, String actionCommand, ButtonGroup buttonGroup,
            boolean selected) {
        JRadioButton rb = new JRadioButton(text);
        rb.setSelected(selected);
        rb.setActionCommand(actionCommand);
        buttonGroup.add(rb);
        return rb;
    }

    public static JPanel makeBorderedPanel(String borderTitle) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(borderTitle));
        return panel;
    }

    public static JPanel makeBorderedPanel(LayoutManager layout, String borderTitle) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(new TitledBorder(borderTitle));
        return panel;
    }

    public static Font scaleFont(Component comp, double scale) {
        return UiUtils.scaleFont(comp.getFont(), scale);
    }

    public static Font scaleFont(Font font, double scale) {
        return new Font(font.getName(), font.getStyle(), (int) (font.getSize() * scale));
    }

    /** Sets the preferred width of all visible columns, using {@link #packColumn(JTable, int, int)} */
    public static void packColumns(JTable table, int margin) {
        for (int c = 0; c < table.getColumnCount(); c++) {
            packColumn(table, c, margin);
        }
    }

    /**
     * Sets the preferred width of the visible column specified by vColIndex. The column will be just wide enough to
     * show the column head and the widest cell in the column. Margin pixels are added to the left and right (resulting
     * in an additional width of 2*margin pixels).
     */
    public static void packColumn(JTable table, int vColIndex, int margin) {
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        TableColumn col = colModel.getColumn(vColIndex);
        int width = 0;

        // Get width of column header
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
        width = comp.getPreferredSize().width;

        // Get maximum width of column data
        for (int r = 0; r < table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, vColIndex);
            comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false, r,
                    vColIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        // Add margin
        width += 2 * margin;

        // Set the width
        col.setPreferredWidth(width);
    }

    public static void reSizeColumn(TableColumn col, int min, int preferred, int max) {
        col.setMinWidth(min);
        col.setPreferredWidth(preferred);
        col.setMaxWidth(max);
    }

    public static Dimension getMaxUsableScreenSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // we don't directly use the width from the Toolkit, as on multi-screen twin displays, it gives the total
        // width of all displays
        // thus, we take the smaller value of the toolkit's and the graphics environment
        // we don't take the graphics environment directly, as that gives the total, and not just the usable width
        // i.e. it doesn't take a left/right (i.e. vertical) toolbar into account
        // thus, this approach should work fine on single-display screens with a vertical toolbar,
        // as well as on multi-screen displays w/o such a vertical toolbar
        // it will most probably not work well on multi-screen displays with a vertical toolbar, as the frame will
        // be too wide
        screenSize.width = Math.min(screenSize.width,
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth());
        screenSize.height -= 48;
        return screenSize;
    }

    public static JButton createBrowseButton(final JTextField txtInput, final Window parent,
            final boolean directoryBrowser) {
        JButton btnChooser = new JButton();
        btnChooser.setText("...");
        btnChooser.setToolTipText("Browse for the " + (directoryBrowser ? "directory" : "file"));
        btnChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(directoryBrowser ? JFileChooser.DIRECTORIES_ONLY
                        : JFileChooser.FILES_ONLY);
                if (txtInput.getText().trim().length() > 0) {
                    File current = new File(txtInput.getText());
                    if (directoryBrowser && !current.isDirectory()) {
                        current = current.getParentFile();
                    }
                    fileChooser.setCurrentDirectory(current);
                }

                int result;
                result = fileChooser.showOpenDialog(parent);
                if (result == JFileChooser.APPROVE_OPTION) {
                    final File selectedFile = fileChooser.getSelectedFile();
                    txtInput.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        return btnChooser;
    }

}
