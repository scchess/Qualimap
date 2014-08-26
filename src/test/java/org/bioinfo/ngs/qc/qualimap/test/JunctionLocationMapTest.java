/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2014 Garcia-Alcalde et al.
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
