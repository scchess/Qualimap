package org.bioinfo.ngs.qc.qualimap.common;

import java.awt.Paint;

import org.jfree.chart.renderer.category.BarRenderer;

public class CustomBarRenderer extends BarRenderer {
	private static final long serialVersionUID = 4262785128320088720L;
	private Paint[] colors;

    public CustomBarRenderer(final Paint[] colors) {
        this.colors = colors;
        setShadowVisible(false);
        setDrawBarOutline(false);
    }
   
    public Paint getItemPaint(final int row, final int column) {
        return this.colors[column % this.colors.length];
    }   
}