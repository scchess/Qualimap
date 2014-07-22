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
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.common.LibraryProtocol;
import org.bioinfo.ngs.qc.qualimap.process.ComputeCountsTask;

import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by kokonech
 * Date: 1/27/12
 * Time: 12:20 PM
 */
public class ComputeCountsTool extends NgsSmartTool {

    public static String OPTION_ANNOTATION = "gtf";
    public static String OPTION_BAM = "bam";
    public static String OPTION_FEATURE_ID = "id";
    public static String OPTION_FEATURE_TYPE = "type";
    public static String OPTION_PROTOCOL = "p";
    public static String OPTION_OUT_FILE = "out";
    public static String OPTION_ALGORITHM = "algorithm";
    public static String OPTION_PAIRED = "pe";
    public static String OPTION_ALREADY_SORTED = "s";

    String bamFile, gffFile, outFile, featureType, attrName, alg;
    LibraryProtocol protocol;
    boolean pairedAnalysis, sortingRequired;

    public ComputeCountsTool() {
        super(Constants.TOOL_NAME_COMPUTE_COUNTS,false,false, false);
        this.pairedAnalysis = false;
        this.sortingRequired = false;
        this.protocol = LibraryProtocol.NON_STRAND_SPECIFIC;
    }

    @Override
    protected void initOptions() {

        options.addOption( requiredOption(OPTION_BAM, true, "Mapping file in BAM format") );
		options.addOption(requiredOption(OPTION_ANNOTATION, true, "Region file in GTF, GFF or BED format. " +
                "If GTF format is provided, counting is based on attributes, otherwise based on feature name") );
        options.addOption(new Option(OPTION_PROTOCOL, "protocol", true,
                "Library protocol: " + LibraryProtocol.getProtocolNamesString()) );
        options.addOption(new Option(OPTION_FEATURE_TYPE, true, "GTF-specific. Value of the third column of the GTF considered" +
                " for counting. Other types will be ignored. Default: exon"));
        options.addOption(new Option(OPTION_FEATURE_ID, true, "GTF-specific. Attribute of the GTF to be used as feature ID. " +
                "Regions with the same ID will be aggregated as part of the same feature. Default: gene_id."));
        options.addOption(new Option(OPTION_ALGORITHM, true, "Counting algorithm: " + ComputeCountsTask.getAlgorithmTypes()));
        options.addOption(new Option(OPTION_OUT_FILE, true, "Path to output file") );
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

        if(commandLine.hasOption(OPTION_PROTOCOL)) {
		    String protocolName = commandLine.getOptionValue(OPTION_PROTOCOL);
            if ( !ComputeCountsTask.supportedLibraryProtocol(protocolName) ) {
                throw  new ParseException("wrong protocol type! supported types: " +
                        LibraryProtocol.getProtocolNamesString());
            } else {
                protocol = LibraryProtocol.getProtocolByName(protocolName);
            }
        }

        if (commandLine.hasOption(OPTION_OUT_FILE)) {
            outFile = commandLine.getOptionValue(OPTION_OUT_FILE);
        } else {
            outFile = "";
        }

        if (commandLine.hasOption(OPTION_FEATURE_TYPE)) {
            featureType = commandLine.getOptionValue(OPTION_FEATURE_TYPE);
        } else {
            featureType = ComputeCountsTask.EXON_TYPE_ATTR;
        }

        if (commandLine.hasOption(OPTION_FEATURE_ID)) {
            attrName = commandLine.getOptionValue(OPTION_FEATURE_ID);
        } else {
            attrName = ComputeCountsTask.GENE_ID_ATTR;
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

        if (commandLine.hasOption(OPTION_PAIRED)) {
            pairedAnalysis = true;
            if (!commandLine.hasOption(OPTION_ALREADY_SORTED)) {
                sortingRequired = true;
            }
        }



    }

    @Override
    protected void execute() throws Exception {

        ComputeCountsTask computeCountsTask = new ComputeCountsTask(bamFile, gffFile);
        computeCountsTask.setProtocol(protocol);
        computeCountsTask.setCountingAlgorithm(alg);
        computeCountsTask.setAttrName(attrName);
        computeCountsTask.addSupportedFeatureType(featureType);
        if (pairedAnalysis) {
            computeCountsTask.setPairedEndAnalysis();
            if (sortingRequired) {
                computeCountsTask.setSortingRequired();
            }
        }

        PrintWriter outWriter = outFile.isEmpty() ?
                new PrintWriter(new OutputStreamWriter(System.out)) :
                new PrintWriter(new FileWriter(outFile));


        try {
            computeCountsTask.run();

            Map<String,Double> counts = computeCountsTask.getReadCounts();
            for (Map.Entry<String,Double> entry: counts.entrySet()) {
                long roundedValue = entry.getValue().longValue();
                String str = entry.getKey() + "\t" + roundedValue;
                outWriter.println(str);
            }


            outWriter.flush();

        } catch (Exception e) {
            System.err.println("Error while calculating counts! " + e.getMessage());
            e.printStackTrace();
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Calculation successful!\n");
        message.append( computeCountsTask.getOutputStatsMessage() );


        if (!outFile.isEmpty()) {
            message.append("Result is saved to file ").append(outFile);
        }

        System.err.println(message);

    }


}
