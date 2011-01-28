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
package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;

/**
 * Handles zooming by the mouse wheel.
 * 
 * @author Robert Neumayer
 * @version $Id: MyWheelZoomEventHandler.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class MyWheelZoomEventHandler extends PBasicInputEventHandler {
    // private Point2D wheelEventCanvasPoint = null;

    private static double minScale = 0;

    private static double maxScale = Double.MAX_VALUE;

    public MyWheelZoomEventHandler() {
        super();
    }

    @Override
    public void mouseWheelRotated(PInputEvent e) {
        processZoomEvent(e, e.getWheelRotation(), e.getCamera());
    }

    /**
     * This method actually handles the zoom event; the method is also used by the key event handler in {@link SOMPane}.
     */
    public static void processZoomEvent(PInputEvent e, int zoomDirection, PCamera camera) {
        double scaleDelta = 1 - Math.signum(zoomDirection) * 0.08;

        double currentScale = camera.getViewScale();
        double newScale = currentScale * scaleDelta;

        if (newScale < minScale) {
            scaleDelta = minScale / currentScale;
        }
        if (maxScale > 0 && newScale > maxScale) {
            scaleDelta = maxScale / currentScale;
        }
        Point2D mousePosition = e.getCanvasPosition();
        Point2D localMousePosition = camera.localToView(mousePosition);
        camera.scaleViewAboutPoint(scaleDelta, localMousePosition.getX(), localMousePosition.getY());
    }
}
