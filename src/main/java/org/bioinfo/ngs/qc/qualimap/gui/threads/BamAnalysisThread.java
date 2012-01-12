package org.bioinfo.ngs.qc.qualimap.gui.threads;

import java.util.TimerTask;
import java.util.Timer;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.panels.BamAnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.BamQCSplitted;
import org.bioinfo.ngs.qc.qualimap.process.BamStatsAnalysis;

import javax.swing.*;

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
	private BamAnalysisDialog bamDialog;

	/** Variables that contains the tab properties loaded in the thread */
	TabPropertiesVO tabProperties;

    private static class UpdateProgressTask extends TimerTask {
        JProgressBar progressBar;
        BamStatsAnalysis bamQC;
        UpdateProgressTask(BamStatsAnalysis bamQC, JProgressBar progressBar) {
            this.bamQC = bamQC;
            this.progressBar = progressBar;
        }

        @Override
        public void run() {
            progressBar.setValue(bamQC.getProgress());
        }
    }

	public BamAnalysisThread(String str, BamAnalysisDialog bamDialog, TabPropertiesVO tabProperties) {
		super(str);
		this.processedString = null;
		this.loadPercent = 0.0;
		this.bamDialog = bamDialog;
        this.tabProperties = tabProperties;
		logger = new Logger(this.getClass().getName());
	}

	/**
	 * Public method to run this thread. Its executed when an user call to
	 * method start over this thread.
	 */
	public void run() {
		//BamQCSplitted bamQC = null;

		// Create the outputDir directory
		StringBuilder outputDirPath = tabProperties.createDirectory();

		// Create a BamQCSplitted with a reference file or without it
		/*if (bamDialog.getFastaFile() != null) {
			bamQC = new BamQCSplitted(bamDialog.getInputFile().getAbsolutePath(), bamDialog.getFastaFile().getAbsolutePath());
		} else {
			bamQC = new BamQCSplitted(bamDialog.getInputFile().getAbsolutePath());
		}*/
        BamStatsAnalysis bamQC = new BamStatsAnalysis(bamDialog.getInputFile().getAbsolutePath());

		// Set the number of windows
		bamQC.setNumberOfWindows(bamDialog.getNumberOfWindows());

		// Set the region file
		if (bamDialog.getRegionFile() != null) {
			bamQC.setSelectedRegions(bamDialog.getRegionFile().getAbsolutePath());
            bamQC.setComputeOutsideStats(bamDialog.getComputeOutsideRegions());
		}

		// Put the gff variable to know if the user has added a region file only
		// if we are analyzing the exome
		tabProperties.setGffSelected(tabProperties.getTypeAnalysis() == Constants.TYPE_BAM_ANALYSIS_EXOME);
        tabProperties.setOutsideStatsAvailable(bamDialog.getComputeOutsideRegions());

		bamQC.setComputeChromosomeStats(true);
		//bamQC.setComputeOutsideStats(true);

		// reporting
		bamQC.activeReporting(outputDirPath.toString());

		Timer timer = new Timer(true);
        timer.schedule( new UpdateProgressTask(bamQC, bamDialog.getProgressBar()), 100, 1000);

        bamDialog.setUiEnabled(false);
        bamDialog.getProgressStream().setText("Running BAM file analysis...");

		try {

            bamQC.run();
	        timer.cancel();

            tabProperties.setPairedData(bamQC.isPairedData());
            tabProperties.setBamStats(bamQC.getBamStats());
            tabProperties.setGenomeLocator(bamQC.getLocator());

			bamDialog.getProgressStream().setText("End of bam qc");
            bamDialog.getProgressBar().setValue(100);
	
			// report
			bamDialog.getProgressStream().setText("Computing report...");
			BamQCRegionReporter reporter = new BamQCRegionReporter();
	
			// Draw the Chromosome Limits or not
			reporter.setPaintChromosomeLimits(bamDialog.getDrawChromosomeLimits());
	
			bamDialog.getProgressStream().setText("   text report...");
			reporter.loadReportData(bamQC.getBamStats());
			//increaseProgressBar(1.0, bamQC);
			bamDialog.getProgressStream().setText("OK");
			tabProperties.setReporter(reporter);
	
			// Increment the pogress bar
			bamDialog.getProgressStream().setText("   charts...");
			reporter.computeChartsBuffers(bamQC.getBamStats(), bamQC.getLocator(), bamQC.isPairedData());
			//increaseProgressBar(2.0, bamQC);
			bamDialog.getProgressStream().setText("OK");
	
			// Set the reporter into the created tab
			tabProperties.setReporter(reporter);
	
			if ( bamDialog.getRegionFile() != null && bamDialog.getComputeOutsideRegions() ) {
				//BamQCRegionReporter insideReporter = new BamQCRegionReporter();
				BamQCRegionReporter outsideReporter = new BamQCRegionReporter();
	
				// Draw the Chromosome Limits or not
				//insideReporter.setPaintChromosomeLimits(bamDialog.getDrawChromosomeLimits());
				outsideReporter.setPaintChromosomeLimits(bamDialog.getDrawChromosomeLimits());
	
				// save stats
				//bamDialog.getProgressStream().setText("   inside text report...");
				//insideReporter.loadReportData(bamQC.getBamStats());
				//bamDialog.getProgressStream().setText("OK");
				//increaseProgressBar(3.0, bamQC);
	
				// save charts
				//bamDialog.getProgressStream().setText("   inside charts...");
				//insideReporter.computeChartsBuffers(bamQC.getBamStats(), null, bamQC.isPairedData());
				//bamDialog.getProgressStream().setText("OK");
				//increaseProgressBar(4.0, bamQC);
	
				// save stats
				bamDialog.getProgressStream().setText("   outside text report...");
				outsideReporter.loadReportData(bamQC.getOutsideBamStats());
				bamDialog.getProgressStream().setText("OK");
				//increaseProgressBar(5.0, bamQC);
	
				// save charts
				bamDialog.getProgressStream().setText("   outside charts...");
				outsideReporter.computeChartsBuffers(bamQC.getOutsideBamStats(), bamQC.getLocator(), bamQC.isPairedData());
				bamDialog.getProgressStream().setText("OK");
				//increaseProgressBar(6.0, bamQC);
	
				// Set the reporters into the created tab
				//tabProperties.setInsideReporter(insideReporter);
				tabProperties.setOutsideReporter(outsideReporter);
			}
	
			// Increment the pogress bar
			bamDialog.getProgressStream().setText("OK");
			bamDialog.getProgressBar().setValue(100);
		} catch( OutOfMemoryError e) {
            JOptionPane.showMessageDialog(null, "<html><body align=\"center\">Out of memory!<br>Try increasing number of windows in Advanced Options" +
                    "<br>or changing Java virtual machine settings.</body></html>", "Calculate statistics", JOptionPane.ERROR_MESSAGE);
            bamDialog.setUiEnabled(true);
            return;
        } catch (Exception e) {
		    JOptionPane.showMessageDialog(null, "Analysis is failed. Reason: " + e.getMessage(), "Calculate statistics", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            bamDialog.setUiEnabled(true);
            return;
		}
		bamDialog.addNewPane(tabProperties);
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
		if (bamDialog.getRegionFile() != null) {
			result = (int) Math.ceil(numElem * (100.0 / 6.0));
		} else {
			result = (int) (numElem * (100 / 2));
		}
		result = (int) Math.round(result * 0.15);

		bamDialog.getProgressBar().setValue((int) Math.round(bamQc.getProgress() * 0.85) + result);
	}


}
