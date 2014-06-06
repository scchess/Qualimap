package org.bioinfo.ngs.qc.qualimap.gui.threads;

import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
import org.bioinfo.ngs.qc.qualimap.gui.panels.MultisampleBamQcDialog;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPageController;
import org.bioinfo.ngs.qc.qualimap.process.MultisampleBamQcAnalysis;

import javax.swing.*;
import java.io.File;

/**
 * Created by kokonech
 * Date: 6/6/14
 * Time: 3:42 PM
 */
public class MultisampleBamQcThread extends Thread {

    /** Variable to manage the panel with the progressbar at the init */
    MultisampleBamQcDialog settingsDlg;

    /** Variables that contains the tab properties loaded in the thread */
    TabPageController tabProperties;

    static class OutputParsingThread extends LoggerThread {

        MultisampleBamQcDialog parentDialog;


        OutputParsingThread(MultisampleBamQcDialog parentDialog) {
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



    public MultisampleBamQcThread(MultisampleBamQcDialog multiBamQcDialog, TabPageController tabProperties) {
        super("MultisampleBamQcThread");
        this.settingsDlg = multiBamQcDialog;
        this.tabProperties = tabProperties;

    }

    /**
     * Public method to run this thread. Its executed when an user call to
     * method start over this thread.
     */
    public void run() {

        settingsDlg.setUiEnabled(false);

        String homePath = settingsDlg.getHomeFrame().getQualimapFolder() + File.separator;

        MultisampleBamQcAnalysis multiBamQcAnalysis = new MultisampleBamQcAnalysis(tabProperties, homePath,
                settingsDlg.getDataItems());


        OutputParsingThread outputParsingThread= new OutputParsingThread( settingsDlg ) ;
        multiBamQcAnalysis.setOutputParsingThread(outputParsingThread);

        try {
            multiBamQcAnalysis.run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(settingsDlg, "Failed to analyze Qualimap reports data!\n" + e.getMessage(),
                    settingsDlg.getTitle(), JOptionPane.ERROR_MESSAGE);
            settingsDlg.setUiEnabled(true);
            //settingsDlg.resetUi();
            return;
        }

        String paneTitle = "" + settingsDlg.getItemCount();
        settingsDlg.getHomeFrame().addNewPane(paneTitle, tabProperties);
    }



}
