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
package at.tuwien.ifs.somtoolbox.visualization.thematicmap;

import java.awt.Graphics2D;

import edu.cornell.cs.voronoi.IPnt;
import edu.cornell.cs.voronoi.Pnt;

/**
 * @author Taha Abdel Aziz
 * @version $Id: Segment.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class Segment {
    public IPnt end1;

    public IPnt end2;

    public SOMRegion neighborRegion;

    /** Creates a new instance of Segment */
    public Segment(IPnt end1, IPnt end2) {
        this.end1 = end1;
        this.end2 = end2;
    }

    public void draw(Graphics2D g) {
        g.drawLine((int) end1.coord(0), (int) end1.coord(1), (int) end2.coord(0), (int) end2.coord(1));
    }

    @Override
    public String toString() {
        return "end1: " + end1 + " -- end2: " + end2;
    }

    public void flip() {
        IPnt x = end1;
        end1 = end2;
        end2 = x;
    }

    public Pnt getMidPoint() {
        double x = (end1.coord(0) + end2.coord(0)) / 2;
        double y = (end1.coord(1) + end2.coord(1)) / 2;
        return new Pnt(x, y);
    }

    public IPnt getPointFrom(int from, double distance) {
        IPnt p1 = null;
        IPnt p2 = null;
        IPnt ret = new Pnt(0, 0);
        if (from == 0) {
            p1 = end1;
            p2 = end2;
        } else {
            p1 = end2;
            p2 = end1;
        }

        double x1 = p1.coord(0);
        double y1 = p1.coord(1);
        double x2 = p2.coord(0);
        double y2 = p2.coord(1);

        if (x1 == x2) { // vertical
            if (y2 > y1) {
                return new Pnt(x1, y1 + distance);
            } else {
                return new Pnt(x1, y1 - distance);
            }

        } else if (y1 == y2) { // horizontal
            if (x2 > x1) {
                return new Pnt(x1 + distance, y1);
            } else {
                return new Pnt(x1 - distance, y1);
            }
        } else {
            double alpha = Math.atan(Math.abs((y2 - y1) / (x2 - x1)));
            double deltaX = distance * Math.cos(alpha);
            double deltaY = distance * Math.sin(alpha);
            if (x2 > x1 && y2 > y1) { // Quartal 1
                return new Pnt(x1 + deltaX, y1 + deltaY);
            } else if (x2 < x1 && y2 > y1) { // Quartal 2
                return new Pnt(x1 - deltaX, y1 + deltaY);
            } else if (x2 < x1 && y2 < y1) { // Quartal 3
                return new Pnt(x1 - deltaX, y1 - deltaY);
            } else if (x2 > x1 && y2 < y1) { // Quartal 4
                return new Pnt(x1 + deltaX, y1 - deltaY);
            }
        }
        return ret;

    }

    public double getLength() {
        double x1 = end1.coord(0);
        double y1 = end1.coord(1);
        double x2 = end2.coord(0);
        double y2 = end2.coord(1);

        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

}
