package org.bioinfo.ngs.qc.qualimap.beans;

import java.util.ArrayList;
import java.util.List;

public class XYVector {

	private double maxValue;
	private List<XYItem> items;
	
	public XYVector(){
		items = new ArrayList<XYItem>();
		maxValue = 0;
	}
	
	public XYVector(double[] x, double[] y){
		items = new ArrayList<XYItem>();
		for(int i=0; i<x.length; i++){
			items.add(new XYItem(x[i], y[i]));
			if(y[i]>maxValue) maxValue = y[i];
		}
	}
	
	public XYVector(List<Double> x, List<Double> y){		
		items = new ArrayList<XYItem>();
		for(int i=0; i<x.size(); i++){
			items.add(new XYItem(x.get(i), y.get(i)));
			if(y.get(i)>maxValue) maxValue = y.get(i);
		}
	}
	
	public XYVector(List<Double> x, List<Double> y, List<Double> deviation){
		this(x,y,deviation,true);
	}
	
	public XYVector(List<Double> x, List<Double> y, List<Double> deviation, boolean isDeviation){
		items = new ArrayList<XYItem>();
		double up,down;
		for(int i=0; i<x.size(); i++){
			if(isDeviation){
				items.add(new XYIntervalItem(x.get(i),x.get(i),x.get(i),y.get(i),y.get(i)-deviation.get(i),y.get(i)+deviation.get(i)));	
			} else {
				if(y.get(i)>deviation.get(i)){
					up = y.get(i);
					down = deviation.get(i);
				} else {
					up = deviation.get(i);
					down = y.get(i);
				}
				items.add(new XYIntervalItem(x.get(i),x.get(i),x.get(i),y.get(i),down,up));	
			}
			if(y.get(i)>maxValue) maxValue = y.get(i);
		}
	}
	
	public void addItem(XYItem item){
		items.add(item);
		if(item.getY()>maxValue) maxValue = item.getY();
	}
	
	public double[] getXVector(){
		double[] xs = new double[items.size()];
		for(int i=0; i<items.size(); i++){
			xs[i] = items.get(i).getX();
		}
		return xs;
	}
	
	public double[] getYVector(){
		double[] ys = new double[items.size()];
		for(int i=0; i<items.size(); i++){
			ys[i] = items.get(i).getY();
		}
		return ys;
	}
	
	public int getSize(){
		return items.size();
	}
	
	public XYItem get(int index){
		if(index<0 || index>items.size()) return null;
		else return items.get(index);
	}

	/**
	 * @return the maxValue
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}	
}