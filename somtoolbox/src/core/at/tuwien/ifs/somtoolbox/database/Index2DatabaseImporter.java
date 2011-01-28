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
package at.tuwien.ifs.somtoolbox.database;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import at.ec3.DoubleMatrix;
import at.ec3.IntMatrix;

import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * Reads data from a TeSeTool generated (Lucene-based) index and writes it to a database.
 * 
 * @author Rudolf Mayer
 * @version $Id: Index2DatabaseImporter.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class Index2DatabaseImporter {
    public static final String[] templateFields = new String[] { "number", "label", "documentFrequency",
            "collectionTermFrequency", "minimumTermFrequency", "maximumTermFrequency", "meanTermFrequency", "comment" };

    public static final String[] documentTableFields = new String[] { "number", "label" };

    public static final String[] documentTermTableFields = new String[] { "documentNumber", "termNumber",
            "rawTermFrequency", "weight", "weightNormalised" };

    MySQLConnector dbConnector;

    private PreparedStatement documentTermPreparedStatement;

    public Index2DatabaseImporter(String databaseServerAddress, String databaseName, String user, String password,
            String databaseTableNamePrefix) throws SQLException, IOException {
        dbConnector = new MySQLConnector(databaseServerAddress, databaseName, user, password, databaseTableNamePrefix);
        dbConnector.setupTables();

    }

    public void writeTemplateVector(IntMatrix tfMatrix, Vector<String> selectedTerms, HashMap<Integer, Object> allTerms)
            throws SQLException {
        Map<Object, Integer> reversedTermsMap = VectorTools.reverseHashMap(allTerms);
        // write the template vector
        StdErrProgressWriter progress = new StdErrProgressWriter(selectedTerms.size(), "Writing template vector ", 10);
        int index = 0;
        for (int i = 0; i < selectedTerms.size(); i++) {
            progress.progress();
            if (allTerms.containsValue(selectedTerms.get(i))) {
                int terminmatrix = Integer.parseInt(reversedTermsMap.get(selectedTerms.get(i)).toString());
                String label = (String) allTerms.get(new Integer(terminmatrix));
                Integer df = new Integer(tfMatrix.getColumnCardinality(terminmatrix));
                Integer tf = new Integer(tfMatrix.getSumColumnValue(terminmatrix));
                Integer min = new Integer(tfMatrix.getminColumnValue(terminmatrix));
                Integer max = new Integer(tfMatrix.getmaxColumnValue(terminmatrix));
                Double mean = new Double(tf.doubleValue() / df.doubleValue());

                Object[] values = new Object[] { new Integer(index), label, df, tf, min, max, mean, "" };
                try {
                    dbConnector.doInsert(dbConnector.getTermTableName(), templateFields, values);
                    index++;
                } catch (SQLException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "Error in communicating with the database for element " + index + ": '" + e.getMessage()
                                    + "'. Aborting.");
                    e.printStackTrace();
                    System.out.println("label: " + label);
                    System.out.println("index: " + i);
                    try {
                        System.err.println(URLDecoder.decode(label, "UTF8"));
                    } catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    System.exit(-1);
                }
            }
        }
    }

    private void initDocumentTermPreparedStatement() throws SQLException {
        StringBuffer sql = new StringBuffer(50 + documentTermTableFields.length * +documentTermTableFields.length * 10);
        sql.append("INSERT into ").append(dbConnector.getDocumentTermsTableName()).append(
                " (documentNumber, termNumber, rawTermFrequency, weight, weightNormalised)");
        sql.append(" VALUES (?,?,?,?,?);");
        this.documentTermPreparedStatement = dbConnector.getPreparedStatement(sql.toString());
    }

    private void executeDocumentTermInsert(int documentNumber, int termNumber, int rawTermFrequency, double weight,
            double weightNormalised) throws SQLException {
        documentTermPreparedStatement.setInt(1, documentNumber);
        documentTermPreparedStatement.setInt(2, termNumber);
        documentTermPreparedStatement.setInt(3, rawTermFrequency);
        documentTermPreparedStatement.setDouble(4, weight);
        documentTermPreparedStatement.setDouble(5, weightNormalised);
        documentTermPreparedStatement.execute();
    }

    /**
     * Writes the vector from a term-frequency matrix.
     */
    public void writeInputVector(IntMatrix tfMatrix, Vector<String> selectedTerms, HashMap<Integer, Object> allTerms,
            HashMap<Integer, Object> labelMap) throws SQLException {
        System.out.println("Writing input vector, calculating on the fly");
        initDocumentTermPreparedStatement();
        int documentNumber = labelMap.size();
        Map<Object, Integer> terms_hm_reverse = new HashMap<Object, Integer>();

        boolean reduced = false;
        if (selectedTerms.size() != allTerms.size()) {
            reduced = true;
        }

        int[] documentFrequencies = new int[selectedTerms.size()];
        if (reduced) {
            StdErrProgressWriter progressDf = new StdErrProgressWriter(documentFrequencies.length,
                    "Calculating df values ", 10);
            System.out.println("\n\nreduced, calcucalting hashmap\n");
            terms_hm_reverse = VectorTools.reverseHashMap(allTerms);
            for (int i = 0; i < selectedTerms.size(); i++) {
                progressDf.progress();
                int terminmatrix = terms_hm_reverse.get(selectedTerms.get(i)).intValue();
                documentFrequencies[terminmatrix] = tfMatrix.getColumnCardinality(terminmatrix);
            }
        } else {
            StdErrProgressWriter progressDf = new StdErrProgressWriter(documentFrequencies.length,
                    "Calculating df values ", 10);
            for (int i = 0; i < documentFrequencies.length; i++) {
                progressDf.progress();
                documentFrequencies[i] = tfMatrix.getColumnCardinality(i);
            }
        }

        StdErrProgressWriter progress = new StdErrProgressWriter(tfMatrix.rows(), "Writing input vector ", 5);
        for (int i = 0; i < tfMatrix.rows(); i++) {
            dbConnector.doInsert(dbConnector.getDocumentTableName(), documentTableFields, new Object[] {
                    new Integer(i), labelMap.get(new Integer(i)).toString() });
            int[] tfs = new int[selectedTerms.size()];
            double[] weights = new double[selectedTerms.size()];
            double[] weightsNormalised = new double[selectedTerms.size()];
            if (reduced) {
                for (int j = 0; j < selectedTerms.size(); j++) {
                    int terminmatrix = terms_hm_reverse.get(selectedTerms.get(j)).intValue();
                    tfs[j] = tfMatrix.get(i, terminmatrix);
                    double fraq = (double) documentNumber / (double) documentFrequencies[terminmatrix];
                    weights[j] = tfs[j] * Math.log(fraq);
                }
            } else {
                for (int j = 0; j < allTerms.size(); j++) {
                    tfs[j] = tfMatrix.get(i, j);
                    if (tfs[j] > 0) {
                        double fraq = (double) documentNumber / (double) documentFrequencies[j];
                        weights[j] = tfs[j] * Math.log(fraq);
                    }
                }
            }
            weightsNormalised = VectorTools.normaliseByLength(weights);
            // Object[][] values = new Object[tfs.length][];
            for (int termIndex = 0; termIndex < tfs.length; termIndex++) {
                // values[termIndex] = new Object[] { new Integer(i), new Integer(termIndex), new
                // Double(tfs[termIndex]),
                // new Double(weights[termIndex]), new Double(weightsNormalised[termIndex]) };
                // dbConnector.doInsert(dbConnector.getDocumentTermsTableName(), documentTermTableFields,
                // new Object[] { new Integer(i), new Integer(termIndex), new Double(tfs[termIndex]), new
                // Double(weights[termIndex]),
                // new Double(weightsNormalised[termIndex]) });
                executeDocumentTermInsert(i, termIndex, tfs[termIndex], weights[termIndex],
                        weightsNormalised[termIndex]);
            }
            // dbConnector.doInsert(dbConnector.getDocumentTermsTableName(), documentTermTableFields, values);

            // force some garbage collection
            // if (i % 50 == 0) {
            // Runtime rt = Runtime.getRuntime();
            // long mem = rt.freeMemory();
            // System.gc();
            // System.out.println("Ran garbage collection. Freed: " + StringUtils.readableBytes(rt.freeMemory() - mem) +
            // ". Total in use: "
            // + StringUtils.readableBytes(rt.totalMemory() - rt.freeMemory()));
            // }
            progress.progress();
        }
    }

    /**
     * Writes the input vectors from an already caluclated tfxidf matrix.
     */
    public void writeInputVector(DoubleMatrix inputVectorMatrix, HashMap<Integer, Object> labelMap) throws SQLException {
        StdErrProgressWriter progress = new StdErrProgressWriter(inputVectorMatrix.rows(), "Writing input vector ");
        for (int i = 0; i < inputVectorMatrix.rows(); i++) {
            progress.progress();
            dbConnector.doInsert(dbConnector.getDocumentTableName(), documentTableFields, new Object[] {
                    new Integer(i), labelMap.get(new Integer(i)).toString() });
            double[] st = inputVectorMatrix.getRow(i);
            for (int termIndex = 0; termIndex < st.length; termIndex++) {
                dbConnector.doInsert(dbConnector.getDocumentTermsTableName(), documentTermTableFields, new Object[] {
                        new Integer(i), new Integer(termIndex), new Double(st[termIndex]) });
            }

        }
    }

}
