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
package at.tuwien.ifs.somtoolbox.output.labeling;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Defines basic functionality for Labelers. All classes providing labelling algorothm implementations should implement
 * this interface.
 * 
 * @author Michael Dittenbach
 * @version $Id: Labeler.java 3590 2010-05-21 10:43:45Z mayer $
 */
public interface Labeler {

    /**
     * Determines and adds labels to the units of a GrowingSOM (should be NetworkModel in the future).
     * 
     * @param gsom The GrowingSOM to be labeled.
     * @param data The data that is already mapped onto the GrowingSOM
     * @param num The number of labels per node.
     */
    public void label(GrowingSOM gsom, InputData data, int num); // TODO: GrowingSOM -> NetworkModel or similar

    public void label(GrowingSOM gsom, InputData data, int num, boolean ignoreLabelsWithZero); // TODO: GrowingSOM ->

    // NetworkModel or
    // similar

    public void label(GHSOM ghsom, InputData data, int num); // TODO: GHSOM -> NetworkModel or similar

}
