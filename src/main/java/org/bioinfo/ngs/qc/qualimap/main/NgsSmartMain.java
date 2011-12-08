package org.bioinfo.ngs.qc.qualimap.main;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;

public class NgsSmartMain {
	
	/**
	 * @param args
	 * @throws Exception 
	 */

    public static void main(String[] args) throws Exception {
		Logger logger = new Logger();
        NgsSmartTool tool = null;

		if(args.length == 0){
			launchGUI(args,logger);
		} else {
						
			String toolName = args[0];

			// tools
			if(toolName.equalsIgnoreCase("bamqc")){
				tool = new BamQcTool();
			}
			
			if(toolName.equalsIgnoreCase("rna-seq")){
				tool = new RNAseqTool();
			}

            if (toolName.equalsIgnoreCase("run-tests")) {
                //runTests();
            } else if(toolName.equalsIgnoreCase("-h") || toolName.equalsIgnoreCase("-help") || toolName.equalsIgnoreCase("--h") || toolName.equalsIgnoreCase("--help")){
				logger.println("");
				logger.println(getHelp());
			} else {
				logger.println("");
				logger.println("Selected tool: " + toolName);
				if(tool==null){
                    launchGUI(args,logger);
				} else {
					try {					
						tool.run(args);
					} catch(ParseException pe){					
						logger.println("");
						logger.println("ERROR: " + pe.getMessage());
						logger.println("");
						tool.printHelp();
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void launchGUI(String[] args, Logger logger) throws ParseException{
		
		// getting home folder
		Options options = new Options();
		options.addOption("home", true, "home path");
		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = parser.parse(options, args, true);
		String home =  commandLine.getOptionValue("home");
		
		// launching GUI
		System.setProperty("java.awt.headless", "false");
		HomeFrame inst = new HomeFrame();
		if(home.endsWith("."))home=home.substring(0,home.length()-1);
		inst.setQualimapFolder(home);
		inst.setLocationRelativeTo(null);
		inst.setVisible(true);		
		
	}
	
	public static String getHelp() throws IOException{		
		InputStream resource = ClassLoader.getSystemResourceAsStream("org/bioinfo/ngs/qc/qualimap/help/main-help.txt");
		return IOUtils.toString(resource);	
	}

	public static void error(Logger logger, String message) throws IOException{
		logger.println("");
		logger.println(message);
		logger.println("");
		logger.println(getHelp());
	}
}