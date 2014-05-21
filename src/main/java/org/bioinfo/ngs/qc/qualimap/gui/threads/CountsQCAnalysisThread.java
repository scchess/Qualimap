/**
 * Created by kokonech
 * Date: 5/15/14
 * Time: 5:30 PM
 */
package org.bioinfo.ngs.qc.qualimap.gui.threads;

import java.io.*;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
import org.bioinfo.ngs.qc.qualimap.gui.panels.CountsQcDialog;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPageController;
import org.bioinfo.ngs.qc.qualimap.process.MultisampleCountsAnalysis;

import javax.swing.*;

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

    static class OutputParsingThread extends LoggerThread {

        CountsQcDialog parentDialog;


        OutputParsingThread(CountsQcDialog parentDialog) {
            this.parentDialog = parentDialog;

        }

        @Override
        public void logLine(String msg) {
            System.out.println(msg);
            /*if (msg.contains("STATUS:")) {
                parentDialog.setProgressStatus(msg.split(":")[1]);
            }*/
            JTextArea logArea = parentDialog.getLogArea();
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getText().length());
        }

    }



	public CountsQCAnalysisThread(CountsQcDialog countsQcDialog, TabPageController tabProperties) {
		super("MultisampleCountsQcThread");
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

        MultisampleCountsAnalysis countsAnalysis = new MultisampleCountsAnalysis(tabProperties, homePath,
                settingsDlg.getDataItems());

        countsAnalysis.setConditionNames(  settingsDlg.getConditionsMap() );

        if (settingsDlg.performComparison()) {
            countsAnalysis.activateComparison();
        }

        //countsAnalysis.setThreshold( settingsDlg.getThreshold() );

        boolean  includeInfoFile = settingsDlg.annotationIsProvided();
        if (includeInfoFile) {
            String infoFilePath;
            if (settingsDlg.infoFileIsProvided())  {
                infoFilePath = settingsDlg.getInfoFilePath();
            } else {
                infoFilePath =  homePath + "species" + File.separator + settingsDlg.getSelectedSpecies();
            }
            countsAnalysis.setInfoFilePath(infoFilePath);
        }

        OutputParsingThread outputParsingThread= new OutputParsingThread( settingsDlg ) ;
        countsAnalysis.setOutputParsingThread(outputParsingThread);

        try {
            countsAnalysis.run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(settingsDlg, "Failed to analyze counts data. " + e.getMessage(),
                    settingsDlg.getTitle(), JOptionPane.ERROR_MESSAGE);
            settingsDlg.setUiEnabled(true);
            //settingsDlg.resetUi();
            return;
        }

        String paneTitle = "" + settingsDlg.getItemCount();
        settingsDlg.getHomeFrame().addNewPane(paneTitle, tabProperties);
	}



}
