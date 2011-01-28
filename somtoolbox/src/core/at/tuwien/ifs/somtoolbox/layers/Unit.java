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
package at.tuwien.ifs.somtoolbox.layers;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * Represents a unit on a map. It has a position in terms of x and y coordinates and an n-dimensional weight vector.
 * Data can be mapped onto a <code>Unit</code>. Labels can be assigned to a <code>Unit</code> to describe the mapped
 * data. A <code>Unit</code> can also have an assigned map for use in hierarchical models.<br>
 * TODO: The type of <code>mappedSOM</code> should be made more general than it is now. FIXME: similar to what should be
 * done with {@link GrowingLayer}, we should make a Unit3D version out of this for 3D SOMs, to keep the memory
 * fingerprint small, and avoid overloading many methods with 2D and 3D params, which makes it very hard to read
 * 
 * @author Michael Dittenbach
 * @version $Id: Unit.java 3621 2010-07-07 13:33:28Z mayer $
 */
public class Unit extends InputContainer {

    /**
     * Types of feature weighting modes
     * 
     * @author Rudolf Mayer
     */
    public enum FeatureWeightMode {
        GLOBAL, // GLOBAL weighting as in Nürnberger & Detyniecki
        LOCAL, // LOCAL weighting as in Nürnberger & Detyniecki
        GENERAL, // GENERAL weighting as in Nürnberger & Detyniecki
    }

    public static final String KEYWORDS = "KEYWORDS";

    public static final String GATE = "GATE";

    public static final String CONTEXT = "CONTEXT";

    public static final String LABELSOM = "LabelSOM";

    private static final int INIT_RANDOM = 10;

    private static final int INIT_INTERVAL_INTERPOLATE = 20;

    private static final int INIT_VECTOR = 30;

    private static final int INIT_PCA = 40;

    private int dim = 0;

    // TODO: find a more generic way for labelling
    private Label[] labels = null; // LabelSOM labels

    private Label[] kaskiGateLabels = null; // 

    private Label[] kaskiLabels = null;

    private Label[] gateWeights = null;

    private Label[] bestcontextWeights = null;

    private Label[] contextGateLabels = null;

    private Layer layer = null;

    private GrowingSOM mappedSOM = null; // TODO: Should be NetworkModel or similar in the future

    private double quantizationError = 0;

    private double[] weightVector = null;

    private double[] featureWeights = null;

    private int xPos = -1;

    private int yPos = -1;

    private int zPos = -1;

    private ArrayList<InputDatum> batchSomNeighbourhood;

    /**
     * Constructs a <code>Unit</code> on <code>Layer</code> specified by argument <code>layer</code> at position
     * <code>x</code>/<code>y</code> with a given weight vector <code>vec</code>.
     * 
     * @param l the layer that contains this <code>Unit</code>.
     * @param x the horizontal position on the <code>layer</code>.
     * @param y the vertical position on the <code>layer</code>.
     * @param vec the weight vector.
     */
    public Unit(Layer l, int x, int y, double[] vec) {
        this(l, x, y, 0, vec);
    }

    /**
     * Constructs a <code>Unit</code> on <code>Layer</code> specified by argument <code>layer</code> at position
     * <code>x</code>/<code>y</code> with a given weight vector <code>vec</code>.
     * 
     * @param l the layer that contains this <code>Unit</code>.
     * @param x the horizontal position on the <code>layer</code>.
     * @param y the vertical position on the <code>layer</code>.
     * @param z the depth position on the <code>layer</code>.
     * @param vec the weight vector.
     */
    public Unit(Layer l, int x, int y, int z, double[] vec) {
        layer = l;
        xPos = x;
        yPos = y;
        zPos = z;
        weightVector = vec;
        dim = vec.length;
        // FIXME: don't initialise this here - if we don't use the batchmode, we just waste memory...
        batchSomNeighbourhood = new ArrayList<InputDatum>();
    }

    /**
     * Constructs a <code>Unit</code> on <code>Layer</code> specified by argument <code>layer</code> at position
     * <code>x</code>/<code>y</code> with a randomly initialized weight vector of dimension <code>d</code>. Argument
     * <code>norm</code> determines whether the weight vector should be normalized or not. TODO: This might be change in
     * the future due to unflexibility regarding hard coded normalization methods.
     * 
     * @param l the layer that contains this <code>Unit</code>.
     * @param x the horizontal position on the <code>layer</code>.
     * @param y the vertical position on the <code>layer</code>.
     * @param d the dimensionality of the weight vector.
     * @param rand a random number generator provided by the caller.
     * @param norm the type of normalization (see text above).
     */
    public Unit(Layer l, int x, int y, int d, Random rand, boolean norm) {
        this(l, x, y, 0, d, rand, norm, INIT_RANDOM);
    }

    /**
     * Constructs a <code>Unit</code> on <code>Layer</code> specified by argument <code>layer</code> at position
     * <code>x</code>/<code>y</code> with a randomly initialized weight vector of dimension <code>d</code>. Argument
     * <code>norm</code> determines whether the weight vector should be normalized or not. TODO: This might be change in
     * the future due to unflexibility regarding hard coded normalization methods.
     * 
     * @param l the layer that contains this <code>Unit</code>.
     * @param x the horizontal position on the <code>layer</code>.
     * @param y the vertical position on the <code>layer</code>.
     * @param z the depth position on the <code>layer</code>.
     * @param d the dimensionality of the weight vector.
     * @param rand a random number generator provided by the caller.
     * @param norm the type of normalization (see text above).
     */
    public Unit(Layer l, int x, int y, int z, int d, Random rand, boolean norm) {
        this(l, x, y, z, d, rand, norm, INIT_RANDOM);
    }

    public Unit(Layer l, int x, int y, int d, Random rand, boolean norm, int initialisationMode) {
        this(l, x, y, 0, d, rand, norm, initialisationMode);
    }

    public Unit(Layer l, int x, int y, int z, int d, Random rand, boolean norm, int initialisationMode) {
        layer = l;
        xPos = x;
        yPos = y;
        zPos = z;
        dim = d;
        batchSomNeighbourhood = new ArrayList<InputDatum>();
        weightVector = new double[dim];
        if (initialisationMode == INIT_RANDOM) {
            for (int i = 0; i < dim; i++) {
                weightVector[i] = rand.nextDouble();
            }
        } else if (initialisationMode == INIT_INTERVAL_INTERPOLATE) {
            for (int i = 0; i < dim; i++) {
                double r = rand.nextDouble();
                double[][] intervals = ((GrowingLayer) l).getData().getDataIntervals();
                weightVector[i] = intervals[i][0] + (intervals[i][1] - intervals[i][0]) * r;
            }
        } else if (initialisationMode == INIT_VECTOR) {
            double r = rand.nextDouble();
            int index = (int) (((GrowingLayer) l).getData().numVectors() * r);
            weightVector = ((GrowingLayer) l).getData().getInputDatum(index).getVector().toArray();
            // System.out.println("Initialised " + x + "/" + y + "as:" + VectorTools.printVector(weightVector));
        } else if (initialisationMode == INIT_PCA) {
            // TODO: do PCA
        }
        if (norm) {
            VectorTools.normaliseVectorToUnitLength(weightVector);
        }
    }

    /**
     * Adds a single input datum to the unit. The method also calculates the distance between the unit's weight vector
     * and the datum.
     * 
     * @param datum the input datum to be added.
     * @param calcQE determines if the quantization error should be recalculated.
     * @see #addMappedInput(String, double, boolean)
     */
    public void addMappedInput(InputDatum datum, boolean calcQE) {
        try {
            double dist = layer.getMetric().distance(datum.getVector(), this.weightVector);
            addMappedInput(datum.getLabel(), dist, calcQE);
        } catch (MetricException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            System.exit(-1); // TODO: EXCEPTION HANDLING!!
        }
    }

    /**
     * Map all the input vectors contained in specified <code>InputData</code> object onto this unit.
     * 
     * @param data The container for input vector
     * @param calcQE determines if the quantization error should be recalculated.
     * @see #addMappedInput(InputDatum, boolean)
     */
    public void addMappedInput(InputData data, boolean calcQE) {
        for (int d = 0; d < data.numVectors(); d++) {
            addMappedInput(data.getInputDatum(d), calcQE);
        }
    }

    /**
     * Convenience method to add an input datum specified by its name and distance. The quantization error is
     * recalculated if argument <code>calcQE</code> is <code>true</code>.
     * 
     * @param name the name of the input datum.
     * @param dist the precalculated distance between input datum and weight vector
     * @param calcQE determines if the quantization error should be recalculated.
     */
    public void addMappedInput(String name, double dist, boolean calcQE) {
        super.addMappedInput(name, new Double(dist));
        if (calcQE) {
            calculateQuantizationError();
        }
    }

    @Override
    public void removeMappedInput(String label) {
        super.removeMappedInput(label);
        calculateQuantizationError();
    }

    /**
     * Recalculates the quantization error for this unit.
     */
    public void calculateQuantizationError() {
        quantizationError = 0;
        for (int i = 0; i < getNumberOfMappedInputs(); i++) {
            quantizationError += getMappedInputDistance(i);
        }
    }

    /**
     * Removes the labels of this unit.
     */
    public void clearLabels() {
        labels = null;
    }

    /**
     * Removes the mapped input data and sets this units quantization error to 0.
     */
    public void clearMappedInput() {
        super.clearMappedInputs();
        quantizationError = 0;
    }

    /**
     * Returns an array of labels or null, if no labels are assigned to this unit.
     * 
     * @return an array of labels or null.
     */
    public Label[] getLabels(String type) {
        if (type == LABELSOM) {
            return getLabels();
        } else if (type == KEYWORDS) {
            return getKaskiLabels();
        } else if (type == GATE) {
            return getKaskiGateLabels();
        } else if (type == CONTEXT) {
            return getBestContextWeights();
        } else {
            return null;
        }
    }

    public Label[] getLabels() {
        return labels;
    }

    public Label[] getKaskiGateLabels() {
        return kaskiGateLabels;
    }

    public Label[] getKaskiLabels() {
        return kaskiLabels;
    }

    public Label[] getGateWeights() {
        return gateWeights;
    }

    public Label[] getBestContextWeights() {
        return bestcontextWeights;
    }

    public Label[] getContextGateLabels() {
        return contextGateLabels;
    }

    /**
     * Returns the layer of units this unit is part of.
     * 
     * @return the layer of units this unit is part of.
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Returns the map identification string of this unit's layer.
     * 
     * @return the map identification string of this unit's layer.
     */
    public String getMapIdString() {
        return layer.getIdString();
    }

    /**
     * Returns the level of this unit's layer in a hierarchy of maps.
     * 
     * @return the level of this unit's layer in a hierarchy of maps.
     */
    public int getMapLevel() {
        return layer.getLevel();
    }

    /**
     * Returns the map assigned to this unit or <code>null</code> otherwise.
     * 
     * @return the map assigned to this unit or <code>null</code> otherwise.
     */
    public GrowingSOM getMappedSOM() {
        return mappedSOM;
    }

    /**
     * Assigns a map to this unit.
     * 
     * @param mappedSOM a map to be assigned to this unit.
     */
    public void setMappedSOM(GrowingSOM mappedSOM) {
        this.mappedSOM = mappedSOM;
    }

    /**
     * Returns the width of this unit's map.
     * 
     * @return the width of this unit's map.
     */
    public int getMapXSize() {
        return layer.getXSize();
    }

    /**
     * Returns the height of this unit's map.
     * 
     * @return the height of this unit's map.
     */
    public int getMapYSize() {
        return layer.getYSize();
    }

    /**
     * Returns the depth of this unit's map.
     * 
     * @return the depth of this unit's map.
     */
    public int getMapZSize() {
        return layer.getZSize();
    }

    /**
     * Calculates and returns the mean quantization error of this unit. This is 0, if no input is mapped onto this unit.
     * 
     * @return the mean quantization error for this unit.
     */
    /*
     * public double getMeanQuantizationError() { if (mappedInputs.getNumberOfMappedInputs()>0) { return
     * (quantizationError/mappedInputs.getNumberOfMappedInputs()); } else { return 0; } }
     */

    /**
     * Returns the quantization error of this unit.
     * 
     * @return the quantization error of this unit.
     */
    /*
     * public double getQuantizationError() { return quantizationError; }
     */

    /**
     * Returns the weight vector of this unit.
     * 
     * @return the weight vector of this unit.
     */
    public double[] getWeightVector() {
        return weightVector;
    }

    /**
     * Sets the weight vector of this unit.
     * 
     * @param vector the weight vector.
     */
    public void setWeightVector(double[] vector) throws SOMToolboxException {
        if (vector != null && vector.length == dim) {
            weightVector = vector;
        } else {
            throw new SOMToolboxException("Vector is null or has wrong dimensionality.");
        }
    }

    /**
     * Returns the horizontal position of this unit on the map it is part of.
     * 
     * @return the horizontal position of this unit on the map it is part of.
     */
    public int getXPos() {
        return xPos;
    }

    /**
     * Returns the vertical position of this unit on the map it is part of.
     * 
     * @return the vertical position of this unit on the map it is part of.
     */
    public int getYPos() {
        return yPos;
    }

    /**
     * Returns the depth position of this unit on the map it is part of.
     * 
     * @return the depth position of this unit on the map it is part of.
     */
    public int getZPos() {
        return zPos;
    }

    /**
     * Sets this unit's weight vector to the vector of the input datum specified by argument <code>datum</code>.
     * 
     * @param datum the input datum.
     */
    public void initWeightVectorBySample(InputDatum datum) {
        weightVector = datum.getVector().toArray();
    }

    /**
     * Restores the labels of a unit based on the information provided by the arguments. The value of argument
     * <code>nrUnitLabels</code> must be equal to the dimensionalities of the arrays specified in the other arguments.
     * If this is not the case, no labels will be restored.
     * 
     * @param nrUnitLabels the number of labels.
     * @param unitLabels an array of strings containing the labels' names.
     * @param unitLabelsQe an array of double values containing the qunatization errors for the single labels.
     * @param unitLabelsWgt an array of double values containing the actual values for the single labels.
     */
    public void restoreLabels(int nrUnitLabels, String[] unitLabels, double[] unitLabelsQe, double[] unitLabelsWgt) {
        if (nrUnitLabels > 0 && unitLabels.length == nrUnitLabels && unitLabelsQe.length == nrUnitLabels
                && unitLabelsWgt.length == nrUnitLabels) {
            labels = new Label[nrUnitLabels];
            for (int i = 0; i < nrUnitLabels; i++) {
                labels[i] = new Label(unitLabels[i], unitLabelsWgt[i], unitLabelsQe[i]);
            }
        }
    }

    public void restoreContextGateLabels(int nrContextGate, String[] contextGateUnitLabels) {
        contextGateLabels = new Label[nrContextGate];
        for (int i = 0; i < nrContextGate; i++) {
            contextGateLabels[i] = new Label(contextGateUnitLabels[i]);
        }
    }

    public void restoreKaskiLabels(int nrKaski, String[] kaskiUnitLabels, double[] kaskiUnitLabelsWgt) {
        kaskiLabels = new Label[nrKaski];
        for (int i = 0; i < nrKaski; i++) {
            kaskiLabels[i] = new Label(kaskiUnitLabels[i], kaskiUnitLabelsWgt[i]);
        }
    }

    public void restoreKaskiGateLabels(int nrKaskiGate, String[] kaskiGateUnitabels) {
        kaskiGateLabels = new Label[nrKaskiGate];
        for (int i = 0; i < nrKaskiGate; i++) {
            kaskiGateLabels[i] = new Label(kaskiGateUnitabels[i]);
        }
    }

    public void restoreGateWeightLabels(int nrgateweights, String[] gateWeightUnitLabels) {
        gateWeights = new Label[nrgateweights];
        for (int i = 0; i < nrgateweights; i++) {
            gateWeights[i] = new Label(gateWeightUnitLabels[i]);
        }
    }

    public void restoreBestContextWeightLabels(int nrbestcontext, String[] bestContextWeightUnitLabels) {
        bestcontextWeights = new Label[nrbestcontext];
        for (int i = 0; i < nrbestcontext; i++) {
            bestcontextWeights[i] = new Label(bestContextWeightUnitLabels[i]);
        }
    }

    /**
     * Restores the mapped input data of a unit based on the information provided by the arguments. The value of
     * argument <code>nrVecsMapped</code> must be equal to the dimensionalities of the arrays specified in the other
     * arguments. If this is not the case, no input data will be restored. The quantization error will also be
     * recalculated.
     * 
     * @param nrVecsMapped the number of input data.
     * @param mappedVecs an array of strings containing the data identifiers.
     * @param mappedVecsDist an array of double values containing the distances between the weight vector and the
     *            respective input data.
     */
    public void restoreMappings(int nrVecsMapped, String[] mappedVecs, double[] mappedVecsDist) {
        for (int i = 0; i < nrVecsMapped; i++) {
            addMappedInput(mappedVecs[i], mappedVecsDist[i], false);
        }
        calculateQuantizationError();
    }

    /**
     * Assigns labels to this unit.
     * 
     * @param labels array of labels to be assigned to this unit.
     */
    public void setLabels(Label[] labels) {
        this.labels = labels;
    }

    public void setKaskiGateLabels(Label[] kaski_gate_labels) {
        this.kaskiGateLabels = kaski_gate_labels;
    }

    public void setContextGateLabels(Label[] context_gate_labels) {
        this.contextGateLabels = context_gate_labels;
    }

    /**
     * Sets the coordinates of this unit on the map, if they have changed. This happens in architectures with growing
     * map sizes during training.
     * 
     * @param x the horizontal position on the map.
     * @param y the vertical position on the map.
     * @param z the height position on the map.
     */
    public void updatePosition(int x, int y, int z) {
        xPos = x;
        yPos = y;
        zPos = z;
    }

    public void updatePosition(int x, int y) {
        updatePosition(x, y, 0);
    }

    public void addBatchSomNeighbour(InputDatum d) {
        batchSomNeighbourhood.add(d);
    }

    public void clearBatchSomList() {
        batchSomNeighbourhood.clear();
    }

    public void getWeightVectorFromBatchSomNeighbourhood() {
        double meanValue;
        for (int i = 0; i < weightVector.length; i++) {
            meanValue = 0;
            for (int j = 0; j < batchSomNeighbourhood.size(); j++) {
                meanValue += batchSomNeighbourhood.get(j).getVector().get(i);
            }
            meanValue = meanValue / batchSomNeighbourhood.size();
            weightVector[i] = meanValue;
        }
    }

    @Override
    public String toString() {
        return "Unit[" + printCoordinates() + "]";
    }

    public String printCoordinates() {
        if (getMapZSize() > 1) {
            return xPos + "/" + yPos + "/" + zPos;
        } else {
            return xPos + "/" + yPos;
        }
    }

    public String printCoordinatesSpaceSeparated() {
        if (getMapZSize() > 1) {
            return xPos + " " + yPos + " " + zPos;
        } else {
            return xPos + " " + yPos;
        }
    }

    public String printUnitDetails(InputData inputData, TemplateVector tv) {
        StringBuffer sb = new StringBuffer("Unit details for ").append(getXPos()).append("/").append(getYPos()).append(
                ", ").append(getNumberOfMappedInputs()).append(" mapped inputs:\n");
        final int numSpace = 6;
        final int firstColumnSpace = 10;
        final int numDigits = numSpace - 3;
        if (tv != null) {
            sb.append(StringUtils.getSpaces(firstColumnSpace + 1));
            for (int i = 0; i < tv.dim(); i++) {
                String label = tv.getLabel(i).length() >= numSpace ? tv.getLabel(i).substring(0, numSpace - 1)
                        : tv.getLabel(i);
                sb.append(label).append(StringUtils.getSpaces(numSpace - label.length()));
            }
            sb.append("\n");
        }
        final String headerWeightVector = "WeightVec";
        final String headerFeatureWeight = "FeatWeight";
        sb.append(headerWeightVector).append(StringUtils.getSpaces(firstColumnSpace - headerWeightVector.length())).append(
                StringUtils.toStringWithPrecision(getWeightVector(), numDigits)).append("\n");
        if (featureWeights != null) {
            sb.append(headerFeatureWeight).append(
                    StringUtils.getSpaces(firstColumnSpace - headerFeatureWeight.length())).append(
                    StringUtils.toStringWithPrecision(featureWeights, numDigits)).append("\n");
        }
        if (getNumberOfMappedInputs() > 0) {
            for (String label : getMappedInputNames()) {
                sb.append(label).append(StringUtils.getSpaces(firstColumnSpace - label.length()));
                if (inputData != null && inputData.getInputDatum(label) != null) {
                    sb.append(
                            StringUtils.toStringWithPrecision(inputData.getInputDatum(label).getVector().toArray(),
                                    numDigits)).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public double[] getFeatureWeights() {
        return featureWeights;
    }

    public void setFeatureWeights(double[] featureWeights) {
        this.featureWeights = featureWeights;
    }

    public void copyFeatureWeights(double[] featureWeights) {
        for (int i = 0; i < featureWeights.length; i++) {
            this.featureWeights[i] = featureWeights[i];
        }
    }

    public int getDim() {
        return dim;
    }

    public String getUnitLabels() {
        StringBuffer label = new StringBuffer();
        if (labels != null) {
            for (int i = 0; i < labels.length; i++) {
                if (i > 0) {
                    label.append(", ");
                }
                label.append(labels[i].getName());
            }
        }
        if (labels == null || labels.length == 0) {
            label.append("&lt;no labels available&gt;");
        }
        return label.toString();
    }

    void setPositions(int x, int y, int z) {
        this.xPos = x;
        this.yPos = y;
        this.yPos = y;
    }

    public boolean isTopLeftUnit() {
        return xPos == 0 && yPos == 0 && zPos == 0;
    }

}
