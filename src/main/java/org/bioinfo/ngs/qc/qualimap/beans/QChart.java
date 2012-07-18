/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2012 Garcia-Alcalde et al.
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

    public void setDataWriter(ChartRawDataWriter dataWriter) {
        this.dataWriter = dataWriter;
    }
}
