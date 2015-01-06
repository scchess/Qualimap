/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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

import org.bioinfo.ngs.qc.qualimap.gui.panels.MultisampleBamQcDialog;
import org.bioinfo.ngs.qc.qualimap.gui.utils.AnalysisDialogLoggerThread;
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

    public void run() {

        settingsDlg.setUiEnabled(false);
        settingsDlg.getProgressBar().setValue(0);

        String homePath = settingsDlg.getHomeFrame().getQualimapFolder() + File.separator;

        MultisampleBamQcAnalysis multiBamQcAnalysis = new MultisampleBamQcAnalysis(tabProperties, homePath,
                settingsDlg.getDataItems());
        AnalysisDialogLoggerThread outputParsingThread= new AnalysisDialogLoggerThread( settingsDlg ) ;
        multiBamQcAnalysis.setOutputParsingThread(outputParsingThread);

        if (settingsDlg.runBamQcFirst()) {
            multiBamQcAnalysis.setRunBamQcFirst( settingsDlg.getBamQcConfig() );
        }

        try {
            multiBamQcAnalysis.run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(settingsDlg, "Failed to run multi-sample BAM QC!\n" + e.getMessage(),
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
