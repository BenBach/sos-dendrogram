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
package at.tuwien.ifs.somtoolbox.models;

import java.io.IOException;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.MnemonicSOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.input.SOMInputReader;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.layers.MnemonicGrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.output.HTMLOutputter;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.output.labeling.AbstractLabeler;
import at.tuwien.ifs.somtoolbox.output.labeling.Labeler;
import at.tuwien.ifs.somtoolbox.properties.FileProperties;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;

/**
 * This class implements so-called Mnemonic SOMs, i.e. two-dimensional SOMs which are not rectangular or quadratic, but
 * have a grid which is not fully occupied with units.<br>
 * More details can be found in:<br>
 * <i>Mnemonic SOMs: Recognisable Shapes for Self-Organizing Maps</i>. Proceedings of the 5th Workshop On
 * Self-Organizing Maps Paris (WSOM 2005), pp. 131-138, September 5-8 2005, Paris, France. [<a
 * href="http://www.ifs.tuwien.ac.at/~mayer/publications/pdf/may_wsom05.pdf" target="_blank">Download as PDF</a>].
 * 
 * @author Rudolf Mayer
 * @version $Id: MnemonicSOM.java 3685 2010-07-15 09:15:06Z frank $
 */
public class MnemonicSOM extends GrowingSOM implements SOMToolboxApp {

    // TODO: Description
    public static String DESCRIPTION = "MenmonicSOM has a grid which is not fully occupied with units.";

    // TODO: Long_Description
    public static String LONG_DESCRIPTION = DESCRIPTION;

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getSwitchHtmlOutput(false),
            OptionFactory.getOptLabeling(false), OptionFactory.getOptNumberLabels(false),
            OptionFactory.getOptDimension(true), OptionFactory.getOptMapDescriptionFile(true),
            OptionFactory.getOptUnitDescriptionFile(true), OptionFactory.getOptNumberWinners(false),
            OptionFactory.getSwitchSkipDataWinnerMapping(), OptionFactory.getOptProperties(true) };

    public static final Type APPLICATION_TYPE = Type.Training;

    public MnemonicSOM(SOMInputReader ir) throws SOMToolboxException {
        this(1, null, ir);
    }

    private MnemonicSOM(int id, Unit su, SOMInputReader ir) throws SOMToolboxException {
        // TODO: think about rand seed (7), use map description file when/ provided);
        super(id, su, ir, new MnemonicGrowingLayer(id, su, ir.getXSize(), ir.getYSize(), ir.getZSize(),
                ir.getMetricName(), ir.getDim(), ir.getVectors(), 7));
    }

    public static void main(String[] args) {
        GrowingSOM gsom = null;
        InputData data = null;
        SOMProperties somProps = null;
        FileProperties fileProps = null;

        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting MnemonicSOM");

        String propFileName = config.getString("properties");
        String unitDescFileName = config.getString("unitDescriptionFile", null);
        String mapDescFileName = config.getString("mapDescriptionFile", null);
        String labelerName = config.getString("labeling", null);
        int numLabels = config.getInt("numberLabels", 5);
        boolean skipDataWinnerMapping = config.getBoolean("skipDataWinnerMapping", false);
        int dimension = config.getInt("dimension");
        Labeler labeler = null;
        // TODO: use parameter for max
        int numWinners = config.getInt("numberWinners", SOMLibDataWinnerMapping.MAX_DATA_WINNERS);

        if (labelerName != null) { // if labeling then label
            try {
                labeler = AbstractLabeler.instantiate(labelerName);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Instantiated labeler " + labelerName);
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not instantiate labeler \"" + labelerName + "\".");
                System.exit(-1);
            }
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Training a sparse SOM.");
        try {
            fileProps = new FileProperties(propFileName);
            somProps = new SOMProperties(propFileName);
        } catch (PropertiesException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            gsom = new MnemonicSOM(new MnemonicSOMLibFormatInputReader(null, unitDescFileName, mapDescFileName,
                    dimension));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            e.printStackTrace();
            System.exit(-1);
        }

        data = getInputData(fileProps);

        // setting input data so it is accessible by map output
        gsom.setSharedInputObjects(new SharedSOMVisualisationData(null, null, null, null,
                fileProps.vectorFileName(true), fileProps.templateFileName(true), null));
        gsom.getSharedInputObjects().setData(SOMVisualisationData.INPUT_VECTOR, data);

        gsom.train(data, somProps);

        if (labelerName != null) { // if labeling then label
            labeler.label(gsom, data, numLabels);
        }

        try {
            SOMLibMapOutputter.write(gsom, fileProps.outputDirectory(), fileProps.namePrefix(false), true, somProps,
                    fileProps);
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not open or write to output file " + fileProps.namePrefix(false) + ": " + e.getMessage());
            System.exit(-1);
        }
        numWinners = Math.min(numWinners, gsom.getLayer().getXSize() * gsom.getLayer().getYSize());
        if (!skipDataWinnerMapping) {
            try {
                SOMLibMapOutputter.writeDataWinnerMappingFile(gsom, data, numWinners, fileProps.outputDirectory(),
                        fileProps.namePrefix(false), true);
            } catch (IOException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not open or write to output file " + fileProps.namePrefix(false) + ": " + e.getMessage());
                System.exit(-1);
            }
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Skipping writing data winner mapping file");
        }

        if (config.getBoolean("htmlOutput") == true) {
            try {
                new HTMLOutputter().write(gsom, fileProps.outputDirectory(), fileProps.namePrefix(false));
            } catch (IOException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Could not open or write to output file " + fileProps.namePrefix(false) + ": " + e.getMessage());
                System.exit(-1);
            }
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("finished GrowingSOM");
    }

}