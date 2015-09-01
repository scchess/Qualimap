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
    String countsFilePath,reportFilePath;


    boolean outputCounts;

    public RNASeqQCAnalysis(AnalysisResultManager resultManager, ComputeCountsTask task) {
        this.resultManager = resultManager;
        this.computeCountsTask = task;
        this.loggerThread = task.getLogger();
        computeCountsTask.setCollectRnaSeqStats(true);
        countsFilePath = "";
        reportFilePath = "";

        outputCounts = false;

    }

    public void run() throws Exception {
        computeCountsTask.run();

        createResultReport();

        if (reportFilePath.length() > 0 ) {
            writeReport();
        }

        if (countsFilePath.length() > 0) {
            writeGeneCounts();
        }

    }


    public void writeReport() throws IOException{

		// init report
		PrintWriter report = new PrintWriter(new FileWriter(reportFilePath));
        StringUtilsSwing sdf = new StringUtilsSwing();
        TranscriptDataHandler th = computeCountsTask.getTranscriptDataHandler();

		report.println("RNA-Seq QC report");
		report.println("-----------------------------------");
		report.println("");

        report.println(">>>>>>> Input");
		report.println("");
		report.println("    bam file = " + computeCountsTask.pathToBamFile );
        report.println("    gff file = " + computeCountsTask.pathToGffFile );
        report.println("    counting algorithm = " + computeCountsTask.countingAlgorithm);
        report.println("    protocol = " + computeCountsTask.protocol.toString());
        report.println("");
		report.println("");

		report.println(">>>>>>> Reads alignment");
		report.println("");
        if (computeCountsTask.pairedEndAnalysis) {
            report.println("    reads aligned (left/right) = " +
                            sdf.formatLong(computeCountsTask.getLeftProperInPair()) + " / " +
                            sdf.formatLong(computeCountsTask.getRightProperInPair())
                    );
            report.println("    read pairs aligned  = " +
                            sdf.formatLong(computeCountsTask.getNumberOfMappedPairs()));
        } else {
            report.println("    reads aligned  = " + sdf.formatLong(computeCountsTask.getPrimaryAlignmentsNumber()));
        }
        report.println("    total alignments = " + sdf.formatLong(computeCountsTask.getTotalAlignmentsNumber()));
        report.println("    secondary alignments = " + sdf.formatLong(computeCountsTask.getSecondaryAlignmentsNumber()));
        report.println("    non-unique alignments = " + sdf.formatLong(computeCountsTask.getAlignmentNotUniqueNumber()));
        report.println("    aligned to genes  = " + sdf.formatLong(computeCountsTask.getTotalReadCounts()));
        report.println("    ambiguous alignments = " + sdf.formatLong(computeCountsTask.getAmbiguousNumber()));
        report.println("    no feature assigned = " +  sdf.formatLong(computeCountsTask.getNoFeatureNumber()));
        report.println("    not aligned = "  + sdf.formatLong(computeCountsTask.getNotAlignedNumber()));
        report.println("");
		report.println("");

        report.println(">>>>>>> Reads genomic origin");
        report.println("");
        long totalReadCount = computeCountsTask.getTotalReadCounts() + computeCountsTask.getNoFeatureNumber();
        long exonicReadCount = totalReadCount - computeCountsTask.getNoFeatureNumber();
        long intronicReadCount = th.getNumIntronicReads();
        long intergenicReadCount = th.getNumIntergenicReads();
        long intersectingExonReadCount = th.getNumReadsIntersectingExonRegion();
        report.println("    exonic =  " + sdf.formatLong(exonicReadCount) + " (" +
                sdf.formatPercentage( (100.*exonicReadCount) /  totalReadCount ) + ")");
        report.println("    intronic = " + sdf.formatLong(intronicReadCount) + " (" +
                sdf.formatPercentage( (100.*intronicReadCount) /  totalReadCount ) + ")");
        report.println("    intergenic = " + sdf.formatLong(intergenicReadCount) + " (" +
                sdf.formatPercentage( (100.*intergenicReadCount) /  totalReadCount ) + ")");

        report.println("    overlapping exon = " + sdf.formatLong(intersectingExonReadCount) +
                       " (" + sdf.formatPercentage( (100.*intersectingExonReadCount) / totalReadCount) + ")");


        report.println("");
        report.println("");

        report.println(">>>>>>> Transcript coverage profile");
        report.println("");
        report.println("    5' bias = " +  sdf.formatDecimal(th.getMedianFivePrimeBias()));
        report.println("    3' bias = " + sdf.formatDecimal(th.getMedianThreePrimeBias()));
        report.println("    5'-3' bias = " + sdf.formatDecimal(th.getMedianFiveToThreeBias()));
        report.println("");
        report.println("");

        report.println(">>>>>>> Junction analysis");
        report.println("");
        long numReadsWithJunctions = th.getNumReadsWithJunctions();
        report.println("    reads at junctions = " + sdf.formatLong(numReadsWithJunctions));
        report.println("");
        if (numReadsWithJunctions > 0) {
            List<JunctionInfo> junctionList = th.computeSortedJunctionsMap();

            int count = 0;
            for(int i = junctionList.size() -1; i >= 0 && count <= 10; i--){
                JunctionInfo info = junctionList.get(i);
                report.println("    " + info.getJunctionString() + " : " + sdf.formatPercentage(info.getPercentage()));
                count += 1;
            }

        }

		report.close();
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

        if (computeCountsTask.pairedEndAnalysis) {
            readsAlignment.addRow("Number of mapped reads (left/right):",
                    sdf.formatLong(computeCountsTask.getLeftProperInPair()) + " / " +
                    sdf.formatLong(computeCountsTask.getRightProperInPair())
            );
            readsAlignment.addRow("Number of aligned pairs (without duplicates):",
                    sdf.formatLong(computeCountsTask.getNumberOfMappedPairs()));
        } else {
            readsAlignment.addRow("Number of mapped reads:", sdf.formatLong(computeCountsTask.getPrimaryAlignmentsNumber()));
        }
        readsAlignment.addRow("Total number of alignments:", sdf.formatLong(computeCountsTask.getTotalAlignmentsNumber()));
        readsAlignment.addRow("Number of secondary alignments:", sdf.formatLong(computeCountsTask.getSecondaryAlignmentsNumber()));
        readsAlignment.addRow("Number of non-unique alignments:", sdf.formatLong(computeCountsTask.getAlignmentNotUniqueNumber()));
        readsAlignment.addRow("Aligned to genes:", sdf.formatLong(computeCountsTask.getTotalReadCounts()));
        readsAlignment.addRow("Ambiguous alignments:", sdf.formatLong(computeCountsTask.getAmbiguousNumber()));
        readsAlignment.addRow("No feature assigned:", sdf.formatLong(computeCountsTask.getNoFeatureNumber()));
        readsAlignment.addRow("Not aligned:", sdf.formatLong(computeCountsTask.getNotAlignedNumber()));

        summaryKeeper.addSection(readsAlignment);

        StatsKeeper.Section readsOrigin = new StatsKeeper.Section("Reads genomic origin");
        long totalReadCount = computeCountsTask.getTotalReadCounts() + computeCountsTask.getNoFeatureNumber();
        long exonicReadCount = totalReadCount - computeCountsTask.getNoFeatureNumber();
        long intronicReadCount = th.getNumIntronicReads();
        long intergenicReadCount = th.getNumIntergenicReads();
        long intersectingExonReadCount = th.getNumReadsIntersectingExonRegion();
        readsOrigin.addRow("Exonic: ", sdf.formatLong(exonicReadCount) + " / " +
                sdf.formatPercentage( (100.*exonicReadCount) /  totalReadCount ));
        readsOrigin.addRow("Intronic: ", sdf.formatLong(intronicReadCount) + " / " +
                sdf.formatPercentage( (100.*intronicReadCount) /  totalReadCount ));
        readsOrigin.addRow("Intergenic: ", sdf.formatLong(intergenicReadCount) + " / " +
                sdf.formatPercentage( (100.*intergenicReadCount) /  totalReadCount ));
        readsOrigin.addRow("Intronic/intergenic overlapping exon: ", sdf.formatLong(intersectingExonReadCount)
                + " / " + sdf.formatPercentage( (100.*intersectingExonReadCount) / totalReadCount));
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


        StatsKeeper.Section transcriptCoverage = new StatsKeeper.Section("Transcript coverage profile");
        transcriptCoverage.addRow("5' bias:", sdf.formatDecimal(th.getMedianFivePrimeBias()));
        transcriptCoverage.addRow("3' bias:", sdf.formatDecimal(th.getMedianThreePrimeBias()));
        transcriptCoverage.addRow("5'-3' bias:", sdf.formatDecimal(th.getMedianFiveToThreeBias()));

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

        Map<String,String> inputParams = new TreeMap<String, String>();
        inputParams.put("BAM file: ", computeCountsTask.pathToBamFile);
        inputParams.put("GTF file: ", computeCountsTask.pathToGffFile);
        inputParams.put("Protocol: ", computeCountsTask.protocol.toString());
        inputParams.put("Counting algorithm:", computeCountsTask.countingAlgorithm);
        inputParams.put("Paired-end sequencing:", computeCountsTask.pairedEndAnalysis ? "yes" : "no");
        inputParams.put("Sorting performed:", computeCountsTask.sortingRequired ? "yes" : "no");


        Date date = new Date();
        inputParams.put("Analysis date: ", date.toString());

        reporter.addInputDataSection("Input", inputParams);

    }


    public void setCountsFilePath(String path) {
        countsFilePath = path;
    }


    public void setReportFilePath(String path) {
        reportFilePath = path;
    }
}
