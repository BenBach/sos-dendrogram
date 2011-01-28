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

import java.io.IOException;
import java.util.ArrayList;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.UnflaggedOption;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.util.CollectionUtils;

/**
 * Compares the contents of two template vectors.
 * 
 * @author Rudolf Mayer
 * @version $Id: TemplateVectorComparator.java 3676 2010-07-15 09:08:14Z frank $
 */
public class TemplateVectorComparator implements SOMToolboxApp {

    public static final Parameter[] OPTIONS = new Parameter[] {
            new UnflaggedOption("templateVectorFile", JSAP.STRING_PARSER, true, "First template vector file."),
            new UnflaggedOption("templateVectorFile2", JSAP.STRING_PARSER, true, "Second template vector file.") };

    public static final String DESCRIPTION = "Compares the contents of two template vectors";

    public static final String LONG_DESCRIPTION = DESCRIPTION;

    public static final Type APPLICATION_TYPE = Type.Utils;

    public static void main(String[] args) throws IOException {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);
        SOMLibTemplateVector tv1 = new SOMLibTemplateVector(config.getString("templateVectorFile"));
        SOMLibTemplateVector tv2 = new SOMLibTemplateVector(config.getString("templateVectorFile2"));
        ArrayList<String>[] uniqueElements = CollectionUtils.getUniqueElements(tv1.getLabelsAsList(),
                tv2.getLabelsAsList());
        printTerms(uniqueElements[0], tv1, 1, config.getString("templateVectorFile"));
        printTerms(uniqueElements[1], tv2, 2, config.getString("templateVectorFile2"));
    }

    private static void printTerms(final ArrayList<String> onlyInOne, SOMLibTemplateVector tv1, int i, String title) {
        System.out.println("\n==============================================================");
        System.out.println("Terms only in vector " + i + " (" + onlyInOne.size() + ", " + title + ")");
        for (String s : onlyInOne) {
            System.out.println("\t" + tv1.getElement(s));
        }
    }

}
