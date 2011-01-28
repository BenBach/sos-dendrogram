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
package at.tuwien.ifs.somtoolbox.layers.quality;

/**
 * All quality measure algorithm implementations should implement this interface.
 * 
 * @author Michael Dittenbach
 * @version $Id: QualityMeasure.java 3358 2010-02-11 14:35:07Z mayer $
 */
public interface QualityMeasure {

    public double getMapQuality(String name) throws QualityMeasureNotFoundException;

    public String[] getMapQualityDescriptions();

    public String[] getMapQualityNames();

    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException;

    public String[] getUnitQualityDescriptions();

    public String[] getUnitQualityNames();

}
