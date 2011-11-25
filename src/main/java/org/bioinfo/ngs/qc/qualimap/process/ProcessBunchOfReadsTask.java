package org.bioinfo.ngs.qc.qualimap.process;

import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import org.bioinfo.ngs.qc.qualimap.beans.BamGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats;
import org.bioinfo.ngs.qc.qualimap.beans.GenomeLocator;
import org.bioinfo.ngs.qc.qualimap.beans.SingleReadData;

import java.util.ArrayList;
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
    private static Object lock = new Object();
    HashMap<Long, SingleReadData> taskResults;


    public ProcessBunchOfReadsTask(List<SAMRecord> reads, BamGenomeWindow window, BamStatsAnalysis ctx)  {
        this.reads = reads;
        this.ctx = ctx;
        isPairedData = true;
        insertSize = -1;
        currentWindow = window;
        taskResults = new HashMap<Long, SingleReadData>();
    }


    private SingleReadData getWindowData(Long windowStart) {
        if (taskResults.containsKey(windowStart)) {
            return taskResults.get(windowStart);
        } else {
            SingleReadData data = new SingleReadData(windowStart);
            taskResults.put(windowStart, data);
            return data;
        }
    }

    public Collection<SingleReadData> call() {

        //System.out.println("Started bunch of read analysis. The first read is" + reads.get(0).getReadName());

        for (SAMRecord read : reads) {

            long position = ctx.getLocator().getAbsoluteCoordinates(read.getReferenceName(), read.getAlignmentStart());
            //System.out.println("From ProcessReadTask: started analysis of read " + read.getReadName() + ", size: " + read.getReadLength());

            String alignment = null;
            // compute alignment
            try {
                alignment = BamGenomeWindow.computeAlignment(read);
            } catch (SAMFormatException e) {
                System.err.println("Problem analyzing the read: " + read.getReadName());
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

            if (alignment == null || alignment.isEmpty()) {
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


            // acum read
            boolean outOfBounds = processRead(currentWindow, read, alignment, ctx.getLocator());
            //System.out.println("From ProcessReadTask: calculated stats for read" + read.getHeader().toString());

            if(outOfBounds) {
                //System.out.println("From ProcessReadTask: propogating read" + read.getHeader().toString());
                propagateRead(alignment, position, position + alignment.length()-1, read.getMappingQuality(), insertSize,true);
            }

        }



        return taskResults.values();
    }

    protected boolean processRead(BamGenomeWindow window, SAMRecord read, String alignment, GenomeLocator locator){
        long readStart = locator.getAbsoluteCoordinates(read.getReferenceName(),read.getAlignmentStart());
        long readEnd = locator.getAbsoluteCoordinates(read.getReferenceName(),read.getAlignmentEnd());
        return processRead(window, alignment, readStart, readEnd, read.getMappingQuality(),read.getInferredInsertSize());
    }


    private boolean processRead(BamGenomeWindow window, String alignment, long readStart, long readEnd, int mappingQuality, long insertSize){

            long windowSize = window.getWindowSize();
            long windowStart = window.getStart();

            SingleReadData readData = getWindowData(windowStart);

            //boolean  selectedRegionsAvailable = window.isSelectedRegionsAvailable();
            //boolean[] selectedRegions = window.getSelectedRegions();

            // working variables
            boolean outOfBounds = false;
            long relative;
            long pos;

            if(readEnd<readStart){
                System.err.println("WARNING: read aligment start is greater than end: " + readStart + " > " + readEnd);
            }

            // acums
            //readData.numberOfProcessedReads++;
            if(readEnd> window.getEnd()){
                outOfBounds = true;
                //readData.numberOfOutOfBoundsReads++;
            }

            // TO FIX
            if(readStart>= window.getStart()) {
//			numberOfSequencedBases+=read.getReadBases().length;
//			numberOfCigarElements+=read.getCigar().numCigarElements();
            }

            char nucleotide;
            // run read
            for(long j=readStart; j<=readEnd; j++){
                relative = (int)(j - windowStart);
                pos = (int)(j-readStart);

                if(relative<0){

                } else if(relative >= windowSize){
                    //	System.err.println("WARNING: " + read.getReadName() + " is fuera del tiesto " + relative);
                } else {
                    //if(!selectedRegionsAvailable || (selectedRegionsAvailable && selectedRegions[(int)relative])) {
                        nucleotide = alignment.charAt((int)pos);

                        // aligned bases
                        readData.numberOfAlignedBases++;

                        // Any letter
                        if(nucleotide=='A' || nucleotide=='C' || nucleotide=='T' || nucleotide=='G'){
                            readData.acumBase(relative);
                            if(insertSize!=-1){
                                readData.acumProperlyPairedBase(relative);
                            }
                        }

                        // ATCG content
                        if(nucleotide=='A'){
                            readData.acumA(relative);
                        }
                        else if(nucleotide=='C'){
                            readData.acumC(relative);
                        }
                        else if(nucleotide=='T'){
                            readData.acumT(relative);
                        }
                        else if(nucleotide=='G'){
                            readData.acumG(relative);
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

            //taskResults.add(readData);

            return outOfBounds;
        }


    private void propagateRead(String alignment,long readStart, long readEnd, int mappingQuality,long insertSize,boolean detailed){
		// init covering stat
		long ws, we;
		String name;
        BamStats bamStats = ctx.getBamStats();
		int index = bamStats.getNumberOfProcessedWindows()+1;

		BamGenomeWindow adjacentWindow;
		boolean outOfBounds = true;
		while(outOfBounds && index < bamStats.getNumberOfWindows()){
			// next currentWindow
			ws = bamStats.getWindowStart(index);
			we = bamStats.getWindowEnd(index);
			name = bamStats.getWindowName(index);

            synchronized (lock) {
                ConcurrentMap<Long,BamGenomeWindow> openWindows = ctx.getOpenWindows();
                if(openWindows.containsKey(ws)){
                    adjacentWindow = openWindows.get(ws);
                } else {
                    adjacentWindow = ctx.initWindow(name, ws, Math.min(we, bamStats.getReferenceSize()), ctx.getReference(), detailed);
                    bamStats.incInitializedWindows();
                    openWindows.put(ws,adjacentWindow);
                }
            }
            // acum read
            outOfBounds = processRead(adjacentWindow, alignment,readStart,readEnd,mappingQuality,insertSize);

			index++;
		}
    }


}
