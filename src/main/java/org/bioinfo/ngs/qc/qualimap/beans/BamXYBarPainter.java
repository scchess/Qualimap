/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2013 Garcia-Alcalde et al.
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RectangularShape;
import java.io.Serializable;

import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.ui.RectangleEdge;

public class BamXYBarPainter implements XYBarPainter, Serializable {
	private static Color DEFAULT_BAR_COLOR = new Color(100,100,250,150);
	private static Color DEFAULT_BORDER_BAR_COLOR = new Color(50,50,50);
	private Color barColor;
	private Color borderBarColor;
	
	public BamXYBarPainter(){
		barColor = DEFAULT_BAR_COLOR;
		borderBarColor = DEFAULT_BORDER_BAR_COLOR;
	}

    public BamXYBarPainter(Color color){
		barColor = color;
		borderBarColor = DEFAULT_BORDER_BAR_COLOR;
	}


	@Override
	public void paintBar(Graphics2D g, XYBarRenderer renderer, int row, int column, RectangularShape shape, RectangleEdge edge) {
		// create shape		
		RectangularShape myshape = (RectangularShape)shape.clone(); 

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

}