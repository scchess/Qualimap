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
package org.bioinfo.ngs.qc.qualimap.process;


import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;
import org.bioinfo.ngs.qc.qualimap.common.LibraryProtocol;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;

/**
 * Created by kokonech
 * Date: 12/12/11
 * Time: 2:52 PM
 */

public class RNASeqQCAnalysis  {


    ComputeCountsTask computeCountsTask;
    private AnalysisResultManager resultManager;

    public RNASeqQCAnalysis(AnalysisResultManager resultManager, ComputeCountsTask task) {
        this.resultManager = resultManager;
        this.computeCountsTask = task;

    }

    public void run() throws Exception {
        computeCountsTask.run();

        createResultReport();
    }

    private void createResultReport() {
        //BamQCRegionReporter reporter = tabProperties.getReporter();
        //prepareInputDescription(reporter);


    }

    private void prepareInputDescription(StatsReporter reporter) {


    }


}
