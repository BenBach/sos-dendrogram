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
import java.sql.SQLException;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;

/**
 * Imports input and template vector files to a database.
 * 
 * @author Rudolf Mayer
 * @version $Id: VectorFile2DatabaseImporter.java 3683 2010-07-15 09:13:01Z frank $
 */
public class VectorFile2DatabaseImporter implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputVectorFile(true),
            OptionFactory.getOptTemplateVectorFile(true), OptionFactory.getOptDatabaseName(true),
            OptionFactory.getOptDatabaseTableNamePrefix(true), OptionFactory.getOptDatabaseServerAddress(false),
            OptionFactory.getOptDatabaseUser(false), OptionFactory.getOptDatabasePassword(false) };

    public static final String DESCRIPTION = "Imports input and template vector files to a database";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Helper;

    /**
     * This class customises the handling of data read from the file by storing it in the DB.
     * 
     * @author Rudolf Mayer
     */
    private class InputVectorImporter extends SOMLibSparseInputData {

        private MySQLConnector dbConnector;

        public InputVectorImporter(String inputVectorFile, MySQLConnector dbConnector) {
            super();
            this.dbConnector = dbConnector;
            readVectorFile(inputVectorFile, false);
        }

        /**
         * Stores the information read in the database.
         */
        @Override
        protected void processLine(int documentIndex, String[] lineElements) throws Exception {
            String label = lineElements[dim].trim();
            dbConnector.doInsert(dbConnector.getDocumentTableName(), new String[] { "number", "label" }, new Object[] {
                    new Integer(documentIndex), label });
            for (int termIndex = 0; termIndex < dim; termIndex++) {
                dbConnector.doInsert(dbConnector.getDocumentTermsTableName(), new String[] { "documentNumber",
                        "termNumber", "weight" }, new Object[] { new Integer(documentIndex), new Integer(termIndex),
                        Double.valueOf(lineElements[termIndex]) });
            }
        }
    }

    /**
     * This class customises the handling of data read from the file by storing it in the DB.
     * 
     * @author Rudolf Mayer
     */
    private class TemplateVectorImporter extends SOMLibTemplateVector {

        private MySQLConnector dbConnector;

        public TemplateVectorImporter(String templateFileName, MySQLConnector dbConnector) throws IOException {
            super();
            this.dbConnector = dbConnector;
            readTemplateVectorFile(templateFileName);
        }

        /**
         * Stores the information read in the database.
         */
        @Override
        protected void processLine(int index, String[] lineElements) {
            super.processLine(index, lineElements);
            try {
                String[] fields = new String[] { "number", "label", "documentFrequency", "collectionTermFrequency",
                        "minimumTermFrequency", "maximumTermFrequency", "meanTermFrequency", "comment" };
                Object[] values = new Object[] { index, elements[index].getLabel(),
                        elements[index].getDocumentFrequency(), elements[index].getCollectionTermFrequency(),
                        elements[index].getMinimumTermFrequency(), elements[index].getMaximumTermFrequency(),
                        elements[index].getMeanTermFrequency(), elements[index].getComment() };
                dbConnector.doInsert(dbConnector.getTermTableName(), fields, values);
            } catch (SQLException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Error in communicating with the database for element " + index + ": '" + e.getMessage()
                                + "'. Aborting.");
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Erronous label name: " + elements[index].getLabel());
                e.printStackTrace();
                try {
                    System.err.println(URLDecoder.decode(elements[index].getLabel(), "UTF8"));
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                System.exit(-1);
            }
        }
    }

    /**
     * Starts the import to the database.
     * 
     * @param args Needed program arguments:
     *            <ul>
     *            <li>-v inputVectorFile, mandatory</li>
     *            <li>-t templateVectorFile, mandatory</li>
     *            <li>--dbName databaseName, mandatory</li>
     *            <li>--tablePrefix databaseTableNamePrefix, mandatory</li>
     *            <li>--server databaseServerAddress, optional</li>
     *            <li>--user databaseUser, optional</li>
     *            <li>--password databasePassword, optional</li>
     *            </ul>
     * @throws SQLException If there is a problem connecting to the database.
     * @throws IOException If the input or template vector file can't be read.
     */
    public static void main(String[] args) throws SQLException, IOException {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);
        String inputVectorFile = config.getString("inputVectorFile");
        String templateVectorFile = config.getString("templateVectorFile");
        String databaseTableNamePrefix = config.getString("databaseTableNamePrefix");
        String databaseServerAddress = config.getString("databaseServerAddress");
        String databaseName = config.getString("databaseName");
        String user = config.getString("databaseUser");
        String password = config.getString("databasePassword");

        new VectorFile2DatabaseImporter(inputVectorFile, templateVectorFile, databaseServerAddress, databaseName, user,
                password, databaseTableNamePrefix);
    }

    public VectorFile2DatabaseImporter(String inputVectorFile, String templateVectorFile, String databaseServerAddress,
            String databaseName, String user, String password, String databaseTableNamePrefix) throws SQLException,
            IOException {
        MySQLConnector dbConnector = new MySQLConnector(databaseServerAddress, databaseName, user, password,
                databaseTableNamePrefix);
        dbConnector.setupTables();
        new TemplateVectorImporter(templateVectorFile, dbConnector);
        new InputVectorImporter(inputVectorFile, dbConnector);
    }

}
