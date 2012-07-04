package org.bioinfo.ngs.qc.qualimap.beans;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by kokonech
 * Date: 6/27/12
 * Time: 12:00 PM
 */
public class BamQCBarChart extends ChartRawDataWriter {


    String xLabel, yLabel, title, subTitle;
    Collection<CategoryItem> series;
    JFreeChart chart;

    static class CustomRenderer extends BarRenderer {

        /** The colors. */
        private Paint[] colors;

        /**
         * Creates a new renderer.
         *
         * @param colors  the colors.
         */
        public CustomRenderer(final Paint[] colors) {
            this.colors = colors;
        }

        /**
         * Returns the paint for an item.  Overrides the default behaviour inherited from
         * AbstractSeriesRenderer.
         *
         * @param row  the series.
         * @param column  the category.
         *
         * @return The item color.
         */
        public Paint getItemPaint(final int row, final int column) {
            return this.colors[column % this.colors.length];
        }
    }

    public BamQCBarChart(String title, String subTitle, String xLabel, String yLabel,
                         Collection<CategoryItem> series) {
        this.title = title;
        this.subTitle = subTitle;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.series = series;

    }



    public void render() throws IOException {

		// create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (CategoryItem item : series) {
            dataset.addValue(item.getValue(), "data", item.getName());
        }

        chart = ChartFactory.createBarChart(
            title,         // chart title
            xLabel,         // domain axis label
            yLabel,             // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips?
            false                     // URLs?
        );

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

        CategoryPlot plot = chart.getCategoryPlot();
		// axis style
		Font axisFont = new Font(Font.SANS_SERIF,Font.PLAIN,11);
		plot.getDomainAxis().setLabelFont(axisFont);
		plot.getRangeAxis().setLabelFont(axisFont);
		Font tickFont = new Font(Font.SANS_SERIF,Font.PLAIN,10);
		plot.getDomainAxis().setTickLabelFont(tickFont);
		plot.getRangeAxis().setTickLabelFont(tickFont);

        //plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
        plot.getDomainAxis().setMaximumCategoryLabelWidthRatio(100.0f);

         final CategoryItemRenderer renderer = new CustomRenderer(
            new Paint[] {Color.red, Color.blue, Color.green,
                Color.black, Color.cyan, Color.yellow }
        );

        plot.setRenderer(renderer);

		// grid
		plot.setBackgroundPaint(Color.WHITE);
		chart.setBackgroundPaint(new Color(230,230,230));
		plot.setDomainGridlinePaint(new Color(214,139,74));
		plot.setRangeGridlinePaint(new Color(214,139,74));


	}

    @Override
    void exportData(BufferedWriter dataWriter) throws IOException {
        dataWriter.write("#" + xLabel + "\t" + yLabel + "\n");

        for (CategoryItem item : series) {
           dataWriter.write(item.getName() + "\t" + Integer.toString(item.getValue()) + "\n");
        }


    }

    public JFreeChart getChart() {
        return chart;
    }
}
