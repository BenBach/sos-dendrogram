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
package at.tuwien.ifs.somtoolbox.visualization.thematicmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Taha Abdel Aziz
 * @version $Id: RndIndexGenerator.java 3883 2010-11-02 17:13:23Z frank $
 */
public class RndIndexGenerator {
    double[] scale;

    ArrayList<SOMClass> classes;

    /** Creates a new instance of IdexGenerator */
    public RndIndexGenerator(ArrayList<SOMClass> classes) {
        this.classes = classes;
        Collections.sort(classes, new ClassComperator());
        scale = new double[classes.size() + 1];
        double x = 0;
        for (int i = 1; i <= classes.size(); i++) {
            SOMClass c = classes.get(i - 1);
            scale[i] = x + c.share;
            x = scale[i];
        }
    }

    public SOMClass getNextIndex() {
        double rnd = Math.random();
        for (int i = 1; i <= scale.length; i++) {
            double d0 = scale[i - 1];
            double d1 = scale[i];
            if (rnd > d0 && rnd <= d1) {
                return classes.get(i - 1);
            }
        }
        return null;
    }

    class ClassComperator implements Comparator<SOMClass> {

        @Override
        public int compare(SOMClass c1, SOMClass c2) {
            if (c1.share > c2.share) {
                return 1;
            } else if (c1.share < c2.share) {
                return -1;
            } else {
                return 0;
            }
        }
    }

}
