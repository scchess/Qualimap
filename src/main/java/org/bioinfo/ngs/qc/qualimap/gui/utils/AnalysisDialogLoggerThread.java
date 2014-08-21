package org.bioinfo.ngs.qc.qualimap.gui.utils;

import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.AnalysisDialog;

import javax.swing.*;

/**
 * Created by kokonech
 * Date: 8/21/14
 * Time: 11:22 AM
 */
public class AnalysisDialogLoggerThread extends LoggerThread {

        AnalysisDialog parentDialog;


        public AnalysisDialogLoggerThread(AnalysisDialog parentDialog) {
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

        @Override
        public void updateProgress(int progress) {
            parentDialog.updateProgress(progress);
        }
}
