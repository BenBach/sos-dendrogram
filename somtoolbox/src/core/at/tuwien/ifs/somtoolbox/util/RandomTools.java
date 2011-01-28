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
package at.tuwien.ifs.somtoolbox.util;

import java.util.Random;

/**
 * @author Robert Neumayer
 * @version 17 Jan 2009
 * @deprecated substitute with commons-math.rand*
 **/

@Deprecated
public class RandomTools {

    public static int[] permutation(Random random, int n) {

        assert n > 0;
        // intitial element order is irrelevant so long as each int 1..n occurs exactly once
        // inorder initialization assures that is the case

        int[] sample = new int[n];
        for (int k = 0; k < sample.length; k++) {
            sample[k] = k; // + 1;
        }
        // loop invariant: the tail of the sample array is randomized.
        // Intitally the tail is empty; at each step move a random
        // element from front of array into the tail, then decrement boundary of tail
        int last = sample.length - 1; // last is maximal index of elements not in the tail

        while (last > 0) {
            // Select random index in range 0..last, and swap its contents with those at last
            // The null swap is allowed; it should be possible that sample[k] does not change
            swap(random.nextInt(last + 1), last, sample);
            last -= 1;
        }
        return sample;
    }

    private static void swap(int j, int k, int[] array) {
        int temp = array[k];
        array[k] = array[j];
        array[j] = temp;
    }

}
