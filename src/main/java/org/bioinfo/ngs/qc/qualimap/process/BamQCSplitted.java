package org.bioinfo.ngs.qc.qualimap.process;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.formats.core.feature.Gff;
import org.bioinfo.formats.core.feature.io.GffReader;
import org.bioinfo.formats.core.sequence.Fasta;
import org.bioinfo.formats.core.sequence.io.FastaReader;
import org.bioinfo.formats.exception.FileFormatException;
import org.bioinfo.ngs.qc.qualimap.beans.BamDetailedGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCInsideOutsideAlignment;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats;
import org.bioinfo.ngs.qc.qualimap.beans.GenomeLocator;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;


public class BamQCSplitted {
	public static final int DEFAULT_NUMBER_OF_WINDOWS = 500;
	
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
	
	// statistics
	private BamStats bamStats; 
	
	private Logger logger;
	
	// working variables
	private BamGenomeWindow currentWindow;
	private HashMap<Long,BamGenomeWindow> openWindows;
	
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
	
	/** Variable to manage the last action done in the program to show it
	 * to the user. */
	private String lastActionDone;
	
	public BamQCSplitted(String bamFile){
		this.bamFile = bamFile;		
		this.numberOfWindows = DEFAULT_NUMBER_OF_WINDOWS;
		logger = new Logger();
	}
	
	public BamQCSplitted(String bamFile, String referenceFile){
		this.bamFile = bamFile;
		this.referenceFile = referenceFile;
		this.referenceAvailable = true;		
		this.numberOfWindows = DEFAULT_NUMBER_OF_WINDOWS;
		logger = new Logger();
	}
	
	public void activeReporting(String outdir){
		this.outdir = outdir;
		this.activeReporting = true;	
	}
	
	public void activeCoverageReporting(){		
		this.saveCoverage = true;
	}
	
	public void setSelectedRegions(String gffFile){
		this.gffFile = gffFile;
		selectedRegionsAvailable = true;		
	}

	public void run() throws Exception{
		String prefix = "w";
		
		// init reader
		SAMFileReader reader = new SAMFileReader(new File(bamFile));
		
		// org.bioinfo.ntools.process header
		lastActionDone = "loading sam header header";
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
		effectiveNumberOfWindows = computeEffectiveNumberOfWindows(referenceSize,windowSize);
		bamStats = new BamStats("genome",referenceSize,effectiveNumberOfWindows);
		logger.println("effectiveNumberOfWindows " + effectiveNumberOfWindows);
		bamStats.setSourceFile(bamFile);
		bamStats.setWindowReferences(prefix,windowSize);
		openWindows = new HashMap<Long,BamGenomeWindow>();
		currentWindow = nextWindow(bamStats,openWindows,reference,true,true);
		
		if(activeReporting) {
			bamStats.activateWindowReporting(outdir + "/genome_window.txt");
		}
		
		if(saveCoverage){
			bamStats.activateCoverageReporting(outdir + "/genome_coverage.txt");
		}
		
		if(selectedRegionsAvailable){
			
			// load selected regions
			loadSelectedRegions();
			
			//Thread.sleep(40000);
			
			// inside			
			insideWindowSize = computeWindowSize(insideReferenceSize, numberOfWindows);
			effectiveInsideNumberOfWindows = computeEffectiveNumberOfWindows(insideReferenceSize, insideWindowSize);
			insideBamStats = new BamStats("inside",insideReferenceSize,effectiveInsideNumberOfWindows);
			insideBamStats.setSourceFile(bamFile);
			insideBamStats.setWindowReferences("in_" + prefix,insideWindowSize);
			openInsideWindows = new HashMap<Long,BamGenomeWindow>();
			currentInsideWindow = nextWindow(insideBamStats,openInsideWindows,null,true,true);
			if(activeReporting) {
				insideBamStats.activateWindowReporting(outdir + "/inside_window.txt");
			}
			if(saveCoverage){
				insideBamStats.activateCoverageReporting(outdir + "/inside_coverage.txt");
			}
			
			if(computeOutsideStats){
				// outside			
				outsideWindowSize = computeWindowSize(outsideReferenceSize, numberOfWindows);
				effectiveOutsideNumberOfWindows = computeEffectiveNumberOfWindows(outsideReferenceSize, outsideWindowSize);
				outsideBamStats = new BamStats("outside",outsideReferenceSize,effectiveOutsideNumberOfWindows);
				outsideBamStats.setSourceFile(bamFile);
				outsideBamStats.setWindowReferences("out_" + prefix,outsideWindowSize);
				openOutsideWindows = new HashMap<Long,BamGenomeWindow>();			
				currentOutsideWindow = nextWindow(outsideBamStats,openOutsideWindows,null,true,true);
				
				if(activeReporting) {
					outsideBamStats.activateWindowReporting(outdir + "/outside_window.txt");
				}
				
				if(saveCoverage){
					outsideBamStats.activateCoverageReporting(outdir + "/outside_coverage.txt");
				}
			}
			
		}
	
		// chromosome stats
		BamGenomeWindow currentChromosome = null;
		HashMap<Long, BamGenomeWindow> openChromosomeWindows = null; 
		if(computeChromosomeStats){
			chromosomeStats = new BamStats("chromosomes", referenceSize, numberOfReferenceContigs);
			chromosomeStats.setWindowReferences(locator);
			openChromosomeWindows = new HashMap<Long, BamGenomeWindow>();
			currentChromosome = nextWindow(chromosomeStats,openChromosomeWindows,null,false,false);
			chromosomeStats.activateWindowReporting(outdir + "/" + Constants.NAME_OF_FILE_CHROMOSOMES);
		}
		
		// init working variables
		long position = 0;
		int maxConcurrentOpenWindow = 0;
		String alignment;
		long insideReadStart,outsideReadStart,insideReadEnd,outsideReadEnd;
		boolean outOfBounds;		
		int currentRegion = 0;
		int insertSize;
		isPairedData = true;
		
		// run reads
		for(SAMRecord read:reader){
			// filter invalid reads
			if(read.isValid()==null && !read.getDuplicateReadFlag()){
				// compute alignment
				alignment = BamGenomeWindow.computeAlignment(read);
				
				// isize
	
				try {
					if(computeInsertSize && read.getProperPairFlag()){
						insertSize = read.getInferredInsertSize();
					} else {
						insertSize = -1;
					}
				} catch(IllegalStateException ise){
					insertSize = -1;
					isPairedData = false;
				}
				
				// acum mapped reads
				if(!read.getReadUnmappedFlag()) numberOfMappedReads++;
			
				// compute absolute position
				position = locator.getAbsoluteCoordinates(read.getReferenceName(),read.getAlignmentStart());
				
				// something strange has happend?
				if(position!=-1) {
					
					// chromosome
					if(computeChromosomeStats){
						if(position>currentChromosome.getEnd()){
							currentChromosome = finalizeAndGetNextWindow(position,currentChromosome,openChromosomeWindows,chromosomeStats,null,false,false);				
						}
						
						if(currentChromosome!=null){
							// acum read
							outOfBounds = currentChromosome.acumRead(read,alignment,locator);					
							if(outOfBounds) {							
								propagateRead(alignment,position,position+alignment.length()-1,read.getMappingQuality(),insertSize,currentChromosome,chromosomeStats,openChromosomeWindows,null,false,false);
							}
						}
					}
					
					// finalize current and get next window
					if(position>currentWindow.getEnd()){
						currentWindow = finalizeAndGetNextWindow(position,currentWindow,openWindows,bamStats,reference,true,true);				
					}
					
					if(currentWindow!=null){
						// acum read
						outOfBounds = currentWindow.acumRead(read,alignment,locator);
						if(outOfBounds) propagateRead(alignment,position,position+alignment.length()-1,read.getMappingQuality(),insertSize,currentWindow,bamStats,openWindows,reference,true,true);
					}
					
					if(selectedRegionsAvailable){
						while(currentRegion<(numberOfSelectedRegions-1) && position>selectedRegionStarts[currentRegion+1]){
							currentRegion++;
						}
												
						BamQCInsideOutsideAlignment ioa = new BamQCInsideOutsideAlignment();
						ioa.computeInsideAndOutsideAlignment(position,alignment,currentRegion,selectedRegionStarts,selectedRegionEnds,referenceSize);
						
						// inside
						if(ioa.getInsideAlignment().length()>0) {
							if(ioa.getInsideRegion()==-1){
								insideReadStart = 0;								
							} else {
								insideReadStart = selectedRegionRelativePositions[ioa.getInsideRegion()] + (ioa.getInsideReadStart() - selectedRegionStarts[ioa.getInsideRegion()]);								
							}
							insideReadEnd = insideReadStart+ioa.getInsideAlignment().length()-1;
							
							// finalize current and get next window
							if(currentInsideWindow!=null && insideReadStart>currentInsideWindow.getEnd()){								
								currentInsideWindow = finalizeAndGetNextWindow(insideReadStart,currentInsideWindow,openInsideWindows,insideBamStats,null,false,true);
							}							
							// acum read
							if(currentInsideWindow!=null){
								outOfBounds = currentInsideWindow.acumRead(ioa.getInsideAlignment(),insideReadStart,insideReadEnd,read.getMappingQuality(),insertSize);					
								if(outOfBounds) propagateRead(ioa.getInsideAlignment(),insideReadStart,insideReadEnd,read.getMappingQuality(),insertSize,currentInsideWindow,insideBamStats,openInsideWindows,null,true,false);
							}
						
							numberOfInsideMappedReads++;
						}
						
						// outside
						if(computeOutsideStats){
							if(ioa.getOutsideAlignment().length()>0) {							
								if(ioa.getOutsideRegion()==-1){
									outsideReadStart = ioa.getOutsideReadStart();							}
								else {
									outsideReadStart = (selectedRegionStarts[ioa.getOutsideRegion()]-selectedRegionRelativePositions[ioa.getOutsideRegion()]) + (ioa.getOutsideReadStart() - selectedRegionEnds[ioa.getOutsideRegion()] - 1);
								}
								outsideReadEnd = outsideReadStart + ioa.getOutsideAlignment().length()-1;
								
								// finalize current and get next window											
								if(currentOutsideWindow!=null && outsideReadStart>currentOutsideWindow.getEnd()){
									currentOutsideWindow = finalizeAndGetNextWindow(outsideReadStart,currentOutsideWindow,openOutsideWindows,outsideBamStats,null,false,true);
								}
								
								// acum read
								if(currentOutsideWindow!=null){
									outOfBounds = currentOutsideWindow.acumRead(ioa.getOutsideAlignment(),outsideReadStart,outsideReadEnd,read.getMappingQuality(),insertSize);					
									if(outOfBounds) propagateRead(ioa.getOutsideAlignment(),outsideReadStart,outsideReadEnd,read.getMappingQuality(),insertSize,currentOutsideWindow,outsideBamStats,openOutsideWindows,null,true,false);
								}
								
								numberOfOutsideMappedReads++;
							}
						}
					}
					
					numberOfValidReads++;
				}
			}
			
			numberOfReads++;

			// summaries
			if(maxConcurrentOpenWindow<openWindows.size()){
				maxConcurrentOpenWindow = (openWindows.size() + 1);
			}
			
		}
		
		// close stream	
		reader.close();
		
		// close last current windows
		currentWindow = finalizeAndGetNextWindow(bamStats.getReferenceSize(), currentWindow, openWindows, bamStats, reference, true, true);
		
		if(currentWindow!=null){
			finalizeWindow(currentWindow,bamStats,true);
		}
		
		if(selectedRegionsAvailable){
			currentInsideWindow = finalizeAndGetNextWindow(insideBamStats.getReferenceSize(),currentInsideWindow,openInsideWindows,insideBamStats,null,false,true);
			if(currentInsideWindow!=null){
				finalizeWindow(currentInsideWindow,insideBamStats,false);
			}
			if(computeOutsideStats){
				currentOutsideWindow = finalizeAndGetNextWindow(outsideBamStats.getReferenceSize(),currentOutsideWindow,openOutsideWindows,outsideBamStats,null,false,true);
				if(currentOutsideWindow!=null){
					finalizeWindow(currentOutsideWindow,outsideBamStats,false);
				}
			}
		}
		
		if(computeChromosomeStats){
			currentChromosome = finalizeAndGetNextWindow(chromosomeStats.getReferenceSize(),currentChromosome,openChromosomeWindows,chromosomeStats,null,false,false);
			if(currentChromosome!=null){
				finalizeWindow(currentChromosome,chromosomeStats,false);
			}
		}
		
		logger.println("");
						
		// summarize
		percentageOfValidReads = ((double)numberOfValidReads/(double)numberOfReads)*100.0;
		bamStats.setNumberOfReads(numberOfReads);
		bamStats.setNumberOfMappedReads(numberOfMappedReads);
		bamStats.setPercentageOfMappedReads(((double)numberOfMappedReads/(double)numberOfReads)*100.0);
		bamStats.setPercentageOfValidReads(percentageOfValidReads);
		
		if(selectedRegionsAvailable){
			// inside
			insideBamStats.setNumberOfReads(numberOfReads);
			insideBamStats.setNumberOfMappedReads(numberOfInsideMappedReads);
			insideBamStats.setPercentageOfMappedReads(((double)numberOfInsideMappedReads/(double)numberOfReads)*100.0);
			insideBamStats.setPercentageOfValidReads(percentageOfValidReads);
			if(computeOutsideStats){
				// outside
				outsideBamStats.setNumberOfReads(numberOfReads);
				outsideBamStats.setNumberOfMappedReads(numberOfOutsideMappedReads);
				outsideBamStats.setPercentageOfMappedReads(((double)numberOfOutsideMappedReads/(double)numberOfReads)*100.0);
				outsideBamStats.setPercentageOfValidReads(percentageOfValidReads);
			}
		}
		
		// compute descriptors
		logger.print("Computing descriptors...");
		bamStats.computeDescriptors();
		if(selectedRegionsAvailable){
			insideBamStats.computeDescriptors();
			if(computeOutsideStats){
				outsideBamStats.computeDescriptors();
			}
		}
		
		logger.println("OK");
		
		// compute histograms
		logger.print("Computing histograms...");
		bamStats.computeCoverageHistogram();
		if(selectedRegionsAvailable){
			insideBamStats.computeCoverageHistogram();
			if(computeOutsideStats){
				outsideBamStats.computeCoverageHistogram();
			}
		}
		
		logger.println("OK");
		
		if(computeChromosomeStats){
			chromosomeStats.computeDescriptors();
		}
	}
	
	public boolean isPairedData() {
    	return isPairedData;
    }

	public void setPairedData(boolean isPairedData) {
    	this.isPairedData = isPairedData;
    }

	/** 
	 * Function to calculate percent of qualimap ran 
	 * @return double value of the percent ran
	 */
	public double getProgress(){
		if(bamStats!=null){
			return ((double)(bamStats.getNumberOfProcessedWindows())/(double)(bamStats.getNumberOfWindows()))*100.0;
		} else {
			return 0;
		}
	}
	
//	private void initSelectedRegions() throws SecurityException, IOException, NoSuchMethodException, FileFormatException{		
//		System.out.println("initializing regions from " + gffFile + ".....");
//		numberOfSelectedRegions = RegionManager.countRegions(gffFile);
//		System.out.println("found " + numberOfSelectedRegions + " regions");
//	}
	
	private void loadSelectedRegions() throws SecurityException, IOException, NoSuchMethodException, FileFormatException{
		
		// init gff reader
		numberOfSelectedRegions = 0;		
		Gff region;		
		GffReader gffReader = new GffReader(gffFile);	
		System.out.println("initializing regions from " + gffFile + ".....");
		while((region = gffReader.read())!=null){
			numberOfSelectedRegions++;
		}
		gffReader.close();
		System.out.println("found " + numberOfSelectedRegions + " regions");
		System.out.println("initilizing memory... ");
		
		selectedRegionStarts = new long[numberOfSelectedRegions];
		selectedRegionEnds = new long[numberOfSelectedRegions];
		selectedRegionRelativePositions = new long[numberOfSelectedRegions];

		System.out.println("filling region references... ");
		gffReader = new GffReader(gffFile);
		long relative = 0;
		int index = 0;
		long pos;
//		long lastEnd = 0;
//		while((region = gffReader.read())!=null){
//			pos = locator.getAbsoluteCoordinates(region.getSequenceName(),region.getStart());
//			selectedRegionStarts[index] = Math.max(lastEnd,pos);
//			selectedRegionEnds[index] = Math.max(lastEnd,pos + region.getEnd()-region.getStart());
//			selectedRegionRelativePositions[index] = relative;
//			relative+=(selectedRegionEnds[index]-selectedRegionStarts[index]+1);
//			lastEnd = selectedRegionEnds[index];	
//			System.err.println(region.getStart() + ":" + region.getEnd() + "       " + pos + ":" + (pos + region.getEnd()-region.getStart()) + "      "  + selectedRegionStarts[index] + ":" + selectedRegionEnds[index] +  "     " + relative);
//			index++;
//		}
		
		while((region = gffReader.read())!=null){
			pos = locator.getAbsoluteCoordinates(region.getSequenceName(),region.getStart());
			if(pos==-1) {
				System.err.println(">>>>>> " + region.getSequenceName() + ":" + region.getStart());
			}
			selectedRegionStarts[index] = pos;
			selectedRegionEnds[index] = pos + region.getEnd()-region.getStart();
			selectedRegionRelativePositions[index] = relative;
			relative+=(selectedRegionEnds[index]-selectedRegionStarts[index]+1);		
			index++;
		}
		
		insideReferenceSize = relative;
		outsideReferenceSize = referenceSize-insideReferenceSize;
		
		System.err.println("insideReferenceSize: " + insideReferenceSize);
		System.err.println("outsideReferenceSize: " + outsideReferenceSize);
		
	}
	
	
	private void propagateRead(String alignment,long readStart, long readEnd, int mappingQuality,long insertSize,BamGenomeWindow currentWindow, BamStats bamStats, HashMap<Long,BamGenomeWindow> openWindows, byte[]reference,boolean detailed,boolean verbose){
		// init covering stat
		long ws, we;
		String name;
		int index = bamStats.getNumberOfProcessedWindows()+1;
		
		BamGenomeWindow adjacentWindow;
		boolean outOfBounds = true;		
		while(outOfBounds && index < bamStats.getNumberOfWindows()){
			// next currentWindow
			ws = bamStats.getWindowStart(index);
			we = bamStats.getWindowEnd(index);
			name = bamStats.getWindowName(index);
			
			// already exist?			
			if(openWindows.containsKey(ws)){
				adjacentWindow = openWindows.get(ws);
			} else {
				adjacentWindow = initWindow(name,ws,Math.min(we,bamStats.getReferenceSize()),reference,detailed,verbose);
				bamStats.incInitializedWindows();
				openWindows.put(ws,adjacentWindow);				
			}
			
			// acum read
			outOfBounds = adjacentWindow.acumRead(alignment,readStart,readEnd,mappingQuality,insertSize);
			
			index++;
		}
	}
	
	
	/*
	 * Window management functions
	 */
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
	
	
	private BamGenomeWindow finalizeAndGetNextWindow(long position, BamGenomeWindow lastWindow,
			HashMap<Long,BamGenomeWindow> openWindows,BamStats bamStats,byte[]reference, 
			boolean verbose,boolean detailed) throws CloneNotSupportedException{
		// acum globals
		finalizeWindow(lastWindow,bamStats,verbose);
				
		// prepare next currentWindow
		BamGenomeWindow currentWindow = nextWindow(bamStats,openWindows,reference,detailed,verbose);
		
		if(currentWindow!=null) {
			// position is still far away
			while(position>currentWindow.getEnd()) {
				// acum globals
				finalizeWindow(currentWindow,bamStats,verbose);
				// prepare next currentWindow			
				currentWindow = nextWindow(bamStats,openWindows,reference,detailed,verbose);
			}
		}
				
		return currentWindow;
	}
	
	
	private BamGenomeWindow nextWindow(BamStats bamStats,HashMap<Long,BamGenomeWindow> openWindows,byte[]reference,boolean detailed,boolean verbose){		
		// init new current
		BamGenomeWindow currentWindow = null;
		
		if(bamStats.getNumberOfProcessedWindows()<bamStats.getNumberOfWindows()){
			long windowStart = bamStats.getCurrentWindowStart();
			
			if(windowStart<=bamStats.getReferenceSize()){	
				long windowEnd = bamStats.getCurrentWindowEnd();
				if(openWindows.containsKey(windowStart)){
					currentWindow = openWindows.get(windowStart);
					openWindows.remove(windowStart);
				} else {					
					currentWindow = initWindow(bamStats.getCurrentWindowName(),windowStart,Math.min(windowEnd,bamStats.getReferenceSize()),reference,detailed,verbose);					
					bamStats.incInitializedWindows();
				}
			}
		}
	
		return currentWindow;
	}
	
	
	private BamGenomeWindow initWindow(String name,long windowStart,long windowEnd, byte[]reference, boolean detailed, boolean verbose){		
		byte[]miniReference = null;
		if(reference!=null) {
			miniReference = new byte[(int)(windowEnd-windowStart+1)];
			miniReference = Arrays.copyOfRange(reference,(int)(windowStart-1),(int)(windowEnd-1));
		}
		
		if(detailed){
			return new BamDetailedGenomeWindow(name,windowStart,windowEnd,miniReference);	
		} else {
			return new BamGenomeWindow(name,windowStart,windowEnd,miniReference);
		}
	}
	
	
	private void finalizeWindow(BamGenomeWindow currentWindow, BamStats bamStats, boolean verbose) throws CloneNotSupportedException{
		
		if(verbose){
			lastActionDone = "Processing window " + currentWindow.getName() + " " +  (bamStats.getNumberOfProcessedWindows()+1) + " of " + bamStats.getNumberOfWindows() + " (" + currentWindow.getStart() + ":" + currentWindow.getEnd() + ")";
			logger.println(lastActionDone);
		}
		
		if(verbose){
			logger.println("     > sumarizing window...");
		}
		
		currentWindow.computeDescriptors();

		if(verbose){
			logger.println("     > adding to global stats...");
		}
		
		bamStats.addWindowInformation(currentWindow);
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
	
	
	/**
	 * @return the referenceFile
	 */
	public String getReferenceFile() {
		return referenceFile;
	}

	/**
	 * @param referenceFile the referenceFile to set
	 */
	public void setReferenceFile(String referenceFile) {
		this.referenceFile = referenceFile;
	}

	/**
	 * @return the bamFile
	 */
	public String getBamFile() {
		return bamFile;
	}

	/**
	 * @param bamFile the bamFile to set
	 */
	public void setBamFile(String bamFile) {
		this.bamFile = bamFile;
	}

	/**
	 * @return the referenceAvailable
	 */
	public boolean isReferenceAvailable() {
		return referenceAvailable;
	}

	/**
	 * @param referenceAvailable the referenceAvailable to set
	 */
	public void setReferenceAvailable(boolean referenceAvailable) {
		this.referenceAvailable = referenceAvailable;
	}

	/**
	 * @return the reference
	 */
	public byte[] getReference() {
		return reference;
	}

	/**
	 * @param reference the reference to set
	 */
	public void setReference(byte[] reference) {
		this.reference = reference;
	}

	/**
	 * @return the numberOfReferenceContigs
	 */
	public int getNumberOfReferenceContigs() {
		return numberOfReferenceContigs;
	}

	/**
	 * @param numberOfReferenceContigs the numberOfReferenceContigs to set
	 */
	public void setNumberOfReferenceContigs(int numberOfReferenceContigs) {
		this.numberOfReferenceContigs = numberOfReferenceContigs;
	}

	/**
	 * @return the numberOfWindows
	 */
	public int getNumberOfWindows() {
		return numberOfWindows;
	}

	/**
	 * @param numberOfWindows the numberOfWindows to set
	 */
	public void setNumberOfWindows(int numberOfWindows) {
		this.numberOfWindows = numberOfWindows;
	}

	/**
	 * @return the windowSize
	 */
	public int getWindowSize() {
		return windowSize;
	}

	/**
	 * @param windowSize the windowSize to set
	 */
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	/**
	 * @return the locator
	 */
	public GenomeLocator getLocator() {
		return locator;
	}

	/**
	 * @param locator the locator to set
	 */
	public void setLocator(GenomeLocator locator) {
		this.locator = locator;
	}

	/**
	 * @return the numberOfReads
	 */
	public int getNumberOfReads() {
		return numberOfReads;
	}

	/**
	 * @param numberOfReads the numberOfReads to set
	 */
	public void setNumberOfReads(int numberOfReads) {
		this.numberOfReads = numberOfReads;
	}

	/**
	 * @return the numberOfValidReads
	 */
	public int getNumberOfValidReads() {
		return numberOfValidReads;
	}

	/**
	 * @param numberOfValidReads the numberOfValidReads to set
	 */
	public void setNumberOfValidReads(int numberOfValidReads) {
		this.numberOfValidReads = numberOfValidReads;
	}

	/**
	 * @return the percentageOfValidReads
	 */
	public double getPercentageOfValidReads() {
		return percentageOfValidReads;
	}

	/**
	 * @param percentageOfValidReads the percentageOfValidReads to set
	 */
	public void setPercentageOfValidReads(double percentageOfValidReads) {
		this.percentageOfValidReads = percentageOfValidReads;
	}

	/**
	 * @return the numberOfMappedReads
	 */
	public int getNumberOfMappedReads() {
		return numberOfMappedReads;
	}

	/**
	 * @param numberOfMappedReads the numberOfMappedReads to set
	 */
	public void setNumberOfMappedReads(int numberOfMappedReads) {
		this.numberOfMappedReads = numberOfMappedReads;
	}

	/**
	 * @return the percentageOfMappedReads
	 */
	public double getPercentageOfMappedReads() {
		return percentageOfMappedReads;
	}

	/**
	 * @param percentageOfMappedReads the percentageOfMappedReads to set
	 */
	public void setPercentageOfMappedReads(double percentageOfMappedReads) {
		this.percentageOfMappedReads = percentageOfMappedReads;
	}

	/**
	 * @return the bamStats
	 */
	public BamStats getBamStats() {
		return bamStats;
	}

	/**
	 * @param bamStats the bamStats to set
	 */
	public void setBamStats(BamStats bamStats) {
		this.bamStats = bamStats;
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}
	
	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @return the insideBamStats
	 */
	public BamStats getInsideBamStats() {
		return insideBamStats;
	}

	/**
	 * @param insideBamStats the insideBamStats to set
	 */
	public void setInsideBamStats(BamStats insideBamStats) {
		this.insideBamStats = insideBamStats;
	}

	/**
	 * @return the outsideBamStats
	 */
	public BamStats getOutsideBamStats() {
		return outsideBamStats;
	}

	/**
	 * @param outsideBamStats the outsideBamStats to set
	 */
	public void setOutsideBamStats(BamStats outsideBamStats) {
		this.outsideBamStats = outsideBamStats;
	}

	/**
	 * @return the chromosomeStats
	 */
	public BamStats getChromosomeStats() {
		return chromosomeStats;
	}

	/**
	 * @param chromosomeStats the chromosomeStats to set
	 */
	public void setChromosomeStats(BamStats chromosomeStats) {
		this.chromosomeStats = chromosomeStats;
	}

	/**
	 * Return the last action done for the algorithm.
	 * @return
	 */
	public String getLastActionDone() {
		return lastActionDone;
	}

	public void setLastActionDone(String lastActionDone) {
		this.lastActionDone = lastActionDone;
	}

	/**
	 * @return the computeOutsideStats
	 */
	public boolean isComputeOutsideStats() {
		return computeOutsideStats;
	}

	/**
	 * @param computeOutsideStats the computeOutsideStats to set
	 */
	public void setComputeOutsideStats(boolean computeOutsideStats) {
		this.computeOutsideStats = computeOutsideStats;
	}

	/**
	 * @return the computeChromosomeStats
	 */
	public boolean isComputeChromosomeStats() {
		return computeChromosomeStats;
	}

	/**
	 * @param computeChromosomeStats the computeChromosomeStats to set
	 */
	public void setComputeChromosomeStats(boolean computeChromosomeStats) {
		this.computeChromosomeStats = computeChromosomeStats;
	}

	/**
	 * @return the computeInsertSize
	 */
	public boolean isComputeInsertSize() {
		return computeInsertSize;
	}

	/**
	 * @param computeInsertSize the computeInsertSize to set
	 */
	public void setComputeInsertSize(boolean computeInsertSize) {
		this.computeInsertSize = computeInsertSize;
	}	
}
