package org.bioinfo.ngs.qc.qualimap.main;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.cli.ParseException;
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

		if(args.length == 0 || args[0].equals("--home")){
			launchGUI(args,logger);
		} else {
						
			String toolName = args[0];

			// TODO: use factories map to create tools
			// tools
			if(toolName.equalsIgnoreCase("genomic")){
				tool = new BamQcTool();
			}
			
			if(toolName.equalsIgnoreCase("rna-seq")){
				tool = new RNAseqTool();
			}

            if (toolName.equals("counts")) {
                tool = new CountReadsTool();
            }

            if (toolName.equals("epi")) {
                tool = new EpiTool();
            }

            if (toolName.equalsIgnoreCase("run-tests")) {
                System.out.println("Supposed to run tests...");
                //runTests();
            } else if(toolName.equalsIgnoreCase("-h") || toolName.equalsIgnoreCase("-help") || toolName.equalsIgnoreCase("--h") || toolName.equalsIgnoreCase("--help")){
				logger.println("");
				logger.println(getHelp());
			} else {
				logger.println("");
				logger.println("Selected tool: " + toolName);
				if(tool==null){
                    logger.println("No proper tool name is provided.\n");
                    logger.println(getHelp());
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
		

		String qualimapHomeDir =  System.getenv("QUALIMAP_HOME");
        if ( args.length > 1 && args[0].equals("--home")) {
            qualimapHomeDir = args[1];
        }

        // launching GUI
        System.setProperty("java.awt.headless", "false");
        HomeFrame inst = new HomeFrame(qualimapHomeDir);

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