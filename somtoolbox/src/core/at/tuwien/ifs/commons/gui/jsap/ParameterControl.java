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

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Jakob Frank
 * @version $Id: ParameterControl.java 3867 2010-10-21 15:50:10Z mayer $
 */
public abstract class ParameterControl {

    private static final long serialVersionUID = 1L;

    protected static final int TOOL_TIP_WIDTH = 60;

    private static final JLabel DEFAULT_EDITOR = new JLabel("<unknown parameter type>");

    private Color labelFGColor = null;

    private JLabel label = null;

    private final Parameter param;

    /**
     * 
     */
    public ParameterControl(Parameter param) {
        this.param = param;
    }

    /**
     * @return The Control of this parameter
     */
    public JComponent getEditor() {
        return DEFAULT_EDITOR;
    }

    /**
     * @return The commandline part of this parameter
     */
    public abstract String[] getCommandLine();

    public boolean isRequired() {
        return false;
    }

    public static ParameterControl createParameterControl(Parameter param, JSAPResult result)
            throws SOMToolboxException {
        if (param instanceof Switch) {
            return new SwitchControl((Switch) param, result);
        } else if (param instanceof FlaggedOption) {
            return new FlaggedOptionControl((FlaggedOption) param, result);
        } else if (param instanceof UnflaggedOption) {
            return new UnflaggedOptionControl((UnflaggedOption) param, result);
        } else {
            // TODO: throw a better error...
            throw new SOMToolboxException("Should/Must not happen...");
        }
    }

    static String createFlagString(char shortFlag, String longFlag) {
        if (shortFlag != JSAP.NO_SHORTFLAG) {
            return "-" + shortFlag;
        } else {
            return "--".concat(longFlag);
        }
    }

    /**
     * @return check if the content if the editor is valid.
     */
    public boolean validate() {
        if (!isValid()) {
            labelFGColor = getLabel().getForeground();
            getLabel().setForeground(Color.RED);
        } else {
            if (labelFGColor != null) {
                getLabel().setForeground(labelFGColor);
            }
        }
        return isValid();
    }

    abstract boolean isValid();

    public JLabel getLabel() {
        if (label == null) {
            label = new JLabel(param.getUsageName() + ":" + (isRequired() ? "*" : ""));
            label.setToolTipText(formatToolTip(param.getHelp() + (isRequired() ? " (required)" : "")));
        }
        return label;
    }

    protected static String formatToolTip(String text) {
        return "<html>".concat(StringUtils.wrap(text, TOOL_TIP_WIDTH, "<br>")).concat("</html>");
    }
}
