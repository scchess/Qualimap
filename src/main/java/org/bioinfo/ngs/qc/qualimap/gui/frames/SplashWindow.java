/*******************************************************************************
 * Copyright (c) 2009 Stefan Götz.
 * All rights reserved. 
 * 
 * Contributors:
 *     Stefan Götz - initial API and implementation
 * 
 * The software is provided "as is", without any warranty of any kind.
 ******************************************************************************/


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
		this.setVisible(true);
		final Thread splashThread = new Thread(waitRunner, "SplashThread");
		splashThread.start();
	}
}

