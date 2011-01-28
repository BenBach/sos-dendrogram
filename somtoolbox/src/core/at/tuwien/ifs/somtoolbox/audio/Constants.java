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
package at.tuwien.ifs.somtoolbox.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * @author Ewald Peiszer
 * @version $Id: Constants.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class Constants {

    public static final boolean BIG_ENDIAN = false;

    public static final AudioFormat DATALINE_FORMAT = new AudioFormat(44100, 16, 2, true, Constants.BIG_ENDIAN);

    public static final DataLine.Info DATALINE_FORMAT_INFO = new DataLine.Info(SourceDataLine.class, DATALINE_FORMAT);

    public static final String DECODED_SUFFIX = " Decoded.wav";

    public static final AudioFormat MONO_FORMAT = new AudioFormat(44100, 16, 1, true, BIG_ENDIAN);

}
