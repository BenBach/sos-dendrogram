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
package at.tuwien.ifs.somtoolbox.output.labeling;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.Label;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * Implements the <code>LabelSOM</code> labelling method, as described in <i><b>A. Rauber, and D. Merkl</b>: Automatic
 * Labeling of Self-Organizing Maps for Information Retrieval In: Journal of Systems Research and Information Systems
 * (JSRIS), Vol. 10, Nr. 10, pp 23-45, OPA, Gordon and Breach Science Publishers, December 2001.</i>
 * 
 * @author Michael Dittenbach
 * @version $Id: LabelSOM.java 3883 2010-11-02 17:13:23Z frank $
 */
public class LabelSOM extends AbstractLabeler implements Labeler, SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputVectorFile(true),
            OptionFactory.getOptTemplateVectorFile(true), OptionFactory.getOptWeightVectorFile(true),
            OptionFactory.getOptUnitDescriptionFile(true), OptionFactory.getOptNumberLabels(false, "5"),
            OptionFactory.getSwitchIsDenseData(), OptionFactory.getSwitchIgnoreLabelsWithZero(),
            OptionFactory.getOptMapDescriptionFile(false) };

    public static final String DESCRIPTION = "Implements the LabelSOM labelling method";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Helper;

    /**
     * Method for stand-alone execution of map labelling. Options are:<br/>
     * <ul>
     * <li>-v Input file containing the input vectors of.</li>
     * <li>-t Template vector file containing vector element labels.</li>
     * <li>-w Weight vector filename, mand.</li>
     * <li>-u Unit description file, mand.</li>
     * <li>-n Number of labels, opt., default = 5</li>
     * <li>-d Set if input data vectors are densely populated.</li>
     * <li>-m Map description file, opt.</li>
     * </ul>
     * 
     * @param args the execution arguments as stated above.
     */
    public static void main(String[] args) {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        int numLabels = config.getInt("numberLabels", AbstractNetworkModel.DEFAULT_LABEL_COUNT);
        String inputVectorFilename = AbstractOptionFactory.getFilePath(config, "inputVectorFile");
        boolean denseData = config.getBoolean("denseData", false);
        boolean ignoreLabelsWithZero = config.getBoolean("ignoreLabelsWithZero", false);
        String templateVectorFilename = AbstractOptionFactory.getFilePath(config, "templateVectorFile");
        String unitDescriptionFilename = AbstractOptionFactory.getFilePath(config, "unitDescriptionFile");
        String weightVectorFilename = AbstractOptionFactory.getFilePath(config, "weightVectorFile");
        String mapDescriptionFilename = AbstractOptionFactory.getFilePath(config, "mapDescriptionFile");

        String outputDirName = unitDescriptionFilename.substring(0,
                unitDescriptionFilename.lastIndexOf(System.getProperty("file.separator")) + 1);
        if (StringUtils.isBlank(outputDirName)) {
            outputDirName = ".";
        }
        String outputFileName = unitDescriptionFilename.substring(
                unitDescriptionFilename.lastIndexOf(System.getProperty("file.separator")) + 1,
                unitDescriptionFilename.indexOf('.',
                        unitDescriptionFilename.lastIndexOf(System.getProperty("file.separator")) + 1));

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Labelling map '" + outputFileName + "' to output directory: " + outputDirName);

        GrowingSOM gsom = null;
        try {
            gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFilename, unitDescriptionFilename,
                    mapDescriptionFilename));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            return;
        }
        // TODO: cacheBlock=1, no problem
        InputData data = InputDataFactory.open(inputVectorFilename, templateVectorFilename, !denseData, true, 1,
                7);
        LabelSOM labeler = new LabelSOM();
        labeler.label(gsom, data, numLabels, ignoreLabelsWithZero);

        try {
            // TODO: make output format an argument, zipped output
            SOMLibMapOutputter.writeUnitDescriptionFile(gsom, outputDirName, outputFileName, true);
        } catch (IOException e) { // TODO: create new exception type
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not open or write to output file: " + e.getMessage() + ": " + e.getMessage());
            return;
        }

    }

    @Override
    public void label(GHSOM ghsom, InputData data, int num) {
        label(ghsom.topLayerMap(), data, num);
    }

    @Override
    public void label(GrowingSOM gsom, InputData data, int num) {
        label(gsom, data, num, false);
    }

    @Override
    public void label(GrowingSOM gsom, InputData data, int num, boolean ignoreLabelsWithZero) {
        if (num > data.templateVector().dim()) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Specified number of labels (" + num + ") exceeds number of features in template vector ("
                            + data.templateVector().dim()
                            + ") - defaulting to number of features as maximum possible value.");
            num = data.templateVector().dim();
        }
        Unit[] units = gsom.getLayer().getAllUnits();
        StdErrProgressWriter progress = new StdErrProgressWriter(units.length, "Labelling unit ");
        for (int i = 0; i < units.length; i++) { // do labeling for each unit
            progress.progress(i);
            if (units[i].getNumberOfMappedInputs() != 0) {
                InputDatum[] unitData = data.getInputDatum(units[i].getMappedInputNames());
                Label[] allLabels = new Label[data.dim()];

                // for each feature, check all inputs (qe und durchschnittsvalue)
                for (int ve = 0; ve < data.dim(); ve++) {
                    double meanVal = 0;
                    double qeVal = 0;
                    for (InputDatum element : unitData) {
                        meanVal += element.getVector().get(ve);
                        qeVal += Math.abs(element.getVector().get(ve) - units[i].getWeightVector()[ve]);
                    }
                    meanVal = meanVal / unitData.length;
                    qeVal = qeVal / unitData.length;

                    // if we shall ignore zero labels, ignore those with mean==0, and very small qe
                    if (ignoreLabelsWithZero && meanVal == 0 && qeVal * 100 < 0.1) {
                        allLabels[ve] = new Label("", meanVal, qeVal);
                    } else {
                        allLabels[ve] = new Label(data.templateVector().getLabel(ve), meanVal, qeVal);
                    }
                }
                Label[] labelSortedByQe = new Label[data.dim()];
                Label[] labelSortedByMean = new Label[data.dim()];
                for (int j = 0; j < data.dim(); j++) {
                    labelSortedByQe[j] = allLabels[j];
                    labelSortedByMean[j] = allLabels[j];
                }
                Label.sortByQe(labelSortedByQe, Label.SORT_ASC);
                Label.sortByValue(labelSortedByMean, Label.SORT_ASC);

                // determine select num top labels
                Label[] labels = new Label[num];
                int found = 0;
                int lab = 0;
                while (found < num && lab < data.dim()) { // go through list sorted by qe
                    boolean found2 = false;
                    int lab2 = data.dim() - 1;
                    while (found2 == false && lab2 >= data.dim() - num) {
                        if (labelSortedByMean[lab2].equals(labelSortedByQe[lab])) {
                            found2 = true;
                            labels[found] = labelSortedByQe[lab];
                            found++;
                        }
                        lab2--;
                    }
                    lab++;
                }
                Label.sortByValueQe(labels, Label.SORT_DESC, Label.SORT_ASC);
                units[i].setLabels(labels);

                if (units[i].getMappedSOM() != null) { // label subordinate maps as well
                    label(units[i].getMappedSOM(), data, num);
                }

            }
        }
        gsom.setLabelled(true);
    }
}
