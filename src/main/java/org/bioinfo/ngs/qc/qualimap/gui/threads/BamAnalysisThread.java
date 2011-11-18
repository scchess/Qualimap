package org.bioinfo.ngs.qc.qualimap.gui.threads;

import java.awt.Component;

import javax.swing.*;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.panels.OpenFilePanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.BamQCSplitted;
import org.bioinfo.ngs.qc.qualimap.process.BamStatsAnalysis;

/**
 * Class to manage a thread to do the Bam analysis to the input bam files and
 * load the results into graphics structures
 * 
 * @author Luis Miguel Cruz
 */
public class BamAnalysisThread extends Thread {
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

	/** Variables that contains the tab properties loaded in the thread */
	TabPropertiesVO tabProperties;

	public BamAnalysisThread(String str, Component component, TabPropertiesVO tabProperties) {
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
		//BamQCSplitted bamQC = null;

		// Show the ProgressBar and the Text Description
		openFilePanel.getProgressStream().setVisible(true);
		openFilePanel.getProgressBar().setVisible(true);

		// Create the outputDir directory
		StringBuilder outputDirPath = tabProperties.createDirectory();

		// Create a BamQCSplitted with a reference file or without it
		/*if (openFilePanel.getFastaFile() != null) {
			bamQC = new BamQCSplitted(openFilePanel.getInputFile().getAbsolutePath(), openFilePanel.getFastaFile().getAbsolutePath());
		} else {
			bamQC = new BamQCSplitted(openFilePanel.getInputFile().getAbsolutePath());
		}*/
        BamStatsAnalysis bamQC = new BamStatsAnalysis(openFilePanel.getInputFile().getAbsolutePath());

		// Set the number of windows
		if (!openFilePanel.getValueNw().getText().isEmpty()) {
			bamQC.setNumberOfWindows(Integer.parseInt(openFilePanel.getValueNw().getText()));
		} else {
			bamQC.setNumberOfWindows(Constants.DEFAULT_NUMBER_OF_WINDOWS);
		}

		// Set the region file
		if (openFilePanel.getRegionFile() != null) {
		//	bamQC.setSelectedRegions(openFilePanel.getRegionFile().getAbsolutePath());
		}

		// Put the gff variable to know if the user has added a region file only
		// if we are analyzing the exome
		tabProperties.setGffSelected(false);
		if (openFilePanel.getHomeFrame().getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {
			tabProperties.setGffSelected(true);
		}

		//bamQC.setComputeChromosomeStats(true);
		//bamQC.setComputeOutsideStats(true);


		// reporting
		//bamQC.activeReporting(outputDirPath.toString());

		openFilePanel.getProgressStream().setText("Starting bam qc....");

		try {
	        bamQC.run();
	        tabProperties.setPairedData(bamQC.isPairedData());
            tabProperties.setBamStats(bamQC.getBamStats());
            tabProperties.setGenomeLocator(bamQC.getLocator());

			// t.setThreadDone(true);
			openFilePanel.getProgressStream().setText("End of bam qc");
	
			// report
			openFilePanel.getProgressStream().setText("Computing report...");
			BamQCRegionReporter reporter = new BamQCRegionReporter();
	
			// Draw the Chromosome Limits or not
			reporter.setPaintChromosomeLimits(openFilePanel.getDrawChromosomeLimits().isSelected());
	
			openFilePanel.getProgressStream().setText("   text report...");
			reporter.loadReportData(bamQC.getBamStats());
			//increaseProgressBar(1.0, bamQC);
			openFilePanel.getProgressStream().setText("OK");
			tabProperties.setReporter(reporter);
	
			// Increment the pogress bar
			openFilePanel.getProgressStream().setText("   charts...");
			reporter.computeChartsBuffers(bamQC.getBamStats(), bamQC.getLocator(), bamQC.isPairedData());
			//increaseProgressBar(2.0, bamQC);
			openFilePanel.getProgressStream().setText("OK");
	
			// Set the reporter into the created tab
			tabProperties.setReporter(reporter);
	
			/*if (openFilePanel.getRegionFile() != null) {
				BamQCRegionReporter insideReporter = new BamQCRegionReporter();
				BamQCRegionReporter outsideReporter = new BamQCRegionReporter();
	
				// Draw the Chromosome Limits or not
				insideReporter.setPaintChromosomeLimits(openFilePanel.getDrawChromosomeLimits().isSelected());
				outsideReporter.setPaintChromosomeLimits(openFilePanel.getDrawChromosomeLimits().isSelected());
	
				// save stats
				openFilePanel.getProgressStream().setText("   inside text report...");
				insideReporter.loadReportData(bamQC.getInsideBamStats());
				openFilePanel.getProgressStream().setText("OK");
				increaseProgressBar(3.0, bamQC);
	
				// save charts
				openFilePanel.getProgressStream().setText("   inside charts...");
				insideReporter.computeChartsBuffers(bamQC.getInsideBamStats(), null, bamQC.isPairedData());
				openFilePanel.getProgressStream().setText("OK");
				increaseProgressBar(4.0, bamQC);
	
				// save stats
				openFilePanel.getProgressStream().setText("   outside text report...");
				outsideReporter.loadReportData(bamQC.getOutsideBamStats());
				openFilePanel.getProgressStream().setText("OK");
				increaseProgressBar(5.0, bamQC);
	
				// save charts
				openFilePanel.getProgressStream().setText("   outside charts...");
				outsideReporter.computeChartsBuffers(bamQC.getOutsideBamStats(), null, bamQC.isPairedData());
				openFilePanel.getProgressStream().setText("OK");
				increaseProgressBar(6.0, bamQC);
	
				// Set the reporters into the created tab
				tabProperties.setInsideReporter(insideReporter);
				tabProperties.setOutsideReporter(outsideReporter);
			} */
	
			// Increment the pogress bar
			openFilePanel.getProgressStream().setText("OK");
			openFilePanel.getProgressBar().setValue(100);
		} catch( OutOfMemoryError e) {
            JOptionPane.showMessageDialog(null, "<html><body align=\"center\">Out of memory!<br>Try increasing number of windows in Advanced Options" +
                    "<br>or changing Java virtual machine settings.</body></html>", "Calculate statistics", JOptionPane.ERROR_MESSAGE);
            resetOpenFilePanel();
            return;
        } catch (Exception e) {
		    JOptionPane.showMessageDialog(null, "Analysis is failed. Reason: " + e.getMessage(), "Calculate statistics", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            resetOpenFilePanel();
            return;
		}
		openFilePanel.getHomeFrame().addNewPane(openFilePanel,tabProperties);
	}

    private void resetOpenFilePanel() {
        openFilePanel.getProgressBar().setValue(0);
        openFilePanel.getProgressBar().setVisible(false);
        openFilePanel.getProgressStream().setText("");
        openFilePanel.getStartAnalysisButton().setEnabled(true);
    }
    /**
	 * Increase the progress bar in the percent depends on the number of the
	 * element computed.
	 * 
	 * @param numElem
	 *            number of the element computed
	 */
	private void increaseProgressBar(double numElem, BamQCSplitted bamQc) {
		int result = 0;
		if (openFilePanel.getRegionFile() != null) {
			result = (int) Math.ceil(numElem * (100.0 / 6.0));
		} else {
			result = (int) (numElem * (100 / 2));
		}
		result = (int) Math.round(result * 0.15);

		openFilePanel.getProgressBar().setValue((int) Math.round(bamQc.getProgress() * 0.85) + result);
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
