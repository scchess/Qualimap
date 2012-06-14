package org.bioinfo.ngs.qc.qualimap.beans;

import org.jfree.chart.JFreeChart;

import java.awt.image.BufferedImage;

/**
 * Created by kokonech
 * Date: 3/29/12
 * Time: 12:01 PM
 */
public class QChart {

    JFreeChart chart;
    BufferedImage bufferedImage;
    String title, toolTip, name;
    ChartRawDataWriter dataWriter;

    public QChart(String name, JFreeChart chart) {

        this.name = name;
        this.title = chart.getTitle().getText();
        this.chart = chart;
        this.toolTip = chart.getTitle().getText();
        this.bufferedImage = null;

    }

     public QChart(String name, JFreeChart chart, ChartRawDataWriter writer) {

        this.name = name;
        this.title = chart.getTitle().getText();
        this.chart = chart;
        this.toolTip = chart.getTitle().getText();
        this.bufferedImage = null;
        this.dataWriter = writer;

    }


    public QChart(String imageName, String title, BufferedImage image) {
        this.name = imageName;
        this.title = title;
        this.chart = null;
        this.toolTip = title;
        this.bufferedImage = image;

    }


    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getToolTip() {
        return toolTip;
    }


    public boolean isBufferedImage() {
        return chart == null;
    }

    public JFreeChart getJFreeChart() {
        return chart;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public boolean canExportRawData() {
        return dataWriter != null;
    }


    public ChartRawDataWriter getDataWriter() {
        return dataWriter;
    }
}
