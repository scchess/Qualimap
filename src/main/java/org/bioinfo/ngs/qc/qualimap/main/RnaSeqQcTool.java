/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2014 Garcia-Alcalde et al.
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
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.common.LibraryProtocol;
import org.bioinfo.ngs.qc.qualimap.process.ComputeCountsTask;
import org.bioinfo.ngs.qc.qualimap.process.RNASeqQCAnalysis;
import java.io.File;

/**
 * Created by kokonech
 * Date: 7/14/13
 * Time: 4:15 PM
 */

public class RnaSeqQcTool extends NgsSmartTool {

    public static String OPTION_ANNOTATION = "gtf";
    public static String OPTION_BAM = "bam";
    public static String OPTION_COUNTS_FILE = "oc";
    public static String OPTION_ALGORITHM = "a";
    public static String OPTION_PAIRED = "pe";
    public static String OPTION_ALREADY_SORTED = "s";

    String bamFile, gffFile, countsFile, protocol,alg;
    boolean pairedAnalysis, sortingRequired;

    public RnaSeqQcTool() {
        super(Constants.TOOL_NAME_RNASEQ_QC, false);
        pairedAnalysis = false;
        sortingRequired = false;
    }

    @Override
    protected void initOptions() {
        options.addOption( requiredOption(OPTION_BAM, true, "Input mapping file in BAM format.") );
        options.addOption(requiredOption(OPTION_ANNOTATION, true, "Annotations file in Ensembl GTF format.") );
        options.addOption(new Option(OPTION_COUNTS_FILE, true, "Path to output computed counts.") );
        options.addOption( getProtocolOption() );
        options.addOption(new Option(OPTION_ALGORITHM, "algorithm", true, "Counting algorithm: " +
                ComputeCountsTask.getAlgorithmTypes()  + ".") );

        options.addOption(new Option(OPTION_PAIRED, "paired", false, "Setting this flag for paired-end experiments will result " +
                "in counting fragments instead of reads") );
        options.addOption(new Option(OPTION_ALREADY_SORTED, "sorted", true,
                "This flag indicates that the input file is already sorted by name. " +
                "If not set, additional sorting by name will be performed. " +
                "Only required for paired-end analysis. " ) );


    }

    @Override
    protected void checkOptions() throws ParseException {

        bamFile = commandLine.getOptionValue(OPTION_BAM);
        if (!exists(bamFile))
            throw new ParseException("input mapping file not found");

        gffFile = commandLine.getOptionValue(OPTION_ANNOTATION);
        if(!exists(gffFile))
            throw new ParseException("input region gtf file not found");

        if(commandLine.hasOption(Constants.CMDLINE_OPTION_LIBRARY_PROTOCOL)) {
            protocol = commandLine.getOptionValue(Constants.CMDLINE_OPTION_LIBRARY_PROTOCOL);
            if ( !(protocol.equals( LibraryProtocol.PROTOCOL_FORWARD_STRAND ) ||
                    protocol.equals( LibraryProtocol.PROTOCOL_REVERSE_STRAND ) ||
                    protocol.equals( LibraryProtocol.PROTOCOL_NON_STRAND_SPECIFIC)) ) {
                throw  new ParseException("wrong protocol type! supported types: " +
                        LibraryProtocol.getProtocolNamesString());
            }
        } else {
            protocol = LibraryProtocol.PROTOCOL_NON_STRAND_SPECIFIC;
        }

        if (commandLine.hasOption(OPTION_PAIRED)) {
            pairedAnalysis = true;
            if (!commandLine.hasOption(OPTION_ALREADY_SORTED)) {
                sortingRequired = true;
            }
        }



        if (commandLine.hasOption(OPTION_COUNTS_FILE)) {
            countsFile = commandLine.getOptionValue(OPTION_COUNTS_FILE);
        } else {
            countsFile = "";
        }


        if (commandLine.hasOption(OPTION_ALGORITHM)) {
            alg = commandLine.getOptionValue(OPTION_ALGORITHM);
            if (! ( alg.equalsIgnoreCase(ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED)  ||
                    alg.equalsIgnoreCase(ComputeCountsTask.COUNTING_ALGORITHM_PROPORTIONAL)) ) {
                throw new ParseException("Unknown algorithm! Possible values are: "
                        + ComputeCountsTask.getAlgorithmTypes());
            }
        } else {
            alg = ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED;
        }


    }

    @Override
    protected void initOutputDir() {
        if (outdir.equals(".")) {
            outdir = FilenameUtils.removeExtension(new File(bamFile).getAbsolutePath()) + "_rnaseq_qc";
        }
        super.initOutputDir();
    }


    @Override
    protected void execute() throws Exception {

        initOutputDir();

        ComputeCountsTask computeCountsTask = new ComputeCountsTask(bamFile, gffFile);
        computeCountsTask.setProtocol(LibraryProtocol.getProtocolByName(protocol));
        computeCountsTask.setCountingAlgorithm(alg);
        computeCountsTask.setCollectRnaSeqStats(true);

        if (pairedAnalysis) {
            computeCountsTask.setPairedEndAnalysis();
            if (sortingRequired) {
                computeCountsTask.setSortingRequired();
            }
        }

        AnalysisResultManager resultManager = new AnalysisResultManager(AnalysisType.RNA_SEQ_QC);

        RNASeqQCAnalysis rnaSeqQCAnalysis = new RNASeqQCAnalysis(resultManager, computeCountsTask);
        if (countsFile.length() > 0) {
            rnaSeqQCAnalysis.setCountsFilePath(countsFile);
        }

        rnaSeqQCAnalysis.run();

        exportResult(resultManager);


    }
}
