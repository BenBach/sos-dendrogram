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
package at.tuwien.ifs.somtoolbox.apps;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.layers.TrainingInterruptionListener;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.properties.FileProperties;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.visualization.SmoothedDataHistograms;

/**
 * <p>
 * <i>Created on Sep 23, 2004</i>
 * </p>
 * 
 * @author Michael Dittenbach
 * @version $Id: SDHTrainingSequenceGenerator.java 3877 2010-11-02 15:43:17Z frank $
 */
// FIXME: make this a generic class that can work on any visualisation!
public class SDHTrainingSequenceGenerator implements TrainingInterruptionListener {

    private int interruptEvery = 0;

    private SOMProperties somProps = null;

    private FileProperties fileProps = null;

    private InputData data = null;

    private GrowingSOM gsom = null;

    private SmoothedDataHistograms sdh = null;

    public SDHTrainingSequenceGenerator(String[] args) {
        interruptEvery = Integer.parseInt(args[1]);
        try {
            somProps = new SOMProperties(args[0]);
            fileProps = new FileProperties(args[0]);
        } catch (PropertiesException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }
        data = InputDataFactory.open(fileProps.vectorFileName(true), fileProps.templateFileName(true),
                fileProps.sparseData(), true, 0, 7);
        gsom = new GrowingSOM(false, somProps, data);
        sdh = new SmoothedDataHistograms();
        // sdh.setFileName(0, fileProps.vectorFileName(true)); // FIXME: maybe
        // FIXME: was changed to different mechanism, update!?!

        gsom.getLayer().setTrainingInterruptionListener(this, interruptEvery);
        gsom.train(data, somProps);
        // interruptionOccurred()

    }

    @Override
    public void interruptionOccurred(int currentIteration, int numIterations) {
        System.out.println(currentIteration);
        try {
            BufferedImage image = sdh.createVisualization(2, gsom, 600, 400);
            ImageIO.write(image, "png", new File(fileProps.outputDirectory() + System.getProperty("file.separator")
                    + fileProps.namePrefix(true) + "_" + StringUtils.format(currentIteration, 6) + ".png"));
        } catch (SOMToolboxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        // args: propertyFile interruptEvery
        new SDHTrainingSequenceGenerator(args);
    }

}
