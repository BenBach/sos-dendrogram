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
package at.tuwien.ifs.somtoolbox.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * FIXME: this is a copy from valhalla, merge back into IFS-commons!
 * 
 * @author Rudolf Mayer
 * @version $Id: ElementCounter.java 3883 2010-11-02 17:13:23Z frank $
 * @param <T> the type of objects that this object may be compared to
 */
public class ElementCounter<T extends Comparable<T>> {
    private HashMap<T, Integer> map = new HashMap<T, Integer>();

    public void incCount(T key) {
        map.put(key, getCount(key) + 1);
    }

    public int getCount(T key) {
        if (map.get(key) == null) {
            return 0;
        } else {
            return map.get(key);
        }
    }

    public Set<T> keySet() {
        return map.keySet();
    }

    public ArrayList<T> keyList() {
        return new ArrayList<T>(map.keySet());
    }

    public ArrayList<T> keyList(int minCount) {
        ArrayList<T> result = new ArrayList<T>();
        for (Entry<T, Integer> entry : entrySet()) {
            if (entry.getValue() >= minCount) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Set<Entry<T, Integer>> entrySet() {
        return map.entrySet();
    }

    public Set<Entry<T, Integer>> entrySet(int minCount) {
        Set<Entry<T, Integer>> result = new HashSet<Entry<T, Integer>>();
        for (Entry<T, Integer> entry : entrySet()) {
            if (entry.getValue() >= minCount) {
                result.add(entry);
            }
        }
        return result;
    }

    public Collection<Integer> values() {
        return map.values();
    }

    public int totalCount() {
        int totalCount = 0;
        for (Integer count : map.values()) {
            totalCount += count;
        }
        return totalCount;
    }

    public int size() {
        return map.size();
    }

    @Override
    public String toString() {
        return toString(10);
    }

    public String toString(int width) {
        long sum = 0;
        StringBuilder sb = new StringBuilder();
        ArrayList<T> keyList = keyList();
        Collections.sort(keyList);
        int rows = keyList.size() / width;
        if (keyList.size() % width > 0) {
            rows++;
        }
        for (int i = 0; i < rows; i++) {
            for (int j = i * width; j < (i + 1) * width && j < keyList.size(); j++) {
                sb.append(keyList.get(j) + "\t");
            }
            sb.append("\n");
            for (int j = i * width; j < (i + 1) * width && j < keyList.size(); j++) {
                T key = keyList.get(j);
                Integer value = map.get(key);
                sb.append(value + "\t");
                sum += value;
            }
            sb.append("\n\n");
        }
        sb.append("Total: ").append(sum).append("\n\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        ElementCounter<Integer> counter = new ElementCounter<Integer>();
        int n = 100;
        for (int i = 0; i < n * 100; i++) {
            int x = (int) (Math.random() * 100) + 1;
            counter.incCount(x);
        }
        System.out.println(counter.toString(13));
    }

}
