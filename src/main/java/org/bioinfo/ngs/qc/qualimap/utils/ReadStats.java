package org.bioinfo.ngs.qc.qualimap.utils;

import net.sf.samtools.SAMRecord;

/**
 * Created by kokonech
 * Date: 5/11/12
 * Time: 2:17 PM
 */
public class ReadStats {

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

    public ReadStats() {}

    public void collectReadStats(SAMRecord read) {
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
