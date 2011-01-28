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
package at.tuwien.ifs.somtoolbox.layers.quality;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.mutable.MutableDouble;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterNode;

public class EntropyAndPurityCalculator {

    private double entropy;

    private double purity;

    // FIXME implement normalised mutual information
    // http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
    public EntropyAndPurityCalculator(List<ClusterNode> clusters, SOMLibClassInformation classInfo) {
        // for all clusters
        int clusterCounter = 0;
        double[] halfWeightedEntropyValues = new double[clusters.size()];
        double[] halfWeightedPurityValues = new double[clusters.size()];
        int vectorCounter = 0;
        for (ClusterNode clusterNode : clusters) {
            int numberOfInputs = clusterNode.getNumberOfInputs();

            vectorCounter += numberOfInputs;
            LabelledCounterMapToBePutInCOMMONS lcm = new LabelledCounterMapToBePutInCOMMONS(classInfo.classNames());
            // for all nodes in cluster
            GeneralUnitPNode[] nodes = clusterNode.getNodes();
            for (GeneralUnitPNode node : nodes) {
                String inputNames[] = node.getUnit().getMappedInputNames();
                if (inputNames != null) {
                    // for all inputs mapped onto node
                    for (String inputName : inputNames) {
                        String className = null;
                        try {
                            className = classInfo.getClassName(inputName);
                        } catch (SOMLibFileFormatException e) {
                            e.printStackTrace();
                        }
                        lcm.increment(className);
                    }
                }
            }

            // System.out.println("In cluster " + clusterCounter + ": " + lcm.getCounter());
            double entropySum = 0;
            double purity = Double.MIN_VALUE;
            for (String key : lcm.keySet()) {
                // System.out.println("\t" + key + ": " + lcm.get(key) + " / " + lcm.getCounter());
                double classProbability = lcm.get(key).doubleValue() / lcm.getCounter();
                double entropy = entropy(classProbability);
                // System.out.println("\tent: " + entropy);
                entropySum += entropy;
                if (classProbability > purity) {
                    purity = classProbability;
                }

            }
            // System.out.println("Cluster entropy: " + entropySum);
            // System.out.println("Cluster purity: " + purity);

            double halfWeightedEntropy = entropySum * lcm.getCounter();
            double halfWeightedPurity = purity * lcm.getCounter();
            // System.out.println("Weighted cluster entropy (" + lcm.getCounter() + "): " + halfWeightedEntropy);
            // System.out.println("Weighted cluster purity (" + lcm.getCounter() + "): " + halfWeightedPurity);
            // counting clusters
            halfWeightedEntropyValues[clusterCounter] = halfWeightedEntropy;
            halfWeightedPurityValues[clusterCounter] = halfWeightedPurity;
            clusterCounter++;
        }

        // finally, we build the some over the normalised values (that is divided by the toal number of vectors)
        double finalEntValue = 0d;
        double finalPurityValue = 0d;
        for (int i = 0; i < clusterCounter; i++) {
            double normalisedEntropy = halfWeightedEntropyValues[i] / vectorCounter;
            double normalisedPurity = halfWeightedPurityValues[i] / vectorCounter;
            // System.out.println("norm: " + normalisedEntropy);
            finalEntValue += normalisedEntropy;
            finalPurityValue += normalisedPurity;
        }
        // System.out.println("sum over entropy norms: " + finalEntValue);
        // System.out.println("sum over purity norms: " + finalPurityValue);

        entropy = finalEntValue;
        purity = finalPurityValue;
    }

    // FIXME see to it that these are used from cm imports
    // FIXME see to it that these are used from cm imports
    // FIXME see to it that these are used from cm imports
    public static double entropy(double value) {
        double val = -getLog2(value) * value;
        val = !Double.isInfinite(val) ? val : 0d;
        return !Double.isNaN(val) ? val : 0d;
    }

    // FIXME see to it that these are used from cm imports
    // FIXME see to it that these are used from cm imports
    // FIXME see to it that these are used from cm imports
    public static double getLog2(double value) {
        return Math.log(value) / Math.log(2.0);
    }

    public double getEntropy() {
        return entropy;
    }

    public double getPurity() {
        return purity;
    }

    public class LabelledCounterMapToBePutInCOMMONS {

        // FIXME use generics for aa vaere alfa
        private HashMap<String, MutableDouble> map;

        private double counter;

        /** here we count the sum of all entries */
        public double getCounter() {
            return counter;
        }

        public LabelledCounterMapToBePutInCOMMONS() {
            map = new HashMap<String, MutableDouble>();
            counter = 0;
        }

        public LabelledCounterMapToBePutInCOMMONS(String[] labels) {
            this();
            for (String label : labels) {
                map.put(label, new MutableDouble(0));
            }
        }

        public int size() {
            return map.size();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < map.keySet().size(); i++) {
                sb.append(map.keySet().toArray()[i] + " " + map.values().toArray()[i] + "\n");
            }
            return sb.toString();
        }

        /** increment the counter for the given key */
        public void increment(String key) {
            this.map.get(key).increment();
            counter++;
        }

        /** increment the counter for the given key this one checks for existence */
        public void incrementOrAdd(String key) {
            MutableDouble v = this.map.get(key);
            if (v == null) {
                this.map.put(key, new MutableDouble(1));
            } else {
                increment(key);
            }
            counter++;
        }

        public Collection<MutableDouble> entrySet() {
            return this.map.values();
        }

        public Collection<String> keySet() {
            return this.map.keySet();
        }

        public MutableDouble get(String key) {
            return this.map.get(key);
        }

    }

}
