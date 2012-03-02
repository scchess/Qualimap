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
    long insertSize;
    boolean isPairedData;
    boolean analyzeRegions, computeOutsideStats;
    private static int INITIAL_SIZE = 64;
    private static final Object lock = new Object();
    HashMap<Long, SingleReadData> analysisResults;
    HashMap<Long, SingleReadData> outOfRegionsResults;
    ArrayList<Float> readsGcContent;
    ReadStatsCollector readStatsCollector;
    ArrayList<Float> outOfRegionsReadsGCContent;
    ReadStatsCollector outOfRegionsReadStatsCollector;

    public static class ReadStatsCollector {

        int[] readsAContent;
        int[] readsCContent;
        int[] readsGContent;
        int[] readsTContent;
        int[] readsNContent;
        ArrayList<Float> readsGcContent;

        int numBases;
        int numGC;


        public int[] getReadsAContent() {
            return readsAContent;
        }

        public int[] getReadsCContent() {
            return readsCContent;
        }

        public int[] getReadsGContent() {
            return readsGContent;
        }

        public int[] getReadsTContent() {
            return readsTContent;
        }

        public int[] getReadsNContent() {
            return readsNContent;
        }

        public ArrayList<Float> getReadsGcContent() {
            return readsGcContent;
        }

        ReadStatsCollector() {
            readsGcContent = new ArrayList<Float>();
            readsAContent = new int[INITIAL_SIZE];
            readsCContent = new int[INITIAL_SIZE];
            readsGContent = new int[INITIAL_SIZE];
            readsTContent = new int[INITIAL_SIZE];
            readsNContent = new int[INITIAL_SIZE];
        }

        void saveGC() {
            if (numGC != 0) {
                float gcContent = (float)numGC / (float)numBases;
                readsGcContent.add(gcContent);
            }

            numBases = 0;
            numGC = 0;
        }


        public void collectBases(SAMRecord read) {
            byte[] readBases = read.getReadBases();
            for (int pos = 0; pos < readBases.length; ++pos) {
                byte base = readBases[pos];
                if (base == 'A') {
                    incAsContent(pos);
                    numBases++;
                } else if (base == 'C') {
                    incCsContent(pos);
                    numGC++;
                    numBases++;
                } else if (base == 'G') {
                    incGsContent(pos);
                    numGC++;
                    numBases++;
                } else if (base == 'T') {
                    incTsContent(pos);
                    numBases++;
                } else if (base == 'N') {
                    incNsContent(pos);
                }

                if (numBases >= 1000) {
                    saveGC();
                }
            }

        }

        private void incAsContent(int pos) {
            readsAContent = ensureArraySize(readsAContent, pos);
            readsAContent[pos]++;
        }

        private void incGsContent(int pos) {
            readsGContent = ensureArraySize(readsGContent, pos);
            readsGContent[pos]++;
        }

        private void incCsContent(int pos) {
            readsCContent = ensureArraySize(readsCContent, pos);
            readsCContent[pos]++;
        }

        private void incTsContent(int pos) {
            readsTContent = ensureArraySize(readsTContent, pos);
            readsTContent[pos]++;
        }

        private void incNsContent(int pos) {
            readsNContent = ensureArraySize(readsNContent, pos);
            readsNContent[pos]++;
        }

    }


    public static class Result {
        Collection<SingleReadData> readsData;
        Collection<SingleReadData> outRegionReadsData;
        ReadStatsCollector readsStatsCollector;
        ReadStatsCollector outRegionReadStatsCollector;

        public int[] getReadsAContent() {
            return readsStatsCollector.getReadsAContent();
        }

        public int[] getReadsTContent() {
            return readsStatsCollector.getReadsTContent();
        }

        public int[] getReadsNContent() {
            return readsStatsCollector.getReadsNContent();
        }

        public int[] getReadsCContent() {
            return readsStatsCollector.getReadsCContent();
        }

        public int[] getReadsGContent() {
            return readsStatsCollector.getReadsGContent();
        }

        public void setReadsStatsCollector(ReadStatsCollector readsStatsCollector) {
            this.readsStatsCollector = readsStatsCollector;
        }

        public void setOutRegionReadStatsCollector(ReadStatsCollector readsStatsCollector) {
            this.outRegionReadStatsCollector = readsStatsCollector;
        }

        public void setGlobalReadsData(Collection<SingleReadData> readsData) {
            this.readsData = readsData;
        }

        public Collection<SingleReadData> getGlobalReadsData() {
            return readsData;
        }

        public void setOutOfRegionReadsData(Collection<SingleReadData> inRegionReadsData) {
            this.outRegionReadsData = inRegionReadsData;
        }

        public Collection<SingleReadData> getOutOfRegionReadsData() {
            return outRegionReadsData;
        }

        public Collection<Float> getReadsGcContent() {
            return readsStatsCollector.getReadsGcContent();
        }


        public Collection<Float> getOutRegionOfReadsGcContent() {
            return outRegionReadStatsCollector.getReadsGcContent();
        }


        public int[] getOutOfRegionsReadsAContent() {
            return outRegionReadStatsCollector.getReadsAContent();
        }

        public int[] getOutOfRegionsReadsCContent() {
            return outRegionReadStatsCollector.getReadsCContent();
        }

        public int[] getOutOfRegionsReadsGContent() {
            return outRegionReadStatsCollector.getReadsGContent();
        }

        public int[] getOutOfRegionsReadsTContent() {
            return outRegionReadStatsCollector.getReadsTContent();
        }

        public int[] getOutOfRegionsReadsNContent() {
            return outRegionReadStatsCollector.getReadsNContent();
        }
    }

    public ProcessBunchOfReadsTask(List<SAMRecord> reads, BamGenomeWindow window, BamStatsAnalysis ctx)  {
        this.reads = reads;
        this.ctx = ctx;
        this.analyzeRegions = ctx.selectedRegionsAvailable();
        isPairedData = true;
        insertSize = -1;
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


    public Result call() {

        //long startTime = System.currentTimeMillis();
        //System.out.println("Started bunch of read analysis. The first read is" + reads.get(0).getReadName());

        Result taskResult = new Result();

        for (SAMRecord read : reads) {

            analyzeReadsContent(read);

            long position = ctx.getLocator().getAbsoluteCoordinates(read.getReferenceName(), read.getAlignmentStart());
            //System.out.println("From ProcessReadTask: started analysis of read " + read.getReadName() + ", size: " + read.getReadLength());

            char[] alignment = null;
            // compute alignment
            try {
                alignment = BamGenomeWindow.computeAlignment(read);
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
                    insertSize = read.getInferredInsertSize();
                }
            } catch(IllegalStateException ise){
                insertSize = -1;
                isPairedData = false;
            }

            // NEW VERSION
            /*try {
                boolean outOfBounds = processRead(currentWindow, read, position);
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
            boolean outOfBounds = processRead(currentWindow, alignment, position, readEnd,
                    read.getMappingQuality(), insertSize);

            if(outOfBounds) {
                //System.out.println("From ProcessReadTask: propogating read" + read.getHeader().toString());
                propagateRead(alignment, position, readEnd, read.getMappingQuality(),
                        insertSize, true);
            }

        }

        /*for (SingleReadData readData : analysisResults.values()) {
            float gcContent = (float) (readData.numberOfCs + readData.numberOfGs) / readData.numberOfSequencedBases;
            readsGcContent.add(gcContent);
        }*/

        readStatsCollector.saveGC();
        taskResult.setGlobalReadsData(analysisResults.values());
        taskResult.setReadsStatsCollector(readStatsCollector);

        if (analyzeRegions && computeOutsideStats) {
            /*for (SingleReadData readData : outOfRegionsResults.values()) {
                float gcContent = (float) (readData.numberOfCs + readData.numberOfGs) / readData.numberOfSequencedBases;
                outOfRegionsReadsGCContent.add(gcContent);
            }*/
            outOfRegionsReadStatsCollector.saveGC();
            taskResult.setOutOfRegionReadsData(outOfRegionsResults.values());
            taskResult.setOutRegionReadStatsCollector(outOfRegionsReadStatsCollector);
        }


        //long endTime = System.currentTimeMillis();
        //System.out.println("Analyze bunch of reads time: " + (endTime - startTime));

        return taskResult;
    }

    private void analyzeReadsContent(SAMRecord read) {

        if (read.getAttribute(Constants.READ_IN_REGION).equals(1) ) {
            readStatsCollector.collectBases(read);
        } else if (analyzeRegions && computeOutsideStats) {
            outOfRegionsReadStatsCollector.collectBases(read);
        }
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


    private boolean processRead(BamGenomeWindow window, SAMRecord read, long alignmentStart ) {

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
            // TODO: check on every iteration? -> we can do it better! :)
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
            outOfBounds = processRead(adjacentWindow, read, readStart );

			index++;
		}
    }*/

    private boolean processRead(BamGenomeWindow window, char[] alignment, long readStart, long readEnd,
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
                // mapping quality
                readData.acumMappingQuality(relative, mappingQuality);
                // insert size
                readData.acumInsertSize(relative, insertSize);

                if (nucleotide != '-' && nucleotide != 'N') {
                    readData.acumBase(relative, nucleotide, insertSize);
                }

            }



        }


        return outOfBounds;
    }

    private void propagateRead(char[] alignment,long readStart, long readEnd,
                               int mappingQuality,long insertSize,boolean detailed ){
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
            outOfBounds = processRead(adjacentWindow, alignment,readStart,readEnd,
                    mappingQuality,insertSize);

			index++;
		}
    }

    private static int[] ensureArraySize(int[] array, int pos) {
        int size = array.length;
        if (pos >= size) {
            int new_size = size*2 < pos + 1 ? pos + 1 : size*2;
            int[] new_array = new int[new_size];
            System.arraycopy(array, 0, new_array, 0, array.length);
            return new_array;
        }
        return array;
    }



}
