package org.bioinfo.ngs.qc.qualimap.gui.utils;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Class to manage the graphic image to display in the application 
 * @author Luis Miguel Cruz
 */
public class GraphicImagePanel extends JPanel  {
	/** serial version UID */
	private static final long serialVersionUID = 1L;
	
	private BufferedImage bufferedImage;
	private Image image;
	

    /**
     * Paint the component into the original window
     */
    public void paintComponent(Graphics g) {
    	if(image!=null){
    		g.drawImage(image, 0, 0, null);
    	}                
    }
    
    /**
     * Set the image to display
     * @param original BufferedImage with the image
     */
    public void setImage(BufferedImage original){
    	this.bufferedImage = original;
    }
    
    /**
     * Resize an image with the dimensions wanted
	 * @param width integer with the image width
     * @param height integer with the image height
     */
    public void resizeImage(int width, int height){
    	int scale = Image.SCALE_REPLICATE;
    	if(width < Constants.GRAPHIC_TO_SAVE_WIDTH || height < Constants.GRAPHIC_TO_SAVE_HEIGHT){
    		scale = Image.SCALE_SMOOTH;
    	}
    	this.image = bufferedImage.getScaledInstance(width, height, scale);
    }

}
