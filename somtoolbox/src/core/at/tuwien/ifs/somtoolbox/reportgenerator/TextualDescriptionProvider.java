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
package at.tuwien.ifs.somtoolbox.reportgenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class TextualDescriptionProvider {
    private static HashMap<String, String> scientificDescriptions = new HashMap<String, String>();

    private static HashMap<String, String> texts = new HashMap<String, String>();

    static {
        readScientificDescriptionTXTFile();
        readTexts();
    }

    /**
     * Reads the Textfile containing all descriptions.
     * 
     * @return false if an error occured, true otherwise
     */
    private static boolean readScientificDescriptionTXTFile() {
        try {
            Document doc = new SAXBuilder().build("./src/core/rsc/reportGenerator/scientificDescriptions.xml");
            Element root = doc.getRootElement();
            @SuppressWarnings("unchecked")
            final List<Element> children = root.getChildren();
            for (Element child : children) {
                // String shortName = child.getAttributeValue("shortName");
                String name = child.getAttributeValue("longName");
                String description = child.getTextNormalize();
                scientificDescriptions.put(name, description);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return scientificDescriptions.size() != 0;
    }

    /** gets the scientific Description of the Method with ID, or null otherwise */
    public static String getScientificDescription(String ID) {
        String out = scientificDescriptions.get(ID);
        if (out == null) {
            out = "Currently there is no description available for this Method, or no Method Description File has been added to the Report.";
        }
        return out;
    }

    public static String getText(String id) {
        String out = texts.get(id);
        if (out == null) {
            out = "Currently there is no description available for this Method, or no Method Description File has been added to the Report.";
        }
        return out;
    }

    /**
     * Reads the Textfile containing all descriptions.
     * 
     * @return false if an error occured, true otherwise
     */
    private static boolean readTexts() {
        try {
            Document doc = new SAXBuilder().build("./src/core/rsc/reportGenerator/texts.xml");
            Element root = doc.getRootElement();
            @SuppressWarnings("unchecked")
            final List<Element> children = root.getChildren();
            for (Element child : children) {
                String key = child.getAttributeValue("name");
                String value = child.getText();
                String s = "__scientific_description_";
                while (value.contains(s)) {
                    int begin = value.indexOf(s) + s.length();
                    int end = value.indexOf("__", begin);
                    String id = value.substring(begin, end);
                    value = value.replace(s + id + "__", getScientificDescription(id));
                }
                texts.put(key, value);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return scientificDescriptions.size() != 0;
    }

    public static void main(String[] args) {
        System.out.println(texts);
    }
}
