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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.apache.commons.collections.CollectionUtils;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.layers.Label;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * A control that allows comparing two or more selected areas in regard to the labels they contain.
 * 
 * @author Rudolf Mayer
 * @version $Id: ComparisonPanel.java 3873 2010-10-28 09:29:58Z frank $
 */
public class ComparisonPanel extends AbstractSelectionPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private static final String FILE_NAME = "FILE_NAME";

    private static final String SORT_BY_NAME = "SORT_BY_NAME";

    private static final String SORT_BY_VALUE = "SORT_BY_VALUE";

    private JButton btLabelSOM = null;

    private ButtonGroup buttonGroupType = new ButtonGroup();

    private JRadioButton radioButtonTypeDataItemName = new JRadioButton();

    private JRadioButton radioButtonTypeLabelSOM = new JRadioButton();

    private JRadioButton radioButtonTypeKeywordLabels = new JRadioButton();

    private ButtonGroup buttonGroupSortBy = new ButtonGroup();

    private JRadioButton radioButtonSortByName = new JRadioButton();

    private JRadioButton radioButtonSortByValue = new JRadioButton();

    // private JRadioButton radioButtonTypeKeywordsGateLabels = new JRadioButton();
    //
    // private JRadioButton radioButtonTypeContextLabels = new JRadioButton();

    int listCount;

    private JScrollPane unionPane;

    private JList unionList;

    private JPanel buttonsPanelSortBy;

    public ComparisonPanel(CommonSOMViewerStateData state) {
        this(state, 2);
    }

    public ComparisonPanel(CommonSOMViewerStateData state, int listCount) {
        super(new GridBagLayout(), state, "Comparison Control", listCount);
        this.listCount = listCount;
        this.initGUIElements();
        currentSelectionArea = -1;
    }

    protected void initGUIElements() {
        GridBagConstraints c = new GridBagConstraints();
        btLabelSOM = new JButton("LabelSOM");
        btLabelSOM.setActionCommand(Unit.LABELSOM);
        btLabelSOM.addActionListener(this);

        c.anchor = GridBagConstraints.NORTH;
        c.gridy = 0;

        JPanel buttonsPanelType = new JPanel(new FlowLayout(FlowLayout.LEFT));

        radioButtonTypeDataItemName = new JRadioButton("Name");
        radioButtonTypeDataItemName.setActionCommand(FILE_NAME);
        radioButtonTypeDataItemName.addActionListener(this);
        buttonGroupType.add(radioButtonTypeDataItemName);
        buttonsPanelType.add(radioButtonTypeDataItemName);

        c.gridx = GridBagConstraints.REMAINDER;

        radioButtonTypeLabelSOM = new JRadioButton("LabelSOM");
        radioButtonTypeLabelSOM.setActionCommand(Unit.LABELSOM);
        radioButtonTypeLabelSOM.addActionListener(this);
        buttonGroupType.add(radioButtonTypeLabelSOM);
        buttonsPanelType.add(radioButtonTypeLabelSOM);

        radioButtonTypeKeywordLabels = new JRadioButton("Keywords");
        radioButtonTypeKeywordLabels.setActionCommand(Unit.KEYWORDS);
        radioButtonTypeKeywordLabels.addActionListener(this);
        buttonGroupType.add(radioButtonTypeKeywordLabels);
        buttonsPanelType.add(radioButtonTypeKeywordLabels);

        // radioButtonTypeKeywordsGateLabels = new JRadioButton("Gate");
        // radioButtonTypeKeywordsGateLabels.setActionCommand(Unit.GATE);
        // radioButtonTypeKeywordsGateLabels.addActionListener(this);
        // buttonGroupType.add(radioButtonTypeKeywordsGateLabels);
        // buttons.add(radioButtonTypeKeywordsGateLabels);
        //
        // radioButtonTypeContextLabels = new JRadioButton("Context");
        // radioButtonTypeContextLabels.setActionCommand(Unit.CONTEXT);
        // radioButtonTypeContextLabels.addActionListener(this);
        // buttonGroupType.add(radioButtonTypeContextLabels);
        // buttons.add(radioButtonTypeContextLabels);

        radioButtonTypeDataItemName.setSelected(true);
        getContentPane().add(buttonsPanelType, c);
        c.gridy = c.gridy + 1;

        buttonsPanelSortBy = new JPanel();
        buttonsPanelSortBy.add(new JLabel("Sort by:"));

        radioButtonSortByName = new JRadioButton("Name");
        radioButtonSortByName.setActionCommand(SORT_BY_NAME);
        radioButtonSortByName.addActionListener(this);
        buttonGroupSortBy.add(radioButtonSortByName);
        buttonsPanelSortBy.add(radioButtonSortByName);

        radioButtonSortByValue = new JRadioButton("Value");
        radioButtonSortByValue.setActionCommand(SORT_BY_VALUE);
        radioButtonSortByValue.addActionListener(this);
        buttonGroupSortBy.add(radioButtonSortByValue);
        buttonsPanelSortBy.add(radioButtonSortByValue);

        radioButtonSortByName.setSelected(true);
        enableSortByButtons(false);
        getContentPane().add(buttonsPanelSortBy, c);
        c.gridy = c.gridy + 1;

        c.gridx = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.weighty = 1.0;
        JPanel listScrollerPanel = new JPanel(new GridLayout((int) Math.ceil(listScrollers.length / 2), 2));
        for (int i = 0; i < listScrollers.length; i++) {
            listScrollers[i] = new JScrollPane();
            listScrollers[i].setColumnHeaderView(new JLabel("Selction area " + (i + 1)));
            listScrollers[i].setViewportView(playlists[i]);
            playlists[i].setVisibleRowCount(10);
            listScrollerPanel.add(listScrollers[i], c);
            listScrollers[i].setPreferredSize(new Dimension(100, 220)); // FIXME: more dynamic calculation of size
        }
        getContentPane().add(listScrollerPanel, c);
        c.gridy = c.gridy + 1;

        unionList = new JList();
        unionList.setVisibleRowCount(5);
        unionPane = new JScrollPane(unionList);
        unionPane.setColumnHeaderView(new JLabel("Common Labels"));
        getContentPane().add(unionPane, c);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String type = buttonGroupType.getSelection().getActionCommand();
        if (type == FILE_NAME) {
            enableSortByButtons(false);
            showDataItemNames();
        } else if (type == Unit.LABELSOM || type == Unit.GATE || type == Unit.KEYWORDS || type == Unit.CONTEXT) {
            enableSortByButtons(true);
            showLabels(type, buttonGroupSortBy.getSelection().getActionCommand());
        }
    }

    @Override
    public void unitSelectionChanged(Object[] selection, boolean newSelection) {
        super.unitSelectionChanged(selection, newSelection);
        String type = buttonGroupType.getSelection().getActionCommand();
        if (type == Unit.LABELSOM || type == Unit.GATE || type == Unit.KEYWORDS || type == Unit.CONTEXT) {
            enableSortByButtons(true);
            showLabels(type, buttonGroupSortBy.getSelection().getActionCommand());
        }
    }

    private void enableSortByButtons(boolean enabled) {
        radioButtonSortByValue.setEnabled(enabled);
        radioButtonSortByName.setEnabled(enabled);
    }

    private void showDataItemNames() {
        for (int i = 0; i < listScrollers.length; i++) {
            listScrollers[i].setViewportView(playlists[i]);
        }
    }

    public void showLabels(String labelType, String sortBy) {
        int count = 2;
        ArrayList<HashSet<Label>> labelsSet = new ArrayList<HashSet<Label>>();
        for (int i = 0; i < selections.length; i++) {
            labelsSet.add(new HashSet<Label>());
            if (selections[i] != null) {
                for (int j = 0; j < selections[i].length; j++) {
                    if (selections[i][j].getLabels(labelType) != null) {
                        CollectionUtils.addAll(labelsSet.get(i), selections[i][j].getLabels(labelType));
                    }
                }
            }
        }

        ArrayList<Hashtable<String, Label>> labelsHash = new ArrayList<Hashtable<String, Label>>();
        for (int i = 0; i < labelsSet.size(); i++) {
            labelsHash.add(new Hashtable<String, Label>());
            for (Label label : labelsSet.get(i)) {
                labelsHash.get(i).put(label.getName(), label);
            }
        }

        Label[][] labelsArray = new Label[count][];
        for (int i = 0; i < labelsSet.size(); i++) {
            labelsArray[i] = labelsSet.get(i).toArray(new Label[labelsSet.get(i).size()]);
            if (sortBy == SORT_BY_VALUE) {
                Label.sortByValue(labelsArray[i], Label.SORT_DESC);
            } else if (sortBy == SORT_BY_NAME) {
                Label.sortByName(labelsArray[i], Label.SORT_ASC);
            }
        }

        TreeSet<String> unionSet = new TreeSet<String>();
        for (Label label : labelsSet.get(0)) {
            if (labelsHash.get(1).get(label.getName()) != null) {
                unionSet.add(label.getName());
            }
        }
        for (int i = 0; i < listScrollers.length; i++) {
            DefaultListModel listModel = new DefaultListModel();
            for (int j = 0; j < labelsArray[i].length; j++) {
                listModel.addElement(labelsArray[i][j].getNameAndScaledValue(5));
            }
            JList list = new JList(listModel);
            list.setVisibleRowCount(5);
            listScrollers[i].setViewportView(list);
        }

        DefaultListModel unionListModel = new DefaultListModel();
        for (String element : unionSet) {
            unionListModel.addElement(element);
        }
        unionList.setModel(unionListModel);
        unionPane.setViewportView(unionList);
    }

}
