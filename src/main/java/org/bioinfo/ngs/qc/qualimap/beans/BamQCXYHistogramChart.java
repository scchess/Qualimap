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

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class BamQCXYHistogramChart extends ChartRawDataWriter {
	// org.bioinfo.ntools.main params
	private String title;
	private String subTitle;
	private String xLabel;
	private String yLabel;
	
	// histogram series
	private List<String> names;
	private List<XYVector> histograms;
	private List<Color> colors;
	
	// chart
	private JFreeChart chart;
	
	private int numberOfBins;
	private boolean cumulative;
	private boolean zoomed;
	private double maxValue;
	private boolean rangeAxisIntegerTicks;
	private boolean domainAxisIntegerTicks;
	private boolean adjustDomainAxisLimits;
    private double domainAxisTickUnitSize;
	
	public BamQCXYHistogramChart(String title,String subTitle, String xLabel, String yLabel){
		
		// main params
		this.title = title;
		this.subTitle = subTitle;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		
		// 
		names = new ArrayList<String>();
		histograms = new ArrayList<XYVector>();
		colors = new ArrayList<Color>();
		numberOfBins = 50;
		maxValue = 100;
		adjustDomainAxisLimits = true;
		
	}
	
	
	public void addHistogram(String name, XYVector histogram, Color color){
		names.add(name);
		histograms.add(histogram);
		colors.add(color);
	}
	
	
	public void render() throws IOException{
		
		// init chart
		chart = ChartFactory.createXYBarChart(title,xLabel,false,yLabel, null, PlotOrientation.VERTICAL, true, true, false);
		
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
		if(domainAxisIntegerTicks){
			NumberAxis numberaxis = (NumberAxis)chart.getXYPlot().getDomainAxis();   
		    numberaxis.setAutoRangeIncludesZero(false);   
		    numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		}

        if (domainAxisTickUnitSize != 0) {
            NumberAxis numberaxis = (NumberAxis)chart.getXYPlot().getDomainAxis();
            numberaxis.setTickLabelFont(new Font(Font.SANS_SERIF,Font.PLAIN,8));
		    numberaxis.setTickUnit( new NumberTickUnit(domainAxisTickUnitSize));
        }

		if(rangeAxisIntegerTicks){
			NumberAxis numberaxis = (NumberAxis)chart.getXYPlot().getRangeAxis();
		    numberaxis.setAutoRangeIncludesZero(false);
		    numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        }


		// grid
		plot.setBackgroundPaint(Color.WHITE);
		chart.setBackgroundPaint(new Color(230,230,230));
		plot.setDomainGridlinePaint(new Color(214,139,74));
		plot.setRangeGridlinePaint(new Color(214,139,74));
		
		// prepare series		
		if(!zoomed && histograms.get(0).getSize() > 0) {
			maxValue = histograms.get(0).get(histograms.get(0).getSize()-1).getX();
		}
		
		double inc = maxValue/(double)numberOfBins;		
		double [] covs = new double[numberOfBins+1];	
		for(int i=0; i<covs.length; i++){
			covs[i] = inc*i;
		}
		
		// convert to bins
		double [] values = new double[numberOfBins+1];		
		double [] rfreqs = new double[numberOfBins+1];
		XYItem item;
		int pos;
		double total = 0;
		
		for(int i=0; i<histograms.get(0).getSize(); i++){
			item = histograms.get(0).get(i);
//			if(item.getX()>0){
				pos = (int)Math.floor(item.getX()/inc);
				if(pos<covs.length){
					values[pos] = values[pos] + item.getY();
					rfreqs[pos] = rfreqs[pos] + 1;					
				}
				total+=item.getY();
//			}
		}

	
		
		XYSeries series = new XYSeries("frequencies");
		double acum = 0;
		double next;

		for(int i=0; i<values.length; i++){
			if(cumulative){
				acum += (values[i]/total)*100.0;
				next = acum;
			} else {
				next = values[i];
			}			
			series.add(covs[i],next);			
		}
				
		// mean dataset
		chart.getXYPlot().setDataset(0, new XYSeriesCollection(series));
		
		// mean renderer
		XYBarRenderer renderer = new XYBarRenderer();
		BamXYBarPainter barPainter = new BamXYBarPainter();
		renderer.setBarPainter(barPainter);
		plot.setRenderer(renderer);

		// adjust axis limits
		if(adjustDomainAxisLimits && histograms.get(0).getSize() > 0){
            double minDomainAxis = histograms.get(0).get(0).getX() - inc/2.0;
		    double maxDomainAxis = maxValue + inc/2.0;
			chart.getXYPlot().getDomainAxis().setRange(minDomainAxis,maxDomainAxis); 
		}
	}


	public void zoom(double maxValue){
		this.maxValue = maxValue;
		zoomed = true;		
	}
	
	/**
	 * @return the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}


	/**
	 * @param numberOfBins the numberOfBins to set
	 */
	public void setNumberOfBins(int numberOfBins) {
		this.numberOfBins = numberOfBins;
	}


	/**
	 * @param rangeAxisIntegerTicks the rangeAxisIntegerTicks to set
	 */
	public void setRangeAxisIntegerTicks(boolean rangeAxisIntegerTicks) {
		this.rangeAxisIntegerTicks = rangeAxisIntegerTicks;
	}


	/**
	 * @param domainAxisIntegerTicks the domainAxisIntegerTicks to set
	 */
	public void setDomainAxisIntegerTicks(boolean domainAxisIntegerTicks) {
		this.domainAxisIntegerTicks = domainAxisIntegerTicks;
	}


    public void setDomainAxisTickUnitSize(double domainAxisTickUnitSize) {
        this.domainAxisTickUnitSize = domainAxisTickUnitSize;
    }

    @Override
    void exportData(BufferedWriter dataWriter) throws IOException {

        dataWriter.write("#" + xLabel);

        //Export whole data set

        for (String name : names) {
            dataWriter.write("\t" + name);
        }
        dataWriter.write("\n");

        int len = histograms.get(0).getSize();
        double[] xData = histograms.get(0).getXVector();

        for (int i = 0; i < len; ++i) {
            dataWriter.write(Double.toString(xData[i]));
            for (XYVector data : histograms) {
                dataWriter.write("\t" + data.get(i).getY());
            }
            dataWriter.write("\n");
        }

    }
}