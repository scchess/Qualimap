package org.bioinfo.ngs.qc.qualimap.common;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bioinfo.math.util.MathUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class GraphUtils {
	
	public static void saveChart(JFreeChart chart, String fileName, int width, int height) throws IOException{
		ChartUtilities.saveChartAsPNG(new File(fileName), chart, width, height);
	}
	
	public static JFreeChart getXYChartMap(List<Double> xs, HashMap<String,List<Double>> ys, 
			String title, String xLabel, String yLabel, HashMap<String,Color> seriesColors){
		XYSeriesCollection dataset = new XYSeriesCollection();
				 
		List<String> categories = getKeys(ys);
		
		for(String category:categories){
			XYSeries series = new XYSeries(category);
			
			for(int i=0; i<xs.size(); i++){
				series.add((double)xs.get(i),(double)ys.get(category).get(i));
			}
		
			dataset.addSeries(series);
		}		
		
		JFreeChart chart = ChartFactory.createXYLineChart(title, xLabel,yLabel, dataset, PlotOrientation.VERTICAL, true, false, false);
			
		for(int i=0; i<categories.size(); i++){
			chart.getXYPlot().getRenderer().setSeriesPaint(i,seriesColors.get(categories.get(i)));
			chart.getXYPlot().getRenderer().setSeriesStroke(i,new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,1.0f, new float[] {10.0f, 6.0f}, 0.0f));			
		}
		
		chart.setPadding(new RectangleInsets(5,10,15,10));
				
		return chart;
	}
	
	public static void saveCategoryChart(String fileName, int width, int height, 
			List<String> categories, double[] values, String title, String subTitle, 
			String categoryLabel, String valueLabel, int limit, 
			HashMap<String,String> descriptions,
			HashMap<String,Color> categoryColors) throws IOException{
		JFreeChart chart = getCategoryChart(categories,values,title,subTitle,categoryLabel,valueLabel,limit,descriptions,categoryColors);
		saveChart(chart, fileName, width, height);
	}
	
	public static JFreeChart getCategoryChart(List<String> categories, double[] values,
			String title, String subTitle, String categoryLabel, String valueLabel,
			int limit,HashMap<String,String> descriptions, HashMap<String,Color> categoryColors){
		// compute total hits
		double totalHits =  MathUtils.sum(values);
		int n = Math.min(limit,categories.size());
		int on = 1;
		if(categories.size()<=limit) on = 0;
		
		// init dataset
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
				
		// init colors
		Paint[] colors = new Paint[n + on];
		
		String description;
		int cont = 0;
		for(int i=0; i<n; i++){
			// division
			description = "";
			if(descriptions!=null && descriptions.containsKey(categories.get(i))) description = " (" + descriptions.get(categories.get(i)) + ")";

			// add data
			dataset.setValue(values[i],"x",categories.get(i) + description);

			if(categoryColors!=null){
				if(categoryColors.containsKey(categories.get(i))){
					colors[i] = categoryColors.get(categories.get(i));
				} else {
					colors[i] = new Color(200,200,200);
				}
			}
			
			cont+=values[i];
		}
					
		// compute others contribution
		if(on>0){
			dataset.setValue(totalHits-cont, "x","Other");
			if(categoryColors!=null){
				colors[n] = new Color(200,200,200);	
			}						
		}
		
		// create chart		
		JFreeChart chart = initCategoryChart(title, subTitle, categoryLabel, valueLabel, PlotOrientation.HORIZONTAL, colors); 
		
		// set dataset
		chart.getCategoryPlot().setDataset(dataset);
		
		// set style
		Font axisFont = new Font(Font.SANS_SERIF,Font.ITALIC,12);
		chart.getCategoryPlot().getDomainAxis().setLabelFont(axisFont);		
		chart.getCategoryPlot().getRangeAxis().setLabelFont(axisFont);		
		Font tickFont = new Font(Font.SANS_SERIF,Font.PLAIN,11);
		chart.getCategoryPlot().getDomainAxis().setTickLabelFont(tickFont);
		
		return chart;
	}
	
	/*
	 * xy area chart
	 */	
	public static void saveXYAreaChart(String fileName, int width, int height,double[] x, double[] y, String title, String subTitle, String xLabel, String yLabel) throws IOException{
		JFreeChart chart = getXYAreaChart(x, y, title, subTitle, xLabel, yLabel);
		ChartUtilities.saveChartAsPNG(new File(fileName), chart, width, height);
	}

	public static JFreeChart getXYAreaChart(double[] x, double[] y, String title, String subTitle, String xLabel, String yLabel){
		System.err.println(x.length + " " + y.length);
		XYSeries series = new XYSeries("");
		for(int i=0; i<x.length; i++){
			series.add(x[i], y[i]);
		}
		
		XYSeriesCollection dataset = new XYSeriesCollection(series);
		
		JFreeChart chart = initXYAreaChart(title, subTitle, xLabel, yLabel, PlotOrientation.VERTICAL);
				
		chart.getXYPlot().setDataset(dataset);
	
		return chart;
	}
	
	public static JFreeChart initXYAreaChart(String title, String subTitle, String xLabel,
			String yLabel, PlotOrientation plotOrientation){
		// init chart
		JFreeChart chart = ChartFactory.createXYAreaChart(title,xLabel,yLabel, null, plotOrientation, true, true, false);
		
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
		
		// set style
		Font axisFont = new Font(Font.SANS_SERIF,Font.PLAIN,11);
		chart.getXYPlot().getDomainAxis().setLabelFont(axisFont);
		chart.getXYPlot().getRangeAxis().setLabelFont(axisFont);
		Font tickFont = new Font(Font.SANS_SERIF,Font.PLAIN,11);
		chart.getXYPlot().getDomainAxis().setTickLabelFont(tickFont);
		
		// grid
		chart.getXYPlot().setBackgroundPaint(Color.WHITE);
		chart.setBackgroundPaint(new Color(230,230,230));
		chart.getXYPlot().setDomainGridlinePaint(new Color(214,139,74));
		chart.getXYPlot().setRangeGridlinePaint(new Color(214,139,74));
		chart.getXYPlot().setDomainMinorGridlinesVisible(true);
		chart.getXYPlot().setDomainMinorGridlinePaint(Color.red);
		
		return chart;
	}
	
	private static JFreeChart initCategoryChart(String title, String subTitle, String xLabel,
			String yLabel, PlotOrientation plotOrientation, Paint[] colors){
		// init chart
		JFreeChart chart = ChartFactory.createBarChart(title,xLabel,yLabel, null, plotOrientation, false, false, false);
		
		// init titles
		chart.setTitle(title);
		TextTitle sub = new TextTitle(subTitle);
		sub.setPadding(5, 5, 15, 5);
		chart.setSubtitles(Arrays.asList(sub));
		
		// init renderer
		if(colors!=null){
			CategoryItemRenderer renderer = new CustomBarRenderer(colors);		
			chart.getCategoryPlot().setRenderer(renderer);
		}
				
		// other params
		chart.setPadding(new RectangleInsets(30,30,30,30));		
				
		return chart;
		
	}

	private static List<String> getKeys(HashMap<String,?> map){
		List<String> keys = new ArrayList<String>(map.size());
		Object[] raw = map.keySet().toArray();
		for(int i=0; i<raw.length; i++) keys.add((String)raw[i]);
		return keys;
	}	
}