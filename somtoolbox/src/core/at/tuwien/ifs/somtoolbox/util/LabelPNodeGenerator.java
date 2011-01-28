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
package at.tuwien.ifs.somtoolbox.util;

import java.awt.Color;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.EditLabelEventListener;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusteringTree;

/**
 * Convenience class for creating new labels. (cluster and manual)
 * 
 * @author Angela Roiger
 * @version $Id: LabelPNodeGenerator.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class LabelPNodeGenerator {
    private static EditLabelEventListener labelListener = new EditLabelEventListener();

    /**
     * Adds an additional line of text to the label.
     * 
     * @param labelNode The node to add the text to
     * @param text the text string
     * @param fontSize the desired font size
     * @param xOffset <i>unused</i>
     * @param yOffset <i>unused</i>
     */
    public static void addTextToLabel(PNode labelNode, String text, int fontSize, double xOffset, double yOffset) {
        PText labelText = newLabelText(text, fontSize, 0, 0);
        labelText.setOffset(labelNode.getChild(labelNode.getChildrenCount() - 1).getXOffset(), labelNode.getChild(
                labelNode.getChildrenCount() - 1).getYOffset()
                + labelNode.getChild(labelNode.getChildrenCount() - 1).getHeight());
        labelNode.addChild(labelText);
        // ok, his function is never used. can probably be deleted
    }

    public static void changeColor(PText label, Color c) {
        label.setPaint(c);
    }

    /**
     * Set the rotation of the PNode in radians
     * 
     * @param labelNode the node to be rotated
     * @param rotation the rotation in radians
     */
    public static void changeRotation(PNode labelNode, double rotation) {
        labelNode.setRotation(rotation);
    }

    public static PNode newLabel(PText textNode) {
        PNode labelNode = new PNode();
        labelNode.addChild(textNode);
        labelNode.addInputEventListener(labelListener);
        return labelNode;
    }

    public static PNode newLabel(String text, int fontSize) {
        return newLabel(newLabelText(text, fontSize, 0, 0));
    }

    public static PNode newLabel(String text, int fontSize, double xPos, double yPos, double xOffset, double yOffset) {
        PNode labelNode = newLabel(text, fontSize);
        labelNode.setX(xPos);
        labelNode.setY(yPos);
        labelNode.setOffset(xOffset, yOffset);
        return labelNode;
    }

    public static PNode newLabelNode(double xOffset, double yOffset, double rotation) {
        PNode n = new PNode();
        n.setOffset(xOffset, yOffset);
        n.setRotation(rotation);
        return n;
    }

    public static PText newLabelText(String text, float fontSize) {
        return newLabelText(text, fontSize, 0, 0);
    }

    public static PText newLabelText(String text, float fontSize, double xOffset, double yOffset) {
        PText labelText = new PText(text);
        labelText.setFont(ClusteringTree.defaultFont.deriveFont(fontSize));
        labelText.setOffset(xOffset, yOffset);
        labelText.addAttribute("type", "manualLabel");
        return labelText;
    }

    public static PText newLabelTextLocation(String text, float fontSize, double x, double y) {
        PText labelText = new PText(text);
        labelText.setFont(ClusteringTree.defaultFont.deriveFont(fontSize));
        labelText.setX(x);
        labelText.setY(y);
        labelText.addAttribute("type", "manualLabel");
        return labelText;
    }

}
