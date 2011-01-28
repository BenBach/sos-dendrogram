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

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * Is thrown if data dimensions are erroneous. E.g. if the dimensions given in $DATA_DIM header line does not agree with
 * $VEC_DIM or if a Rhythm Pattern is instanciated, and the dimensions do not match the weight vector.
 * 
 * @author Thomas Lidy
 * @version $Id: DataDimensionException.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class DataDimensionException extends SOMToolboxException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     */
    public DataDimensionException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently
     * be initialized by a call to {@link #initCause}.
     * 
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */

    public DataDimensionException(String message) {
        super(message);
    }

}