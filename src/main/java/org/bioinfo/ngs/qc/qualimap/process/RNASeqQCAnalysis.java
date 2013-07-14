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
import org.bioinfo.ngs.qc.qualimap.beans.QChart;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;
import org.bioinfo.ngs.qc.qualimap.common.LibraryProtocol;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
import org.bioinfo.ngs.qc.qualimap.common.TranscriptDataHandler;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StatsKeeper;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StringUtilsSwing;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
        computeCountsTask.setCalcCoverageBias(true);

    }

    public void run() throws Exception {
        computeCountsTask.run();

        createResultReport();
    }

    private void createResultReport() {
        StatsReporter reporter = new StatsReporter();
        prepareHtmlSummary(reporter);
        prepareInputDescription(reporter);
        createCharts(reporter);


        resultManager.addReporter(reporter);


    }

    private void createCharts(StatsReporter reporter) {

        TranscriptDataHandler th = computeCountsTask.getTranscriptDataHandler();
        List<QChart> plots = th.createPlots();

        reporter.setChartList(plots);

    }

    private void prepareHtmlSummary(StatsReporter reporter) {


        StatsKeeper summaryKeeper = reporter.getSummaryStatsKeeper();
        StringUtilsSwing sdf = new StringUtilsSwing();

        StatsKeeper.Section readsAlignment = new StatsKeeper.Section("Reads alignment");

        readsAlignment.addRow("Aligned to genes:", sdf.formatLong(computeCountsTask.getTotalReadCounts()));
        readsAlignment.addRow("Non-unique alignment:", sdf.formatLong(computeCountsTask.getAlignmentNotUniqueNumber()));
        readsAlignment.addRow("Ambiguous alignment:", sdf.formatLong(computeCountsTask.getAmbiguousNumber()));
        readsAlignment.addRow("No feature assigned:", sdf.formatLong(computeCountsTask.getNoFeatureNumber()));
        readsAlignment.addRow("Not aligned:", sdf.formatLong(computeCountsTask.getNotAlignedNumber()));

        summaryKeeper.addSection(readsAlignment);

        TranscriptDataHandler transcriptDataHandler = computeCountsTask.getTranscriptDataHandler();
        StatsKeeper.Section transcriptCoverage = new StatsKeeper.Section("Transcript coverage");
        transcriptCoverage.addRow("5' bias:", sdf.formatDecimal(transcriptDataHandler.getMedianFivePrimeBias()));
        transcriptCoverage.addRow("3' bias:", sdf.formatDecimal(transcriptDataHandler.getMedianThreePrimeBias()));
        transcriptCoverage.addRow("5'-3' bias:", sdf.formatDecimal(transcriptDataHandler.getMedianFiveToThreeBias()));

        summaryKeeper.addSection(transcriptCoverage);



    }

    private void prepareInputDescription(StatsReporter reporter) {

        HashMap<String,String> inputParams = new HashMap<String, String>();
        Date date = new Date();
        inputParams.put("Analysis date: ", date.toString());
        inputParams.put("BAM file: ", computeCountsTask.pathToBamFile);
        inputParams.put("GTF file: ", computeCountsTask.pathToGffFile);
        inputParams.put("Protocol: ", computeCountsTask.protocol);

        reporter.addInputDataSection("Input", inputParams);

    }


}
