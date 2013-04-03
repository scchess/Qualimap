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

import java.io.File;

import org.apache.commons.cli.*;
import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.common.AppSettings;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.threads.ExportHtmlThread;
import org.bioinfo.ngs.qc.qualimap.gui.threads.ExportPdfThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

public abstract class NgsSmartTool {
	
	// log
	protected Logger logger;
	
	// environment
	protected String homePath;
	
	// arguments
	protected Options options;
	protected CommandLine commandLine;
	protected CommandLineParser parser;
	
	// common params
	protected String outdir;
    protected String toolName;
    protected String outputType;
    protected boolean outDirIsRequired,outFormatIsRequired, homeDirIsRequired;

    static String OPTION_NAME_OUTDIR = "outdir";
    static String OPTION_NAME_HOMEDIR = "home";
    static String OPTION_NAME_OUTPUT_TYPE = "outformat";
    static String OPTION_NAME_PATH_TO_RSCRIPT = "rscriptpath";

	public NgsSmartTool(String toolName){

		this.toolName = toolName;
		this.outDirIsRequired = true;
        this.outFormatIsRequired = true;
        init();
    }

    public NgsSmartTool(String toolName, boolean outDirIsRequired, boolean outFormatIsRequired){

		this.toolName = toolName;
		this.outDirIsRequired = outDirIsRequired;
        this.outFormatIsRequired = outFormatIsRequired;
        init();
    }



    public void init() {

        // log
        logger = new Logger();

        // environment
		homePath = System.getenv("QUALIMAP_HOME");
        if (homePath == null) {
            homePath = new File("").getAbsolutePath() + File.separator;
        }
		if(homePath.endsWith(File.separator)){
			homePath = homePath.substring(0,homePath.length()-1);
		}

		// arguments
		options = new Options();
		parser = new PosixParser();
		outdir = "";
        outputType = Constants.REPORT_TYPE_HTML;

		initCommonOptions();

		initOptions();

    }
	
	private void initCommonOptions(){
		if (homeDirIsRequired) {
            options.addOption(OPTION_NAME_HOMEDIR, true, "home folder of Qualimap");
        }

        if (outDirIsRequired) {
            options.addOption( OPTION_NAME_OUTDIR, true, "output folder" );
        }
        if (outFormatIsRequired) {
            options.addOption( OPTION_NAME_OUTPUT_TYPE, true, "output report format (PDF or HTML, default is HTML)");
        }

        options.addOption( OPTION_NAME_PATH_TO_RSCRIPT, true, "path to Rscript executable (by default it is assumed " +
                "to be available from system $PATH)" );
	}
	
	// init options
	protected abstract void initOptions();
	
	// parse options 
	protected void parse(String[] args) throws ParseException{
		// get command line
		commandLine = parser.parse(options, args);

        // fill common options
		if(commandLine.hasOption(OPTION_NAME_OUTDIR)){
			outdir = commandLine.getOptionValue(OPTION_NAME_OUTDIR);
		}


        if (commandLine.hasOption(OPTION_NAME_OUTPUT_TYPE)) {
            outputType = commandLine.getOptionValue(OPTION_NAME_OUTPUT_TYPE);
            if (!outputType.equals(Constants.REPORT_TYPE_HTML) && !outputType.equals(Constants.REPORT_TYPE_PDF)) {
                throw new ParseException("Unknown output report format " + outputType);
            }
        }

        if (commandLine.hasOption(OPTION_NAME_PATH_TO_RSCRIPT)) {
            String pathToRScript = commandLine.getOptionValue(OPTION_NAME_PATH_TO_RSCRIPT);
            if (! (new File(pathToRScript).canExecute()) ) {
                throw new ParseException("Wrong path to RScript command: " + pathToRScript);
            }
            AppSettings.getGlobalSettings().setPathToRScript(pathToRScript);
        }


	}
	
	// check options
	protected abstract void checkOptions() throws ParseException;
	
	// execute tool
	protected abstract void execute() throws Exception;
	
	
	// public run (parse and execute)
	public void run(String[] args) throws Exception{
		// parse
		parse(args);
		
		// check options
		checkOptions();
		
		// execute
		execute();
	}
	
	protected void printHelp(){
		HelpFormatter h = new HelpFormatter();
		h.setWidth(80);
		h.printHelp("qualimap " + toolName, options, true);
		logger.println("");
		logger.println("");
		logger.println("");
	}

	protected void initOutputDir(){

        if(!outdir.isEmpty()){
        	if(new File(outdir).exists()){
				logger.warn("output folder already exists");
			} else {
				boolean ok = new File(outdir).mkdirs();
                if (!ok) {
                    logger.error("Failed to create output directory.");
                }
			}
		}
	}

    protected static Option requiredOption(String shortName, String longName, boolean hasArgument, String shortDescription ) {
            Option option = new Option(shortName, longName, hasArgument, shortDescription);
            option.setRequired(true);
            return option;
    }


    protected static Option requiredOption(String shortName, boolean hasArgument, String shortDescription ) {
        Option option = new Option(shortName, null, hasArgument, shortDescription);
        option.setRequired(true);
        return option;
    }

	protected boolean exists(String fileName){
		return new File(fileName).exists();
	}

    protected void exportResult(TabPropertiesVO tabProperties) {
         Thread exportReportThread = outputType.equals( Constants.REPORT_TYPE_PDF ) ?
                new ExportPdfThread(tabProperties, outdir + File.separator + "report.pdf") :
                new ExportHtmlThread(tabProperties, outdir);

         exportReportThread.run();
    }

}
