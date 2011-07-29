package org.bioinfo.ngs.qc.qualimap.utils;

import org.bioinfo.commons.exec.Command;
import org.bioinfo.commons.exec.SingleProcess;

public class Rlauncher{
	private String command;
	private String launchMode;
	
	public Rlauncher(String command, String launchMode){
		this.command = command;
		if (! launchMode.equalsIgnoreCase("Rscript") && ! launchMode.equalsIgnoreCase("BATCH")){
			System.out.println("Rlauncher class. ERROR. launchMode = " + launchMode + ". Expected 'Rscript' or 'BATCH'");
			System.exit(-1);
		}else{
			this.launchMode = launchMode;
		}
	}
	
	public void run(){
		Command cmd = new Command(toString());
		System.out.println(toString());
		SingleProcess process = new SingleProcess(cmd);
		process.getRunnableProcess().run();
		
		System.out.println("output: " + process.getRunnableProcess().getOutput());
		System.err.println("err: " + process.getRunnableProcess().getError());
	}
	
	public String toString(){
		String cmd = null;
		if (launchMode.equalsIgnoreCase("Rscript")){
			cmd = "Rscript " + command;
		}else{
			// TODO!
		}
		
		return cmd;
	}

}
