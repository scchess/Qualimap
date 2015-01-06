/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.gui.utils;

import org.bioinfo.ngs.qc.qualimap.common.Constants;

import java.awt.*;
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
