/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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

import java.util.*;

import net.sf.picard.util.Interval;
import net.sf.picard.util.OverlapDetector;

/**
 * Created by kokonech
 * Date: 2/6/12
 * Time: 3:39 PM
 */
public class RegionOverlapLookupTable {

    public static class OverlapResult {

        boolean overlaps;
        boolean strandCorrect;

        public OverlapResult(boolean  intervalOverlaps, boolean strandCorrect) {
            this.overlaps = intervalOverlaps;
            this.strandCorrect = strandCorrect;
        }

        public boolean strandMatches() {
            return  strandCorrect;
        }

        public boolean intervalOverlaps() {
            return overlaps;
        }

    }

    static class Feature  {
        String name;
        boolean isPositiveStrand;
        Feature(String name, boolean isPositiveStrand) {
            this.name = name;
            this.isPositiveStrand = isPositiveStrand;
        }
    }


    OverlapDetector<Feature> overlapDetector;
    Set<String> sequenceNames;
    int regionCount;


    public RegionOverlapLookupTable() {
        overlapDetector = new OverlapDetector<Feature>(0,0);
        sequenceNames = new HashSet<String>();
        regionCount = 0;
    }

    public void putRegion(int startPos, int endPos, String seqName, boolean positiveStranded) {
        // unique feature name
        String featureName = "region" + ++regionCount;
        overlapDetector.addLhs( new Feature(featureName, positiveStranded), new Interval(seqName, startPos, endPos));
        sequenceNames.add(seqName);
    }

    public boolean overlaps(int readStart, int readEnd, String seqName) {
        Collection<Feature> overlaps = overlapDetector.getOverlaps(new Interval(seqName, readStart, readEnd));

        return overlaps.size() > 0;
    }

    public OverlapResult overlaps(int readStart, int readEnd, String seqName, boolean forwardStrandExpected) {
        Collection<Feature> overlaps = overlapDetector.getOverlaps(new Interval(seqName, readStart, readEnd));

        Iterator<Feature> it = overlaps.iterator();

        int numStrandMatches = 0;
        int numMatches = 0;

        while (it.hasNext()) {
            numMatches++;
            boolean itemHasForwardStrand = it.next().isPositiveStrand;
            if (itemHasForwardStrand == forwardStrandExpected) {
                ++numStrandMatches;
            }
        }

        return new OverlapResult(numMatches > 0, numStrandMatches == numMatches && numStrandMatches > 0 );

    }

    public Set<String> getSequenceNames() {
        return sequenceNames;
    }

}
