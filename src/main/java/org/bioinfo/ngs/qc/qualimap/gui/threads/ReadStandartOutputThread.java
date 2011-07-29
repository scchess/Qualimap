package org.bioinfo.ngs.qc.qualimap.gui.threads;

import java.awt.Component;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.gui.panels.OpenFilePanel;
import org.bioinfo.ngs.qc.qualimap.process.BamQCSplitted;

public class ReadStandartOutputThread extends Thread {
	/** Logger to print information */
	protected Logger logger;

	/** Variable to manage the panel with the progress bar at the init */
	private OpenFilePanel openFilePanel;

	/** Variables that contains the bamqc */
	BamQCSplitted bamQc;

	private boolean threadDone;

	public ReadStandartOutputThread(String str, Component component,
			BamQCSplitted bamQc) {
		super(str);
		if (component instanceof OpenFilePanel) {
			this.openFilePanel = (OpenFilePanel) component;
		}
		this.bamQc = bamQc;
		this.threadDone = false;
	}

	/**
	 * Public method to run this thread. Its executed when an user call to
	 * method start over this thread.
	 */
	public void run() {
		String lastActionDone = null;

		try {
			while (!threadDone) {
				openFilePanel.getProgressBar().setValue((int)Math.round(bamQc.getProgress() * 0.85));
				if(lastActionDone == null || !bamQc.getLastActionDone().equalsIgnoreCase(lastActionDone)){
					openFilePanel.getProgressStream().setText(bamQc.getLastActionDone());	
				}
			
				// Sleep 100 ms
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public boolean isThreadDone() {
		return threadDone;
	}

	public void setThreadDone(boolean threadDone) {
		this.threadDone = threadDone;
	}
}
