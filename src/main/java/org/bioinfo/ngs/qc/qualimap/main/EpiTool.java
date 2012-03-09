package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.EpiAnalysis;
import org.bioinfo.ngs.qc.qualimap.utils.LoggerThread;

/**
 * Created by kokonech
 * Date: 1/27/12
 * Time: 12:14 PM
 */
public class EpiTool extends NgsSmartTool {

    static final String OPTION_NAME_SAMPLE = "sample";
    static final String OPTION_NAME_CONTROL = "control";
    static final String OPTION_NAME_REGIONS = "regions";

    EpiAnalysis.Config cfg;

    static String getVizTypes() {
        return Constants.VIZ_TYPE_HEATMAP + " or " + Constants.VIZ_TYPE_LINE;
    }

    public EpiTool() {
        super("epigenetics");
        cfg = new EpiAnalysis.Config();

    }

    @Override
    protected void initOptions() {

        options.addOption(requiredOption("sample", true, "path to sample BAM file"));
        options.addOption(requiredOption("control", true, "path to control BAM file"));
        options.addOption(requiredOption("regions", true, "path to regions file"));
        options.addOption("name", true , "name of the replicate");
        options.addOption("l", true, "left offset (default is 2000)");
        options.addOption("r", true, "right offset (default is 500)");
        options.addOption("b", "bin-size", true, "size of the bin (default is 100)");
        options.addOption("expr", true, "name of the experiment");
        options.addOption("c", "clusters", true, "comma-separated list of cluster sizes");
        options.addOption("f", "fragment-length", true, "smoothing length of a fragment");
        options.addOption("viz", true, "visualization type: " + getVizTypes());

    }

    @Override
    protected void checkOptions() throws ParseException {

        if (!commandLine.hasOption(OPTION_NAME_OUTDIR)) {
            throw new ParseException("Output dir is not set.");
        }

        cfg.pathToRegions = commandLine.getOptionValue(OPTION_NAME_REGIONS);

        EpiAnalysis.ReplicateItem item = new EpiAnalysis.ReplicateItem();
        item.medipPath =  commandLine.getOptionValue(OPTION_NAME_SAMPLE);
        item.inputPath = commandLine.getOptionValue(OPTION_NAME_CONTROL);
        if (commandLine.hasOption("name")) {
            item.name = commandLine.getOptionValue("name");
        } else {
            item.name = "Sample 1";
        }
        cfg.replicates.add(item);

        if (commandLine.hasOption("expr")) {
            cfg.experimentName = commandLine.getOptionValue("expr");
        }

        if (commandLine.hasOption("viz")) {
            cfg.vizType = commandLine.getOptionValue("viz");
        }

        if (commandLine.hasOption("l")) {
            cfg.leftOffset = Integer.parseInt(commandLine.getOptionValue("l"));
        }

        if (commandLine.hasOption("r")) {
            cfg.rightOffset = Integer.parseInt(commandLine.getOptionValue("r"));
        }

        if (commandLine.hasOption("b")) {
            cfg.binSize = Integer.parseInt(commandLine.getOptionValue("b"));
        }

        if (commandLine.hasOption("fl")) {
            cfg.fragmentLength = Integer.parseInt(commandLine.getOptionValue("fl"));
        }

        if (commandLine.hasOption("c")) {
            cfg.clusters = commandLine.getOptionValue("c").trim();
            if (cfg.clusters.split(",").length == 0) {
                throw new ParseException("Clusters are incorrectly formatted.");
            }
        }

    }

    @Override
    protected void execute() throws Exception {

        initOutputDir();

        TabPropertiesVO tabProperties = new TabPropertiesVO();
        tabProperties.setTypeAnalysis(Constants.TYPE_BAM_ANALYSIS_EPI);

        EpiAnalysis epiAnalysis = new EpiAnalysis(tabProperties,homePath,cfg);

        LoggerThread loggerThread = new LoggerThread() {
            protected void logLine(String msg) {
                System.out.println(msg);
            }
        };
        epiAnalysis.setOutputParsingThread(loggerThread);

        try {
            epiAnalysis.run();
        } catch (Exception e) {
            System.err.println("Failed to run analysis\n" + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        exportResult(tabProperties);

        System.out.println("Finished");

    }
}
