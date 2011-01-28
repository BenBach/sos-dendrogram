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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.database.MySQLConnector;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * Implements {@link InputData} by reading the vector information from a database.
 * 
 * @author liegl
 * @author Rudolf Mayer
 * @version $Id: DataBaseSOMLibSparseInputData.java 3883 2010-11-02 17:13:23Z frank $
 */
public class DataBaseSOMLibSparseInputData extends AbstractSOMLibSparseInputData {

    private MySQLConnector dbConnector;

    public DataBaseSOMLibSparseInputData(MySQLConnector dbConnector, boolean sparse, boolean norm, int numCacheBlocks,
            long seed) {
        super(norm, new Random(seed));
        nameCache = new LinkedHashMap<String, Integer>();
        this.dbConnector = dbConnector;

        readVectorMetaDataFromDataBase();
        templateVector = new DataBaseSOMLibTemplateVector(dbConnector);
    }

    public DataBaseSOMLibSparseInputData(MySQLConnector dbConnector, boolean sparse, String classInfoFileName,
            boolean norm, int numCacheBlocks, long seed) throws SOMToolboxException {
        this(dbConnector, sparse, norm, numCacheBlocks, seed);
        classInfo = new SOMLibClassInformation(classInfoFileName);
    }

    /**
     * Read input meta data from the database. This includes:
     * <ul>
     * <li>Number of input vectors (<code>numVectors()</code>)</li>
     * <li>Vector dimension (<code>dim()</code>)</li>
     * <li>The input data labels / names (<code>dataNames</code>)</li>
     * <li>Initialising the name cache (<code>nameCache</code>)</li>
     * <li>Calculating the mean vector (<code>getMeanVector()</code>)</li>
     * </ul>
     */
    private void readVectorMetaDataFromDataBase() {
        try {
            // Get the dimension of the matrix

            // a.) Get the number of documents (numVectors)
            String query = "SELECT COUNT(*) FROM " + dbConnector.getDocumentTableName();
            ResultSet r = dbConnector.executeSelect(query);
            while (r.next()) {
                this.numVectors = r.getInt(1);
            }

            // b.) Get the number of terms (dim)
            query = "SELECT COUNT(*) FROM " + dbConnector.getTermTableName();
            r = dbConnector.executeSelect(query);
            while (r.next()) {
                this.dim = r.getInt(1);
            }

            dataNames = new String[numVectors];

            // initialize mean vector
            calculateMeanVector();

            query = "SELECT label FROM " + dbConnector.getDocumentTableName() + " ORDER BY number ASC";
            r = dbConnector.executeSelect(query);
            int j = 0;
            while (r.next()) {
                // Fill the array dataNames with the labels of the document
                String label = r.getString(1);
                dataNames[j] = label;

                // insert into nameCache
                nameCache.put(label, new Integer(j++));
            }
        } catch (SQLException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(getErrorMessage(e));
            System.exit(1);
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Input vector file format seems to be correct. Riding on ...");
    }

    @Override
    public InputDatum getInputDatum(int index) {
        DoubleMatrix1D dmatr = readVectorFromDataBase(index);

        // Create InputDatum with the information retrieved from the database and return it
        return new InputDatum(dataNames[index], dmatr); // data.viewRow(d));
    }

    @Override
    public double[] getInputVector(int d) {
        return readVectorFromDataBase(d).toArray();
    }

    /**
     * Read a vector from the database, identified by the given index.
     * 
     * @param rowindex the index of the vector.
     * @return the input vector.
     */
    private DoubleMatrix1D readVectorFromDataBase(int rowindex) {
        DoubleMatrix1D dmatr = null;

        try {
            // Build the query
            String query = "SELECT termNumber, weightNormalised from " + dbConnector.getDocumentTermsTableName()
                    + " WHERE documentNumber=" + rowindex + " ORDER BY termNumber ASC";
            ResultSet r = dbConnector.executeSelect(query);
            // Build the vector
            dmatr = new DenseDoubleMatrix1D(dim);
            while (r.next()) {
                dmatr.set(r.getInt(1), r.getDouble(2));
            }
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(getErrorMessage(e));
            System.exit(-1);
        }

        return dmatr;
    }

    @Override
    public double mqe0(DistanceMetric metric) {
        if (mqe0 == -1) { // mqe0 for data was not yet calculated
            mqe0 = 0;
            try {
                for (int i = 0; i < numVectors; i++) {
                    DenseDoubleMatrix1D dm = (DenseDoubleMatrix1D) this.readVectorFromDataBase(i);
                    mqe0 += metric.distance(meanVector, dm);
                }
            } catch (MetricException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                System.exit(-1);
            }
        }
        return mqe0;
    }

    /**
     * TODO: this reads the whole subset into the memory.
     * 
     * @see at.tuwien.ifs.somtoolbox.data.InputData#subset(java.lang.String[])
     */
    @Override
    public InputData subset(String[] names) {

        SparseDoubleMatrix2D newData = new SparseDoubleMatrix2D(names.length, dim);

        for (int i = 0; i < names.length; i++) {
            try {
                int index = nameCache.get(names[i]).intValue();
                // newData.viewRow(i).assign(data.viewRow(index));
                newData.viewRow(i).assign(this.readVectorFromDataBase(index));
            } catch (NullPointerException e) {
                return null;
            }
        }

        DataBaseSOMLibSparseInputData res = null; // TODO: implement
        return res;
    }

    /** Initialise the mean-vector. */
    protected void calculateMeanVector() throws SQLException {
        meanVector = new DenseDoubleMatrix1D(dim);

        // if the input matrix is 11.000 x 5340 then the mean vector is 5340
        // where every line is the sum of one column of the input matrix divided by the number of terms (5340)

        String query = "SELECT SUM(weight)/COUNT(*) FROM  " + dbConnector.getDocumentTermsTableName()
                + " GROUP BY termNumber";
        ResultSet resultMean = dbConnector.executeSelect(query);
        int j = 0;
        while (resultMean.next()) {
            meanVector.set(j++, resultMean.getDouble(1));
        }
    }

    private String getErrorMessage(Exception e) {
        return "An error occured while communicating with database: '" + e.getMessage() + "'. Aborting.";
    }

    @Override
    public double getValue(int x, int y) {
        return getInputDatum(x).getVector().get(y);
    }

}
