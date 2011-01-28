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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.martiansoftware.jsap.JSAPResult;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;

/**
 * This class bundles file-related utilities.
 * 
 * @author Rudolf Mayer
 * @author Doris Baum
 * @version $Id: FileUtils.java 3916 2010-11-04 14:46:37Z mayer $
 */
public class FileUtils {

    private static final String COMMENT_INDICATOR = "#";

    public static class SOMDescriptionFileFilter extends FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            String[] allowedFileEndings = { SOMLibFormatInputReader.unitFileNameSuffix,
                    SOMLibFormatInputReader.weightFileNameSuffix, SOMLibFormatInputReader.mapFileNameSuffix };

            String fileName = file.getName();
            if (fileName != null) {
                for (String allowedFileEnding : allowedFileEndings) {
                    if (fileName.endsWith(allowedFileEnding) || fileName.endsWith(allowedFileEnding + ".gz")) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "SOM description files";
        }
    }

    /**
     * Opens a file specified by argument <code>fileName</code> and returns a <code>BufferedReader</code>. This method
     * opens both, uncompressed and gzipped files transparent to the calling function. If the specified file is not
     * found, the suffix .gz is appended. If this name is again not found, a <code>FileNotFoundException</code> is
     * thrown.
     * 
     * @param fileType the type of the file to open.
     * @param fileName the name of the file to open.
     * @return a <code>BufferedReader</code> to the requested file.
     * @throws FileNotFoundException if the file with the given name is not found.
     */
    public static BufferedReader openFile(String fileType, String fileName) throws FileNotFoundException {
        return openFile(fileType, new File(fileName));
    }

    /**
     * Opens a file specified by argument <code>file</code> and returns a <code>BufferedReader</code>. This method opens
     * both, uncompressed and gzipped files transparent to the calling function. If the specified file is not found, the
     * suffix .gz is appended. If this name is again not found, a <code>FileNotFoundException</code> is thrown.
     * 
     * @param fileType the type of the file to open.
     * @param file the file to open.
     * @return a <code>BufferedReader</code> to the requested file.
     * @throws FileNotFoundException if the file is not found.
     */
    public static BufferedReader openFile(String fileType, File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(getInputStream(fileType, file)));
    }

    public static InputStream getInputStream(String fileType, String fileName) throws FileNotFoundException {
        return getInputStream(fileType, new File(fileName));
    }

    public static InputStream getInputStream(String fileType, File file) throws FileNotFoundException {
        InputStream is;
        File gzFile = new File(file.getParentFile(), file.getName() + ".gz");

        if (file == null || !file.exists()) { // we don't find a file with the original file name
            if (gzFile.exists()) { // we check if a '.gz' file exists
                file = gzFile; // if yes, we use this file name
            } else {
                throw new FileNotFoundException("File " + file + " or " + gzFile + " not found (trying file "
                        + file.getAbsolutePath() + ".");
            }
        }

        try {
            is = new GZIPInputStream(new FileInputStream(file));
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(file + " is gzip compressed. Trying compressed read.");
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(fileType + " " + file + " not found.");
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    file + " is not gzip compressed. Trying uncompressed read.");
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e2) {
                throw new FileNotFoundException("File " + " " + file + " not found.");
            }
        }
        return is;
    }

    public static PrintWriter openFileForWriting(String fileType, String fileName) throws IOException {
        return openFileForWriting(fileType, fileName, false);
    }

    public static PrintWriter openFileForWriting(String fileType, String fileName, boolean gzipped) throws IOException {
        if (gzipped) {
            return new PrintWriter(new GZIPOutputStream(new FileOutputStream(fileName.endsWith(".gz") ? fileName
                    : fileName + ".gz")));
        } else {
            return new PrintWriter(new FileWriter(fileName));
        }
    }

    /**
     * Extracts the prefix from a SOM description filename so that the corresponding other two description files can be
     * found
     */
    public static String extractSOMLibInputPrefix(String filename) {
        String prefix = new String(filename);
        int index = prefix.indexOf(".gz");
        if (index != -1) {
            prefix = prefix.substring(0, index);
        }
        if (prefix.endsWith(".unit") || prefix.endsWith(".wgt") || prefix.endsWith(".map")) {
            prefix = prefix.substring(0, prefix.lastIndexOf("."));
        }
        return prefix;
    }

    public static String extractSOMLibDataPrefix(String filename) {
        String prefix = new String(filename);
        int index = prefix.indexOf(".gz");
        if (index != -1) {
            prefix = prefix.substring(0, index);
        }
        if (prefix.endsWith(".tv") || prefix.endsWith(".vec")) {
            prefix = prefix.substring(0, prefix.lastIndexOf("."));
        }
        return prefix;
    }

    /**
     * Reads the headers of a SOMLib File, and stores the values in a map.
     */
    public static HashMap<String, String> readSOMLibFileHeaders(BufferedReader br, String fileType) throws IOException {
        HashMap<String, String> map = new HashMap<String, String>();
        String line = null;
        int lineNumber = 0;
        while ((line = br.readLine()) != null) {
            lineNumber++;
            line = line.trim();
            if (line.startsWith(COMMENT_INDICATOR)) { // ignore comment lines
                Logger.getLogger("at.tuwien.ifs.somtoolbox").finest("Read comment '" + line + "'.");
            } else if (line.startsWith("$")) { // 
                StringTokenizer tokenizer = new StringTokenizer(line, " \t");
                String key = tokenizer.nextToken();
                String value = null;
                if (tokenizer.hasMoreElements()) {
                    value = tokenizer.nextToken("").trim();
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Header in " + fileType + " file corrupt!");
                    throw new IOException("Header in " + fileType + " file corrupt!");
                }
                map.put(key, value);
            } else if (line.length() > 0) { // we reached a content line, stop reading
                map.put("FIRST_CONTENT_LINE", line);
                map.put("LINE_NUMBER", String.valueOf(lineNumber));
                return map;
            }
        }
        return map;
    }

    public static String[] findAllSOMLibFiles(JSAPResult config, final String optNameInputs,
            final String optNameInputDir, final String extensionToFind, String extensionToCheck) {
        String[] inputs = config.getStringArray(optNameInputs);
        String inputDirectory = config.getString(optNameInputDir);

        if ((inputs == null || inputs.length == 0) && inputDirectory == null || inputs != null && inputs.length > 0
                && inputDirectory != null) {
            System.out.println("You need to specify exactly one out of '" + optNameInputs + "' or '" + optNameInputDir
                    + "'");
            System.exit(-1);
        }

        if (inputDirectory != null) {
            File dir = new File(inputDirectory);
            System.out.println("Checking for input files in " + dir.getAbsolutePath());
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(extensionToFind) || name.endsWith(extensionToFind + ".gz");
                }
            });
            Arrays.sort(files);
            ArrayList<String> validInputs = new ArrayList<String>();
            for (File file : files) {
                String baseFileName = StringUtils.stripSuffix(file.getAbsolutePath());
                if (extensionToCheck != null && new File(baseFileName + extensionToCheck).exists()
                        || new File(baseFileName + extensionToCheck + ".gz").exists()) {
                    validInputs.add(baseFileName);
                    System.out.println("Adding input " + baseFileName);
                } else {
                    System.out.println("Found template vector file '" + file.getAbsolutePath() + "', but no fitting '"
                            + extensionToCheck + "' or '" + extensionToCheck + ".gz' file as '"
                            + new File(baseFileName + extensionToCheck).getAbsolutePath() + "', skipping!");
                }
            }
            inputs = validInputs.toArray(new String[validInputs.size()]);
        } else {
            // we should validate that we also have the matching other type of files present, i.e. all .vec files for
            // all .tv files, etc..
        }
        return inputs;
    }

    public static String stripPathPrefix(final String fileName) {
        if (fileName.contains(File.separator)) {
            return fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        } else {
            return fileName;
        }
    }

    public static void copyFile(String source, String destination) throws FileNotFoundException, IOException,
            SOMToolboxException {
        // some checks on whether we can write..
        if (!new File(source).canRead()) {
            throw new SOMToolboxException("Can't read from source file '" + source + "'. Not copying file.");
        }
        new File(destination).createNewFile();
        if (!new File(destination).canWrite()) {
            throw new SOMToolboxException("Can't write to destination file '" + destination + "'. Not copying file.");
        }
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.flush();
        out.close();

    }

    public static void copyFileSafe(File destinationFileName, File sourceFileName) {
        try {
            FileInputStream in = new FileInputStream(sourceFileName);
            FileOutputStream out = new FileOutputStream(destinationFileName);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void copyFileSafe(String destinationFileName, String sourceFileName) {
        copyFileSafe(new File(destinationFileName), new File(sourceFileName));
    }

    public static void copyResource(String destinationDirectory, String sourceDirectory, String fileName) {
        copyFileSafe(destinationDirectory + File.separator + fileName, ExportUtils.class.getResource(
                sourceDirectory + File.separator + fileName).getFile());
    }

    public static boolean fileStartsWith(String fileName, String match) throws FileNotFoundException, IOException {
        CharBuffer cbuf = CharBuffer.allocate(match.length());
        BufferedReader reader = openFile("", fileName);
        reader.read(cbuf);
        reader.close();
        return cbuf.toString().equals(match);
    }

    public static String[] readLines(String filename) throws IOException {
        ArrayList<String> lines = readLinesAsList(filename);
        return lines.toArray(new String[lines.size()]);
    }

    public static ArrayList<String> readLinesAsList(String filename) throws FileNotFoundException, IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        ArrayList<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }

    /** Reads the complete contents of the file denoted by the given filename. uses {@link #readFileContents(File)}. */
    public static String readFileContents(String filename) throws FileNotFoundException, IOException {
        return readFileContents(new File(filename));
    }

    /** Reads the complete contents of the given file. */
    public static String readFileContents(final File file) throws IOException {
        try {
            Scanner scanner = new Scanner(file).useDelimiter("\\Z");
            String contents = scanner.next();
            scanner.close();
            return contents;
        } catch (NoSuchElementException e) {
            // some files are not properly terminated, read byte by byte..
            FileReader reader = new FileReader(file);
            char[] cbuf = new char[1];
            StringBuilder contents = new StringBuilder();
            while (reader.read(cbuf) != -1) {
                contents.append(cbuf);
            }
            return contents.toString();
        }
    }

    public static String readFromFile(String resourcePath, String fileName) throws FileNotFoundException, IOException {
        return FileUtils.readFromFile(ExportUtils.class.getResource(resourcePath + fileName).getFile());
    }

    public static String readFromFile(String fileName) throws FileNotFoundException, IOException {
        String line;
        String content = "";
        line = null;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while ((line = reader.readLine()) != null) {
            content += line + "\n";
        }
        return content;
    }

    public static String prepareOutputDir(String dir) {
        if (org.apache.commons.lang.StringUtils.isNotEmpty(dir) && !dir.endsWith(File.separator)) {
            return dir + File.separator;
        } else {
            return dir;
        }
    }

    public static void clearOutputDir(String outputDir) {
        File outDir = new File(outputDir);
        if (outDir.exists() && outDir.isDirectory()) {
            File[] content = outDir.listFiles();
            for (File element : content) {
                element.delete();
            }
        }
        outDir.mkdir();
    }

    public static void writeToFile(String content, String pathname) throws IOException {
        File file = new File(pathname);
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        output.write(content);
        output.close();
    }

    public static String getPathFrom(String unitDescriptionFileName) {
        try {
            return new File(unitDescriptionFileName).getParentFile().getAbsolutePath();
        } catch (Exception e) {
            if (unitDescriptionFileName.contains("/")) {
                return unitDescriptionFileName.substring(0, unitDescriptionFileName.lastIndexOf("/") - 1);
            } else if (unitDescriptionFileName.contains("\\")) {
                return unitDescriptionFileName.substring(0, unitDescriptionFileName.lastIndexOf("\\") - 1);
            }
        }
        return "";
    }

    public static void saveImageToFile(String fileName, BufferedImage buim) throws SOMToolboxException {
        if (!fileName.endsWith(".png")) {
            fileName += ".png";
        }
        try {
            ImageIO.write(buim, "png", new File(fileName));
        } catch (FileNotFoundException e) {
            throw new SOMToolboxException(e.getMessage());
        } catch (IOException e) {
            throw new SOMToolboxException(e.getMessage());
        } catch (Exception e) {
            throw new SOMToolboxException(e.getMessage());
        }
    }

    public static void writeFile(String fileName, String data) throws FileNotFoundException, IOException {
        FileOutputStream fos;
        fos = new FileOutputStream(fileName);
        fos.write(data.getBytes());
        fos.flush();
        fos.close();
    }

    /** Checks whether the given String is a valid URL or not */
    public static boolean isURL(String potentialURL) {
        try {
            new URL(potentialURL);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    /**
     * Reads (and discards) all comment lines (indicated by {@value #COMMENT_INDICATOR}) at the beginning of a file.
     * 
     * @return the first non-comment line
     */
    public static String consumeHeaderComments(BufferedReader br) throws IOException {
        String line = null;
        do { // read header comments
            line = br.readLine();
        } while (line != null && line.startsWith(COMMENT_INDICATOR));
        return line;
    }

    /** returns the given path, if needed appended by a {@link File#separator} */
    public static String getPathPrefix(String fDir) {
        if (org.apache.commons.lang.StringUtils.isBlank(fDir)) {
            return "";
        } else {
            return fDir + (!fDir.endsWith(File.separator) ? File.separator : "");
        }
    }

    /** Computes the suffix (extension) of a file name, with or without .gz */
    public static String getSuffix(String suffix, boolean gzipped) {
        if (gzipped == true) {
            return "." + suffix + ".gz";
        } else {
            return "." + suffix;
        }
    }

    /** Finds all files matching any of the given extensions, in any subdirecotry of the given root path */
    public static LinkedList<File> getAllFilesInRoot(File root, String... extensions) {
        for (int i = 0; i < extensions.length; i++) {
            if (!extensions[i].startsWith(".")) {
                extensions[i] = "." + extensions[i];
            }
        }
        return getAllFilesInRoot(new LinkedList<File>(), root, new SuffixFileFilter(extensions));
    }

    private static LinkedList<File> getAllFilesInRoot(LinkedList<File> fileList, File root,
            java.io.FileFilter fileFilter) {
        File[] listFiles = root.listFiles(fileFilter);
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    getAllFilesInRoot(fileList, file, fileFilter);
                } else {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    public static void gunzip(String source) throws SOMToolboxException {
        if (source.endsWith(".gz")) {
            gunzip(source, source.substring(0, source.length() - 3));
        } else {
            throw new SOMToolboxException("File must end with .gz to automatically unzip it");
        }
    }

    public static void gunzip(String source, String destination) {
        FileOutputStream out = null;
        GZIPInputStream zIn = null;
        FileInputStream fis = null;
        try {
            out = new FileOutputStream(destination);
            fis = new FileInputStream(source);
            zIn = new GZIPInputStream(fis);
            byte[] buffer = new byte[8 * 1024];
            int count = 0;
            do {
                out.write(buffer, 0, count);
                count = zIn.read(buffer, 0, buffer.length);
            } while (count != -1);
        } catch (IOException ioe) {
            System.out.println("Problem expanding gzip " + ioe.getMessage());
        } finally {
            close(fis);
            close(out);
            close(zIn);
        }
    }

    public static void close(Closeable f) {
        if (f != null) {
            try {
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}