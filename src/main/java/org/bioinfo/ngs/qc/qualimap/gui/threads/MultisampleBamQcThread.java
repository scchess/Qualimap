package org.bioinfo.ngs.qc.qualimap.gui.threads;

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

    MultisampleBamQcDialog settingsDlg;

    TabPageController tabProperties;


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
        settingsDlg.getProgressBar().setValue(0);

        String homePath = settingsDlg.getHomeFrame().getQualimapFolder() + File.separator;

        MultisampleBamQcAnalysis multiBamQcAnalysis = new MultisampleBamQcAnalysis(tabProperties, homePath,
                settingsDlg.getDataItems());

        try {
            multiBamQcAnalysis.run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(settingsDlg, "Failed to analyze Qualimap reports data!\n" + e.getMessage(),
                    settingsDlg.getTitle(), JOptionPane.ERROR_MESSAGE);
            settingsDlg.setUiEnabled(true);
            //settingsDlg.resetUi();
            return;
        }
        settingsDlg.getProgressBar().setValue(100);

        String paneTitle = "" + settingsDlg.getItemCount();
        settingsDlg.getHomeFrame().addNewPane(paneTitle, tabProperties);
    }



}
