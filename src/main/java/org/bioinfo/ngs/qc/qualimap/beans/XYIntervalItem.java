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