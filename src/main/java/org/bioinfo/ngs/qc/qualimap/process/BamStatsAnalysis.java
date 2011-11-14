package org.bioinfo.ngs.qc.qualimap.process;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.formats.core.sequence.Fasta;
import org.bioinfo.formats.core.sequence.io.FastaReader;
import org.bioinfo.ngs.qc.qualimap.beans.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by  kokonech
 * Date: 11/7/11
 * Time: 4:06 PM
 */
public class BamStatsAnalysis {

    // input data
	private String bamFile;
	private String referenceFile;

	// reference
	private boolean referenceAvailable;
	private byte[] reference;
	private long referenceSize;
	private int numberOfReferenceContigs;

	// currentWindow management
	private int numberOfWindows;
	private int effectiveNumberOfWindows;
	private int windowSize;

	// coordinates transformer
	private GenomeLocator locator;

	// globals
	private int numberOfReads;
	private int numberOfValidReads;
	private double percentageOfValidReads;
	private int numberOfMappedReads;
	private double percentageOfMappedReads;
    private int numberOfDuplicatedReads;
    Future<Integer> lastResult;

	// statistics
	private BamStats bamStats;

	private Logger logger;

	// working variables
	private BamGenomeWindow currentWindow;
	private ConcurrentMap<Long,BamGenomeWindow> openWindows;

	// nucleotide reporting
	private String outdir;

	// gff support
	private boolean selectedRegionsAvailable;
	private String gffFile;
	private int numberOfSelectedRegions;

	// inside
	private long insideReferenceSize;
	private int insideWindowSize;
	private int effectiveInsideNumberOfWindows;
	private BamGenomeWindow currentInsideWindow;
	private HashMap<Long,BamGenomeWindow> openInsideWindows;
	private BamStats insideBamStats;
	private int numberOfInsideMappedReads;

	// outside
	private boolean computeOutsideStats;
	private long outsideReferenceSize;
	private int outsideWindowSize;
	private int effectiveOutsideNumberOfWindows;
	private BamGenomeWindow currentOutsideWindow;
	private HashMap<Long,BamGenomeWindow> openOutsideWindows;
	private BamStats outsideBamStats;
	private int numberOfOutsideMappedReads;

	private long[] selectedRegionStarts;
	private long[] selectedRegionEnds;
	private long[] selectedRegionRelativePositions;

	// insert size
	private boolean computeInsertSize;

	// chromosome
	private boolean computeChromosomeStats;
	private BamStats chromosomeStats;

	// reporting
	private boolean activeReporting;
	private boolean saveCoverage;
	private boolean isPairedData;

    private ExecutorService threadPool;

    public BamStatsAnalysis(String bamFile){
		this.bamFile = bamFile;
		this.numberOfWindows = 200;
		logger = new Logger();
        threadPool = Executors.newFixedThreadPool(4);
    }

    public void run() throws Exception{


        // init reader
		SAMFileReader reader = new SAMFileReader(new File(bamFile));

        // org.bioinfo.ntools.process header
		String lastActionDone = "loading sam header header";
		logger.println(lastActionDone);
		SAMFileHeader header = reader.getFileHeader();

        // load locator
        lastActionDone = "loading locator";
        logger.println(lastActionDone);
        loadLocator(header);

        // load reference
        lastActionDone = "loading reference";
        logger.println(lastActionDone);
        loadReference();

        long startTime = System.currentTimeMillis();

        // init window set
        windowSize = computeWindowSize(referenceSize,numberOfWindows);
        effectiveNumberOfWindows = computeEffectiveNumberOfWindows(referenceSize,windowSize);
        bamStats = new BamStats("genome",referenceSize,effectiveNumberOfWindows);
        logger.println("effectiveNumberOfWindows " + effectiveNumberOfWindows);
        bamStats.setSourceFile(bamFile);
        bamStats.setWindowReferences("w",windowSize);
        openWindows = new ConcurrentHashMap<Long,BamGenomeWindow>();

        currentWindow = nextWindow(bamStats,openWindows,reference,true,true);

        // init working variables
		isPairedData = true;

		// run reads
        SAMRecordIterator iter = reader.iterator();
        List<Callable<List<SingleReadData>>> taskList = new ArrayList<Callable<List<SingleReadData>>>();

        while(iter.hasNext()){

            SAMRecord read = null;

            try {
                read = iter.next();
            } catch (RuntimeException e) {
                logger.warn( e.getMessage() );
            }

            if (read == null) {
                continue;
            }

			// filter invalid reads
			if(read.isValid() == null){
                 if (read.getDuplicateReadFlag()) {
                    numberOfDuplicatedReads++;
                 }

				// accumulate only mapped reads
				if(!read.getReadUnmappedFlag()) {
                    numberOfMappedReads++;
                }  else {
                    ++numberOfReads;
                    continue;
                }


			    // compute absolute position
				long position = locator.getAbsoluteCoordinates(read.getReferenceName(),read.getAlignmentStart());

                // finalize current and get next window
			    if(position > currentWindow.getEnd() ){
                    analyzeReads(taskList);
                    //finalize
				    currentWindow = finalizeAndGetNextWindow(position,currentWindow,openWindows,
                            bamStats,reference,true,true);
                    taskList.clear();
                }

                if (currentWindow == null) {
                    //Some reads are out of reference bounds?
                    break;
                }

                taskList.add( new ProcessReadTask(read, position, currentWindow, this));
                numberOfValidReads++;

            }

            numberOfReads++;

        }
        // close stream
        reader.close();


        if (!taskList.isEmpty()) {
            int numWindows = bamStats.getNumberOfWindows();
            long lastPosition = bamStats.getWindowEnd(numWindows - 1) + 1;
            analyzeReads(taskList);
            //finalize
            finalizeAndGetNextWindow(lastPosition,currentWindow,openWindows,bamStats,reference,true,true);
            taskList.clear();
        }

        if (lastResult != null) {
            lastResult.get();
        }

        threadPool.shutdown();

        long endTime = System.currentTimeMillis();


        logger.println("Number of reads: " + numberOfReads);
        logger.println("Number of mapped reads: " + numberOfMappedReads);
        logger.println("Number of valid reads: " + numberOfValidReads);
        logger.println("Number of dupliated reads: " + numberOfDuplicatedReads);
        logger.println("Time taken: " + (endTime - startTime) / 1000);

        // summarize
		percentageOfValidReads = ((double)numberOfValidReads/(double)numberOfReads)*100.0;
		bamStats.setNumberOfReads(numberOfReads);
		bamStats.setNumberOfMappedReads(numberOfMappedReads);
		bamStats.setPercentageOfMappedReads(((double)numberOfMappedReads/(double)numberOfReads)*100.0);
		bamStats.setPercentageOfValidReads(percentageOfValidReads);

		// compute descriptors
		logger.print("Computing descriptors...");
		bamStats.computeDescriptors();

		// compute histograms
		logger.print("Computing histograms...");
		bamStats.computeCoverageHistogram();

    }


    public void analyzeReads(List<Callable<List<SingleReadData>>> taskList ) throws InterruptedException, ExecutionException {

        List<Future<List<SingleReadData>>> results  = threadPool.invokeAll(taskList);
        // wait till all tasks are finished
        for (Future<List<SingleReadData>> result : results) {
            List<SingleReadData> dataset = result.get();
            for (SingleReadData rd : dataset) {
                BamGenomeWindow w = openWindows.get(rd.getWindowStart());
                w.addReadData(rd);
            }
        }
    }

    public static BamGenomeWindow initWindow(String name,long windowStart,long windowEnd, byte[]reference, boolean detailed, boolean verbose){
		byte[]miniReference = null;
		if(reference!=null) {
			miniReference = new byte[(int)(windowEnd-windowStart+1)];
			miniReference = Arrays.copyOfRange(reference, (int) (windowStart - 1), (int) (windowEnd - 1));
		}

		if(detailed){
			return new BamDetailedGenomeWindow(name,windowStart,windowEnd,miniReference);
		} else {
			return new BamGenomeWindow(name,windowStart,windowEnd,miniReference);
		}
	}


    private static BamGenomeWindow nextWindow(BamStats bamStats, Map<Long,BamGenomeWindow> openWindows,byte[]reference,boolean detailed,boolean verbose){
		// init new current
		BamGenomeWindow currentWindow = null;

		if(bamStats.getNumberOfProcessedWindows() < bamStats.getNumberOfWindows()){
			int numProcessed = bamStats.getNumberOfProcessedWindows();
            long windowStart = bamStats.getWindowStart(numProcessed);

			if(windowStart <= bamStats.getReferenceSize()){
				long windowEnd = bamStats.getCurrentWindowEnd();
				if(openWindows.containsKey(windowStart)){
					currentWindow = openWindows.get(windowStart);
					//openWindows.remove(windowStart);
				} else {
					currentWindow = initWindow(bamStats.getCurrentWindowName(),windowStart,Math.min(windowEnd,bamStats.getReferenceSize()),reference,detailed,verbose);
					bamStats.incInitializedWindows();
                    openWindows.put(windowStart, currentWindow);
				}
			}
		}

		return currentWindow;
	}

    private BamGenomeWindow finalizeAndGetNextWindow(long position, BamGenomeWindow lastWindow,
                                                     Map<Long,BamGenomeWindow> openWindows,BamStats bamStats,byte[]reference,
                                                     boolean detailed, boolean verbose) throws CloneNotSupportedException{
        // position is still far away
        while(position > lastWindow.getEnd() ) {
            lastResult = finalizeWindow(lastWindow);
            /*try {
                lastResult.get();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            lastWindow = nextWindow(bamStats,openWindows,reference,detailed,verbose);
            if (lastWindow == null) {
                break;
            }

        }

        return lastWindow;
    }

    private Future<Integer> finalizeWindow(BamGenomeWindow window) {
        long windowStart = bamStats.getCurrentWindowStart();
        openWindows.remove(windowStart);
        bamStats.incProcessedWindows();
        return threadPool.submit( new FinalizeWindowTask(bamStats,window));
    }


    private void loadLocator(SAMFileHeader header){
		locator = new GenomeLocator();
		for(int i=0; i<header.getSequenceDictionary().getSequences().size(); i++){
			locator.addContig(header.getSequence(i).getSequenceName(), header.getSequence(i).getSequenceLength());
		}
	}


	private void loadReference() throws Exception{
		if(referenceAvailable){
			// prepare reader
			FastaReader reader = new FastaReader(referenceFile);

			// init buffer
			StringBuffer referenceBuffer = new StringBuffer();

			numberOfReferenceContigs = 0;
			Fasta contig;
			while((contig=reader.read())!=null){
				referenceBuffer.append(contig.getSeq().toUpperCase());
				numberOfReferenceContigs++;
			}

			reader.close();
			reference = referenceBuffer.toString().getBytes();
			referenceSize = reference.length;
			if(reference.length!=locator.getTotalSize()){
				throw new Exception("invalid reference file, number of nucleotides differs");
			}
		} else {
			referenceSize = locator.getTotalSize();
			numberOfReferenceContigs = locator.getContigs().size();
		}
	}


    private int computeWindowSize(long referenceSize, int numberOfWindows){
		int windowSize = (int)Math.floor((double)referenceSize/(double)numberOfWindows);
		if(((double)referenceSize/(double)numberOfWindows)>windowSize){
			windowSize++;
		}

		return windowSize;
	}


	private int computeEffectiveNumberOfWindows(long referenceSize, int windowSize){
		int numberOfWindows = (int)Math.floor((double)referenceSize/(double)windowSize);
		if(((double)referenceSize/(double)windowSize)>numberOfWindows){
			numberOfWindows++;
		}

		return numberOfWindows;
	}

    public GenomeLocator getLocator() {
        return locator;
    }

    byte[] getReference() {
        return reference;
    }

    public BamStats getBamStats() {
        return bamStats;
    }

    ConcurrentMap<Long,BamGenomeWindow> getOpenWindows() {
        return openWindows;
    }

    public boolean isPairedData() {
        return isPairedData;
    }
}
