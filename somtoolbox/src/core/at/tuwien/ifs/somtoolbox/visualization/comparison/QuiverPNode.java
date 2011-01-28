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
package at.tuwien.ifs.somtoolbox.visualization.comparison;

import java.util.ArrayList;
import java.util.logging.Logger;

import edu.umd.cs.piccolo.PNode;

import at.tuwien.ifs.somtoolbox.apps.viewer.ArrowPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;
import at.tuwien.ifs.somtoolbox.apps.viewer.UnitSelectionListener;

/**
 * @author Doris Baum
 * @version $Id: QuiverPNode.java 3883 2010-11-02 17:13:23Z frank $
 */
public class QuiverPNode extends PNode implements UnitSelectionListener {
    private static final long serialVersionUID = 1L;

    // private boolean arrowVisibility = false;
    private boolean outlierArrows = true;

    private boolean adjacentArrows = true;

    private boolean stableArrows = true;

    private boolean clusterArrows = false;

    private boolean cumulative = false;

    private Object[] unitSelection = new Object[0];

    private SOMPane sompane = null;

    public QuiverPNode(SOMPane sompane) {
        this.sompane = sompane;
        setPickable(false);
    }

    public void dropArrows() {
        this.removeAllChildren();
    }

    public void computeArrows() {
        // reset all the units on the source map to reference no arrows
        sompane.getMap().resetArrows();

        // have variables for easier reference
        MapPNode map = sompane.getMap();
        MapPNode map2 = sompane.getSecondMap();
        double secMapXOffset = sompane.getSecMapXOffset();
        double secMapYOffset = sompane.getSecMapYOffset();

        // start with a fresh and shiny PNode
        this.removeAllChildren();

        try {
            // (re)calculate the shifts between the two SOMs
            ArrayList<Shift> allShifts = null;

            // if we're using a cluster view of the arrows
            if (clusterArrows) {
                allShifts = sompane.getSOMComparision().calculateClusterShifts(map, map2);

                // if we're using the "normal" view
            } else {
                allShifts = sompane.getSOMComparision().calculateShifts(cumulative);
            }

            // make arrows for the data shifts
            ArrowPNode currentArrow = null;
            Shift currentShift = null;
            for (int i = 0; i < allShifts.size(); i++) {
                // shift info from ArrayList
                currentShift = allShifts.get(i);
                // Calculate coordinates for the arrow
                currentArrow = new ArrowPNode((currentShift.getX1() + 0.5) * map.getUnitWidth(),
                        (currentShift.getY1() + 0.5) * map.getUnitHeight(), secMapXOffset
                                + (currentShift.getX2() + 0.5) * map2.getUnitWidth(), secMapYOffset
                                + (currentShift.getY2() + 0.5) * map2.getUnitHeight());
                currentArrow.setPickable(false);
                // / set type (and thus color) of arrow
                currentArrow.setType(currentShift.getType());
                // set width of arrow
                if (sompane.getSOMComparision().isAbsolute() || clusterArrows) {
                    currentArrow.setProportionalWidth(currentShift.getProportion());
                } else {
                    currentArrow.setProportionalWidth(currentShift.getPercent());
                }
                this.addChild(currentArrow);
                // tell the arrow source unit that it is source of this new arrow
                map.getUnit(currentShift.getX1(), currentShift.getY1()).addArrow(currentArrow);
            }

            // determine which of the new arrows should be visible
            this.updateArrowTypeVisibility();
            this.updateArrowSelectionVisibility();

            if (sompane.isShiftArrowsVisibility()) {
                this.updateClusterBorders();
            }

        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateArrowTypeVisibility() {
        for (int i = 0; i < this.getChildrenCount(); i++) {
            ArrowPNode curArrow = (ArrowPNode) this.getChild(i);
            if (stableArrows && curArrow.getType() == ArrowPNode.STABLE || adjacentArrows
                    && curArrow.getType() == ArrowPNode.ADJACENT || outlierArrows
                    && curArrow.getType() == ArrowPNode.OUTLIER || clusterArrows
                    && curArrow.getType() == ArrowPNode.CLUSTER) {
                curArrow.setTypeVisibility(true);
            } else {
                curArrow.setTypeVisibility(false);
            }
        }
        this.repaint();
    }

    public void updateArrowSelectionVisibility() {
        // if there are units selected
        if (unitSelection.length > 0) {
            // ... first make all arrows invisible
            this.setAllArrowsSelectionVisibility(false);

            GeneralUnitPNode currentSelectedUnit = null;
            // go through all selected units
            for (Object element : unitSelection) {
                if (element instanceof GeneralUnitPNode) {
                    // get the current selected unit
                    currentSelectedUnit = (GeneralUnitPNode) element;
                    // go through the list of arrows on this node and make each one visible
                    for (ArrowPNode arrowPNode : currentSelectedUnit.getArrows()) {
                        arrowPNode.setSelectionVisibility(true);
                    }
                }
            }
        } else {
            // if nothing is selected, just make all arrows visible
            this.setAllArrowsSelectionVisibility(true);
        }
        this.repaint();
    }

    private void setAllArrowsSelectionVisibility(boolean vis) {
        for (int i = 0; i < this.getChildrenCount(); i++) {
            ((ArrowPNode) this.getChild(i)).setSelectionVisibility(vis);
        }
    }

    @Override
    public void unitSelectionChanged(Object[] selection, boolean newSelection) {
        unitSelection = selection;
        this.updateArrowSelectionVisibility();
    }

    public void updateClusterBorders() {
        MapPNode map = sompane.getMap();
        MapPNode map2 = sompane.getSecondMap();

        // angela:showClusters - true->false geändert, damit die linien nicht immer dünner werden
        // tritt auf wenn nicht jedes mal der baum neu berechnet wird
        boolean clusterremove = false;
        if (sompane.isShiftArrowsVisibility() && clusterArrows) {
            map.showClusters(sompane.getSOMComparision().getClusterNo(), false);
            if (map2 != null) {
                map2.showClusters(sompane.getSOMComparision().getClusterNo(), false);
            } else {
                clusterremove = true;
            }
        } else {
            clusterremove = true;
        }
        if (clusterremove) {
            map.setClusteringElements(null);
            if (map2 != null) {
                map2.setClusteringElements(null);
            }
        }

        map.repaint();
        if (map2 != null) {
            map2.repaint();
        }
    }

    public boolean clusterArrowsOn() {
        return clusterArrows;
    }

    public void enableClusterArrows(boolean clusterArrows) {
        if (this.clusterArrows != clusterArrows) {
            this.clusterArrows = clusterArrows;
            this.computeArrows();
        }
    }

    public void setMultiMatch(boolean multiMatch) {
        if (sompane.getSOMComparision().isMultiMatch() != multiMatch) {
            sompane.getSOMComparision().setMultiMatch(multiMatch);
            this.computeArrows();
        }
    }

    public boolean isCumulative() {
        return cumulative;
    }

    public void setCumulative(boolean cumulative) {
        if (this.cumulative != cumulative) {
            this.cumulative = cumulative;
            this.computeArrows();
        }
    }

    public boolean outlierArrowsOn() {
        return outlierArrows;
    }

    public void enableOutlierArrows(boolean outlierArrows) {
        this.outlierArrows = outlierArrows;
        this.updateArrowTypeVisibility();
    }

    public boolean stableArrowsOn() {
        return stableArrows;
    }

    public void enableStableArrows(boolean stableArrows) {
        this.stableArrows = stableArrows;
        this.updateArrowTypeVisibility();
    }

    public boolean adjacentArrowsOn() {
        return adjacentArrows;
    }

    public void enableAdjacentArrows(boolean adjacentArrows) {
        this.adjacentArrows = adjacentArrows;
        this.updateArrowTypeVisibility();
    }
}
