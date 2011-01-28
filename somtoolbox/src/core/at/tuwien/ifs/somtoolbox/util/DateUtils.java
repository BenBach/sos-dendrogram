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

public class DateUtils {

    /**
     * Formats length of time periods in a nice format
     * 
     * @param milis a time difference in milliseconds to format
     * @return formatted time (e.g.: "1 second", "3 hours", "2 weeks" "41 years"...)
     */
    public static String formatDurationOneUnit(long milis) {
        long secs = milis / 1000l;

        if (secs < 2l) {
            return "1 second";
        }
        if (secs == 2l) {
            return "2 seconds";
        }

        // default:
        long value = secs;
        String unit = "seconds";

        // now try to give it a meaning:
        long[] breaks = { 31536000, 2628000, 604800, 86400, 3600, 60, 1 };
        String[] desc = { "year", "month", "week", "day", "hour", "minute", "second" };

        int i = 0;
        while (i <= breaks.length && secs <= 2 * breaks[i]) {
            i++;
        }
        value = secs / breaks[i];
        unit = desc[i];
        if (value >= 2) {
            unit = unit + "s";
        }

        String retval = value + " " + unit;

        // if...
        return retval;
    }

    /**
     * Formats length of time periods in a nice format
     * 
     * @param milis a time difference in milliseconds to format
     * @return formatted time (e.g.: "3 hours, 2 minutes, 5 seconds")
     */
    public static String formatDuration(long milis) {
        // special formatting for times under 10 second
        if (milis < 10 * 1000) {
            return milis / 10 / 100.0 + " seconds";
        }
        long seconds = milis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        String result = "";
        if (hours > 0) {
            result += hours;
            if (hours == 1) {
                result += " hour, ";
            } else {
                result += " hours, ";
            }
            minutes = minutes - hours * 60;
            seconds = seconds - hours * 60 * 60;
        }
        if (minutes > 0) {
            result += minutes;
            if (minutes == 1) {
                result += " minute, ";
            } else {
                result += " minutes, ";
            }
            seconds = seconds - minutes * 60;
        }
        result += seconds;
        if (seconds == 1) {
            result += " second";
        } else {
            result += " seconds";
        }
        return result;
    }

    public static String shortFormatDuration(long millis) {
        boolean isNeg = millis < 0;
        millis = Math.abs(millis);
        double sec = millis / 1000d;
        millis %= 1000;
        long min = (long) Math.floor(sec) / 60;
        sec = sec % 60;
        long hrs = min / 60;
        min = min % 60;

        if (isNeg) {
            hrs *= -1;
        }

        return String.format("%d:%02d:%02.0f", hrs, min, sec);
    }

    public static void main(String[] args) {
        // Test: shall print 1 hour, 52 minutes, 58 seconds
        System.out.println(formatDuration((1 * 60 * 60 + 52 * 60 + 58) * 1000 + 132));
        System.out.println(shortFormatDuration((1 * 60 * 60 + 52 * 60 + 58) * 1000 + 132));
        // Test: shall print 2 hours, 1 minute, 1 second
        System.out.println(formatDuration((2 * 60 * 60 + 1 * 60 + 1) * 1000 + 895));
        System.out.println(shortFormatDuration((2 * 60 * 60 + 1 * 60 + 1) * 1000 + 895));
    }
}