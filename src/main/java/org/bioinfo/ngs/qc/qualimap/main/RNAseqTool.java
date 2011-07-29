package org.bioinfo.ngs.qc.qualimap.main;

import java.io.File;

import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.utils.Rlauncher;
import org.bioinfo.tool.OptionFactory;


public class RNAseqTool extends NgsSmartTool{
	private String data1;
	private String data2;
	private String name1;
	private String name2;
	
	private String infoFile;
	private String groupsFile;
	
	private String species;
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
	
	public RNAseqTool(){
		RNAseqTool.INFO_FILE_HUMAN_60 = homePath + File.separator + SPECIES_FOLDER + File.separator +"human.61.genes.biotypes.txt";
		RNAseqTool.GROUPS_FILE_HUMAN_60 = homePath + File.separator + SPECIES_FOLDER + File.separator +"human.biotypes.groups.txt";
		RNAseqTool.INFO_FILE_MOUSE_60 = homePath + File.separator + SPECIES_FOLDER + File.separator +"mouse.61.genes.biotypes.txt";
		RNAseqTool.GROUPS_FILE_MOUSE_60 = homePath + File.separator + SPECIES_FOLDER + File.separator +"mouse.biotypes.groups.txt";
		RNAseqTool.SCRIPT_R = homePath + File.separator + RFUNCTIONS_FOLDER + File.separator + "qualimapRscript.r";
	}
	
	@Override
	protected void initOptions() {
		//options.addOption("gui", false, "open gui");
		options.addOption(OptionFactory.createOption("outdir", "o", "output folder", true, true));
		options.addOption("d1", "data1", true, "First file with counts");
		options.addOption("d2", "data2", true, "Second file with counts");
		options.addOption("n1", "name1", true, "Name for the first sample");
		options.addOption("n2", "name2", true, "Name for second sample");
		options.addOption("i", "info", true, "Info file.");
		options.addOption("s", "species", true, "Use default files for the given species [human | mouse]");
		options.addOption("g", "groups", false, "If passed biotypes will be grouped accordingly to the groping file.");
		options.addOption("k", "countsThreshold", true, "Threshold for the number of counts");
	}
	
	@Override
	protected void checkOptions() throws ParseException {
		// check outdir
		//if(!commandLine.hasOption("o")) throw new ParseException("output folder required");
		
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
		}
				
		// Info file
		if(commandLine.hasOption("info")) {
			infoFile = commandLine.getOptionValue("info");
			if(!exists(infoFile)) throw new ParseException("file of information (--info) " + infoFile + " not found");
		}
		
		// Groups file
//		if(commandLine.hasOption("groups")) {
//			groupsFile = commandLine.getOptionValue("groups");
//			if(!exists(groupsFile)) throw new ParseException("file of groups (--groups) " + groupsFile + " not found");
//			
//		}
		
		if(commandLine.hasOption("species")) {
			species =  commandLine.getOptionValue("species");
			
			if(species.equalsIgnoreCase("human")){
				infoFile = INFO_FILE_HUMAN_60;
				if(commandLine.hasOption("groups")){
					groupsFile =  GROUPS_FILE_HUMAN_60;
				}
			}else{
				if(species.equalsIgnoreCase("mouse")){
					infoFile = INFO_FILE_MOUSE_60;
					if(commandLine.hasOption("groups")){
						groupsFile =  GROUPS_FILE_HUMAN_60;
					}
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
		
		// create command
		composeCommand();
		//System.out.println(this.cmd);
		
		System.out.println("Hola");
		Rlauncher R = new Rlauncher(cmd, "Rscript");
		System.out.println("Adios");
		R.run();
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
		if (commandLine.hasOption("groups")) addArgument("--groups", groupsFile);
		if (commandLine.hasOption("k")) addArgument("-k", String.valueOf(k));
		if (commandLine.hasOption("o")) addArgument("-o", outdir);
		
		return cmd;
	}
	
	
	protected void addArgument(String name, String value){
		this.cmd = this.cmd + " " + name + " " + value;
	}
}
