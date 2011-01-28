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
package at.tuwien.ifs.somtoolbox.clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cluster<E> implements Iterable<E> {
    public static final String CONTENT_SEPARATOR_CHAR = "  |  ";

    protected List<E> data;

    protected String label;

    protected Cluster() {
        this.data = new ArrayList<E>();
    }

    public Cluster(E datum) {
        this(datum, null);
    }

    public Cluster(E datum, String label) {
        this.data = new ArrayList<E>();
        data.add(datum);
        this.label = label;
    }

    public Cluster(List<E> data) {
        this(data, null);
    }

    public Cluster(List<E> data, String label) {
        this.data = data;
        this.label = label;
    }

    public E get(int index) {
        return data.get(index);
    }

    public List<E> getData() {
        return data;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public Iterator<E> iterator() {
        return data.iterator();
    }

    public int size() {
        return data.size();
    }

    public String contentToString() {
        StringBuilder sb = new StringBuilder();
        for (E element : data) {
            if (sb.length() > 0) {
                sb.append(CONTENT_SEPARATOR_CHAR);
            }
            sb.append(element.toString());
        }
        return sb.toString();
    }

}
