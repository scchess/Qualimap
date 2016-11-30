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

import com.hp.hpl.jena.graph.query.SimpleQueryEngine;
import net.sf.samtools.SAMRecord;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kokonech
 * Date: 5/11/12
 * Time: 2:17 PM
 */
public class BamStatsCollector {


    class ReadAlignmentInfo{
        int firstReadEndPos;
        int secondReadStartPos;
        ReadAlignmentInfo() {
            firstReadEndPos = 0;
            secondReadStartPos = 0;
        }
    }

    long numMappedReads, numPairedReads, numSupplementaryAlignments;
    long numMappedFirstInPair, numMappedSecondInPair, numSingletons;
    long numMarkedDuplicates;
    boolean collectIntersectingReadPairs;
    long numOverlappingReadPairs, numOverlappingBases;
    Map<String,ReadAlignmentInfo> pairsCollector;
    String curChromosome;
    // TODO: read length might influence?
    // int readLength;

    public long getNumMappedReads() {
        return numMappedReads;
    }

    public long getNumSupplementaryAlignments() {
        return numSupplementaryAlignments;
    }

    public long getNumMappedFirstInPair() {
        return numMappedFirstInPair;
    }

    public long getNumMappedSecondInPair() {
        return numMappedSecondInPair;
    }

    public long getNumSingletons() {
        return numSingletons;
    }

    public long getNumPairedReads() {
        return numPairedReads;
    }

    public long getNumOverlappingReadPairs() {
        return numOverlappingReadPairs;
    }

    public long getNumOverlappingBases() {
        return numOverlappingBases;
    }

    public long getNumMarkedDuplicates() {
        return numMarkedDuplicates;
    }


    public BamStatsCollector()
    {
        collectIntersectingReadPairs = false;
    }

    public void enableIntersectingReadsCollection() {
        collectIntersectingReadPairs = true;
        pairsCollector = new HashMap<String,ReadAlignmentInfo>();
        curChromosome = "";

    }


    public boolean updateStats(SAMRecord read) {
        int flagValue = read.getFlags();
        if ((flagValue & Constants.SAM_FLAG_SUPP_ALIGNMENT) > 0) {
             numSupplementaryAlignments++;
        } else {
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
        if (read.getDuplicateReadFlag()) {
            numMarkedDuplicates++;
            return true;
        } else {
            return false;
        }



    }

    public void finalizeAlignmentInfo() {
        if (!curChromosome.isEmpty()) {
            //System.out.println("Collected " + pairsCollector.size() + "  read pairs from chromosome " + curChromosome);
            for (Map.Entry<String, ReadAlignmentInfo> entry : pairsCollector.entrySet()) {
                ReadAlignmentInfo info = entry.getValue();
                if (info.secondReadStartPos > 0) {
                    int intersectionSize = info.firstReadEndPos - info.secondReadStartPos + 1;
                    if (intersectionSize > 0 ) {
                        numOverlappingBases += intersectionSize;
                        numOverlappingReadPairs++;
                    }
                }
            }
            //System.out.println("Number of intersecting pairs " + numOverlappingReadPairs);
        }
    }

    public void collectPairedReadInfo(SAMRecord read) {

        if (!read.getReadPairedFlag() || read.getMateUnmappedFlag()) {
            return;
        }

        String chr = read.getReferenceName();
        if (!curChromosome.equals(chr)) {
            finalizeAlignmentInfo();
            curChromosome = chr;
            pairsCollector.clear();
        }

        //TODO: what if there are several alignments of the same read?

        String readName = read.getReadName();
        if (pairsCollector.containsKey(readName)){
            ReadAlignmentInfo info = pairsCollector.get(readName);
            info.secondReadStartPos = read.getAlignmentStart();
        }  else {
            ReadAlignmentInfo info = new ReadAlignmentInfo();
            info.firstReadEndPos = read.getAlignmentEnd();
            pairsCollector.put(readName, info);
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



}
