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

import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.ItemSelectionListener;
import at.tuwien.ifs.somtoolbox.apps.viewer.UnitSelectionListener;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Rudolf Mayer
 * @version $Id: AbstractSelectionPanel.java 3873 2010-10-28 09:29:58Z frank $
 */
public abstract class AbstractSelectionPanel extends AbstractViewerControl implements UnitSelectionListener,
        ListSelectionListener {
    private static final long serialVersionUID = 1L;

    protected ItemSelectionListener itemListener = null;

    public ItemSelectionListener getItemListener() {
        return itemListener;
    }

    public void setItemListener(ItemSelectionListener itemListener) {
        this.itemListener = itemListener;
    }

    protected DefaultListModel[] playlistmodels;

    protected JList[] playlists;

    protected Vector<String>[] absPathVectors;

    /**
     * units graphically marked on the map.
     */
    protected Vector<GeneralUnitPNode> markedUnits = new Vector<GeneralUnitPNode>();

    /**
     * units that contribute to the current playlist.
     */
    protected Vector<Unit> unitsInPlaylist = new Vector<Unit>();

    protected JScrollPane[] listScrollers;

    protected int currentSelectionArea = 0;

    private int selectionAreaCount;

    protected GeneralUnitPNode[][] selections;

    public AbstractSelectionPanel(LayoutManager layout, CommonSOMViewerStateData state, String title) {
        this(layout, state, title, 1);
    }

    @SuppressWarnings("unchecked")
    public AbstractSelectionPanel(LayoutManager layout, CommonSOMViewerStateData state, String title,
            int selectionAreaCount) {
        super(title, state, layout);
        this.selectionAreaCount = selectionAreaCount;
        playlistmodels = new DefaultListModel[selectionAreaCount];
        playlists = new JList[selectionAreaCount];
        absPathVectors = new Vector[selectionAreaCount];
        selections = new GeneralUnitPNode[selectionAreaCount][];
        listScrollers = new JScrollPane[selectionAreaCount];
        for (int i = 0; i < selectionAreaCount; i++) {
            playlistmodels[i] = new DefaultListModel();
            playlists[i] = new JList(playlistmodels[i]);
            playlists[i].setVisibleRowCount(5);
            absPathVectors[i] = new Vector<String>();
            playlists[i].getSelectionModel().addListSelectionListener(this);
        }
    }

    /**
     * get a String[] containing all objects (absolute paths) in current selection panel
     * 
     * @return an array of selected objects
     */
    protected Object[] getSelectedSongs() {
        return getSelectedSongs(currentSelectionArea);
    }

    /**
     * get a String[] containing all objects (absolute paths) in the given selection panel
     * 
     * @return an array of selected objects
     */
    protected Object[] getSelectedSongs(int selectionArea) {
        int[] selection = this.playlists[selectionArea].getSelectedIndices();
        Object[] oar = new String[selection.length];
        for (int i = 0; i < selection.length; i++) {
            oar[i] = this.absPathVectors[selectionArea].toArray()[selection[i]];
        }
        return oar;
    }

    /**
     * Clears the current playlist and the absPathVectors.<br>
     * If there are units currently marked on the map, the mark is removed.
     */
    public void clearList() {
        playlistmodels[currentSelectionArea].removeAllElements();
        absPathVectors[currentSelectionArea].removeAllElements();
        unitsInPlaylist.clear();
        unmarkUnits();
    }

    /**
     * adds an element to the current playlist and the absPathVectors
     */
    public void addToList(String elementName, Unit u) {
        addToList(elementName, CommonSOMViewerStateData.fileNamePrefix, u);
    }

    /**
     * adds an element to the current playlist and the absPathVectors
     * 
     * @param fileNamePrefix an alternative fileNamePrefix
     */
    public void addToList(String elementName, String fileNamePrefix, Unit u) {
        if (!unitsInPlaylist.contains(u)) {
            System.out.print(".");
            unitsInPlaylist.add(u);
        }
        playlistmodels[currentSelectionArea].addElement(elementName);
        absPathVectors[currentSelectionArea].add(fileNamePrefix + elementName + CommonSOMViewerStateData.fileNameSuffix);
    }

    /**
     * graphically marks a unit on the map, and preserves the list of marked units for later clearance
     */
    protected void markUnit(int x, int y) {
        GeneralUnitPNode unit = state.mapPNode.getUnit(x, y);
        if (!markedUnits.contains(unit)) {
            unit.setQueryHit();
            markedUnits.add(unit);
        }
    }

    /**
     * Unmarks all currently marked units on the map.<br>
     * Which units are marked is determined by the vector <code>markedUnits</code>
     */
    protected void unmarkUnits() {
        if (!markedUnits.isEmpty()) {
            for (GeneralUnitPNode unit : markedUnits) {
                unit.removeQueryHit();
            }
            markedUnits.clear();
        }
    }

    /**
     * called whenever the selection has changed, the new selection is added to the JList and to the list of the file's
     * absolute pathnames
     */
    @Override
    public void unitSelectionChanged(Object[] selection, boolean newSelection) {
        // System.out.printf("Abstr.SelPane: unitSelChanged(%d, %b)%n", selection.length, newSelection);
        if (selectionAreaCount > 1 && newSelection) {
            currentSelectionArea += 1;
            if (currentSelectionArea >= selectionAreaCount || currentSelectionArea < 0) {
                currentSelectionArea = 0;
            }
        }
        if (currentSelectionArea >= 0) {
            // remove all previously selected files
            clearList();
        }
        if (selection.length <= 0) {
            return;
        }

        // Angela: make new Array with only GeneralUnitPNodes inside to avoid exceptions when (cluster)labels are shown.
        ArrayList<Object> tmpSelection = new ArrayList<Object>();
        for (Object element : selection) {
            if (element instanceof GeneralUnitPNode) {
                tmpSelection.add(element);
            }
        }
        selection = tmpSelection.toArray();

        selections[currentSelectionArea] = new GeneralUnitPNode[selection.length];
        Vector<Unit> list2 = new Vector<Unit>();
        for (int u = 0; u < selection.length; u++) {
            GeneralUnitPNode gupn = (GeneralUnitPNode) selection[u];
            selections[currentSelectionArea][u] = gupn;
            String[] names = gupn.getMappedDataNames();
            // SOMLibDataInformation dataInfo = ((GeneralUnitPNode) selection[u]).getDataInfo();
            // TODO: for some reason the ordering of the units gets mixed up. in PlaySOMPlayer it works with this
            // approach...
            list2.add(gupn.getUnit());
            if (!unitsInPlaylist.contains(gupn.getUnit())) {
                unitsInPlaylist.add(gupn.getUnit());
            }

            if (names == null) {
                continue;
            }
            for (String element : names) {
                Object newElement = null;
                // TODO: ask Robert if UTF-8 decoding is necessary here
                // Cristoph Becker 20061129: Robert says it does no harm - and I need it.
                // So I am activating it again ;-)
                try {
                    // if (dataInfo != null) {
                    // newElement = URLDecoder.decode(dataInfo.getDataDisplayName(names[i]), "UTF-8");
                    // } else {
                    newElement = URLDecoder.decode(element, "UTF-8");
                    // }
                    absPathVectors[currentSelectionArea].add(CommonSOMViewerStateData.fileNamePrefix
                            + URLDecoder.decode(element, "UTF-8") + CommonSOMViewerStateData.fileNameSuffix);
                } catch (UnsupportedEncodingException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                            "Failed to decode string ... adding encoded one");
                    absPathVectors[currentSelectionArea].add(CommonSOMViewerStateData.fileNamePrefix + element
                            + CommonSOMViewerStateData.fileNameSuffix);
                    newElement = element;
                }
                if (!playlistmodels[currentSelectionArea].contains(newElement)) {
                    playlistmodels[currentSelectionArea].addElement(newElement);
                }
            }

        }
        unitsInPlaylist.retainAll(list2);

    }

    protected void addListScrollPanels(GridBagConstraints c) {
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.weighty = 1.0;
        for (int i = 0; i < selectionAreaCount; i++) {
            listScrollers[i] = new JScrollPane();
            listScrollers[i].setViewportView(playlists[i]);
            getContentPane().add(listScrollers[i], c);
        }
    }

    protected void addSingleListScrollPanel(Object constraints) {
        listScrollers[0] = new JScrollPane();
        listScrollers[0].setViewportView(playlists[0]);
        getContentPane().add(listScrollers[0], constraints);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (itemListener != null) {
            itemListener.itemSelected(playlists[0].getSelectedValues());
        }
    }

}
