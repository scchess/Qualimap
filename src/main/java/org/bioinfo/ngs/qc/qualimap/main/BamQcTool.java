/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2013 Garcia-Alcalde et al.
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
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.common.LibraryProtocol;
import org.bioinfo.ngs.qc.qualimap.gui.threads.BamAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.AnalysisType;
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
    private boolean paintChromosomeLimits;
	private boolean computeOutsideStats;
    private String genomeToCompare;
    private LibraryProtocol protocol;

    static final String OPTION_NAME_BAM_FILE = "bam";
    static final String OPTION_NAME_GFF_FILE = "gff";
    static final String OPTION_COMPARE_WITH_GENOME_DISTRIBUTION = "gd";
    static final String OPTION_PAINT_CHROMOSOMES = "c";
    static final String OPTION_NUM_WINDOWS = "nw";
    static final String OPTION_CHUNK_SIZE = "nr";
    static final String OPTION_NUM_THREADS = "nt";
    static final String OPTION_OUTSIDE_STATS = "os";
    static final String OPTION_LIBRARY_PROTOCOL = "p";
    static final String OPTION_MIN_HOMOPOLYMER_SIZE = "hm";

    public BamQcTool(){
        super(Constants.TOOL_NAME_BAMQC);
        numThreads = Runtime.getRuntime().availableProcessors();
        protocol = LibraryProtocol.STRAND_NON_SPECIFIC;
        genomeToCompare = "";
    }

	@Override
	protected void initOptions() {

        Option opt = new Option(OPTION_NAME_BAM_FILE, true, "input mapping file");
        opt.setRequired(true);
        options.addOption( opt );

        options.addOption( OPTION_NAME_GFF_FILE,  true, "region file (in GFF/GTF or BED format)");
        options.addOption(OPTION_NUM_WINDOWS, true,
                "number of windows (default is "+ Constants.DEFAULT_NUMBER_OF_WINDOWS + ")");
        options.addOption(OPTION_NUM_THREADS, true,
                "number of threads (default is " +  Runtime.getRuntime().availableProcessors() + ")");
        options.addOption(OPTION_CHUNK_SIZE, true,
                "number of reads in the chunk" );
         options.addOption(OPTION_MIN_HOMOPOLYMER_SIZE, true,
                "minimum size for a homopolymer to be considered in indel analysis (default is "
                        + Constants.DEFAULT_HOMOPOLYMER_SIZE + ") " );

		options.addOption(OPTION_PAINT_CHROMOSOMES, "paint-chromosome-limits", false, "paint chromosome limits inside charts");
		options.addOption(OPTION_OUTSIDE_STATS, "outside-stats", false, "compute region outside stats (works only with -gff option)");
        options.addOption(OPTION_COMPARE_WITH_GENOME_DISTRIBUTION, true, "compare with genome distribution " +
                "(possible values: HUMAN or MOUSE)");

        options.addOption(OPTION_LIBRARY_PROTOCOL, true,
                "specify protocol to calculate correct strand reads (works only with -gff option, " +
                        "possible values are STRAND-SPECIFIC-FORWARD or STRAND-SPECIFIC-REVERSE, " +
                        "default is NON-STRAND-SPECIFIC) ");

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
			if(commandLine.hasOption(OPTION_OUTSIDE_STATS)) {
				computeOutsideStats = true;					
			}

             if (commandLine.hasOption(OPTION_LIBRARY_PROTOCOL)) {
                protocol = LibraryProtocol.getProtocolByName(
                        commandLine.getOptionValue(OPTION_LIBRARY_PROTOCOL).toLowerCase()
                );
             }
		}


		numberOfWindows =  commandLine.hasOption(OPTION_NUM_WINDOWS) ?
			Integer.parseInt(commandLine.getOptionValue(OPTION_NUM_WINDOWS))
                : Constants.DEFAULT_NUMBER_OF_WINDOWS;

        numThreads = commandLine.hasOption(OPTION_NUM_THREADS) ?
                Integer.parseInt(commandLine.getOptionValue(OPTION_NUM_THREADS)) : Runtime.getRuntime().availableProcessors();

        bunchSize = commandLine.hasOption(OPTION_CHUNK_SIZE) ?
                Integer.parseInt(commandLine.getOptionValue(OPTION_CHUNK_SIZE)) : Constants.DEFAULT_CHUNK_SIZE;


        minHomopolymerSize = commandLine.hasOption(OPTION_MIN_HOMOPOLYMER_SIZE) ?
                Integer.parseInt(commandLine.getOptionValue(OPTION_MIN_HOMOPOLYMER_SIZE)) : Constants.DEFAULT_HOMOPOLYMER_SIZE;


        if (commandLine.hasOption(OPTION_COMPARE_WITH_GENOME_DISTRIBUTION)) {
            String val = commandLine.getOptionValue(OPTION_COMPARE_WITH_GENOME_DISTRIBUTION);
            if (val.equalsIgnoreCase("human")) {
                genomeToCompare = BamStatsAnalysis.HUMAN_GENOME_ID;
            } else if (val.equalsIgnoreCase("mouse")) {
                genomeToCompare = BamStatsAnalysis.MOUSE_GENOME_ID;
            } else {
                throw new ParseException("Unknown genome \"" + val+ "\", please use HUMAN or MOUSE");

            }
        }


		paintChromosomeLimits =  commandLine.hasOption(OPTION_PAINT_CHROMOSOMES);

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

		// reporting
		bamQC.activeReporting(outdir);
		//if(saveCoverage) bamQC.ctiveCoverageReporting();

		logger.println("Starting bam qc....");

		// number of windows
		bamQC.setNumberOfWindows(numberOfWindows);
        bamQC.setNumberOfThreads(numThreads);
        bamQC.setNumberOfReadsInBunch(bunchSize);
        bamQC.setProtocol(protocol);
        bamQC.setMinHomopolymerSize(minHomopolymerSize);

		// run evaluation
		bamQC.run();

		logger.println("end of bam qc");

		logger.println("Computing report...");

		BamQCRegionReporter reporter = new BamQCRegionReporter(selectedRegionsAvailable, true);
		reporter.setPaintChromosomeLimits(paintChromosomeLimits);
        if (!genomeToCompare.isEmpty()) {
            reporter.setGenomeGCContentName(genomeToCompare);
        }


        BamAnalysisThread.prepareInputDescription(reporter, bamQC, paintChromosomeLimits);

		// save stats

        reporter.writeReport(bamQC.getBamStats(),outdir);

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
            BamAnalysisThread.prepareInputDescription(outsideReporter, bamQC, paintChromosomeLimits);
            outsideReporter.writeReport(bamQC.getOutsideBamStats(),outdir);

            outsideReporter.loadReportData(bamQC.getOutsideBamStats());
            outsideReporter.computeChartsBuffers(bamQC.getOutsideBamStats(), bamQC.getLocator(), bamQC.isPairedData());

            resultManager.addReporter(outsideReporter);
        }


        exportResult(resultManager);

        logger.println("Finished");

	}
}
