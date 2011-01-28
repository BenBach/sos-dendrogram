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
package at.tuwien.ifs.somtoolbox.apps.viewer.fileutils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * This class is mainly used to convert legacy descriptions into the PocketSOMFormat
 * <p>
 * <i>Created on Jan 21, 2005</i>
 * </p>
 * 
 * @author Robert Neumayer
 * @author Rudolf Mayer
 * @version $Id: PocketSOMFormatUtils.java 3586 2010-05-21 10:34:19Z mayer $
 */
public class PocketSOMFormatUtils {

    /**
     * get the max number of mapped instances of a unit for the given map
     */
    public static int getMaxNumberOfMappedElements(SOMLibFormatInputReader ir) {
        int x = ir.getXSize();
        int y = ir.getYSize();
        int max = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (ir.getMappedVecs(i, j) != null) {
                    if (ir.getMappedVecs(i, j).length > max) {
                        max = ir.getMappedVecs(i, j).length;
                    }
                }
            }
        }
        return max;
    }

    /**
     * Gets the max number of mapped instances of a unit for the GrowingLayer.
     */
    public static int getMaxNumberOfMappedElements(GrowingLayer layer) {
        int x = layer.getXSize();
        int y = layer.getYSize();
        int max = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                try {
                    Unit unit = layer.getUnit(i, j);
                    if (unit != null) {
                        if (unit.getMappedInputNames() != null) {
                            max = Math.max(unit.getMappedInputNames().length, max);
                        }
                    }
                } catch (LayerAccessException e) {
                    // should not happen
                    e.printStackTrace();
                }
            }
        }
        return max;
    }

    /**
     * reads a given map description in SOMLibInputFormat and writes to the given file in PocketSOMFormat
     * 
     * @param outputFileName name of the PocketSOM formatted file to be created
     */
    public static void convertMapFormat(String wgt, String unit, String map, String outputFileName) {
        SOMLibFormatInputReader ir = null;
        try {
            ir = new SOMLibFormatInputReader(wgt, unit, map);
            System.out.println("Finished reading SOMLibFormat");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SOMLibFileFormatException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName));

            int x = ir.getXSize();
            int y = ir.getYSize();

            out.write(x + "\n");
            out.write(y + "\n");
            out.write(PocketSOMFormatUtils.getMaxNumberOfMappedElements(ir) + "\n");

            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    if (ir.getMappedVecs(i, j) != null) {
                        String[] vectors = ir.getMappedVecs(i, j);
                        for (String vector : vectors) {
                            // System.out.println("Writing: " + i + " " + j + " " + vectors[k]);
                            out.write(i + " " + j + " " + vector + "\n");
                        }
                    }
                }
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static StringBuilder createPocketSomMapping(GrowingLayer layer) {
        final String nl = "\n";
        StringBuilder res = new StringBuilder();

        int x = layer.getXSize();
        int y = layer.getYSize();

        res.append(x).append(nl);
        res.append(y).append(nl);
        res.append(PocketSOMFormatUtils.getMaxNumberOfMappedElements(layer)).append(nl);

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                String[] vectors;
                try {
                    Unit unit = layer.getUnit(i, j);
                    if (unit != null) {
                        vectors = unit.getMappedInputNames();
                        if (vectors != null) {
                            for (String vector : vectors) {
                                // System.out.println("Writing: " + i + " " + j + " " + vectors[k]);
                                res.append(i).append(" ").append(j).append(" ").append(vector).append(nl);
                            }
                        }
                    }
                } catch (LayerAccessException e) {
                    // should not happen
                    e.printStackTrace();
                }
            }
        }

        return res;
    }

    /**
     * Writes the map information of the given GrowingLayer to the given file in PocketSOMFormat
     * 
     * @param layer The layer containing the map information
     * @param outputFileName name of the PocketSOM formatted file to be created
     */
    public static void convertMapFormat(GrowingLayer layer, String outputFileName) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName));
            out.write(createPocketSomMapping(layer).toString());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * reads a given map description in SOMLibInputFormat and writes to the given file in PocketSOMFormat changing the
     * size of the map according to factor
     * 
     * @param outputFileName name of the PocketSOM formatted file to be created
     * @param factor 0 an empty map, 1.0 for a full map
     * @deprecated since streaming works this is not needed anymore (it never worked very well anyway)
     */
    @Deprecated
    public static void lightenMapFormat(String wgt, String unit, String map, String outputFileName, float factor) {
        SOMLibFormatInputReader ir = null;
        try {
            ir = new SOMLibFormatInputReader(wgt, unit, map);
            System.out.println("Finished reading SOMLibFormat");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SOMLibFileFormatException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName));
            BufferedWriter outNames = new BufferedWriter(new FileWriter(outputFileName + "protocol.txt"));

            int x = ir.getXSize();
            int y = ir.getYSize();

            out.write(x + "\n");
            out.write(y + "\n");
            out.write(PocketSOMFormatUtils.getMaxNumberOfMappedElements(ir) + "\n");

            int takeThisOne = (int) (1 / factor);
            System.out.println("Using every " + takeThisOne + " th song!");
            int counter = 0;
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    if (ir.getMappedVecs(i, j) != null) {

                        String[] vectors = ir.getMappedVecs(i, j);
                        for (String vector : vectors) {
                            // if(i % takeThisOne == 0){
                            System.out.println("Writing: " + i + " " + j + " " + vector);
                            out.write(i + " " + j + " " + vector + "\n");
                            outNames.write("cp /home/rn/dists/somtoolbox-0.4.1/mp3s/" + vector
                                    + ".mp3 /home/public/PDAMobileSOMViewer/mp3s\n");
                            counter++;
                            // }
                        }
                    }
                }
            }
            System.out.println("A total of " + counter + " songs survived");
            out.close();
            outNames.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
