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
package at.tuwien.ifs.somtoolbox.util.mnemonic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;

/**
 * @author Rudolf Mayer
 * @version $Id: MapPanel.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class MapPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public static int BORDER = 10;

    public static int initialWidth = 800;

    public static int initialHeight = 600;

    protected EventListenerList listenerList = new EventListenerList();

    private DragListener dragListener = new DragListener();

    private BufferedImage backgroundImageMap;

    private boolean[][] toDraw;

    private double aspectRatio = 1.9455252918287937;

    private double nodeDiameter = 1;

    private double nodeSpacingX;

    private double nodeSpacingY;

    private double zoomAspect;

    private int zoomedHeight;

    private int zoomedWidth;

    public MapPanel(boolean[][] toDraw, String image) {
        this.toDraw = toDraw;
        jbInit();

        setSize(initialWidth, initialHeight);
        setRequestFocusEnabled(false);
        if (image != null) {
            try {
                backgroundImageMap = ImageIO.read(new File(image));
                aspectRatio = (double) backgroundImageMap.getWidth() / (double) backgroundImageMap.getHeight();
                double maxWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 40;
                double maxHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 200;
                double aspectX = maxWidth / backgroundImageMap.getWidth();
                double aspectY = maxHeight / backgroundImageMap.getHeight();
                zoomAspect = Math.min(aspectX, aspectY);
                zoomedHeight = (int) (backgroundImageMap.getHeight() * zoomAspect);
                zoomedWidth = (int) (backgroundImageMap.getWidth() * zoomAspect);
                setSize(getPreferredSize());
                System.out.println("set background map: " + zoomedWidth + "/" + zoomedHeight + ", max zoom: "
                        + zoomAspect);
            } catch (IOException e) {
                System.out.println("could not read image '" + image + "': " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            aspectRatio = toDraw.length / toDraw[0].length;
        }
        if (toDraw != null) {
            setNodes();
        }
    }

    public MapPanel(boolean[][] toDraw) {
        this.toDraw = toDraw;
        jbInit();
        setSize(initialWidth, initialHeight);
        setRequestFocusEnabled(false);
        aspectRatio = toDraw.length / toDraw[0].length;
        setNodes();
    }

    public MapPanel(int totalNodes, String image) {
        this(null, image);
        createNodes(totalNodes);
    }

    public void createNodes(int totalNodes) {
        int x = (int) Math.round(Math.sqrt(totalNodes * aspectRatio));
        int y = (int) Math.round(x / aspectRatio);
        toDraw = new boolean[x][y];
        System.out.println("toDraw: " + toDraw.length + " * " + toDraw[0].length);
        setNodes();
    }

    /**
     * 
     */
    private void setNodes() {
        double availableWidth = getSize().getWidth() - 2 * BORDER;
        double availableHeight = getSize().getHeight() - 2 * BORDER;

        double nodeWidth = availableWidth / (2 * toDraw.length - 1);
        double nodeHeight = availableHeight / (2 * toDraw[0].length - 1);
        nodeDiameter = (int) Math.min(nodeWidth, nodeHeight);
        nodeSpacingX = (availableWidth - nodeDiameter * toDraw.length) / toDraw.length;
        nodeSpacingY = (availableHeight - nodeDiameter * toDraw[0].length) / toDraw[0].length;
        System.out.println("nodeDiameter: " + nodeDiameter + " spacing x: " + nodeSpacingX + " y: " + nodeSpacingY);

        for (int col = 0; col < toDraw.length; col++) {
            for (int row = 0; row < toDraw[0].length; row++) {
                int xPos = getXPos(col);
                int yPos = getYPos(row);
                // System.out.println("RGB at " + col + "/" + row + ":" + backgroundImageMap.getRGB(xPos, yPos));
                if (backgroundImageMap == null
                        || backgroundImageMap.getRGB((int) (xPos / zoomAspect), (int) (yPos / zoomAspect)) < -1) {
                    toDraw[col][row] = true;
                }
                // if (backgroundImageMap.getRGB(xPos, yPos) != -1) {
                // System.out.println("blue value:" +
                // backgroundImageMap.getColorModel().getBlue(backgroundImageMap.getRGB(xPos, yPos)));
                // System.out.println("red value:" +
                // backgroundImageMap.getColorModel().getRed(backgroundImageMap.getRGB(xPos, yPos)));
                // System.out.println("green value:" +
                // backgroundImageMap.getColorModel().getGreen(backgroundImageMap.getRGB(xPos, yPos)));
                // }
            }
        }

    }

    /**
     * @param row the row
     * @return the vertical position on the screen
     */
    private int getYPos(int row) {
        return (int) (BORDER + row * (nodeDiameter + nodeSpacingY));
    }

    /**
     * @param col the column
     * @return the horizontal position on the screen
     */
    private int getXPos(int col) {
        return (int) (BORDER + col * (nodeDiameter + nodeSpacingX));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        setBackground(Color.WHITE);
        setForeground(Color.GRAY);
        if (backgroundImageMap != null) {
            g.drawImage(backgroundImageMap, BORDER, BORDER, zoomedWidth, zoomedHeight, this);
        }

        // System.out.println("Painting..." + toDraw.length + " / " + toDraw[0].length + " diameter: " + nodeDiameter);
        for (int col = 0; col < toDraw.length; col++) {
            for (int row = 0; row < toDraw[0].length; row++) {
                int xPos = getXPos(col);
                int yPos = getYPos(row);

                if (toDraw[col][row]) {
                    setForeground(Color.GRAY);
                    g.fillRoundRect(xPos, yPos, (int) nodeDiameter - 1, (int) nodeDiameter - 1, 2 * BORDER, 2 * BORDER);

                } else {
                    g.drawRoundRect(xPos, yPos, (int) nodeDiameter - 1, (int) nodeDiameter - 1, 2 * BORDER, 2 * BORDER);
                }
            }
        }

        g.drawRect((int) dragListener.startX, (int) dragListener.startY,
                (int) (dragListener.endX - dragListener.startX), (int) (dragListener.endY - dragListener.startY));
    }

    private void jbInit() {
        registerListeners();
    }

    private void registerListeners() {
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                processMouseClick(e.getX(), e.getY());
            }
        });

        this.addMouseListener(dragListener);
        this.addMouseMotionListener(dragListener);
    }

    public void processMouseClick(double x, double y) {
        // System.out.println("in processMouseClick " + x + "/" + y);
        double translatedX = x - BORDER;
        double translatedY = y - BORDER;

        double actualX = translatedX % (nodeDiameter + nodeSpacingX);
        double actualY = translatedY % (nodeDiameter + nodeSpacingY);
        // System.out.println("original: " + x + "/" + y + " ==> " + actualX + "/" + actualY);

        if (actualX < nodeDiameter && actualY < nodeDiameter) {
            System.out.println("IN!");

            int nodeX = (int) (translatedX / (nodeDiameter + nodeSpacingX));
            int nodeY = (int) (translatedY / (nodeDiameter + nodeSpacingY));
            invertNode(nodeX, nodeY);

            fireChangeEvent();
        } else {
            System.out.println("OUT!");
        }
    }

    /**
     * @param nodeX x-pos of the node to invert
     * @param nodeY y-pos of the node to invert
     */
    private void invertNode(final int nodeX, final int nodeY) {
        System.out.print("inverting node " + nodeX + "/" + nodeY);
        toDraw[nodeX][nodeY] = !toDraw[nodeX][nodeY];
        System.out.println(" --> " + toDraw[nodeX][nodeY]);
        repaint();
    }

    // Notify all listeners that have registered interest for notification on this event type. The event instance is
    // lazily created using the
    // parameters passed into the fire method.
    protected void fireChangeEvent() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                // Lazily create the event:
                ((ChangeListener) listeners[i + 1]).stateChanged(new ChangeEvent(this));
            }
        }
    }

    public void processAreaSelection(double startX, double startY, double endX, double endY) {
        int startCol = (int) Math.ceil((startX - BORDER) / (2 * nodeDiameter));
        int endCol = (int) Math.floor((endX - BORDER) / (2 * nodeDiameter));
        int startRow = (int) Math.ceil((startY - BORDER) / (2 * nodeDiameter));
        int endRow = (int) Math.floor((endY - BORDER) / (2 * nodeDiameter));

        System.out.println("processAreaSelection: " + startCol + "/" + startRow + " - " + endCol + "/" + endRow);

        for (int col = startCol; col <= endCol; col++) {
            for (int row = startRow; row <= endRow; row++) {
                invertNode(col, row);
            }
        }
        fireChangeEvent();
    }

    public File saveScreenToImage(File file) {
        try {
            BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = bi.createGraphics();
            paint(g);
            System.out.println("filename: " + file.getName());
            if (file.getName().indexOf('.') == -1) { // no extension provided
                file = new File(file.getAbsoluteFile() + ".jpg");
                System.out.println("new file  ==> " + file.getAbsoluteFile());
            }
            String imageType = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            ImageIO.write(bi, imageType, file);
            return file;
        } catch (java.io.IOException ioEx) {
            System.out.println("Error saving sreen to file " + ioEx);
            ioEx.printStackTrace(System.err);
        }
        return null;
    }

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    class DragListener extends MouseInputAdapter {
        double endX;

        double endY;

        double startX;

        double startY;

        @Override
        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            endX = e.getX();
            endY = e.getY();
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            endX = e.getX();
            endY = e.getY();
            processAreaSelection(Math.min(startX, endX), Math.min(startY, endY), Math.max(startX, endX), Math.max(
                    startY, endY));
            startX = startY = endX = endY = 0;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (backgroundImageMap != null) {
            System.out.println("preferred size: " + new Dimension(zoomedWidth + 2 * BORDER, zoomedHeight + 2 * BORDER));
            return new Dimension(zoomedWidth + 2 * BORDER, zoomedHeight + 2 * BORDER);
        } else {
            return new Dimension(100, 100);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public boolean[][] getToDraw() {
        return toDraw;
    }

    public int getNodeCount() {
        return getToDraw().length * getToDraw()[0].length;
    }
}