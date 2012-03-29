package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
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
public class CountReadsTool extends NgsSmartTool {

    String bamFile, gffFile, outFile, protocol, featureType, attrName, alg;

    static String getProtocolTypes() {
        return  ComputeCountsTask.PROTOCOL_FORWARD_STRAND + ","
                + ComputeCountsTask.PROTOCOL_REVERSE_STRAND + " or "
                + ComputeCountsTask.PROTOCOL_NON_STRAND_SPECIFIC;
    }

    static String getAlgorithmTypes() {
        return ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED + "(default) or " +
                ComputeCountsTask.COUNTING_ALGORITHM_PROPORTIONAL;
    }

    public CountReadsTool() {
        super(Constants.TOOL_NAME_COMPUTE_COUNTS,false,false);
    }

    @Override
    protected void initOptions() {

        options.addOption( requiredOption("bam", true, "mapping file in BAM format)") );
		options.addOption(requiredOption("gff", true, "region file in GFF format") );
        options.addOption(new Option("o", "output", true, "path to output file") );
        options.addOption(new Option("p", "protocol", true, getProtocolTypes()) );
        options.addOption(new Option("feature", true, "feature type, default is \"exon\" "));
        options.addOption(new Option("attr", true, "attribute to count, default is \"gene\" "));
        options.addOption(new Option("alg", true, getAlgorithmTypes()));


    }

    @Override
    protected void checkOptions() throws ParseException {

        bamFile = commandLine.getOptionValue("bam");
		if (!exists(bamFile))
            throw new ParseException("input mapping file not found");

        gffFile = commandLine.getOptionValue("gff");
	    if(!exists(gffFile))
            throw new ParseException("input region gff file not found");

        if(commandLine.hasOption("protocol")) {
		    protocol = commandLine.getOptionValue("protocol");
            if ( !(protocol.equals( ComputeCountsTask.PROTOCOL_FORWARD_STRAND ) ||
                    protocol.equals( ComputeCountsTask.PROTOCOL_NON_STRAND_SPECIFIC ) ||
                    protocol.equals( ComputeCountsTask.PROTOCOL_NON_STRAND_SPECIFIC)) ) {
                throw  new ParseException("wrong protocol type! supported types: " + getProtocolTypes());
            }
        } else {
            protocol = ComputeCountsTask.PROTOCOL_FORWARD_STRAND;
        }

        if (commandLine.hasOption("o")) {
            outFile = commandLine.getOptionValue("o");
        } else {
            outFile = "";
        }

        if (commandLine.hasOption("feature")) {
            featureType = commandLine.getOptionValue("feature");
        } else {
            featureType = ComputeCountsTask.EXON_TYPE_ATTR;
        }

        if (commandLine.hasOption("attr")) {
            attrName = commandLine.getOptionValue("attr");
        } else {
            attrName = ComputeCountsTask.GENE_ID_ATTR;
        }

        if (commandLine.hasOption("alg")) {
            alg = commandLine.getOptionValue("alg");
            if (! ( alg.equalsIgnoreCase(ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED)  ||
                     alg.equalsIgnoreCase(ComputeCountsTask.COUNTING_ALGORITHM_PROPORTIONAL)) ) {
                throw new ParseException("Unknown algorithm! Possible values are: " + getAlgorithmTypes());
            }
        } else {
            alg = ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED;
        }







    }

    @Override
    protected void execute() throws Exception {

        ComputeCountsTask computeCountsTask = new ComputeCountsTask(bamFile, gffFile);
        computeCountsTask.setProtocol(protocol);
        computeCountsTask.setCountingAlgorithm(alg);
        computeCountsTask.setAttrName(attrName);
        computeCountsTask.addSupportedFeatureType(featureType);

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

        long totalCounted = computeCountsTask.getTotalReadCounts();
        long noFeature = computeCountsTask.getNoFeatureNumber();
        long notUnique = computeCountsTask.getAlignmentNotUniqueNumber();
        long ambiguous = computeCountsTask.getAmbiguousNumber();

        StringBuilder message = new StringBuilder();
        message.append("Calculation successful!\n");
        message.append("Feature read counts: ").append(totalCounted).append("\n");
        message.append("No feature: ").append(noFeature).append("\n");
        message.append("Not unique alignment: ").append(notUnique).append("\n");
        message.append("Ambiguous: ").append(ambiguous).append("\n");
        if (!outFile.isEmpty()) {
            message.append("Result is saved to file ").append(outFile);
        }

        System.err.print(message);

    }


}
