package org.bioinfo.ngs.qc.qualimap.process;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import org.bioinfo.ngs.qc.qualimap.beans.BamGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats;
import org.bioinfo.ngs.qc.qualimap.beans.SingleReadData;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by kokonech
 * Date: 11/15/11
 * Time: 5:04 PM
 */
public class ProcessBunchOfReadsTask implements Callable {
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
        readStatsCollector = new ReadStatsCollector();
        if ( analyzeRegions && computeOutsideStats ) {
            outOfRegionsResults = new HashMap<Long, SingleReadData>();
            outOfRegionsReadsGCContent = new ArrayList<Float>();
            outOfRegionsReadStatsCollector = new ReadStatsCollector();
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

            //analyzeReadsContent(read);

            long position = ctx.getLocator().getAbsoluteCoordinates(read.getReferenceName(), read.getAlignmentStart());
            //System.out.println("From ProcessReadTask: started analysis of read " + read.getReadName() + ", size: " + read.getReadLength());

            char[] alignment = null;
            // compute alignment
            try {
                ReadStatsCollector statsCollector = getReadStatsCollector(read);
                if (statsCollector != null) {
                    // collect some read stats
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
            long insertSize = -1;
            try {
                if(computeInsertSize && read.getProperPairFlag()){
                    insertSize = read.getInferredInsertSize();
                }
            } catch(IllegalStateException ise){
                isPairedData = false;
            }

            int mappingQuality = read.getMappingQuality();

            // NEW VERSION
            /*try {
                boolean outOfBounds = processReadAlignment(currentWindow, read, position);
                if(outOfBounds) {
                    propagateRead(read, position);
                }
            } catch (SAMFormatException e) {
                System.err.println("Problem analyzing the read: " + read.getReadName());
                System.err.println(e.getMessage());
                e.printStackTrace();
            }*/

            long readEnd = position + alignment.length - 1;

            // acum read

            //regionLookupTable = createRegionLookupTable(position, readEnd, ctx.getRegionsTree());
            boolean outOfBounds = processReadAlignment(currentWindow, alignment, position, readEnd,
                    mappingQuality, insertSize);

            if(outOfBounds) {
                //System.out.println("From ProcessReadTask: propogating read" + read.getHeader().toString());
                propagateRead(alignment, position, readEnd, mappingQuality, insertSize);
            }

        }

        /*for (SingleReadData readData : analysisResults.values()) {
            float gcContent = (float) (readData.numberOfCs + readData.numberOfGs) / readData.numberOfSequencedBases;
            readsGcContent.add(gcContent);
        }*/

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

    /**
     * This method computes read to reference alignment vector along with collecting read's stats
     * @param read The record being analyzed
     * @param statsCollector Read stats collector
     * @return Alignment vector, which includes extended issues
     */

    public static char[] computeReadAlignment(SAMRecord read, ReadStatsCollector statsCollector){
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
			if(cigarChar == 'M'){
				// get base
				byte base = readBases[readPos];
                statsCollector.collectBase(readPos, base);
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
			if(cigarChar == 'M'){
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


    /*char[] calculateAlignmentVector(SAMRecord read) {

        Cigar cigar = read.getCigar();

        // precompute total size of alignment
        int totalSize = 0;
        List<CigarElement> elementList = cigar.getCigarElements();
        //int numCigarElements = cigar.numCigarElements();
        for(CigarElement element : elementList){
            totalSize += element.getLength();
        }

        // compute extended cigar
        char[] extended = new char[totalSize];
        int mpos = 0;
        int npos;
        for(CigarElement element : elementList){
            npos = mpos + element.getLength();
            Arrays.fill(extended, mpos, npos, element.getOperator().name().charAt(0));
            mpos = npos;
        }

        return extended;
    }


    private boolean processReadAlignment(BamGenomeWindow window, SAMRecord read, long alignmentStart ) {

        // init read params
        int alignmentLength = read.getAlignmentEnd() - read.getAlignmentStart() + 1;

        if (alignmentLength < 0) {
            return false;
        }

        // init extended cigar portion
        char[] extendedCigarVector = calculateAlignmentVector(read);

        //char[] alignmentVector = new char[alignmentLength];

        int readPos = 0;
        char base;
        byte[] readBases = read.getReadBases();
        long alignmentPos = alignmentStart;
        long windowStart = window.getStart();
        long windowSize = window.getWindowSize();
        SingleReadData readData = getWindowData(windowStart, analysisResults);

        for( char cigarCode : extendedCigarVector){

            long relative = alignmentPos - windowStart;
            boolean validAlignment = relative >= 0 && relative < windowSize;
            if (analyzeRegions && validAlignment) {
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
            // M
            if(cigarCode == 'M'){
                if (validAlignment) {
                    base = (char) readBases[readPos];
                    readData.numberOfAlignedBases++;
                    readData.acumBase(relative, base, insertSize);
                    // mapping quality
                    readData.acumMappingQuality(relative, read.getMappingQuality());
                    // insert size
                    readData.acumInsertSize(relative, insertSize);
                }
                readPos++;
                //collect readData
                alignmentPos++;

            }
            // I
            else if(cigarCode == 'I'){
                //do nothing
            }
            // D
            else if(cigarCode == 'D'){
                if (validAlignment) {
                    readData.numberOfAlignedBases++;
                    readData.acumMappingQuality(relative, read.getMappingQuality());
                    readData.acumInsertSize(relative, insertSize);
                }
                // do nothing
                alignmentPos++;

            }
            // N
            else if(cigarCode=='N'){
                if (validAlignment) {
                    readData.numberOfAlignedBases++;
                    readData.acumMappingQuality(relative, read.getMappingQuality());
                    readData.acumInsertSize(relative, insertSize);
                }
                alignmentPos++;
            }
            // S
            else if(cigarCode == 'S'){
                readPos++;
                //acum read data
            }
            // H
            else if(cigarCode == 'H'){
                //readPos++;
            }
            // P
            else if (cigarCode == 'P' ){
                //alignmentVector[alignmentPos] = '-';
                alignmentPos++;
            }
        }

        return  alignmentPos > window.getEnd();

    }

    private void propagateRead(SAMRecord read, long readStart){
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
            }

            // acum read
            outOfBounds = processReadAlignment(adjacentWindow, read, readStart );

			index++;
		}
    }*/

    private boolean processReadAlignment(BamGenomeWindow window, char[] alignment, long readStart, long readEnd,
                                         int mappingQuality, long insertSize) {

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

                // TODO: check on every iteration? -> we can do it better! :)
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
                readData.numberOfAlignedBases++;

                if (nucleotide != '-' && nucleotide != 'N') {
                    // mapping quality
                    readData.acumMappingQuality(relative, mappingQuality);
                    // insert size
                    readData.acumInsertSize(relative, insertSize);
                    // base stats
                    readData.acumBase(relative, nucleotide, insertSize);
                }

            }



        }


        return outOfBounds;
    }

    private void propagateRead(char[] alignment,long readStart, long readEnd,
                               int mappingQuality,long insertSize ){
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
                    mappingQuality, insertSize);

			index++;
		}
    }





}
