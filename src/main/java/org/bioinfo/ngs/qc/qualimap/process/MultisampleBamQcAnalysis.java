package org.bioinfo.ngs.qc.qualimap.process;

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

    XYVector loadHistogramData(String inputFilePath, double minX, double maxX) throws IOException {
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
            XYVector histData = loadHistogramData(path,1,51);
            baseChart.addSeries(bamQcResult.name, histData, getSampleColor(i) );
            ++i;
        }

        baseChart.render();

        return new QChart(chartName, baseChart.getChart());
    }

    private void createCharts(StatsReporter reporter) throws Exception {
        ArrayList<QChart> charts = new ArrayList<QChart>();

        QChart coverageChart = createHistogramBasedChart("Coverage", "coverage_histogram.txt",
                "Coverage", "Number of positions");
        charts.add(coverageChart);

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
