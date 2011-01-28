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
package at.tuwien.ifs.somtoolbox.util.inputVerifier;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * Verifies that the given input contains only {@link Double} numbers. Currently, {@link #verify(JComponent)} only
 * supports {@link JTextField}.
 * 
 * @author Rudolf Mayer
 * @version $Id: DoubleNumberInputVerifier.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class DoubleNumberInputVerifier extends InputVerifier {
    @Override
    public boolean verify(JComponent input) {
        if (input instanceof JTextField) {
            String text = ((JTextField) input).getText();
            try {
                Double.parseDouble(text);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return true;
        }
    }
}