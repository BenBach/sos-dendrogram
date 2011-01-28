/*
 * Copyright 2004-2010 Institute of Software Technology and Interactive Systems, Vienna University of Technology
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
package at.tuwien.ifs.commons.util.jsap;

import java.util.ArrayList;

import com.martiansoftware.jsap.Option;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.SubClassFinder;

/**
 * Helper class for bash_completion generation. (Not automated)
 * 
 * @author frank
 * @version $Id: EnumeratedStringParserLister.java 3977 2010-12-16 13:39:01Z frank $
 */
public class EnumeratedStringParserLister {

    /**
     */
    public static void main(String[] args) {

        ArrayList<Class<? extends SOMToolboxApp>> runnables = SubClassFinder.findSubclassesOf(SOMToolboxApp.class, true);

        for (Class<? extends SOMToolboxApp> app : runnables) {
            try {
                Parameter[] options = (Parameter[]) app.getField("OPTIONS").get(null);
                for (Parameter param : options) {
                    if (param instanceof Option) {
                        Option opt = (Option) param;
                        if (opt.getStringParser() instanceof EnumeratedStringParser) {
                            EnumeratedStringParser sp = (EnumeratedStringParser) opt.getStringParser();
                            String[] vals = sp.getValidOptionValues();
                            System.out.printf("%s%n", opt.getSyntax());
                            System.out.printf("\t%S)%n\t\tCOMPREPLY=( $(compgen -W \"%s\" -- \"$cur\") )%n\t\t;;%n",
                                    app.getSimpleName(), StringUtils.toString(vals, "", "", " "));
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore...
            }
        }
    }

}
