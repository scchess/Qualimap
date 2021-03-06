/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2016 Garcia-Alcalde et al.
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

    protected String namePostfix, fileName;
    protected StatsKeeper inputDataKeeper;
    protected StatsKeeper summaryStatsKeeper;
    protected StatsKeeper tableDataStatsKeeper;
    protected String name;


    public StatsReporter() {
        name = "Results";
        namePostfix = "";
        fileName = "qualimapReport";
        inputDataKeeper = new StatsKeeper();
        summaryStatsKeeper = new StatsKeeper();
        tableDataStatsKeeper = new StatsKeeper();
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
                boolean  header = s.getName().equals(Constants.TABLE_STATS_HEADER);
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

        if (tableDataStatsKeeper.getSections().size() > 0) {
            addChromosomesSections(summaryHtml, width, tableDataStatsKeeper);
        }


        return summaryHtml.toString();
    }


    public String getInputDescription(int tableWidth) {


        if (inputDataKeeper.getSections().isEmpty()) {
            return null;
        }

        StringBuilder inputDesc = new StringBuilder();

        inputDesc.append("<p align=center><a name=\"input\"> <b>Input data & parameters</b></p>").append(HtmlJPanel.BR);
        inputDesc.append(HtmlJPanel.getTableHeader(tableWidth, "EEEEEE"));

        List<StatsKeeper.Section> inputDataSections = inputDataKeeper.getSections();

        for (StatsKeeper.Section section : inputDataSections) {
            inputDesc.append(HtmlJPanel.COLSTART).append("<b>").append(section.getName()).append("</b>");
            List<String[]> params = section.getRows();
            inputDesc.append(HtmlJPanel.getTableHeader(tableWidth, "FFFFFF"));
            for ( String[] row: params ) {
                inputDesc.append(HtmlJPanel.COLSTARTFIX);
                for (int i = 0; i < row.length; ++i) {
                    inputDesc.append(row[i]);
                    if (i != row.length - 1) {
                         inputDesc.append(HtmlJPanel.COLMID);
                    }
                }
                inputDesc.append(HtmlJPanel.COLEND) ;
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

    public boolean hasSummary() {
        return summaryStatsKeeper.getSections().size() > 0;
    }

    public boolean hasTableData() {
        return tableDataStatsKeeper.getSections().size() > 0;
    }

    public List<StatsKeeper.Section> getSummaryDataSections() {
        return summaryStatsKeeper.getSections();
    }

    public List<StatsKeeper.Section> getInputDataSections() {
        return inputDataKeeper.getSections();
    }

    public StatsKeeper getSummaryStatsKeeper() {
        return summaryStatsKeeper;
    }

    public StatsKeeper getTableDataStatsKeeper() {
        return tableDataStatsKeeper;
    }

    public StatsKeeper getInputDataKeeper() {
        return inputDataKeeper;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return  name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamePostfix() {
        return namePostfix;
    }

    public boolean hasInputDescription() {
        return inputDataKeeper.getSections().size() > 0;
    }
}
