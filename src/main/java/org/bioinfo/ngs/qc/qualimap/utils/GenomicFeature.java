package org.bioinfo.ngs.qc.qualimap.utils;

import java.util.HashMap;
import java.util.Map;
import net.sf.picard.util.Interval;

/**
 * Created by kokonech
 * Date: 6/8/12
 * Time: 6:00 PM
 */
public class GenomicFeature {

    Interval interval;
    Map<String,String> attributes;

    public GenomicFeature(String seqName, int start, int end, boolean isNegative, String featureName ) {
        interval = new Interval(seqName, start, end, isNegative, featureName);
        attributes = new HashMap<String, String>();
    }

    public int getStart() {
        return interval.getStart();
    }

    public int getEnd() {
        return interval.getEnd();
    }

    public String getSequenceName() {
        return interval.getSequence();
    }


}
