/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2014 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.common;

import org.bioinfo.ngs.qc.qualimap.beans.QChart;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;
import org.bioinfo.ngs.qc.qualimap.gui.utils.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StatsKeeper;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StatsKeeper.Section;
import org.bioinfo.ngs.qc.qualimap.main.NgsSmartMain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by kokonech
 * Date: 3/13/12
 * Time: 10:08 AM
 */
public class HtmlReportGenerator {

    StatsReporter reporter;
    AnalysisType analysisType;
    String dirPath;
    StringBuffer htmlReport;
    List<String> plotNames, plotLinks;

    public HtmlReportGenerator(StatsReporter reporter, String dirPath, AnalysisType analysisType) {
        this.reporter = reporter;
        this.analysisType = analysisType;
        this.dirPath = dirPath;
    }

    public StringBuffer getReport() {

        htmlReport = new StringBuffer();
        plotNames = new ArrayList<String>();
        plotLinks = new ArrayList<String>();

        appendHeader();
        appendBody();

        return htmlReport;

    }

    void append(String line) {
        htmlReport.append(line).append("\n");
    }

    void appendHeader() {

        append("<!DOCTYPE HTML>");
        append("<html>");
        append("\t<head>");
        append("\t\t<link rel=\"stylesheet\" href=\"_static/agogo.css\" type=\"text/css\" />");
        append("\t\t<link rel=\"stylesheet\" href=\"_static/report.css\" " +
                "type=\"text/css\" />");
        append("\t<title>Qualimap report: " + analysisType.toString() + "</title>\n");
        append("\t</head>");

    }


    void appendHeaderWrapper() {
        append("\t<div class=\"header-wrapper\">");
        append("\t\t<div class=\"header\">");
        append("\t\t<p class=\"logo\"><a href=\"http://qualimap.bioinfo.cipf.es/\">");
        append("\t\t<img class=\"logo\" src=\"_static/qualimap_logo_small.png\" " +
                "alt=\"Logo\"/>");
        append("\t\t</a></p>");
        append("<div class=\"headertitle\">" +
                "<a href=\"\">Qualimap Report: " + analysisType.toString() + "</a></div>");
        append("\t</div>");
        append("</div>\n");
    }

    void appendContentWrapper() {
        append("<div class=\"content-wrapper\">");
        append("<div class=\"content\">");
        appendDocument();
        appendSideBar();
        appendClearer();
        append("</div>\n");
        append("</div>\n");
    }

    private void appendClearer() {
        append("<div class=\"clearer\"></div>");
    }


    void appendDocument() {

        append("<div class=\"document\">");
        append("<div class=\"documentwrapper\">");
        append("<div class=\"bodywrapper\">");
        append("<div class=\"body\">");

        if (reporter.hasSummary()) {
            appendSummary();
        }

        appendInputData();

        appendGraphs();

        append("</div>\n");
        append("</div>\n");
        append("</div>\n");
        append("</div>\n");

    }


    void appendSummary() {

        append("<div class=section>");
        append("<h2>Summary<a class=\"headerlink\" " +
         "name=\"summary\" title=\"Permalink to this headline\">&nbsp;</a></h2>");


        append("<div class=summary>\n");

        List<Section> summarySections = reporter.getSummaryDataSections();
        appendTableFromStats(summarySections);


		if (analysisType.isBamQC()) {
			appendChromosomeStats();
        }

        append("</div>");

        append("</div> <!-- summary section -->\n");

    }

    void appendTableFromStats(List<Section> sections) {
        for(Section s : sections) {
            append("\n<div class=table-summary>");
            append("<h3>" + s.getName() + "</h3>" );
            append("<table class=\"summary hovertable\">");
            for (String[] row : s.getRows()) {
                append("<tr onmouseover=\"this.style.backgroundColor='#EEEEEC';\" " +
                        "onmouseout=\"this.style.backgroundColor='#FFFFFF';\">");
                append("<td class=column1>" + row[0] + "</td>");
                append("<td class=column2>" + row[1] + "</td>");
                append("</tr>");
            }
            append("</table>");
            append("</div>\n");
        }

    }

    void appendChromosomeStats() {

        StatsKeeper chrStatsKeeper = reporter.getChromosomeStats();
        List<Section> sections = chrStatsKeeper.getSections();

        append("\n<div class=table-summary>");
        append("<h3>" + chrStatsKeeper.getName() + "</h3>" );
        append("<table class=\"summary hovertable\">");

        for(Section s : sections) {
            boolean sectionIsHeader = s.getName().equals(Constants.CHROMOSOME_STATS_HEADER);
            for (String[] row : s.getRows()) {
                append("<tr onmouseover=\"this.style.backgroundColor='#EEEEEC';\" " +
                        "onmouseout=\"this.style.backgroundColor='#FFFFFF';\">");
                for (String item : row) {
                    if (sectionIsHeader) {
                        append("<td><b>" + item + "</b></td>");
                    } else {
                        append("<td>" + item + "</td>");
                    }
                }
                append("</tr>");
            }
        }

        append("</table>");
        append("</div>\n");

    }


    void appendInputData() {
        append("<div class=section>");
        append("<h2>Input data and parameters<a class=\"headerlink\" " +
                "name=\"input\" title=\"Permalink to this headline\">&nbsp;</a></h2>");


        append("<div class=summary>\n");

        List<Section> inputSections = reporter.getInputDataSections();
        appendTableFromStats(inputSections);

        append("</div>");

        append("</div> <!-- summary section -->\n");

    }

    void appendGraphs() {

        htmlReport.append("\n\n");
        for (QChart chart : reporter.getCharts()) {

            String imagePath = getImagePath(chart.getName());

            plotNames.add(chart.getTitle());
            plotLinks.add(chart.getName());

            append("<div class=section>\n");
            append("<h2>" + chart.getTitle() + "<a class=\"headerlink\" name=\""
                    + chart.getName() + "\" "
                    + "title=\"Permalink to this headline\">&nbsp;</a></h2>\n");
            append("<div><img width=100% src=\"" + imagePath + "\"></div>\n");
            append("</div><!-- graph section -->\n\n");


        }

    }

    String getImagePath(String imageName) {
        String imagePath = imageName;
        String extension = imageName.substring(imageName.lastIndexOf(".") + 1);
        if (!extension.equalsIgnoreCase("png")) {
            imagePath += ".png";
        }
        return imagePath;
    }

    void appendSideBar() {
        append("\n<div class=\"sidebar\">");
        append("<h3>Contents</h3>");

        if (analysisType.isBamQC()) {
            append("<li class=\"toctree-l1\"><a class=\"reference internal\" href=\"#summary\">Summary</a></li>");
        }

        append("<li class=\"toctree-l1\"><a class=\"reference internal\" href=\"#input\">Input data & parameters</a></li>");

        int sz = plotNames.size();

        for (int i = 0; i < sz; ++i) {
            append("<li class=\"toctree-l1\"><a class=\"reference internal\" " +
                    "href=\"#" + plotLinks.get(i) + "\">" + plotNames.get(i) + "</a></li>");
        }

        append("</div> <!-- sidebar -->\n");
    }

    void appendBody() {
        append("<body>");
        appendHeaderWrapper();
        appendContentWrapper();
        appendFooter();
        append("</body>");
    }

    private void appendFooter() {
        append("\n<div class=\"footer-wrapper\">");
        append("<div class=\"footer\">");

        //left
        append("<div class=\"left\">");
        append("<div class=\"footer\">");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        append(dateFormat.format(date));
        append("</div>");
        append("</div>");

        //right
        append("<div class=\"right\">");
        append("<div class=\"footer\">");
        append("Generated by QualiMap " + NgsSmartMain.APP_VERSION );
        append("</div");
        append("</div>");

        append("<div class=\"clearer\"></div>");
        append("</div> <!-- footer -->");
        append("</div>");
    }


}
