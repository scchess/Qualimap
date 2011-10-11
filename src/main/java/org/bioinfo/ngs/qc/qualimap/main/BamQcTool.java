package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.process.BamQCSplitted;


public class BamQcTool extends NgsSmartTool{
	private String bamFile;
	private String gffFile;
	private String referenceFile;
	private boolean referenceAvailable;
	private boolean selectedRegionsAvailable;
	private int numberOfWindows;
	private boolean saveCoverage;
	private boolean paintChromosomeLimits;
	private boolean computeChromosomeStats;
	private boolean computeOutsideStats;
	private boolean computeInsertSize;
	private String qualimapFolder;


	@Override
	protected void initOptions() {
		
		options.addOption("i", true, "mapping file (bam format)");
		options.addOption("gff", true, "region file (gff format)");
//		options.addOption("reference", true, "reference genome file (fasta format)");
		
		options.addOption("nw", true, "number of windows (advance)");
		
		options.addOption("paint_chromosome_limits", false, "paint chromosome limits inside charts");
		options.addOption("outside_stats", false, "compute region outside stats (with -gff option)");
		options.addOption("chr_stats", false, "compute chromosome stats");
		options.addOption("isize", false, "compute insert size chart (only for pair-end seq)");
		
		options.addOption("sc", false, "save coverage per nucleotide");
	}

	@Override
	protected void checkOptions() throws ParseException {
		
		// home
		if(!commandLine.hasOption("home")) throw new ParseException("qualimap folder required");
		qualimapFolder = commandLine.getOptionValue("home");

		// output folder
		if(!commandLine.hasOption("o")) throw new ParseException("output folder required");

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
		numberOfWindows = BamQCSplitted.DEFAULT_NUMBER_OF_WINDOWS;
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

	}


	@Override
	protected void execute() throws Exception {

		// check outdir
		initOutputDir();

		// init bamqc
		BamQCSplitted bamQC = referenceAvailable ? new BamQCSplitted(bamFile,referenceFile) :
                  new BamQCSplitted(bamFile);

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
		if(saveCoverage) bamQC.activeCoverageReporting();

		logger.println("Starting bam qc....");

		// number of windows
		bamQC.setNumberOfWindows(numberOfWindows);

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
			reporter.writeReport(bamQC.getInsideBamStats(),outdir);
			logger.println("OK");		
			// save charts
			logger.print("   inside charts...");
			reporter.saveCharts(bamQC.getInsideBamStats(), outdir, null, bamQC.isPairedData());
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
