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

import java.awt.Frame;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Observable;

import javax.swing.JFileChooser;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.MySOMVisualisationDataFileFilter;

/**
 * @author Rudolf Mayer
 * @version $Id: SOMVisualisationData.java 3628 2010-07-07 22:58:37Z mayer $
 */
public class SOMVisualisationData extends Observable {
    public static final String CLASS_INFO = "Class Information File";

    public static final String DATA_INFO = "Data Information File";

    public static final String DATA_WINNER_MAPPING = "Data Winner Mapping File";

    public static final String INPUT_VECTOR = "Input Vector File";

    public static final String TEMPLATE_VECTOR = "Template Vector File";

    public static final String QUALITY_MEASURE_CACHE_FILE = "QM Cache File";

    public static final String LINKAGE_MAP = "Date Item Linkage Map";

    public static final String INPUT_CORRECTIONS = "Input Corrections";

    public static final String INPUT_VECTOR_DISTANCE_MATRIX = "Input Vector Distance Matrix";

    public static final String REGRESS_INFORMATION = "Regression Information File";

    public static final String ADAPTIVE_COORDINATES = "Adaptive Coordinates File";

    private Class<?> classType;

    private Object data;

    private String[] extensions;

    private String fileName;

    private String type;

    public SOMVisualisationData(String[] extensions, Object data, Class<?> classType, String type) {
        this.extensions = extensions;
        this.data = data;
        this.classType = classType;
        this.type = type;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public Object getData() {
        return data;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public String getFileName() {
        return fileName;
    }

    private String getMessageFromException(Exception e) {
        String message = "";
        if (e.getMessage() != null) {
            message += "\n" + e.getMessage();
        }
        if (e.getCause() != null) {
            message += "\n" + e.getCause().getMessage();
        }
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean loadFromFile(JFileChooser fileChooser, Frame parentFrame) throws SOMToolboxException {
        if (fileChooser.getSelectedFile() != null) { // reusing the dialog
            fileChooser = new JFileChooser(fileChooser.getSelectedFile().getPath());
        }
        fileChooser.setFileFilter(new MySOMVisualisationDataFileFilter(this));
        fileChooser.setName(getType());
        int returnVal = fileChooser.showDialog(parentFrame, "Open " + getType());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            readFromFile(fileChooser.getSelectedFile().getAbsolutePath());
            return true;
        } else {
            return false;
        }

    }

    public void readFromFile(String fileName) throws SOMToolboxException {
        try {
            Constructor<?> constr = getClassType().getConstructor(new Class[] { String.class });
            setData(constr.newInstance(new Object[] { fileName }));
            setFileName(fileName);
        } catch (InvocationTargetException e) {
            throw new SOMToolboxException(e.getCause().getMessage(), e.getCause());
        } catch (Exception e) {
            try {
                Method method = getClassType().getMethod("initFromFile", String.class);
                Object obj = method.invoke(null, fileName);
                setData(obj);
                setFileName(fileName);
            } catch (InvocationTargetException e2) {
                throw new SOMToolboxException(e2.getCause().getMessage(), e2.getCause());
            } catch (Exception e2) {
                e.printStackTrace();
                e2.printStackTrace();
                throw new SOMToolboxException("Error loading file!\n" + getMessageFromException(e), e);
            }
        }
    }

    public void setData(Object data) {
        this.data = data;
        setChanged();
        notifyObservers(data);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
