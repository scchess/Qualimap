package org.bioinfo.ngs.qc.qualimap.process;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.bioinfo.ngs.qc.qualimap.beans.*;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
import org.jfree.chart.ChartColor;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kokonech
 * Date: 6/5/14
 * Time: 12:45 PM
 */
public class MultisampleBamQcAnalysis extends AnalysisProcess{


    List<SampleInfo> bamQCResults;
    LoggerThread loggerThread;
    Paint[] palette;
    String rawDataDir;

    public MultisampleBamQcAnalysis(AnalysisResultManager tabProperties,
                                    String homePath,
                                    List<SampleInfo> bamQCResults) {
        super(tabProperties, homePath);
        this.bamQCResults = bamQCResults;
        this.palette = ChartColor.createDefaultPaintArray();
        this.rawDataDir = "raw_data_qualimapReport";
    }

    @Override
    public void run() throws Exception {
        checkInputPaths();

        StatsReporter reporter = new StatsReporter();
        reporter.setFileName( "multisampleBamQcReport" );


        prepareInputDescription(reporter);
        createCharts(reporter);


        tabProperties.addReporter(reporter);

    }

    XYVector loadColumnData(String inputFilePath, double minX, double maxX) throws IOException {
        XYVector data = new XYVector();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        while ( (line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }

            String[] items = line.split("\t");
            if (items.length < 2) {
                continue;
            }

            double d1 = Double.parseDouble(items[0]);
            if (d1 <= minX || d1 >= maxX) {
                continue;
            }
            double d2 = Double.parseDouble(items[1]);

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
            String path = bamQcResult.path + File.separator + rawDataDir + File.separator + dataPath;
            XYVector histData = loadColumnData(path, 0, Double.MAX_VALUE);
            baseChart.addSeries(bamQcResult.name, histData, getSampleColor(i) );
            ++i;
        }

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
        BamQCChart baseChart = new BamQCChart("Coverage across reference",
                            "Multi-sample BAM QC", "Position in reference (relative)", "Coverage");

        DescriptiveStatistics stats = new DescriptiveStatistics();
        int k = 0;
        for (SampleInfo bamQcResult : bamQCResults) {
            String path = bamQcResult.path + File.separator + rawDataDir + File.separator + "coverage_across_reference.txt";
            XYVector rawData = loadColumnData(path, 0, Double.MAX_VALUE);
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
            String path = bamQcResult.path + File.separator + rawDataDir + File.separator + dataPath;
            XYVector histData = loadColumnData(path, 1, 51);
            baseChart.addSeries(bamQcResult.name, histData, getSampleColor(i) );
            ++i;
        }

        baseChart.render();

        return new QChart(chartName, baseChart.getChart());
    }


    private void createCharts(StatsReporter reporter) throws Exception {
        ArrayList<QChart> charts = new ArrayList<QChart>();

        QChart coverageAcrossRefChart = createCoverageAcrossReferenceChart();
        charts.add(coverageAcrossRefChart);

        QChart coverageChart = createCoverageProfileChart("Coverage Profile (1-50X)", "coverage_histogram.txt",
                "Coverage", "Number of loci");
        charts.add(coverageChart);

        QChart genomeFractionCoverage = createHistogramBasedChart("Reference Fraction Coverage",
                "genome_fraction_coverage.txt", "Coverage", "Fraction of reference (%)");
        charts.add(genomeFractionCoverage);

        QChart duplicationRate = createHistogramBasedChart("Duplication Rate",
                "duplication_rate_histogram.txt", "Duplication rate", "Number of loci");
        charts.add(duplicationRate);

        QChart readsGCContentDistr = createHistogramBasedChart("Mapped reads GC content",
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

    private void checkInputPaths() throws RuntimeException {
        for (SampleInfo sampleInfo : bamQCResults) {
            File dataFolder = new File( sampleInfo.path + File.separator + rawDataDir);
            if (!dataFolder.exists()) {
                throw new RuntimeException("The raw data doesn't exist for BAM QC result " + sampleInfo.path);
            }

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
