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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.lang.ArrayUtils;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.VisualisationUtils;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizerInstance;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;

/**
 * Save Visualisations of a map to an image file.
 * 
 * @author Jakob Frank
 * @author Rudolf Mayer
 * @version $Id: VisualisationImageSaver.java 3666 2010-07-15 08:59:32Z frank $
 */
public class VisualisationImageSaver implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = { OptionFactory.getOptUnitDescriptionFile(true),
            OptionFactory.getOptWeightVectorFile(true), OptionFactory.getOptInputVectorFile(false),
            OptionFactory.getOptTemplateVectorFile(false), OptionFactory.getOptDataWinnerMappingFile(false),
            OptionFactory.getOptClassInformationFile(false), OptionFactory.getOptBaseName(false),
            OptionFactory.getOptWidth(false), OptionFactory.getOptHeight(false),
            OptionFactory.getOptImageFileType(false), OptionFactory.getOptDrawUnitGrid(false),
            OptionFactory.getOptVisualisations(false) };

    public static final String DESCRIPTION = "Save Visualisations of a map as images to a file.";

    public static final String LONG_DESCRIPTION = "Provides a batch mode to save several/all visualisations of a map to image files.";

    public static final Type APPLICATION_TYPE = Type.Utils;

    public static void main(String[] args) {
        JSAPResult res = OptionFactory.parseResults(args, OPTIONS);

        String uFile = res.getString("unitDescriptionFile");
        String wFile = res.getString("weightVectorFile");
        String dwmFile = res.getString("dataWinnerMappingFile");
        String cFile = res.getString("classInformationFile");
        String vFile = res.getString("inputVectorFile");
        String tFile = res.getString("templateVectorFile");
        String ftype = res.getString("filetype");
        boolean unitGrid = res.getBoolean("unitGrid");

        String basename = res.getString("basename");
        if (basename == null) {
            basename = FileUtils.extractSOMLibInputPrefix(uFile);
        }
        basename = new File(basename).getAbsolutePath();
        int unitW = res.getInt("width");
        int unitH = res.getInt("height", unitW);

        String[] vizs = res.getStringArray("vis");

        GrowingSOM gsom = null;
        CommonSOMViewerStateData state = CommonSOMViewerStateData.getInstance();
        try {
            SOMLibFormatInputReader inputReader = new SOMLibFormatInputReader(wFile, uFile, null);
            gsom = new GrowingSOM(inputReader);

            SharedSOMVisualisationData d = new SharedSOMVisualisationData(cFile, null, null, dwmFile, vFile, tFile,
                    null);
            d.readAvailableData();
            state.inputDataObjects = d;
            gsom.setSharedInputObjects(d);

            Visualizations.initVisualizations(d, inputReader, 0, Palettes.getDefaultPalette(),
                    Palettes.getAvailablePalettes());

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        } catch (SOMLibFileFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }

        if (ArrayUtils.isEmpty(vizs)) {
            System.out.println("No specific visualisation specified - saving all available visualisations.");
            vizs = Visualizations.getReadyVisualizationNames();
            System.out.println("Found " + vizs.length + ": " + Arrays.toString(vizs));
        }

        for (String viz : vizs) {
            BackgroundImageVisualizerInstance v = Visualizations.getVisualizationByName(viz);
            if (v == null) {
                System.out.println("Visualization '" + viz + "' not found!");
                continue;
            }
            BackgroundImageVisualizer i = v.getVis();

            GrowingLayer layer = gsom.getLayer();
            try {
                int height = unitH * layer.getYSize();
                int width = unitW * layer.getXSize();
                HashMap<String, BufferedImage> visualizationFlavours = i.getVisualizationFlavours(v.getVariant(), gsom,
                        width, height);
                ArrayList<String> keys = new ArrayList<String>(visualizationFlavours.keySet());
                Collections.sort(keys);

                // if the visualisation has more than 5 flavours, we create a sub-dir for it
                String subDirName = "";
                String oldBasename = basename; // save original base name for later
                if (keys.size() > 5) {
                    String parentDir = new File(basename).getParentFile().getPath(); // get the parent path
                    String filePrefix = basename.substring(parentDir.length()); // end the file name prefix
                    subDirName = parentDir + File.separator + filePrefix + "_" + viz + File.separator; // compose a new
                    // subdir name
                    new File(subDirName).mkdir(); // create the dir
                    basename = subDirName + filePrefix; // and extend the base name by the subdir
                }
                for (String key : keys) {
                    File out = new File(basename + "_" + viz + key + "." + ftype);
                    System.out.println("Generating visualisation '" + viz + "' as '" + out.getPath() + "'.");
                    BufferedImage image = visualizationFlavours.get(key);
                    if (unitGrid) {
                        VisualisationUtils.drawUnitGrid(image, gsom, width, height);
                    }
                    ImageIO.write(image, ftype, out);
                }
                basename = oldBasename; // reset base name
            } catch (SOMToolboxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}
