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

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

/**
 * Created by kokonech
 * Date: 7/16/14
 * Time: 3:08 PM
 */
public class BamQCPointChart {


    String chartTitle, subTitle, xLabel, yLabel;
    JFreeChart chart;
    XYSeriesCollection dataset;
    List<Color> colors;
    List<String> names;
    Map<String,XYSeries> groupSeries;

    public BamQCPointChart(String title, String subTitle, String xLabel, String yLabel) {
        this.chartTitle = title;
        this.subTitle = subTitle;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        dataset = new XYSeriesCollection();
        colors = new ArrayList<Color>();
        names = new ArrayList<String>();
        groupSeries = new HashMap<String, XYSeries>();

    }

    public void addPoint(String sampleName, double x, double y, Color c) {
        XYSeries series = new XYSeries(sampleName);
        series.add(x, y);
        dataset.addSeries(series);
        colors.add(c);
        names.add(sampleName);

    }

    public void addPointToGroupSeries(String groupName, double x, double y, Color c) {
        if (groupSeries.containsKey(groupName)) {
            XYSeries series = groupSeries.get(groupName);
            series.add(x,y);
        } else {
            XYSeries s = new XYSeries(groupName);
            s.add(x,y);
            groupSeries.put(groupName, s);
            colors.add(c);
            names.add(groupName);
        }
    }

    public void finalizeGroupSeries() {
        for (String name: names) {
            XYSeries s = groupSeries.get(name);
            dataset.addSeries(s);
        }
    }

    public void render() {
        // create the chart...
        chart = ChartFactory.createXYLineChart(
                chartTitle,      // chart title
                xLabel,                      // x axis label
                yLabel,                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
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

        final XYPlot plot = chart.getXYPlot();

        // grid
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(new Color(214, 139, 74));
        plot.setRangeGridlinePaint(new Color(214, 139, 74));
        plot.setDomainMinorGridlinesVisible(true);
        plot.setDomainMinorGridlinePaint(Color.red);


        // prepare legend
		LegendItemSource lis = new LegendItemSource() {
			LegendItemCollection lic = new LegendItemCollection();
			public LegendItemCollection getLegendItems() {
				return lic;
			}
		};

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseLinesVisible(false);
        plot.setRenderer(renderer);

        for (int i = 0; i < plot.getSeriesCount(); ++i) {

            LegendItem legendItem = new LegendItem(names.get(i));
			legendItem.setLabelFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));
			legendItem.setLabelPaint(Color.darkGray);
            legendItem.setFillPaint(colors.get(i));
            lis.getLegendItems().add(legendItem);

            double size =   10.0;
            double delta = size / 2;
            renderer.setSeriesShape(i, new Ellipse2D.Double(-delta, -delta, size, size));
            renderer.setSeriesPaint(i, colors.get(i));

        }

        chart.addLegend(new LegendTitle(lis));
        chart.getLegend().setMargin(2,0,-7,10);
        chart.getLegend().setVerticalAlignment(VerticalAlignment.TOP);
        chart.getLegend().setHorizontalAlignment(HorizontalAlignment.RIGHT);
        chart.getLegend().setLegendItemGraphicPadding(new RectangleInsets(5,5,5,5));


        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());




    }


    public JFreeChart getChart() {
        return chart;
    }

}
