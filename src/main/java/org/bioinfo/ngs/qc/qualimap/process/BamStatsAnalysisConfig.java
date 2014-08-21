package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.common.Constants;

/**
 * Created by kokonech
 * Date: 8/20/14
 * Time: 10:33 AM
 */

public class BamStatsAnalysisConfig {

    public boolean drawChromosomeLimits;
    public String gffFile;
    public int numberOfWindows, bunchSize, minHomopolymerSize;
    public BamStatsAnalysisConfig() {
        this.drawChromosomeLimits = false;
        this.numberOfWindows = Constants.DEFAULT_NUMBER_OF_WINDOWS;
        this.bunchSize = Constants.DEFAULT_CHUNK_SIZE;
        this.minHomopolymerSize = Constants.DEFAULT_HOMOPOLYMER_SIZE;

    }

    public  boolean regionsAvailable() {
        return gffFile != null;
    }

}
