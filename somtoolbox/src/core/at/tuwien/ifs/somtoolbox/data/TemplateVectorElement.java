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

import at.tuwien.ifs.somtoolbox.apps.helper.SOMLibInputMerger;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class represents one element or attribute of the {@link TemplateVector}.
 * 
 * @author Rudolf Mayer
 * @version $Id: TemplateVectorElement.java 3883 2010-11-02 17:13:23Z frank $
 */
public class TemplateVectorElement implements Comparable<TemplateVectorElement> {
    private final TemplateVector tv;

    /**
     * The label or name associated with this attribute.
     */
    private String label;

    /**
     * Indicates in how many documents or feature vectors this attribute is present, i.e. has an input vector value <>
     * 0.
     */
    private int documentFrequency = -1;

    /**
     * The term frequency in the whole collection - how often does this attribute show up in the whole collection of
     * feature vectors, i.e. a counter for the attribute, the sum of all values of the attribute (sum across all feature
     * vectors).
     */
    private int collectionTermFrequency = -1;

    /**
     * Minimum value of this attribute in the collection of feature vectors.
     */
    private int minimumTermFrequency = -1;

    /**
     * Maximum value of this attribute in the collection of feature vectors.
     */
    private int maximumTermFrequency = -1;

    /**
     * Mean value of this attribute in the collection of feature vectors.
     */
    private double meanTermFrequency = -1;

    /**
     * Optional comment for this attribute.
     */
    private String comment = null;

    private int index;

    public int getIndex() {
        return index;
    }

    public TemplateVectorElement(TemplateVector tv, String label, int index) {
        this(tv, label, index, -1, -1);
    }

    public TemplateVectorElement(TemplateVector tv, String label, int index, int documentFrequency,
            int documentTermFrequency) {
        this.tv = tv;
        this.index = index;
        this.label = label;
        this.documentFrequency = documentFrequency;
        this.collectionTermFrequency = documentTermFrequency;
    }

    /**
     * Gets the document frequency.
     * 
     * @return the document frequency
     */
    public int getDocumentFrequency() {
        return documentFrequency;
    }

    public void setDocumentFrequency(int documentFrequency) {
        this.documentFrequency = documentFrequency;
    }

    /**
     * Gets the term frequency in the whole collection.
     * 
     * @return the frequency of this term in the whole collection
     */
    public int getCollectionTermFrequency() {
        return collectionTermFrequency;
    }

    public void setCollectionTermFrequency(int collectionTermFrequency) {
        this.collectionTermFrequency = collectionTermFrequency;
    }

    /**
     * Gets the label.
     * 
     * @return the label of this attribute
     */
    public String getLabel() {
        return label;
    }

    protected void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the comment.
     * 
     * @return the comment attached to this attribute
     */
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the maximum tf.
     * 
     * @return the maximum value of this attribute in the collection of feature vectors
     */
    public int getMaximumTermFrequency() {
        return maximumTermFrequency;
    }

    public void setMaximumTermFrequency(int maximumTermFrequency) {
        this.maximumTermFrequency = maximumTermFrequency;
    }

    /**
     * Gets the mean tf.
     * 
     * @return the mean value of this attribute in the collection of feature vectors
     */
    public double getMeanTermFrequency() {
        return meanTermFrequency;
    }

    public void setMeanTermFrequency(double meanTermFrequency) {
        this.meanTermFrequency = meanTermFrequency;
    }

    /**
     * Gets the minimum tf.
     * 
     * @return the minimum value of this attribute in the collection of feature vectors
     */
    public int getMinimumTermFrequency() {
        return minimumTermFrequency;
    }

    public void setMinimumTermFrequency(int minimumTermFrequency) {
        this.minimumTermFrequency = minimumTermFrequency;
    }

    /**
     * Compares two {@link TemplateVectorElement}s by comparing the two labels.
     * 
     * @see String#compareTo(String)
     */
    @Override
    public int compareTo(TemplateVectorElement o) {
        return label.compareTo(o.getLabel());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(StringUtils.getSpaces(tv.getLongestStringLength() - label.length()));

        if (documentFrequency != -1) {
            sb.append("\tdf: " + documentFrequency);
        }
        if (collectionTermFrequency != -1) {
            sb.append("\ttf: " + collectionTermFrequency);
        }
        if (minimumTermFrequency != -1) {
            sb.append("\tminTf: " + minimumTermFrequency);
        }
        if (maximumTermFrequency != -1) {
            sb.append("\tmaxTf: " + maximumTermFrequency);
        }
        if (meanTermFrequency != -1) {
            sb.append("\tmeanTf: " + StringUtils.format(meanTermFrequency, 1));
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(comment)) {
            sb.append("\tcomment: " + comment);
        }
        return sb.toString();
    }

    /**
     * Merge the statistical information of the current template vector element with another element, used e.g. in
     * {@link SOMLibInputMerger}.
     */
    public void mergeStatiscticsWithOtherElement(TemplateVectorElement other) {
        int TF = getCollectionTermFrequency() == -1 ? 0 : getCollectionTermFrequency();
        int df = getDocumentFrequency() == -1 ? 0 : getDocumentFrequency();
        int maxFreq = getMaximumTermFrequency() == -1 ? 0 : getMaximumTermFrequency();
        int minFreq = getMinimumTermFrequency() == -1 ? Integer.MAX_VALUE : getMinimumTermFrequency();
        double meanFreq = getMeanTermFrequency() == -1 ? 0 : getMeanTermFrequency();

        setCollectionTermFrequency(TF + other.getCollectionTermFrequency());
        setDocumentFrequency(df + other.getDocumentFrequency());
        setMaximumTermFrequency(Math.max(maxFreq, other.getMaximumTermFrequency()));
        setMeanTermFrequency((tv.numVectors() * meanFreq + other.tv.numVectors() * other.meanTermFrequency)
                / (tv.numVectors() + other.tv.numVectors()));

        setMinimumTermFrequency(Math.min(minFreq, other.getMinimumTermFrequency()));
    }

    /** Returns the {@link TemplateVector} this element is associated to. */
    public TemplateVector getTemplateVector() {
        return tv;
    }

}