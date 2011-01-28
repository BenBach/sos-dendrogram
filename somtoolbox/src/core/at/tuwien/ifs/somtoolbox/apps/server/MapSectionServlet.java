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

import at.tuwien.ifs.somtoolbox.data.SOMLibDataInformation;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: MapSectionServlet.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class MapSectionServlet extends HttpServlet {
    protected static SOMMap map;

    protected static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (map == null) {
            map = SOMMap.getInstance();
        }
        System.out.println("\n\n Section Servlet: " + StringUtils.printMap(request.getParameterMap()));
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("text/html");
        out.println("<link rel=\"StyleSheet\" href=\"style.css\" type=\"text/css\" media=\"all\"/>");

        SOMPageParameters params = new SOMPageParameters(request);
        params.setShowLabelsDefault(true);
        params.parseRequest(request);

        try {
            HTMLMapInformation mapData = map.createMapSection(request, params.vis, params.palette,
                    params.smoothingFactor, params.showNodes, params.showGrid, params.fodokId, params.mapWidth,
                    params.mapHeight);
            SOMLibDataInformation dataInfo = SOMMap.som.inputDataObjects.getDataInfo();
            out.println("<img border=\"0\" src=\"" + mapData.getImagePath() + "\" usemap=\"#som-map\" />");
            out.println(mapData.getImageMap() + "<br>");
            String[] relatedDocs = mapData.getNNearest();
            if (relatedDocs != null && relatedDocs.length > 0) {
                out.println("<b>Related documents</b><br>");
                for (String relatedDoc : relatedDocs) {
                    String docDisplayName = relatedDoc;
                    if (dataInfo != null && dataInfo.getDataDisplayName(relatedDoc) != null) {
                        docDisplayName = dataInfo.getDataDisplayName(relatedDoc);
                    }
                    out.println("<a href=\"" + map.documentDetailLink + "?" + map.documentDetailParamName + "="
                            + relatedDoc + "\">" + docDisplayName + "<br>");
                }
            } else {
                out.println("<b>No documents</b><br>");
            }
        } catch (LayerAccessException e) {
            e.printStackTrace();
        }

    }
}
