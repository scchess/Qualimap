package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;

import javax.swing.*;

/**
 * Created by kokonech
 * Date: 5/15/14
 * Time: 5:49 PM
 */

public abstract class AnalysisProcess {

    protected AnalysisResultManager tabProperties;
    protected LoggerThread outputParsingThread;
    protected  JLabel progressStream;
    protected  String homePath;

    public AnalysisProcess(AnalysisResultManager tabProperties, String homePath) {
        this.tabProperties = tabProperties;
        this.homePath = homePath;
        this.outputParsingThread = null;
        this.progressStream = null;
    }

    public abstract void run() throws Exception;

    protected abstract void prepareInputDescription(StatsReporter reporter);

    protected  abstract void reportProgress(String msg);


}
