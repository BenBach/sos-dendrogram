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

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

/**
 * This class provides utility methods for String manipulation.
 * 
 * @author Rudolf Mayer
 * @version $Id: StringUtils.java 3888 2010-11-02 17:42:53Z frank $
 */
public class StringUtils {
    public static final String REGEX_SPACE_OR_TAB = "[ \t]+";

    private static final String DEFAULT_ARRAY_SEPARATOR = ", ";

    private static final String DEFAULT_ELIPSIS = "...";

    private static HashMap<String, DecimalFormat> decimalFormats = new HashMap<String, DecimalFormat>();

    /** Returns a {@link DecimalFormat} with the given number of fractionDigits, with or without trailing zeros. */
    public static DecimalFormat getDecimalFormat(int fractionDigits, boolean withZeros, int leadingDigits) {
        String pattern = repeatString(leadingDigits, "0") + "." + repeatString(fractionDigits, withZeros ? "0" : "#");
        if (!decimalFormats.containsKey(pattern)) {
            // when initializing number formats, using US locale avoids format troubles on localized OS's (, instead of
            // .)
            DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);
            format.applyPattern(pattern);
            decimalFormats.put(pattern, format);
        }
        return decimalFormats.get(pattern);
    }

    /**
     * Returns a {@link DecimalFormat} intended to formatting integers with the given number of digits, potentially with
     * leading zeros
     */
    public static DecimalFormat getIntegerFormat(int digits) {
        DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);
        format.applyPattern(repeatString(digits, "0"));
        return format;
    }

    /**
     * Formats a double value with the given number of fractionDigits. uses {@link #format(double, int, boolean)} with
     * no trailing zeros.
     */
    public static String format(double number, int fractionDigits) {
        return format(number, fractionDigits, false);
    }

    public static String formatAsPercent(double value, double maxValue, int fractionDigits) {
        return format(value * 100d / maxValue, fractionDigits, false) + "%";
    }

    /**
     * Formats a double value with the given number of fractionDigits.
     * 
     * @param withZeros indicates whether there should be trailing zeros to fill up all fraction digits
     */
    public static String format(double number, int fractionDigits, boolean withZeros) {
        return format(number, fractionDigits, withZeros, 1);
    }

    public static String format(double number, int fractionDigits, boolean withZeros, int leadingDigits) {
        return getDecimalFormat(fractionDigits, withZeros, leadingDigits).format(number);
    }

    /** Formats a double value with the given number of digits, potentially including leading zeros. */
    public static String format(int number, int digits) {
        return getIntegerFormat(digits).format(number);
    }

    /**
     * Returns the string until (excluding) the first dot (.)
     * 
     * @return filename without suffices
     */
    public static String stripSuffix(String sMitSuffix) {
        if (sMitSuffix.contains(File.separator)) {
            sMitSuffix = sMitSuffix.substring(sMitSuffix.lastIndexOf(File.separator) + 1);
        }
        int pos = sMitSuffix.indexOf(".");
        return sMitSuffix.substring(0, pos);
    }

    public static String stripQuotes(String s) {
        if (s.startsWith("'") && s.endsWith("'")) {
            s = s.substring(1, s.length() - 1);
        } else if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * Makes sure that the given String ends with the OS-correct File.separator ('/' on Unix, '\\' on Windows)
     */
    public static String makeStringEndWithCorrectFileSeparator(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            // Cut last char and OS-append correct separator
            return path.substring(0, path.length() - 1) + File.separator;
        } else {
            return path + File.separator;
        }
    }

    /**
     * Formats byte size in nice format
     * 
     * @param byteSize the size in bytes to format
     * @return Formatted number of bytes (eg: empty, 15 B, 12kB, 821 kB, 3 MB...)
     */
    public static String readableBytes(long byteSize) {
        if (byteSize < 0l) {
            return "invalid";
        }
        if (byteSize < 1l) {
            return "empty";
        }

        float byteSizeF = new java.lang.Float(byteSize).floatValue();
        String unit = "bytes";
        float factor = 1f;
        String[] desc = { "B", "kB", "MB", "GB", "TB" };

        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat();
        decimalFormat.setMaximumFractionDigits(1);
        decimalFormat.setGroupingUsed(true);

        String value = decimalFormat.format(byteSizeF);

        int i = 0;
        while (i + 1 < desc.length && (value.length() > 4 || value.length() > 3 && value.indexOf('.') < 0)) {
            i++;
            factor = factor * 1024l;
            value = decimalFormat.format(byteSizeF / factor);
        }
        if (value.charAt(0) == '0' && i > 0) { // go one back if a too-big scale is used
            value = decimalFormat.format(java.lang.Math.round(1024 * byteSizeF / factor));
            i--;
        }

        if (value.length() > 3 && value.indexOf('.') > 0) {
            value = value.substring(0, value.indexOf('.'));
        }

        unit = desc[i];
        return value + " " + unit;
    }

    public static String escapeString(String s) {
        s = s.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("-", "_").replaceAll("\\(", "_").replaceAll("\\)",
                "_");
        while (s.indexOf("__") != -1) {
            s = s.replaceAll("__", "_");
        }
        return s;
    }

    public static String toString(Collection<?> collection) {
        return toString(collection, "{", "}");
    }

    public static String toString(Collection<?> collection, String start, String end) {
        return toString(collection, start, end, DEFAULT_ARRAY_SEPARATOR);
    }

    public static String toString(Collection<?> collection, String start, String end, String separator) {
        if (collection == null) {
            return start + "null" + end;
        }

        StringBuilder b = new StringBuilder();
        b.append(start);
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext();) {
            b.append(iterator.next());
            if (iterator.hasNext()) {
                b.append(separator);
            }
        }
        b.append(end);
        return b.toString();
    }

    public static String toString(Object[] array, String start, String end) {
        return toString(array, start, end, DEFAULT_ARRAY_SEPARATOR);
    }

    public static String toString(Object[] array, String start, String end, String separator) {
        if (array == null) {
            return start + "null" + end;
        }

        StringBuilder b = new StringBuilder();
        b.append(start);
        for (int i = 0; i < array.length; i++) {
            b.append(String.valueOf(array[i]));
            if (i + 1 < array.length) {
                b.append(separator);
            }
        }
        b.append(end);
        return b.toString();
    }

    public static String toString(Object[] array, String start, String end, String separator, String encasing) {
        if (array == null) {
            return start + "null" + end;
        }

        StringBuilder b = new StringBuilder();
        b.append(start);
        for (int i = 0; i < array.length; i++) {
            b.append(encasing).append(String.valueOf(array[i])).append(encasing);
            if (i + 1 < array.length) {
                b.append(separator);
            }
        }
        b.append(end);
        return b.toString();
    }

    public static String toString(Object[] array, int maxElements) {
        return toString(array, "[", "]", DEFAULT_ARRAY_SEPARATOR, maxElements);
    }

    public static String toString(Object[] array, String start, String end, String separator, int maxElements) {
        if (array == null) {
            return start + "null" + end;
        }

        StringBuilder b = new StringBuilder();
        b.append(start);
        for (int i = 0; i < array.length && i < maxElements; i++) {
            b.append(String.valueOf(array[i]));
            if (i + 1 < array.length) {
                b.append(separator);
            }
            if (i + 1 == maxElements && array.length > maxElements) {
                b.append(DEFAULT_ELIPSIS);
            }
        }
        b.append(end);
        return b.toString();
    }

    public static String toString(int[] array, String start, String end) {
        if (array == null) {
            return start + "null" + end;
        }

        StringBuilder b = new StringBuilder();
        b.append(start);
        for (int i = 0; i < array.length; i++) {
            b.append(String.valueOf(array[i]));
            if (i + 1 < array.length) {
                b.append(DEFAULT_ARRAY_SEPARATOR);
            }
        }
        b.append(end);
        return b.toString();
    }

    public static String toString(double[] array, String start, String end) {
        if (array == null) {
            return start + "null" + end;
        }

        StringBuilder b = new StringBuilder();
        b.append(start);
        for (int i = 0; i < array.length; i++) {
            b.append(String.valueOf(array[i]));
            if (i + 1 < array.length) {
                b.append(DEFAULT_ARRAY_SEPARATOR);
            }
        }
        b.append(end);
        return b.toString();
    }

    public static String beautifyForHTML(String source) {
        return source.replaceAll("_", " ").replaceAll("&", "&amp;").replaceAll("-", " - ").replaceAll("/", " / ").replaceAll(
                "  ", " ");
    }

    public static boolean equals(Object o, String s) {
        return o instanceof String && org.apache.commons.lang.StringUtils.equals((String) o, s);
    }

    /** Checks whether the given String equals any of the given options. */
    public static boolean equalsAny(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.equals(element)) {
                return true;
            }
        }
        return false;
    }

    /** Checks whether the given String equals any of the given options when calling {@link Object#toString()} on them */
    public static boolean equalsAny(String s, Object... options) {
        if (s == null) {
            return false;
        }
        for (Object element : options) {
            if (s.equals(element.toString())) {
                return true;
            }
        }
        return false;
    }

    /** Checks whether the given String equals any of the given options, ignoring case */
    public static boolean equalsAnyIgnoreCase(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.equalsIgnoreCase(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equalsAny(Object o, String... options) {
        return o instanceof String && equalsAny((String) o, options);
    }

    /** Checks whether the given String starts with any of the given options. */
    public static boolean startsWithAny(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.startsWith(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean endsWithAny(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.endsWith(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAny(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.contains(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesAny(String[] regExps, String s) {
        for (String regExp : regExps) {
            if (s.matches(regExp)) {
                // System.out.println("Match: " + s + " on reg-exp" + regExps[i]);
                return true;
            }
        }
        return false;
    }

    /** Checks whether the given String starts with any of the given options, ignoring the case */
    public static boolean startsWithAnyIgnoreCase(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.toLowerCase().startsWith(element.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /** Prints a double[] with the given number of decimal digits. */
    public static String toStringWithPrecision(double[] a, int digits) {
        if (a == null) {
            return "null";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "[]";
        }
        StringBuilder b = new StringBuilder("[");
        for (double element : a) {
            b.append(String.format("%1." + digits + "f ", element));
        }
        b.setCharAt(b.length() - 1, ']');
        return b.toString();

        // DecimalFormat decimalFormat = getDecimalFormat(digits);
        // b.append('[');
        // for (int i = 0;; i++) {
        // String formatted = decimalFormat.format(a[i]);
        // b.append(formatted + getSpaces(digits + 3 - formatted.length()));
        // if (i == iMax) {
        // return b.append(']').toString();
        // }
        // }
    }

    /**
     * Returns a string representation of the contents of the specified array in the same fashion as
     * {@link Arrays#toString(double[])}, but limiting the output to the given maxIndices parameter.
     */
    public static String toString(double[] a, int maxElements) {
        if (a == null) {
            return "null";
        }
        int iMax = Math.min(a.length, maxElements);

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; i < iMax; i++) {
            b.append(a[i]);
            if (i + 1 < iMax) {
                b.append(DEFAULT_ARRAY_SEPARATOR);
            }
            if (i + 1 == maxElements && a.length > maxElements) {
                b.append(DEFAULT_ELIPSIS);
            }
        }
        b.append(']');
        return b.toString();
    }

    public static String toString(double[][] data) {
        StringBuilder sb = new StringBuilder(data.length * data[0].length * 5);
        // sb.append(data.length).append("x").append(data[0].length).append(" matrix\n");
        for (double[] element : data) {
            sb.append(toString(element, "", "")).append("\n");
        }
        return sb.toString();
    }

    public static String toString(double[][][] data) {
        StringBuilder sb = new StringBuilder(data.length * data[0].length * data[0][0].length * 5);
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                double[] array = data[i][j];
                sb.append(i + "/" + j + " [");
                for (int k = 0; k < array.length; k++) {
                    sb.append(format(array[k], 3, true));
                    if (k + 1 < array.length) {
                        sb.append(",");
                    }
                }
                sb.append("]\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static DecimalFormat getDecimalFormat(int digits) {
        String precision = "";
        for (int i = 0; i < digits; i++) {
            precision += "#";
        }
        final DecimalFormat decimalFormat = new DecimalFormat("#." + precision);
        return decimalFormat;
    }

    public static String getSpaces(int num) {
        return repeatString(num, " ");
    }

    public static String repeatString(int num, String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static int getLongestStringLength(Iterable<String> c) {
        int max = 0;
        for (String s : c) {
            if (s.length() > max) {
                max = s.length();
            }
        }
        return max;
    }

    public static int getLongestStringLength(String[] c) {
        int max = 0;
        for (String s : c) {
            if (s.length() > max) {
                max = s.length();
            }
        }
        return max;
    }

    public static String formatMaxLengthEllipsis(String s, int maxLen) {
        return formatMaxLengthEllipsis(s, maxLen, DEFAULT_ELIPSIS);
    }

    public static String formatMaxLengthEllipsis(String s, int maxLen, String ellipsis) {
        if (maxLen != -1 && s.length() > maxLen && maxLen > ellipsis.length()) {// content longer than allowed =>
            // //shorten the content
            int cutAt = maxLen - ellipsis.length();
            return s.substring(0, cutAt) + ellipsis;
        }
        return s;
    }

    public static String formatEndMaxLengthEllipsis(String s, int maxLen) {
        return formatEndMaxLengthEllipsis(s, maxLen, DEFAULT_ELIPSIS);
    }

    public static String formatEndMaxLengthEllipsis(String s, int maxLen, String ellipsis) {
        if (maxLen != -1 && s.length() > maxLen && maxLen > ellipsis.length()) {// content longer than allowed =>
            // //shorten the content
            int cutAt = maxLen - ellipsis.length();
            return ellipsis + s.substring(s.length() - cutAt);
        }
        return s;
    }

    public static String toString(Point2D[] a) {
        int digits = 4;
        if (a == null) {
            return "null";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "[]";
        }

        DecimalFormat decimalFormat = getDecimalFormat(digits);
        StringBuilder b = new StringBuilder();
        for (Point2D element : a) {
            String formatted = decimalFormat.format(element.getX()) + "/" + decimalFormat.format(element.getY());
            b.append(formatted).append(getSpaces((digits + 3) * 2 - formatted.length())).append(" ");
        }
        return b.toString();
    }

    public static String toString(Point2D[][] a) {
        StringBuilder sb = new StringBuilder(a.length * a[0].length * 5);
        for (Point2D[] element : a) {
            sb.append(toString(element)).append("\n");
        }
        return sb.toString();
    }

    @SuppressWarnings("rawtypes")
    public static String printMap(Map m) {
        StringBuffer sb = new StringBuffer("{");
        for (Object key : m.keySet()) {
            Object value = m.get(key);
            if (sb.length() > 1) {
                sb.append(DEFAULT_ARRAY_SEPARATOR);
            }
            sb.append(key).append("=");
            sb.append(ArrayUtils.toString(value));
        }
        return sb.append("}").toString();
    }

    /** Returns the common starting portion of the two Strings, or an empty String if there is no common part. */
    public static String getCommonPrefix(String s1, String s2) {
        int i = 0;
        if (s1 == null || s2 == null || s1.length() == 0 || s2.length() == 0) {
            return "";
        }
        while (i < s1.length() && i < s2.length() && s1.charAt(i) == s2.charAt(i)) {
            i++;
        }
        return s1.substring(0, i);
    }

    public static String getCommonPrefix(Collection<String> c) {
        if (c.size() == 0) {
            return "";
        }

        Iterator<String> iterator = c.iterator();
        String commonPrefix = iterator.next();
        while (iterator.hasNext()) {
            commonPrefix = getCommonPrefix(commonPrefix, iterator.next());
        }
        return commonPrefix;
    }

    public static String getCommonPrefix(String[] a) {
        if (a.length == 0) {
            return "";
        }

        String commonPrefix = a[0];
        for (int i = 1; i < a.length; i++) {
            commonPrefix = getCommonPrefix(commonPrefix, a[i]);
        }
        return commonPrefix;
    }

    /** Returns the common starting portion of the two Strings, or an empty String if there is no common part. */
    public static String getCommonSuffix(String s1, String s2) {
        int i = 0;
        if (s1 == null || s2 == null || s1.length() == 0 || s2.length() == 0) {
            return "";
        }
        while (i < s1.length() && i < s2.length() && s1.charAt(s1.length() - 1 - i) == s2.charAt(s2.length() - 1 - i)) {
            i++;
        }
        return s1.substring(s1.length() - i);
    }

    public static String getCommonSuffix(Collection<String> c) {
        if (c.size() == 0) {
            return "";
        }

        Iterator<String> iterator = c.iterator();
        String commonSuffix = iterator.next();
        while (iterator.hasNext()) {
            commonSuffix = getCommonSuffix(commonSuffix, iterator.next());
        }
        return commonSuffix;
    }

    public static String getCommonSuffix(String[] a) {
        if (a.length == 0) {
            return "";
        }

        String commonSuffix = a[0];
        for (int i = 1; i < a.length; i++) {
            commonSuffix = getCommonSuffix(commonSuffix, a[i]);
        }
        return commonSuffix;
    }

    public static String[] getDifferences(String[] a) {
        String[] res = new String[a.length];
        String commonPrefix = getCommonPrefix(a);
        String commonSuffix = getCommonSuffix(a);
        for (int i = 0; i < res.length; i++) {
            res[i] = a[i].substring(commonPrefix.length(), a[i].length() - commonSuffix.length());
        }
        return res;
    }

    /**
     * Calculate the Levenshtein Distance between the two given Strings (String1 and String2).
     * 
     * @param s String1
     * @param t String2
     * @return the Levenshtein Distance between the two strings
     * @see <a
     *      href="http://www.javalobby.org/java/forums/t15908.html">http://www.javalobby.org/java/forums/t15908.html</a>
     */
    public static int levenshteinDistance(String s, String t) {
        int n = s.length();
        int m = t.length();

        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }

        int[][] d = new int[n + 1][m + 1];

        for (int i = 0; i <= n; d[i][0] = i++) {
            ;
        }
        for (int j = 1; j <= m; d[0][j] = j++) {
            ;
        }

        for (int i = 1; i <= n; i++) {
            char sc = s.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int v = d[i - 1][j - 1];
                if (t.charAt(j - 1) != sc) {
                    v++;
                }
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), v);
            }
        }
        return d[n][m];
    }

    public static String[] concat(String[] array1, String[] array2) {
        if (array2 == null) {
            return array1;
        } else if (array1 == null) {
            return array2;
        }
        int lengthFirst = array1.length;
        int lengthSecond = array2.length;
        String[] ret = new String[lengthFirst + lengthSecond];
        System.arraycopy(array1, 0, ret, 0, lengthFirst);
        System.arraycopy(array2, 0, ret, lengthFirst, lengthSecond);
        return ret;
    }

    public static String[] trim(String[] split) {
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }

    public static void main(String[] args) {
        String[] strings = { "partition_0_1370_converted", "partition_1_1696_converted", "partition_2_1729_converted",
                "partition_3_1937_converted", "partition_4_1669_converted", "partition_5_1146_converted" };
        System.out.println(Arrays.toString(strings));
        System.out.println(getCommonPrefix(strings));
        System.out.println(getCommonSuffix(strings));
        System.out.println(Arrays.toString(getDifferences(strings)));

        double[] doubles = { .5, 1.5, 11.5, 21.5 };
        for (double d : doubles) {
            System.out.println("printing " + d);
            System.out.println(format(d, 2));
            System.out.println(format(d, 2, true));
            System.out.println(format(d, 2, true, 2));
            System.out.println(format(d, 2, true, 4));
            System.out.println();
        }

        System.out.println(toString(doubles, doubles.length));
        System.out.println(toString(doubles, 2));
        System.out.println(toString(doubles, 3));

        System.out.println(toString(strings, strings.length));
        System.out.println(toString(strings, 2));
        System.out.println(toString(strings, 4));

    }

    public static final String[][] STRING_URLENCODE_REPLACEMENTS = { { "/", "_" }, { "?", "_" }, { "&", "_" },
            { "=", "_" }, { "%3A", ":" }, { "%2C", "," }, { "%25", "_" }, { "%C3%A8", "e" }, { "%C3%A9", "e" },
            { "%C3%89", "E" }, { "%C3%A1", "a" }, { "%C3%B6", "ö" }, { "%C3%96", "Ö" }, { "%C3%AB", "e" },
            { "%C3%B3", "o" }, { "%C2%A1", "j" }, { "%C3%BC", "ü" }, { "%C3%9C", "Ü" }, { "%C2%BD", ",5" },
            { "%C3%A0", "a" }, { "%C3%A4", "a" }, { "%C3%AF", "i" }, { "%60", "'" }, { "%C3%BA", "u" },
            { "%C3%B1", "n" }, { "%C3%81", "A" }, { "%C3%A6", "ae" }, { "%C2%B0", "" }, { "%C3%AA", "e" },
            { "%C3%B8", "o" }, { "%C3%A9", "o" }, { "%C3%AA", "e" }, { "%C3%A7", "c" }, { "%C3%B4", "o" },
            { "%C3%AD", "i" }, { "%C3%80", "A" }, { "%C3%A3", "a" }, { "%C3%BF", "y" }, { "%C2%93", "" },
            { "%C2%94", "" }, { "%C2%B2", "2" }, { "%C3%83%C2%AD", "i" }, { "%C3%9F", "ss" }, { "%C3%B2", "o" },
            { "%C3%A2", "a" }, { "%C3%87", "C" }, { "%C3%AE", "i" }, { "%C3%88", "e" }, { "%C2%B7", "-" },
            { "%C3%83%C2%BC", "ü" }, { "%C3%BB", "u" }, { "%C3%93", "O" }, { "%C3%A5", "a" }, { "%C3%B9", "u" },
            { "%C3%BD", "y" }, { "%C3%83%C2%A3", "A" }, { "%C3%A3", "a" }, { "%C3%83%C2%A9", "e" }, { "%C3%86", "Ae" },
            { "%C3%84", "Ä" }, { "%C3%AC", "i" }, { "%C2%92", "'" }, { "%C3%B0", "o" }, { "%C3%82%C2%92", "'" },
            { "%C3%82", "A" }, { "%C3%98", "O" }, { "%C3%9A", "U" }, { "%C2%B5", "mu" }, { "%C2%AA", "a" },
            { "%C3%8D", "I" }, { "%C3%83%C2%B1", "n" }, { "%C3%B5", "o" }, { "%C3%83%C2%B3", "o" }, { "%C3%83", "A" },
            { "%C3%83%C2%B8", "a" }, { "A%C2%B8", "a" }, { "%C2%A4", "a" }, { "%C2%AE", "(R)" }, { "%C3%85", "A" } };

    public static String replaceURLEncode(String s) {
        for (String[] strings : STRING_URLENCODE_REPLACEMENTS) {
            if (s.contains(strings[0])) {
                s = s.replace(strings[0], strings[1]);
            }
        }
        return s;
    }

    /**
     * formats a boolean value according to the given type returns the needed string representation of a boolean value
     * that can be printed to the report.<br/>
     * At the moment, only one transtormation is supported:
     * <ul>
     * <li>type: 0: true -> yes, false->no (default)</li>
     * </ul>
     * 
     * @param type the type of conversion/strings wished (see above)
     * @param value the boolean value
     * @return a string that encodes the boolean value in a way that it can be printed
     */
    public static String formatBooleanValue(int type, boolean value) {
        switch (type) {
            default:
                return value ? "yes" : "no";
        }
    }

    /**
     * creates an representation of a color that can be used in html or css takes an int - array of size 3 and
     * transforms it to a hex String of structure #rrggbb The array must have at least length 3 and must not contain
     * null values.
     * 
     * @param rgb an array specifying the red, green and blue parts of the color
     * @return a hex string with structure #rrggbb according to the values in the given array
     */
    public static String getRGBString(int[] rgb) {
        return "#" + Integer.toHexString(rgb[0]) + Integer.toHexString(rgb[1]) + Integer.toHexString(rgb[2]);
    }

    /**
     * creates an representation of a color that can be used in html or css takes an int - array of size 3 and
     * transforms it to a hex String of structure #rrggbb The array must have at least length 3 and must not contain
     * null values.
     * 
     * @param rgb an array specifying the red, green and blue parts of the color
     * @return a hex string with structure #rrggbb according to the values in the given array
     */
    public static String getLatexRGBString(int[] rgb) {
        double r = Math.round((float) ((double) rgb[0] / (double) 255 * 100)) / 100d;
        double g = Math.round((float) ((double) rgb[1] / (double) 255 * 100)) / 100d;
        double b = Math.round((float) ((double) rgb[2] / (double) 255 * 100)) / 100d;
        return r + "," + g + "," + b;
    }

    /**
     * returns the correct format of the given String needed for displaying it in the comparison table this means, that
     * if the String is null or empty, "-" is returned instead of the String
     * 
     * @param value the string that shall be formatted
     * @return the input string or "-" if the input is inadequate
     */
    public static String formatString(String value) {
        if (value == null || value.length() <= 0) {
            return "-";
        } else {
            return value;
        }
    }

    /**
     * returns the correct format of a double needed for displaying it in the comparison table this means, that if the
     * value is smaller than 0, the string "-" is returned instead of the value
     * 
     * @param value a double value for which a String representation is needed
     * @return a string containing the value of the double up to 2 values behind the ., or "-" if the value is smaller 0
     */
    public static String formatDouble(double value) {
        if (value < 0) {
            return "-";
        } else {
            return String.format("%.2f", value);
        }
    }

    /**
     * This extra method had to be written because URLEncoder.encode 1) performs an encoding of a slash (/), 2) encodes
     * a space as a +, but should encode it as %20. Note that URLEncoder.encode is originally meant for encoding HTML
     * form data only, not arbitrary URLs. However, no other methods of encoding special characters in URLs has been
     * found (the URI methods did not work).
     * 
     * @param url a non-encoded URL
     * @return encoded URL
     */
    public static String URLencode(String url) {
        String parts[] = url.split("/");
        StringBuffer encodedURL = new StringBuffer();

        // leave part 0, which is the protocol (http:) unchanged
        encodedURL.append(parts[0]);

        // and start with part 1
        for (int i = 1; i < parts.length; i++) {
            try {
                // if (i > 0)
                encodedURL.append("/");
                encodedURL.append(URLEncoder.encode(parts[i], "UTF-8"));
            } catch (UnsupportedEncodingException uee) {
                System.err.println(uee.getMessage());
                return null;
            }
        }

        // Spaces are replaced by + but should be replaced by %20
        // + that existed before have already been replace by %2B in the previous step

        return encodedURL.toString().replace("+", "%20");
    }

    public static String ensureExtension(String fileName, String extension) {
        if (!fileName.endsWith(extension)) {
            return fileName + extension;
        }
        return fileName;
    }

    public static String interleave(String[] s, String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            sb.append(s[i]);
            if (i + 1 < s.length) {
                sb.append(string);
            }
        }
        return sb.toString();
    }

    public static String interleave(double[] d, String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length; i++) {
            sb.append(d[i]);
            if (i + 1 < d.length) {
                sb.append(string);
            }
        }
        return sb.toString();
    }

    public static String appendExtension(String fileName, String extension) {
        if (!fileName.endsWith("gz") && !fileName.endsWith(extension)) {
            return fileName + extension;
        } else {
            return fileName;
        }
    }

    public static String appendOrReplaceExtension(String fileName, String oldExtension, String newExtension) {
        return StringUtils.appendExtension(fileName.replace(oldExtension, newExtension), newExtension);
    }

    public static String pad(double value, int len) {
        return StringUtils.pad(format(value, 5), len);
    }

    public static String pad(String s, int len) {
        return s + getSpaces(len - s.length());
    }

    /**
     * @param string The {@link String} to wrap
     * @param lineWidth the width <code>string</code> is wrapped to
     * @return the <code>string</code> wrapped to the width of <code>lineWidth</code>
     */
    public static String wrap(String string, int lineWidth) {
        return wrap(string, lineWidth, 0);
    }

    /**
     * @param string The {@link String} to wrap
     * @param lineWidth the width <code>string</code> is wrapped to
     * @param indent prefix each newly added line with <code>indent</code> spaces
     * @return the <code>string</code> wrapped to the width of <code>lineWidth</code>
     */
    public static String wrap(String string, int lineWidth, int indent) {
        return wrap(string, lineWidth, indent, true);
    }

    /**
     * @param string The {@link String} to wrap
     * @param lineWidth the width <code>string</code> is wrapped to
     * @param indent prefix each newly added line with <code>indent</code> spaces
     * @param wordBoundaries only wrap at word boudaries (aka Spaces).
     * @return the <code>string</code> wrapped to the width of <code>lineWidth</code>
     */
    public static String wrap(String string, int lineWidth, int indent, boolean wordBoundaries) {
        return wrap(string, lineWidth, getSpaces(indent), wordBoundaries);
    }

    /**
     * @param string The {@link String} to wrap
     * @param lineWidth the width <code>string</code> is wrapped to
     * @param indentString each newly added line will be prefixed with this string
     * @return the <code>string</code> wrapped to the width of <code>lineWidth</code>
     */
    public static String wrap(String string, int lineWidth, String indentString) {
        return wrap(string, lineWidth, indentString, true);
    }

    /**
     * @param string The {@link String} to wrap
     * @param lineWidth the width <code>string</code> is wrapped to
     * @param indentString each newly added line will be prefixed with this string
     * @param wordBoundaries only wrap at word boudaries (aka Spaces).
     * @return the <code>string</code> wrapped to the width of <code>lineWidth</code>
     */
    public static String wrap(String string, int lineWidth, String indentString, boolean wordBoundaries) {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        StringBuilder result = new StringBuilder();
        BufferedReader br = new BufferedReader(new StringReader(string));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                if (wordBoundaries) {
                    String[] words = line.split(" ");
                    int curLineWidth = 0;
                    for (String word : words) {
                        if (curLineWidth + word.length() + 2 >= lineWidth) {
                            result.append(lineSeparator);
                            result.append(indentString);
                            curLineWidth = indentString.length();
                        }
                        result.append(word).append(" ");
                        curLineWidth += word.length() + 1;
                    }
                    result.deleteCharAt(result.length() - 1);
                } else {
                    int stepWidth = lineWidth - 1;
                    for (int i = 0; i < line.length(); i += stepWidth) {
                        if (i > 0) {
                            stepWidth = lineWidth - 1 - indentString.length();
                            if (line.charAt(i) == ' ') {
                                i++;
                            }
                        }
                        result.append(line.substring(i, Math.min(i + stepWidth, line.length())));
                        if (i + stepWidth < line.length()) {
                            result.append(lineSeparator);
                            result.append(indentString);
                        }
                    }
                }
                result.append(lineSeparator);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    /** parses double values from a comma-separated string */
    public static double[] parseDoubles(String s) {
        String[] parts = s.split(",");
        return parseDoubles(parts);
    }

    public static double[] parseDoublesAndRanges(String s) {
        String[] parts = s.split(",");
        ArrayList<Double> resultTemp = new ArrayList<Double>(parts.length);
        for (String part2 : parts) {
            String part = part2.trim();
            if (part.contains("-")) {
                String[] rangeValues = part.split("-");
                double rangeStart = Double.parseDouble(rangeValues[0]);
                double rangeEnd = Double.parseDouble(rangeValues[1]);
                for (double j = rangeStart; j <= rangeEnd; j++) {
                    resultTemp.add(j);
                }
                if (!resultTemp.contains(rangeEnd)) {
                    resultTemp.add(rangeEnd);
                }
            } else {
                resultTemp.add(Double.parseDouble(part));
            }
        }
        double[] result = new double[resultTemp.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = resultTemp.get(i).doubleValue();
        }
        return result;
    }

    /** parses double values from a string array, after trimming of the strings is performed */
    private static double[] parseDoubles(String[] parts) {
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Double.parseDouble(parts[i].trim());
        }
        return result;
    }

    public static String escapeForWeka(String label) {
        return label.replaceAll("'", "_").replaceAll(" ", "_");
    }

    public static String escapeClassNameForWeka(String label) {
        return label.replaceAll("'", "_");
    }

}
