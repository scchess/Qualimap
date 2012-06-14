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
    // org.bioinfo.ntools.main params
	private String title;
	private String subTitle;
	private String xLabel;
	private String yLabel;

	// histogram series
	XYVector data;
    Map<Double,String> barNames;

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


    public BamQCHistogramChart(String title,String subTitle, String xLabel, String yLabel,
                               XYVector data, Map<Double,String> barNames){

		// main params
		this.title = title;
		this.subTitle = subTitle;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
        this.data = data;
        this.barNames = barNames;

		numberOfBins = 50;
		maxValue = 100;
		adjustDomainAxisLimits = true;

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
		//chart = ChartFactory.createXYBarChart(title, xLabel, false, yLabel, null, PlotOrientation.VERTICAL, true, true, false);
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
		/*if(domainAxisIntegerTicks){
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
        }*/


		// grid
		plot.setBackgroundPaint(Color.WHITE);
		chart.setBackgroundPaint(new Color(230,230,230));
		plot.setDomainGridlinePaint(new Color(214,139,74));
		plot.setRangeGridlinePaint(new Color(214,139,74));



		// prepare series
		/*(if(!zoomed && histograms.get(0).getSize() > 0) {
			maxValue = histograms.get(0).get(histograms.get(0).getSize()-1).getX();
		}*/

		/*double inc = maxValue/(double)numberOfBins;
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
		double next = 0;

		for(int i=0; i<values.length; i++){
			if(cumulative){
				acum += (values[i]/total)*100.0;
				next = acum;
			} else {
				next = values[i];
			}
			series.add(covs[i],next);
		}*/


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
		BamXYBarPainter barPainter = new BamXYBarPainter(len);
		renderer.setBarPainter(barPainter);
		plot.setRenderer(renderer);

		// adjust axis limits
		//if(adjustDomainAxisLimits && histograms.get(0).getSize() > 0){
        //    double minDomainAxis = histograms.get(0).get(0).getX() - inc/2.0;
		//    double maxDomainAxis = maxValue + inc/2.0;
		//	chart.getXYPlot().getDomainAxis().setRange(minDomainAxis,maxDomainAxis);
		//}
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
