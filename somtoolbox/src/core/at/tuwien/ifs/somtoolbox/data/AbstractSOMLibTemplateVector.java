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
import java.util.Hashtable;

import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * This abstract implementation provides basic support for operating on a {@link TemplateVector}. Sub-classes have to
 * implement constructors and methods to read and create a template vector, e.g. from a file or a database.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: AbstractSOMLibTemplateVector.java 3883 2010-11-02 17:13:23Z frank $
 */
public abstract class AbstractSOMLibTemplateVector implements TemplateVector {

    /**
     * The dimension of the template vector, i.e. the number of attributes.
     */
    protected int dim = 0;

    protected int numInfo = 0;

    protected int numVectors = 0;

    protected String templateFileName = null;

    /**
     * The attributes of the template vector.
     */
    protected TemplateVectorElement[] elements = null;

    /**
     * A mapping label --&gt; attribute to allow fast access.
     */
    protected Hashtable<String, TemplateVectorElement> elementMap = new Hashtable<String, TemplateVectorElement>();

    protected int longestStringLength = -1;

    @Override
    public int dim() {
        return dim;
    }

    @Override
    public int numVectors() {
        return numVectors;
    }

    @Override
    public int numinfo() {
        return numInfo;
    }

    @Override
    public String getLabel(int i) {
        return elements[i].getLabel();
    }

    @Override
    public String[] getLabels() {
        String[] res = new String[elements.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = elements[i].getLabel();
        }
        return res;
    }

    @Override
    public ArrayList<String> getLabelsAsList() {
        ArrayList<String> res = new ArrayList<String>(elements.length);
        for (TemplateVectorElement element : elements) {
            res.add(element.getLabel());
        }
        return res;
    }

    @Override
    public int getIndexOfFeature(String label) {
        if (containsLabel(label)) {
            return elementMap.get(label).getIndex();
        } else {
            return -1;
        }
    }

    public int getIndex(String label) {
        TemplateVectorElement templateVectorElement = elementMap.get(label);
        if (templateVectorElement != null) {
            return templateVectorElement.getIndex();
        } else {
            return -1;
        }
    }

    public TemplateVectorElement getElement(String label) {
        return elementMap.get(label);
    }

    @Override
    public boolean containsLabel(String label) {
        return elementMap.containsKey(label);
    }

    /**
     * @param label the name of the term.
     * @return The document frequency of the given term
     */
    public int getDocumentFrequency(String label) {
        return elementMap.get(label).getDocumentFrequency();
    }

    /**
     * @param queryTerms A map containing <label, frequency> pairs for each term.
     * @return A vector according to the tfxidf weighting scheme
     */
    public double[] getTFxIDFVectorFromTerms(Hashtable<String, Integer> queryTerms) {
        double[] vector = new double[dim];
        for (int i = 0; i < dim; i++) {
            if (queryTerms.get(elements[i].getLabel()) != null) {
                double tf = queryTerms.get(elements[i].getLabel()).intValue();
                vector[i] = tf * Math.log((double) elements.length / (double) elements[i].getDocumentFrequency());
            } else {
                vector[i] = 0;
            }
        }
        // FIXME: normalise only when input is normalised?
        vector = VectorTools.normaliseVectorToUnitLength(vector);
        return vector;
    }

    @Override
    public TemplateVectorElement getElement(int index) {
        return elements[index];
    }

    @Override
    public int getLongestStringLength() {
        if (longestStringLength == -1) {
            longestStringLength = StringUtils.getLongestStringLength(elementMap.keySet());
        }
        return longestStringLength;
    }

    @Override
    public void incNumVectors(int numVectors) {
        this.numVectors += numVectors;
    }
}
