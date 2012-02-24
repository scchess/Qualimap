package org.bioinfo.ngs.qc.qualimap.process;

import net.sf.samtools.*;
import net.sf.picard.util.IntervalTree;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.formats.core.feature.Gff;
import org.bioinfo.formats.core.feature.io.GffReader;
import org.bioinfo.formats.core.sequence.Fasta;
import org.bioinfo.formats.core.sequence.io.FastaReader;
import org.bioinfo.formats.exception.FileFormatException;
import org.bioinfo.ngs.qc.qualimap.beans.*;
import org.bioinfo.ngs.qc.qualimap.beans.BamDetailedGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats;
import org.bioinfo.ngs.qc.qualimap.beans.ContigRecord;
import org.bioinfo.ngs.qc.qualimap.beans.GenomeLocator;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.utils.ReadStartsHistogram;
import org.bioinfo.ngs.qc.qualimap.utils.RegionLookupTable;

import java.io.File;
import java.io.IOException;
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
	private int numberOfDuplicatedReads;
    private int numberOfPairedReads;
    private int numberOfSingletons;

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
    private int threadNumber;
    private int numReadsInBunch;

	// outside
	private boolean computeOutsideStats;
	private BamGenomeWindow currentOutsideWindow;
	private HashMap<Long,BamGenomeWindow> openOutsideWindows;
    private BamStats outsideBamStats;
	private int numberOfOutsideMappedReads;
    private int progress;

    // counting unique reads
    private ReadStartsHistogram readStartsHistogram;
    private ReadStartsHistogram readStartsHistogramOutside;

    // read size
    long acumReadSize;
    int maxReadSize;
    int minReadSize;

    //regions
	private long[] selectedRegionStarts;
	private long[] selectedRegionEnds;
    RegionLookupTable regionLookupTable;

	// insert size
	private boolean computeInsertSize;

	// chromosome
	private boolean computeChromosomeStats;
	private BamStats chromosomeStats;
    private BamGenomeWindow currentChromosome;
    private ConcurrentMap<Long,BamGenomeWindow> openChromosomeWindows;
    private ArrayList<Integer> chromosomeWindowIndexes;
    IntervalTree<Integer> regionsTree;

    private int maxSizeOfTaskQueue;

	// reporting
	private boolean activeReporting;
	private boolean saveCoverage;
	private boolean isPairedData;
    List<Future<ProcessBunchOfReadsTask.Result>> results;
    long timeToCalcOverlappers;

    private static Map<String,String> genomeGcContentMap;

    public static synchronized Map<String, String> getGcContentFileMap() {

        if (genomeGcContentMap == null) {
            genomeGcContentMap = new HashMap<String,String>();
            genomeGcContentMap.put("HUMAN genome (hg19)", "species/human.hg19.gc_histogram.txt");
        }

        return genomeGcContentMap;
    }

    private ExecutorService workerThreadPool;

    public BamStatsAnalysis(String bamFile) {
		this.bamFile = bamFile;
		this.numberOfWindows = 400;
        this.numReadsInBunch = 2000;
        this.maxSizeOfTaskQueue = 10;
        this.minReadSize = Integer.MAX_VALUE;
        this.threadNumber = 4;
        this.computeChromosomeStats = true;
        this.selectedRegionsAvailable =false;
        this.computeOutsideStats = false;
        this.outdir = ".";
		logger = new Logger();
        chromosomeWindowIndexes = new ArrayList<Integer>();
        readStartsHistogram = new ReadStartsHistogram();
        readStartsHistogramOutside = new ReadStartsHistogram();

    }

    public void run() throws Exception{

        long startTime = System.currentTimeMillis();

        workerThreadPool = Executors.newFixedThreadPool(threadNumber);

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

        // init window set
        windowSize = computeWindowSize(referenceSize,numberOfWindows);
        //effectiveNumberOfWindows = computeEffectiveNumberOfWindows(referenceSize,windowSize);
        List<Long> windowPositions = computeWindowPositions(windowSize);
        effectiveNumberOfWindows = windowPositions.size();
        bamStats = new BamStats("genome",referenceSize,effectiveNumberOfWindows);
        logger.println("effectiveNumberOfWindows " + effectiveNumberOfWindows);
        logger.println("Number of threads: " + threadNumber);
        bamStats.setSourceFile(bamFile);
        //bamStats.setWindowReferences("w",windowSize);
        bamStats.setWindowReferences("w", windowPositions);
        openWindows = new ConcurrentHashMap<Long,BamGenomeWindow>();

        //regions
        if(selectedRegionsAvailable){

			// load selected regions
            loadSelectedRegions();

            // outside of regions stats
            if (computeOutsideStats) {
                outsideBamStats = new BamStats("outside",referenceSize, effectiveNumberOfWindows);
                outsideBamStats.setSourceFile(bamFile);
                outsideBamStats.setWindowReferences("out_w",windowPositions);
                openOutsideWindows = new HashMap<Long,BamGenomeWindow>();
                currentOutsideWindow = nextWindow(outsideBamStats,openOutsideWindows,reference,true);

                if(activeReporting) {
                    outsideBamStats.activateWindowReporting(outdir + "/outside_window.txt");
                }

                if(saveCoverage){
                    outsideBamStats.activateCoverageReporting(outdir + "/outside_coverage.txt");
                }

                // We have twice more data from the bunch.
                // TODO: Is this really required? look at bug-126
                maxSizeOfTaskQueue /= 2;
            }

		}

        // chromosome stats
		if(computeChromosomeStats){
			chromosomeStats = new BamStats("chromosomes", referenceSize, numberOfReferenceContigs);
			chromosomeStats.setWindowReferences(locator);
			openChromosomeWindows = new ConcurrentHashMap<Long, BamGenomeWindow>();
			currentChromosome = nextWindow(chromosomeStats,openChromosomeWindows,reference,false);
		    chromosomeStats.activateWindowReporting(outdir + "/" + Constants.NAME_OF_FILE_CHROMOSOMES);
        }

        currentWindow = nextWindow(bamStats,openWindows,reference,true);

        // init working variables
		isPairedData = true;

		// run reads
        SAMRecordIterator iter = reader.iterator();

        ArrayList<SAMRecord> readsBunch = new ArrayList<SAMRecord>();
        results = new ArrayList<Future<ProcessBunchOfReadsTask.Result>>();

        timeToCalcOverlappers = 0;

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

            // compute absolute position
            long position = locator.getAbsoluteCoordinates(read.getReferenceName(),read.getAlignmentStart());

            //compute read size
            int readSize = read.getReadLength();
            acumReadSize += readSize;
            if (readSize > maxReadSize) {
                maxReadSize = readSize;
            }
            if (readSize < minReadSize) {
                minReadSize = readSize;
            }

            ++numberOfReads;

			// filter invalid reads
			if(read.isValid() == null){
                 if (read.getDuplicateReadFlag()) {
                    numberOfDuplicatedReads++;
                 }
                 // accumulate only mapped reads
				if(read.getReadUnmappedFlag()) {
                    continue;
                }
                if (read.getReadPairedFlag()) {
                    numberOfPairedReads++;
                    if (read.getMateUnmappedFlag()) {
                        numberOfSingletons++;
                    }
                }

                long findOverlappersStart = System.currentTimeMillis();

                if (selectedRegionsAvailable) {
                    //boolean readOverlapsRegions = readOverlapsRegions(position, position + read.getReadLength() - 1);
                    boolean readOverlapsRegions = readOverlapsRegions(read.getAlignmentStart(),
                            read.getAlignmentEnd(), read.getReferenceName());

                    if (readOverlapsRegions) {
                        numberOfMappedReads++;
                        readStartsHistogram.update(position);
                    } else {
                        numberOfOutsideMappedReads++;
                        readStartsHistogramOutside.update(position);
                    }
                } else {
                    numberOfMappedReads++;
                    readStartsHistogram.update(position);
                }

                timeToCalcOverlappers += System.currentTimeMillis() - findOverlappersStart;

                if (computeChromosomeStats && position > currentChromosome.getEnd()) {
                    collectAnalysisResults(readsBunch);
                    currentChromosome = finalizeAndGetNextWindow(position, currentChromosome,
                            openChromosomeWindows, chromosomeStats, reference, false);
                }

                // finalize current and get next window
			    if(position > currentWindow.getEnd() ){
                    //analyzeReads(readsBunch);
                    collectAnalysisResults(readsBunch);
                    //finalize
				    currentWindow = finalizeAndGetNextWindow(position,currentWindow,openWindows,
                            bamStats,reference,true);

                    if (selectedRegionsAvailable && computeOutsideStats) {
                        currentOutsideWindow.inverseRegions();
                        currentOutsideWindow = finalizeAndGetNextWindow(position,currentOutsideWindow,
                                                    openOutsideWindows, outsideBamStats,reference,true);
                    }

                }

                if (currentWindow == null) {
                    //Some reads are out of reference bounds?
                    break;
                }

                readsBunch.add(read);
                if (readsBunch.size() >= numReadsInBunch) {
                    if (results.size() >= maxSizeOfTaskQueue )  {
                        //System.out.println("Max size of task queue is exceeded!");
                        collectAnalysisResults(readsBunch);
                    } else {
                        analyzeReadsBunch(readsBunch);
                    }
                }

                numberOfValidReads++;

            }

        }

        // close stream
        reader.close();

        if (!readsBunch.isEmpty()) {
            int numWindows = bamStats.getNumberOfWindows();
            long lastPosition = bamStats.getWindowEnd(numWindows - 1) + 1;
            collectAnalysisResults(readsBunch);
            //finalize
            finalizeAndGetNextWindow(lastPosition,currentWindow,openWindows,bamStats,reference,true);
            if (computeChromosomeStats) {
                finalizeAndGetNextWindow(lastPosition,currentChromosome, openChromosomeWindows,
                    chromosomeStats, reference, false);
            }
            if (selectedRegionsAvailable && computeOutsideStats) {
                currentOutsideWindow.inverseRegions();
                finalizeAndGetNextWindow(lastPosition,currentOutsideWindow, openOutsideWindows,
                                outsideBamStats, reference, true);
            }
        }

        workerThreadPool.shutdown();
        workerThreadPool.awaitTermination(2, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();

        logger.println("Number of reads: " + numberOfReads);
        logger.println("Number of mapped reads: " + numberOfMappedReads);
        logger.println("Number of valid reads: " + numberOfValidReads);
        logger.println("Number of duplicated reads: " + numberOfDuplicatedReads);
        logger.println("Time taken to analyze reads: " + (endTime - startTime) / 1000);

        // summarize

        percentageOfValidReads = ((double)numberOfValidReads/(double)numberOfReads)*100.0;
        bamStats.setNumberOfReads(numberOfReads);
        if (selectedRegionsAvailable) {
            bamStats.setNumSelectedRegions(numberOfSelectedRegions);
            bamStats.setInRegionReferenceSize(insideReferenceSize);
            int totalNumberOfMappedReads = numberOfMappedReads + numberOfOutsideMappedReads;
            bamStats.setNumberOfMappedReads(totalNumberOfMappedReads);
            bamStats.setPercentageOfMappedReads( (totalNumberOfMappedReads / (double) numberOfReads) * 100.0);
            bamStats.setNumberOfInsideMappedReads(numberOfMappedReads);
            bamStats.setPercentageOfInsideMappedReads( (numberOfMappedReads / (double) totalNumberOfMappedReads) * 100.0);
            bamStats.setNumberOfOutsideMappedReads(numberOfOutsideMappedReads);
            bamStats.setPercentageOfOutsideMappedReads((numberOfOutsideMappedReads / (double) totalNumberOfMappedReads) * 100.0);
        } else {
            bamStats.setNumberOfMappedReads(numberOfMappedReads);
            bamStats.setPercentageOfMappedReads((numberOfMappedReads/(double)numberOfReads)*100.0);
        }
        bamStats.setNumberOfPairedReads(numberOfPairedReads);
        bamStats.setPercentageOfPairedReads( (numberOfPairedReads / (double)numberOfReads)*100.0 );
        bamStats.setNumberOfSingletons(numberOfSingletons);
        bamStats.setPercentageOfSingletons( (numberOfSingletons / (double) numberOfReads)*100.0 );
        bamStats.setPercentageOfValidReads(percentageOfValidReads);
        bamStats.setReferenceSize(referenceSize);
        bamStats.setUniqueReadStarts(readStartsHistogram.getHistorgram());
        bamStats.setReadMaxSize(maxReadSize);
        bamStats.setReadMinSize(minReadSize);
        bamStats.setReadMeanSize( acumReadSize / (double) numberOfReads );

        // compute descriptors
        logger.println("Computing descriptors...");
		bamStats.computeDescriptors();
        // compute histograms
		logger.println("Computing histograms...");
		bamStats.computeHistograms();

        if(selectedRegionsAvailable && computeOutsideStats){
            outsideBamStats.setReferenceSize(referenceSize);
            outsideBamStats.setNumSelectedRegions(numberOfSelectedRegions);
            outsideBamStats.setInRegionReferenceSize(insideReferenceSize);
            outsideBamStats.setNumberOfReads(numberOfReads);
            int totalNumberOfMappedReads = numberOfMappedReads + numberOfOutsideMappedReads;
            outsideBamStats.setNumberOfMappedReads(totalNumberOfMappedReads);
            outsideBamStats.setPercentageOfMappedReads( (totalNumberOfMappedReads / (double) numberOfReads) * 100.0);
            outsideBamStats.setNumberOfInsideMappedReads(numberOfMappedReads);
            outsideBamStats.setPercentageOfInsideMappedReads( (numberOfMappedReads / (double) totalNumberOfMappedReads) * 100.0);
            outsideBamStats.setNumberOfOutsideMappedReads(numberOfOutsideMappedReads);
            outsideBamStats.setPercentageOfOutsideMappedReads((numberOfOutsideMappedReads / (double) totalNumberOfMappedReads) * 100.0);logger.println("Computing descriptors for outside regions...");
            outsideBamStats.setNumberOfPairedReads(numberOfPairedReads);
            outsideBamStats.setPercentageOfPairedReads( (numberOfPairedReads / (double)numberOfReads)*100.0 );
            outsideBamStats.setNumberOfSingletons(numberOfSingletons);
            outsideBamStats.setPercentageOfSingletons( (numberOfSingletons / (double) numberOfReads)*100.0 );
      	    outsideBamStats.setUniqueReadStarts(readStartsHistogramOutside.getHistorgram());
            outsideBamStats.computeDescriptors();
            logger.println("Computing histograms for outside regions...");
		    outsideBamStats.computeHistograms();
        }

        long overallTime = System.currentTimeMillis();
        logger.println("Overall analysis time: " + (overallTime - startTime) / 1000);

    }

    private static List<SAMRecord> getShallowCopy(List<SAMRecord> list) {
        ArrayList<SAMRecord> result = new ArrayList<SAMRecord>(list.size());

        for (SAMRecord r : list)  {
            result.add(r);
        }

        return result;

    }

    private void analyzeReadsBunch( ArrayList<SAMRecord> readsBunch ) throws ExecutionException, InterruptedException {
         List<SAMRecord> bunch = getShallowCopy(readsBunch);
         Callable<ProcessBunchOfReadsTask.Result> task = new ProcessBunchOfReadsTask(bunch,currentWindow, this);
         Future<ProcessBunchOfReadsTask.Result> result = workerThreadPool.submit(task);
         results.add( result );
         readsBunch.clear();
    }

    private void collectAnalysisResults(ArrayList<SAMRecord> readsBunch) throws InterruptedException, ExecutionException {

        // start last bunch
        analyzeReadsBunch(readsBunch);

        // wait till all tasks are finished
        for (Future<ProcessBunchOfReadsTask.Result> result : results) {
            ProcessBunchOfReadsTask.Result taskResult = result.get();
            Collection<SingleReadData> dataset = taskResult.getGlobalReadsData();
            for (SingleReadData rd : dataset) {
                BamGenomeWindow w = openWindows.get(rd.getWindowStart());
                w.addReadData(rd);
                if (computeChromosomeStats) {
                    currentChromosome.addReadData(rd);
                }
            }
            bamStats.addGcContentData( taskResult.getReadsGcContent() );
            bamStats.addReadsAsData ( taskResult.getReadsAContent() );
            bamStats.addReadsCsData ( taskResult.getReadsCContent() );
            bamStats.addReadsGsData ( taskResult.getReadsGContent() );
            bamStats.addReadsTsData ( taskResult.getReadsTContent() );
            bamStats.addReadsNsData ( taskResult.getReadsNContent() );


            if (selectedRegionsAvailable && computeOutsideStats) {
                Collection<SingleReadData> outsideData = taskResult.getOutOfRegionReadsData();
                for (SingleReadData rd : outsideData) {
                    BamGenomeWindow w = getOpenWindow(rd.getWindowStart(),
                            outsideBamStats,
                            openOutsideWindows);
                    w.addReadData(rd);
                }
                outsideBamStats.addGcContentData( taskResult.getOutRegionOfReadsGcContent());
            }
        }

        results.clear();

    }


    public BamGenomeWindow getOpenWindow(long windowStart,BamStats bamStats,
                                         Map<Long,BamGenomeWindow> openWindows) {
        BamGenomeWindow window;
        if(openWindows.containsKey(windowStart)){
            window = openWindows.get(windowStart);
        } else {
            int numInitWindows = bamStats.getNumberOfInitializedWindows();
            String windowName = bamStats.getWindowName(numInitWindows);
            long windowEnd = bamStats.getWindowEnd(numInitWindows);
            window = initWindow(windowName, windowStart,
                    Math.min(windowEnd, bamStats.getReferenceSize()), reference, true);
            bamStats.incInitializedWindows();
            openWindows.put(windowStart,window);
        }

        return window;
    }

    //TODO: try using this method for better performance
    /*private void calculateRegionsLookUpTableForWindowNew(BamGenomeWindow w) {

        int windowSize = (int) w.getWindowSize();

        BitSet bitSet = new BitSet((int)w.getWindowSize());

        ContigRecord windowContig = locator.getContigCoordinates(w.getStart());

        int relativeWindowStart = (int) windowContig.getRelative();
        int relativeWindowEnd = relativeWindowStart + windowSize - 1;
        String contigName = windowContig.getName();

        regionLookupTable.markIntersectingRegions(bitSet, relativeWindowStart,
                relativeWindowEnd, contigName);

        w.setSelectedRegions(bitSet);
        w.setSelectedRegionsAvailable(true);
    }*/


    private void calculateRegionsLookUpTableForWindow(BamGenomeWindow w) {

        long windowStart = w.getStart();
        long windowEnd = w.getEnd();

        BitSet bitSet = new BitSet((int)w.getWindowSize());

        int numRegions = selectedRegionStarts.length;
        for (int i = 0; i < numRegions; ++i) {
            long regionStart = selectedRegionStarts[i];
            long regionEnd = selectedRegionEnds[i];

            if ( regionStart >= windowStart && regionStart <= windowEnd ) {
                //System.out.println("Have match! Type1 " + w.getName());
                long end = Math.min(windowEnd,regionEnd);
                bitSet.set((int)(regionStart-windowStart), (int)(end-windowStart + 1),true);
            } else if (regionEnd >= windowStart && regionEnd <= windowEnd) {
                //System.out.println("Have match! Type2 " + w.getName());
                bitSet.set(0, (int)(regionEnd - windowStart + 1), true);
            } else if (regionStart <= windowStart && regionEnd >= windowEnd) {
                //System.out.println("Have match! Type3 " + w.getName());
                bitSet.set(0, (int)(windowEnd - windowStart + 1),true);
            }

        }

        w.setSelectedRegions(bitSet);
        w.setSelectedRegionsAvailable(true);
    }



    public BamGenomeWindow initWindow(String name,long windowStart,long windowEnd, byte[]reference,
                                             boolean detailed){

        byte[]miniReference = null;
		if(reference!=null) {
			miniReference = new byte[(int)(windowEnd-windowStart+1)];
			miniReference = Arrays.copyOfRange(reference, (int) (windowStart - 1), (int) (windowEnd - 1));
		}

        BamGenomeWindow w = detailed ? new BamDetailedGenomeWindow(name,windowStart,windowEnd,miniReference) :
                    new BamGenomeWindow(name,windowStart,windowEnd,miniReference);

        if (selectedRegionsAvailable) {
            calculateRegionsLookUpTableForWindow(w);
        }

        return w;
	}


    private BamGenomeWindow nextWindow(BamStats bamStats, Map<Long,BamGenomeWindow> openWindows,byte[]reference,boolean detailed){
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
					currentWindow = initWindow(bamStats.getCurrentWindowName(),windowStart,Math.min(windowEnd,bamStats.getReferenceSize()),reference,detailed);
					bamStats.incInitializedWindows();
                    openWindows.put(windowStart, currentWindow);
				}
			}
		}

		return currentWindow;
	}

    private BamGenomeWindow finalizeAndGetNextWindow(long position, BamGenomeWindow lastWindow,
                                                     Map<Long,BamGenomeWindow> openWindows,BamStats bamStats,
                                                     byte[]reference, boolean detailed)
            throws CloneNotSupportedException, ExecutionException, InterruptedException {
        // position is still far away
        while(position > lastWindow.getEnd() ) {
            updateProgress();
            //finalizeWindowInSameThread(lastWindow);
            finalizeWindow(lastWindow, bamStats, openWindows);
            lastWindow = nextWindow(bamStats,openWindows,reference,detailed);
            if (lastWindow == null) {
                break;
            }

        }

        return lastWindow;
    }


    private void updateProgress() {
        if (!computeOutsideStats) {
            progress = (bamStats.getNumberOfProcessedWindows() * 100) / effectiveNumberOfWindows;
        } else {
            progress = ((bamStats.getNumberOfProcessedWindows() +
                    outsideBamStats.getNumberOfProcessedWindows()) * 50) / (effectiveNumberOfWindows);
        }
    }
    private Future<Integer> finalizeWindow(BamGenomeWindow window, BamStats bamStats,
                                           Map<Long,BamGenomeWindow> openWindows) {
        long windowStart = bamStats.getCurrentWindowStart();
        openWindows.remove(windowStart);
        bamStats.incProcessedWindows();
        //System.out.println("Time taken to count overlappers: " + timeToCalcOverlappers);
        timeToCalcOverlappers = 0;
        return workerThreadPool.submit( new FinalizeWindowTask(bamStats,window));
    }


    private static Integer finalizeWindowInSameThread(BamGenomeWindow window,BamStats bamStats,
                                               Map<Long,BamGenomeWindow> openWindows) {
        long windowStart = bamStats.getCurrentWindowStart();
        openWindows.remove(windowStart);
        bamStats.incProcessedWindows();
        FinalizeWindowTask task = new FinalizeWindowTask(bamStats,window);
        return task.call();
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
			StringBuilder referenceBuffer = new StringBuilder();

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

    private void loadSelectedRegions() throws SecurityException, IOException, NoSuchMethodException, FileFormatException {

		// init gff reader
		numberOfSelectedRegions = 0;
		Gff region;
		GffReader gffReader = new GffReader(gffFile);
		System.out.println("initializing regions from " + gffFile + ".....");
		while((region = gffReader.read())!=null){
			/*if (!region.getFeature().equalsIgnoreCase("exon")) {
                continue;
            }*/
            numberOfSelectedRegions++;
		}
		gffReader.close();
		if (numberOfSelectedRegions == 0) {
            throw new RuntimeException("Failed to load selected regions.");
        }
        System.out.println("found " + numberOfSelectedRegions + " regions");
		System.out.println("initializing memory... ");

		selectedRegionStarts = new long[numberOfSelectedRegions];
		selectedRegionEnds = new long[numberOfSelectedRegions];
        regionLookupTable = new RegionLookupTable();
		//selectedRegionRelativePositions = new long[numberOfSelectedRegions];
        regionsTree = new IntervalTree<Integer>();

		System.out.println("filling region references... ");
		gffReader = new GffReader(gffFile);
		long relative = 0;
		int index = 0;
		long pos;
		long lastEnd = 0;
        insideReferenceSize = 0;
		while((region = gffReader.read())!=null){
            /*if (!region.getFeature().equalsIgnoreCase("exon")) {
                continue;
            }*/
            pos = locator.getAbsoluteCoordinates(region.getSequenceName(),region.getStart());
	        int regionLength = region.getEnd() - region.getStart() + 1;
            insideReferenceSize += regionLength;
            selectedRegionStarts[index] = pos;
            selectedRegionEnds[index] = pos + regionLength - 1;
            regionLookupTable.putRegion(region.getStart(), region.getEnd(), region.getSequenceName());

			//selectedRegionStarts[index] = Math.max(lastEnd,pos);
			//selectedRegionEnds[index] = Math.max(lastEnd,pos + region.getEnd()-region.getStart());
			//selectedRegionRelativePositions[index] = relative;
            //regionsTree.put(region.getStart(), region.getEnd(), index);
			//relative+=(selectedRegionEnds[index]-selectedRegionStarts[index]+1);
			//lastEnd = selectedRegionEnds[index];
			//System.err.println(region.getStart() + ":" + region.getEnd() + "       " + pos + ":" + (pos + region.getEnd()-region.getStart()) + "      "  + selectedRegionStarts[index] + ":" + selectedRegionEnds[index] +  "     " + relative);
			index++;
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

    private ArrayList<Long> computeWindowPositions(int windowSize){
        List<ContigRecord> contigs = locator.getContigs();
        ArrayList<Long> windowStarts = new ArrayList<Long>();

        long startPos = 1;
        int i = 0;
        int numContigs  = contigs.size();
        while (startPos < referenceSize) {
            windowStarts.add(startPos);
            startPos += windowSize;

            while (i < numContigs) {
                long nextContigStart =  contigs.get(i).getEnd() + 1;
                if (startPos >= nextContigStart ) {
                    if (startPos > nextContigStart && nextContigStart < referenceSize) {
                        //System.out.println("Chromosome window break: " + (windowStarts.size() + 1));
                        windowStarts.add(nextContigStart);
                        System.out.println("window start: " + nextContigStart);
                    }
                    chromosomeWindowIndexes.add(i);
                    i++;
                } else {
                    break;
                }
            }
        }

        return windowStarts;
    }

    private boolean readOverlapsRegions(int readStart, int readEnd, String seqName) {

        return regionLookupTable.overlaps(readStart, readEnd, seqName);
    }


    public GenomeLocator getLocator() {
        return locator;
    }

    public BamStats getBamStats() {
        return bamStats;
    }

    public BamStats getOutsideBamStats() {
        return outsideBamStats;
    }

    ConcurrentMap<Long,BamGenomeWindow> getOpenWindows() {
        return openWindows;
    }

    public boolean isPairedData() {
        return isPairedData;
    }

    public boolean selectedRegionsAvailable() {
        return selectedRegionsAvailable;
    }

    public void setNumberOfWindows(int windowsNum) {
        numberOfWindows = windowsNum;
    }

    public void setComputeInsertSize(boolean computeInsertSize) {
        this.computeInsertSize = computeInsertSize;
    }

    public void setReferenceFile(String referenceFile) {
        this.referenceFile = referenceFile;
    }

    public void activeReporting(String outdir){
		this.outdir = outdir;
		this.activeReporting = true;
	}

    public void setComputeChromosomeStats(boolean computeChromosomeStats) {
        this.computeChromosomeStats = computeChromosomeStats;
    }

    public void setSelectedRegions(String gffFile){
		this.gffFile = gffFile;
		selectedRegionsAvailable = true;
	}

    public void setComputeOutsideStats(boolean computeOutsideStats) {
        this.computeOutsideStats = computeOutsideStats;
    }

    public boolean getComputeOutsideStats() {
        return computeOutsideStats;
    }

    public void setNumberOfThreads(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    public int getProgress() {
        return progress;
    }

}
