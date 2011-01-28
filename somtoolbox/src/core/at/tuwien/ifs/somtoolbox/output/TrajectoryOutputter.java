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
package at.tuwien.ifs.somtoolbox.output;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Generates a graphical representation of a trajectory of the given points over the map.
 * 
 * @author Michael Dittenbach
 * @version $Id: TrajectoryOutputter.java 3688 2010-07-15 09:17:46Z frank $
 */
public class TrajectoryOutputter implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptHighlightedDataNamesFile(true),
            OptionFactory.getOptUnitDescriptionFile(true), OptionFactory.getOptMapDescriptionFile(false),
            OptionFactory.getSwitchDrawLines(), OptionFactory.getOptImageFileName() };

    public static final String DESCRIPTION = "Generates a graphical representation of a trajectory of the given points over the map";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Utils;

    private static final int unitWidth = 100;

    private static final int unitHeight = 130;

    public static void main(String[] args) {
        // -d dataNamesFilename, mand.
        // -u unitDescriptionFileName, mand.
        // -m mapDescriptionFileName, opt.
        // -l drawLines, switch
        // imageName

        // register and parse all options
        JSAPResult config = AbstractOptionFactory.parseResults(args, OPTIONS);

        String dataNamesFileName = config.getString("dataNamesFile");
        String unitDescriptionFileName = config.getString("unitDescriptionFile");
        String mapDescriptionFileName = config.getString("mapDescriptionFile", null);
        String imageFileName = config.getString("imageFile");
        boolean drawLines = config.getBoolean("drawLines");

        String fDir = imageFileName.substring(0, imageFileName.lastIndexOf(System.getProperty("file.separator")) + 1);
        String fName = imageFileName.substring(imageFileName.lastIndexOf(System.getProperty("file.separator")) + 1);
        if (fName.endsWith(".png") || fName.endsWith(".eps")) {
            fName = fName.substring(0, (fName.length() - 4));
        }

        GrowingSOM gsom = null;
        try {
            gsom = new GrowingSOM(new SOMLibFormatInputReader(null, unitDescriptionFileName, mapDescriptionFileName));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }

        String[] dataNames = readDataNames(dataNamesFileName);

        /*
         * animation code for (int i=0; i<dataNames.length;i++) { String[] dnAni = new String[i+1]; for (int j=0;j<(i+1);j++) { dnAni[j] =
         * dataNames[j]; } try { write(gsom, fDir, fName+""+i, dnAni, drawLines); } catch (IOException e) {
         * Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("asf"); // TODO: change string System.exit(-1); } }
         */
        try {
            write(gsom, fDir, fName, dataNames, drawLines);
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("IOExeption while writing: " + e.getMessage()); // TODO:
            // change
            // string
            System.exit(-1);
        }

    }

    /*
     * public static void write(GHSOM ghsom, String fDir, String fName, String[] dataNames) throws IOException { write(ghsom.topLayerMap(), fDir,
     * fName, dataNames); }
     */

    public static void write(GrowingSOM gsom, String fDir, String fName, String[] dataNames, boolean drawLines)
            throws IOException {
        int imageWidth = gsom.getLayer().getXSize() * unitWidth + 5;
        int imageHeight = gsom.getLayer().getYSize() * unitHeight + 5;

        /** ** png output *** */

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        createGraphics(g, imageWidth, imageHeight, gsom, dataNames, drawLines);
        ImageIO.write(image, "png", new File(fDir + fName + ".png"));

        /** ** eps output *** */

        try {
            @SuppressWarnings("unchecked")
            Class<Graphics2D> c = (Class<Graphics2D>) Class.forName("org.sourceforge.jlibeps.epsgraphics.EpsGraphics2D");

            Constructor<Graphics2D> constr = c.getConstructor(String.class, OutputStream.class, int.class, int.class,
                    int.class, int.class);
            FileOutputStream epsOut = new FileOutputStream(fDir + fName + ".eps");

            // EpsGraphics2D geps = new EpsGraphics2D(fName, epsOut, 0, 0, imageWidth, imageHeight);
            Graphics2D geps = constr.newInstance(new Object[] { fName, epsOut, 0, 0, imageWidth, imageHeight });
            createGraphics(geps, imageWidth, imageHeight, gsom, dataNames, drawLines);

            // geps.flush();
            c.getMethod("flush", new Class<?>[] {}).invoke(geps, new Object[] {});
            // geps.close();
            c.getMethod("close", new Class<?>[] {}).invoke(geps, new Object[] {});
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "EPS-Output not possible. To enable add \"jlibeps.jar\" to your classpath");
        }
    }

    private static int[][] getNrDataNames(GrowingSOM gsom, String[] dataNames) {
        int[][] res = new int[gsom.getLayer().getXSize()][gsom.getLayer().getYSize()];
        try {
            for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
                for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                    res[i][j] = 0;
                    for (String dataName : dataNames) {
                        for (int mi = 0; mi < gsom.getLayer().getUnit(i, j).getNumberOfMappedInputs(); mi++) {
                            if (dataName.equals(gsom.getLayer().getUnit(i, j).getMappedInputNames()[mi])) {
                                res[i][j]++;
                            }
                        }
                    }
                }
            }
        } catch (LayerAccessException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            System.exit(-1);
        }
        return res;
    }

    private static int getMaxNrDataNames(int[][] nrDataNames, int x, int y) {
        int res = 0;
        for (int j = 0; j < y; j++) {
            for (int i = 0; i < x; i++) {
                if (nrDataNames[i][j] > res) {
                    res = nrDataNames[i][j];
                }
            }
        }
        return res;
    }

    private static void createGraphics(Graphics2D g, int imageWidth, int imageHeight, GrowingSOM gsom,
            String[] dataNames, boolean drawLines) {
        g.setColor(Color.white);
        g.fillRect(0, 0, imageWidth, imageHeight);
        g.setColor(Color.black);
        // frame
        g.setStroke(new BasicStroke(4));
        g.draw(new Rectangle2D.Double(1, 1, imageWidth - 2, imageHeight - 2));

        int[][] nrDataNames = getNrDataNames(gsom, dataNames);
        int maxDataNames = getMaxNrDataNames(nrDataNames, gsom.getLayer().getXSize(), gsom.getLayer().getYSize());
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                Unit u = null;
                try {
                    u = gsom.getLayer().getUnit(i, j);
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                    System.exit(-1);
                }
                g.setStroke(new BasicStroke(1));
                g.setColor(Color.lightGray);
                int luCornerX = i * unitWidth + 1;
                int luCornerY = j * unitHeight + 1;

                /** ** unit rectangle begin *** */

                if (nrDataNames[i][j] > 0) {
                    int minGreen = 133;
                    int maxGreen = 211;
                    int minBlue = 0;
                    int maxBlue = 163;
                    int green = 0;
                    int blue = 0;
                    if (maxDataNames == 1) {
                        green = minGreen;
                        blue = minBlue;
                    } else {
                        green = (maxGreen - minGreen) / (maxDataNames - 1) * (maxDataNames - nrDataNames[i][j])
                                + minGreen;
                        blue = (maxBlue - minBlue) / (maxDataNames - 1) * (maxDataNames - nrDataNames[i][j]) + minBlue;
                    }
                    // System.out.println("Unit "+i+"/"+j+": 255,"+green+","+blue);
                    Color bg = new Color(255, green, blue);
                    g.setPaint(bg);
                    g.fill(new Rectangle2D.Double((i * unitWidth + 3), (j * unitHeight + 3), unitWidth - 1,
                            unitHeight - 1));
                } else {
                    // g.setPaint(Color.white);
                    // System.out.println("x1:"+((i*unitWidth)+1)+", y1:"+((j*unitHeight)+1)+", x2:"+((i*unitWidth)+unitWidth)+",
                    // y2:"+((j*unitHeight)+unitHeight));
                    g.draw(new Rectangle2D.Double((i * unitWidth + 3), (j * unitHeight + 3), unitWidth - 1,
                            unitHeight - 1));
                    // System.out.println("Unit "+i+"/"+j+": no color");
                }
                /** ** unit rectangle end *** */

                /** ** labels begin *** */
                int textPaddingX = 3;
                int textPaddingY = 2;
                int fontSize = 8;
                int textPosY = luCornerY + textPaddingY + fontSize;
                if (u.getLabels() != null) {
                    g.setFont(new Font("Sans", Font.PLAIN, fontSize));
                    g.setColor(Color.black); // TODO: color according to qe?
                    for (int l = 0; l < u.getLabels().length; l++) {
                        g.drawString(u.getLabels()[l].getName(), (luCornerX + textPaddingX), textPosY);
                        textPosY += fontSize + textPaddingY;
                    }
                }
                /** ** labels end *** */
                /** ** data *** */
                boolean separator = true;
                if (u.getNumberOfMappedInputs() > 0) {
                    g.setFont(new Font("Sans", Font.BOLD, fontSize));
                    g.setColor(Color.decode("#4c9b21")); // dark green
                    for (String dataName : dataNames) {
                        boolean found = false;
                        int mi = 0;
                        while (mi < u.getNumberOfMappedInputs() && !found) {
                            if (dataName.equals(u.getMappedInputNames()[mi])) {
                                found = true;
                                if (separator) {
                                    Color oldCol = g.getColor();
                                    g.setColor(Color.black);
                                    g.draw(new Line2D.Double((luCornerX + textPaddingX), textPosY - 6, (luCornerX
                                            + textPaddingX + unitWidth - textPaddingX), textPosY - 6));
                                    g.setColor(oldCol);
                                    textPosY += 4;
                                    separator = false;
                                }

                                g.drawString(dataName, (luCornerX + textPaddingX), textPosY);
                                textPosY += fontSize + 0; // textPaddingY;
                            }
                            mi++;
                        }
                    }
                }
                /** ** data *** */
            }
        }
        int x = 0;
        int y = 0;
        if (drawLines == true) {
            GeneralPath trajectory = new GeneralPath(Path2D.WIND_EVEN_ODD, dataNames.length);
            for (int d = 0; d < dataNames.length; d++) {
                Unit u = gsom.getLayer().getUnitForDatum(dataNames[d]);
                if (u == null) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "Datum " + dataNames[d] + " not found on map. Aborting.");
                    System.exit(-1); // TODO: exception or ignore
                }
                x = u.getXPos() * unitWidth + unitWidth / 2;
                y = u.getYPos() * unitHeight + unitHeight / 2;
                if (d == 0) {
                    trajectory.moveTo(x, y);
                    /* starting point begin */
                    g.setColor(Color.black);
                    g.setStroke(new BasicStroke(2));
                    g.draw(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
                    /* starting point end */
                } else {
                    trajectory.lineTo(x, y);
                    // int oldX = (gsom.getLayer().getUnitForDatum(dataNames[d - 1]).getXPos() * unitWidth) + (unitWidth
                    // / 2);
                    // int oldY = (gsom.getLayer().getUnitForDatum(dataNames[d - 1]).getYPos() * unitHeight) +
                    // (unitHeight / 2);
                }
            }
            g.setColor(Color.red);
            g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(trajectory);

            /* end point begin */
            g.setColor(Color.black);
            g.setStroke(new BasicStroke(2));
            g.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
            /* end point end */
        }
    }

    public static String[] readDataNames(String fName) {
        ArrayList<String> tmpList = new ArrayList<String>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fName));
            String line = null;
            while ((line = br.readLine()) != null) {
                StringTokenizer strtok = new StringTokenizer(line, " \t", false);
                while (strtok.hasMoreTokens()) {
                    tmpList.add(strtok.nextToken());
                }
            }
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not open or read from file " + fName + " containing the data names. Aborting.");
            System.exit(-1);
        }
        if (tmpList.isEmpty()) {
            return null;
        } else {
            String[] res = new String[tmpList.size()];
            for (int i = 0; i < tmpList.size(); i++) {
                res[i] = tmpList.get(i);
            }
            return res;
        }
    }

}
