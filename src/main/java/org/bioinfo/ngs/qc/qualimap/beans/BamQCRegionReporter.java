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

import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
import org.bioinfo.commons.utils.ListUtils;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats.ChromosomeInfo;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StatsKeeper;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StringUtilsSwing;
import org.bioinfo.ngs.qc.qualimap.process.BamStatsAnalysis;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.ui.RectangleInsets;

public class BamQCRegionReporter extends StatsReporter implements Serializable {



    public double getPercentSingletons() {
        return percentageSingletons;
    }

    public double getPercentageBothMatesPaired() {
        return ((numPairedReads - numSingletons) * 100.0) / (double) numReads ;
    }

    public double getPercentageClippedReads() {
        return (numClippedReads * 100.0) / (double) numReads;
    }

    public double getPercentageOverlappingPairs() {
        // pair has 2 reads
        return (numOverlappingReadPairs * 200.0 / (double) numReads);
    }

    public double getPercentageBothMatesPairedInRegions() {
       return ((numPairedReadsInRegions - numSingletonsInRegions) * 100.0) / (double) numReads ;
    }

    public double getPercentageDublicateReadsMarked() {
          return (numDuplicatedReadsMarked * 100.0) / (double) numReads ;
    }

    public double getPercentageDublicateReadsEstimated() {
              return (numDuplicatedReadsEstimated * 100.0) / (double) numReads ;
    }

    public double getPercentageSkippedDublicatedReads() {
              return (numDuplicatesSkipped * 100.0) / (double) numReads ;
    }

    public void setGenomeGCContentName(String genomeName) {
        this.genomeGCContentName = genomeName;
    }

    private boolean paintChromosomeLimits;

	private Long referenceSize, aNumber, cNumber, gNumber,
	tNumber, nNumber, numReads, numMappedReads,
    /*numMappedBases, numSequencedBases, numAlignedBases,*/
    numBasesInsideRegions;

	private Double aPercent, cPercent, gPercent, tPercent, nPercent,
	gcPercent, percentMappedReads, meanMappingQuality, meanInsertSize,
    stdInsertSize, meanCoverage, stdCoverage, adaptedMeanCoverage;

    private int p25InsertSize, medianInsertSize, p75InsertSize;

    int readMinSize, readMaxSize;
    int numClippedReads;
    double readsWithInsertionPercentage, readsWithDeletionPercentage;
    double readMeanSize;

    private long numPairedReads, numberOfMappedFirstOfPair, numberOfMappedSecondOfPair;
    private double percantagePairedReads, percentageOfMappedFirstOfPair, percentageOfMappedSecondOfPair;
    private long numSingletons;
    private double percentageSingletons;
    private long numCorrectStrandReads;
    private long numDuplicatedReadsMarked, numDuplicatedReadsEstimated, numDuplicatesSkipped;

    boolean includeIntersectingPairs;
    private long numOverlappingReadPairs;

    private long numMappedReadsInRegions, numPairedReadsInRegions;
    private double percentageMappedReadsInRegions;

    private long numMappedFirstOfPairInRegions, numMappedSecondOfPairInRegions;
    private double percentageOfMappedFirstOfPairInRegions, percentageOfMappedSecondOfPairInRegions;
    private long numSingletonsInRegions;
    private double percentageSingletonsInRegions, percentageCorrectStrandReads;

    private double duplicationRate;

    private int numInsertions, numDeletions;
    private long numMismatches;
    private double homopolymerIndelFraction;
    private double alignmentErrorRate;

    private Map<String,String> warnings;
    String genomeGCContentName;
    int numSelectedRegions;

    static NumberFormat decimailFormatter = DecimalFormat.getInstance(Locale.US);

    public BamQCRegionReporter(boolean gffIsAvailable, boolean inside) {
        if (gffIsAvailable) {
            if (inside) {
                namePostfix = " (inside of regions)";
            } else {
                namePostfix = " (outside of regions)";
                fileName = "qualimapReportOutsideRegions";
            }
        } else {
            namePostfix = "";
        }
        genomeGCContentName = "";

        decimailFormatter.setMaximumFractionDigits(2);

    }


	public void writeReport(BamStats bamStats, String outdir) throws IOException{
		// init report		
		String reportFile = outdir + "/" + bamStats.getName() + "_results.txt";
		PrintWriter report = new PrintWriter(new File(reportFile));

		// header
		report.println("BamQC report");
		report.println("-----------------------------------");
		report.println("");

		// input
		report.println(">>>>>>> Input");
		report.println("");
		report.println("     bam file = " + bamStats.getSourceFile());
		report.println("     outfile = " + reportFile);
		report.println("");
		report.println("");

		// globals
		report.println(">>>>>>> Reference");
		report.println("");
		report.println("     number of bases = " + formatLong(bamStats.getReferenceSize()) + " bp");
		report.println("     number of contigs = " + bamStats.getNumberOfReferenceContigs());
		report.println("");
		report.println("");

		// globals
		report.println(">>>>>>> Globals");
		report.println("");
		report.println("     number of windows = " + bamStats.getNumberOfWindows());
		report.println("");
		report.println("     number of reads = " + formatLong(bamStats.getNumberOfReads()));		
		report.println("     number of mapped reads = " + formatLong(bamStats.getNumberOfMappedReads()) +
                " (" + formatPercentage(bamStats.getPercentageOfMappedReads())+ ")");
        report.println("");

		if (bamStats.getNumberOfPairedReads() > 0) {
            report.println("     number of mapped paired reads (first in pair) = "
                    + formatLong(bamStats.getNumberOfMappedFirstOfPair()) );
            report.println("     number of mapped paired reads (second in pair) = "
                    + formatLong(bamStats.getNumberOfMappedSecondOfPair()) );
            report.println("     number of mapped paired reads (both in pair) = "
                    + formatLong(bamStats.getNumberOfPairedReads() - bamStats.getNumberOfSingletons()) );
            report.println("     number of mapped paired reads (singletons) = "
                    + formatLong(bamStats.getNumberOfSingletons()) );
            if (bamStats.getNumOverlappingReadPairs() > 0) {
                report.println("     number of overlapping read pairs = "
                     + formatLong(bamStats.getNumOverlappingReadPairs()) );
            }
            report.println("");
        }

        report.println("     number of mapped bases = " + formatLong(bamStats.getNumberOfMappedBases()) + " bp");
		report.println("     number of sequenced bases = " + formatLong(bamStats.getNumberOfSequencedBases()) + " bp");
		report.println("     number of aligned bases = " + formatLong(bamStats.getNumberOfAlignedBases()) + " bp");
        if (bamStats.getNumDetectedDuplicatedReads() > 0) {
            report.println("     number of duplicated reads (flagged) = " + formatLong(bamStats.getNumDetectedDuplicatedReads()) );
        } else {
            report.println("     number of duplicated reads (estimated) = " + formatLong(bamStats.getNumDuplicatedReadsEstimated()) );
            report.println("     duplication rate = " + formatPercentage(bamStats.getDuplicationRate()) );
        }

        if (bamStats.getNumberOfMappedReads() == 0) {
            report.close();
            return;
        }


        report.println("");
		report.println("");

        // insert size
        report.println(">>>>>>> Insert size");
        report.println("");
        report.println("     mean insert size = " + formatDecimal(bamStats.getMeanInsertSize()) );
        report.println("     std insert size = " + formatDecimal(bamStats.getStdInsertSize()) );
        report.println("     median insert size = " + bamStats.getMedianInsertSize());
        report.println("");
        report.println("");

		// mapping quality		
		report.println(">>>>>>> Mapping quality");
		report.println("");
		report.println("     mean mapping quality = " + formatDecimal(bamStats.getMeanMappingQualityPerWindow()));
		report.println("");
		report.println("");

		// actg content
		report.println(">>>>>>> ACTG content");
		report.println("");
		report.println("     number of A's = " + formatLong(bamStats.getNumberOfAs()) +  " bp (" +
                formatPercentage(bamStats.getMeanARelativeContent()) + ")");
		report.println("     number of C's = " + formatLong(bamStats.getNumberOfCs()) +  " bp (" +
                formatPercentage(bamStats.getMeanCRelativeContent()) + ")");
		report.println("     number of T's = " + formatLong(bamStats.getNumberOfTs()) +  " bp (" +
                formatPercentage(bamStats.getMeanTRelativeContent()) + ")");
		report.println("     number of G's = " + formatLong(bamStats.getNumberOfGs()) +  " bp (" +
                formatPercentage(bamStats.getMeanGRelativeContent()) + ")");
		report.println("     number of N's = " + formatLong(bamStats.getNumberOfNs()) +  " bp (" +
                formatPercentage(bamStats.getMeanNRelativeContent()) + ")");
		report.println("");
		report.println("     GC percentage = " + formatPercentage(bamStats.getMeanGcRelativeContent()));
		//		report.println("     AT percentage = " + formatPercentage(bamStats.getMeanAtRelativeContent()));
		report.println("");
		report.println("");


		// Mismatches and indels

        report.println(">>>>>>> Mismatches and indels");
        report.println("");
        report.println("    general error rate = " + formatDecimal(bamStats.getErrorRate()) ) ;
        report.println("    number of mismatches = " + formatLong(bamStats.getNumMismatches()) ) ;

        long numIndels= bamStats.getNumIndels();
        if (numIndels > 0) {
            report.println("    number of insertions = " + formatLong(bamStats.getNumInsertions()) );
            report.println("    mapped reads with insertion percentage = " +
                    formatPercentage(bamStats.getReadsWithInsertionPercentage()));
            report.println("    number of deletions = " + formatLong(bamStats.getNumDeletions()) );
            report.println("    mapped reads with deletion percentage = " +
                    formatPercentage(bamStats.getReadsWithDeletionPercentage()));
            report.println("    homopolymer indels = " + formatPercentage(bamStats.getHomopolymerIndelFraction() * 100.0));
        }

        report.println("");
		report.println("");


		// coverageData
		report.println(">>>>>>> Coverage");
		report.println("");
		report.println("     mean coverageData = " + formatDecimal(bamStats.getMeanCoverage()) + "X");
		report.println("     std coverageData = " + formatDecimal(bamStats.getStdCoverage()) + "X");
        if (bamStats.getNumOverlappingReadPairs() > 0) {
            report.println("     paired-end adapted mean coverage = "
                      + formatDecimal(bamStats.getAdaptedMeanCoverage()) + "X" );
        }

		report.println("");
		for(int i=0; i<bamStats.getCoverageQuotes().getSize(); i++){
			report.println("     There is a " +
                    StringUtils.decimalFormat(bamStats.getCoverageQuotes().get(i).getY(),"#0.##") +
                    "% of reference with a coverageData >= " + (int)bamStats.getCoverageQuotes().get(i).getX()
                    + "X");
		}
		report.println("");
		report.println("");


		// coverage per chromosome data
		report.println(">>>>>>> Coverage per contig");
		report.println("");
		ChromosomeInfo[] chromosomeStats = bamStats.getChromosomeStats();
        for (ChromosomeInfo chromosomeInfo : chromosomeStats) {
            report.println("\t" + chromosomeInfo.getName() + "\t" +
                    chromosomeInfo.getLength() + "\t" + chromosomeInfo.getNumBases() + "\t" +
                    chromosomeInfo.getCovMean() + "\t" + chromosomeInfo.getCovStd());
        }
		report.println("");
		report.println("");

		report.close();
	}

    private XYToolTipGenerator createTooltipGenerator(List<Double> windowReferences, GenomeLocator locator ) {

        if (locator == null) {
            return null;
        }

        List<String> toolTips = new ArrayList<String>();

        for (double pos : windowReferences) {
            ContigRecord rec = locator.getContigCoordinates((long) pos);
            long start = rec.getStart();
            long relativePos = (long)pos - start + 1;
            toolTips.add(rec.getName() + ", relative position: " + relativePos);
        }

        CustomXYToolTipGenerator generator = new CustomXYToolTipGenerator();
        generator.addToolTipSeries(toolTips);

        return generator;

    }

    /**
	 * Function to load the data variables obtained from the input file/s
	 * @param bamStats data read in the input file
	 */
	public void loadReportData(BamStats bamStats) {

        this.referenceSize = bamStats.getReferenceSize();

		/*if(bamStats.isReferenceAvailable()) {
			this.referenceFileName = bamStats.getReferenceFile();
			this.aReferenceNumber = bamStats.getNumberOfAsInReference();
			this.aReferencePercent = bamStats.getMeanARelativeContentPerWindowInReference();
			this.cReferenceNumber = bamStats.getNumberOfCsInReference();
			this.cReferencePercent = bamStats.getMeanCRelativeContentPerWindowInReference();
			this.tReferenceNumber = bamStats.getNumberOfTsInReference();
			this.tReferencePercent = bamStats.getMeanTRelativeContentPerWindowInReference();
			this.gReferenceNumber = bamStats.getNumberOfGsInReference();
			this.gReferencePercent = bamStats.getMeanGRelativeContentPerWindowInReference();
			this.nReferenceNumber = bamStats.getNumberOfNsInReference();
			this.nReferencePercent = bamStats.getMeanNRelativeContentPerWindowInReference();
    		this.gcPercent = bamStats.getMeanGcRelativeContentPerWindowInReference();
		}*/

		// globals
		this.numReads = bamStats.getNumberOfReads();
		this.numMappedReads = bamStats.getNumberOfMappedReads();
		this.percentMappedReads = bamStats.getPercentageOfMappedReads();
		/*this.numMappedBases = bamStats.getNumberOfMappedBases();
		this.numSequencedBases = bamStats.getNumberOfSequencedBases();
		this.numAlignedBases = bamStats.getNumberOfAlignedBases();*/
        this.duplicationRate = bamStats.getDuplicationRate();
        this.numDuplicatedReadsMarked = bamStats.getNumDetectedDuplicatedReads();
        this.numDuplicatedReadsEstimated = bamStats.getNumDuplicatedReadsEstimated();
        this.numDuplicatesSkipped = bamStats.getNumDuplicatesSkipped();


        // paired reads
        this.numPairedReads = bamStats.getNumberOfPairedReads();
        this.percantagePairedReads = bamStats.getPercentageOfPairedReads();
        this.numSingletons = bamStats.getNumberOfSingletons();
        this.percentageSingletons = bamStats.getPercentageOfSingletons();
        this.numberOfMappedFirstOfPair = bamStats.getNumberOfMappedFirstOfPair();
        this.percentageOfMappedFirstOfPair = ( (double) numberOfMappedFirstOfPair / numReads) * 100.0;
        this.numberOfMappedSecondOfPair = bamStats.getNumberOfMappedSecondOfPair();
        this.percentageOfMappedSecondOfPair = ( (double) numberOfMappedSecondOfPair / numReads ) * 100.0;

        this.includeIntersectingPairs = bamStats.reportOverlappingReadPairs();
        this.numOverlappingReadPairs = bamStats.getNumOverlappingReadPairs();

        // read properties
        this.numClippedReads = bamStats.getNumClippedReads();
        this.readsWithInsertionPercentage = bamStats.getReadsWithInsertionPercentage();
        this.readsWithDeletionPercentage = bamStats.getReadsWithDeletionPercentage();


        // regions related
        this.numSelectedRegions = bamStats.getNumSelectedRegions();
        this.numBasesInsideRegions = bamStats.getInRegionReferenceSize();
        this.numMappedReadsInRegions = bamStats.getNumberOfMappedReadsInRegions();
        this.numPairedReadsInRegions = bamStats.getNumberOfPairedReadsInRegions();
        this.percentageMappedReadsInRegions = bamStats.getPercentageOfInsideMappedReads();
        this.numMappedFirstOfPairInRegions = bamStats.getNumberOfMappedFirstOfPairInRegions();
        this.percentageOfMappedFirstOfPairInRegions =
                ( (double) numMappedFirstOfPairInRegions / numReads) * 100.0;
        this.numMappedSecondOfPairInRegions = bamStats.getNumberOfMappedSecondOfPairInRegions();
        this.percentageOfMappedSecondOfPairInRegions =
                ( (double) numMappedSecondOfPairInRegions / numReads) * 100.0;
        this.numSingletonsInRegions = bamStats.getNumberOfSingletonsInRegions();
        this.percentageSingletonsInRegions = ( (double) numSingletonsInRegions / numReads ) * 100.0;
        this.numCorrectStrandReads = bamStats.getNumCorrectStrandReads();
        this.percentageCorrectStrandReads = ((double) numCorrectStrandReads / numReads ) * 100.0;

		// mapping quality		
		this.meanMappingQuality = bamStats.getMeanMappingQualityPerWindow();

        // insert size
        this.meanInsertSize = bamStats.getMeanInsertSize();
        this.p25InsertSize = bamStats.getP25InsertSize();
        this.medianInsertSize = bamStats.getMedianInsertSize();
        this.p75InsertSize = bamStats.getP75InsertSize();
        this.stdInsertSize = bamStats.getStdInsertSize();

		// actg content
		this.aNumber = bamStats.getNumberOfAs();
		this.aPercent = bamStats.getMeanARelativeContent();
		this.cNumber = bamStats.getNumberOfCs();
		this.cPercent = bamStats.getMeanCRelativeContent();
		this.tNumber = bamStats.getNumberOfTs();
		this.tPercent = bamStats.getMeanTRelativeContent();
		this.gNumber = bamStats.getNumberOfGs();
		this.gPercent = bamStats.getMeanGRelativeContent();
		this.nNumber = bamStats.getNumberOfNs();
		this.nPercent = bamStats.getMeanNRelativeContent();
		this.gcPercent = bamStats.getMeanGcRelativeContent();

		// coverageData
		this.meanCoverage = bamStats.getMeanCoverage();
		this.stdCoverage = bamStats.getStdCoverage();
        this.adaptedMeanCoverage = bamStats.getAdaptedMeanCoverage();

        // read sizes
        readMaxSize = bamStats.getReadMaxSize();
        readMinSize = bamStats.getReadMinSize();
        readMeanSize = bamStats.getReadMeanSize();

        // indels
        numInsertions = bamStats.getNumInsertions();
        numDeletions = bamStats.getNumDeletions();
        homopolymerIndelFraction = bamStats.getHomopolymerIndelFraction();
        numMismatches = bamStats.getNumMismatches();
        alignmentErrorRate = bamStats.getErrorRate();

        warnings = bamStats.getWarnings();

        prepareSummaryStatsKeeper();

        prepareChromosomeStatsKeeper(bamStats.getChromosomeStats());



	}

	/**
	 * Create all the charts from the input file/s and save each of them into a bufferedImage
	 * @param bamStats BamStats class that contains the information of the graphics titles
	 * @param locator Genomic locator
     * @param isPairedData Paired data identifier
	 * @throws IOException Error during computation
	 */
	public void computeChartsBuffers(BamStats bamStats, GenomeLocator locator, boolean isPairedData) throws IOException{

        // define a stroke
		Stroke stroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {4.0f, 8.0f}, 0.0f);

		if (charts == null) {
            charts = new ArrayList<QChart>();
        }

        if (bamStats.getNumberOfMappedBases() == 0) {
             return;
        }


		// some variables		
		double maxValue = 50;
		String subTitle = new File(bamStats.getSourceFile()).getName() + namePostfix;

		// compute window centers
		List<Double> windowReferences = new ArrayList<Double>(bamStats.getNumberOfWindows());

        for(int i=0; i<bamStats.getNumberOfWindows(); i++){
			windowReferences.add((double)(bamStats.getWindowStart(i)+bamStats.getWindowEnd(i))/2.0);
        }
		double lastReference = windowReferences.get(windowReferences.size()-1);

		// max coverageData+std
		double maxCoverage = 0;
		for(int i=0; i<bamStats.getCoverageAcrossReference().size(); i++){
			if (bamStats.getCoverageAcrossReference().get(i)+bamStats.getStdCoverageAcrossReference().get(i)>maxCoverage) {
                maxCoverage = bamStats.getCoverageAcrossReference().get(i)+bamStats.getStdCoverageAcrossReference().get(i);
            }
		}

        double maxInsertSize = 0;
        if (isPairedData && maxCoverage > 0) {
            for (int i = 0; i < bamStats.getInsertSizeAcrossReference().size(); i++) {
                double iSize = bamStats.getInsertSizeAcrossReference().get(i);
                if (iSize > maxInsertSize) {
                    maxInsertSize = iSize;
                }
            }
        }

        // compute chromosome limits
		Color chromosomeColor = new Color(40,40,40,150);
		XYVector chromosomeCoverageLimits = null;
		XYVector chromosomePercentageLimits = null;
		XYVector chromosomeBytedLimits = null;
        XYVector chromosomeInsertSizeLimits = null;
        List<XYBoxAnnotation>  chromosomeAnnotations = null;
        Map<Double,String> chromosomeNames = null;

        if(paintChromosomeLimits && locator!=null){
			int numberOfChromosomes = locator.getContigs().size();
			chromosomeCoverageLimits = new XYVector();
			chromosomePercentageLimits = new XYVector();
			chromosomeBytedLimits = new XYVector();
            chromosomeInsertSizeLimits = new XYVector();
            chromosomeAnnotations = new ArrayList<XYBoxAnnotation>();
            chromosomeNames = new HashMap<Double, String>();
            for(int i=0; i<numberOfChromosomes; i++){
                long chrStart = locator.getContigs().get(i).getPosition();
                long chrSize = locator.getContigs().get(i).getSize();
                long chrEnd = locator.getContigs().get(i).getEnd();
                String chrName = locator.getContigs().get(i).getName();

                XYBoxAnnotation xyBoxAnnotation = new XYBoxAnnotation( (double) chrStart, 0.0,
                    (double)( chrStart + chrSize), maxCoverage, null, null);
                xyBoxAnnotation.setToolTipText(chrName);
                chromosomeAnnotations.add(xyBoxAnnotation);

                // chromosome names on boundaries
                chromosomeNames.put( (double) chrStart + ((double)chrSize)*0.8, chrName);

                //QUESTION: why are there are three of them???

            	// coverageData
				chromosomeCoverageLimits.addItem(new XYItem(chrEnd,0));
				chromosomeCoverageLimits.addItem(new XYItem(chrEnd,maxCoverage));
				chromosomeCoverageLimits.addItem(new XYItem(chrEnd,0));
				// percentage
				chromosomePercentageLimits.addItem(new XYItem(chrEnd,0));
				chromosomePercentageLimits.addItem(new XYItem(chrEnd,100));
				chromosomePercentageLimits.addItem(new XYItem(chrEnd,0));
				// byte
				chromosomeBytedLimits.addItem(new XYItem(chrEnd,0));
				chromosomeBytedLimits.addItem(new XYItem(chrEnd,255));
				chromosomeBytedLimits.addItem(new XYItem(chrEnd,0));
                // insert size
                chromosomeInsertSizeLimits.addItem(new XYItem(chrEnd,0));
                chromosomeInsertSizeLimits.addItem(new XYItem(chrEnd,maxInsertSize));
                chromosomeInsertSizeLimits.addItem(new XYItem(chrEnd,0));


			}
        }

		///////////////// Coverage and GC-content across reference ////////////////////////////////

		// coverageData
		BamQCChart coverageChart = new BamQCChart(Constants.PLOT_TITLE_COVERAGE_ACROSS_REFERENCE,
                subTitle, "Position (bp)", "Coverage (X)");
        XYToolTipGenerator toolTipGenerator = createTooltipGenerator(windowReferences, locator);
        coverageChart.setToolTipGenerator(toolTipGenerator);
        coverageChart.setSeriesToExportIndex(0);
        coverageChart.addIntervalRenderedSeries("Coverage",new XYVector(windowReferences,
                bamStats.getCoverageAcrossReference(), bamStats.getStdCoverageAcrossReference()),
                new Color(250,50,50,150), new Color(50,50,250), 0.2f);

        //Preparing "smart" zoom for coverage across region
        Double[] coverageData = ListUtils.toArray(bamStats.getCoverageAcrossReference());
        double upperCoverageBound = 2*StatUtils.percentile(ArrayUtils.toPrimitive(coverageData), 90);
        if (upperCoverageBound == 0) {
            // possible in rare cases when the coverage is very low coverage
            upperCoverageBound = maxCoverage*0.9;
        }


        if( paintChromosomeLimits && locator!=null ) {
            coverageChart.addSeries("chromosomes", chromosomeCoverageLimits, chromosomeColor, stroke,
                    false, chromosomeAnnotations);
            coverageChart.writeChromsomeNames(0.9*upperCoverageBound, chromosomeNames);
        }
        coverageChart.render();

        coverageChart.getChart().getXYPlot().getRangeAxis().setRange(0, upperCoverageBound);

        // gc content
		BamQCChart gcContentChart = new BamQCChart("GC/AT relative content", subTitle,
                "absolute position (bp)", "GC (%)");
		gcContentChart.setToolTipGenerator(toolTipGenerator);
        gcContentChart.setPercentageChart(true);
		gcContentChart.addSeries("GC content(%)", new XYVector(windowReferences,bamStats.getGcRelativeContentAcrossReference()), new Color(50,50,50,150));
		gcContentChart.addSeries("mean GC content", new XYVector(Arrays.asList(0.0,lastReference), Arrays.asList(bamStats.getMeanGcRelativeContentPerWindow(),bamStats.getMeanGcRelativeContentPerWindow())),new Color(255,0,0,180),stroke,true);
		if(paintChromosomeLimits && locator!=null) {
            gcContentChart.addSeries("chromosomes",chromosomePercentageLimits,chromosomeColor,stroke,
                    false, chromosomeAnnotations);
        }
	    gcContentChart.render();


        // combined plot
		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis("Position (bp)"));
        plot.getDomainAxis().setTickLabelsVisible(false);
        plot.getDomainAxis().setTickMarksVisible(false);
        plot.add(coverageChart.getChart().getXYPlot(),5);
		plot.setGap(0);
		plot.add(gcContentChart.getChart().getXYPlot(),1);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart combinedChart = new JFreeChart("Coverage across reference",plot);
		combinedChart.setPadding(new RectangleInsets(30,20,30,20));
		combinedChart.addSubtitle(coverageChart.getChart().getSubtitle(0));
        charts.add(new QChart(bamStats.getName() + "_coverage_across_reference.png", combinedChart, coverageChart) );



		////////////////////////////// Balanced coverage histogram ////////////////////////////////

        BamQCHistogramChart coverageHistogram = new BamQCHistogramChart(Constants.PLOT_TITLE_COVERAGE_HISTOGRAM,
                subTitle, "Coverage (X)", "Number of genomic locations",
                bamStats.getBalancedCoverageHistogram(), bamStats.getBalancedCoverageBarNames());

		coverageHistogram.render();

        XYVectorDataWriter coverageHistDataWriter = new XYVectorDataWriter(bamStats.getCoverageHistogram(),
                "Coverage", "Number of genomic locations");
        charts.add( new QChart(bamStats.getName() + "_coverage_histogram.png",
				coverageHistogram.getChart(), coverageHistDataWriter) );


		/////////////////////////////// Coverage Histogram 0-50x //////////////////////////////////

        double minCoverage = bamStats.getCoverageHistogram().get(0).getX();
        if (minCoverage < 40) {
            // This histogram only makes sense for low coverage samples
            BamQCXYHistogramChart coverageRangedHistogram =
                    new BamQCXYHistogramChart(Constants.PLOT_TITLE_COVERAGE_HISTOGRAM_0_50,
                            subTitle, "Coverage (X)", "Number of genomic locations");
            coverageRangedHistogram.addHistogram("coverageData", bamStats.getCoverageHistogram(), Color.blue);
            coverageRangedHistogram.setNumberOfBins(50);
            coverageRangedHistogram.zoom(maxValue);
            coverageRangedHistogram.setDomainAxisIntegerTicks(true);
            coverageRangedHistogram.setDomainAxisTickUnitSize(1.0);
            coverageRangedHistogram.render();

            charts.add( new QChart(bamStats.getName() + "_coverage_0to" + (int)maxValue + "_histogram.png",
                    coverageRangedHistogram.getChart() ) );
        }


		//////////////////////////////// Genome Fraction Coverage /////////////////////////////////
        BamQCChart coverageQuota = new BamQCChart(Constants.PLOT_TITLE_GENOME_FRACTION_COVERAGE, subTitle,
                "Coverage (X)", "Fraction of reference (%)");
		coverageQuota.setPercentageChart(true);
		coverageQuota.addBarRenderedSeries("Coverage", bamStats.getCoverageQuotes(), new Color(255,20,20,150));
		coverageQuota.setDomainAxisIntegerTicks(true);
        coverageQuota.setDomainAxisTickUnitSize(1.0);
        coverageQuota.render();
		charts.add( new QChart(bamStats.getName() + "_coverage_quotes.png", coverageQuota.getChart(),
                coverageQuota) );

        /////////////////////////////// Duplication Rate Histogram ///////////////////////////////
        BamQCXYHistogramChart uniqueReadStartsHistogram =
                new BamQCXYHistogramChart(Constants.PLOT_TITLE_DUPLICATION_RATE_HISTOGRAM,
                subTitle, "Duplication rate", "Number of loci");
        uniqueReadStartsHistogram.addHistogram("Coverage", bamStats.getUniqueReadStartsHistogram(), Color.GREEN);
        uniqueReadStartsHistogram.setDomainAxisIntegerTicks(true);
        uniqueReadStartsHistogram.setDomainAxisTickUnitSize(1.0);
        uniqueReadStartsHistogram.render();

        charts.add(new QChart( bamStats.getName() + "_uniq_read_starts_histogram.png",
                uniqueReadStartsHistogram.getChart(), uniqueReadStartsHistogram )  );



        ////////////////////////////// Reads nucleotide content ////////////////////////////////////
        if (bamStats.getReadMaxSize() > 0) {
            BamQCChart readsContentChart = new BamQCChart(Constants.PLOT_TITLE_READS_NUCLEOTIDE_CONTENT,
                    subTitle, " Position (bp)", " Nucleotide Content (%) ");
            readsContentChart.addSeries("A", bamStats.getReadsAsHistogram(), new Color(255, 0,0,255));
            readsContentChart.addSeries("C", bamStats.getReadsCsHistogram(), new Color(0, 0,255,255));
            readsContentChart.addSeries("G", bamStats.getReadsGsHistogram(), new Color(0, 255,0,255));
            readsContentChart.addSeries("T", bamStats.getReadsTsHistogram(), new Color(0, 0, 0,255));
            readsContentChart.addSeries("N", bamStats.getReadsNsHistogram(), new Color(0, 255, 255, 255));
            readsContentChart.setAdjustDomainAxisLimits(false);
            readsContentChart.setDomainAxisIntegerTicks(true);
            readsContentChart.setPercentageChart(true);
            readsContentChart.render();
            charts.add(new QChart(bamStats.getName() + "_reads_content_per_read_position.png",
                    readsContentChart.getChart(), readsContentChart));
        }


        /////////////////////////////// Reads GC Content histogram ///////////////////////////////////

        BamQCChart gcContentHistChart = new BamQCChart(Constants.PLOT_TITLE_READS_GC_CONTENT, subTitle,
                "GC Content (%)", "Fraction of reads");
		gcContentHistChart.addSeries("Sample", bamStats.getGcContentHistogram(), new Color(20, 10, 255, 255));
        if (!genomeGCContentName.isEmpty()) {
            XYVector gcContentHist = getGenomeGcContentHistogram();
            if (gcContentHist.getSize() != 0) {
                gcContentHistChart.addSeries(genomeGCContentName, gcContentHist, new Color(255, 10, 20, 255));
            }
        }
        gcContentHistChart.setDomainAxisIntegerTicks(true);
        gcContentHistChart.setAdjustDomainAxisLimits(false);
        gcContentHistChart.setPercentageChart(false);
        gcContentHistChart.render();
		charts.add(new QChart(bamStats.getName() + "_gc_content_per_window.png", gcContentHistChart.getChart(),
                gcContentHistChart ));

        ///////////////// Clipping profile ///////////////


        if (bamStats.clippingIsPresent()) {
            BamQCChart clippingProfile = new BamQCChart(Constants.PLOT_TITLE_READS_CLIPPING_PROFILE,
                    subTitle, "Read position (bp)", " Clipped bases (%)");
            clippingProfile.addSeries("Clipping profile", bamStats.getReadsClippingProfileHistogram(), new Color(255, 0, 0, 255));
            clippingProfile.setAdjustDomainAxisLimits(false);
            clippingProfile.setDomainAxisIntegerTicks(true);
            clippingProfile.setPercentageChart(true);
            clippingProfile.setShowLegend(false);
            clippingProfile.render();
            charts.add(new QChart(bamStats.getName() + "_reads_clipping_profile.png",
                    clippingProfile.getChart(), clippingProfile));

        }

        ///////////////// Homopolymer indels ///////////////

        if (bamStats.getNumIndels() > 0 ) {
            BamQCBarChart homopolymerIndels = new BamQCBarChart( Constants.PLOT_TITLE_HOMOPOLYMER_INDELS,
                    subTitle, "Type of indel", "Number of indels", bamStats.getHomopolymerIndels() );
            homopolymerIndels.render();
            charts.add( new QChart(bamStats.getName() + "_homopolymer_indels", homopolymerIndels.getChart(), homopolymerIndels ));
        }


		///////////////// mapping quality charts ///////////////

		// mapping quality across reference
		BamQCChart mappingQuality = new BamQCChart(Constants.PLOT_TITLE_MAPPING_QUALITY_ACROSS_REFERENCE,
                subTitle, "Position (bp)", "Mapping quality");
		mappingQuality.addSeries("mapping quality",new XYVector(windowReferences, bamStats.getMappingQualityAcrossReference()), new Color(250,50,50,150));
        mappingQuality.setSeriesToExportIndex(0);
        if( paintChromosomeLimits && locator!=null ) {
            mappingQuality.addSeries("chromosomes", chromosomeBytedLimits, chromosomeColor, stroke,
                    false, chromosomeAnnotations);
            mappingQuality.writeChromsomeNames(220, chromosomeNames);

        }
		mappingQuality.render();
		mappingQuality.getChart().getXYPlot().getRangeAxis().setRange(0,255);
		charts.add(new QChart(bamStats.getName() + "_mapping_quality_across_reference.png",
                mappingQuality.getChart(), mappingQuality ) );


		// mapping quality histogram
		BamQCXYHistogramChart mappingQualityHistogram =
                new BamQCXYHistogramChart(Constants.PLOT_TITLE_MAPPING_QUALITY_HISTOGRAM,
                        subTitle, "Mapping quality", "Number of genomic locations");
		mappingQualityHistogram.addHistogram("mapping quality", bamStats.getMappingQualityHistogram(), Color.blue);
		mappingQualityHistogram.setNumberOfBins(50);
        mappingQualityHistogram.render();
		charts.add(new QChart( bamStats.getName() + "_mapping_quality_histogram.png",
                mappingQualityHistogram.getChart(), mappingQualityHistogram ) );


        // Insert size

		if(isPairedData){
			// insert size across reference
			BamQCChart insertSize = new BamQCChart(Constants.PLOT_TITLE_INSERT_SIZE_ACROSS_REFERENCE,
                    subTitle, "Position (bp)", "Insert size (bp)");
			insertSize.addSeries("insert size",new XYVector(windowReferences, bamStats.getInsertSizeAcrossReference()), new Color(15,170,90,150));
            if(paintChromosomeLimits && locator!=null) {
                insertSize.addSeries("chromosomes",chromosomeInsertSizeLimits,chromosomeColor,stroke,
                        false,chromosomeAnnotations);
                insertSize.writeChromsomeNames(maxInsertSize*0.9, chromosomeNames);

            }
            insertSize.render();
            insertSize.getChart().getXYPlot().getRangeAxis().setRange(0,maxInsertSize);

			charts.add(new QChart( bamStats.getName() + "_insert_size_across_reference.png",
                    insertSize.getChart(), insertSize ));
	
			// mapping quality histogram
			BamQCXYHistogramChart insertSizeHistogram =
                    new BamQCXYHistogramChart(Constants.PLOT_TITLE_INSERT_SIZE_HISTOGRAM,
                            subTitle, "Insert size (bp)", "Number of reads");
			insertSizeHistogram.addHistogram("insert size", bamStats.getInsertSizeHistogram(), new Color(15,170,90,150));
			insertSizeHistogram.setNumberOfBins(50);
            insertSizeHistogram.setRangeAxisIntegerTicks(true);
			insertSizeHistogram.render();
			charts.add(new QChart(bamStats.getName() + "_insert_size_histogram.png",
                    insertSizeHistogram.getChart(), insertSizeHistogram));
		}
	}

    private String formatLong(long decimal){
        return decimailFormatter.format(decimal);
    }

	private String formatDecimal(double decimal){
        return decimailFormatter.format(decimal);
	}

	private String formatPercentage(double percentage){
		return decimailFormatter.format(percentage) + "%";
    }




	/**
	 * @param paintChromosomeLimits the paintChromosomeLimits to set
	 */
	public void setPaintChromosomeLimits(boolean paintChromosomeLimits) {
		this.paintChromosomeLimits = paintChromosomeLimits;
	}

	public Long getaNumber() {
		return aNumber;
	}

	public Long getcNumber() {
		return cNumber;
	}

	public Long getgNumber() {
		return gNumber;
	}

	public Long gettNumber() {
		return tNumber;
	}

	public Long getnNumber() {
		return nNumber;
	}

	public Double getaPercent() {
		return aPercent;
	}

	public Double getcPercent() {
		return cPercent;
	}

	public Double getgPercent() {
		return gPercent;
	}

	public Double gettPercent() {
		return tPercent;
	}

	public Double getnPercent() {
		return nPercent;
	}

	public Double getGcPercent() {
		return gcPercent;
	}

	public Double getPercentMappedReads() {
		return percentMappedReads;
	}

	public List<StatsKeeper.Section> getInputDataSections() {
        return inputDataKeeper.getSections();
    }

    private void prepareSummaryStatsKeeper() {
        StringUtilsSwing sdf = new StringUtilsSwing();
        String postfix = getNamePostfix();

        if (warnings != null && !warnings.isEmpty()) {
            StatsKeeper.Section warningsSection = new StatsKeeper.Section("Warnings");
            for (Map.Entry<String,String> entry : warnings.entrySet()) {
                warningsSection.addRow(entry.getKey(), entry.getValue());
            }
            summaryStatsKeeper.addSection(warningsSection);
        }

        StatsKeeper.Section globals = new StatsKeeper.Section("Globals");

        globals.addRow("Reference size", sdf.formatLong(referenceSize));

        globals.addRow("Number of reads", sdf.formatLong(numReads));

        globals.addRow("Mapped reads", sdf.formatLong(numMappedReads)
                + " / " + sdf.formatPercentage(getPercentMappedReads()));

        globals.addRow("Unmapped reads",
                sdf.formatLong(numReads - numMappedReads) + " / "
                        + sdf.formatPercentage(100.0 - getPercentMappedReads()));

        globals.addRow("Mapped paired reads",
                sdf.formatLong(numPairedReads) + " / "
                        + sdf.formatPercentage(percantagePairedReads) );
        if (numPairedReads > 0) {

            globals.addRow("Mapped reads, first in pair",
                    sdf.formatLong(numberOfMappedFirstOfPair) + " / " +
                    sdf.formatPercentage(percentageOfMappedFirstOfPair));

            globals.addRow("Mapped reads,  second in pair",
                    sdf.formatLong(numberOfMappedSecondOfPair) + " / " +
                    sdf.formatPercentage(percentageOfMappedSecondOfPair));

            globals.addRow("Mapped reads, both in pair",
                                       sdf.formatLong(numPairedReads - numSingletons) + " / "
                                               + sdf.formatPercentage((getPercentageBothMatesPaired())));

            globals.addRow("Mapped reads, singletons",
                    sdf.formatLong(numSingletons) + " / "
                            + sdf.formatPercentage(getPercentSingletons()));


        }



        globals.addRow("Read min/max/mean length",
                sdf.formatLong(readMinSize) + " / "
                        + sdf.formatLong(readMaxSize) + " / "
                        + sdf.formatDecimal(readMeanSize));




        if (numSelectedRegions == 0) {

            if (includeIntersectingPairs) {
                globals.addRow("Overlapping read pairs",
                        sdf.formatLong(numOverlappingReadPairs) + " / " +
                        sdf.formatPercentage(getPercentageOverlappingPairs()));
            }

            if (numDuplicatedReadsMarked > 0) {
                globals.addRow("Duplicated reads (flagged)", sdf.formatLong(numDuplicatedReadsMarked) + " / " +
                                                       sdf.formatPercentage(getPercentageDublicateReadsMarked()));
            }

            if (numDuplicatedReadsEstimated > 0) {
                globals.addRow("Duplicated reads (estimated)",
                        sdf.formatLong(numDuplicatedReadsEstimated) + " / " +
                        sdf.formatPercentage(getPercentageDublicateReadsEstimated()));
                globals.addRow("Duplication rate", sdf.formatPercentage(duplicationRate));
            }

        }

        globals.addRow("Clipped reads",
                        sdf.formatInteger(numClippedReads) + " / " +
                        sdf.formatPercentage(getPercentageClippedReads()));


        if (numDuplicatesSkipped > 0) {
            globals.addRow("Duplicated reads skipped: ",
                    sdf.formatLong(numDuplicatesSkipped) + " / " +
                    sdf.formatPercentage(getPercentageSkippedDublicatedReads()));
        }


        summaryStatsKeeper.addSection(globals);

        if (numSelectedRegions > 0) {

            StatsKeeper.Section globalsInRegions = new StatsKeeper.Section("Globals" + postfix);
            globalsInRegions.addRow("Regions size/percentage of reference",
                    sdf.formatLong((numBasesInsideRegions))
                            + " / " + sdf.formatPercentage(getSelectedRegionsPercentage()));

            globalsInRegions.addRow("Mapped reads",
                    sdf.formatLong(numMappedReadsInRegions)
                            + "  /  " + sdf.formatPercentage(percentageMappedReadsInRegions));
            if (numPairedReads > 0) {
                globalsInRegions.addRow("Mapped reads, only first in pair",
                        sdf.formatLong(numMappedFirstOfPairInRegions) + " / " +
                                sdf.formatPercentage(percentageOfMappedFirstOfPairInRegions));

                globalsInRegions.addRow("Mapped reads, only second in pair",
                        sdf.formatLong(numMappedSecondOfPairInRegions) + " / " +
                                sdf.formatPercentage(percentageOfMappedSecondOfPairInRegions));
                globalsInRegions.addRow("Mapped reads, both in pair",
                                   sdf.formatLong(numPairedReadsInRegions - numSingletonsInRegions) + " / "
                                        + sdf.formatPercentage((getPercentageBothMatesPairedInRegions())));
                globalsInRegions.addRow("Mapped reads, singletons",
                        sdf.formatLong(numSingletonsInRegions) + " / "
                                + sdf.formatPercentage(percentageSingletonsInRegions));


                globalsInRegions.addRow("Correct strand reads",
                        sdf.formatLong(numCorrectStrandReads) + " / " +
                        sdf.formatPercentage(percentageCorrectStrandReads) );


                globalsInRegions.addRow("Clipped reads",
                                    sdf.formatInteger(numClippedReads) + " / " +
                                    sdf.formatPercentage(getPercentageClippedReads()));

                if (includeIntersectingPairs) {
                    globalsInRegions.addRow("Overlapping read pairs",
                        sdf.formatLong(numOverlappingReadPairs)+ " / " +
                        sdf.formatPercentage(getPercentageOverlappingPairs()));
                }

            }

            if (numDuplicatedReadsMarked > 0) {
                globalsInRegions.addRow("Duplicated reads (flagged)", sdf.formatLong(numDuplicatedReadsMarked) + " / " +
                                  sdf.formatPercentage(getPercentageDublicateReadsMarked()));
            }

            if (numDuplicatedReadsEstimated > 0) {
                globalsInRegions.addRow("Duplicated reads (estimated)",
                    sdf.formatLong(numDuplicatedReadsEstimated) + " / " +
                    sdf.formatPercentage(getPercentageDublicateReadsEstimated()));
                globalsInRegions.addRow("Duplication rate", sdf.formatPercentage(duplicationRate));
            }



            summaryStatsKeeper.addSection(globalsInRegions);

        }

        StatsKeeper.Section acgtContent = new StatsKeeper.Section("ACGT Content" + postfix);

        acgtContent.addRow("Number/percentage of A's", sdf.formatLong(getaNumber()) +
                " / " + sdf.formatPercentage(getaPercent()));
		acgtContent.addRow("Number/percentage of C's",sdf.formatLong(getcNumber()) +
                " / " + sdf.formatPercentage(getcPercent()));
		acgtContent.addRow("Number/percentage of T's", sdf.formatLong(gettNumber()) +
                " / " + sdf.formatPercentage(gettPercent()));
		acgtContent.addRow("Number/percentage of G's",sdf.formatLong(getgNumber()) +
                " / " + sdf.formatPercentage(getgPercent()));
		acgtContent.addRow("Number/percentage of N's",sdf.formatLong(getnNumber()) +
                " / " + sdf.formatPercentage(getnPercent()));
		acgtContent.addRow("GC Percentage", sdf.formatPercentage(getGcPercent()));

        summaryStatsKeeper.addSection(acgtContent);



		StatsKeeper.Section coverageSection = new StatsKeeper.Section("Coverage" + postfix);
		coverageSection.addRow("Mean", sdf.formatDecimal(meanCoverage));
		coverageSection.addRow("Standard Deviation",sdf.formatDecimal(stdCoverage) );

        if (numOverlappingReadPairs > 0) {
            coverageSection.addRow("Mean (paired-end reads overlap ignored)", sdf.formatDecimal(adaptedMeanCoverage));
        }

		summaryStatsKeeper.addSection(coverageSection);

		StatsKeeper.Section mappingQualitySection = new StatsKeeper.Section("Mapping Quality" + postfix);
		mappingQualitySection.addRow("Mean Mapping Quality", sdf.formatDecimal(meanMappingQuality));
		summaryStatsKeeper.addSection(mappingQualitySection);

        if (meanInsertSize != 0)
        {
            StatsKeeper.Section insertSizeSection = new StatsKeeper.Section("Insert size" + postfix);
            insertSizeSection.addRow("Mean", sdf.formatDecimal(meanInsertSize));
            insertSizeSection.addRow("Standard Deviation", sdf.formatDecimal(stdInsertSize));
            insertSizeSection.addRow("P25/Median/P75", sdf.formatDecimal(p25InsertSize) + " / " +
                    sdf.formatDecimal(medianInsertSize) + " / " + sdf.formatDecimal(p75InsertSize));
            summaryStatsKeeper.addSection(insertSizeSection);
        }

        int numIndels = numInsertions + numDeletions;
        if ( numIndels > 0 || numMismatches > 0 || alignmentErrorRate > 0) {
            StatsKeeper.Section indelsSection = new StatsKeeper.Section("Mismatches and indels" + postfix);
            if (alignmentErrorRate > 0) {
                indelsSection.addRow("General error rate", sdf.formatPercentage(alignmentErrorRate * 100.0));
            }
            if (numMismatches > 0) {
                indelsSection.addRow("Mismatches",sdf.formatDecimal(numMismatches));
            }
            //indelsSection.addRow("Total reads with indels", sdf.formatInteger(numIndels));
            if (numIndels > 0) {
                indelsSection.addRow("Insertions",sdf.formatDecimal(numInsertions) );
                indelsSection.addRow("Mapped reads with at least one insertion",
                        sdf.formatPercentage(readsWithInsertionPercentage));
                indelsSection.addRow("Deletions",sdf.formatDecimal(numDeletions) );
                indelsSection.addRow("Mapped reads with at least one deletion",
                        sdf.formatPercentage(readsWithDeletionPercentage));
                indelsSection.addRow("Homopolymer indels",sdf.formatPercentage(homopolymerIndelFraction * 100.0) );
            }

            summaryStatsKeeper.addSection(indelsSection);

        }


    }

    private void prepareChromosomeStatsKeeper(BamStats.ChromosomeInfo[] statsArray) {


        tableDataStatsKeeper = new StatsKeeper();
        tableDataStatsKeeper.setName("Chromosome stats" + namePostfix );

        if (numMappedReads == 0) {
            return;
        }

        StatsKeeper.Section headerSection = new StatsKeeper.Section(Constants.TABLE_STATS_HEADER);
        String[] header = {
                "Name", "Length", "Mapped bases", "Mean coverage", "Standard deviation"
        };
        headerSection.addRow(header);
        tableDataStatsKeeper.addSection(headerSection);


        StatsKeeper.Section dataSection = new StatsKeeper.Section(Constants.TABLE_STATS_DATA);

        for (BamStats.ChromosomeInfo statsRecord : statsArray) {
            String[] row = new String[5];
            row[0] =  statsRecord.getName();
            row[1] = Long.toString(statsRecord.getLength());
            row[2] = Long.toString(statsRecord.getNumBases());
            row[3] = StringUtils.decimalFormat(statsRecord.getCovMean(),"#,###,###,###.##");
            row[4] = StringUtils.decimalFormat(statsRecord.getCovStd(),"#,###,###,###.##");

            dataSection.addRow(row);
        }

        tableDataStatsKeeper.addSection(dataSection);

    }

    public XYVector getGenomeGcContentHistogram() {
        XYVector res = new XYVector();
        try {

            String pathToGC = Constants.pathResources + BamStatsAnalysis.getGcContentFileMap().get(genomeGCContentName);

            //System.out.println("Path to genome is " + pathToGC);

            BufferedReader reader =  new BufferedReader(
                    new InputStreamReader( getClass().getResourceAsStream( pathToGC ) ) );

            String line;

            double acum = 0;
            int counter = -1;
            double index = 1;
            while ( (line = reader.readLine()) != null ) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] vals = line.split(" : ");
                double value = Double.parseDouble(vals[1].trim());
                // skip the zero value
                //if (index == 0.0) {
                //    continue;
                //}
                acum += value;
                counter++;
                if (counter == 10) {
                    res.addItem(new XYItem(index, acum));
                    acum = 0;
                    counter = 0;
                    index += 1;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        }

        return res;
    }

    public Properties generateBamQcProperties() {

        Properties prop = new Properties();


        List<StatsKeeper.Section> summarySections = getSummaryDataSections();
        for (StatsKeeper.Section s : summarySections) {
            String sectionName = s.getName().toUpperCase().replaceAll("\\s","_");
            for (String[] row : s.getRows()) {
                StringBuilder builder = new StringBuilder();
                builder.append(sectionName).append("_").append(row[0].replaceAll("\\s","_"));
                prop.setProperty(builder.toString(), row[1].replaceAll("\\s", ""));
            }
        }

        /*prop.setProperty("refSize", referenceSize.toString());
        prop.setProperty("numContigs", contigsNumber.toString());
        prop.setProperty("numRegions", Integer.toString(numSelectedRegions));

        // globals
        prop.setProperty("numWindows", numWindows.toString());
        prop.setProperty("numReads", numReads.toString());
        prop.setProperty("numMappedReads", numMappedReads.toString());
        prop.setProperty("percentMappedReads", percentMappedReads.toString());
        prop.setProperty("numMappedBases", numMappedBases.toString());
        prop.setProperty("numSequencedBases", numSequencedBases.toString());
        prop.setProperty("numAlignedBases", numAlignedBases.toString());
        prop.setProperty("numPairedReads", Integer.toString(numPairedReads));
        prop.setProperty("numSingletons", Integer.toString(numSingletons));
        prop.setProperty("duplicationRate", Double.toString(duplicationRate));

        // regions related
        prop.setProperty("refSizeInRegions", Long.toString(numBasesInsideRegions));
        prop.setProperty("numMappedReadsInRegions", Integer.toString(numMappedReadsInRegions));
        prop.setProperty("percentMappedReadsInRegions", Double.toString(percentageMappedReadsInRegions));
        prop.setProperty("numPairedReadsInRegions", Integer.toString(numPairedReadsInRegions));
        prop.setProperty("numSingletonsInRegions", Integer.toString(numSingletonsInRegions));

        // coverageData
        prop.setProperty("meanCoverage", meanCoverage.toString());
        prop.setProperty("stdCoverage", stdCoverage.toString());

        // mapping quality
        prop.setProperty("meanMappingQuality", meanMappingQuality.toString());

        // insert size
        prop.setProperty("meanInsertSize", meanInsertSize.toString());

        // actg content
        prop.setProperty("aNumber", aNumber.toString());
        prop.setProperty("aPercent", aPercent.toString());
        prop.setProperty("cNumber", cNumber.toString());
        prop.setProperty("cPercent", cPercent.toString());
        prop.setProperty("tNumber", tNumber.toString());
        prop.setProperty("tPercent", tPercent.toString());
        prop.setProperty("gNumber", gNumber.toString());
        prop.setProperty("gPercent", gPercent.toString());
        prop.setProperty("nNumber", nNumber.toString());
        prop.setProperty("nPercent",  nPercent.toString());
        prop.setProperty("gcPercent", gcPercent.toString()); */


        // per reference stats
        StatsKeeper.Section dataSection = tableDataStatsKeeper.getSections().get(1);

        for (String[] row : dataSection.getRows() ) {
            prop.setProperty(row[0], Arrays.toString(ArrayUtils.subarray(row, 1, row.length - 1)) );
        }


        return prop;
    }


    public double getSelectedRegionsPercentage() {
        return (numBasesInsideRegions / (double) referenceSize) * 100.0;
    }


}
