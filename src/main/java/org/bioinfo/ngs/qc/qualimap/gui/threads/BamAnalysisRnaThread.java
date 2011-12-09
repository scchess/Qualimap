package org.bioinfo.ngs.qc.qualimap.gui.threads;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.bioinfo.commons.exec.Command;
import org.bioinfo.commons.exec.SingleProcess;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.panels.OpenFilePanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.RNAAnalysisVO;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StringUtilsSwing;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

/**
 * Class to manage a thread that do the analysis from RNA-Seq of the input files
 * 
 * @author Luis Miguel Cruz
 */
public class BamAnalysisRnaThread extends Thread {
	/**
	 * Variable to manage the return string loaded for the thread of the
	 * statistics each moment.
	 */
	private String processedString;

	/** Variable to manage the percent of statistics loaded each moment */
	private Double loadPercent;

	/** Logger to print information */
	protected Logger logger;

	/** Variable to manage the panel with the progressbar at the init */
	private OpenFilePanel openFilePanel;

	/** Variable to control the percent of each iteration of the progress bar */
	private double percentLoad;

	/**
	 * Variable to control number of differents steps to load before showing the
	 * new tab. This variable is used to control the percent loaded
	 */
	private double numStepsRnaToDo;

	/** Variable to control the current step loaded */
	private int currentStepLoaded;

	/** Variables that contains the tab properties loaded in the thread */
	TabPropertiesVO tabProperties;

	public BamAnalysisRnaThread(String str, Component component, TabPropertiesVO tabProperties) {
		super(str);
		this.processedString = null;
		this.loadPercent = new Double(0.0);
		this.currentStepLoaded = 0;
		if (component instanceof OpenFilePanel) {
			this.openFilePanel = (OpenFilePanel) component;
		}
		this.tabProperties = tabProperties;
		logger = new Logger(this.getClass().getName());
	}

	/**
	 * Public method to run this thread. Its executed when an user call to
	 * method start over this thread.
	 */
	public void run() {
			// Show the ProgressBar and the Text Description
			openFilePanel.getProgressStream().setVisible(true);
			openFilePanel.getProgressBar().setVisible(true);

			RNAAnalysisVO rnaAnalysisVO = tabProperties.getRnaAnalysisVO();
			// Create the outputDir directory
			StringBuilder outputDirPath = tabProperties.createDirectory();

			increaseProgressBar(currentStepLoaded, "Building Rscript sentence");

			// Create the string to execute
			String command = "Rscript " + openFilePanel.getHomeFrame().getQualimapFolder() + "scripts"+File.separator+"qualimapRscript.r";

			command += " --homesrc " + openFilePanel.getHomeFrame().getQualimapFolder() + "scripts";
			command += " --data1 " + openFilePanel.getPathDataFile().getText();
			command += " --name1 " + openFilePanel.getName1().getText().replace(" ", "_");

			if (!openFilePanel.getPathCountFile2().getText().trim().isEmpty()) {
				command += " --data2 " + openFilePanel.getPathCountFile2().getText();
				command += " --name2 " + openFilePanel.getName2().getText().replace(" ", "_");
			}

			if (!openFilePanel.getPathInfoFile().getText().trim().isEmpty()) {
				command += " --info " + openFilePanel.getPathInfoFile().getText();
			}

			String pathSpecie = null;

			if (rnaAnalysisVO.getSpecieFileIsSet()) {
				pathSpecie = openFilePanel.getHomeFrame().getQualimapFolder() + "species";

				command += " --info ";
				if (openFilePanel.getComboSpecies().getSelectedItem().toString().equalsIgnoreCase(Constants.TYPE_COMBO_SPECIES_HUMAN)) {
					command += pathSpecie + File.separator + Constants.FILE_SPECIES_INFO_HUMAN;
					// command += " --groups " + pathSpecie + "/" +
					// Constants.FILE_SPECIES_GROUPS_HUMAN;
				} else if (openFilePanel.getComboSpecies().getSelectedItem().toString().equalsIgnoreCase(Constants.TYPE_COMBO_SPECIES_MOUSE)) {
					command += pathSpecie + File.separator + Constants.FILE_SPECIES_INFO_MOUSE;
					// command += " --groups " + pathSpecie + "/" +
					// Constants.FILE_SPECIES_GROUPS_MOUSE;
				}
			}

			command += " -o " + outputDirPath;

			String pathInfoFile = "";
			boolean infoFileSelected = false;
			if (rnaAnalysisVO.getInfoFileIsSet()) {
				pathInfoFile = openFilePanel.getPathInfoFile().getText();
				infoFileSelected = true;
			} else if (rnaAnalysisVO.getSpecieFileIsSet()) {
				if (openFilePanel.getComboSpecies().getSelectedItem().toString().equalsIgnoreCase(Constants.TYPE_COMBO_SPECIES_HUMAN)) {
					pathInfoFile = openFilePanel.getHomeFrame().getQualimapFolder() + "species"+File.separator + Constants.FILE_SPECIES_INFO_HUMAN;
				} else if (openFilePanel.getComboSpecies().getSelectedItem().toString().equalsIgnoreCase(Constants.TYPE_COMBO_SPECIES_MOUSE)) {
					pathInfoFile = openFilePanel.getHomeFrame().getQualimapFolder() + "species"+File.separator + Constants.FILE_SPECIES_INFO_MOUSE;
				}
				infoFileSelected = true;
			}

			// If the info file is selected (or species is selected), load
			// different classes found in it into a map
			if (infoFileSelected) {
				FileReader fr;
                try {
	                fr = new FileReader(pathInfoFile);
               
					BufferedReader br = new BufferedReader(fr);
	
					String line;
					while ((line = br.readLine()) != null) {
						String[] words = line.trim().split("\t");
						if (words == null || words.length != 2) {
	
						} else {
							if (!rnaAnalysisVO.getMapClassesInfoFile().containsKey(words)) {
								rnaAnalysisVO.getMapClassesInfoFile().put(words[1], words[1] + ".png");
							}
						}
					}
                } catch (FileNotFoundException e) {
	               e.printStackTrace();
                } catch (IOException e) {
	               e.printStackTrace();
                }
			}

			if (tabProperties.getRnaAnalysisVO().getMapClassesInfoFile() != null && tabProperties.getRnaAnalysisVO().getMapClassesInfoFile().size() > 0) {
				this.numStepsRnaToDo = 5 + tabProperties.getRnaAnalysisVO().getMapClassesInfoFile().size();
			} else {
				this.numStepsRnaToDo = 3;
			}
			percentLoad = (100.0 / numStepsRnaToDo);

			increaseProgressBar(currentStepLoaded, "Running Rscript sentence");
			Command cmd = new Command(command);
			System.out.println(command);
			SingleProcess process = new SingleProcess(cmd);
			process.getRunnableProcess().run();
			try {
	            loadBufferedImages();
            } catch (IOException e) {
	            e.printStackTrace();
            }
            String inputFileName = openFilePanel.getInputFile().getName();
            openFilePanel.getHomeFrame().addNewPane(inputFileName, tabProperties);
	}

	/**
	 * Function to load the images into a map of buffered images
	 * 
	 * @throws IOException
	 */
	private void loadBufferedImages() throws IOException {
		BamQCRegionReporter reporter = tabProperties.getReporter();
		reporter.setImageMap(new HashMap<String, BufferedImage>());
		increaseProgressBar(currentStepLoaded, "Loading graphic: Global Saturation");
		// Insert in the tab the graphic of the Global Saturations
		addImage(reporter,Constants.GRAPHIC_NAME_RNA_GLOBAL_SATURATION);
		
		Iterator it = tabProperties.getRnaAnalysisVO().getMapClassesInfoFile().entrySet().iterator();
		// If a info_file or species is selected, add the Saturation per class,
		// the counts per class and the graphics of each biotype
		if (it.hasNext()) {
			increaseProgressBar(currentStepLoaded, "Loading graphic: Detection per Class");
			addImage(reporter,Constants.GRAPHIC_NAME_RNA_SATURATION_PER_CLASS);
			increaseProgressBar(currentStepLoaded, "Loading graphic: Counts per Class");
			addImage(reporter,Constants.GRAPHIC_NAME_RNA_COUNTS_PER_CLASS);
			
			while (it.hasNext()) {
				Map.Entry<String, String> aux = (Map.Entry<String, String>) it.next();
				increaseProgressBar(currentStepLoaded, "Loading graphic: " + aux.getKey());
				addImage(reporter,aux.getValue());
				addImage(reporter,aux.getKey()+"_boxplot.png");
			}
			addImage(reporter,"unknown.png");
			addImage(reporter,"unknown_boxplot.png");			
		}
	}
	
	private void addImage(BamQCRegionReporter reporter, String name){
		String path = HomeFrame.outputpath + tabProperties.getOutputFolder().toString() + name;
		BufferedImage imageToDisplay = null;
        try {
	        imageToDisplay = ImageIO.read(new FileInputStream(new File(path)));
			reporter.getImageMap().put(name, imageToDisplay);
        } catch (FileNotFoundException e) {
	       System.out.println("Image not found: " + path );
        } catch (IOException e) {
        	System.out.println("Image not found: " + path );
        }
	}

	/**
	 * Increase the progress bar in the percent depends on the number of the
	 * element computed.
	 * 
	 * @param numElem
	 *            number of the element computed
	 */
	private void increaseProgressBar(double numElem, String action) {
		int result = 0;

		// Increase the number of files loaded
		currentStepLoaded++;
		// Increase the progress bar value
		result = (int) Math.ceil(numElem * percentLoad);
		openFilePanel.getProgressBar().setValue(result);
		if (action != null) {
			openFilePanel.getProgressStream().setText(action);
		}
	}

	// ******************************************************************************************
	// ********************************* GETTERS / SETTERS
	// **************************************
	// ******************************************************************************************
	public String getProcessedString() {
		return processedString;
	}

	public void setProcessedString(String processedString) {
		this.processedString = processedString;
	}

	public Double getLoadPercent() {
		return loadPercent;
	}

	public void setLoadPercent(Double loadPercent) {
		this.loadPercent = loadPercent;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}
