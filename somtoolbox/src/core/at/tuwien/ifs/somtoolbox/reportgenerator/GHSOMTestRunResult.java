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

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.labeling.LabelSOM;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: GHSOMTestRunResult.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class GHSOMTestRunResult extends GGSOMTestRunResult {

    private GHSOM ghsom = null;

    /**
     * creates a new instance of this type, by handing over a TestRunResult object. All information needed are then
     * taken from this object (all the paths to the files, etc ... )
     * 
     * @param result an Object from which all the filepaths, the dataset information and the run id can be taken
     */
    public GHSOMTestRunResult(TestRunResult result) {
        super(result);
    }

    /**
     * returns a GHSOM representing the SOM trained in this run
     * 
     * @return the GHSOM rebuilt from the map file
     */
    public GHSOM getGHSOM() {
        if (this.ghsom == null) {
            this.ghsom = new GHSOM(this.getInputReader());
            this.visData = this.getVisData();
            this.ghsom.setSharedInputObjects(this.visData);

            // label the som
            LabelSOM labelsom = new LabelSOM();
            labelsom.label(this.ghsom, this.getDatasetInfo().getInputData(), this.getDatasetInfo().getVectorDim());

            SOMLibFormatInputReader somlib = this.getInputReader();
            Palette[] palettes = Palettes.getAvailablePalettes();
            Palette defaultPalette = Palettes.getDefaultPalette();
            int defaultPaletteIndex = Palettes.getPaletteIndex(defaultPalette);
            Visualizations.initVisualizations(visData, somlib, defaultPaletteIndex, defaultPalette, palettes);
        }
        return ghsom;
    }

    @Override
    public int getNumberOfMaps() {
        return this.countSubmaps(this.getGHSOM().topLayerMap(), 1);
    }

    private int countSubmaps(GrowingSOM map, int count) {
        ArrayList<GrowingSOM> submaps = map.getLayer().getAllSubMaps();
        for (int i = 0; i < submaps.size(); i++) {
            count += this.countSubmaps(submaps.get(i), 1);
        }
        return count;
    }

}
