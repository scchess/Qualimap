package org.bioinfo.ngs.qc.qualimap.main;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.bioinfo.commons.log.Logger;

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
	
	
	public NgsSmartTool(){
		// log
		logger = new Logger();
		
		// environment
		homePath = System.getenv("QUALIMAP_HOME");
		if(homePath.endsWith(File.separator)){
			homePath = homePath.substring(0,homePath.length()-1);
		}
		// arguments
		options = new Options();
		parser = new PosixParser();
		
		initCommonOptions();
		
		initOptions();
	}
	
	private void initCommonOptions(){
		options.addOption("home", true, "qualimap folder");
		options.addOption("o", true, "output folder");
	}
	
	// init options
	protected abstract void initOptions();
	
	// parse options 
	protected void parse(String[] args) throws ParseException{
		// get command line
		commandLine = parser.parse(options, args);
	
		// fill common options
		if(commandLine.hasOption("o")){
			outdir = commandLine.getOptionValue("o");
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
		h.printHelp("ntools", options, true);
		logger.println("");
		logger.println("");
		logger.println("");
	}

	protected int parseInt(String value, String errorMessage) throws ParseException{
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException nfe){
			throw new ParseException(errorMessage); 
		}
	}
	
	protected double parseDouble(String value, String errorMessage) throws ParseException{
		try {
			return Double.parseDouble(value);
		} catch(NumberFormatException nfe){
			throw new ParseException(errorMessage); 
		}
	}
	
	protected void initOutputDir(){
		if(outdir==null){
			logger.error("output folder has not been provided");
		} else {
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
	
	/**
	 * @return the homePath
	 */
	public String getHomePath() {
		return homePath;
	}

	/**
	 * @param homePath the homePath to set
	 */
	public void setHomePath(String homePath) {
		this.homePath = homePath;
	}
}
