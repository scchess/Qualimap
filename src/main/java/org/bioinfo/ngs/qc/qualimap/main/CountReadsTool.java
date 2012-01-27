package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.process.CountReadsAnalysis;
import org.bioinfo.tool.OptionFactory;

import javax.swing.*;
import javax.swing.text.html.Option;
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

    String bamFile, gffFile, outFile, protocol;

    static String getProtocolTypes() {
        return  CountReadsAnalysis.FORWARD_STRAND + ","
                + CountReadsAnalysis.REVERSE_STRAND + " or "
                + CountReadsAnalysis.NON_STRAND_SPECIFIC;
    }

    public CountReadsTool() {
        super("counts");
    }

    @Override
    protected void initOptions() {

        options.addOption( OptionFactory.createOption("in", "Mapping file in Bam format)", true, true) );
		options.addOption(OptionFactory.createOption("gff", "Region file in GFF format", true, true) );
        options.addOption(OptionFactory.createOption("output", "Output file.", false, true) );
        options.addOption(OptionFactory.createOption("protocol", getProtocolTypes(), false, true) );

    }

    @Override
    protected void checkOptions() throws ParseException {

        bamFile = commandLine.getOptionValue("in");
		if (!exists(bamFile))
            throw new ParseException("input mapping file not found");

        gffFile = commandLine.getOptionValue("gff");
	    if(!exists(gffFile))
            throw new ParseException("input region gff file not found");

        if(commandLine.hasOption("protocol")) {
		    protocol = commandLine.getOptionValue("stranded");
            if ( !(protocol == CountReadsAnalysis.FORWARD_STRAND ||
                    protocol == CountReadsAnalysis.NON_STRAND_SPECIFIC ||
                    protocol == CountReadsAnalysis.NON_STRAND_SPECIFIC) ) {
                throw  new ParseException("wrong protocol type! supported types: " + getProtocolTypes());
            }
        } else {
            protocol = CountReadsAnalysis.FORWARD_STRAND;
        }

        if (commandLine.hasOption("outfile")) {
            outFile = commandLine.getOptionValue("outfile");
        } else {
            outFile = "";
        }

    }

    @Override
    protected void execute() throws Exception {

        CountReadsAnalysis  countReadsAnalysis = new CountReadsAnalysis(bamFile, gffFile);
        countReadsAnalysis.setProtocol(protocol);

        PrintWriter outWriter = outFile.isEmpty() ?
                new PrintWriter(new OutputStreamWriter(System.out)) :
                new PrintWriter(new FileWriter(outFile));


        try {
            countReadsAnalysis.run();

            Map<String,Long> counts = countReadsAnalysis.getReadCounts();
            for (Map.Entry<String,Long> entry: counts.entrySet()) {
                String str = entry.getKey() + "\t" + entry.getValue().toString();
                outWriter.println(str);
            }

            outWriter.flush();

        } catch (Exception e) {
            System.err.println("Error while calculating counts! " + e.getMessage());
            return;
        }

        long totalCounted = countReadsAnalysis.getTotalReadCounts();
        long noFeature = countReadsAnalysis.getNoFeatureNumber();
        long notUnique = countReadsAnalysis.getAlignmentNotUniqueNumber();
        long ambiguous = countReadsAnalysis.getAmbiguousNumber();

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
