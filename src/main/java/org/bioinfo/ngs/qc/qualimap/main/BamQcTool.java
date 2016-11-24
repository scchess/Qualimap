/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2016 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.common.LibraryProtocol;
import org.bioinfo.ngs.qc.qualimap.common.SkipDuplicatesMode;
import org.bioinfo.ngs.qc.qualimap.process.BamStatsAnalysis;


import java.io.File;

public class BamQcTool extends NgsSmartTool{

    private String bamFile;
	private String gffFile;
	private boolean selectedRegionsAvailable;
	private int numberOfWindows;
	private int numThreads;
    private int bunchSize;
    private int minHomopolymerSize;
    private boolean paintChromosomeLimits, skipDuplicated;
    private boolean collectOverlappingPairedEndReads;
	private boolean computeOutsideStats;
    private String genomeToCompare;
    private String coverageReportFile;
    private LibraryProtocol protocol;
    private SkipDuplicatesMode skipDuplicatesMode;

    public BamQcTool(){
        super(Constants.TOOL_NAME_BAMQC,false);
        numThreads = Runtime.getRuntime().availableProcessors();
        protocol = LibraryProtocol.NON_STRAND_SPECIFIC;
        skipDuplicatesMode = SkipDuplicatesMode.BOTH;
        genomeToCompare = "";
        coverageReportFile = "";
    }

	@Override
	protected void initOptions() {

        Option opt = new Option(Constants.BAMQC_OPTION_BAM_FILE, true, "Input mapping file in BAM format");
        opt.setRequired(true);
        options.addOption( opt );

        options.addOption(Constants.BAMQC_OPTION_GFF_FILE, "feature-file", true,
                "Feature file with regions of interest in GFF/GTF or BED format");
        options.addOption(Constants.BAMQC_OPTION_NUM_WINDOWS, true,
                "Number of windows (default is "+ Constants.DEFAULT_NUMBER_OF_WINDOWS + ")");
        options.addOption(Constants.BAMQC_OPTION_NUM_THREADS, true,
                    "Number of threads (default is " +  Runtime.getRuntime().availableProcessors() + ")");
        options.addOption(Constants.BAMQC_OPTION_CHUNK_SIZE, true,
                "Number of reads analyzed in a chunk (default is " + Constants.DEFAULT_CHUNK_SIZE + ")" );
        options.addOption(Constants.BAMQC_OPTION_MIN_HOMOPOLYMER_SIZE, true,
                "Minimum size for a homopolymer to be considered in indel analysis (default is "
                        + Constants.DEFAULT_HOMOPOLYMER_SIZE + ") " );
        options.addOption(Constants.BAMQC_OPTION_COVERAGE_REPORT_FILE, "output-genome-coverage",  true,
                "File to save per base non-zero coverage. Warning: large files " +
                "are expected for large genomes");
        options.addOption(Constants.BAMQC_OPTION_PAINT_CHROMOSOMES, "paint-chromosome-limits", false,
                "Paint chromosome limits inside charts");
        options.addOption(Constants.BAMQC_OPTION_SKIP_DUPLICATED, "skip-duplicated",  false,
                                "Activate this option to skip duplicated alignments from the analysis. " +
                                "If the duplicates are not flagged in the BAM file, then they will be detected" +
                                        " by Qualimap and can be selected for skipping.");
        options.addOption(Constants.BAMQC_OPTION_SKIP_DUPLICATES_MODE, "skip-dup-mode", true,
                                        "Specific type of duplicated alignments to skip (if this option is activated).\n" +
                                                "0 : only flagged duplicates (default)\n" +
                                                "1 : only estimated by Qualimap\n" +
                                                "2 : both flagged and estimated"

                                        );

        options.addOption(Constants.BAMQC_OPTION_COLLECT_OVERLAP_PAIRS, "collect-overlap-pairs",  false,
                                "Activate this option to collect statistics of overlapping paired-end reads " );
        options.addOption(Constants.BAMQC_OPTION_OUTSIDE_STATS, "outside-stats", false,
                "Report information for the regions outside those defined by feature-file " +
                        " (ignored when -gff option is not set)");
        options.addOption(Constants.BAMQC_OPTION_COMPARE_WITH_GENOME_DISTRIBUTION, "genome-gc-distr",
                true, "Species to compare with genome GC distribution. " +
                "Possible values: HUMAN - hg19; MOUSE - mm9(default), mm10");
        options.addOption(getProtocolOption());
	}

	@Override
	protected void checkOptions() throws ParseException {
		
		// input

        bamFile = commandLine.getOptionValue(Constants.BAMQC_OPTION_BAM_FILE);
		if(!exists(bamFile)) throw new ParseException("input mapping file not found");

		// gff
		if(commandLine.hasOption(Constants.BAMQC_OPTION_GFF_FILE)) {
			gffFile = commandLine.getOptionValue(Constants.BAMQC_OPTION_GFF_FILE);
			if(!exists(gffFile)) {
                throw new ParseException("input region gff file not found");
            }
			selectedRegionsAvailable = true;
			if(commandLine.hasOption(Constants.BAMQC_OPTION_OUTSIDE_STATS)) {
				computeOutsideStats = true;					
			}

             if (commandLine.hasOption(Constants.CMDLINE_OPTION_LIBRARY_PROTOCOL)) {
                protocol = LibraryProtocol.getProtocolByName(
                        commandLine.getOptionValue(Constants.CMDLINE_OPTION_LIBRARY_PROTOCOL).toLowerCase()
                );
             }
		}


		numberOfWindows =  commandLine.hasOption(Constants.BAMQC_OPTION_NUM_WINDOWS) ?
			Integer.parseInt(commandLine.getOptionValue(Constants.BAMQC_OPTION_NUM_WINDOWS))
                : Constants.DEFAULT_NUMBER_OF_WINDOWS;

        numThreads = commandLine.hasOption(Constants.BAMQC_OPTION_NUM_THREADS) ?
                Integer.parseInt(commandLine.getOptionValue(Constants.BAMQC_OPTION_NUM_THREADS)) : Runtime.getRuntime().availableProcessors();

        bunchSize = commandLine.hasOption(Constants.BAMQC_OPTION_CHUNK_SIZE) ?
                Integer.parseInt(commandLine.getOptionValue(Constants.BAMQC_OPTION_CHUNK_SIZE)) : Constants.DEFAULT_CHUNK_SIZE;


        minHomopolymerSize = commandLine.hasOption(Constants.BAMQC_OPTION_MIN_HOMOPOLYMER_SIZE) ?
                Integer.parseInt(commandLine.getOptionValue(Constants.BAMQC_OPTION_MIN_HOMOPOLYMER_SIZE)) : Constants.DEFAULT_HOMOPOLYMER_SIZE;


        if (commandLine.hasOption(Constants.BAMQC_OPTION_COMPARE_WITH_GENOME_DISTRIBUTION)) {
            String val = commandLine.getOptionValue(Constants.BAMQC_OPTION_COMPARE_WITH_GENOME_DISTRIBUTION);
            if (val.equalsIgnoreCase(BamStatsAnalysis.HUMAN_GENOME_NAME) || val.equalsIgnoreCase("hg19")) {
                genomeToCompare = BamStatsAnalysis.HUMAN_GENOME_ID;
            } else if (val.equalsIgnoreCase(BamStatsAnalysis.MOUSE_GENOME_NAME) || (val.equals("mm9"))) {
                genomeToCompare = BamStatsAnalysis.MOUSE_GENOME_ID;
            } else if (val.equals("mm10")) {
                genomeToCompare = BamStatsAnalysis.MM10_GENOME_ID;
            } else {
                throw new ParseException("Unknown genome \"" + val+ "\", please use available");

            }
        }

        if (commandLine.hasOption(Constants.BAMQC_OPTION_COVERAGE_REPORT_FILE)) {
            coverageReportFile = commandLine.getOptionValue(Constants.BAMQC_OPTION_COVERAGE_REPORT_FILE);
        }


		paintChromosomeLimits =  commandLine.hasOption(Constants.BAMQC_OPTION_PAINT_CHROMOSOMES);
        skipDuplicated = commandLine.hasOption(Constants.BAMQC_OPTION_SKIP_DUPLICATED);
        if (skipDuplicated) {
            skipDuplicatesMode = SkipDuplicatesMode.ONLY_MARKED_DUPLICATES;
        }
        if (commandLine.hasOption(Constants.BAMQC_OPTION_SKIP_DUPLICATES_MODE)) {
            int mode = Integer.parseInt(commandLine.getOptionValue(Constants.BAMQC_OPTION_SKIP_DUPLICATES_MODE));
            if (mode == 2) {
                skipDuplicatesMode = SkipDuplicatesMode.BOTH;
            } else if (mode == 0) {
                skipDuplicatesMode = SkipDuplicatesMode.ONLY_MARKED_DUPLICATES;
            } else if (mode == 1) {
                skipDuplicatesMode = SkipDuplicatesMode.ONLY_DETECTED_DUPLICATES;
            }


        }

        collectOverlappingPairedEndReads = commandLine.hasOption(Constants.BAMQC_OPTION_COLLECT_OVERLAP_PAIRS);

	}

    @Override
    protected void initOutputDir() {
        if (outdir.equals(".")) {
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

		// reporting
		bamQC.activeReporting(outdir);
        if (coverageReportFile.length() > 0) {
            bamQC.setPathToCoverageReport(coverageReportFile);
        }

		logger.println("Starting bam qc....");

		// number of windows
		bamQC.setNumberOfWindows(numberOfWindows);
        bamQC.setNumberOfThreads(numThreads);
        bamQC.setNumberOfReadsInBunch(bunchSize);
        bamQC.setProtocol(protocol);
        bamQC.setMinHomopolymerSize(minHomopolymerSize);
        if (skipDuplicated) {
            bamQC.setSkipDuplicatedReads(true, skipDuplicatesMode);
        }

        if (collectOverlappingPairedEndReads){
            bamQC.activateIntersectingPairedEndReadsStats();
        }

		// run evaluation
		bamQC.run();

		logger.println("end of bam qc");

		logger.println("Computing report...");

		BamQCRegionReporter reporter = new BamQCRegionReporter(selectedRegionsAvailable, true);
		reporter.setPaintChromosomeLimits(paintChromosomeLimits);
        if (!genomeToCompare.isEmpty()) {
            reporter.setGenomeGCContentName(genomeToCompare);
        }

        bamQC.prepareInputDescription(reporter, paintChromosomeLimits);

		// save stats

        reporter.writeReport(bamQC.getBamStats(),outdir, true);

        AnalysisResultManager resultManager = new AnalysisResultManager(AnalysisType.BAM_QC);

        reporter.loadReportData(bamQC.getBamStats());
        reporter.computeChartsBuffers(bamQC.getBamStats(), bamQC.getLocator(), bamQC.isPairedData());
        resultManager.addReporter(reporter);

        if(selectedRegionsAvailable && computeOutsideStats){

            BamQCRegionReporter outsideReporter = new BamQCRegionReporter(selectedRegionsAvailable, false);
            outsideReporter.setPaintChromosomeLimits(paintChromosomeLimits);
            if (!genomeToCompare.isEmpty()) {
                outsideReporter.setGenomeGCContentName(genomeToCompare);
            }
            bamQC.prepareInputDescription(outsideReporter, paintChromosomeLimits);
            outsideReporter.writeReport(bamQC.getOutsideBamStats(),outdir, false);

            outsideReporter.loadReportData(bamQC.getOutsideBamStats());
            outsideReporter.computeChartsBuffers(bamQC.getOutsideBamStats(), bamQC.getLocator(), bamQC.isPairedData());

            resultManager.addReporter(outsideReporter);
        }


        exportResult(resultManager);

        logger.println("Finished");

	}
}
