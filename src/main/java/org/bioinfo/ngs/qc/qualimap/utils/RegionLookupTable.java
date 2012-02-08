package org.bioinfo.ngs.qc.qualimap.utils;

import java.util.*;

import net.sf.picard.util.Interval;
import net.sf.picard.util.IntervalTree;

/**
 * Created by kokonech
 * Date: 2/6/12
 * Time: 3:39 PM
 */
public class RegionLookupTable {

    Map<String, IntervalTree<Integer>> intervalTreeMap;

    public RegionLookupTable() {
        intervalTreeMap = new HashMap<String, IntervalTree<Integer>>();
    }

    public void putRegion(int startPos, int endPos, String seqName) {
        IntervalTree<Integer> tree = intervalTreeMap.get(seqName);
        if (tree == null) {
            tree = new IntervalTree<Integer>();
            intervalTreeMap.put(seqName, tree);
        }
        tree.put(startPos, endPos, 0);
    }

    public void markIntersectingRegions(BitSet windowBitMask, int startPos, int endPos, String seqName) {
        IntervalTree<Integer> tree = intervalTreeMap.get(seqName);
        if (tree == null){
            return;
        }
        Iterator<IntervalTree.Node<Integer>> iter =  tree.overlappers(startPos, endPos);
        while(iter.hasNext()) {
            IntervalTree.Node<Integer> node = iter.next();
            windowBitMask.set(node.getStart(), node.getEnd() + 1);
        }
    }


    public boolean overlaps(int readStart, int readEnd, String seqName) {
        IntervalTree<Integer> tree = intervalTreeMap.get(seqName);
        if (tree == null) {
            return false;
        }
        return tree.overlappers(readStart, readEnd).hasNext();
    }
}
