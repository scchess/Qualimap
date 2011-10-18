package org.bioinfo.ngs.qc.qualimap.gui.threads;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats;
import org.bioinfo.ngs.qc.qualimap.beans.GenomeLocator;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.panels.OpenFilePanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StringUtilsSwing;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;

/**
 * Class to load the graphic Images from a Zip file into the GUI application
 * 
 * @author Luis Miguel Cruz
 */
public class GraphicsFromZipAnalysisThread extends Thread {
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

	/** Variables that contains the name of the graphic image in each report */
	private Map<String, String> reportFileNames, insideReportFileNames, outsideReportFileNames;

	/** Variables that contains the tab properties loaded in the thread */
	TabPropertiesVO tabProperties;

	/** Variable to control that all the files are loaded */
	private int numLoadedFiles;

	/** Variable to control the percent of each iteration of the progress bar */
	private double percentLoad;

	/** Variable to control the number of files in the zip file */
	private double numFilesInZip;

	public GraphicsFromZipAnalysisThread(String str, Component component, TabPropertiesVO tabProperties) {
		super(str);
		this.processedString = null;
		this.loadPercent = new Double(0.0);
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
		String zipFileName = openFilePanel.getPathDataFile().getText().substring(openFilePanel.getPathDataFile().getText().lastIndexOf("/") + 1);

		try {
			FileInputStream zipFile = new FileInputStream(openFilePanel.getPathDataFile().getText());
			ZipInputStream zip = new ZipInputStream(new BufferedInputStream(zipFile));
			ZipEntry entry;
			int numOfDirs = 0;
			numLoadedFiles = 0;
			tabProperties.createDirectory();
			numFilesInZip = 0;

			// Count the number of files in the ZIP file
			while ((entry = zip.getNextEntry()) != null) {
				numFilesInZip++;
			}

			// We select the properties files to extract the properties values
			// (some of them needed to extract the number
			// of graphics)
			zipFile = new FileInputStream(openFilePanel.getPathDataFile().getText());
			zip = new ZipInputStream(new BufferedInputStream(zipFile));
			percentLoad = (100.0 / numFilesInZip);

			while ((entry = zip.getNextEntry()) != null && numOfDirs < 2) {
				if (!entry.isDirectory()) {
					String fileName = entry.getName();
					String[] s = fileName.split("/");

					if (s.length > 1) {
						fileName = s[s.length - 1];
					}

					// Fill the map that contains the files to load and the
					// percent loaded
					if (fileName.equalsIgnoreCase(Constants.NAME_OF_PROPERTIES_IN_ZIP_FILE)) {
						insertTextIntoReporter(tabProperties, entry, fileName, tabProperties.getReporter());
						increaseProgressBar(numLoadedFiles, fileName);
					} else if (fileName.equalsIgnoreCase(Constants.NAME_OF_INSIDE_PROPERTIES_IN_ZIP_FILE)) {
						insertTextIntoReporter(tabProperties, entry, fileName, tabProperties.getInsideReporter());
						increaseProgressBar(numLoadedFiles, fileName);
					} else if (fileName.equalsIgnoreCase(Constants.NAME_OF_OUTSIDE_PROPERTIES_IN_ZIP_FILE)) {
						insertTextIntoReporter(tabProperties, entry, fileName, tabProperties.getOutsideReporter());
						increaseProgressBar(numLoadedFiles, fileName);
					}
				} else {
					numOfDirs++;
				}
			}

			if (numOfDirs >= 2) {
				JOptionPane.showMessageDialog(null, " • Incorrect Zip Structure", "Error", 0);
			} else {
				zipFile = new FileInputStream(openFilePanel.getPathDataFile().getText());
				zip = new ZipInputStream(new BufferedInputStream(zipFile));

				while ((entry = zip.getNextEntry()) != null && numOfDirs < 2) {
					if (!entry.isDirectory()) {
						if (entry.getName().equals(Constants.NAME_OF_GENOME_LOCATOR_IN_ZIP_FILE)){
                            ObjectInputStream in = new ObjectInputStream(zip);
                            try {
                                GenomeLocator locator = (GenomeLocator) in.readObject();
                                tabProperties.setGenomeLocator(locator);
                                increaseProgressBar(numLoadedFiles, entry.getName());
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else if (entry.getName().equals(Constants.NAME_OF_BAM_STATS_IN_ZIP_FILE)) {
                            ObjectInputStream in = new ObjectInputStream(zip);
                            try {
                                BamStats bamStats = (BamStats) in.readObject();
                                tabProperties.setBamStats(bamStats);
                                increaseProgressBar(numLoadedFiles, entry.getName());
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            loadFile(tabProperties, entry);
                        }
					} else {
						numOfDirs++;
					}
				}
			}
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, " • Cannot open the Zip File", "Error", 0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, " • Can not read the Zip File entry", "Error", 0);
		} finally {
			try {
				if (!allFilesLoaded(tabProperties)) {
					// Show the ProgressBar and the Text Description
					openFilePanel.getProgressStream().setVisible(false);
					openFilePanel.getProgressBar().setVisible(false);

					JOptionPane.showMessageDialog(null, " • Cannot load all the needed files, review the zip file structure", "Error", 0);
				} else {
                    openFilePanel.getHomeFrame().setTypeAnalysis(tabProperties.getTypeAnalysis());
                    BamStats bamStats = tabProperties.getBamStats();
                    GenomeLocator locator = tabProperties.getGenomeLocator();
                    tabProperties.getReporter().computeChartsBuffers(bamStats,locator,tabProperties.isPairedData());
                    openFilePanel.getHomeFrame().addNewPane(openFilePanel, tabProperties);
				}
				// Stop the thread
				join();
			} catch (InterruptedException e) {
				logger.error("Unable to sleep the principal thread while the statistics are generated");
			} catch (IOException e) {
                logger.error("Unable to compute chart buffers");
            }
        }
	}

	/**
	 * Evaluate if the number of loaded files is the correct number of files or
	 * not
	 * 
	 * @param tabProperties
	 *            Properties where we have the loaded files
	 * @return
	 */
	private boolean allFilesLoaded(TabPropertiesVO tabProperties) {
		Integer size = null;
		boolean result = false;

		if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_DNA) == 0) {
			size = 4; //tabProperties.getReporter().getMapCharts().size() + 2;
		} else if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {
			size = tabProperties.getInsideReporter().getMapCharts().size() + tabProperties.getOutsideReporter().getMapCharts().size() + 2;
		} else if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_RNA) == 0) {
			size = tabProperties.getReporter().getMapCharts().size() + 1;
		}

		if (size != null && size.compareTo(numLoadedFiles) == 0) {
			result = true;
		}

		return result;
	}

	/**
	 * Load the graphic file into the reporter
	 * 
	 * @param tabProperties
	 *            Object that contains the tab graphs and data
	 * @param entry
	 *            file read from the zip file
	 */
	private void loadFile(TabPropertiesVO tabProperties, ZipEntry entry) {
		String fileName = entry.getName();
		String[] s = fileName.split("/");

		if (s.length > 1) {
			fileName = s[s.length - 1];
		}

        if (fileName.equalsIgnoreCase(Constants.NAME_OF_FILE_CHROMOSOMES)) {
			createFileIntoFolder(tabProperties, entry, fileName);
            increaseProgressBar(numLoadedFiles, fileName);
        }

		//TODO: check this stuff carefully
		/*if (fileName.equalsIgnoreCase(Constants.NAME_OF_FILE_CHROMOSOMES)) {
			if (fileName.equalsIgnoreCase(Constants.NAME_OF_FILE_CHROMOSOMES)) {
				createFileIntoFolder(tabProperties, entry, fileName);
			} else {
				insertGraphIntoReporter(tabProperties, entry, fileName, tabProperties.getReporter());
			}
			increaseProgressBar(numLoadedFiles, fileName);
		} else if (tabProperties.getInsideReporter().getMapCharts() != null && tabProperties.getInsideReporter().getMapCharts().containsKey(fileName)) {
			tabProperties.setGffSelected(true);
			if (!fileName.equalsIgnoreCase(Constants.NAME_OF_INSIDE_PROPERTIES_IN_ZIP_FILE)) {
				insertGraphIntoReporter(tabProperties, entry, fileName, tabProperties.getInsideReporter());
			}
			increaseProgressBar(numLoadedFiles, fileName);
		} else if (tabProperties.getOutsideReporter().getMapCharts() != null && tabProperties.getOutsideReporter().getMapCharts().containsKey(fileName)) {
			tabProperties.setGffSelected(true);
			if (!fileName.equalsIgnoreCase(Constants.NAME_OF_OUTSIDE_PROPERTIES_IN_ZIP_FILE)) {
				insertGraphIntoReporter(tabProperties, entry, fileName, tabProperties.getOutsideReporter());
			}
			increaseProgressBar(numLoadedFiles, fileName);
		} */
	}

	/**
	 * Load the graphic file into the reporter with the name received
	 * 
	 * @param tabProperties
	 *            Object that contains the tab graphs and data
	 * @param entry
	 *            file read from the zip file.
	 * @param graphicName
	 *            , String with the graphic name.
	 * @param reporter
	 *            , BamQCRegionReporter with contains the reporter of the
	 *            graphic images.
	 */
	private void insertGraphIntoReporter(TabPropertiesVO tabProperties, ZipEntry entry, String graphicName, BamQCRegionReporter reporter) {
		try {

            ZipFile zipFile = new ZipFile(openFilePanel.getPathDataFile().getText());
            ObjectInputStream in = new ObjectInputStream(zipFile.getInputStream(entry));
            String str = (String) in.readObject();
            System.out.println(str);
            Plot plot = (Plot) in.readObject();
            if (reporter.getMapCharts() == null) {
                reporter.setMapCharts(new HashMap<String, JFreeChart>());
            }

            reporter.getMapCharts().put(graphicName, new JFreeChart(plot));
			//BufferedImage image = ImageIO.read(bis);

			/*if (reporter.getMap() == null) {
				reporter.setImageMap(new HashMap<String, BufferedImage>());
			}
			reporter.getImageMap().put(graphicName, image);*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load the data information from the properties file into the reporter with
	 * the name received
	 * 
	 * @param tabProperties
	 *            Object that contains the tab graphs and data
	 * @param entry
	 *            file read from the zip file.
	 * @param fileName
	 *            , String with the file name.
	 * @param reporter
	 *            , BamQCRegionReporter with contains the reporter of the
	 *            graphic images.
	 */
	private void insertTextIntoReporter(TabPropertiesVO tabProperties, ZipEntry entry, String fileName, BamQCRegionReporter reporter) {
		Properties prop = new Properties();
		StringUtilsSwing stringUtils = new StringUtilsSwing();
		try {
			ZipFile zipFile = new ZipFile(openFilePanel.getPathDataFile().getText());
			prop.load(new BufferedInputStream(zipFile.getInputStream(entry)));

			// Set the type of the tab analysis
			tabProperties.setTypeAnalysis(stringUtils.parseInt(prop.getProperty("typeAnalysis")));
            tabProperties.setPairedData( stringUtils.parseBool(prop.getProperty("isPairedData")));

			// Put the names of the graphics images into a map
			String listaNombreMapas = prop.getProperty("mapGraphicNames");
			String[] mapaNombres = listaNombreMapas.split(",");
			//TODO: check this
            reporter.setMapCharts(new HashMap<String, JFreeChart>());
			for (int i = 0; i < mapaNombres.length; i++) {
				reporter.getMapCharts().put(mapaNombres[i], null);
			}

			if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_RNA) == 0) {
				tabProperties.getRnaAnalysisVO().setInfoFileIsSet(Boolean.parseBoolean(prop.getProperty("infoFileSelected")));
				tabProperties.getRnaAnalysisVO().setSpecieFileIsSet(Boolean.parseBoolean(prop.getProperty("speciesFileSelected")));
				
				Map<String, Object> mapGraphicNames = new HashMap<String, Object>();
				String nameFile;

				// Fill the map of names of the graphic images
				Iterator<?> it = reporter.getMapCharts().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, Object> entryMap = (Map.Entry<String, Object>) it.next();
					nameFile = entryMap.getKey();
					if (!nameFile.endsWith("_boxplot.png") && !nameFile.equalsIgnoreCase(Constants.GRAPHIC_NAME_RNA_GLOBAL_SATURATION) && !nameFile.equalsIgnoreCase(Constants.GRAPHIC_NAME_RNA_SATURATION_PER_CLASS) && !nameFile.equalsIgnoreCase(Constants.GRAPHIC_NAME_RNA_COUNTS_PER_CLASS)) {
						mapGraphicNames.put(entryMap.getKey().substring(0, entryMap.getKey().length() - 4), entryMap.getKey());
					}
				}

				tabProperties.getRnaAnalysisVO().setMapClassesInfoFile(mapGraphicNames);
			}

			reporter.setBamFileName(prop.getProperty("bamFileName"));

			reporter.setBasesNumber(stringUtils.parseLong(prop.getProperty("basesNumber")));
			reporter.setContigsNumber(stringUtils.parseLong(prop.getProperty("contigsNumber")));

			reporter.setReferenceFileName(prop.getProperty("referenceFileName"));
			if (reporter.getReferenceFileName() != null) {
				reporter.setaReferenceNumber(stringUtils.parseLong(prop.getProperty("aReferenceNumber")));
				reporter.setaReferencePercent(stringUtils.parseDouble(prop.getProperty("aReferencePercent")));
				reporter.setcReferenceNumber(stringUtils.parseLong(prop.getProperty("cReferenceNumber")));
				reporter.setcReferencePercent(stringUtils.parseDouble(prop.getProperty("cReferencePercent")));
				reporter.settReferenceNumber(stringUtils.parseLong(prop.getProperty("tReferenceNumber")));
				reporter.settReferencePercent(stringUtils.parseDouble(prop.getProperty("tReferencePercent")));
				reporter.setgReferenceNumber(stringUtils.parseLong(prop.getProperty("gReferenceNumber")));
				reporter.setgReferencePercent(stringUtils.parseDouble(prop.getProperty("gReferencePercent")));
				reporter.setnReferenceNumber(stringUtils.parseLong(prop.getProperty("nReferenceNumber")));
				reporter.setnReferencePercent(stringUtils.parseDouble(prop.getProperty("nReferencePercent")));

				reporter.setGcPercent(stringUtils.parseDouble(prop.getProperty("gcPercent")));
				reporter.setAtPercent(stringUtils.parseDouble(prop.getProperty("atPercent")));
			}

			// globals
			reporter.setNumWindows(stringUtils.parseInt(prop.getProperty("numWindows")));
			reporter.setNumReads(stringUtils.parseLong(prop.getProperty("numReads")));
			reporter.setNumMappedReads(stringUtils.parseInt(prop.getProperty("numMappedReads")));
			reporter.setPercentMappedReads(stringUtils.parseDouble(prop.getProperty("percentMappedReads")));
			reporter.setNumMappedBases(stringUtils.parseLong(prop.getProperty("numMappedBases")));
			reporter.setNumSequencedBases(stringUtils.parseLong(prop.getProperty("numSequencedBases")));
			reporter.setNumAlignedBases(stringUtils.parseLong(prop.getProperty("numAlignedBases")));

			// mapping quality
			reporter.setMeanMappingQuality(stringUtils.parseDouble(prop.getProperty("meanMappingQuality")));

			// actg content
			reporter.setaNumber(stringUtils.parseLong(prop.getProperty("aNumber")));
			reporter.setaPercent(stringUtils.parseDouble(prop.getProperty("aPercent")));
			reporter.setcNumber(stringUtils.parseLong(prop.getProperty("cNumber")));
			reporter.setcPercent(stringUtils.parseDouble(prop.getProperty("cPercent")));
			reporter.settNumber(stringUtils.parseLong(prop.getProperty("tNumber")));
			reporter.settPercent(stringUtils.parseDouble(prop.getProperty("tPercent")));
			reporter.setgNumber(stringUtils.parseLong(prop.getProperty("gNumber")));
			reporter.setgPercent(stringUtils.parseDouble(prop.getProperty("gPercent")));
			reporter.setnNumber(stringUtils.parseLong(prop.getProperty("nNumber")));
			reporter.setnPercent(stringUtils.parseDouble(prop.getProperty("nPercent")));
			reporter.setGcPercent(stringUtils.parseDouble(prop.getProperty("gcPercent")));
			reporter.setAtPercent(stringUtils.parseDouble(prop.getProperty("atPercent")));

			// coverage
			reporter.setMeanCoverage(stringUtils.parseDouble(prop.getProperty("meanCoverage")));
			reporter.setStdCoverage(stringUtils.parseDouble(prop.getProperty("stdCoverage")));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, " • Property File Not Found", "Error", 0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, " • Error reading the properties file", "Error", 0);
		}
	}

	/**
	 * Uncompress a File and create a new File into the folder that manage tab
	 * selected with the necessary information.
	 * 
	 * @param tabProperties
	 *            Object that contains the tab graphs and data
	 * @param entry
	 *            file read from the zip file.
	 * @param fileName
	 *            , String with the file name.
	 */
	private void createFileIntoFolder(TabPropertiesVO tabProperties, ZipEntry entry, String fileName) {
		try {
			ZipFile zipFile = new ZipFile(openFilePanel.getPathDataFile().getText());
			InputStream inputStream = zipFile.getInputStream(entry);

			FileOutputStream outstream = new FileOutputStream(HomeFrame.outputpath + tabProperties.getOutputFolder() + fileName);
			int n;
			byte[] buf = new byte[1024];

			while ((n = inputStream.read(buf, 0, 1024)) > -1) {
				outstream.write(buf, 0, n);
			}

			outstream.close();
            inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Increase the progress bar in the percent depends on the number of the
	 * element computed.
	 * 
	 * @param numElem
	 *            number of the element computed
	 * @param fileName
	 *            name of the file that is loading
	 */
	private void increaseProgressBar(double numElem, String fileName) {
		int result = 0;

		// Increase the number of files loaded
		numLoadedFiles++;
		// Increase the progress bar value
		result = (int) Math.ceil(numElem * percentLoad);
		openFilePanel.getProgressBar().setValue(result);
		if (fileName != null) {
			openFilePanel.getProgressStream().setText("Loading File: " + fileName);
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
