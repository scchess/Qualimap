package org.bioinfo.ngs.qc.qualimap.gui.utils;

/**
 * Created by kokonech
 * Date: 6/22/12
 * Time: 4:05 PM
 */
public enum AnalysisType {

    BAM_QC, COUNTS_QC, CLUSTERING;

    static final String NAME_BAM_QC = "BAM QC";
    static final String NAME_COUNTS_QC = "Counts QC";
    static final String NAME_CLUSTERING = "Clustering";
    static final String UNKNOWN = "Unknown analysis";

    public String toString() {
        if (this == BAM_QC) {
            return NAME_BAM_QC;
        } else if (this == COUNTS_QC) {
            return NAME_COUNTS_QC;
        } else if (this == CLUSTERING ) {
            return NAME_CLUSTERING;
        } else {
            return UNKNOWN;
        }
    }

    public boolean isBamQC() {
        return this == BAM_QC;
    }
}
