package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.process.BamQCSplitted;
import org.bioinfo.ngs.qc.qualimap.process.BamStatsAnalysis;

import java.io.File;


public class BamQcTool extends NgsSmartTool{
	private String bamFile;
	private String gffFile;
	private String referenceFile;
    private boolean referenceAvailable;
	private boolean selectedRegionsAvailable;
	private int numberOfWindows;
	private int numThreads;
    private boolean saveCoverage;
	private boolean paintChromosomeLimits;
	private boolean computeChromosomeStats;
	private boolean computeOutsideStats;
	private boolean computeInsertSize;

    public BamQcTool(){
        super("bamqc");
        numThreads = Runtime.getRuntime().availableProcessors();
    }

	@Override
	protected void initOptions() {
		
		options.addOption("i", true, "mapping file (bam format)");
		options.addOption("gff", true, "region file (gff format)");
//		options.addOption("reference", true, "reference genome file (fasta format)");
		
		options.addOption("nw", true, "number of windows (advanced)");
        options.addOption("nt", true, "number of threads (advanced)");

		
		options.addOption("paint_chromosome_limits", false, "paint chromosome limits inside charts");
		options.addOption("outside_stats", false, "compute region outside stats (with -gff option)");
		options.addOption("chr_stats", false, "compute chromosome stats");
		options.addOption("isize", false, "compute insert size chart (only for pair-end seq)");
		
		options.addOption("sc", false, "save coverageData per nucleotide");
	}

	@Override
	protected void checkOptions() throws ParseException {
		
		// input
		if(!commandLine.hasOption("i")) throw new ParseException("input mapping file required");
		bamFile = commandLine.getOptionValue("i");
		if(!exists(bamFile)) throw new ParseException("input mapping file not found");

		// gff
		if(commandLine.hasOption("gff")) {
			gffFile = commandLine.getOptionValue("gff");
			if(!exists(gffFile)) throw new ParseException("input region gff file not found");
			selectedRegionsAvailable = true;
			if(commandLine.hasOption("outside-stats")) {
				computeOutsideStats = true;					
			}
		}

		// reference
		if(commandLine.hasOption("reference")) {
			referenceFile = commandLine.getOptionValue("reference");
			if(!exists(referenceFile)) throw new ParseException("reference file not found");
			referenceAvailable = true;
		}

		// number of windows
		numberOfWindows = Constants.DEFAULT_NUMBER_OF_WINDOWS;
		if(commandLine.hasOption("nw")) {
			numberOfWindows = Integer.parseInt(commandLine.getOptionValue("nw"));
		}

		// reporting
		if(commandLine.hasOption("sc")) {
			saveCoverage = true;
		}

		// reporting
		if(commandLine.hasOption("paint_chromosome_limits")) {
			paintChromosomeLimits = true;
		}

		// chromosome stats
		if(commandLine.hasOption("chr_stats")) {
			computeChromosomeStats = true;					
		}

		// insert size
		if(commandLine.hasOption("isize")) {
			computeInsertSize = true;					
		}

        if (commandLine.hasOption("nt")) {
            numThreads = Integer.parseInt(commandLine.getOptionValue("nt"));
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

		if (referenceAvailable) {
            bamQC.setReferenceFile(referenceFile);
        }


        if(selectedRegionsAvailable){
			bamQC.setSelectedRegions(gffFile);
			bamQC.setComputeOutsideStats(computeOutsideStats);
		}

		// insert size
		bamQC.setComputeInsertSize(computeInsertSize);
		
		// chromosome stats
		bamQC.setComputeChromosomeStats(computeChromosomeStats);

		// reporting
		bamQC.activeReporting(outdir);
		//if(saveCoverage) bamQC.ctiveCoverageReporting();

		logger.println("Starting bam qc....");

		// number of windows
		bamQC.setNumberOfWindows(numberOfWindows);
        bamQC.setNumberOfThreads(numThreads);

		// run evaluation
		bamQC.run();

		logger.println("end of bam qc");

		logger.println("Computing report...");
		// report
		BamQCRegionReporter reporter = new BamQCRegionReporter();
		// paints
		reporter.setPaintChromosomeLimits(paintChromosomeLimits);
		// save stats
		logger.print("   text report...");
		reporter.writeReport(bamQC.getBamStats(),outdir);
		logger.println("OK");		
		// save charts
		logger.print("   charts...");
		reporter.saveCharts(bamQC.getBamStats(), outdir, bamQC.getLocator(), bamQC.isPairedData());
		logger.println("OK");

		if(selectedRegionsAvailable){
			// save stats
			logger.print("   inside text report...");
			reporter.writeReport(bamQC.getBamStats(),outdir);
			logger.println("OK");		
			// save charts
			logger.print("   inside charts...");
			reporter.saveCharts(bamQC.getBamStats(), outdir, null, bamQC.isPairedData());
			logger.println("OK");

			// save stats
			if(computeOutsideStats){
				logger.print("   outside text report...");
				reporter.writeReport(bamQC.getOutsideBamStats(),outdir);
				logger.println("OK");		
				// save charts
				logger.print("   outside charts...");
				reporter.saveCharts(bamQC.getOutsideBamStats(), outdir, null, bamQC.isPairedData());
				logger.println("OK");
			}

		}

	}
}
