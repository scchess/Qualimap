/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2012 Garcia-Alcalde et al.
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

import net.sf.samtools.SAMRecord;

/**
 * Created by kokonech
 * Date: 5/11/12
 * Time: 2:17 PM
 */
public class BamStatsCollector {

    int numMappedReads, numPairedReads;
    int numMappedFirstInPair, numMappedSecondInPair, numSingletons;

    public int getNumMappedReads() {
        return numMappedReads;
    }

    public int getNumMappedFirstInPair() {
        return numMappedFirstInPair;
    }

    public int getNumMappedSecondInPair() {
        return numMappedSecondInPair;
    }

    public int getNumSingletons() {
        return numSingletons;
    }

    public BamStatsCollector() {}

    public void updateStats(SAMRecord read) {
        numMappedReads++;
        if (read.getReadPairedFlag()) {
            numPairedReads++;
            if (read.getFirstOfPairFlag()) {
                numMappedFirstInPair++;
            } else if (read.getSecondOfPairFlag()) {
                numMappedSecondInPair++;
            }
            if (read.getMateUnmappedFlag()) {
                numSingletons++;
            }
        }

    }

    public String report() {
        StringBuilder buf = new StringBuilder();
        buf.append("Num mapped reads: ").append(numMappedReads).append("\n");
        buf.append("Num mapped first of pair: ").append(numMappedFirstInPair).append("\n");
        buf.append("Num mapped second of pair: ").append(numMappedSecondInPair).append("\n");
        buf.append("Num singletons: ").append(numSingletons).append("\n");
        return buf.toString();
    }


    public int getNumPairedReads() {
        return numPairedReads;
    }
}
