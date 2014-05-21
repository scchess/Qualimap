package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;

/**
 * Created by kokonech
 * Date: 5/15/14
 * Time: 5:49 PM
 */

public abstract class AnalysisProcess {

    protected AnalysisResultManager tabProperties;
    protected LoggerThread loggerThread;
    protected  String homePath;

    public AnalysisProcess(AnalysisResultManager tabProperties, String homePath) {
        this.tabProperties = tabProperties;
        this.homePath = homePath;
        this.loggerThread = null;
    }

    public abstract void run() throws Exception;


}
