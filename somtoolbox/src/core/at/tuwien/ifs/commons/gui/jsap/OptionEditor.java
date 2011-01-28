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
package at.tuwien.ifs.commons.gui.jsap;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Option;
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.martiansoftware.jsap.stringparsers.IntegerStringParser;

/**
 * @author Jakob Frank
 * @version $Id: OptionEditor.java 3867 2010-10-21 15:50:10Z mayer $
 */
public abstract class OptionEditor extends JPanel {

    private static final long serialVersionUID = 1L;

    final Option option;

    protected boolean userInput = false;

    public OptionEditor(Option option) {
        this.option = option;
        setLayout(new GridBagLayout());

    }

    String getFlatDefaultList() {
        StringBuilder sb = new StringBuilder();
        if (option.getDefault() != null) {
            for (int i = 0; i < option.getDefault().length; i++) {
                if (i > 0) {
                    sb.append(option.getListSeparator());
                }
                sb.append(option.getDefault()[i]);
            }
        }

        return sb.toString();
    }

    abstract boolean checkValidity();

    abstract String getArgument();

    static OptionEditor createParameterEditor(Option option) {
        StringParser parser = option.getStringParser();
        if (parser instanceof IntegerStringParser) {
            return new IntegerEditor(option);
        } else if (parser instanceof BooleanStringParser) {
            return new BooleanEditor(option);
        } else if (parser instanceof EnumeratedStringParser) {
            return new EnumerationEditor(option);
        } else if (parser instanceof FileStringParser) {
            return new FileEditor(option);
        } else {
            return new StringEditor(option);
        }
    }

    /**
     * @return <code>true</code> if the user changed the content.
     */
    public boolean containsUserInput() {
        return userInput;
    }

    /**
     * Sets the initial value by retrieving it from the given {@link JSAPResult}
     * 
     * @param result the {@link JSAPResult}
     */
    public abstract void setInitialValue(JSAPResult result);
}
