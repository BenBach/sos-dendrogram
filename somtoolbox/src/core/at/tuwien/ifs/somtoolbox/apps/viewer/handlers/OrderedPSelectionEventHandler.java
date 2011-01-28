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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;

/**
 * A Selection Event Handler that stores the selected items in the selection (insertion) order.
 * 
 * @author Rudolf Mayer
 * @version $Id: OrderedPSelectionEventHandler.java 3877 2010-11-02 15:43:17Z frank $
 */
@SuppressWarnings("unchecked")
public class OrderedPSelectionEventHandler extends PSelectionEventHandler {

    protected Set<GeneralUnitPNode> currentSelection;

    public OrderedPSelectionEventHandler(PNode marqueeParent, PNode selectableParent) {
        super(marqueeParent, selectableParent);
    }

    @SuppressWarnings("rawtypes")
    public OrderedPSelectionEventHandler(PNode marqueeParent, List selectableParents) {
        super(marqueeParent, selectableParents);
    }

    @Override
    protected void init() {
        currentSelection = Collections.synchronizedSet(new LinkedHashSet<GeneralUnitPNode>());
        super.init();
    }

    /**
     * Returns a copy of the currently selected nodes. Overriding the super-class method to ensure an insertion-order of
     * the elements (the super class returns the keyset of a hashmap).
     */
    @Override
    public Collection<GeneralUnitPNode> getSelection() {
        if (currentSelection != null) {
            return this.currentSelection;
        } else {
            return super.getSelection();
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void select(Collection items) {
        currentSelection.addAll(items);
        super.select(items);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void unselect(Collection items) {
        super.unselect(items);
        currentSelection.removeAll(items);
    }

    @Override
    protected void startDrag(PInputEvent e) {
        if (!isOptionSelection(e)) {
            this.currentSelection = Collections.synchronizedSet(new LinkedHashSet<GeneralUnitPNode>());
        }
        super.startDrag(e);
    }

    /**
     * check if an object has already been selected or not
     * 
     * @param o - object to search for in the current selection
     * @return true if the object is already selected, false otherwise.
     */
    protected boolean alreadySelected(Object o) {
        return currentSelection.contains(o);
        // for (int i = 0; i < this.currentSelection.size(); i++) {
        // if (this.currentSelection.elementAt(i).equals(o)) {
        // return true;
        // }
        // }
        // return false;
    }

    @Override
    public void select(PNode node) {
        if (node instanceof GeneralUnitPNode) {
            currentSelection.add((GeneralUnitPNode) node);
        }
        super.select(node);
    }

    @Override
    public void unselect(PNode node) {
        super.unselect(node);
        currentSelection.remove(node);
    }

    @Override
    public void unselectAll() {
        super.unselectAll();
        currentSelection = Collections.synchronizedSet(new LinkedHashSet<GeneralUnitPNode>());
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void select(Map items) {
        for (Object object : items.keySet()) {
            if (object instanceof GeneralUnitPNode) {
                currentSelection.add((GeneralUnitPNode) object);
            }
        }
        super.select(items);
    }

}