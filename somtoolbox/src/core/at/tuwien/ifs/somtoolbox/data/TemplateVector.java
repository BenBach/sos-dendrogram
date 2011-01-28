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

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.apps.helper.SOMLibInputMerger;

/**
 * The template vector provides the attribute structure of the input vectors used for the training process of a
 * Self-Organizing Map. It is usually written by a parser or vector generator program creating the vector structure.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: TemplateVector.java 3583 2010-05-21 10:07:41Z mayer $
 */
public interface TemplateVector {

    String templateFileNameSuffix = ".tv";

    /**
     * Gets the dimension.
     * 
     * @return the dimension of the template vector, i.e. the number of attributes
     */
    public int dim();

    /**
     * Gets the label at the given index.
     * 
     * @return the name of the label at the given index
     */
    public String getLabel(int i);

    /** Gets all the labels defined in this template vector. */
    public String[] getLabels();

    /** Gets all the labels defined in this template vector as a list. */
    public ArrayList<String> getLabelsAsList();

    /** tests whether there is a feature/attribute with the given label */
    public boolean containsLabel(String label);

    /** Returns the numerical index of the feature with the given name. */
    public int getIndexOfFeature(String label);

    /** Return how many vectors are in the input vector file associated with this template vector */
    public int numVectors();

    /** Returns how many columns the template vector contains, i.e. the $XDIM. */
    public int numinfo();

    /** returns the template vector element for the feature/attribute at the given position */
    public TemplateVectorElement getElement(int index);

    /** calculates the length of the longest feature/attribute label */
    public int getLongestStringLength();

    /** Increase the num-vectors counter, used e.g. when merging input files in {@link SOMLibInputMerger} . */
    public void incNumVectors(int numVectors);
}
