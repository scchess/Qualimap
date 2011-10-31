package org.bioinfo.ngs.qc.qualimap.gui.threads;

import java.awt.Component;
import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.panels.SavePanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

/**
 * Class to manage a thread taht save the loaded data into a Zip file
 * 
 * @author Luis Miguel Cruz
 */
public class SaveZipThread extends Thread {
	/** Logger to print information */
	protected Logger logger;

	/** Variable to manage the panel with the progress bar to increase */
	private SavePanel savePanel;

	/** Variable to control that all the files are saved */
	private int numSavedFiles;

	/** Variable to control the percent of each iteration of the progress bar */
	private double percentLoad;

	/** Variable to manage the path of the file that we are going to save */
	private String path;

	/** Variables that contains the tab properties loaded in the thread */
	TabPropertiesVO tabProperties;

	public SaveZipThread(String str, Component component, TabPropertiesVO tabProperties, String path) {
		super(str);
		if (component instanceof SavePanel) {
			this.savePanel = (SavePanel) component;
		}
		this.tabProperties = tabProperties;
		this.path = path;
	}

	/**
	 * Public method to run this thread. Its executed when an user call to
	 * method start over this thread.
	 */
	public void run() {
		try {
			FileOutputStream outputStream = new FileOutputStream(path);
			ZipOutputStream zipFile = new ZipOutputStream(outputStream);

			// Show the ProgressBar and the Text Description
			savePanel.getProgressStream().setVisible(true);
			savePanel.getProgressBar().setVisible(true);

			// Set the number of files saved to initial value
			numSavedFiles = 0;

			boolean success = false;

			if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_DNA) == 0) {
				// Number of graphics + 3 files of properties
				int numFilesToSave = tabProperties.getReporter().getMapCharts().size() + 1;

				percentLoad = (100.0 / numFilesToSave);

				// Add the files of the first reporter
				BamQCRegionReporter reporter = tabProperties.getReporter();

				success = addFilesToZip(zipFile, reporter, Constants.NAME_OF_PROPERTIES_IN_ZIP_FILE, tabProperties);
			} else if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {
				int numFilesToSave = tabProperties.getInsideReporter().getMapCharts().size() + tabProperties.getOutsideReporter().getMapCharts().size() + 2;

				percentLoad = (100.0 / numFilesToSave);

				// Add the files of the inside region
				BamQCRegionReporter reporter = tabProperties.getInsideReporter();
				success = addFilesToZip(zipFile, reporter, Constants.NAME_OF_INSIDE_PROPERTIES_IN_ZIP_FILE, tabProperties);

				// Add the files of the third reporter
				if (success) {
					reporter = tabProperties.getOutsideReporter();
					success = addFilesToZip(zipFile, reporter, Constants.NAME_OF_OUTSIDE_PROPERTIES_IN_ZIP_FILE, tabProperties);
				}
			} else if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_RNA) == 0) {
				int numFilesToSave = 2;
				Map<String, Object> mapGenotypes = tabProperties.getRnaAnalysisVO().getMapClassesInfoFile();
				if (mapGenotypes != null && mapGenotypes.size() > 0) {
					numFilesToSave += mapGenotypes.size();
				}

				percentLoad = (100.0 / numFilesToSave);

				// Add the files of the inside region
				BamQCRegionReporter reporter = tabProperties.getReporter();

				success = addFilesToZip(zipFile, reporter, Constants.NAME_OF_PROPERTIES_IN_ZIP_FILE, tabProperties);
			}

			try {
				zipFile.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Can not create the zip File \n", "Error", 0);
			}

			if (success) {
				// Close the window and show an info message
				savePanel.getHomeFrame().getPopUpDialog().setVisible(false);
				savePanel.getHomeFrame().remove(savePanel.getHomeFrame().getPopUpDialog());
				JOptionPane.showMessageDialog(null, "Zip File Created Successfully \n", "Success", JOptionPane.INFORMATION_MESSAGE);
			} else {
				// If the file could not generate correctly
				File f = new File(path);
				f.delete();
				JOptionPane.showMessageDialog(null, "Unable to create the zip file \n", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Unable to create the zip file \n", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Function that adds the files of a reporter into the zip file
	 * 
	 * @param zipFile
	 *            contains the zip file that the system is generating
	 * @param reporter
	 *            information to put in the zip file
	 * @param namePropFile
	 *            name of the file of properties to generate
	 * @param tabProperties
	 *            properties of the tab selected
	 * @return result
     *            true if file is saved successfully
	 */
	private boolean addFilesToZip(ZipOutputStream zipFile, BamQCRegionReporter reporter, String namePropFile, TabPropertiesVO tabProperties) {
		boolean result = true;
		//BufferedImage bufImage = null;
		String fileName = namePropFile;

        try {
            Properties prop = new Properties();
            generatePropertiesFile(prop, reporter, tabProperties);
            zipFile.putNextEntry(new ZipEntry(namePropFile));
			prop.store(zipFile, null);
			increaseProgressBar(numSavedFiles, namePropFile);

			if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_DNA) == 0 || tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {
				// Generate the Chromosomes file to compress (only if we aren't
				// processing the regions)
				if (namePropFile.equalsIgnoreCase(Constants.NAME_OF_PROPERTIES_IN_ZIP_FILE)) {
					File file = new File(HomeFrame.outputpath + tabProperties.getOutputFolder() + Constants.NAME_OF_FILE_CHROMOSOMES);
					FileInputStream in = new FileInputStream(file); // Stream to
																	// read file
					zipFile.putNextEntry(new ZipEntry(Constants.NAME_OF_FILE_CHROMOSOMES)); // Store
																							// entry
					int bytesRead;
					byte[] buffer = new byte[4096]; // Create a buffer for
													// copying
					while ((bytesRead = in.read(buffer)) != -1) {
						zipFile.write(buffer, 0, bytesRead);
					}
					in.close();
				}
			}

            //Map<String,JFreeChart> chartMap = reporter.getMapCharts();

            zipFile.putNextEntry(new ZipEntry(Constants.NAME_OF_GENOME_LOCATOR_IN_ZIP_FILE));
            ObjectOutputStream out1 = new ObjectOutputStream(zipFile);
            out1.writeObject(tabProperties.getGenomeLocator());

            zipFile.putNextEntry(new ZipEntry(Constants.NAME_OF_BAM_STATS_IN_ZIP_FILE));
            ObjectOutput out2 = new ObjectOutputStream(zipFile);
            out2.writeObject(tabProperties.getBamStats());

            // TODO: use this code for export to html
            //for (Map.Entry<String, JFreeChart> entry : chartMap.entrySet() ) {
            //if (entry.getValue() instanceof JFreeChart) {
				//	bufImage = ((JFreeChart) entry.getValue()).createBufferedImage(Constants.GRAPHIC_TO_SAVE_WIDTH, Constants.GRAPHIC_TO_SAVE_HEIGHT);
				//}
                /*
                else {
					bufImage = (BufferedImage) entry.getValue();
				} */
				//ImageIO.write(bufImage, fileName.substring(fileName.lastIndexOf(".") + 1), zipFile);
			    //increaseProgressBar(numSavedFiles, fileName);
			//}
			savePanel.getProgressBar().setValue(100);
		} catch (IOException e) {
			result = false;
			savePanel.getProgressBar().setVisible(false);
			savePanel.getProgressStream().setVisible(false);
			JOptionPane.showMessageDialog(null, "Can not create the zip file (error compressing the file) " + fileName + " \n", "Error", 0);
		}

		return result;
	}


    public static void generateBamQcProperties(Properties prop, BamQCRegionReporter reporter) {
            prop.setProperty("bamFileName", reporter.getBamFileName());
			prop.setProperty("basesNumber", reporter.getBasesNumber().toString());
			prop.setProperty("contigsNumber", reporter.getContigsNumber().toString());

			if (reporter.getReferenceFileName() != null && !reporter.getReferenceFileName().isEmpty()) {
				prop.setProperty("referenceFileName", reporter.getReferenceFileName());
				prop.setProperty("aReferenceNumber", reporter.getaReferenceNumber().toString());
				prop.setProperty("aReferencePercent", reporter.getaReferencePercent().toString());
				prop.setProperty("cReferenceNumber", reporter.getcReferenceNumber().toString());
				prop.setProperty("cReferencePercent", reporter.getcReferencePercent().toString());
				prop.setProperty("tReferenceNumber", reporter.gettReferenceNumber().toString());
				prop.setProperty("tReferencePercent", reporter.gettReferencePercent().toString());
				prop.setProperty("gReferenceNumber", reporter.getgReferenceNumber().toString());
				prop.setProperty("gReferencePercent", reporter.getgReferencePercent().toString());
				prop.setProperty("nReferenceNumber", reporter.getnReferenceNumber().toString());
				prop.setProperty("nReferencePercent", reporter.getnReferencePercent().toString());
			}

			prop.setProperty("gcPercent", reporter.getGcPercent().toString());
			//prop.setProperty("atPercent", reporter.getAtPercent().toString());

			// globals
			prop.setProperty("numWindows", reporter.getNumWindows().toString());
			prop.setProperty("numReads", reporter.getNumReads().toString());
			prop.setProperty("numMappedReads", reporter.getNumMappedReads().toString());
			prop.setProperty("percentMappedReads", reporter.getPercentMappedReads().toString());
			prop.setProperty("numMappedBases", reporter.getNumMappedBases().toString());
			prop.setProperty("numSequencedBases", reporter.getNumSequencedBases().toString());
			prop.setProperty("numAlignedBases", reporter.getNumAlignedBases().toString());

			// mapping quality
			prop.setProperty("meanMappingQuality", reporter.getMeanMappingQuality().toString());

			// actg content
			prop.setProperty("aNumber", reporter.getaNumber().toString());
			prop.setProperty("aPercent", reporter.getaPercent().toString());
			prop.setProperty("cNumber", reporter.getcNumber().toString());
			prop.setProperty("cPercent", reporter.getcPercent().toString());
			prop.setProperty("tNumber", reporter.gettNumber().toString());
			prop.setProperty("tPercent", reporter.gettPercent().toString());
			prop.setProperty("gNumber", reporter.getgNumber().toString());
			prop.setProperty("gPercent", reporter.getgPercent().toString());
			prop.setProperty("nNumber", reporter.getnNumber().toString());
			prop.setProperty("nPercent", reporter.getnPercent().toString());
			prop.setProperty("gcPercent", reporter.getGcPercent().toString());
			//TODO: add relative content?
			//prop.setProperty("atPercent", reporter.getAtPercent().toString());

			// coverage
			prop.setProperty("meanCoverage", reporter.getMeanCoverage().toString());
			prop.setProperty("stdCoverage", reporter.getStdCoverage().toString());
    }


	/**
	 * Function that fill the properties file
	 * 
	 * @param prop
	 *            properties file
	 * @param reporter
	 *            with the input information
	 * @param tabProperties
	 *            properties of the tab selected
	 */
	private void generatePropertiesFile(Properties prop, BamQCRegionReporter reporter, TabPropertiesVO tabProperties) {
		prop.setProperty("typeAnalysis", tabProperties.getTypeAnalysis().toString());
        prop.setProperty("isPairedData", tabProperties.isPairedData() ? "true" : "false");

		if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_DNA) == 0 || tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {
			generateBamQcProperties(prop, reporter);
		} else if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_RNA) == 0) {
			prop.setProperty("infoFileSelected", tabProperties.getRnaAnalysisVO().getInfoFileIsSet().toString());
			prop.setProperty("speciesFileSelected", tabProperties.getRnaAnalysisVO().getSpecieFileIsSet().toString());
		}

		Set<String> chartNames = reporter.getMapCharts().keySet();
        Iterator iter = chartNames.iterator();

        if (!chartNames.isEmpty()) {
			String s = "";
			while (iter.hasNext()) {
				s += iter.next();
				if (iter.hasNext()) {
					s += ",";
				}
			}
			prop.setProperty("mapGraphicNames", s);
		}
	}

	/**
	 * Increase the progress bar in the percent depends on the number of the
	 * element computed.
	 * 
	 * @param numElem
	 *            number of the element computed
	 */
	private void increaseProgressBar(double numElem, String fileName) {
		// Increase the number of files loaded
		numSavedFiles++;
		// Increase the progress bar value
		int result = (int) Math.ceil(numElem * percentLoad);
		savePanel.getProgressBar().setValue(result);
		if (fileName != null) {
			savePanel.getProgressStream().setText("Compressing File: " + fileName);
		}
	}
}
