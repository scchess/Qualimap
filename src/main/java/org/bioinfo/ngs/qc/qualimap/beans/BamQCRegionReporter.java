package org.bioinfo.ngs.qc.qualimap.beans;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.ngs.qc.qualimap.utils.GraphUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.ui.RectangleInsets;

public class BamQCRegionReporter implements Serializable {
	
	
	private boolean paintChromosomeLimits;

	/** Variable to contain the Charts generated in the class */
	private Map<String, JFreeChart> mapCharts;

    /** Contains buffered images of the charts */
    private Map<String, BufferedImage> imageMap;

	/** Variable that contains the input files names */
	private String bamFileName, referenceFileName;

	private Integer numWindows, numMappedReads;

	private Long basesNumber, contigsNumber, aNumber, cNumber, gNumber,
	tNumber, nNumber, numReads, numMappedBases, numSequencedBases,
	numAlignedBases, aReferenceNumber, cReferenceNumber, gReferenceNumber,
	tReferenceNumber, nReferenceNumber;

	private Double aPercent, cPercent, gPercent, tPercent, nPercent,
	gcPercent, atPercent, percentMappedReads, meanMappingQuality,
	aReferencePercent, cReferencePercent, gReferencePercent,
	tReferencePercent, nReferencePercent, meanCoverage, stdCoverage;


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

		// coverage
		report.println(">>>>>>> Coverage");
		report.println("");
		report.println("     mean coverage = " + formatDecimal(bamStats.getMeanCoverage()) + "X");
		report.println("     std coverage = " + formatDecimal(bamStats.getStdCoverage()) + "X");
		report.println("");
		for(int i=0; i<bamStats.getCoverageQuotes().getSize(); i++){
			report.println("     There is a " + StringUtils.decimalFormat(bamStats.getCoverageQuotes().get(i).getY(),"#0.##") + "% of reference with a coverage >= " + (1+(int)bamStats.getCoverageQuotes().get(i).getX()) + "X");
		}
		report.println("");
		report.println("");

		report.close();
	}

    private XYToolTipGenerator createTooltipGenerator(List<Double> windowReferences, GenomeLocator locator ) {

        List<String> toolTips = new ArrayList<String>();

        for (double pos : windowReferences) {
            ContigRecord rec = locator.getContigCoordinates((int) pos);
            long start = rec.getStart();
            long relativePos = (long)pos - start + 1;
            toolTips.add("Chromosome: " + rec.getName() + ", relative position: " + relativePos);
        }

        CustomXYToolTipGenerator generator = new CustomXYToolTipGenerator();
        generator.addToolTipSeries(toolTips);

        return generator;

    }

    public void saveCharts(BamStats bamStats, String outdir, GenomeLocator locator, boolean isPairedData) throws IOException{

		if (mapCharts == null) {
            computeChartsBuffers(bamStats, locator, isPairedData);
        }

        for (Map.Entry<String, JFreeChart> entry : mapCharts.entrySet()) {
            String fileName = entry.getKey();
            JFreeChart chart = entry.getValue();
            GraphUtils.saveChart(chart, outdir + "/" + fileName, 800, 600);
        }

	}

	/**
	 * Function to load the data variables obtained from the input file/s
	 * @param bamStats data read in the input file
	 * @throws IOException some errors that can happen
	 */
	public void loadReportData(BamStats bamStats) throws IOException{
		this.bamFileName = bamStats.getSourceFile();
		this.basesNumber = bamStats.getReferenceSize();
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
			this.atPercent = bamStats.getMeanAtRelativeContentPerWindowInReference();
		}

		// globals
		this.numWindows = bamStats.getNumberOfWindows();
		this.numReads = bamStats.getNumberOfReads();
		this.numMappedReads = bamStats.getNumberOfMappedReads();
		this.percentMappedReads = bamStats.getPercentageOfMappedReads();
		this.numMappedBases = bamStats.getNumberOfMappedBases();
		this.numSequencedBases = bamStats.getNumberOfSequencedBases();
		this.numAlignedBases = bamStats.getNumberOfAlignedBases();

		// mapping quality		
		this.meanMappingQuality = bamStats.getMeanMappingQualityPerWindow();

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
		//TODO: calculate mean at relative content?
		//this.atPercent = bamStats.getMeanAtRelativeContent();

		// coverage
		this.meanCoverage = bamStats.getMeanCoverage();
		this.stdCoverage = bamStats.getStdCoverage();
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

		if(mapCharts == null){
			mapCharts = new HashMap<String, JFreeChart>();
		}

		// some variables		
		double maxValue = 50;
		String subTitle = new File(bamStats.getSourceFile()).getName();

		// compute window centers
		List<Double> windowReferences = new ArrayList<Double>(bamStats.getNumberOfWindows());

        for(int i=0; i<bamStats.getNumberOfWindows(); i++){
			windowReferences.add((double)(bamStats.getWindowStart(i)+bamStats.getWindowEnd(i))/2.0);
        }
		double lastReference = windowReferences.get(windowReferences.size()-1);

		// max coverage+std
		double maxCoverage = 0;
		for(int i=0; i<bamStats.getCoverageAcrossReference().size(); i++){
			if (bamStats.getCoverageAcrossReference().get(i)+bamStats.getStdCoverageAcrossReference().get(i)>maxCoverage) {
                maxCoverage = bamStats.getCoverageAcrossReference().get(i)+bamStats.getStdCoverageAcrossReference().get(i);
            }
		}

        // compute chromosome limits
		Color chromosomeColor = new Color(40,40,40,150);
		XYVector chromosomeCoverageLimits = null;
		XYVector chromosomePercentageLimits = null;
		XYVector chromosomeBytedLimits = null;
		if(paintChromosomeLimits && locator!=null){
			int numberOfChromosomes = locator.getContigs().size();
			chromosomeCoverageLimits = new XYVector();
			chromosomePercentageLimits = new XYVector();
			chromosomeBytedLimits = new XYVector();
			for(int i=0; i<numberOfChromosomes; i++){
				// coverage
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
			}
		}


		///////////////// coverage charts /////////////// 

		// coverage (and gc) across reference
		// coverage
		BamQCChart coverageChart = new BamQCChart("Coverage across reference", subTitle, "absolute position (bp)", "Coverage");
        XYToolTipGenerator toolTipGenerator = createTooltipGenerator(windowReferences, locator);
        coverageChart.setToolTipGenerator(toolTipGenerator);
        coverageChart.addIntervalRenderedSeries("Coverage",new XYVector(windowReferences, bamStats.getCoverageAcrossReference(), bamStats.getStdCoverageAcrossReference()), new Color(250,50,50,150), new Color(50,50,250), 0.2f);

        if(paintChromosomeLimits && locator!=null) {
            coverageChart.addSeries("chromosomes",chromosomeCoverageLimits,chromosomeColor,stroke,false);
        }
        coverageChart.render();
		coverageChart.getChart().getXYPlot().getRangeAxis().setLowerBound(0);
        // gc content
		BamQCChart gcContentChart = new BamQCChart("GC/AT relative content", subTitle, "absolute position (bp)", "%");
		gcContentChart.setToolTipGenerator(toolTipGenerator);
        gcContentChart.setPercentageChart(true);
		gcContentChart.addSeries("GC content", new XYVector(windowReferences,bamStats.getGcRelativeContentAcrossReference()), new Color(50,50,50,150));
		gcContentChart.addSeries("mean GC content", new XYVector(Arrays.asList(0.0,lastReference), Arrays.asList(bamStats.getMeanGcRelativeContentPerWindow(),bamStats.getMeanGcRelativeContentPerWindow())),new Color(50,50,50,150),stroke,true);
		if(paintChromosomeLimits && locator!=null) {
            gcContentChart.addSeries("chromosomes",chromosomePercentageLimits,chromosomeColor,stroke,false);
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
        mapCharts.put(bamStats.getName() + "_coverage_across_reference.png",combinedChart);

		// coverage histogram
		BamQCXYHistogramChart coverageHistogram = new BamQCXYHistogramChart("Coverage histogram", subTitle, "coverage (bp)", "frequency");
		coverageHistogram.addHistogram("coverage", bamStats.getCoverageHistogram(), Color.blue);
		coverageHistogram.setNumberOfBins(Math.min(50,(int)bamStats.getCoverageHistogram().getMaxValue()));
		coverageHistogram.setDomainAxisIntegerTicks(true);
		coverageHistogram.render();
		coverageHistogram.getChart().getXYPlot().getDomainAxis().setRange(bamStats.getCoverageHistogram().get(0).getX(),bamStats.getCoverageHistogram().get(bamStats.getCoverageHistogram().getSize()-1).getX());
		mapCharts.put(
				bamStats.getName() + "_coverage_histogram.png",
				coverageHistogram.getChart());

		// coverage ranged histogram
		BamQCXYHistogramChart coverageRangedHistogram = new BamQCXYHistogramChart("Coverage histogram (0 - " + (int)maxValue + "x)", subTitle, "coverage (bp)", "frequency");
		coverageRangedHistogram.addHistogram("coverage", bamStats.getCoverageHistogram(), Color.blue);
		coverageRangedHistogram.setNumberOfBins(50);
		coverageRangedHistogram.zoom(maxValue);
		coverageRangedHistogram.setDomainAxisIntegerTicks(true);
		coverageRangedHistogram.render();
		mapCharts.put(
				bamStats.getName() + "_coverage_0to" + (int)maxValue + "_histogram.png",
				coverageRangedHistogram.getChart());

		//		  // coverage cumulative histogram
		//		BamQCXYHistogramChart cumulativeCoverageHistogram = new BamQCXYHistogramChart("Coverage cumulative histogram", subTitle, "coverage (bp)", "relative coverture of reference (%)");
		//		cumulativeCoverageHistogram.addHistogram("coverage", bamStats.getCoverageHistogram(), Color.blue);
		//		cumulativeCoverageHistogram.setCumulative(true);
		//		cumulativeCoverageHistogram.setNumberOfBins(50);
		//		cumulativeCoverageHistogram.setDomainAxisIntegerTicks(true);
		//		cumulativeCoverageHistogram.render();
		//		mapCharts.put(
		//				bamStats.getName() + "_coverage_cumulative_histogram.png",
		//				cumulativeCoverageHistogram.getChart());
		//		
		//		  // coverage cumulative ranged histogram
		//		BamQCXYHistogramChart cumulativeRangedCoverageHistogram = new BamQCXYHistogramChart("Coverage cumulative histogram (0 - " + (int)maxValue + "x)", subTitle, "coverage (bp)", "relative coverture of reference (%)");
		//		cumulativeRangedCoverageHistogram.addHistogram("coverage", bamStats.getCoverageHistogram(), Color.blue);
		//		cumulativeRangedCoverageHistogram.setCumulative(true);
		//		cumulativeRangedCoverageHistogram.setNumberOfBins(50);		
		//		cumulativeRangedCoverageHistogram.zoom(maxValue);
		//		cumulativeRangedCoverageHistogram.setDomainAxisIntegerTicks(true);
		//		cumulativeRangedCoverageHistogram.render();
		//		mapCharts.put(
		//				bamStats.getName() + "_coverage_0to" + (int)maxValue + "_cumulative_histogram.png",
		//				cumulativeRangedCoverageHistogram.getChart());

		// coverage quota
		BamQCChart coverageQuota = new BamQCChart("Coverage quota", subTitle, "coverage (bp)", "relative coverture of reference (%)");
		coverageQuota.setPercentageChart(true);
		coverageQuota.addBarRenderedSeries("Coverture", bamStats.getCoverageQuotes(), new Color(255,20,20,150));
		coverageQuota.setDomainAxisIntegerTicks(true);
		coverageQuota.render();
		mapCharts.put(bamStats.getName() + "_coverage_quotes.png", coverageQuota.getChart());


		///////////////// actg content charts ///////////////

		//		  // actg content across reference
		//		BamQCChart actgContentChart = new BamQCChart("Nucleotide relative content", subTitle, "absolute position (bp)", "relative content (%)");
		//		actgContentChart.setPercentageChart(true);
		//		actgContentChart.addSeries("A", new XYVector(windowReferences,bamStats.getaRelativeContentAcrossReference()), new Color(50,200,50,200));
		//		//if(bamQC.isReferenceAvailable()) actgContentChart.addSeries("A ref", new XYVector(windowReferences,bamStats.getaRelativeContentInReference()), new Color(50,200,50,200),stroke);
		//		actgContentChart.addSeries("T", new XYVector(windowReferences,bamStats.gettRelativeContentAcrossReference()), new Color(200,50,50,200));
		//		actgContentChart.addSeries("C", new XYVector(windowReferences,bamStats.getcRelativeContentAcrossReference()), new Color(50,50,200,200));
		//		actgContentChart.addSeries("G", new XYVector(windowReferences,bamStats.getgRelativeContentAcrossReference()), new Color(50,50,50,200));		
		//		actgContentChart.addSeries("N", new XYVector(windowReferences,bamStats.getnRelativeContentAcrossReference()), new Color(150,150,150,150));
		//		if(paintChromosomeLimits && locator!=null) actgContentChart.addSeries("chromosomes",chromosomePercentageLimits,chromosomeColor,stroke);
		//		actgContentChart.render();
		//		mapCharts.put(
		//				bamStats.getName() + "_actg_across_reference.png",
		//				actgContentChart.getChart());

		//  		  // GC/AT content across reference
		//		BamQCChart gcContentChart = new BamQCChart("GC/AT relative content", subTitle, "absolute position (bp)", "relative content (%)");
		//		gcContentChart.setPercentageChart(true);
		//		if(bamStats.isReferenceAvailable()) {
		//			gcContentChart.addIntervalRenderedSeries("CG", new XYVector(windowReferences,bamStats.getGcRelativeContentAcrossReference(),bamStats.getGcRelativeContentInReference(),false), new Color(250,50,50,150), new Color(250,50,50,250),0.4f);
		//			gcContentChart.addIntervalRenderedSeries("AT", new XYVector(windowReferences,bamStats.getAtRelativeContentAcrossReference(),bamStats.getAtRelativeContentInReference(),false), new Color(50,50,250,150), new Color(50,50,250,250),0.4f);
		//		} else {
		//			gcContentChart.addSeries("CG", new XYVector(windowReferences,bamStats.getGcRelativeContentAcrossReference()), new Color(250,50,50,150));
		//			gcContentChart.addSeries("AT", new XYVector(windowReferences,bamStats.getAtRelativeContentAcrossReference()), new Color(50,50,250,150));
		//		}
		//		gcContentChart.addSeries("mean GC", new XYVector(Arrays.asList(0.0,lastReference), Arrays.asList(bamStats.getMeanGcRelativeContentPerWindow(),bamStats.getMeanGcRelativeContentPerWindow())),new Color(150,50,50,150),stroke);		
		//		gcContentChart.addSeries("mean AT", new XYVector(Arrays.asList(0.0,lastReference), Arrays.asList(bamStats.getMeanAtRelativeContentPerWindow(),bamStats.getMeanAtRelativeContentPerWindow())),new Color(50,50,150,150),stroke);
		//		if(paintChromosomeLimits && locator!=null) gcContentChart.addSeries("chromosomes",chromosomePercentageLimits,chromosomeColor,stroke);
		//		gcContentChart.render();
		//		mapCharts.put(
		//				bamStats.getName() + "_gc_across_reference.png",
		//				gcContentChart.getChart());


		///////////////// mapping quality charts ///////////////

		// mapping quality across reference
		BamQCChart mappingQuality = new BamQCChart("Mapping quality across reference", subTitle, "absolute position (bp)", "mapping quality");
		mappingQuality.addSeries("mapping quality",new XYVector(windowReferences, bamStats.getMappingQualityAcrossReference()), new Color(250,50,50,150));
		mappingQuality.render();
		mappingQuality.getChart().getXYPlot().getRangeAxis().setRange(0,255);
		if(paintChromosomeLimits && locator!=null) mappingQuality.addSeries("chromosomes",chromosomeBytedLimits,chromosomeColor,stroke,false);
		mapCharts.put(
				bamStats.getName() + "_mapping_quality_across_reference.png",
				mappingQuality.getChart());

		// mapping quality histogram
		BamQCXYHistogramChart mappingQualityHistogram = new BamQCXYHistogramChart("Mapping quality histogram", subTitle, "mapping quality", "frequency");
		mappingQualityHistogram.addHistogram("mapping quality", bamStats.getMappingQualityHistogram(), Color.blue);
		mappingQualityHistogram.setNumberOfBins(50);		
		mappingQualityHistogram.render();
		mapCharts.put(
				bamStats.getName() + "_mapping_quality_histogram.png",
				mappingQualityHistogram.getChart());
		
		if(isPairedData){
			// insert size across reference
			BamQCChart insertSize = new BamQCChart("Insert size across reference", subTitle, "absolute position (bp)", "insert size (bp)");
			insertSize.addSeries("insert size",new XYVector(windowReferences, bamStats.getInsertSizeAcrossReference()), new Color(15,170,90,150));
			insertSize.render();		
			if(paintChromosomeLimits && locator!=null) insertSize.addSeries("chromosomes",chromosomeBytedLimits,chromosomeColor,stroke,false);
			mapCharts.put(bamStats.getName() + "_insert_size_across_reference.png", insertSize.getChart());
	
			// mapping quality histogram
			BamQCXYHistogramChart insertSizeHistogram = new BamQCXYHistogramChart("Insert size histogram", subTitle, "insert size (bp)", "frequency");
			insertSizeHistogram.addHistogram("insert size", bamStats.getInsertSizeHistogram(), new Color(15,170,90,150));
			insertSizeHistogram.setNumberOfBins(50);		
			insertSizeHistogram.render();
			mapCharts.put(bamStats.getName() + "_insert_size_histogram.png", insertSizeHistogram.getChart());
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


	// ******************************************************************************************
	// ********************************* GETTERS / SETTERS **************************************
	// ******************************************************************************************

	/**
	 * @param paintChromosomeLimits the paintChromosomeLimits to set
	 */
	public void setPaintChromosomeLimits(boolean paintChromosomeLimits) {
		this.paintChromosomeLimits = paintChromosomeLimits;
	}

	public Map<String, JFreeChart> getMapCharts() {
		return mapCharts;
	}

	public void setMapCharts(Map<String, JFreeChart> mapCharts) {
		this.mapCharts = mapCharts;
	}

    public Map<String, BufferedImage> getImageMap() {
            return imageMap;
        }

    public void setImageMap(Map<String, BufferedImage> imageMap) {
        this.imageMap = imageMap;
    }


	public String getBamFileName() {
		return bamFileName;
	}

	public void setBamFileName(String bamFileName) {
		this.bamFileName = bamFileName;
	}

	public String getReferenceFileName() {
		return referenceFileName;
	}

	public void setReferenceFileName(String referenceFileName) {
		this.referenceFileName = referenceFileName;
	}

	public Integer getNumWindows() {
		return numWindows;
	}

	public void setNumWindows(Integer numWindows) {
		this.numWindows = numWindows;
	}

	public Long getBasesNumber() {
		return basesNumber;
	}

	public void setBasesNumber(Long basesNumber) {
		this.basesNumber = basesNumber;
	}

	public Long getContigsNumber() {
		return contigsNumber;
	}

	public void setContigsNumber(Long contigsNumber) {
		this.contigsNumber = contigsNumber;
	}

	public Long getaNumber() {
		return aNumber;
	}

	public void setaNumber(Long aNumber) {
		this.aNumber = aNumber;
	}

	public Long getcNumber() {
		return cNumber;
	}

	public void setcNumber(Long cNumber) {
		this.cNumber = cNumber;
	}

	public Long getgNumber() {
		return gNumber;
	}

	public void setgNumber(Long gNumber) {
		this.gNumber = gNumber;
	}

	public Long gettNumber() {
		return tNumber;
	}

	public void settNumber(Long tNumber) {
		this.tNumber = tNumber;
	}

	public Long getnNumber() {
		return nNumber;
	}

	public void setnNumber(Long nNumber) {
		this.nNumber = nNumber;
	}

	public Long getNumReads() {
		return numReads;
	}

	public void setNumReads(Long numReads) {
		this.numReads = numReads;
	}

	public Integer getNumMappedReads() {
		return numMappedReads;
	}

	public void setNumMappedReads(Integer numMappedReads) {
		this.numMappedReads = numMappedReads;
	}

	public Long getNumMappedBases() {
		return numMappedBases;
	}

	public void setNumMappedBases(Long numMappedBases) {
		this.numMappedBases = numMappedBases;
	}

	public Double getaPercent() {
		return aPercent;
	}

	public void setaPercent(Double aPercent) {
		this.aPercent = aPercent;
	}

	public Double getcPercent() {
		return cPercent;
	}

	public void setcPercent(Double cPercent) {
		this.cPercent = cPercent;
	}

	public Double getgPercent() {
		return gPercent;
	}

	public void setgPercent(Double gPercent) {
		this.gPercent = gPercent;
	}

	public Double gettPercent() {
		return tPercent;
	}

	public void settPercent(Double tPercent) {
		this.tPercent = tPercent;
	}

	public Double getnPercent() {
		return nPercent;
	}

	public void setnPercent(Double nPercent) {
		this.nPercent = nPercent;
	}

	public Double getGcPercent() {
		return gcPercent;
	}

	public void setGcPercent(Double gcPercent) {
		this.gcPercent = gcPercent;
	}

	public Double getAtPercent() {
		return atPercent;
	}

	public void setAtPercent(Double atPercent) {
		this.atPercent = atPercent;
	}

	public Double getPercentMappedReads() {
		return percentMappedReads;
	}

	public void setPercentMappedReads(Double percentMappedReads) {
		this.percentMappedReads = percentMappedReads;
	}

	public Long getNumSequencedBases() {
		return numSequencedBases;
	}

	public void setNumSequencedBases(Long numSequencedBases) {
		this.numSequencedBases = numSequencedBases;
	}

	public Long getNumAlignedBases() {
		return numAlignedBases;
	}

	public void setNumAlignedBases(Long numAlignedBases) {
		this.numAlignedBases = numAlignedBases;
	}

	public Long getaReferenceNumber() {
		return aReferenceNumber;
	}

	public void setaReferenceNumber(Long aReferenceNumber) {
		this.aReferenceNumber = aReferenceNumber;
	}

	public Long getcReferenceNumber() {
		return cReferenceNumber;
	}

	public void setcReferenceNumber(Long cReferenceNumber) {
		this.cReferenceNumber = cReferenceNumber;
	}

	public Long getgReferenceNumber() {
		return gReferenceNumber;
	}

	public void setgReferenceNumber(Long gReferenceNumber) {
		this.gReferenceNumber = gReferenceNumber;
	}

	public Long gettReferenceNumber() {
		return tReferenceNumber;
	}

	public void settReferenceNumber(Long tReferenceNumber) {
		this.tReferenceNumber = tReferenceNumber;
	}

	public Long getnReferenceNumber() {
		return nReferenceNumber;
	}

	public void setnReferenceNumber(Long nReferenceNumber) {
		this.nReferenceNumber = nReferenceNumber;
	}

	public Double getMeanMappingQuality() {
		return meanMappingQuality;
	}

	public void setMeanMappingQuality(Double meanMappingQuality) {
		this.meanMappingQuality = meanMappingQuality;
	}

	public Double getaReferencePercent() {
		return aReferencePercent;
	}

	public void setaReferencePercent(Double aReferencePercent) {
		this.aReferencePercent = aReferencePercent;
	}

	public Double getcReferencePercent() {
		return cReferencePercent;
	}

	public void setcReferencePercent(Double cReferencePercent) {
		this.cReferencePercent = cReferencePercent;
	}

	public Double getgReferencePercent() {
		return gReferencePercent;
	}

	public void setgReferencePercent(Double gReferencePercent) {
		this.gReferencePercent = gReferencePercent;
	}

	public Double gettReferencePercent() {
		return tReferencePercent;
	}

	public void settReferencePercent(Double tReferencePercent) {
		this.tReferencePercent = tReferencePercent;
	}

	public Double getnReferencePercent() {
		return nReferencePercent;
	}

	public void setnReferencePercent(Double nReferencePercent) {
		this.nReferencePercent = nReferencePercent;
	}

	public Double getMeanCoverage() {
		return meanCoverage;
	}

	public void setMeanCoverage(Double meanCoverage) {
		this.meanCoverage = meanCoverage;
	}

	public Double getStdCoverage() {
		return stdCoverage;
	}

	public void setStdCoverage(Double stdCoverage) {
		this.stdCoverage = stdCoverage;
	}
}
