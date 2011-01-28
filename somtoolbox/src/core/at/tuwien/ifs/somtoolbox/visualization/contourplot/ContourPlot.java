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
package at.tuwien.ifs.somtoolbox.visualization.contourplot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

import cern.colt.matrix.DoubleMatrix2D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * Computes and draws the contours.
 * 
 * @version $Id: ContourPlot.java 3883 2010-11-02 17:13:23Z frank $
 */
public class ContourPlot {
    private static final long serialVersionUID = 1L;

    private final static int MIN_Y_STEPS = 2;

    private final static boolean SHOW_NUMBERS = true;

    private final static int PLOT_MARGIN = 0;

    private final static double Z_MAX_MAX = 1.0E+10, Z_MIN_MIN = -Z_MAX_MAX;

    private final static String EOL = System.getProperty("line.separator");

    private int xSteps, ySteps; // the grid steps

    private float z[][]; // the z values

    private boolean logInterpolation = false; // interpolation flag

    private int width, height; // size of the plot

    private double deltaX, deltaY; // the increments in the grid

    // Below, data members, most of which are adapted from Fortran variables in Snyder's code:
    private int numberOfContours = 6;

    private int l1[] = new int[4];

    private int l2[] = new int[4];

    private int ij[] = new int[2];

    private int i1[] = new int[2];

    private int i2[] = new int[2];

    private int i3[] = new int[6];

    private int ibkey, icur, jcur, ii, jj, elle, ix, iedge, iflag, ni, ks;

    private int cntrIndex, prevIndex;

    private int idir, nxidir, k;

    private double z1, z2, cval, zMax, zMin;

    private double intersect[] = new double[4];

    private double xy[] = new double[2];

    private double prevXY[] = new double[2];

    private float cv[] = new float[numberOfContours];

    private boolean jump;

    private Color[] palette = null;

    private boolean fill = true;

    private Stack<Profile> profiles = new Stack<Profile>();

    private static class Profile {
        int height;

        boolean boundary = false;

        Stack<Point2D> points = new Stack<Point2D>();

        Profile(int height) {
            this.height = height;
        }

        void push(double x, double y) {
            points.add(new Point2D.Double(x, y));
        }
    }

    public ContourPlot(int x, int y, int width, int height) {
        xSteps = x;
        ySteps = y;
        this.width = width - 2 * PLOT_MARGIN;
        this.height = height - 2 * PLOT_MARGIN;
    }

    private int sign(int a, int b) {
        a = Math.abs(a);
        if (b < 0) {
            return -a;
        } else {
            return a;
        }
    }

    /**
     * sets the first two components of the contour value array to equal values, thus preventing subsequent drawing of
     * the contour plot.
     */
    private void InvalidData() {
        cv[0] = (float) 0.0;
        cv[1] = (float) 0.0;
    }

    /** scans the data in {@link #z} in order to assign values to {@link #zMin} and {@link #zMax} */
    private void GetExtremes() throws SOMToolboxException {
        int i, j;
        double here;

        zMin = z[0][0];
        zMax = zMin;
        for (i = 0; i < xSteps; i++) {
            for (j = 0; j < ySteps; j++) {
                here = z[i][j];
                if (zMin > here) {
                    zMin = here;
                }
                if (zMax < here) {
                    zMax = here;
                }
            }
        }
        if (zMin == zMax) {
            InvalidData();
            throw new SOMToolboxException("Error parsing z values: " + EOL + "All z values are equal!");
        }
        return;
    }

    /**
     * interpolate between {@link #zMin} and {@link #zMax}, logarithmically or linearly, in order to assign contour
     * values to the array {@link #cv}
     */
    private void AssignContourValues() throws SOMToolboxException {
        int i;
        double delta;

        if (logInterpolation && zMin <= 0.0) {
            InvalidData();
            throw new SOMToolboxException("Logarithmic interpolation not possible because of nonpositive values.");
        }
        if (logInterpolation) {
            double temp = Math.log(zMin);

            delta = (Math.log(zMax) - temp) / numberOfContours;
            for (i = 0; i < numberOfContours; i++) {
                cv[i] = (float) Math.exp(temp + (i + 1) * delta);
            }
        } else {
            delta = (zMax - zMin) / numberOfContours;
            for (i = 0; i < numberOfContours; i++) {
                cv[i] = (float) (zMin + (i + 1) * delta);
            }
        }
    }

    /**
     * sets the colour of the graphics object, given the contour index, by interpolating linearly between
     * {@link Color#BLUE} & {@link Color#red}
     */
    private void SetColour(Graphics g) {
        Color c = new Color(((numberOfContours - cntrIndex) * Color.blue.getRed() + cntrIndex * Color.red.getRed())
                / numberOfContours, ((numberOfContours - cntrIndex) * Color.blue.getGreen() + cntrIndex
                * Color.red.getGreen())
                / numberOfContours, ((numberOfContours - cntrIndex) * Color.blue.getBlue() + cntrIndex
                * Color.red.getBlue())
                / numberOfContours);

        g.setColor(c);
    }

    private Color GetColour(int index) {
        if (palette == null) {
            int ind = numberOfContours - index;
            Color c = new Color((ind * Color.blue.getRed() + index * Color.red.getRed()) / numberOfContours, // red
                    (ind * Color.blue.getGreen() + index * Color.red.getGreen()) / numberOfContours,// green
                    (ind * Color.blue.getBlue() + index * Color.red.getBlue()) / numberOfContours); // blue

            return c;
        }
        double delta = index / (double) numberOfContours;
        int colorIdx = (int) (delta * palette.length);
        return palette[colorIdx];
    }

    private double ClipX(double x) {
        return Math.min(Math.max(PLOT_MARGIN, x), width - PLOT_MARGIN);
    }

    private double ClipY(double y) {
        return Math.min(Math.max(PLOT_MARGIN, y), height - PLOT_MARGIN);
    }

    private void DrawProfile(Graphics g, Profile profile) {
        int x, y;

        Polygon polygon = new Polygon();

        double dX = width / (xSteps - 1.0);
        double dY = height / (ySteps - 1.0);

        double sX = xSteps / (double) (xSteps - 2);
        double sY = ySteps / (double) (ySteps - 2);

        for (Point2D p : profile.points) {
            x = (int) ClipX(((p.getX() - 1.0) * dX - deltaX) * sX);
            y = (int) ClipY(((p.getY() - 1.0) * dY - deltaY) * sY);

            polygon.addPoint(x, y);
        }

        if (fill) {
            // FIXME: temporary solution for bug 225, see https://olymp.ifs.tuwien.ac.at/trac/somtoolbox/ticket/225
            // FIXME: find a better solution, by not having the last region empty...
            g.setColor(GetColour(Math.min(profile.height, profile.height + 1)));
            g.fillPolygon(polygon);
        }

        g.setColor(Color.BLACK);
        g.drawPolygon(polygon);
    }

    /**
     * the guts of drawing and is called directly or indirectly by {@link #ContourPlotKernel(Graphics, boolean[])} in
     * order to draw a segment of a contour or to set the pen position "prevXY". Its action depends on "iflag":
     * <ul>
     * <li>iflag == 1 means Continue a contour</li>
     * <li>iflag == 2 means Start a contour at a boundary</li>
     * <li>iflag == 3 means Start a contour not at a boundary</li>
     * <li>iflag == 4 means Finish contour at a boundary</li>
     * <li>iflag == 5 means Finish closed contour not at boundary</li>
     * <li>iflag == 6 means Set pen position</li>
     * </ul>
     * If the constant "SHOW_NUMBERS" is true then when completing a contour ("iflag" == 4 or 5) the contour index is
     * drawn adjacent to where the contour ends.
     */
    private void DrawKernel(Graphics g) {
        // int prevU, prevV, u, v;

        if (iflag == 2 || iflag == 3) {
            profiles.push(new Profile(cntrIndex));
        }

        if (profiles.size() > 0) {
            Profile profile = profiles.peek();

            if (iflag == 2 || iflag == 4) {
                profile.boundary = true;
            }

            profile.push(xy[0], xy[1]);
        }

        /*
         * if ((iflag == 1) || (iflag == 4) || (iflag == 5)) { if (cntrIndex != prevIndex) { // Must change colour SetColour(g); prevIndex =
         * cntrIndex; } prevU = (int) ((prevXY[0] - 1.0) * deltaX); prevV = (int) ((prevXY[1] - 1.0) * deltaY); u = (int) ((xy[0] - 1.0) * deltaX); v
         * = (int) ((xy[1] - 1.0) * deltaY); // Interchange horizontal & vertical g.drawLine(PLOT_MARGIN + prevV, PLOT_MARGIN + prevU, PLOT_MARGIN +
         * v, PLOT_MARGIN + u); if ((SHOW_NUMBERS) && ((iflag == 4) || (iflag == 5))) { if (u == 0) u = u - WEE_BIT; else if (u == d.width) u = u +
         * PLOT_MARGIN / 2; else if (v == 0) v = v - PLOT_MARGIN / 2; else if (v == d.height) v = v + WEE_BIT;
         * g.drawString(Integer.toString(cntrIndex), PLOT_MARGIN + v, PLOT_MARGIN + u); } }
         */
        prevXY[0] = xy[0];
        prevXY[1] = xy[1];
    }

    private void DetectBoundary() {
        ix = 1;
        if (ij[1 - elle] != 1) {
            ii = ij[0] - i1[1 - elle];
            jj = ij[1] - i1[elle];
            if (z[ii - 1][jj - 1] <= Z_MAX_MAX) {
                ii = ij[0] + i2[elle];
                jj = ij[1] + i2[1 - elle];
                if (z[ii - 1][jj - 1] < Z_MAX_MAX) {
                    ix = 0;
                }
            }
            if (ij[1 - elle] >= l1[1 - elle]) {
                ix = ix + 2;
                return;
            }
        }
        ii = ij[0] + i1[1 - elle];
        jj = ij[1] + i1[elle];
        if (z[ii - 1][jj - 1] > Z_MAX_MAX) {
            ix = ix + 2;
            return;
        }
        if (z[ij[0]][ij[1]] >= Z_MAX_MAX) {
            ix = ix + 2;
        }
    }

    /** corresponds to a block of code starting at label 20 in Synder's subroutine "GCONTR". */
    private boolean Routine_label_020() {
        l2[0] = ij[0];
        l2[1] = ij[1];
        l2[2] = -ij[0];
        l2[3] = -ij[1];
        idir = 0;
        nxidir = 1;
        k = 1;
        ij[0] = Math.abs(ij[0]);
        ij[1] = Math.abs(ij[1]);
        if (z[ij[0] - 1][ij[1] - 1] > Z_MAX_MAX) {
            elle = idir % 2;
            ij[elle] = sign(ij[elle], l1[k - 1]);
            return true;
        }
        elle = 0;
        return false;
    }

    /** corresponds to a block of code starting at label 50 in Synder's subroutine "GCONTR". */
    private boolean Routine_label_050() {
        while (true) {
            if (ij[elle] >= l1[elle]) {
                if (++elle <= 1) {
                    continue;
                }
                elle = idir % 2;
                ij[elle] = sign(ij[elle], l1[k - 1]);
                if (Routine_label_150()) {
                    return true;
                }
                continue;
            }
            ii = ij[0] + i1[elle];
            jj = ij[1] + i1[1 - elle];
            if (z[ii - 1][jj - 1] > Z_MAX_MAX) {
                if (++elle <= 1) {
                    continue;
                }
                elle = idir % 2;
                ij[elle] = sign(ij[elle], l1[k - 1]);
                if (Routine_label_150()) {
                    return true;
                }
                continue;
            }
            break;
        }
        jump = false;
        return false;
    }

    /** corresponds to a block of code starting at label 150 in Synder's subroutine "GCONTR". */
    private boolean Routine_label_150() {
        while (true) {
            // Lines from z[ij[0]-1][ij[1]-1]
            // to z[ij[0] ][ij[1]-1]
            // and z[ij[0]-1][ij[1]]
            // are not satisfactory. Continue the spiral.
            if (ij[elle] < l1[k - 1]) {
                ij[elle]++;
                if (ij[elle] > l2[k - 1]) {
                    l2[k - 1] = ij[elle];
                    idir = nxidir;
                    nxidir = idir + 1;
                    k = nxidir;
                    if (nxidir > 3) {
                        nxidir = 0;
                    }
                }
                ij[0] = Math.abs(ij[0]);
                ij[1] = Math.abs(ij[1]);
                if (z[ij[0] - 1][ij[1] - 1] > Z_MAX_MAX) {
                    elle = idir % 2;
                    ij[elle] = sign(ij[elle], l1[k - 1]);
                    continue;
                }
                elle = 0;
                return false;
            }
            if (idir != nxidir) {
                nxidir++;
                ij[elle] = l1[k - 1];
                k = nxidir;
                elle = 1 - elle;
                ij[elle] = l2[k - 1];
                if (nxidir > 3) {
                    nxidir = 0;
                }
                continue;
            }

            if (ibkey != 0) {
                return true;
            }
            ibkey = 1;
            ij[0] = icur;
            ij[1] = jcur;
            if (Routine_label_020()) {
                continue;
            }
            return false;
        }
    }

    /**
     * corresponds to a block of code starting at label 200 in Synder's subroutine "GCONTR". It has return values 0, 1
     * or 2.
     */
    private short Routine_label_200(Graphics g, boolean workSpace[]) {
        while (true) {
            xy[elle] = 1.0 * ij[elle] + intersect[iedge - 1];
            xy[1 - elle] = 1.0 * ij[1 - elle];
            workSpace[2 * (xSteps * (ySteps * cntrIndex + ij[1] - 1) + ij[0] - 1) + elle] = true;
            DrawKernel(g);
            if (iflag >= 4) {
                icur = ij[0];
                jcur = ij[1];
                return 1;
            }
            ContinueContour();
            if (!workSpace[2 * (xSteps * (ySteps * cntrIndex + ij[1] - 1) + ij[0] - 1) + elle]) {
                return 2;
            }
            iflag = 5; // 5. Finish a closed contour
            iedge = ks + 2;
            if (iedge > 4) {
                iedge = iedge - 4;
            }
            intersect[iedge - 1] = intersect[ks - 1];
        }
    }

    /**
     * true iff the current segment in the grid is crossed by one of the contour values and has not already been
     * processed for that value.
     */
    private boolean CrossedByContour(boolean workSpace[]) {
        ii = ij[0] + i1[elle];
        jj = ij[1] + i1[1 - elle];
        z1 = z[ij[0] - 1][ij[1] - 1];
        z2 = z[ii - 1][jj - 1];
        for (cntrIndex = 0; cntrIndex < numberOfContours; cntrIndex++) {
            int i = 2 * (xSteps * (ySteps * cntrIndex + ij[1] - 1) + ij[0] - 1) + elle;

            if (!workSpace[i]) {
                float x = cv[cntrIndex];
                if (x > Math.min(z1, z2) && x <= Math.max(z1, z2)) {
                    workSpace[i] = true;
                    return true;
                }
            }
        }
        return false;
    }

    /** continues tracing a contour. Edges are numbered clockwise, the bottom edge being # 1. */
    private void ContinueContour() {
        short local_k;

        ni = 1;
        if (iedge >= 3) {
            ij[0] = ij[0] - i3[iedge - 1];
            ij[1] = ij[1] - i3[iedge + 1];
        }
        for (local_k = 1; local_k < 5; local_k++) {
            if (local_k != iedge) {
                ii = ij[0] + i3[local_k - 1];
                jj = ij[1] + i3[local_k];
                z1 = z[ii - 1][jj - 1];
                ii = ij[0] + i3[local_k];
                jj = ij[1] + i3[local_k + 1];
                z2 = z[ii - 1][jj - 1];
                if (cval > Math.min(z1, z2) && cval <= Math.max(z1, z2)) {
                    if (local_k == 1 || local_k == 4) {
                        double zz = z2;

                        z2 = z1;
                        z1 = zz;
                    }
                    intersect[local_k - 1] = (cval - z1) / (z2 - z1);
                    ni++;
                    ks = local_k;
                }
            }
        }
        if (ni != 2) {
            // The contour crosses all 4 edges of cell being examined. Choose lines top-to-left & bottom-to-right if
            // interpolation point on top edge
            // is less than interpolation point on bottom edge.
            // Otherwise, choose the other pair. This method produces the same results if axes are reversed.
            // The contour may close at any edge, but must not cross itself inside any cell.
            ks = 5 - iedge;
            if (intersect[2] >= intersect[0]) {
                ks = 3 - iedge;
                if (ks <= 0) {
                    ks = ks + 4;
                }
            }
        }
        // Determine whether the contour will close or run into a boundary at edge ks of the current cell.
        elle = ks - 1;
        iflag = 1; // 1. Continue a contour
        jump = true;
        if (ks >= 3) {
            ij[0] = ij[0] + i3[ks - 1];
            ij[1] = ij[1] + i3[ks + 1];
            elle = ks - 3;
        }
    }

    /** corresponds to Synder's subroutine "GCONTR". */
    private void ContourPlotKernel(Graphics g, boolean workSpace[]) {
        short val_label_200;

        l1[0] = xSteps;
        l1[1] = ySteps;
        l1[2] = -1;
        l1[3] = -1;
        i1[0] = 1;
        i1[1] = 0;
        i2[0] = 1;
        i2[1] = -1;
        i3[0] = 1;
        i3[1] = 0;
        i3[2] = 0;
        i3[3] = 1;
        i3[4] = 1;
        i3[5] = 0;
        prevXY[0] = 0.0;
        prevXY[1] = 0.0;
        xy[0] = 1.0;
        xy[1] = 1.0;
        cntrIndex = 0;
        prevIndex = -1;
        iflag = 6;
        DrawKernel(g);
        icur = Math.max(1, Math.min((int) Math.floor(xy[0]), xSteps));
        jcur = Math.max(1, Math.min((int) Math.floor(xy[1]), ySteps));
        ibkey = 0;
        ij[0] = icur;
        ij[1] = jcur;
        if (Routine_label_020() && Routine_label_150()) {
            return;
        }
        if (Routine_label_050()) {
            return;
        }
        while (true) {
            DetectBoundary();
            if (jump) {
                if (ix != 0) {
                    iflag = 4; // Finish contour at boundary
                }
                iedge = ks + 2;
                if (iedge > 4) {
                    iedge = iedge - 4;
                }
                intersect[iedge - 1] = intersect[ks - 1];
                val_label_200 = Routine_label_200(g, workSpace);
                if (val_label_200 == 1) {
                    if (Routine_label_020() && Routine_label_150()) {
                        return;
                    }
                    if (Routine_label_050()) {
                        return;
                    }
                    continue;
                }
                if (val_label_200 == 2) {
                    continue;
                }
                return;
            }
            if (ix != 3 && ix + ibkey != 0 && CrossedByContour(workSpace)) {
                // An acceptable line segment has been found. Follow contour until it hits a boundary or closes.
                iedge = elle + 1;
                cval = cv[cntrIndex];
                if (ix != 1) {
                    iedge = iedge + 2;
                }
                iflag = 2 + ibkey;
                intersect[iedge - 1] = (cval - z1) / (z2 - z1);
                val_label_200 = Routine_label_200(g, workSpace);
                if (val_label_200 == 1) {
                    if (Routine_label_020() && Routine_label_150()) {
                        return;
                    }
                    if (Routine_label_050()) {
                        return;
                    }
                    continue;
                }
                if (val_label_200 == 2) {
                    continue;
                }
                return;
            }
            if (++elle > 1) {
                elle = idir % 2;
                ij[elle] = sign(ij[elle], l1[k - 1]);
                if (Routine_label_150()) {
                    return;
                }
            }
            if (Routine_label_050()) {
                return;
            }
        }
    }

    /** draws the contours provided that the first two contour values are not equal (which would indicate invalid data) */
    public void paint(Graphics g) {
        int workLength = 2 * xSteps * ySteps * numberOfContours;
        boolean workSpace[]; // used to remember which segments in the grid have been crossed by which contours.

        deltaX = width / (xSteps - 1.0);
        deltaY = height / (ySteps - 1.0);

        if (cv[0] != cv[1]) { // Valid data
            workSpace = new boolean[workLength];
            profiles = new Stack<Profile>();
            ContourPlotKernel(g, workSpace);

            Comparator<Profile> comparator = new Comparator<Profile>() {
                @Override
                public int compare(Profile line1, Profile line2) {
                    if (line1.height < line2.height) {
                        return -1;
                    } else if (line1.height > line2.height) {
                        return 1;
                    }

                    return 0;
                }
            };

            Collections.sort(profiles, comparator);

            if (fill) {
                g.setColor(GetColour(0));
                g.fillRect(PLOT_MARGIN, PLOT_MARGIN, width, height);
            }

            for (Profile profile : profiles) {
                DrawProfile(g, profile);
            }
        }
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }

    public void setPalette(Color[] palette) {
        this.palette = palette;
    }

    public void setZedMatrix(DoubleMatrix2D zed) throws SOMToolboxException {
        z = new float[zed.columns() + 2][zed.rows() + 2];

        for (int x = 0; x < zed.columns(); x++) {
            for (int y = 0; y < zed.rows(); y++) {
                z[x + 1][y + 1] = (float) zed.get(y, x);
            }
        }

        MakeMatrixRectangular();
        GetExtremes();
        if (zMax > Z_MAX_MAX) {
            zMax = Z_MAX_MAX;
        }
        if (zMin < Z_MIN_MIN) {
            zMin = Z_MIN_MIN;
        }
        AssignContourValues();
    }

    public void setNumberOfContours(int numberOfContours) {
        this.numberOfContours = numberOfContours;
        cv = new float[numberOfContours];
    }

    public void setLogInterpolation(boolean logInterpolation) {
        this.logInterpolation = logInterpolation;
    }

    /** appends zero(s) to the end of any row of "z" which is shorter than the longest row. */
    private void MakeMatrixRectangular() {
        int i, y, leng;

        xSteps = z.length;
        ySteps = MIN_Y_STEPS;
        for (i = 0; i < xSteps; i++) {
            y = z[i].length;
            if (ySteps < y) {
                ySteps = y;
            }
        }

        for (i = 0; i < xSteps; i++) {
            leng = z[i].length;
            if (leng < ySteps) {
                float temp[] = new float[ySteps];

                System.arraycopy(z[i], 0, temp, 0, leng);
                while (leng < ySteps) {
                    temp[leng++] = 0;
                }
                z[i] = temp;
            }
        }
    }
}
