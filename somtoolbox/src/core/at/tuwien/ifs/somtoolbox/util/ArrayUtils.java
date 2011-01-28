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

import java.util.Arrays;

/**
 * Class gathering utilities related to Arrays.
 * 
 * @author Rudolf Mayer
 * @version $Id: ArrayUtils.java 3651 2010-07-13 09:58:16Z mayer $
 */
public class ArrayUtils {
    /**
     * Initialises an array of the given size, with the value at each index corresponding to the index, i.e. 0, 1, 2,
     * ....
     */
    public static int[] getLinearArray(int dim) {
        int[] columnOrder = new int[dim];
        for (int i = 0; i < columnOrder.length; i++) {
            columnOrder[i] = i;
        }
        return columnOrder;
    }

    /**
     * Gets a string representation just as {@link Arrays#toString(int[])}, but at most until the given max amount of
     * values.
     */
    public static String toString(int[] a, int maxValues) {
        if (a == null || a.length < maxValues) {
            return Arrays.toString(a);
        }
        StringBuilder b = new StringBuilder(maxValues * 7);
        b.append("[");
        for (int i = 0; i < maxValues; i++) {
            b.append(a[i]);
            b.append(", ");
        }
        return b.append(" ...]").toString();
    }

    /** A vararg wrapper around {@link Arrays#toString(int[])} */
    public static String toString(int... a) {
        return Arrays.toString(a);
    }

    /** Counts the number of occurrences of the given string in the given array */
    public static int countOccurrences(String s, String[] array) {
        int num = 0;
        for (String element : array) {
            if (s.equals(element)) {
                num++;
            }
        }
        return num;
    }

    public static int getBinIndex(double value, double[] bins) {
        if (value <= bins[0]) {
            return 0;
        }
        for (int i = 1; i < bins.length; i++) {
            if (value > bins[i - 1] && value < bins[i]) {
                return i - 1;
            }
        }
        return bins.length - 1;
    }

    /** create a double[] which contains percentage values, i.e. the values from 0.01 to 1.0 with a step-size of 0.01 */
    public static double[] getLinearPercentageArray() {
        double[] p = new double[100];
        for (int i = 0; i < p.length; i++) {
            p[i] = (double) (i + 1) / 100;
        }
        return p;
    }

    /** Converts a double[] to a Double[] (auto-boxing doesn't work for arrays...) */
    public static Double[] doubleToDoubleArray(double... a) {
        Double[] aDouble = new Double[a.length];
        for (int i = 0; i < a.length; i++) {
            aDouble[i] = a[i];
        }
        return aDouble;
    }
}
