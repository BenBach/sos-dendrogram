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
package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Hashtable;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;

import at.tuwien.ifs.somtoolbox.apps.viewer.controls.LabelEditColorChooser;

/**
 * Event Listerner for double clicks on cluster- and manual labels. Contains also the dialoge for editing the label.
 * 
 * @author Angela Roiger
 * @version $Id: EditLabelEventListener.java 3877 2010-11-02 15:43:17Z frank $
 */
public class EditLabelEventListener extends PBasicInputEventHandler {

    PText text;

    JDialog editDialog = null;

    TextArea newText;

    JButton hideButton;

    @Override
    public void mouseClicked(PInputEvent event) {
        PNode n = event.getInputManager().getMouseOver().getPickedNode();
        if (event.getClickCount() == 2 && PText.class.isInstance(n)) {

            // System.out.println("click");

            if (editDialog != null) {
                editDialog.setVisible(false);
                editDialog = null;
            }

            text = (PText) n;

            editDialog = new JDialog();
            GridBagConstraints c = new GridBagConstraints();
            editDialog.setSize(300, 300);
            editDialog.setTitle("Edit Label");
            editDialog.getContentPane().setLayout(new GridLayout(2, 1));

            JPanel editText = new JPanel();
            editText.add(new JLabel("Edit Text"));
            // newText = new TextField(text.getText(),20);
            newText = new TextArea(text.getText(), 4, 20);
            newText.addTextListener(new TextListener() {
                @Override
                public void textValueChanged(TextEvent e) {
                    text.setText(newText.getText());
                    text.repaint();
                }
            });
            editText.add(newText);
            if (text.getVisible() == true) {
                hideButton = new JButton("hide label");
            } else {
                hideButton = new JButton("show label");
            }
            hideButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (text.getVisible() == true) {
                        text.setVisible(false);
                        hideButton.setText("show Label");
                    } else {
                        text.setVisible(true);
                        hideButton.setText("hide Label");
                    }
                }
            });
            editText.add(hideButton);

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final PNode parent = text.getParent();
                    parent.removeChild(text);
                    editDialog.setVisible(false);
                    text = null;
                }
            });
            editText.add(deleteButton);

            editDialog.getContentPane().add(editText, c);

            Container general = new Container();
            general.setLayout(new GridLayout(3, 1));

            JPanel size = new JPanel();
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(text.getFont().getSize(), 0, Integer.MAX_VALUE, 1));
            spinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int newSize = ((Integer) ((JSpinner) e.getSource()).getValue()).intValue();
                    try {
                        text.setFont(new Font(text.getFont().getFontName(), text.getFont().getStyle(), newSize));
                        text.repaint();
                    } catch (java.lang.Exception ex) {
                        JOptionPane.showMessageDialog(null, ex);
                    }
                }
            });
            size.add(new JLabel("Font Size:"));
            size.add(spinner);

            // change color...
            JButton colorButton = new JButton("Color");
            colorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    new LabelEditColorChooser(editDialog, text);
                }
            });

            size.add(colorButton);

            general.add(size, c);

            int start = new Double(text.getParent().getRotation() / (2 * Math.PI) * 360).intValue();
            JSlider rot = new JSlider(0, 360, start);
            Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
            labelTable.put(new Integer(0), new JLabel("0"));
            labelTable.put(new Integer(90), new JLabel("90"));
            labelTable.put(new Integer(180), new JLabel("180"));
            labelTable.put(new Integer(270), new JLabel("270"));
            labelTable.put(new Integer(360), new JLabel("360"));
            rot.setLabelTable(labelTable);
            rot.setPaintLabels(true);
            rot.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int degree = ((JSlider) e.getSource()).getValue();
                    double d = new Double(degree).doubleValue() / 360 * 2 * Math.PI;
                    text.getParent().setRotation(d);
                    text.repaint();
                }
            });

            JPanel rotate = new JPanel();
            rotate.add(new JLabel("Rotate by"));
            rotate.add(rot);
            general.add(rotate);

            JButton okButton = new JButton("OK");
            okButton.setMnemonic('o');
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    PNode parent = text.getParent();

                    double yOffset = 0d;
                    // move labels up/down if size of other labels changed:
                    for (ListIterator<?> l = parent.getChildrenIterator(); l.hasNext();) {
                        PText currentLabel = (PText) l.next();
                        if (new String("clusterLabel").equals(currentLabel.getAttribute("type"))) {
                            yOffset = yOffset + currentLabel.getYOffset() + currentLabel.getHeight();
                        } else if (new String("smallClusterLabel").equals(currentLabel.getAttribute("type"))) {
                            currentLabel.setOffset(currentLabel.getXOffset(), yOffset);
                            yOffset = yOffset + currentLabel.getHeight();
                        }

                        currentLabel.repaint();
                    }
                    editDialog.setVisible(false);
                    editDialog = null;
                    // editDialog.dispose();
                }
            });

            general.add(okButton);
            editDialog.getContentPane().add(general);

            editDialog.setModal(false);

            editDialog.setVisible(true);

        }
    }
}
