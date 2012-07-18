/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2012 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.gui.dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.PopupKeyListener;

/**
 * Class to show the Information of the application.
 * @author Luis Miguel Cruz
 */
public class AboutDialog extends JDialog {
	/** Serial Version ID */
	private static final long serialVersionUID = 8521724385994032467L;
	
	/** Container to draw dynamic elements into the GroupLayout of the main frame */
	private Container container;
	
	private AbstractAction abstractActionCloseAbout;
	
	/** Logger to print information */
	protected Logger logger;
	
	/** Dimension for the jdialog */
	private Dimension dim;
	
	private HomeFrame homeFrame;
	

	public AboutDialog(HomeFrame frame) {
		super(frame);
		logger = new Logger(this.getClass().getName());
		this.homeFrame = frame;
		initGUI();
	}
	
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
			getContentPane().setLayout(thisLayout);
			thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup().addGap(550));
			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup().addGap(400));
			
			container = getContentPane();
			
			dim = new Dimension(550, 400);

			pack();
			this.setSize(dim);
			
			loadInformation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadInformation(){
		Image image;
		try {
			image = ImageIO.read(getClass().getResource(
					Constants.pathImages + "logo.png"));
			
			PopupKeyListener keyListener = new PopupKeyListener(homeFrame, this, null);
			
			JLabel logoBioInfo = new JLabel(
					new ImageIcon(
							image.getScaledInstance(dim.width, -1, Image.SCALE_SMOOTH)), JLabel.CENTER);
			logoBioInfo.setSize(logoBioInfo.getPreferredSize());
			logoBioInfo.setOpaque(false);
			logoBioInfo.setLocation(0, 0);
			container.add(logoBioInfo);
			
			image = ImageIO.read(getClass().getResource(
					Constants.pathImages + "cipf_alpha.gif"));
			
			JLabel imageCipf = new JLabel(new ImageIcon(image.getScaledInstance(120, -1, Image.SCALE_SMOOTH)), JLabel.CENTER);
			imageCipf.setSize(imageCipf.getPreferredSize());
			imageCipf.setOpaque(false);
			imageCipf.setLocation(
					Constants.marginLeftForElement,
					70);
			container.add(imageCipf);
			
			int location = imageCipf.getWidth()+ 2*(Constants.marginLeftForElement);
			JTextArea textAboutTitle = new JTextArea();
			textAboutTitle.setText("     Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin tortor ligula, convallis eu " +
					"fermentum sed, tempus a justo. Donec porta leo quis quam commodo vehicula. Cras vel hendrerit purus. " +
					"Praesent vel laoreet lacus. Nam sit amet lacinia ante.");
			textAboutTitle.setSize(dim.width - location - Constants.marginLeftForElement, imageCipf.getHeight());
			textAboutTitle.setLocation(location, 85);
			textAboutTitle.setBackground(null);
			textAboutTitle.setOpaque(false);
			textAboutTitle.setLineWrap(true);
			textAboutTitle.setWrapStyleWord(true);
			textAboutTitle.setEditable(false);
			textAboutTitle.addKeyListener(keyListener);
			container.add(textAboutTitle);
			
			JTextArea textAbout = new JTextArea();
			textAbout.setText("     Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin tortor ligula, convallis eu " +
					"fermentum sed, tempus a justo. Donec porta leo quis quam commodo vehicula. Cras vel hendrerit purus. " +
					"Praesent vel laoreet lacus. Nam sit amet lacinia ante. Nunc quam ante, euismod nec scelerisque vitae, " +
					"condimentum fermentum dui. Donec porta cursus tortor et iaculis. Vivamus cursus congue turpis, eget cursus " +
					"tortor iaculis ac. Donec tempor bibendum placerat. In in risus erat, ut pulvinar dui. Maecenas id nisl " +
					"eget libero cursus dignissim. Etiam venenatis cursus neque, at imperdiet nisi hendrerit sit amet. " +
					"Phasellus vestibulum gravida posuere. Vivamus gravida mauris at nulla semper a dignissim purus euismod. " +
					"Aliquam ut libero eget eros pulvinar ornare eget vulputate augue. Praesent nisl dolor, convallis in " +
					"congue et, posuere at est.");
			textAbout.setSize(dim.width - 2*(Constants.marginLeftForElement),
					170);
			textAbout.setLocation(
					Constants.marginLeftForElement,
					textAboutTitle.getY() + textAboutTitle.getHeight() + 5);
			textAbout.setBackground(null);
			textAbout.setOpaque(false);
			textAbout.setLineWrap(true);
			textAbout.setWrapStyleWord(true);
			textAbout.setEditable(false);
			textAbout.addKeyListener(keyListener);
			container.add(textAbout);
			
			JButton	buttonOk = new JButton();
			buttonOk.setText("OK");
			buttonOk.setSize(buttonOk.getPreferredSize());
			buttonOk.setAction(getAbstractActionCloseAbout());
			buttonOk.setLocation(
					dim.width - buttonOk.getWidth() - 50,
					dim.height - buttonOk.getHeight() - 50);
			buttonOk.setOpaque(true);
			buttonOk.addKeyListener(keyListener);
			container.add(buttonOk);
			
		} catch (IOException e) {
			logger.error("Cannot load the image for about info");
		}
	}
	
	private AbstractAction getAbstractActionCloseAbout() {
		if(abstractActionCloseAbout == null) {
			abstractActionCloseAbout = new AbstractAction("OK", null) {
				private static final long serialVersionUID = 1624997520647406057L;

				public void actionPerformed(ActionEvent evt) {
					dispose();
				}
			};
		}
		return abstractActionCloseAbout;
	}

}
