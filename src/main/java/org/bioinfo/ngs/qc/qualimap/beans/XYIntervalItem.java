/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2014 Garcia-Alcalde et al.
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

public class XYIntervalItem extends XYItem {
	private double xUpDeviation;
	private double xDownDeviation;
	private double yUpDeviation;
	private double yDownDeviation;
	
	public XYIntervalItem(double x, double xDownDeviation, double xUpDeviation, double y, double yDownDeviation, double yUpDeviation){
		super(x,y);
		this.xDownDeviation = xDownDeviation;
		this.xUpDeviation = xUpDeviation;
		this.yDownDeviation = yDownDeviation;
		this.yUpDeviation = yUpDeviation;
	}

	/**
	 * @return the xUpDeviation
	 */
	public double getxUpDeviation() {
		return xUpDeviation;
	}

	/**
	 * @param xUpDeviation the xUpDeviation to set
	 */
	public void setxUpDeviation(double xUpDeviation) {
		this.xUpDeviation = xUpDeviation;
	}

	/**
	 * @return the xDownDeviation
	 */
	public double getxDownDeviation() {
		return xDownDeviation;
	}

	/**
	 * @param xDownDeviation the xDownDeviation to set
	 */
	public void setxDownDeviation(double xDownDeviation) {
		this.xDownDeviation = xDownDeviation;
	}

	/**
	 * @return the yUpDeviation
	 */
	public double getyUpDeviation() {
		return yUpDeviation;
	}

	/**
	 * @param yUpDeviation the yUpDeviation to set
	 */
	public void setyUpDeviation(double yUpDeviation) {
		this.yUpDeviation = yUpDeviation;
	}

	/**
	 * @return the yDownDeviation
	 */
	public double getyDownDeviation() {
		return yDownDeviation;
	}

	/**
	 * @param yDownDeviation the yDownDeviation to set
	 */
	public void setyDownDeviation(double yDownDeviation) {
		this.yDownDeviation = yDownDeviation;
	}	
}