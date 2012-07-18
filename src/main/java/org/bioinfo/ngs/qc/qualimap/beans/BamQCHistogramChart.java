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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by kokonech
 * Date: 3/23/12
 * Time: 10:10 AM
 */
public class BamQCHistogramChart extends ChartRawDataWriter {

    private String title;
	private String subTitle;
	private String xLabel;
	private String yLabel;

	// histogram series
	XYVector data;
    Map<Double,String> barNames;

	// chart
	private JFreeChart chart;

    public BamQCHistogramChart(String title,String subTitle, String xLabel, String yLabel,
                               XYVector data, Map<Double,String> barNames){

		// main params
		this.title = title;
		this.subTitle = subTitle;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
        this.data = data;
        this.barNames = barNames;

	}

    public JFreeChart getChart() {
        return chart;
    }


    static class CustomTickUnit extends NumberTickUnit {

        double maxValue;
        Map<Double,String> tickLabels;
        public CustomTickUnit(double size, double maxValue, Map<Double,String> tickLabels) {
            super(size);
            this.maxValue = maxValue;
            this.tickLabels = tickLabels;

        }

        public String valueToString(double val) {
            if (val >= 0 && val <= maxValue){
                if (Math.floor(val) == val) {
                    if (tickLabels.containsKey(val)) {
                        //System.out.println("Val is " + val);
                        return tickLabels.get(val);
                    }
                }
            }
            return "";
        }

    }

	public void render() throws IOException {

		// init chart
		chart = ChartFactory.createHistogram(title, xLabel, yLabel, null, PlotOrientation.VERTICAL, true, true, false);


		// title
		TextTitle textTitle = new TextTitle(title);
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

		// other params
		chart.setPadding(new RectangleInsets(30,20,30,20));

        XYPlot plot = chart.getXYPlot();
		// axis style
		Font axisFont = new Font(Font.SANS_SERIF,Font.PLAIN,11);
		plot.getDomainAxis().setLabelFont(axisFont);
		plot.getRangeAxis().setLabelFont(axisFont);
		Font tickFont = new Font(Font.SANS_SERIF,Font.PLAIN,10);
		plot.getDomainAxis().setTickLabelFont(tickFont);
		plot.getRangeAxis().setTickLabelFont(tickFont);

		// grid
		plot.setBackgroundPaint(Color.WHITE);
		chart.setBackgroundPaint(new Color(230,230,230));
		plot.setDomainGridlinePaint(new Color(214,139,74));
		plot.setRangeGridlinePaint(new Color(214,139,74));

        XYSeries series = new XYSeries("Coverage");

        double[] covs = data.getXVector();
        double[] freqs = data.getYVector();
        int len = covs.length;

        for (int i = 0; i < len; ++i) {
            series.add(covs[i], freqs[i]);
        }

		// mean dataset
		chart.getXYPlot().setDataset(0, new XYSeriesCollection(series));

        NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
        //domainAxis.setTickLabelsVisible(false);
        domainAxis.setTickMarksVisible(false);
        double maxCov = covs[len - 1];
        domainAxis.setTickUnit( new CustomTickUnit(0.5,maxCov,barNames));
        domainAxis.setVerticalTickLabels(true);

		// mean renderer
		XYBarRenderer renderer = new XYBarRenderer();
		BamXYBarPainter barPainter = new BamXYBarPainter();
		renderer.setBarPainter(barPainter);
		plot.setRenderer(renderer);

	}


    @Override
    void exportData(BufferedWriter dataWriter) throws IOException{
        dataWriter.write(xLabel + "\t" + yLabel + "\n");
        int datalen =  data.getSize();
        double[] covs = data.getXVector();
        double[] freqs = data.getYVector();
        for (int i = 0; i < datalen; ++i) {
            dataWriter.write(freqs[i] + "\t" + covs[i] + "\n");
        }

    }



}
