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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A least-recently-used LRU cache, based on {@link LinkedHashMap}. This cache can hold a fixed maximum number of
 * elements; if a new element is added, and the cache is full, the least recently used entry is removed.
 * 
 * @author Rudolf Mayer
 * @version $Id: LeastRecentlyUsedCache.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class LeastRecentlyUsedCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;

    private int cacheSize;

    /**
     * Creates a new least-recently-used cache.
     * 
     * @param size the maximum number of entries that will be kept in this cache.
     */
    public LeastRecentlyUsedCache(int size) {
        // need to invoke constructor with all arguments, as there is no way to otherwise set LinkedHashMap.accessOrder
        // to true
        super((int) Math.ceil(size / 0.75f) + 1, 0.75f, true);
        this.cacheSize = size;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > LeastRecentlyUsedCache.this.cacheSize;
    }

}
