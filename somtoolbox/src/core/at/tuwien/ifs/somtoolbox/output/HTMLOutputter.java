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
package at.tuwien.ifs.somtoolbox.output;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasureNotFoundException;
import at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.labeling.AbstractLabeler;
import at.tuwien.ifs.somtoolbox.output.labeling.Labeler;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Michael Dittenbach
 * @author Christoph Becker
 * @author Rudolf Mayer
 * @version $Id: HTMLOutputter.java 3830 2010-10-06 16:29:11Z mayer $
 */

public class HTMLOutputter implements SOMToolboxApp {
    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptMetric(false),
            OptionFactory.getSwitchIsNormalized(), OptionFactory.getOptInputVectorFile(false),
            OptionFactory.getSwitchIsDenseData(), OptionFactory.getOptLabeling(false),
            OptionFactory.getSwitchIgnoreLabelsWithZero(), OptionFactory.getOptNumberLabels(false),
            OptionFactory.getOptTemplateVectorFile(false), OptionFactory.getOptWeightVectorFile(false),
            OptionFactory.getOptUnitDescriptionFile(false), OptionFactory.getOptMapDescriptionFile(false),
            OptionFactory.getOptHtmlFileName() };

    public static String DESCRIPTION = "Creates an HTML representation of the Map.";

    public static String LONG_DESCRIPTION = "Creates an HTML representation of the Map. The representation displays a hit histogram, and shows the names of the mapped inputs.";

    public static final Type APPLICATION_TYPE = Type.Utils;

    private static final String styleFileName = "somtoolbox.css";

    private static final String tooltipFileName = "wz_tooltip.js";

    private static final String _xmlHeader = "<?xml version=\"1.0\" encoding=\"iso-8859-15\"?>\n";

    private static final String _docType = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";

    private static final String _htmlTag = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n\n";

    private static final String _headTag = "<head>\n";

    private static final String _linkToStyle = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + styleFileName
            + "\">";

    private static final String _titleTag = "<title>";

    private static final String _titleTagClose = "</title>\n";

    private static final String _headTagClose = "</head>\n\n";

    private static final String _bodyTag = "<body>\n";

    private static final String _scriptTag = "<script language=\"JavaScript\" type=\"text/javascript\" src=\""
            + tooltipFileName + "\"></script>\n";

    private static final String _bodyTagClose = "</body>\n";

    private static final String _htmlTagClose = "</html>";

    /**
     * suffix to append to hrefs. This allows the labelling of hrefs to omit the filesuffix, looks much better. defaults
     * to empty string for backwards compatibility
     */
    private String linksuffix = "";

    /**
     * @return the linksuffix currently used
     * @see #linksuffix
     */
    public String getLinksuffix() {
        return linksuffix;
    }

    /**
     * @see #linksuffix
     * @param linksuffix the new suffix to use
     */
    public void setLinksuffix(String linksuffix) {
        this.linksuffix = linksuffix;
    }

    /**
     * Main method for standalone operation. Three possible uses:
     * <ol>
     * <li>generate HTML representation directly from unit description file</li>
     * <li>label the already mapped data and generate HTML</li>
     * <li>map data onto a trained SOM, label the data and generate HTML output.</li>
     * </ol>
     * b. and c. are convenience functions.<br>
     * <br>
     * Options are:
     * <ul>
     * <li>-m metricName, opt., default = L2Metric</li>
     * <li>-n normalization method, opt., default = NONE</li>
     * <li>-v input vector filename, opt.</li>
     * <li>-l labeler, opt., default = null</li>
     * <li>-n number of labels, opt., default = 5</li>
     * <li>-t template vector filename, opt.</li>
     * <li>-u unit description file, opt. (makes sense if no labeling is given and an already labeled map should be
     * written)</li>
     * <li>-w weight vector filename, opt.</li>
     * <li>-d dense input vector matrix, switch</li>
     * <li>html filename, mand.</li>
     * </ul>
     * 
     * @param args the execution arguments as stated above.
     */
    public static void main(String[] args) {
        // register and parse all options for the HTMLOutputter
        JSAP jsap = OptionFactory.registerOptions(OPTIONS);
        JSAPResult config = OptionFactory.parseResults(args, jsap);

        // boolean normalization = config.getBoolean("normalization", false);
        String inputVectorFilename = config.getString("inputVectorFile");
        boolean denseData = config.getBoolean("dense", false);
        boolean ignoreLabelsWithZero = config.getBoolean("ignoreLabelsWithZero", false);
        String templateVectorFilename = config.getString("templateVectorFile", null);
        String labelerName = config.getString("labeling", null);
        int numLabels = config.getInt("numberLabels", AbstractNetworkModel.DEFAULT_LABEL_COUNT);
        String unitDescriptionFilename = config.getString("unitDescriptionFile", null);
        String weightVectorFilename = config.getString("weightVectorFile");
        String mapDescriptionFilename = config.getString("mapDescriptionFile", null);
        String htmlFilename = config.getString("htmlFile");
        if (htmlFilename.endsWith(".html")) {
            htmlFilename = htmlFilename.substring(0, (htmlFilename.length() - 5));
        }

        GrowingSOM gsom = null;
        InputData data = null;
        Labeler labeler = null;

        if (labelerName == null) { // no labeler -> just do html from unit description
            if (unitDescriptionFilename == null) { // error, unit description file required
                OptionFactory.printUsage(jsap, HTMLOutputter.class.getName(), config,
                        "no labeling specified and no unit description file provided.");
            }
            if (inputVectorFilename != null || templateVectorFilename != null || weightVectorFilename != null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "No labeler provided. Ignoring options other than unit description file. Continuing.");
            }
            try {
                gsom = new GrowingSOM(
                        new SOMLibFormatInputReader(null, unitDescriptionFilename, mapDescriptionFilename));
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
                System.exit(-1);
            }

        } else { // labeler given -> more to do
            if (inputVectorFilename == null || templateVectorFilename == null || weightVectorFilename == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Labeler provided. Input vector file, template vector file and weight vector file required. Aborting.");
                System.exit(-1);
            }
            if (labelerName != null) { // if labeling then label
                try {
                    labeler = AbstractLabeler.instantiate(labelerName);
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Instantiated labeler " + labelerName);
                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                            "Could not instantiate labeler \"" + labelerName + "\".");
                    System.exit(-1);
                }
            }
            if (unitDescriptionFilename == null) { // no unit description -> map, label, to html
                try {
                    gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFilename, null,
                            mapDescriptionFilename));
                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
                    System.exit(-1);
                }
                data = InputDataFactory.open(inputVectorFilename, templateVectorFilename, !denseData, true, 1, 7);
                gsom.getLayer().mapData(data);
            } else { // unit description -> label, to html
                try {
                    gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFilename, unitDescriptionFilename,
                            mapDescriptionFilename));
                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
                    System.exit(-1);
                }
                data = InputDataFactory.open(inputVectorFilename, templateVectorFilename, !denseData, true, 1, 7);
            }
            // label map in any case
            if (labelerName != null) { // if labeling then label
                labeler.label(gsom, data, numLabels, ignoreLabelsWithZero);
            }
        }
        // save to html in any case
        String fDir = htmlFilename.substring(0, htmlFilename.lastIndexOf(System.getProperty("file.separator")) + 1);
        String fName = htmlFilename.substring(htmlFilename.lastIndexOf(System.getProperty("file.separator")) + 1);

        try {
            new HTMLOutputter().write(gsom, fDir, fName); // TODO: directory
        } catch (IOException e) { // TODO: create new exception type
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                    "Could not open or write to html file " + fName + ": " + e.getMessage());
            System.exit(-1);
        }

    }

    /**
     * Writes the HTML representation of a GHSOM to a file.
     * 
     * @param ghsom The GHSOM to be written.
     * @param fDir Directory where to write the file to.
     * @param fName Filename without suffix. Usually the name of the training run.
     */
    public void write(GHSOM ghsom, String fDir, String fName, String[] dataNames) throws IOException {
        write(ghsom.topLayerMap(), fDir, fName, dataNames);
    }

    public void write(GHSOM ghsom, String fDir, String fName) throws IOException {
        write(ghsom.topLayerMap(), fDir, fName, null);
    }

    /**
     * Writes the HTML representation of a GrowingSOM to a file.
     * 
     * @param gsom The growing SOM to be written.
     * @param fDir Directory where to write the file to.
     * @param fName Filename without suffix. Usually the name of the training run.
     */
    public void write(GrowingSOM gsom, String fDir, String fName, String[] dataNames) throws IOException {
        writeTooltipFile(fDir);
        writeStyleFile(fDir);

        _write(gsom, fDir, fName, dataNames);
    }

    public void write(GrowingSOM gsom, String fDir, String fName) throws IOException {
        write(gsom, fDir, fName, null);
    }

    private void _write(GrowingSOM gsom, String fDir, String fName, String[] dataNames) throws IOException {
        double[] minmax = calcMinMax(gsom);

        String finalName = fDir + fName + gsom.getLayer().getIdString() + ".html";
        // BufferedWriter bw = new BufferedWriter(fileWriter);

        final FileWriter fileWriter = new FileWriter(finalName);
        System.out.println(fileWriter.getEncoding());
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(finalName), "UTF-8");
        System.out.println(out.getEncoding());
        BufferedWriter bw = new BufferedWriter(out);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Saving HTML file " + fDir + fName + gsom.getLayer().getIdString() + ".html");
        bw.write(createHTML(gsom, fDir, fName, minmax, dataNames));
        bw.close();
    }

    /**
     * Creates the string containing the HTML representation of a map.
     * 
     * @param gsom The GrowingSOM to be written.
     * @param fDir Directory where to write the file.
     * @param fName Filename without suffix. Usually the name of the training run.
     * @param minmax Array of double containing the minima and maxima of distances between data items and weight
     *            vectors, and label values respectively. These values are used for coloring. [0] minimum distance, [1]
     *            maximum distance, [2] minimum label value, [3] maximum label value.
     * @param dataNames Array of strings containing data items to highlight on the map
     * @return String containing the HTML representation.
     */
    private String createHTML(GrowingSOM gsom, String fDir, String fName, double[] minmax, String[] dataNames)
            throws IOException {
        StringBuffer res = new StringBuffer();
        // Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Creating HTML string");

        String revisionString = "<!-- Layer revision: " + gsom.getLayer().getRevision() + " -->\n";

        res.append(_xmlHeader).append(_docType).append(_htmlTag).append(_headTag).append(_linkToStyle).append(_titleTag);
        res.append(fName).append(" - level ").append(gsom.getLayer().getLevel()).append(" map"); // page title
        res.append(_titleTagClose).append(revisionString).append(_headTagClose).append(_bodyTag);

        res.append("<h1>").append(fName).append(" (Level ").append(gsom.getLayer().getLevel()).append(")</h1>\n");

        res.append(upperLayerMapInfo(fName, gsom.getLayer().getSuperUnit()));
        res.append(mapQuantizationInfo(gsom));

        res.append(mapRepresentation(gsom, minmax, fDir, fName, dataNames));

        res.append(_scriptTag).append(_bodyTagClose).append(_htmlTagClose);

        return res.toString();
    }

    private String upperLayerMapInfo(String name, Unit su) {
        StringBuffer res = new StringBuffer();
        if (su != null) {
            res.append("<p>\n");
            res.append("<a href=\"").append(name).append(su.getMapIdString()).append(
                    ".html\">Upper layer map</a> overview:<br/>\n");
            res.append(createMiniMap(name, su));
            res.append("</p>\n");
        }
        return res.toString();
    }

    private String mapQuantizationInfo(GrowingSOM gsom) {
        StringBuffer res = new StringBuffer();
        QualityMeasure qm = null;
        if ((qm = gsom.getLayer().getQualityMeasure()) != null) {
            res.append("<p>\n");
            try {
                for (int i = 0; i < qm.getMapQualityNames().length; i++) {
                    res.append(qm.getMapQualityDescriptions()[i] + ": "
                            + StringUtils.format(qm.getMapQuality(qm.getMapQualityNames()[i]), 5) + "<br/>\n");
                }
            } catch (QualityMeasureNotFoundException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        e.getMessage() + " Aborting. BTW: the must be a major flaw"
                                + "in the quality measure class that has been used.");
                System.exit(-1);
            }
        }
        return res.toString();
    }

    private String mapRepresentation(GrowingSOM gsom, double[] minmax, String fDir, String fName, String[] dataNames)
            throws IOException {
        StringBuffer res = new StringBuffer();
        res.append("<table class=\"map\">\n");
        for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
            res.append("<tr>\n");
            for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                Unit u = null;
                try {
                    u = gsom.getLayer().getUnit(i, j);
                } catch (LayerAccessException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                    System.exit(-1);
                }
                // adapted to mnemonic (sparse) SOMs
                if (u == null) {
                    res.append("<td onmouseover=\"this.T_WIDTH=60;this.T_TITLE=\'(").append(i).append("/").append(j).append(
                            ")\';return escape(\'no unit\');\">\n");
                    res.append("&nbsp\n");

                } else {
                    res.append(unitRepresentation(u, minmax, fDir, fName, dataNames));
                }
                /** ** the unit begin *** */
                /** ** the unit end *** */
            }
            res.append("</tr>\n");
        }
        res.append("</table>\n");
        return res.toString();
    }

    private String unitRepresentation(Unit u, double[] minmax, String fDir, String fName, String[] dataNames)
            throws IOException {
        StringBuffer res = new StringBuffer();
        if (u.getNumberOfMappedInputs() == 0) { // no data mapped onto unit
            res.append(emptyUnitRepresentation(u));
        } else { // data mapped onto unit
            res.append(nonEmptyUnitRepresentation(u, minmax, fDir, fName, dataNames));
        }
        res.append("</td>\n");
        return res.toString();
    }

    private String emptyUnitRepresentation(Unit u) {
        StringBuffer res = new StringBuffer();
        res.append("<td class=\"map\" onmouseover=\"this.T_WIDTH=60;this.T_TITLE=\'(").append(u.getXPos()).append("/").append(
                u.getYPos()).append(")\';return escape(\'no data\');\">\n");
        res.append("&nbsp\n");
        return res.toString();
    }

    private String nonEmptyUnitRepresentation(Unit u, double[] minmax, String fDir, String fName, String[] hlDataNames)
            throws IOException {
        // double minDistance = minmax[0];
        // double maxDistance = minmax[1];
        // double minLabelValue = minmax[2];
        // double maxLabelValue = minmax[3];
        StringBuffer res = new StringBuffer();
        res.append("<td class=\"map\">\n");

        res.append(labelInfo(u, minmax));

        // String[] dataNames = u.getMappedInputNames();
        // double[] datadistances = u.getMappedInputDistances();

        if (u.getMappedSOM() != null) { // sub-som, list data in tooltip
            res.append(expandedUnitRepresentation(u, minmax, fDir, fName, hlDataNames));
        } else { // leaf node
            res.append(leafUnitRepresentation(u, minmax, hlDataNames));
        }
        return res.toString();
    }

    private String expandedUnitRepresentation(Unit u, double[] minmax, String fDir, String fName, String[] hlDataNames)
            throws IOException {
        double minDistance = minmax[0];
        double maxDistance = minmax[1];
        // double minLabelValue = minmax[2];
        // double maxLabelValue = minmax[3];
        String[] dataNames = u.getMappedInputNames();
        double[] datadistances = u.getMappedInputDistances();

        /** ** check if downlink to highlight start *** */
        int nrData = 0;
        int dn = 0;
        if (hlDataNames != null) {
            while (dn < dataNames.length) {
                int hl = 0;
                while (hl < hlDataNames.length) {
                    if (hlDataNames[hl].equals(dataNames[dn])) {
                        nrData++;
                    }
                    hl++;
                }
                dn++;
            }
        }
        /** ** check if downlink to highlight end *** */

        StringBuffer res = new StringBuffer();
        res.append("<span onmouseover=\"").append("this.T_STICKY=true;this.T_WIDTH=10;return escape(\'").append(
                createTooltipDataTable(dataNames, datadistances, minDistance, maxDistance, hlDataNames)).append("\');").append(
                "\">").append(dataNames.length).append(" data items</span><br/>\n");
        if (nrData == 0) {
            res.append("<a href=\"").append(fName).append(u.getMappedSOM().getLayer().getIdString()).append(".html").append(
                    "\">down</a>\n");
        } else {
            res.append(
                    "<span class=\"marked\" onmouseover=\"this.T_WIDTH=210;return escape(\'# of matching data items: ").append(
                    nrData).append("\');\"><a href=\"").append(fName).append(u.getMappedSOM().getLayer().getIdString()).append(
                    ".html").append("\">down</a></span>\n");
        }
        res.append("<hr/>\n");
        _write(u.getMappedSOM(), fDir, fName, hlDataNames);
        return res.toString();
    }

    private String leafUnitRepresentation(Unit u, double[] minmax, String[] hlDataNames) {
        double minDistance = minmax[0];
        double maxDistance = minmax[1];
        // double minLabelValue = minmax[2];
        // double maxLabelValue = minmax[3];
        String[] dataNames = u.getMappedInputNames();
        double[] datadistances = u.getMappedInputDistances();
        StringBuffer res = new StringBuffer();
        for (int l = 0; l < u.getNumberOfMappedInputs(); l++) {
            String highlightString = "";
            String highlightItemNr = "";
            if (hlDataNames != null) {
                /** ** check if data to highlight start *** */
                boolean found = false;
                int hl = 0;
                while (!found && hl < hlDataNames.length) {
                    if (hlDataNames[hl].equals(dataNames[l])) {
                        found = true;
                        highlightString = "class=\"marked\"";
                        highlightItemNr = "&nbsp;" + (hl + 1) + "";
                    }
                    hl++;
                }
            }
            /** ** check if data to highlight end *** */

            res.append("<span ").append(highlightString).append(" id=\"datum").append(
                    (Math.round(((u.getMappedInputDistances()[l] - minDistance) / (maxDistance - minDistance) * 9)) * 10 + 10)).append(
                    "\" ").append("onmouseover=\"this.T_WIDTH=210;this.T_TITLE=\'(").append(u.getXPos()).append("/").append(
                    u.getYPos()).append(") ");
            try {
                QualityMeasure qm = null;
                if ((qm = u.getLayer().getQualityMeasure()) != null) {
                    for (int i = 0; i < qm.getUnitQualityNames().length; i++) {
                        res.append(qm.getUnitQualityNames()[i]).append("=").append(
                                StringUtils.format(
                                        qm.getUnitQualities(qm.getUnitQualityNames()[i])[u.getXPos()][u.getYPos()], 5)).append(
                                " ");
                    }
                }
            } catch (QualityMeasureNotFoundException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        e.getMessage() + " Aborting. BTW: the must be a major flaw"
                                + "in the quality measure class that has been used.");
                System.exit(-1);
            }
            // "qe="+form.format(u.getQuantizationError())+" " +
            // "mqe="+form.format(u.getMeanQuantizationError())+
            String dataName = dataNames[l];
            try {
                dataName = URLDecoder.decode(dataName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace(System.err);
                // Logger.getLogger("at.tuwien.ifs.somtoolbox.output.HTMLOutputter").log(LogLevel.SEVERE,"decoder failed",e);
                // Just logging e.getMessage() is usually not enough!
                // I'd suggest commons-logging ;-) .... and, of course, log4j ;)
            }

            res.append("\';return escape(\'dist. from weight vec.: ").append(StringUtils.format(datadistances[l], 5)).append(
                    "\');\">").append("<a href=\"files/").append(dataName).append(linksuffix).append("\">").append(
                    dataName).append("</a>").append("</span>").append(highlightItemNr).append("<br/>\n");
        }
        return res.toString();
    }

    private String labelInfo(Unit u, double[] minmax) {
        // double minDistance = minmax[0];
        // double maxDistance = minmax[1];
        double minLabelValue = minmax[2];
        double maxLabelValue = minmax[3];
        StringBuffer res = new StringBuffer();
        if (u.getLabels() != null) {
            try {
                for (int l = 0; l < u.getLabels().length; l++) {
                    res.append("<span id=\"label").append(
                            (Math.round(((u.getLabels()[l].getValue() - minLabelValue)
                                    / (maxLabelValue - minLabelValue) * 9)) * 10 + 10)).append("\" ").append(
                            "onmouseover=\"this.T_WIDTH=210;this.T_TITLE=\'(").append(u.getXPos()).append("/").append(
                            u.getYPos()).append(") ");
                    QualityMeasure qm = null;
                    if ((qm = u.getLayer().getQualityMeasure()) != null) {
                        for (int i = 0; i < qm.getUnitQualityNames().length; i++) {
                            res.append(qm.getUnitQualityNames()[i]).append("=").append(
                                    StringUtils.format(
                                            qm.getUnitQualities(qm.getUnitQualityNames()[i])[u.getXPos()][u.getYPos()],
                                            5)).append(" ");
                        }
                    }
                    // "qe="+form.format(u.getQuantizationError())+" " +
                    // "mqe="+form.format(u.getMeanQuantizationError())+
                    res.append("\';return escape(\'mean=").append(StringUtils.format(u.getLabels()[l].getValue(), 5)).append(
                            ", qe=").append(StringUtils.format(u.getLabels()[l].getQe(), 5)).append("\');\">").append(
                            u.getLabels()[l].getName()).append("</span><br/>\n");
                }
            } catch (QualityMeasureNotFoundException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        e.getMessage() + " Aborting. BTW: the must be a major flaw"
                                + "in the quality measure class that has been used.");
                System.exit(-1);
            }
            res.append("<hr/>\n");
        }
        return res.toString();
    }

    private String createTooltipDataTable(String[] names, double[] distances, double minDistance, double maxDistance,
            String[] hlDataNames) {
        StringBuffer res = new StringBuffer();
        int rows = 20;
        int cols = names.length / rows;
        if (rows * cols < names.length) {
            cols++;
        }

        res.append("<table class=&quot;datapreview&quot;>");
        for (int r = 0; r < rows; r++) {
            res.append("<tr>");
            for (int c = 0; c < cols; c++) {
                int i = c * rows + r;
                if (i < names.length) {
                    /** ** check if data to highlight start *** */
                    boolean found = false;
                    String highlightString = "";
                    // String highlightItemNr = "";
                    int hl = 0;
                    if (hlDataNames != null) {
                        while (!found && hl < hlDataNames.length) {
                            if (hlDataNames[hl].equals(names[i])) {
                                found = true;
                                highlightString = "class=&quot;marked&quot;";
                                // highlightItemNr = "&nbsp;" + (hl+1) + "";
                            }
                            hl++;
                        }
                    }
                    /** ** check if data to highlight end *** */
                    res.append("<td><span ").append(highlightString).append(" id=&quot;datum").append(
                            (Math.round(((distances[i] - minDistance) / (maxDistance - minDistance) * 9)) * 10 + 10)).append(
                            "&quot;>").append("<a href=&quot;files/").append(names[i]).append("&quot;>").append(
                            names[i]).append("</a></span></td>");
                } else {
                    res.append("<td></td>");
                }
            }
            res.append("</tr>");
        }
        res.append("</table>");
        return res.toString();
    }

    private String createMiniMap(String fName, Unit u) {
        int xSize = u.getMapXSize();
        int ySize = u.getMapYSize();
        int xPos = u.getXPos();
        int yPos = u.getYPos();
        StringBuffer res = new StringBuffer();
        res.append("<table class=\"miniview\">\n");
        for (int j = 0; j < ySize; j++) {
            res.append("<tr>\n");
            for (int i = 0; i < xSize; i++) {
                if (i == xPos && j == yPos) {
                    res.append("    <td id=\"full\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>\n");
                } else {
                    try {
                        if (u.getLayer().getUnit(i, j).getMappedSOM() != null) {
                            res.append("    <td id=\"expanded\"><a href=\"").append(fName).append(
                                    u.getLayer().getUnit(i, j).getMappedSOM().getLayer().getIdString()).append(".html").append(
                                    "\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a></td>\n");
                        } else {
                            res.append("    <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>\n");
                        }
                    } catch (SOMToolboxException e) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                        System.exit(-1);
                    }
                }
            }
            res.append("</tr>\n");
        }
        res.append("</table>\n");
        return res.toString();
    }

    private double[] calcMinMax(GrowingSOM gsom) {
        double minDistance = Double.MAX_VALUE;
        double maxDistance = 0;
        double minLabelValue = Double.MAX_VALUE;
        double maxLabelValue = 0;
        Unit[] u = gsom.getLayer().getAllUnits();

        for (Unit element : u) {
            if (element.getNumberOfMappedInputs() > 0) {
                for (int j = 0; j < element.getMappedInputDistances().length; j++) {
                    double dist = element.getMappedInputDistances()[j];
                    if (dist > maxDistance) {
                        maxDistance = dist;
                    }
                    if (dist < minDistance) {
                        minDistance = dist;
                    }
                }
            }
            if (element.getLabels() != null) {
                for (int j = 0; j < element.getLabels().length; j++) {
                    double labelValue = element.getLabels()[j].getValue();
                    if (labelValue > maxLabelValue) {
                        maxLabelValue = labelValue;
                    }
                    if (labelValue < minLabelValue) {
                        minLabelValue = labelValue;
                    }
                }
            }
        }
        double[] res = new double[4];
        res[0] = minDistance;
        res[1] = maxDistance;
        res[2] = minLabelValue;
        res[3] = maxLabelValue;
        return res;
    }

    // public void write(GrowingSOM gsom, String fName, Labeller label) throws IOException { // daten bereits gemappt
    // }

    // public void write(GrowingSOM gsom, InputData data, String fName, Labeller label) throws IOException { // daten
    // mappen und schreiben
    // }

    private static void writeTooltipFile(String fDir) {
        String tooltip = ""
                + "/* This notice must be untouched at all times.\n"
                + "\n"
                + "wz_tooltip.js    v. 3.25\n"
                + "\n"
                + "The latest version is available at\n"
                + "http://www.walterzorn.com\n"
                + "or http://www.devira.com\n"
                + "or http://www.walterzorn.de\n"
                + "\n"
                + "Copyright (c) 2002-2003 Walter Zorn. All rights reserved.\n"
                + "Created 1. 12. 2002 by Walter Zorn (Web: http://www.walterzorn.com )\n"
                + "Last modified: 21. 4. 2004\n"
                + "\n"
                + "Cross-browser tooltips working even in Opera 5 and 6,\n"
                + "as well as in NN 4, Gecko-Browsers, IE4+, Opera 7 and Konqueror.\n"
                + "No onmouseouts required.\n"
                + "Appearance of tooltips can be individually configured\n"
                + "via commands within the onmouseovers.\n"
                + "\n"
                + "This program is free software;\n"
                + "you can redistribute it and/or modify it under the terms of the\n"
                + "GNU General Public License as published by the Free Software Foundation;\n"
                + "either version 2 of the License, or (at your option) any later version.\n"
                + "This program is distributed in the hope that it will be useful,\n"
                + "but WITHOUT ANY WARRANTY;\n"
                + "without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n"
                + "See the GNU General Public License\n"
                + "at http://www.gnu.org/copyleft/gpl.html for more details.\n"
                + "*/\n"
                + "\n"
                + "\n"
                + "\n"
                + "////////////////  GLOBAL TOOPTIP CONFIGURATION  /////////////////////\n"
                + "var ttBgColor      = \'#fafba0\';\n"
                + "var ttBgImg        = \'\';           // path to background image;\n"
                + "var ttBorderColor  = \'#000000\';\n"
                + "var ttBorderWidth  = 1;\n"
                + "var ttDelay        = 300;          // time span until tooltip shows up [milliseconds]\n"
                + "var ttFontColor    = \'#000000\';\n"
                + "var ttFontFace     = \'luxi sans, arial, helvetica, sans-serif\';\n"
                + "var ttFontSize     = \'13px\';\n"
                + "var ttFontWeight   = \'normal\';     // alternative is \'bold\';\n"
                + "var ttOffsetX      = 8;            // horizontal offset of left-top corner from mousepointer\n"
                + "var ttOffsetY      = 19;           // vertical offset                   \"\n"
                + "var ttPadding      = 2;            // spacing between border and content\n"
                + "var ttShadowColor  = \'\';\n"
                + "var ttShadowWidth  = 0;\n"
                + "var ttTitleColor   = \'#ffffff\';    // color of caption text\n"
                + "var ttWidth        = 90;\n"
                + "////////////////////  END OF TOOLTIP CONFIG  ////////////////////////\n"
                + "\n"
                + "\n"
                + "\n"
                + "//////////////  TAGS WITH TOOLTIP FUNCTIONALITY  ////////////////////\n"
                + "// List may be extended or shortened:\n"
                + "var tt_tags = new Array(\'a\',\'area\',\'b\',\'big\',\'caption\',\'center\',\'code\',\'dd\',\'div\',\'dl\',\'dt\',\'em\',\'h1\',\'h2\',\'h3\',\'h4\',\'h5\',\'h6\',\'i\',\'img\',\'input\',\'li\',\'map\',\'ol\',\'p\',\'pre\',\'s\',\'small\',\'span\',\'strike\',\'strong\',\'sub\',\'sup\',\'table\',\'td\',\'th\',\'tr\',\'tt\',\'u\',\'var\',\'ul\',\'layer\');\n"
                + "/////////////////////////////////////////////////////////////////////\n"
                + "\n"
                + "\n"
                + "\n"
                + "///////// DON\'T CHANGE ANYTHING BELOW THIS LINE /////////////////////\n"
                + "var tt_obj,                // current tooltip\n"
                + "tt_objW = 0, tt_objH = 0,  // width and height of tt_obj\n"
                + "tt_objX = 0, tt_objY = 0,\n"
                + "tt_offX = 0, tt_offY = 0,\n"
                + "xlim = 0, ylim = 0,        // right and bottom borders of visible client area\n"
                + "tt_above = false,          // true if T_ABOVE cmd\n"
                + "tt_static = false,         // tt_obj static?\n"
                + "tt_sticky = false,         // tt_obj sticky?\n"
                + "tt_wait = false,\n"
                + "tt_vis = false,            // tooltip visibility flag\n"
                + "tt_dwn = false,            // true while tooltip below mousepointer\n"
                + "tt_u = \'undefined\',\n"
                + "tt_inputs = new Array();   // drop-down-boxes to be hidden in IE\n"
                + "\n"
                + "\n"
                + "var tt_db = (document.compatMode && document.compatMode != \'BackCompat\')? document.documentElement : document.body? document.body : null,\n"
                + "tt_n = navigator.userAgent.toLowerCase();\n"
                + "\n"
                + "// Browser flags\n"
                + "var tt_op = !!(window.opera && document.getElementById),\n"
                + "tt_op6 = tt_op && !document.defaultView,\n"
                + "tt_ie = tt_n.indexOf(\'msie\') != -1 && document.all && tt_db && !tt_op,\n"
                + "tt_n4 = (document.layers && typeof document.classes != \"undefined\"),\n"
                + "tt_n6 = (!tt_op && document.defaultView && typeof document.defaultView.getComputedStyle != \"undefined\"),\n"
                + "tt_w3c = !tt_ie && !tt_n6 && !tt_op && document.getElementById;\n"
                + "\n"
                + "tt_n = \'\';\n"
                + "\n"
                + "\n"
                + "function tt_Int(t_x)\n"
                + "{\n"
                + "	var t_y;\n"
                + "	return isNaN(t_y = parseInt(t_x))? 0 : t_y;\n"
                + "}\n"
                + "\n"
                + "\n"
                + "function wzReplace(t_x, t_y)\n"
                + "{\n"
                + "	var t_ret = \'\',\n"
                + "	t_str = this,\n"
                + "	t_xI;\n"
                + "	while ((t_xI = t_str.indexOf(t_x)) != -1)\n"
                + "	{\n"
                + "		t_ret += t_str.substring(0, t_xI) + t_y;\n"
                + "		t_str = t_str.substring(t_xI + t_x.length);\n"
                + "	}\n"
                + "	return t_ret+t_str;\n"
                + "}\n"
                + "String.prototype.wzReplace = wzReplace;\n"
                + "\n"
                + "\n"
                + "function tt_N4Tags(tagtyp, t_d, t_y)\n"
                + "{\n"
                + "	t_d = t_d || document;\n"
                + "	t_y = t_y || new Array();\n"
                + "	var t_x = (tagtyp==\'a\')? t_d.links : t_d.layers;\n"
                + "	for (var z = t_x.length; z--;) t_y[t_y.length] = t_x[z];\n"
                + "	for (var z = t_d.layers.length; z--;) t_y = tt_N4Tags(tagtyp, t_d.layers[z].document, t_y);\n"
                + "	return t_y;\n"
                + "}\n"
                + "\n"
                + "\n"
                + "function tt_GetSelects()\n"
                + "{\n"
                + "	if (!tt_op6 && !tt_ie) return;\n"
                + "	var t_s = tt_op6? \'input\' : \'select\';\n"
                + "	if (document.all)\n"
                + "	{\n"
                + "		var t_i = document.all.tags(t_s).length; while (t_i--)\n"
                + "			tt_inputs[t_i] = document.all.tags(t_s)[t_i];\n"
                + "	}\n"
                + "	else if (document.getElementsByTagName)\n"
                + "	{\n"
                + "		var t_i = document.getElementsByTagName(t_s).length; while (t_i--)\n"
                + "			tt_inputs[t_i] = document.getElementsByTagName(t_s)[t_i];\n"
                + "	}\n"
                + "	var t_i = tt_inputs.length; while (t_i--)\n"
                + "	{\n"
                + "		tt_inputs[t_i].x = 0;\n"
                + "		tt_inputs[t_i].y = 0;\n"
                + "		var t_o = tt_inputs[t_i];\n"
                + "		while (t_o)\n"
                + "		{\n"
                + "			tt_inputs[t_i].x += t_o.offsetLeft || 0;\n"
                + "			tt_inputs[t_i].y += t_o.offsetTop|| 0;\n"
                + "			t_o = t_o.offsetParent;\n"
                + "		}\n"
                + "	}\n"
                + "}\n"
                + "\n"
                + "\n"
                + "function tt_Htm(tt, t_id, txt)\n"
                + "{\n"
                + "	var t_bgc = (typeof tt.T_BGCOLOR != tt_u)? tt.T_BGCOLOR : ttBgColor,\n"
                + "	t_bgimg   = (typeof tt.T_BGIMG != tt_u)? tt.T_BGIMG : ttBgImg,\n"
                + "	t_bc      = (typeof tt.T_BORDERCOLOR != tt_u)? tt.T_BORDERCOLOR : ttBorderColor,\n"
                + "	t_bw      = (typeof tt.T_BORDERWIDTH != tt_u)? tt.T_BORDERWIDTH : ttBorderWidth,\n"
                + "	t_ff      = (typeof tt.T_FONTFACE != tt_u)? tt.T_FONTFACE : ttFontFace,\n"
                + "	t_fc      = (typeof tt.T_FONTCOLOR != tt_u)? tt.T_FONTCOLOR : ttFontColor,\n"
                + "	t_fsz     = (typeof tt.T_FONTSIZE != tt_u)? tt.T_FONTSIZE : ttFontSize,\n"
                + "	t_fwght   = (typeof tt.T_FONTWEIGHT != tt_u)? tt.T_FONTWEIGHT : ttFontWeight,\n"
                + "	t_padd    = (typeof tt.T_PADDING != tt_u)? tt.T_PADDING : ttPadding,\n"
                + "	t_shc     = (typeof tt.T_SHADOWCOLOR != tt_u)? tt.T_SHADOWCOLOR : (ttShadowColor || 0),\n"
                + "	t_shw     = (typeof tt.T_SHADOWWIDTH != tt_u)? tt.T_SHADOWWIDTH : (ttShadowWidth || 0),\n"
                + "	t_tit     = (typeof tt.T_TITLE != tt_u)? tt.T_TITLE : \'\',\n"
                + "	t_titc    = (typeof tt.T_TITLECOLOR != tt_u)? tt.T_TITLECOLOR : ttTitleColor,\n"
                + "	t_w       = (typeof tt.T_WIDTH != tt_u)? tt.T_WIDTH  : ttWidth;\n"
                + "	if (t_shc || t_shw)\n"
                + "	{\n"
                + "		t_shc = t_shc || \'#cccccc\';\n"
                + "		t_shw = t_shw || 3;\n"
                + "	}\n"
                + "	if (tt_n4 && (t_fsz == \'10px\' || t_fsz == \'11px\')) t_fsz = \'12px\';\n"
                + "\n"
                + "\n"
                + "	var t_y = \'<div id=\"\' + t_id + \'\" style=\"position:absolute;z-index:1010;\';\n"
                + "	t_y += \'left:0px;top:0px;width:\' + (t_w+t_shw) + \'px;visibility:\' + (tt_n4? \'hide\' : \'hidden\') + \';\">\';\n"
                + "	t_y += \'<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"\' + (t_bc? (\' bgcolor=\"\' + t_bc + \'\"\') : \'\') + \' width=\"\' + t_w + \'\">\';\n"
                + "	if (t_tit)\n"
                + "	{\n"
                + "		t_y += \'<tr><td style=\"padding-left:3px;\"><font color=\"\' + t_titc + \'\" face=\"\' + t_ff + \'\" \';\n"
                + "		t_y += \'style=\"color:\' + t_titc + \';font-family:\' + t_ff + \';font-size:\' + t_fsz + \';\"><b>\';\n"
                + "		t_y += t_tit + \'<\\/b><\\/font><\\/td><\\/tr>\';\n"
                + "	}\n"
                + "	t_y += \'<tr><td><table border=\"0\" cellpadding=\"\' + t_padd + \'\" cellspacing=\"\' + t_bw + \'\" width=\"100%\">\';\n"
                + "	t_y += \'<tr><td\' + (t_bgc? (\' bgcolor=\"\' + t_bgc + \'\"\') : \'\') + (t_bgimg? \' background=\"\' + t_bgimg + \'\"\' : \'\');\n"
                + "	if (tt_n6) t_y += \' style=\"padding:\' + t_padd + \'px;\"\';\n"
                + "	t_y += \'><font color=\"\' + t_fc + \'\" face=\"\' + t_ff + \'\"\';\n"
                + "	t_y += \' style=\"color:\' + t_fc + \';font-family:\' + t_ff + \';font-size:\' + t_fsz + \';font-weight:\' + t_fwght + \';\">\';\n"
                + "	if (t_fwght == \'bold\') t_y += \'<b>\';\n"
                + "	t_y += txt;\n"
                + "	if (t_fwght == \'bold\') t_y += \'<\\/b>\';\n"
                + "	t_y += \'<\\/font><\\/td><\\/tr><\\/table><\\/td><\\/tr><\\/table>\';\n"
                + "	if (t_shw)\n"
                + "	{\n"
                + "		var t_spct = Math.round(t_shw*1.3);\n"
                + "		if (tt_n4)\n"
                + "		{\n"
                + "			t_y += \'<layer bgcolor=\"\' + t_shc + \'\" left=\"\' + t_w + \'\" top=\"\' + t_spct + \'\" width=\"\' + t_shw + \'\" height=\"0\"><\\/layer>\';\n"
                + "			t_y += \'<layer bgcolor=\"\' + t_shc + \'\" left=\"\' + t_spct + \'\" align=\"bottom\" width=\"\' + (t_w-t_spct) + \'\" height=\"\' + t_shw + \'\"><\\/layer>\';\n"
                + "		}\n"
                + "		else\n"
                + "		{\n"
                + "			var t_opa = tt_n6? \'-moz-opacity:0.85;\' : tt_ie? \'filter:Alpha(opacity=85);\' : \'\';\n"
                + "			t_y += \'<div id=\"\' + t_id + \'R\" style=\"position:absolute;background:\' + t_shc + \';left:\' + t_w + \'px;top:\' + t_spct + \'px;width:\' + t_shw + \'px;height:1px;overflow:hidden;\' + t_opa + \'\"><\\/div>\';\n"
                + "			t_y += \'<div style=\"position:relative;background:\' + t_shc + \';left:\' + t_spct + \'px;top:0px;width:\' + (t_w-t_spct) + \'px;height:\' + t_shw + \'px;overflow:hidden;\' + t_opa + \'\"><\\/div>\';\n"
                + "		}\n"
                + "	}\n"
                + "	t_y += \'<\\/div>\';\n"
                + "	return t_y;\n"
                + "}\n"
                + "\n"
                + "\n"
                + "function tt_Init()\n"
                + "{\n"
                + "	if (!(tt_op || tt_n4 || tt_n6 || tt_ie || tt_w3c)) return;\n"
                + "\n"
                + "	var htm = tt_n4? \'<div style=\"position:absolute;\"><\\/div>\' : \'\',\n"
                + "	tags,\n"
                + "	t_tj,\n"
                + "	over,\n"
                + "	esc = \'return escape(\';\n"
                + "	var i = tt_tags.length; while (i--)\n"
                + "	{\n"
                + "		tags = tt_ie? (document.all.tags(tt_tags[i]) || 1)\n"
                + "			: document.getElementsByTagName? (document.getElementsByTagName(tt_tags[i]) || 1)\n"
                + "			: (!tt_n4 && tt_tags[i]==\'a\')? document.links\n"
                + "			: 1;\n"
                + "		if (tt_n4 && (tt_tags[i] == \'a\' || tt_tags[i] == \'layer\')) tags = tt_N4Tags(tt_tags[i]);\n"
                + "		var j = tags.length; while (j--)\n"
                + "		{\n"
                + "			if (typeof (t_tj = tags[j]).onmouseover == \'function\' && t_tj.onmouseover.toString().indexOf(esc) != -1 && !tt_n6 || tt_n6 && (over = t_tj.getAttribute(\'onmouseover\')) && over.indexOf(esc) != -1)\n"
                + "			{\n"
                + "				if (over) t_tj.onmouseover = new Function(over);\n"
                + "				var txt = unescape(t_tj.onmouseover());\n"
                + "				htm += tt_Htm(\n"
                + "					t_tj,\n"
                + "					\'tOoLtIp\'+i+\'\'+j,\n"
                + "					txt.wzReplace(\'& \',\'&\')\n"
                + "				);\n"
                + "\n"
                + "				t_tj.onmouseover = new Function(\'e\',\n"
                + "					\'tt_Show(e,\'+\n"
                + "					\'\"tOoLtIp\' +i+\'\'+j+ \'\",\'+\n"
                + "					(typeof t_tj.T_ABOVE != tt_u) + \',\'+\n"
                + "					((typeof t_tj.T_DELAY != tt_u)? t_tj.T_DELAY : ttDelay) + \',\'+\n"
                + "					((typeof t_tj.T_FIX != tt_u)? \'\"\'+t_tj.T_FIX+\'\"\' : \'\"\"\') + \',\'+\n"
                + "					(typeof t_tj.T_LEFT != tt_u) + \',\'+\n"
                + "					((typeof t_tj.T_OFFSETX != tt_u)? t_tj.T_OFFSETX : ttOffsetX) + \',\'+\n"
                + "					((typeof t_tj.T_OFFSETY != tt_u)? t_tj.T_OFFSETY : ttOffsetY) + \',\'+\n"
                + "					(typeof t_tj.T_STATIC != tt_u) + \',\'+\n"
                + "					(typeof t_tj.T_STICKY != tt_u) +\n"
                + "					\');\'\n"
                + "				);\n"
                + "				t_tj.onmouseout = tt_Hide;\n"
                + "				if (t_tj.alt) t_tj.alt = \"\";\n"
                + "				if (t_tj.title) t_tj.title = \"\";\n"
                + "			}\n"
                + "		}\n"
                + "	}\n"
                + "	document.write(htm);\n"
                + "}\n"
                + "\n"
                + "\n"
                + "function tt_EvX(t_e)\n"
                + "{\n"
                + "	var t_y = tt_Int(t_e.pageX || t_e.clientX || 0) +\n"
                + "		tt_Int(tt_ie? tt_db.scrollLeft : 0) +\n"
                + "		tt_offX;\n"
                + "	if (t_y > xlim) t_y = xlim;\n"
                + "	var t_scr = tt_Int(window.pageXOffset || (tt_db? tt_db.scrollLeft : 0) || 0);\n"
                + "	if (t_y < t_scr) t_y = t_scr;\n"
                + "	return t_y;\n"
                + "}\n"
                + "\n"
                + "\n"
                + "function tt_EvY(t_e)\n"
                + "{\n"
                + "	var t_y = tt_Int(t_e.pageY || t_e.clientY || 0) +\n"
                + "		tt_Int(tt_ie? tt_db.scrollTop : 0);\n"
                + "	if (tt_above) t_y -= (tt_objH + tt_offY - (tt_op? 31 : 15));\n"
                + "	else if (t_y > ylim || !tt_dwn && t_y > ylim-24)\n"
                + "	{\n"
                + "		t_y -= (tt_objH + 5);\n"
                + "		tt_dwn = false;\n"
                + "	}\n"
                + "	else\n"
                + "	{\n"
                + "		t_y += tt_offY;\n"
                + "		tt_dwn = true;\n"
                + "	}\n"
                + "	return t_y;\n"
                + "}\n"
                + "\n"
                + "\n"
                + "function tt_ReleasMov()\n"
                + "{\n"
                + "	if (document.onmousemove == tt_Move)\n"
                + "	{\n"
                + "		if (document.releaseEvents) document.releaseEvents(Event.MOUSEMOVE);\n"
                + "		document.onmousemove = null;\n"
                + "	}\n"
                + "}\n"
                + "\n"
                + "\n"
                + "function tt_HideInput()\n"
                + "{\n"
                + "	if (!(tt_ie || tt_op6) || !tt_inputs) return;\n"
                + "	var t_o;\n"
                + "	var t_i = tt_inputs.length; while (t_i--)\n"
                + "	{\n"
                + "		t_o = tt_inputs[t_i];\n"
                + "		if (tt_vis && tt_objX+tt_objW > t_o.x && tt_objX < t_o.x+t_o.offsetWidth && tt_objY+tt_objH > t_o.y && tt_objY < t_o.y+t_o.offsetHeight)\n"
                + "			t_o.style.visibility = \'hidden\';\n" + "		else t_o.style.visibility = \'visible\';\n" + "	}\n"
                + "}\n" + "\n" + "\n" + "function tt_GetDiv(t_id)\n" + "{\n" + "	return (\n"
                + "		tt_n4? (document.layers[t_id] || null)\n" + "		: tt_ie? (document.all[t_id] || null)\n"
                + "		: (document.getElementById(t_id) || null)\n" + "	);\n" + "}\n" + "\n" + "\n"
                + "function tt_GetDivW()\n" + "{\n" + "	return (\n" + "		tt_n4? tt_obj.clip.width\n"
                + "		: tt_obj.style.pixelWidth? tt_obj.style.pixelWidth\n" + "		: tt_obj.offsetWidth\n" + "	);\n"
                + "}\n" + "\n" + "\n" + "function tt_GetDivH()\n" + "{\n" + "	return (\n"
                + "		tt_n4? tt_obj.clip.height\n" + "		: tt_obj.style.pixelHeight? tt_obj.style.pixelHeight\n"
                + "		: tt_obj.offsetHeight\n" + "	);\n" + "}\n" + "\n" + "\n"
                + "// Compat with DragDrop Lib: Ensure z-index of tooltip is lifted beyond toplevel dragdrop element\n"
                + "function tt_SetDivZ()\n" + "{\n" + "	var t_i = tt_obj.style || tt_obj;\n"
                + "	if (window.dd && dd.z)\n" + "		t_i.zIndex = Math.max(dd.z+1, t_i.zIndex);\n" + "}\n" + "\n" + "\n"
                + "function tt_SetDivPos(t_x, t_y)\n" + "{\n" + "	var t_i = tt_obj.style || tt_obj;\n"
                + "	var t_px = (tt_op6 || tt_n4)? \'\' : \'px\';\n" + "	t_i.left = (tt_objX = t_x) + t_px;\n"
                + "	t_i.top = (tt_objY = t_y) + t_px;\n" + "	tt_HideInput();\n" + "}\n" + "\n" + "\n"
                + "function tt_ShowDiv(t_x)\n" + "{\n" + "	if (tt_n4) tt_obj.visibility = t_x? \'show\' : \'hide\';\n"
                + "	else tt_obj.style.visibility = t_x? \'visible\' : \'hidden\';\n" + "	tt_vis = t_x;\n"
                + "	tt_HideInput();\n" + "}\n" + "\n" + "\n"
                + "function tt_Show(t_e, t_id, t_above, t_delay, t_fix, t_left, t_offx, t_offy, t_static, t_sticky)\n"
                + "{\n" + "	if (tt_obj) tt_Hide();\n" + "	var t_mf = document.onmousemove || null;\n"
                + "	if (window.dd && (window.DRAG && t_mf == DRAG || window.RESIZE && t_mf == RESIZE)) return;\n"
                + "	var t_uf = document.onmouseup || null;\n" + "	if (t_mf && t_uf) t_uf(t_e);\n" + "\n"
                + "	tt_obj = tt_GetDiv(t_id);\n" + "	if (tt_obj)\n" + "	{\n" + "		tt_dwn = !(tt_above = t_above);\n"
                + "		tt_static = t_static;\n" + "		tt_sticky = t_sticky;\n" + "		tt_objW = tt_GetDivW();\n"
                + "		tt_objH = tt_GetDivH();\n" + "		tt_offX = t_left? -(tt_objW+t_offx) : t_offx;\n"
                + "		tt_offY = t_offy;\n" + "		if (tt_op) tt_offY += 21;\n" + "		if (tt_n4)\n" + "		{\n"
                + "			if (tt_obj.document.layers.length)\n" + "			{\n" + "				var t_sh = tt_obj.document.layers[0];\n"
                + "				t_sh.clip.height = tt_objH - Math.round(t_sh.clip.width*1.3);\n" + "			}\n" + "		}\n"
                + "		else\n" + "		{\n" + "			var t_sh = tt_GetDiv(t_id+\'R\');\n" + "			if (t_sh)\n" + "			{\n"
                + "				var t_h = tt_objH - tt_Int(t_sh.style.pixelTop || t_sh.style.top || 0);\n"
                + "				if (typeof t_sh.style.pixelHeight != tt_u) t_sh.style.pixelHeight = t_h;\n"
                + "				else t_sh.style.height = t_h + \'px\';\n" + "			}\n" + "		}\n" + "\n" + "		tt_GetSelects();\n"
                + "\n" + "		xlim = tt_Int((tt_db && tt_db.clientWidth)? tt_db.clientWidth : window.innerWidth) +\n"
                + "			tt_Int(window.pageXOffset || (tt_db? tt_db.scrollLeft : 0) || 0) -\n" + "			tt_objW -\n"
                + "			(tt_n4? 21 : 0);\n" + "		ylim = tt_Int(window.innerHeight || tt_db.clientHeight) +\n"
                + "			tt_Int(window.pageYOffset || (tt_db? tt_db.scrollTop : 0) || 0) -\n" + "			tt_objH - tt_offY;\n"
                + "\n" + "		tt_SetDivZ();\n" + "		t_e = t_e || window.event;\n"
                + "		if (t_fix) tt_SetDivPos(tt_Int((t_fix = t_fix.split(\',\'))[0]), tt_Int(t_fix[1]));\n"
                + "		else tt_SetDivPos(tt_EvX(t_e), tt_EvY(t_e));\n" + "\n" + "		window.tt_rdl = window.setTimeout(\n"
                + "			\'if (tt_sticky)\'+\n" + "			\'{\'+\n" + "				\'tt_ReleasMov();\'+\n"
                + "				\'window.tt_upFunc = document.onmouseup || null;\'+\n"
                + "				\'if (document.captureEvents) document.captureEvents(Event.MOUSEUP);\'+\n"
                + "				\'document.onmouseup = new Function(\"window.setTimeout(\\'tt_Hide();\\', 10);\");\'+\n"
                + "			\'}\'+\n" + "			\'else if (tt_static) tt_ReleasMov();\'+\n" + "			\'tt_ShowDiv(\\'true\\');\',\n"
                + "			t_delay\n" + "		);\n" + "\n" + "		if (!t_fix)\n" + "		{\n"
                + "			if (document.captureEvents) document.captureEvents(Event.MOUSEMOVE);\n"
                + "			document.onmousemove = tt_Move;\n" + "		}\n" + "	}\n" + "}\n" + "\n" + "\n"
                + "var tt_area = false;\n" + "function tt_Move(t_ev)\n" + "{\n" + "	if (!tt_obj) return;\n"
                + "	if (tt_n6 || tt_w3c)\n" + "	{\n" + "		if (tt_wait) return;\n" + "		tt_wait = true;\n"
                + "		setTimeout(\'tt_wait = false;\', 5);\n" + "	}\n" + "	var t_e = t_ev || window.event;\n"
                + "	tt_SetDivPos(tt_EvX(t_e), tt_EvY(t_e));\n" + "	if (tt_op6)\n" + "	{\n"
                + "		if (tt_area && t_e.target.tagName != \'AREA\') tt_Hide();\n"
                + "		else if (t_e.target.tagName == \'AREA\') tt_area = true;\n" + "	}\n" + "}\n" + "\n" + "\n"
                + "function tt_Hide()\n" + "{\n" + "	if (window.tt_obj)\n" + "	{\n"
                + "		if (window.tt_rdl) window.clearTimeout(tt_rdl);\n" + "		if (!tt_sticky || tt_sticky && !tt_vis)\n"
                + "		{\n" + "			tt_ShowDiv(false);\n" + "			tt_SetDivPos(-tt_objW, -tt_objH);\n"
                + "			tt_obj = null;\n"
                + "			if (typeof window.tt_upFunc != tt_u) document.onmouseup = window.tt_upFunc;\n" + "		}\n"
                + "		tt_sticky = false;\n" + "		if (tt_op6 && tt_area) tt_area = false;\n" + "		tt_ReleasMov();\n"
                + "		tt_HideInput();\n" + "	}\n" + "}\n" + "\n" + "\n" + "tt_Init();\n";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fDir + tooltipFileName));
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing helper file " + fDir + tooltipFileName);
            bw.write(tooltip);
            bw.close();
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Could not write javascript helper file: " + fDir + tooltipFileName + ". Continuing anyway.");
        }
    }

    private static void writeStyleFile(String fDir) {
        String style = "" + "body {\n" + "    font-size: 13px; font-family: luxi sans, arial, helvetica, sans-serif;\n"
                + "}\n" + "table.datapreview {\n" + "    border: 0px;\n" + "    font-size: 9px;\n" + "}\n"
                + "table.map {\n" + "    border: 1px solid #000000;\n" + "}\n" + "td.map {\n"
                + "    border: 1px solid #000000; padding: 2px;\n" + "}\n" + "span.marked {\n"
                + "    background-color: #6cf36c;\n" + "    border: dashed 1px #4baa4b;\n" + "}\n"
                + "span#datum100 {\n" + "    color: #0000ff;\n" + "}\n" + "span#datum100 a {\n"
                + "    color: #0000ff;\n" + "}\n" + "span#datum100 a:visited {\n" + "    color: #ff0000;\n" + "}\n"
                + "span#datum90 {\n" + "    color: #0000c0;\n" + "}\n" + "span#datum90 a {\n" + "    color: #0000c0;\n"
                + "}\n" + "span#datum90 a:visited {\n" + "    color: #c00000;\n" + "}\n" + "span#datum80 {\n"
                + "    color: #0000a0;\n" + "}\n" + "span#datum80 a {\n" + "    color: #0000a0;\n" + "}\n"
                + "span#datum80 a:visited {\n" + "    color: #a00000;\n" + "}\n" + "span#datum70 {\n"
                + "    color: #000090;\n" + "}\n" + "span#datum70 a {\n" + "    color: #000090;\n" + "}\n"
                + "span#datum70 a:visited {\n" + "    color: #900000;\n" + "}\n" + "span#datum60 {\n"
                + "    color: #000080;\n" + "}\n" + "span#datum60 a {\n" + "    color: #000080;\n" + "}\n"
                + "span#datum60 a:visited {\n" + "    color: #800000;\n" + "}\n" + "span#datum50 {\n"
                + "    color: #000060;\n" + "}\n" + "span#datum50 a {\n" + "    color: #000060;\n" + "}\n"
                + "span#datum50 a:visited {\n" + "    color: #600000;\n" + "}\n" + "span#datum40 {\n"
                + "    color: #000050;\n" + "}\n" + "span#datum40 a {\n" + "    color: #000050;\n" + "}\n"
                + "span#datum40 a:visited {\n" + "    color: #500000;\n" + "}\n" + "span#datum30 {\n"
                + "    color: #000040;\n" + "}\n" + "span#datum30 a {\n" + "    color: #000040;\n" + "}\n"
                + "span#datum30 a:visited {\n" + "    color: #400000;\n" + "}\n" + "span#datum20 {\n"
                + "    color: #000030;\n" + "}\n" + "span#datum20 a {\n" + "    color: #000030;\n" + "}\n"
                + "span#datum20 a:visited {\n" + "    color: #300000;\n" + "}\n" + "span#datum10 {\n"
                + "    color: #000020;\n" + "}\n" + "span#datum10 a {\n" + "    color: #000020;\n" + "}\n"
                + "span#datum10 a:visited {\n" + "    color: #200000;\n" + "}\n" + "span#label100 {\n"
                + "    color: #000000;\n" + "}\n" + "span#label90 {\n" + "    color: #151515;\n" + "}\n"
                + "span#label80 {\n" + "    color: #353535;\n" + "}\n" + "span#label70 {\n" + "    color: #474747;\n"
                + "}\n" + "span#label60 {\n" + "    color: #565656;\n" + "}\n" + "span#label50 {\n"
                + "    color: #707070;\n" + "}\n" + "span#label40 {\n" + "    color: #808080;\n" + "}\n"
                + "span#label30 {\n" + "    color: #909090;\n" + "}\n" + "span#label20 {\n" + "    color: #a0a0a0;\n"
                + "}\n" + "span#label10 {\n" + "    color: #b5b5b5;\n" + "}\n" + "table.miniview {\n"
                + "    border: 1px solid #000000; font-size: 7px;\n" + "}\n" + "table.miniview td {\n"
                + "    border: 1px solid #000000; padding-left: 0px; padding-right: 0px;\n" + "}\n"
                + "table.miniview td#full {\n" + "    background-color: #000000;\n" + "}\n"
                + "table.miniview td#expanded {\n" + "    background-color: #999999;\n" + "}\n"
                + "table.miniview td#expanded a {\n" + "    text-decoration: none;\n" + "}\n";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fDir + styleFileName));
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing helper file " + fDir + styleFileName);
            bw.write(style);
            bw.close();
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Could not write CSS file: " + fDir + styleFileName
                            + ". Output might look messy. Continuing anyway.");
        }
    }

}
