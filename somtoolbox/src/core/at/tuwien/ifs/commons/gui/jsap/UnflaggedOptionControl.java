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

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * @author Jakob Frank
 * @version $Id: UnflaggedOptionControl.java 3867 2010-10-21 15:50:10Z mayer $
 */
public class UnflaggedOptionControl extends ParameterControl {

    private OptionEditor editor = null;

    private final UnflaggedOption opt;

    /**
     * @param opt The {@link UnflaggedOption} this editor is for.
     */
    public UnflaggedOptionControl(UnflaggedOption opt) {
        super(opt);
        this.opt = opt;
    }

    /**
     * @param opt The {@link UnflaggedOption} this editor is for.
     */
    public UnflaggedOptionControl(UnflaggedOption opt, JSAPResult result) {
        this(opt);
        getEditor().setInitialValue(result);
    }

    @Override
    public OptionEditor getEditor() {
        if (editor == null) {
            editor = OptionEditor.createParameterEditor(opt);
            editor.setToolTipText(getLabel().getToolTipText());
        }
        return editor;
    }

    @Override
    public String[] getCommandLine() {
        return new String[] { editor.getArgument() };
    }

    @Override
    public boolean isRequired() {
        return opt.required();
    }

    @Override
    boolean isValid() {
        return editor.checkValidity();
    }
}
