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
package org.bioinfo.ngs.qc.qualimap.beans;

import java.io.*;
import java.util.*;

import org.bioinfo.commons.utils.ArrayUtils;
import org.bioinfo.commons.utils.ListUtils;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.math.util.MathUtils;
import org.bioinfo.ngs.qc.qualimap.process.ReadStatsCollector;
import org.bioinfo.ngs.qc.qualimap.common.ReadStartsHistogram;

public class BamStats implements Serializable {
	private String name;
	private String sourceFile;
	//private String referenceFile;
	
	// globals
	private long numberOfMappedBases;
	private long numberOfSequencedBases;
	private long numberOfAlignedBases;
	private long numberOfReads;
	private long numberOfMappedReads;
	private long numberOfPairedReads;
    private long numberOfSingletons;
    private long numberOfMappedFirstOfPair;
    private long numberOfMappedSecondOfPair;

    // regions related
    private long numberOfMappedReadsInRegions;
    private long numberOfPairedReadsInRegions;
    private long numberOfSingletonsInRegions;
    private long numberOfMappedFirstOfPairInRegions;
    private long numberOfMappedSecondOfPairInRegions;
    private long numCorrectStrandReads;

    private ArrayList<Long>  numMappedBasesPerWindow;
    private ArrayList<Long> coverageSquaredPerWindow;
    private ArrayList<Long> windowLengthes;


	/*
	 * 
	 *  Reference params
	 *   
	 */
	
	private long referenceSize;
	private long numberOfReferenceContigs;

    private long inRegionReferenceSize;
    private int numSelectedRegions;

    /*
 	  // A content
	private long numberOfAsInReference;
	private double meanAContentPerWindowInReference;
	private double meanARelativeContentPerWindowInReference;
	private List<Double> aContentInReference;
	private List<Double> aRelativeContentInReference;
	
	  // C content
	private long numberOfCsInReference;
	private double meanCContentPerWindowInReference;
	private double meanCRelativeContentPerWindowInReference;
	private List<Double> cContentInReference;
	private List<Double> cRelativeContentInReference;
	
	  // T content
	private long numberOfTsInReference;
	private double meanTContentPerWindowInReference;
	private double meanTRelativeContentPerWindowInReference;
	private List<Double> tContentInReference;
	private List<Double> tRelativeContentInReference;
	
  	  // G content
	private long numberOfGsInReference;
	private double meanGContentPerWindowInReference;
	private double meanGRelativeContentPerWindowInReference;
	private List<Double> gContentInReference;
	private List<Double> gRelativeContentInReference;
	
	  // N content
	private long numberOfNsInReference;
	private double meanNContentPerWindowInReference;
	private double meanNRelativeContentPerWindowInReference;
	private List<Double> nContentInReference;
	private List<Double> nRelativeContentInReference;
	
	  // GC content
	private double meanGcContentInReference;
	private double meanGcContentPerWindowInReference;
	private double meanGcRelativeContentPerWindowInReference;
	private List<Double> gcContentInReference;
	private List<Double> gcRelativeContentInReference;
	
	  // AT content
	private double meanAtContentInReference;
	private double meanAtContentPerWindowInReference;
	private double meanAtRelativeContentPerWindowInReference;
	private List<Double> atContentInReference;
	private List<Double> atRelativeContentInReference;
	 */

	/*
	 * 
	 * Sample params
	 * 
	 */
	
	// coverageData
	private double meanCoverage;
	private double meanCoveragePerWindow;
	private double stdCoverage;
	private List<Double> coverageAcrossReference;
	private List<Double> stdCoverageAcrossReference;
	private HashMap<Long,Long> coverageHistogramMap;
	private long[] coverageHistogramCache;
    private XYVector coverageHistogram;
	private XYVector acumCoverageHistogram;
    private ReadStartsHistogram readStartsHistogram;
    private XYVector uniqueReadStartsHistogram;
    private XYVector balancedCoverageHistogram;
	private int maxCoverageQuota;
	private XYVector coverageQuotes;
    private Map<Double, String> balancedCoverageBarNames;
    double duplicationRate;
	
	// quality
	private double meanMappingQualityPerWindow;
	private List<Double> mappingQualityAcrossReference;
	private HashMap<Long,Long> mappingQualityHistogramMap;
    private long[] mappingQualityHistogramCache;
	private XYVector mappingQualityHistogram;
	
	// A content
	private long numberOfAs;
	private double meanAContentPerWindow;
	private double meanARelativeContentPerWindow;
	private double meanARelativeContent;
	private List<Double> aContentAcrossReference;
	private List<Double> aRelativeContentAcrossReference;
	
	// C content
	private long numberOfCs;
	private double meanCContentPerWindow;
	private double meanCRelativeContentPerWindow;
	private double meanCRelativeContent;
	private List<Double> cContentAcrossReference;
	private List<Double> cRelativeContentAcrossReference;	
	
	// T content
	private long numberOfTs;
	private double meanTContentPerWindow;
	private double meanTRelativeContentPerWindow;
	private double meanTRelativeContent;
	private List<Double> tContentAcrossReference;
	private List<Double> tRelativeContentAcrossReference;
	
	// G content
	private long numberOfGs;
	private double meanGContentPerWindow;
	private double meanGRelativeContentPerWindow;
	private double meanGRelativeContent;
	private List<Double> gContentAcrossReference;
	private List<Double> gRelativeContentAcrossReference;
	
	// N content
	private long numberOfNs;
	private double meanNContentPerWindow;
	private double meanNRelativeContentPerWindow;
	private double meanNRelativeContent;
	private List<Double> nContentAcrossReference;
	private List<Double> nRelativeContentAcrossReference;
	
	// GC content
	private double meanGcContent;
	private double meanGcContentPerWindow;
	private double meanGcRelativeContentPerWindow;
	private double meanGcRelativeContent;
	private List<Double> gcContentAcrossReference;
	private List<Double> gcRelativeContentAcrossReference;

	// insert size
	private double meanInsertSize;
	private int p25InsertSize, medianInsertSize, p75InsertSize;
    private double stdInsertSize;
    private List<Double> insertSizeAcrossReference;
    private XYVector insertSizeHistogram;
    private ArrayList<Integer> insertSizeArray;
	private HashMap<Long,Long> insertSizeHistogramMap;
    private long[] insertSizeHistogramCache;

    // reads stats
    double readMeanSize;
    int readMaxSize, readMinSize;
    int numClippedReads, numReadsWithInsertion, numReadsWithDeletion;
    List<Long> readsAsData;
    List<Long> readsCsData;
    List<Long> readsGsData;
    List<Long> readsTsData;
    List<Long> readsNsData;
    List<Long> readsClippingData;
    XYVector readsAsHistogram;
    XYVector readsCsHistogram;
    XYVector readsGsHistogram;
    XYVector readsTsHistogram;
    XYVector readsNsHistogram;
    XYVector readsClippingProfileHistogram;
    int[] homopolymerIndelsData;
    private boolean reportNonZeroCoverageOnly;
    private long estimatedNumDuplicatedReads;

    public long getNumMismatches() {
        return numMismatches;
    }

    public double getErrorRate() {
        double errorRate = 0.;
        if (acumEditDistance > 0 && numberOfMappedBases > 0) {
            errorRate = ( (double)acumEditDistance / numberOfMappedBases);
        }

        return errorRate;
    }

    public long getEstimatedNumDuplicatedReads() {
        return estimatedNumDuplicatedReads;
    }

    // chromosome stats
    public class ChromosomeInfo {
        String name;
        long length, numBases;
        double covMean,covStd;

        public String getName() {
            return name;
        }

        public long getLength() {
            return length;
        }

        public long getNumBases() {
            return numBases;
        }

        public double getCovMean() {
            return covMean;
        }

        public double getCovStd() {
            return covStd;
        }
    }

    public ChromosomeInfo[] getChromosomeStats() {
        return chromosomeStats;
    }


    ChromosomeInfo[] chromosomeStats;


	// windows
	private int numberOfWindows;
	private int numberOfProcessedWindows;
	private int numberOfInitializedWindows;	
	private boolean windowPositionsAvailable;
	private long[] windowSizes;	
	private long[] windowStarts;
	private long[] windowEnds;
	private String[] windowNames;
	
	// reporting
	private boolean activeWindowReporting;
	transient private PrintWriter windowReport;
	private boolean activeCoverageReporting;
	transient private PrintWriter coverageReport;
    private long sumCoverageSquared;
    private final int CACHE_SIZE = 2000;
    private Map<String,String> warnings;
    GenomeLocator locator;


    // gc content histogram
    public static int  NUM_BINS = 1000;
    static int SMOOTH_DISTANCE = 0;
    private double[] gcContentHistogram;
    boolean avaialableGenomeGcContentData;
    long sampleCount;

    private int numInsertions;
    private int numDeletions;

    private long numMismatches, acumEditDistance;

    public BamStats(String name, GenomeLocator locator, long referenceSize, int numberOfWindows){

		// global
		this.name = name;
		this.referenceSize = referenceSize;
		this.numberOfWindows = numberOfWindows;
        this.locator = locator;
		numberOfMappedBases = 0;
		numberOfSequencedBases = 0;
		numberOfAlignedBases = 0;
						
		// reference
		  // ACTG across reference arrays		
		/*aContentInReference = new ArrayList<Double>(numberOfWindows);
		aRelativeContentInReference = new ArrayList<Double>(numberOfWindows);
		cContentInReference = new ArrayList<Double>(numberOfWindows);
		cRelativeContentInReference = new ArrayList<Double>(numberOfWindows);
		tContentInReference = new ArrayList<Double>(numberOfWindows);
		tRelativeContentInReference = new ArrayList<Double>(numberOfWindows);
		gContentInReference = new ArrayList<Double>(numberOfWindows);
		gRelativeContentInReference = new ArrayList<Double>(numberOfWindows);
		nContentInReference = new ArrayList<Double>(numberOfWindows);
		nRelativeContentInReference = new ArrayList<Double>(numberOfWindows);
		gcContentInReference = new ArrayList<Double>(numberOfWindows);		
		gcRelativeContentInReference = new ArrayList<Double>(numberOfWindows);
		atContentInReference = new ArrayList<Double>(numberOfWindows);
		atRelativeContentInReference = new ArrayList<Double>(numberOfWindows);*/

		// coverageData across reference arrays
		coverageAcrossReference = new ArrayList<Double>(numberOfWindows);
		stdCoverageAcrossReference = new ArrayList<Double>(numberOfWindows);
		coverageHistogramMap = new HashMap<Long,Long>(numberOfWindows);
        coverageHistogramCache = new long[CACHE_SIZE];


		
		// quality
		mappingQualityAcrossReference = new ArrayList<Double>(numberOfWindows);
		mappingQualityHistogramMap = new HashMap<Long,Long>(numberOfWindows);
        mappingQualityHistogramCache = new long[CACHE_SIZE];
		
		// ACTG across reference arrays		
		aContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		aRelativeContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		cContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		cRelativeContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		tContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		tRelativeContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		gContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		gRelativeContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		nContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		nRelativeContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		gcContentAcrossReference = new ArrayList<Double>(numberOfWindows);
		gcRelativeContentAcrossReference = new ArrayList<Double>(numberOfWindows);
//		atContentAcrossReference = new ArrayList<Double>(numberOfWindows);
//		atRelativeContentAcrossReference = new ArrayList<Double>(numberOfWindows);

        numMappedBasesPerWindow = new ArrayList<Long>(numberOfWindows);
        coverageSquaredPerWindow = new ArrayList<Long>(numberOfWindows);
        windowLengthes = new ArrayList<Long>(numberOfWindows);



		// gc content histogram
        gcContentHistogram = new double[NUM_BINS + 1];
        sampleCount = 0;
        avaialableGenomeGcContentData = false;

		// insert size
		insertSizeAcrossReference = new ArrayList<Double>(numberOfWindows);
		insertSizeHistogramMap = new HashMap<Long,Long>(numberOfWindows);
        insertSizeHistogramCache = new long[CACHE_SIZE];
        insertSizeArray = new ArrayList<Integer>();

        // reads
        readsAsData = new ArrayList<Long>();
        readsCsData = new ArrayList<Long>();
        readsGsData = new ArrayList<Long>();
        readsTsData = new ArrayList<Long>();
        readsNsData = new ArrayList<Long>();
        readsClippingData = new ArrayList<Long>();
        homopolymerIndelsData = new int[6];

		// others		
		maxCoverageQuota = 50;
		numberOfProcessedWindows = 0;
        numberOfInitializedWindows = 0;

        readStartsHistogram =  new ReadStartsHistogram();

        warnings = new HashMap<String, String>() ;
		
	}

	/*public void setWindowReferences(String prefix,int windowSize){
		windowNames = new String[numberOfWindows];
		windowSizes = new long[numberOfWindows];
		windowStarts = new long[numberOfWindows];
		windowEnds = new long[numberOfWindows];		
		for(int i=0; i<numberOfWindows; i++){
			windowNames[i] = prefix + "_" + (i+1);
			windowSizes[i] = windowSize;
			windowStarts[i] = (long)windowSize*(long)i+1;
			windowEnds[i] = windowStarts[i] + windowSize - 1;
		}
		windowPositionsAvailable=true;
	}*/

    public void setWindowReferences(String prefix, List<Long> windowPositions) {
        windowNames = new String[numberOfWindows];
        windowSizes = new long[numberOfWindows];
        windowStarts = new long[numberOfWindows];
        windowEnds = new long[numberOfWindows];

        for (int i = 0; i < numberOfWindows; ++i) {
            windowNames[i] = prefix + "_" + (i+1);
            windowStarts[i] = windowPositions.get(i);
            if (i + 1 == numberOfWindows ) {
                windowEnds[i] = referenceSize;
            } else {
                windowEnds[i] = windowPositions.get(i+1) - 1;
            }
            windowSizes[i] = windowEnds[i] - windowStarts[i] + 1;
        }
    }


	/*
	 * 
	 * Reporting
	 * 
	 */
	public void activateWindowReporting(String windowReportFile) throws FileNotFoundException{
		this.windowReport = new PrintWriter(new File(windowReportFile));
		this.activeWindowReporting = true;
		reportWindowHeader();
	}
	
	public void closeWindowReporting(){
		this.windowReport.close();
	}
	
	public void reportWindowHeader(){
		windowReport.println("#name\tabsolute_pos\tmapped_bases\tmean_coverage\tstd_coverage");
	}
	
	public void reportWindow(BamGenomeWindow window){
		windowReport.print(window.getName() + "\t");
		windowReport.print(window.getStart()+":"+window.getEnd() + "\t");
		windowReport.print(window.getNumberOfMappedBases() + "\t");
		windowReport.print(StringUtils.decimalFormat(window.getMeanCoverage(),"#,###,###,###.##") + "\t");
		windowReport.print(StringUtils.decimalFormat(window.getStdCoverage(),"#,###,###,###.##"));
		windowReport.println();
		windowReport.flush();
	}
	
	
	public void activateCoverageReporting(String coverageReportFile, boolean nonZeroCoverageOnly) throws FileNotFoundException{
		this.coverageReport = new PrintWriter(new File(coverageReportFile));
		this.activeCoverageReporting = true;
        this.reportNonZeroCoverageOnly = nonZeroCoverageOnly;
		reportCoverageHeader();
	}
	
	public void closeCoverageReporting(){
		this.coverageReport.close();
	}
	
	public void reportCoverageHeader(){
		coverageReport.println("#chr\tpos\tcoverage");
	}
	
	public void reportCoverage(BamDetailedGenomeWindow window){
        ContigRecord rec = locator.getContigCoordinates(window.getStart());
		for(int i=0; i<window.getCoverageAcrossReference().length; i++){
            int coverage = window.getCoverageAcrossReference()[i];
            if (coverage == 0 && reportNonZeroCoverageOnly) {
                continue;
            }
            coverageReport.print(rec.getName() + "\t");
            coverageReport.print((rec.getRelative() + i) + "\t");
			coverageReport.print(window.getCoverageAcrossReference()[i]);
			coverageReport.println();
		}
        coverageReport.flush();
	}
	
	/*
	 * Window management
	 */
	public synchronized void incInitializedWindows(){
		numberOfInitializedWindows++;
	}

    public synchronized void incProcessedWindows() {
        numberOfProcessedWindows++;
    }

	public synchronized  void addWindowInformation(BamGenomeWindow window){

        //TODO: bad design
        boolean isInstanceOfBamGenomeWindow =  window instanceof BamDetailedGenomeWindow;

		// global
		numberOfMappedBases+=window.getNumberOfMappedBases();
		numberOfSequencedBases+=window.getNumberOfSequencedBases();
		numberOfAlignedBases+=window.getNumberOfAlignedBases();

		/*
		* Reference
		*/

        /*
		 // A
		numberOfAsInReference+=window.getNumberOfAsInReference();
		aContentInReference.add((double)window.getNumberOfAsInReference());
		aRelativeContentInReference.add(window.getaRelativeContentInReference());
		  // C
		numberOfCsInReference+=window.getNumberOfCsInReference();
		cContentInReference.add((double)window.getNumberOfCsInReference());
		cRelativeContentInReference.add(window.getcRelativeContentInReference());
		  // T
		numberOfTsInReference+=window.getNumberOfTsInReference();
		tContentInReference.add((double)window.getNumberOfTsInReference());
		tRelativeContentInReference.add(window.gettRelativeContentInReference());
		  // G
		numberOfGsInReference+=window.getNumberOfGsInReference();
		gContentInReference.add((double)window.getNumberOfGsInReference());
		gRelativeContentInReference.add(window.getgRelativeContentInReference());	
		  // N
		numberOfNsInReference+=window.getNumberOfNsInReference();
		nContentInReference.add((double)window.getNumberOfNsInReference());		
		nRelativeContentInReference.add(window.getnRelativeContentInReference());
		  // GC		
		gcContentInReference.add((double)window.getNumberOfGcsInReference());
		gcRelativeContentInReference.add(window.getGcRelativeContentInReference());
		  // AT
		atContentInReference.add((double)window.getNumberOfAtsInReference());
		atRelativeContentInReference.add(window.getAtRelativeContentInReference());
        */

        windowLengthes.add( window.getEffectiveWindowLength() );
        numMappedBasesPerWindow.add( window.getNumberOfMappedBases() );

		/*
		 * Sample
		 */
		
		// coverageData across reference
		coverageAcrossReference.add(window.getMeanCoverage());
		stdCoverageAcrossReference.add(window.getStdCoverage());
        if (isInstanceOfBamGenomeWindow) {
            coverageSquaredPerWindow.add( ((BamDetailedGenomeWindow)window).getSumCoverageSquared() );
            sumCoverageSquared += ((BamDetailedGenomeWindow)window).getSumCoverageSquared();
            updateHistograms((BamDetailedGenomeWindow)window);
        }
		
		// quality
		mappingQualityAcrossReference.add(window.getMeanMappingQuality());		
		/*if(isInstanceOfBamGenomeWindow) {
            updateHistogramFromLongVector(mappingQualityHistogramMap,((BamDetailedGenomeWindow)window).getMappingQualityAcrossReference());
        }*/

		// A
		numberOfAs+=window.getNumberOfAs();
		aContentAcrossReference.add(window.getMeanAContent());
		aRelativeContentAcrossReference.add(window.getMeanARelativeContent());
		
		// C
		numberOfCs+=window.getNumberOfCs();
		cContentAcrossReference.add(window.getMeanCContent());
		cRelativeContentAcrossReference.add(window.getMeanCRelativeContent());
		
		  // T
		numberOfTs+=window.getNumberOfTs();
		tContentAcrossReference.add(window.getMeanTContent());
		tRelativeContentAcrossReference.add(window.getMeanTRelativeContent());
		
		  // G
		numberOfGs+=window.getNumberOfGs();
		gContentAcrossReference.add(window.getMeanGContent());
		gRelativeContentAcrossReference.add(window.getMeanGRelativeContent());
		
		  // N
		numberOfNs+=window.getNumberOfNs();
		nContentAcrossReference.add(window.getMeanNContent());		
		nRelativeContentAcrossReference.add(window.getMeanNRelativeContent());
		
		  // GC
		gcContentAcrossReference.add(window.getMeanGcContent());
		gcRelativeContentAcrossReference.add(window.getMeanGcRelativeContent());

        //gcContentHistogram[ (int) window.getMeanGcRelativeContent() ]++;
		
    	// insert size
		insertSizeAcrossReference.add(window.getMeanInsertSize());
		/*if(isInstanceOfBamGenomeWindow){
			updateHistogramFromLongVector(insertSizeHistogramMap,((BamDetailedGenomeWindow)window).getInsertSizeAcrossReference());
		}*/
		
		// reporting
		if(activeWindowReporting) reportWindow(window);
		if(isInstanceOfBamGenomeWindow){
			if(activeCoverageReporting) {
                reportCoverage((BamDetailedGenomeWindow)window);
            }
		}
	}
	
	public void computeDescriptors(){

		//TODO: this can be parallel!

		/* Reference */

        /*
		// A
		meanAContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(aContentInReference));	
		meanARelativeContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(aRelativeContentInReference));		
		
		// C
		meanCContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(cContentInReference));
		meanCRelativeContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(cRelativeContentInReference));
		
		// T
		meanTContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(tContentInReference));
		meanTRelativeContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(tRelativeContentInReference));
		
		// G
		meanGContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(gContentInReference));
		meanGRelativeContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(gRelativeContentInReference));
		
		// N
		meanNContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(nContentInReference));
		meanNRelativeContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(nRelativeContentInReference));
				
		// GC
		meanGcContentInReference = (double)(numberOfGsInReference+numberOfCsInReference)/(double)referenceSize;
		meanGcContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(gcContentInReference));
		meanGcRelativeContentPerWindowInReference = MathUtils.mean(ListUtils.toDoubleArray(gcRelativeContentInReference));
        */

		/*
		 * Sample  
		 */

        System.out.println("numberOfMappedBases: " + numberOfMappedBases);
        System.out.println("referenceSize: " + referenceSize);
        System.out.println("numberOfSequencedBases: " + numberOfSequencedBases);
        System.out.println("numberOfAs: " + numberOfAs);

        long effectiveRefSize = numSelectedRegions > 0 ? inRegionReferenceSize : referenceSize;
        meanCoverage = (double) numberOfMappedBases / (double) effectiveRefSize;

        if (numberOfSequencedBases != 0) {

            stdCoverage = Math.sqrt( (double) sumCoverageSquared / (double) effectiveRefSize - meanCoverage*meanCoverage);

            // quality
            meanMappingQualityPerWindow = MathUtils.mean(ListUtils.toDoubleArray(mappingQualityAcrossReference));

            // A
            meanAContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(aContentAcrossReference));
            meanARelativeContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(aRelativeContentAcrossReference));
            meanARelativeContent = ((double)numberOfAs/(double)numberOfSequencedBases)*100.0;

            // C
            meanCContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(cContentAcrossReference));
            meanCRelativeContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(cRelativeContentAcrossReference));
            meanCRelativeContent = ((double)numberOfCs/(double)numberOfSequencedBases)*100.0;

            // T
            meanTContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(tContentAcrossReference));
            meanTRelativeContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(tRelativeContentAcrossReference));
            meanTRelativeContent = ((double)numberOfTs/(double)numberOfSequencedBases)*100.0;

            // G
            meanGContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(gContentAcrossReference));
            meanGRelativeContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(gRelativeContentAcrossReference));
            meanGRelativeContent = ((double)numberOfGs/(double)numberOfSequencedBases)*100.0;

            // N
            meanNContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(nContentAcrossReference));
            meanNRelativeContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(nRelativeContentAcrossReference));
            meanNRelativeContent = ((double)numberOfNs/(double)numberOfSequencedBases)*100.0;

            // GC
            meanGcContent = (double)(numberOfGs+numberOfCs)/(double)referenceSize;
            meanGcContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(gcContentAcrossReference));
            meanGcRelativeContentPerWindow = MathUtils.mean(ListUtils.toDoubleArray(gcRelativeContentAcrossReference));
            meanGcRelativeContent = ((double)(numberOfGs+numberOfCs)/(double)numberOfSequencedBases)*100.0;

        }


		// reporting
		if(activeWindowReporting) {
            closeWindowReporting();
        }

        if(activeCoverageReporting) {
            closeCoverageReporting();
        }
		
	}
	
	/*
	 *  Histograms
	 */


	public void updateHistograms(BamDetailedGenomeWindow window){
		for(int i=0; i<window.getCoverageAcrossReference().length; i++){
			if ( window.selectedRegionsAvailable ) {
                if (!window.getSelectedRegions().get(i)) {
                    continue;
                }
            }
			// coverageData
			updateHistogramValue(coverageHistogramCache, coverageHistogramMap, window.getCoverageAcrossReference()[i]);
			
			long quality = window.getMappingQualityAcrossReference()[i];
            if (quality != -1) {
			    updateHistogramValue(mappingQualityHistogramCache, mappingQualityHistogramMap, quality);
            }
            // insert size
            /*long insertSize = window.getInsertSizeAcrossReference()[i];
            if (insertSize > 0) {
                //System.out.println("IS = " + insertSize);
                updateHistogramValue(insertSizeHistogramCache, insertSizeHistogramMap, insertSize);
            }*/
        }
	}

	/*@SuppressWarnings("unchecked")
	public void updateHistogramValue(@SuppressWarnings("rawtypes") HashMap map, int key){
		if(!map.containsKey(key)){
			map.put(key, Long.valueOf(1));
		} else {
            long last = (Long)map.get(key);
			last++;
			map.put(key,last);
		}
	} */


    /*public void dumpInsertData() {

        try {
        PrintWriter writer = new PrintWriter( new FileWriter("/home/kokonech/insert_size_dump.txt"));

        for ( double val : insertSizeAcrossReference) {
            writer.println(val);
        }

        writer.close();
        } catch (IOException ex) {
            System.out.println("Failed to dump insert size data");
        }

    }*/

    public void updateHistogramValue(long[] cache, HashMap<Long,Long> map, long key){
		if (key < CACHE_SIZE) {
            cache[(int)key]++;
        } else if(!map.containsKey(key)){
			map.put(key, 1L);
		} else {
            map.put(key, map.get(key) + 1);
    	}
	}


    public void computeHistograms() {

        addCacheDataToMap(mappingQualityHistogramCache,mappingQualityHistogramMap);
        mappingQualityHistogram = computeVectorHistogram(mappingQualityHistogramMap);
        addCacheDataToMap(coverageHistogramCache, coverageHistogramMap);

        computeInsertSizeHistogram();
        computeCoverageHistogram();
        computeUniqueReadStartsHistogram();
        computeGCContentHistogram();
        computeReadsContentHistogrmas();
        computeReadsClippingProfileHistogram();

    }

    public XYVector getReadsAsHistogram() {
        return readsAsHistogram;
    }

    public XYVector getReadsCsHistogram() {
        return readsCsHistogram;
    }

    public XYVector getReadsGsHistogram() {
        return readsGsHistogram;
    }

    public XYVector getReadsTsHistogram() {
        return readsTsHistogram;
    }

    public XYVector getReadsNsHistogram() {
        return readsNsHistogram;
    }

    public XYVector getReadsClippingProfileHistogram() {
        return  readsClippingProfileHistogram;
    }


    private void computeReadsClippingProfileHistogram() {
        if (readMaxSize == 0 || !clippingIsPresent()) {
            return;
        }

        ensureListSize(readsClippingData, readMaxSize);

        double totalBasesClipped = 0;
        for (long val : readsClippingData) {
            totalBasesClipped += val;
        }

        readsClippingProfileHistogram = new XYVector();

        for (int pos = 0; pos < readMaxSize; ++pos) {
            double val = (readsClippingData.get(pos) / totalBasesClipped) * 100.0;
            readsClippingProfileHistogram.addItem( new XYItem(pos, val));
        }


    }

    private void computeReadsContentHistogrmas() {

        readsAsHistogram = new XYVector();
        readsCsHistogram = new XYVector();
        readsGsHistogram = new XYVector();
        readsTsHistogram = new XYVector();
        readsNsHistogram = new XYVector();

        int totalSize = readsAsData.size() + readsCsData.size() + readsGsData.size()
                + readsTsData.size() + readsNsData.size();

        if (totalSize == 0 )  {
        }

        // make sure that we have enough data
        ensureListSize(readsAsData, readMaxSize);
        ensureListSize(readsCsData, readMaxSize);
        ensureListSize(readsGsData, readMaxSize);
        ensureListSize(readsTsData, readMaxSize);
        ensureListSize(readsNsData, readMaxSize);

        for (int i = 0; i < readMaxSize; ++i ) {

            long numA = readsAsData.get(i);
            long numC = readsCsData.get(i);
            long numG = readsGsData.get(i);
            long numT = readsTsData.get(i);
            long numN = readsNsData.get(i);
            double sum =  numA + numC + numG + numT + numN;

            readsAsHistogram.addItem( new XYItem(i, (numA  / sum) * 100.0 ));
            readsCsHistogram.addItem( new XYItem(i, (numC / sum) * 100.0 ));
            readsGsHistogram.addItem( new XYItem(i, (numG / sum) * 100.0 ));
            readsTsHistogram.addItem( new XYItem(i, (numT / sum) * 100.0 ));
            readsNsHistogram.addItem( new XYItem(i, (numN / sum) * 100.0 ));

        }

    }

    private void computeGCContentHistogram() {


        //normalize
        //double normalizer = sampleCount - gcContentHistogram[0];
        for (int i = 0; i < NUM_BINS + 1; ++i) {
            gcContentHistogram[i] /= sampleCount;
        }

        //smooth
        for (int i = SMOOTH_DISTANCE; i < NUM_BINS - SMOOTH_DISTANCE; ++i) {
            double res = 0;
            for (int j = 0; j <= 2*SMOOTH_DISTANCE; ++j) {
                res += gcContentHistogram[i + j - SMOOTH_DISTANCE];
            }
            gcContentHistogram[i] = res / (double) (SMOOTH_DISTANCE*2 + 1 );
        }


        /*double sum = 0;
        for (int i = 1; i < NUM_BINS + 1; ++i) {
            sum += gcContentHistogram[i];
        }

        System.out.println("Sum is " + sum + ", sampleCount is " + sampleCount);
        */

        /*try {
            FileWriter writer = new FileWriter("/home/kokonech/out.file");
            for (int i = 0; i < 101; ++i) {
                String line = "" + i + " " + gcContentHistogram[i] + "\n";
                writer.write(line);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/

    }


    /*public int getHistogramSize(){
		// read keys
		Object[] raw = coverageHistogramMap.keySet().toArray();
		int totalCoverage = 0;
		for(int i=0; i<raw.length; i++) {
			int key = (Integer)raw[i];
			totalCoverage+=coverageHistogramMap.get(key);			
		}
		return totalCoverage;
	}*/


    private void computeUniqueReadStartsHistogram() {

        long[] uniqueReadStartsArray = readStartsHistogram.getHistorgram();

        uniqueReadStartsHistogram = new XYVector();

        long numPositions = 0;

        for (int i = 1; i <= ReadStartsHistogram.MAX_READ_STARTS_PER_POSITION; ++i ) {
            long val = uniqueReadStartsArray[i];
            numPositions += val;
            uniqueReadStartsHistogram.addItem( new XYItem(i, val));
        }

        duplicationRate =  ( 1.0 - (double) (uniqueReadStartsArray[1]) / numPositions ) * 100.0d;

    }

    private void computeInsertSizeHistogram() {

        if (insertSizeArray.isEmpty()) {
            return;
        }

        Collections.sort(insertSizeArray);

        int size = insertSizeArray.size();
        int medianIndex =  size / 2;
        int percentile25Index = size / 4;
        int percentile75Index = percentile25Index*3;

        p25InsertSize = insertSizeArray.get(percentile25Index);
        medianInsertSize = insertSizeArray.get(medianIndex);
        p75InsertSize = insertSizeArray.get(percentile75Index);
        double[] insertData =   ListUtils.toDoubleArray(insertSizeArray);
        meanInsertSize = MathUtils.mean( insertData );
        stdInsertSize = MathUtils.standardDeviation( insertData );

        double border = insertSizeArray.get(percentile75Index)*2.;

        for (int val : insertSizeArray) {
            if (val <= border) {
                updateHistogramValue(insertSizeHistogramCache, insertSizeHistogramMap, (long) val);
            }
        }

        addCacheDataToMap(insertSizeHistogramCache,insertSizeHistogramMap);
        insertSizeHistogram = computeVectorHistogram(insertSizeHistogramMap);


    }


	private void computeCoverageHistogram(){

        double[] coverages = new double[coverageHistogramMap.size()];
		double[] freqs = new double[coverageHistogramMap.size()];

		// read keys
		Object[] raw = coverageHistogramMap.keySet().toArray();
		int totalCoverage = 0;
		for(int i=0; i<raw.length; i++) {
			coverages[i] = (Long)raw[i];
			freqs[i] = coverageHistogramMap.get(raw[i]);
			totalCoverage+=freqs[i];
		}

		// sort coverages
		int[] index = ArrayUtils.order(coverages);
		double[] sortedCoverages = ArrayUtils.ordered(coverages,index);
		double[] sortedFreqs = ArrayUtils.ordered(freqs,index);

		// fill output
		coverageHistogram = new XYVector();
		for(int i=0; i<sortedCoverages.length; i++){
			coverageHistogram.addItem(new XYItem(sortedCoverages[i],sortedFreqs[i]));
		}


        //TODO: what if coverage is constant, for example 1 everywhere? This has code has to be checked

        // compute balanced coverage histogram
        balancedCoverageHistogram = new XYVector();
        balancedCoverageBarNames = new HashMap<Double, String>();
        double maxCoverage = sortedCoverages[coverages.length - 1];
        double minCoverage = sortedCoverages[0];
        int binCount = 30;
        double n = Math.pow(maxCoverage - minCoverage, 1.0 / binCount );

        int border = (int) (minCoverage);

        ArrayList<Integer> balancedCoverages = new ArrayList<Integer>();
        balancedCoverages.add(border);

        int k = sortedCoverages.length > 1 ? 1 : 0;

        for (int i = 0; i <= binCount; ++i) {
            int newBorder = (int) (Math.round( Math.pow(n, i) ) + minCoverage);
            if (newBorder > border && newBorder >= sortedCoverages[k]) {
                balancedCoverages.add(newBorder);
                border = newBorder;
                while ( k < sortedCoverages.length && newBorder >= sortedCoverages[k]) {
                    ++k;
                }
            }
        }

        int borderIndex = 0;

        if (sortedCoverages.length == 1) {
            String barName = "" + sortedCoverages[0];
            balancedCoverageBarNames.put(0., barName);
            balancedCoverageHistogram.addItem(new XYItem(0, sortedFreqs[0]));
        } else {
            for (int i = 0; i < balancedCoverages.size(); ++i) {
                int coverage = balancedCoverages.get(i);
                double sum = 0;
                int prevIndex = borderIndex;
                while(  borderIndex < sortedCoverages.length && sortedCoverages[borderIndex] <= coverage) {
                    sum += sortedFreqs[borderIndex];
                    ++borderIndex;
                }
                //System.out.println("i = " + i + " ,borderIndex = " + borderIndex + " ,sum= " + sum) ;
                String barName = borderIndex == prevIndex + 1 ? (int)sortedCoverages[prevIndex] + "" :
                        (int)sortedCoverages[prevIndex] +" - " + (int)sortedCoverages[(borderIndex - 1)];
                //System.out.println("Bar name is " + barName);
                balancedCoverageBarNames.put((double)i, barName);
                balancedCoverageHistogram.addItem(new XYItem(i, sum));

            }
        }


		// compute acum histogram
		acumCoverageHistogram = new XYVector();
		double acum = 0.0;
		for(int i=0; i<sortedCoverages.length; i++){
			acum+=sortedFreqs[i];
			acumCoverageHistogram.addItem(new XYItem(sortedCoverages[i],acum));//(acum/(double)totalCoverage)*100.0));
		}
		
		// coverageData quotes
		coverageQuotes = new XYVector();
		double total = acum;
		acum = 0;
		for(long i=0; i<=maxCoverageQuota; i++){			
			if(coverageHistogramMap.containsKey(i)){
				acum+=(coverageHistogramMap.get(i)/total)*100.0;
			}			
			coverageQuotes.addItem(new XYItem(i+1, Math.max(0,100.0-acum)));
		}

		
	}


    private void addCacheDataToMap(long[] cache, HashMap<Long,Long> map) {
        for (int i = 0; i < CACHE_SIZE; ++i) {
            long val = cache[i];
            if (val > 0) {
                map.put((long)i,val);
            }
        }
    }

    private XYVector computeVectorHistogram(HashMap<Long,Long> map){

		double[] coverages = new double[map.size()];
		double[] freqs = new double[map.size()];

		// read keys
		Object[] raw = map.keySet().toArray();
		long totalCoverage = 0;
		for(int i=0; i<raw.length; i++) {
			coverages[i] = (Long)raw[i];
			freqs[i] = (double) map.get(raw[i]);
			totalCoverage+=freqs[i];
		}

		// sort coverageData
		int[] index = ArrayUtils.order(coverages);
		double[] sortedCoverages = ArrayUtils.ordered(coverages,index);
		double[] sortedFreqs = ArrayUtils.ordered(freqs,index);

		// fill output
		XYVector vectorHistogram = new XYVector();
		for(int i=0; i<sortedCoverages.length; i++){
			vectorHistogram.addItem(new XYItem(sortedCoverages[i],sortedFreqs[i]));
		}

		return vectorHistogram;

	}

	public String getWindowName(int index){
		return windowNames[index];
	}

	public long getWindowStart(int index){
		return windowStarts[index];
	}
	
	public long getWindowEnd(int index){
		return windowEnds[index];
	}
	
	public String getCurrentWindowName(){
		return windowNames[numberOfProcessedWindows];
	}
	
	public long getCurrentWindowStart(){
		return windowStarts[numberOfProcessedWindows];
	}
	
	public long getCurrentWindowEnd(){
		return windowEnds[numberOfProcessedWindows];
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the sourceFile
	 */
	public String getSourceFile() {
		return sourceFile;
	}

	/**
	 * @param sourceFile the sourceFile to set
	 */
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	/**
	 * @return the numberOfMappedBases
	 */
	public long getNumberOfMappedBases() {
		return numberOfMappedBases;
	}

	/**
	 * @return the numberOfAlignedBases
	 */
	public long getNumberOfAlignedBases() {
		return numberOfAlignedBases;
	}

    public long getNumberOfSequencedBases() {
            return numberOfSequencedBases;
    }

	/**
	 * @return the numberOfReads
	 */
	public long getNumberOfReads() {
		return numberOfReads;
	}

	/**
	 * @param numberOfReads the numberOfReads to set
	 */
	public void setNumberOfReads(long numberOfReads) {
		this.numberOfReads = numberOfReads;
	}

	/**
	 * @return the numberOfMappedReads
	 */
	public long getNumberOfMappedReads() {
		return numberOfMappedReads;
	}

	/**
	 * @param numberOfMappedReads the numberOfMappedReads to set
	 */
	public void setNumberOfMappedReads(long numberOfMappedReads) {
		this.numberOfMappedReads = numberOfMappedReads;
	}

	/**
	 * @return the percentageOfMappedReads
	 */
	public double getPercentageOfMappedReads() {
		return ( numberOfMappedReads / (double) numberOfReads) * 100.0;
	}

    public XYVector getUniqueReadStartsHistogram() {
        return uniqueReadStartsHistogram;
    }

    /**
	 * @return the referenceSize
	 */
	public long getReferenceSize() {
		return referenceSize;
	}

	/**
	 * @param referenceSize the referenceSize to set
	 */
	public void setReferenceSize(long referenceSize) {
		this.referenceSize = referenceSize;
	}

	/**
	 * @return the numberOfReferenceContigs
	 */
	public long getNumberOfReferenceContigs() {
		return numberOfReferenceContigs;
	}

	/**
	 * @param numberOfReferenceContigs the numberOfReferenceContigs to set
	 */
	public void setNumberOfReferenceContigs(long numberOfReferenceContigs) {
		this.numberOfReferenceContigs = numberOfReferenceContigs;
	}

	/**
	 * @return the coverageAcrossReference
	 */
	public List<Double> getCoverageAcrossReference() {
		return coverageAcrossReference;
	}

	public List<Double> getStdCoverageAcrossReference() {
		return stdCoverageAcrossReference;
	}


	/**
	 * @return the coverageHistogram
	 */
	public XYVector getCoverageHistogram() {
		return coverageHistogram;
	}

	/**
	 * @return the coverageQuotes
	 */
	public XYVector getCoverageQuotes() {
		return coverageQuotes;
	}

	/**
	 * @return the meanMappingQualityPerWindow
	 */
	public double getMeanMappingQualityPerWindow() {
		return meanMappingQualityPerWindow;
	}

	/**
	 * @return the mappingQualityAcrossReference
	 */
	public List<Double> getMappingQualityAcrossReference() {
		return mappingQualityAcrossReference;
	}

	/**
	 * @return the mappingQualityHistogram
	 */
	public XYVector getMappingQualityHistogram() {
		return mappingQualityHistogram;
	}

	/**
	 * @return the numberOfAs
	 */
	public long getNumberOfAs() {
		return numberOfAs;
	}


	/**
	 * @return the meanARelativeContent
	 */
	public double getMeanARelativeContent() {
		return meanARelativeContent;
	}

	/**
	 * @return the numberOfCs
	 */
	public long getNumberOfCs() {
		return numberOfCs;
	}


	/**
	 * @return the meanCRelativeContent
	 */
	public double getMeanCRelativeContent() {
		return meanCRelativeContent;
	}

	/**
	 * @return the numberOfTs
	 */
	public long getNumberOfTs() {
		return numberOfTs;
	}


	/**
	 * @return the meanTRelativeContent
	 */
	public double getMeanTRelativeContent() {
		return meanTRelativeContent;
	}

	/**
	 * @return the numberOfGs
	 */
	public long getNumberOfGs() {
		return numberOfGs;
	}

	/**
	 * @return the meanGRelativeContent
	 */
	public double getMeanGRelativeContent() {
		return meanGRelativeContent;
	}

	/**
	 * @return the numberOfNs
	 */
	public long getNumberOfNs() {
		return numberOfNs;
	}

	/**
	 * @return the meanNRelativeContent
	 */
	public double getMeanNRelativeContent() {
		return meanNRelativeContent;
	}

	/**
	 * @return the meanGcRelativeContentPerWindow
	 */
	public double getMeanGcRelativeContentPerWindow() {
		return meanGcRelativeContentPerWindow;
	}

	/**
	 * @return the meanGcRelativeContent
	 */
	public double getMeanGcRelativeContent() {
		return meanGcRelativeContent;
	}


	/**
	 * @return the gcRelativeContentAcrossReference
	 */
	public List<Double> getGcRelativeContentAcrossReference() {
		return gcRelativeContentAcrossReference;
	}

	/**
	 * @return the numberOfWindows
	 */
	public int getNumberOfWindows() {
		return numberOfWindows;
	}

	/**
	 * @return the numberOfProcessedWindows
	 */
	public int getNumberOfProcessedWindows() {
		return numberOfProcessedWindows;
	}

	/**
	 * @return the numberOfInitializedWindows
	 */
	public int getNumberOfInitializedWindows() {
		return numberOfInitializedWindows;
	}

    public double getMeanCoverage() {
        return meanCoverage;
    }

    public double getStdCoverage() {
        return stdCoverage;
    }


	/**
	 * @return the meanInsertSize
	 */
	public double getMeanInsertSize() {
		return meanInsertSize;
	}


	/**
	 * @return the insertSizeAcrossReference
	 */
	public List<Double> getInsertSizeAcrossReference() {
		return insertSizeAcrossReference;
	}

	/**
	 * @return the insertSizeHistogram
	 */
	public XYVector getInsertSizeHistogram() {
		return insertSizeHistogram;
	}

    public long getInRegionReferenceSize() {
        return inRegionReferenceSize;
    }

    public void setInRegionReferenceSize(long inRegionReferenceSize) {
        this.inRegionReferenceSize = inRegionReferenceSize;
    }

    public int getNumSelectedRegions() {
        return numSelectedRegions;
    }

    public void setNumSelectedRegions(int numSelectedRegions) {
        this.numSelectedRegions = numSelectedRegions;
    }

    public long getNumberOfMappedReadsInRegions() {
        return numberOfMappedReadsInRegions;
    }

    public void setNumberOfMappedReadsInRegions(long numberOfMappedReadsInRegions) {
        this.numberOfMappedReadsInRegions = numberOfMappedReadsInRegions;
    }
    public double getPercentageOfInsideMappedReads() {
        return (numberOfMappedReadsInRegions / (double) numberOfReads) * 100.0;
    }


    public XYVector getGcContentHistogram() {
        XYVector result = new XYVector();

        int iterCount = NUM_BINS / 100;
        int counter = 0;
        double acum = 0;
        double index = 1;
        for (int i = 1; i < NUM_BINS + 1; ++i) {
            counter++;
            acum += gcContentHistogram[i];
            if (counter == iterCount) {
                result.addItem( new XYItem(index, acum));
                counter = 0;
                acum = 0;
                index++;
            }
        }

        return result;
    }

    public void addReadStatsData(ReadStatsCollector readStatsCollector) {


        addReadsAsData(readStatsCollector.getReadsAContent());
        addReadsCsData(readStatsCollector.getReadsCContent());
        addReadsGsData(readStatsCollector.getReadsGContent());
        addReadsTsData(readStatsCollector.getReadsTContent());
        addReadsNsData(readStatsCollector.getReadsNContent());
        addReadsClippingInfo(readStatsCollector.getReadsClippingInfo());

        ArrayList<Float> readsGcContent = readStatsCollector.getReadsGcContent();
        for (float val : readsGcContent) {
            //System.out.println(val);
            gcContentHistogram[ (int) (val * NUM_BINS) ]++;
        }
        sampleCount += readsGcContent.size();

        numClippedReads += readStatsCollector.getNumClippedReads();
        numReadsWithInsertion += readStatsCollector.getNumReadsWithInsertion();
        numReadsWithDeletion += readStatsCollector.getNumReadsWithDeletion();

        int[] homopolymerIndels = readStatsCollector.getHomopolymerIndels();
        int numHomopolymerIndels = 0;
        for (int i = 0; i < 5; ++i) {
            homopolymerIndelsData[i] += homopolymerIndels[i];
            numHomopolymerIndels += homopolymerIndels[i];
        }

        numInsertions += readStatsCollector.getNumInsertions();
        numDeletions += readStatsCollector.getNumDeletions();
        numMismatches += readStatsCollector.getNumMismatches();
        acumEditDistance += readStatsCollector.getEditDistance();


        homopolymerIndelsData[5] += readStatsCollector.getNumIndels() - numHomopolymerIndels;

    }


    public Collection<CategoryItem> getHomopolymerIndels() {
        ArrayList<CategoryItem> res = new ArrayList<CategoryItem>();

        res.add( new CategoryItem("polyA", homopolymerIndelsData[0] ) );
        res.add( new CategoryItem("polyC", homopolymerIndelsData[1] ) );
        res.add( new CategoryItem("polyG", homopolymerIndelsData[2] ) );
        res.add( new CategoryItem("polyT", homopolymerIndelsData[3] ) );
        res.add( new CategoryItem("polyN", homopolymerIndelsData[4] ) );
        res.add( new CategoryItem("Non-poly", homopolymerIndelsData[5] ) );

        return res;
    }

    public long getNumberOfPairedReads() {
        return numberOfPairedReads;
    }

    public void setNumberOfPairedReads(long numberOfPairedReads) {
        this.numberOfPairedReads = numberOfPairedReads;
    }

    public double getPercentageOfPairedReads() {
        return (numberOfPairedReads / (double) numberOfReads) * 100.0;
    }


    public long getNumberOfSingletons() {
        return numberOfSingletons;
    }

    public void setNumberOfSingletons(long numberOfSingletons) {
        this.numberOfSingletons = numberOfSingletons;
    }

    public double getPercentageOfSingletons() {
        return (numberOfSingletons / (double) numberOfReads) * 100.0;
    }

    public void addReadsAsData(int[] readsAContent) {
        ensureListSize(readsAsData, readsAContent.length);
        for (int i = 0; i < readsAContent.length; ++i) {
            long val = readsAsData.get(i);
            val += readsAContent[i];
            readsAsData.set(i, val);
        }
    }

    public void addReadsCsData(int[] readsCContent) {
        ensureListSize(readsCsData, readsCContent.length);
        for (int i = 0; i < readsCContent.length; ++i) {
            long val = readsCsData.get(i);
            val += readsCContent[i];
            readsCsData.set(i, val);
        }
    }

    public void addReadsGsData(int[] readsGContent) {
        ensureListSize(readsGsData, readsGContent.length);
        for (int i = 0; i < readsGContent.length; ++i) {
            long val = readsGsData.get(i);
            val += readsGContent[i];
            readsGsData.set(i, val);
        }
    }

    public void addReadsTsData(int[] readsTContent) {
        ensureListSize(readsTsData, readsTContent.length);
        for (int i = 0; i < readsTContent.length; ++i) {
            long val = readsTsData.get(i);
            val += readsTContent[i];
            readsTsData.set(i, val);
        }
    }

    public void addReadsNsData(int[] readsNContent) {
        ensureListSize(readsNsData, readsNContent.length);
        for (int i = 0; i < readsNContent.length; ++i) {
            long val = readsNsData.get(i);
            val += readsNContent[i];
            readsNsData.set(i, val);
        }
    }

    public void addReadsClippingInfo(int[] readsClippingInfo) {
        ensureListSize(readsClippingData, readsClippingInfo.length);
        for (int i = 0; i < readsClippingInfo.length; ++i) {
            long val = readsClippingData.get(i);
            val += readsClippingInfo[i];
            readsClippingData.set(i, val);
        }
    }


    public boolean clippingIsPresent() {

        for (long val : readsClippingData) {
            if (val > 0) {
                return true;
            }
        }

        return false;
    }

    public int getReadMaxSize() {
        return readMaxSize;
    }

    public void setReadMaxSize(int readMaxSize) {
        this.readMaxSize = readMaxSize;
    }

    public double getReadMeanSize() {
        return readMeanSize;
    }

    public void setReadMeanSize(double readMeanSize) {
        this.readMeanSize = readMeanSize;
    }

    public int getReadMinSize() {
        return  readMinSize;
    }

    public void setReadMinSize(int minReadSize) {
        this.readMinSize = minReadSize;
    }


    private static void ensureListSize(List<Long> list, int expectedSize) {
        int listSize = list.size();
        if (listSize < expectedSize) {
            int numElements = expectedSize - listSize + 1;
            for (int i = 0; i < numElements; ++i) {
                list.add(0L);
            }
        }

    }

    public void computeChromosomeStats(GenomeLocator locator, ArrayList<Integer> chromosomeWindowIndexes) throws IOException {
        int chromosomeCount = chromosomeWindowIndexes.size();
        List<ContigRecord> contigRecords = locator.getContigs();

        //chromoWriter.println("#name\tabsolute_pos\tmapped_bases\tmean_coverage\tstd_coverage");

        chromosomeStats = new ChromosomeInfo[chromosomeCount];

        for (int k = 0; k < chromosomeCount; ++k) {
            int firstWindowIndex = chromosomeWindowIndexes.get(k);
            int lastWindowIndex = k + 1 < chromosomeCount
                    ? chromosomeWindowIndexes.get(k + 1) - 1 : numberOfWindows - 1;

            long numBases = 0;
            long length = 0;
            long sumCovSquared = 0;
            for (int i = firstWindowIndex; i <= lastWindowIndex; ++i) {
                numBases += numMappedBasesPerWindow.get(i);
                sumCovSquared += coverageSquaredPerWindow.get(i);
                length += windowLengthes.get(i);
            }

            ContigRecord contig = contigRecords.get(k);

            ChromosomeInfo info = new ChromosomeInfo();
            info.name = contig.getName();
            if (length != 0) {
                info.length = length;
                info.numBases = numBases;
                info.covMean =  numBases / (double) length;
                info.covStd = Math.sqrt( sumCovSquared / (double)length - info.covMean *info.covMean);
            }
            chromosomeStats[k] = info;

            //if (length == 0) {
            //    chromoWriter.println("0\t0\t0\t");
            //} else {
            //    chromoWriter.print(numBases + "\t");
            //    double mean =  numBases / length;
            //    double std = Math.sqrt( sumCovSquared / length - mean*mean);
                //chromoWriter.print(StringUtils.decimalFormat(mean,"#,###,###,###.##")+ "\t" );
                //chromoWriter.println(StringUtils.decimalFormat(std, "#,###,###,###.##"));
            //}
        }

    }

    public void setCoverageMean(double covMean) {
        this.meanCoverage = covMean;
    }

    public void setCoverageStd(double covStd) {
        this.stdCoverage = covStd;
    }

    public void setMeanMappingQuality(double mmq) {
        this.meanMappingQualityPerWindow = mmq;
    }

    public void setMeanGcContent(double gcContent) {
        this.meanGcRelativeContent = gcContent;
    }

    public void setMedianInsertSize(int insertSize) {
        this.medianInsertSize = insertSize;
    }

    public XYVector getBalancedCoverageHistogram() {
        return balancedCoverageHistogram;
    }

    public Map<Double,String> getBalancedCoverageBarNames() {
        return balancedCoverageBarNames;
    }

    public void addWarning(String warningIdChromosomeNotFound, String msg) {
        warnings.put(warningIdChromosomeNotFound, msg);
    }

    public Map<String,String> getWarnings() {
        return warnings;
    }

    public int getP25InsertSize() {
        return p25InsertSize;
    }

    public int getMedianInsertSize() {
        return medianInsertSize;
    }

    public int getP75InsertSize() {
        return p75InsertSize;
    }

    public double getStdInsertSize()  {
        return stdInsertSize;
    }

    public long getNumberOfMappedFirstOfPair() {
        return numberOfMappedFirstOfPair;
    }

    public void setNumberOfMappedFirstOfPair(long numberOfMappedFirstOfPair) {
        this.numberOfMappedFirstOfPair = numberOfMappedFirstOfPair;
    }

    public long getNumberOfMappedSecondOfPair() {
        return numberOfMappedSecondOfPair;
    }

    public void setNumberOfMappedSecondOfPair(long numberOfMappedSecondOfPair) {
        this.numberOfMappedSecondOfPair = numberOfMappedSecondOfPair;
    }

    public long getNumberOfSingletonsInRegions() {
        return numberOfSingletonsInRegions;
    }

    public void setNumberOfSingletonsInRegions(long numberOfSingletonsInRegions) {
        this.numberOfSingletonsInRegions = numberOfSingletonsInRegions;
    }

    public long getNumberOfMappedFirstOfPairInRegions() {
        return numberOfMappedFirstOfPairInRegions;
    }

    public void setNumberOfMappedFirstOfPairInRegions(long numberOfMappedFirstOfPairInRegions) {
        this.numberOfMappedFirstOfPairInRegions = numberOfMappedFirstOfPairInRegions;
    }

    public long getNumberOfMappedSecondOfPairInRegions() {
        return numberOfMappedSecondOfPairInRegions;
    }

    public void setNumberOfMappedSecondOfPairInRegions(long numberOfMappedSecondOfPairInRegions) {
        this.numberOfMappedSecondOfPairInRegions = numberOfMappedSecondOfPairInRegions;
    }


    public void updateInsertSizeHistogram(int insertSize) {
        if (insertSize >= 0 ) {
            insertSizeArray.add(insertSize);
        }
    }


    public void setNumberOfPairedReadsInRegions(long numPairedReads) {
        numberOfPairedReadsInRegions = numPairedReads;
    }

    public long getNumberOfPairedReadsInRegions() {
        return numberOfPairedReadsInRegions;
    }

    public double getDuplicationRate() {
        return duplicationRate;
    }

    public boolean updateReadStartsHistogram(long position) {
        boolean duplicate = readStartsHistogram.update(position);
        if (duplicate) {
            estimatedNumDuplicatedReads++;
        }
        return duplicate;
    }

    public int getNumClippedReads() {
        return numClippedReads;
    }

    public double getReadsWithInsertionPercentage() {
        return (numReadsWithInsertion / (double) numberOfMappedReads) * 100.0;
    }

    public double getReadsWithDeletionPercentage() {
        return (numReadsWithDeletion / (double) numberOfMappedReads) * 100.0;
    }

    public void setNumberOfCorrectStrandReads(long numberOfCorrectStrandReads) {
        this.numCorrectStrandReads = numberOfCorrectStrandReads;
    }

    public long getNumCorrectStrandReads() {
        return numCorrectStrandReads;
    }

    public int getNumIndels() {
        return numInsertions + numDeletions;
    }

    public int getNumInsertions() {
        return numInsertions;
    }

    public int getNumDeletions() {
        return numDeletions;
    }

    public double getHomopolymerIndelFraction() {

        return (1.0 - homopolymerIndelsData[5] / (double) (numInsertions + numDeletions));
    }

}
