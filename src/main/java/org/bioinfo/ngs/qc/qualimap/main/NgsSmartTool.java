package org.bioinfo.ngs.qc.qualimap.main;

import java.io.File;

import EDU.oswego.cs.dl.util.concurrent.FJTask;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.tool.OptionFactory;

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
    protected boolean outDirIsRequired;

	
	public NgsSmartTool(String toolName){

		this.toolName = toolName;
		// log
		logger = new Logger();
        outDirIsRequired = true;
		
		// environment
		homePath = System.getenv("QUALIMAP_HOME");
        if (homePath == null) {
            homePath = "";
        }
		if(homePath.endsWith(File.separator)){
			homePath = homePath.substring(0,homePath.length()-1);
		}

		// arguments
		options = new Options();
		parser = new PosixParser();
		outdir = "";

		initCommonOptions();
		
		initOptions();
	}
	
	private void initCommonOptions(){
		options.addOption(OptionFactory.createOption("home", "Qualimap home folder", false, true));
		if (outDirIsRequired) {
            options.addOption( OptionFactory.createOption("outdir", "Output folder", false, true) );
        }
	}
	
	// init options
	protected abstract void initOptions();
	
	// parse options 
	protected void parse(String[] args) throws ParseException{
		// get command line
		commandLine = parser.parse(options, args);
	
		// fill common options
		if(commandLine.hasOption("outdir")){
			outdir = commandLine.getOptionValue("outdir");
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
		h.setWidth(150);		
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
				new File(outdir).mkdirs();
			}
		}
	}
	
	protected boolean exists(String fileName){
		return new File(fileName).exists();
	}

}
