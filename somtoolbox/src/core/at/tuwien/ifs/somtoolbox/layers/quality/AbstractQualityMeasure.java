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

import java.lang.reflect.InvocationTargetException;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;

/**
 * Provides basic functionality for quality measure algorithms.
 * 
 * @author Michael Dittenbach
 * @version $Id: AbstractQualityMeasure.java 3883 2010-11-02 17:13:23Z frank $
 */
public abstract class AbstractQualityMeasure implements QualityMeasure {

    protected Layer layer;

    protected InputData data;

    protected String[] mapQualityNames;

    protected String[] mapQualityDescriptions;

    protected String[] unitQualityNames;

    protected String[] unitQualityDescriptions;

    /**
     * Instantiates a certain quality measure class specified by argument <code>mqName</code>.
     * 
     * @param qmName the name of the quality measure.
     * @return a quality measure object of class <code>mqName</code>.
     * @throws ClassNotFoundException if class denoted by argument <code>mqName</code> is not found.
     * @throws InstantiationException if if this Class represents an abstract class, an interface, an array class, a
     *             primitive type, or void; or if the class has no nullary constructor; or if the instantiation fails
     *             for some other reason.
     * @throws IllegalAccessException if the class or its nullary constructor is not accessible.
     */
    public static QualityMeasure instantiate(String qmName, Layer layer, InputData data) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        QualityMeasure measure = null;
        Class<?>[] argsClasses = new Class[] { Layer.class, InputData.class };
        Object[] argsValues = new Object[] { layer, data };
        measure = (QualityMeasure) Class.forName(qmName).getConstructor(argsClasses).newInstance(argsValues);
        return measure;
    }

    public AbstractQualityMeasure(Layer layer, InputData data) {
        this.layer = layer;
        this.data = data;
    }

    @Override
    public final String[] getMapQualityNames() {
        return mapQualityNames;
    }

    @Override
    public final String[] getUnitQualityNames() {
        return unitQualityNames;
    }

    @Override
    public String[] getMapQualityDescriptions() {
        return mapQualityDescriptions;
    }

    @Override
    public String[] getUnitQualityDescriptions() {
        return unitQualityDescriptions;
    }

    /**
     * @param qmName "<code>ClassName.methodName</code>"
     * @return an array containing the class name on index 0, and the method name on index 1.
     */
    public static String[] splitNameAndMethod(String qmName) {
        String qmClassName = null;
        String qmMethodName = null;

        int lastDotIndex = qmName.lastIndexOf('.');
        qmClassName = qmName.substring(0, lastDotIndex);
        qmMethodName = qmName.substring(lastDotIndex + 1);
        return new String[] { qmClassName, qmMethodName };
    }
}
