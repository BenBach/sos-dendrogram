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
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver.httphandler;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.PocketSOMFormatUtils;

/**
 * The ConfigurationProvider provides sends all PocketSOM-Files via http: (psom, jpeg and list)
 * 
 * @author Jakob Frank
 */
public class PocketSOMConfigProvider implements HttpHandler {
    protected static final String PSOM = ".psom";

    protected static final String IMG = "image.png";

    // protected static final String IMG = "visualisation";

    protected static final String MAPPING = "mapping.list";

    private static final String NL = "\n";

    private static final boolean DEBUG = false;

    private static final String LOG_SEP = " - ";

    private final Logger log;

    private final int connectorEndpointPort;

    private final String musicContext;

    private final CommonSOMViewerStateData state;

    public PocketSOMConfigProvider(CommonSOMViewerStateData state, int port, String mapContentContext) {
        log = Logger.getLogger(this.getClass().getName());
        this.state = state;
        this.musicContext = mapContentContext;
        connectorEndpointPort = port;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (DEBUG) {
            printRequest(t);
        }
        String method = t.getRequestMethod();
        if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD")) {
            String f = t.getRequestURI().getPath();
            if (f.endsWith(PSOM)) {
                sendPsomFile(t);
            } else if (f.endsWith(IMG)) {
                sendVisualisation(t);
            } else if (f.endsWith(MAPPING)) {
                sendMapping(t);
            } else {
                HttpErrorHandler.sendError(t, 404);
            }
        } else {
            HttpErrorHandler.sendError(t, 501);

        }
    }

    private void sendMapping(HttpExchange t) throws IOException {
        log.info("Delivering mapping");
        StringBuilder response = new StringBuilder();
        if (state != null && state.growingLayer != null) {
            response = PocketSOMFormatUtils.createPocketSomMapping(state.growingLayer);
        } else {
            response.append(2).append(NL);
            response.append(2).append(NL);
            response.append(1).append(NL);
            response.append(0 + " " + 0 + " null/null").append(NL);
            response.append(0 + " " + 1 + " null/eins").append(NL);
            response.append(1 + " " + 0 + " eins/null").append(NL);
            response.append(1 + " " + 1 + " eins/eins").append(NL);
        }

        // Send it back
        // FIXME: How should we handle non-ASCII chars?
        String answer = response.toString();
        t.sendResponseHeaders(200, answer.length());
        if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            OutputStream os = t.getResponseBody();
            // PrintStream ps = new PrintStream(os, false, "utf-8");
            // ps.print(answer);
            os.write(answer.getBytes("utf-8"));
            // ps.close();
            // ps.flush();
            os.close();
        }
        t.close();
        log.info("Mapping delivered");
    }

    private void sendVisualisation(HttpExchange t) throws IOException {
        log.info("Delivering Visualisation");
        try {
            int unitW = 50;
            String format = "png";
            if (getClient(t).matches(".*[iI][sS][oO][mM].*")) {
                unitW = 50;
            }

            // Check for size-request
            String query = t.getRequestURI().getQuery();
            if (query != null) {
                String args[] = query.split("&");
                for (String arg : args) {
                    try {
                        String kv[] = arg.split("=", 2);
                        if (kv[0].equalsIgnoreCase("unitSize")) {
                            unitW = Integer.parseInt(kv[1]);
                        } else if (kv[0].equalsIgnoreCase("imageSize")) {
                            if (state == null || state.growingLayer == null) {
                                continue;
                            }
                            int pixels = Integer.parseInt(kv[1]);
                            int units = Math.max(state.growingLayer.getXSize(), state.growingLayer.getYSize());
                            unitW = pixels / units;
                        } else if (kv[0].equalsIgnoreCase("format")) {
                            if (kv[1].equalsIgnoreCase("jpeg") || kv[1].equalsIgnoreCase("png")) {
                                format = kv[1];
                            }
                        }
                    } catch (Exception e) {
                        log.warning("Received invalid image-size argument: \"" + arg + "\"");
                    }
                }
            }

            BufferedImage bi = ExportUtils.getVisualization(state, unitW);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bi, format, bos);

            log.info(200 + LOG_SEP + t.getRequestURI().toString());
            t.getResponseHeaders().add("Content-Type", "image/" + format);
            t.getResponseHeaders().add("Accept-Ranges", "bytes");
            t.sendResponseHeaders(200, bos.size());
            if (t.getRequestMethod().equalsIgnoreCase("GET")) {
                OutputStream os = t.getResponseBody();
                os.write(bos.toByteArray());
                os.close();
            }
            t.close();
        } catch (SOMToolboxException e) {
            e.printStackTrace();
            log.warning("Delivering Visualisation failed!");
            HttpErrorHandler.sendError(t, 500);
        } catch (IOException e) {
            e.printStackTrace();
            log.warning("Delivering Visualisation failed!");
            throw e;
        }

    }

    private String getClient(HttpExchange t) {
        String c = "";

        List<String> cl = t.getRequestHeaders().get("User-agent");
        for (String string : cl) {
            c += string + ", ";
        }
        return c;
    }

    private void sendPsomFile(HttpExchange t) throws IOException {
        log.info("Delivering PSOM-file");
        final String host = t.getRequestHeaders().getFirst("Host");

        String path = t.getRequestURI().getPath();
        path = path.substring(0, path.lastIndexOf('/') + 1);

        StringBuilder response = new StringBuilder();
        response.append("# PocketSOM 0.8 Collection file").append(NL);
        response.append(NL);

        if (host == null) {
            response.append("# All Paths not starting with \"http\" are server-urls").append(NL);
            response.append("relative=1").append(NL);
        }
        response.append(NL);

        response.append("# Collection URL").append(NL);
        response.append("collectionURL=");
        if (host != null) {
            response.append("http://").append(host);
        }
        response.append(musicContext).append('/').append(NL);
        response.append(NL);

        response.append("# image of the map (landscape) for visualization").append(NL);
        response.append("mapimage=");
        if (host != null) {
            response.append("http://").append(host);
        }
        response.append(path).append(IMG).append(NL);
        response.append(NL);

        response.append("# PockeSOM map data (data with locations on the map)").append(NL);
        response.append("mapdata=");
        if (host != null) {
            response.append("http://").append(host);
        }
        response.append(path).append(MAPPING).append(NL);
        response.append(NL);

        response.append("# Advanced configuration").append(NL);
        response.append("playSOM_Address=");
        int posOfClon = host.lastIndexOf(':');
        String address = host;
        if (posOfClon > 0) {
            address = host.substring(0, posOfClon);
        }
        response.append(address).append(':').append(connectorEndpointPort).append(NL);
        response.append(NL);
        response.append(NL);

        // Send it.
        String answer = response.toString();
        log.info(200 + LOG_SEP + t.getRequestURI().toString());
        t.sendResponseHeaders(200, answer.length());
        if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            OutputStream os = t.getResponseBody();
            os.write(answer.getBytes());
            os.close();
        }
        t.close();
    }

    private void printRequest(HttpExchange t) throws IOException {
        System.out.println("Method: " + t.getRequestMethod());
        System.out.println("URI: " + t.getRequestURI());
        System.out.println("Scheme: " + t.getRequestURI().getScheme());
        System.out.println("URI-Port: " + t.getRequestURI().getPort());
        System.out.println("Local: " + t.getLocalAddress().getHostName());
        System.out.println("LocalP: " + t.getLocalAddress().getPort());

        System.out.println("Headers:");
        Headers h = t.getRequestHeaders();
        for (Object name : h.keySet()) {
            String key = (String) name;
            System.out.println("  " + key + ": " + h.getFirst(key));
        }

        System.out.println("Body:");
        InputStream is = t.getRequestBody();
        BufferedReader sr = new BufferedReader(new InputStreamReader(is));
        String line = "";
        while ((line = sr.readLine()) != null) {
            System.out.println("  " + line);
        }

    }

}