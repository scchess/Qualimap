package org.bioinfo.ngs.qc.qualimap.beans;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
import org.bioinfo.commons.utils.ListUtils;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.ngs.qc.qualimap.gui.panels.HtmlJPanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
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

public class BamQCRegionReporter implements Serializable {

    private int numCorrectStrandReads;

    public String getNamePostfix() {
        return namePostfix;
    }

    public double getPercentSingletons() {
        return percentageSingletons;
    }

    public double getPercentageBothMatesPaired() {
        return ((numPairedReads - numSingletons) * 100.0) / (double) numReads ;
    }

    public double getPercentageBothMatesPairedInRegions() {
       return ((numPairedReadsInRegions - numSingletonsInRegions) * 100.0) / (double) numReads ;
    }

    public void setGenomeGCContentName(String genomeName) {
        this.genomeGCContentName = genomeName;
    }

    private boolean paintChromosomeLimits;
    private List<QChart> charts;

	/** Variable that contains the input files names */
	private String bamFileName, referenceFileName;

	private Integer numWindows, numMappedReads;

	private Long referenceSize, contigsNumber, aNumber, cNumber, gNumber,
	tNumber, nNumber, numReads, numMappedBases, numSequencedBases,
	numAlignedBases, aReferenceNumber, cReferenceNumber, gReferenceNumber,
	tReferenceNumber, nReferenceNumber, numBasesInsideRegions;

	private Double aPercent, cPercent, gPercent, tPercent, nPercent,
	gcPercent, percentMappedReads, meanMappingQuality, meanInsertSize,
    medianInsertSize,
	aReferencePercent, cReferencePercent, gReferencePercent,
	tReferencePercent, nReferencePercent, meanCoverage, stdCoverage;

    int readMinSize, readMaxSize, numClippedReads;
    double readMeanSize;

    private int numPairedReads, numberOfMappedFirstOfPair, numberOfMappedSecondOfPair;
    private double percantagePairedReads, percentageOfMappedFirstOfPair, percentageOfMappedSecondOfPair;
    private int numSingletons;
    private double percentageSingletons;

    private int numMappedReadsInRegions, numPairedReadsInRegions;
    private double percentageMappedReadsInRegions;

    private int numMappedFirstOfPairInRegions, numMappedSecondOfPairInRegions;
    private double percentageOfMappedFirstOfPairInRegions, percentageOfMappedSecondOfPairInRegions;
    private int numSingletonsInRegions;
    private double percentageSingletonsInRegions, percentageCorrectStrandReads;

    private double duplicationRate;

    private int numInsertions, numDeletions;
    private double homopolymerIndelFraction;

    StatsKeeper inputDataKeeper;
    StatsKeeper summaryStatsKeeper;
    StatsKeeper chromosomeStatsKeeper;

    private Map<String,String> warnings;
    String namePostfix;
    String genomeGCContentName;
    String chromosomeFilePath;
    int numSelectedRegions;

    public BamQCRegionReporter() {
        namePostfix = "";
        inputDataKeeper = new StatsKeeper();
        summaryStatsKeeper = null;
        chromosomeStatsKeeper = null;
        chromosomeFilePath = "";
        genomeGCContentName = "";
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
		if(bamStats.isReferenceAvailable()) {
			report.println("     reference file = " + bamStats.getReferenceFile());
			report.println("");			
			report.println("     number of A's = " + formatLong(bamStats.getNumberOfAsInReference()) +  " bp (" + formatPercentage(bamStats.getMeanARelativeContentPerWindowInReference()) + ")");
			report.println("     number of C's = " + formatLong(bamStats.getNumberOfCsInReference()) +  " bp (" + formatPercentage(bamStats.getMeanCRelativeContentPerWindowInReference()) + ")");
			report.println("     number of T's = " + formatLong(bamStats.getNumberOfTsInReference()) +  " bp (" + formatPercentage(bamStats.getMeanTRelativeContentPerWindowInReference()) + ")");
			report.println("     number of G's = " + formatLong(bamStats.getNumberOfGsInReference()) +  " bp (" + formatPercentage(bamStats.getMeanGRelativeContentPerWindowInReference()) + ")");
			report.println("     number of N's = " + formatLong(bamStats.getNumberOfNsInReference()) +  " bp (" + formatPercentage(bamStats.getMeanNRelativeContentPerWindowInReference()) + ")");
			report.println("");
			report.println("     GC percentage = " + formatPercentage(bamStats.getMeanGcRelativeContentPerWindowInReference()));
			report.println("     AT percentage = " + formatPercentage(bamStats.getMeanAtRelativeContentPerWindowInReference()));
		} else {
			report.println("");
			report.println("     (reference file is not available)");
			report.println("");
		}
		report.println("");
		report.println("");

		// globals
		report.println(">>>>>>> Globals");
		report.println("");
		report.println("     number of windows = " + bamStats.getNumberOfWindows());
		report.println("");
		report.println("     number of reads = " + formatLong(bamStats.getNumberOfReads()));		
		report.println("     number of mapped reads = " + formatInteger(bamStats.getNumberOfMappedReads()) + " (" + formatPercentage(bamStats.getPercentageOfMappedReads())+ ")");
		report.println("");
		report.println("     number of mapped bases = " + formatLong(bamStats.getNumberOfMappedBases()) + " bp");
		report.println("     number of sequenced bases = " + formatLong(bamStats.getNumberOfSequencedBases()) + " bp");
		report.println("     number of aligned bases = " + formatLong(bamStats.getNumberOfAlignedBases()) + " bp");
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
		report.println("     number of A's = " + formatLong(bamStats.getNumberOfAs()) +  " bp (" + formatPercentage(bamStats.getMeanARelativeContent()) + ")");
		report.println("     number of C's = " + formatLong(bamStats.getNumberOfCs()) +  " bp (" + formatPercentage(bamStats.getMeanCRelativeContent()) + ")");
		report.println("     number of T's = " + formatLong(bamStats.getNumberOfTs()) +  " bp (" + formatPercentage(bamStats.getMeanTRelativeContent()) + ")");
		report.println("     number of G's = " + formatLong(bamStats.getNumberOfGs()) +  " bp (" + formatPercentage(bamStats.getMeanGRelativeContent()) + ")");
		report.println("     number of N's = " + formatLong(bamStats.getNumberOfNs()) +  " bp (" + formatPercentage(bamStats.getMeanNRelativeContent()) + ")");
		report.println("");
		report.println("     GC percentage = " + formatPercentage(bamStats.getMeanGcRelativeContent()));
		//		report.println("     AT percentage = " + formatPercentage(bamStats.getMeanAtRelativeContent()));
		report.println("");
		report.println("");

		// coverageData
		report.println(">>>>>>> Coverage");
		report.println("");
		report.println("     mean coverageData = " + formatDecimal(bamStats.getMeanCoverage()) + "X");
		report.println("     std coverageData = " + formatDecimal(bamStats.getStdCoverage()) + "X");
		report.println("");
		for(int i=0; i<bamStats.getCoverageQuotes().getSize(); i++){
			report.println("     There is a " + StringUtils.decimalFormat(bamStats.getCoverageQuotes().get(i).getY(),"#0.##") + "% of reference with a coverageData >= " + (1+(int)bamStats.getCoverageQuotes().get(i).getX()) + "X");
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
		this.bamFileName = bamStats.getSourceFile();
		this.referenceSize = bamStats.getReferenceSize();
		this.contigsNumber = bamStats.getNumberOfReferenceContigs();

		if(bamStats.isReferenceAvailable()) {
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
		}

		// globals
		this.numWindows = bamStats.getNumberOfWindows();
		this.numReads = bamStats.getNumberOfReads();
		this.numMappedReads = bamStats.getNumberOfMappedReads();
		this.percentMappedReads = bamStats.getPercentageOfMappedReads();
		this.numMappedBases = bamStats.getNumberOfMappedBases();
		this.numSequencedBases = bamStats.getNumberOfSequencedBases();
		this.numAlignedBases = bamStats.getNumberOfAlignedBases();
        this.duplicationRate = bamStats.getDuplicationRate();

        // paired reads
        this.numPairedReads = bamStats.getNumberOfPairedReads();
        this.percantagePairedReads = bamStats.getPercentageOfPairedReads();
        this.numSingletons = bamStats.getNumberOfSingletons();
        this.percentageSingletons = bamStats.getPercentageOfSingletons();
        this.numberOfMappedFirstOfPair = bamStats.getNumberOfMappedFirstOfPair();
        this.percentageOfMappedFirstOfPair = ( (double) numberOfMappedFirstOfPair / numReads) * 100.0;
        this.numberOfMappedSecondOfPair = bamStats.getNumberOfMappedSecondOfPair();
        this.percentageOfMappedSecondOfPair = ( (double) numberOfMappedSecondOfPair / numReads ) * 100.0;
        this.numClippedReads = bamStats.getNumClippedReads();

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
        this.medianInsertSize = bamStats.getMedianInsertSize();

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

        // read sizes
        readMaxSize = bamStats.getReadMaxSize();
        readMinSize = bamStats.getReadMinSize();
        readMeanSize = bamStats.getReadMeanSize();

        // indels
        numInsertions = bamStats.getNumInsertions();
        numDeletions = bamStats.getNumDeletions();
        homopolymerIndelFraction = bamStats.getHomopolymerIndelFraction();


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
        if (isPairedData) {
            int length = bamStats.getInsertSizeHistogram().getSize();
            maxInsertSize = bamStats.getInsertSizeHistogram().get(length - 1).getX();
        }
        // compute chromosome limits
		Color chromosomeColor = new Color(40,40,40,150);
		XYVector chromosomeCoverageLimits = null;
		XYVector chromosomePercentageLimits = null;
		XYVector chromosomeBytedLimits = null;
        XYVector chromosomeInsertSizeLimits = null;
        List<XYBoxAnnotation>  chromosomeAnnotations = null;

        if(paintChromosomeLimits && locator!=null){
			int numberOfChromosomes = locator.getContigs().size();
			chromosomeCoverageLimits = new XYVector();
			chromosomePercentageLimits = new XYVector();
			chromosomeBytedLimits = new XYVector();
            chromosomeInsertSizeLimits = new XYVector();
            chromosomeAnnotations = new ArrayList<XYBoxAnnotation>();
            for(int i=0; i<numberOfChromosomes; i++){
                long chrStart = locator.getContigs().get(i).getPosition();
                long chrSize = locator.getContigs().get(i).getSize();

                XYBoxAnnotation xyBoxAnnotation = new XYBoxAnnotation( (double) chrStart, 0.0,
                    (double)( chrStart + chrSize), maxCoverage, null, null);
                xyBoxAnnotation.setToolTipText(locator.getContigs().get(i).getName());
                chromosomeAnnotations.add(xyBoxAnnotation);

            	// coverageData
				chromosomeCoverageLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),0));
				chromosomeCoverageLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),maxCoverage));
				chromosomeCoverageLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),0));
				// percentage
				chromosomePercentageLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),0));
				chromosomePercentageLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),100));
				chromosomePercentageLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),0));
				// byte
				chromosomeBytedLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),0));
				chromosomeBytedLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),255));
				chromosomeBytedLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),0));
                // insert size
                chromosomeInsertSizeLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),0));
                chromosomeInsertSizeLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),maxInsertSize));
                chromosomeInsertSizeLimits.addItem(new XYItem(locator.getContigs().get(i).getEnd(),0));


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
        if( paintChromosomeLimits && locator!=null ) {
            coverageChart.addSeries("chromosomes", chromosomeCoverageLimits, chromosomeColor, stroke,
                    false, chromosomeAnnotations);
        }
        coverageChart.render();

        //Setting "smart" zoom for coverage across region
        Double[] coverageData = ListUtils.toArray(bamStats.getCoverageAcrossReference());
        double upperCoverageBound = 2*StatUtils.percentile(ArrayUtils.toPrimitive(coverageData), 90);
        if (upperCoverageBound == 0) {
            // possible in rare cases when the coverage is very low coverage
            upperCoverageBound = maxCoverage*0.9;
        }
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


        ////////////////////////////// Reads nucleotide content ////////////////////////////////////
        if (bamStats.getReadMaxSize() > 0) {
            BamQCChart readsContentChart = new BamQCChart(Constants.PLOT_TITLE_READS_NUCLEOTIDE_CONTENT,
                    subTitle, " Position (bp)", " Nucleotide Content (%) ");
            readsContentChart.addSeries("% A", bamStats.getReadsAsHistogram(), new Color(255, 0,0,255));
            readsContentChart.addSeries("% C", bamStats.getReadsCsHistogram(), new Color(0, 0,255,255));
            readsContentChart.addSeries("% G", bamStats.getReadsGsHistogram(), new Color(0, 255,0,255));
            readsContentChart.addSeries("% T", bamStats.getReadsTsHistogram(), new Color(0, 0, 0,255));
            readsContentChart.addSeries("% N", bamStats.getReadsNsHistogram(), new Color(0, 255, 255, 255));
            readsContentChart.setAdjustDomainAxisLimits(false);
            readsContentChart.setDomainAxisIntegerTicks(true);
            readsContentChart.setPercentageChart(true);
            readsContentChart.render();
            charts.add(new QChart(bamStats.getName() + "_reads_content_per_read_position.png",
                    readsContentChart.getChart(), readsContentChart));
        }

        if (bamStats.clippingIsPresent()) {
            BamQCChart clippingProfile = new BamQCChart(Constants.PLOT_TITLE_READS_CLIPPING_PROFILE,
                    subTitle, "Read position (bp)", " Bases clipped (%)");
            clippingProfile.addSeries("Clipping profile", bamStats.getReadsClippingProfileHistogram(), new Color(255, 0, 0, 255));
            clippingProfile.setAdjustDomainAxisLimits(false);
            clippingProfile.setDomainAxisIntegerTicks(true);
            clippingProfile.setPercentageChart(true);
            clippingProfile.render();
            charts.add(new QChart(bamStats.getName() + "_reads_clipping_profile.png",
                    clippingProfile.getChart(), clippingProfile));

        }

        if (bamStats.getNumIndels() > 0 ) {
            BamQCBarChart homopolymerIndels = new BamQCBarChart( Constants.PLOT_TITLE_HOMOPOLYMER_INDELS,
                    subTitle, "Type of indel", "Number of indels", bamStats.getHomopolymerIndels() );
            homopolymerIndels.render();
            charts.add( new QChart(bamStats.getName() + "_homopolymer_indels", homopolymerIndels.getChart(), homopolymerIndels ));
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



		///////////////// mapping quality charts ///////////////

		// mapping quality across reference
		BamQCChart mappingQuality = new BamQCChart(Constants.PLOT_TITLE_MAPPING_QUALITY_ACROSS_REFERENCE,
                subTitle, "Position (bp)", "Mapping quality");
		mappingQuality.addSeries("mapping quality",new XYVector(windowReferences, bamStats.getMappingQualityAcrossReference()), new Color(250,50,50,150));
        mappingQuality.setSeriesToExportIndex(0);
        if( paintChromosomeLimits && locator!=null ) {
            mappingQuality.addSeries("chromosomes", chromosomeBytedLimits, chromosomeColor, stroke,
                    false, chromosomeAnnotations);
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
            }
            insertSize.render();
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


	private String formatInteger(int decimal){
		return StringUtils.decimalFormat(decimal,"###,###,###,###,###,###,###.##");
	}

	private String formatLong(long decimal){
		return StringUtils.decimalFormat(decimal,"###,###,####,###,###,###,###,###,###,###,###.##");
	}

	private String formatDecimal(double decimal){
		return StringUtils.decimalFormat(decimal,"###,###,###,###,###,###,###.##");
	}

	private String formatPercentage(double percentage){
		return StringUtils.decimalFormat(percentage,"###.##")+"%";
	}


    public void addInputDataSection(String name, Map<String,String> paramsMap) {

        StatsKeeper.Section section = new StatsKeeper.Section(name);
        section.addData(paramsMap);

        inputDataKeeper.addSection(section);
    }

    public String getInputDescription(int tableWidth) {


        if (inputDataKeeper.getSections().isEmpty()) {
            return "No input description is available";
        }

        StringBuilder inputDesc = new StringBuilder();

        inputDesc.append("<p align=center><a name=\"input\"> <b>Input data & parameters</b></p>" + HtmlJPanel.BR);
        inputDesc.append(HtmlJPanel.getTableHeader(tableWidth, "EEEEEE"));

        List<StatsKeeper.Section> inputDataSections = inputDataKeeper.getSections();

        for (StatsKeeper.Section section : inputDataSections) {
            inputDesc.append(HtmlJPanel.COLSTART).append("<b>").append(section.getName()).append("</b>");
            List<String[]> params = section.getRows();
            inputDesc.append(HtmlJPanel.getTableHeader(tableWidth, "FFFFFF"));
            for ( String[] row: params ) {
                 inputDesc.append(HtmlJPanel.COLSTARTFIX).append(row[0]).
                         append(HtmlJPanel.COLMID).append( row[1] ).append( HtmlJPanel.COLEND) ;
            }
            inputDesc.append(HtmlJPanel.getTableFooter());
            inputDesc.append(HtmlJPanel.COLEND);
        }

        inputDesc.append(HtmlJPanel.getTableFooter());

        return inputDesc.toString();

    }


	// ******************************************************************************************
	// ********************************* GETTERS / SETTERS **************************************
	// ******************************************************************************************

	/**
	 * @param paintChromosomeLimits the paintChromosomeLimits to set
	 */
	public void setPaintChromosomeLimits(boolean paintChromosomeLimits) {
		this.paintChromosomeLimits = paintChromosomeLimits;
	}


    public QChart findChartByName(String name) {
        for (QChart chart : charts) {
            if (chart.getName().equals(name)) {
                return chart;
            }
        }

        return null;
    }

	public String getBamFileName() {
		return bamFileName;
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

    public List<StatsKeeper.Section> getSummaryDataSections() {
        if (summaryStatsKeeper == null) {
            createSummaryStatsKeeper();
        }

        return summaryStatsKeeper.getSections();
    }

    private void createSummaryStatsKeeper() {
        summaryStatsKeeper = new StatsKeeper();
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

        globals.addRow("Mapped reads", sdf.formatInteger(numMappedReads)
                + " / " + sdf.formatPercentage(getPercentMappedReads()));

        globals.addRow("Unmapped reads",
                sdf.formatLong(numReads - numMappedReads) + " / "
                        + sdf.formatPercentage(100.0 - getPercentMappedReads()));

        globals.addRow("Paired reads",
                sdf.formatLong(numPairedReads) + " / "
                        + sdf.formatPercentage(percantagePairedReads) );
        if (numPairedReads > 0) {

            globals.addRow("Mapped reads, only first in pair",
                    sdf.formatInteger(numberOfMappedFirstOfPair) + " / " +
                    sdf.formatPercentage(percentageOfMappedFirstOfPair));

            globals.addRow("Mapped reads, only second in pair",
                    sdf.formatInteger(numberOfMappedSecondOfPair) + " / " +
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

        globals.addRow("Clipped reads", sdf.formatInteger(numClippedReads));
        globals.addRow("Duplication rate", sdf.formatPercentage(duplicationRate));


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
                        sdf.formatInteger(numMappedFirstOfPairInRegions) + " / " +
                                sdf.formatPercentage(percentageOfMappedFirstOfPairInRegions));

                globalsInRegions.addRow("Mapped reads, only second in pair",
                        sdf.formatInteger(numMappedSecondOfPairInRegions) + " / " +
                                sdf.formatPercentage(percentageOfMappedSecondOfPairInRegions));
                globalsInRegions.addRow("Mapped reads, both in pair",
                                   sdf.formatInteger(numPairedReadsInRegions - numSingletonsInRegions) + " / "
                                        + sdf.formatPercentage((getPercentageBothMatesPairedInRegions())));
                globalsInRegions.addRow("Mapped reads, singletons",
                        sdf.formatInteger(numSingletonsInRegions) + " / "
                                + sdf.formatPercentage(percentageSingletonsInRegions));


                globalsInRegions.addRow("Correct strand reads",
                        sdf.formatInteger(numCorrectStrandReads) + " / " +
                        sdf.formatPercentage(percentageCorrectStrandReads) );
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
		summaryStatsKeeper.addSection(coverageSection);

		StatsKeeper.Section mappingQualitySection = new StatsKeeper.Section("Mapping Quality" + postfix);
		mappingQualitySection.addRow("Mean Mapping Quality", sdf.formatDecimal(meanMappingQuality));
		summaryStatsKeeper.addSection(mappingQualitySection);

        int numIndels = numInsertions + numDeletions;
        if ( numIndels > 0) {
            StatsKeeper.Section indelsSection = new StatsKeeper.Section("Indels" + postfix);
            indelsSection.addRow("Total", sdf.formatInteger(numIndels));
            indelsSection.addRow("Insertions",sdf.formatDecimal(numInsertions) );
            indelsSection.addRow("Deletions",sdf.formatDecimal(numDeletions) );
            indelsSection.addRow("Percentage homopolymer",sdf.formatPercentage(homopolymerIndelFraction * 100.0) );

            summaryStatsKeeper.addSection(indelsSection);

        }


        if (meanInsertSize != 0)
        {
            StatsKeeper.Section insertSizeSection = new StatsKeeper.Section("Insert size" + postfix);
            insertSizeSection.addRow("Mean", sdf.formatDecimal(meanInsertSize));
            insertSizeSection.addRow("Median", sdf.formatDecimal(medianInsertSize));
            summaryStatsKeeper.addSection(insertSizeSection);
        }
    }

    public List<StatsKeeper.Section> getChromosomeSections() {
        if (chromosomeStatsKeeper == null) {
            createChromosomeStatsKeeper();
        }
        return chromosomeStatsKeeper.getSections();
    }

    private void createChromosomeStatsKeeper() {

        chromosomeStatsKeeper = new StatsKeeper();

        StatsKeeper.Section headerSection = new StatsKeeper.Section(Constants.CHROMOSOME_STATS_HEADER);
        String[] header = {
                "Name", "Length", "Mapped bases", "Mean coverage", "Standard deviation"
        };
        headerSection.addRow(header);
        chromosomeStatsKeeper.addSection(headerSection);

        StatsKeeper.Section dataSection = new StatsKeeper.Section(Constants.CHROMOSOME_STATS_DATA);
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(new File(chromosomeFilePath)));
            String strLine;
            // Iterate the file reading the lines
            while ((strLine = br.readLine()) != null) {
                // Test if the read is the header of the table or not

                if (!strLine.startsWith("#")) {
                    String[] tableValues = strLine.split("\t");
                    String[] coords = tableValues[1].split(":");
                    long len = Long.parseLong(coords[1]) - Long.parseLong(coords[0]) + 1;
                    tableValues[1] = Long.toString(len);
                    dataSection.addRow(tableValues);

                }
            }

            chromosomeStatsKeeper.addSection(dataSection);

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

    }

    public void setNamePostfix(String namePostfix) {
        this.namePostfix = namePostfix;
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

    public void setChromosomeFilePath(String chromosomeFilePath) {
        this.chromosomeFilePath = chromosomeFilePath;
    }


    public void setWarningInfo(Map<String, String> warnings) {
        this.warnings  = warnings;
    }

    public List<QChart> getCharts() {
        return charts;
    }

    public void setChartList(List<QChart> chartList) {
        charts = chartList;
    }


    public Properties generateBamQcProperties() {

        Properties prop = new Properties();

        prop.setProperty("refSize", referenceSize.toString());
        prop.setProperty("numContigs", contigsNumber.toString());
        prop.setProperty("numRegions", Integer.toString(numSelectedRegions));

        // reference
        /*if (reporter.getReferenceFileName() != null && !reporter.getReferenceFileName().isEmpty()) {
            prop.setProperty("referenceFileName", reporter.getReferenceFileName());
            prop.setProperty("aReferenceNumber", reporter.getaReferenceNumber().toString());
            prop.setProperty("aReferencePercent", reporter.getaReferencePercent().toString());
            prop.setProperty("cReferenceNumber", reporter.getcReferenceNumber().toString());
            prop.setProperty("cReferencePercent", reporter.getcReferencePercent().toString());
            prop.setProperty("tReferenceNumber", reporter.gettReferenceNumber().toString());
            prop.setProperty("tReferencePercent", reporter.gettReferencePercent().toString());
            prop.setProperty("gReferenceNumber", reporter.getgReferenceNumber().toString());
            prop.setProperty("gReferencePercent", reporter.getgReferencePercent().toString());
            prop.setProperty("nReferenceNumber", reporter.getnReferenceNumber().toString());
            prop.setProperty("nReferencePercent", reporter.getnReferencePercent().toString());
         }*/

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
        prop.setProperty("gcPercent", gcPercent.toString());

        return prop;
    }


    public double getSelectedRegionsPercentage() {
        return (numBasesInsideRegions / (double) referenceSize) * 100.0;
    }

}
