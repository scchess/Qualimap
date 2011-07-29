package org.bioinfo.ngs.qc.qualimap.gui.utils;

import javax.swing.JComponent;

/**
 * Class to manage the reference coordinates of a reference field, to know
 * where to put the next field.
 * @author Luis Miguel Cruz
 */
public class ReferencePosition extends JComponent{
	private static final long serialVersionUID = -4345710003046866348L;
	
	JComponent component;
	
	public ReferencePosition(){
		component = null;
	}
	
	public ReferencePosition(JComponent component){
		this.component = component;
	}
	
	
	// ******************************************************************************************
	// ********************************* GETTERS / SETTERS **************************************
	// ******************************************************************************************
	public JComponent getComponent() {
		return component;
	}

	public void setComponent(JComponent component) {
		this.component = component;
	}
	
	public int getX() {
		return component.getLocation().x;
	}

	public void setX(Integer x) {
		this.component.setLocation(x, component.getLocation().y);
	}

	public int getY() {
		return component.getLocation().y;
	}

	public void setY(Integer y) {
		this.component.setLocation(component.getLocation().x, y);
	}
	
	public int getWidth(){
		return this.component.getWidth();
	}
	
	public void setWidth(int width){
		this.component.setSize(width, this.component.getHeight());
	}
	
	public int getHeight(){
		return this.component.getHeight();
	}
	
	public void setHeight(int height){
		this.component.setSize(this.component.getWidth(), height);
	}
}

