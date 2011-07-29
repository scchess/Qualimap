package org.bioinfo.ngs.qc.qualimap.beans;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RectangularShape;

import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.ui.RectangleEdge;

public class BamXYBarPainter implements XYBarPainter{
	private double barwidth;
	private int numberOfBars = 20;
	private static Color DEFAULT_BAR_COLOR = new Color(100,100,250,150);
	private static Color DEFAULT_BORDER_BAR_COLOR = new Color(50,50,50);
	private Color barColor;
	private Color borderBarColor;
	
	public BamXYBarPainter(int numberOfBars){
		this.numberOfBars = numberOfBars;
		barColor = DEFAULT_BAR_COLOR;
		borderBarColor = DEFAULT_BORDER_BAR_COLOR;
	}
	
	@Override
	public void paintBar(Graphics2D g, XYBarRenderer renderer, int row, int column, RectangularShape shape, RectangleEdge edge) {
		// create shape		
		RectangularShape myshape = (RectangularShape)shape.clone(); 
		barwidth = (g.getClip().getBounds().getWidth()/(double)numberOfBars)*0.9;
		myshape.setFrame(shape.getMinX(),shape.getMinY(),barwidth,shape.getHeight());
		
		// fill
		g.setColor(barColor);
		g.fill(myshape);
		
		// border
		g.setColor(borderBarColor);
		g.setStroke(new BasicStroke(1.5f));
		g.draw(myshape);
	}
	
	@Override
	public void paintBarShadow(Graphics2D arg0, XYBarRenderer arg1, int arg2, int arg3, RectangularShape arg4, RectangleEdge arg5, boolean arg6) {
		// NOTHING TO DO				
	}
	/**
	 * @return the barwidth
	 */
	public double getBarwidth() {
		return barwidth;
	}
	/**
	 * @param barwidth the barwidth to set
	 */
	public void setBarwidth(double barwidth) {
		this.barwidth = barwidth;
	}

	/**
	 * @return the barColor
	 */
	public Color getBarColor() {
		return barColor;
	}

	/**
	 * @param barColor the barColor to set
	 */
	public void setBarColor(Color barColor) {
		this.barColor = barColor;
	}

	/**
	 * @return the borderBarColor
	 */
	public Color getBorderBarColor() {
		return borderBarColor;
	}

	/**
	 * @param borderBarColor the borderBarColor to set
	 */
	public void setBorderBarColor(Color borderBarColor) {
		this.borderBarColor = borderBarColor;
	}			
}