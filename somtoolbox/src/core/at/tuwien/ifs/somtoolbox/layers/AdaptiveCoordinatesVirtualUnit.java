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
package at.tuwien.ifs.somtoolbox.layers;

/**
 * A virtual unit for the adaptive coordinates visualisation.
 * 
 * @author Rudolf Mayer
 * @version $Id: AdaptiveCoordinatesVirtualUnit.java 3869 2010-10-21 15:56:09Z mayer $
 */
public class AdaptiveCoordinatesVirtualUnit {
    // adaptive coordinates
    private double adaptiveXPos = -1.0;

    private double adaptiveYPos = -1.0;

    private double distanceToWinners = 0.0;

    // coordinates in output space
    private double outputXPos;

    private double outputYPos;

    public AdaptiveCoordinatesVirtualUnit(double xPos, double yPos) {
        this.adaptiveXPos = xPos;
        this.adaptiveYPos = yPos;
        this.outputXPos = xPos;
        this.outputYPos = yPos;
    }

    public AdaptiveCoordinatesVirtualUnit() {
    }

    /**
     * Calculates the new position on the virtual space of this unit with respect to its relative distance.
     * 
     * @param unitDistanceToInputAfterAdaption the new distance to the input vector, after the model vector was adapted
     * @param axWinner ax position of the winner. ax must be > 0
     * @param ayWinner ay position of the winner. ay must be > 0
     */
    public void updateAdaptiveCoordinates(double unitDistanceToInputAfterAdaption, double axWinner, double ayWinner) {
        if (distanceToWinners != 0.0) {
            // Calculate relative distance
            double dDist = (distanceToWinners - unitDistanceToInputAfterAdaption) / distanceToWinners;
            adaptiveXPos = adaptiveXPos + dDist * (axWinner - adaptiveXPos);
            adaptiveYPos = adaptiveYPos + dDist * (ayWinner - adaptiveYPos);
        }
    }

    public void setDistanceToWinner(double distanceToWinners) {
        this.distanceToWinners = distanceToWinners;
    }

    public void setACxPos(double acXPos) {
        this.adaptiveXPos = acXPos;
    }

    public void setACyPos(double acXPos) {
        this.adaptiveYPos = acXPos;
    }

    /** @return the horizontal position of this unit on the virtual space it is part of. */
    public double getAXPos() {
        return adaptiveXPos;
    }

    /** @return the vertical position of this unit on the virtual space it is part of. */
    public double getAYPos() {
        return adaptiveYPos;
    }

    @Override
    public String toString() {
        return coordinatesString() + " (Unit " + unitCoordinatesString() + ")";
    }

    private String unitCoordinatesString() {
        return "[" + outputXPos + "/" + outputYPos + "]";
    }

    public String coordinatesString() {
        return adaptiveXPos + "/" + adaptiveYPos;
    }

}