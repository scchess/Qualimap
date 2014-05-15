package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;

/**
 * Created by kokonech
 * Date: 5/15/14
 * Time: 4:03 PM
 */
public class MultisampleCountsAnalysis extends AnalysisProcess{


    public MultisampleCountsAnalysis(AnalysisResultManager tabProperties, String homePath) {
        super(tabProperties, homePath);
    }

    @Override
    public void run() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void prepareInputDescription(StatsReporter reporter) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void reportProgress(String msg) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
