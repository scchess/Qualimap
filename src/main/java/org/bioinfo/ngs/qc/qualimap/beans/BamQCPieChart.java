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
package org.bioinfo.ngs.qc.qualimap.beans;

import org.bioinfo.commons.utils.StringUtils;
import org.jfree.chart.*;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.text.AttributedString;
import java.util.Arrays;

/**
 * Created by kokonech
 * Date: 8/18/14
 * Time: 4:04 PM
 */
public class BamQCPieChart {
    String chartTitle, subTitle;
    JFreeChart chart;
    PieDataset pieDataset;

    static class CustomLabelGenerator implements PieSectionLabelGenerator {


        @Override
        public String generateSectionLabel(final PieDataset dataset, final Comparable key) {
            String result = null;
            if (dataset != null) {
                String formattedVal = StringUtils.decimalFormat(dataset.getValue(key).doubleValue(), "###.##") + "%";
                result = key.toString() + " - " + formattedVal;
            }
            return result;
        }

        @Override
        public AttributedString generateAttributedSectionLabel(PieDataset dataset, Comparable key) {
            return null;
        }

    }


    public BamQCPieChart(String chartTitle, String subTitle, PieDataset pieDataset ) {
        this.chartTitle = chartTitle;
        this.subTitle = subTitle;
        this.pieDataset = pieDataset;
    }


    public void render() {

        // create the chart...
        chart = ChartFactory.createPieChart(
                chartTitle,      // chart title
                pieDataset,                  // data
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // title
        TextTitle textTitle = new TextTitle(chartTitle);
        textTitle.setFont(new Font(Font.SANS_SERIF,Font.BOLD,18));
        textTitle.setPaint(Color.darkGray);
        chart.setTitle(textTitle);

        // subtitle
        TextTitle sub = new TextTitle(subTitle);
        Font subFont = new Font(Font.SANS_SERIF,Font.PLAIN,12);
        sub.setFont(subFont);
        sub.setPadding(5, 5, 15, 5);
        sub.setPaint(Color.darkGray);
        chart.setSubtitles(Arrays.asList(sub));

        //background
        chart.setBackgroundPaint(new Color(230,230,230));

        // other params
        chart.setPadding(new RectangleInsets(30,20,30,20));

        //final StandardLegend legend = (StandardLegend) chart.getLegend();
        //legend.setDisplaySeriesShapes(true);

        PiePlot piePlot = (PiePlot) chart.getPlot();
        piePlot.setNoDataMessage("No data available");
        //piePlot.setCircular(false);
        piePlot.setLabelGap(0.02);
        piePlot.setLabelGenerator( new CustomLabelGenerator() );



        // grid
        piePlot.setBackgroundPaint(Color.white);



    }

    public JFreeChart getJFreeChart() {
        return chart;
    }

}
