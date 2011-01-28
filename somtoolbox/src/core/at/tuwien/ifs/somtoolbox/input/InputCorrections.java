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
package at.tuwien.ifs.somtoolbox.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.util.FileUtils;

public class InputCorrections {
    /** Type how input corrections where created. */
    public enum CreationType {
        MANUAL, // for manually drawn corrections
        COMPUTED, // computed corrections
    }

    private ArrayList<InputCorrection> corrections = null;

    public class InputCorrection {
        private Unit sourceUnit = null;

        private Unit targetUnit = null;

        private String label = null;

        private double originalDistance;

        private CreationType creationType;

        public InputCorrection(Unit sourceUnit, Unit targetUnit, String label, double originalDistance,
                CreationType creationType) {
            this.sourceUnit = sourceUnit;
            this.targetUnit = targetUnit;
            this.label = label;
            this.originalDistance = originalDistance;
            this.creationType = creationType;
        }

        public Unit getSourceUnit() {
            return sourceUnit;
        }

        public String getLabel() {
            return label;
        }

        public Unit getTargetUnit() {
            return targetUnit;
        }

        public double getOriginalDistance() {
            return originalDistance;
        }

        public CreationType getCreationType() {
            return creationType;
        }

        @Override
        public String toString() {
            return "Correction '" + label + "': " + sourceUnit + " -> " + targetUnit;
        }

        public String getPrintString() {
            return label + " " + sourceUnit.getXPos() + "/" + sourceUnit.getYPos() + " " + targetUnit.getXPos() + "/"
                    + targetUnit.getYPos();
        }

    }

    public InputCorrections() {
        corrections = new ArrayList<InputCorrection>();
    }

    public InputCorrections(String fileName, Layer layer, InputData data) throws SOMToolboxException {
        this();
        readFromFile(fileName, layer, data);
    }

    public void readFromFile(String fileName, Layer layer, InputData data) throws SOMToolboxException {
        try {
            BufferedReader reader = FileUtils.openFile("Input correction file", fileName);
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(" ");
                String label = parts[0];
                String[] sourceString = parts[1].split("/");
                String[] targetString = parts[2].split("/");

                Unit source = layer.getUnit(Integer.parseInt(sourceString[0]), Integer.parseInt(sourceString[1]));
                Unit target = layer.getUnit(Integer.parseInt(targetString[0]), Integer.parseInt(targetString[1]));
                double newDistance = data != null ? layer.getMetric().distance(target.getWeightVector(),
                        data.getInputDatum(label).getVector()) : 0;

                corrections.add(new InputCorrection(source, target, label, source.getMappedInputDistance(label),
                        CreationType.MANUAL));
                source.removeMappedInput(label);
                target.addMappedInput(label, newDistance, true);
            }
        } catch (IOException e) {
            throw new SOMToolboxException("Error loading input corrections file: " + e.getMessage());
        }
    }

    public int size() {
        return corrections.size();
    }

    public InputCorrection get(int index) {
        return corrections.get(index);
    }

    public InputCorrection get(String label) {
        for (int i = 0; i < corrections.size(); i++) {
            if (label.equals(corrections.get(i).getLabel())) {
                return corrections.get(i);
            }
        }
        return null;
    }

    private int getIndex(String label) {
        for (int i = 0; i < corrections.size(); i++) {
            if (label.equals(corrections.get(i).getLabel())) {
                return i;
            }
        }
        return -1;
    }

    private void remove(String label) {
        corrections.remove(getIndex(label));
    }

    public ArrayList<InputCorrection> getInputCorrections() {
        return corrections;
    }

    public InputCorrection addComputedInputCorrection(Unit source, Unit target, String label,
            InputCorrections manualCorrections) throws SOMToolboxException {
        double originalDistance = manualCorrections.get(label) != null
                ? manualCorrections.get(label).getOriginalDistance() : source.getMappedInputDistance(label);
        InputCorrection correction = new InputCorrection(source, target, label, originalDistance, CreationType.COMPUTED);
        corrections.add(correction);
        return correction;
    }

    public InputCorrection addManualInputCorrection(Unit source, Unit target, String label) throws SOMToolboxException {
        // check if a correction already exists for this input
        InputCorrection correction = get(label);
        if (correction != null) {
            // if we moved an input back to the original unit, remove that correction
            if (correction.sourceUnit.equals(target)) {
                remove(label);
            }
            correction.targetUnit = target; // and update target unit
        } else { // otherwise add new
            correction = new InputCorrection(source, target, label, source.getMappedInputDistance(label),
                    CreationType.MANUAL);
            corrections.add(correction);
        }
        return correction;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Input corrections:\n");
        for (InputCorrection correction : corrections) {
            sb.append(correction).append("\n");
        }
        return sb.toString();
    }

    public void writeToFile(File outputFile) throws SOMToolboxException {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(outputFile));
            out.println("# Input corrections file, stored on " + new Date());
            out.println("# File format: <label> <sourceX/sourceY> <targetX/targetY>");
            for (InputCorrection correction : corrections) {
                out.println(correction.getPrintString());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new SOMToolboxException("Error writing input corrections: " + e.getMessage(), e);
        }
    }

}
