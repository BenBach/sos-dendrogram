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
package at.tuwien.ifs.somtoolbox.visualization.comparison;

/**
 * @author Doris Baum
 * @version $Id: Shift.java 3883 2010-11-02 17:13:23Z frank $
 */
public class Shift implements Comparable<Shift> {
    private int x1 = 0;

    private int y1 = 0;

    private int x2 = 0;

    private int y2 = 0;

    private int count = -1;

    private double percent = -1;

    private double proportion = -1;

    private int breadth = 1;

    private String label = "";

    private int type = -1;

    public static final int STABLE = 1;

    public static final int ADJACENT = 2;

    public static final int OUTLIER = 3;

    public static final int CLUSTER = 4;

    @Override
    public int compareTo(Shift other) {
        int compareY1 = new Integer(y1).compareTo(other.y1);
        if (compareY1 != 0) {
            return compareY1;
        } else {
            int compareX1 = new Integer(x1).compareTo(other.x1);
            if (compareX1 != 0) {
                return compareX1;
            } else {
                int compareY2 = new Integer(y2).compareTo(other.y2);
                if (compareY2 != 0) {
                    return compareY2;
                } else {
                    int compareX2 = new Integer(x2).compareTo(other.x2);
                    return compareX2;
                }
            }
        }
    }

    public String getLabel() {
        return label;
    }

    public int getX1() {
        return x1;
    }

    public int getX2() {
        return x2;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    protected void setCoords(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    protected void setLabel(String label) {
        this.label = label;
    }

    protected void setX1(int x1) {
        this.x1 = x1;
    }

    protected void setX2(int x2) {
        this.x2 = x2;
    }

    protected void setY1(int y1) {
        this.y1 = y1;
    }

    protected void setY2(int y2) {
        this.y2 = y2;
    }

    public int getType() {
        return type;
    }

    protected void setType(int type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public double getProportion() {
        return proportion;
    }

    protected void setCount(int count) {
        this.count = count;
    }

    protected void setProportion(double proportion) {
        this.proportion = proportion;
    }

    @Override
    public String toString() {
        String string = "(" + x1 + ", " + y1 + ") --> (" + x2 + ", " + y2 + "); " + label + ", count: " + count;
        return string;
    }

    public int getBreadth() {
        return breadth;
    }

    protected void setBreadth(int breadth) {
        this.breadth = breadth;
    }

    public double getPercent() {
        return percent;
    }

    protected void setPercent(double percent) {
        this.percent = percent;
    }
}
