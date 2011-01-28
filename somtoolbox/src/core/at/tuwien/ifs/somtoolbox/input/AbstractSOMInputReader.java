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
package at.tuwien.ifs.somtoolbox.input;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.layers.Layer.GridLayout;
import at.tuwien.ifs.somtoolbox.layers.Layer.GridTopology;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Provides generic functionality to read a saved network model.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: AbstractSOMInputReader.java 3883 2010-11-02 17:13:23Z frank $
 */
public abstract class AbstractSOMInputReader implements SOMInputReader {
    /**
     * Inner class holding the information about a specific unit.
     * 
     * @author Michael Dittenbach
     */
    protected class UnitInformation {
        protected String[] bestContextLabels = null;

        protected String[] contextGateLabels = null;

        protected double[] gateWeightLabels = null;

        protected String[] kaskiGateLabels = null;

        // private int kaskiWeights = 0;

        // private int gateWeights = 0;

        // private int bestContext = 0;

        protected String[] kaskiLabels = null;

        protected double[] kaskiWeightLabels = null;

        protected String[] mappedVecs = null;

        protected double[] mappedVecsDist = null;

        protected int nrbestcontext = 0;

        protected int nrContextGate = 0;

        protected int nrgateweights = 0;

        protected int nrKaski = 0;

        protected int nrKaskiGate = 0;

        protected int nrkaskiweights = 0;

        protected int nrSomsMapped = 0;

        protected int nrUnitLabels = 0;

        protected int nrVecMapped = 0;

        protected int posX = 0;

        protected int posY = 0;

        protected int posZ = 0;

        protected double quantErrorUnit = 0;

        protected double quantErrorUnitAvg = 0;

        protected String unitId = null;

        protected String[] unitLabels = null;

        protected double[] unitLabelsQe = null;

        protected double[] unitLabelsWgt = null;

        protected String[] urlMappedSoms = null;

        protected double[] vector = null;

        /**
         * Sole constructor.
         */
        protected UnitInformation() {
            vector = new double[0];
        }

        protected UnitInformation(int dim) {
            vector = new double[dim];
        }

    }

    protected GridLayout gridLayout = GridLayout.rectangular;

    protected GridTopology gridTopology = GridTopology.planar;

    protected boolean labelled = false;

    protected int dim = 0;

    protected String metricName = "at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric"; // default metric

    protected UnitInformation[][][] unitInfo = null;

    protected int xSize = 0;

    protected int ySize = 0;

    protected int zSize = 0;

    /**
     * The common prefix of all input vector labels. Will be once computed in {@link #getCommonVectorLabelPrefix()}, and
     * then cached.
     */
    protected String commonLabelPrefix = null;

    protected ArrayList<String> allVectorNames = new ArrayList<String>();

    public AbstractSOMInputReader() {
        super();
    }

    @Override
    public String[] getBestContextUnitLabels(int x, int y) {
        return getBestContextUnitLabels(x, y, 0);
    }

    @Override
    public String[] getBestContextUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].bestContextLabels;
    }

    @Override
    public String[] getContextGateUnitLabels(int x, int y) {
        return getContextGateUnitLabels(x, y, 0);
    }

    @Override
    public String[] getContextGateUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].contextGateLabels;
    }

    /** @see at.tuwien.ifs.somtoolbox.input.SOMInputReader#getDim() */
    @Override
    public int getDim() {
        return dim;
    }

    @Override
    public String[] getKaskiGateUnitLabels(int x, int y) {
        return getKaskiGateUnitLabels(x, y, 0);
    }

    @Override
    public String[] getKaskiGateUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].kaskiGateLabels;
    }

    @Override
    public double[] getKaskiGateUnitLabelsWgt(int x, int y) {
        return getKaskiGateUnitLabelsWgt(x, y, 0);
    }

    @Override
    public double[] getKaskiGateUnitLabelsWgt(int x, int y, int z) {
        return unitInfo[x][y][z].gateWeightLabels;
    }

    @Override
    public String[] getKaskiUnitLabels(int x, int y) {
        return getKaskiUnitLabels(x, y, 0);
    }

    @Override
    public String[] getKaskiUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].kaskiLabels;
    }

    @Override
    public double[] getKaskiUnitLabelsWgt(int x, int y) {
        return getKaskiUnitLabelsWgt(x, y, 0);
    }

    @Override
    public double[] getKaskiUnitLabelsWgt(int x, int y, int z) {
        return unitInfo[x][y][z].kaskiWeightLabels;
    }

    @Override
    public String[] getMappedVecs(int x, int y) {
        return getMappedVecs(x, y, 0);
    }

    @Override
    public String[] getMappedVecs(int x, int y, int z) {
        return unitInfo[x][y][z].mappedVecs;
    }

    @Override
    public double[] getMappedVecsDist(int x, int y) {
        return getMappedVecsDist(x, y, 0);
    }

    @Override
    public double[] getMappedVecsDist(int x, int y, int z) {
        return unitInfo[x][y][z].mappedVecsDist;
    }

    @Override
    public String getMetricName() {
        return metricName;
    }

    @Override
    public int getNrBestContext(int x, int y) {
        return getNrBestContext(x, y, 0);
    }

    @Override
    public int getNrBestContext(int x, int y, int z) {
        return unitInfo[x][y][z].nrbestcontext;
    }

    @Override
    public int getNrContextGateLabels(int x, int y) {
        return getNrContextGateLabels(x, y, 0);
    }

    @Override
    public int getNrContextGateLabels(int x, int y, int z) {
        return unitInfo[x][y][z].nrContextGate;
    }

    @Override
    public int getNrGateWeights(int x, int y) {
        return getNrGateWeights(x, y, 0);
    }

    @Override
    public int getNrGateWeights(int x, int y, int z) {
        return unitInfo[x][y][z].nrgateweights;
    }

    @Override
    public int getNrKaskiGateLabels(int x, int y) {
        return getNrKaskiGateLabels(x, y, 0);
    }

    @Override
    public int getNrKaskiGateLabels(int x, int y, int z) {
        return unitInfo[x][y][z].nrKaskiGate;
    }

    @Override
    public int getNrKaskiLabels(int x, int y) {
        return getNrKaskiLabels(x, y, 0);
    }

    @Override
    public int getNrKaskiLabels(int x, int y, int z) {
        return unitInfo[x][y][z].nrKaski;
    }

    @Override
    public int getNrKaskiWeights(int x, int y) {
        return getNrKaskiWeights(x, y, 0);
    }

    @Override
    public int getNrKaskiWeights(int x, int y, int z) {
        return unitInfo[x][y][z].nrkaskiweights;
    }

    // public String[] getcontextLabels(int x, int y) {
    // return unitInfo[x][y].contextLabels;
    // }

    @Override
    public int getNrSomsMapped(int x, int y) {
        return getNrSomsMapped(x, y, 0);
    }

    @Override
    public int getNrSomsMapped(int x, int y, int z) {
        return unitInfo[x][y][z].nrSomsMapped;
    }

    @Override
    public int getNrUnitLabels(int x, int y) {
        return getNrUnitLabels(x, y, 0);
    }

    @Override
    public int getNrUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].nrUnitLabels;
    }

    @Override
    public int getNrVecMapped(int x, int y) {
        return getNrVecMapped(x, y, 0);
    }

    @Override
    public int getNrVecMapped(int x, int y, int z) {
        return unitInfo[x][y][z].nrVecMapped;
    }

    @Override
    public double getQuantErrorUnit(int x, int y) {
        return getQuantErrorUnit(x, y, 0);
    }

    @Override
    public double getQuantErrorUnit(int x, int y, int z) {
        return unitInfo[x][y][z].quantErrorUnit;
    }

    @Override
    public double getQuantErrorUnitAvg(int x, int y) {
        return getQuantErrorUnitAvg(x, y, 0);
    }

    @Override
    public double getQuantErrorUnitAvg(int x, int y, int z) {
        return unitInfo[x][y][z].quantErrorUnitAvg;
    }

    @Override
    public String[] getUnitLabels(int x, int y) {
        return getUnitLabels(x, y, 0);
    }

    @Override
    public String[] getUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].unitLabels;
    }

    @Override
    public double[] getUnitLabelsQe(int x, int y) {
        return getUnitLabelsQe(x, y, 0);
    }

    @Override
    public double[] getUnitLabelsQe(int x, int y, int z) {
        return unitInfo[x][y][z].unitLabelsQe;
    }

    @Override
    public double[] getUnitLabelsWgt(int x, int y) {
        return getUnitLabelsWgt(x, y, 0);
    }

    @Override
    public double[] getUnitLabelsWgt(int x, int y, int z) {
        return unitInfo[x][y][z].unitLabelsWgt;
    }

    @Override
    public String[] getUrlMappedSoms(int x, int y) {
        return getUrlMappedSoms(x, y, 0);
    }

    @Override
    public String[] getUrlMappedSoms(int x, int y, int z) {
        return unitInfo[x][y][z].urlMappedSoms;
    }

    @Override
    public double[][][][] getVectors() {
        double[][][][] res = new double[xSize][ySize][zSize][];
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    res[i][j][k] = unitInfo[i][j][k].vector;
                }
            }
        }
        return res;
    }

    @Override
    public int getXSize() {
        return xSize;
    }

    @Override
    public int getYSize() {
        return ySize;
    }

    @Override
    public int getZSize() {
        return zSize;
    }

    protected void initUnitInformation() {
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    unitInfo[i][j][k] = new UnitInformation();
                }
            }
        }
    }

    @Override
    public String getCommonVectorLabelPrefix() {
        if (commonLabelPrefix == null) {
            commonLabelPrefix = StringUtils.getCommonPrefix(allVectorNames);
        }
        return commonLabelPrefix;
    }

    @Override
    public boolean isLabelled() {
        return labelled;
    }

    @Override
    public GridLayout getGridLayout() {
        return gridLayout;
    }

    @Override
    public GridTopology getGridTopology() {
        return gridTopology;
    }

}