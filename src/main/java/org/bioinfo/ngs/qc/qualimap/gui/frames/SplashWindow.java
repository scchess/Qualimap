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
/*
 * Blast2GO
 * All Rights Reserved.
 */
package org.bioinfo.ngs.qc.qualimap.gui.frames;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;

import javax.swing.*;

public class SplashWindow extends JWindow {
	public SplashWindow(final File file, final HomeFrame f, final int waitTime) {
		super(f);
		final JLabel l = new JLabel(new ImageIcon(file.getPath()));
		this.rest(l, f, waitTime);
	}

	public SplashWindow(final URL fileurl, final HomeFrame f, final int waitTime) {
		super(f);
		final JLabel l = new JLabel(new ImageIcon(fileurl));
		this.rest(l, f, waitTime);
	}
	
	public SplashWindow(final String text, final HomeFrame f, final int waitTime) {
		super(f);
		final JLabel l = new JLabel(text);
		l.setFont(new Font("Courier", Font.PLAIN, 6));
		this.rest(l, f, waitTime);
	}

	private void rest(final JLabel l, final HomeFrame f, final int waitTime) {
		this.getContentPane().add(l, BorderLayout.CENTER);
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension labelSize = l.getPreferredSize();
		this.setBounds((screenSize.width - labelSize.width) / 2, (screenSize.height  - labelSize.height) / 2, labelSize.width, labelSize.height);
		this.validate();
		this.pack();
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(final MouseEvent e) {
				SplashWindow.this.setVisible(false);
				SplashWindow.this.dispose();
			}
		});
		final int pause = waitTime;
		final HomeFrame aMainGui = f;
		final Runnable closerRunner = new Runnable() {
			public void run() {
                SplashWindow.this.setVisible(false);
				aMainGui.setVisible(true);
				SplashWindow.this.dispose();
			}
		};
		final Runnable waitRunner = new Runnable() {
			public void run() {
				try {
					Thread.sleep(pause);
					SwingUtilities.invokeAndWait(closerRunner);
				} catch (Exception e) {
					System.out.println(e.getMessage());
					// can catch InvocationTargetException
					// can catch InterruptedException
				}
			}
		};
        this.setLocationRelativeTo(f);
		this.setVisible(true);
		final Thread splashThread = new Thread(waitRunner, "SplashThread");
		splashThread.start();
	}
}

