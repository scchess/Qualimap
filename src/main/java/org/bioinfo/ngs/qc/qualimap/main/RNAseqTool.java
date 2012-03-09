package org.bioinfo.ngs.qc.qualimap.main;

import java.io.File;

import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.CountsAnalysis;
import org.bioinfo.ngs.qc.qualimap.utils.Rlauncher;
import org.bioinfo.tool.OptionFactory;


public class RNAseqTool extends NgsSmartTool{
	private String data1;
	private String data2;
	private String name1;
	private String name2;
	
	private String infoFile;

	private int k;
	
	private String cmd;

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
		super("rna-seq");
        RNAseqTool.INFO_FILE_HUMAN_60 = homePath + File.separator + SPECIES_FOLDER + File.separator +"human.61.genes.biotypes.txt";
		RNAseqTool.GROUPS_FILE_HUMAN_60 = homePath + File.separator + SPECIES_FOLDER + File.separator +"human.biotypes.groups.txt";
		RNAseqTool.INFO_FILE_MOUSE_60 = homePath + File.separator + SPECIES_FOLDER + File.separator +"mouse.61.genes.biotypes.txt";
		RNAseqTool.GROUPS_FILE_MOUSE_60 = homePath + File.separator + SPECIES_FOLDER + File.separator +"mouse.biotypes.groups.txt";
		RNAseqTool.SCRIPT_R = homePath + File.separator + RFUNCTIONS_FOLDER + File.separator + "qualimapRscript.r";
	    secondSampleIsProvided = false;
        infoFile = "";
    }
	
	@Override
	protected void initOptions() {
		options.addOption("d1", "data1", true, "First file with counts");
		options.addOption("d2", "data2", true, "Second file with counts");
		options.addOption("n1", "name1", true, "Name for the first sample");
		options.addOption("n2", "name2", true, "Name for second sample");
		options.addOption("i", "info", true, "Info file.");
		options.addOption("s", "species", true, "Use default files for the given species [human | mouse]");
		options.addOption("k", "threshold", true, "Threshold for the number of counts");
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
			if(!exists(data1)) throw new ParseException("input counts file (--data1) " + data1 + " not found");
			
			name1 = "\"";
			if (commandLine.hasOption("name1")){
				name1 += commandLine.getOptionValue("name1");
			}else{
				name1 += "Sample1";
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
			if(!exists(infoFile)) throw new ParseException("file of information (--info) " + infoFile + " not found");
		} else if(commandLine.hasOption("species")) {
			String species =  commandLine.getOptionValue("species");
			
			if(species.equalsIgnoreCase("human")){
				infoFile = INFO_FILE_HUMAN_60;
			}else{
				if(species.equalsIgnoreCase("mouse")){
					infoFile = INFO_FILE_MOUSE_60;
				}else{
					throw new ParseException("species " + species + " not found. Please select [human | mouse]");
				}
			}
		}
		
		// threshold for the number of counts 
		k = DEFAULT_NUMBER_OF_COUNTS;
		if(commandLine.hasOption("k")) {
			k = Integer.parseInt(commandLine.getOptionValue("k"));
		}
	}


	@Override
	protected void execute() throws Exception {
		// init output dir
		initOutputDir();

        TabPropertiesVO tabProperties = new TabPropertiesVO();
        tabProperties.setTypeAnalysis(Constants.TYPE_BAM_ANALYSIS_RNA);

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
	

	protected String composeCommand(){
		this.cmd = SCRIPT_R;
		addArgument("--data1", data1);
		
		// Add the path of the R script
		addArgument("--homesrc", homePath + "/" + RFUNCTIONS_FOLDER);
		
		if (commandLine.hasOption("name1"))addArgument("--name1", name1);
		if (commandLine.hasOption("data2")) addArgument("--data2", data2);
		if (commandLine.hasOption("name2")) addArgument("--name2", name2);
		if (commandLine.hasOption("info") || commandLine.hasOption("species")) addArgument("--info", infoFile);
		if (commandLine.hasOption("k")) addArgument("-k", String.valueOf(k));
		if (commandLine.hasOption("o")) addArgument("-o", outdir);
		
		return cmd;
	}
	
	
	protected void addArgument(String name, String value){
		this.cmd = this.cmd + " " + name + " " + value;
	}
}
