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

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
import org.bioinfo.ngs.qc.qualimap.gui.panels.RNASeqQCDialog;

import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPageController;
import org.bioinfo.ngs.qc.qualimap.process.ComputeCountsTask;
import org.bioinfo.ngs.qc.qualimap.process.RNASeqQCAnalysis;

import javax.swing.*;

/**
 * Created by kokonech
 * Date: 7/13/13
 * Time: 12:44 PM
 */
public class RNASeqQCAnalysisThread extends  Thread {

    /** Logger to print information */
	protected Logger logger;

	/** Variable to manage the panel with the progressbar at the init */
	private RNASeqQCDialog settingsDlg;

	/** Variables that contains the tab properties loaded in the thread */
	TabPageController resultManager;

	public RNASeqQCAnalysisThread(RNASeqQCDialog rnaSeqQCDialog,TabPageController resultManager) {

        super("RNASeqQCAnalysisThread");
		this.settingsDlg = rnaSeqQCDialog;
        this.resultManager = resultManager;
		logger = new Logger(this.getClass().getName());
	}

	/**
	 * Public method to run this thread. Its executed when an user call to
	 * method start over this thread.
     */
    public void run() {

        settingsDlg.setUiEnabled(false);

        String bamPath = settingsDlg.getBamFilePath();
        String gffPath = settingsDlg.getGtfFilePath();

        ComputeCountsTask computeCountsTask = new ComputeCountsTask(bamPath, gffPath);
        computeCountsTask.setProtocol(settingsDlg.getProtocol());
        computeCountsTask.setCountingAlgorithm(settingsDlg.getCountingAlgorithm());
        computeCountsTask.addSupportedFeatureType(ComputeCountsTask.EXON_TYPE_ATTR);
        computeCountsTask.setAttrName(ComputeCountsTask.GENE_ID_ATTR);

        final JTextArea logArea = settingsDlg.getLogTextArea();
        logArea.setText("");

        computeCountsTask.setLogger( new LoggerThread() {
            @Override
            public void logLine(String msg) {
                logArea.append(msg + "\n");
                logArea.setCaretPosition(logArea.getText().length());
            }
        });

        RNASeqQCAnalysis rnaSeqQCAnalysis = new RNASeqQCAnalysis(resultManager, computeCountsTask );

        try {
            rnaSeqQCAnalysis.run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(settingsDlg, "Failed to analyze counts data. " + e.getMessage(),
                    settingsDlg.getTitle(), JOptionPane.ERROR_MESSAGE);
            settingsDlg.setUiEnabled(true);
            return;
        }

        settingsDlg.getHomeFrame().addNewPane(bamPath, resultManager);
	}

}
