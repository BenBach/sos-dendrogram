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
package at.tuwien.ifs.somtoolbox.apps.server;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.LabelXmlUtils;
import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * Utility class to create images of labels, which can be used to overlay a visualisation
 * 
 * @author Rudolf Mayer
 * @version $Id: LabelImageCreator.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class LabelImageCreator {
    private static final int CELL_SIZE_VIEWER = 130;

    private static final int CELL_SIZE_SERVER = SOMMap.DEFAULT_CELL_SIZE;

    private static final double SCALE = (double) CELL_SIZE_SERVER / (double) CELL_SIZE_VIEWER;

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException,
            SOMToolboxException {
        // register and parse all options for the AttendeeMapper
        JSAPResult config = AbstractOptionFactory.parseResults(args, OptionFactory.getOptLabelFileName(true),
                OptionFactory.getOptOutputDirectory(true));

        String labelFileName = config.getString("labelFileName");
        String outputDirectory = config.getString("outputDirectory");
        new LabelImageCreator().writeImages(labelFileName, outputDirectory);
    }

    /**
     * Creates and writes the label images.
     * 
     * @param labelFileName the file containing the XML descriptions of the labels.
     * @param outputDirectory the directory to write to. Existing files with the same name will be overwritten.
     */
    public void writeImages(String labelFileName, String outputDirectory) throws ParserConfigurationException,
            SAXException, IOException, SOMToolboxException {

        File f = new File(labelFileName);
        ArrayList<LabelDescription>[] restoreLabelsByLevel = LabelXmlUtils.restoreLabelsByFontSizeLevel(f);
        int levels = 3;
        int x = (restoreLabelsByLevel.length + 2) / levels - 1;

        for (int i = 0; i < levels; i++) {
            double scale = SCALE * (i + 1);
            // System.out.println("scale: " + scale);
            double d = scale;
            int xSize = 44;
            int ySize = 32;
            BufferedImage image = new BufferedImage((int) (xSize * CELL_SIZE_VIEWER * d), (int) (ySize
                    * CELL_SIZE_VIEWER * d), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
            g.setColor(Color.BLACK);
            int start = i * x;
            int end = (i + 1) * x;
            start = Math.min(i, restoreLabelsByLevel.length - 1);
            end = Math.min(i + 1, restoreLabelsByLevel.length - 1);
            System.out.println("using: " + start + "-" + end);
            for (int j = end; j >= start; j--) {
                ArrayList<LabelDescription> list = restoreLabelsByLevel[j];
                for (int k = 0; k < list.size(); k++) {
                    LabelDescription label = list.get(k);
                    if (label.isVisible()) {
                        if (j == end) { // last set of labels in this level
                            label.setColor(Color.GRAY);
                        } else {
                            label.setColor(Color.BLACK);
                        }
                        g.setFont(label.getFont(scale));
                        // TODO: maybe set a lighter default color
                        g.setColor(label.getColor());
                        AffineTransform at = new AffineTransform();
                        at.setToRotation(label.getRotation());
                        g.setTransform(at);
                        // TODO: maybe correct location of labels that do not fit completly inside the image
                        final String[] split = label.getText().split(System.getProperty("line.separator"));
                        for (int index = 0; index < split.length; index++) {
                            g.drawString(split[index], label.getX(scale), label.getYBaseline(g.getFontMetrics(), scale,
                                    index));
                        }
                    } else {
                        System.out.println("Ignoring not-visible label " + label.getText());
                    }
                }
            }

            FileUtils.saveImageToFile(outputDirectory + "/label-zoom" + (i + 1) + ".png", image);
        }
    }

}
