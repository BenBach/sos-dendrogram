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
package at.tuwien.ifs.commons.models;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * @author Jakob Frank
 * @version $Id: ClassComboBoxModel.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class ClassComboBoxModel<A> extends AbstractListModel implements ComboBoxModel, Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Class<? extends A>> content;

    private int selectedIndex = -1;

    public ClassComboBoxModel(List<Class<? extends A>> content) {
        this.content = content;
    }

    public ClassComboBoxModel(Class<? extends A>[] content) {
        this.content = Arrays.asList(content);
    }

    @Override
    public String getSelectedItem() {
        if (selectedIndex < 0 || selectedIndex >= content.size()) {
            return null;
        } else {
            return content.get(selectedIndex).getSimpleName();
        }
    }

    public Class<? extends A> getSelectedClass() {
        if (selectedIndex < 0 || selectedIndex >= content.size()) {
            return null;
        }
        return content.get(selectedIndex);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedIndex = -1;
        for (int i = 0; i < content.size(); i++) {
            if (content.get(i).getSimpleName().equals(anItem.toString())) {
                selectedIndex = i;
                break;
            }
        }

        fireContentsChanged(this, selectedIndex, selectedIndex);
    }

    @Override
    public Object getElementAt(int index) {
        return content.get(index).getSimpleName();
    }

    public Class<? extends A> getClassAt(int index) {
        return content.get(index);
    }

    @Override
    public int getSize() {
        return content.size();
    }
}
