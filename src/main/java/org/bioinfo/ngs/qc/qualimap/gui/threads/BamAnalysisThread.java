/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2014 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.gui.threads;

import net.sf.samtools.SAMFormatException;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.panels.BamAnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.gui.utils.AnalysisDialogLoggerThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPageController;
import org.bioinfo.ngs.qc.qualimap.process.BamStatsAnalysis;

import javax.swing.*;

/**
 * Class to manage a thread to do the Bam analysis to the input bam files and
 * calculate resulting charts
 * 
 * @author kokonech
 */
public class BamAnalysisThread extends Thread {

	/** Logger to print information */
	protected Logger logger;

	/** Variable to manage the panel with the progressbar at the init */
	private BamAnalysisDialog bamDialog;

	/** Variables that contains the tab properties loaded in the thread */
    TabPageController resultManager;

	public BamAnalysisThread(String str, BamAnalysisDialog bamDialog, TabPageController tabProperties) {
		super(str);
		this.bamDialog = bamDialog;
        this.resultManager = tabProperties;
		logger = new Logger(this.getClass().getName());
	}

	/**
	 * Public method to run this thread. Its executed when an user call to
	 * method start over this thread.
	 */
	public void run() {

		// Create the outputDir directory
		StringBuilder outputDirPath = resultManager.createDirectory();

		BamStatsAnalysis bamQC = new BamStatsAnalysis(bamDialog.getInputFile().getAbsolutePath());

		// Set the number of windows
		bamQC.setNumberOfWindows(bamDialog.getNumberOfWindows());
        bamQC.setNumberOfThreads(bamDialog.getNumThreads());
        bamQC.setNumberOfReadsInBunch(bamDialog.getBunchSize());
        bamQC.setProtocol( bamDialog.getLibraryProtocol() );
        bamQC.setMinHomopolymerSize( bamDialog.getMinHomopolymerSize());

		// Set the region file
        boolean regionsAvailable = false;
		if (bamDialog.getRegionFile() != null) {
			bamQC.setSelectedRegions(bamDialog.getRegionFile().getAbsolutePath());
            bamQC.setComputeOutsideStats(bamDialog.getComputeOutsideRegions());
            regionsAvailable = true;
		}

		// reporting
		bamQC.activeReporting(outputDirPath.toString());

		// setup logging
        AnalysisDialogLoggerThread loggerThread = new AnalysisDialogLoggerThread(bamDialog);
        bamQC.setLoggerThread(loggerThread);

        bamDialog.setUiEnabled(false);
        bamDialog.getProgressStream().setText("Running BAM file analysis...");

		try {

            bamQC.run();
	        //timer.cancel();

			bamDialog.getProgressStream().setText("End of bam qc");
            bamDialog.getProgressBar().setValue(100);
	
			// report
			bamDialog.getProgressStream().setText("Computing report...");
			BamQCRegionReporter reporter = new BamQCRegionReporter(regionsAvailable, true);
            bamQC.prepareInputDescription(reporter, bamDialog.getDrawChromosomeLimits());

			// Draw the Chromosome Limits or not
			reporter.setPaintChromosomeLimits( bamDialog.getDrawChromosomeLimits() );
            if (bamDialog.compareGcContentToPrecalculated()) {
                String genomeName = bamDialog.getGenomeName();
                reporter.setGenomeGCContentName(genomeName);
            }

			bamDialog.getProgressStream().setText("   text report...");
			reporter.loadReportData(bamQC.getBamStats());
			bamDialog.getProgressStream().setText("OK");

			bamDialog.getProgressStream().setText("   charts...");
			reporter.computeChartsBuffers(bamQC.getBamStats(), bamQC.getLocator(), bamQC.isPairedData());
		    bamDialog.getProgressStream().setText("OK");


			// Set the reporter into the created tab
			resultManager.addReporter(reporter);

            if (bamDialog.getRegionFile() != null && bamDialog.getComputeOutsideRegions() ) {

                BamQCRegionReporter outsideReporter = new BamQCRegionReporter(regionsAvailable, false);

                bamQC.prepareInputDescription(outsideReporter, bamDialog.getDrawChromosomeLimits());
	            // Draw the Chromosome Limits or not
				outsideReporter.setPaintChromosomeLimits(bamDialog.getDrawChromosomeLimits());

                if (bamDialog.compareGcContentToPrecalculated()) {
                    String genomeName = bamDialog.getGenomeName();
                    outsideReporter.setGenomeGCContentName(genomeName);
                }

				// save stats
				bamDialog.getProgressStream().setText("   outside text report...");
				outsideReporter.loadReportData(bamQC.getOutsideBamStats());
				bamDialog.getProgressStream().setText("OK");

				// save charts
				bamDialog.getProgressStream().setText("   outside charts...");
				outsideReporter.computeChartsBuffers(bamQC.getOutsideBamStats(), bamQC.getLocator(), bamQC.isPairedData());
				bamDialog.getProgressStream().setText("OK");

				// Set the reporters into the created tab
				resultManager.addReporter(outsideReporter);
            }

			// Increment the progress bar
			bamDialog.getProgressStream().setText("OK");
			bamDialog.getProgressBar().setValue(100);

		} catch( OutOfMemoryError e) {
            JOptionPane.showMessageDialog(bamDialog, "<html><body align=\"center\">Out of memory!" +
                    "<br>Try decreasing the size of the chunk in the Advanced Options" +
                    "<br>or changing Java virtual machine settings.</body></html>",
                    bamDialog.getTitle(), JOptionPane.ERROR_MESSAGE);
            bamDialog.setUiEnabled(true);
            return;
        } catch (SAMFormatException se) {
            System.out.print(se.getMessage());
            JOptionPane.showMessageDialog(null, "Error parsing BAM file! See log for details.",
                    bamDialog.getTitle(), JOptionPane.ERROR_MESSAGE);
            se.printStackTrace();
            bamDialog.setUiEnabled(true);
            return;
        } catch (Exception e) {
		    JOptionPane.showMessageDialog(null, "Analysis is failed. " + e.getMessage(),
                    bamDialog.getTitle(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            bamDialog.setUiEnabled(true);
            return;
     	}

        bamDialog.addNewPane(resultManager);

    }


}
