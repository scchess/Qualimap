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
import org.bioinfo.ngs.qc.qualimap.gui.panels.HtmlJPanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.utils.GraphUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.ui.RectangleInsets;

public class BamQCRegionReporter implements Serializable {


    public String getNamePostfix() {
        return namePostfix;
    }

    static public class InputDataSection {
        String sectionName;
        Map<String, String> data;

        InputDataSection(String sectionName) {
            this.sectionName = sectionName;
        }

        public String getName() {
            return sectionName;
        }

        public void setData(Map<String, String> paramsMap) {
            data = paramsMap;
        }

        public Map<String,String> getData() {
            return data;
        }
    }


    private boolean paintChromosomeLimits;

	/** Variable to contain the Charts generated in the class */
	private Map<String, JFreeChart> mapCharts;

    /** Contains buffered images of the charts */
    private Map<String, BufferedImage> imageMap;

	/** Variable that contains the input files names */
	private String bamFileName, referenceFileName;

	private Integer numWindows, numMappedReads;

	private Long referenceSize, contigsNumber, aNumber, cNumber, gNumber,
	tNumber, nNumber, numReads, numMappedBases, numSequencedBases,
	numAlignedBases, aReferenceNumber, cReferenceNumber, gReferenceNumber,
	tReferenceNumber, nReferenceNumber, numBasesInsideRegions;

	private Double aPercent, cPercent, gPercent, tPercent, nPercent,
	gcPercent, atPercent, percentMappedReads, meanMappingQuality,
	aReferencePercent, cReferencePercent, gReferencePercent,
	tReferencePercent, nReferencePercent, meanCoverage, stdCoverage;

    private long numInsideMappedReads, numOutsideMappedReads;
    private double percentageInsideMappedReads, percentageOutsideMappedReads;

    List<InputDataSection> inputDataSections;
    String namePostfix;
    int numSelectedRegions;

    public BamQCRegionReporter() {
        namePostfix = "";
        inputDataSections = new ArrayList<InputDataSection>();
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

        // regions related
        this.numSelectedRegions = bamStats.getNumSelectedRegions();
        this.numBasesInsideRegions = bamStats.getInRegionReferenceSize();
        this.numInsideMappedReads = bamStats.getNumberOfInsideMappedReads();
        this.percentageInsideMappedReads = bamStats.getPercentageOfInsideMappedReads();
        this.numOutsideMappedReads = bamStats.getNumberOfOutsideMappedReads();
        this.percentageOutsideMappedReads = bamStats.getPercentageOfOutsideMappedReads();

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

		// coverageData
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
			}
		}


        ///////////////// coverageData charts ///////////////

		// coverageData (and gc) across reference
		// coverageData
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
		gcContentChart.addSeries("mean GC content", new XYVector(Arrays.asList(0.0,lastReference), Arrays.asList(bamStats.getMeanGcRelativeContentPerWindow(),bamStats.getMeanGcRelativeContentPerWindow())),new Color(255,0,0,180),stroke,true);
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

		// coverageData histogram
		BamQCXYHistogramChart coverageHistogram = new BamQCXYHistogramChart("Coverage histogram", subTitle, "coverageData (bp)", "frequency");
		coverageHistogram.addHistogram("coverageData", bamStats.getCoverageHistogram(), Color.blue);
		coverageHistogram.setNumberOfBins(Math.min(50, (int) bamStats.getCoverageHistogram().getMaxValue()));
		coverageHistogram.setDomainAxisIntegerTicks(true);
	    coverageHistogram.render();
		// TODO: move this code to render() method?
        if (bamStats.getCoverageHistogram().getSize() > 0) {
            double lower = bamStats.getCoverageHistogram().get(0).getX();
            double upper = bamStats.getCoverageHistogram().get(bamStats.getCoverageHistogram().getSize()-1).getX();
            coverageHistogram.getChart().getXYPlot().getDomainAxis().setRange(lower,upper);
        }
        mapCharts.put(
				bamStats.getName() + "_coverage_histogram.png",
				coverageHistogram.getChart());

		// coverageData ranged histogram
		BamQCXYHistogramChart coverageRangedHistogram =
                new BamQCXYHistogramChart("Coverage histogram (0 - " + (int)maxValue + "x)",
                subTitle, "coverageData (bp)", "frequency");
		coverageRangedHistogram.addHistogram("coverageData", bamStats.getCoverageHistogram(), Color.blue);
		coverageRangedHistogram.setNumberOfBins(50);
		coverageRangedHistogram.zoom(maxValue);
		coverageRangedHistogram.setDomainAxisIntegerTicks(true);
		coverageRangedHistogram.setDomainAxisTickUnitSize(1.0);
        coverageRangedHistogram.render();
		mapCharts.put(
				bamStats.getName() + "_coverage_0to" + (int)maxValue + "_histogram.png",
				coverageRangedHistogram.getChart());


        // coverageData ranged histogram
		BamQCXYHistogramChart uniqueReadStartsHistogram =
                new BamQCXYHistogramChart("Unique reads per position",
                subTitle, "Reads per position", "Number of reads");
		uniqueReadStartsHistogram.addHistogram("coverageData", bamStats.getUniqueReadStartsHistogram(), Color.GREEN);
		uniqueReadStartsHistogram.setDomainAxisIntegerTicks(true);
		uniqueReadStartsHistogram.setDomainAxisTickUnitSize(1.0);
        uniqueReadStartsHistogram.render();
		mapCharts.put( bamStats.getName() + "_uniq_read_starts_histogram.png",
				uniqueReadStartsHistogram.getChart());



		//		  // coverageData cumulative histogram
		//		BamQCXYHistogramChart cumulativeCoverageHistogram = new BamQCXYHistogramChart("Coverage cumulative histogram", subTitle, "coverageData (bp)", "relative coverture of reference (%)");
		//		cumulativeCoverageHistogram.addHistogram("coverageData", bamStats.getCoverageHistogram(), Color.blue);
		//		cumulativeCoverageHistogram.setCumulative(true);
		//		cumulativeCoverageHistogram.setNumberOfBins(50);
		//		cumulativeCoverageHistogram.setDomainAxisIntegerTicks(true);
		//		cumulativeCoverageHistogram.render();
		//		mapCharts.put(
		//				bamStats.getName() + "_coverage_cumulative_histogram.png",
		//				cumulativeCoverageHistogram.getChart());
		//		
		//		  // coverageData cumulative ranged histogram
		//		BamQCXYHistogramChart cumulativeRangedCoverageHistogram = new BamQCXYHistogramChart("Coverage cumulative histogram (0 - " + (int)maxValue + "x)", subTitle, "coverageData (bp)", "relative coverture of reference (%)");
		//		cumulativeRangedCoverageHistogram.addHistogram("coverageData", bamStats.getCoverageHistogram(), Color.blue);
		//		cumulativeRangedCoverageHistogram.setCumulative(true);
		//		cumulativeRangedCoverageHistogram.setNumberOfBins(50);		
		//		cumulativeRangedCoverageHistogram.zoom(maxValue);
		//		cumulativeRangedCoverageHistogram.setDomainAxisIntegerTicks(true);
		//		cumulativeRangedCoverageHistogram.render();
		//		mapCharts.put(
		//				bamStats.getName() + "_coverage_0to" + (int)maxValue + "_cumulative_histogram.png",
		//				cumulativeRangedCoverageHistogram.getChart());

		// coverageData quota
		BamQCChart coverageQuota = new BamQCChart("Coverage quota", subTitle,
                "coverageData (bp)", "relative coverture of reference (%)");
		coverageQuota.setPercentageChart(true);
		coverageQuota.addBarRenderedSeries("Coverture", bamStats.getCoverageQuotes(), new Color(255,20,20,150));
		coverageQuota.setDomainAxisIntegerTicks(true);
        coverageQuota.setDomainAxisTickUnitSize(1.0);
        coverageQuota.render();
		mapCharts.put(bamStats.getName() + "_coverage_quotes.png", coverageQuota.getChart());


        BamQCChart gcContentHistChart = new BamQCChart("GC Content Historgram", subTitle,
                "GC content %", "Fraction of reads");
	    gcContentHistChart.setPercentageChart(true);
		gcContentHistChart.addSeries("Sample", bamStats.getGcContentHistogram(), new Color(20, 10, 255, 255));

        if (bamStats.availableGenomeGcContentHistogram()) {
            gcContentHistChart.addSeries("Genome", bamStats.getGenomeGcContentHistogram(), new Color(255, 10, 20, 255));
        }
        gcContentHistChart.setDomainAxisIntegerTicks(true);
        gcContentHistChart.setAdjustDomainAxisLimits(false);
        gcContentHistChart.setPercentageChart(false);
        gcContentHistChart.render();
		mapCharts.put(bamStats.getName() + "_gc_content_per_window.png", gcContentHistChart.getChart());


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
		BamQCChart mappingQuality = new BamQCChart("Mapping quality across reference",
                subTitle, "absolute position (bp)", "mapping quality");
		mappingQuality.addSeries("mapping quality",new XYVector(windowReferences, bamStats.getMappingQualityAcrossReference()), new Color(250,50,50,150));
        if(paintChromosomeLimits && locator!=null) {
                    mappingQuality.addSeries("chromosomes",chromosomeBytedLimits,chromosomeColor,stroke,false);
        }
		mappingQuality.render();
		mappingQuality.getChart().getXYPlot().getRangeAxis().setRange(0,255);
		mapCharts.put(
				bamStats.getName() + "_mapping_quality_across_reference.png",
				mappingQuality.getChart());

		// mapping quality histogram
		BamQCXYHistogramChart mappingQualityHistogram =
                new BamQCXYHistogramChart("Mapping quality histogram",
                        subTitle, "mapping quality", "frequency");
		mappingQualityHistogram.addHistogram("mapping quality", bamStats.getMappingQualityHistogram(), Color.blue);
		mappingQualityHistogram.setNumberOfBins(50);		
		mappingQualityHistogram.render();
		mapCharts.put(
				bamStats.getName() + "_mapping_quality_histogram.png",
				mappingQualityHistogram.getChart());
		
		if(isPairedData){
			// insert size across reference
			BamQCChart insertSize = new BamQCChart("Insert size across reference",
                    subTitle, "absolute position (bp)", "insert size (bp)");
			insertSize.addSeries("insert size",new XYVector(windowReferences, bamStats.getInsertSizeAcrossReference()), new Color(15,170,90,150));
            if(paintChromosomeLimits && locator!=null) {
                insertSize.addSeries("chromosomes",chromosomeBytedLimits,chromosomeColor,stroke,false);
            }
            insertSize.render();
			mapCharts.put(bamStats.getName() + "_insert_size_across_reference.png", insertSize.getChart());
	
			// mapping quality histogram
			BamQCXYHistogramChart insertSizeHistogram =
                    new BamQCXYHistogramChart("Insert size histogram", subTitle, "insert size (bp)", "frequency");
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


    public void addInputDataSection(String name, Map<String,String> paramsMap) {

        InputDataSection section = new InputDataSection(name);
        section.setData(paramsMap);

        inputDataSections.add(section);
    }

    public String getInputDescription(int tableWidth) {


        if (inputDataSections.isEmpty()) {
            return "No input description is available";
        }

        StringBuilder inputDesc = new StringBuilder();

        inputDesc.append("<p align=center><a name=\"input\"> <b>Input data & parameters</b></p>" + HtmlJPanel.BR);
        inputDesc.append(HtmlJPanel.getTableHeader(tableWidth, "EEEEEE"));


        for (InputDataSection section : inputDataSections) {
            inputDesc.append(HtmlJPanel.COLSTART + "<b>" + section.getName() +  "</b>");
            Map<String,String> paramsMap = section.getData();
            inputDesc.append(HtmlJPanel.getTableHeader(tableWidth, "FFFFFF"));
            for ( Map.Entry<String,String> entry: paramsMap.entrySet() ) {
                 inputDesc.append(HtmlJPanel.COLSTARTFIX + entry.getKey() + HtmlJPanel.COLMID
                    + entry.getValue() + HtmlJPanel.COLEND);
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

    public Object getChart(String name) {
        if (mapCharts != null && mapCharts.containsKey(name)) {
            return mapCharts.get(name);
        } else if (imageMap != null && imageMap.containsKey(name)) {
            return imageMap.get(name);
        } else {
            return null;
        }
    }

	//TODO: getMapCharts() and get getImageMap() should be somehow replaced by generic functions
    // For example it is possible to introduce generic class ChartImage and hide impl details
    public Map<String, JFreeChart> getMapCharts() {
		return mapCharts;
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
		return referenceSize;
	}

	public void setBasesNumber(Long basesNumber) {
		this.referenceSize = basesNumber;
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

    public List<InputDataSection> getInputDataSections() {
        return inputDataSections;
    }

    public void setNamePostfix(String namePostfix) {
        this.namePostfix = namePostfix;
    }

    public int getNumSelectedRegions() {
        return numSelectedRegions;
    }

    public long getInRegionsReferenceSize() {
        return numBasesInsideRegions;
    }

    public long getNumInsideMappedReads() {
        return numInsideMappedReads;
    }

    public double getPercentageInsideMappedReads() {
        return percentageInsideMappedReads;
    }

    public long getNumOutsideMappedReads() {
        return numOutsideMappedReads;
    }

    public double getPercentageOutsideMappedReads() {
        return percentageOutsideMappedReads;
    }


}
