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

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.*;
import org.jfree.ui.*;

public class BamQCChart extends ChartRawDataWriter implements Serializable {

    private String title;
	private String subTitle;
	private String xLabel;
	private String yLabel;
	
	// series
	private List<String> names;
	private List<XYVector> series;
	private List<Color> colors;
	private List<Stroke> strokes;
	private List<AbstractXYItemRenderer> renderers;
    private XYToolTipGenerator toolTipGenerator;
	private int numberOfSeries;
    private int seriesToExportIndex;
	
	// other params
	private PlotOrientation orientation;
	private boolean aPercentageChart;
	private boolean rangeAxisIntegerTicks;
	private boolean domainAxisIntegerTicks;
	private boolean adjustDomainAxisLimits;
    private boolean showLegend;
	
	// chart
	private JFreeChart chart;
    private double domainAxisTickUnitSize;


    public BamQCChart(String title,String subTitle, String xLabel, String yLabel){
		// init  org.bioinfo.ntools.main params
		this.title = title;
		this.subTitle = subTitle;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
	
		// init series
		names = new ArrayList<String>();
		series = new ArrayList<XYVector>();
		colors = new ArrayList<Color>();
		strokes = new ArrayList<Stroke>();
		renderers = new ArrayList<AbstractXYItemRenderer>();
		numberOfSeries = 0;
		
		// other params
		orientation = PlotOrientation.VERTICAL;
		adjustDomainAxisLimits = true;
        domainAxisTickUnitSize = 0;
        seriesToExportIndex = -1;
        showLegend = true;
	}
	
	// line rendered series
	public void addSeries(String name, XYVector series, Color color){
		addSeries(name,series,color,new BasicStroke(1.5f), true);
	}
	
	public void addSeries(String name, XYVector series, Color color, Stroke stroke, boolean visibleInLegend,
                          List<XYBoxAnnotation> annotations){
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (XYBoxAnnotation ann : annotations) {
            renderer.addAnnotation(ann, Layer.BACKGROUND);
        }
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesVisibleInLegend(0, visibleInLegend);
        addSeries(name,series,color,stroke,renderer);
	}

	public void addSeries(String name, XYVector series, Color color, Stroke stroke, boolean visibleInLegend){
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesVisibleInLegend(0, visibleInLegend);
        addSeries(name,series,color,stroke,renderer);
	}


	// bar rendered series
	public void addBarRenderedSeries(String name, XYVector series, Color color){		
		addBarRenderedSeries(name,series,color,new BasicStroke(1.5f));		
	}
	
	
	public void addBarRenderedSeries(String name, XYVector series, Color color, Stroke stroke){
		XYBarRenderer renderer = new XYBarRenderer();
		BamXYBarPainter barPainter = new BamXYBarPainter(color);
		renderer.setBarPainter(barPainter);
        addSeries(name, series, color, stroke, renderer);
	}
	
	
	// bar rendered series
	public void addIntervalRenderedSeries(String name, XYVector series, Color lineColor, Color deviationColor, float alpha){		
		addIntervalRenderedSeries(name,series,lineColor,deviationColor,alpha,new BasicStroke(1.5f));		
	}
	
	
	public void addIntervalRenderedSeries(String name, XYVector series, Color lineColor, Color deviationColor, float alpha, Stroke stroke){
		DeviationRenderer renderer = new DeviationRenderer(true,false);
		renderer.setSeriesFillPaint(0,deviationColor);
		renderer.setAlpha(alpha);
        addSeries(name, series, lineColor, stroke, renderer);
	}
	
	
	public void addSeries(String name, XYVector series, Color color, Stroke stroke, AbstractXYItemRenderer renderer){
		this.names.add(name);
		this.series.add(series);
		this.colors.add(color);
		this.strokes.add(stroke);
		this.renderers.add(renderer);
		numberOfSeries++;
	}
	
	void setToolTipGenerator(XYToolTipGenerator toolTipGenerator) {
        this.toolTipGenerator = toolTipGenerator;
    }

    public void setAdjustDomainAxisLimits(boolean adjustDomainAxisLimits) {
        this.adjustDomainAxisLimits = adjustDomainAxisLimits;
    }

    public void render(){
		// init chart
		chart = ChartFactory.createXYLineChart(title,xLabel,yLabel, null, PlotOrientation.VERTICAL, true, true, false);
		
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
		
		// axis style
		Font axisFont = new Font(Font.SANS_SERIF,Font.PLAIN,11);
		chart.getXYPlot().getDomainAxis().setLabelFont(axisFont);
		chart.getXYPlot().getRangeAxis().setLabelFont(axisFont);
		Font tickFont = new Font(Font.SANS_SERIF,Font.PLAIN,10);
		chart.getXYPlot().getDomainAxis().setTickLabelFont(tickFont);
		chart.getXYPlot().getRangeAxis().setTickLabelFont(tickFont);
		
		if(aPercentageChart) {
			chart.getXYPlot().getRangeAxis().setAutoRange(false);
			chart.getXYPlot().getRangeAxis().setRange(0, 100);
		}
		
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
		chart.getXYPlot().setBackgroundPaint(Color.WHITE);
		chart.setBackgroundPaint(new Color(230,230,230));
		chart.getXYPlot().setDomainGridlinePaint(new Color(214,139,74));
		chart.getXYPlot().setRangeGridlinePaint(new Color(214,139,74));
		chart.getXYPlot().setDomainMinorGridlinesVisible(true);
		chart.getXYPlot().setDomainMinorGridlinePaint(Color.red);

		// prepare legend
		LegendItemSource lis = new LegendItemSource() {			
			LegendItemCollection lic = new LegendItemCollection();			
			public LegendItemCollection getLegendItems() {
				return lic;
			}
		};
		
		double minDomainAxis = Double.MAX_VALUE;
		double maxDomainAxis = Double.NEGATIVE_INFINITY;		
		double minNumberOfPoints = Double.MAX_VALUE;
		double minDomainAxisSeries, maxDomainAxisSeries, numberOfSeriesPoints;
		boolean anyBarRendered = false;
		
		// prepare series
		for (int i=0; i<numberOfSeries; i++){
			minDomainAxisSeries = series.get(i).get(0).getX();
			maxDomainAxisSeries = series.get(i).get(series.get(i).getSize()-1).getX();
			numberOfSeriesPoints = series.get(i).getSize();
			
			if(minDomainAxisSeries<minDomainAxis) minDomainAxis = minDomainAxisSeries;
			if(maxDomainAxisSeries>maxDomainAxis) maxDomainAxis = maxDomainAxisSeries;
			if(numberOfSeriesPoints<minNumberOfPoints) minNumberOfPoints = numberOfSeriesPoints;

            if(renderers.get(i) instanceof DeviationRenderer){
				YIntervalSeries currentIntervalSeries = new YIntervalSeries(names.get(i));
				
				// add points
				XYIntervalItem item;
				for(int j=0; j<series.get(i).getSize(); j++){
					item = (XYIntervalItem) series.get(i).get(j);					
					currentIntervalSeries.add(item.getX(),item.getY(),item.getyDownDeviation(),item.getyUpDeviation());
				}
				
				// add series
				YIntervalSeriesCollection data = new YIntervalSeriesCollection();
				data.addSeries(currentIntervalSeries);
				chart.getXYPlot().setDataset(i,data);
            } else {
				if(renderers.get(i) instanceof XYBarRenderer) anyBarRendered = true;
				// init series
				XYSeries currentSeries = new XYSeries(names.get(i));
				
				// add points
				for(int j=0; j<series.get(i).getSize(); j++){
					currentSeries.add(series.get(i).get(j).getX(),series.get(i).get(j).getY());
				}
				
				// add series
				chart.getXYPlot().setDataset(i, new XYSeriesCollection(currentSeries));
            }
			
			// set stroke
			renderers.get(i).setSeriesStroke(0, strokes.get(i));

			// set color
			renderers.get(i).setSeriesPaint(0, colors.get(i));

            // set tooltip generator
            renderers.get(i).setBaseToolTipGenerator(toolTipGenerator);

            // add renderer
            chart.getXYPlot().setRenderer(i,renderers.get(i));

			// add legend item
			LegendItem legendItem = new LegendItem(names.get(i));
			legendItem.setLabelFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));
			legendItem.setLabelPaint(Color.darkGray);
			legendItem.setFillPaint(colors.get(i));
			lis.getLegendItems().add(legendItem);
		}

		// finalize legend
        if (showLegend) {
            chart.addLegend(new LegendTitle(lis));
            chart.getLegend().setMargin(2,0,-7,10);
            chart.getLegend().setVerticalAlignment(VerticalAlignment.TOP);
            chart.getLegend().setHorizontalAlignment(HorizontalAlignment.RIGHT);
            chart.getLegend().setLegendItemGraphicPadding(new RectangleInsets(5,5,5,5));
        }

		// adjust axis limits
		double abit = 0;
		if(anyBarRendered) abit = ((maxDomainAxis-minDomainAxis)/minNumberOfPoints)/2.0;
		
		if(adjustDomainAxisLimits){
			chart.getXYPlot().getDomainAxis().setRange(minDomainAxis-abit,maxDomainAxis+abit); 
		}
	}

	/**
	 * @return the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}


	/**
	 * @param aPercentageChart the aPercentageChart to set
	 */
	public void setPercentageChart(boolean aPercentageChart) {
		this.aPercentageChart = aPercentageChart;
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

    public void setSeriesToExportIndex(int seriesToExportIndex) {
        this.seriesToExportIndex = seriesToExportIndex;
    }

    @Override
    void exportData(BufferedWriter dataWriter) throws IOException {

        dataWriter.write("#" + xLabel);

        if (seriesToExportIndex != -1 && names.size() > seriesToExportIndex && series.size() > seriesToExportIndex ) {
            //export only single series
            dataWriter.write("\t" + names.get(seriesToExportIndex) + "\n");
            XYVector s = series.get(seriesToExportIndex);
            int len = s.getSize();
            for (int i = 0; i < len; ++i) {
                dataWriter.write(s.get(i).getX() + "\t" + s.get(i).getY() + "\n");
            }


        } else {

            //Export whole data set

            for (String name : names) {
                dataWriter.write("\t" + name);
            }
            dataWriter.write("\n");

            int len = series.get(0).getSize();
            double[] xData = series.get(0).getXVector();

            for (int i = 0; i < len; ++i) {
                dataWriter.write(Double.toString(xData[i]));
                for (XYVector data : series) {
                    if (data.getSize() > i) {
                        dataWriter.write("\t" + data.get(i).getY());
                    }
                }
                dataWriter.write("\n");
            }

        }

    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }


}