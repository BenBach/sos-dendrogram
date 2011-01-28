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
import java.util.ArrayList;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.database.MySQLConnector;

/**
 * Implements a {@link TemplateVector} by reading the vector information from a database.
 * 
 * @author liegl
 * @author Rudolf Mayer
 * @version $Id: DataBaseSOMLibTemplateVector.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class DataBaseSOMLibTemplateVector extends AbstractSOMLibTemplateVector {

    /**
     * Creates a new {@link TemplateVector} by reading the labels and other attribute information from the database.
     */
    public DataBaseSOMLibTemplateVector(MySQLConnector dbConnector) {
        super();
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start reading template vector from DB.");

        try {
            String query = "SELECT label, number, documentFrequency, collectionTermFrequency, minimumTermFrequency, maximumTermFrequency, meanTermFrequency, comment FROM "
                    + dbConnector.getTermTableName() + " ORDER BY number";
            ResultSet r = dbConnector.executeSelect(query);
            ArrayList<TemplateVectorElement> elementList = new ArrayList<TemplateVectorElement>();
            while (r.next()) {
                TemplateVectorElement e = new TemplateVectorElement(this, r.getString("label"), r.getInt("number"));
                e.setDocumentFrequency(r.getInt("documentFrequency"));
                e.setCollectionTermFrequency(r.getInt("collectionTermFrequency"));
                e.setMinimumTermFrequency(r.getInt("minimumTermFrequency"));
                e.setMaximumTermFrequency(r.getInt("maximumTermFrequency"));
                e.setMeanTermFrequency(r.getDouble("meanTermFrequency"));
                e.setComment(r.getString("comment"));
                elementList.add(e);
            }
            dim = elementList.size();
            elements = elementList.toArray(new TemplateVectorElement[elementList.size()]);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "An error occured while communicating with database. Aborting.");
            System.exit(1);
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Template vector file format seems to be correct. Riding on ...");
    }

}
