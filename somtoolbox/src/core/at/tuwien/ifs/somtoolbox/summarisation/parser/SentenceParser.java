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
package at.tuwien.ifs.somtoolbox.summarisation.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author Julius Penaranda
 * @version $Id: SentenceParser.java 3590 2010-05-21 10:43:45Z mayer $
 */
public class SentenceParser {
    private String prefix = null;

    public ArrayList<String>[] parsedDocuments = null;

    private int numDocs = 0;

    private ArrayList<String> filenames = new ArrayList<String>();

    private HeadlineTextParser headtextparser = null;

    private DocumentParser docparser = null;

    @SuppressWarnings("unchecked")
    public SentenceParser(Object[] itemNames) {
        this.parsedDocuments = new ArrayList[itemNames.length];
    }

    public void setFileNamePrefix(String fnprefix) {
        this.prefix = fnprefix;
    }

    public void find_parse_Document(String name) {
        try {
            File file = new File(prefix + "/" + java.net.URLDecoder.decode(name, "UTF-8"));
            filenames.add(name);

            // if (file.isDirectory()) {
            // File[] files = file.listFiles();
            // if (files != null) {
            // for (int i = 0; i < files.length; i++) {
            // if (files[i].getName().equals(name)) {
            if (file.exists()) {
                System.out.println("Document found: " + name);
                System.out.println("Parsing document: " + name);
                this.parsedDocuments[numDocs++] = parseDocument(file);
                System.out.println("sentences in document " + file + ": "
                        + (this.parsedDocuments[numDocs - 1].size() - 1));
                // System.out.println("parsedDocument: "+ parsedDocuments.length);
            } else {
                System.out.println("File not found: " + file.getAbsolutePath());
            }
            // }
            // }
            // else {
            // System.out.println("SentenceParser: Please specify data item path");
            // }
        } catch (IOException io) {
            System.err.println("an IO-Error occured");
        }
    }

    private ArrayList<String> parseDocument(File document) throws IOException {
        FileInputStream fstream = new FileInputStream(document);
        BufferedReader d = new BufferedReader(new InputStreamReader(fstream));
        ArrayList<String> result = new ArrayList<String>();

        String line = d.readLine();
        while (line.equals("")) { // ignore blank spaces
            line = d.readLine();
        }
        if (line.indexOf("<HEADLINE", 0) != -1) {
            if (this.headtextparser == null) {
                headtextparser = new HeadlineTextParser(this);
            }
            headtextparser.setReader(d);
            headtextparser.parse();
            result = headtextparser.getDocument();
        } else if (line.indexOf("Subject") != -1) {
            System.out.println("skipping header line: " + line);
        } else {
            if (this.docparser == null) {
                docparser = new DocumentParser(this);
            }
            docparser.setReader(d);
            docparser.newDoc();
            docparser.storeTitle(line);
            docparser.storeText();
            result = docparser.getDocument();
        }
        return result;
    }

    /** finds sentence within String and add it to parseddoc */
    public String findSentence(String line, ArrayList<String> parseddoc) {
        int offS = 0;
        int index = 0;
        boolean found = false;

        while (line.indexOf(".", offS) != -1 || line.indexOf("; ", offS) != -1 || line.indexOf("? ", offS) != -1
                || line.indexOf("! ", offS) != -1) {
            index = line.length();
            if (line.indexOf(".", offS) != -1) {
                if (index >= line.indexOf(".", offS)) {
                    // check what comes after '.'

                    // if ". " is at the end of line
                    if (line.indexOf(". ", offS) + 2 == line.length()) {
                        System.out.println("special case: eol");
                        index = line.indexOf(". ", offS);
                        found = true;
                    } else if (line.indexOf(".", offS) + 1 == line.length()) {
                        System.out.println("special case2: eol");
                        index = line.indexOf(".", offS);
                        found = true;
                    } else {
                        // check what comes after "."
                        char y = line.charAt(line.indexOf(".", offS) + 1);

                        // if y=='"'
                        if (y == 34) {
                            index = line.indexOf(".", offS) + 1;
                            System.out.println("case anfuehrungszeichen");
                            found = true;
                        }

                        // check what comes after ". "
                        if (!found && line.indexOf(". ", offS) != -1) {
                            char z = line.charAt(line.indexOf(". ", offS) + 2);

                            // if z is number, lower case, '-' or '('
                            if (z >= 49 && z <= 57 || z >= 97 && z <= 122 || z == 45 || z == 40) {
                                System.out.println("case lower, number");
                                offS = line.indexOf(". ", offS) + 2;
                            } else {
                                System.out.println("normal case");
                                index = line.indexOf(". ", offS);
                                found = true;

                            }
                        } else {
                            offS = line.indexOf(".", offS) + 1;
                        }
                    }
                }
            }
            if (line.indexOf("; ", offS) != -1) {
                System.out.println("; gefunden");
                if (index >= line.indexOf("; ", offS)) {
                    index = line.indexOf("; ", offS);
                }
                found = true;
            }
            if (line.indexOf("? ", offS) != -1) {
                if (index >= line.indexOf("? ", offS)) {
                    index = line.indexOf("? ", offS);
                }
                found = true;
            }
            if (line.indexOf("! ", offS) != -1) {
                if (index >= line.indexOf("! ", offS)) {
                    index = line.indexOf("! ", offS);
                }
                found = true;
            }
            if (found) {
                // System.out.println("index: "+index+" ll: "+line.length());
                if (line.substring(0, index + 1).indexOf("&UR") != -1) {
                    System.out.println("&UR wird ignoriert");
                } else {
                    parseddoc.add(line.substring(0, index + 1));
                }
                // System.out.println("Sentence geaddet: "+line.substring(0, index+1)+" index: "+index+1);
                line = line.substring(index + 1);
                System.out.println("line �brig: " + line);
                System.out.println("offs: " + offS);
                found = false;
                offS = 0;
            }

        }
        return line;
    }

    /**
     * deletes tags within a Web document
     * 
     * @param line String
     * @return String
     */
    String delete_tags(String line) {
        Character sign;
        char[] chars;
        boolean not_add = false;
        String parsedline = "";

        chars = line.toCharArray();
        for (char c : chars) {
            sign = new Character(c);
            if (sign.equals(new Character('<'))) {
                not_add = true;
            } else if (sign.equals(new Character('>')) && not_add) {
                not_add = false;
            } else if (!not_add) {
                parsedline = parsedline + sign.toString();
            }
        }
        return parsedline;
    }

    public ArrayList<String>[] getParsedDocuments() {
        return this.parsedDocuments;
    }

    public ArrayList<String> getFileNames() {
        return filenames;
    }
}

class HeadlineTextParser {
    private SentenceParser sParser = null;

    private BufferedReader reader = null;

    private ArrayList<String> document = null;

    public HeadlineTextParser(SentenceParser parser) {
        this.sParser = parser;
    }

    public void setReader(BufferedReader rd) {
        this.reader = rd;
    }

    public void parse() {
        boolean start = false;
        document = new ArrayList<String>();

        try {
            // store title
            // String title= sParser.delete_tags(line);
            String sentence = new String("");
            String title = "";

            String line = reader.readLine();
            while (line.indexOf("</HEADLINE", 0) == -1) {
                title = title + sParser.delete_tags(line);
                line = reader.readLine();
            }
            document.add(title);

            // store text
            while (line != null) {
                if (!line.equals("")) {

                    if (line.indexOf("<TEXT", 0) > -1) {
                        start = true;
                    }

                    if (line.indexOf("</TEXT", 0) > -1) {
                        if (sentence.length() > 2) {
                            document.add(sentence);
                        }
                        System.out.println("stop parsing..");
                        start = false;
                    }

                    if (start) {
                        if (line.indexOf(".", 0) != -1 || line.indexOf(";", 0) != -1 || line.indexOf("?", 0) != -1
                                || line.indexOf("!", 0) != -1) {
                            sentence = sParser.findSentence(sParser.delete_tags(sentence + " " + line), this.document);
                        } else {
                            sentence = sentence + " " + line;
                        }
                    }
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public ArrayList<String> getDocument() {
        return this.document;
    }
}

class DocumentParser {
    private SentenceParser sParser = null;

    private BufferedReader reader = null;

    private ArrayList<String> document = null;

    public DocumentParser(SentenceParser parser) {
        this.sParser = parser;
    }

    public void setReader(BufferedReader rd) {
        this.reader = rd;
    }

    public void newDoc() {
        this.document = new ArrayList<String>();
    }

    public void storeTitle(String line) {
        document.add(line);
    }

    public void storeText() {
        try {
            String sentence = new String("");
            String line = reader.readLine();

            // store text
            while (line != null) {
                if (!line.equals("")) {
                    if (line.indexOf(".", 0) != -1 || line.indexOf(";", 0) != -1 || line.indexOf("?", 0) != -1
                            || line.indexOf("!", 0) != -1) {
                        sentence = sParser.findSentence(sParser.delete_tags(sentence + " " + line), this.document);
                    } else {
                        sentence = sentence + " " + line;
                    }
                }
                line = reader.readLine();
            }
            /*
             * System.out.println("changing code for lyrics corpora"); System.out.println("parsing lyrics document.."); while(line!=null) {
             * this.document.add(line); line = reader.readLine(); }
             */

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public ArrayList<String> getDocument() {
        return this.document;
    }
}
