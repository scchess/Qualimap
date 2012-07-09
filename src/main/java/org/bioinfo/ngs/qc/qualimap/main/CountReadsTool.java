package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.LibraryProtocol;
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

    public static String OPTION_GTF = "gtf";
    public static String OPTION_BAM = "bam";
    public static String OPTION_FEATURE_ID = "id";
    public static String OPTION_FEATURE_TYPE = "type";
    public static String OPTION_PROTOCOL = "protocol";
    public static String OPTION_OUT_FILE = "out";
    public static String OPTION_ALGORITHM = "algorithm";
    public static String OPTION_5_TO_3_BIAS = "b";

    String bamFile, gffFile, outFile, protocol, featureType, attrName, alg;
    boolean calcCovBias;

    static String getProtocolTypes() {
        return  LibraryProtocol.PROTOCOL_FORWARD_STRAND + ","
                + LibraryProtocol.PROTOCOL_REVERSE_STRAND + " or "
                + LibraryProtocol.PROTOCOL_NON_STRAND_SPECIFIC;
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

        options.addOption( requiredOption(OPTION_BAM, true, "mapping file in BAM format)") );
		options.addOption(requiredOption(OPTION_GTF, true, "region file in GTF format") );
        options.addOption(new Option(OPTION_OUT_FILE, true, "path to output file") );
        options.addOption(new Option(OPTION_PROTOCOL, true, getProtocolTypes()) );
        options.addOption(new Option(OPTION_FEATURE_TYPE, true, "Value of the third column of the GTF considered" +
                " for counting. Other types will be ignored. Default: exon"));
        options.addOption(new Option(OPTION_FEATURE_ID, true, "attribute of the GTF to be used as feature ID. " +
                "Regions with the same ID will be aggregated as part of the same feature. Default: gene_id."));
        options.addOption(new Option(OPTION_ALGORITHM, true, getAlgorithmTypes()));
        options.addOption(new Option(OPTION_OUT_FILE, true, "path to output file") );
        options.addOption( new Option(OPTION_5_TO_3_BIAS, false, "calculate 5' and 3' coverage bias"));


    }

    @Override
    protected void checkOptions() throws ParseException {

        bamFile = commandLine.getOptionValue(OPTION_BAM);
		if (!exists(bamFile))
            throw new ParseException("input mapping file not found");

        gffFile = commandLine.getOptionValue(OPTION_GTF);
	    if(!exists(gffFile))
            throw new ParseException("input region gtf file not found");

        if(commandLine.hasOption(OPTION_PROTOCOL)) {
		    protocol = commandLine.getOptionValue(OPTION_PROTOCOL);
            if ( !(protocol.equals( LibraryProtocol.PROTOCOL_FORWARD_STRAND ) ||
                    protocol.equals( LibraryProtocol.PROTOCOL_NON_STRAND_SPECIFIC ) ||
                    protocol.equals( LibraryProtocol.PROTOCOL_NON_STRAND_SPECIFIC)) ) {
                throw  new ParseException("wrong protocol type! supported types: " + getProtocolTypes());
            }
        } else {
            protocol = LibraryProtocol.PROTOCOL_FORWARD_STRAND;
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
                throw new ParseException("Unknown algorithm! Possible values are: " + getAlgorithmTypes());
            }
        } else {
            alg = ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED;
        }


        calcCovBias = commandLine.hasOption(OPTION_5_TO_3_BIAS);





    }

    @Override
    protected void execute() throws Exception {

        ComputeCountsTask computeCountsTask = new ComputeCountsTask(bamFile, gffFile);
        computeCountsTask.setProtocol(protocol);
        computeCountsTask.setCountingAlgorithm(alg);
        computeCountsTask.setAttrName(attrName);
        computeCountsTask.addSupportedFeatureType(featureType);
        computeCountsTask.setCalcCoverageBias(calcCovBias);

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
