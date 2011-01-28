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
package at.tuwien.ifs.commons.gui.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.JXTitledPanel;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;

/**
 * <p>
 * This component combines a {@link JXCollapsiblePane} with a clickable title bar to toggle between collapsed and
 * expanded state.
 * </p>
 * <p>
 * The methods {@link #add(Component)}, {@link #add(Component, Object)}, {@link #remove(Component)},
 * {@link #remove(int)}, {@link #removeAll()} delegate to {@link JXCollapsiblePane}.
 * </p>
 * <p>
 * Use {@link #setTitle(String)}, {@link #setIcon(Icon)}, {@link #setIconTitleGap(int)},
 * {@link #setContentBackground(Color)}, {@link #setContentBorder(Border)}, {@link #setTitleBackground(Color)} and
 * {@link #setTitleBorder(Border)} so set the appearence.
 * </p>
 * <p>
 * Use {@link #setCollapsed(boolean)} to collapse/expand the contentPane.
 * </p>
 * 
 * @author Jakob Frank
 * @version $Id: TitledCollapsiblePanel.java 3888 2010-11-02 17:42:53Z frank $
 * @see JXCollapsiblePane
 * @see JXTitledPanel
 */
public class TitledCollapsiblePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public enum Orientation {
        TOP, LEFT, RIGHT, BOTTOM;

        private String getBLConstraint() {
            switch (this) {
                case BOTTOM:
                    return BorderLayout.SOUTH;
                case RIGHT:
                    return BorderLayout.EAST;
                case LEFT:
                    return BorderLayout.WEST;
                case TOP:
                default:
                    return BorderLayout.NORTH;
            }
        }

        private Direction getContentDirection() {
            switch (this) {
                case BOTTOM:
                    return Direction.DOWN;
                case RIGHT:
                    return Direction.RIGHT;
                case LEFT:
                    return Direction.LEFT;
                case TOP:
                default:
                    return Direction.UP;
            }
        }

    }

    private Orientation orientation;

    private final TitleRow title;

    public int getDecoratorPadding() {
        return title.getDecoratorPadding();
    }

    public void setDecoratorPadding(int decoratorPadding) {
        title.setDecoratorPadding(decoratorPadding);
    }

    private final JXCollapsiblePane contentCollapser;

    private Container contentContainer;

    public TitledCollapsiblePanel() {
        this("");
    }

    public TitledCollapsiblePanel(String title) {
        this.title = new TitleRow(title);
        this.contentCollapser = new JXCollapsiblePane();
        setContentPane(new JPanel());
        this.setOrientation(Orientation.TOP);
        init();
    }

    public TitledCollapsiblePanel(LayoutManager layout) {
        this("", layout);
    }

    public TitledCollapsiblePanel(String title, CommonSOMViewerStateData state, LayoutManager layout) {
        this(title, layout);
    }

    public TitledCollapsiblePanel(String title, LayoutManager layout) {
        this(title, layout, false);
    }

    public TitledCollapsiblePanel(String title, boolean collapsed) {
        this(title);
        setCollapsed(collapsed);
    }

    public TitledCollapsiblePanel(String title, LayoutManager layout, boolean collapsed) {
        this(title);
        setLayout(layout);
        setCollapsed(collapsed);
    }

    @Override
    public Component add(Component comp) {
        if (contentContainer != null) {
            return contentContainer.add(comp);
        } else {
            return comp;
        }
    }

    @Override
    public void add(Component comp, Object constraints) {
        if (contentContainer != null) {
            contentContainer.add(comp, constraints);
        }
    }

    @Override
    public Component add(Component comp, int index) {
        return contentContainer.add(comp, index);
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        contentContainer.add(comp, constraints, index);
    }

    @Override
    public Component add(String name, Component comp) {
        return contentContainer.add(name, comp);
    }

    public int getHorizontalTitleAlignment() {
        return title.getHorizontalAlignment();
    }

    public int getHorizontalTitleTextPosition() {
        return title.getHorizontalTextPosition();
    }

    public Icon getIcon() {
        return title.getIcon();
    }

    public int getIconTitleGap() {
        return title.getIconTextGap();
    }

    @Override
    public LayoutManager getLayout() {
        return contentContainer.getLayout();
    }

    public String getTitle() {
        return title.getText();
    }

    private void init() {
        super.setLayout(new BorderLayout());

        initTitle();
        initContent();
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        super.add(title, BorderLayout.NORTH);
        super.add(contentCollapser, BorderLayout.CENTER);
    }

    private void initContent() {
        contentCollapser.setBorder(BorderFactory.createEmptyBorder());
        contentCollapser.addPropertyChangeListener("collapsed", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                title.revalidate();
                title.repaint();
                TitledCollapsiblePanel.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
                        evt.getNewValue());
            }
        });
    }

    private void initTitle() {
        title.setBackground(Color.lightGray);
        title.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        title.setFocusPainted(false);
        title.setHorizontalAlignment(SwingConstants.LEFT);
    }

    public void setContentBackground(Color bg) {
        contentCollapser.setBackground(bg);
        contentContainer.setBackground(bg);
    }

    public void setHorizontalTitleAlignment(int alignment) {
        title.setHorizontalAlignment(alignment);
    }

    public void setHorizontalTitleTextPosition(int textPosition) {
        title.setHorizontalTextPosition(textPosition);
    }

    public void setIcon(Icon defaultIcon) {
        title.setIcon(defaultIcon);
    }

    public void setIconTitleGap(int iconTextGap) {
        title.setIconTextGap(iconTextGap);
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        if (contentContainer != null) {
            contentContainer.setLayout(mgr);
        }
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setTitleBackground(Color bg) {
        title.setBackground(bg);
    }

    public void setTitleBorder(Border border) {
        title.setBorder(border);
    }

    public Border getTitleBorder() {
        return title.getBorder();
    }

    public Color getTitleBackground() {
        return title.getBackground();
    }

    /**
     * The title decoration is the expand/collapse icon in the top right corner of the title.
     */
    public void setShowTitleDecoration(boolean show) {
        title.setShowDecorators(show);
    }

    /**
     * see {@link #getShowTitleDecoration()}
     */
    public boolean getShowTitleDecoration() {
        return title.getShowDecorators();
    }

    public Color getContentBackground() {
        return contentContainer.getBackground();
    }

    public Border getContentBorder() {
        return contentCollapser.getBorder();
    }

    public Container getContentPane() {
        return contentContainer;
    }

    public void setContentPane(Container contentPane) {
        contentContainer = contentPane;
        contentCollapser.removeAll();
        contentCollapser.add(contentPane);
    }

    @Override
    public void remove(Component comp) {
        contentContainer.remove(comp);
    }

    @Override
    public void remove(int index) {
        contentContainer.remove(index);
    }

    @Override
    public void removeAll() {
        contentContainer.removeAll();
    }

    public void setContentBorder(Border border) {
        contentCollapser.setBorder(border);
    }

    /**
     * @see JXCollapsiblePane#isAnimated()
     * @return true if the pane is animated, false otherwise
     */
    public boolean isAnimated() {
        return contentCollapser.isAnimated();
    }

    /**
     * @see JXCollapsiblePane#isCollapsed()
     * @return true if the pane is collapsed, false if expanded
     */
    public boolean isCollapsed() {
        return contentCollapser.isCollapsed();
    }

    /**
     * @see JXCollapsiblePane#setAnimated(boolean)
     */
    public void setAnimated(boolean animated) {
        contentCollapser.setAnimated(animated);
    }

    /**
     * @see JXCollapsiblePane#setCollapsed(boolean)
     */
    public void setCollapsed(boolean val) {
        if (val != isCollapsed()) {
            contentCollapser.setCollapsed(val);
        }
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;

        super.removeAll();
        super.add(title, orientation.getBLConstraint());
        super.add(contentCollapser, BorderLayout.CENTER);
        contentCollapser.setDirection(orientation.getContentDirection());
        revalidate();
    }

    public Orientation getOrientation() {
        return orientation;
    }

    private class TitleRow extends JButton {
        private final Icon iconPlus;

        private final Icon iconMinus;

        private static final long serialVersionUID = 1L;

        private int decoratorPadding = 1;

        private boolean showDecorators = true;

        private Icon getToggleIcon() {
            if (isCollapsed()) {
                return iconPlus;
            } else {
                return iconMinus;
            }
        }

        public int getDecoratorPadding() {
            return decoratorPadding;
        }

        public void setDecoratorPadding(int decoratorPadding) {
            this.decoratorPadding = decoratorPadding;
        }

        public TitleRow(String title) {
            this(title, true);
        }

        public TitleRow(String title, boolean fancy) {
            super(title);

            iconPlus = loadIcon(fancy, true);
            iconMinus = loadIcon(fancy, false);

            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    contentCollapser.setCollapsed(!contentCollapser.isCollapsed());
                }
            });
        }

        private Icon loadIcon(boolean fancy, boolean plus) {
            if (!fancy) {
                return new PlaceholderIcon(plus);
            }
            String fname = (plus ? "plus" : "minus") + ".png";
            Icon tmp;
            try {
                tmp = new ImageIcon(ClassLoader.getSystemResource(SOMViewer.RESOURCE_PATH_ICONS + fname));
            } catch (Exception e) {
                tmp = new PlaceholderIcon(plus);
            }
            return tmp;
        }

        @Override
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            if (!getShowDecorators()) {
                return;
            }

            Graphics myG = g.create();
            myG.setColor(getForeground());

            Icon icon = getToggleIcon();
            icon.paintIcon(TitleRow.this, myG, TitleRow.this.getWidth() - TitleRow.this.getInsets().right
                    - decoratorPadding - icon.getIconWidth(), TitleRow.this.getInsets().top + decoratorPadding);
        }

        @Override
        public final Dimension getMinimumSize() {
            Dimension superMin = super.getMinimumSize();
            superMin.height = Math.max(superMin.height, getToggleIcon().getIconHeight() + 2 * decoratorPadding
                    + getInsets().top + getInsets().bottom);
            superMin.width = Math.max(superMin.width, getToggleIcon().getIconWidth() + 2 * decoratorPadding
                    + getInsets().left + getInsets().right);
            return superMin;
        }

        @Override
        public final Dimension getPreferredSize() {
            Dimension superPref = super.getPreferredSize();
            superPref.height = Math.max(superPref.height, getToggleIcon().getIconHeight() + 2 * decoratorPadding
                    + getInsets().top + getInsets().bottom);
            superPref.width = Math.max(superPref.width, getToggleIcon().getIconWidth() + 2 * decoratorPadding
                    + getInsets().left + getInsets().right);
            return superPref;
        }

        public void setShowDecorators(boolean showDecorators) {
            this.showDecorators = showDecorators;
        }

        public boolean getShowDecorators() {
            return showDecorators;
        }

        private class PlaceholderIcon implements Icon {
            private final boolean isPlus;

            private int size = 11;

            private boolean showFrame = true;

            public PlaceholderIcon(boolean isPlus) {
                this(isPlus, 11);
            }

            public PlaceholderIcon(boolean isPlus, int size) {
                this.isPlus = isPlus;
                this.size = size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics myG = g.create();

                myG.setColor(c.getForeground());

                int frameGap = size / 5;
                if (!showFrame) {
                    frameGap = 0;
                } else {
                    myG.drawRect(x, y, size - 1, size - 1);
                }
                myG.drawLine(x + frameGap, y + size / 2, x + size - frameGap - 1, y + size / 2);
                if (isPlus) {
                    myG.drawLine(x + size / 2, y + frameGap, x + size / 2, y + size - frameGap - 1);
                }

            }

        }
    }

    // FIXME: Useless.
    @Deprecated
    public void pack() {

    }

    /**
     * Do not override this method. Use {@link #setPreferredSize(Dimension)} if necessary!
     * 
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public final Dimension getPreferredSize() {
        return super.getPreferredSize();
    }

}
