package org.bioinfo.ngs.qc.qualimap.utils;

import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StatsKeeper;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StatsKeeper.Section;
import org.jfree.chart.JFreeChart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by kokonech
 * Date: 3/13/12
 * Time: 10:08 AM
 */
public class HtmlReportGenerator {

    BamQCRegionReporter reporter;
    boolean genomicAnalysis;
    String dirPath;
    StringBuffer htmlReport;
    List<String> plotNames, plotLinks;

    public HtmlReportGenerator(BamQCRegionReporter reporter, String dirPath, boolean genomicAnalysis) {
        this.reporter = reporter;
        this.genomicAnalysis = genomicAnalysis;
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
        append("\t<title>Qualimap report: Genomic analysis</title>\n");
        append("\t</head>");

    }


    void appendHeaderWrapper() {
        append("\t<div class=\"header-wrapper\">");
        append("\t\t<div class=\"header\">");
        append("\t\t<p class=\"logo\"><a href=\"http://qualimap.bioinfo.cipf.es/\">");
        append("\t\t<img class=\"logo\" src=\"/home/kokonech/_static/qualimap_logo_small.png\" " +
                "alt=\"Logo\"/>");
        append("\t\t</a></p>");
        append("<div class=\"headertitle\">" +
                "<a href=\"\">Qualimap Report</a></div>");
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

        if (genomicAnalysis) {
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

        List<Section> summarySections = reporter.getSummaryInputSections();
        appendTableFromStats(summarySections);


		if (genomicAnalysis) {
			appendChromosomeStats();
        }

        append("</div>");

        append("</div> <!-- summary section -->\n");

    }

    void appendTableFromStats(List<Section> sections) {
        for(Section s : sections) {
            append("\n<div class=table-summary>");
            append("<h3>" + s.getName() + "</h3>" );
            append("<table class=summary>");
            for (String[] row : s.getRows()) {
                append("<tr>");
                append("<td class=column1>" + row[0] + "</td>");
                append("<td class=column2>" + row[1] + "</td>");
                append("</tr>");
            }
            append("</table>");
            append("</div>\n");
        }

    }

    void appendChromosomeStats() {

        List<Section> sections = reporter.getChromosomeSections();

        append("\n<div class=table-summary>");
        append("<h3>" + "Per chromosome statistics" + reporter.getNamePostfix() + "</h3>" );
        append("<table class=summary>");

        for(Section s : sections) {
            boolean sectionIsHeader = s.getName().equals(Constants.CHROMOSOME_STATS_HEADER);
            for (String[] row : s.getRows()) {
                append("<tr>");
                for (String item : row) {
                    if (sectionIsHeader) {
                        append("<td><b>" + item + "</b></td>");
                    } else {
                        append("<td>" + item + "</b></td>");
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
        Iterator<?> it = genomicAnalysis ? reporter.getMapCharts().entrySet().iterator()
                : reporter.getImageMap().entrySet().iterator();


        htmlReport.append("\n\n");
        while(it.hasNext()){
            @SuppressWarnings("unchecked")
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();

            String name = entry.getKey();

            if(entry.getValue() instanceof JFreeChart){
                name = ((JFreeChart)entry.getValue()).getTitle().getText();
            }

            String imagePath = getImagePath(entry.getKey());

            plotNames.add(name);
            plotLinks.add(entry.getKey());

            append("<div class=section>\n");
            append("<h2>" + name + "</h2><a class=\"headerlink\" name=\""
                    + entry.getKey() + "\">"
                    + "title=\"Permalink to this headline\">&nbsp;</a></h2>\n");
            append("<div><img src=" + imagePath + "></div>\n");
            append("</div><!-- graph section -->\n\n");


        }

    }

    String getImagePath(String imageName) {
        String imagePath = dirPath + "/" + imageName;
        String extension = imageName.substring(imageName.lastIndexOf(".") + 1);
        if (!extension.equalsIgnoreCase("png")) {
            imagePath += ".png";
        }
        return imagePath;
    }

    void appendSideBar() {
        append("\n<div class=\"sidebar\">");
        append("<h3>Contents</h3>");

        if (genomicAnalysis) {
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
        append("Generated by QualiMap");
        append("</div");
        append("</div>");

        append("<div class=\"clearer\"></div>");
        append("</div> <!-- footer -->");
        append("</div>");
    }


}
