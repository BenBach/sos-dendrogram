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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;

import org.jfree.util.PaintList;

import edu.cornell.cs.voronoi.DelaunayTriangulation;
import edu.cornell.cs.voronoi.Pnt;
import edu.cornell.cs.voronoi.Simplex;

import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.util.ProgressListener;
import at.tuwien.ifs.somtoolbox.util.ProgressListenerFactory;

/**
 * @author Taha Abdel Aziz
 * @version $Id: RegionManager.java 3888 2010-11-02 17:42:53Z frank $
 */
public class RegionManager {
    public static final int BORDER = 5;

    public static final int DEFAULT_ZOOM_FACTOR = 10;

    private int zoom = DEFAULT_ZOOM_FACTOR;

    int initialSize = 10000;

    int width;

    int height;

    ArrayList<SOMRegion> regions = new ArrayList<SOMRegion>();

    Simplex initialTriangle = new Simplex(new Pnt[] { new Pnt(-initialSize, -initialSize),
            new Pnt(initialSize, -initialSize), new Pnt(0, initialSize) });

    DelaunayTriangulation mainDt = new DelaunayTriangulation(initialTriangle);

    DelaunayTriangulation resolvedDt = new DelaunayTriangulation(initialTriangle);

    SOMLibClassInformation classInfo;

    double min_visible_class = 0;

    private PaintList paintList;

    public RegionManager(SOMLibClassInformation classInfo, PaintList paintList, int width, int height,
            double min_visible_class) {
        this(classInfo, paintList, width, height, min_visible_class, DEFAULT_ZOOM_FACTOR);
    }

    /** Creates a new instance of SOMegions */
    public RegionManager(SOMLibClassInformation classInfo, PaintList paintList, int width, int height,
            double min_visible_class, int zoom) {
        this.classInfo = classInfo;
        this.paintList = paintList;
        this.width = width;
        this.height = height;
        this.min_visible_class = min_visible_class;
        this.zoom = zoom;
    }

    public SOMRegion getRegion(ArrayList<SOMRegion> candidates, double x, double y) {
        for (int i = 0; i < candidates.size(); i++) {
            SOMRegion r = candidates.get(i);
            if (r.equals(new Pnt(x, y))) {
                return r;
            }
        }
        return null;
    }

    public SOMRegion getRegion(ArrayList<SOMRegion> candidates, Pnt center) {
        for (int i = 0; i < candidates.size(); i++) {
            SOMRegion r = candidates.get(i);
            if (r.equals(center)) {
                return r;
            }
        }
        return null;
    }

    public SOMRegion addNewRegion(Unit unit) {
        SOMRegion r = new SOMRegion(unit, classInfo, paintList, zoom);
        regions.add(r);
        double x = (double) unit.getXPos() * zoom + BORDER;
        double y = (double) unit.getYPos() * zoom + BORDER;
        Pnt point = new Pnt(x, y);
        mainDt.delaunayPlace(point);
        return r;
    }

    public void removeRegion(SOMRegion r) {
        regions.remove(r);
    }

    public void drawRegions(Graphics2D g) {
        if (regions.isEmpty()) {
            return;
        }
        for (int i = 0; i < regions.size(); i++) {
            SOMRegion r = regions.get(i);
            r.drawRegion(g);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void drawDelaunayTrangulation(Graphics2D g) {
        for (Iterator it = mainDt.iterator(); it.hasNext();) {
            Simplex triangle = (Simplex) it.next();
            Pnt[] t_points = (Pnt[]) triangle.toArray(new Pnt[0]);
            for (int i = 0; i < t_points.length; i++) {
                int x1 = (int) t_points[i].coord(0);
                int y1 = (int) t_points[i].coord(1);
                int x2 = 0;
                int y2 = 0;

                if (i + 1 < t_points.length) {
                    x2 = (int) t_points[i + 1].coord(0);
                    y2 = (int) t_points[i + 1].coord(1);
                } else {
                    x2 = (int) t_points[0].coord(0);
                    y2 = (int) t_points[0].coord(1);
                }
                g.setColor(Color.CYAN);
                g.drawLine(x1, y1, x2, y2);
            }
        }
    }

    public void fillRegions(Graphics2D g, boolean chessboard) {
        if (!regions.isEmpty()) {
            for (int i = 0; i < regions.size(); i++) {
                regions.get(i).fillRegion(g, chessboard);
            }
            for (int i = 0; i < regions.size(); i++) {
                regions.get(i).resolve(min_visible_class);
            }

            for (int i = 0; i < regions.size(); i++) {
                regions.get(i).fillRegion(g, chessboard);
            }
        }
    }

    /**
     * Fills SOMRegions according to the specified ClassID. Note that SOMRegions where the ClassID is not the Main class
     * are colored lighter.
     */

    public void fillSingleRegion(Graphics2D g, boolean chessboard, int ClassID) {
        for (int i = 0; i < regions.size(); i++) {
            SOMRegion r = regions.get(i);
            int resolve = r.resolveMainClassIndex(ClassID);
            Color c = classInfo.getClassColor(ClassID);
            if (resolve != ClassID && resolve > -1) {
                /*
                 * double p = r.getClassWithIndex(ClassID).hits / r.mainClass.hits ; if(p >= 1.0) p=p-1.0; int red =
                 * Math.min((int)Math.round(c.getRed()*(1+p)), 255); int green = Math.min((int)Math.round(c.getGreen()*(1+p)), 255); int blue =
                 * Math.min((int)Math.round(c.getBlue()*(1+p)), 255); Color d = new Color(red,green,blue);
                 */
                g.setColor(c);
                g.fillPolygon(r);
            }
            if (r.mainClass.classIndex == ClassID) {
                g.setColor(c);
                g.fillPolygon(r);
            }
        }
    }

    public void build() {
        ProgressListener progressWriter = ProgressListenerFactory.getInstance().createProgressListener(regions.size(),
                "Processing region ", 10);
        build(regions, mainDt);

        for (int i = 0; i < regions.size(); i++) {
            SOMRegion r = regions.get(i);
            r.makeUniqe();
            cut();
            r.sortSegments();
            r.calcRelations();
            progressWriter.progress();
        }
    }

    public void cut() {
        for (int i = 0; i < regions.size(); i++) {
            SOMRegion r = regions.get(i);
            r.cut(width, height);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void build(ArrayList<SOMRegion> regions, DelaunayTriangulation dt) {
        Color[] c = new Color[8];
        c[0] = Color.BLACK;
        c[1] = Color.BLUE;
        c[2] = Color.CYAN;
        c[3] = Color.DARK_GRAY;
        c[4] = Color.GREEN;
        c[5] = Color.RED;
        c[6] = Color.YELLOW;
        c[7] = Color.MAGENTA;
        // int n=0;
        // Loop through all the edges of the DT (each is done twice)
        for (Iterator it = dt.iterator(); it.hasNext();) {
            Simplex triangle = (Simplex) it.next();
            Pnt[] t_points = (Pnt[]) triangle.toArray(new Pnt[0]);
            Pnt p = Pnt.circumcenter(t_points);
            for (Iterator otherIt = dt.neighbors(triangle).iterator(); otherIt.hasNext();) {
                Simplex other = (Simplex) otherIt.next();

                Pnt[] n_points = (Pnt[]) other.toArray(new Pnt[0]);
                Pnt q = Pnt.circumcenter(n_points);
                ArrayList<Pnt> common = new ArrayList<Pnt>();
                for (Pnt t_point : t_points) {
                    for (Pnt n_point : n_points) {
                        if (t_point.equals(n_point)) {
                            common.add(t_point);
                        }
                    }
                }

                Pnt node1 = common.get(0);
                Pnt node2 = common.get(1);

                SOMRegion r1 = getRegion(regions, node1);
                SOMRegion r2 = getRegion(regions, node2);

                if (r1 != null) {
                    Segment s = new Segment(p, q);
                    s.neighborRegion = r2;
                    r1.addSegment(s);
                }
                if (r2 != null) {
                    Segment s = new Segment(p, q);
                    s.neighborRegion = r1;
                    r2.addSegment(s);
                }
            }
        }
    }

    public void resolve() {
        for (int i = 0; i < regions.size(); i++) {
            SOMRegion r = regions.get(i);
            r.resolve(min_visible_class);
        }

        // for (int i = 0; i < resolvedRegions.size(); i++) {
        // Pnt pnt = (Pnt)resolvedRegions.get(i);
        // resolvedDt.delaunayPlace(pnt);
        // }
        // build(resolvedRegions, resolvedDt);
    }

    /** Allows to reset the resolved state, for reusing the diagram. */
    public void resetResolvingState() {
        for (int i = 0; i < regions.size(); i++) {
            SOMRegion r = regions.get(i);
            r.resolved = false;
        }
    }

    /**
     * Goes through all the Regions and returns the Region with smallest Entropy Error. This is used in the End to
     * spread the Value among the whole Palette Interval (used for Visualization)
     * 
     * @return smallest Entropy
     */
    public SOMRegion getMaximumEntropyRegion() {
        SOMRegion reg;
        int counter = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < this.regions.size(); i++) {
            SOMRegion r = this.regions.get(i);
            double c1 = r.calculateEntropy();
            // System.out.println("Region Entropy  :"+c1);
            if (c1 > max) {
                max = c1;
                counter = i;
            }
        }
        // System.out.println("Max Region Entropy  :"+max);
        return this.regions.get(counter);
    }

    /**
     * Goes through all the Regions and returns the Region with biggest Entropy Error. This is used in the End to spread
     * the Value among the whole Palette Interval (used for Visualization)
     * 
     * @return biggest Entropy
     */
    public SOMRegion getMinimumEntropyRegion() {
        // SOMRegion reg;
        int counter = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < this.regions.size(); i++) {
            SOMRegion r = this.regions.get(i);
            double c1 = r.calculateEntropy();
            if (c1 < min) {
                min = c1;
                counter = i;
            }
        }
        return this.regions.get(counter);
    }

    public ArrayList<SOMRegion> getRegions() {
        return regions;
    }

}
