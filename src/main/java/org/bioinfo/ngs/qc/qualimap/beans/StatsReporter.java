package org.bioinfo.ngs.qc.qualimap.beans;

import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.panels.HtmlJPanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StatsKeeper;

import java.util.List;
import java.util.Map;

/**
 * Created by kokonech
 * Date: 7/13/13
 * Time: 2:16 PM
 */
public class StatsReporter {
    protected List<QChart> charts;

    protected String reportName, fileName;
    protected StatsKeeper inputDataKeeper;
    protected StatsKeeper summaryStatsKeeper;
    protected StatsKeeper chromosomeStatsKeeper;


    public StatsReporter() {
        reportName = "Results";
        fileName = "qualimapReport";
        inputDataKeeper = new StatsKeeper();
        summaryStatsKeeper = new StatsKeeper();
        chromosomeStatsKeeper = new StatsKeeper();
    }

    public void addInputDataSection(String name, Map<String,String> paramsMap) {

        StatsKeeper.Section section = new StatsKeeper.Section(name);
        section.addData(paramsMap);

        inputDataKeeper.addSection(section);
    }

    public static void addSummarySection(StringBuffer buf, StatsKeeper.Section s, int width) {

            buf.append(HtmlJPanel.COLSTART).append("<b>").append(s.getName()).append("</b>");
            buf.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));

            for (String[] row : s.getRows()) {
                buf.append(HtmlJPanel.COLSTARTFIX).append(row[0]).append(HtmlJPanel.COLMID)
                        .append(row[1]).append(HtmlJPanel.COLEND);
            }

            buf.append(HtmlJPanel.getTableFooter());
            buf.append(HtmlJPanel.COLEND);


        }

        public static void addChromosomesSections(StringBuffer summaryHtml,
                                                  int width,
                                                  StatsKeeper chromosomeStats) {
            List<StatsKeeper.Section> chromosomeSections = chromosomeStats.getSections();
            summaryHtml.append(HtmlJPanel.COLSTART).append("<b>").append(chromosomeStats.getName()).append("</b>");
            summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));

            for (StatsKeeper.Section s : chromosomeSections ) {
                boolean  header = s.getName().equals(Constants.CHROMOSOME_STATS_HEADER);
                List<String[]> rows = s.getRows();
                for (String[] row : rows) {
                    summaryHtml.append("<tr>");
                    for (String item : row) {
                        if (header) {
                            summaryHtml.append("<td><b>").append(item).append("</b></td>");
                        }   else {
                            summaryHtml.append("<td>").append(item).append("</td>");
                        }
                    }
                    summaryHtml.append("</tr>");
                }

            }

            summaryHtml.append(HtmlJPanel.getTableFooter());
            summaryHtml.append(HtmlJPanel.COLEND);
        }


    public String getSummary(int width) {
        StringBuffer summaryHtml = new StringBuffer("");

        List<StatsKeeper.Section> summarySections = summaryStatsKeeper.getSections();
        summaryHtml.append("<p align=center> <b>Summary</b></p>").append(HtmlJPanel.BR);
        summaryHtml.append(HtmlJPanel.getTableHeader(width, "EEEEEE"));


        for (StatsKeeper.Section s: summarySections) {
            addSummarySection(summaryHtml, s, width);
        }

        if (chromosomeStatsKeeper.getSections().size() > 0) {
            addChromosomesSections(summaryHtml, width, chromosomeStatsKeeper);
        }


        return summaryHtml.toString();
    }


    public String getInputDescription(int tableWidth) {


        if (inputDataKeeper.getSections().isEmpty()) {
            return "No input description is available";
        }

        StringBuilder inputDesc = new StringBuilder();

        inputDesc.append("<p align=center><a name=\"input\"> <b>Input data & parameters</b></p>" + HtmlJPanel.BR);
        inputDesc.append(HtmlJPanel.getTableHeader(tableWidth, "EEEEEE"));

        List<StatsKeeper.Section> inputDataSections = inputDataKeeper.getSections();

        for (StatsKeeper.Section section : inputDataSections) {
            inputDesc.append(HtmlJPanel.COLSTART).append("<b>").append(section.getName()).append("</b>");
            List<String[]> params = section.getRows();
            inputDesc.append(HtmlJPanel.getTableHeader(tableWidth, "FFFFFF"));
            for ( String[] row: params ) {
                 inputDesc.append(HtmlJPanel.COLSTARTFIX).append(row[0]).
                         append(HtmlJPanel.COLMID).append( row[1] ).append( HtmlJPanel.COLEND) ;
            }
            inputDesc.append(HtmlJPanel.getTableFooter());
            inputDesc.append(HtmlJPanel.COLEND);
        }

        inputDesc.append(HtmlJPanel.getTableFooter());

        return inputDesc.toString();

    }



    public List<QChart> getCharts() {
        return charts;
    }

    public void setChartList(List<QChart> chartList) {
        charts = chartList;
    }

    public QChart findChartByName(String name) {
        for (QChart chart : charts) {
            if (chart.getName().equals(name)) {
                return chart;
            }
        }

        return null;
    }


    public String getName() {
        return reportName;
    }

    public boolean hasSummary() {
        return summaryStatsKeeper.getSections().size() > 0;
    }

    public List<StatsKeeper.Section> getSummaryDataSections() {
        return summaryStatsKeeper.getSections();
    }

    public List<StatsKeeper.Section> getInputDataSections() {
        return inputDataKeeper.getSections();
    }

    public StatsKeeper getChromosomeStats() {
        return chromosomeStatsKeeper;
    }

    public String getFileName() {
        return fileName;
    }
}
