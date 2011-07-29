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
			if(jProgressBar == null || (jProgressBar != null && !jProgressBar.isVisible())){
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
