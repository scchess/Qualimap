package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
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

    public MultisampleBamQcTool() {
        super(Constants.TOOL_NAME_MULTISAMPLE_BAM_QC, false);
    }

    @Override
    protected void initOptions() {
        options.addOption(requiredOption("d", "data", true,
                "File describing the input data. Format of the file is a 2-column tab-delimited table." +
                "\nColumn 1: sample name \nColumn 2: path to the BAM QC result for the sample"));

    }

    @Override
    protected void checkOptions() throws ParseException {
        inputFile = commandLine.getOptionValue("input");
        if(!exists(inputFile)) {
            throw new ParseException("input data description file (--data) " + inputFile + " is not found");
        }
    }

    @Override
    protected void initOutputDir() {
        if (outdir.equals(".")) {
            outdir = FilenameUtils.removeExtension(new File(inputFile).getParent()) + File.separator + "multi_bamqc";
        }
        super.initOutputDir();
    }


    @Override
    protected void execute() throws Exception {
        // init output dir
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

        LoggerThread loggerThread = new LoggerThread() {
            public void logLine(String msg) {
                System.out.println(msg);
            }
        };
        multiBamQCAnalysis.setOutputParsingThread(loggerThread);


        try {
            multiBamQCAnalysis.run();
        } catch (Exception e) {
            System.err.println("Failed to perform multi-sample BAM QC analysis");
            e.printStackTrace();
            System.exit(-1);
        }

        logger.println("\nPreparing result report");
        exportResult(resultManager);

        logger.println("Finished");
    }
}
