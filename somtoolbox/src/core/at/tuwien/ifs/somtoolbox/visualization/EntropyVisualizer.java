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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import org.jfree.util.PaintList;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.visualization.thematicmap.RegionManager;
import at.tuwien.ifs.somtoolbox.visualization.thematicmap.SOMRegion;

/**
 * @author Martin Waitzbauer (0226025)
 * @version $Id: EntropyVisualizer.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class EntropyVisualizer extends AbstractMatrixVisualizer implements QualityMeasureVisualizer {

    private int zoom = MapPNode.DEFAULT_UNIT_WIDTH / preferredScaleFactor;

    double min_visible_class = 0;

    public final SOMRegion MAXENTROPY_REGION = null;

    public final SOMRegion MINENTROPY_REGION = null;

    protected Hashtable<String, RegionManager> regionCache = new Hashtable<String, RegionManager>();

    private RegionManager regionManager;

    private ArrayList<Double> entropyclass_list = new ArrayList<Double>();

    private ArrayList<HashMap<String, Integer>> entropyclassnames_list = new ArrayList<HashMap<String, Integer>>();

    public EntropyVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Entropy Visualiser" };
        VISUALIZATION_SHORT_NAMES = new String[] { "Entropy", "PseudoSilhouetteVal", "SOMSilhouette" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "TODO!" };
        neededInputObjects = new String[] { SOMVisualisationData.CLASS_INFO };
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        checkNeededObjectsAvailable(gsom);
        // need to set the zoom level for exports
        zoom = width / gsom.getLayer().getXSize();

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = res.createGraphics();
        int xSize = gsom.getLayer().getXSize();
        int ySize = gsom.getLayer().getYSize();
        PaintList paintList = inputObjects.getClassInfo().getPaintList();
        String key = min_visible_class + "_" + width + "_" + height;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        try {
            this.regionManager = new RegionManager(inputObjects.getClassInfo(), paintList, width, height,
                    min_visible_class, zoom);
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    Unit unit = gsom.getLayer().getUnit(i, j);
                    if (unit != null && unit.getNumberOfMappedInputs() != 0) {
                        this.regionManager.addNewRegion(unit);
                    }
                }
            }
            this.regionManager.build();
            this.regionCache.put(key, this.regionManager);
        } catch (LayerAccessException e) {
            e.printStackTrace();
            return null;
        }
        this.regionManager = regionCache.get(key);
        this.fillRegions(index, g);
        this.regionManager.drawRegions(g);
        // System.out.print("RegionManagerregions: "+this.regionManager.regions.size());
        return res;

    }

    /**
     * Fills the Regions
     * 
     * @param index Index specifies the Class to be displayed, if index is set to -1, all the classes are painted
     */
    public void fillRegions(int index, Graphics2D g) {
        double max = this.regionManager.getMaximumEntropyRegion().calculateEntropy();
        double temp = 0.0;
        ArrayList<String[][]> temp_regionclassnames = new ArrayList<String[][]>();
        for (int i = 0; i < this.regionManager.getRegions().size(); i++) {
            SOMRegion r = this.regionManager.getRegions().get(i);
            double c1 = r.calculateEntropy();
            Color c = this.getBestFittingColor(c1, max);
            g.setColor(c);
            if (index > -1) { /* only Paint the specified ClassID */
                if (r.resolveMainClassIndex(index) > -1) {/* check if the Current Region is populated wit at least 1 individual of the index class */
                    g.fillPolygon(r);
                    temp_regionclassnames.add(r.getClasses());
                    temp += c1;
                }
            } else {
                // paint the full image
                g.fillPolygon(r);
            }
        }
        if (index > -1) {/*-1 is  all classes by definition*/
            this.entropyclass_list.add(temp);
            this.mergeClasses(temp_regionclassnames);
        }

    }

    /**
     * Spreads the current Region Entropy Error along the whole Palette Interval. (Entropy Error uses to be very small,
     * and thus generates only colors in a small interval)
     */
    private Color getBestFittingColor(double c1, double max) {
        int x;
        if (c1 == max) {
            x = (int) Math.round(c1 / max * palette.maxColourIndex());
        } else {
            x = (int) Math.round(c1 / max * palette.getNumberOfColours());
        }
        // System.out.println("Farbe: "+x);
        return palette.getColor(x);
    }

    /** returns the maximum entropy regions value as double;F */
    public double getMaximumEntropy() {
        return this.regionManager.getMaximumEntropyRegion().calculateEntropy();
    }

    /** returns the minimum entropy regions value as double; */
    public double getMinimumEntropy() {
        return this.regionManager.getMinimumEntropyRegion().calculateEntropy();
    }

    /** returns the Maximum Entropy Region's Class Names */
    public String[][] getMaximumEntropyRegionNames() {
        return this.regionManager.getMaximumEntropyRegion().getClasses();
    }

    /**
     * returns the percentage of regions with 0 Entropy
     * 
     * @return counter
     */
    public double getPercOfZeroEntropyRegions() {
        double counter = 0;
        for (int i = 0; i < this.regionManager.getRegions().size(); i++) {
            SOMRegion r = this.regionManager.getRegions().get(i);
            if (r.calculateEntropy() == 0.0) {
                counter++;
            }
        }
        return counter / this.regionManager.getRegions().size() * 100;
    }

    /** returns the entropy for class with 'index' */
    public double ClassEntropy(int index) {
        return this.entropyclass_list.get(index);
    }

    /**
     * helper method: go through all regions in the arraylist(all of which have the same index) and count classnames and
     * hits.
     * 
     * @param target is an arraylist , containing all different Regions with the specified Class index as Main class
     */
    private void mergeClasses(ArrayList<String[][]> target) {
        HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        for (int i = 0; i < target.size(); i++) {
            String[][] temp = target.get(i);
            for (int b = 0; b < temp.length; b++) {
                if (!hmap.containsKey(temp[b][0])) {
                    hmap.put(temp[b][0], Integer.valueOf(temp[b][1]));
                } else {
                    int c = hmap.get(temp[b][0]);
                    c += Integer.valueOf(temp[b][1]);
                    hmap.put(temp[b][0], c);
                }
            }
        }
        this.entropyclassnames_list.add(hmap);
    }

    /**
     * returns every other class+hits contained in the regions with classmembers from the given index, or null if index
     * exceeds the possible number of classes
     */
    public String[][] ClassEntropyNames(int index) {
        HashMap<String, Integer> hmap = this.entropyclassnames_list.get(index);
        String[][] sarray = null;
        if (index <= this.inputObjects.getClassInfo().numClasses()) {
            sarray = new String[hmap.size()][2];
            Set<String> set = hmap.keySet();
            Collection<Integer> col = hmap.values();
            String[] kString = set.toArray(new String[hmap.size()]);
            Integer[] vInt = col.toArray(new Integer[hmap.size()]);
            for (int i = 0; i < hmap.size(); i++) {
                sarray[i][0] = kString[i];
                sarray[i][1] = String.valueOf(vInt[i]);
            }
        }
        return sarray;
    }
}
