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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;

import org.jfree.util.PaintList;

import edu.cornell.cs.voronoi.IPnt;
import edu.cornell.cs.voronoi.Pnt;

import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Taha Abdel Aziz
 * @version $Id: SOMRegion.java 3883 2010-11-02 17:13:23Z frank $
 */
public class SOMRegion extends Polygon implements IPnt {
    private static final long serialVersionUID = 1L;

    Pnt center;

    ArrayList<Segment> segments = new ArrayList<Segment>();

    Color borderColor = Color.BLACK;

    Color fillcolor = Color.YELLOW;

    public ArrayList<SOMClass> classes = new ArrayList<SOMClass>();

    public SOMClass mainClass;

    Unit unit;

    SOMLibClassInformation classInfo;

    boolean resolved = false;

    double area = 0;

    RndIndexGenerator indexGenerator;

    ArrayList<Grid> grids;

    private ArrayList<Polygon> polygons;

    double min_visible_class = 0;

    private PaintList paintList;

    /** Creates a new instance of SOMNode */
    /*
     * public SOMRegion(Pnt center) { super(center.coord(0), center.coord(1)); }
     */

    public SOMRegion(Unit unit, SOMLibClassInformation classInfo, PaintList paintList, int zoom) {
        this.center = new Pnt(unit.getXPos() * zoom + RegionManager.BORDER, unit.getYPos() * zoom
                + RegionManager.BORDER);
        this.unit = unit;
        this.classInfo = classInfo;
        this.paintList = paintList;
        double[] values = new double[classInfo.numClasses()];
        for (int v = 0; v < values.length; v++) {
            values[v] = 0;
        }
        for (int i = 0; i < unit.getNumberOfMappedInputs(); i++) {
            int classIndex = classInfo.getClassIndex(unit.getMappedInputName(i));
            if (classIndex == -1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Class index could not be retrieved for item " + unit.getMappedInputName(i)
                                + "; ignoring item.");
            } else {
                values[classIndex] += 1;
            }
        }

        // int clssIndex=0;
        double maxValue = 0;
        double allHits = 0;

        for (int i = 0; i < classInfo.numClasses(); i++) {
            if (values[i] > 0) {
                SOMClass clss = new SOMClass(i, values[i]);
                classes.add(clss);
                allHits += values[i];
                if (values[i] > maxValue) {
                    maxValue = values[i];
                    mainClass = clss;
                }
            }

        }

        for (int i = 0; i < classes.size(); i++) {
            SOMClass sc = classes.get(i);
            sc.share = sc.hits / allHits;
        }
        indexGenerator = new RndIndexGenerator(classes);
    }

    public SOMRegion(Unit unit, IPnt pnt, int clssIndex, SOMLibClassInformation classInfo, PaintList paintList) {
        center = new Pnt(pnt.coord(0), pnt.coord(1));
        this.unit = unit;
        this.classInfo = classInfo;
        this.paintList = paintList;
        // this.mainClassIndex=clssIndex;
        double[] values = new double[classInfo.numClasses()];
        for (int v = 0; v < values.length; v++) {
            values[v] = 0;
        }
        for (int i = 0; i < unit.getNumberOfMappedInputs(); i++) {
            values[classInfo.getClassIndex(unit.getMappedInputNames()[i])] += 1;
        }

        double maxValue = 0;
        double allHits = 0;

        for (int i = 0; i < classInfo.numClasses(); i++) {
            if (values[i] > 0) {
                SOMClass clss = new SOMClass(i, values[i]);
                classes.add(clss);
                allHits += values[i];
                if (values[i] > maxValue) {
                    maxValue = values[i];
                    mainClass = clss;
                }
            }

        }

        for (int i = 0; i < classes.size(); i++) {
            SOMClass sc = classes.get(i);
            sc.share = sc.hits / allHits;
        }

        indexGenerator = new RndIndexGenerator(classes);
    }

    public void addSegment(Segment seg) {
        segments.add(seg);
    }

    public Unit getUnit() {
        return unit;
    }

    public void sortSegments() {
        if (segments.isEmpty()) {
            return;
        }
        Segment s = segments.get(0);
        ArrayList<Segment> res = new ArrayList<Segment>();
        res.add(s);
        segments.remove(s);
        int n = 0;
        boolean done = true;
        while (!segments.isEmpty()) {
            if (n > 100) {
                done = false;
                break;
            }
            n++;
            for (int i = 0; i < segments.size(); i++) {
                Segment ss = segments.get(i);
                if (s.end2.equals(ss.end1) && !s.end1.equals(ss.end2)) {
                    res.add(ss);
                    segments.remove(ss);
                    s = ss;
                    break;
                } else if (s.end2.equals(ss.end2) && !s.end1.equals(ss.end1)) {
                    res.add(ss);
                    segments.remove(ss);
                    ss.flip();
                    s = ss;
                    break;
                }
            }
        }

        if (!done) {
            if (!res.isEmpty()) {
                s = res.get(0);
            } else {
                s = segments.get(segments.size() - 1);
            }

            n = 0;
            while (!segments.isEmpty()) {
                if (n > 100) {
                    break;
                }
                n++;
                for (int i = 0; i < segments.size(); i++) {
                    Segment ss = segments.get(i);
                    if (s.end1.equals(ss.end2) && !s.end2.equals(ss.end1)) {
                        res.add(0, ss);
                        segments.remove(ss);
                        s = ss;
                        break;
                    } else if (s.end1.equals(ss.end1) && !s.end2.equals(ss.end2)) {
                        res.add(0, ss);
                        segments.remove(ss);
                        ss.flip();
                        s = ss;
                        break;
                    }
                }
            }
        }
        segments = res;

        int len = segments.size();
        int[] xx = new int[len];
        int[] yy = new int[len];
        Point[] points = new Point[len];
        for (int i = 0; i < len; i++) {
            Segment seg = segments.get(i);
            xx[i] = (int) seg.end1.coord(0);
            yy[i] = (int) seg.end1.coord(1);
            points[i] = new Point(xx[i], yy[i]);
        }

        super.xpoints = xx;
        super.ypoints = yy;
        super.npoints = len;
        super.getBounds();
    }

    public void makeUniqe() {
        if (segments.isEmpty()) {
            return;
        }
        ArrayList<Segment> res = new ArrayList<Segment>();

        while (!segments.isEmpty()) {
            Segment s = segments.get(0);
            if (s.end1.equals(s.end2)) {
                segments.remove(s);
                continue;
            }
            res.add(s);
            segments.remove(s);
            IPnt p1 = s.end1;
            IPnt p2 = s.end2;
            for (int i = 0; i < segments.size(); i++) {
                Segment ss = segments.get(i);
                IPnt pp1 = ss.end1;
                IPnt pp2 = ss.end2;
                if (p1.equals(pp1) && p2.equals(pp2) || p1.equals(pp2) && p2.equals(pp1)) {
                    segments.remove(ss);
                }
            }
        }
        segments = res;
    }

    public void drawRegion(Graphics2D g) {
        if (segments.size() > 0) {
            g.setColor(borderColor);
            g.setStroke(new BasicStroke(0.0f));
            g.drawPolygon(this);
        }
    }

    public void fillRegion(Graphics2D g, boolean chessboard) {
        if (!resolved) {
            fillcolor = (Color) paintList.getPaint(mainClass.classIndex);
            Color c = repairColor(fillcolor);
            g.setColor(c);
            if (segments.isEmpty()) {
                return;
            }
            g.fillPolygon(this);
        } else {
            if (chessboard) {
                if (polygons == null) { // calculate polygons
                    polygons = new ArrayList<Polygon>();

                    Rectangle2D rect = getBounds2D();
                    double w = rect.getWidth();

                    double h = rect.getHeight();

                    if (h > 200 || w > 200) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("ERROR: h>200 & w>200");
                        return;
                    }

                    int x = (int) rect.getX();
                    int y = (int) rect.getY();

                    int xSteps = (int) (w / (int) Grid.SIZE);
                    int ySteps = (int) (h / (int) Grid.SIZE);
                    // int n = classes.size();
                    for (int i = 0; i < xSteps; i++) {
                        for (int j = 0; j < ySteps; j++) {
                            Polygon p = new Polygon();
                            p.addPoint((int) (x + i * Grid.SIZE), (int) (y + j * Grid.SIZE));
                            p.addPoint((int) (x + i * Grid.SIZE + Grid.SIZE), (int) (y + j * Grid.SIZE));
                            p.addPoint((int) (x + i * Grid.SIZE + Grid.SIZE), (int) (y + Grid.SIZE + j * Grid.SIZE));
                            p.addPoint((int) (x + i * Grid.SIZE), (int) (y + Grid.SIZE + j * Grid.SIZE));
                            if (!this.contains(p.getBounds())) {
                                continue;
                            }
                            SOMClass clss = indexGenerator.getNextIndex();
                            g.setColor((Color) paintList.getPaint(clss.classIndex));
                            g.fillPolygon(p);
                            polygons.add(p);
                        }
                    }
                } else { // use pre-calculated polygons
                    for (int i = 0; i < polygons.size(); i++) {
                        SOMClass clss = indexGenerator.getNextIndex();
                        g.setColor((Color) paintList.getPaint(clss.classIndex));
                        Polygon p = polygons.get(i);
                        g.fillPolygon(p);
                    }
                }
            } else {
                for (int i = 0; i < grids.size(); i++) {
                    Grid grid = grids.get(i);
                    if (grid.clss == null) {
                        continue;
                    }
                    g.setColor((Color) paintList.getPaint(grid.clss.classIndex));
                    g.fillRect((int) grid.topLeft.coord(0), (int) grid.topLeft.coord(1), (int) Grid.SIZE,
                            (int) Grid.SIZE);
                }
            }
        }
    }

    private Color repairColor(Color color) {
        int red = Math.min(color.getRed() + 10, 255);
        int green = Math.min(color.getGreen() + 10, 255);
        int blue = Math.min(color.getBlue() + 10, 255);
        return new Color(red, green, blue);
    }

    public void setRegionBorderColor(Color col) {
        this.borderColor = col;
    }

    public void setFillColor(Color col) {
        this.fillcolor = col;
    }

    public SOMClass getClass(int index) {
        for (int i = 0; i < classes.size(); i++) {
            SOMClass c = classes.get(i);
            if (c.classIndex == index) {
                return c;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            s += seg + "\n";
        }
        return s + "-----------------------";
    }

    int getRelationNumber(int index) {
        int ret = 0;
        for (int i = 0; i < segments.size(); i++) {
            Segment ss = segments.get(i);
            if (ss.neighborRegion != null) {
                for (int n = 0; n < ss.neighborRegion.classes.size(); n++) {
                    SOMClass sc = ss.neighborRegion.classes.get(n);
                    if (sc.classIndex == index) {
                        ret++;
                    }
                }
            }
        }
        return ret;
    }

    double[] getRelationNumberAndWeights(int index) {
        double[] ret = new double[2];
        for (int i = 0; i < segments.size(); i++) {
            Segment ss = segments.get(i);
            if (ss.neighborRegion != null) {
                for (int n = 0; n < ss.neighborRegion.classes.size(); n++) {
                    SOMClass sc = ss.neighborRegion.classes.get(n);
                    if (sc.classIndex == index) {
                        ret[0]++;
                        ret[1] += sc.hits;
                    }
                }
            }
        }
        return ret;
    }

    public void cut(int width, int height) {
        int left = 0;
        int right = 1;
        int oben = 0;
        int unten = 1;
        for (int i = 0; i < segments.size(); i++) {
            Segment ss = segments.get(i);
            double x1 = ss.end1.coord(0);
            double y1 = ss.end1.coord(1);
            double x2 = ss.end2.coord(0);
            double y2 = ss.end2.coord(1);
            int hor1 = -1;
            int ver1 = -1;

            int hor2 = -1;
            int ver2 = -1;

            if (x1 > width) {
                ss.end1 = new Pnt(width, y1);
                hor1 = right;
            }
            if (x1 < 0) {
                ss.end1 = new Pnt(0, y1);
                hor1 = left;
            }
            if (y1 > height) {
                ss.end1 = new Pnt(x1, height);
                ver1 = unten;
            }
            if (y1 < 0) {
                ss.end1 = new Pnt(x1, 0);
                ver1 = oben;
            }

            if (x2 > width) {
                ss.end2 = new Pnt(width, y2);
                hor2 = right;
            }
            if (x2 < 0) {
                ss.end2 = new Pnt(0, y2);
                hor2 = left;
            }
            if (y2 > height) {
                ss.end2 = new Pnt(x2, height);
                ver2 = unten;
            }
            if (y2 < 0) {
                ss.end2 = new Pnt(x2, 0);
                ver2 = oben;
            }

            if (ver2 == oben && hor1 == right || ver1 == oben && hor2 == right) {
                segments.remove(ss);
                segments.add(new Segment(new Pnt(x1, y1), new Pnt(width, 0)));
                segments.add(new Segment(new Pnt(width, 0), new Pnt(x2, y2)));
            }
            if (ver2 == unten && hor1 == right || ver1 == unten && hor2 == right) {
                segments.remove(ss);
                segments.add(new Segment(new Pnt(x1, y1), new Pnt(width, height)));
                segments.add(new Segment(new Pnt(width, height), new Pnt(x2, y2)));
            }

            if (ver2 == oben && hor1 == left || ver1 == oben && hor2 == left) {
                segments.remove(ss);
                segments.add(new Segment(new Pnt(x1, y1), new Pnt(0, 0)));
                segments.add(new Segment(new Pnt(0, 0), new Pnt(x2, y2)));
            }

            if (ver2 == unten && hor1 == left || ver1 == unten && hor2 == left) {
                segments.remove(ss);
                segments.add(new Segment(new Pnt(x1, y1), new Pnt(0, height)));
                segments.add(new Segment(new Pnt(0, height), new Pnt(x2, y2)));
            }
        }
    }

    public void calcRelations() {
        double max = 0;
        for (int n = 0; n < classes.size(); n++) {
            SOMClass sc = classes.get(n);
            double[] relationNumberAndWeights = getRelationNumberAndWeights(sc.classIndex);
            sc.relationNum = (int) relationNumberAndWeights[0];
            sc.relationWeight = relationNumberAndWeights[1];
            if (sc.relationWeight > max) {
                max = sc.relationNum;
                mainClass = sc;
            }

        }
        mainClass.finished = true;
    }

    public void resolve(double min_visible_class) {
        this.min_visible_class = min_visible_class;
        resolved = true;

        if (grids == null) {
            grids = new ArrayList<Grid>();
            calcGrids();
            assignClassGrids();
        }
    }

    public void calcGrids() {
        Rectangle2D rect = getBounds2D();
        double w = rect.getWidth();

        double h = rect.getHeight();

        if (h > 150 || w > 150) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").fine("Error: " + this);
            return;
        }

        int x = (int) rect.getX();
        int y = (int) rect.getY();

        int xSteps = (int) (w / (int) Grid.SIZE);
        int ySteps = (int) (h / (int) Grid.SIZE);

        for (int i = 0; i < xSteps; i++) {
            for (int j = 0; j < ySteps; j++) {
                Polygon p = new Polygon();
                p.addPoint((int) (x + i * Grid.SIZE), (int) (y + j * Grid.SIZE));
                p.addPoint((int) (x + i * Grid.SIZE + Grid.SIZE), (int) (y + j * Grid.SIZE));
                p.addPoint((int) (x + i * Grid.SIZE + Grid.SIZE), (int) (y + Grid.SIZE + j * Grid.SIZE));
                p.addPoint((int) (x + i * Grid.SIZE), (int) (y + Grid.SIZE + j * Grid.SIZE));
                if (this.contains(p.getBounds().x + Grid.SIZE / 2, p.getBounds().y + Grid.SIZE / 2)) {
                    Pnt topLeft = new Pnt(x + i * Grid.SIZE, y + j * Grid.SIZE);
                    Pnt bottomRight = new Pnt(x + i * Grid.SIZE + Grid.SIZE, y + Grid.SIZE + j * Grid.SIZE);
                    Grid grid = new Grid(topLeft, bottomRight);
                    grids.add(grid);
                }
            }
        }
    }

    static int x = 0;

    public void assignClassGrids() {
        Iterator<SOMClass> it = classes.iterator();

        while (it.hasNext()) {
            SOMClass clss = it.next();
            if (clss.finished) {
                continue;
            }
            if (clss.share == 0) {
                System.out.println("Class share == 0!");
            }
            if (clss.share < min_visible_class) {
                continue;
            }
            int n = getRelationNumber(clss.classIndex);
            if (n == 0) { // isoliert
                Segment virtualConnection = new Segment(this, this);
                assignClassGrids(clss, virtualConnection, 1.0, 1, 1);

            } else {
                for (int i = 0; i < segments.size(); i++) {
                    Segment s = segments.get(i);
                    SOMRegion nachbar = s.neighborRegion;
                    if (nachbar == null) {
                        continue;
                    }
                    SOMClass otherClass = nachbar.getClass(clss.classIndex);
                    if (otherClass == null) {
                        continue;
                    }
                    Segment virtualConnection;

                    if (n > 1) {
                        int nextSegInd = i + 1;
                        if (nextSegInd >= segments.size()) {
                            nextSegInd = 0;
                        }
                        Segment nextSeg = segments.get(nextSegInd);
                        SOMRegion nextReg = nextSeg.neighborRegion;
                        SOMClass c = null;
                        if (nextReg != null) {
                            c = nextReg.getClass(clss.classIndex);
                        }
                        if (c == null) {
                            nextSegInd = i - 1;
                            if (nextSegInd < 0) {
                                nextSegInd = segments.size() - 1;
                            }
                            nextSeg = segments.get(nextSegInd);
                            nextReg = nextSeg.neighborRegion;
                            if (nextReg != null) {
                                c = nextReg.getClass(clss.classIndex);
                            }
                            if (c == null) {
                                virtualConnection = new Segment(this, s.getMidPoint());
                            } else {
                                virtualConnection = new Segment(s.end1, s.end1);
                            }
                        } else {
                            virtualConnection = new Segment(s.end2, s.end2);
                        }

                        // virtualConnection = new Segment(s.end1, s.end2);
                        assignClassGrids(clss, virtualConnection, 1.0 / n, clss.share, otherClass.share);
                    } else {
                        // virtualConnection = new Segment(s.end1, s.end2);
                        virtualConnection = new Segment(this, s.getMidPoint());
                        assignClassGrids(clss, virtualConnection, 1.0, clss.share, otherClass.share);
                    }
                }
            }
            // clss.finished=true;
        }
    }

    public void assignClassGrids(SOMClass clss, Segment seg, double anteilVonAnteil, double weight1, double weight2) {
        Collections.sort(grids, new SegDistComperator(seg, weight1, weight2));
        int n = (int) (grids.size() * clss.share * anteilVonAnteil);
        int finisched = 0;

        for (int i = 0; i < grids.size() && finisched < n; i++) {
            Grid grid = grids.get(i);
            if (grid.occupied) {
                continue;
            }
            grid.occupied = true;
            grid.clss = clss;
            finisched++;
        }
        // clss.finished=true;
    }

    // private class ClassComparator implements Comparator{
    // public int compare(Object o1, Object o2){
    // SOMClass c1 = (SOMClass)o1;
    // SOMClass c2 = (SOMClass)o2;
    // if(c1.anteil < c2.anteil)
    // return 1;
    // else if (c1.anteil > c2.anteil)
    // return -1;
    // else
    // return 0;
    // }
    //    
    // }
    //    
    @Override
    public Pnt add(Pnt p) {
        return center.add(p);
    }

    @Override
    public double angle(Pnt p) {
        return center.angle(p);
    }

    @Override
    public Pnt bisector(Pnt point) {
        return center.bisector(point);

    }

    @Override
    public double coord(int i) {
        return center.coord(i);
    }

    @Override
    public int dimCheck(Pnt p) {
        return center.dimCheck(p);

    }

    @Override
    public int dimension() {
        return center.dimension();
    }

    @Override
    public double dot(Pnt p) {
        return center.dot(p);
    }

    @Override
    public boolean equals(Object other) {
        return center.equals(other, true);
    }

    @Override
    public boolean equals(Object other, boolean round) {
        return center.equals(other, round);
    }

    @Override
    public Pnt extend(double[] coords) {
        return center.extend(coords);
    }

    @Override
    public int hashCode() {
        int retValue;

        retValue = center.hashCode();
        return retValue;
    }

    @Override
    public boolean isInside(Pnt[] simplex) {
        return center.isInside(simplex);
    }

    @Override
    public Pnt isOn(Pnt[] simplex) {
        return center.isOn(simplex);
    }

    @Override
    public Pnt isOutside(Pnt[] simplex) {
        return center.isOutside(simplex);
    }

    @Override
    public double magnitude() {
        return center.magnitude();
    }

    @Override
    public int[] relation(Pnt[] simplex) {
        return center.relation(simplex);
    }

    @Override
    public Pnt subtract(Pnt p) {
        return center.subtract(p);
    }

    @Override
    public int vsCircumcircle(Pnt[] simplex) {
        return center.vsCircumcircle(simplex);
    }

    /**
     * Resolves the Main Class Index from any given ClassIndex in the Region, or -1 if the ClassINdex is not part of the
     * Region
     */
    public int resolveMainClassIndex(int index) {
        for (int i = 0; i < classes.size(); i++) {
            SOMClass clss = this.classes.get(i);
            if (index == clss.classIndex) {
                return this.mainClass.classIndex;
            }
        }
        return -1;
    }

    /** Returns the SOMClass with the specified index or NULL otherwise */
    public SOMClass getClassWithIndex(int index) {
        for (int i = 0; i < this.classes.size(); i++) {
            SOMClass clss = this.classes.get(i);
            if (clss.classIndex == index) {
                return clss;
            }
        }
        return null;
    }

    /** Calculates the Entropy in the SOM Region */
    public double calculateEntropy() {
        int n = 0;
        double e = 0.0;
        // calculate the amount of inputs
        for (int i = 0; i < this.classes.size(); i++) {
            SOMClass clss = classes.get(i);
            n += (int) clss.hits;
        }
        // calculate the entropy
        for (int i = 0; i < this.classes.size(); i++) {
            SOMClass clss = classes.get(i);
            // double c = clss.hits / n;
            // double v = Math.log(clss.hits / n) / Math.log(2.0);
            e -= clss.hits / n * Math.log(clss.hits / n) / Math.log(2.0);
        }
        return e;
    }

    /** Returns the class names of the Region on [0] and its hits on [1] */
    public String[][] getClasses() {

        String[][] cNames = new String[this.classes.size()][2];
        for (int i = 0; i < cNames.length; i++) {
            int cIndex = this.classes.get(i).classIndex;
            cNames[i][0] = classInfo.classNames()[cIndex];
            cNames[i][1] = String.valueOf((int) this.classes.get(i).hits);
        }
        return cNames;
    }
}
