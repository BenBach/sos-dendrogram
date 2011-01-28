/*
 * Copyright 2004-2010 Institute of Software Technology and Interactive Systems, Vienna University of Technology
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
package at.tuwien.ifs.commons.util;

/**
 * A collection of math-related utility methods.
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: MathUtils.java 3800 2010-09-13 14:03:43Z frank $
 */
public class MathUtils {

    /** finds the minimum value from a given array of integer values */
    public static int min(int... arguments) {
        int min = Integer.MAX_VALUE;
        for (int argument : arguments) {
            if (argument < min) {
                min = argument;
            }
        }
        return min;
    }

    /** contrains a value within the given lower and upper boundaries */
    public static final int constrainWithin(int i, int lower, int upper) {
        return Math.max(lower, Math.min(i, upper));
    }

    /** contrains a value within the given lower and upper boundaries */
    public static int constrainWithin(int i, int lower, double upper) {
        return constrainWithin(i, lower, (int) upper);
    }

    /**
     * caps a value by the given maximum value.
     * 
     * @deprecated use {@link Math#min(int, int)} instead.
     */
    @Deprecated
    public static final int cap(int i, int cap) {
        return Math.min(i, cap);
    }

    /**
     * caps a value by the given minimum value.
     * 
     * @deprecated use {@link Math#max(int, int)} instead
     */
    @Deprecated
    public static final int capLower(int i, int lowerCap) {
        return Math.max(i, lowerCap);
    }

    /** caps a value by the given minimum value. */
    public static double capLower(double i, double lowerCap) {
        if (i < lowerCap) {
            return lowerCap;
        } else {
            return i;
        }
    }

    /**
     * caps a value by the given maximum value.
     * 
     * @deprecated use {@link Math#min(long, long)} instead.
     */
    @Deprecated
    public static final long cap(long i, long cap) {
        return Math.min(i, cap);
    }

    /**
     * sums up the values in the array and returns the sum
     * 
     * @param in the array over which the sum shall be calculated
     * @return the sum of all values in the array
     */
    public static double getSumOf(double[] in) {
        double out = 0;
        for (double element : in) {
            out += element;
        }
        return out;
    }

    /**
     * sums up the values in the array and returns the sum
     * 
     * @param in the array over which the sum shall be calculated
     * @return the sum of all values in the array
     */
    public static int getSumOf(int[] in) {
        int out = 0;
        for (int element : in) {
            out += element;
        }
        return out;
    }

    public static int numberOfDigits(int i) {
        return 1 + (int) Math.log10(Math.abs(i));
    }

    public static int numberOfDigits(long i) {
        return 1 + (int) Math.log10(Math.abs(i));
    }
}
