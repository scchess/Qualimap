package org.bioinfo.ngs.qc.qualimap.process;

import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import org.bioinfo.ngs.qc.qualimap.beans.BamGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats;
import org.bioinfo.ngs.qc.qualimap.beans.SingleReadData;
import org.bioinfo.ngs.qc.qualimap.gui.threads.BamAnalysisThread;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
    private static final Object lock = new Object();
    HashMap<Long, SingleReadData> analysisResults;
    HashMap<Long, SingleReadData> outOfRegionsResults;


    public static class Result {
        Collection<SingleReadData> readsData;
        Collection<SingleReadData> outRegionReadsData;

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

        //System.out.println("Started bunch of read analysis. The first read is" + reads.get(0).getReadName());
        Result taskResult = new Result();

        for (SAMRecord read : reads) {

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
        if (analyzeRegions && computeOutsideStats) {
            taskResult.setOutOfRegionReadsData(outOfRegionsResults.values());
        }
        return taskResult;
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

        // TODO: FIX?
        //if(readStart>= window.getStart()) {
//			numberOfSequencedBases+=read.getReadBases().length;
//			numberOfCigarElements+=read.getCigar().numCigarElements();
        //}

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


}
