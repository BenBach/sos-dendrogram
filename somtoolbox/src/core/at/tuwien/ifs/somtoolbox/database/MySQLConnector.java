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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * This class handles the communication with a MySQL database.<br/>
 * TODO: replace using this class by using e.g. Hibernate
 * 
 * @author Rudolf Mayer
 * @version $Id: MySQLConnector.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class MySQLConnector extends DBConnector {

    public MySQLConnector(String databaseUrl, String databaseName, String user, String password, String tableNamePrefix) {
        super(databaseUrl, databaseName, user, password, tableNamePrefix);
        statementEndCharacter = ";";
    }

    /**
     * Opens a Connection to the database
     * 
     * @return the database connection
     * @throws SQLException if a database access error occurs or this method is called on a closed Statement
     */
    @Override
    public Connection openConnection() throws SQLException {
        con = null;
        // Get an instance of the database driver
        try {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Loading JDBC driver....");
            Class.forName("com.mysql.jdbc.Driver");
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("done!");
            // driver found - proceed
            String connectionString = "jdbc:mysql://" + databaseUrl + ":3306/" + databaseName + "?user=" + user;
            String connectionStringDBServer = "jdbc:mysql://" + databaseUrl + ":3306/" + "?user=" + user;
            if (password != null && !password.equals("")) {
                connectionString += "&password=" + password;
                connectionStringDBServer += "&password=" + password;
            }
            // create database if needed
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Opening connection to " + connectionStringDBServer);
            con = DriverManager.getConnection(connectionStringDBServer);
            con.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + databaseName);

            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Opening connection to " + connectionString);
            con = DriverManager.getConnection(connectionString);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Opened connection.");
            return con;
        } catch (ClassNotFoundException e) { // If no driver is found - terminate
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Unable to find appropriate MySQL database driver 'com.mysql.jdbc.Driver'. Aborting.");
            e.printStackTrace();
            throw new SQLException(e);
        }

    }

}
