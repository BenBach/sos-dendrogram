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
package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.util.Hashtable;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;

/**
 * An abstract visualiser that displays properties of a single selected item, e.g. a componentn plane as in
 * {@link ComponentPlanesVisualizer}, or an input vector as in {@link ActivityHistogram}.
 * 
 * @author Rudolf Mayer
 * @version $Id: AbstractItemVisualizer.java 3874 2010-11-02 14:14:38Z mayer $
 */
public abstract class AbstractItemVisualizer extends AbstractMatrixVisualizer {
    protected int currentElement = 0;

    public AbstractItemVisualizer() {
        super();
    }

    /**
     * A base class for a control panel allowing selection of a single item.
     * 
     * @author Rudolf Mayer
     * @version $Id: AbstractItemVisualizer.java 3874 2010-11-02 14:14:38Z mayer $
     */
    protected class AbstractSelectedItemVisualizerControlPanel extends VisualizationControlPanel implements
            ActionListener, ListSelectionListener {
        private static final long serialVersionUID = 1L;

        protected static final int VISIBLE_ROWS = 7;

        /**
         * A list containing the names of all elements.
         */
        protected JList list = null;

        /**
         * A mapping from an element name to it's index in the list of items to be display, to provide faster access.
         */
        protected Hashtable<String, Integer> elementToIndexMap = new Hashtable<String, Integer>();

        /**
         * Copy of the previous search text, to prevent an update when the new search text is the same.
         */
        protected String oldText = null;

        /**
         * A text-field to directly search for an element.
         */
        protected JTextField text = null;

        public AbstractSelectedItemVisualizerControlPanel(String title) {
            super(title);
            addComponentListener(this);
            text = new JTextField();
            text.addActionListener(this);
            oldText = text.getText();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String newText = text.getText();
            if (!oldText.equals(newText)) {
                if (newText.equals("")) {
                    currentElement = 0;
                    list.setSelectedIndex(currentElement);
                    list.ensureIndexIsVisible(currentElement);
                } else {
                    boolean found = false;

                    // first try to find exact matches
                    for (int i = 0; i < list.getModel().getSize(); i++) {
                        if (list.getModel().getElementAt(i).equals(newText)) {
                            currentElement = elementToIndexMap.get(list.getModel().getElementAt(i)).intValue();
                            list.setSelectedIndex(currentElement);
                            list.ensureIndexIsVisible(currentElement);
                            text.setText((String) list.getSelectedValue());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // then try to find partly matches
                        final int index = list.getNextMatch(newText, 0, Position.Bias.Forward);
                        if (index != -1) {
                            list.setSelectedIndex(index);
                            text.setText((String) list.getSelectedValue());
                        }
                    }
                }
                oldText = newText;
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                JList l = (JList) e.getSource();
                currentElement = l.getSelectedIndex();
                l.ensureIndexIsVisible(currentElement);
                if (visualizationUpdateListener != null) {
                    visualizationUpdateListener.updateVisualization();
                }

            }
        }

        protected void initialiseList(String[] labels) {
            DefaultListModel listModel = new DefaultListModel();
            for (int i = 0; i < labels.length; i++) {
                listModel.addElement(labels[i]);
                elementToIndexMap.put(labels[i], i);
            }
            list = new JList(listModel);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setSelectedIndex(0);
            list.addListSelectionListener(this);
            list.setVisibleRowCount(Math.min(VISIBLE_ROWS, labels.length));
        }

        @Override
        public void componentResized(ComponentEvent e) {
            int componentHeight = getHeight();
            if (componentHeight > 0) {
                int visibleRowCount = Math.min(elementToIndexMap.size(), (componentHeight - 50) / 16);
                if (visibleRowCount != list.getVisibleRowCount()) {
                    list.setVisibleRowCount(visibleRowCount);
                    list.invalidate();
                }
            }
        }
    }
}