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
package org.bioinfo.ngs.qc.qualimap.gui.utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 * Class that do the actions to do when a user press a key in
 * a Popup dialog. 
 * @author Luis Miguel Cruz
 */
public class PopupKeyListener implements KeyListener{
	private JDialog jDialog;
	
	private JFrame homeFrame;
	
	private JProgressBar jProgressBar;
	
	public PopupKeyListener(JFrame homeFrame, JDialog jDialog, JProgressBar jProgressBar){
		super();
		this.jDialog = jDialog;
		this.homeFrame = homeFrame;
		this.jProgressBar = jProgressBar;
	}
	
	/**
	 * Function that executes when a key is pressed
	 */
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			// Close the jDialog
			if(jProgressBar == null || !jProgressBar.isVisible() ){
				jDialog.setVisible(false);
				homeFrame.remove(jDialog);
			} else {
				JOptionPane.showMessageDialog(null,
						"Unable to close the JDialog while its running", "Error", 0);
			}
        }
	}

	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent e) {}
}
