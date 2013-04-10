/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2013 Garcia-Alcalde et al.
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

import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.CountsAnalysis;


public class RNAseqTool extends NgsSmartTool{
	private String data1;
	private String data2;
	private String name1;
	private String name2;
	
	private String infoFile;

	private int k;
	
	public static String INFO_FILE_HUMAN_60;
	public static String GROUPS_FILE_HUMAN_60;
	public static String INFO_FILE_MOUSE_60;
	public static String GROUPS_FILE_MOUSE_60;
	public static int DEFAULT_NUMBER_OF_COUNTS = 5;
	public static String SCRIPT_R;
	private static final String SPECIES_FOLDER = "species";
	private static final String RFUNCTIONS_FOLDER = "scripts";
    private boolean secondSampleIsProvided;
	
	public RNAseqTool(){
		super(Constants.TOOL_NAME_RNA_SEQ);

        RNAseqTool.INFO_FILE_HUMAN_60 = homePath + File.separator + SPECIES_FOLDER +
                File.separator + Constants.FILE_SPECIES_INFO_HUMAN;
		RNAseqTool.GROUPS_FILE_HUMAN_60 = homePath + File.separator + SPECIES_FOLDER +
                File.separator + Constants.FILE_SPECIES_GROUPS_HUMAN;
		RNAseqTool.INFO_FILE_MOUSE_60 = homePath + File.separator + SPECIES_FOLDER +
                File.separator +Constants.FILE_SPECIES_INFO_MOUSE;
		RNAseqTool.GROUPS_FILE_MOUSE_60 = homePath + File.separator + SPECIES_FOLDER +
                File.separator  + Constants.FILE_SPECIES_GROUPS_MOUSE;

        RNAseqTool.SCRIPT_R = homePath + File.separator + RFUNCTIONS_FOLDER +
                File.separator + "qualimapRscript.r";

        secondSampleIsProvided = false;
        infoFile = "";
        k = DEFAULT_NUMBER_OF_COUNTS;
	}
	
	@Override
	protected void initOptions() {
		options.addOption(requiredOption("d1", "data1", true, "first file with counts"));
		options.addOption("d2", "data2", true, "second file with counts");
		options.addOption("n1", "name1", true, "name for the first sample");
		options.addOption("n2", "name2", true, "name for second sample");
		options.addOption("i", "info", true, "info file");
		options.addOption("s", "species", true, "use default file for the given species [human | mouse]");
		options.addOption("k", "threshold", true, "threshold for the number of counts");
	}
	
	@Override
	protected void checkOptions() throws ParseException {
		// check outdir
		if(!commandLine.hasOption(OPTION_NAME_OUTDIR)){
            throw new ParseException("output folder required");
        }

		// input
		if(!commandLine.hasOption("data1")){
			throw new ParseException("input counts file required");
		}else{
			data1 = commandLine.getOptionValue("data1");
			if(!exists(data1)) {
                throw new ParseException("input counts file (--data1) " + data1 + " not found");
            }
			
			name1 = "\"";
			if (commandLine.hasOption("name1")){
				name1 += commandLine.getOptionValue("name1");
			}else{
				name1 += "Sample 1";
			}
			name1 += "\"";
		}
		
		// data2
		if(commandLine.hasOption("data2")) {
			data2 = commandLine.getOptionValue("data2");
			if(!exists(data2)) throw new ParseException("input counts file (--data2) " + data2 + " not found");

			name2 = "\"";
			if (commandLine.hasOption("name2")){
				name2 += commandLine.getOptionValue("name2");
			} else{
				name2 += "Sample2";
			}
			name2 += "\"";
            secondSampleIsProvided = true;
		}
				
		// Info file
		if(commandLine.hasOption("info")) {
			infoFile = commandLine.getOptionValue("info");
			if(!exists(infoFile)) {
                throw new ParseException("file of information (--info) " + infoFile + " not found");
            }
		} else if(commandLine.hasOption("species")) {
			String species =  commandLine.getOptionValue("species");

            if(species.equalsIgnoreCase("human")){
                infoFile = INFO_FILE_HUMAN_60;
            }else if(species.equalsIgnoreCase("mouse")){
                infoFile = INFO_FILE_MOUSE_60;
            }else{
                throw new ParseException("species " + species + " not found. Please select [human | mouse]");
            }
        }

		
		// threshold for the number of counts 
		if(commandLine.hasOption("k")) {
			k = Integer.parseInt(commandLine.getOptionValue("k"));
		}
	}


	@Override
	protected void execute() throws Exception {
		// init output dir
		initOutputDir();

        TabPropertiesVO tabProperties = new TabPropertiesVO(AnalysisType.COUNTS_QC);

        CountsAnalysis countsAnalysis = new CountsAnalysis(tabProperties, homePath + File.separator);

        countsAnalysis.setSample1Name( name1 );
        countsAnalysis.setFirstSampleDataPath( data1 );

        if (secondSampleIsProvided) {
            countsAnalysis.setSecondSampleIsProvided(true);
            countsAnalysis.setSample2Name( name2 );
             countsAnalysis.setSecondSampleDataPath( data2 );
        }

        countsAnalysis.setThreshold( k );

        if (!infoFile.isEmpty()) {

            countsAnalysis.setInfoFilePath(infoFile);
        }


		try {
            countsAnalysis.run();
        } catch (Exception e) {
            System.err.println("Failed to analyze counts");
            e.printStackTrace();
            System.exit(-1);
        }


        exportResult(tabProperties);

        logger.println("Finished");
	}

}
