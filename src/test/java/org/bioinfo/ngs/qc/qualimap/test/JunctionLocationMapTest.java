package org.bioinfo.ngs.qc.qualimap.test;

import org.bioinfo.ngs.qc.qualimap.common.JunctionLocationMap;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by kokonech
 * Date: 8/19/14
 * Time: 10:31 AM
 */
public class JunctionLocationMapTest {

    @Test
    public void simpleTests() {

        JunctionLocationMap locationMap = new JunctionLocationMap();

        locationMap.put("chr1", 25, 30);
        locationMap.put("chr1", 25, 80);
        locationMap.put("chr1", 100, 200);
        assert(locationMap.size("chr1") == 4);


        locationMap.setupIntervalTreeMap();

        assertTrue(locationMap.hasOverlap("chr1", 26));
        assertTrue(locationMap.hasOverlap("chr1", 79));
        assertFalse(locationMap.hasOverlap("chr1", 780));






    }



}
