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
package at.tuwien.ifs.somtoolbox.apps.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * @author frank
 * @version $Id: SomFilePacker.java 3889 2010-11-03 12:45:46Z frank $
 */
public class SomFilePacker implements SOMToolboxApp {

    public static final String DESCRIPTION = "Packer to create autonomous SomFiles";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptOutputFileName(true),
            OptionFactory.getOptUnitDescriptionFile(true), OptionFactory.getOptWeightVectorFile(true),
            OptionFactory.getOptHighlightedDataNamesFile(false), OptionFactory.getOptClassInformationFile(false),
            OptionFactory.getOptRegressionInformationFile(false), OptionFactory.getOptMapDescriptionFile(false),
            OptionFactory.getOptDataInformationFileFile(false), OptionFactory.getOptFileNamePrefix(false),
            OptionFactory.getOptFileNameSuffix(false), OptionFactory.getOptDataWinnerMappingFile(false),
            OptionFactory.getOptInputVectorFile(false), OptionFactory.getOptTemplateVectorFile(false),
            OptionFactory.getOptLinkageFile(false), OptionFactory.getOptClassColoursFile(false),
            OptionFactory.getOptInputCorrections(false) };

    public static final Type APPLICATION_TYPE = Type.Utils;

    public static final String VERSION = "Java SOMToolbox\n1\n";

    private static final String META_INF = "META-INF";

    private static final String META_INF_VERSION = META_INF + "/version";

    private static final String META_INF_INFO = META_INF + "/info";

    private static final String META_INF_ROOT = META_INF + "/root";

    private Hashtable<SomFileEntries, File> mapData;

    public enum SomFileEntries {
        UNIT, WGT, MAP, CLS, REG, VEC, TV, DWM, LINK, COLOR;
        public static String getRootKey(SomFileEntries entry) {
            return entry.toString();
        }

        public static String getParamID(SomFileEntries e) {
            switch (e) {
                case UNIT:
                    return "unitDescriptionFile";
                case WGT:
                    return "weightVectorFile";
                case MAP:
                    return "mapDescriptionFile";
                case CLS:
                    return "classInformationFile";
                case REG:
                    return "regressionInformationFile";
                case VEC:
                    return "inputVectorFile";
                case TV:
                    return "templateVectorFile";
                case DWM:
                    return "dataWinnerMappingFile";
                case LINK:
                    return "linkageMapFile";
                case COLOR:
                    return "classColours";
            }
            return null;
        }

        public static SomFileEntries getEntryByParamID(String id) {
            if (id.equals("unitDescriptionFile")) {
                return UNIT;
            } else if (id.equals("weightVectorFile")) {
                return WGT;
            } else if (id.equals("mapDescriptionFile")) {
                return MAP;
            } else if (id.equals("classInformationFile")) {
                return CLS;
            } else if (id.equals("regressionInformationFile")) {
                return REG;
            } else if (id.equals("inputVectorFile")) {
                return VEC;
            } else if (id.equals("templateVectorFile")) {
                return TV;
            } else if (id.equals("dataWinnerMappingFile")) {
                return DWM;
            } else if (id.equals("linkageMapFile")) {
                return LINK;
            } else if (id.equals("classColours")) {
                return COLOR;
            }
            return null;
        }
    }

    /**
     * @param args Commandline arguments
     */
    public static void main(String[] args) {
        JSAPResult config = AbstractOptionFactory.parseResults(args, OPTIONS);

        SomFilePacker pack = new SomFilePacker();

        pack.addDataFile(config, "unitDescriptionFile");
        pack.addDataFile(config, "weightVectorFile");
        pack.addDataFile(config, "mapDescriptionFile");
        pack.addDataFile(config, "classInformationFile");
        pack.addDataFile(config, "regressionInformationFile");
        pack.addDataFile(config, "dataInformationFile");
        pack.addDataFile(config, "inputVectorFile");
        pack.addDataFile(config, "templateVectorFile");
        pack.addDataFile(config, "dataWinnerMappingFile");
        pack.addDataFile(config, "linkageMapFile");
        pack.addDataFile(config, "classColours");

        pack.addDataFiles(config.getString("fileNamePrefix"), config.getString("fileNameSuffix"));

        try {
            pack.writeSomFile(config.getString("output"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param prefix FileNamePrefix
     * @param suffix FileNameSuffix
     */
    private void addDataFiles(String prefix, String suffix) {
        // TODO: Add the data files to...
    }

    @SuppressWarnings("unused")
    private void addDataFiles(String prefix) {
        addDataFiles(prefix, "");
    }

    /**
     * 
     */
    public SomFilePacker() {
        mapData = new Hashtable<SomFileEntries, File>();
    }

    private void addDataFile(JSAPResult config, String id) {
        String f = config.getString(id);
        if (f == null) {
            return;
        }
        File file = new File(f);
        if (!file.canRead()) {
            System.err.println("Can't read file: " + file.getPath());
            return;
        }

        mapData.put(SomFileEntries.getEntryByParamID(id), file);
    }

    private void writeSomFile(String fname) throws IOException {
        FileOutputStream fos = new FileOutputStream(fname);
        CheckedOutputStream check = new CheckedOutputStream(fos, new CRC32());
        BufferedOutputStream bos = new BufferedOutputStream(check);
        ZipOutputStream som = new ZipOutputStream(bos);
        som.setComment("a comment");
        ZipEntry zeV = new ZipEntry(META_INF_VERSION);
        // zeV.setComment("System");
        // zeV.setMethod(ZipEntry.STORED);
        // zeV.setSize(VERSION.getBytes().length);
        // zeV.setCompressedSize(VERSION.getBytes().length);
        // CRC32 crc = new CRC32();
        // crc.update(VERSION.getBytes());
        // zeV.setCrc(crc.getValue());
        som.putNextEntry(zeV);
        som.write(VERSION.getBytes());

        ZipEntry zeI = new ZipEntry(META_INF_INFO);
        som.putNextEntry(zeI);
        som.write("\n".getBytes());

        Properties root = new Properties();
        for (SomFileEntries e : mapData.keySet()) {
            InputStream fis = FileUtils.getInputStream(SomFileEntries.getParamID(e), mapData.get(e));
            ZipEntry ze = new ZipEntry(cleanFilename(mapData.get(e).getName()));
            root.put(e.toString(), ze.getName());
            som.putNextEntry(ze);
            int count;
            byte[] data = new byte[1024];
            while ((count = fis.read(data, 0, 1024)) != -1) {
                som.write(data, 0, count);
            }
            fis.close();
        }

        ZipEntry zuR = new ZipEntry(META_INF_ROOT);
        som.putNextEntry(zuR);
        root.store(som, "Created with " + SomFilePacker.class.getSimpleName());

        som.close();
    }

    /**
     * Remove a .gz suffix and other stuff.
     */
    private String cleanFilename(String name) {
        if (name.endsWith(".gz")) {
            name = name.substring(0, name.length() - 3);
        }
        return name;
    }
}
