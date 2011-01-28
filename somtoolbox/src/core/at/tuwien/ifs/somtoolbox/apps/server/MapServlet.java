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
package at.tuwien.ifs.somtoolbox.apps.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import cern.colt.Arrays;

import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizerInstance;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;

/**
 * @author Rudolf Mayer
 * @version $Id: MapServlet.java 3589 2010-05-21 10:42:01Z mayer $
 */
public class MapServlet extends HttpServlet {

    protected static SOMMap map;

    protected static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (map == null) {
            map = SOMMap.getInstance();
        }
        System.out.println("\n\n Map Servlet: "
                + at.tuwien.ifs.somtoolbox.util.StringUtils.printMap(request.getParameterMap()));
        response.setBufferSize(128 * 1024);
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("text/html");
        out.println("<link rel=\"StyleSheet\" href=\"style.css\" type=\"text/css\" media=\"all\"/>");

        SOMPageParameters params = new SOMPageParameters(request);
        params.setShowLabelsDefault(true);
        params.parseRequest(request);

        if (params.fodokIds != null) {
            String[] ids = params.fodokIds.split(",");
            System.out.println("processing ids:" + Arrays.toString(ids));
        }

        String basicMoveLink = params.basicLink + "&zoom=" + params.zoom;
        String basicMoveXLink = basicMoveLink + "&moveY=" + params.move.height;
        String basicMoveYLink = basicMoveLink + "&moveX=" + params.move.width;
        String basicZoomLink = params.basicLink + "&moveX=" + params.move.width + "moveY=" + params.move.height
                + "&zoom=";

        try {
            HTMLMapInformation mapData = map.createFullMap(request, params.vis, params.palette, params.smoothingFactor,
                    params.mapWidth, params.mapHeight, params.zoom, params.moveX, params.moveY, params.pointSelection,
                    params.showGrid, params.showNodes, params.showLabels, params.fodokIds, params.fullLink,
                    params.areaSelection);
            out.println("<table border=\"0\">");

            // table layout:
            // | zoom & selection tools & vis selecter
            // | | move up | |
            // | move left | map | move right |
            // | | move down | |

            // first table row: zoom & selection tools
            out.println("<tr>");
            out.println("<td colspan=\"3\" align=\"center\" valign=\"top\">");

            // zoom out button
            if (params.zoom > 1) {
                out.println("<a title=\"zoom out\" href=\""
                        + basicZoomLink
                        + (params.zoom - 1)
                        + "\"><img align=\"top\" border=\"0\" src=\"images/map/zoomOut.gif\" onmouseover=\"this.src='images/map/zoomOutSelected.gif';\" onmouseout=\"this.src='images/map/zoomOut.gif';\" ></a>");
            } else {
                out.println("<img align=\"top\" border=\"0\" src=\"images/map/zoomOutNA.gif\">");
            }

            // direct links to change zoom levels & zoom status
            for (int i = 1; i <= 3; i++) {
                if (params.zoom == i) {
                    out.println("<div valign=\"top\" class=\"mapScaleOn\" style=\"margin: 1px 0pt 3px;\" title=\"Current zoom level: "
                            + i + "\">" + i + "</div>");
                } else {
                    out.println("<a valign=\"top\" class=\"mapScaleOff\" href=\"" + basicZoomLink + i
                            + "\" title=\"Switch to zoom level " + i + "\" style=\"margin: 1px 0pt 3px;\">" + i
                            + "</a>");
                }
            }

            // zoom in button
            if (params.zoom < 3) {
                out.println("<a title=\"zoom in\" href=\""
                        + basicZoomLink
                        + (params.zoom + 1)
                        + "\"><img align=\"top\" border=\"0\" src=\"images/map/zoomIn.gif\" onmouseover=\"this.src='images/map/zoomInSelected.gif';\" onmouseout=\"this.src='images/map/zoomIn.gif';\" ></a>");
            } else {
                out.println("<img align=\"top\" border=\"0\" src=\"images/map/zoomInNA.gif\">");
            }

            // area selection toggle
            out.println("<a href=\"javascript:toggleAreaSelection('yes');\">");
            out.println("  <img align=\"top\" alt=\"Select area of units\" border=\"0\" height=\"22\" src=\"images/map/areaSelection.gif\" title=\"Select area of units\" width=\"43\">");
            out.println("</a>");

            // select between default vis and class
            BackgroundImageVisualizerInstance currentVis = Visualizations.getVisualizationByName(StringUtils.isNotBlank(request.getParameter("visualisation"))
                    ? request.getParameter("visualisation") : params.visDefault);
            // String visControl = map.getVisualisationSelect(params.visDefault, request.getParameter("visualisation"));
            String visControl = ServerVisualizations.getVisualisationsControl(ServerSOM.availableVis, currentVis);
            out.println("<form style=\"display:inline;\">");
            // pass on parameters
            printHiddenFormInput(request, out, "fodokId");
            printHiddenFormInput(request, out, "fodokIds");
            printHiddenFormInput(request, out, "zoom");
            printHiddenFormInput(request, out, "moveX");
            printHiddenFormInput(request, out, "moveY");
            printHiddenFormInput(request, out, "x");
            printHiddenFormInput(request, out, "y");
            out.println(visControl);
            // String vis = request.getParameter("visualisation");
            // if (vis == null) {
            // vis = params.visDefault;
            // }
            // out.println(ServerVisualizations.getVisualisationsControl(vis));

            out.println("<noscript><input style=\"font-size: x-small;\" type=\"submit\" value=\"Update!\"></noscript>");
            out.println("</form>");

            out.println("</td>");

            if (params.vis.equals("Thematic Class Map")) {
                out.println("<td rowspan=\"4\" bordercolor=\"black\" style=\"border: thin\">");
                out.println("<b>Class legend</b>");
                out.println(map.getClassLegend());
                out.println("</td>");
            }

            out.println("</tr>");

            // second row
            out.println("<tr>");
            out.println("<td></td>");

            // navigate up
            out.println("<td align=\"center\">");
            if (params.allowMoveUp) {
                out.println("<a href=\"" + basicMoveYLink + "&moveY=" + (params.move.height - 1)
                        + "\" alt=\"^^\" title=\"Navigate up\">");
                out.println("<img border=\"0\" alt=\"^^\" src=\"images/map/navigateUp.png\" onmouseover=\"this.src='images/map/navigateUpSelected.png';\" onmouseout=\"this.src='images/map/navigateUp.png';\">");
                out.println("</a>");
            } else {
                out.println("<img border=\"0\" alt=\"^^\" src=\"images/map/navigateUpNA.png\" title=\"Already at upper map edge\">");
            }
            out.println("</td>");
            out.println("<td></td>");
            out.println("</tr>");

            // third row
            out.println("<tr>");

            // navigate left
            out.println("<td>");
            if (params.allowMoveLeft) {
                out.println("<a href=\"" + basicMoveXLink + "&moveX=" + (params.move.width - 1)
                        + "\" alt=\"&lt;&lt;\" title=\"Navigate up\">");
                out.println("<img border=\"0\" alt=\"vv\" src=\"images/map/navigateLeft.png\" onmouseover=\"this.src='images/map/navigateLeftSelected.png';\" onmouseout=\"this.src='images/map/navigateLeft.png';\">");
                out.println("</a>");
            } else {
                out.println("<img border=\"0\" alt=\"&lt;&lt;\" src=\"images/map/navigateLeftNA.png\" title=\"Already at left map edge\">");
            }
            out.println("</td>");

            // map
            out.println("<td align=\"center\"><img border=\"0\" src=\"" + mapData.getImagePath()
                    + "\" usemap=\"#som-map\" alt=\"\"/></td>");

            // navigate right
            out.println("<td>");
            if (params.allowMoveRight) {
                out.println("<a href=\"" + basicMoveXLink + "&moveX=" + (params.move.width + 1)
                        + "\" alt=\"&gt;&gt;\" title=\"Navigate right\">");
                out.println("<img border=\"0\" alt=\"&gt;&gt;\" src=\"images/map/navigateRight.png\" onmouseover=\"this.src='images/map/navigateRightSelected.png';\" onmouseout=\"this.src='images/map/navigateRight.png';\">");
                out.println("</a>");
            } else {
                out.println("<img border=\"0\" alt=\"&gt;&gt;\" src=\"images/map/navigateRightNA.png\" title=\"Already at right map edge\">");
            }
            out.println("</td>");
            out.println("</tr>");

            // 4th row
            out.println("<tr>");
            out.println("<td></td>");

            // navigate down
            out.println("<td align=\"center\">");
            if (params.allowMoveDown) {
                out.println("<a href=\"" + basicMoveYLink + "&moveY=" + (params.move.height + 1)
                        + "\" alt=\"vv\" title=\"Navigate down\">");
                out.println("<img border=\"0\" alt=\"vv\" src=\"images/map/navigateDown.png\" onmouseover=\"this.src='images/map/navigateDownSelected.png';\" onmouseout=\"this.src='images/map/navigateDown.png';\">");
                out.println("</a>");
            } else {
                out.println("<img border=\"0\" alt=\"vv\" src=\"images/map/navigateDownNA.png\" title=\"Already at lower map edge\">");
            }
            out.println("</td>");

            out.println("<td></td>");
            out.println("</tr>");
            out.println("</table>");

            // print the image map
            out.println(mapData.getImageMap());

            // print documents on the selected unit
            if (params.pointSelection != null) {
                if (SOMMap.som.growingLayer.isValidUnitLocation(params.pointSelection)) {
                    String[] names = SOMMap.som.growingLayer.getUnit(params.pointSelection.x, params.pointSelection.y).getMappedInputNames();
                    out.println("<b>Selected unit " + params.pointSelection.x + "/" + params.pointSelection.y
                            + "</b><br>");
                    out.println("<b>Representative terms:</b> "
                            + SOMMap.som.growingLayer.getUnit(params.pointSelection.x, params.pointSelection.y).getUnitLabels()
                            + "<br>");
                    out.println("<b>Assigned documents:</b><br>");
                    if (names != null) {
                        for (String name : names) {
                            out.println("<a target=\"_top\" href=\"" + map.documentDetailLink + "?"
                                    + map.documentDetailParamName + "=" + name + "\">" + name + "</a>");
                        }
                    } else {
                        out.println("None<br>");
                    }
                    out.println("<br>");
                } else { // warning for invalid unit selection
                    out.println("<b>Invalid unit selection " + params.pointSelection.x + "/" + params.pointSelection.x
                            + "!<b/><br>");
                }
            }

            // expert mode
            if (request.getParameter("expertMode") != null) {
                out.println("<form>");

                // pass on parameters
                printHiddenFormInput(request, out, "fodokId");
                printHiddenFormInput(request, out, "fodokIds");
                printHiddenFormInput(request, out, "expertMode");
                printHiddenFormInput(request, out, "dataMode");

                // controls for changing palette, visualisation and visualisation specific parameters
                out.println(ServerPalettes.getPaletteControl(params.palette));
                out.println(ServerVisualizations.getVisualisationsControl(params.vis));
                BackgroundImageVisualizer backgroundImageVisualizer = Visualizations.getVisualizationByName(params.vis).getVis();
                out.println(backgroundImageVisualizer.getHTMLVisualisationControl(request.getParameterMap()));

                out.println("<input type=\"submit\" value=\"Update!\">");
                out.println("</form>");
                if (true) {

                }
            }

            // data mode - print all data items in a table
            if (request.getParameter("dataMode") != null) {
                String[] names = SOMMap.som.growingLayer.getAllMappedDataNames();
                out.println("<b>All documents:</b><br>");
                out.println("<span style=\"font-size:xx-small;\">");
                out.println("<table style=\"font-size:xx-small;\">");
                int columnCount = 10;
                if (names[0].length() > 30) {
                    columnCount = 5;
                }
                for (int i = 0; i < names.length; i++) {
                    if (i % columnCount == 0) {
                        out.println("<tr>");
                    }
                    String displayName = names[i];
                    int maxLengt = 40;
                    if (displayName.length() > maxLengt) {
                        displayName = displayName.substring(0, maxLengt - 3) + "...";
                    }
                    out.println("<td style=\"font-size:xx-small; padding-left: 5px; padding-right: 5px;\"><a href=\"map?fodokIds="
                            + names[i]
                            + params.linkParams
                            + "\" title=\""
                            + names[i]
                            + "\">"
                            + displayName
                            + "</a></td>");
                    if (i + 1 % columnCount == 0) {
                        out.println("</tr>");
                    }
                }
                out.println("</table>");
            }
        } catch (LayerAccessException e) {
            e.printStackTrace();
        }

    }

    private void printHiddenFormInput(HttpServletRequest request, ServletOutputStream out, String paramName)
            throws IOException {
        if (request.getParameter(paramName) != null && request.getParameter(paramName).trim().length() > 0) {
            out.print("<input type=\"hidden\" name=\"" + paramName + "\" value=\"" + request.getParameter(paramName)
                    + "\">");
        }
    }
}
