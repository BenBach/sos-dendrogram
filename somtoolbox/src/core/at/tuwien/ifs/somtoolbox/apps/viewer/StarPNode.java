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
import java.awt.Graphics2D;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Visualises the input as a star on the map.
 * 
 * @author Khalid Latif
 * @version $Id: StarPNode.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class StarPNode extends InputPNode {

    private static final long serialVersionUID = 1l;

    private static final Color ORANGE = new Color(250, 117, 48);

    private static final Color YELLOW = new Color(255, 244, 164);

    private Color lineColor;

    public StarPNode() {
        lineColor = YELLOW;
        addInputEventListener(new PBasicInputEventHandler() {
            @Override
            public void mouseEntered(PInputEvent event) {
                lineColor = ORANGE;
                repaint();
            }

            @Override
            public void mouseExited(PInputEvent event) {
                lineColor = YELLOW;
                repaint();
            }
        });
    }

    public StarPNode(double x, double y) {
        super(x, y);
    }

    /** @see edu.umd.cs.piccolo.PNode#paint(edu.umd.cs.piccolo.util.PPaintContext) */
    @Override
    protected void paint(PPaintContext paintContext) {
        Graphics2D g2 = paintContext.getGraphics();
        g2.setStroke(new BasicStroke(1f));
        // g2.setPaint(new TexturePaint());
        g2.setPaint(lineColor);
        int x1 = (int) getX();
        int y1 = (int) getY();
        g2.drawLine(x1, y1 + HEIGHT_2, x1 + WIDTH, y1 + HEIGHT_2); // hor
        g2.drawLine(x1 + WIDTH_2, y1, x1 + WIDTH_2, y1 + HEIGHT); // ver
        g2.setPaint(Color.WHITE);
        g2.fillOval(x1 + WIDTH_4, y1 + HEIGHT_4, WIDTH_2, HEIGHT_2);
    }

}
