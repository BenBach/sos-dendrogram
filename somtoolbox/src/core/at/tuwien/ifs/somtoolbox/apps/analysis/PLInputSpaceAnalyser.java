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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.stat.StatUtils;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * @author Jakob Frank
 * @version $Id: PLInputSpaceAnalyser.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class PLInputSpaceAnalyser implements PLAnalyser {

    private InputData inputData;

    private PrintStream stats, histogram;

    private boolean initialised = false;

    public PLInputSpaceAnalyser() {
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.apps.analysis.PLAnalyser#init()
     */
    @Override
    public void init(PlaylistAnalysis parent) {
        try {
            stats = new PrintStream(new File(parent.getOutDir(), parent.getOutBasename() + ".in.stats.csv"));
            parent.printHeader(stats);
            stats.println("playlist,length,max,min,mean,median,var,sigma");

            histogram = new PrintStream(new File(parent.getOutDir(), parent.getOutBasename() + ".in.hist.csv"));
            parent.printHeader(histogram);

            inputData = parent.getInputData();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        initialised = true;
    }

    /**
     * @param plName Name of the Playlist
     * @param playList Content of the playlist (entries)
     */
    @Override
    public void analyse(String plName, List<String> playList) {
        if (!initialised) {
            return;
        }

        try {
            L2Metric metric = new L2Metric();
            LinkedList<Double> distList = new LinkedList<Double>();
            for (int i = 0; i < playList.size() - 1; i++) {
                InputDatum item1 = inputData.getInputDatum(playList.get(i));
                InputDatum item2 = inputData.getInputDatum(playList.get(i + 1));
                if (item1 == null || item2 == null) {
                    continue;
                }
                distList.add(metric.distance(item1.getVector(), item2.getVector()));
            }

            double[] dists = new double[distList.size()];
            for (int i = 0; i < dists.length; i++) {
                dists[i] = distList.get(i);
            }

            printHistogram(plName, dists);
            printPLStats(plName, dists);
        } catch (MetricException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void printPLStats(String plName, double[] dists) {
        if (dists.length == 0) {
            return;
        }

        double max = StatUtils.max(dists);
        double min = StatUtils.min(dists);
        double mean = StatUtils.mean(dists);
        double var = StatUtils.variance(dists, mean);

        double[] local = Arrays.copyOf(dists, dists.length);
        Arrays.sort(local);
        double median;
        if (local.length % 2 == 0) {
            median = 0.5 * (local[local.length / 2 - 1] + local[local.length / 2]);
        } else {
            median = local[(local.length / 2)];
        }

        stats.printf("%s,%d,%f,%f,%f,%f,%f,%f%n", plName, dists.length, max, min, mean, median, var, Math.sqrt(var));
        stats.flush();
    }

    /**
     */
    private void printHistogram(String plName, double[] dists) {

        for (double dist : dists) {
            histogram.println(dist);
        }
        histogram.flush();
    }

    @Override
    public void finish() {
        histogram.close();
        stats.close();
    }

}
