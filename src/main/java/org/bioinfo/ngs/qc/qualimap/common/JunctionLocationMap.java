/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2016 Garcia-Alcalde et al.
 * http://qualimap.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.bioinfo.ngs.qc.qualimap.common;

import net.sf.picard.util.IntervalTree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kokonech
 * Date: 8/18/14
 * Time: 5:44 PM
 */
public class JunctionLocationMap {

    HashMap<String, HashSet<Integer>> locationMap;
    HashMap<String, IntervalTree<Integer>> locationIntervalTreeMap;

    public JunctionLocationMap() {
        locationMap = new HashMap<String, HashSet<Integer>>();
    }


    public void put(String sequence, int pos1, int pos2) {
        if (!locationMap.containsKey(sequence)) {
            locationMap.put(sequence, new HashSet<Integer>() );
        }

        Set<Integer> positionSet = locationMap.get(sequence);
        positionSet.add(pos1);
        positionSet.add(pos2);

    }


    public void setupIntervalTreeMap() {

        locationIntervalTreeMap = new HashMap<String, IntervalTree<Integer>>();

        for (String key : locationMap.keySet()) {
            IntervalTree<Integer> iTree =  new IntervalTree<Integer>();
            HashSet<Integer> posSet = locationMap.get(key);

            for (Integer pos : posSet) {
                iTree.put(pos - 1, pos + 1, pos);
            }
            locationIntervalTreeMap.put(key, iTree );

        }

    }

    public boolean hasOverlap(String sequence, int junctionPosition) {

        IntervalTree<Integer> iTree = locationIntervalTreeMap.get(sequence);

        return iTree != null && iTree.overlappers(junctionPosition, junctionPosition + 1).hasNext();


    }


    public int size(String seq) {
        return locationMap.get(seq).size();
    }

}
