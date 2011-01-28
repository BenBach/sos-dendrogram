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

import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.normalisation.MinMaxNormaliser;
import at.tuwien.ifs.somtoolbox.data.normalisation.StandardScoreNormaliser;
import at.tuwien.ifs.somtoolbox.data.normalisation.UnitLengthNormaliser;

/**
 * Handles the normalization of vector files in SOMLib format. This class can be run in standalone mode taking two
 * arguments, i.e. input and output file. If the input file is gzip-compressed, the output will also be written
 * gzip-compressed. The .gz suffix has to be specified manually in order not to alter filenames to something other than
 * intended by the user.
 * <p>
 * <i>Created on Mar 16, 2004</i>
 * </p>
 * 
 * @author Michael Dittenbach
 * @version $Id: SOMLibVectorNormalization.java 3835 2010-10-08 14:45:09Z mayer $
 */
public class SOMLibVectorNormalization implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptNormMethod(false),
            OptionFactory.getOptInputFileName(), OptionFactory.getOptOutputVector() };

    public static final String DESCRIPTION = "Handles the normalization of vector files in SOMLib format";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Helper;

    /**
     * Static method for standalone invocation.
     * 
     * @param args Usage: method-type input-filename output-filename
     */
    public static void main(String[] args) {
        // -n normalization type, opt., default=UNIT_LEN
        // input file
        // output file

        // register and parse all options for the SOMLibVectorNormalization
        JSAP jsap = OptionFactory.registerOptions(OPTIONS);
        JSAPResult config = OptionFactory.parseResults(args, jsap);

        String method = config.getString("method");
        String inputFileName = AbstractOptionFactory.getFilePath(config, "input");
        String outputFileName = AbstractOptionFactory.getFilePath(config, "output");

        try {
            if (method.equals("UNIT_LEN")) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting normalisation to unit length");
                new UnitLengthNormaliser().normalise(inputFileName, outputFileName);
            } else if (method.equals("MIN_MAX")) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting min-max normalisation");
                new MinMaxNormaliser().normalise(inputFileName, outputFileName);
            } else if (method.equals("STANDARD_SCORE")) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting standard score normalisation");
                new StandardScoreNormaliser().normalise(inputFileName, outputFileName);
            }
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("finished Normalization");
    }

    /**
     * No normalisation.
     */
    public static final int NONE = 0;

    /**
     * Normalise vectors to unit length.
     */
    public static final int UNIT_LEN = 1;
}
