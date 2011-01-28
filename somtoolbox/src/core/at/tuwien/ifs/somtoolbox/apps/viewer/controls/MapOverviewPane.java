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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.P3DRect;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;

/**
 * Implements an overview of the {@link MapPNode}, indicating the currently displayed area of the map. Allows also to
 * move the currently displayed area.
 * 
 * @author Michael Dittenbach
 * @version $Id: MapOverviewPane.java 3873 2010-10-28 09:29:58Z frank $
 */
public class MapOverviewPane extends AbstractViewerControl {
    private static final long serialVersionUID = 1L;

    private final MapOverviewCanvas moc;

    public MapOverviewPane(String title, CommonSOMViewerStateData state) {
        super(title, state, new GridLayout(1, 1));
        moc = new MapOverviewCanvas();

        getContentPane().add(moc);
        moc.setPreferredSize(new Dimension(state.controlElementsWidth, 150));
        setVisible(true);
    }

    public void connect(PCanvas canvas, PLayer[] viewed_layers) {
        moc.connect(canvas, viewed_layers);
    }

    public class MapOverviewCanvas extends PCanvas implements PropertyChangeListener {
        private static final long serialVersionUID = 1L;

        /**
         * This is the node that shows the viewed area.
         */
        PNode areaVisiblePNode;

        /**
         * This is the canvas that is being viewed
         */
        PCanvas viewedCanvas;

        /**
         * The change listener to know when to update the birds eye view.
         */
        PropertyChangeListener changeListener;

        int layerCount;

        /**
         * Creates a new instance of a BirdsEyeView
         */
        public MapOverviewCanvas() {

            // create the PropertyChangeListener for listening to the viewed canvas
            changeListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateFromViewed();
                }
            };

            // create the coverage node
            areaVisiblePNode = new P3DRect();
            areaVisiblePNode.setPaint(new Color(128, 128, 255));
            areaVisiblePNode.setTransparency(.8f);
            areaVisiblePNode.setBounds(0, 0, 100, 100);
            getCamera().addChild(areaVisiblePNode);

            // add the drag event handler
            getCamera().addInputEventListener(new PDragSequenceEventHandler() {
                @Override
                protected void startDrag(PInputEvent e) {
                    if (e.getPickedNode() == areaVisiblePNode) {
                        super.startDrag(e);
                    }
                }

                @Override
                protected void drag(PInputEvent e) {
                    PDimension dim = e.getDelta();
                    viewedCanvas.getCamera().translateView(0 - dim.getWidth(), 0 - dim.getHeight());
                }

            });

            // remove Pan and Zoom
            removeInputEventListener(getPanEventHandler());
            removeInputEventListener(getZoomEventHandler());

            setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);

        }

        public void connect(PCanvas canvas, PLayer[] viewed_layers) {

            this.viewedCanvas = canvas;
            layerCount = 0;

            viewedCanvas.getCamera().addPropertyChangeListener(changeListener);

            for (layerCount = 0; layerCount < viewed_layers.length; ++layerCount) {
                getCamera().addLayer(layerCount, viewed_layers[layerCount]);
            }

        }

        /**
         * Add a layer to list of viewed layers
         */
        public void addLayer(PLayer new_layer) {
            getCamera().addLayer(new_layer);
            layerCount++;
        }

        /**
         * Remove the layer from the viewed layers
         */
        public void removeLayer(PLayer old_layer) {
            getCamera().removeLayer(old_layer);
            layerCount--;
        }

        /**
         * Stop the birds eye view from receiving events from the viewed canvas and remove all layers
         */
        public void disconnect() {
            viewedCanvas.getCamera().removePropertyChangeListener(changeListener);

            for (int i = 0; i < getCamera().getLayerCount(); ++i) {
                getCamera().removeLayer(i);
            }

        }

        /**
         * This method will get called when the viewed canvas changes
         */
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            updateFromViewed();
        }

        /**
         * This method gets the state of the viewed canvas and updates the BirdsEyeViewer This can be called from
         * outside code
         */
        public void updateFromViewed() {

            double viewedX;
            double viewedY;
            double viewedHeight;
            double viewedWidth;

            double ul_camera_x = viewedCanvas.getCamera().getViewBounds().getX();
            double ul_camera_y = viewedCanvas.getCamera().getViewBounds().getY();
            double lr_camera_x = ul_camera_x + viewedCanvas.getCamera().getViewBounds().getWidth();
            double lr_camera_y = ul_camera_y + viewedCanvas.getCamera().getViewBounds().getHeight();

            Rectangle2D drag_bounds = getCamera().getUnionOfLayerFullBounds();

            double ul_layer_x = drag_bounds.getX();
            double ul_layer_y = drag_bounds.getY();
            double lr_layer_x = drag_bounds.getX() + drag_bounds.getWidth();
            double lr_layer_y = drag_bounds.getY() + drag_bounds.getHeight();

            // find the upper left corner

            // set to the lesser value
            if (ul_camera_x < ul_layer_x) {
                viewedX = ul_layer_x;
            } else {
                viewedX = ul_camera_x;
            }

            // same for y
            if (ul_camera_y < ul_layer_y) {
                viewedY = ul_layer_y;
            } else {
                viewedY = ul_camera_y;
            }

            // find the lower right corner

            // set to the greater value
            if (lr_camera_x < lr_layer_x) {
                viewedWidth = lr_camera_x - viewedX;
            } else {
                viewedWidth = lr_layer_x - viewedX;
            }

            // same for height
            if (lr_camera_y < lr_layer_y) {
                viewedHeight = lr_camera_y - viewedY;
            } else {
                viewedHeight = lr_layer_y - viewedY;
            }

            Rectangle2D bounds = new Rectangle2D.Double(viewedX, viewedY, viewedWidth, viewedHeight);
            bounds = getCamera().viewToLocal(bounds);
            areaVisiblePNode.setBounds(bounds);

            // keep the birds eye view centered
            getCamera().animateViewToCenterBounds(drag_bounds, true, 0);

        }

    } // class BirdsEyeView

}