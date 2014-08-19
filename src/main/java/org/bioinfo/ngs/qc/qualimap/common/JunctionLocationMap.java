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
