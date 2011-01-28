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
package at.tuwien.ifs.somtoolbox.apps.viewer.fileutils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

import at.tuwien.ifs.somtoolbox.apps.server.LabelDescription;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.EditLabelEventListener;
import at.tuwien.ifs.somtoolbox.util.LabelPNodeGenerator;

/**
 * Provides functionality to save all labels into a XML file.
 * 
 * @author Angela Roiger
 * @version $Id: LabelXmlUtils.java 3877 2010-11-02 15:43:17Z frank $
 */
public class LabelXmlUtils {

    private static EditLabelEventListener ll = new EditLabelEventListener();

    /**
     * Saves the labels(cluster labels and manual labels) of the map to the file.
     * 
     * @param map the MapPNode containing the labels
     * @param f the File to store the labels
     * @return returns true if saving to File was successful, false otherwise
     */
    public static boolean saveLabelsToFile(MapPNode map, File f) {
        Document doc;
        try {
            doc = createXmlDocument(map);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        Source src = new DOMSource(doc);
        Result res = new StreamResult(f);
        Transformer trans;
        try {
            trans = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            return false;
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
            return false;
        }
        try {
            trans.transform(src, res);
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void addLabelsToXml(Iterator<?> it, Node node, Document document) {
        while (it.hasNext()) {
            PNode element = (PNode) it.next();
            Element n = document.createElement("labelNode");
            node.appendChild(n);
            n.setAttribute("rotation", new Double(element.getRotation()).toString());
            n.setAttribute("xPos", new Double(element.getX()).toString());
            n.setAttribute("yPos", new Double(element.getY()).toString());
            n.setAttribute("xOffset", new Double(element.getXOffset()).toString());
            n.setAttribute("yOffset", new Double(element.getYOffset()).toString());
            // Node rot = n.appendChild(document.createAttribute("rotation"));
            // rot.setNodeValue();
            for (ListIterator<?> iterator = element.getChildrenIterator(); iterator.hasNext();) {
                PText text = (PText) iterator.next();
                Element textNode = document.createElement("labelText");
                n.appendChild(textNode);
                textNode.setAttribute("text", text.getText());
                textNode.setAttribute("xPos", new Double(text.getX()).toString());
                textNode.setAttribute("yPos", new Double(text.getY()).toString());
                textNode.setAttribute("xOffset", new Double(text.getXOffset()).toString());
                textNode.setAttribute("yOffset", new Double(text.getYOffset()).toString());
                textNode.setAttribute("fontSize", new Float(text.getFont().getSize2D()).toString());
                textNode.setAttribute("visible", Boolean.toString(text.getVisible()));

                // System.out.println(text.getTextPaint().toString());
                if (text.getTextPaint() instanceof Color) {
                    Color c = (Color) text.getTextPaint();
                    textNode.setAttribute("paint", Integer.toString(c.getRGB()));
                }
            }

        }
    }

    /**
     * Creates a XML Document containing all labels from the map
     */
    private static Document createXmlDocument(MapPNode map) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Node som = document.appendChild(document.createElement("som"));
        addLabelsToXml(map.getManualLabels().getChildrenIterator(), som, document);

        addLabelsToXml(map.getAllClusterLabels().iterator(), som, document);

        return document;
    }

    public static Document readXmlDocumentFromFile(File f) throws ParserConfigurationException, SAXException,
            IOException {
        Document doc;
        DocumentBuilder build = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = build.parse(f);
        return doc;
    }

    public static PNode restoreLabelsFromFile(File f) throws ParserConfigurationException, SAXException, IOException {
        Document doc = readXmlDocumentFromFile(f);
        Node som = doc.getFirstChild();
        NodeList nodes = som.getChildNodes();

        PNode allLabels = new PNode();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node container = nodes.item(i);
            NamedNodeMap attrib = container.getAttributes();
            Node x = attrib.getNamedItem("xOffset");
            double xOffset = Double.parseDouble(x.getNodeValue());
            Node y = attrib.getNamedItem("yOffset");
            double yOffset = Double.parseDouble(y.getNodeValue());
            Node r = attrib.getNamedItem("rotation");
            double rotation = Double.parseDouble(r.getNodeValue());

            PNode labelNode = LabelPNodeGenerator.newLabelNode(xOffset, yOffset, rotation);

            NodeList texts = container.getChildNodes();
            for (int j = 0; j < texts.getLength(); j++) {
                NamedNodeMap textAttrib = texts.item(j).getAttributes();
                Node fs = textAttrib.getNamedItem("fontSize");
                float fsText = Float.parseFloat(fs.getNodeValue());
                Node xt = textAttrib.getNamedItem("xOffset");
                double xText = Double.parseDouble(xt.getNodeValue());
                Node yt = textAttrib.getNamedItem("yOffset");
                double yText = Double.parseDouble(yt.getNodeValue());
                Node txt = textAttrib.getNamedItem("text");
                String text = txt.getNodeValue();

                // color
                PText t = LabelPNodeGenerator.newLabelText(text, fsText, xText, yText);
                t.setRotation(rotation);
                Node vis = textAttrib.getNamedItem("visible");
                t.setVisible(Boolean.valueOf(vis.getNodeValue()).booleanValue());

                Node color = textAttrib.getNamedItem("paint");
                if (color != null) {
                    t.setTextPaint(new Color(Integer.parseInt(color.getNodeValue())));
                }
                t.addInputEventListener(ll);
                labelNode.addChild(t);

            }
            allLabels.addChild(labelNode);
        }
        return allLabels;
    }

    /**
     * Reads the labels from the given file, and groups them regarding their font size into arrays.
     */
    public static ArrayList<LabelDescription>[] restoreLabelsByFontSizeLevel(File f)
            throws ParserConfigurationException, SAXException, IOException {
        Document doc = readXmlDocumentFromFile(f);
        Node som = doc.getFirstChild();
        NodeList nodes = som.getChildNodes();

        Hashtable<Float, ArrayList<LabelDescription>> allLabels = new Hashtable<Float, ArrayList<LabelDescription>>();
        TreeSet<Float> fontSizes = new TreeSet<Float>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node container = nodes.item(i);
            NamedNodeMap attrib = container.getAttributes();
            Node x = attrib.getNamedItem("xOffset");
            double xOffset = Double.parseDouble(x.getNodeValue());
            Node y = attrib.getNamedItem("yOffset");
            double yOffset = Double.parseDouble(y.getNodeValue());
            Node r = attrib.getNamedItem("rotation");
            double rotation = Double.parseDouble(r.getNodeValue());

            NodeList texts = container.getChildNodes();
            for (int j = 0; j < texts.getLength(); j++) {
                NamedNodeMap textAttrib = texts.item(j).getAttributes();
                Node fontSize = textAttrib.getNamedItem("fontSize");
                float fsText = Float.parseFloat(fontSize.getNodeValue());
                Node xt = textAttrib.getNamedItem("xOffset");
                double xText = Double.parseDouble(xt.getNodeValue());
                Node yt = textAttrib.getNamedItem("yOffset");
                double yText = Double.parseDouble(yt.getNodeValue());
                Node txt = textAttrib.getNamedItem("text");
                String text = txt.getNodeValue();
                Node vis = textAttrib.getNamedItem("visible");
                boolean visible = Boolean.parseBoolean(vis.getNodeValue());

                LabelDescription label = new LabelDescription(text, fsText, (int) (xText + xOffset),
                        (int) (yText + yOffset), rotation, visible);
                Node color = textAttrib.getNamedItem("paint");
                if (color != null) {
                    label.setColor(new Color(Integer.parseInt(color.getNodeValue())));
                }
                ArrayList<LabelDescription> l = allLabels.get(new Float(fsText));
                if (l == null) {
                    l = new ArrayList<LabelDescription>();
                }

                if (!l.contains(label)) {
                    l.add(label);
                }
                allLabels.put(new Float(fsText), l);
                fontSizes.add(new Float(fsText));
            }
        }
        @SuppressWarnings("unchecked")
        ArrayList<LabelDescription>[] res = new ArrayList[fontSizes.size()];
        Iterator<Float> iter = fontSizes.iterator();
        for (int i = fontSizes.size() - 1; iter.hasNext(); i--) {
            res[i] = allLabels.get(iter.next());
        }
        return res;
    }

}
