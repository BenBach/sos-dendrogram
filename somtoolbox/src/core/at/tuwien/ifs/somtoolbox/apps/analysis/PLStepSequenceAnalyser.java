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
package at.tuwien.ifs.somtoolbox.apps.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

public class PLStepSequenceAnalyser implements PLAnalyser {

    PrintStream inS, outS;

    private GrowingLayer som;

    private boolean initialised;

    private InputData inputData;

    @Override
    public void analyse(String plName, List<String> playList) {
        if (!initialised) {
            return;
        }

        outS.print(plName);
        inS.print(plName);

        L2Metric metric = new L2Metric();
        for (int i = 0; i < playList.size() - 1; i++) {

            Unit u1 = som.getUnitForDatum(playList.get(i));
            Unit u2 = som.getUnitForDatum(playList.get(i + 1));
            if (u1 == null || u2 == null) {
                outS.print(",Unknown");
            } else {
                outS.format(",%.3f", som.getMapDistance(u1, u2));
            }

            InputDatum item1 = inputData.getInputDatum(playList.get(i));
            InputDatum item2 = inputData.getInputDatum(playList.get(i + 1));
            if (item1 == null || item2 == null) {
                inS.print(",Unknown");
            } else {
                try {
                    inS.format(",%.3f", metric.distance(item1.getVector(), item2.getVector()));
                } catch (MetricException e) {
                    inS.print(",NaN");
                }
            }

        }
        outS.println();
        inS.println();
    }

    @Override
    public void finish() {
        if (!initialised) {
            return;
        }
        inS.close();
        outS.close();
        initialised = false;
    }

    @Override
    public void init(PlaylistAnalysis parent) {
        try {
            inS = new PrintStream(new File(parent.getOutDir(), parent.getOutBasename() + ".in.step.csv"));
            parent.printHeader(inS);
            inS.println("playlist,1-2,2-3,3-4,usw...");

            outS = new PrintStream(new File(parent.getOutDir(), parent.getOutBasename() + ".out.step.csv"));
            parent.printHeader(outS);
            outS.println("playlist,1-2,2-3,3-4,usw...");

            som = parent.getSom().getLayer();
            inputData = parent.getInputData();
            initialised = true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
