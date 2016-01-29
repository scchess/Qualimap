/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2016 Garcia-Alcalde et al.
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.PopupKeyListener;
import org.bioinfo.ngs.qc.qualimap.main.NgsSmartMain;

/**
 * Class to show the Information of the application.
 * @author kokonech
 */
public class AboutDialog extends JDialog {
	/** Serial Version ID */
	private static final long serialVersionUID = 8521724385994032467L;
	
	private AbstractAction abstractActionCloseAbout;
	
	/** Logger to print information */
	protected Logger logger;
	
	private HomeFrame homeFrame;

    static class LinkMouseAdapter extends MouseAdapter {

        String url;
        public LinkMouseAdapter(String url) {
            this.url = url;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

	public AboutDialog(HomeFrame frame) {
		super(frame, "About");
		logger = new Logger(this.getClass().getName());
		this.homeFrame = frame;
		initGUI();
        pack();
        setResizable(false);
	}

    private void initGUI() {

        PopupKeyListener keyListener = new PopupKeyListener(homeFrame, this, null);

        getContentPane().setLayout(new MigLayout("insets 20"));

        String aboutText = "<html><body align=\"center\">QualiMap: software for evaluating next " +
                "generation sequencing<br> alignment data</body></html>";
        add(new JLabel(aboutText), "align center,wrap");
        String versionText = "Version: " + NgsSmartMain.APP_VERSION.replace("v.", "");
        add(new JLabel(versionText), "align center,wrap");
        String dateText =  "Build date: " + NgsSmartMain.APP_BUILT_DATE;
        add(new JLabel(dateText), "align center,wrap 20px");

        try {

            Image imageCipf  = ImageIO.read(getClass().getResource(
                    Constants.pathImages + "cipf_alpha.gif"));

            JLabel cipfLink = new JLabel(new ImageIcon(imageCipf.getScaledInstance(-1, 80, Image.SCALE_SMOOTH)),
                    JLabel.CENTER);
            cipfLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cipfLink.addMouseListener(new LinkMouseAdapter("http://www.cipf.es/"));

            Image imageMpiib  = ImageIO.read(getClass().getResource(
                    Constants.pathImages + "mpiib.jpeg"));


            JLabel mpiibLink = new JLabel(new ImageIcon(imageMpiib.getScaledInstance(-1, 80, Image.SCALE_SMOOTH)),
                    JLabel.CENTER);
            mpiibLink.setCursor( new Cursor(Cursor.HAND_CURSOR));
            mpiibLink.addMouseListener(new LinkMouseAdapter("http://www.mpiib-berlin.mpg.de/"));

            add(cipfLink, "align center, wrap 20px");
            add(mpiibLink, "align center, wrap 20px");

        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton	buttonOk = new JButton();
		buttonOk.setText("OK");
		buttonOk.setAction(getAbstractActionCloseAbout());
		buttonOk.addKeyListener(keyListener);
		add(buttonOk, "center");


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
