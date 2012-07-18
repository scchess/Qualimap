/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2012 Garcia-Alcalde et al.
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
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.EpiAnalysis;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;

import java.io.File;

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
        super(Constants.TOOL_NAME_CLUSTERING);
        cfg = new EpiAnalysis.Config();

    }

    @Override
    protected void initOptions() {

        options.addOption(requiredOption("sample", true, "comma-separated list of sample BAM files"));
        options.addOption(requiredOption("control", true, "comma-separated list of control BAM files"));
        options.addOption(requiredOption("regions", true, "path to regions file"));
        options.addOption("name", true , "comma-separated names of the replicates");
        options.addOption("l", true, "upstream offset (default is 2000)");
        options.addOption("r", true, "downstream offset (default is 500)");
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
        if (! (new File(cfg.pathToRegions)).exists()) {
            throw new ParseException("Regions file doesn't exists");
        }

        String[] samples = commandLine.getOptionValue(OPTION_NAME_SAMPLE).split(",");

        for (String sample : samples) {
            if (! (new File(sample)).exists()) {
                throw new ParseException("Sample file " + sample + " doesn't exists");
            }
        }

        String[] controls =  commandLine.getOptionValue(OPTION_NAME_CONTROL).split(",");

        for (String control : controls) {
            if (! (new File(control)).exists()) {
                throw new ParseException("Control file " + control + " doesn't exists");
            }
        }

        if (controls.length != samples.length) {
            throw new ParseException("Number of samples doesn't match number of controls");
        }

        int numReplicates = samples.length;


        String names[];

        if (commandLine.hasOption("name")) {
            names = commandLine.getOptionValue("name").split(",");
            if (names.length != numReplicates) {
                throw new ParseException("Number of names doesn't match number of replicates");
            }
        } else {
            names = new String[numReplicates];
            for (int i = 0; i < numReplicates; ++i ) {
                names[i] = "Sample " + (i + 1);
            }
        }

        for (int i = 0; i < numReplicates; ++i) {
            EpiAnalysis.ReplicateItem item = new EpiAnalysis.ReplicateItem();
            item.medipPath =  samples[i];
            item.inputPath = controls[i];
            item.name = names[i];
            cfg.replicates.add(item);
        }

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

        TabPropertiesVO tabProperties = new TabPropertiesVO(AnalysisType.CLUSTERING);

        EpiAnalysis epiAnalysis = new EpiAnalysis(tabProperties,homePath,cfg);

        LoggerThread loggerThread = new LoggerThread() {
            public void logLine(String msg) {
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
