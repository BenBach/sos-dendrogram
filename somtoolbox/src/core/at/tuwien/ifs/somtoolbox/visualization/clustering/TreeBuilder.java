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
package at.tuwien.ifs.somtoolbox.visualization.clustering;

import javax.swing.ProgressMonitor;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.util.AdaptiveStdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * Superclass providing aborting and status monitoring functionality for the clustering algorithms.
 * 
 * @author Angela Roiger
 * @version $Id: TreeBuilder.java 3358 2010-02-11 14:35:07Z mayer $
 */
public abstract class TreeBuilder {
    protected int level;

    protected ProgressMonitor monitor; // use this to show the status

    protected StdErrProgressWriter progressWriter;

    protected int progress = 0; // how far are we?

    public abstract ClusteringTree createTree(GeneralUnitPNode[][] units) throws ClusteringAbortedException;

    public void setMonitor(ProgressMonitor monitor) {
        this.monitor = monitor;
    }

    protected void resetMonitor(int maximum) {
        progress = 0;
        if (monitor != null) {
            monitor.setMaximum(maximum);
        }
        progressWriter = new AdaptiveStdErrProgressWriter(maximum, "Calculating clusters: ");
    }

    protected void incrementMonitor() {
        if (monitor != null) {
            monitor.setProgress(++progress);
        }
        progressWriter.progress();
    }

    protected void finishMonitor() {
        if (monitor != null) {
            monitor.close();
        }
        progressWriter.progress(progressWriter.getSteps());
    }

    protected void allowAborting() throws ClusteringAbortedException {
        if (monitor != null && monitor.isCanceled()) {
            throw new ClusteringAbortedException();
        }
    }

    public abstract String getClusteringAlgName();

}
