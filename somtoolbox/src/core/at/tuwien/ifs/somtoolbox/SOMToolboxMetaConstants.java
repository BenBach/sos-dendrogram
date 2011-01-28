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
package at.tuwien.ifs.somtoolbox;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class SOMToolboxMetaConstants {

    public static final File USER_CONFIG_DIR = new File(System.getProperty("user.home"), ".somtoolbox");

    public static final File USER_SOMVIEWER_PREFS = new File(USER_CONFIG_DIR, "somviewerrc");

    public static final String VERSION = "0.7.5-2.xCustom+b201101271726";

    public static String getVersion() {
        if (StringUtils.isNotBlank(VERSION)) {
            return VERSION;
        } else {
            return "custom-build-" + new SimpleDateFormat("yyyy-MM-dd_HH:mm").format(new Date()) + "";
        }
    }

}
