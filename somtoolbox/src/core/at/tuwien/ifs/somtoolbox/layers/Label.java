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
package at.tuwien.ifs.somtoolbox.layers;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A simple class representing a unit label. This class holds the name, the value and the quantization error of a label.
 * It also provides static methods for sorting an array of labels according to on of the three properties mentioned
 * before.
 * 
 * @author Michael Dittenbach
 * @version $Id: Label.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class Label implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int SORT_ASC = 0;

    private static final int SORT_BYMEAN = 1;

    private static final int SORT_BYNAME = 2;

    private static final int SORT_BYQE = 0;

    public static final int SORT_DESC = 1;

    /**
     * Internal method for the quick sort algorithm.
     * 
     * @param a an array of labels to be sorted.
     * @param type the sorting criterion ({@link #SORT_BYMEAN}, {@link #SORT_BYNAME} or {@link #SORT_BYQE}).
     * @param order the order of sorting, either ascending ({@link #SORT_ASC}) or descending ({@link #SORT_DESC}).
     */
    private static void qsort(Label a[], int type, int order) { // type=0 -> qe, type=1 -> value, type=2 -> name
        qsort(a, 0, a.length - 1, type);
        if (order == SORT_DESC) {
            reverse(a);
        }
    }

    /**
     * Internal method for the quick sort algorithm.
     * 
     * @param a an array of labels to be sorted.
     * @param lo0 the lower boundary.
     * @param hi0 the upper boundary.
     * @param type the sorting criterion ({@link #SORT_BYMEAN}, {@link #SORT_BYNAME} or {@link #SORT_BYQE}).
     */
    private static void qsort(Label a[], int lo0, int hi0, int type) {
        int lo = lo0;
        int hi = hi0;
        Label mid;
        if (hi0 > lo0) {
            mid = a[(lo0 + hi0) / 2];
            while (lo <= hi) {
                if (type == 0) {
                    while (lo < hi0 && a[lo].getQe() < mid.getQe()) {
                        ++lo;
                    }
                    while (hi > lo0 && a[hi].getQe() > mid.getQe()) {
                        --hi;
                    }
                } else if (type == 1) {
                    while (lo < hi0 && a[lo].getValue() < mid.getValue()) {
                        ++lo;
                    }
                    while (hi > lo0 && a[hi].getValue() > mid.getValue()) {
                        --hi;
                    }
                } else if (type == 2) {
                    while (lo < hi0 && a[lo].getName().compareTo(mid.getName()) < 0) {
                        ++lo;
                    }
                    while (hi > lo0 && a[hi].getName().compareTo(mid.getName()) > 0) {
                        --hi;
                    }
                }
                if (lo <= hi) {
                    swap(a, lo, hi);
                    ++lo;
                    --hi;
                }
            }
            if (lo0 < hi) {
                qsort(a, lo0, hi, type);
            }
            if (lo < hi0) {
                qsort(a, lo, hi0, type);
            }

        }
    }

    /**
     * Convenience method for the reversal of an array of labels.
     * 
     * @param a the array of labels to be reversed.
     */
    private static void reverse(Label a[]) {
        reverse(a, 0, a.length - 1);
    }

    /**
     * Internal method for reversal of a part of an array of labels.
     * 
     * @param a the array of labels to be reversed.
     * @param start the lower boundary.
     * @param end the upper boundary.
     */
    private static void reverse(Label a[], int start, int end) {
        if (end - start > 1) {
            Label t = null;
            for (int r = start; r < (end - start) / 2; r++) {
                t = a[end - start - r];
                a[end - start - r] = a[r];
                a[r] = t;
            }
        }

    }

    /**
     * Convenience method for sorting an array of labels by their values.
     * 
     * @param labels an array of labels to be sorted.
     * @param order the sorting order, either ascending ({@link #SORT_ASC}) or descending ({@link #SORT_DESC}).
     */
    public static void sortByValue(Label[] labels, int order) {
        qsort(labels, SORT_BYMEAN, order);
    }

    /**
     * Convenience method for sorting an array of labels by their values and quantization errors (in case of equal
     * value). The sorting order is specified separately for both criteria by arguments <code>order1</code> and
     * <code>order2</code>.
     * 
     * @param labels an array of labels to be sorted.
     * @param order1 the sorting order for the primary criterion, either ascending ({@link #SORT_ASC}) or descending (
     *            {@link #SORT_DESC}).
     * @param order2 the sorting order for the secondary criterion, either ascending ({@link #SORT_ASC}) or descending (
     *            {@link #SORT_DESC}).
     */
    public static void sortByValueQe(Label[] labels, int order1, int order2) {
        sortByValue(labels, order1);
        int i1 = 0;
        int i2 = 0;
        while (i1 < labels.length) {
            double val = labels[i1].getValue();
            while (i2 < labels.length && labels[i2].getValue() == val) {
                i2++;
            }
            qsort(labels, i1, (i2 - 1), SORT_BYQE);
            if (order2 == SORT_DESC) {
                reverse(labels, i1, (i2 - 1));
            }
            i1 = i2;
        }
    }

    /**
     * Convenience method for sorting an array of labels by their names.
     * 
     * @param labels an array of labels to be sorted.
     * @param order the sorting order, either ascending ({@link #SORT_ASC}) or descending ({@link #SORT_DESC}).
     */
    public static void sortByName(Label[] labels, int order) {
        qsort(labels, SORT_BYNAME, order);
    }

    /**
     * Convenience method for sorting an array of labels by their quantization errors.
     * 
     * @param labels an array of labels to be sorted.
     * @param order the sorting order, either ascending ({@link #SORT_ASC}) or descending ({@link #SORT_DESC}).
     */
    public static void sortByQe(Label[] labels, int order) {
        qsort(labels, SORT_BYQE, order);
    }

    /**
     * Convenience method for sorting an array of labels by their quantization errors and values (in case of equal
     * quantization errors). The sorting order is determined separately for both criteria by arguments
     * <code>order1</code> and <code>order2</code>.
     * 
     * @param labels an array of labels to be sorted.
     * @param order1 the sorting order for the primary criterion, either ascending ({@link #SORT_ASC}) or descending (
     *            {@link #SORT_DESC}).
     * @param order2 the sorting order for the secondary criterion, either ascending ({@link #SORT_ASC}) or descending (
     *            {@link #SORT_DESC}).
     */
    public static void sortByQeValue(Label[] labels, int order1, int order2) {
        sortByQe(labels, order1);
        int i1 = 0;
        int i2 = 0;
        while (i1 < labels.length) {
            double val = labels[i1].getQe();
            while (i2 < labels.length && labels[i2].getQe() == val) {
                i2++;
            }
            qsort(labels, i1, (i2 - 1), SORT_BYMEAN);
            if (order2 == SORT_DESC) {
                reverse(labels, i1, (i2 - 1));
            }
            i1 = i2;
        }
    }

    /**
     * Internal method for the quick sort algorithm for swapping two elements (labels) of an array.
     * 
     * @param a an array of labels.
     * @param i index of first element to be swapped with second.
     * @param j index of second element to be swapped with first.
     */
    private static void swap(Label a[], int i, int j) {
        Label T;
        T = a[i];
        a[i] = a[j];
        a[j] = T;
    }

    private String name;

    private double qe;

    private double value;

    private String bestContext;

    /**
     * Constructs a label object with the given arguments.
     * 
     * @param name the name of the label.
     * @param value the label value.
     * @param qe the quantization error of the label.
     */
    public Label(String name, double value, double qe) {
        this.name = name;
        this.value = value;
        this.qe = qe;
    }

    public Label(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public Label(String name) {
        this.name = name;
    }

    public Label(String name, String BestContext) {
        this.name = name;
        this.bestContext = BestContext;
    }

    /**
     * Returns the name of the label.
     * 
     * @return the name of the label.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the quantization error of this label.
     * 
     * @return the quantization error of this label.
     */
    public double getQe() {
        return qe;
    }

    public String getBestContext() {
        return bestContext;
    }

    /**
     * Returns the value of this label.
     * 
     * @return the value of this label.
     */
    public double getValue() {
        return value;
    }

    public String getNameAndScaledValue(int scale) {
        double formattedValue = new BigDecimal(getValue()).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        return getName() + "[" + formattedValue + "]";
    }

}
