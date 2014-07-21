package org.bioinfo.ngs.qc.qualimap.process;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.bioinfo.ngs.qc.qualimap.beans.*;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StatsKeeper;
import org.jfree.chart.ChartColor;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by kokonech
 * Date: 6/5/14
 * Time: 12:45 PM
 */
public class MultisampleBamQcAnalysis extends AnalysisProcess{


    List<SampleInfo> bamQCResults;
    List<double[]> sampleData;
    LoggerThread loggerThread;
    Paint[] palette;
    Map<SampleInfo,String> rawDataDirs;

    static final int NUM_FEATURES = 5;

    public MultisampleBamQcAnalysis(AnalysisResultManager tabProperties,
                                    String homePath,
                                    List<SampleInfo> bamQCResults) {
        super(tabProperties, homePath);
        this.bamQCResults = bamQCResults;
        this.palette = ChartColor.createDefaultPaintArray();
        this.rawDataDirs = new HashMap<SampleInfo, String>();
        sampleData = new ArrayList<double[]>();
    }

    @Override
    public void run() throws Exception {
        checkInputPaths();

        StatsReporter reporter = new StatsReporter();
        reporter.setFileName( "multisampleBamQcReport" );


        prepareInputDescription(reporter);
        createSummaryTable(reporter);
        createCharts(reporter);


        tabProperties.addReporter(reporter);

    }


    private BamStats loadSummaryStats(String inputFilePath) throws IOException {
        BamStats bamStats = new BamStats(null,null, 0,0);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        while ( (line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }
            if (line.contains("mean coverageData =")) {
                double meanCoverage = Double.parseDouble( line.split("=")[1].trim().replace("X","") );
                bamStats.setCoverageMean(meanCoverage);
            } else if (line.contains("std coverageData =")) {
                double stdCoverage = Double.parseDouble(line.split("=")[1].trim().replace("X", ""));
                bamStats.setCoverageStd(stdCoverage);
            } else if (line.contains("mean mapping quality =")) {
                double mappingQuality = Double.parseDouble(line.split("=")[1].trim());
                bamStats.setMeanMappingQuality(mappingQuality);
            } else if (line.contains("GC percentage =")) {
                // This is actually in percents already - only should be used in the context of Multiple BAM QC
                double gcPercentage = Double.parseDouble(line.split("=")[1].trim().replace("%", ""));
                bamStats.setMeanGcContent(gcPercentage);
            } else if (line.contains("median insert size =")) {
                int insertSize = Integer.parseInt(line.split("=")[1].trim());
                bamStats.setMedianInsertSize(insertSize);
            }




        }

        return bamStats;

    }


    private void createSummaryTable(StatsReporter reporter) throws IOException {

        logLine("Creating summary...\n");

        StatsKeeper summaryKeeper = reporter.getSummaryStatsKeeper();

        StatsKeeper.Section section = new StatsKeeper.Section("Globals");
        section.addRow("Number of samples", Integer.toString( bamQCResults.size() ));
        summaryKeeper.addSection(section);

        StatsKeeper tableDataKeeper = reporter.getTableDataStatsKeeper();

        StatsKeeper.Section headerSection = new StatsKeeper.Section("header");
        String[] header = {"Sample name", "Coverage mean", "Coverage std",
                "GC percentage", "Mapping quality mean", "Insert size mean" };
        headerSection.addRow( header );
        tableDataKeeper.addSection(headerSection);

        StatsKeeper.Section dataSection = new StatsKeeper.Section("data");

        for (SampleInfo bamQcResult : bamQCResults) {
            String path = bamQcResult.path + File.separator + "genome_results.txt";
            BamStats stats = loadSummaryStats(path);

            String[] row = new String[header.length];
            row[0] = bamQcResult.name;
            row[1] = Double.toString( stats.getMeanCoverage() );
            row[2] = Double.toString( stats.getStdCoverage() );
            row[3] = Double.toString( stats.getMeanGcRelativeContent() );
            row[4] = Double.toString( stats.getMeanMappingQualityPerWindow() );
            row[5] = Double.toString( stats.getMeanInsertSize() );
            dataSection.addRow(row);

            double[] sample = new double[NUM_FEATURES];
            sample[0] = stats.getMeanCoverage();
            sample[1] = stats.getStdCoverage();
            sample[2] = stats.getMeanGcRelativeContent();
            sample[3] = stats.getMeanMappingQualityPerWindow();
            sample[4] = stats.getMeanInsertSize();
            sampleData.add(sample);


        }
        tableDataKeeper.addSection(dataSection);



    }


    XYVector loadColumnData(File inputFile, double minX, double maxX, int dataColumn) throws IOException {
        XYVector data = new XYVector();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
        String line;
        while ( (line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }

            String[] items = line.split("\t");
            if (items.length < dataColumn + 1) {
                continue;
            }

            double d1 = Double.parseDouble(items[0]);
            if (d1 <= minX || d1 >= maxX) {
                continue;
            }
            double d2 = Double.parseDouble(items[dataColumn]);

            data.addItem( new XYItem(d1,d2));

        }
        return data;
    }


    Color getSampleColor(int idx) {
        return (Color) palette[idx % palette.length];
    }

    QChart createHistogramBasedChart(String chartName, String dataPath, String xTitle, String yTitle) throws IOException {
        BamQCChart baseChart = new BamQCChart(chartName,
                            "Multi-sample BAM QC", xTitle, yTitle);

        int i = 0;
        for (SampleInfo bamQcResult : bamQCResults) {
            String path = rawDataDirs.get(bamQcResult) + File.separator + dataPath;
            File inputFile = new File(path);
            if (!inputFile.exists()) {
                continue;
            }
            XYVector histData = loadColumnData(inputFile, 0, Double.MAX_VALUE, 1);
            baseChart.addSeries(bamQcResult.name, histData, getSampleColor(i) );
            ++i;
        }

        baseChart.render();

        return new QChart(chartName, baseChart.getChart());
    }

    QChart createReadsGcContentChart(String chartName, String dataPath, String xTitle, String yTitle) throws IOException {
            BamQCChart baseChart = new BamQCChart(chartName,
                                "Multi-sample BAM QC", xTitle, yTitle);

            int i = 0;
            for (SampleInfo bamQcResult : bamQCResults) {

                File inputFile = new File(  rawDataDirs.get(bamQcResult) + File.separator + dataPath );

                if (!inputFile.exists()) {
                    continue;
                }

                XYVector cData = loadColumnData(inputFile, 0, Double.MAX_VALUE, 2);
                XYVector gData = loadColumnData(inputFile, 0, Double.MAX_VALUE, 3);

                if (cData.getSize() != gData.getSize()) {
                    continue;
                }

                XYVector gcData = new XYVector();
                for (int j = 0; j < cData.getSize(); ++j) {
                    double pos = cData.get(j).getX();
                    double gc = cData.get(j).getY() + gData.get(j).getY();
                    gcData.addItem( new XYItem(pos,gc));
                }

                baseChart.addSeries(bamQcResult.name, gcData, getSampleColor(i));
                ++i;
            }


            // TODO: think of empty charts
            baseChart.render();

            return new QChart(chartName, baseChart.getChart());
        }



    XYVector scaleXAxis(XYVector raw) {
        XYVector scaled = new XYVector();


        double scaleFactor = 1./raw.getSize();

        for (int i = 0; i < raw.getSize(); ++i) {
            XYItem item = raw.get(i);
            XYItem newItem = new XYItem(scaleFactor*i, item.getY());
            scaled.addItem(newItem);
        }

        return scaled;

    }



    QChart createCoverageAcrossReferenceChart() throws IOException {
        BamQCChart baseChart = new BamQCChart("Coverage Across Reference",
                            "Multi-sample BAM QC", "Position in reference (relative)", "Coverage");

        DescriptiveStatistics stats = new DescriptiveStatistics();
        int k = 0;
        for (SampleInfo bamQcResult : bamQCResults) {
            String path = rawDataDirs.get(bamQcResult) + File.separator + "coverage_across_reference.txt";
            XYVector rawData = loadColumnData(new File(path), 0, Double.MAX_VALUE, 1);
            XYVector scaledData = scaleXAxis(rawData);
            for (int i = 0; i < scaledData.getSize(); ++i) {
                stats.addValue( scaledData.get(i).getY() );
            }

            baseChart.addSeries(bamQcResult.name, scaledData, getSampleColor(k) );
            ++k;
        }
        baseChart.setDomainAxisIntegerTicks(false);

        baseChart.render();
        double p75 = stats.getPercentile(75);
        if (p75 > 0) {
            baseChart.getChart().getXYPlot().getRangeAxis().setRange(0, 2*p75);
        }
        stats.clear();

        return new QChart("Coverage across reference", baseChart.getChart());
    }

    QChart createCoverageProfileChart(String chartName, String dataPath, String xTitle, String yTitle) throws IOException {
        BamQCChart baseChart = new BamQCChart(chartName,
                            "Multi-sample BAM QC", xTitle, yTitle);

        int i = 0;
        for (SampleInfo bamQcResult : bamQCResults) {
            String path = rawDataDirs.get(bamQcResult) + File.separator + dataPath;
            XYVector histData = loadColumnData(new File(path), 0, 51,1);
            baseChart.addSeries(bamQcResult.name, histData, getSampleColor(i) );
            ++i;
        }

        baseChart.render();

        return new QChart(chartName, baseChart.getChart());
    }

    private QChart createPCABiPlot() {

        PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis();
        pca.setup(bamQCResults.size(), NUM_FEATURES);

        for (double[] sample : sampleData) {
            pca.addSample( sample );
        }

        logLine("Running PCA...\n");
        pca.computeBasis(2);

        String chartName =  "PCA biplot";

        BamQCPointChart baseChart = new BamQCPointChart(chartName,
                                    "Multi-sample BAM QC", "PC1", "PC2");

        for (int i = 0; i < sampleData.size(); ++i) {
            double[] transformedSample = pca.sampleToEigenSpace(sampleData.get(i));
            baseChart.addPoint(bamQCResults.get(i).name, transformedSample[0],transformedSample[1], getSampleColor(i) );
        }

        baseChart.render();

        return new QChart(chartName, baseChart.getChart());
    }


    private void createCharts(StatsReporter reporter) throws Exception {

        ArrayList<QChart> charts = new ArrayList<QChart>();

        QChart pcaBiPlot = createPCABiPlot();
        charts.add(pcaBiPlot);

        logLine("Creating charts...\n");

        QChart coverageAcrossRefChart = createCoverageAcrossReferenceChart();
        charts.add(coverageAcrossRefChart);

        QChart coverageChart = createCoverageProfileChart("Coverage Histogram (0-50X)", "coverage_histogram.txt",
                "Coverage", "Number of loci");
        charts.add(coverageChart);

        QChart genomeFractionCoverage = createHistogramBasedChart("Genome Fraction Coverage",
                "genome_fraction_coverage.txt", "Coverage", "Fraction of genome (%)");
        charts.add(genomeFractionCoverage);

        QChart duplicationRate = createHistogramBasedChart("Duplication Rate Histogram",
                "duplication_rate_histogram.txt", "Duplication rate", "Number of loci");
        charts.add(duplicationRate);


        QChart readsGcContent = createReadsGcContentChart("Mapped reads GC-content",
                "mapped_reads_nucleotide_content.txt", "GC-content", "Read position");
        charts.add(readsGcContent);

        QChart readsClippingProfile = createHistogramBasedChart("Mapped Reads Clipping Profile",
                "mapped_reads_clipping_profile.txt", "Read position (bp)", "Clipped bases (%)");
        charts.add(readsClippingProfile);


        QChart readsGCContentDistr = createHistogramBasedChart("Mapped Reads GC-content Distribution",
                        "mapped_reads_gc-content_distribution.txt", "GC Content (%)", "Fraction of reads");
        charts.add(readsGCContentDistr);



        reporter.setChartList(charts);

    }


    private void prepareInputDescription(StatsReporter reporter) {
        HashMap<String,String> sampleParams = new HashMap<String, String>();
        for ( SampleInfo info : bamQCResults ) {
            sampleParams.put(info.name, info.path );
        }
        reporter.addInputDataSection("Samples", sampleParams);


    }


    private String findRawDataPath(String targetDir) {

        File dir = new File(targetDir);
        if (!dir.exists() || !dir.isDirectory())  {
            return null;
        }

        File[] children = dir.listFiles();
        Arrays.sort(children);

        for (File child : children ) {
            String fileName = child.getName();
            if (child.isDirectory() && fileName.startsWith("raw_data")) {
                return child.getAbsolutePath();
            }
        }

        return null;

    }


    private void checkInputPaths() throws RuntimeException {
        for (SampleInfo sampleInfo : bamQCResults) {

            String rawDataDir = findRawDataPath(sampleInfo.path);


            if (rawDataDir == null) {
                throw new RuntimeException("The raw data doesn't exist for BAM QC result folder:" + sampleInfo.path +
                        "\nPlease check raw data directory is present.\n");
            }

            rawDataDirs.put(sampleInfo, rawDataDir);

        }
    }

    public void setOutputParsingThread(LoggerThread loggerThread) {
        this.loggerThread = loggerThread;
    }

    public static List<SampleInfo> parseInputFile(String inputFilePath) throws IOException {
        ArrayList<SampleInfo> paths = new ArrayList<SampleInfo>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        while ( (line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }

            String[] items = line.trim().split("\\s+");

            if (items.length != 2) {
                continue;
            }

            paths.add(  new SampleInfo(items[0], items[1] ) );

        }

        return paths;

    }

}
