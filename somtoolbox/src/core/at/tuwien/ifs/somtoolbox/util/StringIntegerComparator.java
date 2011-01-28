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

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

/**
 * Comparator for two Strings, with special Integer comparison if these Strings are actually Integer values.
 * 
 * @author Rudolf Mayer
 * @version $Id: StringIntegerComparator.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class StringIntegerComparator implements Comparator<String> {
    boolean allIntegers = false;

    boolean checkedValues = false;

    public StringIntegerComparator() {
        allIntegers = false;
        checkedValues = false;
    }

    public StringIntegerComparator(String[] classNames) {
        allIntegers = true;
        for (String string : classNames) {
            if (!StringUtils.isNumeric(string.trim())) {
                allIntegers = false;
                break;
            }
        }
        checkedValues = true;
    }

    @Override
    public int compare(String o1, String o2) {
        if (checkedValues && allIntegers || // we checked before that we have only ints
                !checkedValues && StringUtils.isNumeric(o1.trim()) && StringUtils.isNumeric(o2.trim())) { // no
            // checking,
            // but both
            // in
            return Integer.valueOf(o1.trim()).compareTo(Integer.valueOf(o2.trim())); // => do integer comparison
        } else { // otherwise => string comparison
            return o1.compareTo(o2);
        }
    }
}
