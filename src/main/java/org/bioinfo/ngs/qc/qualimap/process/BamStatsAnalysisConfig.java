package org.bioinfo.ngs.qc.qualimap.process;

/**
 * Created by kokonech
 * Date: 8/20/14
 * Time: 10:33 AM
 */

public class BamStatsAnalysisConfig {

    public boolean regionsAvailable;
    public boolean drawChromosomeLimits;
    public String gffFile;
    public BamStatsAnalysisConfig() {
        this.regionsAvailable = false;
        this.drawChromosomeLimits = true;
    }

}
