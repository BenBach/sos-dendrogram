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
package at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.VisualisationUtils;

/**
 * @author Thomas Kern
 * @author Magdalena Widl
 * @author Rudolf Mayer
 * @version $Id: InputdataGraph.java 3622 2010-07-07 13:34:42Z mayer $
 */
public class InputdataGraph extends Graph {
    InputData data;

    public InputdataGraph(GrowingSOM gsom) {
        super(gsom);
        this.data = gsom.getSharedInputObjects().getInputData();
    }

    @Override
    protected List<Edge> calculateEdge() {

        Unit[] ret = new Unit[data.numVectors()];

        int counter = 0;

        for (String dataName : data.getLabels()) {
            Unit other = gsom.getLayer().getUnitForDatum(dataName);
            ret[counter] = new Unit(gsom.getLayer(), other.getXPos(), other.getYPos(), data.getData()[counter]);

            counter++;
        }

        return connect_neighbours(ret);
    }

    @Override
    protected ArrayList<Unit> getNeighbours(int horIndex, int verIndex, Unit[][] units) {
        return null;
    }

    @Override
    protected void createNodes(Unit[] units) {
        for (Unit anUnit : units) {
            adjList.put(new Node(anUnit.toString(), anUnit.getXPos(), anUnit.getYPos(), anUnit), new LinkedList<Edge>());
        }
    }

    @Override
    public void drawLine(Graphics2D g, int unitWidth, int unitHeight, Edge e, boolean weighting) {
        Unit n = e.getStart().getUnit();
        Unit n1 = e.getEnd().getUnit();
        Point unitCentre = VisualisationUtils.getUnitCentreLocation(n, unitWidth, unitHeight);
        Point unitCentre1 = VisualisationUtils.getUnitCentreLocation(n1, unitWidth, unitHeight);

        int[] lineThickness = computeLineThickness(e, unitWidth, unitHeight, weighting);
        int lineWidth = lineThickness[0];
        int lineHeight = lineThickness[1];

        VisualisationUtils.drawThickLine(g, unitCentre.x, unitCentre.y, unitCentre1.x, unitCentre1.y, lineWidth,
                lineHeight);
    }

    private List<Edge> connect_neighbours(Unit[] units) {
        createNodes(units);
        HashMap<Unit, Unit> hm = new HashMap<Unit, Unit>();

        for (Unit mainunit : units) {

            // attaches each unit to mainunit
            for (Unit neighbour : units) {
                connectTwoNodes(mainunit, hm, neighbour);
            }
        }

        return kruskalMST();
    }
}
