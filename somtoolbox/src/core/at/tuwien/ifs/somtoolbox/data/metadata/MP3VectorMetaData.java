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
package at.tuwien.ifs.somtoolbox.data.metadata;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.v1.ID3V1Tag;
import org.blinkenlights.jid3.v2.ID3V2Tag;

/**
 * @author Jakob Frank
 * @version $Id: MP3VectorMetaData.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class MP3VectorMetaData extends AudioVectorMetaData {

    private static String displayFormat = "$title - $artist";

    private static String emptyLabel = displayFormat.replaceAll("\\$[a-z]+", "");

    private String cacheTitle = null, cacheAlbum = null, cacheArtist = null;

    private final MP3File mp3File;

    public MP3VectorMetaData(String filename) throws FileNotFoundException {
        this(checkFileExistence(filename), filename);
    }

    public MP3VectorMetaData(File file, String id) throws FileNotFoundException {
        super(file, id);
        mp3File = new MP3File(file);

        createSearchCache();
    }

    public MP3VectorMetaData(File file) throws FileNotFoundException {
        this(file, file.getPath());
    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.data.vectorlabel.AbstractVectorLabel#getDisplayLabel()
     */
    @Override
    public String getDisplayLabel() {
        if (getAudioFile().canRead()) {
            String label = displayFormat;
            label = label.replaceAll("\\$artist", getArtist());
            label = label.replaceAll("\\$title", getTitle());
            label = label.replaceAll("\\$album", getAlbum());
            label = label.replaceAll("\\$id", getID());

            if (label.equals(emptyLabel)) {
                return getID();
            } else {
                return label;
            }
        } else {
            return super.getDisplayLabel();
        }
    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.data.vectorlabel.AbstractVectorLabel#matches(java.util.regex.Pattern)
     */
    @Override
    public boolean matches(Pattern pattern) {
        Matcher mTitle = pattern.matcher(getTitle());
        Matcher mArtist = pattern.matcher(getArtist());
        Matcher mAlbum = pattern.matcher(getAlbum());
        Matcher mId = pattern.matcher(getID());

        return mId.find() || mTitle.find() || mArtist.find() || mAlbum.find();
    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.data.vectorlabel.AbstractVectorLabel#matches(java.lang.String, boolean)
     */
    @Override
    public boolean matches(String pattern, boolean ignoreCase) {
        if (ignoreCase) {
            pattern = pattern.toLowerCase();
        }
        String[] qs = pattern.split("\\s+");
        String lTi = ignoreCase ? getTitle().toLowerCase() : getTitle();
        String lAl = ignoreCase ? getAlbum().toLowerCase() : getAlbum();
        String lAr = ignoreCase ? getArtist().toLowerCase() : getArtist();
        String lId = ignoreCase ? getID().toLowerCase() : getID();
        for (String q : qs) {
            if (!(lId.contains(q) || lTi.contains(q) || lAl.contains(q) || lAr.contains(q))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Define the Format of the DisplayLabel.
     * <p>
     * The following macros are valid and can be used:
     * <ul>
     * <li><code>$artist</code> is replaced by the artists name</li>
     * <li><code>$title</code> is replaced by the track title</li>
     * <li><code>$album</code> is replaced by the albums name</li>
     * </ul>
     * <p>
     * The default format is <code>$title - $artist</code>.
     * 
     * @param format the Format.
     */
    public static void setDisplayLabelFormat(String format) {
        displayFormat = format;
    }

    public static void main(String[] args) {
        System.out.printf("Testing%n");
        try {
            AudioVectorMetaData l = new MP3VectorMetaData(
                    "/mnt/colossus/last.fm/2007/Wir sind Helden/Endlich Ein Grund Zur Panik/Popstar.mp3");
            AudioVectorMetaData l2 = new MP3VectorMetaData(
                    "/mnt/colossus/last.fm/2007/LaFee/Jetzt Erst Recht/Weg Von Dir.mp3");
            System.out.printf("l1: %s%n", l.getDisplayLabel());
            System.out.printf("l2: %s%n", l2.getDisplayLabel());
            // MP3VectorLabel.setDisplayLabelFormat("$artist - $album - $title");
            System.out.printf("%s%n%n", l.getDisplayLabel());

            LinkedList<MP3VectorMetaData> lls = new LinkedList<MP3VectorMetaData>();
            File baseDir = new File("/mnt/storage/music/frank-private/T");
            System.out.printf("Creating filelist from %s...%n", baseDir.getPath());
            long start = System.currentTimeMillis();
            fillListWithFiles(lls, baseDir);
            long dur = System.currentTimeMillis() - start;
            System.out.printf("Filelist with %d files in %.2f sec.%n", lls.size(), dur / 1000d);

            System.out.printf("%n");

            String q = "irl y";
            System.out.printf("Search for \"%s\" in %d labels.%n", q, lls.size());
            int found = 0;
            start = System.currentTimeMillis();
            for (AudioVectorMetaData label : lls) {
                if (label.matches(q)) {
                    System.out.printf("  %s%n", label.getDisplayLabel());
                    found++;
                }
            }
            dur = System.currentTimeMillis() - start;
            System.out.printf("Found %d in %.2f sec.%n", found, dur / 1000d);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.printf("Done%n");
    }

    private static void fillListWithFiles(LinkedList<MP3VectorMetaData> lls, File baseDir) throws FileNotFoundException {
        if (!baseDir.exists()) {
            return;
        }

        if (baseDir.isDirectory()) {
            File[] fs = baseDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        return true;
                    }
                    if (pathname.getName().endsWith(".mp3")) {
                        return true;
                    }
                    return false;
                }
            });
            for (File file : fs) {
                if (file.isDirectory()) {
                    fillListWithFiles(lls, file);
                } else {
                    MP3VectorMetaData l = new MP3VectorMetaData(file);
                    l.createSearchCache();
                    lls.add(l);
                }
            }
        }
    }

    private String getArtist() {
        if (cacheArtist == null) {
            try {
                ID3V2Tag v2 = mp3File.getID3V2Tag();
                if (v2 != null) {
                    cacheArtist = v2.getArtist();
                }
                ID3V1Tag v1 = mp3File.getID3V1Tag();
                if (v1 != null) {
                    cacheArtist = v1.getArtist();
                }
            } catch (ID3Exception e) {
            }
            if (cacheArtist == null) {
                return "";
            }
        }
        return cacheArtist;
    }

    private String getTitle() {
        if (cacheTitle == null) {
            try {
                ID3V2Tag v2 = mp3File.getID3V2Tag();
                if (v2 != null) {
                    cacheTitle = v2.getTitle();
                }
                ID3V1Tag v1 = mp3File.getID3V1Tag();
                if (v1 != null) {
                    cacheTitle = v1.getTitle();
                }
            } catch (ID3Exception e) {
            }
            if (cacheTitle == null) {
                return "";
            }
        }
        return cacheTitle;
    }

    private String getAlbum() {
        if (cacheAlbum == null) {
            try {
                ID3V2Tag v2 = mp3File.getID3V2Tag();
                if (v2 != null) {
                    cacheAlbum = v2.getAlbum();
                }
                ID3V1Tag v1 = mp3File.getID3V1Tag();
                if (v1 != null) {
                    cacheAlbum = v1.getAlbum();
                }
            } catch (ID3Exception e) {
            }
            if (cacheAlbum == null) {
                return "";
            }
        }
        return cacheAlbum;
    }

    private void createSearchCache() {
        getTitle();
        getAlbum();
        getArtist();
    }

}
