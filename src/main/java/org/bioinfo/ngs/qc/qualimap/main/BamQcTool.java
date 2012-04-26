package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.threads.BamAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.BamStatsAnalysis;

import java.io.File;

public class BamQcTool extends NgsSmartTool{

    private String bamFile;
	private String gffFile;
	private boolean selectedRegionsAvailable;
	private int numberOfWindows;
	private int numThreads;
    private int bunchSize;
    private boolean paintChromosomeLimits;
	private boolean computeOutsideStats;

    static String OPTION_NAME_BAM_FILE = "bam";
    static String OPTION_NAME_GFF_FILE = "gff";


    public BamQcTool(){
        super(Constants.TOOL_NAME_BAMQC);
        numThreads = Runtime.getRuntime().availableProcessors();
        paintChromosomeLimits = true;
    }

	@Override
	protected void initOptions() {

        Option opt = new Option(OPTION_NAME_BAM_FILE, true, "input mapping file");
        opt.setRequired(true);
        options.addOption( opt );

        options.addOption( OPTION_NAME_GFF_FILE,  true, "region file (gff format)");
        options.addOption("nw", true, "number of windows (advanced)");
        options.addOption("nt", true, "number of threads (advanced)");
        options.addOption("nr", true, "number of reads in the bunch (advanced)");

		options.addOption("c", "paint-chromosome-limits", false, "paint chromosome limits inside charts");
		options.addOption("os", "outside-stats", false, "compute region outside stats (only with -gff option)");

	}

	@Override
	protected void checkOptions() throws ParseException {
		
		// input

        bamFile = commandLine.getOptionValue(OPTION_NAME_BAM_FILE);
		if(!exists(bamFile)) throw new ParseException("input mapping file not found");

		// gff
		if(commandLine.hasOption(OPTION_NAME_GFF_FILE)) {
			gffFile = commandLine.getOptionValue(OPTION_NAME_GFF_FILE);
			if(!exists(gffFile)) {
                throw new ParseException("input region gff file not found");
            }
			selectedRegionsAvailable = true;
			if(commandLine.hasOption("outside-stats")) {
				computeOutsideStats = true;					
			}
		}


		numberOfWindows =  commandLine.hasOption("nw") ?
			Integer.parseInt(commandLine.getOptionValue("nw")) : Constants.DEFAULT_NUMBER_OF_WINDOWS;

        numThreads = commandLine.hasOption("nt") ?
                Integer.parseInt(commandLine.getOptionValue("nt")) : Runtime.getRuntime().availableProcessors();

        bunchSize = commandLine.hasOption("bs") ?
                Integer.parseInt(commandLine.getOptionValue("bs")) : 1000;


		// reporting
		if(commandLine.hasOption("paint_chromosome_limits")) {
			paintChromosomeLimits = true;
		}

	}

    @Override
    protected void initOutputDir() {
        if (outdir.isEmpty()) {
            outdir = FilenameUtils.removeExtension(new File(bamFile).getAbsolutePath()) + "_stats";
        }
        super.initOutputDir();
    }

	@Override
	protected void execute() throws Exception {

        long memAvailable =  Runtime.getRuntime().totalMemory() / 1000000;
		long memMax = Runtime.getRuntime().maxMemory() / 1000000;
        System.out.println("Available memory (Mb): " +  memAvailable);
        System.out.println("Max memory (Mb): " +  memMax);

		// check outdir
		initOutputDir();

		// init bamqc
		BamStatsAnalysis bamQC = new BamStatsAnalysis(bamFile);

        if(selectedRegionsAvailable){
			bamQC.setSelectedRegions(gffFile);
			bamQC.setComputeOutsideStats(computeOutsideStats);
		}

		// chromosome stats
		bamQC.setComputeChromosomeStats(true);

		// reporting
		bamQC.activeReporting(outdir);
		//if(saveCoverage) bamQC.ctiveCoverageReporting();

		logger.println("Starting bam qc....");

		// number of windows
		bamQC.setNumberOfWindows(numberOfWindows);
        bamQC.setNumberOfThreads(numThreads);
        bamQC.setNumberOfReadsInBunch(bunchSize);

		// run evaluation
		bamQC.run();

		logger.println("end of bam qc");

		logger.println("Computing report...");

		BamQCRegionReporter reporter = new BamQCRegionReporter();
		reporter.setPaintChromosomeLimits(paintChromosomeLimits);
        reporter.setChromosomeFilePath(outdir + File.separator + Constants.NAME_OF_FILE_CHROMOSOMES);
        BamAnalysisThread.prepareInputDescription(reporter, bamQC, paintChromosomeLimits);

		// save stats

        reporter.writeReport(bamQC.getBamStats(),outdir);

        TabPropertiesVO tabProperties = new TabPropertiesVO();
        tabProperties.setTypeAnalysis(Constants.TYPE_BAM_ANALYSIS_DNA);
        tabProperties.setBamStats(bamQC.getBamStats());
        tabProperties.setPairedData(bamQC.isPairedData());
        tabProperties.setBamStats(bamQC.getBamStats());
        tabProperties.setGenomeLocator(bamQC.getLocator());

        reporter.loadReportData(bamQC.getBamStats());
        reporter.computeChartsBuffers(bamQC.getBamStats(), bamQC.getLocator(), bamQC.isPairedData());
        tabProperties.setReporter(reporter);


        if(selectedRegionsAvailable && computeOutsideStats){

            BamQCRegionReporter outsideReporter = new BamQCRegionReporter();
            outsideReporter.setNamePostfix(" (outside of regions)");
            outsideReporter.setChromosomeFilePath(outdir  + File.separator + Constants.NAME_OF_FILE_CHROMOSOMES_OUTSIDE);
            outsideReporter.setPaintChromosomeLimits(paintChromosomeLimits);
            BamAnalysisThread.prepareInputDescription(outsideReporter, bamQC, paintChromosomeLimits);


            outsideReporter.writeReport(bamQC.getOutsideBamStats(),outdir);

            outsideReporter.loadReportData(bamQC.getOutsideBamStats());
            outsideReporter.computeChartsBuffers(bamQC.getOutsideBamStats(), bamQC.getLocator(), bamQC.isPairedData());

            tabProperties.setOutsideReporter(outsideReporter);
            tabProperties.setOutsideStatsAvailable(true);
        }


        exportResult(tabProperties);

        logger.println("Finished");

	}
}
