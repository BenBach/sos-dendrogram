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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * A class handling SOMLib map files.
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMLibMapDescription.java 3655 2010-07-13 15:24:01Z mayer $
 */
public class SOMLibMapDescription {
    public SOMLibMapDescription(String fileName) throws IOException {
        readMapDescriptionFile(fileName);
    }

    public SOMLibMapDescription() {
    }

    public static final String TYPE = "$TYPE";

    public static final String GRID_TOPOLOGY = "$GRID_TOPOLOGY";

    public static final String GRID_LAYOUT = "$GRID_LAYOUT";

    public static final String X_DIM = "$XDIM";

    public static final String Y_DIM = "$YDIM";

    public static final String Z_DIM = "$ZDIM";

    public static final String VEC_DIM = "$VEC_DIM";

    public static final String STORAGE_DATE = "$STORAGE_DATE";

    public static final String TRAINING_TIME = "$TRAINING_TIME";

    public static final String LEARNRATE_TYPE = "$LEARNRATE_TYPE";

    public static final String LEARNRATE_INIT = "$LEARNRATE_INIT";

    public static final String NEIGHBOURHOOD_TYPE = "$NEIGHBORHOOD_TYPE";

    public static final String NEIGHBOURHOOD_INIT = "$NEIGHBORHOOD_INIT";

    public static final String RANDOM_INIT = "$RAND_INIT";

    public static final String TOTAL_ITERATIONS = "$ITERATIONS_TOTAL";

    public static final String TOTAL_TRAINING_VECTORS = "$NR_TRAINVEC_TOTAL";

    public static final String VECTORS_NORMALISED = "$VEC_NORMALIZED";

    public static final String QUANT_ERROR_MAP = "$QUANTERROR_MAP";

    public static final String QUANT_ERROR_VECTOR = "$QUANTERROR_VEC";

    public static final String URL_TRAINING_VECTOR = "$URL_TRAINING_VEC";

    public static final String URL_TRAINING_VECTOR_DESCRIPTION = "$URL_TRAINING_VEC_DESCR";

    public static final String URL_WEIGHT_VECTOR = "$URL_WEIGHT_VEC";

    public static final String URL_QUANT_ERROR_MAP = "$URL_QUANTERR_MAP";

    public static final String URL_MAPPED_INPUT_VECTOR = "$URL_MAPPED_INPUT_VEC";

    public static final String URL_MAPPED_INPUT_VECTOR_DESCRIPTION = "$URL_MAPPED_INPUT_VEC_DESCR";

    public static final String URL_UNIT_DESCRIPTION = "$URL_UNIT_DESCR";

    public static final String URL_TEMPLATE_VECTOR = "$URL_TEMPLATE_VECTOR";

    public static final String URL_LABELS = "$URL_LABELS";

    public static final String URL_DATA_WINNER_MAPPING = "$URL_DATA_WINNER_MAPPING";

    public static final String URL_CLASS_INFO = "$URL_CLASS_INFO";

    public static final String METRIC = "$METRIC";

    public static final String LAYER_REVISION = "$LAYER_REVISION";

    public static final String DESCRIPTION = "$DESCRIPTION";

    public static final String AVAILABLE_VIS = "$AVAILABLE_VIS";

    public static final String DATA_TYPE = "$DATA_TYPE";

    public static final String DATA_DIM = "$DATA_DIM";

    public static final String ADAPTIVE_COORDINATES_THRESHOLD = "$ADAPTIVE_COORDINATES_THRESHOLD";

    Hashtable<String, String> prop = new Hashtable<String, String>();

    /** All properties names, in the order as they will be written to the Map Description File. */
    private static final String[] propertyNames = { TYPE, GRID_TOPOLOGY, GRID_LAYOUT, X_DIM, Y_DIM, Z_DIM, VEC_DIM,
            STORAGE_DATE, TRAINING_TIME, LEARNRATE_TYPE, LEARNRATE_INIT, NEIGHBOURHOOD_TYPE, NEIGHBOURHOOD_INIT,
            RANDOM_INIT, TOTAL_ITERATIONS, TOTAL_TRAINING_VECTORS, ADAPTIVE_COORDINATES_THRESHOLD, VECTORS_NORMALISED,
            QUANT_ERROR_MAP, QUANT_ERROR_VECTOR, URL_TRAINING_VECTOR, URL_TRAINING_VECTOR_DESCRIPTION,
            URL_WEIGHT_VECTOR, URL_QUANT_ERROR_MAP, URL_MAPPED_INPUT_VECTOR, URL_MAPPED_INPUT_VECTOR_DESCRIPTION,
            URL_UNIT_DESCRIPTION, URL_TEMPLATE_VECTOR, URL_DATA_WINNER_MAPPING, URL_CLASS_INFO, URL_LABELS, METRIC,
            LAYER_REVISION, DESCRIPTION, AVAILABLE_VIS, DATA_TYPE, DATA_DIM };

    private static final String[] integerFields = { X_DIM, Y_DIM, Z_DIM, VEC_DIM, TOTAL_ITERATIONS,
            TOTAL_TRAINING_VECTORS };

    private static final String[] doubleFields = { LEARNRATE_INIT, NEIGHBOURHOOD_INIT, QUANT_ERROR_MAP,
            QUANT_ERROR_VECTOR };

    private static final String[] doubleArrrayFields = { ADAPTIVE_COORDINATES_THRESHOLD };

    private static final String[] longFields = { TRAINING_TIME, RANDOM_INIT };

    private static final String[] dateFields = { STORAGE_DATE };

    private static final String[] booleanFields = { VECTORS_NORMALISED };

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

    public void readMapDescriptionFile(String fileName) throws IOException {
        // prop = new Properties();
        prop.clear();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = null;
        int lineNo = 0;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!(line.equals("") || line.startsWith("#"))) {
                String[] property = line.split(" ", 2);
                if (property.length == 1) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                            "Undefined property value for property '" + property[0] + "', line #" + lineNo + " ("
                                    + line + ")");
                } else {
                    prop.put(property[0], property[1]);
                }
            }
            lineNo++;
        }
        reader.close();
        // check for well-formed
        for (String key : integerFields) {
            if (StringUtils.isNotBlank(prop.get(key))) {
                try {
                    Integer.parseInt(prop.get(key));
                } catch (NumberFormatException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "Error reading map file: not an <Integer> for value '" + key + "': " + prop.get(key));
                }
            }
        }
        for (String key : doubleFields) {
            if (StringUtils.isNotBlank(prop.get(key))) {
                try {
                    Double.parseDouble(prop.get(key));
                } catch (NumberFormatException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "Error reading map file: not a <Double> for value '" + key + "': " + prop.get(key));
                }
            }
        }
        for (String key : longFields) {
            if (StringUtils.isNotBlank(prop.get(key))) {
                try {
                    Long.parseLong(prop.get(key));
                } catch (NumberFormatException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "Error reading map file: not a <Long> for value '" + key + "': " + prop.get(key));
                }
            }
        }
        for (String booleanField : booleanFields) {
            String key = booleanField;
            if (prop.get(key) == null || !prop.get(key).equalsIgnoreCase("true")
                    || !prop.get(key).equalsIgnoreCase("false")) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Error reading map file: not a <Boolean> for value '" + key + "': " + prop.get(key));
            }
        }
        for (String key : dateFields) {
            try {
                simpleDateFormat.parse(prop.get(key));
            } catch (ParseException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Error reading map file: not a <Date> for value '" + key + "': " + prop.get(key));
            }
        }

    }

    public String getProperty(String key) {
        return prop.get(key);
    }

    public void setProperty(String propertyName, String value) throws SOMToolboxException {
        setProperty(propertyName, String.valueOf(value), propertyNames, String.class);
    }

    public void setProperty(String propertyName, int value) throws SOMToolboxException {
        setProperty(propertyName, String.valueOf(value), integerFields, Integer.class);
    }

    public void setProperty(String propertyName, double value) throws SOMToolboxException {
        setProperty(propertyName, String.valueOf(value), doubleFields, Double.class);
    }

    public void setProperty(String propertyName, double[] value) throws SOMToolboxException {
        setProperty(propertyName, at.tuwien.ifs.somtoolbox.util.StringUtils.toString(value, "", ""),
                doubleArrrayFields, Double.class);
    }

    public void setProperty(String propertyName, boolean value) throws SOMToolboxException {
        setProperty(propertyName, String.valueOf(value), booleanFields, Boolean.class);
    }

    public void setProperty(String propertyName, long value) throws SOMToolboxException {
        setProperty(propertyName, String.valueOf(value), longFields, Long.class);
    }

    public void setProperty(String propertyName, Date value) throws SOMToolboxException {
        setProperty(propertyName, String.valueOf(value), dateFields, Date.class);
    }

    private void setProperty(String propertyName, String value, String[] validPropertyNames, Class<?> type)
            throws SOMToolboxException {
        if (ArrayUtils.contains(propertyNames, propertyName) && ArrayUtils.contains(validPropertyNames, propertyName)) {
            prop.put(propertyName, String.valueOf(value));
        } else {
            throw new SOMToolboxException("Unkown Map Description property of type <" + type + ">: '" + propertyName
                    + "'.");
        }
    }

    public void writeMapDescriptionFile(String fileName) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        for (String propertyName : propertyNames) {
            final String value = prop.get(propertyName);
            bw.write(propertyName + " " + (value != null ? value : "") + "\n");
        }
        bw.close();
    }

    public static void main(String[] args) {
        try {
            new SOMLibMapDescription(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
