package org.bioinfo.ngs.qc.qualimap.process;

import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import org.bioinfo.ngs.qc.qualimap.beans.BamGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats;
import org.bioinfo.ngs.qc.qualimap.beans.SingleReadData;

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
    int[] readsAContent,readsCContent, readsGContent, readsTContent,readsNContent;

    //TODO: readsGCContent, readsContent etc. for out of regions

    public static class Result {
        Collection<SingleReadData> readsData;
        Collection<SingleReadData> outRegionReadsData;
        Collection<Float> readsGcContent;
    int[] readsAContent;
        int[] readsCContent;
        int[] readsGContent;
        int[] readsTContent;
        int[] readsNContent;

        public int[] getReadsAContent() {
            return readsAContent;
        }

        public void setReadsAContent(int[] readsAContent) {
            this.readsAContent = readsAContent;
        }

        public int[] getReadsTContent() {
            return readsTContent;
        }

        public void setReadsTContent(int[] readsTContent) {
            this.readsTContent = readsTContent;
        }

        public int[] getReadsNContent() {
            return readsNContent;
        }

        public void setReadsNContent(int[] readsNContent) {
            this.readsNContent = readsNContent;
        }


        public int[] getReadsCContent() {
            return readsCContent;
        }

        public void setReadsCContent(int[] readsCContent) {
            this.readsCContent = readsCContent;
        }

        public int[] getReadsGContent() {
            return readsGContent;
        }

        public void setReadsGContent(int[] readsGContent) {
            this.readsGContent = readsGContent;
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

        public void setReadsGcContent(Collection<Float> gcContent) {
            this.readsGcContent = gcContent;
        }

        public Collection<Float> getReadsGcContent() {
            return readsGcContent;
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
        readsAContent = new int[INITIAL_SIZE];
        readsCContent = new int[INITIAL_SIZE];
        readsGContent = new int[INITIAL_SIZE];
        readsTContent = new int[INITIAL_SIZE];
        readsNContent = new int[INITIAL_SIZE];

        if ( analyzeRegions && computeOutsideStats ) {
            outOfRegionsResults = new HashMap<Long, SingleReadData>();
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
        taskResult.setGlobalReadsData(analysisResults.values());
        taskResult.setReadsGcContent(readsGcContent);
        taskResult.setReadsAContent(readsAContent);
        taskResult.setReadsCContent(readsCContent);
        taskResult.setReadsGContent(readsGContent);
        taskResult.setReadsTContent(readsTContent);
        taskResult.setReadsNContent(readsNContent);
        if (analyzeRegions && computeOutsideStats) {
            taskResult.setOutOfRegionReadsData(outOfRegionsResults.values());
        }


        //long endTime = System.currentTimeMillis();
        //System.out.println("Analyze bunch of reads time: " + (endTime - startTime));

        return taskResult;
    }

    private void analyzeReadsContent(SAMRecord read) {
        byte[] readBases = read.getReadBases();
        for (int pos = 0; pos < readBases.length; ++pos) {
            byte base = readBases[pos];
            if (base == 'A') {
                incAsContent(pos);
            } else if (base == 'C') {
                incCsContent(pos);
            } else if (base == 'G') {
                incGsContent(pos);
            } else if (base == 'T') {
                incTsContent(pos);
            } else if (base == 'N') {
                incNsContent(pos);
            }
        }
    }


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
                        }
                    } else {
                        if (computeOutsideStats) {
                            readData = getWindowData(windowStart, outOfRegionsResults);
                        } else {
                            continue;
                        }
                    }
                }

                char nucleotide = alignment[pos];

                // aligned bases
                readData.numberOfAlignedBases++;

                // Any letter
                /*if(nucleotide=='A' || nucleotide=='C' || nucleotide=='T' || nucleotide=='G'){
                    readData.acumBase(relative);
                    if(insertSize!=-1){
                        readData.acumProperlyPairedBase(relative);
                    }
                }*/

                // ATCG content
                if(nucleotide=='A'){
                    readData.acumA(relative);
                    readData.acumBase(relative);
                    if(insertSize!=-1){
                        readData.acumProperlyPairedBase(relative);
                    }
                }
                else if(nucleotide=='C'){
                    readData.acumC(relative);
                    readData.acumBase(relative);
                    if(insertSize!=-1){
                        readData.acumProperlyPairedBase(relative);
                    }
                }
                else if(nucleotide=='T'){
                    readData.acumT(relative);
                    readData.acumBase(relative);
                    if(insertSize!=-1){
                        readData.acumProperlyPairedBase(relative);
                    }
                }
                else if(nucleotide=='G'){
                    readData.acumG(relative);
                    readData.acumBase(relative);
                    if(insertSize!=-1){
                        readData.acumProperlyPairedBase(relative);
                    }
                }
                else if(nucleotide=='-'){
                }
                else if(nucleotide=='N'){

                }

                // mapping quality
                readData.acumMappingQuality(relative, mappingQuality);

                // insert size
                readData.acumInsertSize(relative, insertSize);

                //}
            }
            if (readData.numberOfMappedBases > 0) {
                float readGcContent = (float) (readData.numberOfCs + readData.numberOfGs) / readData.numberOfSequencedBases;
                readsGcContent.add( readGcContent );
            }
        }


        return outOfBounds;
    }

    private int[] ensureArraySize(int[] array, int pos) {
        int size = array.length;
        if (pos >= size) {
            int new_size = size*2 < pos + 1 ? pos + 1 : size*2;
            int[] new_array = new int[new_size];
            for (int i = 0; i < array.length; ++i) {
                new_array[i] = array[i];
            }
            return new_array;
        }
        return array;
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


}
