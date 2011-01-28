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
package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Displays a tool-tip window displayed on mouse-over events in the {@link SOMPane}. This class makes use of the <a
 * href="http://www.cs.umd.edu/hcil/jazz/" target="_blank">Piccolo framework</a>.
 * 
 * @author Michael Dittenbach
 * @version $Id: ToolTipPNode.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class ToolTipPNode extends PText {
    private static final long serialVersionUID = 1L;

    private Rectangle2D border = new Rectangle2D.Double();

    private final Color backgroundColor = Color.decode("#feffb9");

    private final Color borderColor = Color.decode("#fcff00");

    private final int fontSize = 10;

    public ToolTipPNode() {
        super();
        setFont(new Font("Sans", Font.PLAIN, fontSize));
    }

    public ToolTipPNode(String aText) {
        super(aText);
        setFont(new Font("Sans", Font.PLAIN, fontSize));
    }

    @Override
    protected void paint(PPaintContext paintContext) {
        Graphics2D g2d = paintContext.getGraphics();
        // super.paint(paintContext);

        border.setRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        g2d.setStroke(new BasicStroke(1f));
        g2d.setPaint(backgroundColor);
        g2d.fill(border);
        g2d.setColor(borderColor);
        g2d.draw(border);

        super.paint(paintContext);
    }

    @Override
    public boolean getPickable() {
        return false;
    }
}
