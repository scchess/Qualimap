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
package org.bioinfo.ngs.qc.qualimap.process;

import net.sf.samtools.*;
import org.bioinfo.ngs.qc.qualimap.beans.BamGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats;
import org.bioinfo.ngs.qc.qualimap.beans.SingleReadData;
import org.bioinfo.ngs.qc.qualimap.common.Constants;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by kokonech
 * Date: 11/15/11
 * Time: 5:04 PM
 */
public class ProcessBunchOfReadsTask implements Callable<ProcessBunchOfReadsTask.Result> {
    List<SAMRecord> reads;
    BamStatsAnalysis ctx;
    BamGenomeWindow currentWindow;
    boolean computeInsertSize;
    boolean isPairedData;
    boolean analyzeRegions, computeOutsideStats;
    private static final Object lock = new Object();
    HashMap<Long, SingleReadData> analysisResults;
    HashMap<Long, SingleReadData> outOfRegionsResults;
    ArrayList<Float> readsGcContent;
    ReadStatsCollector readStatsCollector;
    ArrayList<Float> outOfRegionsReadsGCContent;
    ReadStatsCollector outOfRegionsReadStatsCollector;

    static final char CIGAR_M = CigarOperator.MATCH_OR_MISMATCH.name().charAt(0);
    static final char CIGAR_EQ = CigarOperator.EQ.name().charAt(0);
    //TODO: use variables instead of magic constants in computeAlignment()
    //static final char CIGAR_X = CigarOperator.X.name().charAt(0);

    public static class Result {
        Collection<SingleReadData> readsData;
        Collection<SingleReadData> outRegionReadsData;
        ReadStatsCollector readStatsCollector;
        ReadStatsCollector outRegionReadStatsCollector;

        public ReadStatsCollector getReadStatsCollector() {
            return readStatsCollector;
        }

        public ReadStatsCollector getOutRegionReadStatsCollector() {
            return outRegionReadStatsCollector;
        }

        public void setReadStatsCollector(ReadStatsCollector readStatsCollector) {
            this.readStatsCollector = readStatsCollector;
        }

        public void setOutRegionReadStatsCollector(ReadStatsCollector readsStatsCollector) {
            this.outRegionReadStatsCollector = readsStatsCollector;
        }

        public void setGlobalReadsData(Collection<SingleReadData> readsData) {
            this.readsData = readsData;
        }

        public Collection<SingleReadData> getReadAlignmentData() {
            return readsData;
        }

        public void setOutOfRegionReadsData(Collection<SingleReadData> inRegionReadsData) {
            this.outRegionReadsData = inRegionReadsData;
        }

        public Collection<SingleReadData> getOutOfRegionReadsData() {
            return outRegionReadsData;
        }

    }

    public ProcessBunchOfReadsTask(List<SAMRecord> reads, BamGenomeWindow window, BamStatsAnalysis ctx)  {
        this.reads = reads;
        this.ctx = ctx;
        this.analyzeRegions = ctx.selectedRegionsAvailable();
        computeInsertSize = true;
        isPairedData = true;
        currentWindow = window;
        analysisResults = new HashMap<Long, SingleReadData>();
        computeOutsideStats = ctx.getComputeOutsideStats();
        readsGcContent = new ArrayList<Float>();
        readStatsCollector = new ReadStatsCollector(ctx.getMinHomopolymerSize());
        if ( analyzeRegions && computeOutsideStats ) {
            outOfRegionsResults = new HashMap<Long, SingleReadData>();
            outOfRegionsReadsGCContent = new ArrayList<Float>();
            outOfRegionsReadStatsCollector = new ReadStatsCollector(ctx.getMinHomopolymerSize());
        }

    }


    static private SingleReadData getWindowData(Long windowStart, HashMap<Long,SingleReadData> resultsMap) {
        if (resultsMap.containsKey(windowStart)) {
            return resultsMap.get(windowStart);
        } else {
            SingleReadData data = new SingleReadData(windowStart);
            resultsMap.put(windowStart, data);
            return data;
        }
    }


    ReadStatsCollector getReadStatsCollector(SAMRecord read) {
        if (read.getAttribute(Constants.READ_IN_REGION).equals(1) ) {
           return  readStatsCollector;
        } else if (analyzeRegions && computeOutsideStats) {
            return outOfRegionsReadStatsCollector;
        }

        return null;
    }


    public Result call() {

        //long startTime = System.currentTimeMillis();
        //System.out.println("Started bunch of read analysis. The first read is" + reads.get(0).getReadName());

        Result taskResult = new Result();

        for (SAMRecord read : reads) {

            long position = ctx.getLocator().getAbsoluteCoordinates(read.getReferenceName(), read.getAlignmentStart());

            char[] alignment = null;
            // compute alignment
            try {
                ReadStatsCollector statsCollector = getReadStatsCollector(read);
                if (statsCollector != null) {
                    // compute alignment and collect read stats
                    alignment = computeReadAlignment(read, statsCollector);
                } else {
                    // only compute alignment
                    alignment = computeReadAlignment(read);
                }
            } catch (SAMFormatException e) {
                System.err.println("Problem analyzing the read: " + read.getReadName());
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

            if (alignment == null ) {
                continue;
            }

            //System.out.println("From ProcessReadTask: computed alignment for read" + read.getHeader().toString());

            // insert size

            try {
                if(computeInsertSize && read.getProperPairFlag()){
                    long insertSize = read.getInferredInsertSize();
                    currentWindow.acumInsertSize(insertSize);
                }
            } catch(IllegalStateException ise){
                isPairedData = false;
            }

            int mappingQuality = read.getMappingQuality();
            long readEnd = position + alignment.length - 1;

            // acum read

            //regionOverlapLookupTable = createRegionLookupTable(position, readEnd, ctx.getRegionsTree());
            boolean outOfBounds = processReadAlignment(currentWindow, alignment, position, readEnd,
                    mappingQuality);

            if(outOfBounds) {
                //System.out.println("From ProcessReadTask: propogating read" + read.getHeader().toString());
                propagateRead(alignment, position, readEnd, mappingQuality);
            }

        }

        readStatsCollector.saveGC();
        taskResult.setGlobalReadsData(analysisResults.values());
        taskResult.setReadStatsCollector(readStatsCollector);

        if (analyzeRegions && computeOutsideStats) {
            outOfRegionsReadStatsCollector.saveGC();
            taskResult.setOutOfRegionReadsData(outOfRegionsResults.values());
            taskResult.setOutRegionReadStatsCollector(outOfRegionsReadStatsCollector);
        }


        //long endTime = System.currentTimeMillis();
        //System.out.println("Analyze bunch of reads time: " + (endTime - startTime));

        return taskResult;
    }

    static private int computeNumMismatches(SAMRecord read) {

        int numMismatches = 0;
        String mdAttr = read.getStringAttribute("MD");

        if (mdAttr == null) {
            return numMismatches;
        }

        for (int i = 0; i < mdAttr.length(); i++){
            char c = mdAttr.charAt(i);

            if (c == 'A' || c== 'C' || c == 'G' || c == 'T') {
                numMismatches += 1;
            }
        }

        return numMismatches;
    }

    /**
     * This method computes read to reference alignment vector along with collecting read's stats
     * @param read The record being analyzed
     * @param statsCollector Read stats collector
     * @return Alignment vector, which includes extended issues
     */

    public static char[] computeReadAlignment(SAMRecord read, ReadStatsCollector statsCollector){
		// init read params
		int alignmentLength = (read.getAlignmentEnd()-read.getAlignmentStart()+1);

        //TODO: analyze alignment when read length is zero
        int readLength = read.getReadLength();

        if (alignmentLength < 0 || readLength == 0) {
            return null;
        }

        //System.err.println("Computing alignment for read " + read.getReadName());
		Cigar cigar = read.getCigar();

		// precompute total size of alignment
		int totalSize = 0;
        List<CigarElement> elementList = cigar.getCigarElements();
        //int numCigarElements = cigar.numCigarElements();
        boolean readIsClipped = false;
        boolean readHasDeletions = false;
        boolean readHasInsertions = false;
		for(CigarElement element : elementList){
			totalSize += element.getLength();
            if (element.getOperator() == CigarOperator.H || element.getOperator() == CigarOperator.S) {
                readIsClipped = true;
            } else if (element.getOperator() == CigarOperator.I) {
                statsCollector.incNumInsertions();
                if (!readHasInsertions) {
                    statsCollector.incNumReadsWithInsertion();
                }
                readHasInsertions = true;

            } else if (element.getOperator() == CigarOperator.D) {
                statsCollector.incNumDeletions();
                if (!readHasDeletions) {
                    statsCollector.incNumReadsWithDeletion();
                }
                readHasDeletions = true;
            }
		}

		// compute extended cigar
		char[] extendedCigarVector = new char[totalSize];
		int mpos = 0;
		int npos;
        for(CigarElement element : elementList){
		    npos = mpos + element.getLength();
			Arrays.fill(extendedCigarVector, mpos, npos, element.getOperator().name().charAt(0));
			mpos = npos;
		}

		char[] alignmentVector = new char[alignmentLength];

		int readPos = 0;
		int alignmentPos = 0;
		byte[] readBases = read.getReadBases();
        statsCollector.resetCounters();

		for( int pos = 0; pos < extendedCigarVector.length; ++pos){
            char cigarChar = extendedCigarVector[pos];
			// M
			if(cigarChar == CIGAR_M || cigarChar == CIGAR_EQ){
				// get base
                byte base = readBases[readPos];
                statsCollector.collectBase(readPos, base, false);
                readPos++;
				alignmentVector[alignmentPos] = (char) base;
				alignmentPos++;
			}
			// I
			else if(cigarChar == 'I'){
                byte base = readBases[readPos];
                statsCollector.collectBase(readPos, base, true);
                readPos++;
            }
			// D
			else if(cigarChar == 'D'){
                int nextCigarPos = pos + 1;
                if (nextCigarPos < extendedCigarVector.length && extendedCigarVector[nextCigarPos] != 'D' ) {
                    byte nextBase = readPos + 1 < readBases.length ? readBases[readPos + 1] : -1;
                    statsCollector.collectDeletedBase(nextBase);
                }
                alignmentVector[alignmentPos] = '-';
				alignmentPos++;
			}
			// N
			else if(cigarChar == 'N'){
				alignmentVector[alignmentPos] = 'N';
				alignmentPos++;
			}
			// S
			else if(cigarChar == 'S'){
                statsCollector.incClippingContent(readPos);
            	readPos++;
			}
			// H
			else if(cigarChar =='H'){
                statsCollector.incClippingContent(readPos);
            }
			// P
			else if(cigarChar == 'P'){
				alignmentVector[alignmentPos] = '-';
				alignmentPos++;
			}
		}

        if (readIsClipped) {
            statsCollector.incNumClippedReads();
        }

        int numMismatches = computeNumMismatches(read);
        statsCollector.incNumMismatches(numMismatches);

        Integer editDist = read.getIntegerAttribute("NM");
        if (editDist != null) {
            statsCollector.incEditDistance(editDist);
        }




		return alignmentVector;
	}


     public static char[] computeReadAlignment(SAMRecord read){
		// init read params
		int alignmentLength = (read.getAlignmentEnd()-read.getAlignmentStart()+1);

        if (alignmentLength < 0) {
            return null;
        }


		Cigar cigar = read.getCigar();

		// precompute total size of alignment
		int totalSize = 0;
        List<CigarElement> elementList = cigar.getCigarElements();
        //int numCigarElements = cigar.numCigarElements();
		for(CigarElement element : elementList){
			totalSize += element.getLength();
		}

		// compute extended cigar
		char[] extendedCigarVector = new char[totalSize];
		int mpos = 0;
		int npos;
        for(CigarElement element : elementList){
		    npos = mpos + element.getLength();
			Arrays.fill(extendedCigarVector, mpos, npos, element.getOperator().name().charAt(0));
			mpos = npos;
		}

		// init extended cigar portion
		//char[] extendedCigarVector = extended; // Arrays.copyOfRange(extended,0,mpos);

		char[] alignmentVector = new char[alignmentLength];

		int readPos = 0;
		int alignmentPos = 0;
		byte[] readBases = read.getReadBases();

		for(char cigarChar : extendedCigarVector){
			// M
			if(cigarChar == CIGAR_M || cigarChar == CIGAR_EQ){
				// get base
				byte base = readBases[readPos];
                readPos++;
				// set base
				alignmentVector[alignmentPos] = (char) base;
				alignmentPos++;
			}
			// I
			else if(cigarChar == 'I'){
			    readPos++;
			}
			// D
			else if(cigarChar == 'D'){
				alignmentVector[alignmentPos] = '-';
				alignmentPos++;
			}
			// N
			else if(cigarChar == 'N'){
				alignmentVector[alignmentPos] = 'N';
				alignmentPos++;
			}
			// S
			else if(cigarChar == 'S'){
                readPos++;
			}
			// H
			else if(cigarChar =='H'){

            }
			// P
			else if(cigarChar == 'P'){
				alignmentVector[alignmentPos] = '-';
				alignmentPos++;
			}
		}

		return alignmentVector;
	}

    private boolean processReadAlignment(BamGenomeWindow window, char[] alignment, long readStart, long readEnd,
                                         int mappingQuality) {

        long windowSize = window.getWindowSize();
        long windowStart = window.getStart();

        SingleReadData readData = getWindowData(windowStart, analysisResults);

        // working variables
        boolean outOfBounds = false;
        long relative;
        int pos;

        if(readEnd < readStart){
            System.err.println("WARNING: read alignment start is greater than end: " + readStart + " > " + readEnd);
        }

        //readData.numberOfProcessedReads++;
        if(readEnd> window.getEnd()){
            outOfBounds = true;
            //readData.numberOfOutOfBoundsReads++;
        }


        // run read
        for(long j=readStart; j<=readEnd; j++){
            relative = (int)(j - windowStart);
            pos = (int)(j-readStart);

            if(relative<0){

            } else if(relative >= windowSize){
                //	System.err.println("WARNING: " + read.getReadName() + " is fuera del tiesto " + relative);
                break;
            } else {

                if (analyzeRegions) {
                    boolean insideOfRegion = window.getSelectedRegions().get((int)relative);
                    if (insideOfRegion) {
                        if (computeOutsideStats) {
                            readData =getWindowData(windowStart, analysisResults);
                            //statsCollector = insideCollector;
                        }
                    } else {
                        if (computeOutsideStats) {
                            readData = getWindowData(windowStart, outOfRegionsResults);
                            //statsCollector = outsideCollector;
                        } else {
                            continue;
                        }
                    }
                }

                char nucleotide = alignment[pos];

                // aligned bases
                readData.numberOfMappedBases++;

                if (nucleotide != '-' && nucleotide != 'N') {
                    // mapping quality
                    readData.acumMappingQuality(relative, mappingQuality);
                    // base stats
                    readData.acumBase(relative, nucleotide);
                }

            }



        }


        return outOfBounds;
    }

    private void propagateRead(char[] alignment,long readStart, long readEnd,
                               int mappingQuality ){
        // init covering stat
        BamStats bamStats = ctx.getBamStats();
		int index = bamStats.getNumberOfProcessedWindows()+1;

		BamGenomeWindow adjacentWindow;
		boolean outOfBounds = true;
		while(outOfBounds && index < bamStats.getNumberOfWindows()){

			// next currentWindow
			long ws = bamStats.getWindowStart(index);

		    synchronized (lock) {
                ConcurrentMap<Long,BamGenomeWindow> openWindows = ctx.getOpenWindows();
                adjacentWindow = ctx.getOpenWindow(ws, bamStats, openWindows);
                if (computeOutsideStats) {
                    Map<Long,BamGenomeWindow> openOutsideWindows = ctx.getOpenOutsideWindows();
                    ctx.getOpenWindow(ws, ctx.getOutsideBamStats(), openOutsideWindows);
                }
            }

            // acum read
            outOfBounds = processReadAlignment(adjacentWindow, alignment, readStart, readEnd,
                    mappingQuality);

			index++;
		}
    }





}
