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
package at.tuwien.ifs.somtoolbox.apps;

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.util.mnemonic.MnemonicSOMGenerator;

/**
 * Marker interface for all SOMToolbox Applications. Add this interface to each class conatining a main-method that is
 * part of the "production" framework. <strong>Attention!</strong> Classes implementing this Interface <b>must</b>
 * contain a <code>main</code> method, and the following members:
 * <ul>
 * <li><code>public static {@link Type} APPLICATION_TYPE</code></li>
 * <li><code>public static String DESCRIPTION</code></li>
 * <li><code>public static String LONG_DESCRIPTION</code></li>
 * <li><code>public static {@link Parameter}[] OPTIONS</code></li>
 * </ul>
 * 
 * @author Jakob Frank
 * @version $Id: SOMToolboxApp.java 3695 2010-07-16 13:05:16Z frank $
 */
public interface SOMToolboxApp {

    public static final Comparator<Class<? extends SOMToolboxApp>> TYPE_GROUPED_COMPARATOR = new Comparator<Class<? extends SOMToolboxApp>>() {

        @Override
        public int compare(Class<? extends SOMToolboxApp> o1, Class<? extends SOMToolboxApp> o2) {
            Type t1 = Type.getType(o1);
            Type t2 = Type.getType(o2);
            if (t1.equals(t2)) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            } else {
                return t1.compareTo(t2);
            }

        }
    };

    /**
     * This enum lists the known application types.
     * 
     * @author Jakob Frank
     * @version $Id: SOMToolboxApp.java 3695 2010-07-16 13:05:16Z frank $
     */
    public static enum Type {
        /**
         * Main-Classes marked with {@link #Training} are used to create new SOMs, etc...
         */
        Training,
        /**
         * Main-Classes marked with {@link #Viewer} indicate Applications to <i>view</i> data. SOMs, InputData, etc...
         */
        Viewer,
        /**
         * {@link #Utils} are apps that useful but are not part of the core features, such as FileFormatConverter,
         * {@link MnemonicSOMGenerator}, etc...
         */
        Utils,
        /**
         * Other useful stuff.
         */
        Helper,
        /**
         * Everything else, the default value.
         */
        Other;

        public static Type getType(Class<? extends SOMToolboxApp> c) {
            try {
                return (Type) c.getField("APPLICATION_TYPE").get(null);
            } catch (Exception e) {
            }
            return Type.Other;
        }
    };

    @SuppressWarnings("unchecked")
    public static final Map<String, Class<?>> REQUIRED_MEMBERS = ArrayUtils.toMap(new Object[][] {
            { "APPLICATION_TYPE", Type.class }, { "DESCRIPTION", String.class }, { "LONG_DESCRIPTION", String.class },
            { "OPTIONS", Parameter[].class } });

    public static final String DEV_BY_STRING = "Developed by the IR Team at IFS (Vienna University of Technology)";

    public static final String HOMEPAGE = "http://www.ifs.tuwien.ac.at/dm/somtoolbox/";
}
