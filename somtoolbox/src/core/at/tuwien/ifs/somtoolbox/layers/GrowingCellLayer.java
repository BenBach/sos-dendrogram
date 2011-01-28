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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.JFrame;

import cern.colt.matrix.DoubleMatrix1D;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.layers.quality.AbstractQualityMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;
import at.tuwien.ifs.somtoolbox.models.GrowingCellStructures;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;
import at.tuwien.ifs.somtoolbox.util.ProgressListener;
import at.tuwien.ifs.somtoolbox.util.ProgressListenerFactory;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.comparables.UnitDistance;
import at.tuwien.ifs.somtoolbox.util.growingCellStructures.GrowingCellDrawSurface;
import at.tuwien.ifs.somtoolbox.util.growingCellStructures.GrowingCellTetraheder;

/**
 * @author Johannes Inführ
 * @author Andreas Zweng
 * @version $Id: GrowingCellLayer.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class GrowingCellLayer implements Layer {
    // member variables
    /** List of Units that get trained */
    private List<GrowingCellUnit> units;

    /** List of Tetraheders formed by the Units */
    private List<GrowingCellTetraheder> tetraheders;

    /** Data used for training */
    private InputData data;

    /** Dimensions of the input-data */
    private int dim;

    /** QualityMesure used for GrowingCellStructures */
    private QualityMeasure qualityMeasure;

    /** Distance Metric used for GrowingCellStructures */
    private DistanceMetric metric;

    /** Storagearray for Units for export */
    private GrowingCellUnit unitfield[][];

    /** Weight-Vector for Units that fill holes in unitfield */
    private double[] emptyUnitMarkVector;

    /** Random Nr Generator for Weight-Vector initialisation */
    private Random rand;

    // Options for Cell-SOM

    /** Adaption Factor of best matching unit */
    private float epsilonB;

    /** Adaption Factor of neighbors of best matching unit */
    private float epsilonN;

    /** Reduction Factor for signal counter */
    private float alpha;

    /** Number of adaption steps between adding/removing of Units */
    private int lamda;

    /** Cutoff-Value for normalized probability density */
    private float eta;

    /** Number of Iterations on the input data */
    private int maxEpochs;

    // Options for visualization
    /** Frame used to display the CellUnits */
    private JFrame display;

    /** Scaling Factor for gravity between Units */
    private double attractScale = 0.10;

    /** Scaling Factor for repulsion between Units */
    private double repellingScale = 0.2;

    /** Scaling Factor for gravity between Units independent of connectedness/distance */
    private double coherenceForceFactor = 0.001;

    /**
     * Std. Constructor for Cell Layers
     * 
     * @param dim Dimensions of the InputData
     * @param normalize Wheter or not the Data is normalized
     * @param randomSeed Seed to use for random components
     * @param data InputData to be used for training
     */
    public GrowingCellLayer(int dim, boolean normalize, long randomSeed, InputData data) {
        units = new LinkedList<GrowingCellUnit>();
        unitfield = null;
        tetraheders = new LinkedList<GrowingCellTetraheder>();

        emptyUnitMarkVector = new double[dim];
        for (int i = 0; i < dim; i++) {
            emptyUnitMarkVector[i] = Double.NEGATIVE_INFINITY;
        }

        rand = new Random(randomSeed);

        this.dim = dim;
        this.data = data;

        try {
            metric = AbstractMetric.instantiate("at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initializing the Display
        display = new JFrame("displ");
        display.setSize(800, 600);
        display.setContentPane(new GrowingCellDrawSurface());
        display.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        display.setVisible(true);

    }

    @Override
    public Unit[] getAllUnits() {
        Unit[] ret = new Unit[units.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = units.get(i);
        }

        return ret;
    }

    @Override
    public double[][] getComponentPlane(int component) {
        return getComponentPlane(component, 0);
    }

    @Override
    public double[][] getComponentPlane(int component, int z) {
        if (component >= 0 && component < dim) {
            double[][] ret = new double[getXSize()][getYSize()];
            for (int j = 0; j < getYSize(); j++) {
                for (int i = 0; i < getXSize(); i++) {
                    try {
                        ret[i][j] = getUnit(i, j, z).getWeightVector()[component];
                    } catch (LayerAccessException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                }
            }
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public String getIdString() {
        return "CellLayer";
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public double getMapDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
    }

    @Override
    public double getMapDistance(Unit u1, Unit u2) {
        return getMapDistance(u1.getXPos(), u1.getYPos(), u1.getZPos(), u2.getXPos(), u2.getYPos(), u2.getZPos());
    }

    @Override
    public DistanceMetric getMetric() {
        return metric;
    }

    @Override
    public int getNumberOfMappedInputs() {
        int count = 0;
        for (GrowingCellUnit u : units) {
            count += u.getNumberOfMappedInputs();
        }
        return count;
    }

    @Override
    public QualityMeasure getQualityMeasure() {
        return qualityMeasure;
    }

    @Override
    public String getRevision() {
        return "1.0";
    }

    @Override
    public Unit getUnit(int x, int y) throws LayerAccessException {
        return getUnit(x, y, 0);
    }

    @Override
    public Unit getUnit(int x, int y, int z) throws LayerAccessException {
        if (z != 0) {
            throw new LayerAccessException();
        }

        return unitfield[y][x];
    }

    @Override
    public Unit getUnitForDatum(String name) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Access to unimplemented method");
        return null;
    }

    @Override
    public int getXSize() {
        if (unitfield != null) {
            if (unitfield[0] != null) {
                return unitfield[0].length;
            }
        }
        return 0;
    }

    @Override
    public int getYSize() {
        if (unitfield != null) {
            return unitfield.length;
        }
        return 0;
    }

    @Override
    public int getZSize() {
        return 1;
    }

    public UnitDistance[] getWinnersAndDistances(InputDatum input, int numWinners) {
        if (numWinners > getXSize() * getYSize() * getZSize()) {
            numWinners = getXSize() * getYSize() * getZSize();
        }
        UnitDistance[] res = new UnitDistance[numWinners];
        DoubleMatrix1D vec = input.getVector();
        // FIXME: this algorithm should be optimisable, especially the inserting
        // part!
        for (int k = 0; k < getZSize(); k++) {
            for (int j = 0; j < getYSize(); j++) {
                for (int i = 0; i < getXSize(); i++) {
                    Unit u = null;
                    try {
                        u = getUnit(i, j, k);
                    } catch (LayerAccessException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    if (u == null) {
                        continue;
                    }

                    double distance = 0;
                    try {
                        distance = metric.distance(u.getWeightVector(), vec);
                    } catch (Exception e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                    int element = 0;
                    boolean inserted = false;
                    while (inserted == false && element < numWinners) {
                        if (res[element] == null || distance < res[element].getDistance()) { // found place to insert
                            // unit
                            for (int m = numWinners - 2; m >= element; m--) { // move units with greater distance to
                                // right
                                res[m + 1] = res[m];
                            }

                            res[element] = new UnitDistance(u, distance);

                            inserted = true;
                        }
                        element++;
                    }

                }
            }
        }
        return res;
    }

    public int getDim() {
        return dim;
    }

    public Collection<? extends GrowingCellStructures> getAllSubMaps() {
        return new ArrayList<GrowingCellStructures>();// no submaps available
    }

    /**
     * Trains the CellLayer with the given Parameters and returns the QualityMeasure
     * 
     * @param data InputData
     * @param epsilonB Adaption factor for best matching unit
     * @param epsilonN Adaption factor for neighbours of best matching unit
     * @param alpha Reduction factor for Signalcount
     * @param lamda Interval between Adding/Removing of Units
     * @param eta Cutoff-Value for normalised probability density
     * @param props Additional properties to use (numIterations==maximum number of epochs, quality measure name)
     */
    public QualityMeasure train(InputData data, float epsilonB, float epsilonN, float alpha, int lamda, float eta,
            SOMProperties props) {
        // set training parameters
        this.maxEpochs = props.numIterations();
        this.epsilonB = epsilonB;
        this.epsilonN = epsilonN;
        this.alpha = alpha;
        this.lamda = lamda;
        this.eta = eta;

        String qualityMeasureName = props.growthQualityMeasureName();

        if (qualityMeasureName == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "No quality measure provided for training. Using mean quantization error of map.");
            qualityMeasureName = "at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError.mqe";
        }
        String[] qmNameMethod = AbstractQualityMeasure.splitNameAndMethod(qualityMeasureName);

        // set up first tetraheder with 3 units
        GrowingCellUnit[] initUnits = new GrowingCellUnit[3];

        for (int j = 0; j < 3; j++) {
            double[] weights = new double[data.dim()];

            // random vector initialisation
            for (int i = 0; i < weights.length; i++) {
                double r = rand.nextDouble();

                double[][] intervals = data.getDataIntervals();
                weights[i] = intervals[i][0] + (intervals[i][1] - intervals[i][0]) * r;
            }

            initUnits[j] = new GrowingCellUnit(this, weights);
        }

        GrowingCellTetraheder initTetraheder = new GrowingCellTetraheder(initUnits[0], initUnits[1], initUnits[2]);

        // add constructed tetraheder + units
        tetraheders.add(initTetraheder);
        units.add(initUnits[0]);
        units.add(initUnits[1]);
        units.add(initUnits[2]);

        // place the units (in the visualization)
        initUnits[0].putAtPosition(10, 10);
        initUnits[1].putAtPosition(20, 20);
        initUnits[2].putAtPosition(15, 20);

        // move the units to a stable position (in visualization)
        updatePositions();

        ProgressListener progressWriter = ProgressListenerFactory.getInstance().createProgressListener(maxEpochs,
                "Epoche ", 50, maxEpochs / 200);

        // do actual training
        trainNormal(data, progressWriter);

        // shrink unitcoordinates for suitable output
        prepareForOutput();

        // fixed size, just map the data and finish
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Training done.");
        clearMappedInput();
        mapCompleteData(data);

        QualityMeasure qm = null;

        // calc QualityMeasure
        try {
            qm = AbstractQualityMeasure.instantiate(qmNameMethod[0], this, data);
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not instantiate quality measure.");
            System.exit(-1);
        }

        this.qualityMeasure = qm;
        return qm;
    }

    /**
     * Maps data to the units
     * 
     * @param data InputData to map
     */
    private void mapCompleteData(InputData data) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start mapping data.");
        InputDatum datum = null;
        Unit winner = null;
        int numVectors = data.numVectors();
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors, "Mapping datum ", 50);
        for (int i = 0; i < numVectors; i++) {
            datum = data.getInputDatum(i);
            winner = getWinner(datum);
            if (winner == null) {
                System.err.println("getwinner null???");
            }
            winner.addMappedInput(datum, false);
            progressWriter.progress();
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished mapping data.");
    }

    /**
     * Clears the mapped Input of every unit
     */
    private void clearMappedInput() {
        for (Unit u : units) {
            u.clearMappedInput();
        }
    }

    /**
     * Trains the CellLayer with the secified InputData and logs to progressWriter
     * 
     * @param data Data used for training
     * @param progressWriter ProgressListener for logging
     */
    private void trainNormal(InputData data, ProgressListener progressWriter) {
        int noOfSignals = 0;
        int maxSignals = maxEpochs * data.numVectors();

        while (true) {
            noOfSignals++;
            if (noOfSignals == maxSignals) {
                break;
            }

            // get new input
            InputDatum currentInput = data.getRandomInputDatum(noOfSignals, maxSignals);

            // get winner & update weight vectors
            final GrowingCellUnit winner = getWinner(currentInput);
            updateWinnerAndNeighbors(winner, currentInput);

            // decrease the signal counters
            decreaseSignalCounters();

            if (noOfSignals % lamda == 0)// adding & removing of Units every lamda signals
            {
                // Adding Unit
                GrowingCellUnit sfwinner = getSignalFrequencyWinner();
                GrowingCellUnit sfpartner = getFarthestNeighbor(sfwinner);

                double[] sfwinnerweights = sfwinner.getWeightVector();
                double[] sfpartnerweights = sfpartner.getWeightVector();
                double[] newWeights = new double[dim];

                // create interpolated weight vector
                for (int i = 0; i < dim; i++) {
                    newWeights[i] = 0.5 * (sfwinnerweights[i] + sfpartnerweights[i]);
                }

                // create unit and put it at an interpolated position in visualization
                GrowingCellUnit newUnit = new GrowingCellUnit(this, newWeights);
                newUnit.putAtPosition((sfwinner.getX() + sfpartner.getX()) / 2.0,
                        (sfwinner.getY() + sfpartner.getY()) / 2.0);
                units.add(newUnit);

                // calculate old voronoi estimate of top neighs of new node
                List<GrowingCellUnit> neighborsToBe = getInsertionTopologicalNeighbors(sfwinner, sfpartner);
                calculateVoronoiEstimate(neighborsToBe);

                // insert unit
                List<GrowingCellTetraheder> touchedTetraheders = getTouchedTetraheders(sfwinner, sfpartner);

                // add new unit into the existing net of units
                for (GrowingCellTetraheder t : touchedTetraheders) {
                    GrowingCellUnit z = t.getRemainingUnit(sfwinner, sfpartner);

                    GrowingCellTetraheder n1 = new GrowingCellTetraheder(sfwinner, newUnit, z);
                    GrowingCellTetraheder n2 = new GrowingCellTetraheder(sfpartner, newUnit, z);

                    sfwinner.disconnect(t);
                    sfpartner.disconnect(t);
                    z.disconnect(t);

                    tetraheders.remove(t);
                    tetraheders.add(n1);
                    tetraheders.add(n2);
                }

                // calculate new voronoi estimates, update signal counter
                updateInsertSignalCounters(newUnit, neighborsToBe);

                // Removing units
                List<GrowingCellUnit> unitsToRemove = new LinkedList<GrowingCellUnit>();

                calculateVoronoiEstimate(units); // estimates for all units are needed

                // compute sum of signal frequencies and voronoi region estimates
                double sumoffrequency = 0;
                double sumofVoronoiEstimates = 0;
                for (GrowingCellUnit u : units) {
                    sumoffrequency += u.getSignalCounter();
                    sumofVoronoiEstimates += u.getVoronoiEstimate();
                }

                // calculate units to remove
                for (GrowingCellUnit u : units) {
                    double relativeSignalFrequency = u.getSignalCounter() / sumoffrequency;
                    double probabilityDensityEstimate = relativeSignalFrequency / u.getVoronoiEstimate();
                    double normalizedPDA = probabilityDensityEstimate * sumofVoronoiEstimates;

                    if (normalizedPDA < eta) {
                        unitsToRemove.add(u);
                    }

                    if (unitsToRemove.size() > units.size() / 20 - 1) {
                        // System.out.println("saving undeserving units");
                        break;
                    }
                }

                // TODO: Bad Hack for very few input vectors (animals), prevents shrinking to zero when one useless unit
                // kills the rest
                if (units.size() > 10 && unitsToRemove.size() > 0) {
                    // System.out.print("found "+unitsToRemove.size()+" of "+units.size()+" units to remove\n");
                    removeUnits(unitsToRemove);
                }

                // update cellunit positions in display-space
                updatePositions();

                // checkConsistency();//check consistency of actions, used for dbg
            }

            if (noOfSignals % data.numVectors() == 0)// one epoch
            {
                progressWriter.progress();
            }
        }
    }

    /**
     * Update the Position of Units in display-space by means of a simple physics simulation
     */
    private void updatePositions() {
        boolean changed = false;
        int itcnt = 0;
        do {
            changed = false;
            itcnt++;
            if (itcnt > 1)// hard limit of 10 iterations to stop calculation in bigger networks
            {
                // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Break during layout of nodes");
                break;
            }
            for (GrowingCellUnit unitToMove : units)// move every unit
            {
                double[] force = { 0, 0 };// accumulator of acting forces

                for (GrowingCellUnit unitForcing : units)// calculate repelling force
                {
                    if (unitToMove.equals(unitForcing)) {
                        continue;
                    }

                    addRepellingForce(unitToMove, unitForcing, force);
                }

                for (GrowingCellUnit neig : getTopologicalNeighbors(unitToMove))// calculate attracting force
                {
                    addAttractingForce(unitToMove, neig, force);
                }

                double[] coherenceForce = { 0, 0 };
                for (GrowingCellUnit unitForcing : units) {
                    if (unitToMove.equals(unitForcing)) {
                        continue;
                    }

                    addAttractingForce(unitToMove, unitForcing, coherenceForce);
                }

                if (Math.abs(force[0]) > 0.5 || Math.abs(force[1]) > 0.5) {
                    changed = true;
                }

                // unitToMove.setApplicapleForce(force);

                unitToMove.applyMovement(force[0], force[1]);// apply forces
                unitToMove.applyMovement(coherenceForce[0] * coherenceForceFactor, coherenceForce[1]
                        * coherenceForceFactor);
            }

            // for(CellUnit u:units)u.applySavedForce();

        } while (changed);

        // System.out.println("needed its:"+itcnt);

        correctUnitPositions();
        clearMappedInput();
        mapCompleteData(data);
        ((GrowingCellDrawSurface) display.getContentPane()).drawTheCells(tetraheders.toArray());
        // for(CellUnit u:units)System.out.println(u);

    }

    /**
     * Correct Unit Positions so that they stay at the upper left corner (10,10) and don't drift away
     */
    private void correctUnitPositions() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;

        // calculate min x and y of positions
        for (GrowingCellUnit u : units) {
            if (u.getX() < minX) {
                minX = u.getX();
            }
            if (u.getY() < minY) {
                minY = u.getY();
            }
        }

        // calculate translation to upper left corner
        double difx = 0, dify = 0;
        difx = -minX + 10;
        dify = -minY + 10;

        // apply
        for (GrowingCellUnit u : units) {
            u.putAtPosition(u.getX() + difx, u.getY() + dify);
        }
    }

    /**
     * Calculate the attracting force between unitToMove and unitForcing and store it in attractingForce
     * 
     * @param unitToMove unit to be moved
     * @param unitForcing unit which applies the force
     * @param attractingForce vector to add the resulting force to
     */
    private void addAttractingForce(GrowingCellUnit unitToMove, GrowingCellUnit unitForcing, double[] attractingForce) {
        int dia = unitToMove.getDiameter();

        double xdif = unitToMove.getX() - unitForcing.getX();
        double ydif = unitToMove.getY() - unitForcing.getY();

        double d = Math.pow(xdif * xdif + ydif * ydif, 0.5);

        // force proportional to distance if more than diameter away
        double force = 0;
        if (d >= dia) {
            force = (d - dia) / 2;
        }

        double xnorm = -1 * xdif / d;
        double ynorm = -1 * ydif / d;

        double attrX = attractScale * xnorm * force;
        double attrY = attractScale * ynorm * force;

        attractingForce[0] += attrX;
        attractingForce[1] += attrY;
    }

    /**
     * Calculate the repelling force between unitToMove and unitForcing and store it in force
     * 
     * @param unitToMove unit to be moved
     * @param unitForcing unit which applies the fore
     * @param force vector to add the resulting force to
     */
    private void addRepellingForce(GrowingCellUnit unitToMove, GrowingCellUnit unitForcing, double[] force) {
        int dia = unitToMove.getDiameter();

        double xdif = unitToMove.getX() - unitForcing.getX();
        double ydif = unitToMove.getY() - unitForcing.getY();

        double d = Math.pow(xdif * xdif + ydif * ydif, 0.5);

        double forceMagnitude = 0;
        if (3 * dia < d) {
            forceMagnitude = 0;
        }
        if (2 * dia < d && d <= 3 * dia) {
            forceMagnitude = dia / 5.0;
        }
        if (dia < d && d <= 2 * dia) {
            forceMagnitude = dia / 2.0;
        }
        if (0 < d && d <= dia) {
            forceMagnitude = dia;
        }
        if (d == 0) {
            forceMagnitude = 0;
        }

        double xnorm = xdif / d;
        double ynorm = ydif / d;

        double repelX = repellingScale * xnorm * forceMagnitude;
        double repelY = repellingScale * ynorm * forceMagnitude;

        force[0] += repelX;
        force[1] += repelY;
    }

    /**
     * removes units specified in unitsToRemove and possible resulting disconnected units
     * 
     * @param unitsToRemove the units that need to be removed
     */
    private void removeUnits(List<GrowingCellUnit> unitsToRemove) {
        // int allowedPrimaryRemovals=(int)0.05*units.size(); schon wieder ein hidden parameter
        // int removals=0;

        for (GrowingCellUnit u : unitsToRemove)// for all units that need to be removed
        {
            for (GrowingCellTetraheder t : u.getConnectedTetraheders())// check all tetraheders the unit is connected to
            {
                for (GrowingCellUnit connU : t.getCellUnits())// for every unit in that tetraeder
                {
                    if (!connU.equals(u)) {
                        connU.disconnect(t);// disconnect that unit (if its not the unit to be removed)
                    }
                    if (connU.getConnectedTetraheders().size() == 0)// remove that unit to if it has no more connected
                    // tetraheders
                    {
                        units.remove(connU);
                    }
                }
                tetraheders.remove(t); // remove the tetraeder
            }

            units.remove(u);// remove the unit
            // removals++;
            // if(removals>=allowedPrimaryRemovals)return;
        }
    }

    /** Checks consistency of tetraheders and units */
    private void checkConsistency() {
        HashSet<GrowingCellUnit> knownUnits = new HashSet<GrowingCellUnit>();

        for (GrowingCellTetraheder t : tetraheders) {
            for (GrowingCellUnit u : t.getCellUnits()) {
                knownUnits.add(u);
            }
        }

        for (GrowingCellUnit u : units) {
            if (!knownUnits.remove(u)) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("surplus units");
            }
        }

        if (!(knownUnits.size() == 0)) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("unknown units");
        }

        HashSet<GrowingCellTetraheder> knownTetraheder = new HashSet<GrowingCellTetraheder>();

        for (GrowingCellUnit u : units) {
            for (GrowingCellTetraheder t : u.getConnectedTetraheders()) {
                knownTetraheder.add(t);
            }
        }

        for (GrowingCellTetraheder t : tetraheders) {
            if (!knownTetraheder.remove(t)) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("surplus tetraeder");
            }
        }

        if (!(knownTetraheder.size() == 0)) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("unknown tetraeder");
        }

        for (GrowingCellUnit u : units) {
            if (u.getConnectedTetraheders().size() == 0) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("disconnected node");
            }
        }
    }

    /**
     * Updates the SignalCounters after an insert of a new unit
     * 
     * @param newUnit the new inserted unit
     * @param neighbors the neighbors of that unit
     */
    private void updateInsertSignalCounters(GrowingCellUnit newUnit, List<GrowingCellUnit> neighbors) {
        double sumOfFreqDeltas = 0;
        List<GrowingCellUnit> topneighs = getTopologicalNeighbors(newUnit);

        // decreasing signal count of neighbors
        for (GrowingCellUnit u : neighbors) {
            // sanity check
            if (!topneighs.remove(u)) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("got neighbor which is not topological");
            }

            double newVoronoiEstimate = calculateVoronoiEstimate(u);
            double freqDelta = (newVoronoiEstimate - u.getVoronoiEstimate()) / u.getVoronoiEstimate()
                    * u.getSignalCounter();

            if (Double.isNaN(freqDelta)) {
                freqDelta = 0;
            }

            sumOfFreqDeltas += freqDelta;

            u.setSignalCounter(u.getSignalCounter() + freqDelta);
        }

        if (topneighs.size() != 0)// sanity check
        {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("missed neighbor(s)");
        }

        // set signal count of new unit
        newUnit.setSignalCounter(-sumOfFreqDeltas);
    }

    /**
     * Sets the voronoiEstimate of all neighborsToBe
     * 
     * @param neighborsToBe Units that get an updated estimate
     */
    private void calculateVoronoiEstimate(List<GrowingCellUnit> neighborsToBe) {
        for (GrowingCellUnit u : neighborsToBe) {
            u.setVoronoiEstimate(calculateVoronoiEstimate(u));
        }
    }

    /**
     * Calculates the voronoi region estimate for unit u
     * 
     * @param u Unit to calculate the estimate for
     * @return the calculated voronoi region volume estimate
     */
    private double calculateVoronoiEstimate(GrowingCellUnit u) {
        double[] weight = u.getWeightVector();
        List<GrowingCellUnit> neighsOfU = getTopologicalNeighbors(u);

        double sumOfDist = 0;
        for (GrowingCellUnit n : neighsOfU) {
            sumOfDist += getDistance(weight, n.getWeightVector());
        }

        double avgDist = sumOfDist / neighsOfU.size();
        double voronoiEstimate = Math.pow(avgDist, dim);// approx by dim hypercube of avgdist edgelenght

        return voronoiEstimate;
    }

    /**
     * Returns Tetraheders that have u1 and u2 in common
     * 
     * @param u1 CellUnit 1
     * @param u2 CellUnit 2
     * @return Tetraheders which are connected to u1 and u2
     */
    private List<GrowingCellTetraheder> getTouchedTetraheders(GrowingCellUnit u1, GrowingCellUnit u2) {
        List<GrowingCellTetraheder> touchedTetraheders = new LinkedList<GrowingCellTetraheder>();

        for (GrowingCellTetraheder t : u1.getConnectedTetraheders()) {
            if (t.contains(u2)) {
                touchedTetraheders.add(t);
            }
        }

        return touchedTetraheders;
    }

    /** * @return CellUnits which will be neighbors of a new unit between sfwinner and sfpartner */
    private List<GrowingCellUnit> getInsertionTopologicalNeighbors(GrowingCellUnit sfwinner, GrowingCellUnit sfpartner) {
        List<GrowingCellUnit> topolNeighbors = new LinkedList<GrowingCellUnit>();

        topolNeighbors.add(sfwinner);
        topolNeighbors.add(sfpartner);

        for (GrowingCellTetraheder t : sfwinner.getConnectedTetraheders()) {
            if (t.contains(sfpartner)) {
                topolNeighbors.add(t.getRemainingUnit(sfwinner, sfpartner));
            }
        }

        return topolNeighbors;
    }

    /**
     * @return CellUnit with highest signal frequency
     */
    private GrowingCellUnit getSignalFrequencyWinner() {
        // compute sum
        double sumoffrequency = 0;

        for (GrowingCellUnit u : units) {
            sumoffrequency += u.getSignalCounter();
        }

        // calculate freq
        double bestRelativeFrequ = 0;
        GrowingCellUnit bestUnit = null;

        for (GrowingCellUnit u : units) {
            if (u.getSignalCounter() / sumoffrequency > bestRelativeFrequ) {
                bestRelativeFrequ = u.getSignalCounter() / sumoffrequency;
                bestUnit = u;
            }
        }

        return bestUnit;
    }

    /**
     * Decrease SignalCoutners of units by alpha percent
     */
    private void decreaseSignalCounters() {
        for (GrowingCellUnit u : units) {
            u.setSignalCounter(u.getSignalCounter() - alpha * u.getSignalCounter());
        }
    }

    /**
     * Updates the weight-vectors of winning unit and its neighbors
     * 
     * @param winner the winning unit
     * @param currentInput current input vector
     */
    private void updateWinnerAndNeighbors(GrowingCellUnit winner, InputDatum currentInput) {
        double[] weightWinner = winner.getWeightVector();
        double[] inputWeights = currentInput.getVector().toArray();

        // update of winner
        for (int i = 0; i < dim; i++) {
            weightWinner[i] = weightWinner[i] + epsilonB * (inputWeights[i] - weightWinner[i]);
        }
        winner.setSignalCounter(winner.getSignalCounter() + 1);

        // update of topological neighbors
        List<GrowingCellUnit> neighbors = getTopologicalNeighbors(winner);

        for (GrowingCellUnit neighbor : neighbors) {
            // neighbor.setSignalCounter(neighbor.getSignalCounter()+0.3);//TODO: is das gut so?

            double[] weightNeighbor = neighbor.getWeightVector();

            for (int i = 0; i < dim; i++) {
                weightNeighbor[i] = weightNeighbor[i] + epsilonN * (inputWeights[i] - weightNeighbor[i]);
            }
        }
    }

    /** @return the topological neighbours of the unit */
    private List<GrowingCellUnit> getTopologicalNeighbors(GrowingCellUnit unit) {
        List<GrowingCellUnit> neighbors = new ArrayList<GrowingCellUnit>();

        for (GrowingCellTetraheder ct : unit.getConnectedTetraheders()) {
            GrowingCellUnit[] unitsOfTetraheder = ct.getCellUnits();

            for (GrowingCellUnit u : unitsOfTetraheder) {
                if (u == unit) {
                    continue;// self not neighbor
                }
                if (neighbors.contains(u)) {
                    continue;// no duplicates
                }
                neighbors.add(u);
            }
        }

        return neighbors;
    }

    /** @return unit closest to input */
    private GrowingCellUnit getWinner(InputDatum input) {
        GrowingCellUnit winner = null;
        double smallestDistance = Double.MAX_VALUE;
        double[] inputVector = input.getVector().toArray();

        for (GrowingCellUnit u : units) {
            double distance = getDistance(u.getWeightVector(), inputVector);

            if (distance < smallestDistance) {
                smallestDistance = distance;
                winner = u;
            }
        }

        return winner;
    }

    /** @return topological neighbor with the largest distance to unit */
    private GrowingCellUnit getFarthestNeighbor(GrowingCellUnit unit) {
        GrowingCellUnit farthest = null;
        double largestDistance = 0;
        double[] unitWeights = unit.getWeightVector();

        for (GrowingCellUnit u : getTopologicalNeighbors(unit)) {
            double distance = getDistance(u.getWeightVector(), unitWeights);

            if (distance > largestDistance) {
                largestDistance = distance;
                farthest = u;
            }
        }

        return farthest;
    }

    /** @return Distance between v1 and v2 */
    private double getDistance(double[] v1, double[] v2) {
        double distance = 0;
        try {
            distance = metric.distance(v1, v2);
        } catch (MetricException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            System.exit(-1);
        }

        return distance;
    }

    /**
     * @return InputData that is used for training
     */
    public InputData getData() {
        return data;
    }

    /**
     * Moves the units for optimized space requirement The order along x and y axis stays the same, but the space
     * between units gets reduced to 0, x is reduced before y so a narrow stripe along the y axis will emerge
     */
    private void prepareForOutput() {
        int[] dimandoff = new int[4];
        analyzeDimension(units, dimandoff);
        int xdim = dimandoff[0];
        int ydim = dimandoff[1];
        int xoff = dimandoff[2];
        int yoff = dimandoff[3];

        unitfield = new GrowingCellUnit[ydim][xdim];

        for (GrowingCellUnit u : units) {
            unitfield[u.getYPos() - yoff][u.getXPos() - xoff] = u.clone();
        }

        // compact the rows
        for (int rowind = 0; rowind < ydim; rowind++) {
            GrowingCellUnit[] row = unitfield[rowind];
            int firstEmptyColumn = -1;
            for (int colindex = 0; colindex < xdim; colindex++) {
                if (firstEmptyColumn == -1 && row[colindex] == null) {
                    firstEmptyColumn = colindex;
                }
                if (firstEmptyColumn != -1) {
                    if (row[colindex] == null) {
                        continue;
                    }

                    row[firstEmptyColumn] = row[colindex];
                    row[colindex] = null;

                    firstEmptyColumn++;
                }
            }
        }

        // compact the columns
        for (int colind = 0; colind < xdim; colind++) {
            int firstEmptyRow = -1;
            for (int rowind = 0; rowind < ydim; rowind++) {
                if (firstEmptyRow == -1 && unitfield[rowind][colind] == null) {
                    firstEmptyRow = rowind;
                }
                if (firstEmptyRow != -1) {
                    if (unitfield[rowind][colind] == null) {
                        continue;
                    }

                    unitfield[firstEmptyRow][colind] = unitfield[rowind][colind];
                    unitfield[rowind][colind] = null;

                    firstEmptyRow++;
                }
            }
        }

        // set the new coordinates
        for (int y = 0; y < ydim; y++) {
            for (int x = 0; x < xdim; x++) {
                if (unitfield[y][x] != null) {
                    unitfield[y][x].putAtPosition(x, y);
                }
            }
        }

        analyzeDimension(unitfield, dimandoff);
        xdim = dimandoff[0];
        ydim = dimandoff[1];
        xoff = dimandoff[2];
        yoff = dimandoff[3];

        // create a unitfield of the right (reduced) size and copy units
        GrowingCellUnit[][] unitfieldnew = new GrowingCellUnit[ydim][xdim];
        for (GrowingCellUnit[] line : unitfield) {
            for (GrowingCellUnit u : line) {
                if (u != null) {
                    unitfieldnew[u.getYPos()][u.getXPos()] = u;
                }
            }
        }

        unitfield = unitfieldnew;

        // fill blank spots with emptyUnitMarkVector
        for (int y = 0; y < ydim; y++) {
            for (int x = 0; x < xdim; x++) {
                if (unitfield[y][x] == null) {
                    unitfield[y][x] = new GrowingCellUnit(this, emptyUnitMarkVector);
                    unitfield[y][x].putAtPosition(x, y);
                }
            }
        }
    }

    /**
     * Analyses maximum extension of the Units in data and their offset
     * 
     * @param data array with units (with null Objects)
     */
    private void analyzeDimension(GrowingCellUnit[][] data, int dimandoffset[]) {
        Integer minx = Integer.MAX_VALUE;
        Integer miny = Integer.MAX_VALUE;
        Integer maxx = Integer.MIN_VALUE;
        Integer maxy = Integer.MIN_VALUE;

        for (GrowingCellUnit[] line : data) {
            for (GrowingCellUnit u : line) {
                if (u == null) {
                    continue;
                }

                if (u.getXPos() < minx) {
                    minx = u.getXPos();
                }
                if (u.getXPos() > maxx) {
                    maxx = u.getXPos();
                }
                if (u.getYPos() < miny) {
                    miny = u.getYPos();
                }
                if (u.getYPos() > maxy) {
                    maxy = u.getYPos();
                }
            }
        }

        // for(CellUnit u:units)
        // {
        // if(u.getXPos()<minx)minx=u.getXPos();
        // if(u.getXPos()>maxx)maxx=u.getXPos();
        // if(u.getYPos()<miny)miny=u.getYPos();
        // if(u.getYPos()>maxy)maxy=u.getYPos();
        // }

        dimandoffset[0] = maxx - minx + 1;
        dimandoffset[1] = maxy - miny + 1;

        dimandoffset[2] = minx;
        dimandoffset[3] = miny;

        System.out.println("would simulate (" + dimandoffset[0] + "," + dimandoffset[1] + ") , " + dimandoffset[0]
                * dimandoffset[1] + " units, really: " + units.size());
    }

    /**
     * Analyses maximum extension of the Units in units and their offset
     * 
     * @param units List of Units without null elements
     */
    private void analyzeDimension(List<GrowingCellUnit> units, int dimandoffset[]) {
        Integer minx = Integer.MAX_VALUE;
        Integer miny = Integer.MAX_VALUE;
        Integer maxx = Integer.MIN_VALUE;
        Integer maxy = Integer.MIN_VALUE;

        for (GrowingCellUnit u : units) {
            if (u.getXPos() < minx) {
                minx = u.getXPos();
            }
            if (u.getXPos() > maxx) {
                maxx = u.getXPos();
            }
            if (u.getYPos() < miny) {
                miny = u.getYPos();
            }
            if (u.getYPos() > maxy) {
                maxy = u.getYPos();
            }
        }

        dimandoffset[0] = maxx - minx + 1;
        dimandoffset[1] = maxy - miny + 1;

        dimandoffset[2] = minx;
        dimandoffset[3] = miny;

        System.out.print("\nwould simulate (" + dimandoffset[0] + "," + dimandoffset[1] + ") , " + dimandoffset[0]
                * dimandoffset[1] + " units, really: " + units.size() + "\n");
    }

    @Override
    public GridLayout getGridLayout() {
        return GridLayout.triangular;
    }

    @Override
    public GridTopology getGridTopology() {
        return GridTopology.planar;
    }
}
