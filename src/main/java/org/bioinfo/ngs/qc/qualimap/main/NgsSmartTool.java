package org.bioinfo.ngs.qc.qualimap.main;

import java.io.File;
import java.util.jar.Attributes;

import org.apache.commons.cli.*;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.gui.threads.ExportHtmlThread;
import org.bioinfo.ngs.qc.qualimap.gui.threads.SavePdfThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
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
    protected boolean outDirIsRequired;

    static String OPTION_NAME_OUTDIR = "outdir";
    static String OPTION_NAME_HOMEDIR = "home";
    static String OPTION_NAME_OUTPUT_TYPE = "outformat";

	public NgsSmartTool(String toolName){

		this.toolName = toolName;
		// log
		logger = new Logger();
        outDirIsRequired = true;

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
		options.addOption(OPTION_NAME_HOMEDIR, true, "home folder of Qualimap");
		if (outDirIsRequired) {
            options.addOption( OPTION_NAME_OUTDIR, true, "output folder" );
        }

        options.addOption( OPTION_NAME_OUTPUT_TYPE, true, "output report format (PDF or HTML, default is HTML)");

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
            if (outputType != Constants.REPORT_TYPE_HTML && outputType != Constants.REPORT_TYPE_PDF) {
                throw new ParseException("Unknown output report format " + outputType);
            }
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
                new SavePdfThread(tabProperties, outdir + File.separator + "report.pdf") :
                new ExportHtmlThread(tabProperties, outdir);

         exportReportThread.run();
    }

}
