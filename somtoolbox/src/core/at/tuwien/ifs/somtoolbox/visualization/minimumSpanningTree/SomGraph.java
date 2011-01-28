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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.VisualisationUtils;

/**
 * @author Thomas Kern
 * @author Magdalena Widl
 * @author Rudolf Mayer
 * @version $Id: SomGraph.java 3622 2010-07-07 13:34:42Z mayer $
 */
public class SomGraph extends Graph {
    public enum NeighbourhoodMode {
        All, Diagonal, Direct
    }

    private boolean skipInterpolationUnits;

    private NeighbourhoodMode neighbourhoodMode;

    public SomGraph(GrowingSOM gsom, boolean skipInterpolationUnits, NeighbourhoodMode neighbourhoodMode) {
        super(gsom);
        this.skipInterpolationUnits = skipInterpolationUnits;
        this.neighbourhoodMode = neighbourhoodMode;
    }

    @Override
    protected List<Edge> calculateEdge() {

        Unit[][] units = this.gsom.getLayer().get2DUnits();

        Unit[] allUnits = this.gsom.getLayer().getAllUnits();
        createNodes(allUnits);

        HashMap<Unit, Unit> hm = new HashMap<Unit, Unit>();

        for (int i = 0; i < units.length; i++) {
            for (int j = 0; j < units[i].length; j++) {
                if (!skipInterpolationUnits || units[i][j].getNumberOfMappedInputs() > 0) {
                    try {
                        List<Unit> toConnect;
                        switch (neighbourhoodMode) {
                            case All:
                                toConnect = Arrays.asList(allUnits);
                                break;
                            case Diagonal:
                                toConnect = gsom.getLayer().getNeighbouringUnits(i, j, 1.5);
                                break;
                            case Direct:
                            default:
                                toConnect = gsom.getLayer().getNeighbouringUnits(i, j);
                                break;
                        }
                        for (Unit neighbour : toConnect) {
                            if (!skipInterpolationUnits || neighbour.getNumberOfMappedInputs() > 0) {
                                connectTwoNodes(units[i][j], hm, neighbour);
                            }
                        }
                    } catch (LayerAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return kruskalMST();
    }

    @Override
    protected ArrayList<Unit> getNeighbours(int horIndex, int verIndex, Unit[][] units) {

        ArrayList<Unit> unit = new ArrayList<Unit>();
        if (verIndex > 0) {
            unit.add(units[horIndex][verIndex - 1]);
        }
        if (verIndex + 2 <= units[horIndex].length) {
            unit.add(units[horIndex][verIndex + 1]);
        }

        if (horIndex > 0) {
            unit.add(units[horIndex - 1][verIndex]);
        }
        if (horIndex + 2 <= units.length) {
            unit.add(units[horIndex + 1][verIndex]);
        }

        return unit;
    }

    @Override
    protected void createNodes(Unit[] units) {
        for (Unit anUnit : units) {
            if (!skipInterpolationUnits || anUnit.getNumberOfMappedInputs() > 0) {
                adjList.put(new Node(anUnit.toString(), anUnit.getXPos(), anUnit.getYPos(), anUnit),
                        new LinkedList<Edge>());
            }
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

        // horizontal line
        for (int i = 0; i < Math.abs(n.getXPos() - n1.getXPos()); i++) {
            VisualisationUtils.drawThickLine(g, unitCentre.x, unitCentre.y, unitCentre1.x, unitCentre1.y, lineWidth,
                    lineHeight);
        }

        // vertical line
        for (int i = 0; i < Math.abs(n.getYPos() - n1.getYPos()); i++) {
            VisualisationUtils.drawThickLine(g, unitCentre.x, unitCentre.y, unitCentre1.x, unitCentre1.y, lineWidth,
                    lineHeight);
        }

    }
}
