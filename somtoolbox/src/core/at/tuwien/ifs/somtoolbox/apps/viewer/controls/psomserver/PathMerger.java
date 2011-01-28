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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.util.LineShape;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.data.InputDatum;

/**
 * @author Jakob Frank
 * @version $Id: PathMerger.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class PathMerger {

    private final MapPNode map;

    private PNode root, borderRoot, connsRoot, circlesRoot;

    private List<PNode> drawedStuff;

    private boolean debug;

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public PathMerger(MapPNode map) {
        this(map, false);
    }

    public PathMerger(MapPNode map, boolean debug) {
        this.map = map;
        this.debug = debug;
        this.drawedStuff = new LinkedList<PNode>();

        root = new PNode();
        borderRoot = new PNode();
        connsRoot = new PNode();
        circlesRoot = new PNode();

        root.addChild(borderRoot);
        root.addChild(connsRoot);
        root.addChild(circlesRoot);
        map.addChild(root);

    }

    public void unitBasedMerge(PNode[] paths) {
        if (paths.length != 2) {
            return;
        }

        deleteAllDrawnStuff();

        List<GeneralUnitPNode> u1 = convertPathToUnits(paths[0]);
        List<GeneralUnitPNode> u2 = convertPathToUnits(paths[1]);

        for (GeneralUnitPNode unit1 : u1) {
            GeneralUnitPNode unit2 = findClosest(unit1, u2);
            System.out.printf("Pair: %d/%d - %d/%d%n", unit1.getUnit().getXPos(), unit1.getUnit().getYPos(),
                    unit2.getUnit().getXPos(), unit2.getUnit().getYPos());
            float x1 = (float) (unit1.getX() + unit1.getWidth() / 2);
            float y1 = (float) (unit1.getY() + unit1.getHeight() / 2);
            float x2 = (float) (unit2.getX() + unit2.getWidth() / 2);
            float y2 = (float) (unit2.getY() + unit2.getHeight() / 2);
            PPath p = PPath.createLine(x1, y1, x2, y2);
            p.setPickable(false);
            p.setStrokePaint(Color.white);
            drawedStuff.add(p);
            paths[0].addChild(p);
        }

        for (GeneralUnitPNode unit1 : u2) {
            GeneralUnitPNode unit2 = findClosest(unit1, u1);
            System.out.printf("Pair: %d/%d - %d/%d%n", unit1.getUnit().getXPos(), unit1.getUnit().getYPos(),
                    unit2.getUnit().getXPos(), unit2.getUnit().getYPos());
            float x1 = (float) (unit1.getX() + unit1.getWidth() / 2);
            float y1 = (float) (unit1.getY() + unit1.getHeight() / 2);
            float x2 = (float) (unit2.getX() + unit2.getWidth() / 2);
            float y2 = (float) (unit2.getY() + unit2.getHeight() / 2);
            PPath p = PPath.createLine(x1, y1, x2, y2);
            p.setPickable(false);
            p.setStrokePaint(Color.black);
            drawedStuff.add(p);
            paths[1].addChild(p);
        }

    }

    public void deleteAllDrawnStuff() {
        while (drawedStuff.size() > 0) {
            drawedStuff.remove(0).removeFromParent();
        }
        root.removeAllChildren();
    }

    public void newIntermediateMapMerge(PNode[] paths) {
        if (paths.length != 2) {
            return;
        }

        deleteAllDrawnStuff();

        PLine l1 = (PLine) paths[0].getChild(0);
        PLine l2 = (PLine) paths[1].getChild(0);

        PLine border = new PLine();

        Point2D s1 = l1.getPoint(0, new Point2D.Double());
        int i1 = 0, i2 = 0;
        double minD = Double.MAX_VALUE;
        for (int i = 0; i < l2.getPointCount(); i++) {
            double curD = s1.distanceSq(l2.getPoint(i, new Point2D.Double()));
            if (curD < minD) {
                minD = curD;
                i2 = i;
            }
        }

        for (; i1 < l1.getPointCount() && i2 < l2.getPointCount(); i1++, i2++) {
            Point2D middle = getMiddlePoint(l1.getPoint(i1, new Point2D.Double()),
                    l2.getPoint(i2, new Point2D.Double()));
            // System.out.printf("LinePoint: %f/%f%n", middle.getX() / map.getWidth(), middle.getY() / map.getHeight());
            border.addPoint(border.getPointCount(), middle.getX(), middle.getY());
        }

        border.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        border.setStrokePaint(Color.black);

        drawedStuff.add(border);
        borderRoot.addChild(border);
    }

    public void newIntermediateInputSpaceMerge(PNode[] paths) {
        if (paths.length != 2) {
            return;
        }

        deleteAllDrawnStuff();

        List<GeneralUnitPNode> ul1 = convertPathToUnits(paths[0]);
        List<GeneralUnitPNode> ul2 = convertPathToUnits(paths[1]);

        PLine border = new PLine();

        int i1 = 0, i2 = 0;
        GeneralUnitPNode s1 = ul1.get(i1);
        double minD = Double.MAX_VALUE;
        for (int i = 0; i < ul2.size(); i++) {
            double curD = distSq(s1.getUnit().getWeightVector(), ul2.get(i).getUnit().getWeightVector());
            if (curD < minD) {
                minD = curD;
                i2 = i;
            }
        }

        CommonSOMViewerStateData state = CommonSOMViewerStateData.getInstance();

        for (; i1 < ul1.size() && i2 < ul2.size(); i1++, i2++) {
            double[] m1 = ul1.get(i1).getUnit().getWeightVector();
            double[] m2 = ul2.get(i2).getUnit().getWeightVector();

            double[] middle = getMiddlePoint(m1, m2);

            GeneralUnitPNode m = map.getUnit(state.growingLayer.getWinner(new InputDatum("", middle)));

            // System.out.println("AddPoint");
            border.addPoint(border.getPointCount(), m.getX() + m.getWidth() / 2, m.getY() + m.getHeight() / 2);
        }

        border.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        border.setStrokePaint(Color.black);

        drawedStuff.add(border);
        borderRoot.addChild(border);
    }

    private double dist(double[] d1, double[] d2) {
        return Math.sqrt(distSq(d1, d2));
    }

    private double distSq(double[] d1, double[] d2) {
        double res = 0;
        for (int i = 0; i < d1.length; i++) {
            res += Math.pow(d1[i] - d2[i], 2);
        }
        return res;
    }

    private double[] getMiddlePoint(double[] d1, double[] d2) {
        double[] res = new double[d1.length];
        double dist = dist(d1, d2) / 2;

        for (int i = 0; i < res.length; i++) {
            res[i] = d1[i] + dist * (d1[i] - d2[i]);
        }
        return res;
    }

    public void lineBasedMerge(PNode[] paths) {
        if (paths.length != 2) {
            return;
        }

        deleteAllDrawnStuff();

        PLine l1 = (PLine) paths[0].getChild(0);
        PLine l2 = (PLine) paths[1].getChild(0);

        PLine border = new PLine();

        findBorder(l1, l2, Color.white, border);
        findBorder(l2, l1, Color.white, border);

        Point2D s1 = l1.getPoint(0, new Point2D.Double());
        Point2D s2 = l2.getPoint(0, new Point2D.Double());
        Point2D e2 = l2.getPoint(l2.getPointCount() - 1, new Point2D.Double());

        Point2D current;
        if (s1.distance(s2) < s1.distance(e2)) {
            current = getMiddlePoint(s1, s2);
        } else {
            current = getMiddlePoint(s1, e2);
        }

        LinkedList<Point2D> borderPointList = new LinkedList<Point2D>();
        for (int i = 0; i < border.getPointCount(); i++) {
            borderPointList.add(border.getPoint(i, new Point2D.Double()));
        }

        PPath startPoint = createCircle(current, 7f);

        border = new PLine();
        border.addPoint(0, current.getX(), current.getY());
        while (borderPointList.size() > 0) {
            Point2D next = findClosest(current, borderPointList);
            border.addPoint(border.getChildrenCount(), next.getX(), next.getY());
            borderPointList.remove(next);
            current = next;
        }
        border.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        border.setStrokePaint(Color.black);
        startPoint.setStrokePaint(Color.white);
        if (debug) {
            drawedStuff.add(startPoint);
            border.addChild(startPoint);
        }
        drawedStuff.add(border);
        borderRoot.addChild(border);

    }

    /**
     * Find the border between two lines.
     * 
     * @param l1 first Line
     * @param l2 second Line
     * @param c {@link Color} of the border
     * @param border where to add the border
     */
    private void findBorder(PLine l1, PLine l2, Color c, PLine border) {
        float width = ((BasicStroke) l1.getStroke()).getLineWidth();
        BasicStroke s = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        LineShape ls = l1.getLineReference();
        for (int i = 0; i < ls.getPointCount(); i++) {
            Point2D p1 = ls.getPoint(i, new java.awt.geom.Point2D.Double());
            Point2D p2 = findClosest(p1, l2);

            Point2D middle = getMiddlePoint(p1, p2);
            border.addPoint(border.getChildrenCount(), middle.getX(), middle.getY());

            if (debug) {
                PLine l = new PLine();
                l.addPoint(0, p1.getX(), p1.getY());
                l.addPoint(1, p2.getX(), p2.getY());
                l.setStroke(s);
                l.setStrokePaint(c);
                l.setTransparency(.4f);
                drawedStuff.add(l);
                l1.addChild(l);

                PPath m = createCircle(middle, width / 4);
                m.setTransparency(.4f);
                m.setStrokePaint(Color.red);
                drawedStuff.add(m);
                l1.addChild(m);

                PPath a = createCircle(p1, width / 2);
                a.setTransparency(.4f);
                a.setStroke(s);
                a.setStrokePaint(c);
                a.removeAllChildren();
                drawedStuff.add(a);
                l1.addChild(a);
            }

        }
    }

    /**
     * Create a circle {@link PNode}
     * 
     * @param center the center of the circle
     * @param radius the radius (size) of the circle
     * @return a round {@link PNode}
     */
    private PPath createCircle(Point2D center, float radius) {
        return PPath.createEllipse((float) center.getX() - radius, (float) center.getY() - radius, 2 * radius,
                2 * radius);
    }

    /**
     * Get the middle point between the Points.
     * 
     * @param p1 first Point
     * @param p2 second Point
     * @return the Point in the middle
     */
    private Point2D getMiddlePoint(Point2D p1, Point2D p2) {
        return new java.awt.geom.Point2D.Double(p1.getX() + .5f * (p2.getX() - p1.getX()), p1.getY() + .5f
                * (p2.getY() - p1.getY()));
    }

    private Point2D findClosest(Point2D point, PLine candidates) {
        Point2D closest = null, current = null;
        double minDist = Double.MAX_VALUE;
        LineShape ls = candidates.getLineReference();
        for (int i = 0; i < ls.getPointCount(); i++) {
            current = ls.getPoint(i, new java.awt.geom.Point2D.Double());
            double d = point.distanceSq(current);
            if (d < minDist) {
                minDist = d;
                closest = current;
            }
        }

        return closest;
    }

    private Point2D findClosest(Point2D point, List<Point2D> candidates) {
        Point2D closest = null, current = null;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < candidates.size(); i++) {
            current = candidates.get(i);
            double d = point.distanceSq(current);
            if (d < minDist) {
                minDist = d;
                closest = current;
            }
        }

        return closest;
    }

    private <A extends PNode> A findClosest(A unit, List<A> candidates) {
        A closest = null;
        double minDist = Double.MAX_VALUE;
        for (A c : candidates) {
            double curDist = Math.pow(c.getX() - unit.getX(), 2) + Math.pow(c.getY() - unit.getY(), 2);
            if (curDist < minDist) {
                minDist = curDist;
                closest = c;
            }
        }
        return closest;
    }

    private List<GeneralUnitPNode> convertPathToUnits(PNode node) {
        LinkedList<GeneralUnitPNode> units = new LinkedList<GeneralUnitPNode>();

        Iterator<?> children = node.getChildrenIterator();
        GeneralUnitPNode lastU1 = null;
        while (children.hasNext()) {
            PNode child = (PNode) children.next();
            if (child instanceof PLine) {
                PLine line = (PLine) child;
                float width = ((BasicStroke) line.getStroke()).getLineWidth();
                Point2D p = null;
                for (int i = 0; i < line.getPointCount(); i++) {
                    p = line.getPoint(i, p);
                    GeneralUnitPNode u = map.getGeneralUnitPNodeAtPos(p);
                    if (u != lastU1) {
                        units.add(u);
                        lastU1 = u;
                    }
                    if (debug) {
                        PLine l = new PLine();
                        l.addPoint(0, p.getX(), p.getY());
                        l.addPoint(1, u.getX() + u.getWidth() / 2, u.getY() + u.getHeight() / 2);
                        l.setStrokePaint(Color.white);
                        l.setTransparency(.4f);
                        l.setStroke(new BasicStroke(width / 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        drawedStuff.add(l);
                        line.addChild(l);
                        PPath a = createCircle(p, width / 2);
                        a.setTransparency(.4f);
                        a.setStroke(new BasicStroke(width / 4));
                        a.setStrokePaint(Color.white);
                        a.removeAllChildren();
                        drawedStuff.add(a);
                        line.addChild(a);
                    }
                }
            }
        }

        return units;
    }

    @SuppressWarnings("unused")
    private String printNode(PNode node) {
        PBounds b = node.getBounds();
        return String.format("%s (%.2f %.2f %.2f %.2f)", node.getClass().getSimpleName(), b.x, b.y, b.height, b.width);
    }

    public void highlightIntersectingUnits(PNode node, boolean target) {
        for (GeneralUnitPNode n : convertPathToUnits(node)) {
            n.setSelected(target);
        }
    }

    public void concatPaths(PNode[] paths) {
        if (paths.length != 2) {
            return;
        }

        deleteAllDrawnStuff();

        PLine l1 = (PLine) paths[0].getChild(0);
        PLine l2 = (PLine) paths[1].getChild(0);

        Point2D s1 = l1.getPoint(0, new Point2D.Double());
        Point2D e1 = l1.getPoint(l1.getPointCount() - 1, new Point2D.Double());
        Point2D s2 = l2.getPoint(0, new Point2D.Double());
        Point2D e2 = l2.getPoint(l2.getPointCount() - 1, new Point2D.Double());

        Point2D from, to;
        if (e1.distanceSq(s2) < e2.distanceSq(s1)) {
            // 1 after 2
            from = e1;
            to = s2;
        } else {
            // 2 after 1
            from = e2;
            to = s1;
        }
        final int steps = 5;
        double xStep = (to.getX() - from.getX()) / steps;
        double yStep = (to.getY() - from.getY()) / steps;

        // TODO: Allow routing in different spaces...
        PLine trans = new PLine();
        for (int i = 0; i <= steps; i++) {
            trans.addPoint(i, from.getX() + xStep * i, from.getY() + yStep * i);
            if (debug && i != 0 && i != steps) {
                PPath z = createCircle(trans.getPoint(i, new Point2D.Double()),
                        ((BasicStroke) l1.getStroke()).getLineWidth() / 2);
                z.setStrokePaint(Color.BLACK);
                z.setPaint(null);
                circlesRoot.addChild(z);
            }
        }
        trans.addChild(createCircle(trans.getPoint(0, new Point2D.Double()),
                ((BasicStroke) l1.getStroke()).getLineWidth() / 2 * 1.25f));
        trans.setStroke(l1.getStroke());
        trans.setStrokePaint(Color.MAGENTA);
        borderRoot.addChild(trans);

        if (debug) {
            PPath s = createCircle(from, ((BasicStroke) l1.getStroke()).getLineWidth() / 2);
            s.setStrokePaint(Color.WHITE);
            s.setPaint(Color.WHITE);
            s.setTransparency(.7f);
            circlesRoot.addChild(s);
            PPath e = createCircle(to, ((BasicStroke) l1.getStroke()).getLineWidth() / 2);
            e.setStrokePaint(Color.BLACK);
            e.setPaint(Color.BLACK);
            e.setTransparency(.7f);
            circlesRoot.addChild(e);
        }
    }

    public PLine getBorderLine() {
        if (borderRoot.getChildrenCount() == 1) {
            return (PLine) borderRoot.getChild(0);
        }
        return null;
    }

}
