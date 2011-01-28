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
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * This input object represents a mapping between input items. E.g. for an email corpus the mapping reply->original mail
 * would be saved.
 * 
 * @author Rudolf Mayer
 * @version $Id: DataItemLinkageMap.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class DataItemLinkageMap extends Hashtable<String, String> {
    private static final long serialVersionUID = 1L;

    public DataItemLinkageMap(String fileName) throws IOException {
        BufferedReader reader = FileUtils.openFile(SOMVisualisationData.LINKAGE_MAP, fileName);
        String line = null;
        int lineCount = 0;
        while ((line = reader.readLine()) != null) {
            lineCount++;
            String[] items = line.split(" ");
            if (items == null || items.length < 2 || items.length == 2 && items[1] == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Line #" + lineCount + "oes not contain 2 elements: '" + line + "'. Aborting.");
            } else {
                put(items[0].trim(), items[1].trim());
            }
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Linkage file read, loaded " + size() + " mappings.");
    }

}
