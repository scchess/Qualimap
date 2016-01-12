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
package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
import org.bioinfo.ngs.qc.qualimap.process.BamStatsAnalysisConfig;
import org.bioinfo.ngs.qc.qualimap.process.MultisampleBamQcAnalysis;
import org.bioinfo.ngs.qc.qualimap.process.SampleInfo;

import java.io.File;
import java.util.List;

/**
 * Created by kokonech
 * Date: 6/5/14
 * Time: 12:48 PM
 */
public class MultisampleBamQcTool extends NgsSmartTool{


    String inputFile;
    boolean runBamQCFirst;
    BamStatsAnalysisConfig bamQcCfg;

    public MultisampleBamQcTool() {
        super(Constants.TOOL_NAME_MULTISAMPLE_BAM_QC, false);
    }

    @Override
    protected void initOptions() {
        options.addOption(requiredOption("d", "data", true,
                "File describing the input data. Format of the file is a 2- or 3-column tab-delimited table." +
                "\nColumn 1: sample name \nColumn 2: either path to the BAM QC result or path to BAM file (-r mode)" +
                        "\nColumn 3: group name (activates sample group marking)\n"));

        options.addOption("r", "run-bamqc", false, "Raw BAM files are provided as input. If this option is activated" +
                " BAM QC process first will be run for each sample, then multi-sample " +
                "analysis will be performed.");

        // BAM QC options

        options.addOption(Constants.BAMQC_OPTION_GFF_FILE, "feature-file", true,
                "Only for -r mode. Feature file with regions of interest in GFF/GTF or BED format");
        options.addOption(Constants.BAMQC_OPTION_NUM_WINDOWS, true,
                "Only for -r mode. Number of windows (default is "+ Constants.DEFAULT_NUMBER_OF_WINDOWS + ")");
        options.addOption(Constants.BAMQC_OPTION_CHUNK_SIZE, true,
                "Only for -r mode. Number of reads analyzed in a chunk (default is " + Constants.DEFAULT_CHUNK_SIZE + ")" );
        options.addOption(Constants.BAMQC_OPTION_MIN_HOMOPOLYMER_SIZE, true,
                "Only for -r mode. Minimum size for a homopolymer to be considered in indel analysis (default is "
                        + Constants.DEFAULT_HOMOPOLYMER_SIZE + ") " );
        options.addOption(Constants.BAMQC_OPTION_PAINT_CHROMOSOMES, "paint-chromosome-limits", false,
                "Only for -r mode. Paint chromosome limits inside charts");



    }

    @Override
    protected void checkOptions() throws ParseException {
        inputFile = commandLine.getOptionValue("d");
        if(!exists(inputFile)) {
            throw new ParseException("input data description file (--data) " + inputFile + " is not found");
        }

        runBamQCFirst = commandLine.hasOption("r");
        if (runBamQCFirst) {
            bamQcCfg = new BamStatsAnalysisConfig();

            if(commandLine.hasOption(Constants.BAMQC_OPTION_GFF_FILE)) {
                bamQcCfg.gffFile = commandLine.getOptionValue(Constants.BAMQC_OPTION_GFF_FILE);
                if(!exists(bamQcCfg.gffFile)) {
                    throw new ParseException("input region gff file not found");
                }
            }


            bamQcCfg.numberOfWindows =  commandLine.hasOption(Constants.BAMQC_OPTION_NUM_WINDOWS) ?
                    Integer.parseInt(commandLine.getOptionValue(Constants.BAMQC_OPTION_NUM_WINDOWS))
                    : Constants.DEFAULT_NUMBER_OF_WINDOWS;

            bamQcCfg.bunchSize = commandLine.hasOption(Constants.BAMQC_OPTION_CHUNK_SIZE) ?
                    Integer.parseInt(commandLine.getOptionValue(Constants.BAMQC_OPTION_CHUNK_SIZE)) :
                    Constants.DEFAULT_CHUNK_SIZE;


            bamQcCfg.minHomopolymerSize = commandLine.hasOption(Constants.BAMQC_OPTION_MIN_HOMOPOLYMER_SIZE) ?
                    Integer.parseInt(commandLine.getOptionValue(Constants.BAMQC_OPTION_MIN_HOMOPOLYMER_SIZE)) :
                    Constants.DEFAULT_HOMOPOLYMER_SIZE;

            bamQcCfg.drawChromosomeLimits =  commandLine.hasOption(Constants.BAMQC_OPTION_PAINT_CHROMOSOMES);

        }
    }

    @Override
    protected void initOutputDir() {
        if (outdir.equals(".")) {
            String parentDir = new File(inputFile).getParent();
            if (parentDir == null) {
                parentDir = ".";
            }
            outdir = parentDir + File.separator + "multi_bamqc";
        }
        super.initOutputDir();
    }


    @Override
    protected void execute() throws Exception {

        initOutputDir();

        AnalysisResultManager resultManager = new AnalysisResultManager(AnalysisType.MULTISAMPLE_BAM_QC);

        List<SampleInfo> bamQcResults = MultisampleBamQcAnalysis.parseInputFile(inputFile);

        if (bamQcResults.size() == 0) {
            System.err.println("The input file " + inputFile + " does not contain any samples. " +
                    "Please check file format.");
            System.exit(-1);
        }

        MultisampleBamQcAnalysis multiBamQCAnalysis =
                new MultisampleBamQcAnalysis(resultManager, homePath, bamQcResults);
        if (runBamQCFirst) {
            multiBamQCAnalysis.setRunBamQcFirst(bamQcCfg);
        }

        LoggerThread loggerThread = new LoggerThread() {
            public void logLine(String msg) {
                System.out.println(msg);
            }
        };

        multiBamQCAnalysis.setOutputParsingThread(loggerThread);

        multiBamQCAnalysis.run();

        logger.println("\nPreparing result report");
        exportResult(resultManager);

        logger.println("Finished");
    }
}
