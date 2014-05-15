/**
 * Created by kokonech
 * Date: 5/15/14
 * Time: 5:30 PM
 */
package org.bioinfo.ngs.qc.qualimap.gui.threads;

import java.io.*;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.gui.panels.CountsQcDialog;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPageController;

/**
 * Class to manage a thread that performs the analysis from counts of the input files
 *
 * @author kokonech
 */
public class CountsQCAnalysisThread extends Thread {


	/** Logger to print information */
	protected Logger logger;

	/** Variable to manage the panel with the progressbar at the init */
	private CountsQcDialog settingsDlg;

	/** Variables that contains the tab properties loaded in the thread */
	TabPageController tabProperties;

    String infoFilePath;

	public CountsQCAnalysisThread(String str, CountsQcDialog countsQcDialog, TabPageController tabProperties) {
		super(str);
		this.settingsDlg = countsQcDialog;
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

        /*CountsAnalysis countsAnalysis = new CountsAnalysis(tabProperties, homePath);

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
        settingsDlg.getHomeFrame().addNewPane(inputFileName, tabProperties);*/
	}



}
