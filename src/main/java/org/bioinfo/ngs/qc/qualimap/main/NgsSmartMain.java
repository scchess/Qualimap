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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.ParseException;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;

public class NgsSmartMain {


    public static String APP_VERSION = "debug";


	public static void main(String[] args) throws Exception {
		Logger logger = new Logger();
        NgsSmartTool tool = null;

        tryLoadingAppProperties();

		if(args.length == 0 || args[0].equals("--home")){
			launchGUI(args);
		} else {
						
			String toolName = args[0];

			// TODO: use factories map to create tools
			// tools
			if(toolName.equalsIgnoreCase(Constants.TOOL_NAME_BAMQC)){
				tool = new BamQcTool();
			}
			
			if(toolName.equalsIgnoreCase(Constants.TOOL_NAME_RNA_SEQ)){
				tool = new RNAseqTool();
			}

            if (toolName.equals(Constants.TOOL_NAME_COMPUTE_COUNTS)) {
                tool = new CountReadsTool();
            }

            if (toolName.equals(Constants.TOOL_NAME_CLUSTERING)) {
                tool = new EpiTool();
            }

            if (toolName.equals(Constants.TOOL_NAME_GC_CONTENT)) {
                tool = new GCContentTool();
            }

            if (toolName.equals(Constants.TOOL_NAME_INDEL_COUNT)) {
                tool = new IndelCountTool();
            }

            if (toolName.equalsIgnoreCase("run-tests")) {
                System.out.println("Supposed to run tests...");
                //runTests();
            } else if(toolName.equalsIgnoreCase("-h") || toolName.equalsIgnoreCase("-help")
                    || toolName.equalsIgnoreCase("--h") || toolName.equalsIgnoreCase("--help")){
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

    private static void tryLoadingAppProperties()  {
        InputStream inStream = ClassLoader.getSystemClassLoader().getResourceAsStream("app.properties");
        if (inStream == null) {
            return;
        }

        Properties appProperties = new Properties();
        try {
            appProperties.load(inStream);
        } catch (IOException e) {
            System.err.println("Failed to load app propreties");
            return;
        }

        String version =  appProperties.get("app.version").toString();
        String timestamp =  appProperties.get("app.buildTime").toString();

        if (version != null) {
            System.out.println("QualiMap v." + appProperties.get("app.version").toString());
            APP_VERSION = "v." + version;
        }

        if (timestamp != null) {
            System.out.println("Built on " + appProperties.get("app.buildTime").toString());
        }
    }

    public static void launchGUI(String[] args) throws ParseException{
		

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
		return IOUtils.toString(resource)+"\n";
	}

	public static void error(Logger logger, String message) throws IOException{
		logger.println("");
		logger.println(message);
		logger.println("");
		logger.println(getHelp());
	}
}