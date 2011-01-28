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
package at.tuwien.ifs.somtoolbox.data.metadata;

import java.util.regex.Pattern;

/**
 * This is an empty MetaData object. All operations just work in the "name" (the ID).
 * 
 * @author Jakob Frank
 * @version $Id: EmtpyVectorMetaData.java 3583 2010-05-21 10:07:41Z mayer $
 * @see #getID()
 */
public class EmtpyVectorMetaData extends AbstractVectorMetaData {

    private final String id;

    public EmtpyVectorMetaData(String id) {
        this.id = id;
    }

    @Override
    public boolean matches(String pattern, boolean ignoreCase) {
        String lP = ignoreCase ? pattern.toLowerCase() : pattern;
        String lID = ignoreCase ? id.toLowerCase() : id;
        return lID.contains(lP);
    }

    @Override
    public boolean matches(Pattern pattern) {
        return pattern.matcher(id).find();
    }

    @Override
    public String getDisplayLabel() {
        return id;
    }

    @Override
    public String getID() {
        return id;
    }

}
