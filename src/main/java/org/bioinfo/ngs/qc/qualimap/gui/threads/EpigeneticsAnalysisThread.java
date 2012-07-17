package org.bioinfo.ngs.qc.qualimap.gui.threads;

import org.bioinfo.ngs.qc.qualimap.gui.panels.EpigeneticAnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.EpiAnalysis;
import org.bioinfo.ngs.qc.qualimap.process.EpiAnalysis.Config;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;


import javax.swing.*;

/**
 * Created by kokonech
 * Date: 1/4/12
 * Time: 3:00 PM
 */

public class EpigeneticsAnalysisThread extends Thread {

    EpigeneticAnalysisDialog settingsDialog;
    TabPropertiesVO tabProperties;

    static class OutputParsingThread extends LoggerThread {

        EpigeneticAnalysisDialog parentDialog;


        OutputParsingThread(EpigeneticAnalysisDialog parentDialog) {
            this.parentDialog = parentDialog;

        }

        @Override
        public void logLine(String msg) {
            System.out.println(msg);
            if (msg.contains("STATUS:")) {
                parentDialog.setProgressStatus(msg.split(":")[1]);
            }
            JTextArea logArea = parentDialog.getLogArea();
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getText().length());
        }

    }

    public EpigeneticsAnalysisThread(EpigeneticAnalysisDialog settingsDialog, TabPropertiesVO tabProperties) {
        super("EpigeneticsAnalysisThread");
        this.settingsDialog = settingsDialog;
        this.tabProperties = tabProperties;

    }

    public void run()  {

        settingsDialog.setUiEnabled(false);
        settingsDialog.getLogArea().setText("");

        Config cfg = new Config();

        cfg.replicates = settingsDialog.getSampleItems();
        cfg.experimentName = settingsDialog.getExperimentName();
        cfg.pathToRegions = settingsDialog.getGeneSelectionPath();
        cfg.leftOffset = Integer.parseInt(settingsDialog.getLeftOffset());
        cfg.rightOffset = Integer.parseInt(settingsDialog.getRightOffset());
        cfg.binSize = Integer.parseInt(settingsDialog.getStep());
        cfg.fragmentLength  = Integer.parseInt(settingsDialog.getReadSmoothingLength());
        cfg.clusters = settingsDialog.getClusterNumbers();
        cfg.vizType = settingsDialog.getVisuzliationType();

        String homePath = settingsDialog.getHomeFrame().getQualimapFolder();
        EpiAnalysis epiAnalysis = new EpiAnalysis(tabProperties, homePath, cfg);

        OutputParsingThread outputParsingThread= new OutputParsingThread( settingsDialog ) ;
        epiAnalysis.setOutputParsingThread(outputParsingThread);

        epiAnalysis.setProgressStream(settingsDialog.getProgressStream());


        try {

            epiAnalysis.run();

        } catch (Exception e)  {
            JOptionPane.showMessageDialog(settingsDialog, e.getMessage(),
                    "Epigenetics analysis", JOptionPane.ERROR_MESSAGE );
            e.printStackTrace();
            settingsDialog.setUiEnabled(true);
            return;
        }


        String inputFileName = settingsDialog.getInputDataName();
        settingsDialog.getHomeFrame().addNewPane(inputFileName, tabProperties);

        settingsDialog.setVisible(false);

    }


}
