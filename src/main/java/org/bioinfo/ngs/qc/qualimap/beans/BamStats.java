/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2012 Garcia-Alcalde et al.
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

import org.apache.commons.math.stat.StatUtils;
import org.bioinfo.commons.utils.ArrayUtils;
import org.bioinfo.commons.utils.ListUtils;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.math.util.MathUtils;
import org.bioinfo.ngs.qc.qualimap.process.ReadStatsCollector;
import org.bioinfo.ngs.qc.qualimap.common.ReadStartsHistogram;

public class BamStats implements Serializable {
	private String name;
	private String sourceFile;
	private String referenceFile;
	
	// globals
	private long numberOfMappedBases;
	private long numberOfSequencedBases;
	private long numberOfAlignedBases;
	private long numberOfReads;
	private long numberOfValidReads;
	private double percentageOfValidReads;
	private int numberOfMappedReads;
	private int numberOfPairedReads;
    private int numberOfSingletons;
    private int numberOfMappedFirstOfPair;
    private int numberOfMappedSecondOfPair;

    // regions related
    private  int numberOfMappedReadsInRegions;
    private int numberOfPairedReadsInRegions;
    private int numberOfSingletonsInRegions;
    private int numberOfMappedFirstOfPairInRegions;
    private int numberOfMappedSecondOfPairInRegions;
    private int numCorrectStrandReads;

    private ArrayList<Long>  numMappedBasesPerWindow;
    private ArrayList<Long> coverageSquaredPerWindow;
    private ArrayList<Double> windowLengthes;


	/*
	 * 
	 *  Reference params
	 *   
	 */
	
	private long referenceSize;
	private long numberOfReferenceContigs;

    private long inRegionReferenceSize;
    private int numSelectedRegions;
	private boolean referenceAvailable;
	
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
	
//	// AT content
//	private double meanAtContent;
//	private double meanAtContentPerWindow;
//	private double meanAtRelativeContentPerWindow;
//	private double meanAtRelativeContent;
//	private List<Double> atContentAcrossReference;
//	private List<Double> atRelativeContentAcrossReference;
		
	// insert size
	private double meanInsertSize;
	private double meanInsertSizePerWindow;
    private double medianInsertSize;
    private List<Double> insertSizeAcrossReference;
	private XYVector insertSizeHistogram;
    private ArrayList<Integer> insertSizeArray;
	private HashMap<Long,Long> insertSizeHistogramMap;
    private long[] insertSizeHistogramCache;

    // reads stats
    double readMeanSize;
    int readMaxSize, readMinSize;
    int numClippedReads;
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
	private String windowReportFile;
	transient private PrintWriter windowReport;
	private boolean activeCoverageReporting;
	private String coverageReportFile;
	transient private PrintWriter coverageReport;
    private long sumCoverageSquared;
    private final int CACHE_SIZE = 2000;
    private Map<String,String> warnings;


    // gc content histogram
    public static int  NUM_BINS = 1000;
    static int SMOOTH_DISTANCE = 0;
    private double[] gcContentHistogram;
    boolean avaialableGenomeGcContentData;
    long sampleCount;

    private int numInsertions;
    private int numDeletions;

    public BamStats(String name, long referenceSize, int numberOfWindows){

		// global
		this.name = name;
		this.referenceSize = referenceSize;
		this.numberOfWindows = numberOfWindows;
		numberOfMappedBases = 0;
		numberOfSequencedBases = 0;
		numberOfAlignedBases = 0;
						
		// reference
		  // ACTG across reference arrays		
		aContentInReference = new ArrayList<Double>(numberOfWindows);
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
		atRelativeContentInReference = new ArrayList<Double>(numberOfWindows);

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
        windowLengthes = new ArrayList<Double>(numberOfWindows);



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

	public void setWindowReferences(GenomeLocator locator){
		windowNames = new String[numberOfWindows];
		windowSizes = new long[numberOfWindows];
		windowStarts = new long[numberOfWindows];
		windowEnds = new long[numberOfWindows];
		ContigRecord contig;
		for(int i=0; i<locator.getContigs().size(); i++){
			contig = locator.getContigs().get(i);			
			windowNames[i]= contig.getName();
			windowSizes[i] = contig.getSize();
			windowStarts[i] = contig.getStart();
			windowEnds[i] = contig.getEnd();
		}
		windowPositionsAvailable=true;
	}
	
	public void setWindowReferences(String prefix,int windowSize){
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
	}

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
		this.windowReportFile = windowReportFile;
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
	
	
	public void activateCoverageReporting(String coverageReportFile) throws FileNotFoundException{
		this.coverageReportFile = coverageReportFile;
		this.coverageReport = new PrintWriter(new File(coverageReportFile));
		this.activeCoverageReporting = true;
		reportCoverageHeader();
	}
	
	public void closeCoverageReporting(){
		this.coverageReport.close();
	}
	
	public void reportCoverageHeader(){
//		coverageReport.println("#position\tcoverageData\tAs\tCs\tTs\tGs");
		coverageReport.println("#position\tcoverageData");
	}
	
	public void reportCoverage(BamDetailedGenomeWindow window){
		coverageReport.println("# window " + window.getName());
		for(int i=0; i<window.getCoverageAcrossReference().length; i++){
			coverageReport.print((window.getStart()+i) + "\t");
			coverageReport.print(window.getCoverageAcrossReference()[i]);
//			coverageReport.print(window.getaContentAcrossReference()[i] + "\t");
//			coverageReport.print(window.getcContentAcrossReference()[i] + "\t");
//			coverageReport.print(window.gettContentAcrossReference()[i] + "\t");
//			coverageReport.print(window.getgContentAcrossReference()[i] + "\t");
//			coverageReport.print(window.getnContentAcrossReference()[i] + "\t");
			coverageReport.println();
			coverageReport.flush();
		}		
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
			if(activeCoverageReporting) reportCoverage((BamDetailedGenomeWindow)window);
		}
	}
	
	public void computeDescriptors(){

		//TODO: this can be parallel!

		/* Reference */

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

		// insert size
        double[] insertData = ListUtils.toDoubleArray(insertSizeAcrossReference);
		meanInsertSizePerWindow = MathUtils.mean(insertData);
		meanInsertSize = meanInsertSizePerWindow;
        medianInsertSize = StatUtils.percentile(insertData, 50);


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

    private double calcMedianInsertSize(XYVector insertSizeHistogram) {
        int size = insertSizeHistogram.getSize();
        double[] values = insertSizeHistogram.getYVector();

        if (values.length == 0) {
            return 0;
        }

        double maxValue = values[0];
        int maxValueIndex = 0;

        for (int i = 1; i < size; ++i) {
            double curVal = values[i];
            if (curVal > maxValue) {
                maxValue = curVal;
                maxValueIndex = i;
            }
        }

        return insertSizeHistogram.get(maxValueIndex).getX();
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

        for (int i = 0; i <= readMaxSize; ++i ) {

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


    public int getHistogramSize(){
		// read keys
		Object[] raw = coverageHistogramMap.keySet().toArray();
		int totalCoverage = 0;
		for(int i=0; i<raw.length; i++) {
			int key = (Integer)raw[i];
			totalCoverage+=coverageHistogramMap.get(key);			
		}
		return totalCoverage;
	}


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
        int percentile75Index = size * 3/ 4;

        medianInsertSize = insertSizeArray.get(medianIndex);

        double border = insertSizeArray.get(percentile75Index)*2;

        for (int val : insertSizeArray) {
            if (val < border) {
                updateHistogramValue(insertSizeHistogramCache, insertSizeHistogramMap, (long) val);
            }
        }

        addCacheDataToMap(insertSizeHistogramCache,insertSizeHistogramMap);
        insertSizeHistogram = computeVectorHistogram(insertSizeHistogramMap);

        /*medianInsertSize = calcMedianInsertSize(insertSizeHistogram);*/




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
		
//		// compute insert size histogram
//		double[] insertSizes = new double[insertSizeHistogramMap.size()];
//		insertSizeHistogram = new XYVector();
//		long iacum = 0;
//		for(int i=0; i<sortedCoverages.length; i++){
//			acum+=sortedFreqs[i];
//			acumCoverageHistogram.addItem(new XYItem(sortedCoverages[i],acum));//(acum/(double)totalCoverage)*100.0));
//		}

		
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
	
	public long getWindowSize(int index){
		return windowSizes[index];
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
	
	public String getNextWindowName(){
		return windowNames[numberOfProcessedWindows+1];
	}
	
	public long getNextWindowStart(){
		return windowStarts[numberOfProcessedWindows+1];
	}
	
	public long getNextWindowEnd(){
		return windowEnds[numberOfProcessedWindows+1];
	}
	
	public String getNextInitializedWindowName(){
		return windowNames[numberOfInitializedWindows+1];
	}
	
	public long getNextInitializedWindowStart(){
		return windowStarts[numberOfInitializedWindows+1];
	}
	
	public long getNextInitializedWindowEnd(){
		return windowEnds[numberOfInitializedWindows+1];		
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the numberOfMappedBases
	 */
	public long getNumberOfMappedBases() {
		return numberOfMappedBases;
	}

	/**
	 * @param numberOfMappedBases the numberOfMappedBases to set
	 */
	public void setNumberOfMappedBases(long numberOfMappedBases) {
		this.numberOfMappedBases = numberOfMappedBases;
	}

	/**
	 * @return the numberOfSequencedBases
	 */
	public long getNumberOfSequencedBases() {
		return numberOfSequencedBases;
	}

	/**
	 * @param numberOfSequencedBases the numberOfSequencedBases to set
	 */
	public void setNumberOfSequencedBases(long numberOfSequencedBases) {
		this.numberOfSequencedBases = numberOfSequencedBases;
	}

	/**
	 * @return the numberOfAlignedBases
	 */
	public long getNumberOfAlignedBases() {
		return numberOfAlignedBases;
	}

	/**
	 * @param numberOfAlignedBases the numberOfAlignedBases to set
	 */
	public void setNumberOfAlignedBases(long numberOfAlignedBases) {
		this.numberOfAlignedBases = numberOfAlignedBases;
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
	 * @return the numberOfValidReads
	 */
	public long getNumberOfValidReads() {
		return numberOfValidReads;
	}

	/**
	 * @param numberOfValidReads the numberOfValidReads to set
	 */
	public void setNumberOfValidReads(long numberOfValidReads) {
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
	 * @return the numberOfAsInReference
	 */
	public long getNumberOfAsInReference() {
		return numberOfAsInReference;
	}

	/**
	 * @param numberOfAsInReference the numberOfAsInReference to set
	 */
	public void setNumberOfAsInReference(long numberOfAsInReference) {
		this.numberOfAsInReference = numberOfAsInReference;
	}

	/**
	 * @return the meanAContentPerWindowInReference
	 */
	public double getMeanAContentPerWindowInReference() {
		return meanAContentPerWindowInReference;
	}

	/**
	 * @param meanAContentPerWindowInReference the meanAContentPerWindowInReference to set
	 */
	public void setMeanAContentPerWindowInReference(
			double meanAContentPerWindowInReference) {
		this.meanAContentPerWindowInReference = meanAContentPerWindowInReference;
	}

	/**
	 * @return the meanARelativeContentPerWindowInReference
	 */
	public double getMeanARelativeContentPerWindowInReference() {
		return meanARelativeContentPerWindowInReference;
	}

	/**
	 * @param meanARelativeContentPerWindowInReference the meanARelativeContentPerWindowInReference to set
	 */
	public void setMeanARelativeContentPerWindowInReference(
			double meanARelativeContentPerWindowInReference) {
		this.meanARelativeContentPerWindowInReference = meanARelativeContentPerWindowInReference;
	}

	/**
	 * @return the aContentInReference
	 */
	public List<Double> getaContentInReference() {
		return aContentInReference;
	}

	/**
	 * @param aContentInReference the aContentInReference to set
	 */
	public void setaContentInReference(List<Double> aContentInReference) {
		this.aContentInReference = aContentInReference;
	}

	/**
	 * @return the aRelativeContentInReference
	 */
	public List<Double> getaRelativeContentInReference() {
		return aRelativeContentInReference;
	}

	/**
	 * @param aRelativeContentInReference the aRelativeContentInReference to set
	 */
	public void setaRelativeContentInReference(
			List<Double> aRelativeContentInReference) {
		this.aRelativeContentInReference = aRelativeContentInReference;
	}

	/**
	 * @return the numberOfCsInReference
	 */
	public long getNumberOfCsInReference() {
		return numberOfCsInReference;
	}

	/**
	 * @param numberOfCsInReference the numberOfCsInReference to set
	 */
	public void setNumberOfCsInReference(long numberOfCsInReference) {
		this.numberOfCsInReference = numberOfCsInReference;
	}

	/**
	 * @return the meanCContentPerWindowInReference
	 */
	public double getMeanCContentPerWindowInReference() {
		return meanCContentPerWindowInReference;
	}

	/**
	 * @param meanCContentPerWindowInReference the meanCContentPerWindowInReference to set
	 */
	public void setMeanCContentPerWindowInReference(
			double meanCContentPerWindowInReference) {
		this.meanCContentPerWindowInReference = meanCContentPerWindowInReference;
	}

	/**
	 * @return the meanCRelativeContentPerWindowInReference
	 */
	public double getMeanCRelativeContentPerWindowInReference() {
		return meanCRelativeContentPerWindowInReference;
	}

	/**
	 * @param meanCRelativeContentPerWindowInReference the meanCRelativeContentPerWindowInReference to set
	 */
	public void setMeanCRelativeContentPerWindowInReference(
			double meanCRelativeContentPerWindowInReference) {
		this.meanCRelativeContentPerWindowInReference = meanCRelativeContentPerWindowInReference;
	}

	/**
	 * @return the cContentInReference
	 */
	public List<Double> getcContentInReference() {
		return cContentInReference;
	}

	/**
	 * @param cContentInReference the cContentInReference to set
	 */
	public void setcContentInReference(List<Double> cContentInReference) {
		this.cContentInReference = cContentInReference;
	}

	/**
	 * @return the cRelativeContentInReference
	 */
	public List<Double> getcRelativeContentInReference() {
		return cRelativeContentInReference;
	}

	/**
	 * @param cRelativeContentInReference the cRelativeContentInReference to set
	 */
	public void setcRelativeContentInReference(
			List<Double> cRelativeContentInReference) {
		this.cRelativeContentInReference = cRelativeContentInReference;
	}

	/**
	 * @return the numberOfTsInReference
	 */
	public long getNumberOfTsInReference() {
		return numberOfTsInReference;
	}

	/**
	 * @param numberOfTsInReference the numberOfTsInReference to set
	 */
	public void setNumberOfTsInReference(long numberOfTsInReference) {
		this.numberOfTsInReference = numberOfTsInReference;
	}

	/**
	 * @return the meanTContentPerWindowInReference
	 */
	public double getMeanTContentPerWindowInReference() {
		return meanTContentPerWindowInReference;
	}

	/**
	 * @param meanTContentPerWindowInReference the meanTContentPerWindowInReference to set
	 */
	public void setMeanTContentPerWindowInReference(
			double meanTContentPerWindowInReference) {
		this.meanTContentPerWindowInReference = meanTContentPerWindowInReference;
	}

	/**
	 * @return the meanTRelativeContentPerWindowInReference
	 */
	public double getMeanTRelativeContentPerWindowInReference() {
		return meanTRelativeContentPerWindowInReference;
	}

	/**
	 * @param meanTRelativeContentPerWindowInReference the meanTRelativeContentPerWindowInReference to set
	 */
	public void setMeanTRelativeContentPerWindowInReference(
			double meanTRelativeContentPerWindowInReference) {
		this.meanTRelativeContentPerWindowInReference = meanTRelativeContentPerWindowInReference;
	}

	/**
	 * @return the tContentInReference
	 */
	public List<Double> gettContentInReference() {
		return tContentInReference;
	}

	/**
	 * @param tContentInReference the tContentInReference to set
	 */
	public void settContentInReference(List<Double> tContentInReference) {
		this.tContentInReference = tContentInReference;
	}

	/**
	 * @return the tRelativeContentInReference
	 */
	public List<Double> gettRelativeContentInReference() {
		return tRelativeContentInReference;
	}

	/**
	 * @param tRelativeContentInReference the tRelativeContentInReference to set
	 */
	public void settRelativeContentInReference(
			List<Double> tRelativeContentInReference) {
		this.tRelativeContentInReference = tRelativeContentInReference;
	}

	/**
	 * @return the numberOfGsInReference
	 */
	public long getNumberOfGsInReference() {
		return numberOfGsInReference;
	}

	/**
	 * @param numberOfGsInReference the numberOfGsInReference to set
	 */
	public void setNumberOfGsInReference(long numberOfGsInReference) {
		this.numberOfGsInReference = numberOfGsInReference;
	}

	/**
	 * @return the meanGContentPerWindowInReference
	 */
	public double getMeanGContentPerWindowInReference() {
		return meanGContentPerWindowInReference;
	}

	/**
	 * @param meanGContentPerWindowInReference the meanGContentPerWindowInReference to set
	 */
	public void setMeanGContentPerWindowInReference(
			double meanGContentPerWindowInReference) {
		this.meanGContentPerWindowInReference = meanGContentPerWindowInReference;
	}

	/**
	 * @return the meanGRelativeContentPerWindowInReference
	 */
	public double getMeanGRelativeContentPerWindowInReference() {
		return meanGRelativeContentPerWindowInReference;
	}


	/**
	 * @return the numberOfNsInReference
	 */
	public long getNumberOfNsInReference() {
		return numberOfNsInReference;
	}

	/**
	 * @param numberOfNsInReference the numberOfNsInReference to set
	 */
	public void setNumberOfNsInReference(long numberOfNsInReference) {
		this.numberOfNsInReference = numberOfNsInReference;
	}

	/**
	 * @return the meanNContentPerWindowInReference
	 */
	public double getMeanNContentPerWindowInReference() {
		return meanNContentPerWindowInReference;
	}

	/**
	 * @param meanNContentPerWindowInReference the meanNContentPerWindowInReference to set
	 */
	public void setMeanNContentPerWindowInReference(
			double meanNContentPerWindowInReference) {
		this.meanNContentPerWindowInReference = meanNContentPerWindowInReference;
	}

	/**
	 * @return the meanNRelativeContentPerWindowInReference
	 */
	public double getMeanNRelativeContentPerWindowInReference() {
		return meanNRelativeContentPerWindowInReference;
	}

	/**
	 * @param meanNRelativeContentPerWindowInReference the meanNRelativeContentPerWindowInReference to set
	 */
	public void setMeanNRelativeContentPerWindowInReference(
			double meanNRelativeContentPerWindowInReference) {
		this.meanNRelativeContentPerWindowInReference = meanNRelativeContentPerWindowInReference;
	}

	/**
	 * @return the nContentInReference
	 */
	public List<Double> getnContentInReference() {
		return nContentInReference;
	}

	/**
	 * @param nContentInReference the nContentInReference to set
	 */
	public void setnContentInReference(List<Double> nContentInReference) {
		this.nContentInReference = nContentInReference;
	}

	/**
	 * @return the nRelativeContentInReference
	 */
	public List<Double> getnRelativeContentInReference() {
		return nRelativeContentInReference;
	}

	/**
	 * @param nRelativeContentInReference the nRelativeContentInReference to set
	 */
	public void setnRelativeContentInReference(
			List<Double> nRelativeContentInReference) {
		this.nRelativeContentInReference = nRelativeContentInReference;
	}

	/**
	 * @return the meanGcContentInReference
	 */
	public double getMeanGcContentInReference() {
		return meanGcContentInReference;
	}

	/**
	 * @param meanGcContentInReference the meanGcContentInReference to set
	 */
	public void setMeanGcContentInReference(double meanGcContentInReference) {
		this.meanGcContentInReference = meanGcContentInReference;
	}

	/**
	 * @return the meanGcContentPerWindowInReference
	 */
	public double getMeanGcContentPerWindowInReference() {
		return meanGcContentPerWindowInReference;
	}

	/**
	 * @param meanGcContentPerWindowInReference the meanGcContentPerWindowInReference to set
	 */
	public void setMeanGcContentPerWindowInReference(
			double meanGcContentPerWindowInReference) {
		this.meanGcContentPerWindowInReference = meanGcContentPerWindowInReference;
	}

	/**
	 * @return the meanGcRelativeContentPerWindowInReference
	 */
	public double getMeanGcRelativeContentPerWindowInReference() {
		return meanGcRelativeContentPerWindowInReference;
	}

	/**
	 * @param meanGcRelativeContentPerWindowInReference the meanGcRelativeContentPerWindowInReference to set
	 */
	public void setMeanGcRelativeContentPerWindowInReference(
			double meanGcRelativeContentPerWindowInReference) {
		this.meanGcRelativeContentPerWindowInReference = meanGcRelativeContentPerWindowInReference;
	}

	/**
	 * @return the gcContentInReference
	 */
	public List<Double> getGcContentInReference() {
		return gcContentInReference;
	}

	/**
	 * @param gcContentInReference the gcContentInReference to set
	 */
	public void setGcContentInReference(List<Double> gcContentInReference) {
		this.gcContentInReference = gcContentInReference;
	}

	/**
	 * @return the gcRelativeContentInReference
	 */
	public List<Double> getGcRelativeContentInReference() {
		return gcRelativeContentInReference;
	}

	/**
	 * @param gcRelativeContentInReference the gcRelativeContentInReference to set
	 */
	public void setGcRelativeContentInReference(
			List<Double> gcRelativeContentInReference) {
		this.gcRelativeContentInReference = gcRelativeContentInReference;
	}

	/**
	 * @return the meanAtContentInReference
	 */
	public double getMeanAtContentInReference() {
		return meanAtContentInReference;
	}


	/**
	 * @return the meanAtContentPerWindowInReference
	 */
	public double getMeanAtContentPerWindowInReference() {
		return meanAtContentPerWindowInReference;
	}


	/**
	 * @return the meanAtRelativeContentPerWindowInReference
	 */
	public double getMeanAtRelativeContentPerWindowInReference() {
		return meanAtRelativeContentPerWindowInReference;
	}

	/**
	 * @param meanAtRelativeContentPerWindowInReference the meanAtRelativeContentPerWindowInReference to set
	 */
	public void setMeanAtRelativeContentPerWindowInReference(
			double meanAtRelativeContentPerWindowInReference) {
		this.meanAtRelativeContentPerWindowInReference = meanAtRelativeContentPerWindowInReference;
	}

	/**
	 * @return the atContentInReference
	 */
	public List<Double> getAtContentInReference() {
		return atContentInReference;
	}

	/**
	 * @param atContentInReference the atContentInReference to set
	 */
	public void setAtContentInReference(List<Double> atContentInReference) {
		this.atContentInReference = atContentInReference;
	}

	/**
	 * @return the atRelativeContentInReference
	 */
	public List<Double> getAtRelativeContentInReference() {
		return atRelativeContentInReference;
	}

	/**
	 * @param atRelativeContentInReference the atRelativeContentInReference to set
	 */
	public void setAtRelativeContentInReference(
			List<Double> atRelativeContentInReference) {
		this.atRelativeContentInReference = atRelativeContentInReference;
	}

	/**
	 * @return the meanCoverage
	 */
	public double getMeanCoverage() {
		return meanCoverage;
	}

	/**
	 * @param meanCoverage the meanCoverage to set
	 */
	public void setMeanCoverage(double meanCoverage) {
		this.meanCoverage = meanCoverage;
	}

	/**
	 * @return the meanCoveragePerWindow
	 */
	public double getMeanCoveragePerWindow() {
		return meanCoveragePerWindow;
	}

	/**
	 * @param meanCoveragePerWindow the meanCoveragePerWindow to set
	 */
	public void setMeanCoveragePerWindow(double meanCoveragePerWindow) {
		this.meanCoveragePerWindow = meanCoveragePerWindow;
	}

	/**
	 * @return the stdCoverage
	 */
	public double getStdCoverage() {
		return stdCoverage;
	}

	/**
	 * @param stdCoverage the stdCoverage to set
	 */
	public void setStdCoverage(double stdCoverage) {
		this.stdCoverage = stdCoverage;
	}

	/**
	 * @return the coverageAcrossReference
	 */
	public List<Double> getCoverageAcrossReference() {
		return coverageAcrossReference;
	}

	/**
	 * @param coverageAcrossReference the coverageAcrossReference to set
	 */
	public void setCoverageAcrossReference(List<Double> coverageAcrossReference) {
		this.coverageAcrossReference = coverageAcrossReference;
	}

	/**
	 * @return the stdCoverageAcrossReference
	 */
	public List<Double> getStdCoverageAcrossReference() {
		return stdCoverageAcrossReference;
	}

	/**
	 * @param stdCoverageAcrossReference the stdCoverageAcrossReference to set
	 */
	public void setStdCoverageAcrossReference(
			List<Double> stdCoverageAcrossReference) {
		this.stdCoverageAcrossReference = stdCoverageAcrossReference;
	}


	/**
	 * @return the coverageHistogram
	 */
	public XYVector getCoverageHistogram() {
		return coverageHistogram;
	}

	/**
	 * @param coverageHistogram the coverageHistogram to set
	 */
	public void setCoverageHistogram(XYVector coverageHistogram) {
		this.coverageHistogram = coverageHistogram;
	}

	/**
	 * @return the acumCoverageHistogram
	 */
	public XYVector getAcumCoverageHistogram() {
		return acumCoverageHistogram;
	}

	/**
	 * @param acumCoverageHistogram the acumCoverageHistogram to set
	 */
	public void setAcumCoverageHistogram(XYVector acumCoverageHistogram) {
		this.acumCoverageHistogram = acumCoverageHistogram;
	}

	/**
	 * @return the maxCoverageQuota
	 */
	public int getMaxCoverageQuota() {
		return maxCoverageQuota;
	}

	/**
	 * @param maxCoverageQuota the maxCoverageQuota to set
	 */
	public void setMaxCoverageQuota(int maxCoverageQuota) {
		this.maxCoverageQuota = maxCoverageQuota;
	}

	/**
	 * @return the coverageQuotes
	 */
	public XYVector getCoverageQuotes() {
		return coverageQuotes;
	}

	/**
	 * @param coverageQuotes the coverageQuotes to set
	 */
	public void setCoverageQuotes(XYVector coverageQuotes) {
		this.coverageQuotes = coverageQuotes;
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
	 * @return the meanAContentPerWindow
	 */
	public double getMeanAContentPerWindow() {
		return meanAContentPerWindow;
	}

	/**
	 * @param meanAContentPerWindow the meanAContentPerWindow to set
	 */
	public void setMeanAContentPerWindow(double meanAContentPerWindow) {
		this.meanAContentPerWindow = meanAContentPerWindow;
	}

	/**
	 * @return the meanARelativeContentPerWindow
	 */
	public double getMeanARelativeContentPerWindow() {
		return meanARelativeContentPerWindow;
	}

	/**
	 * @param meanARelativeContentPerWindow the meanARelativeContentPerWindow to set
	 */
	public void setMeanARelativeContentPerWindow(
			double meanARelativeContentPerWindow) {
		this.meanARelativeContentPerWindow = meanARelativeContentPerWindow;
	}

	/**
	 * @return the meanARelativeContent
	 */
	public double getMeanARelativeContent() {
		return meanARelativeContent;
	}

	/**
	 * @param meanARelativeContent the meanARelativeContent to set
	 */
	public void setMeanARelativeContent(double meanARelativeContent) {
		this.meanARelativeContent = meanARelativeContent;
	}

	/**
	 * @return the aContentAcrossReference
	 */
	public List<Double> getaContentAcrossReference() {
		return aContentAcrossReference;
	}

	/**
	 * @param aContentAcrossReference the aContentAcrossReference to set
	 */
	public void setaContentAcrossReference(List<Double> aContentAcrossReference) {
		this.aContentAcrossReference = aContentAcrossReference;
	}


	/**
	 * @return the numberOfCs
	 */
	public long getNumberOfCs() {
		return numberOfCs;
	}

	/**
	 * @param numberOfCs the numberOfCs to set
	 */
	public void setNumberOfCs(long numberOfCs) {
		this.numberOfCs = numberOfCs;
	}

	/**
	 * @return the meanCContentPerWindow
	 */
	public double getMeanCContentPerWindow() {
		return meanCContentPerWindow;
	}

	/**
	 * @param meanCContentPerWindow the meanCContentPerWindow to set
	 */
	public void setMeanCContentPerWindow(double meanCContentPerWindow) {
		this.meanCContentPerWindow = meanCContentPerWindow;
	}

	/**
	 * @return the meanCRelativeContentPerWindow
	 */
	public double getMeanCRelativeContentPerWindow() {
		return meanCRelativeContentPerWindow;
	}

	/**
	 * @param meanCRelativeContentPerWindow the meanCRelativeContentPerWindow to set
	 */
	public void setMeanCRelativeContentPerWindow(
			double meanCRelativeContentPerWindow) {
		this.meanCRelativeContentPerWindow = meanCRelativeContentPerWindow;
	}

	/**
	 * @return the meanCRelativeContent
	 */
	public double getMeanCRelativeContent() {
		return meanCRelativeContent;
	}

	/**
	 * @param meanCRelativeContent the meanCRelativeContent to set
	 */
	public void setMeanCRelativeContent(double meanCRelativeContent) {
		this.meanCRelativeContent = meanCRelativeContent;
	}

	/**
	 * @return the cContentAcrossReference
	 */
	public List<Double> getcContentAcrossReference() {
		return cContentAcrossReference;
	}

	/**
	 * @param cContentAcrossReference the cContentAcrossReference to set
	 */
	public void setcContentAcrossReference(List<Double> cContentAcrossReference) {
		this.cContentAcrossReference = cContentAcrossReference;
	}

	/**
	 * @return the cRelativeContentAcrossReference
	 */
	public List<Double> getcRelativeContentAcrossReference() {
		return cRelativeContentAcrossReference;
	}

	/**
	 * @param cRelativeContentAcrossReference the cRelativeContentAcrossReference to set
	 */
	public void setcRelativeContentAcrossReference(
			List<Double> cRelativeContentAcrossReference) {
		this.cRelativeContentAcrossReference = cRelativeContentAcrossReference;
	}

	/**
	 * @return the numberOfTs
	 */
	public long getNumberOfTs() {
		return numberOfTs;
	}

	/**
	 * @param numberOfTs the numberOfTs to set
	 */
	public void setNumberOfTs(long numberOfTs) {
		this.numberOfTs = numberOfTs;
	}

	/**
	 * @return the meanTContentPerWindow
	 */
	public double getMeanTContentPerWindow() {
		return meanTContentPerWindow;
	}

	/**
	 * @param meanTContentPerWindow the meanTContentPerWindow to set
	 */
	public void setMeanTContentPerWindow(double meanTContentPerWindow) {
		this.meanTContentPerWindow = meanTContentPerWindow;
	}

	/**
	 * @return the meanTRelativeContentPerWindow
	 */
	public double getMeanTRelativeContentPerWindow() {
		return meanTRelativeContentPerWindow;
	}

	/**
	 * @param meanTRelativeContentPerWindow the meanTRelativeContentPerWindow to set
	 */
	public void setMeanTRelativeContentPerWindow(
			double meanTRelativeContentPerWindow) {
		this.meanTRelativeContentPerWindow = meanTRelativeContentPerWindow;
	}

	/**
	 * @return the meanTRelativeContent
	 */
	public double getMeanTRelativeContent() {
		return meanTRelativeContent;
	}

	/**
	 * @param meanTRelativeContent the meanTRelativeContent to set
	 */
	public void setMeanTRelativeContent(double meanTRelativeContent) {
		this.meanTRelativeContent = meanTRelativeContent;
	}

	/**
	 * @return the tContentAcrossReference
	 */
	public List<Double> gettContentAcrossReference() {
		return tContentAcrossReference;
	}

	/**
	 * @param tContentAcrossReference the tContentAcrossReference to set
	 */
	public void settContentAcrossReference(List<Double> tContentAcrossReference) {
		this.tContentAcrossReference = tContentAcrossReference;
	}

	/**
	 * @return the tRelativeContentAcrossReference
	 */
	public List<Double> gettRelativeContentAcrossReference() {
		return tRelativeContentAcrossReference;
	}

	/**
	 * @param tRelativeContentAcrossReference the tRelativeContentAcrossReference to set
	 */
	public void settRelativeContentAcrossReference(
			List<Double> tRelativeContentAcrossReference) {
		this.tRelativeContentAcrossReference = tRelativeContentAcrossReference;
	}

	/**
	 * @return the numberOfGs
	 */
	public long getNumberOfGs() {
		return numberOfGs;
	}

	/**
	 * @param numberOfGs the numberOfGs to set
	 */
	public void setNumberOfGs(long numberOfGs) {
		this.numberOfGs = numberOfGs;
	}

	/**
	 * @return the meanGContentPerWindow
	 */
	public double getMeanGContentPerWindow() {
		return meanGContentPerWindow;
	}

	/**
	 * @param meanGContentPerWindow the meanGContentPerWindow to set
	 */
	public void setMeanGContentPerWindow(double meanGContentPerWindow) {
		this.meanGContentPerWindow = meanGContentPerWindow;
	}

	/**
	 * @return the meanGRelativeContentPerWindow
	 */
	public double getMeanGRelativeContentPerWindow() {
		return meanGRelativeContentPerWindow;
	}

	/**
	 * @param meanGRelativeContentPerWindow the meanGRelativeContentPerWindow to set
	 */
	public void setMeanGRelativeContentPerWindow(
			double meanGRelativeContentPerWindow) {
		this.meanGRelativeContentPerWindow = meanGRelativeContentPerWindow;
	}

	/**
	 * @return the meanGRelativeContent
	 */
	public double getMeanGRelativeContent() {
		return meanGRelativeContent;
	}

	/**
	 * @param meanGRelativeContent the meanGRelativeContent to set
	 */
	public void setMeanGRelativeContent(double meanGRelativeContent) {
		this.meanGRelativeContent = meanGRelativeContent;
	}

	/**
	 * @return the gContentAcrossReference
	 */
	public List<Double> getgContentAcrossReference() {
		return gContentAcrossReference;
	}

	/**
	 * @param gContentAcrossReference the gContentAcrossReference to set
	 */
	public void setgContentAcrossReference(List<Double> gContentAcrossReference) {
		this.gContentAcrossReference = gContentAcrossReference;
	}

	/**
	 * @return the gRelativeContentAcrossReference
	 */
	public List<Double> getgRelativeContentAcrossReference() {
		return gRelativeContentAcrossReference;
	}

	/**
	 * @param gRelativeContentAcrossReference the gRelativeContentAcrossReference to set
	 */
	public void setgRelativeContentAcrossReference(
			List<Double> gRelativeContentAcrossReference) {
		this.gRelativeContentAcrossReference = gRelativeContentAcrossReference;
	}

	/**
	 * @return the numberOfNs
	 */
	public long getNumberOfNs() {
		return numberOfNs;
	}

	/**
	 * @param numberOfNs the numberOfNs to set
	 */
	public void setNumberOfNs(long numberOfNs) {
		this.numberOfNs = numberOfNs;
	}

	/**
	 * @return the meanNContentPerWindow
	 */
	public double getMeanNContentPerWindow() {
		return meanNContentPerWindow;
	}

	/**
	 * @param meanNContentPerWindow the meanNContentPerWindow to set
	 */
	public void setMeanNContentPerWindow(double meanNContentPerWindow) {
		this.meanNContentPerWindow = meanNContentPerWindow;
	}

	/**
	 * @return the meanNRelativeContentPerWindow
	 */
	public double getMeanNRelativeContentPerWindow() {
		return meanNRelativeContentPerWindow;
	}

	/**
	 * @param meanNRelativeContentPerWindow the meanNRelativeContentPerWindow to set
	 */
	public void setMeanNRelativeContentPerWindow(
			double meanNRelativeContentPerWindow) {
		this.meanNRelativeContentPerWindow = meanNRelativeContentPerWindow;
	}

	/**
	 * @return the meanNRelativeContent
	 */
	public double getMeanNRelativeContent() {
		return meanNRelativeContent;
	}

	/**
	 * @param meanNRelativeContent the meanNRelativeContent to set
	 */
	public void setMeanNRelativeContent(double meanNRelativeContent) {
		this.meanNRelativeContent = meanNRelativeContent;
	}

	/**
	 * @return the nContentAcrossReference
	 */
	public List<Double> getnContentAcrossReference() {
		return nContentAcrossReference;
	}

	/**
	 * @param nContentAcrossReference the nContentAcrossReference to set
	 */
	public void setnContentAcrossReference(List<Double> nContentAcrossReference) {
		this.nContentAcrossReference = nContentAcrossReference;
	}

	/**
	 * @return the nRelativeContentAcrossReference
	 */
	public List<Double> getnRelativeContentAcrossReference() {
		return nRelativeContentAcrossReference;
	}

	/**
	 * @param nRelativeContentAcrossReference the nRelativeContentAcrossReference to set
	 */
	public void setnRelativeContentAcrossReference(
			List<Double> nRelativeContentAcrossReference) {
		this.nRelativeContentAcrossReference = nRelativeContentAcrossReference;
	}

	/**
	 * @return the meanGcContent
	 */
	public double getMeanGcContent() {
		return meanGcContent;
	}

	/**
	 * @param meanGcContent the meanGcContent to set
	 */
	public void setMeanGcContent(double meanGcContent) {
		this.meanGcContent = meanGcContent;
	}

	/**
	 * @return the meanGcContentPerWindow
	 */
	public double getMeanGcContentPerWindow() {
		return meanGcContentPerWindow;
	}

	/**
	 * @param meanGcContentPerWindow the meanGcContentPerWindow to set
	 */
	public void setMeanGcContentPerWindow(double meanGcContentPerWindow) {
		this.meanGcContentPerWindow = meanGcContentPerWindow;
	}

	/**
	 * @return the meanGcRelativeContentPerWindow
	 */
	public double getMeanGcRelativeContentPerWindow() {
		return meanGcRelativeContentPerWindow;
	}

	/**
	 * @param meanGcRelativeContentPerWindow the meanGcRelativeContentPerWindow to set
	 */
	public void setMeanGcRelativeContentPerWindow(
			double meanGcRelativeContentPerWindow) {
		this.meanGcRelativeContentPerWindow = meanGcRelativeContentPerWindow;
	}

	/**
	 * @return the meanGcRelativeContent
	 */
	public double getMeanGcRelativeContent() {
		return meanGcRelativeContent;
	}

	/**
	 * @param meanGcRelativeContent the meanGcRelativeContent to set
	 */
	public void setMeanGcRelativeContent(double meanGcRelativeContent) {
		this.meanGcRelativeContent = meanGcRelativeContent;
	}

	/**
	 * @return the gcContentAcrossReference
	 */
	public List<Double> getGcContentAcrossReference() {
		return gcContentAcrossReference;
	}

	/**
	 * @param gcContentAcrossReference the gcContentAcrossReference to set
	 */
	public void setGcContentAcrossReference(List<Double> gcContentAcrossReference) {
		this.gcContentAcrossReference = gcContentAcrossReference;
	}

	/**
	 * @return the gcRelativeContentAcrossReference
	 */
	public List<Double> getGcRelativeContentAcrossReference() {
		return gcRelativeContentAcrossReference;
	}

	/**
	 * @param gcRelativeContentAcrossReference the gcRelativeContentAcrossReference to set
	 */
	public void setGcRelativeContentAcrossReference(
			List<Double> gcRelativeContentAcrossReference) {
		this.gcRelativeContentAcrossReference = gcRelativeContentAcrossReference;
	}

//	/**
//	 * @return the meanAtContent
//	 */
//	public double getMeanAtContent() {
//		return meanAtContent;
//	}
//
//	/**
//	 * @param meanAtContent the meanAtContent to set
//	 */
//	public void setMeanAtContent(double meanAtContent) {
//		this.meanAtContent = meanAtContent;
//	}
//
//	/**
//	 * @return the meanAtContentPerWindow
//	 */
//	public double getMeanAtContentPerWindow() {
//		return meanAtContentPerWindow;
//	}
//
//	/**
//	 * @param meanAtContentPerWindow the meanAtContentPerWindow to set
//	 */
//	public void setMeanAtContentPerWindow(double meanAtContentPerWindow) {
//		this.meanAtContentPerWindow = meanAtContentPerWindow;
//	}
//
//	/**
//	 * @return the meanAtRelativeContentPerWindow
//	 */
//	public double getMeanAtRelativeContentPerWindow() {
//		return meanAtRelativeContentPerWindow;
//	}
//
//	/**
//	 * @param meanAtRelativeContentPerWindow the meanAtRelativeContentPerWindow to set
//	 */
//	public void setMeanAtRelativeContentPerWindow(
//			double meanAtRelativeContentPerWindow) {
//		this.meanAtRelativeContentPerWindow = meanAtRelativeContentPerWindow;
//	}
//
//	/**
//	 * @return the meanAtRelativeContent
//	 */
//	public double getMeanAtRelativeContent() {
//		return meanAtRelativeContent;
//	}
//
//	/**
//	 * @param meanAtRelativeContent the meanAtRelativeContent to set
//	 */
//	public void setMeanAtRelativeContent(double meanAtRelativeContent) {
//		this.meanAtRelativeContent = meanAtRelativeContent;
//	}
//
//	/**
//	 * @return the atContentAcrossReference
//	 */
//	public List<Double> getAtContentAcrossReference() {
//		return atContentAcrossReference;
//	}
//
//	/**
//	 * @param atContentAcrossReference the atContentAcrossReference to set
//	 */
//	public void setAtContentAcrossReference(List<Double> atContentAcrossReference) {
//		this.atContentAcrossReference = atContentAcrossReference;
//	}
//
//	/**
//	 * @return the atRelativeContentAcrossReference
//	 */
//	public List<Double> getAtRelativeContentAcrossReference() {
//		return atRelativeContentAcrossReference;
//	}
//
//	/**
//	 * @param atRelativeContentAcrossReference the atRelativeContentAcrossReference to set
//	 */
//	public void setAtRelativeContentAcrossReference(
//			List<Double> atRelativeContentAcrossReference) {
//		this.atRelativeContentAcrossReference = atRelativeContentAcrossReference;
//	}

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
	 * @return the numberOfProcessedWindows
	 */
	public int getNumberOfProcessedWindows() {
		return numberOfProcessedWindows;
	}

	/**
	 * @param numberOfProcessedWindows the numberOfProcessedWindows to set
	 */
	public void setNumberOfProcessedWindows(int numberOfProcessedWindows) {
		this.numberOfProcessedWindows = numberOfProcessedWindows;
	}

	/**
	 * @return the numberOfInitializedWindows
	 */
	public int getNumberOfInitializedWindows() {
		return numberOfInitializedWindows;
	}

	/**
	 * @param numberOfInitializedWindows the numberOfInitializedWindows to set
	 */
	public void setNumberOfInitializedWindows(int numberOfInitializedWindows) {
		this.numberOfInitializedWindows = numberOfInitializedWindows;
	}

	/**
	 * @return the windowPositionsAvailable
	 */
	public boolean isWindowPositionsAvailable() {
		return windowPositionsAvailable;
	}

	/**
	 * @param windowPositionsAvailable the windowPositionsAvailable to set
	 */
	public void setWindowPositionsAvailable(boolean windowPositionsAvailable) {
		this.windowPositionsAvailable = windowPositionsAvailable;
	}

	/**
	 * @return the windowSizes
	 */
	public long[] getWindowSizes() {
		return windowSizes;
	}

	/**
	 * @param windowSizes the windowSizes to set
	 */
	public void setWindowSizes(long[] windowSizes) {
		this.windowSizes = windowSizes;
	}

	/**
	 * @return the windowStarts
	 */
	public long[] getWindowStarts() {
		return windowStarts;
	}

	/**
	 * @param windowStarts the windowStarts to set
	 */
	public void setWindowStarts(long[] windowStarts) {
		this.windowStarts = windowStarts;
	}

	/**
	 * @return the windowEnds
	 */
	public long[] getWindowEnds() {
		return windowEnds;
	}

	/**
	 * @param windowEnds the windowEnds to set
	 */
	public void setWindowEnds(long[] windowEnds) {
		this.windowEnds = windowEnds;
	}

	/**
	 * @return the windowNames
	 */
	public String[] getWindowNames() {
		return windowNames;
	}

	/**
	 * @param windowNames the windowNames to set
	 */
	public void setWindowNames(String[] windowNames) {
		this.windowNames = windowNames;
	}

	/**
	 * @return the activeWindowReporting
	 */
	public boolean isActiveWindowReporting() {
		return activeWindowReporting;
	}

	/**
	 * @param activeWindowReporting the activeWindowReporting to set
	 */
	public void setActiveWindowReporting(boolean activeWindowReporting) {
		this.activeWindowReporting = activeWindowReporting;
	}

	/**
	 * @return the windowReportFile
	 */
	public String getWindowReportFile() {
		return windowReportFile;
	}

	/**
	 * @param windowReportFile the windowReportFile to set
	 */
	public void setWindowReportFile(String windowReportFile) {
		this.windowReportFile = windowReportFile;
	}

	/**
	 * @return the windowReport
	 */
	public PrintWriter getWindowReport() {
		return windowReport;
	}

	/**
	 * @param windowReport the windowReport to set
	 */
	public void setWindowReport(PrintWriter windowReport) {
		this.windowReport = windowReport;
	}

	/**
	 * @return the activeCoverageReporting
	 */
	public boolean isActiveCoverageReporting() {
		return activeCoverageReporting;
	}

	/**
	 * @param activeCoverageReporting the activeCoverageReporting to set
	 */
	public void setActiveCoverageReporting(boolean activeCoverageReporting) {
		this.activeCoverageReporting = activeCoverageReporting;
	}

	/**
	 * @return the coverageReportFile
	 */
	public String getCoverageReportFile() {
		return coverageReportFile;
	}

	/**
	 * @param coverageReportFile the coverageReportFile to set
	 */
	public void setCoverageReportFile(String coverageReportFile) {
		this.coverageReportFile = coverageReportFile;
	}

	/**
	 * @return the coverageReport
	 */
	public PrintWriter getCoverageReport() {
		return coverageReport;
	}

	/**
	 * @param coverageReport the coverageReport to set
	 */
	public void setCoverageReport(PrintWriter coverageReport) {
		this.coverageReport = coverageReport;
	}

	/**
	 * @return the coverageHistogramMap
	 */
	public HashMap<Long, Long> getCoverageHistogramMap() {
		return coverageHistogramMap;
	}

	/**
	 * @param coverageHistogramMap the coverageHistogramMap to set
	 */
	public void setCoverageHistogramMap(HashMap<Long, Long> coverageHistogramMap) {
		this.coverageHistogramMap = coverageHistogramMap;
	}


	/**
	 * @return the meanInsertSize
	 */
	public double getMeanInsertSize() {
		return meanInsertSize;
	}

	/**
	 * @param meanInsertSize the meanInsertSize to set
	 */
	public void setMeanInsertSize(double meanInsertSize) {
		this.meanInsertSize = meanInsertSize;
	}

	/**
	 * @return the meanInsertSizePerWindow
	 */
	public double getMeanInsertSizePerWindow() {
		return meanInsertSizePerWindow;
	}

	/**
	 * @param meanInsertSizePerWindow the meanInsertSizePerWindow to set
	 */
	public void setMeanInsertSizePerWindow(double meanInsertSizePerWindow) {
		this.meanInsertSizePerWindow = meanInsertSizePerWindow;
	}

	/**
	 * @return the insertSizeAcrossReference
	 */
	public List<Double> getInsertSizeAcrossReference() {
		return insertSizeAcrossReference;
	}

	/**
	 * @param insertSizeAcrossReference the insertSizeAcrossReference to set
	 */
	public void setInsertSizeAcrossReference(List<Double> insertSizeAcrossReference) {
		this.insertSizeAcrossReference = insertSizeAcrossReference;
	}

	/**
	 * @return the insertSizeHistogram
	 */
	public XYVector getInsertSizeHistogram() {
		return insertSizeHistogram;
	}

	/**
	 * @param insertSizeHistogram the insertSizeHistogram to set
	 */
	public void setInsertSizeHistogram(XYVector insertSizeHistogram) {
		this.insertSizeHistogram = insertSizeHistogram;
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

    public int getNumberOfMappedReadsInRegions() {
        return numberOfMappedReadsInRegions;
    }

    public void setNumberOfMappedReadsInRegions(int numberOfMappedReadsInRegions) {
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

    public boolean availableGenomeGcContentHistogram() {
        return avaialableGenomeGcContentData;
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

        int[] homopolymerIndels = readStatsCollector.getHomopolymerIndels();
        int numHomopolymerIndels = 0;
        for (int i = 0; i < 5; ++i) {
            homopolymerIndelsData[i] += homopolymerIndels[i];
            numHomopolymerIndels += homopolymerIndels[i];
        }

        numInsertions += readStatsCollector.getNumInsertions();
        numDeletions += readStatsCollector.getNumDeletions();

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

    public int[] getHomopolymerIndelsData() {
        return homopolymerIndelsData;
    }

    public int getNumberOfPairedReads() {
        return numberOfPairedReads;
    }

    public void setNumberOfPairedReads(int numberOfPairedReads) {
        this.numberOfPairedReads = numberOfPairedReads;
    }

    public double getPercentageOfPairedReads() {
        return (numberOfPairedReads / (double) numberOfReads) * 100.0;
    }


    public int getNumberOfSingletons() {
        return numberOfSingletons;
    }

    public void setNumberOfSingletons(int numberOfSingletons) {
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


    public void saveChromosomeStats(String fileName, GenomeLocator locator, ArrayList<Integer> chromosomeWindowIndexes) throws IOException {
        int chromosomeCount = chromosomeWindowIndexes.size();
        List<ContigRecord> contigRecords = locator.getContigs();

        PrintWriter chromoWriter = new PrintWriter(new FileWriter(fileName));

        chromoWriter.println("#name\tabsolute_pos\tmapped_bases\tmean_coverage\tstd_coverage");
        for (int k = 0; k < chromosomeCount; ++k) {
            int firstWindowIndex = chromosomeWindowIndexes.get(k);
            int lastWindowIndex = k + 1 < chromosomeCount
                    ? chromosomeWindowIndexes.get(k + 1) - 1 : numberOfWindows - 1;

            long numBases = 0;
            double length = 0;
            long sumCovSquared = 0;
            for (int i = firstWindowIndex; i <= lastWindowIndex; ++i) {
                numBases += numMappedBasesPerWindow.get(i);
                sumCovSquared += coverageSquaredPerWindow.get(i);
                length += windowLengthes.get(i);
            }

            ContigRecord contig = contigRecords.get(k);
            chromoWriter.print(contig.getName() + "\t");
            chromoWriter.print(contig.getStart() + ":"+ contig.getEnd() + "\t");

            if (length == 0) {
                chromoWriter.println("0\t0\t0\t");
            } else {
                chromoWriter.print(numBases + "\t");
                double mean =  numBases / length;
                double std = Math.sqrt( sumCovSquared / length - mean*mean);
                chromoWriter.print(StringUtils.decimalFormat(mean,"#,###,###,###.##")+ "\t" );
                chromoWriter.println(StringUtils.decimalFormat(std, "#,###,###,###.##"));
            }
        }

        chromoWriter.close();
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

    public double getMedianInsertSize() {
        return medianInsertSize;
    }

    public int getNumberOfMappedFirstOfPair() {
        return numberOfMappedFirstOfPair;
    }

    public void setNumberOfMappedFirstOfPair(int numberOfMappedFirstOfPair) {
        this.numberOfMappedFirstOfPair = numberOfMappedFirstOfPair;
    }

    public int getNumberOfMappedSecondOfPair() {
        return numberOfMappedSecondOfPair;
    }

    public void setNumberOfMappedSecondOfPair(int numberOfMappedSecondOfPair) {
        this.numberOfMappedSecondOfPair = numberOfMappedSecondOfPair;
    }

    public int getNumberOfSingletonsInRegions() {
        return numberOfSingletonsInRegions;
    }

    public void setNumberOfSingletonsInRegions(int numberOfSingletonsInRegions) {
        this.numberOfSingletonsInRegions = numberOfSingletonsInRegions;
    }

    public int getNumberOfMappedFirstOfPairInRegions() {
        return numberOfMappedFirstOfPairInRegions;
    }

    public void setNumberOfMappedFirstOfPairInRegions(int numberOfMappedFirstOfPairInRegions) {
        this.numberOfMappedFirstOfPairInRegions = numberOfMappedFirstOfPairInRegions;
    }

    public int getNumberOfMappedSecondOfPairInRegions() {
        return numberOfMappedSecondOfPairInRegions;
    }

    public void setNumberOfMappedSecondOfPairInRegions(int numberOfMappedSecondOfPairInRegions) {
        this.numberOfMappedSecondOfPairInRegions = numberOfMappedSecondOfPairInRegions;
    }


    public void updateInsertSizeHistogram(int insertSize) {
        if (insertSize > 0 ) {
            insertSizeArray.add(insertSize);
        }
    }


    public void setNumberOfPairedReadsInRegions(int numPairedReads) {
        numberOfPairedReadsInRegions = numPairedReads;
    }

    public int getNumberOfPairedReadsInRegions() {
        return numberOfPairedReadsInRegions;
    }

    public double getDuplicationRate() {
        return duplicationRate;
    }

    public void updateReadStartsHistogram(long position) {
        readStartsHistogram.update(position);
    }

    public int getNumClippedReads() {
        return numClippedReads;
    }

    public void setNumberOfCorrectStrandReads(int numberOfCorrectStrandReads) {
        this.numCorrectStrandReads = numberOfCorrectStrandReads;
    }

    public int getNumCorrectStrandReads() {
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