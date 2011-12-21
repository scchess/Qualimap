package org.bioinfo.ngs.qc.qualimap.utils;

import net.sf.picard.util.Interval;
import net.sf.picard.util.IntervalTree;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;

import java.util.*;

/**
 * Created by kokonech
 * Date: 12/20/11
 * Time: 12:43 PM
 */
public class GenomicRegionSet {
    IntervalTree<Set<String>> intervalTree;
    MultiMap<String,Interval> featureIntervalMap;
    Set<Interval> ambiguousRegions;

    /* concatenates intersecting intervals */
    private static Interval concatenateIntervals(Interval i1, Interval i2) {
        int start =  Math.min(i1.getStart(),i2.getStart());
        int end = Math.max(i1.getEnd(), i2.getEnd());
        return new Interval(i1.getSequence(), start,end, i1.isPositiveStrand(), i1.getName());
    }

    public GenomicRegionSet() {
        intervalTree =  new IntervalTree<Set<String>>();
        featureIntervalMap = new MultiHashMap<String, Interval>();
        ambiguousRegions = new HashSet<Interval>();
    }

    public boolean intervalIsAmbiguous(int start, int end) {
        for (Interval i : ambiguousRegions) {
            if (i.getStart() == start && i.getEnd() == end) {
                return true;
            }
        }

        return false;
    }

    public void addRegion(GtfParser.Record r) {

        String featureName = r.getGeneId();

        Interval newInterval = new Interval(r.getSeqName(), r.getStart(), r.getEnd(), r.getStrand(), featureName);
        List<Interval> toRemove = new ArrayList<Interval>();

        /*if (intervalIsAmbiguous(newInterval.getStart(), newInterval.getEnd())) {
            System.out.println(featureName);
            return;
        }*/


        /*if (dublicateInterval != null) {
            String iv = dublicateInterval.getValue();
            if ( !iv.equals( featureName ) ) {
                // The interval tree has the following limitation:
                // there can not be 2 regions with same coordinates but different values
                // To work around this limitation we filter such regions at earlier stage of analysis
                ambiguousRegions.add(newInterval);
                // remove ambiguous region
                //intervalTree.remove(r.getStart(), r.getEnd());
                //System.out.println(featureName);
            }
            return;
        }*/


        if (featureIntervalMap.containsKey(featureName)) {
            Collection<Interval> intervals = featureIntervalMap.get(featureName);
            for (Interval interval: intervals ) {
                // TODO: check strand also!
                if (newInterval.intersects(interval)) {
                    if (newInterval.getStart() == interval.getStart() && newInterval.getEnd() == interval.getEnd()) {
                        // interval is already present for this feature
                        return;
                    }
                    newInterval = concatenateIntervals(newInterval, interval);
                    toRemove.add(interval);
                }
            }
            intervals.removeAll(toRemove);
        }

        for (Interval iv : toRemove) {
            intervalTree.remove(iv.getStart(), iv.getEnd());
        }

        IntervalTree.Node<Set<String>> dublicateInterval = intervalTree.find(newInterval.getStart(), newInterval.getEnd());
        if (dublicateInterval != null ) {
            Set<String> intervalFeatures = dublicateInterval.getValue();
            intervalFeatures.add(featureName);
            /*if (!intervalFeatures.contains(featureName)) {
                // Single interval contains a number of features
                intervalFeatures.add(featureName);
            }*/
            return;
        } else {
            Set<String> intervalFeatures = new HashSet<String>();
            intervalFeatures.add(featureName);
            intervalTree.put(newInterval.getStart(), newInterval.getEnd(), intervalFeatures);
            featureIntervalMap.put(r.getGeneId(), newInterval);
        }
    }

    public  Iterator<IntervalTree.Node<Set<String>>> overlappers(int start, int end ) {
        return intervalTree.overlappers(start, end);
    }



}
