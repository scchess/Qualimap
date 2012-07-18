/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2012 Garcia-Alcalde et al.
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

import java.io.*;
import javax.swing.*;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.gui.panels.CountsAnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.CountsAnalysis;

/**
 * Class to manage a thread that performs the analysis from RNA-Seq of the input files
 * 
 * @author kokonech
 */
public class CountsAnalysisThread extends Thread {


	/** Logger to print information */
	protected Logger logger;

	/** Variable to manage the panel with the progressbar at the init */
	private CountsAnalysisDialog settingsDlg;

	/** Variables that contains the tab properties loaded in the thread */
	TabPropertiesVO tabProperties;

    String infoFilePath;

	public CountsAnalysisThread(String str, CountsAnalysisDialog countsAnalysisDialog, TabPropertiesVO tabProperties) {
		super(str);
		this.settingsDlg = countsAnalysisDialog;
        this.tabProperties = tabProperties;
		logger = new Logger(this.getClass().getName());
	}

	/**
	 * Public method to run this thread. Its executed when an user call to
	 * method start over this thread.
     */
    public void run() {

        settingsDlg.setUiEnabled(false);

        String homePath = settingsDlg.getHomeFrame().getQualimapFolder() + File.separator;

        CountsAnalysis countsAnalysis = new CountsAnalysis(tabProperties, homePath);

        countsAnalysis.setSample1Name( settingsDlg.getName1() );
        countsAnalysis.setFirstSampleDataPath( settingsDlg.getFirstSampleDataPath() );

        if (settingsDlg.secondSampleIsProvided()) {
            countsAnalysis.setSecondSampleIsProvided(true);
            countsAnalysis.setSample2Name( settingsDlg.getName2() );
            countsAnalysis.setSecondSampleDataPath( settingsDlg.getSecondSampleDataPath() );
        }

        countsAnalysis.setThreshold( settingsDlg.getThreshold() );

        boolean  includeInfoFile = settingsDlg.includeInfoFile();
        if (includeInfoFile) {
            if (settingsDlg.infoFileIsProvided())  {
                infoFilePath = settingsDlg.getInfoFilePath();
            } else {
                infoFilePath =  homePath + "species" +
                        File.separator + settingsDlg.getSelectedSpecies();
            }
            countsAnalysis.setInfoFilePath(infoFilePath);
        }

        countsAnalysis.setProgressControls(settingsDlg.getProgressBar(), settingsDlg.getProgressStream());

        try {
            countsAnalysis.run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(settingsDlg, "Failed to analyze counts data. " + e.getMessage(),
                    settingsDlg.getTitle(), JOptionPane.ERROR_MESSAGE);
            settingsDlg.setUiEnabled(true);
            settingsDlg.resetUi();
            return;
        }

        String inputFileName = settingsDlg.getInputDataName();
        settingsDlg.getHomeFrame().addNewPane(inputFileName, tabProperties);
	}



}
