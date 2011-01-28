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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Implements a {@link TemplateVector} based on a SOMLib <a
 * href="http://olymp.ifs.tuwien.ac.at/somtoolbox/doc/somlibFileFormat.html#template_vector">Template Vector File</a>.
 * See also a <a href="../../../../../ssd.tv">sample TemplateVector file</a>.
 * <p>
 * <i>Created on May 14, 2004</i>
 * </p>
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: SOMLibTemplateVector.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class SOMLibTemplateVector extends AbstractSOMLibTemplateVector {
    private int lineNumber = 0;

    /**
     * Creates an empty instance.
     */
    protected SOMLibTemplateVector() {
    }

    /** Constructor intended to be used when generating data. All attributes will be called "componen_x". */
    public SOMLibTemplateVector(int numVectors, int dim) {
        this.dim = dim;
        this.numInfo = 2;
        this.numVectors = numVectors;
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Initializing template vector with " + dim + " dimensions.");
        elements = new TemplateVectorElement[dim];
        for (int i = 0; i < dim; i++) {
            elements[i] = new TemplateVectorElement(this, "component_" + i, i);
        }
    }

    /** Creates an instance with the given component names, and the specified detail level numInfo */
    public SOMLibTemplateVector(int numVectors, String[] componentNames, int numInfo) throws IOException {
        this.dim = componentNames.length;
        this.numInfo = numInfo;
        this.numVectors = numVectors;
        elements = new TemplateVectorElement[dim];
        for (int i = 0; i < dim; i++) {
            elements[i] = new TemplateVectorElement(this, componentNames[i], i);
            elementMap.put(elements[i].getLabel(), elements[i]);
        }
    }

    /** Creates an instance with the given component names. */
    public SOMLibTemplateVector(int numVectors, String[] componentNames) throws IOException {
        this(numVectors, componentNames, 7);
    }

    /**
     * Creates a new {@link TemplateVector} by reading from the given file.
     * 
     * @param templateFileName the file to read from
     * @throws IOException in case of problems reading the file
     */
    public SOMLibTemplateVector(String templateFileName) throws IOException {
        this.templateFileName = templateFileName;
        readTemplateVectorFile(templateFileName);
    }

    /**
     * Reads the {@link TemplateVector} information from the given filename. The file format has to follow the
     * specification given in the <a
     * href="http://olymp.ifs.tuwien.ac.at/somtoolbox/doc/somlibFileFormat.html#template_vector">SOMLib Data Files
     * specification</a>. See also a <a href="../../../../../doc/examples/ssd.tv">sample TemplateVector file</a>.
     */
    public void readTemplateVectorFile(String templateFileName) throws IOException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start reading template vector.");

        BufferedReader br = FileUtils.openFile("Template vector file", templateFileName);

        String line = readTemplateVectorFileHeader(br);
        elements = new TemplateVectorElement[dim];

        int index = 0;
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(dim, "Reading feature ", 10);
        while (line != null) {
            // skip comment lines and empty lines
            if (line.trim().length() == 0 || line.trim().startsWith("#")) {
                line = br.readLine();
                continue;
            }
            progressWriter.progress(index + 1);
            String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
            if (lineElements.length != numInfo) {
                throw new IOException("Template vector file corrupt in element number " + index + ": expected "
                        + numInfo + " line elements, found " + lineElements.length + " "
                        + getErrorDetails(line, lineNumber));
            } else { // vector format ok. checking number format and creating Vector of element.
                // check index
                if (index >= dim) {
                    throw new IOException("Template vector file corrupt, attempting to read element #" + (index + 1)
                            + ", specified dimensionality is " + dim + ". Aborting.");
                }
                try {
                    processLine(index, lineElements);
                } catch (NumberFormatException e) { // does not happen at the moment
                    NumberFormatException ex = new NumberFormatException(
                            "Template vector number format corrupt in vector number " + index + ": " + e.getMessage());
                    ex.setStackTrace(e.getStackTrace());
                    throw ex;
                }
            }
            index++;
            lineNumber++;
            line = br.readLine();
        }
        if (index != dim) {
            throw new IOException("Template vector file corrupt. Incorrect number of dimensions(index=" + index
                    + ", dim=" + dim + ").");
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Template vector file correctly loaded.");
    }

    protected String readTemplateVectorFileHeader(BufferedReader br) throws IOException {
        String line = null;
        lineNumber = 0;
        // PROCESS HEADER with arbitrary number of comment lines & lines starting with $
        while ((line = br.readLine()) != null) {
            lineNumber++;
            if (line.startsWith("#")) { // ignore comments
                continue;
            }
            if (!line.startsWith("$")) {
                break;
            }

            if (line.startsWith("$TYPE")) {
                // ignore
            } else if (line.startsWith("$XDIM")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    numInfo = Integer.parseInt(lineElements[1]);
                    if (numInfo < 2) {
                        throw new IOException(
                                "Template vector file format corrupt. At least 2 columns (number, label) required.");
                    }
                } else {
                    throw new IOException(getErrorMessage("$XDIM requires a numeric parameter.", line, lineNumber));
                }
            } else if (line.startsWith("$YDIM")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    try {
                        numVectors = Integer.parseInt(lineElements[1]);
                    } catch (NumberFormatException e) {
                        throw new IOException(getErrorMessage("$YDIM requires a valid numeric parameter.", line,
                                lineNumber));
                    }
                } else {
                    throw new IOException(getErrorMessage("$YDIM requires a numeric parameter.", line, lineNumber));
                }
            } else if (line.startsWith("$VEC_DIM") || line.startsWith("$VECDIM")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    try {
                        dim = Integer.parseInt(lineElements[1]);
                    } catch (NumberFormatException e) {
                        throw new IOException(getErrorMessage("$VEC_DIM requires a numeric parameter.", line,
                                lineNumber));
                    }
                } else {
                    throw new IOException(getErrorMessage("$VEC_DIM requires a numeric parameter.", line, lineNumber));
                }
            }
        }
        return line;
    }

    private String getErrorMessage(String messageDetail, String line, int lineNumber) {
        return "Template vector file corrupt: " + messageDetail + " " + getErrorDetails(line, lineNumber);
    }

    private String getErrorDetails(String line, int lineNumber) {
        return "(in line " + lineNumber + ": '" + line + "')";
    }

    /**
     * Parse information from one single line in the {@link TemplateVector} file, representing one attribute.
     * 
     * @param index the index (number) of this attribute
     * @param lineElements the elements of this line, split by the delimiter
     */
    protected void processLine(int index, String[] lineElements) {
        elements[index] = new TemplateVectorElement(this, lineElements[1].trim(), index);
        elementMap.put(elements[index].getLabel(), elements[index]);
        if (numInfo > 2) {
            elements[index].setDocumentFrequency(Integer.parseInt(lineElements[2]));
        }
        if (numInfo > 3) {
            elements[index].setCollectionTermFrequency(Integer.parseInt(lineElements[3]));
        }
        if (numInfo > 4) {
            elements[index].setMinimumTermFrequency(Integer.parseInt(lineElements[4]));
        }
        if (numInfo > 5) {
            elements[index].setMaximumTermFrequency(Integer.parseInt(lineElements[5]));
        }
        if (numInfo > 6) {
            elements[index].setMeanTermFrequency(Double.parseDouble(lineElements[6]));
        }
        if (numInfo > 7) {
            elements[index].setComment(lineElements[7]);
        }
    }

    /** Sets the names of the vector elements. */
    public void setComponentNames(String[] componentNames) {
        for (int i = 0; i < componentNames.length; i++) {
            elements[i] = new TemplateVectorElement(this, componentNames[i], i);
        }
    }

    public void setLabel(int index, String label) {
        elements[index].setLabel(label);
    }
}
