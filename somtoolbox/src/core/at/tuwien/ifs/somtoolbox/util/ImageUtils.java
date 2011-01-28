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
package at.tuwien.ifs.somtoolbox.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * This class provides methods to manipulate images.
 * 
 * @author Rudolf Mayer
 * @version $Id: ImageUtils.java 3608 2010-06-25 08:46:18Z mayer $
 */
public class ImageUtils {

    enum Origin {
        TOP, RIGHT, LEFT, BOTTOM;
    }

    /**
     * Automatically crops an image, by continously removing full rows or columns from all sides, as long as all the
     * pixels in them are all white
     */
    public static BufferedImage autoCrop(BufferedImage bi) {
        // find the crop points from all sides
        int cropFromTop = findNumberOfRowsToCrop(bi, Origin.TOP);
        int cropFromBottom = findNumberOfRowsToCrop(bi, Origin.BOTTOM);
        int cropFromLeft = findNumberOfColumnsToCrop(bi, Origin.LEFT);
        int cropFromRight = findNumberOfColumnsToCrop(bi, Origin.RIGHT);

        int width = bi.getWidth() - cropFromLeft - cropFromRight;
        int height = bi.getHeight() - cropFromBottom - cropFromTop;
        // System.out.println(width);
        return bi.getSubimage(cropFromLeft, cropFromTop, width, height);
    }

    /**
     * Computes how many rows of pixels can be cropped from the specified {@link Origin}; only {@link Origin#TOP} and
     * {@link Origin#BOTTOM} are valid
     */
    private static int findNumberOfRowsToCrop(BufferedImage bi, Origin origin) {
        if (origin != Origin.TOP && origin != Origin.BOTTOM) {
            throw new IllegalArgumentException("Direction can onl be '" + Origin.TOP + "' (downwards) or '"
                    + Origin.BOTTOM + "' (upwards)");
        }
        int rows = 0;
        while (rows >= 0 && rows < bi.getHeight()) {
            for (int i = 0; i < bi.getWidth(); i++) {
                Color color = new Color(bi.getRGB(i, origin == Origin.TOP ? rows : bi.getHeight() - rows - 1));
                if (!color.equals(Color.WHITE)) {
                    return rows;
                }
            }
            rows++;
        }
        return rows;
    }

    /**
     * Computes how many cols of pixels can be cropped from the specified {@link Origin}; only {@link Origin#LEFT} and
     * {@link Origin#RIGHT} are valid
     */
    private static int findNumberOfColumnsToCrop(BufferedImage bi, Origin origin) {
        if (origin != Origin.RIGHT && origin != Origin.LEFT) {
            throw new IllegalArgumentException("Direction can onl be '" + Origin.LEFT + "' (rightwards) or '"
                    + Origin.RIGHT + "' (leftwards)");
        }
        int cols = 0;
        while (cols >= 0 && cols < bi.getWidth()) {
            for (int i = 0; i < bi.getHeight(); i++) {
                Color color = new Color(bi.getRGB(origin == Origin.LEFT ? cols : bi.getWidth() - cols - 1, i));
                if (!color.equals(Color.WHITE)) {
                    return cols;
                }
            }
            cols++;
        }
        return cols;
    }

    /** Compares if two images contain the same pixel content */
    public static boolean equalPixelContent(BufferedImage i1, BufferedImage i2) {
        // simple check first: compare the dimension of the images
        if (i1.getWidth() != i2.getWidth() || i1.getHeight() != i2.getHeight()) {
            return false;
        }
        // now do a pixel comparison
        for (int i = 0; i < i1.getWidth(null); i++) {
            for (int j = 0; j < i2.getHeight(null); j++) {
                if (i1.getRGB(i, j) != i2.getRGB(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static BufferedImage scaleImage(BufferedImage buim, int width) {
        int height = (int) (width / ((double) buim.getWidth() / (double) buim.getHeight()));
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        scaledImage.createGraphics().drawImage(buim.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        return scaledImage;
    }

    public static BufferedImage scaleImageByHeight(BufferedImage buim, int height) {
        int width = (int) (height / ((double) buim.getHeight() / (double) buim.getWidth()));
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        scaledImage.createGraphics().drawImage(buim.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        return scaledImage;
    }

    /** Creates a {@link BufferedImage} with a white, empty background. */
    public static BufferedImage createEmptyImage(int width, int height) {
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();
        Color c = g.getBackground();
        g.setBackground(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setBackground(c);
        return res;
    }

    /** Creates a {@link BufferedImage} with the given colour as background */
    public static BufferedImage createImage(int width, int height, int bkcolor) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, bkcolor);
            }
        }

        return image;
    }
}
