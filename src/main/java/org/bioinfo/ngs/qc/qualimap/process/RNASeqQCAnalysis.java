/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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
import org.bioinfo.ngs.qc.qualimap.common.JunctionInfo;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
import org.bioinfo.ngs.qc.qualimap.common.TranscriptDataHandler;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StatsKeeper;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StringUtilsSwing;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by kokonech
 * Date: 12/12/11
 * Time: 2:52 PM
 */

public class RNASeqQCAnalysis  {


    ComputeCountsTask computeCountsTask;
    private AnalysisResultManager resultManager;
    LoggerThread loggerThread;
    String countsFilePath;


    boolean outputCounts;

    public RNASeqQCAnalysis(AnalysisResultManager resultManager, ComputeCountsTask task) {
        this.resultManager = resultManager;
        this.computeCountsTask = task;
        this.loggerThread = task.getLogger();
        computeCountsTask.setCollectRnaSeqStats(true);
        countsFilePath = "";

        outputCounts = false;

    }

    public void run() throws Exception {
        computeCountsTask.run();

        createResultReport();

        if (countsFilePath.length() > 0) {
            writeGeneCounts();
        }

    }

    private void writeGeneCounts() throws IOException {
        PrintWriter outWriter =  new PrintWriter(new FileWriter(countsFilePath));
        Map<String,Double> counts = computeCountsTask.getReadCounts();
        for (Map.Entry<String,Double> entry: counts.entrySet()) {
            long roundedValue = entry.getValue().longValue();
            String str = entry.getKey() + "\t" + roundedValue;
            outWriter.println(str);
        }
        outWriter.flush();
    }

    private void createResultReport() throws IOException {
        StatsReporter reporter = new StatsReporter();
        prepareHtmlSummary(reporter);
        prepareInputDescription(reporter);
        createCharts(reporter);

        resultManager.addReporter(reporter);


    }

    private void createCharts(StatsReporter reporter) throws IOException {

        TranscriptDataHandler th = computeCountsTask.getTranscriptDataHandler();

        loggerThread.logLine("Creating plots");
        th.setNumTotalReads(computeCountsTask.getTotalReadCounts() + computeCountsTask.getNoFeatureNumber());
        List<QChart> plots = th.createPlots(computeCountsTask.getSampleName());

        reporter.setChartList(plots);

    }

    private void prepareHtmlSummary(StatsReporter reporter) {


        StatsKeeper summaryKeeper = reporter.getSummaryStatsKeeper();
        StringUtilsSwing sdf = new StringUtilsSwing();
        TranscriptDataHandler th = computeCountsTask.getTranscriptDataHandler();

        StatsKeeper.Section readsAlignment = new StatsKeeper.Section("Reads alignment");

        readsAlignment.addRow("Aligned to genes:", sdf.formatLong(computeCountsTask.getTotalReadCounts()));
        readsAlignment.addRow("No feature assigned:", sdf.formatLong(computeCountsTask.getNoFeatureNumber()));
        readsAlignment.addRow("Non-unique alignment:", sdf.formatLong(computeCountsTask.getAlignmentNotUniqueNumber()));
        readsAlignment.addRow("Ambiguous alignment:", sdf.formatLong(computeCountsTask.getAmbiguousNumber()));
        readsAlignment.addRow("Not aligned:", sdf.formatLong(computeCountsTask.getNotAlignedNumber()));

        summaryKeeper.addSection(readsAlignment);

        StatsKeeper.Section readsOrigin = new StatsKeeper.Section("Reads genomic origin");
        long totalReadCount = computeCountsTask.getTotalReadCounts() + computeCountsTask.getNoFeatureNumber();
        long exonicReadCount = totalReadCount - computeCountsTask.getNoFeatureNumber();
        long intronicReadCount = th.getNumIntronicReads();
        long intergenicReadCount = th.getNumIntergenicReads();
        readsOrigin.addRow("Exonic: ", sdf.formatLong(exonicReadCount) + " / " +
                sdf.formatPercentage( (100.*exonicReadCount) /  totalReadCount ));
        readsOrigin.addRow("Intronic: ", sdf.formatLong(intronicReadCount) + " / " +
                sdf.formatPercentage( (100.*intronicReadCount) /  totalReadCount ));
        readsOrigin.addRow("Intergenic: ", sdf.formatLong(intergenicReadCount) + " / " +
                sdf.formatPercentage( (100.*intergenicReadCount) /  totalReadCount ));
        summaryKeeper.addSection(readsOrigin);

        //TODO: fix this in case of SE reads
        /*if (computeCountsTask.getLibraryProtocol() != LibraryProtocol.NON_STRAND_SPECIFIC) {
            StatsKeeper.Section libraryProtocol = new StatsKeeper.Section("Library protocol");
            double correctlyMappedPercentage =
                    (100.*computeCountsTask.getProtocolCorrectlyMapped()) / computeCountsTask.getTotalFragmentCount() ;
            libraryProtocol.addRow("Concordant fragments: ",
                    sdf.formatLong(computeCountsTask.getProtocolCorrectlyMapped()) + " / " +
                    sdf.formatPercentage(correctlyMappedPercentage));
        }*/


        TranscriptDataHandler transcriptDataHandler = computeCountsTask.getTranscriptDataHandler();
        StatsKeeper.Section transcriptCoverage = new StatsKeeper.Section("Transcript coverage profile");
        transcriptCoverage.addRow("5' bias:", sdf.formatDecimal(transcriptDataHandler.getMedianFivePrimeBias()));
        transcriptCoverage.addRow("3' bias:", sdf.formatDecimal(transcriptDataHandler.getMedianThreePrimeBias()));
        transcriptCoverage.addRow("5'-3' bias:", sdf.formatDecimal(transcriptDataHandler.getMedianFiveToThreeBias()));

        summaryKeeper.addSection(transcriptCoverage);

        StatsKeeper.Section junctionAnalysisSection = new StatsKeeper.Section("Junction analysis");
        long numReadsWithJunctions = th.getNumReadsWithJunctions();
        junctionAnalysisSection.addRow("Reads at junctions:", sdf.formatLong(numReadsWithJunctions));
        if (numReadsWithJunctions > 0) {
            List<JunctionInfo> junctionList = th.computeSortedJunctionsMap();

            int count = 0;
            for(int i = junctionList.size() -1; i >= 0 && count <= 10; i--){
                JunctionInfo info = junctionList.get(i);
                junctionAnalysisSection.addRow(info.getJunctionString(), sdf.formatPercentage(info.getPercentage()));
                count += 1;
            }

        }
        summaryKeeper.addSection(junctionAnalysisSection);




    }

    private void prepareInputDescription(StatsReporter reporter) {

        HashMap<String,String> inputParams = new HashMap<String, String>();
        Date date = new Date();
        inputParams.put("Analysis date: ", date.toString());
        inputParams.put("BAM file: ", computeCountsTask.pathToBamFile);
        inputParams.put("GTF file: ", computeCountsTask.pathToGffFile);
        inputParams.put("Protocol: ", computeCountsTask.protocol.toString());
        inputParams.put("Counting algorithm", computeCountsTask.countingAlgorithm);

        reporter.addInputDataSection("Input", inputParams);

    }


    public void setCountsFilePath(String path) {
        countsFilePath = path;
    }


}
