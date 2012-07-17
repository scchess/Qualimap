package org.bioinfo.ngs.qc.qualimap.common;

public class UniqueID {
	static long current= System.currentTimeMillis();
	static public synchronized long get(){
		return current++;
	}
}
