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

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A least-recently-used LRU cache, based on {@link LinkedHashMap}. This cache can hold a fixed a number of
 * {@link BufferedImage} elements, until the specified memory limit is reached. If a new element is added, and the cache
 * is full, the least recently used entry is removed.<br/>
 * 
 * @author Rudolf Mayer
 * @version $Id: LeastRecentelyUsedImageCache.java 3601 2010-06-23 13:40:28Z mayer $
 */
public final class LeastRecentelyUsedImageCache extends LinkedHashMap<String, BufferedImage> {
    private static final long serialVersionUID = 1L;

    private long maxCacheSize;

    private String maxCacheSizeReadable;

    public LeastRecentelyUsedImageCache(long maxCacheSize) {
        // need to invoke constructor with all arguments
        // as there is no way to otherwise set LinkedHashMap.accessOrder to true
        super(16, 0.75f, true);
        this.maxCacheSize = maxCacheSize;
        maxCacheSizeReadable = StringUtils.readableBytes(maxCacheSize);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Initialised visualisation image cache with " + maxCacheSizeReadable);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
        long totalSize = 0;
        Collection<BufferedImage> values = values();
        for (BufferedImage bufferedImage : values) {
            long imageSize = bufferedImage.getColorModel().getPixelSize() * bufferedImage.getHeight()
                    * bufferedImage.getWidth();
            totalSize += imageSize;
            if (totalSize > maxCacheSize) {
                break;
            }
        }
        if (totalSize > maxCacheSize) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Occupied cache size of " + StringUtils.readableBytes(totalSize) + " exceeds max cache size of "
                            + maxCacheSizeReadable + " - removing eldest entry.");
            return true;
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").fine(
                    "Current cache size of " + StringUtils.readableBytes(totalSize) + " is "
                            + (int) (totalSize / (double) maxCacheSize * 100) + "% of max cache ("
                            + maxCacheSizeReadable + ")");
            return false;
        }
    }
}