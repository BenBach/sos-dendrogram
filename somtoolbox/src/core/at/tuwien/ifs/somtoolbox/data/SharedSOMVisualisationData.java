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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.distance.InputVectorDistanceMatrix;
import at.tuwien.ifs.somtoolbox.input.DataItemLinkageMap;
import at.tuwien.ifs.somtoolbox.input.InputCorrections;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.layers.AdaptiveCoordinatesVirtualLayer;

/**
 * @author Rudolf Mayer
 * @version $Id: SharedSOMVisualisationData.java 3704 2010-07-20 10:42:42Z mayer $
 */
public class SharedSOMVisualisationData {
    private HashMap<String, SOMVisualisationData> objects = new HashMap<String, SOMVisualisationData>();

    public SOMLibClassInformation getClassInfo() {
        return (SOMLibClassInformation) getObject(SOMVisualisationData.CLASS_INFO).getData();
    }

    public SOMLibDataInformation getDataInfo() {
        return (SOMLibDataInformation) getObject(SOMVisualisationData.DATA_INFO).getData();
    }

    public SOMLibDataWinnerMapping getDataWinnerMapping() {
        return (SOMLibDataWinnerMapping) getObject(SOMVisualisationData.DATA_WINNER_MAPPING).getData();
    }

    public InputData getInputData() {
        return (InputData) getObject(SOMVisualisationData.INPUT_VECTOR).getData();
    }

    public SOMLibTemplateVector getTemplateVector() {
        return (SOMLibTemplateVector) getObject(SOMVisualisationData.TEMPLATE_VECTOR).getData();
    }

    public SOMLibRegressInformation getSOMLibRegressInformation() {
        return (SOMLibRegressInformation) getObject(SOMVisualisationData.REGRESS_INFORMATION).getData();
    }

    public AdaptiveCoordinatesVirtualLayer getAdaptiveCoordinates() {
        return (AdaptiveCoordinatesVirtualLayer) getObject(SOMVisualisationData.ADAPTIVE_COORDINATES).getData();
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getLinkageMap() {
        return (Map<String, String>) getObject(SOMVisualisationData.LINKAGE_MAP).getData();
    }

    public InputCorrections getInputCorrections() {
        return (InputCorrections) getObject(SOMVisualisationData.INPUT_CORRECTIONS).getData();
    }

    public InputVectorDistanceMatrix getInputVectorDistanceMatrix() {
        return (InputVectorDistanceMatrix) getObject(SOMVisualisationData.INPUT_VECTOR_DISTANCE_MATRIX).getData();
    }

    public SharedSOMVisualisationData() {
        // add known objects
        // other objects need to be added by the visualisation that needs it
        objects.put(SOMVisualisationData.CLASS_INFO, new SOMVisualisationData(new String[] { "cls", "clsinf",
                "clsinfo", "classinfo", "txt" }, null, SOMLibClassInformation.class, SOMVisualisationData.CLASS_INFO));
        objects.put(SOMVisualisationData.REGRESS_INFORMATION, new SOMVisualisationData(
                new String[] { "reginf", "txt" }, null, SOMLibRegressInformation.class,
                SOMVisualisationData.REGRESS_INFORMATION));
        objects.put(SOMVisualisationData.DATA_INFO, new SOMVisualisationData(new String[] { "" }, null,
                SOMLibDataInformation.class, SOMVisualisationData.DATA_INFO));
        objects.put(SOMVisualisationData.DATA_WINNER_MAPPING, new SOMVisualisationData(new String[] { "dwm" }, null,
                SOMLibDataWinnerMapping.class, SOMVisualisationData.DATA_WINNER_MAPPING));
        objects.put(SOMVisualisationData.INPUT_VECTOR, new SOMVisualisationData(new String[] { "", "tfxidf" }, null,
                SOMLibSparseInputData.class, SOMVisualisationData.INPUT_VECTOR));
        objects.put(SOMVisualisationData.TEMPLATE_VECTOR, new SOMVisualisationData(new String[] { "tv" }, null,
                SOMLibTemplateVector.class, SOMVisualisationData.TEMPLATE_VECTOR));
        objects.put(SOMVisualisationData.LINKAGE_MAP, new SOMVisualisationData(new String[] { "linkage" }, null,
                DataItemLinkageMap.class, SOMVisualisationData.LINKAGE_MAP));
        objects.put(SOMVisualisationData.INPUT_CORRECTIONS, new SOMVisualisationData(new String[] { "correction" },
                null, InputCorrections.class, SOMVisualisationData.INPUT_CORRECTIONS));
        objects.put(SOMVisualisationData.INPUT_VECTOR_DISTANCE_MATRIX, new SOMVisualisationData(new String[] { "dist",
                "dist.bin" }, null, InputVectorDistanceMatrix.class, SOMVisualisationData.INPUT_VECTOR_DISTANCE_MATRIX));
        objects.put(SOMVisualisationData.ADAPTIVE_COORDINATES, new SOMVisualisationData(
                new String[] { AdaptiveCoordinatesVirtualLayer.FILE_EXTENSION }, null,
                AdaptiveCoordinatesVirtualLayer.class, SOMVisualisationData.ADAPTIVE_COORDINATES));

    }

    public SharedSOMVisualisationData(String classInformationFileName, String regressionInformationFileName,
            String dataInformationFileName, String dataWinnerMappingFileName, String inputVectorFileName,
            String templateVectorFileName, String linkageMapFileName) {
        this();
        setFileName(SOMVisualisationData.CLASS_INFO, classInformationFileName);
        setFileName(SOMVisualisationData.REGRESS_INFORMATION, regressionInformationFileName);
        setFileName(SOMVisualisationData.DATA_INFO, dataInformationFileName);
        setFileName(SOMVisualisationData.DATA_WINNER_MAPPING, dataWinnerMappingFileName);
        setFileName(SOMVisualisationData.INPUT_VECTOR, inputVectorFileName);
        setFileName(SOMVisualisationData.TEMPLATE_VECTOR, templateVectorFileName);
        setFileName(SOMVisualisationData.LINKAGE_MAP, linkageMapFileName);
    }

    public SharedSOMVisualisationData(String classInformationFileName, String dataWinnerMappingFileName,
            String inputVectorFileName, String templateVectorFileName) {
        this(classInformationFileName, null, null, dataWinnerMappingFileName, inputVectorFileName,
                templateVectorFileName, null);
    }

    public SOMVisualisationData getObject(String name) {
        return objects.get(name);
    }

    public Object getData(String name) {
        return objects.get(name).getData();
    }

    /**
     * reads all available input files
     */
    public void readAvailableData() {
        for (SOMVisualisationData data : objects.values()) {
            if (data.getData() == null && data.getFileName() != null && !data.getFileName().equals("")) { // we got no
                // data, but
                // got a file
                // name
                try {
                    data.readFromFile(data.getFileName());

                    // FIXME this is a bad hack, but needed until #78
                    // (https://olymp.ifs.tuwien.ac.at/trac/somtoolbox/ticket/78) is implemented
                    if (data.getType().equals(SOMVisualisationData.TEMPLATE_VECTOR)) {
                        if (getInputData() != null) {
                            getInputData().setTemplateVector((TemplateVector) data.getData());
                        }
                    }
                } catch (SOMToolboxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setData(String name, Object data) {
        objects.get(name).setData(data);
    }

    public void setFileName(String name, String fileName) {
        objects.get(name).setFileName(fileName);
    }

    public void setData(String name, Object data, String fileName) {
        setData(name, data);
        setFileName(name, fileName);
    }

    public void setInputCorrections(String fileName) {
        setFileName(SOMVisualisationData.INPUT_CORRECTIONS, fileName);
    }

    public int size() {
        return objects.size();
    }

    public Iterator<String> iterator() {
        return objects.keySet().iterator();
    }

    public SOMVisualisationData[] getObjects() {
        return objects.values().toArray(new SOMVisualisationData[size()]);
    }

    /** Compute the hash code as a sum of the codes of the data in the values of the {@link #objects} */
    public int dataHashCode() {
        int code = 0;
        for (SOMVisualisationData data : objects.values()) {
            if (data.getData() != null) {
                code += data.getData().hashCode();
            }
        }
        return code;
    }

    public static void main(String[] args) throws SOMToolboxException {
        // testing the hash code generation
        SharedSOMVisualisationData data = new SharedSOMVisualisationData();
        System.out.println(data.dataHashCode());
        Object data2 = "Test1";
        data.setData(SOMVisualisationData.CLASS_INFO, data2);
        System.out.println(data.dataHashCode());
        data.setData(SOMVisualisationData.CLASS_INFO, "Test2");
        System.out.println(data.dataHashCode());
        data.setData(SOMVisualisationData.CLASS_INFO, data2);
        System.out.println(data.dataHashCode());
    }

}
