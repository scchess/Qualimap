package org.bioinfo.ngs.qc.qualimap.main;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.threads.EpigeneticsAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.threads.ExportHtmlThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.tool.OptionFactory;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by kokonech
 * Date: 1/27/12
 * Time: 12:14 PM
 */
public class EpiTool extends NgsSmartTool {

    String cfgFile, vizType;

    static String getVizTypes() {
        return Constants.VIZ_TYPE_HEATMAP + " or " + Constants.VIZ_TYPE_LINE;
    }

    public EpiTool() {
        super("epigenomics");
    }

    @Override
    protected void initOptions() {

        options.addOption(OptionFactory.createOption("cfg", "Configuration file", true, true) );
		options.addOption(OptionFactory.createOption("viz","Visualization type: " + getVizTypes(),false, true));

    }

    @Override
    protected void checkOptions() throws ParseException {
        cfgFile = commandLine.getOptionValue("cfg");
        if (!exists(cfgFile)) {
            throw new ParseException("Configuration file is not found.");
        }

        if (commandLine.hasOption("viz")) {
            vizType = commandLine.getOptionValue("viz");
        } else {
            vizType = Constants.VIZ_TYPE_HEATMAP;
        }



    }

    @Override
    protected void execute() throws Exception {


        // Create the command to execute
        String commandString = "Rscript " + homePath
                + File.separator +  "scripts"+ File.separator + "paintLocation.r";

        commandString += " --fileConfig=" + cfgFile;
        commandString += " --homedir=" + homePath + File.separator + "scripts";
        commandString += " --vizType=" + vizType;

        System.out.println(commandString);
        System.out.println();

        final Process p = Runtime.getRuntime().exec(commandString);
        Thread outputReader = new Thread(new Runnable() { public void run() {
            try {
                IOUtils.copy(p.getInputStream(), System.out);
            } catch (IOException e) {
                System.err.println("Failed to redirect process output");
            }
        } } );
        outputReader.start();
        p.waitFor();
        outputReader.join();

        System.out.println("Finished");

    }
}
