/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2013 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.gui.utils;

/**
 * Created by kokonech
 * Date: 6/22/12
 * Time: 4:05 PM
 */
public enum AnalysisType {

    BAM_QC, RNA_SEQ_QC, COUNTS_QC, CLUSTERING;

    static final String NAME_BAM_QC = "BAM QC";
    static final String NAME_RNA_SEQ_QC = "RNA Seq QC";
    static final String NAME_COUNTS_QC = "Counts QC";
    static final String NAME_CLUSTERING = "Clustering";
    static final String UNKNOWN = "Unknown analysis";

    public String toString() {
        if (this == BAM_QC) {
            return NAME_BAM_QC;
        }else if (this == RNA_SEQ_QC) {
            return NAME_RNA_SEQ_QC;
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
