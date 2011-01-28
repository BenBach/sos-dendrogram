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
package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.jar.JarFile;

import org.jdom.Comment;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import at.tuwien.ifs.commons.util.MathUtils;
import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class represents a palette for the visualization.
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: Palette.java 3888 2010-11-02 17:42:53Z frank $
 */
public class Palette implements Comparable<Palette> {
    private String name;

    private String description;

    private String paletteGroup = "";

    private Color[] colors;

    private ColorGradient gradient;

    private int numberOfGradientColours;

    private String shortName;

    private boolean hidden = false;

    private boolean reversed = false;

    public Palette() {
        this("", "", "", new Color[] { Color.WHITE, Color.BLACK });
    }

    public Palette(String name, String shortName, String description, ColorGradient gradient,
            int numberOfGradientColours) {
        this(name, shortName, description, gradient.toPalette(numberOfGradientColours));
        this.gradient = gradient;
        this.numberOfGradientColours = numberOfGradientColours;
    }

    public Palette(String name, String shortName, String description, Color[] colors) {
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.colors = colors;
        this.paletteGroup = "";
    }

    public String getPaletteGroup() {
        return paletteGroup;
    }

    public void setPaletteGroup(String paletteGroup) {
        this.paletteGroup = paletteGroup;
    }

    public void deleteColor(int pos) {
        if (gradient != null) {
            gradient.deleteGradientPoing(pos);
            colors = gradient.toPalette(numberOfGradientColours);
        } else {
            if (colors.length < 3) {
                return;
            }
            Color[] c2 = new Color[colors.length - 1];
            for (int i = 0, j = 0; i < c2.length && j < colors.length; i++, j++) {
                if (i == pos) {
                    j++;
                }
                c2[i] = colors[j];
            }
            colors = c2;
        }
    }

    public void insertColor(int pos, Color c) {
        if (gradient != null) {
            gradient.insertGradientPoint(pos + 1, c);
            colors = gradient.toPalette(numberOfGradientColours);
        } else {
            Color[] c2 = new Color[colors.length + 1];
            for (int i = 0, j = 0; i < c2.length && j < colors.length; i++, j++) {
                c2[i] = colors[j];
                if (i == pos) {
                    i++;
                    c2[i] = c;
                }
            }
            colors = c2;
        }
    }

    public Color[] getColors() {
        return colors;
    }

    public Color getColor(int index) {
        return colors[index];
    }

    public Color getColorConstrained(int index) {
        return colors[MathUtils.constrainWithin(index, 0, maxColourIndex())];
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    /**
     * Compares palette objects by name.
     */
    @Override
    public int compareTo(Palette other) {
        return getName().compareTo(other.getName());
    }

    @Override
    public String toString() {
        return getName() + (reversed ? " (reversed)" : "");
    }

    public String getCacheKey() {
        return getShortName() + (reversed ? " (reversed)" : "");
    }

    public String getLongDescription() {
        return getName() + "-" + getDescription() + "[" + colors.length + " colours]";
    }

    public int getNumberOfGradientColours() {
        return numberOfGradientColours;
    }

    public ColorGradient getGradient() {
        return gradient;
    }

    public void setColor(int index, Color c) {
        if (gradient != null) {
            gradient.setGradientPoint(index, -1, c);
        } else {
            if (c != null) {
                colors[index] = c;
            }
        }
    }

    public void setGradient(ColorGradient gradient) {
        setGradient(gradient, numberOfGradientColours);
    }

    private void setGradient(ColorGradient gradient, int numberOfGradientColours) {
        this.gradient = gradient;
        this.numberOfGradientColours = numberOfGradientColours;
        this.colors = gradient.toPalette(numberOfGradientColours);
    }

    /**
     * Sets the number of gradient colours to be used, and generates the palette colours ({@link #colors}) new.
     */
    public void setNumberOfGradientColours(int numberOfGradientColours) {
        this.numberOfGradientColours = numberOfGradientColours;
        if (gradient != null) {
            this.colors = gradient.toPalette(numberOfGradientColours);
        }
    }

    public static Palette loadPaletteFromXML(InputStream in) throws JDOMException, IOException {
        Document doc;
        SAXBuilder b = new SAXBuilder(true);
        doc = b.build(in, Palette.class.getResource("/rsc/palettes/somtoolboxPalette.dtd").toExternalForm());
        return loadPaletteFromXML(doc);
    }

    public static Palette loadPaletteFromXML(File file) throws SOMToolboxException {
        try {
            return loadPaletteFromXML(new FileInputStream(file));
        } catch (Exception e) {
            throw new SOMToolboxException("Could not load \"" + file.getName() + "\"", e);
        }
    }

    public static Palette loadPaletteFromXML(JarFile jarFile, String name) throws SOMToolboxException {
        try {
            return loadPaletteFromXML(jarFile.getInputStream(jarFile.getEntry(name)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new SOMToolboxException("Could not load \"" + name + "\" (not a valid palette)");
    }

    private static Palette loadPaletteFromXML(Document doc) throws JDOMException {
        Element root = doc.getRootElement();
        String version = root.getAttributeValue("version");
        if (version == null || version.equalsIgnoreCase("1.0")) {
            return loadPaletteFromXML_v1x0(root);
        }
        throw new JDOMException("Unknown Palette-Version");
    }

    @SuppressWarnings("rawtypes")
    private static Palette loadPaletteFromXML_v1x0(Element root) {

        String shortName, name, desc, group;
        shortName = root.getChildTextNormalize("shortName");
        name = root.getChildTextNormalize("longName");
        desc = root.getChildTextNormalize("description");
        group = root.getAttributeValue("group");

        int colorCount = -1;
        Element colors = root.getChild("colors");
        try {
            colorCount = Integer.parseInt(colors.getAttributeValue("colorCount"));
        } catch (NumberFormatException e) {
            colorCount = -1;
        }
        boolean isRealRepresentation = StringUtils.equals(colors.getAttributeValue("representation"), "real");

        List colorList = colors.getChildren();
        int c = colorList.size();
        boolean useGradientPoints = true;
        double[] gradientPoints = new double[c];
        Color[] gradientColors = new Color[c];
        for (int i = 0; i < c; i++) {
            Element color = (Element) colorList.get(i);
            try {
                String gPoint = color.getChildTextNormalize("gradientPoint");
                if (gPoint != null) {
                    gradientPoints[i] = Double.parseDouble(gPoint);
                } else {
                    useGradientPoints = false;
                }
            } catch (NumberFormatException e) {
                useGradientPoints = false;
            }

            if (isRealRepresentation) {
                int r = (int) Math.round(255 * Double.parseDouble(color.getChildTextNormalize("red")));
                int g = (int) Math.round(255 * Double.parseDouble(color.getChildTextNormalize("green")));
                int b = (int) Math.round(255 * Double.parseDouble(color.getChildTextNormalize("blue")));
                gradientColors[i] = new Color(r, g, b);
            } else {
                int r = Integer.parseInt(color.getChildTextNormalize("red"));
                int g = Integer.parseInt(color.getChildTextNormalize("green"));
                int b = Integer.parseInt(color.getChildTextNormalize("blue"));
                gradientColors[i] = new Color(r, g, b);
            }
        }

        Palette res = null;
        if (colorCount > 0) {
            if (!useGradientPoints) {
                for (int i = 0; i < gradientColors.length; i++) {
                    gradientPoints[i] = (double) i / (double) (gradientColors.length - 1);
                }
            }
            try {
                ColorGradient gradient = new ColorGradient(gradientPoints, gradientColors);
                res = new Palette(name, shortName, desc, gradient, colorCount);
            } catch (SOMToolboxException e) {
                // This does not happen!
                e.printStackTrace();
            }
        } else {
            res = new Palette(name, shortName, desc, gradientColors);
        }

        if (group != null) {
            res.setPaletteGroup(group);
        }
        res.setHidden(Boolean.parseBoolean(root.getAttributeValue("hidden")));

        return res;
    }

    /**
     * Save the Palette as Matlab code to the given file.
     * 
     * @param file The file to save to or <c>null</c> to write to <c>System.out</c>
     */
    public void savePaletteToMatlab(File file) {
        try {
            PrintStream printStream = System.out;
            if (file != null) {
                if (file.exists()) {
                    String p = file.getAbsolutePath();
                    String newName = file.getName() + new SimpleDateFormat("yyyyMMdd_hhmm_ss").format(new Date());
                    file.renameTo(new File(file.getParentFile(), newName));
                    file = new File(p);
                }
                printStream = new PrintStream(new FileOutputStream(file));
            }
            printStream.println("% Colormap for " + getName() + " (" + getDescription() + ")");
            Color[] colors = getColors();
            for (Color element : colors) {
                printStream.println((double) element.getRed() / 255 + " " + (double) element.getGreen() / 255 + " "
                        + (double) element.getBlue() / 255);
            }
            if (file != null) {
                printStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the Palette as Javacode to the given file.
     * 
     * @param file The file to save to or <c>null</c> to write to <c>System.out</c>
     */
    public void savePaletteAsJavaCode(File file) {
        PrintStream printStream = System.out;
        if (file != null) {
            try {
                printStream = new PrintStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        ColorGradient g = getGradient();
        printStream.println();
        printStream.println("// Java Code for Palette " + getName() + " (" + getDescription() + ")"
                + ", generated by Palette Editor");

        String paletteVarName = StringUtils.escapeString(getName()).toLowerCase();

        if (g != null) {
            String pointsVarName = paletteVarName + "_points";
            printStream.print("double[] " + pointsVarName + " = new double[] { ");
            for (int i = 0; i < g.getNumberOfPoints(); i++) {
                printStream.print(g.getGradientPoint(i));
                if (i + 1 < g.getNumberOfPoints()) {
                    printStream.print(", ");
                }
            }
            printStream.println(" };");

            String colorsVarName = paletteVarName + "_colors";
            printStream.print("Color[] " + colorsVarName + " = new Color[] {");
            for (int i = 0; i < g.getNumberOfPoints(); i++) {
                printStream.print("new Color(" + g.getGradientColor(i).getRed() + ", "
                        + g.getGradientColor(i).getGreen() + ", " + g.getGradientColor(i).getBlue() + ")");
                if (i + 1 < g.getNumberOfPoints()) {
                    printStream.print(", ");
                }
            }
            printStream.println(" };");
            printStream.println("try {");
            printStream.println("    ColorGradient gradient = new ColorGradient(" + pointsVarName + ", "
                    + colorsVarName + ");");
            printStream.println("    Palette " + paletteVarName + " = new Palette(\"" + getName() + "\", \""
                    + getShortName() + "\", \"" + getDescription() + "\", gradient, " + getNumberOfGradientColours()
                    + "); ");
            printStream.println("    " + paletteVarName + ".setHidden(" + (isHidden() ? "true" : "false") + ");");
            printStream.println("    paletteList.add(" + paletteVarName + ");");
            printStream.println("} catch (SOMToolboxException e) {");
            printStream.println("    // TODO Auto-generated catch block");
            printStream.println("    e.printStackTrace();");
            printStream.println("}");
        } else {

            String colorsVarName = paletteVarName + "_colors";
            printStream.print("Color[] " + colorsVarName + " = new Color[] {");
            Color[] cls = getColors();
            for (int i = 0; i < cls.length; i++) {
                printStream.print("new Color(" + cls[i].getRed() + ", " + cls[i].getGreen() + ", " + cls[i].getBlue()
                        + ")");
                if (i + 1 < cls.length) {
                    printStream.print(", ");
                }
            }
            printStream.println(" };");

            printStream.println("Palette " + paletteVarName + " = new Palette(\"" + getName() + "\", \""
                    + getShortName() + "\", \"" + getDescription() + "\", " + colorsVarName + "); ");
            printStream.println("paletteList.add(" + paletteVarName + ");");
        }
        printStream.println("// End generated Code");
        printStream.println();

        if (file != null) {
            printStream.close();
        }
    }

    public void savePaletteToXML(File file) {
        Document doc = new Document();
        doc.setDocType(new DocType("palette", "somtoolboxPalette.dtd"));
        Comment comment = new Comment(" Color-Palette for SOMViewer ");
        doc.addContent(comment);

        doc.setRootElement(getPaletteAsXML());

        try {
            OutputStream os = new FileOutputStream(file);
            XMLOutputter out = new XMLOutputter();
            out.setFormat(Format.getPrettyFormat());
            out.output(doc, os);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The root element <code>&lt;palette&gt;...&lt;/palette&gt;</code>
     */
    public Element getPaletteAsXML() {
        Element root = new Element("palette");
        root.setAttribute("version", "1.0");
        root.setAttribute("hidden", isHidden() ? "true" : "false");
        if (this.getPaletteGroup().length() > 0) {
            root.setAttribute("group", this.getPaletteGroup());
        }

        Element shortName = new Element("shortName");
        shortName.setText(this.getShortName());
        root.addContent(shortName);
        Element longName = new Element("longName");
        longName.setText(this.getName());
        root.addContent(longName);
        Element desc = new Element("description");
        desc.setText(this.getDescription());
        root.addContent(desc);

        // The Colors
        Element colorsElement = new Element("colors");
        if (this.getNumberOfGradientColours() > 0) {
            colorsElement.setAttribute("colorCount", this.getNumberOfGradientColours() + "");
        }

        ColorGradient cg = this.getGradient();
        if (cg != null) {
            for (int i = 0; i < cg.getNumberOfPoints(); i++) {
                Element color = new Element("color");
                // Point
                color.addContent(new Element("gradientPoint").setText(cg.getGradientPoint(i) + ""));
                // Color
                Color c = cg.getGradientColor(i);
                color.addContent(new Element("red").setText(c.getRed() + ""));
                color.addContent(new Element("green").setText(c.getGreen() + ""));
                color.addContent(new Element("blue").setText(c.getBlue() + ""));

                colorsElement.addContent(color);
            }
        } else {
            Color[] colors = this.getColors();
            for (Color c : colors) {
                Element color = new Element("color");
                color.addContent(new Element("red").setText(c.getRed() + ""));
                color.addContent(new Element("green").setText(c.getGreen() + ""));
                color.addContent(new Element("blue").setText(c.getBlue() + ""));

                colorsElement.addContent(color);
            }
        }

        root.addContent(colorsElement);
        return root;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void reverse() {
        this.reversed = !this.reversed;
        colors = reverseColors();
    }

    public boolean isReversed() {
        return reversed;
    }

    public Color[] reverseColors() {
        return Palettes.reversePalette(this);
    }

    public int getNumberOfColours() {
        return colors.length;
    }

    public double maxColourIndex() {
        return colors.length - 1;
    }

    /**
     * Returns true if the palette should not be shown in the Palettes menu.
     * 
     * @return <code>true</code> iff the palette is secret.
     */
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

}
