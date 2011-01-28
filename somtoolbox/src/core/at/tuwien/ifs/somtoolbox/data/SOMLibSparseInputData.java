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
package at.tuwien.ifs.somtoolbox.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.jet.math.Functions;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Implements {@link InputData} based on a SOMLib <a
 * href="http://olymp.ifs.tuwien.ac.at/somtoolbox/doc/somlibFileFormat.html#input_vectors">Input Vector File</a>.
 * 
 * @author Michael Dittenbach
 * @version $Id: SOMLibSparseInputData.java 3971 2010-12-15 13:18:39Z mayer $
 */
public class SOMLibSparseInputData extends AbstractSOMLibSparseInputData {
    public static final String INPUT_VECTOR_FILE_FORMAT_CORRUPT_MESSAGE = "Input vector file corrupt in vector number ";

    public static final boolean DEFAULT_NORMALISED = true;

    public static final int DEFAULT_NUM_CACHE_BLOCKS = 1;

    public static final int DEFAULT_RANDOM_SEED = 7;

    public static final boolean DEFAULT_SPARSE = true;

    private boolean containsMissingValues = false;

    /** Counts how many of the feature values are not zero; stores an int value for each vector in the input data. */
    protected int[] nonZeros;

    protected boolean sparse;

    /**
     * The actual data. Each row in the matrix represents one vector.
     */
    protected DoubleMatrix2D data = null;

    private int ydim = 1;

    /** Constructor intended for generated synthetic data. */
    public SOMLibSparseInputData(InputDatum[] inputData, SOMLibClassInformation classInfo) {
        String[] dataNames = new String[inputData.length];
        DenseDoubleMatrix2D data = new DenseDoubleMatrix2D(inputData.length, inputData[0].getDim());
        for (int i = 0; i < dataNames.length; i++) {
            // System.out.println(i + " + " + inputData[i]);
            dataNames[i] = inputData[i].getLabel();
            DoubleMatrix1D vector = inputData[i].getVector();
            for (int j = 0; j < vector.size(); j++) {
                data.setQuick(i, j, vector.getQuick(j));
            }
        }
        initFromExistingData(data, dataNames, false, new Random(), null, classInfo);
        nonZeros = new int[inputData.length];
    }

    /**
     * Constructor intended for subset generation.
     */
    protected SOMLibSparseInputData(DoubleMatrix2D data, String[] dataNames, boolean norm, Random rand,
            TemplateVector tv, SOMLibClassInformation clsInfo) {
        initFromExistingData(data, dataNames, norm, rand, tv, clsInfo);
    }

    private void initFromExistingData(DoubleMatrix2D data, String[] dataNames, boolean norm, Random rand,
            TemplateVector tv, SOMLibClassInformation clsInfo) {
        this.data = data;
        this.dataNames = dataNames;
        this.dim = data.columns();
        this.numVectors = dataNames.length;
        this.isNormalized = norm;

        nameCache = new LinkedHashMap<String, Integer>();
        meanVector = new DenseDoubleMatrix1D(dim);
        for (int i = 0; i < dataNames.length; i++) {
            meanVector.assign(data.viewRow(i), Functions.plus); // add to mean vector
            nameCache.put(dataNames[i], new Integer(i));
        }
        meanVector.assign(Functions.div(numVectors)); // calculating mean vector

        this.rand = rand;
        this.templateVector = tv;
        this.classInfo = clsInfo;
    }

    /**
     * Uses default values for sparsity (<code>true</code>), normalisation (<code>true</code>), chacheblocks (
     * <code>1</code>) and seed (<code>7</code> ).
     */
    public SOMLibSparseInputData(String vectorFileName) {
        this(vectorFileName, DEFAULT_SPARSE, DEFAULT_NORMALISED, DEFAULT_NUM_CACHE_BLOCKS, DEFAULT_RANDOM_SEED);
    }

    public SOMLibSparseInputData(String vectorFileName, boolean sparse, boolean norm, int numCacheBlocks, long seed) {
        source = vectorFileName;
        init(sparse, norm, seed);
        readVectorFile(vectorFileName, sparse);

        // TODO: use SVD infos for map size determination, write standalone class
        // System.out.print("Singular Values: ");
        // SingularValueDecomposition svd = new SingularValueDecomposition(data.viewDice());
        // double[] sv = svd.getSingularValues();
        // for (int i = 0; i < sv.length; i++) {
        // System.out.print(sv[i] + ", ");
        // }
        // System.out.println();
    }

    public void init(boolean sparse, boolean norm, long seed) {
        isNormalized = norm;
        nameCache = new LinkedHashMap<String, Integer>();
        rand = new Random(seed);
        this.sparse = sparse;
    }

    public SOMLibSparseInputData(String vectorFileName, String templateFileName) {
        this(vectorFileName, templateFileName, DEFAULT_SPARSE, DEFAULT_NORMALISED, DEFAULT_NUM_CACHE_BLOCKS,
                DEFAULT_RANDOM_SEED);
    }

    public SOMLibSparseInputData(String vectorFileName, String templateFileName, boolean sparse, boolean norm,
            int numCacheBlocks, long seed) {
        this(vectorFileName, sparse, norm, numCacheBlocks, seed);

        try {
            if (templateFileName == null) {
                templateVector = new SOMLibTemplateVector(numVectors, dim); // initialize new default Template Vector
            } else {
                templateVector = new SOMLibTemplateVector(templateFileName);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (dim != templateVector.dim()) {
            String errorMessage = "Dimensionalities in input vector file and template vector file differ (" + dim
                    + " != " + templateVector.dim() + ". Aborting.";
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public SOMLibSparseInputData(String vectorFileName, String templateFileName, String classInfoFileName)
            throws SOMToolboxException {
        this(vectorFileName, templateFileName, classInfoFileName, DEFAULT_SPARSE, DEFAULT_NORMALISED,
                DEFAULT_NUM_CACHE_BLOCKS, DEFAULT_RANDOM_SEED);
    }

    public SOMLibSparseInputData(String vectorFileName, String templateFileName, String classInfoFileName,
            boolean sparse, boolean norm, int numCacheBlocks, long seed) throws SOMToolboxException {
        this(vectorFileName, templateFileName, sparse, norm, numCacheBlocks, seed);
        if (classInfoFileName != null) {
            classInfo = new SOMLibClassInformation(classInfoFileName);
        }
    }

    protected SOMLibSparseInputData() {
        super();
    }

    @Override
    public InputDatum getInputDatum(int index) {
        return new InputDatum(dataNames[index], data.viewRow(index), nonZeros[index]);
    }

    @Override
    public double[] getInputVector(int d) {
        return data.viewRow(d).toArray();
    }

    @Override
    public double getValue(int x, int y) {
        return data.get(x, y);
    }

    @Override
    public double mqe0(DistanceMetric metric) {
        if (mqe0 == -1) { // mqe0 for data was not yet calculated
            mqe0 = 0;
            try {
                for (int i = 0; i < numVectors; i++) {
                    mqe0 += metric.distance(meanVector, data.viewRow(i));
                }
            } catch (MetricException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return mqe0;
    }

    /**
     * Reads the input data from the given file, which has to follow the <a
     * href="http://olymp.ifs.tuwien.ac.at/somtoolbox/doc/somlibFileFormat.html#input_vectors">Input Vector File</a>
     * specification. Additionally calculates the {@link AbstractSOMLibSparseInputData#meanVector} and creates the
     * {@link AbstractSOMLibSparseInputData#nameCache} for faster index search.
     * 
     * @param vectorFileName the name of the input vector file.
     */
    protected void readVectorFile(String vectorFileName, boolean sparse) {
        BufferedReader br = openFile(vectorFileName);
        String line = null;
        int lineNumber = 0;

        try {
            // PROCESS HEADER with arbitrary number of comment lines & lines starting with $
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.startsWith("#") || line.equals("")) { // ignore comments and empty lines
                    continue;
                }
                if (!line.startsWith("$")) {
                    break;
                }

                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length < 2) {
                    String msg = "Header in input vector file corrupt in line #" + lineNumber
                            + ": less than two elements!";
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(msg);
                    throw new IOException(msg);
                }
                String header = lineElements[0];
                String content = lineElements[1];

                if (header.equals("$TYPE")) {
                    // do nothing
                } else if (header.equals("$DATA_TYPE")) {
                    // determine type of vector data, if data is audio set sparsity to false
                    String[] subtypes = content.split("-", 2);
                    content_type = subtypes[0];
                    if (subtypes.length > 1) {
                        content_subtype = subtypes[1];
                    }
                    if (content_type.equals("audio")) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                                "Content type = audio. Setting sparsity to false.");
                        sparse = false;
                    }
                } else if (header.equals("$DATA_DIM")) {
                    String[] strDataDim = content.split("x", 2);
                    featureMatrixRows = Integer.parseInt(strDataDim[0]);
                    featureMatrixCols = Integer.parseInt(strDataDim[1]);

                } else if (header.equals("$XDIM")) {
                    numVectors = Integer.parseInt(content);
                } else if (header.equals("$YDIM")) {
                    ydim = Integer.parseInt(content);
                } else if (header.equals("$VEC_DIM") || header.equals("$VECDIM")) {
                    dim = Integer.parseInt(content);
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Unkown Header line '" + line + "', ingoring.");
                }
            }

            numVectors *= ydim;

            // PROCESS REMAINDER OF FILE

            initDataStructures(sparse);

            int index = 0;
            StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors, "Reading input datum ", 10);

            while (line != null) {
                if (!line.equals("")) {
                    // sanity check for numVectors
                    if (index >= numVectors) {
                        String errorMessage = "Input vector file corrupt. Incorrect number of vectors: header says "
                                + numVectors + ", but already reading vector " + (index + 1) + ". Aborting.";
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(errorMessage);
                        throw new IOException(errorMessage);
                    }

                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB, dim + 1);
                    if (lineElements.length != dim + 1) {
                        String msg = INPUT_VECTOR_FILE_FORMAT_CORRUPT_MESSAGE + (index + 1)
                                + ": dimension specified is " + dim + ", found " + (lineElements.length - 1)
                                + ". Aborting.";
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(msg);
                        throw new IOException(msg);
                    } else { // vector syntax ok. checking number format and calculating meanVector.
                        try {
                            processLine(index, lineElements);
                        } catch (NumberFormatException e) {
                            String msg = INPUT_VECTOR_FILE_FORMAT_CORRUPT_MESSAGE + (index + 1) + " (line #"
                                    + lineNumber + "): " + e.getMessage() + ". Aborting.";
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(msg);
                            throw new IOException(msg);
                        }
                    }
                    progressWriter.progress();
                    index++;
                }
                line = br.readLine();
                lineNumber++;
            }

            if (containsMissingValues) {
                System.out.println("\n\n");
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Input data file contained missing values - be sure to handle them correctly in sub-sequent steps!\n\n");
            }

            if (index != numVectors) {
                String errorMessage = "Input vector file corrupt. Incorrect number of vectors: header says "
                        + numVectors + ", but read " + index + ". Aborting.";
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(errorMessage);
                throw new IOException(errorMessage);
            } else { // file is sane
                meanVector.assign(Functions.div(numVectors)); // calculating mean vector
            }
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ERROR_MESSAGE_FILE_FORMAT_CORRUPT);
            throw new IllegalArgumentException(e.getMessage());
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Input vector file format seems to be correct. Riding on ...");
    }

    protected void initDataStructures(boolean sparse) {
        initMatrix(sparse);
        dataNames = new String[numVectors];
        nonZeros = new int[numVectors];
        // initialize mean vector
        meanVector = new DenseDoubleMatrix1D(dim);
    }

    protected void initMatrix(boolean sparse) {
        if (sparse == true) {
            data = new SparseDoubleMatrix2D(numVectors, dim);

        } else {
            data = new DenseDoubleMatrix2D(numVectors, dim);
        }
    }

    protected static BufferedReader openFile(String vectorFileName) {
        try {
            return FileUtils.openFile("Input vector file", vectorFileName);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Process a single line of the input vector file.
     * 
     * @param index the line index
     * @param lineElements the line elements, split by the delimeters
     */
    protected void processLine(int index, String[] lineElements) throws Exception {
        for (int ve = 0; ve < dim; ve++) {
            setMatrixValue(index, ve, parseDouble(lineElements[ve]));
        }
        addInstance(index, lineElements[dim]);
    }

    protected double parseDouble(String s) {
        if (s.trim().equals("?")) {
            containsMissingValues = true;
            return MISSING_VALUE;
        }
        return Double.parseDouble(s);
    }

    protected void setMatrixValue(int row, int column, double value) {
        data.setQuick(row, column, value);
        if (value != 0.0d) {
            nonZeros[row]++;
        }
    }

    protected void addInstance(int index, String label) {
        // avoid heading or trailing spaces --> can create problems with DB driven vectors that do not store those
        // spaces
        dataNames[index] = label.trim();

        // insert into nameCache
        nameCache.put(label, new Integer(index));
        /*
         * if (isNormalized==Normalization.UNIT_LEN) { // create normalized vector before adding to meanVec
         * Normalization.normalizeRowToUnitLength(data, index-1); }
         */

        meanVector.assign(data.viewRow(index), Functions.plus); // add to mean vector
    }

    @Override
    public InputData subset(String[] names) {
        SparseDoubleMatrix2D newData = new SparseDoubleMatrix2D(names.length, dim);
        int[] nonZerosNew = new int[names.length];

        for (int i = 0; i < names.length; i++) {
            try {
                int index = nameCache.get(names[i]).intValue();
                newData.viewRow(i).assign(data.viewRow(index));
                nonZerosNew[i] = this.nonZeros[index];
            } catch (NullPointerException e) {
                return null;
            }
        }

        SOMLibSparseInputData res = new SOMLibSparseInputData(newData, names, isNormalized, rand, templateVector,
                classInfo);
        res.nonZeros = nonZerosNew;
        return res;
    }

    /** Method for stand-alone execution, prints useful information about the input data. */
    public static void main(String[] args) throws Exception {
        // register and parse all options for the AttendeeMapper
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptInputVectorFile(true));
        String inputFileName = config.getString("inputVectorFile");
        SOMLibSparseInputData libSparseInputData = new SOMLibSparseInputData(inputFileName);
        Hashtable<Integer, Integer> featureDensities = libSparseInputData.getFeatureDensities();
        ArrayList<Integer> arrayList = new ArrayList<Integer>(featureDensities.keySet());
        Collections.sort(arrayList);
        for (int i = 0; i < arrayList.size(); i++) {
            System.out.println(arrayList.get(i) + ": " + featureDensities.get(arrayList.get(i)));
        }
    }

    public static long getDimensionality(String vectorFileName) {
        BufferedReader br = openFile(vectorFileName);
        String line = null;
        int numVectors = 0;
        int ydim = 1;
        int dim = 0;

        try {
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.equals("")) { // ignore comments and empty lines
                    continue;
                }
                if (!line.startsWith("$")) {
                    break;
                }
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length < 2) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Header in input vector file corrupt!");
                    throw new IOException("Header in input vector file corrupt!");
                }
                String content = lineElements[1];
                if (lineElements[0].equals("$XDIM")) {
                    numVectors = Integer.parseInt(content);
                } else if (lineElements[0].equals("$YDIM")) {
                    ydim = Integer.parseInt(content);
                } else if (lineElements[0].startsWith("$VEC_DIM") || lineElements[0].startsWith("$VECDIM")) {
                    dim = Integer.parseInt(content);
                }
            }
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ERROR_MESSAGE_FILE_FORMAT_CORRUPT);
            throw new IllegalArgumentException(e.getMessage());
        }
        return numVectors * ydim * dim;
    }

    public void setLabel(int index, String name) {
        dataNames[index] = name;
    }

}
