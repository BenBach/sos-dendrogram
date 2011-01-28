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
package at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class QMConfigurationProfile {

    private ArrayList[] QMCriteriaContainer;

    public int SIZE;

    public QMConfigurationProfile(int size) {
        this.QMCriteriaContainer = new ArrayList[size];
        Arrays.fill(QMCriteriaContainer, null);
        this.SIZE = size;
    }

    public void createNewElement(int index) {
        this.QMCriteriaContainer[index] = new ArrayList();
    }

    public void insert(int index, Object o) {
        this.QMCriteriaContainer[index].add(o);
    }

    public void insert(int index, int pos, Object o) {
        this.QMCriteriaContainer[index].add(pos, o);
    }

    public boolean isEmpty() {
        boolean empty = true;
        for (ArrayList element : this.QMCriteriaContainer) {
            if (element != null) {
                empty = false;
            }
        }
        return empty;
    }

    public boolean isNullatPos(int index) {
        if (this.QMCriteriaContainer[index] == null) {
            return true;
        } else {
            return false;
        }
    }

    public int lengthOfElement(int index) {
        return this.QMCriteriaContainer[index].size();
    }

    public Object getElement(int index, int pos) {
        return this.QMCriteriaContainer[index].get(pos);
    }
}
