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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: MapPreviewServlet.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class MapPreviewServlet extends HttpServlet {
    protected static SOMMap map;

    protected static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        if (map == null) {
            map = SOMMap.getInstance();
        }
        System.out.println("\n\n Preview Servlet: " + StringUtils.printMap(request.getParameterMap()));
        try {
            SOMPageParameters params = new SOMPageParameters(request);
            params.setShowGridDefault(false);
            // params.setShowLabelsDefault(false);
            params.parseRequest(request);

            HTMLMapInformation mapData = map.createFullMap(request, params.vis, params.palette, params.smoothingFactor,
                    params.showNodes, params.showGrid, params.showLabels, params.fodokIds, params.mapWidth,
                    params.mapHeight, params.fullLink);

            // Get the absolute path of the image
            ServletContext sc = getServletContext();
            String filename = sc.getRealPath(mapData.getImagePath());

            // Get the MIME type of the image
            String mimeType = sc.getMimeType(filename);
            if (mimeType == null) {
                sc.log("Could not get MIME type of " + filename);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            // Set content type
            resp.setContentType(mimeType);

            // Set content size
            File file = new File(filename);
            resp.setContentLength((int) file.length());

            // Open the file and output streams
            FileInputStream in = new FileInputStream(file);
            OutputStream out = resp.getOutputStream();

            // Copy the contents of the file to the output stream
            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = in.read(buf)) >= 0) {
                out.write(buf, 0, count);
            }
            in.close();
            out.close();
        } catch (LayerAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
