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
package at.tuwien.ifs.somtoolbox.properties;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

/**
 * Properties for I/O stuff.
 * 
 * @author Michael Dittenbach
 * @version $Id: FileProperties.java 3979 2010-12-16 17:11:39Z mayer $
 */
public class FileProperties extends Properties {
    private static final long serialVersionUID = 1L;

    private boolean isNormalized = true;

    private String namePrefix = null;

    private int numCacheBlocks = 0;

    private String outputDirectory = null;

    private long randomSeed = -1;

    private boolean sparseData = true;

    private String templateFileName = null;

    private String vectorFileName = null;

    private String workingDirectory = null;

    // Database-Properties
    private boolean usingDatabase = false;

    private String databaseServerAddress = null;

    private String databaseName = null;

    private String databaseUser = null;

    private String databasePassword = null;

    private String databaseTableNamePrefix = null;

    private String sourceFileName;

    /**
     * Loads and encapsulated properties related to the input data.
     * 
     * @param fname Name of the properties file.
     */
    public FileProperties(String fname) throws PropertiesException {
        this.sourceFileName = fname;
        try {
            load(new FileInputStream(fname));
        } catch (Exception e) {
            throw new PropertiesException("Could not open properties file " + fname);
        }
        parse();
    }

    public FileProperties(Properties properties) throws PropertiesException {
        putAll(properties);
        parse();
    }

    private void parse() throws PropertiesException {
        try {
            workingDirectory = getProperty("workingDirectory");
            if (StringUtils.isBlank(workingDirectory)) {
                workingDirectory = "." + File.separator;
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No workingDirectory given. Defaulting to '" + workingDirectory + "' ("
                                + new File(workingDirectory).getAbsolutePath() + ")");
            } else if (workingDirectory.trim().equals(".") || workingDirectory.trim().startsWith("./")) {
                String msg = "Relative workingDirectory '" + workingDirectory + "' given. Expanding to '";
                String parentDirectory = new File(sourceFileName).getParentFile().getAbsolutePath();
                workingDirectory = workingDirectory.replace(".", parentDirectory);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(msg + workingDirectory + "'.");
            }
            if (!workingDirectory.endsWith(File.separator)) {
                workingDirectory += File.separator;
            }

            outputDirectory = getProperty("outputDirectory");
            if (outputDirectory == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No outputDirectory given. Defaulting to " + "." + File.separator);
                outputDirectory = "";
            } else {
                if (!outputDirectory.endsWith(File.separator)) {
                    outputDirectory += File.separator;
                }
            }

            // create outputdirectory if not existing
            File outputDir = new File(outputDirectory());
            if (!outputDir.exists()) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Output directory '" + outputDir.getAbsolutePath() + "' does not exist. Trying to create it.");
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Successfully created output directory: " + outputDir.mkdirs());
            }

            namePrefix = getProperty("namePrefix");
            if (namePrefix == null) {
                throw new PropertiesException("No namePrefix given.");
            } else {
                namePrefix = namePrefix.trim();
            }
            vectorFileName = getProperty("vectorFileName");
            if (vectorFileName == null) {
                throw new PropertiesException("No vectorFileName given.");
            }
            templateFileName = getProperty("templateFileName");
            if (templateFileName == null) {
                // throw new PropertiesException("No templateFileName given.");
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No templateFileName given. Using default template vector.");
            }
            String sparseDataStr = getProperty("sparseData");
            if (sparseDataStr == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("No sparsity information given. Using true.");
                sparseData = true;
            } else {
                if (sparseDataStr.equals("true") || sparseDataStr.equals("yes")) {
                    sparseData = true;
                } else if (sparseDataStr.equals("false") || sparseDataStr.equals("no")) {
                    sparseData = false;
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                            "Sparsity information unclear. Use true|yes or false|no. Using true.");
                }
            }
            String normStr = getProperty("isNormalized");
            if (normStr == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "No normalization information given. Use true|yes or false|no. Using true.");
                isNormalized = true;
            } else {
                if (normStr.equals("true") || normStr.equals("yes")) {
                    isNormalized = true;
                } else if (normStr.equals("false") || normStr.equals("no")) {
                    isNormalized = false;
                } else {
                    throw new PropertiesException("Normalization information unclear. Use true|yes or false|no.");
                }
            }
            String rs = getProperty("randomSeed");
            if (rs == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("No randomSeed given. Defaulting to 7.");
                randomSeed = 7;
            } else {
                randomSeed = Long.parseLong(rs);
            }
            String cs = getProperty("numCacheBlocks");
            if (cs == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No numCacheBlocks given. Reading the data en bloc. Can be problematic with large input data.");
                numCacheBlocks = 1;
            } else {
                numCacheBlocks = Integer.parseInt(cs);
            }

            // Should a database be used?
            String database = getProperty("useDatabase");
            if (database != null && database.equals("true")) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Database found. Proceeding in database mode.");
                this.usingDatabase = true;

                String databaseServerAddressStr = getProperty("databaseServerAddress");
                if (databaseServerAddressStr == null || databaseServerAddressStr.equals("")) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                            "No databaseServerAddress given. Defaulting to 'localhost'");
                    databaseServerAddress = "localhost";
                } else {
                    databaseServerAddress = databaseServerAddressStr;
                }

                String databaseNameStr = getProperty("databaseName");
                if (databaseNameStr == null || databaseNameStr.equals("")) {
                    throw new PropertiesException("No databaseName given. Aborting");
                } else {
                    databaseName = databaseNameStr;
                }

                String databasePasswordStr = getProperty("databasePassword");
                if (databasePasswordStr == null || databasePasswordStr.equals("")) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                            "No databasePassword given. Defaulting to empty password");
                    databasePassword = "";
                } else {
                    databasePassword = databasePasswordStr;
                }

                String databaseTableNamePrefixStr = getProperty("databaseTableNamePrefix");
                if (databaseTableNamePrefixStr == null || databaseTableNamePrefixStr.equals("")) {
                    throw new PropertiesException("No databaseTableNamePrefix given. Aborting");
                } else {
                    databaseTableNamePrefix = databaseTableNamePrefixStr;
                }

                String databaseUserStr = getProperty("databaseUser");
                if (databaseUserStr == null || databaseUserStr.equals("")) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info("No databaseUser given. Defaulting to 'root'");
                    databaseUser = "root";
                } else {
                    databaseUser = databaseUserStr;
                }

            } else {
                // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("No database specified. Proceeding in normal mode.");
                this.usingDatabase = false;
            }

        } catch (NumberFormatException e) {
            throw new PropertiesException("Illegal numeric value in properties file.");
        }
    }

    /**
     * Returns <code>true</code>, if the vectors are normalized to unit length. This information is used for map
     * creation to know when to normalize the units' weight vectors.
     * 
     * @return Returns <code>true</code>, if the vectors are normalized to unit length.
     */
    public boolean isNormalized() {
        return isNormalized;
    }

    /**
     * Returns the name of the test run.
     * 
     * @return the name of the test run.
     */
    public String namePrefix(boolean withPrefix) {
        if (withPrefix == true) {
            return workingDirectory + namePrefix;
        } else {
            return namePrefix;
        }
    }

    /**
     * Not used at the moment.
     * 
     * @return Returns the numCacheBlocks.
     */
    public int numCacheBlocks() {
        return numCacheBlocks;
    }

    /**
     * Returns the name of the output directory.
     * 
     * @return the name of the output directory.
     */

    public String outputDirectory() {
        return prependDirectory(true, outputDirectory, workingDirectory);
    }

    /**
     * Returns the random seed.
     * 
     * @return the random seed.
     */
    public long randomSeed() {
        return randomSeed;
    }

    /**
     * Returns <code>true</code> if the input data vectors are sparsely populated.
     * 
     * @return <code>true</code> if the input data vectors are sparsely populated.
     */
    public boolean sparseData() {
        return sparseData;
    }

    /**
     * Returns the name of the template vector file. The file name includes the working directory, if argument
     * <code>withPrefix</code> is true.
     * 
     * @param withPrefix determines if the file name is prefixed with the working directory.
     * @return the name of the template vector file.
     */
    public String templateFileName(boolean withPrefix) {
        return prependDirectory(withPrefix, templateFileName, workingDirectory);
    }

    private String prependDirectory(boolean withPrefix, String path, String dir) {
        if (path != null && withPrefix == true) {
            return prependDirectory(path, dir);
        } else {
            return path;
        }
    }

    private String prependDirectory(String path, String dir) {
        if (path.startsWith(File.separator) || new File(path).isAbsolute()) {
            return path;
        } else {
            return dir + path;
        }
    }

    /**
     * Returns the name of the input vector file. The file name includes the working directory, if argument
     * <code>withPrefix</code> is true.
     * 
     * @param withPrefix determines if the file name is prefixed with the working directory.
     * @return the name of the input vector file.
     */
    public String vectorFileName(boolean withPrefix) {
        return prependDirectory(withPrefix, vectorFileName, workingDirectory);
    }

    /**
     * Returns the name of the working directory.
     * 
     * @return the name of the working directory.
     */
    public String workingDirectory() {
        return workingDirectory;
    }

    public boolean isUsingDatabase() {
        return usingDatabase;
    }

    public void setUsingDatabase(boolean usedatabase) {
        this.usingDatabase = usedatabase;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getDatabaseServerAddress() {
        return databaseServerAddress;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabaseTableNamePrefix() {
        return databaseTableNamePrefix;
    }

}