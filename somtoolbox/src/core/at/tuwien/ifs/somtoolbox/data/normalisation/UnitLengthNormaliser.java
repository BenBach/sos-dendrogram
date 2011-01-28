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
package at.tuwien.ifs.somtoolbox.data.normalisation;

import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * Unit length normalisation, normalises the length of the instance to 1.
 * 
 * @author Rudolf Mayer
 * @version $Id: UnitLengthNormaliser.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class UnitLengthNormaliser extends AbstractNormaliser {

    @Override
    protected void processLine(int index, String[] lineElements) throws Exception {
        double[] vector = new double[dim];
        for (int ve = 0; ve < dim; ve++) {
            double value = Double.parseDouble(lineElements[ve]);
            setMatrixValue(index, ve, value);
            vector[ve] = value;
        }
        vector = VectorTools.normaliseVectorToUnitLength(vector);
        for (int ve = 0; ve < dim; ve++) {
            if (vector[ve] == 0) {
                writer.write("0 ");
            } else {
                writer.write(StringUtils.format(vector[ve], 15) + " ");
            }
        }
        writer.write(lineElements[dim]);
        writer.newLine();
    }

    @Override
    public void preReading() {
        // we will do everything during reading
    }

    @Override
    public void postReading() {
        // we did everything during reading already
    }

}
